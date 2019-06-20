package shacl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import benchmarking.GeneratorUtil;
import core.Element;
import core.Literal;
import core.Rule;
import core.Triple_Pattern;
import core.URI;
import core.Util;
import core.Variable;
import core.Variable_Instance;
import logic.Binding;
import logic.BindingImpl;
import logic.Predicate;
import logic.PredicateExpansion;
import logic.PredicateExpansionBySPARQLquery;
import logic.PredicateInstantiation;
import logic.PredicateInstantiationImpl;
import logic.PredicateTemplate;
import logic.PredicateUtil;
import logic.RDFUtil;
import logic.ResourceLiteral;
import logic.ResourceURI;
import logic.RuleImpl;
import logic.StatRecorder;
import logic.VariableImpl;

public class Existential_Validator {

	//A
	public static Set<Existential_Constraint> validate(Schema schema, Set<Rule> rules){
		Set<PredicateInstantiation> s_plus = expandSchema(schema, rules);
		Set<Triple_Pattern> s_plus_as_TPs = util_translate_PredicateInstantiation_2_Triple_Patterns(s_plus);
				
		Set<Existential_Constraint> invalids = new HashSet<Existential_Constraint>();
		for(Existential_Constraint ex : schema.getSchema_Existentials()) {
			if(!invalids.contains(ex)) {
				invalids.addAll(validate(schema, ex,rules,s_plus_as_TPs));
			}
		}
		return invalids;
	}
	//B
	public static Set<Existential_Constraint> validate(Schema schema, Existential_Constraint ex, Set<Rule> rules, Set<Triple_Pattern> s_plus){
		Set<Existential_Constraint> violated_constraints = new HashSet<Existential_Constraint>();
		Set<Map<Integer, Element>> mappings = Util.evaluate(Util.query_expansion(ex.getAntecedent()),sandboxGraphOf(s_plus));
		for(Map<Integer, Element> m : mappings) {
			// if there is a mapping, then we need to check whether the new facts can cause the violation of the existential rule
			violated_constraints.addAll(validate_with_mapping(schema, ex, rules, s_plus, m));
		}
		return violated_constraints;
	}
	//C
	public static Set<Existential_Constraint> validate_with_mapping(Schema schema, Existential_Constraint ex, Set<Rule> rules, Set<Triple_Pattern> s_plus, Map<Integer, Element> m){
		Set<Existential_Constraint> violated_constraints = new HashSet<Existential_Constraint>();
		Set<Triple_Pattern> I_plus = new HashSet<Triple_Pattern>();
		Set<Triple_Pattern> I_plus_considered = new HashSet<Triple_Pattern>();
		Set<Triple_Pattern> I_minus = new HashSet<Triple_Pattern>();
		I_plus = Util.apply_mapping(false, m,ex.getAntecedent()); // do not retain mappings, to keep some orginal variables
		Set<Triple<Set<Triple_Pattern>,Set<Triple_Pattern>,Set<Triple_Pattern>>> I_trips = new HashSet<Triple<Set<Triple_Pattern>,Set<Triple_Pattern>,Set<Triple_Pattern>>>();
		
/*		while(I_plus.size() > 0) {
			Triple_Pattern t_plus =  I_plus.iterator().next();
			for(Rule r : rules) {
				if(  Util.is_instance_of(sandboxTripleOf(t_plus),r.getConsequent())) {
					validate_with_mapping_and_rule(s_plus, I_plus,I_plus_considered,I_minus,t_plus,r);
				}
			}
			I_plus.remove(t_plus);
			I_plus_considered.add(t_plus);
		}*/
		
		I_trips = get_all_possible_derivations(0,I_plus, new ImmutableTriple<Set<Triple_Pattern>,Set<Triple_Pattern>,Set<Triple_Pattern>>(I_plus,I_plus_considered,I_minus), rules,s_plus, m, ex, schema);
		
		for(Triple<Set<Triple_Pattern>,Set<Triple_Pattern>,Set<Triple_Pattern>> trip : I_trips) {
			
			Set<Triple_Pattern> I_minus_local = trip.getRight();
			Set<Triple_Pattern> I_plus_considered_local = trip.getMiddle();
			
			Set<Map<Integer,Element>> possible_mappings = 
					Util.filter_mappings_to_lambda(
							Util.evaluate(Util.query_expansion(I_minus_local), 
									sandboxGraphOf(schema.getSchema_Graph())));
			for(Map<Integer,Element> mp : possible_mappings) {
				Set<Triple_Pattern> I_minus_local2 = Util.apply_mapping(mp, I_minus_local);
				// extend with existentials to ensure that it is a valid instance
				I_minus_local2 = extend_with_existential_constraints(schema.getSchema_Existentials(), I_minus_local2);
				
				// change each variable into a new URI to generate a graph without variables
				Set<Triple_Pattern> I = new HashSet<Triple_Pattern>();
				I.addAll(I_minus_local2);
				I.addAll(I_plus_considered_local);
				Set<Triple_Pattern> grounded_triples = Util.ground_with_fresh_variables(I); 
				
				// compute the closure on these patterns
				grounded_triples = Util.closure(grounded_triples, rules);
				
				if(!Util.is_existential_constraint_valid(ex,grounded_triples))
					violated_constraints.add(ex);
			}
			
			
		}
		
		return violated_constraints;
	}
	
	
	public static Set<Triple<Set<Triple_Pattern>,Set<Triple_Pattern>,Set<Triple_Pattern>>> get_all_possible_derivations( int inference_number,
			Set<Triple_Pattern> original_I_plus, Triple<Set<Triple_Pattern>,Set<Triple_Pattern>,Set<Triple_Pattern>> I_trip, Set<Rule> rules,Set<Triple_Pattern> s_plus, Map<Integer, Element> m, Existential_Constraint ex, Schema s){
		
		Set<Triple<Set<Triple_Pattern>,Set<Triple_Pattern>,Set<Triple_Pattern>>> result = new HashSet<Triple<Set<Triple_Pattern>,Set<Triple_Pattern>,Set<Triple_Pattern>>>();
			Set<Triple_Pattern> I_plus_orig = I_trip.getLeft();
/*			Set<Triple_Pattern> I_plus = new HashSet<Triple_Pattern>();
			Set<Triple_Pattern> I_plus_considered = new HashSet<Triple_Pattern>();
			Set<Triple_Pattern> I_minus = new HashSet<Triple_Pattern>(); */		
			if(I_plus_orig.size() > 0) {
				Triple_Pattern t_plus =  I_plus_orig.iterator().next();
				// possible origin from rule
				for(Rule r : rules) {
					// for each mapping
					if(  Util.is_instance_of(sandboxTripleOf(t_plus),r.getConsequent())) {
						Set<Triple_Pattern> I_plus = clone_set(I_trip.getLeft());
						Set<Triple_Pattern> I_plus_considered = clone_set(I_trip.getMiddle());
						Set<Triple_Pattern> I_minus = clone_set(I_trip.getRight());
						boolean possible_rule_application = validate_with_mapping_and_rule(s, s_plus, I_plus,I_plus_considered,I_minus,t_plus,r);
						if(possible_rule_application) {
							I_plus.remove(t_plus);
							I_plus_considered.add(t_plus);
							
							boolean has_alternative_solution = false;
							for(Triple_Pattern tp : s.getSchema_Graph()) {
								if(  Util.is_instance_of(sandboxTripleOf(t_plus),tp)) {
									has_alternative_solution = true;
									}
								}
							if(inference_number < 7 || !has_alternative_solution) {
								if(inference_number > 200) {
									throw new CyclicRulesException("ERROR, the validation of this schema seems to be stuck in an infinite loop");
								}
								result.addAll(get_all_possible_derivations(++inference_number, original_I_plus, new ImmutableTriple<Set<Triple_Pattern>,Set<Triple_Pattern>,Set<Triple_Pattern>>(I_plus, I_plus_considered, I_minus), rules, s_plus, m, ex, s));
							}
						}
					}
				}
				// possible origin from original schema 
				if(!original_I_plus.contains(t_plus)) // exclude the original triples of the t_plus that, by assumption are the ones that we just inferred
					for(Triple_Pattern tp : s.getSchema_Graph()) {
					// for each mapping
					if(  Util.is_instance_of(sandboxTripleOf(t_plus),tp)) {
						Set<Triple_Pattern> I_plus = clone_set(I_trip.getLeft());
						Set<Triple_Pattern> I_plus_considered = clone_set(I_trip.getMiddle());
						Set<Triple_Pattern> I_minus = clone_set(I_trip.getRight());
						I_minus.add(t_plus);
						I_plus.remove(t_plus);
						I_plus_considered.add(t_plus); //TODO redundant? but should be ok, right?
						result.addAll(get_all_possible_derivations(inference_number,original_I_plus, new ImmutableTriple<Set<Triple_Pattern>,Set<Triple_Pattern>,Set<Triple_Pattern>>(I_plus, I_plus_considered, I_minus), rules, s_plus, m, ex, s));
					}
				}
				return result;
			} else {
				// TODO probably unnecessary cloning
				Set<Triple_Pattern> I_plus = clone_set(I_trip.getLeft());
				Set<Triple_Pattern> I_plus_considered = clone_set(I_trip.getMiddle());
				Set<Triple_Pattern> I_minus = clone_set(I_trip.getRight());
				result.add(new ImmutableTriple<Set<Triple_Pattern>,Set<Triple_Pattern>,Set<Triple_Pattern>>(I_plus, I_plus_considered, I_minus));
			}
		return result;
	}
	
