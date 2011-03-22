package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.Helper.*;
import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.logger;
import static uk.ac.manchester.cs.jfact.kernel.DagTag.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.manchester.cs.jfact.dep.DepSet;
import uk.ac.manchester.cs.jfact.dep.DepSetFactory;
import uk.ac.manchester.cs.jfact.helpers.ArrayIntMap;
import uk.ac.manchester.cs.jfact.helpers.DLVertex;
import uk.ac.manchester.cs.jfact.helpers.Helper;
import uk.ac.manchester.cs.jfact.helpers.IfDefs;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;
import uk.ac.manchester.cs.jfact.helpers.Reference;
import uk.ac.manchester.cs.jfact.kernel.state.DLCompletionTreeSaveState;
import uk.ac.manchester.cs.jfact.kernel.state.SaveList;

public final class DlCompletionTree implements Comparable<DlCompletionTree> {
	/** restore blocked node */
	static final class UnBlock extends Restorer {
		private final DlCompletionTree p;
		private final DlCompletionTree unblockBlocker;
		private final DepSet dep;
		private final boolean pBlocked;
		private final boolean dBlocked;

		public UnBlock(DlCompletionTree q) {
			p = q;
			unblockBlocker = q.blocker;
			dep = DepSetFactory.create(q.pDep);
			pBlocked = q.pBlocked;
			dBlocked = q.dBlocked;
		}

		@Override
		public void restore() {
			p.setBlocker(unblockBlocker);
			p.pDep = DepSetFactory.create(dep);
			p.pBlocked = pBlocked;
			p.dBlocked = dBlocked;
		}
	}

	/** restore (un)cached node */
	final static class CacheRestorer extends Restorer {
		private final DlCompletionTree p;
		private final boolean isCached;

		public CacheRestorer(DlCompletionTree q) {
			p = q;
			isCached = q.cached;
		}

		@Override
		public void restore() {
			p.cached = isCached;
		}
	}

	/** restore node after IR set change */
	final class IRRestorer extends Restorer {
		private final int n;

		public IRRestorer() {
			n = inequalityRelation.size();
		}

		@Override
		public void restore() {
			Helper.resize(inequalityRelation, n);
			inequalityRelation_helper.clear();
			//TODO check performances of this
			for (int i = 0; i < inequalityRelation.size(); i++) {
				if (inequalityRelation.get(i) != null) {
					inequalityRelation_helper.put(inequalityRelation.get(i).getConcept(), inequalityRelation.get(i));
				}
			}
		}
	}

	/** label of a node */
	private CGLabel label = new CGLabel();
	//TODO check for better access
	/** inequality relation information respecting current node */
	protected final List<ConceptWDep> inequalityRelation = new ArrayList<ConceptWDep>();
	protected final Map<Integer, ConceptWDep> inequalityRelation_helper = new HashMap<Integer, ConceptWDep>();
	//TODO check whether access should be improved
	/** Neighbours information */
	private final List<DlCompletionTreeArc> neighbour = new ArrayList<DlCompletionTreeArc>();
	/** pointer to last saved node */
	private final SaveList saves = new SaveList();
	/** ID of node (used in print) */
	private final int id;
	/** concept that init the newly created node */
	private int init;
	/** blocker of a node */
	protected DlCompletionTree blocker;
	/** dep-set for Purge op */
	protected DepSet pDep = DepSetFactory.create();
	// save state information
	protected int curLevel; // current level
	/** is given node a data node */
	private boolean flagDataNode = true;
	/** flag if node is Cached */
	protected boolean cached = true;
	/** flag whether node is permanently/temporarily blocked */
	protected boolean pBlocked = true;
	/** flag whether node is directly/indirectly blocked */
	protected boolean dBlocked = true;
	/**
	 * Whether node is affected by change of some potential blocker. This flag
	 * may be viewed as a cache for a 'blocked' status
	 */
	private boolean affected = true;
	/** level of a nominal node; 0 means blockable one */
	private int nominalLevel;

