package datagenerator;
import java.util.Random;
public abstract class RandomVariableGenerator  {

    int _start;
    int _stop;
	protected static long _seed = System.currentTimeMillis();
    String generatorType;
    Random random;
    
    public String getType(){
        return generatorType;
    }
    
    public void setStop(int avg){
        _stop = avg;
    }
    
    public void setStart(int st){
        _start = st;
    }
    public int getStop(){
        return _stop;
    }
    public int getStart(){
        return _start;
    }
    
    public abstract String getRandomVariable();
}
