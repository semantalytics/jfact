package conformance;

import static org.junit.Assert.*;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

@SuppressWarnings("javadoc")
public class WebOnt_description_logic_602_test {
    @Test
    public void testWebOnt_description_logic_602() throws OWLOntologyCreationException,
            OWLOntologyStorageException {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLDataFactory f = m.getOWLDataFactory();
        OWLOntology o = m.createOntology();
        OWLClass A = f.getOWLClass(IRI.create("urn:A"));
        OWLClass C = f.getOWLClass(IRI.create("urn:C"));
        OWLClass D = f.getOWLClass(IRI.create("urn:D"));
        OWLClass B = f.getOWLClass(IRI.create("urn:B"));
        OWLClass U = f.getOWLClass(IRI.create("urn:U"));
        OWLObjectProperty p = f.getOWLObjectProperty(IRI.create("urn:p"));
        OWLObjectProperty r = f.getOWLObjectProperty(IRI.create("urn:r"));
        OWLObjectAllValuesFrom rAllC = f.getOWLObjectAllValuesFrom(r, C);
        m.addAxiom(o, f.getOWLEquivalentClassesAxiom(A, rAllC));
        m.addAxiom(o, f.getOWLSubClassOfAxiom(A, D));
        m.addAxiom(o, f.getOWLSubClassOfAxiom(U, C));
        m.addAxiom(o, f.getOWLSubClassOfAxiom(U, B));
        m.addAxiom(o, f.getOWLEquivalentClassesAxiom(C, rAllC));
        m.addAxiom(o,
                f.getOWLEquivalentClassesAxiom(D, f.getOWLObjectMaxCardinality(0, p)));
        m.addAxiom(o,
                f.getOWLEquivalentClassesAxiom(B, f.getOWLObjectMinCardinality(1, p)));
        m.saveOntology(o, new OWLFunctionalSyntaxOntologyFormat(),
                new SystemOutDocumentTarget());
        OWLReasoner reasoner = Factory.factory().createReasoner(o);
        assertTrue("cannot infer disjoint",
                reasoner.isEntailed(f.getOWLDisjointClassesAxiom(D, B)));
        assertTrue("cannot infer U [= B",
                reasoner.isEntailed(f.getOWLSubClassOfAxiom(U, B)));
        assertTrue("cannot infer U [= C",
                reasoner.isEntailed(f.getOWLSubClassOfAxiom(U, C)));
        assertTrue("cannot infer C [= r some C",
                reasoner.isEntailed(f.getOWLSubClassOfAxiom(C, rAllC)));
        assertTrue("cannot infer r some C = A",
                reasoner.isEntailed(f.getOWLEquivalentClassesAxiom(rAllC, A)));
        assertTrue("cannot infer A [= D",
                reasoner.isEntailed(f.getOWLSubClassOfAxiom(A, D)));
        assertTrue("cannot infer U [= D",
                reasoner.isEntailed(f.getOWLSubClassOfAxiom(U, D)));
        assertFalse("cannot find unsatisfiable class", reasoner.isSatisfiable(U));
    }
}
