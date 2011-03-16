package uk.ac.manchester.cs.jfact.dep;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
public class DepSetFactory {
	public final static DepSet create() {
		return new DepSet();
	}

	public final static DepSet create(int i) {
		return new DepSet(i);
	}

	public final static DepSet create(DepSet dep) {
		DepSet toReturn = new DepSet();
		toReturn.add(dep);
		return toReturn;
	}

	public final static DepSet plus(final DepSet ds1, final DepSet ds2) {
		DepSet toReturn = new DepSet();
		toReturn.add(ds1);
		toReturn.add(ds2);
		return toReturn;
	}
}