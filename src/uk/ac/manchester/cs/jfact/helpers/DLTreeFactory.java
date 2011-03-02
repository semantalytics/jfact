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
import uk.ac.manchester.cs.jfact.kernel.TConcept;
import uk.ac.manchester.cs.jfact.kernel.TLexeme;
import uk.ac.manchester.cs.jfact.kernel.TNamedEntry;
import uk.ac.manchester.cs.jfact.kernel.TRole;
import uk.ac.manchester.cs.jfact.kernel.Token;


public class DLTreeFactory {
	private static EnumSet<Token> SNFCalls = EnumSet.of(TOP, BOTTOM, CNAME,
			INAME, RNAME, DNAME, DATAEXPR, NOT, INV, AND, FORALL, LE,
			REFLEXIVE, RCOMPOSITION, PROJFROM, PROJINTO);

	//	private static boolean containsC(DLTree C, DLTree D) {
	//		switch (C.token()) {
	//			case CNAME:
	//				return C.equals(D);
	//			case AND:
	//				boolean ret = false;
	//				for (DLTree d : C.Children()) {
	//					ret |= containsC(d, D);
	//					if (ret) {
	//						return ret;
	//					}
	//				}
	//				return ret;
	//				//				return containsC(C.Left(), D) || containsC(C.Right(), D);
	//			default:
	//				return false;
	//		}
	//	}
	/** create BOTTOM element */
	public static DLTree createBottom() {
		return new LEAFDLTree(new TLexeme(BOTTOM));
	}

