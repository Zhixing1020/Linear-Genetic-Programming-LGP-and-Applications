package zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.reproduce;

import java.util.ArrayList;
import java.util.Vector;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.multiobjective.MultiObjectiveFitness;
import zhixing.cpxInd.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator;
import zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO;
import zhixing.cpxInd.individual.LGPIndividual;

public abstract class LongPlateMate_2to1 extends LongPlateMate {

    abstract public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread);
	
//	@Override
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread,
	        final LGPIndividual[] parents,
	        final int taskid,
	        final Vector<Integer> cand_taskid){
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
        	
        	int numTasks = cand_taskid.size();
        	
        	LGPIndividual_MPMO par[] = new LGPIndividual_MPMO[numTasks];
        	ArrayList<ArrayList<Integer>> graphs = new ArrayList<ArrayList<Integer>>();     	
        	Integer tar[] = new Integer[1];
        	
        	//get the effective instruction from elites of different tasks.
        	for(int i = 0; i<numTasks; i++){
        		par[i] = (LGPIndividual_MPMO) ((LGPIndividual)parents[i]).lightClone();
        		par[i].updateStatus(cand_taskid.get(i));
        		
        		int target = par[i].getCurrentOutputRegister();
	        	tar[0] = target;  
        		
	        	ArrayList<Integer> cand = par[i].getSubGraph(par[i].getTreesLength()-1, tar);
	        	
//	        	ArrayList<Integer> cand = new ArrayList<>();
//	        	for(int j = par[i].getTreesLength()-1; j>=0;j--){
//	        		cand.add(j);
//	        	}
	        	
        		graphs.add(cand);
        	}
        	
        	//merge into j1
        	//1. set the length of j1, by mean length + random detail
        	double sum_length = 0;
        	for(int i = 0;i<numTasks;i++){
        		sum_length += par[i].getTreesLength();
        		//sum_length += par[i].getEffTreesLength(i);
        	}
        	int length = (int) (sum_length/numTasks + (state.random[thread].nextInt(2*step_size + 1) - step_size));
        	
        	if(length > j1.getMaxNumTrees()){
        		length = j1.getMaxNumTrees();
        	}
        	if(length < j1.getMinNumTrees()){
        		length = j1.getMinNumTrees();
        	}
        	
//        	for(int i = 0;i<numTasks;i++){
//    		sum_length += par[i].getEffTreesLength(i);
//    	}
//    	int length = (int) sum_length;
//    	if(sum_length > j1.getMaxNumTrees()){
//    		length = j1.getMaxNumTrees();
//    	}
//    	if(sum_length < j1.getMinNumTrees()){
//    		length = j1.getMinNumTrees();
//    	}
        	
        	//2. randomly add the instruction from different tasks into j1
        	int try_task;
        	for(int l = 0;l<sum_length;l++){
        		int trial = numTasks;
        		try_task = state.random[thread].nextInt(numTasks);
        		while(graphs.get(try_task).size()==0 && trial >0){
        			try_task = (try_task + 1) % numTasks;
        			trial --;
        		}
        		if(trial <= 0){
        			break;
        		}
        		
        		if(graphs.get(try_task).size()>0){
        			j1.addTree(0, par[try_task].getTreeStruct(graphs.get(try_task).get(0)));
            		
        			graphs.get(try_task).remove(0);
        		}
//        		else if(j1.getTreesLength() == 0){
//        			j1.rebuildIndividual(state, thread);
//        		}
//        		else
//        			continue;
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
    				if(((LGPIndividual_MPMO)j1).getEffTreesLength(taskid) <= length){
    					for(int x = 0;x < j1.getTreesLength();x++) {
    		        		if(!j1.getTreeStruct(res).status){break;}
    		        		res = (res + state.random[thread].nextInt(j1.getTreesLength())) % j1.getTreesLength();
    		        	}
    					
    				}
    				
//    				else{
//	    				double peta = state.random[thread].nextDouble();
//	    				int L = j1.getTreesLength();
//	    				double sum = 0;
//	    				for(int j = 0;j<j1.getTreesLength();j++){
//	    					sum += (L-j)/(0.5*L*(L));
//	    					res = j;
//	    					if(sum >= peta){
//	    						break;
//	    					}
//	    				}
//    				}
    				j1.removeTree(res);
    			}
    		}
            
            if(microMutation != null) j1 = (LGPIndividual) microMutation.produce(subpopulation, j1, state, thread);
            
            //because different subpopulations may have different output registers, some just make them consistent           
//          j1.setOutputRegisters(example.getOutputRegisters());
//    		j1.fitness = (MultiObjectiveFitness)example.fitness.clone();
//    		((LGPIndividual_MPMO)j1).skillFactor = ((LGPIndividual_MPMO)example).skillFactor;
            
            inds[q] = j1;
            q++;
            parnt ++;
            
            }
            
        return n;
	}
}
