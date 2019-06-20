package shacl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import core.Element;
import core.Literal;
import core.Namespace;
import core.SearchUtil;
import core.Triple_Pattern;
import core.URI;
import core.Util;
import core.Variable;
import core.Variable_Instance;

public class Translator_to_Graph_Pattern {
	
	public static Schema translate(String SHACL, Set<URI> all_predicates){
		return translate(Util.unprefixedTurtleToModel(SHACL) , all_predicates);
	}
	
	public static Namespace sh = new Namespace("sh","http://www.w3.org/ns/shacl#");
	private static Namespace rdf = new Namespace("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	private static Namespace rdfs = new Namespace("rdfs","http://www.w3.org/2000/01/rdf-schema#");
	
	private static Property rdf_type = ResourceFactory.createProperty(rdf.getNamespace()+"type");
	private static Property sh_nodeKind = ResourceFactory.createProperty(sh.getNamespace()+"nodeKind");
	private static Property sh_property = ResourceFactory.createProperty(sh.getNamespace()+"property");
	private static Property sh_path = ResourceFactory.createProperty(sh.getNamespace()+"path");
	private static Property sh_inversePath = ResourceFactory.createProperty(sh.getNamespace()+"inversePath");
	private static Property sh_in = ResourceFactory.createProperty(sh.getNamespace()+"in");
	private static Property sh_hasValue = ResourceFactory.createProperty(sh.getNamespace()+"hasValue");
	private static Property sh_node = ResourceFactory.createProperty(sh.getNamespace()+"node");
	private static Property sh_class = ResourceFactory.createProperty(sh.getNamespace()+"class");
	private static Property sh_minCount = ResourceFactory.createProperty(sh.getNamespace()+"minCount");
	private static Property sh_targetNode = ResourceFactory.createProperty(sh.getNamespace()+"targetNode");
	private static Property sh_targetClass = ResourceFactory.createProperty(sh.getNamespace()+"targetClass");
	private static Property sh_targetObjectsOf = ResourceFactory.createProperty(sh.getNamespace()+"targetObjectsOf");
	private static Property sh_targetSubjectsOf = ResourceFactory.createProperty(sh.getNamespace()+"targetSubjectsOf");
	
	private static RDFNode sh_NodeShape = ResourceFactory.createResource(sh.getNamespace()+"NodeShape");
	private static RDFNode sh_IRIOrLiteral = ResourceFactory.createResource(sh.getNamespace()+"IRIOrLiteral");
	private static RDFNode sh_IRI = ResourceFactory.createResource(sh.getNamespace()+"IRI");
	
	public static Set<URI> impossiblePredicates;
	public static Schema translate(Model SHACL, Set<URI> all_predicates){
		impossiblePredicates = new HashSet<URI>();
		Set<Existential_Constraint> existentials = new HashSet<Existential_Constraint>();
		Set<Triple_Pattern> patterns_result = new HashSet<Triple_Pattern>();
		Set<Set<Triple_Pattern>> unique_restrictions = new HashSet<Set<Triple_Pattern>>();
		Set<Triple_Pattern> patterns_to_restrict = new HashSet<Triple_Pattern>();
		ResIterator shapes = SHACL.listResourcesWithProperty(rdf_type, sh_NodeShape);
		while(shapes.hasNext()) {
			Resource shape = shapes.next();
			//System.out.println("SHAPE "+shape);
			Set<Existential_Constraint> exs = new HashSet<Existential_Constraint>();
			exs.add(new Existential_Constraint());
			if(shape.hasProperty(sh_targetClass) || shape.hasProperty(sh_targetNode)) {
				processNodeShape(existentials, exs, new HashSet<Element>(), null, null, shape, SHACL, patterns_to_restrict, all_predicates, unique_restrictions);
				
			}
			if(shape.hasProperty(sh_targetObjectsOf) || shape.hasProperty(sh_targetSubjectsOf)) {
				
				processPredicateShape(existentials, exs, new HashSet<Element>(), null, null, shape, SHACL, patterns_to_restrict, all_predicates, unique_restrictions);
			}
			
		}
		
		
		patterns_result.addAll(patterns_to_restrict);
		
		// check whether the class restrictions are unique (e.g. they do not conflict with other classes or other restrictions)
		for(Set<Triple_Pattern> restriction : unique_restrictions) if(restriction != null){
			if(!SearchUtil.areTriplePatternsSubsumedBy( SearchUtil.searchByPredicate(restriction.iterator().next().getPredicate(),patterns_result), restriction)) 
				throw new SHACLTranslationException("ERROR, cannot create a schema graph that constrains relation on a class, because other objects are allowed more freedom with that relation.");
		}
		all_predicates.removeAll(impossiblePredicates);
		
		// check that all the consequences of the existentials are instantiatable
		
		for(Existential_Constraint ex :  existentials) {
			for(Triple_Pattern tp : ex.getConsequent()) {
				if(!SearchUtil.isTripleSubsumedByTriplePatterns(tp, patterns_result))
					throw new SHACLTranslationException("ERROR, there is an existential requirement that cannot be satisfied (e.g. a class requirement, when that class cannot be declared). More specifically: "+ex+ " \n (Offending triple: "+tp+")");
			}
		}
		
		return new Schema(patterns_result, existentials);
	}
	
	
	private static void processPredicateShape(Set<Existential_Constraint> completed_existentials, Set<Existential_Constraint> exs, Set<Element> ex_anchors, List<RDFNode> classes, List<RDFNode> targetNodes, Resource shape, Model SHACL, Set<Triple_Pattern> patterns_to_restrict, Set<URI> all_predicates, Set<Set<Triple_Pattern>> unique_restrictions) {
		URI path = null;
		boolean path_inverse = false;
		if(shape.asResource().hasProperty(sh_path)) {
			RDFNode pathObject = shape.asResource().getProperty(sh_path).getObject();
			if(pathObject.isAnon()) {
				path = new URI(shape.asResource().getProperty(sh_inversePath).getObject().asResource().getURI());
				path_inverse = true;
			} else {
				path = new URI(pathObject.asResource().getURI());
				path_inverse = false;
			}
			Set<Triple_Pattern> newPatternsToRestrict = allow_triples(false,path_inverse,path, new Variable(false) , patterns_to_restrict);
			patterns_to_restrict.clear(); patterns_to_restrict.addAll(newPatternsToRestrict);
		}
		
		// create the first anchor variable if this is a root shape
		if(ex_anchors.size() == 0) ex_anchors.add(new Variable_Instance(true,0));

		
		
		boolean inverse = shape.hasProperty(sh_targetSubjectsOf);
		List<RDFNode> predicates;
		if(inverse) predicates = SHACL.listObjectsOfProperty(shape,  sh_targetSubjectsOf).toList();
		else  predicates = SHACL.listObjectsOfProperty(shape, sh_targetObjectsOf).toList();
		if(path!= null) inverse = path_inverse;
		if(path!= null && predicates.size() > 0)
			throw new SHACLTranslationException("ERROR, a shape cannot refer to another shape with targets. This can be easily solved by referring to a copy of the shape without targets. ");
		if(predicates.size() == 0) {
			predicates.add(SHACL.getProperty(path.getURI()));
		}
		
		Set<Existential_Constraint> new_exs = new HashSet<Existential_Constraint>();
		Set<Element> new_ex_anchors = new HashSet<Element>();
		for(Existential_Constraint ex : exs) {
			if(ex_anchors.size() == 0) ex_anchors.add(new Variable_Instance(true,0));
			for(Element anchor : ex_anchors) {
				for(RDFNode pred : predicates) {
					if(inverse) {
						Variable new_anchor = new Variable_Instance(true, Util.getFreshVariableID_on_existentials(exs));
						new_exs.add(ex.newWithExtendedAntecedent( 
								new Triple_Pattern(new_anchor, new URI(pred.asResource().getURI()),anchor  )));
						new_ex_anchors.add(new_anchor);
					} else {
						Variable new_anchor = new Variable_Instance(false, Util.getFreshVariableID_on_existentials(exs));
						anchor.enforceNoLiteral();
						new_exs.add(ex.newWithExtendedAntecedent( 
								new Triple_Pattern(anchor, new URI(pred.asResource().getURI()), new_anchor )));
						new_ex_anchors.add(new_anchor);
					}

				}
			}
		}
		if(exs.size() == 0) {
			for(Element old_anchor : ex_anchors) {
				Existential_Constraint ex = new Existential_Constraint();
				if(path_inverse) {
					Variable new_anchor = new Variable_Instance(true, Util.getFreshVariableID_on_existentials(exs));
					new_exs.add(ex.newWithExtendedAntecedent( 
							new Triple_Pattern(new_anchor, path ,old_anchor  )));
					new_ex_anchors.add(new_anchor);
				} else {
					Variable new_anchor = new Variable_Instance(false, Util.getFreshVariableID_on_existentials(exs));
					old_anchor.enforceNoLiteral();
					new_exs.add(ex.newWithExtendedAntecedent( 
							new Triple_Pattern(old_anchor, path, new_anchor )));
					new_ex_anchors.add(new_anchor);
				}
			}
		}
		
		for(RDFNode pred : predicates) {
			// Record that we encoutered this predicate, so it should be allowed (unless somewhere else it is restricted)
			all_predicates.add(new URI(pred.asResource().getURI()));
			if(!inverse) {
				Set<Triple_Pattern> newPatternsToRestrict = allow_triples(false,inverse,new URI(pred.asResource().getURI()), new Variable(false) , patterns_to_restrict);
				patterns_to_restrict.clear(); patterns_to_restrict.addAll(newPatternsToRestrict);
			} else {
				Set<Triple_Pattern> newPatternsToRestrict = allow_triples(false,inverse,new URI(pred.asResource().getURI()), new Variable(true) , patterns_to_restrict);
				patterns_to_restrict.clear(); patterns_to_restrict.addAll(newPatternsToRestrict);
			}
			
			//max one sh:in
			if(shape.hasProperty(sh_in)) {
				List<RDFNode> list = (shape.getPropertyResourceValue(sh_in).as( RDFList.class )).asJavaList();
				if(classes != null) 
					unique_restrictions.add(allow_triples(false,inverse, new URI(pred.asResource().getURI()), convertListRDFNodeToSetOfElements(list) , null));
				if(targetNodes != null) {
					unique_restrictions.add(allow_triples(null,false,inverse, new URI(pred.asResource().getURI()), convertListRDFNodeToSetOfElements(list) , null));
				}
				if(classes == null && targetNodes == null) {
					Set<Triple_Pattern> newPatternsToRestrict = allow_triples(true,inverse,new URI(pred.asResource().getURI()), convertListRDFNodeToSetOfElements(list) , patterns_to_restrict);
					patterns_to_restrict.clear(); patterns_to_restrict.addAll(newPatternsToRestrict);
				}
			}
			if(shape.asResource().hasProperty(sh_nodeKind)) {
				RDFNode nodeKind = shape.asResource().getProperty(sh_nodeKind).getObject();
				if(inverse){
					Set<Triple_Pattern> newPatternsToRestrict = allow_triples(false,inverse,new URI(pred.asResource().getURI()), new Variable(true) , patterns_to_restrict);
					patterns_to_restrict.clear(); patterns_to_restrict.addAll(newPatternsToRestrict);
				} else {
					Set<Triple_Pattern> newPatternsToRestrict = allow_triples(false,inverse,new URI(pred.asResource().getURI()), new Variable(false) , patterns_to_restrict);
					patterns_to_restrict.clear(); patterns_to_restrict.addAll(newPatternsToRestrict);
				}
				if(nodeKind.equals(sh_IRI)) {
					if(classes != null) 
						unique_restrictions.add(allow_triples(false,inverse, new URI(pred.asResource().getURI()), new Variable(true) , null));
					if(targetNodes != null) {
						unique_restrictions.add(allow_triples(false,inverse, new URI(pred.asResource().getURI()), new Variable(true) , null));
					}
					if(classes == null && targetNodes == null) {
						Set<Triple_Pattern> newPatternsToRestrict = allow_triples(true,inverse,new URI(pred.asResource().getURI()), new Variable(true) , patterns_to_restrict);
						patterns_to_restrict.clear(); patterns_to_restrict.addAll(newPatternsToRestrict);
					}
				} else if(nodeKind.equals(sh_IRIOrLiteral)) {
					if(classes == null && targetNodes == null) {
						if(!inverse) {
							Set<Triple_Pattern> newPatternsToRestrict = allow_triples(true,inverse,new URI(pred.asResource().getURI()), new Variable(false) , patterns_to_restrict);
							patterns_to_restrict.clear(); patterns_to_restrict.addAll(newPatternsToRestrict);
						} else {
							Set<Triple_Pattern> newPatternsToRestrict = allow_triples(true,inverse,new URI(pred.asResource().getURI()), new Variable(true) , patterns_to_restrict);
							patterns_to_restrict.clear(); patterns_to_restrict.addAll(newPatternsToRestrict);
						}
					}
				}
			}
			if(shape.hasProperty(sh_class)) {
				RDFNode nodeClass = shape.asResource().getProperty(sh_class).getObject();
				Element class_value = new URI(nodeClass.asResource().getURI());
				for(Existential_Constraint ex : exs) {
					int newVarID = Util.getFreshVariableID_on_existentials(exs);
					for(Element anchor : ex_anchors) {
						Existential_Constraint ex1 = ex.newWithExtendedAntecedent( 
								inverse ? new Triple_Pattern(new Variable_Instance(true, newVarID), new URI(pred.asResource().getURI()), anchor) 
										: new Triple_Pattern(anchor, new URI(pred.asResource().getURI()), new Variable_Instance(true, newVarID))
										);
						completed_existentials.add(ex1.newWithExtendedConsequent( 
								new Triple_Pattern(new Variable_Instance(true, newVarID), new URI(rdf_type.getURI()), class_value)  ));
					}
				}	
				// we also need to record that the object/subject must be of a URI
				//if(classes != null || targetNodes != null) {
				{
					Set<Triple_Pattern> newPatternsToRestrict = allow_triples(true,inverse, new URI(pred.asResource().getURI()), new Variable(true) , patterns_to_restrict);
					patterns_to_restrict.clear(); patterns_to_restrict.addAll(newPatternsToRestrict);
					newPatternsToRestrict = allow_triples(true,!inverse, new URI(pred.asResource().getURI()), new Variable(true) , patterns_to_restrict);
					patterns_to_restrict.clear(); patterns_to_restrict.addAll(newPatternsToRestrict);
				}
				//	}
				// we should also record that the anchor must be a URI
				/*if(targetNodes != null) {
					unique_restrictions.add(allow_triples(false,inverse, new URI(pred.asResource().getURI()), new Variable(true) , null));
				}*/
				if(classes == null && targetNodes == null) {
					Set<Triple_Pattern> newPatternsToRestrict = allow_triples(true,inverse,new URI(pred.asResource().getURI()), new Variable(true) , patterns_to_restrict);
					patterns_to_restrict.clear(); patterns_to_restrict.addAll(newPatternsToRestrict);
				}
				
				// we need to allow for RDF type predicates
				Set<Triple_Pattern> newPatternsToRestrict = allow_triples(false,false,new URI(rdf, "type"),new Variable(false), patterns_to_restrict);
				patterns_to_restrict.clear(); patterns_to_restrict.addAll(newPatternsToRestrict);
				all_predicates.add(new URI(rdf, "type"));
				
			}
			if(shape.hasProperty(sh_hasValue)) {
				RDFNode nodeKind = shape.asResource().getProperty(sh_hasValue).getObject();
				Element value = nodeKind.isLiteral() ? new Literal(nodeKind.asLiteral().getLexicalForm()) : new URI(nodeKind.asResource().getURI());
				for(Existential_Constraint ex : exs) {
					for(Element anchor : ex_anchors) {
						completed_existentials.add(ex.newWithExtendedConsequent(
								inverse ? new Triple_Pattern(value, new URI(pred.asResource().getURI()), anchor) 
										: new Triple_Pattern(anchor, new URI(pred.asResource().getURI()), value)
										));
					}
				}	
			}
			if(shape.hasProperty(sh_minCount)) {
				RDFNode nodeKind = shape.asResource().getProperty(sh_minCount).getObject();
				if(!nodeKind.isLiteral() || nodeKind.asLiteral().getInt() != 1) throw new SHACLTranslationException("ERROR: cannot deal with values of the minCount property different from 1");
				Element value = nodeKind.isLiteral() ? new Literal(nodeKind.asLiteral().getLexicalForm()) : new URI(nodeKind.asResource().getURI());
				for(Existential_Constraint ex : exs) {
					for(Element anchor : ex_anchors) {
						completed_existentials.add(ex.newWithExtendedConsequent(
								inverse ? new Triple_Pattern(new Variable_Instance(true, Util.getFreshVariableID_on_existentials(exs)), new URI(pred.asResource().getURI()), anchor) 
										: new Triple_Pattern(anchor, new URI(pred.asResource().getURI()), new Variable_Instance(false, Util.getFreshVariableID_on_existentials(exs)))
										));
					}
				}	
			}
			
			List<RDFNode> nodeShapes = 	SHACL.listObjectsOfProperty(shape, sh_node).toList();
			for(RDFNode nodeShape : nodeShapes) {
				processNodeShape(completed_existentials, new_exs, new_ex_anchors, classes, targetNodes, nodeShape.asResource(), SHACL, patterns_to_restrict, all_predicates, unique_restrictions);
			}
			
			
			
			List<RDFNode> propertyShapes = 	SHACL.listObjectsOfProperty(shape, sh_property).toList();
			for(RDFNode propShape : propertyShapes) {
				
				processPredicateShape(completed_existentials, new_exs, new_ex_anchors, classes, targetNodes, propShape.asResource(), SHACL, patterns_to_restrict, all_predicates, unique_restrictions);
			}
		}
	}
	
	private static void processNodeShape(Set<Existential_Constraint> completed_existentials, Set<Existential_Constraint> exs, Set<Element> ex_anchors, List<RDFNode> classes, List<RDFNode> targetNodes, Resource shape, Model SHACL, Set<Triple_Pattern> patterns_to_restrict, Set<URI> all_predicates, Set<Set<Triple_Pattern>> unique_restrictions) {
		if((shape.hasProperty(sh_targetClass) ||  shape.hasProperty(sh_targetNode)) && (classes!= null || targetNodes != null)) 
			throw new SHACLTranslationException("ERROR, a shape cannot refer to another shape with targets. This can be easily solved by referring to a copy of the shape without targets. ");
		if(shape.hasProperty(sh_targetClass)) classes = SHACL.listObjectsOfProperty(shape, sh_targetClass).toList();
		if(shape.hasProperty(sh_targetNode)) targetNodes = SHACL.listObjectsOfProperty(shape, sh_targetNode).toList();
		
		Set<Existential_Constraint> new_exs = new HashSet<Existential_Constraint>();
		Set<Element> new_ex_anchors = new HashSet<Element>();
		if(classes == null && ex_anchors.size() > 0) {
			for(Existential_Constraint ex : exs) {
				new_exs.add(new Existential_Constraint(ex.getAntecedent(),ex.getConsequent()));
			}
			new_ex_anchors.addAll(ex_anchors);
		} else for(Existential_Constraint ex : exs) {
			if(ex_anchors.size() == 0) ex_anchors.add(new Variable_Instance(true,0));
			for(Element anchor : ex_anchors) {
				if(classes != null) for(RDFNode clas : classes) {
					new_exs.add(ex.newWithExtendedAntecedent( 
							new Triple_Pattern(anchor, new URI(rdf, "type"), new URI(clas.asResource().getURI()))));
					new_ex_anchors.add(anchor);
				}
				if(targetNodes != null) for(RDFNode targetNode : targetNodes) {
					if(targetNode.isLiteral()) {
						new_ex_anchors.add(new Literal(targetNode.asLiteral().getLexicalForm()));
					} else {
						new_ex_anchors.add(new URI(targetNode.asResource().getURI()));
					}
				} 
			}
		}
		
		
		if(classes != null) /*for(RDFNode clss : classes)*/ {
			Set<Triple_Pattern> newPatternsToRestrict = allow_triples(false,false,new URI(rdf, "type"),new Variable(false), patterns_to_restrict);
			patterns_to_restrict.clear(); patterns_to_restrict.addAll(newPatternsToRestrict);
			all_predicates.add(new URI(rdf, "type"));
		}
		List<RDFNode> propertyShapes = 	SHACL.listObjectsOfProperty(shape, sh_property).toList();
		for(RDFNode propShape : propertyShapes) {
			
			processPredicateShape(completed_existentials, new_exs, new_ex_anchors, classes, targetNodes, propShape.asResource(), SHACL, patterns_to_restrict, all_predicates, unique_restrictions);
		}
	}
	
	public static Set<Element> convertListRDFNodeToSetOfElements(List<RDFNode> list) {
		Set<Element> elements = new HashSet<Element>();
		for(RDFNode node : list) {
			if(node.isLiteral()) elements.add(new Literal(node.asLiteral().getString()));
			else elements.add(new URI(node.asResource().getURI()));
		}
		return elements;
	}
	
	/**
	 * 
	 * @param restrict true to add a restriction on the predicates, false to allow
	 * @return
	 */
	private static Set<Triple_Pattern> allow_triples(boolean restrict, boolean inverse, URI predicate, Set<Element> values, Set<Triple_Pattern> patterns){
		return allow_triples(null, restrict, inverse, predicate, values, patterns);
	}
	private static Set<Triple_Pattern> allow_triples(Element baseValue, boolean restrict, boolean inverse, URI predicate, Set<Element> values, Set<Triple_Pattern> patterns){
		Set<Triple_Pattern> result_patterns = new HashSet<Triple_Pattern>();
		Set<Triple_Pattern> existing_patterns = new HashSet<Triple_Pattern>();
		Set<Triple_Pattern> new_patterns = new HashSet<Triple_Pattern>();
		if(impossiblePredicates.contains(predicate)) return patterns;
		Element baseValueReal = baseValue;
		if(baseValueReal == null) {
				baseValueReal = new Variable(!inverse);
		}
		for(Element el : values) {
			if(!inverse) {
				new_patterns.add(new Triple_Pattern(baseValueReal, predicate, el));
			} else {
				new_patterns.add(new Triple_Pattern(el, predicate, baseValueReal));
			}
		}
		if(patterns == null) return new_patterns;
		// first check what is already available
		for(Triple_Pattern tp : patterns) {
			if(!tp.getPredicate().equals(predicate)) {
				// do not modify patterns with other predicates
				result_patterns.add(tp);
			} else {
				existing_patterns.add(tp);
			}
		}
		if(existing_patterns.size() == 0) {
			// if it is the first time we get this predicate, we can just allow it as it is
			result_patterns.addAll(new_patterns);
		} else {
			if (restrict){
				// if this predicate already had a restriction, but we need to restrict it further, then compute the further restriction
				Set<Triple_Pattern> difference_patterns = Util.computeSetDifferenceOfTriplePatternsWithSamePredicate(existing_patterns, new_patterns);
				if(difference_patterns.size() == 0) {
					impossiblePredicates.add(predicate);
				}
				result_patterns.addAll(difference_patterns);
			} else {
				result_patterns.addAll(existing_patterns);
			}
		
		}
		return result_patterns;
	}
	private static Set<Triple_Pattern> allow_triples(Element baseValue, boolean restrict, boolean inverse, URI predicate, Element value, Set<Triple_Pattern> patterns){
		Set<Element> singletonSet = new HashSet<Element>();
		singletonSet.add(value);
		return allow_triples(baseValue, restrict, inverse, predicate,singletonSet, patterns);
	}
	private static Set<Triple_Pattern> allow_triples(boolean restrict, boolean inverse, URI predicate, Element value, Set<Triple_Pattern> patterns){
		Set<Element> singletonSet = new HashSet<Element>();
		singletonSet.add(value);
		return allow_triples(restrict, inverse, predicate,singletonSet, patterns);
	}
	
/*	private static Set<Triple_Pattern> process_restrictions(Set<Triple_Pattern> patterns_to_restrict, List<RDFNode> list, URI predicate) {
		Set<Triple_Pattern> new_patterns_to_restrict = new HashSet<Triple_Pattern>();
		for(Triple_Pattern tp : patterns_to_restrict) {
			if(!tp.getPredicate().equals(predicate)) {
				new_patterns_to_restrict.add(tp);
			} else {
				if(tp.getObject().equals(new Variable(false))) {
					//skip
				} else {
					throw new RuntimeException("ERROR, cannot deal with these constraints");
				}
			}
		}
		return new_patterns_to_restrict;
	}
	
	private static Set<Triple_Pattern> process_restrictions(Set<Triple_Pattern> patterns_to_restrict, URI predicate) {
		Set<Triple_Pattern> new_patterns_to_restrict = new HashSet<Triple_Pattern>();
		boolean add_with_variable = true;
		for(Triple_Pattern tp : patterns_to_restrict) {
			new_patterns_to_restrict.add(tp);
			if(!tp.getPredicate().equals(predicate)) {
			} else {
				add_with_variable = false;
			}
		}
		if(add_with_variable) 
			new_patterns_to_restrict.add(new Triple_Pattern(new Variable(true), new URI(rdf, "type"), new Variable(false)));
		return new_patterns_to_restrict;
	}*/
}