	/** check if B2 holds for given DL vertex with C=V */
	private boolean B2(final DLVertex v, int C) {
		assert hasParent();
		RAStateTransitions RST = v.getRole().getAutomaton().getBase().get(v.getState());
		if (v.getRole().isSimple()) {
			return B2Simple(RST, v.getConceptIndex());
		} else {
			if (RST.isEmpty()) {
				return true;
			}
			if (RST.isSingleton()) {
				return B2Simple(RST, C - v.getState() + RST.getTransitionEnd());
			}
			return B2Complex(RST, C - v.getState());
		}
	}

	/** check whether a node can block another one with init concept C */
	public boolean canBlockInit(int C) {
		if (C == bpBOTTOM) {
			return false;
		}
		if (C == bpTOP) {
			return true;
		}
		return label.contains(C);
	}

	/** log saving/restoring node */
	private void logSRNode(final String action) {
		logger.print(Templates.LOG_SR_NODE, action, id, neighbour.size(), curLevel);
	}

	/** get letter corresponding to the blocking mode */
	private String getBlockingStatusName() {
		return isPBlocked() ? "p" : isDBlocked() ? "d" : isIBlocked() ? "i" : "u";
	}

	/** log node status (d-,i-,p-blocked or cached */
	private String logNodeBStatus() {
		String toReturn = "";
		// blocking status information
		if (blocker != null) {
			toReturn += getBlockingStatusName();
			toReturn += blocker.id;
		}
		if (isCached()) {
			toReturn += "c";
		}
		return toReturn;
	}

	/** log if node became p-blocked */
	private void logNodeBlocked() {
		logger.print(Templates.LOG_NODE_BLOCKED, getBlockingStatusName(), id, (blocker == null ? "" : ","), (blocker == null ? "" : blocker.id));
	}

	public DlCompletionTree(int newId) {
		id = newId;
	}

	/** add given arc P as a neighbour */
	public void addNeighbour(DlCompletionTreeArc p) {
		neighbour.add(p);
	}

	/** get Node's id */
	public int getId() {
		return id;
	}

	/** check if the node is cached (IE need not to be expanded) */
	public boolean isCached() {
		return cached;
	}

	/** set cached status of given node */
	public Restorer setCached(boolean val) {
		if (cached == val) {
			return null;
		}
		Restorer ret = new CacheRestorer(this);
		cached = val;
		return ret;
	}

	// data node methods
	public boolean isDataNode() {
		return flagDataNode;
	}

	public void setDataNode() {
		flagDataNode = true;
	}

	// nominal node methods
	public boolean isBlockableNode() {
		return nominalLevel == BLOCKABLE_LEVEL;
	}

	public boolean isNominalNode() {
		return nominalLevel != BLOCKABLE_LEVEL;
	}

	public void setNominalLevel() {
		setNominalLevel(0);
	}

	public void setNominalLevel(int newLevel) {
		nominalLevel = newLevel;
	}

	public int getNominalLevel() {
		return nominalLevel;
	}

	/**
	 * adds concept P to a label, defined by TAG; update blocked status if
	 * necessary
	 */
	public void addConcept(final ConceptWDep p, DagTag tag) {
		label.add(tag, p);
	}

	/** set the Init concept */
	public void setInit(int p) {
		init = p;
	}

	public int getInit() {
		return init;
	}

	public List<DlCompletionTreeArc> getNeighbour() {
		return neighbour;
	}

	/** return true if node is a non-root; works for reflexive roles */
	public boolean hasParent() {
		if (neighbour.isEmpty()) {
			return false;
		}
		return neighbour.get(0).isPredEdge();
	}

	/** check if SOME rule is applicable; includes transitive SOME support */
	public DlCompletionTree isSomeApplicable(final Role R, int C) {
		return R.isTransitive() ? isTSomeApplicable(R, C) : isNSomeApplicable(R, C);
	}

	/** RW access to a label */
	public CGLabel label() {
		return label;
	}

	// label iterators
	/** begin() iterator for a label with simple concepts */
	public List<ConceptWDep> beginl_sc() {
		return label.get_sc();
	}

