package uk.ac.manchester.cs.jfact.kernel.actors;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.manchester.cs.jfact.kernel.ClassifiableEntry;
import uk.ac.manchester.cs.jfact.kernel.TExpressionManager;
import uk.ac.manchester.cs.jfact.kernel.TaxonomyVertex;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLConceptExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLDataRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLIndividualExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLObjectRoleExpression;

public class JTaxonomyActor implements Actor {
	final TExpressionManager EM;
	/** 2D array to return */
	final List<List<TDLExpression>> acc = new ArrayList<List<TDLExpression>>();
	/** 1D array to return */
	final List<TDLExpression> plain = new ArrayList<TDLExpression>();
	/** temporary vector to keep synonyms */
	//final List<TDLExpression> syn = new ArrayList<TDLExpression>();
	private final Policy policy;

	/** try current entry */
	public List<TDLExpression> tryEntry(ClassifiableEntry p) {
		List<TDLExpression> toReturn = new ArrayList<TDLExpression>();
		if (p.isSystem()) {
			return toReturn;
		}
		if (policy.applicable(p)) {
			toReturn.add(policy.buildTree(EM, p));
		}
		return toReturn;
	}

	public JTaxonomyActor(TExpressionManager em, Policy policy) {
		EM = em;
		this.policy = policy;
	}

	/**
	 * get single vector of synonyms (necessary for Equivalents, for example)
	 */
	public Collection<TDLConceptExpression> getClassSynonyms() {
		Collection<TDLConceptExpression> toReturn = new ArrayList<TDLConceptExpression>();
		if (!acc.isEmpty()) {
			for (TDLExpression e : acc.get(0)) {
				toReturn.add((TDLConceptExpression) e);
			}
		}
		return toReturn;
	}

	public Collection<TDLIndividualExpression> getIndividualSynonyms() {
		Collection<TDLIndividualExpression> toReturn = new ArrayList<TDLIndividualExpression>();
		if (!acc.isEmpty()) {
			for (TDLExpression e : acc.get(0)) {
				toReturn.add((TDLIndividualExpression) e);
			}
		}
		return toReturn;
	}

	public Collection<TDLObjectRoleExpression> getObjectPropertySynonyms() {
		Collection<TDLObjectRoleExpression> toReturn = new ArrayList<TDLObjectRoleExpression>();
		if (!acc.isEmpty()) {
			for (TDLExpression e : acc.get(0)) {
				toReturn.add((TDLObjectRoleExpression) e);
			}
		}
		return toReturn;
	}

	public Collection<TDLDataRoleExpression> getDataPropertySynonyms() {
		Collection<TDLDataRoleExpression> toReturn = new ArrayList<TDLDataRoleExpression>();
		if (!acc.isEmpty()) {
			for (TDLExpression e : acc.get(0)) {
				toReturn.add((TDLDataRoleExpression) e);
			}
		}
		return toReturn;
	}

	public Collection<TDLIndividualExpression> getPlainIndividualElements() {
		Collection<TDLIndividualExpression> toReturn = new ArrayList<TDLIndividualExpression>(plain.size());
		for (TDLExpression e : plain) {
			toReturn.add((TDLIndividualExpression) e);
		}
		return toReturn;
	}

	public Collection<TDLConceptExpression> getPlainClassElements() {
		Collection<TDLConceptExpression> toReturn = new ArrayList<TDLConceptExpression>(plain.size());
		for (TDLExpression e : plain) {
			toReturn.add((TDLConceptExpression) e);
		}
		return toReturn;
	}

	/** get 2D array of all required elements of the taxonomy */
	public Collection<Collection<TDLConceptExpression>> getClassElements() {
		Collection<Collection<TDLConceptExpression>> toReturn = new ArrayList<Collection<TDLConceptExpression>>();
		for (List<TDLExpression> l : acc) {
			List<TDLConceptExpression> list = new ArrayList<TDLConceptExpression>();
			for (TDLExpression e : l) {
				list.add((TDLConceptExpression) e);
			}
			toReturn.add(list);
		}
		return toReturn;
	}

	/** get 2D array of all required elements of the taxonomy */
	public Collection<Collection<TDLObjectRoleExpression>> getObjectPropertyElements() {
		Collection<Collection<TDLObjectRoleExpression>> toReturn = new ArrayList<Collection<TDLObjectRoleExpression>>();
		for (List<TDLExpression> l : acc) {
			List<TDLObjectRoleExpression> list = new ArrayList<TDLObjectRoleExpression>();
			for (TDLExpression e : l) {
				list.add((TDLObjectRoleExpression) e);
			}
			toReturn.add(list);
		}
		return toReturn;
	}

	public Collection<Collection<TDLDataRoleExpression>> getDataPropertyElements() {
		Collection<Collection<TDLDataRoleExpression>> toReturn = new ArrayList<Collection<TDLDataRoleExpression>>();
		for (List<TDLExpression> l : acc) {
			List<TDLDataRoleExpression> list = new ArrayList<TDLDataRoleExpression>();
			for (TDLExpression e : l) {
				list.add((TDLDataRoleExpression) e);
			}
			toReturn.add(list);
		}
		return toReturn;
	}

	/** get 2D array of all required elements of the taxonomy */
	public Collection<Collection<TDLIndividualExpression>> getIndividualElements() {
		Collection<Collection<TDLIndividualExpression>> toReturn = new ArrayList<Collection<TDLIndividualExpression>>();
		for (List<TDLExpression> l : acc) {
			List<TDLIndividualExpression> list = new ArrayList<TDLIndividualExpression>();
			for (TDLExpression e : l) {
				list.add((TDLIndividualExpression) e);
			}
			toReturn.add(list);
		}
		return toReturn;
	}

	/** taxonomy walking method. */
	/**
	 * @return true if node was processed, and there is no need to go further,
	 *         false if node can not be processed in current settings
	 */
	public boolean apply(TaxonomyVertex v) {
		List<TDLExpression> syn = tryEntry(v.getPrimer());
		for (ClassifiableEntry p : v.begin_syn()) {
			syn.addAll(tryEntry(p));
		}
		/** no applicable elements were found */
		if (syn.isEmpty()) {
			return false;
		}
		if (policy.needPlain()) {
			plain.addAll(syn);
		} else {
			acc.add(syn);
		}
		return true;
	}
}