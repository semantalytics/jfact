package uk.ac.manchester.cs.jfact.kernel.dl;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import uk.ac.manchester.cs.jfact.kernel.NamedEntry;
import uk.ac.manchester.cs.jfact.kernel.datatype.DataValue;
import uk.ac.manchester.cs.jfact.kernel.datatype.Datatypes;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataType;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataTypeExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.NamedEntity;
import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitor;
import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitorEx;

public final class DataTypeName extends NamedEntry implements DataTypeExpression, NamedEntity, DataType {
	private final Datatypes datatype;

	public DataTypeName(Datatypes d) {
		super(d.name());
		this.datatype = d;
	}

	public void accept(DLExpressionVisitor visitor) {
		visitor.visit(this);
	}

	public <O> O accept(DLExpressionVisitorEx<O> visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof DataTypeName) {
			return this.datatype == ((DataTypeName) obj).datatype;
		}
		return false;
	}

	public Datatypes getDatatype() {
		return datatype;
	}

	public NamedEntity getEntity() {
		return this;
	}

	@Override
	public String getName() {
		return datatype.name();
	}

	public DataValue getValue(String name) {
		return new DataValue(name, datatype);
	}

	@Override
	public int hashCode() {
		return datatype.hashCode();
	}
}