package logic;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;

public class PredicateExpansionBySPARQLquery implements PredicateExpansion{
	
	private Set<Predicate> knownPredicates;

	private Set<Rule> rules;
	
	private Map<String,String> RDFprefixes;
	
	private Model additionalVocabularies;
	
	private boolean debugPrint = false;
	private boolean debugPrintOWLconsistencyChecks = false;
	
	private Map<Rule, Set<Map<String,RDFNode>>> inconsistentRuleApplications = new HashMap<Rule, Set<Map<String,RDFNode>>>();
	
	public PredicateExpansionBySPARQLquery(Set<Predicate> knownPredicates, Set<Rule> rules) {
		this.knownPredicates = knownPredicates;
		this.rules = rules;
		this.additionalVocabularies = null;
	}
	
	public PredicateExpansionBySPARQLquery(Set<Predicate> knownPredicates, Set<Rule> rules, Model additionalVocabularies) {
		this.knownPredicates = knownPredicates;
		this.rules = rules;
		this.additionalVocabularies = additionalVocabularies;
	}
	
	public PredicateExpansionBySPARQLquery() {
		knownPredicates = new HashSet<Predicate>();
		rules = new HashSet<Rule>();
	}
	
	private int statinconsistencycheck;
	private int statinconsistencycheckfound;
	private int statinconsistencycheckreused;
	private int overallConsistencyChecks;
	private int rulesConsidered;
	private int ruleApplicationConsidered;
	
	public boolean checkOWLconsistency(Rule r, Map<String,RDFNode> bindingsMap, Model baseModel, Set<PredicateInstantiation> inferrablePredicates) {
		overallConsistencyChecks++;
		Reasoner reasoner = ReasonerRegistry.getOWLMiniReasoner();
		
		//try {
		//	baseModel.write(new FileOutputStream(new File(System.getProperty("user.dir") + "/resources/outputgraphXBASEMODEL.ttl")),"Turtle");
		//		} catch (FileNotFoundException e) {
		//			e.printStackTrace();
		//		}
		
		// TODO this is not compatible yet with owl:DatatypeProperty
		// to fix this we can just turn lambda nodes to blank nodes
		
		Model modelExpanded = RDFUtil.generateRuleInstantiationModel(r,bindingsMap,RDFprefixes, knownPredicates, inferrablePredicates).add(baseModel);
		
		reasoner = reasoner.bindSchema(modelExpanded);
		//try {
		//	modelExpanded.write(new FileOutputStream(new File(System.getProperty("user.dir") + "/resources/outputgraphExpandedRule.ttl")),"Turtle");
		//		} catch (FileNotFoundException e) {
		//			e.printStackTrace();
		//		}
		
		
		InfModel infmodel = ModelFactory.createInfModel(reasoner, modelExpanded);
		ValidityReport validity = infmodel.validate();
		if (validity.isValid()) {
			if(debugPrintOWLconsistencyChecks) System.out.print(".");
		} else {
		}
		return validity.isValid();
	}

	@Override
	public Set<PredicateInstantiation> expand(int approach, Set<PredicateInstantiation> existingPredicates) {
		return expand(approach, existingPredicates,false, null);
	}
	
	public Set<PredicateInstantiation> expand(int approach, Set<PredicateInstantiation> existingPredicates, StatRecorder sr) {
		return expand(approach, existingPredicates,false, sr);
	}
	
	/**
	 * Approach
	 * 0 for the orginal GPPG
	 * 1 for original chase
	 * 2 for GPPG2, GPPG variant with double lambdas
	 * 3 for original GPPG with smart filtering
	 * 4 for critical instance with smart filtering
	 * @param approach
	 * @param existingPredicates
	 * @param consistencyCheck
	 * @param sr
	 * @return
	 */
	public Set<PredicateInstantiation> expand(int approach, Set<PredicateInstantiation> existingPredicates, boolean consistencyCheck, StatRecorder sr) {
		if(approach == 0) return expandGPPG(existingPredicates, consistencyCheck, sr);
		else if (approach == 1) return expandCritical(existingPredicates, consistencyCheck, sr);
		else if (approach == 2) return expandGPPG2(existingPredicates, consistencyCheck, sr);
		else if (approach == 3) return expandGPPGwithFilters(existingPredicates, consistencyCheck, sr);
		else if (approach == 4) return expandCriticalWithFilters(existingPredicates, consistencyCheck, sr);
		throw new RuntimeException("ERROR, approach ID must be either 1 or 0");
	}

