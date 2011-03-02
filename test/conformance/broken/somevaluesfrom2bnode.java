package conformance.broken;

import conformance.Factory;
import junit.framework.TestCase;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class somevaluesfrom2bnode extends TestCase {
	public void testsomevaluesfrom2bnode() {
		String premise = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"  \n"
				+ "          xmlns:ex=\"http://example.org/\"\n"
				+ "          xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "          xml:base=\"http://example.org/\">\n"
				+ "  <owl:Ontology />\n"
				+ "  <owl:ObjectProperty rdf:about=\"p\"/>\n"
				+ "  <rdf:Description rdf:about=\"a\">\n"
				+ "        <rdf:type>\n"
				+ "            <owl:Restriction>\n"
				+ "                <owl:onProperty rdf:resource=\"p\"/>\n"
				+ "                <owl:someValuesFrom rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>\n"
				+ "            </owl:Restriction>\n"
				+ "        </rdf:type>\n"
				+ "    </rdf:Description>\n" + "</rdf:RDF>";
		String conclusion = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"  \n"
				+ "          xmlns:ex=\"http://example.org/\"\n"
				+ "          xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "          xml:base=\"http://example.org/\">\n"
				+ "  <owl:Ontology />\n"
				+ "  <owl:ObjectProperty rdf:about=\"p\"/>\n"
				+ "  <rdf:Description rdf:about=\"a\">\n"
				+ "    <ex:p><rdf:Description/></ex:p> \n"
				+ "  </rdf:Description>\n" + "</rdf:RDF>";
		String id = "somevaluesfrom2bnode";
		TestClasses tc = TestClasses.valueOf("POSITIVE_IMPL");
		String d = "Shows that a BNode is an existential variable.";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
		r.setReasonerFactory(Factory.factory());
		r.run();
	}
}