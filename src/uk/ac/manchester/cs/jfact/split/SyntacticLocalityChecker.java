package uk.ac.manchester.cs.jfact.split;

import java.util.List;

import uk.ac.manchester.cs.jfact.kernel.Ontology;
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
import uk.ac.manchester.cs.jfact.kernel.dl.DataRoleBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.DataRoleName;
import uk.ac.manchester.cs.jfact.kernel.dl.DataRoleTop;
import uk.ac.manchester.cs.jfact.kernel.dl.DataTop;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleChain;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleInverse;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleName;
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
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ConceptExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Expression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ObjectRoleExpression;
import uk.ac.manchester.cs.jfact.visitors.DLAxiomVisitor;
import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitor;
import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitorAdapter;

/// helper class to set signature and locality class
class SigAccessor extends DLExpressionVisitorAdapter {
	/// signature of a module
	TSignature sig = new TSignature();

	/// init c'tor
	SigAccessor(TSignature s) {
		sig = new TSignature(s);
	}

	/// @return true iff EXPR is a top datatype
	static boolean isTopDT(Expression expr) {
		return expr instanceof DataTop;
	}

	/// @return true iff EXPR is a top datatype or a built-in datatype; FIXME for now -- just top
	static boolean isTopOrBuiltInDT(Expression expr) {
		return isTopDT(expr);
	}

	/// @return true iff EXPR is a top datatype or an infinite built-in datatype; FIXME for now -- just top
	static boolean isTopOrBuiltInInfDT(Expression expr) {
		return isTopDT(expr);
	}
}

/// check whether class expressions are equivalent to bottom wrt given locality class
class BotEquivalenceEvaluator extends SigAccessor implements DLExpressionVisitor {
	/// corresponding top evaluator
	TopEquivalenceEvaluator TopEval = null;
	/// keep the value here
	boolean isBotEq;

	/// check whether the expression is top-equivalent
	boolean isTopEquivalent(Expression expr) {
		return TopEval.isTopEquivalent(expr);
	}

	/// @return true iff role expression in equivalent to const wrt locality
	boolean isREquivalent(Expression expr) {
		return sig.topRLocal() ? isTopEquivalent(expr) : isBotEquivalent(expr);
	}

	/// init c'tor
	BotEquivalenceEvaluator(TSignature s) {
		super(s);
	}

	// set fields
	/// set the corresponding top evaluator
	void setTopEval(TopEquivalenceEvaluator eval) {
		TopEval = eval;
	}

	/// @return true iff an EXPRession is equivalent to bottom wrt defined policy
	boolean isBotEquivalent(Expression expr) {
		expr.accept(this);
		return isBotEq;
	}

	// concept expressions
	@Override
	public void visit(ConceptTop expr) {
		isBotEq = false;
	}

	@Override
	public void visit(ConceptBottom expr) {
		isBotEq = true;
	}

	@Override
	public void visit(ConceptName expr) {
		isBotEq = !sig.topCLocal() && !sig.contains(expr);
	}

	@Override
	public void visit(ConceptNot expr) {
		isBotEq = isTopEquivalent(expr.getConcept());
	}

	@Override
	public void visit(ConceptAnd expr) {
		for (ConceptExpression p : expr.getArguments()) {
			if (isBotEquivalent(p)) {
				return;
			}
		}
		isBotEq = false;
	}

	@Override
	public void visit(ConceptOr expr) {
		for (ConceptExpression p : expr.getArguments()) {
			if (!isBotEquivalent(p)) {
				return;
			}
		}
		isBotEq = true;
	}

	@Override
	public void visit(ConceptOneOf expr) {
		isBotEq = expr.isEmpty();
	}

	@Override
	public void visit(ConceptObjectSelf expr) {
		isBotEq = !sig.topRLocal() && isBotEquivalent(expr.getOR());
	}

	@Override
	public void visit(ConceptObjectValue expr) {
		isBotEq = !sig.topRLocal() && isBotEquivalent(expr.getOR());
	}

	@Override
	public void visit(ConceptObjectExists expr) {
		isBotEq = isBotEquivalent(expr.getConcept());
		if (!sig.topRLocal()) {
			isBotEq |= isBotEquivalent(expr.getOR());
		}
	}

	@Override
	public void visit(ConceptObjectForall expr) {
		isBotEq = sig.topRLocal() && isTopEquivalent(expr.getOR())
				&& isBotEquivalent(expr.getConcept());
	}

