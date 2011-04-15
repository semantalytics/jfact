package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.DLTree.equalTrees;
import static uk.ac.manchester.cs.jfact.helpers.Helper.*;
import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.logger;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.semanticweb.owlapi.reasoner.ReasonerInternalException;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

import uk.ac.manchester.cs.jfact.helpers.DLTree;
import uk.ac.manchester.cs.jfact.helpers.DLTreeFactory;
import uk.ac.manchester.cs.jfact.helpers.DLVertex;
import uk.ac.manchester.cs.jfact.helpers.FastSet;
import uk.ac.manchester.cs.jfact.helpers.FastSetFactory;
import uk.ac.manchester.cs.jfact.helpers.IfDefs;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapterStringBuilder;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;
import uk.ac.manchester.cs.jfact.helpers.Pair;
import uk.ac.manchester.cs.jfact.helpers.Timer;
import uk.ac.manchester.cs.jfact.helpers.UnreachableSituationException;
import uk.ac.manchester.cs.jfact.kernel.datatype.DataEntry;
import uk.ac.manchester.cs.jfact.kernel.datatype.DataTypeCenter;
import uk.ac.manchester.cs.jfact.kernel.datatype.Datatypes;
import uk.ac.manchester.cs.jfact.kernel.dl.DataTypeName;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheConst;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheInterface;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheSingleton;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheState;

public final class TBox {
	protected static final class IndividualCreator implements
			NameCreator<Individual> {
		public Individual makeEntry(String name) {
			return new Individual(name);
		}
	}

	protected static final class ConceptCreator implements NameCreator<Concept> {
		public Concept makeEntry(String name) {
			return new Concept(name);
		}
	}

	/**
	 * class for simple rules like Ch :- Cb1, Cbi, CbN; all C are primitive
	 * named concepts
	 */
	final static class SimpleRule {
		/** body of the rule */
		private final List<Concept> simpleRuleBody = new ArrayList<Concept>();
		/** head of the rule as a DLTree */
		protected final DLTree tHead;
		/** head of the rule as a BP */
		private int bpHead;

		public SimpleRule(final List<Concept> body, DLTree head) {
			simpleRuleBody.addAll(body);
			tHead = head;
			setBpHead(bpINVALID);
		}

		public boolean applicable(DlSatTester Reasoner) {
			return Reasoner.applicable(this);
		}

		public List<Concept> getBody() {
			return simpleRuleBody;
		}

		public void setBpHead(int bpHead) {
			this.bpHead = bpHead;
		}

		public int getBpHead() {
			return bpHead;
		}
	}

	private long relevance = 1;
	private DLDag dlHeap;
	/** reasoner for TBox-related queries w/o nominals */
	private DlSatTester stdReasoner;
	/** reasoner for TBox-related queries with nominals */
	private NominalReasoner nomReasoner;
	/** progress monitor */
	private ReasonerProgressMonitor pMonitor;
	/** taxonomy structure of a TBox */
	private DLConceptTaxonomy pTax;
	/** DataType center */
	private DataTypeCenter datatypeCenter = new DataTypeCenter();
	/** set of reasoning options */
	private final IFOptionSet pOptions;
	/** status of the KB */
	private KBStatus kbStatus;
	/** global KB features */
	private LogicFeatures KBFeatures = new LogicFeatures();
	/** GCI features */
	private LogicFeatures GCIFeatures = new LogicFeatures();
	/** nominal cloud features */
	private LogicFeatures nominalCloudFeatures = new LogicFeatures();
	/** aux features */
	private LogicFeatures auxFeatures = new LogicFeatures();
	/** pointer to current feature (in case of local ones) */
	private LogicFeatures curFeature = new LogicFeatures();
	/**
	 * concept representing temporary one that can not be used anywhere in the
	 * ontology
	 */
	private Concept pTemp;
	/** temporary concept that represents query */
	private Concept defConcept;
	/** all named concepts */
	private final NamedEntryCollection<Concept> concepts;
	/** all named individuals/nominals */
	private final NamedEntryCollection<Individual> individuals;
	/** "normal" (object) roles */
	private RoleMaster objectRoleMaster;
	/** data roles */
	private RoleMaster dataRoleMaster;
	/** set of GCIs */
	private AxiomSet axioms;
	/** given individual-individual relations */
	private List<Related> relatedIndividuals = new ArrayList<Related>();
	/** known disjoint sets of individuals */
	private List<List<Individual>> differentIndividuals = new ArrayList<List<Individual>>();
	/** all simple rules in KB */
	private List<SimpleRule> simpleRules = new ArrayList<SimpleRule>();
	/** internalisation of a general axioms */
	private int internalisedGeneralAxiom;
	/** KB flags about GCIs */
	private KBFlags GCIs;
	/** cache for the \forall R.C replacements during absorption */
	private Map<DLTree, Concept> forall_R_C_Cache = new HashMap<DLTree, Concept>();
	/** current aux concept's ID */
	private int auxConceptID;
	/**
	 * how many times nominals were found during translation to DAG; local to
	 * BuildDAG
	 */
	private int nNominalReferences;
	/** searchable stack for the told subsumers */
	private Set<Concept> conceptInProcess = new HashSet<Concept>();
	/** fairness constraints */
	private List<Concept> fairness = new ArrayList<Concept>();
	/** single SAT/SUB test timeout in milliseconds */
	private long testTimeout;
	// Flags section
	/** flag for full/short KB */
	private boolean useRelevantOnly;
	/** flag for creating taxonomy */
	private boolean useCompletelyDefined;
	/** flag for dumping TBox relevant to query */
	private boolean dumpQuery;
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
	private boolean consistent;
	/** whether KB(ABox) is precompleted */
	private boolean precompleted;
	/** time spend for preprocessing */
	private long preprocTime;
	/** time spend for consistency checking */
	private long consistTime;
	/** number of concepts and individuals; used to set index for modelCache */
	int nC = 0;
	/** number of all distinct roles; used to set index for modelCache */
	int nR = 0;
	/** map to show the possible equivalence between individuals */
	Map<Concept, Pair<Individual, Boolean>> sameIndividuals = new HashMap<Concept, Pair<Individual, Boolean>>();
	/** all the synonyms in the told subsumers' cycle */
	Set<Concept> toldSynonyms = new HashSet<Concept>();

	/** RW begin() for individuals */
	public List<Individual> i_begin() {
		return individuals.getList();
	}

	/** get concept by it's BP (non-final version) */
	public DataEntry getDataEntryByBP(int bp) {
		DataEntry p = (DataEntry) dlHeap.get(bp).getConcept();
		assert p != null;
		return p;
	}

	/** add description to a concept; @return true in case of error */
	public boolean initNonPrimitive(Concept p, DLTree desc) {
		if (!p.canInitNonPrim(desc)) {
			return true;
		}
		makeNonPrimitive(p, desc);
		return false;
	}

	/** make concept non-primitive; @return it's old description */
	public DLTree makeNonPrimitive(Concept p, DLTree desc) {
		DLTree ret = p.makeNonPrimitive(desc);
		checkEarlySynonym(p);
		return ret;
	}

