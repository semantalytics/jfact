package uk.ac.manchester.cs.jfact.split;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Axiom;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.NamedEntity;

class SigIndex {
	/// map between entities and axioms that contains them in their signature
	Map<NamedEntity, Set<Axiom>> Base;
	/// locality checker
	SyntacticLocalityChecker Checker;
	/// sets of axioms non-local wrt the empty signature
	Set<Axiom> NonLocalTrue = new HashSet<Axiom>();
	Set<Axiom> NonLocalFalse = new HashSet<Axiom>();
	/// empty signature to test the non-locality
	TSignature emptySig = new TSignature();

	/// add axiom AX to the non-local set with top-locality value TOP
	void checkNonLocal(Axiom ax, boolean top) {
		emptySig.setLocality(top);
		if (!Checker.local(ax)) {
			if (top) {
				NonLocalFalse.add(ax);
			} else {
				NonLocalTrue.add(ax);
			}
		}
	}

	/// empty c'tor
	SigIndex() {
		Checker = new SyntacticLocalityChecker(emptySig);
	}

	// work with axioms
	/// register an axiom
	void registerAx(Axiom ax) {
		for (NamedEntity p : ax.getSignature().begin()) {
			Base.get(p).add(ax);
		}
		// check whether the axiom is non-local
		checkNonLocal(ax, /* top= */false);
		checkNonLocal(ax, /* top= */true);
	}

	/// unregister an axiom AX
	void unregisterAx(Axiom ax) {
		for (NamedEntity p : ax.getSignature().begin()) {
			Base.get(p).remove(ax);
		}
		// remove from the non-locality
		NonLocalFalse.remove(ax);
		NonLocalTrue.remove(ax);
	}

	/// process an axiom wrt its Used status
	void processAx(Axiom ax) {
		if (ax.isUsed()) {
			registerAx(ax);
		} else {
			unregisterAx(ax);
		}
	}

	/// process the range [begin,end) of axioms
	void processRange(Collection<Axiom> c) {
		for (Axiom ax : c) {
			processAx(ax);
		}
	}

	// get the set by the index
	/// given an entity, return a set of all axioms that tontain this entity in a signature
	Set<Axiom> getAxioms(NamedEntity entity) {
		return Base.get(entity);
	}

	/// get the non-local axioms with top-locality value TOP
	Set<Axiom> getNonLocal(boolean top) {
		return top ? NonLocalFalse : NonLocalTrue;
	}
}
