package uk.ac.manchester.cs.jfact.kernel.datatype;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

import uk.ac.manchester.cs.jfact.kernel.dl.DataTypeName;
import uk.ac.manchester.cs.jfact.kernel.voc.Vocabulary;

public enum Datatypes {
	INT {
		@Override
		public Literal<Integer> parse(String s) {
			return new IntRep(Integer.parseInt(s));
		}

		@Override
		public Literal<Integer> build(Object s) {
			return new IntRep((Integer) s);
		}

		@Override
		public boolean compatible(Datatypes d) {
			return super.compatible(d) || EnumSet.of(NEGINT, POSINT, NONNEGINT, NONPOSINT).contains(d);
		}
	},
	SHORT {
		@Override
		public Literal<Short> parse(String s) {
			return new ShortRep(Short.parseShort(s));
		}

		@Override
		public Literal<Short> build(Object s) {
			return new ShortRep((Short) s);
		}
	},
	BYTE {
		@Override
		public Literal<Byte> parse(String s) {
			return new ByteRep(Byte.parseByte(s));
		}

		@Override
		public Literal<Byte> build(Object s) {
			return new ByteRep((Byte) s);
		}

		@Override
		public boolean compatible(Datatypes d) {
			return super.compatible(d) || d == SHORT;
		}
	},
	BOOLEAN {
		@Override
		public Literal<Boolean> parse(String s) {
			return new BoolRep(Boolean.parseBoolean(s));
		}

		@Override
		public Literal<Boolean> build(Object s) {
			return new BoolRep((Boolean) s);
		}
	},
	DOUBLE {
		@Override
		public Literal<Double> parse(String s) {
			return new DoubleRep(Double.parseDouble(s.replace("inf", "Infinity").replace("INF", "Infinity")));
		}

		@Override
		public Literal<Double> build(Object s) {
			return new DoubleRep((Double) s);
		}
	},
	FLOAT {
		@Override
		public Literal<Float> parse(String s) {
			return new FloatRep(Float.parseFloat(s.replace("inf", "Infinity").replace("INF", "Infinity")));
		}

		@Override
		public Literal<Float> build(Object s) {
			return new FloatRep((Float) s);
		}
	},
	STRING {
		@Override
		public Literal<String> parse(String s) {
			return new StringRep(s);
		}

		@Override
		public Literal<String> build(Object s) {
			return new StringRep((String) s);
		}
	},
	LITERAL {
		@Override
		public Literal<?> parse(String s) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Literal<?> build(Object s) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean compatible(Datatypes d) {
			return true;
		}
	},
	DATETIME {
		@Override
		public Literal<XMLGregorianCalendar> parse(String s) {
			XMLGregorianCalendar cal;
			try {
				cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(s);
			} catch (DatatypeConfigurationException e) {
				throw new ReasonerInternalException(e);
			} catch (IllegalArgumentException e) {
				throw new ReasonerInternalException("Error parsing " + s, e);
			}
			return new DateTimeRep(cal);
		}

		@Override
		public Literal<XMLGregorianCalendar> build(Object s) {
			return new DateTimeRep((XMLGregorianCalendar) s);
		}
	},
	POSINT {
		@Override
		public Literal<Integer> parse(String s) {
			return new PosIntRep(Integer.parseInt(s));
		}

		@Override
		public Literal<Integer> build(Object s) {
			return new PosIntRep((Integer) s);
		}
	},
	NEGINT {
		@Override
		public Literal<Integer> parse(String s) {
			return new NegIntRep(Integer.parseInt(s));
		}

		@Override
		public Literal<Integer> build(Object s) {
			return new NegIntRep((Integer) s);
		}
	},
	NONPOSINT {
		@Override
		public Literal<Integer> parse(String s) {
			return new NonPosIntRep(Integer.parseInt(s));
		}

		@Override
		public Literal<Integer> build(Object s) {
			return new NonPosIntRep((Integer) s);
		}

		@Override
		public boolean compatible(Datatypes d) {
			return super.compatible(d) || d == NEGINT;
		}
	},
	NONNEGINT {
		@Override
		public Literal<Integer> parse(String s) {
			return new NonNegIntRep(Integer.parseInt(s));
		}

		@Override
		public Literal<Integer> build(Object s) {
			return new NonNegIntRep((Integer) s);
		}

		@Override
		public boolean compatible(Datatypes d) {
			return super.compatible(d) || d == POSINT;
		}
	},
	REAL {
		@Override
		public Literal<BigDecimal> parse(String s) {
			return new RealRep(new BigDecimal(s));
		}

		@Override
		public Literal<BigDecimal> build(Object s) {
			return new RealRep((BigDecimal) s);
		}

		@Override
		public boolean compatible(Datatypes d) {
			return super.compatible(d) || EnumSet.complementOf(EnumSet.of(STRING, LITERAL, DATETIME, DOUBLE, FLOAT)).contains(d);
		}
	},
	RATIONAL {
		@Override
		public Literal<BigDecimal> parse(String s) {
			return new RationalRep(new BigDecimal(s));
		}

		@Override
		public Literal<BigDecimal> build(Object s) {
			return new RationalRep((BigDecimal) s);
		}

		@Override
		public boolean compatible(Datatypes d) {
			return REAL.compatible(d);
		}
	};
	public abstract Literal<?> parse(String s);

