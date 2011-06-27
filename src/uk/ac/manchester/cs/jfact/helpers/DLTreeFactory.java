package uk.ac.manchester.cs.jfact.helpers;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.kernel.ClassifiableEntry.resolveSynonym;
import static uk.ac.manchester.cs.jfact.kernel.Token.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import uk.ac.manchester.cs.jfact.kernel.ClassifiableEntry;
import uk.ac.manchester.cs.jfact.kernel.Concept;
import uk.ac.manchester.cs.jfact.kernel.Lexeme;
import uk.ac.manchester.cs.jfact.kernel.NamedEntry;
import uk.ac.manchester.cs.jfact.kernel.Role;
import uk.ac.manchester.cs.jfact.kernel.Token;

public final class DLTreeFactory {
	private static final EnumSet<Token> snfCalls = EnumSet.of(TOP, BOTTOM,
			CNAME, INAME, RNAME, DNAME, DATAEXPR, NOT, INV, AND, FORALL, LE,
			SELF, RCOMPOSITION, PROJFROM, PROJINTO);

	/** create BOTTOM element */
	public static DLTree createBottom() {
		return new LEAFDLTree(new Lexeme(BOTTOM));
	}

	public static DLTree createInverse(DLTree R) {
		assert R != null;
		if (R.token() == INV) {
			DLTree p = R.getChild().copy();
			return p;
		}
		if (R.token() == RNAME) {
			return new ONEDLTree(new Lexeme(INV), R);
		}
		throw new UnreachableSituationException();
	}

	/** build a construction in the form AND (\neg q_i) */
	public static DLTree buildDisjAux(List<DLTree> beg) {
		List<DLTree> args = new ArrayList<DLTree>(beg.size());
		for (DLTree i : beg) {
			args.add(DLTreeFactory.createSNFNot(i.copy()));
		}
		return DLTreeFactory.createSNFAnd(args);
	}

	public static DLTree createSNFAnd(DLTree C, DLTree D) {
		if (C == null) {
			return D;
		}
		if (D == null) {
			return C;
		}
		if (C.isTOP() || D.isBOTTOM()) {
			return D;
		}
		if (D.isTOP() || C.isBOTTOM()) {
			return C;
		}
		return new NDLTree(new Lexeme(AND), C, D);
	}

	public static DLTree createSNFAnd(Collection<DLTree> collection) {
		if (collection.size() == 0) {
			return createTop();
		}
		if (collection.size() == 1) {
			return collection.iterator().next();
		}
		List<DLTree> l = new ArrayList<DLTree>();
		for (DLTree d : collection) {
			if (d == null) {
				continue;
			}
			if (d.isBOTTOM()) {
				return createBottom();
			}
			if (d.isAND()) {
				l.addAll(d.getChildren());
			} else {
				l.add(d);
			}
		}
		if (l.size() == 0) {
			return createTop();
		}
		if (l.size() == 1) {
			return l.get(0);
		}
		return new NDLTree(new Lexeme(AND), l);
	}

	public static DLTree createSNFAnd(Collection<DLTree> collection,
			DLTree ancestor) {
		boolean hasTop = false;
		List<DLTree> l = new ArrayList<DLTree>();
		for (DLTree d : collection) {
			if (d.isTOP()) {
				hasTop = true;
			}
			if (d.isBOTTOM()) {
				return createBottom();
			}
			if (d.isAND()) {
				l.addAll(d.getChildren());
			} else {
				l.add(d);
			}
		}
		if (hasTop && l.size() == 0) {
			return createTop();
		}
		if (l.size() == collection.size()) {
			// no changes, return the ancestor
			return ancestor;
		}
		return new NDLTree(new Lexeme(AND), l);
	}

	/** create existential restriction of given formulas (\ER.C) */
	public static DLTree createSNFExists(DLTree R, DLTree C) {
		// \ER.C . \not\AR.\not C
		return createSNFNot(createSNFForall(R, createSNFNot(C)));
	}

	public static DLTree createSNFForall(DLTree R, DLTree C) {
		if (C.isTOP()) {
			return C;
		} else if (Role.resolveRole(R).isBottom()) {
			return createTop();
		} else {
			return new TWODLTree(new Lexeme(FORALL), R, C);
		}
	}

	/** create at-most (LE) restriction of given formulas (<= n R.C) */
	public static DLTree createSNFLE(int n, DLTree R, DLTree C) {
		if (C.isBOTTOM()) { // <= n R.F -> T;
			return createTop();
		}
		if (n == 0) {
			return createSNFForall(R, createSNFNot(C));
		}
		if (Role.resolveRole(R).isBottom()) { // <=n Bot.C = T
			return createTop();
		}
		return new TWODLTree(new Lexeme(LE, n), R, C);
	}

	public static DLTree createSNFGE(int n, DLTree R, DLTree C) {
		if (n == 0) {
			return createTop();
		}
		if (C.isBOTTOM()) {
			return C;
		} else {
			return createSNFNot(createSNFLE(n - 1, R, C));
		}
	}

