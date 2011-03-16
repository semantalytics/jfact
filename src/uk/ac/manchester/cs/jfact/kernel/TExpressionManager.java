package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.manchester.cs.jfact.kernel.datatype.TDLDataValue;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptAnd;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptDataExactCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptDataExists;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptDataForall;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptDataMaxCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptDataMinCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptDataValue;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptName;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptNot;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptObjectExactCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptObjectExists;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptObjectForall;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptObjectMaxCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptObjectMinCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptObjectSelf;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptObjectValue;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptOneOf;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptOr;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLConceptTop;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataAnd;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataNot;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataOneOf;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataOr;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataRoleBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataRoleName;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataRoleTop;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataTop;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataTypeName;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLDataTypeRestriction;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLFacetMaxExclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLFacetMaxInclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLFacetMinExclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLFacetMinInclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLIndividualName;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLObjectRoleBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLObjectRoleChain;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLObjectRoleInverse;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLObjectRoleName;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLObjectRoleProjectionFrom;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLObjectRoleProjectionInto;
import uk.ac.manchester.cs.jfact.kernel.dl.TDLObjectRoleTop;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLConceptExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLDataExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLDataRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLDataTypeExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLFacetExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLIndividualExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLObjectRoleComplexExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLObjectRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.voc.Vocabulary;

public class TExpressionManager {
	/** Cache for the inverse roles */
	protected static class TInverseRoleCache {
		/** map tail into an object head(tail) */
		private Map<TDLObjectRoleExpression, TDLObjectRoleExpression> Map;

		/** get an object corresponding to Head.Tail */
		public TDLObjectRoleExpression get(TDLObjectRoleExpression tail) {
			// try to find cached dep-set
			if (Map != null && Map.containsKey(tail)) {
				return Map.get(tail);
			}
			// no cached entry -- create a new one and cache it
			TDLObjectRoleExpression concat = build(tail);
			if (Map == null) {
				Map = new HashMap<TDLObjectRoleExpression, TDLObjectRoleExpression>();
			}
			Map.put(tail, concat);
			return concat;
		}

		/** clear the cache */
		public void clear() {
			if (Map != null) {
				Map.clear();
			}
		}

		/** host expression manager */
		protected final TExpressionManager pManager;

		public TInverseRoleCache(TExpressionManager p) {
			pManager = p;
		}

		public TDLObjectRoleExpression build(TDLObjectRoleExpression tail) {
			return pManager.record(new TDLObjectRoleInverse(tail));
		}
	}

	protected static final class DataroleNameCreator implements TNameCreator<TDLDataRoleName> {
		public TDLDataRoleName makeEntry(String name) {
			return new TDLDataRoleName(name);
		}
	}

	protected static final class ObjectroleNameCreator implements TNameCreator<TDLObjectRoleName> {
		public TDLObjectRoleName makeEntry(String name) {
			return new TDLObjectRoleName(name);
		}
	}

	protected static final class IndividualNameCreator implements TNameCreator<TDLIndividualName> {
		public TDLIndividualName makeEntry(String name) {
			return new TDLIndividualName(name);
		}
	}

	protected static final class ConceptNameCreator implements TNameCreator<TDLConceptName> {
		public TDLConceptName makeEntry(String name) {
			return new TDLConceptName(name);
		}
	}

	/** nameset for concepts */
	private final TNameSet<TDLConceptName> NS_C = new TNameSet<TDLConceptName>(new ConceptNameCreator());
	/** nameset for individuals */
	private final TNameSet<TDLIndividualName> NS_I = new TNameSet<TDLIndividualName>(new IndividualNameCreator());
	/** nameset for object roles */
	private final TNameSet<TDLObjectRoleName> NS_OR = new TNameSet<TDLObjectRoleName>(new ObjectroleNameCreator());
	/** nameset for data roles */
	private final TNameSet<TDLDataRoleName> NS_DR = new TNameSet<TDLDataRoleName>(new DataroleNameCreator());
	/** n-ary queue for arguments */
	//private final TNAryQueue<TDLExpression> ArgQueue = new TNAryQueue<TDLExpression>();
	/** TOP concept */
	private final TDLConceptTop CTop;
	/** BOTTOM concept */
	private final TDLConceptBottom CBottom;
	/** TOP object role */
	private final TDLObjectRoleTop ORTop;
	/** BOTTOM object role */
	private final TDLObjectRoleBottom ORBottom;
	/** TOP data role */
	private final TDLDataRoleTop DRTop;
	/** BOTTOM data role */
	private final TDLDataRoleBottom DRBottom;
	/** TOP data element */
	private final TDLDataTop DTop;
	/** BOTTOM data element */
	private final TDLDataBottom DBottom;
	/** record all the references */
	private final List<TDLExpression> RefRecorder = new ArrayList<TDLExpression>();
	/** cache for the role inverses */
	private final TInverseRoleCache InverseRoleCache;