	public abstract Literal<?> build(Object s);

	public DataType getTDataType() {
		return tdatatype;
	}

	public DataTypeName getDataTypeName() {
		return datatypename;
	}

	private final DataTypeName datatypename = new DataTypeName(this);
	private final DataType tdatatype = new DataType(datatypename);

	public boolean compatible(Datatypes d) {
		return d == this;// || d == LITERAL;
	}

	private final static Map<String, Datatypes> datatypeMap = buildDatatypeMap();

	public static Datatypes getBuiltInDataType(String DTName) {
		if (datatypeMap.containsKey(DTName)) {
			return datatypeMap.get(DTName);
		}
		// FIXME!! Data Top
		// FIXME extensions
		throw new ReasonerInternalException("Unsupported datatype " + DTName);
	}

	private static Map<String, Datatypes> buildDatatypeMap() {
		Map<String, Datatypes> toReturn = new HashMap<String, Datatypes>();
		toReturn.put(Vocabulary.LITERAL, STRING);
		toReturn.put(Vocabulary.PLAIN_LITERAL, STRING);
		toReturn.put(Vocabulary.XMLLITERAL, STRING);
		toReturn.put(Vocabulary.STRING, STRING);
		toReturn.put(Vocabulary.ANY_URI, STRING);
		toReturn.put(Vocabulary.INTEGER, INT);
		toReturn.put(Vocabulary.INT, INT);
		toReturn.put(Vocabulary.NON_NEGATIVE_INTEGER, NONNEGINT);
		toReturn.put(Vocabulary.POSITIVE_INTEGER, POSINT);
		toReturn.put(Vocabulary.NEGATIVE_INTEGER, NEGINT);
		toReturn.put(Vocabulary.SHORT, SHORT);
		toReturn.put(Vocabulary.BYTE, BYTE);
		toReturn.put(Vocabulary.UNSIGNEDBYTE, BYTE);
		toReturn.put(Vocabulary.UNSIGNEDINT, INT);
		toReturn.put(Vocabulary.UNSIGNEDSHORT, SHORT);
		toReturn.put(Vocabulary.NONPOSINT, NONPOSINT);
		toReturn.put(Vocabulary.FLOAT, FLOAT);
		toReturn.put(Vocabulary.DOUBLE, DOUBLE);
		toReturn.put(Vocabulary.REAL, REAL);
		toReturn.put(Vocabulary.RATIONAL, FLOAT);
		toReturn.put(Vocabulary.DECIMAL, FLOAT);
		toReturn.put(Vocabulary.BOOLEAN, BOOLEAN);
		toReturn.put(Vocabulary.DATETIME, DATETIME);
		toReturn.put(Vocabulary.DATE, DATETIME);
		return toReturn;
	}
}

class IntRep implements Literal<Integer> {
	protected Integer value;

	public Datatypes getDatatype() {
		return Datatypes.INT;
	}

	public IntRep(Integer v) {
		value = v;
	}

	public int compareTo(Literal<Integer> o) {
		return value.compareTo(o.getValue());
	}

	public Integer getValue() {
		return value;
	}

	public boolean correctMin(boolean excl) {
		if (excl) {
			// transform (n,} into [n+1,}
			value++;
			return false;
		}
		return excl;
	}

	public boolean correctMax(boolean excl) {
		if (excl) {
			// transform (n,} into [n+1,}
			value--;
			return false;
		}
		return excl;
	}

	public boolean lesser(Literal<Integer> other) {
		return this.compareTo(other) < 0;
	}

	@Override
	public String toString() {
		return " " + value.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj.getClass().equals(IntRep.class)) {
			return value.equals(((IntRep) obj).getValue());
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return value.hashCode();
	}
}

class PosIntRep extends IntRep {
	@Override
	public Datatypes getDatatype() {
		return Datatypes.POSINT;
	}

