package datatypes;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;

import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

import datatypes.Datatype.Facet;
import datatypes.Datatype.cardinality;
import datatypes.Datatype.ordered;
import datatypes.Facets.whitespace;

public final class DatatypeFactory {
	static final String namespace = "http://www.w3.org/2001/XMLSchema#";
	static final List<Facet> FACETS2 = Utils.getFacets(Facets.length, Facets.minLength,
			Facets.maxLength, Facets.pattern, Facets.enumeration, Facets.whiteSpace);
	static final List<Facet> FACETS3 = Utils.getFacets(Facets.pattern, Facets.whiteSpace,
			Facets.enumeration, Facets.totalDigits, Facets.fractionDigits,
			Facets.maxInclusive, Facets.maxExclusive, Facets.minInclusive,
			Facets.minExclusive);
	static final List<Facet> FACETS4 = Utils.getFacets(Facets.pattern,
			Facets.enumeration, Facets.whiteSpace, Facets.maxInclusive,
			Facets.maxExclusive, Facets.minInclusive, Facets.minExclusive);
	static final LITERAL_DATATYPE LITERAL = new LITERAL_DATATYPE();
	static final ANYURI_DATATYPE ANYURI = new ANYURI_DATATYPE();
	static final BASE64BINARY_DATATYPE BASE64BINARY = new BASE64BINARY_DATATYPE();
	static final BOOLEAN_DATATYPE BOOLEAN = new BOOLEAN_DATATYPE();
	static final DATETIME_DATATYPE DATETIME = new DATETIME_DATATYPE();
	static final HEXBINARY_DATATYPE HEXBINARY = new HEXBINARY_DATATYPE();
	static final STRING_DATATYPE STRING = new STRING_DATATYPE();
	static final PLAINLITERAL_DATATYPE PLAINLITERAL = new PLAINLITERAL_DATATYPE();
	static final REAL_DATATYPE REAL = new REAL_DATATYPE();
	static final RATIONAL_DATATYPE RATIONAL = new RATIONAL_DATATYPE();
	static final DATETIMESTAMP_DATATYPE DATETIMESTAMP = new DATETIMESTAMP_DATATYPE();
	static final DECIMAL_DATATYPE DECIMAL = new DECIMAL_DATATYPE();
	static final INTEGER_DATATYPE INTEGER = new INTEGER_DATATYPE();
	static final DOUBLE_DATATYPE DOUBLE = new DOUBLE_DATATYPE();
	static final FLOAT_DATATYPE FLOAT = new FLOAT_DATATYPE();
	static final NONPOSITIVEINTEGER_DATATYPE NONPOSITIVEINTEGER = new NONPOSITIVEINTEGER_DATATYPE();
	static final NEGATIVEINTEGER_DATATYPE NEGATIVEINTEGER = new NEGATIVEINTEGER_DATATYPE();
	static final NONNEGATIVEINTEGER_DATATYPE NONNEGATIVEINTEGER = new NONNEGATIVEINTEGER_DATATYPE();
	static final POSITIVEINTEGER_DATATYPE POSITIVEINTEGER = new POSITIVEINTEGER_DATATYPE();
	static final LONG_DATATYPE LONG = new LONG_DATATYPE();
	static final INT_DATATYPE INT = new INT_DATATYPE();
	static final SHORT_DATATYPE SHORT = new SHORT_DATATYPE();
	static final BYTE_DATATYPE BYTE = new BYTE_DATATYPE();
	static final UNSIGNEDLONG_DATATYPE UNSIGNEDLONG = new UNSIGNEDLONG_DATATYPE();
	static final UNSIGNEDINT_DATATYPE UNSIGNEDINT = new UNSIGNEDINT_DATATYPE();
	static final UNSIGNEDSHORT_DATATYPE UNSIGNEDSHORT = new UNSIGNEDSHORT_DATATYPE();
	static final UNSIGNEDBYTE_DATATYPE UNSIGNEDBYTE = new UNSIGNEDBYTE_DATATYPE();
	static final NORMALIZEDSTRING_DATATYPE NORMALIZEDSTRING = new NORMALIZEDSTRING_DATATYPE();
	static final TOKEN_DATATYPE TOKEN = new TOKEN_DATATYPE();
	static final LANGUAGE_DATATYPE LANGUAGE = new LANGUAGE_DATATYPE();
	static final NAME_DATATYPE NAME = new NAME_DATATYPE();
	static final NCNAME_DATATYPE NCNAME = new NCNAME_DATATYPE();
	static final NMTOKEN_DATATYPE NMTOKEN = new NMTOKEN_DATATYPE();
	static final NMTOKENS_DATATYPE NMTOKENS = new NMTOKENS_DATATYPE();
	static final XMLLITERAL_DATATYPE XMLLITERAL = new XMLLITERAL_DATATYPE();
	private static final Datatype[] values = new Datatype[] { ANYURI, BASE64BINARY,
			BOOLEAN, DATETIME, HEXBINARY, LITERAL, PLAINLITERAL, REAL, STRING,
			DATETIMESTAMP, DECIMAL, DOUBLE, FLOAT, BYTE, INT, INTEGER, LONG,
			NEGATIVEINTEGER, NONNEGATIVEINTEGER, NONPOSITIVEINTEGER, POSITIVEINTEGER,
			SHORT, UNSIGNEDBYTE, UNSIGNEDINT, UNSIGNEDLONG, UNSIGNEDSHORT, RATIONAL,
			LANGUAGE, NAME, NCNAME, NMTOKEN, NMTOKENS, NORMALIZEDSTRING, TOKEN,
			XMLLITERAL };
	private final Map<String, Datatype> knownDatatypes = new HashMap<String, Datatype>();

	private DatatypeFactory() {
		for (Datatype d : values) {
			knownDatatypes.put(d.getDatatypeURI(), d);
		}
	}

	public Datatype getKnownDatatype(String key) {
		return knownDatatypes.get(key);
	}

	public boolean isKnownDatatype(String key) {
		return knownDatatypes.containsKey(key);
	}

	public static DatatypeFactory getInstance() {
		return new DatatypeFactory();
	}

	public Datatype defineNewDatatype(final Datatype base, final String uri,
			final Collection<Facet> facets, final Collection<Datatype> ancestors,
			final Map<Facet, Object> knownFacets) {
		return defineNewDatatype(base, uri, facets, ancestors, knownFacets,
				base.getOrdered(), base.getNumeric(), base.getCardinality(),
				base.getBounded());
	}

	public Datatype defineNewDatatype(final Datatype base, final String uri) {
		return defineNewDatatype(base, uri, null, null, null, base.getOrdered(),
				base.getNumeric(), base.getCardinality(), base.getBounded());
	}