	/** checks if C is defined as C=D and set Synonyms accordingly */
	public void checkEarlySynonym(Concept p) {
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
	public int reflexive2dag(final Role R) {
		// input check: only simple roles are allowed in the reflexivity construction
		if (!R.isSimple()) {
			throw new ReasonerInternalException(
					"Non simple role used as simple: " + R.getName());
		}
		return -dlHeap.add(new DLVertex(dtIrr, 0, R, bpINVALID, null));
	}

	/** create forall node for data role */
	public int dataForall2dag(final Role R, int C) {
		return dlHeap.add(new DLVertex(dtForall, 0, R, C, null));
	}

	/** create atmost node for data role */
	public int dataAtMost2dag(int n, final Role R, int C) {
		return dlHeap.add(new DLVertex(dtLE, n, R, C, null));
	}

	/** @return a pointer to concept representation */
	public int concept2dag(Concept p) {
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
		axioms.addAxiom(C, D);
	}

	/** absorb all axioms */
	public void absorbAxioms() {
		int nSynonyms = countSynonyms();
		axioms.absorb();
		if (countSynonyms() > nSynonyms) {
			replaceAllSynonyms();
		}
		if (axioms.wasRoleAbsorptionApplied()) {
			initToldSubsumers();
		}
	}

	/** set told TOP concept whether necessary */
	public void initToldSubsumers() {
		for (Concept pc : concepts.getList()) {
			if (!pc.isSynonym()) {
				pc.initToldSubsumers();
			}
		}
		for (Individual pi : individuals.getList()) {
			if (!pi.isSynonym()) {
				pi.initToldSubsumers();
			}
		}
	}

	/** set told TOP concept whether necessary */
	public void setToldTop() {
		for (Concept pc : concepts.getList()) {
			pc.setToldTop(top);
		}
		for (Individual pi : individuals.getList()) {
			pi.setToldTop(top);
		}
	}

	/** calculate TS depth for all concepts */
	public void calculateTSDepth() {
		for (Concept pc : concepts.getList()) {
			pc.calculateTSDepth();
		}
		for (Individual pi : individuals.getList()) {
			pi.calculateTSDepth();
		}
	}

	/** @return number of synonyms in the KB */
	public int countSynonyms() {
		int nSynonyms = 0;
		for (Concept pc : concepts.getList()) {
			if (pc.isSynonym()) {
				++nSynonyms;
			}
		}
		for (Individual pi : individuals.getList()) {
			if (pi.isSynonym()) {
				++nSynonyms;
			}
		}
		return nSynonyms;
	}

	/** init Extra Rule field in concepts given by a vector V with a given INDEX */
	public void initRuleFields(final List<Concept> v, int index) {
		for (Concept q : v) {
			q.addExtraRule(index);
		}
	}

	/** mark all concepts wrt their classification tag */
	public void fillsClassificationTag() {
		for (Concept pc : concepts.getList()) {
			pc.getClassTag();
		}
		for (Individual pi : individuals.getList()) {
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
	public void printConcepts(LogAdapter o) {
		if (concepts.size() == 0) {
			return;
		}
		o.print(String.format("Concepts (%s):\n", concepts.size()));
		for (Concept pc : concepts.getList()) {
			printConcept(o, pc);
		}
	}

	/** print all registered individuals */
	public void printIndividuals(LogAdapter o) {
		if (individuals.size() == 0) {
			return;
		}
		o.print(String.format("Individuals (%s):\n", individuals.size()));
		for (Individual pi : individuals.getList()) {
			printConcept(o, pi);
		}
	}

	public void printSimpleRules(LogAdapter o) {
		if (simpleRules.isEmpty()) {
			return;
		}
		o.print(String.format("Simple rules (%s):\n", simpleRules.size()));
		for (SimpleRule p : simpleRules) {
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

	public void printAxioms(LeveLogger.LogAdapter o) {
		if (internalisedGeneralAxiom == bpTOP) {
			return;
		}
		o.print("Axioms:\nT [=");
		depth = 0;
		printDagEntry(o, internalisedGeneralAxiom);
	}

	int depth;

	/** check if the role R is irreflexive */
	public boolean isIrreflexive(Role R) {
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
	private void collectLogicFeature(final Concept p) {
		if (curFeature != null) {
			curFeature.fillConceptData(p);
		}
	}

	/** gather information about logical features of relevant role */
	private void collectLogicFeature(final Role p) {
		if (curFeature != null) {
			curFeature.fillRoleData(p, p.inverse().isRelevant(relevance));
		}
	}

	/** gather information about logical features of relevant DAG entry */
	private void collectLogicFeature(final DLVertex v, boolean pos) {
		if (curFeature != null) {
			curFeature.fillDAGData(v, pos);
		}
	}

	/** mark all active GCIs relevant */
	private void markGCIsRelevant() {
		setRelevant(internalisedGeneralAxiom);
	}

	/** set all TBox content (namely, concepts and GCIs) relevant */
	private void markAllRelevant() {
		for (Concept pc : concepts.getList()) {
			if (!pc.isRelevant(relevance)) {
				++nRelevantCCalls;
				pc.setRelevant(relevance);
				collectLogicFeature(pc);
				setRelevant(pc.getpBody());
			}
			//			setRelevant(pc);
		}
		for (Individual pi : individuals.getList()) {
			if (!pi.isRelevant(relevance)) {
				++nRelevantCCalls;
				pi.setRelevant(relevance);
				collectLogicFeature(pi);
				setRelevant(pi.getpBody());
			}
			//setRelevant(pi);
		}
		markGCIsRelevant();
	}

	/** clear all relevance info */
	public void clearRelevanceInfo() {
		relevance++;
	}

	/** get fresh concept */
	public DLTree getFreshConcept() {
		return DLTreeFactory.buildTree(new Lexeme(CNAME, pTemp));
	}

	/** put relevance information to a concept's data */
	private void setConceptRelevant(Concept p) {
		curFeature = p.getPosFeatures();
		setRelevant(p.getpBody());
		KBFeatures.or(p.getPosFeatures());
		collectLogicFeature(p);
		clearRelevanceInfo();
		// nothing to do for neg-prim concepts
		if (p.isPrimitive()) {
			return;
		}
		curFeature = p.getNegFeatures();
		setRelevant(-p.getpBody());
		KBFeatures.or(p.getNegFeatures());
		clearRelevanceInfo();
	}

	/** update AUX features with the given one; update roles if necessary */
	private void updateAuxFeatures(final LogicFeatures lf) {
		if (!lf.isEmpty()) {
			auxFeatures.or(lf);
			auxFeatures.mergeRoles();
		}
	}

	/** clear current features */
	public void clearFeatures() {
		curFeature = null;
	}

	/** get RW access to used Role Master */
	public RoleMaster getORM() {
		return objectRoleMaster;
	}

	/** get RW access to used DataRole Master */
	public RoleMaster getDRM() {
		return dataRoleMaster;
	}

	/** get RW access to the RoleMaster depending of the R */
	public RoleMaster getRM(final Role R) {
		return R.isDataRole() ? dataRoleMaster : objectRoleMaster;
	}

	/** get RW access to a DT center */
	public DataTypeCenter getDataTypeCenter() {
		return datatypeCenter;
	}

	/** return registered concept by given NAME; @return null if can't register */
	public Concept getConcept(String name) {
		return concepts.get(name);
	}

	/**
	 * return registered individual by given NAME; @return null if can't
	 * register
	 */
	public Individual getIndividual(String name) {
		return individuals.get(name);
	}

	/** @return true iff given NAME is a name of a registered individual */
	private boolean isIndividual(final String name) {
		return individuals.isRegistered(name);
	}

	/** @return true iff given TREE represents a registered individual */
	public boolean isIndividual(final DLTree tree) {
		return tree.token() == INAME
				&& isIndividual(tree.elem().getNE().getName());
	}

	//TODO move
	/** get TOP/BOTTOM/CN/IN by the DLTree entry */
	public Concept getCI(final DLTree name) {
		if (name.isTOP()) {
			return top;
		}
		if (name.isBOTTOM()) {
			return bottom;
		}
		if (!name.isName()) {
			return null;
		}
		if (name.token() == CNAME) {
			return (Concept) name.elem().getNE();
		} else {
			return (Individual) name.elem().getNE();
		}
	}

	/** get a DL tree by a given concept-like C */
	public DLTree getTree(Concept C) {
		if (C == null) {
			return null;
		}
		if (C.isTop()) {
			return DLTreeFactory.createTop();
		}
		if (C.isBottom()) {
			return DLTreeFactory.createBottom();
		}
		return DLTreeFactory.buildTree(new Lexeme(
				isIndividual(C.getName()) ? INAME : CNAME, C));
	}

	/**
	 * set the flag that forbid usage of undefined names for concepts/roles; @return
	 * old value
	 */
	public boolean setForbidUndefinedNames(boolean val) {
		objectRoleMaster.setUndefinedNames(!val);
		dataRoleMaster.setUndefinedNames(!val);
		individuals.setLocked(val);
		return concepts.setLocked(val);
	}

	/** individual relation <a,b>:R */
	public void registerIndividualRelation(NamedEntry a, NamedEntry R,
			NamedEntry b) {
		if (!isIndividual(a.getName()) || !isIndividual(b.getName())) {
			throw new ReasonerInternalException(
					"Individual expected in related()");
		}
		relatedIndividuals.add(new Related((Individual) a, (Individual) b,
				(Role) R));
		relatedIndividuals.add(new Related((Individual) b, (Individual) a,
				((Role) R).inverse()));
	}

	/** add axiom CN [= D for concept CN */
	public void addSubsumeAxiom(Concept C, DLTree D) {
		addSubsumeAxiom(getTree(C), D);
	}

	/** add simple rule RULE to the TBox' rules */
	public void addSimpleRule(SimpleRule Rule) {
		initRuleFields(Rule.getBody(), simpleRules.size());
		simpleRules.add(Rule);
	}

	/** @return true if KB contains fairness constraints */
	public boolean hasFC() {
		return !fairness.isEmpty();
	}

	public void setFairnessConstraintDLTrees(List<DLTree> l) {
		for (int i = 0; i < l.size(); i++) {
			// build a flag for a FC
			Concept fc = getAuxConcept(null);
			fairness.add(fc);
			// make an axiom: C [= FC
			addSubsumeAxiom(l.get(i), getTree(fc));
		}
	}

	/** GCI Axioms access */
	public int getTG() {
		return internalisedGeneralAxiom;
	}

	/** get simple rule by its INDEX */
	public final SimpleRule getSimpleRule(int index) {
		return simpleRules.get(index);
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
		return p.hasFunctionalRestriction() || p.hasNumberRestriction()
				|| p.hasQNumberRestriction();
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
		precompleted = true;
	}

	/** if KB is precompleted */
	public boolean isPrecompleted() {
		return precompleted;
	}

	/** get status flag */
	public KBStatus getStatus() {
		return kbStatus;
	}

	/** set consistency flag */
	public void setConsistency(boolean val) {
		kbStatus = kbCChecked;
		consistent = val;
	}

	/** check if the ontology is consistent */
	public boolean isConsistent() {
		if (kbStatus.ordinal() < kbCChecked.ordinal()) {
			prepareReasoning();
			if (kbStatus.ordinal() < kbCChecked.ordinal() && consistent) {
				setConsistency(performConsistencyCheck());
			}
		}
		return consistent;
	}

	/** test if 2 concept non-subsumption can be determined by sorts checking */
	public boolean testSortedNonSubsumption(final Concept p, final Concept q) {
		// sorted reasoning doesn't work in presence of GCIs
		if (!canUseSortedReasoning()) {
			return false;
		}
		// doesn't work for the SAT tests
		if (q == null) {
			return false;
		}
		return !dlHeap.haveSameSort(p.getpName(), q.getpName());
	}

	/** print TBox as a whole */
	public void print(LogAdapter o) {
		dlHeap.printStat(o);
		objectRoleMaster.print(o, "Object");
		dataRoleMaster.print(o, "Data");
		printConcepts(o);
		printIndividuals(o);
		printSimpleRules(o);
		printAxioms(o);
		dlHeap.print(o);
	}

	public void buildDAG() {
		nNominalReferences = 0;
		for (Concept pc : concepts.getList()) {
			concept2dag(pc);
		}
		for (Individual pi : individuals.getList()) {
			concept2dag(pi);
		}
		for (SimpleRule q : simpleRules) {
			q.setBpHead(tree2dag(q.tHead));
		}
		// builds Roles range and domain
		initRangeDomain(objectRoleMaster);
		initRangeDomain(dataRoleMaster);
		DLTree GCI = axioms.getGCI();
		// add special domains to the GCIs
		List<DLTree> list = new ArrayList<DLTree>();
		//TODO quick for Robert's ontology
		for (Role p : objectRoleMaster.getRoles()) {
			if (!p.isSynonym() && p.hasSpecialDomain()) {
				list.add(p.getTSpecialDomain().copy());
			}
		}
		if (list.size() > 0) {
			list.add(GCI);
			GCI = DLTreeFactory.createSNFAnd(list);
		}
		internalisedGeneralAxiom = tree2dag(GCI);
		GCI = null;
		// mark GCI flags
		GCIs.setGCI(internalisedGeneralAxiom != bpTOP);
		GCIs.setReflexive(objectRoleMaster.hasReflexiveRoles());
		for (Role p : objectRoleMaster.getRoles()) {
			if (!p.isSynonym() && p.isTopFunc()) {
				p.setFunctional(atmost2dag(1, p, bpTOP));
			}
		}
		for (Role p : dataRoleMaster.getRoles()) {
			if (!p.isSynonym() && p.isTopFunc()) {
				p.setFunctional(atmost2dag(1, p, bpTOP));
			}
		}
		concept2dag(pTemp);
		if (nNominalReferences > 0) {
			int nInd = individuals.getList().size();
			if (nInd > 100 && nNominalReferences > nInd) {
				isLikeWINE = true;
			}
		}
	}

	public void initRangeDomain(RoleMaster RM) {
		for (Role p : RM.getRoles()) {
			if (!p.isSynonym()) {
				Role R = p;
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

	public int addDataExprToHeap(DataEntry p) {
		if (isValid(p.getBP())) {
			return p.getBP();
		}
		DagTag dt = p.isBasicDataType() ? dtDataType
				: p.isDataValue() ? dtDataValue : dtDataExpr;
		int hostBP = bpTOP;
		//TODO this is broken: the next two lines are commented but should not be
		if (p.getType() != null) {
			hostBP = addDataExprToHeap(p.getType());
		}
		// sets it off 0 and 1 - although it's not necessarily correct
		//hostBP = p.getDatatype().ordinal() + 2;
		DLVertex ver = new DLVertex(dt, 0, null, hostBP, null);
		ver.setConcept(p);
		//	System.out.println("TBox.addDataExprToHeap() "+p);
		p.setBP(dlHeap.directAdd(ver));
		//dlHeap.print(new LeveLogger.LogAdapterStream());
		return p.getBP();
	}

	public int addDataExprToHeap(Datatypes p) {
		//XXX possibly a bug here
		//int hostBP = p.getDatatype().ordinal() + 2;
		//
		// create a concept and check if it's already in the list
		// TODO needs to be more efficient
		DataTypeName concept = new DataTypeName(p);
		int index = dlHeap.index(concept);
		if (index != bpINVALID) {
			return index;
		}
		//		System.out.println("TBox.addDataExprToHeap(datatypename) "+p);
		// else, create a new vertex and add it
		DLVertex ver = new DLVertex(dtDataType, 0, null, bpTOP, null);
		ver.setConcept(concept);
		int directAdd = dlHeap.directAdd(ver);
		return directAdd;
	}

	public void addConceptToHeap(Concept pConcept) {
		DagTag tag = pConcept.isPrimitive() ? (pConcept.isSingleton() ? dtPSingleton
				: dtPConcept)
				: pConcept.isSingleton() ? dtNSingleton : dtNConcept;
		// NSingleton is a nominal
		if (tag == dtNSingleton && !pConcept.isSynonym()) {
			((Individual) pConcept).setNominal(true);
		}
		DLVertex ver = new DLVertex(tag);
		ver.setConcept(pConcept);
		pConcept.setpName(dlHeap.directAdd(ver));
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
		final Lexeme cur = t.elem();
		int ret = bpINVALID;
		switch (cur.getToken()) {
			case BOTTOM:
				ret = bpBOTTOM;
				break;
			case TOP:
				ret = bpTOP;
				break;
			case DATAEXPR:
				if (cur.getNE() instanceof DataEntry) {
					ret = addDataExprToHeap((DataEntry) cur.getNE());
				} else {
					ret = addDataExprToHeap(((DataTypeName) cur.getNE())
							.getDatatype());
				}
				break;
			case CNAME:
				ret = concept2dag((Concept) cur.getNE());
				break;
			case INAME:
				++nNominalReferences;// definitely a nominal
				Individual ind = (Individual) cur.getNE();
				ind.setNominal(true);
				ret = concept2dag(ind);
				break;
			case NOT:
				ret = -tree2dag(t.getChild());
				break;
			case AND:
				ret = and2dag(new DLVertex(dtAnd), t);
				break;
			case FORALL:
				ret = forall2dag(Role.resolveRole(t.getLeft()),
						tree2dag(t.getRight()));
				break;
			case REFLEXIVE:
				ret = reflexive2dag(Role.resolveRole(t.getChild()));
				break;
			case LE:
				ret = atmost2dag(cur.getData(), Role.resolveRole(t.getLeft()),
						tree2dag(t.getRight()));
				break;
			case PROJFROM:
				ret = dlHeap
						.directAdd(new DLVertex(DagTag.dtProj, 0, Role
								.resolveRole(t.getLeft()), tree2dag(t
								.getRight().getRight()), Role.resolveRole(t
								.getRight().getLeft())));
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
			return dlHeap.add(v);
		}
		return ret;
	}

	public int forall2dag(final Role R, int C) {
		if (R.isDataRole()) {
			return dataForall2dag(R, C);
		}
		int ret = dlHeap.add(new DLVertex(dtForall, 0, R, C, null));
		if (R.isSimple()) {
			return ret;
		}
		if (!dlHeap.isLast(ret)) {
			return ret;
		}
		for (int i = 1; i < R.getAutomaton().size(); ++i) {
			dlHeap.directAddAndCache(new DLVertex(dtForall, i, R, C, null));
		}
		return ret;
	}

	public int atmost2dag(int n, final Role R, int C) {
		if (!R.isSimple()) {
			throw new ReasonerInternalException(
					"Non simple role used as simple: " + R.getName());
		}
		if (R.isDataRole()) {
			return dataAtMost2dag(n, R, C);
		}
		int ret = dlHeap.add(new DLVertex(dtLE, n, R, C, null));
		if (!dlHeap.isLast(ret)) {
			return ret;
		}
		for (int m = n - 1; m > 0; --m) {
			dlHeap.directAddAndCache(new DLVertex(dtLE, m, R, C, null));
		}
		dlHeap.directAddAndCache(new DLVertex(dtNN));
		return ret;
	}

	private final boolean fillANDVertex(DLVertex v, final DLTree t) {
		if (t.isAND()) {
			boolean ret = false;
			List<DLTree> children = t.getChildren();
			final int size = children.size();
			for (int i = 0; i < size; i++) {
				ret |= fillANDVertex(v, children.get(i));
			}
			return ret;
		} else {
			return v.addChild(tree2dag(t));
		}
	}

	public void initTaxonomy() {
		pTax = new DLConceptTaxonomy(top, bottom, this, GCIs);
	}

	private List<Concept> arrayCD = new ArrayList<Concept>(),
			arrayNoCD = new ArrayList<Concept>(),
			arrayNP = new ArrayList<Concept>();

	public <T extends Concept> int fillArrays(List<T> begin) {
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
			dlHeap.setSubOrder();
			initTaxonomy();
			needConcept |= needIndividual;
		} else {
			return;
		}
		if (verboseOutput) {
			logger.println("Processing query...");
		}
		Timer locTimer = new Timer();
		locTimer.start();
		nItems = 0;
		arrayCD.clear();
		arrayNoCD.clear();
		arrayNP.clear();
		nItems += fillArrays(concepts.getList());
		nItems += fillArrays(individuals.getList());
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
		locTimer.stop();
		if (verboseOutput) {
			logger.println(" done in " + locTimer.calcDelta() + " seconds\n");
		}
		if (needConcept && kbStatus.ordinal() < kbClassified.ordinal()) {
			kbStatus = kbClassified;
		}
		if (needIndividual) {
			kbStatus = kbRealised;
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

	public void classifyConcepts(final List<Concept> collection,
			boolean curCompletelyDefined, final String type) {
		pTax.setCompletelyDefined(curCompletelyDefined);
		logger.print(Templates.CLASSIFY_CONCEPTS, type);
		int n = 0;
		for (Concept q : collection) {
			if (!interrupted.get() && !q.isClassified()) {
				// need to classify concept
				classifyEntry(q);
				if (q.isClassified()) {
					++n;
				}
			}
		}
		logger.print(Templates.CLASSIFY_CONCEPTS2, n, type);
	}

	/** classify single concept */
	void classifyEntry(Concept entry) {
		if (isBlockedInd(entry)) {
			classifyEntry(getBlockingInd(entry)); // make sure that the possible synonym is already classified
		}
		if (!entry.isClassified()) {
			pTax.classifyEntry(entry);
		}
	}

	public TBox(final IFOptionSet Options, final String TopORoleName,
			final String BotORoleName, final String TopDRoleName,
			final String BotDRoleName, AtomicBoolean interrupted) {
		this.interrupted = interrupted;
		axioms = new AxiomSet(this);
		GCIs = new KBFlags();
		dlHeap = new DLDag(Options);
		stdReasoner = null;
		nomReasoner = null;
		pMonitor = null;
		pTax = null;
		pOptions = Options;
		kbStatus = kbLoading;
		curFeature = null;
		defConcept = null;
		concepts = new NamedEntryCollection<Concept>("concept",
				new ConceptCreator());
		individuals = new NamedEntryCollection<Individual>("individual",
				new IndividualCreator());
		objectRoleMaster = new RoleMaster(false, TopORoleName, BotORoleName);
		dataRoleMaster = new RoleMaster(true, TopDRoleName, BotDRoleName);
		axioms = new AxiomSet(this);
		internalisedGeneralAxiom = bpTOP;
		auxConceptID = 0;
		useSortedReasoning = true;
		isLikeGALEN = false;
		isLikeWINE = false;
		consistent = true;
		precompleted = false;
		preprocTime = 0;
		consistTime = 0;
		readConfig(Options);
		initTopBottom();
		setForbidUndefinedNames(false);
	}

	public Concept getAuxConcept(DLTree desc) {
		Concept C = getConcept(" aux" + ++auxConceptID);
		C.setSystem();
		C.setNonClassifiable();
		C.setPrimitive();
		C.addDesc(desc);
		C.initToldSubsumers();
		return C;
	}

	private Concept top;
	private Concept bottom;

	private void initTopBottom() {
		top = Concept.getTOP();
		bottom = Concept.getBOTTOM();
		pTemp = Concept.getTEMP();
	}

	public void prepareReasoning() {
		preprocess();
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
			DumpLisp lDump = new DumpLisp(of);
			dump(lDump);
			of.close();
			clearRelevanceInfo();
		}
		dlHeap.setSatOrder();
		setToDoPriorities();
	}

	public void prepareFeatures(final Concept pConcept, final Concept qConcept) {
		auxFeatures = new LogicFeatures(GCIFeatures);
		if (pConcept != null) {
			updateAuxFeatures(pConcept.getPosFeatures());
		}
		if (qConcept != null) {
			updateAuxFeatures(qConcept.getNegFeatures());
		}
		if (auxFeatures.hasSingletons()) {
			updateAuxFeatures(nominalCloudFeatures);
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
		for (Concept pc : concepts.getList()) {
			if (pc.isPrimitive()) {
				initSingletonCache(pc, false);
			}
		}
		for (Individual pi : individuals.getList()) {
			if (pi.isPrimitive()) {
				initSingletonCache(pi, false);
			}
		}
	}

	public boolean performConsistencyCheck() {
		if (verboseOutput) {
			logger.println("Consistency checking...");
		}
		Timer pt = new Timer();
		pt.start();
		buildSimpleCache();
		Concept test = nominalCloudFeatures.hasSingletons()
				&& individuals.getList().size() > 0 ? individuals.getList()
				.get(0) : null;
		prepareFeatures(test, null);
		boolean ret = false;
		if (test != null) {
			if (dlHeap.getCache(bpTOP) == null) {
				initConstCache(bpTOP);
			}
			ret = nomReasoner.consistentNominalCloud();
		} else {
			ret = isSatisfiable(top);
			// setup cache for GCI
			if (GCIs.isGCI()) {
				dlHeap.setCache(-internalisedGeneralAxiom, new ModelCacheConst(
						false));
			}
		}
		pt.stop();
		consistTime = pt.calcDelta();
		if (verboseOutput) {
			logger.println(" done in " + consistTime + " seconds\n");
		}
		return ret;
	}

	public boolean isSatisfiable(final Concept pConcept) {
		assert pConcept != null;
		ModelCacheInterface cache = dlHeap.getCache(pConcept.getpName());
		if (cache != null) {
			return cache.getState() != ModelCacheState.csInvalid;
		}
		logger.println(Templates.IS_SATISFIABLE, pConcept.getName());
		prepareFeatures(pConcept, null);
		boolean result = getReasoner().runSat(pConcept.resolveId(), bpTOP);
		cache = getReasoner().buildCacheByCGraph(result);
		dlHeap.setCache(pConcept.getpName(), cache);
		clearFeatures();
		logger.print(Templates.IS_SATISFIABLE1, pConcept.getName(),
				(!result ? "un" : ""));
		return result;
	}

	public boolean isSubHolds(Concept pConcept, Concept qConcept) {
		assert pConcept != null && qConcept != null;
		logger.print(Templates.ISSUBHOLDS1, pConcept.getName(),
				qConcept.getName());
		prepareFeatures(pConcept, qConcept);
		boolean result = !getReasoner().runSat(pConcept.resolveId(),
				-qConcept.resolveId());
		clearFeatures();
		logger.print(Templates.ISSUBHOLDS2, pConcept.getName(),
				qConcept.getName(), (!result ? " NOT" : ""));
		return result;
	}

	public boolean isSameIndividuals(final Individual a, final Individual b) {
		if (a.equals(b)) {
			return true;
		}
		if (!isIndividual(a.getName()) || !isIndividual(b.getName())) {
			throw new ReasonerInternalException(
					"Individuals are expected in the isSameIndividuals() query");
		}
		if (a.getNode() == null || b.getNode() == null) {
			if (a.isSynonym()) {
				return isSameIndividuals((Individual) a.getSynonym(), b);
			}
			if (b.isSynonym()) {
				return isSameIndividuals(a, (Individual) b.getSynonym());
			}
			throw new ReasonerInternalException(
					"isSameIndividuals() query with non-realised ontology");
		}
		return a.getNode().resolvePBlocker()
				.equals(b.getNode().resolvePBlocker());
	}

	public boolean isDisjointRoles(final Role R, final Role S) {
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
		logger.println(Templates.READ_CONFIG, useCompletelyDefined,
				useRelevantOnly, dumpQuery, alwaysPreferEquals,
				usePrecompletion);
		if (axioms.initAbsorptionFlags(Options.getText("absorptionFlags"))) {
			throw new ReasonerInternalException(
					"Incorrect absorption flags given");
		}
		verboseOutput = true;
	}

	/**
	 * remove concept from TBox by given EXTERNAL id. XXX WARNING!! tested only
	 * for TempConcept!!!
	 */
	public void removeConcept(Concept p) {
		assert p.equals(defConcept);
		// clear DAG and name indeces (if necessary)
		if (isCorrect(p.getpName())) {
			dlHeap.removeAfter(p.getpName());
		}
		if (concepts.remove(p)) {
			throw new UnreachableSituationException();
		}
	}

	public void clearQueryConcept() {
		removeConcept(defConcept);
	}

	private static final String defConceptName = "jfact.default";

	public Concept createQueryConcept(final DLTree desc) {
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
		dlHeap.setExpressionCache(false);
		addConceptToHeap(defConcept);
		setConceptRelevant(defConcept);
		initCache(defConcept, false);
		return defConcept;
	}

	public void classifyQueryConcept() {
		defConcept.initToldSubsumers();
		assert pTax != null;
		pTax.setCompletelyDefined(false);
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
		assert kbStatus.ordinal() >= kbCChecked.ordinal();
		if (consistent) {
			o.print("Required");
		} else {
			o.print("KB is inconsistent. Query is NOT processed\nConsistency");
		}
		long sum = preprocTime + consistTime;
		o.print(String
				.format(" check done in %s seconds\nof which:\nPreproc. takes %s seconds\nConsist. takes %s seconds",
						time, preprocTime, consistTime));
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
		o.print((float) f / 1000);
		o.print(" seconds\n");
		print(o);
	}

	public void printDagEntry(LeveLogger.LogAdapter o, int p) {
		depth++;
		if (depth % 200 == 0) {
			System.out.println("TBox.printDagEntry()");
			return;
		}
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
			printDagEntry(o, -p);
			o.print(")");
			return;
		}
		final DLVertex v = dlHeap.get(Math.abs(p));
		DagTag type = v.getType();
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
				o.print(String.format(" (%s %s)", type.getName(), v.getRole()
						.getName()));
				return;
			case dtCollection:
			case dtAnd:
				o.print(" (");
				o.print(type.getName());
				for (int q : v.begin()) {
					printDagEntry(o, q);
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
				printDagEntry(o, v.getConceptIndex());
				o.print(")");
				return;
			case dtProj:
				o.print(String.format(" (%s %s)", type.getName(), v.getRole()
						.getName()));
				printDagEntry(o, v.getConceptIndex());
				o.print(String.format(" => %s)", v.getProjRole().getName()));
				return;
			case dtNN:
				throw new UnreachableSituationException();
			default:
				throw new ReasonerInternalException(
						"Error printing vertex of type " + type.getName() + "("
								+ type + ")");
		}
	}

	public void printConcept(LogAdapter o, final Concept p) {
		if (isValid(p.getpName())) {
			o.print(p.getClassTagPlain().getCTTagName());
			if (p.isSingleton()) {
				o.print((p.isNominal() ? 'o' : '!'));
			}
			o.print(String.format(".%s [%s] %s", p.getName(), p.getTsDepth(),
					(p.isNonPrimitive() ? "=" : "[=")));
			if (isValid(p.getpBody())) {
				depth = 0;
				printDagEntry(o, p.getpBody());
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
		for (Concept pc : concepts.getList()) {
			if (pc.isRelevant(relevance)) {
				dumpConcept(dump, pc);
			}
		}
		for (Individual pi : individuals.getList()) {
			if (pi.isRelevant(relevance)) {
				dumpConcept(dump, pi);
			}
		}
		if (internalisedGeneralAxiom != bpTOP) {
			dump.startAx(DIOp.diImpliesC);
			dump.dumpTop();
			dump.contAx(DIOp.diImpliesC);
			dumpExpression(dump, internalisedGeneralAxiom);
			dump.finishAx(DIOp.diImpliesC);
		}
		dump.epilogue();
	}

	public void dumpConcept(DumpInterface dump, final Concept p) {
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

	public void dumpRole(DumpInterface dump, final Role p) {
		if (p.getId() > 0 || !p.inverse().isRelevant(relevance)) {
			final Role q = p.getId() > 0 ? p : p.inverse();
			dump.startAx(DIOp.diDefineR);
			dump.dumpRole(q);
			dump.finishAx(DIOp.diDefineR);
			for (ClassifiableEntry i : q.getToldSubsumers()) {
				dump.startAx(DIOp.diImpliesR);
				dump.dumpRole(q);
				dump.contAx(DIOp.diImpliesR);
				dump.dumpRole((Role) i);
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
		final DLVertex v = dlHeap.get(Math.abs(p));
		DagTag type = v.getType();
		switch (type) {
			case dtTop: {
				dump.dumpTop();
				return;
			}
			case dtPConcept:
			case dtNConcept:
			case dtPSingleton:
			case dtNSingleton: {
				dump.dumpConcept((Concept) v.getConcept());
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
				dumpExpression(dump, v.getConceptIndex());
				dump.finishOp(DIOp.diForall);
				return;
			case dtLE:
				dump.startOp(DIOp.diLE, v.getNumberLE());
				dump.dumpRole(v.getRole());
				dump.contOp(DIOp.diLE);
				dumpExpression(dump, v.getConceptIndex());
				dump.finishOp(DIOp.diLE);
				return;
			default:
				throw new ReasonerInternalException(
						"Error dumping vertex of type " + type.getName() + "("
								+ type + ")");
		}
	}

	public void dumpAllRoles(DumpInterface dump) {
		for (Role p : objectRoleMaster.getRoles()) {
			if (p.isRelevant(relevance)) {
				assert !p.isSynonym();
				dumpRole(dump, p);
			}
		}
		for (Role p : dataRoleMaster.getRoles()) {
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
		Concept C = resolveSynonym(getCI(CN));
		assert C != null;
		// lie: this will never be reached
		if (C.isBottom()) {
			return DLTreeFactory.createBottom();
		}
		if (C.isTop()) {
		} else if (!(C.isSingleton() && D.isName())
				&& equalTrees(C.getDescription(), D)) {
			makeNonPrimitive(C, D);
		} else {
			return CN;
		}
		return null;
	}

	public DLTree applyAxiomCNToC(DLTree CN, DLTree D) {
		Concept C = resolveSynonym(getCI(CN));
		assert C != null;
		if (C.isTop()) {
			return DLTreeFactory.createTop();
		}
		if (C.isBottom()) {
		} else if (C.isPrimitive()) {
			C.addDesc(D);
		} else {
			addSubsumeForDefined(C, D);
		}
		return null;
	}

	public void addSubsumeForDefined(Concept C, DLTree D) {
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
			Role.resolveRole(sup.getLeft()).setRange(sup.getRight().copy());
			return true;
		}
		if (sub.token() == NOT && sub.getChild().token() == FORALL
				&& sub.getChild().getRight().isBOTTOM()) {
			Role.resolveRole(sub.getChild().getLeft()).setDomain(sup);
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
		Concept C = resolveSynonym(getCI(left));
		if (C == null || C.isTop() || C.isBottom()) {
			return false;
		}
		Concept D = getCI(right);
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
		Concept C = resolveSynonym(getCI(left));
		if (C == null || C.isTop() || C.isBottom()) {
			return false;
		}
		Concept D = resolveSynonym(getCI(right));
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
			if (d.isName() && ((Concept) d.elem().getNE()).isPrimitive()) {
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
		List<Individual> acc = new ArrayList<Individual>();
		for (int i = 0; i < l.size(); i++) {
			if (isIndividual(l.get(i))) {
				acc.add((Individual) l.get(i).elem().getNE());
				l.set(i, null);
			} else {
				throw new ReasonerInternalException(
						"Only individuals allowed in processDifferent()");
			}
		}
		if (acc.size() > 1) {
			differentIndividuals.add(acc);
		}
	}

	public void processSame(List<DLTree> l) {
		final int size = l.size();
		for (int i = 0; i < size; i++) {
			if (!isIndividual(l.get(i))) {
				throw new ReasonerInternalException(
						"Only individuals allowed in processSame()");
			}
		}
		for (int i = 0; i < size - 1; i++) {
			//TODO check if this is checking all combinations
			addEqualityAxiom(l.get(i), l.get(i + 1).copy());
		}
	}

	public void processDisjointR(List<DLTree> l) {
		if (l.isEmpty()) {
			throw new ReasonerInternalException("Empty disjoint role axiom");
		}
		final int size = l.size();
		for (int i = 0; i < size; i++) {
			if (DLTreeFactory.isUniversalRole(l.get(i))) {
				throw new ReasonerInternalException(
						"Universal role in the disjoint roles axiom");
			}
		}
		List<Role> roles = new ArrayList<Role>(size);
		for (int i = 0; i < size; i++) {
			roles.add(Role.resolveRole(l.get(i)));
		}
		RoleMaster RM = getRM(roles.get(0));
		for (int i = 0; i < size - 1; i++) {
			for (int j = i + 1; j < size; j++) {
				RM.addDisjointRoles(roles.get(i), roles.get(j));
			}
		}
	}

	public void processEquivalentR(List<DLTree> l) {
		if (l.size() > 0) {
			RoleMaster RM = getRM(Role.resolveRole(l.get(0)));
			for (int i = 0; i < l.size() - 1; i++) {
				RM.addRoleSynonym(Role.resolveRole(l.get(i)),
						Role.resolveRole(l.get(i + 1)));
			}
			l.clear();
		}
	}

	public void preprocess() {
		if (verboseOutput) {
			logger.println("\nPreprocessing...");
		}
		Timer pt = new Timer();
		pt.start();
		objectRoleMaster.initAncDesc();
		dataRoleMaster.initAncDesc();
		if (verboseOutput) {
			try {
				//TODO
				PrintStream oroles = new PrintStream("Taxonomy.ORoles");
				LogAdapterStringBuilder b = new LogAdapterStringBuilder();
				objectRoleMaster.getTaxonomy().print(b);
				oroles.print(b.toString());
				oroles.close();
				b = new LogAdapterStringBuilder();
				PrintStream droles = new PrintStream("Taxonomy.DRoles");
				dataRoleMaster.getTaxonomy().print(b);
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
		absorbAxioms();
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
		dlHeap.setOrderDefaults(isLikeGALEN ? "Fdn" : isLikeWINE ? "Sdp"
				: "Sap", isLikeGALEN ? "Ban" : isLikeWINE ? "Fdn" : "Dap");
		dlHeap.gatherStatistic();
		calculateStatistic();
		removeExtraDescriptions();
		pt.stop();
		preprocTime = pt.calcDelta();
		if (verboseOutput) {
			logger.println(" done in " + pt.calcDelta() + " seconds\n");
		}
	}

	private void setAllIndexes() {
		nC = 1; // start with 1 to make index 0 an indicator of "not processed"
		pTemp.setIndex(nC++);
		for (Concept pc : concepts.getList()) {
			if (!pc.isSynonym()) {
				pc.setIndex(nC++);
			}
		}
		for (Individual pi : individuals.getList()) {
			if (!pi.isSynonym()) {
				pi.setIndex(nC++);
			}
		}
		++nC; // place for the query concept
		nR = 1; // the same
		for (Role r : objectRoleMaster.getRoles()) {
			if (!r.isSynonym()) {
				r.setIndex(nR++);
			}
		}
		for (Role r : dataRoleMaster.getRoles()) {
			if (!r.isSynonym()) {
				r.setIndex(nR++);
			}
		}
	}

	private void replaceAllSynonyms() {
		for (Role r : objectRoleMaster.getRoles()) {
			if (!r.isSynonym()) {
				DLTreeFactory.replaceSynonymsFromTree(r.getTDomain());
			}
		}
		for (Role dr : dataRoleMaster.getRoles()) {
			if (!dr.isSynonym()) {
				DLTreeFactory.replaceSynonymsFromTree(dr.getTDomain());
			}
		}
		for (Concept pc : concepts.getList()) {
			if (DLTreeFactory.replaceSynonymsFromTree(pc.getDescription())) {
				pc.initToldSubsumers();
			}
		}
		for (Individual pi : individuals.getList()) {
			if (DLTreeFactory.replaceSynonymsFromTree(pi.getDescription())) {
				pi.initToldSubsumers();
			}
		}
	}

	public void preprocessRelated() {
		for (Related q : relatedIndividuals) {
			q.simplify();
		}
	}

	public void transformToldCycles() {
		int nSynonyms = countSynonyms();
		clearRelevanceInfo();
		for (Concept pc : concepts.getList()) {
			if (!pc.isSynonym()) {
				checkToldCycle(pc);
			}
		}
		for (Individual pi : individuals.getList()) {
			if (!pi.isSynonym()) {
				checkToldCycle(pi);
			}
		}
		clearRelevanceInfo();
		nSynonyms = countSynonyms() - nSynonyms;
		if (nSynonyms > 0) {
			logger.print(Templates.TRANSFORM_TOLD_CYCLES, nSynonyms);
			replaceAllSynonyms();
		}
	}

	public Concept checkToldCycle(Concept _p) {
		assert _p != null;
		Concept p = resolveSynonym(_p);
		if (p.isTop()) {
			return null;
		}
		if (conceptInProcess.contains(p)) {
			return p;
		}
		if (p.isRelevant(relevance)) {
			return null;
		}
		Concept ret = null;
		conceptInProcess.add(p);
		boolean redo = false;
		while (!redo) {
			redo = true;
			for (ClassifiableEntry r : p.getToldSubsumers()) {
				if ((ret = checkToldCycle((Concept) r)) != null) {
					if (ret.equals(p)) {
						toldSynonyms.add(p);
						for (Concept q : toldSynonyms) {
							if (q.isSingleton()) {
								p = q;
							}
						}
						Set<DLTree> leaves = new HashSet<DLTree>();
						for (Concept q : toldSynonyms) {
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
						toldSynonyms.clear();
						p.setPrimitive();
						p.addLeaves(leaves);
						p.removeSelfFromDescription();
						if (!ret.equals(p)) {
							conceptInProcess.remove(ret);
							conceptInProcess.add(p);
							ret.setRelevant(relevance);
							p.dropRelevant(relevance);
						}
						ret = null;
						redo = false;
						break;
					} else {
						toldSynonyms.add(p);
						redo = true;
						break;
					}
				}
			}
		}
		conceptInProcess.remove(p);
		p.setRelevant(relevance);
		return ret;
	}

	public void transformSingletonHierarchy() {
		int nSynonyms = countSynonyms();
		boolean changed;
		do {
			changed = false;
			for (Individual pi : individuals.getList()) {
				if (!pi.isSynonym() && pi.isHasSP()) {
					Individual i = transformSingletonWithSP(pi);
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

	public Individual getSPForConcept(Concept p) {
		for (ClassifiableEntry r : p.getToldSubsumers()) {
			Concept i = (Concept) r;
			if (i.isSingleton()) {
				return (Individual) i;
			}
			if (i.isHasSP()) {
				return transformSingletonWithSP(i);
			}
		}
		throw new UnreachableSituationException();
	}

	private Individual transformSingletonWithSP(Concept p) {
		Individual i = getSPForConcept(p);
		if (p.isSingleton()) {
			i.addRelated((Individual) p);
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
			for (Related p : relatedIndividuals) {
				dlHeap.updateSorts(p.getA().getpName(), p.getRole(), p.getB()
						.getpName());
			}
			// simple rules needs the same treatement
			for (SimpleRule q : simpleRules) {
				MergableLabel lab = dlHeap.get(q.bpHead).getSort();
				for (Concept r : q.simpleRuleBody) {
					dlHeap.merge(lab, r.getpName());
				}
			}
			// create sorts for concept and/or roles
			dlHeap.determineSorts(objectRoleMaster, dataRoleMaster);
		}
	}

	public void calculateStatistic() {
		int npFull = 0, nsFull = 0;
		int nPC = 0, nNC = 0, nSing = 0;
		int nNoTold = 0;
		for (Concept pc : concepts.getList()) {
			final Concept n = pc;
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
		for (Individual pi : individuals.getList()) {
			final Concept n = pi;
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
		if (IfDefs.USE_LOGGING) {
			logger.print(String
					.format("There are %s primitive concepts used\n of which %s completely defined\n      and %s has no told subsumers\nThere are %s non-primitive concepts used\n of which %s synonyms\nThere are %s individuals or nominals used\n",
							nPC, npFull, nNoTold, nNC, nsFull, nSing));
		}
	}

	public void removeExtraDescriptions() {
		for (Concept pc : concepts.getList()) {
			pc.removeDescription();
		}
		for (Individual pi : individuals.getList()) {
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
	public boolean isBlockedInd(Concept C) {
		return sameIndividuals.containsKey(C);
	}

	/** get individual that blocks C; works only for blocked individuals C */
	public Individual getBlockingInd(Concept C) {
		return sameIndividuals.get(C).first;
	}

	/** @return true iff an individual blocks C deterministically */
	public boolean isBlockingDet(Concept C) {
		return sameIndividuals.get(C).second;
	}

	/** init const cache for either bpTOP or bpBOTTOM */
	private void initConstCache(int p) {
		dlHeap.setCache(p, ModelCacheConst.createConstCache(p));
	}

	/** init [singleton] cache for given concept and polarity */
	private void initSingletonCache(Concept p, boolean pos) {
		dlHeap.setCache(createBiPointer(p.getpName(), pos),
				new ModelCacheSingleton(createBiPointer(p.index(), pos)));
	}

	public ModelCacheInterface initCache(final Concept pConcept, boolean sub) {
		int bp = sub ? -pConcept.getpName() : pConcept.getpName();
		ModelCacheInterface cache = dlHeap.getCache(bp);
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
	public ModelCacheState testCachedNonSubsumption(final Concept p,
			final Concept q) {
		final ModelCacheInterface pCache = initCache(p, /*sub=*/false);
		final ModelCacheInterface nCache = initCache(q, /*sub=*/true);
		return pCache.canMerge(nCache);
	}

	public void initReasoner() {
		if (stdReasoner == null) {
			assert nomReasoner == null;
			stdReasoner = new DlSatTester(this, pOptions);
			stdReasoner.setTestTimeout(testTimeout);
			if (nominalCloudFeatures.hasSingletons()) {
				nomReasoner = new NominalReasoner(this, pOptions);
				nomReasoner.setTestTimeout(testTimeout);
			}
		}
	}

	private long nRelevantCCalls;
	private long nRelevantBCalls;

	/** set relevance for a DLVertex */
	private final void setRelevant(int _p) {
		FastSet done = FastSetFactory.create();
		LinkedList<Integer> queue = new LinkedList<Integer>();
		queue.add(_p);
		while (queue.size() > 0) {
			int p = queue.remove(0);
			if (done.contains(p)) {
				// skip cycles
				//				System.out.println("TBox.setRelevant() cycle: "+p);
				continue;
			}
			done.add(p);
			assert isValid(p);
			if (p == bpTOP || p == bpBOTTOM) {
				continue;
			}
			final DLVertex v = realSetRelevant(p);
			DagTag type = v.getType();
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
					Concept concept = (Concept) v.getConcept();
					if (!concept.isRelevant(relevance)) {
						++nRelevantCCalls;
						concept.setRelevant(relevance);
						collectLogicFeature(concept);
						//setRelevant(concept.getpBody());
						queue.add(concept.getpBody());
					}
					//setRelevant((Concept) v.getConcept());
					break;
				case dtForall:
				case dtLE:
					//setRelevant(v.getRole());
				{
					Role _role = v.getRole();
					List<Role> rolesToExplore = new LinkedList<Role>();
					rolesToExplore.add(_role);
					while (rolesToExplore.size() > 0) {
						Role roleToExplore = rolesToExplore.remove(0);
						if (roleToExplore.getId() != 0
								&& !roleToExplore.isRelevant(relevance)) {
							roleToExplore.setRelevant(relevance);
							collectLogicFeature(roleToExplore);
							//setRelevant(roleToExplore.getBPDomain());
							//setRelevant(roleToExplore.getBPRange());
							queue.add(roleToExplore.getBPDomain());
							queue.add(roleToExplore.getBPRange());
							rolesToExplore.addAll(roleToExplore.getAncestor());
						}
					}
				}
					//setRelevant(v.getConceptIndex());
					queue.add(v.getConceptIndex());
					break;
				case dtProj:
					//setRelevant(v.getConceptIndex());
					queue.add(v.getConceptIndex());
					break;
				case dtIrr:
					//setRelevant(v.getRole());
				{
					Role _role = v.getRole();
					List<Role> rolesToExplore = new LinkedList<Role>();
					rolesToExplore.add(_role);
					while (rolesToExplore.size() > 0) {
						Role roleToExplore = rolesToExplore.remove(0);
						if (roleToExplore.getId() != 0
								&& !roleToExplore.isRelevant(relevance)) {
							roleToExplore.setRelevant(relevance);
							collectLogicFeature(roleToExplore);
							//setRelevant(roleToExplore.getBPDomain());
							//setRelevant(roleToExplore.getBPRange());
							queue.add(roleToExplore.getBPDomain());
							queue.add(roleToExplore.getBPRange());
							rolesToExplore.addAll(roleToExplore.getAncestor());
						}
					}
				}
					break;
				case dtAnd:
				case dtCollection:
					for (int q : v.begin()) {
						//setRelevant(q);
						queue.add(q);
					}
					break;
				default:
					throw new ReasonerInternalException(
							"Error setting relevant vertex of type " + type);
			}
		}
	}

	private DLVertex realSetRelevant(int p) {
		final DLVertex v = dlHeap.get(p);
		boolean pos = p > 0;
		++nRelevantBCalls;
		collectLogicFeature(v, pos);
		return v;
	}

	/** set given concept relevant wrt current TBox if not checked yet */
	//	private final void setRelevant(Concept p) {
	//		if (!p.isRelevant(relevance)) {
	//			++nRelevantCCalls;
	//			p.setRelevant(relevance);
	//			collectLogicFeature(p);
	//			setRelevant(p.getpBody());
	//		}
	//	}
	/** set given role relevant wrt current TBox if not checked yet */
	//	private final void setRelevant(Role _p) {
	//		List<Role> rolesToExplore = new LinkedList<Role>();
	//		rolesToExplore.add(_p);
	//		while (rolesToExplore.size() > 0) {
	//			Role roleToExplore = rolesToExplore.remove(0);
	//			if (roleToExplore.getId() != 0
	//					&& !roleToExplore.isRelevant(relevance)) {
	//				roleToExplore.setRelevant(relevance);
	//				collectLogicFeature(roleToExplore);
	//				setRelevant(roleToExplore.getBPDomain());
	//				setRelevant(roleToExplore.getBPRange());
	//				rolesToExplore.addAll(roleToExplore.getAncestor());
	//			}
	//		}
	//	}
	private void gatherRelevanceInfo() {
		nRelevantCCalls = 0;
		nRelevantBCalls = 0;
		int bSize = 0;
		curFeature = GCIFeatures;
		markGCIsRelevant();
		clearRelevanceInfo();
		KBFeatures.or(GCIFeatures);
		nominalCloudFeatures = new LogicFeatures(GCIFeatures);
		for (Individual pi : individuals.getList()) {
			setConceptRelevant(pi);
			nominalCloudFeatures.or(pi.getPosFeatures());
		}
		if (nominalCloudFeatures.hasSomeAll() && !relatedIndividuals.isEmpty()) {
			nominalCloudFeatures.setInverseRoles();
		}
		for (Concept pc : concepts.getList()) {
			setConceptRelevant(pc);
		}
		bSize = dlHeap.size() - 2;
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
		logger.print("KB contains " + (GCIs.isGCI() ? "" : "NO ")
				+ "GCIs\nKB contains " + (GCIs.isReflexive() ? "" : "NO ")
				+ "reflexive roles\nKB contains " + (GCIs.isRnD() ? "" : "NO ")
				+ "range and domain restrictions\n");
	}

	public List<List<Individual>> getDifferent() {
		return differentIndividuals;
	}

	public List<Related> getRelatedI() {
		return relatedIndividuals;
	}

	public DLDag getDLHeap() {
		return dlHeap;
	}

	public KBFlags getGCIs() {
		return GCIs;
	}

	/** replace (AR:C) with X such that C [= AR^-:X for fresh X. @return X */
	public Concept replaceForall(DLTree RC) {
		// check whether we already did this before for given R,C
		if (forall_R_C_Cache.containsKey(RC)) {
			return forall_R_C_Cache.get(RC);
		}
		Concept X = getAuxConcept(null);
		DLTree C = DLTreeFactory.createSNFNot(RC.getRight().copy());
		// create ax axiom C [= AR^-.X
		addSubsumeAxiom(C, DLTreeFactory.createSNFForall(
				DLTreeFactory.createInverse(RC.getLeft().copy()), getTree(X)));
		// save cache for R,C
		forall_R_C_Cache.put(RC, X);
		return X;
	}

	public AtomicBoolean isCancelled() {
		return interrupted;
	}
}

enum KBStatus {
	kbEmpty, // no axioms loaded yet; not used in TBox
	kbLoading, // axioms are added to the KB, no preprocessing done
	kbCChecked, // KB is preprocessed and consistency checked
	kbClassified, // KB is classified
	kbRealised
	// KB is realised
}

enum DIOp {
	// concept expressions
	diNot("not"), diAnd("and"), diOr("or"), diExists("some"), diForall("all"), diGE(
			"atleast"), diLE("atmost"),
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
	diDefineR("defprimrole"), diTransitiveR("transitive"), diFunctionalR(
			"functional"), diImpliesR("implies_r"), diEqualsR("equal_r"), diDomainR(
			"domain"), diRangeR("range"),
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
	public void dumpName(final NamedEntry p) {
		o.print(p.getName());
	}

	/** dump concept atom (as used in expression) */
	@SuppressWarnings("unused")
	public void dumpConcept(final Concept p) {
	}

	/** dump role atom (as used in expression) */
	@SuppressWarnings("unused")
	public void dumpRole(final Role p) {
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
	public void dumpName(final NamedEntry p) {
		o.print("|" + p.getName() + "|");
	}

	/** dump concept atom (as used in expression) */
	@Override
	public void dumpConcept(final Concept p) {
		dumpName(p);
	}

	/** dump role atom (as used in expression) */
	@Override
	public void dumpRole(final Role p) {
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