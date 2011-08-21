package datatypes;

public interface Literal {
	public Datatype getDatatype();

	public String value();

	public <O> O typedValue();
}
