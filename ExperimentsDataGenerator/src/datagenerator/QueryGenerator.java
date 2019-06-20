package datagenerator;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import chase.graph.Edge;
import chase.graph.Graph;
import chase.graph.Vertex;
import datalog.DatalogParser;
import datalog.DatalogScanner;
import gqr.ConstraintParser;
import gqr.DatalogQuery;
import gqr.GQR;
import gqr.GQRNode;
import gqr.Index;
import gqr.JoinDescription;
import gqr.JoinInView;
import gqr.Pair;
import gqr.PredicateJoin;
import gqr.SourcePredicateJoin;

public class QueryGenerator {
	
	ArrayList<gqr.Predicate> targetSchema;
	
	/**
	 * Parses a dependenciesFile and generates queries that contain meaningful joins 
	 * @param dependenciesFile
	 * @param depNumber
	 * @throws IOException
	 */
	public QueryGenerator(Graph g, File STdependencies, File TdependenciesFile) throws IOException{
		
		

		ConstraintParser cparser = new ConstraintParser(TdependenciesFile, 100);
		cparser.parseConstraints();
		List<DatalogQuery> targetConstraints = cparser.getConstraints();
		
		cparser = new ConstraintParser(STdependencies, 100);
		cparser.parseConstraints();
		List<DatalogQuery> stConstraints = cparser.getConstraints();
		

		Map<String, gqr.Predicate> targetSchemaMap =  new HashMap<String, gqr.Predicate>();	
//		
		//for all target constraints
		for(DatalogQuery targetCon : targetConstraints)
		{

			// Create constraint PJs
			// The first list of the pair contains the antecedents of the constraint
			// The first list of the pair contains the consequences of the constraint
			Pair<List<SourcePredicateJoin>,List<SourcePredicateJoin>> constraintPJs = Index.createTGDConstraintPJs(targetCon);
			for(SourcePredicateJoin stPJ: constraintPJs.getB())
			{
				targetSchemaMap.put(stPJ.getPredicate().toString(),stPJ.getPredicate());
			}
			addConstraintInWAGraph(g,constraintPJs);
		}
//		
		
		//for all s-t constraints
		for(DatalogQuery stCon : stConstraints)
		{

			// Create constraint PJs
			// The first list of the pair contains the antecedents of the constraint
			// The first list of the pair contains the consequences of the constraint
			Pair<List<SourcePredicateJoin>,List<SourcePredicateJoin>> constraintPJs = Index.createTGDConstraintPJs(stCon);
			for(SourcePredicateJoin stPJ: constraintPJs.getB())
			{
				targetSchemaMap.put(stPJ.getPredicate().toString(),stPJ.getPredicate());
			}
			addConstraintInWAGraph(g,constraintPJs);
		}
		
		targetSchema = new ArrayList<gqr.Predicate>(targetSchemaMap.values());
	}
	
	public QueryGenerator(Graph g, ArrayList<String> STdependencies,  ArrayList<String> TdependenciesFile) throws IOException{
		
		

		ConstraintParser cparser = new ConstraintParser(TdependenciesFile, 100);
		cparser.parseConstraints();
		List<DatalogQuery> targetConstraints = cparser.getConstraints();
		
		cparser = new ConstraintParser(STdependencies, 100);
		cparser.parseConstraints();
		List<DatalogQuery> stConstraints = cparser.getConstraints();
		

		Map<String, gqr.Predicate> targetSchemaMap =  new HashMap<String, gqr.Predicate>();	
//		
		//for all target constraints
		for(DatalogQuery targetCon : targetConstraints)
		{

			// Create constraint PJs
			// The first list of the pair contains the antecedents of the constraint
			// The first list of the pair contains the consequences of the constraint
			Pair<List<SourcePredicateJoin>,List<SourcePredicateJoin>> constraintPJs = Index.createTGDConstraintPJs(targetCon);
			for(SourcePredicateJoin stPJ: constraintPJs.getB())
			{
				targetSchemaMap.put(stPJ.getPredicate().toString(),stPJ.getPredicate());
			}
			addConstraintInWAGraph(g,constraintPJs);
		}
//		
		
		//for all s-t constraints
		for(DatalogQuery stCon : stConstraints)
		{

			// Create constraint PJs
			// The first list of the pair contains the antecedents of the constraint
			// The first list of the pair contains the consequences of the constraint
			Pair<List<SourcePredicateJoin>,List<SourcePredicateJoin>> constraintPJs = Index.createTGDConstraintPJs(stCon);
			for(SourcePredicateJoin stPJ: constraintPJs.getB())
			{
				targetSchemaMap.put(stPJ.getPredicate().toString(),stPJ.getPredicate());
			}
			addConstraintInWAGraph(g,constraintPJs);
		}
		
		targetSchema = new ArrayList<gqr.Predicate>(targetSchemaMap.values());
	}
	
	public ArrayList<gqr.Predicate> getTargetSchema()
	{
		return targetSchema;
	}
	
