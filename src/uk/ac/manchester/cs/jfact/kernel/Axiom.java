package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
 Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
 This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.kernel.InAx.*;
import static uk.ac.manchester.cs.jfact.kernel.Token.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import uk.ac.manchester.cs.jfact.helpers.DLTree;
import uk.ac.manchester.cs.jfact.helpers.DLTreeFactory;
import uk.ac.manchester.cs.jfact.helpers.IfDefs;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;

public final class Axiom {
	// NS for different DLTree matchers for trees in axiom
	/// absorb into negation of a concept; @return true if absorption is performed
	public boolean absorbIntoNegConcept(TBox KB) {
		List<DLTree> Cons = new ArrayList<DLTree>();
		Concept Concept;
		DLTree bestConcept = null;
		// finds all primitive negated concept names without description
		for (DLTree p : disjuncts) {
			if (p.token() == NOT && p.getChild().isName()
					&& (Concept = getConcept(p.getChild())).isPrimitive()
					&& !Concept.isSingleton() && Concept.getDescription() == null) {
				SAbsNAttempt();
				Cons.add(p);
			}
		}
		// if no concept names -- return;
		if (Cons.isEmpty()) {
			return false;
		}
		SAbsNApply();
		// FIXME!! as for now: just take the 1st concept name
		if (bestConcept == null) {
			bestConcept = Cons.get(0);
		}
		// normal concept absorption
		Concept = InAx.getConcept(bestConcept.getChild());
		if (LeveLogger.isAbsorptionActive()) {
			LeveLogger.logger_absorption.print(" N-Absorb GCI to concept "
					+ Concept.getName());
			if (Cons.size() > 1) {
				LeveLogger.logger_absorption.print(" (other options are");
				for (int j = 1; j < Cons.size(); ++j) {
					LeveLogger.logger_absorption.print(" "
							+ InAx.getConcept(Cons.get(j).getChild()).getName());
				}
				LeveLogger.logger_absorption.print(")");
			}
		}
		// replace ~C [= D with C=~notC, notC [= D:
		// make notC [= D
		Concept nC = KB.getAuxConcept(createAnAxiom(bestConcept));
		// define C = ~notC; C had an empty desc, so it's safe not to delete it
		KB.makeNonPrimitive(Concept, DLTreeFactory.createSNFNot(KB.getTree(nC)));
		return true;
	}

	/** GCI is presented in the form (or Disjuncts); */
	private final LinkedHashSet<DLTree> disjuncts = new LinkedHashSet<DLTree>();

	/** create a copy of a given GCI; ignore SKIP entry */
	private Axiom copy(DLTree skip) {
		Axiom ret = new Axiom();
		for (DLTree i : disjuncts) {
			if (!i.equals(skip)) {
				ret.disjuncts.add(i.copy());
			}
		}
		return ret;
	}

	/** simplify (OR C ...) for a non-primitive C in a given position */
	private Axiom simplifyPosNP(DLTree pos) {
		SAbsRepCN();
		Axiom ret = copy(pos);
		ret.add(DLTreeFactory.createSNFNot(InAx.getConcept(pos.getChild())
				.getDescription().copy()));
		if (LeveLogger.isAbsorptionActive()) {
			LeveLogger.logger_absorption.print(" simplify CN expression for "
					+ pos.getChild());
		}
		return ret;
	}

	/** simplify (OR ~C ...) for a non-primitive C in a given position */
	private Axiom simplifyNegNP(DLTree pos) {
		SAbsRepCN();
		Axiom ret = copy(pos);
		ret.add(InAx.getConcept(pos).getDescription().copy());
		if (LeveLogger.isAbsorptionActive()) {
			LeveLogger.logger_absorption.print(" simplify ~CN expression for " + pos);
		}
		return ret;
	}

	/** split (OR (AND...) ...) in a given position */
	private List<Axiom> split(List<Axiom> acc, DLTree pos, DLTree pAnd) {
		if (pAnd.isAND()) {
			// split the AND
			List<DLTree> children = new ArrayList<DLTree>(pAnd.getChildren());
			acc = split(acc, pos, children.remove(0));
			if (children.size() > 0) {
				acc = split(acc, pos, DLTreeFactory.createSNFAnd(children));
			}
		} else {
			Axiom ret = copy(pos);
			ret.add(DLTreeFactory.createSNFNot(pAnd.copy()));
			acc.add(ret);
		}
		return acc;
	}

