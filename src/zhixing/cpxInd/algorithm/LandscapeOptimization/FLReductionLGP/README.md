# Fitness Landscape Compression of GP-based symbolic search #

This package implements the paper [1]

[1] Z. Huang et al. "Fitness Landscape Compression for Genetic Programming-based Symbolic Search"


### Project Structure ###

**`SubpopulationFLR.java`**

This class extends `SubpopulationFLO` by defining new updating board manners.

**`indexing`**

* `IndexList4LGP_FLR` extends `simpleLGP.indexing.IndexList4LGP` into a dynamic list.

**`individual/reproduce`**

* `AnealingTournamentSelection` defines an anealing tournament selection, whose tournament size increases from a small szie to a large one during the evolution.
* `NeighborhoodSearch_noFLO` it is an ablation operator, moving geno vectors without the guidance of FLO
* `NeighborhoodSearchFast` is the actual operator in the experiment, implementing the origin-attracting move.
* `NeighborhoodSearchFLR4LGP` is a time-consuming implementation of the origin-attracting move.

### Running Examples ###

Refer to [applying fitness landscape optimization to DJSS](../../../../djss/algorithm/LandscapeOptimization) and [to symbolic regression](../../../../symbolicregression/algorithm/LandscapeOptimization) and replace the parameter files with [FLC for DJSS parameters](../../../../djss/algorithm/LandscapeOptimization/FLReductionLGP/parameters/FLR-LGP-JSS.params) and [FLC for SR parameters](../../../../symbolicregression/algorithm/LandscapeOptimization/FLReduction/parameters/FLR-LGP-SR.params).
