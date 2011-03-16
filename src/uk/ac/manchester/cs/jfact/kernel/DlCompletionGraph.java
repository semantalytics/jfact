package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.Helper.InitBranchingLevelValue;
import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.LL;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import uk.ac.manchester.cs.jfact.dep.DepSet;
import uk.ac.manchester.cs.jfact.dep.TSaveStack;
import uk.ac.manchester.cs.jfact.helpers.FastSet;
import uk.ac.manchester.cs.jfact.helpers.FastSetFactory;
import uk.ac.manchester.cs.jfact.helpers.Helper;
import uk.ac.manchester.cs.jfact.helpers.IfDefs;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;
import uk.ac.manchester.cs.jfact.helpers.Reference;
import uk.ac.manchester.cs.jfact.kernel.state.DLCompletionGraphSaveState;

public final class DlCompletionGraph {
	/** initial value of IR level */
	private static final int initIRLevel = 0;
	//XXX is this actually used?
	/** allocator for edges */
	private final List<DlCompletionTreeArc> CTEdgeHeap = new ArrayList<DlCompletionTreeArc>();
	/** heap itself */
	private final List<DlCompletionTree> NodeBase;
	/** nodes, saved on current branching level */
	private final List<DlCompletionTree> SavedNodes = new ArrayList<DlCompletionTree>();
	/** host reasoner */
	private final DlSatTester pReasoner;
	/** remember the last generated ID for the node */
	private int nodeId = 0;
	/** index of the next unallocated entry */
	private int endUsed;
	/** current branching level (synchronised with resoner's one) */
	private int branchingLevel;
	/** current IR level (should be valid BP) */
	private int IRLevel;
	/** stack for rarely changed information */
	private final TRareSaveStack RareStack = new TRareSaveStack();
	/** stack for usual saving/restoring */
	public final TSaveStack<DLCompletionGraphSaveState> Stack = new TSaveStack<DLCompletionGraphSaveState>();
	// helpers for the output
	/** bitmap to remember which node was printed */
	// TODO change to regular
	private final FastSet CGPFlag = FastSetFactory.create();
	/** indent to print CGraph nodes */
	private int CGPIndent;
	// statistical members
	/** number of node' saves */
	private int nNodeSaves;
	/** number of node' saves */
	private int nNodeRestores;
	// flags
	/** use or not lazy blocking (ie test blocking only expanding exists) */
	private boolean useLazyBlocking;
	/** whether to use Anywhere blocking as opposed to an ancestor one */
	private boolean useAnywhereBlocking;
	/** check if session has inverse roles */
	private boolean sessionHasInverseRoles;
	/** check if session has number restrictions */
	private boolean sessionHasNumberRestrictions;

	/** init vector [B,E) with new objects T */
	private void initNodeArray(List<DlCompletionTree> l, int b, int e) {
		for (int p = b; p < e; ++p) {
			l.set(p, new DlCompletionTree(nodeId++));
		}
	}

	/** increase heap size */
	private void grow() {
		int size = NodeBase.size();
		Helper.resize(NodeBase, NodeBase.size() * 2);
		initNodeArray(NodeBase, size, NodeBase.size());
	}

	/** init root node */
	private void initRoot() {
		assert endUsed == 0;
		getNewNode();
	}

	/** invalidate EDGE, save restoring info */
	private void invalidateEdge(DlCompletionTreeArc edge) {
		saveRareCond(edge.save());
	}

	/** check if d-blocked node is still d-blocked */
	private boolean isStillDBlocked(final DlCompletionTree node) {
		return node.isDBlocked() && isBlockedBy(node, node.Blocker);
	}

	/** try to find d-blocker for a node */
	private void findDBlocker(DlCompletionTree node) {
		saveNode(node, branchingLevel);
		node.clearAffected();
		if (node.isBlocked()) {
			saveRareCond(node.setUBlocked());
		}
		if (useAnywhereBlocking) {
			findDAnywhereBlocker(node);
		} else {
			findDAncestorBlocker(node);
		}
	}

