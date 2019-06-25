## SHACL Constraints with Inference Rules

This repository contains four Java Eclipse projects:

#### 1. CoreLogic

This project contains the core classes to compute simple schema consequence and rule applicability using the SCORE and CRITICAL approaches. The two approaches are defined in the `SchemaExpansionBySPARQLquery.java` Java class.

#### 3. ExperimentsDataGenerator

This project contains `Existential_Validator.java` the core class to compute the existential preserving schema consequence (reusing using the SCORE approache). 
It also contains classes to translate SHACL descriptions into Triplestore schemas and vice versa (`Translator_to_SHACL.java` and `Translator_to_Graph_Pattern.java`). 

#### 4. ExperimentsDataGenerator

This project contains the classes needed to generate random schema and rulesets. These will be stored (and reused in later runs) under the `chasebench\GPPG\` subfolder.

#### 5. Experiments

This project contains the runnable main class (in the `runBenchmark` Java class) that will start two experiments:

1. The experiment to compare the computation time of the SCORE and CRITICAL approaches.
2. The experiment to evaluate the computation time of the simple vs. existential preserving schema consequence.

These experiments will output python code to plot the results (requires the `matplotlib` library).

## Installation instructions

To run this project you will need Java and Eclipse IDE (it was tested on Java 1.8 and Eclipse Oxigen). To visualise the results of the experiments you will need Python and `matplotlib` library (tested with Python 2.7.12).

1. Load this project in Eclipse. You can do this by downloading this repository, and then, in Eclipse, selecting "File -> Open project from file system", and chosing the directory where you have downloaded this project. The Eclipse Package Explorer should now contain the 4 required projects.

2. If Eclipse is detecting a cycle error "A cycle was detected in the build path of project..." set circular dependencies from errors to warnings here "Windows -> Preferences -> Java-> Compiler -> Building -> Circular Dependencies". The project should now be ready to run.

3. Run the main method in `runBenchmark.java` in the Experiments sub-project. If necessary, set up a new Java run configuration in Eclipse to run it. This method will run two experiments.

The first experiment is run with method `experiment_1_critical_and_score_scalability_comparision()` and the second with method `experiment_2_different_scalability_with_existentials()`. If you want to run only one of them, you can comment out the other one in the main method of `runBenchmark`. 

* When an experiment is run, it will first perform a warmup run to reduce the performance effect of a cold-start. You can disergard the outputs of the warmup run. 
* Depending on the machine you are running it on, these experiments might take any time between a few minutes to several hours. See below for information on how to configure the experiments if you want to try different variations or speed up the experiments.

The main output of the experiment will be two lists `C` and `T` for each algorithm, where `C[x]` is the value of the core parameter studied by the experiment (i.e. number of triples in the schema graph for experiment 1, and number of existential constraints for experiment 2) in each configuration, and `T[x]` is the average compuation time with configuration `C[x]`.
To make the results easy to visualise, these lists for each algorithm will be outputted as matplotlib plot scripts. At the end of each experiment, a full script will be produced. You can copy this code and run it as a Python script to visualise the results. This code will be displayed in the console output, and also saved in file `Experiments\resultOutputs.txt`.


#### Interim results

Since the experiments might run for a long time, this implementation prints plot traces after testing each configuration of the experiment. An example interim plot is displayed below:


```
[17]  Critical: 14772.7 SCORE: 8.8
 plt.plot([5.0, 9.0, 13.0, 17.0],[0.0076, 0.0078, 0.0068, 0.0088], linestyle='-', marker=',', color='C0', label=r'\texttt{score}')
 plt.plot([5.0, 9.0, 13.0, 17.0],[0.11120000000000001, 0.6698, 4.8002, 14.7727], linestyle='-', marker='^', color='C1', label=r'\texttt{critical}')
