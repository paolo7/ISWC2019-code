package logic;

public interface Variable {

	public int getVarNum();
	public boolean isSimpleVar();
	public boolean areLiteralsAllowed();
	public String prettyPrint();
	
}
