package uk.ac.manchester.cs.jfact.kernel;

import org.semanticweb.owlapi.reasoner.OWLReasonerRuntimeException;

public class ReasonerFreshEntityException extends OWLReasonerRuntimeException {
	public ReasonerFreshEntityException() {
	}

	public ReasonerFreshEntityException(String s) {
		super(s);
	}

	public ReasonerFreshEntityException(String s, Throwable t) {
		super(s, t);
	}

	public ReasonerFreshEntityException(Throwable t) {
		super(t);
	}
}
