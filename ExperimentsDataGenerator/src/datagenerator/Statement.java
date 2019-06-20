package datagenerator;
import java.util.Vector;
public class Statement {
	//This class implements the statement, or a conjunctive query in datalog
	//It allows the following methods:
	//Constructors: Statement() -- uses the default values
	//				Statement(int a_size) -- creates a new statement with this initial size
	//				Statement(Statement a) -- makes a deep copy of "a"
	//
	//Other functions:
	//Statement reorderSubgoals(Statement re_order_orig, Statement order_by)--
	//                          returns re_order_orig as ordered by order_by
	//boolean addSubgoal(Predicate aval) -- adds a subgoal to the statement, increasing
	//										the size of body if necessary
	//Vector getDistinguishedVariables() -- returns the head variables
	//int size() -- returns the number of predicates in the statement
	//boolean addAllSubgoals(Statement a_state) -- adds all of the subgoals in a_state to this
	//boolean equals(Statement astate) -- returns if the statement is exactly equal to astate
	//predicate getHead() -- returns the head of the statement
	//public boolean variableIsDistinguished(String avar) -- returns if the variable is distinguished
	//Vector predicatesThatContainFunction(String fun) -- returns a vector with the predicates
	//													  that use that function
	//Vector predicatesThatContainVariable(String avar) -- returns a vector with all the predicates
	//													   that use avar
	//Predicate subgoalI(int i) -- returns the subgoal at position I
	//boolean containsSubgoal(Predicate aval) -- returns true if aval is in the statement
	//int numPredicateOccurances(String a_head) -- returns the number of time the predicate name
	//											   occurs in the statement
	//void print() -- prints a representation to system.out
	//StringBuffer printString() -- returns the string representation of the statement
	//Vector findUniqueVariables() -- returns a vector with the unique variables in the statement
	//boolean contains(Statement R) -- returns true if this contains R (in the containment sense)
	//int predicateNumber(Predicate pred_to_check) -- returns the predicate number of the original predicate
	//Statement answersToQuery(Statement tuples) -- returns the answer of this over the tuples in tuples
	//boolean read(String values) -- reads in values into this.  returns false if it doesn't succeed
	//
	//
	// There is also an iterator over the subgoals in the statement:
	//
	//void first() -- sets the iterator to the first value
	//boolean isDone() -- returns if the iterator is done
	//void next() -- advances the current state of the iterator
	//String current() -- returns the current variable
	//int getCurrentLocation() -- returns the current location of the iterator
	//void replaceCurrent(String new_var) -- replaces the current string with another

	
    protected Predicate head; //the head of the query
    protected Predicate [] body; // the body of the query
    protected int currentpred; //the current predicate for the iterator
    protected int _max_size;//the maximum size we currently can handle (the size of body)
    protected static final int _int_max = 10000000;// the maximum int value needed for testing
    protected int _num_subgoals; //the number of subgoals in the statement
    protected boolean _unique_up_to_date; //if the count of unique variables is up to date
    protected Vector _unique_vars; //a listing of unique variables
	
    public Statement() {
        _unique_up_to_date = false;
        _unique_vars = new Vector(50);
        _max_size = 50;
        currentpred = 0;
        body = new Predicate[50];
        head = new Predicate();
        _num_subgoals = 0;
}
    
    public Statement(int a_size){
        _unique_up_to_date = false;
        _unique_vars = new Vector(a_size);
        _max_size = a_size;
        currentpred = 0;
        body = new Predicate[a_size];
        head = new Predicate();
        _num_subgoals = 0;
        
    }
    
	public Statement(String input){
        _unique_up_to_date = false;
        _unique_vars = new Vector(50);
        _max_size = 50;
        currentpred = 0;
        body = new Predicate[50];
        head = new Predicate();
        _num_subgoals = 0;
		read(input);
	}
    public Statement(Statement a){
     _num_subgoals = a._num_subgoals;
     currentpred = 0;
     head = new Predicate(a.head);
     body = new Predicate[a._max_size];
     for (int i = 0; i < a.size();i++){
        body[i] = new Predicate(a.subgoalI(i));
     }
     _unique_vars = a._unique_vars;

     _unique_up_to_date = a._unique_up_to_date;
    }//end copy constructor
    
    
   public void setHead(Predicate aval){
          head = aval;
    }
    
