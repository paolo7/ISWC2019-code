package core;

import java.util.Map;
import java.util.Set;

public class Rule {

	private Set<Triple_Pattern> antecedent;
	private Triple_Pattern consequent;
	
	public Set<Triple_Pattern> getAntecedent() {
		return antecedent;
	}

	public Triple_Pattern getConsequent() {
		return consequent;
	}

	
	public Rule(Set<Triple_Pattern> antecedent, Triple_Pattern consequent) {
		this.antecedent = antecedent;
		this.consequent = consequent;
	}
	
	@Override
	public String toString() {
		return antecedent+" ==> "+consequent;
	}
	
}
