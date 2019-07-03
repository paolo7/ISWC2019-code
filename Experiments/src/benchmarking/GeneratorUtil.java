package benchmarking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import core.Triple_Pattern;
import datagenerator.GPPGRuleGenerator;
import logic.Binding;
import logic.BindingImpl;
import logic.ConversionFilter;
import logic.ConversionTriple;
import logic.ConversionTripleImpl;
import logic.ExternalDB;
import logic.ExternalDB_GraphDB;
import logic.LabelService;
import logic.LabelServiceImpl;
import logic.Predicate;
import logic.PredicateExpansion;
import logic.PredicateExpansionBySPARQLquery;
import logic.PredicateImpl;
import logic.PredicateInstantiation;
import logic.PredicateInstantiationImpl;
import logic.PredicateTemplate;
import logic.PredicateTemplateImpl;
import logic.PredicateUtil;
import logic.RDFUtil;
import logic.ResourceLiteral;
import logic.ResourceURI;
import logic.Rule;
import logic.RuleImpl;
import logic.StatRecorder;
import logic.TextTemplate;
import logic.TextTemplateImpl;
import logic.VariableImpl;
import shacl.Existential_Constraint;
import shacl.Existential_Validator;
import shacl.Schema;

public class GeneratorUtil {

	public static Set<Predicate> knownPredicates = new HashSet<Predicate>();
	
	
	public static ScoreResult evaluatePerformance(int approach, int sizeOfPredicateSpace, int numOfRules, int schemaviewSize, int constraintSize, int constantPool, double constantCreationRate) throws FileNotFoundException, CloneNotSupportedException, IOException {
		return evaluatePerformance(8,2,approach, sizeOfPredicateSpace, numOfRules, schemaviewSize, constraintSize, constantPool, constantCreationRate);
	}

	/**
	 * 
	 * @param approach use 0 for GPPG, and 1 for critical instance
	 * @param sizeOfPredicateSpace
	 * @param numOfRules
	 * @param schemaviewSize
	 * @param constraintSize
	 * @param constantPool
	 * @param constantCreationRate
	 * @return
	 * @throws FileNotFoundException
	 * @throws CloneNotSupportedException
	 * @throws IOException
	 */
	public static ScoreResult evaluatePerformance(int repetitions, int cutoff, int approach, int sizeOfPredicateSpace, int numOfRules, int schemaviewSize, int constraintSize, int constantPool, double constantCreationRate) throws FileNotFoundException, CloneNotSupportedException, IOException {
		
		if(schemaviewSize > (sizeOfPredicateSpace*0.9) || schemaviewSize > sizeOfPredicateSpace-5) {
			throw new RuntimeException("ERROR, cannot instantiate "+schemaviewSize+" schema atoms with only "+sizeOfPredicateSpace+" predicates.");
		}
		List<ScoreResult> scores = new LinkedList<ScoreResult>();
		for(int i = 0; i < repetitions; i++) {
			scores.add(evaluatePerformanceIteration(approach, sizeOfPredicateSpace, numOfRules, i, schemaviewSize, constraintSize, constantPool, constantCreationRate, true));	
		}
		
		System.out.println("\n@@@@@@@@@ Configuration: sizeOfPredicateSpace: "+sizeOfPredicateSpace+" numOfRules: "+numOfRules+" schemaviewSize: "+schemaviewSize+" constraintSize: "+constraintSize+" constantPool: "+constantPool+" constantCreationRate: "+constantCreationRate+"\n"
				+ "@@@@@@@@@ Avg over "+(repetitions-cutoff)+" reps, with "+cutoff+" warmup discarded.");
		/*for(int i = 0; i < repetitions; i++) {
			System.out.println(scores.get(i));
		}*/
		
		/*long totTime = 0;
		int iterations = 0;
		int totNewPred = 0;
		
		for(int i = cutoff; i < repetitions; i++) {
			iterations++;
			totTime += scores.get(i).time;
			totNewPred += scores.get(i).newPredicates;
		}
		double avgTime = ((double)totTime)/iterations;
		double avgNewPred = ((double)totNewPred)/iterations;*/
		ScoreResult avergage = avgResult(cutoff, scores);
		System.out.println("\n@@@@@@@@@ Average result: Time: "+avergage.time+" NewPred: "+avergage.newPredicates+"\n");
		return avergage;
	}
	
