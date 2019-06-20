package shacl;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import core.Element;
import core.Literal;
import core.Log;
import core.Namespace;
import core.SearchUtil;
import core.Triple_Pattern;
import core.URI;
import core.Util;
import core.Variable;

public class Translator_to_SHACL {

	public static Namespace ns = new Namespace("sh","http://www.w3.org/ns/shacl#");
	// This set contains the URI suffixes used to cross-reference shapes
	// it is used to guarantee their uniqueness
	private static Set<String> shapeCodesUsed;
	public static boolean printComments = true;
	
	
	public static Model schemaToSHACLModel(Set<Triple_Pattern> patterns) {
		String turtleSerialisation = translateToTurtleSHACL(patterns);
		Model model = ModelFactory.createDefaultModel() ;
		return model.read(new ByteArrayInputStream(turtleSerialisation.getBytes()), null, "TURTLE");
	}
	
	/**
	 * Translate a set of schema triple patterns into its corresponding SHACL representation
	 * @param patterns the schema patterns to convert
	 * @return the SHACL representation serialised as a string using the Turtle format
	 */
	public static String translateToTurtleSHACL(Set<Triple_Pattern> patterns) {
		// A) Create the prefixes for the SHACL turtle file 
		String result = Util.getTurtlePrefixes(patterns)+"\n"+Util.getTurtlePrefix(ns)+"\n";
		if(printComments) result = "### SHACL translation from triple pattern schema\n\n##Prefixes:\n\n"+result; 
		
		// Initialise the set of used shapes URIs to the empty set
		shapeCodesUsed = new HashSet<String>();
				
		// Sort the triple patterns by predicate
		Map<URI,Set<Triple_Pattern>> predicateMap =  new HashMap<URI,Set<Triple_Pattern>>();
		for(Triple_Pattern tp : patterns) {
			if(!predicateMap.containsKey(tp.getPredicate())) 
				predicateMap.put(tp.getPredicate(), new HashSet<Triple_Pattern>());
			predicateMap.get(tp.getPredicate()).add(tp);
		}
		
		
		// B) Process each predicate separately (since there is no interaction between different predicates)
		for(URI predicate : predicateMap.keySet()) {
			result += "\n"+translatePredicateSubset(predicate, predicateMap.get(predicate));
		}
		return result;
	}
	
	/**
	 * Compute the SHACL translation for a single predicate
	 * @param predicate the predicate being considered
	 * @param patterns a set of schema triple patterns, all of which having the predicate in the input as predicate
	 * @return
	 */
	private static String translatePredicateSubset(URI predicate, Set<Triple_Pattern> patterns) {
		String result = "";
		if(printComments) result += "# Shapes of predicate "+predicate+"\n";
		
		// Precompute the set of triple patterns that contains a variable in the subject
		Set<Triple_Pattern> patternsWithVariableSubj = SearchUtil.searchBySubject(new Variable(true), patterns);
		
		// A) Case of constant (URI) in the subject position
		Set<URI> subjectsURI = SearchUtil.searchURISubjects(patterns);
		for(URI uri : subjectsURI) {
			result += A_helperSHACL_URI_in_subject_position(uri, patternsWithVariableSubj, predicate, patterns);
		}
		
		// B) Case of constant (literal or URI) in the object position
		//Set<Element> objectsConstant = SearchUtil.searchConstantObjects(patterns);
						/*for(Element constant : objectsConstant) {
							result += B_helperSHACL_constant_in_object_position(constant, predicate, patterns);
						}*/
		
		// C/D) Case of variables in the subject position
		if(patternsWithVariableSubj.isEmpty()) {
			// C) Case in which there is no triple pattern with a variable in the subject position
			result += C_helperSHACL_no_variable_in_subject_position(subjectsURI, predicate, patterns);
		} else {
			// D) Case in which there is at least one triple pattern with a variable in the subject position
			
			Set<String> extraShapes = new HashSet<String>();
			
			// D1) Restrictions on the objects of triples with a variable as a subject
			result += D1_helperSHACL_restrictions_on_objects_if_variable_as_subject(extraShapes,predicate,patterns);
			
			// D2) Restrictions on the subjects of triples with a variable as object
			result += D2_helperSHACL_restrictions_on_subjects_if_variable_as_object(extraShapes,predicate,patterns);
			
			// Write down the additional shapes computed earlier 
			for(String s: extraShapes) {
				result += s;
			}
		}
		return result;
	}
	
