package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import static uk.ac.manchester.cs.jfact.helpers.Helper.*;
import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.LL;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.semanticweb.owlapi.model.OWLRuntimeException;

import uk.ac.manchester.cs.jfact.helpers.DLVertex;
import uk.ac.manchester.cs.jfact.helpers.FastSet;
import uk.ac.manchester.cs.jfact.helpers.FastSetFactory;
import uk.ac.manchester.cs.jfact.helpers.Helper;
import uk.ac.manchester.cs.jfact.helpers.IfDefs;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger;
import uk.ac.manchester.cs.jfact.helpers.UnreachableSituationException;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheInterface;

public final  class DLDag {
	/** body of DAG */
	private final List<DLVertex> Heap = new ArrayList<DLVertex>();
	/** all the AND nodes (needs to recompute) */
	private final FastSet listAnds = FastSetFactory.create();
	private final EnumMap<DagTag, DLVTable> indexes = new EnumMap<DagTag, DLVTable>(
			DagTag.class);
	/** cache efficiency -- statistic purposes */
	private int nCacheHits;
	/** size of sort array */
	private int sortArraySize;
	// tunable flags (set by readConfig)
	/**
	 * sort strings: option[0] for SAT/cache tests, option[1] for SUB/classify
	 * tests
	 */
	private String orSortSat;
	private String orSortSub;
	/** sort index (if necessary). Possible values are Size, Depth, Freq */
	private int iSort;
	/** whether or not sorting order is ascending */
	private boolean sortAscend;
	/** prefer non-generating rules in OR orderings */
	private boolean preferNonGen;
	/** flag whether cache should be used */
	private boolean useDLVCache;

	/** check if given string is correct sort ordering representation */
	private boolean isCorrectOption(final String str) {
		if (str == null) {
			return false;
		}
		int n = str.length();
		if (n < 1 || n > 3) {
			return false;
		}
		char Method = str.charAt(0), Order = n >= 2 ? str.charAt(1) : 'a', NGPref = n == 3 ? str
				.charAt(2) : 'p';
		return (Method == 'S' || Method == 'D' || Method == 'F'
				|| Method == 'B' || Method == 'G' || Method == '0')
				&& (Order == 'a' || Order == 'd')
				&& (NGPref == 'p' || NGPref == 'n');
	}

	/** change order of ADD elements wrt statistic */
	private void Recompute() {
		for (int p = 0; p < listAnds.size(); p++) {
			Heap.get(listAnds.get(p)).sortEntry(this);
		}
	}

	/** clear all DFS info from elements of DAG */
	private void clearDFS() {
		for (DLVertex d : Heap) {
			d.clearDFS();
		}
	}

	/** update index corresponding to DLVertex's tag */
	public void updateIndex(DagTag tag, int value) {
		if (!indexes.containsKey(tag)) {
			return;
		}
		indexes.get(tag).addElement(value);
		if (tag == DagTag.dtCollection || tag == DagTag.dtAnd) {
			listAnds.add(value);
		}
	}

	/** add vertex to the end of DAG and calculate it's statistic if necessary */
	public int directAdd(DLVertex v) {
		Heap.add(v);
		// return an index of just added entry
		return Heap.size() - 1;
	}

	/**
	 * add vertex to the end of DAG and calculate it's statistic if necessary;
	 * put it into cache
	 */
	public int directAddAndCache(DLVertex v) {
		int ret = directAdd(v);
		if (useDLVCache) {
			updateIndex(v.Type(), ret);
		}
		return ret;
	}

	/** check if given index points to the last DAG entry */
	public boolean isLast(int p) {
		return Math.abs(p) == Heap.size() - 1;
	}

	// access methods
	/** whether to use cache for nodes */
	public void setExpressionCache(boolean val) {
		useDLVCache = val;
	}

	/** access by index */
	public final DLVertex get(int i) {
		assert isValid(i);
		//assert(Math.abs(i) < Heap.size());
		return Heap.get(i < 0 ? -i : i);//Math.abs(i));
	}

