package zhixing.cpxInd.algorithm.Multiform.individual.reproduce;

import ec.EvolutionState;
import ec.Individual;
import ec.select.TournamentSelection;
import zhixing.djss.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator4DJSS;
import zhixing.djss.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO4DJSS;

public class Tournament_merge4Multiform extends TournamentSelection {
	//to select an elite individual from different GP populations simultaneously
	
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread) 
	        {
	        int n=INDS_PRODUCED;
	        if (n<min) n = min;
	        if (n>max) n = max;
	        
	        for(int q=0;q<n;q++)
	            inds[start+q] = produce_merge(subpopulation, state, thread);
	        return n;
	        }
	
	public Individual produce_merge(final int subpopulation,
	        final EvolutionState state,
	        final int thread)
    {
	        // pick size random individuals from a random sub-population, then pick the best.
		
			int num_subpop = state.population.subpops.length;
			int trial_subpop = state.random[thread].nextInt(num_subpop);
			int best = getRandomIndividual(0, trial_subpop, state, thread);
			
			
	        Individual[] oldinds = state.population.subpops[trial_subpop].individuals;
	        Individual bestind = oldinds[best];
	        
	        int s = getTournamentSizeToUse(state.random[thread]);
	                
	        if (pickWorst){
	        	
	        	for (int x=1;x<s;x++)
                {
//	        		trial_subpop = state.random[thread].nextInt(num_subpop);
//	        		oldinds = state.population.subpops[trial_subpop].individuals;
	                int j = getRandomIndividual(x, trial_subpop, state, thread);
	                if (!betterThan(oldinds[j], bestind, subpopulation, state, thread))  // j is at least as bad as best
	                {
	                	best = j;
	                	bestind = oldinds[j];
	                }
	                    
                }
	        }
	            
	        else{
	        	for (int x=1;x<s;x++)
                {
//	        		trial_subpop = state.random[thread].nextInt(num_subpop);
//	        		oldinds = state.population.subpops[trial_subpop].individuals;
	        		int j = getRandomIndividual(x, trial_subpop, state, thread);
	                if (betterThan(oldinds[j], bestind, subpopulation, state, thread))  // j is better than best
	                {
	                	best = j;
	                	bestind = oldinds[j];
	                }
                }
	        }
	            
	            
	        return bestind;
    }
}
