package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.logger;
import static uk.ac.manchester.cs.jfact.kernel.ClassifiableEntry.resolveSynonym;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

import uk.ac.manchester.cs.jfact.helpers.IfDefs;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;
import uk.ac.manchester.cs.jfact.kernel.actors.Actor;
import uk.ac.manchester.cs.jfact.kernel.actors.SupConceptActor;

public class Taxonomy {
	/** array of taxonomy verteces */
	private final List<TaxonomyVertex> graph = new ArrayList<TaxonomyVertex>();
	/** aux. vertex to be included to taxonomy */
	protected TaxonomyVertex current;
	/** pointer to currently classified entry */
	protected ClassifiableEntry curEntry;
	/** number of tested entryes */
	protected int nEntries;
	/** number of completely-defined entries */
	protected long nCDEntries;
	/**
	 * optimisation flag: if entry is completely defined by it's told subsumers,
	 * no other classification required
	 */
	protected boolean useCompletelyDefined;
	/** behaviour flag: if true, insert temporary vertex into taxonomy */
	protected boolean willInsertIntoTaxonomy;
	/** stack for Taxonomy creation */
	private final LinkedList<ClassifiableEntry> waitStack = new LinkedList<ClassifiableEntry>();
	/** told subsumers corresponding to a given entry */
	private final LinkedList<Collection<ClassifiableEntry>> ksStack = new LinkedList<Collection<ClassifiableEntry>>();
	/** labellers for marking taxonomy */
	protected long checkLabel = 1;
	protected long valueLabel = 1;

	/**
	 * apply ACTOR to subgraph starting from NODE as defined by flags; this
	 * version is intended to work only with SupConceptActor, which requires the
	 * method to return as soon as the apply() method returns false
	 */
	public boolean getRelativesInfo(TaxonomyVertex node, SupConceptActor actor,
			boolean needCurrent, boolean onlyDirect, boolean upDirection) {
		// if current node processed OK and there is no need to continue -- exit
		// this is the helper to the case like getDomain():
		//   if there is a named concept that represent's a domain -- that's what we need
		if (needCurrent) {
			if (!actor.apply(node)) {
				return false;
			}
			if (onlyDirect) {
				return true;
			}
		}
		Queue<List<TaxonomyVertex>> queue = new LinkedList<List<TaxonomyVertex>>();
		queue.add(node.neigh(upDirection));
		while (queue.size() > 0) {
			List<TaxonomyVertex> neigh = queue.remove();
			final int size = neigh.size();
			for (int i = 0; i < size; i++) {
				TaxonomyVertex _node = neigh.get(i);
				// recursive applicability checking
				if (!_node.isChecked(checkLabel)) {
					// label node as visited
					_node.setChecked(checkLabel);
					// if current node processed OK and there is no need to continue -- exit
					// if node is NOT processed for some reasons -- go to another level
					if (!actor.apply(_node)) {
						return false;
					}
					if (onlyDirect) {
						continue;
					}
					// apply method to the proper neighbours with proper parameters
					queue.add(_node.neigh(upDirection));
				}
			}
		}
		clearCheckedLabel();
		return true;
	}

	/** apply ACTOR to subgraph starting from NODE as defined by flags; */
	public void getRelativesInfo(TaxonomyVertex node, Actor actor,
			boolean needCurrent, boolean onlyDirect, boolean upDirection) {
		// if current node processed OK and there is no need to continue -- exit
		// this is the helper to the case like getDomain():
		//   if there is a named concept that represent's a domain -- that's what we need
		if (needCurrent && actor.apply(node) && onlyDirect) {
			return;
		}
		Queue<List<TaxonomyVertex>> queue = new LinkedList<List<TaxonomyVertex>>();
		queue.add(node.neigh(upDirection));
		while (queue.size() > 0) {
			List<TaxonomyVertex> neigh = queue.remove();// node.neigh(upDirection);
			final int size = neigh.size();
			for (int i = 0; i < size; i++) {
				TaxonomyVertex _node = neigh.get(i);
				// recursive applicability checking
				if (!_node.isChecked(checkLabel)) {
					// label node as visited
					_node.setChecked(checkLabel);
					// if current node processed OK and there is no need to continue -- exit
					// if node is NOT processed for some reasons -- go to another level
					if (actor.apply(_node) && onlyDirect) {
						continue;
					}
					// apply method to the proper neighbours with proper parameters
					queue.add(_node.neigh(upDirection));
				}
			}
		}
		clearCheckedLabel();
	}

	/** clear the CHECKED label from all the taxonomy vertex */
	protected final void clearCheckedLabel() {
		checkLabel++;
	}

