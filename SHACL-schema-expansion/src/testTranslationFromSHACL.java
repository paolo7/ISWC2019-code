import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.jena.rdf.model.Model;

import core.Triple_Pattern;
import core.URI;
import core.Util;
import shacl.SHACLTranslationException;
import shacl.Schema;
import shacl.Translator_to_Graph_Pattern;

public class testTranslationFromSHACL {

	public static void test_translation_from_SHACL() {
		List<String> SHACL_models = new LinkedList<String>();
		/*SHACL_models.add("ex:s a sh:NodeShape . " + 
				"ex:s sh:targetClass ex:uri6 .  ");
		SHACL_models.add("ex:s a sh:NodeShape . " + 
				"ex:s sh:targetClass ex:uri6 .  " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . ");*/
		
		
		SHACL_models.add(                                      
				"ex:s a sh:NodeShape ; " +
				"    sh:targetObjectsOf ex:uri1 ; "+
				"      sh:class ex:uri2 . " +
				"ex:uri2 a sh:NodeShape ; " +
				" sh:targetObjectsOf rdf:type ; "+
				"        sh:in (ex:uri6 ex:uri7 ex:uri8) . " +               //////////////////NOT TRANSLATABLE
				"");
		
		SHACL_models.add(    
				"ex:s1 a sh:NodeShape ; " +
				"    sh:targetObjectsOf ex:uri2 ; "+
				"      sh:class ex:uri6 . " +
				"ex:s a sh:NodeShape ; " + 
				"     sh:targetClass ex:uri6 ;"
				+ "   sh:property [ "
				+ "      sh:path ex:uri4 ; "
				+ "      sh:hasValue ex:uri6 ; "
				+ "   ] "
				+ "   . " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri10 ex:uri8) . " +
				"ex:k a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"");
		
		SHACL_models.add(                               
				"ex:s a sh:NodeShape ; " + 
				"     sh:targetClass ex:uri6 ;"
				+ "   sh:property [ "
				+ "      sh:path ex:uri4 ; "
				+ "      sh:hasValue ex:uri6 ; "
				+ "   ] "
				+ "   . " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri10 ex:uri8) . " +
				"ex:uri4 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"");
		
		SHACL_models.add(     
				"ex:d a sh:NodeShape ; " + 
						"     sh:targetNode ex:uri3 ;"
						+ "   sh:property [ "
						+ "      sh:path ex:uri13 ; "
						+ "      sh:nodeKind sh:IRIOrLiteral ; "
						+ "   ] "
						+ "   . " +
				"ex:d1 a sh:NodeShape ; " + 
						"     sh:targetNode ex:uri3 ;"
						+ "   sh:property [ "
						+ "      sh:path ex:uri4 ; "
						+ "      sh:in (ex:uri1 ex:uri2 ) ; "
						+ "   ] "
						+ "   . " +
				"ex:s a sh:NodeShape ; " + 
				"     sh:targetClass ex:uri6 ;"
				+ "   sh:property [ "
				+ "      sh:path ex:uri10 ; "
				+ "      sh:nodeKind sh:IRI ; "
				+ "   ] "
				+ "   . " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri6 ex:uri8) . " +
				"ex:s2 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri1 ex:uri2) . " +
				"ex:uri7 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"ex:uri8 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri10 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"ex:uri9 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri10 ; "+
				"   sh:in ( \"lit-1\" \"lit-2\") . " +
				"");
		SHACL_models.add(                               
				"ex:s a sh:NodeShape ; " + 
				"     sh:targetClass ex:uri6 ;"
				+ "   sh:property [ "
				+ "      sh:path ex:uri10 ; "
				+ "      sh:nodeKind sh:IRI ; "
				+ "   ] "
				+ "   . " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri6 ex:uri8) . " +
				"ex:s2 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri1 ex:uri2) . " +
				"ex:uri7 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"ex:uri8 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri10 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"ex:uri9 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri10 ; "+
				"   sh:in ( \"lit-1\" \"lit-2\") . " +
				"");
		SHACL_models.add(                               
				"ex:s a sh:NodeShape ; " + 
				"     sh:targetClass ex:uri6 ;"
				+ "   sh:property [ "
				+ "      sh:path ex:uri10 ; "
				+ "      sh:nodeKind sh:IRI ; "
				+ "   ] "
				+ "   . " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri6 ex:uri8) . " +
				"ex:s2 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri1 ex:uri2) . " +
				"ex:uri7 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"ex:uri8 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri10 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"");
		SHACL_models.add(                               
				"ex:s a sh:NodeShape ; " + 
				"     sh:targetClass ex:uri6 ;"
				+ "   sh:property [ "
				+ "      sh:path ex:uri10 ; "
				+ "      sh:nodeKind sh:IRI ; "
				+ "   ] "
				+ "   . " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri6 ex:uri8) . " +
				"ex:s2 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri1 ex:uri2) . " +
				"ex:uri7 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"ex:uri8 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri10 ; "+
				"   sh:nodeKind sh:IRI . " +
				"");
		SHACL_models.add(                               
				"ex:s a sh:NodeShape ; " + 
				"     sh:targetClass ex:uri6 ;"
				+ "   sh:property [ "
				+ "      sh:path ex:uri10 ; "
				+ "   ] "
				+ "   . " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri6 ex:uri8) . " +
				"ex:s2 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri1 ex:uri2) . " +
				"ex:uri7 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"ex:uri8 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri10 ; "+
				"   sh:nodeKind sh:IRI . " +
				"");
		SHACL_models.add(                               
				"ex:s a sh:NodeShape ; " + 
				"     sh:targetClass ex:uri6 ;"
				+ "   sh:property [ "
				+ "      sh:path ex:uri4 ; "
				+ "   ] "
				+ "   . " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri6 ex:uri8) . " +
				"ex:s2 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri1 ex:uri2) . " +
				"ex:uri7 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"ex:uri8 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri10 ; "+
				"   sh:nodeKind sh:IRI . " +
				"");
		SHACL_models.add(                               
				"ex:s a sh:NodeShape ; " + 
				"     sh:targetClass ex:uri6 ;"
				+ "   sh:property [ "
				+ "      sh:path ex:uri4 ; "
				+ "   ] "
				+ "   . " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri6 ex:uri8) . " +
				"ex:s2 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri1 ex:uri2) . " +
				"ex:uri7 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"");
		SHACL_models.add(                               
				"ex:s a sh:NodeShape ; " + 
				"     sh:targetClass ex:uri6 ;"
				+ "   sh:property [ "
				+ "      sh:path ex:uri4 ; "
				+ "      sh:in (ex:uri6 ex:uri10 ex:uri8 ex:uri12) ; " 
				+ "   ] "
				+ "   . " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri6 ex:uri8) . " +
				"ex:s2 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri1 ex:uri2) . " +
				"ex:uri7 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"");
		SHACL_models.add(                               
				"ex:s a sh:NodeShape ; " + 
				"     sh:targetClass ex:uri6 ;"
				+ "   sh:property [ "
				+ "      sh:path ex:uri4 ; "
				+ "      sh:in (ex:uri6 ex:uri10 ex:uri8) ; " 
				+ "   ] "
				+ "   . " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri6 ex:uri8) . " +
				"ex:uri7 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"");
		SHACL_models.add(                               
				"ex:s a sh:NodeShape ; " + 
				"     sh:targetClass ex:uri6 ;"
				+ "   sh:property [ "
				+ "      sh:path ex:uri4 ; "
				+ "      sh:in (ex:uri6 ex:uri10 ex:uri8) ; " 
				+ "   ] "
				+ "   . " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri4 ; "+
				"   sh:in (ex:uri6 ex:uri10 ex:uri8) . " +
				"ex:uri4 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"");
		SHACL_models.add(                               
				"ex:s a sh:NodeShape ; " + 
				"     sh:targetClass ex:uri6 ;"
				+ "   sh:property [ "
				+ "      sh:path ex:uri4 ; "
				+ "      sh:in (ex:uri6 ex:uri10 ex:uri8) ; "   //////////////////NOT TRANSLATABLE
				+ "   ] "
				+ "   . " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri10 ex:uri8) . " +
				"ex:uri4 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"");
		SHACL_models.add(
				"ex:s a sh:NodeShape ; " + 
				"     sh:targetClass ex:uri6 ;"
				+ "   sh:property [ "
				+ "      sh:path ex:uri4 ; "
				+ "   ] "
				+ "   . " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri10 ex:uri8) . " +
				"ex:uri4 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"");
		SHACL_models.add("ex:s a sh:NodeShape . " + 
				"ex:s sh:targetClass ex:uri6 .  " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf rdf:type ; "+
				"   sh:in (ex:uri6 ex:uri10 ex:uri8) . " +
				"ex:uri4 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf rdf:type ; "+
				"   sh:in (ex:uri6 ex:uri2 ) . " +
				"");
		
		
		
		
		SHACL_models.add("ex:s a sh:NodeShape . " + 
				"ex:s sh:targetClass ex:uri6 .  " +
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7) . ");
		SHACL_models.add("ex:s a sh:NodeShape . " + 
				"ex:s sh:targetClass ex:uri6 .  " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7) . ");
		SHACL_models.add("ex:s a sh:NodeShape . " + 
				"ex:s sh:targetClass ex:uri6 .  " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . ");
		SHACL_models.add("ex:s a sh:NodeShape . " + 
				"ex:s sh:targetClass ex:uri6 .  " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri6 ex:uri8) . " +
				"");
		SHACL_models.add("ex:s a sh:NodeShape . " + 
				"ex:s sh:targetClass ex:uri6 .  " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri10 ex:uri8) . " +
				"ex:uri4 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri10 ex:uri9 ) . " +
				"");
		SHACL_models.add("ex:s a sh:NodeShape . " + 
				"ex:s sh:targetClass ex:uri6 .  " +
				"ex:uri1 a sh:NodeShape . " + 
				"ex:uri1 sh:targetClass ex:uri7 . "+
				"ex:uri2 a sh:NodeShape . " + 
				"ex:uri2 sh:targetObjectsOf rdf:type . "+
				"ex:uri2 sh:in (ex:uri6 ex:uri7 ex:uri8) . " +
				"ex:uri3 a sh:NodeShape ; " + 
				"   sh:targetObjectsOf ex:uri5 ; "+
				"   sh:in (ex:uri6 ex:uri10 ex:uri8) . " +
				"ex:uri4 a sh:NodeShape ; " + 
				"   sh:targetSubjectsOf ex:uri52 ; "+
				"   sh:in (ex:uri1 ex:uri2 ) . " +
				"");
		
		
		boolean passed = test_translation_from_SHACL(SHACL_models, 10);
		
		
		passed = test_translation_from_SHACL(SHACL_models, 1000);
		
		System.out.println("\n\nFINAL = "+passed);
	}
	
	public static boolean test_translation_from_SHACL(List<String> SHACL_models, int maxIterations) {
		boolean passed = true;
		int i = 0;
		for(String s : SHACL_models) {
			try {
				Model SHACL = Util.unprefixedTurtleToModel(testClass.getPrefixes()+s);
				Set<URI> predicates = new HashSet<URI>(); 
				Schema graphPatternSchema = Translator_to_Graph_Pattern.translate(SHACL, predicates);
				boolean current_passed = testClass.test_schema_pattern_equivalence(SHACL, graphPatternSchema, maxIterations, predicates);
				passed = passed && current_passed;
				System.out.println((i++)+" * "+current_passed);
			} catch (SHACLTranslationException ex) {
				System.out.println((i++)+" SHACL Not Translatable "+ex.getMessage());
			}
		}
		
		System.out.println("Group ("+maxIterations+") = "+passed+"\n");
		return passed;
		
	}
	
	//TODO
	public static String generate_random_SHACL_Model(Random r) {
		String result = "";
		int number_of_shapes = r.nextInt(20);
		for(int i = 0; i < number_of_shapes; i++) {
			URI shapeuri = (URI) Util.getRandomURI(r);
			if(r.nextBoolean()) {
				// create a new shape
				if(r.nextBoolean()) {
					// create a class shape
				}
				else {
					// create a node shape
				}
			} else {
				// create a new property shape
				boolean inverse = r.nextBoolean();
			}
		}
		return result;
	}

}
