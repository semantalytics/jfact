package conformance;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Set;

import junit.framework.Assert;

import org.semanticweb.owlapi.api.test.alternate.ProfileValidationTestCase;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLRuntimeException;

import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class JUnitRunnerFromFile extends JUnitRunner {
	public static String readFile(File f) {
		StringBuilder b = new StringBuilder();
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));
			String l = r.readLine();
			while (l != null) {
				b.append(l);
				b.append('\n');
				l = r.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return b.toString();
	}

	public JUnitRunnerFromFile(File premise, File consequence, String testId,
			TestClasses t, String description) {
		super(readFile(premise), readFile(consequence), testId, t, description);
	}

}
