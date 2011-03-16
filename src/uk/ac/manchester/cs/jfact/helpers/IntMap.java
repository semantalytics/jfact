package uk.ac.manchester.cs.jfact.helpers;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.ArrayList;
import java.util.List;

public final class IntMap<V> {
	class Entry implements Comparable<Entry> {
		int index;
		V value;

		@Override
		public int hashCode() {
			return index;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (this == obj) {
				return true;
			}
			if (obj instanceof IntMap.Entry) {
				return index == ((IntMap.Entry) obj).index;
			}
			return false;
		}

		public int compareTo(Entry arg0) {
			return this.index - arg0.index;
		}

		@Override
		public String toString() {
			return "{" + index + " " + value + "}";
		}
	}

	private List<Entry> values = new ArrayList<Entry>();

	public void clear() {
		values.clear();
		size = 0;
		//cache.resetContained();
	}

	//private final IntCache cache = new IntCache();
	public boolean containsKey(int key) {
		//		if (cache.isContained(key)) {
		//			return true;
		//		}
		//		if (cache.isNotContained(key)) {
		//			return false;
		//		}
		boolean toReturn = insertionIndex(key) > -1;
		//		if (toReturn) {
		//			cache.hit(key);
		//		} else {
		//			cache.miss(key);
		//		}
		return toReturn;
	}

	private int insertionIndex(int key) {
		if (size == 0) {
			return -1;
		}
		if (key < values.get(0).index) {
			return -1;
		}
		if (key > values.get(size - 1).index) {
			return -size() - 1;
		}
		int lowerbound = 0;
		if (size < AbstractFastSet.limit) {
			for (; lowerbound < size; lowerbound++) {
				int v = values.get(lowerbound).index;
				if (v == key) {
					return lowerbound;
				}
				if (v > key) {
					return -lowerbound - 1;
				}
			}
			return -lowerbound - 1;
		}
		int upperbound = size - 1;
		while (lowerbound <= upperbound) {
			int intermediate = lowerbound + (upperbound - lowerbound) / 2;
			int v = values.get(intermediate).index;
			if (v == key) {
				return intermediate;
			}
			if (v < key) {
				lowerbound = intermediate + 1;
			} else {
				upperbound = intermediate - 1;
			}
		}
		return -lowerbound - 1;
	}

	public boolean containsValue(V value) {
		for (int i = 0; i < size; i++) {
			if (values.get(i).value.equals(value)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsAll(IntMap<V> c) {
		if (c.size == 0) {
			return true;
		}
		if (size == 0) {
			return false;
		}
		if (c.size > size) {
			return false;
		}
		if (values.get(0).index > c.values.get(0).index || values.get(size - 1).index < c.values.get(c.size - 1).index) {
			// c boundaries are outside this set
			return false;
		}
		int i = 0;
		int j = 0;
		int currentValue;
		while (j < c.size) {
			currentValue = c.values.get(j).index;
			boolean found = false;
			while (i < size) {
				if (values.get(i).index == currentValue) {
					// found the current value, next element in c - increase j
					found = true;
					break;
				}
				if (values.get(i).index > currentValue) {
					// found a value larger than the value it's looking for - c is not contained
					return false;
				}
				// get(i) is < than current value: check next i
				i++;
			}
			if (!found) {
				// finished exploring this and currentValue was not found - it happens if currentValue < any element in this set
				return false;
			}
			j++;
		}
		return true;
	}

	public List<Entry> entrySet() {
		return values;
	}

	public int index(int key) {
		//		if (cache.isNotContained(key)) {
		//			return -1;
		//		}
		return insertionIndex(key);
	}

	public V get(int key) {
		//		if (cache.isNotContained(key)) {
		//			return null;
		//		}
		int index = insertionIndex(key);
		if (index < 0) {
			return null;
		}
		return values.get(index).value;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int[] keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	int size = 0;

	public void put(int key, V value) {
		int index = insertionIndex(key);
		if (index > -1) {
			values.get(index).value = value;
			return;
		}
		//cache.add(key);
		index = -index - 1;
		Entry e = new Entry();
		e.index = key;
		e.value = value;
		values.add(index, e);
		size++;
	}

	public V remove(int key) {
		int index = insertionIndex(key);
		if (index > -1) {
			//cache.delete(key);
			size--;
			return values.remove(index).value;
		}
		return null;
	}

	public int size() {
		return size;
	}

	@Override
	public int hashCode() {
		return values.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof IntMap) {
			return values.equals(((IntMap<?>) obj).values);
		}
		return false;
	}

	@Override
	public String toString() {
		return values.toString();
	}
}
