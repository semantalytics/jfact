package conformance.autogenerated;

import org.junit.Test;

import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class owl2_rl_invalid_owlreal {
	@Test
	public void testowl2_rl_invalid_owlreal() {
		String premise = "<rdf:RDF\n"
				+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n"
				+ "  <owl:Ontology />\n"
				+ "  <owl:Class rdf:about=\"http://owl2.test/rules#C_Sub\">\n"
				+ "    <rdfs:subClassOf>\n"
				+ "      <owl:Restriction>\n"
				+ "        <owl:allValuesFrom rdf:resource=\"http://www.w3.org/2002/07/owl#real\"/>\n"
				+ "        <owl:onProperty>\n"
				+ "          <owl:DatatypeProperty rdf:about=\"http://owl2.test/rules#p\"/>\n"
				+ "        </owl:onProperty>\n" + "      </owl:Restriction>\n"
				+ "    </rdfs:subClassOf>\n" + "  </owl:Class>\n" + "</rdf:RDF>";
		String conclusion = "";
		String id = "owl2_rl_invalid_owlreal";
		TestClasses tc = TestClasses.valueOf("CONSISTENCY");
		String d = "Invalid OWL 2 RL because owl:real is used.";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
		r.setReasonerFactory(Factory.factory());
		r.run();
	}
}