	public static Set<Triple_Pattern> clone_set(Set<Triple_Pattern> set){
		Set<Triple_Pattern> clone = new HashSet<Triple_Pattern>();
		clone.addAll(set);
		return clone;
	}
	
	public static Set<Triple_Pattern> extend_with_existential_constraints(Set<Existential_Constraint> constraints, Set<Triple_Pattern> I_minus) {
		
		boolean no_more_applications = false;
		while(!no_more_applications) {
			no_more_applications = true;
			for(Existential_Constraint ex : constraints) {
				Set<Triple_Pattern> new_I_minus = new HashSet<Triple_Pattern>();
				for(Triple_Pattern tp : I_minus) {
					Map<Integer, Element> mapping = Util.evaluate_join_aware_on_triple(ex.getSingletonAntecedent(),tp);
					if(mapping != null) {
						new_I_minus.addAll(Util.apply_mapping(mapping, ex.getConsequent()));
					}
				}
				if(I_minus.addAll(new_I_minus))
					no_more_applications = false;
			}
		}
		
		return I_minus;
	}
	
	
	public static boolean validate_with_mapping_and_rule(Schema s, Set<Triple_Pattern> s_plus, Set<Triple_Pattern> I_plus, Set<Triple_Pattern> I_plus_considered, Set<Triple_Pattern> I_minus, Triple_Pattern t_plus, Rule r){
		Map<Integer, Element> mapping = Util.evaluate_join_aware_on_triple(r.getConsequent(),t_plus);
		// uniquify rule's variables
		Set<Triple_Pattern> I =  new HashSet<Triple_Pattern>();
		I.addAll(I_minus);
		I.addAll(I_plus);
		I.addAll(I_plus_considered);
		for(Triple_Pattern t : r.getAntecedent()) {
			for(int i = 0; i < 3; i += 2) {
				if(t.get(i) instanceof Variable_Instance && !mapping.containsKey(new Integer(((Variable_Instance)t.get(i)).getIndex())) ) {
					mapping.put(new Integer(((Variable_Instance)t.get(i)).getIndex()), 
							new Variable_Instance(Util.occurs_in_subject_or_object_pos(t.get(i), r.getAntecedent()) || i == 0 || i == 1, Util.getFreshVariableID_on_patterns(I)));
				}
			}
		}
		
		for(Triple_Pattern t : r.getAntecedent()) {
			Triple_Pattern mt = Util.apply_mapping(mapping, t); 
			Triple_Pattern mt_sandbox = sandboxTripleOf(mt); 
			if(Util.is_instance_of(mt_sandbox, s_plus) ) {
				if(!I_plus_considered.contains(mt))
					I_plus.add(mt);
			} else {
				//TODO can probably stop here if the rule is not applicable
				//if(Util.is_instance_of(mt_sandbox, s.getSchema_Graph()) ) {
					I_minus.add(mt);
				//} else 
				//	return false;\\\\
			}
		}
		return true;
	}

