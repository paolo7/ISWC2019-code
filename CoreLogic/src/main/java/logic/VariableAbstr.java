package logic;

public abstract class VariableAbstr implements Variable {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + (isSimpleVar() || areLiteralsAllowed() ? 1231 : 1237);
		result = prime * result + (isSimpleVar() ? 1231 : 1237);
		result = prime * result + getVarNum();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		VariableImpl other = (VariableImpl) obj;
		if ((isSimpleVar() || areLiteralsAllowed()) != (other.isSimpleVar() || other.areLiteralsAllowed()))
			return false;
		if (isSimpleVar() != other.isSimpleVar())
			return false;
		if (getVarNum() != other.getVarNum())
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return getVarNum()+"";
		//if(areLiteralsAllowed()) return "?v"+getVarNum()+"'";
		//else return "?v"+getVarNum();
	}
	
	@Override
	public String prettyPrint() {
		if(isSimpleVar()) return ""+getVarNum();
		if(areLiteralsAllowed()) return ""+getVarNum()+"+";
		else return ""+getVarNum()+"-";
	}
	
}
