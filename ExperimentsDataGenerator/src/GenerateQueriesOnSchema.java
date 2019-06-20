import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import chase.graph.Graph;
import datagenerator.QueryGenerator;

public class GenerateQueriesOnSchema {

	

	public static void main(String[] args) {
		
		
		File stTGDsFile = new File(System.getProperty("user.dir")+"/chasebench/llunatic/doctors/stTGDs");
		ArrayList<String> stTGDs = pasrseIntoMyStrangeLookingConstraints(stTGDsFile, true);
		File tTGDsFile = new File(System.getProperty("user.dir")+"/chasebench/llunatic/doctors/targetTGDs");
		ArrayList<String> targetTGDs = pasrseIntoMyStrangeLookingConstraints(tTGDsFile, false);
		
		int numberOfqueries = 20;
		int querysize = 4;
		int sizeOfVarSpace = 60;
		int numOfFreeVarsPerquery = 3;
		int maxNumOfRepeatedRelationsPerquery = 2;
		
		Graph wa_graph = new Graph();
		try {
			QueryGenerator qg = new QueryGenerator(wa_graph,stTGDs,targetTGDs);
		
			ArrayList<gqr.Predicate> targetSchema = qg.getTargetSchema();
		
			System.out.println("Target Schema size:" +targetSchema.size());
			
			for(int i=0; i<=numberOfqueries; i++)//create 9 queries
			{
				
				String query = qg.createQuery(targetSchema, querysize, sizeOfVarSpace, numOfFreeVarsPerquery, maxNumOfRepeatedRelationsPerquery);
				
				//String query = "q0(X27):-prescription(X27,X34,X1,X2),targethospital(X3,X21,X22,X1,X34)";
				
				while(!qg.isGoodQuery(wa_graph,query))
				{
					//System.out.println("Retracting Bad query "+query.replace("q(", "q"+i+"("));
					query= qg.createQuery(targetSchema, querysize, sizeOfVarSpace, numOfFreeVarsPerquery, maxNumOfRepeatedRelationsPerquery);
				}
				System.out.println(query.replace("q(", "q"+i+"("));
			}
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	
	}
	
	public static ArrayList<String> pasrseIntoMyStrangeLookingConstraints(File tgdsFile, boolean sourceToTarget)
	{
		
		ArrayList<String> constraints = new ArrayList<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(tgdsFile));
		    String line = br.readLine();
		    int i=0;
		    while (line != null) {
		    	String head= line.substring(0, line.indexOf("->"))+",";
		    	String body= line.substring(line.indexOf("->")+2);
		    	int sizeOfHead = head.length() - head.replace(")", "").length();
		    	String constInMyFormat = (sourceToTarget?"st":"t")+"C"+(i++)+"(DC"+sizeOfHead+") :- " + head+body;
		    	//System.out.println(constInMyFormat);
		    	constraints.add(constInMyFormat);
		    	//m299004($X1,$X2,$X7,$X8) & m155004($X2,$X3,$X9,$X10) & m118004($X3,$X4,$X11,$X12) -> m224004($X0,$X1,$X5,$X6).
		        line = br.readLine();
		    }
		    br.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		
		return constraints;
		
	}
	

}
