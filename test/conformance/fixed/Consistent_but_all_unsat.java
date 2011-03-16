package conformance.fixed;

import junit.framework.TestCase;
import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class Consistent_but_all_unsat extends TestCase {
	public void testConsistent_but_all_unsat() {
		String premise = "<?xml version=\"1.0\"?>\n" + "\n" + "\n" + "<!DOCTYPE rdf:RDF [\n" + "    <!ENTITY example \"http://example.com/\" >\n" + "    <!ENTITY owl \"http://www.w3.org/2002/07/owl#\" >\n" + "    <!ENTITY xsd \"http://www.w3.org/2001/XMLSchema#\" >\n"
				+ "    <!ENTITY owl2xml \"http://www.w3.org/2006/12/owl2-xml#\" >\n" + "    <!ENTITY rdfs \"http://www.w3.org/2000/01/rdf-schema#\" >\n" + "    <!ENTITY rdf \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >\n" + "]>\n" + "\n" + "\n"
				+ "<rdf:RDF xmlns=\"http://example.com/\"\n" + "     xml:base=\"http://example.com/\"\n" + "     xmlns:owl2xml=\"http://www.w3.org/2006/12/owl2-xml#\"\n" + "     xmlns:example=\"http://example.com/\"\n" + "     xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"
				+ "     xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" + "     xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" + "     xmlns:owl=\"http://www.w3.org/2002/07/owl#\">\n"
				+ "    <owl:Ontology rdf:about=\"http://owl.semanticweb.org/page/Special:GetOntology/Consistent-but-all-unsat?m=p\"/>\n" + "    \n" + "\n" + "\n" + "    <!-- \n" + "    ///////////////////////////////////////////////////////////////////////////////////////\n"
				+ "    //\n" + "    // Object Properties\n" + "    //\n" + "    ///////////////////////////////////////////////////////////////////////////////////////\n" + "     -->\n" + "\n" + "    \n" + "\n" + "\n" + "    <!-- http://example.com/2aTOa -->\n" + "\n"
				+ "    <owl:ObjectProperty rdf:about=\"2aTOa\">\n" + "        <rdf:type rdf:resource=\"&owl;FunctionalProperty\"/>\n" + "        <rdf:type rdf:resource=\"&owl;InverseFunctionalProperty\"/>\n"
				+ "        <rdfs:label rdf:datatype=\"&xsd;string\">2a&lt;=&gt;a</rdfs:label>\n" + "    </owl:ObjectProperty>\n" + "    \n" + "\n" + "\n" + "    <!-- http://example.com/2aTObUNIONc -->\n" + "\n" + "    <owl:ObjectProperty rdf:about=\"2aTObUNIONc\">\n"
				+ "        <rdf:type rdf:resource=\"&owl;FunctionalProperty\"/>\n" + "        <rdf:type rdf:resource=\"&owl;InverseFunctionalProperty\"/>\n" + "        <rdfs:label rdf:datatype=\"&xsd;string\">2a&lt;=&gt;bUNIONc</rdfs:label>\n" + "    </owl:ObjectProperty>\n"
				+ "    \n" + "\n" + "\n" + "    <!-- http://example.com/aTO2a -->\n" + "\n" + "    <owl:ObjectProperty rdf:about=\"aTO2a\">\n" + "        <rdf:type rdf:resource=\"&owl;FunctionalProperty\"/>\n"
				+ "        <rdf:type rdf:resource=\"&owl;InverseFunctionalProperty\"/>\n" + "        <rdfs:label rdf:datatype=\"&xsd;string\">a&lt;=&gt;2a</rdfs:label>\n" + "        <owl:inverseOf rdf:resource=\"2aTOa\"/>\n" + "    </owl:ObjectProperty>\n" + "    \n" + "\n"
				+ "\n" + "    <!-- http://example.com/aTOb -->\n" + "\n" + "    <owl:ObjectProperty rdf:about=\"aTOb\">\n" + "        <rdf:type rdf:resource=\"&owl;FunctionalProperty\"/>\n" + "        <rdf:type rdf:resource=\"&owl;InverseFunctionalProperty\"/>\n"
				+ "        <rdfs:label rdf:datatype=\"&xsd;string\">a&lt;=&gt;b</rdfs:label>\n" + "    </owl:ObjectProperty>\n" + "    \n" + "\n" + "\n" + "    <!-- http://example.com/bUNIONcTO2a -->\n" + "\n" + "    <owl:ObjectProperty rdf:about=\"bUNIONcTO2a\">\n"
				+ "        <rdf:type rdf:resource=\"&owl;FunctionalProperty\"/>\n" + "        <rdf:type rdf:resource=\"&owl;InverseFunctionalProperty\"/>\n" + "        <rdfs:label rdf:datatype=\"&xsd;string\">bUNIONc&lt;=&gt;2a</rdfs:label>\n"
				+ "        <owl:inverseOf rdf:resource=\"2aTObUNIONc\"/>\n" + "    </owl:ObjectProperty>\n" + "    \n" + "\n" + "\n" + "    <!-- http://example.com/bTOa -->\n" + "\n" + "    <owl:ObjectProperty rdf:about=\"bTOa\">\n"
				+ "        <rdf:type rdf:resource=\"&owl;FunctionalProperty\"/>\n" + "        <rdf:type rdf:resource=\"&owl;InverseFunctionalProperty\"/>\n" + "        <rdfs:label rdf:datatype=\"&xsd;string\">b&lt;=&gt;a</rdfs:label>\n"
				+ "        <owl:inverseOf rdf:resource=\"aTOb\"/>\n" + "    </owl:ObjectProperty>\n" + "    \n" + "\n" + "\n" + "    <!-- http://example.com/bTOc -->\n" + "\n" + "    <owl:ObjectProperty rdf:about=\"bTOc\">\n"
				+ "        <rdf:type rdf:resource=\"&owl;FunctionalProperty\"/>\n" + "        <rdf:type rdf:resource=\"&owl;InverseFunctionalProperty\"/>\n" + "        <rdfs:label rdf:datatype=\"&xsd;string\">b&lt;=&gt;c</rdfs:label>\n" + "    </owl:ObjectProperty>\n" + "    \n"
				+ "\n" + "\n" + "    <!-- http://example.com/cTOb -->\n" + "\n" + "    <owl:ObjectProperty rdf:about=\"cTOb\">\n" + "        <rdf:type rdf:resource=\"&owl;FunctionalProperty\"/>\n" + "        <rdf:type rdf:resource=\"&owl;InverseFunctionalProperty\"/>\n"
				+ "        <rdfs:label rdf:datatype=\"&xsd;string\">c&lt;=&gt;b</rdfs:label>\n" + "        <owl:inverseOf rdf:resource=\"bTOc\"/>\n" + "    </owl:ObjectProperty>\n" + "    \n" + "\n" + "\n" + "    <!-- \n"
				+ "    ///////////////////////////////////////////////////////////////////////////////////////\n" + "    //\n" + "    // Classes\n" + "    //\n" + "    ///////////////////////////////////////////////////////////////////////////////////////\n" + "     -->\n"
				+ "\n" + "    \n" + "\n" + "\n" + "    <!-- http://example.com/2a -->\n" + "\n" + "    <owl:Class rdf:about=\"2a\">\n" + "        <rdfs:label rdf:datatype=\"&xsd;string\">2a</rdfs:label>\n" + "        <rdfs:subClassOf>\n" + "            <owl:Restriction>\n"
				+ "                <owl:onProperty rdf:resource=\"2aTObUNIONc\"/>\n" + "                <owl:someValuesFrom rdf:resource=\"bUNIONc\"/>\n" + "            </owl:Restriction>\n" + "        </rdfs:subClassOf>\n" + "        <rdfs:subClassOf>\n"
				+ "            <owl:Restriction>\n" + "                <owl:onProperty rdf:resource=\"2aTOa\"/>\n" + "                <owl:someValuesFrom rdf:resource=\"a\"/>\n" + "            </owl:Restriction>\n" + "        </rdfs:subClassOf>\n"
				+ "        <owl:disjointWith rdf:resource=\"a\"/>\n" + "        <owl:disjointWith rdf:resource=\"b\"/>\n" + "        <owl:disjointWith rdf:resource=\"bUNIONc\"/>\n" + "        <owl:disjointWith rdf:resource=\"c\"/>\n" + "    </owl:Class>\n" + "    \n" + "\n"
				+ "\n" + "    <!-- http://example.com/a -->\n" + "\n" + "    <owl:Class rdf:about=\"a\">\n" + "        <rdfs:label rdf:datatype=\"&xsd;string\">a</rdfs:label>\n" + "        <rdfs:subClassOf>\n" + "            <owl:Class>\n"
				+ "                <owl:unionOf rdf:parseType=\"Collection\">\n" + "                    <owl:Class>\n" + "                        <owl:oneOf rdf:parseType=\"Collection\">\n" + "                            <rdf:Description rdf:about=\"i1\"/>\n"
				+ "                        </owl:oneOf>\n" + "                    </owl:Class>\n" + "                    <owl:Class>\n" + "                        <owl:oneOf rdf:parseType=\"Collection\">\n" + "                            <rdf:Description rdf:about=\"i2\"/>\n"
				+ "                        </owl:oneOf>\n" + "                    </owl:Class>\n" + "                    <owl:Class>\n" + "                        <owl:oneOf rdf:parseType=\"Collection\">\n" + "                            <rdf:Description rdf:about=\"i3\"/>\n"
				+ "                        </owl:oneOf>\n" + "                    </owl:Class>\n" + "                </owl:unionOf>\n" + "            </owl:Class>\n" + "        </rdfs:subClassOf>\n" + "        <rdfs:subClassOf>\n" + "            <owl:Restriction>\n"
				+ "                <owl:onProperty rdf:resource=\"aTO2a\"/>\n" + "                <owl:someValuesFrom rdf:resource=\"2a\"/>\n" + "            </owl:Restriction>\n" + "        </rdfs:subClassOf>\n" + "        <rdfs:subClassOf>\n"
				+ "            <owl:Restriction>\n" + "                <owl:onProperty rdf:resource=\"aTOb\"/>\n" + "                <owl:someValuesFrom rdf:resource=\"b\"/>\n" + "            </owl:Restriction>\n" + "        </rdfs:subClassOf>\n"
				+ "        <owl:disjointWith rdf:resource=\"b\"/>\n" + "        <owl:disjointWith rdf:resource=\"c\"/>\n" + "    </owl:Class>\n" + "    \n" + "\n" + "\n" + "    <!-- http://example.com/b -->\n" + "\n" + "    <owl:Class rdf:about=\"b\">\n"
				+ "        <rdfs:label rdf:datatype=\"&xsd;string\">b</rdfs:label>\n" + "        <rdfs:subClassOf>\n" + "            <owl:Restriction>\n" + "                <owl:onProperty rdf:resource=\"bTOc\"/>\n" + "                <owl:someValuesFrom rdf:resource=\"c\"/>\n"
				+ "            </owl:Restriction>\n" + "        </rdfs:subClassOf>\n" + "        <rdfs:subClassOf>\n" + "            <owl:Restriction>\n" + "                <owl:onProperty rdf:resource=\"bTOa\"/>\n" + "                <owl:someValuesFrom rdf:resource=\"a\"/>\n"
				+ "            </owl:Restriction>\n" + "        </rdfs:subClassOf>\n" + "        <owl:disjointWith rdf:resource=\"c\"/>\n" + "    </owl:Class>\n" + "    \n" + "\n" + "\n" + "    <!-- http://example.com/bUNIONc -->\n" + "\n"
				+ "    <owl:Class rdf:about=\"bUNIONc\">\n" + "        <rdfs:label rdf:datatype=\"&xsd;string\">bUNIONc</rdfs:label>\n" + "        <owl:equivalentClass>\n" + "            <owl:Class>\n" + "                <owl:unionOf rdf:parseType=\"Collection\">\n"
				+ "                    <rdf:Description rdf:about=\"b\"/>\n" + "                    <rdf:Description rdf:about=\"c\"/>\n" + "                </owl:unionOf>\n" + "            </owl:Class>\n" + "        </owl:equivalentClass>\n" + "        <rdfs:subClassOf>\n"
				+ "            <owl:Restriction>\n" + "                <owl:onProperty rdf:resource=\"bUNIONcTO2a\"/>\n" + "                <owl:someValuesFrom rdf:resource=\"2a\"/>\n" + "            </owl:Restriction>\n" + "        </rdfs:subClassOf>\n" + "    </owl:Class>\n"
				+ "    \n" + "\n" + "\n" + "    <!-- http://example.com/c -->\n" + "\n" + "    <owl:Class rdf:about=\"c\">\n" + "        <rdfs:label rdf:datatype=\"&xsd;string\">c</rdfs:label>\n" + "        <rdfs:subClassOf>\n" + "            <owl:Restriction>\n"
				+ "                <owl:onProperty rdf:resource=\"cTOb\"/>\n" + "                <owl:someValuesFrom rdf:resource=\"b\"/>\n" + "            </owl:Restriction>\n" + "        </rdfs:subClassOf>\n" + "    </owl:Class>\n" + "    \n" + "\n" + "\n" + "    <!-- \n"
				+ "    ///////////////////////////////////////////////////////////////////////////////////////\n" + "    //\n" + "    // Individuals\n" + "    //\n" + "    ///////////////////////////////////////////////////////////////////////////////////////\n" + "     -->\n"
				+ "\n" + "    \n" + "\n" + "\n" + "    <!-- http://example.com/i1 -->\n" + "\n" + "    <rdf:Description rdf:about=\"i1\"/>\n" + "    \n" + "\n" + "\n" + "    <!-- http://example.com/i2 -->\n" + "\n" + "    <rdf:Description rdf:about=\"i2\"/>\n" + "    \n" + "\n"
				+ "\n" + "    <!-- http://example.com/i3 -->\n" + "\n" + "    <rdf:Description rdf:about=\"i3\"/>\n" + "</rdf:RDF>\n" + "\n" + "\n" + "\n" + "<!-- Generated by the OWL API (version 2.2.1.972) http://owlapi.sourceforge.net -->";
		String conclusion = "<?xml version=\"1.0\"?>\n" + "\n" + "\n" + "<!DOCTYPE rdf:RDF [\n" + "    <!ENTITY owl \"http://www.w3.org/2002/07/owl#\" >\n" + "    <!ENTITY xsd \"http://www.w3.org/2001/XMLSchema#\" >\n"
				+ "    <!ENTITY owl2xml \"http://www.w3.org/2006/12/owl2-xml#\" >\n" + "    <!ENTITY rdfs \"http://www.w3.org/2000/01/rdf-schema#\" >\n" + "    <!ENTITY rdf \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >\n"
				+ "    <!ENTITY Ontology1242664364013 \"http://www.semanticweb.org/ontologies/2009/4/Ontology1242664364013.owl#\" >\n" + "    <!ENTITY Ontology12426643640132 \"http://www.semanticweb.org/ontologies/2009/4/Ontology1242664364013.owl#2\" >\n" + "]>\n" + "\n" + "\n"
				+ "<rdf:RDF xmlns=\"http://example.com/\"\n" + "     xml:base=\"http://example.com/\"\n" + "     xmlns:owl2xml=\"http://www.w3.org/2006/12/owl2-xml#\"\n" + "     xmlns:example=\"http://example.com/\"\n" + "     xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"
				+ "     xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" + "     xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" + "     xmlns:owl=\"http://www.w3.org/2002/07/owl#\">\n"
				+ "    <owl:Ontology rdf:about=\"http://owl.semanticweb.org/page/Special:GetOntology/Consistent-but-all-unsat?m=c\"/>\n" + "    \n" + "\n" + "\n" + "\n" + "    <!-- \n"
				+ "    ///////////////////////////////////////////////////////////////////////////////////////\n" + "    //\n" + "    // Classes\n" + "    //\n" + "    ///////////////////////////////////////////////////////////////////////////////////////\n" + "     -->\n"
				+ "\n" + "    \n" + "\n" + "\n" + "    <!-- http://www.semanticweb.org/ontologies/2009/4/Ontology1242664364013.owl/2a -->\n" + "\n" + "    <owl:Class rdf:about=\"2a\">\n" + "        <rdfs:subClassOf rdf:resource=\"&owl;Nothing\"/>\n" + "    </owl:Class>\n"
				+ "    \n" + "\n" + "\n" + "    <!-- http://www.semanticweb.org/ontologies/2009/4/Ontology1242664364013.owl/a -->\n" + "\n" + "    <owl:Class rdf:about=\"a\">\n" + "        <rdfs:subClassOf rdf:resource=\"&owl;Nothing\"/>\n"
				+ "        <rdfs:subClassOf rdf:resource=\"&owl;Thing\"/>\n" + "    </owl:Class>\n" + "    \n" + "\n" + "\n" + "    <!-- http://www.semanticweb.org/ontologies/2009/4/Ontology1242664364013.owl/b -->\n" + "\n" + "    <owl:Class rdf:about=\"b\">\n"
				+ "        <rdfs:subClassOf rdf:resource=\"&owl;Nothing\"/>\n" + "    </owl:Class>\n" + "    \n" + "\n" + "\n" + "    <!-- http://www.semanticweb.org/ontologies/2009/4/Ontology1242664364013.owl/c -->\n" + "\n" + "    <owl:Class rdf:about=\"c\">\n"
				+ "        <rdfs:subClassOf rdf:resource=\"&owl;Nothing\"/>\n" + "    </owl:Class>\n" + "    \n" + "\n" + "\n" + "    <!-- http://www.w3.org/2002/07/owl#Nothing -->\n" + "\n" + "    <owl:Class rdf:about=\"&owl;Nothing\"/>\n" + "    \n" + "\n" + "\n"
				+ "    <!-- http://www.w3.org/2002/07/owl#Thing -->\n" + "\n" + "    <owl:Class rdf:about=\"&owl;Thing\"/>\n" + "</rdf:RDF>\n" + "\n" + "\n" + "\n" + "<!-- Generated by the OWL API (version 2.2.1.972) http://owlapi.sourceforge.net -->";
		String id = "Consistent_but_all_unsat";
		TestClasses tc = TestClasses.valueOf("POSITIVE_IMPL");
		String d = "An ontology that is consistent, but all named classes are unsatisfiable.  Ideas by Alan Ruttenberg";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
		r.setReasonerFactory(Factory.factory());
		r.run();
	}
}