```

The number in the square brakets is the value of the core parameter studied by the experiment in the configuration that was just completed. It is followed by the average computation time (in milliseconds) of the two algorithms in that configuration. The following two lines are the plot traces for the results collected so far. If you want to visualise the results collected so far, copy the two plot traces in the spot indicated in the code below, and run it in Python.


```
from mpl_toolkits.mplot3d import Axes3D
from matplotlib.pyplot import figure
from matplotlib import rcParams
figure(num=None, figsize=(6.4, 2.7), dpi=300, facecolor='w', edgecolor='k')
import matplotlib.pyplot as plt
import numpy as np
rc('font', **{'family':'serif', 'serif':['Computer Modern Roman'], 'monospace': ['Computer Modern Typewriter'], 'size': ['larger']})
plt.rc('text', usetex=True)
params = {'axes.labelsize': 13,'axes.titlesize':13, 'legend.fontsize': 13, 'xtick.labelsize': 13, 'ytick.labelsize': 13 }
rcParams.update(params)

*** INSERT INTERIM RESULTS HERE ***

plt.ylabel('Seconds')
plt.xlabel('core parameter of interim results')
plt.legend()
plt.tight_layout()
plt.show()
```

#### Experiment configuration

You can easily fine tune the experiments by changing the parameters inside methods `experiment_1_critical_and_score_scalability_comparision()` and `experiment_2_different_scalability_with_existentials()`. The parameters you can change are the following:

Parameters of the starting configuration:

* `atomsInAntecedent`: how many triples appear in the antecedent of each inference rule
* `constantCreationRate`: whenever the random schema and inference rule generator needs to choose whether to create a variable or a constant, it will choose a constant with this rate.
* `ruleNum`: the number of inference rules
* `initialSchemaviewSize`: the number of triple patterns in the schema graph
* `existentials`: the number of existential validity rules in the schema
* `millisecondTimeout`: if the total computation time for one algorithm in a configuration exceeds `millisecondTimeout*repetitions`, then subsequent configurations will not test that algorithm.

Each experiment tests two algorithms across multiple configurations, each time averaging over a number of repetitions.

* `repetitions`: the number of times each configuration will be tested. The output in the plots will be the average of this many repetitions. You can reduce this number to increase performance, although it would introduce more noise. 
* `stepIncrease` and `configurations`: each experiment starts by testing the starting configuration, as defined by the previous parameters. It will then modify the core parameter studied by the experiment (`initialSchemaviewSize` for experiment 1, and `existentials` for experiment 2) by incrementing it of the amount specified in `stepIncrease` and test this new configuration. This process will continue until the core parameter studied by the experiment exceeds `configurations`. For example, in experiment 2, if `existentials=0`, `stepIncrease=10`, and `configurations=100`, then the experiment will test 11 different configurations of `existentials`: `[0,10,20,30,40,50,60,70,80,90,100]`.

#### Inspecting schemas and rules.

Sets of inference rules are obtained from rule files, these randomly generated rule files are created on demand whenever experiments are run and reused if the same configuration of rules is reused. After running an experiment for the first time, rule files can be found in folder `Experiment\chasebench\GPPG\`. Schemas are generated using these rulesets and are not saved to file.

To inspect schemas, rules, and the corresponding effect of computing a schema expansion, you can do the following:

Place a breakpoint in the method `evaluatePerformanceIteration` (line 184 of `GPPGbenchmark\src\benchmarking\GeneratorUtil.java`) and run the experiment in debug mode. Then, once execution stops on this breakpoint, create the following new watch expressions:
* `rules` this object contains the list of rules used for the schema expansion
* `schema.pretty_print_string()` this is a string representation of (1) the schema graph, (2) its existential constraints, if any, and (3) the SHACL representation of this graph. For simplicity, the no-literal set is not modelled as a separate object. Instead, each variable will be displayed with a `+` or a `-` symbol next to it. Variables with a `-` symbol are included in the no-literal set, while the ones with a `+` are not.


To view the new schema graph and no-literal set after a basic schema expansion, place a breakpoint at line 189 (at line `int newschemaSize = newPredicates.size();`) and watch expression `newPredicates`.
To view the set of `retained_constraints`, place a breakpoint at line 195 (at line `long time2 = new Date().getTime();`) and watch expression `retained_constraints`.

Note 1: make sure you wait until the warmup run is finished before activating the breakpoint, or you might not be visualising the schemas and rulesets that you expect.

Note 2: if you want to inspect existential constraints, run the second experiment in debug mode, and place the breakpoint to view the schema after the first configuration is complete (as the first configuration has no existential constraints).

Note 3: debug interruptions to inspect schema and rules do not stop the timer that calculates algorithm performance. So if you interrupt the execution, the final experiment results will not be correct.