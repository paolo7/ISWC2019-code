package core;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

import logic.RDFUtil;
import shacl.Existential_Constraint;
import shacl.Existential_Validator;
import shacl.Translator_to_SHACL;

public class Util {

	private static long index = 0;
	
	public static String getUniqueCode(Element el) {
		if(el instanceof URI) {
			URI uri = (URI) el;
			if(uri.getNamespace() != null) return uri.getNamespacesuffix();
			return ""+index++;			
		}
		if(el instanceof Variable) {
			if(((Variable)el).isNoLit())
				return "Vmin";
			else return "Vplus";
		}
		return "literal";
	}
	
	public static String getTurtlePrefixes(Set<Triple_Pattern> patterns) {
		String prefixes = "#Prefixes:";
		Set<Namespace> namespaces = new HashSet<Namespace>();
		Set<URI> uris = SearchUtil.searchURIs(patterns);
		for(URI uri : uris) {
			if(uri.getNamespace() != null) namespaces.add(uri.getNamespace());
		}
		for(Namespace ns : namespaces) {
			prefixes += "\n"+getTurtlePrefix(ns);
		}
		return prefixes;
	}
	public static String getTurtlePrefix(Namespace ns) {
		return "@prefix "+ns.getPrefix()+": <"+ns.getNamespace()+"> .";
	}

	public static boolean areInstancesOfSchema(List<Triple_Pattern> instances, List<Triple_Pattern> schema) {
		for(Triple_Pattern tp : instances) {
			Statement s = ResourceFactory.createStatement(
					ResourceFactory.createResource(((URI) tp.getSubject()).getURI()), 
					ResourceFactory.createProperty(((URI) tp.getPredicate()).getURI()), 
					
					tp.getObject() instanceof URI ? 
							ResourceFactory.createResource(((URI) tp.getObject()).getURI()) : 
								ResourceFactory.createStringLiteral(((Literal) tp.getObject()).getValue())
								);
			if(!Util.isInstanceOfSchema(s,schema)) return false;
		}
		return true;
	}

	public static boolean isInstanceOfSchema(Statement s, List<Triple_Pattern> schema) {
		for(Triple_Pattern tp : schema) {
			if(Util.isInstanceOfPattern(s,tp)) return true;
		}
		for(Triple_Pattern tp : schema) {
			if(Util.isInstanceOfPattern(s,tp)) return true;
		}
		return false;
	}
	
	public static boolean areExistentialConstraintsValid(List<Triple_Pattern> instance, Set<Existential_Constraint> constraints) {
		for(Existential_Constraint constraint : constraints) {
			//if(!isExistentialConstraintValid(instance, ))
		}
		return true;
	}
	public static boolean isExistentialConstraintValid(Model m, Existential_Constraint constraint) {
		Query query = QueryFactory.create(constraint.toSPARQL_antecedent());
		QueryExecution qexec = QueryExecutionFactory.create(query, m) ;
		ResultSet resultSet = qexec.execSelect();
		while (resultSet.hasNext()) {
			QuerySolution sol = resultSet.next();
			
			boolean impossible_binding = false;
			String queryString = "ASK {\n ";
			for(Triple_Pattern tp : constraint.getConsequent()) {
				Element new_subject = tp.getSubject();
				if(new_subject instanceof Variable_Instance) {
					if(sol.contains(((Variable_Instance) new_subject).toSPARQLResultelement())) {
						RDFNode binding = sol.get(  (((Variable_Instance) new_subject).toSPARQLResultelement()) );
						if(binding.isResource())
							new_subject = new URI(binding.asResource().getURI());
						else {
							impossible_binding = true;
						}
					} 
				}
				Element new_predicate = tp.getPredicate();
				Element new_object = tp.getObject();
				if(new_object instanceof Variable_Instance) {
					if(sol.contains(((Variable_Instance) new_object).toSPARQLResultelement())) {
						RDFNode binding = sol.get(  (((Variable_Instance) new_object).toSPARQLResultelement()) );
						if(binding.isLiteral()) {
							new_object = new Literal(binding.asLiteral().getLexicalForm());
						} else {
							new_object = new URI(binding.asResource().getURI());
						}
					} 
				}
				queryString += new_subject.toSPARQLelement()+" "+new_predicate.toSPARQLelement()+" "+new_object.toSPARQLelement()+" . \n";
				if(new_object instanceof Variable && (((Variable) new_object).isNoLit()))
					queryString += " \n FILTER isIRI("+new_object.toSPARQLelement()+") ";
			}
			if(!impossible_binding) {
				queryString += "\n}";
				QueryExecution qexec2 = QueryExecutionFactory.create(QueryFactory.create(queryString), m) ;
				boolean result = qexec2.execAsk();
				if(!result) return false;
			}
		}
		return true;
	}
	
