package conformance.autogenerated;

import junit.framework.TestCase;
import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class WebOnt_disjointWith_004 extends TestCase {
	public void testWebOnt_disjointWith_004() {
		String premise = "<rdf:RDF\n"
				+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "    xml:base=\"http://www.w3.org/2002/03owlt/disjointWith/consistent004\" >\n"
				+ "    <owl:Ontology/>\n"
				+ "    <owl:Class rdf:nodeID=\"A\">\n"
				+ "      <owl:intersectionOf rdf:parseType=\"Collection\">\n"
				+ "        <owl:Class rdf:ID=\"A\"/>\n"
				+ "        <owl:Class rdf:ID=\"K\"/>\n"
				+ "      </owl:intersectionOf>\n"
				+ "      <owl:disjointWith rdf:nodeID=\"B\"/>\n"
				+ "      <owl:disjointWith rdf:nodeID=\"D\"/>\n"
				+ "      <owl:disjointWith rdf:nodeID=\"E\"/>\n"
				+ "    </owl:Class>\n" + "    <owl:Class rdf:nodeID=\"B\">\n"
				+ "      <owl:intersectionOf rdf:parseType=\"Collection\">\n"
				+ "        <owl:Class rdf:ID=\"B\"/>\n"
				+ "        <owl:Class rdf:ID=\"K\"/>\n"
				+ "      </owl:intersectionOf>\n"
				+ "      <owl:disjointWith rdf:nodeID=\"A\"/>\n"
				+ "      <owl:disjointWith rdf:nodeID=\"C\"/>\n"
				+ "      <owl:disjointWith rdf:nodeID=\"E\"/>\n"
				+ "    </owl:Class>\n" + "    <owl:Class rdf:nodeID=\"C\">\n"
				+ "      <owl:intersectionOf rdf:parseType=\"Collection\">\n"
				+ "        <owl:Class rdf:ID=\"C\"/>\n"
				+ "        <owl:Class rdf:ID=\"K\"/>\n"
				+ "      </owl:intersectionOf>\n"
				+ "      <owl:disjointWith rdf:nodeID=\"A\"/>\n"
				+ "      <owl:disjointWith rdf:nodeID=\"E\"/>\n"
				+ "    </owl:Class>\n" + "    <owl:Class rdf:nodeID=\"D\">\n"
				+ "      <owl:intersectionOf rdf:parseType=\"Collection\">\n"
				+ "        <owl:Class rdf:ID=\"D\"/>\n"
				+ "        <owl:Class rdf:ID=\"K\"/>\n"
				+ "      </owl:intersectionOf>\n"
				+ "      <owl:disjointWith rdf:nodeID=\"B\"/>\n"
				+ "      <owl:disjointWith rdf:nodeID=\"E\"/>\n"
				+ "    </owl:Class>\n" + "    <owl:Class rdf:nodeID=\"E\">\n"
				+ "      <owl:intersectionOf rdf:parseType=\"Collection\">\n"
				+ "        <owl:Class rdf:ID=\"K\"/>\n"
				+ "        <owl:Class rdf:ID=\"E\"/>\n"
				+ "      </owl:intersectionOf>\n" + "    </owl:Class>\n" + "\n"
				+ "</rdf:RDF>";
		String conclusion = "";
		String id = "WebOnt_disjointWith_004";
		TestClasses tc = TestClasses.valueOf("CONSISTENCY");
		String d = "This example has owl:disjointWith edges in the graph which cannot be generated\n"
				+ "by the mapping rules for DisjointClasses. Consider the lack of owl:disjointWith edge\n"
				+ "between nodes C and D.";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
		r.setReasonerFactory(Factory.factory());
		r.run();
	}
}