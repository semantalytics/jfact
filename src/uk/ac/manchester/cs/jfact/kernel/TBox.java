package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.DLTree.equalTrees;
import static uk.ac.manchester.cs.jfact.helpers.Helper.*;
import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.LL;
import static uk.ac.manchester.cs.jfact.kernel.ClassifiableEntry.resolveSynonym;
import static uk.ac.manchester.cs.jfact.kernel.DagTag.*;
import static uk.ac.manchester.cs.jfact.kernel.KBStatus.*;
import static uk.ac.manchester.cs.jfact.kernel.Token.*;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.semanticweb.owlapi.reasoner.ReasonerInternalException;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

import uk.ac.manchester.cs.jfact.helpers.DLTree;
import uk.ac.manchester.cs.jfact.helpers.DLTreeFactory;
import uk.ac.manchester.cs.jfact.helpers.DLVertex;
import uk.ac.manchester.cs.jfact.helpers.FastSetFactory;
import uk.ac.manchester.cs.jfact.helpers.IfDefs;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapterStringBuilder;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;
import uk.ac.manchester.cs.jfact.helpers.Pair;
import uk.ac.manchester.cs.jfact.helpers.TsProcTimer;
import uk.ac.manchester.cs.jfact.helpers.UnreachableSituationException;
import uk.ac.manchester.cs.jfact.kernel.datatype.DataTypeCenter;
import uk.ac.manchester.cs.jfact.kernel.datatype.TDataEntry;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataTypeName;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheConst;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheInterface;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheSingleton;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheState;

public class TBox {
	protected static final class IndividualCreator implements TNameCreator<TIndividual> {
		public TIndividual makeEntry(String name) {
			return new TIndividual(name);
		}
	}

	protected static final class ConceptCreator implements TNameCreator<TConcept> {
		public TConcept makeEntry(String name) {
			return new TConcept(name);
		}
	}

	/**
	 * class for simple rules like Ch :- Cb1, Cbi, CbN; all C are primitive
	 * named concepts
	 */
	final static class TSimpleRule {
		/** body of the rule */
		private final List<TConcept> Body = new ArrayList<TConcept>();
		/** head of the rule as a DLTree */
		protected final DLTree tHead;
		/** head of the rule as a BP */
		private int bpHead;

		public TSimpleRule(final List<TConcept> body, DLTree head) {
			Body.addAll(body);
			tHead = head;
			setBpHead(bpINVALID);
		}

		public boolean applicable(DlSatTester Reasoner) {
			return Reasoner.applicable(this);
		}

		public List<TConcept> getBody() {
			return Body;
		}

		public void setBpHead(int bpHead) {
			this.bpHead = bpHead;
		}

		public int getBpHead() {
			return bpHead;
		}
	}

	private final TLabeller relevance = new TLabeller();
	private DLDag DLHeap;
	/** reasoner for TBox-related queries w/o nominals */
	private DlSatTester stdReasoner;
	/** reasoner for TBox-related queries with nominals */
	private NominalReasoner nomReasoner;
	/** progress monitor */
	private ReasonerProgressMonitor pMonitor;
	/** taxonomy structure of a TBox */
	private DLConceptTaxonomy pTax;
	/** DataType center */
	private DataTypeCenter DTCenter = new DataTypeCenter();
	/** set of reasoning options */
	private final IFOptionSet pOptions;
	/** status of the KB */
	private KBStatus Status;
	/** global KB features */
	private LogicFeatures KBFeatures = new LogicFeatures();
	/** GCI features */
	private LogicFeatures GCIFeatures = new LogicFeatures();
	/** nominal cloud features */
	private LogicFeatures NCFeatures = new LogicFeatures();
	/** aux features */
	private LogicFeatures auxFeatures = new LogicFeatures();
	/** pointer to current feature (in case of local ones) */
	private LogicFeatures curFeature = new LogicFeatures();
	// auxiliary concepts for Taxonomy
	/** concept representing Top */
	private TConcept pTop;
	/** concept representing Bottom */
	private TConcept pBottom;
	/**
	 * concept representing temporary one that can not be used anywhere in the
	 * ontology
	 */
	private TConcept pTemp;
	/** temporary concept that represents query */
	private TConcept defConcept;
	/** all named concepts */
	private final TNECollection<TConcept> Concepts;
	/** all named individuals/nominals */
	private final TNECollection<TIndividual> Individuals;
	/** "normal" (object) roles */
	private RoleMaster ORM;
	/** data roles */
	private RoleMaster DRM;
	/** set of GCIs */
	private TAxiomSet Axioms;
	/** given individual-individual relations */
	private List<TRelated> RelatedI = new ArrayList<TRelated>();
	/** known disjoint sets of individuals */
	private List<List<TIndividual>> Different = new ArrayList<List<TIndividual>>();
	/** all simple rules in KB */
	private List<TSimpleRule> SimpleRules = new ArrayList<TSimpleRule>();
	/** internalisation of a general axioms */
	private int T_G;
	/** KB flags about GCIs */
	private TKBFlags GCIs;
	/** cache for the \forall R.C replacements during absorption */
	private Map<DLTree, TConcept> RCCache = new HashMap<DLTree, TConcept>();
	/** current axiom's ID */
	//int axiomId;
	/** current aux concept's ID */
	private int auxConceptID;
	/**
	 * how many times nominals were found during translation to DAG; local to
	 * BuildDAG
	 */
	private int nNominalReferences;
	/** searchable stack for the told subsumers */
	private Set<TConcept> CInProcess = new HashSet<TConcept>();
	/** fairness constraints */
	private List<TConcept> Fairness = new ArrayList<TConcept>();
	/** single SAT/SUB test timeout in milliseconds */
	private long testTimeout;
	// Flags section
	/** flag for full/short KB */
	private boolean useRelevantOnly;
	/** flag for creating taxonomy */
	private boolean useCompletelyDefined;
	/** flag for dumping TBox relevant to query */
	private boolean dumpQuery;
	//	/** whether or not we need classification. Set up in checkQueryNames() */
	//	private boolean needClassification;
	/** shall we prefer C=D axioms to C[=E in definition of concepts */
	private boolean alwaysPreferEquals;
	/** shall verbose output be used */
	private boolean verboseOutput;
	/** whether we use sorted reasoning; depends on some simplifications */
	private boolean useSortedReasoning;
	/** flag whether TBox is GALEN-like */
	private boolean isLikeGALEN;
	/** flag whether TBox is WINE-like */
	private boolean isLikeWINE;
	/** flag whether precompletion should be used */
	private boolean usePrecompletion;
	/** whether KB is consistent */
	private boolean Consistent;
	/** whether KB(ABox) is precompleted */
	private boolean Precompleted;
	/** time spend for preprocessing */
	private long preprocTime;
	/** time spend for consistency checking */
	private long consistTime;
	/** number of concepts and individuals; used to set index for modelCache */
	int nC = 0;
	/** number of all distinct roles; used to set index for modelCache */
	int nR = 0;
	/** map to show the possible equivalence between individuals */
	Map<TConcept, Pair<TIndividual, Boolean>> SameI = new HashMap<TConcept, Pair<TIndividual, Boolean>>();
	/** all the synonyms in the told subsumers' cycle */
	Set<TConcept> ToldSynonyms = new HashSet<TConcept>();

	//--		internal iterators
	/** RW begin() for concepts */
	//	public List<TConcept> c_begin() {
	//		return Concepts.getList();
	//	}
	/** RW begin() for individuals */
	public List<TIndividual> i_begin() {
		return Individuals.getList();
	}

	/** get concept by it's BP (non-final version) */
	public TDataEntry getDataEntryByBP(int bp) {
		TDataEntry p = (TDataEntry) DLHeap.get(bp).getConcept();
		assert p != null;
		return p;
	}

	/** add description to a concept; @return true in case of error */
	public boolean initNonPrimitive(TConcept p, DLTree desc) {
		if (!p.canInitNonPrim(desc)) {
			return true;
		}
		// delete return value in case of duplicated desc
		//deleteTree(
		makeNonPrimitive(p, desc);
		//);
		return false;
	}

	/** make concept non-primitive; @return it's old description */
	public DLTree makeNonPrimitive(TConcept p, DLTree desc) {
		DLTree ret = p.makeNonPrimitive(desc);
		checkEarlySynonym(p);
		return ret;
	}

	/** checks if C is defined as C=D and set Synonyms accordingly */
	public void checkEarlySynonym(TConcept p) {
		if (p.isSynonym()) {
			return; // nothing to do
		}
		if (p.isPrimitive()) {
			return; // couldn't be a synonym
		}
		if (!p.getDescription().isCN()) {
			return; // complex expression -- not a synonym(imm.)
		}
		p.setSynonym(getCI(p.getDescription()));
		p.initToldSubsumers();
	}

	/** process a disjoint set [beg,end) in a usual manner */
	public void processDisjoint(List<DLTree> beg) {
		while (beg.size() > 0) {
			DLTree r = beg.remove(0);
			addSubsumeAxiom(r, DLTreeFactory.buildDisjAux(beg));
		}
	}

	/** create REFLEXIVE node */
	public int reflexive2dag(final TRole R) {
		// input check: only simple roles are allowed in the reflexivity construction
		if (!R.isSimple()) {
			throw new ReasonerInternalException("Non simple role used as simple: " + R.getName());
		}
		return -DLHeap.add(new DLVertex(dtIrr, 0, R, bpINVALID, null));
	}

	/** create forall node for data role */
	public int dataForall2dag(final TRole R, int C) {
		return DLHeap.add(new DLVertex(dtForall, 0, R, C, null));
	}

	/** create atmost node for data role */
	public int dataAtMost2dag(int n, final TRole R, int C) {
		return DLHeap.add(new DLVertex(dtLE, n, R, C, null));
	}

	/** @return a pointer to concept representation */
	public int concept2dag(TConcept p) {
		if (p == null) {
			return bpINVALID;
		}
		if (!isValid(p.getpName())) {
			addConceptToHeap(p);
		}
		return p.resolveId();
	}

	/** try to absorb GCI C[=D; if not possible, just record this GCI */
	public void processGCI(DLTree C, DLTree D) {
		Axioms.addAxiom(C, D);
	}

	/** absorb all axioms */
	public void AbsorbAxioms() {
		int nSynonyms = countSynonyms();
		Axioms.absorb();
		if (countSynonyms() > nSynonyms) {
			replaceAllSynonyms();
		}
		if (Axioms.wasRoleAbsorptionApplied()) {
			initToldSubsumers();
		}
	}

