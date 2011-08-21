package datatypes;

import java.util.Collection;
import java.util.Map;

public interface Datatype {
	public enum cardinality {
		FINITE, COUNTABLYINFINITE
	}

	public interface Facet {}

	//equal facet: implemented by the equals() method on values
	public enum ordered {
		FALSE, PARTIAL, TOTAL
	}

	public Collection<Datatype> getAncestors();

	public boolean getBounded();

	public cardinality getCardinality();

	public Collection<Facet> getFacets();

	public Map<? extends Facet, ? extends Object> getKnownFacetValues();

	public boolean getNumeric();

	public ordered getOrdered();

	/** @return true if this datatype has type as an ancestor */
	public boolean isSubType(Datatype type);

	/**
	 * @return the datatype uri as a string (there does seem to be no need for a
	 *         more complex representation)
	 */
	public String getDatatypeURI();

	/**
	 * @return true if l is a literal with compatible datatype and value included
	 *         in this datatype value space
	 */
	public boolean isCompatible(Literal l);

	public boolean isInValueSpace(Literal l);

	public boolean isCompatible(Datatype type);

	/**
	 * @param datatypes
	 *            list of datatypes to intersect with this datatype to obtain a
	 *            list of values. All datatypes, including this, need to be
	 *            pairwise compatible or the enumeration of values will be
	 *            empty; for datatypes which are not finite, the intersection
	 *            must be finite or an empty collection will be returned.
	 * @return the list of possible values for this datatype which are
	 *         compatible with the listed datatypes.
	 */
	public Collection<Literal> listValues(LiteralFactory factory, Datatype... datatypes);

	/**
	 * parses a literal form to a value in the datatype value space; for use
	 * when building Literals
	 */
	public Object parseValue(String s);
}
