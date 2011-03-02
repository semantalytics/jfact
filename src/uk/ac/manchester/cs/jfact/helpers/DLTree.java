package uk.ac.manchester.cs.jfact.helpers;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import static uk.ac.manchester.cs.jfact.kernel.Token.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import uk.ac.manchester.cs.jfact.kernel.TLexeme;
import uk.ac.manchester.cs.jfact.kernel.Token;


public abstract class DLTree {
	private static final CloningVisitor cloner = new CloningVisitor();
	/** element in the tree node */
	protected TLexeme elem;
	/** children collection */
	protected Collection<DLTree> children;
	protected DLTree ancestor;

	public DLTree(final TLexeme Init) {
		elem = Init;
	}

	public Token token() {
		return elem.getToken();
	}

	public boolean isTOP() {
		return elem.getToken() == TOP;
	}

	public boolean isNOT() {
		return elem.getToken() == NOT;
	}

	public boolean isBOTTOM() {
		return elem.getToken() == BOTTOM;
	}

	public boolean isAND() {
		return elem.getToken() == AND;
	}

	public TLexeme elem() {
		return elem;
	}

	public abstract DLTree Child();

	public abstract DLTree Left();

	public abstract DLTree Right();

	public DLTree Ancestor() {
		return ancestor;
	}

	public final void SetAncestor(DLTree r) {
		ancestor = r;
	}

	public final void addChild(DLTree d) {
		if (d != null) {
			children.add(d);
			d.ancestor = this;
		}
	}

	public DLTree(Token tok) {
		this(new TLexeme(tok));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof DLTree) {
			DLTree t2 = (DLTree) obj;
			return equalTrees(this, t2);
		}
		return false;
	}

	@Override
	public String toString() {
		if (Children().size() > 0) {
			StringBuilder b = new StringBuilder();
			b.append("(");
			b.append(elem.toString());
			for (DLTree d : Children()) {
				b.append(' ');
				b.append(d.toString());
			}
			b.append(")");
			return b.toString();
		} else {
			return elem.toString();
		}
	}

	@Override
	public int hashCode() {
		return elem.hashCode() + (children == null ? 0 : children.hashCode());
	}

	abstract void accept(DLTreeVisitor v);

	abstract <O> O accept(DLTreeVisitorEx<O> v);

	public abstract void replace(DLTree toReplace, DLTree replacement);

	public Collection<DLTree> Children() {
		return children;
	}

	//	public static boolean equalTrees(final DLTree t1, final DLTree t2) {
	//		if (t1 == null && t2 == null) {
	//			return true;
	//		}
	//		if (t1 == null || t2 == null) {
	//			return false;
	//		}
	//		return t1.Element().equals(t2.Element())
	//				&& equalTrees(t1.Left(), t2.Left())
	//				&& equalTrees(t1.Right(), t2.Right());
	//	}
	public static boolean equalTrees(final DLTree t1, final DLTree t2) {
		if (t1 == null && t2 == null) {
			return true;
		}
		if (t1 == null || t2 == null) {
			return false;
		}
		if (t1.elem.equals(t2.elem)) {
			if (t1 instanceof LEAFDLTree) {
				return true;
			}
			if (t1 instanceof ONEDLTree) {
				return t1.Child().equals(t2.Child());
			}
			Collection<DLTree> c1 = t1.Children();
			Collection<DLTree> c2 = t2.Children();
			return c1.size() == c2.size() && c1.containsAll(c2)
					&& c2.containsAll(c1);
		}
		return false;
		//		List<DLTree> navigate1 = new ArrayList<DLTree>();
		//		List<DLTree> navigate2 = new ArrayList<DLTree>();
		//		navigate1.add(t1);
		//		navigate2.add(t2);
		//		for (int i = 0; i < navigate1.size(); i++) {
		//			if (navigate1.size() != navigate2.size()) {
		//				//size differs, return false
		//				return false;
		//			}
		//			if (!navigate1.get(i).elem.equals(navigate2.get(i).elem)) {
		//				// corresponding elements not the same = false
		//				return false;
		//			}
		//			navigate1.addAll(navigate1.get(i).Children());
		//			navigate2.addAll(navigate2.get(i).Children());
		//		}
		//		return true;
	}

	public DLTree copy() {
		return this.accept(cloner);
	}

	/** check if DL tree is a concept-like name */
	public boolean isCN() {
		return isConst() || isName();
	}

	// check if DL tree is a concept constant
	public boolean isConst() {
		//		if (t == null) {
		//			return false;
		//		}
		if (isTOP() || isBOTTOM()) {
			return true;
		}
		return false;
	}

	// check if DL tree is a concept/individual name
	public boolean isName() {
		//		if (t == null) {
		//			return false;
		//		}
		return token() == CNAME || token() == INAME;
	}
}

interface DLTreeVisitor {
	void visit(LEAFDLTree t);

	void visit(ONEDLTree t);

	void visit(TWODLTree t);

	void visit(NDLTree t);
}

interface DLTreeVisitorEx<O> {
	O visit(LEAFDLTree t);

	O visit(ONEDLTree t);

	O visit(TWODLTree t);

	O visit(NDLTree t);
}

class CloningVisitor implements DLTreeVisitorEx<DLTree> {
	public DLTree visit(LEAFDLTree t) {
		return new LEAFDLTree(new TLexeme(t.elem));
	}

