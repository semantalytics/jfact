package conformance.broken;

import junit.framework.TestCase;
import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class WebOnt_oneOf_004 extends TestCase {
	public void testWebOnt_oneOf_004() {
		String premise = "<!DOCTYPE rdf:RDF [\n"
				+ "   <!ENTITY xsd \"http://www.w3.org/2001/XMLSchema#\">\n"
				+ "   <!ENTITY rdf \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n"
				+ "]>\n"
				+ "<rdf:RDF\n"
				+ " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ " xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
				+ " xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ " xmlns:first=\"http://www.w3.org/2002/03owlt/oneOf/premises004#\"\n"
				+ " xml:base=\"http://www.w3.org/2002/03owlt/oneOf/premises004\" >\n"
				+ " <owl:Ontology/>\n"
				+ " <owl:DatatypeProperty rdf:ID=\"p\">\n"
				+ "  <rdfs:range>\n"
				+ "   <owl:DataRange>\n"
				+ "    <owl:oneOf>\n"
				+ "     <rdf:List>\n"
				+ "      <rdf:first rdf:datatype=\"&xsd;integer\">1</rdf:first>\n"
				+ "      <rdf:rest>\n"
				+ "       <rdf:List>\n"
				+ "        <rdf:first rdf:datatype=\"&xsd;integer\">2</rdf:first>\n"
				+ "        <rdf:rest>\n"
				+ "         <rdf:List>\n"
				+ "          <rdf:first rdf:datatype=\"&xsd;integer\">3</rdf:first>\n"
				+ "          <rdf:rest>\n"
				+ "           <rdf:List>\n"
				+ "            <rdf:first rdf:datatype=\"&xsd;integer\">4</rdf:first>\n"
				+ "            <rdf:rest rdf:resource=\"&rdf;nil\"/>\n"
				+ "           </rdf:List>\n"
				+ "          </rdf:rest>\n"
				+ "         </rdf:List>\n"
				+ "        </rdf:rest>\n"
				+ "       </rdf:List>\n"
				+ "      </rdf:rest>\n"
				+ "     </rdf:List>\n"
				+ "    </owl:oneOf>\n"
				+ "   </owl:DataRange>\n"
				+ "  </rdfs:range>\n"
				+ "  <rdfs:range>\n"
				+ "   <owl:DataRange>\n"
				+ "    <owl:oneOf>\n"
				+ "     <rdf:List>\n"
				+ "      <rdf:first rdf:datatype=\"&xsd;integer\">4</rdf:first>\n"
				+ "      <rdf:rest>\n"
				+ "       <rdf:List>\n"
				+ "        <rdf:first rdf:datatype=\"&xsd;integer\">5</rdf:first>\n"
				+ "        <rdf:rest>\n"
				+ "         <rdf:List>\n"
				+ "          <rdf:first rdf:datatype=\"&xsd;integer\">6</rdf:first>\n"
				+ "          <rdf:rest rdf:resource=\"&rdf;nil\"/>\n"
				+ "         </rdf:List>\n"
				+ "        </rdf:rest>\n"
				+ "       </rdf:List>\n"
				+ "      </rdf:rest>\n"
				+ "     </rdf:List>\n"
				+ "    </owl:oneOf>\n"
				+ "   </owl:DataRange>\n"
				+ "  </rdfs:range>\n"
				+ " </owl:DatatypeProperty>\n"
				+ " <owl:Thing rdf:ID=\"i\">\n"
				+ "  <rdf:type>\n"
				+ "   <owl:Restriction>\n"
				+ "    <owl:onProperty rdf:resource=\"#p\"/>\n"
				+ "    <owl:minCardinality rdf:datatype=\"&xsd;int\">1</owl:minCardinality>\n"
				+ "   </owl:Restriction>\n" + "  </rdf:type>\n"
				+ " </owl:Thing>\n" + "</rdf:RDF>";
		String conclusion = "<rdf:RDF\n"
				+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "    xmlns:first=\"http://www.w3.org/2002/03owlt/oneOf/premises004#\"\n"
				+ "    xml:base=\"http://www.w3.org/2002/03owlt/oneOf/conclusions004\" >\n"
				+ "   <owl:Ontology/>\n"
				+ "   <owl:DatatypeProperty rdf:about=\"premises004#p\"/>\n"
				+ "   <owl:Thing rdf:about=\"premises004#i\">\n"
				+ "     <first:p rdf:datatype=\n"
				+ "\"http://www.w3.org/2001/XMLSchema#integer\">4</first:p>\n"
				+ "   </owl:Thing>\n" + "</rdf:RDF>";
		String id = "WebOnt_oneOf_004";
		TestClasses tc = TestClasses.valueOf("POSITIVE_IMPL");
		String d = "This test illustrates the use of dataRange in OWL DL.\n"
				+ "This test combines some of the ugliest features of XML, RDF and OWL.";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
		r.setReasonerFactory(Factory.factory());
		r.run();
	}
}