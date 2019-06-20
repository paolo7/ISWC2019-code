package datagenerator;
import java.util.Random;

public class RegularRandomVariableGenerator extends RandomVariableGenerator{
    int _average;
    int _range;
    public RegularRandomVariableGenerator(int start, int stop){
        _start = start;// - (int).5 * arange; //note, the point here is that
        //we want the variables to be started half way, or they
        //will have different behavior than the normal ones, which
        //start halfway, which would be bad...
        _stop = stop; 
        if (_stop < _start){
            System.out.println("In RegularRandomVariableGenerator; can't have a start value greater");
            System.out.println("than the stop value; using start plus 5");
            _stop = _start + 5;
        }
        //_average = (_stop + start) / 2;    
        _range = _stop - _start;
        random = new Random(_seed);//let's try giving
		_seed++;
		//it a random seed; this should make it more random, unless the 
		//init
        generatorType = "Regular";
	}//end of int, int constructor
    
    public String getRandomVariable(){
        //int astart = _;
        //int arange = _range;
        int retval = _start;
        double arand = random.nextDouble();
        retval +=  (int)(_range * arand);
       
        return (new Integer(retval)).toString();
        
    }//end of getRandomVariable
}//end of class RegularRandomVariableGenerator