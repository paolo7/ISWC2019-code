package datagenerator;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Vector;

public class ChainRandomStatementGenerator extends RandomStatementGenerator{ 
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
    String FunctionOffset;
  
    
    public ChainRandomStatementGenerator( int start,int stop, int num_fun,int length,int dist, int num_dup){
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
				System.out.println("You have requested more predicates than available in ChainRandomStatementGenerator; exiting");
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
}//end ChainRandomStatementGenerator(int,int)
    
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
        
    
 	public static void main(String []args){
	String dir = System.getProperty("user.dir")+"/constraint_data/" +
 	"longDistinguishedViews";
//			"chain_10qX10000v+qAsView_20PredSpaceTill20v_" +
//			"500predSpaceAfter20_8PredBody_4var_10Dtill80v_3Dtill10000v_5repMax/data";
//			
	//new File(dir).mkdir();
	
	for(int i=1; i<10; i++)
		//int i=0;
		{
			

			new File(dir+"/run_"+i).mkdir();
//			new File(dir+"/run_"+i+"/views_for_q_"+i).mkdir();

//			File file1 = new File(dir+"/run_"+i+"/views_for_q_"+i+"/query_"+i+".txt");
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

			ChainRandomStatementGenerator a1 = new ChainRandomStatementGenerator(1, 20, 5, 10, 0, 2);
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
			
			File file = new File(dir+"/run_"+i+"/views_for_q_"+i+".txt");
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			PrintWriter outfile;
			try {
				outfile = new PrintWriter(file);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
			
//			for(int j=0; j<21; j++)
//			{
//				
////				File file = new File(dir+"/run_"+i+"/views_for_q_"+i+"/view_"+j+".txt");
////				try {
////					file.createNewFile();
////				} catch (IOException e) {
////					throw new RuntimeException(e);
////				}
////				PrintWriter outfile;
////				try {
////					outfile = new PrintWriter(file);
////				} catch (FileNotFoundException e) {
////					throw new RuntimeException(e);
////				}
//				
//
//				ChainRandomStatementGenerator a = new ChainRandomStatementGenerator(1, 500, 8, 10, 70, 5);
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
//				foo.print();
//				
////				if(j>0)
////				{
////					 BufferedReader in;
////					try {
////						in = new BufferedReader(new FileReader(dir+"/run_"+i+"/views_for_q_"+i+"/view_"+(j-1)+".txt"));
////					} catch (FileNotFoundException e1) {
////						throw new RuntimeException(e1);
////					}
////					
////					 String line;
////					try {
////						line = in.readLine();
////					} catch (IOException e) {
////						throw new RuntimeException(e);
////					}
////					
////					while (line != null)
////					{
////						outfile.append(line).append(System.getProperty("line.separator"));
////						try {
////							line = in.readLine();
////						} catch (IOException e) {
////							throw new RuntimeException(e);
////						}
////					}
////					
////					try {
////						in.close();
////					} catch (IOException e) {
////						e.printStackTrace();
////					}
////				}
//				
//				outfile.append(foo.printString()).append(System.getProperty("line.separator"));
////				outfile.close();
//			}
			Random r = new Random();
			for(int j=0; j<10000; j++)
			{
//				File file = new File(dir+"/run_"+i+"/views_for_q_"+i+"/view_"+j+".txt");
//				try {
//					file.createNewFile();
//				} catch (IOException e) {
//					throw new RuntimeException(e);
//				}
//				PrintWriter outfile;
//				try {
//					outfile = new PrintWriter(file);
//				} catch (FileNotFoundException e) {
//					throw new RuntimeException(e);
//				}

				ChainRandomStatementGenerator a = new ChainRandomStatementGenerator(1, (j<300)?300:j, 8, 10, 1+r.nextInt(70), 5);
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
				
				
//				foo.print();
				
//				if(j>0)
//				{
//					 BufferedReader in;
//					try {
//						in = new BufferedReader(new FileReader(dir+"/run_"+i+"/views_for_q_"+i+"/view_"+(j-1)+".txt"));
//					} catch (FileNotFoundException e1) {
//						throw new RuntimeException(e1);
//					}
//					
//					 String line;
//					try {
//						line = in.readLine();
//					} catch (IOException e) {
//						throw new RuntimeException(e);
//					}
//					
//					while (line != null)
//					{
//						outfile.append(line).append(System.getProperty("line.separator"));
//						try {
//							line = in.readLine();
//						} catch (IOException e) {
//							throw new RuntimeException(e);
//						}
//					}
//					
//					try {
//						in.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
				
				outfile.append(foo.printString()).append(System.getProperty("line.separator"));
//				outfile.close();
			}
			outfile.close();

		}
 	}

    
//	public static void main(String []args){
//		String dir = "/opt/GQRexperiments/ucq_fixedQ2VRelevanceUpto180_thereafterIcreasing/data/";
//				
////		new File().mkdir();
//		
//		
//		
//		//This 3x10 queries UCQ with 1000 views (one viewset file)
//		for(int i=0; i<100; i++)
//		{
//			
//			new File(dir+"/run_"+i).mkdir();
//			
//
//			//=============================== 10 queries for case i with pscpace size  = 20
//			
//			File file1 = new File(dir+"/run_"+i+"/queries_"+i+"_20p.txt");
//			
//			try {
//				file1.createNewFile();
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//			PrintWriter outfile1;
//			try {
//				outfile1 = new PrintWriter(file1);
//			} catch (FileNotFoundException e) {
//				throw new RuntimeException(e);
//			}
//			
//			for(int j=0; j<10; j++)
//			{
//				//ChainRandomStatementGenerator(start_pred_space 1, stop_pred_space 20, #preds/body 8, predlength 4, #dist 10, #dupl 5);
//				ChainRandomStatementGenerator a1 = new ChainRandomStatementGenerator(1, 20, 8, 4, 7, 4);
//				Statement foo1 = a1.getRandomStatement(false);
//				foo1.getHead().setFunctionHead("q"+i+j+"20p"); //add prefix q in the heads
//
//				Vector vec2 = new Vector();
//				for(Object ob:foo1.getHead().variables)
//				{
//					String str = ((String)ob);
//					vec2.add("X"+str);// add prefix X in all variables
//				}
//				foo1.getHead().variables = vec2;
//
//				for(Predicate pred:foo1.body)
//				{
//					Vector vec3 = new Vector();
//					if(pred == null)
//						continue;
//					pred.setFunctionHead("m"+pred.getFunctionHead()); //put prefix m in front of a predicate's name
//					for(Object ob:pred.variables)
//					{
//						String str = ((String)ob);
//						vec3.add("X"+str);
//					}
//					pred.variables = vec3;
//				}
//
//				foo1.print();
//				outfile1.append(foo1.printString()).append(System.getProperty("line.separator"));
//			}
//			outfile1.close();
//
//			//==============================
//			
//			//=============================== 10 queries for case i with pscpace size  = 40
//			
//			file1 = new File(dir+"/run_"+i+"/queries_"+i+"_40p.txt");
//			
//			try {
//				file1.createNewFile();
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//
//			try {
//				outfile1 = new PrintWriter(file1);
//			} catch (FileNotFoundException e) {
//				throw new RuntimeException(e);
//			}
//			
//			for(int j=0; j<10; j++)
//			{
//				//ChainRandomStatementGenerator(start_pred_space 1, stop_pred_space 20, #preds/body 8, predlength 4, #dist 10, #dupl 5);
//				ChainRandomStatementGenerator a1 = new ChainRandomStatementGenerator(21, 60, 8, 4, 7, 4);
//				Statement foo1 = a1.getRandomStatement(false);
//				foo1.getHead().setFunctionHead("q"+i+""+j+"40p"); //add prefix q in the heads
//
//				Vector vec2 = new Vector();
//				for(Object ob:foo1.getHead().variables)
//				{
//					String str = ((String)ob);
//					vec2.add("X"+str);// add prefix X in all variables
//				}
//				foo1.getHead().variables = vec2;
//
//				for(Predicate pred:foo1.body)
//				{
//					Vector vec3 = new Vector();
//					if(pred == null)
//						continue;
//					pred.setFunctionHead("m"+pred.getFunctionHead()); //put prefix m in front of a predicate's name
//					for(Object ob:pred.variables)
//					{
//						String str = ((String)ob);
//						vec3.add("X"+str);
//					}
//					pred.variables = vec3;
//				}
//
//				foo1.print();
//				outfile1.append(foo1.printString()).append(System.getProperty("line.separator"));
//			}
//			outfile1.close();
//
//			//==============================
//			
//			//=============================== 10 queries for case i with pscpace size  = 60
//			
//			file1 = new File(dir+"/run_"+i+"/queries_"+i+"_60p.txt");
//			
//			try {
//				file1.createNewFile();
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//
//			try {
//				outfile1 = new PrintWriter(file1);
//			} catch (FileNotFoundException e) {
//				throw new RuntimeException(e);
//			}
//			
//			for(int j=0; j<10; j++)
//			{
//				//ChainRandomStatementGenerator(start_pred_space 1, stop_pred_space 20, #preds/body 8, predlength 4, #dist 10, #dupl 5);
//				ChainRandomStatementGenerator a1 = new ChainRandomStatementGenerator(1, 60, 8, 4, 7, 4);
//				Statement foo1 = a1.getRandomStatement(false);
//				foo1.getHead().setFunctionHead("q"+i+""+j+"60p"); //add prefix q in the heads
//
//				Vector vec2 = new Vector();
//				for(Object ob:foo1.getHead().variables)
//				{
//					String str = ((String)ob);
//					vec2.add("X"+str);// add prefix X in all variables
//				}
//				foo1.getHead().variables = vec2;
//
//				for(Predicate pred:foo1.body)
//				{
//					Vector vec3 = new Vector();
//					if(pred == null)
//						continue;
//					pred.setFunctionHead("m"+pred.getFunctionHead()); //put prefix m in front of a predicate's name
//					for(Object ob:pred.variables)
//					{
//						String str = ((String)ob);
//						vec3.add("X"+str);
//					}
//					pred.variables = vec3;
//				}
//
//				foo1.print();
//				outfile1.append(foo1.printString()).append(System.getProperty("line.separator"));
//			}
//			outfile1.close();
//			//==============================
//			
//			
//			// CONSTRUCT VIEWS IN THE SAME FILE
//			
//			File file = new File(dir+"/run_"+i+"/views_for_q_"+i+".txt");
//			try {
//				file.createNewFile();
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//			PrintWriter outfile;
//			try {
//				outfile = new PrintWriter(file);
////				foo1.getHead().setFunctionHead("vq"); ///COPY THE QUERYY IN THE VIEWSSSSSSSSSS
////				outfile.append(foo1.printString()).append(System.getProperty("line.separator"));
//			} catch (FileNotFoundException e) {
//				throw new RuntimeException(e);
//			}
//			
//			for(int j=0; j<10000; j++)
//			{
//				ChainRandomStatementGenerator a = null;
//				if(j<180)
//				{
//					if(j%4 == 0)
//						a = new ChainRandomStatementGenerator(1, 20, 8, 4, 10, 4);
//					else if(j%4 == 1 || j%4 == 2)
//						a = new ChainRandomStatementGenerator(21, 60, 8, 4, 10, 4);
//					else
//						a = new ChainRandomStatementGenerator(61, 80, 8, 4, 10, 4);
//				}
//				else
//				{
//					a = new ChainRandomStatementGenerator(1, j, 8, 4, 3, 4);
////					if(j%4 == 0)
////						a = new ChainRandomStatementGenerator(1, 20, 8, 4, 3, 4);
////					else if(j%4 == 1 || j%4 == 2)
////						a = new ChainRandomStatementGenerator(21, 60, 8, 4, 3, 4);
////					else
////						a = new ChainRandomStatementGenerator(61, 80, 8, 4, 3, 4);
//				}
////				
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
//			
//			outfile.close();
//		}
//	}
	
	
   /*
     	public static void main(String []args){
		String dir = System.getProperty("user.home")+"/users_link/gkonstant/Desktop/" +
				"experiments/chain_10qX10000v+qAsView_20PredSpaceTill20v_" +
				"500predSpaceAfter20_8PredBody_4var_10Dtill80v_3Dtill10000v_5repMax/data";
				
		//new File(dir).mkdir();
		
		
		//FOR 100qX140 views with each viewset in different file : 1) go to end of file 2) uncomment code 3) look at remaining comments
		//This is for 10 queries with 10000 views (one viewset file)
		for(int i=0; i<10; i++)
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
			
			//ChainRandomStatementGenerator(start_pred_space 1, stop_pred_space 20, #preds/body 8, predlength 4, #dist 10, #dupl 5);
			ChainRandomStatementGenerator a1 = new ChainRandomStatementGenerator(1, 20, 8, 4, 10, 5);
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
			
			for(int j=0; j<81; j++)
			{
				ChainRandomStatementGenerator a = null;
				if(j<21)
					a = new ChainRandomStatementGenerator(1, 20, 8, 4, 10, 5);
				else
					a = new ChainRandomStatementGenerator(1, j, 8, 4, 10, 5);
				
					
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
				
				
				//foo.print();
				
				outfile.append(foo.printString()).append(System.getProperty("line.separator"));
			}
			
			for(int j=81; j<10000; j++)
			{
				if(j%1000 == 0)
					System.out.println(j);
				
				ChainRandomStatementGenerator a = new ChainRandomStatementGenerator(1, 500, 8, 4, 3, 5);
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
		}
	}
    */
}
/*
for(int i=1; i<10; i++)
//int i=0;
{
	

	new File(dir+"/run_"+i).mkdir();
//	new File(dir+"/run_"+i+"/views_for_q_"+i).mkdir();

//	File file1 = new File(dir+"/run_"+i+"/views_for_q_"+i+"/query_"+i+".txt");
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

	ChainRandomStatementGenerator a1 = new ChainRandomStatementGenerator(1, 20, 8, 4, 10, 5);
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
	
	File file = new File(dir+"/run_"+i+"/views_for_q_"+i+".txt");
	try {
		file.createNewFile();
	} catch (IOException e) {
		throw new RuntimeException(e);
	}
	PrintWriter outfile;
	try {
		outfile = new PrintWriter(file);
	} catch (FileNotFoundException e) {
		throw new RuntimeException(e);
	}
	
	for(int j=0; j<81; j++)
	{
		
//		File file = new File(dir+"/run_"+i+"/views_for_q_"+i+"/view_"+j+".txt");
//		try {
//			file.createNewFile();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//		PrintWriter outfile;
//		try {
//			outfile = new PrintWriter(file);
//		} catch (FileNotFoundException e) {
//			throw new RuntimeException(e);
//		}
		

		ChainRandomStatementGenerator a = new ChainRandomStatementGenerator(1, 20, 8, 4, 10, 5);
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
		
		
		foo.print();
		
//		if(j>0)
//		{
//			 BufferedReader in;
//			try {
//				in = new BufferedReader(new FileReader(dir+"/run_"+i+"/views_for_q_"+i+"/view_"+(j-1)+".txt"));
//			} catch (FileNotFoundException e1) {
//				throw new RuntimeException(e1);
//			}
//			
//			 String line;
//			try {
//				line = in.readLine();
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//			
//			while (line != null)
//			{
//				outfile.append(line).append(System.getProperty("line.separator"));
//				try {
//					line = in.readLine();
//				} catch (IOException e) {
//					throw new RuntimeException(e);
//				}
//			}
//			
//			try {
//				in.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		
		outfile.append(foo.printString()).append(System.getProperty("line.separator"));
//		outfile.close();
	}
	
	for(int j=81; j<10000; j++)
	{
//		File file = new File(dir+"/run_"+i+"/views_for_q_"+i+"/view_"+j+".txt");
//		try {
//			file.createNewFile();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//		PrintWriter outfile;
//		try {
//			outfile = new PrintWriter(file);
//		} catch (FileNotFoundException e) {
//			throw new RuntimeException(e);
//		}

		ChainRandomStatementGenerator a = new ChainRandomStatementGenerator(1, 20, 8, 4, 3, 5);
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
		
		
//		foo.print();
		
//		if(j>0)
//		{
//			 BufferedReader in;
//			try {
//				in = new BufferedReader(new FileReader(dir+"/run_"+i+"/views_for_q_"+i+"/view_"+(j-1)+".txt"));
//			} catch (FileNotFoundException e1) {
//				throw new RuntimeException(e1);
//			}
//			
//			 String line;
//			try {
//				line = in.readLine();
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//			
//			while (line != null)
//			{
//				outfile.append(line).append(System.getProperty("line.separator"));
//				try {
//					line = in.readLine();
//				} catch (IOException e) {
//					throw new RuntimeException(e);
//				}
//			}
//			
//			try {
//				in.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		
		outfile.append(foo.printString()).append(System.getProperty("line.separator"));
//		outfile.close();
	}
	outfile.close();
*/