	/** unblock all the children of the node */
	private void unblockNodeChildren(DlCompletionTree node) {
		for (DlCompletionTreeArc q : node.getNeighbour()) {
			if (q.isSuccEdge() && !q.isIBlocked() && !q.isReflexiveEdge()) {
				unblockNode(q.getArcEnd(), false);
			}
		}
	}

	/** mark NODE as a d-blocked by a BLOCKER */
	private void setNodeDBlocked(DlCompletionTree node, final DlCompletionTree blocker) {
		saveRareCond(node.setDBlocked(blocker));
		propagateIBlockedStatus(node, node);
	}

	/** mark NODE as an i-blocked by a BLOCKER */
	private void setNodeIBlocked(DlCompletionTree node, final DlCompletionTree blocker) {
		// nominal nodes can't be blocked
		if (node.isPBlocked() || node.isNominalNode()) {
			return;
		}
		node.clearAffected();
		// already iBlocked -- nothing changes
		if (node.isIBlocked() && node.Blocker.equals(blocker)) {
			return;
		}
		// prevent node to be IBlocked due to reflexivity
		if (node.equals(blocker)) {
			return;
		}
		saveRareCond(node.setIBlocked(blocker));
		propagateIBlockedStatus(node, blocker);
	}

	/** propagate i-blocked status to all children of NODE */
	private void propagateIBlockedStatus(DlCompletionTree node, final DlCompletionTree blocker) {
		List<DlCompletionTreeArc> neighbour = node.getNeighbour();
		int size=neighbour.size();
		for (int i=0;i<size;i++) {DlCompletionTreeArc q = neighbour.get(i);
			if (q.isSuccEdge() && !q.isIBlocked()) {
				setNodeIBlocked(q.getArcEnd(), blocker);
			}
		}
	}

	/** @return true iff node might became unblocked */
	private boolean canBeUnBlocked(DlCompletionTree node) {
		// in presence of inverse roles it is not enough
		// to check the affected flag for both node and its blocker
		// see tModal* for example
		if (sessionHasInverseRoles) {
			return true;
		}
		// if node is affected -- it can be unblocked;
		// if blocker became blocked itself -- the same
		return node.isAffected() || node.isIllegallyDBlocked();
	}

	/** print proper indentation */
	private void PrintIndent(LogAdapter o) {
		o.print("\n|");
		for (int i = 1; i < CGPIndent; ++i) {
			o.print(" |");
		}
	}

	/** c'tor: make INIT_SIZE objects */
	protected DlCompletionGraph(int initSize, DlSatTester p) {
		NodeBase = new ArrayList<DlCompletionTree>();
		Helper.resize(NodeBase, initSize);
		pReasoner = p;
		nodeId = 0;
		endUsed = 0;
		branchingLevel = InitBranchingLevelValue;
		IRLevel = initIRLevel;
		initNodeArray(NodeBase, 0, NodeBase.size());
		clearStatistics();
		initRoot();
	}

	// flag setting
	/** set flags for blocking */
	protected void initContext(boolean useLB, boolean useAB) {
		useLazyBlocking = useLB;
		useAnywhereBlocking = useAB;
	}

	/** set blocking method for a session */
	protected void setBlockingMethod(boolean hasInverse, boolean hasQCR) {
		sessionHasInverseRoles = hasInverse;
		sessionHasNumberRestrictions = hasQCR;
	}

	/** add concept C of a type TAG to NODE; call blocking check if appropriate */
	protected void addConceptToNode(DlCompletionTree node, ConceptWDep c, DagTag tag) {
		node.addConcept(c, tag);
		if (useLazyBlocking) {
			node.setAffected();
		} else {
			detectBlockedStatus(node);
		}
	}

	// access to nodes
	/** get a root node (non-const) */
	protected DlCompletionTree getRoot() {
		return NodeBase.get(0).resolvePBlocker();
	}

	/** get new node (with internal level) */
	protected DlCompletionTree getNewNode() {
		if (endUsed >= NodeBase.size()) {
			grow();
		}
		DlCompletionTree ret = NodeBase.get(endUsed++);
		ret.init(branchingLevel);
		return ret;
	}

