package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.manchester.cs.jfact.helpers.Helper;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;

public final class RoleAutomaton {
	/** all transitions of the automaton, groupped by a starting state */
	private final List<RAStateTransitions> Base = new ArrayList<RAStateTransitions>();
	/** first state of the (temporary) chain of the automata; set by initChain() */
	//private final int chainBeg;
	/**
	 * final state of the (temporary) chain of the automata; set by addToChain()
	 */
	//private final int chainEnd;
	/** maps original automata state into the new ones (used in copyRA) */
	private int[] map = new int[0];
	/** initial state of the next automaton in chain */
	private int iRA;
	/** flag whether automaton is input safe */
	private boolean ISafe;
	/** flag whether automaton is output safe */
	private boolean OSafe;
	/** flag for the automaton to be completed */
	//private boolean Complete;

	/** make sure that STATE exists in the automaton (update ton's size) */
	private void ensureState(int state) {
		//if(Base.size()==state+1) {
		//	return;
		//}
		if (state >= Base.size()) {
			Helper.resize(Base, state + 1);
		}
		//		Helper.resize(Base, state + 1);
		for (int i = 0; i < Base.size(); i++) {
			if (Base.get(i) == null) {
				Base.set(i, new RAStateTransitions());
			}
		}
	}

	public RoleAutomaton() {
		iRA = 0;
		ISafe = true;
		OSafe = true;
		//	chainBeg = 0;
		//	chainEnd = 0;
		//Complete = false;
		ensureState(1);
	}

	/** make the beginning of the chain */
	public void initChain(int from) {
		iRA = from;
	}

	/** add an Automaton to the chain with a default final state */
	public boolean addToChain(RoleAutomaton RA, boolean oSafe) {
		return addToChain(RA, oSafe, size() + 1);
	}

	// i/o safety
	/** get the i-safe value */
	public boolean isISafe() {
		return ISafe;
	}

	/** get the o-safe value */
	public boolean isOSafe() {
		return OSafe;
	}

	// add single RA
	/** add RA from simple subrole to given one */
	public void addSimpleRA(RoleAutomaton RA) {
		boolean ok = Base.get(initial).addToExisting(RA.Base.get(initial).begin().get(0));
		assert ok;
	}

	/** add RA from a subrole to given one */
	public void addRA(RoleAutomaton RA) {
		initChain(initial);
		addToChain(RA, /*oSafe=*/false, final_state);
	}

	/**
	 * add TRANSition leading from a given STATE; check whether all states are
	 * correct
	 */
	public void addTransitionSafe(int state, RATransition trans) {
		ensureState(state);
		ensureState(trans.final_state());
		addTransition(state, trans);
	}

	/** state that the automaton is i-unsafe */
	public void setIUnsafe() {
		ISafe = false;
	}

	/** state that the automaton is o-unsafe */
	public void setOUnsafe() {
		OSafe = false;
	}

	/** check whether transition between FROM and TO breaks safety */
	public void checkTransition(int from, int to) {
		if (from == final_state) {
			setOUnsafe();
		}
		if (to == initial) {
			setIUnsafe();
		}
	}

	/**
	 * add TRANSition leading from a state FROM; all states are known to fit the
	 * ton
	 */
	public void addTransition(int from, RATransition trans) {
		checkTransition(from, trans.final_state());
		Base.get(from).add(trans);
	}

	/** make the internal chain transition (between chainState and TO) */
	public void nextChainTransition(int to) {
		addTransition(iRA, new RATransition(to));
		iRA = to;
	}

	/** get the initial state */
	public static final int initial = 0;
	/** get the final state */
	public static final int final_state = 1;

	/** create new state */
	public int newState() {
		int ret = Base.size();
		//Base.add(new RAStateTransitions());
		ensureState(ret);
		return ret;
	}

	/** get the 1st (multi-)transition starting in STATE */
	public RAStateTransitions begin(int state) {
		return Base.get(state);
	}

	/** return number of distinct states */
	public int size() {
		return Base.size();
	}

	/** set up all transitions passing number of roles */
	public void setup(int nRoles, boolean data) {
		for (int i = 0; i < Base.size(); ++i) {
			Base.get(i).setup(i, nRoles, data);
		}
	}

	public void Print(LogAdapter o) {
		for (int state = 0; state < Base.size(); ++state) {
			Base.get(state).Print(o);
		}
	}

	public void addCopy(RoleAutomaton RA) {
		for (int i = 0; i < RA.size(); ++i) {
			int from = map[i];
			RAStateTransitions RST = Base.get(from);
			RAStateTransitions RSTOrig = RA.Base.get(i);
			if (RSTOrig.isEmpty()) {
				continue;
			}
			List<RATransition> begin = RSTOrig.begin();
			for (int j = 0; j < begin.size(); j++) {
				RATransition p = begin.get(j);
				int to = p.final_state();
				RATransition trans = new RATransition(map[to]);
				checkTransition(from, trans.final_state());
				trans.add(p);
				// try to merge transitions going to the original final state
				if (to == 1 && RST.addToExisting(trans)) {
					//delete trans;
				} else {
					RST.add(trans);
				}
			}
		}
	}

	/**
	 * init internal map according to RA size, with new initial state from
	 * chainState and final (FRA) states
	 */
	public void initMap(int RASize, int fRA) {
		map = Arrays.copyOf(map, RASize);
		// new state in the automaton
		int newState = size() - 1;
		// fill initial state; it is always known in the automata
		map[0] = iRA;
		// fills the final state; if it is not known -- adjust newState
		if (fRA >= size()) {
			fRA = size(); // make sure we don't create an extra unused state
			++newState;
		}
		map[1] = fRA;
		// check transitions as it may turns out to be a single transition
		checkTransition(iRA, fRA);
		// set new initial state
		iRA = fRA;
		// fills the rest of map
		for (int i = 2; i < RASize; ++i) {
			map[i] = ++newState;
		}
		// reserve enough space for the new automaton
		ensureState(newState);
	}

	/**
	 * add an Automaton to the chain that would start from the iRA; OSAFE shows
	 * the safety of a previous automaton in a chain
	 */
	public boolean addToChain(RoleAutomaton RA, boolean oSafe, int fRA) {
		boolean needFinalTrans = fRA < size() && !RA.isOSafe();
		// we can skip transition if chaining automata are i- and o-safe
		if (!oSafe && !RA.isISafe()) {
			nextChainTransition(newState());
		}
		// check whether we need an output transition
		initMap(RA.size(), needFinalTrans ? size() : fRA);
		addCopy(RA);
		if (needFinalTrans) {
			nextChainTransition(fRA);
		}
		return RA.isOSafe();
	}

	public List<RAStateTransitions> getBase() {
		return Base;
	}
}