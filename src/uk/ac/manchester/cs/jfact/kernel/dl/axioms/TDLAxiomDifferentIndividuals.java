package uk.ac.manchester.cs.jfact.kernel.dl.axioms;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.Collection;
import java.util.List;

import uk.ac.manchester.cs.jfact.kernel.dl.TDLNAryExpressionImpl;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLAxiom;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLIndividualExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.TDLNAryExpression;
import uk.ac.manchester.cs.jfact.visitors.DLAxiomVisitor;
import uk.ac.manchester.cs.jfact.visitors.DLAxiomVisitorEx;

/** Different individuals axiom */
public class TDLAxiomDifferentIndividuals extends TDLAxiomImpl implements TDLAxiom, TDLNAryExpression<TDLIndividualExpression> {
	private final TDLNAryExpressionImpl<TDLIndividualExpression> delegate;

	//for i1 != ... != in
	public TDLAxiomDifferentIndividuals(final List<TDLExpression> v) {
		delegate = new TDLNAryExpressionImpl<TDLIndividualExpression>();
		delegate.add(v);
	}

	public <O> O accept(DLAxiomVisitorEx<O> visitor) {
		return visitor.visit(this);
	}

	public void accept(DLAxiomVisitor visitor) {
		visitor.visit(this);
	}

	public TDLIndividualExpression transform(TDLExpression arg) {
		return delegate.transform(arg);
	}

	public void add(TDLExpression p) {
		delegate.add(p);
	}

	public List<TDLIndividualExpression> getArguments() {
		return delegate.getArguments();
	}

	public void add(Collection<TDLExpression> v) {
		delegate.add(v);
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public int size() {
		return delegate.size();
	}
}