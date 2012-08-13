package conformance.autogenerated;

import org.junit.Test;

import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class owl2_rl_valid_rightside_allvaluesfrom {
    @Test
    public void testowl2_rl_valid_rightside_allvaluesfrom() {
        String premise = "<rdf:RDF\n"
                + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
                + "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
                + "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\">\n"
                + "  <owl:Ontology />\n"
                + "  <owl:Class rdf:about=\"http://owl2.test/rules#C\">\n"
                + "    <rdfs:subClassOf>\n"
                + "      <owl:Restriction>\n"
                + "        <owl:onProperty>\n"
                + "          <owl:ObjectProperty rdf:about=\"http://owl2.test/rules#op\"/>\n"
                + "        </owl:onProperty>\n" + "        <owl:allValuesFrom>\n"
                + "          <owl:Class rdf:about=\"http://owl2.test/rules#C1\"/>\n"
                + "        </owl:allValuesFrom>\n" + "      </owl:Restriction>\n"
                + "    </rdfs:subClassOf>\n" + "  </owl:Class>\n" + "</rdf:RDF>";
        String conclusion = "";
        String id = "owl2_rl_valid_rightside_allvaluesfrom";
        TestClasses tc = TestClasses.valueOf("CONSISTENCY");
        String d = "Valid RL usage of allValuesFrom.";
        JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
        r.setReasonerFactory(Factory.factory());
        r.run();
    }
}