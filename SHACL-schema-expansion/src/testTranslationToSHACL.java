import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

import core.Element;
import core.Literal;
import core.Triple_Pattern;
import core.URI;
import core.Util;
import core.Variable;
import shacl.Existential_Constraint;
import shacl.Schema;
import shacl.TopBraid_SHACL_wrapper;
import shacl.Translator_to_SHACL;

public class testTranslationToSHACL {

	public static void test_translation_to_SHACL() {
		// Systematic test, that covers all possible schemas and instances under a given size
		boolean systematic = testTranslationToSHACL.testSystematically(2,2); // e.g., with max 2 triples in schema and 2 triples in instance there are about 14 million possible schema-instance combinations
		// Systematic test, that covers all possible schemas and instances under a given size
		boolean systematic2 = testTranslationToSHACL.testSystematically(3,1); // e.g., with max 2 triples in schema and 2 triples in instance there are about 14 million possible schema-instance combinations
		
		// Random test that randomly samples schemas and instances from a much larger set (impossible to cover in its entirety)
		// this test is enhanced with a few manually designed tests
		boolean random = true;
		//random = runCompactTestSuite();
		System.out.println("Testing finished, with results: ("+systematic+", "+systematic2+") on the systematic test and ("+random+") on the random sampling test.");
	}

	private static boolean develop_test1() {
		
		Triple_Pattern tp1 = new Triple_Pattern(new URI(testClass.ns,"a"), new URI(testClass.ns,"p"), new Variable(false));
		System.out.println(tp1);
		Triple_Pattern tp2 = new Triple_Pattern(new Variable(true), new URI(testClass.ns,"p"), new Literal("lit"));
		System.out.println(tp2);
		
		Triple_Pattern tp3 = new Triple_Pattern(new URI(testClass.ns,"f"), new URI(testClass.ns,"q"), new Variable(true));
		System.out.println(tp3);
		
		Triple_Pattern tp4 = new Triple_Pattern(new URI(testClass.ns,"f"), new URI(testClass.ns,"q"), new Literal("lita"));
		System.out.println(tp4);
		
		Triple_Pattern tp5 = new Triple_Pattern(new URI(testClass.ns,"b"), new URI(testClass.ns,"q"), new URI(testClass.ns,"j"));
		System.out.println(tp5);
		
		Set<Triple_Pattern> schema = new HashSet<Triple_Pattern>();
		schema.add(tp1);
		schema.add(tp2);
		schema.add(tp3);
		schema.add(tp4);
		schema.add(tp5);
		
		System.out.println("\n\n");
		
		Translator_to_SHACL.translateToTurtleSHACL(schema);
		
		return true;
		
	}

	private static boolean develop_test2() {
		
		Set<Triple_Pattern> schema = new HashSet<Triple_Pattern>();
		
		schema.add(new Triple_Pattern(new Variable(true), new URI(testClass.sosa,"observedProperty"), new URI(testClass.ns,"CO_Danger")));
		schema.add(new Triple_Pattern(new Variable(true), new URI(testClass.sosa,"observedProperty"), new URI(testClass.ns,"WorkerTag")));
		schema.add(new Triple_Pattern(new Variable(true), new URI(testClass.sosa,"hasFeatureOfInterest"), new URI(testClass.ns,"TunnelA")));
		schema.add(new Triple_Pattern(new Variable(true), new URI(testClass.sosa,"hasResult"), new Variable(false)));
		testClass.utility_print_schema(schema);
		Translator_to_SHACL.translateToTurtleSHACL(schema);
		
		schema.add(new Triple_Pattern(new URI(testClass.ns,"TunnelA"), new URI(testClass.rdf,"type"), new URI(testClass.ns,"TrespassedArea")));
		schema.add(new Triple_Pattern(new URI(testClass.ns,"TunnelA"), new URI(testClass.rdf,"type"), new URI(testClass.ns,"OffLimitArea")));
		testClass.utility_print_schema(schema);
		Translator_to_SHACL.translateToTurtleSHACL(schema);
		
		return true;
		
	}

