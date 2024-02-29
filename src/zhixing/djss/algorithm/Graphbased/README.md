# Graph-based Seach Mechanisms in LGP for DJSS #

### Project Structure ###

**`Graphbased/individual`**

This sub-package implements the necessary classes fo graph-based LGP, including LGP individual and genetic operators. (see [graph-based LGP](../../../cpxInd/algorithm/Graphbased)).

### Running Examples ###

**Example 1 - Apply LGP+ALX in [1] to solve DJSS**

1. locate the [parameter file](./parameters/AMbasedLGP-JSS.params).
2. Run the main class with the arguments ``` -file [parameter file path] -p eval.problem.eval-model.sim-models.0.util-level=0.95 -p seed.0=4 -p eval.problem.eval-model.sim-seed=8 -p eval.problem.eval-model.sim-models.0.num-machines=10 ```. 

[1] Zhixing Huang, Yi Mei, Fangfang Zhang, Mengjie Zhang, and Wolfgang Banzhaf, “Bridging directed acyclic graphs to linear representations in linear genetic programming: a case study of dynamic scheduling,” Genetic Programming and Evolvable Machine, vol. 25, no. 1, p. 5, Jan. 2024, doi: 10.1007/s10710-023-09478-8.
