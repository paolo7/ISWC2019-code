package logic;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;

public class RuleImpl extends RuleAbstr{
	
	Set<PredicateInstantiation> antecedent;
	Set<PredicateTemplate> consequent;
	boolean createsNewPredicate;
	List<TextTemplate> label;

	
	public RuleImpl(Set<PredicateInstantiation> antecedent, Set<PredicateTemplate> consequent) {
		this.antecedent = antecedent;
		this.consequent = consequent;
		this.label = null;
		createsNewPredicate = false;
	}
	
	public RuleImpl(Set<PredicateInstantiation> antecedent, Set<PredicateTemplate> consequent, List<TextTemplate> label) {
		this.antecedent = antecedent;
		this.consequent = consequent;
		this.label = label;
		createsNewPredicate = true;
	}

	@Override
	public Set<PredicateInstantiation> getAntecedent() {
		return antecedent;
	}

	@Override
	public Set<PredicateTemplate> getConsequent() {
		return consequent;
	}

	@Override
	public boolean createsNewPredicate() {
		return createsNewPredicate;
	}

	@Override
	public List<TextTemplate> getLabel() {
		return label;
	}
	
	private Set<ConversionTriple> computeConstraints(Map<String, RDFNode> bindingsMap, Set<Predicate> predicates, Binding[] newBindings){
		Set<ConversionTriple> constraints = new HashSet<ConversionTriple>();
		for(PredicateInstantiation pi: getAntecedent()) {
			Set<ConversionTriple> newConstraints = pi.applyBinding(bindingsMap, newBindings).getLeft();
			if(newConstraints != null) constraints.addAll(newConstraints);
		}
		return constraints;
	}

	@Override
	public Set<PredicateInstantiation> applyRule(Map<String, RDFNode> bindingsMap, Set<Predicate> predicates, Set<PredicateInstantiation> existingPredicates) {
		Set<PredicateInstantiation> newpredicates = new HashSet<PredicateInstantiation>();
		for(PredicateTemplate pt: consequent) {
			Set<ConversionTriple> constraints = computeConstraints(bindingsMap, predicates, pt.getBindings());
			// try to get constraints from the available predicate instantiations
			Set<ConversionTriple> importedConstraints = null;
			int compatiblenum = 0;
			for(PredicateInstantiation pi : this.getAntecedent()) {
				for(PredicateInstantiation piExisting : existingPredicates) {
					if(pi.compatible(piExisting, bindingsMap)) {					
						compatiblenum++;
						importedConstraints = piExisting.getAdditionalConstraints(pi.getBindings());
					}
				}
				if (compatiblenum == 1) constraints.addAll(importedConstraints);
				//if (compatiblenum == 2) System.out.println("Warning. Multiple predicate instantiations found for the antecedents of a rule. OWL-consistency not guaranteed.");
			}
			if(RDFUtil.ignoreConstraints) constraints = new HashSet<ConversionTriple>();
			newpredicates.add(pt.applyRule(bindingsMap, predicates, label, antecedent, constraints));
		}
		return newpredicates;
	}

	@Override
	public Set<PredicateInstantiation> applyRule(Map<String, RDFNode> bindingsMap, Set<Integer> deltas, Set<Predicate> predicates, Set<PredicateInstantiation> existingPredicates) {
		Set<PredicateInstantiation> newpredicates = new HashSet<PredicateInstantiation>();
		for(PredicateTemplate pt: consequent) {
			Set<ConversionTriple> constraints = computeConstraints(bindingsMap, predicates, pt.getBindings());
			newpredicates.add(pt.applyRule(bindingsMap, deltas, predicates, label, antecedent, constraints));
		}
		return newpredicates;
	}
	
}