	@Override
	public void visit(ConceptObjectMinCardinality expr) {
		isBotEq = expr.getCardinality() > 0
				&& (isBotEquivalent(expr.getConcept()) || !sig.topRLocal()
						&& isBotEquivalent(expr.getOR()));
	}

	@Override
	public void visit(ConceptObjectMaxCardinality expr) {
		isBotEq = sig.topRLocal() && expr.getCardinality() > 0
				&& isTopEquivalent(expr.getOR()) && isTopEquivalent(expr.getConcept());
	}

	@Override
	public void visit(ConceptObjectExactCardinality expr) {
		isBotEq = expr.getCardinality() > 0
				&& (isBotEquivalent(expr.getConcept()) || isREquivalent(expr.getOR())
						&& (sig.topRLocal() ? isTopEquivalent(expr.getConcept()) : true));
	}

	@Override
	public void visit(ConceptDataValue expr) {
		isBotEq = !sig.topRLocal() && isBotEquivalent(expr.getDataRoleExpression());
	}

	@Override
	public void visit(ConceptDataExists expr) {
		isBotEq = !sig.topRLocal() && isBotEquivalent(expr.getDataRoleExpression());
	}

	@Override
	public void visit(ConceptDataForall expr) {
		isBotEq = sig.topRLocal() && isTopEquivalent(expr.getDataRoleExpression())
				&& !isTopDT(expr.getExpr());
	}

	@Override
	public void visit(ConceptDataMinCardinality expr) {
		isBotEq = !sig.topRLocal() && expr.getCardinality() > 0
				&& isBotEquivalent(expr.getDataRoleExpression());
	}

	@Override
	public void visit(ConceptDataMaxCardinality expr) {
		isBotEq = sig.topRLocal()
				&& isTopEquivalent(expr.getDataRoleExpression())
				&& (expr.getCardinality() <= 1 ? isTopOrBuiltInDT(expr.getExpr())
						: isTopOrBuiltInInfDT(expr.getExpr()));
	}

	@Override
	public void visit(ConceptDataExactCardinality expr) {
		isBotEq = isREquivalent(expr.getDataRoleExpression())
				&& (sig.topRLocal() ? expr.getCardinality() == 0 ? isTopOrBuiltInDT(expr
						.getExpr()) : isTopOrBuiltInInfDT(expr.getExpr()) : expr
						.getCardinality() > 0);
	}

	// object role expressions
	@Override
	public void visit(ObjectRoleTop expr) {
		isBotEq = false;
	}

	@Override
	public void visit(ObjectRoleBottom expr) {
		isBotEq = true;
	}

	@Override
	public void visit(ObjectRoleName expr) {
		isBotEq = !sig.topRLocal() && !sig.contains(expr);
	}

	@Override
	public void visit(ObjectRoleInverse expr) {
		isBotEq = isBotEquivalent(expr.getOR());
	}

	@Override
	public void visit(ObjectRoleChain expr) {
		for (ObjectRoleExpression p : expr.getArguments()) {
			if (isBotEquivalent(p)) {
				return;
			}
		}
		isBotEq = false;
	}

	// data role expressions
	@Override
	public void visit(DataRoleTop expr) {
		isBotEq = false;
	}

	@Override
	public void visit(DataRoleBottom expr) {
		isBotEq = true;
	}

	@Override
	public void visit(DataRoleName expr) {
		isBotEq = !sig.topRLocal() && !sig.contains(expr);
	}
}

/// check whether class expressions are equivalent to top wrt given locality class
class TopEquivalenceEvaluator extends SigAccessor implements DLExpressionVisitor {
	/// corresponding bottom evaluator
	BotEquivalenceEvaluator BotEval = null;
	/// keep the value here
	boolean isTopEq;

	/// check whether the expression is top-equivalent
	boolean isBotEquivalent(Expression expr) {
		return BotEval.isBotEquivalent(expr);
	}

	/// @return true iff role expression in equivalent to const wrt locality
	boolean isREquivalent(Expression expr) {
		return sig.topRLocal() ? isTopEquivalent(expr) : isBotEquivalent(expr);
	}

	/// init c'tor
	TopEquivalenceEvaluator(TSignature s) {
		super(s);
	}

	// set fields
	/// set the corresponding bottom evaluator
	void setBotEval(BotEquivalenceEvaluator eval) {
		BotEval = eval;
	}

	/// @return true iff an EXPRession is equivalent to top wrt defined policy
	boolean isTopEquivalent(Expression expr) {
		expr.accept(this);
		return isTopEq;
	}

	// concept expressions
	@Override
	public void visit(ConceptTop expr) {
		isTopEq = true;
	}

