import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

import core.Namespace;
import core.Triple_Pattern;
import core.URI;
import core.Util;
import shacl.Schema;
import shacl.TopBraid_SHACL_wrapper;

public class testClass {

	static Namespace ns = new Namespace("ex","http://example.org/ns#");
	static Namespace sosa = new Namespace("sosa","http://www.w3.org/ns/sosa/");
	static Namespace rdf = new Namespace("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	private static Namespace rdfs = new Namespace("rdfs","http://www.w3.org/2000/01/rdf-schema#");
	private static Namespace sh = new Namespace("sh","http://www.w3.org/ns/shacl#");
	
	public static void main(String[] args) {
		
		//testTranslationToSHACL.test_translation_to_SHACL();
		//testTranslationFromSHACL.test_translation_from_SHACL();
		testExistentialConstraintsValidation.simple_test();
	}
	
	//@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
	static String getPrefixes() {
		String prefixes = "\n";
		prefixes += getPrefixString(ns)+"\n";
		prefixes += getPrefixString(sosa)+"\n";
		prefixes += getPrefixString(rdf)+"\n";
		prefixes += getPrefixString(rdfs)+"\n";
		prefixes += getPrefixString(sh)+"\n";
		return prefixes;
	}
	private static String getPrefixString(Namespace ns) {
		String prefixes = "\n";
		prefixes += "@prefix "+ns.getPrefix()+": <"+ns.getNamespace()+">";
		return prefixes;
	}
	
	static void utility_print_schema(Set<Triple_Pattern> schema) {
		System.out.println("\n# SCHEMA:");
		for(Triple_Pattern tp : schema) {
			System.out.println("# "+tp);
		}
		System.out.println("\n###\n");
	}
	
	static boolean validateFalse(boolean b) {
		if(!b) {
			//System.out.println(" - test correct (negative)");
			return true;
		}
		//System.out.println(" - test FAILED (negative)");
		return false;
	}
	static boolean validateTrue(boolean b) {
		if(b) {
			//System.out.println(" - test correct (positive)");
			return true;
		}
		//System.out.println(" - test FAILED (positive)");
		return false;
	}
	
	
	static Model getRandomNonInstance(Random r, List<Triple_Pattern> schema, Set<URI> predicates) {
		Model model = ModelFactory.createDefaultModel() ;
		// start with a real instance 50% of the time
		if(r.nextBoolean()) model = testTranslationToSHACL.getRandomInstance(r,schema);
		// extend it with some wrong statements
		Object[] predicatesArray = predicates.toArray();
		int i = 0;
		int attempts = 0;
		while(i < 1+r.nextInt(testTranslationToSHACL.maxFalseInstances-1)) {
			attempts++;
			if(attempts > 5000) return null;
			// we chose a pattern to reuse its predicate
			int patternChosen = r.nextInt(schema.size());
			Statement s;
			if(r.nextBoolean()) {
				s = ResourceFactory.createStatement(
						Util.instantiateSubject(r,testTranslationToSHACL.randomSubject(r)), 
						Util.instantiatePredicate(r,schema.get(patternChosen).getPredicate()), 
						Util.instantiateObject(r,testTranslationToSHACL.randomObject(r)));
			} else {
				
				s = ResourceFactory.createStatement(
						Util.instantiateSubject(r,testTranslationToSHACL.randomSubject(r)), 
						Util.instantiatePredicate(r,(URI)predicatesArray[r.nextInt(predicates.size())]), 
						Util.instantiateObject(r,testTranslationToSHACL.randomObject(r)));
			}
			/*if(attempts > 1000)
				s = ResourceFactory.createStatement(
						testTranslationToSHACL.instantiateSubject(r,testTranslationToSHACL.randomSubject(r)), 
						testTranslationToSHACL.instantiatePredicate(r,new URI("http://freshuri.freshuri/")), 
						testTranslationToSHACL.instantiateObject(r,testTranslationToSHACL.randomObject(r)));*/
			if(!Util.isInstanceOfSchema(s,schema)) {
				model.add(s);
				i++;
			}
		}
		return model;
	}
	
	
	
	static boolean test_schema_pattern_equivalence(Model SHACL, Schema schema, int maxIterations, Set<URI> predicates) {
		boolean result = true;
		for(int i = 0; i < maxIterations; i++) {
			Model positive  = testTranslationToSHACL.getRandomInstanceWithExistentials(new Random(i), schema);
			Model negative  = getRandomNonInstance(new Random(i), new ArrayList<Triple_Pattern>(schema.getSchema_Graph()), predicates);
			if(negative != null) {				
				boolean validationSCHEMA1 = TopBraid_SHACL_wrapper.validate(positive, SHACL);
				boolean validationSCHEMA2 = !TopBraid_SHACL_wrapper.validate(negative, SHACL);
				result = result && validationSCHEMA1 && validationSCHEMA2;
				if(!result) {
					System.out.print("-");
				}
			} else System.out.print(".");
		}
		return result;
	}
}
