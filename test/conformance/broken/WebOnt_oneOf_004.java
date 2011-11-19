package conformance.broken;

import junit.framework.TestCase;
import conformance.Factory;
import conformance.JUnitRunner;
import conformance.TestClasses;

public class WebOnt_oneOf_004 extends TestCase {
	public void testWebOnt_oneOf_004() throws Exception {
		String premise = "Prefix(xsd:=<http://www.w3.org/2001/XMLSchema#>)\n"
				+ "Prefix(owl:=<http://www.w3.org/2002/07/owl#>)\nPrefix(xml:=<http://www.w3.org/XML/1998/namespace>)\nPrefix(rdf:=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)\nPrefix(rdfs:=<http://www.w3.org/2000/01/rdf-schema#>)\n"
				+ "Ontology(\nDeclaration(DataProperty(<urn:t:p#p>))\n"
				+ "DataPropertyRange(<urn:t:p#p> DataOneOf(\"1\"^^xsd:integer \"2\"^^xsd:integer \"3\"^^xsd:integer \"4\"^^xsd:integer))\n"
				+ "DataPropertyRange(<urn:t:p#p> DataOneOf(\"4\"^^xsd:integer \"5\"^^xsd:integer \"6\"^^xsd:integer))\n"
				+ "ClassAssertion(owl:Thing <urn:t:p#i>)\n"
				+ "ClassAssertion(DataMinCardinality(1 <urn:t:p#p>) <urn:t:p#i>)\n"
				//+"DataPropertyAssertion(<urn:t:p#p> <urn:t:p#i> \"4\"^^xsd:integer)"
				+ ")";
		String conclusion = "Prefix(xsd:=<http://www.w3.org/2001/XMLSchema#>)\n"
				+ "Prefix(owl:=<http://www.w3.org/2002/07/owl#>)\nPrefix(xml:=<http://www.w3.org/XML/1998/namespace>)\nPrefix(rdf:=<http://www.w3.org/1999/02/22-rdf-syntax-ns#>)\nPrefix(rdfs:=<http://www.w3.org/2000/01/rdf-schema#>)\n"
				+ "Ontology(\nDeclaration(DataProperty(<urn:t:p#p>))\n"
				+ "ClassAssertion(owl:Thing <urn:t:p#i>)\n"
				+ "DataPropertyAssertion(<urn:t:p#p> <urn:t:p#i> \"4\"^^xsd:integer))";
		String id = "WebOnt_oneOf_004";
		TestClasses tc = TestClasses.valueOf("POSITIVE_IMPL");
		String d = "This test illustrates the use of dataRange in OWL DL.\n"
				+ "This test combines some of the ugliest features of XML, RDF and OWL.";
		JUnitRunner r = new JUnitRunner(premise, conclusion, id, tc, d);
//		OWLOntology o = r.getPremise();
//		final OWLOntologyManager m = o.getOWLOntologyManager();
//		OWLDataFactory f = m.getOWLDataFactory();
//		final OWLDataComplementOf owlDataComplementOf = f.getOWLDataComplementOf(f
//				.getOWLDataOneOf(f.getOWLLiteral("4", OWL2Datatype.XSD_INTEGER)));
//		final OWLDataProperty p = f.getOWLDataProperty(IRI.create("urn:t:p#p"));
//		OWLIndividual i = f.getOWLNamedIndividual(IRI.create("urn:t:p#i"));
//		m.addAxiom(
//				o,
//				f.getOWLClassAssertionAxiom(
//						f.getOWLDataAllValuesFrom(p, owlDataComplementOf), i));
//		m.saveOntology(o, new OWLFunctionalSyntaxOntologyFormat(), new SystemOutDocumentTarget());
//		JFactReasonerConfiguration c=new JFactReasonerConfiguration();
//		c.setLoggingActive(true);
//		OWLReasoner reasoner=Factory.factory().createReasoner(o,c);
//		assertFalse(reasoner.isConsistent());
				r.setReasonerFactory(Factory.factory());
				r.getConfiguration().setLoggingActive(true);
				r.run();
	}
}