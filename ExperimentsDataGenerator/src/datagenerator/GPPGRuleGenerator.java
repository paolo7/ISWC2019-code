package datagenerator;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Vector;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import chase.graph.Edge;
import chase.graph.Graph;
import chase.graph.Vertex;
import datalog.DatalogParser;
import datalog.DatalogScanner;
import gqr.DatalogQuery;
import gqr.GQRNode;
import gqr.Index;
import gqr.JoinDescription;
import gqr.JoinInView;
import gqr.Pair;
import gqr.PredicateJoin;
import gqr.SourcePredicateJoin;

public class GPPGRuleGenerator extends RandomStatementGenerator{ 
	//this class is going to have chained queries
	//a chained query is one where all of the atoms are linked
	//by a chain of variables, and they share no other variables.
	//The differerent parameters that can be set are:
	//Function starting: THIS IS INPUT VARIABLE 'start'
	//the number at which functions will start
	//Function (George: she means function=predicate)stopping:
	//the number at which functions will stop THIS IS INPUT VARIABLE 'stop'
	//the number of each function that you're gounig to get is roughly THIS IS INPUT VARIABLE 'num_fun'
	//1/(function stopping - function starting)
	//Function length: length of each function; note, a function must have 
	//at least 2 variables, because otherwise, it can't chain.
	//currently, all lengths will be the same
	//Number of distinguished variables (George: number of returning variables)THIS IS INPUT VARIABLE 'dist'
	//The number of variables that are distinguished.  Since
	//this is a chain query, as long as one variable is distinguished, 
	//it makes sense.
	//at the moment, the first variable in the function is always the 
	//chained one


	int NumberFunctions;
	int FunctionStart;
	int FunctionStop;
	int FunctionLength;
	int _max_number_duplicates;
	int _number_distinguished;
	RandomVariableGenerator FunctionGenerator;
	//String FunctionOffset;
	
	RandomVariableGenerator getRandomVariableGenerator()
	{
		return FunctionGenerator;
	}


	public GPPGRuleGenerator( int start,int stop, int num_fun,int length,int dist, int num_dup){
		_max_number_duplicates = 1;
		//need to figure out how to do assertions
		//assert start <  stop 
		//assert num_fun > 0
		//assert len
		//note, I assume that it means that if NumberDistinguished = -1, then
		//it wants them all.
		//actually, i assume in this case that we have three choices,
		//1,2, all, but that's beside the point
		NumberFunctions = num_fun;
		FunctionStart = start;
		FunctionStop = stop;
		if (FunctionStart > FunctionStop){
			//then we've screwed up; return an error message and exit
			System.out.println("function stop greater than the function start in ChainRandomStatementGenerator; exiting");
			System.exit(1);
		}
		if (NumberFunctions > _max_number_duplicates * (FunctionStop - FunctionStart)){
			NumberFunctions = _max_number_duplicates * (FunctionStop - FunctionStart);
			//System.out.println("You have requested more predicates than available in ChainRandomStatementGenerator; exiting");
			//throw new RuntimeException("You have requested more predicates than available in ChainRandomStatementGenerator; exiting");
			//System.exit(1);
		}
		FunctionLength = length;
		_number_distinguished = dist;
		_max_number_duplicates = num_dup;
		FunctionGenerator = new RegularRandomVariableGenerator(start,stop);
		//Random rand = new Random();
//		if (FunctionLength < 10){
//			FunctionOffset = "00" + FunctionLength;
//		}
//		else if (FunctionLength < 100){
//			FunctionOffset = "0" + FunctionLength;
//		}
	}//end ChainRandomStatementGenerator(int,int)

	private String getFunctionHead(){

		String retval = FunctionGenerator.getRandomVariable();// + FunctionOffset;
		return retval;
	}




	public Statement getRandomStatement(boolean use_all){
		//this function returns a random statement based on the
		//values for the mean and deviation of the 
		//size of the query, and the mean and deviation of the 
		//num of variables and the number of function heads.
		//note; need to think about this more carefully for 
		//variables and function heads.
		Statement retval = new Statement();
		int i,j;
		int extra_variable = FunctionLength+1;
		//int length;
		//int fcnlength;
		//String temp;
		Predicate apred;
		String a_function_head;
		for (i = 0; i < NumberFunctions; i++){
			apred = new Predicate();
			a_function_head = getFunctionHead();
			while (retval.numPredicateOccurances(a_function_head) == _max_number_duplicates){
				//if this is the case, then we need to pick a new one
				a_function_head = getFunctionHead();
				//note, we should be safe entering this loop, because we have carefully 
				//checked the number of predicates available earlier
			}
			apred.setFunctionHead(a_function_head);
			apred.addVariable((new Integer(i)).toString());
			apred.addVariable((new Integer(i+1)).toString());
			for (j = 2; j < FunctionLength; j++){
				apred.addVariable((new Integer(extra_variable)).toString());
				extra_variable++;
			}
			retval.addSubgoal(apred);  

		}
		generateHead(retval); 
		return retval;

	}

