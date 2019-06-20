package logic;

import java.util.List;
import java.util.Set;

public class PredicateImpl extends PredicateAbstr {

	private String name;
	private int varnum;
	
	private Set<ConversionTriple> translationToRDF;
	private Set<ConversionFilter> translationToRDFFilters;
	private List<TextTemplate> textLabel;
	
	public PredicateImpl(String name, int varnum, Set<ConversionTriple> translationToRDF, Set<ConversionFilter> translationToRDFFilters, List<TextTemplate> textLabel) {
		if(name == null)
			throw new RuntimeException("ERROR: trying to instantiate a predicate without a name");
		if(translationToRDF == null && translationToRDFFilters == null)
			throw new RuntimeException("ERROR: trying to instantiate a predicate without an RDF translation");	
		if(textLabel == null)
			throw new RuntimeException("ERROR: trying to instantiate a predicate without a textual label");	
		if(translationToRDF != null && translationToRDF.size() == 0 && translationToRDFFilters == null)
			throw new RuntimeException("ERROR: trying to instantiate a predicate with empty RDF translation");	
		//if(translationToRDFFilters.size() == 0 && translationToRDF == null)
		//	throw new RuntimeException("ERROR: trying to instantiate a predicate with empty textual label");	
		if(varnum < 0) 
			throw new RuntimeException("ERROR: trying to instantiate a predicate with less than one variable");
		this.name = name;
		this.varnum = varnum;
		this.translationToRDF = translationToRDF;
		this.textLabel = textLabel;
		this.translationToRDFFilters = translationToRDFFilters;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getVarnum() {
		return varnum;
	}

	@Override
	public Set<ConversionTriple> getRDFtranslation() {
		return translationToRDF;
	}
	
	@Override
	public Set<ConversionFilter> getRDFtranslationFilters() {
		return translationToRDFFilters;
	}

	@Override
	public List<TextTemplate> getTextLabel() {
		return textLabel;
	}

/*	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PredicateImpl other = (PredicateImpl) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (textLabel == null) {
			if (other.textLabel != null)
				return false;
		} else if (!textLabel.equals(other.textLabel))
			return false;
		if (translationToRDF == null) {
			if (other.translationToRDF != null)
				return false;
		} else if (!translationToRDF.equals(other.translationToRDF))
			return false;
		if (varnum != other.varnum)
			return false;
		return true;
	}*/
	
	
	
}
