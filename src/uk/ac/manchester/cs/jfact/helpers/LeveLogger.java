package uk.ac.manchester.cs.jfact.helpers;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.io.IOException;
import java.io.OutputStream;

public final class LeveLogger {
	public enum Templates {
		TAX_TRYING("\nTAX: trying '%s' [= '%s'... "), INTERVAL(" %s%s"), CLASH(
				" DT-%s"), CHECKCLASH(" DT-TT"), CREATE_EDGE(" ce(%s%s%s,%s)"), IS_BLOCKED_FAILURE_BY(
				" fb(%s,%s)"), LOG_NODE_BLOCKED(" %sb(%s%s%s)"), LOG_SR_NODE(
				" %s(%s[%s],%s)"), DETERMINE_SORTS(
				"\nThere are %s different sorts in TBox\n"), SET_ORDER_DEFAULTS1(
				"orSortSat: initial=%s, default=%s"), SET_ORDER_DEFAULTS2(
				", used=%s\n orSortSub: initial=%s, default=%s"), SET_ORDER_DEFAULTS3(
				", used=%s\n"), WRITE_STATE(
				"\nLoaded KB used DL with following features:\nKB contains %sinverse role(s)\nKB contains %srole hierarchy\nKB contains %stransitive role(s)\nKB contains %squanitifier(s)\nKB contains %sfunctional restriction(s)\nKB contains %snumber restriction(s)\nKB contains %snominal(s)\n"), BUILD_CACHE_UNSAT(
				"\nDAG entry %s is unsatisfiable\n"), CAN_BE_CACHED(" cf(%s)"), CHECK_MERGE_CLASH(
				" x(%s,%s%s)"), COMMON_TACTIC_BODY_OR(" E(%s)"), COMMON_TACTIC_BODY_SOME(
				" nf(%s)"), COMMON_TACTIC_BODY_SOME2(" f(%s):"), CONSISTENT_NOMINAL(
				"\nThe ontology is %s"), DN(" DN(%s%s)"), CN(" cn(%s%s)"), NN(
				" NN(%s)"), E(" E(%s,%s,%s)"), LOG_FINISH_ENTRY(" Clash%s"), SPACE(
				" %s"), DLVERTEXPrint(
				"[d(%s/%s),s(%s/%s),b(%s/%s),g(%s/%s),f(%s/%s)] %s"), DLVERTEXPrint2(
				"(%s) %s %s"), DLVERTEXPrint3(" %s{%s} %s"), DLVERTEXPrint4(
				" %s, %s => %s"), LOGCACHEENTRY("\nConst cache: element %s"), DLCOMPLETIONTREEARC(
				"<%s%s>"), DLCONCEPTTAXONOMY(
				"Totally %s subsumption tests was made\nAmong them %s (%s) successfull\n"
						+ "Besides that %s successfull and %s unsuccessfull subsumption tests were cached\n"
						+ "%sThere were made %s search calls\n"
						+ "There were made %s Sub calls, of which %s non-trivial\nCurrent efficiency (wrt Brute-force) is %s\n"), PRINTDAGUSAGE(
				"There are %s unused DAG entries (% of %s total)\n"), READCONFIG(
				"Init useSemanticBranching = %s\nInit useBackjumping = %s\nInit useLazyBlocking  = %s\nInit useAnywhereBlocking = %s\n"), PRINT_STAT(
				"Heap size = %s nodes\nThere were %s cache hits\n"), REPORT1(
				" cached(%s)"), SAVE(" ss(%s)"), ISSUBHOLDS1(
				"\n----------------------\nChecking subsumption '%s [= %s':\n"), ISSUBHOLDS2(
				"\nThe '%s [= %s' subsumption%s holds w.r.t. TBox"), INCORPORATE(
				"\nTAX:inserting '%s' with up = {"), MERGE(" m(%s->%s)"), RESTORE(
				" sr(%s)"), CLASSIFY_CONCEPTS(
				"\n\n---Start classifying %s concepts"), CLASSIFY_CONCEPTS2(
				"\n---Done: %s %s concepts classified"), READ_CONFIG(
				"Init useCompletelyDefined = %s\nInit useRelevantOnly = %s\nInit dumpQuery = %s\nInit alwaysPreferEquals = %s\nInit usePrecompletion = %s"), TOLD_SUBSUMERS(
				" '%s'"), TRANSFORM_TOLD_CYCLES(
				"\nTold cycle elimination done with %s synonyms created"), IS_SATISFIABLE(
				"\n-----------\nChecking satisfiability of '%s':"), IS_SATISFIABLE1(
				"\nThe '%s' concept is %ssatisfiable w.r.t. TBox");
		private final String template;

