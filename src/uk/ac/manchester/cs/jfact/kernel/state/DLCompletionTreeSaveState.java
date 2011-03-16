package uk.ac.manchester.cs.jfact.kernel.state;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
/** class for saving Completion Tree nodes state */
public class DLCompletionTreeSaveState {
	/** saving status of the label */
	protected final SaveState lab;
	/** curLevel of the Node structure */
	protected int curLevel;
	/** amount of neighbours */
	protected int nNeighbours;

	public DLCompletionTreeSaveState() {
		lab = new SaveState();
	}

	/** copy c'tor */
	DLCompletionTreeSaveState(DLCompletionTreeSaveState node) {
		lab = new SaveState(node.lab);
		curLevel = node.curLevel;
		nNeighbours = node.nNeighbours;
	}

	/** get level of a saved node */
	public int level() {
		return curLevel;
	}

	public SaveState getLab() {
		return this.lab;
	}

	public int getCurLevel() {
		return this.curLevel;
	}

	public int getnNeighbours() {
		return this.nNeighbours;
	}

	public void setCurLevel(int curLevel) {
		this.curLevel = curLevel;
	}

	public void setnNeighbours(int nNeighbours) {
		this.nNeighbours = nNeighbours;
	}
}