package uk.ac.manchester.cs.jfact.kernel.modelcaches;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.LL;
import static uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheState.*;
import static uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheType.mctIan;

import java.util.List;

import uk.ac.manchester.cs.jfact.helpers.DLVertex;
import uk.ac.manchester.cs.jfact.helpers.FastSet;
import uk.ac.manchester.cs.jfact.helpers.FastSetFactory;
import uk.ac.manchester.cs.jfact.helpers.IfDefs;
import uk.ac.manchester.cs.jfact.helpers.UnreachableSituationException;
import uk.ac.manchester.cs.jfact.kernel.ClassifiableEntry;
import uk.ac.manchester.cs.jfact.kernel.ConceptWDep;
import uk.ac.manchester.cs.jfact.kernel.DLDag;
import uk.ac.manchester.cs.jfact.kernel.DlCompletionTree;
import uk.ac.manchester.cs.jfact.kernel.DlCompletionTreeArc;
import uk.ac.manchester.cs.jfact.kernel.RAStateTransitions;
import uk.ac.manchester.cs.jfact.kernel.RATransition;
import uk.ac.manchester.cs.jfact.kernel.TRole;


public class ModelCacheIan extends ModelCacheInterface {
	// sets for the cache
	/** named concepts that appears positively det-lly in a root node of a cache */
	public final FastSet posDConcepts = FastSetFactory.create();
	/** named concepts that appears positively non-det in a root node of a cache */
	public final FastSet posNConcepts = FastSetFactory.create();
	/** named concepts that appears negatively det-lly in a root node of a cache */
	public final FastSet negDConcepts = FastSetFactory.create();
	/** named concepts that appears negatively non-det in a root node of a cache */
	public final FastSet negNConcepts = FastSetFactory.create();
	/** extra det-lly concepts that are (partial) Simple Rule applications */
	public final FastSet extraDConcepts = FastSetFactory.create();
	/** extra non-det concepts that are (partial) Simple Rule applications */
	public final FastSet extraNConcepts = FastSetFactory.create();
	/** role names that are labels of the outgoing edges from the root node */
	public final FastSet existsRoles = FastSetFactory.create();
	/** role names that appears in the \A restrictions in the root node */
	public final FastSet forallRoles = FastSetFactory.create();
	/** role names that appears in the atmost restrictions in the root node */
	public final FastSet funcRoles = FastSetFactory.create();
	/** current state of cache model; recalculates on every change */
	public ModelCacheState curState;

	/** process CT label in given interval; set Deterministic accordingly */
	private void processLabelInterval(final DLDag DLHeap,
			List<ConceptWDep> start) {
		for (int i = 0; i < start.size(); i++) {
			ConceptWDep p = start.get(i);
			int bp = p.getConcept();
			processConcept(DLHeap.get(bp), bp > 0, p.getDep().isEmpty());
		}
	}

	/** fills cache sets by tree.Label; set Deterministic accordingly */
	private void initCacheByLabel(final DLDag DLHeap, final DlCompletionTree pCT) {
		processLabelInterval(DLHeap, pCT.beginl_sc());
		processLabelInterval(DLHeap, pCT.beginl_cc());
	}

	/** Create cache model of given CompletionTree using given HEAP */
	public ModelCacheIan(final DLDag heap, final DlCompletionTree p,
			boolean flagNominals, int nC, int nR) {
		super(flagNominals);
		initCacheByLabel(heap, p);
		initRolesFromArcs(p);
	}

	/** empty c'tor */
	public ModelCacheIan(boolean flagNominals, int nC, int nR) {
		super(flagNominals);
		curState = csValid;
	}

	@Override
	public ModelCacheState getState() {
		return curState;
	}

	FastSet getDConcepts(boolean pos) {
		return pos ? posDConcepts : negDConcepts;
	}

	/** get RW access to N-concepts wrt polarity */
	FastSet getNConcepts(boolean pos) {
		return pos ? posNConcepts : negNConcepts;
	}

	/** get RW access to extra concepts wrt deterministic flag */
	FastSet getExtra(boolean det) {
		return det ? extraDConcepts : extraNConcepts;
	}

	/**
	 * init existRoles from arcs; can be used to create pseudo-cache with deps
	 * of CT edges
	 */
	public void initRolesFromArcs(final DlCompletionTree pCT) {
		for (DlCompletionTreeArc q : pCT.getNeighbour()) {
			if (!q.isIBlocked()) {
				addExistsRole(q.getRole());
			}
		}
		curState = csValid;
	}

	/** Get the tag identifying the cache type */
	@Override
	public ModelCacheType getCacheType() {
		return mctIan;
	}

	/** get type of cache (deep or shallow) */
	@Override
	public boolean shallowCache() {
		return existsRoles.isEmpty();
	}

