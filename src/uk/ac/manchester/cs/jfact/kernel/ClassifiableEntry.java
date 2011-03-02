package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClassifiableEntry extends TNamedEntry {
	/** link to taxonomy entry for current entry */
	protected TaxonomyVertex taxVertex;
	/**
	 * links to 'told subsumers' (entries that are direct super-entries for
	 * current)
	 */
	protected final LinkedHashSet<ClassifiableEntry> toldSubsumers = new LinkedHashSet<ClassifiableEntry>();
	/**
	 * pointer to synonym (entry which contains whole information the same as
	 * current)
	 */
	protected ClassifiableEntry pSynonym;
	/** index as a vertex in the SubsumptionMap */
	protected int Index;

	protected ClassifiableEntry( String name) {
		super(name);
		taxVertex = null;
		pSynonym = null;
		Index = 0;
	}

	/** is current entry classified */
	protected final boolean isClassified() {
		return taxVertex != null;
	}

	/** set up given entry */
	protected final void setTaxVertex(TaxonomyVertex vertex) {
		taxVertex = vertex;
	}

	/** get taxonomy vertex of the entry */
	protected final TaxonomyVertex getTaxVertex() {
		return taxVertex;
	}

	// completely defined interface
	/** a Completely Defined flag */
	protected final boolean isCompletelyDefined() {
		return bits.get(Flags.CompletelyDefined.ordinal());
	}

	public final void clearCompletelyDefined() {
		bits.clear(Flags.CompletelyDefined.ordinal());
	}

	protected final void setCompletelyDefined(boolean action) {
		if (action) {
			bits.set(Flags.CompletelyDefined.ordinal(), action);
		} else {
			bits.clear(Flags.CompletelyDefined.ordinal());
		}
	}

	/** a non-classifiable flag */
	protected final boolean isNonClassifiable() {
		return bits.get(Flags.NonClassifiable.ordinal());
	}

	protected final void setNonClassifiable() {
		bits.set(Flags.NonClassifiable.ordinal());
	}

	public final void clearNonClassifiable() {
		bits.clear(Flags.NonClassifiable.ordinal());
	}

	public final void setNonClassifiable(boolean action) {
		if (action) {
			bits.set(Flags.NonClassifiable.ordinal(), action);
		} else {
			bits.clear(Flags.NonClassifiable.ordinal());
		}
	}

	/** told subsumers */
	protected final Collection<ClassifiableEntry> getToldSubsumers() {
		return toldSubsumers;
	}

	/** check whether entry ihas any TS */
	protected final boolean hasToldSubsumers() {
		return !toldSubsumers.isEmpty();
	}

	/** add told subsumer of entry (duplications possible) */
	protected final void addParent(ClassifiableEntry parent) {
		toldSubsumers.add(parent);
	}

	/** add all parents (with duplicates) from the range to current node */
	protected final void addParents(Collection<ClassifiableEntry> entries) {
		for (ClassifiableEntry c : entries) {
			addParentIfNew(c);
		}
	}

	// index interface
	/** get the index value */
	public final int index() {
		return Index;
	}

	/** set the index value */
	public void setIndex(int ind) {
		Index = ind;
	}

	// synonym interface
	/** check if current entry is a synonym */
	public final boolean isSynonym() {
		return pSynonym != null;
	}

	/** get synonym of current entry */
	protected  final ClassifiableEntry getSynonym() {
		return pSynonym;
	}

	/** make sure that synonym's representative is not a synonym itself */
	protected  final void canonicaliseSynonym() {
		if (isSynonym()) {
			while (pSynonym.isSynonym()) {
				pSynonym = pSynonym.pSynonym;
			}
		}
	}

	/** add entry's synonym */
	protected final void setSynonym(ClassifiableEntry syn) {
		assert pSynonym == null; // do it only once
		// check there are no cycles
		Set<ClassifiableEntry> set = new HashSet<ClassifiableEntry>();
		set.add(this);
		ClassifiableEntry runner = syn;
		while (runner.isSynonym() && !set.contains(runner.pSynonym)) {
			set.add(runner.pSynonym);
			runner = runner.pSynonym;
		}
		if (set.contains(runner.pSynonym)) {
			// then adding this synonym would cause a loop
			System.out
					.println("ClassifiableEntry.setSynonym(): warning: assigning this synonym would create a loop; ignored\nignored synonym: "
							+ this
							+ " -> "
							+ syn
							+ "\nPrevious synonyms: "
							+ set);
		} else {
			pSynonym = syn;
			canonicaliseSynonym();
		}
	}

	/** if two synonyms are in 'told' list, merge them */
	protected final void removeSynonymsFromParents() {
		List<ClassifiableEntry> toRemove = new ArrayList<ClassifiableEntry>();
		for (ClassifiableEntry c : toldSubsumers) {
			if (this == resolveSynonym(c)) {
				toRemove.add(c);
			}
		}
		toldSubsumers.removeAll(toRemove);
	}

	public final static <T extends ClassifiableEntry> T resolveSynonym(T p) {
		return p == null ? null
				: p.isSynonym() ? resolveSynonym((T) p.pSynonym) : p;
	}

	protected final void addParentIfNew(ClassifiableEntry parent) {
		// resolve synonyms
		parent = resolveSynonym(parent);
		// node can not be its own parent
		if (parent == this) {
			return;
		}
		toldSubsumers.add(parent);
	}
}