	/** split an axiom; @return new axiom and/or NULL */
	public List<Axiom> split() {
		List<Axiom> acc = new ArrayList<Axiom>();
		for (DLTree p : disjuncts) {
			if (InAx.isAnd(p)) {
				SAbsSplit();
				if (LeveLogger.isAbsorptionActive()) {
					LeveLogger.logger_absorption.print(" split AND espression "
							+ p.getChild());
				}
				acc = split(acc, p, p.getChildren().iterator().next());
				// no need to split more than once:
				// every extra splits would be together with unsplitted parts
				// like: (A or B) and (C or D) would be transform into
				// A and (C or D), B and (C or D), (A or B) and C, (A or B) and D
				// so just return here
				return acc;
			}
		}
		return acc;
	}

	/** create an empty GCI */
	public Axiom() {}

	//	/** create a copy of a given GCI */
	//	public Axiom(final Axiom ax) {
	//		for (DLTree i : ax.disjuncts) {
	//			disjuncts.add(i.copy());
	//		}
	//	}
	/** add DLTree to an axiom */
	public void add(DLTree p) {
		if (InAx.isBot(p)) {
			return; // nothing to do
		}
		// flatten the disjunctions on the fly
		if (InAx.isOr(p)) {
			for (DLTree d : p.getChildren()) {
				add(d);
			}
			return;
		}
		disjuncts.add(p);
	}

	/** dump GCI for debug purposes */
	public void dump(LogAdapter o) {
		o.print(" (neg-and");
		for (DLTree p : disjuncts) {
			o.print(p.toString());
		}
		o.print(")");
	}

	/// replace a defined concept with its description
	public Axiom simplifyCN() {
		for (DLTree p : disjuncts) {
			if (InAx.isPosNP(p)) {
				return simplifyPosNP(p);
			} else if (InAx.isNegNP(p)) {
				return simplifyNegNP(p);
			}
		}
		return null;
	}

	/// replace a universal restriction with a fresh concept
	public Axiom simplifyForall(TBox KB) {
		for (DLTree i : disjuncts) {
			if (InAx.isAbsForall(i)) {
				return simplifyForall(i, KB);
			}
		}
		return null;
	}

	private Axiom simplifyForall(DLTree pos, TBox KB) {
		SAbsRepForall();
		DLTree pAll = pos.getChild(); // (all R ~C)
		if (LeveLogger.isAbsorptionActive()) {
			LeveLogger.logger_absorption.print(" simplify ALL expression" + pAll);
		}
		Axiom ret = copy(pos);
		ret.add(KB.getTree(KB.replaceForall(pAll.copy())));
		return ret;
	}

	/// create a concept expression corresponding to a given GCI; ignore SKIP entry
	public DLTree createAnAxiom(DLTree replaced) {
		assert !disjuncts.isEmpty();
		List<DLTree> leaves = new ArrayList<DLTree>();
		for (DLTree d : disjuncts) {
			if (!d.equals(replaced)) {
				leaves.add(d.copy());
			}
		}
		DLTree result = DLTreeFactory.createSNFAnd(leaves);
		return DLTreeFactory.createSNFNot(result);
	}

	/// absorb into BOTTOM; @return true if absorption is performed
	public boolean absorbIntoBottom() {
		List<DLTree> Pos = new ArrayList<DLTree>(), Neg = new ArrayList<DLTree>();
		for (DLTree p : disjuncts) {
			switch (p.token()) {
				case BOTTOM: // axiom in the form T [= T or ...; nothing to do
					SAbsBApply();
					if (IfDefs.RKG_DEBUG_ABSORPTION) {
						LeveLogger.logger_absorption.print(" Absorb into BOTTOM");
					}
					return true;
				case TOP: // skip it here
					break;
				case NOT: // something negated: put it into NEG
					Neg.add(p.getChild());
					break;
				default: // something positive: save in POS
					Pos.add(p);
					break;
			}
		}
		// now check whether there is a concept in both POS and NEG
		for (DLTree q : Neg) {
			for (DLTree s : Pos) {
				if (q.equals(s)) {
					SAbsBApply();
					if (IfDefs.RKG_DEBUG_ABSORPTION) {
						LeveLogger.logger_absorption
								.print(" Absorb into BOTTOM due to (not" + q + ") and"
										+ s);
					}
					return true;
				}
			}
		}
		return false;
	}

