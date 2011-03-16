package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.kernel.TExpressionManager.isUniversalRole;
import static uk.ac.manchester.cs.jfact.kernel.TRole.resolveRole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

import uk.ac.manchester.cs.jfact.helpers.DLTree;
import uk.ac.manchester.cs.jfact.helpers.DLTreeFactory;
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
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLIndividualExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLRoleExpression;
import uk.ac.manchester.cs.jfact.visitors.DLAxiomVisitor;

public class TOntologyLoader implements DLAxiomVisitor {
	/** KB to load the ontology */
	private final TBox kb;
	/** Transforms TDLExpression hierarchy to the DLTree */
	private final TExpressionTranslator ETrans;
	/** temporary vector for arguments of TBox n-ary axioms */
	private final List<DLTree> ArgList = new ArrayList<DLTree>();

	/** get role by the DLTree; throw exception if unable */
	private TRole getRole(final TDLRoleExpression r, final String reason) {
		try {
			return resolveRole(r.accept(ETrans));
		} catch (OWLRuntimeException e) {
			throw new ReasonerInternalException(reason + "\t" + e.getMessage(), e);
		}
	}

	/** get an individual be the DLTree; throw exception if unable */
	public TIndividual getIndividual(final TDLIndividualExpression I, final String reason) {
		DLTree i = I.accept(ETrans);
		if (i == null) {
			throw new ReasonerInternalException(reason);
		}
		return (TIndividual) kb.getCI(i);
	}

	/**
	 * ensure that the expression EXPR has its named entities linked to the KB
	 * ones
	 */
	public void ensureNames(final TDLExpression Expr) {
		assert Expr != null; // FORNOW
	}

	/** prepare arguments for the [begin,end) interval */
	private <T extends TDLExpression> void prepareArgList(Collection<T> c) {
		ArgList.clear();
		for (T t : c) {
			ensureNames(t);
			ArgList.add(t.accept(ETrans));
		}
	}

	public void visit(TDLAxiomDeclaration axiom) {
		ensureNames(axiom.getDeclaration());
		axiom.getDeclaration().accept(ETrans); // names in the KB
	}

	// n-ary axioms
	public void visit(TDLAxiomEquivalentConcepts axiom) {
		prepareArgList(axiom.getArguments());
		kb.processEquivalentC(ArgList);
	}

	public void visit(TDLAxiomDisjointConcepts axiom) {
		prepareArgList(axiom.getArguments());
		kb.processDisjointC(ArgList);
	}

	public void visit(TDLAxiomEquivalentORoles axiom) {
		prepareArgList(axiom.getArguments());
		kb.processEquivalentR(ArgList);
	}

	public void visit(TDLAxiomEquivalentDRoles axiom) {
		prepareArgList(axiom.getArguments());
		kb.processEquivalentR(ArgList);
	}

	public void visit(TDLAxiomDisjointORoles axiom) {
		prepareArgList(axiom.getArguments());
		kb.processDisjointR(ArgList);
	}

	public void visit(TDLAxiomDisjointDRoles axiom) {
		prepareArgList(axiom.getArguments());
		kb.processDisjointR(ArgList);
	}

	public void visit(TDLAxiomDisjointUnion axiom) {
		// first make a disjoint axiom
		prepareArgList(axiom.getArguments());
		kb.processDisjointC(ArgList);
		// now define C as a union-of axiom
		ArgList.clear();
		ensureNames(axiom.getC());
		ArgList.add(axiom.getC().accept(ETrans));
		List<DLTree> list = new ArrayList<DLTree>();
		for (TDLExpression p : axiom.getArguments()) {
			list.add(p.accept(ETrans));
		}
		ArgList.add(DLTreeFactory.createSNFOr(list));
		kb.processEquivalentC(ArgList);
	}

	public void visit(TDLAxiomSameIndividuals axiom) {
		prepareArgList(axiom.getArguments());
		kb.processSame(ArgList);
	}

	public void visit(TDLAxiomDifferentIndividuals axiom) {
		prepareArgList(axiom.getArguments());
		kb.processDifferent(ArgList);
	}

	public void visit(TDLAxiomFairnessConstraint axiom) {
		prepareArgList(axiom.getArguments());
		kb.setFairnessConstraintDLTrees(ArgList);
	}

