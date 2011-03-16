package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import uk.ac.manchester.cs.jfact.dep.DepSet;
import uk.ac.manchester.cs.jfact.dep.DepSetFactory;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;

public final class ConceptWDep {
	/** "pointer" to a concept in DAG */
	private final int Concept;
	/** dep-set for a concept */
	private final DepSet depSet;

	/** c'tor with empty dep-set */
	public ConceptWDep(int p) {
		Concept = p;
		depSet = DepSetFactory.create();
	}

	/** usual c'tor */
	protected ConceptWDep(int p, final DepSet dep) {
		Concept = p;
		depSet = DepSetFactory.create(dep);
	}

	public int getConcept() {
		return Concept;
	}

	/** get dep-set part */
	public final DepSet getDep() {
		return depSet;
	}

	/** add dep-set to a CWD */
	protected void addDep(final DepSet d) {
		depSet.add(d);
	}

	/** print concept and a dep-set */
	@Override
	public String toString() {
		String string = Concept + depSet.toString();
		return string;
	}

	@Override
	public int hashCode() {
		return Concept;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof ConceptWDep) {
			return Concept == ((ConceptWDep) obj).Concept;
		}
		return false;
	}

	public void print(LogAdapter lL) {
		lL.print(Concept);
		if (depSet != null) {
			depSet.Print(lL);
		}
	}
}