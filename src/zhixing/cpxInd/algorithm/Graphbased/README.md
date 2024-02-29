# Graph-based Search Mechanisms of Linear Genetic Programming #

This package implements the papers [1,2].

[1] Zhixing Huang, Yi Mei, Fangfang Zhang, Mengjie Zhang, and Wolfgang Banzhaf, “Bridging directed acyclic graphs to linear representations in linear genetic programming: a case study of dynamic scheduling,” Genetic Programming and Evolvable Machine, vol. 25, no. 1, p. 5, Jan. 2024, doi: 10.1007/s10710-023-09478-8.

[2] Zhixing Huang, Yi Mei, Fangfang Zhang, Mengjie Zhang, “Graph-based linear genetic programming: a case study of dynamic scheduling,” in Proceedings of the Genetic and Evolutionary Computation Conference, New York, NY, USA: ACM, Jul. 2022, pp. 955–963. doi: 10.1145/3512290.3528730.

### Project Structure ###
**`individual/reproduce`**
* `AMCrossover.java` defines the class of adjacency matrix-based crossover.
* `AMMicroMutation.java` defines the class of adjacency matrix-based micro mutation (i.e., AMX in [1]).
* `ATCrossover.java` defines the class of adjacency list-based crossover in [1].
* `FrequencyAllMacroMutation.java` defines the class of frequency-based macro mutation. The frequency is collected based on the elite individuals in the entire population.
* `FrequencyMacroMutation.java` defines the class of frequency-based macro mutation. The frequency is collected based on another competitive individual.
* `FrequencyMicroMutation.java` defines the class of frequency-based micro mutation (i.e., FX in [1]). The frequency is collected based on another competitive individual.
* `GraphCrossover.java` defines the class of the graph-based crossover in [2].
* `GraphMacroMutation.java` defines the class of the graph-based macro mutation, proposed in [2].
* `GraphMicroMutation.java` defines the class of the graph-based micro mutation, proposed in [2].

* `individual/GraphAttributes.java` defines a very high-level interface for the graph characteristics in LGP. Please check its type hierarchy if you are interested.
* `individual/LGPIndividual4Graph.java` implements some default methods of `individual/GraphAttributes.java`.

### Running Examples ###
Refer to [applying LGP+ALX to solve DJSS](../../../djss/algorithm/Graphbased/README.md) and [to symbolic regression](../../../symbolicregression/algorithm/graphbased).
