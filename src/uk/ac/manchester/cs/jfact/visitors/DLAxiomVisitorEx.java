package uk.ac.manchester.cs.jfact.visitors;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import uk.ac.manchester.cs.jfact.kernel.TOntology;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomConceptInclusion;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDRoleDomain;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDRoleFunctional;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDRoleRange;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDRoleSubsumption;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDeclaration;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDifferentIndividuals;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDisjointConcepts;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDisjointDRoles;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDisjointORoles;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomDisjointUnion;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomEquivalentConcepts;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomEquivalentDRoles;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomEquivalentORoles;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomFairnessConstraint;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomInstanceOf;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomORoleDomain;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomORoleFunctional;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomORoleRange;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomORoleSubsumption;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRelatedTo;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRelatedToNot;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRoleAsymmetric;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRoleInverse;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRoleInverseFunctional;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRoleIrreflexive;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRoleReflexive;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRoleSymmetric;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomRoleTransitive;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomSameIndividuals;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomValueOf;
import uk.ac.manchester.cs.jfact.kernel.dl.axioms.TDLAxiomValueOfNot;

public interface DLAxiomVisitorEx<O> {
	public O visit(TDLAxiomDeclaration axiom);

	public O visit(TDLAxiomEquivalentConcepts axiom);

	public O visit(TDLAxiomDisjointConcepts axiom);

	public O visit(TDLAxiomEquivalentORoles axiom);

	public O visit(TDLAxiomEquivalentDRoles axiom);

	public O visit(TDLAxiomDisjointUnion axiom);

	public O visit(TDLAxiomDisjointORoles axiom);

	public O visit(TDLAxiomDisjointDRoles axiom);

	public O visit(TDLAxiomSameIndividuals axiom);

	public O visit(TDLAxiomDifferentIndividuals axiom);

	public O visit(TDLAxiomFairnessConstraint axiom);

	public O visit(TDLAxiomRoleInverse axiom);

	public O visit(TDLAxiomORoleSubsumption axiom);

	public O visit(TDLAxiomDRoleSubsumption axiom);

	public O visit(TDLAxiomORoleDomain axiom);

	public O visit(TDLAxiomDRoleDomain axiom);

	public O visit(TDLAxiomORoleRange axiom);

	public O visit(TDLAxiomDRoleRange axiom);

	public O visit(TDLAxiomRoleTransitive axiom);

	public O visit(TDLAxiomRoleReflexive axiom);

	public O visit(TDLAxiomRoleIrreflexive axiom);

	public O visit(TDLAxiomRoleSymmetric axiom);

	public O visit(TDLAxiomRoleAsymmetric axiom);

	public O visit(TDLAxiomORoleFunctional axiom);

	public O visit(TDLAxiomDRoleFunctional axiom);

	public O visit(TDLAxiomRoleInverseFunctional axiom);

	public O visit(TDLAxiomConceptInclusion axiom);

	public O visit(TDLAxiomInstanceOf axiom);

	public O visit(TDLAxiomRelatedTo axiom);

	public O visit(TDLAxiomRelatedToNot axiom);

	public O visit(TDLAxiomValueOf axiom);

	public O visit(TDLAxiomValueOfNot axiom);

	public O visitOntology(TOntology ontology);
}