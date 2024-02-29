# Semantic Linear Genetic Programming (SLGP) #

This package implements the semantic linear genetic programming proposed in [1].

[1] Zhixing Huang, Yi Mei, and Jinghui Zhong, “Semantic Linear Genetic Programming for Symbolic Regression,” IEEE Transactions on Cybernetics, pp. 1–14, 2022, doi: 10.1109/TCYB.2022.3181461.

### Project Sturcture ###

**`SubpopulationSLGP.java`**

This class defines the population of SLGP.

**`individual`**

* `individual` defines the SLGP individual and its corresponding genetic operators.

  - `SLGPIndividual.java` defines the class of SLGP individuals.
 
  - `GPTreeStructSemantic.java` defines the class of the instructions in SLGP individuals.
 
  - `reproduce/InstrWiseCrossover.java` defines an instruction-wise crossover operator for SLGP individuals.
 
  - `reproduce/MutateAndDivide.java` defines the proposed mutate-and-divide operator in [1].

**`library`**

This package defines the classes of the semantic library ("SL" in short) in SLGP.

* `fitness` defines the classes of the fitness of the library item.

  - `Frequency.java` defines the fitness based on the selection frequency of the library items.
  - `SLFitness.java` is the abstract class of the fitness of library items.

* `produce` defines the classes of the genetic operators to evolve the instructions in the semantic library. Specifically, `SLMultiBreedingPipeline.java` defines the breeding pipeline. Other classes are the genetic operators for new instruction production, including crossover, macro and micro mutation, and adjacency-list-based crossover. 

* `select` defines the classes of the selection methods in the evolution of library instructions. Specifically, `SLTournamentSelection.java` defines a basic tournament selection method. `SLMergeTournamentSelection.java` defines a tournament selection in case there are multiple SLGP sub-population and each has its own semantic library.

* `LibraryItem.java` defines the class of library items (i.e., an instruction, its output semantic vector set, a fitness, and other related variables).

* `SemanticLibrary.java` defines the class of the semantic library.

* `SemanticVector.java` defines the class of a semantic vector.

* `SLBreedingPipeline.java` defines the breeding pipeline for the semantic library.

* `SLBreedingSource.java` defines an abstract class of the breeding source for semantic libraries.

* `SVSet.java` defines the class of a semantic vector set.

### Running Examples ###

**Example 1 - applying SLGP to solve symbolic regression problems**

Refer to [applying SLGP to SR problems and its test procedure](../../../symbolicregression/algorithm/semantic).

