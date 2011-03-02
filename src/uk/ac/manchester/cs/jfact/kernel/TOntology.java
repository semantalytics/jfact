package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLAxiom;


public class TOntology {
	/** all the axioms */
	private final List<TDLAxiom> Axioms = new ArrayList<TDLAxiom>();
	/** expression manager that builds all the expressions for the axioms */
	private final TExpressionManager EManager = new TExpressionManager();
	/** id to be given to the next axiom */
	private int axiomId;
	/** true iff ontology was changed */
	private boolean changed;

	public TOntology() {
		axiomId = 0;
		changed = false;
	}

	/** @return true iff the ontology was changed since its last load */
	public boolean isChanged() {
		return changed;
	}

	/** set the processed marker to the end of the ontology */
	public void setProcessed() {
		changed = false;
	}

	/** add given axiom to the ontology */
	public TDLAxiom add(TDLAxiom p) {
		p.setId(++axiomId);
		Axioms.add(p);
		changed = true;
		return p;
	}

	/// mark all the axioms as not in the module
	void clearModuleInfo() {
		for (int i = 0; i < Axioms.size(); i++) {
			Axioms.get(i).setInModule(false);
		}
	}

	/** retract given axiom to the ontology */
	public void retract(TDLAxiom p) {
		if (p.getId() <= Axioms.size() && Axioms.get(p.getId() - 1).equals(p)) {
			changed = true;
			p.setUsed(false);
		}
	}

	/** clear the ontology */
	public void clear() {
		Axioms.clear();
		EManager.clear();
		changed = false;
	}

	/** get access to an expression manager */
	public TExpressionManager getExpressionManager() {
		return EManager;
	}

	// iterators
	/** RW begin() for the whole ontology */
	protected List<TDLAxiom> begin() {
		return Axioms;
	}

	/** size of the ontology */
	public int size() {
		return Axioms.size();
	}
}