package uk.ac.manchester.cs.jfact.helpers;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import java.util.List;

import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;


public class AccumulatedStatistic {
	/** accumulate all registered statistic elements */
	public static void accumulateAll(List<AccumulatedStatistic> root) {
		for (AccumulatedStatistic cur : root) {
			cur.accumulate();
		}
	}

	/** accumulated statistic */
	private int total;
	/** current session statistic */
	private int local;

	public static AccumulatedStatistic build(List<AccumulatedStatistic> root) {
		AccumulatedStatistic toReturn = new AccumulatedStatistic();
		root.add(toReturn);
		return toReturn;
	}

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

	public void Print(LogAdapter l, boolean b, String s1, String s2) {
		l.print(s1);
		if (b) {
			l.print(local);
		} else {
			l.print(total);
		}
		l.print(s2);
	}
}