	@Override
	public void visit(ConceptBottom expr) {
		isTopEq = false;
	}

	@Override
	public void visit(ConceptName expr) {
		isTopEq = sig.topCLocal() && !sig.contains(expr);
	}

	@Override
	public void visit(ConceptNot expr) {
		isTopEq = isBotEquivalent(expr.getConcept());
	}

	@Override
	public void visit(ConceptAnd expr) {
		for (ConceptExpression p : expr.getArguments()) {
			if (!isTopEquivalent(p)) {
				return;
			}
		}
		isTopEq = true;
	}

	@Override
	public void visit(ConceptOr expr) {
		for (ConceptExpression p : expr.getArguments()) {
			if (isTopEquivalent(p)) {
				return;
			}
		}
		isTopEq = false;
	}

	@Override
	public void visit(ConceptOneOf expr) {
		isTopEq = false;
	}

	@Override
	public void visit(ConceptObjectSelf expr) {
		isTopEq = sig.topRLocal() && isTopEquivalent(expr.getOR());
	}

	@Override
	public void visit(ConceptObjectValue expr) {
		isTopEq = sig.topRLocal() && isTopEquivalent(expr.getOR());
	}

	@Override
	public void visit(ConceptObjectExists expr) {
		isTopEq = sig.topRLocal() && isTopEquivalent(expr.getOR())
				&& isTopEquivalent(expr.getConcept());
	}

	@Override
	public void visit(ConceptObjectForall expr) {
		isTopEq = isTopEquivalent(expr.getConcept()) || !sig.topRLocal()
				&& isBotEquivalent(expr.getOR());
	}

	@Override
	public void visit(ConceptObjectMinCardinality expr) {
		isTopEq = expr.getCardinality() == 0 || sig.topRLocal()
				&& isTopEquivalent(expr.getOR()) && isTopEquivalent(expr.getConcept());
	}

	@Override
	public void visit(ConceptObjectMaxCardinality expr) {
		isTopEq = isBotEquivalent(expr.getConcept()) || !sig.topRLocal()
				&& isBotEquivalent(expr.getOR());
	}

	@Override
	public void visit(ConceptObjectExactCardinality expr) {
		isTopEq = expr.getCardinality() == 0
				&& (isBotEquivalent(expr.getConcept()) || !sig.topRLocal()
						&& isBotEquivalent(expr.getOR()));
	}

	@Override
	public void visit(ConceptDataValue expr) {
		isTopEq = sig.topRLocal() && isTopEquivalent(expr.getDataRoleExpression());
	}

	@Override
	public void visit(ConceptDataExists expr) {
		isTopEq = sig.topRLocal() && isTopEquivalent(expr.getDataRoleExpression())
				&& isTopOrBuiltInDT(expr.getExpr());
	}

	@Override
	public void visit(ConceptDataForall expr) {
		isTopEq = isTopDT(expr.getExpr()) || !sig.topRLocal()
				&& isBotEquivalent(expr.getDataRoleExpression());
	}

	@Override
	public void visit(ConceptDataMinCardinality expr) {
		isTopEq = expr.getCardinality() == 0;
		if (sig.topRLocal()) {
			isTopEq |= isTopEquivalent(expr.getDataRoleExpression())
					&& (expr.getCardinality() == 1 ? isTopOrBuiltInDT(expr.getExpr())
							: isTopOrBuiltInInfDT(expr.getExpr()));
		}
	}

	@Override
	public void visit(ConceptDataMaxCardinality expr) {
		isTopEq = !sig.topRLocal() && isBotEquivalent(expr.getDataRoleExpression());
	}

	@Override
	public void visit(ConceptDataExactCardinality expr) {
		isTopEq = !sig.topRLocal() && expr.getCardinality() == 0
				&& isBotEquivalent(expr.getDataRoleExpression());
	}

	// object role expressions
	@Override
	public void visit(ObjectRoleTop expr) {
		isTopEq = true;
	}

	@Override
	public void visit(ObjectRoleBottom expr) {
		isTopEq = false;
	}

	@Override
	public void visit(ObjectRoleName expr) {
		isTopEq = sig.topRLocal() && !sig.contains(expr);
	}

	@Override
	public void visit(ObjectRoleInverse expr) {
		isTopEq = isTopEquivalent(expr.getOR());
	}

	@Override
	public void visit(ObjectRoleChain expr) {
		for (ObjectRoleExpression p : expr.getArguments()) {
			if (!isTopEquivalent(p)) {
				return;
			}
		}
		isTopEq = true;
	}