	private static boolean runTestSuite() {
		int passed = 0;
		int failed = 0;
		
		int k = 0;
		System.out.println("RUNNING A MANUALLY CREATED TEST SUITE");
		// example
		k = testTranslationToSHACL.test1() ? passed++ : failed++;
		k = testTranslationToSHACL.test2() ? passed++ : failed++;
		k = testTranslationToSHACL.test3() ? passed++ : failed++;
		int max_tests = 1000000;
		System.out.println("RUNNING "+max_tests+" (DETERMINISTICALLY) RANDOM TESTS");
		// random
		for(int i = 0; i < max_tests; i++) {
			k = testTranslationToSHACL.testRandom(i) ? passed++ : failed++;
			System.out.println(failed+" failed / total "+i);
		}
		
		if(failed == 0) System.out.println("ALL TESTS PASSED ("+passed+"/"+(failed+passed)+")");
		else System.out.println("SOME TESTS FAILED ("+passed+"/"+(failed+passed)+")");
		return failed == 0;
	}

	private static boolean runCompactTestSuite() {
		
		testTranslationToSHACL.URIsize = 10;
		testTranslationToSHACL.maxInstances = 18;
		testTranslationToSHACL.maxFalseInstances = 5;
		testTranslationToSHACL.maxSchemaTriples = 13;
		
		
		int passed = 0;
		int failed = 0;
		
		int k = 0;
		System.out.println("RUNNING A MANUALLY CREATED TEST SUITE");
		// example
		k = testTranslationToSHACL.test1() ? passed++ : failed++;
		k = testTranslationToSHACL.test2() ? passed++ : failed++;
		k = testTranslationToSHACL.test3() ? passed++ : failed++;
		int max_tests = 1000000000;
		System.out.println("RUNNING "+max_tests+" (DETERMINISTICALLY) RANDOM TESTS - COMPACT");
		// random
		Set<Integer> failedSeeds = new HashSet<Integer>();
		for(int i = 0; i < max_tests; i++) {
			boolean success = testTranslationToSHACL.testRandom(i);
			if(!success) failedSeeds.add(new Integer(i));
			k = success ? passed++ : failed++;
			if(i%100 == 00) {///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				System.out.print(failed+" failed / total "+i+ " [");
				for(Integer in : failedSeeds) {
					System.out.print(in+",");
					}
				System.out.println("]");
				}
		}
		
		if(failed == 0) System.out.println("ALL TESTS PASSED ("+passed+"/"+(failed+passed)+")");
		else System.out.println("SOME TESTS FAILED ("+passed+"/"+(failed+passed)+")");
		return failed == 0;
	}

	static boolean test2() {
		boolean result = true;
		
		// create the schema
		Set<Triple_Pattern> schema = new HashSet<Triple_Pattern>();
		schema.add(new Triple_Pattern(new URI(testClass.ns,"8"), new URI(testClass.ns,"p"), new Literal("lit")));
		schema.add(new Triple_Pattern(new URI(testClass.ns,"9"), new URI(testClass.ns,"p"), new Variable(false)));
		schema.add(new Triple_Pattern(new Variable(true), new URI(testClass.ns,"p"), new URI(testClass.ns,"2")));
		String prefixes = Util.getTurtlePrefixes(schema);
					
			// create a positive instance 1
			String instance = prefixes
					+"ex:8 ex:p \"lit\" .\n"
					+"ex:9 ex:p \"lit\" .\n";
			
			result = result && testClass.validateTrue( TopBraid_SHACL_wrapper.validate(Util.unprefixedTurtleToModel(instance), schema));
		
		
			
			// create a positive instance 1
			String noninstance = prefixes
					+"ex:8 ex:p \"lit\" .\n"
					+"ex:10 ex:p \"lit\" .\n";
			
			result = result && testClass.validateFalse( TopBraid_SHACL_wrapper.validate(Util.unprefixedTurtleToModel(noninstance), schema));
		
			if(!result) {
				System.out.println("FAILURE FOUND");
				
			}
			
		return result;
	
	}

