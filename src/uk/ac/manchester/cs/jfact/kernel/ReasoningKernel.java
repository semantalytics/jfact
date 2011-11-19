package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
 Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
 This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.DLTree.equalTrees;
import static uk.ac.manchester.cs.jfact.kernel.CacheStatus.*;
import static uk.ac.manchester.cs.jfact.kernel.KBStatus.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

import uk.ac.manchester.cs.jfact.helpers.DLTree;
import uk.ac.manchester.cs.jfact.helpers.DLTreeFactory;
import uk.ac.manchester.cs.jfact.helpers.ELFAxiomChecker;
import uk.ac.manchester.cs.jfact.helpers.ELFNormalizer;
import uk.ac.manchester.cs.jfact.helpers.ELFReasoner;
import uk.ac.manchester.cs.jfact.helpers.LogAdapter;
import uk.ac.manchester.cs.jfact.helpers.UnreachableSituationException;
import uk.ac.manchester.cs.jfact.kernel.actors.Actor;
import uk.ac.manchester.cs.jfact.kernel.actors.RIActor;
import uk.ac.manchester.cs.jfact.kernel.actors.SupConceptActor;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptName;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptTop;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomConceptInclusion;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomDRoleDomain;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomDRoleFunctional;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomDRoleRange;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomDRoleSubsumption;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomDeclaration;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomDifferentIndividuals;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomDisjointConcepts;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomDisjointDRoles;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomDisjointORoles;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomDisjointUnion;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomEquivalentConcepts;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomEquivalentDRoles;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomEquivalentORoles;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomFairnessConstraint;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomInstanceOf;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomORoleDomain;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomORoleFunctional;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomORoleRange;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomORoleSubsumption;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomRelatedTo;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomRelatedToNot;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomRoleAsymmetric;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomRoleInverse;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomRoleInverseFunctional;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomRoleIrreflexive;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomRoleReflexive;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomRoleSymmetric;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomRoleTransitive;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomSameIndividuals;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomValueOf;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomValueOfNot;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Axiom;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ConceptExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Expression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.IndividualExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ObjectRoleComplexExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ObjectRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.RoleExpression;
import uk.ac.manchester.cs.jfact.kernel.options.JFactReasonerConfiguration;
import uk.ac.manchester.cs.jfact.split.TAxiomSplitter;
import datatypes.DatatypeFactory;
import datatypes.Literal;
import datatypes.LiteralEntry;

public final class ReasoningKernel {
	/** options for the kernel and all related substructures */
	private final JFactReasonerConfiguration kernelOptions;
	/** local TBox (to be created) */
	private TBox pTBox;
	/** set of axioms */
	private final Ontology ontology = new Ontology();
	/** expression translator to work with queries */
	private ExpressionTranslator pET;
	// Top/Bottom role names: if set, they will appear in all hierarchy-related output
	/** top object role name */
	private String topORoleName;
	/** bottom object role name */
	private String botORoleName;
	/** top data role name */
	private String topDRoleName;
	/** bottom data role name */
	private String botDRoleName;
	// values to propagate to the new KB in case of clearance
	private AtomicBoolean interrupted;

	public void setInterruptedSwitch(AtomicBoolean b) {
		interrupted = b;
	}

	// reasoning cache
	/** cache level */
	private CacheStatus cacheLevel;
	/** cached query concept description */
	private DLTree cachedQueryTree;
	/** cached concept (either defConcept or existing one) */
	private Concept cachedConcept;
	/** cached query result (taxonomy position) */
	private TaxonomyVertex cachedVertex;
	// internal flags
	/** set if TBox throws an exception during preprocessing/classification */
	private boolean reasoningFailed;
	/** trace vector for the last operation (set from the TBox trace-sets) */
	private final List<Axiom> traceVec = new ArrayList<Axiom>();
	/** flag to gather trace information for the next reasoner's call */
	private boolean needTracing;
	private final DatatypeFactory datatypeFactory;
	// types for knowledge exploration
	/// dag-2-interface translator used in knowledge exploration
	TDag2Interface D2I;
	/// cached query input description
	ConceptExpression cachedQuery;

	//-----------------------------------------------------------------------------
	//--		internal query cache manipulation
	//-----------------------------------------------------------------------------
	/// clear query cache
	void clearQueryCache() {
		cachedQuery = null; //deleteTree(cachedQueryTree);
		cachedQueryTree = null;
	}

	/// set query cache value to QUERY
	void setQueryCache(ConceptExpression query) {
		clearQueryCache();
		cachedQuery = query;
	}

	/// set query cache value to QUERY
	void setQueryCache(DLTree query) {
		clearQueryCache();
		cachedQueryTree = query;
	}

	/// check whether query cache is the same as QUERY
	boolean checkQueryCache(ConceptExpression query) {
		return cachedQuery == query;
	}

	/// check whether query cache is the same as QUERY
	boolean checkQueryCache(DLTree query) {
		return equalTrees(cachedQueryTree, query);
	}

	/** get status of the KB */
	private KBStatus getStatus() {
		if (pTBox == null) {
			return kbEmpty;
		}
		// if the ontology is changed, it needs to be reclassified
		if (ontology.isChanged()) {
			return kbLoading;
		}
		return pTBox.getStatus();
	}

	/** get DLTree corresponding to an expression EXPR */
	private DLTree e(final Expression expr) {
		return expr.accept(pET);
	}

	/// get fresh filled depending of a type of R
	private DLTree getFreshFiller(DLTree R) {
		if (Role.resolveRole(R).isDataRole()) {
			final LiteralEntry t = new LiteralEntry("freshliteral");
			t.setLiteral(DatatypeFactory.LITERAL.buildLiteral("freshliteral"));
			return DLTreeFactory.wrap(t);
		} else {
			return getTBox().getFreshConcept();
		}
	}

	/** clear cache and flags */
	private void initCacheAndFlags() {
		cacheLevel = csEmpty;
		clearQueryCache();
		cachedConcept = null;
		cachedVertex = null;
		reasoningFailed = false;
		needTracing = false;
	}

	public void needTracing() {
		needTracing = true;
	}

	/** @return the trace-set of the last reasoning operation */
	public List<Axiom> getTrace() {
		List<Axiom> toReturn = new ArrayList<Axiom>(traceVec);
		traceVec.clear();
		return toReturn;
	}

	/** axiom C = C1 or ... or Cn; C1 != ... != Cn */
	public Axiom disjointUnion(ConceptExpression C, List<Expression> l) {
		return ontology.add(new AxiomDisjointUnion(C, l));
	}

	/** get related cache for an individual I */
	private List<Individual> getRelated(Individual I, final Role R) {
		if (!I.hasRelatedCache(R)) {
			I.setRelatedCache(R, buildRelatedCache(I, R));
		}
		return I.getRelatedCache(R);
	}

