package datagenerator;
import java.util.Random;import java.util.Vector;

public class DuplicateCountRandomStatementGenerator extends RandomStatementGenerator{
    //the purpose of this class is to keep a strict
    //count on the number of duplicate heads in the query
    //all variables are randomized; all heads are randomized within
    //the start/stop boundaries
    //the number of variables per function is a constant (otherwise heads would
    //change)
    
    private int _num_duplicates;
//    private int _num_distinct_heads;
    private int _num_vars_per_predicate;
    private int _num_predicates;
    private int _function_start;
    private int _function_stop;
    private RandomVariableGenerator _function_head_generator;
    private RandomVariableGenerator _variable_generator;
    private int _number_distinguished;
    public DuplicateCountRandomStatementGenerator(int fun_start, int fun_stop,int dup,int pred_length,int num_pred,int num_dist,int num_vars){
        _num_duplicates = dup;
        //_num_distinct_heads = dist;
		_number_distinguished = num_dist;
		_num_vars_per_predicate = pred_length;
        _num_predicates = num_pred;
        _function_start = fun_start;
        _function_stop = fun_stop;
        if (_function_stop - _function_start < _num_predicates){
            //then we'll never be able to find heads that meet this criteria
            System.out.println("in DuplicateCountRandomStatementGenerator, too many distinct heads");
            System.out.println("increasing to use all variables from start to how many we need");
            _function_stop = _function_start + _num_predicates;
        }
        //now we also need to make sure that the number of variables is enough...
        //*** need to figure out what to add to do that.
        _function_head_generator = new RegularRandomVariableGenerator(_function_start, _function_stop);
        _variable_generator = new RegularRandomVariableGenerator(0,num_vars);
	}
    
    private Vector generateFunctionHeads(){
        Vector retval = new Vector(_num_predicates);
        Vector test = new Vector(_num_predicates);
        int i;
        String temp;
        if (_function_stop - _function_start  == _num_predicates){
            //then we just need to use them all
            for (i  = 0; i < _num_predicates; i++){
                retval.addElement(padHead((new Integer(i+_function_start)).toString(),_num_vars_per_predicate));
            }
        }
		else{
            for (i = 0; i < _num_predicates; i++){
                temp = _function_head_generator.getRandomVariable();
                while(test.contains(temp)){
                    temp= _function_head_generator.getRandomVariable();
                }
                test.addElement(temp);
                retval.addElement(padHead(temp,_num_vars_per_predicate));
            }
        }
        return retval;
    }//end generateFunctionHeads
    
    boolean containsPred(Predicate to_check, Vector the_list){
        //returns true if the_list contains to_check
        int i;
        for (i = 0; i < the_list.size();i++){
            if(((Predicate)the_list.elementAt(i)).equals(to_check)){
                return true;
            }
        }
        return false;
    }
        
    public Vector getAllDuplicates(String head){
        int i,j; 
        Vector retval = new Vector(_num_duplicates);
        //Predicate[] retval = new Predicate[_num_duplicates];
        Predicate temp;
        for (i = 0; i < _num_duplicates; i++){
            temp = new Predicate();
            temp.setFunctionHead(head);
         
            for  (j = 0; j < _num_vars_per_predicate;j++){
                temp.addVariable(_variable_generator.getRandomVariable());
            }
            //now we have one, make use it's not in there.
            while (containsPred(temp,retval)){
                temp = new Predicate();
                temp.setFunctionHead(head);
                for(j = 0; j < _num_vars_per_predicate;j++){
                    temp.addVariable(_variable_generator.getRandomVariable());
                }
            }
            //at this point, we know that we have one that wasn't there before
            retval.addElement(temp);
        }//now we have all of them
        return retval;
    }//end getAllDuplicates
            
            
    public Statement getRandomStatement(boolean use_all){
        int prednumber;
        int i;
        //Predicate one;
        Vector some_dups;
        Statement retval = new Statement();
        Vector heads = generateFunctionHeads();
        for (prednumber = 0; prednumber < _num_predicates; prednumber++){
            some_dups = getAllDuplicates((String)heads.elementAt(prednumber));
            for (i = 0; i < _num_duplicates; i++){
                retval.addSubgoal((Predicate)some_dups.elementAt(i));
            }
        }
        //at this point, we've added all of the predicates, now set the head
       generateHead(retval);
            

        return retval;
	}
	
        public void generateHead(Statement a_state){
		Predicate head = new Predicate();
		head.setFunctionHead("q");

		Vector unique_vars = a_state.findUniqueVariables();
		
		int i;
		int rand;
		float percent_distinguished = (float) _number_distinguished /
									  (float) unique_vars.size();
		int num_dist_needed = _number_distinguished;

		boolean all_done = false;
		boolean add_to_end = false;
		Random random = new Random();
		if (_number_distinguished < -1){
			System.out.println("You have entered generateHead in DuplicateCountRandomStatementGenerator with fewer than zero distinguished variables");
			System.exit(1);
		}
		if (_number_distinguished == -1){
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
  
	public static void main(String [] args){		  DuplicateCountRandomStatementGenerator bob = new DuplicateCountRandomStatementGenerator(1,15,3,5,4,4,20);
//		  DuplicateCountRandomStatementGenerator bob = new DuplicateCountRandomStatementGenerator(1,15,3,5,4,4,20);
		  Statement bobbob = bob.getRandomStatement(false);		  bobbob.print();
		 // bobbob.printString();
		  System.out.println("done");
	}
}
    