	static boolean test3() {
		boolean result = true;
		
		// create the schema
		Set<Triple_Pattern> schema = new HashSet<Triple_Pattern>();
		schema.add(new Triple_Pattern(new URI(testClass.ns,"7"), new URI(testClass.ns,"9"), new URI(testClass.ns,"10")));
		schema.add(new Triple_Pattern(new Variable(true), new URI(testClass.ns,"9"), new Literal("lit8")));
		schema.add(new Triple_Pattern(new Variable(true), new URI(testClass.ns,"9"), new Literal("lit12")));
		String prefixes = Util.getTurtlePrefixes(schema);
					
			// create a positive instance 1
			String instance = prefixes
					+"ex:7 ex:9 \"lit8\" .\n"
					+"ex:7 ex:9 ex:10 .\n";
			
			result = result && testClass.validateTrue( TopBraid_SHACL_wrapper.validate(Util.unprefixedTurtleToModel(instance), schema));
		
		
			
			// create a negative instance 1
			String noninstance = prefixes
					+"ex:4 ex:9 \"lit8\" .\n"
					+"ex:4 ex:9 ex:10 .\n"
					+"ex:7 ex:9 ex:10 .\n";
			
			result = result && testClass.validateFalse( TopBraid_SHACL_wrapper.validate(Util.unprefixedTurtleToModel(noninstance), schema));
		
			if(!result) {
				System.out.println("FAILURE FOUND");
				
			}
			
		return result;
	
	}

	static boolean test1() {
		boolean result = true;
		
		// create the schema
		Set<Triple_Pattern> schema = new HashSet<Triple_Pattern>();
		schema.add(new Triple_Pattern(new Variable(true), new URI(testClass.sosa,"observedProperty"), new URI(testClass.ns,"CO_Danger")));
		schema.add(new Triple_Pattern(new Variable(true), new URI(testClass.sosa,"observedProperty"), new URI(testClass.ns,"WorkerTag")));
		schema.add(new Triple_Pattern(new Variable(true), new URI(testClass.sosa,"hasFeatureOfInterest"), new URI(testClass.ns,"TunnelA")));
		schema.add(new Triple_Pattern(new Variable(true), new URI(testClass.sosa,"hasResult"), new Variable(false)));
		schema.add(new Triple_Pattern(new URI(testClass.ns,"TunnelA"), new URI(testClass.rdf,"type"), new URI(testClass.ns,"TrespassedArea")));
		schema.add(new Triple_Pattern(new URI(testClass.ns,"TunnelA"), new URI(testClass.rdf,"type"), new URI(testClass.ns,"OffLimitArea")));
		String prefixes = Util.getTurtlePrefixes(schema);
		{			
			// create a positive instance 1
			String instance = prefixes
					+"ex:o1 sosa:observedProperty ex:CO_Danger .\n" + 
					"ex:o1 sosa:hasFeatureOfInterest ex:TunnelA .\n" + 
					"ex:o1 sosa:hasResult \"1\" .\n" + 
					"ex:o2 sosa:observedProperty ex:WorkerTag . \n" + 
					"ex:o2 sosa:hasFeatureOfInterest ex:TunnelA .\n" + 
					"ex:o2 sosa:hasResult ex:John .\n" + 
					"ex:TunnelA rdf:type ex:OffLimitArea .\n" + 
					"ex:TunnelA rdf:type ex:TrespassedArea ."
					+"";
			
			result = result && testClass.validateTrue( TopBraid_SHACL_wrapper.validate(Util.unprefixedTurtleToModel(instance), schema));
		}
		
		{			
			// create a positive instance 2
			String instance = prefixes
					+"ex:o1 sosa:observedProperty ex:CO_Danger .\n" + 
					"ex:o1 sosa:observedProperty ex:WorkerTag .\n" + 
					"ex:o1 sosa:hasFeatureOfInterest ex:TunnelA .\n" + 
					"ex:o1 sosa:hasResult \"123\" .\n" + 
					"ex:o1 sosa:observedProperty ex:WorkerTag . \n" + 
					"ex:o1 sosa:hasFeatureOfInterest ex:TunnelA .\n" + 
					"ex:o1 sosa:hasResult ex:John .\n" + 
					"ex:TunnelA rdf:type ex:OffLimitArea .\n" + 
					"ex:TunnelA rdf:type ex:TrespassedArea ."
					+"";
			
			result = result && testClass.validateTrue( TopBraid_SHACL_wrapper.validate(Util.unprefixedTurtleToModel(instance), schema));
		}
		
		{			
			// create a negative instance 1
			String instance = prefixes
					+"ex:o1 sosa:observedProperty ex:CO_Danger .\n" + 
					"ex:o1 sosa:hasFeatureOfInterest ex:TunnelA .\n" + 
					"ex:o1 sosa:hasResult \"1\" .\n" + 
					"ex:o1 sosa:observedProperty ex:WorkerTag . \n" + 
					"ex:o2 sosa:hasFeatureOfInterest ex:TunnelA .\n" + 
					"ex:o1 sosa:hasResult ex:John .\n" + 
					"ex:TunnelA rdf:type ex:OffLimitArea .\n" + 
					"ex:TunnelA rdf:type ex:TrespassedArea2 ."
					+"";
			
			result = result && testClass.validateFalse( TopBraid_SHACL_wrapper.validate(Util.unprefixedTurtleToModel(instance), schema));
		}
		{			
			// create a negative instance 2
			String instance = prefixes
					+"ex:o1 sosa:observedProperty ex:CO_Danger .\n" + 
					"ex:o1 sosa:hasFeatureOfInterest ex:TunnelA .\n" + 
					"ex:o1 sosa:hasResult \"1\" .\n" + 
					"ex:o2 sosa:observedProperty ex:WorkerTag . \n" + 
					"ex:o2 sosa:hasFeatureOfInterest ex:TunnelA .\n" + 
					"ex:o2 sosa:hasResult ex:John .\n" + 
					"ex:TunnelA rdf:type ex:OffLimitArea .\n" + 
					"ex:TunnelA rdf:type ex:TrespassedArea .\n" + 
					"ex:TunnelAb rdf:type ex:TrespassedArea ."
					+"";
			
			result = result && testClass.validateFalse( TopBraid_SHACL_wrapper.validate(Util.unprefixedTurtleToModel(instance), schema));
		}
		{			
			// create a negative instance 3
			String instance = prefixes
					+"ex:o1 sosa:observedProperty ex:CO_Danger2 .\n" + 
					"ex:o2 sosa:hasFeatureOfInterest ex:TunnelA .\n" + 
					"ex:o1 sosa:hasResult \"1\" .\n" + 
					"ex:o2 sosa:observedProperty ex:WorkerTag . \n" + 
					"ex:o1 sosa:hasFeatureOfInterest ex:TunnelA .\n" + 
					"ex:o1 sosa:hasResult ex:John .\n" + 
					"ex:TunnelA rdf:type ex:OffLimitArea .\n" + 
					"ex:TunnelA rdf:type ex:TrespassedArea ."
					+"";
			
			result = result && testClass.validateFalse( TopBraid_SHACL_wrapper.validate(Util.unprefixedTurtleToModel(instance), schema));
		}
		
		return result;
	}

