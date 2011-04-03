package uk.ac.manchester.cs.jfact.kernel.datatype;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.logger;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.manchester.cs.jfact.dep.DepSet;
import uk.ac.manchester.cs.jfact.dep.DepSetFactory;
import uk.ac.manchester.cs.jfact.helpers.DLVertex;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;
import uk.ac.manchester.cs.jfact.helpers.Reference;
import uk.ac.manchester.cs.jfact.helpers.UnreachableSituationException;
import uk.ac.manchester.cs.jfact.kernel.DLDag;
import uk.ac.manchester.cs.jfact.kernel.NamedEntry;
import uk.ac.manchester.cs.jfact.kernel.datatype.DataTypeAppearance.DepDTE;
import uk.ac.manchester.cs.jfact.kernel.dl.DataTypeName;

public final class DataTypeReasoner {
	/** map Type.pName.Type appearance */
	private final Map<Datatypes, DataTypeAppearance> map = new LinkedHashMap<Datatypes, DataTypeAppearance>();
	/** external DAG */
	private final DLDag dlHeap;
	/** dep-set for the clash for *all* the types */
	private final Reference<DepSet> clashDep = new Reference<DepSet>();

	/** process data value */
	private boolean processDataValue(boolean pos, DataEntry c, final DepSet dep) {
		DataTypeAppearance type = map.get(c.getDatatype());
		if (pos) {
			type.setPType(new DepDTE(c, dep));
		}
		// create interval [c,c]
		DataInterval constraints = new DataInterval();
		constraints.updateMin( /*excl=*/false, c.getComp());
		constraints.updateMax( /*excl=*/false, c.getComp());
		return type.addInterval(pos, constraints, dep);
	}

	/** process data expr */
	private boolean processDataExpr(boolean pos, final DataEntry c, final DepSet dep) {
		final DataInterval constraints = c.getFacet();
		if (constraints.isEmpty()) {
			return false;
		}
		DataTypeAppearance type = map.get(c.getDatatype());
		if (pos) {
			type.setPType(new DepDTE(c, dep));
		}
		return type.addInterval(pos, constraints, dep);
	}

	/** get data entry structure by a BP */
	private NamedEntry getDataEntry(int p) {
		return dlHeap.get(p).getConcept();
	}

	/** get TDE with a dep-set by a CWD */
	private DepDTE getDTE(int p, final DepSet dep) {
		return new DepDTE(getDataEntry(p), dep);
	}

	/** c'tor: save DAG */
	public DataTypeReasoner(final DLDag dag) {
		dlHeap = dag;
	}

	// managing DTR
	/** add data type to the reasoner */
	protected void registerDataType(Datatypes p) {
		map.put(p, new DataTypeAppearance(clashDep));
	}

	/** prepare types for the reasoning */
	public void clear() {
		for (DataTypeAppearance p : map.values()) {
			p.clear();
		}
	}

	/** get clash-set */
	public DepSet getClashSet() {
		return clashDep.getReference();
	}

	public boolean addDataEntry(int p, final DepSet dep) {
		final DLVertex v = dlHeap.get(p);
		NamedEntry dataEntry = getDataEntry(p);
		switch (v.getType()) {
			case dtDataType: {
				DataTypeAppearance type = map.get(dataEntry instanceof DataEntry ? ((DataEntry) dataEntry).getDatatype() : ((DataTypeName) dataEntry).getDatatype());
				logger.print(Templates.INTERVAL, (p > 0 ? "+" : "-"), dataEntry.getName());
				if (p > 0) {
					type.setPType(getDTE(p, dep));
				} else {
					type.setNType(getDTE(p, dep));
				}
				return false;
			}
			case dtDataValue:
				return processDataValue(p > 0, (DataEntry) dataEntry, dep);
			case dtDataExpr:
				return processDataExpr(p > 0, (DataEntry) dataEntry, dep);
			case dtAnd:
				return false;
			default:
				//TODO this case needs investigation; is it a mistake? whenever something is supposed to be a data node and is actually a primitive concept?
				// or is it just a regular clash?
				System.out.println("DataTypeReasoner.addDataEntry() warning: this case might indicate errors in the datatype reasoning");
				return true;
				//throw new UnreachableSituationException(v.toString());
		}
	}

	public boolean checkClash() {
		DataTypeAppearance type = null;
		for (DataTypeAppearance p : map.values()) {
			if (p.hasPType()) {
				if (type == null) {
					type = p;
				} else {
					Datatypes type_datatype = type.getPDatatype();
					Datatypes p_datatype = p.getPDatatype();
					if (!p_datatype.compatible(type_datatype) && !type_datatype.compatible(p_datatype)) {
						logger.print(Templates.CHECKCLASH);
						clashDep.setReference(DepSetFactory.plus(type.getPType().second, p.getPType().second));
						return true;
					}
					// if one of them is compatible with the other but not the other way around, then replace type with the most restrictive one
					// XXX this is still dubious
					if (type_datatype.compatible(p_datatype) && !p_datatype.compatible(type_datatype)) {
						type = p;
					}
					// else irrelevant: type is already the most restrictive
				}
			}
		}
		return type != null ? type.checkPNTypeClash() : false;
	}
}