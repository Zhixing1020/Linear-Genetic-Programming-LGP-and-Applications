# Multitask LGP #

This package implements the experiments in [1]

[1] Zhixing Huang, Yi Mei, Fangfang Zhang, and Mengjie Zhang, “Multitask Linear Genetic Programming with Shared Individuals and its Application to Dynamic Job Shop Scheduling,” IEEE Transactions on Evolutionary Computation, pp. 1–15, 2023, doi: 10.1109/TEVC.2023.3263871.

### Project Structure ###

(✔️ indicates the genetic operators used in the experiments in [1]. Other operators are for unpublished experiments.)

**`M2GP`** implements the compared method M<sup>2</sup>LGP based on [M2GP[2]](https://ieeexplore.ieee.org/document/9382963). 

* `M2GP.individual.reproduce`

  - `BetterParentRes_LGP_2PXover.java` implements a 2-point LGP crossover with a better-parent reservation strategy. It is an extension of the proposed better-parent reservation strategy to M<sup>2</sup>LGP.
  - `GraphXov_M2GP.java` implements a graph-based crossover within the multitask context.
  - `OriginBased_LGP_2PXoverPipeline.java` implements a 2-point LGP crossover with an origin-based offspring reservation strategy [2]. ✔️
  - `RiffleShuffle_singlePop.java` implements a riffle shuffle operator for single-output LGP individuals within the same population for M<sup>2</sup>LGP.
  - `RiffleShuffle_twoPop.java` implements a riffle shuffle operator for single-output LGP individuals between multiple populations for M<sup>2</sup>LGP.

**`MFEA`** implements the compared method MFLGP [1].

* `breeder` defines the class of breeder in GP. The breeder defines the procedure of generating offspring.

  - `MFEA_Breeder.java` defines the breeder based on a multi-factorial EA framework (implementing a necessary function for concatenating the parent and trial population). 
  - `MFGPS_Breeder.java` defines the breeder based on a multi-factorial EA framework but applying GP parent selection methods for offspring production. (✔️ in MFLGP)

* `evaluator` defines the abstract class of evaluating the population of MFLGP.

* `individual` defines the MFLGP individuals and the corresponding genetic operators.

  - `LGPIndividual_MFEA.java` defines the class of LGP individual in MFLGP. 

* `individual/reproduce`

  - `ClassGraphCrossover.java` defines the class graph crossover. The class graph crossover is designed based on "C. Downey, M. Zhang, and W. N. Browne, “New crossover operators in linear genetic programming for multiclass object classification,” in Proceedings of the Annual Genetic and Evolutionary Computation Conference, 2010, pp. 885–892. doi: 10.1145/1830483.1830644."
  - `GraphXov_AssoMate.java` defines a graph-based crossover with assortative mating. The assortative mating is designed based on "A. Gupta, Y. S. Ong, and L. Feng, “Multifactorial Evolution: Toward Evolutionary Multitasking,” IEEE Transactions on Evolutionary Computation, vol. 20, no. 3, pp. 343–357, 2016, doi: 10.1109/TEVC.2015.2458037.". The graph-based crossover is designed based on "Z. Huang, Y. Mei, F. Zhang, and M. Zhang, “Graph-based linear genetic programming: a case study of dynamic scheduling,” in Proceedings of the Genetic and Evolutionary Computation Conference, New York, NY, USA: ACM, Jul. 2022, pp. 955–963. doi: 10.1145/3512290.3528730. " The difference between _class graph crossover_ and _graph-based crossover_ is that there is a crossover point in _graph-based crossover_. The _graph-based crossover_ constructs the sub-graph from the crossover point to the beginning of the program (i.e., top-down way in DAG). Contrarily, _class graph crossover_ constructs the sub-graph from the end of the program every time.
  - `LGP2PointXoverPipeline_AssoMate.java` defines the 2-point LGP crossover with assortative mating. ✔️
  - `LGPMaMicroMutationPipeline.java` defines the class of combining macro and micro mutation into one operation. This design is consistent with the design of mutation in "Multifactorial Evolution: Toward Evolutionary Multitasking". ✔️
  - `MFEABreedingPipeline.java` defines the breeding pipeline of MFEA. The breeding pipeline is different from basic LGP as it varies with the skill factors of selected parents.
  - `MFGPSBreedingPipeline.java` extends `MFEABreedingPipeline.java` by selecting parents with `TournamentSelection_ScalarRank.java`.
  - `TournamentSelection_ScalarRank.java` defines a variant of tournament selection that selects individuals based on their scalar ranks rather than raw fitness. ✔️

* `statistics` defines the classes of logging functions of MFLGP.

**`MultipopMultioutreg`** implements the multitask LGP with shared individuals (MLSI) in [1].

* `breeder` defines the class of breeding which contains necessary functions for preparing the offspring production.

* `fitness` defines the class of the fitness in MLSI.

* `individual` defines the MLSI individuals and its corresponding genetic operators.

  - `individual/reproduce` defines the classes of genetic operators. In this package, the suffix "_SharedIns" denotes that the genetic operator might select parents across different tasks. The suffix "_SpecificIns" denotes that the genetic operator only select parents from its own task.
 
    - `CollectEliteInds.java` defines the class of getting legal competitive individuals across MLSI sub-populations.
   
    - `GraphXov_SharedIns.java` defines the class of performing graph-based crossover. This graph-based crossover swaps the shared sub-graphs in individuals for multiple tasks.
   
    - `GraphXov_SpecificIns.java` defines the class of performing graph-based crossover. This crossover focuses on one task each time.
   
    - `LGPMacroMutation_Specific.java` and `LGPMicroMutation_Specific.java` are the macro and micro mutation for a specific task each time.✔️
   
    - `LongPlateMate.java` defines an abstract class of riffle shuffle on multiple tasks. The `LongPlateMate_2to1.java` extends the `LongPlateMate.java` by only allowing MLSI to mate two individuals each time (✔️). The two individuals are from different tasks. 
   
    - `TwoPointXov_SharedIns.java` defines an abstract class of 2-point crossover on multiple tasks.
   
    - `TwoPointXov_SpecificIns.java` defines the class of performing 2-point crossover. This crossover focuses on one task each time. The better-parent reservation in [1] is implemented in this class. ✔️
   
    - `TournamentSel_SharedIns.java` defines the class of tournament selection. The selection criterion is the linear combination of the best and second best scalar rank.
   
    - `TournamentSel_SpecificIns.java` defines the class of tournament selection. The selection criterion is the scalar rank of the target task.✔️
 
  - `individual/LGPIndividual_MPMO.java` defines the MLSI individual class.

* `statistics` defines the logging functions of MLSI.



### Running Examples ###

**Example 1 - Running M<sup>2</sup>LGP for DJSS**

Refer to [applying M<sup>2</sup>LGP to DJSS and testing the results](../../../djss/algorithm/multitask/M2GP).

**Example 2 - Running MFLGP for DJSS**

Refer to [applying MFLGP to DJSS and testing the results](../../../djss/algorithm/multitask/MFEA).

**Example 3 - Running MLSI for DJSS**

Refer to [applying MLSI to DJSS and testing the results](../../../djss/algorithm/multitask/MultipopMultioutreg).