	public static Binding[] util_extract_binding_from_Triple_Pattern(Triple_Pattern tp) {
		Binding[] bindings = new Binding[2];
		if(tp.getSubject() instanceof URI) {
			bindings[0] = 	new BindingImpl(new ResourceURI( ((URI) tp.getSubject()).getURI() ));		
		} else if(tp.getSubject() instanceof Literal) {
			bindings[0] = 	new BindingImpl(new ResourceLiteral( ((Literal) tp.getSubject()).getValue(), XMLSchema.STRING));		
		} else if(tp.getSubject() instanceof Variable) {
			if(tp.getSubject() instanceof Variable_Instance) {
				bindings[0] = new BindingImpl(new VariableImpl(( (Variable_Instance) tp.getSubject()).getIndex(), !( (Variable_Instance) tp.getSubject()).isNoLit()));
			} else {
				bindings[0] = new BindingImpl(new VariableImpl(0, !( (Variable) tp.getSubject()).isNoLit()));
			}
		} else throw new RuntimeException("ERROR, wrong element for a predicate instantiation");
		if(tp.getObject() instanceof URI) {
			bindings[1] = 	new BindingImpl(new ResourceURI( ((URI) tp.getObject()).getURI() ));		
		} else if(tp.getObject() instanceof Literal) {
			bindings[1] = 	new BindingImpl(new ResourceLiteral( ((Literal) tp.getObject()).getValue(), XMLSchema.STRING));		
		} else if(tp.getObject() instanceof Variable) {
			if(tp.getObject() instanceof Variable_Instance) {
				bindings[1] = new BindingImpl(new VariableImpl(( (Variable_Instance) tp.getObject()).getIndex(), !( (Variable) tp.getObject()).isNoLit()));
			} else {
				bindings[1] = new BindingImpl(new VariableImpl( 1, !( (Variable) tp.getObject()).isNoLit()));
	
			}
		} else throw new RuntimeException("ERROR, wrong element for a predicate instantiation");		
		return bindings;
	}
	public static Element util_Binding_2_Element(Binding b) {
		if(b.isVar()) {
			return new Variable_Instance(!b.getVar().areLiteralsAllowed(), b.getVar().getVarNum());
		} else {
			if(b.getConstant().isLiteral()) {
				return new Literal(  ((ResourceLiteral)b.getConstant()).getLexicalValue()  );
			} else {
				return new URI(  ((ResourceURI)b.getConstant()).getURI()  );
			}
		}
	}
	
