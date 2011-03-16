package uk.ac.manchester.cs.jfact.kernel;

/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/
import static uk.ac.manchester.cs.jfact.helpers.DLTree.equalTrees;
import static uk.ac.manchester.cs.jfact.helpers.Helper.bpINVALID;
import static uk.ac.manchester.cs.jfact.kernel.Token.RCOMPOSITION;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.reasoner.ReasonerInternalException;

import uk.ac.manchester.cs.jfact.helpers.DLTree;
import uk.ac.manchester.cs.jfact.helpers.DLTreeFactory;
import uk.ac.manchester.cs.jfact.helpers.FastSet;
import uk.ac.manchester.cs.jfact.helpers.FastSetFactory;
import uk.ac.manchester.cs.jfact.helpers.LeveLogger.LogAdapter;
import uk.ac.manchester.cs.jfact.kernel.actors.AddRoleActor;

public class TRole extends ClassifiableEntry {
	static class TKnownValue {
		/** flag value */
		protected boolean value;
		/** whether flag set or not */
		protected boolean known;

		public TKnownValue(boolean val) {
			value = val;
			known = false;
		}

		public TKnownValue() {
			this(false);
		}

		/** @return true iff the value is known to be set */
		protected boolean isKnown() {
			return known;
		}

		/** @return the value */
		protected boolean getValue() {
			return value;
		}

		/** set the value; it is now known */
		protected void setValue(boolean val) {
			value = val;
			known = true;
		}
	}

	/** role that are inverse of given one */
	private TRole Inverse;
	/** Domain of role as a concept description; default null */
	private DLTree pDomain;
	/** Domain of role as a concept description; default NULL */
	private DLTree pSpecialDomain;
	/** domain in the form AR.Range for the complex roles */
	private int bpSpecialDomain;
	/** Domain of role as a pointer to DAG entry */
	private int bpDomain;
	/** pointer to role's functional definition DAG entry (or just TOP) */
	private int Functional;
	/** is role relevant to current query */
	private long rel;
	/** label of a domain (inverse role is used for a range label) */
	private final MergableLabel domLabel = new MergableLabel();
	// for later filling
	private final List<TRole> Ancestor = new ArrayList<TRole>();
	private final List<TRole> Descendant = new ArrayList<TRole>();
	/** set of the most functional super-roles */
	private final List<TRole> TopFunc = new ArrayList<TRole>();
	/** set of the roles that are disjoint with a given one */
	private final Set<TRole> Disjoint = new HashSet<TRole>();
	/** all compositions in the form R1*R2*\ldots*Rn [= R */
	private final List<List<TRole>> subCompositions = new ArrayList<List<TRole>>();
	/** bit-vector of all parents */
	private final FastSet AncMap = FastSetFactory.create();
	/** bit-vector of all roles disjoint with current */
	private final FastSet DJRoles = FastSetFactory.create();
	/** automaton for role */
	private final RoleAutomaton A = new RoleAutomaton();
	/** value for functionality */
	private final TKnownValue Functionality = new TKnownValue();
	/** value for symmetry */
	private final TKnownValue Symmetry = new TKnownValue();
	/** value for asymmetricity */
	private final TKnownValue Asymmetry = new TKnownValue();
	/** value for transitivity */
	private final TKnownValue Transitivity = new TKnownValue();
	/** value for reflexivity */
	private final TKnownValue Reflexivity = new TKnownValue();
	/** value for reflexivity */
	private final TKnownValue Irreflexivity = new TKnownValue();
	/** flag to show that this role needs special R&D processing */
	private boolean SpecialDomain;
	private int index;

	/** add automaton of a sub-role to a given one */
	private void addSubRoleAutomaton(final TRole R) {
		if (equals(R)) {
			return;
		}
		if (R.isSimple()) {
			A.addSimpleRA(R.A);
		} else {
			A.addRA(R.A);
		}
	}

	private void addTrivialTransition(TRole r) {
		A.addTransitionSafe(RoleAutomaton.initial, new RATransition(RoleAutomaton.final_state, r));
	}

