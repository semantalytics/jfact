package uk.ac.manchester.cs.jfact;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.AxiomNotInProfileException;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.ClassExpressionNotInProfileException;
import org.semanticweb.owlapi.reasoner.FreshEntitiesException;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;
import org.semanticweb.owlapi.reasoner.impl.DefaultNode;
import org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNode;
import org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet;
import org.semanticweb.owlapi.util.Version;

import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.kernel.ExpressionManager;
import uk.ac.manchester.cs.jfact.kernel.NamedEntry;
import uk.ac.manchester.cs.jfact.kernel.ReasoningKernel;
import uk.ac.manchester.cs.jfact.kernel.actors.ClassPolicy;
import uk.ac.manchester.cs.jfact.kernel.actors.DataPropertyPolicy;
import uk.ac.manchester.cs.jfact.kernel.actors.IndividualPolicy;
import uk.ac.manchester.cs.jfact.kernel.actors.ObjectPropertyPolicy;
import uk.ac.manchester.cs.jfact.kernel.actors.TaxonomyActor;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ConceptExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.IndividualExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ObjectRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.voc.Vocabulary;

/**
 * Synchronization policy: all methods for OWLReasoner are synchronized, except
 * the methods which do not touch the kernel or only affect threadsafe data
 * structures. inner private classes are not synchronized since methods from
 * those classes cannot be invoked from outsize synchronized methods.
 *
 */
