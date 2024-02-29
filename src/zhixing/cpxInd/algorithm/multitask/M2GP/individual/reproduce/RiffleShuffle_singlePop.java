package zhixing.cpxInd.algorithm.multitask.M2GP.individual.reproduce;

import java.util.ArrayList;
import java.util.Vector;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.multiobjective.MultiObjectiveFitness;
import zhixing.cpxInd.individual.LGPIndividual;

public class RiffleShuffle_singlePop extends zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.reproduce.LongPlateMate{

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
        int n = minChildProduction();
        if (n < min) n = min;
        if (n > max) n = max;

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already


        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        for(int q=start, parnt = 0;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
           
            sources[0].produce(1,1,0,subpopulation,parents,state,thread);
            sources[1].produce(1,1,1,subpopulation,parents,state,thread);
            
            // at this point, parents[] contains our two selected individuals
            LGPIndividual[] parnts = new LGPIndividual[2];
        	for(int ind = 0 ; ind < parnts.length; ind++){
        		parnts[ind] = (LGPIndividual) this.parents[ind]; 
        	}
        	
            q += this.produce(min, max, start, subpopulation, inds, state, thread, parnts);

            }
            
        return n;
        }
	
	
	//@Override
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread,
	        final LGPIndividual[] parents
	        ){
		// how many individuals should we make?
        int n = minChildProduction();
        if (n < min) n = min;
        if (n > max) n = max;

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        for(int q=start, parnt = 0;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
            
            // at this point, parents[] contains our two selected individuals
            
            // are our tree values valid?
            if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= parents[0].getTreesLength()))
                // uh oh
                state.output.fatal("LGP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= parents[1].getTreesLength()))
                // uh oh
                state.output.fatal("LGP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

            int t1=0, t2=0;
            LGPIndividual example = (LGPIndividual) state.population.subpops[subpopulation].individuals[0];
            LGPIndividual j1;
            j1 = ((LGPIndividual)example).lightClone();
        	t1 = parnt;
        	j1.getTreeStructs().clear();
        	
        	//int numTasks = cand_taskid.size();
        	
        	//LGPIndividual par[] = new LGPIndividual[numTasks];
        	ArrayList<ArrayList<Integer>> graphs = new ArrayList<ArrayList<Integer>>();     	
        	Integer tar[] = new Integer[1];
        	
        	//get the effective instruction from elites of different tasks.
        	for(int i = 0;i<parents.length;i++){  
        		
	        	ArrayList<Integer> cand = parents[i].getSubGraph(parents[i].getTreesLength()-1, new Integer[]{0}); //by default, R0 is the output register
	        	
        		graphs.add(cand);
        	}
        	
        	//merge into j1
        	//1. set the length of j1, by mean length + random detail
        	double sum_length = 0;
        	for(int i = 0;i<parents.length;i++){
        		sum_length += parents[i].getTreesLength();
        		//sum_length += par[i].getEffTreesLength(i);
        	}
        	int length = (int) (sum_length/parents.length+ (state.random[thread].nextInt(2*step_size + 1) - step_size));
        	
        	if(length > j1.getMaxNumTrees()){
        		length = j1.getMaxNumTrees();
        	}
        	if(length < j1.getMinNumTrees()){
        		length = j1.getMinNumTrees();
        	}
        	
        	
        	//2. randomly add the instruction from different tasks into j1
        	int try_task;
        	for(int l = 0;l<sum_length;l++){
        		int trial = parents.length;
        		try_task = state.random[thread].nextInt(parents.length);
        		while(graphs.get(try_task).size()==0 && trial >0){
        			try_task = (try_task + 1) % parents.length;
        			trial --;
        		}
        		if(trial <= 0){
        			break;
        		}
        		
        		if(graphs.get(try_task).size()>0){
        			j1.addTree(0, parents[try_task].getTreeStruct(graphs.get(try_task).get(0)));
            		
        			graphs.get(try_task).remove(0);
        		}
//	        		else if(j1.getTreesLength() == 0){
//	        			j1.rebuildIndividual(state, thread);
//	        		}
//	        		else
//	        			continue;
        	}
        	
        	if(j1.getTreesLength() == 0){
    			j1.rebuildIndividual(state, thread);
    		}
        	
        	//remove introns or instructions
        	//int taskid = state.random[thread].nextInt(numTasks);
    		if(j1.getTreesLength() > length){
    			int cnt = j1.getTreesLength() - length;
    			for(int k = 0; k<cnt; k++){
    				int res = state.random[thread].nextInt(j1.getTreesLength());
    				for(int x = 0;x < j1.getTreesLength();x++) {
		        		if(!j1.getTreeStruct(res).status){break;}
		        		res = (res + state.random[thread].nextInt(j1.getTreesLength())) % j1.getTreesLength();
		        	}
    				
    				j1.removeTree(res);
    			}
    		}
            
            if(microMutation != null) j1 = (LGPIndividual) microMutation.produce(subpopulation, j1, state, thread);
            
            //because different subpopulations may have different output registers, some just make them consistent           
//	          j1.setOutputRegisters(example.getOutputRegisters());
//	    		j1.fitness = (MultiObjectiveFitness)example.fitness.clone();
//	    		((LGPIndividual_MPMO)j1).skillFactor = ((LGPIndividual_MPMO)example).skillFactor;
            
            inds[q] = j1;
            q++;
            parnt ++;
            
            }
            
        return n;
	}
}
