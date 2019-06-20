package core;

public interface Element {

	public String toSHACLconstraint();
	public String toSPARQLelement();
	public boolean equals(Object obj);
	public int hashCode();
	public void enforceNoLiteral();
}
