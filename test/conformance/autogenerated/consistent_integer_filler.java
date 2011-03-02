package conformance.autogenerated;

import conformance.Factory;
import junit.framework.TestCase;
import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class consistent_integer_filler extends TestCase {
	public void testconsistent_integer_filler() {
		String premise = "Prefix(:=<http://example.org/>)\n"
				+ "Prefix(xsd:=<http://www.w3.org/2001/XMLSchema#>)\n"
				+ "Ontology(\n" + "  Declaration(NamedIndividual(:a))\n"
				+ "  Declaration(DataProperty(:dp))\n"
				+ "  Declaration(Class(:A))\n"
				+ "  SubClassOf(:A DataHasValue(:dp \"18\"^^xsd:integer)) \n"
				+ "  ClassAssertion(:A :a) \n"
				+ "  ClassAssertion(DataAllValuesFrom(:dp xsd:integer) :a)\n"
				+ ")";
		String conclusion = "";
		String id = "consistent_integer_filler";
		TestClasses tc = TestClasses.valueOf("CONSISTENCY");
		String d = "The individual a is in the extension of the class A, which implies that it has a hasAge filler of 18 as integer, which is consistent with the all values from integer assertion for a.";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
		r.setReasonerFactory(Factory.factory());
		r.run();
	}
}