	// data role expressions
	@Override
	public void visit(DataRoleTop expr) {
		isTopEq = true;
	}

	@Override
	public void visit(DataRoleBottom expr) {
		isTopEq = false;
	}

	@Override
	public void visit(DataRoleName expr) {
		isTopEq = sig.topRLocal() && !sig.contains(expr);
	}
}

/// syntactic locality checker for DL axioms
class SyntacticLocalityChecker extends SigAccessor implements DLAxiomVisitor {
	/// top evaluator
	TopEquivalenceEvaluator TopEval;
	/// bottom evaluator
	BotEquivalenceEvaluator BotEval;
	/// remember the axiom locality value here
	boolean isLocal;

	/// @return true iff EXPR is top equivalent
	boolean isTopEquivalent(Expression expr) {
		return TopEval.isTopEquivalent(expr);
	}

	/// @return true iff EXPR is bottom equivalent
	boolean isBotEquivalent(Expression expr) {
		return BotEval.isBotEquivalent(expr);
	}

	/// @return true iff role expression in equivalent to const wrt locality
	boolean isREquivalent(Expression expr) {
		return sig.topRLocal() ? isTopEquivalent(expr) : isBotEquivalent(expr);
	}

	/// init c'tor
	SyntacticLocalityChecker(TSignature s) {
		super(s);
		TopEval = new TopEquivalenceEvaluator(s);
		BotEval = new BotEquivalenceEvaluator(s);
		TopEval.setBotEval(BotEval);
		BotEval.setTopEval(TopEval);
	}

	// set fields
	/// @return true iff an AXIOM is local wrt defined policy
	boolean local(Axiom axiom) {
		axiom.accept(this);
		return isLocal;
	}

	/// load ontology to a given KB
	public void visitOntology(Ontology ontology) {
		for (Axiom p : ontology.begin()) {
			if (p.isUsed()) {
				p.accept(this);
			}
		}
	}

	public void visit(AxiomDeclaration axiom) {
		isLocal = true;
	}

	public void visit(AxiomEquivalentConcepts axiom) {
		// 1 element => local
		if (axiom.size() == 1) {
			isLocal = true;
			return;
		}
		// axiom is local iff all the classes are either top- or bot-local
		isLocal = false;
		List<ConceptExpression> args = axiom.getArguments();
		if (args.size() > 0) {
			if (isBotEquivalent(args.get(0))) {
				for (int i = 1; i < args.size(); i++) {
					if (!isBotEquivalent(args.get(i))) {
						return;
					}
				}
			} else {
				if (!isTopEquivalent(args.get(0))) {
					return;
				}
				for (int i = 1; i < args.size(); i++) {
					if (!isTopEquivalent(args.get(i))) {
						return;
					}
				}
			}
		}
		isLocal = true;
	}

	public void visit(AxiomDisjointConcepts axiom) {
		// local iff at most 1 concept is not bot-equiv
		boolean hasNBE = false;
		isLocal = true;
		for (ConceptExpression p : axiom.getArguments()) {
			if (!isBotEquivalent(p)) {
				if (hasNBE) {
					isLocal = false;
					break;
				} else {
					hasNBE = true;
				}
			}
		}
	}

	public void visit(AxiomDisjointUnion axiom) {
		isLocal = false;
		boolean topLoc = sig.topCLocal();
		if (!(topLoc ? isTopEquivalent(axiom.getC()) : isBotEquivalent(axiom.getC()))) {
			return;
		}
		boolean topEqDesc = false;
		for (ConceptExpression p : axiom.getArguments()) {
			if (!isBotEquivalent(p)) {
				if (!topLoc) {
					return; // non-local straight away
				}
				if (isTopEquivalent(p)) {
					if (topEqDesc) {
						return; // 2nd top in there -- non-local
					} else {
						topEqDesc = true;
					}
				} else {
					return; // non-local
				}
			}
		}
		isLocal = true;
	}

	public void visit(AxiomEquivalentORoles axiom) {
		isLocal = true;
		if (axiom.size() <= 1) {
			return;
		}
		for (ObjectRoleExpression p : axiom.getArguments()) {
			if (!isREquivalent(p)) {
				isLocal = false;
				break;
			}
		}
	}

	public void visit(AxiomEquivalentDRoles axiom) {
		isLocal = true;
		if (axiom.size() <= 1) {
			return;
		}
		for (DataRoleExpression p : axiom.getArguments()) {
			if (!isREquivalent(p)) {
				isLocal = false;
				break;
			}
		}
	}

