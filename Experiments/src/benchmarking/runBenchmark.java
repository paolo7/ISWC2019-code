package benchmarking;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.LogManager;



import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;

import logic.Predicate;
import logic.PredicateInstantiationImpl;
import logic.RDFUtil;
import logic.Rule;
import shacl.CyclicRulesException;
import shacl.LiteralEnforcingException;

public class runBenchmark {

	public static void main(String[] args) throws Exception {
		
		LogManager.getLogManager().reset();
		
		RDFUtil.disableRedundancyCheck = true;
		RDFUtil.ignoreConstraints = true;

		//// Uncomment the following line to enable debug prints into console of all the schema expansions
		// GeneratorUtil.debug_mode = false;
		
		PredicateInstantiationImpl.enable_additional_constraints = false;
		
		// Experiment (a), testing the difference in computation time between the score and critical approaches
		experiment_1_critical_and_score_scalability_comparision();

		// Experiment (b), testing the difference in scalability between the con(S,R) and con^{ex}(S,R) using the score approach
		experiment_2_different_scalability_with_existentials();
		
		if(GeneratorUtil.debug_mode == true) System.out.println("WARNING, debug mode was active, so the recorded computation time might not be accurate.");
	}
	
	/**
	 * 
	 * @param warmup whether to do a warmup run before starting
	 * @throws FileNotFoundException
	 * @throws CloneNotSupportedException
	 * @throws IOException
	 * @throws PythonExecutionException
	 */
	private static void runCriticalInstancePerformanceComparisonSchemaSize(boolean warmup) throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		
		
		if(warmup) {
			System.out.println("\n(Warmup run)\n");
			runCriticalInstancePerformanceComparisonSchemaSize(false);
			System.out.println("\n(Final run)\n");
		} 
		int atomsInAntecedent = 2;
		// what is the chance that each variable in a rule is substituted with a constant
		double constantCreationRate = 0.1;
			// when instantiating a new constant, choose one out of this many
			int constantPool = 3;
		// number of rules to consider
		int ruleNum = 4;
		// number of atoms in schema
		int schemaviewSize =  2;
		    // how many possible predicates to consider
		    int sizeOfPredicateSpace = 7;
		
		
		
		// SCORE
		List<Double> timeGPPG = new LinkedList<Double>();
		List<Double> timeCritical = new LinkedList<Double>();
		List<Double> timeGPPGeach = new LinkedList<Double>();
		List<Double> timeCriticaleach = new LinkedList<Double>();
		List<Double> xAxisComlexityGPPG = new LinkedList<Double>();
		List<Double> xAxisComlexityCritical = new LinkedList<Double>();
		
		RDFUtil.excludePredicatesFromCriticalInstanceConstants = false;
		
		// each configuration tests the algorithms on inputs of different size
		int configurations = 20;
		// for each configuration a number of repetitions is done and the average score is recorded
		int repetitions = 25;
		int stepIncrease = 2;
		
		long millisecondTimeout = 120000;
		
		boolean stopRecordingCritical = false;
		for(int j = 0; j <= configurations; j += stepIncrease) {
			List<ScoreResult> scoresGPPG = new LinkedList<ScoreResult>();
			List<ScoreResult> scoresCritical = new LinkedList<ScoreResult>();
			int newschemaviewSize = schemaviewSize +j;
			int newsizeOfPredicateSpace = (int) (((double)newschemaviewSize)*1.5); // J
			int newconstantPool = newschemaviewSize;
			int newruleNum = ruleNum;
			for(int i = 0; i < repetitions*2; i++) {
				if(i % 2 == 0) {
					if(!stopRecordingCritical) {						
						ScoreResult srCritical = GeneratorUtil.evaluatePerformanceIteration(4, newsizeOfPredicateSpace, newruleNum, i/2, newschemaviewSize, atomsInAntecedent, newconstantPool, constantCreationRate, true);
						if(srCritical.time > millisecondTimeout) {
							stopRecordingCritical = true;
							System.out.println("STOP RECORDING CRITICAL AFTER COMPUTE TIME "+srCritical.time);
						}
						scoresCritical.add(srCritical);
					}
				} else {
					ScoreResult srGPPG = GeneratorUtil.evaluatePerformanceIteration(3, newsizeOfPredicateSpace, newruleNum, i/2, newschemaviewSize, atomsInAntecedent, newconstantPool, constantCreationRate, true);				
					scoresGPPG.add(srGPPG);
				}				
			}
			if(scoresGPPG.size() == repetitions) {
				ScoreResult scoreGPPG = GeneratorUtil.avgResult(scoresGPPG);
				timeGPPG.add(scoreGPPG.time);
				timeGPPGeach.add(scoreGPPG.averageRuleApplicationTime);
				xAxisComlexityGPPG.add((double)newschemaviewSize);
				System.out.println(j+" GPPG "+scoreGPPG.time);
				System.out.println(j+"               GPPG2 "+scoreGPPG.averageRuleApplicationTime);
			}
			if(scoresCritical.size() == repetitions) {
				ScoreResult scoreCritical = GeneratorUtil.avgResult(scoresCritical);
				xAxisComlexityCritical.add((double)newschemaviewSize);
				timeCritical.add(scoreCritical.time);
				timeCriticaleach.add(scoreCritical.averageRuleApplicationTime);
				System.out.println(j+" Critical "+scoreCritical.time);
				System.out.println(j+"               Critical2 "+scoreCritical.averageRuleApplicationTime);
			}
			System.out.println("");
			
		}
		//if(warmup == false) {
			// PRINT 
		{
			Plot plt = Plot.create();
			//plt.plot().add(xAxisComlexityGPPG, timeGPPG);
			plt.plot().add(xAxisComlexityCritical, timeCritical);
			//plt.plot().add(xAxisComlexityGPPG, timeGPPGeach);
			plt.plot().add(xAxisComlexityGPPG, timeGPPG);
			plt.xlabel("Triplestore Schema Size");
			plt.ylabel("milliseconds");
			plt.title("Average (over "+repetitions+") completion time of the chase vs Triplestore Schema Size");
			plt.legend();
			plt.show();
		}
		{
			Plot plt = Plot.create();
			plt.plot().add(xAxisComlexityCritical, timeCriticaleach);
			//plt.plot().add(xAxisComlexityCritical, timeCritical);
			plt.plot().add(xAxisComlexityGPPG, timeGPPGeach);
			//plt.plot().add(xAxisComlexityCritical, timeCriticaleach);
			plt.xlabel("Triplestore Schema Size");
			plt.ylabel("milliseconds");
			plt.title("Average (over "+repetitions+") time of each schema expansion by single rule application vs Triplestore Schema Size");
			plt.legend();
			plt.show();
		}
		
