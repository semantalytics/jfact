package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;

/** class to represent transitions from a single state in an automaton */
public final class RAStateTransitions {
	/** all transitions */
	protected final List<RATransition> Base = new ArrayList<RATransition>();
	/** check whether there is an empty transition going from this state */
	protected boolean EmptyTransition;
	/** set of all roles that can be applied by one of the transitions */
	//private final FastSet ApplicableRoles = new IntSet();
	private final BitSet ApplicableRoles = new BitSet();
	/** state from which all the transition starts */
	private int from;
	/** flag whether the role is data or not (valid only for simple automata) */
	private boolean DataRole;
	private int size = 0;

	/** RW begin */
	public List<RATransition> begin() {
		return Base;
	}

	public RAStateTransitions() {
		EmptyTransition = false;
	}

	/** add a transition from a given state */
	public void add(RATransition trans) {
		Base.add(trans);
		size++;
		if (trans.isEmpty()) {
			EmptyTransition = true;
		}
		//		CacheMaster.instance.drop(this);
	}

	/** @return true iff there are no transitions from this state */
	public boolean isEmpty() {
		return size == 0;
	}

	/** @return true iff there is an empty transition from the state */
	public boolean hasEmptyTransition() {
		return EmptyTransition;
	}

	/** print all the transitions starting from the state FROM */
	public void Print(LogAdapter o) {
		for (int i = 0; i < size; i++) {
			Base.get(i).Print(o, from);
		}
	}

	/** set up state transitions: no more additions to the structure */
	public void setup(int state, int nRoles, boolean data) {
		from = state;
		DataRole = data;
		//ApplicableRoles.ensureMaxSetSize(nRoles);
		// fills the set of recognisable roles
		for (int i = 0; i < size; i++) {
			for (TRole t : Base.get(i).begin()) {
				ApplicableRoles.set(t.getIndex());
			}
			//ApplicableRoles.addAll(Base.get(i).begin());
		}
	}

	/**
	 * add information from TRANS to existing transition between the same
	 * states. @return false if no such transition found
	 */
	public boolean addToExisting(RATransition trans) {
		int to = trans.final_state();
		boolean tEmpty = trans.isEmpty();
		for (int i = 0; i < size; i++) {
			RATransition p = Base.get(i);
			//TODO index in Base
			if (p.final_state() == to && p.isEmpty() == tEmpty) { // found existing transition
				p.add(trans);
				return true;
			}
		}
		// no transition from->to found
		return false;
	}

	public boolean recognise(TRole R) {
		if (R == null) {
			return false;
		}
		return R.isDataRole() == DataRole && ApplicableRoles.get(R.getIndex());
		//				&& CacheMaster.instance.contains(this, R.getIndex());// ApplicableRoles.contains(R.getIndex());
	}

	/** @return true iff there is only one transition */
	public boolean isSingleton() {
		return size == 1;
	}

	/** @return final state of the 1st transition; used for singletons */
	public int getTransitionEnd() {
		return Base.get(0).final_state();
	}
}