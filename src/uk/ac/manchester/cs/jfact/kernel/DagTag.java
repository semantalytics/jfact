package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import java.util.EnumSet;

/** different Concept Expression tags */
public enum DagTag {
	// illegal entry
	dtBad,
	// operations
	dtTop, dtAnd, dtCollection, dtForall, dtLE, dtUAll, // \dall U.C
	dtIrr, // \neg\exists R.Self
	dtProj, // aux vertex with Projection FROM the current node
	dtNN, // NN-rule was applied
	// ID's
	dtPConcept, // primitive concept
	dtNConcept, // non-primitive concept
	dtPSingleton, dtNSingleton, dtDataType, dtDataValue, dtDataExpr;
	// data type with restrictions
	/** check whether given DagTag is a primitive named concept-like entity */
	public boolean isPNameTag() {
		return this == DagTag.dtPConcept || this == DagTag.dtPSingleton;
	}

	/** check whether given DagTag is a non-primitive named concept-like entity */
	public boolean isNNameTag() {
		return this == DagTag.dtNConcept || this == DagTag.dtNSingleton;
	}

	/** check whether given DagTag is a named concept-like entity */
	public boolean isCNameTag() {
		return isPNameTag() || isNNameTag();
	}

	private static final EnumSet<DagTag> complexConceptsEnumSet = EnumSet.of(
			DagTag.dtForall, DagTag.dtLE, DagTag.dtIrr, DagTag.dtUAll,
			DagTag.dtNN);

	/** @return true iff TAG represents complex concept */
	public boolean isComplexConcept() {
		return complexConceptsEnumSet.contains(this);
		//		tag == DagTag.dtForall || tag == DagTag.dtLE
		//				|| tag == DagTag.dtIrr || tag == DagTag.dtUAll
		//				|| tag == DagTag.dtNN;
	}
}
