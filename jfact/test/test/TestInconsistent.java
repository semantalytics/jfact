package test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import uk.ac.manchester.cs.jfact.JFactFactory;
import uk.ac.manchester.cs.jfact.kernel.options.JFactReasonerConfiguration;

public class TestInconsistent {
    @Test
    public void testInconsistent() throws Exception {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.createOntology();
        OWLDataFactory f = m.getOWLDataFactory();
        OWLLiteral lit = f.getOWLLiteral("3", OWL2Datatype.XSD_INTEGER);
        OWLIndividual a = f.getOWLNamedIndividual(IRI.create("urn:t:a"));
        OWLClass A = f.getOWLClass(IRI.create("urn:t:A"));
        OWLDataProperty p = f.getOWLDataProperty(IRI.create("urn:t:p"));
        m.addAxiom(o, f.getOWLClassAssertionAxiom(A, a));
        m.addAxiom(o, f.getOWLDataPropertyAssertionAxiom(p, a, lit));
        OWLDataOneOf oneof = f.getOWLDataOneOf(lit);
        m.addAxiom(o, f.getOWLSubClassOfAxiom(A, f.getOWLDataAllValuesFrom(p, oneof)));
        m.addAxiom(
                o,
                f.getOWLSubClassOfAxiom(A,
                        f.getOWLDataAllValuesFrom(p, f.getOWLDataComplementOf(oneof))));
        JFactReasonerConfiguration c = new JFactReasonerConfiguration();
        // c.setverboseOutput(true);
        // c.setLoggingActive(true);
        OWLReasoner r = new JFactFactory().createReasoner(o, c);
        assertFalse("O is supposed to be inconsistent", r.isConsistent());
    }
}
