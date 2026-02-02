# Develop Tunable Primitives of LGP for Multi-target Symbolic Regression Problems #

### Project Structure ###

* `individual` defines the function and terminal entities (i.e., tunable primitives) and their related argument classes in `primitive` and defines the micro mutation operator for turning the tunable primitives in `produce`.

* `parameters` includes the parameter files for running LGP with turnable primitives for MTSR.

### Running Examples ###

**Example 1 - Applying LGP with turnable primitives to MTSR problems**

1. Locate the [parameter file](./parameters/entityLGP_SRBench.params).
2. Set the problem-specific individual parameters `num-output-register`, `output-register.0`, `to-wrap = true`.
3. Set the problem, fitness evaluation methods, Kfold index and total number, and targets of the problem. For example, for the _1203_BNG_pwLinear_ problem and _RSE_ fitness, we have:
```
eval.problem.dataname = 1203_BNG_pwLinear
eval.problem.fitness = RSE
eval.problem.Kfold_index = 0
eval.problem.Kfold_num = 50
eval.problem.target_num = 1
eval.problem.targets.0 = 0
```
4. Run the main class `ec.Evolve` with the input arguments `-file [parameter file path] -p seed.0=4 -p SRproblemMTar.location=[symbolic regression benchmark data path]\\`. The random seed is set to 4, and the `multi-target symbolic regression benchmark data path` stores the dataset.

[1] unpublished

**Example 2 - Test the output program of LGP with tunable primitives**

1. Locate the output .stat files (i.e., out.stat and outtabular.stat).
2. Locate the path of test data.
3. Run the main class `zhixing/symbreg_multitarget/ruleanalysis/RuleTest4LGPSRMT` with the arguments:
```
[output file path]\ [test data path]\ [problem name] [number of independent runs] [number of maximum registers] [number of maximum iterations] [number of objectives] [objective] [parameter file path] -p eval.problem.Kfold_index=[index of K fold CV]
```
For example, if we take _RSE_ as a performance metric to evaluate the test performance on _R1_ problem, we have:
```
[output file path]\ [test data path]\ R1 1 16 100 1 RSE [parameter file path] -p eval.problem.Kfold_index=0
```
