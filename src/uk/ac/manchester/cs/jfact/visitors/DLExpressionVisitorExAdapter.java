package uk.ac.manchester.cs.jfact.visitors;

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

@SuppressWarnings("unused")
public abstract class DLExpressionVisitorExAdapter<A> implements
		DLExpressionVisitorEx<A> {
	public A visit(ConceptTop expr) {
		return null;
	}

	public A visit(ConceptBottom expr) {
		return null;
	}

	public A visit(ConceptName expr) {
		return null;
	}

	public A visit(ConceptNot expr) {
		return null;
	}

	public A visit(ConceptAnd expr) {
		return null;
	}

	public A visit(ConceptOr expr) {
		return null;
	}

	public A visit(ConceptOneOf expr) {
		return null;
	}

	public A visit(ConceptObjectSelf expr) {
		return null;
	}

	public A visit(ConceptObjectValue expr) {
		return null;
	}

	public A visit(ConceptObjectExists expr) {
		return null;
	}

	public A visit(ConceptObjectForall expr) {
		return null;
	}

	public A visit(ConceptObjectMinCardinality expr) {
		return null;
	}

	public A visit(ConceptObjectMaxCardinality expr) {
		return null;
	}

	public A visit(ConceptObjectExactCardinality expr) {
		return null;
	}

	public A visit(ConceptDataValue expr) {
		return null;
	}

	public A visit(ConceptDataExists expr) {
		return null;
	}

	public A visit(ConceptDataForall expr) {
		return null;
	}

	public A visit(ConceptDataMinCardinality expr) {
		return null;
	}

	public A visit(ConceptDataMaxCardinality expr) {
		return null;
	}

	public A visit(ConceptDataExactCardinality expr) {
		return null;
	}

	public A visit(IndividualName expr) {
		return null;
	}

	public A visit(ObjectRoleTop expr) {
		return null;
	}

	public A visit(ObjectRoleBottom expr) {
		return null;
	}

	public A visit(ObjectRoleName expr) {
		return null;
	}

	public A visit(ObjectRoleInverse expr) {
		return null;
	}

	public A visit(ObjectRoleChain expr) {
		return null;
	}

	public A visit(ObjectRoleProjectionFrom expr) {
		return null;
	}

	public A visit(ObjectRoleProjectionInto expr) {
		return null;
	}

	public A visit(DataRoleTop expr) {
		return null;
	}

	public A visit(DataRoleBottom expr) {
		return null;
	}

	public A visit(DataRoleName expr) {
		return null;
	}

	public A visit(DataTop expr) {
		return null;
	}

	public A visit(DataBottom expr) {
		return null;
	}

	public A visit(DataTypeName expr) {
		return null;
	}

	public A visit(DataTypeRestriction expr) {
		return null;
	}

	public A visit(DataValue expr) {
		return null;
	}

	public A visit(DataNot expr) {
		return null;
	}

	public A visit(DataAnd expr) {
		return null;
	}

	public A visit(DataOr expr) {
		return null;
	}

	public A visit(DataOneOf expr) {
		return null;
	}

	public A visit(FacetMinInclusive expr) {
		return null;
	}

	public A visit(FacetMinExclusive expr) {
		return null;
	}

	public A visit(FacetMaxInclusive expr) {
		return null;
	}

	public A visit(FacetMaxExclusive expr) {
		return null;
	}
}
