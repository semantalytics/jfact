package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import java.util.BitSet;

import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;


public class TNamedEntry {
	/** name of the entry */
	protected final String extName;
	/** entry identifier */
	protected int extId;
	protected final BitSet bits = new BitSet();

	public TNamedEntry(final String name) {
		assert name != null;
		extName = name;
		extId = 0; // sets local id
	}

	/** gets name of given entry */
	public String getName() {
		return extName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof TNamedEntry) {
			TNamedEntry e = (TNamedEntry) obj;
			return extName.equals(e.extName) && extId == e.extId;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return extName.hashCode();// + extId;
	}

	/** set internal ID */
	public void setId(int id) {
		extId = id;
	}

	/** get internal ID */
	public int getId() {
		return extId;
	}

	@Override
	public String toString() {
		return extName + " " + extId;
	}

	protected enum Flags {
		//0x1
		System,
		//0x1000
		Top,
		//0x2000
		Bottom,
		//0x2
		CompletelyDefined,
		//0x4
		NonClassifiable,
		//0x10
		Primitive,
		//0x20
		HasSP,
		//0x40
		Nominal,
		//0x10
		Simple,
		//0x40
		Finished,
		//0x20
		DataRole
	}

	/** a System flag */
	public boolean isSystem() {
		return bits.get(Flags.System.ordinal());
	}

	public void setSystem() {
		bits.set(Flags.System.ordinal());
	}

	// hierarchy interface
	/** a Top-of-the-hierarchy flag */
	public boolean isTop() {
		return bits.get(Flags.Top.ordinal());
	}

	/** a Bottom-of-the-hierarchy flag */
	public boolean isBottom() {
		return bits.get(Flags.Bottom.ordinal());
	}

	public void Print(LogAdapter o) {
		o.print(getName());
	}
}