package conformance.autogenerated;

import conformance.Factory;
import junit.framework.TestCase;
import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class WebOnt_backwardCompatibleWith_002 extends TestCase {
	public void testWebOnt_backwardCompatibleWith_002() {
		String premise = "<rdf:RDF\n"
				+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
				+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "    xmlns:first=\"http://www.w3.org/2002/03owlt/backwardCompatibleWith/consistent002#\"\n"
				+ "    xml:base=\"http://www.w3.org/2002/03owlt/backwardCompatibleWith/consistent002\" >\n"
				+ "   <rdf:Description>\n"
				+ "      <owl:backwardCompatibleWith>\n"
				+ "        <owl:Ontology rdf:about=\"http://www.example.org/\"/>\n"
				+ "      </owl:backwardCompatibleWith>\n"
				+ "   </rdf:Description>\n" + "\n" + "</rdf:RDF>";
		String conclusion = "";
		String id = "WebOnt_backwardCompatibleWith_002";
		TestClasses tc = TestClasses.valueOf("CONSISTENCY");
		String d = "In OWL Lite and DL the subject and object of a triple with predicate <code>owl:backwardCompatibleWith</code> must both be explicitly typed as <code>owl:Ontology</code>.  In OWL 2, this RDF graph parses to a single ontology with URI http://www.example.org/ and an annotation assertion between a blank node and that URI.";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
		r.setReasonerFactory(Factory.factory());
		r.run();
	}
}