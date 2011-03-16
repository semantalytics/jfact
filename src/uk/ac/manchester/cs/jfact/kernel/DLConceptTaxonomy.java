package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.LeveLogger.LL;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.Templates;
import uk.ac.manchester.cs.jfact.kernel.TConcept.CTTag;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheInterface;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.ModelCacheState;

public final class DLConceptTaxonomy extends Taxonomy {
	/** host tBox */
	private final TBox tBox;
	/** common descendants of all parents of currently classified concept */
	private final List<TaxonomyVertex> Common = new ArrayList<TaxonomyVertex>();
	// statistic counters
	private long nConcepts;
	private long nTries;
	private long nPositives;
	private long nNegatives;
	private long nSearchCalls;
	private long nSubCalls;
	private long nNonTrivialSubCalls;
	/** number of positive cached subsumptions */
	private long nCachedPositive;
	/** number of negative cached subsumptions */
	private long nCachedNegative;
	/** number of non-subsumptions detected by a sorted reasoning */
	private long nSortedNegative;
	/** indicator of taxonomy creation progress */
	private ReasonerProgressMonitor pTaxProgress;
	// flags
	/** flag to use Bottom-Up search */
	private final boolean flagNeedBottomUp;
	/// number of processed common parents
	protected int nCommon = 1;

	//--	General support for DL concept classification
	/** get access to curEntry as a TConcept */
	private final TConcept curConcept() {
		return (TConcept) curEntry;
	}

	private boolean enhancedSubs(boolean upDirection, TaxonomyVertex cur) {
		++nSubCalls;
		if (cur.isValued(valueLabel)) {
			return cur.getValue();
		} else {
			return cur.setValued(enhancedSubs2(upDirection, cur), valueLabel);
		}
	}

	/** check if told subsumer P have to be classified during current session */
	//@Override
	//	public boolean needToldClassification(ClassifiableEntry p) {
	//		if (useCompletelyDefined && !((TConcept) p).isPrimitive()) {
	//			return false;
	//		}
	//		return true;
	//	}
	/** explicitely run TD phase */
	@Override
	public void runTopDown() {
		searchBaader( /*upDirection=*/false, getTopVertex());
	}

	/** setup BU phase (ie, identify/set children candidates) */
	//	@Override
	//	public void setupBottomUp() {
	//	}
	/** explicitely run BU phase */
	@Override
	public void runBottomUp() {
		try {
			if (propagateUp()) {
				return;
			}
			if (isEqualToTop()) {
				return;
			}
			if (!willInsertIntoTaxonomy) { // after classification -- bottom set up already
				searchBaader( /*upDirection=*/true, getBottomVertex());
				return;
			}
			// during classification -- have to find leaf nodes
			for (TaxonomyVertex p : Common) {
				if (p.noNeighbours(false)) {
					searchBaader(true, p);
				}
			}
		} finally {
			clearCommon();
		}
	}

	/** actions that to be done BEFORE entry will be classified */
	@Override
	public void preClassificationActions() {
		++nConcepts;
		if (pTaxProgress != null) {
			pTaxProgress.reasonerTaskProgressChanged((int) nConcepts, tBox.getNItems());
		}
	}

	/** check if it is necessary to log taxonomy action */
	@Override
	boolean needLogging() {
		return true;
	}

	/** the only c'tor */
	DLConceptTaxonomy(final TConcept pTop, final TConcept pBottom, TBox kb, final TKBFlags GCIs) {
		super(pTop, pBottom);
		tBox = kb;
		nConcepts = 0;
		nTries = 0;
		nPositives = 0;
		nNegatives = 0;
		nSearchCalls = 0;
		nSubCalls = 0;
		nNonTrivialSubCalls = 0;
		nCachedPositive = 0;
		nCachedNegative = 0;
		nSortedNegative = 0;
		pTaxProgress = null;
		flagNeedBottomUp = GCIs.isGCI() || GCIs.isReflexive() && GCIs.isRnD();
	}

	/** set progress indicator */
	void setProgressIndicator(ReasonerProgressMonitor pMon) {
		pTaxProgress = pMon;
	}