	public static int URIsize = 15;
	static int maxInstances = 50;
	static int maxFalseInstances = 3;
	static int maxSchemaTriples = 20;
	static Triple_Pattern getRandomTriplePattern(Random r) {
		return new Triple_Pattern(testTranslationToSHACL.randomSubject(r), testTranslationToSHACL.randomPredicate(r), testTranslationToSHACL.randomObject(r));
	}

	static Element randomSubject(Random r) {
		if(r.nextBoolean()) return Util.getRandomURI(r);
		else return new Variable(true);
	}

	static Element randomPredicate(Random r) {
		return Util.getRandomURI(r);
	}

	static Element randomObject(Random r) {
		switch(r.nextInt(3)) {
		case 0:
			return Util.getRandomURI(r);
		case 1:
			return Util.getRandomLiteral(r);
		default:
			return testTranslationToSHACL.getRandomVariable(r);
		}
	}

	static Element getRandomVariable(Random r) {
		return new Variable(r.nextBoolean());
	}

	static Model getRandomInstanceWithExistentials(Random r, Schema schema) {
		List<Triple_Pattern> schema_patterns = new ArrayList<Triple_Pattern>(schema.getSchema_Graph());
		Model model = ModelFactory.createDefaultModel() ;
		for(int i = 0; i < r.nextInt(maxInstances);i++) {
			int patternChosen = r.nextInt(schema_patterns.size());
			Statement s = ResourceFactory.createStatement(
					Util.instantiateSubject(r,schema_patterns.get(patternChosen).getSubject()), 
					Util.instantiatePredicate(r,schema_patterns.get(patternChosen).getPredicate()), 
					Util.instantiateObject(r,schema_patterns.get(patternChosen).getObject()));
			model.add(s);
		}
		
		boolean all_satisfied = false;
		// exit the loop only when all of the constraints are satisfied
		while(!all_satisfied) {
			all_satisfied = true;
			for(Existential_Constraint constraint : schema.getSchema_Existentials()) {
				while(!Util.isExistentialConstraintValid(model, constraint)) {
					all_satisfied = false;
					Util.satisfyConstraint(r,model, constraint);
				}
			}
		}
		return model;
	}
	static Model getRandomInstance(Random r, List<Triple_Pattern> schema) {
		Model model = ModelFactory.createDefaultModel() ;
		for(int i = 0; i < r.nextInt(maxInstances);i++) {
			int patternChosen = r.nextInt(schema.size());
			Statement s = ResourceFactory.createStatement(
					Util.instantiateSubject(r,schema.get(patternChosen).getSubject()), 
					Util.instantiatePredicate(r,schema.get(patternChosen).getPredicate()), 
					Util.instantiateObject(r,schema.get(patternChosen).getObject()));
			model.add(s);
		}
		return model;
	}