	public Set<PredicateInstantiation> expandCritical(Set<PredicateInstantiation> existingPredicates, boolean consistencyCheck, StatRecorder sr) {
		Set<PredicateInstantiation> newPredicates = new HashSet<PredicateInstantiation>();
		for(Rule r: rules) {
			long time1 = new Date().getTime();
			Model sandboxModel = RDFUtil.generateCriticalInstanceModel(existingPredicates,RDFprefixes,r);
			sandboxModel.getNsPrefixMap().put("e", "http://example.com/");
			sandboxModel.getNsPrefixMap().put("ex", "http://example.com/");
			String SPARQLquery = RDFUtil.getSPARQLprefixes(sandboxModel)+"\n PREFIX e:   <http://example.com/> \n PREFIX ex:   <http://example.com/> \n"+r.getAntecedentSPARQL();
			Query query = QueryFactory.create(SPARQLquery) ;
			QueryExecution qe = QueryExecutionFactory.create(query, sandboxModel);
		    ResultSet rs = qe.execSelect();
		    while (rs.hasNext())
			{
		    	QuerySolution binding = rs.nextSolution();
		    	Map<String,RDFNode> bindingsMap = new HashMap<String,RDFNode>();
		    	for(Iterator<String> i = binding.varNames(); i.hasNext();) {
		    		String var =  i.next();
		    		RDFNode value = binding.get(var);
	    			if(value.isResource() && (!value.isAnon()) && value.asResource().getURI().equals(RDFUtil.LAMBDAURI)) {	    				
	    				//value = null;
	    			}
		    		bindingsMap.put(var, value);
		    		}
		    	Set<PredicateInstantiation> inferrablePredicates = null;
		    	inferrablePredicates = r.applyRule(bindingsMap, knownPredicates, existingPredicates);
		    	newPredicates.addAll(inferrablePredicates);
			}
		    long time2 = new Date().getTime();
		    if(sr != null) {
		    	sr.avgTimeRuleApplication.add((double)time2-time1);
		    }
		}
		newPredicates.removeAll(existingPredicates);
		
		int removed = RDFUtil.filterRedundantPredicates(existingPredicates,newPredicates, false, false);
		if(debugPrint) 
			System.out.println("Filtered out "+removed+" redundant predicate instantiations.");
		
		for(PredicateInstantiation pi : newPredicates) {
			Predicate p = pi.getPredicate();
			if(PredicateUtil.containsOne(p.getName(), p.getVarnum(), knownPredicates)) {
				knownPredicates.add(p);
			}

		}
		if(newPredicates.size() == 0) return newPredicates;
		
		Set<PredicateInstantiation> newKnownPredicates = new HashSet<PredicateInstantiation>();
		newKnownPredicates.addAll(existingPredicates);
		newKnownPredicates.addAll(newPredicates);
		newPredicates.addAll(expandGPPG(newKnownPredicates,consistencyCheck,sr));
		
		return newPredicates;
	}	
	
	public Set<PredicateInstantiation> expandCriticalWithFilters(Set<PredicateInstantiation> existingPredicates, boolean consistencyCheck, StatRecorder sr) {
		Set<PredicateInstantiation> newPredicates = new HashSet<PredicateInstantiation>();
		for(Rule r: rules) {
			long time1 = new Date().getTime();
			Model sandboxModel = RDFUtil.generateCriticalInstanceModel(existingPredicates,RDFprefixes,r);
			sandboxModel.getNsPrefixMap().put("e", "http://example.com/");
			sandboxModel.getNsPrefixMap().put("ex", "http://example.com/");
			String SPARQLquery = RDFUtil.getSPARQLprefixes(sandboxModel)+"\n PREFIX e:   <http://example.com/> \n PREFIX ex:   <http://example.com/> \n"+r.getAntecedentSPARQL();
			Query query = QueryFactory.create(SPARQLquery) ;
			QueryExecution qe = QueryExecutionFactory.create(query, sandboxModel);
		    ResultSet rs = qe.execSelect();
		    while (rs.hasNext())
			{
		    	QuerySolution binding = rs.nextSolution();
		    	Set<Integer> newDeltas = filterBinding(false, binding, r, existingPredicates);
		    	
		    	if(newDeltas != null) {
		    		Map<String,RDFNode> bindingsMap = new HashMap<String,RDFNode>();
		    		for(Iterator<String> i = binding.varNames(); i.hasNext();) {
		    			String var =  i.next();
		    			RDFNode value = binding.get(var);
		    			//if(value.isResource() && (!value.isAnon()) && value.asResource().getURI().equals(RDFUtil.LAMBDAURI)) {	    				
		    				//value = null;
		    			//}
		    			bindingsMap.put(var, value);
		    		}
		    		Set<PredicateInstantiation> inferrablePredicates = r.applyRule(bindingsMap, newDeltas, knownPredicates, existingPredicates);
		    		// the next line increases performance in case large numbers of redundand bindings are found
		    		RDFUtil.filterRedundantPredicates(new HashSet<PredicateInstantiation>(),inferrablePredicates, false, false);
		    		newPredicates.addAll(inferrablePredicates);
		    	}
			}
		    long time2 = new Date().getTime();
		    if(sr != null) {
		    	sr.avgTimeRuleApplication.add((double)time2-time1);
		    }
		}
		newPredicates.removeAll(existingPredicates);
		
		int removed = RDFUtil.filterRedundantPredicates(existingPredicates,newPredicates, false, false);
		if(debugPrint) 
			System.out.println("Filtered out "+removed+" redundant predicate instantiations.");
		
		for(PredicateInstantiation pi : newPredicates) {
			Predicate p = pi.getPredicate();
			if(PredicateUtil.containsOne(p.getName(), p.getVarnum(), knownPredicates)) {
				knownPredicates.add(p);
			}

		}
		if(newPredicates.size() == 0) return newPredicates;
		
		Set<PredicateInstantiation> newKnownPredicates = new HashSet<PredicateInstantiation>();
		newKnownPredicates.addAll(existingPredicates);
		newKnownPredicates.addAll(newPredicates);
		newPredicates.addAll(expandCriticalWithFilters(newKnownPredicates,consistencyCheck,sr));
		
		return newPredicates;
	}
	
