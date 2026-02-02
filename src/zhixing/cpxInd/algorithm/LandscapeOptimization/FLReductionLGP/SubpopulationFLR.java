package zhixing.cpxInd.algorithm.LandscapeOptimization.FLReductionLGP;

import java.util.Collections;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.LandscapeOptimization.SubpopulationFLO;
import zhixing.cpxInd.individual.CpxGPIndividual;

public class SubpopulationFLR extends SubpopulationFLO {
	
	@Override
	public void updateBoard(EvolutionState state, int thread) {
//		fullBoard.loadOutAnchors((int) (1. - (double)state.generation / state.numGenerations));
		fullBoard.clear();
		for(int i = 0; i<this.individuals.length; i++) {
			fullBoard.addIndividual((CpxGPIndividual) this.individuals[i]); //no clone, to improve memory efficiency
		}
		Collections.sort(fullBoard);
//		fullBoard.reloadAnchors(state, thread);

	}
}
