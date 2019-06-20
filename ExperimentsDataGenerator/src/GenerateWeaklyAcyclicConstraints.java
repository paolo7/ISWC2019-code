import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import chase.graph.Graph;
import datagenerator.QueryGenerator;
import datagenerator.VariablePredicateLengthWeaklyAcyclicGenerator;
import datagenerator.WeaklyAcyclicGenerator;
import gqr.Predicate;

public class GenerateWeaklyAcyclicConstraints {

	public static void main(String []args) throws CloneNotSupportedException, IOException{
		
		String outputdir = System.getProperty("user.dir")+"/chasebench/generalWA/";
		int noOfdataSets = 1;
		int numberOfconstraintsPerDataSet=100;
		int sizeOfPredicateSpace = 300;
		int constraintSize = 4;
		int arity = 5;
		int maxNoOfRepeteadRelsPerConstraint = 2;
		
		
		WeaklyAcyclicGenerator.generateChaseConstraintData(outputdir,noOfdataSets,numberOfconstraintsPerDataSet,sizeOfPredicateSpace, constraintSize, arity, maxNoOfRepeteadRelsPerConstraint);
		
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