	private void clearLabels() {
		checkLabel++;
		valueLabel++;
	}

	/** initialise aux entry with given concept p */
	private void setCurrentEntry(final ClassifiableEntry p) {
		current.clear();
		curEntry = p;
	}

	/** check if no classification needed (synonym, orphan, unsatisfiable) */
	protected boolean immediatelyClassified() {
		return classifySynonym();
	}

	/** check if it is possible to skip TD phase */
	protected boolean needTopDown() {
		return false;
	}

	/** explicitely run TD phase */
	protected void runTopDown() {
	}

	/** check if it is possible to skip BU phase */
	protected boolean needBottomUp() {
		return false;
	}

	/** explicitely run BU phase */
	protected void runBottomUp() {
	}

	/** actions that to be done BEFORE entry will be classified */
	protected void preClassificationActions() {
	}

	//--	DFS-based classification
	/** add top entry together with its known subsumers */
	private void addTop(ClassifiableEntry p) {
		waitStack.push(p);
		ksStack.push(p.getToldSubsumers());
	}

	/** remove top entry */
	protected void removeTop() {
		waitStack.pop();
		ksStack.pop();
	}

	/** check if it is necessary to log taxonomy action */
	protected boolean needLogging() {
		return true;
	}

	public Taxonomy(final ClassifiableEntry pTop,
			final ClassifiableEntry pBottom) {
		current = new TaxonomyVertex();
		curEntry = null;
		nEntries = 0;
		nCDEntries = 0;
		useCompletelyDefined = false;
		willInsertIntoTaxonomy = true;
		graph.add(new TaxonomyVertex(pBottom)); // bottom
		graph.add(new TaxonomyVertex(pTop)); // top
	}

	/** special access to TOP of taxonomy */
	public TaxonomyVertex getTopVertex() {
		return graph.get(1);
	}

	/** special access to BOTTOM of taxonomy */
	public TaxonomyVertex getBottomVertex() {
		return graph.get(0);
	}

	//--	classification interface
	// flags interface
	/** set Completely Defined flag */
	public void setCompletelyDefined(boolean use) {
		useCompletelyDefined = use;
	}

	/** call this method after taxonomy is built */
	public void finalise() {
		// create links from leaf concepts to bottom
		final boolean upDirection = false;
		// TODO maybe useful to index Graph
		for (int i = 1; i < graph.size(); i++) {
			TaxonomyVertex p = graph.get(i);
			if (p.noNeighbours(upDirection)) {
				p.addNeighbour(upDirection, getBottomVertex());
				getBottomVertex().addNeighbour(!upDirection, p);
			}
		}
		willInsertIntoTaxonomy = false; // after finalisation one shouldn't add new entries to taxonomy
	}

	private void setupTopDown() {
		setToldSubsumers();
		if (!needTopDown()) {
			++nCDEntries;
			setNonRedundantCandidates();
		}
	}

	public void print(LogAdapter o) {
		o.print(String
				.format("Taxonomy consists of %s entries\n            of which %s are completely defined\n\nAll entries are in format:\n\"entry\" {n: parent_1 ... parent_n} {m: child_1 child_m}\n\n",
						nEntries, nCDEntries));
		for (int i = 1; i < graph.size(); i++) {
			TaxonomyVertex p = graph.get(i);
			p.print(o);
		}
		getBottomVertex().print(o);
	}

	@Override
	public String toString() {
		LogAdapter l = new LeveLogger.LogAdapterStringBuilder();
		print(l);
		return l.toString();
	}

	public void insertCurrent(TaxonomyVertex syn) {
		if (willInsertIntoTaxonomy) {
			// check if current concept is synonym to someone
			if (syn != null) {
				syn.addSynonym(curEntry);
				if (IfDefs.USE_LOGGING) {
					logger.print("\nTAX:set " + curEntry.getName() + " equal "
							+ syn.getPrimer().getName());
				}
			} else {
				// just incorporate it as a special entry and save into Graph
				current.incorporate(curEntry);
				graph.add(current);
				// we used the Current so need to create a new one
				current = new TaxonomyVertex();
			}
		} else // check if node is synonym of existing one and copy EXISTING info to Current
		{
			if (syn != null) {
				curEntry.setTaxVertex(syn);
			} else {
				current.setSample(curEntry);
			}
		}
	}

	private void performClassification() {
		// do something before classification (tunable)
		preClassificationActions();
		++nEntries;
		logger.print("\n\nTAX: start classifying entry ");
		logger.print(curEntry.getName());
		// if no classification needed -- nothing to do
		if (immediatelyClassified()) {
			return;
		}
		// perform main classification
		generalTwoPhaseClassification();
		// create new vertex
		insertCurrent(current.isSynonymNode());
		// clear all labels
		clearLabels();
	}