	/** begin() iterator for a label with complex concepts */
	public List<ConceptWDep> beginl_cc() {
		return label.get_cc();
	}

	/** begin() iterator for a label with simple concepts */
	public ArrayIntMap beginl_sc_concepts() {
		return label.get_sc_concepts();
	}

	/** begin() iterator for a label with complex concepts */
	public ArrayIntMap beginl_cc_concepts() {
		return label.get_cc_concepts();
	}

	/** check whether node's label contains P */
	public boolean isLabelledBy(int p) {
		return label.contains(p);
	}

	//  Blocked-By methods for different logics
	/** check blocking condition for SH logic */
	public boolean isBlockedBy_SH(final DlCompletionTree p) {
		return label.lesserequal(p.label);
	}

	/** check blocking condition for SHI logic */
	public boolean isBlockedBy_SHI(final DLDag dag, final DlCompletionTree p) {
		return isCommonlyBlockedBy(dag, p);
	}

	/** check blocking condition for SHIQ logic using optimised blocking */
	public boolean isBlockedBy_SHIQ(final DLDag dag, final DlCompletionTree p) {
		return isCommonlyBlockedBy(dag, p) && (isCBlockedBy(dag, p) || isABlockedBy(dag, p));
	}

	// WARNING!! works only for blockable nodes
	// every non-root node will have first upcoming edge pointed to a parent
	/**
	 * return RO pointer to the parent node; WARNING: correct only for nodes
	 * with hasParent()==TRUE
	 */
	//final DlCompletionTree getParentNode () { return (begin()).getArcEnd(); }
	/**
	 * return RW pointer to the parent node; WARNING: correct only for nodes
	 * with hasParent()==TRUE
	 */
	public DlCompletionTree getParentNode() {
		return neighbour.get(0).getArcEnd();
	}

	// managing AFFECTED flag
	/** check whether node is affected by blocking-related changes */
	public boolean isAffected() {
		return affected;
	}

	/** set node (and all subnodes) affected */
	public void setAffected() {
		// don't mark already affected, nominal or p-blocked nodes
		if (isAffected() || isNominalNode() || isPBlocked()) {
			return;
		}
		affected = true;
		for (int i = 0; i < neighbour.size(); i++) {
			DlCompletionTreeArc q = neighbour.get(i);
			if (q.isSuccEdge()) {
				q.getArcEnd().setAffected();
			}
		}
	}

	/** clear affected flag */
	public void clearAffected() {
		affected = false;
	}

	// just returns calculated values
	/** check if node is directly blocked */
	public boolean isDBlocked() {
		return blocker != null && !pBlocked && dBlocked;
	}

	/** check if node is indirectly blocked */
	public boolean isIBlocked() {
		return blocker != null && !pBlocked && !dBlocked;
	}

	/** check if node is purged (and so indirectly blocked) */
	public boolean isPBlocked() {
		return blocker != null && pBlocked && !dBlocked;
	}

	public boolean isBlockedPBlockedNominalNodeCached() {
		return cached || isNominalNode() || isBlocked() || isPBlocked();
	}

	/** check if node is blocked (d/i) */
	public boolean isBlocked() {
		return blocker != null && !pBlocked;
	}

	/** check the legality of the direct block */
	public boolean isIllegallyDBlocked() {
		return isDBlocked() && blocker.isBlocked();
	}

	/** get access to the blocker */
	public DlCompletionTree getBlocker() {
		return blocker;
	}

	/** get purge dep-set of a given node */
	public DepSet getPurgeDep() {
		return pDep;
	}

	/** get node to which current one was merged */
	public DlCompletionTree resolvePBlocker() {
		if (isPBlocked()) {
			return blocker.resolvePBlocker();
		} else {
			return this;
		}
	}

	/** get node to which current one was merged; fills DEP from pDep's */
	public DlCompletionTree resolvePBlocker(DepSet dep) {
		if (!isPBlocked()) {
			return this;
		}
		dep.add(pDep);
		return blocker.resolvePBlocker(dep);
	}

