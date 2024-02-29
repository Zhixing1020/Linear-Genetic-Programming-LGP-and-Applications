package zhixing.cpxInd.algorithm.multitask.MFEA.individual.reproduce;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import ec.select.TournamentSelection;
import zhixing.cpxInd.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator;
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA;

public class TournamentSelection_ScalarRank extends TournamentSelection{
	/** Returns true if *first* is a better (fitter, whatever) individual than *second*. */
	@Override
    public boolean betterThan(Individual first, Individual second, int subpopulation, EvolutionState state, int thread)
        {
        return ((LGPIndividual_MFEA)first).scalarRank < ((LGPIndividual_MFEA)second).scalarRank;
        }
    
	@Override
	public int produce(final int subpopulation,
	        final EvolutionState state,
	        final int thread)
	        {
		
			//collect the individuals from a certain skill factor
			ArrayList<Integer> indexes = new ArrayList<>();
			int tarSF = state.random[0].nextInt(((MFEA_Evaluator)state.evaluator).getNumTasks());
			int trials = 20;
			while(indexes.size() == 0 && trials >0){
				//randomly select a skill factor
				tarSF = (tarSF + 1) % ((MFEA_Evaluator)state.evaluator).getNumTasks();
				
				for(int i = 0;i<state.population.subpops[subpopulation].individuals.length;i++){
					if(((LGPIndividual_MFEA)state.population.subpops[subpopulation].individuals[i]).skillFactor == tarSF){
						indexes.add(i);
					}
				}
				trials --;
			}
			if(trials==0){
				System.err.print("inconsistent skillfactor in TournamentSelection_ScalarRank\n");
				System.exit(1);
			}
			
	        // pick tournament-size random individuals, then pick the best.
			Individual[] oldinds = state.population.subpops[subpopulation].individuals;
	        int best = getRandomIndex(0, indexes, state, thread);
	        
	        int s = getTournamentSizeToUse(state.random[thread]);
	                
	        if (pickWorst)
	            for (int x=1;x<s;x++)
	                {
	                int j = getRandomIndex(x, indexes, state, thread);
	                if (!betterThan(oldinds[j], oldinds[best], subpopulation, state, thread))  // j is at least as bad as best
	                    best = j;
	                }
	        else
	            for (int x=1;x<s;x++)
	                {
	                int j = getRandomIndex(x, indexes, state, thread);
	                if (betterThan(oldinds[j], oldinds[best], subpopulation, state, thread))  // j is better than best
	                    best = j;
	                }
	            
	        return best;
	        }
	
	protected int getRandomIndex(int number, List pop, EvolutionState state, int thread)
    {
    return (int) pop.get(state.random[thread].nextInt(pop.size())) ;
    }
}