	/** get size of DAG */
	public int size() {
		return Heap.size();
	}

	/** get approximation of the size after query is added */
	public int maxSize() {
		return size() + (size() < 220 ? 10 : size() / 20);
	}

	/** use SUB options to OR ordering */
	public void setSubOrder() {
		setOrderOptions(orSortSub);
	}

	/** use SAT options to OR ordering; */
	public void setSatOrder() {
		setOrderOptions(orSortSat);
	}

	/** get cache for given BiPointer (may return null if no cache defined) */
	public final ModelCacheInterface getCache(int p) {
		return get(p).getCache(p > 0);
	}

	/** set cache for given BiPointer; @return given cache */
	public void setCache(int p, final ModelCacheInterface cache) {
		get(p).setCache(p > 0, cache);
	}

	// sort interface
	/** merge two given DAG entries */
	public void merge(MergableLabel ml, int p) {
		if (p != bpINVALID && p != bpTOP && p != bpBOTTOM) {
			get(p).merge(ml);
		}
	}

	/** check if two BPs are of the same sort */
	public boolean haveSameSort(int p, int q) {
		if (IfDefs.RKG_USE_SORTED_REASONING) {
			assert p > 0 && q > 0; // sanity check
			// everything has the same label as TOP
			if (p == 1 || q == 1) {
				return true;
			}
			// if some concepts were added to DAG => nothing to say
			if (p >= sortArraySize || q >= sortArraySize) {
				return true;
			}
			// check whether two sorts are identical
			return get(p).getSort().equals(get(q).getSort());
		} else {
			return true;
		}
	}

	// output interface
	/** print DAG size and number of cache hits, together with DAG usage */
	public void PrintStat(LogAdapter o) {
		o.print(Templates.PRINT_STAT, Heap.size(), nCacheHits);
		if (IfDefs.RKG_PRINT_DAG_USAGE) {
			PrintDAGUsage(o);
		}
	}

	/** print the whole DAG */
	public void Print(LogAdapter o) {
		o.print("\nDag structure");
		for (int i = 1; i < size(); ++i) {
			o.print("\n");
			o.print(i);
			o.print(" ");
			get(i).Print(o);
		}
		o.print("\n");
	}

	@Override
	public String toString() {
		LogAdapter l = new LeveLogger.LogAdapterStringBuilder();
		Print(l);
		return l.toString();
	}

	// save/load interface; implementation is in SaveLoad.cpp
	public int add(DLVertex v) {
		int ret = useDLVCache ? indexes.get(v.Type()).locate(v) : bpINVALID;
		if (!isValid(ret)) {
			ret = directAddAndCache(v);
			return ret;
		}
		// node was found in cache
		++nCacheHits;
		return ret;
	}

	public DLDag(final IFOptionSet Options) {
		/** hash-table for verteces (and, all, LE) fast search */
		DLVTable indexAnd = null;
		DLVTable indexAll = null;
		DLVTable indexLE = null;
		indexAnd = new DLVTable(this);
		indexAll = new DLVTable(this);
		indexLE = new DLVTable(this);
		indexes.put(DagTag.dtCollection, indexAnd);
		indexes.put(DagTag.dtAnd, indexAnd);
		indexes.put(DagTag.dtIrr, indexAll);
		indexes.put(DagTag.dtUAll, indexAll);
		indexes.put(DagTag.dtForall, indexAll);
		indexes.put(DagTag.dtLE, indexLE);
		nCacheHits = 0;
		useDLVCache = true;
		Heap.add(new DLVertex(DagTag.dtBad));
		Heap.add(new DLVertex(DagTag.dtTop));
		readConfig(Options);
	}

