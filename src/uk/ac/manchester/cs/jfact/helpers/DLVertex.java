package uk.ac.manchester.cs.jfact.helpers;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.Helper.*;
import static uk.ac.manchester.cs.jfact.kernel.DagTag.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;
import uk.ac.manchester.cs.jfact.kernel.DLDag;
import uk.ac.manchester.cs.jfact.kernel.DagTag;
import uk.ac.manchester.cs.jfact.kernel.MergableLabel;
import uk.ac.manchester.cs.jfact.kernel.TNamedEntry;
import uk.ac.manchester.cs.jfact.kernel.TRole;
import uk.ac.manchester.cs.jfact.kernel.datatype.TDataEntry;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheInterface;

public final class DLVertex extends DLVertexTagDFS {
	static class ChildSet {
		private Comparator<Integer> c = new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				if (o1.equals(o2)) {
					return 0;
				}
				if (sorter.less(o1, o2)) {
					return -1;
				}
				return 1;
			}
		};
		private final FastSet set = FastSetFactory.create();
		private final SortedIntList original = new SortedIntList();
		int[] sorted = null;
		protected DLDag sorter = null;

		@Override
		public boolean equals(Object arg0) {
			if (arg0 == null) {
				return false;
			}
			if (this == arg0) {
				return true;
			}
			if (arg0 instanceof ChildSet) {
				ChildSet arg = (ChildSet) arg0;
				return set.equals(arg.set);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return set.hashCode();
		}

		public void setSorter(DLDag d) {
			sorter = d;
			sorted = null;
		}

		public int[] sorted() {
			if (sorted == null) {
				sorted = new int[set.size()];
				if (sorter == null) {
					for (int i = 0; i < set.size(); i++) {
						// if the re is no sorting, use the original insertion order
						sorted[i] = original.get(i);
					}
				} else {
					List<Integer> l = new ArrayList<Integer>();
					for (int i = 0; i < set.size(); ++i) {
						l.add(set.get(i));
					}
					Collections.sort(l, c);
					for (int i = 0; i < sorted.length; ++i) {
						sorted[i] = l.get(i);
					}
				}
			}
			return sorted;
		}

		public boolean contains(int inverse) {
			return set.contains(inverse);
		}

		public void clear() {
			set.clear();
			sorted = null;
		}

		public boolean add(int p) {
			int size = set.size();
			set.add(p);
			if (set.size() > size) {
				original.add(p);
				sorted = null;
				return true;
			}
			return false;
		}
	}

	/** set of arguments (CEs, numbers for NR) */
	private final ChildSet Child = new ChildSet();
	/** pointer to concept-like entry (for PConcept, etc) */
	private TNamedEntry Concept = null;
	/** pointer to role (for E\A, NR) */
	private final TRole Role;
	/** projection role (used for projection op only) */
	private final TRole ProjRole;
	/** C if available */
	private int C;
	/** n if available */
	private final int n;
	/** maximal depth, size and frequency of reference of the expression */
	private final MergableLabel Sort = new MergableLabel();

	/** get RW access to the label */
	public MergableLabel getSort() {
		return Sort;
	}

	/** merge local label to label LABEL */
	public void merge(MergableLabel label) {
		Sort.merge(label);
	}

	/** c'tor for Top/CN/And (before adding any operands) */
	public DLVertex(DagTag op) {
		this(op, 0, null, bpINVALID, null);
	}

	/** c'tor for <= n R_C; and for \A R{n}_C; Note order C, n, R.pointer */
	public DLVertex(DagTag op, int m, final TRole R, int c, TRole ProjR) {
		super(op);
		Role = R;
		ProjRole = ProjR;
		C = c;
		n = m;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof DLVertex) {
			DLVertex v = (DLVertex) obj;
			return Op == v.Op && compare(Role, v.Role) && compare(ProjRole, v.ProjRole) && C == v.C && n == v.n && Child.equals(v.Child);
		}
		return false;
	}

	private boolean compare(Object o1, Object o2) {
		if (o1 == null) {
			return o2 == null;
		}
		return o1.equals(o2);
	}

	@Override
	public int hashCode() {
		return (Op == null ? 0 : Op.hashCode()) + (Role == null ? 0 : Role.hashCode()) + (ProjRole == null ? 0 : ProjRole.hashCode()) + C + n + (Child == null ? 0 : Child.hashCode());
	}

	/** return C for concepts/quantifiers/NR verteces */
	public int getC() {
		return C;
	}

	/** return N for the (<= n R) vertex */
	public int getNumberLE() {
		return n;
	}

	/** return N for the (>= n R) vertex */
	public int getNumberGE() {
		return n + 1;
	}

	/** return STATE for the (\all R{state}.C) vertex */
	public int getState() {
		return n;
	}

	/** return pointer to the first concept name of the entry */
	public int[] begin() {
		return Child.sorted();
	}

	/** return pointer to Role for the Role-like verteces */
	public TRole getRole() {
		return Role;
	}

	/** return pointer to Projection Role for the Projection verteces */
	public TRole getProjRole() {
		return ProjRole;
	}

	/** get (RW) TConcept for concept-like fields */
	public TNamedEntry getConcept() {
		return Concept;
	}

	/** set TConcept value to entry */
	public void setConcept(TNamedEntry p) {
		Concept = p;
	}

	/** set a concept (child) to Name-like vertex */
	public void setChild(int p) {
		C = p;
	}

	public boolean addChild(int p) {
		if (p == bpTOP) {
			return false;
		}
		if (Op == dtBad) {
			return true;
		}
		if (p == bpBOTTOM) {
			//clash:
			Child.clear();
			Op = dtBad;
			return true;
		}
		if (Child.contains(-p)) {
			Child.clear();
			Op = dtBad;
			return true;
		}
		Child.add(p);
		return false;
	}

	public int getAndToDagValue() {
		if (Child.set.size() == 0) {
			return bpTOP;
		}
		if (Child.set.size() == 1) {
			return Child.set.get(0);
		}
		return bpINVALID;
	}

	public void sortEntry(final DLDag dag) {
		if (Op != dtAnd) {
			return;
		}
		Child.setSorter(dag);
	}

	public void Print(LeveLogger.LogAdapter o) {
		o.print(Templates.DLVERTEXPrint, stat[0], stat[1], stat[2], stat[3], stat[4], stat[5], stat[6], stat[7], stat[8], stat[9], Op.getName());
		switch (Op) {
			case dtAnd:
			case dtCollection:
				break;
			case dtTop:
			case dtUAll:
			case dtNN:
				return;
			case dtDataExpr:
				o.print(Templates.SPACE, ((TDataEntry) Concept).getFacet());
				return;
			case dtDataValue:
			case dtDataType:
			case dtPConcept:
			case dtNConcept:
			case dtPSingleton:
			case dtNSingleton:
				o.print(Templates.DLVERTEXPrint2, Concept.getName(), (Op.isNNameTag() ? "=" : "[="), C);
				return;
			case dtLE:
				o.print(Templates.SPACE, n);
				o.print(Templates.SPACE, Role.getName());
				o.print(Templates.SPACE, C);
				return;
			case dtForall:
				o.print(Templates.DLVERTEXPrint3, Role.getName(), n, C);
				return;
			case dtIrr:
				o.print(Templates.SPACE, Role.getName());
				return;
			case dtProj:
				o.print(Templates.DLVERTEXPrint4, Role.getName(), C, ProjRole.getName());
				return;
			default:
				throw new ReasonerInternalException(String.format("Error printing vertex of type %s(%s)", Op.getName(), Op));
		}
		for (int q : Child.sorted()) {
			o.print(Templates.SPACE, q);
		}
	}

	@Override
	public String toString() {
		LogAdapter l = new LeveLogger.LogAdapterStringBuilder();
		Print(l);
		return l.toString();
	}

	/** maximal depth, size and frequency of reference of the expression */
	protected final int[] stat = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };//10 elements

	/** add-up all stat values at once by explicit values */
	public void updateStatValues(int d, int s, int b, int g, boolean pos) {
		StatIndex.updateStatValues(d, s, b, g, pos, stat);
	}

	/** add-up all values at once by a given vertex */
	public void updateStatValues(DLVertex v, boolean posV, boolean pos) {
		StatIndex.updateStatValues(v, posV, pos, stat);
	}

	/** increment frequency value */
	public void incFreqValue(boolean pos) {
		StatIndex.incFreqValue(pos, stat);
	}

	// get methods
	/** general access to a stat value by index */
	public int getStat(int i) {
		return stat[i];
	}

	/** general access to a stat value by index */
	public int getDepth(boolean pos) {
		return StatIndex.getDepth(pos, stat);
	}

	/** usage statistic for pos- and neg occurences of a vertex */
	protected long posUsage = 0;
	protected long negUsage = 0;

	/** get access to a usage wrt POS */
	public long getUsage(boolean pos) {
		return pos ? posUsage : negUsage;
	}
}

