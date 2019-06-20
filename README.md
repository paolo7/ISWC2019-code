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
