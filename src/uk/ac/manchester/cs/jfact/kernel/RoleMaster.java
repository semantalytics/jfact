package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import static uk.ac.manchester.cs.jfact.kernel.Token.*;

import java.util.ArrayList;
import java.util.List;


import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

import uk.ac.manchester.cs.jfact.helpers.DLTree;
import uk.ac.manchester.cs.jfact.helpers.DLTreeFactory;
import uk.ac.manchester.cs.jfact.helpers.Helper;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;

public final  class RoleMaster {
	protected static final class RoleCreator implements TNameCreator<TRole> {
		public TRole makeEntry(String name) {
			return new TRole(name);
		}
	}

	/** number of the last registered role */
	private int newRoleId;
	/** all registered roles */
	private final List<TRole> Roles = new ArrayList<TRole>();
	/** internal empty role (bottom in the taxonomy) */
	private final TRole emptyRole;
	/** internal universal role (top in the taxonomy) */
	private final TRole universalRole;
	/** roles nameset */
	private final TNameSet<TRole> roleNS;
	/** Taxonomy of roles */
	private final Taxonomy pTax;
	/** two halves of disjoint roles axioms */
	private final List<TRole> DJRolesA = new ArrayList<TRole>();
	private final List<TRole> DJRolesB = new ArrayList<TRole>();
	/** flag whether to create data roles or not */
	private final boolean DataRoles;
	/** flag if it is possible to introduce new names */
	private boolean useUndefinedNames;
	private static final int firstRoleIndex = 2;

	/** TRole and it's inverse in RoleBox */
	private void registerRole(TRole r) {
		assert r != null && r.getInverse() == null; // sanity check
		assert r.getId() == 0; // only call it for the new roles
		if (DataRoles) {
			r.setDataRole();
		}
		Roles.add(r);
		r.setId(newRoleId);
		// create new role which would be inverse of R
		String iname = "-";
		iname += r.getName();
		TRole ri = new TRole(iname);
		// set up inverse
		r.setInverse(ri);
		ri.setInverse(r);
		Roles.add(ri);
		ri.setId(-newRoleId);
		++newRoleId;
	}

	/** @return true if P is a role that is registered in the RM */
	private boolean isRegisteredRole(final TNamedEntry p) {
		if (!(p instanceof TRole)) {
			return false;
		}
		final TRole R = (TRole) p;
		int ind = R.getIndex();
		return ind >= firstRoleIndex && ind < Roles.size()
				&& Roles.get(ind).equals(p);
	}

	/** get number of roles */
	public int size() {
		return Roles.size() / 2 - 1;
	}

	public RoleMaster(boolean dataRoles, final String TopRoleName,
			final String BotRoleName) {
		newRoleId = 1;
		emptyRole = new TRole(BotRoleName.equals("") ? "emptyRole"
				: BotRoleName);
		universalRole = new TRole(TopRoleName.equals("") ? "universalRole"
				: TopRoleName);
		roleNS = new TNameSet<TRole>(new RoleCreator());
		DataRoles = dataRoles;
		useUndefinedNames = true;
		// no zero-named roles allowed
		Roles.add(null);
		Roles.add(null);
		// setup empty role
		emptyRole.setId(0);
		emptyRole.setInverse(emptyRole);
		emptyRole.setDataRole(dataRoles);
		emptyRole.setBPDomain(Helper.bpBOTTOM);
		// setup universal role
		universalRole.setId(0);
		universalRole.setInverse(universalRole);
		universalRole.setDataRole(dataRoles);
		universalRole.setBPDomain(Helper.bpTOP);
		// create roles taxonomy
		pTax = new Taxonomy(universalRole, emptyRole);
	}

	/** create role entry with given name */
	public TNamedEntry ensureRoleName(final String name) {
		// check for the Top/Bottom names
		if (name.equals(emptyRole.getName())) {
			return emptyRole;
		}
		if (name.equals(universalRole.getName())) {
			return universalRole;
		}
		// new name from NS
		TRole p = roleNS.insert(name);
		// check what happens
		if (p == null) {
			throw new OWLRuntimeException("Unable to register '" + name
					+ "' as a " + (DataRoles ? "data role" : "role"));
		}
		if (isRegisteredRole(p)) {
			return p;
		}
		if (p.getId() != 0 || // not registered but has non-null ID
				!useUndefinedNames) {
			throw new OWLRuntimeException("Unable to register '" + name
					+ "' as a " + (DataRoles ? "data role" : "role"));
		}
		registerRole(p);
		return p;
	}

	/** add parent for the input role */
	public void addRoleParent(TRole role, TRole parent) {
		if (role.isDataRole() != parent.isDataRole()) {
			throw new ReasonerInternalException(
					"Mixed object and data roles in role subsumption axiom");
		}
		role.addParent(parent);
		role.getInverse().addParent(parent.getInverse());
	}

	/** add synonym to existing role */
	public void addRoleSynonym(TRole role, TRole syn) {
		if (!role.equals(syn)) {
			addRoleParent(role, syn);
			addRoleParent(syn, role);
		}
	}

	/** a pair of disjoint roles */
	public void addDisjointRoles(TRole R, TRole S) {
		// object- and data roles are always disjoint
		if (R.isDataRole() != S.isDataRole()) {
			return;
		}
		DJRolesA.add(R);
		DJRolesB.add(S);
	}

	/** change the undefined names usage policy */
	public void setUndefinedNames(boolean val) {
		useUndefinedNames = val;
	}

	public List<TRole> getRoles() {
		return Roles.subList(firstRoleIndex, Roles.size());
	}

	/** get access to the taxonomy */
	public Taxonomy getTaxonomy() {
		return pTax;
	}

