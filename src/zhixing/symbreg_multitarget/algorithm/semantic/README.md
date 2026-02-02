# Apply Semantic Linear Genetic Programming (SLGP) to Multi-target Symbolic Regression Problems #

### Project Structure ###

* `individual` defines the class of the SLGP individual for MTSR problems.

* `library` defines the class of the semantic library for MTSR problems.

* `parameters` includes the parameter files of SLGP for MTSR.

### Running Examples ###

**Example 1 - Applying SLGP to MTSR problems**

1. Locate the [parameter file](./parameters/SLGP_SRMT-200g.params). This .params file mainly borrows the settings of the [parameter file](../../../symbolicregression/algorithm/semantic/parameters/SLGP_SR-200g.params).
2. The main differences between these two .params files are 1) found by searching `SRMT` in the .params file and 2) `eval.problem` and its related settings.

**Example 2 - Test the output program of SLGP**

1. Locate the output .stat files (i.e., out.stat and outtabular.stat).
2. Locate the path of test data.
3. Run the main class `zhixing/symbreg_multitarget/ruleanalysis/RuleTest4LGPSRMT` with the arguments:
```
[output file path]\ [test data path]\ [problem name] [number of independent runs] [number of maximum registers] [number of maximum iterations] [number of objectives] [objective] [parameter file path] -p eval.problem.Kfold_index=[index of K-fold CV]
```
For example, if we take _RSE_ as a performance metric to evaluate the test performance on _R1_ problem, we have:
```
[output file path]\ [test data path]\ R1 1 16 100 1 RSE [parameter file path] -p eval.problem.Kfold_index=0
```
