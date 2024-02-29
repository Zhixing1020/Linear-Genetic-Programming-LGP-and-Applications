package zhixing.cpxInd.algorithm.multitask.MFEA.individual.reproduce;

import ec.BreedingSource;
import ec.EvolutionState;
import ec.Individual;
import ec.SelectionMethod;
import ec.breed.MultiBreedingPipeline;
import ec.gp.koza.CrossoverPipeline;
import ec.select.RandomSelection;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator;
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA;

public class MFEABreedingPipeline extends MultiBreedingPipeline {
	public static int[] selectIndex = new int[2]; //the index of selected parents
    public final static String P_RMP = "rmp";
    public static double rmp;
	
	protected SelectionMethod selection;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); //this one is setup the things in MultiBreedingPipeline

//        selection = new TournamentSelection();

        selection = new RandomSelection();
        selection.setup(state, base); //selection is a new defined instance of TournamentSelection, if we want to use the parameter in TournamentSelection, we need setup selection.
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
	        //BreedingSource s;
	        //= sources[BreedingSource.pickRandom(
	         //       sources,state.random[thread].nextDouble())];
	        int total;
	        
	        LGPIndividual_MFEA[] parents = new LGPIndividual_MFEA[2];
	        
	        for(int ind = 0 ; ind < parents.length; ind++){
	        	int tarSF = state.random[0].nextInt(((MFEA_Evaluator)state.evaluator).getNumTasks());
	        	int trial=10;
	        	do{
	        		selectIndex[ind]  = selection.produce(subpopulation, state, thread);
	        	}while(((LGPIndividual_MFEA) state.population.subpops[subpopulation].individuals[selectIndex[ind]]).skillFactor != tarSF 
	        			&& trial-- >0);
	            
	            parents[ind] =  (LGPIndividual_MFEA) state.population.subpops[subpopulation].individuals[selectIndex[ind]];
	        }
	        
	        double rnd = state.random[0].nextDouble();
	        rmp = state.parameters.getDoubleWithDefault(new Parameter(P_RMP), null, 1);
	        
	        if(parents[0].skillFactor == parents[1].skillFactor || rnd < rmp){
	            //crossover, return two offspring
	            //mc.produce();
	            //i++;
	        	int flag = 0;

	        	if(sources.length ==3 || state.random[thread].nextDouble()<sources[2].probability){
	        		flag = 2;
	        	}
	        	else if (state.random[thread].nextDouble()<sources[2].probability+sources[3].probability){
	        		flag = 3;
	        	}
//	        	LGP2PointXoverPipeline_AssoMate ss = (LGP2PointXoverPipeline_AssoMate) sources[2];
//	        	ClassGraphCrossover ss = (ClassGraphCrossover) sources[3];
	        	CrossoverPipeline ss = (CrossoverPipeline) sources[flag];
	            if (generateMax) //false
	            {
	                if (maxGeneratable==0)
	                    maxGeneratable = maxChildProduction();
	                int n = maxGeneratable;
	                if (n < min) n = min;
	                if (n > max) n = max;

	                total = ss.produce(n,n,start,subpopulation,inds,state,thread);
	            }
	            else
	            {
	            	if(flag == 2){
	            		total = ((LGP2PointXoverPipeline_AssoMate) ss).produce(
		                        min,max,start,subpopulation,inds,state,thread,parents);
	            	}
	                
	            	else /*if (flag == 3)*/{
	            		total = ((ClassGraphCrossover) ss).produce(
		                        min,max,start,subpopulation,inds,state,thread,parents);
	            	}
	            }
	        }
	        else{
	            //mutation for each parent, return two offspring
	            //j++;
	        	LGPMaMicroMutationPipeline ss = (LGPMaMicroMutationPipeline) sources[1];
	            if (generateMax) //false
	            {
	                if (maxGeneratable==0)
	                    maxGeneratable = maxChildProduction();
	                int n = maxGeneratable;
	                if (n < min) n = min;
	                if (n > max) n = max;

	                total = ss.produce(
	                        n,n,start,subpopulation,inds,state,thread);
	            }
	            else
	            {
	                total =  ss.produce(
	                        min,max,start,subpopulation,inds,state,thread, parents);
	            }
	        }
	            
	        // clone if necessary
//	        if (s instanceof SelectionMethod)
//	            for(int q=start; q < total+start; q++)
//	                inds[q] = (Individual)(inds[q].clone());
//	        
	        return total;
	        }
	
	
}
