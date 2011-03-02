package uk.ac.manchester.cs.jfact.kernel.actors;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.cs.jfact.kernel.ClassifiableEntry;
import uk.ac.manchester.cs.jfact.kernel.TConcept;
import uk.ac.manchester.cs.jfact.kernel.TIndividual;
import uk.ac.manchester.cs.jfact.kernel.TaxonomyVertex;


public class RIActor implements Actor {
	private final List<TIndividual> acc = new ArrayList<TIndividual>();

	private boolean tryEntry(ClassifiableEntry p) {
		if (p.isSystem() || !((TConcept) p).isSingleton()) {
			return false;
		}
		acc.add((TIndividual) p);
		return true;
	}

	public RIActor() {
	}

	public boolean apply(TaxonomyVertex v) {
		boolean ret = tryEntry(v.getPrimer());
		for (ClassifiableEntry p : v.begin_syn()) {
			ret |= tryEntry(p);
		}
		return ret;
	}

	public List<TIndividual> getAcc() {
		return acc;
	}
}