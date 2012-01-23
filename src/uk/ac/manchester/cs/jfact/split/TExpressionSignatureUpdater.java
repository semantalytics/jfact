package uk.ac.manchester.cs.jfact.split;

import uk.ac.manchester.cs.jfact.datatypes.Datatype;
import uk.ac.manchester.cs.jfact.datatypes.Literal;
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
import uk.ac.manchester.cs.jfact.kernel.dl.IndividualName;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleChain;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleInverse;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleName;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleProjectionFrom;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleProjectionInto;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleTop;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ConceptArg;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataRoleArg;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Expression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.IndividualExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.NAryExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.NamedEntity;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ObjectRoleArg;
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

	public void visit(Datatype<?> expr) {}

	public void visit(Literal<?> expr) {}

	public void visit(DataNot expr) {}

	public void visit(DataAnd expr) {}

	public void visit(DataOr expr) {}

	public void visit(DataOneOf expr) {}
}
