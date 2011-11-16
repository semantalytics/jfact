package conformance.fixed;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;

import uk.ac.manchester.cs.jfact.JFactFactory;
import uk.ac.manchester.cs.jfact.kernel.options.JFactReasonerConfiguration;

public class TestDateTime extends TestCase {
	public void testEqual() throws Exception {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology o = m.createOntology();
		OWLDataFactory f = m.getOWLDataFactory();
		OWLNamedIndividual x = f
				.getOWLNamedIndividual(IRI.create("urn:test:x"));
		OWLNamedIndividual y = f
				.getOWLNamedIndividual(IRI.create("urn:test:y"));
		OWLDataProperty p = f.getOWLDataProperty(IRI.create("urn:test:p"));
		OWLLiteral date = f.getOWLLiteral("2008-07-08T20:44:11.656+01:00",
				OWL2Datatype.XSD_DATE_TIME);
		m.addAxiom(o, f.getOWLDataPropertyAssertionAxiom(p, x, date));
		m.addAxiom(o, f.getOWLDataPropertyAssertionAxiom(p, y, date));
		m.addAxiom(o, f.getOWLFunctionalDataPropertyAxiom(p));
		m.addAxiom(o, f.getOWLSameIndividualAxiom(x, y));
		JFactReasonerConfiguration c=new JFactReasonerConfiguration();
		//c.setAbsorptionLoggingActive(true);

		OWLReasoner r = new JFactFactory().createReasoner(o,c);
		assertTrue(
				"Ontology was supposed to be consistent!\n"
						+ o.getLogicalAxioms(), r.isConsistent());
	}

	public void testDifferent() throws Exception {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology o = m.createOntology();
		OWLDataFactory f = m.getOWLDataFactory();
		OWLNamedIndividual x = f
				.getOWLNamedIndividual(IRI.create("urn:test:x"));
		OWLNamedIndividual y = f
				.getOWLNamedIndividual(IRI.create("urn:test:y"));
		OWLDataProperty p = f.getOWLDataProperty(IRI.create("urn:test:p"));
		OWLLiteral date1 = f.getOWLLiteral("2008-07-08T20:44:11.656+01:00",
				OWL2Datatype.XSD_DATE_TIME);
		OWLLiteral date2 = f.getOWLLiteral("2008-07-10T20:44:11.656+01:00",
				OWL2Datatype.XSD_DATE_TIME);
		m.addAxiom(o, f.getOWLDataPropertyAssertionAxiom(p, x, date1));
		m.addAxiom(o, f.getOWLDataPropertyAssertionAxiom(p, y, date2));
		m.addAxiom(o, f.getOWLFunctionalDataPropertyAxiom(p));
		m.addAxiom(o, f.getOWLSameIndividualAxiom(x, y));
		JFactReasonerConfiguration c=new JFactReasonerConfiguration();
		c.setLoggingActive(true);

		OWLReasoner r = new JFactFactory().createReasoner(o,c);
		assertFalse(
				"Ontology was supposed to be inconsistent!\n"
						+ o.getLogicalAxioms(), r.isConsistent());
	}

	public void testBetween() throws Exception {
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology o = m.createOntology();
		OWLDataFactory f = m.getOWLDataFactory();
		OWLNamedIndividual x = f
				.getOWLNamedIndividual(IRI.create("urn:test:x"));
		OWLClass c = f.getOWLClass(IRI.create("urn:test:c"));
		OWLDataProperty p = f.getOWLDataProperty(IRI.create("urn:test:p"));
		OWLLiteral date1 = f.getOWLLiteral("2008-07-08T20:44:11.656+01:00",
				OWL2Datatype.XSD_DATE_TIME);
		OWLLiteral date3 = f.getOWLLiteral("2008-07-09T20:44:11.656+01:00",
				OWL2Datatype.XSD_DATE_TIME);
		OWLLiteral date2 = f.getOWLLiteral("2008-07-10T20:44:11.656+01:00",
				OWL2Datatype.XSD_DATE_TIME);
		OWLDataRange range = f.getOWLDatatypeRestriction(
				f.getOWLDatatype(OWL2Datatype.XSD_DATE_TIME.getIRI()),
				f.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, date1),
				f.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, date2));
		OWLClassExpression psome = f.getOWLDataSomeValuesFrom(p, range);
		m.addAxiom(o, f.getOWLEquivalentClassesAxiom(c, psome));
		m.addAxiom(o, f.getOWLDataPropertyAssertionAxiom(p, x, date3));
		m.addAxiom(o, f.getOWLFunctionalDataPropertyAxiom(p));
		JFactReasonerConfiguration conf=new JFactReasonerConfiguration();
		conf.setLoggingActive(true);

		OWLReasoner r = new JFactFactory().createReasoner(o,conf);


		assertTrue(r.isConsistent());
		assertTrue(
				"x was supposed to be an instance of c!\n"
						+ o.getLogicalAxioms(),
				r.isEntailed(f.getOWLClassAssertionAxiom(c, x)));
	}
}