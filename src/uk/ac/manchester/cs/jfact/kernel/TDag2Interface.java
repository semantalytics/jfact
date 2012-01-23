package uk.ac.manchester.cs.jfact.kernel;

import java.util.ArrayList;
import java.util.List;

import uk.ac.manchester.cs.jfact.helpers.DLVertex;
import uk.ac.manchester.cs.jfact.helpers.Helper;
import uk.ac.manchester.cs.jfact.helpers.UnreachableSituationException;
import uk.ac.manchester.cs.jfact.kernel.dl.ConceptTop;
import uk.ac.manchester.cs.jfact.kernel.dl.DataTop;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.ConceptExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.DataExpression;
import uk.ac.manchester.cs.jfact.kernel.dl.interfaces.Expression;

/// class to translate DAG entities into the TDL* expressions
class TDag2Interface {
	/// DAG to be translated
	private final DLDag Dag;
	/// expression manager
	private final ExpressionManager Manager;
	/// vector of cached expressions
	private final List<ConceptExpression> TransConcept = new ArrayList<ConceptExpression>();
	private final List<DataExpression> TransData = new ArrayList<DataExpression>();

	/// build concept expression by a vertex V
	ConceptExpression buildCExpr(DLVertex v) {
		switch (v.getType()) {
			case dtTop:
				return Manager.top();
			case dtNConcept:
			case dtPConcept:
				return Manager.concept(v.getConcept().getName());
			case dtPSingleton:
			case dtNSingleton:
				return Manager.oneOf(Manager.individual(v.getConcept().getName()));
			case dtAnd:
			case dtCollection: {
				List<Expression> list = new ArrayList<Expression>();
				for (int p : v.begin()) {
					list.add(getCExpr(p));
				}
				return Manager.and(list);
			}
			case dtForall:
				if (v.getRole().isDataRole()) {
					return Manager.forall(Manager.dataRole(v.getRole().getName()),
							getDExpr(v.getConceptIndex()));
				} else {
					return Manager.forall(Manager.objectRole(v.getRole().getName()),
							getCExpr(v.getConceptIndex()));
				}
			case dtLE:
				if (v.getRole().isDataRole()) {
					return Manager.maxCardinality(v.getNumberLE(),
							Manager.dataRole(v.getRole().getName()),
							getDExpr(v.getConceptIndex()));
				} else {
					return Manager.maxCardinality(v.getNumberLE(),
							Manager.objectRole(v.getRole().getName()),
							getCExpr(v.getConceptIndex()));
				}
			case dtIrr:
				return Manager.not(Manager.selfReference(Manager.objectRole(v.getRole()
						.getName())));
			case dtProj:
			case dtNN:
			case dtChoose:
			case dtSplitConcept: // these are artificial constructions and shouldn't be visible
				return Manager.top();
			default:
				throw new UnreachableSituationException();
		}
	}

	/// build data expression by a vertex V
	DataExpression buildDExpr(DLVertex v) {
		switch (v.getType()) {
			case dtTop:
				return Manager.dataTop();
			case dtDataType:
			case dtDataValue:
			case dtDataExpr: // TODO: no data stuff yet
				return Manager.dataTop();
			case dtAnd:
			case dtCollection: {
				List<Expression> list = new ArrayList<Expression>();
				for (int p : v.begin()) {
					list.add(getDExpr(p));
				}
				return Manager.dataAnd(list);
			}
			default:
				throw new UnreachableSituationException();
		}
	}

	/// build expression by a vertex V given the DATA flag
	Expression buildExpr(DLVertex v, boolean data) {
		if (data) {
			return buildDExpr(v);
		} else {
			return buildCExpr(v);
		}
	}

	/// init c'tor
	TDag2Interface(DLDag dag, ExpressionManager manager) {
		Dag = dag;
		Manager = manager;
		Helper.resize(TransConcept, dag.size());
		Helper.resize(TransData, dag.size());
	}

	/// make sure that size of expression cache is the same as the size of a DAG
	void ensureDagSize() {
		int ds = Dag.size(), ts = TransConcept.size();
		if (ds == ts) {
			return;
		}
		Helper.resize(TransConcept, ds);
		Helper.resize(TransData, ds);
		if (ds > ts) {
			while (ts != ds) {
				TransConcept.set(ts++, null);
				TransData.set(ts++, null);
			}
		}
	}

	/// get concept expression corresponding index of vertex
	ConceptExpression getCExpr(int p) {
		if (p < 0) {
			return Manager.not(getCExpr(-p));
		}
		if (TransConcept.get(p) == null) {
			TransConcept.set(p, buildCExpr(Dag.get(p)));
		}
		return TransConcept.get(p);
	}

	/// get data expression corresponding index of vertex
	DataExpression getDExpr(int p) {
		if (p < 0) {
			return Manager.dataNot(getDExpr(-p));
		}
		DataExpression expression = TransData.get(p);
		if (expression == null) {
			expression = buildDExpr(Dag.get(p));
			TransData.set(p, expression);
		}
		return expression;
	}
}