	public static Element util_RDFNode_2_Element(RDFNode node) {
		if(node.isLiteral()) {
			return new Literal(node.asLiteral().getLexicalForm());
		} else {
			return new URI(node.asResource().getURI());
		}
	}
	
	public static void satisfyConstraint(Random r, Model m, Existential_Constraint constraint) {
		Query query = QueryFactory.create(constraint.toSPARQL_antecedent());
		QueryExecution qexec = QueryExecutionFactory.create(query, m) ;
		ResultSet resultSet = qexec.execSelect();
		Set<Statement> new_statements = new HashSet<Statement>();
		while (resultSet.hasNext()) {
			QuerySolution sol = resultSet.next();
			for(Triple_Pattern tp : constraint.getConsequent()) {
				Element new_subject = tp.getSubject();
				if(new_subject instanceof Variable_Instance) {
					if(sol.contains(((Variable_Instance) new_subject).toSPARQLResultelement())) {
						RDFNode binding = sol.get(  (((Variable_Instance) new_subject).toSPARQLResultelement()) );
						new_subject = new URI(binding.asResource().getURI());
					} else {
						new_subject = getRandomURI(r);
					}
				}
				Element new_predicate = tp.getPredicate();
				Element new_object = tp.getObject();
				if(new_object instanceof Variable_Instance) {
					if(sol.contains(((Variable_Instance) new_object).toSPARQLResultelement())) {
						RDFNode binding = sol.get(  (((Variable_Instance) new_object).toSPARQLResultelement()) );
						if(binding.isLiteral()) {
							new_object = new Literal(binding.asLiteral().getLexicalForm());
						} else {
							new_object = new URI(binding.asResource().getURI());
						}
					} else {
						if(r.nextBoolean()) {
							new_object = getRandomURI(r);
						} else {
							new_object = getRandomLiteral(r);
						}
					}
				}
				Statement new_s = ResourceFactory.createStatement(instantiateSubject(r, new_subject), 
						instantiatePredicate(r, new_predicate), 
						instantiateObject(r, new_object));
				new_statements.add(new_s);
			}
		}
		for(Statement s: new_statements)
			m.add(s);
		qexec.close();
	}

	public static boolean isInstanceOfPattern(Statement s, Triple_Pattern tp) {
		Model model = ModelFactory.createDefaultModel() ;
		model.add(s);
		String object = tp.getObject().toSPARQLelement();
		String queryString = "ASK {\n "+tp.getSubject().toSPARQLelement()+" "+tp.getPredicate().toSPARQLelement()+" "+object+" . ";
		if(tp.getObject() instanceof Variable && ((Variable)tp.getObject()).isNoLit())
			queryString += " \n FILTER isIRI("+object+") ";
		queryString += "\n}";
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
		boolean result = qexec.execAsk();
		return result;
	}

	public static Model unprefixedTurtleToModel(String rdf) {
		Model model = ModelFactory.createDefaultModel() ;
		return model.read(new ByteArrayInputStream((getTurtlePrefix(Translator_to_SHACL.ns)+"\n"+rdf).getBytes()), null, "TURTLE");
	}

	public static Set<Element> toSingleton(Element e){
		Set<Element> singleton = new HashSet<Element>();
		singleton.add(e);
		return singleton;
	}

