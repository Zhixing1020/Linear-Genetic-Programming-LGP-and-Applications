package zhixing.cpxInd.algorithm.LandscapeOptimization.FLReductionLGP.individual.reproduce;

import ec.EvolutionState;
import ec.Individual;

public class AnealingTournamentSelection extends ec.select.TournamentSelection{

	@Override
	public int produce(final int subpopulation,
	        final EvolutionState state,
	        final int thread)
	        {
	        // pick size random individuals, then pick the best.
	        Individual[] oldinds = state.population.subpops[subpopulation].individuals;
	        int best = getRandomIndividual(0, subpopulation, state, thread);
	        
	        int max_s = getTournamentSizeToUse(state.random[thread]);
	        
	        int s = (int) Math.min(max_s, Math.ceil(Math.sqrt((double)(state.generation) / state.numGenerations )*max_s)+1);
	                
	        if (pickWorst)
	            for (int x=1;x<s;x++)
	                {
	                int j = getRandomIndividual(x, subpopulation, state, thread);
	                if (!betterThan(oldinds[j], oldinds[best], subpopulation, state, thread))  // j is at least as bad as best
	                    best = j;
	                
	                }
	        else
	            for (int x=1;x<s;x++)
	                {
	                int j = getRandomIndividual(x, subpopulation, state, thread);
	                if (betterThan(oldinds[j], oldinds[best], subpopulation, state, thread))  // j is better than best
	                    best = j;
	                }
	            
	        return best;
	        }
}
