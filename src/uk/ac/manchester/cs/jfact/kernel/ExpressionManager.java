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

import uk.ac.manchester.cs.jfact.kernel.datatype.DataValue;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptAnd;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptDataExactCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptDataExists;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptDataForall;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptDataMaxCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptDataMinCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptDataValue;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptName;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptNot;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectExactCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectExists;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectForall;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectMaxCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectMinCardinality;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectSelf;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptObjectValue;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptOneOf;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptOr;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptTop;
import uk.ac.manchester.cs.jfact.kernel.dl.DataAnd;
import uk.ac.manchester.cs.jfact.kernel.dl.DataBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.DataNot;
import uk.ac.manchester.cs.jfact.kernel.dl.DataOneOf;
import uk.ac.manchester.cs.jfact.kernel.dl.DataOr;
import uk.ac.manchester.cs.jfact.kernel.dl.DataRoleBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.DataRoleName;
import uk.ac.manchester.cs.jfact.kernel.dl.DataRoleTop;
import uk.ac.manchester.cs.jfact.kernel.dl.DataTop;
import uk.ac.manchester.cs.jfact.kernel.dl.DataTypeName;
import uk.ac.manchester.cs.jfact.kernel.dl.DataTypeRestriction;
import uk.ac.manchester.cs.jfact.kernel.dl.FacetMaxExclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.FacetMaxInclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.FacetMinExclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.FacetMinInclusive;
import uk.ac.manchester.cs.jfact.kernel.dl.IndividualName;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleBottom;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleChain;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleInverse;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleName;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleProjectionFrom;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleProjectionInto;
import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleTop;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ConceptExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataTypeExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Expression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.FacetExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.IndividualExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ObjectRoleComplexExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ObjectRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.voc.Vocabulary;

public final class ExpressionManager {
	/** Cache for the inverse roles */
	protected final class InverseRoleCache {
		/** map tail into an object head(tail) */
		private Map<ObjectRoleExpression, ObjectRoleExpression> map;

		/** get an object corresponding to Head.Tail */
		public ObjectRoleExpression get(ObjectRoleExpression tail) {
			// try to find cached dep-set
			if (map != null && map.containsKey(tail)) {
				return map.get(tail);
			}
			// no cached entry -- create a new one and cache it
			ObjectRoleExpression concat = build(tail);
			if (map == null) {
				map = new HashMap<ObjectRoleExpression, ObjectRoleExpression>();
			}
			map.put(tail, concat);
			return concat;
		}

		/** clear the cache */
		public void clear() {
			if (map != null) {
				map.clear();
			}
		}

		public ObjectRoleExpression build(ObjectRoleExpression tail) {
			return record(new ObjectRoleInverse(tail));
		}
	}

	protected static final class DataroleNameCreator implements NameCreator<DataRoleName> {
		public DataRoleName makeEntry(String name) {
			return new DataRoleName(name);
		}
	}

	protected static final class ObjectroleNameCreator implements NameCreator<ObjectRoleName> {
		public ObjectRoleName makeEntry(String name) {
			return new ObjectRoleName(name);
		}
	}

	protected static final class IndividualNameCreator implements NameCreator<IndividualName> {
		public IndividualName makeEntry(String name) {
			return new IndividualName(name);
		}
	}

	protected static final class ConceptNameCreator implements NameCreator<ConceptName> {
		public ConceptName makeEntry(String name) {
			return new ConceptName(name);
		}
	}

	/** nameset for concepts */
	private final NameSet<ConceptName> conceptNameset = new NameSet<ConceptName>(new ConceptNameCreator());
	/** nameset for individuals */
	private final NameSet<IndividualName> individualNameset = new NameSet<IndividualName>(new IndividualNameCreator());
	/** nameset for object roles */
	private final NameSet<ObjectRoleName> objectRoleNameset = new NameSet<ObjectRoleName>(new ObjectroleNameCreator());
	/** nameset for data roles */
	private final NameSet<DataRoleName> dataRoleNameset = new NameSet<DataRoleName>(new DataroleNameCreator());
	/** TOP concept */
	private final ConceptTop top = new ConceptTop();
	/** BOTTOM concept */
	private final ConceptBottom bottom = new ConceptBottom();
	/** TOP object role */
	private final ObjectRoleTop objectRoleTop = new ObjectRoleTop();
	/** BOTTOM object role */
	private final ObjectRoleBottom objectRoleBottom = new ObjectRoleBottom();
	/** TOP data role */
	private final DataRoleTop dataRoleTop = new DataRoleTop();
	/** BOTTOM data role */
	private final DataRoleBottom dataRoleBottom = new DataRoleBottom();
	/** TOP data element */
	private final DataTop dataTop = new DataTop();
	/** BOTTOM data element */
	private final DataBottom dataBottom = new DataBottom();
	/** record all the references */
	private final List<Expression> referenceRecorder = new ArrayList<Expression>();
	/** cache for the role inverses */
	private final InverseRoleCache inverseRoleCache = new InverseRoleCache();