	/** get an automaton by a (possibly synonymical) role */
	private final RoleAutomaton completeAutomatonByRole(TRole R, Set<TRole> RInProcess) {
		assert !R.isSynonym(); // no synonyms here
		assert R != this; // no case ...*S*... [= S
		R.completeAutomaton(RInProcess);
		return R.A;
	}

	void mergeSupersDomain() {
		for (int i = 0; i < Ancestor.size(); i++) {
			domLabel.merge(Ancestor.get(i).domLabel);
		}
		// for reflexive role -- merge domain and range labels
		if (isReflexive()) {
			domLabel.merge(getRangeLabel());
		}
		// for R1*R2*...*Rn [= R, merge dom(R) with dom(R1) and ran(R) with ran(Rn)
		for (List<TRole> q : subCompositions) {
			if (!q.isEmpty()) {
				domLabel.merge(q.get(0).domLabel);
				getRangeLabel().merge(q.get(q.size() - 1).getRangeLabel());
			}
		}
	}

	/** get inverse of given role (non-final version) */
	public TRole inverse() {
		assert Inverse != null;
		return resolveSynonym(Inverse);
	}

	/** get real inverse of a role (RO) */
	public TRole realInverse() {
		assert Inverse != null;
		return Inverse;
	}

	/** set inverse to given role */
	public void setInverse(TRole p) {
		assert Inverse == null;
		Inverse = p;
	}

	/** a Simple flag (not simple if role or any of its sub-roles is transitive) */
	public boolean isSimple() {
		return bits.get(Flags.Simple.ordinal());
	}

	public void setSimple() {
		bits.set(Flags.Simple.ordinal());
	}

	public void clearSimple() {
		bits.clear(Flags.Simple.ordinal());
	}

	public void setSimple(boolean action) {
		if (action) {
			bits.set(Flags.Simple.ordinal());
		} else {
			bits.clear(Flags.Simple.ordinal());
		}
	}

	/** flag for recursive walks (used in Automaton creation) */
	public boolean isFinished() {
		return bits.get(Flags.Finished.ordinal());
	}

	public void setFinished() {
		bits.set(Flags.Finished.ordinal());
	}

	public void clearFinished() {
		bits.clear(Flags.Finished.ordinal());
	}

	public void setFinished(boolean action) {
		if (action) {
			bits.set(Flags.Finished.ordinal());
		} else {
			bits.clear(Flags.Finished.ordinal());
		}
	}

	public DLTree getTSpecialDomain() {
		return pSpecialDomain;
	}

	/** @return true iff role has a special domain */
	public boolean hasSpecialDomain() {
		return SpecialDomain;
	}

	/** init special domain; call this only after *ALL* the domains are known */
	public void initSpecialDomain() {
		if (!hasSpecialDomain() || getTRange() == null) {
			pSpecialDomain = DLTreeFactory.createTop();
		} else {
			pSpecialDomain = DLTreeFactory.createSNFForall(DLTreeFactory.buildTree(new TLexeme(Token.RNAME, this)), getTRange().copy());
		}
	}

	/** set the special domain value */
	public void setSpecialDomain(int bp) {
		bpSpecialDomain = bp;
	}

	/** distinguish data- and non-data role */
	public boolean isDataRole() {
		return bits.get(Flags.DataRole.ordinal());
	}

	public void setDataRole() {
		bits.set(Flags.DataRole.ordinal());
	}

	public void clearDataRole() {
		bits.clear(Flags.DataRole.ordinal());
	}

	public void setDataRole(boolean action) {
		if (action) {
			bits.set(Flags.DataRole.ordinal());
		} else {
			bits.clear(Flags.DataRole.ordinal());
		}
	}

	/** test if role is functional (ie, have some functional ancestors) */
	public boolean isFunctional() {
		return Functionality.getValue();
	}

	/** check whether the functionality of a role is known */
	public boolean isFunctionalityKnown() {
		return Functionality.isKnown();
	}

	/** set role functionality value */
	public void setFunctional(boolean value) {
		Functionality.setValue(value);
	}

	/** mark role (topmost) functional */
	public void setFunctional() {
		if (TopFunc.isEmpty()) {
			TopFunc.add(this);
		}
		setFunctional(true);
	}

	// transitivity
	/** check whether the role is transitive */
	public boolean isTransitive() {
		return Transitivity.getValue();
	}