	private boolean isUnsatisfiable() {
		final TConcept p = curConcept();
		if (tBox.isSatisfiable(p)) {
			return false;
		}
		insertCurrent(getBottomVertex());
		//		// for unsatisfiable concepts:
		//		if (willInsertIntoTaxonomy) {
		//			// add to BOTTOM
		//			getBottomVertex().addSynonym(p);
		//			//delete Current;
		//			Current = null;
		//		} else {
		//			p.setTaxVertex(getBottomVertex());
		//		}
		return true;
	}

	@Override
	boolean immediatelyClassified() {
		if (classifySynonym()) {
			return true;
		}
		if (curConcept().getClassTagPlain() == CTTag.cttTrueCompletelyDefined) {
			return false;
			// true CD concepts can not be unsat
		}
		// after SAT testing plan would be implemented
		tBox.initCache(curConcept(), false);
		return isUnsatisfiable();
	}

	@Override
	boolean needTopDown() {
		return !(useCompletelyDefined && curEntry.isCompletelyDefined());
	}

	@Override
	boolean needBottomUp() {
		// we DON'T need bottom-up phase for primitive concepts during CD-like reasoning
		// if no GCIs are in the TBox (C [= T, T [= X or Y, X [= D, Y [= D)
		// or no reflexive roles w/RnD precent (Refl(R), Range(R)=D)
		return flagNeedBottomUp || !useCompletelyDefined || curConcept().isNonPrimitive();
	}

	boolean testSub(final TConcept p, final TConcept q) {
		assert p != null;
		assert q != null;
		if (q.isSingleton() // singleton on the RHS is useless iff...
				&& q.isPrimitive() // it is primitive
				&& !q.isNominal()) {
			return false;
		}
		LL.print(Templates.TAX_TRYING, p.getName(), q.getName());
		if (tBox.testSortedNonSubsumption(p, q)) {
			LL.print("NOT holds (sorted result)");
			++nSortedNegative;
			return false;
		}
		switch (tBox.testCachedNonSubsumption(p, q)) {
			case csValid:
				LL.print("NOT holds (cached result)");
				++nCachedNegative;
				return false;
			case csInvalid:
				LL.print("holds (cached result)");
				++nCachedPositive;
				return true;
			default:
				LL.print("wasted cache test");
				break;
		}
		return testSubTBox(p, q);
	}

	/** test subsumption via TBox explicitely */
	boolean testSubTBox(TConcept p, TConcept q) {
		boolean res = tBox.isSubHolds(p, q);
		// update statistic
		++nTries;
		if (res) {
			++nPositives;
		} else {
			++nNegatives;
		}
		return res;
	}

	@Override
	public void print(LogAdapter o) {
		o.print(Templates.DLCONCEPTTAXONOMY, nTries, nPositives, nPositives * 100 / Math.max(1, nTries), nCachedPositive, nCachedNegative, (nSortedNegative > 0 ? String.format("Sorted reasoning deals with %s non-subsumptions\n", nSortedNegative) : ""), nSearchCalls, nSubCalls,
				nNonTrivialSubCalls, nEntries * (nEntries - 1) / Math.max(1, nTries));
		super.print(o);
	}

	private void searchBaader(boolean upDirection, TaxonomyVertex cur) {
		cur.setChecked(checkLabel);
		++nSearchCalls;
		boolean noPosSucc = true;
		List<TaxonomyVertex> neigh = cur.neigh(upDirection);
		int size=neigh.size();
		for (int i=0;i<size;i++) { TaxonomyVertex p = neigh.get(i);
			if (enhancedSubs(upDirection, p)) {
				if (!p.isChecked(checkLabel)) {
					searchBaader(upDirection, p);
				}
				noPosSucc = false;
			}
		}
		// in case current node is unchecked (no BOTTOM node) -- check it explicitely
		if (!cur.isValued(valueLabel)) {
			cur.setValued(testSubsumption(upDirection, cur), valueLabel);
		}
		if (noPosSucc && cur.getValue()) {
			Current.addNeighbour(!upDirection, cur);
		}
	}

