package logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;

public abstract class PredicateInstantiationAbstr implements PredicateInstantiation {

	@Override
	public String toSPARQL() {
		String snippet = "";
		String freshVarPrefix = RDFUtil.getNewFreshVariablePrefix();
		if (this.getPredicate().getRDFtranslation() != null) for(ConversionTriple ct: this.getPredicate().getRDFtranslation()) {
			snippet += ct.toSPARQL(this.getBindings(), freshVarPrefix)+" .\n";
		}
		if (this.getPredicate().getRDFtranslationFilters() != null) for(ConversionFilter cf: this.getPredicate().getRDFtranslationFilters()) {
			snippet += cf.toSPARQL(this.getBindings(), freshVarPrefix)+" .\n";
		}
		return snippet;
	}
	
	@Override
	public String toSPARQL_INSERT(String baseNew) {
		String snippet = "";
		if (this.getPredicate().getRDFtranslation() != null) for(ConversionTriple ct: this.getPredicate().getRDFtranslation()) {
			snippet += ct.toSPARQL_INSERT(this.getBindings(), baseNew)+" .\n";
		}
		if (this.getPredicate().getRDFtranslationFilters() != null) for(ConversionFilter cf: this.getPredicate().getRDFtranslationFilters()) {
			snippet += cf.toSPARQL_INSERT(this.getBindings(), baseNew)+" .\n";
		}
		return snippet;
	}
	
	@Override
	public boolean hasVariables() {
		if(this.getPredicate().getVarnum() == 0) {
			return false;			
		}
		for(Binding b : this.getBindings()) {
			if(b.isVar()) return true;
		}
		return false;
	} 
	
	@Override
	public Set<Integer> getNoLitVariables(){
		Set<Integer> noLitVars = new HashSet<Integer>();
		if(this.getPredicate().getRDFtranslation() != null) for(ConversionTriple ct: this.getPredicate().getRDFtranslation()) {
			noLitVars.addAll(ct.getNoLitVariables(this.getBindings()));
		}
		return noLitVars;
	}
	
	@Override
	public Set<Integer> getVariables(){
		Set<Integer> noLitVars = new HashSet<Integer>();
		if(this.getPredicate().getRDFtranslation() != null) for(ConversionTriple ct: this.getPredicate().getRDFtranslation()) {
			noLitVars.addAll(ct.getVariables(this.getBindings()));
		}
		return noLitVars;
	}
	
	@Override
	public String toGPPGSPARQL() {
		return toGPPGSPARQL(0);
	}
	
	@Override
	public String toGPPGSPARQL(int variant) {
		String snippet = "";
		if(this.getPredicate().getRDFtranslation() != null) for(ConversionTriple ct: this.getPredicate().getRDFtranslation()) {
			snippet += ct.toGPPGSPARQL(variant, getNoLitVariables(), this.getBindings())+" \n";
		}
		return snippet;
	}
	
	@Override
    public boolean equals(Object o) {
  
        if (o == this) {
            return true;
        }
        if (!(o instanceof PredicateInstantiation)) {
            return false;
        }
        PredicateInstantiation p = (PredicateInstantiation) o;
        
        if(! this.getPredicate().equals(p.getPredicate())) return false;
        for(int i = 0; i < this.getBindings().length; i++) {
        	if(! this.getBinding(i).equals(p.getBinding(i))) return false;
        }
        if(this.getAdditionalConstraints().size() != p.getAdditionalConstraints().size())
        	return false;
        if(!this.getAdditionalConstraints().equals(p.getAdditionalConstraints()))
        	return false;
        return true;
    }
	
	@Override
	public String toString() {
		String s = this.getPredicate().getName()+"(";
		boolean first = true;
		for(int i = 0; i < this.getPredicate().getVarnum(); i++) {
			if(first) first = false;
			else s += ", ";
			s += this.getBindings()[i].prettyPrint();
		}
		s += ")";
		if(!getAdditionalConstraints().isEmpty()) {
			s += " {";
			for(ConversionTriple ct : getAdditionalConstraints())
				s += "["+ct+"]";
			s += "}";
		}
		return s;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + Arrays.hashCode(this.getBindings());
		result = prime * result + ((this.getPredicate() == null) ? 0 : this.getPredicate().hashCode());
		return result;
	}
	
	@Override
	public boolean compatible(PredicateInstantiation other, Map<String, RDFNode> bindingsMap) {
		if(!getPredicate().equals(other.getPredicate())) return false;
		for(int i = 0; i < getBindings().length; i++) {
			Binding boundValue = getBindings()[i];
			if(boundValue.isVar() && bindingsMap.get("v"+boundValue.getVar()) != null && bindingsMap.get("v"+boundValue.getVar()).isURIResource()) 
				boundValue = new BindingImpl(new ResourceURI(bindingsMap.get("v"+boundValue.getVar()).asResource().getURI()));
			else if(boundValue.isVar() && bindingsMap.get("v"+boundValue.getVar()) != null && bindingsMap.get("v"+boundValue.getVar()).isLiteral()) 
				boundValue = new BindingImpl(new ResourceLiteral(bindingsMap.get("v"+boundValue.getVar()).asLiteral().getLexicalForm(), bindingsMap.get("v"+boundValue.getVar()).asLiteral().getDatatypeURI()));
			if(boundValue.isConstant() && other.getBindings()[i].isConstant() && ! boundValue.equals(other.getBindings()[i]))
				return false;
		}
		return true;
	}
	
	@Override
	public Set<ConversionTriple> getAdditionalConstraints(Binding[] bindings) {
		Set<ConversionTriple> additional = getAdditionalConstraints();
		Set<ConversionTriple> additionalTranslated = new HashSet<ConversionTriple>();
		for(ConversionTriple ct : additional) {
			additionalTranslated.add(ct.applyBinding(bindings));
		}
		return additionalTranslated;
	}
}
