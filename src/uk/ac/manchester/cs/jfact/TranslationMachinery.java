package uk.ac.manchester.cs.jfact;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataRangeVisitorEx;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;
import org.semanticweb.owlapi.reasoner.impl.DefaultNode;
import org.semanticweb.owlapi.reasoner.impl.DefaultNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNode;
import org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLDatatypeNode;
import org.semanticweb.owlapi.reasoner.impl.OWLDatatypeNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNode;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNode;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet;
import org.semanticweb.owlapi.vocab.OWLFacet;

import uk.ac.manchester.cs.jfact.kernel.ExpressionManager;
import uk.ac.manchester.cs.jfact.kernel.ReasoningKernel;
import uk.ac.manchester.cs.jfact.kernel.datatype.DataValue;
import uk.ac.manchester.cs.jfact.kernel.datatype.Datatypes;
import uk.ac.manchester.cs.jfact.kernel.dl.DataTypeName;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Axiom;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ConceptExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataTypeExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Entity;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Expression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.FacetExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.IndividualExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ObjectRoleComplexExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ObjectRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.voc.Vocabulary;

public final class TranslationMachinery {
	private volatile AxiomTranslator axiomTranslator;
	private volatile ClassExpressionTranslator classExpressionTranslator;
	private volatile DataRangeTranslator dataRangeTranslator;
	private volatile ObjectPropertyTranslator objectPropertyTranslator;
	private volatile DataPropertyTranslator dataPropertyTranslator;
	private volatile IndividualTranslator individualTranslator;
	private volatile EntailmentChecker entailmentChecker;
	private final Map<OWLAxiom, Axiom> axiom2PtrMap = new HashMap<OWLAxiom, Axiom>();
	private final Map<Axiom, OWLAxiom> ptr2AxiomMap = new HashMap<Axiom, OWLAxiom>();
	protected final ReasoningKernel kernel;
	protected final ExpressionManager em;
	protected final OWLDataFactory df;

	public TranslationMachinery(ReasoningKernel kernel, OWLDataFactory df) {
		this.kernel = kernel;
		em = kernel.getExpressionManager();
		this.df = df;
		axiomTranslator = new AxiomTranslator();
		classExpressionTranslator = new ClassExpressionTranslator();
		dataRangeTranslator = new DataRangeTranslator();
		objectPropertyTranslator = new ObjectPropertyTranslator();
		dataPropertyTranslator = new DataPropertyTranslator();
		individualTranslator = new IndividualTranslator();
		entailmentChecker = new EntailmentChecker();
	}

	public DataTypeName getBuiltInDataType(String DTName) {
		return new DataTypeName(Datatypes.getBuiltInDataType(DTName));
	}

	public ObjectRoleExpression getTopObjectProperty() {
		return em.objectRole(Vocabulary.TOP_OBJECT_PROPERTY);
	}

	public ObjectRoleExpression getBottomObjectProperty() {
		return em.objectRole(Vocabulary.BOTTOM_OBJECT_PROPERTY);
	}

	public DataRoleExpression getTopDataProperty() {
		return em.dataRole(Vocabulary.TOP_DATA_PROPERTY);
	}

	public DataRoleExpression getBottomDataProperty() {
		return em.dataRole(Vocabulary.BOTTOM_DATA_PROPERTY);
	}

	public void loadAxiom(OWLAxiom axiom) {
		final Axiom axiomPointer = axiom.accept(axiomTranslator);
		if (axiomPointer != null) {
			axiom2PtrMap.put(axiom, axiomPointer);
		}
	}

	public void retractAxiom(OWLAxiom axiom) {
		final Axiom ptr = axiom2PtrMap.get(axiom);
		if (ptr != null) {
			kernel.retract(ptr);
			axiom2PtrMap.remove(axiom);
		}
	}

	protected ConceptExpression toClassPointer(OWLClassExpression classExpression) {
		return classExpression.accept(classExpressionTranslator);
	}

	protected DataExpression toDataTypeExpressionPointer(OWLDataRange dataRange) {
		return dataRange.accept(dataRangeTranslator);
	}

	protected ObjectRoleExpression toObjectPropertyPointer(OWLObjectPropertyExpression propertyExpression) {
		OWLObjectPropertyExpression simp = propertyExpression.getSimplified();
		if (simp.isAnonymous()) {
			OWLObjectInverseOf inv = (OWLObjectInverseOf) simp;
			return em.inverse(objectPropertyTranslator.getPointerFromEntity(inv.getInverse().asOWLObjectProperty()));
		} else {
			return objectPropertyTranslator.getPointerFromEntity(simp.asOWLObjectProperty());
		}
	}

	protected DataRoleExpression toDataPropertyPointer(OWLDataPropertyExpression propertyExpression) {
		return dataPropertyTranslator.getPointerFromEntity(propertyExpression.asOWLDataProperty());
	}

	protected synchronized IndividualExpression toIndividualPointer(OWLIndividual individual) {
		if (!individual.isAnonymous()) {
			return individualTranslator.getPointerFromEntity(individual.asOWLNamedIndividual());
		} else {
			return em.individual(individual.toStringID());
		}
	}

	protected synchronized DataTypeExpression toDataTypePointer(OWLDatatype datatype) {
		if (datatype == null) {
			throw new NullPointerException();
		}
		return getBuiltInDataType(datatype.toStringID());
	}