		private Templates(String s) {
			template = s;
		}

		public String getTemplate() {
			return template;
		}
	}

	public static boolean isAbsorptionActive() {
		return IfDefs.RKG_DEBUG_ABSORPTION;
	}

	public static interface LogAdapter {
		public void print(Templates t, Object... strings);

		public void println(Templates t, Object... strings);

		public void print(int i);

		public void println(int i);

		public void println();

		public void print(double d);

		public void println(double d);

		public void print(float f);

		public void println(float f);

		public void print(boolean b);

		public void println(boolean b);

		public void print(byte b);

		public void println(byte b);

		public void print(char c);

		public void println(char c);

		public void print(short s);

		public void println(short s);

		public void print(String s);

		public void println(String s);
	}

	@SuppressWarnings("unused")
	public static class LogAdapterImpl implements LogAdapter {
		public void print(Templates t, Object... strings) {
		}

		public void println(Templates t, Object... strings) {
		}

		public void print(int i) {
		}

		public void println(int i) {
		}

		public void print(double d) {
		}

		public void println(double d) {
		}

		public void print(float f) {
		}

		public void println(float f) {
		}

		public void print(boolean b) {
		}

		public void println(boolean b) {
		}

		public void print(byte b) {
		}

		public void println(byte b) {
		}

		public void print(char c) {
		}

		public void println(char c) {
		}

		public void print(short s) {
		}

		public void println(short s) {
		}

		public void print(String s) {
		}

		public void println(String s) {
		}

		public void println() {
		}
	}

	public static class LogAdapterStream implements LogAdapter {
		private final OutputStream out = System.out;

		public void print(Templates t, Object... strings) {
			print(String.format(t.getTemplate(), strings));
		}

		public void println(Templates t, Object... strings) {
			print(String.format(t.getTemplate(), strings));
			println();
		}

		public void print(int i) {
			print(Integer.toString(i));
		}

		public void println(int i) {
			print(Integer.toString(i));
			println();
		}

		public void print(double i) {
			try {
				out.write(Double.toString(i).getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void println(double i) {
			print(i);
			println();
		}

		public void print(float i) {
			try {
				out.write(Float.toString(i).getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void println(float i) {
			print(i);
			println();
		}

		public void print(boolean i) {
			try {
				out.write(Boolean.toString(i).getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void println(boolean i) {
			print(i);
			println();
		}

		public void print(byte i) {
			print(Byte.toString(i));
		}

		public void println(byte i) {
			print(Byte.toString(i));
			println();
		}

		public void print(char i) {
			print(Character.toString(i));
		}

		public void println(char i) {
			print(i);
			println();
		}

		public void print(short i) {
			print(Short.toString(i));
		}

		public void println(short i) {
			print(i);
			println();
		}

		public void print(String i) {
			try {
				out.write(i.getBytes());
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void println(String i) {
			print(i);
			println();
		}

		public void println() {
			print('\n');
		}
	}

	public static class LogAdapterStringBuilder implements LogAdapter {
		StringBuilder b = new StringBuilder();

		public void print(Templates t, Object... strings) {
			b.append(String.format(t.getTemplate(), strings));
		}

		public void println(Templates t, Object... strings) {
			print(t, strings);
			println();
		}

		public void println() {
			b.append('\n');
		}

		public void print(int i) {
			b.append(i);
		}

		public void println(int i) {
			b.append(i);
			println();
		}

		public void print(double i) {
			b.append(i);
		}

		public void println(double i) {
			b.append(i);
			println();
		}

		public void print(float i) {
			b.append(i);
		}

		public void println(float i) {
			b.append(i);
			println();
		}

		public void print(boolean i) {
			b.append(i);
		}

		public void println(boolean i) {
			b.append(i);
			println();
		}

		public void print(byte i) {
			b.append(i);
		}

		public void println(byte i) {
			b.append(i);
			println();
		}

		public void print(char i) {
			b.append(i);
		}

		public void println(char i) {
			b.append(i);
			println();
		}

		public void print(short i) {
			b.append(i);
		}

		public void println(short i) {
			b.append(i);
			println();
		}

		public void print(String i) {
			b.append(i);
		}

		public void println(String i) {
			b.append(i);
			println();
		}

		@Override
		public String toString() {
			return b.toString();
		}
	}

	public static final LogAdapter logger = IfDefs.USE_LOGGING ? new LogAdapterStream()
			: new LogAdapterImpl();
	public static final LogAdapter logger_absorption = IfDefs.RKG_DEBUG_ABSORPTION ? new LogAdapterStream()
			: new LogAdapterImpl();
}