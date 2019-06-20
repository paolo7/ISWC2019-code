package logic;

import java.util.Arrays;

public abstract class PredicateTemplateAbstr implements PredicateTemplate{

	@Override
    public boolean equals(Object o) {
  
        if (o == this) {
            return true;
        }
        if (!(o instanceof PredicateTemplate)) {
            return false;
        }
        PredicateTemplate p = (PredicateTemplate) o;
        if(! this.getName().equals(p.getName()) || this.getBindings().length != p.getBindings().length)
        	return false;
        for(int i = 0; i < this.getBindings().length; i++)
        	if(!this.getBindings()[i].equals(p.getBindings()[i])) return false;
        return true;
    }
	
	@Override
	public String toString() {
		String s = "";
		for(TextTemplate tt : this.getName())
			s += tt.toString();
		s += "(";
		boolean first = true;
		for(int i = 0; i < this.getBindings().length; i++) {
			if(first) first = false;
			else s += ", ";
			s += this.getBindings()[i].prettyPrint();
		}
		return s+")";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + Arrays.hashCode(this.getBindings());
		result = prime * result + ((this.getName() == null) ? 0 : this.getName().hashCode());
		return result;
	}
	
}
