package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import static uk.ac.manchester.cs.jfact.helpers.Helper.*;

import java.util.List;

import uk.ac.manchester.cs.jfact.helpers.ArrayIntMap;
import uk.ac.manchester.cs.jfact.helpers.FastSet;
import uk.ac.manchester.cs.jfact.helpers.FastSetFactory;
import uk.ac.manchester.cs.jfact.helpers.IntMap;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.kernel.state.SaveState;


public final class CGLabel {
	private static int idcounter = 0;

	private static int getnewId() {
		return idcounter++;
	}

	/** all simple concepts, labelled a node */
	private final CWDArray scLabel;
	/** all complex concepts (ie, FORALL, GE), labelled a node */
	private final CWDArray ccLabel;

	static final class CGLabelRelationCache {
		IntMap<FastSet> lesserequal = new IntMap<FastSet>();
		IntMap<FastSet> reverse = new IntMap<FastSet>();

		void removeAllReferences(CGLabel l) {
			lesserequal.remove(l.id);
			if (lesserequal.size() == 0) {
				reverse.clear();
			} else {
				FastSet list = reverse.remove(l.id);
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						FastSet temp = lesserequal.get(list.get(i));
						if (temp != null) {
							temp.remove(l.id);
							if (temp.size() == 0) {
								lesserequal.remove(list.get(i));
							}
						}
					}
				}
			}
		}

		void addLesserEqual(CGLabel l1, CGLabel l2) {
			add(lesserequal, l1, l2);
			add(reverse, l2, l1);
		}

		private void add(IntMap<FastSet> m, CGLabel l1, CGLabel l2) {
			FastSet list = m.get(l1.id);
			if (list == null) {
				list = FastSetFactory.create();
				m.put(l1.id, list);
			}
			list.add(l2.id);
		}

		private static boolean contains(IntMap<FastSet> m, CGLabel l1,
				CGLabel l2) {
			FastSet l = m.get(l1.id);
			if (l != null) {
				return l.contains(l2.id);
			}
			return false;
		}

		boolean containsLesserEqual(CGLabel l1, CGLabel l2) {
			return contains(lesserequal, l1, l2);
		}
	}

	private static final CGLabelRelationCache cache = new CGLabelRelationCache();
	private static final CGLabelRelationCache misscache = new CGLabelRelationCache();

	public void cleanCache() {
		if (cacheNeedsCleaning) {
			cleanCacheUnconditionally();
		}
	}

	//	void cleanCacheMaster() {
	//		if (cached) {
	//			cached = false;
	//			CacheMaster.instance.drop(this);
	//			missedCalls = 0;
	//		}
	//	}
	public void cleanCacheUnconditionally() {
		cache.removeAllReferences(this);
		misscache.removeAllReferences(this);
		cacheNeedsCleaning = false;
	}

	private boolean cacheNeedsCleaning = false;
	final int id;

	//	private IntCache looseCache = new HashIntCache();
	//private IntSet forContains=new IntSet();
	public CGLabel() {
		scLabel = new CWDArray();
		ccLabel = new CWDArray();
		id = getnewId();
	}

	protected List<ConceptWDep> get_sc() {
		return scLabel.getBase();
	}

	protected List<ConceptWDep> get_cc() {
		return ccLabel.getBase();
	}

	public ArrayIntMap get_sc_concepts() {
		return scLabel.getContainedConcepts();
	}

	public ArrayIntMap get_cc_concepts() {
		return ccLabel.getContainedConcepts();
	}

	/** get (RW) label associated with the concepts defined by TAG */
	protected CWDArray getLabel(DagTag tag) {
		return tag.isComplexConcept() ? ccLabel : scLabel;
	}

	public void add(DagTag tag, ConceptWDep p) {
		getLabel(tag).private_add(p);
		cacheNeedsCleaning = true;
		//	cleanCacheMaster();
	}

	

	/** check whether node is labelled by complex concept P */
	public boolean containsCC(int p) {
		boolean b = ccLabel.contains(p);
		return b;
	}

	@Override
	public int hashCode() {
		return id;//.hashCode();// scLabel.hashCode() + ccLabel.hashCode();
	}

	//	static int hits = 0;
	//	static int miss = 0;
	protected boolean lesserequal(final CGLabel label) {
		cleanCache();
		if (this == label) {
			return true;
		}
		if (cache.containsLesserEqual(this, label)) {
			//		hits++;
			return true;
		}
		if (misscache.containsLesserEqual(this, label)) {
			//			hits++;
			return false;
		}
		//		miss++;
		//		if(hits%10000==0||miss%10000==0) {
		//			System.out.println("CGLabel.lesserequal() "+hits+"\t"+miss);
		//		}
		boolean toReturn = scLabel.lesserequal(label.scLabel)
				&& ccLabel.lesserequal(label.ccLabel);
		if (toReturn) {
			cache.addLesserEqual(this, label);
		} else {
			misscache.addLesserEqual(this, label);
		}
		return toReturn;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof CGLabel) {
			CGLabel obj2 = (CGLabel) obj;
			boolean toReturn = scLabel.equals(obj2.scLabel)
					&& ccLabel.equals(obj2.ccLabel);
			return toReturn;
		}
		return false;
	}

	/** save label using given SS */
	public void save(SaveState ss) {
		ss.setSc(scLabel.save());
		ss.setCc(ccLabel.save());
	}

	/** restore label to given LEVEL using given SS */
	public void restore(final SaveState ss, int level) {
		scLabel.restore(ss.getSc(), level);
		ccLabel.restore(ss.getCc(), level);
		cacheNeedsCleaning = true;
		//cleanCacheMaster();
	}

	/** print the whole label */
	protected void print(LeveLogger.LogAdapter o) {
		scLabel.print(o);
		ccLabel.print(o);
	}

	@Override
	public String toString() {
		LogAdapter l = new LeveLogger.LogAdapterStringBuilder();
		print(l);
		return l.toString();
	}

	protected void init() {
		cleanCacheUnconditionally();
		//	cleanCacheMaster();
		// init label with reasonable size
		scLabel.init(); // FIXME!! correct size later on
		ccLabel.init(); // FIXME!! correct size later on
	}

	//	int missedCalls = 0;
	//	boolean cached = false;
	protected boolean contains(int p) {
		assert isCorrect(p);
		if (p == bpTOP) {
			return true;
		}
		if (p == bpBOTTOM) {
			return false;
		}
		//		missedCalls++;
		//		if (missedCalls > 10) {
		//			//			scLabel.missedCalls=0;
		//			//			ccLabel.missedCalls=0;
		//			//			CacheMaster.instance.drop(scLabel);
		//			//			CacheMaster.instance.drop(ccLabel);
		//			cached = true;
		//			return CacheMaster.instance.contains(this, p);
		//		}
		boolean b = scLabel.contains(p) || ccLabel.contains(p);
		return b;
	}

	//	int cachemisses = 0;
	public int baseSize() {
		return ccLabel.size() + scLabel.size();
	}

	public Integer getId() {
		return id;
	}
}