	public Set<PredicateInstantiation> expandGPPG(Set<PredicateInstantiation> existingPredicates, boolean consistencyCheck, StatRecorder sr) {
		statinconsistencycheck = 0;
		statinconsistencycheckfound = 0;
		statinconsistencycheckreused = 0;
		overallConsistencyChecks = 0;
		rulesConsidered = 0;
		ruleApplicationConsidered = 0;
		
		if(debugPrint) System.out.println("*************** Expansion Iteration");
		if(debugPrint) System.out.println("***************   Num. of known predicates "+knownPredicates.size()+"");
		if(debugPrint) System.out.println("***************   Num. of rules "+rules.size()+"\n");
		if(debugPrint) System.out.println("***************   Num. of available predicates "+existingPredicates.size()+"");
		
		Set<PredicateInstantiation> newPredicates = new HashSet<PredicateInstantiation>();
		Model basicModel = null;
		if(consistencyCheck) {
			basicModel = RDFUtil.generateBasicModel(existingPredicates,RDFprefixes);
			basicModel.add(additionalVocabularies);
		}
		
		// compute sandbox model for the Graph-Pattern evaluation over a Pattern-Constrained Graph (GPPG) 
		Model sandboxModel = RDFUtil.generateGPPGSandboxModel(existingPredicates,RDFprefixes);
		for(Rule r: rules) {
			long time1 = new Date().getTime();
		    if(sr != null) sandboxModel = RDFUtil.generateGPPGSandboxModel(existingPredicates,RDFprefixes);
			rulesConsidered++;
			// Perform GPPG
			// Compute query expansion
			String SPARQLquery = RDFUtil.getSPARQLprefixes(sandboxModel)+r.getGPPGAntecedentSPARQL();
			Set<Integer> varsNoLit = r.getNoLitVariables();
			// Evaluate query over the sandbox graph
			Query query = QueryFactory.create(SPARQLquery) ;
			QueryExecution qe = QueryExecutionFactory.create(query, sandboxModel);
		    ResultSet rs = qe.execSelect();
		    while (rs.hasNext())
			{
		    	/*if(r.getConsequent().iterator().next().getName().get(0).toString().equals("xxaaxx")) {
		    		System.out.println("xxaaxx");
		    	}*/
		    	QuerySolution binding = rs.nextSolution();
		    	// the results of a GPPG evaluation, because of the Duplicate Empty Set assumption, might contain bindings without all the required variables
		    	// these bindings can be ignored as they are semantic duplicates of other bindings that contain all the variables
		    	boolean completeResultSet = true;
		    	for(Integer var : r.getAllVariables()) 
		    		if (!binding.contains("?v"+var)) 
		    			completeResultSet = false;
		    	if(completeResultSet) {		
		    		ruleApplicationConsidered++;
		    		Map<String,RDFNode> bindingsMap = new HashMap<String,RDFNode>();
		    		// Perform delta filtering
		    		boolean validBinding = true;
		    		for(Iterator<String> i = binding.varNames(); i.hasNext();) {
		    			String var =  i.next();
		    			RDFNode value = binding.get(var);
		    			if(value.isResource() && value.isAnon()) value = null;
		    			if(value.isLiteral()) {
		    				// remove assignments from variables to literals if such variables are used in subj or pred position in the antecedent
		    				if(varsNoLit.contains(new Integer(var.replaceFirst("v", ""))))
		    					validBinding = false;
		    			}
		    			else if(value.isResource() && (!value.isAnon()) && value.asResource().getURI().equals(RDFUtil.LAMBDAURI)) {
		    				//value = null;
		    			}
		    			bindingsMap.put(var, value);
		    		}
		    		// just create an entry if it's not there, it contains no maps in the set
		    		if(!inconsistentRuleApplications.containsKey(r))
		    			inconsistentRuleApplications.put(r, new HashSet<Map<String,RDFNode>>());
		    		Set<PredicateInstantiation> inferrablePredicates = null;
		    		if(validBinding) {
		    			if(consistencyCheck) {		    				
		    				if(inconsistentRuleApplications.get(r).contains(bindingsMap)) {
		    					if(debugPrintOWLconsistencyChecks) System.out.print("@");						
		    					validBinding = false;
		    					statinconsistencycheckreused++;
		    				}
		    			}
		    			else {
		    				inferrablePredicates = r.applyRule(bindingsMap, knownPredicates, existingPredicates);
		    				if (consistencyCheck && !checkOWLconsistency(r,bindingsMap, basicModel, inferrablePredicates)) {
		    					validBinding = false;
		    					inconsistentRuleApplications.get(r).add(bindingsMap);
		    					if(debugPrintOWLconsistencyChecks) System.out.print("#");
		    					statinconsistencycheckfound++;
		    					statinconsistencycheck++;
		    				}
		    			}
		    		}
		    		if(validBinding) {	
		    			newPredicates.addAll(inferrablePredicates);
		    		}
		    	}
			}
		    long time2 = new Date().getTime();
		    if(sr != null) {
		    	sr.avgTimeRuleApplication.add((double)time2-time1);
		    }
		}
		newPredicates.removeAll(existingPredicates);
		
		int removed = RDFUtil.filterRedundantPredicates(existingPredicates,newPredicates, false, false);
		if(debugPrint) 
			System.out.println("Filtered out "+removed+" redundant predicate instantiations.");
		
		for(PredicateInstantiation pi : newPredicates) {
			Predicate p = pi.getPredicate();
			if(PredicateUtil.containsOne(p.getName(), p.getVarnum(), knownPredicates)) {
				knownPredicates.add(p);
			}

		}
		
		if(debugPrint) System.out.println("\n*************** **** Considered "+rulesConsidered+" rules, for a total of "+ruleApplicationConsidered+" combinations.");
		if(debugPrint) System.out.println("*************** **** Consistency checks made "+overallConsistencyChecks);
		if(debugPrint) System.out.println("*************** **** "+statinconsistencycheck+" OWL reasoning checks, of which "+statinconsistencycheckfound+" found an inconsistency. Previous results were reused "+statinconsistencycheckreused+" times.");
		
		
		if(newPredicates.size() == 0) return newPredicates;
		Set<PredicateInstantiation> newKnownPredicates = new HashSet<PredicateInstantiation>();
		newKnownPredicates.addAll(existingPredicates);
		newKnownPredicates.addAll(newPredicates);
		newPredicates.addAll(expandGPPG(newKnownPredicates,consistencyCheck,sr));
		
		
		return newPredicates;
	}

