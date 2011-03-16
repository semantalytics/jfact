package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.manchester.cs.jfact.helpers.DLTree;

public class TIndividual extends TConcept {
	/** pointer to nominal node (works for singletons only) */
	private DlCompletionTree node;
	/** index for axioms <this,C>:R */
	private final List<TRelated> RelatedIndex = new ArrayList<TRelated>();
	/** map for the related individuals: Map[R]={i:R(this,i)} */
	private Map<TRole, List<TIndividual>> pRelatedMap;
	// precompletion support
	/**
	 * vector that contains LINKS to the concept expressions that are labels of
	 * an individual
	 */
	private final Set<DLTree> CSSet = new HashSet<DLTree>();
	/** new concept expression to be added to the label */
	//private DLTree PCConcept;
	// TODO not sure what's the use
	private List<DLTree> copy = new ArrayList<DLTree>();

	public TIndividual(final String name) {
		super(name);
		node = null;
		pRelatedMap = new HashMap<TRole, List<TIndividual>>();
		//	PCConcept = null;
	}

	/** check whether a concept is indeed a singleton */
	@Override
	public boolean isSingleton() {
		return true;
	}

	/** init told subsumers of the individual by it's description */
	@Override
	public void initToldSubsumers() {
		toldSubsumers.clear();
		clearHasSP();
		if (isRelated()) {
			updateToldFromRelated();
		}
		// normalise description if the only parent is TOP
		if (isPrimitive() && Description != null && Description.isTOP()) {
			removeDescription();
		}
		// not a completely defined if there are extra rules or related individuals
		boolean CD = !hasExtraRules() && isPrimitive() && !isRelated();
		if (Description != null || hasToldSubsumers()) {
			CD &= super.initToldSubsumers(Description, new HashSet<TRole>());
		}
		setCompletelyDefined(CD);
	}

	// related things
	/** update told subsumers from the RELATED axioms in a given range */
	private <T extends TRelated> void updateTold(List<T> begin, Set<TRole> RolesProcessed) {
		for (int i = 0; i < begin.size(); i++) {
			SearchTSbyRoleAndSupers(begin.get(i).getRole(), RolesProcessed);
		}
	}

	/** check if individual connected to something with RELATED statement */
	private boolean isRelated() {
		return !RelatedIndex.isEmpty();
	}

	/** set individual related */
	public void addRelated(TRelated p) {
		RelatedIndex.add(p);
	}

	/** add all the related elements from the given P */
	public void addRelated(TIndividual p) {
		RelatedIndex.addAll(p.RelatedIndex);
	}

	// related map access
	/** @return true if has cache for related individuals via role R */
	public boolean hasRelatedCache(final TRole R) {
		return pRelatedMap.containsKey(R);
	}

	/** get set of individuals related to THIS via R */
	public List<TIndividual> getRelatedCache(final TRole R) {
		assert pRelatedMap.containsKey(R);
		return pRelatedMap.get(R);
	}

	/** set the cache of individuals related to THIS via R */
	public void setRelatedCache(final TRole R, final List<TIndividual> v) {
		assert !pRelatedMap.containsKey(R);
		pRelatedMap.put(R, v);
	}

	// precompletion interface
	/** check whether EXPR already exists in the precompletion set */
	private boolean containsPCExpr(final DLTree expr) {
		if (expr == null) {
			return true;
		}
		return CSSet.contains(expr);
		//		for (DLTree p : CSSet) {
		//			if (equalTrees(p, expr)) {
		//				return true;
		//			}
		//		}
		//		return false;
	}

	/** unconditionally adds EXPR to the precompletion information */
	private void addPCExprAlways(final DLTree expr) {
		CSSet.add(expr);
		copy.add(expr.copy());
		//		PCConcept = DLTreeFactory.createSNFAnd(PCConcept,
		//				expr.copy());
	}

	/**
	 * add EXPR to th ePC information if it is a new expression; @return true if
	 * was added
	 */
	public boolean addPCExpr(final DLTree expr) {
		if (containsPCExpr(expr)) {
			return false;
		}
		addPCExprAlways(expr);
		return true;
	}

	/** update individual's description from precompletion information */
	public void usePCInfo() {
		//deleteTree(Description);
		//		Description = PCConcept;
		removeDescription();
		addLeaves(copy);//Desc(PCConcept);
		copy.clear();
		//PCConcept = null;
		// we change description of a concept, so we need to rebuild the TS info
		// note that precompletion succeed; so there is no need to take into account
		// RELATED information
		super.initToldSubsumers();
	}

	/** remove all precompletion-related information */
	public void clearPCInfo() {
		//deleteTree(PCConcept);
		//PCConcept = null;
		copy.clear();
		CSSet.clear();
	}

	// TIndividual RELATED-dependent method' implementation
	private void updateToldFromRelated() {
		Set<TRole> RolesProcessed = new HashSet<TRole>();
		updateTold(RelatedIndex, RolesProcessed);
	}

	protected DlCompletionTree getNode() {
		return node;
	}

	protected void setNode(DlCompletionTree node) {
		this.node = node;
	}

	protected List<TRelated> getRelatedIndex() {
		return RelatedIndex;
	}
}
//class TRelatedMap extends HashMap<TRole, List<TIndividual>> {
//	public TRelatedMap() {
//	}
//
//	/** check whether role is in map */
//	public boolean hasRole(final TRole R) {
//		return containsKey(R);
//	}
//
//	/** get array related to role */
//	public List<TIndividual> getRelated(final TRole R) {
//		assert hasRole(R);
//		return get(R);
//	}
//
//	/** add related wrt role */
//	public void setRelated(final TRole R, final List<TIndividual> v) {
//		assert !hasRole(R);
//		put(R, v);
//	}
//}