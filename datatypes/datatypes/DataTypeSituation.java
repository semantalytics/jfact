package datatypes;

/* This file is part of the JFact DL reasoner
 Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
 This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.manchester.cs.jfact.dep.DepSet;
import uk.ac.manchester.cs.jfact.helpers.FastSetSimple;

public final class DataTypeSituation<R extends Comparable<R>> {
	/** positive type appearance */
	private DepSet pType;
	/** negative type appearance */
	private DepSet nType;
	/** interval of possible values */
	private Set<DepInterval<R>> constraints = new HashSet<DepInterval<R>>();
	/** accumulated dep-set */
	private DepSet accDep = DepSet.create();
	/** dep-set for the clash */
	private final DataTypeReasoner reasoner;
	private final Datatype<R> type;
	private List<Literal<?>> literals = new ArrayList<Literal<?>>();

	protected DataTypeSituation(Datatype<R> p, DataTypeReasoner dep) {
		if (p == null) {
			throw new IllegalArgumentException("p cannot be null");
		}
		this.type = p;
		reasoner = dep;
		constraints.add(new DepInterval<R>());
	}

	/**
	 * update and add a single interval I to the constraints.
	 *
	 * @return true iff clash occurs
	 */
	private boolean addUpdatedInterval(DepInterval<R> i, Datatype<R> interval,
			DepSet localDep) {
		if (!i.consistent(interval)) {
			localDep.add(i.locDep);
			reasoner.reportClash(localDep, "C-IT");
			return true;
		}
		if (!i.update(interval, localDep)) {
			constraints.add(i);
		}
		if (!hasPType()) {
			constraints.add(i);
		}
		if (!i.checkMinMaxClash()) {
			constraints.add(i);
		} else {
			accDep.add(i.locDep);
		}
		return false;
	}

	public Datatype<?> getType() {
		return type;
	}

	/**
	 * add restrictions [POS]INT to intervals
	 *
	 * @return true if clash occurs
	 */
	public boolean addInterval(boolean pos, Datatype<R> interval, DepSet dep) {
		if (interval instanceof DatatypeEnumeration) {
			literals.addAll(interval.listValues());
		}
		Datatype<R> realInterval = pos ? interval : new DatatypeNegation<R>(interval);
		Set<DepInterval<R>> c = constraints;
		constraints = new HashSet<DepInterval<R>>();
		if (!c.isEmpty()) {
			for (DepInterval<R> d : c) {
				if (addUpdatedInterval(d, realInterval, DepSet.create(dep))) {
					return true;
				}
			}
		}
		if (constraints.isEmpty()) {
			reasoner.reportClash(accDep, "C-MM");
			return true;
		}
		return false;
	}

	/** @return true iff PType and NType leads to clash */
	public boolean checkPNTypeClash() {
		if (hasNType() && hasPType()) {
			reasoner.reportClash(DepSet.plus(pType, nType), "TNT");
			return true;
		}
		for (DepInterval<R> d : constraints) {
			final boolean checkMinMaxClash = d.checkMinMaxClash();
			if (checkMinMaxClash) {
				accDep.add(d.locDep);
				reasoner.reportClash(accDep, "C-MM");
			}
			return checkMinMaxClash;
		}
		return false;
	}

	private boolean emptyConstraints() {
		return constraints.isEmpty() || constraints.iterator().next().e == null;
	}

	public boolean checkCompatibleValue(DataTypeSituation<?> other) {
		if (!type.isCompatible(other.type)) {
			System.out.println("DataTypeSituation.checkCompatibleValue() " + type + "\t"
					+ other.type + type.isCompatible(other.type));
			return false;
		}
		if (this.emptyConstraints() || other.emptyConstraints()) {
			return true;
		}
		List<Literal<?>> allLiterals = new ArrayList<Literal<?>>(literals);
		allLiterals.addAll(other.literals);
		List<Datatype<?>> allRestrictions = new ArrayList<Datatype<?>>();
		for (DepInterval<?> d : other.constraints) {
			if (d.e != null) {
				allRestrictions.add(d.e);
			}
		}
		for (DepInterval<?> d : constraints) {
			if (d.e != null) {
				allRestrictions.add(d.e);
			}
		}
		for (Literal<?> l : allLiterals) {
			if (!type.isCompatible(l) || !other.type.isCompatible(l)) {
				return false;
			}
			for (Datatype<?> d : allRestrictions) {
				if (!d.isCompatible(l)) {
					return false;
				}
			}
		}
		return true;
	}

	/** data interval with dep-sets */
	static final class DepInterval<R extends Comparable<R>> {
		DatatypeExpression<R> e;
		/** local dep-set */
		FastSetSimple locDep;

		@Override
		public String toString() {
			return "depInterval{" + e + "}";
		}

		/** update MIN border of an TYPE's interval with VALUE wrt EXCL */
		public boolean update(Datatype<R> value, final DepSet dep) {
			if (e == null) {
				if (value.isExpression()) {
					e = value.asExpression();
				} else {
					e = DatatypeFactory.getDatatypeExpression(value);
				}
				locDep = dep == null ? null : dep.getDelegate();
				return false;
			} else {
				// TODO compare value spaces
				if (e instanceof DatatypeEnumeration || e instanceof DatatypeNegation) {
					// cannot update an enumeration
					return false;
				}
				for (Map.Entry<Facet, Object> f : value.getKnownFacetValues().entrySet()) {
					e = e.addFacet(f.getKey(), f.getValue());
				}
			}
			//TODO needs to return false if the new expression has the same value space as the old one
			locDep = dep == null ? null : dep.getDelegate();
			return true;
		}

		/** check if the interval is consistent wrt given type */
		public boolean consistent(Datatype<R> type) {
			return e == null || e.isCompatible(type);
		}

		public boolean checkMinMaxClash() {
			if (e == null) {
				return false;
			}
			return e.emptyValueSpace();
		}

		@Override
		public boolean equals(Object obj) {
			if (super.equals(obj)) {
				return true;
			}
			if (obj instanceof DepInterval) {
				return (e == null ? ((DepInterval) obj).e == null : e
						.equals(((DepInterval) obj).e)) && locDep == null ? ((DepInterval) obj).locDep == null
						: locDep.equals(((DepInterval) obj).locDep);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (e == null ? 0 : e.hashCode())
					+ (locDep == null ? 0 : locDep.hashCode());
		}
	}

	// presence interface
	/** check if type is present positively in the node */
	public boolean hasPType() {
		return pType != null;
	}

	/** check if type is present negatively in the node */
	public boolean hasNType() {
		return nType != null;
	}

	/** set the precense of the PType */
	public void setPType(final DepSet type) {
		pType = type;
	}

	public void setNType(DepSet t) {
		nType = t;
	}

	public DepSet getPType() {
		return pType;
	}

	public DepSet getNType() {
		return nType;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + constraints;
	}
}