	private static String uniquifyCode(String code) {
		while(shapeCodesUsed.contains(code)) {
			code+="A";
		};
		shapeCodesUsed.add(code);
		return code;
	}
	
	
	
	private static String A_helperSHACL_URI_in_subject_position(URI uri_in_subject, Set<Triple_Pattern> patternsWithVariableSubj, URI predicate, Set<Triple_Pattern> patterns) {
		String result = "";
		Set<Triple_Pattern> patternsWithThisURISubj = SearchUtil.searchBySubject(uri_in_subject, patterns);
		
		// collect all the objects of triples with a variable as a subject
		Set<Element> freedoms = SearchUtil.getObjects(patternsWithVariableSubj);
		freedoms.addAll(SearchUtil.getObjects(patternsWithThisURISubj));
		// add all the objects of triples with this URI as subject
		
		// compute the constraints
		List<String> freedomsEncoded = generateFreedoms(freedoms);
		
		String code = uniquifyCode(Util.getUniqueCode(uri_in_subject)+"-"+Util.getUniqueCode(predicate));
		
		result+=ns.getPrefix()+":"+uniquifyCode("shape-var-"+code)+"\n"
				+ "    a sh:NodeShape ;\n"
				+ "    sh:targetNode "+uri_in_subject.toSHACLconstraint()+" ;\n"
				+ "    sh:path "+predicate.toSHACLconstraint()+" ;\n";
		if(freedomsEncoded.size() == 1) {
			result += "    "+freedomsEncoded.get(0)+"\n    .\n";
		} else {
			result +=  "    sh:or (\n";
			for(String freedomEncoded : freedomsEncoded) {
				result += "        [ "+freedomEncoded+"]\n";
			}
			result +=  "    ) .\n";
		}
		return result;
	}
	
	private static String B_helperSHACL_constant_in_object_position(Element constant, URI predicate, Set<Triple_Pattern> patterns) {
		String result = "";
		Set<Triple_Pattern> patternsWithThisConstantObj = SearchUtil.searchByObject(constant, patterns);
		
		// collect all the subjects of those selected triples
		Set<Element> freedoms = SearchUtil.getSubjects(patternsWithThisConstantObj);
		Set<Triple_Pattern> patternsWithVariablePlusObj = SearchUtil.searchByObject(new Variable(false), patterns);
		freedoms.addAll(SearchUtil.getSubjects(patternsWithVariablePlusObj));
		if(constant instanceof URI) {
			// consider also variables- in the object position
			Set<Triple_Pattern> patternsWithVariableMinObj = SearchUtil.searchByObject(new Variable(true), patterns);
			freedoms.addAll(SearchUtil.getSubjects(patternsWithVariableMinObj));
		}
		
		// compute the constraints
		List<String> freedomsEncoded = generateFreedoms(freedoms);
		
		String code = uniquifyCode("ReverseConstant-"+Util.getUniqueCode(constant)+"-"+Util.getUniqueCode(predicate));
		
		result+=ns.getPrefix()+":"+uniquifyCode("shape-var-"+code)+"\n"
				+ "    a sh:NodeShape ;\n"
				+ "    sh:targetNode "+constant.toSHACLconstraint()+" ;\n"
				+ "    sh:path [ sh:inversePath "+predicate.toSHACLconstraint()+" ];\n";
		if(freedomsEncoded.size() == 1) {
			result += "    "+freedomsEncoded.get(0)+"\n    .\n";
		} else {
			result +=  "    sh:or (\n";
			for(String freedomEncoded : freedomsEncoded) {
				result += "        [ "+freedomEncoded+"]\n";
			}
			result +=  "    ) .\n";
		}
		
		return result;
	}
	
	private static String C_helperSHACL_no_variable_in_subject_position(Set<URI> subjectsURI, URI predicate, Set<Triple_Pattern> patterns) {
		// If there are no patterns with variables in the subject, we can constrain the subjects
		// of triples to be one of the specific URIs that appear as subjects (subjectsURI)
		String result = "";
		String code = uniquifyCode(Util.getUniqueCode(predicate));
		Set<Element> urisInSubjPosition = new HashSet<Element>();
		urisInSubjPosition.addAll(subjectsURI);
		List<String> freedomsEncoded = generateFreedoms(urisInSubjPosition);
		if(freedomsEncoded.size() != 1 ) throw new RuntimeException("ERROR, there should not be more than one freedom here.");
		result += ns.getPrefix()+":"+uniquifyCode("shape-var-"+code)+"\n"
				+ "    a sh:NodeShape ;\n"
				+ "    sh:targetSubjectsOf "+predicate.toSHACLconstraint()+" ;\n"
				+ "    "+freedomsEncoded.get(0)+"\n    .";
		return result;
	}
	