	public void removeAfter(int n) {
		assert n < size();
		while (n < Heap.size()) {
			//TODO check: looks useless
			//			switch (Heap.get(n).Type()) {
			//				case dtDataType:
			//				case dtDataValue:
			//				case dtDataExpr:
			//					((TDataEntry) Heap.get(n).getConcept()).setBP(bpINVALID);
			//					break;
			//				default:
			//					break;
			//			}
			Heap.remove(n);
			//delete p;
		}
		Helper.resize(Heap, n);
	}

	public void readConfig(final IFOptionSet Options) {
		assert Options != null;
		orSortSat = Options.getText("orSortSat");
		orSortSub = Options.getText("orSortSub");
		if (!isCorrectOption(orSortSat) || !isCorrectOption(orSortSub)) {
			throw new OWLRuntimeException("DAG: wrong OR sorting options");
		}
	}

	public void setOrderDefaults(final String defSat, final String defSub) {
		assert isCorrectOption(defSat) && isCorrectOption(defSub);
		LL.print(Templates.SET_ORDER_DEFAULTS1, orSortSat, defSat);
		if (orSortSat.charAt(0) == '0') {
			orSortSat = defSat;
		}
		LL.print(Templates.SET_ORDER_DEFAULTS2, orSortSat, orSortSub, defSub);
		if (orSortSub.charAt(0) == '0') {
			orSortSub = defSub;
		}
		LL.print(Templates.SET_ORDER_DEFAULTS3, orSortSub);
	}

	public void setOrderOptions(final String opt) {
		if (opt.charAt(0) == '0') {
			return;
		}
		sortAscend = opt.charAt(1) == 'a';
		preferNonGen = opt.charAt(2) == 'p';
		iSort = opt.charAt(0) == 'S' ? DLVertex.getStatIndexSize(false) : opt
				.charAt(0) == 'D' ? DLVertex.getStatIndexDepth(false) : opt
				.charAt(0) == 'B' ? DLVertex.getStatIndexBranch(false) : opt
				.charAt(0) == 'G' ? DLVertex.getStatIndexGener(false)
				: DLVertex.getStatIndexFreq(false);
		Recompute();
	}

	private void computeVertexStat(int p) {
		DLVertex v = get(p);
		boolean pos = p > 0;
		// this vertex is already processed
		if (v.isProcessed(pos)) {
			return;
		}
		// in case of cycle: mark concept as such
		if (v.isVisited(pos)) {
			v.setInCycle(pos);
			// FIXME!! now the fact of cycle is not used; later on mark a node in cycle iff any child is/
			return;
		}
		v.setVisited(pos);
		// ensure that the statistic is gather for all sub-concepts of the expression
		switch (v.Type()) {
			case dtCollection: // if pos then behaves like and
				if (!pos) {
					break;
				}
				// fallthrough
				//$FALL-THROUGH$
			case dtAnd: // check all the conjuncts
				for (int q : v.begin()) {
					computeVertexStat(q, pos);
				}
				break;
			case dtProj:
				if (!pos) {
					break;
				}
				// fallthrough
				//$FALL-THROUGH$
			case dtPConcept:
			case dtNConcept:
			case dtPSingleton:
			case dtNSingleton:
			case dtForall:
			case dtUAll:
			case dtLE: // check a single referenced concept
				computeVertexStat(v.getC(), pos);
				break;
			default: // nothing to do
				break;
		}
		v.setProcessed(pos);
		// here all the necessary statistics is gathered -- use it in the init
		updateVertexStat(p);
	}

	private void updateVertexStat(int p) {
		DLVertex v = get(p);
		boolean pos = p > 0;
		int d = 0, s = 1, b = 0, g = 0;
		if (!v.omitStat(pos)) {
			if (isValid(v.getC())) {
				updateVertexStat(v, v.getC(), pos);
			} else {
				for (int q : v.begin()) {
					updateVertexStat(v, q, pos);
				}
			}
		}
		// correct values wrt POS
		d = v.getDepth(pos);
		switch (v.Type()) {
			case dtAnd:
				if (!pos) {
					++b; // OR is branching
				}
				break;
			case dtForall:
				++d; // increase depth
				if (!pos) {
					++g; // SOME is generating
				}
				break;
			case dtLE:
				++d; // increase depth
				if (!pos) {
					++g; // >= is generating
				} else if (v.getNumberLE() != 1) {
					++b; // <= is branching
				}
				break;
			case dtProj:
				if (pos) {
					++b; // projection sometimes involves branching
				}
				break;
			default:
				break;
		}
		v.updateStatValues(d, s, b, g, pos);
	}

