package logic;

import java.util.List;
import java.util.Set;

/**
 * Each implementation should reuse the implementation of equals specified by PredicateAbstr.
 * @author paolo
 *
 */
public interface Predicate {

	public String getName();
	public int getVarnum();
	public Set<ConversionTriple> getRDFtranslation();
	public Set<ConversionFilter> getRDFtranslationFilters();
	public String toSPARQL();
	public List<TextTemplate> getTextLabel();
}
