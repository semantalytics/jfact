package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.LL;
import static uk.ac.manchester.cs.jfact.kernel.DagTag.*;
import static uk.ac.manchester.cs.jfact.kernel.Redo.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;


import org.semanticweb.owlapi.reasoner.TimeOutException;

import uk.ac.manchester.cs.jfact.dep.DepSet;
import uk.ac.manchester.cs.jfact.dep.DepSetFactory;
import uk.ac.manchester.cs.jfact.dep.TSaveStack;
import uk.ac.manchester.cs.jfact.helpers.DLVertex;
import uk.ac.manchester.cs.jfact.helpers.FastSet;
import uk.ac.manchester.cs.jfact.helpers.FastSetFactory;
import uk.ac.manchester.cs.jfact.helpers.Helper;
import uk.ac.manchester.cs.jfact.helpers.IfDefs;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger;
import uk.ac.manchester.cs.jfact.helpers.Reference;
import uk.ac.manchester.cs.jfact.helpers.TsProcTimer;
import uk.ac.manchester.cs.jfact.helpers.UnreachableSituationException;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;
import uk.ac.manchester.cs.jfact.kernel.TBox.TSimpleRule;
import uk.ac.manchester.cs.jfact.kernel.ToDoList.ToDoEntry;
import uk.ac.manchester.cs.jfact.kernel.datatype.DataTypeReasoner;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheConst;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheIan;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheInterface;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheState;

public class DlSatTester {
	/** Enum for usage the Tactics to a ToDoEntry */
	abstract class BranchingContext {
		/** currently processed node */
		protected DlCompletionTree node;
		/** currently processed concept */
		protected ConceptWDep concept;
		/** positions of the Used members */
		//	protected int pUsedIndex, nUsedIndex;
		/** dependences for branching clashes */
		protected DepSet branchDep = DepSetFactory.create();

		/** empty c'tor */
		public BranchingContext() {
			node = null;
			concept = new ConceptWDep(Helper.bpINVALID);
		}

		/** init indeces (if necessary) */
		public abstract void init();

		/** give the next branching alternative */
		public abstract void nextOption();

		@Override
		public String toString() {
			return this.getClass().getSimpleName() + " dep '" + branchDep
					+ "' curconcept '" + concept + "' curnode '" + node + "'";
		}
	}

	abstract class BCChoose extends BranchingContext {
	}

	/** stack to keep BContext */
final class BCStack extends TSaveStack<BranchingContext> {
		/** pool for OR contexts */
		//private final List<BCOr> PoolOr = new ArrayList<BCOr>();
		/** pool for NN contexts */
		//private final List<BCNN> PoolNN = new ArrayList<BCNN>();
		/** pool for LE contexts */
		//private final List<BCLE> PoolLE = new ArrayList<BCLE>();
		/** pool for Choose contexts */
		//private final List<BCChoose> PoolCh = new ArrayList<BCChoose>();
		/** single entry for the barrier (good for nominal reasoner) */
		private final BCBarrier bcBarrier;

		/** push method to use */
		@Override
		public void push(BranchingContext p) {
			p.init();
			initBC(p);
			super.push(p);
			//System.out.println("BCStack.push() "+this.hashCode()+" " + list.getLast());
		}

		BCStack() {
			bcBarrier = new BCBarrier();
		}

		@Override
		public BranchingContext pop() {
			assert !isEmpty();
			//			if (isEmpty()) {
			//				System.out.println("BCStack.pop()");
			//			}
			BranchingContext pop = super.pop();
			//			if (isEmpty()) {
			//				System.out.println("BCStack.pop() "+this.hashCode()+" Empty stack");
			//			} else {
			//				System.out.println("BCStack.pop() "+this.hashCode()+" " + list.getLast());
			//			}
			return pop;
		}

		/** get BC for Or-rule */
		protected BranchingContext pushOr() {
			BCOr o = new BCOr();
			//PoolOr.add(o);
			push(o);
			return o;
		}

		/** get BC for NN-rule */
		protected BranchingContext pushNN() {
			BCNN n = new BCNN();
			//PoolNN.add(n);
			push(n);
			return n;
		}

		/** get BC for LE-rule */
		protected BranchingContext pushLE() {
			BCLE e = new BCLE();
			//PoolLE.add(e);
			push(e);
			return e;
		}

		/** get BC for Choose-rule */
		protected BranchingContext pushCh() {
			BCChoose c = new BCChoose() {
				@Override
				public void nextOption() {
				}

				@Override
				public void init() {
				}
			};
			//PoolCh.add(c);
			push(c);
			return c;
		}

		/** get BC for the barrier */
		protected BranchingContext pushBarrier() {
			push(bcBarrier);
			return bcBarrier;
		}

		/** clear all the pools */
		protected void clearPools() {
			//PoolOr.clear();
			//PoolNN.clear();
			//PoolLE.clear();
			//PoolCh.clear();
		}

		/** clear the stack and pools */
		@Override
		public void clear() {
			clearPools();
			super.clear();
		}
	}

	 final class BCBarrier extends BranchingContext {
		@Override
		public void init() {
		}

		@Override
		public void nextOption() {
		}
	}

	 final class BCLE extends BranchingContext {
		/** current branching index; used in several branching rules */
		private int branchIndex;
		/** index of a merge-candidate (in LE concept) */
		private int mergeCandIndex;
		/** vector of edges to be merged */
		private List<DlCompletionTreeArc> edges = new ArrayList<DlCompletionTreeArc>();

		/** init tag and indeces */
		@Override
		public void init() {
			branchIndex = 0;
			mergeCandIndex = 0;
		}

		/** correct mergeCandIndex after changing */
		protected void resetMCI() {
			mergeCandIndex = edges.size() - 1;
		}

		/** give the next branching alternative */
		@Override
		public void nextOption() {
			--mergeCandIndex;
			// get new merge candidate
			if (mergeCandIndex == branchIndex) {
				// nothing more can be mergeable to BI node
				++branchIndex;
				// change the candidate to merge to
				resetMCI();
			}
		}

		// access to the fields
		/** get FROM pointer to merge */
		protected DlCompletionTreeArc getFrom() {
			return edges.get(mergeCandIndex);
		}

		/** get FROM pointer to merge */
		protected DlCompletionTreeArc getTo() {
			return edges.get(branchIndex);
		}

		/** check if the LE has no option to process */
		protected boolean noMoreLEOptions() {
			return mergeCandIndex <= branchIndex;
		}

		protected List<DlCompletionTreeArc> getEdgesToMerge() {
			return edges;
		}

		protected void setEdgesToMerge(List<DlCompletionTreeArc> edgesToMerge) {
			edges = edgesToMerge;
		}
	}

	 final class BCNN extends BranchingContext {
		/** current branching index; used in several branching rules */
		private int branchIndex;

		/** init tag and indeces */
		@Override
		public void init() {
			branchIndex = 1;
		}

		/** give the next branching alternative */
		@Override
		public void nextOption() {
			++branchIndex;
		}

		// access to the fields
		/** check if the NN has no option to process */
		protected boolean noMoreNNOptions(int n) {
			return branchIndex > n;
		}

		protected int getBranchIndex() {
			return branchIndex;
		}

		public void setBranchIndex(int branchIndex) {
			this.branchIndex = branchIndex;
		}
	}

	 final class BCOr extends BranchingContext {
		/** current branching index; used in several branching rules */
		private int branchIndex;
		/** useful disjuncts (ready to add) in case of OR */
		private List<ConceptWDep> applicableOrEntries = new ArrayList<ConceptWDep>();

		/** init tag and indeces */
		@Override
		public void init() {
			branchIndex = 0;
		}

		/** give the next branching alternative */
		@Override
		public void nextOption() {
			++branchIndex;
		}

		// access to the fields
		/** check if the current processing OR entry is the last one */
		protected boolean isLastOrEntry() {
			return applicableOrEntries.size() == branchIndex + 1;
		}

		/** 1st element of OrIndex */
		protected List<ConceptWDep> orBeg() {
			return applicableOrEntries;
		}

		/** current element of OrIndex */
		protected ConceptWDep orCur() {
			return applicableOrEntries.get(branchIndex);//orBeg() + branchIndex; 
		}

		protected int getBranchIndex() {
			return branchIndex;
		}

		protected List<ConceptWDep> getApplicableOrEntries() {
			return applicableOrEntries;
		}

		protected void setApplicableOrEntries(
				List<ConceptWDep> applicableOrEntries) {
			this.applicableOrEntries = applicableOrEntries;
		}

		@Override
		public String toString() {
			StringBuilder o = new StringBuilder();
			o.append("BCOR ");
			o.append(branchIndex);
			o.append(" dep ");
			o.append(branchDep);
			o.append(" curconcept ");
			o.append(concept);
			o.append(" curnode ");
			o.append(node);
			o.append(" orentries [");
			o.append(applicableOrEntries);
			o.append("]");
			return o.toString();
		}
	}

	/** host TBox */
	protected final TBox tBox;
	/** link to dag from TBox */
	protected final DLDag DLHeap;
	/** all nominals defined in TBox */
	//	private final List<TIndividual> Nominals = new ArrayList<TIndividual>();
	/** all the reflexive roles */
	private final List<TRole> ReflexiveRoles = new ArrayList<TRole>();
	/** manager for all the dep-sets corresponding to a graph here */
	//private final TDepSetManager Manager;
	/** Completion Graph of tested concept(s) */
	protected final DlCompletionGraph CGraph;
	/** Todo list */
	private final ToDoList TODO = new ToDoList();
	/** reasoning subsystem for the datatypes */
	private final DataTypeReasoner DTReasoner;
	/** Used sets for pos- and neg- entries */
	//	private final FastSet nUsed = FastSetFactory.create();
	//	private final FastSet pUsed = FastSetFactory.create();
	private final FastSet used = new FastSet() {
		BitSet pos = new BitSet();

		public int[] toIntArray() {
			throw new UnsupportedOperationException();
		}

		public int size() {
			throw new UnsupportedOperationException();
		}

		public void removeAt(int o) {
			throw new UnsupportedOperationException();
		}

		public void removeAllValues(int... values) {
			throw new UnsupportedOperationException();
		}

		public void removeAll(int i, int end) {
			throw new UnsupportedOperationException();
		}

		public void remove(int o) {
			throw new UnsupportedOperationException();
		}

		public boolean isEmpty() {
			throw new UnsupportedOperationException();
		}

		public boolean intersect(FastSet f) {
			throw new UnsupportedOperationException();
		}

		public int get(int i) {
			throw new UnsupportedOperationException();
		}

		public boolean containsAny(FastSet c) {
			throw new UnsupportedOperationException();
		}

		public boolean containsAll(FastSet c) {
			throw new UnsupportedOperationException();
		}

		private final int asPositive(int p) {
			return p >= 0 ? 2 * p : 1 - 2 * p;
		}

		public boolean contains(int o) {
			return this.pos.get(asPositive(o));
		}

		public void clear() {
			pos = new BitSet();
		}

		public void addAll(FastSet c) {
			throw new UnsupportedOperationException();
		}

		public void add(int e) {
			this.pos.set(asPositive(e));
		}
	};//FastSetFactory.create FastSetFactory.create();
	/** GCI-related KB flags */
	private final TKBFlags GCIs;
	/** record nodes that were processed during Cascaded Cache construction */
	private final FastSet inProcess = FastSetFactory.create();
	/** timer for the SAT tests (ie, cache creation) */
	private final TsProcTimer satTimer = new TsProcTimer();
	/** timer for the SUB tests (ie, general subsumption) */
	private final TsProcTimer subTimer = new TsProcTimer();
	/** timer for a single test; use it as a timeout checker */
	private TsProcTimer testTimer = new TsProcTimer();
	/** SAT test timeout in seconds (if non-zero) */
	private long testTimeout;
	// save/restore option
	/** stack for the local reasoner's state */
	protected final BCStack Stack = new BCStack();
	/** context from the restored branching rule */
	protected BranchingContext bContext;
	/** index of last non-det situation */
	private int tryLevel;
	/** shift in order to determine the 1st non-det application */
	protected int nonDetShift;
	// current values
	/** currently processed CTree node */
	protected DlCompletionTree curNode;
	/** currently processed Concept */
	private ConceptWDep curConcept;
	/** last processed d-blocked node */
	//private DlCompletionTree dBlocked;
	/** size of the DAG with some extra space */
	private int dagSize;
	/** temporary array used in OR operation */
	private List<ConceptWDep> OrConceptsToTest = new ArrayList<ConceptWDep>();
	/** temporary array used in <= operations */
	private List<DlCompletionTreeArc> EdgesToMerge = new ArrayList<DlCompletionTreeArc>();
	/** contains clash set if clash is encountered in a node label */
	private DepSet clashSet = DepSetFactory.create();
	/** flag for switching semantic branching */
	private boolean useSemanticBranching;
	/** flag for switching backjumping */
	private boolean useBackjumping;
	/** whether or not check blocking status as late as possible */
	private boolean useLazyBlocking;
	/** flag for switching between Anywhere and Ancestor blockings */
	private boolean useAnywhereBlocking;
	// session status flags:
	/** true if nominal-related expansion rule was fired during reasoning */
	private boolean encounterNominal;
	/** flag to show if it is necessary to produce DT reasoning immideately */
	private boolean checkDataNode;
	/** cache for testing whether it's possible to non-expand newly created node */
	ModelCacheIan newNodeCache;
	/** auxilliary cache that is built from the edges of newly created node */
	ModelCacheIan newNodeEdges;
	Stats stats = new Stats();

