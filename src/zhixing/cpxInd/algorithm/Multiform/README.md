# Multi-representation Genetic Programming (MRGP) #

This package defines the MRGP classes of a case study on tree-based and linear representations [1].

[1] Zhixing Huang et al. "Cross-Representation Genetic Programming: A Case Study on Tree-Based and Linear Representations". Evol. Comput. 2025, Dec 1;33(4):541-568. doi: 10.1162/evco.a.25.

### Project Structure ###

**`individual`**

* `reproduce`

  - `ATCrossover4LGP.java` defines the class of adjacency list-based crossover for LGP (the receiver in the crossover must be LGP individual)
  - `ATCrossover4SLGP.java` defines the class of adjacency list-based crossover for semantic LGP (the receiver in the crossover must be SLGP individual)
  - `ATCrossover4TGP.java` defines the class of adjacency list-based crossover for TGP (the receiver in the crossover must be TGP individual)
  - `Tournament_merge4Multiform.java` defines the class of tournament selection from multiple sub-populations, each for a GP representation.

* `LGPIndividual4MForm.java` defines the class of the LGP individual in MRGP.

* `TGPIndividual4MForm.java` defines the class of the TGP individual in MRGP.

**`statistics`**

* `MultiFormStatistics.java` defines the logging functions of MRGP since there are two output individuals in MRGP, each for a GP representation.

### Running Examples ###

Apply MRGP [to DJSS problems](../../../djss/algorithm/Multiform) and [to symbolic regression](../../../symbolicregression/algorithm/multiform).
