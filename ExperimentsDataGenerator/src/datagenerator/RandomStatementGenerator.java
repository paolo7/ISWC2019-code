package datagenerator;


//import java.lang.*;
import java.util.Random;
import java.util.Vector;
public abstract class RandomStatementGenerator  {
    String generatorType;
    Random random;
    
       
    public abstract Statement getRandomStatement(boolean use_all);
    
    public void generateHead(Statement a_state,boolean use_all){
        
        Vector unique_vars = a_state.findUniqueVariables();
        int i,j,k;
        //Vector contains_variable_2;
        //int rand;
        //String a_variable= "";
        //Predicate subgoal_to_add;
        //boolean need_to_add;
        Predicate head = new Predicate();
        head.setFunctionHead("Q");
        Predicate a_subgoal;
        //Vector vars_to_check = new Vector();
        //Statement contains_variables = new Statement();
        Statement does_not_contain_vars = new Statement();
        //Statement need_to_check = new Statement();
        //a_subgoal = a_state.subgoalI(0);
        Statement predicates_to_check = a_state;
        Vector variables_to_check;
        //note, special case if the statement is only one big
        
        if (use_all){
            for (i = 0; i < unique_vars.size();i++){
                head.addVariable((String)unique_vars.elementAt(i));
            }
        }
        else{
         
            while (predicates_to_check.size() != 0){
                //while there are still predicates left unaccounted for
                a_subgoal = predicates_to_check.subgoalI(0);
                variables_to_check = new Vector();
                for (i = 0; i < a_subgoal.variables.size(); i++){
                    //add all of the variables in the first predicate
                    if (!variables_to_check.contains(a_subgoal.variableI(i))){
                        variables_to_check.addElement(a_subgoal.variableI(i));
                    }
                }
                for (i = 0; i < variables_to_check.size(); i++){
                    does_not_contain_vars = new Statement();
                    for (j = 0; j < predicates_to_check.size(); j++){
                        if (predicates_to_check.subgoalI(j).containsVariable((String)variables_to_check.elementAt(i))){
                            //in this case, we need to add the rest of the variables
                            //to what we're checking
                            a_subgoal = predicates_to_check.subgoalI(j);
                            for (k = 0; k < a_subgoal.variables.size();k++){
                                if (!variables_to_check.contains(a_subgoal.variableI(k))){
                                    variables_to_check.addElement(a_subgoal.variableI(k));
                                }//end if
                            }//end for
                        }//end of if we need to add variables to the ones to be checked
                        else {
                            //we need to add it to the next to check
                            does_not_contain_vars.addSubgoal(predicates_to_check.subgoalI(j));
                        }//end of if we need to add it to the list of things to check
                    }//end of looping over the predicates to make sure that we have all of the subgoals with *this* variable
                    //now we need to prepare for the next iteration
                    predicates_to_check = does_not_contain_vars;
                }//end of what we have checked
                //at this point, we have all of the variables that are connected in one blob.
                //in variables_to_check...
                //now we do something with it, and start the whole thing over with does_not_contain_vars
                if (variables_to_check.size() > 0){ //this should always be the case, but just to be sure..
                    head.addVariable((String)variables_to_check.elementAt(0));
                }
            }//end for of the whole thing...
        }

        a_state.setHead(head);
        //System.gc();
      
    }//end of generateHead

    protected String padHead(String func_head,int num_vars){
        String retval = new String(func_head);
        if (num_vars < 10){
            retval = retval + "0";
        }
        if (num_vars < 100){
            retval = retval + "0";
        }
        retval = retval + (new Integer(num_vars)).toString();
        return retval;
    }//end padHead
}