	/** set told TOP concept whether necessary */
	public void initToldSubsumers() {
		for (TConcept pc : Concepts.getList()) {
			if (!pc.isSynonym()) {
				pc.initToldSubsumers();
			}
		}
		for (TIndividual pi : Individuals.getList()) {
			if (!pi.isSynonym()) {
				pi.initToldSubsumers();
			}
		}
	}

	/** set told TOP concept whether necessary */
	public void setToldTop() {
		TConcept top = pTop;
		for (TConcept pc : Concepts.getList()) {
			pc.setToldTop(top);
		}
		for (TIndividual pi : Individuals.getList()) {
			pi.setToldTop(top);
		}
	}

	/** calculate TS depth for all concepts */
	public void calculateTSDepth() {
		for (TConcept pc : Concepts.getList()) {
			pc.calculateTSDepth();
		}
		for (TIndividual pi : Individuals.getList()) {
			pi.calculateTSDepth();
		}
	}

	/** @return number of synonyms in the KB */
	public int countSynonyms() {
		int nSynonyms = 0;
		for (TConcept pc : Concepts.getList()) {
			if (pc.isSynonym()) {
				++nSynonyms;
			}
		}
		for (TIndividual pi : Individuals.getList()) {
			if (pi.isSynonym()) {
				++nSynonyms;
			}
		}
		return nSynonyms;
	}

	/** init Extra Rule field in concepts given by a vector V with a given INDEX */
	public void initRuleFields(final List<TConcept> v, int index) {
		for (TConcept q : v) {
			q.addExtraRule(index);
		}
	}

	/** mark all concepts wrt their classification tag */
	public void fillsClassificationTag() {
		for (TConcept pc : Concepts.getList()) {
			pc.getClassTag();
		}
		for (TIndividual pi : Individuals.getList()) {
			pi.getClassTag();
		}
	}

	/** get RW reasoner wrt nominal case */
	public DlSatTester getReasoner() {
		assert curFeature != null;
		if (curFeature.hasSingletons()) {
			return nomReasoner;
		} else {
			return stdReasoner;
		}
	}

	/** print all registered concepts */
	public void PrintConcepts(LogAdapter o) {
		if (Concepts.size() == 0) {
			return;
		}
		o.print(String.format("Concepts (%s):\n", Concepts.size()));
		for (TConcept pc : Concepts.getList()) {
			PrintConcept(o, pc);
		}
	}

	/** print all registered individuals */
	public void PrintIndividuals(LogAdapter o) {
		if (Individuals.size() == 0) {
			return;
		}
		o.print(String.format("Individuals (%s):\n", Individuals.size()));
		for (TIndividual pi : Individuals.getList()) {
			PrintConcept(o, pi);
		}
	}

	public void PrintSimpleRules(LogAdapter o) {
		if (SimpleRules.isEmpty()) {
			return;
		}
		o.print(String.format("Simple rules (%s):\n", SimpleRules.size()));
		for (TSimpleRule p : SimpleRules) {
			o.print("(");
			for (int i = 0; i < p.getBody().size(); i++) {
				if (i > 0) {
					o.print(", ");
				}
				o.print(p.getBody().get(i).getName());
			}
			o.print(String.format(") => %s\n", p.tHead));
		}
	}

	public void PrintAxioms(LeveLogger.LogAdapter o) {
		if (T_G == bpTOP) {
			return;
		}
		o.print("Axioms:\nT [=");
		PrintDagEntry(o, T_G);
	}

	/** check if the role R is irreflexive */
	protected boolean isIrreflexive(TRole R) {
		assert R != null;
		// data roles are irreflexive
		if (R.isDataRole()) {
			return true;
		}
		// prepare feature that are KB features
		// FIXME!! overkill, but fine for now as it is sound
		curFeature = KBFeatures;
		getReasoner().setBlockingMethod(isIRinQuery(), isNRinQuery());
		boolean result = getReasoner().checkIrreflexivity(R);
		clearFeatures();
		return result;
	}

	/** gather information about logical features of relevant concept */
	public void collectLogicFeature(final TConcept p) {
		if (curFeature != null) {
			curFeature.fillConceptData(p);
		}
	}

	/** gather information about logical features of relevant role */
	public void collectLogicFeature(final TRole p) {
		if (curFeature != null) {
			curFeature.fillRoleData(p, p.inverse().isRelevant(relevance));
		}
	}

	/** gather information about logical features of relevant DAG entry */
	public void collectLogicFeature(final DLVertex v, boolean pos) {
		if (curFeature != null) {
			curFeature.fillDAGData(v, pos);
		}
	}

	/** mark all active GCIs relevant */
	public void markGCIsRelevant() {
		setRelevant(T_G);
	}

	/** set all TBox content (namely, concepts and GCIs) relevant */
	public void markAllRelevant() {
		for (TConcept pc : Concepts.getList()) {
			setRelevant(pc);
		}
		for (TIndividual pi : Individuals.getList()) {
			setRelevant(pi);
		}
		markGCIsRelevant();
	}

	/** clear all relevance info */
	public void clearRelevanceInfo() {
		relevance.newLabel();
	}

	/** get fresh concept */
	public DLTree getFreshConcept() {
		return DLTreeFactory.buildTree(new TLexeme(CNAME, pTemp));
	}

	/** put relevance information to a concept's data */
	private void setConceptRelevant(TConcept p) {
		curFeature = p.getPosFeatures();
		setRelevant(p.getpBody());
		KBFeatures.binaryOrOperator(p.getPosFeatures());
		collectLogicFeature(p);
		clearRelevanceInfo();
		// nothing to do for neg-prim concepts
		if (p.isPrimitive()) {
			return;
		}
		curFeature = p.getNegFeatures();
		setRelevant(-p.getpBody());
		KBFeatures.binaryOrOperator(p.getNegFeatures());
		clearRelevanceInfo();
	}

	/** update AUX features with the given one; update roles if necessary */
	private void updateAuxFeatures(final LogicFeatures lf) {
		if (!lf.isEmpty()) {
			auxFeatures.binaryOrOperator(lf);
			auxFeatures.mergeRoles();
		}
	}

	/** clear current features */
	public void clearFeatures() {
		curFeature = null;
	}

	/** get RW access to used Role Master */
	public RoleMaster getORM() {
		return ORM;
	}

	/** get RW access to used DataRole Master */
	public RoleMaster getDRM() {
		return DRM;
	}

	/** get RW access to the RoleMaster depending of the R */
	public RoleMaster getRM(final TRole R) {
		return R.isDataRole() ? DRM : ORM;
	}

	/** get RW access to a DT center */
	public DataTypeCenter getDataTypeCenter() {
		return DTCenter;
	}

	/** return registered concept by given NAME; @return null if can't register */
	protected TConcept getConcept(String name) {
		return Concepts.get(name);
	}

	/**
	 * return registered individual by given NAME; @return null if can't
	 * register
	 */
	protected TIndividual getIndividual(String name) {
		return Individuals.get(name);
	}

	/** @return true iff given NAME is a name of a registered individual */
	private boolean isIndividual(final String name) {
		return Individuals.isRegistered(name);
	}

	/** @return true iff given ENTRY is a registered individual */
	public boolean isIndividual(final TNamedEntry entry) {
		return isIndividual(entry.getName());
	}

	/** @return true iff given TREE represents a registered individual */
	public boolean isIndividual(final DLTree tree) {
		return tree.token() == INAME && isIndividual(tree.elem().getNE());
	}

	/** get TOP/BOTTOM/CN/IN by the DLTree entry */
	public TConcept getCI(final DLTree name) {
		if (name.isTOP()) {
			return pTop;
		}
		if (name.isBOTTOM()) {
			return pBottom;
		}
		if (!name.isName()) {
			return null;
		}
		if (name.token() == CNAME) {
			return (TConcept) name.elem().getNE();
		} else {
			return (TIndividual) name.elem().getNE();
		}
	}

	/** get a DL tree by a given concept-like C */
	public DLTree getTree(TConcept C) {
		if (C == null) {
			return null;
		}
		if (C.equals(pTop)) {
			return DLTreeFactory.createTop();
		}
		if (C.equals(pBottom)) {
			return DLTreeFactory.createBottom();
		}
		return DLTreeFactory.buildTree(new TLexeme(isIndividual(C) ? INAME : CNAME, C));
	}

	/**
	 * set the flag that forbid usage of undefined names for concepts/roles; @return
	 * old value
	 */
	public boolean setForbidUndefinedNames(boolean val) {
		ORM.setUndefinedNames(!val);
		DRM.setUndefinedNames(!val);
		Individuals.setLocked(val);
		return Concepts.setLocked(val);
	}

	/** individual relation <a,b>:R */
	public void RegisterIndividualRelation(TNamedEntry a, TNamedEntry R, TNamedEntry b) {
		if (!isIndividual(a) || !isIndividual(b)) {
			throw new ReasonerInternalException("Individual expected in related()");
		}
		RelatedI.add(new TRelated((TIndividual) a, (TIndividual) b, (TRole) R));
		RelatedI.add(new TRelated((TIndividual) b, (TIndividual) a, ((TRole) R).inverse()));
	}

	/** add axiom CN [= D for concept CN */
	public void addSubsumeAxiom(TConcept C, DLTree D) {
		addSubsumeAxiom(getTree(C), D);
	}

	/** add simple rule RULE to the TBox' rules */
	public void addSimpleRule(TSimpleRule Rule) {
		initRuleFields(Rule.getBody(), SimpleRules.size());
		SimpleRules.add(Rule);
	}

	/** @return true if KB contains fairness constraints */
	public boolean hasFC() {
		return !Fairness.isEmpty();
	}

	public void setFairnessConstraintDLTrees(List<DLTree> l) {
		for (int i = 0; i < l.size(); i++) {
			// build a flag for a FC
			TConcept fc = getAuxConcept(null);
			Fairness.add(fc);
			// make an axiom: C [= FC
			addSubsumeAxiom(l.get(i), getTree(fc));
		}
	}

	/** GCI Axioms access */
	public int getTG() {
		return T_G;
	}

	/** get simple rule by its INDEX */
	public final TSimpleRule getSimpleRule(int index) {
		return SimpleRules.get(index);
	}

	/** check if the relevant part of KB contains inverse roles. */
	public boolean isIRinQuery() {
		if (curFeature != null) {
			return curFeature.hasInverseRole();
		} else {
			return KBFeatures.hasInverseRole();
		}
	}

