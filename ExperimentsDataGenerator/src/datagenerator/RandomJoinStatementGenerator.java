package datagenerator;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Vector;

import chase.graph.Graph;

public class RandomJoinStatementGenerator extends RandomStatementGenerator{ 
	//this class is going to have random queries
	//a random query is one where all of the atoms are linked
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
	String FunctionOffset;
	private RandomVariableGenerator _variable_generator;

	public RandomJoinStatementGenerator( int start,int stop, int num_fun,int length,int dist, int num_dup){
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
			System.out.println("function stop greater than the function start in RandomJoinStatementGenerator; exiting");
			System.exit(1);
		}
		if (NumberFunctions > _max_number_duplicates * (FunctionStop - FunctionStart)){
			System.out.println("You have requested more predicates than available in RandomJoinStatementGenerator; exiting");
			System.exit(1);
		}
		FunctionLength = length;
		_number_distinguished = dist;
		_max_number_duplicates = num_dup;
		FunctionGenerator = new RegularRandomVariableGenerator(start,stop);
		//Random rand = new Random();
		if (FunctionLength < 10){
			FunctionOffset = "00" + FunctionLength;
		}
		else if (FunctionLength < 100){
			FunctionOffset = "0" + FunctionLength;
		}

		int num_vars = (length * num_fun) / 2;