	/**
	 * check whether the loop between a DBlocked NODE and it's parent blocked
	 * contains C
	 */
	public boolean isLoopLabelled(int c) {
		assert isDBlocked();
		if (blocker.isLabelledBy(c)) {
			return true;
		}
		for (DlCompletionTree p = getParentNode(); p.hasParent() && p != blocker; p = p.getParentNode()) {
			if (p.isLabelledBy(c)) {
				return true;
			}
		}
		return false;
	}

	// re-building blocking hierarchy
	/** set node blocked */
	private Restorer setBlocked(final DlCompletionTree blocker, boolean permanently, boolean directly) {
		Restorer ret = new UnBlock(this);
		setBlocker(blocker);
		pBlocked = permanently;
		dBlocked = directly;
		if (IfDefs.USE_LOGGING) {
			logNodeBlocked();
		}
		return ret;
	}

	/** mark node d-blocked */
	public Restorer setDBlocked(final DlCompletionTree blocker) {
		return setBlocked(blocker, false, true);
	}

	/** mark node i-blocked */
	public Restorer setIBlocked(final DlCompletionTree blocker) {
		return setBlocked(blocker, false, false);
	}

	/** mark node unblocked */
	public Restorer setUBlocked() {
		return setBlocked(null, true, true);
	}

	/** mark node purged */
	public Restorer setPBlocked(final DlCompletionTree blocker, final DepSet dep) {
		Restorer ret = new UnBlock(this);
		setBlocker(blocker);
		if (isNominalNode()) {
			pDep = DepSetFactory.create(dep);
		}
		pBlocked = true;
		dBlocked = false;
		if (IfDefs.USE_LOGGING) {
			logNodeBlocked();
		}
		return ret;
	}

	//	checking edge labelling
	/** check if edge to NODE is labelled by R; return null if does not */
	public DlCompletionTreeArc getEdgeLabelled(final Role R, final DlCompletionTree node) {
		for (int i = 0; i < neighbour.size(); i++) {
			DlCompletionTreeArc p = neighbour.get(i);
			if (p.getArcEnd().equals(node) && p.isNeighbour(R)) {
				return p;
			}
		}
		return null;
	}

	/** check if parent arc is labelled by R; works only for blockable nodes */
	private boolean isParentArcLabelled(final Role R) {
		return getEdgeLabelled(R, getParentNode()) != null;
	}

	// inequality relation interface
	/**
	 * init IR with given entry and dep-set; @return true if IR already has this
	 * label
	 */
	public boolean initIR(int level, final DepSet ds) {
		Reference<DepSet> dummy = new Reference<DepSet>(DepSetFactory.create()); // we don't need a clash-set here
		if (inIRwithC(level, ds, dummy)) {
			return true;
		}
		ConceptWDep conceptWDep = new ConceptWDep(level, ds);
		inequalityRelation.add(conceptWDep);
		inequalityRelation_helper.put(level, conceptWDep);
		return false;
	}

	/// check if IR for the node contains C
	private boolean inIRwithC(int level, DepSet ds, Reference<DepSet> dep) {
		if (inequalityRelation.isEmpty()) {
			return false;
		}
		ConceptWDep p = inequalityRelation_helper.get(level);
		if (p != null) {
			dep.getReference().add(p.getDep());
			dep.getReference().add(ds);
			return true;
		}
		return false;
	}

	// saving/restoring
	/** check if node needs to be saved */
	public boolean needSave(int newLevel) {
		return curLevel < newLevel;
	}

	/** save node using internal stack */
	public void save(int level) {
		DLCompletionTreeSaveState node = new DLCompletionTreeSaveState();
		saves.push(node);
		save(node);
		curLevel = level;
	}

	/** check if node needs to be restored */
	public boolean needRestore(int restLevel) {
		return curLevel > restLevel;
	}

	/** restore node from the topmost entry */
	void restore() {
		assert !saves.isEmpty();
		restore(saves.pop());
	}