	public static ScoreResult avgResult(List<ScoreResult> scores) {
		return avgResult(0, scores);
	}
	public static ScoreResult avgResult(int cutoff, List<ScoreResult> scores) {
		double totTime = 0;
		double totNewPred = 0;
		double iterations = 0;
		double timePerRule = 0;
		double applicableRules = 0;
		for(int i = cutoff; i < scores.size(); i++) {
			iterations++;
			totTime += scores.get(i).time;
			totNewPred += scores.get(i).newPredicates;
			timePerRule += scores.get(i).averageRuleApplicationTime;
			applicableRules += scores.get(i).applicableRules;
		}
		return new ScoreResult(((double)totTime)/iterations, ((double)totNewPred)/iterations, 0, timePerRule/iterations, applicableRules/iterations);
	}
	
	public static ScoreResult evaluatePerformanceIteration(int approach, int sizeOfPredicateSpace, int numOfRules, int datasetID, int schemaviewSize, int constraintSize, int constantPool, double constantCreationRate, boolean randomSchema) throws FileNotFoundException, CloneNotSupportedException, IOException {
		return evaluatePerformanceIteration(-1, approach, sizeOfPredicateSpace, numOfRules, datasetID, schemaviewSize, constraintSize, constantPool, constantCreationRate, randomSchema);
	} 
	
	public static boolean debug_mode = false;
	