	// blocking
	/** update blocked status for d-blocked node */
	protected void updateDBlockedStatus(DlCompletionTree node) {
		if (!canBeUnBlocked(node)) {
			return;
		}
		if (isStillDBlocked(node)) {
			// FIXME!! clear affected in all children
			node.clearAffected();
		} else {
			detectBlockedStatus(node);
		}
		assert !node.isAffected();
	}

	/** retest every d-blocked node in the CG. Use it after the CG was build */
	protected void retestCGBlockedStatus() {
		boolean repeat = false;
		do {
			for (int i = 0; i < endUsed; i++) {
				DlCompletionTree p = NodeBase.get(i);
				if (p.isDBlocked()) {
					updateDBlockedStatus(p);
				}
			}
			// we need to repeat the thing if something became unblocked and then blocked again,
			// in case one of the blockers became blocked itself; see tModal3 for such an example
			repeat = false;
			for (int i = 0; i < endUsed; i++) {
				DlCompletionTree p = NodeBase.get(i);
				if (p.isIllegallyDBlocked()) {
					repeat = true;
					break;
				}
			}
		} while (repeat);
	}

	/** clear all the session statistics */
	protected void clearStatistics() {
		nNodeSaves = 0;
		nNodeRestores = 0;
	}

	/** mark all heap elements as unused */
	protected void clear() {
		CTEdgeHeap.clear();
		endUsed = 0;
		branchingLevel = InitBranchingLevelValue;
		IRLevel = initIRLevel;
		RareStack.clear();
		Stack.clear();
		SavedNodes.clear();
		initRoot();
	}

	/** get number of nodes in the CGraph */
	protected int maxSize() {
		return NodeBase.size();
	}

	/** save rarely appeared info if P is non-null */
	protected void saveRareCond(TRestorer p) {
		if (p == null) {
			throw new IllegalArgumentException();
		}
		RareStack.push(p);
	}

	protected void saveRareCond(List<TRestorer> p) {
		for (int i = 0; i < p.size(); i++) {
			RareStack.push(p.get(i));
		}
	}

	// role/node
	/** add role R with dep-set DEP to the label of the TO arc */
	protected DlCompletionTreeArc addRoleLabel(DlCompletionTree from, DlCompletionTree to, boolean isPredEdge, final TRole R, // name of role (arc label)
			final DepSet dep) // dep-set of the arc label
	{
		// check if GCraph already has FROM.TO edge labelled with RNAME
		DlCompletionTreeArc ret = from.getEdgeLabelled(R, to);
		if (ret == null) {
			ret = createEdge(from, to, isPredEdge, R, dep);
		} else {
			if (!dep.isEmpty()) {
				saveRareCond(ret.addDep(dep));
			}
		}
		return ret;
	}

	/** Create an empty R-neighbour of FROM; @return an edge to created node */
	protected DlCompletionTreeArc createNeighbour(DlCompletionTree from, boolean isPredEdge, final TRole r, // name of role (arc label)
			final DepSet dep) // dep-set of the arc label
	{
		if (IfDefs.RKG_IMPROVE_SAVE_RESTORE_DEPSET) {
			assert branchingLevel == dep.level() + 1;
		}
		return createEdge(from, getNewNode(), isPredEdge, r, dep);
	}

	/** Create an R-loop of NODE wrt dep-set DEP; @return a loop edge */
	protected DlCompletionTreeArc createLoop(DlCompletionTree node, final TRole r, final DepSet dep) {
		return addRoleLabel(node, node, /*isPredEdge=*/false, r, dep);
	}

	/** save given node wrt level */
	protected void saveNode(DlCompletionTree node, int level) {
		if (node.needSave(level)) {
			node.save(level);
			SavedNodes.add(node);
			++nNodeSaves;
		}
	}

	/** restore given node wrt level */
	private void restoreNode(DlCompletionTree node, int level) {
		if (node.needRestore(level)) {
			node.restore(level);
			++nNodeRestores;
		}
	}