    protected Statement reorderSubgoals(Statement re_order_orig, Statement order_by){
        //the purpose of this function is to reorder the first statement by how many times 
        //the second one occurs.
        //first, we count how many times each predicate occurs
        Statement re_order = new Statement(re_order_orig);
        Statement retval = new Statement(re_order.size());
        retval.setHead(re_order.getHead());
        Vector ordering_list = new Vector(order_by.size());
        int [] ordering_count =  new int[order_by.size()];
        String [] final_ordering = new String[order_by.size()];
        int num_unique = 0;
        int i,j;
        boolean added_one;
        int num_remaining;
        int counted_so_far = 1;
        int current_smallest;
        int smallest_elt_location = 0;
        int pred_index;
        int pred_number;
        for (pred_number = 0; pred_number < order_by.size(); pred_number++){
            pred_index = ordering_list.indexOf(order_by.subgoalI(pred_number).getFunctionHead());
            if (pred_index == -1){ //then the element was not found; add it to the end
            //of the list
                ordering_list.addElement(order_by.subgoalI(pred_number).getFunctionHead());
                ordering_count[num_unique] = 1; //we now have one count of it
                num_unique++;
            }
            else {//it was already there, and we need to update the current count
                ordering_count[pred_index]++;
            }
        }
        //at this point, we have our list.... now we just need to do the sorting
        //in some efficient manner.
        num_remaining = num_unique;
        for (i = 0; i < num_unique;i++){
            current_smallest = _int_max;
            
            for (j = 0, added_one = false; j < num_remaining && added_one == false; j++){
                if (ordering_count[j] == counted_so_far){
                    //then we know that it must be the smallest, or at least tied,
                    //so use that value instead
                    smallest_elt_location = j;
                    added_one = true;
                }//end if
                else if (ordering_count[j] < current_smallest){
                    current_smallest = ordering_count[j];
                    smallest_elt_location = j;
                }//end else if
            }//end inner for
            final_ordering[i] = (String)ordering_list.elementAt(smallest_elt_location);
            
            //now we've fixed that; let's move the last element over
            ordering_list.setElementAt(ordering_list.elementAt(num_remaining -1), smallest_elt_location);
            ordering_count[smallest_elt_location] = ordering_count[num_remaining -1];
               
            num_remaining--;
        }//end outer for
        //at this point, we have the final ordering in the right manner...
        //now rearrange the final answer and return it
        for (i = 0; i < num_unique;i++){
            for (j = 0; j < re_order.size();j++){
                if (re_order.subgoalI(j).getFunctionHead().equals(final_ordering[i])){
                    //then it's time to add it.
                    retval.addSubgoal(re_order.subgoalI(j));
                    //now shove the rest over so that we don't have to keep checking
                    //things over and over again
                    //actually, no, that might screw up the original, oh fine, i'll
                    //make a copy....  if this code actually speeds it up, i'll eat
                    //my hat.
                    re_order.body[j] = re_order.body[re_order.size() -1];
                    re_order._num_subgoals--;
                    j--;//now we need to check j again
                }
            }
        }
		//at this point, we still need to add in the rest of the subgoals that we
		//haven't done, so let's add them all in
		retval.addAllSubgoals(re_order);
        return retval;
        
    }

    public boolean addSubgoal(Predicate aval){
         if (_num_subgoals +1 > _max_size){
            //then we can add without a problem, otherwise we need to 
            //copy things over.
            //I realize that this is much like doing a vector, but this
            //may allow us to be more efficient.
            //we need to increase the size
            Predicate [] temp = new Predicate[_max_size * 2];
            //just double the side; we shouldn't need to do this often
            for (int i = 0; i < _num_subgoals; i++){
                temp[i] = body[i];
            }
            //at this point, we've copied over everything, now move it over and
            //then up _max_subgoals.  Then proceed as usual
            body = temp;
            _max_size = _max_size * 2;
         }
         body[_num_subgoals] = aval;
         _num_subgoals++;
         _unique_up_to_date = false;
         return true;
    }
    public Vector getDistinguishedVariables(){
        return head.variables;
    }
    
    public int size(){
        return _num_subgoals;
    }
    
    public boolean addAllSubgoals(Statement a_state){
        for (a_state.first();!a_state.isDone();a_state.next()){
            addSubgoal(a_state.current());
        }
        _unique_up_to_date = false;
        return true;
    }//end of addAllSubgoals
    