	public static ScoreResult evaluatePerformanceIteration(int existentials, int approach, int sizeOfPredicateSpace, int numOfRules, int datasetID, int schemaviewSize, int constraintSize, int constantPool, double constantCreationRate, boolean randomSchema) throws FileNotFoundException, CloneNotSupportedException, IOException {
		if(debug_mode) {
			System.out.println("\n\n ### SCHEMA EXPANSION ITERATION ");
			System.out.println(" ### Method = " + (approach == 4 ? "CRITICAL" : "SCORE") + (existentials < 0 ? "(basic schema consequence)" : "(existential preserving schema consequence)"));
		}
		// load the set of predicates and rules
		Set<Predicate> returnPredicates = new HashSet<Predicate>(); 
		ArrayList<Rule> returnRulesList = new ArrayList<Rule>();
		long timea = new Date().getTime();
		generateOrLoadOrderedRules(sizeOfPredicateSpace, numOfRules, datasetID,constraintSize, constantPool, constantCreationRate, returnPredicates, returnRulesList);	
		Set<Rule> returnRules = new HashSet<Rule>(returnRulesList);
		//System.out.println(returnRules);
		//System.out.println(returnPredicates);
		// load the 
		//ExternalDB_GraphDB.setLoggingLevelToError();
		//ExternalDB eDB = new ExternalDB_GraphDB("http://10.22.15.92:7200/", "test", "temp");
		Map<String,String> prefixes = getStandardPrefixes();
		Set<PredicateInstantiation> existingPredicates = generateRandomSchema(datasetID, schemaviewSize, returnPredicates, constantPool, constantCreationRate, sizeOfPredicateSpace, randomSchema ? 0 : Math.max(0, constraintSize), returnRules);
		LabelService labelservice = new LabelServiceImpl(existingPredicates, prefixes);
		RDFUtil.labelService = labelservice;
		
		PredicateExpansion expansion = new PredicateExpansionBySPARQLquery(returnPredicates, returnRules);
		expansion.setPrefixes(prefixes);
		int schemaSize = existingPredicates.size();
		long timeb = new Date().getTime();
		//System.out.println("    * "+(timeb-timea)+" milliseconds to generate a synthetic dataset");
		List<core.Rule> rules = null;
		Schema schema = null;
		{
			rules = Existential_Validator.util_Predicate_Rules_2_Rules(returnRulesList);
			List<Triple_Pattern> allRuleConsequents = new ArrayList<Triple_Pattern>();
			for(core.Rule r : rules) allRuleConsequents.add(r.getConsequent());
			List<Triple_Pattern> allRuleAntecedents = new ArrayList<Triple_Pattern>();
			for(core.Rule r : rules) allRuleAntecedents.addAll(r.getAntecedent());
			Set<Existential_Constraint> schema_Existentials = new HashSet<Existential_Constraint>();
			// create the existential constraints
			Set<Triple_Pattern> schemaGraphPattern = Existential_Validator.util_translate_PredicateInstantiation_2_Triple_Patterns(existingPredicates);
			Random r = new Random(datasetID);
			for(int i = 0; i < existentials; i++) {
				Triple_Pattern t1 = allRuleConsequents.get(r.nextInt(allRuleConsequents.size()));
				Triple_Pattern t2 = allRuleAntecedents.get(r.nextInt(allRuleAntecedents.size()));
				Existential_Constraint e = new Existential_Constraint(t1, t2);
				schema_Existentials.add(e);
				
			}
			schema = new Schema(schemaGraphPattern,	schema_Existentials
					);
		}
		if(debug_mode) {
			System.out.println("\n # ORIGINAL RULES:");
			System.out.println(core.Rule.prettyPrintRuleset(rules));
			System.out.println("\n # ORIGINAL SCHEMA:");
			System.out.println(schema.pretty_print_string());
		}
		System.gc();
		long time1 = new Date().getTime();
		StatRecorder sr = new StatRecorder();
		ScoreResult result = null;
		Set<Existential_Constraint> retained_constraints = schema.getSchema_Existentials();
		Set<PredicateInstantiation> newPredicates = null;
		if(existentials < 0 || debug_mode) {
			newPredicates = expansion.expand(approach, existingPredicates,sr);
			int newschemaSize = newPredicates.size();
			long time2 = new Date().getTime();
			RDFUtil.filterRedundantPredicates(existingPredicates, newPredicates, false, false);
			result = new ScoreResult(time2-time1, newschemaSize, 0, sr.getAvgTime(), sr.applicableRules.size());
		} 
		if(existentials >= 0){
			retained_constraints = Existential_Validator.validate(schema, new HashSet<core.Rule>(rules));
			long time2 = new Date().getTime();
			result = new ScoreResult(time2-time1, -1, 0, sr.getAvgTime(), sr.applicableRules.size());
		}
		if(debug_mode) {
			newPredicates.addAll(existingPredicates);
			Set<Triple_Pattern> newSchemaGraphPattern = Existential_Validator.util_translate_PredicateInstantiation_2_Triple_Patterns(newPredicates);
			Schema expandedSchema = new Schema(newSchemaGraphPattern,	retained_constraints );
			System.out.println(" # SCHEMA AFTER EXPANSION:");
			System.out.println(expandedSchema.pretty_print_string());
		}
		return result;
		/*int newSchemaNoVars = 0;
		for(PredicateInstantiation pi : newPredicates) {
			boolean allVars = true;
			for(Binding b: pi.getBindings()) {
				if(b.isConstant()) 
					allVars = false;
			}
			if(allVars) 
				newSchemaNoVars++;
		}*/
	}
	