	public PosIntRep(Integer v) {
		super(v);
		if (v.intValue() < 1) {
			throw new IllegalArgumentException("out of range: " + v);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj.getClass().equals(PosIntRep.class)) {
			return value.equals(((PosIntRep) obj).getValue());
		}
		return false;
	}
}

class NegIntRep extends IntRep {
	@Override
	public Datatypes getDatatype() {
		return Datatypes.NEGINT;
	}

	public NegIntRep(Integer v) {
		super(v);
		if (v.intValue() > -1) {
			throw new IllegalArgumentException("out of range: " + v);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj.getClass().equals(NegIntRep.class)) {
			return value.equals(((NegIntRep) obj).getValue());
		}
		return false;
	}
}

class NonPosIntRep extends IntRep {
	@Override
	public Datatypes getDatatype() {
		return Datatypes.NONPOSINT;
	}

	public NonPosIntRep(Integer v) {
		super(v);
		if (v.intValue() > 0) {
			throw new IllegalArgumentException("out of range: " + v);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj.getClass().equals(NonPosIntRep.class)) {
			return value.equals(((NonPosIntRep) obj).getValue());
		}
		return false;
	}
}

class NonNegIntRep extends IntRep {
	@Override
	public Datatypes getDatatype() {
		return Datatypes.NONNEGINT;
	}

	public NonNegIntRep(Integer v) {
		super(v);
		if (v.intValue() < 0) {
			throw new IllegalArgumentException("out of range: " + v);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj.getClass().equals(NonNegIntRep.class)) {
			return value.equals(((NonNegIntRep) obj).getValue());
		}
		return false;
	}
}

class DateTimeRep implements Literal<XMLGregorianCalendar> {
	protected XMLGregorianCalendar value;

	public Datatypes getDatatype() {
		return Datatypes.DATETIME;
	}

	public DateTimeRep(XMLGregorianCalendar v) {
		value = v;
	}

	public int compareTo(Literal<XMLGregorianCalendar> o) {
		return value.compare(o.getValue());
	}

	public XMLGregorianCalendar getValue() {
		return value;
	}

	public boolean correctMin(boolean excl) {
		return excl;
	}

	public boolean correctMax(boolean excl) {
		return excl;
	}

	public boolean lesser(Literal<XMLGregorianCalendar> other) {
		return this.compareTo(other) < 0;
	}

