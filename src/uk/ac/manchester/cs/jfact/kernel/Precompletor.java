package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import java.util.LinkedList;


import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

import uk.ac.manchester.cs.jfact.helpers.DLTree;
import uk.ac.manchester.cs.jfact.helpers.Pair;

public final  class Precompletor {
	/** host KB */
	private final TBox KB;
	/** ToDo List for precompletion */
	private final LinkedList<Pair<TIndividual, DLTree>> toDoList = new LinkedList<Pair<TIndividual, DLTree>>();

	/** init precompletion process */
	private void initPrecompletion() {
	}

	/** propagate RIA and rules throughout individual cloud */
	private void propagateRIA() {
	}

	/** init labels of related elements with R&D */
	private void addRnD() {
	}

	/** init ToDo List with elements in every individuals' description */
	private void initToDoList() {
		for (TIndividual p : KB.i_begin()) {
			processTree(p, p.getDescription());
		}
	}

	/** update individuals' info from precompletion */
	private void updateIndividualsFromPrecompletion() {
		for (TIndividual p : KB.i_begin()) {
			p.usePCInfo();
		}
	}

	/** add (Ind:Expr) to the ToDo List */
	private void addToDoEntry(TIndividual ind, final DLTree expr) {
		if (ind.addPCExpr(expr)) {
			toDoList.addLast(new Pair<TIndividual, DLTree>(ind, expr));
		}
	}

	/** process the whole tree */
	private void processTree(TIndividual ind, DLTree expr) {
		if (expr == null) {
			return;
		}
		switch (expr.token()) {
			case AND: // go recursively
				for (DLTree d : expr.Children()) {
					processTree(ind, d);
				}
				break;
			default: // add non-AND expression to the ToDo list
				addToDoEntry(ind, expr);
				break;
		}
	}

	//	private void processTree(TIndividual ind, Collection<DLTree> expr) {
	//		for (DLTree d : expr) {
	//			processTree(ind, d);
	//		}
	//	}
	/** process forall restriction */
	private void processForall(TIndividual ind, final TRole R, final DLTree expr) {
		for (TRelated i : ind.getRelatedIndex()) {
			if (i.getRole().lesserequal(R)) {
				processTree(i.getB(), expr);
			}
		}
	}

	/** empty c'tor */
	public Precompletor(TBox box) {
		KB = box;
	}

	/** perform precompletion; @return true if precompletion failed */
	public void performPrecompletion() {
		initPrecompletion();
		propagateRIA();
		initToDoList();
		addRnD();
		if (runPrecompletion()) {
			KB.setConsistency(false);
		}
		updateIndividualsFromPrecompletion();
		KB.setPrecompleted();
	}

	private boolean runPrecompletion() {
		while (!toDoList.isEmpty()) {
			Pair<TIndividual, DLTree> cur = toDoList.removeLast();
			switch (cur.second.token()) {
				case TOP:
					break;
				case BOTTOM:
					return true;
				case CNAME:
					addToDoEntry(cur.first, ((TConcept) cur.second.elem()
							.getNE()).getDescription());
					break;
				case AND:
					processTree(cur.first, cur.second);
					break;
				case FORALL:
					processForall(cur.first, (TRole) cur.second.Left().elem()
							.getNE(), cur.second.Right());
					break;
				case NOT:
					switch (cur.second.Child().token()) {
						case FORALL:
							break;
						default:
							throw new ReasonerInternalException(
									"Unsupported concept expression: "
											+ cur.second.Child() + "\n");
					}
					break;
				default:
					throw new ReasonerInternalException(
							"Unsupported concept expression: " + cur.second
									+ "\n");
			}
		}
		return false;
	}
}
//class PCToDoList {
//	/** remember all the entries here */
//	private final List<PCToDoEntry> Base = new ArrayList<PCToDoEntry>();
//
//	public PCToDoList() {
//	}
//
//	/** add new entry */
//	public void add(TIndividual ind, final DLTree expr) {
//		Base.add(new PCToDoEntry(ind, expr));
//	}
//
//	/** get next entry */
//	public final PCToDoEntry get() {
//		return Base.remove(Base.size() - 1);
//	}
//
//	/** check whether ToDo list is empty */
//	public boolean isEmpty() {
//		return Base.isEmpty();
//	}
//}
///** precompletion ToDo List Entry */
//class PCToDoEntry {
//
//	/** individual to expand */
//	public final TIndividual Ind;
//	/** concept expression to expand */
//	public final DLTree Expr;
//
//	PCToDoEntry(TIndividual ind, final DLTree expr) {
//		Ind = ind;
//		Expr = expr;
//	}
//}