	/** check if the relevant part of KB contains number restrictions. */
	public boolean isNRinQuery() {
		final LogicFeatures p = curFeature != null ? curFeature : KBFeatures;
		return p.hasFunctionalRestriction() || p.hasNumberRestriction() || p.hasQNumberRestriction();
	}

	/** check if the relevant part of KB contains singletons */
	public boolean testHasNominals() {
		if (curFeature != null) {
			return curFeature.hasSingletons();
		} else {
			return KBFeatures.hasSingletons();
		}
	}

	/** check if Sorted Reasoning is applicable */
	public boolean canUseSortedReasoning() {
		return useSortedReasoning && !GCIs.isGCI() && !GCIs.isReflexive();
	}

	/** perform classification (assuming KB is consistent) */
	public void performClassification() {
		createTaxonomy(false);
	}

	/** perform realisation (assuming KB is consistent) */
	public void performRealisation() {
		createTaxonomy(true);
	}

	/** get (READ-WRITE) access to internal Taxonomy of concepts */
	public DLConceptTaxonomy getTaxonomy() {
		return pTax;
	}

	/** set given structure as a progress monitor */
	public void setProgressMonitor(ReasonerProgressMonitor pMon) {
		pMonitor = pMon;
	}

	/**
	 * set verbose output (ie, default progress monitor, concept and role
	 * taxonomies) wrt given VALUE
	 */
	public void setVerboseOutput(boolean value) {
		verboseOutput = value;
	}

	/** inform that KB is precompleted */
	public void setPrecompleted() {
		Precompleted = true;
	}

	/** if KB is precompleted */
	public boolean isPrecompleted() {
		return Precompleted;
	}

	/** get status flag */
	public KBStatus getStatus() {
		return Status;
	}

	/** set consistency flag */
	public void setConsistency(boolean val) {
		Status = kbCChecked;
		Consistent = val;
	}

	/** check if the ontology is consistent */
	public boolean isConsistent() {
		if (Status.ordinal() < kbCChecked.ordinal()) {
			prepareReasoning();
			if (Status.ordinal() < kbCChecked.ordinal() && Consistent) {
				setConsistency(performConsistencyCheck());
			}
		}
		return Consistent;
	}

	/** test if 2 concept non-subsumption can be determined by sorts checking */
	public boolean testSortedNonSubsumption(final TConcept p, final TConcept q) {
		// sorted reasoning doesn't work in presence of GCIs
		if (!canUseSortedReasoning()) {
			return false;
		}
		// doesn't work for the SAT tests
		if (q == null) {
			return false;
		}
		return !DLHeap.haveSameSort(p.getpName(), q.getpName());
	}

	/** print TBox as a whole */
	public void Print(LogAdapter o) {
		DLHeap.PrintStat(o);
		ORM.Print(o, "Object");
		DRM.Print(o, "Data");
		PrintConcepts(o);
		PrintIndividuals(o);
		PrintSimpleRules(o);
		PrintAxioms(o);
		DLHeap.Print(o);
	}

	public void buildDAG() {
		nNominalReferences = 0;
		for (TConcept pc : Concepts.getList()) {
			concept2dag(pc);
		}
		for (TIndividual pi : Individuals.getList()) {
			concept2dag(pi);
		}
		for (TSimpleRule q : SimpleRules) {
			q.setBpHead(tree2dag(q.tHead));
		}
		// builds Roles range and domain
		initRangeDomain(ORM);
		initRangeDomain(DRM);
		DLTree GCI = Axioms.getGCI();
		// add special domains to the GCIs
		List<DLTree> list = new ArrayList<DLTree>();
		for (TRole p : ORM.getRoles()) {
			if (!p.isSynonym() && p.hasSpecialDomain()) {
				list.add(p.getTSpecialDomain().copy());
			}
		}
		if (list.size() > 0) {
			list.add(GCI);
			GCI = DLTreeFactory.createSNFAnd(list);
		}
		T_G = tree2dag(GCI);
		//deleteTree(GCI);
		GCI = null;
		// mark GCI flags
		GCIs.setGCI(T_G != bpTOP);
		GCIs.setReflexive(ORM.hasReflexiveRoles());
		for (TRole p : ORM.getRoles()) {
			if (!p.isSynonym() && p.isTopFunc()) {
				p.setFunctional(atmost2dag(1, p, bpTOP));
			}
		}
		for (TRole p : DRM.getRoles()) {
			if (!p.isSynonym() && p.isTopFunc()) {
				p.setFunctional(atmost2dag(1, p, bpTOP));
			}
		}
		concept2dag(pTemp);
		if (nNominalReferences > 0) {
			int nInd = Individuals.getList().size();
			if (nInd > 100 && nNominalReferences > nInd) {
				isLikeWINE = true;
			}
		}
	}

	public void initRangeDomain(RoleMaster RM) {
		for (TRole p : RM.getRoles()) {
			if (!p.isSynonym()) {
				TRole R = p;
				if (IfDefs.RKG_UPDATE_RND_FROM_SUPERROLES) {
					// add R&D from super-roles (do it AFTER axioms are transformed into R&D)
					R.collectDomainFromSupers();
				}
				DLTree dom = R.getTDomain();
				int bp = bpTOP;
				if (dom != null) {
					bp = tree2dag(dom);
					GCIs.setRnD();
				}
				R.setBPDomain(bp);
				// special domain for R is AR.Range
				R.initSpecialDomain();
				if (R.hasSpecialDomain()) {
					R.setSpecialDomain(tree2dag(R.getTSpecialDomain()));
				}
			}
		}
	}

	public int addDataExprToHeap(TDataEntry p) {
		if (isValid(p.getBP())) {
			return p.getBP();
		}
		DagTag dt = p.isBasicDataType() ? dtDataType : p.isDataValue() ? dtDataValue : dtDataExpr;
		int hostBP = bpTOP;
		//TODO this is broken: the next two lines are commented but should not be
		//		if (p.getType() != null) {
		//			hostBP = addDataExprToHeap(p.getType());
		//		}
		// sets it off 0 and 1 - although it's not necessarily correct
		hostBP = p.getDatatype().ordinal() + 2;
		DLVertex ver = new DLVertex(dt, 0, null, hostBP, null);
		ver.setConcept(p);
		p.setBP(DLHeap.directAdd(ver));
		return p.getBP();
	}

	public int addDataExprToHeap(TDLDataTypeName p) {
		DagTag dt = dtDataType;
		//XXX possibly a bug here
		int hostBP = p.getDatatype().ordinal() + 2;
		DLVertex ver = new DLVertex(dt, 0, null, hostBP, null);
		ver.setConcept(p);
		return DLHeap.directAdd(ver);
	}

	public void addConceptToHeap(TConcept pConcept) {
		DagTag tag = pConcept.isPrimitive() ? (pConcept.isSingleton() ? dtPSingleton : dtPConcept) : pConcept.isSingleton() ? dtNSingleton : dtNConcept;
		// NSingleton is a nominal
		if (tag == dtNSingleton && !pConcept.isSynonym()) {
			((TIndividual) pConcept).setNominal(true);
		}
		DLVertex ver = new DLVertex(tag);
		ver.setConcept(pConcept);
		pConcept.setpName(DLHeap.directAdd(ver));
		int desc = bpTOP;
		if (pConcept.getDescription() != null) {
			desc = tree2dag(pConcept.getDescription());
		} else {
			assert pConcept.isPrimitive();
		}
		pConcept.setpBody(desc);
		ver.setChild(desc);
	}

	public int tree2dag(DLTree t) {
		if (t == null) {
			return bpINVALID;
		}
		final TLexeme cur = t.elem();
		int ret = bpINVALID;
		switch (cur.getToken()) {
			case BOTTOM:
				ret = bpBOTTOM;
				break;
			case TOP:
				ret = bpTOP;
				break;
			case DATAEXPR:
				if (cur.getNE() instanceof TDataEntry) {
					ret = addDataExprToHeap((TDataEntry) cur.getNE());
				} else {
					ret = addDataExprToHeap((TDLDataTypeName) cur.getNE());
				}
				break;
			case CNAME:
				ret = concept2dag((TConcept) cur.getNE());
				break;
			case INAME:
				++nNominalReferences;// definitely a nominal
				TIndividual ind = (TIndividual) cur.getNE();
				ind.setNominal(true);
				ret = concept2dag(ind);
				break;
			case NOT:
				ret = -tree2dag(t.Child());
				break;
			case AND:
				ret = and2dag(new DLVertex(dtAnd), t);
				break;
			case FORALL:
				ret = forall2dag(TRole.resolveRole(t.Left()), tree2dag(t.Right()));
				break;
			case REFLEXIVE:
				ret = reflexive2dag(TRole.resolveRole(t.Child()));
				break;
			case LE:
				ret = atmost2dag(cur.getData(), TRole.resolveRole(t.Left()), tree2dag(t.Right()));
				break;
			case PROJFROM:
				ret = DLHeap.directAdd(new DLVertex(DagTag.dtProj, 0, TRole.resolveRole(t.Left()), tree2dag(t.Right().Right()), TRole.resolveRole(t.Right().Left())));
				break;
			default:
				assert DLTreeFactory.isSNF(t);
				throw new UnreachableSituationException();
		}
		return ret;
	}

	/** fills AND-like vertex V with an AND-like expression T; process result */
	public int and2dag(DLVertex v, DLTree t) {
		int ret = bpBOTTOM;
		if (!fillANDVertex(v, t)) {
			int value = v.getAndToDagValue();
			if (value != bpINVALID) {
				return value;
			}
			return DLHeap.add(v);
		}
		return ret;
	}

	public int forall2dag(final TRole R, int C) {
		if (R.isDataRole()) {
			return dataForall2dag(R, C);
		}
		int ret = DLHeap.add(new DLVertex(dtForall, 0, R, C, null));
		if (R.isSimple()) {
			return ret;
		}
		if (!DLHeap.isLast(ret)) {
			return ret;
		}
		for (int i = 1; i < R.getAutomaton().size(); ++i) {
			DLHeap.directAddAndCache(new DLVertex(dtForall, i, R, C, null));
		}
		return ret;
	}