	private static String D1_helperSHACL_restrictions_on_objects_if_variable_as_subject(Set<String> extraShapes, URI predicate, Set<Triple_Pattern> patterns) {
		String result = "";
		String code = uniquifyCode(Util.getUniqueCode(predicate));
		result+=ns.getPrefix()+":"+uniquifyCode("VarSubj-"+code)+"\n"
				+ "    a sh:NodeShape ;\n"
				+ "    sh:targetObjectsOf "+predicate+" ;\n";
		
		// D1.1 collect all the objects of triples with a variable as a subject
		Set<Element> freedoms = SearchUtil.getObjects(SearchUtil.searchBySubject(new Variable(true), patterns));
		// compute the constraints
		List<String> freedomsEncoded = generateFreedoms(freedoms);
		
		// D1.2 conside the case of objects of triples with a URI as a subject
		// compute the constraints for all the patterns with URIs as subjects
		for(Triple_Pattern tp : SearchUtil.searchWithConstantSubjects(patterns)) {
			
			// D1.2.1 find all the triple patterns with a constant as subject
			
			// find all the patterns whose object is subsumed by this object.
			Set<Triple_Pattern> additionalFreedoms = SearchUtil.searchByObjectSubsumedBy(tp.getObject(), patterns);
			for(Triple_Pattern af : additionalFreedoms) {
				String codeInverseBaseAF = uniquifyCode("inverseAF-"+Util.getUniqueCode(af.getSubject())+"-"+Util.getUniqueCode(tp.getObject())+"-"+Util.getUniqueCode(predicate));
				
				// find all the 
				Set<Element> possibleSubjectsOfAF = SearchUtil.getSubjects(SearchUtil.searchByObjectSubsuming(af.getObject(), patterns));
				
				extraShapes.add(computeInverseConstraintSingleURI(codeInverseBaseAF, possibleSubjectsOfAF, predicate, patterns));
				Set<Element> singletonAdditionalFreedom = new HashSet<Element>();
				singletonAdditionalFreedom.add(af.getObject());
				String additioanlFreedom = generateFreedoms(singletonAdditionalFreedom).get(0) + " sh:node " + ns.getPrefix()+":"+codeInverseBaseAF +" ;";
				freedomsEncoded.add(additioanlFreedom);
			}
		}
		
		if(freedomsEncoded.size() == 1) {
			result += "    "+freedomsEncoded.get(0)+"\n    .\n";
		} else {
			result +=  "    sh:or (\n";
			for(String freedomEncoded : freedomsEncoded) {
				result += "        [ "+freedomEncoded+"]\n";
			}
			result +=  "    ) .\n";
		}
		return result;
	}
	
	
	private static String D2_helperSHACL_restrictions_on_subjects_if_variable_as_object(Set<String> extraShapes, URI predicate, Set<Triple_Pattern> patterns) {
		String result = "";
		String code = uniquifyCode(Util.getUniqueCode(predicate));
		///////////////////////////////////////////////////////////////
		// we now need to constrain the subject of the predicate
		List<String> subjFreedomsEncoded = new LinkedList<String>();
		for(Triple_Pattern tp : patterns) {
			String newSubjectFreedom = "";
			newSubjectFreedom += generateFreedoms(Util.toSingleton(tp.getSubject())).get(0);
			if( (!(tp.getObject() instanceof Variable)) || ((Variable)tp.getObject()).isNoLit() ) { //if tp[3] is not a var+
				// add the additional object constraints
				String codeForwardAF = uniquifyCode("forwardAF-"+Util.getUniqueCode(tp.getSubject())+"-"+Util.getUniqueCode(tp.getObject())+"-"+Util.getUniqueCode(predicate));
				extraShapes.add(computeForwardConstraintSingleURI(codeForwardAF, tp.getObject(), predicate, patterns));
				newSubjectFreedom += " sh:node " + ns.getPrefix()+":"+codeForwardAF +" ;";
			}
			subjFreedomsEncoded.add(newSubjectFreedom);
		}
		result+=ns.getPrefix()+":"+uniquifyCode("VarSubj-"+code)+"\n"
				+ "    a sh:NodeShape ;\n"
				+ "    sh:targetSubjectsOf "+predicate+" ;\n";
		if(subjFreedomsEncoded.size() == 1) {
			result += "    "+subjFreedomsEncoded.get(0)+"\n    .\n";
		} else {
			result +=  "    sh:or (\n";
			for(String freedomEncoded : subjFreedomsEncoded) {
				result += "        [ "+freedomEncoded+"]\n";
			}
			result +=  "    ) .\n";
		}
		return result;
	}
	

	
	private static String computeInverseConstraintSingleURI(String codeInverse, Set<Element> subjects, URI predicate, Set<Triple_Pattern> patterns) {
		String shapeInverse = ns.getPrefix()+":"+codeInverse+"\n"
		+ "    a sh:NodeShape ;\n"
		+ "    sh:property [\n"
		+ "          sh:path [ sh:inversePath "+predicate+" ];\n";
		
		List<String> freedoms = generateFreedoms(subjects);
		if(freedoms.size() == 1) {
			shapeInverse += "          "+freedoms.get(0)+"\n    ] .\n";
		} else {
			shapeInverse +=  "          sh:or (\n";
			for(String freedomEncoded : freedoms) {
				shapeInverse += "        [ "+freedomEncoded+"]\n";
			}
			shapeInverse +=  "          ) ] .\n";
		}
		return shapeInverse;	
	}
	private static String computeForwardConstraintSingleURI(String codeForward, Element object, URI predicate, Set<Triple_Pattern> patterns) {
		String shapeForward = ns.getPrefix()+":"+codeForward+"\n"
		+ "    a sh:NodeShape ;\n"
		+ "    sh:property [\n"
		+ "          sh:path "+predicate+";\n";
		List<String> freedoms = generateFreedomsWithHasValueSingletonSet(Util.toSingleton(object));
		if(freedoms.size() == 1) {
			shapeForward += "          "+freedoms.get(0)+"\n    ] .\n";
		} else {
			throw new RuntimeException("ERROR: the freedom of a singleton element must be singleton");
			/*shapeForward +=  "          sh:or (\n";
			for(String freedomEncoded : freedoms) {
				shapeForward += "        [ "+freedomEncoded+"]\n";
			}
			shapeForward +=  "          ) ] .\n";*/
		}
		return shapeForward;	
	}
	
