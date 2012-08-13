package conformance.autogenerated;

import org.junit.Test;

import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class DisjointClasses_001 {
    @Test
    public void testDisjointClasses_001() {
        String premise = "<?xml version=\"1.0\"?>\n" + "<rdf:RDF\n"
                + "  xml:base  = \"http://example.org/\"\n"
                + "  xmlns     = \"http://example.org/\"\n"
                + "  xmlns:owl = \"http://www.w3.org/2002/07/owl#\"\n"
                + "  xmlns:rdf = \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n"
                + "\n" + "<owl:Ontology/>\n" + "\n" + "<owl:Class rdf:about=\"Boy\" />\n"
                + "<owl:Class rdf:about=\"Girl\" />\n" + "\n"
                + "<rdf:Description rdf:about=\"Boy\">\n"
                + "  <owl:disjointWith rdf:resource=\"Girl\" />\n"
                + "</rdf:Description>\n" + "\n" + "<Boy rdf:about=\"Stewie\" />\n" + "\n"
                + "</rdf:RDF>";
        String conclusion = "<?xml version=\"1.0\"?>\n" + "<rdf:RDF\n"
                + "  xml:base  = \"http://example.org/\"\n"
                + "  xmlns     = \"http://example.org/\"\n"
                + "  xmlns:owl = \"http://www.w3.org/2002/07/owl#\"\n"
                + "  xmlns:rdf = \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n"
                + "\n" + "<owl:Ontology/>\n" + "\n"
                + "<owl:Class rdf:about=\"Girl\" />\n" + "\n"
                + "<rdf:Description rdf:about=\"Stewie\">\n" + "  <rdf:type>\n"
                + "    <owl:Class>\n"
                + "      <owl:complementOf rdf:resource=\"Girl\" />\n"
                + "    </owl:Class>\n" + "  </rdf:type>\n" + "</rdf:Description>\n"
                + "\n" + "</rdf:RDF>";
        String id = "DisjointClasses_001";
        TestClasses tc = TestClasses.valueOf("POSITIVE_IMPL");
        String d = "Demonstrates a binary disjoint classes axiom based on example in the Structural Specification and Functional-Style Syntax document.";
        JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
        r.setReasonerFactory(Factory.factory());
        r.run();
    }
}