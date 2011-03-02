package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import uk.ac.manchester.cs.jfact.dep.DepSet;
import uk.ac.manchester.cs.jfact.dep.DepSetFactory;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;

public  final class DlCompletionTreeArc {
	/** pointer to "to" node */
	private DlCompletionTree Node;
	/** role, labelling given arc */
	protected TRole Role;
	/** dep-set of the arc */
	protected DepSet depSet;
	/** pointer to reverse arc */
	protected DlCompletionTreeArc Reverse;
	/** true if the edge going from a predecessor to a successor */
	private boolean SuccEdge;

	/**
	 * init an arc with R as a label and NODE on given LEVEL; use it inside
	 * MAKEARCS only
	 */
	protected void init(final TRole role, final DepSet dep,
			DlCompletionTree node) {
		Role = role;
		depSet = DepSetFactory.create(dep);
		Node = node;
		Reverse = null;
	}

	/** class for restoring edge */
	 final static class TCTEdgeRestorer extends TRestorer {
		private DlCompletionTreeArc p;
		private TRole r;

		public TCTEdgeRestorer(DlCompletionTreeArc q) {
			p = q;
			r = q.Role;
		}

		@Override
		public void restore() {
			p.Role = r;
			p.Reverse.Role = r.inverse();
		}
	}

	/** class for restoring dep-set */
	 final 	static class TCTEdgeDepRestorer extends TRestorer {
		private DlCompletionTreeArc p;
		private final DepSet dep;

		public TCTEdgeDepRestorer(DlCompletionTreeArc q) {
			p = q;
			dep = DepSetFactory.create(q.getDep());
		}

		@Override
		public void restore() {
			p.depSet = DepSetFactory.create(dep);
		}
	}

	/** set given arc as a reverse of current */
	protected void setReverse(DlCompletionTreeArc v) {
		Reverse = v;
		v.Reverse = this;
	}

	public DlCompletionTreeArc() {
		SuccEdge = true;
	}

	/** get label of the edge */
	public TRole getRole() {
		return Role;
	}

	/** get dep-set of the edge */
	protected DepSet getDep() {
		return depSet;
	}

	/** set the successor field */
	protected void setSuccEdge(boolean val) {
		SuccEdge = val;
	}

	/** @return true if the edge is the successor one */
	protected boolean isSuccEdge() {
		return SuccEdge;
	}

	/** @return true if the edge is the predecessor one */
	protected boolean isPredEdge() {
		return !SuccEdge;
	}

	/** get (RW) access to the end of arc */
	protected DlCompletionTree getArcEnd() {
		return Node;
	}

	/** get access to reverse arc */
	protected DlCompletionTreeArc getReverse() {
		return Reverse;
	}

	/** check if arc is labelled by a super-role of PROLE */
	protected boolean isNeighbour(final TRole pRole) {
		return Role != null && Role.lesserequal(pRole);
	}

	/** same as above; fills DEP with current DEPSET if so */
	protected boolean isNeighbour(final TRole pRole, DepSet dep) {
		if (isNeighbour(pRole)) {
			dep.clear();
			dep.add(depSet);
			return true;
		}
		return false;
	}

	/** is arc merged to another */
	public boolean isIBlocked() {
		return Role == null;
	}

	/** check whether the edge is reflexive */
	protected boolean isReflexiveEdge() {
		return Node.equals(Reverse.Node);
	}

	// saving/restoring
	/** save and invalidate arc (together with reverse arc) */
	protected TRestorer save() {
		if (Role == null) {
			throw new IllegalArgumentException();
		}
		TRestorer ret = new TCTEdgeRestorer(this);
		Role = null;
		Reverse.Role = null;
		return ret;
	}

	/** add dep-set to an edge; return restorer */
	protected TRestorer addDep(DepSet dep) {
		if (dep.isEmpty()) {
			throw new IllegalArgumentException();
		}
		TRestorer ret = new TCTEdgeDepRestorer(this);
		depSet.add(dep);
		return ret;
	}

	// output
	/** print current arc */
	protected void Print(LogAdapter o) {
		o.print(Templates.DLCOMPLETIONTREEARC,
				(isIBlocked() ? "-" : Role.getName()), depSet);
	}
}