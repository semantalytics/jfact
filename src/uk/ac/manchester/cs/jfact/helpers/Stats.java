package uk.ac.manchester.cs.jfact.helpers;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.kernel.DlCompletionGraph;

public final class Stats {
	public AccumulatedStatistic build(List<AccumulatedStatistic> root) {
		AccumulatedStatistic toReturn = new AccumulatedStatistic();
		root.add(toReturn);
		return toReturn;
	}

	public final static class AccumulatedStatistic {
		/** accumulated statistic */
		private int total;
		/** current session statistic */
		private int local;

		/** c'tor: link itself to the list */
		private AccumulatedStatistic() {
			total = 0;
			local = 0;
		}

		/** increment local value */
		public void inc() {
			++local;
		}

		/** add local value to a global one */
		private void accumulate() {
			total += local;
			local = 0;
		}

		public void print(LogAdapter l, boolean b, String s1, String s2) {
			l.print(s1);
			if (b) {
				l.print(local);
			} else {
				l.print(total);
			}
			l.print(s2);
		}
	}

	// statistic elements
	/** all AccumulatedStatistic members are linked together */
	private final List<AccumulatedStatistic> root = new ArrayList<AccumulatedStatistic>();
	private final AccumulatedStatistic nTacticCalls = build(root);
	private final AccumulatedStatistic nUseless = build(root);
	private final AccumulatedStatistic nIdCalls = build(root);
	private final AccumulatedStatistic nSingletonCalls = build(root);
	private final AccumulatedStatistic nOrCalls = build(root);
	private final AccumulatedStatistic nOrBrCalls = build(root);
	private final AccumulatedStatistic nAndCalls = build(root);
	private final AccumulatedStatistic nSomeCalls = build(root);
	private final AccumulatedStatistic nAllCalls = build(root);
	private final AccumulatedStatistic nFuncCalls = build(root);
	private final AccumulatedStatistic nLeCalls = build(root);
	private final AccumulatedStatistic nGeCalls = build(root);
	private final AccumulatedStatistic nNNCalls = build(root);
	private final AccumulatedStatistic nMergeCalls = build(root);
	private final AccumulatedStatistic nAutoEmptyLookups = build(root);
	private final AccumulatedStatistic nAutoTransLookups = build(root);
	private final AccumulatedStatistic nSRuleAdd = build(root);
	private final AccumulatedStatistic nSRuleFire = build(root);
	private final AccumulatedStatistic nStateSaves = build(root);
	private final AccumulatedStatistic nStateRestores = build(root);
	private final AccumulatedStatistic nNodeSaves = build(root);
	private final AccumulatedStatistic nNodeRestores = build(root);
	private final AccumulatedStatistic nLookups = build(root);
	private final AccumulatedStatistic nFairnessViolations = build(root);
	// reasoning cache
	private final AccumulatedStatistic nCacheTry = build(root);
	private final AccumulatedStatistic nCacheFailedNoCache = build(root);
	private final AccumulatedStatistic nCacheFailedShallow = build(root);
	private final AccumulatedStatistic nCacheFailed = build(root);
	private final AccumulatedStatistic nCachedSat = build(root);
	private final AccumulatedStatistic nCachedUnsat = build(root);

	public void accumulate() {
		for (AccumulatedStatistic cur : root) {
			cur.accumulate();
		}
	}

