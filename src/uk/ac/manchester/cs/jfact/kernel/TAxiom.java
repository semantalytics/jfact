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

public final class TAxiom {
	// NS for different DLTree matchers for trees in axiom
	/// absorb into negation of a concept; @return true if absorption is performed
	boolean absorbIntoNegConcept(TBox KB) {
		List<DLTree> Cons = new ArrayList<DLTree>();
		TConcept Concept;
		DLTree bestConcept = null;
		// finds all primitive negated concept names without description
		for (DLTree p : Disjuncts) {
			if (p.token() == NOT && p.Child().isName() && (Concept = getConcept(p.Child())).isPrimitive() && !Concept.isSingleton() && Concept.getDescription() == null) {
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
		Concept = InAx.getConcept(bestConcept.Child());
		if (LeveLogger.isAbsorptionActive()) {
			LeveLogger.LL_ABSORPTION.print(" N-Absorb GCI to concept " + Concept.getName());
			if (Cons.size() > 1) {
				LeveLogger.LL_ABSORPTION.print(" (other options are");
				for (int j = 1; j < Cons.size(); ++j) {
					LeveLogger.LL_ABSORPTION.print(" " + InAx.getConcept(Cons.get(j).Child()).getName());
				}
				LeveLogger.LL_ABSORPTION.print(")");
			}
		}
		// replace ~C [= D with C=~notC, notC [= D:
		// make notC [= D
		TConcept nC = KB.getAuxConcept(createAnAxiom(bestConcept));
		// define C = ~notC; C had an empty desc, so it's safe not to delete it
		KB.makeNonPrimitive(Concept, DLTreeFactory.createSNFNot(KB.getTree(nC)));
		return true;
	}

	/** GCI is presented in the form (or Disjuncts); */
	private final LinkedHashSet<DLTree> Disjuncts = new LinkedHashSet<DLTree>();

	/** create a copy of a given GCI; ignore SKIP entry */
	TAxiom copy(DLTree skip) {
		TAxiom ret = new TAxiom();
		for (DLTree i : Disjuncts) {
			if (!i.equals(skip)) {
				ret.Disjuncts.add(i.copy());
			}
		}
		return ret;
	}

	/** simplify (OR C ...) for a non-primitive C in a given position */
	TAxiom simplifyPosNP(DLTree pos) {
		SAbsRepCN();
		TAxiom ret = copy(pos);
		ret.add(DLTreeFactory.createSNFNot(InAx.getConcept(pos.Child()).getDescription().copy()));
		if (LeveLogger.isAbsorptionActive()) {
			LeveLogger.LL_ABSORPTION.print(" simplify CN expression for " + pos.Child());
		}
		return ret;
	}

	/** simplify (OR ~C ...) for a non-primitive C in a given position */
	TAxiom simplifyNegNP(DLTree pos) {
		SAbsRepCN();
		TAxiom ret = copy(pos);
		ret.add(InAx.getConcept(pos).getDescription().copy());
		if (LeveLogger.isAbsorptionActive()) {
			LeveLogger.LL_ABSORPTION.print(" simplify ~CN expression for " + pos);
		}
		return ret;
	}

	/** split (OR (AND...) ...) in a given position */
	List<TAxiom> split(List<TAxiom> acc, DLTree pos, DLTree pAnd) {
		if (pAnd.isAND()) {
			// split the AND
			List<DLTree> children = new ArrayList<DLTree>(pAnd.Children());
			acc = split(acc, pos, children.remove(0));
			if (children.size() > 0) {
				acc = split(acc, pos, DLTreeFactory.createSNFAnd(children));
			}
		} else {
			TAxiom ret = copy(pos);
			ret.add(DLTreeFactory.createSNFNot(pAnd.copy()));
			acc.add(ret);
		}
		return acc;
	}

	/** split an axiom; @return new axiom and/or NULL */
	List<TAxiom> split() {
		List<TAxiom> acc = new ArrayList<TAxiom>();
		for (DLTree p : Disjuncts) {
			if (InAx.isAnd(p)) {
				SAbsSplit();
				if (LeveLogger.isAbsorptionActive()) {
					LeveLogger.LL_ABSORPTION.print(" split AND espression " + p.Child());
				}
				acc = split(acc, p, p.Children().iterator().next());
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
	public TAxiom() {
	}

	/** create a copy of a given GCI */
	public TAxiom(final TAxiom ax) {
		for (DLTree i : ax.Disjuncts) {
			//if (!Disjuncts.contains(i)) {
			Disjuncts.add(i.copy());
			//}
		}
	}

	/** add DLTree to an axiom */
	void add(DLTree p) {
		if (InAx.isBot(p)) {
			return; // nothing to do
		}
		// flatten the disjunctions on the fly
		if (InAx.isOr(p)) {
			for (DLTree d : p.Children()) {
				add(d);
			}
			return;
		}
		//if (!Disjuncts.contains(p)) {
		Disjuncts.add(p);
		//}
	}

	/** dump GCI for debug purposes */
	public void dump(LogAdapter o) {
		o.print(" (neg-and");
		for (DLTree p : Disjuncts) {
			o.print(p.toString());
		}
		o.print(")");
	}

	/// replace a defined concept with its description
	public TAxiom simplifyCN() {
		for (DLTree p : Disjuncts) {
			if (InAx.isPosNP(p)) {
				return simplifyPosNP(p);
			} else if (InAx.isNegNP(p)) {
				return simplifyNegNP(p);
			}
		}
		return null;
	}

	/// replace a universal restriction with a fresh concept
	TAxiom simplifyForall(TBox KB) {
		for (DLTree i : Disjuncts) {
			if (InAx.isAbsForall(i)) {
				return simplifyForall(i, KB);
			}
		}
		return null;
	}

	TAxiom simplifyForall(DLTree pos, TBox KB) {
		SAbsRepForall();
		DLTree pAll = pos.Child(); // (all R ~C)
		if (LeveLogger.isAbsorptionActive()) {
			LeveLogger.LL_ABSORPTION.print(" simplify ALL expression" + pAll);
		}
		TAxiom ret = copy(pos);
		ret.add(KB.getTree(KB.replaceForall(pAll.copy())));
		return ret;
	}

	/// create a concept expression corresponding to a given GCI; ignore SKIP entry
	public DLTree createAnAxiom(DLTree replaced) {
		assert !Disjuncts.isEmpty();
		List<DLTree> leaves = new ArrayList<DLTree>();
		for (DLTree d : Disjuncts) {
			if (!d.equals(replaced)) {
				leaves.add(d.copy());
			}
		}
		DLTree result = DLTreeFactory.createSNFAnd(leaves);
		return DLTreeFactory.createSNFNot(result);
	}

	/// absorb into BOTTOM; @return true if absorption is performed
	boolean absorbIntoBottom() {
		List<DLTree> Pos = new ArrayList<DLTree>(), Neg = new ArrayList<DLTree>();
		for (DLTree p : Disjuncts)
			switch (p.token()) {
				case BOTTOM: // axiom in the form T [= T or ...; nothing to do
					SAbsBApply();
					if (IfDefs.RKG_DEBUG_ABSORPTION) {
						LeveLogger.LL_ABSORPTION.print(" Absorb into BOTTOM");
					}
					return true;
				case TOP: // skip it here
					break;
				case NOT: // something negated: put it into NEG
					Neg.add(p.Child());
					break;
				default: // something positive: save in POS
					Pos.add(p);
					break;
			}
		// now check whether there is a concept in both POS and NEG
		for (DLTree q : Neg)
			for (DLTree s : Pos)
				if (q.equals(s)) {
					SAbsBApply();
					if (IfDefs.RKG_DEBUG_ABSORPTION) {
						LeveLogger.LL_ABSORPTION.print(" Absorb into BOTTOM due to (not" + q + ") and" + s);
					}
					return true;
				}
		return false;
	}

	public boolean absorbIntoConcept(TBox KB) {
		List<DLTree> Cons = new ArrayList<DLTree>();
		DLTree bestConcept = null;
		for (DLTree p : Disjuncts) {
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
		TConcept Concept = InAx.getConcept(bestConcept);
		if (LeveLogger.isAbsorptionActive()) {
			LeveLogger.LL_ABSORPTION.print(" C-Absorb GCI to concept " + Concept.getName());
			//dump(LeveLogger.LL_ABSORPTION);
			if (Cons.size() > 1) {
				LeveLogger.LL_ABSORPTION.print(" (other options are");
				for (int j = 1; j < Cons.size(); ++j) {
					LeveLogger.LL_ABSORPTION.print(" " + InAx.getConcept(Cons.get(j)).getName());
				}
				LeveLogger.LL_ABSORPTION.print(")");
			}
		}
		//		bestConcept = createTop();
		//		Cons.set(0, bestConcept);
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
		for (DLTree p : Disjuncts) {
			if (p.token() == NOT && (p.Child().token() == FORALL || p.Child().token() == LE)) {
				SAbsRAttempt();
				Cons.add(p);
				if (p.Child().Right().isBOTTOM()) {
					bestSome = p;
					break;
				}
			}
		}
		if (Cons.isEmpty()) {
			return false;
		}
		SAbsRApply();
		TRole Role;
		if (bestSome != null) {
			Role = TRole.resolveRole(bestSome.Child().Left());
		} else {
			Role = TRole.resolveRole(Cons.get(0).Child().Left());
		}
		if (LeveLogger.isAbsorptionActive()) {
			LeveLogger.LL_ABSORPTION.print(" R-Absorb GCI to the domain of role " + Role.getName());
			if (Cons.size() > 1) {
				LeveLogger.LL_ABSORPTION.print(" (other options are");
				for (int j = 1; j < Cons.size(); ++j) {
					LeveLogger.LL_ABSORPTION.print(" " + TRole.resolveRole(Cons.get(j).Child().Left()).getName());
				}
				LeveLogger.LL_ABSORPTION.print(")");
			}
		}
		Role.setDomain(createAnAxiom(bestSome));
		return true;
	}

	/** absorb into TOP; @return true if absorption performs */
	boolean absorbIntoTop(TBox KB) {
		TConcept C = null;
		// check whether the axiom is Top [= C
		for (DLTree p : Disjuncts) {
			if (InAx.isBot(p)) {
				continue;
			} else if (InAx.isPosCN(p)) // C found
			{
				if (C != null) {
					return false;
				}
				C = InAx.getConcept(p.Child());
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
			LeveLogger.LL_ABSORPTION.println("TAxiom.absorbIntoTop() T-Absorb GCI to axiom");
			if (desc != null) {
				LeveLogger.LL_ABSORPTION.println("s *TOP* [=" + desc + " and");
			}
			LeveLogger.LL.println(" " + C.getName() + " = *TOP*");
			//dump(LeveLogger.LL);
		}
		if (desc != null) {
			KB.addSubsumeAxiom(DLTreeFactory.createTop(), desc);
			//		if ( C.Description != null )
			//			KB.addSubsumeAxiom ( new Reference<DLTree>(createTop()), new Reference<DLTree>(KB.makeNonPrimitive ( C,  createTop() )) );
			//		else	// just make C = Top
			//			KB.makeNonPrimitive ( C, createTop() );
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
		if (arg0 instanceof TAxiom) {
			TAxiom ax = (TAxiom) arg0;
			return Disjuncts.equals(ax.Disjuncts);
			//			if (Disjuncts.size() != ax.Disjuncts.size()) {
			//				return false;
			//			}
			//			Set<DLTree> set = new HashSet<DLTree>(Disjuncts);
			//			for (DLTree d : ax.Disjuncts) {
			//				if (!set.contains(d)) {
			//					return false;
			//				}
			//			}
			//			set = new HashSet<DLTree>(ax.Disjuncts);
			//			for (DLTree d : Disjuncts) {
			//				if (!set.contains(d)) {
			//					return false;
			//				}
			//			}
			//			return true;
			//			return Disjuncts.size() == ax.Disjuncts.size() ? Disjuncts
			//					.containsAll(ax.Disjuncts)
			//					&& ax.Disjuncts.containsAll(Disjuncts) : false;
			//			return Disjuncts.equals(ax.Disjuncts);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Disjuncts.hashCode();
	}
}