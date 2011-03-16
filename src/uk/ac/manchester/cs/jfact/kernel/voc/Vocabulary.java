package uk.ac.manchester.cs.jfact.kernel.voc;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
public class Vocabulary {
	public final static String OWL_NAMESPACE = "http://www.w3.org/2002/07/owl#";
	public final static String TOP_OBJECT_PROPERTY = OWL_NAMESPACE + "topObjectProperty";
	public final static String BOTTOM_OBJECT_PROPERTY = OWL_NAMESPACE + "bottomObjectProperty";
	public final static String TOP_DATA_PROPERTY = OWL_NAMESPACE + "topDataProperty";
	public final static String BOTTOM_DATA_PROPERTY = OWL_NAMESPACE + "bottomDataProperty";
	public final static String LITERAL = "http://www.w3.org/2000/01/rdf-schema#Literal";
	public final static String PLAIN_LITERAL = "http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral";
	public final static String XMLLITERAL = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";
	public final static String STRING = "http://www.w3.org/2001/XMLSchema#string";
	public final static String ANY_URI = "http://www.w3.org/2001/XMLSchema#anyURI";
	public final static String INTEGER = "http://www.w3.org/2001/XMLSchema#integer";
	public final static String INT = "http://www.w3.org/2001/XMLSchema#int";
	public final static String NON_NEGATIVE_INTEGER = "http://www.w3.org/2001/XMLSchema#nonNegativeInteger";
	public final static String POSITIVE_INTEGER = "http://www.w3.org/2001/XMLSchema#positiveInteger";
	public final static String NEGATIVE_INTEGER = "http://www.w3.org/2001/XMLSchema#negativeInteger";
	public final static String SHORT = "http://www.w3.org/2001/XMLSchema#short";
	public final static String BYTE = "http://www.w3.org/2001/XMLSchema#byte";
	public final static String FLOAT = "http://www.w3.org/2001/XMLSchema#float";
	public final static String DOUBLE = "http://www.w3.org/2001/XMLSchema#double";
	public final static String REAL = "http://www.w3.org/2002/07/owl#real";
	public final static String RATIONAL = "http://www.w3.org/2002/07/owl#rational";
	public final static String DECIMAL = "http://www.w3.org/2001/XMLSchema#decimal";
	public final static String BOOLEAN = "http://www.w3.org/2001/XMLSchema#boolean";
	public final static String UNSIGNEDBYTE = "http://www.w3.org/2001/XMLSchema#unsignedByte";
	public final static String UNSIGNEDSHORT = "http://www.w3.org/2001/XMLSchema#unsignedShort";
	public final static String UNSIGNEDINT = "http://www.w3.org/2001/XMLSchema#unsignedInt";
	public final static String NONPOSINT = "http://www.w3.org/2001/XMLSchema#nonPositiveInteger";
	public final static String DATETIME = "http://www.w3.org/2001/XMLSchema#dateTime";
	public final static String DATE = "http://www.w3.org/2001/XMLSchema#date";
	//	/** get name of the default string datatype */
	//	public static final String getStrTypeName() {
	//		return "http://www.w3.org/2001/XMLSchema//#string";
	//	}
	//
	//	/** get name of the default integer datatype */
	//	public static final String getIntTypeName() {
	//		return "http://www.w3.org/2001/XMLSchema//#integer";
	//	}
	//
	//	/** get name of the default floating point datatype */
	//	public static final String getRealTypeName() {
	//		return "http://www.w3.org/2001/XMLSchema//#float";
	//	}
	//
	//	/** get name of the default boolean datatype */
	//	public static final String getBoolTypeName() {
	//		return "http://www.w3.org/2001/XMLSchema//#boolean";
	//	}
}