	//-----------------------------------------------------------------------------
	//--		internal reasoning methods
	//-----------------------------------------------------------------------------
	/** @return true iff C is satisfiable */
	private boolean checkSatTree(DLTree C) {
		//		if (C.isCN()) {
		//			return getTBox().isSatisfiable(getTBox().getCI(C));
		//		}
		setUpCache(C, csSat);
		return getTBox().isSatisfiable(cachedConcept);
	}

	/// @return true iff C is satisfiable
	boolean checkSat(ConceptExpression C) {
		setUpCache(C, csSat);
		return getTBox().isSatisfiable(cachedConcept);
	}

	/// helper; @return true iff C is either named concept of Top/Bot
	boolean isNameOrConst(ConceptExpression C) {
		return C instanceof ConceptName || C instanceof ConceptTop
				|| C instanceof ConceptBottom;
	}

	boolean isNameOrConst(DLTree C) {
		return C.isBOTTOM() || C.isTOP() || C.isName();
	}

	/// @return true iff C [= D holds
	boolean checkSub(ConceptExpression C, ConceptExpression D) {
		if (isNameOrConst(D) && isNameOrConst(C)) {
			return checkSub(getTBox().getCI(e(C)), getTBox().getCI(e(D)));
		}
		return !checkSat(getExpressionManager().and(C, getExpressionManager().not(D)));
	}

	/** @return true iff C [= D holds */
	private boolean checkSub(Concept C, Concept D) {
		// check whether a concept is fresh
		if (D.getpName() == 0) // D is fresh
		{
			if (C.getpName() == 0) {
				return C == D; // 2 fresh concepts subsumes one another iff they are the same
			} else {
				// C is known
				return !getTBox().isSatisfiable(C); // C [= D iff C=\bottom
			}
		} else // D is known
		if (C.getpName() == 0) {
			// C [= D iff D = \top, or ~D = \bottom
			return !checkSatTree(DLTreeFactory.createSNFNot(getTBox().getTree(C)));
		}
		if (getStatus().ordinal() < kbClassified.ordinal()) {
			return getTBox().isSubHolds(C, D);
		}
		// classified => do the taxonomy traversal
		SupConceptActor actor = new SupConceptActor(D);
		Taxonomy tax = getCTaxonomy();
		if (tax.getRelativesInfo(C.getTaxVertex(), actor, true, false, true)) {
			return false;
		} else {
			tax.clearCheckedLabel();
			return true;
		}
	}

	//	/** @return true iff C [= D holds */
	//	private boolean checkSub(DLTree C, DLTree D) {
	//		if (C.isCN() && D.isCN()) {
	//			return checkSub(getTBox().getCI(C), getTBox().getCI(D));
	//		}
	//		return !checkSatTree(DLTreeFactory.createSNFAnd(C, DLTreeFactory.createSNFNot(D)));
	//	}
	// get access to internal structures
	/** @throw an exception if no TBox found */
	private void checkTBox() {
		if (pTBox == null) {
			throw new ReasonerInternalException("KB Not Initialised");
		}
	}

	/** get RW access to TBox */
	private TBox getTBox() {
		checkTBox();
		return pTBox;
	}

	/** clear TBox and related structures; keep ontology in place */
	private void clearTBox() {
		pTBox = null;
		pET = null;
		D2I = null;
	}

	/** get RW access to Object RoleMaster from TBox */
	private RoleMaster getORM() {
		return getTBox().getORM();
	}

	/** get RW access to Data RoleMaster from TBox */
	private RoleMaster getDRM() {
		return getTBox().getDRM();
	}

	/** get access to the concept hierarchy */
	private Taxonomy getCTaxonomy() {
		if (!isKBClassified()) {
			throw new ReasonerInternalException(
					"No access to concept taxonomy: ontology not classified");
		}
		return getTBox().getTaxonomy();
	}

	/** get access to the object role hierarchy */
	private Taxonomy getORTaxonomy() {
		if (!isKBPreprocessed()) {
			throw new ReasonerInternalException(
					"No access to the object role taxonomy: ontology not preprocessed");
		}
		return getORM().getTaxonomy();
	}

	/** get access to the data role hierarchy */
	private Taxonomy getDRTaxonomy() {
		if (!isKBPreprocessed()) {
			throw new ReasonerInternalException(
					"No access to the data role taxonomy: ontology not preprocessed");
		}
		return getDRM().getTaxonomy();
	}

	// transformation methods
	/** get individual by the TIndividualExpr */
	private Individual getIndividual(final IndividualExpression i, final String reason) {
		DLTree I = e(i);
		if (I == null) {
			throw new ReasonerInternalException(reason);
		}
		return (Individual) getTBox().getCI(I);
	}

	/** get role by the TRoleExpr */
	private Role getRole(final RoleExpression r, final String reason) {
		return Role.resolveRole(e(r));
	}

	/** get taxonomy of the property wrt it's name */
	private Taxonomy getTaxonomy(Role R) {
		return R.isDataRole() ? getDRTaxonomy() : getORTaxonomy();
	}

	/** get taxonomy vertext of the property wrt it's name */
	private TaxonomyVertex getTaxVertex(Role R) {
		return R.getTaxVertex();
	}

	private JFactReasonerConfiguration getOptions() {
		return kernelOptions;
	}

	/** return classification status of KB */
	public boolean isKBPreprocessed() {
		return getStatus().ordinal() >= kbCChecked.ordinal();
	}

	/** return classification status of KB */
	public boolean isKBClassified() {
		return getStatus().ordinal() >= kbClassified.ordinal();
	}

	/** return realistion status of KB */
	public boolean isKBRealised() {
		return getStatus().ordinal() >= kbRealised.ordinal();
	}

	/** set top/bottom role names to use them in the related output */
	public void setTopBottomRoleNames(final String topO, final String botO,
			final String topD, final String botD) {
		topORoleName = topO;
		botORoleName = botO;
		topDRoleName = topD;
		botDRoleName = botD;
		ontology.getExpressionManager().setTopBottomRoles(topORoleName, botORoleName,
				topDRoleName, botDRoleName);
	}

	/**
	 * dump query processing TIME, reasoning statistics and a (preprocessed)
	 * TBox
	 */
	public void writeReasoningResult(LogAdapter o, long time) {
		getTBox().clearQueryConcept(); // get rid of the query leftovers
		getTBox().writeReasoningResult(o, time);
	}

	// helper methods to query properties of roles
	/** @return true if R is functional wrt ontology */
	private boolean checkFunctionality(DLTree R) {
		// R is transitive iff \ER.C and \ER.\not C is unsatisfiable
		DLTree tmp = DLTreeFactory.createSNFExists(R.copy(),
				DLTreeFactory.createSNFNot(getFreshFiller(R)));
		tmp = DLTreeFactory.createSNFAnd(tmp,
				DLTreeFactory.createSNFExists(R, getFreshFiller(R)));
		return !checkSatTree(tmp);
	}

