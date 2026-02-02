# Upper Bound Analysis for Linear Genetic Programming #

This package implements the experiment studies of the upper bound analysis on linear genetic programming [1].

[1] Zhixing Huang et al. "Bridging Fitness With Search Spaces By Fitness Supremums: A Theoretical Study on LGP", doi: https://doi.org/10.48550/arXiv.2505.21991.

### Project Structure ###

**FitnessUBTheorem**

- `FitnessUB.java` samples programs based on initialization and genetic operators (these results are not shown in the current manuscript)
- `SampleItem.java`: a data structure for `FitnessUB.java`.

**individual.reproduce**

- `MacroMutation_fixstep.java` is a simple operator to produce offspring by adding instructions, with optional effectiveness check.
- `MacroMutation_replace.java` is a simple operator to produce offspring by replacing instructions, with an optional effectiveness check.
- `PureMacroMutation4FUB.java` is a simple operator to produce offspring by replacing instructions, without effectiveness check.

**MovingRate**

- `MovingRate_Comp.java` compares the moving rate under different settings (e.g., program size (m), step size (u), and editing distance (d))

**theorymodel**

- `add1remove1`: the theoretical model in [1] is based on the genetic operator (adding or removing a random instruction)
- `CombinationNum.java` is a utilization class