	private static List<String> generateFreedoms(Set<Element> elements){
		List<String> result = new LinkedList<String>();
		if(SearchUtil.containsVar(false, elements)) {
			result.add("sh:nodeKind sh:IRIOrLiteral ;");
			// we can immediately return, as this is the most general freedom
			return result;
		}
		boolean ignoreURIfreedoms =  false;
		if(SearchUtil.containsVar(true, elements)) {
			result.add("sh:nodeKind sh:IRI ;");
			// we can ignore URI freedoms, as they are subsumed by this one
			ignoreURIfreedoms =  true;
		}
		
		String individualNodeConstraint = null;
		for(Element e : elements) {
			if(! (e instanceof URI && ignoreURIfreedoms )) {
				if(e instanceof URI || e instanceof Literal) {
					if(individualNodeConstraint == null) individualNodeConstraint = "sh:in (";
					individualNodeConstraint += e.toSHACLconstraint()+" ";
				}
			}
		}
		if(individualNodeConstraint != null) {
			individualNodeConstraint += ") ;";
			result.add(individualNodeConstraint);
		}
		return result;
	}
	
	private static List<String> generateFreedomsWithHasValueSingletonSet(Set<Element> elements){
		if(elements.size()>1) throw new RuntimeException("ERROR, this method is only intended for singleton sets");
		List<String> result = new LinkedList<String>();
		if(SearchUtil.containsVar(false, elements)) {
			result.add("sh:nodeKind sh:IRIOrLiteral ;");
			// we can immediately return, as this is the most general freedom
			return result;
		}
		boolean ignoreURIfreedoms =  false;
		if(SearchUtil.containsVar(true, elements)) {
			result.add("sh:nodeKind sh:IRI ;");
			// we can ignore URI freedoms, as they are subsumed by this one
			ignoreURIfreedoms =  true;
		}
		
		String individualNodeConstraint = null;
		for(Element e : elements) {
			if(! (e instanceof URI && ignoreURIfreedoms )) {
				if(e instanceof URI || e instanceof Literal) {
					if(individualNodeConstraint == null) individualNodeConstraint = "sh:hasValue ";
					individualNodeConstraint += e.toSHACLconstraint()+" ";
				}
			}
		}
		if(individualNodeConstraint != null) {
			individualNodeConstraint += " ;";
			result.add(individualNodeConstraint);
		}
		return result;
	}
	
	
	
}
