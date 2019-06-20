package logic;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

public class PredicateInstantiationImpl extends PredicateInstantiationAbstr {
	
	public static boolean enable_additional_constraints = true;

	private Predicate predicate;
	private Binding[] bindings;
	public Set<ConversionTriple> additionalConstraints = new HashSet<>();
	
	/**
	 * The lenght of vars and resources should be equal to the varnum of the predicate.
	 * For each index i, either vars[i] == -1 or resources[i] == null
	 * @param predicate
	 * @param vars
	 * @param bindings
	 * @throws RuntimeException if vers.length != predicate.getVarnum() or if resources.length != predicate.getVarnum() or if (vars[i] == -1 and resources[i] == null) OR (vars[i] != -1 and resources[i] != null)
	 */
	public PredicateInstantiationImpl(Predicate predicate, Binding[] bindings) {
		this.predicate = predicate;
		this.bindings = bindings;
		if(bindings.length != predicate.getVarnum())
			throw new RuntimeException("ERROR: Instantiating a predicate with a number of bindings different from the number allowed by the predicate signature.");
		for(int i = 0; i < bindings.length; i++) {
			if(bindings[i] == null)
				throw new RuntimeException("ERROR: trying to instantiate a predicate with a null binding.");
		}
	}
		
	public PredicateInstantiationImpl(Predicate predicate, Binding[] bindings, Set<ConversionTriple> additionalConstraints) {
		this(predicate, bindings);
		if(enable_additional_constraints) this.additionalConstraints = additionalConstraints;	
	}
	
	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	@Override
	public Binding[] getBindings() {
		return bindings;
	}

	@Override
	public Binding getBinding(int index) {
		return bindings[index];
	}

	@Override
	public Pair<Set<ConversionTriple>,Set<ConversionFilter>> applyBinding(Map<String, RDFNode> bindingsMap, Binding[] newSignatureBindings) {
		Set<ConversionTriple> newTriples = new HashSet<ConversionTriple>();
		Set<ConversionFilter> newFilters = new HashSet<ConversionFilter>();
		if(predicate.getRDFtranslation() != null) for(ConversionTriple ct: predicate.getRDFtranslation()) {
			Binding subject = readjustTo0(reBind(ct.getSubject(),bindingsMap),bindingsMap, newSignatureBindings);
			Binding predicate = readjustTo0(reBind(ct.getPredicate(),bindingsMap),bindingsMap, newSignatureBindings);
			Binding object = readjustTo0(reBind(ct.getObject(),bindingsMap),bindingsMap, newSignatureBindings);
			newTriples.add(new ConversionTripleImpl(subject,predicate,object));
		} else newTriples = null;
		if(predicate.getRDFtranslationFilters() != null) for(ConversionFilter cf: predicate.getRDFtranslationFilters()) {
			List<TextTemplate> ttList = new LinkedList<TextTemplate>();
			for(TextTemplate tt : cf.templates) {
				if(tt.isText()) ttList.add(new TextTemplateImpl(tt.getText()));
				else {
					Binding b = readjustTo0(reBind(new BindingImpl(new VariableImpl(tt.getVar())),bindingsMap),bindingsMap, newSignatureBindings);
					if(b.isConstant()) {
						if(b.getConstant().isLiteral()) {
							
							ttList.add(new TextTemplateImpl(b.getConstant().getLexicalValue()));
						}
						if(b.getConstant().isURI()) ttList.add(new TextTemplateImpl("<"+b.getConstant().getLexicalValue()+">"));
					}
					else ttList.add(new TextTemplateImpl(b.getVar().getVarNum()));
				}
			}
			newFilters.add(new ConversionFilter(ttList));
		} else newFilters = null;
		return new ImmutablePair<Set<ConversionTriple>,Set<ConversionFilter>>(newTriples,newFilters);
	}
	
	
	private Binding readjustTo0(Binding b, Map<String, RDFNode> bindingsMap, Binding[] newSignatureBindings) {
		if(b.isConstant()) return b;
		int found = -1;
		for(int i = 0; i < newSignatureBindings.length; i++) {
			if(newSignatureBindings[i].isVar() && newSignatureBindings[i].getVar() == b.getVar() && bindingsMap.containsKey("v"+i))
				found = i;
		}
		if(found != -1) return new BindingImpl(new VariableImpl(found));
		else {
			if(b.isConstant()) return b;
			else {
				return new BindingImpl(new VariableImpl(b.getVar().getVarNum()+newSignatureBindings.length));
			}
			//throw new RuntimeException("ERROR: trying to instantiate a predicate, but there is a variable in the label that is not a variable of the predicate signature.");
		}
	}
	
	private Binding reBind(Binding b, Map<String, RDFNode> bindingsMap) {
		if(b.isConstant()) return b;
		else {
			Binding originalBinding = bindings[b.getVar().getVarNum()];
			if(originalBinding.isConstant()) return originalBinding;
			else if(bindingsMap.containsKey("v"+originalBinding.getVar())) {				
				RDFNode node = bindingsMap.get("v"+originalBinding.getVar());
				if(node == null || node.isAnon()) return originalBinding;
				else {
					if(node.isLiteral()) 
						return new BindingImpl(new ResourceLiteral(node.asLiteral().getLexicalForm(), node.asLiteral().getDatatypeURI()));
					else if(node.isURIResource()) return new BindingImpl(new ResourceURI(node.asResource().getURI()));
					else throw new RuntimeException("ERROR: cannot create a conversion triple because node is neither null, nor blank, nor literal, nor URI");
				}
			} else return originalBinding;
				
		}
	}

	@Override
	public Set<ConversionTriple> getAdditionalConstraints() {
		return additionalConstraints;
	}
	
	



/*	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PredicateInstantiationImpl other = (PredicateInstantiationImpl) obj;
		if (!Arrays.equals(bindings, other.bindings))
			return false;
		if (predicate == null) {
			if (other.predicate != null)
				return false;
		} else if (!predicate.equals(other.predicate))
			return false;
		return true;
	}*/


	
	


}