	// role axioms
	public void visit(TDLAxiomRoleInverse axiom) {
		ensureNames(axiom.getRole());
		ensureNames(axiom.getInvRole());
		TRole R = getRole(axiom.getRole(), "Role expression expected in Role Inverse axiom");
		TRole iR = getRole(axiom.getInvRole(), "Role expression expected in Role Inverse axiom");
		kb.getRM(R).addRoleSynonym(iR.inverse(), R);
	}

	public void visit(TDLAxiomORoleSubsumption axiom) {
		ensureNames(axiom.getRole());
		ensureNames(axiom.getSubRole());
		DLTree Sub = axiom.getSubRole().accept(ETrans);
		TRole R = getRole(axiom.getRole(), "Role expression expected in Object Roles Subsumption axiom");
		kb.getRM(R).addRoleParent(Sub, R);
	}

	public void visit(TDLAxiomDRoleSubsumption axiom) {
		ensureNames(axiom.getRole());
		ensureNames(axiom.getSubRole());
		TRole R = getRole(axiom.getRole(), "Role expression expected in Data Roles Subsumption axiom");
		TRole S = getRole(axiom.getSubRole(), "Role expression expected in Data Roles Subsumption axiom");
		kb.getDRM().addRoleParent(S, R);
	}

	public void visit(TDLAxiomORoleDomain axiom) {
		ensureNames(axiom.getRole());
		ensureNames(axiom.getDomain());
		getRole(axiom.getRole(), "Role expression expected in Object Role Domain axiom").setDomain(axiom.getDomain().accept(ETrans));
	}

	public void visit(TDLAxiomDRoleDomain axiom) {
		ensureNames(axiom.getRole());
		ensureNames(axiom.getDomain());
		getRole(axiom.getRole(), "Role expression expected in Data Role Domain axiom").setDomain(axiom.getDomain().accept(ETrans));
	}

	public void visit(TDLAxiomORoleRange axiom) {
		ensureNames(axiom.getRole());
		ensureNames(axiom.getRange());
		getRole(axiom.getRole(), "Role expression expected in Object Role Range axiom").setRange(axiom.getRange().accept(ETrans));
	}

	public void visit(TDLAxiomDRoleRange axiom) {
		ensureNames(axiom.getRole());
		ensureNames(axiom.getRange());
		getRole(axiom.getRole(), "Role expression expected in Data Role Range axiom").setRange(axiom.getRange().accept(ETrans));
	}

	public void visit(TDLAxiomRoleTransitive axiom) {
		ensureNames(axiom.getRole());
		if (!isUniversalRole(axiom.getRole())) {
			getRole(axiom.getRole(), "Role expression expected in Role Transitivity axiom").setTransitive();
		}
	}

	public void visit(TDLAxiomRoleReflexive axiom) {
		ensureNames(axiom.getRole());
		if (!isUniversalRole(axiom.getRole())) {
			getRole(axiom.getRole(), "Role expression expected in Role Reflexivity axiom").setReflexive();
		}
	}

	public void visit(TDLAxiomRoleIrreflexive axiom) {
		ensureNames(axiom.getRole());
		if (isUniversalRole(axiom.getRole())) {
			throw new InconsistentOntologyException();
		}
		TRole R = getRole(axiom.getRole(), "Role expression expected in Role Irreflexivity axiom");
		R.setDomain(DLTreeFactory.createSNFNot(DLTreeFactory.buildTree(new TLexeme(Token.REFLEXIVE), axiom.getRole().accept(ETrans))));
		R.setIrreflexive(true);
	}

	public void visit(TDLAxiomRoleSymmetric axiom) {
		ensureNames(axiom.getRole());
		if (!isUniversalRole(axiom.getRole())) {
			TRole R = getRole(axiom.getRole(), "Role expression expected in Role Symmetry axiom");
			R.setSymmetric(true);
			kb.getORM().addRoleParent(R, R.inverse());
		}
	}

	public void visit(TDLAxiomRoleAsymmetric axiom) {
		ensureNames(axiom.getRole());
		if (isUniversalRole(axiom.getRole())) {
			throw new InconsistentOntologyException();
		}
		TRole R = getRole(axiom.getRole(), "Role expression expected in Role Asymmetry axiom");
		R.setAsymmetric(true);
		kb.getORM().addDisjointRoles(R, R.inverse());
	}

	public void visit(TDLAxiomORoleFunctional axiom) {
		ensureNames(axiom.getRole());
		if (isUniversalRole(axiom.getRole())) {
			throw new InconsistentOntologyException();
		}
		getRole(axiom.getRole(), "Role expression expected in Object Role Functionality axiom").setFunctional();
	}

