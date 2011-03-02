package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import static uk.ac.manchester.cs.jfact.kernel.Token.*;

import java.util.ArrayList;
import java.util.List;


import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

import uk.ac.manchester.cs.jfact.helpers.DLTree;
import uk.ac.manchester.cs.jfact.helpers.DLTreeFactory;
import uk.ac.manchester.cs.jfact.kernel.datatype.TDLDataValue;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptAnd;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptDataExactCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptDataExists;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptDataForall;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptDataMaxCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptDataMinCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptDataValue;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptName;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptNot;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptObjectExactCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptObjectExists;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptObjectForall;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptObjectMaxCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptObjectMinCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptObjectSelf;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptObjectValue;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptOneOf;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptOr;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptTop;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataAnd;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataNot;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataOneOf;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataOr;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataRoleBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataRoleName;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataRoleTop;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataTop;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataTypeName;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataTypeRestriction;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLFacetMaxExclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLFacetMaxInclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLFacetMinExclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLFacetMinInclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLIndividualName;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLObjectRoleBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLObjectRoleChain;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLObjectRoleInverse;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLObjectRoleName;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLObjectRoleProjectionFrom;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLObjectRoleProjectionInto;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLObjectRoleTop;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLNAryExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLObjectRoleExpression;
import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitorEx;

public class TExpressionTranslator implements DLExpressionVisitorEx<DLTree> {
	/** tree corresponding to a processing expression */
	//private DLTree tree;
	/** TBox to get access to the named entities */
	private final TBox KB;

	TExpressionTranslator(TBox kb) {
		//setTree(null);
		KB = kb;
	}

	// concept expressions
	public DLTree visit(final TDLConceptTop expr) {
		return DLTreeFactory.createTop();
	}

	public DLTree visit(final TDLConceptBottom expr) {
		return DLTreeFactory.createBottom();
	}

	public DLTree visit(final TDLConceptName expr) {
		return DLTreeFactory.buildTree(new TLexeme(CNAME, KB.getConcept(expr
				.getName())));
	}

	public DLTree visit(final TDLConceptNot expr) {
		return DLTreeFactory.createSNFNot(expr.getConcept().accept(this));
	}

	private List<DLTree> visitArgs(
			TDLNAryExpression<? extends TDLExpression> expr) {
		List<DLTree> args = new ArrayList<DLTree>();
		List<? extends TDLExpression> list = expr.getArguments();
		for (int i = 0; i < list.size(); i++) {
			args.add(list.get(i).accept(this));
		}
		return args;
	}

	public DLTree visit(final TDLConceptAnd expr) {
		return DLTreeFactory.createSNFAnd(visitArgs(expr));
	}

	public DLTree visit(final TDLConceptOr expr) {
		return DLTreeFactory.createSNFOr(visitArgs(expr));
	}

	public DLTree visit(final TDLConceptOneOf expr) {
		return DLTreeFactory.createSNFOr(visitArgs(expr));
	}

	public DLTree visit(final TDLConceptObjectSelf expr) {
		return DLTreeFactory.buildTree(new TLexeme(REFLEXIVE), expr.getOR()
				.accept(this));
	}

	public DLTree visit(final TDLConceptObjectValue expr) {
		return DLTreeFactory.createSNFExists(expr.getOR().accept(this), expr
				.getI().accept(this));
	}

	public DLTree visit(final TDLConceptObjectExists expr) {
		return DLTreeFactory.createSNFExists(expr.getOR().accept(this), expr
				.getConcept().accept(this));
	}

	public DLTree visit(final TDLConceptObjectForall expr) {
		return DLTreeFactory.createSNFForall(expr.getOR().accept(this), expr
				.getConcept().accept(this));
	}

	public DLTree visit(final TDLConceptObjectMinCardinality expr) {
		return DLTreeFactory.createSNFGE(expr.getNumber(),
				expr.getOR().accept(this), expr.getConcept().accept(this));
	}

	public DLTree visit(final TDLConceptObjectMaxCardinality expr) {
		return DLTreeFactory.createSNFLE(expr.getNumber(),
				expr.getOR().accept(this), expr.getConcept().accept(this));
	}

	public DLTree visit(final TDLConceptObjectExactCardinality expr) {
		DLTree le = DLTreeFactory.createSNFLE(expr.getNumber(), expr.getOR()
				.accept(this).copy(), expr.getConcept().accept(this).copy());
		DLTree ge = DLTreeFactory.createSNFGE(expr.getNumber(), expr.getOR()
				.accept(this).copy(), expr.getConcept().accept(this).copy());
		return DLTreeFactory.createSNFAnd(ge, le);
	}

	public DLTree visit(final TDLConceptDataValue expr) {
		return DLTreeFactory.createSNFExists(expr.getDR().accept(this), expr
				.getExpr().accept(this));
	}

	public DLTree visit(final TDLConceptDataExists expr) {
		return DLTreeFactory.createSNFExists(expr.getDR().accept(this), expr
				.getExpr().accept(this));
	}

	public DLTree visit(final TDLConceptDataForall expr) {
		return DLTreeFactory.createSNFForall(expr.getDR().accept(this), expr
				.getExpr().accept(this));
	}

	public DLTree visit(final TDLConceptDataMinCardinality expr) {
		return DLTreeFactory.createSNFGE(expr.getNumber(),
				expr.getDR().accept(this), expr.getExpr().accept(this));
	}

