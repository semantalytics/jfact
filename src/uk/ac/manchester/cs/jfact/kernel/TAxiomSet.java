package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.*;
import static uk.ac.manchester.cs.jfact.kernel.InAx.*;

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.cs.jfact.helpers.DLTree;
import uk.ac.manchester.cs.jfact.helpers.DLTreeFactory;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger;

public final class TAxiomSet {
	/** host TBox that holds all concepts/etc */
	private final TBox Host;
	/** set of axioms that accumilates incoming (and newly created) axioms; Tg */
	private List<TAxiom> Accum = new ArrayList<TAxiom>();

	private interface Abs {
		boolean AbsMethod(TAxiom ax);
	}

	/// set of absorption action, in order
	List<Abs> ActionVector = new ArrayList<TAxiomSet.Abs>();

	/** add already built GCI p */
	private void insertGCI(TAxiom p) {
		if (LeveLogger.isAbsorptionActive()) {
			LL_ABSORPTION.print("\n new axiom (" + Accum.size() + "):");
			p.dump(LL_ABSORPTION);
		}
		Accum.add(p);
	}

	/// absorb single axiom AX into negated concept; @return true if succeed
	boolean absorbIntoNegConcept(TAxiom ax) {
		return ax.absorbIntoNegConcept(Host);
	}

	/** insert GCI if new; @return true iff already exists */
	boolean insertIfNew(TAxiom q) {
		if (!Accum.contains(q)) {
			insertGCI(q);
			return false;
		}
		return true;
	}

	/** absorb single axiom AX into TOP; @return true if succeed */
	boolean absorbIntoTop(TAxiom ax) {
		return ax.absorbIntoTop(Host);
	}

	/// helper that inserts an axiom into Accum; @return bool if success
	boolean processNewAxiom(TAxiom q) {
		if (q == null) {
			return false;
		}
		if (insertIfNew(q)) {
			//delete q;
			return false;
		}
		return true;
	}

	/// replace a defined concept with its description
	boolean simplifyCN(TAxiom p) {
		return processNewAxiom(p.simplifyCN());
	}

	/// replace a universal restriction with a fresh concept
	boolean simplifyForall(TAxiom p) {
		return processNewAxiom(p.simplifyForall(Host));
	}

	/// absorb single axiom AX into BOTTOM; @return true if succeed
	boolean absorbIntoBottom(TAxiom ax) {
		return ax.absorbIntoBottom();
	}

	/** absorb single axiom AX into concept; @return true if succeed */
	boolean absorbIntoConcept(TAxiom ax) {
		return ax.absorbIntoConcept(Host);
	}

	/** absorb single axiom AX into role domain; @return true if succeed */
	boolean absorbIntoDomain(TAxiom ax) {
		return ax.absorbIntoDomain();
	}

	public TAxiomSet(TBox host) {
		Host = host;
	}

	/** add axiom for the GCI C [= D */
	public void addAxiom(DLTree C, DLTree D) {
		SAbsInput();
		TAxiom p = new TAxiom();
		p.add(C);
		p.add(DLTreeFactory.createSNFNot(D));
		insertGCI(p);
	}

	/** get number of (not absorbed) GCIs */
	private int size() {
		return Accum.size();
	}

	/** @return true if non-concept aborption were executed */
	public boolean wasRoleAbsorptionApplied() {
		String string = "SAbsRApply";
		return InAx.created.containsKey(string);
	}

	/** get GCI of all non-absorbed axioms */
	public DLTree getGCI() {
		List<DLTree> l = new ArrayList<DLTree>();
		for (TAxiom p : Accum) {
			l.add(p.createAnAxiom(null));
		}
		return DLTreeFactory.createSNFAnd(l);
	}

	/** split given axiom */
	boolean split(TAxiom p) {
		List<TAxiom> splitted = p.split();
		if (splitted.isEmpty()) {
			// nothing to split
			return false;
		}
		for (TAxiom q : splitted) {
			if (Accum.contains(q)) {
				// there is already such an axiom in process; delete it
				return false;
			}
		}
		// do the actual insertion if necessary
		for (TAxiom q : splitted) {
			insertGCI(q);
		}
		return true;
	}

