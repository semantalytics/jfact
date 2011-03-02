package uk.ac.manchester.cs.jfact.kernel.dl;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import uk.ac.manchester.cs.jfact.kernel.datatype.TDLDataValue;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLConceptDataRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLDataRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDataExpressionArg;
import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitor;
import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitorEx;

public class TDLConceptDataValue implements TDLConceptDataRoleExpression,
		TDataExpressionArg<TDLDataValue> {
	private final TDLDataRoleExpression delegate;

	public TDLConceptDataValue(TDLDataRoleExpression R, TDLDataValue V) {
		Expr = V;
		delegate = R;
	}

	/** data expression argument */
	protected final TDLDataValue Expr;

	/** get access to the argument */
	public TDLDataValue getExpr() {
		return Expr;
	}

	public <O> O accept(DLExpressionVisitorEx<O> visitor) {
		return visitor.visit(this);
	}

	public void accept(DLExpressionVisitor visitor) {
		visitor.visit(this);
	}

	public TDLDataRoleExpression getDR() {
		return delegate;
	}
}