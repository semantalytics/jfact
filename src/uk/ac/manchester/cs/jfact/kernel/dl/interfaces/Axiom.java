package uk.ac.manchester.cs.jfact.kernel.dl.interfaces;

/* This file is part of the JFact DL reasoner
 Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
 This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import uk.ac.manchester.cs.jfact.split.TSignature;
import uk.ac.manchester.cs.jfact.visitors.DLAxiomVisitor;
import uk.ac.manchester.cs.jfact.visitors.DLAxiomVisitorEx;

public interface Axiom extends Entity {
	/** set the id */
	public void setId(int Id);

	/** get the id */
	public int getId();

	/** set the used flag */
	public void setUsed(boolean Used);

	/** get the value of the used flag */
	public boolean isUsed();

	public boolean isInModule();

	public void setInModule(boolean inModule);

	/** accept method for the visitor pattern */
	public void accept(DLAxiomVisitor visitor);

	public <O> O accept(DLAxiomVisitorEx<O> visitor);

	public void setInSS(boolean flag);

	public boolean isInSS();

	public TSignature getSignature();
}