	private Set<ConversionTriple> getAllRewritings(ConversionTriple ct, PredicateInstantiation pi) {
		Set<ConversionTriple> rewritings = new HashSet<ConversionTriple>();
		Binding subject = ct.getSubject();
		Binding predicate = ct.getPredicate();
		Binding object = ct.getObject();
		if(subject.isVar()) subject = pi.getBindings()[subject.getVar().getVarNum()];
		if(predicate.isVar()) predicate = pi.getBindings()[predicate.getVar().getVarNum()];
		if(object.isVar()) object = pi.getBindings()[object.getVar().getVarNum()];
		Binding lambda = new BindingImpl(new ResourceURI(RDFUtil.LAMBDAURI));
		rewritings.add(new ConversionTripleImpl(subject, predicate, object));
		rewritings.add(new ConversionTripleImpl(lambda, predicate, object));
		rewritings.add(new ConversionTripleImpl(subject, lambda, object));
		rewritings.add(new ConversionTripleImpl(subject, predicate, lambda));
		rewritings.add(new ConversionTripleImpl(lambda, lambda, object));
		rewritings.add(new ConversionTripleImpl(lambda, predicate, lambda));
		rewritings.add(new ConversionTripleImpl(subject, lambda, lambda));
		rewritings.add(new ConversionTripleImpl(lambda, lambda, lambda));
		return rewritings;
	}
	
