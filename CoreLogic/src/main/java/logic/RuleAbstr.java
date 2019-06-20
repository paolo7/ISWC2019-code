package logic;

import java.util.HashSet;
import java.util.Set;

public abstract class RuleAbstr implements Rule{

	@Override
	public String getAntecedentSPARQL() {
		String SPARQL = "SELECT DISTINCT *";
		
		/*// get the SELECT variables
		
		Set<Integer> selectVars = new HashSet<Integer>();
		for(PredicateTemplate ep : this.getConsequent()) {
			for(int i = 0; i < ep.getBindings().length; i++) {
				if(ep.getBindings()[i].isVar())
					selectVars.add(new Integer(ep.getBindings()[i].getVar()));
			}
			for(TextTemplate tt : ep.getName()) {
				if(tt.isVar()) selectVars.add(new Integer(tt.getVar()));
			}
		}
		boolean first = true;
		for(Integer i: selectVars) {
			if (first) first = false;
			else SPARQL += " ";
			SPARQL += "?v"+i;
		}*/
		SPARQL += "\nWHERE {\n";
		//  compute the WHERE clause
		for(PredicateInstantiation ip : this.getAntecedent()) {
			SPARQL += ip.toSPARQL();
		}
		return SPARQL+"}";
	}
	
	@Override
	public Set<Integer> getNoLitVariables(){
		Set<Integer> noLitVars = new HashSet<Integer>();
		for(PredicateInstantiation ep : this.getAntecedent()) {
			noLitVars.addAll(ep.getNoLitVariables());
		}
		return noLitVars;
	}
	
	@Override
	public Set<Integer> getAllVariables(){
		Set<Integer> noLitVars = new HashSet<Integer>();
		for(PredicateInstantiation ep : this.getAntecedent()) {
			noLitVars.addAll(ep.getVariables());
		}
		return noLitVars;
	}
	
	@Override
	public String getGPPGAntecedentSPARQL() {
		return getGPPGAntecedentSPARQL(0);
	}
	
	@Override
	public String getGPPGAntecedentSPARQL(int variant) {
		Set<Integer> vars = this.getAllVariables();
		String SPARQL = "SELECT DISTINCT *";
		/*boolean first = true;
		for(Integer i : vars) {
			//if(!first)SPARQL += ", ";
			SPARQL += "?v"+i;
			first = false;
		}
		if(vars.size() == 0 ) SPARQL += " * ";*/
		SPARQL = SPARQL + " WHERE {\n";
		for(PredicateInstantiation ep : this.getAntecedent()) {
			SPARQL += ep.toGPPGSPARQL(variant);
		}
		return SPARQL + "}";
	}
	
	@Override
    public boolean equals(Object o) {
  
        if (o == this) {
            return true;
        }
        if (!(o instanceof Rule)) {
            return false;
        }
        Rule p = (Rule) o;
        return 
        		this.getAntecedent().equals(p.getAntecedent()) && 
        		this.getConsequent().equals(p.getConsequent()) &&
        		this.createsNewPredicate() == p.createsNewPredicate() && 
        		(!this.createsNewPredicate() || this.getLabel().equals(p.getLabel()))
        	;
    }
	
	@Override
	public String toString() {
		String s = "RULE   : ";
		boolean first = true;
		for(PredicateTemplate p : this.getConsequent()) {
			if(first) first = false;
			else s += " AND ";
			s += p;
		}
		s += " <== ";
		first = true;
		for(PredicateInstantiation p : this.getAntecedent()) {
			if(first) first = false;
			else s += " AND ";
			s += p;
		}
		first = true;
		if(this.createsNewPredicate()) {
			s += "\nLABEL  : ";
			for(TextTemplate t : this.getLabel()) {
				if(first) first = false;
				else s += " ";
				s += t;
			}
		}
		
		return s+"\n";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + ((this.getAntecedent() == null) ? 0 : this.getAntecedent().hashCode());
		result = prime * result + ((this.getConsequent() == null) ? 0 : this.getConsequent().hashCode());
		result = prime * result + (this.createsNewPredicate() ? 1231 : 1237);
		result = prime * result + ((this.getLabel() == null) ? 0 : this.getLabel().hashCode());
		return result;
	}
	
	public Binding[] getNewPredicateBasicBindings() {
		PredicateTemplate pt = this.getConsequent().iterator().next();
		return pt.getBindings();
	}
	
}