	public DLTree visit(ONEDLTree t) {
		return new ONEDLTree(new TLexeme(t.elem), t.Child().accept(this));
	}

	public DLTree visit(TWODLTree t) {
		return new TWODLTree(new TLexeme(t.elem), t.Left().accept(this), t
				.Right().accept(this));
	}

	public DLTree visit(NDLTree t) {
		List<DLTree> l = new ArrayList<DLTree>();
		for (DLTree tree : t.children) {
			l.add(tree.accept(this));
		}
		return new NDLTree(new TLexeme(t.elem), l);
	}
}

class ReverseCloningVisitor implements DLTreeVisitorEx<DLTree> {
	public DLTree visit(LEAFDLTree t) {
		return DLTreeFactory.inverseComposition(t);
		//		return new LEAFDLTree(new TLexeme(t.elem));
	}

	public DLTree visit(ONEDLTree t) {
		return new ONEDLTree(new TLexeme(t.elem), t.Child().accept(this));
	}

	public DLTree visit(TWODLTree t) {
		return new TWODLTree(new TLexeme(t.elem), t.Right().accept(this), t
				.Left().accept(this));
	}

	public DLTree visit(NDLTree t) {
		List<DLTree> l = new ArrayList<DLTree>(t.children);
		List<DLTree> actual = new ArrayList<DLTree>();
		Collections.reverse(l);
		for (DLTree tree : l) {
			actual.add(tree.accept(this));
		}
		return new NDLTree(new TLexeme(t.elem), actual);
	}
}

/** things that have no children */
class LEAFDLTree extends DLTree {
	LEAFDLTree(TLexeme l) {
		super(l);
	}

	@Override
	public DLTree Child() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DLTree Left() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DLTree Right() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<DLTree> Children() {
		return Collections.emptyList();
	}

	@Override
	void accept(DLTreeVisitor v) {
		v.visit(this);
	}

	@Override
	<O> O accept(DLTreeVisitorEx<O> v) {
		return v.visit(this);
	}

	@Override
	public void replace(DLTree toReplace, DLTree replacement) {
		throw new UnsupportedOperationException();
	}
}

/** covers trees with only one child, i.e., inverse, not */
class ONEDLTree extends DLTree {
	DLTree child;

	ONEDLTree(TLexeme l, DLTree t) {
		super(l);
		child = t;
		if (t != null) {
			t.ancestor = this;
		}
	}

	@Override
	public DLTree Left() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DLTree Child() {
		return child;
	}

	@Override
	public DLTree Right() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<DLTree> Children() {
		return Collections.singleton(child);
	}

	@Override
	void accept(DLTreeVisitor v) {
		v.visit(this);
	}

	@Override
	<O> O accept(DLTreeVisitorEx<O> v) {
		return v.visit(this);
	}

	@Override
	public void replace(DLTree toReplace, DLTree replacement) {
		if (child == toReplace) {
			child = replacement;
			if (replacement != null) {
				replacement.ancestor = this;
			}
		}
	}
}

/** covers trees with two and only two children */
class TWODLTree extends DLTree {
	TWODLTree(TLexeme l, DLTree t1, DLTree t2) {
		super(l);
		children = new ArrayList<DLTree>(2);
		addChild(t1);
		addChild(t2);
	}

	@Override
	public DLTree Child() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DLTree Left() {
		return ((List<DLTree>) children).get(0);
	}

	@Override
	public DLTree Right() {
		return ((List<DLTree>) children).get(1);
	}

	@Override
	void accept(DLTreeVisitor v) {
		v.visit(this);
	}

	@Override
	<O> O accept(DLTreeVisitorEx<O> v) {
		return v.visit(this);
	}

	@Override
	public void replace(DLTree toReplace, DLTree replacement) {
		int p = ((List<DLTree>) children).indexOf(toReplace);
		if (p > -1) {
			((List<DLTree>) children).set(p, replacement);
			if (replacement != null) {
				replacement.ancestor = this;
			}
		}
	}
}

class NDLTree extends DLTree {
	public NDLTree(TLexeme l, Collection<DLTree> trees) {
		super(l);
		children = new ArrayList<DLTree>();//LinkedHashSet<DLTree>();
		if (trees.size() < 2) {
			throw new RuntimeException(
					"not enough elements in the n-ary element");
		}
		for (DLTree d : trees) {
			addChild(d);
		}
	}

	public NDLTree(TLexeme l, DLTree C, DLTree D) {
		super(l);
		children = new ArrayList<DLTree>();//LinkedHashSet<DLTree>();
		if (C == null || D == null) {
			throw new RuntimeException(
					"not enough elements in the n-ary element");
		}
		addChild(C);
		addChild(D);
	}

	@Override
	public DLTree Child() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DLTree Left() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DLTree Right() {
		throw new UnsupportedOperationException();
	}

	@Override
	void accept(DLTreeVisitor v) {
		v.visit(this);
	}

	@Override
	<O> O accept(DLTreeVisitorEx<O> v) {
		return v.visit(this);
	}

	@Override
	public void replace(DLTree toReplace, DLTree replacement) {
		if (children.contains(toReplace)) {
			children.remove(toReplace);
			if (replacement != null) {
				children.add(replacement);
				replacement.ancestor = this;
			}
		}
	}
}
