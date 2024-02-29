# Linear Genetic Programming (LGP) and Applications

This project implements a basic linear genetic programming algorithm and some of its advanced variants based on ECJ. The main feature of this project is that it decouples the algorithm implementations and problem applications, which facilitates the application to other domains. In this project, we apply LGP to dynamic job shop scheduling and symbolic regression, respectively.

This project is developed based on https://github.com/meiyi1986/GPJSS and the pseudo codes in [1]. To identify the difference between this project and the [existing one](https://github.com/meiyi1986/GPJSS), you would search `===zhixing` or `=== zhixing` in this package. The comments highlight the different parts.

[1] M. Brameier and W. Banzhaf, Linear Genetic Programming. New York, NY: Springer US, 2007.

### What is this repository for? ###

* This is a package for Linear Genetic Programming (LGP) and its applications, written by Zhixing Huang.
* The package is based on the Java ECJ package, which is available from https://cs.gmu.edu/~eclab/projects/ecj/.
* Version 1.0.0
* Java 11

### How do I get set up? ###

1. Download the source files in the `src/` folder and the dependencies in the `libraries/` folder.
2. Create a Java Project using `src/` and `libraries/`. The ECJ functions and the core code are located in `src/`.
3. Rebuild the whole project. Now you are ready to run.
4. Before starting, it is highly recommended to get yourself familiar with the ECJ package, especially the GP part. You can start from the four tutorials located at `src/ec/app/tutorialx` (x = 1, ..., 4). Turorial 4 is about GP for symbolic regression, which is very useful for understanding this project. A more thorough manual can be found in https://cs.gmu.edu/~eclab/projects/ecj/docs/manual/manual.pdf.

### Project structure ###

The main project is located in [`./src/zhixing/`](./src/zhixing). The other packages like `./src/ec/` and `./src/yimei/`, are based on the [existing one](https://github.com/meiyi1986/GPJSS). 

### Running experiments ###

Please refer to [djss package](./src/zhixing/djss) to get examples of running LGP, testing output programs, and drawing example programs.

### Who do I talk to? ###

* Email: zhixing.huang@ecs.vuw.ac.nz

