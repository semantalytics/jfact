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

public interface DLExpressionVisitorEmpty {
	// concept expressions
	void visit(ConceptTop expr);

	void visit(ConceptBottom expr);

	void visit(ConceptName expr);

	void visit(ConceptNot expr);

	void visit(ConceptAnd expr);

	void visit(ConceptOr expr);

	void visit(ConceptOneOf expr);

	void visit(ConceptObjectSelf expr);

	void visit(ConceptObjectValue expr);

	void visit(ConceptObjectExists expr);

	void visit(ConceptObjectForall expr);

	void visit(ConceptObjectMinCardinality expr);

	void visit(ConceptObjectMaxCardinality expr);

	void visit(ConceptObjectExactCardinality expr);

	void visit(ConceptDataValue expr);

	void visit(ConceptDataExists expr);

	void visit(ConceptDataForall expr);

	void visit(ConceptDataMinCardinality expr);

	void visit(ConceptDataMaxCardinality expr);

	void visit(ConceptDataExactCardinality expr);

	// individual expressions
	void visit(IndividualName expr);

	// object role expressions
	void visit(ObjectRoleTop expr);

	void visit(ObjectRoleBottom expr);

	void visit(ObjectRoleName expr);

	void visit(ObjectRoleInverse expr);

	void visit(ObjectRoleChain expr);

	void visit(ObjectRoleProjectionFrom expr);

	void visit(ObjectRoleProjectionInto expr);

	// data role expressions
	void visit(DataRoleTop expr);

	void visit(DataRoleBottom expr);

	void visit(DataRoleName expr);

	// data expressions
	void visit(DataTop expr);

	void visit(DataBottom expr);

	void visit(DataTypeName expr);

	void visit(DataTypeRestriction expr);

	void visit(DataValue expr);

	void visit(DataNot expr);

	void visit(DataAnd expr);

	void visit(DataOr expr);

	void visit(DataOneOf expr);

	// facets
	void visit(FacetMinInclusive expr);

	void visit(FacetMinExclusive expr);

	void visit(FacetMaxInclusive expr);

	void visit(FacetMaxExclusive expr);
}