	public int atmost2dag(int n, final TRole R, int C) {
		if (!R.isSimple()) {
			throw new ReasonerInternalException("Non simple role used as simple: " + R.getName());
		}
		if (R.isDataRole()) {
			return dataAtMost2dag(n, R, C);
		}
		int ret = DLHeap.add(new DLVertex(dtLE, n, R, C, null));
		if (!DLHeap.isLast(ret)) {
			return ret;
		}
		for (int m = n - 1; m > 0; --m) {
			DLHeap.directAddAndCache(new DLVertex(dtLE, m, R, C, null));
		}
		DLHeap.directAddAndCache(new DLVertex(dtNN));
		return ret;
	}

	private final boolean fillANDVertex(DLVertex v, final DLTree t) {
		if (t.isAND()) {
			boolean ret = false;
			for (DLTree d : t.Children()) {
				ret |= fillANDVertex(v, d);
			}
			return ret;
		} else {
			return v.addChild(tree2dag(t));
		}
	}

	public void initTaxonomy() {
		pTax = new DLConceptTaxonomy(pTop, pBottom, this, GCIs);
	}

	private List<TConcept> arrayCD = new ArrayList<TConcept>(), arrayNoCD = new ArrayList<TConcept>(), arrayNP = new ArrayList<TConcept>();

	public <T extends TConcept> int fillArrays(List<T> begin) {
		int n = 0;
		for (T p : begin) {
			if (p.isNonClassifiable()) {
				continue;
			}
			++n;
			switch (p.getClassTag()) {
				case cttTrueCompletelyDefined:
					arrayCD.add(p);
					break;
				case cttNonPrimitive:
				case cttHasNonPrimitiveTS:
					arrayNP.add(p);
					break;
				default:
					arrayNoCD.add(p);
					break;
			}
		}
		return n;
	}

	private int nItems = 0;
	private AtomicBoolean interrupted;

	public int getNItems() {
		return nItems;
	}

	public void createTaxonomy(boolean needIndividual) {
		boolean needConcept = !needIndividual;
		if (pTax == null) {
			DLHeap.setSubOrder();
			initTaxonomy();
			needConcept |= needIndividual;
		} else {
			return;
		}
		if (verboseOutput) {
			LL.println("Processing query...");
		}
		TsProcTimer locTimer = new TsProcTimer();
		locTimer.Start();
		nItems = 0;
		arrayCD.clear();
		arrayNoCD.clear();
		arrayNP.clear();
		nItems += fillArrays(Concepts.getList());
		nItems += fillArrays(Individuals.getList());
		if (pMonitor != null) {
			pMonitor.reasonerTaskStarted(ReasonerProgressMonitor.CLASSIFYING);
			pTax.setProgressIndicator(pMonitor);
		}
		classifyConcepts(arrayCD, true, "completely defined");
		classifyConcepts(arrayNoCD, false, "regular");
		classifyConcepts(arrayNP, false, "non-primitive");
		if (pMonitor != null) {
			pMonitor.reasonerTaskStopped();
			pMonitor = null;
		}
		pTax.finalise();
		locTimer.Stop();
		//pTax.print(LeveLogger.LL);
		if (verboseOutput) {
			LL.println(" done in " + locTimer.calcDelta() + " seconds\n");
		}
		if (needConcept && Status.ordinal() < kbClassified.ordinal()) {
			Status = kbClassified;
		}
		if (needIndividual) {
			Status = kbRealised;
		}
		if (verboseOutput) {
			//TODO
			//			PrintStream of;
			//			try {
			//of = new PrintStream("Taxonomy.log");
			//pTax.print(System.out);
			//of.close();
			//} catch (FileNotFoundException e) {
			//throw new RuntimeException(e);
			//}
		}
	}

	public void classifyConcepts(final List<TConcept> collection, boolean curCompletelyDefined, final String type) {
		pTax.setCompletelyDefined(curCompletelyDefined);
		LL.print(Templates.CLASSIFY_CONCEPTS, type);
		int n = 0;
		for (TConcept q : collection) {
			if (!interrupted.get() && !q.isClassified()) {
				// need to classify concept
				classifyEntry(q);
				if (q.isClassified()) {
					++n;
				}
			}
		}
		LL.print(Templates.CLASSIFY_CONCEPTS2, n, type);
	}

	/** classify single concept */
	void classifyEntry(TConcept entry) {
		if (isBlockedInd(entry)) {
			classifyEntry(getBlockingInd(entry)); // make sure that the possible synonym is already classified
		}
		if (!entry.isClassified()) {
			pTax.classifyEntry(entry);
		}
	}

	protected TBox(final IFOptionSet Options, final String TopORoleName, final String BotORoleName, final String TopDRoleName, final String BotDRoleName, AtomicBoolean interrupted) {
		this.interrupted = interrupted;
		Axioms = new TAxiomSet(this);
		GCIs = new TKBFlags();
		DLHeap = new DLDag(Options);
		stdReasoner = null;
		nomReasoner = null;
		pMonitor = null;
		pTax = null;
		pOptions = Options;
		Status = kbLoading;
		curFeature = null;
		defConcept = null;
		Concepts = new TNECollection<TConcept>("concept", new ConceptCreator());
		Individuals = new TNECollection<TIndividual>("individual", new IndividualCreator());
		ORM = new RoleMaster(false, TopORoleName, BotORoleName);
		DRM = new RoleMaster(true, TopDRoleName, BotDRoleName);
		Axioms = new TAxiomSet(this);
		T_G = bpTOP;
		auxConceptID = 0;
		useSortedReasoning = true;
		isLikeGALEN = false;
		isLikeWINE = false;
		Consistent = true;
		Precompleted = false;
		preprocTime = 0;
		consistTime = 0;
		readConfig(Options);
		initTopBottom();
		setForbidUndefinedNames(false);
	}

	public TConcept getAuxConcept(DLTree desc) {
		TConcept C = getConcept(" aux" + ++auxConceptID);
		C.setSystem();
		C.setNonClassifiable();
		C.setPrimitive();
		C.addDesc(desc);
		C.initToldSubsumers();
		return C;
	}

	private void initTopBottom() {
		pBottom = TConcept.BOTTOM;
		pTop = TConcept.TOP;
		pTemp = TConcept.getTEMP();
	}

