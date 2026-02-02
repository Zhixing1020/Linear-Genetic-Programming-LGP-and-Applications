package zhixing.cpxInd.individual.reproduce;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.koza.MutationPipeline;
import ec.util.Parameter;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.individual.LGPIndividual;

public class LGPSwapPipeline extends LGPMicroMutationPipeline {

	public static final String SWAP = "swap";
	public static final String P_EFFFLAG = "effective"; //swap effective instructions or not
	public static final String P_STEP = "step"; //the number of swapping instructions on one side
	
	public static final String P_MICROMUTBASE = "micro_base";
	
	public int stepSize;
	
	public boolean effflag;
	
	protected LGPMicroMutationPipeline microMutation;
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = LGPDefaults.base().push(SWAP);
	    
		stepSize = state.parameters.getInt(base.push(P_STEP), def.push(P_STEP), 1);
		 if (stepSize == 0)
	            state.output.fatal("LGPFreeMutation Pipeline has an invalid number of step size (it must be >= 1).",base.push(P_STEP),def.push(P_STEP));
		 
		 effflag = state.parameters.getBoolean(base.push(P_EFFFLAG), def.push(P_EFFFLAG),false);
		 
		 Parameter microbase = new Parameter(state.parameters.getString(base.push(P_MICROMUTBASE), def.push(P_MICROMUTBASE))) ;
		 microMutation = null;
		 if(!microbase.toString().equals("null")){
        	//microMutation = new LGPMicroMutationPipeline();
        	microMutation = (LGPMicroMutationPipeline)(state.parameters.getInstanceForParameter(
                    microbase, def.push(P_MICROMUTBASE), MutationPipeline.class));
   		 	microMutation.setup(state, microbase);
        }
	}
	
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread) 
	        {
	        // grab individuals from our source and stick 'em right into inds.
	        // we'll modify them from there
	        int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);

	        // should we bother?
	        if (!state.random[thread].nextBoolean(likelihood))
	            return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did


	        GPInitializer initializer = ((GPInitializer)state.initializer);
	        
	        // now let's mutate 'em
	        for(int q=start; q < n+start; q++)
	            {
	            LGPIndividual i = (LGPIndividual)inds[q];
	            
	            inds[q] = this.produce(subpopulation, i, state, thread);

	            }
	        return n;
	        }
	
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
//		        int t = getLegalMutateIndex(j, state, thread);
		        
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
		        int step = state.random[thread].nextInt(stepSize) + 1;
//		        int des = Math.min(t + step, j.getTreesLength()-1);
		        
//		        boolean eff1 = j.getTreeStruct(t).status;
//		        boolean eff2 = j.getTreeStruct(des).status;
		        for(int s = 0; s<step; s++) {
		        	int t = getLegalMutateIndex(j, state, thread);
		        	int des = Math.min(t+1, j.getTreesLength()-1);
		        	swapInstructions(j, t, des);
		        }
		        j.evaluated = false; 
		        
		        double new_effrate = ((double)j.getEffTreesLength()) / j.getTreesLength();
		        
		        if(Math.abs(new_effrate - old_effrate) <= 0.3  /*j.getTreeStruct(t).status && j.getTreeStruct(des).status == eff1*/) break;
	        }
	        
	        if(microMutation != null) j = (LGPIndividual) microMutation.produce(subpopulation, j, state, thread);
	        
	        return j;
	}
	
	protected int getLegalMutateIndex(LGPIndividual ind, EvolutionState state, int thread) {
		int res = state.random[thread].nextInt(ind.getTreesLength() - 1);
		
		if(effflag) {//guarantee the effectiveness of the selected instruction
			for(int x = 0;x<numTries;x++) {
        		if(ind.getTreeStruct(res).status) break;
        		res = state.random[thread].nextInt(ind.getTreesLength());
        	}
			
    	}
		
		return res;
	}
	
	protected void moveInstruction(LGPIndividual ind, int src, int des) {
		//src: the index of the to-be-removed instruction
		//des: the destination index of the instruction
		
		GPTreeStruct instr = ind.getTreeStruct(src);
		ind.removeTree(src);
		ind.addTree(des, instr);
	}
	
	protected void swapInstructions(LGPIndividual ind, int p1, int p2) {
		//p1 and p2 are the indexes of the to-be-swapped instructions
		GPTreeStruct instr1 = ind.getTreeStruct(p1);
		GPTreeStruct instr2 = ind.getTreeStruct(p2);
		
		ind.setTree(p1, instr2);
		ind.setTree(p2, instr1);
	}
}