	/** check whether the transitivity of a role is known */
	public boolean isTransitivityKnown() {
		return Transitivity.isKnown();
	}

	/** set the transitivity of both role and it's inverse */
	public void setTransitive(boolean value) {
		Transitivity.setValue(value);
		inverse().Transitivity.setValue(value);
	}

	public void setTransitive() {
		setTransitive(true);
	}

	// symmetry
	/** check whether the role is symmetric */
	public boolean isSymmetric() {
		return Symmetry.getValue();
	}

	/** check whether the symmetry of a role is known */
	public boolean isSymmetryKnown() {
		return Symmetry.isKnown();
	}

	/** set the symmetry of both role and it's inverse */
	public void setSymmetric(boolean value) {
		Symmetry.setValue(value);
		inverse().Symmetry.setValue(value);
	}

	public void setSymmetric() {
		setSymmetric(true);
	}

	// asymmetry
	/** check whether the role is asymmetric */
	public boolean isAsymmetric() {
		return Asymmetry.getValue();
	}

	/** check whether the asymmetry of a role is known */
	public boolean isAsymmetryKnown() {
		return Asymmetry.isKnown();
	}

	/** set the asymmetry of both role and it's inverse */
	public void setAsymmetric(boolean value) {
		Asymmetry.setValue(value);
		inverse().Asymmetry.setValue(value);
	}

	/** check whether the role is reflexive */
	public boolean isReflexive() {
		return Reflexivity.getValue();
	}

	/** check whether the reflexivity of a role is known */
	public boolean isReflexivityKnown() {
		return Reflexivity.isKnown();
	}

	/** set the reflexivity of both role and it's inverse */
	public void setReflexive(boolean value) {
		Reflexivity.setValue(value);
		inverse().Reflexivity.setValue(value);
	}

	public void setReflexive() {
		setReflexive(true);
	}

	// irreflexivity
	/** check whether the role is irreflexive */
	public boolean isIrreflexive() {
		return Irreflexivity.getValue();
	}

	/** check whether the irreflexivity of a role is known */
	public boolean isIrreflexivityKnown() {
		return Irreflexivity.isKnown();
	}

	/** set the irreflexivity of both role and it's inverse */
	public void setIrreflexive(boolean value) {
		Irreflexivity.setValue(value);
		inverse().Irreflexivity.setValue(value);
	}

	public void setIrreflexive() {
		setIrreflexive(true);
	}

	/**
	 * check if the role is topmost-functional (ie, has no functional ancestors)
	 */
	public boolean isTopFunc() {
		return isFunctional() && TopFunc.get(0).equals(this);
	}

	/** set functional attribute to given value (functional DAG vertex) */
	public void setFunctional(int fNode) {
		Functional = fNode;
	}

	/** get the Functional DAG vertex */
	public int getFunctional() {
		return Functional;
	}

	// relevance
	/** is given role relevant to given Labeller's state */
	public boolean isRelevant(final TLabeller lab) {
		return lab.isLabelled(rel);
	}

	/** make given role relevant to given Labeller's state */
	public void setRelevant(final TLabeller lab) {
		rel = lab.getLabel();
	}

	// Sorted reasoning interface
	/** get label of the role's domain */
	public MergableLabel getDomainLabel() {
		return domLabel;
	}

	/** get label of the role's range */
	public MergableLabel getRangeLabel() {
		return inverse().domLabel;
	}

	/** add p to domain of the role */
	public void setDomain(DLTree p) {
		if (equalTrees(pDomain, p)) {
			// usual case when you have a name for inverse role
		} else if (DLTreeFactory.isFunctionalExpr(p, this)) {
			setFunctional();
			// functional restriction in the role domain means the role is functional
		} else {
			pDomain = DLTreeFactory.createSNFAnd(Arrays.asList(pDomain, p));
		}
	}

	/** add p to range of the role */
	public void setRange(DLTree p) {
		inverse().setDomain(p);
	}

	/** get domain-as-a-tree of the role */
	public DLTree getTDomain() {
		return pDomain;
	}

	/** get range-as-a-tree of the role */
	private DLTree getTRange() {
		return inverse().pDomain;
	}

