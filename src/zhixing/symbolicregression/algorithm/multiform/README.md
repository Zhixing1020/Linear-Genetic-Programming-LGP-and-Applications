# Apply Multi-representation GP (MRGP) to Symbolic Regression Problems #

This package implements the SR experiments in [1]

[1] Zhixing Huang et al. "Cross-Representation Genetic Programming: A Case Study on Tree-Based and Linear Representations". Evol. Comput. 2025, Dec 1;33(4):541-568. doi: 10.1162/evco.a.25.

### Project Structure ###

**`individual`**

This package implements the necessary classes of abstract MRGP.

**`ruleanalysis`**

This package implements the test procedure of MRGP. Specifically, `ruleanalysis` picks the better output rule between the two representations as the best rule of the current generation. 

**`statistics`**

This package implements the logging functions of MRGP for solving SR problems.

**`MRGPEvolutionStateSR_testSemDiv.java`**

This EvolutionState is for ECJ first round review, checking the semantic diversity over the top 20 individuals at the final generation.

### Running Examples ###

**Example 1 - MRGP for SR based on basic tree-based and linear representations**

1. locate the [parameter file](./parameters/MultiformGP-SR.params).
2. Run the main class `ec.Evolve` with the input arguments `-file [parameter file path] -p seed.0=4 -p SRproblem.location=[symbolic regression benchmark data path]\\`. The random seed is set to 4, and the `symbolic regression benchmark data path` stores [symbolic regression benchmarks](../../dataset).

**Example 2 - MRGP for SR based on TGP, LGP, and semantic LGP**

1. locate the [parameter file](./parameters/MRGP-LTSL-SR.params).
2. Run the main class `ec.Evolve` with the input arguments `-file [parameter file path] -p seed.0=4 -p SRproblem.location=[symbolic regression benchmark data path]\\`. The random seed is set to 4, and the `symbolic regression benchmark data path` stores [symbolic regression benchmarks](../../dataset).
