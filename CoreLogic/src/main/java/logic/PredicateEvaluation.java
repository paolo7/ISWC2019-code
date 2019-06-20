package logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;

	public class PredicateEvaluation {
	
		public static void evaluate(ExternalDB eDB, Set<PredicateInstantiation> predicates) {
			Set<Predicate> alreadyVisualised = new HashSet<Predicate>();
			for (PredicateInstantiation p: predicates) {
				if(!alreadyVisualised.contains(p.getPredicate()) && !p.getPredicate().getName().matches("triple|location|hasLocation|hasWKTLocation|observation|sensor|featureOfInterest|madeObservationA|measurementOfObservation|timeOfObservation|featureOfObservation|propertyOfObservation|hasClass|domain|range")) {					
					System.out.println("\nAVAILABLE PREDICATE: "+p+"\n----\n"+p.getPredicate()+"----");
					evaluate(eDB, p.getPredicate());
					alreadyVisualised.add(p.getPredicate());
				}
				
			}
		}
		
	public static void evaluate(ExternalDB eDB, Predicate predicate) {
		
		//System.out.println(predicate.hasVariables()+" >> "+predicate);
		//if(!predicate.hasVariables()) return;
		String SPARQLquery = RDFUtil.getSPARQLprefixes(eDB) + "SELECT * WHERE {" + predicate.toSPARQL() + "}";
		TupleQueryResult result = eDB.query(SPARQLquery);
		if(predicate.getName().equals("HighCO2concentrationAlert"))
			System.out.println(predicate.getName());
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			String resultString = "";
	    	for (TextTemplate tt: predicate.getTextLabel()) {
	    		if (tt.isText()) {
	    			resultString += tt.getText()+" ";
	    		} else {
	    			Value v = bindingSet.getBinding("v"+tt.getVar()).getValue(); 
	    			resultString += RDFUtil.resolveLabelOfURI(v.stringValue())+" ";
		
	    		}
	    	}
	    	System.out.println(resultString);
	    	
	        /*BindingSet bindingSet = result.next();
	        for(String name : bindingSet.getBindingNames()) {
	        	Value v = bindingSet.getBinding(name).getValue();
	        	System.out.println(name+": "+v.stringValue());
	        }
	        System.out.println("");*/
	    }
	    result.close();
	    
		}
	
	public static void computeRuleClosure(ExternalDB eDB, Set<Rule> rules, Set<Predicate> knownPredicates) {
		
		//System.out.println("Start) Triples in DB: "+eDB.countTriples());
		boolean terminationReached = false;
		while (!terminationReached) {
			int triples = eDB.countTriples();
			for(Rule r : rules) {
				/*if(r.getConsequent().iterator().next().getName().iterator().next().getText().equals("inside")) {
					System.out.println("rrrr "+r);
				}*/
				/*if(r.getConsequent().iterator().next().getName().iterator().next().getText().equals("hasWKTLocation")) {
					System.out.println(r);
				}*/
				if(!r.createsNewPredicate()) 
					applyRule(eDB, r, knownPredicates);
			}			
			if(eDB.countTriples() == triples) terminationReached = true;
			//System.out.println(iteration++ + ") Triples in DB: "+eDB.countTriples());
		}
	    
	}
	
	
	public static void applyRule(ExternalDB eDB, Rule rule, Set<Predicate> knownPredicates) {
		if(rule.createsNewPredicate()) throw new RuntimeException("Error, cannot apply rule to dataset because it is a predicate creation rule: \n"+rule.toString());
		String SPARQLquery = RDFUtil.getSPARQLdefaultPrefixes()+rule.getAntecedentSPARQL();
		TupleQueryResult result = eDB.query(SPARQLquery);

		//try {
			while (result.hasNext()) {
				Set<PredicateInstantiation> pis =  new HashSet<PredicateInstantiation>();
				BindingSet bindingSet = result.next();
				Map<String,RDFNode> bindingsMap = new HashMap<String,RDFNode>();
				for(String var :  bindingSet.getBindingNames()) {
					Value value = bindingSet.getValue(var);
					//if(value.getClass() == BNode.class) value = null;
					bindingsMap.put(var, RDFUtil.asJenaNode(value));
				}
				
				for(PredicateTemplate pt: rule.getConsequent()) {
					//Predicate predicate = PredicateUtil.get(predicateName, bindings.length, predicates);
					pis.add(pt.applyRule(bindingsMap, knownPredicates, null, null, null));
				}
				String baseNew = RDFUtil.getBlankNodeBaseURI();
				for(PredicateInstantiation pi : pis) {
					eDB.insertFullyInstantiatedPredicate(pi,baseNew);
				}
			}
			
		/*} catch (QueryEvaluationException e) {
			System.out.println("+ "+e.getMessage());
		}*/
		result.close();
	}
}