	/** clear the cache */
	public void clear() {
		posDConcepts.clear();
		posNConcepts.clear();
		negDConcepts.clear();
		negNConcepts.clear();
		if (IfDefs.RKG_USE_SIMPLE_RULES) {
			extraDConcepts.clear();
			extraNConcepts.clear();
		}
		existsRoles.clear();
		forallRoles.clear();
		funcRoles.clear();
		curState = csValid;
	}

	public void processConcept(final DLVertex cur, boolean pos, boolean det) {
		switch (cur.Type()) {
			case dtTop:
			case dtDataType:
			case dtDataValue:
			case dtDataExpr:
				throw new UnreachableSituationException();
			case dtNConcept:
			case dtPConcept:
			case dtNSingleton:
			case dtPSingleton:
				int toAdd = ((ClassifiableEntry) cur.getConcept()).index();
				//if (isNegative(bp)) {
				(det ? getDConcepts(pos) : getNConcepts(pos)).add(toAdd);
				//				} else {
				//					(det ? posDConcepts : posNConcepts).add(toAdd);
				//				}
				break;
			case dtIrr: // for \neg \ER.Self: add R to AR-set
			case dtForall: // add AR.C roles to forallRoles
			case dtLE: // for <= n R: add R to forallRoles
				if (pos) // no need to deal with existantionals here: they would be created through edges
				{
					if (cur.getRole().isSimple()) {
						forallRoles.add(cur.getRole().index());
					} else {
						processAutomaton(cur);
					}
				}
				break;
			default: // all other -- nothing to do
				break;
		}
	}

	public void processAutomaton(final DLVertex cur) {
		RAStateTransitions RST = cur.getRole().getAutomaton().getBase()
				.get(cur.getState());
		// for every transition starting from a given state,
		// add the role that is accepted by a transition
		List<RATransition> begin = RST.begin();
		for (int i = 0; i < begin.size(); i++) {
			for (TRole r : begin.get(i).begin()) {
				forallRoles.add(r.index());
			}
		}
	}

	//	public void addExistsRole(final TRole R) {
	//		existsRoles.add(R.index());
	//		if (R.isTopFunc()) {
	//			funcRoles.add(R.index());
	//		}
	//		for (TRole r : R.begin_anc()) {
	//			existsRoles.add(r.index());
	//			if (r.isTopFunc()) {
	//				funcRoles.add(r.index());
	//			}
	//		}
	//	}
	/** adds role to exists- and func-role if necessary */
	void addRoleToCache(TRole R) {
		existsRoles.add(R.index());
		if (R.isTopFunc()) {
			funcRoles.add(R.index());
		}
	}

	/** adds role (and all its super-roles) to exists- and funcRoles */
	void addExistsRole(TRole R) {
		addRoleToCache(R);
		List<TRole> list = R.getAncestor();
		for (int i = 0; i < list.size(); i++) {
			addRoleToCache(list.get(i));
		}
	}

	@Override
	public ModelCacheState canMerge(final ModelCacheInterface p) {
		if (hasNominalClash(p)) {
			return csFailed;
		}
		if (p.getState() != csValid || curState != csValid) {
			return mergeStatus(p.getState(), curState);
		}
		switch (p.getCacheType()) {
			case mctConst:
				return csValid;
			case mctSingleton: {
				int Singleton = ((ModelCacheSingleton) p).getValue();
				return isMergableSingleton(Math.abs(Singleton), Singleton > 0);
			}
			case mctIan:
				return isMergableIan((ModelCacheIan) p);
			default:
				return csUnknown;
		}
	}

	//	private ModelCacheState isMergableSingleton(final ModelCacheSingleton p) {
	//		int Singleton = p.getValue();
	//		assert isValid(Singleton);
	//		if (isPositive(Singleton)) {
	//			if (negDConcepts.contains(Singleton)) {
	//				return csInvalid;
	//			} else if (negNConcepts.contains(Singleton)) {
	//				return csFailed;
	//			}
	//		} else {
	//			Singleton = -Singleton;
	//			if (posDConcepts.contains(Singleton)) {
	//				return csInvalid;
	//			} else if (posNConcepts.contains(Singleton)) {
	//				return csFailed;
	//			}
	//		}
	//		return csValid;
	//	}
	public ModelCacheState isMergableSingleton(int Singleton, boolean pos) {
		assert Singleton != 0;
		// deterministic clash
		if (getDConcepts(!pos).contains(Singleton)) {
			return csInvalid;
		} else if (getNConcepts(!pos).contains(Singleton)) {
			return csFailed;
		}
		return csValid;
	}

