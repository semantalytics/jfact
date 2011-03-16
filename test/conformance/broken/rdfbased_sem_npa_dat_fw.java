package conformance.broken;

import junit.framework.TestCase;
import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class rdfbased_sem_npa_dat_fw extends TestCase {
	public void testrdfbased_sem_npa_dat_fw() {
		String premise = "<rdf:RDF\n" + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" + "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n" + "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" + "    xmlns:ex=\"http://www.example.org#\"\n"
				+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n" + "  <rdf:Description rdf:about=\"http://www.example.org#z\">\n" + "    <owl:sourceIndividual>\n" + "      <rdf:Description rdf:about=\"http://www.example.org#s\">\n" + "        <ex:p>data</ex:p>\n"
				+ "      </rdf:Description>\n" + "    </owl:sourceIndividual>\n" + "    <owl:assertionProperty rdf:resource=\"http://www.example.org#p\"/>\n" + "    <owl:targetValue>data</owl:targetValue>\n" + "  </rdf:Description>\n" + "</rdf:RDF>";
		String conclusion = "";
		String id = "rdfbased_sem_npa_dat_fw";
		TestClasses tc = TestClasses.valueOf("INCONSISTENCY");
		String d = "A negative data property assertion DNPA(s p \"data\") must not occur together with the corresponding positive data property assertion s p \"data\".";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
		r.setReasonerFactory(Factory.factory());
		r.run();
	}
}