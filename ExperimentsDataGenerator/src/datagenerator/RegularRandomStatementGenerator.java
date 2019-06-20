package datagenerator;
import java.util.Random;
public class RegularRandomStatementGenerator extends RandomStatementGenerator{ 
    //this class is to return a random query based on parameters
    //passed in.  It is going to do everything as a normal variable...
    //at least at the moment; I should probably subclass the whole
    //variable thing
    //at any rate, it can give you a random query (or view, whatever
    //floats your boat), hey I should rename this; also need
    //to think about what to do about different starting spots for
    //variables... hmmm... That could be fixed by giving an 
    //input on where to start naming things, but it really needs
    //to be thought out.  Anyway, you get random size, and random
    //variables and functions.
    public int sizeMean;
    int sizeDeviation;
    Random random;
    int variableMean;
    int variableDeviation;
    int _fun_start;
    int _fun_stop;
    int _range;
    int functionLength;
    //int maxNumberOfPredicateDuplicates;
    RandomVariableGenerator functionGenerator;
    RandomVariableGenerator variableGenerator;
    
    public RegularRandomStatementGenerator(int fun_start, int fun_stop,int funlen,int numvarvals){
            //sizeMean = avg; 
            //maxNumberOfPredicateDuplicates = maxdup;
            //sizeDeviation = dev;
            random = new Random();
            sizeMean = funlen;
            _fun_start = fun_start;
            _fun_stop = fun_stop;
            functionLength = numvarvals;
            functionGenerator = null;
            variableGenerator = null;
            if (_fun_start > _fun_stop){
                System.out.println("can't have a function starter more than the stopper");
                System.out.println("using the start value plus 5");
                _fun_stop = _fun_start + 5;
            }
            setVariableGenerator("Regular",0,numvarvals);
            setFunctionGenerator("Regular",_fun_start,_fun_stop);
	}//end NormalRandomStatementGenerator(int,int)
    
    public boolean setVariableGenerator(String type, int start, int stop){
        if (stop < start || type == null){
            return false;
        }
        else if (type.equals("Regular")){
            variableGenerator = new RegularRandomVariableGenerator(start,stop);
            return true;
        }
        else if (type.equals("Normal")){
            variableGenerator = new NormalRandomVariableGenerator(start,stop);
            return true;
        }
        else { // unknown type of varible generator desired
            variableGenerator = new RegularRandomVariableGenerator(start,stop);
            System.out.println("no valid type recognized for randomVariable Generator");
            System.out.println("using Regular");
            return true;
        }
    }//end setVariableGenerator
     public boolean setFunctionGenerator(String type, int start, int stop){
        if (stop < start || type == null){
            return false;
        }
        else if (type.equals("Regular")){
            functionGenerator = new RegularRandomVariableGenerator(start,stop);
            return true;
        }
        else if (type.equals("Normal")){
            functionGenerator = new RegularRandomVariableGenerator(start,stop);
            return true;
        }
        else { // unknown type of varible generator desired
            functionGenerator = new RegularRandomVariableGenerator(start,stop);
            System.out.println("no valid type recognized for randomVariable Generator");
            System.out.println("using Regular");
            return true;
        }
    }//end setFunctionGenerator
           
        
    
    private int selectSize(){
        //this gives you back a new size for a statement
        //based on normal distribution on the averageSize
        //and deviationSize currently in use; I use the 
        //java rounding rather than picking my own.  values
        //less than one are rounded up to one.
        double rand = random.nextGaussian();
        int retval = sizeMean;
        retval += (int) (rand * sizeDeviation);
        if (retval < 1) {
            retval = 1;
        }
        return retval;
    }//end int selectSize(void)
    
 public Statement getRandomStatement(boolean use_all){
     //this function returns a random statement based on the
     //values for the mean and deviation of the 
     //size of the query, and the mean and deviation of the 
     //num of variables and the number of function heads.
     //note; need to think about this more carefully for 
     //variables and function heads.
     Statement retval = new Statement();
     int i,j;
     int length;
     int fcnlength;
     String temp;
     Predicate apred;
     length = selectSize();
     if (functionGenerator == null || variableGenerator == null){
        System.out.println("error, you must tell the generators before generating");
        return null;
     }
     for (i = 0; i < length; i++){
        //now we have to create the individual clauses.... oops; need
        //a parameter for the length of the function... not to 
        //mention a way of keeping straight which ones are of which length...
        //hmmm... I can always append the length to the function name, yeah,
        //that'll work.
        fcnlength = (int)(functionLength * random.nextFloat());
        if (fcnlength < 1){
            fcnlength = 1;
        }
        apred = new Predicate();
        temp= padHead(functionGenerator.getRandomVariable(),fcnlength);
        //Vector a_vec;
  //      a_vec = retval.predicatesThatContainFunction(temp);
 //       if (a_vec.size() >= maxNumberOfPredicateDuplicates){
 //           i--;
  //      }
  //      else {
            apred.setFunctionHead(temp);
            for (j = 0; j < fcnlength; j++){
                apred.addVariable(variableGenerator.getRandomVariable());
            }
            if (!retval.containsSubgoal(apred)){
                //we don't want duplicate subgoals
                retval.addSubgoal(apred);
            } 
            else
            {
                //otherwise we need to generate another subgoal, so 
                //decrease the counter
            i--;
            }
     //   }//end else
        }//end for
        fcnlength = (int)(functionLength * random.nextFloat());
        if (fcnlength < 1){
            fcnlength = 1;
        }
        apred = new Predicate();
        temp = functionGenerator.getRandomVariable();
        if (fcnlength < 10){
            temp = temp + "0";
        }
        if (fcnlength < 100){
            temp = temp + "0";
        }
        temp = temp + (new Integer(fcnlength)).toString();
        apred.setFunctionHead(temp);
        for (j = 0; j < fcnlength; j++){
            apred.addVariable(variableGenerator.getRandomVariable());
        }
       generateHead(retval,use_all);

     return retval;
   }
   	public static void main(String [] args){//		  RegularRandomStatementGenerator bob = new RegularRandomStatementGenerator(1,5,10,30);////		  DuplicateCountRandomStatementGenerator bob = new DuplicateCountRandomStatementGenerator(1,15,3,5,4,4,20);////		  Statement bobbob = bob.getRandomStatement(false);//		  bobbob.print();//		 // bobbob.printString();////		  System.out.println("done");//		  //		  NormalRandomStatementGenerator bob1 = new NormalRandomStatementGenerator(1,5,10,30);////		  DuplicateCountRandomStatementGenerator bob = new DuplicateCountRandomStatementGenerator(1,15,3,5,4,4,20);////		  Statement bobbob1 = bob1.getRandomStatement(false);//		  bobbob1.print();//		 // bobbob.printString();////		  System.out.println("done");		  		  ChainRandomStatementGenerator bob1 = new ChainRandomStatementGenerator(1, 20, 8, 4, 10, 5);//		  DuplicateCountRandomStatementGenerator bob = new DuplicateCountRandomStatementGenerator(1,15,3,5,4,4,20);		  Statement bobbob1 = bob1.getRandomStatement(false);		  bobbob1.print();		 // bobbob.printString();	}
        
   
}