	/** gather vertex freq statistics */
	private void computeVertexFreq(int p) {
		DLVertex v = get(p);
		boolean pos = p > 0;
		if (v.isVisited(pos)) {
			return;
		}
		v.incFreqValue(pos); // increment frequence of current vertex
		v.setVisited(pos);
		if (v.omitStat(pos)) {
			return;
		}
		// increment frequence of all subvertex
		if (isValid(v.getC())) {
			computeVertexFreq(v.getC(), pos);
		} else {
			for (int q : v.begin()) {
				computeVertexFreq(q, pos);
			}
		}
	}

	private void computeVertexStat(int p, boolean pos) {
		computeVertexStat(createBiPointer(p, pos));
	}

	/** helper for the recursion */
	private void updateVertexStat(DLVertex v, int p, boolean pos) {
		//		v.getStatsDelegate().updateStatValues(get(p).getStatsDelegate(),
		//				pos == isPositive(p), pos);
		DLVertex w = get(p);
		boolean same = pos && (p > 0);
		//		boolean posW = pos == p>0;
		// update in-cycle information
		if (w.isInCycle(same)) {
			v.setInCycle(pos);
		}
		v.updateStatValues(w, same, pos);
	}

	/** helper for the recursion */
	private void computeVertexFreq(int p, boolean pos) {
		computeVertexFreq(createBiPointer(p, pos));
	}

	public void gatherStatistic() {
		// gather main statistics for disjunctions
		for (int i = 0; i < listAnds.size(); i++) {
			computeVertexStat(-listAnds.get(i));
		}
		// if necessary -- gather frequency
		if (orSortSat.charAt(0) != 'F' && orSortSub.charAt(0) != 'F') {
			return;
		}
		clearDFS();
		for (int i = size() - 1; i > 1; --i) {
			if (get(i).Type().isCNameTag()) {
				computeVertexFreq(i);
			}
		}
	}

	public boolean less(int p1, int p2) {
		if (preferNonGen) {
			if (p1 < 0 && p2 > 0) {
				return true;
			}
			if (p1 > 0 && p2 < 0) {
				return false;
			}
		}
		final DLVertex v1 = get(p1);
		final DLVertex v2 = get(p2);
		int key1 = v1.getStat(iSort);
		int key2 = v2.getStat(iSort);
		if (sortAscend) {
			return key1 < key2;
		} else {
			return key2 < key1;
		}
	}

	public void PrintDAGUsage(LogAdapter o) {
		int n = 0; // number of no-used DAG entries
		int total = Heap.size() * 2 - 2; // number of total DAG entries
		for (DLVertex i : Heap) {
			if (i.getUsage(true) == 0) {
				++n;
			}
			if (i.getUsage(false) == 0) {
				++n;
			}
		}
		o.print(Templates.PRINTDAGUSAGE, n, n * 100 / total, total);
	}

