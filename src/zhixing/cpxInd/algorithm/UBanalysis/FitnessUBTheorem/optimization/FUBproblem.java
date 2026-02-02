package zhixing.cpxInd.algorithm.UBanalysis.FitnessUBTheorem.optimization;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.semantic.library.SemanticVector;

public interface FUBproblem {
	//define the evaluation function for semantics
	
	public double evaluate(final EvolutionState state, 
	        final int subpopulation,
	        final int threadnum,
	        final SemanticVector sv
	        );
}