	public Set<Integer> filterBinding(boolean considerRewritings, QuerySolution mapping, Rule r, Set<PredicateInstantiation> schema){
		Set<Integer> newDeltas = new HashSet<Integer>();
		Map<Variable, Boolean> conditionMet = new HashMap<Variable, Boolean>();

		// Check 1: remove mapping if it does not contain bindings for all variables
		for(Integer v : r.getAllVariables()) 
    		if (!mapping.contains("?v"+v)) 
    			return null;
		
		// Check 2: if v goes to literal, check that it does not occur in subject or predicate position in the consequent
		// otherwise it would generate an invalid triple pattern
		for(Integer v : r.getAllVariables()) {
			RDFNode mv = mapping.get("?v"+v);
			if(mv.isLiteral()) {
				for(PredicateTemplate pt : r.getConsequent()) {
					for(ConversionTriple ct : PredicateUtil.get(pt,knownPredicates).getRDFtranslation()){
						ConversionTriple ct_consequent = ct.applyBinding(pt.getBindings());
						if(ct_consequent.getSubject().isVar() && ct_consequent.getSubject().getVar().getVarNum() == v) {
							return null;
						}
						if(ct_consequent.getPredicate().isVar() && ct_consequent.getPredicate().getVar().getVarNum() == v) {
							return null;
						}
					}
				}
			}
		}
		
		// All variables occurring in the subject and predicate position of the consequent are automatically in delta
		for(PredicateTemplate pt : r.getConsequent()) {
			for(ConversionTriple ct : PredicateUtil.get(pt,knownPredicates).getRDFtranslation()){
				ConversionTriple ct_consequent = ct.applyBinding(pt.getBindings());
				if(ct_consequent.getSubject().isVar()) newDeltas.add(ct_consequent.getSubject().getVar().getVarNum());
				if(ct_consequent.getPredicate().isVar()) newDeltas.add(ct_consequent.getPredicate().getVar().getVarNum());
			}
		}
		
		
		// Check 3: verify condition
		// take all the triples tq in A with v in position i
		for(PredicateInstantiation pi: r.getAntecedent()) {
			for(ConversionTriple ct : pi.getPredicate().getRDFtranslation()) {
				for(int i = 0; i < 3; i++) {
					Binding b = ct.applyBinding(pi.getBindings()).get(i);
					if(b.isVar() || ( b.isConstant() && b.getConstant().isLiteral())) {
						boolean oneRewritingFound = false;
						Set<ConversionTriple> queryTriples;
						if(considerRewritings) {
							queryTriples = getAllRewritings(ct,pi);
						} else {
							queryTriples = new HashSet<ConversionTriple>();
							queryTriples.add(ct.applyBinding(pi.getBindings()));
						}
						if(b.isConstant() && b.getConstant().isLiteral()) {
							boolean condition_met = true;
							try {									
								ConversionTriple mtq = RDFUtil.applyMapping(ct.applyBinding(pi.getBindings()), mapping);
								Set<ConversionTriple> matchedSchemaTriples = RDFUtil.getModellingSchemaTriples(schema, mtq);
								for(ConversionTriple ts: matchedSchemaTriples) {
									oneRewritingFound = true;
									if(ts.get(i).isConstant() && ts.get(i).getConstant().isLiteral() && ts.get(i).getConstant().equals(b.getConstant()))
										condition_met = false;
									if(ts.get(i).isVar() && 
											( !ts.get(i).getVar().isSimpleVar() && ts.get(i).getVar().areLiteralsAllowed())) {
										condition_met = false;
									}
								}
							} catch (MappingInvalidException ex) {
								// if the mapping is not valid, ignore it
							}
							// if the condition is met, then it is not be possible to match this literal to a variable that allows literals
							if(condition_met) 
								return null;
						} else {
							for(ConversionTriple tq: queryTriples) {
								Variable v = b.getVar();
								if(v.getVarNum() != b.getVar().getVarNum()) throw new RuntimeException("ERROR: the rewriting of a query triple should not contain a new variable.");
								try{									
									ConversionTriple mtq = RDFUtil.applyMapping(tq, mapping);
									Set<ConversionTriple> matchedSchemaTriples = RDFUtil.getModellingSchemaTriples(schema, mtq);
									if(!conditionMet.containsKey(v)) conditionMet.put(v, Boolean.TRUE);
									for(ConversionTriple ts: matchedSchemaTriples) {
										oneRewritingFound = true;
										if(ts.get(i).isVar() && 
												( !ts.get(i).getVar().isSimpleVar() && ts.get(i).getVar().areLiteralsAllowed())) {
											conditionMet.put(v, Boolean.FALSE);
										}
									}
								} catch (MappingInvalidException ex) {
									// if the mapping is not valid, ignore it
								}
							}
							if(!oneRewritingFound) throw new RuntimeException("ERROR, there should be at least one matching schema triples, or the mapping could not have been generated.");
							RDFNode value = mapping.get("?v"+b.getVar().getVarNum());
							if(value.isLiteral()) {
								// the variable is mapped to a literal
								
								if(!conditionMet.get(b.getVar()) && i == 2) {
									// CASE 1: condition not met, and variable occurs in the object position
									// 	nothing to do, this variable can be matched to the literal
								} else {
									// CASE 2: condition met, or variable occurring in subject or predicate position
									// 	ignore this mapping, as it can't be matched to literals
									return null;
								}
								
							} else if(value.isURIResource() && value.asResource().getURI().equals(RDFUtil.LAMBDAURI)) {
								// the variable is mapped to lambda
								
								if(!conditionMet.get(b.getVar()) && i == 2) {
									// CASE 1: condition not met, and variable occurs in the object position
									// 	nothing to do, this variable can be matched to literals
								} else {
									// CASE 2: condition met, or variable occurring in subject or predicate position
									// 	add the variable to the delta, as it can't be matched to literals
									newDeltas.add(new Integer(b.getVar().getVarNum()));
								}
							}
						}
					}
				}
			}
		}
		return newDeltas;
	}
	
/*	public Set<PredicateInstantiation> expandGPPGwithFilters(Set<PredicateInstantiation> existingPredicates, boolean consistencyCheck, StatRecorder sr) {
		return expandGPPGwithFilters(false, existingPredicates, consistencyCheck, sr);
	}
	
	
*/
	

	
	public Set<PredicateInstantiation> expandGPPGwithFilters( Set<PredicateInstantiation> existingPredicates, boolean consistencyCheck, StatRecorder sr) {
		
		statinconsistencycheck = 0;
		statinconsistencycheckfound = 0;
		statinconsistencycheckreused = 0;
		overallConsistencyChecks = 0;
		rulesConsidered = 0;
		ruleApplicationConsidered = 0;
		
		if(debugPrint) System.out.println("*************** Expansion Iteration");
		if(debugPrint) System.out.println("***************   Num. of known predicates "+knownPredicates.size()+"");
		if(debugPrint) System.out.println("***************   Num. of rules "+rules.size()+"\n");
		if(debugPrint) System.out.println("***************   Num. of available predicates "+existingPredicates.size()+"");
		
		Set<PredicateInstantiation> newPredicates = new HashSet<PredicateInstantiation>();
		Model basicModel = null;
		if(consistencyCheck) {
			basicModel = RDFUtil.generateBasicModel(existingPredicates,RDFprefixes);
			basicModel.add(additionalVocabularies);
		}
		
		// compute sandbox model for the Graph-Pattern evaluation over a Pattern-Constrained Graph (GPPG) 
		Model sandboxModel = RDFUtil.generateGPPGSandboxModel(existingPredicates,RDFprefixes);
		for(Rule r: rules) {
			long time1 = new Date().getTime();
		    if(sr != null) sandboxModel = RDFUtil.generateGPPGSandboxModel(existingPredicates,RDFprefixes);
			rulesConsidered++;
			// Perform GPPG
			// Compute query expansion
			String SPARQLquery = RDFUtil.getSPARQLprefixes(sandboxModel)+r.getGPPGAntecedentSPARQL();
			Set<Integer> varsNoLit = r.getNoLitVariables();
			// Evaluate query over the sandbox graph
			Query query = QueryFactory.create(SPARQLquery) ;
			QueryExecution qe = QueryExecutionFactory.create(query, sandboxModel);
		    ResultSet rs = qe.execSelect();
		    while (rs.hasNext())
			{
		    	QuerySolution binding = rs.nextSolution();
		    	
		    	Set<Integer> newDeltas = filterBinding(true, binding, r, existingPredicates);
		    	
		    	if(newDeltas != null) {
		    		if(sr != null) {
		    			sr.applicableRules.add(r);
		    		}
		    		
		    		Map<String,RDFNode> bindingsMap = new HashMap<String,RDFNode>();
		    		for(Iterator<String> i = binding.varNames(); i.hasNext();) {
		    			String var =  i.next();
		    			RDFNode value = binding.get(var);
		    			bindingsMap.put(var, value);
		    		}	
		    		Set<PredicateInstantiation> inferrablePredicates = r.applyRule(bindingsMap, newDeltas, knownPredicates, existingPredicates);
		    		
		    		newPredicates.addAll(inferrablePredicates);
		    	}
			}
		    long time2 = new Date().getTime();
		    if(sr != null) {
		    	sr.avgTimeRuleApplication.add((double)time2-time1);
		    }
		}
		if(sr != null) {
			sr.inferrable_triples.addAll(newPredicates);
			RDFUtil.filterRedundantPredicates(newPredicates, false, false);
		}
		
		newPredicates.removeAll(existingPredicates);
		int removed = RDFUtil.filterRedundantPredicates(existingPredicates,newPredicates, false, false);
		if(debugPrint) 
			System.out.println("Filtered out "+removed+" redundant predicate instantiations.");
		
		
		for(PredicateInstantiation pi : newPredicates) {
			Predicate p = pi.getPredicate();
			if(PredicateUtil.containsOne(p.getName(), p.getVarnum(), knownPredicates)) {
				knownPredicates.add(p);
			}

		}
		
		if(debugPrint) System.out.println("\n*************** **** Considered "+rulesConsidered+" rules, for a total of "+ruleApplicationConsidered+" combinations.");
		if(debugPrint) System.out.println("*************** **** Consistency checks made "+overallConsistencyChecks);
		if(debugPrint) System.out.println("*************** **** "+statinconsistencycheck+" OWL reasoning checks, of which "+statinconsistencycheckfound+" found an inconsistency. Previous results were reused "+statinconsistencycheckreused+" times.");
		
		
		if(newPredicates.size() == 0) return newPredicates;
		Set<PredicateInstantiation> newKnownPredicates = new HashSet<PredicateInstantiation>();
		newKnownPredicates.addAll(existingPredicates);
		newKnownPredicates.addAll(newPredicates);
		newPredicates.addAll(expandGPPGwithFilters(newKnownPredicates,consistencyCheck,sr));
		
		
		return newPredicates;
	}

		