	public Datatype defineNewDatatype(final String uri) {
		return defineNewDatatype(LITERAL, uri, null, null, null, LITERAL.getOrdered(),
				LITERAL.getNumeric(), LITERAL.getCardinality(), LITERAL.getBounded());
	}

	public Datatype defineNewDatatype(final Datatype base, final String uri,
			final Collection<Facet> facets, final Collection<Datatype> ancestors,
			final Map<Facet, Object> knownFacets, final ordered ord,
			final boolean numeric, final cardinality card, final boolean bound) {
		if (knownDatatypes.containsKey(uri)) {
			throw new IllegalArgumentException(
					"datatype definitions cannot be overridden: " + uri
							+ " is already in the known types");
		}
		if (base == null) {
			return defineNewDatatype(LITERAL, uri, facets, ancestors, knownFacets, ord,
					numeric, card, bound);
		}
		final List<Facet> f = new ArrayList<Facet>(base.getFacets());
		if (facets != null) {
			f.addAll(facets);
		}
		final Set<Datatype> a = new HashSet<Datatype>(base.getAncestors());
		if (ancestors != null) {
			a.addAll(ancestors);
		}
		a.add(base);
		final Map<Facet, Object> known = new HashMap<Facet, Object>(
				base.getKnownFacetValues());
		if (knownFacets != null) {
			known.putAll(knownFacets);
		}
		Datatype toReturn = new ABSTRACT_DATATYPE() {
			public ordered getOrdered() {
				return ord;
			}

			public boolean getNumeric() {
				return numeric;
			}

			public String getDatatypeURI() {
				return uri;
			}

			public cardinality getCardinality() {
				return card;
			}

			public boolean getBounded() {
				return bound;
			}

			public boolean isInValueSpace(Literal l) {
				return base.isInValueSpace(l);
			}

			public Collection<Literal> listValues(LiteralFactory factory,
					Datatype... datatypes) {
				return base.listValues(factory, datatypes);
			}

			public Object parseValue(String s) {
				return base.parseValue(s);
			}
		};
		knownDatatypes.put(uri, toReturn);
		return toReturn;
	}

	static abstract class ABSTRACT_DATATYPE implements Datatype {
		protected List<Facet> facets;
		protected Set<Datatype> ancestors;
		protected Map<Facet, Object> knownFacetValues;

		public ABSTRACT_DATATYPE() {}

		public Collection<Datatype> getAncestors() {
			return ancestors;
		}

		public Collection<Facet> getFacets() {
			return facets;
		}

		public Map<? extends Facet, ? extends Object> getKnownFacetValues() {
			return knownFacetValues;
		}

		public boolean isSubType(Datatype type) {
			return ancestors.contains(type);
		}

		@Override
		public String toString() {
			return "Datatype[" + getDatatypeURI() + "]";
		}

		public boolean isCompatible(Datatype type) {
			return type instanceof ANYURI_DATATYPE || type.isSubType(this)
					|| this.isSubType(type);
		}

		public boolean isCompatible(Literal l) {
			if (!this.isCompatible(l.getDatatype())) {
				return false;
			}
			// TODO check the value is in the value space
			return isInValueSpace(l);
		}
	}

	static class ANYURI_DATATYPE extends ABSTRACT_DATATYPE implements Datatype {
		//	  <xs:simpleType name="anyURI" id="anyURI">
		//	    <xs:annotation>
		//	      <xs:appinfo>
		//	        <hfp:hasFacet name="length"/>
		//	        <hfp:hasFacet name="minLength"/>
		//	        <hfp:hasFacet name="maxLength"/>
		//	        <hfp:hasFacet name="pattern"/>
		//	        <hfp:hasFacet name="enumeration"/>
		//	        <hfp:hasFacet name="whiteSpace"/>
		//	        <hfp:hasProperty name="ordered" value="false"/>
		//	        <hfp:hasProperty name="bounded" value="false"/>
		//	        <hfp:hasProperty name="cardinality" value="countably infinite"/>
		//	        <hfp:hasProperty name="numeric" value="false"/>
		//	      </xs:appinfo>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#anyURI"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:anySimpleType">
		//	      <xs:whiteSpace fixed="true" value="collapse" id="anyURI.whiteSpace"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		private static final String uri = namespace + "anyURI";

		public String getDatatypeURI() {
			return uri;
		}

