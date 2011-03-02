package uk.ac.manchester.cs.jfact.kernel;
/* This file is part of the JFact DL reasoner
Copyright 2011 by Ignazio Palmisano, Dmitry Tsarkov, University of Manchester
This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version. 
This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA*/

import static uk.ac.manchester.cs.jfact.helpers.Helper.*;
import static uk.ac.manchester.cs.jfact.kernel.Token.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uk.ac.manchester.cs.jfact.helpers.DLTree;
import uk.ac.manchester.cs.jfact.helpers.DLTreeFactory;
import uk.ac.manchester.cs.jfact.helpers.FastSet;
import uk.ac.manchester.cs.jfact.helpers.FastSetFactory;
import uk.ac.manchester.cs.jfact.helpers.IfDefs;
import uk.ac.manchester.cs.jfact.helpers.UnreachableSituationException;


public class TConcept extends ClassifiableEntry {
	public static final TConcept BOTTOM = new TConcept("BOTTOM");
	public static final TConcept TOP = new TConcept("TOP");

	public static TConcept getTEMP() {
		TConcept TEMP = new TConcept(" ");
		TEMP.setId(-1);
		TEMP.setTsDepth(1);
		TEMP.setClassTag(CTTag.cttTrueCompletelyDefined);
		return TEMP;
	}

	static {
		BOTTOM.setId(-1);
		BOTTOM.setpName(bpBOTTOM);
		BOTTOM.setpBody(bpBOTTOM);
		TOP.setId(-1);
		TOP.setpName(bpTOP);
		TOP.setpBody(bpTOP);
		TOP.setTsDepth(1);
		TOP.setClassTag(CTTag.cttTrueCompletelyDefined);
	}

	/** type of concept wrt classifiability */
	public enum CTTag {
		/** not specified */
		cttUnspecified('u'),
		/** concept with all parents -- TCD */
		cttTrueCompletelyDefined('T'),
		/** concept w/o any told subsumers */
		cttOrphan('O'),
		/** concept with all parents -- LCD, TCD or Orptans */
		cttLikeCompletelyDefined('L'),
		/** concept with non-primitive TS */
		cttHasNonPrimitiveTS('N'),
		/** any other primitive concept */
		cttRegular('r'),
		/** any non-primitive concept (except synonyms) */
		cttNonPrimitive('n');
		private final char c;

		private CTTag(char c) {
			this.c = c;
		}

		protected char getCTTagName() {
			return c;
		}
	}

	/** label to use in relevant-only checks */
	private long rel;
	/**
	 * classification type of concept: completely defined (true- or like-), no
	 * TS, other
	 */
	private CTTag classTag;
	/** depth of the concept wrt told subsumers */
	private int tsDepth;
	/** pointer to the entry in DAG with concept name */
	private int pName;
	/** pointer to the entry in DAG with concept definition */
	private int pBody;
	/** features for C */
	private final LogicFeatures posFeatures = new LogicFeatures();
	/** features for ~C */
	private final LogicFeatures negFeatures = new LogicFeatures();
	/** all extra rules for a given concept */
	private final FastSet erSet = FastSetFactory.create();
	protected DLTree Description;

	/**
	 * adds concept as a told subsumer of current one; @return value for CDC
	 * analisys
	 */
	private boolean addToldSubsumer(TConcept p) {
		if (p != this) {
			addParentIfNew(p);
			if (p.isSingleton() || p.isHasSP()) {
				setHasSP(); // this has singleton parent
			}
		}
		// if non-primitive concept was found in a description, it's not CD
		return p.isPrimitive();
	}

	public TConcept(final String name) {
		super(name);
		rel = 0;
		classTag = CTTag.cttUnspecified;
		tsDepth = 0;
		pName = bpINVALID;
		pBody = bpINVALID;
		setPrimitive();
	}

	/** add index of a simple rule in TBox to the ER set */
	public void addExtraRule(int p) {
		erSet.add(p);
		setCompletelyDefined(false);
	}