	public static Element computeElementDifference(Element e1, Element e2){
		if(e1.equals(e2)) 
			return e1;
		else {
			if(SearchUtil.isElementSubsumedBy(e1, e2)) {
				return e1;
			} else if(SearchUtil.isElementSubsumedBy(e2, e1)) {
				return e2;
			} else {
				return null;
			}
		}
	}

	public static Set<Triple_Pattern> computeSetDifferenceOfTriplePatternsWithSamePredicate(Set<Triple_Pattern> patterns1, Set<Triple_Pattern> patterns2){
		Set<Triple_Pattern> difference_patterns = new HashSet<Triple_Pattern>();
		for(Triple_Pattern tp1 : patterns1) {
			for(Triple_Pattern tp2 : patterns2) {
				Triple_Pattern new_tp = Util.computeTripleDifference(tp1,tp2);
				if(new_tp != null) difference_patterns.add(new_tp);
			}
		}
		return difference_patterns;
	}

	public static Triple_Pattern computeTripleDifference(Triple_Pattern tp1, Triple_Pattern tp2){
		if(tp2.equals(tp1)) 
			return tp1;
		else {
			if(SearchUtil.isTripleSubsumedBy(tp1, tp2)) {
				return tp1;
			} else if(SearchUtil.isTripleSubsumedBy(tp2, tp1)) {
				return tp2;
			} else {
				Element new_subject = computeElementDifference(tp1.getSubject(), tp2.getSubject());
				Element new_predicate = computeElementDifference(tp1.getPredicate(), tp2.getPredicate());
				Element new_object = computeElementDifference(tp1.getObject(), tp2.getObject());
				if(new_subject != null && new_predicate != null && new_object != null)
					return new Triple_Pattern(new_subject, new_predicate, new_object);
				else {
					new_object = computeElementDifference(tp1.getObject(), tp2.getObject());
				}
			}
		}
		return null;
	}
	
	public static int getFreshVariableID_on_existentials(Set<Existential_Constraint> exs) {
		return getFreshVariableID(null, exs);
	}
	public static int getFreshVariableID_on_patterns(Set<Triple_Pattern> P) {
		return getFreshVariableID(P, null);
	}
	public static int getFreshVariableID(Set<Triple_Pattern> P, Set<Existential_Constraint> exs) {
		Set<Integer> usedVars = new HashSet<Integer>();
		if(exs != null) for(Existential_Constraint ex : exs) {
			for(Triple_Pattern tp : ex.getAntecedent()) {
				if(tp.getSubject() instanceof Variable) usedVars.add(new Integer(((Variable_Instance) tp.getSubject()).getIndex()));
				if(tp.getObject() instanceof Variable) usedVars.add(new Integer(((Variable_Instance) tp.getObject()).getIndex()));
			}
			for(Triple_Pattern tp : ex.getConsequent()) {
				if(tp.getSubject() instanceof Variable) usedVars.add(new Integer(((Variable_Instance) tp.getSubject()).getIndex()));
				if(tp.getObject() instanceof Variable) usedVars.add(new Integer(((Variable_Instance) tp.getObject()).getIndex()));
			}
		}
		if(P != null) for(Triple_Pattern t : P) {
			if(t.getSubject() instanceof Variable) usedVars.add(new Integer(((Variable_Instance) t.getSubject()).getIndex()));
			if(t.getObject() instanceof Variable) usedVars.add(new Integer(((Variable_Instance) t.getObject()).getIndex()));
		}
		int i = 1;
		while(usedVars.contains(new Integer(i))) { i++; }
		return i;
	}
	
	public static int URIsize = 15;
	public static Namespace ns = new Namespace("ex","http://example.org/ns#");
	public static Element getRandomURI(Random r) {
		return new URI(ns,"uri"+r.nextInt(URIsize));
	}

	public static Element getRandomLiteral(Random r) {
		return new Literal("lit-"+r.nextInt(URIsize));
	}

	public static Resource instantiateSubject(Random r, Element subject) {
		Resource uri = ResourceFactory.createResource(((URI) getRandomURI(r)).getURI());
		if(subject instanceof URI) return ResourceFactory.createResource(((URI) subject).getURI());
		return uri;
	}

	public static Property instantiatePredicate(Random r, Element predicate) {
		return ResourceFactory.createProperty(((URI) predicate).getURI());
	}

