package logic;

public class BindingImpl extends BindingAbstr {

	private Variable var;
	private Resource resource;
	
	public BindingImpl(Resource resource) {
		this.var = null;
		this.resource = resource;
	}
	public BindingImpl(Variable var) {
		this.var = var;
		this.resource = null;
	}
	
	@Override
	public boolean isVar() {
		return var != null;
	}

	@Override
	public boolean isConstant() {
		return !isVar();
	}

	@Override
	public Variable getVar() {
		if(!isVar()) throw new RuntimeException("ERROR: Attempted to read a variable binding from a binding to a constant");
		return var;
	}

	@Override
	public Resource getConstant() {
		if(isVar()) throw new RuntimeException("ERROR: Attempted to read a constant binding from a binding to a variable");
		return resource;
	}

}
