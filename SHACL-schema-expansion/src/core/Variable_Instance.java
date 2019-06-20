package core;

public class Variable_Instance extends Variable {


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + index;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Variable_Instance other = (Variable_Instance) obj;
		if (index != other.index)
			return false;
		return true;
	}

	private int index;
	public Variable_Instance(boolean noLit, int index) {
		super(noLit);
		this.index = index;
	}
	public int getIndex() {
		return index;
	}
	@Override
	public String toString() {
		return "?v"+index + (isNoLit() ? "-" : "+");
	}
	public String toSPARQLResultelement() {
		return "v"+index;
	}
	
	@Override
	public String toSPARQLelement() {
		return "?v"+index;
	}
	
}