	public String createQuery(ArrayList<gqr.Predicate> targetSchema, int querysize, int sizeOfVariableSpace, int numberOfDistinguishedVars, int maxNumberOfDuplicatePredicates) {
		//create  first query
		
		VariablePredicateLengthRandomStatementGenerator a1 = new VariablePredicateLengthRandomStatementGenerator(targetSchema,sizeOfVariableSpace, 1, targetSchema.size()+1, querysize, numberOfDistinguishedVars, maxNumberOfDuplicatePredicates);
		Statement foo1 = null;
		while(foo1 == null)
		{
			try{
				foo1 = a1.getRandomStatement(false);
			}catch(InputMismatchException e)
			{
				foo1 = null;
				System.out.println("Cannot use binary relation -- retracting constraint");
			}
		}
		
		foo1.getHead().setFunctionHead("q"); //add prefix q in the heads

		Vector vec2 = new Vector();
		for(Object ob:foo1.getHead().variables)
		{
			String str = ((String)ob);
			vec2.add("X"+str);// add prefix X in all variables
		}
		foo1.getHead().variables = vec2;

		for(Predicate pred:foo1.body)
		{
			Vector vec3 = new Vector();
			if(pred == null)
				continue;
			String predname = pred.getFunctionHead();
			predname = predname.substring(0, predname.lastIndexOf("00"+querysize));
			pred.setFunctionHead(targetSchema.get(new Integer(predname)-1).name); //put prefix m in front of a predicate's name
			for(Object ob:pred.variables)
			{
				String str = ((String)ob);
				vec3.add("X"+str);
			}
			pred.variables = vec3;
		}

		return foo1.printString().toString();
	}

	public boolean isGoodQuery(Graph g, String q)
	{
		//Is the query good? If not keep on creating
		DatalogQuery query = null;
		DatalogScanner scanner = new DatalogScanner(new StringReader(q));
		DatalogParser parser = new DatalogParser(scanner);
		try{
			query = parser.query();	
		} catch (RecognitionException re) {
			throw new RuntimeException(re);
		} catch (TokenStreamException e) {
			throw new RuntimeException(e);
		}

		List<SourcePredicateJoin> l = GQR.createRulePJsList(query);
		Set<LinkedHashSet<Vertex>> joinsSets = new LinkedHashSet<LinkedHashSet<Vertex>>();
		
		//check if free vars fall on nulls
		for(SourcePredicateJoin spj:l)
		{	
			for(Entry<Integer,GQRNode> n:spj.getGqrNodes().entrySet())
			{
				if(n.getValue().isExistential())
					continue;
				
				Vertex v=new Vertex(new JoinDescription(spj.getPredicate(), n.getKey()));
				for(Vertex v_orig: g.getVertices())
				{
					if(v.equals(v_orig))
					{
						v= v_orig;
					    break;
					}
				}
						
				for(Edge e:v.getIncomingEdges())
				{
					if(e.getCost() == 1)
						return false;
				}
			}
		}

		//Set<Vertex> joins = new LinkedHashSet<Vertex>();
		for(SourcePredicateJoin spj:l)
		{	
			for(Entry<Integer,GQRNode> n:spj.getGqrNodes().entrySet())
			{
				Set<Vertex> joins =  setInJoinSetsThatContainsVertex(joinsSets, new Vertex(new JoinDescription(spj.getPredicate(), n.getKey())));

				if(!joins.isEmpty())
				{
					continue;//joins have been created already
				}

				//else it's the first time we're processing this set of joins
				joins.add(new Vertex(new JoinDescription(spj.getPredicate(), n.getKey())));
				for(JoinDescription jd:n.getValue().getInfobox().getJoinInViews().iterator().next().getJoinDescriptions())
					joins.add(new Vertex(jd));
			}
		}

		for(LinkedHashSet<Vertex> joins: joinsSets)
		{
			if(joins.size() <=1)
				continue;
			
			Random r = new Random();
			if(r.nextInt(10) ==5)
				continue;
			
			if(!haveSameWAGraphAntecedent(g,joins) || !areExistentialsWithCommonSpecialEdgePredesscor(g,joins))
				return false;
		}
		return true;
	}
		
	private boolean haveSameWAGraphAntecedent(Graph g, LinkedHashSet<Vertex> joins) {
		
		LinkedHashSet<Vertex> set = new LinkedHashSet<Vertex>();
		boolean first = true;
		
		for(Vertex v:joins)
		{
			for(Vertex v_orig: g.getVertices())
			{
				if(v.equals(v_orig))
				{
					v= v_orig;
				    break;
				}
			}
			
			if(!g.contains(v))
				throw new RuntimeException("Node not contained in graph");
			
			if(first)
			{
				first = false;
				set.addAll(g.getSimpleAncestors(v));
			}
			else
				set.retainAll(g.getSimpleAncestors(v));
		}
		return !set.isEmpty();//if the set is empty, the joins have no common ancestor
	}

