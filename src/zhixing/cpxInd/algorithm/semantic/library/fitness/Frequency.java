package zhixing.cpxInd.algorithm.semantic.library.fitness;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;

public class Frequency extends SLFitness{

	@Override
	public double determineFitness(EvolutionState state, int thread, SemanticLibrary semlib, int index) {
			
		value = semlib.getItem(index).frequency;
		
		return value;
	}

	@Override
	public Object clone() {
		Frequency obj = new Frequency();
		return obj;
	}

}
