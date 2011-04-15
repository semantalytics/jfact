package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.Helper.*;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import uk.ac.manchester.cs.jfact.helpers.ArrayIntMap;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.kernel.state.SaveState;

public final class CGLabel {
	private static int idcounter = 0;

	private static int getnewId() {
		return idcounter++;
	}

	/** all simple concepts, labelled a node */
	private final CWDArray scLabel;
	/** all complex concepts (ie, FORALL, GE), labelled a node */
	private final CWDArray ccLabel;
	private final int id;

	public CGLabel() {
		scLabel = new CWDArray();
		ccLabel = new CWDArray();
		id = getnewId();
	}

	public List<ConceptWDep> get_sc() {
		return scLabel.getBase();
	}

	public List<ConceptWDep> get_cc() {
		return ccLabel.getBase();
	}

	public ArrayIntMap get_sc_concepts() {
		return scLabel.getContainedConcepts();
	}

	public ArrayIntMap get_cc_concepts() {
		return ccLabel.getContainedConcepts();
	}

	/** get (RW) label associated with the concepts defined by TAG */
	public CWDArray getLabel(DagTag tag) {
		return tag.isComplexConcept() ? ccLabel : scLabel;
	}

	public void add(DagTag tag, ConceptWDep p) {
		getLabel(tag).private_add(p);
		clearMyCache();
	}

	//	protected final void _clearCache() {
	//		for (CGLabel c : lesserEquals) {
	//			c.lesserEquals.remove(this);
	//		//	c.notLesserEquals.remove(this);
	//		}
	//		//for (CGLabel c : notLesserEquals) {
	//		//	c.lesserEquals.remove(this);
	//		//	c.notLesserEquals.remove(this);
	//		//}
	//		lesserEquals.clear();
	//		//notLesserEquals.clear();
	//	}
	protected final void clearMyCache() {
		lesserEquals.clear();
	}

	protected final void clearOthersCache() {
		for (CGLabel c : lesserEquals) {
			c.lesserEquals.remove(this);
		}
	}

	/** check whether node is labelled by complex concept P */
	public boolean containsCC(int p) {
		return ccLabel.contains(p);
	}

	@Override
	public int hashCode() {
		return id;
	}

	private final Set<CGLabel> lesserEquals = Collections
			.newSetFromMap(new IdentityHashMap<CGLabel, Boolean>());

	//	private final Set<CGLabel> notLesserEquals = Collections
	//			.newSetFromMap(new IdentityHashMap<CGLabel, Boolean>());
	//	static int hit=0;
	//	static int miss=0;
	public boolean lesserequal(final CGLabel label) {
		if (this == label) {
			return true;
		}
		if (lesserEquals.contains(label)) {
			//			hit++;
			//			if(hit%1000==0||miss%1000==0) {
			//				System.out.println("CGLabel.lesserequal() "+hit+"\t"+miss);
			//			}
			return true;
		}
		//		if (notLesserEquals.contains(label)) {
		//			miss++;
		//			if(hit%1000==0||miss%1000==0) {
		//				System.out.println("CGLabel.lesserequal() "+hit+"\t"+miss);
		//			}
		//			return false;
		//		}
		boolean toReturn = scLabel.lesserequal(label.scLabel)
				&& ccLabel.lesserequal(label.ccLabel);
		if (toReturn) {
			lesserEquals.add(label);
			//		} else {
			//			notLesserEquals.add(label);
		}
		return toReturn;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof CGLabel) {
			CGLabel obj2 = (CGLabel) obj;
			boolean toReturn = scLabel.equals(obj2.scLabel)
					&& ccLabel.equals(obj2.ccLabel);
			return toReturn;
		}
		return false;
	}

	/** save label using given SS */
	public void save(SaveState ss) {
		ss.setSc(scLabel.save());
		ss.setCc(ccLabel.save());
	}

	/** restore label to given LEVEL using given SS */
	public final void restore(final SaveState ss, int level) {
		scLabel.restore(ss.getSc(), level);
		ccLabel.restore(ss.getCc(), level);
		//_clearCache();
		clearOthersCache();
	}

	/** print the whole label */
	public void print(LeveLogger.LogAdapter o) {
		scLabel.print(o);
		ccLabel.print(o);
	}

	@Override
	public String toString() {
		LogAdapter l = new LeveLogger.LogAdapterStringBuilder();
		print(l);
		return l.toString();
	}

	public final void init() {
		//_clearCache();
		clearOthersCache();
		clearMyCache();
		scLabel.init();
		ccLabel.init();
	}

	public boolean contains(int p) {
		assert isCorrect(p);
		if (p == bpTOP) {
			return true;
		}
		if (p == bpBOTTOM) {
			return false;
		}
		boolean b = scLabel.contains(p) || ccLabel.contains(p);
		return b;
	}

	public int baseSize() {
		return ccLabel.size() + scLabel.size();
	}

	public int getId() {
		return id;
	}
}
