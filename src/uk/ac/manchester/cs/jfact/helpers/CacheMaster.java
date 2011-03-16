package uk.ac.manchester.cs.jfact.helpers;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
public final class CacheMaster {
	//	public static final CacheMaster instance = new CacheMaster();
	//	static final int size = 2500;
	//	static final int limit = 2000;
	//	private final List<BitSet> containsCaches = new ArrayList<BitSet>(size);
	//	private final List<BitSet> cwdContainsCaches = new ArrayList<BitSet>(size);
	//	private final LinkedHashMap<Integer, BitSet> rstContainsCaches = new LinkedHashMap<Integer, BitSet>(
	//			 size, 0.75F, true) {
	//		protected boolean removeEldestEntry(
	//				java.util.Map.Entry<Integer, BitSet> eldest) {
	//			return size() > limit;
	//		}
	//	};
	//	private final Set<Integer> hold = new HashSet<Integer>();
	//	public final void holdOntoThis(CGLabel label) {
	//	hold.add(label.getId());
	//addCache(label);
	//}
	//	public final void stopHolding(CGLabel label) {
	//hold.remove(label.getId());
	//}
	//	public final boolean contains(CGLabel label, int bp) {
	//		BitSet cache = addCache(label);
	//		return cache.get(asPositive(bp));
	//	}
	//
	//	public final BitSet addCache(CGLabel label) {
	//		int key = label.getId();
	//		if (key >= containsCaches.size()) {
	//			Helper.resize(containsCaches, key + 1);
	//		}
	//		if (containsCaches.get(key) == null) {
	//			containsCaches.set(key, new BitSet());
	//			BitSet cache = containsCaches.get(key);
	//			FastSet set = label.get_sc_concepts();
	//			for (int i = 0; i < set.size(); i++) {
	//				cache.set(asPositive(set.get(i)));
	//			}
	//			set = label.get_cc_concepts();
	//			for (int i = 0; i < set.size(); i++) {
	//				cache.set(asPositive(set.get(i)));
	//			}
	//		}
	//		return containsCaches.get(key);
	//	}
	//	public final boolean contains(CWDArray label, int bp) {
	//		int key = label.getId();
	//		if (key >= cwdContainsCaches.size()) {
	//			Helper.resize(cwdContainsCaches, key+1);
	//		}
	//		if (cwdContainsCaches.get(key) == null) {
	//			cwdContainsCaches.set(key, new BitSet());
	//			BitSet cache = cwdContainsCaches.get(label.getId());
	//			FastSet set = label.getContainedConcepts();
	//			for (int i = 0; i < set.size(); i++) {
	//				cache.set(asPositive(set.get(i)));
	//			}
	//		}
	//		return cwdContainsCaches.get(key).get(asPositive(bp));
	//	}
	//
	//	public final void drop(CGLabel label) {
	//		
	//		containsCaches.set(label.getId(), null);
	//	//	hold.remove(label.getId());
	//		//System.out.println("CacheMaster.drop() "+label.hashCode());
	//	}
	//
	//	public final void drop(CWDArray c) {
	//		cwdContainsCaches.set(c.getId(), null);
	//	}
	//	public boolean contains(RAStateTransitions label, int bp) {
	//		BitSet cache = rstContainsCaches.get(label.getId());
	//		if (cache == null) {
	//			cache = new BitSet();
	//			FastSet set = label.getApplicableRoles();
	//			for (int i = 0; i < set.size(); i++) {
	//				cache.set(set.get(i));
	//			}
	//			rstContainsCaches.put(label.getId(), cache);
	//		}
	//		return cache.get(bp);
	//	}
	//
	//	public void drop(RAStateTransitions c) {
	//		rstContainsCaches.remove(c.getId());
	//	}
	static final int asPositive(int p) {
		return p >= 0 ? 2 * p : 1 - 2 * p;
	}
}
