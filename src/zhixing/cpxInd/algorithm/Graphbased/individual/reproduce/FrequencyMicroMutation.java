package zhixing.cpxInd.algorithm.Graphbased.individual.reproduce;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public abstract class FrequencyMicroMutation extends zhixing.cpxInd.individual.reproduce.LGPMicroMutationPipeline {
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
	}
	
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
		int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);
		
        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already



        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        for(int q=start, parnt = 0;q<n+start; q++)  // keep on going until we're filled up
            {
        	
        	LGPIndividual[] parnts = new LGPIndividual[2];
        	
            // grab two individuals from our sources
        	sources[0].produce(2,2,0,subpopulation,parnts,state,thread);
        	
            inds[q] = this.produce(min, max, start, subpopulation, state, thread, parnts);
            }
            
        return n;
        }
	
	abstract public LGPIndividual produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final EvolutionState state,
	        final int thread,
	        final LGPIndividual[] parents);
	
	
	protected int sampleBasedFrequency(double []frequency,  int start, int end, EvolutionState state, int thread) {
		//start: index of start visiting, end: index of stop visiting
		int res = state.random[thread].nextInt(end - start);
		
		double fre []=new double [end - start];
		for(int d = start;d<end;d++) {
			fre[d-start] = frequency[d];
		}
		
		double sum = 0;
		for(int i = 0;i<end - start;i++) {
			sum+=fre[i];
		}
		for(int i = 0;i<end - start;i++) {
			fre[i] /= sum;
		}
		
		double prob = state.random[thread].nextDouble();
		double tmp = 0;
		for(int f = 0;f<end - start;f++) {
			tmp += fre[f];
			if(tmp>=prob) {
				res = f;
				break;
			}
		}
		
		return res;
	}
}
