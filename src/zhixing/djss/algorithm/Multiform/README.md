# Apply Multi-representation GP (MRGP) to DJSS Problems #

This package implements the DJSS experiments in [1]

[1] Zhixing Huang et al. "Cross-Representation Genetic Programming: A Case Study on Tree-Based and Linear Representations". Evol. Comput. 2025, Dec 1;33(4):541-568. doi: 10.1162/evco.a.25.

### Project Structure ###

**`individual`**

This package implements the necessary classes of abstract MRGP.

**`ruleanalysis`**

This package implements the test procedure of MRGP. Specifically, `ruleanalysis` picks the better output rule between the two representations as the best rule of the current generation. 

**`util`**

This package implements the functions that parse LGP and TGP programs into GraphViz representations.

### Running Examples ###

**Example 1 - MRGP for DJSS based on basic tree-based and linear representations**

1. locate the [parameter file](./parameters/MultiformGP-JSS.params).
2. Run the main class with the arguments ```-file [parameter file] -p eval.problem.eval-model.sim-models.0.util-level=0.95 -p seed.0=4 -p eval.problem.eval-model.sim-seed=8 -p eval.problem.eval-model.sim-models.0.num-machines=10```.

**Example 2 - MRGP for DJSS based on TGP, LGP, and Grammar-guided LGP**

1. locate the [parameter file](./parameters/MultiformGP-G2LGP-JSS.params).
2. locate the [grammar rule file path](../Grammar/grammarrules/ruleslibrary/DJSS_rules-try.txt).
3. Run the main class with the arguments ``` -file [parameter file path] -p eval.problem.eval-model.sim-models.0.util-level=0.95 -p seed.0=4 -p eval.problem.eval-model.sim-seed=8 -p eval.problem.eval-model.sim-models.0.num-machines=10 -p pop.subpop.2.species.ind.rulepath=[grammar rule file path]```. 