	public void visit(AxiomDisjointORoles axiom) {
		isLocal = false;
		if (sig.topRLocal()) {
			return;
		}
		boolean hasNBE = false;
		for (ObjectRoleExpression p : axiom.getArguments()) {
			if (!isREquivalent(p)) {
				if (hasNBE) {
					return; // false here
				} else {
					hasNBE = true;
				}
			}
		}
		isLocal = true;
	}

	public void visit(AxiomDisjointDRoles axiom) {
		isLocal = false;
		if (sig.topRLocal()) {
			return;
		}
		boolean hasNBE = false;
		for (DataRoleExpression p : axiom.getArguments()) {
			if (!isREquivalent(p)) {
				if (hasNBE) {
					return; // false here
				} else {
					hasNBE = true;
				}
			}
		}
		isLocal = true;
	}

	public void visit(AxiomSameIndividuals axiom) {
		isLocal = true;
	}

	public void visit(AxiomDifferentIndividuals axiom) {
		isLocal = true;
	}

	/// there is no such axiom in OWL API, but I hope nobody would use Fairness here
	public void visit(AxiomFairnessConstraint axiom) {
		isLocal = true;
	}

	public void visit(AxiomRoleInverse axiom) {
		isLocal = isREquivalent(axiom.getRole()) && isREquivalent(axiom.getInvRole());
	}

	public void visit(AxiomORoleSubsumption axiom) {
		isLocal = isREquivalent(sig.topRLocal() ? axiom.getRole() : axiom.getSubRole());
	}

	public void visit(AxiomDRoleSubsumption axiom) {
		isLocal = isREquivalent(sig.topRLocal() ? axiom.getRole() : axiom.getSubRole());
	}

	public void visit(AxiomORoleDomain axiom) {
		isLocal = isTopEquivalent(axiom.getDomain());
		if (!sig.topRLocal()) {
			isLocal |= isBotEquivalent(axiom.getRole());
		}
	}

	public void visit(AxiomDRoleDomain axiom) {
		isLocal = isTopEquivalent(axiom.getDomain());
		if (!sig.topRLocal()) {
			isLocal |= isBotEquivalent(axiom.getRole());
		}
	}

	public void visit(AxiomORoleRange axiom) {
		isLocal = isTopEquivalent(axiom.getRange());
		if (!sig.topRLocal()) {
			isLocal |= isBotEquivalent(axiom.getRole());
		}
	}

	public void visit(AxiomDRoleRange axiom) {
		isLocal = isTopDT(axiom.getRange());
		if (!sig.topRLocal()) {
			isLocal |= isBotEquivalent(axiom.getRole());
		}
	}

	public void visit(AxiomRoleTransitive axiom) {
		isLocal = isREquivalent(axiom.getRole());
	}

	public void visit(AxiomRoleReflexive axiom) {
		isLocal = isREquivalent(axiom.getRole());
	}

	public void visit(AxiomRoleIrreflexive axiom) {
		isLocal = !sig.topRLocal();
	}

	public void visit(AxiomRoleSymmetric axiom) {
		isLocal = isREquivalent(axiom.getRole());
	}

	public void visit(AxiomRoleAsymmetric axiom) {
		isLocal = !sig.topRLocal();
	}

	public void visit(AxiomORoleFunctional axiom) {
		isLocal = !sig.topRLocal() && isBotEquivalent(axiom.getRole());
	}

	public void visit(AxiomDRoleFunctional axiom) {
		isLocal = !sig.topRLocal() && isBotEquivalent(axiom.getRole());
	}

	public void visit(AxiomRoleInverseFunctional axiom) {
		isLocal = !sig.topRLocal() && isBotEquivalent(axiom.getRole());
	}

	public void visit(AxiomConceptInclusion axiom) {
		isLocal = isBotEquivalent(axiom.getSubConcept())
				|| isTopEquivalent(axiom.getSupConcept());
	}

	public void visit(AxiomInstanceOf axiom) {
		isLocal = isTopEquivalent(axiom.getC());
	}

	public void visit(AxiomRelatedTo axiom) {
		isLocal = sig.topRLocal() && isTopEquivalent(axiom.getRelation());
	}

	public void visit(AxiomRelatedToNot axiom) {
		isLocal = !sig.topRLocal() && isBotEquivalent(axiom.getRelation());
	}

	public void visit(AxiomValueOf axiom) {
		isLocal = sig.topRLocal() && isTopEquivalent(axiom.getAttribute());
	}

	public void visit(AxiomValueOfNot axiom) {
		isLocal = !sig.topRLocal() && isBotEquivalent(axiom.getAttribute());
	}
}