# Apply Multitask Multipopulation Linear Genetic Programming (M<sup>2</sup>LGP) to DJSS #

### Project Structure ###

**`individualevaluation`** defines the fitness evaluation functions for DJSS.

**`individualoptimization`** defines the optimization problems for DJSS.

**`ruleanalysis`** defines the class of test procedure of M<sup>2</sup>LGP for DJSS.

**`statistics`** defines the logging functions of M<sup>2</sup>LGP.

**`parameters`**

  - `LGP-M2GP-GC-JSS.params` defines the parameters of applying graph-based crossover in M<sup>2</sup>LGP.
  - `LGP-M2GP-JSS.params` defines the parameters of M<sup>2</sup>LGP in [1]. ✔️
  - `LGP-M2GP-JSS2.params` defines the parameters of applying riffle shuffle in M<sup>2</sup>LGP.
  - `LGP-MP-JSS.params` defines the parameters of multi-population LGP for multitask optimization. No knowledge transfer among sub-populations. It is the baseline "MPLGP" in [1]. ✔️

### Running Examples ###

**Example 1 - Run M<sup>2</sup>LGP for DJSS**

1. Locate the [parameter file](./parameters/LGP-M2GP-JSS.params).
2. Set the parameter file. Note that we have to set the number of sub-populations, the optimization tasks, and the utilization level of the tasks. Take the **scenario D** in [1] as an example:

   * the number of sub-populations:
    ```
    pop.subpops = 2
    ```

   * the optimization tasks:
    ```
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

**Example 2 - Test the output programs of M<sup>2</sup>LGP**

1. Locate the output .stat files (i.e., `out.stat` and `outtabular.stat`).
2. The main class of M<sup>2</sup>LGP test procedure is specified in `zhixing/djss/algorithm/multitask/M2GP/ruleanalysis/RuleTest4LGP_M2GP.java`. Run the main class with arguments:
```
[path of the result files]\ [number of independent runs] [maximum number of registers] [maximum iteration times of loops] dynamic-job-shop [the number of DJSS scenarios] [DJSS scenario1, scenario2, ...] [number of objectives] [objective1, objective2, ...] 
```
Take scenario D in [1] as an example. The input arguments of the test main class would be:
```
[path of the result files]\ 1 16 100 dynamic-job-shop 2 missing-0.95-1.5 missing-0.95-1.5 2 max-flowtime max-tardiness
```
3. Check the outputs of the test procedure in the `test` folder in your running directory.
