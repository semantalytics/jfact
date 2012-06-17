package conformance.autogenerated;

import org.junit.Test;

import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class WebOnt_description_logic_625 {
	@Test
	public void testWebOnt_description_logic_625() {
		String premise = "<rdf:RDF\n"
				+ "    xmlns:oiled=\"http://oiled.man.example.net/test#\"\n"
				+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
				+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ " xml:base=\"http://www.w3.org/2002/03owlt/description-logic/consistent625\">\n"
				+ " <owl:Ontology rdf:about=\"\"/>\n"
				+ " <owl:Class rdf:about=\"http://oiled.man.example.net/test#Satisfiable\">\n"
				+ "  <owl:intersectionOf rdf:parseType=\"Collection\">\n"
				+ "   <owl:Class rdf:about=\"http://oiled.man.example.net/test#a.comp\"/>\n"
				+ "   <owl:Restriction>\n"
				+ "    <owl:onProperty>\n"
				+ "     <owl:ObjectProperty rdf:about=\"http://oiled.man.example.net/test#invF\"/>\n"
				+ "    </owl:onProperty>\n"
				+ "    <owl:someValuesFrom>\n"
				+ "     <owl:Class rdf:about=\"http://oiled.man.example.net/test#a\"/>\n"
				+ "    </owl:someValuesFrom>\n"
				+ "   </owl:Restriction>\n"
				+ "   <owl:Restriction>\n"
				+ "    <owl:onProperty>\n"
				+ "     <owl:ObjectProperty rdf:about=\"http://oiled.man.example.net/test#invR\"/>\n"
				+ "    </owl:onProperty>\n"
				+ "    <owl:someValuesFrom>\n"
				+ "     <owl:Class rdf:about=\"#V.2\"/>\n"
				+ "    </owl:someValuesFrom>\n"
				+ "   </owl:Restriction>\n"
				+ "  </owl:intersectionOf>\n"
				+ " </owl:Class>\n"
				+ " <owl:Class rdf:ID=\"V.2\">\n"
				+ "  <owl:equivalentClass>\n"
				+ "   <owl:Restriction>\n"
				+ "    <owl:onProperty>\n"
				+ "     <owl:ObjectProperty rdf:about=\"http://oiled.man.example.net/test#invF\"/>\n"
				+ "    </owl:onProperty>\n"
				+ "    <owl:someValuesFrom>\n"
				+ "     <owl:Class rdf:about=\"http://oiled.man.example.net/test#a\"/>\n"
				+ "    </owl:someValuesFrom>\n"
				+ "   </owl:Restriction>\n"
				+ "  </owl:equivalentClass>\n"
				+ " </owl:Class>\n"
				+ " <owl:Class rdf:about=\"http://oiled.man.example.net/test#a.comp\">\n"
				+ "  <owl:equivalentClass>\n"
				+ "   <owl:Restriction>\n"
				+ "    <owl:onProperty>\n"
				+ "     <owl:DatatypeProperty rdf:ID=\"P.1\"/>\n"
				+ "    </owl:onProperty>\n"
				+ "    <owl:minCardinality rdf:datatype=\"/2001/XMLSchema#int\"\n"
				+ "    >1</owl:minCardinality>\n"
				+ "   </owl:Restriction>\n"
				+ "  </owl:equivalentClass>\n"
				+ " </owl:Class>\n"
				+ " <owl:Class rdf:about=\"http://oiled.man.example.net/test#a\">\n"
				+ "  <owl:equivalentClass>\n"
				+ "   <owl:Restriction>\n"
				+ "    <owl:onProperty rdf:resource=\"#P.1\"/>\n"
				+ "    <owl:maxCardinality rdf:datatype=\"/2001/XMLSchema#int\"\n"
				+ "    >0</owl:maxCardinality>\n"
				+ "   </owl:Restriction>\n"
				+ "  </owl:equivalentClass>\n"
				+ " </owl:Class>\n"
				+ " <owl:ObjectProperty rdf:about=\"http://oiled.man.example.net/test#f\">\n"
				+ "  <rdfs:subPropertyOf>\n"
				+ "   <owl:ObjectProperty rdf:about=\"http://oiled.man.example.net/test#r\"/>\n"
				+ "  </rdfs:subPropertyOf>\n"
				+ "  <rdf:type rdf:resource=\"/2002/07/owl#FunctionalProperty\"/>\n"
				+ " </owl:ObjectProperty>\n"
				+ " <owl:ObjectProperty rdf:about=\"http://oiled.man.example.net/test#invF\">\n"
				+ "  <owl:inverseOf rdf:resource=\"http://oiled.man.example.net/test#f\"/>\n"
				+ " </owl:ObjectProperty>\n"
				+ " <owl:ObjectProperty rdf:about=\"http://oiled.man.example.net/test#r\">\n"
				+ "  <rdf:type rdf:resource=\"/2002/07/owl#TransitiveProperty\"/>\n"
				+ " </owl:ObjectProperty>\n"
				+ " <owl:ObjectProperty rdf:about=\"http://oiled.man.example.net/test#invR\">\n"
				+ "  <owl:inverseOf rdf:resource=\"http://oiled.man.example.net/test#r\"/>\n"
				+ " </owl:ObjectProperty>\n" + " <oiled:Satisfiable/>\n" + "</rdf:RDF>";
		String conclusion = "";
		String id = "WebOnt_description_logic_625";
		TestClasses tc = TestClasses.valueOf("CONSISTENCY");
		String d = "DL Test: t5f.1\n" + "Non-finite model example from paper\n" + "\n"
				+ "The concept should be coherent but has no finite model";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
		r.setReasonerFactory(Factory.factory());
		r.run();
	}
}