		_variable_generator = new RegularRandomVariableGenerator(0,num_vars);
	}//end RandomJoinStatementGenerator(int,int)

	private String getFunctionHead(){

		String retval = FunctionGenerator.getRandomVariable() + FunctionOffset;
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

			for (j = 0; j < FunctionLength; j++){
				apred.addVariable(_variable_generator.getRandomVariable());

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
			System.out.println("can't have a statement with no distinguished variables in RandomJoinStatementGenerator.generateHead");
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

	public static void main(String [] args) throws CloneNotSupportedException{

		//		 RandomJoinStatementGenerator bob = new RandomJoinStatementGenerator(1, 20, 8, 4, 3, 5);
		////		  DuplicateCountRandomStatementGenerator bob = new DuplicateCountRandomStatementGenerator(1,15,3,5,4,4,20);
		//
		//		  Statement bobbob = bob.getRandomStatement(false);
		//		  
		//		  bobbob.print();

		//				String dir = System.getProperty("user.home")+"/users_link/gkonstant/Desktop/" +
		//						"experiments/random_10qX10000v+qAsView_20PredSpaceTill20v_" +
		//						"500predSpaceAfter20_8PredBody_4var_10Dtill80v_3Dtill10000v_5repMax/data";

		//new File(dir).mkdir();
		String dir = System.getProperty("user.dir")+"/constraint_data/random/";
		System.out.println("dir");

		//FOR 100qX140 views with each viewset in different file : 1) go to end of file 2) uncomment code 3) look at remaining comments
		for(int i=0; i<30; i++)
		{

			
			new File(dir+"/run_"+i).mkdir();
			File file1 = new File(dir+"/run_"+i+"/query_"+i+".txt");

			try {
				file1.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			PrintWriter outfile1;
			try {
				outfile1 = new PrintWriter(file1);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}

			RandomJoinStatementGenerator a1 = new RandomJoinStatementGenerator(1, 20, 8, 4, 6, 5);
			Statement foo1 = a1.getRandomStatement(false);
			foo1.getHead().setFunctionHead("q"+i); //add prefix q in the heads

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
				pred.setFunctionHead("m"+pred.getFunctionHead()); //put prefix m in front of a predicate's name
				for(Object ob:pred.variables)
				{
					String str = ((String)ob);
					vec3.add("X"+str);
				}
				pred.variables = vec3;
			}

			foo1.print();

			outfile1.append(foo1.printString()).append(System.getProperty("line.separator"));
			outfile1.close();
			System.out.println("Juist wrote file: "+outfile1);


			// CONSTRUCT VIEWS IN THE SAME FILE

			File file = new File(dir+"/run_"+i+"/views_for_q_"+i+".txt");
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			PrintWriter outfile;
			try {
				outfile = new PrintWriter(file);///COPY THE QUERYY IN THE VIEWSSSSSSSSSS
				foo1.getHead().setFunctionHead("vq");
				outfile.append(foo1.printString()).append(System.getProperty("line.separator"));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}

//			for(int j=0; j<81; j++)
//			{
//				RandomJoinStatementGenerator a = null;
//				if(j<21)
//					a = new RandomJoinStatementGenerator(1, 20, 8, 4, 10, 5);
//				else
//					a = new RandomJoinStatementGenerator(1, j, 8, 4, 10, 5);
//
//
//				Statement foo = a.getRandomStatement(false);
//				foo.getHead().setFunctionHead("v"+j);
//
//				Vector vec = new Vector();
//				for(Object ob:foo.getHead().variables)
//				{
//					String str = ((String)ob);
//					vec.add("X"+str);
//				}
//				foo.getHead().variables = vec;
//
//				for(Predicate pred:foo.body)
//				{
//					Vector vec1 = new Vector();
//					if(pred == null)
//						continue;
//					pred.setFunctionHead("m"+pred.getFunctionHead());
//					for(Object ob:pred.variables)
//					{
//						String str = ((String)ob);
//						vec1.add("X"+str);
//					}
//					pred.variables = vec1;
//				}
//
//
//				//foo.print();
//
//				outfile.append(foo.printString()).append(System.getProperty("line.separator"));
//			}

			for(int j=0; j<1000; j++)
			{
//				if(j%1000 == 0)
//					System.out.println(j);

				RandomJoinStatementGenerator a = new RandomJoinStatementGenerator(1, 500, 8, 4, 3, 5);
				Statement foo = a.getRandomStatement(false);
				foo.getHead().setFunctionHead("v"+j);

				Vector vec = new Vector();
				for(Object ob:foo.getHead().variables)
				{
					String str = ((String)ob);
					vec.add("X"+str);
				}
				foo.getHead().variables = vec;

				for(Predicate pred:foo.body)
				{
					Vector vec1 = new Vector();
					if(pred == null)
						continue;
					pred.setFunctionHead("m"+pred.getFunctionHead());
					for(Object ob:pred.variables)
					{
						String str = ((String)ob);
						vec1.add("X"+str);
					}
					pred.variables = vec1;
				}

				outfile.append(foo.printString()).append(System.getProperty("line.separator"));
			}
			outfile.close();
			System.out.println("Just wrote views file: "+outfile);

			Graph g = new Graph();

			//CREATE CONSTRAINTS

			File file2 = new File(dir+"/run_"+i+"/constraints_for_q_"+i+".txt");
			try {
				file2.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			PrintWriter constraintFile;
			try {
				constraintFile = new PrintWriter(file2);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}

			for(int j=0; j<1000; j++)
			{
				RandomJoinStatementGenerator a = null; //new WeaklyAcyclicLAVGenerator(1, 20, 4, 4, 1, 3);

				// 					if(j%10 == 0)
				// 						a = new WeaklyAcyclicLAVGenerator(1, 30, 4, 4, 1, 3);
				//					if(j%10 == 1)
				//						a = new StarRandomStatementGenerator(1, 60, 4, 4, 1, 3);
				//					else
				a = new RandomJoinStatementGenerator(1, 500/*(j>500)?500:((j<200)?200:j)*/, 5, 4, 1, 3);


				Statement constraintStatement = a.getRandomStatement(false);
				constraintStatement.getHead().setFunctionHead("c"+j);

				Vector vec = new Vector();
				for(Object ob:constraintStatement.getHead().variables)
				{
					String str = ((String)ob);
					vec.add("DC");//don't care
				}
				constraintStatement.getHead().variables = vec;

				for(Predicate pred:constraintStatement.body)
				{
					Vector vec1 = new Vector();
					if(pred == null)
						continue;
					pred.setFunctionHead("m"+pred.getFunctionHead());
					for(Object ob:pred.variables)
					{
						String str = ((String)ob);
						vec1.add("X"+str);
					}
					pred.variables = vec1;
				}

				String constraint = constraintStatement.printString().toString();

				//it seems that the chain generator is partial to generating the same first predicate in a rule. In the case of queries/views it doesn't matter. In the case of LAV constraints, the first predicate is the antecedent, so we'll mix things up a bit.

				int start = constraint.indexOf("m");
				int end = constraint.indexOf(",m");
				String firstPredicate = constraint.substring(start,end);
				constraint = constraint.substring(0,start)+constraint.substring(end+1)+","+firstPredicate;
				// 					System.out.println(constraint);

				if(WeaklyAcyclicLAVGenerator.graphRemainsWeaklyAcyclic((Graph)g.clone(), constraint))
				{
					WeaklyAcyclicLAVGenerator.addConstraintInGraph(g,constraint);
					constraintFile.append(constraint).append(System.getProperty("line.separator"));
				}
				else
				{
					System.out.println("Retracting constraint, Weak Acyclicity violated");
					j--;
				}
			}
			constraintFile.close();

			//END CREATE CONSTRAINTS---------------------------------------------

		}
	}    
}