	/** check if a concept is in a disjoint relation with anything */
	public boolean hasExtraRules() {
		return !erSet.isEmpty();
	}

	/** iterator for accessing DJ elements */
	public FastSet er_begin() {
		return erSet;
	}

	/** check whether a concept is indeed a singleton */
	public boolean isSingleton() {
		return false;
	}

	protected CTTag getClassTagPlain() {
		return classTag;
	}

	/** get value of a tag; determine it if unset */
	protected CTTag getClassTag() {
		if (classTag == CTTag.cttUnspecified) {
			classTag = determineClassTag();
		}
		return classTag;
	}

	/** remove concept from its own definition (like in case C [= (or C ...) */
	public void removeSelfFromDescription() {
		Description = replaceWithConstOld(Description);//treeReplaceConst(Description);
		initToldSubsumers();
	}

	/** remove concept description (to save space) */
	public void removeDescription() {
		Description = null;
	}

	/**
	 * check whether it is possible to init this as a non-primitive concept with
	 * DESC
	 */
	public boolean canInitNonPrim(DLTree desc) {
		if (Description == null) {
			return true;
		}
		if (isNonPrimitive() && Description.equals(desc)) {
			return true;
		}
		return false;
	}

	/**
	 * switch primitive concept to non-primitive with new definition; @return
	 * old definition
	 */
	public DLTree makeNonPrimitive(DLTree desc) {
		DLTree ret = Description;
		removeDescription();
		addDesc(desc);
		setPrimitive(false);
		return ret;
	}

	@Override
	public String toString() {
		if (IfDefs.RKG_DEBUG_ABSORPTION || IfDefs._USE_LOGGING) {
			String frag = extName.substring(extName.lastIndexOf("/") + 1);
			return frag.substring(frag.lastIndexOf("#") + 1);
		}
		return extName;
	}

	/** init told subsumers of the concept by it's description */
	public void initToldSubsumers() {
		toldSubsumers.clear();
		clearHasSP();
		// normalise description if the only parent is TOP
		if (isPrimitive() && Description != null && Description.isTOP()) {
			removeDescription();
		}
		boolean CD = !hasExtraRules() && isPrimitive(); // not a completely defined if there are extra rules
		if (Description != null) {
			CD &= initToldSubsumers(Description, new HashSet<TRole>());
		}
		setCompletelyDefined(CD);
	}

	/** init TOP told subsumer if necessary */
	public void setToldTop(TConcept top) {
		if (Description == null && !hasToldSubsumers()) {
			addParent(top);
		}
	}

	public int resolveId() {
		//if (getId() == -1) // it is special concept: Top or Bottom
		if (pName == bpINVALID) {
			return pBody;
		}
		if (isSynonym()) {
			TConcept r = resolveSynonym(this);
			if (r != this) {
				return r.resolveId();
			}
		}
		return pName; // return concept's name
	}

	public void addDesc(DLTree Desc) {
		if (Desc == null) {
			return;
		}
		assert !isNonPrimitive();
		if (Desc.isAND()) {
			if (Description == null) {
				Description = Desc.copy();
			} else {
				if (Description.isAND()) {
					for (DLTree c : Desc.Children()) {
						Description.addChild(c);
					}
				} else {
					// if it's not an AND then a new AND must be created
					DLTree temp = Description;
					Description = Desc.copy();
					Description.addChild(temp);
				}
			}
		} else {
			if (Description == null) {
				Description = Desc.copy();
			} else {
				if (Description.isAND()) {
					Description.addChild(Desc);
				} else {
					Description = DLTreeFactory.createSNFAnd(Description, Desc);
				}
			}
		}
	}

