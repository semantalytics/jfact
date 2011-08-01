package uk.ac.manchester.cs.jfact.kernel.dl.interfaces;

/* This file is part of the JFact DL reasoner
 Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
 This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import java.util.Collection;
import java.util.List;

public interface NAryExpression<Argument extends Expression> {
	/** transform general expression into the argument one */
	public Argument transform(final Expression arg);

	/** add a single element to the array */
	public void add(final Expression p);

	/** add a vector */
	public void add(final Collection<Expression> v);

	/** access to members */
	public List<Argument> getArguments();

	public boolean isEmpty();

	public int size();
}