	/** record the reference; @return the argument */
	protected <T extends Expression> T record(T arg) {
		referenceRecorder.add(arg);
		return arg;
	}

	/** get number of registered concepts */
	public int nConcepts() {
		return conceptNameset.size();
	}

	/** get number of registered individuals */
	public int nIndividuals() {
		return individualNameset.size();
	}

	/** get number of registered object roles */
	public int nORoles() {
		return objectRoleNameset.size();
	}

	/** get number of registered data roles */
	public int nDRoles() {
		return dataRoleNameset.size();
	}

	/** get TOP concept */
	public ConceptExpression top() {
		return top;
	}

	/** get BOTTOM concept */
	public ConceptExpression bottom() {
		return bottom;
	}

	/** get named concept */
	public ConceptExpression concept(final String name) {
		return conceptNameset.insert(name);
	}

	/** get negation of a concept C */
	public ConceptExpression not(final ConceptExpression C) {
		return record(new ConceptNot(C));
	}

	/**
	 * get an n-ary conjunction expression; take the arguments from the last
	 * argument list
	 */
	public ConceptExpression and(List<Expression> l) {
		return record(new ConceptAnd(l));
	}

	/** @return C and D */
	public ConceptExpression and(ConceptExpression C, ConceptExpression D) {
		return and(Arrays.<Expression> asList(C, D));
	}

	/** @return C or D */
	public ConceptExpression or(ConceptExpression C, ConceptExpression D) {
		return or(Arrays.<Expression> asList(C, D));
	}

	/**
	 * get an n-ary disjunction expression; take the arguments from the last
	 * argument list
	 */
	public ConceptExpression or(List<Expression> l) {
		return record(new ConceptOr(l));
	}

	/**
	 * get an n-ary one-of expression; take the arguments from the last argument
	 * list
	 */
	public ConceptExpression oneOf(List<Expression> l) {
		return record(new ConceptOneOf(l));
	}

	public ObjectRoleExpression inverse(ObjectRoleExpression R) {
		return inverseRoleCache.get(R);
	}

	/** @return concept {I} for the individual I */
	public ConceptExpression oneOf(final IndividualExpression I) {
		return oneOf(Arrays.<Expression> asList(I));
	}

	/** get self-reference restriction of an object role R */
	public ConceptExpression selfReference(final ObjectRoleExpression R) {
		return record(new ConceptObjectSelf(R));
	}

	/** get value restriction wrt an object role R and an individual I */
	public ConceptExpression value(final ObjectRoleExpression R, final IndividualExpression I) {
		return record(new ConceptObjectValue(R, I));
	}

	/** get existential restriction wrt an object role R and a concept C */
	public ConceptExpression exists(final ObjectRoleExpression R, final ConceptExpression C) {
		return record(new ConceptObjectExists(R, C));
	}

	/** get universal restriction wrt an object role R and a concept C */
	public ConceptExpression forall(final ObjectRoleExpression R, final ConceptExpression C) {
		return record(new ConceptObjectForall(R, C));
	}

	/**
	 * get min cardinality restriction wrt number N, an object role R and a
	 * concept C
	 */
	public ConceptExpression minCardinality(int n, final ObjectRoleExpression R, final ConceptExpression C) {
		return record(new ConceptObjectMinCardinality(n, R, C));
	}

	/**
	 * get max cardinality restriction wrt number N, an object role R and a
	 * concept C
	 */
	public ConceptExpression maxCardinality(int n, final ObjectRoleExpression R, final ConceptExpression C) {
		return record(new ConceptObjectMaxCardinality(n, R, C));
	}

	/**
	 * get exact cardinality restriction wrt number N, an object role R and a
	 * concept C
	 */
	public ConceptExpression cardinality(int n, final ObjectRoleExpression R, final ConceptExpression C) {
		return record(new ConceptObjectExactCardinality(n, R, C));
	}

	/** get value restriction wrt a data role R and a data value V */
	public ConceptExpression value(final DataRoleExpression R, final DataValue V) {
		return record(new ConceptDataValue(R, V));
	}

	/** get existential restriction wrt a data role R and a data expression E */
	public ConceptExpression exists(final DataRoleExpression R, final DataExpression E) {
		return record(new ConceptDataExists(R, E));
	}

	/** get universal restriction wrt a data role R and a data expression E */
	public ConceptExpression forall(final DataRoleExpression R, final DataExpression E) {
		return record(new ConceptDataForall(R, E));
	}

	/**
	 * get min cardinality restriction wrt number N, a data role R and a data
	 * expression E
	 */
	public ConceptExpression minCardinality(int n, final DataRoleExpression R, final DataExpression E) {
		return record(new ConceptDataMinCardinality(n, R, E));
	}

	/**
	 * get max cardinality restriction wrt number N, a data role R and a data
	 * expression E
	 */
	public ConceptExpression maxCardinality(int n, final DataRoleExpression R, final DataExpression E) {
		return record(new ConceptDataMaxCardinality(n, R, E));
	}

