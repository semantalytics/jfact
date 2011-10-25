package datatypes;

public interface DatatypeCombination<Type, Element> {
	public Type add(Element d);

	public boolean isCompatible(Literal<?> l);

	public String getDatatypeURI();

	public boolean isCompatible(Datatype<?> type);

	public Iterable<Element> getList();

	public boolean emptyValueSpace();

	public Datatype<?> getHost();
}