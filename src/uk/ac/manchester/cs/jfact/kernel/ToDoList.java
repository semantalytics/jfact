package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.LL;
import static uk.ac.manchester.cs.jfact.kernel.ToDoPriorMatrix.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

import uk.ac.manchester.cs.jfact.dep.DepSet;
import uk.ac.manchester.cs.jfact.dep.TSaveStack;
import uk.ac.manchester.cs.jfact.helpers.FastSetSimple;
import uk.ac.manchester.cs.jfact.helpers.Helper;
import uk.ac.manchester.cs.jfact.helpers.IfDefs;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.helpers.UnreachableSituationException;

public class ToDoList {
	/** the entry of Todo table */
	public static class ToDoEntry {
		/** node to include concept */
		private final DlCompletionTree Node;
		private int Concept;
		private FastSetSimple Delegate;

		ToDoEntry(DlCompletionTree n, ConceptWDep off) {
			Node = n;
			Concept = off.getConcept();
			Delegate = off.getDep().getDelegate();
		}

		protected DlCompletionTree getNode() {
			return Node;
		}

		protected int getOffsetConcept() {
			return Concept;
		}

		protected FastSetSimple getOffsetDepSet() {
			return Delegate;
		}

		@Override
		public String toString() {
			return "Node(" + Node.getId() + "), offset(" + new ConceptWDep(Concept, new DepSet(Delegate)) + ")";
		}

		public void Print(LogAdapter l) {
			l.print("Node(" + Node.getId() + "), offset(");
			new ConceptWDep(Concept, new DepSet(Delegate)).print(l);
			l.print(")");
		}
	}

	/** class to represent single queue */
	static final class ArrayQueue {
		/** waiting ops queue */
		final List<ToDoEntry> Wait = new ArrayList<ToDoEntry>(50);
		/** start pointer; points to the 1st element in the queue */
		int sPointer = 0;

		/** add entry to a queue */
		public void add(DlCompletionTree node, ConceptWDep offset) {
			Wait.add(new ToDoEntry(node, offset));
		}

		/** clear queue */
		public void clear() {
			sPointer = 0;
			Wait.clear();
		}

		/** check if queue empty */
		public boolean isEmpty() {
			return sPointer == Wait.size();
		}

		/** get next entry from the queue; works for non-empty queues */
		public final ToDoEntry get() {
			return Wait.get(sPointer++);
		}

		/** save queue content to the given entry */
		public void save(int[][] tss, int pos) {
			tss[pos][0] = sPointer;//.sp = sPointer;
			tss[pos][1] = Wait.size();// .ep = Wait.size();
		}

		/** restore queue content from the given entry */
		public void restore(int[][] tss, int pos) {
			sPointer = tss[pos][0];//.sp;
			Helper.resize(Wait, tss[pos][1]);//.ep);
		}

		public void restore(int sp, int ep) {
			sPointer = sp;
			Helper.resize(Wait, ep);
		}

		public void Print(LogAdapter l) {
			l.print("ArrayQueue{" + sPointer + ",");
			for (ToDoEntry t : Wait) {
				t.Print(l);
				l.print(" ");
			}
			l.print("}");
		}
	}

	/** class for saving/restoring priority queue Todo */
	static final class QueueQueueSaveState {
		/** save whole array */
		protected List<ToDoEntry> Wait;// = new ArrayList<ToDoEntry>();
		/** save start point of queue of entries */
		protected int sp;
		/** save end point of queue of entries */
		protected int ep;
		/** save flag of queue's consistency */
		protected boolean queueBroken;
	}

	/** class to represent single priority queue */
	static final class QueueQueue {
		/** waiting ops queue */
		private List<ToDoEntry> _Wait = new ArrayList<ToDoEntry>();
		/** start pointer; points to the 1st element in the queue */
		private int sPointer = 0;
		/** flag for checking whether queue was reordered */
		private boolean queueBroken = false;
		int size = 0;

		/** add entry to a queue */
		void add(DlCompletionTree Node, ConceptWDep offset) {
			try {
				if (isEmpty() || // no problems with empty queue and if no priority clashes
						_Wait.get(_Wait.size() - 1).getNode().getNominalLevel() <= Node.getNominalLevel()) {
					_Wait.add(new ToDoEntry(Node, offset));
					return;
				}
				// here we need to put e on the proper place
				int n = _Wait.size();
				ToDoEntry e = new ToDoEntry(Node, offset);
				_Wait.add(e); // will be rewritten
				while (n > sPointer && _Wait.get(n - 1).getNode().getNominalLevel() > Node.getNominalLevel()) {
					_Wait.set(n, _Wait.get(n - 1));
					--n;
				}
				_Wait.set(n, e);
				queueBroken = true;
			} finally {
				size = _Wait.size();
			}
		}

		/** clear queue */
		void clear() {
			sPointer = 0;
			queueBroken = false;
			_Wait.clear();
			size = 0;
		}

		/** check if queue empty */
		boolean isEmpty() {
			return sPointer == size;
		}