	private boolean isBlockedBy(final DlCompletionTree node, final DlCompletionTree blocker) {
		assert !node.isNominalNode();
		assert !blocker.isNominalNode();
		if (blocker.isBlocked()) {
			return false;
		}
		if (!blocker.canBlockInit(node.getInit())) {
			return false;
		}
		boolean ret;
		if (sessionHasInverseRoles) {
			final DLDag dag = pReasoner.getDAG();
			if (sessionHasNumberRestrictions) {
				ret = node.isBlockedBy_SHIQ(dag, blocker);
			} else {
				ret = node.isBlockedBy_SHI(dag, blocker);
			}
		} else {
			ret = node.isBlockedBy_SH(blocker);
		}
		if (IfDefs.USE_BLOCKING_STATISTICS && !ret && IfDefs._USE_LOGGING) {
			LL.print(Templates.IS_BLOCKED_FAILURE_BY, node.getId(), blocker.getId());
		}
		return ret;
	}

	//	void prepareForContains() {
	//		for (int i = 0; i < endUsed; i++) {
	//			CacheMaster.instance.holdOntoThis(NodeBase.get(i).label());
	//		}
	//	}
	//
	//	void clearForContains() {
	//		for (int i = 0; i < endUsed; i++) {
	//			CacheMaster.instance.stopHolding(NodeBase.get(i).label());
	//		}
	//	}
	public void detectBlockedStatus(DlCompletionTree node) {
		DlCompletionTree p = node;
		boolean wasBlocked = node.isBlocked();
		boolean wasDBlocked = node.isDBlocked();
		node.setAffected();
		while (p.hasParent() && p.isBlockableNode() && p.isAffected()) {
			findDBlocker(p);
			if (p.isBlocked()) {
				//	this.Print(LL);
				return;
			}
			p = p.getParentNode();
		}
		p.clearAffected();
		if (wasBlocked && !node.isBlocked()) {
			unblockNode(node, wasDBlocked);
		}
	}

	private void unblockNode(DlCompletionTree node, boolean wasDBlocked) {
		if (node.isPBlocked() || !node.isBlockableNode()) {
			return;
		}
		if (!wasDBlocked) {
			saveRareCond(node.setUBlocked());
		}
		pReasoner.repeatUnblockedNode(node, wasDBlocked);
		unblockNodeChildren(node);
	}

	private void findDAncestorBlocker(DlCompletionTree node) {
		DlCompletionTree p = node;
		while (p.hasParent()) {
			p = p.getParentNode();
			if (!p.isBlockableNode()) {
				return;
			}
			if (isBlockedBy(node, p)) {
				setNodeDBlocked(node, p);
				return;
			}
		}
	}

	private void findDAnywhereBlocker(DlCompletionTree node) {
		for (int i = 0; i < endUsed && i != node.getId(); i++) {
			//	  final DlCompletionTree p = q;
			//			if (i == node.getId()) {
			//				return;
			//			}
			DlCompletionTree p = NodeBase.get(i);
			if (!p.isBlockedPBlockedNominalNodeCached()) {
				if (isBlockedBy(node, p)) {
					setNodeDBlocked(node, p);
					return;
				}
			}
		}
	}

	/**
	 * Class for maintaining graph of CT nodes. Behaves like deleteless
	 * allocator for nodes, plus some obvious features
	 */
	protected boolean nonMergable(final DlCompletionTree p, final DlCompletionTree q, Reference<DepSet> dep) {
		return p.nonMergable(q, dep);
	}

	private void updateIR(DlCompletionTree p, final DlCompletionTree q, final DepSet toAdd) {
		if (!q.IR.isEmpty()) {
			saveRareCond(p.updateIR(q, toAdd));
		}
	}

	protected void initIR() {
		++IRLevel;
	}

	protected boolean setCurIR(DlCompletionTree node, final DepSet ds) {
		return node.initIR(IRLevel, ds);
	}

	protected void finiIR() {
	}