	public static RDFNode instantiateObject(Random r, Element object) {
		if(object instanceof URI) return ResourceFactory.createResource(((URI) object).getURI());
		if(object instanceof Literal) return ResourceFactory.createStringLiteral(((Literal) object).getValue());
		Resource uri = ResourceFactory.createResource(((URI) getRandomURI(r)).getURI());
		if(((Variable) object).isNoLit()) return uri;
		// else create a literal with 50% probability
		if(r.nextBoolean()) {
			return ResourceFactory.createStringLiteral(((Literal) getRandomLiteral(r)).getValue());
		}
		return uri;
	}

	public static Model convertInstanceSchemaToModel(List<Triple_Pattern> instance) {
		Model model = ModelFactory.createDefaultModel() ;
		for(Triple_Pattern tp : instance) {
			Statement s = ResourceFactory.createStatement(
					ResourceFactory.createResource(((URI) tp.getSubject()).getURI()), 
					ResourceFactory.createProperty(((URI) tp.getPredicate()).getURI()), 
					
					tp.getObject() instanceof URI ? 
							ResourceFactory.createResource(((URI) tp.getObject()).getURI()) : 
								ResourceFactory.createStringLiteral(((Literal) tp.getObject()).getValue())
								);
			model.add(s);
		}
		return model;
	}
	
	public static Set<Triple_Pattern> apply_mapping(Map<Integer, Element> m, Set<Triple_Pattern> graph){
		return apply_mapping(true, m, graph);
	}
	public static Set<Triple_Pattern> apply_mapping(boolean retain_lambdas, Map<Integer, Element> m, Set<Triple_Pattern> graph){
		Map<Integer, Element> new_m =  new HashMap<Integer, Element>();
		if(!retain_lambdas) {
			// remove all mappings to lambda
			for(Integer i : m.keySet()) {
				if(!m.get(i).equals(new URI(RDFUtil.LAMBDAURI)))
					new_m.put(i, m.get(i));
			}
			m = new_m;
		}
		Set<Triple_Pattern> result = new HashSet<Triple_Pattern>();
		for(Triple_Pattern tp : graph) {
			if(tp.getObject() instanceof Variable_Instance && m.containsKey( new Integer(((Variable_Instance)tp.getObject()).getIndex())))
				m.get(new Integer(((Variable_Instance)tp.getObject()).getIndex())).enforceNoLiteral();
			result.add(new Triple_Pattern(
				tp.getSubject() instanceof Variable_Instance && m.containsKey( new Integer(((Variable_Instance)tp.getSubject()).getIndex())) ? 
						m.get(new Integer(((Variable_Instance)tp.getSubject()).getIndex())) : tp.getSubject(), 
				tp.getPredicate(),
				tp.getObject() instanceof Variable_Instance && m.containsKey( new Integer(((Variable_Instance)tp.getObject()).getIndex())) ? 
						m.get(new Integer(((Variable_Instance)tp.getObject()).getIndex())) : tp.getObject()
				));
		}
		return result;
	}
	public static Triple_Pattern apply_mapping(Map<Integer, Element> m, Triple_Pattern tp){
		Set<Triple_Pattern> patterns = new HashSet<Triple_Pattern>();
		patterns.add(tp);
		return apply_mapping(m,patterns).iterator().next();
	}
	public static boolean is_instance_of(Triple_Pattern t1, Triple_Pattern t2) {
		return is_element_subsumed_by(t1.get(0), t2.get(0)) &&
				is_element_subsumed_by(t1.get(1), t2.get(1)) &&
				is_element_subsumed_by(t1.get(2), t2.get(2));
	}
	public static boolean is_instance_of(Triple_Pattern t1, Set<Triple_Pattern> T) {
		for(Triple_Pattern t2 : T) {
			if(is_instance_of(t1,t2)) return true;
		}
		return false;
	}
	public static boolean is_element_subsumed_by(Element e1, Element e2) {
		if(e1.equals(e2)) return true;
		if(e2 instanceof Variable) {
			if(!((Variable) e2).isNoLit()) return true;
			else {
				if(e1 instanceof URI) return true;
				else if(e1 instanceof Variable && ((Variable)e1).isNoLit()) return true;
				return false;
			}
		}
		return false;
	}
/*	public static boolean is_subsumed_by(Triple_Pattern t1, Triple_Pattern t2) {
		Map<Integer, Element> constraints = new HashMap<Integer, Element>();
		for(int i = 0; i < 3; i++) {
			Element e1 = t1.get(i);
			Element e2 = t2.get(i);
			if(e1 instanceof Variable_Instance) {
				Integer index = ((Variable_Instance) e1).getIndex();
				if(constraints.containsKey(index)) e1 = constraints.get(index);
			}
			if(!e1 instanceof) {
				
			}
			
		}
	}*/
	
	
	/*public static void uniquify_rule_vars(Rule r) {
		
	}*/
	public static Set<Triple_Pattern> ground_with_fresh_variables(Set<Triple_Pattern> patterns){
		Set<Triple_Pattern> ground_triples = new HashSet<Triple_Pattern>();
		for(Triple_Pattern tp : patterns) {
			ground_triples.add(tp.util_ground_variables());
		}
		return ground_triples;
	}
	
