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

public final class Precompletor {
//	/** host KB */
//	private final TBox kb;
//	/** ToDo List for precompletion */
//	private final LinkedList<Pair<Individual, DLTree>> toDoList = new LinkedList<Pair<Individual, DLTree>>();
//
//	/** init precompletion process */
//	private void initPrecompletion() {}
//
//	/** propagate RIA and rules throughout individual cloud */
//	private void propagateRIA() {}
//
//	/** init labels of related elements with R&D */
//	private void addRnD() {}
//
//	/** init ToDo List with elements in every individuals' description */
//	private void initToDoList() {
//		for (Individual p : kb.i_begin()) {
//			processTree(p, p.getDescription());
//		}
//	}
//
//	/** update individuals' info from precompletion */
//	private void updateIndividualsFromPrecompletion() {
//		for (Individual p : kb.i_begin()) {
//			p.usePCInfo();
//		}
//	}
//
//	/** add (Ind:Expr) to the ToDo List */
//	private void addToDoEntry(Individual ind, final DLTree expr) {
//		if (ind.addPCExpr(expr)) {
//			toDoList.addLast(new Pair<Individual, DLTree>(ind, expr));
//		}
//	}
//
//	/** process the whole tree */
//	private void processTree(Individual ind, DLTree expr) {
//		if (expr == null) {
//			return;
//		}
//		switch (expr.token()) {
//			case AND: // go recursively
//				for (DLTree d : expr.getChildren()) {
//					processTree(ind, d);
//				}
//				break;
//			default: // add non-AND expression to the ToDo list
//				addToDoEntry(ind, expr);
//				break;
//		}
//	}
//
//	/** process forall restriction */
//	private void processForall(Individual ind, final Role R, final DLTree expr) {
//		for (Related i : ind.getRelatedIndex()) {
//			if (i.getRole().lesserequal(R)) {
//				processTree(i.getB(), expr);
//			}
//		}
//	}
//
//	/** empty c'tor */
//	public Precompletor(TBox box) {
//		kb = box;
//	}
//
//	/** perform precompletion; @return true if precompletion failed */
//	public void performPrecompletion() {
//		initPrecompletion();
//		propagateRIA();
//		initToDoList();
//		addRnD();
//		if (runPrecompletion()) {
//			kb.setConsistency(false);
//		}
//		updateIndividualsFromPrecompletion();
//		kb.setPrecompleted();
//	}
//
//	private boolean runPrecompletion() {
//		while (!toDoList.isEmpty()) {
//			Pair<Individual, DLTree> cur = toDoList.removeLast();
//			switch (cur.second.token()) {
//				case TOP:
//					break;
//				case BOTTOM:
//					return true;
//				case CNAME:
//					addToDoEntry(cur.first,
//							((Concept) cur.second.elem().getNE()).getDescription());
//					break;
//				case AND:
//					processTree(cur.first, cur.second);
//					break;
//				case FORALL:
//					processForall(cur.first, (Role) cur.second.getLeft().elem().getNE(),
//							cur.second.getRight());
//					break;
//				case NOT:
//					switch (cur.second.getChild().token()) {
//						case FORALL:
//							break;
//						default:
//							throw new ReasonerInternalException(
//									"Unsupported concept expression: "
//											+ cur.second.getChild() + "\n");
//					}
//					break;
//				default:
//					throw new ReasonerInternalException(
//							"Unsupported concept expression: " + cur.second + "\n");
//			}
//		}
//		return false;
//	}
}