	/** record the reference; @return the argument */
	protected <T extends TDLExpression> T record(T arg) {
		RefRecorder.add(arg);
		return arg;
	}

	/** get number of registered concepts */
	public int nConcepts() {
		return NS_C.size();
	}

	/** get number of registered individuals */
	public int nIndividuals() {
		return NS_I.size();
	}

	/** get number of registered object roles */
	public int nORoles() {
		return NS_OR.size();
	}

	/** get number of registered data roles */
	public int nDRoles() {
		return NS_DR.size();
	}

	// argument lists
	//	/** opens new argument list */
	//	public void newArgList() {
	//		ArgQueue.openArgList();
	//	}
	//
	//	/** add argument ARG to the current argument list */
	//	public void addArg(final TDLExpression arg) {
	//		ArgQueue.addArg(arg);
	//	}
	//
	//	/** get the latest argument list */
	//	protected final List<TDLExpression> getArgList() {
	//		//FIXME needs refactoring
	//		return ArgQueue.getLastArgList();
	//	}
	/** get TOP concept */
	public TDLConceptExpression Top() {
		return CTop;
	}

	/** get BOTTOM concept */
	public TDLConceptExpression Bottom() {
		return CBottom;
	}

	/** get named concept */
	public TDLConceptExpression Concept(final String name) {
		return NS_C.insert(name);
	}

	/** get negation of a concept C */
	public TDLConceptExpression Not(final TDLConceptExpression C) {
		return record(new TDLConceptNot(C));
	}

	/**
	 * get an n-ary conjunction expression; take the arguments from the last
	 * argument list
	 */
	public TDLConceptExpression And(List<TDLExpression> l) {
		return record(new TDLConceptAnd(l));
	}

	/** @return C and D */
	public TDLConceptExpression And(TDLConceptExpression C, TDLConceptExpression D) {
		return And(Arrays.<TDLExpression> asList(C, D));
	}

	/** @return C or D */
	public TDLConceptExpression Or(TDLConceptExpression C, TDLConceptExpression D) {
		return Or(Arrays.<TDLExpression> asList(C, D));
	}

	/**
	 * get an n-ary disjunction expression; take the arguments from the last
	 * argument list
	 */
	public TDLConceptExpression Or(List<TDLExpression> l) {
		return record(new TDLConceptOr(l));
	}

	/**
	 * get an n-ary one-of expression; take the arguments from the last argument
	 * list
	 */
	public TDLConceptExpression OneOf(List<TDLExpression> l) {
		return record(new TDLConceptOneOf(l));
	}

	public TDLObjectRoleExpression Inverse(TDLObjectRoleExpression R) {
		return InverseRoleCache.get(R);
	}

	/** @return concept {I} for the individual I */
	protected TDLConceptExpression OneOf(final TDLIndividualExpression I) {
		return OneOf(Arrays.<TDLExpression> asList(I));
	}

	/** get self-reference restriction of an object role R */
	public TDLConceptExpression SelfReference(final TDLObjectRoleExpression R) {
		return record(new TDLConceptObjectSelf(R));
	}

	/** get value restriction wrt an object role R and an individual I */
	public TDLConceptExpression Value(final TDLObjectRoleExpression R, final TDLIndividualExpression I) {
		return record(new TDLConceptObjectValue(R, I));
	}

	/** get existential restriction wrt an object role R and a concept C */
	public TDLConceptExpression Exists(final TDLObjectRoleExpression R, final TDLConceptExpression C) {
		return record(new TDLConceptObjectExists(R, C));
	}