	private DlCompletionTreeArc createEdge(DlCompletionTree from, DlCompletionTree to, boolean isPredEdge, final TRole roleName, final DepSet dep) {
		DlCompletionTreeArc forward = new DlCompletionTreeArc();
		CTEdgeHeap.add(forward);
		forward.init(roleName, dep, to);
		forward.setSuccEdge(!isPredEdge);
		DlCompletionTreeArc backward = new DlCompletionTreeArc();
		CTEdgeHeap.add(backward);
		backward.init(roleName.inverse(), dep, from);
		backward.setSuccEdge(isPredEdge);
		forward.setReverse(backward);
		saveNode(from, branchingLevel);
		saveNode(to, branchingLevel);
		from.addNeighbour(forward);
		to.addNeighbour(backward);
		if (IfDefs._USE_LOGGING) {
			LL.print(Templates.CREATE_EDGE, (isPredEdge ? to.getId() : from.getId()), (isPredEdge ? "<-" : "->"), (isPredEdge ? from.getId() : to.getId()), roleName.getName());
		}
		return forward;
	}

	private DlCompletionTreeArc moveEdge(DlCompletionTree node, DlCompletionTreeArc edge, boolean isPredEdge, final DepSet dep) {
		if (edge.isIBlocked()) {
			return null;
		}
		if (!isPredEdge && !edge.getArcEnd().isNominalNode()) {
			return null;
		}
		final TRole R = edge.getRole();
		if (edge.isReflexiveEdge()) {
			return createLoop(node, R, dep);
		}
		DlCompletionTree to = edge.getArcEnd();
		if (R != null) {
			invalidateEdge(edge);
		}
		for (DlCompletionTreeArc p : node.getNeighbour()) {
			if (p.getArcEnd().equals(to) && p.isPredEdge() != isPredEdge) {
				return addRoleLabel(node, to, !isPredEdge, R, dep);
			}
		}
		return addRoleLabel(node, to, isPredEdge, R, dep);
	}

	protected void Merge(DlCompletionTree from, DlCompletionTree to, final DepSet dep, List<DlCompletionTreeArc> edges) {
		edges.clear();
		for (DlCompletionTreeArc p : from.getNeighbour()) {
			if (p.isPredEdge() || p.getArcEnd().isNominalNode()) {
				DlCompletionTreeArc temp = moveEdge(to, p, p.isPredEdge(), dep);
				if (temp != null) {
					edges.add(temp);
				}
			}
			if (p.isSuccEdge()) {
				purgeEdge(p, to, dep);
			}
		}
		updateIR(to, from, dep);
		purgeNode(from, to, dep);
	}

	private void purgeNode(DlCompletionTree p, final DlCompletionTree root, final DepSet dep) {
		if (p.isPBlocked()) {
			return;
		}
		saveRareCond(p.setPBlocked(root, dep));
		for (DlCompletionTreeArc q : p.getNeighbour()) {
			if (q.isSuccEdge() && !q.isIBlocked()) {
				purgeEdge(q, root, dep);
			}
		}
	}

	private void purgeEdge(DlCompletionTreeArc e, final DlCompletionTree root, final DepSet dep) {
		if (e.getRole() != null) {
			invalidateEdge(e);
		}
		if (e.getArcEnd().isBlockableNode()) {
			purgeNode(e.getArcEnd(), root, dep);
		}
	}

	protected void save() {
		DLCompletionGraphSaveState s = new DLCompletionGraphSaveState();
		Stack.push(s);
		s.setnNodes(endUsed);
		s.setsNodes(SavedNodes.size());
		s.setnEdges(CTEdgeHeap.size());
		RareStack.incLevel();
		++branchingLevel;
	}

