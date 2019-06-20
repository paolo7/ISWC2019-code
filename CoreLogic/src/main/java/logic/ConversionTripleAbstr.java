package logic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public abstract class ConversionTripleAbstr implements ConversionTriple{

	
	@Override
	public Binding get(int i) {
		if (i == 0) return getSubject();
		else if (i == 1) return getPredicate();
		else if (i == 2) return getObject();
		else throw new RuntimeException("ERROR: "+i+" is not a valid index of a triple.");
	}
	@Override
	public Set<Integer> getNoLitVariables(Binding[] bindings){
		Set<Integer> noLitVars = new HashSet<Integer>();
		if(this.getSubject().isVar()) {
			Binding b = bindings[this.getSubject().getVar().getVarNum()];
			if(b.isVar()) noLitVars.add(new Integer(b.getVar().getVarNum()));
		}
		if(this.getPredicate().isVar()) {
			Binding b = bindings[this.getPredicate().getVar().getVarNum()];
			if(b.isVar()) noLitVars.add(new Integer(b.getVar().getVarNum()));
		}
		return noLitVars;
	}
	@Override
	public Set<Integer> getVariables(Binding[] bindings){
		Set<Integer> vars = new HashSet<Integer>();
		if(this.getSubject().isVar()) {
			Binding b = bindings[this.getSubject().getVar().getVarNum()];
			if(b.isVar()) vars.add(new Integer(b.getVar().getVarNum()));
		}
		if(this.getPredicate().isVar()) {
			Binding b = bindings[this.getPredicate().getVar().getVarNum()];
			if(b.isVar()) vars.add(new Integer(b.getVar().getVarNum()));
		}
		if(this.getObject().isVar()) {
			Binding b = bindings[this.getObject().getVar().getVarNum()];
			if(b.isVar()) vars.add(new Integer(b.getVar().getVarNum()));
		}
		return vars;
	}
	@Override
	public Set<Integer> getNoLitVariables(){
		Set<Integer> noLitVars = new HashSet<Integer>();
		if(this.getSubject().isVar()) {
			noLitVars.add(new Integer(this.getSubject().getVar().getVarNum()));
		}
		if(this.getPredicate().isVar()) {
			noLitVars.add(new Integer(this.getPredicate().getVar().getVarNum()));
		}
		return noLitVars;
	}
	
	@Override
	public String toGPPGSPARQL(Binding[] bindings) {
		return toGPPGSPARQL(0,null,bindings);
	}
	@Override
	public String toGPPGSPARQL(int variant, Set<Integer> noLitVars, Binding[] bindings) {
		String snippet = " {";
		//snippet += " UNION {"+toGPPGSPARQL(bindings,true,true,true) + (variant == 1 ? getFilterMustBeURI2(bindings) : "") + "}"; 
		// this case can be removed as it would only match the trivial case of the schema of all triplestores
		snippet += " 	   {"+toGPPGSPARQL(bindings,false,true,true) + (variant == 1 ? getFilterMustBeURI2(bindings) : "") + "}";
		snippet += " UNION {"+toGPPGSPARQL(bindings,true,true,false) + (variant == 1 ? getFilterMustBeURI3(bindings) : "") + "}";
		snippet += " UNION {"+toGPPGSPARQL(bindings,false,true,false) + (variant == 1 ? getFilterMustBeURI3(bindings) : "") + "}";
		snippet += " UNION {"+toGPPGSPARQL(bindings,true,false,true) + (variant == 1 ? getFilterMustBeURI2(bindings) : "") + "}";
		snippet += " UNION {"+toGPPGSPARQL(bindings,true,false,false) + (variant == 1 ? getFilterMustBeURI3(bindings) : "") + "}";
		snippet += " UNION {"+toGPPGSPARQL(bindings,false,false,true) + (variant == 1 ? getFilterMustBeURI2(bindings) : "") + "}";
		snippet += " UNION {"+toGPPGSPARQL(bindings,false,false,false) + (variant == 1 ? getFilterMustBeURI3(bindings) : "") + "}";
		if(variant == 1) {
			//only add extra literal lambdas if there is a variable in the object position which can be a literal
			if(this.getObject().isVar() && bindings[this.getObject().getVar().getVarNum()].isVar()) {
				if(!noLitVars.contains(new Integer(bindings[this.getObject().getVar().getVarNum()].getVar().getVarNum()))) {
					snippet += " UNION {"+toGPPGSPARQL(1,bindings,false,false,true) + "}";
					snippet += " UNION {"+toGPPGSPARQL(1,bindings,true,false,true) + "}";
					snippet += " UNION {"+toGPPGSPARQL(1,bindings,false,true,true) + "}";
					// this case can be removed as it would only match the trivial case of the schema of all triplestores
					//snippet += " UNION {"+toGPPGSPARQL(1,bindings,true,true,true) + "}";
				}
			}
		}
		return snippet+"}\n";
	}

	private String getFilterMustBeURI2(Binding[] bindings) {
		
		if(this.getObject().isVar()) {
			Binding b = bindings[this.getObject().getVar().getVarNum()];
			if(b.isVar()) return " BIND (<"+RDFUtil.LAMBDAURI+"> AS ?Rv"+b.getVar().getVarNum()+") "+
					" FILTER isIRI( IF( bound(?v"+b.getVar().getVarNum()+"), ?v"+b.getVar().getVarNum()+", <"+RDFUtil.LAMBDAURI+">) ) ";
		}
		return "";
	}
	private String getFilterMustBeURI3(Binding[] bindings) {
		
		if(this.getObject().isVar()) {
			Binding b = bindings[this.getObject().getVar().getVarNum()];
			if(b.isVar()) return " FILTER (!bound (?Rv"+b.getVar().getVarNum()+"))";
		}
		return "";
	}
	
	private String getFilterMustBeURI(Binding[] bindings) {
		
		if(this.getObject().isVar()) {
			Binding b = bindings[this.getObject().getVar().getVarNum()];
			if(b.isVar()) return "FILTER isIRI(?v"+b.getVar().getVarNum()+")";
		}
		return "";
	}
	
	public String toGPPGSPARQL(Binding[] bindings, boolean lambdaS, boolean lambdaP, boolean lambdaO) {
		return toGPPGSPARQL(0, bindings, lambdaS, lambdaP, lambdaO);
	}
	public String toGPPGSPARQL(int variant, Binding[] bindings, boolean lambdaS, boolean lambdaP, boolean lambdaO) {
		String snippet = " ";
		if (lambdaS) snippet += "<"+RDFUtil.LAMBDAURI+">";
		else {
			if(this.getSubject().isConstant()) snippet += this.getSubject().getConstant().getLexicalValueExpanded();
			else {
				Binding b = bindings[this.getSubject().getVar().getVarNum()];
				if(b.isConstant()) snippet += b.getConstant().getLexicalValueExpanded();
				else snippet += "?v"+b.getVar().getVarNum();
			}
		}
		snippet += " ";
		if (lambdaP) snippet += "<"+RDFUtil.LAMBDAURI+">";
		else {
			if(this.getPredicate().isConstant()) snippet += this.getPredicate().getConstant().getLexicalValueExpanded();
			else {
				Binding b = bindings[this.getPredicate().getVar().getVarNum()];
				if(b.isConstant()) snippet += b.getConstant().getLexicalValueExpanded();
				else snippet += "?v"+b.getVar().getVarNum();
			}
		}
		snippet += " ";
		if (lambdaO) {
			if(variant == 0) snippet += "<"+RDFUtil.LAMBDAURI+">";
			else if(variant == 1) snippet += "<"+RDFUtil.LAMBDAURILit+">";
		}
		else {
			if(this.getObject().isConstant()) snippet += this.getObject().getConstant().getLexicalValueExpanded();
			else {
				Binding b = bindings[this.getObject().getVar().getVarNum()];
				if(b.isConstant()) {
					if(b.getConstant().isLiteral()){
						snippet += "\""+b.getConstant().getLexicalValue()+"\"";
					} else {
						snippet += b.getConstant().getLexicalValueExpanded();
					}
				}
				else snippet += "?v"+b.getVar().getVarNum();
			}
		}
		return snippet+"\n";
	}
	
	
	
	public ConversionTriple applyBinding(Binding[] bindings) {
		return new ConversionTripleImpl(applyBindingHelper(bindings, getSubject()), 
				applyBindingHelper(bindings, getPredicate()), 
				applyBindingHelper(bindings, getObject()));
	}
	public Binding applyBindingHelper(Binding[] bindings, Binding binding) {
		if(binding.isConstant()) return binding;
		if(binding.getVar().getVarNum() < bindings.length) return bindings[binding.getVar().getVarNum()];
		return binding;
	}
	
	
	@Override
	public String toSPARQL(Binding[] bindings, String freshVarPrefix) {
		String snippet = "";
		if(this.getSubject().isConstant()) snippet += this.getSubject().getConstant().getLexicalValue();
		else if (this.getSubject().getVar().getVarNum() >= bindings.length) {
			snippet += "?v"+this.getSubject().getVar()+freshVarPrefix;
		} else {
			Binding b = bindings[this.getSubject().getVar().getVarNum()];
			if(b.isConstant()) snippet += b.getConstant().getLexicalValue();
			else snippet += "?v"+b.getVar();
		}
		snippet += " ";
		if(this.getPredicate().isConstant()) snippet += this.getPredicate().getConstant().getLexicalValueExpanded();
		else if (this.getPredicate().getVar().getVarNum() >= bindings.length) {
			snippet += "?v"+this.getPredicate().getVar()+freshVarPrefix;
		} else {
			Binding b = bindings[this.getPredicate().getVar().getVarNum()];
			if(b.isConstant()) snippet += b.getConstant().getLexicalValue();
			else snippet += "?v"+b.getVar();
		}
		snippet += " ";
		if(this.getObject().isConstant()) snippet += this.getObject().getConstant().getLexicalValue();
		else if (this.getObject().getVar().getVarNum() >= bindings.length) {
			snippet += "?v"+this.getObject().getVar()+freshVarPrefix;
		} else {
			Binding b = bindings[this.getObject().getVar().getVarNum()];
			if(b.isConstant()) {
				if(b.getConstant().isLiteral() && RDFUtil.isNumericDatatypeIRI(((ResourceLiteral) b.getConstant()).getLiteralTypeIRI()))
					snippet += b.getConstant().getLexicalValue();
				else if(b.getConstant().isLiteral()) snippet += "\""+b.getConstant().getLexicalValue()+"\"";
				else snippet += b.getConstant().getLexicalValueExpanded();
			}
			else snippet += "?v"+b.getVar();
		}
		return snippet;
	}
	
	@Override
	public String toSPARQL() {
		String snippet = "";
		if(this.getSubject().isConstant()) snippet += this.getSubject().getConstant().getLexicalValue();
		else {
			snippet += "?v"+this.getSubject().getVar();
		}
		snippet += " ";
		if(this.getPredicate().isConstant()) snippet += this.getPredicate().getConstant().getLexicalValue();
		else {
			snippet += "?v"+this.getPredicate().getVar();
		} 
		snippet += " ";
		if(this.getObject().isConstant()) snippet += this.getObject().getConstant().getLexicalValue();
		else {
			snippet += "?v"+this.getObject().getVar();
		} 
		return snippet;
	}
	

	
	@Override
	public String toSPARQL_INSERT(Binding[] bindings, String baseNew) {
		String snippet = "";
		if(this.getSubject().isConstant()) snippet += this.getSubject().getConstant().getLexicalValueExpanded();
		else if (this.getSubject().getVar().getVarNum() >= bindings.length) {
			snippet += RDFUtil.getBlankNodeOrNewVarString(baseNew,this.getSubject().getVar().getVarNum());
		} else {
			Binding b = bindings[this.getSubject().getVar().getVarNum()];
			if(b.isConstant()) snippet += b.getConstant().getLexicalValueExpanded();
			else snippet += RDFUtil.getBlankNodeOrNewVarString(baseNew,b.getVar().getVarNum());
		}
		snippet += " ";
		if(this.getPredicate().isConstant()) snippet += this.getPredicate().getConstant().getLexicalValueExpanded();
		else if (this.getPredicate().getVar().getVarNum() >= bindings.length) {
			snippet += RDFUtil.getBlankNodeOrNewVarString(baseNew,this.getPredicate().getVar().getVarNum());
		} else {
			Binding b = bindings[this.getPredicate().getVar().getVarNum()];
			if(b.isConstant()) snippet += b.getConstant().getLexicalValueExpanded();
			else snippet += RDFUtil.getBlankNodeOrNewVarString(baseNew,b.getVar().getVarNum());
		}
		snippet += " ";
		if(this.getObject().isConstant()) snippet += this.getObject().getConstant().getLexicalValueExpanded();
		else if (this.getObject().getVar().getVarNum() >= bindings.length) {
			snippet += RDFUtil.getBlankNodeOrNewVarString(baseNew,this.getObject().getVar().getVarNum());
		} else {
			Binding b = bindings[this.getObject().getVar().getVarNum()];
			if(b.isConstant()) {
				if(b.getConstant().isLiteral() && RDFUtil.isNumericDatatypeIRI(((ResourceLiteral) b.getConstant()).getLiteralTypeIRI()))
					snippet += b.getConstant().getLexicalValue();
				else snippet += b.getConstant().getLexicalValueExpanded();
			}
			else snippet += RDFUtil.getBlankNodeOrNewVarString(baseNew,b.getVar().getVarNum());
		}
		return snippet;
	}
	

	
	@Override
    public boolean equals(Object o) {
  
        if (o == this) {
            return true;
        }
        if (!(o instanceof ConversionTriple)) {
            return false;
        }
        ConversionTriple p = (ConversionTriple) o;
         
        return this.getSubject().equals(p.getSubject()) && this.getPredicate().equals(p.getPredicate())
        		&& this.getObject().equals(p.getObject());
    }
	
	@Override
	public String toString() {
		return this.getSubject().prettyPrint()+" "+this.getPredicate().prettyPrint()+" "+this.getObject().prettyPrint();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + ((this.getObject() == null) ? 0 : this.getObject().hashCode());
		result = prime * result + ((this.getPredicate() == null) ? 0 : this.getPredicate().hashCode());
		result = prime * result + ((this.getSubject() == null) ? 0 : this.getSubject().hashCode());
		return result;
	}
	
}
