### Project Structure ###

* `algorithm` defines the detailed implementations of LGP algorithms

* `dynamicoptimization` defines the simulation in which the coefficients of the optimization objectives change over the simulation.

* `individual` defines the basic LGP individual for solving DJSS problems.

* `individualevaluation` defines the fitness evaluation functions of basic LGP for DJSS.

* `individualoptimization` defines the optimization problems of basic LGP for DJSS.

* `jobshop` defines the job shop implementation that accepts an LGP individual for problem solving.

*  `ruleanalysis` contains the classes for rule analysis, e.g. reading rules from the ECJ result file, testing rules, calculating the program length, depth, number of unique terminals, etc.

*  `simulation` contains the classes for discrete event simulation for dynamic job shop scheduling.

*  `statistics` defines the logging functions of basic LGP.

*  `util` defines the random number samplers of DJSS simulation and the parsing functions for basic LGP (i.e., draw the DAG of LGP programs).

### Running Examples ###

**Example 1 - Applying basic LGP for DJSS problems [1]**

1. locate the [parameter file](./parameters/simpleLGP-JSS.params).
2. Run the main class `ec.Evolve.java` with the arguments ``` -file [parameter file path] -p eval.problem.eval-model.sim-models.0.util-level=0.95 -p seed.0=4 -p eval.problem.eval-model.sim-seed=8 -p eval.problem.eval-model.sim-models.0.num-machines=10 ```. The `eval.problem.eval-model.sim-models.0.util-level=0.95` specifies the utilization level of the job shop is 0.95, `seed.0=4` specifies the random seed of LGP is 4, `eval.problem.eval-model.sim-seed=8` specifies the random seed of DJSS is 8, `eval.problem.eval-model.sim-models.0.num-machines=10` specifies the number of machines in the job shop is 10. Other parameter settings follow the parameter file.
By this means, most of the parameters are specified by 'simpleLGP-JSS.params', while some specific parameters are defined by "*-p xxx*", the same way as any ECJ applications.

3. Finally you will get two result files `out.stat` and `outtabular.stat` in the project home directory. 
The format of `outtabular.stat` is
"[Generation index] [Population mean fitness]\t[Best fitness per generation]\t[Best fitness so far]\t[Population mean absolutate program length]\t[Population mean effective program length]\t[Population average effective rate]\t[Absolute program length of the best individual]\t[Effective program length of the best individual]\t[Effective rate of the best individual]\t[running time so far in seconds]".
In the batch running model, you are required to define the name of the results files by yourselves. For example,
```-p stat.file=[home directory]/job.((ARRAY_TASK_ID)).out.stat -p stat.child.0.file=[home directory]/job.((ARRAY_TASK_ID)).outtabular.stat``` where ARRAY_TASK_ID is the ID of each run in the batch model.
The example result files `out.stat` and `outtabular.stat` are given in the repository home directory.

[1] Zhixing Huang, Yi Mei, Fangfang Zhang, and Mengjie Zhang, “A Further Investigation to Improve Linear Genetic Programming in Dynamic Job Shop Scheduling,” in Proceedings of IEEE Symposium Series on Computational Intelligence, 2022, pp. 496–503. doi: 10.1109/SSCI51031.2022.10022208.

**Example 2 - Test the output programs of basic LGP for DJSS**

1. The main class is specified in `src/zhixing/djss/ruleanalysis/RuleTest4LGP.java`. Run the main class with the argument "[path of the result files]\ [number of independent runs] dynamic-job-shop [DJSS scenario] [maximum number of registers] [maximum iteration times of loops] [number of objectives] [objective]".
For example,
```[path of the result files]\ 1 dynamic-job-shop missing-0.95-1.5 16 100 1 mean-flowtime```

2. The example `out.stat` and `outtabular.stat` of basic LGP are attached in the home directory of the project.

3. A `test` folder is generated in your running directory. The outputs of the test procedure are recorded in `test` after finishing the test procedure.

**Example 3 - Draw the directed acyclic graphs of LGP programs**

1. Install [Graphviz](https://graphviz.org/download/) beforehand.
2. Locate the output .stat files (i.e., `out.stat` and `outtabular.stat`).
3. The main class parses the output programs into .dot format. The main class is specified in `src/zhixing/djss/util/Parser.java`. Set the variables in the `main()` before running the code.
```
String path = "D:\\xxxx\\";   //directory of the results of all the compared methods.
String algo = "basicLGP";    //the folder name of a compared method.
String scenario = "mean-flowtime-0.95-1.5"; //the name of a specific scenario: [objective]-[utilization level]-[delay factor]


String sourcePath = path + algo + "\\" + scenario + "\\";

int numRuns = 50;   //number of independent runs

int numRegs = 8;    //number of registers

int maxIterations = 100;   //maximum iteration of looping operations.
```
4. Run the main class in `Parser.java` and get the .dot files of the output rules for each independent run.
5. Draw the DAGs by the following shell script:
```
root='[path of the algorithm results]\'

algo='[name of the compared algorithm]'

scenario=('[scenario name 1]' '[scenario name 2]')

runs=50

for ((ind=0;ind<=(${#scenario}-1);ind++))
do
	cd ${root}${algo}'\'${scenario[$ind]}'\'
	echo 'printing '${root}${algo}'\'${scenario[$ind]}'\'
	for((r=0;r<$runs;r++))
	do
		dot -Tpdf job.$r.bestrule.dot -o graph_${scenario[$ind]}.$r.pdf
	done
done
```
