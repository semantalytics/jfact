package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import uk.ac.manchester.cs.jfact.dep.DepSet;
import uk.ac.manchester.cs.jfact.helpers.ArrayIntMap;
import uk.ac.manchester.cs.jfact.helpers.FastSetSimple;
import uk.ac.manchester.cs.jfact.helpers.Helper;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapterStringBuilder;

public final class CWDArray {
	/** array of concepts together with dep-sets */
	private final List<ConceptWDep> base = new ArrayList<ConceptWDep>();
	private BitSet cache;
	private final ArrayIntMap indexes = new ArrayIntMap();
	private boolean createCache = false;
	private final static int cacheLimit = 1;

	/** init/clear label */
	public void init() {
		base.clear();
		cache = null;
		indexes.clear();
		createCache = false;
	}

	public List<ConceptWDep> getBase() {
		return base;
	}

	public ArrayIntMap getContainedConcepts() {
		return indexes;
	}

	/** adds concept P to a label - to be called only from CGLabel */
	protected void private_add(final ConceptWDep p) {
		base.add(p);
		if (cache != null) {
			cache.set(asPositive(p.getConcept()));
		}
		indexes.put(p.getConcept(), base.size() - 1);
		createCache = base.size() > cacheLimit;
	}

	/** check whether label contains BP (ignoring dep-set) */
	public boolean contains(int bp) {
		if (cache == null && createCache) {
			initCache();
		}
		if (cache != null) {
			return cache.get(asPositive(bp));
		} else {
			//	System.out.println("CWDArray.contains() "+indexes.size());
			return indexes.containsKey(bp);
		}
	}

	private void initCache() {
		cache = new BitSet();
		for (int i = 0; i < indexes.size(); i++) {
			cache.set(asPositive(indexes.keySet(i)));
		}
	}

	static final int asPositive(int p) {
		return p >= 0 ? 2 * p : 1 - 2 * p;
	}

	public int index(int bp) {
		// check that the index actually exist: quicker
		if (cache != null && !cache.get(asPositive(bp))) {
			return -1;
		}
		return indexes.get(bp);
	}

	public DepSet get(int bp) {
		// check that the index actually exist: quicker
		if (cache != null && !cache.get(asPositive(bp))) {
			return null;
		}
		int i = indexes.get(bp);
		if (i < 0) {
			return null;
		}
		return base.get(i).getDep();
	}

	public int size() {
		return base.size();
	}

	public boolean lesserequal(final CWDArray label) {
		// checks the keys are in both maps
		return label.indexes.containsAll(indexes);
	}

	@Override
	public int hashCode() {
		return indexes.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof CWDArray) {
			CWDArray obj2 = (CWDArray) obj;
			return this.indexes.equals(obj2.indexes);
		}
		return false;
	}

	/** save label using given SS */
	public int save() {
		return base.size();
	}

	public Restorer updateDepSet(int index, DepSet dep) {
		if (dep.isEmpty()) {
			throw new IllegalArgumentException();
		}
		Restorer ret = new UnMerge(this, base.get(index), index);
		base.get(index).addDep(dep);
		return ret;
	}

	public List<Restorer> updateDepSet(DepSet dep) {
		if (dep.isEmpty()) {
			throw new IllegalArgumentException();
		}
		List<Restorer> toReturn = new ArrayList<Restorer>(base.size());
		for (int i = 0; i < base.size(); i++) {
			Restorer ret = new UnMerge(this, base.get(i), i);
			base.get(i).addDep(dep);
			toReturn.add(ret);
		}
		return toReturn;
	}

	public void restore(int ss, int level) {
		for (int i = ss; i < base.size(); i++) {
			int concept = base.get(i).getConcept();
			indexes.remove(concept);
			if (cache != null) {
				cache.clear(asPositive(concept));
			}
		}
		Helper.resize(base, ss);
	}

	public void print(LeveLogger.LogAdapter o) {
		o.print(" [");
		for (int i = 0; i < base.size(); i++) {
			if (i != 0) {
				o.print(", ");
			}
			base.get(i).print(o);
		}
		o.print("]");
	}

	@Override
	public String toString() {
		LogAdapterStringBuilder b = new LogAdapterStringBuilder();
		print(b);
		return b.toString();
	}
}

final class UnMerge extends Restorer {
	private final CWDArray label;
	private final int offset;
	private final FastSetSimple dep;

	UnMerge(CWDArray lab, ConceptWDep p, int offset) {
		label = lab;
		this.offset = offset;
		dep = p.getDep().getDelegate();
	}

	@Override
	public void restore() {
		label.getBase().set(offset, new ConceptWDep(label.getBase().get(offset).getConcept(), new DepSet(dep)));
	}
}