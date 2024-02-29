package zhixing.cpxInd.algorithm.multitask.MFEA.individual.reproduce;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPBreedingPipeline;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.koza.CrossoverPipeline;
import ec.util.Parameter;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA;

public class ClassGraphCrossover extends CrossoverPipeline{
	
	@Override
	public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) {
		
		System.err.print("haven't define the produce function with self-selected parents for ClassGraphCrossover");
		System.exit(1);
		
		return 0;
	}
	

	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread,
	        final LGPIndividual_MFEA[] parents) {
		
		
		// how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        for(int q=start, parnt = 0;q<n+start; /* no increment */)  // keep on going until we're filled up
            {   	
            
            //select a target output register
        	int target = state.random[thread].nextInt(parents[0].getOutputRegisters().length) ;
        	
        	//collect the class graph for the target output register (updateStatus)
        	int t[] = new int[1];
        	t[0] = target;        	
            
            // are our tree values valid?
            if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= parents[0].getTreesLength()))
                // uh oh
                state.output.fatal("LGP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= parents[1].getTreesLength()))
                // uh oh
                state.output.fatal("LGP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

            int t1=0, t2=0;
            LGPIndividual j1, j2;
            j1 = ((LGPIndividual)parents[parnt]).lightClone();
        	t1 = parnt;
            j2 = ((LGPIndividual)parents[(parnt + 1)%parents.length]).lightClone();
            t2 = (parnt + 1)%parents.length;
            
            if(state.random[thread].nextDouble()<0.5){
            	((LGPIndividual_MFEA)j1).skillFactor = ((LGPIndividual_MFEA)parents[parnt]).skillFactor;
            	((LGPIndividual_MFEA)j2).skillFactor = ((LGPIndividual_MFEA)parents[(parnt + 1)%parents.length]).skillFactor;
            }
            else{
            	((LGPIndividual_MFEA)j1).skillFactor = ((LGPIndividual_MFEA)parents[(parnt + 1)%parents.length]).skillFactor;
            	((LGPIndividual_MFEA)j2).skillFactor = ((LGPIndividual_MFEA)parents[parnt]).skillFactor;
            }
            
            //pick two instruction segments and skip the ineffective instructions
            j1.updateStatus(j1.getTreesLength(), t);
            j2.updateStatus(j2.getTreesLength(), t);
            
        	ArrayList<Integer> cand1 = getEffectiveInstr(j1, 0, j1.getTreesLength());
        	ArrayList<Integer> cand2 = getEffectiveInstr(j2, 0, j2.getTreesLength());
        	
        	int minsize = Math.min(cand1.size(), cand2.size());
        	
        	int modPos1 = 0, modPos2 = 0;
        	if(cand1.isEmpty()){
        		modPos1 = j1.getTreesLength() - 1;
        	}
        	else{
        		modPos1 = cand1.get(0);
        	}
        	if(cand2.isEmpty()){
        		modPos2 = j2.getTreesLength() - 1;
        	}
        	else{
        		modPos2 = cand2.get(0);
        	}
        	
        	//replace j1's instructions by j2
        	for(int i = 0;i<minsize;i++){
        		int destin = cand1.get(i);
        		int source = cand2.get(i);
        		
        		GPTreeStruct tree = (GPTreeStruct) (parents[t2].getTree(source).clone());
        		//tree = (GPTree)(parents[parnt].getTree(pick).lightClone());
                tree.owner = j1;
                tree.child = (GPNode)(parents[t2].getTree(source).child.clone());
                tree.child.parent = tree;
                tree.child.argposition = 0;
                j1.setTree(destin, tree);
                j1.evaluated = false; 
        	}
        	//replace j2's instructions by j1
        	for(int i = 0;i<minsize;i++){
        		int destin = cand2.get(i);
        		int source = cand1.get(i);
        		
        		GPTreeStruct tree = (GPTreeStruct) (parents[t1].getTree(source).clone());
        		//tree = (GPTree)(parents[parnt].getTree(pick).lightClone());
                tree.owner = j2;
                tree.child = (GPNode)(parents[t1].getTree(source).child.clone());
                tree.child.parent = tree;
                tree.child.argposition = 0;
                j2.setTree(destin, tree);
                j2.evaluated = false; 
        	}
            
        	
        	if(cand1.size() > cand2.size()){
        		//remove j1 based on cand1
        		for(int i = minsize;i<cand1.size();i++){
        			if(j1.getTreesLength() <= j1.getMinNumTrees()){
        				break;
        			}
        			
        			j1.removeTree(cand1.get(i));
        			j1.evaluated = false;
        		}
        		
        		//complement j2 based on cand1 from j1
        		for(int i = minsize;i<cand1.size();i++){
        			if(j2.getTreesLength() >= j2.getMaxNumTrees()){
        				break;
        			}
        			
        			int source = cand1.get(i);
            		
            		GPTreeStruct tree = (GPTreeStruct) (parents[t1].getTree(source).clone());
            		//tree = (GPTree)(parents[parnt].getTree(pick).lightClone());
                    tree.owner = j2;
                    tree.child = (GPNode)(parents[t1].getTree(source).child.clone());
                    tree.child.parent = tree;
                    tree.child.argposition = 0;
        			j2.addTree(0, tree);
        			j2.evaluated = false;
        		}
        	}
        	else if(cand1.size() < cand2.size()){
        		//complement j1 based on cand2 from j2
        		for(int i = minsize;i<cand2.size();i++){
        			if(j1.getTreesLength() >= j1.getMaxNumTrees()){
        				break;
        			}
        			
        			int source = cand2.get(i);
            		
            		GPTreeStruct tree = (GPTreeStruct) (parents[t2].getTree(source).clone());
            		//tree = (GPTree)(parents[parnt].getTree(pick).lightClone());
                    tree.owner = j1;
                    tree.child = (GPNode)(parents[t2].getTree(source).child.clone());
                    tree.child.parent = tree;
                    tree.child.argposition = 0;
        			j1.addTree(0, tree);
        			j1.evaluated = false;
        		}
        		
        		//remove j2 based on cand2
        		for(int i = minsize;i<cand2.size();i++){
        			if(j2.getTreesLength() <= j2.getMinNumTrees()){
        				break;
        			}
        			
        			j2.removeTree(cand2.get(i));
        			j2.evaluated = false;
        		}
        	}
        	
            // add the individuals to the population
            inds[q] = j1;
            q++;
            parnt ++;
            if (q<n+start && !tossSecondParent)
            {
            	if(j2.getTreesLength() < j2.getMinNumTrees() || j2.getTreesLength() > j2.getMaxNumTrees()){
                	state.output.fatal("illegal tree number in linear cross j2");
                }
	            inds[q] = j2;
	            q++;
	            parnt ++;
            }
            

            }
            
        return n;
	}
	
	protected ArrayList<Integer> getEffectiveInstr(LGPIndividual ind, int begin, int pickNum){
		// check "pickNum" instructions reversely
		//return true if there is at least one effective instruction
		if(pickNum <0){
			System.err.print("the pickNum in getEffectiveInstr is invaild\n");
			System.exit(1);
		}
		
		ArrayList res = new ArrayList<Integer>();
		for(int i = pickNum - 1;i>=0;i--){
			if((ind.getTreeStruct(begin + i)).status){
				res.add(begin + i);
			}
		}
		return res;
	}


	
}
