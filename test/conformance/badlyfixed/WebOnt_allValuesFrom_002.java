package conformance.badlyfixed;

import junit.framework.TestCase;
import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class WebOnt_allValuesFrom_002 extends TestCase {
	public void testWebOnt_allValuesFrom_002() {
		String premise = "<rdf:RDF\n" + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" + "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" + "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "    xmlns:first=\"http://www.w3.org/2002/03owlt/allValuesFrom/premises002#\"\n" + "    xml:base=\"http://www.w3.org/2002/03owlt/allValuesFrom/premises002\" >\n" + "    <owl:Ontology/>\n" + "    <owl:Class rdf:ID=\"r\">\n" + "      <rdfs:subClassOf>\n"
				+ "        <owl:Restriction>\n" + "            <owl:onProperty rdf:resource=\"#p\"/>\n" + "            <owl:allValuesFrom rdf:resource=\"#c\"/>\n" + "        </owl:Restriction>\n" + "      </rdfs:subClassOf>\n" + "    </owl:Class>\n"
				+ "    <owl:ObjectProperty rdf:ID=\"p\"/>\n" + "\n" + "    <owl:Class rdf:ID=\"c\"/>\n" + "    <first:r rdf:ID=\"i\">\n" + "      <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/>\n" + "    </first:r>\n" + "</rdf:RDF>";
		String conclusion = "<rdf:RDF\n" + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" + "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" + "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "    xmlns:first=\"http://www.w3.org/2002/03owlt/allValuesFrom/premises002#\"\n" + "    xmlns:second=\"http://www.w3.org/2002/03owlt/allValuesFrom/nonconclusions002#\"\n" + "    xml:base=\"http://www.w3.org/2002/03owlt/allValuesFrom/nonconclusions002\" >\n"
				+ "    <owl:Ontology/>\n" + "    <owl:Thing rdf:about=\"premises002#i\">\n" + "        <first:p>\n" + "           <first:c rdf:nodeID=\"o\" />\n" + "         </first:p>\n" + "    </owl:Thing>\n" + "    <owl:Thing rdf:nodeID=\"o\" />\n"
				+ "    <owl:ObjectProperty rdf:about=\"premises002#p\"/>\n" + "    <owl:Class rdf:about=\"premises002#c\"/>\n" + "</rdf:RDF>";
		String id = "WebOnt_allValuesFrom_002";
		TestClasses tc = TestClasses.valueOf("NEGATIVE_IMPL");
		String d = "Another simple example; contrast with <code>owl:someValuesFrom</code>.";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
		r.setReasonerFactory(Factory.factory());
		r.run();
	}
}