	static boolean testRandom(int seed) {
		boolean result = true;
		Random r = new Random(seed);
		List<Triple_Pattern> schema = new LinkedList<Triple_Pattern>();
		//generate random schema
		for(int i = 0; i < 1+r.nextInt(maxSchemaTriples-1); i++) {
			schema.add(getRandomTriplePattern(r));			
		}
		// generate random positive instance
		Model instance = getRandomInstance(r,schema);
		result = result && testClass.validateTrue( TopBraid_SHACL_wrapper.validate(instance, new HashSet<Triple_Pattern>(schema)));
		
		Model noninstance = testClass.getRandomNonInstance(r,schema, null);
		if(noninstance == null) {
			//System.out.println("    ... skipped because a non-instance could not be found after 5.000 attempts ...");
		} else {
			result = result && testClass.validateFalse( TopBraid_SHACL_wrapper.validate(noninstance, new HashSet<Triple_Pattern>(schema)));
		}
		if(!result) {
			System.out.println("FAILURE FOUND FOR seed: "+seed);
			
		}
		
		return result;
	}

	static long pos_counter = 0;
	static long neg_counter = 0;
	static List<String> errors = new LinkedList<String>();
	static boolean testSystematicPair(List<Triple_Pattern> schema, List<Triple_Pattern> instance) {
		Model m = Util.convertInstanceSchemaToModel(instance);
		boolean validationSHACL = TopBraid_SHACL_wrapper.validate(m, new HashSet<Triple_Pattern>(schema));
		boolean validationSCHEMA = Util.areInstancesOfSchema(instance,schema);
		/*if(validationSHACL != validationSCHEMA) {
			//System.out.print("error");
			//errors.add("Lcurrent "+Lcurrent+" Lcurrent2 "+Lcurrent2);
			errors.add("Lc "+Lcurrent+" Lc2 "+Lcurrent2);
		}*/
		if(validationSHACL) pos_counter++;
		else neg_counter++;
		return validationSHACL ==validationSCHEMA;
	}

	/**
	 * 
	 * @return
	 */
	static boolean testSystematically(int max_schema_size, int max_instance_size) {
		long counter = 0;
		boolean result = true;
		long start = new Date().getTime();
		for(int schema_size = 1; schema_size <= max_schema_size; schema_size++) {
			
					long L = 0;
					while(L >= 0) {
						testTranslationToSHACL.Lcurrent = L;
						testTranslationToSHACL.usedURIs = 0;
						testTranslationToSHACL.usedLiterals = 0;
						testTranslationToSHACL.predicates = new LinkedList<Long>();
						// the maximum number of URI needed is schema_size*3 (if every element is a different URI)
						// the maximum number of literals needed is schema_size (if every object is a different literal)
						List<Triple_Pattern> schema = testTranslationToSHACL.generateSystematicSchema(schema_size, schema_size*3, schema_size);
						if(schema != null) {							
							for(int instance_size = 0; instance_size <= max_instance_size; instance_size++) {
								long L2 = 0;
								while(L2 >= 0) {
									testTranslationToSHACL.Lcurrent2 = L2;
									// the maximum number of URI needed is instance_size*2+usedURIs 
									//   (the ones counted in usedURIs, plus a fresh URI for every subject and object)
									// the maximum number of literals needed is instance_size+usedLiterals 
									//   (all the ones used in the schema (usedLiterals) plus a fresh one per instance triple)
									List<Triple_Pattern> instance = testTranslationToSHACL.generateInstance(instance_size, (int) (((instance_size)*2)+testTranslationToSHACL.usedURIs), (int) (instance_size+testTranslationToSHACL.usedLiterals));
									result = result && testSystematicPair(schema,instance);
									if(!result) {
										errors.add("L "+L+" L2 "+L2);
										//System.out.println("ERROR");
									}
									counter++;
									if(counter == 1000) {
										double time_passed = new Date().getTime()-start;
										System.out.println(schema+" "+result+" "+(counter/1000000.0)+"M" + " ["+((counter*1000*60*60)/time_passed)+"] {p "+pos_counter+"/n "+neg_counter+"}"+errors);//108629434
									
									}
									if(counter % 100000 == 0) {
										double time_passed = new Date().getTime()-start;
										System.out.println(schema+" "+result+" "+(counter/1000000.0)+"M" + " ["+((counter*1000*60*60)/time_passed)+"] {p "+pos_counter+"/n "+neg_counter+"}"+errors);//108629434
									
									}
									if(testTranslationToSHACL.Lcurrent2 == 0) L2++;
									else {
										L2 = -1;
									}
								}
							}
						}
						if(testTranslationToSHACL.Lcurrent == 0) L++;
						else {
							L = -1;
						}
					}
		}
		double time_passed = new Date().getTime()-start;
		System.out.println(counter);
		System.out.println("\nFINAL RESULT: "+result+" "+(counter/1000000.0)+"M" + " ["+((counter*1000*60*60)/time_passed)+"] {p "+pos_counter+"/n "+neg_counter+"}"+errors);
		
		return result;
	}