	public static DLTree createInverse(DLTree R) {
		assert R != null;
		if (R.token() == INV) {
			DLTree p = R.Child().copy();
			return p;
		}
		if (R.token() == RNAME) {
			return new ONEDLTree(new TLexeme(INV), R);
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
		return new NDLTree(new TLexeme(AND), C, D);
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
				l.addAll(d.Children());
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
		return new NDLTree(new TLexeme(AND), l);
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
				l.addAll(d.Children());
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
		return new NDLTree(new TLexeme(AND), l);
	}

	/** create existential restriction of given formulas (\ER.C) */
	public static DLTree createSNFExists(DLTree R, DLTree C) {
		// \ER.C . \not\AR.\not C
		return createSNFNot(createSNFForall(R, createSNFNot(C)));
	}

	public static DLTree createSNFForall(DLTree R, DLTree C) {
		if (C.isTOP()) {
			return C;
		} else {
			return new TWODLTree(new TLexeme(FORALL), R, C);
		}
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

	/** create at-most (LE) restriction of given formulas (<= n R.C) */
	public static DLTree createSNFLE(int n, DLTree R, DLTree C) {
		if (C.isBOTTOM()) {
			// <= n R.F . T;
			return createTop();
		}
		if (n == 0) {
			return createSNFForall(R, createSNFNot(C));
		}
		return new TWODLTree(new TLexeme(LE, n), R, C);
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
			return C.Child().copy();
		}
		// general case
		return new ONEDLTree(new TLexeme(NOT), C);
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
			return C.Child().copy();
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

	//	@Deprecated
	//	public static DLTree createSNFReducedAnd(DLTree C, DLTree D) {
	//		if (C == null || D == null) {
	//			return createSNFAnd(C, D);
	//		}
	//		if (D.token() == CNAME && containsC(C, D)) {
	//			return C;
	//		} else if (D.isAND()) {
	//			
	//			
	//			
	//			
	//			DLTree toReturn = D.copy();
	//			toReturn.addChild(C);
	//			//C = toReturn;
	//			return toReturn;
	//		} else {
	//			return createSNFAnd(C, D);
	//		}
	//	}
	//
	//	@Deprecated
	//	public static DLTree createSNFReducedAnd(DLTree C, DLTree D, DLTree ancestor) {
	//		if (C == null || D == null) {
	//			return ancestor;
	//		}
	//		if (D.token() == CNAME && containsC(C, D)) {
	//			return C;
	//		} else if (D.isAND()) {
	//			DLTree toReturn = createSNFReducedAnd(C, D.Left(), ancestor);
	//			toReturn = createSNFReducedAnd(toReturn, D.Right(), ancestor);
	//			//C = toReturn;
	//			return toReturn;
	//		} else {
	//			return ancestor;
	//		}
	//	}
	/** create TOP element */
	public static DLTree createTop() {
		return new LEAFDLTree(new TLexeme(TOP));
	}

	public static DLTree inverseComposition(final DLTree tree) {
		//XXX this needs to be checked with a proper test
		// see rolemaster.cpp, inverseComposition
		if (tree.token() == RCOMPOSITION) {
			return tree.accept(new ReverseCloningVisitor());
			//			return new DLTree(new TLexeme(RCOMPOSITION),
			//					inverseComposition(tree.Right()),
			//					inverseComposition(tree.Left()));
		} else {
			return new LEAFDLTree(new TLexeme(RNAME, TRole.resolveRole(tree)
					.inverse()));
		}
	}

	/** get DLTree by a given TDE */
	public static DLTree wrap(TNamedEntry t) {
		return new LEAFDLTree(new TLexeme(Token.DATAEXPR, t));
	}

	/** get TDE by a given DLTree */
	public static TNamedEntry unwrap(final DLTree t) {
		return t.elem().getNE();
	}

	public static DLTree buildTree(TLexeme t, DLTree t1, DLTree t2) {
		return new TWODLTree(t, t1, t2);
	}

	public static DLTree buildTree(TLexeme t, DLTree t1) {
		return new ONEDLTree(t, t1);
	}

	public static DLTree buildTree(TLexeme t) {
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
	public static boolean isFunctionalExpr(final DLTree t, final TNamedEntry R) {
		return t != null && t.token() == LE
				&& R.equals(t.Left().elem().getNE()) && t.elem().getData() == 1
				&& t.Right().isTOP();
	}

	public static boolean isSNF(final DLTree t) {
		if (t == null) {
			return true;
		}
		if (SNFCalls.contains(t.token())) {
			return isSNF(t.Left()) && isSNF(t.Right());
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
			for (DLTree t : t1.Children()) {
				if (!isSubTree(t, t2)) {
					return false;
				}
			}
			return true;
			//			return isSubTree(t1.Left(), t2) && isSubTree(t1.Right(), t2);
		}
		if (t2.isAND()) {
			for (DLTree t : t2.Children()) {
				if (isSubTree(t1, t)) {
					return true;
				}
			}
			return false;
			//			return isSubTree(t1, t2.Left()) || isSubTree(t1, t2.Right());
		}
		return t1.equals(t2);
	}

	/** check whether T is U-Role */
	public static boolean isUniversalRole(final DLTree t) {
		return isRName(t) && t.elem().getNE().isTop();
	}

	/**
	 * reshapes the tree to balance ANDs and remove duplicates; note: it is hard
	 * to guarantee that ALL unbalanced AND subtrees have been balanced. For
	 * now, rebalance the highest tree.
	 */
	//	public static DLTree rebalance(DLTree t) {
	//		List<DLTree> visit = treeAsList(t);
	//		// now visit has all nodes, the highest AND is first in the list
	//		DLTree base = null;
	//		for (int i = 0; base == null && i < visit.size(); i++) {
	//			if (visit.get(i).isAND()) {
	//				base = visit.get(i);
	//			}
	//		}
	//		if (base == null) {
	//			// can't find any AND: return the argument
	//			return t;
	//		}
	//		// now base is the highest AND node
	//		// find the AND nodes in this AND tree and its leaves
	//		List<DLTree> subtree = new ArrayList<DLTree>();
	//		List<DLTree> leaves = new ArrayList<DLTree>();
	//		subtree.add(base);
	//		computeLeaves(subtree, leaves);
	//		// subtree rooted at subtree[0]
	//		// leaves in AND, order irrelevant
	//		//There is at least one AND node and two leaves: "no AND nodes" would have returned already and less than two leaves means at least one AND node is malformed.
	//		//a balanced tree wound have 2N leaves and N-1 ANDs, so |subtree| / |leaves| must be close 0.5; however leaves must be unique
	//		int unbalanced = 0;
	//		for (DLTree d : subtree) {
	//			if ((d.Left().token() != AND && d.Right().isAND())
	//					|| (d.Left().isAND() && d.Right().token() != AND)) {
	//				// then one side is unbalanced
	//				unbalanced++;
	//			}
	//		}
	//		boolean checkBottomsOnly = false;
	//		if (unbalanced > subtree.size() / 2) {
	//			// not worth if not at least half the nodes are unbalanced; in this case, just check if there are any BOTTOMS (much greater advantage)
	//			checkBottomsOnly = true;
	//		}
	//		// check basic cases: drop TOPs from the leaves and if there is a BOTTOM drop the whole subtree and replace the root with BOTTOM
	//		for (int i = 0; i < leaves.size();) {
	//			DLTree tree = leaves.get(i);
	//			if (tree.isBOTTOM()) {
	//				// then replace the whole subtree
	//				if (base == t) {
	//					return createBottom();
	//				}
	//				// else replace the root in its ancestor
	//				if (base == base.Ancestor().Left()) {
	//					base.Ancestor().SetLeft(createBottom());
	//				} else {
	//					base.Ancestor().SetRight(createBottom());
	//				}
	//				return t;
	//			}
	//			if (tree.isTOP()) {
	//				leaves.remove(i);
	//			} else {
	//				i++;
	//			}
	//		}
	//		if (!checkBottomsOnly) {
	//			Set<DLTree> leafset = new HashSet<DLTree>(leaves);
	//			leaves.clear();
	//			leaves.addAll(leafset);
	//			//		double d=((double)subtree.size())/leaves.size();
	//			//		if(d>0.5) {
	//			// then it's worth rebalancing
	//			//}
	//			// build a tree to attach the leaves to
	//			DLTree newBase = buildBalancedAndTree(leaves);
	//			if (base == t) {
	//				return newBase;
	//			}
	//			if (base.Ancestor().Left() == base) {
	//				base.Ancestor().SetLeft(newBase);
	//			} else {
	//				base.Ancestor().SetRight(newBase);
	//			}
	//		}
	//		return t;
	//	}
	public static boolean replaceSynonymsFromTree(DLTree desc) {
		if (desc == null) {
			return false;
		}
		if (desc.isName()) {
			ClassifiableEntry entry = (ClassifiableEntry) desc.elem.getNE();
			if (entry.isSynonym()) {
				entry = resolveSynonym(entry);
				if (entry.getId() == -1) {
					desc.elem = new TLexeme(entry.getName().equals("TOP") ? TOP
							: BOTTOM);
				} else {
					desc.elem = new TLexeme(
							((TConcept) entry).isSingleton() ? INAME : CNAME,
							entry);
				}
				return true;
			} else {
				return false;
			}
		} else {
			boolean ret = false;
			for (DLTree d : desc.Children()) {
				ret |= replaceSynonymsFromTree(d);
			}
			return ret;
		}
	}
}
