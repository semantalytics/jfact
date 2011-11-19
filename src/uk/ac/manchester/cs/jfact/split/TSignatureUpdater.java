package uk.ac.manchester.cs.jfact.split;

import java.util.Collection;

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
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Axiom;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Expression;
import uk.ac.manchester.cs.jfact.visitors.DLAxiomVisitor;

/// update signature by adding the signature of a given axiom to it
public class TSignatureUpdater implements DLAxiomVisitor {
	/// helper with expressions
	TExpressionSignatureUpdater Updater;

	/// helper for the expression processing
	void v(Expression E) {
		E.accept(Updater);
	}

	/// helper for the [begin,end) interval
	void v(Collection<? extends Expression> arg) {
		for (Expression e : arg) {
			v(e);
		}
	}

	public void visit(AxiomDeclaration axiom) {
		v(axiom.getDeclaration());
	}

	public void visit(AxiomEquivalentConcepts axiom) {
		v(axiom.getArguments());
	}

	public void visit(AxiomDisjointConcepts axiom) {
		v(axiom.getArguments());
	}

	public void visit(AxiomDisjointUnion axiom) {
		v(axiom.getC());
		v(axiom.getArguments());
	}

	public void visit(AxiomEquivalentORoles axiom) {
		v(axiom.getArguments());
	}

	public void visit(AxiomEquivalentDRoles axiom) {
		v(axiom.getArguments());
	}

	public void visit(AxiomDisjointORoles axiom) {
		v(axiom.getArguments());
	}

	public void visit(AxiomDisjointDRoles axiom) {
		v(axiom.getArguments());
	}

	public void visit(AxiomSameIndividuals axiom) {
		v(axiom.getArguments());
	}

	public void visit(AxiomDifferentIndividuals axiom) {
		v(axiom.getArguments());
	}

	public void visit(AxiomFairnessConstraint axiom) {
		v(axiom.getArguments());
	}

	public void visit(AxiomRoleInverse axiom) {
		v(axiom.getRole());
		v(axiom.getInvRole());
	}

	public void visit(AxiomORoleSubsumption axiom) {
		v(axiom.getRole());
		v(axiom.getSubRole());
	}

	public void visit(AxiomDRoleSubsumption axiom) {
		v(axiom.getRole());
		v(axiom.getSubRole());
	}

	public void visit(AxiomORoleDomain axiom) {
		v(axiom.getRole());
		v(axiom.getDomain());
	}

	public void visit(AxiomDRoleDomain axiom) {
		v(axiom.getRole());
		v(axiom.getDomain());
	}

	public void visit(AxiomORoleRange axiom) {
		v(axiom.getRole());
		v(axiom.getRange());
	}

	public void visit(AxiomDRoleRange axiom) {
		v(axiom.getRole());
		v(axiom.getRange());
	}

	public void visit(AxiomRoleTransitive axiom) {
		v(axiom.getRole());
	}

	public void visit(AxiomRoleReflexive axiom) {
		v(axiom.getRole());
	}

	public void visit(AxiomRoleIrreflexive axiom) {
		v(axiom.getRole());
	}

	public void visit(AxiomRoleSymmetric axiom) {
		v(axiom.getRole());
	}

	public void visit(AxiomRoleAsymmetric axiom) {
		v(axiom.getRole());
	}

	public void visit(AxiomORoleFunctional axiom) {
		v(axiom.getRole());
	}

	public void visit(AxiomDRoleFunctional axiom) {
		v(axiom.getRole());
	}

	public void visit(AxiomRoleInverseFunctional axiom) {
		v(axiom.getRole());
	}

	public void visit(AxiomConceptInclusion axiom) {
		v(axiom.getSubConcept());
		v(axiom.getSupConcept());
	}

	public void visit(AxiomInstanceOf axiom) {
		v(axiom.getIndividual());
		v(axiom.getC());
	}

	public void visit(AxiomRelatedTo axiom) {
		v(axiom.getIndividual());
		v(axiom.getRelation());
		v(axiom.getRelatedIndividual());
	}

	public void visit(AxiomRelatedToNot axiom) {
		v(axiom.getIndividual());
		v(axiom.getRelation());
		v(axiom.getRelatedIndividual());
	}

	public void visit(AxiomValueOf axiom) {
		v(axiom.getIndividual());
		v(axiom.getAttribute());
	}

	public void visit(AxiomValueOfNot axiom) {
		v(axiom.getIndividual());
		v(axiom.getAttribute());
	}

	public TSignatureUpdater(TSignature sig) {
		Updater = new TExpressionSignatureUpdater(sig);
	}

	/// load ontology to a given KB
	public void visitOntology(Ontology ontology) {
		for (Axiom p : ontology.begin()) {
			if (p.isUsed()) {
				p.accept(this);
			}
		}
	}
}