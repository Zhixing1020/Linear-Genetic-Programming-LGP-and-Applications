# Applying Grammar-guided LGP to Solve DJSS problems #

### Project Structure ###

**`EvolutionState`** defines the evolutionary framework of applying grammar-guided LGP for DJSS.

**`grammarrules/ruleslibrary`** stores the grammar rule files.

**`individual`** implements the necessary classes of the abstract grammar-guided LGP individual.

**`ruleanalysis`** defines the test procedure of grammar-guided LGP for DJSS. The test procedure includes the file operations on the IF features.

### Running Examples ###
**Example 1 - grammar-guided LGP for basic DJSS**

1. locate the [grammar rule file](./grammarrules/ruleslibrary/DJSS_rules-gecco.txt).
2. locate the [parameter file](./parameters/GrammarLGP-JSS-gecco.params).
3. run the main class `ec.Evolve.java` with arguments ``` -file [parameter file path] -p eval.problem.eval-model.sim-models.0.util-level=0.95 -p seed.0=4 -p eval.problem.eval-model.sim-seed=8 -p eval.problem.eval-model.sim-models.0.num-machines=10 -p pop.subpop.0.species.ind.rulepath=[grammar rule file path] ```. The arguments indicate that the job shop simulation has a utilization level of 0.95, the random seed of GP is 4, the random seed of DJSS simulation is 8, the job shop has 10 machines, and LGP follows the grammar rules defined in `[grammar rule file path]`.

**Example 2 - grammar-guided LGP with IF operations for solving energy-aware DJSS**

1. locate the [grammar rule file](./grammarrules/ruleslibrary/DJSS_rules_IF.txt).
2. locate the [parameter file](./parameters/GrammarLGP-JSS.params).
3. note that to run energy-aware DJSS, we have to ensure the following settings in the parameter file (others keep the same):
```
eval.problem = zhixing.djss.individualoptimization.IndividualDynamicOptimizationProblem

eval.problem.eval-model = zhixing.djss.individualevaluation.DOEvaluationModel4Ind

terminals-from = relative4Grammar

gp.fs.0.size = 12
```
if we run the DJSS considering 1) performance (i.e., tardiness and flowtime), 2) energy, and 3) response cost, we have to add
```
-p eval.problem.eval-model.sim-models.0.dynamic-mode=advance
```
in the running arguments or the shell script. The `zhixing.jss.cpxInd.individualoptimization.IndividualDynamicOptimizationProblem` uses energy-aware DJSS by default.

4. run the main class  with arguments ``` -file [parameter file path] -p eval.problem.eval-model.sim-models.0.util-level=0.95 -p seed.0=4 -p eval.problem.eval-model.sim-seed=8 -p eval.problem.eval-model.sim-models.0.num-machines=10 -p pop.subpop.0.species.ind.rulepath=[grammar rule file path] ```
  
