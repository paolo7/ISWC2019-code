package datagenerator;

public class Mapping{
    public String variable;
    String mapping;
    
    public Mapping(){
        variable = new String();
        mapping = new String();
}
    
    public Mapping(Mapping amap){
        variable = new String (amap.variable);
		if (amap.mapping == null){
			mapping = null;
		}
		else{
			mapping = new String (amap.mapping);
		}
    }
    
    public Mapping(String newvar,String amap){
        variable = new String(newvar);
		if (amap == null){
			mapping = null;
		}
		else{
			mapping = new String(amap);
		}
    }
    
    public void print(){
        System.out.print(printString());
    }
    
    public StringBuffer printString(){
        StringBuffer retval = new StringBuffer();
        retval.append(variable);
        retval.append("->");
        retval.append(mapping);
        return retval;
    }
	
	public Object clone(){
		return new Mapping(variable,mapping);
	}
}