		ANYURI_DATATYPE() {
			facets = FACETS2;
			ancestors = Utils.generateAncestors(LITERAL);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(LITERAL.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		public boolean getBounded() {
			return false;
		}

		public cardinality getCardinality() {
			return cardinality.COUNTABLYINFINITE;
		}

		public boolean getNumeric() {
			return false;
		}

		public ordered getOrdered() {
			return ordered.FALSE;
		}

		public Collection<Literal> listValues(LiteralFactory factory,
				Datatype... datatypes) {
			// cannot enumerate ANYURI values
			return Collections.emptyList();
		}

		public Object parseValue(String s) {
			return whitespace.collapse.normalize(s);
		}

		public boolean isInValueSpace(Literal l) {
			try {
				URI.create(l.value());
				return true;
			} catch (IllegalArgumentException e) {
				return false;
			}
		}
	}

	static class BASE64BINARY_DATATYPE extends ABSTRACT_DATATYPE implements Datatype {
		private static final String uri = namespace + "base64Binary";

		public String getDatatypeURI() {
			return uri;
		}

		//	  <xs:simpleType name="base64Binary" id="base64Binary">
		//	    <xs:annotation>
		//	      <xs:appinfo>
		//	        <hfp:hasFacet name="length"/>
		//	        <hfp:hasFacet name="minLength"/>
		//	        <hfp:hasFacet name="maxLength"/>
		//	        <hfp:hasFacet name="pattern"/>
		//	        <hfp:hasFacet name="enumeration"/>
		//	        <hfp:hasFacet name="whiteSpace"/>
		//	        <hfp:hasProperty name="ordered" value="false"/>
		//	        <hfp:hasProperty name="bounded" value="false"/>
		//	        <hfp:hasProperty name="cardinality" value="countably infinite"/>
		//	        <hfp:hasProperty name="numeric" value="false"/>
		//	      </xs:appinfo>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#base64Binary"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:anySimpleType">
		//	      <xs:whiteSpace fixed="true" value="collapse" id="base64Binary.whiteSpace"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		BASE64BINARY_DATATYPE() {
			facets = FACETS2;
			ancestors = Utils.generateAncestors(LITERAL);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(LITERAL.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		public boolean getBounded() {
			return false;
		}

		public cardinality getCardinality() {
			return cardinality.COUNTABLYINFINITE;
		}

		public boolean getNumeric() {
			return false;
		}

		public ordered getOrdered() {
			return ordered.FALSE;
		}

		public Collection<Literal> listValues(LiteralFactory factory,
				Datatype... datatypes) {
			// cannot enumerate Base64 values
			return Collections.emptyList();
		}

		public Object parseValue(String s) {
			return whitespace.collapse.normalize(s);
		}

		public boolean isInValueSpace(Literal l) {
			// all characters are letters, numbers, or +/=
			String s = l.value();
			for (int i = 0; i < s.length(); i++) {
				final char c = s.charAt(i);
				if (!Character.isLetter(c) && !Character.isDigit(c)
						&& "+/=".indexOf(c) == -1) {
					return false;
				}
			}
			return true;
		}
	}

	static class BOOLEAN_DATATYPE extends ABSTRACT_DATATYPE implements Datatype {
		private static final String uri = namespace + "boolean";

		//	 <xs:simpleType name="boolean" id="boolean">
		//	    <xs:annotation>
		//	      <xs:appinfo>
		//	        <hfp:hasFacet name="pattern"/>
		//	        <hfp:hasFacet name="whiteSpace"/>
		//	        <hfp:hasProperty name="ordered" value="false"/>
		//	        <hfp:hasProperty name="bounded" value="false"/>
		//	        <hfp:hasProperty name="cardinality" value="finite"/>
		//	        <hfp:hasProperty name="numeric" value="false"/>
		//	      </xs:appinfo>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#boolean"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:anySimpleType">
		//	      <xs:whiteSpace fixed="true" value="collapse" id="boolean.whiteSpace"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		BOOLEAN_DATATYPE() {
			facets = Utils.getFacets(Facets.pattern, Facets.whiteSpace);
			ancestors = Utils.generateAncestors(LITERAL);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(LITERAL.getKnownFacetValues());
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		public boolean getBounded() {
			//XXX note that the specs says unbounded - yeah right
			return true;
		}

		public cardinality getCardinality() {
			return cardinality.FINITE;
		}

		public boolean getNumeric() {
			return false;
		}

		public ordered getOrdered() {
			return ordered.FALSE;
		}

		public String getDatatypeURI() {
			return uri;
		}

		public Collection<Literal> listValues(LiteralFactory factory,
				Datatype... datatypes) {
			for (Datatype d : datatypes) {
				// if any of the datatypes is not compatible, there is no common intersection
				if (!d.isCompatible(this)) {
					return Collections.emptyList();
				}
			}
			// if all datatypes are compatible, the intersection is the two booleans minu any restriction
			List<Literal> toReturn = new ArrayList<Literal>();
			toReturn.add(factory.parse(this, Boolean.toString(true)));
			toReturn.add(factory.parse(this, Boolean.toString(false)));
			for (Datatype d : datatypes) {
				for (int i = 0; i < toReturn.size();) {
					if (!d.isCompatible(toReturn.get(i))) {
						toReturn.remove(i);
					} else {
						i++;
					}
				}
			}
			return toReturn;
		}

		public Object parseValue(String s) {
			return Boolean.parseBoolean(s);
		}

		public boolean isInValueSpace(Literal l) {
			return l.typedValue() != null;
		}
	}

	static class DATETIME_DATATYPE extends ABSTRACT_DATATYPE implements Datatype {
		private static final String uri = namespace + "dateTime";

		//	  <xs:simpleType name="dateTime" id="dateTime">
		//	    <xs:annotation>
		//	      <xs:appinfo>
		//	        <hfp:hasFacet name="pattern"/>
		//	        <hfp:hasFacet name="enumeration"/>
		//	        <hfp:hasFacet name="whiteSpace"/>
		//	        <hfp:hasFacet name="maxInclusive"/>
		//	        <hfp:hasFacet name="maxExclusive"/>
		//	        <hfp:hasFacet name="minInclusive"/>
		//	        <hfp:hasFacet name="minExclusive"/>
		//	        <hfp:hasProperty name="ordered" value="partial"/>
		//	        <hfp:hasProperty name="bounded" value="false"/>
		//	        <hfp:hasProperty name="cardinality" value="countably infinite"/>
		//	        <hfp:hasProperty name="numeric" value="false"/>
		//	      </xs:appinfo>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#dateTime"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:anySimpleType">
		//	      <xs:whiteSpace fixed="true" value="collapse" id="dateTime.whiteSpace"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		DATETIME_DATATYPE() {
			facets = Utils.getFacets(Facets.pattern, Facets.enumeration,
					Facets.whiteSpace, Facets.maxInclusive, Facets.maxExclusive,
					Facets.minInclusive, Facets.minExclusive);
			ancestors = Utils.generateAncestors(LITERAL);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(LITERAL.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		public boolean getBounded() {
			return false;
		}

		public cardinality getCardinality() {
			return cardinality.COUNTABLYINFINITE;
		}

		public boolean getNumeric() {
			return false;
		}

		public ordered getOrdered() {
			return ordered.PARTIAL;
		}

		public String getDatatypeURI() {
			return uri;
		}

		public Collection<Literal> listValues(LiteralFactory factory,
				Datatype... datatypes) {
			return Collections.emptyList();
		}

		public Object parseValue(String s) {
			XMLGregorianCalendar cal;
			try {
				cal = javax.xml.datatype.DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(s);
				return cal;
			} catch (DatatypeConfigurationException e) {
				throw new ReasonerInternalException(e);
			}
		}

		public boolean isInValueSpace(Literal l) {
			try {
				parseValue(l.value());
			} catch (Throwable e) {
				if (e instanceof Error) {
					throw (Error) e;
				}
				return false;
			}
			return true;
		}
	}

	static class HEXBINARY_DATATYPE extends ABSTRACT_DATATYPE implements Datatype {
		private static final String uri = namespace + "hexBinary";

		//	  <xs:simpleType name="hexBinary" id="hexBinary">
		//	    <xs:annotation>
		//	      <xs:appinfo>
		//	        <hfp:hasFacet name="length"/>
		//	        <hfp:hasFacet name="minLength"/>
		//	        <hfp:hasFacet name="maxLength"/>
		//	        <hfp:hasFacet name="pattern"/>
		//	        <hfp:hasFacet name="enumeration"/>
		//	        <hfp:hasFacet name="whiteSpace"/>
		//	        <hfp:hasProperty name="ordered" value="false"/>
		//	        <hfp:hasProperty name="bounded" value="false"/>
		//	        <hfp:hasProperty name="cardinality" value="countably infinite"/>
		//	        <hfp:hasProperty name="numeric" value="false"/>
		//	      </xs:appinfo>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#binary"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:anySimpleType">
		//	      <xs:whiteSpace fixed="true" value="collapse" id="hexBinary.whiteSpace"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		HEXBINARY_DATATYPE() {
			facets = FACETS2;
			ancestors = Utils.generateAncestors(LITERAL);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(LITERAL.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		public boolean getBounded() {
			return false;
		}

		public cardinality getCardinality() {
			return cardinality.COUNTABLYINFINITE;
		}

		public boolean getNumeric() {
			return false;
		}

		public ordered getOrdered() {
			return ordered.FALSE;
		}

		public String getDatatypeURI() {
			return uri;
		}

		public Collection<Literal> listValues(LiteralFactory factory,
				Datatype... datatypes) {
			// cannot enumerate hexbinary values
			return Collections.emptyList();
		}

		public Object parseValue(String s) {
			return whitespace.collapse.normalize(s);
		}

		public boolean isInValueSpace(Literal l) {
			// all characters are numbers, or ABCDEF
			String s = l.value();
			for (int i = 0; i < s.length(); i++) {
				final char c = s.charAt(i);
				if (!Character.isDigit(c) && "ABCDEF".indexOf(c) == -1) {
					return false;
				}
			}
			return true;
		}
	}

	static class LITERAL_DATATYPE extends ABSTRACT_DATATYPE implements Datatype {
		LITERAL_DATATYPE() {
			ancestors = Collections.emptySet();
			facets = Collections.emptyList();
			knownFacetValues = Collections.emptyMap();
		}

		public boolean getBounded() {
			return false;
		}

		public cardinality getCardinality() {
			return cardinality.COUNTABLYINFINITE;
		}

		public boolean getNumeric() {
			return false;
		}

		public ordered getOrdered() {
			return ordered.FALSE;
		}

		private static final String uri = "http://www.w3.org/2000/01/rdf-schema#Literal";

		public String getDatatypeURI() {
			return uri;
		}

		public Collection<Literal> listValues(LiteralFactory factory,
				Datatype... datatypes) {
			// cannot enumerate Literal values
			return Collections.emptyList();
		}

		public Object parseValue(String s) {
			return s;
		}

		public boolean isInValueSpace(Literal l) {
			return true;
		}
	}

	static class PLAINLITERAL_DATATYPE extends ABSTRACT_DATATYPE implements Datatype {
		PLAINLITERAL_DATATYPE() {
			facets = Utils.getFacets(Facets.length, Facets.minLength, Facets.maxLength,
					Facets.pattern, Facets.enumeration);
			ancestors = Utils.generateAncestors(LITERAL);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(LITERAL.getKnownFacetValues());
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		public boolean getBounded() {
			return false;
		}

		public cardinality getCardinality() {
			return cardinality.COUNTABLYINFINITE;
		}

		public boolean getNumeric() {
			return false;
		}

		public ordered getOrdered() {
			return ordered.FALSE;
		}

		private static final String uri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral";

		public String getDatatypeURI() {
			return uri;
		}

		public Collection<Literal> listValues(LiteralFactory factory,
				Datatype... datatypes) {
			// cannot enumerate plainliteral values
			return Collections.emptyList();
		}

		public boolean isInValueSpace(Literal l) {
			return true;
		}

		public Object parseValue(String s) {
			return s;
		}
	}

	static class REAL_DATATYPE extends ABSTRACT_DATATYPE implements Datatype {
		REAL_DATATYPE() {
			facets = Utils.getFacets(Facets.maxInclusive, Facets.maxExclusive,
					Facets.minInclusive, Facets.minExclusive);
			ancestors = Utils.generateAncestors(LITERAL);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(LITERAL.getKnownFacetValues());
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		public boolean getBounded() {
			return false;
		}

		public cardinality getCardinality() {
			return cardinality.COUNTABLYINFINITE;
		}

		public boolean getNumeric() {
			return true;
		}

		public ordered getOrdered() {
			return ordered.PARTIAL;
		}

		private static final String uri = "http://www.w3.org/2002/07/owl#real";

		public String getDatatypeURI() {
			return uri;
		}

		public Object parseValue(String s) {
			return new BigDecimal(s);
		}

		public boolean isInValueSpace(Literal l) {
			try {
				parseValue(l.value());
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		}

		public Collection<Literal> listValues(LiteralFactory factory,
				Datatype... datatypes) {
			return Collections.emptyList();
		}
	}

	static class STRING_DATATYPE extends ABSTRACT_DATATYPE implements Datatype {
		private static final String uri = namespace + "string";

		//	  <xs:simpleType name="string" id="string">
		//	    <xs:annotation>
		//	      <xs:appinfo>
		//	        <hfp:hasFacet name="length"/>
		//	        <hfp:hasFacet name="minLength"/>
		//	        <hfp:hasFacet name="maxLength"/>
		//	        <hfp:hasFacet name="pattern"/>
		//	        <hfp:hasFacet name="enumeration"/>
		//	        <hfp:hasFacet name="whiteSpace"/>
		//	        <hfp:hasProperty name="ordered" value="false"/>
		//	        <hfp:hasProperty name="bounded" value="false"/>
		//	        <hfp:hasProperty name="cardinality" value="countably infinite"/>
		//	        <hfp:hasProperty name="numeric" value="false"/>
		//	      </xs:appinfo>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#string"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:anySimpleType">
		//	      <xs:whiteSpace value="preserve" id="string.preserve"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		STRING_DATATYPE() {
			facets = FACETS2;
			ancestors = Utils.generateAncestors(LITERAL);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(LITERAL.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.preserve);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		public boolean getBounded() {
			return false;
		}

		public cardinality getCardinality() {
			return cardinality.COUNTABLYINFINITE;
		}

		public boolean getNumeric() {
			return false;
		}

		public ordered getOrdered() {
			return ordered.FALSE;
		}

		public String getDatatypeURI() {
			return uri;
		}

		public Collection<Literal> listValues(LiteralFactory factory,
				Datatype... datatypes) {
			// cannot enumerate String values
			return Collections.emptyList();
		}

		public Object parseValue(String s) {
			return s;
		}

		public boolean isInValueSpace(Literal l) {
			return true;
			//TODO override in subtypes to provide syntax validation
		}
	}

	static class DATETIMESTAMP_DATATYPE extends DATETIME_DATATYPE implements Datatype {
		DATETIMESTAMP_DATATYPE() {
			ancestors = Utils.generateAncestors(DATETIME);
			//TODO check what's required for this
		}

		private static final String uri = namespace + "dateTimeStamp";

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class DECIMAL_DATATYPE extends RATIONAL_DATATYPE implements Datatype {
		private static final String uri = namespace + "decimal";

		//	  <xs:simpleType name="decimal" id="decimal">
		//	    <xs:annotation>
		//	      <xs:appinfo>
		//	        <hfp:hasFacet name="totalDigits"/>
		//	        <hfp:hasFacet name="fractionDigits"/>
		//	        <hfp:hasFacet name="pattern"/>
		//	        <hfp:hasFacet name="whiteSpace"/>
		//	        <hfp:hasFacet name="enumeration"/>
		//	        <hfp:hasFacet name="maxInclusive"/>
		//	        <hfp:hasFacet name="maxExclusive"/>
		//	        <hfp:hasFacet name="minInclusive"/>
		//	        <hfp:hasFacet name="minExclusive"/>
		//	        <hfp:hasProperty name="ordered" value="total"/>
		//	        <hfp:hasProperty name="bounded" value="false"/>
		//	        <hfp:hasProperty name="cardinality" value="countably infinite"/>
		//	        <hfp:hasProperty name="numeric" value="true"/>
		//	      </xs:appinfo>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#decimal"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:anySimpleType">
		//	      <xs:whiteSpace fixed="true" value="collapse" id="decimal.whiteSpace"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		DECIMAL_DATATYPE() {
			facets = Utils.getFacets(Facets.totalDigits, Facets.fractionDigits,
					Facets.pattern, Facets.whiteSpace, Facets.enumeration,
					Facets.maxInclusive, Facets.maxExclusive, Facets.minInclusive,
					Facets.minExclusive);
			ancestors = Utils.generateAncestors(RATIONAL);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public ordered getOrdered() {
			return ordered.TOTAL;
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class DOUBLE_DATATYPE extends ABSTRACT_DATATYPE implements Datatype {
		private static final String uri = namespace + "double";

		//	<xs:simpleType name="double" id="double">
		//    <xs:annotation>
		//      <xs:appinfo>
		//        <hfp:hasFacet name="pattern"/>
		//        <hfp:hasFacet name="enumeration"/>
		//        <hfp:hasFacet name="whiteSpace"/>
		//        <hfp:hasFacet name="maxInclusive"/>
		//        <hfp:hasFacet name="maxExclusive"/>
		//        <hfp:hasFacet name="minInclusive"/>
		//        <hfp:hasFacet name="minExclusive"/>
		//        <hfp:hasProperty name="ordered" value="partial"/>
		//        <hfp:hasProperty name="bounded" value="true"/>
		//        <hfp:hasProperty name="cardinality" value="finite"/>
		//        <hfp:hasProperty name="numeric" value="true"/>
		//      </xs:appinfo>
		//      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#double"/>
		//    </xs:annotation>
		//    <xs:restriction base="xs:anySimpleType">
		//    <xs:whiteSpace fixed="true" value="collapse" id="double.whiteSpace"/>
		//  </xs:restriction>
		//</xs:simpleType>
		DOUBLE_DATATYPE() {
			facets = Utils.getFacets(Facets.pattern, Facets.enumeration,
					Facets.whiteSpace, Facets.maxInclusive, Facets.maxExclusive,
					Facets.minInclusive, Facets.minExclusive);
			ancestors = Utils.generateAncestors(LITERAL);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(LITERAL.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		public boolean getBounded() {
			return true;
		}

		public cardinality getCardinality() {
			return cardinality.FINITE;
		}

		public boolean getNumeric() {
			return true;
		}

		public ordered getOrdered() {
			//XXX really?
			return ordered.PARTIAL;
		}

		public String getDatatypeURI() {
			return uri;
		}

		public Object parseValue(String s) {
			return Double.parseDouble(s);
		}

		public boolean isInValueSpace(Literal l) {
			try {
				parseValue(l.value());
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		}

		public Collection<Literal> listValues(LiteralFactory factory,
				Datatype... datatypes) {
			return Collections.emptyList();
		}
	}

	static class FLOAT_DATATYPE extends ABSTRACT_DATATYPE implements Datatype {
		private static final String uri = namespace + "float";

		//	 <xs:simpleType name="float" id="float">
		//	    <xs:annotation>
		//	      <xs:appinfo>
		//	        <hfp:hasFacet name="pattern"/>
		//	        <hfp:hasFacet name="enumeration"/>
		//	        <hfp:hasFacet name="whiteSpace"/>
		//	        <hfp:hasFacet name="maxInclusive"/>
		//	        <hfp:hasFacet name="maxExclusive"/>
		//	        <hfp:hasFacet name="minInclusive"/>
		//	        <hfp:hasFacet name="minExclusive"/>
		//	        <hfp:hasProperty name="ordered" value="partial"/>
		//	        <hfp:hasProperty name="bounded" value="true"/>
		//	        <hfp:hasProperty name="cardinality" value="finite"/>
		//	        <hfp:hasProperty name="numeric" value="true"/>
		//	      </xs:appinfo>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#float"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:anySimpleType">
		//	      <xs:whiteSpace fixed="true" value="collapse" id="float.whiteSpace"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		FLOAT_DATATYPE() {
			facets = FACETS4;
			ancestors = Utils.generateAncestors(LITERAL);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(LITERAL.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		public boolean getBounded() {
			return true;
		}

		public cardinality getCardinality() {
			return cardinality.FINITE;
		}

		public boolean getNumeric() {
			return true;
		}

		public ordered getOrdered() {
			return ordered.PARTIAL;
		}

		public String getDatatypeURI() {
			return uri;
		}

		public Object parseValue(String s) {
			return Float.parseFloat(s);
		}

		public boolean isInValueSpace(Literal l) {
			try {
				parseValue(l.value());
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		}

		public Collection<Literal> listValues(LiteralFactory factory,
				Datatype... datatypes) {
			return Collections.emptyList();
		}
	}

	static class BYTE_DATATYPE extends SHORT_DATATYPE implements Datatype {
		private static final String uri = namespace + "byte";

		//	  <xs:simpleType name="byte" id="byte">
		//	    <xs:annotation>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#byte"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:short">
		//	      <xs:minInclusive value="-128" id="byte.minInclusive"/>
		//	      <xs:maxInclusive value="127" id="byte.maxInclusive"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		BYTE_DATATYPE() {
			ancestors = Utils.generateAncestors(SHORT);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[\\-+]?[0-9]+");
			map.put(Facets.minInclusive, -128);
			map.put(Facets.maxInclusive, 127);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class INT_DATATYPE extends LONG_DATATYPE implements Datatype {
		private static final String uri = namespace + "int";

		//	  <xs:simpleType name="int" id="int">
		//	    <xs:annotation>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#int"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:long">
		//	      <xs:minInclusive value="-2147483648" id="int.minInclusive"/>
		//	      <xs:maxInclusive value="2147483647" id="int.maxInclusive"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		INT_DATATYPE() {
			ancestors = Utils.generateAncestors(LONG);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[\\-+]?[0-9]+");
			map.put(Facets.minInclusive, -2147483648);
			map.put(Facets.maxInclusive, 2147483647);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class INTEGER_DATATYPE extends DECIMAL_DATATYPE implements Datatype {
		private static final String uri = namespace + "integer";

		//	  <xs:simpleType name="integer" id="integer">
		//	    <xs:annotation>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#integer"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:decimal">
		//	      <xs:fractionDigits fixed="true" value="0" id="integer.fractionDigits"/>
		//	      <xs:pattern value="[\-+]?[0-9]+"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		INTEGER_DATATYPE() {
			facets = FACETS3;
			ancestors = Utils.generateAncestors(DECIMAL);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[\\-+]?[0-9]+");
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class LONG_DATATYPE extends INTEGER_DATATYPE implements Datatype {
		private static final String uri = namespace + "long";

		//	  <xs:simpleType name="long" id="long">
		//	    <xs:annotation>
		//	      <xs:appinfo>
		//	        <hfp:hasProperty name="bounded" value="true"/>
		//	        <hfp:hasProperty name="cardinality" value="finite"/>
		//	      </xs:appinfo>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#long"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:integer">
		//	      <xs:minInclusive value="-9223372036854775808" id="long.minInclusive"/>
		//	      <xs:maxInclusive  value="9223372036854775807" id="long.maxInclusive"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		LONG_DATATYPE() {
			ancestors = Utils.generateAncestors(INTEGER);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[\\-+]?[0-9]+");
			map.put(Facets.minInclusive, -9223372036854775808L);
			map.put(Facets.maxInclusive, 9223372036854775807L);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public boolean getBounded() {
			return true;
		}

		@Override
		public cardinality getCardinality() {
			return cardinality.FINITE;
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class NEGATIVEINTEGER_DATATYPE extends NONPOSITIVEINTEGER_DATATYPE implements
			Datatype {
		private static final String uri = namespace + "negativeInteger";

		//	  <xs:simpleType name="negativeInteger" id="negativeInteger">
		//	    <xs:annotation>
		//	      <xs:documentation
		//	           source="http://www.w3.org/TR/xmlschema-2/#negativeInteger"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:nonPositiveInteger">
		//	      <xs:maxInclusive value="-1" id="negativeInteger.maxInclusive"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		NEGATIVEINTEGER_DATATYPE() {
			ancestors = Utils.generateAncestors(NONPOSITIVEINTEGER);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[\\-+]?[0-9]+");
			map.put(Facets.maxInclusive, -1);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class NONNEGATIVEINTEGER_DATATYPE extends INTEGER_DATATYPE implements Datatype {
		private static final String uri = namespace + "nonNegativeInteger";

		//	  <xs:simpleType name="nonNegativeInteger" id="nonNegativeInteger">
		//	    <xs:annotation>
		//	      <xs:documentation
		//	           source="http://www.w3.org/TR/xmlschema-2/#nonNegativeInteger"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:integer">
		//	      <xs:minInclusive value="0" id="nonNegativeInteger.minInclusive"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		NONNEGATIVEINTEGER_DATATYPE() {
			ancestors = Utils.generateAncestors(INTEGER);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[\\-+]?[0-9]+");
			map.put(Facets.minInclusive, 0);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class NONPOSITIVEINTEGER_DATATYPE extends INTEGER_DATATYPE implements Datatype {
		private static final String uri = namespace + "nonPositiveInteger";

		//	  <xs:simpleType name="nonPositiveInteger" id="nonPositiveInteger">
		//	    <xs:annotation>
		//	      <xs:documentation
		//	           source="http://www.w3.org/TR/xmlschema-2/#nonPositiveInteger"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:integer">
		//	      <xs:maxInclusive value="0" id="nonPositiveInteger.maxInclusive"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		NONPOSITIVEINTEGER_DATATYPE() {
			ancestors = Utils.generateAncestors(INTEGER);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[\\-+]?[0-9]+");
			map.put(Facets.maxInclusive, 0);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class POSITIVEINTEGER_DATATYPE extends NONNEGATIVEINTEGER_DATATYPE implements
			Datatype {
		private static final String uri = namespace + "positiveInteger";

		//	  <xs:simpleType name="positiveInteger" id="positiveInteger">
		//	    <xs:annotation>
		//	      <xs:documentation
		//	           source="http://www.w3.org/TR/xmlschema-2/#positiveInteger"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:nonNegativeInteger">
		//	      <xs:minInclusive value="1" id="positiveInteger.minInclusive"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		POSITIVEINTEGER_DATATYPE() {
			ancestors = Utils.generateAncestors(NONNEGATIVEINTEGER);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[\\-+]?[0-9]+");
			map.put(Facets.minInclusive, 1);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class SHORT_DATATYPE extends INT_DATATYPE implements Datatype {
		private static final String uri = namespace + "short";

		//	  <xs:simpleType name="short" id="short">
		//	    <xs:annotation>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#short"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:int">
		//	      <xs:minInclusive value="-32768" id="short.minInclusive"/>
		//	      <xs:maxInclusive value="32767" id="short.maxInclusive"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		SHORT_DATATYPE() {
			ancestors = Utils.generateAncestors(INT);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[\\-+]?[0-9]+");
			map.put(Facets.minInclusive, -32768);
			map.put(Facets.maxInclusive, 32767);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class UNSIGNEDBYTE_DATATYPE extends UNSIGNEDSHORT_DATATYPE implements Datatype {
		private static final String uri = namespace + "unsignedByte";

		//	  <xs:simpleType name="unsignedByte" id="unsignedByte">
		//	    <xs:annotation>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#unsignedByte"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:unsignedShort">
		//	      <xs:maxInclusive value="255" id="unsignedByte.maxInclusive"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		UNSIGNEDBYTE_DATATYPE() {
			ancestors = Utils.generateAncestors(UNSIGNEDSHORT);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[\\-+]?[0-9]+");
			map.put(Facets.minInclusive, 0);
			map.put(Facets.maxInclusive, 255);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class UNSIGNEDINT_DATATYPE extends UNSIGNEDLONG_DATATYPE implements Datatype {
		private static final String uri = namespace + "unsignedInt";

		//	  <xs:simpleType name="unsignedInt" id="unsignedInt">
		//	    <xs:annotation>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#unsignedInt"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:unsignedLong">
		//	      <xs:maxInclusive value="4294967295" id="unsignedInt.maxInclusive"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		UNSIGNEDINT_DATATYPE() {
			ancestors = Utils.generateAncestors(UNSIGNEDLONG);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[\\-+]?[0-9]+");
			map.put(Facets.minInclusive, 0);
			map.put(Facets.maxInclusive, 4294967295L);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class UNSIGNEDLONG_DATATYPE extends NONNEGATIVEINTEGER_DATATYPE implements
			Datatype {
		private static final String uri = namespace + "unsignedLong";

		//	  <xs:simpleType name="unsignedLong" id="unsignedLong">
		//	    <xs:annotation>
		//	      <xs:appinfo>
		//	        <hfp:hasProperty name="bounded" value="true"/>
		//	        <hfp:hasProperty name="cardinality" value="finite"/>
		//	      </xs:appinfo>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#unsignedLong"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:nonNegativeInteger">
		//	      <xs:maxInclusive value="18446744073709551615"
		//	                       id="unsignedLong.maxInclusive"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		UNSIGNEDLONG_DATATYPE() {
			ancestors = Utils.generateAncestors(NONNEGATIVEINTEGER);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[\\-+]?[0-9]+");
			map.put(Facets.minInclusive, 0);
			map.put(Facets.maxInclusive, new BigInteger("18446744073709551615"));
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class UNSIGNEDSHORT_DATATYPE extends UNSIGNEDINT_DATATYPE implements Datatype {
		private static final String uri = namespace + "unsignedShort";

		//	  <xs:simpleType name="unsignedShort" id="unsignedShort">
		//	    <xs:annotation>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#unsignedShort"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:unsignedInt">
		//	      <xs:maxInclusive value="65535" id="unsignedShort.maxInclusive"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		UNSIGNEDSHORT_DATATYPE() {
			ancestors = Utils.generateAncestors(UNSIGNEDINT);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[\\-+]?[0-9]+");
			map.put(Facets.minInclusive, 0);
			map.put(Facets.maxInclusive, 65535);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class RATIONAL_DATATYPE extends REAL_DATATYPE implements Datatype {
		RATIONAL_DATATYPE() {
			ancestors = Utils.generateAncestors(REAL);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		private static final String uri = "http://www.w3.org/2002/07/owl#rational";

		@Override
		public String getDatatypeURI() {
			return uri;
		}

		@Override
		public Object parseValue(String s) {
			int i = s.indexOf('/');
			if (i == -1) {
				throw new IllegalArgumentException(
						"invalid string used: no '/' character separating longs: " + s);
			}
			double n = Long.parseLong(s.substring(0, i));
			double d = Long.parseLong(s.substring(i + 1));
			BigDecimal b = new BigDecimal(n / d);
			return b;
		}
	}

	static class LANGUAGE_DATATYPE extends TOKEN_DATATYPE implements Datatype {
		private static final String uri = namespace + "language";

		//	  <xs:simpleType name="language" id="language">
		//	    <xs:annotation>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#language"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:token">
		//	      <xs:pattern value="[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*"
		//	                  id="language.pattern">
		//	        <xs:annotation>
		//	          <xs:documentation source="http://www.ietf.org/rfc/rfc3066.txt">
		//	            pattern specifies the content of section 2.12 of XML 1.0e2
		//	            and RFC 3066 (Revised version of RFC 1766).
		//	          </xs:documentation>
		//	        </xs:annotation>
		//	      </xs:pattern>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		LANGUAGE_DATATYPE() {
			ancestors = Utils.generateAncestors(TOKEN);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*");
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class NAME_DATATYPE extends TOKEN_DATATYPE implements Datatype {
		private static final String uri = namespace + "Name";

		//	  <xs:simpleType name="Name" id="Name">
		//	    <xs:annotation>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#Name"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:token">
		//	      <xs:pattern value="\i\c*" id="Name.pattern">
		//	        <xs:annotation>
		//	          <xs:documentation source="http://www.w3.org/TR/REC-xml#NT-Name">
		//	            pattern matches production 5 from the XML spec
		//	          </xs:documentation>
		//	        </xs:annotation>
		//	      </xs:pattern>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		NAME_DATATYPE() {
			ancestors = Utils.generateAncestors(TOKEN);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "\\i\\c*");
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class NCNAME_DATATYPE extends NAME_DATATYPE implements Datatype {
		private static final String uri = namespace + "NCName";

		//	  <xs:simpleType name="NCName" id="NCName">
		//	    <xs:annotation>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#NCName"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:Name">
		//	      <xs:pattern value="[\i-[:]][\c-[:]]*" id="NCName.pattern">
		//	        <xs:annotation>
		//	          <xs:documentation
		//	               source="http://www.w3.org/TR/REC-xml-names/#NT-NCName">
		//	            pattern matches production 4 from the Namespaces in XML spec
		//	          </xs:documentation>
		//	        </xs:annotation>
		//	      </xs:pattern>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		NCNAME_DATATYPE() {
			ancestors = Utils.generateAncestors(NAME);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "[\\i-[:]][\\c-[:]]*");
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class NMTOKEN_DATATYPE extends TOKEN_DATATYPE implements Datatype {
		private static final String uri = namespace + "NMTOKEN";

		//	  <xs:simpleType name="NMTOKEN" id="NMTOKEN">
		//	    <xs:annotation>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#NMTOKEN"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:token">
		//	      <xs:pattern value="\c+" id="NMTOKEN.pattern">
		//	        <xs:annotation>
		//	          <xs:documentation source="http://www.w3.org/TR/REC-xml#NT-Nmtoken">
		//	            pattern matches production 7 from the XML spec
		//	          </xs:documentation>
		//	        </xs:annotation>
		//	      </xs:pattern>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		NMTOKEN_DATATYPE() {
			ancestors = Utils.generateAncestors(TOKEN);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.pattern, "\\c+");
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class NMTOKENS_DATATYPE extends NMTOKEN_DATATYPE implements Datatype {
		private static final String uri = namespace + "NMTOKENS";

		//	  <xs:simpleType name="NMTOKENS" id="NMTOKENS">
		//	    <xs:annotation>
		//	      <xs:appinfo>
		//	        <hfp:hasFacet name="length"/>
		//	        <hfp:hasFacet name="minLength"/>
		//	        <hfp:hasFacet name="maxLength"/>
		//	        <hfp:hasFacet name="enumeration"/>
		//	        <hfp:hasFacet name="whiteSpace"/>
		//	        <hfp:hasFacet name="pattern"/>
		//	        <hfp:hasProperty name="ordered" value="false"/>
		//	        <hfp:hasProperty name="bounded" value="false"/>
		//	        <hfp:hasProperty name="cardinality" value="countably infinite"/>
		//	        <hfp:hasProperty name="numeric" value="false"/>
		//	      </xs:appinfo>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#NMTOKENS"/>
		//	    </xs:annotation>
		//	    <xs:restriction>
		//	      <xs:simpleType>
		//	        <xs:list itemType="xs:NMTOKEN"/>
		//	      </xs:simpleType>
		//	      <xs:minLength value="1" id="NMTOKENS.minLength"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		NMTOKENS_DATATYPE() {
			ancestors = Utils.generateAncestors(NMTOKEN);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			map.put(Facets.minLength, 1);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class NORMALIZEDSTRING_DATATYPE extends STRING_DATATYPE implements Datatype {
		private static final String uri = namespace + "normalizedString";

		//	  <xs:simpleType name="normalizedString" id="normalizedString">
		//	    <xs:annotation>
		//	      <xs:documentation
		//	           source="http://www.w3.org/TR/xmlschema-2/#normalizedString"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:string">
		//	      <xs:whiteSpace value="replace" id="normalizedString.whiteSpace"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		NORMALIZEDSTRING_DATATYPE() {
			ancestors = Utils.generateAncestors(STRING);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.replace);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class TOKEN_DATATYPE extends NORMALIZEDSTRING_DATATYPE implements Datatype {
		private static final String uri = namespace + "token";

		//	  <xs:simpleType name="token" id="token">
		//	    <xs:annotation>
		//	      <xs:documentation source="http://www.w3.org/TR/xmlschema-2/#token"/>
		//	    </xs:annotation>
		//	    <xs:restriction base="xs:normalizedString">
		//	      <xs:whiteSpace value="collapse" id="token.whiteSpace"/>
		//	    </xs:restriction>
		//	  </xs:simpleType>
		TOKEN_DATATYPE() {
			ancestors = Utils.generateAncestors(NORMALIZEDSTRING);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(super.getKnownFacetValues());
			map.put(Facets.whiteSpace, whitespace.collapse);
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		@Override
		public String getDatatypeURI() {
			return uri;
		}
	}

	static class XMLLITERAL_DATATYPE extends ABSTRACT_DATATYPE implements Datatype {
		XMLLITERAL_DATATYPE() {
			facets = Collections.emptyList();
			ancestors = Utils.generateAncestors(LITERAL);
			Map<Facet, Object> map = new HashMap<Datatype.Facet, Object>();
			map.putAll(LITERAL.getKnownFacetValues());
			knownFacetValues = Collections.unmodifiableMap(map);
		}

		public boolean getBounded() {
			return false;
		}

		public cardinality getCardinality() {
			return cardinality.COUNTABLYINFINITE;
		}

		public boolean getNumeric() {
			return false;
		}

		public ordered getOrdered() {
			return ordered.FALSE;
		}

		private static final String uri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";

		public String getDatatypeURI() {
			return uri;
		}

		public Collection<Literal> listValues(LiteralFactory factory,
				Datatype... datatypes) {
			// cannot enumerate xml literal values
			return Collections.emptyList();
		}

		public Object parseValue(String s) {
			// XXX sort of arbitrary decision; the specs say it depends on the XML datatype whitespace normalization policy, but that's not clear. Some W3C tests assume that the policy is collapse
			return whitespace.collapse.normalize(s);
		}

		public boolean isInValueSpace(Literal l) {
			try {
				DocumentBuilderFactory.newInstance().newDocumentBuilder()
						.parse(new ByteArrayInputStream(l.value().getBytes()));
			} catch (Exception e) {
				return false;
			}
			return true;
		}
	}

	public static Datatype ANYURI() {
		return ANYURI;
	}

	public static Datatype BASE64BINARY() {
		return BASE64BINARY;
	}

	public static Datatype BOOLEAN() {
		return BOOLEAN;
	}

	public static Datatype DATETIME() {
		return DATETIME;
	}

	public static Datatype HEXBINARY() {
		return HEXBINARY;
	}

	public static Datatype LITERAL() {
		return LITERAL;
	}

	public static Datatype PLAINLITERAL() {
		return PLAINLITERAL;
	}

	public static Datatype REAL() {
		return REAL;
	}

	public static Datatype STRING() {
		return STRING;
	}

	public static Datatype DATETIMESTAMP() {
		return DATETIMESTAMP;
	}

	public static Datatype DECIMAL() {
		return DECIMAL;
	}

	public static Datatype DOUBLE() {
		return DOUBLE;
	}

	public static Datatype FLOAT() {
		return FLOAT;
	}

	public static Datatype BYTE() {
		return BYTE;
	}

	public static Datatype INT() {
		return INT;
	}

	public static Datatype INTEGER() {
		return INTEGER;
	}

	public static Datatype LONG() {
		return LONG;
	}

	public static Datatype NEGATIVEINTEGER() {
		return NEGATIVEINTEGER;
	}

	public static Datatype NONNEGATIVEINTEGER() {
		return NONNEGATIVEINTEGER;
	}

	public static Datatype NONPOSITIVEINTEGER() {
		return NONPOSITIVEINTEGER;
	}

	public static Datatype POSITIVEINTEGER() {
		return POSITIVEINTEGER;
	}

	public static Datatype SHORT() {
		return SHORT;
	}

	public static Datatype UNSIGNEDBYTE() {
		return UNSIGNEDBYTE;
	}

	public static Datatype UNSIGNEDINT() {
		return UNSIGNEDINT;
	}

	public static Datatype UNSIGNEDLONG() {
		return UNSIGNEDLONG;
	}

	public static Datatype UNSIGNEDSHORT() {
		return UNSIGNEDSHORT;
	}

	public static Datatype RATIONAL() {
		return RATIONAL;
	}

	public static Datatype LANGUAGE() {
		return LANGUAGE;
	}

	public static Datatype NAME() {
		return NAME;
	}

	public static Datatype NCNAME() {
		return NCNAME;
	}

	public static Datatype NMTOKEN() {
		return NMTOKEN;
	}

	public static Datatype NMTOKENS() {
		return NMTOKENS;
	}

	public static Datatype NORMALIZEDSTRING() {
		return NORMALIZEDSTRING;
	}

	public static Datatype TOKEN() {
		return TOKEN;
	}

	public static Datatype XMLLITERAL() {
		return XMLLITERAL;
	}

	public static Datatype[] getValues() {
		return Arrays.copyOf(values, values.length);
	}
}