	public static boolean testSemanticEqualityOfApproaches(int sizeOfPredicateSpace, int numOfRules, int datasetID, int schemaviewSize, int constraintSize, int constantPool, double constantCreationRate) throws FileNotFoundException, CloneNotSupportedException, IOException {
		// load the set of predicates and rules
		
		Set<PredicateInstantiation> newPredicates3;
		Set<PredicateInstantiation> newPredicates4;
		{
			Set<Predicate> returnPredicates = new HashSet<Predicate>(); 
			Set<Rule> returnRules = new HashSet<Rule>();
			generateOrLoad(sizeOfPredicateSpace, numOfRules, datasetID,constraintSize, constantPool, constantCreationRate, returnPredicates, returnRules);	
			Map<String,String> prefixes = getStandardPrefixes();
			Set<PredicateInstantiation> existingPredicates = generateRandomSchema(datasetID, schemaviewSize, returnPredicates, constantPool, constantCreationRate, sizeOfPredicateSpace);
			LabelService labelservice = new LabelServiceImpl(existingPredicates, prefixes);
			RDFUtil.labelService = labelservice;
			PredicateExpansion expansion = new PredicateExpansionBySPARQLquery(returnPredicates, returnRules);
			expansion.setPrefixes(prefixes);
			newPredicates3 = expansion.expand(3, existingPredicates);
			RDFUtil.filterRedundantPredicates(existingPredicates, newPredicates3, false, false);			
		}
		{
			Set<Predicate> returnPredicates = new HashSet<Predicate>(); 
			Set<Rule> returnRules = new HashSet<Rule>();
			generateOrLoad(sizeOfPredicateSpace, numOfRules, datasetID,constraintSize, constantPool, constantCreationRate, returnPredicates, returnRules);	
			Map<String,String> prefixes = getStandardPrefixes();
			Set<PredicateInstantiation> existingPredicates = generateRandomSchema(datasetID, schemaviewSize, returnPredicates, constantPool, constantCreationRate, sizeOfPredicateSpace);
			LabelService labelservice = new LabelServiceImpl(existingPredicates, prefixes);
			RDFUtil.labelService = labelservice;
			PredicateExpansion expansion = new PredicateExpansionBySPARQLquery(returnPredicates, returnRules);
			expansion.setPrefixes(prefixes);
			newPredicates4 = expansion.expand(4, existingPredicates);
			RDFUtil.filterRedundantPredicates(existingPredicates, newPredicates4, false, false);			
		}
		boolean result = testSemanticEquivalence(newPredicates3, newPredicates4);
		
		
		
		
		/*if(newPredicates3.size() > 0) {
			for(PredicateInstantiation pi: newPredicates3) {
				for(Binding b : pi.getBindings()) {
					if(b.isConstant() && b.getConstant().isLiteral()) {
						System.out.println("");
						
					}
				}
			}
			for(PredicateInstantiation pi: newPredicates4) {
				for(Binding b : pi.getBindings()) {
					if(b.isConstant() && b.getConstant().isLiteral()) {
						System.out.println("");
						
					}
				}
			}
		}
		if(!result) {
			{
				Set<Predicate> returnPredicates = new HashSet<Predicate>(); 
				Set<Rule> returnRules = new HashSet<Rule>();
				generateOrLoad(sizeOfPredicateSpace, numOfRules, datasetID,constraintSize, constantPool, constantCreationRate, returnPredicates, returnRules);	
				Map<String,String> prefixes = getStandardPrefixes();
				Set<PredicateInstantiation> existingPredicates = generateRandomSchema(datasetID, schemaviewSize, returnPredicates, constantPool, constantCreationRate, sizeOfPredicateSpace);
				LabelService labelservice = new LabelServiceImpl(existingPredicates, prefixes);
				RDFUtil.labelService = labelservice;
				PredicateExpansion expansion = new PredicateExpansionBySPARQLquery(returnPredicates, returnRules);
				expansion.setPrefixes(prefixes);
				newPredicates3 = expansion.expand(3, existingPredicates);
				RDFUtil.filterRedundantPredicates(existingPredicates, newPredicates3, false, false);			
			}
			{
				Set<Predicate> returnPredicates = new HashSet<Predicate>(); 
				Set<Rule> returnRules = new HashSet<Rule>();
				generateOrLoad(sizeOfPredicateSpace, numOfRules, datasetID,constraintSize, constantPool, constantCreationRate, returnPredicates, returnRules);	
				Map<String,String> prefixes = getStandardPrefixes();
				Set<PredicateInstantiation> existingPredicates = generateRandomSchema(datasetID, schemaviewSize, returnPredicates, constantPool, constantCreationRate, sizeOfPredicateSpace);
				LabelService labelservice = new LabelServiceImpl(existingPredicates, prefixes);
				RDFUtil.labelService = labelservice;
				PredicateExpansion expansion = new PredicateExpansionBySPARQLquery(returnPredicates, returnRules);
				expansion.setPrefixes(prefixes);
				newPredicates4 = expansion.expand(4, existingPredicates);
				RDFUtil.filterRedundantPredicates(existingPredicates, newPredicates4, false, false);			
			}
			
			testSemanticEquivalence(newPredicates3, newPredicates4);
		}*/
			
			
			
			
		System.out.println("\nAre those results equal? "+result+ "\nTotal number of bindings found by approaches a) and b): ["+newPredicates3.size()+"], ["+newPredicates4.size()+"]");		
		
		return result;
	}
	
