package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

public enum Token {
	BAD_LEX(""), // = 50,
	UNUSED(""), // never used one
	LEXEOF(""),
	// symbols
	LBRACK(""), RBRACK(""), AND("and"), OR("or"), NOT("not"), INV("inv"), RCOMPOSITION(
			"compose"), // role composition
	PROJINTO("project_into"), // role projection into
	PROJFROM("project_from"), // role projection from
	TOP("*TOP*"), BOTTOM("*BOTTOM*"), EXISTS("some"), FORALL("all"), GE(
			"at-least"),
	//	ATLEAST = GE,
	LE("at-most"),
	//	ATMOST = LE,
	// common metasymbols
	ID(""), // should NOT appear in KB -- use *NAME instead
	NUM(""), DATAEXPR("dataexpr"), // any data expression: data value, [constrained] datatype
	// more precise ID's discretion
	CNAME("cname"), // name of a concept
	INAME("one-of"), // name of a singleton
	RNAME("rname"), // name of a role
	DNAME("dname"), // name of a data role
	// FaCT commands
	// definitions
	PCONCEPT("primconcept"), PROLE(""), PATTR(""), CONCEPT("concept"), DATAROLE(
			""),
	// FaCT++ commands for internal DataTypes
	NUMBER(""), STRING(""), REAL(""), BOOL(""),
	// datatype operations command names -- used only as an external commands
	DTGT(""), DTLT(""), DTGE(""), DTLE(""), DONEOF(""),
	// general commands
	SUBSUMES(""), DISJOINT(""), EQUAL_C(""),
	// new for roles
	INVERSE(""), EQUAL_R(""), IMPLIES_R(""), DISJOINT_R(""), FUNCTIONAL(""), TRANSITIVE(
			""), REFLEXIVE("self-ref"), IRREFLEXIVE(""), SYMMETRIC(""), ANTISYMMETRIC(
			""), ROLERANGE(""), ROLEDOMAIN(""),
	// new for individuals
	DEFINDIVIDUAL(""), INSTANCE(""), RELATED(""), ONEOF(""), SAME(""), DIFFERENT(
			""),
	// fairness constraints
	FAIRNESS("");
	private final String s;

	private Token(String s) {
		this.s = s;
	}

	public String TokenName() {
		if (s.length() > 0) {
			return s;
		}
		throw new ReasonerInternalException("token " + toString()
				+ "has no name");
	}
}