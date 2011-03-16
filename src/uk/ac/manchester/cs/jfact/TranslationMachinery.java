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

import uk.ac.manchester.cs.jfact.kernel.ReasoningKernel;
import uk.ac.manchester.cs.jfact.kernel.TExpressionManager;
import uk.ac.manchester.cs.jfact.kernel.datatype.Datatypes;
import uk.ac.manchester.cs.jfact.kernel.datatype.TDLDataValue;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataTypeName;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLAxiom;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLConceptExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLDataExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLDataRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLDataTypeExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLEntity;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLFacetExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLIndividualExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLObjectRoleComplexExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLObjectRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.voc.Vocabulary;

public final class TranslationMachinery {
	private volatile AxiomTranslator axiomTranslator;
	private volatile ClassExpressionTranslator classExpressionTranslator;
	private volatile DataRangeTranslator dataRangeTranslator;
	private volatile ObjectPropertyTranslator objectPropertyTranslator;
	private volatile DataPropertyTranslator dataPropertyTranslator;
	private volatile IndividualTranslator individualTranslator;
	private volatile EntailmentChecker entailmentChecker;
	private final Map<OWLAxiom, TDLAxiom> axiom2PtrMap = new HashMap<OWLAxiom, TDLAxiom>();
	private final Map<TDLAxiom, OWLAxiom> ptr2AxiomMap = new HashMap<TDLAxiom, OWLAxiom>();
	protected final ReasoningKernel kernel;
	protected final TExpressionManager em;
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

	public TDLDataTypeName getBuiltInDataType(String DTName) {
		return new TDLDataTypeName(Datatypes.getBuiltInDataType(DTName));
	}

	public TDLObjectRoleExpression getTopObjectProperty() {
		return em.ObjectRole(Vocabulary.TOP_OBJECT_PROPERTY);
	}

	public TDLObjectRoleExpression getBottomObjectProperty() {
		return em.ObjectRole(Vocabulary.BOTTOM_OBJECT_PROPERTY);
	}

	public TDLDataRoleExpression getTopDataProperty() {
		return em.DataRole(Vocabulary.TOP_DATA_PROPERTY);
	}

	public TDLDataRoleExpression getBottomDataProperty() {
		return em.DataRole(Vocabulary.BOTTOM_DATA_PROPERTY);
	}

	public void loadAxiom(OWLAxiom axiom) {
		final TDLAxiom axiomPointer = axiom.accept(axiomTranslator);
		if (axiomPointer != null) {
			axiom2PtrMap.put(axiom, axiomPointer);
		}
	}

	public void retractAxiom(OWLAxiom axiom) {
		final TDLAxiom ptr = axiom2PtrMap.get(axiom);
		if (ptr != null) {
			kernel.retract(ptr);
			axiom2PtrMap.remove(axiom);
		}
	}

	protected TDLConceptExpression toClassPointer(OWLClassExpression classExpression) {
		return classExpression.accept(classExpressionTranslator);
	}

	protected TDLDataExpression toDataTypeExpressionPointer(OWLDataRange dataRange) {
		return dataRange.accept(dataRangeTranslator);
	}

	protected TDLObjectRoleExpression toObjectPropertyPointer(OWLObjectPropertyExpression propertyExpression) {
		OWLObjectPropertyExpression simp = propertyExpression.getSimplified();
		if (simp.isAnonymous()) {
			OWLObjectInverseOf inv = (OWLObjectInverseOf) simp;
			return em.Inverse(objectPropertyTranslator.getPointerFromEntity(inv.getInverse().asOWLObjectProperty()));
		} else {
			return objectPropertyTranslator.getPointerFromEntity(simp.asOWLObjectProperty());
		}
	}

	protected TDLDataRoleExpression toDataPropertyPointer(OWLDataPropertyExpression propertyExpression) {
		return dataPropertyTranslator.getPointerFromEntity(propertyExpression.asOWLDataProperty());
	}

