package datatypes;

public enum cardinality {
	FINITE, COUNTABLYINFINITE;
	public static cardinality parse(String string) {
		if (string.equals("countably infinite")) {
			return COUNTABLYINFINITE;
		}
		//XXX not the best solution but should work
		return FINITE;
	}
}