	/** get universal restriction wrt an object role R and a concept C */
	public TDLConceptExpression Forall(final TDLObjectRoleExpression R, final TDLConceptExpression C) {
		return record(new TDLConceptObjectForall(R, C));
	}

	/**
	 * get min cardinality restriction wrt number N, an object role R and a
	 * concept C
	 */
	public TDLConceptExpression MinCardinality(int n, final TDLObjectRoleExpression R, final TDLConceptExpression C) {
		return record(new TDLConceptObjectMinCardinality(n, R, C));
	}

	/**
	 * get max cardinality restriction wrt number N, an object role R and a
	 * concept C
	 */
	public TDLConceptExpression MaxCardinality(int n, final TDLObjectRoleExpression R, final TDLConceptExpression C) {
		return record(new TDLConceptObjectMaxCardinality(n, R, C));
	}

	/**
	 * get exact cardinality restriction wrt number N, an object role R and a
	 * concept C
	 */
	public TDLConceptExpression Cardinality(int n, final TDLObjectRoleExpression R, final TDLConceptExpression C) {
		return record(new TDLConceptObjectExactCardinality(n, R, C));
	}

	/** get value restriction wrt a data role R and a data value V */
	public TDLConceptExpression Value(final TDLDataRoleExpression R, final TDLDataValue V) {
		return record(new TDLConceptDataValue(R, V));
	}

	/** get existential restriction wrt a data role R and a data expression E */
	public TDLConceptExpression Exists(final TDLDataRoleExpression R, final TDLDataExpression E) {
		return record(new TDLConceptDataExists(R, E));
	}

	/** get universal restriction wrt a data role R and a data expression E */
	public TDLConceptExpression Forall(final TDLDataRoleExpression R, final TDLDataExpression E) {
		return record(new TDLConceptDataForall(R, E));
	}

	/**
	 * get min cardinality restriction wrt number N, a data role R and a data
	 * expression E
	 */
	public TDLConceptExpression MinCardinality(int n, final TDLDataRoleExpression R, final TDLDataExpression E) {
		return record(new TDLConceptDataMinCardinality(n, R, E));
	}

	/**
	 * get max cardinality restriction wrt number N, a data role R and a data
	 * expression E
	 */
	public TDLConceptExpression MaxCardinality(int n, final TDLDataRoleExpression R, final TDLDataExpression E) {
		return record(new TDLConceptDataMaxCardinality(n, R, E));
	}

	/**
	 * get exact cardinality restriction wrt number N, a data role R and a data
	 * expression E
	 */
	public TDLConceptExpression Cardinality(int n, final TDLDataRoleExpression R, final TDLDataExpression E) {
		return record(new TDLConceptDataExactCardinality(n, R, E));
	}

	// individuals
	/** get named individual */
	public TDLIndividualExpression Individual(final String name) {
		return NS_I.insert(name);
	}

	// object roles
	/** get TOP object role */
	public TDLObjectRoleExpression ObjectRoleTop() {
		return ORTop;
	}

	/** get BOTTOM object role */
	public TDLObjectRoleExpression ObjectRoleBottom() {
		return ORBottom;
	}

	/** get named object role */
	public TDLObjectRoleExpression ObjectRole(final String name) {
		return NS_OR.insert(name);
	}

	/**
	 * get a role chain corresponding to R1 o ... o Rn; take the arguments from
	 * the last argument list
	 */
	public TDLObjectRoleComplexExpression Compose(List<TDLExpression> l) {
		return record(new TDLObjectRoleChain(l));
	}

	/** get a expression corresponding to R projected from C */
	public TDLObjectRoleComplexExpression ProjectFrom(final TDLObjectRoleExpression R, final TDLConceptExpression C) {
		return record(new TDLObjectRoleProjectionFrom(R, C));
	}

	/** get a expression corresponding to R projected into C */
	public TDLObjectRoleComplexExpression ProjectInto(final TDLObjectRoleExpression R, final TDLConceptExpression C) {
		return record(new TDLObjectRoleProjectionInto(R, C));
	}

	// data roles
	/** get TOP data role */
	public TDLDataRoleExpression DataRoleTop() {
		return DRTop;
	}

	/** get BOTTOM data role */
	public TDLDataRoleExpression DataRoleBottom() {
		return DRBottom;
	}

	/** get named data role */
	public TDLDataRoleExpression DataRole(final String name) {
		return NS_DR.insert(name);
	}

