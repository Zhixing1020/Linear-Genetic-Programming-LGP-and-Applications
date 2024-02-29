# Apply Multitask Linear Genetic Programming with Shared Individuals (MLSI) to DJSS #

### Project Structure ###

**`EvolutionState`** defines the evolutionary framework of MLSI for DJSS.

**`individual`** implements the class of the MLSI individual and its corresponding genetic operators for DJSS.

* `LGPIndividual_MPMO4DJSS.java` defines the class of MLSI individual.

* `reproduce/LongPlateMate_2to1_4DJSS.java` defines the class of riffle shuffle in [1].

* `reproduce/TwoPointXov_SpecificIns4DJSS.java` defines the class of 2-point crossover in [1].

**`individualevaluation`** defines the fitness evaluation functions for DJSS.

**`individualoptimization`** defines the optimization problems for DJSS.

**`ruleanalysis`** defines the class of test procedure of MLSI for DJSS.

**`statistics`** defines the logging functions of MLSI.

**`parameters`**

  - `MPMO.params` defines the parameters of MLSI in [1]. ✔️
  - `OPMO.params` defines the parameters of only using one output register in MLSI. 
  - `MultiOutReg4AllTasks.params` defines the parameters of only evolve one population of shared indivdiuals.

### Running Examples ###

**Example 1 - Run MLSI for DJSS**

1. Locate the [parameter file](./parameters/MPMO.params).
2. Set the parameter file. Note that we have to set the number of sub-populations, the number of output registers, the number of objectives, the optimization tasks, and the utilization level of the tasks. Take the **scenario D** in [1] as an example:

   * the number of sub-populations, output registers, and objectives:
    ```
    pop.subpops = 3

    pop.subpop.0.species.ind.num-output-register = 2

    pop.subpop.0.species.fitness.num-objectives = 2
    ```

   * the optimization tasks:
    ```
    eval.num_task = 2
    eval.problem.eval-model.objectives = 2
    eval.problem.eval-model.objectives.0 = max-flowtime
    eval.problem.eval-model.objectives.1 = max-tardiness
    ```

    * the utilization level of the tasks:
    ```
    eval.problem.eval-model.sim-models = 2
    eval.problem.eval-model.sim-models.0.util-level = 0.95
    eval.problem.eval-model.sim-models.1.util-level = 0.95
    ```


3. Run the main class `ec.Evolve` with running arguments ```-file [parameter file path]```.

[1] Zhixing Huang, Yi Mei, Fangfang Zhang, and Mengjie Zhang, “Multitask Linear Genetic Programming with Shared Individuals and its Application to Dynamic Job Shop Scheduling,” IEEE Transactions on Evolutionary Computation, pp. 1–15, 2023, doi: 10.1109/TEVC.2023.3263871.

**Example 2 - Test the output programs of MLSI**

1. Locate the output .stat files (i.e., `out.stat` and `outtabular.stat`).
2. Locate the [parameter file](./parameters/MPMO.params).
3. The main class of MLSI test procedure is specified in `zhixing/djss/algorithm/multitask/MultipopMultioutreg/ruleanalysis/RuleTest4LGP_MPMO.java`. Run the main class with arguments:
```
[parameter file path] [path of the result files]\ [number of independent runs] dynamic-job-shop [the number of DJSS scenarios] [DJSS scenario1 & its output register, scenario2 & its output register, ...] [number of objectives] [objective1, objective2, ...] 
```
Take scenario D in [1] as an example. The input arguments of the test main class would be:
```
[parameter file path] [path of the result files]\ 1 dynamic-job-shop 2 missing-0.95-1.5 0 missing-0.95-1.5 1 2 max-flowtime max-tardiness
```
4. Check the outputs of the test procedure in the `test` folder in your running directory.

**Example 3 - Draw the directed acyclic graphs of the output program of MLSI**

1. Install [Graphviz](https://graphviz.org/download/) beforehand.
2. Locate the output .stat files (i.e., `out.stat` and `outtabular.stat`).
3. The main class of parsing the output programs into .dot format is specified in `zhixing/djss/algorithm/multitask/util/Parser.java`.
4. Set the path of the output .stat files by the variable "path", "algo", and "scenario".
5. Run the main class in `Parser.java` and get the .dot files of the output rules for each independent run.
6. Draw the DAGs by the following shell script:
```
root='[path of the algorithm results]\'

algo='[name of the compared algorithm]'

scenario=('[scenario name 1]' '[scenario name 2]')

runs=50

for ((ind=0;ind<=(${#scenario}-1);ind++))
do
	cd ${root}${algo}'\'${scenario[$ind]}'\'
	echo 'printing '${root}${algo}'\'${scenario[$ind]}'\'
	for((r=0;r<$runs;r++))
	do
		dot -Tpdf job.$r.bestrule.dot -o graph_${scenario[$ind]}.$r.pdf
	done
done
```