	private boolean areExistentialsWithCommonSpecialEdgePredesscor(Graph g, LinkedHashSet<Vertex> joins) {
		
		LinkedHashSet<Vertex> set = new LinkedHashSet<Vertex>();
		boolean first = true;
		for(Vertex v:joins)
		{				
			for(Vertex v_orig: g.getVertices())
			{
				if(v.equals(v_orig))
				{
					v= v_orig;
				    break;
				}
			}
			
			if(first)
			{
				ArrayList<Vertex> fset = g.getExistentialDirectAncestors(v); 
				if(fset.isEmpty())
					continue;
				first = false;
				set.addAll(fset);
			}
			else
				set.retainAll(g.getExistentialDirectAncestors(v));
		}
		return first?true:!set.isEmpty();
	}

	private Set<Vertex> setInJoinSetsThatContainsVertex(Set<LinkedHashSet<Vertex>> joinsSets, Vertex vertex) {
		
		for(LinkedHashSet<Vertex> joins: joinsSets)
		{
			if(joins.contains(vertex))
				return joins;
		}
		
		LinkedHashSet<Vertex> joins = new LinkedHashSet<Vertex>();
		joinsSets.add(joins);
		return joins;
	}

	static void addConstraintInWAGraph(Graph g, Pair<List<SourcePredicateJoin>,List<SourcePredicateJoin>> constraintPJs) {


		List<SourcePredicateJoin> antecedents  = constraintPJs.getA();
		setOriginallyExistentialVariables(antecedents,constraintPJs.getB());


		HashSet<Vertex> commonVariablesBetwenAntecedentAndConsequent = new HashSet<Vertex>();

		for(SourcePredicateJoin antecedent: antecedents)
		{
			for(Entry<Integer,GQRNode> nodeEntry: antecedent.getGqrNodes().entrySet())
			{
				int edgeNo = nodeEntry.getKey();
				GQRNode gqrNode = nodeEntry.getValue();

				JoinDescription node = new JoinDescription(antecedent.getPredicate(),edgeNo);
				Vertex from = new Vertex(node);
				from = g.addVertex(from);

				
				//for all joins of the from node
				for(JoinDescription join: gqrNode.getInfobox().getJoinInViews().iterator().next().getJoinDescriptions())
				{
					//create edges from "from" node to all the joined positions in consequent
					for(PredicateJoin conPJ:constraintPJs.getB())
						if(join.getPredicate().equals(conPJ.getPredicate()))
						{
							Vertex to = new Vertex(new JoinDescription(join.getPredicate(), join.getEdgeNo()));
							to = g.addVertex(to);

							g.addEdge(from,to,0); //zero means regular directed edge 

							commonVariablesBetwenAntecedentAndConsequent.add(from);
						}
				}
			}
		}

		for(SourcePredicateJoin consequent: constraintPJs.getB())
		{
			for(Entry<Integer,GQRNode> nodeEntry: consequent.getGqrNodes().entrySet())
			{
				int edgeNo = nodeEntry.getKey();
				GQRNode gqrNode = nodeEntry.getValue();

				if(gqrNode.isOriginallyExistentialInConstraint())
				{
					Vertex to = new Vertex(new JoinDescription(consequent.getPredicate(), edgeNo));
					to = g.addVertex(to);

					for(Vertex from: commonVariablesBetwenAntecedentAndConsequent)
						g.addEdge(from,to,1);//1 means "starred" directed edge
				}
			}
		}
	}
	
	private static void setOriginallyExistentialVariables(List<SourcePredicateJoin> antecedents, List<SourcePredicateJoin> consequents) {

		for(PredicateJoin pj: consequents)
		{
			Map<Integer, GQRNode> map = pj.getGqrNodes();

			for(int i=1; i<=map.size(); i++)
			{
				GQRNode pjsNode = map.get(new Integer(i));
				List<JoinDescription> joinsWithAntecedent = new ArrayList<JoinDescription>();
				List<JoinDescription> joinsWithConsequents = new ArrayList<JoinDescription>();

				assert(pjsNode.getInfobox().getJoinInViews().size() == 1);

				JoinInView jv = pjsNode.getInfobox().getJoinInViews().iterator().next();

				for(JoinDescription jd : jv.getJoinDescriptions())
				{
					boolean joinsWithAnt = false;
					for(PredicateJoin antPJ:antecedents)
						if(jd.getPredicate().equals(antPJ.getPredicate()))
						{
							joinsWithAnt = true;
							break;
						}

					if(joinsWithAnt)
					{
						joinsWithAntecedent.add(jd);
						pjsNode.setOriginallyExistentialInConstraint(false);//we're setting this multiple times but it's ok
					}
					else
						joinsWithConsequents.add(jd);
				}

				pjsNode.setJoinsWithAntecedent(joinsWithAntecedent);		
				pjsNode.setJoinsWithConsequents(joinsWithConsequents);

			}
		}
	}
}