	/** merge to Domain all domains from super-roles */
	public void collectDomainFromSupers() {
		for (int i = 0; i < Ancestor.size(); i++) {
			setDomain(Ancestor.get(i).pDomain.copy());
		}
	}

	/** set domain-as-a-bipointer to a role */
	public void setBPDomain(int p) {
		bpDomain = p;
	}

	/** get domain-as-a-bipointer of the role */
	public int getBPDomain() {
		return bpDomain;
	}

	/** get range-as-a-bipointer of the role */
	public int getBPRange() {
		return inverse().bpDomain;
	}

	// disjoint roles
	/** set R and THIS as a disjoint; use it after Anc/Desc are determined */
	public void addDisjointRole(TRole R) {
		Disjoint.add(R);
		for (TRole p : R.Descendant) {
			Disjoint.add(p);
			p.Disjoint.add(this);
		}
	}

	/** check (and correct) case whether R != S for R [= S */
	public void checkHierarchicalDisjoint() {
		checkHierarchicalDisjoint(this);
		if (isReflexive()) {
			checkHierarchicalDisjoint(inverse());
		}
	}

	/** check whether a role is disjoint with anything */
	public boolean isDisjoint() {
		return !Disjoint.isEmpty();
	}

	/** check whether a role is disjoint with R */
	public boolean isDisjoint(final TRole r) {
		return DJRoles.contains(r.getIndex());
	}

	/** check if role is a non-strict sub-role of R */
	private boolean lesser(TRole r) {
		return isDataRole() == r.isDataRole() && AncMap.contains(r.getIndex());
	}

	protected boolean lesserequal(TRole r) {
		return equals(r) || lesser(r);
	}

	public List<TRole> getAncestor() {
		return Ancestor;
	}

	/** get access to the func super-roles w/o func parents via iterator */
	public List<TRole> begin_topfunc() {
		return TopFunc;
	}

	/** fills BITMAP with the role's ancestors */
	private void addAncestorsToBitMap(FastSet bitmap) {
		//assert(!bitmap.isEmpty()); // use only after the size is known
		for (int i = 0; i < Ancestor.size(); i++) {
			bitmap.add(Ancestor.get(i).getIndex());
		}
	}

	/** add composition to a role */
	public void addComposition(final DLTree tree) {
		List<TRole> RS = new ArrayList<TRole>();
		fillsComposition(RS, tree);
		subCompositions.add(RS);
	}

	/** get access to a RA for the role */
	public RoleAutomaton getAutomaton() {
		return A;
	}

	// completing internal constructions
	/** eliminate told role cycle */
	public TRole eliminateToldCycles() {
		Set<TRole> RInProcess = new HashSet<TRole>();
		List<TRole> ToldSynonyms = new ArrayList<TRole>();
		return eliminateToldCycles(RInProcess, ToldSynonyms);
	}

	/** complete role automaton */
	public void completeAutomaton(int nRoles) {
		Set<TRole> RInProcess = new HashSet<TRole>();
		completeAutomaton(RInProcess);
		A.setup(nRoles, isDataRole());
	}

	/** check whether role description is consistent */
	public void consistent() {
		if (isSimple()) {
			return;
		}
		if (isFunctional()) {
			throw new ReasonerInternalException("Non simple role used as simple: " + getName());
		}
		if (isDataRole()) {
			throw new ReasonerInternalException("Non simple role used as simple: " + getName());
		}
		if (isDisjoint()) {
			throw new ReasonerInternalException("Non simple role used as simple: " + getName());
		}
	}

	// save/load interface; implementation is in SaveLoad.cpp
	private static TRole resolveRoleHelper(final DLTree t) {
		if (t == null) {
			throw new ReasonerInternalException("Role expression expected");
		}
		switch (t.token()) {
			case RNAME: // role name
			case DNAME: // data role name
				return (TRole) t.elem().getNE();
			case INV: // inversion
				return resolveRoleHelper(t.Child()).inverse();
			default: // error
				throw new ReasonerInternalException("Invalid role expression");
		}
	}

	/** @return R or -R for T in the form (inv ... (inv R)...); remove synonyms */
	public static TRole resolveRole(final DLTree t) {
		return resolveSynonym(resolveRoleHelper(t));
	}

