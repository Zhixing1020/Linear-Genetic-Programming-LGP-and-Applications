# Apply Semantic Linear Genetic Programming (SLGP) to Symbolic Regression Problems #

### Project Structure ###

* `individual` defines the class of the SLGP individual for SR problems.

* `library` defines the class of the semantic library for SR problems.

* `SLGPEvolutionState4SR.java` defines the evolutionary framework of SLGP.

### Running Examples ###

**Example 1 - Applying SLGP to SR problems**

1. Locate the [parameter file](./parameters/SLGP_SR.params).
2. Set the problem and fitness evaluation methods. For example, for the _R1_ problem and _RSE_ fitness, we have:
```
SRproblem.dataname = R1
SRproblem.fitness = RSE
```
3. Set the stopping criteria. In [1], we apply 10<sup>7</sup> NNE as a stopping criterion. However, it might be inconvenient to compare with other GP methods. Thus, we add the number of generations as one of the stopping criteria. Specifically, `generation_stop` and `NNE_stop` decide whether we consider generation or NNE (i.e., false for not considering, and true for considering). `generations` decides the maximum number of generations. NNE is set as 10<sup>7</sup> by default. Once one of the stopping criteria is satisfied, the evolution is terminated. Suppose we limit SLGP to evolve only 200 generations, we have:
```
generations = 200
generation_stop = true

NNE_stop = true
```
4. Run the main class `ec.Evolve` with the input arguments `-file [parameter file path] -p seed.0=4 -p SRproblem.location=[symbolic regression benchmark data path]\\`. The random seed is set to 4, and the `symbolic regression benchmark data path` stores [symbolic regression benchmarks](../../dataset).

[1] Zhixing Huang, Yi Mei, and Jinghui Zhong, “Semantic Linear Genetic Programming for Symbolic Regression,” IEEE Transactions on Cybernetics, pp. 1–14, 2022, doi: 10.1109/TCYB.2022.3181461.

**Example 2 - Test the output program of SLGP**

1. Locate the output .stat files (i.e., out.stat and outtabular.stat).
2. Locate the path of [test data](../../dataset).
3. Run the main class `zhixing/symbolicregression/ruleanalysis/RuleTest4LGPSR` with the arguments:
```
[output file path]\ [test data path]\ [problem name] [number of independent runs] [number of maximum registers] [number of maximum iterations] [number of objectives] [objective].
```
For example, if we take _RSE_ as a performance metric to evaluate the test performance on _R1_ problem, we have:
```
[output file path]\ [test data path]\ R1 1 16 100 1 RSE
```