	public static Set<PredicateInstantiation> expandSchema(Schema schema, Set<Rule> ruleset) {
		Map<String,String> prefixes = GeneratorUtil.getStandardPrefixes();
		Set<PredicateInstantiation> schema_triples = util_Triple_Patterns_2_Predicates(schema);
		Set<logic.Rule> predicateRules = util_Rules_2_Predicate_Rules(ruleset);
		PredicateExpansion expansion = new PredicateExpansionBySPARQLquery(known_predicates, predicateRules);
		expansion.setPrefixes(prefixes);
		StatRecorder sr = new StatRecorder();
		Set<PredicateInstantiation> expanded_schema = expansion.expand(3, schema_triples,sr);
		return sr.inferrable_triples;
	}
	
	public static Set<Triple_Pattern> util_translate_PredicateInstantiation_2_Triple_Patterns(Set<PredicateInstantiation> predicates){
		 Set<Triple_Pattern> patterns = new HashSet<Triple_Pattern>();
		for(PredicateInstantiation pi : predicates) {
			patterns.add(util_Predicate_2_Triple_pattern(pi));
		}
		return patterns;
	}

	public static Triple_Pattern util_Predicate_2_Triple_pattern(PredicateInstantiation pi) {
		String predicateName = pi.getPredicate().getName();	
		if(predicateName.indexOf("http") != 0) predicateName = Util.ns.getNamespace() + predicateName;
		return new Triple_Pattern(
				util_Binding_2_Element(pi.getBinding(0)),
				new URI(predicateName),
				util_Binding_2_Element(pi.getBinding(1))
				);
	}
	public static PredicateInstantiation util_Triple_Pattern_2_Predicates(Triple_Pattern tp) {
		if(!PredicateUtil.containsOne(tp.getPredicate().getURI(), 2, known_predicates)) {
			known_predicates.add(GeneratorUtil.instantiateBinaryPredicateIfNecessary(tp.getPredicate().getURI()));
		}
		Binding[] bindings = util_extract_binding_from_Triple_Pattern(tp);		
		String predicateName = tp.getPredicate().getURI();	
			
		return new PredicateInstantiationImpl(PredicateUtil.get(predicateName, 2, known_predicates), bindings);
	}

