package conformance.autogenerated;

import junit.framework.TestCase;
import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class WebOnt_Ontology_001 extends TestCase {
	public void testWebOnt_Ontology_001() {
		String premise = "<rdf:RDF\n"
				+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
				+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "    xmlns:first=\"http://www.w3.org/2002/03owlt/Ontology/premises001#\"\n"
				+ "    xml:base=\"http://www.w3.org/2002/03owlt/Ontology/premises001\" >\n"
				+ "   <owl:Ontology rdf:about=\"\" />\n"
				+ "   <owl:Class rdf:ID=\"Car\">\n"
				+ "     <owl:equivalentClass>\n"
				+ "       <owl:Class rdf:ID=\"Automobile\"/>\n"
				+ "     </owl:equivalentClass>\n"
				+ "  </owl:Class>\n"
				+ "  <first:Car rdf:ID=\"car\">\n"
				+ "     <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\" />\n"
				+ "  </first:Car>\n"
				+ "  <first:Automobile rdf:ID=\"auto\">\n"
				+ "     <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\" />\n"
				+ "  </first:Automobile>\n" + "</rdf:RDF>";
		String conclusion = "<rdf:RDF\n"
				+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
				+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "    xmlns:first=\"http://www.w3.org/2002/03owlt/Ontology/premises001#\"\n"
				+ "    xml:base=\"http://www.w3.org/2002/03owlt/Ontology/conclusions001\" >\n"
				+ "  <owl:Ontology />\n"
				+ "  <first:Car rdf:about=\"premises001#auto\">\n"
				+ "     <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\" />\n"
				+ "  </first:Car>\n"
				+ "  <first:Automobile rdf:about=\"premises001#car\">\n"
				+ "     <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\" />\n"
				+ "  </first:Automobile>\n"
				+ "   <owl:Class rdf:about=\"premises001#Car\"/>\n"
				+ "   <owl:Class rdf:about=\"premises001#Automobile\"/>\n"
				+ "</rdf:RDF>";
		String id = "WebOnt_Ontology_001";
		TestClasses tc = TestClasses.valueOf("POSITIVE_IMPL");
		String d = "This is a variation of <a xmlns=\"http://www.w3.org/1999/xhtml\" href=\"#equivalentClass-001\">equivalentClass-001</a>,\n"
				+ "showing the use of <code>owl:Ontology</code> triples in the premises and conclusions.";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
		r.setReasonerFactory(Factory.factory());
		r.run();
	}
}