		/** get next entry from the queue; works for non-empty queues */
		final ToDoEntry get() {
			return _Wait.get(sPointer++);
		}

		/** save queue content to the given entry */
		void save(QueueQueueSaveState tss) {
			tss.queueBroken = queueBroken;
			tss.sp = sPointer;
			if (queueBroken) {
				tss.Wait = new ArrayList<ToDoList.ToDoEntry>(_Wait);
			} else {
				// save just end pointer
				tss.ep = _Wait.size();
			}
			queueBroken = false; // clear flag for the next session
		}

		/** restore queue content from the given entry */
		void restore(final QueueQueueSaveState tss) {
			queueBroken = tss.queueBroken;
			sPointer = tss.sp;
			if (queueBroken) {
				// the tss variable is discarded at the end of the restore, so no need to copy
				_Wait = tss.Wait;
				size = _Wait.size();
				//_Wait = new ArrayList<ToDoList.ToDoEntry>(tss.Wait);
			} else {
				// save just end pointer
				Helper.resize(_Wait, tss.ep);
				size=tss.ep;
			}

		}

		@Override
		public String toString() {
			return "{" + (!isEmpty() ? _Wait.get(sPointer) : "empty") + " sPointer: " + sPointer + " size: " + size + " Wait: " + _Wait + "}";
		}
	}

	/** class for saving/restoring array Todo table */
	static final class TODOListSaveState {
		/** save state for queueID */
		//protected ArrayQueueSaveState backupID = new ArrayQueueSaveState();
		protected int backupID_sp;
		protected int backupID_ep;
		/** save state for queueNN */
		protected QueueQueueSaveState backupNN = new QueueQueueSaveState();
		/** save state of all regular queues */
		protected int[][] backup = new int[nRegularOps][2];
		/** save number-of-entries to do */
		protected int noe;

		TODOListSaveState() {
		}

//		private static final ConcurrentLinkedQueue<SaveState> states = new ConcurrentLinkedQueue<ToDoList.SaveState>();
//		private static final AtomicInteger statesSize = new AtomicInteger(0);
//		private final static int limit=100;
//		static {
//			
//			Thread stateFiller = new Thread() {
//				@Override
//				public void run() {
//					while (true) {
//						if (statesSize.get() < limit) {
//							for (int i = 0; i < limit; i++) {
//								states.add(new SaveState());
//							}
//							statesSize.addAndGet(limit);
//						}
////						try {
////							Thread.currentThread().sleep(5);
////						} catch (InterruptedException e) {
////							e.printStackTrace();
////						}
//					}
//				}
//			};
//			stateFiller.setDaemon(true);
//			stateFiller.start();
//		}
//
//		public static final SaveState getInstance() {
//			if(statesSize.get()==0) {
//				System.err.println("ToDoList.SaveState.getInstance() waiting...");
//while(statesSize.get()==0) {
//	//wait
//}
//			}
//			SaveState toReturn =  states.poll();
//			if (toReturn == null) {
//				System.err.println("ToDoList.SaveState.getInstance() STILL waiting...");
//				while (toReturn == null) {
//					toReturn = states.poll();
//				}
//			}
//			statesSize.decrementAndGet();
//			return toReturn;
//		}

		@Override
		public String toString() {
			return "" + noe + " " + backupID_sp + "," + backupID_ep + " " + backupNN + " " + Arrays.toString(backup);
		}
	}

	/** waiting ops queue for IDs */
	private ArrayQueue queueID = new ArrayQueue();
	/** waiting ops queue for <= ops in nominal nodes */
	private QueueQueue queueNN = new QueueQueue();
	/** waiting ops queues */
	private List<ArrayQueue> Wait = new ArrayList<ArrayQueue>(nRegularOps);
	/** stack of saved states */
	private TSaveStack<TODOListSaveState> SaveStack = new TSaveStack<TODOListSaveState>();
	/** priority matrix */
	private ToDoPriorMatrix Matrix = new ToDoPriorMatrix();
	/** number of un-processed entries */
	private int noe;

	/** save current Todo table content to given saveState entry */
	void saveState(TODOListSaveState tss) {
		//queueID.save(tss.backupID);
		tss.backupID_sp = queueID.sPointer;
		tss.backupID_ep = queueID.Wait.size();
		queueNN.save(tss.backupNN);
		for (int i = nRegularOps - 1; i >= 0; --i) {
			//tss.backup[i] = new ArrayQueueSaveState();
			Wait.get(i).save(tss.backup, i);
		}
		tss.noe = noe;
	}

	/** restore Todo table content from given saveState entry */
	void restoreState(final TODOListSaveState tss) {
		//queueID.restore(tss.backupID);
		queueID.restore(tss.backupID_sp, tss.backupID_ep);
		queueNN.restore(tss.backupNN);
		for (int i = nRegularOps - 1; i >= 0; --i) {
			Wait.get(i).restore(tss.backup[i][0], tss.backup[i][1]);
		}
		noe = tss.noe;
	}

	ToDoList() {
		noe = 0;
		//		Helper.resize(Wait, nRegularOps);
		for (int i = 0; i < nRegularOps; i++) {
			Wait.add(new ArrayQueue());
		}
	}