	public boolean absorbIntoConcept(TBox KB) {
		List<DLTree> Cons = new ArrayList<DLTree>();
		DLTree bestConcept = null;
		for (DLTree p : disjuncts) {
			if (InAx.isNegPC(p)) {
				SAbsCAttempt();
				Cons.add(p);
				if (getConcept(p).isSystem()) {
					bestConcept = p;
				}
			}
		}
		if (Cons.isEmpty()) {
			return false;
		}
		SAbsCApply();
		if (bestConcept == null) {
			bestConcept = Cons.get(0);
		}
		// normal concept absorption
		Concept Concept = InAx.getConcept(bestConcept);
		if (LeveLogger.isAbsorptionActive()) {
			LeveLogger.logger_absorption.print(" C-Absorb GCI to concept "
					+ Concept.getName());
			if (Cons.size() > 1) {
				LeveLogger.logger_absorption.print(" (other options are");
				for (int j = 1; j < Cons.size(); ++j) {
					LeveLogger.logger_absorption.print(" "
							+ InAx.getConcept(Cons.get(j)).getName());
				}
				LeveLogger.logger_absorption.print(")");
			}
		}
		Concept.addDesc(createAnAxiom(bestConcept));
		Concept.removeSelfFromDescription();
		KB.clearRelevanceInfo();
		KB.checkToldCycle(Concept);
		KB.clearRelevanceInfo();
		return true;
	}

	public boolean absorbIntoDomain() {
		List<DLTree> Cons = new ArrayList<DLTree>();
		DLTree bestSome = null;
		for (DLTree p : disjuncts) {
			if (p.token() == NOT
					&& (p.getChild().token() == FORALL || p.getChild().token() == LE)) {
				SAbsRAttempt();
				Cons.add(p);
				if (p.getChild().getRight().isBOTTOM()) {
					bestSome = p;
					break;
				}
			}
		}
		if (Cons.isEmpty()) {
			return false;
		}
		SAbsRApply();
		Role role;
		if (bestSome != null) {
			role = Role.resolveRole(bestSome.getChild().getLeft());
		} else {
			role = Role.resolveRole(Cons.get(0).getChild().getLeft());
		}
		if (LeveLogger.isAbsorptionActive()) {
			LeveLogger.logger_absorption.print(" R-Absorb GCI to the domain of role "
					+ role.getName());
			if (Cons.size() > 1) {
				LeveLogger.logger_absorption.print(" (other options are");
				for (int j = 1; j < Cons.size(); ++j) {
					LeveLogger.logger_absorption.print(" "
							+ Role.resolveRole(Cons.get(j).getChild().getLeft())
									.getName());
				}
				LeveLogger.logger_absorption.print(")");
			}
		}
		role.setDomain(createAnAxiom(bestSome));
		return true;
	}

	/** absorb into TOP; @return true if absorption performs */
	public boolean absorbIntoTop(TBox KB) {
		Concept C = null;
		// check whether the axiom is Top [= C
		for (DLTree p : disjuncts) {
			if (InAx.isBot(p)) {
				continue;
			} else if (InAx.isPosCN(p)) // C found
			{
				if (C != null) {
					return false;
				}
				C = InAx.getConcept(p.getChild());
				if (C.isSingleton()) {
					return false;
				}
			} else {
				return false;
			}
		}
		if (C == null) {
			return false;
		}
		SAbsTApply();
		// make an absorption
		DLTree desc = KB.makeNonPrimitive(C, DLTreeFactory.createTop());
		if (LeveLogger.isAbsorptionActive()) {
			LeveLogger.logger_absorption
					.println("TAxiom.absorbIntoTop() T-Absorb GCI to axiom");
			if (desc != null) {
				LeveLogger.logger_absorption.println("s *TOP* [=" + desc + " and");
			}
			LeveLogger.logger.println(" " + C.getName() + " = *TOP*");
		}
		if (desc != null) {
			KB.addSubsumeAxiom(DLTreeFactory.createTop(), desc);
		}
		return true;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null) {
			return false;
		}
		if (this == arg0) {
			return true;
		}
		if (arg0 instanceof Axiom) {
			Axiom ax = (Axiom) arg0;
			return disjuncts.equals(ax.disjuncts);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return disjuncts.hashCode();
	}
}