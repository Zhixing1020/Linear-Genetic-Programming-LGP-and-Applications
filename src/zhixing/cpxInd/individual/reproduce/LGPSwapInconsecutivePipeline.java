package zhixing.cpxInd.individual.reproduce;

import ec.BreedingPipeline;
import ec.EvolutionState;
import zhixing.cpxInd.individual.LGPIndividual;

public class LGPSwapInconsecutivePipeline extends LGPSwapPipeline {

	@Override
	public LGPIndividual produce(
			final int subpopulation,
	        final LGPIndividual ind,
	        final EvolutionState state,
	        final int thread) {
		
			LGPIndividual i = ind;
		
			LGPIndividual j;

	        if (sources[0] instanceof BreedingPipeline)
	            // it's already a copy, so just smash the tree in
	            {
	            j=i;
	            }
	        else // need to clone the individual
	            {
	            j = ((LGPIndividual)i).lightClone();
	            
	            // Fill in various tree information that didn't get filled in there
	            //j.renewTrees();
	            }
	        
	        if(j.getTreesLength() == 1) {
	        	
	        	if(microMutation != null) j = (LGPIndividual) microMutation.produce(subpopulation, j, state, thread);
	        	
	        	return j; //the swapping operation only process individuals with more than one instruction
	        }
	        
	        for(int x=0;x<numTries;x++) {
	        	
	        	if (sources[0] instanceof BreedingPipeline)
		            // it's already a copy, so just smash the tree in
		            {
		            j=i;
		            }
		        else // need to clone the individual
		            {
		            j = ((LGPIndividual)i).lightClone();
		            
		            // Fill in various tree information that didn't get filled in there
		            //j.renewTrees();
		            }
	        	
	        	double old_effrate = ((double)j.getEffTreesLength()) / j.getTreesLength();
	        	
	        	//get the swapping index
		        int t = getLegalMutateIndex(j, state, thread);
		        
//		        //get the size of swapping building blocks
//		        int size = state.random[thread].nextInt(stepSize) + 1; 
//		        size = Math.min(size, j.getTreesLength() - (t + 1));
//		        size = Math.min(size, (t+1));
//		        
//		        //get the starting and the ending points of the segment
//		        int start = t - size;
//		        int end = t + size;
//		        
//		        //swapping instructions
//		        for(int s = 1; s <= size; s++) {
//		        	moveInstruction(j, t+s, start+s);
//		        }
		        
		        //get the to-swap-with instruction
//		        int step = state.random[thread].nextInt(stepSize) + 1;
		        int des = state.random[thread].nextInt(j.getTreesLength());
		        
//		        boolean eff1 = j.getTreeStruct(t).status;
//		        boolean eff2 = j.getTreeStruct(des).status;
		        
		        swapInstructions(j, t, des);
		        j.evaluated = false; 
		        
		        double new_effrate = ((double)j.getEffTreesLength()) / j.getTreesLength();
		        
		        if(Math.abs(new_effrate - old_effrate) <= 0.3  /*j.getTreeStruct(t).status && j.getTreeStruct(des).status == eff1*/) break;
	        }
	        
	        if(microMutation != null) j = (LGPIndividual) microMutation.produce(subpopulation, j, state, thread);
	        
	        return j;
	}
}
