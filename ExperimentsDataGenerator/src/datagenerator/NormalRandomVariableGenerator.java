package datagenerator;
import java.util.Random;

public class NormalRandomVariableGenerator extends RandomVariableGenerator{
    // note that in this case variation is really what
    //determines how different your variables are, and
    //mean detemines where they start.  I really should
    //come up with a new name for them.
    
    int _range;
    public NormalRandomVariableGenerator(int start, int stop){
        _start = start;
        _stop = stop;
        if (_stop < _start){
            System.out.println("In NormalRandomVariableGenerator, can't have the start value greater");
            System.out.println("than the start value; using the start value plus 5");
            _stop = _start + 5;
        }
        
        _range = (stop - start);
        random = new Random(_seed);
		_seed++;
        generatorType = "Normal";
	}
   public String getRandomVariable(){
        //returns a new random variable based on normal 
        //distribution.
        //the parameters used are those of start, variation, and mean
        //come to think of it, if I just have my variables 
        //be numbers, I don't have to worry about that silly 
        //wrapping crap.  Cool...
        int retval = _start;
        retval += (int)(_range * random.nextGaussian());
        return (new Integer(retval)).toString();
   }
}