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

public interface DLExpressionVisitorEmpty {
	// concept expressions
	void visit(TDLConceptTop expr);

	void visit(TDLConceptBottom expr);

	void visit(TDLConceptName expr);

	void visit(TDLConceptNot expr);

	void visit(TDLConceptAnd expr);

	void visit(TDLConceptOr expr);

	void visit(TDLConceptOneOf expr);

	void visit(TDLConceptObjectSelf expr);

	void visit(TDLConceptObjectValue expr);

	void visit(TDLConceptObjectExists expr);

	void visit(TDLConceptObjectForall expr);

	void visit(TDLConceptObjectMinCardinality expr);

	void visit(TDLConceptObjectMaxCardinality expr);

	void visit(TDLConceptObjectExactCardinality expr);

	void visit(TDLConceptDataValue expr);

	void visit(TDLConceptDataExists expr);

	void visit(TDLConceptDataForall expr);

	void visit(TDLConceptDataMinCardinality expr);

	void visit(TDLConceptDataMaxCardinality expr);

	void visit(TDLConceptDataExactCardinality expr);

	// individual expressions
	void visit(TDLIndividualName expr);

	// object role expressions
	void visit(TDLObjectRoleTop expr);

	void visit(TDLObjectRoleBottom expr);

	void visit(TDLObjectRoleName expr);

	void visit(TDLObjectRoleInverse expr);

	void visit(TDLObjectRoleChain expr);

	void visit(TDLObjectRoleProjectionFrom expr);

	void visit(TDLObjectRoleProjectionInto expr);

	// data role expressions
	void visit(TDLDataRoleTop expr);

	void visit(TDLDataRoleBottom expr);

	void visit(TDLDataRoleName expr);

	// data expressions
	void visit(TDLDataTop expr);

	void visit(TDLDataBottom expr);

	void visit(TDLDataTypeName expr);

	void visit(TDLDataTypeRestriction expr);

	void visit(TDLDataValue expr);

	void visit(TDLDataNot expr);

	void visit(TDLDataAnd expr);

	void visit(TDLDataOr expr);

	void visit(TDLDataOneOf expr);

	// facets
	void visit(TDLFacetMinInclusive expr);

	void visit(TDLFacetMinExclusive expr);

	void visit(TDLFacetMaxInclusive expr);

	void visit(TDLFacetMaxExclusive expr);
}