	/** increment statistic counter */
	//	private static void incStat(AccumulatedStatistic stat) {
	//		stat.inc();
	//	}
	/**
	 * Adds ToDo entry which already exists in label of NODE. There is no need
	 * to add entry to label, but it is necessary to provide offset of existing
	 * concept. This is done by providing OFFSET of the concept in NODE's label
	 */
	private void addExistingToDoEntry(DlCompletionTree node, ConceptWDep C,
			final String reason /*= null*/) {
		int bp = C.getConcept();
		TODO.addEntry(node, DLHeap.get(bp).Type(), C);
		if (IfDefs._USE_LOGGING) {
			logEntry(node, C.getConcept(), C.getDep(), reason);
		}
	}

	/** add all elements from NODE label into Todo list */
	private void redoNodeLabel(DlCompletionTree node, final String reason) {
		final CGLabel lab = node.label();
		//CGLabel__const_iterator p;
		List<ConceptWDep> l = lab.get_sc();
		for (int i = 0; i < l.size(); i++) {
			//ConceptWDep p=l.get(i);
			addExistingToDoEntry(node, l.get(i), reason);
		}
		l = lab.get_cc();
		for (int i = 0; i < l.size(); i++) {
			addExistingToDoEntry(node, l.get(i), reason);
		}
	}

	/** make sure that the DAG does not grow larger than that was recorded */
	private void ensureDAGSize() {
		if (dagSize < DLHeap.size()) {
			dagSize = DLHeap.maxSize();
		}
	}

	//--		internal cache support
	/** return cache of given completion tree (implementation) */
	protected final ModelCacheInterface createModelCache(
			final DlCompletionTree p) {
		return new ModelCacheIan(DLHeap, p, encounterNominal, tBox.nC, tBox.nR);
	}

	/** create cache entry for given singleton */
	//	private void registerNominalCache(TIndividual p) {
	//		DLHeap.setCache(p.getpName(), createModelCache(p.getNode()
	//				.resolvePBlocker()));
	//	}
	/** check whether node may be (un)cached; save node if something is changed */
	private ModelCacheState tryCacheNode(DlCompletionTree node) {
		//TODO verify
		ModelCacheState ret = canBeCached(node) ? reportNodeCached(node)
				: ModelCacheState.csFailed;
		// node is cached if RET is csvalid
		boolean val = ret == ModelCacheState.csValid;
		if (node.isCached() != val) {
			TRestorer setCached = node.setCached(val);
			CGraph.saveRareCond(setCached);
		}
		return ret;
	}

	/** @return true iff cache status is invalid */
	static boolean usageByState(ModelCacheState status) {
		return status == ModelCacheState.csInvalid;
	}

	boolean applyExtraRulesIf(TConcept p) {
		if (!p.hasExtraRules()) {
			return false;
		}
		assert p.isPrimitive();
		return applyExtraRules(p);
	}

	//--		internal nominal reasoning interface
	/** check whether reasoning with nominals is performed */
	public boolean hasNominals() {
		return false;// !Nominals.isEmpty();
	}

	/** use classification information for the nominal P */
	//	private void updateClassifiedSingleton(TIndividual p) {
	//		registerNominalCache(p);
	//	}
	/** @return true iff current node is i-blocked (ie, no expansion necessary) */
	private boolean isIBlocked() {
		return curNode.isIBlocked();
	}

	/** @return true iff there is R-neighbour labelled with C */
	private boolean isSomeExists(final TRole R, int C) {
		//TODO verify whether a cache is worth the effort
		if (!used.contains(C)) {
			return false;
		}
		final DlCompletionTree where = curNode.isSomeApplicable(R, C);
		if (where != null && IfDefs._USE_LOGGING) {
			LL.print(Templates.E, R.getName(), where.getId(), C);
		}
		return where != null;
	}

	/**
	 * apply AR.C in and <= nR (if needed) in NODE's label where R is label of
	 * arcSample. Set of applicable concepts is defined by redoForallFlags
	 * value.
	 */
	/** check if branching rule was called for the 1st time */
	private boolean isFirstBranchCall() {
		return bContext == null;
	}

	/** init branching context with given rule type */
	protected void initBC(BranchingContext c) {
		//XXX move to BranchingContext
		// save reasoning context
		c.node = curNode;
		c.concept = new ConceptWDep(curConcept.getConcept(),
				curConcept.getDep());
		c.branchDep = DepSetFactory.create(curConcept.getDep());
	}

	/** create BC for Or rule */
	private void createBCOr() {
		bContext = Stack.pushOr();
		if (LeveLogger.isAbsorptionActive()) {
			LeveLogger.LL_ABSORPTION.println("s.push(" + bContext + ")");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintStream p = new PrintStream(out);
			new Exception().printStackTrace(p);
			p.flush();
			LeveLogger.LL_ABSORPTION.print(out.toString());
		}
	}

	/** create BC for NN-rule */
	private void createBCNN() {
		bContext = Stack.pushNN();
	}

	/** create BC for LE-rule */
	private void createBCLE() {
		bContext = Stack.pushLE();
	}

	/** create BC for Choose-rule */
	private void createBCCh() {
		bContext = Stack.pushCh();
	}

	/** check whether a node represents a functional one */
	private static boolean isFunctionalVertex(final DLVertex v) {
		return v.Type() == DagTag.dtLE && v.getNumberLE() == 1
				&& v.getC() == Helper.bpTOP;
	}

	/**
	 * check if ATLEAST and ATMOST entries are in clash. Both vertex MUST have
	 * dtLE type.
	 */
	private boolean checkNRclash(final DLVertex atleast, final DLVertex atmost) { // >= n R.C clash with <= m S.D iff...
		return (atmost.getC() == Helper.bpTOP || atleast.getC() == atmost
				.getC()) && // either D is TOP or C == D...
				atleast.getNumberGE() > atmost.getNumberLE() && // and n is greater than m...
				atleast.getRole().lesserequal(atmost.getRole()); // and R [= S
	}

	/** quick check whether CURNODE has a clash with a given ATMOST restriction */
	private boolean isQuickClashLE(final DLVertex atmost) {
		List<ConceptWDep> list = curNode.beginl_cc();
		for (int i = 0; i < list.size(); i++) {
			ConceptWDep q = list.get(i);
			if (q.getConcept() < 0 // need at-least restriction
					&& isNRClash(DLHeap.get(q.getConcept()), atmost, q)) {
				return true;
			}
		}
		return false;
	}

