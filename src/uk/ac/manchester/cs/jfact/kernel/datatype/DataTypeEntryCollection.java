package uk.ac.manchester.cs.jfact.kernel.datatype;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import uk.ac.manchester.cs.jfact.kernel.NameCreator;
import uk.ac.manchester.cs.jfact.kernel.NamedEntryCollection;

public final class DataTypeEntryCollection extends
		NamedEntryCollection<DataEntry> {
	/** data type */
	private final Datatypes type;

	@Override
	public void registerNew(DataEntry p) {
		p.setHostType(type);
	}

	public DataTypeEntryCollection(Datatypes t) {
		super(t.name(), new NameCreator<DataEntry>() {
			public DataEntry makeEntry(String n) {
				DataEntry toReturn = new DataEntry(n);
				toReturn.initComp();
				return toReturn;
			}
		});
		type = t;
	}

	public DataEntry getExpr() {
//		if (isLocked()) {
//			return null; // FIXME!! exception later
//		}
		DataEntry ret = registerElem(new DataEntry("expr"));
		return ret;
	}
}