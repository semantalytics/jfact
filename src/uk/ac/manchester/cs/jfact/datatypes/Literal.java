package uk.ac.manchester.cs.jfact.datatypes;

import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataExpression;

public interface Literal<T extends Comparable<T>> extends DataExpression,
		Comparable<Literal<T>> {
	public Datatype<T> getDatatypeExpression();

	public String value();

	public T typedValue();
}
