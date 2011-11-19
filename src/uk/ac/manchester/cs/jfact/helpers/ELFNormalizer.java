package uk.ac.manchester.cs.jfact.helpers;

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.cs.jfact.kernel.ExpressionManager;
import uk.ac.manchester.cs.jfact.kernel.Ontology;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptAnd;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptName;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectExists;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptTop;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomConceptInclusion;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomDisjointConcepts;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomDisjointUnion;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomEquivalentConcepts;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomEquivalentORoles;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomORoleSubsumption;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.AxiomRoleTransitive;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Axiom;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ConceptExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Expression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ObjectRoleExpression;
import uk.ac.manchester.cs.jfact.visitors.DLAxiomVisitorAdapter;

public class ELFNormalizer extends DLAxiomVisitorAdapter {
	/// expression manager to build aux expressions
	ExpressionManager pEM;
	//TLISPOntologyPrinter LP;
	/// set of new/procesed axioms
	List<Axiom> Axioms = new ArrayList<Axiom>();
	/// index of a freah variable
	int index;
	/// true iff the axiom was changed after visiting
	boolean changed;
	/// true iff RHS is in a form \ER.C
	boolean eRHS;

	/// process the axiom and mark it unused if necessary
	void v(Axiom ax) {
		//		std::cout << "Processing ";
		//		ax.accept(LP);
		ax.accept(this);
		if (changed) {
			ax.setUsed(false);
		}
	}

	/// add axiom to a list
	void addAxiom(Axiom ax) {
		//		std::cout << "Adding ";
		//		ax.accept(LP);
		Axioms.add(ax);
	}

	/// create a new name
	ConceptExpression buildFreshName() {
		//TODO check: should the string start with a space?
		return pEM.concept(" ELF_aux_" + index);
	}

	/// split C [= D1 \and \and Dn into C [= D1, C [= Dn
	boolean splitAndRHS(ConceptExpression C, ConceptAnd D) {
		if (D == null) {
			return false;
		}
		// make new axioms
		for (ConceptExpression p : D.getArguments()) {
			addAxiom(new AxiomConceptInclusion(C, p));
		}
		return true;
	}

	/// transform RHS into normalized one. @return a normalized RHS. Set the eRHS flag if it is an existential
	ConceptExpression transformExists(ConceptExpression D) {
		eRHS = false;
		// RHS now contains only Bot, A, \E R.C
		if (!(D instanceof ConceptObjectExists)) {
			assert D instanceof ConceptName || D instanceof ConceptBottom
					|| D instanceof ConceptTop; // for LHS
			return D;
		}
		ConceptObjectExists exists = (ConceptObjectExists) D;
		// check the filler
		ConceptExpression C = exists.getConcept();
		// if the filler is Bot, then the whole expression is bottom
		if (C instanceof ConceptBottom) {
			return C;
		}
		// if the filler is Top or CN then keep the expression
		eRHS = true;
		if (C instanceof ConceptName || C instanceof ConceptTop) {
			return D;
		}
		// complex filler: replace C with new B and the axiom B = C
		ConceptExpression B = buildFreshName();
		List<Expression> args = new ArrayList<Expression>();
		args.add(B);
		args.add(C);
		addAxiom(new AxiomEquivalentConcepts(args));
		return pEM.exists(exists.getOR(), B);
	}

	/// transform conjunction into the binary one with named concepts in it; simplify
	ConceptExpression normalizeLHSAnd(ConceptAnd C) {
		if (C == null) {
			return null;
		}
		List<ConceptExpression> args = new ArrayList<ConceptExpression>();
		// check for bottom argument
		for (ConceptExpression p : C.getArguments()) {
			if (p instanceof ConceptBottom) {
				return p; // all And is equivalent to bottom
			}
		}
		// preprocess conjunctions
		boolean change = false;
		for (ConceptExpression p : C.getArguments()) {
			if (p instanceof ConceptTop) {
				change = true;
				continue; // skip Tops there
			}
			if (p instanceof ConceptName) {
				args.add(p); // keep concept name
			} else {
				// complex expression -- replace with new name
				ConceptExpression A = buildFreshName();
				// it's enough to use implication here
				addAxiom(new AxiomConceptInclusion(p, A));
				args.add(A);
				change = true;
			}
		}
		// check already-binary thing
		if (!change && args.size() == 2 && !eRHS) {
			return C;
		}
		// make conjunction binary
		//		std::cout << "Args.size()==" << args.size() << "\n";
		ConceptExpression B = args.get(0);
		// check the corner case: singleton conjunction
		if (args.size() == 1) {
			return B;
		}
		int size = args.size();
		if (!eRHS) {
			size--;
		}
		// now we have B1 and ... and Bn [= X
		int i;
		for (i = 1; i < size; i++) {
			// transform into B1 and B2 [= A, A and... Bn [= X
			ConceptExpression A = buildFreshName();
			addAxiom(new AxiomConceptInclusion(pEM.and(B, args.get(i)), A));
			B = A;
		}
		// now B and q=q_last are the only conjuncts
		return eRHS ? B : pEM.and(B, args.get(i));
	}

