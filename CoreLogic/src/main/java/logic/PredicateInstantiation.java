package logic;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Each implementation should reuse the implementation of equals specified by PredicateInstantiationAbstr.
 * @author paolo
 *
 */
public interface PredicateInstantiation {

	public Predicate getPredicate();
	public boolean hasVariables();
	public Binding[] getBindings();
	public Binding getBinding(int index);
	public String toSPARQL();
	public String toSPARQL_INSERT(String baseBlank);
	public String toGPPGSPARQL();
	public String toGPPGSPARQL(int variant);
	public Pair<Set<ConversionTriple>,Set<ConversionFilter>> applyBinding(Map<String, RDFNode> bindingsMap, Binding[] newSignatureBindings);
	public Set<ConversionTriple> getAdditionalConstraints(Binding[] bindings);
	public Set<ConversionTriple> getAdditionalConstraints();
	public Set<Integer> getNoLitVariables();
	public Set<Integer> getVariables();
	public boolean compatible(PredicateInstantiation other, Map<String, RDFNode> bindingsMap);
}