	/** restore node to given level */
	void restore(int level) {
		restore(saves.pop(level));
	}

	// output
	/** log node information (number, i/d blockers, cached) */
	public void logNode() {
		logger.print(id);
		logger.print(logNodeBStatus());
	}

	private boolean isCommonlyBlockedBy(DLDag dag, DlCompletionTree p) {
		assert hasParent();
		if (!label.lesserequal(p.label)) {
			return false;
		}
		ArrayIntMap list = p.beginl_cc_concepts();
		for (int i = 0; i < list.size(); i++) {
			int bp = list.keySet(i);
			if (bp > 0) {
				DLVertex v = dag.get(bp);
				if (v.getType() == dtForall) {
					if (!B2(v, bp)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean isABlockedBy(final DLDag dag, final DlCompletionTree p) {
		ArrayIntMap list = p.beginl_cc_concepts();
		for (int i = 0; i < list.size(); i++) {
			int bp = list.keySet(i);
			final DLVertex v = dag.get(bp);
			if (v.getType() == dtForall && bp < 0) {
				if (!B4(p, 1, v.getRole(), -v.getConceptIndex())) {
					return false;
				}
			} else if (v.getType() == dtLE) {
				if (bp > 0) {
					if (!B3(p, v.getNumberLE(), v.getRole(), v.getConceptIndex())) {
						return false;
					}
				} else {
					if (!B4(p, v.getNumberGE(), v.getRole(), v.getConceptIndex())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean isCBlockedBy(DLDag dag, DlCompletionTree p) {
		List<ConceptWDep> list = p.beginl_cc();
		for (int i = 0; i < list.size(); i++) {
			int bp = list.get(i).getConcept();
			if (bp > 0) {
				DLVertex v = dag.get(bp);
				if (v.getType() == dtLE) {
					if (!B5(v.getRole(), v.getConceptIndex())) {
						return false;
					}
				}
			}
		}
		list = getParentNode().beginl_cc();
		for (int i = 0; i < list.size(); i++) {
			int bp = list.get(i).getConcept();
			if (bp < 0) {
				DLVertex v = dag.get(bp);
				if (v.getType() == dtLE) {
					if (!B6(v.getRole(), v.getConceptIndex())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean B2Simple(RAStateTransitions RST, int C) {
		DlCompletionTree parent = getParentNode();
		CGLabel parLab = parent.label();
		//	boolean toReturn = false;
		if (parLab.contains(C)) {
			return true;
		}
		for (int i = 0; i < neighbour.size(); i++) {
			DlCompletionTreeArc p = neighbour.get(i);
			if (!p.isIBlocked() && p.getArcEnd().equals(parent) && RST.recognise(p.getRole())) {
				return false;
			}
		}
		return true;
	}

	public boolean B2Complex(final RAStateTransitions RST, int C) {
		final DlCompletionTree parent = getParentNode();
		final CGLabel parLab = parent.label();
		for (int i = 0; i < neighbour.size(); i++) {
			DlCompletionTreeArc p = neighbour.get(i);
			if (p.isIBlocked() || !p.getArcEnd().equals(parent)) {
				continue;
			}
			Role R = p.getRole();
			if (RST.recognise(R)) {
				List<RATransition> list = RST.begin();
				for (int j = 0; j < list.size(); j++) {
					RATransition q = list.get(i);
					if (q.applicable(R)) {
						if (!parLab.containsCC(C + q.final_state())) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private boolean B3(final DlCompletionTree p, int n, final Role S, int C) {
		assert hasParent();
		boolean ret;
		if (!isParentArcLabelled(S)) {
			ret = true;
		} else if (getParentNode().isLabelledBy(-C)) {
			ret = true;
		} else if (!getParentNode().isLabelledBy(C)) {
			ret = false;
		} else {
			int m = 0;
			for (int i = 0; i < p.neighbour.size(); i++) {
				DlCompletionTreeArc q = p.neighbour.get(i);
				if (q.isSuccEdge() && q.isNeighbour(S) && q.getArcEnd().isLabelledBy(C)) {
					++m;
				}
			}
			ret = m < n;
		}
		return ret;
	}

	private boolean B4(final DlCompletionTree p, int m, final Role T, int E) {
		assert hasParent();
		if (isParentArcLabelled(T) && m == 1 && getParentNode().isLabelledBy(E)) {
			return true;
		}
		int n = 0;
		for (int i = 0; i < neighbour.size(); i++) {
			DlCompletionTreeArc q = neighbour.get(i);
			if (q.isSuccEdge() && q.isNeighbour(T) && q.getArcEnd().isLabelledBy(E)) {
				if (++n >= m) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean B5(final Role T, int E) {
		assert hasParent();
		if (!isParentArcLabelled(T)) {
			return true;
		}
		if (getParentNode().isLabelledBy(-E)) {
			return true;
		}
		return false;
	}

	private boolean B6(final Role U, int F) {
		assert hasParent();
		if (!isParentArcLabelled(U.inverse())) {
			return true;
		}
		if (isLabelledBy(-F)) {
			return true;
		}
		return false;
	}

	public static void printBlockingStat1(LogAdapter o) {
		//		if (tries[0] == 0) {
		//			return;
		//		}
		//		o.print("\nThere were made " + Arrays.toString(tries)
		//				+ " blocking tests of which " + nSucc
		//				+ " successfull.\nBlocking rules failure statistic:");
		//		for (int i = 0; i < 6; ++i) {
		//			if (i != 0) {
		//				o.print(",");
		//			}
		//			o.print(" " + fails[i] + "/" + tries[i]);
		//		}
	}

	public static void clearBlockingStat1() {
		//		for (int i = 5; i >= 0; --i) {
		//			tries[i] = fails[i] = 0;
		//		}
		//		nSucc = failedRule = 0;
	}

	/** default level for the Blockable node */
	public final static int BLOCKABLE_LEVEL = Integer.MAX_VALUE;

	public void init(int level) {
		flagDataNode = false;
		nominalLevel = BLOCKABLE_LEVEL;
		curLevel = level;
		cached = false;
		affected = true;
		// every (newly created) node can be blocked
		dBlocked = true;
		pBlocked = true;
		// unused flag combination
		// cleans the cache where Label is involved
		label.init();
		init = bpTOP;
		// node was used -- clear all previous content
		saves.clear();
		inequalityRelation.clear();
		inequalityRelation_helper.clear();
		neighbour.clear();
		setBlocker(null);
		pDep.clear();
	}

	private DlCompletionTree isTSuccLabelled(final Role R, int C) {
		if (isLabelledBy(C)) {
			return this;
		}
		if (isNominalNode()) {
			return null;
		}
		DlCompletionTree ret = null;
		for (int i = 0; i < neighbour.size(); i++) {
			DlCompletionTreeArc p = neighbour.get(i);
			if (p.isSuccEdge() && p.isNeighbour(R) && !p.isReflexiveEdge() && (ret = p.getArcEnd().isTSuccLabelled(R, C)) != null) {
				return ret;
			}
		}
		return null;
	}

	private DlCompletionTree isTPredLabelled(final Role R, int C, final DlCompletionTree from) {
		if (isLabelledBy(C)) {
			return this;
		}
		if (isNominalNode()) {
			return null;
		}
		DlCompletionTree ret = null;
		for (int i = 0; i < neighbour.size(); i++) {
			DlCompletionTreeArc p = neighbour.get(i);
			if (p.isSuccEdge() && p.isNeighbour(R) && !p.getArcEnd().equals(from) && (ret = p.getArcEnd().isTSuccLabelled(R, C)) != null) {
				return ret;
			}
		}
		if (hasParent() && isParentArcLabelled(R)) {
			return getParentNode().isTPredLabelled(R, C, this);
		} else {
			return null;
		}
	}

	private DlCompletionTree isNSomeApplicable(final Role R, int C) {
		for (int i = 0; i < neighbour.size(); i++) {
			DlCompletionTreeArc p = neighbour.get(i);
			if (p.isNeighbour(R) && p.getArcEnd().isLabelledBy(C)) {
				return p.getArcEnd();
			}
		}
		return null;
	}

	private DlCompletionTree isTSomeApplicable(final Role R, int C) {
		DlCompletionTree ret = null;
		for (int i = 0; i < neighbour.size(); i++) {
			DlCompletionTreeArc p = neighbour.get(i);
			if (p.isNeighbour(R)) {
				if (p.isPredEdge()) {
					ret = p.getArcEnd().isTPredLabelled(R, C, this);
				} else {
					ret = p.getArcEnd().isTSuccLabelled(R, C);
				}
				if (ret != null) {
					return ret;
				}
			}
		}
		return null;
	}

	private void save(DLCompletionTreeSaveState nss) {
		nss.setCurLevel(curLevel);
		nss.setnNeighbours(neighbour.size());
		label.save(nss.getLab());
		if (IfDefs.USE_LOGGING) {
			logSRNode("SaveNode");
		}
	}

	private void restore(DLCompletionTreeSaveState nss) {
		if (nss == null) {
			return;
		}
		curLevel = nss.getCurLevel();
		label.restore(nss.getLab(), curLevel);
		Helper.resize(neighbour, nss.getnNeighbours());
		affected = true;
		if (IfDefs.USE_LOGGING) {
			logSRNode("RestNode");
		}
	}

	public void printBody(LeveLogger.LogAdapter o) {
		o.print(id);
		if (isNominalNode()) {
			o.print("o");
			o.print(nominalLevel);
		}
		o.print("(");
		o.print(curLevel);
		o.print(")");
		if (isDataNode()) {
			o.print("d");
		}
		label.print(o);
		o.print(logNodeBStatus());
	}

	@Override
	public String toString() {
		StringBuilder o = new StringBuilder();
		o.append(id);
		if (isNominalNode()) {
			o.append("o");
			o.append(nominalLevel);
		}
		o.append("(");
		o.append(curLevel);
		o.append(")");
		if (isDataNode()) {
			o.append("d");
		}
		o.append(label);
		o.append(logNodeBStatus());
		return o.toString();
	}

	/// check if the NODE's and current node's IR are labelled with the same level
	public boolean nonMergable(DlCompletionTree node, Reference<DepSet> dep) {
		if (inequalityRelation.isEmpty() || node.inequalityRelation.isEmpty()) {
			return false;
		}
		for (ConceptWDep p : node.inequalityRelation) {
			if (inIRwithC(p.getConcept(), p.getDep(), dep)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * update IR of the current node with IR from NODE and additional clash-set; @return
	 * restorer
	 */
	public Restorer updateIR(DlCompletionTree node, DepSet toAdd) {
		if (node.inequalityRelation.isEmpty()) {
			throw new IllegalArgumentException();
		}
		// save current state
		Restorer ret = new IRRestorer();
		// copy all elements from NODE's IR to current node.
		// FIXME!! do not check if some of them are already in there
		for (ConceptWDep p : node.inequalityRelation) {
			// not adding those already there, they would be ignored anyway
			if (!inequalityRelation_helper.containsKey(p.getConcept())) {
				ConceptWDep conceptWDep = new ConceptWDep(p.getConcept(), toAdd);
				inequalityRelation.add(conceptWDep);
				inequalityRelation_helper.put(p.getConcept(), conceptWDep);
			}
		}
		return ret;
	}

	public int compareTo(DlCompletionTree o) {
		if (nominalLevel == o.nominalLevel) {
			return id - o.id;
		}
		return nominalLevel - o.nominalLevel;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null) {
			return false;
		}
		if (this == arg0) {
			return true;
		}
		if (arg0 instanceof DlCompletionTree) {
			DlCompletionTree arg02 = (DlCompletionTree) arg0;
			return nominalLevel == arg02.nominalLevel && id == arg02.id;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return nominalLevel * id;
	}

	public void setBlocker(DlCompletionTree blocker) {
		this.blocker = blocker;
	}
}