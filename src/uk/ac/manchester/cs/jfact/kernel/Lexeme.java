package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
public final class Lexeme {
	/** Lexeme's Token */
	private final Token token;
	/** pointer to information (for names) */
	private final NamedEntry pName;
	int data;

	private Lexeme(Token tok, NamedEntry e, int i) {
		token = tok;
		pName = e;
		data = i;
	}

	/** default c'tor for pointers */
	public Lexeme(Token tok, NamedEntry p) {
		this(tok, p, 0);
	}

	/** default c'tor for pointers */
	public Lexeme(Token tok) {
		this(tok, null, 0);
	}

	/** default c'tor for numbers */
	public Lexeme(Token tok, int val) {
		this(tok, null, val);
	}

	public Lexeme(Lexeme t) {
		this(t.token, t.pName, t.data);
	}

	/** get Token of given Lexeme */
	public Token getToken() {
		return token;
	}

	/** get name pointer of given lexeme */
	public NamedEntry getNE() {
		return pName;
	}

	/** get data value of given lexeme */
	public int getData() {
		return data;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof Lexeme) {
			Lexeme lex = (Lexeme) obj;
			if (!token.equals(lex.token)) {
				return false;
			}
			if (pName == null && lex.pName == null) {
				return data == lex.data;
			}
			if (pName == null) {
				return false;
			}
			return pName.equals(lex.pName);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return token.hashCode() + data;
	}

	@Override
	public String toString() {
		if (pName == null) {
			return token.getName();
		}
		if (token == Token.INAME) {
			return "(" + token.getName() + " " + pName.toString() + ")";
		} else {
			return token.getName() + " " + pName.toString();
		}
	}
}