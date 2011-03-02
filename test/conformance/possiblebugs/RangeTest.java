package conformance.possiblebugs;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import junit.framework.TestCase;

public class RangeTest extends TestCase{
	public void testError() throws Exception{
		String conclusion = "<rdf:RDF\n"
			+ "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
			+ "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
			+ "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"
			+ "    xmlns:ex=\"http://www.example.org#\"\n"
			+ "    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n"
			//added
			+"<owl:ObjectProperty rdf:about=\"http://www.example.org#p1\"/>\n"
			+"<owl:ObjectProperty rdf:about=\"http://www.example.org#p2\"/>\n"
			+"<owl:Class rdf:about=\"http://www.example.org#c\"/>\n"
			//end added
			+ "  <rdf:Description rdf:about=\"http://www.example.org#p1\">\n"
			+ "    <rdfs:range rdf:resource=\"http://www.example.org#c\"/>\n"
			+ "  </rdf:Description>\n" + "</rdf:RDF>";
		
		OWLOntologyManager m=OWLManager.createOWLOntologyManager();
		OWLOntology o=m.createOntology();
		OWLDataFactory f=m.getOWLDataFactory();
		OWLClass c=f.getOWLClass(IRI.create("urn:test#c"));
		OWLObjectProperty p=f.getOWLObjectProperty(IRI.create("urn:test#p"));
		m.addAxiom(o, f.getOWLObjectPropertyRangeAxiom(p, c));
		SystemOutDocumentTarget t=new SystemOutDocumentTarget();
		m.saveOntology(o,t);
		
		OWLOntology o1=m.loadOntologyFromOntologyDocument(new StringDocumentSource(conclusion));
		for(OWLAxiom ax:o1.getAxioms()) {
			System.out.println("RangeTest.testError() "+ax);
		}
	}
}