	public Set<PredicateInstantiation> expandGPPG2(Set<PredicateInstantiation> existingPredicates, boolean consistencyCheck, StatRecorder sr) {
		statinconsistencycheck = 0;
		statinconsistencycheckfound = 0;
		statinconsistencycheckreused = 0;
		overallConsistencyChecks = 0;
		rulesConsidered = 0;
		ruleApplicationConsidered = 0;
		
		if(debugPrint) System.out.println("*************** Expansion Iteration");
		if(debugPrint) System.out.println("***************   Num. of known predicates "+knownPredicates.size()+"");
		if(debugPrint) System.out.println("***************   Num. of rules "+rules.size()+"\n");
		if(debugPrint) System.out.println("***************   Num. of available predicates "+existingPredicates.size()+"");
		
		Set<PredicateInstantiation> newPredicates = new HashSet<PredicateInstantiation>();
		Model basicModel = null;
		if(consistencyCheck) {
			basicModel = RDFUtil.generateBasicModel(existingPredicates,RDFprefixes);
			basicModel.add(additionalVocabularies);
		}
		
		// compute sandbox model for the Graph-Pattern evaluation over a Pattern-Constrained Graph (GPPG) 
		Model sandboxModel = RDFUtil.generateGPPGSandboxModel(1,existingPredicates,RDFprefixes);
		for(Rule r: rules) {
			long time1 = new Date().getTime();
		    if(sr != null) sandboxModel = RDFUtil.generateGPPGSandboxModel(1,existingPredicates,RDFprefixes);
			rulesConsidered++;
			// Perform GPPG
			// Compute query expansion
			String SPARQLquery = RDFUtil.getSPARQLprefixes(sandboxModel)+r.getGPPGAntecedentSPARQL(1);
			Set<Integer> varsNoLit = r.getNoLitVariables();
			// Evaluate query over the sandbox graph
			Query query = QueryFactory.create(SPARQLquery) ;
			QueryExecution qe = QueryExecutionFactory.create(query, sandboxModel);
		    ResultSet rs = qe.execSelect();
		    while (rs.hasNext())
			{
		    	/*if(r.getConsequent().iterator().next().getName().get(0).toString().equals("xxaaxx")) {
		    		System.out.println("xxaaxx");
		    	}*/
		    	QuerySolution binding = rs.nextSolution();
		    	// the results of a GPPG evaluation, because of the Duplicate Empty Set assumption, might contain bindings without all the required variables
		    	// these bindings can be ignored as they are semantic duplicates of other bindings that contain all the variables
		    	boolean completeResultSet = true;
		    	for(Integer var : r.getAllVariables()) 
		    		if (!binding.contains("?v"+var)) 
		    			completeResultSet = false;
		    	if(completeResultSet) {		
		    		ruleApplicationConsidered++;
		    		Map<String,RDFNode> bindingsMap = new HashMap<String,RDFNode>();
		    		// Perform delta filtering
		    		boolean validBinding = true;
		    		for(Iterator<String> i = binding.varNames(); i.hasNext();) {
		    			String var =  i.next();
		    			RDFNode value = binding.get(var);
		    			if(value.isResource() && value.isAnon()) value = null;
		    			if(value.isLiteral() || (value.isResource() && (!value.isAnon()) && value.asResource().getURI().equals(RDFUtil.LAMBDAURILit) )) {
		    				// remove assignments from variables to literals if such variables are used in subj or pred position in the antecedent
		    				if(varsNoLit.contains(new Integer(var.replaceFirst("v", ""))))
		    					validBinding = false;
		    			}
		    			//else if(value.isResource() && (!value.isAnon()) && value.asResource().getURI().equals(RDFUtil.LAMBDAURI))
		    			//	value = null;
		    			bindingsMap.put(var, value);
		    		}
		    		// just create an entry if it's not there, it contains no maps in the set
		    		if(!inconsistentRuleApplications.containsKey(r))
		    			inconsistentRuleApplications.put(r, new HashSet<Map<String,RDFNode>>());
		    		Set<PredicateInstantiation> inferrablePredicates = null;
		    		if(validBinding) {
		    			if(consistencyCheck) {		    				
		    				if(inconsistentRuleApplications.get(r).contains(bindingsMap)) {
		    					if(debugPrintOWLconsistencyChecks) System.out.print("@");						
		    					validBinding = false;
		    					statinconsistencycheckreused++;
		    				}
		    			}
		    			else {
		    				inferrablePredicates = r.applyRule(bindingsMap, knownPredicates, existingPredicates);
		    				if (consistencyCheck && !checkOWLconsistency(r,bindingsMap, basicModel, inferrablePredicates)) {
		    					validBinding = false;
		    					inconsistentRuleApplications.get(r).add(bindingsMap);
		    					if(debugPrintOWLconsistencyChecks) System.out.print("#");
		    					statinconsistencycheckfound++;
		    					statinconsistencycheck++;
		    				}
		    			}
		    		}
		    		if(validBinding) {	
		    			newPredicates.addAll(inferrablePredicates);
		    		}
		    	}
			}
		    long time2 = new Date().getTime();
		    if(sr != null) {
		    	sr.avgTimeRuleApplication.add((double)time2-time1);
		    }
		}
		newPredicates = PredicateUtil.trimConsequences(newPredicates); 
		newPredicates.removeAll(existingPredicates);
		int removed = RDFUtil.filterRedundantPredicates(existingPredicates,newPredicates, false, false);
		if(debugPrint) 
			System.out.println("Filtered out "+removed+" redundant predicate instantiations.");
		
		for(PredicateInstantiation pi : newPredicates) {
			Predicate p = pi.getPredicate();
			if(PredicateUtil.containsOne(p.getName(), p.getVarnum(), knownPredicates)) {
				knownPredicates.add(p);
			}

		}
		
		if(debugPrint) System.out.println("\n*************** **** Considered "+rulesConsidered+" rules, for a total of "+ruleApplicationConsidered+" combinations.");
		if(debugPrint) System.out.println("*************** **** Consistency checks made "+overallConsistencyChecks);
		if(debugPrint) System.out.println("*************** **** "+statinconsistencycheck+" OWL reasoning checks, of which "+statinconsistencycheckfound+" found an inconsistency. Previous results were reused "+statinconsistencycheckreused+" times.");
		
		
		if(newPredicates.size() == 0) return newPredicates;
		Set<PredicateInstantiation> newKnownPredicates = new HashSet<PredicateInstantiation>();
		newKnownPredicates.addAll(existingPredicates);
		newKnownPredicates.addAll(newPredicates);
		newPredicates.addAll(expandGPPG2(newKnownPredicates,consistencyCheck,sr));
		
		
		return newPredicates;
	}

	@Override
	public Set<Rule> getRules() {
		return rules;
	}

	@Override
	public Set<Predicate> getPredicates() {
		return knownPredicates;
	}

	@Override
	public void setRules(Set<Rule> rules) {
		this.rules = rules;
	}

	@Override
	public void setPredicates(Set<Predicate> predicates) {
		this.knownPredicates = predicates;
	}

	@Override
	public void setPrefixes(Map<String, String> map) {
		this.RDFprefixes = map;
		
	}

}
