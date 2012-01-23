package uk.ac.manchester.cs.jfact.datatypes.test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.cs.jfact.datatypes.Datatype;
import uk.ac.manchester.cs.jfact.datatypes.DatatypeExpression;
import uk.ac.manchester.cs.jfact.datatypes.DatatypeFactory;
import uk.ac.manchester.cs.jfact.datatypes.DatatypeIntersection;
import uk.ac.manchester.cs.jfact.datatypes.DatatypeNumericEnumeration;
import uk.ac.manchester.cs.jfact.datatypes.Facets;
import uk.ac.manchester.cs.jfact.datatypes.NumericDatatype;

import junit.framework.TestCase;

public class CheckIntersection extends TestCase {
	public void testIntersection() {
		DatatypeFactory f=DatatypeFactory.getInstance();
		DatatypeNumericEnumeration<BigInteger> d=new DatatypeNumericEnumeration<BigInteger>((NumericDatatype<BigInteger>)DatatypeFactory.INTEGER, DatatypeFactory.INTEGER.buildLiteral("3"));
		DatatypeExpression<BigInteger> e=f.getNumericDatatypeExpression((NumericDatatype<BigInteger>)DatatypeFactory.INTEGER);
		List<Datatype<?>> list=new ArrayList<Datatype<?>>();
		list.add(d);
		list.add(e.addFacet(Facets.minInclusive, "4"));
		DatatypeIntersection intersection=new DatatypeIntersection(DatatypeFactory.INTEGER, list);
		assertTrue(intersection.emptyValueSpace());
	}




}
