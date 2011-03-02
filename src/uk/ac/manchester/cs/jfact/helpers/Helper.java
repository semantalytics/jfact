package uk.ac.manchester.cs.jfact.helpers;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import java.util.List;

public final class Helper {
	public static void resize(List<?> l, int n) {
		if (l.size() > n) {
			while (l.size() > n) {
				l.remove(l.size() - 1);
			}
		} else {
			while (l.size() < n) {
				l.add(null);
			}
		}
	}

	public static <T> void resize(List<T> l, int n, T filler) {
		if (l.size() > n) {
			while (l.size() > n) {
				l.remove(l.size() - 1);
			}
		} else {
			while (l.size() < n) {
				l.add(filler);
			}
		}
	}

	// uncomment this to have a DAG usage statistics printed
	////#define RKG_PRINT_DAG_USAGE
	// uncomment this to have a DIG-passed information printed
	//#define RKG_PRINT_DIG_MESSAGES
	// uncomment this to have sorted ontology reasoning
	//#define RKG_USE_SORTED_REASONING
	// uncomment this to have absorption debug messages
	////#define RKG_DEBUG_ABSORPTION
	// uncomment this to allow dynamic backjumping
	////#define RKG_USE_DYNAMIC_BACKJUMPING
	//#ifdef RKG_USE_DYNAMIC_BACKJUMPING
	// uncomment this to use improves S/R with better quality
	//#	define RKG_IMPROVE_SAVE_RESTORE_DEPSET
	//#endif
	// uncomment this to update role's R&D from super-roles
	////#define RKG_UPDATE_RND_FROM_SUPERROLES
	// uncomment this to allow simple rules processing
	////#define RKG_USE_SIMPLE_RULES
	// uncomment this to support fairness constraints
	////#define RKG_USE_FAIRNESS
	// uncomment the following line if IR is defined as a list of elements in node label
	//#define RKG_IR_IN_NODE_LABEL
	public static final int InitBranchingLevelValue = 1;
	//	public static void fpp_assert(boolean b) {
	//		if (IfDefs.ENABLE_CHECKING && !b) {
	//			throw new RuntimeException("Unsatisfied assertion!");
	//		}
	//	}
	public static final int bpINVALID = 0;
	public static final int bpTOP = 1;
	public static final int bpBOTTOM = -1;

	public static int createBiPointer(int index, boolean pos) {
		return pos ? index : -index;
	}

	public static boolean isCorrect(int p) {
		return p != bpINVALID;
	}

	public static final boolean isValid(int p) {
		return p != bpINVALID;
	}
	//	/** it is valid if it's either not a bpINVALID or is a basic datatype*/
	//	public static boolean isValid(TDataEntry p) {
	//		return p.getBP() != bpINVALID||p.isBasicDataType();
	//	}
	//	public static boolean isPositive(int p) {
	//		return p > 0;
	//	}
	//	public static boolean isNegative(int p) {
	//		return p < 0;
	//	}
	//	public static int getValue(int p) {
	//		return p > 0 ? p : -p;
	//	}
	//	public static int inverse(int p) {
	//		return -p;
	//	}
}