	/** build the sort system for given TBox */
	public void determineSorts(RoleMaster ORM, RoleMaster DRM) {
		sortArraySize = Heap.size();
		// init roles R&D sorts
		List<TRole> ORM_Begin = ORM.getRoles();
		for (TRole p : ORM_Begin) {
			if (!p.isSynonym()) {
				mergeSorts(p);
			}
		}
		List<TRole> DRM_Begin = DRM.getRoles();
		for (TRole p : DRM_Begin) {
			if (!p.isSynonym()) {
				mergeSorts(p);
			}
		}
		for (int i = 2; i < Heap.size(); ++i) {
			mergeSorts(Heap.get(i));
		}
		int sum = 0;
		for (int i = 2; i < Heap.size(); ++i) {
			MergableLabel lab = Heap.get(i).getSort();
			lab.resolve();
			if (lab.isSample()) {
				++sum;
			}
		}
		for (TRole p : ORM_Begin) {
			if (!p.isSynonym()) {
				MergableLabel lab = p.getDomainLabel();
				lab.resolve();
				if (lab.isSample()) {
					++sum;
				}
			}
		}
		for (TRole p : DRM_Begin) {
			if (!p.isSynonym()) {
				MergableLabel lab = p.getDomainLabel();
				lab.resolve();
				if (lab.isSample()) {
					++sum;
				}
			}
		}
		// we added a temp concept here; don't count it
		if (sum > 0) {
			sum--;
		}
		LL.print(Templates.DETERMINE_SORTS, (sum > 0 ? sum : "no"));
	}

	/** merge sorts for a given role */
	void mergeSorts(TRole R) {
		// associate role domain labels
		R.mergeSupersDomain();
		merge(R.getDomainLabel(), R.getBPDomain());
		// also associate functional nodes (if any)
		for (TRole q : R.begin_topfunc()) {
			merge(R.getDomainLabel(), q.getFunctional());
		}
	}

	/** merge sorts for a given vertex */
	void mergeSorts(DLVertex v) {
		switch (v.Type()) {
			case dtLE: // set R&D for role
			case dtForall:
				v.merge(v.getRole().getDomainLabel()); // domain(role)=cur
				merge(v.getRole().getRangeLabel(), v.getC());
				break;
			case dtProj: // projection: equate R&D of R and ProjR, and D(R) with C
				v.merge(v.getRole().getDomainLabel());
				v.merge(v.getProjRole().getDomainLabel());
				merge(v.getRole().getDomainLabel(), v.getC());
				v.getRole().getRangeLabel()
						.merge(v.getProjRole().getRangeLabel());
				break;
			case dtIrr: // equate R&D for role
				v.merge(v.getRole().getDomainLabel());
				v.merge(v.getRole().getRangeLabel());
				break;
			case dtAnd:
			case dtCollection:
				for (int q : v.begin()) {
					merge(v.getSort(), q);
				}
				break;
			case dtNSingleton:
			case dtPSingleton:
			case dtPConcept:
			case dtNConcept: // merge with description
				merge(v.getSort(), v.getC());
				break;
			case dtDataType: // nothing to do
			case dtDataValue:
			case dtDataExpr:
			case dtNN:
				break;
			case dtTop:
			default:
				throw new UnreachableSituationException();
		}
	}

	/** update sorts for <a,b>:R construction */
	void updateSorts(int a, TRole R, int b) {
		merge(R.getDomainLabel(), a);
		merge(R.getRangeLabel(), b);
	}
}

final class DLVTable {
	/** host DAG that contains actual nodes; */
	private final DLDag host;
	/** HT for nodes */
	private final Map<DLVertex, FastSet> Table = new HashMap<DLVertex, FastSet>();

	protected DLVTable(final DLDag dag) {
		host = dag;
	}

	private int locate(FastSet leaf, final DLVertex v) {
		for (int i = 0; i < leaf.size(); i++) {
			int p = leaf.get(i);
			if (v.equals(host.get(p))) {
				return p;
			}
		}
		return bpINVALID;
	}

	protected int locate(final DLVertex v) {
		FastSet p = Table.get(v);
		return p == null ? bpINVALID : locate(p, v);
	}

	protected void addElement(int pos) {
		FastSet leaf = Table.get(host.get(pos));
		if (leaf == null) {
			leaf = FastSetFactory.create();
			Table.put(host.get(pos), leaf);
		}
		leaf.add(pos);
	}

	@Override
	public String toString() {
		return Table.toString() + "\n" + host.toString();
	}
}
