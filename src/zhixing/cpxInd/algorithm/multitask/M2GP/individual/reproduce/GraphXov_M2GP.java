package zhixing.cpxInd.algorithm.multitask.M2GP.individual.reproduce;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.reproduce.GraphCrossover;

public class GraphXov_M2GP extends GraphCrossover{
	@Override
    public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) 

        {
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

      //get the trial subpopulation for the origin-based offspring reservation
//        int trial = subpopulation;
//        while(state.population.subpops.length > 1 && trial == subpopulation){
//        	trial = state.random[thread].nextInt(state.population.subpops.length);
//        }
        int trial = getLegalTrialPopulation(state, thread, subpopulation);

        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        for(int q=start, parnt = 0;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
           
            sources[0].produce(1,1,0,subpopulation,parents,state,thread);
            sources[1].produce(1,1,1,trial,parents,state,thread);
            
            // at this point, parents[] contains our two selected individuals
            LGPIndividual[] parnts = new LGPIndividual[2];
        	for(int ind = 0 ; ind < parnts.length; ind++){
        		parnts[ind] = (LGPIndividual) this.parents[ind]; 
        	}
        	
            q += this.produce(min, max, start, subpopulation, inds, state, thread, parnts);

            }
            
        return n;
        }
	
	protected int getLegalTrialPopulation(EvolutionState state, int thread, int cursubpop){
		int trial = cursubpop;
        while(state.population.subpops.length > 1 && trial == cursubpop){
        	trial = state.random[thread].nextInt(state.population.subpops.length);
        }
        return trial;
	}
}