	public void generateHead(Statement a_state){
		//this function generates the head of the statement.  Note that probably only the
		//variables used will really matter, because we'll change the head name anyway.
		//thus i will call them all "q", which, come to think of it, is probably 
		//why i didn't get any errors in this before.  oy, i feel dumb.
		Predicate head = new Predicate();
		head.setFunctionHead("q");
		if (_number_distinguished == 1){
			head.addVariable(a_state.subgoalI(0).variableI(0));
		}
		else if  (_number_distinguished == 2){
			head.addVariable(a_state.subgoalI(0).variableI(0));
			head.addVariable(a_state.subgoalI(a_state.size()-1).variableI(1));
		}
		else if (_number_distinguished < 0){
			System.out.println("can't have a statement with no distinguished variables in ChainRandomStatementGenerator.generateHead");
			System.out.println("system will exit");
			System.exit(1);
		}
		else if (_number_distinguished == 0){
			Vector unique_vars = a_state.findUniqueVariables();
			int i;
			_number_distinguished = unique_vars.size();
			for (i = 0; i < _number_distinguished; i++){
				head.addVariable((String)unique_vars.elementAt(i));
			}
		}

		else{
			Vector unique_vars = a_state.findUniqueVariables();


			int i;
			int rand;
			float percent_distinguished = (float) _number_distinguished /
					(float) unique_vars.size();
			int num_dist_needed = _number_distinguished;

			boolean all_done = false;
			boolean add_to_end = false;
			Random random = new Random();

			for (i = 0; all_done == false && 
					add_to_end == false &&
					i < unique_vars.size();i++){
				if (random.nextFloat() < percent_distinguished){
					//then we need to add it
					head.addVariable((String)unique_vars.elementAt(i));
					num_dist_needed--;
					if (num_dist_needed == 0){
						//then we can stop adding- break out of loop
						all_done = true;
					}//end if
				}//end if
				else if((unique_vars.size() - i   - 1) == num_dist_needed){
					//we need to check and make sure that we don't
					//need to just add the rest....
					add_to_end = true;
				}//end else if

			}//end for
			if (add_to_end){
				for (; i < unique_vars.size(); i++){
					head.addVariable((String)unique_vars.elementAt(i));
				}
			}
		}
		a_state.setHead(head);

	}//end of generateHead

	//	public static void main(String []args) throws CloneNotSupportedException{
	//	
	//			ConstraintParser cparser = new ConstraintParser(new File(System.getProperty("user.dir")+"/examples"+"/constraint_examples"+"/constraints_test_naive_chase.txt"),10);
	//			cparser.parseConstraints();
	//			List<DatalogQuery> constraints = cparser.getConstraints();
	//
	//			Graph g = new Graph();
	//			for(DatalogQuery constraint: constraints)
	//			{
	//				System.out.println("Adding Constraint:" + constraint);
	//				if(graphRemainsWeaklyAcyclic(g, constraint.toString()))
	//					System.out.println("Graph remains WA");
	//				else
	//					System.out.println("Graph NOT ACYCLIC now");
	//			}
	//			System.out.println("\n "+g);
	//	}
	
	public static String getFileLocation(String dir,int datasetNum, int numberOfconstraints, int sizeOfPredicateSpace,int constraintSize, int arity, int maxNoOfRepeteadRelsPerConstraint, int constantPool, double constantCreationRate) {
		return dir+"/run_"+datasetNum+"_schemaview_RuletSize"+constraintSize+"_CS"+constantPool+"_rate"+constantCreationRate+"_predSpace"+sizeOfPredicateSpace+".txt";
	}
	public static Random r = new Random();
	
