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

public interface DLAxiomVisitor {
	public void visit(TDLAxiomDeclaration axiom);

	public void visit(TDLAxiomEquivalentConcepts axiom);

	public void visit(TDLAxiomDisjointConcepts axiom);

	public void visit(TDLAxiomEquivalentORoles axiom);

	public void visit(TDLAxiomEquivalentDRoles axiom);

	public void visit(TDLAxiomDisjointORoles axiom);

	public void visit(TDLAxiomDisjointDRoles axiom);

	public void visit(TDLAxiomSameIndividuals axiom);

	public void visit(TDLAxiomDifferentIndividuals axiom);

	public void visit(TDLAxiomFairnessConstraint axiom);

	public void visit(TDLAxiomRoleInverse axiom);

	public void visit(TDLAxiomORoleSubsumption axiom);

	public void visit(TDLAxiomDRoleSubsumption axiom);

	public void visit(TDLAxiomORoleDomain axiom);

	public void visit(TDLAxiomDRoleDomain axiom);

	public void visit(TDLAxiomORoleRange axiom);

	public void visit(TDLAxiomDRoleRange axiom);

	public void visit(TDLAxiomRoleTransitive axiom);

	public void visit(TDLAxiomRoleReflexive axiom);

	public void visit(TDLAxiomRoleIrreflexive axiom);

	public void visit(TDLAxiomRoleSymmetric axiom);

	public void visit(TDLAxiomRoleAsymmetric axiom);

	public void visit(TDLAxiomORoleFunctional axiom);

	public void visit(TDLAxiomDRoleFunctional axiom);

	public void visit(TDLAxiomRoleInverseFunctional axiom);

	public void visit(TDLAxiomConceptInclusion axiom);

	public void visit(TDLAxiomInstanceOf axiom);

	public void visit(TDLAxiomRelatedTo axiom);

	public void visit(TDLAxiomRelatedToNot axiom);

	public void visit(TDLAxiomValueOf axiom);

	public void visit(TDLAxiomValueOfNot axiom);

	public void visitOntology(TOntology ontology);

	void visit(TDLAxiomDisjointUnion axiom);
}