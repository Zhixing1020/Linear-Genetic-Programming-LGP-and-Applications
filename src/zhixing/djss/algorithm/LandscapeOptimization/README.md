# Apply Fitness Landscape Optimization to Enhance LGP for DJSS #

### Project Structure ###

**`EvolutionStateFLO4DJSS.java`**

This class defines the evolutionary framework of LGP with fitness landscape optimization.

**`FLReductionLGP/parameters`**

This package contains the parameters for applying fitness landscape compression to solve DJSS problems.

**`simpleLGP`**

* **`indexing`**

  - `SimpleLGPBuilder4DJSS.java` defines the necessary functions of building LGP instructions based on the primitives in DJSS problems.

* **`toy`**
  - This package is for fulfilling the experiments in Sections IV and V in [1].
  - `EvolutionStateFLO4ToyDJSS.java` defines the evolutionary framework in [1]. The evolutionary framework will output the whole fitness landscape for visualization.
  - `SubpopulationFLO_4LGP4ToyDJSS.java` defines the class of the GP population in [1]. This GP population has a fitness landscape member. However, because the fitness landscape has to enumerate all possible solutions, it is recommended to use the fitness landscape only in small-scale problems.
  - `parameters/simpleLGP-HalfJSS.params` is an example parameter file for analyzing the fitness landscape of toy DJSS problems. The main difference between `simpleLGP-HalfJSS.params` and `FLO-simpleLGP-HalfJSS.params` is the fifth genetic operator, although they both have an operator rate of 0.0. The `NeighborhoodSearch4LGP_plain` in `FLO-simpleLGP-HalfJSS.params` mainly performs a random move within the neighborhood. The `NeighborhoodSearch4LGP` in `simpleLGP-HalfJSS.params` mainly moves LGP genotype vectors towards superior ones.

[1] Z. Huang, Y. Mei, F. Zhang, M. Zhang, and W. Banzhaf, “Fitness Landscape Optimization Makes Stochastic Symbolic Search By Genetic Programming Easier,” IEEE Trans. Evol. Computat., pp. 1–1, 2025, doi: 10.1109/TEVC.2024.3525006.


### Running Example ###

**Example 1 - applying fitness landscape optimization and analysis to DJSS problems**

1. locate the [parameter file](./parameters/FLO-simpleLGP-JSS.params).
2. Run the main class with the arguments ``` -file [parameter file path] -p eval.problem.eval-model.sim-models.0.util-level=0.95 -p seed.0=4 -p eval.problem.eval-model.sim-seed=8 -p eval.problem.eval-model.sim-models.0.num-machines=10 ```. 