    public boolean equals(Statement astate){
        //note, this checks for equality by making sure 
        //that all of the subgoals are the same, though not 
        //necessarily in the same order.
        int counter;
        if (_num_subgoals == astate._num_subgoals && 
            head.equals(astate.head)){
                //now check all of the Predicates...
                for (counter = 0; counter < _num_subgoals; counter++){
                    if (!containsSubgoal((Predicate)astate.subgoalI(counter))){
                        return false;
                    }// end if
                }// end for
                return true;
        }//end if
        return false;
    }
            
    
    public Predicate getHead(){
        return head;
    }
    
    public boolean variableIsDistinguished(String avar){
        if (head.containsVariable(avar)){
            return true;
        }
        return false;
    }
        
    public Vector predicatesThatContainFunction(String fun){
        Vector retval = new Vector();
        Predicate current_pred;
        for (int i= 0; i < _num_subgoals; i++){
            current_pred = subgoalI(i);
            if (current_pred.function.equals(fun)){
                retval.addElement(current_pred);
            }//end if
        }//end for
        return retval;
    }//end predicatesThatContainFunction
    
    public Vector predicatesThatContainVariable(String avar){
        Vector retval = new Vector(10);
        Predicate current_pred;
        for (int i = 0; i < size();i++){
            current_pred = subgoalI(i);
            if (current_pred.containsVariable(avar)){
                retval.addElement(current_pred);
            }
        }
        return retval;
    }
    
    public Predicate subgoalI(int i){
        return (Predicate) body[i];
    }
    
    public boolean containsSubgoal(Predicate aval){
        int counter;
        for (counter = 0; counter < size(); counter++){
            if (subgoalI(counter).equals(aval)){
                   return true;
            }
        }
        return false;
    }
    
	public int numPredicateOccurances(String a_head){
		int retval = 0;
		for (int i = 0; i < size(); i++){
			if (subgoalI(i).getFunctionHead().equals(a_head)){
				//then we need to increment the count
				retval++;
			}
		}
		return retval;
	}//end numPredicateOccurances
		
    public void print(){
        System.out.println(printString());
    }
    
    public StringBuffer printString(){
        StringBuffer retval = new StringBuffer(head.printString().toString());
        int counter;
        retval.append(":-");
        if (_num_subgoals > 0){
            for(counter = 0; counter < _num_subgoals -1;counter++){
                retval.append(subgoalI(counter).printString().toString());
                retval.append(",");
            }
            retval.append(subgoalI(counter).printString().toString());
        }
        return retval;
    }//end printString
    
    public Vector findUniqueVariables(){
        /**returns the unique variables in the statement**/
        if (_unique_up_to_date == true){
            //if the cached value is correct, return it, otherwise
            //calculate a new one
            return _unique_vars;
        }
        _unique_up_to_date = true;
        int i,j,k;
        Vector retvals= new Vector(size());
        String aval;
        int numvars = 0;
        Predicate curr;
        boolean flag;
        for (i = 0; i < _num_subgoals; i++){ 
            //each iteration is one subgoal
            curr = subgoalI(i);
            for(j = 0; j < curr.size(); j++){ 
                //each iteration is one variable
                //System.out.println("foo");
                aval = curr.variableI(j);
                //now check to see if it's in the array
                flag = false;
                for (k = 0; k < numvars && !flag; k++){
                    //check to see if retvals has variable
                    if(retvals.elementAt(k).equals(aval)){
                        flag = true;
                    }
                }//end for checking to see if retvals has variable
                if (flag == false){ 
                    //then didn't have the value; enter it
                    retvals.addElement(new String(aval));
                    numvars++;
                }//end of needing to add it
            }//end for
        }
        _unique_vars = retvals;
        return _unique_vars;
    }//end find uniquevariables
    
            
            
            
    
