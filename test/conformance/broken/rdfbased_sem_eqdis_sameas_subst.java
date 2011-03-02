package conformance.broken;

import conformance.Factory;
import junit.framework.TestCase;
import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class rdfbased_sem_eqdis_sameas_subst extends TestCase {
	public void testrdfbased_sem_eqdis_sameas_subst() {
		//XXX test modified because of ontology not compliant with OWL 2
		String premise = "<rdf:RDF\n"
				+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"
				+ "    xmlns:ex=\"http://www.example.org#\"\n"
				+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n"
				//added
				+"<owl:ObjectProperty rdf:about=\"http://www.example.org#p1\"/>\n"
				// TODO this is a bug, should not be needed by the reasoner
				+"<owl:ObjectProperty rdf:about=\"http://www.example.org#p2\"/>\n"				
				//end added
				+ "  <rdf:Description rdf:about=\"http://www.example.org#s2\">\n"
				+ "    <owl:sameAs>\n"
				+ "      <rdf:Description rdf:about=\"http://www.example.org#s1\">\n"
				+ "        <ex:p1 rdf:resource=\"http://www.example.org#o1\"/>\n"
				+ "      </rdf:Description>\n"
				+ "    </owl:sameAs>\n"
				+ "  </rdf:Description>\n"
				+ "  <rdf:Description rdf:about=\"http://www.example.org#o2\">\n"
				+ "    <owl:sameAs rdf:resource=\"http://www.example.org#o1\"/>\n"
				+ "  </rdf:Description>\n"
				+ "  <rdf:Description rdf:about=\"http://www.example.org#p2\">\n"
				+ "    <owl:sameAs rdf:resource=\"http://www.example.org#p1\"/>\n"
				+ "  </rdf:Description>\n" + "</rdf:RDF>";
		String conclusion = "<rdf:RDF\n"
				+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"
				+ "    xmlns:ex=\"http://www.example.org#\"\n"
				+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n"
				//added
				+"<owl:ObjectProperty rdf:about=\"http://www.example.org#p1\"/>\n"
				+"<owl:ObjectProperty rdf:about=\"http://www.example.org#p2\"/>\n"
				//end added
				+ "  <rdf:Description rdf:about=\"http://www.example.org#s2\">\n"
				+ "    <ex:p1 rdf:resource=\"http://www.example.org#o1\"/>\n"
				+ "  </rdf:Description>\n"
				+ "  <rdf:Description rdf:about=\"http://www.example.org#s1\">\n"
				+ "    <ex:p2 rdf:resource=\"http://www.example.org#o1\"/>\n"
				+ "    <ex:p1 rdf:resource=\"http://www.example.org#o2\"/>\n"
				+ "  </rdf:Description>\n" + "</rdf:RDF>";
		String id = "rdfbased_sem_eqdis_sameas_subst";
		TestClasses tc = TestClasses.valueOf("POSITIVE_IMPL");
		String d = "Equality of two individuals allows for substituting the subject, predicate and object of an RDF triple by an equal individual.";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
		r.setReasonerFactory(Factory.factory());
		r.run();
	}
}