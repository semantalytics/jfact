package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.Helper.bpTOP;
import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.LL;
import uk.ac.manchester.cs.jfact.helpers.DLVertex;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;

public final class LogicFeatures {
	/** all flags in one long */
	private long flags;

	/** set any flag */
	private void setX(LFEnum val) {
		flags |= val.getValue();
	}

	/** get value of any flag */
	private boolean getX(LFEnum val) {
		return (flags & val.getValue()) > 0;
	}

	/** default c'tor */
	public LogicFeatures() {
		flags = 0;
	}

	/** copy c'tor */
	public LogicFeatures(final LogicFeatures lf) {
		flags = lf.flags;
	}

	/** operator add */
	public void binaryOrOperator(final LogicFeatures lf) {
		flags |= lf.flags;
	}

	public boolean hasInverseRole() {
		return getX(LFEnum.lfBothRoles);
	}

	private boolean hasRoleHierarchy() {
		return getX(LFEnum.lfRolesSubsumption);
	}

	private boolean hasTransitiveRole() {
		return getX(LFEnum.lfTransitiveRoles);
	}

	boolean hasSomeAll() {
		return getX(LFEnum.lfSomeConstructor);
	}

	boolean hasFunctionalRestriction() {
		return getX(LFEnum.lfFConstructor) || getX(LFEnum.lfFunctionalRoles);
	}

	boolean hasNumberRestriction() {
		return getX(LFEnum.lfNConstructor);
	}

	boolean hasQNumberRestriction() {
		return getX(LFEnum.lfQConstructor);
	}

	boolean hasSingletons() {
		return getX(LFEnum.lfSingleton);
	}

	boolean hasSelfRef() {
		return getX(LFEnum.lfSelfRef);
	}

	// overall state
	/** check whether no flags are set */
	boolean isEmpty() {
		return flags == 0;
	}

	/** get all the flags at once */
	long getAllFlags() {
		return flags;
	}

	/** set all flags to a given value; @return old value of the flags */
	long setAllFlags(int value) {
		long old = flags;
		flags = value;
		return old;
	}

	/** build bothRoles from single Roles flags */
	void mergeRoles() {
		if (getX(LFEnum.lfDirectRoles) && getX(LFEnum.lfInverseRoles)) {
			setX(LFEnum.lfBothRoles);
		}
	}

	/** allow user to set presence of inverse roles */
	void setInverseRoles() {
		setX(LFEnum.lfBothRoles);
	}

	public static LogicFeatures plus(final LogicFeatures f1, final LogicFeatures f2) {
		LogicFeatures f = new LogicFeatures(f1);
		f.flags |= f2.flags;
		return f;
	}

	void fillConceptData(final TConcept p) {
		if (p.isSingleton()) {
			setX(LFEnum.lfSingleton);
		}
	}

	void fillRoleData(final TRole p, boolean both) {
		if (p.getId() > 0) {
			setX(LFEnum.lfDirectRoles);
		} else {
			setX(LFEnum.lfInverseRoles);
		}
		if (both) {
			setX(LFEnum.lfBothRoles);
		}
		if (p.isTransitive()) {
			setX(LFEnum.lfTransitiveRoles);
		}
		if (p.hasToldSubsumers()) {
			setX(LFEnum.lfRolesSubsumption);
		}
		if (p.isFunctional()) {
			setX(LFEnum.lfFunctionalRoles);
		}
		if (p.getBPDomain() != bpTOP || p.getBPRange() != bpTOP) {
			setX(LFEnum.lfRangeAndDomain);
		}
	}

	void fillDAGData(final DLVertex v, boolean pos) {
		switch (v.Type()) {
			case dtForall:
				setX(LFEnum.lfSomeConstructor);
				break;
			case dtLE:
				setX(LFEnum.lfNConstructor);
				if (v.getC() != bpTOP) {
					setX(LFEnum.lfQConstructor);
				}
				break;
			case dtPSingleton:
			case dtNSingleton:
				setX(LFEnum.lfSingleton);
				break;
			case dtIrr:
				setX(LFEnum.lfSelfRef);
				break;
			default:
				break;
		}
	}

	void writeState() {
		String NO = "NO ";
		String Q = "qualified ";
		LL.print(Templates.WRITE_STATE, (hasInverseRole() ? "" : NO), (hasRoleHierarchy() ? "" : NO), (hasTransitiveRole() ? "" : NO), (hasSomeAll() ? "" : NO), (hasFunctionalRestriction() ? "" : NO), (hasNumberRestriction() ? (hasQNumberRestriction() ? Q : "") : NO),
				(hasSingletons() ? "" : NO));
	}

	enum LFEnum {
		lfInvalid(0),
		// role description
		lfTransitiveRoles(1 << 0), lfRolesSubsumption(1 << 1), lfDirectRoles(1 << 2), lfInverseRoles(1 << 3), lfRangeAndDomain(1 << 4), lfFunctionalRoles(1 << 5),
		// concept description
		lfSomeConstructor(1 << 6), lfFConstructor(1 << 7), lfNConstructor(1 << 8), lfQConstructor(1 << 9), lfSingleton(1 << 10),
		// global description
		lfGeneralAxioms(1 << 11), lfBothRoles(1 << 12),
		// new constructions
		lfSelfRef(1 << 13);
		private final int value;

		LFEnum(int v) {
			value = v;
		}

		protected int getValue() {
			return value;
		}
	}
}