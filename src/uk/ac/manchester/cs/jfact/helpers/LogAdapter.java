package uk.ac.manchester.cs.jfact.helpers;

public interface LogAdapter {
	public void printTemplate(Templates t, Object... strings);

	public void print(int i);

	public void println();

	public void print(double d);

	public void print(float f);

	public void print(boolean b);

	public void print(byte b);

	public void print(char c);

	public void print(short s);

	public void print(String s);

	public void print(Object s);

	public void print(Object... s);

	public void print(Object s1, Object s2);

	public void print(Object s1, Object s2, Object s3);

	public void print(Object s1, Object s2, Object s3, Object s4);

	public void print(Object s1, Object s2, Object s3, Object s4, Object s5);
}