    public boolean contains(Statement R){
        Vector uniqueQVariables;
        int varcounter;
        int i;
        int match = 0;
        Predicate apred;
        Statement ordered_original = this;
		/* this is where we'd put in reorder if it actually sped things up, but it
		   slows things down....
			if (size() > 10 || R.size() > 10){
			//only order if there are at least 10 subgoals..., otherwise, probably
			//not worth it
			ordered_original = reorderSubgoals(this,R);
		}*/
 /*       boolean flag = true;
        Statement q2 = new Statement();
        q2.setHead(new Predicate(R.getHead()));
        Statement x;
        uniqueQVariables = R.findUniqueVariables();
        for(R.first();!R.isDone();R.next()){//looping over the statement
            apred = new Predicate(R.currentSubgoal());
            flag = true;
            for (apred.first(),varcounter = 0;!apred.isDone();
            apred.next(),varcounter++){//now we are looping over the variables
                for(i = 0 , flag = false;
                i < uniqueQVariables.size() && flag==false ;
                i++){
                    if (uniqueQVariables.elementAt(i).equals(apred.current())){
                        flag = true;
                        match = i;
                    }
                }
                if (flag == false){ 
                    //then we screwed up somewhere, this should never happen
                    System.out.println("We failed to set the unique values properly in containment");
                    return false;
                }
                
                apred.variables.setElementAt(Integer.toString(match),varcounter);
            }//end looping over the variables
            q2.addSubgoal(apred);
        }//end looping over the whole thing...
        //note, we still need to do the head; block copy from above
        //q2.print();           
        apred = q2.getHead();
        for (apred.first(),varcounter = 0;!apred.isDone();apred.next(),varcounter++){
            for(i = 0 , flag = false;
            i < uniqueQVariables.size() && flag==false; i++){
                if (uniqueQVariables.elementAt(i).equals(apred.current())){
                    flag = true;
                    match = i;
                }
            }
            if (flag == false){ 
                //then we screwed up somewhere, this should never happen
                System.out.println("We failed to set the unique values properly on the head");
                return false;
            }
            apred.variables.setElementAt(Integer.toString(match),varcounter);
        }
        x = new Statement(q2);
   */     Statement answers = answersToQuery(R);
        if(!answers.containsSubgoal(R.getHead())){
           return false;
        }
       
        return true;
    }//end contains
    
        
    public Predicate currentSubgoal(){
        return subgoalI(currentpred);
    }
    
    public boolean isDone(){
        if (currentpred >= _num_subgoals){
            return true;
        }
        return false;
    }
    
    public Predicate current(){
        return subgoalI(currentpred);
    }
    
    public int getCurrentLocation(){
        return currentpred;
    }
    
