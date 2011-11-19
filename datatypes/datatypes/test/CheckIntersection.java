package datatypes.test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import datatypes.Datatype;
import datatypes.DatatypeExpression;
import datatypes.DatatypeFactory;
import datatypes.DatatypeIntersection;
import datatypes.DatatypeNumericEnumeration;
import datatypes.Facets;
import datatypes.NumericDatatype;

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
