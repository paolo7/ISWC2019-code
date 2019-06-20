import java.io.StringWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;

import core.Element;
import core.Literal;
import core.Rule;
import core.Triple_Pattern;
import core.URI;
import core.Util;
import core.Variable_Instance;
import logic.Predicate;
import logic.PredicateInstantiation;
import logic.PredicateInstantiationImpl;
import shacl.Existential_Constraint;
import shacl.Existential_Validator;
import shacl.SHACLTranslationException;
import shacl.Schema;
import shacl.Translator_to_Graph_Pattern;

public class testExistentialConstraintsValidation {

	public static boolean simple_test() {
		
		PredicateInstantiationImpl.enable_additional_constraints = false;
		List<String> descriptions = new LinkedList<String>();
		List<String> SHACL_models = new LinkedList<String>();
		List<Set<Rule>> ruleset = new LinkedList<Set<Rule>>();
		List<Set<Existential_Constraint>> all_expected_violations = new LinkedList<Set<Existential_Constraint>>();
		//if(false)
		{
			descriptions.add(
					"The example from the paper."
					);
			SHACL_models.add(  
					"ex:s0   a                   sh:NodeShape ;\n" + 
					"      sh:targetObjectsOf  ex:observedProperty ;\n" + 
					"      sh:in               ( ex:COLevel ex:TagID ) .\n" + 
					"ex:s1   a                   sh:NodeShape ;\n" + 
					"      sh:targetClass      ex:PersonnelTag ;\n" + 
					"      sh:property         [ sh:minCount  1 ;\n" + 
					"                            sh:path      ex:carriedBy\n" + 
					"                          ] .\n" + 
					"ex:s2   a                   sh:NodeShape ;\n" + 
					"      sh:targetObjectsOf  ex:hasFeatureOfInterest ;\n" + 
					"      sh:nodeKind         sh:IRI .\n" + 
					"ex:s3   a                   sh:NodeShape ;\n" + 
					"      sh:targetObjectsOf  ex:hasResult ;\n" + 
					"      sh:nodeType         sh:IRIOrLiteral .\n" + 
					"ex:s4   a                   sh:NodeShape ;\n" + 
					"      sh:targetObjectsOf  rdf:type ;\n" + 
					"      sh:in               ( ex:Observation ex:PersonnelTag ) . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			//r1={[?v1,sn:observedProperty,:TagID],[?v1,sn:hasResult,?v2][?v1,sn:hasFeatureOfInterest,?v3],→ {[?v2,rdf:type,:PersonnelTag],[?v2,:isLocatedIn,?v3]}
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"observedProperty"), new URI(testClass.ns,"TagID"),
							new Variable_Instance(true,1), new URI(testClass.ns,"hasResult"), new Variable_Instance(true,2),
							new Variable_Instance(true,1), new URI(testClass.ns,"hasFeatureOfInterest"), new Variable_Instance(false,3),
					},
					new Variable_Instance(true,2), new URI(testClass.rdf,"type"), new URI(testClass.ns,"PersonnelTag")));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"observedProperty"), new URI(testClass.ns,"TagID"),
							new Variable_Instance(true,1), new URI(testClass.ns,"hasResult"), new Variable_Instance(true,2),
							new Variable_Instance(true,1), new URI(testClass.ns,"hasFeatureOfInterest"), new Variable_Instance(false,3),
							},
					new Variable_Instance(true,2), new URI(testClass.ns,"isLocatedIn"), new Variable_Instance(false,3)));
			//r2={[?v1,sn:observedProperty,:COLevel],[?v1,sn:hasResult,"1"],[?v1,sn:hasFeatureOfInterest,?v2],→ {[?v2,rdf:type,:OffLimitArea]}
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"observedProperty"), new URI(testClass.ns,"COLevel"),
							new Variable_Instance(true,1), new URI(testClass.ns,"hasResult"), new Literal("1"),
							new Variable_Instance(true,1), new URI(testClass.ns,"hasFeatureOfInterest"), new Variable_Instance(false,2),
					},
					new Variable_Instance(true,2), new URI(testClass.rdf,"type"), new URI(testClass.ns,"OffLimitArea")));
			//r3={[?v1,:isLocatedIn,?v2]},[?v2,rdf:type,:OffLimitArea]},→ {[?v1,:isTrespassingIn,?v2]}
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"isLocatedIn"), new Variable_Instance(false,2),
							new Variable_Instance(true,2), new URI(testClass.rdf,"type"), new URI(testClass.ns,"OffLimitArea"),
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"isTrespassingIn"), new Variable_Instance(false,2)));
			// existential violations to expect
			//[< ?v0- rdf:type <http://example.org/ns#PersonnelTag> >] ==> [< ?v0- <http://example.org/ns#carriedBy> ?v1+ >]
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.rdf,"type"), new URI(testClass.ns,"PersonnelTag")
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"carriedBy"), new Variable_Instance(false,1)
					));
			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		{
			descriptions.add(
					"running example"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:recordedAtLocation ; "+
					"   sh:property [ "
							+ "      sh:path ex:belongsTo ; "
							+ "      sh:minCount 1 ; "
							+ "   ] . "+				
					
					
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetObjectsOf sosa:observedProperty ; "+
					"   sh:in (ex:COAlert ex:PersonnelTag) . "+
					
					"ex:s2 a sh:NodeShape ; " + 
					"   sh:targetObjectsOf sosa:hasFeatureOfInterest ; "+
					"   sh:nodeType sh:IRI . "+
					"ex:s3 a sh:NodeShape ; " + 
					"   sh:targetObjectsOf sosa:hasResult ; "+
					"   sh:nodeType sh:IRIOrLiteral . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.sosa,"observedProperty"), new URI(testClass.ns,"PersonnelTag"),
							new Variable_Instance(true,1), new URI(testClass.sosa,"hasResult"), new Variable_Instance(false,3),
							new Variable_Instance(true,1), new URI(testClass.sosa,"hasFeatureOfInterest"), new Variable_Instance(false,2),
							},
					new Variable_Instance(true,3), new URI(testClass.ns,"recordedAtLocation"),  new Variable_Instance(false,2)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.sosa,"observedProperty"), new URI(testClass.ns,"MinerTag"),
							new Variable_Instance(true,1), new URI(testClass.sosa,"hasResult"), new Variable_Instance(false,3),
							},
					new Variable_Instance(true,3), new URI(testClass.rdf,"type"),  new URI(testClass.ns,"PersonnelTag")));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.sosa,"observedProperty"), new URI(testClass.ns,"COAlert"),
							new Variable_Instance(true,1), new URI(testClass.sosa,"hasFeatureOfInterest"), new Variable_Instance(false,2),
							//new Variable_Instance(true,1), new URI(testClass.sosa,"hasResult"), new Literal("1"),
							new Variable_Instance(true,4), new URI(testClass.ns,"recordedAtLocation"),  new Variable_Instance(false,2)
							},
					new Variable_Instance(true,4), new URI(testClass.rdf,"isInHazardousLocation"),  new Variable_Instance(false,2)));
					
			// existential violations to expect
			//[< ?v1- <http://example.org/ns#recordedAtLocation> ?v0- >] ==> [< ?v1- <http://example.org/ns#belongsTo> ?v2+ >]
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"recordedAtLocation"), new Variable_Instance(true,0)
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"belongsTo"), new Variable_Instance(false,2)
					));
			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		
		
		{
			descriptions.add(
					"running example"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetClass ex:PersonnelInDanger ; "+
					"   sh:property [ "
					+ "      sh:path ex:hasLocation ; "
					+ "      sh:minCount 1 ; "
					+ "   ] ; "+
					"   sh:property [ "
					+ "      sh:path ex:threatLevel ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetObjectsOf sosa:observedProperty ; "+
					"   sh:in (ex:COAlarm ex:WorkerTag) . "+
					"ex:s2 a sh:NodeShape ; " + 
					"   sh:targetObjectsOf sosa:hasFeatureOfInterest ; "+
					"   sh:nodeType sh:IRI . "+
					"ex:s3 a sh:NodeShape ; " + 
					"   sh:targetObjectsOf sosa:hasResult ; "+
					"   sh:nodeType sh:IRIOrLiteral . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.sosa,"observedProperty"), new URI(testClass.ns,"WorkerTag"),
							new Variable_Instance(true,1), new URI(testClass.sosa,"hasFeatureOfInterest"), new Variable_Instance(true,2),
							new Variable_Instance(true,1), new URI(testClass.sosa,"hasResult"), new Variable_Instance(true,3),
							},
					new Variable_Instance(true,3), new URI(testClass.ns,"hasLocation"), new Variable_Instance(true,2)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.sosa,"observedProperty"), new URI(testClass.ns,"COAlarm"),
							new Variable_Instance(true,1), new URI(testClass.sosa,"hasFeatureOfInterest"), new Variable_Instance(true,2),
							new Variable_Instance(true,1), new URI(testClass.sosa,"hasResult"), new Literal("1"),
							new Variable_Instance(true,3), new URI(testClass.ns,"hasLocation"), new Variable_Instance(true,2),
							},
					new Variable_Instance(true,3), new URI(testClass.rdf,"type"), new URI(testClass.ns,"PersonnelInDanger")));
			// existential violations to expect
			//[< ?v0- rdf:type <http://example.org/ns#PersonnelInDanger> >] ==> [< ?v0- <http://example.org/ns#threatLevel> ?v1+ >]
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.rdf,"type"), new URI(testClass.ns,"PersonnelInDanger")
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"threatLevel"), new Variable_Instance(false,1)
					));
			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}

		{
			descriptions.add(
					"endless recursion of rules? avoided by a hard limit on the max depth of inferences to consider"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri3 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri1 ; "+
					"   sh:targetSubjectsOf ex:uri2 ; "+
					"   sh:targetSubjectsOf ex:uri3 . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri1"), new Variable_Instance(true,1),
							new Variable_Instance(true,1), new URI(testClass.ns,"uri2"), new Variable_Instance(true,2),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri3"), new Variable_Instance(true,2)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri1"), new Variable_Instance(true,1),
							new Variable_Instance(true,1), new URI(testClass.ns,"uri3"), new Variable_Instance(true,2),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,2)));
			// existential violations to expect

			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		
		{
			descriptions.add(
					"example of a very specific existential triggered by a very generic rule"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetNode ex:uri5 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:node ex:s2 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri1 ; "+
					"   sh:targetSubjectsOf ex:uri2 . "+
					"ex:s2 a sh:NodeShape ; " + 
					"   sh:property [ "
					+ "      sh:path ex:uri2 ; "
					+ "      sh:hasValue ex:uri7 ; "
					//+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri2"), new Variable_Instance(true,2),
							new Variable_Instance(true,3), new URI(testClass.ns,"uri1"), new Variable_Instance(true,3),
							},
					new Variable_Instance(true,2), new URI(testClass.ns,"uri1"), new Variable_Instance(true,1)));
			// existential violations to expect
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new URI(testClass.ns,"uri5"), new URI(testClass.ns,"uri1"), new Variable_Instance(true,1)
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri2"), new URI(testClass.ns,"uri7")
					));
			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		
		{
			descriptions.add(
					"example of a very specific existential not triggered by a very generic rule"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetNode ex:uri5 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:node ex:s2 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri1 ; "+
					"   sh:targetSubjectsOf ex:uri2 . "+
					"ex:s2 a sh:NodeShape ; " + 
					"   sh:property [ "
					+ "      sh:path ex:uri2 ; "
					+ "      sh:hasValue ex:uri5 ; "
					//+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri2"), new Variable_Instance(true,2),
							new Variable_Instance(true,3), new URI(testClass.ns,"uri1"), new Variable_Instance(true,3),
							},
					new Variable_Instance(true,2), new URI(testClass.ns,"uri1"), new Variable_Instance(true,1)));
			// existential violations to expect
			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		
		{
			descriptions.add(
					"example of a very specific existential triggered by a very generic rule"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetNode ex:uri5 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:node ex:s2 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri1 ; "+
					"   sh:targetSubjectsOf ex:uri2 . "+
					"ex:s2 a sh:NodeShape ; " + 
					"   sh:property [ "
					+ "      sh:path ex:uri2 ; "
					+ "      sh:hasValue ex:uri7 ; "
					//+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri2"), new Variable_Instance(true,2),
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri1"), new Variable_Instance(true,2)));
			// existential violations to expect
			// [< <http://example.org/ns#uri5> <http://example.org/ns#uri1> ?v1- >] ==> [< ?v1- <http://example.org/ns#uri2> <http://example.org/ns#uri7> >]
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new URI(testClass.ns,"uri5"), new URI(testClass.ns,"uri1"), new Variable_Instance(true,1)
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri2"), new URI(testClass.ns,"uri7")
					));
			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		{
			descriptions.add(
					"example of a very generic existential triggered by a very specific rule"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri1 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri2 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri1 ; "+
					"   sh:targetSubjectsOf ex:uri2 . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new URI(testClass.ns,"uri5"), new URI(testClass.ns,"uri2"), new URI(testClass.ns,"uri7"),
							},
					new URI(testClass.ns,"uri7"), new URI(testClass.ns,"uri1"), new URI(testClass.ns,"uri6")));
			// existential violations to expect
			// [< ?v1- <http://example.org/ns#uri3> ?v0- >] ==> [< ?v1- <http://example.org/ns#uri1> ?v2+ >]
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri1"), new Variable_Instance(true,0)
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri2"), new Variable_Instance(false,2)
					));
			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		{
			descriptions.add(
					"example of a very generic existential not triggered by a very specific rule"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri1 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri2 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri1 ; "+
					"   sh:targetSubjectsOf ex:uri2 . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new URI(testClass.ns,"uri5"), new URI(testClass.ns,"uri2"), new URI(testClass.ns,"uri7"),
							},
					new URI(testClass.ns,"uri5"), new URI(testClass.ns,"uri1"), new URI(testClass.ns,"uri6")));
			// existential violations to expect
			// [< ?v1- <http://example.org/ns#uri3> ?v0- >] ==> [< ?v1- <http://example.org/ns#uri1> ?v2+ >]
			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		
		{
			descriptions.add(
					"there are uri3 can be created from an instance containing either uri1 or uri2. "
					+ "Only the one starting from uri2 violates the inference"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri3 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri1 ; "+
					"   sh:targetSubjectsOf ex:uri2 ; "+
					"   sh:targetSubjectsOf ex:uri3 . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri1"), new Variable_Instance(true,1),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,1)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,1),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri3"), new Variable_Instance(true,1)));
			// existential violations to expect
			// [< ?v1- <http://example.org/ns#uri3> ?v0- >] ==> [< ?v1- <http://example.org/ns#uri1> ?v2+ >]
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri3"), new Variable_Instance(true,0)
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri1"), new Variable_Instance(false,2)
					));
			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		
		{
			descriptions.add(
					"multiple rule paths, one path is ok because the antecedent of the rule satisfies the constraint, "
					+ "the other is ok because the antecedent is expanded with another existential constraint to satisfy the other constraint "
					+ "the last one is ok because the rule is not applicable"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri2 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri0 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri3 ; "+
					"   sh:targetSubjectsOf ex:uri0 . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri0"), new Variable_Instance(true,1),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,1)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri1"), new Variable_Instance(true,1),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,1)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri3"), new Variable_Instance(true,1),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,1)));
			// existential violations to expect
			// [< ?v1- <http://example.org/ns#uri1> ?v0- >] ==> [< ?v1- <http://example.org/ns#uri2> ?v2+ >]
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri2"), new Variable_Instance(true,0)
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri1"), new Variable_Instance(false,2)
					));
			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		
		{
			descriptions.add(
					"multiple rule paths, one path is ok because the antecedent of the rule satisfies the constraint, "
					+ "the other is ok because the antecedent is expanded with another existential constraint to satisfy the other constraint "
					+ "the last one is ok because the rule is not applicable"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri2 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri0 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri0 . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri0"), new Variable_Instance(true,1),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,1)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri1"), new Variable_Instance(true,1),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,1)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri3"), new Variable_Instance(true,1),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,1)));
			// existential violations to expect
			// [< ?v1- <http://example.org/ns#uri1> ?v0- >] ==> [< ?v1- <http://example.org/ns#uri2> ?v2+ >]

			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		
		
		{
			descriptions.add(
					"multiple rule paths, one path is ok because the antecedent of the rule satisfies the constraint, "
					+ "the other is ok because the antecedent is expanded with another existential constraint to satisfy the other constraint"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri2 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri0 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri0 . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri0"), new Variable_Instance(true,1),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,1)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri1"), new Variable_Instance(true,1),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,1)));
			// existential violations to expect
			// [< ?v1- <http://example.org/ns#uri1> ?v0- >] ==> [< ?v1- <http://example.org/ns#uri2> ?v2+ >]

			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		
		{
			descriptions.add(
					"multiple rules create the same predicate triple, only one generates an existential violation (second rule generates violation)"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri2 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri0 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri0 ; "+
					"   sh:targetSubjectsOf ex:uri1 . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri0"), new Variable_Instance(true,1),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,1)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri1"), new Variable_Instance(true,1),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,1)));
			// existential violations to expect
			// [< ?v1- <http://example.org/ns#uri1> ?v0- >] ==> [< ?v1- <http://example.org/ns#uri2> ?v2+ >]
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri2"), new Variable_Instance(true,0)
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri0"), new Variable_Instance(false,2)
					));
			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		{
			descriptions.add(
					"multiple rules create the same predicate triple, only one generates an existential violation (first rule generates violation)"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri2 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri0 . "+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri0"), new Variable_Instance(true,1),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,1)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.ns,"uri1"), new Variable_Instance(true,1),
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new Variable_Instance(true,1)));
			// existential violations to expect
			// [< ?v1- <http://example.org/ns#uri1> ?v0- >] ==> [< ?v1- <http://example.org/ns#uri2> ?v2+ >]
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri2"), new Variable_Instance(true,0)
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri1"), new Variable_Instance(false,2)
					));
			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		} 
		
		{
			descriptions.add(
					"complex set of rules that ultimately fails to validate constraint"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri0 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri1 . "
					+ "ex:s3 a sh:NodeShape ; " + 
					"   sh:targetObjectsOf ex:uri7 ; "+
					"   sh:targetObjectsOf ex:uri8 ; "+
					"   sh:targetObjectsOf ex:uri11 . "+
					"ex:s4 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri15 ; "+
					"   sh:in (ex:uri1 ) . " +
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri9"), new Variable_Instance(true,2),
							new Variable_Instance(true,2), new URI(testClass.ns,"uri4"), new Variable_Instance(true,3),
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri0"), new URI(testClass.ns,"uri0")));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,91), new URI(testClass.ns,"uri1"), new Variable_Instance(true,92),
							new Variable_Instance(true,92), new URI(testClass.ns,"uri2"), new Variable_Instance(true,93),
							},
					new Variable_Instance(true,91), new URI(testClass.ns,"uri4"), new Variable_Instance(true,93)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,81), new URI(testClass.ns,"uri1"), new Variable_Instance(true,89),
							new Variable_Instance(true,87), new URI(testClass.ns,"uri3"), new Variable_Instance(true,88),
							},
					new Variable_Instance(true,88), new URI(testClass.ns,"uri9"), new Variable_Instance(true,81)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,710), new URI(testClass.ns,"uri7"), new Variable_Instance(true,711),
							},
					new Variable_Instance(true,711), new URI(testClass.ns,"uri2"), new Variable_Instance(true,710)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,610), new URI(testClass.ns,"uri11"), new Variable_Instance(true,611),
							},
					new Variable_Instance(true,611), new URI(testClass.ns,"uri1"), new Variable_Instance(true,610)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,513), new URI(testClass.ns,"uri8"), new Variable_Instance(true,512),
							},
					new Variable_Instance(true,513), new URI(testClass.ns,"uri3"), new Variable_Instance(true,512)));
			// existential violations to expect
			// [< ?v1- <http://example.org/ns#uri0> ?v0- >] ==> [< ?v1- <http://example.org/ns#uri1> ?v2+ >]
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri0"), new Variable_Instance(true,0)
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri1"), new Variable_Instance(false,2)
					));
			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		
		
		{
			descriptions.add(
					"complex set of rules that ultimately validates constraint"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri0 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri1 . "
					+ "ex:s3 a sh:NodeShape ; " + 
					"   sh:targetObjectsOf ex:uri7 ; "+
					"   sh:targetObjectsOf ex:uri8 ; "+
					"   sh:targetObjectsOf ex:uri11 . "+
					"ex:s4 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri15 ; "+
					"   sh:in (ex:uri1 ) . " +
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri9"), new Variable_Instance(true,2),
							new Variable_Instance(true,1), new URI(testClass.ns,"uri4"), new Variable_Instance(true,2),
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri0"), new URI(testClass.ns,"uri0")));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri1"), new Variable_Instance(true,2),
							new Variable_Instance(true,2), new URI(testClass.ns,"uri2"), new Variable_Instance(true,3),
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri4"), new Variable_Instance(true,3)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri1"), new Variable_Instance(true,8),
							new Variable_Instance(true,7), new URI(testClass.ns,"uri2"), new Variable_Instance(true,8),
							},
					new Variable_Instance(true,8), new URI(testClass.ns,"uri9"), new Variable_Instance(true,1)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,10), new URI(testClass.ns,"uri7"), new Variable_Instance(true,11),
							},
					new Variable_Instance(true,11), new URI(testClass.ns,"uri2"), new Variable_Instance(true,10)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,10), new URI(testClass.ns,"uri11"), new Variable_Instance(true,11),
							},
					new Variable_Instance(true,11), new URI(testClass.ns,"uri1"), new Variable_Instance(true,10)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,13), new URI(testClass.ns,"uri8"), new Variable_Instance(true,12),
							},
					new Variable_Instance(true,13), new URI(testClass.ns,"uri3"), new Variable_Instance(true,12)));
			// existential violations to expect

			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		
		{
			descriptions.add(
					"complex set of rules that ultimately satisfies the existential"
					);
			SHACL_models.add(  
					"ex:s0 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri0 ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] . "+
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri1 . "
					+ "ex:s3 a sh:NodeShape ; " + 
					"   sh:targetObjectsOf ex:uri7 ; "+
					"   sh:targetObjectsOf ex:uri8 ; "+
					"   sh:targetObjectsOf ex:uri11 . "+
					"ex:s4 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri15 ; "+
					"   sh:in (ex:uri1 ) . " +
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri9"), new Variable_Instance(true,2),
							new Variable_Instance(true,1), new URI(testClass.ns,"uri4"), new Variable_Instance(true,2),
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri0"), new URI(testClass.ns,"uri0")));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri1"), new Variable_Instance(true,2),
							new Variable_Instance(true,2), new URI(testClass.ns,"uri2"), new Variable_Instance(true,3),
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri4"), new Variable_Instance(true,3)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,1), new URI(testClass.ns,"uri1"), new Variable_Instance(true,6),
							new Variable_Instance(true,7), new URI(testClass.ns,"uri2"), new Variable_Instance(true,8),
							},
					new Variable_Instance(true,1), new URI(testClass.ns,"uri9"), new Variable_Instance(true,8)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,10), new URI(testClass.ns,"uri7"), new Variable_Instance(true,11),
							},
					new Variable_Instance(true,11), new URI(testClass.ns,"uri2"), new Variable_Instance(true,10)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,10), new URI(testClass.ns,"uri11"), new Variable_Instance(true,11),
							},
					new Variable_Instance(true,11), new URI(testClass.ns,"uri1"), new Variable_Instance(true,10)));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,13), new URI(testClass.ns,"uri8"), new Variable_Instance(true,12),
							},
					new Variable_Instance(true,13), new URI(testClass.ns,"uri3"), new Variable_Instance(true,12)));
			// existential violations to expect

			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		
		
		
		{
			descriptions.add(
					"although ex:s3 guarantees that that if we have a predicate uri2, we also must have uri1, the constrain of ex:s2 on uri3 is not satisfied"
					);
			SHACL_models.add(  
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri2 ; "+
					"   sh:in (ex:uri4 ex:uri5 ) . " +
					"ex:s2 a sh:NodeShape ; " + 
					"     sh:targetClass ex:uri10 ;"
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] ;"
					+ "   sh:property [ "
					+ "      sh:path ex:uri3 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] "
					+ "   . " +
					"ex:s3 a sh:NodeShape ; " + 
					"   sh:targetObjectsOf ex:uri2 ; "+
					"   sh:nodeKind sh:IRI ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] "
					+ "   ."+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(new Element[] {new Variable_Instance(true,10), new URI(testClass.ns,"uri2"), new Variable_Instance(false,11)},
					new Variable_Instance(true,11), new URI(testClass.rdf,"type"), new URI(testClass.ns,"uri10")));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,14), new URI(testClass.rdf,"type"), new Variable_Instance(true,15),
							new Variable_Instance(true,14), new URI(testClass.ns,"uri1"), new Variable_Instance(true,16),
							},
					new Variable_Instance(true,15), new URI(testClass.ns,"uri16"), new URI(testClass.ns,"uri16")));
			// existential violations to expect
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.rdf,"type"), new URI(testClass.ns,"uri10")
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri3"), new Variable_Instance(false,1)
					));
			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		{
			descriptions.add(
					"no violation, because ex:s3 guarantees that that if we have a predicate uri2, we also must have uri1"
					);
			SHACL_models.add(  
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri2 ; "+
					"   sh:in (ex:uri4 ex:uri5 ) . " +
					"ex:s2 a sh:NodeShape ; " + 
					"     sh:targetClass ex:uri10 ;"
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] ;"
					+ "   . " +
					"ex:s3 a sh:NodeShape ; " + 
					"   sh:targetObjectsOf ex:uri2 ; "+
					"   sh:nodeKind sh:IRI ; "
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] "
					+ "   ."+
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			// rules to add
			rules.add(parse_simple_rule(new Element[] {new Variable_Instance(true,10), new URI(testClass.ns,"uri2"), new Variable_Instance(false,11)},
					new Variable_Instance(true,11), new URI(testClass.rdf,"type"), new URI(testClass.ns,"uri10")));
			rules.add(parse_simple_rule(
					new Element[] {
							new Variable_Instance(true,14), new URI(testClass.rdf,"type"), new Variable_Instance(true,15),
							new Variable_Instance(true,14), new URI(testClass.ns,"uri1"), new Variable_Instance(true,16),
							},
					new Variable_Instance(true,15), new URI(testClass.ns,"uri16"), new URI(testClass.ns,"uri16")));
			// existential violations to expect


			//
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		{
			descriptions.add(
					"violation because there is no rule that creates predicates uri1 for the infinite new possible entities of type uri10"
					);
			SHACL_models.add(  
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri2 ; "+
					"   sh:in (ex:uri4 ex:uri5 ) . " +
					"ex:s2 a sh:NodeShape ; " + 
					"     sh:targetClass ex:uri10 ;"
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] "
					+ "   . " +
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			rules.add(parse_simple_rule(
					new Element[] {new Variable_Instance(true,10), new URI(testClass.ns,"uri2"), new Variable_Instance(false,11)},
					new Variable_Instance(true,11), new URI(testClass.rdf,"type"), new URI(testClass.ns,"uri10")));
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.rdf,"type"), new URI(testClass.ns,"uri10")
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri1"), new Variable_Instance(false,1)
					));
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		{
			descriptions.add(
					"violation because there is no rule that creates predicates uri1 for the two new possible entities of type uri10 (uri4 and uri5)"
					);
			SHACL_models.add(  
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri2 ; "+
					"   sh:in (ex:uri4 ex:uri5 ) . " +
					"ex:s2 a sh:NodeShape ; " + 
					"     sh:targetClass ex:uri10 ;"
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] "
					+ "   . " +
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			rules.add(parse_simple_rule(
					new Element[] {new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new URI(testClass.ns,"uri5")},
					new Variable_Instance(true,0), new URI(testClass.rdf,"type"), new URI(testClass.ns,"uri10")));
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.rdf,"type"), new URI(testClass.ns,"uri10")
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri1"), new Variable_Instance(false,1)
					));
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		{   
			descriptions.add(
					"no violation because there are rules to deal with subjects uri4 and uri5"
					);
			SHACL_models.add(  
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri2 ; "+
					"   sh:in (ex:uri4 ex:uri5 ) . " +
					"ex:s2 a sh:NodeShape ; " + 
					"     sh:targetClass ex:uri10 ;"
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] "
					+ "   . " +
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			rules.add(parse_simple_rule(
					new Element[] {new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new URI(testClass.ns,"uri5")},
					new Variable_Instance(true,0), new URI(testClass.rdf,"type"), new URI(testClass.ns,"uri10")));
			rules.add(parse_simple_rule(
					new Element[] {new URI(testClass.ns,"uri4"), new URI(testClass.rdf,"type"), new URI(testClass.ns,"uri10")},
					new URI(testClass.ns,"uri4"), new URI(testClass.ns,"uri1"), new URI(testClass.ns,"uri4")));
			rules.add(parse_simple_rule(
					new Element[] {new URI(testClass.ns,"uri5"), new URI(testClass.rdf,"type"), new Variable_Instance(false, 4)},
					new URI(testClass.ns,"uri5"), new URI(testClass.ns,"uri1"), new Variable_Instance(false, 4)));
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		{   
			descriptions.add(
					"violation because there is no rule to deal with subject uri6"
					);
			SHACL_models.add(  
					"ex:s1 a sh:NodeShape ; " + 
					"   sh:targetSubjectsOf ex:uri2 ; "+
					"   sh:in (ex:uri4 ex:uri5 ex:uri6) . " +
					"ex:s2 a sh:NodeShape ; " + 
					"     sh:targetClass ex:uri10 ;"
					+ "   sh:property [ "
					+ "      sh:path ex:uri1 ; "
					+ "      sh:minCount 1 ; "
					+ "   ] "
					+ "   . " +
					"");
			Set<Rule> rules = new HashSet<Rule>();
			Set<Existential_Constraint> expected_violations = new HashSet<Existential_Constraint>();
			rules.add(parse_simple_rule(
					new Element[] {new Variable_Instance(true,0), new URI(testClass.ns,"uri2"), new URI(testClass.ns,"uri5")},
					new Variable_Instance(true,0), new URI(testClass.rdf,"type"), new URI(testClass.ns,"uri10")));
			rules.add(parse_simple_rule(
					new Element[] {new URI(testClass.ns,"uri4"), new URI(testClass.rdf,"type"), new URI(testClass.ns,"uri10")},
					new URI(testClass.ns,"uri4"), new URI(testClass.ns,"uri1"), new URI(testClass.ns,"uri4")));
			rules.add(parse_simple_rule(
					new Element[] {new URI(testClass.ns,"uri5"), new URI(testClass.rdf,"type"), new Variable_Instance(false, 4)},
					new URI(testClass.ns,"uri5"), new URI(testClass.ns,"uri1"), new Variable_Instance(false, 4)));
			expected_violations.add(parse_simple_existential_constraint(
					new Element[] {
							new Variable_Instance(true,0), new URI(testClass.rdf,"type"), new URI(testClass.ns,"uri10")
							},
					new Variable_Instance(true,0), new URI(testClass.ns,"uri1"), new Variable_Instance(false,1)
					));
			ruleset.add(rules);
			all_expected_violations.add(expected_violations);
		}
		
		boolean passed = true;
		int maxIterations = 3;
		for(int i = 0; i < SHACL_models.size(); i++) {
			try {
				Existential_Validator.known_predicates = new HashSet<Predicate>();
				Model SHACL = Util.unprefixedTurtleToModel(testClass.getPrefixes()+SHACL_models.get(i));
				Set<URI> predicates = new HashSet<URI>(); 
				Schema graphPatternSchema = Translator_to_Graph_Pattern.translate(SHACL, predicates);
				boolean current_passed = testClass.test_schema_pattern_equivalence(SHACL, graphPatternSchema, maxIterations, predicates);
				if(!current_passed) throw new RuntimeException("ERROR, the conversion from SHACL to triplestore schema is not correct.");
				
				
				Set<Existential_Constraint> invalid_ex_constraints = Existential_Validator.validate(graphPatternSchema, ruleset.get(i));
				
				
				boolean test_passed = invalid_ex_constraints.equals(all_expected_violations.get(i));
				if(!test_passed) passed = false;
				pretty_print(descriptions.get(i), SHACL, graphPatternSchema, ruleset.get(i), invalid_ex_constraints, all_expected_violations.get(i));
				System.out.println((i)+" * "+test_passed + "\n#######################################################################\n\n"
						);
			} catch (SHACLTranslationException ex) {
				System.out.println((i)+" SHACL Not Translatable "+ex.getMessage());
			}
		}
		
		System.out.println("Group ("+maxIterations+") = "+passed+"\n");
		return passed;
	}
	
	public static void pretty_print(String description, Model SHACL, Schema graphPatternSchema, Set<Rule> rules, Set<Existential_Constraint> invalid_ex_constraints, Set<Existential_Constraint> expected_violations ) {
		System.out.println("--");
		System.out.println("----");
		System.out.println("------");
		System.out.println("--------");
		System.out.println("------ "+description);
		System.out.println("--------");
		System.out.println("\n"+graphPatternSchema.pretty_print_string());
		StringWriter out = new StringWriter();
		SHACL.write(out,"TURTLE");
		System.out.println("Original in Turtle:");
		System.out.println(out.toString());
		System.out.println("NEW TRIPLES:");
		Set<PredicateInstantiation> s_plus = Existential_Validator.expandSchema(graphPatternSchema, rules);	
		Set<Triple_Pattern> s_plus_as_TPs = Existential_Validator.util_translate_PredicateInstantiation_2_Triple_Patterns(s_plus);
		for (Triple_Pattern r : s_plus_as_TPs) {
			System.out.println("     "+r);
		}
		System.out.println("RULES:");
		for (Rule r : rules) {
			System.out.println(" - "+r);
		}
		System.out.println("EXISTENTIAL CONSTRAINTS VIOLATIONS:");
		System.out.println(" * expected:");
		for (Existential_Constraint e : expected_violations) {
			System.out.println(" *   "+e);
		}
		System.out.println(" * found:");
		for (Existential_Constraint e : invalid_ex_constraints) {
			System.out.println(" *   "+e);
		}
		System.out.println("--------");
		System.out.println("------");
		System.out.println("----");
		System.out.println("--");
	}
	
	
	public static Rule parse_simple_rule(Element[] antecedents, Element c1, Element c2, Element c3) {
		Set<Triple_Pattern> antecedent = new HashSet<Triple_Pattern>();
		for(int i = 0; i < antecedents.length; i += 3) {
			antecedent.add(new Triple_Pattern(antecedents[i], antecedents[i+1], antecedents[i+2]));
			
		}
		Triple_Pattern consequent = new Triple_Pattern(c1,c2,c3);
		return new Rule(antecedent, consequent);
	}
	public static Existential_Constraint parse_simple_existential_constraint(Element[] antecedents, Element c1, Element c2, Element c3) {
		Set<Triple_Pattern> antecedent = new HashSet<Triple_Pattern>();
		for(int i = 0; i < antecedents.length; i += 3) {
			antecedent.add(new Triple_Pattern(antecedents[i], antecedents[i+1], antecedents[i+2]));
			
		}
		Triple_Pattern consequent = new Triple_Pattern(c1,c2,c3);
		return new Existential_Constraint(antecedent, consequent);
	}
	
	
}