	static long Lcurrent;
	static long Lcurrent2;
	static List<Triple_Pattern> generateSystematicSchema(int schema_size, int max_uri, int max_lit){
		List<Triple_Pattern> schema = new LinkedList<Triple_Pattern>();
		boolean some_nulls = false;
		for(int i = 0; i < schema_size; i++) {
			Triple_Pattern tp = testTranslationToSHACL.generateSystematicTriplePattern(max_uri, max_lit);
			schema.add(tp);
			some_nulls = some_nulls || tp == null;
		}
		if(some_nulls) return null;
		return schema;
	}

	static List<Triple_Pattern> generateInstance(int schema_size, int max_uri, int max_lit){
		List<Triple_Pattern> schema = new LinkedList<Triple_Pattern>();
		for(int i = 0; i < schema_size; i++) {
			schema.add(testTranslationToSHACL.generateSystematicTriple(max_uri, max_lit));
		}
		return schema;
	}

	static long usedURIs;
	static long usedLiterals;
	static List<Long> predicates;
	static Triple_Pattern generateSystematicTriplePattern(int max_uri, int max_lit){
		Element subject = testTranslationToSHACL.generateSystematicSubjectPattern(max_uri, max_lit);
		Element predicate = testTranslationToSHACL.generateSystematicPredicatePattern(max_uri, max_lit);
		Element object = testTranslationToSHACL.generateSystematicObjectPattern(max_uri, max_lit);
		if(subject != null && predicate != null && object != null)
			return new Triple_Pattern(subject, predicate, object);
		else return null;
	}

	static Element generateSystematicSubjectPattern(int max_uri, int max_lit) {
		long subjectPossibilities = 1+max_uri;
		long subjectChosen = Math.round(Math.floorMod(Lcurrent, subjectPossibilities));
		Lcurrent = Math.round(Lcurrent/subjectPossibilities);
		if(subjectChosen == 0) {
			return new Variable(true);
		} else {
			subjectChosen = subjectChosen-1;
			Element el = new URI(testClass.ns,""+subjectChosen);
			if(subjectChosen == usedURIs) {
				usedURIs++;
			} else if(subjectChosen > usedURIs) {
				return null;
			}
			//if(subjectChosen >= usedURIs) usedURIs++;
			return el ;
		}
	}

	static Element generateSystematicPredicatePattern(int max_uri, int max_lit) {
		long subjectPossibilities = max_uri;
		long subjectChosen = Math.round(Math.floorMod(Lcurrent, subjectPossibilities));
		Lcurrent = Math.round(Lcurrent/subjectPossibilities);
		//predicates.add(subjectChosen);
		Element el = new URI(testClass.ns,""+subjectChosen);
		if(!predicates.contains(new Long(subjectChosen)))
			predicates.add(new Long(subjectChosen));
		if(subjectChosen == usedURIs) {
			usedURIs++;
		} else if(subjectChosen > usedURIs) {
			return null;
		}
		//if(subjectChosen >= usedURIs) usedURIs++;
		return el;
	}