	protected void restore(int level) {
		assert level > 0;
		branchingLevel = level;
		RareStack.restore(level);
		DLCompletionGraphSaveState s = Stack.pop(level);
		endUsed = s.getnNodes();
		int nSaved = s.getsNodes();
		if (endUsed < Math.abs(SavedNodes.size() - nSaved)) {
			for (int i = 0; i < endUsed; i++) {
				//XXX check: it was taking into account also empty nodes
				//DlCompletionTree p : NodeBase) {
				restoreNode(NodeBase.get(i), level);
			}
		} else {
			for (int i = nSaved; i < SavedNodes.size(); i++) {
				if (SavedNodes.get(i).getId() < endUsed) {
					restoreNode(SavedNodes.get(i), level);
				}
			}
		}
		Helper.resize(SavedNodes, nSaved);
		Helper.resize(CTEdgeHeap, s.getnEdges());
	}

	protected void Print(LeveLogger.LogAdapter o) {
		CGPIndent = 0;
		CGPFlag.clear();
		//		for (int i = 0; i < endUsed; i++) {
		//			CGPFlag.add(false);
		//		}
		List<DlCompletionTree> l = NodeBase;
		for (int i = 1; i < endUsed && l.get(i).isNominalNode(); ++i) {
			CGPFlag.add(i);
		}
		PrintNode(l.get(0), o);
		for (int i = 1; i < endUsed && l.get(i).isNominalNode(); ++i) {
			CGPFlag.remove(l.get(i).getId());
			PrintNode(l.get(i), o);
		}
		o.print("\n");
	}

	private void PrintEdge(List<DlCompletionTreeArc> l, int pos, DlCompletionTreeArc _edge, final DlCompletionTree parent, LogAdapter o) {
		DlCompletionTreeArc edge = _edge;
		final DlCompletionTree node = edge.getArcEnd();
		boolean succEdge = edge.isSuccEdge();
		PrintIndent(o);
		if (edge.getArcEnd().equals(node) && edge.isSuccEdge() == succEdge) {
			o.print(" ");
			edge.Print(o);
		}
		for (; pos < l.size(); pos++) {
			edge = l.get(pos);
			if (edge.getArcEnd().equals(node) && edge.isSuccEdge() == succEdge) {
				o.print(" ");
				edge.Print(o);
			}
		}
		if (node.equals(parent)) {
			PrintIndent(o);
			o.print("-loop to node ");
			o.print(parent.getId());
		} else {
			PrintNode(node, o);
		}
	}

	private void PrintNode(final DlCompletionTree node, LogAdapter o) {
		if (CGPIndent > 0) {
			PrintIndent(o);
			o.print("-");
		} else {
			o.print("\n");
		}
		node.PrintBody(o);
		if (CGPFlag.contains(node.getId())) {
			o.print("d");
			return;
		}
		CGPFlag.add(node.getId());
		boolean wantPred = node.isNominalNode();
		++CGPIndent;
		List<DlCompletionTreeArc> l = node.getNeighbour();
		for (int i = 0; i < l.size(); i++) {
			if (l.get(i).isSuccEdge() || wantPred && l.get(i).getArcEnd().isNominalNode()) {
				PrintEdge(l, i + 1, l.get(i), node, o);
			}
		}
		--CGPIndent;
	}
}

final class TRareSaveStack {
	/** heap of saved objects */
	private final LinkedList<TRestorer> Base = new LinkedList<TRestorer>();
	/** current level */
	private int curLevel;

	public TRareSaveStack() {
		curLevel = InitBranchingLevelValue;
	}

	/** inclrement current level */
	public void incLevel() {
		++curLevel;
	}

	/** check that stack is empty */
	public boolean isEmpty() {
		return Base.isEmpty();
	}

	/** add a new object to the stack */
	public void push(TRestorer p) {
		p.setRaresavestackLevel(curLevel);
		Base.addLast(p);
	}

	/** get all object from the top of the stack with levels >= LEVEL */
	public void restore(int level) {
		curLevel = level;
		while (Base.size() > 0 && Base.getLast().getRaresavestackLevel() > level) {
			// need to restore: restore last element, remove it from stack
			Base.getLast().restore();
			//delete cur;
			Base.removeLast();
		}
	}

	/** clear stack */
	public void clear() {
		Base.clear();
		curLevel = InitBranchingLevelValue;
	}
}