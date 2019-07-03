package shacl;

import java.util.HashSet;
import java.util.Set;

import core.Triple_Pattern;

public class Existential_Constraint {

	private Set<Triple_Pattern> antecedent;
	private Set<Triple_Pattern> consequent;
	
	public Set<Triple_Pattern> getAntecedent() {
		return antecedent;
	}
	
	public Triple_Pattern getSingletonAntecedent() {
		if(antecedent.size() != 1) throw new RuntimeException("ERROR, this existential constraint's antecedent is not singleton, but a singleton one was requested.");
		return antecedent.iterator().next();
	}

	public Set<Triple_Pattern> getConsequent() {
		return consequent;
	}

	public Existential_Constraint() {
		this.antecedent = new HashSet<Triple_Pattern>();
		this.consequent = new HashSet<Triple_Pattern>();
	}
	
	public Existential_Constraint(Set<Triple_Pattern> antecedent, Set<Triple_Pattern> consequent) {
		this.antecedent = antecedent;
		this.consequent = consequent;
	}
	public Existential_Constraint(Set<Triple_Pattern> antecedent, Triple_Pattern consequent) {
		this.antecedent = antecedent;
		Set<Triple_Pattern> consequentset = new HashSet<Triple_Pattern>();
		consequentset.add(consequent);
		this.consequent = consequentset;
	}
	public Existential_Constraint(Triple_Pattern antecedent, Triple_Pattern consequent) {
		Set<Triple_Pattern> antecedentset = new HashSet<Triple_Pattern>();
		antecedentset.add(antecedent);
		this.antecedent = antecedentset;
		Set<Triple_Pattern> consequentset = new HashSet<Triple_Pattern>();
		consequentset.add(consequent);
		this.consequent = consequentset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((antecedent == null) ? 0 : antecedent.hashCode());
		result = prime * result + ((consequent == null) ? 0 : consequent.hashCode());
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
		Existential_Constraint other = (Existential_Constraint) obj;
		if (antecedent == null) {
			if (other.antecedent != null)
				return false;
		} else if (!antecedent.equals(other.antecedent))
			return false;
		if (consequent == null) {
			if (other.consequent != null)
				return false;
		} else if (!consequent.equals(other.consequent))
			return false;
		return true;
	}

	public Existential_Constraint newWithExtendedAntecedent(Triple_Pattern tp) {
		return newWithExtendedAntecedentHelper(tp,true);
	}
	public Existential_Constraint newWithExtendedConsequent(Triple_Pattern tp) {
		return newWithExtendedAntecedentHelper(tp,false);
	}
	private Existential_Constraint newWithExtendedAntecedentHelper(Triple_Pattern tp, boolean toAntecedent) {
		Set<Triple_Pattern> antecedent = new HashSet<Triple_Pattern>();
		Set<Triple_Pattern> consequent = new HashSet<Triple_Pattern>();
		// clone the previous existential constraint
		for(Triple_Pattern tp1: this.antecedent) {antecedent.add(tp1);}
		for(Triple_Pattern tp2: this.consequent) {consequent.add(tp2);}
		if(toAntecedent) antecedent.add(tp); else consequent.add(tp);
		return new Existential_Constraint(antecedent, consequent);
	}
	
	@Override
	public String toString() {
		return antecedent.toString().replaceAll("\\+","").replaceAll("-","") + " ==> "
				+consequent.toString().replaceAll("\\+","").replaceAll("-","");
	}
	
	public String toSPARQL_antecedent() {
		String query = "SELECT * WHERE {\n";
		for(Triple_Pattern tp : antecedent) {
			query += tp.getSubject().toSPARQLelement() + " " + tp.getPredicate().toSPARQLelement() + " " + tp.getObject().toSPARQLelement() + " . \n";
		}
		return query+"}";
	}
}
