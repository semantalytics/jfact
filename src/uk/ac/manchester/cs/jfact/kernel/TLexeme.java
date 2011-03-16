package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
public class TLexeme {
	/** Lexeme's Token */
	private final Token token;
	/** pointer to information (for names) */
	private final TNamedEntry pName;
	int data;

	private TLexeme(Token tok, TNamedEntry e, int i) {
		token = tok;
		pName = e;
		data = i;
	}

	/** default c'tor for pointers */
	public TLexeme(Token tok, TNamedEntry p) {
		this(tok, p, 0);
	}

	/** default c'tor for pointers */
	public TLexeme(Token tok) {
		this(tok, null, 0);
	}

	/** default c'tor for numbers */
	public TLexeme(Token tok, int val) {
		this(tok, null, val);
	}

	public TLexeme(TLexeme t) {
		this(t.token, t.pName, t.data);
	}

	/** get Token of given Lexeme */
	public Token getToken() {
		return token;
	}

	/** get name pointer of given lexeme */
	public TNamedEntry getNE() {
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
		if (obj instanceof TLexeme) {
			TLexeme lex = (TLexeme) obj;
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
		//		return (pName == null ? "" : pName.toString())
		//				+ (pName == null ? "" : ",") + token.toString()
		//				+ (data == 0 ? "" : "," + data);
		//		return "TLexeme(" + (pName == null ? "null" : pName.toString()) + ","
		//		+ token.toString() + "," + data + ")";
		if (pName == null) {
			return token.TokenName();
		}
		if (token == Token.INAME) {
			return "(" + token.TokenName() + " " + pName.toString() + ")";
		} else {
			return token.TokenName() + " " + pName.toString();
		}
	}
}