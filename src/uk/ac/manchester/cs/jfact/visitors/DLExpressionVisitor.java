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

public interface DLExpressionVisitor {
	// concept expressions
	public void visit(TDLConceptTop expr);

	public void visit(TDLConceptBottom expr);

	public void visit(TDLConceptName expr);

	public void visit(TDLConceptNot expr);

	public void visit(TDLConceptAnd expr);

	public void visit(TDLConceptOr expr);

	public void visit(TDLConceptOneOf expr);

	public void visit(TDLConceptObjectSelf expr);

	public void visit(TDLConceptObjectValue expr);

	public void visit(TDLConceptObjectExists expr);

	public void visit(TDLConceptObjectForall expr);

	public void visit(TDLConceptObjectMinCardinality expr);

	public void visit(TDLConceptObjectMaxCardinality expr);

	public void visit(TDLConceptObjectExactCardinality expr);

	public void visit(TDLConceptDataValue expr);

	public void visit(TDLConceptDataExists expr);

	public void visit(TDLConceptDataForall expr);

	public void visit(TDLConceptDataMinCardinality expr);

	public void visit(TDLConceptDataMaxCardinality expr);

	public void visit(TDLConceptDataExactCardinality expr);

	// individual expressions
	public void visit(TDLIndividualName expr);

	// object role expressions
	public void visit(TDLObjectRoleTop expr);

	public void visit(TDLObjectRoleBottom expr);

	public void visit(TDLObjectRoleName expr);

	public void visit(TDLObjectRoleInverse expr);

	public void visit(TDLObjectRoleChain expr);

	public void visit(TDLObjectRoleProjectionFrom expr);

	public void visit(TDLObjectRoleProjectionInto expr);

	// data role expressions
	public void visit(TDLDataRoleTop expr);

	public void visit(TDLDataRoleBottom expr);

	public void visit(TDLDataRoleName expr);

	// data expressions
	public void visit(TDLDataTop expr);

	public void visit(TDLDataBottom expr);

	public void visit(TDLDataTypeName expr);

	public void visit(TDLDataTypeRestriction expr);

	public void visit(TDLDataValue expr);

	public void visit(TDLDataNot expr);

	public void visit(TDLDataAnd expr);

	public void visit(TDLDataOr expr);

	public void visit(TDLDataOneOf expr);

	// facets
	public void visit(TDLFacetMinInclusive expr);

	public void visit(TDLFacetMinExclusive expr);

	public void visit(TDLFacetMaxInclusive expr);

	public void visit(TDLFacetMaxExclusive expr);
}