	/** init priorities via Options */
	void initPriorities(final IFOptionSet Options, final String optionName) {
		Matrix.initPriorities(Options.getText(optionName), optionName);
	}

	/** clear Todo table */
	void clear() {
		queueID.clear();
		queueNN.clear();
		for (int i = nRegularOps - 1; i >= 0; --i) {
			Wait.get(i).clear();
		}
		SaveStack.clear();
		noe = 0;
	}

	/** check if Todo table is empty */
	boolean isEmpty() {
		return noe == 0;
	}

	// work with entries
	/**
	 * add entry with given NODE and CONCEPT with given OFFSET to the Todo table
	 */
	void addEntry(DlCompletionTree node, DagTag type, ConceptWDep C) {
		int index = Matrix.getIndex(type, C.getConcept() > 0, node.isNominalNode());
		switch (index) {
			case nRegularOps: // unused entry
				return;
			case iId: // ID
				queueID.add(node, C);
				break;
			case iNN: // NN
				queueNN.add(node, C);
				break;
			default: // regular queue
				Wait.get(index).add(node, C);
				break;
		}
		++noe;
	}

	/** save current state using internal stack */
	void save() {
		TODOListSaveState state = new TODOListSaveState();
		saveState(state);
		SaveStack.push(state);
	}

	/** restore state to the given level using internal stack */
	void restore(int level) {
		restoreState(SaveStack.pop(level));
	}

	final ToDoEntry getNextEntry() {
		assert !isEmpty(); // safety check
		// decrease amount of elements-to-process
		--noe;
		// check ID queue
		if (!queueID.isEmpty()) {
			return queueID.get();
		}
		// check NN queue
		if (!queueNN.isEmpty()) {
			return queueNN.get();
		}
		// check regular queues
		for (int i = 0; i < nRegularOps; ++i) {
			ArrayQueue arrayQueue = Wait.get(i);
			if (!arrayQueue.isEmpty()) {
				return arrayQueue.get();
			}
		}
		// that's impossible, but still...
		return null;
	}

	public void Print(LogAdapter lL) {
		lL.print("Todolist{");
		lL.println();
		queueID.Print(lL);
		lL.println();
		for (int i = 0; i < nRegularOps; ++i) {
			Wait.get(i).Print(lL);
			lL.println();
		}
		lL.println();
		lL.print("}");
	}
}

class ToDoPriorMatrix {
	// regular operation indexes
	private int iAnd;
	private int iOr;
	private int iExists;
	private int iForall;
	private int iLE;
	private int iGE;

	public ToDoPriorMatrix() {
	}

	/** number of regular options (o- and NN-rules are not included) */
	protected static final int nRegularOps = 7;
	/**
	 * priority index for o- and ID operations (note that these ops have the
	 * highest priority)
	 */
	protected static final int iId = nRegularOps + 1;
	/** priority index for <= operation in nominal node */
	protected static final int iNN = nRegularOps + 2;

	/** Auxiliary class to get priorities on operations */
	public void initPriorities(final String options, final String optionName) {
		// check for correctness
		if (options.length() < 7) {
			throw new ReasonerInternalException("ToDo List option string should have length 7");
		}
		// init values by symbols loaded
		iAnd = options.charAt(1) - '0';
		iOr = options.charAt(2) - '0';
		iExists = options.charAt(3) - '0';
		iForall = options.charAt(4) - '0';
		iLE = options.charAt(5) - '0';
		iGE = options.charAt(6) - '0';
		// correctness checking
		if (iAnd >= nRegularOps || iOr >= nRegularOps || iExists >= nRegularOps || iForall >= nRegularOps || iGE >= nRegularOps || iLE >= nRegularOps) {
			throw new ReasonerInternalException("ToDo List option out of range");
		}
		// inform about used rules order
		if (IfDefs._USE_LOGGING) {
			LL.print(String.format("\nInit %s = %s%s%s%s%s%s", optionName, iAnd, iOr, iExists, iForall, iLE, iGE));
		}
	}

	public int getIndex(DagTag Op, boolean Sign, boolean NominalNode) {
		switch (Op) {
			case dtAnd:
				return Sign ? iAnd : iOr;
			case dtForall:
			case dtUAll:
			case dtIrr: // process local (ir-)reflexivity as a FORALL
				return Sign ? iForall : iExists;
			case dtProj: // it should be the lowest priority but now just OR's one
				return iOr;
			case dtLE:
				return Sign ? (NominalNode ? iNN : iLE) : iGE;
			case dtDataType:
			case dtDataValue:
			case dtDataExpr:
			case dtNN:
			case dtTop: // no need to process these ops
				return nRegularOps;
			case dtPSingleton:
			case dtPConcept: // no need to process neg of PC
				return Sign ? iId : nRegularOps;
			case dtNSingleton:
			case dtNConcept: // both NC and neg NC are processed
				return iId;
			default: // safety check
				throw new UnreachableSituationException();
				//				return -1;
		}
	}
}