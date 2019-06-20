package core;

public class Variable implements Element{
	
	private static long uniqueVarID = 0;
	private boolean noLit;
	
	public boolean isNoLit() {
		return noLit;
	}

	public Variable(boolean noLit) {
		this.noLit = noLit;
	}
	
	@Override
	public String toString() {
		return "?v" + (noLit ? "-" : "+");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (noLit ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Variable other = (Variable) obj;
		if (noLit != other.noLit)
			return false;
		return true;
	}

	@Override
	public String toSHACLconstraint() {
		return null;
	}

	@Override
	public String toSPARQLelement() {
		return "?v"+(uniqueVarID++);
	}

	@Override
	public void enforceNoLiteral() {
		noLit = true;
	}
	


	
	
}