class DLVertexTagDFS {
	protected DagTag Op; // 17 types
	/** aux field for DFS in presence of cycles */
	protected boolean VisitedPos = false;
	/** aux field for DFS in presence of cycles */
	protected boolean ProcessedPos = false;
	/** true iff node is involved in cycle */
	protected boolean inCyclePos = false;
	/** aux field for DFS in presence of cycles */
	protected boolean VisitedNeg = false;
	/** aux field for DFS in presence of cycles */
	protected boolean ProcessedNeg = false;
	/** true iff node is involved in cycle */
	protected boolean inCycleNeg = false;

	protected DLVertexTagDFS(DagTag op) {
		Op = op;
	}

	// tag access
	/** return tag of the CE */
	public DagTag Type() {
		return Op;
	}

	// DFS-related method
	/** check whether current Vertex is being visited */
	public boolean isVisited(boolean pos) {
		return pos ? VisitedPos : VisitedNeg;
	}

	/** check whether current Vertex is processed */
	public boolean isProcessed(boolean pos) {
		return pos ? ProcessedPos : ProcessedNeg;
	}

	/** set that the node is being visited */
	public void setVisited(boolean pos) {
		if (pos) {
			VisitedPos = true;
		} else {
			VisitedNeg = true;
		}
	}

	/** set that the node' DFS processing is completed */
	public void setProcessed(boolean pos) {
		if (pos) {
			ProcessedPos = true;
			VisitedPos = false;
		} else {
			ProcessedNeg = true;
			VisitedNeg = false;
		}
	}

	/** clear DFS flags */
	public void clearDFS() {
		ProcessedPos = false;
		VisitedPos = false;
		ProcessedNeg = false;
		VisitedNeg = false;
	}

	/** check whether concept is in cycle */
	public boolean isInCycle(boolean pos) {
		return pos ? inCyclePos : inCycleNeg;
	}

	/** set concept is in cycle */
	public void setInCycle(boolean pos) {
		if (pos) {
			inCyclePos = true;
		} else {
			inCycleNeg = true;
		}
	}

	/** cache for the positive entry */
	protected ModelCacheInterface pCache = null;
	/** cache for the negative entry */
	protected ModelCacheInterface nCache = null;

	/** return cache wrt positive flag */
	public ModelCacheInterface getCache(boolean pos) {
		return pos ? pCache : nCache;
	}

	/** set cache wrt positive flag; note that cache is set up only once */
	public void setCache(boolean pos, ModelCacheInterface p) {
		if (pos) {
			pCache = p;
		} else {
			nCache = p;
		}
	}
}
