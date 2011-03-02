package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.LL;
import static uk.ac.manchester.cs.jfact.kernel.ClassifiableEntry.resolveSynonym;

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.cs.jfact.dep.DepSet;
import uk.ac.manchester.cs.jfact.dep.DepSetFactory;
import uk.ac.manchester.cs.jfact.helpers.Helper;
import uk.ac.manchester.cs.jfact.helpers.Pair;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;


public final  class NominalReasoner extends DlSatTester {
	/** all nominals defined in TBox */
	protected List<TIndividual> Nominals = new ArrayList<TIndividual>();

	/** there are nominals */
	@Override
	public boolean hasNominals() {
		return true;
	}

	//-----------------------------------------------------------------------------
	//--		internal nominal reasoning interface
	//-----------------------------------------------------------------------------
	/** init vector of nominals defined in TBox */
	//void initNominalVector (  );
	/** create cache entry for given singleton */
	protected void registerNominalCache(TIndividual p) {
		DLHeap.setCache(p.getpName(), createModelCache(p.getNode()
				.resolvePBlocker()));
	}

	/** init single nominal node */
	protected boolean initNominalNode(TIndividual nom) {
		DlCompletionTree node = CGraph.getNewNode();
		node.setNominalLevel();
		nom.setNode(node); // init nominal with associated node
		return initNewNode(node, DepSetFactory.create(), nom.getpName()); // ABox is inconsistent
	}

	/** create nominal nodes for all individuals in TBox */
	//boolean initNominalCloud ( void );
	/** make an R-edge between related nominals */
	//boolean initRelatedNominals ( const TRelated* rel );
	/** use classification information for the nominal P */
	protected void updateClassifiedSingleton(TIndividual p) {
		registerNominalCache(p);
		if (p.getNode().isPBlocked()) {
			// BP of the individual P is merged to
			int bp = p.getNode().getBlocker().label().get_sc().get(0)
					.getConcept();
			TIndividual blocker = (TIndividual) DLHeap.get(bp).getConcept();
			assert blocker.getNode().equals(p.getNode().getBlocker());
			tBox.SameI.put(p, new Pair<TIndividual, Boolean>(blocker, p
					.getNode().getPurgeDep().isEmpty()));
		}
	}

	public NominalReasoner(TBox tbox, IFOptionSet Options) {
		super(tbox, Options);
		for (TIndividual pi : tBox.i_begin()) {
			if (!pi.isSynonym()) {
				Nominals.add(pi);
			}
		}
	}

	/** prerpare Nominal Reasoner to a new job */
	@Override
	protected void prepareReasoner() {
		//System.out.println("NominalReasoner.prepareReasoner()");
		LL.print("\nInitNominalReasoner:");
		restore(1);
		// check whether branching op is not a barrier...
		if (!(bContext instanceof BCBarrier)) { // replace it with a barrier
			//System.out.println("NominalReasoner.prepareReasoner() addbarrier");
			Stack.pop();
			createBCBarrier();
		}
		// save the barrier (also remember the entry to be produced)
		save();
		// free the memory used in the pools before
		Stack.clearPools();
		// clear last session information
		resetSessionFlags();
	}

	/** check whether ontology with nominals is consistent */
	public boolean consistentNominalCloud() {
		LL.print("\n\nChecking consistency of an ontology with individuals:\n");
		boolean result = false;
		if (initNewNode(CGraph.getRoot(), DepSetFactory.create(), Helper.bpTOP)
				|| initNominalCloud()) {
			LL.print("\ninit done\n");
			result = false;
		} else {
			LL.print("\nrunning sat...");
			result = runSat();
			LL.print(" done: ");
			LL.print(result);
			LL.print("\n");
		}
		if (result && noBranchingOps()) {
			LL.print("InitNominalReasoner[");
			curNode = null;
			createBCBarrier();
			save();
			nonDetShift = 1;
			LL.print("]");
		}
		LL.print(Templates.CONSISTENT_NOMINAL, (result ? "consistent"
				: "INCONSISTENT"));
		if (!result) {
			return false;
		}
		for (TIndividual p : Nominals) {
			updateClassifiedSingleton(p);
		}
		return true;
	}

	private boolean initNominalCloud() {
		for (TIndividual p : Nominals) {
			if (initNominalNode(p)) {
				return true;
			}
		}
		if (!tBox.isPrecompleted()) {
			for (int i = 0; i < tBox.getRelatedI().size(); i += 2) {
				if (initRelatedNominals(tBox.getRelatedI().get(i))) {
					return true;
				}
			}
		}
		if (tBox.getDifferent().isEmpty()) {
			return false;
		}
		DepSet dummy = DepSetFactory.create();
		for (List<TIndividual> r : tBox.getDifferent()) {
			CGraph.initIR();
			for (TIndividual p : r) {
				if (CGraph.setCurIR(resolveSynonym(p).getNode(), dummy)) {
					return true;
				}
			}
			CGraph.finiIR();
		}
		return false;
	}

	@Override
	boolean isNNApplicable(final TRole r, int C, int stopper) {
		if (!curNode.isNominalNode()) {
			return false;
		}
		//XXX in this reasoner this should not happen - should be in nominalreasoner
		if (curNode.isLabelledBy(stopper)) {
			return false;
		}
		List<DlCompletionTreeArc> neighbour = curNode.getNeighbour();
		for (int i = 0; i < neighbour.size(); i++) {
			DlCompletionTreeArc p = neighbour.get(i);
			DlCompletionTree suspect = p.getArcEnd();
			if (p.isPredEdge() && suspect.isBlockableNode() && p.isNeighbour(r)
					&& suspect.isLabelledBy(C)) {
				LL.print(Templates.NN, suspect.getId());
				return true;
			}
		}
		return false;
	}

	private boolean initRelatedNominals(final TRelated rel) {
		DlCompletionTree from = resolveSynonym(rel.getA()).getNode();
		DlCompletionTree to = resolveSynonym(rel.getB()).getNode();
		TRole R = resolveSynonym(rel.getRole());
		DepSet dep = DepSetFactory.create();
		if (R.isDisjoint() && checkDisjointRoleClash(from, to, R, dep)) {
			return true;
		}
		DlCompletionTreeArc pA = CGraph.addRoleLabel(from, to, false, R, dep);
		return setupEdge(pA, dep, 0);
	}

	/** create BC for the barrier */
	private void createBCBarrier() {
		bContext = Stack.pushBarrier();
	}
}