	public void addLeaves(Collection<DLTree> Desc) {
		assert !isNonPrimitive();
		if (Description == null) {
			Description = DLTreeFactory.createSNFAnd(Desc);
		} else {
			if (Description.isAND()) {
				for (DLTree d : Desc) {
					Description.addChild(d);
				}
			} else {
				List<DLTree> l = new ArrayList<DLTree>(Desc);
				l.add(Description);
				Description = DLTreeFactory.createSNFAnd(l);
			}
		}
	}

	private CTTag determineClassTag() {
		if (isSynonym()) {
			return resolveSynonym(this).getClassTag();
		}
		if (!isCompletelyDefined()) {
			return CTTag.cttRegular;
		}
		if (isNonPrimitive()) {
			return CTTag.cttNonPrimitive;
		}
		if (!hasToldSubsumers()) {
			return CTTag.cttOrphan;
		}
		for (ClassifiableEntry p : toldSubsumers) {
			//XXX should not be needed
			if (!p.getToldSubsumers().contains(this)) {
				switch (((TConcept) p).getClassTag()) {
					case cttTrueCompletelyDefined:
						break;
					case cttOrphan:
					case cttLikeCompletelyDefined:
						return CTTag.cttLikeCompletelyDefined;
					case cttRegular:
						return CTTag.cttRegular;
					case cttHasNonPrimitiveTS:
					case cttNonPrimitive:
						return CTTag.cttHasNonPrimitiveTS;
					default:
						throw new UnreachableSituationException();
				}
			}
		}
		return CTTag.cttTrueCompletelyDefined;
	}

	private static final EnumSet<Token> replacements = EnumSet.of(CNAME, INAME,
			RNAME, DNAME);

	public void push(LinkedList<DLTree> stack, DLTree current) {
		// push subtrees: stack size increases by one or two, or current is a leaf
		for (DLTree t : current.Children()) {
			if (t != null) {
				stack.push(t);
			}
		}
	}

	private DLTree replaceWithConstOld(DLTree t) {
		if (t == null) {
			return null;
		}
		Token token = t.token();
		// the three ifs are actually exclusive
		if (replacements.contains(token)
				&& resolveSynonym((ClassifiableEntry) t.elem().getNE()).equals(
						this)) {
			return DLTreeFactory.createTop();
		}
		if (token == AND) {
			List<DLTree> l = new ArrayList<DLTree>();
			for (DLTree d : t.Children()) {
				l.add(replaceWithConstOld(d));
			}
			return DLTreeFactory.createSNFAnd(l, t);
		}
		if (token == NOT) {
			if (t.Child().isAND() || replacements.contains(t.Child().token())) {
				return DLTreeFactory
						.createSNFNot(replaceWithConstOld(t.Child()));
			}
		}
		return t;
	}

	/**
	 * init told subsumers of the concept by given DESCription; @return TRUE iff
	 * concept is CD
	 */
	public boolean initToldSubsumers(final DLTree _desc,
			Set<TRole> RolesProcessed) {
		if (_desc == null || _desc.isTOP()) {
			return true;
		}
		DLTree desc = _desc;
		Token token = desc.token();
		if (replacements.contains(token)) {
			return addToldSubsumer((TConcept) desc.elem().getNE());
		}
		if (token == NOT) {
			if (desc.Child().token() == FORALL || desc.Child().token() == LE) {
				SearchTSbyRoleAndSupers(TRole.resolveRole(desc.Child().Left()),
						RolesProcessed);
			}
			return false;
		}
		if (token == REFLEXIVE) {
			final TRole R = TRole.resolveRole(desc.Child());
			SearchTSbyRoleAndSupers(R, RolesProcessed);
			SearchTSbyRoleAndSupers(R.inverse(), RolesProcessed);
			return false;
		}
		if (token == AND) {
			// push all AND children on the list and traverse the list removing n-th level ANDs and pushing their children in turn; ends up with the leaves of the AND subtree
			boolean toReturn = true;
			for (DLTree t : desc.Children()) {
				toReturn &= initToldSubsumers(t, RolesProcessed);
			}
			return toReturn;
		}
		return false;
	}

