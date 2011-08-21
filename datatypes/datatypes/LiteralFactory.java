package datatypes;

import org.semanticweb.owlapi.model.OWLLiteral;

public class LiteralFactory {
	private static class LiteralImpl implements Literal {
		private final Datatype type;
		private final String value;
		private final Object typedValue;

		public LiteralImpl(Datatype type, String value, Object typedValue) {
			this.type = type;
			this.value = value;
			this.typedValue = typedValue;
		}

		public Datatype getDatatype() {
			return type;
		}

		public String value() {
			return value;
		}

		public <O> O typedValue() {
			return (O) typedValue;
		}
	}

	private final DatatypeFactory factory;

	private LiteralFactory(DatatypeFactory f) {
		factory = f;
	}

	public static LiteralFactory getLiteralFactory(DatatypeFactory factory) {
		if (factory == null) {
			throw new IllegalArgumentException("The datatype factory cannot be null");
		}
		return new LiteralFactory(factory);
	}

	public Literal parse(OWLLiteral literal) {
		final String iri = literal.getDatatype().getIRI().toString();
		if (!factory.isKnownDatatype(iri)) {
			throw new IllegalArgumentException("the type: " + iri
					+ " is not supported by the Datatype factory");
		}
		return parse(factory.getKnownDatatype(iri), literal.getLiteral());
	}

	public Literal parse(Datatype d, String l) {
		return new LiteralImpl(d, l, d.parseValue(l));
	}
}