	public static Set<Triple_Pattern> closure(Set<Triple_Pattern> instance, Set<Rule> rules){
		Set<Triple_Pattern> closure = new HashSet<Triple_Pattern>();
		closure.addAll(instance);
		boolean changed = true;
		while(changed) {
			int former_triple_count = closure.size();
			for(Rule r : rules) {
				apply_rule(r, closure);
			}
			if(former_triple_count == closure.size()) changed = false;
		}
		return closure;
	}
	
	/**
	 * 
	 * @param instance
	 * @param r
	 * @return true if the rule is applicable
	 */
	public static boolean apply_rule(Rule r, Set<Triple_Pattern> instance) {
		Set<Map<Integer, Element>> evaluation = evaluate(r.getAntecedent(),instance);
		for(Map<Integer, Element> m : evaluation) {
			instance.add(apply_mapping(m, r.getConsequent()));
		}
		return true;
	}

	public static Map<Integer, Element> evaluate_join_aware_on_triple(Triple_Pattern qt, Triple_Pattern tp){
		Set<Map<Integer, Element>> mappings = Util.evaluate(qt,Existential_Validator.sandboxTripleOf(tp));
		if(mappings.size() == 0) return null;
		Map<Integer, Element> singleton_mapping = Util.evaluate(qt,Existential_Validator.sandboxTripleOf(tp)).iterator().next();
		Map<Integer, Element> map = new HashMap<Integer, Element>();
		for(int i = 0; i < 3; i += 2) {
			Element eq = qt.get(i);
			Element ep = tp.get(i);
			if(eq instanceof Variable) {
				if(singleton_mapping.get(  new Integer(    ((Variable_Instance) eq).getIndex() )    ).equals(new URI(RDFUtil.LAMBDAURI)) ) {
					map.put(((Variable_Instance) eq).getIndex(), ep);
				} else {
					map.put(((Variable_Instance) eq).getIndex(), singleton_mapping.get(  new Integer(    ((Variable_Instance) eq).getIndex() )    ));
				}
			}
		}
		return map;
	}

	public static Set<Map<Integer, Element>> evaluate(Set<Triple_Pattern> query_triples, Set<Triple_Pattern> instance){
		Model instance_model = convertInstanceSchemaToModel(new ArrayList<Triple_Pattern>(instance));
		String SPARQLquery = RDFUtil.getSPARQLprefixes(instance_model)+Existential_Validator.util_Triple_Patterns_2_toSPARQLQuery(query_triples);
		return evaluate(SPARQLquery, instance);
	}
	