	protected synchronized TDLIndividualExpression toIndividualPointer(OWLIndividual individual) {
		if (!individual.isAnonymous()) {
			return individualTranslator.getPointerFromEntity(individual.asOWLNamedIndividual());
		} else {
			return em.Individual(individual.toStringID());
		}
	}

	protected synchronized TDLDataTypeExpression toDataTypePointer(OWLDatatype datatype) {
		if (datatype == null) {
			throw new NullPointerException();
		}
		return getBuiltInDataType(datatype.toStringID());
	}

	protected synchronized TDLDataValue toDataValuePointer(OWLLiteral literal) {
		String value = literal.getLiteral();
		if (literal.isRDFPlainLiteral()) {
			value = value + "@" + literal.getLang();
		}
		return em.DataValue(value, toDataTypePointer(literal.getDatatype()));
	}

	protected NodeSet<OWLNamedIndividual> translateIndividualPointersToNodeSet(Iterable<TDLIndividualExpression> pointers) {
		OWLNamedIndividualNodeSet ns = new OWLNamedIndividualNodeSet();
		for (TDLIndividualExpression pointer : pointers) {
			if (pointer != null) {
				OWLNamedIndividual ind = individualTranslator.getEntityFromPointer(pointer);
				ns.addEntity(ind);
			}
		}
		return ns;
	}