	public int absorb() {
		// GCIs to process
		//List<TAxiom> Absorbed = new ArrayList<TAxiom>();
		List<TAxiom> GCIs = new ArrayList<TAxiom>();
		// we will change Accum (via split rule), so indexing and compare with size
		//int i=0;
		for (int i = 0; i < Accum.size(); i++) {
			TAxiom ax = Accum.get(i);
			if (LeveLogger.isAbsorptionActive()) {
				LL_ABSORPTION.print("\nProcessing (" + i++ + "):");
			}
			if (!absorbGCI(ax)) {
				//				Absorbed.add(ax);
				//			} else {
				GCIs.add(ax);
			}
		}
		// clear absorbed and remove them from Accum
		Accum = GCIs;
		if (LeveLogger.isAbsorptionActive()) {
			LL_ABSORPTION.print("\nAbsorption done with " + Accum.size() + " GCIs left\n");
		}
		PrintStatistics();
		return size();
	}

	private boolean absorbGCI(TAxiom p) {
		SAbsAction();
		for (Abs abs : ActionVector) {
			if (abs.AbsMethod(p)) {
				return true;
			}
		}
		if (LeveLogger.isAbsorptionActive()) {
			LL_ABSORPTION.print(" keep as GCI");
		}
		return false;
	}

	public boolean initAbsorptionFlags(final String flags) {
		ActionVector.clear();
		for (char c : flags.toCharArray()) {
			switch (c) {
				case 'B':
					ActionVector.add(new Abs() {
						public boolean AbsMethod(TAxiom ax) {
							return absorbIntoBottom(ax);
						}
					});
					break;
				case 'T':
					ActionVector.add(new Abs() {
						public boolean AbsMethod(TAxiom ax) {
							return absorbIntoTop(ax);
						}
					});
					break;
				case 'E':
					ActionVector.add(new Abs() {
						public boolean AbsMethod(TAxiom ax) {
							return simplifyCN(ax);
						}
					});
					break;
				case 'C':
					ActionVector.add(new Abs() {
						public boolean AbsMethod(TAxiom ax) {
							return absorbIntoConcept(ax);
						}
					});
					break;
				case 'N':
					ActionVector.add(new Abs() {
						public boolean AbsMethod(TAxiom ax) {
							return absorbIntoNegConcept(ax);
						}
					});
					break;
				case 'F':
					ActionVector.add(new Abs() {
						public boolean AbsMethod(TAxiom ax) {
							return simplifyForall(ax);
						}
					});
					break;
				case 'R':
					ActionVector.add(new Abs() {
						public boolean AbsMethod(TAxiom ax) {
							return absorbIntoDomain(ax);
						}
					});
					break;
				case 'S':
					ActionVector.add(new Abs() {
						public boolean AbsMethod(TAxiom ax) {
							return split(ax);
						}
					});
					break;
				default:
					return true;
			}
		}
		LL.print("Init absorption order as ");
		LL.println(flags);
		return false;
	}

	private void PrintStatistics() {
		if (!created.containsKey("SAbsAction")) {
			return;
		}
		LL.print("\nAbsorption dealt with " + get("SAbsInput") + " input axioms\nThere were made " + get("SAbsAction") + " absorption actions, of which:");
		if (get("SAbsRepCN") > 0) {
			LL.print("\n\t" + get("SAbsRepCN") + " concept name replacements");
		}
		if (get("SAbsRepForall") > 0) {
			LL.print("\n\t" + get("SAbsRepForall") + " universals replacements");
		}
		if (get("SAbsSplit") > 0) {
			LL.print("\n\t" + get("SAbsSplit") + " conjunction splits");
		}
		if (get("SAbsBApply") > 0) {
			LL.print("\n\t" + get("SAbsBApply") + " BOTTOM absorptions");
		}
		if (get("SAbsTApply") > 0) {
			LL.print("\n\t" + get("SAbsTApply") + " TOP absorptions");
		}
		if (get("SAbsCApply") > 0) {
			LL.print("\n\t" + get("SAbsCApply") + " concept absorption with " + get("SAbsCAttempt") + " possibilities");
		}
		if (get("SAbsNApply") > 0) {
			LL.print("\n\t" + get("SAbsNApply") + " negated concept absorption with " + get("SAbsNAttempt") + " possibilities");
		}
		if (get("SAbsRApply") > 0) {
			LL.print("\n\t" + get("SAbsRApply") + " role domain absorption with " + get("SAbsRAttempt") + " possibilities");
		}
		if (!Accum.isEmpty()) {
			LL.print("\nThere are " + Accum.size() + " GCIs left");
		}
	}
}