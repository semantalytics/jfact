package datatypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import datatypes.Datatype.Facet;

public class Utils {
	public static List<Facet> getFacets(Facet... facets) {
		List<Facet> toReturn = new ArrayList<Facet>();
		for (Facet f : facets) {
			toReturn.add(f);
		}
		return Collections.unmodifiableList(toReturn);
	}

	public static Set<Datatype> generateAncestors(Datatype d) {
		Set<Datatype> toReturn = new HashSet<Datatype>(d.getAncestors());
		toReturn.add(d);
		return Collections.unmodifiableSet(toReturn);
	}
	//	public static Set<Datatype> getAncestors(Datatype... datatypes) {
	//		Set<Datatype> toReturn = new HashSet<Datatype>();
	//		for (Datatype d : datatypes) {
	//			toReturn.add(d);
	//		}
	//		return Collections.unmodifiableSet(toReturn);
	//	}
}
