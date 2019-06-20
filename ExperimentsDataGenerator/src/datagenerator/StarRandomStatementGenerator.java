package datagenerator;
import java.io.BufferedReader;import java.io.File;import java.io.FileNotFoundException;import java.io.FileReader;import java.io.IOException;import java.io.PrintWriter;import java.util.Random;import java.util.Vector;

public class StarRandomStatementGenerator extends RandomStatementGenerator{ 
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
	int _predicate_length;
    int NumberDistinguished;
    RandomVariableGenerator FunctionGenerator;
    String FunctionOffset;
	int _max_number_duplicates;
	String _center_offset;
    
    public StarRandomStatementGenerator( int start,int stop, int num_fun,int predicate_length,int dist,int num_dup){
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
            _predicate_length = predicate_length;
            NumberDistinguished = dist;
			if (NumberDistinguished < -3){//then someone has done something silly
				System.out.println("In StarRandomStatementGenerator someone has attempted to pass in");
				System.out.println("a number less than -3 for the number of distinguished variables; program exiting");
				System.exit(1);
			}
			//_number_of_duplicates = num_dup;
            FunctionGenerator = new RegularRandomVariableGenerator(start,stop);
            //Random rand = new Random();
            if (_predicate_length < 10){
                FunctionOffset = "00" + _predicate_length;
            }
            else if (_predicate_length < 100){
                FunctionOffset = "0" + _predicate_length;
            }
			if (NumberFunctions -1 < 10){
				_center_offset = "00" + (NumberFunctions - 1);
			}
			else if (NumberFunctions -1 < 100){
				_center_offset = "0" + (NumberFunctions - 1);
			}
			if (FunctionStart > FunctionStop){
				//then we've screwed up; return an error message and exit
				System.out.println("function stop greater than the function start in StarRandomStatementGenerator; exiting");
				System.exit(1);
			}
			if (NumberFunctions > _max_number_duplicates * (FunctionStop - FunctionStart)){
				System.out.println("You have requested more predicates than available in StarRandomStatementGenerator; exiting");
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
     int extra_variable = 0;
     //int length;
     //int fcnlength;
     //String temp;
     Predicate apred;
	 String a_function_head;
     for (i = 0; i < NumberFunctions -1; i++){
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
        for (j =0; j < _predicate_length; j++){
            apred.addVariable((new Integer(extra_variable)).toString());
            extra_variable++;
        }
        retval.addSubgoal(apred);  
        
     }
	 //now we need to generate the center predicate
	 generateCenter(retval);
	 if (NumberDistinguished > -2){
		generateHead(retval); 
	 }
     return retval;

  }
 
 public void generateCenter(Statement a_state){
	//this function generates the center predicate.  It chooses randomly
	//a variable from each other predicate and adds it to it.
	Predicate center = new Predicate(NumberFunctions -1);
	Predicate head = new Predicate();
	head.setFunctionHead("q");
	//we want the predicate to have that many variables in it.
	
	String a_function_head = FunctionGenerator.getRandomVariable() + _center_offset;
	Predicate current_pred;
	//int seed = System.currentTimeMillis();
	Random random = new Random(System.currentTimeMillis());
	int current_location;
	int i;
	while (a_state.numPredicateOccurances(a_function_head) == _max_number_duplicates){
		//if this is the case, then we need to pick a new one
		a_function_head = FunctionGenerator.getRandomVariable() + _center_offset;
		//note, we should be safe entering this loop, because we have carefully 
		//checked the number of predicates available earlier
	}
	center.setFunctionHead(a_function_head);
	//now set the variables
	for (a_state.first();!a_state.isDone();a_state.next()){
		current_location = (int) (random.nextFloat() * (float) _predicate_length);
		current_pred = a_state.current();
		center.addVariable(current_pred.variableI(current_location));
		if (NumberDistinguished == -2){//then we need to do all of the non joined ones
			for (i = 0; i < current_pred.size();i++){
				if (i != current_location){
					head.addVariable(current_pred.variableI(i));
				}
			}//end for
		}
		else if (NumberDistinguished == -3){//then we need to do all of the joined ones
			head.addVariable(current_pred.variableI(current_location));
		}
		//otherwise, we leave it for later
		
	}
	//end of making all of the predicates, now add them if necessary
	a_state.addSubgoal(center);
	if (NumberDistinguished < -1){
		a_state.setHead(head);
	}
 }//end generateCenter
 
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
			System.out.println("You have entered generateHead in StarRandomStatementGenerator with fewer than zero distinguished variables");
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
        	public static void main(String []args){				String dir = System.getProperty("user.home")+"/users_link/gkonstant/Desktop/star_queries";		new File(dir).mkdir();				for(int i=0; i<100; i++)		{			new File(dir+"/run_"+i).mkdir();			new File(dir+"/run_"+i+"/views_for_q_"+i).mkdir();			for(int j=0; j<140; j++)			{				File file = new File(dir+"/run_"+i+"/views_for_q_"+i+"/view_"+j+".txt");				try {					file.createNewFile();				} catch (IOException e) {					throw new RuntimeException(e);				}				PrintWriter outfile;				try {					outfile = new PrintWriter(file);				} catch (FileNotFoundException e) {					throw new RuntimeException(e);				}				StarRandomStatementGenerator a = new StarRandomStatementGenerator(1, 8, 5, 4, 4, 5);				Statement foo = a.getRandomStatement(false);				foo.getHead().setFunctionHead("v"+j);								Vector vec = new Vector();				for(Object ob:foo.getHead().variables)				{					String str = ((String)ob);					vec.add("X"+str);				}				foo.getHead().variables = vec;								for(Predicate pred:foo.body)				{					Vector vec1 = new Vector();					if(pred == null)						continue;					pred.setFunctionHead("m"+pred.getFunctionHead());					for(Object ob:pred.variables)					{						String str = ((String)ob);						vec1.add("X"+str);					}					pred.variables = vec1;				}												foo.print();								if(j>0)				{					 BufferedReader in;					try {						in = new BufferedReader(new FileReader(dir+"/run_"+i+"/views_for_q_"+i+"/view_"+(j-1)+".txt"));					} catch (FileNotFoundException e1) {						throw new RuntimeException(e1);					}										 String line;					try {						line = in.readLine();					} catch (IOException e) {						throw new RuntimeException(e);					}										while (line != null)					{						outfile.append(line).append(System.getProperty("line.separator"));						try {							line = in.readLine();						} catch (IOException e) {							throw new RuntimeException(e);						}					}				}								outfile.append(foo.printString()).append(System.getProperty("line.separator"));				outfile.close();			}		}				dir = System.getProperty("user.home")+"/users_link/gkonstant/Desktop/star_queries";		for(int i=0; i<100; i++)		{			new File(dir+"/run_"+i);			new File(dir+"/run_"+i+"/views_for_q_"+i);			File file = new File(dir+"/run_"+i+"/views_for_q_"+i+"/query_"+i+".txt");						StarRandomStatementGenerator a = new StarRandomStatementGenerator(1, 8, 5, 4, 4, 5);			Statement foo = a.getRandomStatement(false);			foo.getHead().setFunctionHead("q"+i);						Vector vec = new Vector();			for(Object ob:foo.getHead().variables)			{				String str = ((String)ob);				vec.add("X"+str);			}			foo.getHead().variables = vec;						for(Predicate pred:foo.body)			{				Vector vec1 = new Vector();				if(pred == null)					continue;				pred.setFunctionHead("m"+pred.getFunctionHead());				for(Object ob:pred.variables)				{					String str = ((String)ob);					vec1.add("X"+str);				}				pred.variables = vec1;			}									foo.print();						PrintWriter outfile;			try {				outfile = new PrintWriter(file);			} catch (FileNotFoundException e) {				throw new RuntimeException(e);			}						outfile.append(foo.printString()).append(System.getProperty("line.separator"));			outfile.close();		}	}
   
}