	/// transform LHS into normalized one. @return a normalized LHS
	ConceptExpression transformLHS(ConceptExpression C) {
		// here C is Top, A, AND and Exists
		// first normalize LHS And to contain only 2 names (or less)
		ConceptExpression And = normalizeLHSAnd((ConceptAnd) C);
		if (And != null) {
			C = And;
		}
		if (C instanceof ConceptAnd) {
			return C;
		}
		// LHS is Top,Bot,A and exists
		boolean flag = eRHS;
		C = transformExists(C); // normalize exists
		if (!(C instanceof ConceptObjectExists)) {
			return C;
		}
		if (flag) // need intermediate var: can't have ER.X [= ES.D
		{
			// make C=ER.X [= B for fresh B
			ConceptExpression B = buildFreshName();
			addAxiom(new AxiomConceptInclusion(C, B));
			return B;
		}
		// here ER.X [= A
		return C;
	}

	// need only do something with a very few axioms as others doesn't present here
	/// replace it with C0 [= Ci, Ci [= C0
	@Override
	public void visit(AxiomEquivalentConcepts axiom) {
		List<ConceptExpression> list = axiom.getArguments();
		ConceptExpression C = list.get(0);
		for (int i = 1; i < list.size(); i++) {
			addAxiom(new AxiomConceptInclusion(C, list.get(i)));
			addAxiom(new AxiomConceptInclusion(list.get(i), C));
		}
		changed = true;
	}

	/// replace with Ci \and Cj [= \bot for 0 <= i < j < n
	@Override
	public void visit(AxiomDisjointConcepts axiom) {
		ConceptExpression bot = pEM.bottom();
		final List<ConceptExpression> arguments = axiom.getArguments();
		for (int i = 0; i < arguments.size(); i++) {
			// FIXME!! replace with new var strait away if necessary
			ConceptExpression C = arguments.get(i);
			for (int j = i + 1; j < arguments.size(); j++) {
				addAxiom(new AxiomConceptInclusion(pEM.and(C, arguments.get(j)), bot));
			}
		}
		changed = true;
	}

	/// the only legal one contains a single element, so is C = D
	@Override
	public void visit(AxiomDisjointUnion axiom) {
		changed = true; // replace it anyway
		switch (axiom.size()) {
			case 0: // strange, but...
				return;
			case 1: // single element, use C=D processing
				addAxiom(new AxiomConceptInclusion(axiom.getC(), axiom.getArguments()
						.get(0)));
				addAxiom(new AxiomConceptInclusion(axiom.getArguments().get(0),
						axiom.getC()));
				break;
			default: // impossible here
				throw new UnreachableSituationException();
		}
	}

	/// normalize equivalence as a number of subsumptions R0 [= Ri, Ri [= R0
	@Override
	public void visit(AxiomEquivalentORoles axiom) {
		final List<ObjectRoleExpression> arguments = axiom.getArguments();
		ObjectRoleExpression R = arguments.get(0);
		for (int i = 1; i < arguments.size(); i++) {
			addAxiom(new AxiomORoleSubsumption(R, arguments.get(i)));
			addAxiom(new AxiomORoleSubsumption(arguments.get(i), R));
		}
		changed = true;
	}

	/// already canonical
	@Override
	public void visit(AxiomORoleSubsumption axiom) {
		changed = false;
	}

	/// normalize transitivity as role inclusion
	@Override
	public void visit(AxiomRoleTransitive axiom) {
		ObjectRoleExpression R = axiom.getRole();
		addAxiom(new AxiomORoleSubsumption(pEM.compose(R, R), R));
		changed = true;
	}

	/// all the job is done here
	@Override
	public void visit(AxiomConceptInclusion axiom) {
		ConceptExpression C = axiom.getSubConcept(), D = axiom.getSupConcept();
		// skip tautologies
		changed = true;
		// \bot [= D is skipped
		if (C instanceof ConceptBottom) {
			return;
		}
		// C [= \top is skipped
		if (D instanceof ConceptTop) {
			return;
		}
		// split the RHS if necessary
		if (splitAndRHS(C, (ConceptAnd) D)) {
			return;
		}
		// do the transformation itself
		// RHS now contains only Bot, A, \E R.C
		ConceptExpression newD = transformExists(D);
		ConceptExpression newC = transformLHS(C);
		if (newC == C && newD == D) {
			changed = false; // nothing changed
		} else // not add axiom
		if (!(newC instanceof ConceptBottom)) {
			addAxiom(new AxiomConceptInclusion(newC, newD));
		}
	}

	@Override
	public void visitOntology(Ontology ontology) {
		for (Axiom p : ontology.begin()) {
			if (p.isUsed()) {
				v(p);
			}
		}
		// will add to Axioms during the process, so can't use iterators
		for (int i = 0; i < Axioms.size(); ++i) {
			v(Axioms.get(i));
		}
		for (int i = 0; i < Axioms.size();) {
			if (Axioms.get(i).isUsed()) {
				ontology.add(Axioms.get(i));
				i++;
			} else {
				Axioms.remove(i);
			}
		}
	}

	public ELFNormalizer(ExpressionManager p) {
		pEM = p;
		//LP(std::cout);
		index = 0;
	}
}