	public static Set<PredicateInstantiation> util_Triple_Patterns_2_Predicates(Schema schema) {
		Set<PredicateInstantiation> predicates = new HashSet<PredicateInstantiation>();
		for(Triple_Pattern tp : schema.getSchema_Graph()) {
			predicates.add(util_Triple_Pattern_2_Predicates(tp));
		}
		return predicates;
	}

	public static PredicateInstantiation util_helper_process_atom(Triple_Pattern tp){
		Predicate p = GeneratorUtil.instantiateBinaryPredicateIfNecessary(tp.getPredicate().getURI());
		known_predicates.add(p);
		return new PredicateInstantiationImpl(p, util_extract_binding_from_Triple_Pattern(tp));
	}

	public static Set<PredicateInstantiation> util_helper_process_antecedent(Set<Triple_Pattern> antecedent){
		Set<PredicateInstantiation> predicates = new HashSet<PredicateInstantiation>();
		for(Triple_Pattern tp : antecedent) {
			predicates.add(util_helper_process_atom(tp));
		}
		return predicates;
	}

	public static Set<PredicateTemplate> util_helper_process_head(Triple_Pattern consequent){
		return GeneratorUtil.asSetOfPredicateTemplates(util_helper_process_atom(consequent));
	}

	public static logic.Rule util_Rule_2_Predicate_Rules(Rule r){
		Set<PredicateTemplate> head = util_helper_process_head(r.getConsequent());
		Set<PredicateInstantiation> antecedent = util_helper_process_antecedent(r.getAntecedent());
		return new RuleImpl(antecedent, head);
	}

	public static Set<logic.Rule> util_Rules_2_Predicate_Rules(Set<Rule> ruleset){
		Set<logic.Rule> rules = new HashSet<logic.Rule>();
		for(Rule r : ruleset) {
			rules.add(util_Rule_2_Predicate_Rules(r));
		}
		return rules;
	}
	
	public static Rule util_Predicate_Rule_2__Rules(logic.Rule r){
		Triple_Pattern head = util_Predicate_2_Triple_pattern(r.getConsequent().iterator().next().asPredicateInstantiation());
		Set<Triple_Pattern> antecedent = util_translate_PredicateInstantiation_2_Triple_Patterns(r.getAntecedent());
		return new Rule(antecedent, head);
	}
	public static Set<Rule> util_Predicate_Rules_2_Rules(Set<logic.Rule> ruleset){
		Set<Rule> rules = new HashSet<Rule>();
		for(logic.Rule r : ruleset) {
			rules.add(util_Predicate_Rule_2__Rules(r));
		}
		return rules;
	}
	public static List<Rule> util_Predicate_Rules_2_Rules(List<logic.Rule> ruleset){
		List<Rule> rules = new LinkedList<Rule>();
		for(logic.Rule r : ruleset) {
			rules.add(util_Predicate_Rule_2__Rules(r));
		}
		return rules;
	}