	/** @return true if R is functional; set the value for R if necessary */
	private boolean getFunctionality(Role R) {
		if (!R.isFunctionalityKnown()) {
			R.setFunctional(checkFunctionality(DLTreeFactory.buildTree(new Lexeme(R
					.isDataRole() ? Token.DNAME : Token.RNAME, R))));
		}
		return R.isFunctional();
	}

	/** @return true if R is transitive wrt ontology */
	private boolean checkTransitivity(DLTree R) {
		// R is transitive iff \ER.\ER.C and \AR.\not C is unsatisfiable
		DLTree tmp = DLTreeFactory.createSNFExists(R.copy(),
				DLTreeFactory.createSNFNot(getTBox().getFreshConcept()));
		tmp = DLTreeFactory.createSNFExists(R.copy(), tmp);
		tmp = DLTreeFactory.createSNFAnd(tmp,
				DLTreeFactory.createSNFForall(R, getTBox().getFreshConcept()));
		return !checkSatTree(tmp);
	}

	/** @return true if R is symmetric wrt ontology */
	private boolean checkSymmetry(DLTree R) {
		// R is symmetric iff C and \ER.\AR.(not C) is unsatisfiable
		DLTree tmp = DLTreeFactory.createSNFForall(R.copy(),
				DLTreeFactory.createSNFNot(getTBox().getFreshConcept()));
		tmp = DLTreeFactory.createSNFAnd(getTBox().getFreshConcept(),
				DLTreeFactory.createSNFExists(R, tmp));
		return !checkSatTree(tmp);
	}

	/** @return true if R is reflexive wrt ontology */
	private boolean checkReflexivity(DLTree R) {
		// R is reflexive iff C and \AR.(not C) is unsatisfiable
		DLTree tmp = DLTreeFactory.createSNFForall(R,
				DLTreeFactory.createSNFNot(getTBox().getFreshConcept()));
		tmp = DLTreeFactory.createSNFAnd(getTBox().getFreshConcept(), tmp);
		return !checkSatTree(tmp);
	}

	/** @return true if R [= S wrt ontology */
	private boolean checkRoleSubsumption(DLTree R, DLTree S) {
		if (Role.resolveRole(R).isDataRole() != Role.resolveRole(S).isDataRole()) {
			return false;
		}
		// R [= S iff \ER.C and \AS.(not C) is unsatisfiable
		DLTree tmp = DLTreeFactory.createSNFForall(S,
				DLTreeFactory.createSNFNot(getFreshFiller(S)));
		tmp = DLTreeFactory.createSNFAnd(
				DLTreeFactory.createSNFExists(R, getFreshFiller(R)), tmp);
		return !checkSatTree(tmp);
	}

	/** get access to an expression manager */
	public ExpressionManager getExpressionManager() {
		return ontology.getExpressionManager();
	}

	/** create new KB */
	private boolean newKB() {
		if (pTBox != null) {
			return true;
		}
		pTBox = new TBox(datatypeFactory, getOptions(), topORoleName, botORoleName,
				topDRoleName, botDRoleName, interrupted);
		pET = new ExpressionTranslator(pTBox);
		initCacheAndFlags();
		return false;
	}

	/** delete existed KB */
	private boolean releaseKB() {
		clearTBox();
		ontology.clear();
		return false;
	}

	/** reset current KB */
	public boolean clearKB() {
		if (pTBox == null) {
			return true;
		}
		return releaseKB() || newKB();
	}

	//	TELLS interface
	// Declaration axioms
	/** axiom declare(x) */
	public Axiom declare(Expression C) {
		return ontology.add(new AxiomDeclaration(C));
	}

	// Concept axioms
	/** axiom C [= D */
	public Axiom impliesConcepts(ConceptExpression C, ConceptExpression D) {
		return ontology.add(new AxiomConceptInclusion(C, D));
	}

	/** axiom C1 = ... = Cn */
	public Axiom equalConcepts(List<Expression> l) {
		return ontology.add(new AxiomEquivalentConcepts(l));
	}

	/** axiom C1 != ... != Cn */
	public Axiom disjointConcepts(List<Expression> l) {
		return ontology.add(new AxiomDisjointConcepts(l));
	}

	// Role axioms
	/** R = Inverse(S) */
	public Axiom setInverseRoles(ObjectRoleExpression R, ObjectRoleExpression S) {
		return ontology.add(new AxiomRoleInverse(R, S));
	}

	/** axiom (R [= S) */
	public Axiom impliesORoles(ObjectRoleComplexExpression R, ObjectRoleExpression S) {
		return ontology.add(new AxiomORoleSubsumption(R, S));
	}

	/** axiom (R [= S) */
	public Axiom impliesDRoles(DataRoleExpression R, DataRoleExpression S) {
		return ontology.add(new AxiomDRoleSubsumption(R, S));
	}

	/** axiom R1 = R2 = ... */
	public Axiom equalORoles(List<Expression> l) {
		return ontology.add(new AxiomEquivalentORoles(l));
	}

	/** axiom R1 = R2 = ... */
	public Axiom equalDRoles(List<Expression> l) {
		return ontology.add(new AxiomEquivalentDRoles(l));
	}

	/** axiom R1 != R2 != ... */
	public Axiom disjointORoles(List<Expression> l) {
		return ontology.add(new AxiomDisjointORoles(l));
	}

	/** axiom R1 != R2 != ... */
	public Axiom disjointDRoles(List<Expression> l) {
		return ontology.add(new AxiomDisjointDRoles(l));
	}

	/** Domain (R C) */
	public Axiom setODomain(ObjectRoleExpression R, ConceptExpression C) {
		return ontology.add(new AxiomORoleDomain(R, C));
	}

	/** Domain (R C) */
	public Axiom setDDomain(DataRoleExpression R, ConceptExpression C) {
		return ontology.add(new AxiomDRoleDomain(R, C));
	}

	/** Range (R C) */
	public Axiom setORange(ObjectRoleExpression R, ConceptExpression C) {
		return ontology.add(new AxiomORoleRange(R, C));
	}

	/** Range (R E) */
	public Axiom setDRange(DataRoleExpression R, DataExpression E) {
		return ontology.add(new AxiomDRoleRange(R, E));
	}

	/** Transitive (R) */
	public Axiom setTransitive(ObjectRoleExpression R) {
		return ontology.add(new AxiomRoleTransitive(R));
	}

	/** Reflexive (R) */
	public Axiom setReflexive(ObjectRoleExpression R) {
		return ontology.add(new AxiomRoleReflexive(R));
	}

	/** Irreflexive (R): Domain(R) = \neg ER.Self */
	public Axiom setIrreflexive(ObjectRoleExpression R) {
		return ontology.add(new AxiomRoleIrreflexive(R));
	}

	/** Symmetric (R): R [= R^- */
	public Axiom setSymmetric(ObjectRoleExpression R) {
		return ontology.add(new AxiomRoleSymmetric(R));
	}

