package logic;

public abstract class BindingAbstr implements Binding{

	@Override
    public boolean equals(Object o) {
  
        if (o == this) {
            return true;
        }
        if (!(o instanceof Binding)) {
            return false;
        }
        Binding p = (Binding) o;
        if(this.isVar() != p.isVar()) return false;
        if(this.isVar()) {
        	if(this.getVar().equals(p.getVar())) return true;
        } else {
        	if(this.getConstant().equals(p.getConstant())) return true;
        }
        return false;
    }
	
	@Override
	public String toString() {
		if(this.isVar()) return this.getVar().toString();
		else return this.getConstant().toString();
	}
	
	@Override
	public String prettyPrint() {
		if(this.isVar()) return "?v"+this.getVar().prettyPrint();
		else return this.getConstant().toString();
	}
	
	@Override
	public int hashCode() {
		Resource resource = null;
		Variable var = null;
		if(this.isConstant()) resource = this.getConstant();
		else var = this.getVar();
		final int prime = 31;
		int result = 7;
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		result = prime * result + ((var == null) ? 0 : var.hashCode());
		return result;
	}
	
}
