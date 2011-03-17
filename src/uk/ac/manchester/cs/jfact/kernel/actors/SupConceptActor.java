package uk.ac.manchester.cs.jfact.kernel.actors;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import uk.ac.manchester.cs.jfact.kernel.ClassifiableEntry;
import uk.ac.manchester.cs.jfact.kernel.TaxonomyVertex;

/** class for exploring concept taxonomy to find super classes */
public class SupConceptActor implements Actor {
	protected final ClassifiableEntry pe;

	//	private void entry(ClassifiableEntry q) {
	//		if (pe.equals(q)) {
	//			throw new RuntimeException();
	//		}
	//	}
	public SupConceptActor(ClassifiableEntry q) {
		pe = q;
	}

	public boolean apply(TaxonomyVertex v) {
		//entry(v.getPrimer());
		//XXX this needs refining, it's using exceptions to close cycles early
		if (pe.equals(v.getPrimer()) || v.begin_syn().contains(pe)) {
			return false;
			//throw new RuntimeException();
		}
		//		for (ClassifiableEntry p : v.begin_syn()) {
		//			entry(p);
		//		}
		return true;
	}
}