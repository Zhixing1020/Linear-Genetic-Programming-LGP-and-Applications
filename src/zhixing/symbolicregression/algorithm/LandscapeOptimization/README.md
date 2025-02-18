# Apply Fitness Landscape Optimization to Enhance LGP for Symbolic Regression #

### Project Structure ###

**`EvolutionStateFLO4SR.java`**

This class defines the evolutionary framework of LGP with fitness landscape optimization.

**`simpleLGP`**

* **`indexing`**

  - `SimpleLGPBuilder4SR.java` defines the necessary functions of building LGP instructions based on the primitives in SR problems.

* **`toy`**
  - This package is for fulfilling the experiments in Sections IV and V in [1].
  - `SubpopulationFLO_ToySR.java` defines the class of the GP population in [1]. This GP population has a fitness landscape member. However, because the fitness landscape has to enumerate all possible solutions, it is recommended to use the fitness landscape only in small-scale problems.
  - `optimization/GPToySymbolicRegression.java` defines the class of the SR problems. We eliminate the normalization (i.e., treat the raw data as the normalized inputs) and remove the linear approximate of outputs (i.e., out_mean = 0. and out_std = 1.).
  - `parameters` includes the parameter files for analyzing the three toy problems. `simpleLGP-ToySR` is an example parameter file for analyzing the fitness landscape of the Toy problem. The main difference between `simpleLGP-xxx.params` and `FLO-simpleLGP-xxx.params` is the fifth genetic operator, although they both have an operator rate of 0.0. The `NeighborhoodSearch4LGP_plain` in `FLO-simpleLGP-HalfJSS.params` mainly performs a random move within the neighborhood. The `NeighborhoodSearch4LGP` in `simpleLGP-HalfJSS.params` mainly moves LGP genotype vectors towards superior ones.

[1] Z. Huang, Y. Mei, F. Zhang, M. Zhang, and W. Banzhaf, “Fitness Landscape Optimization Makes Stochastic Symbolic Search By Genetic Programming Easier,” IEEE Trans. Evol. Computat., pp. 1–1, 2025, doi: 10.1109/TEVC.2024.3525006.


### Running Example ###

**Example 1 - applying fitness landscape optimization and analysis to SR problems**

1. Locate the [parameter file](./parameters/FLO-simpleLGP-SR.params).
2. Run the main class `ec.Evolve` with the input arguments `-file [parameter file path] -p seed.0=4 -p SRproblem.location=[symbolic regression benchmark data path]\\`. The random seed is set to 4, and the `symbolic regression benchmark data path` stores [symbolic regression benchmarks](../../dataset).
