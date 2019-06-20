package logic;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;

public interface Rule {

	public Set<PredicateInstantiation> getAntecedent();
	public Set<PredicateTemplate> getConsequent();
	public boolean createsNewPredicate();
	public List<TextTemplate> getLabel();
	//to remove?
	public String getAntecedentSPARQL();
	public String getGPPGAntecedentSPARQL();
	public String getGPPGAntecedentSPARQL(int variant);
	public Set<PredicateInstantiation> applyRule(Map<String,RDFNode> bindingsMap, Set<Predicate> predicates, Set<PredicateInstantiation> existingPredicates);
	public Set<PredicateInstantiation> applyRule(Map<String,RDFNode> bindingsMap, Set<Integer> deltas, Set<Predicate> predicates, Set<PredicateInstantiation> existingPredicates);
	public Set<Integer> getNoLitVariables();
	public Set<Integer> getAllVariables();
	public Binding[] getNewPredicateBasicBindings();
}
