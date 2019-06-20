package logic;

import java.util.Set;

public interface ConversionTriple {

	public Binding get(int i);
	public Binding getSubject();
	public Binding getPredicate();
	public Binding getObject();
	public String toGPPGSPARQL(Binding[] bindings);
	public String toGPPGSPARQL(int variant, Set<Integer> noLitVars, Binding[] bindings);
	public String toSPARQL(Binding[] bindings, String freshVarPrefix);
	public String toSPARQL();
	public String toSPARQL_INSERT(Binding[] bindings, String baseBlank);
	/**
	 * Get all the variables that cannot be literals
	 * @return
	 */
	public Set<Integer> getNoLitVariables();
	/**
	 * Get all the variables that cannot be literals, after applying a set of bindings
	 * @param bindings
	 * @return
	 */
	public Set<Integer> getNoLitVariables(Binding[] bindings);
	public Set<Integer> getVariables(Binding[] bindings);
	public ConversionTriple applyBinding(Binding[] bindings);
}
