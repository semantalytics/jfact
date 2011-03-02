package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import static uk.ac.manchester.cs.jfact.helpers.DLTree.equalTrees;
import static uk.ac.manchester.cs.jfact.kernel.CacheStatus.*;
import static uk.ac.manchester.cs.jfact.kernel.KBStatus.*;
import static uk.ac.manchester.cs.jfact.kernel.TExpressionManager.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

import uk.ac.manchester.cs.jfact.helpers.DLTree;
import uk.ac.manchester.cs.jfact.helpers.DLTreeFactory;
import uk.ac.manchester.cs.jfact.helpers.UnreachableSituationException;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.kernel.actors.Actor;
import uk.ac.manchester.cs.jfact.kernel.actors.RIActor;
import uk.ac.manchester.cs.jfact.kernel.actors.SupConceptActor;
import uk.ac.manchester.cs.jfact.kernel.datatype.TDLDataValue;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptName;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomConceptInclusion;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDRoleDomain;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDRoleFunctional;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDRoleRange;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDRoleSubsumption;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDeclaration;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDifferentIndividuals;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDisjointConcepts;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDisjointDRoles;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDisjointORoles;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDisjointUnion;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomEquivalentConcepts;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomEquivalentDRoles;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomEquivalentORoles;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomFairnessConstraint;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomInstanceOf;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomORoleDomain;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomORoleFunctional;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomORoleRange;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomORoleSubsumption;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRelatedTo;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRelatedToNot;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRoleAsymmetric;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRoleInverse;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRoleInverseFunctional;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRoleIrreflexive;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRoleReflexive;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRoleSymmetric;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRoleTransitive;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomSameIndividuals;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomValueOf;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomValueOfNot;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLAxiom;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLConceptExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLDataExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLDataRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLIndividualExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLObjectRoleComplexExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLObjectRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLRoleExpression;

public final  class ReasoningKernel {
	/** options for the kernel and all related substructures */
	private final IFOptionSet KernelOptions = new IFOptionSet();
	/** local TBox (to be created) */
	private TBox pTBox;
	/** set of axioms */
	private final TOntology Ontology = new TOntology();
	/** expression translator to work with queries */
	private TExpressionTranslator pET;
	// Top/Bottom role names: if set, they will appear in all hierarchy-related output
	/** top object role name */
	private String TopORoleName;
	/** bottom object role name */
	private String BotORoleName;
	/** top data role name */
	private String TopDRoleName;
	/** bottom data role name */
	private String BotDRoleName;
	// values to propagate to the new KB in case of clearance
	/** progress monitor (if any) */
	private ReasonerProgressMonitor pMonitor;
	private AtomicBoolean interrupted;

	public void setInterruptedSwitch(AtomicBoolean b) {
		interrupted = b;
	}

	/** timeout value */
	private long OpTimeout;
	/** tell reasoner to use verbose output */
	private boolean verboseOutput;
	// reasoning cache
	/** cache level */
	private CacheStatus cacheLevel;
	/** cached query concept description */
	private DLTree cachedQuery;
	/** cached concept (either defConcept or existing one) */
	private TConcept cachedConcept;
	/** cached query result (taxonomy position) */
	private TaxonomyVertex cachedVertex;
	// internal flags
	/** set if TBox throws an exception during preprocessing/classification */
	private boolean reasoningFailed;
	/** trace vector for the last operation (set from the TBox trace-sets) */
	private final List<TDLAxiom> TraceVec = new ArrayList<TDLAxiom>();
	/** flag to gather trace information for the next reasoner's call */
	private boolean NeedTracing;

	/** get status of the KB */
	private KBStatus getStatus() {
		if (pTBox == null) {
			return kbEmpty;
		}
		// if the ontology is changed, it needs to be reclassified
		if (Ontology.isChanged()) {
			return kbLoading;
		}
		return pTBox.getStatus();
	}

	/** get DLTree corresponding to an expression EXPR */
	private DLTree e(final TDLExpression expr) {
		return expr.accept(pET);
	}

	/** clear cache and flags */
	private void initCacheAndFlags() {
		cacheLevel = csEmpty;
		cachedQuery = null;
		cachedConcept = null;
		cachedVertex = null;
		reasoningFailed = false;
		NeedTracing = false;
	}

	public void needTracing() {
		NeedTracing = true;
	}

	/** @return the trace-set of the last reasoning operation */
	public List<TDLAxiom> getTrace() {
		List<TDLAxiom> toReturn = new ArrayList<TDLAxiom>(TraceVec);
		TraceVec.clear();
		return toReturn;
	}

	/** axiom C = C1 or ... or Cn; C1 != ... != Cn */
	public TDLAxiom disjointUnion(TDLConceptExpression C, List<TDLExpression> l) {
		return Ontology.add(new TDLAxiomDisjointUnion(C, l));
	}

	/** get related cache for an individual I */
	private List<TIndividual> getRelated(TIndividual I, final TRole R) {
		if (!I.hasRelatedCache(R)) {
			I.setRelatedCache(R, buildRelatedCache(I, R));
		}
		return I.getRelatedCache(R);
	}

	/** @return true iff C is satisfiable */
	private boolean checkSat(DLTree C) {
		if (C.isCN()) {
			return getTBox().isSatisfiable(getTBox().getCI(C));
		}
		setUpCache(C, csSat);
		return getTBox().isSatisfiable(cachedConcept);
	}

	/** @return true iff C [= D holds */
	private boolean checkSub(TConcept C, TConcept D) {
		if (getStatus().ordinal() < kbClassified.ordinal()) {
			return getTBox().isSubHolds(C, D);
		}
		// classified => do the taxonomy traversal
		SupConceptActor actor = new SupConceptActor(D);
		Taxonomy tax = getCTaxonomy();
		//TODO this appears to be a use of exceptions as a means of breaking through a cycle
		try {
			tax.getRelativesInfo(C.getTaxVertex(), actor, true, false, true);
			return false;
		} catch (RuntimeException e) {
			//e.printStackTrace();
			tax.clearCheckedLabel();
			return true;
		}
	}

