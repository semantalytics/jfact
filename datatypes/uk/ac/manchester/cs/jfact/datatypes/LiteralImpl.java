package uk.ac.manchester.cs.jfact.datatypes;

import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitor;
import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitorEx;

class LiteralImpl<T extends Comparable<T>> implements Literal<T> {
	private final Datatype<T> type;
	private final String value;

	//private final T typedValue;
	public LiteralImpl(Datatype<T> type, String value) {
		//,  T  typedValue) {
		this.type = type;
		this.value = value;
		//this.typedValue = typedValue;
	}

	public Datatype<T> getDatatypeExpression() {
		return type;
	}

	public String value() {
		return value;
	}

	public T typedValue() {
		return type.parseValue(value);// typedValue;
	}

	public void accept(DLExpressionVisitor visitor) {
		visitor.visit(this);
	}

	public <O> O accept(DLExpressionVisitorEx<O> visitor) {
		return visitor.visit(this);
	}

	public int compareTo(Literal<T> arg0) {
		return type.parseValue(value).compareTo(arg0.typedValue());
	}

	@Override
	public String toString() {
		return "\"" + value + "\"^^" + type;
	}

	@Override
	public boolean equals(Object obj) {
		if(super.equals(obj)) {
			return true;

		}
		if(obj instanceof Literal) {
			return this.type.equals(((Literal) obj).getDatatypeExpression()) && typedValue().equals(((Literal) obj).typedValue());
		}

		return false;
	}
	@Override
	public int hashCode() {

		return type.hashCode()+typedValue().hashCode();
	}
}
