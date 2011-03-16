package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.BitSet;

public class TKBFlags {
	enum Flags {
		GCI, RnD, Reflexive
	}

	private final BitSet bits = new BitSet();

	/** flag for GCIs */
	public boolean isGCI() {
		return bits.get(Flags.GCI.ordinal());
	}

	public void setGCI(boolean action) {
		if (action) {
			bits.set(Flags.GCI.ordinal());
		} else {
			bits.clear(Flags.GCI.ordinal());
		}
	}

	/** flag for Range and Domain axioms */
	public boolean isRnD() {
		return bits.get(Flags.RnD.ordinal());
	}

	public void setRnD() {
		bits.set(Flags.RnD.ordinal());
	}

	/** flag for Reflexive roles */
	public boolean isReflexive() {
		return bits.get(Flags.Reflexive.ordinal());
	}

	public void setReflexive(boolean action) {
		if (action) {
			bits.set(Flags.Reflexive.ordinal());
		} else {
			bits.clear(Flags.Reflexive.ordinal());
		}
	}
}