    public void next(){
        currentpred++;
    }
    public void first(){
        currentpred = 0;
    }
    

    
    public int predicateNumber(Predicate pred_to_check){
        //this returns the predicate number that matches
        //we assume that we are being given the orginal predicate; 
        //otherwise it will take longer if we just want equivalent answers
        //will return -1 if not found
        int i;
        for (i = 0,first();!isDone(); next(), i++){
            if (pred_to_check == current()){
                return i;
            }
        }
        return -1;
            
    }
    public Statement answersToQuery(Statement tuples){
        Vector an_assignment = new Vector(size());
        Vector assignments = new Vector(size());
        Vector temp  = new Vector(size());
        Vector temp2;
        int i,j;
        Statement retval = new Statement();
        int variableposition;
        boolean flag,invalid;
        int location = 0;
        Mapping current_mapping;
        Vector unique_vars = findUniqueVariables();
        int num_unique_vars = unique_vars.size();
        Predicate apred;
        Predicate atuple;
        String this_var;
        for (i = 0; i < num_unique_vars; i++){
            an_assignment.addElement(new Mapping((String)unique_vars.elementAt(i)," "));
        }
        assignments.addElement(an_assignment);
        for (first();!isDone();next()){
            //loop over each of the Predicates
            apred = currentSubgoal();
            temp = new Vector(5);
            for (i = 0; i < assignments.size();i++){
                an_assignment = (Vector) assignments.elementAt(i);
                for (tuples.first();
                !tuples.isDone();tuples.next()){
                    atuple = tuples.currentSubgoal();
                    //loop over each assignment;
                    if (atuple.function.equals(apred.function)){
                        //at this point, we know that the functions
                        //are the same; now see if it's consistent
                        Vector newAssign = copyAssignments(an_assignment);
                        for (invalid = false,apred.first(); invalid == false && !apred.isDone();apred.next()){
                      
                            this_var = apred.current();
                            variableposition = apred.getCurrentLocation();
                            //now find the mapping value...
                            for(j = 0, flag = false; 
                            flag == false && j < an_assignment.size();j++){
                                current_mapping = (Mapping)an_assignment.elementAt(j);
                                if (current_mapping.variable.equals(this_var)){
                                    if (current_mapping.mapping.equals(" ")){
                                        //then we've hit a blank, and should add it
                                        flag = true;
                                        ((Mapping)newAssign.elementAt(j)).mapping = atuple.variableI(variableposition);
                                    }
                                    else if (!current_mapping.mapping.equals(atuple.variableI(variableposition))){
                                        flag = true;
                                        invalid = true;
                                    }
                                }//end if current_mapping == this var
                            }//end of finding the mapping value
                        }//end for looking over this Predicate
                        //at this point, if all of the variables match, then we want to copy it
                        //over
                        if (invalid == false){
                            temp.addElement(newAssign);
                        }
                    }//end of if the function head is the same
                }//end of looping over tuples
            }//end of looping over assignments
            
            assignments = temp;
            
        }//end of looping over Predicates
        //now we have all the assignments; we want to 
        //1. make sure that we are only returning those for which there was a
        //complete mapping (meaning those where we really did cover everything)
        //2. only return the answers to queries
        temp = new Vector(5);
        for (i = 0; i < assignments.size();i++){
            an_assignment = (Vector) assignments.elementAt(i);
            for (invalid = false, j = 0;
            j < an_assignment.size() && invalid == false;
            j++){
                //make sure there exists a mapping for everything
                if (((Mapping)an_assignment.elementAt(j)).mapping.equals(" ")){
                    invalid = true;
                }
            }
            if (!invalid){
                temp.addElement(an_assignment);
            }
        }
        assignments = new Vector();
        for (i = 0; i < temp.size();i++){
            //this part appears to be adding in mappings for all things in temp2
            //it should be able to benefit from the new version of predicate()
            temp2 = (Vector) temp.elementAt(i);
            apred = new Predicate(temp2.size());
            apred.setFunctionHead(new String (head.getFunctionHead()));
            for (head.first();!head.isDone();head.next()){
                //find the variable
                this_var = head.current();
                for (j = 0, flag = false;
                j < temp2.size() && flag == false;
                j++){
                    if (((Mapping)temp2.elementAt(j)).variable.equals(this_var)){
                        location = j;
                        flag = true;
                    }
                }//end for going over temp
                if (flag == false){
                    System.out.println("Error: head variables must be included in the body");
                }
                else {
                    apred.addVariable(new String (((Mapping)temp2.elementAt(location)).mapping));
                }
            }// end for each Predicate
            retval.addSubgoal(apred);
        }//end of each assignment
                
        return retval;
    }//end answersToQuery
        
    
    public boolean read(String values){
         head = new Predicate();
        body = new Predicate[_max_size];
        String substr,substr2;
        substr = new String();
        substr2 = new String();
        int index,index2;
        boolean retval;
        substr = values.trim();
        index = substr.indexOf(")");
        substr2 = substr.substring(0,index + 1);//get the )
        if (head.read(substr2)== false){
            return false;
        }
        substr = substr.substring(index+1).trim();
        index = substr.indexOf(":-");
        if (index != 0){ 
            //we need this at the front...
            return false;
        }
        //at this point, we know that we had :- in the right place
        substr = substr.substring(index+2).trim();
        index = substr.indexOf(")");
        while(index > -1){
            substr2 = substr.substring(0,index+1).trim();
            substr = substr.substring(index+1);
            index2 = substr.indexOf(",");
            if (index2 >-1){
                substr= substr.substring(index2+1);
            }
            index = substr.indexOf(")");
            Predicate apred = new Predicate();
            retval = apred.read(substr2);
            if (retval == false){
                System.out.println("Predicate not read properly");
                return false;  
            }
            addSubgoal(apred);
        } //end of while for getting all of the subgoals
        return true;
    }//end of read
    
    public Vector copyAssignments(Vector original){
        int i;
        Vector retval = new Vector(original.size());
        for (i = 0; i < original.size();i++){
            retval.addElement(new Mapping((Mapping)original.elementAt(i)));
        }
        return retval;
    }
    
    public static void main (String args[])
    {
       long a_time;
       int i;
       Statement q= new Statement();
       Statement v = new Statement();
       Statement q2 = new Statement();
       Statement q3 = new Statement();
       
       //String container;
       //String contained;
       //boolean wasContainedIn;
       Vector returned;
       q.read("q(a,b):-e1(a),e2(b)");
       v.read("q(x,y):-e1(x),e(y)");
       q2.read("q(r,s):-e1(r),e1(s),e1(t),e2(r),e2(s),e2(t),e1(t),e4(t)");
       q3.read("q(r,s):-e1(r),e1(s),e1(t),e2(r),e2(s),e3(t)");
	   q3.print();
	   System.out.print(" ordered by ");
	   q2.print();
	   
    }


}