package datatypes;

import datatypes.Datatype.Facet;

public class Facets {
	public enum whitespace {
		// 4.3.6 whiteSpace
		/*
		 * [Definition:] whiteSpace constrains the ·value space· of types
		 * ·derived· from string such that the various behaviors specified in
		 * Attribute Value Normalization in [XML 1.0 (Second Edition)] are
		 * realized. The value of whiteSpace must be one of {preserve, replace,
		 * collapse}.
		 */
		//preserve
		/*
		 * No normalization is done, the value is not changed (this is the
		 * behavior required by [XML 1.0 (Second Edition)] for element content)
		 */
		//replace
		/*
		 * All occurrences of #x9 (tab), #xA (line feed) and #xD (carriage
		 * return) are replaced with #x20 (space)
		 */
		//collapse
		/*
		 * After the processing implied by replace, contiguous sequences of
		 * #x20's are collapsed to a single #x20, and leading and trailing
		 * #x20's are removed.
		 */
		/*
		 * NOTE: The notation #xA used here (and elsewhere in this
		 * specification) represents the Universal Character Set (UCS) code
		 * point hexadecimal A (line feed), which is denoted by U+000A. This
		 * notation is to be distinguished from &#xA;, which is the XML
		 * character reference to that same UCS code point.
		 */
		preserve {
			@Override
			public String normalize(String input) {
				return input;
			}
		},
		replace {
			@Override
			public String normalize(String input) {
				return input.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
			}
		},
		collapse {
			@Override
			public String normalize(String input) {
				StringBuilder b = new StringBuilder(input);
				for (int i = 0; i < b.length(); i++) {
					if (b.charAt(i) == ' ') {
						while (i < b.length() - 1 && b.charAt(i + 1) == ' ') {
							b.deleteCharAt(i + 1);
						}
					}
				}
				return b.toString();
			}
		};
		public abstract String normalize(String input);
	}

	private static class FacetImpl implements Facet {
		public FacetImpl() {}
	}

	public static Facet length = new FacetImpl();
	public static Facet minLength = new FacetImpl();
	public static Facet maxLength = new FacetImpl();
	public static Facet totalDigits = new FacetImpl();
	public static Facet fractionDigits = new FacetImpl();
	public static Facet whiteSpace = new FacetImpl();
	public static Facet pattern = new FacetImpl();
	public static Facet enumeration = new FacetImpl();
	public static Facet maxInclusive = new FacetImpl();
	public static Facet maxExclusive = new FacetImpl();
	public static Facet minInclusive = new FacetImpl();
	public static Facet minExclusive = new FacetImpl();
}
