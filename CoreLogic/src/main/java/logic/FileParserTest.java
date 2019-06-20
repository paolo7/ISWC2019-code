package logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogManager;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

import GraphDB.QueryUtil;

public class FileParserTest {

	
	/*public static void setLoggingLevel(ch.qos.logback.classic.Level level) {
	    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
	    root.setLevel(level);
	}	*/


	
	public static void main(String[] args) throws Exception {
		
		/*String rulefile = System.getProperty("user.dir") + "/resources/rulesBasic01.txt";
		String[] vocabularyFiles = new String[] {};
		boolean evaluateDatabaseInstance = false;*/
		
		
		String rulefile = System.getProperty("user.dir") + "/resources/rulesLiteral1.txt";
		String[] vocabularyFiles = new String[] {};
		/*vocabularyFiles = new String[] {
				System.getProperty("user.dir") + "/resources/vocabularies/SSN.ttl",
				System.getProperty("user.dir") + "/resources/vocabularies/rdf.ttl",
				System.getProperty("user.dir") + "/resources/vocabularies/rdfs.ttl"
		};*/
		boolean evaluateDatabaseInstance = false;
		
		
		////
		//setLoggingLevel(ch.qos.logback.classic.Level.ERROR);
		
		
		
		//String[] vocabularyFiles = new String[] {};
		
		
		// this implementation uses GraphDB as an external triplestore that is GeoSparql enabled.
		// but any other triplestore that follows the rdf4j framework should be compatible
		ExternalDB eDB = new ExternalDB_GraphDB("http://localhost:7200/", "test", "temp");
		if(evaluateDatabaseInstance) {
			eDB.loadRDF(new File(System.getProperty("user.dir")+"/resources/localRDF.ttl"), RDFFormat.TURTLE);
			System.out.println("Loaded dataset with "+eDB.countTriples()+" triples.");
		}
		for(String filepath : vocabularyFiles) {
			eDB.loadRDF(new File(filepath), RDFFormat.TURTLE);
		}
		
		Map<String,String> prefixes = FileParser.parsePrefixes(System.getProperty("user.dir") + "/resources/prefixes.txt");

		
		
		
		Set<Predicate> predicates = new HashSet<Predicate>();
		Set<Rule> rules = new HashSet<Rule>();
		Set<PredicateInstantiation> existingPredicates = new HashSet<PredicateInstantiation>();
		Set<PredicateInstantiation> printPredicates = new HashSet<PredicateInstantiation>();
		
		FileParser.parse(rulefile, predicates, rules, existingPredicates, printPredicates, true, eDB);
		if(evaluateDatabaseInstance) {
			PredicateEvaluation.computeRuleClosure(eDB, rules, predicates);
		}
		
		LabelService labelservice = new LabelServiceImpl(existingPredicates, prefixes);
		
		RDFUtil.labelService = labelservice;
		
		
		// additional vocabularies:
		Model additionalVocabularies = ModelFactory.createDefaultModel();
		for(String filepath : vocabularyFiles) {
			Model externalVocabulary = RDFUtil.loadModel(filepath);
			RDFUtil.loadLabelsFromModel(externalVocabulary);
			additionalVocabularies.add(externalVocabulary);
		}
		RDFUtil.addToDefaultPrefixes(prefixes);
		RDFUtil.addToDefaultPrefixes(additionalVocabularies);
		/*Model ontologySSN = RDFUtil.loadModel(System.getProperty("user.dir") + "/resources/vocabularies/SSN.ttl");
		RDFUtil.loadLabelsFromModel(ontologySSN);
		Model rdf = RDFUtil.loadModel(System.getProperty("user.dir") + "/resources/vocabularies/rdf.ttl");
		RDFUtil.loadLabelsFromModel(rdf);
		Model rdfs = RDFUtil.loadModel(System.getProperty("user.dir") + "/resources/vocabularies/rdfs.ttl");
		RDFUtil.loadLabelsFromModel(rdfs);*/
		
		/*additionalVocabularies.add(ontologySSN);
		additionalVocabularies.add(rdf);
		additionalVocabularies.add(rdfs);*/
		
		System.out.println("*************** KNOWN PREDICATES\n" + 
				"*************** These are the definitions of the predicates that we want to consider\n");
		
		for(Predicate p: predicates) {
			System.out.println(p);
		}
		
		System.out.println("*************** RULES\n" + 
				"*************** These are rules that we want to apply\n");
		
		for(Rule r: rules) {
			System.out.println(r);
		}
		
		System.out.println("*************** AVAILABLE PREDICATES\n" + 
				"*************** These are the predicates that we assume are available from the start, i.e. the predicates that we can infer from a database\n");
		
		for(PredicateInstantiation p: existingPredicates) {
			System.out.println("AVAILABLE PREDICATE: "+p+"\n");
		}
		
		
		for(String s: prefixes.keySet()) {
			eDB.setNamespace(s,prefixes.get(s));
		}
		//System.out.println("\n*************** CHECKING PREDICATED ON TRIPLESTORE BEFORE EXPANSION (known predicates: "+predicates.size()+")\n");
		
		//PredicateEvaluation.evaluate(eDB, existingPredicates);
		
		
		LogManager.getLogManager().reset();
		System.out.println("*************** APPLYING EXPANSION\n");
		PredicateExpansion expansion = new PredicateExpansionBySPARQLquery(predicates, rules, additionalVocabularies);
		expansion.setPrefixes(prefixes);
		Set<PredicateInstantiation> newPredicates = expansion.expand(3,existingPredicates);
		predicates = expansion.getPredicates();
		
		System.out.println("*************** INFERRED PREDICATES\n" + 
				"*************** These are the predicates that we can derive from the ones that we assume to be available\n");
		
		for(PredicateInstantiation p: newPredicates) {
			System.out.println("AVAILABLE PREDICATE: "+p+"\n----\n"+p.getPredicate()+"----\n");
		}
		
		if(evaluateDatabaseInstance) {			
			PredicateEvaluation.computeRuleClosure(eDB, rules, predicates);
			PredicateEvaluation.evaluate(eDB, newPredicates);
		}

	
		// output results as JSON
		existingPredicates.addAll(newPredicates);
		JSONoutput.outputAsJSON("JSONoutput.json", existingPredicates);
		JSONoutput.outputAsJSON("JSONoutput2.json", printPredicates);
		//System.out.println("\n*************** CHECKING INFERRED PREDICATES ON TRIPLESTORE (known predicates: "+predicates.size()+")\n");
		
		
		eDB.clearDB();
	}
	
	
	