	//		TRole implementation
	protected TRole(final String name) {
		super(name);
		index = buildIndex();
		Inverse = null;
		pDomain = null;
		pSpecialDomain = null;
		bpDomain = bpINVALID;
		bpSpecialDomain = bpINVALID;
		Functional = bpINVALID;
		rel = 0;
		SpecialDomain = false;
		setCompletelyDefined(true);
		// role hierarchy is completely defined by it's parents
		addTrivialTransition(this);
	}

	/** get (unsigned) unique index of the role */
	public final int getIndex() {
		return index;
		//		int i = 2 * extId;
		//		return i > 0 ? i : 1 - i;
	}

	private final int buildIndex() {
		int i = 2 * extId;
		return i > 0 ? i : 1 - i;
	}

	@Override
	public void setId(int id) {
		// overriden to update the computed unique index
		super.setId(id);
		index = buildIndex();
	}

	private void fillsComposition(List<TRole> Composition, final DLTree tree) {
		if (tree.token() == RCOMPOSITION) {
			fillsComposition(Composition, tree.Left());
			fillsComposition(Composition, tree.Right());
		} else {
			Composition.add(resolveRole(tree));
		}
	}

	public void addFeaturesToSynonym() {
		if (!isSynonym()) {
			return;
		}
		TRole syn = resolveSynonym(this);
		if (isFunctional() && !syn.isFunctional()) {
			syn.setFunctional();
		}
		if (isTransitive()) {
			syn.setTransitive();
		}
		if (isReflexive()) {
			syn.setReflexive();
		}
		if (isDataRole()) {
			syn.setDataRole();
		}
		if (pDomain != null) {
			syn.setDomain(pDomain.copy());
		}
		if (isDisjoint()) {
			syn.Disjoint.addAll(Disjoint);
		}
		syn.subCompositions.addAll(subCompositions);
		toldSubsumers.clear();
		addParent(syn);
	}

	private TRole eliminateToldCycles(Set<TRole> RInProcess, List<TRole> ToldSynonyms) {
		if (isSynonym()) {
			return null;
		}
		if (RInProcess.contains(this)) {
			ToldSynonyms.add(this);
			return this;
		}
		TRole ret = null;
		RInProcess.add(this);
		removeSynonymsFromParents();
		for (ClassifiableEntry r : toldSubsumers) {
			if ((ret = ((TRole) r).eliminateToldCycles(RInProcess, ToldSynonyms)) != null) {
				if (ret.equals(this)) {
					Collections.sort(ToldSynonyms, new TRoleCompare());
					ret = ToldSynonyms.get(0);
					for (int i = 1; i < ToldSynonyms.size(); i++) {
						TRole p = ToldSynonyms.get(i);
						p.setSynonym(ret);
						ret.addParents(p.getToldSubsumers());
					}
					ToldSynonyms.clear();
					RInProcess.remove(this);
					return ret.eliminateToldCycles(RInProcess, ToldSynonyms);
				} else {
					ToldSynonyms.add(this);
					break;
				}
			}
		}
		RInProcess.remove(this);
		return ret;
	}

	//	private static TRole eliminateToldCycles(TRole root, Set<TRole> RInProcess,
	//			List<TRole> ToldSynonyms) {
	//		if (root.isSynonym()) {
	//			return null;
	//		}
	//		if (RInProcess.contains(root)) {
	//			ToldSynonyms.add(root);
	//			return root;
	//		}
	//		TRole ret = null;
	//		RInProcess.add(root);
	//		root.removeSynonymsFromParents();
	//		for (ClassifiableEntry r : root.told_begin()) {
	//			if ((ret = ((TRole) r)
	//					.eliminateToldCycles(RInProcess, ToldSynonyms)) != null) {
	//				if (ret.equals(root)) {
	//					Collections.sort(ToldSynonyms, new TRoleCompare());
	//					ret = ToldSynonyms.get(0);
	//					for (int i = 1; i < ToldSynonyms.size(); i++) {
	//						TRole p = ToldSynonyms.get(i);
	//						p.setSynonym(ret);
	//						ret.addParents(p.told_begin());
	//					}
	//					ToldSynonyms.clear();
	//					RInProcess.remove(root);
	//					return ret.eliminateToldCycles(RInProcess, ToldSynonyms);
	//				} else {
	//					ToldSynonyms.add(root);
	//					break;
	//				}
	//			}
	//		}
	//		RInProcess.remove(root);
	//		return ret;
	//	}
	@Override
	public String toString() {
		return extName + " " + extId;
		//		LogAdapter b = new LeveLogger.LogAdapterStringBuilder();
		//		Print(b);
		//		return b.toString();
	}

