package zhixing.cpxInd.algorithm.semantic.library.select;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.semantic.SubpopulationSLGP;
import zhixing.cpxInd.algorithm.semantic.library.LibraryItem;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;

public class SLMergeTournamentSelection extends SLTournamentSelection{
	
	@Override
	public LibraryItem produce(EvolutionState state, int thread, SemanticLibrary semlib) {
		
		int ti = state.random[thread].nextInt(state.population.subpops.length);
		
		SemanticLibrary other = ((SubpopulationSLGP)state.population.subpops[ti]).semanticLib;
		
		
		int best = getRandomIndividual(state, thread, other);
		
		int s = size;
		
		LibraryItem bestItem = other.getItem(best);
		bestItem.fitness.determineFitness(state, thread, other, best);
		
		 if (pickSmall)
	            for (int x=1;x<s;x++)
	                {
	                int j = getRandomIndividual(state, thread, other);
	                LibraryItem tmp = other.getItem(j);
	                tmp.fitness.determineFitness(state, thread, other, j);
	                if ( bestItem.fitness.value >= tmp.fitness.value && state.random[thread].nextDouble() > Math.min(tmp.frequency / other.DISTRIBUTE, other.Pthresold))  // j is at least as bad as best
	                    bestItem = tmp;
	                	
	                }
	        else
	            for (int x=1;x<s;x++)
	                {
	                int j = getRandomIndividual(state, thread, other);
	                LibraryItem tmp = other.getItem(j);
	                tmp.fitness.determineFitness(state, thread, other, j);
	                if (bestItem.fitness.value < tmp.fitness.value && state.random[thread].nextDouble() <= Math.min(tmp.frequency / other.DISTRIBUTE, other.Pthresold))  // j is better than best
	                    bestItem = tmp;
	                }
	            
		
		return bestItem;
	}
}
