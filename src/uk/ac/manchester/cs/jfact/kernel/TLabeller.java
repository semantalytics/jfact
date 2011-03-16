package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
/**
 * define class that implements support for labelling entries with cheap
 * 'unselect' operation. An external entity is 'marked' iff it's value equal to
 * the internal counter.
 */
public class TLabeller {
	/** counter */
	private long counter;

	public TLabeller() {
		counter = 1;
	}

	/** create a new label value */
	public void newLabel() {
		counter++;
	}

	/** set given label's value to the counter's one */
	public long getLabel() {
		/** XXX set in the caller */
		return counter;
	}

	/** check if given label is labelled */
	public boolean isLabelled(long lab) {
		return lab == counter;
	}
}