	public void prepareReasoning() {
		Preprocess();
		initReasoner();
		setForbidUndefinedNames(true);
		if (dumpQuery) {
			//TODO
			markAllRelevant();
			PrintStream of;
			try {
				of = new PrintStream("tbox");
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
			//assert(of != null);//.good() );
			DumpLisp lDump = new DumpLisp(of);
			dump(lDump);
			of.close();
			clearRelevanceInfo();
		}
		DLHeap.setSatOrder();
		setToDoPriorities();
	}

	public void prepareFeatures(final TConcept pConcept, final TConcept qConcept) {
		auxFeatures = new LogicFeatures(GCIFeatures);
		if (pConcept != null) {
			updateAuxFeatures(pConcept.getPosFeatures());
		}
		if (qConcept != null) {
			updateAuxFeatures(qConcept.getNegFeatures());
		}
		if (auxFeatures.hasSingletons()) {
			updateAuxFeatures(NCFeatures);
		}
		curFeature = auxFeatures;
		getReasoner().setBlockingMethod(isIRinQuery(), isNRinQuery());
	}

	public void buildSimpleCache() {
		// set cache for BOTTOM entry
		initConstCache(bpBOTTOM);
		// set all the caches for the temp concept
		initSingletonCache(pTemp, /*pos=*/true);
		initSingletonCache(pTemp, /*pos=*/false);
		// inapplicable if KB contains CGIs in any form
		if (GCIs.isGCI() || GCIs.isReflexive()) {
			return;
		}
		// it is now safe to make a TOP cache
		initConstCache(bpTOP);
		for (TConcept pc : Concepts.getList()) {
			if (pc.isPrimitive()) {
				initSingletonCache(pc, false);
			}
		}
		for (TIndividual pi : Individuals.getList()) {
			if (pi.isPrimitive()) {
				initSingletonCache(pi, false);
			}
		}
	}

	public boolean performConsistencyCheck() {
		if (verboseOutput) {
			LL.println("Consistency checking...");
		}
		TsProcTimer pt = new TsProcTimer();
		pt.Start();
		buildSimpleCache();
		TConcept test = NCFeatures.hasSingletons() && Individuals.getList().size() > 0 ? Individuals.getList().get(0) : null;
		prepareFeatures(test, null);
		boolean ret = false;
		if (test != null) {
			if (DLHeap.getCache(bpTOP) == null) {
				initConstCache(bpTOP);
			}
			ret = nomReasoner.consistentNominalCloud();
		} else {
			ret = isSatisfiable(pTop);
			// setup cache for GCI
			if (GCIs.isGCI()) {
				DLHeap.setCache(-T_G, new ModelCacheConst(false));
			}
		}
		pt.Stop();
		consistTime = pt.calcDelta();
		if (verboseOutput) {
			LL.println(" done in " + consistTime + " seconds\n");
		}
		return ret;
	}

	public boolean isSatisfiable(final TConcept pConcept) {
		assert pConcept != null;
		ModelCacheInterface cache = DLHeap.getCache(pConcept.getpName());
		if (cache != null) {
			return cache.getState() != ModelCacheState.csInvalid;
		}
		LL.println(String.format("\n-----------\nChecking satisfiability of '%s':", pConcept.getName()));
		prepareFeatures(pConcept, null);
		boolean result = getReasoner().runSat(pConcept.resolveId(), bpTOP);
		cache = getReasoner().buildCacheByCGraph(result);
		DLHeap.setCache(pConcept.getpName(), cache);
		clearFeatures();
		LL.print(String.format("\nThe '%s' concept is %ssatisfiable w.r.t. TBox", pConcept.getName(), (!result ? "un" : "")));
		return result;
	}

	public boolean isSubHolds(TConcept pConcept, TConcept qConcept) {
		assert pConcept != null && qConcept != null;
		LL.print(Templates.ISSUBHOLDS1, pConcept.getName(), qConcept.getName());
		prepareFeatures(pConcept, qConcept);
		boolean result = !getReasoner().runSat(pConcept.resolveId(), -qConcept.resolveId());
		clearFeatures();
		LL.print(Templates.ISSUBHOLDS2, pConcept.getName(), qConcept.getName(), (!result ? " NOT" : ""));
		return result;
	}

	public boolean isSameIndividuals(final TIndividual a, final TIndividual b) {
		if (a.equals(b)) {
			return true;
		}
		if (!isIndividual(a) || !isIndividual(b)) {
			throw new ReasonerInternalException("Individuals are expected in the isSameIndividuals() query");
		}
		if (a.getNode() == null || b.getNode() == null) {
			if (a.isSynonym()) {
				return isSameIndividuals((TIndividual) a.getSynonym(), b);
			}
			if (b.isSynonym()) {
				return isSameIndividuals(a, (TIndividual) b.getSynonym());
			}
			throw new ReasonerInternalException("isSameIndividuals() query with non-realised ontology");
		}
		return a.getNode().resolvePBlocker().equals(b.getNode().resolvePBlocker());
	}

	public boolean isDisjointRoles(final TRole R, final TRole S) {
		assert R != null && S != null;
		if (R.isDataRole() != S.isDataRole()) {
			return true;
		}
		curFeature = KBFeatures;
		getReasoner().setBlockingMethod(isIRinQuery(), isNRinQuery());
		boolean result = getReasoner().checkDisjointRoles(R, S);
		clearFeatures();
		return result;
	}

	public void readConfig(final IFOptionSet Options) {
		assert Options != null;
		useCompletelyDefined = Options.getBool("useCompletelyDefined");
		useRelevantOnly = Options.getBool("useRelevantOnly");
		dumpQuery = Options.getBool("dumpQuery");
		alwaysPreferEquals = Options.getBool("alwaysPreferEquals");
		usePrecompletion = Options.getBool("usePrecompletion");
		LL.println(Templates.READ_CONFIG, useCompletelyDefined, useRelevantOnly, dumpQuery, alwaysPreferEquals, usePrecompletion);
		if (Axioms.initAbsorptionFlags(Options.getText("absorptionFlags"))) {
			throw new ReasonerInternalException("Incorrect absorption flags given");
		}
		verboseOutput = true;
	}

	/**
	 * remove concept from TBox by given EXTERNAL id. XXX WARNING!! tested only
	 * for TempConcept!!!
	 */
	public void removeConcept(TConcept p) {
		assert p.equals(defConcept);
		// clear DAG and name indeces (if necessary)
		if (isCorrect(p.getpName())) {
			DLHeap.removeAfter(p.getpName());
		}
		if (Concepts.Remove(p)) {
			throw new UnreachableSituationException();
		}
	}

	public void clearQueryConcept() {
		removeConcept(defConcept);
	}

	private static final String defConceptName = "jfact.default";

	public TConcept createQueryConcept(final DLTree desc) {
		assert desc != null;
		if (defConcept != null) {
			clearQueryConcept();
		}
		boolean old = setForbidUndefinedNames(false);
		defConcept = getConcept(defConceptName);
		setForbidUndefinedNames(old);
		assert defConcept != null;
		makeNonPrimitive(defConcept, desc.copy());
		defConcept.setSystem();
		defConcept.setIndex(nC - 1);
		DLHeap.setExpressionCache(false);
		addConceptToHeap(defConcept);
		setConceptRelevant(defConcept);
		initCache(defConcept, false);
		return defConcept;
	}

	public void classifyQueryConcept() {
		defConcept.initToldSubsumers();
		assert pTax != null;
		pTax.setCompletelyDefined(false);
		//pTax.setInsertIntoTaxonomy(false);
		pTax.setProgressIndicator(null);
		pTax.classifyEntry(defConcept);
	}

	public void writeReasoningResult(LogAdapter o, long time) {
		if (nomReasoner != null) {
			o.print("Query processing reasoning statistic: Nominals");
			nomReasoner.writeTotalStatistic(o);
		}
		o.print("Query processing reasoning statistic: Standard");
		stdReasoner.writeTotalStatistic(o);
		assert Status.ordinal() >= kbCChecked.ordinal();
		if (Consistent) {
			o.print("Required");
		} else {
			o.print("KB is inconsistent. Query is NOT processed\nConsistency");
		}
		long sum = preprocTime + consistTime;
		o.print(String.format(" check done in %s seconds\nof which:\nPreproc. takes %s seconds\nConsist. takes %s seconds", time, preprocTime, consistTime));
		if (nomReasoner != null) {
			o.print("\nReasoning NOM:");
			sum += nomReasoner.printReasoningTime(o);
		}
		o.print("\nReasoning STD:");
		sum += stdReasoner.printReasoningTime(o);
		o.print("\nThe rest takes ");
		long f = time - sum;
		if (f < 0) {
			f = 0;
		}
		//		f = (long) (f * 100) / 100.f;
		o.print((float) f / 1000);
		o.print(" seconds\n");
		Print(o);
	}

	public void PrintDagEntry(LeveLogger.LogAdapter o, int p) {
		assert isValid(p);
		if (p == bpTOP) {
			o.print(" *TOP*");
			return;
		} else if (p == bpBOTTOM) {
			o.print(" *BOTTOM*");
			return;
		}
		if (p < 0) {
			o.print(" (not");
			PrintDagEntry(o, -p);
			o.print(")");
			return;
		}
		final DLVertex v = DLHeap.get(Math.abs(p));
		DagTag type = v.Type();
		switch (type) {
			case dtTop:
				o.print(" *TOP*");
				return;
			case dtPConcept:
			case dtNConcept:
			case dtPSingleton:
			case dtNSingleton:
			case dtDataType:
			case dtDataValue:
				o.print(" ");
				o.print(v.getConcept().getName());
				return;
			case dtDataExpr:
				o.print(" ");
				o.print(getDataEntryByBP(p).getFacet().toString());
				return;
			case dtIrr:
				o.print(String.format(" (%s %s)", type.getName(), v.getRole().getName()));
				return;
			case dtCollection:
			case dtAnd:
				o.print(" (");
				o.print(type.getName());
				for (int q : v.begin()) {
					PrintDagEntry(o, q);
				}
				o.print(")");
				return;
			case dtForall:
			case dtLE:
				o.print(" (");
				o.print(type.getName());
				if (type == dtLE) {
					o.print(" ");
					o.print(v.getNumberLE());
				}
				o.print(" ");
				o.print(v.getRole().getName());
				PrintDagEntry(o, v.getC());
				o.print(")");
				return;
			case dtProj:
				o.print(String.format(" (%s %s)", type.getName(), v.getRole().getName()));
				PrintDagEntry(o, v.getC());
				o.print(String.format(" => %s)", v.getProjRole().getName()));
				return;
			case dtNN:
				throw new UnreachableSituationException();
			default:
				throw new ReasonerInternalException("Error printing vertex of type " + type.getName() + "(" + type + ")");
		}
	}

	public void PrintConcept(LogAdapter o, final TConcept p) {
		if (isValid(p.getpName())) {
			o.print(p.getClassTagPlain().getCTTagName());
			//o.print(".");
			if (p.isSingleton()) {
				o.print((p.isNominal() ? 'o' : '!'));
			}
			o.print(String.format(".%s [%s] %s", p.getName(), p.getTsDepth(), (p.isNonPrimitive() ? "=" : "[=")));
			if (isValid(p.getpBody())) {
				PrintDagEntry(o, p.getpBody());
			}
			if (p.getDescription() != null) {
				o.print((p.isNonPrimitive() ? "\n-=" : "\n-[="));
				o.print(p.getDescription().toString());
			}
			o.print("\n");
		}
	}

	private void dump(DumpInterface dump) {
		dump.prologue();
		dumpAllRoles(dump);
		for (TConcept pc : Concepts.getList()) {
			if (pc.isRelevant(relevance)) {
				dumpConcept(dump, pc);
			}
		}
		for (TIndividual pi : Individuals.getList()) {
			if (pi.isRelevant(relevance)) {
				dumpConcept(dump, pi);
			}
		}
		if (T_G != bpTOP) {
			dump.startAx(DIOp.diImpliesC);
			dump.dumpTop();
			dump.contAx(DIOp.diImpliesC);
			dumpExpression(dump, T_G);
			dump.finishAx(DIOp.diImpliesC);
		}
		dump.epilogue();
	}

	public void dumpConcept(DumpInterface dump, final TConcept p) {
		dump.startAx(DIOp.diDefineC);
		dump.dumpConcept(p);
		dump.finishAx(DIOp.diDefineC);
		if (p.getpBody() != bpTOP) {
			DIOp Ax = p.isNonPrimitive() ? DIOp.diEqualsC : DIOp.diImpliesC;
			dump.startAx(Ax);
			dump.dumpConcept(p);
			dump.contAx(Ax);
			dumpExpression(dump, p.getpBody());
			dump.finishAx(Ax);
		}
	}

	public void dumpRole(DumpInterface dump, final TRole p) {
		if (p.getId() > 0 || !p.inverse().isRelevant(relevance)) {
			final TRole q = p.getId() > 0 ? p : p.inverse();
			dump.startAx(DIOp.diDefineR);
			dump.dumpRole(q);
			dump.finishAx(DIOp.diDefineR);
			for (ClassifiableEntry i : q.getToldSubsumers()) {
				dump.startAx(DIOp.diImpliesR);
				dump.dumpRole(q);
				dump.contAx(DIOp.diImpliesR);
				dump.dumpRole((TRole) i);
				dump.finishAx(DIOp.diImpliesR);
			}
		}
		if (p.isTransitive()) {
			dump.startAx(DIOp.diTransitiveR);
			dump.dumpRole(p);
			dump.finishAx(DIOp.diTransitiveR);
		}
		if (p.isTopFunc()) {
			dump.startAx(DIOp.diFunctionalR);
			dump.dumpRole(p);
			dump.finishAx(DIOp.diFunctionalR);
		}
		if (p.getBPDomain() != bpTOP) {
			dump.startAx(DIOp.diDomainR);
			dump.dumpRole(p);
			dump.contAx(DIOp.diDomainR);
			dumpExpression(dump, p.getBPDomain());
			dump.finishAx(DIOp.diDomainR);
		}
		if (p.getBPRange() != bpTOP) {
			dump.startAx(DIOp.diRangeR);
			dump.dumpRole(p);
			dump.contAx(DIOp.diRangeR);
			dumpExpression(dump, p.getBPRange());
			dump.finishAx(DIOp.diRangeR);
		}
	}

	public void dumpExpression(DumpInterface dump, int p) {
		assert isValid(p);
		if (p == bpTOP) {
			dump.dumpTop();
			return;
		}
		if (p == bpBOTTOM) {
			dump.dumpBottom();
			return;
		}
		if (p < 0) {
			dump.startOp(DIOp.diNot);
			dumpExpression(dump, -p);
			dump.finishOp(DIOp.diNot);
			return;
		}
		final DLVertex v = DLHeap.get(Math.abs(p));
		DagTag type = v.Type();
		switch (type) {
			case dtTop: {
				dump.dumpTop();
				return;
			}
			case dtPConcept:
			case dtNConcept:
			case dtPSingleton:
			case dtNSingleton: {
				dump.dumpConcept((TConcept) v.getConcept());
				return;
			}
			case dtAnd:
				dump.startOp(DIOp.diAnd);
				int[] begin = v.begin();
				for (int q : begin) {
					if (q != begin[0]) {
						dump.contOp(DIOp.diAnd);
					}
					dumpExpression(dump, q);
				}
				dump.finishOp(DIOp.diAnd);
				return;
			case dtForall:
				dump.startOp(DIOp.diForall);
				dump.dumpRole(v.getRole());
				dump.contOp(DIOp.diForall);
				dumpExpression(dump, v.getC());
				dump.finishOp(DIOp.diForall);
				return;
			case dtLE:
				dump.startOp(DIOp.diLE, v.getNumberLE());
				dump.dumpRole(v.getRole());
				dump.contOp(DIOp.diLE);
				dumpExpression(dump, v.getC());
				dump.finishOp(DIOp.diLE);
				return;
			default:
				throw new ReasonerInternalException("Error dumping vertex of type " + type.getName() + "(" + type + ")");
		}
	}

	public void dumpAllRoles(DumpInterface dump) {
		for (TRole p : ORM.getRoles()) {
			if (p.isRelevant(relevance)) {
				assert !p.isSynonym();
				dumpRole(dump, p);
			}
		}
		for (TRole p : DRM.getRoles()) {
			if (p.isRelevant(relevance)) {
				assert !p.isSynonym();
				dumpRole(dump, p);
			}
		}
	}

	public void addSubsumeAxiom(DLTree sub, DLTree sup) {
		if (equalTrees(sub, sup)) {
			return;
		}
		if (sup.isCN()) {
			sup = applyAxiomCToCN(sub, sup);
			if (sup == null) {
				return;
			}
		}
		if (sub.isCN()) {
			sub = applyAxiomCNToC(sub, sup);
			if (sub == null) {
				return;
			}
		}
		if (axiomToRangeDomain(sub, sup)) {
		} else {
			processGCI(sub, sup);
		}
	}

	public DLTree applyAxiomCToCN(DLTree D, DLTree CN) {
		TConcept C = resolveSynonym(getCI(CN));
		assert C != null;
		// lie: this will never be reached
		if (C.equals(pBottom)) {
			return DLTreeFactory.createBottom();
		}
		if (C.equals(pTop)) {
		} else if (!(C.isSingleton() && D.isName()) && equalTrees(C.getDescription(), D)) {
			makeNonPrimitive(C, D);
		} else {
			return CN;
		}
		return null;
	}

	public DLTree applyAxiomCNToC(DLTree CN, DLTree D) {
		TConcept C = resolveSynonym(getCI(CN));
		assert C != null;
		if (C.equals(pTop)) {
			return DLTreeFactory.createTop();
		}
		if (C.equals(pBottom)) {
		} else if (C.isPrimitive()) {
			C.addDesc(D);
		} else {
			addSubsumeForDefined(C, D);
		}
		return null;
	}

	public void addSubsumeForDefined(TConcept C, DLTree D) {
		if (DLTreeFactory.isSubTree(D, C.getDescription())) {
			return;
		}
		DLTree oldDesc = C.getDescription().copy();
		C.removeSelfFromDescription();
		if (equalTrees(oldDesc, C.getDescription())) {
			processGCI(oldDesc, D);
			return;
		}
		C.setPrimitive();
		C.addDesc(D);
		addSubsumeAxiom(oldDesc, getTree(C));
	}

	public boolean axiomToRangeDomain(DLTree sub, DLTree sup) {
		if (sub.isTOP() && sup.token() == FORALL) {
			TRole.resolveRole(sup.Left()).setRange(sup.Right().copy());
			return true;
		}
		if (sub.token() == NOT && sub.Child().token() == FORALL && sub.Child().Right().isBOTTOM()) {
			TRole.resolveRole(sub.Child().Left()).setDomain(sup);
			return true;
		}
		return false;
	}

	private void addEqualityAxiom(DLTree left, DLTree right) {
		if (addNonprimitiveDefinition(left, right)) {
			return;
		}
		if (addNonprimitiveDefinition(right, left)) {
			return;
		}
		if (switchToNonprimitive(left, right)) {
			return;
		}
		if (switchToNonprimitive(right, left)) {
			return;
		}
		addSubsumeAxiom(left.copy(), right.copy());
		addSubsumeAxiom(right, left);
	}

	public boolean addNonprimitiveDefinition(DLTree left, DLTree right) {
		TConcept C = resolveSynonym(getCI(left));
		if (C == null || C.equals(pTop) || C.equals(pBottom)) {
			return false;
		}
		TConcept D = getCI(right);
		if (D != null && resolveSynonym(D).equals(C)) {
			return true;
		}
		if (C.isSingleton() && D != null && !D.isSingleton()) {
			return false;
		}
		if (D == null || C.getDescription() == null || D.isPrimitive()) {
			if (!initNonPrimitive(C, right)) {
				return true;
			}
		}
		return false;
	}

	public boolean switchToNonprimitive(DLTree left, DLTree right) {
		TConcept C = resolveSynonym(getCI(left));
		if (C == null || C.equals(pTop) || C.equals(pBottom)) {
			return false;
		}
		TConcept D = resolveSynonym(getCI(right));
		if (C.isSingleton() && D != null && !D.isSingleton()) {
			return false;
		}
		if (alwaysPreferEquals && C.isPrimitive()) {
			addSubsumeForDefined(C, makeNonPrimitive(C, right));
			return true;
		}
		return false;
	}

	public void processDisjointC(Collection<DLTree> beg) {
		List<DLTree> prim = new ArrayList<DLTree>();
		List<DLTree> rest = new ArrayList<DLTree>();
		for (DLTree d : beg) {
			if (d.isName() && ((TConcept) d.elem().getNE()).isPrimitive()) {
				prim.add(d);
			} else {
				rest.add(d);
			}
		}
		if (!prim.isEmpty() && !rest.isEmpty()) {
			DLTree nrest = DLTreeFactory.buildDisjAux(rest);
			for (DLTree q : prim) {
				addSubsumeAxiom(q.copy(), nrest.copy());
			}
		}
		if (!rest.isEmpty()) {
			processDisjoint(rest);
		}
		if (!prim.isEmpty()) {
			processDisjoint(prim);
		}
	}

	public void processEquivalentC(List<DLTree> l) {
		//TODO check if this is taking into account all combinations
		for (int i = 0; i < l.size() - 1; i++) {
			addEqualityAxiom(l.get(i), l.get(i + 1).copy());
		}
	}

	public void processDifferent(List<DLTree> l) {
		List<TIndividual> acc = new ArrayList<TIndividual>();
		for (int i = 0; i < l.size(); i++) {
			if (isIndividual(l.get(i))) {
				acc.add((TIndividual) l.get(i).elem().getNE());
				l.set(i, null);
			} else {
				throw new ReasonerInternalException("Only individuals allowed in processDifferent()");
			}
		}
		if (acc.size() > 1) {
			Different.add(acc);
		}
	}

	public void processSame(List<DLTree> l) {
		if (l.size() == 0) {
			return;
		}
		if (!isIndividual(l.get(0))) {
			throw new ReasonerInternalException("Only individuals allowed in processSame()");
		}
		for (int i = 0; i < l.size() - 1; i++) {
			if (!isIndividual(l.get(i + 1))) {
				throw new ReasonerInternalException("Only individuals allowed in processSame()");
			}
			//TODO check if this is checking all combinations
			addEqualityAxiom(l.get(i), l.get(i + 1).copy());
		}
	}

	public void processDisjointR(List<DLTree> l) {
		if (l.isEmpty()) {
			throw new ReasonerInternalException("Empty disjoint role axiom");
		}
		for (DLTree p : l) {
			if (DLTreeFactory.isUniversalRole(p)) {
				throw new ReasonerInternalException("Universal role in the disjoint roles axiom");
			}
		}
		RoleMaster RM = getRM(TRole.resolveRole(l.get(0)));
		for (int i = 0; i < l.size(); i++) {
			DLTree p = l.get(i);
			TRole r = TRole.resolveRole(p);
			for (int j = i + 1; j < l.size(); j++) {
				RM.addDisjointRoles(r, TRole.resolveRole(l.get(j)));
			}
			l.set(i, null);
		}
	}

	public void processEquivalentR(List<DLTree> l) {
		if (l.size() > 0) {
			RoleMaster RM = getRM(TRole.resolveRole(l.get(0)));
			for (int i = 0; i < l.size() - 1; i++) {
				RM.addRoleSynonym(TRole.resolveRole(l.get(i)), TRole.resolveRole(l.get(i + 1)));
			}
			l.clear();
		}
	}

	public void Preprocess() {
		if (verboseOutput) {
			LL.println("\nPreprocessing...");
		}
		TsProcTimer pt = new TsProcTimer();
		pt.Start();
		ORM.initAncDesc();
		DRM.initAncDesc();
		if (verboseOutput) {
			try {
				//TODO
				PrintStream oroles = new PrintStream("Taxonomy.ORoles");
				LogAdapterStringBuilder b = new LogAdapterStringBuilder();
				ORM.getTaxonomy().print(b);
				oroles.print(b.toString());
				oroles.close();
				b = new LogAdapterStringBuilder();
				PrintStream droles = new PrintStream("Taxonomy.DRoles");
				DRM.getTaxonomy().print(b);
				droles.print(b.toString());
				droles.close();
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		if (countSynonyms() > 0) {
			replaceAllSynonyms();
		}
		preprocessRelated();
		initToldSubsumers();
		transformToldCycles();
		transformSingletonHierarchy();
		AbsorbAxioms();
		setToldTop();
		if (usePrecompletion) {
			performPrecompletion();
		}
		buildDAG();
		fillsClassificationTag();
		calculateTSDepth();
		// set indexes for model caching
		setAllIndexes();
		determineSorts();
		gatherRelevanceInfo();
		// here it is safe to print KB features (all are known; the last one was in Relevance)
		printFeatures();
		DLHeap.setOrderDefaults(isLikeGALEN ? "Fdn" : isLikeWINE ? "Sdp" : "Sap", isLikeGALEN ? "Ban" : isLikeWINE ? "Fdn" : "Dap");
		DLHeap.gatherStatistic();
		CalculateStatistic();
		RemoveExtraDescriptions();
		pt.Stop();
		preprocTime = pt.calcDelta();
		if (verboseOutput) {
			LL.println(" done in " + pt.calcDelta() + " seconds\n");
		}
	}

	void setAllIndexes() {
		nC = 1; // start with 1 to make index 0 an indicator of "not processed"
		pTemp.setIndex(nC++);
		for (TConcept pc : Concepts.getList()) {
			if (!pc.isSynonym()) {
				pc.setIndex(nC++);
			}
		}
		for (TIndividual pi : Individuals.getList()) {
			if (!pi.isSynonym()) {
				pi.setIndex(nC++);
			}
		}
		++nC; // place for the query concept
		nR = 1; // the same
		for (TRole r : ORM.getRoles()) {
			if (!r.isSynonym()) {
				r.setIndex(nR++);
			}
		}
		for (TRole r : DRM.getRoles()) {
			if (!r.isSynonym()) {
				r.setIndex(nR++);
			}
		}
	}

	private void replaceAllSynonyms() {
		for (TRole r : ORM.getRoles()) {
			if (!r.isSynonym()) {
				DLTreeFactory.replaceSynonymsFromTree(r.getTDomain());
			}
		}
		for (TRole dr : DRM.getRoles()) {
			if (!dr.isSynonym()) {
				DLTreeFactory.replaceSynonymsFromTree(dr.getTDomain());
			}
		}
		for (TConcept pc : Concepts.getList()) {
			if (DLTreeFactory.replaceSynonymsFromTree(pc.getDescription())) {
				pc.initToldSubsumers();
			}
		}
		for (TIndividual pi : Individuals.getList()) {
			if (DLTreeFactory.replaceSynonymsFromTree(pi.getDescription())) {
				pi.initToldSubsumers();
			}
		}
	}

	public void preprocessRelated() {
		for (TRelated q : RelatedI) {
			q.simplify();
		}
	}

	public void transformToldCycles() {
		int nSynonyms = countSynonyms();
		clearRelevanceInfo();
		for (TConcept pc : Concepts.getList()) {
			if (!pc.isSynonym()) {
				checkToldCycle(pc);
			}
		}
		for (TIndividual pi : Individuals.getList()) {
			if (!pi.isSynonym()) {
				checkToldCycle(pi);
			}
		}
		clearRelevanceInfo();
		nSynonyms = countSynonyms() - nSynonyms;
		if (nSynonyms > 0) {
			LL.print(Templates.TRANSFORM_TOLD_CYCLES, nSynonyms);
			replaceAllSynonyms();
		}
	}

	public TConcept checkToldCycle(TConcept _p) {
		assert _p != null;
		TConcept p = resolveSynonym(_p);
		if (p.equals(pTop)) {
			return null;
		}
		if (CInProcess.contains(p)) {
			return p;
		}
		if (p.isRelevant(relevance)) {
			return null;
		}
		TConcept ret = null;
		CInProcess.add(p);
		boolean redo = false;
		while (!redo) {
			redo = true;
			for (ClassifiableEntry r : p.getToldSubsumers()) {
				if ((ret = checkToldCycle((TConcept) r)) != null) {
					if (ret.equals(p)) {
						ToldSynonyms.add(p);
						for (TConcept q : ToldSynonyms) {
							if (q.isSingleton()) {
								p = q;
							}
						}
						Set<DLTree> leaves = new HashSet<DLTree>();
						for (TConcept q : ToldSynonyms) {
							if (q != p) {
								DLTree d = makeNonPrimitive(q, getTree(p));
								if (d.isBOTTOM()) {
									leaves.clear();
									leaves.add(d);
									break;
								} else {
									leaves.add(d);
								}
							}
						}
						ToldSynonyms.clear();
						p.setPrimitive();
						p.addLeaves(leaves);
						p.removeSelfFromDescription();
						if (!ret.equals(p)) {
							CInProcess.remove(ret);
							CInProcess.add(p);
							ret.setRelevant(relevance);
							p.dropRelevant(relevance);
						}
						ret = null;
						redo = false;
						break;
					} else {
						ToldSynonyms.add(p);
						redo = true;
						break;
					}
				}
			}
		}
		CInProcess.remove(p);
		p.setRelevant(relevance);
		return ret;
	}

	public void transformSingletonHierarchy() {
		int nSynonyms = countSynonyms();
		boolean changed;
		do {
			changed = false;
			for (TIndividual pi : Individuals.getList()) {
				if (!pi.isSynonym() && pi.isHasSP()) {
					TIndividual i = transformSingletonWithSP(pi);
					i.removeSelfFromDescription();
					changed = true;
				}
			}
		} while (changed);
		nSynonyms = countSynonyms() - nSynonyms;
		if (nSynonyms > 0) {
			replaceAllSynonyms();
		}
	}

	public TIndividual getSPForConcept(TConcept p) {
		for (ClassifiableEntry r : p.getToldSubsumers()) {
			TConcept i = (TConcept) r;
			if (i.isSingleton()) {
				return (TIndividual) i;
			}
			if (i.isHasSP()) {
				return transformSingletonWithSP(i);
			}
		}
		throw new UnreachableSituationException();
	}

	private TIndividual transformSingletonWithSP(TConcept p) {
		TIndividual i = getSPForConcept(p);
		if (p.isSingleton()) {
			i.addRelated((TIndividual) p);
		}
		addSubsumeAxiom(i, makeNonPrimitive(p, getTree(i)));
		return i;
	}

	public void performPrecompletion() {
		Precompletor PC = new Precompletor(this);
		PC.performPrecompletion();
	}

	public void determineSorts() {
		if (IfDefs.RKG_USE_SORTED_REASONING) {
			// Related individuals does not appears in DLHeap,
			// so their sorts shall be determined explicitely
			for (TRelated p : RelatedI) {
				DLHeap.updateSorts(p.getA().getpName(), p.getRole(), p.getB().getpName());
			}
			// simple rules needs the same treatement
			for (TSimpleRule q : SimpleRules) {
				MergableLabel lab = DLHeap.get(q.bpHead).getSort();
				for (TConcept r : q.Body) {
					DLHeap.merge(lab, r.getpName());
				}
			}
			// create sorts for concept and/or roles
			DLHeap.determineSorts(ORM, DRM);
		}
	}

	public void CalculateStatistic() {
		int npFull = 0, nsFull = 0;
		int nPC = 0, nNC = 0, nSing = 0;
		int nNoTold = 0;
		for (TConcept pc : Concepts.getList()) {
			final TConcept n = pc;
			if (!isValid(n.getpName())) {
				continue;
			}
			if (n.isPrimitive()) {
				++nPC;
			} else if (n.isNonPrimitive()) {
				++nNC;
			}
			if (n.isSynonym()) {
				++nsFull;
			}
			if (n.isCompletelyDefined()) {
				if (n.isPrimitive()) {
					++npFull;
				}
			} else if (!n.hasToldSubsumers()) {
				++nNoTold;
			}
		}
		for (TIndividual pi : Individuals.getList()) {
			final TConcept n = pi;
			if (!isValid(n.getpName())) {
				continue;
			}
			++nSing;
			if (n.isPrimitive()) {
				++nPC;
			} else if (n.isNonPrimitive()) {
				++nNC;
			}
			if (n.isSynonym()) {
				++nsFull;
			}
			if (n.isCompletelyDefined()) {
				if (n.isPrimitive()) {
					++npFull;
				}
			} else if (!n.hasToldSubsumers()) {
				++nNoTold;
			}
		}
		if (IfDefs._USE_LOGGING) {
			LL.print(String.format("There are %s primitive concepts used\n of which %s completely defined\n      and %s has no told subsumers\nThere are %s non-primitive concepts used\n of which %s synonyms\nThere are %s individuals or nominals used\n", nPC, npFull, nNoTold,
					nNC, nsFull, nSing));
		}
	}

	public void RemoveExtraDescriptions() {
		for (TConcept pc : Concepts.getList()) {
			pc.removeDescription();
		}
		for (TIndividual pi : Individuals.getList()) {
			pi.removeDescription();
		}
	}

	/** set the value of a test timeout in milliseconds to VALUE */
	public void setTestTimeout(long value) {
		testTimeout = value;
		if (stdReasoner != null) {
			stdReasoner.setTestTimeout(value);
		}
		if (nomReasoner != null) {
			nomReasoner.setTestTimeout(value);
		}
	}

	/** set ToDo priorities using local OPTIONS */
	public void setToDoPriorities() {
		stdReasoner.initToDoPriorities(pOptions);
		if (nomReasoner != null) {
			nomReasoner.initToDoPriorities(pOptions);
		}
	}

	/** @return true iff individual C is known to be p-blocked by another one */
	boolean isBlockedInd(TConcept C) {
		return SameI.containsKey(C);
	}

	/** get individual that blocks C; works only for blocked individuals C */
	TIndividual getBlockingInd(TConcept C) {
		return SameI.get(C).first;
	}

	/** @return true iff an individual blocks C deterministically */
	boolean isBlockingDet(TConcept C) {
		return SameI.get(C).second;
	}

	/** init const cache for either bpTOP or bpBOTTOM */
	void initConstCache(int p) {
		DLHeap.setCache(p, ModelCacheConst.createConstCache(p));
	}

	/** init [singleton] cache for given concept and polarity */
	void initSingletonCache(TConcept p, boolean pos) {
		DLHeap.setCache(createBiPointer(p.getpName(), pos), new ModelCacheSingleton(createBiPointer(p.index(), pos)));
	}

	public ModelCacheInterface initCache(final TConcept pConcept, boolean sub) {
		int bp = sub ? -pConcept.getpName() : pConcept.getpName();
		ModelCacheInterface cache = DLHeap.getCache(bp);
		if (cache == null) {
			if (sub) {
				prepareFeatures(null, pConcept);
			} else {
				prepareFeatures(pConcept, null);
			}
			cache = getReasoner().createCache(bp, FastSetFactory.create());
			clearFeatures();
		}
		return cache;
	}

	/** test if 2 concept non-subsumption can be determined by cache merging */
	public ModelCacheState testCachedNonSubsumption(final TConcept p, final TConcept q) {
		final ModelCacheInterface pCache = initCache(p, /*sub=*/false);
		final ModelCacheInterface nCache = initCache(q, /*sub=*/true);
		return pCache.canMerge(nCache);
	}

	public void initReasoner() {
		if (stdReasoner == null) {
			assert nomReasoner == null;
			stdReasoner = new DlSatTester(this, pOptions);
			stdReasoner.setTestTimeout(testTimeout);
			if (NCFeatures.hasSingletons()) {
				nomReasoner = new NominalReasoner(this, pOptions);
				nomReasoner.setTestTimeout(testTimeout);
			}
		}
	}

	private long nRelevantCCalls;
	private long nRelevantBCalls;

	/** set relevance for a DLVertex */
	public final void setRelevant(int p) {
		assert isValid(p);
		if (p == bpTOP || p == bpBOTTOM) {
			return;
		}
		final DLVertex v = DLHeap.get(p);
		boolean pos = p > 0;
		++nRelevantBCalls;
		collectLogicFeature(v, pos);
		DagTag type = v.Type();
		switch (type) {
			case dtDataType:
			case dtDataValue:
			case dtDataExpr:
			case dtNN:
				break;
			case dtPConcept:
			case dtPSingleton:
			case dtNConcept:
			case dtNSingleton:
				setRelevant((TConcept) v.getConcept());
				break;
			case dtForall:
			case dtLE:
				setRelevant(v.getRole());
				setRelevant(v.getC());
				break;
			case dtProj:
				setRelevant(v.getC());
				break;
			case dtIrr:
				setRelevant(v.getRole());
				break;
			case dtAnd:
			case dtCollection:
				for (int q : v.begin()) {
					setRelevant(q);
				}
				break;
			default:
				throw new ReasonerInternalException("Error setting relevant vertex of type " + type.getName() + "(" + type + ")");
		}
	}

	/** set given concept relevant wrt current TBox if not checked yet */
	public final void setRelevant(TConcept p) {
		if (!p.isRelevant(relevance)) {
			++nRelevantCCalls;
			p.setRelevant(relevance);
			collectLogicFeature(p);
			setRelevant(p.getpBody());
		}
	}

	/** set given role relevant wrt current TBox if not checked yet */
	public final void setRelevant(TRole p) {
		if (p.getId() != 0 && !p.isRelevant(relevance)) {
			p.setRelevant(relevance);
			collectLogicFeature(p);
			setRelevant(p.getBPDomain());
			setRelevant(p.getBPRange());
			List<TRole> list = p.getAncestor();
			int size = list.size();
			for (int i = 0; i < size; i++) {
				setRelevant(list.get(i));
			}
		}
	}

	public void gatherRelevanceInfo() {
		nRelevantCCalls = 0;
		nRelevantBCalls = 0;
		//int cSize = 0;
		int bSize = 0;
		curFeature = GCIFeatures;
		markGCIsRelevant();
		clearRelevanceInfo();
		KBFeatures.binaryOrOperator(GCIFeatures);
		NCFeatures = new LogicFeatures(GCIFeatures);
		for (TIndividual pi : Individuals.getList()) {
			setConceptRelevant(pi);
			NCFeatures.binaryOrOperator(pi.getPosFeatures());
		}
		if (NCFeatures.hasSomeAll() && !RelatedI.isEmpty()) {
			NCFeatures.setInverseRoles();
		}
		for (TConcept pc : Concepts.getList()) {
			setConceptRelevant(pc);
		}
		bSize = DLHeap.size() - 2;
		curFeature = null;
		double bRatio = 0; //
		double sqBSize = 1;
		if (bSize > 20) {
			bRatio = (float) nRelevantBCalls / bSize;
			sqBSize = Math.sqrt(bSize);
		}
		// set up GALEN-like flag; based on r/n^{3/2}, add r/n^2<1
		isLikeGALEN = bRatio > sqBSize * 20 && bRatio < bSize;
	}

	public void printFeatures() {
		KBFeatures.writeState();
		LL.print("KB contains " + (GCIs.isGCI() ? "" : "NO ") + "GCIs\nKB contains " + (GCIs.isReflexive() ? "" : "NO ") + "reflexive roles\nKB contains " + (GCIs.isRnD() ? "" : "NO ") + "range and domain restrictions\n");
	}

	protected List<List<TIndividual>> getDifferent() {
		return Different;
	}

	protected List<TRelated> getRelatedI() {
		return RelatedI;
	}

	protected DLDag getDLHeap() {
		return DLHeap;
	}

	protected TKBFlags getGCIs() {
		return GCIs;
	}

	/** replace (AR:C) with X such that C [= AR^-:X for fresh X. @return X */
	TConcept replaceForall(DLTree RC) {
		// check whether we already did this before for given R,C
		if (RCCache.containsKey(RC)) {
			return RCCache.get(RC);
		}
		TConcept X = getAuxConcept(null);
		DLTree C = DLTreeFactory.createSNFNot(RC.Right().copy());
		// create ax axiom C [= AR^-.X
		addSubsumeAxiom(C, DLTreeFactory.createSNFForall(DLTreeFactory.createInverse(RC.Left().copy()), getTree(X)));
		// save cache for R,C
		RCCache.put(RC, X);
		return X;
	}

	public AtomicBoolean isCancelled() {
		return interrupted;
	}
}

enum DIOp {
	// concept expressions
	diNot("not"), diAnd("and"), diOr("or"), diExists("some"), diForall("all"), diGE("atleast"), diLE("atmost"),
	// role expressions
	diInv,
	// individual expressions
	diOneOf,
	// wrong operation
	//diErrorOp(9),
	// end of the enum
	diEndOp,
	// wrong axiom
	//diErrorAx(9),
	// concept axioms
	diDefineC("defprimconcept"), diImpliesC("implies_c"), diEqualsC("equal_c"), diDisjointC,
	// role axioms
	diDefineR("defprimrole"), diTransitiveR("transitive"), diFunctionalR("functional"), diImpliesR("implies_r"), diEqualsR("equal_r"), diDomainR("domain"), diRangeR("range"),
	// individual axioms
	diInstanceOf;
	private String s;

	private DIOp() {
		s = "";
	}

	private DIOp(String s) {
		this.s = s;
	}

	public String getString() {
		return s;
	}
}

class DumpInterface {
	/** output stream */
	protected final PrintStream o;
	/** indentation level */
	private int indent;
	/** print every axiom on a single line (need for sorting, for example) */
	private boolean oneliner;

	public DumpInterface(PrintStream oo) {
		o = oo;
		indent = 0;
		oneliner = false;
	}

	// global prologue/epilogue
	public final void prologue() {
	}

	public final void epilogue() {
	}

	// general concept expression
	public void dumpTop() {
	}

	public void dumpBottom() {
	}

	@SuppressWarnings("unused")
	public void dumpNumber(int n) {
	}
	@SuppressWarnings("unused")
	public void startOp(DIOp Op) {
	}

	/** start operation >=/<= with number */
	@SuppressWarnings("unused")
	public void startOp(DIOp Op, int n) {
	}
	@SuppressWarnings("unused")
	public void contOp(DIOp Op) {
	}
	@SuppressWarnings("unused")
	public void finishOp(DIOp Op) {
	}
	@SuppressWarnings("unused")
	public void startAx(DIOp Ax) {
	}
	@SuppressWarnings("unused")
	public void contAx(DIOp Ax) {
	}
	@SuppressWarnings("unused")
	public void finishAx(DIOp Ax) {
	}

	/** obtain name by the named entry */
	public void dumpName(final TNamedEntry p) {
		o.print(p.getName());
	}

	/** dump concept atom (as used in expression) */
	@SuppressWarnings("unused")
	public void dumpConcept(final TConcept p) {
	}

	/** dump role atom (as used in expression) */
	@SuppressWarnings("unused")
	public void dumpRole(final TRole p) {
	}

	public final void skipIndent() {
		if (oneliner) {
			return;
		}
		o.print("\n");
		for (int i = indent - 1; i >= 0; --i) {
			o.print("  ");
		}
	}

	public final void incIndent() {
		skipIndent();
		++indent; // operands of AND-like
	}

	public final void decIndent() {
		--indent;
		skipIndent();
	}
}

final class DumpLisp extends DumpInterface {
	public DumpLisp(PrintStream oo) {
		super(oo);
	}

	// general concept expression
	@Override
	public void dumpTop() {
		o.print("*TOP*");
	}

	@Override
	public void dumpBottom() {
		o.print("*BOTTOM*)");
	}

	@Override
	public void dumpNumber(int n) {
		o.print(n + " ");
	}

	/** start operation >=/<= with number */
	@Override
	public void startOp(DIOp Op, int n) {
		startOp(Op);
		dumpNumber(n);
	}

	@Override
	public void contOp(DIOp Op) {
		if (Op == DIOp.diAnd || Op == DIOp.diOr) {
			skipIndent();
		} else {
			o.print(" ");
		}
	}

	@Override
	public void finishOp(DIOp Op) {
		if (Op == DIOp.diAnd || Op == DIOp.diOr) {
			decIndent();
		}
		o.print(")");
	}

	//abstract void startAx ( diAx Ax );
	@Override
	@SuppressWarnings("unused")
	public void contAx(DIOp Ax) {
		o.print(" ");
	}

	@Override
	@SuppressWarnings("unused")
	public void finishAx(DIOp Ax) {
		o.print(")\n");
	}

	/** obtain name by the named entry */
	@Override
	public void dumpName(final TNamedEntry p) {
		o.print("|" + p.getName() + "|");
	}

	/** dump concept atom (as used in expression) */
	@Override
	public void dumpConcept(final TConcept p) {
		dumpName(p);
	}

	/** dump role atom (as used in expression) */
	@Override
	public void dumpRole(final TRole p) {
		if (p.getId() < 0) // inverse
		{
			o.print("(inv ");
			dumpName(p.inverse());
			o.print(")");
		} else {
			dumpName(p);
		}
	}

	@Override
	public void startOp(DIOp Op) {
		if (Op == DIOp.diAnd || Op == DIOp.diOr) {
			incIndent();
		}
		o.print("(");
		o.print(Op.getString());
		if (Op == DIOp.diEndOp) {
			throw new UnreachableSituationException();
		}
		contOp(Op);
	}

	@Override
	public void startAx(DIOp Ax) {
		o.print("(");
		o.print(Ax.getString());
		if (Ax == DIOp.diEndOp) {
			throw new UnreachableSituationException();
		}
		contAx(Ax);
	}
}