	@Override
	public void Print(LogAdapter o) {
		o.print(String.format("Role \"%s\"(%s)%s%s%s%s%s", getName(), getId(), (isTransitive() ? "T" : ""), (isReflexive() ? "R" : ""), (isTopFunc() ? "t" : ""), (isFunctional() ? "F" : ""), (isDataRole() ? "D" : "")));
		if (isSynonym()) {
			o.print(String.format(" = \"%s\"\n", getSynonym().getName()));
			return;
		}
		if (!toldSubsumers.isEmpty()) {
			o.print(" parents={\"");
			//o.print(toldSubsumers.toString());
			List<ClassifiableEntry> l = new ArrayList<ClassifiableEntry>(toldSubsumers);
			for (int i = 0; i < l.size(); i++) {
				if (i > 0) {
					o.print("\", \"");
				}
				o.print(l.get(i).getName());
			}
			o.print("\"}");
		}
		if (!Disjoint.isEmpty()) {
			o.print(" disjoint with {\"");
			List<TRole> l = new ArrayList<TRole>(Disjoint);
			for (int i = 0; i < Disjoint.size(); i++) {
				if (i > 0) {
					o.print("\", \"");
				}
				o.print(l.get(i).getName());
			}
			o.print("\"}");
		}
		if (pDomain != null) {
			o.print(String.format(" Domain=(%s)=%s", bpDomain, pDomain));
		}
		if (getTRange() != null) {
			o.print(String.format(" Range=(%s)=%s", getBPRange(), getTRange()));
		}
		o.print(String.format("\nAutomaton (size %s): %s%s", A.size(), (A.isISafe() ? "I" : "i"), (A.isOSafe() ? "O" : "o")));
		A.Print(o);
		o.print("\n");
	}

	public void initADbyTaxonomy(Taxonomy pTax, int nRoles) {
		assert isClassified(); // safety check
		assert Ancestor.isEmpty() && Descendant.isEmpty();
		// Note that Top/Bottom are not connected to taxonomy yet.
		// fills ancestors by the taxonomy
		AddRoleActor anc = new AddRoleActor(Ancestor);
		pTax.getRelativesInfo(getTaxVertex(), anc, false, false, true);
		// fills descendants by the taxonomy
		AddRoleActor desc = new AddRoleActor(Descendant);
		pTax.getRelativesInfo(getTaxVertex(), desc, false, false, false);
		// determine Simple attribute
		initSimple();
		// init map for fast Anc/Desc access
		addAncestorsToBitMap(AncMap);
		//	pTax.print(LeveLogger.LL);
	}

	public void postProcess() {
		initTopFunc();
		if (isDisjoint()) {
			initDJMap();
		}
	}

	private void initSimple() {
		assert !isSynonym();
		setSimple(false);
		if (isTransitive() || !subCompositions.isEmpty()) {
			return;
		}
		for (TRole p : Descendant) {
			if (p.isTransitive() || !p.subCompositions.isEmpty()) {
				return;
			}
		}
		setSimple(true);
	}

	private boolean isRealTopFunc() {
		if (!isFunctional()) {
			return false;
		}
		for (int i = 0; i < Ancestor.size(); i++) {
			if (Ancestor.get(i).isTopFunc()) {
				return false;
			}
		}
		return true;
	}

	private void initTopFunc() {
		if (isRealTopFunc()) {
			return;
		}
		if (isTopFunc()) {
			TopFunc.clear();
		}
		for (int i = 0; i < Ancestor.size(); i++) {
			TRole p = Ancestor.get(i);
			if (p.isRealTopFunc()) {
				TopFunc.add(p);
			}
		}
		if (!TopFunc.isEmpty()) {
			Functionality.setValue(true);
		}
	}