	public ModelCacheState isMergableIan(final ModelCacheIan q) {
		if (posDConcepts.intersect(q.negDConcepts)
				|| q.posDConcepts.intersect(negDConcepts)
				|| IfDefs.RKG_USE_SIMPLE_RULES
				&& getExtra(true).intersect(q.getExtra(true))) {
			return csInvalid;
		} else if (existsRoles.intersect(q.forallRoles)
				|| q.existsRoles.intersect(forallRoles)
				|| funcRoles.intersect(q.funcRoles)
				|| posDConcepts.intersect(q.negNConcepts)
				|| posNConcepts.intersect(q.negDConcepts)
				|| posNConcepts.intersect(q.negNConcepts)
				|| q.posDConcepts.intersect(negNConcepts)
				|| q.posNConcepts.intersect(negDConcepts)
				|| q.posNConcepts.intersect(negNConcepts)
				|| IfDefs.RKG_USE_SIMPLE_RULES
				&& (getExtra(true).intersect(q.getExtra(false))
						|| getExtra(false).intersect(q.getExtra(true)) || getExtra(
						false).intersect(q.getExtra(false)))) {
			return csFailed;
		} else {
			return csValid;
		}
	}

	public ModelCacheState merge(final ModelCacheInterface p) {
		assert p != null;
		// check for nominal clash
		if (hasNominalClash(p)) {
			curState = csFailed;
			return curState;
		}
		switch (p.getCacheType()) {
			case mctConst: // adds TOP/BOTTOM
				curState = mergeStatus(curState, p.getState());
				break;
			case mctSingleton: // adds Singleton
			{
				int Singleton = ((ModelCacheSingleton) p).getValue();
				mergeSingleton(Math.abs(Singleton), Singleton > 0);
				break;
			}
			case mctIan:
				mergeIan((ModelCacheIan) p);
				break;
			default:
				throw new UnreachableSituationException();
		}
		updateNominalStatus(p);
		return curState;
	}

	/** actual merge with a singleton cache */
	void mergeSingleton(int Singleton, boolean pos) {
		ModelCacheState newState = isMergableSingleton(Singleton, pos);
		if (newState != csValid) {
			curState = mergeStatus(curState, newState);
		} else {
			getDConcepts(pos).add(Singleton);
		}
	}

	/** actual merge with an Ian's cache */
	void mergeIan(ModelCacheIan p) {
		// setup curState
		curState = isMergableIan(p);
		// merge all sets:
		posDConcepts.addAll(p.posDConcepts);
		posNConcepts.addAll(p.posNConcepts);
		negDConcepts.addAll(p.negDConcepts);
		negNConcepts.addAll(p.negNConcepts);
		if (IfDefs.RKG_USE_SIMPLE_RULES) {
			extraDConcepts.addAll(p.extraDConcepts);
			extraNConcepts.addAll(p.extraNConcepts);
		}
		existsRoles.addAll(p.existsRoles);
		forallRoles.addAll(p.forallRoles);
		funcRoles.addAll(p.funcRoles);
	}

	@Override
	public void logCacheEntry(int level) {
		LL.print("\nIan cache: posDConcepts = ");
		logCacheSet(posDConcepts.toString());
		LL.print(", posNConcepts = ");
		logCacheSet(posNConcepts.toString());
		LL.print(", negDConcepts = ");
		logCacheSet(negDConcepts.toString());
		LL.print(", negNConcepts = ");
		logCacheSet(negNConcepts.toString());
		LL.print(", existsRoles = ");
		logCacheSet(existsRoles.toString());
		LL.print(", forallRoles = ");
		logCacheSet(forallRoles.toString());
		LL.print(", funcRoles = ");
		logCacheSet(funcRoles.toString());
	}

	private void logCacheSet(String s) {
		LL.print("{");
		LL.print(s);
		LL.print("}");
	}

	//	private <T> boolean sets_intersect(final Collection<T> s1,
	//			final Collection<T> s2) {
	//		Collection<T> it1 = s1;
	//		Collection<T> it2 = s2;
	//		if (s2.size() < s1.size()) {
	//			it1 = s2;
	//			it2 = s1;
	//		}
	//		for (T t : it1) {
	//			if (it2.contains(t)) {
	//				return true;
	//			}
	//		}
	//		return false;
	//		//		Set<T> s = new HashSet<T>(s1);
	//		//		s.retainAll(s2);
	//		//		return s.size() > 0;
	//	}
	private ModelCacheState mergeStatus(ModelCacheState s1, ModelCacheState s2) {
		// if one of caches is definitely UNSAT, then merge will be the same
		if (s1 == csInvalid || s2 == csInvalid) {
			return csInvalid;
		}
		// if one of caches is unsure then result will be the same
		if (s1 == csFailed || s2 == csFailed) {
			return csFailed;
		}
		// if one of caches is not inited, than result would be the same
		if (s1 == csUnknown || s2 == csUnknown) {
			return csUnknown;
		} else {
			// valid+valid = valid
			return csValid;
		}
	}
}