	private void SearchTSbyRole(final TRole R, Set<TRole> RolesProcessed) {
		if (RolesProcessed.contains(R)) {
			return;
		}
		DLTree Domain = R.getTDomain();
		if (Domain == null || Domain.isConst()) {
			return;
		}
		RolesProcessed.add(R);
		initToldSubsumers(Domain, RolesProcessed);
	}

	public void SearchTSbyRoleAndSupers(final TRole r, Set<TRole> RolesProcessed) {
		SearchTSbyRole(r, RolesProcessed);
		List<TRole> list = r.getAncestor();
		for (int i = 0; i < list.size(); i++) {
			TRole q = list.get(i);
			SearchTSbyRole(q, RolesProcessed);
		}
	}

	public int calculateTSDepth() {
		if (tsDepth > 0) {
			return tsDepth;
		}
		int max = 0;
		for (ClassifiableEntry p : toldSubsumers) {
			//XXX should not be needed
			if (!p.getToldSubsumers().contains(this)) {
				int cur = ((TConcept) p).calculateTSDepth();
				if (max < cur) {
					max = cur;
				}
			}// else both nodes are each other subsumers: same depth?
		}
		return tsDepth = max + 1;
	}

	protected int getpName() {
		return pName;
	}

	protected void setpName(int pName) {
		this.pName = pName;
	}

	protected int getpBody() {
		return pBody;
	}

	protected void setpBody(int pBody) {
		this.pBody = pBody;
	}

	protected DLTree getDescription() {
		return Description;
	}

	protected int getTsDepth() {
		return tsDepth;
	}

	protected void setTsDepth(int tsDepth) {
		this.tsDepth = tsDepth;
	}

	protected LogicFeatures getNegFeatures() {
		return negFeatures;
	}

	protected LogicFeatures getPosFeatures() {
		return posFeatures;
	}

	protected void setClassTag(CTTag classTag) {
		this.classTag = classTag;
	}

	public boolean isPrimitive() {
		return bits.get(Flags.Primitive.ordinal());
	}

	public void setPrimitive() {
		bits.set(Flags.Primitive.ordinal());
	}

	public void clearPrimitive() {
		bits.clear(Flags.Primitive.ordinal());
	}

	private void setPrimitive(boolean action) {
		if (action) {
			bits.set(Flags.Primitive.ordinal());
		} else {
			bits.clear(Flags.Primitive.ordinal());
		}
	}

	/** a HasSingletonParent flag */
	public boolean isHasSP() {
		return bits.get(Flags.HasSP.ordinal());
	}

	private void setHasSP() {
		bits.set(Flags.HasSP.ordinal());
	}

	public void clearHasSP() {
		bits.clear(Flags.HasSP.ordinal());
	}

	public void setHasSP(boolean action) {
		if (action) {
			bits.set(Flags.HasSP.ordinal());
		} else {
			bits.clear(Flags.HasSP.ordinal());
		}
	}

	public boolean isNominal() {
		return bits.get(Flags.Nominal.ordinal());
	}

	public void setNominal(boolean action) {
		if (action) {
			bits.set(Flags.Nominal.ordinal());
		} else {
			bits.clear(Flags.Nominal.ordinal());
		}
	}

	// concept non-primitivity methods
	/** check if concept is non-primitive concept */
	public boolean isNonPrimitive() {
		return !isPrimitive();
	}

	// relevance part
	/** is given concept relevant to given Labeller's state */
	public boolean isRelevant(final TLabeller lab) {
		return lab.isLabelled(rel);
	}

	/** make given concept relevant to given Labeller's state */
	public void setRelevant(final TLabeller lab) {
		rel = lab.getLabel();
	}

	/** make given concept irrelevant to given Labeller's state */
	public void dropRelevant(final TLabeller lab) {
		rel = 0;//lab.clear(rel); 
	}
}
