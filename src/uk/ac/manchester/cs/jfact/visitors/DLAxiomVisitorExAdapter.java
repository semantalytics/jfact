package uk.ac.manchester.cs.jfact.visitors;

import uk.ac.manchester.cs.jfact.kernel.Ontology;
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

@SuppressWarnings("unused")
public class DLAxiomVisitorExAdapter<A> implements DLAxiomVisitorEx<A> {
	public A visit(AxiomDeclaration axiom) {
		return null;
	}

	public A visit(AxiomEquivalentConcepts axiom) {
		return null;
	}

	public A visit(AxiomDisjointConcepts axiom) {
		return null;
	}

	public A visit(AxiomEquivalentORoles axiom) {
		return null;
	}

	public A visit(AxiomEquivalentDRoles axiom) {
		return null;
	}

	public A visit(AxiomDisjointUnion axiom) {
		return null;
	}

	public A visit(AxiomDisjointORoles axiom) {
		return null;
	}

	public A visit(AxiomDisjointDRoles axiom) {
		return null;
	}

	public A visit(AxiomSameIndividuals axiom) {
		return null;
	}

	public A visit(AxiomDifferentIndividuals axiom) {
		return null;
	}

	public A visit(AxiomFairnessConstraint axiom) {
		return null;
	}

	public A visit(AxiomRoleInverse axiom) {
		return null;
	}

	public A visit(AxiomORoleSubsumption axiom) {
		return null;
	}

	public A visit(AxiomDRoleSubsumption axiom) {
		return null;
	}

	public A visit(AxiomORoleDomain axiom) {
		return null;
	}

	public A visit(AxiomDRoleDomain axiom) {
		return null;
	}

	public A visit(AxiomORoleRange axiom) {
		return null;
	}

	public A visit(AxiomDRoleRange axiom) {
		return null;
	}

	public A visit(AxiomRoleTransitive axiom) {
		return null;
	}

	public A visit(AxiomRoleReflexive axiom) {
		return null;
	}

	public A visit(AxiomRoleIrreflexive axiom) {
		return null;
	}

	public A visit(AxiomRoleSymmetric axiom) {
		return null;
	}

	public A visit(AxiomRoleAsymmetric axiom) {
		return null;
	}

	public A visit(AxiomORoleFunctional axiom) {
		return null;
	}

	public A visit(AxiomDRoleFunctional axiom) {
		return null;
	}

	public A visit(AxiomRoleInverseFunctional axiom) {
		return null;
	}

	public A visit(AxiomConceptInclusion axiom) {
		return null;
	}

	public A visit(AxiomInstanceOf axiom) {
		return null;
	}

	public A visit(AxiomRelatedTo axiom) {
		return null;
	}

	public A visit(AxiomRelatedToNot axiom) {
		return null;
	}

	public A visit(AxiomValueOf axiom) {
		return null;
	}

	public A visit(AxiomValueOfNot axiom) {
		return null;
	}

	public A visitOntology(Ontology ontology) {
		return null;
	}
}
