package uk.ac.manchester.cs.jfact.visitors;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
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

public interface DLExpressionVisitorEx<O> {
	// concept expressions
	public O visit(TDLConceptTop expr);

	public O visit(TDLConceptBottom expr);

	public O visit(TDLConceptName expr);

	public O visit(TDLConceptNot expr);

	public O visit(TDLConceptAnd expr);

	public O visit(TDLConceptOr expr);

	public O visit(TDLConceptOneOf expr);

	public O visit(TDLConceptObjectSelf expr);

	public O visit(TDLConceptObjectValue expr);

	public O visit(TDLConceptObjectExists expr);

	public O visit(TDLConceptObjectForall expr);

	public O visit(TDLConceptObjectMinCardinality expr);

	public O visit(TDLConceptObjectMaxCardinality expr);

	public O visit(TDLConceptObjectExactCardinality expr);

	public O visit(TDLConceptDataValue expr);

	public O visit(TDLConceptDataExists expr);

	public O visit(TDLConceptDataForall expr);

	public O visit(TDLConceptDataMinCardinality expr);

	public O visit(TDLConceptDataMaxCardinality expr);

	public O visit(TDLConceptDataExactCardinality expr);

	// individual expressions
	public O visit(TDLIndividualName expr);

	// object role expressions
	public O visit(TDLObjectRoleTop expr);

	public O visit(TDLObjectRoleBottom expr);

	public O visit(TDLObjectRoleName expr);

	public O visit(TDLObjectRoleInverse expr);

	public O visit(TDLObjectRoleChain expr);

	public O visit(TDLObjectRoleProjectionFrom expr);

	public O visit(TDLObjectRoleProjectionInto expr);

	// data role expressions
	public O visit(TDLDataRoleTop expr);

	public O visit(TDLDataRoleBottom expr);

	public O visit(TDLDataRoleName expr);

	// data expressions
	public O visit(TDLDataTop expr);

	public O visit(TDLDataBottom expr);

	public O visit(TDLDataTypeName expr);

	public O visit(TDLDataTypeRestriction expr);

	public O visit(TDLDataValue expr);

	public O visit(TDLDataNot expr);

	public O visit(TDLDataAnd expr);

	public O visit(TDLDataOr expr);

	public O visit(TDLDataOneOf expr);

	// facets
	public O visit(TDLFacetMinInclusive expr);

	public O visit(TDLFacetMinExclusive expr);

	public O visit(TDLFacetMaxInclusive expr);

	public O visit(TDLFacetMaxExclusive expr);
}