		//}
		System.out.println("\nEnd Run\n");
		
	}
	
	private static void experiment_1_critical_and_score_scalability_comparision() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		System.out.println("RUNNING EXPERIMENT 1 - Comparison of SCORE and CRITICAL as schema size increases");
		// each configuration tests the algorithms on inputs of different size
		int configurations = 1000;
		// for each configuration a number of repetitions is done and the average score is recorded
		int repetitions = 10;
		int stepIncrease = 4;
		long millisecondTimeout = 10*60*1000; // 10 minutes timeout
		int atomsInAntecedent = 2;
		double constantCreationRate = 0.1;
		int ruleNum = 4;
		int initialSchemaviewSize =  5;
		runCriticalInstancePerformanceComparisonSchemaSize2(
				"Experiment 1",
				millisecondTimeout,
				5,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				true
				);
		runCriticalInstancePerformanceComparisonSchemaSize2(
				"Experiment 1",
				millisecondTimeout,
				configurations,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				false
				);
	}
	
	
	private static void experiment_different_scalability() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		System.out.println("RUNNING EXPERIMENT 2 - Comparison as schema size increases");
		// each configuration tests the algorithms on inputs of different size
		int configurations = 400;
		// for each configuration a number of repetitions is done and the average score is recorded
		int repetitions = 10;
		int stepIncrease = 50;
		long millisecondTimeout = 10*60*1000; // 5 minutes timeout
		int atomsInAntecedent = 1;
		double constantCreationRate = 0.985;
		int ruleNum = 1;
		int initialSchemaviewSize =  400;
		/*runCriticalInstancePerformanceComparisonQuerySize(
				"Experiment 2",
				millisecondTimeout,
				5,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				true
				);*/
		runCriticalInstancePerformanceComparisonQuerySize(
				"Experiment 2",
				millisecondTimeout,
				configurations,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				false
				);
	}
	
	private static void experiment_2_different_scalability_with_existentials() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		System.out.println("RUNNING EXPERIMENT 2 - Comparison of different scalability between Basic and Existential-Preserving schema consequences.");
		// each configuration tests the algorithms on inputs of different size
		int configurations = 100;
		// for each configuration a number of repetitions is done and the average score is recorded
		int repetitions = 50;
		int stepIncrease = 10;
		long millisecondTimeout = 10*60*1000; // 10 minutes timeout
		int atomsInAntecedent = 2;
		double constantCreationRate = 0.1;
		int ruleNum = 20;
		int initialSchemaviewSize =  100;
		int existentials = 0;
		runScoreAndExistentialPerformanceComparison(
				existentials,
				"Experiment 2 with existentials",
				millisecondTimeout,
				configurations,
				2,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				true
				);
		runScoreAndExistentialPerformanceComparison(
				existentials,
				"Experiment 2 with existentials",
				millisecondTimeout,
				configurations,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				false
				);
	}
	
	private static void runExperiment3() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		//Warmup
		
		String experimentName = "Comparison EXPERIMENT 3 - Comparison as schema size increases for larger atom size";
		System.out.println("RUNNING "+experimentName);
		// each configuration tests the algorithms on inputs of different size
		int configurations = 50;
		// for each configuration a number of repetitions is done and the average score is recorded
		int repetitions = 2;
		int stepIncrease = 1;
		long millisecondTimeout = 5*60*1000; // 5 minutes timeout
		int atomsInAntecedent = 3;
		double constantCreationRate = 0.1;
		int ruleNum = 10;
		int initialSchemaviewSize =  3;
		// WARMUP
		runCriticalInstancePerformanceComparisonSchemaSize2(
				experimentName,
				50,
				2,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				true
				);
		// REAL
		runCriticalInstancePerformanceComparisonSchemaSize2(
				experimentName,
				millisecondTimeout,
				configurations,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				false
				);
	}
	
	private static void runExperiment4() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		//Warmup
		
		String experimentName = "Comparison EXPERIMENT 4 - Comparison as rule size increases";
		System.out.println("RUNNING "+experimentName);
		// each configuration tests the algorithms on inputs of different size
		int configurations = 300;
		// for each configuration a number of repetitions is done and the average score is recorded
		int repetitions = 20;
		int stepIncrease = 2;
		long millisecondTimeout = 5*60*1000; // 5 minutes timeout
		int atomsInAntecedent = 1;
		double constantCreationRate = 0.1;
		int ruleNum = 1;
		int initialSchemaviewSize =  30;
		int rule_increases = 3;
		// WARMUP
		runCriticalInstancePerformanceComparisonSchemaSizeDifferentRuleNumber(
				false,
				1,
				rule_increases,
				experimentName,
				50,
				2,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				true
				);
		// REAL
		runCriticalInstancePerformanceComparisonSchemaSizeDifferentRuleNumber(
				false,
				1,
				rule_increases,
				experimentName,
				millisecondTimeout,
				configurations,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				false
				);
	}

	private static void runExperiment5() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		//Warmup
		
		String experimentName = "Comparison EXPERIMENT 5 - Comparison as rule size increases";
		System.out.println("RUNNING "+experimentName);
		// each configuration tests the algorithms on inputs of different size
		int configurations = 1000;
		// for each configuration a number of repetitions is done and the average score is recorded
		int repetitions = 20;
		int stepIncrease = 25;
		long millisecondTimeout = 5*60*1000; // 5 minutes timeout
		int atomsInAntecedent = 1;
		double constantCreationRate = 0.1;
		int ruleNum = 1;
		int initialSchemaviewSize =  30;
		int rule_increases = 3;
		// WARMUP
		runCriticalInstancePerformanceComparisonSchemaSizeDifferentRuleNumber(
				false,
				1,
				rule_increases,
				experimentName,
				50,
				2,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				true
				);
		// REAL
		runCriticalInstancePerformanceComparisonSchemaSizeDifferentRuleNumber(
				false,
				1,
				rule_increases,
				experimentName,
				millisecondTimeout,
				configurations,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				false
				);
	}

	private static void experiment_2_score_scalability_for_large_rule_sizes() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		//Warmup
		
		String experimentName = "Comparison EXPERIMENT 7 - Comparison as rule size and rule length increases";
		System.out.println("RUNNING "+experimentName);
		// each configuration tests the algorithms on inputs of different size
		int configurations = 200;
		// for each configuration a number of repetitions is done and the average score is recorded
		int repetitions = 20;
		int stepIncrease = 40;
		long millisecondTimeout = 10*60*1000; // 5 minutes timeout
		int atomsInAntecedent = 8;
				//atomsInAntecedent = 12;
		double constantCreationRate = 0.1;
		int ruleNum = 0;
		int initialSchemaviewSize = 50;
		int rule_increases = 12;
		int rule_increase_step = 2;
		int sizeOfPredicateSpace = 60;
		// WARMUP
		runCriticalInstancePerformanceComparisonSchemaSizeDifferentRuleNumber(
				false,
				1,
				1,
				experimentName,
				1,
				1,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				2,
				constantCreationRate,
				ruleNum,
				true,
				sizeOfPredicateSpace
				);
		// REAL
		runCriticalInstancePerformanceComparisonSchemaSizeDifferentRuleNumber(
				false,
				rule_increase_step,
				rule_increases,
				experimentName,
				millisecondTimeout,
				configurations,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				false,
				sizeOfPredicateSpace
				);
	}
	
	private static void runExperiment8() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		//Warmup
		
		String experimentName = "Comparison EXPERIMENT 8 - Comparison as rule size and rule length increases";
		System.out.println("RUNNING "+experimentName);
		// each configuration tests the algorithms on inputs of different size
		int configurations = 200;
		// for each configuration a number of repetitions is done and the average score is recorded
		int repetitions = 1;
		int stepIncrease = 2;
		long millisecondTimeout = 10*60*1000; // 5 minutes timeout
		int atomsInAntecedent = 13;
		double constantCreationRate = 0.1;
		int ruleNum = 0;
		int initialSchemaviewSize = 28;
		int rule_increases = 14;
		int rule_increase_step = 1;
		// WARMUP
		runCriticalInstancePerformanceComparisonSchemaSizeDifferentRuleNumber(
				false,
				1,
				1,
				experimentName,
				1,
				1,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				2,
				constantCreationRate,
				ruleNum,
				true
				);
		// REAL
		runCriticalInstancePerformanceComparisonSchemaSizeDifferentRuleNumber(
				false,
				rule_increase_step,
				rule_increases,
				experimentName,
				millisecondTimeout,
				configurations,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				false
				);
	}
	
	private static void runExperiment6() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		//Warmup
		
		String experimentName = "Comparison EXPERIMENT 6 - Comparison as rule size and rule length increases";
		System.out.println("RUNNING "+experimentName);
		// each configuration tests the algorithms on inputs of different size
		int configurations = 1000;
		// for each configuration a number of repetitions is done and the average score is recorded
		int repetitions = 20;
		int stepIncrease = 50;
		long millisecondTimeout = 5*60*1000; // 5 minutes timeout
		int atomsInAntecedent = 1;
		double constantCreationRate = 0.1;
		int ruleNum = 1;
		int initialSchemaviewSize =  30;
		int rule_increases = 25;
		int rule_increase_step = 1;
		// WARMUP
		runCriticalInstancePerformanceComparisonSchemaSizeDifferentRuleNumber(
				false,
				rule_increase_step,
				rule_increases,
				experimentName,
				50,
				2,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				true
				);
		// REAL
		runCriticalInstancePerformanceComparisonSchemaSizeDifferentRuleNumber(
				true,
				rule_increase_step,
				rule_increases,
				experimentName,
				millisecondTimeout,
				configurations,
				repetitions,
				stepIncrease,
				initialSchemaviewSize,
				atomsInAntecedent,
				constantCreationRate,
				ruleNum,
				false
				);
	}
	

	private static void runCriticalInstancePerformanceComparisonSchemaSize2(String experimentName, long millisecondTimeout, int configurations, int repetitions, int stepIncrease, int initialSchemaviewSize, int atomsInAntecedent, double constantCreationRate, int ruleNum, boolean warmup) throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		
		
		if(warmup) {
			System.out.println("\n(Warmup run "+experimentName+")\n");
		} else {
			System.out.println("\n(Nromal run "+experimentName+")\n");
		}
		//int atomsInAntecedent = 2;
		// what is the chance that each variable in a rule is substituted with a constant
		
					// when instantiating a new constant, choose one out of this many
					//int constantPool = 3;
		// number of rules to consider
		//int ruleNum = 4;
		// number of atoms in schema
		//int initialSchemaviewSize =  2;
		    		// how many possible predicates to consider
		    		//int sizeOfPredicateSpace = 7;
		
		// SCORE
		List<Double> timeGPPG = new LinkedList<Double>();
		List<Double> timeCritical = new LinkedList<Double>();
		//List<Double> timeGPPGeach = new LinkedList<Double>();
		//List<Double> timeCriticaleach = new LinkedList<Double>();
		List<Double> xAxisComlexityGPPG = new LinkedList<Double>();
		List<Double> xAxisComlexityCritical = new LinkedList<Double>();
		
		RDFUtil.excludePredicatesFromCriticalInstanceConstants = false;
		
		// each configuration tests the algorithms on inputs of different size
		//int configurations = 1000;
		// for each configuration a number of repetitions is done and the average score is recorded
		//int repetitions = 20;
		//int stepIncrease = 2;
		
		//long millisecondTimeout = 5*60*1000; // 5 minutes timeout
		
		String labelScore = "r'\\texttt{score}'";
		String labelCritical = "r'\\texttt{critical}'";
		String ylabel = "Seconds";
		String xlabel = "Schema Size";
		
		boolean stopRecordingCritical = false;
		boolean stopRecordingSCORE = false;
		for(int j = 0; j <= configurations; j += stepIncrease) {
			List<ScoreResult> scoresGPPG = new LinkedList<ScoreResult>();
			List<ScoreResult> scoresCritical = new LinkedList<ScoreResult>();
			int newschemaviewSize = initialSchemaviewSize +j;
			int newsizeOfPredicateSpace = (int) (((double)newschemaviewSize)*1.5); // J
			int newconstantPool = newschemaviewSize;
			int newruleNum = ruleNum;
			// do one of each, so not to cluster all the SCORE and all the critical together
			for(int i = 0; i < repetitions*2; i++) {
				if(i % 2 == 0) {
					
					// CRITICAL
					if(!stopRecordingCritical) {						
						ScoreResult srCritical = GeneratorUtil.evaluatePerformanceIteration(4, newsizeOfPredicateSpace, newruleNum, i/2, newschemaviewSize, atomsInAntecedent, newconstantPool, constantCreationRate, false);
						scoresCritical.add(srCritical);
						if(GeneratorUtil.avgResult(scoresCritical).time*(scoresCritical.size()) > millisecondTimeout*repetitions) {
							stopRecordingCritical = true;
							System.out.println("EARLY TERMINATION! STOP RECORDING CRITICAL AFTER "+scoresCritical.size()+" COMPUTE TIME "+GeneratorUtil.avgResult(scoresCritical).time+" > "+repetitions+" times "+millisecondTimeout);
							System.out.println("Last time recorded: "+srCritical.time);
						}
					}
				} else {
					// SCORE
					if(!stopRecordingSCORE) {						
						ScoreResult srSCORE = GeneratorUtil.evaluatePerformanceIteration(3, newsizeOfPredicateSpace, newruleNum, i/2, newschemaviewSize, atomsInAntecedent, newconstantPool, constantCreationRate, false);				
						scoresGPPG.add(srSCORE);
					}
				}				
			}
			// Average results
			ScoreResult scoreGPPG = GeneratorUtil.avgResult(scoresGPPG);
			ScoreResult scoreCritical = GeneratorUtil.avgResult(scoresCritical);
			// terminate if average too high:
			if(scoreCritical.time > millisecondTimeout) {
				stopRecordingCritical = true;
				System.out.println("STOP RECORDING CRITICAL AFTER COMPUTE TIME "+scoreCritical.time);
			} 
			if(scoreGPPG.time > millisecondTimeout) {
				stopRecordingSCORE = true;
				System.out.println("STOP RECORDING SCORE AFTER COMPUTE TIME "+scoreGPPG.time);
			}
			System.out.print("\n["+newschemaviewSize+"] ");
			if(!stopRecordingCritical) {		
				xAxisComlexityCritical.add((double)newschemaviewSize);
				timeCritical.add(scoreCritical.time/1000);
				//timeCriticaleach.add(scoreCritical.averageRuleApplicationTime);
				System.out.print(" Critical: "+scoreCritical.time);
				//System.out.println(j+"               Critical2 "+scoreCritical.averageRuleApplicationTime);				
			}
			if(!stopRecordingSCORE) {		
				//timeGPPGeach.add(scoreGPPG.averageRuleApplicationTime);
				xAxisComlexityGPPG.add((double)newschemaviewSize);
				timeGPPG.add(scoreGPPG.time/1000);
				System.out.print(" SCORE: "+scoreGPPG.time);
				//System.out.println(j+"               GPPG2 "+scoreGPPG.averageRuleApplicationTime);
			}
			String pythonDataSCORE = xAxisComlexityGPPG+","+timeGPPG;
			String pythonDataCRITICAL = xAxisComlexityCritical+","+timeCritical;
			System.out.println("\n plt.plot("+pythonDataSCORE+", linestyle='-', marker=',', color='C0', label="+labelScore+")\n");
			System.out.println(" plt.plot("+pythonDataCRITICAL+", linestyle='-', marker='^', color='C1', label="+labelCritical+")\n");
			System.out.print("\n");
		}
		
		
		
		if(!warmup) {
			String pythonDataSCORE = xAxisComlexityGPPG+","+timeGPPG;
			String pythonDataCRITICAL = xAxisComlexityCritical+","+timeCritical;
			String pythonScript = "from mpl_toolkits.mplot3d import Axes3D\n" + 
					"from matplotlib.pyplot import figure\n" + 
					"from matplotlib import rcParams\n" + 
					"figure(num=None, figsize=(6.4, 2.7), dpi=300, facecolor='w', edgecolor='k')\n" + 
					"import matplotlib.pyplot as plt\n"+
					"import numpy as np\n" + 
					"rc('font', **{'family':'serif', 'serif':['Computer Modern Roman'], 'monospace': ['Computer Modern Typewriter'], 'size': ['larger']})\n" + 
					"plt.rc('text', usetex=True)\n" + 
					"params = {'axes.labelsize': 13,'axes.titlesize':13, 'legend.fontsize': 13, 'xtick.labelsize': 13, 'ytick.labelsize': 13 }\n" + 
					"rcParams.update(params)\n" + 
					"#### EXPERIMENT: "+experimentName+"\n"+
					"#### Date: "+new Date().toString()+"\n"+
					"# MAIN PARAMETERS:\n"+
					"# atomsInAntecedent (n_A) = "+atomsInAntecedent+"\n"+
					"# constantCreationRate (\\pi_C) = "+constantCreationRate+"\n"+
					"# ruleNum (R) = "+ruleNum+"\n"+
					"# initialSchemaviewSize (S)= "+initialSchemaviewSize+"\n"+
					"# constant pools (U and L)= S\n"+
					"# predicate space (P)= 1.5*S\n"+
					"# EXPERIMENT LENGTH:\n"+
					"# configurations added to S from 0 to "+configurations+" tested with steps increases of "+stepIncrease+"\n"+
					"# average time cutoff "+millisecondTimeout+" milliseconds\n"+
					"# number of different datasets per configuration: "+repetitions+"\n\n"+
					"plt.plot("+pythonDataSCORE+", linestyle='-', marker='o', color='C0', label="+labelScore+")\n" + 
					"plt.plot("+pythonDataCRITICAL+", linestyle='-', marker='^', color='C1', label="+labelCritical+")\n" + 
					"plt.ylabel('"+ylabel+"')\n"+
					"plt.xlabel('"+xlabel+"')\n"+
					"plt.legend()\n"+
					"plt.tight_layout()\n"+
					"plt.show()\n\n";
			System.out.println(pythonScript);
			
			try {
				Path p = Paths.get(System.getProperty("user.dir")+"/resultOutputs.txt");
				Files.createDirectories(p.getParent());
				if (!Files.exists(p, LinkOption.NOFOLLOW_LINKS))
				    Files.createFile(p);
				//Files.createFile(p);
			    Files.write(p, pythonScript.getBytes(), StandardOpenOption.APPEND);
			}catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
			
		} else {
			System.out.println("End Warmup run\n\n");
		}
	
		System.out.println("\nEnd Run\n");

	}
	
	

	private static void runCriticalInstancePerformanceComparisonQuerySize(String experimentName, long millisecondTimeout, int configurations, int repetitions, int stepIncrease, int initialSchemaviewSize, int atomsInAntecedent, double constantCreationRate, int ruleNum, boolean warmup) throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		
		
		if(warmup) {
			System.out.println("\n(Warmup run "+experimentName+")\n");
		} else {
			System.out.println("\n(Nromal run "+experimentName+")\n");
		}
		//int atomsInAntecedent = 2;
		// what is the chance that each variable in a rule is substituted with a constant
		
					// when instantiating a new constant, choose one out of this many
					//int constantPool = 3;
		// number of rules to consider
		//int ruleNum = 4;
		// number of atoms in schema
		//int initialSchemaviewSize =  2;
		    		// how many possible predicates to consider
		    		//int sizeOfPredicateSpace = 7;
		
		// SCORE
		List<Double> timeGPPG = new LinkedList<Double>();
		List<Double> timeCritical = new LinkedList<Double>();
		//List<Double> timeGPPGeach = new LinkedList<Double>();
		//List<Double> timeCriticaleach = new LinkedList<Double>();
		List<Double> xAxisComlexityGPPG = new LinkedList<Double>();
		List<Double> xAxisComlexityCritical = new LinkedList<Double>();
		
		RDFUtil.excludePredicatesFromCriticalInstanceConstants = false;
		
		// each configuration tests the algorithms on inputs of different size
		//int configurations = 1000;
		// for each configuration a number of repetitions is done and the average score is recorded
		//int repetitions = 20;
		//int stepIncrease = 2;
		
		//long millisecondTimeout = 5*60*1000; // 5 minutes timeout
		
		String labelScore = "r'\\texttt{score}'";
		String labelCritical = "r'\\texttt{critical}'";
		String ylabel = "Seconds";
		String xlabel = "Atoms in Antecedent";
		
		boolean stopRecordingCritical = false;
		boolean stopRecordingSCORE = false;
		for(int j = 0; j <= configurations; j += stepIncrease) {
			List<ScoreResult> scoresGPPG = new LinkedList<ScoreResult>();
			List<ScoreResult> scoresCritical = new LinkedList<ScoreResult>();
			int newschemaviewSize = initialSchemaviewSize;
			int newsizeOfPredicateSpace = (int) (((double)newschemaviewSize)*1.1); // J
			int newconstantPool = newschemaviewSize;
			int newruleNum = ruleNum;
			int newAtomsInAntecedent =  atomsInAntecedent + j;
			// do one of each, so not to cluster all the SCORE and all the critical together
			for(int i = 0; i < repetitions*2; i++) {
				if(i % 2 == 0) {
					
					// CRITICAL
					if(!stopRecordingCritical) {						
						ScoreResult srCritical = GeneratorUtil.evaluatePerformanceIteration(4, newsizeOfPredicateSpace, newruleNum, i/2, newschemaviewSize, newAtomsInAntecedent, newconstantPool, constantCreationRate, true);
						scoresCritical.add(srCritical);
						if(GeneratorUtil.avgResult(scoresCritical).time*(scoresCritical.size()) > millisecondTimeout*repetitions) {
							stopRecordingCritical = true;
							System.out.println("EARLY TERMINATION! STOP RECORDING CRITICAL AFTER "+scoresCritical.size()+" COMPUTE TIME "+GeneratorUtil.avgResult(scoresCritical).time+" > "+repetitions+" times "+millisecondTimeout);
							System.out.println("Last time recorded: "+srCritical.time);
						}
					}
				} else {
					// SCORE
					if(!stopRecordingSCORE) {						
						ScoreResult srSCORE = GeneratorUtil.evaluatePerformanceIteration(3, newsizeOfPredicateSpace, newruleNum, i/2, newschemaviewSize, newAtomsInAntecedent, newconstantPool, constantCreationRate, true);				
						scoresGPPG.add(srSCORE);
					}
				}				
			}
			// Average results
			ScoreResult scoreGPPG = GeneratorUtil.avgResult(scoresGPPG);
			ScoreResult scoreCritical = GeneratorUtil.avgResult(scoresCritical);
			// terminate if average too high:
			if(scoreCritical.time > millisecondTimeout) {
				stopRecordingCritical = true;
				System.out.println("STOP RECORDING CRITICAL AFTER COMPUTE TIME "+scoreCritical.time);
			} 
			if(scoreGPPG.time > millisecondTimeout) {
				stopRecordingSCORE = true;
				System.out.println("STOP RECORDING SCORE AFTER COMPUTE TIME "+scoreGPPG.time);
			}
			System.out.print("\n["+j+"] ");
			if(!stopRecordingCritical) {		
				xAxisComlexityCritical.add((double)newAtomsInAntecedent);
				timeCritical.add(scoreCritical.time/1000);
				//timeCriticaleach.add(scoreCritical.averageRuleApplicationTime);
				System.out.print(" Critical: "+scoreCritical.time);
				//System.out.println(j+"               Critical2 "+scoreCritical.averageRuleApplicationTime);				
			}
			if(!stopRecordingSCORE) {		
				//timeGPPGeach.add(scoreGPPG.averageRuleApplicationTime);
				xAxisComlexityGPPG.add((double)newAtomsInAntecedent);
				timeGPPG.add(scoreGPPG.time/1000);
				System.out.print(" SCORE: "+scoreGPPG.time);
				//System.out.println(j+"               GPPG2 "+scoreGPPG.averageRuleApplicationTime);
			}
			String pythonDataSCORE = xAxisComlexityGPPG+","+timeGPPG;
			String pythonDataCRITICAL = xAxisComlexityCritical+","+timeCritical;
			System.out.println("\n plt.plot("+pythonDataSCORE+", linestyle='-', marker=',', color='C0', label="+labelScore+")\n");
			System.out.println(" plt.plot("+pythonDataCRITICAL+", linestyle='-', marker='^', color='C1', label="+labelCritical+")\n");
			System.out.print("\n");
		}
		
		
		
		if(!warmup) {
			String pythonDataSCORE = xAxisComlexityGPPG+","+timeGPPG;
			String pythonDataCRITICAL = xAxisComlexityCritical+","+timeCritical;
			String pythonScript = "from mpl_toolkits.mplot3d import Axes3D\n" + 
					"from matplotlib.pyplot import figure\n" + 
					"from matplotlib import rcParams\n" + 
					"figure(num=None, figsize=(6.4, 2.7), dpi=300, facecolor='w', edgecolor='k')\n" + 
					"import matplotlib.pyplot as plt\n"+
					"import numpy as np\n" + 
					"rc('font', **{'family':'serif', 'serif':['Computer Modern Roman'], 'monospace': ['Computer Modern Typewriter'], 'size': ['larger']})\n" + 
					"plt.rc('text', usetex=True)\n" + 
					"params = {'axes.labelsize': 13,'axes.titlesize':13, 'legend.fontsize': 13, 'xtick.labelsize': 13, 'ytick.labelsize': 13 }\n" + 
					"rcParams.update(params)\n" + 
					"#### EXPERIMENT: "+experimentName+"\n"+
					"#### Date: "+new Date().toString()+"\n"+
					"# MAIN PARAMETERS:\n"+
					"# atomsInAntecedent (n_A) = "+atomsInAntecedent+"\n"+
					"# constantCreationRate (\\pi_C) = "+constantCreationRate+"\n"+
					"# ruleNum (R) = "+ruleNum+"\n"+
					"# initialSchemaviewSize (S)= "+initialSchemaviewSize+"\n"+
					"# constant pools (U and L)= S\n"+
					"# predicate space (P)= 1.5*S\n"+
					"# EXPERIMENT LENGTH:\n"+
					"# configurations added to S from 0 to "+configurations+" tested with steps increases of "+stepIncrease+"\n"+
					"# average time cutoff "+millisecondTimeout+" milliseconds\n"+
					"# number of different datasets per configuration: "+repetitions+"\n\n"+
					"plt.plot("+pythonDataSCORE+", linestyle='-', marker='o', color='C0', label="+labelScore+")\n" + 
					"plt.plot("+pythonDataCRITICAL+", linestyle='-', marker='^', color='C1', label="+labelCritical+")\n" + 
					"plt.ylabel('"+ylabel+"')\n"+
					"plt.xlabel('"+xlabel+"')\n"+
					"plt.legend()\n"+
					"plt.tight_layout()\n"+
					"plt.show()\n\n";
			System.out.println(pythonScript);
			
			try {
				Path p = Paths.get(System.getProperty("user.dir")+"/resultOutputs.txt");
				Files.createDirectories(p.getParent());
				if (!Files.exists(p, LinkOption.NOFOLLOW_LINKS))
				    Files.createFile(p);
				//Files.createFile(p);
			    Files.write(p, pythonScript.getBytes(), StandardOpenOption.APPEND);
			}catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
			
		} else {
			System.out.println("End Warmup run\n\n");
		}
	
		System.out.println("\nEnd Run\n");

	}
	
	
	
	private static void runScoreAndExistentialPerformanceComparison(int existentials, String experimentName, long millisecondTimeout, int configurations, int repetitions, int stepIncrease, int initialSchemaviewSize, int atomsInAntecedent, double constantCreationRate, int ruleNum, boolean warmup) throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		
		
		if(warmup) {
			System.out.println("\n(Warmup run "+experimentName+")\n");
		} else {
			System.out.println("\n(Normal run "+experimentName+")\n");
		}
		//int atomsInAntecedent = 2;
		// what is the chance that each variable in a rule is substituted with a constant
		
					// when instantiating a new constant, choose one out of this many
					//int constantPool = 3;
		// number of rules to consider
		//int ruleNum = 4;
		// number of atoms in schema
		//int initialSchemaviewSize =  2;
		    		// how many possible predicates to consider
		    		//int sizeOfPredicateSpace = 7;
		
		// SCORE
		List<Double> timeSCORE = new LinkedList<Double>();
		List<Double> timeExistential = new LinkedList<Double>();
		//List<Double> timeGPPGeach = new LinkedList<Double>();
		//List<Double> timeCriticaleach = new LinkedList<Double>();
		List<Double> xAxisComlexitySCORE = new LinkedList<Double>();
		List<Double> xAxisComlexityExistential = new LinkedList<Double>();
		
		RDFUtil.excludePredicatesFromCriticalInstanceConstants = false;
		
		// each configuration tests the algorithms on inputs of different size
		//int configurations = 1000;
		// for each configuration a number of repetitions is done and the average score is recorded
		//int repetitions = 20;
		//int stepIncrease = 2;
		
		//long millisecondTimeout = 5*60*1000; // 5 minutes timeout
		
		String labelScore = "r'\\texttt{score}'";
		String labelExistential = "r'\\texttt{existential-preserving}'";
		String ylabel = "Seconds";
		String xlabel = "Existential Constraints Size";
		
		boolean stopRecordingExistential = false;
		boolean stopRecordingSCORE = false;
		for(int j = 0; j <= configurations; j += stepIncrease) {
			List<ScoreResult> scoresSCORE = new LinkedList<ScoreResult>();
			List<ScoreResult> scoresExistential = new LinkedList<ScoreResult>();
			int newschemaviewSize = initialSchemaviewSize;
			int newsizeOfPredicateSpace = (int) (((double)newschemaviewSize)*1.1); // J
			int newconstantPool = newschemaviewSize;
			int newruleNum = ruleNum;
			int newAtomsInAntecedent =  atomsInAntecedent;
			int existential_constraints = existentials + j;
			// do one of each, so not to cluster all the SCORE and all the critical together
			for(int i = 0; i < repetitions*2; i++) {
				if(i % 2 == 0) {
					
					// CRITICAL
					if(!stopRecordingExistential) {
						try {
							ScoreResult srCritical = GeneratorUtil.evaluatePerformanceIteration(existential_constraints, 3, newsizeOfPredicateSpace, newruleNum, i/2, newschemaviewSize, newAtomsInAntecedent, newconstantPool, constantCreationRate, true);
							scoresExistential.add(srCritical);
							if(GeneratorUtil.avgResult(scoresExistential).time*(scoresExistential.size()) > millisecondTimeout*repetitions) {
								stopRecordingExistential = true;
								System.out.println("EARLY TERMINATION! STOP RECORDING CRITICAL AFTER "+scoresExistential.size()+" COMPUTE TIME "+GeneratorUtil.avgResult(scoresExistential).time+" > "+repetitions+" times "+millisecondTimeout);
								System.out.println("Last time recorded: "+srCritical.time);
							}
							
						} catch (LiteralEnforcingException e) {
							System.out.print("~ ");
						} catch (CyclicRulesException e ) {
							System.out.print("~' ");
						}
					}
				} else {
					// SCORE
					if(!stopRecordingSCORE) {		
						try {
							ScoreResult srSCORE = GeneratorUtil.evaluatePerformanceIteration(-1, 3, newsizeOfPredicateSpace, newruleNum, i/2, newschemaviewSize, newAtomsInAntecedent, newconstantPool, constantCreationRate, true);				
							scoresSCORE.add(srSCORE);
							
						} catch (LiteralEnforcingException e) {
							System.out.print("# ");
						} catch (CyclicRulesException e ) {
							System.out.print("#' ");
						}
					}
						
						
				}				
			}
			// Average results
			ScoreResult scoreSCORE = GeneratorUtil.avgResult(scoresSCORE);
			ScoreResult scoreExistential = GeneratorUtil.avgResult(scoresExistential);
			// terminate if average too high:
			if(scoreExistential.time > millisecondTimeout) {
				stopRecordingExistential = true;
				System.out.println("STOP RECORDING CRITICAL AFTER COMPUTE TIME "+scoreExistential.time);
			} 
			if(scoreSCORE.time > millisecondTimeout) {
				stopRecordingSCORE = true;
				System.out.println("STOP RECORDING SCORE AFTER COMPUTE TIME "+scoreSCORE.time);
			}
			System.out.print("\n["+existential_constraints+"] ");
			if(!stopRecordingExistential) {		
				xAxisComlexityExistential.add((double)existential_constraints);
				timeExistential.add(scoreExistential.time/1000);
				//timeCriticaleach.add(scoreCritical.averageRuleApplicationTime);
				System.out.print(" SCORE (existential preserving): "+scoreExistential.time);
				//System.out.println(j+"               Critical2 "+scoreCritical.averageRuleApplicationTime);				
			}
			if(!stopRecordingSCORE) {		
				//timeGPPGeach.add(scoreGPPG.averageRuleApplicationTime);
				xAxisComlexitySCORE.add((double)existential_constraints);
				timeSCORE.add(scoreSCORE.time/1000);
				System.out.print(" SCORE (basic): "+scoreSCORE.time);
				//System.out.println(j+"               GPPG2 "+scoreGPPG.averageRuleApplicationTime);
			}
			String pythonDataSCORE = xAxisComlexitySCORE+","+timeSCORE;
			String pythonDataExistential = xAxisComlexityExistential+","+timeExistential;
			System.out.println("\n plt.plot("+pythonDataSCORE+", linestyle='-', marker=',', color='C0', label="+labelScore+")\n");
			System.out.println(" plt.plot("+pythonDataExistential+", linestyle='-', marker='^', color='C1', label="+labelExistential+")\n");
			System.out.print("\n");
		}
		
		
		
		if(!warmup) {
			String pythonDataSCORE = xAxisComlexitySCORE+","+timeSCORE;
			String pythonDataExistential = xAxisComlexityExistential+","+timeExistential;
			String pythonScript = "from mpl_toolkits.mplot3d import Axes3D\n" + 
					"from matplotlib.pyplot import figure\n" + 
					"from matplotlib import rcParams\n" + 
					"figure(num=None, figsize=(6.4, 2.7), dpi=300, facecolor='w', edgecolor='k')\n" + 
					"import matplotlib.pyplot as plt\n"+
					"import numpy as np\n" + 
					"rc('font', **{'family':'serif', 'serif':['Computer Modern Roman'], 'monospace': ['Computer Modern Typewriter'], 'size': ['larger']})\n" + 
					"plt.rc('text', usetex=True)\n" + 
					"params = {'axes.labelsize': 13,'axes.titlesize':13, 'legend.fontsize': 13, 'xtick.labelsize': 13, 'ytick.labelsize': 13 }\n" + 
					"rcParams.update(params)\n" + 
					"#### EXPERIMENT: "+experimentName+"\n"+
					"#### Date: "+new Date().toString()+"\n"+
					"# MAIN PARAMETERS:\n"+
					"# atomsInAntecedent (n_A) = "+atomsInAntecedent+"\n"+
					"# constantCreationRate (\\pi_C) = "+constantCreationRate+"\n"+
					"# ruleNum (R) = "+ruleNum+"\n"+
					"# initialSchemaviewSize (S)= "+initialSchemaviewSize+"\n"+
					"# constant pools (U and L)= S\n"+
					"# predicate space (P)= 1.5*S\n"+
					"# EXPERIMENT LENGTH:\n"+
					"# configurations added to S from 0 to "+configurations+" tested with steps increases of "+stepIncrease+"\n"+
					"# average time cutoff "+millisecondTimeout+" milliseconds\n"+
					"# number of different datasets per configuration: "+repetitions+"\n\n"+
					"plt.plot("+pythonDataSCORE+", linestyle='-', marker='o', color='C0', label="+labelScore+")\n" + 
					"plt.plot("+pythonDataExistential+", linestyle='-', marker='^', color='C1', label="+labelExistential+")\n" + 
					"plt.ylabel('"+ylabel+"')\n"+
					"plt.xlabel('"+xlabel+"')\n"+
					"plt.legend()\n"+
					"plt.tight_layout()\n"+
					"plt.show()\n\n";
			System.out.println(pythonScript);
			
			try {
				Path p = Paths.get(System.getProperty("user.dir")+"/resultOutputs.txt");
				Files.createDirectories(p.getParent());
				if (!Files.exists(p, LinkOption.NOFOLLOW_LINKS))
				    Files.createFile(p);
				//Files.createFile(p);
			    Files.write(p, pythonScript.getBytes(), StandardOpenOption.APPEND);
			}catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
			
		} else {
			System.out.println("End Warmup run\n\n");
		}
	
		System.out.println("\nEnd Run\n");

	}
	
	
	private static void printScorePlot(boolean compact, String experimentName, int atomsInAntecedent, double constantCreationRate, int ruleNum,
			int configurations, int stepIncrease, int rule_increases, int rule_increase_step, long millisecondTimeout, int repetitions,
			int r, Map<Integer,List<Double>> xAxisComlexitiesGPPG, Map<Integer,List<Double>> timesGPPG, Map<Integer,List<Integer>> antecedentsSizeGPPG,
			Map<Integer,List<Double>> newFacts) {
			String labelScore = "SCORE";
			String labelCritical = "CRITICAL";
			String ylabel = "Seconds";
			String xlabel = "Number of Rules";
			String zlabel = "Antecedent Triple Size";
			String pythonScript = "from mpl_toolkits.mplot3d import Axes3D\n"
					+ "from matplotlib.pyplot import figure\n"
					+ "from matplotlib import rcParams\n"
					+ "figure(num=None, figsize=(6.4, 2.7), dpi=300, facecolor='w', edgecolor='k')\n"
					+ "import matplotlib.pyplot as plt\n"
					+ "import numpy as np\n" + 
					"rc('font', **{'family':'serif', 'serif':['Computer Modern Roman'], 'monospace': ['Computer Modern Typewriter'], 'size': ['larger']})\n" + 
					"plt.rc('text', usetex=True)\n" + 
					"params = {'axes.labelsize': 13,'axes.titlesize':13, 'legend.fontsize': 13, 'xtick.labelsize': 13, 'ytick.labelsize': 13 }\n" + 
					"rcParams.update(params)\n" + 
					"#### EXPERIMENT: "+experimentName+"\n"+
					"#### Date: "+new Date().toString()+"\n"+
					"# MAIN PARAMETERS:\n"+
					"# atomsInAntecedent (n_A) = "+atomsInAntecedent+"\n"+
					"# constantCreationRate (\\pi_C) = "+constantCreationRate+"\n"+
					"# ruleNum (R) = "+ruleNum+"\n"+
					"# initialSchemaviewSize (S) = 30\n"+
					"# constant pools (U and L)= S\n"+
					"# predicate space (P)= 1.5*S\n"+
					"# EXPERIMENT LENGTH:\n"+
					"# configurations added to R from 0 to "+configurations+" tested with steps increases of "+stepIncrease+"\n"+
					"# configurations of antecedent length from 0 to "+rule_increases+" tested with steps increases of "+rule_increase_step+"\n"+
					"# average time cutoff "+millisecondTimeout+" milliseconds\n"+
					"# number of different datasets per configuration: "+repetitions+"\n\n";			
			if(compact) pythonScript = "# ...COMPACT\n";
			int colorIndex = 0;	
			for(int r1 = 0; r1 <= r; r1 += rule_increase_step)  {
					String pythonDataSCORE = xAxisComlexitiesGPPG.get(new Integer(r1))+","+timesGPPG.get(new Integer(r1));
					//String pythonDataCRITICAL = xAxisComlexitiesCritical.get(new Integer(r1))+","+timesCritical.get(new Integer(r1));
					pythonScript = pythonScript+
							"plt.plot("+pythonDataSCORE+", linestyle='-', marker='$"+(colorIndex)+"$', color='C"+(colorIndex)+"',  label='"+labelScore+" ("+(r1+atomsInAntecedent)+")')\n"
							//+"plt.plot("+pythonDataCRITICAL+", linestyle='-', marker='^', color='C"+r+"', label='"+labelCritical+"_"+r+"')\n"
							;
					colorIndex++;
				}
			if(!compact) pythonScript = pythonScript+"plt.ylabel('"+ylabel+"')\n"+
						"plt.xlabel('"+xlabel+"')\n"+
						"plt.legend()\n"+
						"plt.tight_layout()\n"+
						"plt.show()\n";
			//}
			System.out.println(pythonScript);
			
			try {
			    Files.write(Paths.get("resultOutputs.txt"), pythonScript.getBytes(), StandardOpenOption.APPEND);
			}catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
			
			labelScore = "SCORE";
			labelCritical = "CRITICAL";
			ylabel = "Applicable Rules";
			xlabel = "Rule Number";
			zlabel = "Antecedent Triple Size";
			pythonScript = "from mpl_toolkits.mplot3d import Axes3D\n"
					+ "from matplotlib.pyplot import figure\n"
					+ "figure(num=None, figsize=(6.4, 2.7), dpi=300, facecolor='w', edgecolor='k')\n"
					+ "import matplotlib.pyplot as plt\n"
					+ "import numpy as np\n" + 
					"#### EXPERIMENT: "+experimentName+"\n"+
					"#### Date: "+new Date().toString()+"\n"+
					"# MAIN PARAMETERS:\n"+
					"# atomsInAntecedent (n_A) = "+atomsInAntecedent+"\n"+
					"# constantCreationRate (\\pi_C) = "+constantCreationRate+"\n"+
					"# ruleNum (R) = "+ruleNum+"\n"+
					"# initialSchemaviewSize (S) = 30\n"+
					"# constant pools (U and L)= S\n"+
					"# predicate space (P)= 1.5*S\n"+
					"# EXPERIMENT LENGTH:\n"+
					"# configurations added to R from 0 to "+configurations+" tested with steps increases of "+stepIncrease+"\n"+
					"# configurations of antecedent length from 0 to "+rule_increases+" tested with steps increases of "+rule_increase_step+"\n"+
					"# average time cutoff "+millisecondTimeout+" milliseconds\n"+
					"# number of different datasets per configuration: "+repetitions+"\n\n";		
			if(compact) pythonScript = "# ...COMPACT\n";
			colorIndex = 0;	
			for(int r1 = 0; r1 <= r; r1 += rule_increase_step)  {
					String pythonDataSCORE = xAxisComlexitiesGPPG.get(new Integer(r1))+","+newFacts.get(new Integer(r1));
					pythonScript = pythonScript+
							"plt.plot("+pythonDataSCORE+", linestyle='-', marker='$"+(colorIndex)+"$', color='C"+(colorIndex)+"',  label='"+labelScore+" ("+(r1+atomsInAntecedent)+")')\n"
							//+"plt.plot("+pythonDataCRITICAL+", linestyle='-', marker='^', color='C"+r+"', label='"+labelCritical+"_"+r+"')\n"
							;
					colorIndex++;
				}
			if(!compact) pythonScript = pythonScript+"plt.ylabel('"+ylabel+"')\n"+
						"plt.xlabel('"+xlabel+"')\n"+
						"plt.legend()\n"+
						"plt.tight_layout()\n"+
						"plt.show()\n";
			//}
				System.out.println(pythonScript+"\n\n");
			
			try {
			    Files.write(Paths.get("resultOutputs.txt"), pythonScript.getBytes(), StandardOpenOption.APPEND);
			}catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
	}
	
	private static void runCriticalInstancePerformanceComparisonSchemaSizeDifferentRuleNumber(boolean schema_proportional_to_rule_num, int rule_increase_step, int rule_increases, String experimentName, long millisecondTimeout, int configurations, int repetitions, int stepIncrease, int initialSchemaviewSize, int atomsInAntecedent, double constantCreationRate, int ruleNum, boolean warmup) throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		runCriticalInstancePerformanceComparisonSchemaSizeDifferentRuleNumber(schema_proportional_to_rule_num, rule_increase_step, rule_increases, experimentName, millisecondTimeout, configurations, repetitions, stepIncrease, initialSchemaviewSize, atomsInAntecedent, constantCreationRate, ruleNum, warmup, -1);
	}

	private static void runCriticalInstancePerformanceComparisonSchemaSizeDifferentRuleNumber(boolean schema_proportional_to_rule_num, int rule_increase_step, int rule_increases, String experimentName, long millisecondTimeout, int configurations, int repetitions, int stepIncrease, int initialSchemaviewSize, int atomsInAntecedent, double constantCreationRate, int ruleNum, boolean warmup, int sizeOfPredicateSpace) throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		
		
		if(warmup) {
			System.out.println("\n(Warmup run "+experimentName+")\n");
		} else {
			System.out.println("\n(Normal run "+experimentName+")\n");
		}
		//int atomsInAntecedent = 2;
		// what is the chance that each variable in a rule is substituted with a constant
		
					// when instantiating a new constant, choose one out of this many
					//int constantPool = 3;
		// number of rules to consider
		//int ruleNum = 4;
		// number of atoms in schema
		//int initialSchemaviewSize =  2;
		    		// how many possible predicates to consider
		    		//int sizeOfPredicateSpace = 7;
		
		Map<Integer,List<Double>> timesGPPG = new HashMap<Integer,List<Double>>();
		Map<Integer,List<Double>> timesCritical = new HashMap<Integer,List<Double>>();
		Map<Integer,List<Double>> xAxisComlexitiesGPPG = new HashMap<Integer,List<Double>>();
		Map<Integer,List<Double>> xAxisComlexitiesCritical = new HashMap<Integer,List<Double>>();
		Map<Integer,List<Integer>> antecedentsSizeGPPG = new HashMap<Integer,List<Integer>>();
		Map<Integer,List<Integer>> antecedentsSizeCritical = new HashMap<Integer,List<Integer>>();
		Map<Integer,List<Double>> newFacts = new HashMap<Integer,List<Double>>();
		
		for(int r = 0; r < rule_increases; r += rule_increase_step) {
			// SCORE
			Integer R = new Integer(r);
			List<Double> timeGPPG = new LinkedList<Double>();
			timesGPPG.put(R, timeGPPG);
			List<Double> newFactsGPPG = new LinkedList<Double>();
			newFacts.put(R, newFactsGPPG);
			List<Double> timeCritical = new LinkedList<Double>();
			timesCritical.put(R, timeCritical);
			//List<Double> timeGPPGeach = new LinkedList<Double>();
			//List<Double> timeCriticaleach = new LinkedList<Double>();
			List<Double> xAxisComlexityGPPG = new LinkedList<Double>();
			xAxisComlexitiesGPPG.put(R, xAxisComlexityGPPG);
			List<Double> xAxisComlexityCritical = new LinkedList<Double>();
			xAxisComlexitiesCritical.put(R, xAxisComlexityCritical);
			List<Integer> antecedentSizeGPPG = new LinkedList<Integer>();
			antecedentsSizeGPPG.put(R, antecedentSizeGPPG);
			List<Integer> antecedentSizeCritical = new LinkedList<Integer>();
			antecedentsSizeCritical.put(R, antecedentSizeCritical);
			
			RDFUtil.excludePredicatesFromCriticalInstanceConstants = false;
			
			// each configuration tests the algorithms on inputs of different size
			//int configurations = 1000;
			// for each configuration a number of repetitions is done and the average score is recorded
			//int repetitions = 20;
			//int stepIncrease = 2;
			
			//long millisecondTimeout = 5*60*1000; // 5 minutes timeout
			
			
			boolean stopRecordingCritical = true;
			boolean stopRecordingSCORE = false;
			for(int j = 0; j <= configurations; j += stepIncrease) {
				List<ScoreResult> scoresGPPG = new LinkedList<ScoreResult>();
				List<ScoreResult> scoresCritical = new LinkedList<ScoreResult>();
				int newschemaviewSize = initialSchemaviewSize;
				int newconstantPool = newschemaviewSize;
				int newruleNum = ruleNum+j;
				int newAtomsInAntecedent = atomsInAntecedent+r;
				if(schema_proportional_to_rule_num) {
					newschemaviewSize = initialSchemaviewSize*newAtomsInAntecedent;
					//newschemaviewSize = newruleNum*(newAtomsInAntecedent-1);
				}
				if(newruleNum == 0) newruleNum = 1;
				int newsizeOfPredicateSpace = sizeOfPredicateSpace;
				if(newsizeOfPredicateSpace < 0) newsizeOfPredicateSpace = (int) (((double)newschemaviewSize)*1.5); // J
				// do one of each, so not to cluster all the SCORE and all the critical together
				for(int i = 0; i < repetitions*2; i++) {
					if(i % 2 == 0) {
						// CRITICAL
						if(!stopRecordingCritical) {						
							ScoreResult srCritical = GeneratorUtil.evaluatePerformanceIteration(4, newsizeOfPredicateSpace, newruleNum, i/2, newschemaviewSize, newAtomsInAntecedent, newconstantPool, constantCreationRate, false);
							scoresCritical.add(srCritical);
							if(GeneratorUtil.avgResult(scoresCritical).time*(scoresCritical.size()) > millisecondTimeout*repetitions) {
								stopRecordingCritical = true;
								System.out.println("EARLY TERMINATION! STOP RECORDING CRITICAL AFTER "+scoresCritical.size()+" COMPUTE TIME "+GeneratorUtil.avgResult(scoresCritical).time+" > "+repetitions+" times "+millisecondTimeout);
								System.out.println("Last time recorded: "+srCritical.time);
							}
						}
					} else {
						// SCORE
						if(!stopRecordingSCORE) {						
							ScoreResult srSCORE = GeneratorUtil.evaluatePerformanceIteration(3, newsizeOfPredicateSpace, newruleNum, i/2, newschemaviewSize, newAtomsInAntecedent, newconstantPool, constantCreationRate, false);				
							scoresGPPG.add(srSCORE);
						}
					}				
				}
				
				// Average results
				ScoreResult scoreGPPG = GeneratorUtil.avgResult(scoresGPPG);
				ScoreResult scoreCritical = GeneratorUtil.avgResult(scoresCritical);
				// terminate if average too high:
				if(scoreCritical.time > millisecondTimeout) {
					stopRecordingCritical = true;
					System.out.println("STOP RECORDING CRITICAL AFTER COMPUTE TIME "+scoreCritical.time);
				} 
				if(scoreGPPG.time > millisecondTimeout) {
					stopRecordingSCORE = true;
					System.out.println("STOP RECORDING SCORE AFTER COMPUTE TIME "+scoreGPPG.time);
				}
				System.out.print("\n["+j+"] ");
				if(!stopRecordingCritical) {		
					xAxisComlexityCritical.add((double)newruleNum);
					antecedentSizeCritical.add((int)newAtomsInAntecedent);
					timeCritical.add(scoreCritical.time/1000);
					//timeCriticaleach.add(scoreCritical.averageRuleApplicationTime);
					System.out.print(" Critical: "+scoreCritical.time/1000);
					//System.out.println(j+"               Critical2 "+scoreCritical.averageRuleApplicationTime);				
				}
				if(!stopRecordingSCORE) {		
					//timeGPPGeach.add(scoreGPPG.averageRuleApplicationTime);
					xAxisComlexityGPPG.add((double)newruleNum);
					antecedentSizeGPPG.add((int)newAtomsInAntecedent);
					timeGPPG.add(scoreGPPG.time/1000);
					newFactsGPPG.add(scoreGPPG.applicableRules);
					System.out.print(" SCORE: "+scoreGPPG.time/1000);
					//System.out.println(j+"               GPPG2 "+scoreGPPG.averageRuleApplicationTime);
				}
				System.out.print("\n");
				if(!warmup) printScorePlot(true, experimentName, atomsInAntecedent, constantCreationRate, ruleNum, configurations, stepIncrease, rule_increases, rule_increase_step, millisecondTimeout, repetitions, r, xAxisComlexitiesGPPG, timesGPPG, antecedentsSizeGPPG, newFacts);

			}
			
			if(!warmup) {
				printScorePlot(false, experimentName, atomsInAntecedent, constantCreationRate, ruleNum, configurations, stepIncrease, rule_increases, rule_increase_step, millisecondTimeout, repetitions, r, xAxisComlexitiesGPPG, timesGPPG, antecedentsSizeGPPG, newFacts);
			}
		}
		
		
		
		
		if(!warmup) {
			System.out.println("\n\n FINAL RESULTS!\n\n");
			printScorePlot(false, experimentName, atomsInAntecedent, constantCreationRate, ruleNum, configurations, stepIncrease, rule_increases, rule_increase_step, millisecondTimeout, repetitions, rule_increases, xAxisComlexitiesGPPG, timesGPPG, antecedentsSizeGPPG, newFacts);

			/*String labelScore = "SCORE";
			String labelCritical = "CRITICAL";
			String ylabel = "Seconds";
			String xlabel = "Rule Number";
			String pythonScript = "import matplotlib.pyplot as plt\n" + 
					"#### EXPERIMENT: "+experimentName+"\n"+
					"#### Date: "+new Date().toString()+"\n"+
					"# MAIN PARAMETERS:\n"+
					"# atomsInAntecedent (n_A) = "+atomsInAntecedent+"\n"+
					"# constantCreationRate (\\pi_C) = "+constantCreationRate+"\n"+
					"# ruleNum (R) = "+ruleNum+"\n"+
					"# initialSchemaviewSize (S) = 30\n"+
					"# constant pools (U and L)= S\n"+
					"# predicate space (P)= 1.5*S\n"+
					"# EXPERIMENT LENGTH:\n"+
					"# configurations added to R from 0 to "+configurations+" tested with steps increases of "+stepIncrease+"\n"+
					"# configurations of antecedent length from 0 to "+rule_increases+" tested with steps increases of "+rule_increase_step+"\n"+
					"# average time cutoff "+millisecondTimeout+" milliseconds\n"+
					"# number of different datasets per configuration: "+repetitions+"\n\n";
			int colorIndex = 0;
			for(int r = 0; r < rule_increases; r += rule_increase_step)  {
				String pythonDataSCORE = xAxisComlexitiesGPPG.get(new Integer(r))+","+timesGPPG.get(new Integer(r));
				//String pythonDataCRITICAL = xAxisComlexitiesCritical.get(new Integer(r))+","+timesCritical.get(new Integer(r));
				pythonScript = pythonScript+
						"plt.plot("+pythonDataSCORE+", linestyle='-', marker='$"+(r+1)+"$', color='C"+(colorIndex)+"',  label='"+labelScore+" ("+(r+1)+")')\n"
						//+"plt.plot("+pythonDataCRITICAL+", linestyle='-', marker='^', color='C"+r+"', label='"+labelCritical+"_"+r+"')\n"
						;
				colorIndex++;
			}
			pythonScript = pythonScript+"plt.ylabel('"+ylabel+"')\n"+
					"plt.xlabel('"+xlabel+"')\n"+
					"plt.legend()\n"+
					"plt.tight_layout()\n"+
					"plt.show()\n\n";
			System.out.println(pythonScript);
			
			try {
			    Files.write(Paths.get("resultOutputs.txt"), pythonScript.getBytes(), StandardOpenOption.APPEND);
			}catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}*/
			
		} else {
			System.out.println("End Warmup run\n\n");
		}
	
		System.out.println("\nEnd Run\n");

	}

	private static void runCriticalInstancePerformanceComparisonRuleNum(boolean warmup) throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		
		
		if(warmup == false) {
			runCriticalInstancePerformanceComparisonRuleNum(true);
		} else {
			System.out.println("(Warmup run)\n");
		}
		int atomsInAntecedent = 4;
		// what is the chance that each variable in a rule is substituted with a constant
		double constantCreationRate = 0.1;
		// when instantiating a new constant, choose one out of this many
		int constantPool = 10;
		// number of rules to consider
		int ruleNum = 10;
		// number of atoms in schema
		int schemaviewSize =  5;
		// how many possible predicates to consider
		int sizeOfPredicateSpace = 10;
		// how many different datasets to create with the same configuration
		int numberOfDifferentDatasets = 1;
		
		
		
		// SCORE
		List<Double> timeGPPG = new LinkedList<Double>();
		List<Double> timeCritical = new LinkedList<Double>();
		List<Double> xAxisComlexityGPPG = new LinkedList<Double>();
		List<Double> xAxisComlexityCritical = new LinkedList<Double>();
		
		RDFUtil.excludePredicatesFromCriticalInstanceConstants = false;
		int repetitions = 10;
		int configurations = 15;
		boolean stopRecordingCritical = false;
		for(int j = 0; j < configurations; j++) {
			List<ScoreResult> scoresGPPG = new LinkedList<ScoreResult>();
			List<ScoreResult> scoresCritical = new LinkedList<ScoreResult>();
			int newschemaviewSize = schemaviewSize;
			int newsizeOfPredicateSpace = sizeOfPredicateSpace;
			int newconstantPool = constantPool;
			int newruleNum = ruleNum + (j*10);
			for(int i = 0; i < repetitions*2; i++) {
				if(i % 2 == 0) {
					if(!stopRecordingCritical) {						
						ScoreResult srCritical = GeneratorUtil.evaluatePerformanceIteration(1, newsizeOfPredicateSpace, newruleNum, i/2, newschemaviewSize, atomsInAntecedent, newconstantPool, constantCreationRate, false);
						if(srCritical.time > 60000) {
							stopRecordingCritical = true;
							System.out.println("STOP RECORDING CRITICAL AFTER COMPUTE TIME "+srCritical.time);
						}
						scoresCritical.add(srCritical);
					}
				} else {
					ScoreResult srGPPG = GeneratorUtil.evaluatePerformanceIteration(0, newsizeOfPredicateSpace, newruleNum, i/2, newschemaviewSize, atomsInAntecedent, newconstantPool, constantCreationRate, false);				
					scoresGPPG.add(srGPPG);
				}				
			}
			if(scoresGPPG.size() == repetitions) {
				ScoreResult scoreGPPG = GeneratorUtil.avgResult(scoresGPPG);
				timeGPPG.add(scoreGPPG.time);
				xAxisComlexityGPPG.add((double)newruleNum);
				System.out.println(j+" GPPG "+scoreGPPG.time);
			}
			if(scoresCritical.size() == repetitions) {
				ScoreResult scoreCritical = GeneratorUtil.avgResult(scoresCritical);
				xAxisComlexityCritical.add((double)newruleNum);
				timeCritical.add(scoreCritical.time);
				System.out.println(j+" Critical "+scoreCritical.time);
			}
			System.out.println("");
			
		}
		//if(warmup == false) {
			// PRINT 
			Plot plt = Plot.create();
			plt.plot().add(xAxisComlexityGPPG, timeGPPG);
			plt.plot().add(xAxisComlexityCritical, timeCritical);
			plt.xlabel("Triplestore Schema Size");
			plt.ylabel("milliseconds");
			plt.title("Time vs Triplestore Schema Size");
			plt.legend();
			plt.show();
		//}
		System.out.println("\nEnd Run\n");
		
	}
	
	private static void runCriticalInstanceSemanticEqualityTest() throws FileNotFoundException, CloneNotSupportedException, IOException {
		System.out.println("This test checks whether the Critical Instance and GPPG approaches produce the same results.");
		int atomsInAntecedent = 3;
		// what is the chance that each variable in a rule is substituted with a constant
		double constantCreationRate = 0.1;
		// when instantiating a new constant, choose one out of this many
		int constantPool = 2;
		// number of rules to consider
		int ruleNum = 6;
		// number of atoms in schema
		int schemaviewSize =  7;
		// how many possible predicates to consider
		int sizeOfPredicateSpace = 15;
		// how many different datasets to create with the same configuration
		boolean allTheSame = true;
		int iterations = 25;
		int differentScenarios = 10;
		
		int iteration = 0;
		
		for(int j = 0; j < differentScenarios/3; j++) {
			for(int i = 0; i < iterations; i++) {
				boolean sr = GeneratorUtil.testSemanticEqualityOfApproaches(sizeOfPredicateSpace+j, ruleNum+(j%4), i, schemaviewSize+j, atomsInAntecedent-1, constantPool+j, constantCreationRate+0.3);				
				System.out.println("Finished Iteration v2-"+ iteration++ +"\nAll tests so far passed? "+allTheSame);
				allTheSame = allTheSame && sr;
			}
		}
		iteration = 0;
		for(int j = 0; j < differentScenarios; j++) {
			for(int i = 0; i < iterations; i++) {
				boolean sr = GeneratorUtil.testSemanticEqualityOfApproaches(sizeOfPredicateSpace+j+j, ruleNum+j, i, schemaviewSize+j, atomsInAntecedent, constantPool+j, constantCreationRate);				
				System.out.println("Finished Iteration v1-"+ iteration++ +"\nAll tests so far passed? "+allTheSame);
				allTheSame = allTheSame && sr;
			}
		}
		
		
		
		
		System.out.println("All comparison equal: "+allTheSame+" (over "+(differentScenarios*iterations*2)+" iterations)");
	}
	
	/**
	 * This tests how well does GPPG scale to larger schema sizes
	 * Size of predicate space is kept 50% bigger than the number of schema triples
	 * The constant pool is kept equal to the schema size
	 * The number of rules is kept equal to the schema size
	 * @throws FileNotFoundException
	 * @throws CloneNotSupportedException
	 * @throws IOException
	 * @throws PythonExecutionException
	 */
	private static void runGradientSchemaView() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		int atomsInAntecedent = 4;
		// what is the chance that each variable in a rule is substituted with a constant
		double constantCreationRate = 0.2;
		// when instantiating a new constant, choose one out of this many
		int constantPool = -1;
		// number of rules to consider
		int ruleNum = -1;
		// number of atoms in schema
		int schemaviewSize =  10;
		// how many possible predicates to consider
		int sizeOfPredicateSpace = -1;
		// how many different datasets to create with the same configuration
		int repetitions = 52;
		int cutoff = 2;
		// SCORE
		List<Double> time = new LinkedList<Double>();
		List<Double> newPred = new LinkedList<Double>();
		List<Double> xAxisRuleSize = new LinkedList<Double>();
		for(int i = 0; i < 50; i++) {
			int newschemaviewSize = schemaviewSize + (i * 10);
			int newconstantPool = newschemaviewSize;
			int newruleNum = newschemaviewSize;
			int newsizeOfPredicateSpace = (int) ( ((double) 1.5)*newschemaviewSize );
			ScoreResult sr = GeneratorUtil.evaluatePerformance(repetitions, cutoff, 0,newsizeOfPredicateSpace, newruleNum, newschemaviewSize, atomsInAntecedent, newconstantPool, constantCreationRate);				
			time.add(sr.time);
			newPred.add(sr.newPredicates);
			xAxisRuleSize.add((double)newschemaviewSize);
		}
		// PRINT 
		Plot plt = Plot.create();
		plt.plot().add(xAxisRuleSize, time);
		//plt.plot().add(xAxisRuleSize, newPred);
		plt.xlabel("Number of triples in the schema");
		plt.ylabel("Milliseconds");
		plt.title("");
		plt.legend();
		plt.show();
		
		Plot plt2 = Plot.create();
		//plt2.plot().add(xAxisRuleSize, time);
		plt2.plot().add(xAxisRuleSize, newPred);
		plt2.xlabel("Number of triples in the schema");
		plt2.ylabel("New predicates");
		plt2.title("");
		plt2.legend();
		plt2.show();
	}
	
	
	
	private static void runGradientCI() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		int atomsInAntecedent = 4;
		// what is the chance that each variable in a rule is substituted with a constant
		double constantCreationRate = 0.1;
		// when instantiating a new constant, choose one out of this many
		int constantPool = 5;
		// number of rules to consider
		int ruleNum = 10;
		// number of atoms in schema
		int schemaviewSize =  5;
		// how many possible predicates to consider
		int sizeOfPredicateSpace = 50;
		// how many different datasets to create with the same configuration
		int numberOfDifferentDatasets = 1;
		// SCORE
		List<Double> time = new LinkedList<Double>();
		List<Double> newPred = new LinkedList<Double>();
		List<Double> xAxisRuleSize = new LinkedList<Double>();
		for(int i = 0; i < 20; i++) {
			constantPool += 2;
			schemaviewSize += 2;
			ScoreResult sr = GeneratorUtil.evaluatePerformance(1,sizeOfPredicateSpace, ruleNum, schemaviewSize, atomsInAntecedent, constantPool, constantCreationRate);				
			time.add(sr.time);
			newPred.add(sr.newPredicates);
			xAxisRuleSize.add((double)constantPool);
		}
		// PRINT 
		Plot plt = Plot.create();
		plt.plot().add(xAxisRuleSize, time);
		//plt.plot().add(xAxisRuleSize, newPred);
		plt.xlabel("constantPool");
		plt.ylabel("milliseconds");
		plt.title("Time");
		plt.legend();
		plt.show();
		
		Plot plt2 = Plot.create();
		//plt2.plot().add(xAxisRuleSize, time);
		plt2.plot().add(xAxisRuleSize, newPred);
		plt2.xlabel("constantPool");
		plt2.ylabel("New Predicates");
		plt2.title("New Predicates");
		plt2.legend();
		plt2.show();
	}
	
	private static void runGradient() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		// BASIC CONFIGURATION
		// how many atoms occur in the antecedent
		int atomsInAntecedent = 4;
		// what is the chance that each variable in a rule is substituted with a constant
		double constantCreationRate = 0.1;
		// when instantiating a new constant, choose one out of this many
		int constantPool = 10;
		// number of rules to consider
		int ruleNum = 5;
		// number of atoms in schema
		int schemaviewSize =  30;
		// how many possible predicates to consider
		int sizeOfPredicateSpace = 50;
		
		
		// SCORE
		List<Double> time = new LinkedList<Double>();
		List<Double> newPred = new LinkedList<Double>();
		List<Double> xAxisRuleSize = new LinkedList<Double>();
		for(int i = 0; i < 50; i++) {
			int currentruleNum = ruleNum+(5*i);
			ScoreResult sr = GeneratorUtil.evaluatePerformance(0,sizeOfPredicateSpace, currentruleNum, schemaviewSize, atomsInAntecedent, constantPool, constantCreationRate);				
			time.add(sr.time);
			newPred.add(sr.newPredicates);
			xAxisRuleSize.add((double)currentruleNum);
		}
		
		// PRINT 
		Plot plt = Plot.create();
		plt.plot().add(xAxisRuleSize, time);
		//plt.plot().add(xAxisRuleSize, newPred);
		plt.xlabel("Rule Size");
		plt.ylabel("milliseconds");
		plt.title("Time");
		plt.legend();
		plt.show();
		
		Plot plt2 = Plot.create();
		//plt2.plot().add(xAxisRuleSize, time);
		plt2.plot().add(xAxisRuleSize, newPred);
		plt2.xlabel("Rule Size");
		plt2.ylabel("New Predicates");
		plt2.title("New Predicates");
		plt2.legend();
		plt2.show();
	}
	
	private static void runGradient2() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		// BASIC CONFIGURATION
		// how many atoms occur in the antecedent
		int atomsInAntecedent = 4;
		// what is the chance that each variable in a rule is substituted with a constant
		double constantCreationRate = 0.1;
		// when instantiating a new constant, choose one out of this many
		int constantPool = 30;
		// number of rules to consider
		int ruleNum = 70;
		// number of atoms in schema
		int schemaviewSize =  10;
		// how many possible predicates to consider
		int sizeOfPredicateSpace = 250;
		
		
		// SCORE
		List<Double> time = new LinkedList<Double>();
		List<Double> newPred = new LinkedList<Double>();
		List<Double> xAxisRuleSize = new LinkedList<Double>();
		for(int i = 0; i < 44; i++) {
			int newSchemaviewSize = schemaviewSize+(5*i);
			ScoreResult sr = GeneratorUtil.evaluatePerformance(0,sizeOfPredicateSpace, ruleNum, newSchemaviewSize, atomsInAntecedent, constantPool, constantCreationRate);				
			time.add(sr.time);
			newPred.add(sr.newPredicates);
			xAxisRuleSize.add((double)newSchemaviewSize);
		}
		
		// PRINT 
		Plot plt = Plot.create();
		plt.plot().add(xAxisRuleSize, time);
		//plt.plot().add(xAxisRuleSize, newPred);
		plt.xlabel("Schema View Size");
		plt.ylabel("milliseconds");
		plt.title("Time");
		plt.legend();
		plt.show();
		
		Plot plt2 = Plot.create();
		//plt2.plot().add(xAxisRuleSize, time);
		plt2.plot().add(xAxisRuleSize, newPred);
		plt2.xlabel("Schema View Size");
		plt2.ylabel("New Predicates");
		plt2.title("New Predicates");
		plt2.legend();
		plt2.show();
	}
	
	private static void runGradient3() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		// BASIC CONFIGURATION
		// how many atoms occur in the antecedent
		int atomsInAntecedent = 4;
		// what is the chance that each variable in a rule is substituted with a constant
		double constantCreationRate = 0.3;
		// when instantiating a new constant, choose one out of this many
		int constantPool = 1;
		// number of rules to consider
		int ruleNum = 100;
		// number of atoms in schema
		int schemaviewSize =  100;
		// how many possible predicates to consider
		int sizeOfPredicateSpace = 250;
		
		
		// SCORE
		List<Double> time = new LinkedList<Double>();
		List<Double> newPred = new LinkedList<Double>();
		List<Double> xAxisRuleSize = new LinkedList<Double>();
		for(int i = 0; i < 25; i++) {
			int newconstantPool = constantPool+(10*i);
			ScoreResult sr = GeneratorUtil.evaluatePerformance(0,sizeOfPredicateSpace, ruleNum, schemaviewSize, atomsInAntecedent, newconstantPool, constantCreationRate);				
			time.add(sr.time);
			newPred.add(sr.newPredicates);
			xAxisRuleSize.add((double)newconstantPool);
		}
		
		// PRINT 
		Plot plt = Plot.create();
		plt.plot().add(xAxisRuleSize, time);
		//plt.plot().add(xAxisRuleSize, newPred);
		plt.xlabel("Schema View Size");
		plt.ylabel("milliseconds");
		plt.title("Time");
		plt.legend();
		plt.show();
		
		Plot plt2 = Plot.create();
		//plt2.plot().add(xAxisRuleSize, time);
		plt2.plot().add(xAxisRuleSize, newPred);
		plt2.xlabel("Constant Pool");
		plt2.ylabel("New Predicates");
		plt2.title("New Predicates");
		plt2.legend();
		plt2.show();
	}
	
	private static void runGradient4() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		// BASIC CONFIGURATION
		// how many atoms occur in the antecedent
		int atomsInAntecedent = 1;
		// what is the chance that each variable in a rule is substituted with a constant
		double constantCreationRate = 0.1;
		// when instantiating a new constant, choose one out of this many
		int constantPool = 30;
		// number of rules to consider
		int ruleNum = 100;
		// number of atoms in schema
		int schemaviewSize =  50;
		// how many possible predicates to consider
		int sizeOfPredicateSpace = 100;
		
		
		// SCORE
		List<Double> time = new LinkedList<Double>();
		List<Double> newPred = new LinkedList<Double>();
		List<Double> xAxisRuleSize = new LinkedList<Double>();
		for(int i = 0; i < 29; i++) {
			int newatomsInAntecedent = atomsInAntecedent+1*i;
			ScoreResult sr = GeneratorUtil.evaluatePerformance(0,sizeOfPredicateSpace, ruleNum, schemaviewSize, newatomsInAntecedent, constantPool, constantCreationRate);				
			time.add(sr.time);
			newPred.add(sr.newPredicates);
			xAxisRuleSize.add((double)newatomsInAntecedent);
		}
		
		// PRINT 
		Plot plt = Plot.create();
		plt.plot().add(xAxisRuleSize, time);
		//plt.plot().add(xAxisRuleSize, newPred);
		plt.xlabel("Rule length");
		plt.ylabel("milliseconds");
		plt.title("Time");
		plt.legend();
		plt.show();
		
		Plot plt2 = Plot.create();
		//plt2.plot().add(xAxisRuleSize, time);
		plt2.plot().add(xAxisRuleSize, newPred);
		plt2.xlabel("Rule length");
		plt2.ylabel("New Predicates");
		plt2.title("New Predicates");
		plt2.legend();
		plt2.show();
	}
	
	private static void runGradient5() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		// BASIC CONFIGURATION
		// how many atoms occur in the antecedent
		int atomsInAntecedent = 4;
		// what is the chance that each variable in a rule is substituted with a constant
		double constantCreationRate = 0.1;
		// when instantiating a new constant, choose one out of this many
		int constantPool = 30;
		// number of rules to consider
		int ruleNum = 100;
		// number of atoms in schema
		int schemaviewSize =  50;
		// how many possible predicates to consider
		int sizeOfPredicateSpace = 60;
		
		
		// SCORE
		List<Double> time = new LinkedList<Double>();
		List<Double> newPred = new LinkedList<Double>();
		List<Double> xAxisRuleSize = new LinkedList<Double>();
		for(int i = 0; i < 20; i++) {
			int newsizeOfPredicateSpace = sizeOfPredicateSpace+(20*i);
			ScoreResult sr = GeneratorUtil.evaluatePerformance(0,newsizeOfPredicateSpace, ruleNum, schemaviewSize, atomsInAntecedent, constantPool, constantCreationRate);				
			time.add(sr.time);
			newPred.add(sr.newPredicates);
			xAxisRuleSize.add((double)newsizeOfPredicateSpace);
		}
		
		// PRINT 
		Plot plt = Plot.create();
		plt.plot().add(xAxisRuleSize, time);
		//plt.plot().add(xAxisRuleSize, newPred);
		plt.xlabel("Size Of Predicate Space");
		plt.ylabel("milliseconds");
		plt.title("Time");
		plt.legend();
		plt.show();
		
		Plot plt2 = Plot.create();
		//plt2.plot().add(xAxisRuleSize, time);
		plt2.plot().add(xAxisRuleSize, newPred);
		plt2.xlabel("Size Of Predicate Space");
		plt2.ylabel("New Predicates");
		plt2.title("New Predicates");
		plt2.legend();
		plt2.show();
	}
	
	private static void runGradient6() throws FileNotFoundException, CloneNotSupportedException, IOException, PythonExecutionException {
		// BASIC CONFIGURATION
		// how many atoms occur in the antecedent
		int atomsInAntecedent = 4;
		// what is the chance that each variable in a rule is substituted with a constant
		double constantCreationRate = 0.01;
		// when instantiating a new constant, choose one out of this many
		int constantPool = 30;
		// number of rules to consider
		int ruleNum = 100;
		// number of atoms in schema
		int schemaviewSize =  50;
		// how many possible predicates to consider
		int sizeOfPredicateSpace = 100;
		
		
		// SCORE
		List<Double> time = new LinkedList<Double>();
		List<Double> newPred = new LinkedList<Double>();
		List<Double> xAxisRuleSize = new LinkedList<Double>();
		for(int i = 0; i < 20; i++) {
			double newconstantCreationRate = constantCreationRate+(0.05*i);
			ScoreResult sr = GeneratorUtil.evaluatePerformance(0,sizeOfPredicateSpace, ruleNum, schemaviewSize, atomsInAntecedent, constantPool, newconstantCreationRate);				
			time.add(sr.time);
			newPred.add(sr.newPredicates);
			xAxisRuleSize.add((double)newconstantCreationRate);
		}
		
		// PRINT 
		Plot plt = Plot.create();
		plt.plot().add(xAxisRuleSize, time);
		//plt.plot().add(xAxisRuleSize, newPred);
		plt.xlabel("constantCreationRate");
		plt.ylabel("milliseconds");
		plt.title("Time");
		plt.legend();
		plt.show();
		
		Plot plt2 = Plot.create();
		//plt2.plot().add(xAxisRuleSize, time);
		plt2.plot().add(xAxisRuleSize, newPred);
		plt2.xlabel("constantCreationRate");
		plt2.ylabel("New Predicates");
		plt2.title("New Predicates");
		plt2.legend();
		plt2.show();
	}
}
