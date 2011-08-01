package uk.ac.manchester.cs.jfact.split;

import java.util.Collection;

import uk.ac.manchester.cs.jfact.kernel.Ontology;
import uk.ac.manchester.cs.jfact.kernel.datatype.DataValue;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptAnd;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptDataExactCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptDataExists;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptDataForall;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptDataMaxCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptDataMinCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptDataValue;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptName;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptNot;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectExactCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectExists;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectForall;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectMaxCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectMinCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectSelf;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectValue;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptOneOf;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptOr;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptTop;
import uk.ac.manchester.cs.jfact.kernel.dl.DataAnd;
import uk.ac.manchester.cs.jfact.kernel.dl.DataBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.DataNot;
import uk.ac.manchester.cs.jfact.kernel.dl.DataOneOf;
import uk.ac.manchester.cs.jfact.kernel.dl.DataOr;
import uk.ac.manchester.cs.jfact.kernel.dl.DataRoleBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.DataRoleName;
import uk.ac.manchester.cs.jfact.kernel.dl.DataRoleTop;
import uk.ac.manchester.cs.jfact.kernel.dl.DataTop;
import uk.ac.manchester.cs.jfact.kernel.dl.DataTypeName;
import uk.ac.manchester.cs.jfact.kernel.dl.DataTypeRestriction;
import uk.ac.manchester.cs.jfact.kernel.dl.FacetMaxExclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.FacetMaxInclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.FacetMinExclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.FacetMinInclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.IndividualName;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleChain;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleInverse;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleName;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleProjectionFrom;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleProjectionInto;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleTop;
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
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ConceptArg;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataRoleArg;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Expression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.IndividualExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.NAryExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.NamedEntity;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ObjectRoleArg;
import uk.ac.manchester.cs.jfact.visitors.DLAxiomVisitor;
import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitor;

/// update the signature by adding all signature elements from the expression
class TExpressionSignatureUpdater implements DLExpressionVisitor {
	/// Signature to be filled
	TSignature sig;

	/// helper for concept arguments
	void vC(ConceptArg expr) {
		expr.getConcept().accept(this);
	}

	/// helper for individual arguments
	void vI(IndividualExpression expr) {
		// should no longer be needed: IndividualNames are NamedEntities themselves
		if (expr instanceof NamedEntity) {
			sig.add((NamedEntity) expr);
		}
	}

	/// helper for object role arguments
	void vOR(ObjectRoleArg expr) {
		expr.getOR().accept(this);
	}

	/// helper for object role arguments
	void vDR(DataRoleArg expr) {
		expr.getDataRoleExpression().accept(this);
	}

	/// helper for the named entity
	void vE(NamedEntity e) {
		sig.add(e);
	}

	/// array helper
	void processArray(NAryExpression<? extends Expression> expr) {
		for (Expression p : expr.getArguments()) {
			p.accept(this);
		}
	}

	//TODO check whether it must copy or change
	public TExpressionSignatureUpdater(TSignature s) {
		sig = new TSignature(s);
	}

	// concept expressions
	public void visit(ConceptTop expr) {}

	public void visit(ConceptBottom expr) {}

	public void visit(ConceptName expr) {
		vE(expr);
	}

	public void visit(ConceptNot expr) {
		vC(expr);
	}

	public void visit(ConceptAnd expr) {
		processArray(expr);
	}

	public void visit(ConceptOr expr) {
		processArray(expr);
	}

	public void visit(ConceptOneOf expr) {
		processArray(expr);
	}

	public void visit(ConceptObjectSelf expr) {
		vOR(expr);
	}

	public void visit(ConceptObjectValue expr) {
		vOR(expr);
		vI(expr.getI());
	}

	public void visit(ConceptObjectExists expr) {
		vOR(expr);
		vC(expr);
	}

	public void visit(ConceptObjectForall expr) {
		vOR(expr);
		vC(expr);
	}

	public void visit(ConceptObjectMinCardinality expr) {
		vOR(expr);
		vC(expr);
	}

	public void visit(ConceptObjectMaxCardinality expr) {
		vOR(expr);
		vC(expr);
	}

	public void visit(ConceptObjectExactCardinality expr) {
		vOR(expr);
		vC(expr);
	}

	public void visit(ConceptDataValue expr) {
		vDR(expr);
	}

	public void visit(ConceptDataExists expr) {
		vDR(expr);
	}

	public void visit(ConceptDataForall expr) {
		vDR(expr);
	}

	public void visit(ConceptDataMinCardinality expr) {
		vDR(expr);
	}

	public void visit(ConceptDataMaxCardinality expr) {
		vDR(expr);
	}

	public void visit(ConceptDataExactCardinality expr) {
		vDR(expr);
	}

	// individual expressions
	public void visit(IndividualName expr) {
		vE(expr);
	}

	// object role expressions
	public void visit(ObjectRoleTop expr) {}

	public void visit(ObjectRoleBottom expr) {}

	public void visit(ObjectRoleName expr) {
		vE(expr);
	}

	public void visit(ObjectRoleInverse expr) {
		vOR(expr);
	}

	public void visit(ObjectRoleChain expr) {
		processArray(expr);
	}

	public void visit(ObjectRoleProjectionFrom expr) {
		vOR(expr);
		vC(expr);
	}

	public void visit(ObjectRoleProjectionInto expr) {
		vOR(expr);
		vC(expr);
	}

	// data role expressions
	public void visit(DataRoleTop expr) {}

	public void visit(DataRoleBottom expr) {}

	public void visit(DataRoleName expr) {
		vE(expr);
	}

	// data expressions
	public void visit(DataTop expr) {}

	public void visit(DataBottom expr) {}

	public void visit(DataTypeName expr) {}

	public void visit(DataTypeRestriction expr) {}

	public void visit(DataValue expr) {}

	public void visit(DataNot expr) {}

	public void visit(DataAnd expr) {}

	public void visit(DataOr expr) {}

	public void visit(DataOneOf expr) {}

	// facets
	public void visit(FacetMinInclusive expr) {}

	public void visit(FacetMinExclusive expr) {}

	public void visit(FacetMaxInclusive expr) {}

	public void visit(FacetMaxExclusive expr) {}
}

/// update signature by adding the signature of a given axiom to it
class TSignatureUpdater implements DLAxiomVisitor {
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