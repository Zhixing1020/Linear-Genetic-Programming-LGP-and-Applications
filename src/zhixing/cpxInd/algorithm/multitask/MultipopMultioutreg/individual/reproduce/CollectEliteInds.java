package zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.reproduce;

import java.util.Arrays;

import ec.EvolutionState;
import ec.Individual;
import ec.SelectionMethod;
import ec.multiobjective.MultiObjectiveFitness;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO;

public class CollectEliteInds extends ec.breed.ReproductionPipeline {
	public int produce(
	        final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread) 
	{
		int trial = getLegalTrialPopulation(state, thread, subpopulation);

	    // grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,start,trial,inds,state,thread);
                
        if (mustClone || sources[0] instanceof SelectionMethod)
            for(int q=start; q < n+start; q++){
            	
            	//because different subpopulations may have different output registers, some just make them consistent
            	LGPIndividual_MPMO j1 = (LGPIndividual_MPMO) state.population.subpops[subpopulation].individuals[0];
            	LGPIndividual j2 = ((LGPIndividual_MPMO)inds[q]).lightClone();
            	if(!Arrays.equals(j1.getOutputRegisters(), j2.getOutputRegisters())){
            		j2.setOutputRegisters(j1.getOutputRegisters());
            		j2.fitness = (MultiObjectiveFitness)j1.fitness.clone();
            		((LGPIndividual_MPMO)j2).skillFactor = ((LGPIndividual_MPMO)j1).skillFactor;
            	}
            	j2.evaluated = false;
            	inds[q] = j2;
            }
                
        return n;
	}
	
	protected int getLegalTrialPopulation(EvolutionState state, int thread, int cursubpop){
		int trial = cursubpop;
		if(cursubpop==0 && state.population.subpops.length > 1){
			trial = 1 + state.random[thread].nextInt(state.population.subpops.length - 1);
		}
		else if(cursubpop > 0){
			trial = cursubpop;
		}
		
//		int trial = state.random[thread].nextInt(state.population.subpops.length);
		return trial;
		
//        return 0;
	}
}
