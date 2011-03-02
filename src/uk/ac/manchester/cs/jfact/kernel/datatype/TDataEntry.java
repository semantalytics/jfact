package uk.ac.manchester.cs.jfact.kernel.datatype;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import static uk.ac.manchester.cs.jfact.helpers.Helper.bpINVALID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;


import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

import uk.ac.manchester.cs.jfact.kernel.TNamedEntry;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataTypeName;

public class TDataEntry extends TNamedEntry {
	/** label to use in relevant-only checks */
	//private long rel;
	/** corresponding type (Type has null in the field) */
	private TDLDataTypeName Type;
	/** DAG index of the entry */
	private int pName;
	/** ComparableDT, used only for values */
	private Literal comp;
	/** restriction to the entry */
	private final TDataInterval Constraints = new TDataInterval();

	/** create data entry with given name */
	public TDataEntry(final String name) {
		super(name);
		Type = null;
		pName = bpINVALID;
	}

	/** check if data entry represents basic data type */
	public boolean isBasicDataType() {
		return Type == null && Constraints.isEmpty();
	}

	/** check if data entry represents data value */
	public boolean isDataValue() {
		return Type != null && Constraints.isEmpty();
	}

	/** set host data type for the data value */
	public void setHostType(TDLDataTypeName type) {
		Type = type;
		//XXX crappy implementation still here
		if (getName().equals("expr") && Type.getDatatype() != Datatypes.STRING) {
			if (Type.getDatatype() == Datatypes.DATETIME) {
				// then a neutral date of some sort
				try {
					comp = Type.getDatatype().build(
							DatatypeFactory.newInstance()
									.newXMLGregorianCalendar());
				} catch (DatatypeConfigurationException e) {
					throw new ReasonerInternalException(e);
				}
			} else {
				// then init with 0
				initComp("0");
			}
		} else {
			initComp();
		}
	}

	public void initComp() {
		if (Type != null) {
			comp = Type.getDatatype().parse(getName());
		}
	}

	public void initComp(String s) {
		if (Type != null) {
			comp = Type.getDatatype().parse(s);
		}
	}

	public Literal getComp() {
		return comp;
	}

	/** get host type */
	public TDLDataTypeName getType() {
		return Type;
	}

	public final Datatypes getDatatype() {
		return Type.getDatatype();
	}

	// facet part
	/** get RW access to constraints of the DE */
	public TDataInterval getFacet() {
		return Constraints;
	}

	/** get pointer to DAG entry corresponding to the data entry */
	public int getBP() {
		return pName;
	}

	/** set DAG index of the data entry */
	public void setBP(int p) {
		pName = p;
	}
}