	public static void generateChaseConstraintData(String dir,int datasetNum, int numberOfconstraints, int sizeOfPredicateSpace,int constraintSize, int arity, int maxNoOfRepeteadRelsPerConstraint, int constantPool, double constantCreationRate) throws CloneNotSupportedException{
		

			// GENERATE DATA FILE IF NECESSARY
			//System.out.println(new File(dir).mkdir());
			Graph g = new Graph();

			//CREATE CONSTRAINTS

			//File file2 = new File(dir+"/run_"+i+"/constraints_for_q_"+i+".txt");
			File file3 = new File(getFileLocation(dir, datasetNum, numberOfconstraints, sizeOfPredicateSpace, constraintSize, arity, maxNoOfRepeteadRelsPerConstraint, constantPool, constantCreationRate));
			
			// initialise the random seed as the number of the dataset
			

			// if the file exists do nothing
			File f = new File(file3.getAbsolutePath());
			if(!(f.exists() && !f.isDirectory())) { 
				
				
				
				
				//System.out.println("Creating new dataset");
				
				try {
					file3.createNewFile();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				//PrintWriter constraintFile;
				PrintWriter constraintFileBlt;
				try {
					//constraintFile = new PrintWriter(file2);
					constraintFileBlt = new PrintWriter(file3);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
				
				Vector<String> constraints = new Vector<String>();
				
				for(int j=0; j<numberOfconstraints; j++)
				{
					
					Map<String,String> constantBinding = new HashMap<String,String>();
					for(int i = 0; i < arity*constraintSize; i++) {
						if(r.nextDouble() < constantCreationRate) {
							if(r.nextBoolean() && i == constraintSize) {
								constantBinding.put(i+"", "\"LIT"+r.nextInt(constantPool)+"\"");								
							} else {
								constantBinding.put(i+"", "e:C"+r.nextInt(constantPool));								
							}
						}
					}
					
					GPPGRuleGenerator ruleGen = null; 
					
//									if(j%10 == 1)
//				 						a = new GPPGRuleGenerator(1, (int) (sizeOfPredicateSpace/5), constraintSize, arity, 1, maxNoOfRepeteadRelsPerConstraint);
//				 					else
					
					ruleGen = new GPPGRuleGenerator(1, sizeOfPredicateSpace, constraintSize, arity, 2, maxNoOfRepeteadRelsPerConstraint);
					
					Statement constraintStatement = ruleGen.getRandomStatement(false); //internal predicate name generator which has been intilialised to return predicates from the space of predicates of size sizeOfPredicateSpace
					
					//head is also going to have a predicate name chosen from that space
					constraintStatement.getHead().setFunctionHead("m"+ruleGen.getRandomVariableGenerator().getRandomVariable());
//
//				change the variables of the head to add whatever prefix I like
//				Vector vec = new Vector();
//				for(Object ob:constraintStatement.getHead().variables)
//				{
//					String str = ((String)ob);
//					Random r = new Random();
//					vec.add("?v"+(r.nextInt(constraintSize-1)+1));//don't care
//				}
//				constraintStatement.getHead().variables = vec;
					
					for(Predicate pred:constraintStatement.body)
					{
//					Vector vec1 = new Vector();
						if(pred == null)
							continue;
						pred.setFunctionHead("m"+pred.getFunctionHead());
//					for(Object ob:pred.variables)
//					{
//						String str = ((String)ob);
//						vec1.add("?v"+str);
//					}
//					pred.variables = vec1;
					}
					
					// ADD CONSTANTS
					
					addConstraints(constraintStatement.body, constantBinding);
					addConstraints(new Predicate[]{constraintStatement.head}, constantBinding);
					
					String constraint = constraintStatement.printString().toString();
					constraintFileBlt.append(constraint).append(System.getProperty("line.separator"));
				}
				//constraintFile.close();
				constraintFileBlt.close();
			}
	}
	

	private static void addConstraints(Predicate[] predicates, Map<String,String> constantBinding){
		for(Predicate p: predicates) if(p != null) {
			Vector newVec = new Vector();
			for(Object o : p.variables) {
				String str = ((String)o);
				if(constantBinding.containsKey(str)) newVec.add(constantBinding.get(str));
				else newVec.add(str);
			}
			p.variables = newVec;
		}
	}


	
	
	private static CharSequence formatViewForBullit(String constraint) {
		
		String out = constraint.replace(":-"," -> ")+".";
		out= out.replace("X", "$X");
		out=out.replace("),", ") & ");
		System.out.println(out);
		return out;
	}

	private static CharSequence formatConstraintForBullit(String constraint) {
		
		String name = constraint.substring(0,constraint.indexOf("("));
		int sizeOfBody = new Integer(constraint.substring(constraint.indexOf("DC")+2,constraint.indexOf(")")));
		//System.out.println("name ="+name);
		//System.out.println("Size of body ="+sizeOfBody);
		int indLastAtom =  indexOfLastBodyAtom(constraint,sizeOfBody);
		//System.out.println("Index of comma after last bofy atom ="+;
		
		String out = constraint.substring(constraint.indexOf(":-")+2,indLastAtom);
		out = out+" -> "+constraint.substring(indLastAtom+1)+".";
		out= out.replace("X", "$X");
		out=out.replace("),", ") & ");
		return out;
	}

	private static int indexOfLastBodyAtom(String text, int sizeOfBody)
	{
	    for (int i = 0; i < text.length()-1; i++)
	    {
	        if (text.charAt(i) == ')' && text.charAt(i+1) == ',')
	        {
	            sizeOfBody--;
	            if (sizeOfBody == 0)
	            {
	                return i+1;
	            }
	        }
	    }
	    return -1;
	}
	static void addConstraintInWAGraph(Graph g, String constraint) {


		DatalogQuery con = null;
		DatalogScanner scanner = new DatalogScanner(new StringReader(constraint));
		DatalogParser parser = new DatalogParser(scanner);
		try{
			con = parser.query();	
		} catch (RecognitionException re) {
			throw new RuntimeException(re);
		} catch (TokenStreamException e) {
			throw new RuntimeException(e);
		}
		
		Pair<List<SourcePredicateJoin>,List<SourcePredicateJoin>> constraintPJs = Index.createTGDConstraintPJs(con);
		
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

	public static boolean graphRemainsWeaklyAcyclic(Graph g, String constraint) {

		addConstraintInWAGraph(g, constraint);

		for(Edge e:g.findCycles())
		{
			if(e.getCost() == 1)
				return false;
		}


		return true;
	}

}