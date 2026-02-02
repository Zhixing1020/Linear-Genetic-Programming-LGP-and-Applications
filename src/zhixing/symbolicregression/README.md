### Project Structure ###

* `algorithm` defines the detailed implementations of LGP algorithms.

* `individual` defines the basic LGP individual for solving SR problems.

* `dataset` the training and test data of SR benchmarks.

* `optimization` defines the class of SR problems by `GPSymbolicRegression.java`.

* `steadystate` defines the evolutionary framework of LGP with steady-state evolutionary framework and its corresponding components.

* `ruleanalysis` defines the test procedure of basic LGP on SR.

*  `statistics` defines the logging functions of basic LGP.

*  `util` defines the parsing functions for basic LGP (i.e., draw the DAG of LGP programs).


### Running Examples ###

**Example 1 - Applying basic LGP for symbolic regression SR**

This example applies LGP to solve SR with a generational EA framework. The settings of parameters follow the common settings for DJSS [1]

[1] Zhixing Huang et al. "Cross-Representation Genetic Programming: A Case Study on Tree-Based and Linear Representations". Evol. Comput. 2025, Dec 1;33(4):541-568. doi: 10.1162/evco.a.25.

1. Locate the [parameter file](./parameters/simpleLGP_SR.params).
2. Locate the [training and test data path](./dataset/).
3. Run the main class `ec.Evolve.java` with the arguments ```-file [parameter file path] -p seed.0=4 -p SRproblem.location=[training and test data path]\```. The benchmark and other parameters are specified in the parameter file.
4. Finally you will get two result files `out. stat` and `outtabular.stat` in the project home directory. 
The format of `outtabular.stat` is
"[Generation index] [Population mean fitness]\t[Best fitness per generation]\t[Best fitness so far]\t[Population mean absolutate program length]\t[Population mean effective program length]\t[Population average effective rate]\t[Absolute program length of the best individual]\t[Effective program length of the best individual]\t[Effective rate of the best individual]\t[running time so far in seconds]".

**Example 2 - Test the output programs of basic LGP for SR**

1. The main class is specified in `src/zhixing/symbolicregression/ruleanalysis/RuleTest4LGPSR.java`. Run the main class with the argument "[path of the result files]\ [training and test data path]\ [name of SR benchmark] [number of independent runs] [maximum number of registers] [maximum iteration times of loops] [number of objectives] [objective]".
For example,
```[path of the result files]\ [training and test data path]\ R1 1 16 100 1 RSE```

2. The example `out.stat` and `outtabular.stat` of basic LGP are attached in the home directory of the project.

3. A `test` folder is generated in your running directory. The outputs of the test procedure are recorded in `test` after finishing the test procedure.