public final class JFactReasoner implements OWLReasoner,
		OWLOntologyChangeListener {
	private static final String REASONER_NAME = "JFact";
	private static final Version VERSION = new Version(0, 0, 0, 0);
	protected final AtomicBoolean interrupted = new AtomicBoolean(false);
	ReasoningKernel kernel;
	private ExpressionManager em;
	private static final EnumSet<InferenceType> supportedInferenceTypes = EnumSet
			.of(InferenceType.CLASS_ASSERTIONS, InferenceType.CLASS_HIERARCHY,
					InferenceType.DATA_PROPERTY_HIERARCHY,
					InferenceType.OBJECT_PROPERTY_HIERARCHY,
					InferenceType.SAME_INDIVIDUAL);
	private final OWLOntologyManager manager;
	private final OWLOntology rootOntology;
	private final BufferingMode bufferingMode;
	private final List<OWLOntologyChange> rawChanges = new ArrayList<OWLOntologyChange>();
	private final List<OWLAxiom> reasonerAxioms = new ArrayList<OWLAxiom>();
	private final long timeOut;
	private final OWLReasonerConfiguration configuration;
	private OWLDataFactory df;
	private TranslationMachinery translationMachinery;
	//holds the consistency status: true for consistent, false for inconsistent, null for not verified (or changes received)
	private Boolean consistencyVerified = null;

	public JFactReasoner(OWLOntology rootOntology,
			OWLReasonerConfiguration configuration, BufferingMode bufferingMode) {
		this.rootOntology = rootOntology;
		df = this.rootOntology.getOWLOntologyManager().getOWLDataFactory();
		kernel = new ReasoningKernel();
		em = kernel.getExpressionManager();
		this.bufferingMode = bufferingMode;
		this.configuration = configuration;
		timeOut = configuration.getTimeOut();
		manager = rootOntology.getOWLOntologyManager();
		for (OWLOntology ont : rootOntology.getImportsClosure()) {
			for (OWLAxiom ax : ont.getLogicalAxioms()) {
				reasonerAxioms.add(ax.getAxiomWithoutAnnotations());
			}
			for (OWLAxiom ax : ont.getAxioms(AxiomType.DECLARATION)) {
				reasonerAxioms.add(ax.getAxiomWithoutAnnotations());
			}
		}
		kernel.setTopBottomRoleNames(Vocabulary.TOP_OBJECT_PROPERTY,
				Vocabulary.BOTTOM_OBJECT_PROPERTY,
				Vocabulary.TOP_DATA_PROPERTY, Vocabulary.BOTTOM_DATA_PROPERTY);
		kernel.setProgressMonitor(configuration.getProgressMonitor());
		kernel.setInterruptedSwitch(interrupted);
		long millis = configuration.getTimeOut();
		if (millis == Long.MAX_VALUE) {
			millis = 0;
		}
		kernel.setOperationTimeout(millis);
		loadReasonerAxioms();
	}

	public synchronized Node<OWLClass> getEquivalentClasses(
			OWLClassExpression ce) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, ReasonerInterruptedException,
			TimeOutException {
		checkConsistency();
		Collection<ConceptExpression> pointers = askEquivalentClasses(translationMachinery
				.toClassPointer(ce));
		return translationMachinery.getClassExpressionTranslator()
				.getNodeFromPointers(pointers);
	}

	public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
			throws OWLException {
		handleRawOntologyChanges(changes);
	}

	protected OWLReasonerConfiguration getReasonerConfiguration() {
		return configuration;
	}

	public BufferingMode getBufferingMode() {
		return bufferingMode;
	}

	public long getTimeOut() {
		return timeOut;
	}

	public OWLOntology getRootOntology() {
		return rootOntology;
	}

	/**
	 * Handles raw ontology changes. If the reasoner is a buffering reasoner
	 * then the changes will be stored in a buffer. If the reasoner is a
	 * non-buffering reasoner then the changes will be automatically flushed
	 * through to the change filter and passed on to the reasoner.
	 *
	 * @param changes
	 *            The list of raw changes.
	 */
	private final static boolean log = false;

	private synchronized void handleRawOntologyChanges(
			List<? extends OWLOntologyChange> changes) {
		if (log) {
			System.out.println(Thread.currentThread().getName()
					+ " OWLReasonerBase.handleRawOntologyChanges() " + changes);
		}
		rawChanges.addAll(changes);
		// We auto-flush the changes if the reasoner is non-buffering
		if (bufferingMode.equals(BufferingMode.NON_BUFFERING)) {
			flush();
		}
	}

	public synchronized List<OWLOntologyChange> getPendingChanges() {
		return new ArrayList<OWLOntologyChange>(rawChanges);
	}

	public synchronized Set<OWLAxiom> getPendingAxiomAdditions() {
		if (rawChanges.size() > 0) {
			Set<OWLAxiom> added = new HashSet<OWLAxiom>();
			computeDiff(added, new HashSet<OWLAxiom>());
			return added;
		}
		return Collections.emptySet();
	}

	public synchronized Set<OWLAxiom> getPendingAxiomRemovals() {
		if (rawChanges.size() > 0) {
			Set<OWLAxiom> removed = new HashSet<OWLAxiom>();
			computeDiff(new HashSet<OWLAxiom>(), removed);
			return removed;
		}
		return Collections.emptySet();
	}

	/**
	 * Flushes the pending changes from the pending change list. The changes
	 * will be analysed to dermine which axioms have actually been added and
	 * removed from the imports closure of the root ontology and then the
	 * reasoner will be asked to handle these changes via the
	 * {@link #handleChanges(java.util.Set, java.util.Set)} method.
	 */
	public synchronized void flush() {
		// Process the changes
		if (rawChanges.size() > 0) {
			final Set<OWLAxiom> added = new HashSet<OWLAxiom>();
			final Set<OWLAxiom> removed = new HashSet<OWLAxiom>();
			computeDiff(added, removed);
			rawChanges.clear();
			if (!added.isEmpty() || !removed.isEmpty()) {
				reasonerAxioms.removeAll(removed);
				reasonerAxioms.addAll(added);
				// set the consistency status to not verified
				consistencyVerified = null;
				handleChanges(added, removed);
			}
		}
	}

	/**
	 * Computes a diff of what axioms have been added and what axioms have been
	 * removed from the list of pending changes. Note that even if the list of
	 * pending changes is non-empty then there may be no changes for the
	 * reasoner to deal with.
	 *
	 * @param added
	 *            The logical axioms that have been added to the imports closure
	 *            of the reasoner root ontology
	 * @param removed
	 *            The logical axioms that have been removed from the imports
	 *            closure of the reasoner root ontology
	 */
	private synchronized void computeDiff(Set<OWLAxiom> added,
			Set<OWLAxiom> removed) {
		for (OWLOntology ont : rootOntology.getImportsClosure()) {
			for (OWLAxiom ax : ont.getLogicalAxioms()) {
				if (!reasonerAxioms.contains(ax.getAxiomWithoutAnnotations())) {
					added.add(ax);
				}
			}
			for (OWLAxiom ax : ont.getAxioms(AxiomType.DECLARATION)) {
				if (!reasonerAxioms.contains(ax.getAxiomWithoutAnnotations())) {
					added.add(ax);
				}
			}
		}
		for (OWLAxiom ax : reasonerAxioms) {
			if (!rootOntology.containsAxiomIgnoreAnnotations(ax, true)) {
				removed.add(ax);
			}
		}
	}

	/**
	 * Gets the axioms that should be currently being reasoned over.
	 *
	 * @return A collections of axioms (not containing duplicates) that the
	 *         reasoner should be taking into consideration when reasoning. This
	 *         set of axioms many not correspond to the current state of the
	 *         imports closure of the reasoner root ontology if the reasoner is
	 *         buffered.
	 */
	private synchronized Collection<OWLAxiom> getReasonerAxioms() {
		return new ArrayList<OWLAxiom>(reasonerAxioms);
	}

	public FreshEntityPolicy getFreshEntityPolicy() {
		return configuration.getFreshEntityPolicy();
	}

	public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
		return configuration.getIndividualNodeSetPolicy();
	}

	protected OWLDataFactory getOWLDataFactory() {
		return df;
	}

	/**
	 * Asks the reasoner implementation to handle axiom additions and removals
	 * from the imports closure of the root ontology. The changes will not
	 * include annotation axiom additions and removals.
	 *
	 * @param addAxioms
	 *            The axioms to be added to the reasoner.
	 * @param removeAxioms
	 *            The axioms to be removed from the reasoner
	 */
	private synchronized void handleChanges(Set<OWLAxiom> addAxioms,
			Set<OWLAxiom> removeAxioms) {
		//kernel.startChanges();
		for (OWLAxiom ax_a : addAxioms) {
			translationMachinery.loadAxiom(ax_a);
		}
		for (OWLAxiom ax_r : removeAxioms) {
			translationMachinery.retractAxiom(ax_r);
		}
		//kernel.endChanges();
	}

	private synchronized void loadReasonerAxioms() {
		getReasonerConfiguration().getProgressMonitor().reasonerTaskStarted(
				ReasonerProgressMonitor.LOADING);
		getReasonerConfiguration().getProgressMonitor().reasonerTaskBusy();
		kernel.clearKB();
		translationMachinery = new TranslationMachinery(kernel, df);
		for (OWLAxiom ax : getReasonerAxioms()) {
			translationMachinery.loadAxiom(ax);
		}
		getReasonerConfiguration().getProgressMonitor().reasonerTaskStopped();
	}

	public String getReasonerName() {
		return REASONER_NAME;
	}

	public Version getReasonerVersion() {
		return VERSION;
	}

	public void interrupt() {
		interrupted.set(true);
	}

	// precompute inferences
	public synchronized void precomputeInferences(
			InferenceType... inferenceTypes)
			throws ReasonerInterruptedException, TimeOutException,
			InconsistentOntologyException {
		for (InferenceType it : inferenceTypes) {
			if (supportedInferenceTypes.contains(it)) {
				if (!kernel.isKBRealised()) {
					kernel.realiseKB();
				}
				return;
			}
		}
	}

	public boolean isPrecomputed(InferenceType inferenceType) {
		if (supportedInferenceTypes.contains(inferenceType)) {
			return kernel.isKBRealised();
		}
		return true;
	}

	public Set<InferenceType> getPrecomputableInferenceTypes() {
		return supportedInferenceTypes;
	}

	// consistency
	public synchronized boolean isConsistent()
			throws ReasonerInterruptedException, TimeOutException {
		if (consistencyVerified == null) {
			consistencyVerified = kernel.isKBConsistent();
		}
		return consistencyVerified;
	}

	private void checkConsistency() {
		if (interrupted.get()) {
			throw new ReasonerInterruptedException();
		}
		if (!isConsistent()) {
			throw new InconsistentOntologyException();
		}
	}

	public synchronized boolean isSatisfiable(OWLClassExpression classExpression)
			throws ReasonerInterruptedException, TimeOutException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		checkConsistency();
		return kernel.isSatisfiable(translationMachinery
				.toClassPointer(classExpression));
	}

	public Node<OWLClass> getUnsatisfiableClasses()
			throws ReasonerInterruptedException, TimeOutException,
			InconsistentOntologyException {
		return getBottomClassNode();
	}

	// entailments
	public synchronized boolean isEntailed(OWLAxiom axiom)
			throws ReasonerInterruptedException,
			UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		checkConsistency();
		//		if (rootOntology.containsAxiom(axiom, true)) {
		//			return true;
		//		}
		//TODO have the exception thrown from the entailment checker
		boolean entailed = axiom.accept(translationMachinery
				.getEntailmentChecker());
		//kernel.writeReasoningResult(LeveLogger.logger, 0);
		//		if(axiom instanceof OWLSubClassOfAxiom && rootOntology.getOntologyID().getOntologyIRI().toString().contains("AnnotatedSiemensStart")) {
		//		if(!((OWLSubClassOfAxiom) axiom).getSuperClass().isAnonymous() && !((OWLSubClassOfAxiom) axiom).getSubClass().isAnonymous()) {
		//			System.out.println("JFactReasoner.isEntailed() "+axiom);
		//		new Exception().printStackTrace(System.out);
		//		//System.out.println("JFactReasoner.isEntailed() "+axiom);
		//		}
		//		}
		return entailed;
	}

	public synchronized boolean isEntailed(Set<? extends OWLAxiom> axioms)
			throws ReasonerInterruptedException,
			UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		for (OWLAxiom ax : axioms) {
			if (!isEntailed(ax)) {
				return false;
			}
		}
		return true;
	}

	public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
		// FIXME!! check later
		return true;
	}

	// tracing
	/*
	 * Return tracing set (set of axioms that were participate in achieving result) for a given entailment.
	 * Return empty set if the axiom is not entailed.
	 */
	public synchronized Set<OWLAxiom> getTrace(OWLAxiom axiom) {
		kernel.needTracing();
		if (isEntailed(axiom)) {
			return translationMachinery.translateTAxiomSet(kernel.getTrace());
		}
		return Collections.emptySet();
	}

	// classes
	public Node<OWLClass> getTopClassNode() {
		return getEquivalentClasses(getOWLDataFactory().getOWLThing());
	}

	public Node<OWLClass> getBottomClassNode() {
		return getEquivalentClasses(getOWLDataFactory().getOWLNothing());
	}

	public synchronized NodeSet<OWLClass> getSubClasses(OWLClassExpression ce,
			boolean direct) throws ReasonerInterruptedException,
			TimeOutException, FreshEntitiesException,
			InconsistentOntologyException {
		checkConsistency();
		return translationMachinery.getClassExpressionTranslator()
				.getNodeSetFromPointers(
						askSubClasses(translationMachinery.toClassPointer(ce),
								direct));
	}

	public synchronized NodeSet<OWLClass> getSuperClasses(
			OWLClassExpression ce, boolean direct)
			throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, ReasonerInterruptedException,
			TimeOutException {
		checkConsistency();
		return translationMachinery
				.getClassExpressionTranslator()
				.getNodeSetFromPointers(
						askSuperClasses(
								translationMachinery.toClassPointer(ce), direct));
	}

	public synchronized NodeSet<OWLClass> getDisjointClasses(
			OWLClassExpression ce) {
		TaxonomyActor actor = new TaxonomyActor(em, new ClassPolicy());
		ConceptExpression p = translationMachinery.toClassPointer(ce);
		kernel.getDisjointConcepts(p, actor);
		return translationMachinery.getClassExpressionTranslator()
				.getNodeSetFromPointers(actor.getClassElements());
		//	OWLClassExpression negCE = df.getOWLObjectComplementOf(ce);
		//		DefaultNodeSet<OWLClass> nodeSet = new OWLClassNodeSet();
		//		Set<Node<OWLClass>> subClasses = getSubClasses(negCE, false).getNodes();
		//		if (subClasses.size() > 0) {
		//			nodeSet.addAllNodes(subClasses);
		//		}
		//		Node<OWLClass> equivalentClasses = getEquivalentClasses(negCE);
		//		if (equivalentClasses.getSize() > 0) {
		//			nodeSet.addNode(equivalentClasses);
		//		}
		//		return nodeSet;
	}

	// object properties
	public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
		return getEquivalentObjectProperties(getOWLDataFactory()
				.getOWLTopObjectProperty());
	}

	public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
		return getEquivalentObjectProperties(getOWLDataFactory()
				.getOWLBottomObjectProperty());
	}

	public synchronized NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(
			OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, ReasonerInterruptedException,
			TimeOutException {
		checkConsistency();
		return translationMachinery.getObjectPropertyTranslator()
				.getNodeSetFromPointers(
						askSubObjectProperties(translationMachinery
								.toObjectPropertyPointer(pe), direct));
	}

	public synchronized NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(
			OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, ReasonerInterruptedException,
			TimeOutException {
		checkConsistency();
		return translationMachinery.getObjectPropertyTranslator()
				.getNodeSetFromPointers(
						askSuperObjectProperties(translationMachinery
								.toObjectPropertyPointer(pe), direct));
	}

	public synchronized Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(
			OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, ReasonerInterruptedException,
			TimeOutException {
		checkConsistency();
		return translationMachinery.getObjectPropertyTranslator()
				.getNodeFromPointers(
						askEquivalentObjectProperties(translationMachinery
								.toObjectPropertyPointer(pe)));
	}

	public synchronized NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(
			OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, ReasonerInterruptedException,
			TimeOutException {
		checkConsistency();
		// TODO: incomplete
		OWLObjectPropertyNodeSet toReturn = new OWLObjectPropertyNodeSet();
		toReturn.addNode(getBottomObjectPropertyNode());
		return toReturn;
	}

	public synchronized Node<OWLObjectPropertyExpression> getInverseObjectProperties(
			OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, ReasonerInterruptedException,
			TimeOutException {
		checkConsistency();
		return translationMachinery.getObjectPropertyTranslator()
				.getNodeFromPointers(
						askEquivalentObjectProperties(translationMachinery
								.toObjectPropertyPointer(pe
										.getInverseProperty())));
	}

	public synchronized NodeSet<OWLClass> getObjectPropertyDomains(
			OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, ReasonerInterruptedException,
			TimeOutException {
		checkConsistency();
		ConceptExpression subClass = translationMachinery
				.toClassPointer(getOWLDataFactory().getOWLObjectSomeValuesFrom(
						pe, getOWLDataFactory().getOWLThing()));
		return translationMachinery.getClassExpressionTranslator()
				.getNodeSetFromPointers(askSuperClasses(subClass, direct));
	}

	public NodeSet<OWLClass> getObjectPropertyRanges(
			OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, ReasonerInterruptedException,
			TimeOutException {
		return getSuperClasses(
				getOWLDataFactory().getOWLObjectSomeValuesFrom(
						pe.getInverseProperty(),
						getOWLDataFactory().getOWLThing()), direct);
	}

	// data properties
	public Node<OWLDataProperty> getTopDataPropertyNode() {
		OWLDataPropertyNode toReturn=new OWLDataPropertyNode();
		toReturn.add(getOWLDataFactory().getOWLTopDataProperty());
		return toReturn;
	}

	public Node<OWLDataProperty> getBottomDataPropertyNode() {
		return getEquivalentDataProperties(getOWLDataFactory()
				.getOWLBottomDataProperty());
	}

	public synchronized NodeSet<OWLDataProperty> getSubDataProperties(
			OWLDataProperty pe, boolean direct)
			throws InconsistentOntologyException, ReasonerInterruptedException,
			TimeOutException {
		checkConsistency();
		return translationMachinery.getDataPropertyTranslator()
				.getNodeSetFromPointers(
						askSubDataProperties(
								translationMachinery.toDataPropertyPointer(pe),
								direct));
	}

	public synchronized NodeSet<OWLDataProperty> getSuperDataProperties(
			OWLDataProperty pe, boolean direct)
			throws InconsistentOntologyException, ReasonerInterruptedException,
			TimeOutException {
		checkConsistency();
		return translationMachinery.getDataPropertyTranslator()
				.getNodeSetFromPointers(
						askSuperDataProperties(
								translationMachinery.toDataPropertyPointer(pe),
								direct));
	}

	public synchronized Node<OWLDataProperty> getEquivalentDataProperties(
			OWLDataProperty pe) throws InconsistentOntologyException,
			ReasonerInterruptedException, TimeOutException {
		checkConsistency();

		return translationMachinery.getDataPropertyTranslator()
				.getNodeFromPointers(
						askEquivalentDataProperties(translationMachinery
								.toDataPropertyPointer(pe)));
	}

	public synchronized NodeSet<OWLDataProperty> getDisjointDataProperties(
			OWLDataPropertyExpression pe) throws InconsistentOntologyException,
			ReasonerInterruptedException, TimeOutException {
		checkConsistency();
		// TODO:
		return new OWLDataPropertyNodeSet();
	}

	public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty pe,
			boolean direct) throws InconsistentOntologyException,
			ReasonerInterruptedException, TimeOutException {
		return getSuperClasses(
				getOWLDataFactory().getOWLDataSomeValuesFrom(pe,
						getOWLDataFactory().getTopDatatype()), direct);
	}

	// individuals
	public synchronized NodeSet<OWLClass> getTypes(OWLNamedIndividual ind,
			boolean direct) throws InconsistentOntologyException,
			ReasonerInterruptedException, TimeOutException {
		checkConsistency();
		return translationMachinery.getClassExpressionTranslator()
				.getNodeSetFromPointers(
						askIndividualTypes(
								translationMachinery.toIndividualPointer(ind),
								direct));
	}

	public synchronized NodeSet<OWLNamedIndividual> getInstances(
			OWLClassExpression ce, boolean direct)
			throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, ReasonerInterruptedException,
			TimeOutException {
		checkConsistency();
		return translationMachinery
				.translateIndividualPointersToNodeSet(askInstances(
						translationMachinery.toClassPointer(ce), direct));
	}

	public synchronized NodeSet<OWLNamedIndividual> getObjectPropertyValues(
			OWLNamedIndividual ind, OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, ReasonerInterruptedException,
			TimeOutException {
		checkConsistency();
		return translationMachinery
				.translateIndividualPointersToNodeSet(askRelatedIndividuals(
						translationMachinery.toIndividualPointer(ind),
						translationMachinery.toObjectPropertyPointer(pe)));
	}

	public synchronized Set<OWLLiteral> getDataPropertyValues(
			OWLNamedIndividual ind, OWLDataProperty pe)
			throws InconsistentOntologyException, ReasonerInterruptedException,
			TimeOutException {
		// TODO:
		return Collections.emptySet();
	}

	public synchronized Node<OWLNamedIndividual> getSameIndividuals(
			OWLNamedIndividual ind) throws InconsistentOntologyException,
			ReasonerInterruptedException, TimeOutException {
		checkConsistency();
		return translationMachinery
				.getIndividualTranslator()
				.getNodeFromPointers(
						askSameAs(translationMachinery.toIndividualPointer(ind)));
	}

	public NodeSet<OWLNamedIndividual> getDifferentIndividuals(
			OWLNamedIndividual ind) throws InconsistentOntologyException,
			ReasonerInterruptedException, TimeOutException {
		OWLClassExpression ce = getOWLDataFactory().getOWLObjectOneOf(ind)
				.getObjectComplementOf();
		return getInstances(ce, false);
	}

	public synchronized void dispose() {
		manager.removeOntologyChangeListener(this);
		translationMachinery = null;
		kernel = null;
	}

	public void dumpClassHierarchy(LogAdapter pw, boolean includeBottomNode) {
		dumpSubClasses(getTopClassNode(), pw, 0, includeBottomNode);
	}

	private void dumpSubClasses(Node<OWLClass> node, LogAdapter pw, int depth,
			boolean includeBottomNode) {
		if (includeBottomNode || !node.isBottomNode()) {
			for (int i = 0; i < depth; i++) {
				pw.print("    ");
			}
			pw.println(node.toString());
			for (Node<OWLClass> sub : getSubClasses(
					node.getRepresentativeElement(), true)) {
				dumpSubClasses(sub, pw, depth + 1, includeBottomNode);
			}
		}
	}

	public Collection<Collection<ConceptExpression>> askSubClasses(
			ConceptExpression arg, boolean direct) {
		TaxonomyActor actor = new TaxonomyActor(em, new ClassPolicy());
		ConceptExpression p = arg;
		kernel.getSubConcepts(p, direct, actor);
		return actor.getClassElements();
	}

	public Collection<Collection<ConceptExpression>> askSuperClasses(
			ConceptExpression arg, boolean direct) {
		TaxonomyActor actor = new TaxonomyActor(em, new ClassPolicy());
		ConceptExpression p = arg;
		kernel.getSupConcepts(p, direct, actor);
		return actor.getClassElements();
	}

	public Collection<ConceptExpression> askEquivalentClasses(
			ConceptExpression arg) {
		TaxonomyActor actor = new TaxonomyActor(em, new ClassPolicy());
		kernel.getEquivalentConcepts(arg, actor);
		return actor.getClassSynonyms();
	}

	public Collection<ConceptExpression> askDisjointClasses(
			ConceptExpression arg) {
		TaxonomyActor actor = new TaxonomyActor(em, new ClassPolicy());
		kernel.getDisjointConcepts(arg, actor);
		return actor.getClassSynonyms();
	}

	public Collection<Collection<ObjectRoleExpression>> askSuperObjectProperties(
			ObjectRoleExpression arg, boolean direct) {
		TaxonomyActor actor = new TaxonomyActor(em, new ObjectPropertyPolicy());
		ObjectRoleExpression p = arg;
		kernel.getSupRoles(p, direct, actor);
		return actor.getObjectPropertyElements();
	}

	public Collection<Collection<ObjectRoleExpression>> askSubObjectProperties(
			ObjectRoleExpression arg, boolean direct) {
		TaxonomyActor actor = new TaxonomyActor(em, new ObjectPropertyPolicy());
		ObjectRoleExpression p = arg;
		kernel.getSubRoles(p, direct, actor);
		return actor.getObjectPropertyElements();
	}

	public Collection<ObjectRoleExpression> askEquivalentObjectProperties(
			ObjectRoleExpression arg) {
		TaxonomyActor actor = new TaxonomyActor(em, new ObjectPropertyPolicy());
		kernel.getEquivalentRoles(arg, actor);
		return actor.getObjectPropertySynonyms();
	}

	public Collection<Collection<ConceptExpression>> askObjectPropertyDomain(
			ObjectRoleExpression arg) {
		TaxonomyActor actor = new TaxonomyActor(em, new ClassPolicy());
		kernel.getRoleDomain(arg, true, actor);
		return actor.getClassElements();
	}

	public Collection<Collection<ConceptExpression>> askObjectPropertyRange(
			ObjectRoleExpression arg) {
		TaxonomyActor actor = new TaxonomyActor(em, new ClassPolicy());
		kernel.getRoleRange(arg, true, actor);
		return actor.getClassElements();
	}

	public Collection<Collection<DataRoleExpression>> askSuperDataProperties(
			DataRoleExpression arg, boolean direct) {
		TaxonomyActor actor = new TaxonomyActor(em, new DataPropertyPolicy());
		DataRoleExpression p = arg;
		kernel.getSupRoles(p, direct, actor);
		return actor.getDataPropertyElements();
	}

	public Collection<Collection<DataRoleExpression>> askSubDataProperties(
			DataRoleExpression arg, boolean direct) {
		TaxonomyActor actor = new TaxonomyActor(em, new DataPropertyPolicy());
		kernel.getSubRoles(arg, direct, actor);
		return actor.getDataPropertyElements();
	}

	public Collection<DataRoleExpression> askEquivalentDataProperties(
			DataRoleExpression arg) {
		TaxonomyActor actor = new TaxonomyActor(em, new DataPropertyPolicy());
		kernel.getEquivalentRoles(arg, actor);
		return actor.getDataPropertySynonyms();
	}

	public Collection<Collection<ConceptExpression>> askDataPropertyDomain(
			DataRoleExpression arg) {
		TaxonomyActor actor = new TaxonomyActor(em, new ClassPolicy());
		kernel.getRoleDomain(arg, true, actor);
		return actor.getClassElements();
	}

	public Collection<Collection<ConceptExpression>> askIndividualTypes(
			IndividualExpression arg, boolean direct) {
		TaxonomyActor actor = new TaxonomyActor(em, new ClassPolicy());
		IndividualExpression p = arg;
		kernel.getTypes(p, direct, actor);
		return actor.getClassElements();
	}

	public Collection<ObjectRoleExpression> askObjectProperties(
			IndividualExpression arg) {
		List<NamedEntry> Rs = new ArrayList<NamedEntry>();
		kernel.getRelatedRoles(arg, Rs, false, false);
		List<ObjectRoleExpression> acc = new ArrayList<ObjectRoleExpression>();
		for (NamedEntry p : Rs) {
			ObjectRoleExpression oName = em.objectRole(p.getName());
			acc.add(oName);
		}
		return acc;
	}

	public Collection<IndividualExpression> askRelatedIndividuals(
			IndividualExpression arg1, ObjectRoleExpression arg2) {
		List<NamedEntry> Js = new ArrayList<NamedEntry>();
		kernel.getRoleFillers(arg1, arg2, Js);
		List<IndividualExpression> acc = new ArrayList<IndividualExpression>();
		for (NamedEntry p : Js) {
			acc.add(em.individual(p.getName()));
		}
		return acc;
	}

	public Collection<DataRoleExpression> askDataProperties(
			IndividualExpression arg) {
		List<NamedEntry> Rs = new ArrayList<NamedEntry>();
		kernel.getRelatedRoles(arg, Rs, true, false);
		List<DataRoleExpression> acc = new ArrayList<DataRoleExpression>();
		for (NamedEntry p : Rs) {
			acc.add(em.dataRole(p.getName()));
		}
		return acc;
	}

	public Collection<IndividualExpression> askInstances(ConceptExpression arg,
			boolean direct) {
		TaxonomyActor actor = new TaxonomyActor(em, new IndividualPolicy(true));
		ConceptExpression p = arg;
		if (direct) {
			kernel.getDirectInstances(p, actor);
		} else {
			kernel.getInstances(p, actor);
		}
		return actor.getPlainIndividualElements();
	}

	public Collection<Collection<IndividualExpression>> askInstancesGrouped(
			ConceptExpression arg, boolean direct) {
		TaxonomyActor actor = new TaxonomyActor(em, new IndividualPolicy(false));
		ConceptExpression p = arg;
		if (direct) {
			kernel.getDirectInstances(p, actor);
		} else {
			kernel.getInstances(p, actor);
		}
		return actor.getIndividualElements();
	}

	public Collection<IndividualExpression> askSameAs(IndividualExpression arg) {
		TaxonomyActor actor = new TaxonomyActor(em, new IndividualPolicy(true));
		kernel.getSameAs(arg, actor);
		return actor.getIndividualSynonyms();
	}
}
