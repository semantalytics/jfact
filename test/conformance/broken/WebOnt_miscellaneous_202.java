package conformance.broken;

import conformance.Factory;
import junit.framework.TestCase;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class WebOnt_miscellaneous_202 extends TestCase {
	public void testWebOnt_miscellaneous_202() {
		String premise = "<rdf:RDF\n"
				+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
				+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "    xmlns=\"http://www.w3.org/1999/xhtml\"\n"
				+ "    xmlns:first=\"http://www.w3.org/2002/03owlt/miscellaneous/consistent202#\"\n"
				+ "    xml:base=\"http://www.w3.org/2002/03owlt/miscellaneous/consistent202\" >\n"
				+ "\n"
				+ "  <owl:Ontology/>\n"
				+ "\n"
				+ "  <owl:DatatypeProperty rdf:ID=\"fp\" />\n"
				+ "  <owl:FunctionalProperty\n"
				+ "           rdf:about=\"#fp\" />\n"
				+ "  <owl:Thing>\n"
				+ "     <first:fp rdf:parseType=\"Literal\"><br />\n"
				+ "<img src=\"vn.png\" alt=\"Venn diagram\" longdesc=\"vn.html\" title=\"Venn\"></img></first:fp>\n"
				+ "     <first:fp rdf:parseType=\"Literal\"><br \n"
				+ "></br>\n" + "<img \n" + "src=\"vn.png\" title=\n"
				+ "\"Venn\" alt\n" + "=\"Venn diagram\" longdesc=\n"
				+ "\"vn.html\" /></first:fp>\n" + "   </owl:Thing>\n"
				+ "</rdf:RDF>";
		String conclusion = "";
		String id = "WebOnt_miscellaneous_202";
		TestClasses tc = TestClasses.valueOf("CONSISTENCY");
		String d = "This shows that insignificant whitespace in an rdf:XMLLiteral is not significant within OWL.";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
		r.setReasonerFactory(Factory.factory());
		r.run();
	}
}