package conformance;

import junit.framework.Assert;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class JUnitRunner {
	private static final int _10000 = 100000;
	private TestClasses t;
	private OWLReasonerFactory f;
	private String testId;
	private String premise;
	private String consequence;
	private String description;

	public JUnitRunner(String premise, String consequence, String testId,
			TestClasses t, String description) {
		this.testId = testId;
		this.premise = premise;
		this.consequence = consequence;
		this.description = description;
		this.t = t;
	}

	public void setReasonerFactory(OWLReasonerFactory f) {
		this.f = f;
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

	private boolean isEntailed(OWLReasoner reasoner, OWLAxiom conclusion,
			boolean expected) {
		try {
			return reasoner.isEntailed(conclusion);
		} catch (RuntimeException e) {
			e.printStackTrace(System.out);
			return !expected;
		}
	}

	public void run() {
		OWLOntology premiseOntology = null;
		OWLOntology conclusionOntology = null;
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		m.setSilentMissingImportsHandling(true);
		try {
			if (premise != null) {
				StringDocumentSource documentSource = new StringDocumentSource(
						premise);
				premiseOntology = m
						.loadOntologyFromOntologyDocument(documentSource);
				OWL2DLProfile profile = new OWL2DLProfile();
				OWLProfileReport report = profile
						.checkOntology(premiseOntology);
				if (report.getViolations().size() > 0) {
					System.out.println("JUnitRunner.run() " + testId);
					System.out
							.println("JUnitRunner.run() premise violations:\n"
									+ report.toString());
					throw new RuntimeException("errors!");
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			//System.out.println("JUnitRunner.run() premise:\n" + premise);
			throw new RuntimeException(e);
		}
		try {
			if (consequence != null) {
				StringDocumentSource documentSource = new StringDocumentSource(
						consequence);
				conclusionOntology = m
						.loadOntologyFromOntologyDocument(documentSource);
				OWL2DLProfile profile = new OWL2DLProfile();
				OWLProfileReport report = profile
						.checkOntology(conclusionOntology);
				if (report.getViolations().size() > 0) {
					System.out.println("JUnitRunner.run() " + testId
							+ report.getViolations().size());
					System.out
							.println("JUnitRunner.run() conclusion violations:\n"
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
		OWLReasoner reasoner = f.createReasoner(premiseOntology,
				new SimpleConfiguration(_10000));
		switch (t) {
			case CONSISTENCY: {
				boolean consistent = isConsistent(reasoner, true);
				if (!consistent) {
					Assert.assertEquals(
							b.toString()
									+ logTroubles(premiseOntology, true,
											consistent, null, t), true,
							consistent);
				}
			}
				break;
			case INCONSISTENCY: {
				boolean consistent = isConsistent(reasoner, false);
				if (consistent) {
					Assert.assertEquals(
							b.toString()
									+ logTroubles(premiseOntology, false,
											consistent, null, t), false,
							consistent);
				}
			}
				break;
			case NEGATIVE_IMPL: {
				boolean consistent = isConsistent(reasoner, true);
				if (!consistent) {
					Assert.assertEquals(
							b.toString()
									+ logTroubles(premiseOntology, true,
											consistent, null, t), true,
							consistent);
				}
				boolean entailed = false;
				for (OWLAxiom ax : conclusionOntology.getLogicalAxioms()) {
					boolean temp = isEntailed(reasoner, ax, false);
					entailed |= temp;
					if (temp) {
						b.append(logTroubles(premiseOntology, false, entailed,
								ax, t));
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
									+ logTroubles(premiseOntology, true,
											consistent, null, t), true,
							consistent);
				}
				boolean entailed = true;
				for (OWLAxiom ax : conclusionOntology.getLogicalAxioms()) {
					boolean temp = isEntailed(reasoner, ax, true);
					entailed &= temp;
					if (!temp) {
						b.append(logTroubles(premiseOntology, true, entailed,
								ax, t));
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
			OWLAxiom ax, TestClasses c) {
		StringBuilder b = new StringBuilder();
		b.append("JUnitRunner.logTroubles() \t");
		b.append(c);
		b.append("\t");
		b.append(testId);
		b.append(" ======================================\n");
		b.append(description);
		b.append("\nPremise:\n");
//		try {
//		o.getOWLOntologyManager().saveOntology(o, new OWLFunctionalSyntaxOntologyFormat(), new SystemOutDocumentTarget());
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
		for (OWLAxiom x : o.getAxioms()) {
			b.append(x);
			b.append("\n");
		}
		b.append("end premise\nexpected: ");
		b.append(expected);
		b.append("\t actual: ");
		b.append(actual);
		b.append("\n");
		if (ax != null) {
			b.append("JUnitRunner.logTroubles() conclusion");
			b.append("\n");
			b.append(ax);
			b.append("\n");
		}
		String string = b.toString();
		System.out.println(string);
		return string;
	}
	//	private void save(OWLOntology o, String string, File folder, String input,
	//			boolean expected, boolean actual) {
	//		if (o != null) {
	//			try {
	//				//				ByteArrayOutputStream fake=new ByteArrayOutputStream();
	//				//				o.getOWLOntologyManager().saveOntology(o, fake);
	//				//				
	//				FileOutputStream out = new FileOutputStream(new File(folder,
	//						string));
	//				//out.write(fake.toByteArray());
	//				out.write(("\n\nExpected value: " + expected
	//						+ "\nActual value: " + actual + "\n\n Ontology as received:\n\n")
	//						.getBytes());
	//				out.write(input.getBytes());
	//				out.close();
	//			} catch (Exception e) {
	//				e.printStackTrace();
	//			}
	//		}
	//	}
}
