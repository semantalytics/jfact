package uk.ac.manchester.cs.jfact.visitors;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
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

public interface DLExpressionVisitorEx<O> {
	// concept expressions
	public O visit(ConceptTop expr);

	public O visit(ConceptBottom expr);

	public O visit(ConceptName expr);

	public O visit(ConceptNot expr);

	public O visit(ConceptAnd expr);

	public O visit(ConceptOr expr);

	public O visit(ConceptOneOf expr);

	public O visit(ConceptObjectSelf expr);

	public O visit(ConceptObjectValue expr);

	public O visit(ConceptObjectExists expr);

	public O visit(ConceptObjectForall expr);

	public O visit(ConceptObjectMinCardinality expr);

	public O visit(ConceptObjectMaxCardinality expr);

	public O visit(ConceptObjectExactCardinality expr);

	public O visit(ConceptDataValue expr);

	public O visit(ConceptDataExists expr);

	public O visit(ConceptDataForall expr);

	public O visit(ConceptDataMinCardinality expr);

	public O visit(ConceptDataMaxCardinality expr);

	public O visit(ConceptDataExactCardinality expr);

	// individual expressions
	public O visit(IndividualName expr);

	// object role expressions
	public O visit(ObjectRoleTop expr);

	public O visit(ObjectRoleBottom expr);

	public O visit(ObjectRoleName expr);

	public O visit(ObjectRoleInverse expr);

	public O visit(ObjectRoleChain expr);

	public O visit(ObjectRoleProjectionFrom expr);

	public O visit(ObjectRoleProjectionInto expr);

	// data role expressions
	public O visit(DataRoleTop expr);

	public O visit(DataRoleBottom expr);

	public O visit(DataRoleName expr);

	// data expressions
	public O visit(DataTop expr);

	public O visit(DataBottom expr);

	public O visit(DataTypeName expr);

	public O visit(DataTypeRestriction expr);

	public O visit(DataValue expr);

	public O visit(DataNot expr);

	public O visit(DataAnd expr);

	public O visit(DataOr expr);

	public O visit(DataOneOf expr);

	// facets
	public O visit(FacetMinInclusive expr);

	public O visit(FacetMinExclusive expr);

	public O visit(FacetMaxInclusive expr);

	public O visit(FacetMaxExclusive expr);
}