	/**
	 * get exact cardinality restriction wrt number N, a data role R and a data
	 * expression E
	 */
	public ConceptExpression cardinality(int n, final DataRoleExpression R, final DataExpression E) {
		return record(new ConceptDataExactCardinality(n, R, E));
	}

	// individuals
	/** get named individual */
	public IndividualExpression individual(final String name) {
		return individualNameset.insert(name);
	}

	// object roles
	/** get TOP object role */
	public ObjectRoleExpression objectRoleTop() {
		return objectRoleTop;
	}

	/** get BOTTOM object role */
	public ObjectRoleExpression objectRoleBottom() {
		return objectRoleBottom;
	}

	/** get named object role */
	public ObjectRoleExpression objectRole(final String name) {
		return objectRoleNameset.insert(name);
	}

	/**
	 * get a role chain corresponding to R1 o ... o Rn; take the arguments from
	 * the last argument list
	 */
	public ObjectRoleComplexExpression compose(List<Expression> l) {
		return record(new ObjectRoleChain(l));
	}

	/** get a expression corresponding to R projected from C */
	public ObjectRoleComplexExpression projectFrom(final ObjectRoleExpression R, final ConceptExpression C) {
		return record(new ObjectRoleProjectionFrom(R, C));
	}

	/** get a expression corresponding to R projected into C */
	public ObjectRoleComplexExpression projectInto(final ObjectRoleExpression R, final ConceptExpression C) {
		return record(new ObjectRoleProjectionInto(R, C));
	}

	// data roles
	/** get TOP data role */
	public DataRoleExpression dataRoleTop() {
		return dataRoleTop;
	}

	/** get BOTTOM data role */
	public DataRoleExpression dataRoleBottom() {
		return dataRoleBottom;
	}

	/** get named data role */
	public DataRoleExpression dataRole(final String name) {
		return dataRoleNameset.insert(name);
	}

	// data expressions
	/** get TOP data element */
	public DataExpression dataTop() {
		return dataTop;
	}

	/** get BOTTOM data element */
	public DataExpression dataBottom() {
		return dataBottom;
	}

	/** get basic string data type */
	public String getDataTop() {
		//XXX there is no link between TDLDataTop and the uri
		return Vocabulary.LITERAL;
	}

	/** get basic boolean data type */
	public DataTypeRestriction restrictedType(DataTypeExpression type, final FacetExpression facet) {
		DataTypeRestriction ret = null;
		if (type instanceof DataTypeRestriction) {
			ret = (DataTypeRestriction) type;
		} else {
			// get a type and build an appropriate restriction of it
			DataTypeName hostType = (DataTypeName) type;
			assert hostType != null;
			ret = record(new DataTypeRestriction(hostType));
		}
		ret.add(facet);
		return ret;
	}

	/** get data value with given VALUE and TYPE; */
	public final DataValue dataValue(final String value, DataTypeExpression type) {
		return getBasicDataType(type).getValue(value);
	}

	/** get negation of a data expression E */
	public DataExpression dataNot(final DataExpression E) {
		return record(new DataNot(E));
	}

	/**
	 * get an n-ary data conjunction expression; take the arguments from the
	 * last argument list
	 */
	public DataExpression dataAnd(List<Expression> l) {
		return record(new DataAnd(l));
	}

	/**
	 * get an n-ary data disjunction expression; take the arguments from the
	 * last argument list
	 */
	public DataExpression dataOr(List<Expression> l) {
		return record(new DataOr(l));
	}

	/**
	 * get an n-ary data one-of expression; take the arguments from the last
	 * argument list
	 */
	public DataExpression dataOneOf(List<Expression> l) {
		return record(new DataOneOf(l));
	}

	/** get minInclusive facet with a given VALUE */
	public final FacetExpression facetMinInclusive(final DataValue V) {
		return record(new FacetMinInclusive(V));
	}

	/** get minExclusive facet with a given VALUE */
	public final FacetExpression facetMinExclusive(final DataValue V) {
		return record(new FacetMinExclusive(V));
	}

	/** get maxInclusive facet with a given VALUE */
	public final FacetExpression facetMaxInclusive(final DataValue V) {
		return record(new FacetMaxInclusive(V));
	}

	/** get maxExclusive facet with a given VALUE */
	public final FacetExpression facetMaxExclusive(final DataValue V) {
		return record(new FacetMaxExclusive(V));
	}

	public void clear() {
		conceptNameset.clear();
		individualNameset.clear();
		objectRoleNameset.clear();
		dataRoleNameset.clear();
		inverseRoleCache.clear();
		referenceRecorder.clear();
	}

	private static DataTypeName getBasicDataType(DataTypeExpression type) {
		if (type instanceof DataTypeName) {
			return (DataTypeName) type;
		}
		DataTypeRestriction hostType = (DataTypeRestriction) type;
		return hostType.getExpr();
	}
}