	public void visit(TDLAxiomDRoleFunctional axiom) {
		ensureNames(axiom.getRole());
		if (isUniversalRole(axiom.getRole())) {
			throw new InconsistentOntologyException();
		}
		getRole(axiom.getRole(), "Role expression expected in Data Role Functionality axiom").setFunctional();
	}

	public void visit(TDLAxiomRoleInverseFunctional axiom) {
		ensureNames(axiom.getRole());
		if (isUniversalRole(axiom.getRole())) {
			throw new InconsistentOntologyException();
		}
		getRole(axiom.getRole(), "Role expression expected in Role Inverse Functionality axiom").inverse().setFunctional();
	}

	// concept/individual axioms
	public void visit(TDLAxiomConceptInclusion axiom) {
		ensureNames(axiom.getSubC());
		ensureNames(axiom.getSupC());
		DLTree C = axiom.getSubC().accept(ETrans);
		DLTree D = axiom.getSupC().accept(ETrans);
		kb.addSubsumeAxiom(C, D);
	}

	public void visit(TDLAxiomInstanceOf axiom) {
		ensureNames(axiom.getIndividual());
		ensureNames(axiom.getC());
		getIndividual(axiom.getIndividual(), "Individual expected in Instance axiom");
		DLTree I = axiom.getIndividual().accept(ETrans);
		DLTree C = axiom.getC().accept(ETrans);
		kb.addSubsumeAxiom(I, C);
	}

	public void visit(TDLAxiomRelatedTo axiom) {
		ensureNames(axiom.getIndividual());
		ensureNames(axiom.getRelation());
		ensureNames(axiom.getRelatedIndividual());
		if (!isUniversalRole(axiom.getRelation())) // nothing to do for universal role
		{
			TIndividual I = getIndividual(axiom.getIndividual(), "Individual expected in Related To axiom");
			TRole R = getRole(axiom.getRelation(), "Role expression expected in Related To axiom");
			TIndividual J = getIndividual(axiom.getRelatedIndividual(), "Individual expected in Related To axiom");
			kb.RegisterIndividualRelation(I, R, J);
		}
	}

	public void visit(TDLAxiomRelatedToNot axiom) {
		ensureNames(axiom.getIndividual());
		ensureNames(axiom.getRelation());
		ensureNames(axiom.getRelatedIndividual());
		if (isUniversalRole(axiom.getRelation())) {
			throw new InconsistentOntologyException();
		}
		// make sure everything is consistent
		getIndividual(axiom.getIndividual(), "Individual expected in Related To Not axiom");
		getIndividual(axiom.getRelatedIndividual(), "Individual expected in Related To Not axiom");
		// make an axiom i:AR.\neg{j}
		kb.addSubsumeAxiom(axiom.getIndividual().accept(ETrans), DLTreeFactory.createSNFForall(axiom.getRelation().accept(ETrans), DLTreeFactory.createSNFNot(axiom.getRelatedIndividual().accept(ETrans))));
	}

	public void visit(TDLAxiomValueOf axiom) {
		ensureNames(axiom.getIndividual());
		ensureNames(axiom.getAttribute());
		getIndividual(axiom.getIndividual(), "Individual expected in Value Of axiom");
		// FIXME!! think about ensuring the value
		// make an axiom i:EA.V
		kb.addSubsumeAxiom(axiom.getIndividual().accept(ETrans), DLTreeFactory.createSNFExists(axiom.getAttribute().accept(ETrans), axiom.getValue().accept(ETrans)));
	}

	public void visit(TDLAxiomValueOfNot axiom) {
		ensureNames(axiom.getIndividual());
		ensureNames(axiom.getAttribute());
		getIndividual(axiom.getIndividual(), "Individual expected in Value Of Not axiom");
		// FIXME!! think about ensuring the value
		if (isUniversalRole(axiom.getAttribute())) {
			throw new InconsistentOntologyException();
		}
		// make an axiom i:AA.\neg V
		kb.addSubsumeAxiom(axiom.getIndividual().accept(ETrans), DLTreeFactory.createSNFForall(axiom.getAttribute().accept(ETrans), DLTreeFactory.createSNFNot(axiom.getValue().accept(ETrans))));
	}

	public TOntologyLoader(TBox KB) {
		kb = KB;
		ETrans = new TExpressionTranslator(KB);
	}

	/** load ontology to a given KB */
	public void visitOntology(TOntology ontology) {
		for (TDLAxiom p : ontology.begin()) {
			if (p.isUsed()) {
				p.accept(this);
			}
		}
	}
}