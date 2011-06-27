package uk.ac.manchester.cs.jfact.split;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import uk.ac.manchester.cs.jfact.kernel.Ontology;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Axiom;

/// class to create modules of an ontology wrt module type
public class TModularizer {
	/// shared signature signature
	TSignature sig;
	/// internal syntactic locality checker
	SyntacticLocalityChecker Checker;
	/// signature updater
	TSignatureUpdater Updater;
	/// module as a list of axioms
	List<Axiom> Module = new ArrayList<Axiom>();

	/// add an axiom to a module
	void addAxiomToModule(Axiom axiom) {
		axiom.setInModule(true);
		Module.add(axiom);
		// update the signature
		axiom.accept(Updater);
	}

	/// mark the ontology O such that all the marked axioms creates the module wrt SIG
	void extractModule(Collection<Axiom> args) {
		int sigSize;
		Module.clear();
		//Module.reserve(args.size());
		// clear the module flag in the input
		for (Axiom p : args) {
			p.setInModule(false);
		}
		do {
			sigSize = sig.size();
			for (Axiom p : args) {
				if (!p.isInModule() && p.isUsed() && !Checker.local(p)) {
					addAxiomToModule(p);
				}
			}
		} while (sigSize != sig.size());
	}

	/// init c'tor
	public TModularizer() {
		Checker = new SyntacticLocalityChecker(sig);
		Updater = new TSignatureUpdater(sig);
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
		// here there is a star: do the cycle until stabilizastion
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

	/// extract module wrt SIGNATURE and TYPE from the set of axioms [BEGIN,END); @return result in the Set
	public void extract(Collection<Axiom> begin, TSignature signature,
			ModuleType type, Set<Axiom> Set) {
		extract(begin, signature, type);
		Set.clear();
		Set.addAll(Module);
	}

	/// extract module wrt SIGNATURE and TYPE from O
	public void extract(Ontology O, TSignature signature, ModuleType type) {
		extract(O.begin(), signature, type);
	}

	/// extract module wrt SIGNATURE and TYPE from O; @return result in the Set
	public void extract(Ontology O, TSignature signature, ModuleType type,
			Set<Axiom> Set) {
		extract(O.begin(), signature, type, Set);
	}

	/// get access to a signature
	public TSignature getSignature() {
		return sig;
	}
}