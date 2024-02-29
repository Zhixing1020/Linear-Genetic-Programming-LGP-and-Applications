package zhixing.cpxInd.algorithm.semantic.library.fitness;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;

public abstract class SLFitness {
	//used for the tournament selection in the semantic library
	
	public double value = 0;
	
	public abstract double determineFitness(EvolutionState state, int thread, SemanticLibrary semlib, int index) ;
	
	public abstract Object clone();
	
	public void setup(final EvolutionState state, final Parameter base)  {}
}
