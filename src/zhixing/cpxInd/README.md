# Project Structure #

* `cpxInd/algorithm` defines the advanced LGP algorithms. The algorithms in `cpxInd/algorithm` include several common packages: `EvolutionState`, `individual`, and `statistics`. `EvolutionState` defines the evolutionary framework of the advanced LGP. For example, the `Grammar/EvolutionState/EvolutionState4Grammar` defines an interface of necessary functions in the evolutionary framework of grammar-guided LGP. `individual` defines the advanced LGP individuals, their primitives (`individual/primitives`), and genetic operators (`individual/reproduce`). `statistics` defines the logging operations for advanced algorithms.

  - `Grammar` [grammar-guided linear genetic programming](./cpxInd/algorithm/Grammar).
  - `Graphbased` [graph-based search mechanisms of linear genetic programming](./cpxInd/algorithm/Graphbased)
  - `LandscapeOptimization` [fitness landscape optimization for stochastic symbolic search](./cpxInd/algorithm/LandscapeOptimization)
  - `Multiform` [Multi-Representation GP: a case study of tree-based and linear representation](./cpxInd/algorithm/Multiform)
  - `multitask` [multitask linear genetic programming](./cpxInd/algorithm/multitask)
  - `semantic` [semantic linear genetic programming](./cpxInd/algorithm/semantic)

* `cpxInd/individual` contains the core classes of LGP individual (mainly in `individual/LGPIndividual.java`).
  - `individual/primitive` contains the core classes of the newly introduced primitives in LGP (e.g., Read- or WriteRegister, FlowOperators).
  - `individual/reproduce` contains the core classes of the basic genetic operators of LGP (`LGP2PointCrossoverPipeline.java` for linear crossover, `LGPMacroMutationPipeline.java` for macro mutation (i.e., effmut1~3 in [1]), and `LGPMicroMutationPipeline.java` for micro mutation).
  - `CpxGPIndividual.java`: an abstract class for defining GP representation variants. It specifies the common functions.
  - `CpxGPInterface4Problem.java`: an interface for executing a GP variant on a specific problem, including register initialization and making Graphviz format.
  - `FlowController.java`: an abstract class for defining flow control operations.
  - `GPTreeStruct.java`: the class of LGP instructions.
  - `TGPInterface4Problem.java`: implementing the common functions of tree-based GP representation for applying in a problem.
  
* `cpxInd/fitnesslandscape` contains the classes of the fitness landscape of linear genetic programming (mainly in `fitnesslandscape/LGPFitnessLandscape.java`).
  - `fitnesslandscape/neighbor` contains the classes of various neighborhood structures on the landscape.
  - `fitnesslandscape/objective` contains the classes of hardness metrics over the landscape. Note that to save the computation time, these metrics perform "importance sampling" when there are more than 300 possible solutions in the search space.
  - `fitnesslandscape/LGPFitnessLandscape.java` defines the class of fitness landscape of linear genetic programming.

* `statistics` defines the logging functions of linear genetic programming.

* `species` defines the basic functions of linear genetic programming species.
