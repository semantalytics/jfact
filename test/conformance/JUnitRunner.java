package conformance;

import junit.framework.Assert;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import uk.ac.manchester.cs.jfact.kernel.options.JFactReasonerConfiguration;

public class JUnitRunner {
	private static final int _10000 = 200000;
	private TestClasses t;
	private OWLReasonerFactory f;
	private String testId;
	private String premise;
	private String consequence;
	private String description;
	OWLOntologyManager m = OWLManager.createOWLOntologyManager();
	private JFactReasonerConfiguration c = new JFactReasonerConfiguration(
			new SimpleConfiguration(_10000));

	public JUnitRunner(String premise, String consequence, String testId, TestClasses t,
			String description) {
		this.testId = testId;
		this.premise = premise;
		this.consequence = consequence;
		this.description = description;
		this.t = t;
	}

	public void setReasonerFactory(OWLReasonerFactory f) {
		this.f = f;
	}

	public JFactReasonerConfiguration getConfiguration() {
		return c;
	}

	private boolean isConsistent(OWLReasoner reasoner, boolean expected) {
		try {
			boolean consistent = reasoner.isConsistent();
			return consistent;
		} catch (RuntimeException e) {
			e.printStackTrace(System.out);
			return !expected;
		}
	}

	private boolean isEntailed(OWLReasoner reasoner, OWLAxiom conclusion, boolean expected) {
		try {
			return reasoner.isEntailed(conclusion);
		} catch (RuntimeException e) {
			e.printStackTrace(System.out);
			return !expected;
		}
	}

	public OWLOntology getPremise() throws OWLOntologyCreationException {
		if (premise != null) {
			StringDocumentSource documentSource = new StringDocumentSource(premise);
			return m.loadOntologyFromOntologyDocument(documentSource);
		}
		return null;
	}

	public OWLOntology getConsequence() throws OWLOntologyCreationException {
		if (consequence != null) {
			StringDocumentSource documentSource = new StringDocumentSource(consequence);
			return m.loadOntologyFromOntologyDocument(documentSource);
		}
		return null;
	}

	public void run() {
		OWLOntology premiseOntology = null;
		OWLOntology conclusionOntology = null;
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		m.setSilentMissingImportsHandling(true);
		try {
			if (premise != null) {
				premiseOntology = getPremise();
				OWL2DLProfile profile = new OWL2DLProfile();
				OWLProfileReport report = profile.checkOntology(premiseOntology);
				if (report.getViolations().size() > 0) {
					System.out.println("JUnitRunner.run() " + testId);
					System.out.println("JUnitRunner.run() premise violations:\n"
							+ report.toString());
					throw new RuntimeException("errors!\n" + report.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			//System.out.println("JUnitRunner.run() premise:\n" + premise);
			throw new RuntimeException(e);
		}
		try {
			if (consequence != null) {

				conclusionOntology = getConsequence();
				OWL2DLProfile profile = new OWL2DLProfile();
				OWLProfileReport report = profile.checkOntology(conclusionOntology);
				if (report.getViolations().size() > 0) {
					System.out.println("JUnitRunner.run() " + testId
							+ report.getViolations().size());
					System.out.println("JUnitRunner.run() conclusion violations:\n"
							+ report.toString());
					throw new RuntimeException("errors!");
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			throw new RuntimeException(e);
		}
		run(premiseOntology, conclusionOntology);
	}

	public void run(OWLOntology premiseOntology, OWLOntology conclusionOntology) {
		StringBuilder b = new StringBuilder();
		b.append("JUnitRunner.logTroubles() premise");
		b.append("\n");
		for (OWLAxiom ax1 : premiseOntology.getAxioms()) {
			b.append(ax1);
			b.append("\n");
		}
		OWLReasoner reasoner = f.createReasoner(premiseOntology, c);
		switch (t) {
			case CONSISTENCY: {
				boolean consistent = isConsistent(reasoner, true);
				if (!consistent) {
					Assert.assertEquals(
							b.toString()
									+ logTroubles(premiseOntology, true, consistent,
											null, t,null), true, consistent);
				}
			}
				break;
			case INCONSISTENCY: {
				boolean consistent = isConsistent(reasoner, false);
				if (consistent) {
					Assert.assertEquals(
							b.toString()
									+ logTroubles(premiseOntology, false, consistent,
											null, t,null), false, consistent);
				}
			}
				break;
			case NEGATIVE_IMPL: {
				boolean consistent = isConsistent(reasoner, true);
				if (!consistent) {
					Assert.assertEquals(
							b.toString()
									+ logTroubles(premiseOntology, true, consistent,
											null, t, null), true, consistent);
				}
				boolean entailed = false;
				for (OWLAxiom ax : conclusionOntology.getLogicalAxioms()) {
					boolean temp = isEntailed(reasoner, ax, false);
					entailed |= temp;
					if (temp) {
						b.append(logTroubles(premiseOntology, false, entailed, ax, t, ax));
					}
				}
				Assert.assertEquals(b.toString(), false, entailed);
			}
				break;
			case POSITIVE_IMPL: {
				boolean consistent = isConsistent(reasoner, true);
				if (!consistent) {
					Assert.assertEquals(
							b.toString()
									+ logTroubles(premiseOntology, true, consistent,
											null, t,null), true, consistent);
				}
				boolean entailed = true;
				for (OWLAxiom ax : conclusionOntology.getLogicalAxioms()) {
					boolean temp = isEntailed(reasoner, ax, true);
					entailed &= temp;
					if (!temp) {
						b.append(logTroubles(premiseOntology, true, entailed, ax, t, ax));
					}
				}
				Assert.assertEquals(b.toString(), true, entailed);
			}
				break;
			default:
				break;
		}
		premiseOntology.getOWLOntologyManager().removeOntologyChangeListener(
				(OWLOntologyChangeListener) reasoner);
	}

	public String logTroubles(OWLOntology o, boolean expected, boolean actual,
			OWLAxiom ax, TestClasses c, OWLAxiom axiom) {
		StringBuilder b = new StringBuilder();
		b.append("JUnitRunner.logTroubles() \t");
		b.append(c);
		b.append("\t");
		b.append(testId);
		b.append("\n ======================================\n");
		b.append(description);
		b.append("\nexpected: ");
		b.append(expected);
		b.append("\t actual: ");
		b.append(actual);
		if(axiom!=null) {
			b.append("\n for axiom: ");b.append(axiom.toString());
		}

		String string = b.toString();
		System.out.println(string);
		return string;
	}


	public void printPremise() throws Exception{
m.saveOntology(getPremise(), new OWLFunctionalSyntaxOntologyFormat(), new SystemOutDocumentTarget());

	}
	public void printConsequence() throws Exception{
		m.saveOntology(getConsequence(), new OWLFunctionalSyntaxOntologyFormat(), new SystemOutDocumentTarget());

			}
}