	/** quick check whether CURNODE has a clash with a given ATLEAST restriction */
	private boolean isQuickClashGE(final DLVertex atleast) {
		List<ConceptWDep> list = curNode.beginl_cc();
		for (int i = 0; i < list.size(); i++) {
			ConceptWDep q = list.get(i);
			if (q.getConcept() > 0 // need at-most restriction
					&& isNRClash(atleast, DLHeap.get(q.getConcept()), q)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * aux method that fills the dep-set for either C or ~C found in the label;
	 * 
	 * @param d
	 *            depset to be changed if a clash is found
	 * @return whether C was found
	 */
	private boolean findChooseRuleConcept(final CWDArray label, int C, DepSet d) {
		if (C == Helper.bpTOP) {
			return true;
		}
		if (findConceptClash(label, C, null)) {
			if (d != null) {
				d.add(getClashSet());
			}
			return true;
		} else if (findConceptClash(label, -C, null)) {
			if (d != null) {
				d.add(getClashSet());
			}
			return false;
		} else {
			throw new UnreachableSituationException();
		}
		//return false;
	}

	/** check whether clash occures EDGE to TO labelled with S disjoint with R */
	private boolean checkDisjointRoleClash(final DlCompletionTreeArc edge,
			DlCompletionTree to, final TRole R, final DepSet dep) { // clash found
		if (edge.getArcEnd().equals(to) && edge.getRole().isDisjoint(R)) {
			setClashSet(dep);
			updateClashSet(edge.getDep());
			return true;
		}
		return false;
	}

	// support for FORALL expansion
	/** Perform expansion of (\neg \ER.Self).DEP to an EDGE */
	private boolean checkIrreflexivity(final DlCompletionTreeArc edge,
			final TRole R, final DepSet dep) {
		// only loops counts here...
		if (!edge.getArcEnd().equals(edge.getReverse().getArcEnd())) {
			return false;
		}
		// which are labelled either with R or with R-
		if (!edge.isNeighbour(R) && !edge.isNeighbour(R.inverse())) {
			return false;
		}
		// set up clash
		setClashSet(dep);
		updateClashSet(edge.getDep());
		return true;
	}

	/**
	 * @return utClash iff given data node contains inconsistent data
	 *         constraints
	 */
	private boolean checkDataClash(final DlCompletionTree node) {
		if (hasDataClash(node)) {
			setClashSet(DTReasoner.getClashSet());
			return true;
		} else {
			return false;
		}
	}

	/** log the result of processing ACTION with entry (N,C{DEP})/REASON */
	private void logNCEntry(final DlCompletionTree n, int bp, DepSet dep,
			final String action, final String reason) {
		LL.print(Templates.SPACE, action);
		LL.print("(");
		n.logNode();
		LL.print(String.format(",%s%s)", bp, dep));
		if (reason != null) {
			LL.print(reason);
		}
	}

	/** log addition of the entry to ToDo list */
	private void logEntry(final DlCompletionTree n, int bp, DepSet dep,
			final String reason) {
		logNCEntry(n, bp, dep, "+", reason);
	}

	/** log clash happened during node processing */
	private void logClash(final DlCompletionTree n, int bp, DepSet dep) {
		logNCEntry(n, bp, dep, "x", DLHeap.get(bp).getTagName());
	}

	/** use this method in ALL dependency stuff (never use tryLevel directly) */
	private int getCurLevel() {
		return tryLevel;
	}

	/** set new branching level (never use tryLevel directly) */
	private void setCurLevel(int level) {
		tryLevel = level;
	}

	/**
	 * @return true if no branching ops were applied during reasoners; FIXME!!
	 *         doesn't work properly with a nominal cloud
	 */
	protected boolean noBranchingOps() {
		return tryLevel == Helper.InitBranchingLevelValue + nonDetShift;
	}

	/** Get save/restore level based on either current- or DS level */
	private int getSaveRestoreLevel(final DepSet ds) {
		// FIXME!!! see more precise it later
		if (IfDefs.RKG_IMPROVE_SAVE_RESTORE_DEPSET) {
			return ds.level() + 1;
		} else {
			return getCurLevel();
		}
	}

	/** restore reasoning state to the latest saved position */
	private void restore() {
		restore(getCurLevel() - 1);
	}

	/** update level in N node and save it's state (if necessary) */
	private void updateLevel(DlCompletionTree n, final DepSet ds) {
		CGraph.saveNode(n, getSaveRestoreLevel(ds));
	}

	/** finalize branching OP processing making deterministic op */
	private void determiniseBranchingOp() {
		bContext = null; // clear context for the next branching op
		Stack.pop(); // remove unnecessary context from the stack
	}

	// access to global clashset, which contains result of clash during label addition
	/** get value of global dep-set */
	private final DepSet getClashSet() {
		return clashSet;
	}

	/** set value of global dep-set to D */
	private void setClashSet(final DepSet d) {
		clashSet = d;
	}

	/** add D to global dep-set */
	private void updateClashSet(final DepSet d) {
		clashSet.add(d);
	}

	/** get dep-set wrt current level */
	private DepSet getCurDepSet() {
		return DepSetFactory.create(getCurLevel() - 1);
		//		DepSet d = DepSetFactory.create();
		//		d.add(getCurLevel() - 1);
		//		return d;// new DepSetImpl(Manager.get(getCurLevel() - 1, null));
	}

	/** get RW access to current branching dep-set */
	private DepSet getBranchDep() {
		return bContext.branchDep;
	}

	/** update cumulative branch-dep with current clash-set */
	private void updateBranchDep() {
		getBranchDep().add(getClashSet());
	}

	/** prepare cumulative dep-set to usage */
	private void prepareBranchDep() {
		getBranchDep().restrict(getCurLevel());
	}

	/** prepare cumulative dep-set and copy itto general clash-set */
	private void useBranchDep() {
		prepareBranchDep();
		setClashSet(getBranchDep());
	}

	/** re-apply all the relevant expantion rules to a given unblocked NODE */
	public void repeatUnblockedNode(DlCompletionTree node, boolean direct) {
		if (direct) {
			applyAllGeneratingRules(node); // re-apply all the generating rules
		} else {
			redoNodeLabel(node, "ubi");
		}
	}

	/**
	 * get access to the DAG associated with it (necessary for the blocking
	 * support)
	 */
	public final DLDag getDAG() {
		return tBox.getDLHeap();
	}

	/** get the ROOT node of the completion graph */
	private final DlCompletionTree getRootNode() {
		return CGraph.getRoot();
	}

	/** init Todo list priority for classification */
	public void initToDoPriorities(final IFOptionSet OptionSet) {
		assert OptionSet != null;
		TODO.initPriorities(OptionSet, "IAOEFLG");
	}

	/** set blocking method for a session */
	public void setBlockingMethod(boolean hasInverse, boolean hasQCR) {
		CGraph.setBlockingMethod(hasInverse, hasQCR);
	}

	/** set SAT test timeout in milliseconds */
	public void setTestTimeout(long ms) {
		testTimeout = ms;
	}

	/** return [singleton] cache for given concept implementation */
	//	final modelCacheInterface createModelCache(int p) {
	//		if (p == bpTOP || p == bpBOTTOM) {
	//			return new modelCacheConst(p == bpTOP);
	//		} else {
	//			return new modelCacheSingleton(p);
	//		}
	//	}
	/** create model cache for the just-classified entry */
	final ModelCacheInterface buildCacheByCGraph(boolean sat) {
		if (sat) {
			return createModelCache(getRootNode());
		} else {
			// unsat => cache is just bottom
			return ModelCacheConst.createConstCache(Helper.bpBOTTOM);
		}
	}

	void writeTotalStatistic(LogAdapter o) {
		if (IfDefs.USE_REASONING_STATISTICS) {
			stats.accumulate(); // ensure that the last reasoning results are in
			stats.logStatisticData(o, false, CGraph);
		}
		if (IfDefs.USE_BLOCKING_STATISTICS) {
			DlCompletionTree.printBlockingStat1(o);
			DlCompletionTree.clearBlockingStat1();
		}
		o.print("\n");
	}

	final ModelCacheInterface createCache(int p, FastSet f) {
		assert Helper.isValid(p);
		ModelCacheInterface cache;
		if ((cache = DLHeap.getCache(p)) != null) {
			return cache;
		}
		prepareCascadedCache(p, f);
		if ((cache = DLHeap.getCache(p)) != null) {
			return cache;
		}
		cache = buildCache(p);
		DLHeap.setCache(p, cache);
		return cache;
	}

	private final static EnumSet<DagTag> handlecollection = EnumSet.of(dtAnd,
			dtCollection);
	private final static EnumSet<DagTag> handleforallle = EnumSet.of(dtForall,
			dtLE);
	private final static EnumSet<DagTag> handlesingleton = EnumSet.of(
			dtPSingleton, dtNSingleton, dtNConcept, dtPConcept);

	private void prepareCascadedCache(int p, FastSet f) {
		//System.out.println("DlSatTester.prepareCascadedCache() "+p);
		if (inProcess.contains(p)) {
			return;
		}
		if (f.contains(p)) {
			return;
		}
		final DLVertex v = DLHeap.get(p);
		boolean pos = p > 0;
		if (v.getCache(pos) != null) {
			return;
		}
		DagTag type = v.Type();
		if (handlecollection.contains(type)) {
			for (int q : v.begin()) {
				prepareCascadedCache(pos ? q : -q, f);
			}
		} else if (handlesingleton.contains(type)) {
			if (!pos && type.isPNameTag()) {
				return;
			}
			inProcess.add(p);
			prepareCascadedCache(pos ? v.getC() : -v.getC(), f);
			inProcess.remove(p);
		} else if (handleforallle.contains(type)) {
			final TRole R = v.getRole();
			if (!R.isDataRole()) {
				int x = pos ? v.getC() : -v.getC();
				if (x != Helper.bpTOP) {
					inProcess.add(x);
					createCache(x, f);
					inProcess.remove(x);
				}
				x = R.getBPRange();
				if (x != Helper.bpTOP) {
					inProcess.add(x);
					createCache(x, f);
					inProcess.remove(x);
				}
			}
		}
		f.add(p);
	}

	private final ModelCacheInterface buildCache(int p) {
		LL.print("\nChecking satisfiability of DAG entry ");
		LL.print(p);
		tBox.PrintDagEntry(LL, p);
		LL.print(":\n");
		boolean sat = runSat(p, Helper.bpTOP);
		if (!sat) {
			LL.print(Templates.BUILD_CACHE_UNSAT, p);
		}
		return buildCacheByCGraph(sat);
	}

	protected void resetSessionFlags() {
		// reflect possible change of DAG size
		ensureDAGSize();
		used.add(Helper.bpTOP);
		used.add(Helper.bpBOTTOM);
		encounterNominal = false;
		checkDataNode = true;
		//dBlocked = null;
	}

	protected boolean initNewNode(DlCompletionTree node, final DepSet dep, int C) {
		if (node.isDataNode()) {
			checkDataNode = false;
		}
		node.setInit(C);
		if (addToDoEntry(node, C, dep, null)) {
			return true;
		}
		if (node.isDataNode()) {
			return false;
		}
		if (addToDoEntry(node, tBox.getTG(), dep, null)) {
			return true;
		}
		if (GCIs.isReflexive() && applyReflexiveRoles(node, dep)) {
			return true;
		}
		return false;
	}

	//	private boolean initNominalNode(final TIndividual nom) {
	//		DlCompletionTree node = CGraph.getNewNode();
	//		node.setNominalLevel();
	//		nom.setNode(node); // init nominal with associated node
	//		return initNewNode(node, new DepSet(), nom.getpName()) == tacticUsage.utClash; // ABox is inconsistent
	//	}
	boolean runSat(int p, int q) {
		prepareReasoner();
		// use general method to init node with P and add Q then
		if (initNewNode(CGraph.getRoot(), DepSetFactory.create(), p)
				|| addToDoEntry(CGraph.getRoot(), q, DepSetFactory.create(),
						null)) {
			return false; // concept[s] unsatisfiable
		}
		// check satisfiability explicitly
		TsProcTimer timer = q == Helper.bpTOP ? satTimer : subTimer;
		timer.Start();
		boolean result = runSat();
		timer.Stop();
		return result;
	}

	boolean checkDisjointRoles(final TRole R, final TRole S) {
		prepareReasoner();
		// use general method to init node...
		DepSet dummy = DepSetFactory.create();
		if (initNewNode(CGraph.getRoot(), dummy, Helper.bpTOP)) {
			return true;
		}
		// ... add edges with R and S...
		curNode = CGraph.getRoot();
		DlCompletionTreeArc edgeR = createOneNeighbour(R, dummy);
		DlCompletionTreeArc edgeS = createOneNeighbour(S, dummy);
		// init new nodes/edges. No need to apply restrictions, as no reasoning have been done yet.
		if (initNewNode(edgeR.getArcEnd(), dummy, Helper.bpTOP)
				|| initNewNode(edgeS.getArcEnd(), dummy, Helper.bpTOP)
				|| setupEdge(edgeR, dummy, /*flags=*/0)
				|| setupEdge(edgeS, dummy, /*flags=*/0)
				|| Merge(edgeS.getArcEnd(), edgeR.getArcEnd(), dummy)) {
			return true;
		}
		// 2 roles are disjoint if current setting is unsatisfiable
		curNode = null;
		return !runSat();
	}

	boolean checkIrreflexivity(TRole R) {
		prepareReasoner();
		// use general method to init node...
		DepSet dummy = DepSetFactory.create();
		if (initNewNode(CGraph.getRoot(), dummy, Helper.bpTOP)) {
			return true;
		}
		// ... add an R-loop
		curNode = CGraph.getRoot();
		DlCompletionTreeArc edgeR = createOneNeighbour(R, dummy);
		// init new nodes/edges. No need to apply restrictions, as no reasoning have been done yet.
		if (initNewNode(edgeR.getArcEnd(), dummy, Helper.bpTOP)
				|| setupEdge(edgeR, dummy, /*flags=*/0)
				|| Merge(edgeR.getArcEnd(), CGraph.getRoot(), dummy)) {
			return true;
		}
		// R is irreflexive if current setting is unsatisfiable
		curNode = null;
		return !runSat();
	}

	// restore implementation
	private boolean backJumpedRestore() {
		// if empty clash dep-set -- concept is unsatisfiable
		if (getClashSet().isEmpty()) {
			return true;
		}
		// some non-deterministic choices were done
		restore(getClashSet().level());
		return false;
	}

	private boolean straightforwardRestore() {
		if (noBranchingOps()) {
			return true; // ... the concept is unsatisfiable
		} else { // restoring the state
			restore();
			return false;
		}
	}

	private boolean tunedRestore() {
		if (useBackjumping) {
			return backJumpedRestore();
		} else {
			return straightforwardRestore();
		}
	}

	private boolean commonTacticBodyAll(final DLVertex cur) {
		assert curConcept.getConcept() > 0 && cur.Type() == dtForall;
		// can't skip singleton models for complex roles due to empty transitions
		if (cur.getRole().isSimple()) {
			return commonTacticBodyAllSimple(cur);
		} else {
			return commonTacticBodyAllComplex(cur);
		}
	}

	protected DlSatTester(TBox tbox, final IFOptionSet Options) {
		tBox = tbox;
		DLHeap = tbox.getDLHeap();
		//DepSetFactory.Manager = new TDepSetManager();
		CGraph = new DlCompletionGraph(1, this);
		DTReasoner = new DataTypeReasoner(tbox.getDLHeap());
		newNodeCache = new ModelCacheIan(true, tbox.nC, tbox.nR);
		newNodeEdges = new ModelCacheIan(false, tbox.nC, tbox.nR);
		GCIs = tbox.getGCIs();
		testTimeout = 0;
		bContext = null;
		tryLevel = Helper.InitBranchingLevelValue;
		nonDetShift = 0;
		curNode = null;
		dagSize = 0;
		readConfig(Options);
		if (tBox.hasFC() && useAnywhereBlocking) {
			useAnywhereBlocking = false;
			LL.print("Fairness constraints: set useAnywhereBlocking = false\n");
		}
		CGraph.initContext(useLazyBlocking, useAnywhereBlocking);
		tBox.getDataTypeCenter().initDataTypeReasoner(DTReasoner);
		tbox.getORM().fillReflexiveRoles(ReflexiveRoles);
		if (IfDefs.USE_BLOCKING_STATISTICS) {
			DlCompletionTree.clearBlockingStat1();
		}
		resetSessionFlags();
	}

	private void readConfig(final IFOptionSet Options) {
		assert Options != null;
		useSemanticBranching = Options.getBool("useSemanticBranching");
		useBackjumping = Options.getBool("useBackjumping");
		useLazyBlocking = Options.getBool("useLazyBlocking");
		useAnywhereBlocking = Options.getBool("useAnywhereBlocking");
		LL.print(Templates.READCONFIG, useSemanticBranching, useBackjumping,
				useLazyBlocking, useAnywhereBlocking);
	}

	protected void prepareReasoner() {
		//System.out.println("DlSatTester.prepareReasoner()");
		CGraph.clear();
		Stack.clear();
		TODO.clear();
		used.clear();
		curNode = null;
		bContext = null;
		tryLevel = Helper.InitBranchingLevelValue;
		// clear last session information
		resetSessionFlags();
	}

	private AddConceptResult checkAddedConcept(final CWDArray lab, int p,
			DepSet dep) {
		assert Helper.isCorrect(p); // sanity checking
		// constants are not allowed here
		assert p != Helper.bpTOP;
		assert p != Helper.bpBOTTOM;
		stats.nLookups.inc();
		stats.nLookups.inc();
		//TODO check this new version
		if (lab.contains(p)) {
			return AddConceptResult.acrExist;
		}
		int inv_p = -p;
		DepSet depset= lab.get(inv_p);
		if (depset!=null) {
			clashSet = DepSetFactory.plus(depset, dep);
			return AddConceptResult.acrClash;
		}
		return AddConceptResult.acrDone;
	}

	private boolean findConceptClash(final CWDArray lab, int bp, DepSet dep) {
		stats.nLookups.inc();
		DepSet depset= lab.get(bp);
		if (depset!=null) {
			clashSet = DepSetFactory.plus(depset, dep);
			return true;
		}
		return false;
	}

	private AddConceptResult tryAddConcept(final CWDArray lab, int bp,
			DepSet dep) {
		boolean canC = used.contains(bp);
		boolean canNegC = used.contains(-bp);
		if (canC) {
			if (canNegC) {
				return checkAddedConcept(lab, bp, dep);
			} else {
				stats.nLookups.inc();
				return lab.contains(bp) ? AddConceptResult.acrExist
						: AddConceptResult.acrDone;
			}
		} else {
			if (canNegC) {
				return findConceptClash(lab, -bp, dep) ? AddConceptResult.acrClash
						: AddConceptResult.acrDone;
			} else {
				return AddConceptResult.acrDone;
			}
		}
	}

	//	private boolean addToDoEntry(DlCompletionTree n, int bp, DepSet dep) {
	//		return addToDoEntry(n, bp, dep, null);
	//	}
	private boolean addToDoEntry(DlCompletionTree n, int bp, DepSet dep,
			final String reason) {
		if (bp == Helper.bpTOP) {
			return false;
		}
		if (bp == Helper.bpBOTTOM) {
			setClashSet(dep);
			if (IfDefs._USE_LOGGING) {
				logClash(n, bp, dep);
			}
			return true;
		}
		final DLVertex v = DLHeap.get(bp);
		DagTag tag = v.Type();
		if (tag == DagTag.dtCollection) {
			if (bp < 0) {
				return false;
			}
			stats.nTacticCalls.inc();
			DlCompletionTree oldNode = curNode;
			ConceptWDep oldConcept = curConcept;
			curNode = n;
			curConcept = new ConceptWDep(bp, dep);
			boolean ret = commonTacticBodyAnd(v);
			curNode = oldNode;
			curConcept = oldConcept;
			return ret;
		}
		switch (tryAddConcept(n.label().getLabel(tag), bp, dep)) {
			case acrClash:
				if (IfDefs._USE_LOGGING) {
					logClash(n, bp, dep);
				}
				return true;
			case acrExist:
				return false;
			case acrDone:
				return insertToDoEntry(n, bp, dep, tag, reason);
			default:
				throw new UnreachableSituationException();
		}
	}

	private boolean insertToDoEntry(DlCompletionTree n, int bp, DepSet dep,
			DagTag tag, String reason) {
		ConceptWDep p = new ConceptWDep(bp, dep);
		updateLevel(n, dep);
		CGraph.addConceptToNode(n, p, tag);
		used.add(bp);
		if (n.isCached()) {
			return correctCachedEntry(n);
		}
		TODO.addEntry(n, tag, p);
		if (n.isDataNode()) {
			return checkDataNode ? checkDataClash(n) : false;
		}
		if (IfDefs._USE_LOGGING) {
			logEntry(n, bp, dep, reason);
		}
		return false;
	}

	private boolean canBeCached(DlCompletionTree node) {
		boolean shallow = true;
		int size = 0;
		if (node.isNominalNode()) {
			return false;
		}
		stats.nCacheTry.inc();
		List<ConceptWDep> list = node.beginl_sc();
		for (int i = 0; i < list.size(); i++) {
			ConceptWDep p = list.get(i);
			if (DLHeap.getCache(p.getConcept()) == null) {
				stats.nCacheFailedNoCache.inc();
				LL.print(Templates.CAN_BE_CACHED, p.getConcept());
				return false;
			}
			shallow &= DLHeap.getCache(p.getConcept()).shallowCache();
			++size;
		}
		list = node.beginl_cc();
		for (int i = 0; i < list.size(); i++) {
			ConceptWDep p = list.get(i);
			if (DLHeap.getCache(p.getConcept()) == null) {
				stats.nCacheFailedNoCache.inc();
				LL.print(Templates.CAN_BE_CACHED, p.getConcept());
				return false;
			}
			shallow &= DLHeap.getCache(p.getConcept()).shallowCache();
			++size;
		}
		if (shallow && size != 0) {
			stats.nCacheFailedShallow.inc();
			LL.print(" cf(s)");
			return false;
		}
		return true;
	}

	/**
	 * build cache of the node (it is known that caching is possible) in
	 * newNodeCache
	 */
	private void doCacheNode(DlCompletionTree node) {
		//modelCacheIan cache = new modelCacheIan(true);
		DepSet dep = DepSetFactory.create();
		newNodeCache.clear();
		List<ConceptWDep> beginl_sc = node.beginl_sc();
		// TODO look at the switches
		for (int i = 0; i < beginl_sc.size(); i++) {
			ConceptWDep p = beginl_sc.get(i);
			dep.add(p.getDep());
			switch (newNodeCache.merge(DLHeap.getCache(p.getConcept()))) {
				case csValid:
					break;
				case csInvalid:
					setClashSet(dep);
					return;
				default:
					return;
			}
		}
		List<ConceptWDep> list = node.beginl_cc();
		for (int i = 0; i < list.size(); i++) {
			ConceptWDep p = list.get(i);
			dep.add(p.getDep());
			switch (newNodeCache.merge(DLHeap.getCache(p.getConcept()))) {
				case csValid:
					break;
				case csInvalid:
					setClashSet(dep);
					return;
				default:
					return;
			}
		}
		// all concepts in label are mergable; now try to add input arc
		newNodeEdges.clear();
		newNodeEdges.initRolesFromArcs(node);
		newNodeCache.merge(newNodeEdges);
	}

	private ModelCacheState reportNodeCached(DlCompletionTree node) {
		doCacheNode(node);
		ModelCacheState status = newNodeCache.getState();
		switch (status) {
			case csValid:
				stats.nCachedSat.inc();
				if (IfDefs._USE_LOGGING) {
					LL.print(Templates.REPORT1, node.getId());
				}
				break;
			case csInvalid:
				stats.nCachedUnsat.inc();
				break;
			case csFailed:
			case csUnknown:
				stats.nCacheFailed.inc();
				LL.print(" cf(c)");
				status = ModelCacheState.csFailed;
				break;
			default:
				throw new UnreachableSituationException();
		}
		return status;
	}

	private boolean correctCachedEntry(DlCompletionTree n) {
		assert n.isCached();
		ModelCacheState status = tryCacheNode(n);
		if (status == ModelCacheState.csFailed) {
			redoNodeLabel(n, "uc");
		}
		return usageByState(status);
	}

	private boolean hasDataClash(final DlCompletionTree Node) {
		assert Node != null && Node.isDataNode();
		DTReasoner.clear();
		for (ConceptWDep r : Node.beginl_sc()) {
			if (DTReasoner.addDataEntry(r.getConcept(), r.getDep())) {
				return true;
			}
		}
		return DTReasoner.checkClash();
	}

	protected boolean runSat() {
		testTimer.Start();
		boolean result = checkSatisfiability();
		testTimer.Stop();
		if (IfDefs._USE_LOGGING) {
			LL.print("\nChecking time in seconds:");
			LL.print(testTimer.toString());
		}
		testTimer.Reset();
		finaliseStatistic();
		if (result && IfDefs._USE_LOGGING) {
			CGraph.Print(LL);
		}
		return result;
	}

	private void finaliseStatistic() {
		CGraph.clearStatistics();
	}

	private boolean applyReflexiveRoles(DlCompletionTree node, final DepSet dep) {
		for (TRole p : ReflexiveRoles) {
			DlCompletionTreeArc pA = CGraph.addRoleLabel(node, node, false, p,
					dep);
			if (setupEdge(pA, dep, 0)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkSatisfiability() {
		int loop = 0;
		//		CGraph.prepareForContains();
		//		try {
		for (;;) {
			if (curNode == null) {
				if (TODO.isEmpty()) {
					if (IfDefs._USE_LOGGING) {
						logIndentation();
						//CGraph.Print(LL);
						LL.print("[*ub:");
					}
					CGraph.retestCGBlockedStatus();
					if (IfDefs._USE_LOGGING) {
						LL.print("]");
					}
					//TODO just added from FaCT
					//dBlocked = null;
					if (TODO.isEmpty()) {
						return true;
					}
				}
				final ToDoEntry curTDE = TODO.getNextEntry();
				assert curTDE != null;
				curNode = curTDE.getNode();
				curConcept = curTDE.getOffset();
			}
			if (++loop == 5000) {
				loop = 0;
				if (tBox.isCancelled().get()) {
					return false;
				}
				if (testTimeout > 0 && testTimer.calcDelta() >= testTimeout) {
					throw new TimeOutException();
				}
			}
			if (commonTactic()) {
				//System.out.print("DlSatTester.checkSatisfiability()");
				if (tunedRestore()) {
					//System.out.println(" exit");
					return false;
				}
				//				System.out.println(" another round");
			} else {
				curNode = null;
			}
		}
		//		} finally {
		//			CGraph.clearForContains();
		//		}
	}

	private void restoreBC() {
		curNode = bContext.node;
		curConcept = new ConceptWDep(bContext.concept.getConcept(),
				bContext.concept.getDep());
		updateBranchDep();
		bContext.nextOption();
	}

	protected void save() {
		//System.out.println(this.getClass().getSimpleName()+ "\tsave top this stack: "+Stack.size()+"\tthat stack: "+		CGraph.Stack.size());
		CGraph.save();
		//System.out.println(this.getClass().getSimpleName()+ "\tsave bot this stack: "+Stack.size()+"\tthat stack: "+		CGraph.Stack.size());
		TODO.save();
		++tryLevel;
		//DepSetFactory.Manager.ensureLevel(getCurLevel());
		bContext = null;
		stats.nStateSaves.inc();
		if (IfDefs._USE_LOGGING) {
			LL.print(Templates.SAVE, (getCurLevel() - 1));
		}
		if (IfDefs.DEBUG_SAVE_RESTORE) {
			CGraph.Print(LL);
			TODO.Print(LL);
		}
	}

	protected void restore(int newTryLevel) {
		//System.out.println(this.getClass().getSimpleName()+ "\ttop this stack: "+Stack.size()+"\tthat stack: "+		CGraph.Stack.size());
		assert !Stack.isEmpty();
		assert newTryLevel > 0;
		//		if (Stack.isEmpty()) {
		//			System.out.println("DlSatTester.restore()");
		//		}
		setCurLevel(newTryLevel);
		bContext = Stack.top(getCurLevel());
		restoreBC();
		CGraph.restore(getCurLevel());
		//System.out.println(this.getClass().getSimpleName()+ "\tbot this stack: "+Stack.size()+"\tthat stack: "+		CGraph.Stack.size());
		TODO.restore(getCurLevel());
		stats.nStateRestores.inc();
		if (IfDefs._USE_LOGGING) {
			LL.print(Templates.RESTORE, getCurLevel());
		}
		if (IfDefs.DEBUG_SAVE_RESTORE) {
			CGraph.Print(LL);
			TODO.Print(LL);
		}
	}

	private void logIndentation() {
		char[] bytes = new char[getCurLevel()];
		Arrays.fill(bytes, ' ');
		bytes[0] = '\n';
		LL.print(new String(bytes));
	}

	private void logStartEntry() {
		logIndentation();
		LL.print("[*(");
		curNode.logNode();
		LL.print(",");
		curConcept.print(LL);
		LL.print("){");
		if (curConcept.getConcept() < 0) {
			LL.print("~");
		}
		LL.print(DLHeap.get(curConcept.getConcept()).getTagName());
		LL.print("}:");
	}

	private void logFinishEntry(boolean res) {
		LL.print("]");
		if (res) {
			LL.print(Templates.LOG_FINISH_ENTRY, getClashSet());
		}
	}

	public float printReasoningTime(LogAdapter o) {
		o.print(String.format(
				"\n     SAT takes %s seconds\n     SUB takes %s seconds",
				satTimer, subTimer));
		return satTimer.calcDelta() + subTimer.calcDelta();
	}

	/**
	 * Tactics section;
	 * 
	 * Each Tactic should have a (small) Usability function <name> and a Real
	 * tactic function <name>Body
	 * 
	 * Each tactic returns: - true - if expansion of CUR lead to clash - false -
	 * overwise
	 * 
	 */
	private boolean commonTactic() {
		if (curNode.isCached() || curNode.isPBlocked()) {
			return false;
		}
		if (IfDefs._USE_LOGGING) {
			logStartEntry();
		}
		boolean ret = false;
		if (!isIBlocked()) {
			ret = commonTacticBody(DLHeap.get(curConcept.getConcept()));
		}
		if (IfDefs._USE_LOGGING) {
			logFinishEntry(ret);
		}
		return ret;
	}

	private boolean commonTacticBody(final DLVertex cur) {
		stats.nTacticCalls.inc();
		switch (cur.Type()) {
			case dtTop:
				throw new UnreachableSituationException();
				//				return tacticUsage.utDone;
			case dtDataType:
			case dtDataValue:
				stats.nUseless.inc();
				return false;
			case dtPSingleton:
			case dtNSingleton:
				//dBlocked = null;
				if (curConcept.getConcept() > 0) {
					return commonTacticBodySingleton(cur);
				} else {
					return commonTacticBodyId(cur);
				}
			case dtNConcept:
			case dtPConcept:
				//dBlocked = null;
				return commonTacticBodyId(cur);
			case dtAnd:
				//dBlocked = null;
				if (curConcept.getConcept() > 0) {
					return commonTacticBodyAnd(cur);
				} else {
					return commonTacticBodyOr(cur);
				}
			case dtForall:
				if (curConcept.getConcept() < 0) {
					return commonTacticBodySome(cur);
				}
				return commonTacticBodyAll(cur);
			case dtIrr:
				if (curConcept.getConcept() < 0) {
					return commonTacticBodySomeSelf(cur.getRole());
				} else {
					return commonTacticBodyIrrefl(cur.getRole());
				}
			case dtLE:
				if (curConcept.getConcept() < 0) {
					return commonTacticBodyGE(cur);
				}
				if (isFunctionalVertex(cur)) {
					return commonTacticBodyFunc(cur);
				} else {
					return commonTacticBodyLE(cur);
				}
			case dtProj:
				assert curConcept.getConcept() > 0;
				return commonTacticBodyProj(cur.getRole(), cur.getC(),
						cur.getProjRole());
			default:
				throw new UnreachableSituationException();
		}
	}

	private boolean commonTacticBodyId(final DLVertex cur) {
		assert cur.Type().isCNameTag(); // safety check
		stats.nIdCalls.inc();
		if (IfDefs.RKG_USE_SIMPLE_RULES) {
			// check if we have some simple rules
			if (curConcept.getConcept() > 0
					&& applyExtraRulesIf((TConcept) cur.getConcept())) {
				return true;
			}
		}
		// get either body(p) or inverse(body(p)), depends on sign of current ID
		int C = curConcept.getConcept() > 0 ? cur.getC() : -cur.getC();
		return addToDoEntry(curNode, C, curConcept.getDep(), null);
	}

	boolean applicable(final TSimpleRule rule) {
		int bp = curConcept.getConcept();
		final CWDArray lab = curNode.label().getLabel(DagTag.dtPConcept);
		DepSet loc = DepSetFactory.create(curConcept.getDep());
		for (TConcept p : rule.getBody()) {
			if (p.getpName() != bp) {
				if (findConceptClash(lab, p.getpName(), loc)) {
					loc.add(getClashSet());
				} else {
					return false;
				}
			}
		}
		setClashSet(loc);
		return true;
	}

	boolean applyExtraRules(TConcept C) {
		FastSet er_begin = C.er_begin();
		for (int i = 0; i < er_begin.size(); i++) {
			TSimpleRule rule = tBox.getSimpleRule(er_begin.get(i));
			stats.nSRuleAdd.inc();
			if (rule.applicable(this)) // apply the rule's head
			{
				stats.nSRuleFire.inc();
				if (addToDoEntry(curNode, rule.getBpHead(), getClashSet(), null)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean commonTacticBodySingleton(final DLVertex cur) {
		assert cur.Type() == dtPSingleton || cur.Type() == dtNSingleton; // safety check
		stats.nSingletonCalls.inc();
		assert hasNominals();
		encounterNominal = true;
		final TIndividual C = (TIndividual) cur.getConcept();
		assert C.getNode() != null;
		DepSet dep = DepSetFactory.create(curConcept.getDep());
		DlCompletionTree realNode = C.getNode().resolvePBlocker(dep);
		if (!realNode.equals(curNode)) {
			return Merge(curNode, realNode, dep);
		}
		return commonTacticBodyId(cur);
	}

	private boolean commonTacticBodyAnd(final DLVertex cur) {
		stats.nAndCalls.inc();
		DepSet dep = curConcept.getDep();
		// FIXME!! I don't know why, but performance is usually BETTER if using r-iters.
		// It's their only usage, so after investigation they can be dropped
		int[] begin = cur.begin();
		for (int i = begin.length - 1; i >= 0; i--) {
			int q = begin[i];
			if (addToDoEntry(curNode, q, dep, null)) {
				return true;
			}
		}
		//		for ( DLVertex::const_reverse_iterator q = cur.rbegin(); q != cur.rend(); ++q )
		//			if ( addToDoEntry ( curNode, *q, dep ) )return true;
		return false;
	}

	private boolean commonTacticBodyOr(final DLVertex cur) {
		assert curConcept.getConcept() < 0 && cur.Type() == dtAnd; // safety check
		stats.nOrCalls.inc();
		if (isFirstBranchCall()) {
			Reference<DepSet> dep = new Reference<DepSet>(
					DepSetFactory.create());
			if (planOrProcessing(cur, dep)) {
				LL.print(Templates.COMMON_TACTIC_BODY_OR,
						OrConceptsToTest.get(OrConceptsToTest.size() - 1));
				return false;
			}
			if (OrConceptsToTest.isEmpty()) {
				setClashSet(dep.getReference());
				return true;
			}
			if (OrConceptsToTest.size() == 1) {
				ConceptWDep C = OrConceptsToTest.get(0);
				return insertToDoEntry(curNode, C.getConcept(),
						dep.getReference(), DLHeap.get(C.getConcept()).Type(),
						"bcp");
			}
			createBCOr();
			bContext.branchDep = DepSetFactory.create(dep.getReference());
			List<ConceptWDep> l = ((BCOr) bContext).getApplicableOrEntries();
			((BCOr) bContext).setApplicableOrEntries(OrConceptsToTest);
			OrConceptsToTest = l;
		}
		return processOrEntry();
	}

	private boolean planOrProcessing(final DLVertex cur, Reference<DepSet> dep) {
		OrConceptsToTest.clear();
		dep.setReference(DepSetFactory.create(curConcept.getDep()));
		// check all OR components for the clash
		CGLabel lab = curNode.label();
		for (int q : cur.begin()) {
			int inverse = -q;
			//ConceptWDep C = new ConceptWDep(Helper.inverse(q));
			switch (tryAddConcept(lab.getLabel(DLHeap.get(inverse).Type()),
					inverse, null)) {
				case acrClash: // clash found -- OK
					dep.getReference().add(getClashSet());
					continue;
				case acrExist: // already have such concept -- save it to the 1st position
					OrConceptsToTest.clear();
					OrConceptsToTest.add(new ConceptWDep(-q));
					return true;
				case acrDone:
					OrConceptsToTest.add(new ConceptWDep(-q));
					continue;
				default: // safety check
					throw new UnreachableSituationException();
			}
		}
		return false;
	}

	private boolean processOrEntry() {
		// save the context here as after save() it would be lost
		BCOr bcOr = (BCOr) bContext;
		String reason = null;
		DepSet dep;
		if (bcOr.isLastOrEntry()) {
			// cumulative dep-set will be used
			prepareBranchDep();
			dep = getBranchDep();
			// no more branching decisions
			determiniseBranchingOp();
			reason = "bcp";
		} else {
			// save current state
			save();
			// new (just branched) dep-set
			dep = getCurDepSet();
			stats.nOrBrCalls.inc();
		}
		// if semantic branching is in use -- add previous entries to the label
		if (useSemanticBranching) {
			for (int i = 0; i < bcOr.getBranchIndex(); i++) {
				int concept = -bcOr.orBeg().get(i).getConcept();
				if (addToDoEntry(curNode, concept, dep, "sb")) {
					throw new UnreachableSituationException();
					// Both Exists and Clash are errors
				}
			}
		}
		// add new entry to current node; we know the result would be DONE
		if (IfDefs.RKG_USE_DYNAMIC_BACKJUMPING) {
			return addToDoEntry(curNode, bcOr.orCur().getConcept(), dep, reason);
		} else {
			return insertToDoEntry(curNode, bcOr.orCur().getConcept(), dep,
					DLHeap.get(bcOr.orCur().getConcept()).Type(), reason);
		}
	}

	private boolean commonTacticBodyAllComplex(DLVertex cur) {
		int state = cur.getState();
		int C = curConcept.getConcept() - state; // corresponds to AR{0}.X
		RAStateTransitions RST = cur.getRole().getAutomaton().getBase()
				.get(state);
		// apply all empty transitions
		if (RST.hasEmptyTransition()) {
			List<RATransition> list = RST.begin();
			for (int i = 0; i < list.size(); i++) {
				RATransition q = list.get(i);
				stats.nAutoEmptyLookups.inc();
				if (q.isEmpty()) {
					if (addToDoEntry(curNode, C + q.final_state(),
							curConcept.getDep(), "e")) {
						return true;
					}
				}
			}
		}
		// apply final-state rule
		if (state == 1) {
			if (addToDoEntry(curNode, cur.getC(), curConcept.getDep(), null)) {
				return true;
			}
		}
		// check whether automaton applicable to any edges
		stats.nAllCalls.inc();
		// check all neighbours
		List<DlCompletionTreeArc> list = curNode.getNeighbour();
		for (int i = 0; i < list.size(); i++) {
			DlCompletionTreeArc p = list.get(i);
			if (RST.recognise(p.getRole())) {
				if (applyTransitions(p, RST, C,
						DepSetFactory.plus(curConcept.getDep(), p.getDep()),
						null)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean commonTacticBodyAllSimple(DLVertex cur) {
		//tacticUsage ret = tacticUsage.utUnusable;
		RAStateTransitions RST = cur.getRole().getAutomaton().getBase().get(0);
		DepSet dep = curConcept.getDep();
		int C = cur.getC();
		// check whether automaton applicable to any edges
		stats.nAllCalls.inc();
		// check all neighbours; as the role is simple then recognise() == applicable()
		for (DlCompletionTreeArc p : curNode.getNeighbour()) {
			if (RST.recognise(p.getRole())) {
				if (addToDoEntry(p.getArcEnd(), C,
						DepSetFactory.plus(dep, p.getDep()), null)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean applyTransitions(DlCompletionTreeArc edge,
			RAStateTransitions RST, int C, DepSet dep, String reason) {
		TRole R = edge.getRole();
		DlCompletionTree node = edge.getArcEnd();
		// fast lane: the single transition which is applicable
		if (RST.isSingleton()) {
			return addToDoEntry(node, C + RST.getTransitionEnd(), dep, reason);
		}
		//tacticUsage ret = tacticUsage.utUnusable;
		// try to apply all transitions to edge
		List<RATransition> begin = RST.begin();
		for (int i = 0; i < begin.size(); i++) {
			RATransition q = begin.get(i);
			stats.nAutoTransLookups.inc();
			if (q.applicable(R)) {
				if (addToDoEntry(node, C + q.final_state(), dep, reason)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Perform expansion of (\AR.C).DEP to an EDGE for simple R with a given
	 * reason
	 */
	private boolean applyUniversalNR(DlCompletionTree Node,
			DlCompletionTreeArc arcSample, DepSet dep_, int flags) {
		// check whether a flag is set
		if (flags == 0) {
			return false;
		}
		//tacticUsage ret = tacticUsage.utUnusable;
		TRole R = arcSample.getRole();
		DepSet dep = DepSetFactory.plus(dep_, arcSample.getDep());
		List<ConceptWDep> base = Node.beginl_cc();
		for (int i = 0; i < base.size(); i++) {
			ConceptWDep p = base.get(i);
			// need only AR.C concepts where ARC is labelled with R
			if (p.getConcept() < 0) {
				continue;
			}
			DLVertex v = DLHeap.get(p.getConcept());
			TRole vR = v.getRole();
			switch (v.Type()) {
				case dtIrr:
					if ((flags & redoIrr.getValue()) > 0) {
						if (checkIrreflexivity(arcSample, vR, dep)) {
							return true;
						}
					}
					break;
				case dtForall: {
					if ((flags & redoForall.getValue()) == 0) {
						break;
					}
					/** check whether transition is possible */
					RAStateTransitions RST = vR.getAutomaton().getBase()
							.get(v.getState());
					if (!RST.recognise(R)) {
						break;
					}
					if (vR.isSimple()) {
						// R is recognised so just add the final state!
						if (addToDoEntry(arcSample.getArcEnd(), v.getC(),
								DepSetFactory.plus(dep, p.getDep()), "ae")) {
							return true;
						}
					} else {
						if (applyTransitions(arcSample, RST,
								p.getConcept() - v.getState(),
								DepSetFactory.plus(dep, p.getDep()), "ae")) {
							return true;
						}
					}
					break;
				}
				case dtLE:
					if (isFunctionalVertex(v)) {
						if ((flags & redoFunc.getValue()) > 0
								&& R.lesserequal(vR)) {
							addExistingToDoEntry(Node, p, "f");
						}
					} else if ((flags & redoAtMost.getValue()) > 0
							&& R.lesserequal(vR)) {
						addExistingToDoEntry(Node, p, "le");
					}
					break;
				default:
					break;
			}
		}
		return false;
	}

	private boolean commonTacticBodySome(final DLVertex cur) {
		final DepSet dep = curConcept.getDep();
		final TRole R = cur.getRole();
		int C = -cur.getC();
		//	tacticUsage ret = tacticUsage.utUnusable;
		if (isSomeExists(R, C)) {
			return false;
		}
		if (C < 0 && DLHeap.get(C).Type() == dtAnd) {
			for (int q : DLHeap.get(C).begin()) {
				if (isSomeExists(R, -q)) {
					return false;
				}
			}
		}
		if (C > 0 && tBox.testHasNominals()) {
			final DLVertex nom = DLHeap.get(C);
			if (nom.Type() == dtPSingleton || nom.Type() == dtNSingleton) {
				return commonTacticBodyValue(R, (TIndividual) nom.getConcept());
			}
		}
		stats.nSomeCalls.inc();
		if (R.isFunctional()) {
			List<TRole> list = R.begin_topfunc();
			for (int i = 0; i < list.size(); i++) {
				int functional = list.get(i).getFunctional();
				switch (tryAddConcept(curNode.label().getLabel(DagTag.dtLE),
						functional, dep)) {
					case acrClash:
						return true;
					case acrDone: {
						updateLevel(curNode, dep);
						ConceptWDep rFuncRestriction1 = new ConceptWDep(
								functional, dep);
						CGraph.addConceptToNode(curNode, rFuncRestriction1,
								DagTag.dtLE);
						used.add(rFuncRestriction1.getConcept());
						LL.print(Templates.COMMON_TACTIC_BODY_SOME,
								rFuncRestriction1);
					}
						break;
					case acrExist:
						break;
					default:
						throw new UnreachableSituationException();
				}
			}
		}
		boolean rFunc = false;
		TRole RF = R;
		ConceptWDep rFuncRestriction = null;
		List<ConceptWDep> list = curNode.beginl_cc();
		for (int i = 0; i < list.size(); i++) {
			ConceptWDep LC = list.get(i);
			final DLVertex ver = DLHeap.get(LC.getConcept());
			if (LC.getConcept() > 0 && isFunctionalVertex(ver)
					&& R.lesserequal(ver.getRole())) {
				if (!rFunc || RF.lesserequal(ver.getRole())) {
					rFunc = true;
					RF = ver.getRole();
					rFuncRestriction = LC;
				}
			}
		}
		if (rFunc) {
			DlCompletionTreeArc functionalArc = null;
			DepSet newDep = DepSetFactory.create();
			for (int i = 0; i < curNode.getNeighbour().size()
					&& functionalArc == null; i++) {
				DlCompletionTreeArc pr = curNode.getNeighbour().get(i);
				if (pr.isNeighbour(RF, newDep)) {
					functionalArc = pr;
				}
			}
			if (functionalArc != null) {
				LL.print(Templates.COMMON_TACTIC_BODY_SOME2, rFuncRestriction);
				DlCompletionTree succ = functionalArc.getArcEnd();
				newDep.add(dep);
				if (R.isDisjoint()
						&& checkDisjointRoleClash(curNode, succ, R, newDep)) {
					return true;
				}
				functionalArc = CGraph.addRoleLabel(curNode, succ,
						functionalArc.isPredEdge(), R, newDep);
				// adds concept to the end of arc
				if (addToDoEntry(succ, C, newDep, null)) {
					return true;
				}
				// if new role label was added...
				if (!RF.equals(R)) {
					// add Range and Domain of a new role; this includes functional, so remove it from the latter
					if (initHeadOfNewEdge(curNode, R, newDep, "RD")) {
						return true;
					}
					if (initHeadOfNewEdge(succ, R.inverse(), newDep, "RR")) {
						return true;
					}
					// check AR.C in both sides of functionalArc
					// FIXME!! for simplicity, check the functionality here (see bEx017). It seems
					// only necessary when R has several functional super-roles, so the condition
					// can be simplified
					if (applyUniversalNR(curNode, functionalArc, newDep,
							redoForall.getValue() | redoFunc.getValue())) {
						return true;
					}
					// if new role label was added to a functionalArc, some functional restrictions
					// in the SUCC node might became applicable. See bFunctional1x test
					if (applyUniversalNR(succ, functionalArc.getReverse(),
							newDep, redoForall.getValue() | redoFunc.getValue()
									| redoAtMost.getValue())) {
						return true;
					}
				}
				return false;
			}
		}
		return createNewEdge(cur.getRole(), C, Redo.redoForall.getValue()
				| Redo.redoAtMost.getValue());
	}

	private boolean commonTacticBodyValue(final TRole R, final TIndividual nom) {
		DepSet dep = DepSetFactory.create(curConcept.getDep());
		if (isCurNodeBlocked()) {
			return false;
		}
		stats.nSomeCalls.inc();
		assert nom.getNode() != null;
		DlCompletionTree realNode = nom.getNode().resolvePBlocker(dep);
		if (R.isDisjoint() && checkDisjointRoleClash(curNode, realNode, R, dep)) {
			return true;
		}
		encounterNominal = true;
		DlCompletionTreeArc edge = CGraph.addRoleLabel(curNode, realNode,
				false, R, dep);
		// add all necessary concepts to both ends of the edge
		return setupEdge(edge, dep, redoForall.getValue() | redoFunc.getValue()
				| redoAtMost.getValue() | redoIrr.getValue());
	}

	private boolean createNewEdge(final TRole R, int C, int flags) {
		//	CGraph.Print(LL);
		if (isCurNodeBlocked()) {
			stats.nUseless.inc();
			return false;
		}
		DlCompletionTreeArc pA = createOneNeighbour(R, curConcept.getDep());
		// add necessary label
		return initNewNode(pA.getArcEnd(), curConcept.getDep(), C)
				|| setupEdge(pA, curConcept.getDep(), flags);
	}

	private DlCompletionTreeArc createOneNeighbour(final TRole R,
			final DepSet dep) {
		return createOneNeighbour(R, dep, DlCompletionTree.BlockableLevel);
	}

	private DlCompletionTreeArc createOneNeighbour(final TRole R,
			final DepSet dep, int level) {
		boolean forNN = level != DlCompletionTree.BlockableLevel;
		DlCompletionTreeArc pA = CGraph.createNeighbour(curNode, forNN, R, dep);
		DlCompletionTree node = pA.getArcEnd();
		if (forNN) {
			node.setNominalLevel(level);
		}
		if (R.isDataRole()) {
			node.setDataNode();
		}
		if (IfDefs._USE_LOGGING) {
			LL.print((R.isDataRole() ? Templates.DN : Templates.CN),
					node.getId(), dep);
		}
		return pA;
	}

	/// check whether current node is blocked
	boolean isCurNodeBlocked() {
		if (!useLazyBlocking) {
			return curNode.isBlocked();
		}
		if (!curNode.isBlocked() && curNode.isAffected()) {
			updateLevel(curNode, curConcept.getDep());
			CGraph.detectBlockedStatus(curNode);
		}
		return curNode.isBlocked();
	}

	void applyAllGeneratingRules(DlCompletionTree node) {
		List<ConceptWDep> base = node.label().get_cc();
		for (int i = 0; i < base.size(); i++) {
			ConceptWDep p = base.get(i);
			if (p.getConcept() <= 0) {
				DLVertex v = DLHeap.get(p.getConcept());
				if (v.Type() == dtLE || v.Type() == dtForall) {
					addExistingToDoEntry(node, p, "ubd");
				}
			}
		}
	}

	public boolean setupEdge(DlCompletionTreeArc pA, final DepSet dep, int flags) {
		DlCompletionTree child = pA.getArcEnd();
		DlCompletionTree from = pA.getReverse().getArcEnd();
		// adds Range and Domain
		if (initHeadOfNewEdge(from, pA.getRole(), dep, "RD")) {
			return true;
		}
		if (initHeadOfNewEdge(child, pA.getReverse().getRole(), dep, "RR")) {
			return true;
		}
		// check if we have any AR.X concepts in current node
		if (applyUniversalNR(from, pA, dep, flags)) {
			return true;
		}
		// for nominal children and loops -- just apply things for the inverses
		if (pA.isPredEdge() || child.isNominalNode() || child.equals(from)) {
			if (applyUniversalNR(child, pA.getReverse(), dep, flags)) {
				return true;
			}
		} else {
			if (child.isDataNode()) {
				checkDataNode = true;
				if (checkDataClash(child)) {
					return true;
				}
			} else {
				// check if it is possible to use cache for new node
				if (usageByState(tryCacheNode(child))) {
					return true;
				}
			}
		}
		// all done
		return false;
	}

	/** add necessary concepts to the head of the new EDGE */
	private boolean initHeadOfNewEdge(DlCompletionTree node, final TRole R,
			final DepSet dep, final String reason) {
		// if R is functional, then add FR with given DEP-set to NODE
		if (R.isFunctional()) {
			for (TRole r : R.begin_topfunc()) {
				if (addToDoEntry(node, r.getFunctional(), dep, "fr")) {
					return true;
				}
			}
		}
		// setup Domain for R
		if (addToDoEntry(node, R.getBPDomain(), dep, reason)) {
			return true;
		}
		if (!IfDefs.RKG_UPDATE_RND_FROM_SUPERROLES) {
			List<TRole> list = R.getAncestor();
			for (int i = 0; i < list.size(); i++) {
				TRole q = list.get(i);
				if (addToDoEntry(node, q.getBPDomain(), dep, reason)) {
					return true;
				}
			}
		}
		return false;
	}

	boolean commonTacticBodyFunc(final DLVertex cur) {
		assert curConcept.getConcept() > 0 && isFunctionalVertex(cur);
		if (isNNApplicable(cur.getRole(), Helper.bpTOP,
				curConcept.getConcept() + 1)) {
			return commonTacticBodyNN(cur);
		}
		stats.nFuncCalls.inc();
		if (isQuickClashLE(cur)) {
			return true;
		}
		findNeighbours(cur.getRole(), Helper.bpTOP, null);
		if (EdgesToMerge.size() < 2) {
			return false;
		}
		DlCompletionTreeArc q = EdgesToMerge.get(0);//.begin();
		DlCompletionTree sample = q.getArcEnd();
		DepSet depF = DepSetFactory.create(curConcept.getDep());
		depF.add(q.getDep());
		for (int i = 1; i < EdgesToMerge.size(); i++) {//++q; q != EdgesToMerge.end(); ++q )
			q = EdgesToMerge.get(i);
			if (!q.getArcEnd().isPBlocked()) {
				if (Merge(q.getArcEnd(), sample,
						DepSetFactory.plus(depF, q.getDep()))) {
					return true;
				}
			}
		}
		return false;
	}

	boolean applyCh(DLVertex cur) {
		int C = cur.getC();
		TRole R = cur.getRole();
		BCLE bcLE = null;
		//applyCh:
		// check if we have Qualified NR
		if (C != Helper.bpTOP) {
			if (commonTacticBodyChoose(R, C)) {
				return true;
			}
		}
		// check whether we need to apply NN rule first
		if (isNNApplicable(R, C, /*stopper=*/
		curConcept.getConcept() + cur.getNumberLE())) {
			//applyNN: 
			return commonTacticBodyNN(cur); // after application <=-rule would be checked again
		}
		// if we are here that it IS first LE call
		if (isQuickClashLE(cur)) {
			return true;
		}
		// we need to repeate merge until there will be necessary amount of edges
		while (true) {
			if (isFirstBranchCall()) {
				DepSet dep = DepSetFactory.create();
				// check the amount of neighbours we have
				findNeighbours(R, C, dep);
				// if the number of R-neighbours satisfies condition -- nothing to do
				if (EdgesToMerge.size() <= cur.getNumberLE()) {
					return false;
				}
				// init context
				createBCLE();
				bContext.branchDep.add(dep);
				// setup BCLE
				bcLE = (BCLE) bContext;
				List<DlCompletionTreeArc> temp = EdgesToMerge;
				EdgesToMerge = bcLE.getEdgesToMerge();
				bcLE.setEdgesToMerge(temp);
				bcLE.resetMCI();
			}
			{
				DlCompletionTreeArc from = null;
				DlCompletionTreeArc to = null;
				boolean applyLE = true;
				while (applyLE) {
					applyLE = false;
					// skip init, because here we are after restoring
					bcLE = (BCLE) bContext;
					if (bcLE.noMoreLEOptions()) { // set global clashset to cummulative one from previous branch failures
						useBranchDep();
						return true;
					}
					// get from- and to-arcs using corresponding indexes in Edges
					from = bcLE.getFrom();
					to = bcLE.getTo();
					Reference<DepSet> dep = new Reference<DepSet>(
							DepSetFactory.create()); // empty dep-set
					// fast check for from->end() and to->end() are in \neq
					if (CGraph.nonMergable(from.getArcEnd(), to.getArcEnd(),
							dep)) {
						if (C == Helper.bpTOP) {
							setClashSet(dep.getReference());
						} else {
							// QCR: update dep-set wrt C
							// here we know that C is in both labels; set a proper clash-set
							DagTag tag = DLHeap.get(C).Type();
							//							ConceptWDep CWD = new ConceptWDep(C,
							//									dep.getReference());
							boolean test;
							// here dep contains the clash-set
							test = findConceptClash(from.getArcEnd().label()
									.getLabel(tag), C, dep.getReference());
							assert test;
							dep.setReference(DepSetFactory.plus(
									dep.getReference(), getClashSet()));
							// save new dep-set
							test = findConceptClash(to.getArcEnd().label()
									.getLabel(tag), C, dep.getReference());
							assert test;
							// both clash-sets are now in common clash-set
						}
						updateBranchDep();
						bContext.nextOption();
						applyLE = true;
					}
				}
				save();
				// add depset from current level and FROM arc and to current dep.set
				DepSet curDep = getCurDepSet();
				curDep.add(from.getDep());
				if (Merge(from.getArcEnd(), to.getArcEnd(), curDep)) {
					return true;
				}
				// it might be the case (see bIssue28) that after the merge there is an R-neigbour
				// that have neither C or ~C in its label (it was far in the nominal cloud)
				if (C != Helper.bpTOP) {
					if (commonTacticBodyChoose(R, C)) {
						return true;
					}
				}
			}
			//		return false;
		}
	}

	private boolean commonTacticBodyLE(final DLVertex cur) {
		assert curConcept.getConcept() > 0 && cur.Type() == dtLE;
		stats.nLeCalls.inc();
		int C = cur.getC();
		TRole R = cur.getRole();
		//tacticUsage ret = tacticUsage.utUnusable;
		BCLE bcLE = null;
		if (!isFirstBranchCall()) {
			if (bContext instanceof BCNN) {
				//break applyNN; // clash in NN-rule: skip choose-rule
				return commonTacticBodyNN(cur); // after application <=-rule would be checked again
			}
			if (bContext instanceof BCLE)
			//break applyLE; // clash in LE-rule: skip all the rest
			{
				// we need to repeate merge until there will be necessary amount of edges
				while (true) {
					{
						DlCompletionTreeArc from = null;
						DlCompletionTreeArc to = null;
						boolean applyLE = true;
						while (applyLE) {
							applyLE = false;
							// skip init, because here we are after restoring
							bcLE = (BCLE) bContext;
							if (bcLE.noMoreLEOptions()) { // set global clashset to cummulative one from previous branch failures
								useBranchDep();
								return true;
							}
							// get from- and to-arcs using corresponding indexes in Edges
							from = bcLE.getFrom();
							to = bcLE.getTo();
							Reference<DepSet> dep = new Reference<DepSet>(
									DepSetFactory.create()); // empty dep-set
							// fast check for from.end() and to.end() are in \neq
							if (CGraph.nonMergable(from.getArcEnd(),
									to.getArcEnd(), dep)) {
								if (C == Helper.bpTOP) {
									setClashSet(dep.getReference());
								} else // QCR: update dep-set wrt C
								{
									// here we know that C is in both labels; set a proper clash-set
									DagTag tag = DLHeap.get(C).Type();
									//									ConceptWDep CWD = new ConceptWDep(C,
									//											dep.getReference());
									boolean test;
									// here dep contains the clash-set
									test = findConceptClash(from.getArcEnd()
											.label().getLabel(tag), C,
											dep.getReference());
									assert test;
									dep.setReference(DepSetFactory.plus(
											dep.getReference(), getClashSet())); // save new dep-set
									test = findConceptClash(to.getArcEnd()
											.label().getLabel(tag), C,
											dep.getReference());
									assert test;
									// both clash-sets are now in common clash-set
								}
								updateBranchDep();
								bContext.nextOption();
								applyLE = true;
							}
						}
						save();
						// add depset from current level and FROM arc and to current dep.set
						DepSet curDep = getCurDepSet();
						curDep.add(from.getDep());
						if (Merge(from.getArcEnd(), to.getArcEnd(), curDep)) {
							return true;
						}
						// it might be the case (see bIssue28) that after the merge there is an R-neigbour
						// that have neither C or ~C in its label (it was far in the nominal cloud)
						if (C != Helper.bpTOP) {
							if (commonTacticBodyChoose(R, C)) {
								return true;
							}
						}
					}
					if (isFirstBranchCall()) {
						DepSet dep = DepSetFactory.create();
						// check the amount of neighbours we have
						findNeighbours(R, C, dep);
						// if the number of R-neighbours satisfies condition -- nothing to do
						if (EdgesToMerge.size() <= cur.getNumberLE()) {
							return false;
						}
						// init context
						createBCLE();
						bContext.branchDep.add(dep);
						// setup BCLE
						bcLE = (BCLE) bContext;
						List<DlCompletionTreeArc> temp = EdgesToMerge;
						EdgesToMerge = bcLE.getEdgesToMerge();
						bcLE.setEdgesToMerge(temp);
						bcLE.resetMCI();
					}
				}
			}
			assert bContext instanceof BCChoose;
			//break applyCh; // clash in choose-rule: redo all
			return applyCh(cur);
		}
		return applyCh(cur);
	}

	private boolean commonTacticBodyGE(final DLVertex cur) {
		if (isCurNodeBlocked()) {
			return false;
		}
		stats.nGeCalls.inc();
		if (isQuickClashGE(cur)) {
			return true;
		}
		// create N new different edges
		return createDifferentNeighbours(cur.getRole(), cur.getC(),
				curConcept.getDep(), cur.getNumberGE(),
				DlCompletionTree.BlockableLevel);
	}

	private boolean createDifferentNeighbours(final TRole R, int C,
			final DepSet dep, int n, int level) {
		DlCompletionTreeArc pA = null;
		CGraph.initIR();
		for (int i = 0; i < n; ++i) {
			pA = createOneNeighbour(R, dep, level);
			DlCompletionTree child = pA.getArcEnd();
			CGraph.setCurIR(child, dep);
			// add necessary new node labels and setup new edge
			if (initNewNode(child, dep, C)) {
				return true;
			}
			if (setupEdge(pA, dep, redoForall.getValue())) {
				return true;
			}
		}
		CGraph.finiIR();
		// re-apply all <= NR in curNode; do it only once for all created nodes; no need for Irr
		return applyUniversalNR(curNode, pA, dep, redoFunc.getValue()
				| redoAtMost.getValue());
	}

	boolean isNRClash(final DLVertex atleast, final DLVertex atmost,
			final ConceptWDep reason) {
		if (atmost.Type() != DagTag.dtLE || atleast.Type() != DagTag.dtLE) {
			return false;
		}
		if (!checkNRclash(atleast, atmost)) {
			return false;
		}
		setClashSet(DepSetFactory.plus(curConcept.getDep(), reason.getDep()));
		if (IfDefs._USE_LOGGING) {
			logClash(curNode, reason.getConcept(), reason.getDep());
		}
		return true;
	}

	boolean checkMergeClash(final CGLabel from, final CGLabel to,
			final DepSet dep, int nodeId) {
		DepSet clashDep = DepSetFactory.create(dep);
		boolean clash = false;
		for (ConceptWDep p : from.get_sc()) {
			int inverse = -p.getConcept();
			if (used.contains(inverse)
					&& findConceptClash(to.getLabel(dtPConcept), inverse,
							p.getDep())) {
				clash = true;
				clashDep.add(getClashSet());
				LL.print(Templates.CHECK_MERGE_CLASH, nodeId, p.getConcept(),
						DepSetFactory.plus(getClashSet(), dep));
			}
		}
		for (ConceptWDep p : from.get_cc()) {
			int inverse = -p.getConcept();
			if (used.contains(inverse)
					&& findConceptClash(to.getLabel(dtForall), inverse,
							p.getDep())) {
				clash = true;
				clashDep.add(getClashSet());
				LL.print(Templates.CHECK_MERGE_CLASH, nodeId, p.getConcept(),
						DepSetFactory.plus(getClashSet(), dep));
			}
		}
		if (clash) {
			setClashSet(clashDep);
		}
		return clash;
	}

	private boolean mergeLabels(final CGLabel from, DlCompletionTree to,
			final DepSet dep) {
		CGLabel lab = to.label();
		CWDArray sc = lab.getLabel(dtPConcept);
		CWDArray cc = lab.getLabel(dtForall);
		if (!dep.isEmpty()) {
			CGraph.saveRareCond(sc.updateDepSet(dep));
			CGraph.saveRareCond(cc.updateDepSet(dep));
		}
		List<ConceptWDep> list = from.get_sc();
		for (int i = 0; i < list.size(); i++) {
			ConceptWDep p = list.get(i);
			int bp = p.getConcept();
			stats.nLookups.inc();
			int index = sc.index(bp);
			if (index > -1) {
				if (!p.getDep().isEmpty()) {
					CGraph.saveRareCond(sc.updateDepSet(index, p.getDep()));
				}
			} else {
				if (insertToDoEntry(to, bp,
						DepSetFactory.plus(dep, p.getDep()), DLHeap.get(bp)
								.Type(), "M")) {
					return true;
				}
			}
		}
		list = from.get_cc();
		for (int i = 0; i < list.size(); i++) {
			ConceptWDep p = list.get(i);
			int bp = p.getConcept();
			stats.nLookups.inc();
			int index = cc.index(bp);
			if (index > -1) {
				if (!p.getDep().isEmpty()) {
					CGraph.saveRareCond(cc.updateDepSet(index, p.getDep()));
				}
			} else {
				if (insertToDoEntry(to, bp,
						DepSetFactory.plus(dep, p.getDep()), DLHeap.get(bp)
								.Type(), "M")) {
					return true;
				}
			}
		}
		return false;
	}

	boolean Merge(DlCompletionTree from, DlCompletionTree to, final DepSet depF) {
		assert !from.isPBlocked();
		assert !from.equals(to);
		assert to.getNominalLevel() <= from.getNominalLevel();
		LL.print(Templates.MERGE, from.getId(), to.getId());
		stats.nMergeCalls.inc();
		DepSet dep = DepSetFactory.create(depF);
		Reference<DepSet> ref = new Reference<DepSet>(dep);
		if (CGraph.nonMergable(from, to, ref)) {
			setClashSet(ref.getReference());
			return true;
		}
		if (checkMergeClash(from.label(), to.label(), depF, to.getId())) {
			return true;
		}
		// copy all node labels
		if (mergeLabels(from.label(), to, depF)) {
			return true;
		}
		List<DlCompletionTreeArc> edges = new ArrayList<DlCompletionTreeArc>();
		CGraph.Merge(from, to, depF, edges);
		for (DlCompletionTreeArc q : edges) {
			if (q.getRole().isDisjoint()
					&& checkDisjointRoleClash(q.getReverse().getArcEnd(),
							q.getArcEnd(), q.getRole(), depF)) {
				{
					return true;
				}
			}
		}
		if (to.isDataNode()) {
			return checkDataClash(to);
		}
		for (DlCompletionTreeArc q : edges) {
			if (applyUniversalNR(
					to,
					q,
					depF,
					redoForall.getValue() | redoFunc.getValue()
							| redoAtMost.getValue() | redoIrr.getValue())) {
				return true;
			}
		}
		return false;
	}

	boolean checkDisjointRoleClash(DlCompletionTree from, DlCompletionTree to,
			final TRole R, final DepSet dep) {
		for (DlCompletionTreeArc p : from.getNeighbour()) {
			if (checkDisjointRoleClash(p, to, R, dep)) {
				return true;
			}
		}
		return false;
	}

	boolean isNewEdge(final DlCompletionTree node, List<DlCompletionTreeArc> e) {
		for (DlCompletionTreeArc q : e) {
			if (q.getArcEnd().equals(node)) {
				return false;
			}
		}
		return true;
	}

	void findNeighbours(final TRole Role, int c, DepSet Dep) {
		EdgesToMerge.clear();
		DagTag tag = DLHeap.get(c).Type();
		for (DlCompletionTreeArc p : curNode.getNeighbour()) {
			if (p.isNeighbour(Role)
					&& isNewEdge(p.getArcEnd(), EdgesToMerge)
					&& findChooseRuleConcept(p.getArcEnd().label()
							.getLabel(tag), c, Dep)) {
				EdgesToMerge.add(p);
			}
		}
		Collections.sort(EdgesToMerge, new EdgeCompare());
	}

	boolean commonTacticBodyChoose(final TRole R, int C) {
		for (DlCompletionTreeArc p : curNode.getNeighbour()) {
			if (p.isNeighbour(R)) {
				if (applyChooseRule(p.getArcEnd(), C)) {
					return true;
				}
			}
		}
		return false;
	}

	boolean applyChooseRule(DlCompletionTree node, int C) {
		if (node.isLabelledBy(C) || node.isLabelledBy(-C)) {
			return false;
		}
		if (isFirstBranchCall()) {
			createBCCh();
			save();
			return addToDoEntry(node, -C, getCurDepSet(), "cr0");
		} else {
			prepareBranchDep();
			DepSet dep = DepSetFactory.create(getBranchDep());
			determiniseBranchingOp();
			return addToDoEntry(node, C, dep, "cr1");
		}
	}

	boolean commonTacticBodyNN(final DLVertex cur) {
		stats.nNNCalls.inc();
		if (isFirstBranchCall()) {
			createBCNN();
		}
		final BCNN bcNN = (BCNN) bContext;
		if (bcNN.noMoreNNOptions(cur.getNumberLE())) {
			useBranchDep();
			return true;
		}
		int NN = bcNN.getBranchIndex();
		save();
		// new (just branched) dep-set
		DepSet curDep = getCurDepSet();
		// make a stopper to mark that NN-rule is applied
		if (addToDoEntry(curNode, curConcept.getConcept() + cur.getNumberLE(),
				DepSetFactory.create(), "NNs")) {
			return true;
		}
		// create curNN new different edges
		if (createDifferentNeighbours(cur.getRole(), cur.getC(), curDep, NN,
				curNode.getNominalLevel() + 1)) {
			return true;
		}
		// now remember NR we just created: it is (<= curNN R), so have to find it
		return addToDoEntry(curNode,
				curConcept.getConcept() + cur.getNumberLE() - NN, curDep, "NN");
	}

	boolean isNNApplicable(final TRole r, int C, int stopper) {
		if (curNode.isNominalNode()) {
			System.out.println("DlSatTester.isNNApplicable()");
		}
		return false;
	}

	boolean commonTacticBodySomeSelf(final TRole R) {
		if (isCurNodeBlocked()) {
			return false;
		}
		for (DlCompletionTreeArc p : curNode.getNeighbour()) {
			if (p.getArcEnd().equals(curNode) && p.isNeighbour(R)) {
				return false;
			}
		}
		final DepSet dep = DepSetFactory.create(curConcept.getDep());
		DlCompletionTreeArc pA = CGraph.createLoop(curNode, R, dep);
		return setupEdge(pA, dep, redoForall.getValue() | redoFunc.getValue()
				| redoAtMost.getValue() | redoIrr.getValue());
	}

	boolean commonTacticBodyIrrefl(final TRole R) {
		for (DlCompletionTreeArc p : curNode.getNeighbour()) {
			if (checkIrreflexivity(p, R, curConcept.getDep())) {
				return true;
			}
		}
		return false;
	}

	boolean commonTacticBodyProj(final TRole R, int C, final TRole ProjR) {
		if (curNode.isLabelledBy(-C)) {
			return false;
		}
		for (int i = 0; i < curNode.getNeighbour().size(); i++) {
			if (curNode.getNeighbour().get(i).isNeighbour(R)) {
				if (checkProjection(curNode.getNeighbour().get(i), C, ProjR)) {
					return true;
				}
			}
		}
		return false;
	}

	boolean checkProjection(DlCompletionTreeArc pA, int C, final TRole ProjR) {
		if (pA.isNeighbour(ProjR)) {
			return false;
		}
		if (curNode.isLabelledBy(-C)) {
			return false;
		}
		//tacticUsage ret = tacticUsage.utUnusable;
		DepSet dep = DepSetFactory.create(curConcept.getDep());
		dep.add(pA.getDep());
		if (!curNode.isLabelledBy(C)) {
			if (isFirstBranchCall()) {
				createBCCh();
				save();
				return addToDoEntry(curNode, -C, getCurDepSet(), "cr0");
			} else {
				prepareBranchDep();
				dep.add(getBranchDep());
				determiniseBranchingOp();
				if (addToDoEntry(curNode, C, dep, "cr1")) {
					return true;
				}
			}
		}
		DlCompletionTree child = pA.getArcEnd();
		return setupEdge(CGraph.addRoleLabel(curNode, child, pA.isPredEdge(),
				ProjR, dep), dep, redoForall.getValue() | redoFunc.getValue()
				| redoAtMost.getValue() | redoIrr.getValue());
	}
}

enum AddConceptResult {
	acrClash, acrExist, acrDone
}

class EdgeCompare implements Comparator<DlCompletionTreeArc>, Serializable {
	public int compare(DlCompletionTreeArc o1, DlCompletionTreeArc o2) {
		return o1.getArcEnd().compareTo(o2.getArcEnd());
	}
}

/** possible flags of re-checking ALL-like expressions in new nodes */
enum Redo {
	redoForall(1), redoFunc(2), redoAtMost(4), redoIrr(8);
	private final int value;

	Redo(int i) {
		value = i;
	}

	protected int getValue() {
		return value;
	}
}