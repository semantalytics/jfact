package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.LL;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import uk.ac.manchester.cs.jfact.helpers.Helper;
import uk.ac.manchester.cs.jfact.helpers.IfDefs;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;

public final class TaxonomyVertex {
	//TODO check if they need to be list
	/** immediate parents and children */
	private final List<TaxonomyVertex> LinksUp = new ArrayList<TaxonomyVertex>();
	private final List<TaxonomyVertex> LinksDown = new ArrayList<TaxonomyVertex>();
	/** entry corresponding to current tax vertex */
	private ClassifiableEntry sample = null;
	//TODO this can be a set, but there is no advantage
	/** synonyms of the sample entry */
	private final Set<ClassifiableEntry> synonyms = new LinkedHashSet<ClassifiableEntry>();
	/** labellers for marking taxonomy */
	//private final static TLabeller valuedLab = new TLabeller();
	// labels for different purposes. all for 2 directions: top-down and bottom-up search
	/** flag if given vertex was checked; connected with checkLab */
	private long theChecked;
	/** flag if given vertex has value; connected with valuedLab */
	private long theValued;
	/** number of common parents of a node */
	private int common;
	/** satisfiability value of a valued vertex */
	private boolean checkValue;

	/** set sample to ENTRY */
	public void setSample(ClassifiableEntry entry) {
		sample = entry;
		entry.setTaxVertex(this);
	}

	/** indirect RW access to Links */
	public List<TaxonomyVertex> neigh(boolean upDirection) {
		return upDirection ? LinksUp : LinksDown;
	}

	// checked part
	public boolean isChecked(long checkLab) {
		return checkLab == theChecked;
	}

	public void setChecked(long checkLab) {
		theChecked = checkLab;
	}

	// value part
	public boolean isValued(long valueLab) {
		return valueLab == theValued;
	}

	public boolean getValue() {
		return checkValue;
	}

	public boolean setValued(boolean val, long valueLab) {
		theValued = valueLab;
		checkValue = val;
		return val;
	}

	// common part
	public boolean isCommon() {
		return common != 0;
	}

	public void setCommon() {
		++common;
	}

	public void clearCommon() {
		common = 0;
	}

	/** keep COMMON flag iff both flags are set; @return true if it is the case */
	public boolean correctCommon(int n) {
		if (common == n) {
			return true;
		}
		common = 0;
		return false;
	}

	/** put initial values on the flags */
	private void initFlags() {
		theChecked = 0;
		theValued = 0;
		common = 0;
	}

	// get info about taxonomy structure
	public Set<ClassifiableEntry> begin_syn() {
		return synonyms;
	}

	public TaxonomyVertex() {
		initFlags();
	}

	/** init c'tor; use it only for Top/Bot initialisations */
	public TaxonomyVertex(final ClassifiableEntry p) {
		initFlags();
		setSample(p);
	}

	/** add P as a synonym to curent vertex */
	public void addSynonym(final ClassifiableEntry p) {
		synonyms.add(p);
		//p.setTaxVertex(this);
		p.setTaxVertex(this);
	}

	/** clears the vertex */
	public void clear() {
		LinksUp.clear();
		LinksDown.clear();
		sample = null;
		initFlags();
	}

	public ClassifiableEntry getPrimer() {
		return sample;
	}

	/** add link in given direction to vertex */
	public void addNeighbour(boolean upDirection, TaxonomyVertex p) {
		if (p == null) {
			System.out.println("TaxonomyVertex.addNeighbour() passed in a null");
		}
		neigh(upDirection).add(p);
	}

	/** check if vertex has no neighbours in given direction */
	public boolean noNeighbours(boolean upDirection) {
		return neigh(upDirection).isEmpty();
	}

	/**
	 * @return v if node represents a synonym (v=Up[i]==Down[j]); @return null
	 *         otherwise
	 */
	public TaxonomyVertex isSynonymNode() {
		// try to find Vertex such that Vertex\in Up and Vertex\in Down
		for (TaxonomyVertex q : neigh(true)) {
			for (TaxonomyVertex r : neigh(false)) {
				if (q.equals(r)) {
					return q;
				}
			}
		}
		return null;
	}

	/** remove latest link (usually to the BOTTOM node) */
	public void removeLastLink(boolean upDirection) {
		Helper.resize(neigh(upDirection), neigh(upDirection).size() - 1);
	}

	/** clear all links in a given direction */
	public void clearLinks(boolean upDirection) {
		neigh(upDirection).clear();
	}

	public void print(LogAdapter o) {
		printSynonyms(o);
		printNeighbours(o, true);
		printNeighbours(o, false);
		o.println();
	}

	public boolean removeLink(boolean upDirection, TaxonomyVertex p) {
		List<TaxonomyVertex> begin = neigh(upDirection);
		int index = begin.indexOf(p);
		if (index > -1) {
			begin.set(index, begin.get(begin.size() - 1));
			removeLastLink(upDirection);
			return true;
		}
		return false;
	}

	//TODO does not work with synonyms
	public void incorporate(ClassifiableEntry entry) {
		// setup sample
		setSample(entry);
		// setup links
		//TODO doublecheck
		List<TaxonomyVertex> falselist = new ArrayList<TaxonomyVertex>(neigh(false));
		List<TaxonomyVertex> truelist = new ArrayList<TaxonomyVertex>(neigh(true));
		for (TaxonomyVertex d : falselist) {
			for (TaxonomyVertex u : truelist) {
				if (d.removeLink(true, u)) {
					u.removeLink(false, d);
				}
			}
			d.addNeighbour(true, this);
		}
		for (TaxonomyVertex u : truelist) {
			u.addNeighbour(false, this);
		}
		if (IfDefs._USE_LOGGING) {
			LL.print(Templates.INCORPORATE, sample.getName());
			for (int i = 0; i < truelist.size(); i++) {
				if (i > 0) {
					LL.print(",");
				}
				LL.print(truelist.get(i).sample.getName());
			}
			LL.print("} and down = {");
			for (int i = 0; i < falselist.size(); i++) {
				if (i > 0) {
					LL.print(",");
				}
				LL.print(falselist.get(i).sample.getName());
			}
			LL.print("}");
		}
	}

	public void printSynonyms(LogAdapter o) {
		assert sample != null;
		if (synonyms.isEmpty()) {
			o.print(String.format("\"%s\"", sample.getName()));
		} else {
			o.print("(\"");
			o.print(sample.getName());
			for (ClassifiableEntry q : begin_syn()) {
				o.print("\"=\"");
				o.print(q.getName());
			}
			o.print("\")");
		}
	}

	public void printNeighbours(LogAdapter o, boolean upDirection) {
		o.print(String.format(" {%s:", neigh(upDirection).size()));
		for (TaxonomyVertex p : neigh(upDirection)) {
			o.print(String.format(" \"%s\"", p.sample.getName()));
		}
		o.print("}");
	}

	@Override
	public String toString() {
		LogAdapter l = new LeveLogger.LogAdapterStringBuilder();
		print(l);
		return l.toString();
	}
}