# Apply Multifactorial Linear Genetic Programming (MFLGP) to DJSS #

### Project Structure ###

**`evaluator`** defines the fitness evaluation of MFLGP, including a necessary function of identifying skill factors.

**`EvolutionState`** defines the evolutionary framework of MFLGP.

* `MFEA_EvolutionState4DJSS.java` applies an environment selection method (i.e., first generate an offspring population, then concatenate the parent and offspring population, and finally select better individuals from the concatenated population to form the next generation).

* `MFGPS_EvolutionState4DJSS.java` extends `MFEA_EvolutionState4DJSS.java` by applying GP selection methods (i.e., a generational EA framework)

**`individual`** implements the abstract class of MFLGP individual for DJSS.

**`individualevaluation`** defines the fitness evaluation functions for DJSS.

**`individualoptimization`** defines the optimization problems for DJSS.

**`ruleanalysis`** defines the class of test procedure of MFLGP for DJSS.

**`parameters`**

  - `LGP-MFEA-JSS.params` defines the parameters of fully following the MFEA evolutionary framework to evolve LGP (e.g., rotating DJSS instances but comparing individual raw fitness, which is unreasonable).
  - `LGP-MFGPS-GC.params` defines the parameters of applying the GP selection method in MFEA and using graph-based crossover.
  - `LGP-MFGPS-JSS.params` defines the parameters of MFLGP in [1]. (It rotates DJSS instances and compares individual scalar ranks) ✔️
  - `LGP-MFGPS-MOCGC-JSS.params` extends `LGP-MFGPS-JSS.params` by using class graph crossover. Each "class" is for a "task", and each class has its own output register ("MO" in short).

### Running Examples ###

**Example 1 - Run MFLGP for DJSS**

1. Locate the [parameter file](./parameters/LGP-MFGPS-JSS.params).
2. Set the parameter file. Note that we have to set the optimization tasks and the simulation settings. Take the **scenario D** in [1] as an example:

   * the optimization tasks:
    ```
    eval.num_task = 2
    eval.problem.eval-model.objectives = 2
    eval.problem.eval-model.objectives.0 = max-flowtime
    eval.problem.eval-model.objectives.1 = max-tardiness
    ```

    * the simulation settings:
    ```
    eval.problem.eval-model.sim-models = 2
    eval.problem.eval-model.sim-models.0.util-level = 0.95
    eval.problem.eval-model.sim-models.0.output_register = 0
    eval.problem.eval-model.sim-models.1.util-level = 0.95
    eval.problem.eval-model.sim-models.1.output_register = 0
    ```


3. Run the main class `ec.Evolve` with running arguments ```-file [parameter file path]```.

[1] Zhixing Huang, Yi Mei, Fangfang Zhang, and Mengjie Zhang, “Multitask Linear Genetic Programming with Shared Individuals and its Application to Dynamic Job Shop Scheduling,” IEEE Transactions on Evolutionary Computation, pp. 1–15, 2023, doi: 10.1109/TEVC.2023.3263871.

**Example 2 - Test the output programs of MFLGP**

1. Locate the output .stat files (i.e., `out.stat` and `outtabular.stat`).
2. Locate the [parameter file](./parameters/LGP-MFGPS-JSS.params).
3. The main class of MFLGP test procedure is specified in `zhixing/djss/algorithm/multitask/MFEA/ruleanalysis/RuleTest4LGP_MFEA.java`. Run the main class with arguments:
```
[parameter file path] [path of the result files]\ [number of independent runs] dynamic-job-shop [the number of DJSS scenarios] [DJSS scenario1 & its output register, scenario2 & its output register, ...] [number of objectives] [objective1, objective2, ...] 
```
Take scenario D in [1] as an example. The input arguments of the test main class would be:
```
[parameter file path] [path of the result files]\ 1 dynamic-job-shop 2 missing-0.95-1.5 0 missing-0.95-1.5 0 2 max-flowtime max-tardiness
```
4. Check the outputs of the test procedure in the `test` folder in your running directory.
