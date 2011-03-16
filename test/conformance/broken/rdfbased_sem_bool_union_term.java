package conformance.broken;

import junit.framework.TestCase;
import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class rdfbased_sem_bool_union_term extends TestCase {
	public void testrdfbased_sem_bool_union_term() {
		//XXX test modified because of ontology not compliant with OWL 2
		String premise = "<rdf:RDF\n" + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" + "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n" + "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" + "    xmlns:ex=\"http://www.example.org#\"\n"
				+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n"
				//added
				+ "  <owl:Class rdf:about=\"http://www.example.org#c\"/>\n" + "  <owl:Class rdf:about=\"http://www.example.org#x\"/>\n" + "  <owl:Class rdf:about=\"http://www.example.org#y\"/>\n"
				//end added
				+ "  <rdf:Description rdf:about=\"http://www.example.org#c\">\n"
				//added
				+ "<rdfs:subClassOf><owl:Class>"
				//end added
				+ "    <owl:unionOf rdf:parseType=\"Collection\">\n" + "      <rdf:Description rdf:about=\"http://www.example.org#x\"/>\n" + "      <rdf:Description rdf:about=\"http://www.example.org#y\"/>\n" + "    </owl:unionOf>\n"
				//added
				+ "</owl:Class></rdfs:subClassOf>"
				//end added
				+ "  </rdf:Description>\n" + "</rdf:RDF>";
		String conclusion = "<rdf:RDF\n" + "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" + "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n" + "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" + "    xmlns:ex=\"http://www.example.org#\"\n"
				+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n"
				//added
				+ "  <owl:Class rdf:about=\"http://www.example.org#c\"/>\n" + "  <owl:Class rdf:about=\"http://www.example.org#x\"/>\n" + "  <owl:Class rdf:about=\"http://www.example.org#y\"/>\n"
				//end added
				+ "  <rdf:Description rdf:about=\"http://www.example.org#x\">\n" + "    <rdfs:subClassOf rdf:resource=\"http://www.example.org#c\"/>\n" + "  </rdf:Description>\n" + "  <rdf:Description rdf:about=\"http://www.example.org#y\">\n"
				+ "    <rdfs:subClassOf rdf:resource=\"http://www.example.org#c\"/>\n" + "  </rdf:Description>\n" + "</rdf:RDF>";
		String id = "rdfbased_sem_bool_union_term";
		TestClasses tc = TestClasses.valueOf("POSITIVE_IMPL");
		String d = "If a class is a union of other classes, then each of the other classes are subclasses of the original class.";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
		r.setReasonerFactory(Factory.factory());
		r.run();
	}
}