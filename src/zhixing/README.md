# README #

### Project Structure ###

The `src/zhixing` package contains the following sub-packages: 
- `cpxInd`
- `optimization`
- `djss`
- `symbolicregression`
- `symbreg_multitarget`
- `symbolic_classification`

Specifically, `cpxInd` defines the algorithms and their necessary functions (or interface), including basic LGP, and its advanced variants. It decouples the algorithm implementation from problems. `optimization` defines an interface for supervised learning problems.  `djss` defines the application of LGP and its variants to dynamic job shop scheduling problems. `symbolicregression` mainly implements the algorithms for symbolic regression. `symbreg_multitarget` defines the applications of LGP for multi-target symbolic regression. `symbolic_classification` implements the algorithms for symbolic classification.

For a more detailed introduction to the packages, please check the README in the sub-packages.
