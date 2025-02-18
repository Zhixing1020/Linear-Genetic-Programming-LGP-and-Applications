# Fitness Landscape Optimization for GP search #

This package implements the paper [1]

[1] Z. Huang, Y. Mei, F. Zhang, M. Zhang, and W. Banzhaf, “Fitness Landscape Optimization Makes Stochastic Symbolic Search By Genetic Programming Easier,” IEEE Trans. Evol. Computat., pp. 1–1, 2025, doi: 10.1109/TEVC.2024.3525006.


### Project Structure ###

**`SubpopulationFLO.java`**

This class defines additional members and methods in the GP population of the fitness landscape optimization.

**`indexing`**

* `AnchorItem` defines the class of items in B<sub>lose</sub> in [1]. It has a unique function of setting up fitness.
* `Board` defines the class of a list to record the competitive or less-competitive individuals.
* `BoardItem` defines the class of the items in `Board`.
* `Direction` defines the class specifying the modification direction of the genotype vector.
* `GenoVector` defines the class of genotype vector, including how it moves along the direction.
* `Index` defines the abstract class of a "symbol" in the symbolic search problems.
* `IndexList` defines the abstract class of the list of `Index`.
* `IndexSymbolBuilder` defines the abstract class of constructing (enumerating) the symbols.

**`objectives`**

* `Ojbective4FLO.java` defines the abstract class of objective functions for the fitness landscape. 
* `Distance.java` defines the objective function of the distance between good solutions.
* `IntervalDistance.java` defines the objective function of the distance between good and poor solutions.
* `Normalization.java` defines the objective function of the consistency between domain knowledge and symbol indexes.

The following objectives are not covered in [1] since they are likely helpless for effectiveness.
* `Causality.java` defines the objective function modeling the causality of the neighborhood landscape.
* `LosingDistance.java` defines the objective function of the distance between poor solutions.
* `Navigation.java` defines the objective function of the moving direction. A high level of navigation implies that a solution can move from poor fitness to good fitness without changing the moving direction too much.
* `norm2Q.java` defines the normalization objective of two genotype vectors.

**`reproduce`**

* `NeighborhoodSearch.java` defines an abstract class of moving genotype against the optimized landscape.

**`simpleLGP`**

This package implements the fitness landscape optimization algorithm for LGP. 

**`simpleLGP/individual/reproduce`**

* `IndividualRoaming.java` randomly moves the LGP individual without the neighborhood concept.
* `NeighborhoodSearch4LGP.java` moves the LGP individual towards another good individual within the neighborhood against the optimized fitness landscape.
* `NeighborhoodSearch4LGP_plain.java` moves the LGP individual randomly against the optimized landscape. It is just a compared method.

### Running Examples ###

Refer to [applying fitness landscape optimization to DJSS](../../../djss/algorithm/LandscapeOptimization) and [to symbolic regression](../../../symbolicregression/algorithm/LandscapeOptimization).