	private void checkHierarchicalDisjoint(TRole R) {
		if (Disjoint.contains(R)) {
			setDomain(DLTreeFactory.createBottom());
			Disjoint.clear();
			return;
		}
		for (TRole p : R.Descendant) {
			if (Disjoint.contains(p)) {
				p.setDomain(DLTreeFactory.createBottom());
				Disjoint.remove(p);
				p.Disjoint.clear();
			}
		}
	}

	private void initDJMap() {
		for (TRole q : Disjoint) {
			DJRoles.add(q.getIndex());
		}
	}

	private void preprocessComposition(List<TRole> RS) {
		boolean same = false;
		int last = RS.size() - 1;
		//TODO doublecheck, strange assignments to what is in the list
		for (int i = 0; i < RS.size(); i++) {
			TRole p = RS.get(i);
			TRole R = resolveSynonym(p);
			if (R.isTop()) {
				throw new ReasonerInternalException("Universal role can not be used in role composition chain");
			}
			if (R.isBottom()) {
				RS.clear();
				return;
			}
			if (R.equals(this)) {
				if (i != 0 && i != last) {
					throw new ReasonerInternalException("Cycle in RIA " + getName());
				}
				if (same) {
					if (last == 1) {
						RS.clear();
						setTransitive();
						return;
					} else {
						throw new ReasonerInternalException("Cycle in RIA " + getName());
					}
				} else {
					same = true;
				}
			}
			RS.set(i, R);//p = R;
		}
	}

	private void completeAutomaton(Set<TRole> RInProcess) {
		if (isFinished()) {
			return;
			// if we found a cycle...
		}
		if (RInProcess.contains(this)) {
			throw new ReasonerInternalException("Cycle in RIA " + getName());
		}
		// start processing role
		RInProcess.add(this);
		// make sure that all sub-roles already have completed automata
		for (TRole p : Descendant) {
			p.completeAutomaton(RInProcess);
		}
		// add automata for complex role inclusions
		for (List<TRole> q : subCompositions) {
			addSubCompositionAutomaton(q, RInProcess);
		}
		// check for the transitivity
		if (isTransitive()) {
			A.addTransitionSafe(RoleAutomaton.final_state, new RATransition(RoleAutomaton.initial));
		}
		// here automaton is complete
		setFinished(true);
		for (ClassifiableEntry p : toldSubsumers) {
			TRole R = (TRole) resolveSynonym(p);
			R.addSubRoleAutomaton(this);
			if (hasSpecialDomain()) {
				R.SpecialDomain = true;
			}
		}
		// finish processing role
		RInProcess.remove(this);
	}

	/** add automaton for a role composition */
	private void addSubCompositionAutomaton(List<TRole> RS, Set<TRole> RInProcess) {
		// first preprocess the role chain
		preprocessComposition(RS);
		if (RS.isEmpty()) {
			return;
		}
		// here we need a special treatment for R&D
		SpecialDomain = true;
		// tune iterators and states
		int p = 0;
		int p_last = RS.size() - 1;
		int from = RoleAutomaton.initial, to = RoleAutomaton.final_state;
		if (RS.get(0).equals(this)) {
			++p;
			from = RoleAutomaton.final_state;
		} else if (RS.get(p_last).equals(this)) {
			--p_last;
			to = RoleAutomaton.initial;
		}
		// make sure the role chain contain at least one element
		assert p <= p_last;
		// create a chain
		boolean oSafe = false; // we couldn't assume that the current role automaton is i- or o-safe
		A.initChain(from);
		for (; p != p_last; ++p) {
			oSafe = A.addToChain(completeAutomatonByRole(RS.get(p), RInProcess), oSafe);
		}
		// add the last automaton to chain
		A.addToChain(completeAutomatonByRole(RS.get(p), RInProcess), oSafe, to);
	}

	protected TRole getInverse() {
		return Inverse;
	}
}

class TRoleCompare implements Comparator<TRole>, Serializable {
	public int compare(TRole p, TRole q) {
		int n = p.getId();
		int m = q.getId();
		if (n > 0 && m < 0) {
			return -1;
		}
		if (n < 0 && m > 0) {
			return 1;
		}
		return 0;
	}
}