	public static Set<Map<Integer, Element>> evaluate(String SPARQLquery, Set<Triple_Pattern> instance){
		Set<Map<Integer, Element>> result = new HashSet<Map<Integer, Element>>();
		Model instance_model = convertInstanceSchemaToModel(new ArrayList<Triple_Pattern>(instance));
		Query query = QueryFactory.create(SPARQLquery) ;
		QueryExecution qe = QueryExecutionFactory.create(query, instance_model);
	    ResultSet rs = qe.execSelect();
	    while (rs.hasNext())
		{
	    	Map<Integer, Element> mapping = new HashMap<Integer, Element>();
	    	QuerySolution binding = rs.nextSolution();
	    	Iterator<String> i = binding.varNames();
	    	while(i.hasNext()) {
	    		String variable_name = i.next();
	    		int varnum = new Integer(variable_name.substring(1));
	    		RDFNode b = binding.get(variable_name);
	    		mapping.put(new Integer(varnum), util_RDFNode_2_Element(b));
	    	}
	    	result.add(mapping);
		}
	    qe.close();
		return result;
	}
	
	public static Set<Map<Integer, Element>> filter_mappings_to_lambda(Set<Map<Integer, Element>> mappings){
		Set<Map<Integer, Element>> new_mappings = new HashSet<Map<Integer, Element>>();
		for(Map<Integer, Element> m : mappings) {
			Map<Integer, Element> new_m = new HashMap<Integer, Element>();
			for(Integer i : m.keySet()) {
				if(!m.get(i).equals(new URI(RDFUtil.LAMBDAURI))) {
					new_m.put(i,m.get(i));
				}
			}
			new_mappings.add(new_m);
		}
		return new_mappings;
	}

	public static Set<Map<Integer, Element>> evaluate(Triple_Pattern query_triple, Triple_Pattern instance){
		Set<Triple_Pattern> q =  new HashSet<Triple_Pattern>();
		q.add(query_triple);
		Set<Triple_Pattern> i =  new HashSet<Triple_Pattern>();
		i.add(instance);
		return evaluate(q,i);
	}
	
	public static boolean is_existential_constraint_valid(Existential_Constraint ex, Set<Triple_Pattern> instance) {
		Set<Map<Integer, Element>> evaluation = evaluate(ex.getAntecedent(),instance);
		for(Map<Integer, Element> m : evaluation) {
			Set<Triple_Pattern> expected_triple_patterns = Util.apply_mapping(m, ex.getConsequent());
			if(!do_triple_patterns_exist(expected_triple_patterns,instance))
				return false;
		}
		return true;
	}
	public static boolean do_triple_patterns_exist(Set<Triple_Pattern> query, Set<Triple_Pattern> instance) {
		Model instance_model = convertInstanceSchemaToModel(new ArrayList<Triple_Pattern>(instance));
		String SPARQLquery = RDFUtil.getSPARQLprefixes(instance_model)+Existential_Validator.util_Triple_Patterns_2_toSPARQLASKQuery(query);
		Query queryASK = QueryFactory.create(SPARQLquery) ;
		QueryExecution qe = QueryExecutionFactory.create(queryASK, instance_model);
	    boolean result =  qe.execAsk();
	    qe.close();
		return result;
	}
	
	public static String query_expansion(Set<Triple_Pattern> q){
		String query = "SELECT * WHERE {\n";
		for(Triple_Pattern tp : q) {
			query += "  {\n"
					+ "  {"+query_expansion_helper(tp, false, false)+"}\n"
					+ "  UNION {"+query_expansion_helper(tp, false, true)+"}\n"
					+ "  UNION {"+query_expansion_helper(tp, true, false)+"}\n"
					+ "  UNION {"+query_expansion_helper(tp, true, true)+"}\n"
					+ "  }\n";
		}
		return query+"\n}";
	}
	public static String query_expansion_helper(Triple_Pattern t, boolean lambda1, boolean lambda3){
		return (lambda1 ? new URI(RDFUtil.LAMBDAURI) : t.getSubject().toSPARQLelement() ) +
				" "+t.getPredicate().toSPARQLelement()+ " " + 
				(lambda3 ? new URI(RDFUtil.LAMBDAURI) : t.getObject().toSPARQLelement() );
	}
	
	public static boolean occurs_in_subject_or_object_pos(Element e, Set<Triple_Pattern> P) {
		for(Triple_Pattern p : P) {
			if(p.getSubject().equals(e) || p.getPredicate().equals(e)) return true;
		}
		return false;
	}
	
}
