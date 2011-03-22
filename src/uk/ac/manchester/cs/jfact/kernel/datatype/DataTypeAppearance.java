package uk.ac.manchester.cs.jfact.kernel.datatype;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.manchester.cs.jfact.dep.DepSet;
import uk.ac.manchester.cs.jfact.dep.DepSetFactory;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;
import uk.ac.manchester.cs.jfact.helpers.Reference;
import uk.ac.manchester.cs.jfact.kernel.dl.DataTypeName;

public final class DataTypeAppearance {
	public static class DepDTE {
		protected DepDTE(Object e, DepSet s) {
			first = e;
			second = DepSetFactory.create(s);
		}

		protected final Object first;
		protected final DepSet second;
	}

	/** positive type appearance */
	private DepDTE pType;
	private Datatypes pdatatype;
	/** negative type appearance */
	private DepDTE nType;
	/** interval of possible values */
	private List<DepInterval> constraints = new ArrayList<DepInterval>();
	/** accumulated dep-set */
	private DepSet accDep = DepSetFactory.create();
	/** dep-set for the clash */
	private final Reference<DepSet> clashDep;
	// local values for the updating
	/** local value for the min/max flag */
	private boolean localMin;
	/** local value for the incl/excl flag */
	private boolean localExcl;
	/** local value for the added value */
	private Literal localValue;
	/** local dep-set for the update */
	private DepSet localDep;

	/**
	 * set clash dep-set to DEP, report with given REASON; @return true to
	 * simplify callers
	 */
	private boolean reportClash(final DepSet dep, final String reason) {
		logger.print(Templates.CLASH, reason); // inform about clash...
		clashDep.setReference(dep);
		return true;
	}

	/** set the local parameters for updating */
	private void setLocal(boolean min, boolean excl, final Literal value, final DepSet dep) {
		localMin = min;
		localExcl = excl;
		localValue = value.getDatatype().build(value.getValue());
		localDep = DepSetFactory.create(dep);
	}

	/**
	 * update and add a single interval I to the constraints. @return true iff
	 * clash occurs
	 */
	private boolean addUpdatedInterval(DepInterval i) {
		Reference<DepSet> ref = new Reference<DepSet>(localDep);
		if (!i.consistent(localValue, ref)) {
			localDep = ref.getReference();
			return reportClash(localDep, "C-IT");
		}
		if (!i.update(localMin, localExcl, localValue, localDep)) {
			constraints.add(i);
		}
		ref = new Reference<DepSet>(accDep);
		if (!hasPType() || !i.checkMinMaxClash(ref)) {
			constraints.add(i);
		}
		accDep = ref.getReference();
		return false;
	}

	/**
	 * update and add all the intervals from the given range. @return true iff
	 * clash occurs
	 */
	private boolean addIntervals(Collection<DepInterval> c) {
		for (DepInterval d : c) {
			if (addUpdatedInterval(new DepInterval(d))) {
				return true;
			}
		}
		return false;
	}

	protected DataTypeAppearance(Reference<DepSet> dep) {
		clashDep = dep;
	}

	/** clear the appearance flags */
	protected void clear() {
		pType = null;
		pdatatype = null;
		nType = null;
		constraints.clear();
		constraints.add(new DepInterval());
		accDep.clear();
	}

	// presence interface
	/** check if type is present positively in the node */
	protected boolean hasPType() {
		return pType != null;
	}

	/** check if type is present negatively in the node */
	private boolean hasNType() {
		return nType != null;
	}

	/** set the precense of the PType */
	protected void setPType(final DepDTE type) {
		if (!hasPType()) {
			pType = type;
			// XXX this would be better with a visitor
			if (pType.first instanceof DataEntry) {
				pdatatype = ((DataEntry) pType.first).getDatatype();
			}
			if (pType.first instanceof DataTypeName) {
				pdatatype = ((DataTypeName) pType.first).getDatatype();
			}
		}
	}

	public Datatypes getPDatatype() {
		return pdatatype;
	}

	/** add restrictions [POS]INT to intervals */
	protected boolean addInterval(boolean pos, final DataInterval Int, final DepSet dep) {
		logger.print(Templates.INTERVAL, (pos ? "+" : "-"), Int);
		return pos ? addPosInterval(Int, dep) : addNegInterval(Int, dep);
	}

	/** @return true iff PType and NType leads to clash */
	protected boolean checkPNTypeClash() {
		if (hasNType()) {
			return reportClash(DepSetFactory.plus(pType.second, nType.second), "TNT");
		}
		return false;
	}

	private boolean addPosInterval(final DataInterval Int, final DepSet dep) {
		if (Int.hasMin()) {
			List<DepInterval> aux = new ArrayList<DepInterval>(constraints);
			constraints.clear();
			setLocal(true, Int.minExcl, Int.min, dep);
			if (addIntervals(aux)) {
				return true;
			}
			aux.clear();
		}
		if (Int.hasMax()) {
			List<DepInterval> aux = new ArrayList<DepInterval>(constraints);
			constraints.clear();
			setLocal(false, Int.maxExcl, Int.max, dep);
			if (addIntervals(aux)) {
				return true;
			}
			aux.clear();
		}
		if (constraints.isEmpty()) {
			return reportClash(accDep, "C-MM");
		}
		return false;
	}

	private boolean addNegInterval(final DataInterval Int, final DepSet dep) {
		List<DepInterval> aux = new ArrayList<DepInterval>(constraints);
		constraints.clear();
		if (Int.hasMin()) {
			setLocal(false, !Int.minExcl, Int.min, dep);
			if (addIntervals(aux)) {
				return true;
			}
		}
		if (Int.hasMax()) {
			setLocal(true, !Int.maxExcl, Int.max, dep);
			if (addIntervals(aux)) {
				return true;
			}
		}
		aux.clear();
		if (constraints.isEmpty()) {
			return reportClash(accDep, "C-MM");
		}
		return false;
	}

	protected void setNType(DepDTE t) {
		nType = t;
	}

	protected DepDTE getPType() {
		return pType;
	}
}

/** data interval with dep-sets */
class DepInterval {
	/** interval itself */
	private DataInterval constraints = new DataInterval();
	/** local dep-set */
	private DepSet locDep;

	public DepInterval() {
	}

	public DepInterval(DepInterval d) {
		constraints = new DataInterval(d.constraints);
		locDep = DepSetFactory.create(d.locDep);
	}

	/** update MIN border of an TYPE's interval with VALUE wrt EXCL */
	public boolean update(boolean min, boolean excl, final Literal value, final DepSet dep) {
		if (!constraints.update(min, excl, value)) {
			return false;
		}
		locDep = DepSetFactory.create(dep);
		return true;
	}

	/** check if the interval is consistent wrt given type */
	public boolean consistent(final Literal type, Reference<DepSet> dep) {
		if (constraints.consistent(type)) {
			return true;
		}
		dep.getReference().add(locDep);
		return false;
	}

	public boolean checkMinMaxClash(Reference<DepSet> dep) {
		if (!constraints.closed()) {
			return false;
		}
		final Literal Min = constraints.min;
		final Literal Max = constraints.max;
		if (Min.lesser(Max)) {
			return false;
		}
		if (Max.lesser(Min) || constraints.minExcl || constraints.maxExcl) {
			dep.getReference().add(locDep);
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return constraints.toString();
	}
}