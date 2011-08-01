package uk.ac.manchester.cs.jfact.kernel.state;

/* This file is part of the JFact DL reasoner
 Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
 This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.LinkedList;

public final class SaveList extends LinkedList<DLCompletionTreeSaveState> {
	@Override
	public DLCompletionTreeSaveState pop() {
		if (!isEmpty()) {
			return super.pop();
		}
		return null;
	}

	@Override
	public DLCompletionTreeSaveState peek() {
		return super.peek();
	}

	/** get element from stack with given level */
	public DLCompletionTreeSaveState pop(int level) {
		DLCompletionTreeSaveState p = isEmpty() ? null : peek();
		while (p != null && p.level() > level) {
			pop();
			p = peek();
		}
		// here p==head and either both == NULL or points to proper element
		if (p != null) {
			pop();
		}
		return p != null ? p : null;
	}
}