	// data expressions
	/** get TOP data element */
	public TDLDataExpression DataTop() {
		return DTop;
	}

	/** get BOTTOM data element */
	public TDLDataExpression DataBottom() {
		return DBottom;
	}

	/** get basic string data type */
	public String getDataTop() {
		//XXX there is no link between TDLDataTop and the uri
		return Vocabulary.LITERAL;
	}

	/** get basic boolean data type */
	public TDLDataTypeRestriction RestrictedType(TDLDataTypeExpression type, final TDLFacetExpression facet) {
		TDLDataTypeRestriction ret = null;
		if (type instanceof TDLDataTypeRestriction) {
			ret = (TDLDataTypeRestriction) type;
		} else {
			// get a type and build an appropriate restriction of it
			TDLDataTypeName hostType = (TDLDataTypeName) type;
			assert hostType != null;
			ret = record(new TDLDataTypeRestriction(hostType));
		}
		ret.add(facet);
		return ret;
	}

	/** get data value with given VALUE and TYPE; */
	public final TDLDataValue DataValue(final String value, TDLDataTypeExpression type) {
		return getBasicDataType(type).getValue(value);
	}

	/** get negation of a data expression E */
	public TDLDataExpression DataNot(final TDLDataExpression E) {
		return record(new TDLDataNot(E));
	}

	/**
	 * get an n-ary data conjunction expression; take the arguments from the
	 * last argument list
	 */
	public TDLDataExpression DataAnd(List<TDLExpression> l) {
		return record(new TDLDataAnd(l));
	}

	/**
	 * get an n-ary data disjunction expression; take the arguments from the
	 * last argument list
	 */
	public TDLDataExpression DataOr(List<TDLExpression> l) {
		return record(new TDLDataOr(l));
	}

	/**
	 * get an n-ary data one-of expression; take the arguments from the last
	 * argument list
	 */
	public TDLDataExpression DataOneOf(List<TDLExpression> l) {
		return record(new TDLDataOneOf(l));
	}

	/** get minInclusive facet with a given VALUE */
	public final TDLFacetExpression FacetMinInclusive(final TDLDataValue V) {
		return record(new TDLFacetMinInclusive(V));
	}

	/** get minExclusive facet with a given VALUE */
	public final TDLFacetExpression FacetMinExclusive(final TDLDataValue V) {
		return record(new TDLFacetMinExclusive(V));
	}

	/** get maxInclusive facet with a given VALUE */
	public final TDLFacetExpression FacetMaxInclusive(final TDLDataValue V) {
		return record(new TDLFacetMaxInclusive(V));
	}

	/** get maxExclusive facet with a given VALUE */
	public final TDLFacetExpression FacetMaxExclusive(final TDLDataValue V) {
		return record(new TDLFacetMaxExclusive(V));
	}

	public TExpressionManager() {
		CTop = new TDLConceptTop();
		CBottom = new TDLConceptBottom();
		ORTop = new TDLObjectRoleTop();
		ORBottom = new TDLObjectRoleBottom();
		DRTop = new TDLDataRoleTop();
		DRBottom = new TDLDataRoleBottom();
		DTop = new TDLDataTop();
		DBottom = new TDLDataBottom();
		InverseRoleCache = new TInverseRoleCache(this);
	}

	public void clear() {
		NS_C.clear();
		NS_I.clear();
		NS_OR.clear();
		NS_DR.clear();
		InverseRoleCache.clear();
		RefRecorder.clear();
	}

	protected static boolean isUniversalRole(final TDLObjectRoleExpression R) {
		return R instanceof TDLObjectRoleTop;
	}

	public static boolean isUniversalRole(final TDLDataRoleExpression R) {
		return R instanceof TDLDataRoleTop;
	}

	protected static boolean isEmptyRole(TDLObjectRoleExpression R) {
		return R instanceof TDLObjectRoleBottom;
	}

	protected static boolean isEmptyRole(TDLDataRoleExpression R) {
		return R instanceof TDLDataRoleBottom;
	}

	private static TDLDataTypeName getBasicDataType(TDLDataTypeExpression type) {
		if (type instanceof TDLDataTypeName) {
			return (TDLDataTypeName) type;
		}
		TDLDataTypeRestriction hostType = (TDLDataTypeRestriction) type;
		return hostType.getExpr();
	}
}