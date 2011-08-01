package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
 Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
 This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;

public final class IFOption {
	/** type of an option */
	public static enum IOType {
		iotBool("boolean") {
			@Override
			public Object get(IFOption i) {
				return i.getBool();
			}
		},
		iotInt("integer") {
			@Override
			public Object get(IFOption i) {
				return i.getInt();
			}
		},
		iotText("text") {
			@Override
			public Object get(IFOption i) {
				return i.getText();
			}
		};
		private final String s;

		private IOType(String s) {
			this.s = s;
		}

		@Override
		public String toString() {
			return s;
		}

		public abstract Object get(IFOption i);
	}

	/** option name */
	private final String optionName;
	/** informal descriprion */
	private final String optionDescription;
	/** default value (name of type) */
	private final String defaultValue;
	/** textual value [relevant iff (type == iotText)] */
	private String tValue;
	/** type of value: bool, int or text */
	private final IOType type;
	/** integer value [relevant iff (type == iotInt)] */
	private int iValue;
	/** boolean value [relevant iff (type == iotBool)] */
	private boolean bValue;

	/** get value of a Boolean option */
	public boolean getBool() {
		assert type == IOType.iotBool;
		return bValue;
	}

	/** get value of an integer option */
	public int getInt() {
		assert type == IOType.iotInt;
		return iValue;
	}

	/** get value of a string option */
	public String getText() {
		assert type == IOType.iotText;
		return tValue;
	}

	public IFOption(final String name, final String desc, IOType t, final String defVal) {
		optionName = name;
		optionDescription = desc;
		defaultValue = defVal;
		type = t;
		setAValue(defVal);
	}

	private boolean setAValue(final String s) {
		if (type == IOType.iotBool) {
			if (s.equals("0") || s.equals("off") || s.equals("no") || s.equals("false")) {
				bValue = false;
			} else if (s.equals("1") || s.equals("on") || s.equals("yes")
					|| s.equals("true")) {
				bValue = true;
			} else {
				return true;
			}
		} else if (type == IOType.iotInt) {
			if (!Character.isDigit(s.charAt(0))) {
				return true;
			} else {
				iValue = Integer.parseInt(s);
			}
		} else {
			tValue = s;
		}
		return false;
	}

	public void printConfString(LogAdapter o) {
		o.print(String
				.format("\n;---\n;--- Option '%s': %s ---\n;---\n;* %s\n;* Default value: '%s'\n\n; %s = %s\n",
						optionName, type, optionDescription, defaultValue, optionName,
						type.get(this)));
	}
}