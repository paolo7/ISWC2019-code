package datagenerator;


//import java.lang.*;
import java.util.Random;
import java.util.Vector;

public class FullRandomStatementGenerator extends RandomStatementGenerator{ 
	//this class is going to have star queries
	//a star query is one where all of the variables are separate, except for
	//one function where they are all linked in glorious abandon.
	//I'll refer to the one where they all link as the "center" for now.
	//one possible interesting feature is that it may be important to
	//have the predicate head of that variable set separately.
	//also, the number of predicates determines the size of the star predicate
	//The differerent parameters that can be set are:
	//Function starting:
	//the number at which functions will start
	//Function stopping:
	//the number at which functions will stop
	//the number of each function that you're gounig to get is roughly 
	//1/(function stopping - function starting)
	//Function length: length of each function;
	//currently, all lengths will be the same, except for the center one
	//Number of distinguished variables
	//There should probably also be some easy way to specify that we want
	//all of the joined variables to be head variables and another to 
	//have all of the non joined variables be head variables.
	
	int NumberFunctions;
	int FunctionStart;
	int FunctionStop;
	//int FunctionLength;
	int NumberDistinguished;
	RandomVariableGenerator FunctionGenerator;
	RandomVariableGenerator _variable_generator;
	String FunctionOffset;
	int _max_number_duplicates;
	String _center_offset;
	
	public FullRandomStatementGenerator( int start,int stop, int num_fun,int dist,int num_dup){
		//need to figure out how to do assertions
		//assert start <  stop 
		//assert num_fun > 0
		//assert len
		//_predicate_length = predicate_length;
		//for NumberDistinguished, I'm going to assume that -2 means use
		//all of the non joined ones, and -3 means use all of the joined ones
		NumberFunctions = num_fun;
		FunctionStart = start;
		FunctionStop = stop;
		_max_number_duplicates = num_dup;
		NumberDistinguished = dist;
		if (NumberDistinguished < -3){//then someone has done something silly
			System.out.println("In FullRandomStatementGenerator someone has attempted to pass in");
			System.out.println("a number less than -3 for the number of distinguished variables; program exiting");
			System.exit(1);
		}
		//_number_of_duplicates = num_dup;
		FunctionGenerator = new RegularRandomVariableGenerator(start,stop);
		_variable_generator = new RegularRandomVariableGenerator(0,NumberFunctions);
		//Random rand = new Random();
		if (NumberFunctions < 10){
			FunctionOffset = "00" + NumberFunctions;
		}
		else if (NumberFunctions < 100){
			FunctionOffset = "0" + NumberFunctions;
		}
		if (NumberFunctions -1 < 10){
			_center_offset = "00" + (NumberFunctions - 1);
		}
		else if (NumberFunctions -1 < 100){
			_center_offset = "0" + (NumberFunctions - 1);
		}
		if (FunctionStart > FunctionStop){
			//then we've screwed up; return an error message and exit
			System.out.println("function stop greater than the function start in FullRandomStatementGenerator; exiting");
			System.exit(1);
		}
		if (NumberFunctions > _max_number_duplicates * (FunctionStop - FunctionStart)){
			System.out.println("You have requested more predicates than available in FullRandomStatementGenerator; exiting");
			System.exit(1);
		}
	}//end StarRandomStatementGenerator(int,int)
	
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
		String [] variables;
		//int length;
		//int fcnlength;
		//String temp;
		Predicate apred;
		String a_function_head;
		for (i = 0; i < NumberFunctions; i++){
			//we want to do this for all of the non center predicates
			apred = new Predicate();
			a_function_head = getFunctionHead();
			while (retval.numPredicateOccurances(a_function_head) == _max_number_duplicates){
				//if this is the case, then we need to pick a new one
				a_function_head = getFunctionHead();
				//note, we should be safe entering this loop, because we have carefully 
				//checked the number of predicates available earlier
			}
			apred.setFunctionHead(a_function_head);
			//apred.addVariable((new Integer(i)).toString());
			//apred.addVariable((new Integer(i+1)).toString());
			variables = createPermutation();
			for (j =0; j < NumberFunctions; j++){
				apred.addVariable(variables[j]);
			}
			retval.addSubgoal(apred);  
			
		}
		//now we need to generate the head
		generateHead(retval);
		return retval;

	}
	
	public String[] createPermutation(){
		String[] retval = new String[NumberFunctions];
		int i,j;
		String a_val;
		boolean found;
		for (i = 0; i < NumberFunctions; i++){
			a_val = _variable_generator.getRandomVariable();
			for(j = 0, found = false; j < i && !found; j++){
				if (retval[j].equals(a_val)){
					found = true;
				}
			}//end for
			if (found){
				i--; //then we need to find this one over again
			}
			retval[i] = a_val;
		}
		return retval;
	}
		
	public void generateHead(Statement a_state){
		Predicate head = new Predicate();
		head.setFunctionHead("q");

		Vector unique_vars = a_state.findUniqueVariables();
		
		int i;
		int rand;
		float percent_distinguished = (float) NumberDistinguished /
									  (float) unique_vars.size();
		int num_dist_needed = NumberDistinguished;

		boolean all_done = false;
		boolean add_to_end = false;
		Random random = new Random();
		if (NumberDistinguished < -1){
			System.out.println("You have entered generateHead in FullRandomStatementGenerator with fewer than zero distinguished variables");
			System.exit(1);
		}
		if (NumberDistinguished == -1){
			//then we need to use them all
			for (i = 0; i < unique_vars.size(); i++){
				head.addVariable((String)unique_vars.elementAt(i));
			}//end for	
		}
		else{
			//otherwise we need to do something slick...
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
			}//end if we needed to add the rest of the variables
		}//end else
		a_state.setHead(head);
	}//end of generateHead
	
	public static void main(String []args){
		//1, 20, 8, 4, 10, 5);
	    FullRandomStatementGenerator a = new FullRandomStatementGenerator(1, 20, 4, 12, 5);
	    Statement foo = a.getRandomStatement(false);
	    foo.print();
	}
    
}