	public static DLTree createSNFNot(DLTree C) {
		assert C != null;
		if (C.isBOTTOM()) {
			// \not F = T
			return createTop();
		}
		if (C.isTOP()) {
			// \not T = F
			return createBottom();
		}
		if (C.token() == NOT) {
			// \not\not C = C
			return C.getChild().copy();
		}
		// general case
		return new ONEDLTree(new Lexeme(NOT), C);
	}

	public static DLTree createSNFNot(DLTree C, DLTree ancestor) {
		assert C != null;
		if (C.isBOTTOM()) {
			// \not F = T
			return createTop();
		}
		if (C.isTOP()) {
			// \not T = F
			return createBottom();
		}
		if (C.token() == NOT) {
			// \not\not C = C
			return C.getChild().copy();
		}
		// general case
		return ancestor;
	}

	/** create disjunction of given formulas */
	public static DLTree createSNFOr(Collection<DLTree> C) {
		// C\or D . \not(\not C\and\not D)
		List<DLTree> list = new ArrayList<DLTree>();
		for (DLTree d : C) {
			list.add(createSNFNot(d));
		}
		return createSNFNot(createSNFAnd(list));
	}

	/** create TOP element */
	public static DLTree createTop() {
		return new LEAFDLTree(new Lexeme(TOP));
	}

	public static DLTree inverseComposition(final DLTree tree) {
		//XXX this needs to be checked with a proper test
		// see rolemaster.cpp, inverseComposition
		if (tree.token() == RCOMPOSITION) {
			return tree.accept(new ReverseCloningVisitor());
		} else {
			return new LEAFDLTree(new Lexeme(RNAME, Role.resolveRole(tree)
					.inverse()));
		}
	}

	/** get DLTree by a given TDE */
	public static DLTree wrap(NamedEntry t) {
		return new LEAFDLTree(new Lexeme(Token.DATAEXPR, t));
	}

	/** get TDE by a given DLTree */
	public static NamedEntry unwrap(final DLTree t) {
		return t.elem().getNE();
	}

	public static DLTree buildTree(Lexeme t, DLTree t1, DLTree t2) {
		return new TWODLTree(t, t1, t2);
	}

	public static DLTree buildTree(Lexeme t, DLTree t1) {
		return new ONEDLTree(t, t1);
	}

	public static DLTree buildTree(Lexeme t) {
		return new LEAFDLTree(t);
	}

	// check if DL tree is a (data)role name
	private static boolean isRName(final DLTree t) {
		if (t == null) {
			return false;
		}
		if (t.token() == RNAME || t.token() == DNAME) {
			return true;
		}
		return false;
	}

	/** check whether T is an expression in the form (atmost 1 RNAME) */
	public static boolean isFunctionalExpr(final DLTree t, final NamedEntry R) {
		return t != null && t.token() == LE
				&& R.equals(t.getLeft().elem().getNE())
				&& t.elem().getData() == 1 && t.getRight().isTOP();
	}

	public static boolean isSNF(final DLTree t) {
		if (t == null) {
			return true;
		}
		if (snfCalls.contains(t.token())) {
			return isSNF(t.getLeft()) && isSNF(t.getRight());
		}
		return false;
	}

	public static boolean isSubTree(final DLTree t1, final DLTree t2) {
		if (t1 == null || t1.isTOP()) {
			return true;
		}
		if (t2 == null) {
			return false;
		}
		if (t1.isAND()) {
			for (DLTree t : t1.getChildren()) {
				if (!isSubTree(t, t2)) {
					return false;
				}
			}
			return true;
		}
		if (t2.isAND()) {
			for (DLTree t : t2.getChildren()) {
				if (isSubTree(t1, t)) {
					return true;
				}
			}
			return false;
		}
		return t1.equals(t2);
	}

	/** check whether T is U-Role */
	public static boolean isUniversalRole(final DLTree t) {
		return isRName(t) && t.elem().getNE().isTop();
	}

	public static boolean replaceSynonymsFromTree(DLTree desc) {
		if (desc == null) {
			return false;
		}
		if (desc.isName()) {
			ClassifiableEntry entry = (ClassifiableEntry) desc.elem.getNE();
			if (entry.isSynonym()) {
				entry = resolveSynonym(entry);
				if (entry.isTop()) {
					desc.elem = new Lexeme(TOP);
				} else if (entry.isBottom()) {
					desc.elem = new Lexeme(BOTTOM);
				} else {
					desc.elem = new Lexeme(
							((Concept) entry).isSingleton() ? INAME : CNAME,
							entry);
				}
				return true;
			} else {
				return false;
			}
		} else {
			boolean ret = false;
			for (DLTree d : desc.getChildren()) {
				ret |= replaceSynonymsFromTree(d);
			}
			return ret;
		}
	}
}
