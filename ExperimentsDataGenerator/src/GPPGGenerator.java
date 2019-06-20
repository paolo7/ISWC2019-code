import java.io.IOException;

import datagenerator.FullRandomStatementGenerator;
import datagenerator.GPPGRuleGenerator;
import datagenerator.Statement;
import datagenerator.WeaklyAcyclicGenerator;

public class GPPGGenerator {

	public static void main(String []args) throws CloneNotSupportedException, IOException{
		
		String outputdir = System.getProperty("user.dir")+"/chasebench/GPPG/";
		int noOfdataSets = 1;
		int numberOfconstraintsPerDataSet=50;
		int sizeOfPredicateSpace = 50;
		int constraintSize = 4;
		int arity = 2;
		int maxNoOfRepeteadRelsPerConstraint = 2;
		
		
		//GPPGRuleGenerator.generateChaseConstraintData(outputdir,noOfdataSets,numberOfconstraintsPerDataSet,sizeOfPredicateSpace, constraintSize, arity, maxNoOfRepeteadRelsPerConstraint);
		
		
///This block of code reads input files and creates an ArrayList of gqr.Predicate objects, i.e., the target schema
		//ArrayList<gqr.Predicate> tSchema = new ArrayList<gqr.Predicate>();
		//File stTGDsFile = new File(System.getProperty("user.dir")+"/chasebench/llunatic/doctors/stTGDs");
		//ArrayList<String> stTGDs = GenerateQueriesOnSchema.pasrseIntoMyStrangeLookingConstraints(stTGDsFile, true);
		//File tTGDsFile = new File(System.getProperty("user.dir")+"/chasebench/llunatic/doctors/targetTGDs");
		//ArrayList<String> targetTGDs = GenerateQueriesOnSchema.pasrseIntoMyStrangeLookingConstraints(tTGDsFile, false);
		//Graph wa_graph = new Graph();
		//QueryGenerator qg = new QueryGenerator(wa_graph,stTGDs,targetTGDs);
		//ArrayList<gqr.Predicate> targetSchema = qg.getTargetSchema();

///this creates weakly acyclic constraints based on its input targetSchema
		//VariablePredicateLengthWeaklyAcyclicGenerator.generateChaseConstraintData(targetSchema,outputdir,noOfdataSets,numberOfconstraintsPerDataSet, constraintSize, maxNoOfRepeteadRelsPerConstraint);
		
	}
	
	
	
	
}