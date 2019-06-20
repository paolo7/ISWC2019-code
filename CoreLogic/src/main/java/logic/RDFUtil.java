package logic;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.PrefixMapping;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public class RDFUtil {
	
	public static String LAMBDAURI = "http://w3id.org/prohow/GPPG#LAMBDA"+new java.util.Date().getTime();
	public static String LAMBDAURILit = "http://w3id.org/prohow/GPPG#LAMBDA"+new java.util.Date().getTime()+"LIT";
	public static LabelService labelService;
	
	private static final Model mInternalModel = ModelFactory.createDefaultModel(); 
	
	public static PrefixMapping prefixes = PrefixMapping.Factory.create();
	
	public static long freshVariablePrefix = 0;
	
	public static boolean ignoreConstraints = false;
	
	private static void generateRuleInstantiationModelHelper(Model model, PredicateInstantiation psi, ConversionTriple ct, Map<String,RDFNode> bindingsMap, Integer i, Binding[] newPredicateBindings) {
		Resource subject = null;
		Property predicate = null;
		RDFNode object = null;
		// if element is constant
		if(ct.getSubject().isConstant()) subject = ResourceFactory.createResource(model.expandPrefix(ct.getSubject().getConstant().getLexicalValue()));
		if(ct.getPredicate().isConstant()) predicate = ResourceFactory.createProperty(model.expandPrefix(ct.getPredicate().getConstant().getLexicalValue()));
		if(ct.getObject().isConstant()) {
			if(ct.getObject().getConstant().isURI())
				object = ResourceFactory.createResource(model.expandPrefix(ct.getObject().getConstant().getLexicalValue()));
			else 
				object = ResourceFactory.createPlainLiteral(ct.getObject().getConstant().getLexicalValue());
		}
		Binding objS = ct.getSubject();
		Binding objP = ct.getPredicate();
		Binding objO = ct.getObject();
		if(newPredicateBindings != null) {
			if(ct.getSubject().isVar() && objS.getVar().getVarNum() < newPredicateBindings.length) objS = newPredicateBindings[objS.getVar().getVarNum()];
			if(ct.getPredicate().isVar() && objP.getVar().getVarNum() < newPredicateBindings.length) objP = newPredicateBindings[objP.getVar().getVarNum()];
			if(ct.getObject().isVar() && objO.getVar().getVarNum() < newPredicateBindings.length) objO = newPredicateBindings[objO.getVar().getVarNum()];
		}
		
		// if element is variable mapped to constant		
		if(objS.isVar() && objS.getVar().getVarNum() < psi.getBindings().length && psi.getBinding(objS.getVar().getVarNum()).isConstant()) {
			if(!psi.getBinding(objS.getVar().getVarNum()).getConstant().isURI())
				throw new RuntimeException("ERROR: the subject of a triple cannot be a literal");
			subject = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(objS.getVar().getVarNum()).getConstant().getLexicalValue()));
		}
		if(objP.isVar() && objP.getVar().getVarNum() < psi.getBindings().length && psi.getBinding(objP.getVar().getVarNum()).isConstant()) {
			if(!psi.getBinding(objP.getVar().getVarNum()).getConstant().isURI())
				throw new RuntimeException("ERROR: the predicate of a triple cannot be a literal");
			predicate = ResourceFactory.createProperty(model.expandPrefix(psi.getBinding(objP.getVar().getVarNum()).getConstant().getLexicalValue()));
		}
		if(objO.isVar() && objO.getVar().getVarNum() < psi.getBindings().length && psi.getBinding(objO.getVar().getVarNum()).isConstant()) {
			if(psi.getBinding(objO.getVar().getVarNum()).getConstant().isURI())
				object = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(objO.getVar().getVarNum()).getConstant().getLexicalValue()));
			else
				object = ResourceFactory.createPlainLiteral(psi.getBinding(objO.getVar().getVarNum()).getConstant().getLexicalValue());
		}
		// if element is variable mapped to variable	
		if(objS.isVar() && objS.getVar().getVarNum() < psi.getBindings().length && psi.getBinding(objS.getVar().getVarNum()).isVar()) {
			if(bindingsMap.containsKey("v"+psi.getBinding(objS.getVar().getVarNum()).getVar().getVarNum())) {
				RDFNode boundValue = bindingsMap.get("v"+psi.getBinding(objS.getVar().getVarNum()).getVar());
				if(boundValue == null) {
					RDFNode element = null;//bindingsMap.get("v"+psi.getBinding(ct.getSubject().getVar()).getVar());
					if(element != null && !element.isURIResource())
						throw new RuntimeException("ERROR: the subject of a triple cannot be a literal");
					if(element == null || element.asResource().getURI().equals(LAMBDAURI))
						subject = model.createResource(new AnonId(LAMBDAURI+psi.getBinding(objS.getVar().getVarNum())));
					else
						subject = element.asResource();					
				} else {
					subject = boundValue.asResource();
				}
			}
		}
		if(objP.isVar() && objP.getVar().getVarNum() < psi.getBindings().length && psi.getBinding(objP.getVar().getVarNum()).isVar() ) {
			RDFNode boundValue = bindingsMap.get("v"+psi.getBinding(objP.getVar().getVarNum()).getVar());
			if(boundValue == null) {
				RDFNode element = null;//bindingsMap.get("v"+psi.getBinding(ct.getPredicate().getVar()).getVar());
				if(element != null && !element.isURIResource())
					throw new RuntimeException("ERROR: the subject of a triple cannot be a literal");
				if(element == null || element.asResource().getURI().equals(LAMBDAURI))
					throw new RuntimeException("ERROR: the predicate of a triple cannot be assigned a lambda value as it cannot be a blank node");
					//predicate = model.createResource(new AnonId(LAMBDAURI+psi.getBinding(objP.getVar()));
				else
					predicate = ResourceFactory.createProperty(model.expandPrefix(element.asResource().getLocalName()));
				i++;
			} else {
				predicate = ResourceFactory.createProperty(boundValue.asResource().getURI());
			}
		}
		if(objO.isVar() && objO.getVar().getVarNum() < psi.getBindings().length && psi.getBinding(objO.getVar().getVarNum()).isVar()) {
			RDFNode boundValue = bindingsMap.get("v"+psi.getBinding(objO.getVar().getVarNum()).getVar());
			if(boundValue == null) {
				RDFNode element = null;//bindingsMap.get("v"+psi.getBinding(ct.getObject().getVar()).getVar());
				if(element == null || element.isURIResource() && element.asResource().getURI().equals(LAMBDAURI))
					object = model.createResource(new AnonId(LAMBDAURI+psi.getBinding(objO.getVar().getVarNum())));
				else 
					object = element;
			} else {
				object = boundValue.asResource();
			}
		}
		/*if(subject == null)
			subject = ResourceFactory.createResource();
		if(predicate == null) {
			predicate = ResourceFactory.createProperty(LAMBDAURI+h+1);
			i++;
		}
		if(object == null)
			object = ResourceFactory.createResource();
		//
*/		if(subject == null)
			subject = model.createResource(new AnonId(LAMBDAURI+"?v"+objS.getVar()));
		if(predicate == null) {
			throw new RuntimeException("ERROR: the predicate of a triple cannot be assigned a lambda value as it cannot be a blank node");
			//predicate = model.createResource(new AnonId(LAMBDAURI+"?v"+objP.getVar());
		}
		if(object == null)
			object = model.createResource(new AnonId(LAMBDAURI+"?v"+objO.getVar()));
		//
		Statement s = ResourceFactory.createStatement(subject, predicate, object);
		model.add(s);
	}
	
	public static String getNewFreshVariablePrefix() {
		freshVariablePrefix++;
		return "f"+freshVariablePrefix;
	}
	
	public static Model generateRuleInstantiationModel(Rule r, Map<String,RDFNode> bindingsMap, Map<String,String> prefixes, Set<Predicate> knownPredicates, Set<PredicateInstantiation> inferrablePredicates) {
		Model model = ModelFactory.createDefaultModel();
		Integer i = 0;
		for(String s: prefixes.keySet()) {
			model.setNsPrefix(s,prefixes.get(s));
		}
		
		for(PredicateInstantiation psi : r.getAntecedent()) {
			Set<ConversionTriple> translationPlusConsequences = new HashSet<ConversionTriple>();
			if(psi.getPredicate().getRDFtranslation() != null) translationPlusConsequences.addAll(psi.getPredicate().getRDFtranslation());
			translationPlusConsequences.addAll(psi.getAdditionalConstraints());
			Binding[] newB = r.getNewPredicateBasicBindings();
			for(ConversionTriple ct: translationPlusConsequences) {
				generateRuleInstantiationModelHelper(model, psi, ct, bindingsMap, i, null);	
			}
		}
		// TODO fix, this part is not working as intended
		for(PredicateInstantiation psi : inferrablePredicates) {
			Set<ConversionTriple> translationPlusConsequences = new HashSet<ConversionTriple>();
			translationPlusConsequences.addAll(psi.getPredicate().getRDFtranslation());
			translationPlusConsequences.addAll(psi.getAdditionalConstraints());
			for(ConversionTriple ct: translationPlusConsequences) {
				generateRuleInstantiationModelHelper(model, psi, ct, bindingsMap, i, r.getNewPredicateBasicBindings());	
			}
		}
		
		
		//try {
			//model.write(new FileOutputStream(new File(System.getProperty("user.dir") + "/resources/outputgraphInstantiationModel.ttl")),"Turtle");
			//} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
		return model;
	}
	
	public static Model generateBasicModel(Set<PredicateInstantiation> predicates, Map<String,String> prefixes) {
		Model model = ModelFactory.createDefaultModel();
		for(String s: prefixes.keySet()) {
			model.setNsPrefix(s,prefixes.get(s));
		}
		int i = 0;		
		for(PredicateInstantiation psi: predicates) if (psi.getPredicate().getRDFtranslation() != null){
			Set<ConversionTriple> translationPlusConsequences = new HashSet<ConversionTriple>();
			translationPlusConsequences.addAll(psi.getPredicate().getRDFtranslation());
			translationPlusConsequences.addAll(psi.getAdditionalConstraints());
			for(ConversionTriple ct: translationPlusConsequences) {
				// if element is variable mapped to variable	
				Resource subject = ResourceFactory.createResource();
				Property predicate = ResourceFactory.createProperty(LAMBDAURI+i);
				i++;
				RDFNode object = ResourceFactory.createResource();	
				// if element is constant
				if(ct.getSubject().isConstant()) subject = ResourceFactory.createResource(model.expandPrefix(ct.getSubject().getConstant().getLexicalValue()));
				if(ct.getPredicate().isConstant()) predicate = ResourceFactory.createProperty(model.expandPrefix(ct.getPredicate().getConstant().getLexicalValue()));
				if(ct.getObject().isConstant()) {
					if(ct.getObject().getConstant().isURI())
						object = ResourceFactory.createResource(model.expandPrefix(ct.getObject().getConstant().getLexicalValue()));
					else 
						object = ResourceFactory.createPlainLiteral(ct.getObject().getConstant().getLexicalValue());
				}
				// if element is variable mapped to constant	
				if(ct.getSubject().isVar() && ct.getSubject().getVar().getVarNum() < psi.getBindings().length && psi.getBinding(ct.getSubject().getVar().getVarNum()).isConstant()) {
					if(!psi.getBinding(ct.getSubject().getVar().getVarNum()).getConstant().isURI())
						throw new RuntimeException("ERROR: the subject of a triple cannot be a literal");
					subject = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(ct.getSubject().getVar().getVarNum()).getConstant().getLexicalValue()));
				}
				if(ct.getPredicate().isVar() && ct.getPredicate().getVar().getVarNum() < psi.getBindings().length && psi.getBinding(ct.getPredicate().getVar().getVarNum()).isConstant()) {
					if(!psi.getBinding(ct.getPredicate().getVar().getVarNum()).getConstant().isURI())
						throw new RuntimeException("ERROR: the predicate of a triple cannot be a literal");
					predicate = ResourceFactory.createProperty(model.expandPrefix(psi.getBinding(ct.getPredicate().getVar().getVarNum()).getConstant().getLexicalValue()));
				}
				if(ct.getObject().isVar() && ct.getObject().getVar().getVarNum() < psi.getBindings().length && psi.getBinding(ct.getObject().getVar().getVarNum()).isConstant()) {
					if(psi.getBinding(ct.getObject().getVar().getVarNum()).getConstant().isURI())
						object = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(ct.getObject().getVar().getVarNum()).getConstant().getLexicalValue()));
					else
						object = ResourceFactory.createPlainLiteral(psi.getBinding(ct.getObject().getVar().getVarNum()).getConstant().getLexicalValue());
				}
				
				Statement s = ResourceFactory.createStatement(subject, predicate, object);
				model.add(s);
			}
		}
		
		//try {
		//	model.write(new FileOutputStream(new File(System.getProperty("user.dir") + "/resources/outputgraphBasic.ttl")),"Turtle");
		//} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		return model;
	}
	
	public static void addToDefaultPrefixes(Map<String,String> prefix) {
		for(String s: prefix.keySet()) {
			prefixes.setNsPrefix(s,prefix.get(s));
		}
	}
	public static void addToDefaultPrefixes(Model additionalVocabularies) {
		prefixes.setNsPrefixes(additionalVocabularies).getNsPrefixMap();
	}
	
	public static Model generateCriticalInstanceModel(Set<PredicateInstantiation> predicates, Map<String,String> prefixes, Rule r) {
		Property lambda = ResourceFactory.createProperty(LAMBDAURI);
		Model model = ModelFactory.createDefaultModel();
		// GATHER ALL CONSTANTS
		Set<logic.Resource> constants = gatherAllConstants(r);
		//Set<String> literals = gatherAllLiterals(r);
		for(PredicateInstantiation pi: predicates) {
			constants.addAll(gatherAllConstants(pi));
			//literals.addAll(gatherAllLiterals(pi));
		}
		constants.add(null);
		// INSTANTIATE THE CRITICAL INSTANCE
		for(PredicateInstantiation psi: predicates) {
			for(ConversionTriple ct: psi.getPredicate().getRDFtranslation()) {
				Resource subject = lambda;
				Property predicate = lambda;
				RDFNode object = lambda;				
				if(ct.getSubject().isConstant()) subject = ResourceFactory.createResource(RDFUtil.expandPrefix(ct.getSubject().getConstant().getLexicalValue()));
				if(ct.getPredicate().isConstant()) 
					predicate = ResourceFactory.createProperty(RDFUtil.expandPrefix(ct.getPredicate().getConstant().getLexicalValue()));
				if(ct.getObject().isConstant()) {
					if(ct.getObject().getConstant().isURI())
						object = ResourceFactory.createResource(RDFUtil.expandPrefix(ct.getObject().getConstant().getLexicalValue()));
					else 
						object = ResourceFactory.createPlainLiteral(ct.getObject().getConstant().getLexicalValue());
				}
				
				if(ct.getSubject().isVar() && ct.getSubject().getVar().getVarNum() < psi.getBindings().length && psi.getBinding(ct.getSubject().getVar().getVarNum()).isConstant()) {
					if(!psi.getBinding(ct.getSubject().getVar().getVarNum()).getConstant().isURI())
						throw new RuntimeException("ERROR: the subject of a triple cannot be a literal");
					subject = ResourceFactory.createResource(RDFUtil.expandPrefix(psi.getBinding(ct.getSubject().getVar().getVarNum()).getConstant().getLexicalValue()));
				}
				if(ct.getPredicate().isVar() && ct.getPredicate().getVar().getVarNum() < psi.getBindings().length && psi.getBinding(ct.getPredicate().getVar().getVarNum()).isConstant()) {
					if(!psi.getBinding(ct.getPredicate().getVar().getVarNum()).getConstant().isURI())
						throw new RuntimeException("ERROR: the predicate of a triple cannot be a literal");
					predicate = ResourceFactory.createProperty(RDFUtil.expandPrefix(psi.getBinding(ct.getPredicate().getVar().getVarNum()).getConstant().getLexicalValue()));
				}
				if(ct.getObject().isVar() && ct.getObject().getVar().getVarNum() < psi.getBindings().length && psi.getBinding(ct.getObject().getVar().getVarNum()).isConstant()) {
					if(psi.getBinding(ct.getObject().getVar().getVarNum()).getConstant().isURI())
						object = ResourceFactory.createResource(RDFUtil.expandPrefix(psi.getBinding(ct.getObject().getVar().getVarNum()).getConstant().getLexicalValue()));
					else
						object = ResourceFactory.createPlainLiteral(psi.getBinding(ct.getObject().getVar().getVarNum()).getConstant().getLexicalValue());
				}
				
				int variables = 0;
				if(subject.isURIResource() && subject.asResource().getURI().equals(LAMBDAURI)) variables++;
				if(predicate.isURIResource() && predicate.asResource().getURI().equals(LAMBDAURI)) variables++;
				if(object.isURIResource() && object.asResource().getURI().equals(LAMBDAURI)) variables++;
				if(variables == 0) {
					Statement base = ResourceFactory.createStatement(subject, predicate, object);
					model.add(base);
				} else if(variables == 1) {
					for(logic.Resource c: constants) {
						Triple<Resource,Property,RDFNode> newTriple = substituteFirstLambdaWithConstant(model,subject, predicate, object, c);
						Statement s = ResourceFactory.createStatement(newTriple.getLeft(), newTriple.getMiddle(), newTriple.getRight());
						model.add(s);
					}
				} else if(variables == 2) {
					for(logic.Resource c: constants) {
						Triple<Resource,Property,RDFNode> newTriple = substituteFirstLambdaWithConstant(model,subject, predicate, object, c);
						Statement s = ResourceFactory.createStatement(newTriple.getLeft(), newTriple.getMiddle(), newTriple.getRight());
						model.add(s);
						for(logic.Resource c2: constants) {
							Triple<Resource,Property,RDFNode> newTriple2 = substituteFirstLambdaWithConstant(model,newTriple.getLeft(), newTriple.getMiddle(), newTriple.getRight(), c2);
							Statement s2 = ResourceFactory.createStatement(newTriple2.getLeft(), newTriple2.getMiddle(), newTriple2.getRight());
							model.add(s2);
						}
					}
				}
				
				
				if(variables == 1) {
					for(logic.Resource c: constants) {
						Triple<Resource,Property,RDFNode> newTriple = substituteLastLambdaWithConstant(model,subject, predicate, object, c);
						Statement s = ResourceFactory.createStatement(newTriple.getLeft(), newTriple.getMiddle(), newTriple.getRight());
						model.add(s);
					}
				} else if(variables == 2) {
					for(logic.Resource c: constants) {
						Triple<Resource,Property,RDFNode> newTriple = substituteLastLambdaWithConstant(model,subject, predicate, object, c);
						Statement s = ResourceFactory.createStatement(newTriple.getLeft(), newTriple.getMiddle(), newTriple.getRight());
						model.add(s);
						for(logic.Resource c2: constants) {
							Triple<Resource,Property,RDFNode> newTriple2 = substituteLastLambdaWithConstant(model,newTriple.getLeft(), newTriple.getMiddle(), newTriple.getRight(), c2);
							Statement s2 = ResourceFactory.createStatement(newTriple2.getLeft(), newTriple2.getMiddle(), newTriple2.getRight());
							model.add(s2);
						}
					}
				}
				
			}
		}
		return model;
	}
	private static Triple<Resource,Property,RDFNode> substituteLastLambdaWithConstant(Model model, Resource subject, Property predicate, RDFNode object, logic.Resource constant) {
		if(constant == null) return new ImmutableTriple<Resource,Property,RDFNode>(subject,predicate,object);
		
		if(constant.isLiteral()) {
			if(object.isURIResource() && object.asResource().getURI().equals(LAMBDAURI)) {
				return new ImmutableTriple<Resource,Property,RDFNode>(subject,predicate,
						ResourceFactory.createPlainLiteral(constant.getLexicalValue()));
			}
			else return new ImmutableTriple<Resource,Property,RDFNode>(subject,predicate,object);
		}
		
		if(object.isURIResource() && object.asResource().getURI().equals(LAMBDAURI)) {
			return new ImmutableTriple<Resource,Property,RDFNode>(subject,predicate,
					ResourceFactory.createResource(RDFUtil.expandPrefix(constant.getLexicalValue()))
					);
		}
		if(predicate.isURIResource() && predicate.asResource().getURI().equals(LAMBDAURI)) {
			return new ImmutableTriple<Resource,Property,RDFNode>(subject,
					ResourceFactory.createProperty(RDFUtil.expandPrefix(constant.getLexicalValue())),
					object);
		}
		if(subject.isURIResource() && subject.asResource().getURI().equals(LAMBDAURI)) {
			return new ImmutableTriple<Resource,Property,RDFNode>(
					ResourceFactory.createResource(RDFUtil.expandPrefix(constant.getLexicalValue()))
					,predicate,object);
		}
		return new ImmutableTriple<Resource,Property,RDFNode>(subject,predicate,object);
	}
	private static Triple<Resource,Property,RDFNode> substituteFirstLambdaWithConstant(Model model, Resource subject, Property predicate, RDFNode object, logic.Resource constant) {
		if(constant == null) return new ImmutableTriple<Resource,Property,RDFNode>(subject,predicate,object);
		
		if(constant.isLiteral()) {
			if(object.isURIResource() && object.asResource().getURI().equals(LAMBDAURI)) {
				return new ImmutableTriple<Resource,Property,RDFNode>(subject,predicate,
						ResourceFactory.createPlainLiteral(constant.getLexicalValue()));
			}
			else return new ImmutableTriple<Resource,Property,RDFNode>(subject,predicate,object);
		}
		
		if(subject.isURIResource() && subject.asResource().getURI().equals(LAMBDAURI)) {
			return new ImmutableTriple<Resource,Property,RDFNode>(
					ResourceFactory.createResource(RDFUtil.expandPrefix(constant.getLexicalValue()))
					,predicate,object);
		}
		if(predicate.isURIResource() && predicate.asResource().getURI().equals(LAMBDAURI)) {
			return new ImmutableTriple<Resource,Property,RDFNode>(subject,
					ResourceFactory.createProperty(RDFUtil.expandPrefix(constant.getLexicalValue())),
					object);
		}
		if(object.isURIResource() && object.asResource().getURI().equals(LAMBDAURI)) {
			return new ImmutableTriple<Resource,Property,RDFNode>(subject,predicate,
					ResourceFactory.createResource(RDFUtil.expandPrefix(constant.getLexicalValue()))
					);
		}
		return new ImmutableTriple<Resource,Property,RDFNode>(subject,predicate,object);
	}

	private static Set<logic.Resource> gatherAllConstants(Rule r){
		Set<logic.Resource> constants = new HashSet<logic.Resource>();
		for(PredicateInstantiation pi: r.getAntecedent()) {
			constants.addAll(gatherAllConstants(pi));
		}
		return constants;
	}
	/*private static Set<String> gatherAllLiterals(Rule r){
		Set<String> constants = new HashSet<String>();
		for(PredicateInstantiation pi: r.getAntecedent()) {
			constants.addAll(gatherAllLiterals(pi));
		}
		return constants;
	}*/
	
	public static boolean excludePredicatesFromCriticalInstanceConstants = false;
	
	private static Set<logic.Resource> gatherAllConstants(PredicateInstantiation pi){
		Set<logic.Resource> constants = new HashSet<logic.Resource>();
		// gather constants from the bindings
		for(Binding b: pi.getBindings()) {
			constants.add(getConstantFromBinding(b));
		}
		// gather constants from the triples of the predicate
		for(ConversionTriple ct : pi.getPredicate().getRDFtranslation()) {
			constants.add(getConstantFromBinding(ct.getSubject()));
			if(!excludePredicatesFromCriticalInstanceConstants) constants.add(getConstantFromBinding(ct.getPredicate()));
			constants.add(getConstantFromBinding(ct.getObject()));
		}
		return constants;
	}
	/*private static Set<String> gatherAllLiterals(PredicateInstantiation pi){
		Set<String> constants = new HashSet<String>();
		// gather constants from the bindings
		for(Binding b: pi.getBindings()) {
			constants.add(getLiteralFromBinding(b));
		}
		// gather constants from the triples of the predicate
		for(ConversionTriple ct : pi.getPredicate().getRDFtranslation()) {
			constants.add(getLiteralFromBinding(ct.getObject()));
		}
		return constants;
	}*/
	
	private static logic.Resource getConstantFromBinding(Binding b) {
		if(b.isConstant()) return b.getConstant();
		return null;
	}
	/*private static String getLiteralFromBinding(Binding b) {
		if(b.isConstant() && b.getConstant().isLiteral()) return b.getConstant().getLexicalValue();
		return null;
	}*/
	
	public static Model generateGPPGSandboxModel(Set<PredicateInstantiation> predicates, Map<String,String> prefixes) {
		return generateGPPGSandboxModel(0, predicates, prefixes);
	}
	/**
	 * 
	 * @param variant 0 for the original GPPG algorithm, 1 for the literal-augmented one
	 * @param predicates
	 * @param prefixes
	 * @return
	 */
	public static Model generateGPPGSandboxModel(int variant, Set<PredicateInstantiation> predicates, Map<String,String> prefixes) {
		Model model = ModelFactory.createDefaultModel();
		for(String s: prefixes.keySet()) {
			model.setNsPrefix(s,prefixes.get(s));
		}
		
		Property lambda = ResourceFactory.createProperty(LAMBDAURI);
		Property lambdaLit = ResourceFactory.createProperty(LAMBDAURILit);
		//Resource lambda = ResourceFactory.createResource(LAMBDAURI);
		
		for(PredicateInstantiation psi: predicates) {
			if(psi.getPredicate().getRDFtranslation() != null) for(ConversionTriple ct: psi.getPredicate().getRDFtranslation()) {
				Resource subject = lambda;
				Property predicate = lambda;
				RDFNode object = lambda;				
				if(ct.getSubject().isConstant()) subject = ResourceFactory.createResource(model.expandPrefix(ct.getSubject().getConstant().getLexicalValue()));
				if(ct.getPredicate().isConstant()) predicate = ResourceFactory.createProperty(model.expandPrefix(ct.getPredicate().getConstant().getLexicalValue()));
				if(ct.getObject().isConstant()) {
					if(ct.getObject().getConstant().isURI())
						object = ResourceFactory.createResource(model.expandPrefix(ct.getObject().getConstant().getLexicalValue()));
					else 
						object = ResourceFactory.createPlainLiteral(ct.getObject().getConstant().getLexicalValue());
				}
				
				if(ct.getSubject().isVar() && ct.getSubject().getVar().getVarNum() < psi.getBindings().length && psi.getBinding(ct.getSubject().getVar().getVarNum()).isConstant()) {
					if(!psi.getBinding(ct.getSubject().getVar().getVarNum()).getConstant().isURI())
						throw new RuntimeException("ERROR: the subject of a triple cannot be a literal");
					subject = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(ct.getSubject().getVar().getVarNum()).getConstant().getLexicalValue()));
				}
				if(ct.getPredicate().isVar() && ct.getPredicate().getVar().getVarNum() < psi.getBindings().length && psi.getBinding(ct.getPredicate().getVar().getVarNum()).isConstant()) {
					if(!psi.getBinding(ct.getPredicate().getVar().getVarNum()).getConstant().isURI())
						throw new RuntimeException("ERROR: the predicate of a triple cannot be a literal");
					predicate = ResourceFactory.createProperty(model.expandPrefix(psi.getBinding(ct.getPredicate().getVar().getVarNum()).getConstant().getLexicalValue()));
				}
				if(ct.getObject().isVar() && ct.getObject().getVar().getVarNum() < psi.getBindings().length && psi.getBinding(ct.getObject().getVar().getVarNum()).isConstant()) {
					if(psi.getBinding(ct.getObject().getVar().getVarNum()).getConstant().isURI())
						object = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(ct.getObject().getVar().getVarNum()).getConstant().getLexicalValue()));
					else
						object = ResourceFactory.createPlainLiteral(psi.getBinding(ct.getObject().getVar().getVarNum()).getConstant().getLexicalValue());
				}
				
				Statement s = ResourceFactory.createStatement(subject, predicate, object);
				model.add(s);
				if(variant == 1) {
					if(object.equals(lambda)) {
						// if it lambda we know that the object is a variable
						if(psi.getBindings()[ct.getObject().getVar().getVarNum()].getVar().areLiteralsAllowed()) {
							// if literals are allowed in the object, add the lambda-literal placeholder
							model.add(ResourceFactory.createStatement(subject, predicate, lambdaLit));
						}
					}
				}
				
			}
		}
		
		//try {
		//	model.write(new FileOutputStream(new File(System.getProperty("user.dir") + "/resources/outputgraph.ttl")),"Turtle");
		//} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		return model;
	}
	
	public static String expandPrefix(String URI) {
		return prefixes.expandPrefix(URI);
	}
	
	public static String resolveLabelOfURI(String URI) {
		if(labelService != null && labelService.hasLabel(URI))
			return labelService.getLabel(URI);
		if(labelService != null && labelService.hasLabel(expandPrefix(URI)))
			return labelService.getLabel(expandPrefix(URI));
		//return URI;
		if(URI.contains("/")) return URI.substring(expandPrefix(URI).lastIndexOf("/") + 1);
		else return URI;
	}
	
	public static boolean isNumericDatatypeIRI(IRI iri) {
		if(iri.equals(XMLSchema.DECIMAL) || 
				iri.equals(XMLSchema.DOUBLE) || 
				iri.equals(XMLSchema.FLOAT) ||
				iri.equals(XMLSchema.INT) ||
				iri.equals(XMLSchema.INTEGER) ||
				iri.equals(XMLSchema.LONG) ||
				iri.equals(XMLSchema.NEGATIVE_INTEGER) ||
				iri.equals(XMLSchema.POSITIVE_INTEGER) ||
				iri.equals(XMLSchema.SHORT) ||
				iri.equals(XMLSchema.UNSIGNED_INT) ||
				iri.equals(XMLSchema.UNSIGNED_LONG) ||
				iri.equals(XMLSchema.UNSIGNED_SHORT) 
				)
			return true;
		return false;
	}
	
	public static String resolveLabelOfURIasURIstring(String URI) {
		String availableLabel = resolveLabelOfURI(URI);
		// camel case code from https://stackoverflow.com/questions/249470/javame-convert-string-to-camelcase
		StringBuffer result = new StringBuffer(availableLabel.length());
		String strl = availableLabel.toLowerCase();
		boolean bMustCapitalize = true;
		for (int i = 0; i < strl.length(); i++)
		{
		  char c = strl.charAt(i);
		  if (c >= 'a' && c <= 'z')
		  {
		    if (bMustCapitalize)
		    {
		      result.append(strl.substring(i, i+1).toUpperCase());
		      bMustCapitalize = false;
		    }
		    else
		    {
		      result.append(c);
		    }
		  }
		  else
		  {
		    bMustCapitalize = true;
		  }
		}
		availableLabel = result.toString().replaceAll("[^A-Za-z0-9]", "");
		if(availableLabel.length() < 15 && availableLabel.length() > 0) return availableLabel;
		else return URI.substring(URI.lastIndexOf("/") + 1);
	}
	
	public static String getSPARQLprefixes(Model m) {
		String prefixes = "";
		for(String key: m.getNsPrefixMap().keySet()) {
			prefixes += "PREFIX "+key+": <"+m.getNsPrefixMap().get(key)+">\n";
		}
		return prefixes;
	}
	
	public static String getSPARQLprefixes(ExternalDB eDB) {
		String prefixes = "";
		Map<String,String> namespaces = eDB.getNamespaces();
		for(String key: namespaces.keySet()) {
			prefixes += "PREFIX "+key+": <"+namespaces.get(key)+">\n";
		}
		return prefixes;
	}
	
	public static String getSPARQLdefaultPrefixes() {
		String prefixesString = "";
		for(String key : prefixes.getNsPrefixMap().keySet()) {
			prefixesString += "PREFIX "+key+": <"+prefixes.getNsPrefixMap().get(key)+">\n";
		}
		return prefixesString;
	}
	
	public static Model loadModel(String path) {
		Model model = ModelFactory.createDefaultModel();
		model.read(path) ;
		return model;
	}
	
	//part of the code taken from from http://www.javased.com/index.php?source_dir=Empire/jena/src/com/clarkparsia/empire/jena/util/JenaSesameUtils.java
	public static RDFNode asJenaNode(Value theValue) { 
		  if (theValue instanceof org.eclipse.rdf4j.model.Literal) { 
		   return asJenaLiteral( (org.eclipse.rdf4j.model.Literal) theValue); 
		  } 
		  else { 
		   return asJenaResource( (org.eclipse.rdf4j.model.Resource) theValue); 
		  } 
		 } 
	
	public static void loadLabelsFromModel(Model m) {
		Selector selector = new SimpleSelector(null, ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"), (String) null);
		StmtIterator iter = m.listStatements(selector);
		while(iter.hasNext()) {
			Statement s = iter.next();
			String uri = s.getSubject().getURI();
			String label = s.getObject().asLiteral().getLexicalForm();
			RDFUtil.labelService.setLabel(uri, label);
		}
	}
	
	public static Literal asJenaLiteral(org.eclipse.rdf4j.model.Literal theLiteral) { 
		  if (theLiteral == null) { 
		   return null; 
		  } 
		  else if (theLiteral.getLanguage() != null) { 
			  Optional<String> language = theLiteral.getLanguage();
			  if (language.isPresent()) return mInternalModel.createLiteral(theLiteral.getLabel(), language.get() );
			  else return mInternalModel.createLiteral(theLiteral.getLabel());
		  } 
		  else if (theLiteral.getDatatype() != null) { 
		   return mInternalModel.createTypedLiteral(theLiteral.getLabel(), 
		              theLiteral.getDatatype().toString()); 
		  } 
		  else { 
		   return mInternalModel.createLiteral(theLiteral.getLabel()); 
		  } 
		 } 
	public static Resource asJenaResource(org.eclipse.rdf4j.model.Resource theRes) { 
		  if (theRes == null) { 
		   return null; 
		  } 
		  else if (theRes instanceof IRI) { 
		   return asJenaIRI( (IRI) theRes); 
		  } 
		  else { 
		   return mInternalModel.createResource(new org.apache.jena.rdf.model.AnonId(((BNode) theRes).getID())); 
		  } 
		 }
	 public static Property asJenaIRI(IRI theIRI) { 
		  if (theIRI == null) { 
		   return null; 
		  } 
		  else { 
		   return mInternalModel.getProperty(theIRI.toString()); 
		  } 
		 } 
	 
	 public static int index = 0;
	 public static String getBlankNodeBaseURI() {
		 String baseURI = prefixes.getNsPrefixURI("blanknode")+index+"n"+new Date().getTime()+"v";
		 index++;
		 return baseURI;
	 }
	 
	public static String getBlankNodeOrNewVarString(String baseNew, int var) {
		if (baseNew == null)
			return "?blank"+var;
		else return "<"+baseNew+var+">";
	}
	
	public static boolean disableRedundancyCheck = false;
	
	public static int filterRedundantPredicates(Set<PredicateInstantiation> set, boolean strict, boolean onlyConstraintRedundant) {
		return filterRedundantPredicates(new HashSet<PredicateInstantiation>(), set, strict, onlyConstraintRedundant);
	}
	
	public static int filterRedundantPredicates(Set<PredicateInstantiation> set1, Set<PredicateInstantiation> set2, boolean strict, boolean onlyConstraintRedundant) {
		
		Set<PredicateInstantiation> toRemove = new HashSet<PredicateInstantiation>();
		int before = set1.size() + set2.size();
		for(PredicateInstantiation pi1: set1) {
			for (PredicateInstantiation pi2: set1) {
				if(!pi1.equals(pi2))
					toRemove.add(getRedundant(pi1,pi2, strict, onlyConstraintRedundant));
			}
		}
		for(PredicateInstantiation pi2rm: toRemove) {
			//System.out.println("REMOVING "+pi2rm);
			set1.remove(pi2rm);
			set2.remove(pi2rm);
		}
		for(PredicateInstantiation pi1: set1) {
			for (PredicateInstantiation pi2: set2) {
				toRemove.add(getRedundant(pi1,pi2, strict, onlyConstraintRedundant));
			}
		}
		for(PredicateInstantiation pi2rm: toRemove) {
			//System.out.println("REMOVING "+pi2rm);
			set1.remove(pi2rm);
			set2.remove(pi2rm);
		}
		/*for(PredicateInstantiation pi1: set2) {
			for (PredicateInstantiation pi2: set2) {
				if(!pi1.equals(pi2))
					toRemove.add(getRedundant(pi1,pi2, strict, onlyConstraintRedundant));
			}
		}*/
		toRemove.remove(null);
		for(PredicateInstantiation pi2rm: toRemove) {
			//System.out.println("REMOVING "+pi2rm);
			set1.remove(pi2rm);
			set2.remove(pi2rm);
		}
		return before - (set1.size() + set2.size());
	}
	
	
	public static int filterRedundantPredicatesOld(Set<PredicateInstantiation> set1, Set<PredicateInstantiation> set2, boolean strict, boolean onlyConstraintRedundant) {
		
		Set<PredicateInstantiation> toRemove = new HashSet<PredicateInstantiation>();
		int before = set1.size() + set2.size();
		for(PredicateInstantiation pi1: set1) {
			for (PredicateInstantiation pi2: set1) {
				if(!pi1.equals(pi2))
					toRemove.add(getRedundant(pi1,pi2, strict, onlyConstraintRedundant));
			}
			for (PredicateInstantiation pi2: set2) {
				toRemove.add(getRedundant(pi1,pi2, strict, onlyConstraintRedundant));
			}
		}
		for(PredicateInstantiation pi1: set2) {
			for (PredicateInstantiation pi2: set1) {
				toRemove.add(getRedundant(pi1,pi2, strict, onlyConstraintRedundant));
			}
			for (PredicateInstantiation pi2: set2) {
				if(!pi1.equals(pi2))
					toRemove.add(getRedundant(pi1,pi2, strict, onlyConstraintRedundant));
			}
		}
		toRemove.remove(null);
		for(PredicateInstantiation pi2rm: toRemove) {
			//System.out.println("REMOVING "+pi2rm);
			set1.remove(pi2rm);
			set2.remove(pi2rm);
		}
		return before - (set1.size() + set2.size());
	}
	
	private static PredicateInstantiation getRedundant(PredicateInstantiation pi1, PredicateInstantiation pi2, boolean strict, boolean onlyConstraintRedundant) {
		if(isSubsumedBy(pi1,pi2, strict) && isSubsumedBy(pi2,pi1, strict)) {
			// if the only difference is the additional constraints, return the one that has a subset of constraints compared to the other
			if(pi1.getAdditionalConstraints().containsAll(pi2.getAdditionalConstraints()))
				return pi1;
			if(pi2.getAdditionalConstraints().containsAll(pi1.getAdditionalConstraints()))
				return pi2;
			// if the set of constraints is different, then do not return either of them
			if(strict) return null;
			else {
				//if(onlyConstraintRedundant) {
					if(pi1.getAdditionalConstraints().size() < pi2.getAdditionalConstraints().size()) 
						return pi1;
					else if(pi1.getAdditionalConstraints().size() > pi2.getAdditionalConstraints().size()) 
						return pi2;
					// remove one of the two, it doesn't matter which one as long as it is always the same one
					if(pi1.toString().compareTo(pi2.toString()) < 0) 
						return pi1;
					else if(pi1.toString().compareTo(pi2.toString()) > 0) 
						return pi2;
					throw new RuntimeException("ERROR, two different predicates which appear to be identical");
					
				/*} else {
					return null;
				}*/
			}
		} else {			
			if(onlyConstraintRedundant) return null;
			if(isSubsumedBy(pi1,pi2, strict)) 
				return pi1;
			if(isSubsumedBy(pi2,pi1, strict)) 
				return pi2;
		}
		return null;
	}
	public static boolean isSubsumedBy(PredicateInstantiation pi1, PredicateInstantiation pi2, boolean strict) {
		// instantiations of different predicates cannot subsume each other
		if(! pi1.getPredicate().equals(pi2.getPredicate())) return false;
		for(int i = 0; i < pi1.getBindings().length; i++) {
			
			/*if(pi1.getPredicate().getName().equals("GeometryInGeometry") && pi1.getBindings()[i].isConstant() && pi1.getBindings()[i].getConstant().isURI() && pi1.getBindings()[i].getConstant().getLexicalValueExpanded().equals("<http://example.com/COconcentration>")) {
				System.out.println("");
			}*/
			
			if(! varIsSubsumedBy(pi1.getBindings()[i], pi2.getBindings()[i], strict)) return false;
		}
		return true;
	}
	private static boolean varIsSubsumedBy(Binding b1, Binding b2, boolean strict) {
		
		// if they are the same, the first one subsumes the other
		if(b1.equals(b2)) return true;
		// a constant is subsumed by a variable
		if(b1.isConstant() && b2.isVar()) return true;
		if(strict) {
			// different variables might result in different semantic interpretations
			if(b1.isVar() && b2.isVar() && !b1.equals(b2)) return false;
		} else {
			if(b1.isVar() && b2.isVar()) {
				// a variable that can only be a uri, or one that can also be a literal, 
				// is subsumed by one that can be uri or literal
				if(!b2.getVar().isSimpleVar() && b2.getVar().areLiteralsAllowed()) return true;
				// a var that can only be uri is subsumed by one that can only be uri
				if(!b1.getVar().isSimpleVar() && !b1.getVar().areLiteralsAllowed() && !b2.getVar().isSimpleVar() && !b2.getVar().areLiteralsAllowed()) return true;
				// a var which can be uri and literal is NOT subsumed by one that can only be uri
				return false;
			}
		}
		return false;
	}
	
	public static ConversionTriple applyMapping(ConversionTriple ct, QuerySolution mapping) {
		return new ConversionTripleImpl(applyMappingOnBinding(ct.getSubject(), mapping), 
				applyMappingOnBinding(ct.getPredicate(), mapping), 
				applyMappingOnBinding(ct.getObject(), mapping));
	}
	
	private static Binding applyMappingOnBinding(Binding b, QuerySolution mapping) {
		if(b.isConstant()) return b;
		else {
			if(mapping.contains("?v"+b.getVar().getVarNum())) {
				RDFNode newVal = mapping.get("?v"+b.getVar().getVarNum());
				logic.Resource res;
				if(newVal.isLiteral()) {
					res = new ResourceLiteral(newVal.asLiteral().getLexicalForm(), newVal.asLiteral().getDatatypeURI());
				} else if (newVal.isURIResource()) {
					res = new ResourceURI(newVal.asResource().getURI());
				} else throw new RuntimeException("ERROR, cannot handle here bindings to anything but literals and uris.");
				return new BindingImpl(res);
			}
		}
		return b;
	}
	
	public static Set<ConversionTriple> getModellingSchemaTriples(Set<PredicateInstantiation> schema, ConversionTriple mtq) {
		Set<ConversionTriple> matched = new HashSet<ConversionTriple>();
		for(PredicateInstantiation pi: schema) {
			for(ConversionTriple ct: pi.getPredicate().getRDFtranslation()) {
				ConversionTriple schemaTriple = ct.applyBinding(pi.getBindings());
				boolean isModelled = true;
				for(int i = 0; i< 3; i++) {
					Binding schemaElement = schemaTriple.get(i);
					Binding tripleElement = mtq.get(i);
					isModelled = isModelled && isModelledBy(tripleElement, schemaElement);
				}
				if(isModelled) {
					matched.add(schemaTriple);
				}
			}
		}
		return matched;
	}

	/**
	 * Return whether b1, element of a triple, is modelled by b2, element of a triple pattern
	 * @param b1
	 * @param b2
	 * @param strict
	 * @return
	 */
	public static boolean isModelledBy(Binding b1, Binding b2) {
	if(b1.isVar()) throw new RuntimeException("ERROR, a variable is not a valid element of a triple.");
	// if they are the same, the first one subsumes the other
	if(b1.equals(b2)) return true;
	// a constant is subsumed by a variable
	if(b1.isConstant() && b2.isVar()) {
		if(b1.getConstant().isURI()) return true;
		else {
			if(b2.getVar().isSimpleVar() || !b2.getVar().areLiteralsAllowed()) {
				// no literals allowed, so this variable cannot model the literal
				return false;
				// otherwise it can model literals, so return true
			} return true;
		}
	}
	if(b1.getConstant().getLexicalValueExpanded().equals(b2.getConstant().getLexicalValueExpanded())) return true;
	// if they are different constants then b2 cannot model b1
	return false;
}
	
}
