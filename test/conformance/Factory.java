package conformance;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.jfact.JFactFactory;

public class Factory {
	private OWLDataFactory f;
	private String ns="urn:test#";
	public Factory(OWLDataFactory f) {
		this.f=f;
	}
	public OWLClass getClass(String x) {
		return f.getOWLClass(IRI.create(ns+x));
	}
	public OWLObjectProperty getOProperty(String x) {
		return f.getOWLObjectProperty(IRI.create(ns+x));
	}
	public OWLDataProperty getDProperty(String x) {
		return f.getOWLDataProperty(IRI.create(ns+x));
	}
	public OWLNamedIndividual getInd(String x) {
		return f.getOWLNamedIndividual(IRI.create(ns+x));
	}
	
	public static final OWLReasonerFactory factory() {
		return new JFactFactory();
	}
}
