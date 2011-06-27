package uk.ac.manchester.cs.jfact.split;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.manchester.cs.jfact.kernel.dl.ObjectRoleInverse;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Expression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.NamedEntity;

/// class to hold the signature of a module
public class TSignature {
	/// set to keep all the elements in signature
	private final Set<NamedEntity> set = new HashSet<NamedEntity>();
	/// true if TOP-locality; false if BOTTOM-locality
	private boolean topLocality;

	public TSignature() {
	}

	/// copy c'tor
	public TSignature(TSignature copy) {
		set.addAll(copy.set);
	}

	/// assignment
	//TODO check references in the original
	//	TSignature& operator= (  TSignature& copy )
	//	{
	//		set = copy.set;
	//		return *this;
	//	}
	// add names to signature
	/// add pointer to named object to signature
	public void add(NamedEntity p) {
		set.add(p);
	}

	/// remove given element from a signature
	public void remove(NamedEntity p) {
		set.remove(p);
	}

	/// set new locality polarity
	public void setLocality(boolean top) {
		topLocality = top;
	}

	// comparison
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof TSignature) {
			return set.equals(((TSignature) obj).set);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return set.hashCode();
	}

	/// check whether 2 signatures are the same
	//	boolean operator == (  TSignature& sig )  { return Set == sig.Set; }
	/// check whether 2 signatures are different
	//	boolean operator != (  TSignature& sig )  { return Set != sig.Set; }
	/// operator <
	//	boolean operator < (  TSignature& sig )  { return Set < sig.Set; }
	/// @return true iff signature contains given element
	public boolean contains(NamedEntity p) {
		return set.contains(p);
	}

	/// @return true iff signature contains given element
	public boolean contains(Expression p) {
		if (p instanceof NamedEntity) {
			return contains((NamedEntity) p);
		}
		if (p instanceof ObjectRoleInverse) {
			return contains(((ObjectRoleInverse) p).getOR());
		}
		return false;
	}

	/// @return size of the signature
	public int size() {
		return set.size();
	}

	/// clear the signature
	public void clear() {
		set.clear();
	}

	public Set<NamedEntity> begin() {
		return set;
	}

	/// @return true iff concepts are treated as TOPs
	public boolean topCLocal() {
		return topLocality;
	}

	/// @return true iff roles are treated as TOPs
	public boolean topRLocal() {
		return topLocality;
	}

	public List<NamedEntity> intersect(TSignature s2) {
		List<NamedEntity> ret = new ArrayList<NamedEntity>();
		Set<NamedEntity> s = new HashSet<NamedEntity>(set);
		s.retainAll(s2.set);
		ret.addAll(s);
		return ret;
	}
}