	@Override
	public String toString() {
		return " " + value.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof DateTimeRep) {
			return value.equals(((DateTimeRep) obj).getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}

class ShortRep implements Literal<Short> {
	protected Short value;

	public Datatypes getDatatype() {
		return Datatypes.SHORT;
	}

	public ShortRep(Short v) {
		value = v;
	}

	public int compareTo(Literal<Short> o) {
		return value.compareTo(o.getValue());
	}

	public Short getValue() {
		return value;
	}

	public boolean correctMin(boolean excl) {
		if (excl) {
			// transform (n,} into [n+1,}
			value++;
			return false;
		}
		return excl;
	}

	public boolean correctMax(boolean excl) {
		if (excl) {
			// transform (n,} into [n+1,}
			value--;
			return false;
		}
		return excl;
	}

	public boolean lesser(Literal<Short> other) {
		return this.compareTo(other) < 0;
	}

	@Override
	public String toString() {
		return " " + value.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof ShortRep) {
			return value.equals(((ShortRep) obj).getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}

class ByteRep implements Literal<Byte> {
	protected Byte value;

	public Datatypes getDatatype() {
		return Datatypes.BYTE;
	}

	public ByteRep(Byte v) {
		value = v;
	}

	public int compareTo(Literal<Byte> o) {
		return value.compareTo(o.getValue());
	}

	public Byte getValue() {
		return value;
	}

	public boolean correctMin(boolean excl) {
		if (excl) {
			// transform (n,} into [n+1,}
			value++;
			return false;
		}
		return excl;
	}

	public boolean correctMax(boolean excl) {
		if (excl) {
			// transform (n,} into [n+1,}
			value--;
			return false;
		}
		return excl;
	}

	public boolean lesser(Literal<Byte> other) {
		return this.compareTo(other) < 0;
	}

	@Override
	public String toString() {
		return " " + value.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof ByteRep) {
			return value.equals(((ByteRep) obj).getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}

/// representation of a boolean value
class BoolRep implements Literal<Boolean> {
	/// value of a bool: 0/1
	protected Boolean value;

	public Datatypes getDatatype() {
		return Datatypes.BOOLEAN;
	}

	/// main comparison method; @returns -1 if this < val, 0 if this == val, 1 if this > val
	public int compareTo(Literal<Boolean> val) {
		return value.compareTo(val.getValue());
	}

	public BoolRep(Boolean b) {
		value = b;
	}

	public Boolean getValue() {
		return value;
	}

	public boolean correctMin(boolean excl) {
		return excl;
	}

	public boolean correctMax(boolean excl) {
		return excl;
	}

	public boolean lesser(Literal<Boolean> other) {
		return this.compareTo(other) < 0;
	}

	@Override
	public String toString() {
		return " " + value.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof BoolRep) {
			return value.equals(((BoolRep) obj).getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}

/// representation of a string value
class StringRep implements Literal<String> {
	/// string itself
	protected String value;

	public Datatypes getDatatype() {
		return Datatypes.STRING;
	}

	public int compareTo(Literal<String> val) {
		return value.compareTo(((StringRep) val).value);
	}

	public StringRep(String name) {
		value = name;
	}

	public String getValue() {
		return value;
	}

	public boolean correctMin(boolean excl) {
		return excl;
	}

	public boolean correctMax(boolean excl) {
		return excl;
	}

	public boolean lesser(Literal<String> other) {
		return this.compareTo(other) < 0;
	}

	@Override
	public String toString() {
		return " " + value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof StringRep) {
			return value.equals(((StringRep) obj).getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}

/// representation of a float value
class FloatRep implements Literal<Float> {
	/// float value of a string
	protected Float value;

	public Datatypes getDatatype() {
		return Datatypes.FLOAT;
	}

	public int compareTo(Literal<Float> val) {
		return value.compareTo(val.getValue());
	}

	public FloatRep(Float f) {
		value = f;
	}

	public Float getValue() {
		return value;
	}

	public boolean correctMin(boolean excl) {
		return excl;
	}

	public boolean correctMax(boolean excl) {
		return excl;
	}

	public boolean lesser(Literal<Float> other) {
		return this.compareTo(other) < 0;
	}

	@Override
	public String toString() {
		return " " + value.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof FloatRep) {
			return value.equals(((FloatRep) obj).getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}

/// representation of a double value
class DoubleRep implements Literal<Double> {
	/// double value of a string
	protected Double value;

	public Datatypes getDatatype() {
		return Datatypes.DOUBLE;
	}

	public int compareTo(Literal<Double> val) {
		return value.compareTo(val.getValue());
	}

	public DoubleRep(Double d) {
		value = d;
	}

	public Double getValue() {
		return value;
	}

	public boolean correctMin(boolean excl) {
		return excl;
	}

	public boolean correctMax(boolean excl) {
		return excl;
	}

	public boolean lesser(Literal<Double> other) {
		return this.compareTo(other) < 0;
	}

	@Override
	public String toString() {
		return " " + value.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof DoubleRep) {
			return value.equals(((DoubleRep) obj).getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}

class RealRep implements Literal<BigDecimal> {
	/// double value of a string
	protected BigDecimal value;

	public Datatypes getDatatype() {
		return Datatypes.REAL;
	}

	public int compareTo(Literal<BigDecimal> val) {
		return value.compareTo(val.getValue());
	}

	public RealRep(BigDecimal d) {
		value = d;
	}

	public BigDecimal getValue() {
		return value;
	}

	public boolean correctMin(boolean excl) {
		return excl;
	}

	public boolean correctMax(boolean excl) {
		return excl;
	}

	public boolean lesser(Literal<BigDecimal> other) {
		return this.compareTo(other) < 0;
	}

	@Override
	public String toString() {
		return " " + value.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof RealRep) {
			return value.equals(((RealRep) obj).getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}

class RationalRep implements Literal<BigDecimal> {
	/// double value of a string
	protected BigDecimal value;

	public Datatypes getDatatype() {
		return Datatypes.RATIONAL;
	}

	public int compareTo(Literal<BigDecimal> val) {
		return value.compareTo(val.getValue());
	}

	public RationalRep(BigDecimal d) {
		value = d;
	}

	public RationalRep(String d) {
		this(parse(d));
	}

	private static BigDecimal parse(String s) {
		int i = s.indexOf('/');
		if (i == -1) {
			throw new IllegalArgumentException("invalid string used: no '/' character separating longs: " + s);
		}
		double n = Long.parseLong(s.substring(0, i));
		double d = Long.parseLong(s.substring(i + 1));
		BigDecimal b = new BigDecimal(n / d);
		return b;
	}

	public BigDecimal getValue() {
		return value;
	}

	public boolean correctMin(boolean excl) {
		return excl;
	}

	public boolean correctMax(boolean excl) {
		return excl;
	}

	public boolean lesser(Literal<BigDecimal> other) {
		return this.compareTo(other) < 0;
	}

	@Override
	public String toString() {
		return " " + value.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof RationalRep) {
			return value.equals(((RationalRep) obj).getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}