	public static boolean testSemanticEqualityOfApproachesOld(int sizeOfPredicateSpace, int numOfRules, int datasetID, int schemaviewSize, int constraintSize, int constantPool, double constantCreationRate) throws FileNotFoundException, CloneNotSupportedException, IOException {
		// load the set of predicates and rules
		
		Set<PredicateInstantiation> newPredicates1;
		Set<PredicateInstantiation> newPredicates2;
		Set<PredicateInstantiation> newPredicates3;
		{
			Set<Predicate> returnPredicates = new HashSet<Predicate>(); 
			Set<Rule> returnRules = new HashSet<Rule>();
			generateOrLoad(sizeOfPredicateSpace, numOfRules, datasetID,constraintSize, constantPool, constantCreationRate, returnPredicates, returnRules);	
			Map<String,String> prefixes = getStandardPrefixes();
			Set<PredicateInstantiation> existingPredicates = generateRandomSchema(datasetID, schemaviewSize, returnPredicates, constantPool, constantCreationRate, sizeOfPredicateSpace);
			LabelService labelservice = new LabelServiceImpl(existingPredicates, prefixes);
			RDFUtil.labelService = labelservice;
			PredicateExpansion expansion = new PredicateExpansionBySPARQLquery(returnPredicates, returnRules);
			expansion.setPrefixes(prefixes);
			newPredicates1 = expansion.expand(0, existingPredicates);
			RDFUtil.filterRedundantPredicates(existingPredicates, newPredicates1, false, false);			
		}
		{
			Set<Predicate> returnPredicates = new HashSet<Predicate>(); 
			Set<Rule> returnRules = new HashSet<Rule>();
			generateOrLoad(sizeOfPredicateSpace, numOfRules, datasetID,constraintSize, constantPool, constantCreationRate, returnPredicates, returnRules);	
			Map<String,String> prefixes = getStandardPrefixes();
			Set<PredicateInstantiation> existingPredicates = generateRandomSchema(datasetID, schemaviewSize, returnPredicates, constantPool, constantCreationRate, sizeOfPredicateSpace);
			LabelService labelservice = new LabelServiceImpl(existingPredicates, prefixes);
			RDFUtil.labelService = labelservice;
			PredicateExpansion expansion = new PredicateExpansionBySPARQLquery(returnPredicates, returnRules);
			expansion.setPrefixes(prefixes);
			newPredicates2 = expansion.expand(1, existingPredicates);
			RDFUtil.filterRedundantPredicates(existingPredicates, newPredicates2, false, false);			
		}
		{
			Set<Predicate> returnPredicates = new HashSet<Predicate>(); 
			Set<Rule> returnRules = new HashSet<Rule>();
			generateOrLoad(sizeOfPredicateSpace, numOfRules, datasetID,constraintSize, constantPool, constantCreationRate, returnPredicates, returnRules);	
			Map<String,String> prefixes = getStandardPrefixes();
			Set<PredicateInstantiation> existingPredicates = generateRandomSchema(datasetID, schemaviewSize, returnPredicates, constantPool, constantCreationRate, sizeOfPredicateSpace);
			LabelService labelservice = new LabelServiceImpl(existingPredicates, prefixes);
			RDFUtil.labelService = labelservice;
			PredicateExpansion expansion = new PredicateExpansionBySPARQLquery(returnPredicates, returnRules);
			expansion.setPrefixes(prefixes);
			newPredicates3 = expansion.expand(2, existingPredicates);
			RDFUtil.filterRedundantPredicates(existingPredicates, newPredicates2, false, false);			
		}
		boolean result = testSemanticEquivalence(newPredicates1, newPredicates2) && testSemanticEquivalence(newPredicates1, newPredicates3);
		System.out.println("Equal: "+result+ "       ["+newPredicates1.size()+"], ["+newPredicates2.size()+"], ["+newPredicates3.size()+"]");	
		
		return result;
	}
	
