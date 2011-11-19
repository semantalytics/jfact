package uk.ac.manchester.cs.jfact.split;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import uk.ac.manchester.cs.jfact.kernel.Ontology;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Axiom;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.NamedEntity;

/// class to create modules of an ontology wrt module type
public class TModularizer {
	/// shared signature signature
	TSignature sig = new TSignature();
	/// internal syntactic locality checker
	SyntacticLocalityChecker Checker;
	/// signature updater
	//TSignatureUpdater Updater;
	/// module as a list of axioms
	List<Axiom> Module = new ArrayList<Axiom>();
	/// pointer to a sig index; if not NULL then use optimized algo
	SigIndex sigIndex;
	/// queue of unprocessed entities
	List<NamedEntity> WorkQueue = new ArrayList<NamedEntity>();
	/// number of locality check calls
	long nChecks;
	/// number of non-local axioms
	long nNonLocal;

	/// update SIG wrt the axiom signature
	void addAxiomSig(Axiom axiom) {
		TSignature axiomSig = axiom.getSignature();
		if (sigIndex != null) {
			for (NamedEntity p : axiomSig.begin()) {
				if (!sig.containsNamedEntity(p)) {
					WorkQueue.add(p);
				}
			}
		}
		sig.add(axiomSig);
	}

	/// add an axiom to a module
	void addAxiomToModule(Axiom axiom) {
		axiom.setInModule(true);
		Module.add(axiom);
		// update the signature
		addAxiomSig(axiom);
	}

	/// @return true iff an AXiom is non-local
	boolean isNonLocal(Axiom ax) {
		++nChecks;
		if (Checker.local(ax)) {
			return false;
		}
		++nNonLocal;
		return true;
	}

	/// add an axiom if it is non-local (or in noCheck is true)
	void addNonLocal(Axiom ax, boolean noCheck) {
		if (noCheck || isNonLocal(ax)) {
			addAxiomToModule(ax);
		}
	}

	/// mark the ontology O such that all the marked axioms creates the module wrt SIG
	void extractModuleLoop(Collection<Axiom> args) {
		int sigSize;
		do {
			sigSize = sig.size();
			for (Axiom p : args) {
				if (!p.isInModule() && p.isUsed()) {
					addNonLocal(p, /* noCheck= */false);
				}
			}
		} while (sigSize != sig.size());
	}

	/// add all the non-local axioms from given axiom-set AxSet
	void addNonLocal(Set<Axiom> AxSet, boolean noCheck) {
		for (Axiom q : AxSet) {
			if (!q.isInModule() && q.isInSS()) {
				addNonLocal(q, noCheck);
			}
		}
	}

	/// build a module traversing axioms by a signature
	void extractModuleQueue() {
		// init queue with a sig
		for (NamedEntity p : sig.begin()) {
			WorkQueue.add(p);
		}
		// add all the axioms that are non-local wrt given value of a top-locality
		addNonLocal(sigIndex.getNonLocal(sig.topCLocal()), /* noCheck= */true);
		// main cycle
		while (!WorkQueue.isEmpty()) {
			NamedEntity entity = WorkQueue.remove(0);
			// for all the axioms that contains entity in their signature
			addNonLocal(sigIndex.getAxioms(entity), /* noCheck= */false);
		}
	}

	/// extract module wrt presence of a sig index
	void extractModule(Collection<Axiom> args) {
		Module.clear();
		//Module.reserve(args.size());
		// clear the module flag in the input
		for (Axiom p : args) {
			p.setInModule(false);
		}
		//		do {
		//			sigSize = sig.size();
		//			for (Axiom p : args) {
		//				if (!p.isInModule() && p.isUsed() && !Checker.local(p)) {
		//					addAxiomToModule(p);
		//				}
		//			}
		//		} while (sigSize != sig.size());
		if (sigIndex != null) {
			for (Axiom p : args) {
				if (p.isUsed()) {
					p.setInSS(true);
				}
			}
			extractModuleQueue();
			for (Axiom p : args) {
				p.setInSS(false);
			}
		} else {
			extractModuleLoop(args);
		}
	}

	/// init c'tor
	public TModularizer() {
		Checker = new SyntacticLocalityChecker(sig);
		sigIndex = null;
		nChecks = 0;
		nNonLocal = 0;
	}

	/// set sig index to a given value
	void setSigIndex(SigIndex p) {
		sigIndex = p;
	}

	/// extract module wrt SIGNATURE and TYPE from the set of axioms [BEGIN,END)
	void extract(Collection<Axiom> begin, TSignature signature, ModuleType type) {
		boolean topLocality = type == ModuleType.M_TOP;
		sig = signature;
		sig.setLocality(topLocality);
		extractModule(begin);
		if (type != ModuleType.M_STAR) {
			return;
		}
		// here there is a star: do the cycle until stabilization
		int size;
		List<Axiom> oldModule = new ArrayList<Axiom>();
		do {
			size = Module.size();
			oldModule.clear();
			oldModule.addAll(Module);
			topLocality = !topLocality;
			sig = signature;
			sig.setLocality(topLocality);
			extractModule(oldModule);
		} while (size != Module.size());
	}

	/// get number of checks made
	long getNChecks() {
		return nChecks;
	}

	/// get number of axioms that were local
	long getNNonLocal() {
		return nNonLocal;
	}

	/// extract module wrt SIGNATURE and TYPE from the set of axioms [BEGIN,END); @return result in the Set
	public void extract(Collection<Axiom> begin, TSignature signature, ModuleType type,
			Set<Axiom> Set) {
		extract(begin, signature, type);
		Set.clear();
		Set.addAll(Module);
	}

	/// extract module wrt SIGNATURE and TYPE from O
	public void extract(Ontology O, TSignature signature, ModuleType type) {
		extract(O.begin(), signature, type);
	}

	/// extract module wrt SIGNATURE and TYPE from O; @return result in the Set
	public void extract(Ontology O, TSignature signature, ModuleType type, Set<Axiom> Set) {
		extract(O.begin(), signature, type, Set);
	}

	/// get access to a signature
	public TSignature getSignature() {
		return sig;
	}
}