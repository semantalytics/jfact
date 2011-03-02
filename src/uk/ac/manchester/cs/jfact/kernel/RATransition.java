package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedHashSet;

import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;


public final  class RATransition {
	/** set of roles that may affect the transition */
	private final LinkedHashSet<TRole> label;// = new ArrayList<TRole>();
	BitSet cache = null;
	/** final state of the transition */
	private final int state;

	/** create a transition to given state */
	public RATransition(int st) {
		state = st;
		label = new LinkedHashSet<TRole>();
	}

	/** create a transition with a given label R to given state ST */
	public RATransition(int st, TRole R) {
		this(st);
		label.add(R);
	}

	/** add label of transition TRANS to transition's label */
	public void add(RATransition trans) {
		label.addAll(trans.label);
		cache = null;
	}

	// query the transition
	/** get the 1st role in (multi-)transition */
	public Collection<TRole> begin() {
		return label;
	}

	/** give a final point of the transition */
	public int final_state() {
		return state;
	}

	/** check whether transition is applicable wrt role R */
	public boolean applicable(TRole R) {
		//TODO check this: here I use getIndex but in other places index() is used, and it is different; TRole.equals() does not use either.
		if (cache == null) {
			cache = new BitSet();
			for (TRole t : label) {
				cache.set(t.getIndex());
			}
		}
		return cache.get(R.getIndex());
		//		return label.contains(R);
	}

	/** check whether transition is empty */
	public boolean isEmpty() {
		return label.isEmpty();
	}

	/** print the transition starting from FROM */
	public void Print(LogAdapter o, int from) {
		o.print(String.format("\n%s -- ", from));
		if (isEmpty()) {
			o.print("e");
		} else {
			o.print(label.toString());
			//			for (int i = 0; i < label.size(); i++) {
			//				if (i > 0) {
			//					o.print(",");
			//				}
			//				o.print("\"");
			//				o.print(label.get(i).getName());
			//				o.print("\"");
			//			}
		}
		o.print(" -> ");
		o.print(final_state());
	}
}