	public static boolean testSemanticEquivalence(Set<PredicateInstantiation> newPredicates1, Set<PredicateInstantiation> newPredicates2) {
		for(PredicateInstantiation pi1: newPredicates1) {
			boolean equivalentExists = false;
			for(PredicateInstantiation pi2: newPredicates2) {
				if( (RDFUtil.isSubsumedBy(pi1,pi2, false) && RDFUtil.isSubsumedBy(pi2,pi1, false))) 
					equivalentExists = true;
			}
			if(! equivalentExists) 
				return false;
		}
		return true;
	}


	private static Set<PredicateInstantiation> generateRandomSchema(int datasetID, int schemaviewSize, Set<Predicate> predicates, int constantPool, double constantCreationRate, int sizeOfPredicateSpace){
		return generateRandomSchema(datasetID, schemaviewSize, predicates, constantPool, constantCreationRate, sizeOfPredicateSpace,0, null);
	}

	public static SortedSet<Rule> getOrderedSet(Collection<Rule> rules) {
		SortedSet<Rule> orderedSet = new TreeSet<Rule>(Comparator.comparing(Rule::toString));
		orderedSet.addAll(rules);
		return orderedSet;
	}
	
	private static Set<PredicateInstantiation> generateRandomSchema(int datasetID, int schemaviewSize, Set<Predicate> predicates, int constantPool, double constantCreationRate, int sizeOfPredicateSpace, int antecedentsPredicatesToReuse, Set<Rule> returnRules){
		Set<PredicateInstantiation> existingPredicates = new LinkedHashSet<PredicateInstantiation>();
		Random rDet = new Random(datasetID);
		int maxRep = 300000;
		if(antecedentsPredicatesToReuse > 0) {
			// first try to reuse predicates from the rules
			for(Rule r: getOrderedSet(returnRules)) {
				if(existingPredicates.size() < schemaviewSize/2) {
				//int predicatesFromRule = 0;
				for(PredicateInstantiation pi: r.getAntecedent()) {
					//if(predicatesFromRule < antecedentsPredicatesToReuse) {
						//predicatesFromRule++;
						existingPredicates.add(pi);
					//}
					}
					//existingPredicates.add(generateRandomSchemaHelper(rDet, predicates, constantPool, constantCreationRate, sizeOfPredicateSpace));
				}
				/*if(existingPredicates.size() >= schemaviewSize) {
					RDFUtil.filterRedundantPredicates(existingPredicates, false, false);
					// if reused axioms are enough, return them without adding any random ones
					if(existingPredicates.size() >= schemaviewSize) {
						while(existingPredicates.size() > schemaviewSize) {
							existingPredicates.remove(existingPredicates.iterator().next());
						}
						return existingPredicates;
					}
				}*/
			}
			
		}
		while(existingPredicates.size() < schemaviewSize) {
			while(existingPredicates.size() < (schemaviewSize > 100 ? schemaviewSize*1.2 : schemaviewSize) ) {
				// create new random predicates
				existingPredicates.add(generateRandomSchemaHelper(rDet, predicates, constantPool, constantCreationRate, sizeOfPredicateSpace));
				maxRep--;
				if(maxRep < 0) 
					throw new RuntimeException("The loop to create a random schema is taking too long.");
			}
			RDFUtil.filterRedundantPredicates(existingPredicates, false, false);
			while(existingPredicates.size() > schemaviewSize) {
				existingPredicates.remove(existingPredicates.iterator().next());
			}
		}
		return existingPredicates;
	}
	
	private static PredicateInstantiation generateRandomSchemaHelper(Random rDet, Set<Predicate> predicates, int constantPool, double constantCreationRate, int sizeOfPredicateSpace){
		Binding[] bindings = new Binding[2];
		for(int i = 0; i < bindings.length; i++) {
			if(rDet.nextDouble() < constantCreationRate) {
				if(rDet.nextBoolean() && i == 1) {
					bindings[i] = new BindingImpl(new ResourceLiteral("LIT"+rDet.nextInt(constantPool), XMLSchema.STRING));	
				} else {					
					bindings[i] = new BindingImpl(new ResourceURI("e:C"+rDet.nextInt(constantPool)));					
				}
			} else {
				bindings[i] = new BindingImpl(new VariableImpl(i, i == 1));
			}
			
		}
		String predicateName = "m"+rDet.nextInt(sizeOfPredicateSpace);
		
		if(!PredicateUtil.containsOne(predicateName, 2, predicates)) {
			predicates.add(instantiateBinaryPredicateIfNecessary(predicateName));
		}
		return new PredicateInstantiationImpl(PredicateUtil.get(predicateName, 2, predicates), bindings);
	}
	
/*	private static <T> Object getRandomObject(Collection<T> from) {
		   int i = GPPGRuleGenerator.r.nextInt(from.size());
		   return from.toArray()[i];
	}*/
	
