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
public class DLAxiomVisitorAdapter implements DLAxiomVisitor {
	public void visit(AxiomDeclaration axiom) {}

	public void visit(AxiomEquivalentConcepts axiom) {}

	public void visit(AxiomDisjointConcepts axiom) {}

	public void visit(AxiomEquivalentORoles axiom) {}

	public void visit(AxiomEquivalentDRoles axiom) {}

	public void visit(AxiomDisjointORoles axiom) {}

	public void visit(AxiomDisjointDRoles axiom) {}

	public void visit(AxiomSameIndividuals axiom) {}

	public void visit(AxiomDifferentIndividuals axiom) {}

	public void visit(AxiomFairnessConstraint axiom) {}

	public void visit(AxiomRoleInverse axiom) {}

	public void visit(AxiomORoleSubsumption axiom) {}

	public void visit(AxiomDRoleSubsumption axiom) {}

	public void visit(AxiomORoleDomain axiom) {}

	public void visit(AxiomDRoleDomain axiom) {}

	public void visit(AxiomORoleRange axiom) {}

	public void visit(AxiomDRoleRange axiom) {}

	public void visit(AxiomRoleTransitive axiom) {}

	public void visit(AxiomRoleReflexive axiom) {}

	public void visit(AxiomRoleIrreflexive axiom) {}

	public void visit(AxiomRoleSymmetric axiom) {}

	public void visit(AxiomRoleAsymmetric axiom) {}

	public void visit(AxiomORoleFunctional axiom) {}

	public void visit(AxiomDRoleFunctional axiom) {}

	public void visit(AxiomRoleInverseFunctional axiom) {}

	public void visit(AxiomConceptInclusion axiom) {}

	public void visit(AxiomInstanceOf axiom) {}

	public void visit(AxiomRelatedTo axiom) {}

	public void visit(AxiomRelatedToNot axiom) {}

	public void visit(AxiomValueOf axiom) {}

	public void visit(AxiomValueOfNot axiom) {}

	public void visitOntology(Ontology ontology) {}

	public void visit(AxiomDisjointUnion axiom) {}
}
