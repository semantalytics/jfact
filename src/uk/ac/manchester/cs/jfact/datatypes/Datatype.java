package uk.ac.manchester.cs.jfact.datatypes;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataExpression;

public interface Datatype<Representation extends Comparable<Representation>> extends
		DataExpression {
	/** @return true if this datatype is an expression */
	public boolean isExpression();

	/**
	 * @return this datatype as a datatype expression, if it is an expression.
	 * @throws UnsupportedOperationException
	 *             if this datatype is not an expression (isExpression() returns
	 *             false)
	 */
	public DatatypeExpression<Representation> asExpression();

	/** @return the known ancestors of this datatype */
	public Collection<Datatype<?>> getAncestors();

	/** @return true if this datatype value space is bounded */
	public boolean getBounded();

	/** @return the cardinality of the value space: finite or countably infinite */
	public cardinality getCardinality();

	/**
	 * @return the available facets for this datatype. The collection is
	 *         immutable - only specs sanctioned facets allowed
	 */
	public Set<Facet> getFacets();

	/** @return the known values for a subset of the available facets */
	public Map<Facet, Object> getKnownFacetValues();

	/**
	 * @return known value for facet, or null if there is no known value for the
	 *         facet
	 */
	public <O extends Comparable<O>> O getFacetValue(Facet f);

	public BigDecimal getNumericFacetValue(Facet f);

	/** @return true if this datatype is numeric */
	public boolean getNumeric();

	/** @return the kind of ordering: false, partial or total */
	public ordered getOrdered();

	/**
	 * @return true if type\s value space and this datatype's value space have
	 *         an intersection, e.g., non negative integers and non positive
	 *         integers intersect at 0
	 */
	public boolean isCompatible(Datatype<?> type);

	/**
	 * @return true if l is a literal with compatible datatype and value
	 *         included in this datatype value space
	 */
	public boolean isCompatible(Literal<?> l);

	/**
	 * @return false if this literal representation does not represent a value
	 *         included in the value space of this datatype; its datatype must
	 *         be this datatype
	 */
	public boolean isInValueSpace(Representation l);

	/**
	 * parses a literal form to a value in the datatype value space; for use
	 * when building Literals
	 */
	public Representation parseValue(String s);

	/**
	 * @return a literal with parseValue(s) as typed value, generic type O equal
	 *         to the internal class representing the type, and datatype this
	 *         datatype.
	 */
	public Literal<Representation> buildLiteral(String s);

	/** @return true if this datatype has type as an ancestor */
	public boolean isSubType(Datatype<?> type);

	/**
	 * @return the datatype uri as a string (there does seem to be no need for a
	 *         more complex representation)
	 */
	public String getDatatypeURI();

	/**
	 * @return the list of possible values for this datatype which are
	 *         compatible with the listed datatypes.
	 */
	public Collection<Literal<Representation>> listValues();

	public boolean isNumericDatatype();

	public NumericDatatype<Representation> asNumericDatatype();

	public boolean isOrderedDatatype();

	public <O extends Comparable<O>> OrderedDatatype<O> asOrderedDatatype();
}