	public static Set<Predicate> known_predicates = new HashSet<Predicate>();
	
    public static Set<Triple_Pattern> sandboxGraphOf(Set<Triple_Pattern> instance){
    	Set<Triple_Pattern> sandboxGraph = new HashSet<Triple_Pattern>();
    	for(Triple_Pattern tp : instance) {
    		sandboxGraph.add(sandboxTripleOf(tp));
    	}
    	return sandboxGraph;
	}
    public static Triple_Pattern sandboxTripleOf(Triple_Pattern tp){
    	return new Triple_Pattern(
    				tp.getSubject() instanceof Variable ? new URI(RDFUtil.LAMBDAURI) : tp.getSubject()  , 
    				tp.getPredicate(), 
    				tp.getObject() instanceof Variable ? new URI(RDFUtil.LAMBDAURI) : tp.getObject()    );
    	
	}
	
    /*public static Set<Map<Integer, Element>> evaluate_join_aware(Set<Triple_Pattern> query_triples, Set<Triple_Pattern> patterns){
		// first, evaluate on the sandbox graph
		Set<Map<Integer, Element>> result = new HashSet<Map<Integer, Element>>();
		Set<Map<Integer, Element>> eval =  evaluate(query_triples, sandboxGraphOf(patterns) );
		for(Map<Integer, Element> m : eval) {
			Set<Map<Integer, Element>> new_ms = new HashSet<Map<Integer, Element>>();
			for(Triple_Pattern q : query_triples) {
				for(int i = 0; i < 3; i++) {
					if(q.get(i) instanceof Variable_Instance) {
						Integer index = ((Variable_Instance)q.get(i)).getIndex();
						if(m.containsKey(index) && m.get(index).equals(new URI(RDFUtil.LAMBDAURI))) {
							// find the original variable index that was in this position in the origin triple
							for(Triple_Pattern g : patterns) if(g.get(i) instanceof Variable) {
								//check that g can be an origin triple for m(q)
								if(Util.is_instance_of(Util.apply_mapping(m, q), g)) {
									Integer var_index = ((Variable_Instance)g.get(i)).getIndex();
									boolean noLitSet = ((Variable_Instance)g.get(i)).isNoLit();
									Variable target = new Variable_Instance(noLitSet, var_index);
									result.add(clone_except_for(m,index,target));
									
									
									
									
									new_m.put(index,target);
									//
									for()
									new_ms.add(new_m);
								}
							}
						}
					}
				}
			}
		}
		return result;
	}*/
	private static Map<Integer, Element>  clone_except_for( Map<Integer, Element> m, Integer index, Variable target) {
		if(!m.containsKey(index) || ! m.get(index).equals(new URI(RDFUtil.LAMBDAURI))) return null;
		Map<Integer, Element> new_m = new HashMap<Integer, Element>();
		for(Integer i : m.keySet()) {
			if(!i.equals(index)) {
				new_m.put(i, m.get(i));
			} else {
				new_m.put(i, target);				
			}
		}
		return new_m;
	}
	
	public static String util_Triple_Patterns_2_toSPARQLQuery(Set<Triple_Pattern> query) {
		String result = "SELECT DISTINCT * WHERE {\n";
		for(Triple_Pattern tp : query)
			result += "  "+util_Triple_Pattern_2_toSPARQLQuery(tp)+"\n";
		return result+"\n  }";
	}
	public static String util_Triple_Patterns_2_toSPARQLASKQuery(Set<Triple_Pattern> query) {
		String result = "ASK {\n";
		for(Triple_Pattern tp : query)
			result += "  "+util_Triple_Pattern_2_toSPARQLQuery(tp)+"\n";
		return result+"\n  }";
	}
	public static String util_Triple_Pattern_2_toSPARQLQuery(Triple_Pattern tp) {
		return tp.getSubject().toSPARQLelement()+" "+tp.getPredicate().toSPARQLelement()+" "+tp.getObject().toSPARQLelement()+" .";
	}
}
