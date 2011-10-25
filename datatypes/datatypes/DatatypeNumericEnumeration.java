package datatypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitor;
import uk.ac.manchester.cs.jfact.visitors.DLExpressionVisitorEx;

public class DatatypeNumericEnumeration<R extends Comparable<R>> extends  DatatypeEnumeration<R> implements NumericDatatype<R>{

	public DatatypeNumericEnumeration(NumericDatatype<R> d) {
		super(d);
	}

	public DatatypeNumericEnumeration(NumericDatatype<R> d, Literal<R> l) {
		this(d);
		literals.add(l);
	}

	public DatatypeNumericEnumeration(NumericDatatype<R> d, Collection<Literal<R>> c) {
		this(d);
		literals.addAll(c);
	}


	public DatatypeNumericEnumeration<R> add(Literal<R> d) {
		DatatypeNumericEnumeration<R> toReturn = new DatatypeNumericEnumeration<R>((NumericDatatype<R>)host, literals);
		toReturn.literals.add(d);
		return toReturn;
	}


	public boolean isNumericDatatype() {
		return true;
	}

	public NumericDatatype<R> asNumericDatatype() {
		return this;
	}

	public boolean isOrderedDatatype() {
		return true;
	}



	public boolean hasMinExclusive() {
		return false;
	}

	public boolean hasMinInclusive() {
		return !literals.isEmpty();
	}

	public boolean hasMaxExclusive() {
		return false;
	}

	public boolean hasMaxInclusive() {
		return !literals.isEmpty();
	}

	public boolean hasMin() {
		return !literals.isEmpty();
	}

	public boolean hasMax() {
		return !literals.isEmpty();
	}

	public BigDecimal getMin() {
		if (literals.isEmpty()) {
			return null;
		}
		return Facets.minInclusive.parseNumber( literals.get(0));
	}

	public BigDecimal getMax() {
		if (literals.isEmpty()) {
			return null;
		}
		return Facets.maxInclusive.parseNumber( literals.get(literals.size()-1));

	}

	public <O extends Comparable<O>> OrderedDatatype<O> asOrderedDatatype() {
		// TODO Auto-generated method stub
		return (OrderedDatatype<O>) this;
	}


}