	public DLTree visit(final TDLConceptDataMaxCardinality expr) {
		return DLTreeFactory.createSNFLE(expr.getNumber(),
				expr.getDR().accept(this), expr.getExpr().accept(this));
	}

	public DLTree visit(final TDLConceptDataExactCardinality expr) {
		DLTree le = DLTreeFactory.createSNFLE(expr.getNumber(), expr.getDR()
				.accept(this).copy(), expr.getExpr().accept(this).copy());
		DLTree ge = DLTreeFactory.createSNFGE(expr.getNumber(), expr.getDR()
				.accept(this).copy(), expr.getExpr().accept(this).copy());
		return DLTreeFactory.createSNFAnd(ge, le);
	}

	// individual expressions
	public DLTree visit(final TDLIndividualName expr) {
		return DLTreeFactory.buildTree(new TLexeme(INAME, KB.getIndividual(expr
				.getName())));
	}

	// object role expressions
	public DLTree visit(final TDLObjectRoleTop expr) {
		throw new ReasonerInternalException(
				"Unsupported expression 'top object role' in transformation");
	}

	public DLTree visit(final TDLObjectRoleBottom expr) {
		throw new ReasonerInternalException(
				"Unsupported expression 'bottom object role' in transformation");
	}

	public DLTree visit(final TDLObjectRoleName expr) {
		return DLTreeFactory.buildTree(new TLexeme(RNAME, KB.getORM()
				.ensureRoleName(expr.getName())));
	}

	public DLTree visit(final TDLObjectRoleInverse expr) {
		return DLTreeFactory.createInverse(expr.getOR().accept(this));
	}

	public DLTree visit(final TDLObjectRoleChain expr) {
		List<TDLObjectRoleExpression> l = new ArrayList<TDLObjectRoleExpression>(
				expr.getArguments());
		if (l.size() == 0) {
			throw new ReasonerInternalException(
					"Unsupported expression 'empty role chain' in transformation");
		}
		DLTree acc = l.get(0).accept(this);
		for (int i = 1; i < l.size(); i++) {
			//TODO this is still a binary tree while it should be n-ary with enforced order
			acc = DLTreeFactory.buildTree(new TLexeme(RCOMPOSITION), acc, l
					.get(i).accept(this));
		}
		return acc;
	}

	public DLTree visit(final TDLObjectRoleProjectionFrom expr) {
		return DLTreeFactory.buildTree(new TLexeme(PROJFROM), expr.getOR()
				.accept(this), expr.getConcept().accept(this));
	}

	public DLTree visit(final TDLObjectRoleProjectionInto expr) {
		return DLTreeFactory.buildTree(new TLexeme(PROJINTO), expr.getOR()
				.accept(this), expr.getConcept().accept(this));
	}

	// data role expressions
	public DLTree visit(final TDLDataRoleTop expr) {
		throw new ReasonerInternalException(
				"Unsupported expression 'top data role' in transformation");
	}

	public DLTree visit(final TDLDataRoleBottom expr) {
		throw new ReasonerInternalException(
				"Unsupported expression 'bottom data role' in transformation");
	}

	public DLTree visit(final TDLDataRoleName expr) {
		return DLTreeFactory.buildTree(new TLexeme(DNAME, KB.getDRM()
				.ensureRoleName(expr.getName())));
	}

	// data expressions
	public DLTree visit(final TDLDataTop expr) {
		return DLTreeFactory.createTop();
	}

	public DLTree visit(final TDLDataBottom expr) {
		return DLTreeFactory.createBottom();
	}

	public DLTree visit(final TDLDataTypeName expr) {
		return DLTreeFactory.wrap(expr);
	}

	public DLTree visit(final TDLDataTypeRestriction expr) {
		return DLTreeFactory.createSNFAnd(visitArgs(expr));
	}

	public DLTree visit(final TDLDataValue expr) {
		// process type
		return KB.getDataTypeCenter().getDataValue(expr.getName(),
				expr.getExpr());
	}

	public DLTree visit(final TDLDataNot expr) {
		return DLTreeFactory.createSNFNot(expr.getExpr().accept(this));
	}

	public DLTree visit(final TDLDataAnd expr) {
		return DLTreeFactory.createSNFAnd(visitArgs(expr));
	}

	public DLTree visit(final TDLDataOr expr) {
		return DLTreeFactory.createSNFOr(visitArgs(expr));
	}

	public DLTree visit(final TDLDataOneOf expr) {
		return DLTreeFactory.createSNFOr(visitArgs(expr));
	}

	// facets
	public DLTree visit(final TDLFacetMinInclusive expr) {
		return KB.getDataTypeCenter().getIntervalFacetExpr(
				expr.getExpr().accept(this), true, false);
	}

	public DLTree visit(final TDLFacetMinExclusive expr) {
		return KB.getDataTypeCenter().getIntervalFacetExpr(
				expr.getExpr().accept(this), true, true);
	}

	public DLTree visit(final TDLFacetMaxInclusive expr) {
		return KB.getDataTypeCenter().getIntervalFacetExpr(
				expr.getExpr().accept(this), false, false);
	}

	public DLTree visit(final TDLFacetMaxExclusive expr) {
		return KB.getDataTypeCenter().getIntervalFacetExpr(
				expr.getExpr().accept(this), false, true);
	}
}