	protected synchronized List<TDLExpression> translateIndividualSet(Set<OWLIndividual> inds) {
		List<TDLExpression> l = new ArrayList<TDLExpression>();
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
			List<TDLExpression> l = new ArrayList<TDLExpression>();
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

	abstract class OWLEntityTranslator<E extends OWLObject, T extends TDLEntity> {
		private final Map<E, T> entity2dlentity = new HashMap<E, T>();
		private final Map<T, E> dlentity2entity = new HashMap<T, E>();

		protected final void FillMaps(E entity, T dlentity) {
			entity2dlentity.put(entity, dlentity);
			dlentity2entity.put(dlentity, entity);
		}

		protected OWLEntityTranslator() {
			E topEntity = getTopEntity();
			if (topEntity != null) {
				FillMaps(topEntity, getTopEntityPointer());
			}
			E bottomEntity = getBottomEntity();
			if (bottomEntity != null) {
				FillMaps(bottomEntity, getBottomEntityPointer());
			}
		}

		protected T registerNewEntity(E entity) {
			T pointer = createPointerForEntity(entity);
			FillMaps(entity, pointer);
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

	final class ObjectPropertyTranslator extends OWLEntityTranslator<OWLObjectPropertyExpression, TDLObjectRoleExpression> {
		public ObjectPropertyTranslator() {
		}

		@Override
		protected TDLObjectRoleExpression getTopEntityPointer() {
			return getTopObjectProperty();
		}

		@Override
		protected TDLObjectRoleExpression getBottomEntityPointer() {
			return getBottomObjectProperty();
		}

		@Override
		protected TDLObjectRoleExpression registerNewEntity(OWLObjectPropertyExpression entity) {
			TDLObjectRoleExpression pointer = createPointerForEntity(entity);
			FillMaps(entity, pointer);
			entity = entity.getInverseProperty().getSimplified();
			FillMaps(entity, createPointerForEntity(entity));
			return pointer;
		}

		@Override
		protected TDLObjectRoleExpression createPointerForEntity(OWLObjectPropertyExpression entity) {
			// FIXME!! think later!!
			TDLObjectRoleExpression p = em.ObjectRole(entity.getNamedProperty().toStringID());
			if (entity.isAnonymous()) {
				p = em.Inverse(p);
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

	final class ComplexObjectPropertyTranslator extends OWLEntityTranslator<OWLObjectPropertyExpression, TDLObjectRoleComplexExpression> {
		public ComplexObjectPropertyTranslator() {
		}

		@Override
		protected TDLObjectRoleComplexExpression getTopEntityPointer() {
			return getTopObjectProperty();
		}

		@Override
		protected TDLObjectRoleComplexExpression getBottomEntityPointer() {
			return getBottomObjectProperty();
		}

		@Override
		protected TDLObjectRoleComplexExpression registerNewEntity(OWLObjectPropertyExpression entity) {
			TDLObjectRoleComplexExpression pointer = createPointerForEntity(entity);
			FillMaps(entity, pointer);
			entity = entity.getInverseProperty().getSimplified();
			FillMaps(entity, createPointerForEntity(entity));
			return pointer;
		}

		@Override
		protected TDLObjectRoleComplexExpression createPointerForEntity(OWLObjectPropertyExpression entity) {
			TDLObjectRoleComplexExpression p = em.ObjectRole(entity.getNamedProperty().toStringID());
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

	final class AxiomTranslator implements OWLAxiomVisitorEx<TDLAxiom> {
		protected final class DeclarationVisitorEx implements OWLEntityVisitorEx<TDLAxiom> {
			public TDLAxiom visit(OWLClass cls) {
				return kernel.declare(toClassPointer(cls));
			}

			public TDLAxiom visit(OWLObjectProperty property) {
				return kernel.declare(toObjectPropertyPointer(property));
			}

			public TDLAxiom visit(OWLDataProperty property) {
				return kernel.declare(toDataPropertyPointer(property));
			}

			public TDLAxiom visit(OWLNamedIndividual individual) {
				return kernel.declare(toIndividualPointer(individual));
			}

			public TDLAxiom visit(OWLDatatype datatype) {
				return kernel.declare(toDataTypePointer(datatype));
				//				throw new ReasonerInternalException(
				//						"JFact Kernel: unsupported operation 'tellDatatypeDeclaration' "+datatype);
				//TODO 
				//				return kernel
				//						.tellDatatypeDeclaration(toDataTypePointer(datatype));
			}

			public TDLAxiom visit(OWLAnnotationProperty property) {
				return null;
			}
		}

		private final DeclarationVisitorEx v;

		public AxiomTranslator() {
			v = new DeclarationVisitorEx();
		}

		public TDLAxiom visit(OWLSubClassOfAxiom axiom) {
			return kernel.impliesConcepts(toClassPointer(axiom.getSubClass()), toClassPointer(axiom.getSuperClass()));
		}

		public TDLAxiom visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
			return kernel.relatedToNot(toIndividualPointer(axiom.getSubject()), toObjectPropertyPointer(axiom.getProperty()), toIndividualPointer(axiom.getObject()));
		}

		public TDLAxiom visit(OWLAsymmetricObjectPropertyAxiom axiom) {
			return kernel.setAsymmetric(toObjectPropertyPointer(axiom.getProperty()));
		}

		public TDLAxiom visit(OWLReflexiveObjectPropertyAxiom axiom) {
			return kernel.setReflexive(toObjectPropertyPointer(axiom.getProperty()));
		}

		public TDLAxiom visit(OWLDisjointClassesAxiom axiom) {
			return kernel.disjointConcepts(translateClassExpressionSet(axiom.getClassExpressions()));
		}

		private List<TDLExpression> translateClassExpressionSet(Set<OWLClassExpression> classExpressions) {
			List<TDLExpression> l = new ArrayList<TDLExpression>();
			for (OWLClassExpression ce : classExpressions) {
				l.add(toClassPointer(ce));
			}
			return l;
		}

		public TDLAxiom visit(OWLDataPropertyDomainAxiom axiom) {
			return kernel.setDDomain(toDataPropertyPointer(axiom.getProperty()), toClassPointer(axiom.getDomain()));
		}

		public TDLAxiom visit(OWLObjectPropertyDomainAxiom axiom) {
			return kernel.setODomain(toObjectPropertyPointer(axiom.getProperty()), toClassPointer(axiom.getDomain()));
		}

		public TDLAxiom visit(OWLEquivalentObjectPropertiesAxiom axiom) {
			return kernel.equalORoles(translateObjectPropertySet(axiom.getProperties()));
		}

		private List<TDLExpression> translateObjectPropertySet(Collection<OWLObjectPropertyExpression> properties) {
			List<TDLExpression> l = new ArrayList<TDLExpression>();
			for (OWLObjectPropertyExpression property : properties) {
				l.add(toObjectPropertyPointer(property));
			}
			return l;
		}

		public TDLAxiom visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
			return kernel.valueOfNot(toIndividualPointer(axiom.getSubject()), toDataPropertyPointer(axiom.getProperty()), toDataValuePointer(axiom.getObject()));
		}

		public TDLAxiom visit(OWLDifferentIndividualsAxiom axiom) {
			return kernel.processDifferent(translateIndividualSet(axiom.getIndividuals()));
		}

		public TDLAxiom visit(OWLDisjointDataPropertiesAxiom axiom) {
			return kernel.disjointDRoles(translateDataPropertySet(axiom.getProperties()));
		}

		private List<TDLExpression> translateDataPropertySet(Set<OWLDataPropertyExpression> properties) {
			List<TDLExpression> l = new ArrayList<TDLExpression>();
			for (OWLDataPropertyExpression property : properties) {
				l.add(toDataPropertyPointer(property));
			}
			return l;
		}

		public TDLAxiom visit(OWLDisjointObjectPropertiesAxiom axiom) {
			return kernel.disjointORoles(translateObjectPropertySet(axiom.getProperties()));
		}

		public TDLAxiom visit(OWLObjectPropertyRangeAxiom axiom) {
			return kernel.setORange(toObjectPropertyPointer(axiom.getProperty()), toClassPointer(axiom.getRange()));
		}

		public TDLAxiom visit(OWLObjectPropertyAssertionAxiom axiom) {
			return kernel.relatedTo(toIndividualPointer(axiom.getSubject()), toObjectPropertyPointer(axiom.getProperty()), toIndividualPointer(axiom.getObject()));
		}

		public TDLAxiom visit(OWLFunctionalObjectPropertyAxiom axiom) {
			return kernel.setOFunctional(toObjectPropertyPointer(axiom.getProperty()));
		}

		public TDLAxiom visit(OWLSubObjectPropertyOfAxiom axiom) {
			return kernel.impliesORoles(toObjectPropertyPointer(axiom.getSubProperty()), toObjectPropertyPointer(axiom.getSuperProperty()));
		}

		public TDLAxiom visit(OWLDisjointUnionAxiom axiom) {
			return kernel.disjointUnion(toClassPointer(axiom.getOWLClass()), translateClassExpressionSet(axiom.getClassExpressions()));
		}

		public TDLAxiom visit(OWLDeclarationAxiom axiom) {
			OWLEntity entity = axiom.getEntity();
			return entity.accept(v);
		}

		public TDLAxiom visit(OWLAnnotationAssertionAxiom axiom) {
			// Ignore
			return null;
		}

		public TDLAxiom visit(OWLSymmetricObjectPropertyAxiom axiom) {
			return kernel.setSymmetric(toObjectPropertyPointer(axiom.getProperty()));
		}

		public TDLAxiom visit(OWLDataPropertyRangeAxiom axiom) {
			return kernel.setDRange(toDataPropertyPointer(axiom.getProperty()), toDataTypeExpressionPointer(axiom.getRange()));
		}

		public TDLAxiom visit(OWLFunctionalDataPropertyAxiom axiom) {
			return kernel.setDFunctional(toDataPropertyPointer(axiom.getProperty()));
		}

		public TDLAxiom visit(OWLEquivalentDataPropertiesAxiom axiom) {
			return kernel.equalDRoles(translateDataPropertySet(axiom.getProperties()));
		}

		public TDLAxiom visit(OWLClassAssertionAxiom axiom) {
			return kernel.instanceOf(toIndividualPointer(axiom.getIndividual()), toClassPointer(axiom.getClassExpression()));
		}

		public TDLAxiom visit(OWLEquivalentClassesAxiom axiom) {
			return kernel.equalConcepts(translateClassExpressionSet(axiom.getClassExpressions()));
		}

		public TDLAxiom visit(OWLDataPropertyAssertionAxiom axiom) {
			return kernel.valueOf(toIndividualPointer(axiom.getSubject()), toDataPropertyPointer(axiom.getProperty()), toDataValuePointer(axiom.getObject()));
		}

		public TDLAxiom visit(OWLTransitiveObjectPropertyAxiom axiom) {
			return kernel.setTransitive(toObjectPropertyPointer(axiom.getProperty()));
		}

		public TDLAxiom visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
			return kernel.setIrreflexive(toObjectPropertyPointer(axiom.getProperty()));
		}

		public TDLAxiom visit(OWLSubDataPropertyOfAxiom axiom) {
			return kernel.impliesDRoles(toDataPropertyPointer(axiom.getSubProperty()), toDataPropertyPointer(axiom.getSuperProperty()));
		}

		public TDLAxiom visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
			return kernel.setInverseFunctional(toObjectPropertyPointer(axiom.getProperty()));
		}

		public TDLAxiom visit(OWLSameIndividualAxiom axiom) {
			return kernel.processSame(translateIndividualSet(axiom.getIndividuals()));
		}

		public TDLAxiom visit(OWLSubPropertyChainOfAxiom axiom) {
			return kernel.impliesORoles(em.Compose(translateObjectPropertySet(axiom.getPropertyChain())), toObjectPropertyPointer(axiom.getSuperProperty()));
		}

		public TDLAxiom visit(OWLInverseObjectPropertiesAxiom axiom) {
			return kernel.setInverseRoles(toObjectPropertyPointer(axiom.getFirstProperty()), toObjectPropertyPointer(axiom.getSecondProperty()));
		}

		public TDLAxiom visit(OWLHasKeyAxiom axiom) {
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

		public TDLAxiom visit(OWLDatatypeDefinitionAxiom axiom) {
			throw new ReasonerInternalException("JFact Kernel: unsupported operation 'OWLDatatypeDefinitionAxiom'");
			//			kernel.getDataSubType(axiom.getDatatype().getIRI().toString(),
			//					toDataTypeExpressionPointer(axiom.getDataRange()));
		}

		public TDLAxiom visit(SWRLRule rule) {
			// Ignore
			return null;
		}

		public TDLAxiom visit(OWLSubAnnotationPropertyOfAxiom axiom) {
			// Ignore
			return null;
		}

		public TDLAxiom visit(OWLAnnotationPropertyDomainAxiom axiom) {
			// Ignore
			return null;
		}

		public TDLAxiom visit(OWLAnnotationPropertyRangeAxiom axiom) {
			// Ignore
			return null;
		}
	}

	final class ClassExpressionTranslator extends OWLEntityTranslator<OWLClass, TDLConceptExpression> implements OWLClassExpressionVisitorEx<TDLConceptExpression> {
		public ClassExpressionTranslator() {
		}

		@Override
		protected TDLConceptExpression getTopEntityPointer() {
			return em.Top();
		}

		@Override
		protected TDLConceptExpression getBottomEntityPointer() {
			return em.Bottom();
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
		protected TDLConceptExpression createPointerForEntity(OWLClass entity) {
			return em.Concept(entity.getIRI().toString());
		}

		@Override
		protected DefaultNode<OWLClass> createDefaultNode() {
			return new OWLClassNode();
		}

		@Override
		protected DefaultNodeSet<OWLClass> createDefaultNodeSet() {
			return new OWLClassNodeSet();
		}

		public TDLConceptExpression visit(OWLClass desc) {
			return getPointerFromEntity(desc);
		}

		public TDLConceptExpression visit(OWLObjectIntersectionOf desc) {
			return em.And(translateClassExpressionSet(desc.getOperands()));
		}

		private List<TDLExpression> translateClassExpressionSet(Set<OWLClassExpression> classExpressions) {
			List<TDLExpression> l = new ArrayList<TDLExpression>();
			for (OWLClassExpression ce : classExpressions) {
				l.add(ce.accept(this));
			}
			return l;
		}

		public TDLConceptExpression visit(OWLObjectUnionOf desc) {
			return em.Or(translateClassExpressionSet(desc.getOperands()));
		}

		public TDLConceptExpression visit(OWLObjectComplementOf desc) {
			return em.Not(desc.getOperand().accept(this));
		}

		public TDLConceptExpression visit(OWLObjectSomeValuesFrom desc) {
			return em.Exists(toObjectPropertyPointer(desc.getProperty()), desc.getFiller().accept(this));
		}

		public TDLConceptExpression visit(OWLObjectAllValuesFrom desc) {
			return em.Forall(toObjectPropertyPointer(desc.getProperty()), desc.getFiller().accept(this));
		}

		public TDLConceptExpression visit(OWLObjectHasValue desc) {
			return em.Value(toObjectPropertyPointer(desc.getProperty()), toIndividualPointer(desc.getValue()));
		}

		public TDLConceptExpression visit(OWLObjectMinCardinality desc) {
			return em.MinCardinality(desc.getCardinality(), toObjectPropertyPointer(desc.getProperty()), desc.getFiller().accept(this));
		}

		public TDLConceptExpression visit(OWLObjectExactCardinality desc) {
			return em.Cardinality(desc.getCardinality(), toObjectPropertyPointer(desc.getProperty()), desc.getFiller().accept(this));
		}

		public TDLConceptExpression visit(OWLObjectMaxCardinality desc) {
			return em.MaxCardinality(desc.getCardinality(), toObjectPropertyPointer(desc.getProperty()), desc.getFiller().accept(this));
		}

		public TDLConceptExpression visit(OWLObjectHasSelf desc) {
			return em.SelfReference(toObjectPropertyPointer(desc.getProperty()));
		}

		public TDLConceptExpression visit(OWLObjectOneOf desc) {
			return em.OneOf(translateIndividualSet(desc.getIndividuals()));
		}

		public TDLConceptExpression visit(OWLDataSomeValuesFrom desc) {
			return em.Exists(toDataPropertyPointer(desc.getProperty()), toDataTypeExpressionPointer(desc.getFiller()));
		}

		public TDLConceptExpression visit(OWLDataAllValuesFrom desc) {
			return em.Forall(toDataPropertyPointer(desc.getProperty()), toDataTypeExpressionPointer(desc.getFiller()));
		}

		public TDLConceptExpression visit(OWLDataHasValue desc) {
			return em.Value(toDataPropertyPointer(desc.getProperty()), toDataValuePointer(desc.getValue()));
		}

		public TDLConceptExpression visit(OWLDataMinCardinality desc) {
			return em.MinCardinality(desc.getCardinality(), toDataPropertyPointer(desc.getProperty()), toDataTypeExpressionPointer(desc.getFiller()));
		}

		public TDLConceptExpression visit(OWLDataExactCardinality desc) {
			return em.Cardinality(desc.getCardinality(), toDataPropertyPointer(desc.getProperty()), toDataTypeExpressionPointer(desc.getFiller()));
		}

		public TDLConceptExpression visit(OWLDataMaxCardinality desc) {
			return em.MaxCardinality(desc.getCardinality(), toDataPropertyPointer(desc.getProperty()), toDataTypeExpressionPointer(desc.getFiller()));
		}
	}

	final class DataPropertyTranslator extends OWLEntityTranslator<OWLDataProperty, TDLDataRoleExpression> {
		public DataPropertyTranslator() {
		}

		@Override
		protected TDLDataRoleExpression getTopEntityPointer() {
			return getTopDataProperty();
		}

		@Override
		protected TDLDataRoleExpression getBottomEntityPointer() {
			return getBottomDataProperty();
		}

		@Override
		protected TDLDataRoleExpression createPointerForEntity(OWLDataProperty entity) {
			return em.DataRole(entity.toStringID());
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

	final class DataRangeTranslator extends OWLEntityTranslator<OWLDatatype, TDLDataExpression> implements OWLDataRangeVisitorEx<TDLDataExpression> {
		public DataRangeTranslator() {
		}

		@Override
		protected TDLDataExpression getTopEntityPointer() {
			return em.DataTop();
		}

		@Override
		protected TDLDataExpression getBottomEntityPointer() {
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
		protected TDLDataExpression createPointerForEntity(OWLDatatype entity) {
			return getBuiltInDataType(entity.toStringID());
		}

		public TDLDataTypeExpression visit(OWLDatatype node) {
			return getBuiltInDataType(node.toStringID());
		}

		public TDLDataExpression visit(OWLDataOneOf node) {
			List<TDLExpression> l = new ArrayList<TDLExpression>();
			for (OWLLiteral literal : node.getValues()) {
				l.add(toDataValuePointer(literal));
			}
			//	kernel.closeArgList();
			return em.DataOneOf(l);
		}

		public TDLDataExpression visit(OWLDataComplementOf node) {
			return em.DataNot(node.getDataRange().accept(this));
		}

		public TDLDataExpression visit(OWLDataIntersectionOf node) {
			return em.DataAnd(translateDataRangeSet(node.getOperands()));
		}

		private List<TDLExpression> translateDataRangeSet(Set<OWLDataRange> dataRanges) {
			List<TDLExpression> l = new ArrayList<TDLExpression>();
			for (OWLDataRange op : dataRanges) {
				l.add(op.accept(this));
			}
			return l;
		}

		public TDLDataExpression visit(OWLDataUnionOf node) {
			return em.DataOr(translateDataRangeSet(node.getOperands()));
		}

		public TDLDataExpression visit(OWLDatatypeRestriction node) {
			TDLDataTypeExpression dte = (TDLDataTypeExpression) node.getDatatype().accept(this);
			for (OWLFacetRestriction restriction : node.getFacetRestrictions()) {
				TDLDataValue dv = toDataValuePointer(restriction.getFacetValue());
				TDLFacetExpression facet;
				if (restriction.getFacet().equals(OWLFacet.MIN_INCLUSIVE)) {
					facet = em.FacetMinInclusive(dv);
				} else if (restriction.getFacet().equals(OWLFacet.MAX_INCLUSIVE)) {
					facet = em.FacetMaxInclusive(dv);
				} else if (restriction.getFacet().equals(OWLFacet.MIN_EXCLUSIVE)) {
					facet = em.FacetMinExclusive(dv);
				} else if (restriction.getFacet().equals(OWLFacet.MAX_EXCLUSIVE)) {
					facet = em.FacetMaxExclusive(dv);
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
				dte = em.RestrictedType(dte, facet);
			}
			return dte;
		}
	}

	final class IndividualTranslator extends OWLEntityTranslator<OWLNamedIndividual, TDLIndividualExpression> {
		public IndividualTranslator() {
		}

		@Override
		protected TDLIndividualExpression getTopEntityPointer() {
			return null;
		}

		@Override
		protected TDLIndividualExpression getBottomEntityPointer() {
			return null;
		}

		@Override
		protected TDLIndividualExpression createPointerForEntity(OWLNamedIndividual entity) {
			return em.Individual(entity.toStringID());
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

	public Set<OWLAxiom> translateTAxiomSet(Collection<TDLAxiom> trace) {
		Set<OWLAxiom> ret = new HashSet<OWLAxiom>();
		for (TDLAxiom ap : trace) {
			ret.add(ptr2AxiomMap.get(ap));
		}
		return ret;
	}
}