	/** AntySymmetric (R): disjoint(R,R^-) */
	public Axiom setAsymmetric(ObjectRoleExpression R) {
		return ontology.add(new AxiomRoleAsymmetric(R));
	}

	/** Functional (R) */
	public Axiom setOFunctional(ObjectRoleExpression R) {
		return ontology.add(new AxiomORoleFunctional(R));
	}

	/** Functional (R) */
	public Axiom setDFunctional(DataRoleExpression R) {
		return ontology.add(new AxiomDRoleFunctional(R));
	}

	/** InverseFunctional (R) */
	public Axiom setInverseFunctional(ObjectRoleExpression R) {
		return ontology.add(new AxiomRoleInverseFunctional(R));
	}

	// Individual axioms
	/** axiom I e C */
	public Axiom instanceOf(IndividualExpression I, ConceptExpression C) {
		return ontology.add(new AxiomInstanceOf(I, C));
	}

	/** axiom <I,J>:R */
	public Axiom relatedTo(IndividualExpression I, ObjectRoleExpression R,
			IndividualExpression J) {
		return ontology.add(new AxiomRelatedTo(I, R, J));
	}

	/** axiom <I,J>:\neg R */
	public Axiom relatedToNot(IndividualExpression I, ObjectRoleExpression R,
			IndividualExpression J) {
		return ontology.add(new AxiomRelatedToNot(I, R, J));
	}

	/** axiom (value I A V) */
	public Axiom valueOf(IndividualExpression I, DataRoleExpression A, Literal<?> V) {
		return ontology.add(new AxiomValueOf(I, A, V));
	}

	/** axiom <I,V>:\neg A */
	public Axiom valueOfNot(IndividualExpression I, DataRoleExpression A, Literal<?> V) {
		return ontology.add(new AxiomValueOfNot(I, A, V));
	}

	/** same individuals */
	public Axiom processSame(List<Expression> l) {
		return ontology.add(new AxiomSameIndividuals(l));
	}

	/** different individuals */
	public Axiom processDifferent(List<Expression> l) {
		return ontology.add(new AxiomDifferentIndividuals(l));
	}

	/** let all concept expressions in the ArgQueue to be fairness constraints */
	public Axiom setFairnessConstraint(List<Expression> l) {
		return ontology.add(new AxiomFairnessConstraint(l));
	}

	/** retract an axiom */
	public void retract(Axiom axiom) {
		ontology.retract(axiom);
	}

	//* ASK part
	/*
	 * Before execution of any query the Kernel make sure that the KB is in an
	 * appropriate state: Preprocessed, Classified or Realised. If the ontology
	 * was changed between asks, incremental classification is performed and the
	 * corrected result is returned.
	 */
	/** return consistency status of KB */
	public boolean isKBConsistent() {
		if (getStatus().ordinal() <= kbLoading.ordinal()) {
			processKB(kbCChecked);
		}
		return getTBox().isConsistent();
	}

	/** ensure that KB is preprocessed/consistence checked */
	private void preprocessKB() {
		if (!isKBConsistent()) {
			throw new InconsistentOntologyException();
		}
	}

	/** ensure that KB is classified */
	public void classifyKB() {
		if (!isKBClassified()) {
			processKB(kbClassified);
		}
		if (!isKBConsistent()) {
			throw new InconsistentOntologyException();
		}
	}

	/** ensure that KB is realised */
	public void realiseKB() {
		if (!isKBRealised()) {
			processKB(kbRealised);
		}
		if (!isKBConsistent()) {
			throw new InconsistentOntologyException();
		}
	}

	// role info retrieval
	/** @return true iff object role is functional */
	public boolean isFunctional(final ObjectRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (getExpressionManager().isUniversalRole(R)) {
			return false; // universal role is not functional
		}
		if (getExpressionManager().isEmptyRole(R)) {
			return true; // empty role is functional
		}
		return getFunctionality(getRole(R, "Role expression expected in isFunctional()"));
	}

	/** @return true iff data role is functional */
	public boolean isFunctional(final DataRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (getExpressionManager().isUniversalRole(R)) {
			return false; // universal role is not functional
		}
		if (getExpressionManager().isEmptyRole(R)) {
			return true; // empty role is functional
		}
		return getFunctionality(getRole(R, "Role expression expected in isFunctional()"));
	}

	/** @return true iff role is inverse-functional */
	public boolean isInverseFunctional(final ObjectRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (getExpressionManager().isUniversalRole(R)) {
			return false; // universal role is not functional
		}
		if (getExpressionManager().isEmptyRole(R)) {
			return true; // empty role is functional
		}
		return getFunctionality(getRole(R,
				"Role expression expected in isInverseFunctional()").inverse());
	}

	/** @return true iff role is transitive */
	public boolean isTransitive(ObjectRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (getExpressionManager().isUniversalRole(R)) {
			return true; // universal role is transitive
		}
		if (getExpressionManager().isEmptyRole(R)) {
			return true; // empty role is transitive
		}
		Role r = getRole(R, "Role expression expected in isTransitive()");
		if (!r.isTransitivityKnown()) {
			r.setTransitive(checkTransitivity(e(R)));
		}
		return r.isTransitive();
	}

	/** @return true iff role is symmetric */
	public boolean isSymmetric(ObjectRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (getExpressionManager().isUniversalRole(R)) {
			return true; // universal role is symmetric
		}
		if (getExpressionManager().isEmptyRole(R)) {
			return true; // empty role is symmetric
		}
		Role r = getRole(R, "Role expression expected in isSymmetric()");
		if (!r.isSymmetryKnown()) {
			r.setSymmetric(checkSymmetry(e(R)));
		}
		return r.isSymmetric();
	}

	/** @return true iff role is asymmetric */
	public boolean isAsymmetric(ObjectRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (getExpressionManager().isUniversalRole(R)) {
			return false; // universal role is not asymmetric
		}
		if (getExpressionManager().isEmptyRole(R)) {
			return true; // empty role is symmetric
		}
		Role r = getRole(R, "Role expression expected in isAsymmetric()");
		if (!r.isAsymmetryKnown()) {
			r.setAsymmetric(getTBox().isDisjointRoles(r, r.inverse()));
		}
		return r.isAsymmetric();
	}

	/** @return true iff role is reflexive */
	public boolean isReflexive(ObjectRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (getExpressionManager().isUniversalRole(R)) {
			return true; // universal role is reflexive
		}
		if (getExpressionManager().isEmptyRole(R)) {
			return false; // empty role is not reflexive
		}
		Role r = getRole(R, "Role expression expected in isReflexive()");
		if (!r.isReflexivityKnown()) {
			r.setReflexive(checkReflexivity(e(R)));
		}
		return r.isReflexive();
	}

