### Project Structure ###

* `algorithm` defines the detailed implementations of LGP algorithms.

* `individual` defines the basic LGP individual for solving multi-target SR problems.
  - `primitive` defines the basic terminals (e.g., `InputFeature4SRMT.java`) and primitive interface (e.g., `EntityInterface4SRMT.java`)

* `optimization` defines the class of MTSR problems by `GPSymbolicRegressionMultiTarget.java`.

* `ruleanalysis` defines the test procedure of basic LGP on MTSR.

*  `util` defines the parsing functions for basic LGP (i.e., draw the DAG of LGP programs).


### Running Examples ###

**Example 1 - Applying basic LGP for multi-target symbolic regression SR**

This example applies LGP to solve MTSR with a generational EA framework. 

1. Locate the [parameter file](./parameters/simpleLGP_SRMT.params).
2. Locate the training and test data.
3. Run the main class `ec.Evolve.java` with the arguments ```-file [parameter file path] -p seed.0=4 -p SRproblemMTar.location=[training and test data path]\```. The benchmark and other parameters are specified in the parameter file.
4. Finally you will get two result files `out. stat` and `outtabular.stat` in the project home directory. 
The format of `outtabular.stat` is
"[Generation index] [Population mean fitness]\t[Best fitness per generation]\t[Best fitness so far]\t[Population mean absolutate program length]\t[PMTaropulation mean effective program length]\t[Population average effective rate]\t[Absolute program length of the best individual]\t[Effective program length of the best individual]\t[Effective rate of the best individual]\t[running time so far in seconds]".

Note: we need to define ```num-output-register```, the corresponding ```output-register.0``` etc, ```to-wrap```, ```target_num```, and ```targets.0``` etc in the ```.params``` file. 

**Example 2 - Test the output programs of basic LGP for MTSR**

1. The main class is specified in `src/zhixing/symbreg_multitarget/ruleanalysis/RuleTest4LGPSRMT.java`. Run the main class with the argument "[path of the result files]\ [training and test data path]\ [name of SR benchmark] [number of independent runs] [maximum number of registers] [maximum iteration times of loops] [number of objectives] [objective] [path of the parameter file] -p eval.problem.Kfold_index=[index of K-fold CV]".
For example,
```[path of the result files]\ [training and test data path]\ [dataset name]\ 1 80 100 1 RSE [path of the parameter file] -p eval.problem.Kfold_idnex=0```
We include the .params file here as the .params file specifies a lot of detailed settings.

2. The example `out.stat` and `outtabular.stat` of basic LGP are attached in the home directory of the project.

3. A `test` folder is generated in your running directory. The outputs of the test procedure are recorded in `test` after finishing the test procedure.