	static Element generateSystematicObjectPattern(int max_uri, int max_lit) {
		long subjectTypeChosen = Math.round(Math.floorMod(Lcurrent, 2+max_uri+max_lit));
		Lcurrent = Math.round(Lcurrent/(2+max_uri+max_lit));
		if(subjectTypeChosen == 0) {
			return new Variable(true);
		} else if(subjectTypeChosen == 1) {
			return new Variable(false);
		} else if(subjectTypeChosen >= 2 && subjectTypeChosen < 2+max_uri) {
			//long subjectPossibilities = 1+usedURIs;
			//long subjectChosen = Math.round(Math.floorMod(Lcurrent, subjectPossibilities));
			long subjectChosen = subjectTypeChosen-2;
			//Lcurrent = Math.round(Lcurrent/subjectPossibilities);
			Element el = new URI(testClass.ns,""+subjectChosen);
			if(subjectChosen == usedURIs) {
				usedURIs++;
			} else if(subjectChosen > usedURIs) {
				return null;
			}
			//if(subjectChosen >= usedURIs) usedURIs++;
			return el;
		} else {
			long subjectChosen = subjectTypeChosen-2-max_uri;
			//long subjectPossibilities = 1+usedLiterals;
			//long subjectChosen = Math.round(Math.floorMod(Lcurrent, subjectPossibilities));
			//Lcurrent = Math.round(Lcurrent/subjectPossibilities);
			Element el = new Literal(""+subjectChosen);
			if(subjectChosen == usedLiterals) {
				usedLiterals++;
			} else if(subjectChosen > usedLiterals) {
				return null;
			}
			//if(subjectChosen >= usedLiterals) usedLiterals++;
			return el;
		}
		
	}

	static Triple_Pattern generateSystematicTriple(int max_uri, int max_lit){
		return new Triple_Pattern(testTranslationToSHACL.generateSystematicSubject(max_uri, max_lit), testTranslationToSHACL.generateSystematicPredicate(max_uri, max_lit), testTranslationToSHACL.generateSystematicObject(max_uri, max_lit));
	}

	static Element generateSystematicSubject(int max_uri, int max_lit) {
		long subjectPossibilities = max_uri;
		long subjectChosen = Math.round(Math.floorMod(Lcurrent2, subjectPossibilities));
		Lcurrent2 = Math.round(Lcurrent2/subjectPossibilities);
		//predicates.add(subjectChosen);
		Element el = new URI(testClass.ns,""+subjectChosen);
		//if(subjectChosen >= usedURIs) usedURIs++;
		return el;
	}

	static Element generateSystematicPredicate(int max_uri, int max_lit) {
		long subjectPossibilities = predicates.size();
		long subjectChosen = Math.round(Math.floorMod(Lcurrent2, subjectPossibilities));
		Lcurrent2 = Math.round(Lcurrent2/subjectPossibilities);
		//predicates.add(subjectChosen);
		Element el = new URI(testClass.ns,""+predicates.get((int)subjectChosen));
		//if(subjectChosen >= usedURIs) usedURIs++;
		return el;
	}

	static Element generateSystematicObject(int max_uri, int max_lit) {
		long subjectTypeChosen = Math.round(Math.floorMod(Lcurrent2, max_uri+max_lit));
		Lcurrent2 = Math.round(Lcurrent2/(max_uri+max_lit));
		if(subjectTypeChosen >= 0 && subjectTypeChosen < max_uri) {
			//long subjectPossibilities = 1+usedURIs;
			//long subjectChosen = Math.round(Math.floorMod(Lcurrent, subjectPossibilities));
			long subjectChosen = subjectTypeChosen;
			//Lcurrent = Math.round(Lcurrent/subjectPossibilities);
			Element el = new URI(testClass.ns,""+subjectChosen);
			//if(subjectChosen >= usedURIs) usedURIs++;
			return el;
		} else {
			long subjectChosen = subjectTypeChosen-max_uri;
			//long subjectPossibilities = 1+usedLiterals;
			//long subjectChosen = Math.round(Math.floorMod(Lcurrent, subjectPossibilities));
			//Lcurrent = Math.round(Lcurrent/subjectPossibilities);
			Element el = new Literal(""+subjectChosen);
			//if(subjectChosen >= usedLiterals) usedLiterals++;
			return el;
		}
		
	}

}
