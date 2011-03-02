package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.cs.jfact.helpers.AccumulatedStatistic;
import uk.ac.manchester.cs.jfact.helpers.IfDefs;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;


public  final  class Stats {
	// statistic elements
	/** all AccumulatedStatistic members are linked together */
	final List<AccumulatedStatistic> root = new ArrayList<AccumulatedStatistic>();
	final AccumulatedStatistic nTacticCalls = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nUseless = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nIdCalls = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nSingletonCalls = AccumulatedStatistic
			.build(root);
	final AccumulatedStatistic nOrCalls = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nOrBrCalls = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nAndCalls = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nSomeCalls = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nAllCalls = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nFuncCalls = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nLeCalls = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nGeCalls = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nNNCalls = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nMergeCalls = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nAutoEmptyLookups = AccumulatedStatistic
			.build(root);
	final AccumulatedStatistic nAutoTransLookups = AccumulatedStatistic
			.build(root);
	final AccumulatedStatistic nSRuleAdd = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nSRuleFire = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nStateSaves = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nStateRestores = AccumulatedStatistic
			.build(root);
	final AccumulatedStatistic nNodeSaves = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nNodeRestores = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nLookups = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nFairnessViolations = AccumulatedStatistic
			.build(root);
	// reasoning cache
	final AccumulatedStatistic nCacheTry = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nCacheFailedNoCache = AccumulatedStatistic
			.build(root);
	final AccumulatedStatistic nCacheFailedShallow = AccumulatedStatistic
			.build(root);
	final AccumulatedStatistic nCacheFailed = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nCachedSat = AccumulatedStatistic.build(root);
	final AccumulatedStatistic nCachedUnsat = AccumulatedStatistic.build(root);

	public void accumulate() {
		AccumulatedStatistic.accumulateAll(root);
	}

	public void logStatisticData(LogAdapter o, boolean needLocal,
			DlCompletionGraph CGraph) {
		if (IfDefs.USE_REASONING_STATISTICS) {
			nTacticCalls.Print(o, needLocal, "\nThere were made ",
					" tactic operations, of which:");
			nIdCalls.Print(o, needLocal, "\n    CN   operations: ", "");
			nSingletonCalls.Print(o, needLocal, "\n           including ",
					" singleton ones");
			nOrCalls.Print(o, needLocal, "\n    OR   operations: ", "");
			nOrBrCalls.Print(o, needLocal, "\n           ",
					" of which are branching");
			nAndCalls.Print(o, needLocal, "\n    AND  operations: ", "");
			nSomeCalls.Print(o, needLocal, "\n    SOME operations: ", "");
			nAllCalls.Print(o, needLocal, "\n    ALL  operations: ", "");
			nFuncCalls.Print(o, needLocal, "\n    Func operations: ", "");
			nLeCalls.Print(o, needLocal, "\n    LE   operations: ", "");
			nGeCalls.Print(o, needLocal, "\n    GE   operations: ", "");
			nUseless.Print(o, needLocal, "\n    N/A  operations: ", "");
			nNNCalls.Print(o, needLocal, "\nThere were made ",
					" NN rule application");
			nMergeCalls.Print(o, needLocal, "\nThere were made ",
					" merging operations");
			nAutoEmptyLookups.Print(o, needLocal, "\nThere were made ",
					" RA empty transition lookups");
			nAutoTransLookups.Print(o, needLocal, "\nThere were made ",
					" RA applicable transition lookups");
			nSRuleAdd.Print(o, needLocal, "\nThere were made ",
					" simple rule additions");
			nSRuleFire.Print(o, needLocal, "\n       of which ",
					" simple rules fired");
			nStateSaves.Print(o, needLocal, "\nThere were made ",
					" save(s) of global state");
			nStateRestores.Print(o, needLocal, "\nThere were made ",
					" restore(s) of global state");
			nNodeSaves.Print(o, needLocal, "\nThere were made ",
					" save(s) of tree state");
			nNodeRestores.Print(o, needLocal, "\nThere were made ",
					" restore(s) of tree state");
			nLookups.Print(o, needLocal, "\nThere were made ",
					" concept lookups");
			if (IfDefs.RKG_USE_FAIRNESS) {
				nFairnessViolations.Print(o, needLocal, "\nThere were ",
						" fairness constraints violation");
			}
			nCacheTry.Print(o, needLocal, "\nThere were made ",
					" tries to cache completion tree node, of which:");
			nCacheFailedNoCache.Print(o, needLocal, "\n                ",
					" fails due to cache absence");
			nCacheFailedShallow.Print(o, needLocal, "\n                ",
					" fails due to shallow node");
			nCacheFailed.Print(o, needLocal, "\n                ",
					" fails due to cache merge failure");
			nCachedSat.Print(o, needLocal, "\n                ",
					" cached satisfiable nodes");
			nCachedUnsat.Print(o, needLocal, "\n                ",
					" cached unsatisfiable nodes");
		}
		if (!needLocal) {
			o.print(String.format("\nThe maximal graph size is %s nodes",
					CGraph.maxSize()));
		}
	}
}
