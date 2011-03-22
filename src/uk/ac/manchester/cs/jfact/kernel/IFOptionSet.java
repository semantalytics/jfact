package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.HashMap;
import java.util.Map;

public final class IFOptionSet {
	/** set of all avaliable (given) options */
	private final Map<String, IFOption> base = new HashMap<String, IFOption>();

	/** empty c'tor */
	public IFOptionSet() {
	}

	/**
	 * an option with given name, description, type and default. @return true
	 * iff such option exists
	 */
	public boolean registerOption(final String name, final String desc, IFOption.IOType t, final String defVal) {
		if (locateOption(name) != null) {
			return true;
		}
		base.put(name, new IFOption(name, desc, t, defVal));
		return false;
	}

	/** get Boolean value of given option */
	public boolean getBool(final String optionName) {
		final IFOption p = locateOption(optionName);
		assert p != null;
		return p.getBool();
	}

	/** get string value of given option */
	public String getText(final String optionName) {
		final IFOption p = locateOption(optionName);
		assert p != null;
		return p.getText();
	}

	private IFOption locateOption(final String name) {
		return base.get(name);
	}
}