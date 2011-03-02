package uk.ac.manchester.cs.jfact.kernel.dl;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLConceptDataCardinalityExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLDataExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLDataRoleExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDataRoleArg;
import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitor;
import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitorEx;

public class TDLConceptDataExactCardinality implements
		TDLConceptDataCardinalityExpression, TDataRoleArg {
	private final int delegate;
	private final TDLDataExpression expression;

	public TDLDataExpression getExpr() {
		return expression;
	}

	public int getNumber() {
		return delegate;
	}

	/** data role argument */
	private final TDLDataRoleExpression DR;

	/** get access to the argument */
	public TDLDataRoleExpression getDR() {
		return DR;
	}

	public TDLConceptDataExactCardinality(int n, final TDLDataRoleExpression R,
			final TDLDataExpression E) {
		DR = R;
		expression = E;
		delegate = n;
	}

	public <O> O accept(DLExpressionVisitorEx<O> visitor) {
		return visitor.visit(this);
	}

	public void accept(DLExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