	protected synchronized DataValue toDataValuePointer(OWLLiteral literal) {
		String value = literal.getLiteral();
		if (literal.isRDFPlainLiteral()) {
			value = value + "@" + literal.getLang();
		}
		return em.dataValue(value, toDataTypePointer(literal.getDatatype()));
	}

	protected NodeSet<OWLNamedIndividual> translateIndividualPointersToNodeSet(Iterable<IndividualExpression> pointers) {
		OWLNamedIndividualNodeSet ns = new OWLNamedIndividualNodeSet();
		for (IndividualExpression pointer : pointers) {
			if (pointer != null) {
				OWLNamedIndividual ind = individualTranslator.getEntityFromPointer(pointer);
				ns.addEntity(ind);
			}
		}
		return ns;
	}

	protected synchronized List<Expression> translateIndividualSet(Set<OWLIndividual> inds) {
		List<Expression> l = new ArrayList<Expression>();
		for (OWLIndividual ind : inds) {
			l.add(toIndividualPointer(ind));
		}
		return l;
	}

	public final class EntailmentChecker implements OWLAxiomVisitorEx<Boolean> {
		public EntailmentChecker() {
		}

		public Boolean visit(OWLSubClassOfAxiom axiom) {
			if (axiom.getSuperClass().equals(df.getOWLThing()) || axiom.getSubClass().equals(df.getOWLNothing())) {
				return true;
			}
			return kernel.isSubsumedBy(toClassPointer(axiom.getSubClass()), toClassPointer(axiom.getSuperClass()));
		}

		public Boolean visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
			return axiom.asOWLSubClassOfAxiom().accept(this);
		}

		public Boolean visit(OWLAsymmetricObjectPropertyAxiom axiom) {
			return kernel.isAsymmetric(toObjectPropertyPointer(axiom.getProperty()));
		}

		public Boolean visit(OWLReflexiveObjectPropertyAxiom axiom) {
			return kernel.isReflexive(toObjectPropertyPointer(axiom.getProperty()));
		}

		public Boolean visit(OWLDisjointClassesAxiom axiom) {
			Set<OWLClassExpression> classExpressions = axiom.getClassExpressions();
			if (classExpressions.size() == 2) {
				Iterator<OWLClassExpression> it = classExpressions.iterator();
				return kernel.isDisjoint(toClassPointer(it.next()), toClassPointer(it.next()));
			} else {
				for (OWLAxiom ax : axiom.asOWLSubClassOfAxioms()) {
					if (!ax.accept(this)) {
						return false;
					}
				}
				return true;
			}
		}

		public Boolean visit(OWLDataPropertyDomainAxiom axiom) {
			return axiom.asOWLSubClassOfAxiom().accept(this);
		}

		public Boolean visit(OWLObjectPropertyDomainAxiom axiom) {
			return axiom.asOWLSubClassOfAxiom().accept(this);
		}

		public Boolean visit(OWLEquivalentObjectPropertiesAxiom axiom) {
			for (OWLAxiom ax : axiom.asSubObjectPropertyOfAxioms()) {
				if (!ax.accept(this)) {
					return false;
				}
			}
			return true;
		}