	/** @return true iff C [= D holds */
	private boolean checkSub(DLTree C, DLTree D) {
		if (C.isCN() && D.isCN()) {
			return checkSub(getTBox().getCI(C), getTBox().getCI(D));
		}
		return !checkSat(DLTreeFactory.createSNFAnd(C,
				DLTreeFactory.createSNFNot(D)));
	}

	// get access to internal structures
	/** @throw an exception if no TBox found */
	private void checkTBox() {
		if (pTBox == null) {
			throw new ReasonerInternalException(
					"FaCT++ Kernel: KB Not Initialised");
		}
	}

	/** get RW access to TBox */
	private TBox getTBox() {
		checkTBox();
		return pTBox;
	}

	/** clear TBox and related structures; keep ontology in place */
	private void clearTBox() {
		//		delete pTBox;
		pTBox = null;
		//	delete pET;
		pET = null;
		//deleteTree(cachedQuery);
		cachedQuery = null;
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
	private TIndividual getIndividual(final TDLIndividualExpression i,
			final String reason) {
		DLTree I = e(i);
		if (I == null) {
			throw new ReasonerInternalException(reason);
		}
		return (TIndividual) getTBox().getCI(I);
	}

	/** get role by the TRoleExpr */
	private TRole getRole(final TDLRoleExpression r, final String reason) {
		return TRole.resolveRole(e(r));
	}

	/** get taxonomy of the property wrt it's name */
	private Taxonomy getTaxonomy(TRole R) {
		return R.isDataRole() ? getDRTaxonomy() : getORTaxonomy();
	}

	/** get taxonomy vertext of the property wrt it's name */
	private TaxonomyVertex getTaxVertex(TRole R) {
		return R.getTaxVertex();
	}

	private IFOptionSet getOptions() {
		return KernelOptions;
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

	/** set Progress monitor to control the classification process */
	public void setProgressMonitor(ReasonerProgressMonitor pMon) {
		pMonitor = pMon;
		if (pTBox != null) {
			pTBox.setProgressMonitor(pMon);
		}
	}

	/** set verbose output (ie, concept and role taxonomies) wrt given VALUE */
	public void setVerboseOutput(boolean value) {
		verboseOutput = value;
		if (pTBox != null) {
			pTBox.setVerboseOutput(value);
		}
	}

	/** set top/bottom role names to use them in the related output */
	public void setTopBottomRoleNames(final String topORoleName,
			final String botORoleName, final String topDRoleName,
			final String botDRoleName) {
		TopORoleName = topORoleName;
		BotORoleName = botORoleName;
		TopDRoleName = topDRoleName;
		BotDRoleName = botDRoleName;
	}

	/**
	 * dump query processing TIME, reasoning statistics and a (preprocessed)
	 * TBox
	 */
	public void writeReasoningResult(LogAdapter o, long time) {
		getTBox().writeReasoningResult(o, time);
	}

	/** set timeout value to VALUE */
	public void setOperationTimeout(long value) {
		OpTimeout = value;
		if (pTBox != null) {
			pTBox.setTestTimeout(value);
		}
	}

	// helper methods to query properties of roles
	/** @return true if R is functional wrt ontology */
	private boolean checkFunctionality(DLTree R) {
		// R is transitive iff \ER.C and \ER.\not C is unsatisfiable
		DLTree tmp = DLTreeFactory.createSNFExists(R.copy(),
				DLTreeFactory.createSNFNot(getTBox().getFreshConcept()));
		tmp = DLTreeFactory.createSNFAnd(tmp,
				DLTreeFactory.createSNFExists(R, getTBox().getFreshConcept()));
		return !checkSat(tmp);
	}

	/** @return true if R is functional; set the value for R if necessary */
	private boolean getFunctionality(TRole R) {
		if (!R.isFunctionalityKnown()) {
			R.setFunctional(checkFunctionality(DLTreeFactory
					.buildTree(new TLexeme(R.isDataRole() ? Token.DNAME
							: Token.RNAME, R))));
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
		return !checkSat(tmp);
	}

	/** @return true if R is symmetric wrt ontology */
	private boolean checkSymmetry(DLTree R) {
		// R is symmetric iff C and \ER.\AR.(not C) is unsatisfiable
		DLTree tmp = DLTreeFactory.createSNFForall(R.copy(),
				DLTreeFactory.createSNFNot(getTBox().getFreshConcept()));
		tmp = DLTreeFactory.createSNFAnd(getTBox().getFreshConcept(),
				DLTreeFactory.createSNFExists(R, tmp));
		return !checkSat(tmp);
	}

	/** @return true if R is reflexive wrt ontology */
	private boolean checkReflexivity(DLTree R) {
		// R is reflexive iff C and \AR.(not C) is unsatisfiable
		DLTree tmp = DLTreeFactory.createSNFForall(R,
				DLTreeFactory.createSNFNot(getTBox().getFreshConcept()));
		tmp = DLTreeFactory.createSNFAnd(getTBox().getFreshConcept(), tmp);
		return !checkSat(tmp);
	}

	/** @return true if R [= S wrt ontology */
	private boolean checkRoleSubsumption(DLTree R, DLTree S) {
		// R [= S iff \ER.C and \AS.(not C) is unsatisfiable
		DLTree tmp = DLTreeFactory.createSNFForall(S,
				DLTreeFactory.createSNFNot(getTBox().getFreshConcept()));
		tmp = DLTreeFactory.createSNFAnd(
				DLTreeFactory.createSNFExists(R, getTBox().getFreshConcept()),
				tmp);
		return !checkSat(tmp);
	}

	/** get access to an expression manager */
	public TExpressionManager getExpressionManager() {
		return Ontology.getExpressionManager();
	}

	/** create new KB */
	private boolean newKB() {
		if (pTBox != null) {
			return true;
		}
		pTBox = new TBox(getOptions(), TopORoleName, BotORoleName,
				TopDRoleName, BotDRoleName, interrupted);
		pTBox.setTestTimeout(OpTimeout);
		pTBox.setProgressMonitor(pMonitor);
		pTBox.setVerboseOutput(verboseOutput);
		pET = new TExpressionTranslator(pTBox);
		initCacheAndFlags();
		return false;
	}

	/** delete existed KB */
	private boolean releaseKB() {
		clearTBox();
		Ontology.clear();
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
	public TDLAxiom declare(TDLExpression C) {
		return Ontology.add(new TDLAxiomDeclaration(C));
	}

	// Concept axioms
	/** axiom C [= D */
	public TDLAxiom impliesConcepts(TDLConceptExpression C,
			TDLConceptExpression D) {
		return Ontology.add(new TDLAxiomConceptInclusion(C, D));
	}

	/** axiom C1 = ... = Cn */
	public TDLAxiom equalConcepts(List<TDLExpression> l) {
		return Ontology.add(new TDLAxiomEquivalentConcepts(l));
	}

	/** axiom C1 != ... != Cn */
	public TDLAxiom disjointConcepts(List<TDLExpression> l) {
		return Ontology.add(new TDLAxiomDisjointConcepts(l));
	}

	// Role axioms
	/** R = Inverse(S) */
	public TDLAxiom setInverseRoles(TDLObjectRoleExpression R,
			TDLObjectRoleExpression S) {
		return Ontology.add(new TDLAxiomRoleInverse(R, S));
	}

	/** axiom (R [= S) */
	public TDLAxiom impliesORoles(TDLObjectRoleComplexExpression R,
			TDLObjectRoleExpression S) {
		return Ontology.add(new TDLAxiomORoleSubsumption(R, S));
	}

	/** axiom (R [= S) */
	public TDLAxiom impliesDRoles(TDLDataRoleExpression R,
			TDLDataRoleExpression S) {
		return Ontology.add(new TDLAxiomDRoleSubsumption(R, S));
	}

	/** axiom R1 = R2 = ... */
	public TDLAxiom equalORoles(List<TDLExpression> l) {
		return Ontology.add(new TDLAxiomEquivalentORoles(l));
	}

	/** axiom R1 = R2 = ... */
	public TDLAxiom equalDRoles(List<TDLExpression> l) {
		return Ontology.add(new TDLAxiomEquivalentDRoles(l));
	}

	/** axiom R1 != R2 != ... */
	public TDLAxiom disjointORoles(List<TDLExpression> l) {
		return Ontology.add(new TDLAxiomDisjointORoles(l));
	}

	/** axiom R1 != R2 != ... */
	public TDLAxiom disjointDRoles(List<TDLExpression> l) {
		return Ontology.add(new TDLAxiomDisjointDRoles(l));
	}

	/** Domain (R C) */
	public TDLAxiom setODomain(TDLObjectRoleExpression R, TDLConceptExpression C) {
		return Ontology.add(new TDLAxiomORoleDomain(R, C));
	}

	/** Domain (R C) */
	public TDLAxiom setDDomain(TDLDataRoleExpression R, TDLConceptExpression C) {
		return Ontology.add(new TDLAxiomDRoleDomain(R, C));
	}

	/** Range (R C) */
	public TDLAxiom setORange(TDLObjectRoleExpression R, TDLConceptExpression C) {
		return Ontology.add(new TDLAxiomORoleRange(R, C));
	}

	/** Range (R E) */
	public TDLAxiom setDRange(TDLDataRoleExpression R, TDLDataExpression E) {
		return Ontology.add(new TDLAxiomDRoleRange(R, E));
	}

	/** Transitive (R) */
	public TDLAxiom setTransitive(TDLObjectRoleExpression R) {
		return Ontology.add(new TDLAxiomRoleTransitive(R));
	}

	/** Reflexive (R) */
	public TDLAxiom setReflexive(TDLObjectRoleExpression R) {
		return Ontology.add(new TDLAxiomRoleReflexive(R));
	}

	/** Irreflexive (R): Domain(R) = \neg ER.Self */
	public TDLAxiom setIrreflexive(TDLObjectRoleExpression R) {
		return Ontology.add(new TDLAxiomRoleIrreflexive(R));
	}

	/** Symmetric (R): R [= R^- */
	public TDLAxiom setSymmetric(TDLObjectRoleExpression R) {
		return Ontology.add(new TDLAxiomRoleSymmetric(R));
	}

	/** AntySymmetric (R): disjoint(R,R^-) */
	public TDLAxiom setAsymmetric(TDLObjectRoleExpression R) {
		return Ontology.add(new TDLAxiomRoleAsymmetric(R));
	}

	/** Functional (R) */
	public TDLAxiom setOFunctional(TDLObjectRoleExpression R) {
		return Ontology.add(new TDLAxiomORoleFunctional(R));
	}

	/** Functional (R) */
	public TDLAxiom setDFunctional(TDLDataRoleExpression R) {
		return Ontology.add(new TDLAxiomDRoleFunctional(R));
	}

	/** InverseFunctional (R) */
	public TDLAxiom setInverseFunctional(TDLObjectRoleExpression R) {
		return Ontology.add(new TDLAxiomRoleInverseFunctional(R));
	}

	// Individual axioms
	/** axiom I e C */
	public TDLAxiom instanceOf(TDLIndividualExpression I, TDLConceptExpression C) {
		return Ontology.add(new TDLAxiomInstanceOf(I, C));
	}

	/** axiom <I,J>:R */
	public TDLAxiom relatedTo(TDLIndividualExpression I,
			TDLObjectRoleExpression R, TDLIndividualExpression J) {
		return Ontology.add(new TDLAxiomRelatedTo(I, R, J));
	}

	/** axiom <I,J>:\neg R */
	public TDLAxiom relatedToNot(TDLIndividualExpression I,
			TDLObjectRoleExpression R, TDLIndividualExpression J) {
		return Ontology.add(new TDLAxiomRelatedToNot(I, R, J));
	}

	/** axiom (value I A V) */
	public TDLAxiom valueOf(TDLIndividualExpression I, TDLDataRoleExpression A,
			TDLDataValue V) {
		return Ontology.add(new TDLAxiomValueOf(I, A, V));
	}

	/** axiom <I,V>:\neg A */
	public TDLAxiom valueOfNot(TDLIndividualExpression I,
			TDLDataRoleExpression A, TDLDataValue V) {
		return Ontology.add(new TDLAxiomValueOfNot(I, A, V));
	}

	/** same individuals */
	public TDLAxiom processSame(List<TDLExpression> l) {
		return Ontology.add(new TDLAxiomSameIndividuals(l));
	}

	/** different individuals */
	public TDLAxiom processDifferent(List<TDLExpression> l) {
		return Ontology.add(new TDLAxiomDifferentIndividuals(l));
	}

	/** let all concept expressions in the ArgQueue to be fairness constraints */
	public TDLAxiom setFairnessConstraint(List<TDLExpression> l) {
		return Ontology.add(new TDLAxiomFairnessConstraint(l));
	}

	/** retract an axiom */
	public void retract(TDLAxiom axiom) {
		Ontology.retract(axiom);
	}

	//* ASK part
	/*
	 * Before execution of any query the Kernel make sure that the KB is in an appropriate
	 * state: Preprocessed, Classified or Realised. If the ontology was changed between asks,
	 * incremental classification is performed and the corrected result is returned.
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
	public boolean isFunctional(final TDLObjectRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (isUniversalRole(R)) {
			return false; // universal role is not functional
		}
		if (isEmptyRole(R)) {
			return true; // empty role is functional
		}
		return getFunctionality(getRole(R,
				"Role expression expected in isFunctional()"));
	}

	/** @return true iff data role is functional */
	public boolean isFunctional(final TDLDataRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (isUniversalRole(R)) {
			return false; // universal role is not functional
		}
		if (isEmptyRole(R)) {
			return true; // empty role is functional
		}
		return getFunctionality(getRole(R,
				"Role expression expected in isFunctional()"));
	}

	/** @return true iff role is inverse-functional */
	public boolean isInverseFunctional(final TDLObjectRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (isUniversalRole(R)) {
			return false; // universal role is not functional
		}
		if (isEmptyRole(R)) {
			return true; // empty role is functional
		}
		return getFunctionality(getRole(R,
				"Role expression expected in isInverseFunctional()").inverse());
	}

	/** @return true iff role is transitive */
	public boolean isTransitive(TDLObjectRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (isUniversalRole(R)) {
			return true; // universal role is transitive
		}
		if (isEmptyRole(R)) {
			return true; // empty role is transitive
		}
		TRole r = getRole(R, "Role expression expected in isTransitive()");
		if (!r.isTransitivityKnown()) {
			r.setTransitive(checkTransitivity(e(R)));
		}
		return r.isTransitive();
	}

	/** @return true iff role is symmetric */
	public boolean isSymmetric(TDLObjectRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (isUniversalRole(R)) {
			return true; // universal role is symmetric
		}
		if (isEmptyRole(R)) {
			return true; // empty role is symmetric
		}
		TRole r = getRole(R, "Role expression expected in isSymmetric()");
		if (!r.isSymmetryKnown()) {
			r.setSymmetric(checkSymmetry(e(R)));
		}
		return r.isSymmetric();
	}

	/** @return true iff role is asymmetric */
	public boolean isAsymmetric(TDLObjectRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (isUniversalRole(R)) {
			return false; // universal role is not asymmetric
		}
		if (isEmptyRole(R)) {
			return true; // empty role is symmetric
		}
		TRole r = getRole(R, "Role expression expected in isAsymmetric()");
		if (!r.isAsymmetryKnown()) {
			r.setAsymmetric(getTBox().isDisjointRoles(r, r.inverse()));
		}
		return r.isAsymmetric();
	}

	/** @return true iff role is reflexive */
	public boolean isReflexive(TDLObjectRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (isUniversalRole(R)) {
			return true; // universal role is reflexive
		}
		if (isEmptyRole(R)) {
			return false; // empty role is not reflexive
		}
		TRole r = getRole(R, "Role expression expected in isReflexive()");
		if (!r.isReflexivityKnown()) {
			r.setReflexive(checkReflexivity(e(R)));
		}
		return r.isReflexive();
	}

	/** @return true iff role is irreflexive */
	public boolean isIrreflexive(TDLObjectRoleExpression R) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (isUniversalRole(R)) {
			return false; // universal role is not irreflexive
		}
		if (isEmptyRole(R)) {
			return true; // empty role is irreflexive
		}
		TRole r = getRole(R, "Role expression expected in isIrreflexive()");
		if (!r.isIrreflexivityKnown()) {
			r.setIrreflexive(getTBox().isIrreflexive(r));
		}
		return r.isIrreflexive();
	}

	/** @return true iff two roles are disjoint */
	public boolean isDisjointRoles(final TDLObjectRoleExpression R,
			final TDLObjectRoleExpression S) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (isUniversalRole(R) || isUniversalRole(S)) {
			return false; // universal role is not disjoint with anything
		}
		if (isEmptyRole(R) || isEmptyRole(S)) {
			return true; // empty role is disjoint with everything
		}
		return getTBox().isDisjointRoles(
				getRole(R, "Role expression expected in isDisjointRoles()"),
				getRole(S, "Role expression expected in isDisjointRoles()"));
	}

	/** @return true iff two roles are disjoint */
	public boolean isDisjointRoles(final TDLDataRoleExpression R,
			final TDLDataRoleExpression S) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (isUniversalRole(R) || isUniversalRole(S)) {
			return false; // universal role is not disjoint with anything
		}
		if (isEmptyRole(R) || isEmptyRole(S)) {
			return true; // empty role is disjoint with everything
		}
		return getTBox().isDisjointRoles(
				getRole(R, "Role expression expected in isDisjointRoles()"),
				getRole(S, "Role expression expected in isDisjointRoles()"));
	}

	/** @return true if R is a sub-role of S */
	public boolean isSubRoles(TDLObjectRoleExpression R,
			TDLObjectRoleExpression S) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (isEmptyRole(R) || isUniversalRole(S)) {
			return true; // \bot [= X [= \top
		}
		if (isUniversalRole(R) && isEmptyRole(S)) {
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
	public boolean isSatisfiable(final TDLConceptExpression C) {
		preprocessKB();
		try {
			return checkSat(e(C));
		} catch (OWLRuntimeException crn) {
			if (C instanceof TDLConceptName) {
				// this is an unknown concept
				return true;
			}
			// complex expression, involving unknown names
			throw crn;
		}
	}

	/** @return true iff C [= D holds */
	public boolean isSubsumedBy(final TDLConceptExpression C,
			final TDLConceptExpression D) {
		preprocessKB();
		try {
			return checkSub(e(C), e(D));
		} catch (OWLRuntimeException e) {
			//XXX this needs a better approach
			System.out
					.println("ReasoningKernel.isSameIndividuals() WARNING: an exception was thrown: returning false as default\n"
							+ e.getMessage());
			//			StringWriter w = new StringWriter();
			//			PrintWriter p = new PrintWriter(w);
			//			e.printStackTrace(p);
			//			System.out.println(w.toString());
			return false;
		}
	}

	/** @return true iff C is disjoint with D; that is, C [= \not D holds */
	public boolean isDisjoint(final TDLConceptExpression C,
			final TDLConceptExpression D) {
		preprocessKB();
		return checkSub(e(C), DLTreeFactory.createSNFNot(e(D)));
	}

	/** @return true iff C is equivalent to D */
	public boolean isEquivalent(final TDLConceptExpression C,
			final TDLConceptExpression D) {
		preprocessKB();
		return isSubsumedBy(C, D) && isSubsumedBy(D, C);
	}

	// concept hierarchy
	/** apply actor__apply() to all DIRECT super-concepts of [complex] C */
	public void getSupConcepts(final TDLConceptExpression C, boolean direct,
			Actor actor) {
		classifyKB(); // ensure KB is ready to answer the query
		setUpCache(e(C), csClassified);
		Taxonomy tax = getCTaxonomy();
		if (direct) {
			tax.getRelativesInfo(cachedVertex, actor, false, true, true);
		} else {
			tax.getRelativesInfo(cachedVertex, actor, false, false, true);
		}
	}

	/** apply actor__apply() to all DIRECT sub-concepts of [complex] C */
	public void getSubConcepts(final TDLConceptExpression C, boolean direct,
			Actor actor) {
		classifyKB(); // ensure KB is ready to answer the query
		setUpCache(e(C), csClassified);
		Taxonomy tax = getCTaxonomy();
		if (direct) {
			tax.getRelativesInfo(cachedVertex, actor, false, true, false);
		} else {
			tax.getRelativesInfo(cachedVertex, actor, false, false, false);
		}
	}

	/** apply actor__apply() to all synonyms of [complex] C */
	public void getEquivalentConcepts(final TDLConceptExpression C, Actor actor) {
		classifyKB(); // ensure KB is ready to answer the query
		setUpCache(e(C), csClassified);
		actor.apply(cachedVertex);
	}

	// role hierarchy
	/** apply actor__apply() to all DIRECT super-roles of [complex] R */
	public void getSupRoles(final TDLRoleExpression r, boolean direct,
			Actor actor) {
		preprocessKB(); // ensure KB is ready to answer the query
		TRole R = getRole(r, "Role expression expected in getSupRoles()");
		Taxonomy tax = getTaxonomy(R);
		if (direct) {
			tax.getRelativesInfo(getTaxVertex(R), actor, false, true, true);
		} else {
			tax.getRelativesInfo(getTaxVertex(R), actor, false, false, true);
		}
	}

	/** apply actor__apply() to all DIRECT sub-roles of [complex] R */
	public void getSubRoles(final TDLRoleExpression r, boolean direct,
			Actor actor) {
		preprocessKB(); // ensure KB is ready to answer the query
		TRole R = getRole(r, "Role expression expected in getSubRoles()");
		Taxonomy tax = getTaxonomy(R);
		if (direct) {
			tax.getRelativesInfo(getTaxVertex(R), actor, false, true, false);
		} else {
			tax.getRelativesInfo(getTaxVertex(R), actor, false, false, false);
		}
	}

	/** apply actor__apply() to all synonyms of [complex] R */
	public void getEquivalentRoles(final TDLRoleExpression r, Actor actor) {
		preprocessKB(); // ensure KB is ready to answer the query
		TRole R = getRole(r, "Role expression expected in getEquivalentRoles()");
		actor.apply(getTaxVertex(R));
	}

	// domain and range as a set of named concepts
	/**
	 * apply actor__apply() to all DIRECT NC that are in the domain of [complex]
	 * R
	 */
	public void getRoleDomain(final TDLRoleExpression r, boolean direct,
			Actor actor) {
		classifyKB(); // ensure KB is ready to answer the query
		setUpCache(
				DLTreeFactory.createSNFExists(e(r), DLTreeFactory.createTop()),
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
	public void getRoleRange(final TDLObjectRoleExpression r, boolean direct,
			Actor actor) {
		getRoleDomain(getExpressionManager().Inverse(r), direct, actor);
	}

	// instances
	/** apply actor__apply() to all direct instances of given [complex] C */
	public void getDirectInstances(final TDLConceptExpression C, Actor actor) {
		realiseKB(); // ensure KB is ready to answer the query
		setUpCache(e(C), csClassified);
		// implement 1-level check by hand
		// if the root vertex contains individuals -- we are done
		if (actor.apply(cachedVertex)) {
			return;
		}
		// if not, just go 1 level down and apply the actor regardless of what's found
		// FIXME!! check again after bucket-method will be implemented
		for (TaxonomyVertex p : cachedVertex.neigh(/*upDirection=*/false)) {
			actor.apply(p);
		}
	}

	/** apply actor__apply() to all instances of given [complex] C */
	public void getInstances(final TDLConceptExpression C, Actor actor) { // FIXME!! check for Racer's/IS approach
		realiseKB(); // ensure KB is ready to answer the query
		setUpCache(e(C), csClassified);
		Taxonomy tax = getCTaxonomy();
		tax.getRelativesInfo(cachedVertex, actor, true, false, false);
	}

	/**
	 * apply actor__apply() to all DIRECT concepts that are types of an
	 * individual I
	 */
	public void getTypes(final TDLIndividualExpression I, boolean direct,
			Actor actor) {
		realiseKB(); // ensure KB is ready to answer the query
		setUpCache(e(I), csClassified);
		Taxonomy tax = getCTaxonomy();
		if (direct) {
			tax.getRelativesInfo(cachedVertex, actor, true, true, true);
		} else {
			tax.getRelativesInfo(cachedVertex, actor, true, false, true);
		}
	}

	/** apply actor__apply() to all synonyms of an individual I */
	public void getSameAs(final TDLIndividualExpression I, Actor actor) {
		realiseKB(); // ensure KB is ready to answer the query
		getEquivalentConcepts(getExpressionManager().OneOf(I), actor);
	}

	/** @return true iff I and J refer to the same individual */
	public boolean isSameIndividuals(final TDLIndividualExpression I,
			final TDLIndividualExpression J) {
		realiseKB();
		try {
			TIndividual i = getIndividual(I,
					"Only known individuals are allowed in the isSameAs()");
			TIndividual j = getIndividual(J,
					"Only known individuals are allowed in the isSameAs()");
			return getTBox().isSameIndividuals(i, j);
		} catch (OWLRuntimeException e) {
			//XXX this needs a better approach
			System.out
					.println("ReasoningKernel.isSameIndividuals() WARNING: an exception was thrown: returning false as default\n"
							+ e.getMessage());
			//			StringWriter w = new StringWriter();
			//			PrintWriter p = new PrintWriter(w);
			//			e.printStackTrace(p);
			//			System.out.println(w.toString());
			return false;
		}
	}

	/** @return true iff individual I is instance of given [complex] C */
	public boolean isInstance(final TDLIndividualExpression I,
			final TDLConceptExpression C) {
		realiseKB(); // ensure KB is ready to answer the query
		try {
			getIndividual(I, "individual name expected in the isInstance()");
			return isSubsumedBy(getExpressionManager().OneOf(I), C);
		} catch (OWLRuntimeException e) {
			//XXX this needs a better approach
			System.out
					.println("ReasoningKernel.isSameIndividuals() WARNING: an exception was thrown: returning false as default\n"
							+ e.getMessage());
			//			StringWriter w = new StringWriter();
			//			PrintWriter p = new PrintWriter(w);
			//			e.printStackTrace(p);
			//			System.out.println(w.toString());
			return false;
		}
	}

	public ReasoningKernel() {
		pTBox = null;
		pET = null;
		pMonitor = null;
		OpTimeout = 0;
		verboseOutput = false;
		cachedQuery = null;
		initCacheAndFlags();
		if (initOptions()) {
			throw new ReasonerInternalException(
					"FaCT++ kernel: Cannot init options");
		}
	}

	private boolean tryIncremental() {
		if (pTBox == null) {
			return true;
		}
		if (!Ontology.isChanged()) {
			return false;
		}
		return true;
	}

	private void forceReload() {
		clearTBox();
		newKB();
		pMonitor = null;
		TOntologyLoader OntologyLoader = new TOntologyLoader(getTBox());
		OntologyLoader.visitOntology(Ontology);
		Ontology.setProcessed();
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
				Classify(status);
				stillGo = false;
				break; // do classification
			}
			case kbClassified: {
				Realise();
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
			Classify(status);
		}
	}

	// do classification
	private void Classify(KBStatus status) {
		// don't do classification twice
		if (status != kbRealised) {
			//goto Realise;
			if (!pTBox.isConsistent()) {
				return;
			}
			pTBox.performClassification();
			return;
		}
		Realise();
	}

	// do realisation
	private void Realise() {
		if (!pTBox.isConsistent()) {
			return;
		}
		pTBox.performRealisation();
	}

	private void setUpCache(DLTree query, CacheStatus level) {
		// if KB was changed since it was classified,
		// we should catch it before
		assert !Ontology.isChanged();
		// check if the query is already cached
		if (cachedQuery != null && equalTrees(cachedQuery, query)) { // ... with the same level -- nothing to do
			query = null;
			if (level.ordinal() <= cacheLevel.ordinal()) {
				return;
			} else { // concept was defined but not classified yet
				assert level == csClassified && cacheLevel != csClassified;
				if (cacheLevel == csEmpty) {
					//XXX this is broken: query is necessarily null but unchecked
					needSetup(level, query);
					return;
				} else {
					needClassify(level);
					return;
				}
			}
		}
		// change current query
		//deleteTree(cachedQuery);
		cachedQuery = query;
		needSetup(level, query);
	}

	// classification only needed for complex expression
	private void needClassify(CacheStatus level) {
		if (level == csClassified) {
			classifyKB();
			getTBox().classifyQueryConcept();
			// cached concept now have to be classified
			assert cachedConcept.isClassified();
			cachedVertex = cachedConcept.getTaxVertex();
		}
	}

	private void needSetup(CacheStatus level, DLTree query) {
		// clean cached info
		cachedVertex = null;
		// check if concept-to-cache is defined in ontology
		if (query.isCN()) {
			cachedConcept = getTBox().getCI(query);
			// undefined/non-classified concept -- need to reclassify
			if (cachedConcept == null) {
				// invalidate cache
				cacheLevel = csEmpty;
				// FIXME!! reclassification
				throw new ReasonerInternalException(
						"FaCT++ Kernel: incremental classification not supported");
			}
			cacheLevel = level;
			if (level == csClassified) // need to set the pointers
			{
				classifyKB();
				//assert ( cachedConcept.isClassified() );
				cachedVertex = cachedConcept.getTaxVertex();
			}
			return;
		}
		// we are preprocessed here
		// case of complex query
		cachedConcept = getTBox().createQueryConcept(query);
		cacheLevel = level;
		needClassify(level);
	}

	/**
	 * @return true iff the chain contained in the arg-list is a sub-property of
	 *         R
	 */
	private boolean checkSubChain(TRole R, List<TDLExpression> l) {
		// retrieve a role chain
		// R1 o ... o Rn [= R iff \ER1.\ER2....\ERn.(notC) and AR.C is unsatisfiable
		DLTree tmp = DLTreeFactory.createSNFNot(getTBox().getFreshConcept());
		for (int i = l.size() - 1; i > -1; i--) {
			TDLExpression p = l.get(i);
			if (!(p instanceof TDLObjectRoleExpression)) {
				throw new ReasonerInternalException(
						"Role expression expected in the role chain construct");
			}
			TDLObjectRoleExpression Ri = (TDLObjectRoleExpression) p;
			tmp = DLTreeFactory.createSNFExists(e(Ri), tmp);
		}
		tmp = DLTreeFactory.createSNFAnd(tmp, DLTreeFactory.createSNFForall(
				DLTreeFactory.buildTree(new TLexeme(Token.RNAME, R)), getTBox()
						.getFreshConcept()));
		return !checkSat(tmp);
	}

	/** @return true if R is a super-role of a chain holding in the args */
	public boolean isSubChain(TDLObjectRoleExpression R, List<TDLExpression> l) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (isUniversalRole(R)) {
			return true; // universal role is a super of any chain
		}
		if (isEmptyRole(R)) {
			return false; // empty role is not a super of any chain
		}
		return checkSubChain(
				getRole(R, "Role expression expected in isSubChain()"), l);
	}

	/** @return true if R is a sub-role of S */
	public boolean isSubRoles(TDLDataRoleExpression R, TDLDataRoleExpression S) {
		preprocessKB(); // ensure KB is ready to answer the query
		if (isEmptyRole(R) || isUniversalRole(S)) {
			return true; // \bot [= X [= \top
		}
		if (isUniversalRole(R) && isEmptyRole(S)) {
			return false; // as \top [= \bot leads to inconsistent ontology
		}
		// told case first
		if (getRole(R, "Role expression expected in isSubRoles()").lesserequal(
				getRole(S, "Role expression expected in isSubRoles()"))) {
			return true;
		}
		// FIXME!! we can hardly do better, but need to think more here
		return false;
	}

	// all-disjoint query implementation
	public boolean isDisjointRoles(List<TDLExpression> l) {
		// grab all roles from the arg-list
		//List<TDLExpression> Disj = getExpressionManager().getArgList();
		List<TRole> Roles = new ArrayList<TRole>(l.size());
		for (TDLExpression p : l) {
			if (p instanceof TDLObjectRoleExpression) {
				TDLObjectRoleExpression ORole = (TDLObjectRoleExpression) p;
				if (isUniversalRole(ORole)) {
					return false; // universal role is not disjoint with anything
				}
				if (isEmptyRole(ORole)) {
					continue; // empty role is disjoint with everything
				}
				Roles.add(getRole(ORole,
						"Role expression expected in isDisjointRoles()"));
			} else {
				if (!(p instanceof TDLDataRoleExpression)) {
					throw new ReasonerInternalException(
							"Role expression expected in isDisjointRoles()");
				}
				TDLDataRoleExpression DRole = (TDLDataRoleExpression) p;
				if (isUniversalRole(DRole)) {
					return false; // universal role is not disjoint with anything
				}
				if (isEmptyRole(DRole)) {
					continue; // empty role is disjoint with everything
				}
				Roles.add(getRole(DRole,
						"Role expression expected in isDisjointRoles()"));
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

	private List<TIndividual> buildRelatedCache(TIndividual I, final TRole R) {
		if (R.isSynonym()) {
			return getRelated(I, ClassifiableEntry.resolveSynonym(R));
		}
		if (R.isDataRole() || R.isBottom()) {
			return new ArrayList<TIndividual>();
		}
		RIActor actor = new RIActor();
		TDLObjectRoleExpression InvR = R.getId() > 0 ? getExpressionManager()
				.Inverse(getExpressionManager().ObjectRole(R.getName()))
				: getExpressionManager().ObjectRole(R.inverse().getName());
		TDLConceptExpression query;
		if (R.isTop()) {
			query = getExpressionManager().Top();
		} else {
			query = getExpressionManager().Value(InvR,
					getExpressionManager().Individual(I.getName()));
		}
		getInstances(query, actor);
		return actor.getAcc();
	}

	public void getRelatedRoles(final TDLIndividualExpression I,
			List<TNamedEntry> Rs, boolean data, boolean needI) {
		realiseKB();
		Rs.clear();
		TIndividual i = getIndividual(I,
				"individual name expected in the getRelatedRoles()");
		RoleMaster RM = data ? getDRM() : getORM();
		for (TRole R : RM.getRoles()) {
			if ((R.getId() > 0 || needI) && !getRelated(i, R).isEmpty()) {
				Rs.add(R);
			}
		}
	}

	public void getRoleFillers(final TDLIndividualExpression I,
			final TDLObjectRoleExpression R, List<TNamedEntry> Result) {
		realiseKB();
		List<TIndividual> vec = getRelated(
				getIndividual(I,
						"Individual name expected in the getRoleFillers()"),
				getRole(R, "Role expression expected in the getRoleFillers()"));
		for (TIndividual p : vec) {
			Result.add(p);
		}
	}

	public boolean isRelated(final TDLIndividualExpression I,
			final TDLObjectRoleExpression R, final TDLIndividualExpression J) {
		realiseKB();
		TIndividual i = getIndividual(I,
				"Individual name expected in the isRelated()");
		TRole r = getRole(R, "Role expression expected in the isRelated()");
		if (r.isDataRole()) {
			return false;
		}
		TIndividual j = getIndividual(J,
				"Individual name expected in the isRelated()");
		List<TIndividual> vec = getRelated(i, r);
		for (TIndividual p : vec) {
			if (j.equals(p)) {
				return true;
			}
		}
		return false;
	}

	private boolean initOptions() {
		if (KernelOptions
				.RegisterOption(
						"useRelevantOnly",
						"Option 'useRelevantOnly' is used when creating internal DAG representation for externally given TBox. "
								+ "If true, DAG contains only concepts, relevant to query. It is safe to leave this option false.",
						IFOption.IOType.iotBool, "false")) {
			return true;
		}
		if (KernelOptions
				.RegisterOption(
						"dumpQuery",
						"Option 'dumpQuery' dumps sub-TBox relevant to given satisfiability/subsumption query.",
						IFOption.IOType.iotBool, "false")) {
			return true;
		}
		if (KernelOptions
				.RegisterOption(
						"absorptionFlags",
						"Option 'absorptionFlags' sets up absorption process for general axioms. "
								+ "It text field of arbitrary length; every symbol means the absorption action: "
								+ "(B)ottom Absorption), (T)op absorption, (E)quivalent concepts replacement, (C)oncept absorption, "
								+ "(N)egated concept absorption, (F)orall expression replacement, (R)ole absorption, (S)plit",
						IFOption.IOType.iotText, "BTECFSR")) {
			return true;
		}
		if (KernelOptions
				.RegisterOption(
						"alwaysPreferEquals",
						"Option 'alwaysPreferEquals' allows user to enforce usage of C=D definition instead of C[=D "
								+ "during absorption, even if implication appeares earlier in stream of axioms.",
						IFOption.IOType.iotBool, "true")) {
			return true;
		}
		if (KernelOptions
				.RegisterOption(
						"usePrecompletion",
						"Option 'usePrecompletion' switchs on and off precompletion process for ABox.",
						IFOption.IOType.iotBool, "false")) {
			return true;
		}
		if (KernelOptions
				.RegisterOption(
						"orSortSub",
						"Option 'orSortSub' define the sorting order of OR vertices in the DAG used in subsumption tests. "
								+ "Option has form of string 'Mop', where 'M' is a sort field (could be 'D' for depth, 'S' for size, 'F' "
								+ "for frequency, and '0' for no sorting), 'o' is a order field (could be 'a' for ascending and 'd' "
								+ "for descending mode), and 'p' is a preference field (could be 'p' for preferencing non-generating "
								+ "rules and 'n' for not doing so).",
						IFOption.IOType.iotText, "0")) {
			return true;
		}
		if (KernelOptions
				.RegisterOption(
						"orSortSat",
						"Option 'orSortSat' define the sorting order of OR vertices in the DAG used in satisfiability tests "
								+ "(used mostly in caching). Option has form of string 'Mop', see orSortSub for details.",
						IFOption.IOType.iotText, "0")) {
			return true;
		}
		if (KernelOptions
				.RegisterOption(
						"IAOEFLG",
						"Option 'IAOEFLG' define the priorities of different operations in TODO list. Possible values are "
								+ "7-digit strings with ony possible digit are 0-6. The digits on the places 1, 2, ..., 7 are for "
								+ "priority of Id, And, Or, Exists, Forall, LE and GE operations respectively. The smaller number means "
								+ "the higher priority. All other constructions (TOP, BOTTOM, etc) has priority 0.",
						IFOption.IOType.iotText, "1263005")) {
			return true;
		}
		if (KernelOptions
				.RegisterOption(
						"useSemanticBranching",
						"Option 'useSemanticBranching' switch semantic branching on and off. The usage of semantic branching "
								+ "usually leads to faster reasoning, but sometime could give small overhead.",
						IFOption.IOType.iotBool, "true")) {
			return true;
		}
		if (KernelOptions
				.RegisterOption(
						"useBackjumping",
						"Option 'useBackjumping' switch backjumping on and off. The usage of backjumping "
								+ "usually leads to much faster reasoning.",
						IFOption.IOType.iotBool, "true")) {
			return true;
		}
		if (KernelOptions
				.RegisterOption(
						"testTimeout",
						"Option 'testTimeout' sets timeout for a single reasoning test in milliseconds.",
						IFOption.IOType.iotInt, "0")) {
			return true;
		}
		if (KernelOptions
				.RegisterOption(
						"useLazyBlocking",
						"Option 'useLazyBlocking' makes checking of blocking status as small as possible. This greatly "
								+ "increase speed of reasoning.",
						IFOption.IOType.iotBool, "true")) {
			return true;
		}
		if (KernelOptions
				.RegisterOption(
						"useAnywhereBlocking",
						"Option 'useAnywhereBlocking' allow user to choose between Anywhere and Ancestor blocking.",
						IFOption.IOType.iotBool, "true")) {
			return true;
		}
		if (KernelOptions
				.RegisterOption(
						"useCompletelyDefined",
						"Option 'useCompletelyDefined' leads to simpler Taxonomy creation if TBox contains no non-primitive "
								+ "concepts. Unfortunately, it is quite rare case.",
						IFOption.IOType.iotBool, "true")) {
			return true;
		}
		return false;
	}
}

enum CacheStatus {
	csEmpty, csSat, csClassified
}