	public static Map<String,String> getStandardPrefixes(){
		Map<String,String> prefixes = new HashMap<String,String>();
		prefixes.put("e", "http://example.com/");
		prefixes.put("ex", "http://example.com/");
		RDFUtil.addToDefaultPrefixes(prefixes);
		return prefixes;
	}
	
	/**
	 * number of rules is always fixed to 1000, if one needs less, they can always ignore some lines
	 * @param constraintSize the number of atoms to have in the antecedent
	 * @throws CloneNotSupportedException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void generateOrLoad(int sizeOfPredicateSpace, int numOfRules, int datasetID, int constraintSize, int constantPool, double constantCreationRate, Set<Predicate> returnPredicates, Set<Rule> returnRules) throws CloneNotSupportedException, FileNotFoundException, IOException {
		knownPredicates = new HashSet<Predicate>();
		String outputdir = System.getProperty("user.dir")+"/chasebench/GPPG/";
		int numberOfconstraintsPerDataSet = 500;
		int arity = 2;
		int maxNoOfRepeteadRelsPerConstraint = 2;
		GPPGRuleGenerator.generateChaseConstraintData(outputdir, datasetID,numberOfconstraintsPerDataSet,sizeOfPredicateSpace, constraintSize, arity, maxNoOfRepeteadRelsPerConstraint, constantPool, constantCreationRate);
		File file = new File(GPPGRuleGenerator.getFileLocation(outputdir,datasetID,numberOfconstraintsPerDataSet,sizeOfPredicateSpace, constraintSize, arity, maxNoOfRepeteadRelsPerConstraint, constantPool, constantCreationRate));
		int count = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line;
		    while ((line = br.readLine()) != null && count < numOfRules) {
		    	count++;
		    	Rule r = processLine(line);
		    	returnRules.add(r);
		    	
		    }
		}
		returnPredicates.addAll(knownPredicates);
	}
	public static void generateOrLoadOrderedRules(int sizeOfPredicateSpace, int numOfRules, int datasetID, int constraintSize, int constantPool, double constantCreationRate, Set<Predicate> returnPredicates, List<Rule> returnRules) throws CloneNotSupportedException, FileNotFoundException, IOException {
		knownPredicates = new HashSet<Predicate>();
		String outputdir = System.getProperty("user.dir")+"/chasebench/GPPG/";
		int numberOfconstraintsPerDataSet = 500;
		int arity = 2;
		int maxNoOfRepeteadRelsPerConstraint = 2;
		GPPGRuleGenerator.generateChaseConstraintData(outputdir, datasetID,numberOfconstraintsPerDataSet,sizeOfPredicateSpace, constraintSize, arity, maxNoOfRepeteadRelsPerConstraint, constantPool, constantCreationRate);
		File file = new File(GPPGRuleGenerator.getFileLocation(outputdir,datasetID,numberOfconstraintsPerDataSet,sizeOfPredicateSpace, constraintSize, arity, maxNoOfRepeteadRelsPerConstraint, constantPool, constantCreationRate));
		int count = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line;
		    while ((line = br.readLine()) != null && count < numOfRules) {
		    	count++;
		    	Rule r = processLine(line);
		    	returnRules.add(r);
		    	
		    }
		}
		returnPredicates.addAll(knownPredicates);
	}
	
	
	private static Rule processLine(String line) {
		String[] headTail = line.split(":-");
		Set<PredicateTemplate> head = asSetOfPredicateTemplates(processHead(headTail[0]));
		Set<PredicateInstantiation> antecedent = processAntecedent(headTail[1]);
		return new RuleImpl(antecedent, head);
		//return new RuleImpl();
	}
	
	private static  PredicateInstantiation processHead(String head) {
		return processAtom(head);
	}
	private static Set<PredicateInstantiation> processAntecedent(String antecedent) {
		Set<PredicateInstantiation> ant = new HashSet<PredicateInstantiation>(); 
		String[] predicates = antecedent.split("\\),");
		for(String atom : predicates) {
			ant.add(processAtom(atom.indexOf(")") > 0 ? atom : atom+")"));
			
			//public PredicateInstantiationImpl(Predicate predicate, Binding[] bindings) {
		}
		return ant;
	}
	private static PredicateInstantiation processAtom(String atom) {
		Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(atom);
		String variables = null;
	    if(m.find()) {
	    	variables = m.group(1);    
	    }
	    String[] variableTokens = new String[0];
	    if(variables != null) variableTokens = variables.split(",");
	    String predicatename = atom.replaceAll("\\(.*\\)", "").trim();
	    
	    Predicate p = instantiateBinaryPredicateIfNecessary(predicatename);
	    Binding[] bindings = new Binding[2];
	    for(int i = 0; i < variableTokens.length; i++) {
	    	if(isInteger(variableTokens[i])) {
	    		// it is a var
	    		if(i==0) {
	    			bindings[i] = new BindingImpl(new VariableImpl(new Integer(variableTokens[i]), false));
	    		} else {
	    			bindings[i] = new BindingImpl(new VariableImpl(new Integer(variableTokens[i]), true));
	    		}
	    	} else {
	    		// it is a constant
	    		
	    		if(variableTokens[i].startsWith("\"") && variableTokens[i].endsWith("\"")) {
	    			// if it contains double quotes it is a literal
	    			bindings[i] =  new BindingImpl(new ResourceLiteral(variableTokens[i].substring(1, variableTokens[i].length()-1), XMLSchema.STRING));	    			
	    		} else {
	    			// else it is a URI, blank nodes are not considered
	    			bindings[i] =  new BindingImpl(new ResourceURI(variableTokens[i]));	 
	    		}
	    	}
	    }
	    PredicateInstantiation pi = new PredicateInstantiationImpl(p, bindings);
	    return pi;
	}
	
	public static Set<PredicateTemplate> asSetOfPredicateTemplates(PredicateInstantiation pi){
		Set<PredicateTemplate> pt = new HashSet<PredicateTemplate>();
		List<TextTemplate> textLabel = new LinkedList<TextTemplate>();
		textLabel.add(new TextTemplateImpl(pi.getPredicate().getName()));
		pt.add(new PredicateTemplateImpl(textLabel, pi.getPredicate(), pi.getBindings()));
		return pt;
	}
	
	public static PredicateInstantiation asPredicateInstantiations(PredicateTemplate pt){
		PredicateInstantiation pi =  new PredicateInstantiationImpl(pt.getPredicateIfExists(), pt.getBindings());
		return pi;
	}
	
	private static boolean isInteger(String input) {
	    try {
	        Integer.parseInt(input);
	        return true;
	    }
	    catch(Exception e) {
	        return false;
	    }
	}
	
	public static Predicate instantiateBinaryPredicateIfNecessary(String predicateName) {
		if(!PredicateUtil.containsOne(predicateName, 2, knownPredicates)) {
			Set<ConversionTriple> translationToRDF = new HashSet<ConversionTriple>();
			translationToRDF.add(new ConversionTripleImpl(new BindingImpl(new VariableImpl(0)), 
					new BindingImpl(new ResourceURI(predicateName.indexOf("http") != 0 ? "e:"+predicateName : predicateName)), new BindingImpl(new VariableImpl(1))));
			Set<ConversionFilter> translationToRDFFilters =  new HashSet<ConversionFilter>();
			List<TextTemplate> textLabel = new LinkedList<TextTemplate>();
			textLabel.add(new TextTemplateImpl(0));
			textLabel.add(new TextTemplateImpl(predicateName));
			textLabel.add(new TextTemplateImpl(1));
			knownPredicates.add(new PredicateImpl(predicateName, 2, translationToRDF, translationToRDFFilters, textLabel));
		}
		return PredicateUtil.get(predicateName, 2, knownPredicates);
	}
}
