package logic;

public interface Binding {

	public boolean isVar();
	public boolean isConstant();
	public Variable getVar();
	public Resource getConstant();
	public String prettyPrint();
}
