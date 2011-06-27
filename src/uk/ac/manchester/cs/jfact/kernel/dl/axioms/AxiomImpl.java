package uk.ac.manchester.cs.jfact.kernel.dl.axioms;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Axiom;

public abstract class AxiomImpl implements Axiom {
	/** id of the axiom */
	private int id;
	/// flag to show whether or not the axiom is in the module
	boolean inModule;
	/** flag to show whether it is used (to support retraction) */
	private boolean used;

	public AxiomImpl() {
		used = true;
		inModule = false;
	}

	public int getId() {
		return id;
	}

	public boolean isInModule() {
		return inModule;
	}

	public boolean isUsed() {
		return used;
	}

	public void setId(int Id) {
		id = Id;
	}

	public void setInModule(boolean inModule) {
		this.inModule = inModule;
	}

	public void setUsed(boolean Used) {
		used = Used;
	}
}