	public static void testGeo(ExternalDB eDB) {
		/*SPARQLRepository q = new SPARQLRepository("http://152.78.64.224:7200/repositories/test1");
		q.initialize();
		RepositoryConnection connection = q.getConnection();
		TupleQueryResult result = QueryUtil.evaluateSelectQuery(connection,"PREFIX my: <http://example.org/ApplicationSchema#>\n" + 
				"PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" + 
				"PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" + 
				"\n" + 
				"SELECT ?a ?b ?aWKT ?bWKT \n" + 
				"WHERE {\n" + 
				"    ?a <http://www.opengis.net/rdf#hasGeometry> ?aGeom .\n" + 
				"    ?b <http://www.opengis.net/rdf#hasGeometry> ?bGeom .\n" + 
				"    ?aGeom geo:asWKT ?aWKT .\n" + 
				"    ?bGeom geo:asWKT ?bWKT .\n" + 
				"    FILTER (geof:sfContains(?aWKT, ?bWKT))\n" + 
				"}");
		while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            for(String name : bindingSet.getBindingNames()) {
            	Value v = bindingSet.getBinding(name).getValue();
            	System.out.println(name+": "+v.stringValue());
            }
            System.out.println("");
        }
        result.close();*/
        
		TupleQueryResult result = eDB.query("PREFIX my: <http://example.org/ApplicationSchema#>\n" + 
				"PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" + 
				"PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" + 
				"\n" + 
				"SELECT ?a ?b ?aWKT ?bWKT \n" + 
				"WHERE {\n" + 
				"    ?a <http://www.opengis.net/rdf#hasGeometry> ?aGeom .\n" + 
				"    ?b <http://www.opengis.net/rdf#hasGeometry> ?bGeom .\n" + 
				"    ?aGeom geo:asWKT ?aWKT .\n" + 
				"    ?bGeom geo:asWKT ?bWKT .\n" + 
				"    FILTER (geof:sfContains(?aWKT, ?bWKT))\n" + 
				"}");
		while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            for(String name : bindingSet.getBindingNames()) {
            	Value v = bindingSet.getBinding(name).getValue();
            	System.out.println(name+": "+v.stringValue());
            }
            System.out.println("");
        }
        result.close();
	}
}