	/** @return true iff role is irreflexive */
	public boolean isIrreflexive(ObjectRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (getExpressionManager().isUniversalRole(R)) {
			return false; // universal role is not irreflexive
		}
		if (getExpressionManager().isEmptyRole(R)) {
			return true; // empty role is irreflexive
		}
		Role r = getRole(R, "Role expression expected in isIrreflexive()");
		if (!r.isIrreflexivityKnown()) {
			r.setIrreflexive(getTBox().isIrreflexive(r));
		}
		return r.isIrreflexive();
	}

	/** @return true iff two roles are disjoint */
	public boolean isDisjointRoles(final ObjectRoleExpression R,
			final ObjectRoleExpression S) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (getExpressionManager().isUniversalRole(R)
				|| getExpressionManager().isUniversalRole(S)) {
			return false; // universal role is not disjoint with anything
		}
		if (getExpressionManager().isEmptyRole(R)
				|| getExpressionManager().isEmptyRole(S)) {
			return true; // empty role is disjoint with everything
		}
		return getTBox().isDisjointRoles(
				getRole(R, "Role expression expected in isDisjointRoles()"),
				getRole(S, "Role expression expected in isDisjointRoles()"));
	}

	/** @return true iff two roles are disjoint */
	public boolean isDisjointRoles(final DataRoleExpression R, final DataRoleExpression S) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (getExpressionManager().isUniversalRole(R)
				|| getExpressionManager().isUniversalRole(S)) {
			return false; // universal role is not disjoint with anything
		}
		if (getExpressionManager().isEmptyRole(R)
				|| getExpressionManager().isEmptyRole(S)) {
			return true; // empty role is disjoint with everything
		}
		return getTBox().isDisjointRoles(
				getRole(R, "Role expression expected in isDisjointRoles()"),
				getRole(S, "Role expression expected in isDisjointRoles()"));
	}

	/** @return true if R is a sub-role of S */
	public boolean isSubRoles(ObjectRoleExpression R, ObjectRoleExpression S) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (getExpressionManager().isEmptyRole(R)
				|| getExpressionManager().isUniversalRole(S)) {
			return true; // \bot [= X [= \top
		}
		if (getExpressionManager().isUniversalRole(R)
				&& getExpressionManager().isEmptyRole(S)) {
			return false; // as \top [= \bot leads to inconsistent ontology
		}
		// told case first
		if (getRole(R, "Role expression expected in isSubRoles()").lesserequal(
				getRole(S, "Role expression expected in isSubRoles()"))) {
			return true;
		}
		// check the general case
		// FIXME!! cache it later
		DLTree r = e(R), s = e(S);
		return checkRoleSubsumption(r, s);
	}

	// single satisfiability
	/** @return true iff C is satisfiable */
	public boolean isSatisfiable(final ConceptExpression C) {
		preprocessKB();
		try {
			return checkSat(C);
		} catch (OWLRuntimeException crn) {
			if (C instanceof ConceptName) {
				// this is an unknown concept
				return true;
			}
			// complex expression, involving unknown names
			throw crn;
		}
	}

	/** @return true iff C [= D holds */
	public boolean isSubsumedBy(final ConceptExpression C, final ConceptExpression D) {
		preprocessKB();
		return checkSub(C, D);
	}

	/** @return true iff C is disjoint with D; that is, C [= \not D holds */
	public boolean isDisjoint(final ConceptExpression C, final ConceptExpression D) {
		preprocessKB();
		return checkSub(C, getExpressionManager().not(D));
	}

	/** @return true iff C is equivalent to D */
	public boolean isEquivalent(final ConceptExpression C, final ConceptExpression D) {
		preprocessKB();
		return isSubsumedBy(C, D) && isSubsumedBy(D, C);
	}

	// concept hierarchy
	/** apply actor__apply() to all DIRECT super-concepts of [complex] C */
	public void getSupConcepts(final ConceptExpression C, boolean direct, Actor actor) {
		classifyKB(); // ensure KB is ready to answer the query
		setUpCache(C, csClassified);
		Taxonomy tax = getCTaxonomy();
		if (direct) {
			tax.getRelativesInfo(cachedVertex, actor, false, true, true);
		} else {
			tax.getRelativesInfo(cachedVertex, actor, false, false, true);
		}
	}

	/** apply actor__apply() to all DIRECT sub-concepts of [complex] C */
	public void getSubConcepts(final ConceptExpression C, boolean direct, Actor actor) {
		classifyKB(); // ensure KB is ready to answer the query
		setUpCache(C, csClassified);
		Taxonomy tax = getCTaxonomy();
		tax.getRelativesInfo(cachedVertex, actor, false, direct, false);
	}

	/** apply actor__apply() to all synonyms of [complex] C */
	public void getEquivalentConcepts(final ConceptExpression C, Actor actor) {
		classifyKB(); // ensure KB is ready to answer the query
		setUpCache(C, csClassified);
		actor.apply(cachedVertex);
	}

	/// apply actor::apply() to all named concepts disjoint with [complex] C
	public void getDisjointConcepts(ConceptExpression C, Actor actor) {
		classifyKB(); // ensure KB is ready to answer the query
		setUpCache(getExpressionManager().not(C), csClassified);
		Taxonomy tax = getCTaxonomy();
		// we are looking for all sub-concepts of (not C) (including synonyms to it)
		tax.getRelativesInfo(cachedVertex, actor, true, false, false);
	}

	// role hierarchy
	/** apply actor__apply() to all DIRECT super-roles of [complex] R */
	public void getSupRoles(final RoleExpression r, boolean direct, Actor actor) {
		preprocessKB(); // ensure KB is ready to answer the query
		Role R = getRole(r, "Role expression expected in getSupRoles()");
		Taxonomy tax = getTaxonomy(R);
		tax.getRelativesInfo(getTaxVertex(R), actor, false, direct, true);
	}

	/** apply actor__apply() to all DIRECT sub-roles of [complex] R */
	public void getSubRoles(final RoleExpression r, boolean direct, Actor actor) {
		preprocessKB(); // ensure KB is ready to answer the query
		Role R = getRole(r, "Role expression expected in getSubRoles()");
		Taxonomy tax = getTaxonomy(R);
		tax.getRelativesInfo(getTaxVertex(R), actor, false, direct, false);
	}

	/** apply actor__apply() to all synonyms of [complex] R */
	public void getEquivalentRoles(final RoleExpression r, Actor actor) {
		preprocessKB(); // ensure KB is ready to answer the query
		Role R = getRole(r, "Role expression expected in getEquivalentRoles()");
		actor.apply(getTaxVertex(R));
	}

	// domain and range as a set of named concepts
	/**
	 * apply actor__apply() to all DIRECT NC that are in the domain of [complex]
	 * R
	 */
	public void getORoleDomain(final ObjectRoleExpression r, boolean direct, Actor actor) {
		classifyKB(); // ensure KB is ready to answer the query
		setUpCache(getExpressionManager().exists(r, getExpressionManager().top()),
				csClassified);
		Taxonomy tax = getCTaxonomy();
		tax.getRelativesInfo(cachedVertex, actor, true, direct, true);
	}

	/// apply actor::apply() to all DIRECT NC that are in the domain of data role R
	//template<class Actor>
	void getDRoleDomain(DataRoleExpression r, boolean direct, Actor actor) {
		classifyKB(); // ensure KB is ready to answer the query
		setUpCache(getExpressionManager().exists(r, getExpressionManager().dataTop()),
				csClassified);
		Taxonomy tax = getCTaxonomy();
		if (direct) {
			tax.getRelativesInfo(cachedVertex, actor, true, true, true);
		} else {
			// gets all named classes that are in the domain of a role
			tax.getRelativesInfo(cachedVertex, actor, true, false, true);
		}
	}

	/**
	 * apply actor__apply() to all DIRECT NC that are in the range of [complex]
	 * R
	 */
	public void getRoleRange(final ObjectRoleExpression r, boolean direct, Actor actor) {
		getORoleDomain(getExpressionManager().inverse(r), direct, actor);
	}

	// instances
	public void getInstances(final ConceptExpression C, Actor actor, boolean direct) {
		if (direct) {
			getDirectInstances(C, actor);
		} else {
			getInstances(C, actor);
		}
	}

	/** apply actor__apply() to all direct instances of given [complex] C */
	public void getDirectInstances(final ConceptExpression C, Actor actor) {
		realiseKB(); // ensure KB is ready to answer the query
		setUpCache(C, csClassified);
		// implement 1-level check by hand
		// if the root vertex contains individuals -- we are done
		if (actor.apply(cachedVertex)) {
			return;
		}
		// if not, just go 1 level down and apply the actor regardless of what's found
		// FIXME!! check again after bucket-method will be implemented
		for (TaxonomyVertex p : cachedVertex.neigh(/* upDirection= */false)) {
			actor.apply(p);
		}
	}

	/** apply actor__apply() to all instances of given [complex] C */
	public void getInstances(final ConceptExpression C, Actor actor) { // FIXME!! check for Racer's/IS approach
		realiseKB(); // ensure KB is ready to answer the query
		setUpCache(C, csClassified);
		Taxonomy tax = getCTaxonomy();
		tax.getRelativesInfo(cachedVertex, actor, true, false, false);
	}

	/**
	 * apply actor__apply() to all DIRECT concepts that are types of an
	 * individual I
	 */
	public void getTypes(final IndividualExpression I, boolean direct, Actor actor) {
		realiseKB(); // ensure KB is ready to answer the query
		setUpCache(getExpressionManager().oneOf(I), csClassified);
		Taxonomy tax = getCTaxonomy();
		tax.getRelativesInfo(cachedVertex, actor, true, direct, true);
	}

	/** apply actor__apply() to all synonyms of an individual I */
	public void getSameAs(final IndividualExpression I, Actor actor) {
		realiseKB(); // ensure KB is ready to answer the query
		getEquivalentConcepts(getExpressionManager().oneOf(I), actor);
	}

	/** @return true iff I and J refer to the same individual */
	public boolean isSameIndividuals(final IndividualExpression I,
			final IndividualExpression J) {
		realiseKB();
		Individual i = getIndividual(I,
				"Only known individuals are allowed in the isSameAs()");
		Individual j = getIndividual(J,
				"Only known individuals are allowed in the isSameAs()");
		return getTBox().isSameIndividuals(i, j);
	}

	//----------------------------------------------------------------------------------
	// knowledge exploration queries
	//----------------------------------------------------------------------------------
	/// build a completion tree for a concept expression C (no caching as it breaks the idea of KE). @return the root node
	DlCompletionTree buildCompletionTree(ConceptExpression C) {
		preprocessKB();
		setUpCache(C, csSat);
		return getTBox().buildCompletionTree(cachedConcept);
	}

	/** @return true iff individual I is instance of given [complex] C */
	public boolean isInstance(final IndividualExpression I, final ConceptExpression C) {
		realiseKB(); // ensure KB is ready to answer the query
		getIndividual(I, "individual name expected in the isInstance()");
		return isSubsumedBy(getExpressionManager().oneOf(I), C);
	}

	public ReasoningKernel(JFactReasonerConfiguration conf, DatatypeFactory factory) {
		D2I = null;
		cachedQuery = null;
		cachedQueryTree = null;
		kernelOptions = conf;
		datatypeFactory = factory;
		pTBox = null;
		pET = null;
		cachedQuery = null;
		initCacheAndFlags();
	}

	/// try to perform the incremental reasoning on the changed ontology
	private boolean tryIncremental() {
		if (pTBox == null) {
			return true;
		}
		if (!ontology.isChanged()) {
			return false;
		}
		return true;
	}

	/// force the re-classification of the changed ontology
	private void forceReload() {
		clearTBox();
		newKB();
		// Protege (as the only user of non-trivial monitors with reload) does not accept multiple usage of a monitor
		// so switch it off after the 1st usage
		if (kernelOptions.isUseELReasoner()) {
			ELFAxiomChecker ac = new ELFAxiomChecker();
			ac.visitOntology(ontology);
			if (ac.value()) {
				ELFNormalizer normalizer = new ELFNormalizer(getExpressionManager());
				normalizer.visitOntology(ontology);
				ELFReasoner reasoner = new ELFReasoner(ontology);
				reasoner.classify();
			}
		}
		// split ontological axioms
		if (kernelOptions.isSplits()) {
			TAxiomSplitter AxiomSplitter = new TAxiomSplitter(ontology);
			AxiomSplitter.buildSplit();
		}
		OntologyLoader OntologyLoader = new OntologyLoader(getTBox());
		OntologyLoader.visitOntology(ontology);
		ontology.setProcessed();
	}

	private void processKB(KBStatus status) {
		assert status.ordinal() >= kbCChecked.ordinal();
		// check whether reasoning was failed
		if (reasoningFailed) {
			throw new ReasonerInternalException(
					"Can't classify KB because of previous errors");
		}
		// check if something have to be done
		if (getStatus().ordinal() >= status.ordinal()) { // nothing to do; but make sure that we are consistent
			if (!isKBConsistent()) {
				throw new InconsistentOntologyException();
			}
			return;
		}
		// here we have to do something: let's decide what to do
		boolean stillGo = true;
		switch (getStatus()) {
			case kbEmpty:
			case kbLoading:
				break; // need to do the whole cycle -- just after the switch
			case kbCChecked: {
				classify(status);
				stillGo = false;
				break; // do classification
			}
			case kbClassified: {
				realise();
				stillGo = false;
				break;
			} // do realisation
			default: // nothing should be here
				throw new UnreachableSituationException();
		}
		if (stillGo) {
			// start with loading and preprocessing -- here might be a failures
			reasoningFailed = true;
			// load the axioms from the ontology to the TBox
			if (tryIncremental()) {
				forceReload();
			}
			// do the consistency check
			pTBox.isConsistent();
			// if there were no exception thrown -- clear the failure status
			reasoningFailed = false;
			if (status == kbCChecked) {
				return;
			}
			classify(status);
		}
	}

	// do classification
	private void classify(KBStatus status) {
		// don't do classification twice
		if (status != kbRealised) {
			//goto Realise;
			if (!pTBox.isConsistent()) {
				return;
			}
			pTBox.performClassification();
			return;
		}
		realise();
	}

	// do realisation
	private void realise() {
		if (!pTBox.isConsistent()) {
			return;
		}
		pTBox.performRealisation();
	}

	//-----------------------------------------------------------------------------
	//--		query caching support
	//-----------------------------------------------------------------------------
	/// classify query; cache is ready at the point. NAMED means whether concept is just a name
	void classifyQuery(boolean named) {
		// make sure KB is classified
		classifyKB();
		if (!named) {
			getTBox().classifyQueryConcept();
		}
		cachedVertex = cachedConcept.getTaxVertex();
		if (cachedVertex == null) {
			cachedVertex = getCTaxonomy().getFreshVertex(cachedConcept);
		}
	}

	void setUpCache(DLTree query, CacheStatus level) {
		// if KB was changed since it was classified,
		// we should catch it before
		assert !ontology.isChanged();
		// check if the query is already cached
		if (checkQueryCache(query)) { // ... with the same level -- nothing to do
										//deleteTree(query);
										//query=null;
			if (level.ordinal() <= cacheLevel.ordinal()) {
				return;
			} else { // concept was defined but not classified yet
				assert level == csClassified && cacheLevel != csClassified;
				if (cacheLevel == csSat) // already check satisfiability
				{
					classifyQuery(cachedQueryTree.isCN());
					return;
				}
			}
		} else {
			// change current query
			setQueryCache(query);
		}
		// clean cached info
		cachedVertex = null;
		cacheLevel = level;
		// check if concept-to-cache is defined in ontology
		if (cachedQueryTree.isCN()) {
			cachedConcept = getTBox().getCI(cachedQueryTree);
		} else // case of complex query
		{
			getTBox().clearQueryConcept();
			cachedConcept = getTBox().createQueryConcept(cachedQueryTree);
		}
		assert cachedConcept != null;
		// preprocess concept is necessary (fresh concept in query or complex one)
		if (cachedConcept.getpName() == 0) {
			getTBox().preprocessQueryConcept(cachedConcept);
		}
		if (level == csClassified) {
			classifyQuery(cachedQueryTree.isCN());
		}
	}

	void setUpCache(ConceptExpression query, CacheStatus level) {
		// if KB was changed since it was classified,
		// we should catch it before
		assert !ontology.isChanged();
		// check if the query is already cached
		if (checkQueryCache(query)) { // ... with the same level -- nothing to do
			if (level.ordinal() <= cacheLevel.ordinal()) {
				return;
			} else { // concept was defined but not classified yet
				assert level == csClassified && cacheLevel != csClassified;
				if (cacheLevel == csSat) // already check satisfiability
				{
					classifyQuery(isNameOrConst(cachedQuery));
					return;
				}
			}
		} else {
			// change current query
			setQueryCache(query);
		}
		// clean cached info
		cachedVertex = null;
		cacheLevel = level;
		// check if concept-to-cache is defined in ontology
		if (isNameOrConst(cachedQuery)) {
			cachedConcept = getTBox().getCI(e(cachedQuery));
		} else // case of complex query
		{
			// need to clear the query before transform it into DLTree
			getTBox().clearQueryConcept();
			// ... as if fresh names appears there, they would be cleaned up
			cachedConcept = getTBox().createQueryConcept(e(cachedQuery));
		}
		assert cachedConcept != null;
		// preprocess concept is necessary (fresh concept in query or complex one)
		if (cachedConcept.getpName() == 0) {
			getTBox().preprocessQueryConcept(cachedConcept);
		}
		if (level == csClassified) {
			classifyQuery(isNameOrConst(cachedQuery));
		}
	}

	//----------------------------------------------------------------------------------
	// knowledge exploration queries
	//----------------------------------------------------------------------------------
	/// build the set of data neighbours of a NODE, put the set into the RESULT variable
	void getDataRoles(DlCompletionTree node, Set<RoleExpression> Result, boolean onlyDet) {
		Result.clear();
		for (DlCompletionTreeArc p : node.getNeighbour()) {
			if (!p.isIBlocked() && p.getArcEnd().isDataNode()
					&& (!onlyDet || p.getDep().isEmpty())) {
				// FIXME!! add also all supers
				Result.add(getExpressionManager().dataRole(p.getRole().getName()));
			}
		}
	}

	/// build the set of object neighbours of a NODE; incoming edges are counted iff NEEDINCOMING is true
	void getObjectRoles(DlCompletionTree node, Set<RoleExpression> Result,
			boolean onlyDet, boolean needIncoming) {
		Result.clear();
		for (DlCompletionTreeArc p : node.getNeighbour()) {
			if (!p.isIBlocked() && !p.getArcEnd().isDataNode()
					&& (!onlyDet || p.getDep().isEmpty())
					&& (needIncoming || p.isSuccEdge())) {
				Result.add(getExpressionManager().objectRole(p.getRole().getName()));
			}
		}
	}

	/// build the set of neighbours of a NODE via role ROLE; put the resulting list into RESULT
	void getNeighbours(DlCompletionTree node, RoleExpression role,
			List<DlCompletionTree> Result) {
		Result.clear();
		Role R = getRole(role, "Role expression expected in getNeighbours() method");
		for (DlCompletionTreeArc p : node.getNeighbour()) {
			if (!p.isIBlocked() && p.isNeighbour(R)) {
				Result.add(p.getArcEnd());
			}
		}
	}

	/// put into RESULT all the data expressions from the NODE label
	void getLabel(DlCompletionTree node, List<Expression> Result, boolean onlyDet) {
		// prepare D2I translator
		if (D2I == null) {
			D2I = new TDag2Interface(getTBox().getDag(), getExpressionManager());
		} else {
			D2I.ensureDagSize();
		}
		boolean data = node.isDataNode();
		Result.clear();
		for (ConceptWDep p : node.beginl_sc()) {
			if (!onlyDet || p.getDep().isEmpty()) {
				Result.add(D2I.getExpr(p.getConcept(), data));
			}
		}
		for (ConceptWDep p : node.beginl_cc()) {
			if (!onlyDet || p.getDep().isEmpty()) {
				Result.add(D2I.getExpr(p.getConcept(), data));
			}
		}
	}

	// classification only needed for complex expression
	//	private void needClassify(CacheStatus level) {
	//		if (level == csClassified) {
	//			classifyKB();
	//			getTBox().classifyQueryConcept();
	//			// cached concept now have to be classified
	//			assert cachedConcept.isClassified();
	//			cachedVertex = cachedConcept.getTaxVertex();
	//		}
	//	}
	//	private void needSetup(CacheStatus level, DLTree query) {
	//		// clean cached info
	//		cachedVertex = null;
	//		// check if concept-to-cache is defined in ontology
	//		if (query.isCN()) {
	//			cachedConcept = getTBox().getCI(query);
	//			// undefined/non-classified concept -- need to reclassify
	//			if (cachedConcept == null) {
	//				// invalidate cache
	//				cacheLevel = csEmpty;
	//				// FIXME!! reclassification
	//				throw new ReasonerInternalException(
	//						"FaCT++ Kernel: incremental classification not supported");
	//			}
	//			cacheLevel = level;
	//			if (level == csClassified) // need to set the pointers
	//			{
	//				classifyKB();
	//				//assert ( cachedConcept.isClassified() );
	//				cachedVertex = cachedConcept.getTaxVertex();
	//			}
	//			return;
	//		}
	//		// we are preprocessed here
	//		// case of complex query
	//		cachedConcept = getTBox().createQueryConcept(query);
	//		cacheLevel = level;
	//		needClassify(level);
	//	}
	/**
	 * @return true iff the chain contained in the arg-list is a sub-property of
	 *         R
	 */
	private boolean checkSubChain(Role R, List<Expression> l) {
		// retrieve a role chain
		// R1 o ... o Rn [= R iff \ER1.\ER2....\ERn.(notC) and AR.C is unsatisfiable
		DLTree tmp = DLTreeFactory.createSNFNot(getTBox().getFreshConcept());
		for (int i = l.size() - 1; i > -1; i--) {
			Expression p = l.get(i);
			if (!(p instanceof ObjectRoleExpression)) {
				throw new ReasonerInternalException(
						"Role expression expected in the role chain construct");
			}
			ObjectRoleExpression Ri = (ObjectRoleExpression) p;
			tmp = DLTreeFactory.createSNFExists(e(Ri), tmp);
		}
		tmp = DLTreeFactory.createSNFAnd(tmp, DLTreeFactory.createSNFForall(DLTreeFactory
				.buildTree(new Lexeme(Token.RNAME, R)), getTBox().getFreshConcept()));
		return !checkSatTree(tmp);
	}

	/** @return true if R is a super-role of a chain holding in the args */
	public boolean isSubChain(ObjectRoleExpression R, List<Expression> l) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (getExpressionManager().isUniversalRole(R)) {
			return true; // universal role is a super of any chain
		}
		if (getExpressionManager().isEmptyRole(R)) {
			return false; // empty role is not a super of any chain
		}
		return checkSubChain(getRole(R, "Role expression expected in isSubChain()"), l);
	}

	/** @return true if R is a sub-role of S */
	public boolean isSubRoles(DataRoleExpression R, DataRoleExpression S) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (getExpressionManager().isEmptyRole(R)
				|| getExpressionManager().isUniversalRole(S)) {
			return true; // \bot [= X [= \top
		}
		if (getExpressionManager().isUniversalRole(R)
				&& getExpressionManager().isEmptyRole(S)) {
			return false; // as \top [= \bot leads to inconsistent ontology
		}
		// told case first
		if (getRole(R, "Role expression expected in isSubRoles()").lesserequal(
				getRole(S, "Role expression expected in isSubRoles()"))) {
			return true;
		}
		// check the general case
		// FIXME!! cache it later
		DLTree r = e(R), s = e(S);
		return checkRoleSubsumption(r, s);
	}

	// all-disjoint query implementation
	public boolean isDisjointRoles(List<Expression> l) {
		// grab all roles from the arg-list
		//List<TDLExpression> Disj = getExpressionManager().getArgList();
		List<Role> Roles = new ArrayList<Role>(l.size());
		for (Expression p : l) {
			if (p instanceof ObjectRoleExpression) {
				ObjectRoleExpression ORole = (ObjectRoleExpression) p;
				if (getExpressionManager().isUniversalRole(ORole)) {
					return false; // universal role is not disjoint with anything
				}
				if (getExpressionManager().isEmptyRole(ORole)) {
					continue; // empty role is disjoint with everything
				}
				Roles.add(getRole(ORole, "Role expression expected in isDisjointRoles()"));
			} else {
				if (!(p instanceof DataRoleExpression)) {
					throw new ReasonerInternalException(
							"Role expression expected in isDisjointRoles()");
				}
				DataRoleExpression DRole = (DataRoleExpression) p;
				if (getExpressionManager().isUniversalRole(DRole)) {
					return false; // universal role is not disjoint with anything
				}
				if (getExpressionManager().isEmptyRole(DRole)) {
					continue; // empty role is disjoint with everything
				}
				Roles.add(getRole(DRole, "Role expression expected in isDisjointRoles()"));
			}
		}
		// test pair-wise disjointness
		for (int i = 0; i < Roles.size() - 1; i++) {
			for (int j = i + 1; j < Roles.size(); j++) {
				if (!getTBox().isDisjointRoles(Roles.get(i), Roles.get(j))) {
					return false;
				}
			}
		}
		return true;
	}

	private List<Individual> buildRelatedCache(Individual I, final Role R) {
		if (R.isSynonym()) {
			return getRelated(I, ClassifiableEntry.resolveSynonym(R));
		}
		if (R.isDataRole() || R.isBottom()) {
			return new ArrayList<Individual>();
		}
		RIActor actor = new RIActor();
		ObjectRoleExpression InvR = R.getId() > 0 ? getExpressionManager().inverse(
				getExpressionManager().objectRole(R.getName())) : getExpressionManager()
				.objectRole(R.inverse().getName());
		ConceptExpression query;
		if (R.isTop()) {
			query = getExpressionManager().top();
		} else {
			query = getExpressionManager().value(InvR,
					getExpressionManager().individual(I.getName()));
		}
		getInstances(query, actor);
		return actor.getAcc();
	}

	public void getRoleFillers(final IndividualExpression I,
			final ObjectRoleExpression R, List<NamedEntry> Result) {
		realiseKB();
		List<Individual> vec = getRelated(
				getIndividual(I, "Individual name expected in the getRoleFillers()"),
				getRole(R, "Role expression expected in the getRoleFillers()"));
		for (Individual p : vec) {
			Result.add(p);
		}
	}

	public boolean isRelated(final IndividualExpression I, final ObjectRoleExpression R,
			final IndividualExpression J) {
		realiseKB();
		Individual i = getIndividual(I, "Individual name expected in the isRelated()");
		Role r = getRole(R, "Role expression expected in the isRelated()");
		if (r.isDataRole()) {
			return false;
		}
		Individual j = getIndividual(J, "Individual name expected in the isRelated()");
		List<Individual> vec = getRelated(i, r);
		for (Individual p : vec) {
			if (j.equals(p)) {
				return true;
			}
		}
		return false;
	}
}

enum CacheStatus {
	csEmpty, csSat, csClassified
}