		public Boolean visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
			return axiom.asOWLSubClassOfAxiom().accept(this);
		}

		public Boolean visit(OWLDifferentIndividualsAxiom axiom) {
			for (OWLSubClassOfAxiom ax : axiom.asOWLSubClassOfAxioms()) {
				if (!ax.accept(this)) {
					return false;
				}
			}
			return true;
		}

		// TODO: this check is incomplete
		public Boolean visit(OWLDisjointDataPropertiesAxiom axiom) {
			List<OWLDataPropertyExpression> l = new ArrayList<OWLDataPropertyExpression>(axiom.getProperties());
			for (int i = 0; i < l.size() - 1; i++) {
				for (int j = i + 1; j < l.size(); j++) {
					if (!kernel.isDisjointRoles(toDataPropertyPointer(l.get(i)), toDataPropertyPointer(l.get(i)))) {
						return false;
					}
				}
			}
			return true;
		}

		public Boolean visit(OWLDisjointObjectPropertiesAxiom axiom) {
			List<OWLObjectPropertyExpression> l = new ArrayList<OWLObjectPropertyExpression>(axiom.getProperties());
			for (int i = 0; i < l.size() - 1; i++) {
				for (int j = i + 1; j < l.size(); j++) {
					if (!kernel.isDisjointRoles(toObjectPropertyPointer(l.get(i)), toObjectPropertyPointer(l.get(i)))) {
						return false;
					}
				}
			}
			return true;
		}

		public Boolean visit(OWLObjectPropertyRangeAxiom axiom) {
			return axiom.asOWLSubClassOfAxiom().accept(this);
		}

		public Boolean visit(OWLObjectPropertyAssertionAxiom axiom) {
			return axiom.asOWLSubClassOfAxiom().accept(this);
		}

		public Boolean visit(OWLFunctionalObjectPropertyAxiom axiom) {
			return kernel.isFunctional(toObjectPropertyPointer(axiom.getProperty()));
		}

		public Boolean visit(OWLSubObjectPropertyOfAxiom axiom) {
			return kernel.isSubRoles(toObjectPropertyPointer(axiom.getSubProperty()), toObjectPropertyPointer(axiom.getSuperProperty()));
		}

		public Boolean visit(OWLDisjointUnionAxiom axiom) {
			return axiom.getOWLEquivalentClassesAxiom().accept(this) && axiom.getOWLDisjointClassesAxiom().accept(this);
		}

		public Boolean visit(OWLDeclarationAxiom axiom) {
			return false;
		}

		public Boolean visit(OWLAnnotationAssertionAxiom axiom) {
			return false;
		}

		public Boolean visit(OWLSymmetricObjectPropertyAxiom axiom) {
			return kernel.isSymmetric(toObjectPropertyPointer(axiom.getProperty()));
		}

		public Boolean visit(OWLDataPropertyRangeAxiom axiom) {
			return axiom.asOWLSubClassOfAxiom().accept(this);
		}

		public Boolean visit(OWLFunctionalDataPropertyAxiom axiom) {
			return kernel.isFunctional(toDataPropertyPointer(axiom.getProperty()));
		}

		public Boolean visit(OWLEquivalentDataPropertiesAxiom axiom) {
			/*	this is not implemented in OWL API
						for (OWLAxiom ax : axiom.asSubDataPropertyOfAxioms()) {
			                if (!ax.accept(this)) {
			                    return false;
			                }
			            }
			            return true;
			*/
			return null;
		}

		public Boolean visit(OWLClassAssertionAxiom axiom) {
			return kernel.isInstance(toIndividualPointer(axiom.getIndividual()), toClassPointer(axiom.getClassExpression()));
		}

		public Boolean visit(OWLEquivalentClassesAxiom axiom) {
			Set<OWLClassExpression> classExpressionSet = axiom.getClassExpressions();
			if (classExpressionSet.size() == 2) {
				Iterator<OWLClassExpression> it = classExpressionSet.iterator();
				return kernel.isEquivalent(toClassPointer(it.next()), toClassPointer(it.next()));
			} else {
				for (OWLAxiom ax : axiom.asOWLSubClassOfAxioms()) {
					if (!ax.accept(this)) {
						return false;
					}
				}
				return true;
			}
		}

		public Boolean visit(OWLDataPropertyAssertionAxiom axiom) {
			return axiom.asOWLSubClassOfAxiom().accept(this);
		}

		public Boolean visit(OWLTransitiveObjectPropertyAxiom axiom) {
			return kernel.isTransitive(toObjectPropertyPointer(axiom.getProperty()));
		}

		public Boolean visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
			return kernel.isIrreflexive(toObjectPropertyPointer(axiom.getProperty()));
		}

		// TODO: this is incomplete
		public Boolean visit(OWLSubDataPropertyOfAxiom axiom) {
			return kernel.isSubRoles(toDataPropertyPointer(axiom.getSubProperty()), toDataPropertyPointer(axiom.getSuperProperty()));
		}

		public Boolean visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
			return kernel.isInverseFunctional(toObjectPropertyPointer(axiom.getProperty()));
		}

		public Boolean visit(OWLSameIndividualAxiom axiom) {
			for (OWLSameIndividualAxiom ax : axiom.asPairwiseAxioms()) {
				Iterator<OWLIndividual> it = ax.getIndividuals().iterator();
				OWLIndividual indA = it.next();
				OWLIndividual indB = it.next();
				if (!kernel.isSameIndividuals(toIndividualPointer(indA), toIndividualPointer(indB))) {
					return false;
				}
			}
			return true;
		}

		public Boolean visit(OWLSubPropertyChainOfAxiom axiom) {
			List<Expression> l = new ArrayList<Expression>();
			for (OWLObjectPropertyExpression p : axiom.getPropertyChain()) {
				l.add(toObjectPropertyPointer(p));
			}
			//kernel.closeArgList();
			return kernel.isSubChain(toObjectPropertyPointer(axiom.getSuperProperty()), l);
		}

		public Boolean visit(OWLInverseObjectPropertiesAxiom axiom) {
			for (OWLAxiom ax : axiom.asSubObjectPropertyOfAxioms()) {
				if (!ax.accept(this)) {
					return false;
				}
			}
			return true;
		}

		public Boolean visit(OWLHasKeyAxiom axiom) {
			// FIXME!! unsupported by FaCT++ ATM
			return null;
		}

		public Boolean visit(OWLDatatypeDefinitionAxiom axiom) {
			// FIXME!! unsupported by FaCT++ ATM
			return null;
		}

		public Boolean visit(SWRLRule rule) {
			// FIXME!! unsupported by FaCT++ ATM
			return null;
		}

		public Boolean visit(OWLSubAnnotationPropertyOfAxiom axiom) {
			return false;
		}

		public Boolean visit(OWLAnnotationPropertyDomainAxiom axiom) {
			return false;
		}

		public Boolean visit(OWLAnnotationPropertyRangeAxiom axiom) {
			return false;
		}
	}

	abstract class OWLEntityTranslator<E extends OWLObject, T extends Entity> {
		private final Map<E, T> entity2dlentity = new HashMap<E, T>();
		private final Map<T, E> dlentity2entity = new HashMap<T, E>();

		protected final void fillMaps(E entity, T dlentity) {
			entity2dlentity.put(entity, dlentity);
			dlentity2entity.put(dlentity, entity);
		}

		protected OWLEntityTranslator() {
			E topEntity = getTopEntity();
			if (topEntity != null) {
				fillMaps(topEntity, getTopEntityPointer());
			}
			E bottomEntity = getBottomEntity();
			if (bottomEntity != null) {
				fillMaps(bottomEntity, getBottomEntityPointer());
			}
		}

		protected T registerNewEntity(E entity) {
			T pointer = createPointerForEntity(entity);
			fillMaps(entity, pointer);
			return pointer;
		}

		public final E getEntityFromPointer(T pointer) {
			return dlentity2entity.get(pointer);
		}

		public final T getPointerFromEntity(E entity) {
			T pointer = entity2dlentity.get(entity);
			if (pointer == null) {
				pointer = registerNewEntity(entity);
			}
			return pointer;
		}

		public Node<E> getNodeFromPointers(Collection<T> pointers) {
			DefaultNode<E> node = createDefaultNode();
			for (T pointer : pointers) {
				node.add(getEntityFromPointer(pointer));
			}
			return node;
		}

		public NodeSet<E> getNodeSetFromPointers(Collection<Collection<T>> pointers) {
			DefaultNodeSet<E> nodeSet = createDefaultNodeSet();
			for (Collection<T> pointerArray : pointers) {
				nodeSet.addNode(getNodeFromPointers(pointerArray));
			}
			return nodeSet;
		}

		protected abstract DefaultNode<E> createDefaultNode();

		protected abstract DefaultNodeSet<E> createDefaultNodeSet();

		protected abstract T getTopEntityPointer();

		protected abstract T getBottomEntityPointer();

		protected abstract T createPointerForEntity(E entity);

		protected abstract E getTopEntity();

		protected abstract E getBottomEntity();
	}

	final class ObjectPropertyTranslator extends OWLEntityTranslator<OWLObjectPropertyExpression, ObjectRoleExpression> {
		public ObjectPropertyTranslator() {
		}

		@Override
		protected ObjectRoleExpression getTopEntityPointer() {
			return getTopObjectProperty();
		}

		@Override
		protected ObjectRoleExpression getBottomEntityPointer() {
			return getBottomObjectProperty();
		}

		@Override
		protected ObjectRoleExpression registerNewEntity(OWLObjectPropertyExpression entity) {
			ObjectRoleExpression pointer = createPointerForEntity(entity);
			fillMaps(entity, pointer);
			entity = entity.getInverseProperty().getSimplified();
			fillMaps(entity, createPointerForEntity(entity));
			return pointer;
		}

		@Override
		protected ObjectRoleExpression createPointerForEntity(OWLObjectPropertyExpression entity) {
			// FIXME!! think later!!
			ObjectRoleExpression p = em.objectRole(entity.getNamedProperty().toStringID());
			if (entity.isAnonymous()) {
				p = em.inverse(p);
			}
			return p;
		}

		@Override
		protected OWLObjectProperty getTopEntity() {
			return df.getOWLTopObjectProperty();
		}

		@Override
		protected OWLObjectProperty getBottomEntity() {
			return df.getOWLBottomObjectProperty();
		}

		@Override
		protected DefaultNode<OWLObjectPropertyExpression> createDefaultNode() {
			return new OWLObjectPropertyNode();
		}

		@Override
		protected DefaultNodeSet<OWLObjectPropertyExpression> createDefaultNodeSet() {
			return new OWLObjectPropertyNodeSet();
		}
	}

	final class ComplexObjectPropertyTranslator extends OWLEntityTranslator<OWLObjectPropertyExpression, ObjectRoleComplexExpression> {
		public ComplexObjectPropertyTranslator() {
		}

		@Override
		protected ObjectRoleComplexExpression getTopEntityPointer() {
			return getTopObjectProperty();
		}

		@Override
		protected ObjectRoleComplexExpression getBottomEntityPointer() {
			return getBottomObjectProperty();
		}

		@Override
		protected ObjectRoleComplexExpression registerNewEntity(OWLObjectPropertyExpression entity) {
			ObjectRoleComplexExpression pointer = createPointerForEntity(entity);
			fillMaps(entity, pointer);
			entity = entity.getInverseProperty().getSimplified();
			fillMaps(entity, createPointerForEntity(entity));
			return pointer;
		}

		@Override
		protected ObjectRoleComplexExpression createPointerForEntity(OWLObjectPropertyExpression entity) {
			ObjectRoleComplexExpression p = em.objectRole(entity.getNamedProperty().toStringID());
			return p;
		}

		@Override
		protected OWLObjectProperty getTopEntity() {
			return df.getOWLTopObjectProperty();
		}

		@Override
		protected OWLObjectProperty getBottomEntity() {
			return df.getOWLBottomObjectProperty();
		}

		@Override
		protected DefaultNode<OWLObjectPropertyExpression> createDefaultNode() {
			return new OWLObjectPropertyNode();
		}

		@Override
		protected DefaultNodeSet<OWLObjectPropertyExpression> createDefaultNodeSet() {
			return new OWLObjectPropertyNodeSet();
		}
	}

	final class AxiomTranslator implements OWLAxiomVisitorEx<Axiom> {
		protected final class DeclarationVisitorEx implements OWLEntityVisitorEx<Axiom> {
			public Axiom visit(OWLClass cls) {
				return kernel.declare(toClassPointer(cls));
			}

			public Axiom visit(OWLObjectProperty property) {
				return kernel.declare(toObjectPropertyPointer(property));
			}

			public Axiom visit(OWLDataProperty property) {
				return kernel.declare(toDataPropertyPointer(property));
			}

			public Axiom visit(OWLNamedIndividual individual) {
				return kernel.declare(toIndividualPointer(individual));
			}

			public Axiom visit(OWLDatatype datatype) {
				return kernel.declare(toDataTypePointer(datatype));
			}

			public Axiom visit(OWLAnnotationProperty property) {
				return null;
			}
		}

		private final DeclarationVisitorEx v;

		public AxiomTranslator() {
			v = new DeclarationVisitorEx();
		}

		public Axiom visit(OWLSubClassOfAxiom axiom) {
			return kernel.impliesConcepts(toClassPointer(axiom.getSubClass()), toClassPointer(axiom.getSuperClass()));
		}

		public Axiom visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
			return kernel.relatedToNot(toIndividualPointer(axiom.getSubject()), toObjectPropertyPointer(axiom.getProperty()), toIndividualPointer(axiom.getObject()));
		}

		public Axiom visit(OWLAsymmetricObjectPropertyAxiom axiom) {
			return kernel.setAsymmetric(toObjectPropertyPointer(axiom.getProperty()));
		}

		public Axiom visit(OWLReflexiveObjectPropertyAxiom axiom) {
			return kernel.setReflexive(toObjectPropertyPointer(axiom.getProperty()));
		}

		public Axiom visit(OWLDisjointClassesAxiom axiom) {
			return kernel.disjointConcepts(translateClassExpressionSet(axiom.getClassExpressions()));
		}

		private List<Expression> translateClassExpressionSet(Set<OWLClassExpression> classExpressions) {
			List<Expression> l = new ArrayList<Expression>();
			for (OWLClassExpression ce : classExpressions) {
				l.add(toClassPointer(ce));
			}
			return l;
		}

		public Axiom visit(OWLDataPropertyDomainAxiom axiom) {
			return kernel.setDDomain(toDataPropertyPointer(axiom.getProperty()), toClassPointer(axiom.getDomain()));
		}

		public Axiom visit(OWLObjectPropertyDomainAxiom axiom) {
			return kernel.setODomain(toObjectPropertyPointer(axiom.getProperty()), toClassPointer(axiom.getDomain()));
		}

		public Axiom visit(OWLEquivalentObjectPropertiesAxiom axiom) {
			return kernel.equalORoles(translateObjectPropertySet(axiom.getProperties()));
		}

		private List<Expression> translateObjectPropertySet(Collection<OWLObjectPropertyExpression> properties) {
			List<Expression> l = new ArrayList<Expression>();
			for (OWLObjectPropertyExpression property : properties) {
				l.add(toObjectPropertyPointer(property));
			}
			return l;
		}

		public Axiom visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
			return kernel.valueOfNot(toIndividualPointer(axiom.getSubject()), toDataPropertyPointer(axiom.getProperty()), toDataValuePointer(axiom.getObject()));
		}

		public Axiom visit(OWLDifferentIndividualsAxiom axiom) {
			return kernel.processDifferent(translateIndividualSet(axiom.getIndividuals()));
		}

		public Axiom visit(OWLDisjointDataPropertiesAxiom axiom) {
			return kernel.disjointDRoles(translateDataPropertySet(axiom.getProperties()));
		}

		private List<Expression> translateDataPropertySet(Set<OWLDataPropertyExpression> properties) {
			List<Expression> l = new ArrayList<Expression>();
			for (OWLDataPropertyExpression property : properties) {
				l.add(toDataPropertyPointer(property));
			}
			return l;
		}

		public Axiom visit(OWLDisjointObjectPropertiesAxiom axiom) {
			return kernel.disjointORoles(translateObjectPropertySet(axiom.getProperties()));
		}

		public Axiom visit(OWLObjectPropertyRangeAxiom axiom) {
			return kernel.setORange(toObjectPropertyPointer(axiom.getProperty()), toClassPointer(axiom.getRange()));
		}

		public Axiom visit(OWLObjectPropertyAssertionAxiom axiom) {
			return kernel.relatedTo(toIndividualPointer(axiom.getSubject()), toObjectPropertyPointer(axiom.getProperty()), toIndividualPointer(axiom.getObject()));
		}

		public Axiom visit(OWLFunctionalObjectPropertyAxiom axiom) {
			return kernel.setOFunctional(toObjectPropertyPointer(axiom.getProperty()));
		}

		public Axiom visit(OWLSubObjectPropertyOfAxiom axiom) {
			return kernel.impliesORoles(toObjectPropertyPointer(axiom.getSubProperty()), toObjectPropertyPointer(axiom.getSuperProperty()));
		}

		public Axiom visit(OWLDisjointUnionAxiom axiom) {
			return kernel.disjointUnion(toClassPointer(axiom.getOWLClass()), translateClassExpressionSet(axiom.getClassExpressions()));
		}

		public Axiom visit(OWLDeclarationAxiom axiom) {
			OWLEntity entity = axiom.getEntity();
			return entity.accept(v);
		}

		public Axiom visit(OWLAnnotationAssertionAxiom axiom) {
			// Ignore
			return null;
		}

		public Axiom visit(OWLSymmetricObjectPropertyAxiom axiom) {
			return kernel.setSymmetric(toObjectPropertyPointer(axiom.getProperty()));
		}

		public Axiom visit(OWLDataPropertyRangeAxiom axiom) {
			return kernel.setDRange(toDataPropertyPointer(axiom.getProperty()), toDataTypeExpressionPointer(axiom.getRange()));
		}

		public Axiom visit(OWLFunctionalDataPropertyAxiom axiom) {
			return kernel.setDFunctional(toDataPropertyPointer(axiom.getProperty()));
		}

		public Axiom visit(OWLEquivalentDataPropertiesAxiom axiom) {
			return kernel.equalDRoles(translateDataPropertySet(axiom.getProperties()));
		}

		public Axiom visit(OWLClassAssertionAxiom axiom) {
			return kernel.instanceOf(toIndividualPointer(axiom.getIndividual()), toClassPointer(axiom.getClassExpression()));
		}

		public Axiom visit(OWLEquivalentClassesAxiom axiom) {
			return kernel.equalConcepts(translateClassExpressionSet(axiom.getClassExpressions()));
		}

		public Axiom visit(OWLDataPropertyAssertionAxiom axiom) {
			return kernel.valueOf(toIndividualPointer(axiom.getSubject()), toDataPropertyPointer(axiom.getProperty()), toDataValuePointer(axiom.getObject()));
		}

		public Axiom visit(OWLTransitiveObjectPropertyAxiom axiom) {
			return kernel.setTransitive(toObjectPropertyPointer(axiom.getProperty()));
		}

		public Axiom visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
			return kernel.setIrreflexive(toObjectPropertyPointer(axiom.getProperty()));
		}

		public Axiom visit(OWLSubDataPropertyOfAxiom axiom) {
			return kernel.impliesDRoles(toDataPropertyPointer(axiom.getSubProperty()), toDataPropertyPointer(axiom.getSuperProperty()));
		}

		public Axiom visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
			return kernel.setInverseFunctional(toObjectPropertyPointer(axiom.getProperty()));
		}

		public Axiom visit(OWLSameIndividualAxiom axiom) {
			return kernel.processSame(translateIndividualSet(axiom.getIndividuals()));
		}

		public Axiom visit(OWLSubPropertyChainOfAxiom axiom) {
			return kernel.impliesORoles(em.compose(translateObjectPropertySet(axiom.getPropertyChain())), toObjectPropertyPointer(axiom.getSuperProperty()));
		}

		public Axiom visit(OWLInverseObjectPropertiesAxiom axiom) {
			return kernel.setInverseRoles(toObjectPropertyPointer(axiom.getFirstProperty()), toObjectPropertyPointer(axiom.getSecondProperty()));
		}

		public Axiom visit(OWLHasKeyAxiom axiom) {
			//			translateObjectPropertySet(axiom.getObjectPropertyExpressions());
			//			TDLObjectRoleExpression objectPropertyPointer = kernel
			//					.getObjectPropertyKey();
			//			translateDataPropertySet(axiom.getDataPropertyExpressions());
			throw new ReasonerInternalException("JFact Kernel: unsupported operation 'getDataPropertyKey'");
			//			TDLDataRoleExpression dataPropertyPointer = kernel
			//					.getDataPropertyKey();
			//			return kernel.tellHasKey(
			//					toClassPointer(axiom.getClassExpression()),
			//					dataPropertyPointer, objectPropertyPointer);
		}

		public Axiom visit(OWLDatatypeDefinitionAxiom axiom) {
			throw new ReasonerInternalException("JFact Kernel: unsupported operation 'OWLDatatypeDefinitionAxiom'");
			//			kernel.getDataSubType(axiom.getDatatype().getIRI().toString(),
			//					toDataTypeExpressionPointer(axiom.getDataRange()));
		}

		public Axiom visit(SWRLRule rule) {
			// Ignore
			return null;
		}

		public Axiom visit(OWLSubAnnotationPropertyOfAxiom axiom) {
			// Ignore
			return null;
		}

		public Axiom visit(OWLAnnotationPropertyDomainAxiom axiom) {
			// Ignore
			return null;
		}

		public Axiom visit(OWLAnnotationPropertyRangeAxiom axiom) {
			// Ignore
			return null;
		}
	}

	final class ClassExpressionTranslator extends OWLEntityTranslator<OWLClass, ConceptExpression> implements OWLClassExpressionVisitorEx<ConceptExpression> {
		public ClassExpressionTranslator() {
		}

		@Override
		protected ConceptExpression getTopEntityPointer() {
			return em.top();
		}

		@Override
		protected ConceptExpression getBottomEntityPointer() {
			return em.bottom();
		}

		@Override
		protected OWLClass getTopEntity() {
			return df.getOWLThing();
		}

		@Override
		protected OWLClass getBottomEntity() {
			return df.getOWLNothing();
		}

		@Override
		protected ConceptExpression createPointerForEntity(OWLClass entity) {
			return em.concept(entity.getIRI().toString());
		}

		@Override
		protected DefaultNode<OWLClass> createDefaultNode() {
			return new OWLClassNode();
		}

		@Override
		protected DefaultNodeSet<OWLClass> createDefaultNodeSet() {
			return new OWLClassNodeSet();
		}

		public ConceptExpression visit(OWLClass desc) {
			return getPointerFromEntity(desc);
		}

		public ConceptExpression visit(OWLObjectIntersectionOf desc) {
			return em.and(translateClassExpressionSet(desc.getOperands()));
		}

		private List<Expression> translateClassExpressionSet(Set<OWLClassExpression> classExpressions) {
			List<Expression> l = new ArrayList<Expression>();
			for (OWLClassExpression ce : classExpressions) {
				l.add(ce.accept(this));
			}
			return l;
		}

		public ConceptExpression visit(OWLObjectUnionOf desc) {
			return em.or(translateClassExpressionSet(desc.getOperands()));
		}

		public ConceptExpression visit(OWLObjectComplementOf desc) {
			return em.not(desc.getOperand().accept(this));
		}

		public ConceptExpression visit(OWLObjectSomeValuesFrom desc) {
			return em.exists(toObjectPropertyPointer(desc.getProperty()), desc.getFiller().accept(this));
		}

		public ConceptExpression visit(OWLObjectAllValuesFrom desc) {
			return em.forall(toObjectPropertyPointer(desc.getProperty()), desc.getFiller().accept(this));
		}

		public ConceptExpression visit(OWLObjectHasValue desc) {
			return em.value(toObjectPropertyPointer(desc.getProperty()), toIndividualPointer(desc.getValue()));
		}

		public ConceptExpression visit(OWLObjectMinCardinality desc) {
			return em.minCardinality(desc.getCardinality(), toObjectPropertyPointer(desc.getProperty()), desc.getFiller().accept(this));
		}

		public ConceptExpression visit(OWLObjectExactCardinality desc) {
			return em.cardinality(desc.getCardinality(), toObjectPropertyPointer(desc.getProperty()), desc.getFiller().accept(this));
		}

		public ConceptExpression visit(OWLObjectMaxCardinality desc) {
			return em.maxCardinality(desc.getCardinality(), toObjectPropertyPointer(desc.getProperty()), desc.getFiller().accept(this));
		}

		public ConceptExpression visit(OWLObjectHasSelf desc) {
			return em.selfReference(toObjectPropertyPointer(desc.getProperty()));
		}

		public ConceptExpression visit(OWLObjectOneOf desc) {
			return em.oneOf(translateIndividualSet(desc.getIndividuals()));
		}

		public ConceptExpression visit(OWLDataSomeValuesFrom desc) {
			return em.exists(toDataPropertyPointer(desc.getProperty()), toDataTypeExpressionPointer(desc.getFiller()));
		}

		public ConceptExpression visit(OWLDataAllValuesFrom desc) {
			return em.forall(toDataPropertyPointer(desc.getProperty()), toDataTypeExpressionPointer(desc.getFiller()));
		}

		public ConceptExpression visit(OWLDataHasValue desc) {
			return em.value(toDataPropertyPointer(desc.getProperty()), toDataValuePointer(desc.getValue()));
		}

		public ConceptExpression visit(OWLDataMinCardinality desc) {
			return em.minCardinality(desc.getCardinality(), toDataPropertyPointer(desc.getProperty()), toDataTypeExpressionPointer(desc.getFiller()));
		}

		public ConceptExpression visit(OWLDataExactCardinality desc) {
			return em.cardinality(desc.getCardinality(), toDataPropertyPointer(desc.getProperty()), toDataTypeExpressionPointer(desc.getFiller()));
		}

		public ConceptExpression visit(OWLDataMaxCardinality desc) {
			return em.maxCardinality(desc.getCardinality(), toDataPropertyPointer(desc.getProperty()), toDataTypeExpressionPointer(desc.getFiller()));
		}
	}

	final class DataPropertyTranslator extends OWLEntityTranslator<OWLDataProperty, DataRoleExpression> {
		public DataPropertyTranslator() {
		}

		@Override
		protected DataRoleExpression getTopEntityPointer() {
			return getTopDataProperty();
		}

		@Override
		protected DataRoleExpression getBottomEntityPointer() {
			return getBottomDataProperty();
		}

		@Override
		protected DataRoleExpression createPointerForEntity(OWLDataProperty entity) {
			return em.dataRole(entity.toStringID());
		}

		@Override
		protected OWLDataProperty getTopEntity() {
			return df.getOWLTopDataProperty();
		}

		@Override
		protected OWLDataProperty getBottomEntity() {
			return df.getOWLBottomDataProperty();
		}

		@Override
		protected DefaultNode<OWLDataProperty> createDefaultNode() {
			return new OWLDataPropertyNode();
		}

		@Override
		protected DefaultNodeSet<OWLDataProperty> createDefaultNodeSet() {
			return new OWLDataPropertyNodeSet();
		}
	}

	final class DataRangeTranslator extends OWLEntityTranslator<OWLDatatype, DataExpression> implements OWLDataRangeVisitorEx<DataExpression> {
		public DataRangeTranslator() {
		}

		@Override
		protected DataExpression getTopEntityPointer() {
			return em.dataTop();
		}

		@Override
		protected DataExpression getBottomEntityPointer() {
			return null;
		}

		@Override
		protected DefaultNode<OWLDatatype> createDefaultNode() {
			return new OWLDatatypeNode();
		}

		@Override
		protected OWLDatatype getTopEntity() {
			return df.getTopDatatype();
		}

		@Override
		protected OWLDatatype getBottomEntity() {
			return null;
		}

		@Override
		protected DefaultNodeSet<OWLDatatype> createDefaultNodeSet() {
			return new OWLDatatypeNodeSet();
		}

		@Override
		protected DataExpression createPointerForEntity(OWLDatatype entity) {
			return getBuiltInDataType(entity.toStringID());
		}

		public DataTypeExpression visit(OWLDatatype node) {
			return getBuiltInDataType(node.toStringID());
		}

		public DataExpression visit(OWLDataOneOf node) {
			List<Expression> l = new ArrayList<Expression>();
			for (OWLLiteral literal : node.getValues()) {
				l.add(toDataValuePointer(literal));
			}
			return em.dataOneOf(l);
		}

		public DataExpression visit(OWLDataComplementOf node) {
			return em.dataNot(node.getDataRange().accept(this));
		}

		public DataExpression visit(OWLDataIntersectionOf node) {
			return em.dataAnd(translateDataRangeSet(node.getOperands()));
		}

		private List<Expression> translateDataRangeSet(Set<OWLDataRange> dataRanges) {
			List<Expression> l = new ArrayList<Expression>();
			for (OWLDataRange op : dataRanges) {
				l.add(op.accept(this));
			}
			return l;
		}

		public DataExpression visit(OWLDataUnionOf node) {
			return em.dataOr(translateDataRangeSet(node.getOperands()));
		}

		public DataExpression visit(OWLDatatypeRestriction node) {
			DataTypeExpression dte = (DataTypeExpression) node.getDatatype().accept(this);
			for (OWLFacetRestriction restriction : node.getFacetRestrictions()) {
				DataValue dv = toDataValuePointer(restriction.getFacetValue());
				FacetExpression facet;
				if (restriction.getFacet().equals(OWLFacet.MIN_INCLUSIVE)) {
					facet = em.facetMinInclusive(dv);
				} else if (restriction.getFacet().equals(OWLFacet.MAX_INCLUSIVE)) {
					facet = em.facetMaxInclusive(dv);
				} else if (restriction.getFacet().equals(OWLFacet.MIN_EXCLUSIVE)) {
					facet = em.facetMinExclusive(dv);
				} else if (restriction.getFacet().equals(OWLFacet.MAX_EXCLUSIVE)) {
					facet = em.facetMaxExclusive(dv);
				} else if (restriction.getFacet().equals(OWLFacet.LENGTH)) {
					//facet = kernel.getLength(dv);
					throw new ReasonerInternalException("JFact Kernel: unsupported facet 'getLength'");
				} else if (restriction.getFacet().equals(OWLFacet.MIN_LENGTH)) {
					//facet = kernel.getMinLength(dv);
					throw new ReasonerInternalException("JFact Kernel: unsupported facet 'getMinLength'");
				} else if (restriction.getFacet().equals(OWLFacet.MAX_LENGTH)) {
					//facet = kernel.getMaxLength(dv);
					throw new ReasonerInternalException("JFact Kernel: unsupported facet 'getMaxLength'");
				} else if (restriction.getFacet().equals(OWLFacet.FRACTION_DIGITS)) {
					//facet = kernel.getFractionDigitsFacet(dv);
					throw new ReasonerInternalException("JFact Kernel: unsupported facet 'getFractionDigitsFacet'");
				} else if (restriction.getFacet().equals(OWLFacet.PATTERN)) {
					//facet = kernel.getPattern(dv);
					throw new ReasonerInternalException("JFact Kernel: unsupported facet 'getPattern'");
				} else if (restriction.getFacet().equals(OWLFacet.TOTAL_DIGITS)) {
					//facet = kernel.getTotalDigitsFacet(dv);
					throw new ReasonerInternalException("JFact Kernel: unsupported facet 'getTotalDigitsFacet'");
				} else {
					throw new OWLRuntimeException("Unsupported facet: " + restriction.getFacet());
				}
				dte = em.restrictedType(dte, facet);
			}
			return dte;
		}
	}

	final class IndividualTranslator extends OWLEntityTranslator<OWLNamedIndividual, IndividualExpression> {
		public IndividualTranslator() {
		}

		@Override
		protected IndividualExpression getTopEntityPointer() {
			return null;
		}

		@Override
		protected IndividualExpression getBottomEntityPointer() {
			return null;
		}

		@Override
		protected IndividualExpression createPointerForEntity(OWLNamedIndividual entity) {
			return em.individual(entity.toStringID());
		}

		@Override
		protected OWLNamedIndividual getTopEntity() {
			return null;
		}

		@Override
		protected OWLNamedIndividual getBottomEntity() {
			return null;
		}

		@Override
		protected DefaultNode<OWLNamedIndividual> createDefaultNode() {
			return new OWLNamedIndividualNode();
		}

		@Override
		protected DefaultNodeSet<OWLNamedIndividual> createDefaultNodeSet() {
			return new OWLNamedIndividualNodeSet();
		}
	}

	public AxiomTranslator getAxiomTranslator() {
		return axiomTranslator;
	}

	public ClassExpressionTranslator getClassExpressionTranslator() {
		return classExpressionTranslator;
	}

	public DataRangeTranslator getDataRangeTranslator() {
		return dataRangeTranslator;
	}

	public ObjectPropertyTranslator getObjectPropertyTranslator() {
		return objectPropertyTranslator;
	}

	public DataPropertyTranslator getDataPropertyTranslator() {
		return dataPropertyTranslator;
	}

	public IndividualTranslator getIndividualTranslator() {
		return individualTranslator;
	}

	public EntailmentChecker getEntailmentChecker() {
		return entailmentChecker;
	}

	public Set<OWLAxiom> translateTAxiomSet(Collection<Axiom> trace) {
		Set<OWLAxiom> ret = new HashSet<OWLAxiom>();
		for (Axiom ap : trace) {
			ret.add(ptr2AxiomMap.get(ap));
		}
		return ret;
	}
}