	public void logStatisticData(LogAdapter o, boolean needLocal,
			DlCompletionGraph CGraph) {
		if (IfDefs.USE_REASONING_STATISTICS) {
			nTacticCalls.print(o, needLocal, "\nThere were made ",
					" tactic operations, of which:");
			nIdCalls.print(o, needLocal, "\n    CN   operations: ", "");
			nSingletonCalls.print(o, needLocal, "\n           including ",
					" singleton ones");
			nOrCalls.print(o, needLocal, "\n    OR   operations: ", "");
			nOrBrCalls.print(o, needLocal, "\n           ",
					" of which are branching");
			nAndCalls.print(o, needLocal, "\n    AND  operations: ", "");
			nSomeCalls.print(o, needLocal, "\n    SOME operations: ", "");
			nAllCalls.print(o, needLocal, "\n    ALL  operations: ", "");
			nFuncCalls.print(o, needLocal, "\n    Func operations: ", "");
			nLeCalls.print(o, needLocal, "\n    LE   operations: ", "");
			nGeCalls.print(o, needLocal, "\n    GE   operations: ", "");
			nUseless.print(o, needLocal, "\n    N/A  operations: ", "");
			nNNCalls.print(o, needLocal, "\nThere were made ",
					" NN rule application");
			nMergeCalls.print(o, needLocal, "\nThere were made ",
					" merging operations");
			nAutoEmptyLookups.print(o, needLocal, "\nThere were made ",
					" RA empty transition lookups");
			nAutoTransLookups.print(o, needLocal, "\nThere were made ",
					" RA applicable transition lookups");
			nSRuleAdd.print(o, needLocal, "\nThere were made ",
					" simple rule additions");
			nSRuleFire.print(o, needLocal, "\n       of which ",
					" simple rules fired");
			nStateSaves.print(o, needLocal, "\nThere were made ",
					" save(s) of global state");
			nStateRestores.print(o, needLocal, "\nThere were made ",
					" restore(s) of global state");
			nNodeSaves.print(o, needLocal, "\nThere were made ",
					" save(s) of tree state");
			nNodeRestores.print(o, needLocal, "\nThere were made ",
					" restore(s) of tree state");
			nLookups.print(o, needLocal, "\nThere were made ",
					" concept lookups");
			if (IfDefs.RKG_USE_FAIRNESS) {
				nFairnessViolations.print(o, needLocal, "\nThere were ",
						" fairness constraints violation");
			}
			nCacheTry.print(o, needLocal, "\nThere were made ",
					" tries to cache completion tree node, of which:");
			nCacheFailedNoCache.print(o, needLocal, "\n                ",
					" fails due to cache absence");
			nCacheFailedShallow.print(o, needLocal, "\n                ",
					" fails due to shallow node");
			nCacheFailed.print(o, needLocal, "\n                ",
					" fails due to cache merge failure");
			nCachedSat.print(o, needLocal, "\n                ",
					" cached satisfiable nodes");
			nCachedUnsat.print(o, needLocal, "\n                ",
					" cached unsatisfiable nodes");
		}
		if (!needLocal) {
			o.print(String.format("\nThe maximal graph size is %s nodes",
					CGraph.maxSize()));
		}
	}

	public AccumulatedStatistic getnTacticCalls() {
		return this.nTacticCalls;
	}

	public AccumulatedStatistic getnUseless() {
		return this.nUseless;
	}

	public AccumulatedStatistic getnIdCalls() {
		return this.nIdCalls;
	}

	public AccumulatedStatistic getnSingletonCalls() {
		return this.nSingletonCalls;
	}

	public AccumulatedStatistic getnOrCalls() {
		return this.nOrCalls;
	}

	public AccumulatedStatistic getnOrBrCalls() {
		return this.nOrBrCalls;
	}

	public AccumulatedStatistic getnAndCalls() {
		return this.nAndCalls;
	}

	public AccumulatedStatistic getnSomeCalls() {
		return this.nSomeCalls;
	}

	public AccumulatedStatistic getnAllCalls() {
		return this.nAllCalls;
	}

	public AccumulatedStatistic getnFuncCalls() {
		return this.nFuncCalls;
	}

	public AccumulatedStatistic getnLeCalls() {
		return this.nLeCalls;
	}

	public AccumulatedStatistic getnGeCalls() {
		return this.nGeCalls;
	}

	public AccumulatedStatistic getnNNCalls() {
		return this.nNNCalls;
	}

	public AccumulatedStatistic getnMergeCalls() {
		return this.nMergeCalls;
	}

	public AccumulatedStatistic getnAutoEmptyLookups() {
		return this.nAutoEmptyLookups;
	}

	public AccumulatedStatistic getnAutoTransLookups() {
		return this.nAutoTransLookups;
	}

	public AccumulatedStatistic getnSRuleAdd() {
		return this.nSRuleAdd;
	}

	public AccumulatedStatistic getnSRuleFire() {
		return this.nSRuleFire;
	}

	public AccumulatedStatistic getnStateSaves() {
		return this.nStateSaves;
	}

	public AccumulatedStatistic getnStateRestores() {
		return this.nStateRestores;
	}

	public AccumulatedStatistic getnNodeSaves() {
		return this.nNodeSaves;
	}

	public AccumulatedStatistic getnNodeRestores() {
		return this.nNodeRestores;
	}

	public AccumulatedStatistic getnLookups() {
		return this.nLookups;
	}

	public AccumulatedStatistic getnFairnessViolations() {
		return this.nFairnessViolations;
	}

	public AccumulatedStatistic getnCacheTry() {
		return this.nCacheTry;
	}

	public AccumulatedStatistic getnCacheFailedNoCache() {
		return this.nCacheFailedNoCache;
	}

	public AccumulatedStatistic getnCacheFailedShallow() {
		return this.nCacheFailedShallow;
	}

	public AccumulatedStatistic getnCacheFailed() {
		return this.nCacheFailed;
	}

	public AccumulatedStatistic getnCachedSat() {
		return this.nCachedSat;
	}

	public AccumulatedStatistic getnCachedUnsat() {
		return this.nCachedUnsat;
	}
}