	private boolean enhancedSubs1(boolean upDirection, TaxonomyVertex cur) {
		++nNonTrivialSubCalls;
		List<TaxonomyVertex> neigh = cur.neigh(!upDirection);
		int size = neigh.size();
		for (int i=0;i<size;i++) {
			if (!enhancedSubs(upDirection, neigh.get(i))) {
				return false;
			}
		}
		return testSubsumption(upDirection, cur);
	}

	/** short-cuf from ENHANCED_SUBS */
	private boolean enhancedSubs2(boolean upDirection, TaxonomyVertex cur) {
		// if bottom-up search and CUR is not a successor of checking entity -- return false
		if (upDirection && !cur.isCommon()) {
			return false;
		}
		return enhancedSubs1(upDirection, cur);
	}

	boolean testSubsumption(boolean upDirection, TaxonomyVertex cur) {
		final TConcept testC = (TConcept) cur.getPrimer();
		if (upDirection) {
			return testSub(testC, curConcept());
		} else {
			return testSub(curConcept(), testC);
		}
	}

	void propagateOneCommon(TaxonomyVertex node) {
		// checked if node already was visited this session
		if (node.isChecked(checkLabel)) {
			return;
		}
		// mark node visited
		node.setChecked(checkLabel);
		node.setCommon();
		if (node.correctCommon(nCommon)) {
			Common.add(node);
		}
		// mark all children
		List<TaxonomyVertex> neigh = node.neigh(false);
		for (int i = 0; i < neigh.size(); i++) {
			propagateOneCommon(neigh.get(i));
		}
	}

	private boolean propagateUp() {
		final boolean upDirection = true;
		nCommon = 1;
		List<TaxonomyVertex> list = Current.neigh(upDirection);
		assert list.size() > 0; // there is at least one parent (TOP)
		TaxonomyVertex p = list.get(0);
		// define possible successors of the node
		propagateOneCommon(p);
		clearCheckedLabel();
		for (int i = 1; i < list.size(); i++) {
			p = list.get(i);
			if (p.noNeighbours(!upDirection)) {
				return true;
			}
			if (Common.isEmpty()) {
				return true;
			}
			++nCommon;
			List<TaxonomyVertex> aux = new ArrayList<TaxonomyVertex>(Common);
			Common.clear();
			propagateOneCommon(p);
			clearCheckedLabel();
			for (TaxonomyVertex q : aux) {
				q.correctCommon(nCommon);
			}
		}
		return false;
	}

	private void clearCommon() {
		for (TaxonomyVertex p : Common) {
			p.clearCommon();
		}
		Common.clear();
	}

	/// check if no BU classification is required as C=TOP
	boolean isEqualToTop() {
		// check this up-front to avoid Sorted check's flaw wrt equals-to-top
		ModelCacheInterface cache = tBox.initCache(curConcept(), true);
		if (cache.getState() != ModelCacheState.csInvalid)
			return false;
		// here concept = TOP
		Current.addNeighbour(false, getTopVertex());
		return true;
	}

	/** @return true iff curEntry is classified as a synonym */
	@Override
	boolean classifySynonym() {
		if (super.classifySynonym()) {
			return true;
		}
		if (curConcept().isSingleton()) {
			TIndividual curI = (TIndividual) curConcept();
			if (tBox.isBlockedInd(curI)) { // check whether current entry is the same as another individual
				TIndividual syn = tBox.getBlockingInd(curI);
				assert syn.getTaxVertex() != null;
				if (tBox.isBlockingDet(curI)) { // deterministic merge => curI = syn
					insertCurrent(syn.getTaxVertex());
					return true;
				} else // non-det merge: check whether it is the same
				{
					LL.print("\nTAX: trying '" + curI.getName() + "' = '" + syn.getName() + "'... ");
					if (testSubTBox(curI, syn)) // they are actually the same
					{
						insertCurrent(syn.getTaxVertex());
						return true;
					}
				}
			}
		}
		return false;
	}
}