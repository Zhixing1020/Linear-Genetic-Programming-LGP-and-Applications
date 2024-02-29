# Grammar-guided Linear Genetic Programming #

This package implements the papers [1,2].

[1] Zhixing Huang, Yi Mei, Fangfang Zhang, and Mengjie Zhang, "Toward Evolving Dispatching Rules With Flow Control Operations By Grammar-Guided Linear Genetic Programming," IEEE Transactions on Evolutionary Computation, pp. 1–15, 2024, doi: 10.1109/TEVC.2024.3353207.

[2] Zhixing Huang, Yi Mei, Fangfang Zhang, and Mengjie Zhang, “Grammar-guided Linear Genetic Programming for Dynamic Job Shop Scheduling,” in Proceedings of the Genetic and Evolutionary Computation Conference, New York, NY, USA: ACM, Jul. 2023, pp. 1137–1145. doi: 10.1145/3583131.3590394.

### Project Structure ###
**`grammarrules`** 
* `AttributeSet.java` defines the class of manipulating available primitives (i.e., functions and terminals in LGP).
*  `DerivationRule.java` defines the class of derivating a module into sub-modules.
*  `Grammarrules.java` defines the class of managing the entire grammar, including attributes, modules, and their derivation rules.
*  `ModuleConstraint.java` defines the class of modules.
*  `ModuleConstraintLib.java` defines the class of managing modules. 

**`individual`**
* `reproduce` defines the core classes of grammar-guided genetic operators. `GrammarCrossoverMultiPoints.java`, `GrammarMacroMutation.java`, and `GrammarMicroMutation.java` are the basic grammar-guided LGP genetic operators in [1,2].
* `DerivationTree.java` defines the class of derivation tree
* `DTNode.java` defines the class of the tree nodes in a derivation tree
* `GPTreeStructGrammar.java` defines the LGP instructions in grammar-guided LGP.
* `InstructionBuilder.java` defines the class of generating instructions based on grammar rules.
* `LGPIndividual4Grammar` defines the LGP individual in grammar-guided LGP.

### Running Examples ###
* refer to [Applying grammar-guided LGP to DJSS](../../../djss/algorithm/Grammar/README.md).