	private void generalTwoPhaseClassification() {
		setupTopDown();
		if (needTopDown()) {
			getTopVertex().setValued(true, valueLabel);
			getBottomVertex().setValued(false, valueLabel);
			runTopDown();
		}
		clearLabels();
		if (needBottomUp()) {
			getBottomVertex().setValued(true, valueLabel);
			runBottomUp();
		}
		clearLabels();
	}

	protected boolean classifySynonym() {
		final ClassifiableEntry syn = resolveSynonym(curEntry);
		if (syn.equals(curEntry)) {
			return false;
		}
		assert willInsertIntoTaxonomy;
		assert syn.getTaxVertex() != null;
		insertCurrent(syn.getTaxVertex());
		return true;
	}

	private void setNonRedundantCandidates() {
		if (!curEntry.hasToldSubsumers()) {
			logger.print("\nTAX: TOP");
		}
		logger.print(" completely defines concept ");
		logger.print(curEntry.getName());
		for (ClassifiableEntry p : ksStack.peek()) {
			TaxonomyVertex par = p.getTaxVertex();
			boolean stillParent = true;
			for (TaxonomyVertex q : par.neigh(false)) {
				if (q.isValued(valueLabel)) {
					stillParent = false;
					break;
				}
			}
			if (stillParent) {
				current.addNeighbour(true, par);
			}
		}
	}

	private void setToldSubsumers() {
		Collection<ClassifiableEntry> top = ksStack.peek();
		if (needLogging() && !top.isEmpty()) {
			logger.print("\nTAX: told subsumers");
		}
		for (ClassifiableEntry p : top) {
			if (p.isClassified()) {
				if (needLogging()) {
					logger.print(Templates.TOLD_SUBSUMERS, p.getName());
				}
				propagateTrueUp(p.getTaxVertex());
			}
		}
		//XXX this is misleading: in the C++ code the only imple,emtnation available will always say that top is empty here even if it never is.
		//		if (!top.isEmpty() && needLogging()) {
		//			LL.print(" and possibly ");
		//			for (ClassifiableEntry q : top) {
		//				LL.print(Templates.TOLD_SUBSUMERS, q.getName());
		//			}
		//		}
	}

	public void classifyEntry(ClassifiableEntry p) {
		assert waitStack.isEmpty();
		if (p.isNonClassifiable()) {
			return;
		}
		addTop(p);
		while (!waitStack.isEmpty()) {
			if (checkToldSubsumers()) {
				classifyTop();
			} else {
				classifyCycle();
			}
		}
	}

	private boolean checkToldSubsumers() {
		assert !waitStack.isEmpty();
		boolean ret = true;
		for (ClassifiableEntry r : ksStack.peek()) {
			assert r != null;
			if (!r.isClassified()) {
				if (waitStack.contains(r)) {
					addTop(r);
					ret = false;
					break;
				}
				addTop(r);
				ret = checkToldSubsumers();
				break;
			}
		}
		return ret;
	}

	private void classifyTop() {
		assert !waitStack.isEmpty();
		// load last concept
		setCurrentEntry(waitStack.peek());
		if (IfDefs.TMP_PRINT_TAXONOMY_INFO) {
			logger.print("\nTrying classify"
					+ (curEntry.isCompletelyDefined() ? " CD " : " ")
					+ curEntry.getName() + "... ");
		}
		performClassification();
		if (IfDefs.TMP_PRINT_TAXONOMY_INFO) {
			logger.print("done");
		}
		removeTop();
	}

	private void classifyCycle() {
		assert !waitStack.isEmpty();
		ClassifiableEntry p = waitStack.peek();
		classifyTop();
		StringBuilder b = new StringBuilder(
				"\n* Concept definitions cycle found: ");
		b.append(p.getName());
		b.append('\n');
		while (!waitStack.isEmpty()) {
			b.append(", ");
			b.append(waitStack.peek().getName());
			b.append('\n');
			waitStack.peek().setTaxVertex(p.getTaxVertex());
			removeTop();
		}
		throw new ReasonerInternalException(b.toString());
	}

	private void propagateTrueUp(TaxonomyVertex node) {
		// if taxonomy class already checked -- do nothing
		if (node.isValued(valueLabel)) {
			assert node.getValue();
			return;
		}
		// overwise -- value it...
		node.setValued(true, valueLabel);
		// ... and value all parents
		List<TaxonomyVertex> list = node.neigh(/*upDirection=*/true);
		for (int i = 0; i < list.size(); i++) {
			propagateTrueUp(list.get(i));
		}
	}
}