	public void Print(LogAdapter o, final String type) {
		if (size() == 0) {
			return;
		}
		o.print(String.format("%s Roles (%s):\n", type, size()));
		for (int i = firstRoleIndex; i < Roles.size(); i++) {
			TRole p = Roles.get(i);
			p.Print(o);
		}
	}

	public boolean hasReflexiveRoles() {
		for (int i = firstRoleIndex; i < Roles.size(); i++) {
			TRole p = Roles.get(i);
			if (p.isReflexive()) {
				return true;
			}
		}
		return false;
	}

	public void fillReflexiveRoles(List<TRole> RR) {
		RR.clear();
		for (int i = firstRoleIndex; i < Roles.size(); i++) {
			TRole p = Roles.get(i);
			if (!p.isSynonym() && p.isReflexive()) {
				RR.add(p);
			}
		}
	}

	public void addRoleParent(DLTree tree, TRole parent) {
		if (tree == null) {
			return;
		}
		if (tree.token() == RCOMPOSITION) {
			parent.addComposition(tree);
			DLTree inv = DLTreeFactory.inverseComposition(tree);
			parent.inverse().addComposition(inv);
			//deleteTree(inv);
		} else if (tree.token() == PROJINTO) {
			TRole R = TRole.resolveRole(tree.Left());
			if (R.isDataRole()) {
				throw new ReasonerInternalException(
						"Projection into not implemented for the data role");
			}
			DLTree C = tree.Right().copy();
			DLTree InvP = DLTreeFactory.buildTree(new TLexeme(RNAME, parent
					.inverse()));
			DLTree InvR = DLTreeFactory.buildTree(new TLexeme(RNAME, R
					.inverse()));
			// C = PROJINTO(PARENT-,C)
			C = DLTreeFactory.buildTree(new TLexeme(PROJINTO), InvP, C);
			// C = PROJFROM(R-,PROJINTO(PARENT-,C))
			C = DLTreeFactory.buildTree(new TLexeme(PROJFROM), InvR, C);
			R.setRange(C);
			//			R.setRange(new Reference<DLTree>(new DLTree(PROJFROM, InvR,
			//					new DLTree(PROJINTO, InvP, C))));
		} else if (tree.token() == PROJFROM) {
			TRole R = TRole.resolveRole(tree.Left());
			DLTree C = tree.Right().copy();
			DLTree P = DLTreeFactory.buildTree(new TLexeme(RNAME, parent));
			// C = PROJINTO(PARENT,C)
			C = DLTreeFactory.buildTree(new TLexeme(PROJINTO), P, C);
			// C = PROJFROM(R,PROJINTO(PARENT,C))
			C = DLTreeFactory.buildTree(new TLexeme(PROJFROM), tree.Left()
					.copy(), C);
			R.setDomain(C);
		} else {
			addRoleParent(TRole.resolveRole(tree), parent);
		}
	}

	public void initAncDesc() {
		int nRoles = Roles.size();
		for (int i = firstRoleIndex; i < Roles.size(); i++) {
			TRole p = Roles.get(i);
			p.eliminateToldCycles();
		}
		for (int i = firstRoleIndex; i < Roles.size(); i++) {
			TRole p = Roles.get(i);
			if (p.isSynonym()) {
				p.canonicaliseSynonym();
				p.addFeaturesToSynonym();
			}
		}
		for (int i = firstRoleIndex; i < Roles.size(); i++) {
			TRole p = Roles.get(i);
			if (!p.isSynonym()) {
				p.removeSynonymsFromParents();
			}
		}
		for (int i = firstRoleIndex; i < Roles.size(); i++) {
			TRole p = Roles.get(i);
			if (!p.isSynonym() && !p.hasToldSubsumers()) {
				p.addParent(universalRole);
			}
		}
		pTax.setCompletelyDefined(true);
		for (int i = firstRoleIndex; i < Roles.size(); i++) {
			TRole p = Roles.get(i);
			if (!p.isClassified()) {
				pTax.classifyEntry(p);
			}
		}
		for (int i = firstRoleIndex; i < Roles.size(); i++) {
			TRole p = Roles.get(i);
			if (!p.isSynonym()) {
				p.initADbyTaxonomy(pTax, nRoles);
			}
		}
		for (int i = firstRoleIndex; i < Roles.size(); i++) {
			TRole p = Roles.get(i);
			if (!p.isSynonym()) {
				p.completeAutomaton(nRoles);
			}
		}
		pTax.finalise();
		if (!DJRolesA.isEmpty()) {
			for (int i = 0; i < DJRolesA.size(); i++) {
				TRole q = DJRolesA.get(i);
				TRole r = DJRolesB.get(i);
				TRole R = ClassifiableEntry.resolveSynonym(q);
				TRole S = ClassifiableEntry.resolveSynonym(r);
				R.addDisjointRole(S);
				S.addDisjointRole(R);
				R.inverse().addDisjointRole(S.inverse());
				S.inverse().addDisjointRole(R.inverse());
			}
			for (int i = firstRoleIndex; i < Roles.size(); i++) {
				TRole p = Roles.get(i);
				if (!p.isSynonym() && p.isDisjoint()) {
					p.checkHierarchicalDisjoint();
				}
			}
		}
		for (int i = firstRoleIndex; i < Roles.size(); i++) {
			TRole p = Roles.get(i);
			if (!p.isSynonym()) {
				p.postProcess();
			}
		}
		for (int i = firstRoleIndex; i < Roles.size(); i++) {
			TRole p = Roles.get(i);
			if (!p.isSynonym()) {
				p.consistent();
			}
		}
	}
}