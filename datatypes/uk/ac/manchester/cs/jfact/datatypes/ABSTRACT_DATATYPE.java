package uk.ac.manchester.cs.jfact.datatypes;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitor;
import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitorEx;

abstract class ABSTRACT_DATATYPE<R extends Comparable<R>> implements Datatype<R> {
	protected final Set<Facet> facets;
	protected Set<Datatype<?>> ancestors;
	protected final Map<Facet, Object> knownFacetValues = new HashMap<Facet, Object>();
	protected final String uri;

	public ABSTRACT_DATATYPE(String u, Set<Facet> f) {
		facets = Collections.unmodifiableSet(f);
		this.uri = u;
	}

	public String getDatatypeURI() {
		return this.uri;
	}

	@Override
	public int hashCode() {
		return uri.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj instanceof Datatype<?>) {
			return this.uri.equals(((Datatype<?>) obj).getDatatypeURI());
		}
		return false;
	}

	public Collection<Datatype<?>> getAncestors() {
		return ancestors;
	}

	public Set<Facet> getFacets() {
		return facets;
	}

	public Map<Facet, Object> getKnownFacetValues() {
		return new HashMap<Facet, Object>(knownFacetValues);
	}

	public <O extends Comparable<O>> O getFacetValue(Facet f) {
		if (knownFacetValues.containsKey(f)) {
			if (f.isNumberFacet()) {
				return (O) getNumericFacetValue(f);
			} else {
				return (O) f.parse(knownFacetValues.get(f));
			}
		}
		return null;
	}

	public BigDecimal getNumericFacetValue(Facet f) {
		if (knownFacetValues.containsKey(f)) {
			if (f.isNumberFacet()) {
				return f.parseNumber(knownFacetValues.get(f));
			}
		}
		return null;
	}

	public boolean isSubType(Datatype<?> type) {
		return ancestors.contains(type) || this.equals(type);
	}

	@Override
	public String toString() {
		String datatypeURI = getDatatypeURI();
		datatypeURI = datatypeURI.substring(datatypeURI.lastIndexOf('#'));
		return datatypeURI;
	}

	public boolean isCompatible(Datatype<?> type) {
		if(type.isExpression()) {

			type=type.asExpression().getHostType();
		}
		return type.equals(this)
				|| type.equals(DatatypeFactory.LITERAL)
				|| type.isSubType(this)
				|| this.isSubType(type);
	}

	public boolean isCompatible(Literal<?> l) {
		if (!this.isCompatible(l.getDatatypeExpression())) {
			return false;
		}
		try {
			R value = parseValue(l.value());
			return isInValueSpace(value);
		} catch (RuntimeException e) {
			// parsing exceptions will be caught here
			return false;
		}
	}

	// most common answer; restrictions on value spaces to be tested in subclasses
	public boolean isInValueSpace(R l) {
		return true;
	}

	public void accept(DLExpressionVisitor visitor) {
		visitor.visit(this);
	}

	public <O> O accept(DLExpressionVisitorEx<O> visitor) {
		return visitor.visit(this);
	}

	public boolean isExpression() {
		return false;
	}

	public DatatypeExpression<R> asExpression() {
		if (!isExpression()) {
			throw new UnsupportedOperationException("Type: " + getDatatypeURI()
					+ " is not an expression");
		}
		return (DatatypeExpression<R>) this;
	}

	public Literal<R> buildLiteral(String s) {
		if (getNumeric()) {
			return new NumericLiteralImpl<R>(this.asNumericDatatype(), s);
		}
		return new LiteralImpl<R>(this, s);
	}

	public Collection<Literal<R>> listValues() {
		return Collections.emptyList();
	}

	public boolean getBounded() {
		return false;
	}

	public boolean getNumeric() {
		return false;
	}

	public ordered getOrdered() {
		return ordered.FALSE;
	}

	public cardinality getCardinality() {
		return cardinality.COUNTABLYINFINITE;
	}

	<T extends Comparable<T>> boolean overlapping(OrderedDatatype<T> first,
			OrderedDatatype<T> second) {
		if (first.hasMaxInclusive() && second.hasMinInclusive()) {
			return first.getMax().compareTo(second.getMin()) >= 0;
		}
		T minSecond = second.getMin();
		if (first.hasMaxExclusive() && minSecond != null) {
			return first.getMax().compareTo(minSecond) > 0;
		}
		// if we get here, first has no max, hence it's unbounded upwards
		return false;
	}

	public boolean isNumericDatatype() {
		return false;
	}

	public NumericDatatype<R> asNumericDatatype() {
		return null;
	}

	public boolean isOrderedDatatype() {
		return false;
	}

	public <O extends Comparable<O>> OrderedDatatype<O> asOrderedDatatype() {
		return null;
	}
}