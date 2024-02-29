package zhixing.cpxInd.algorithm.Graphbased.individual.reproduce;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public class GraphMacroMutation extends zhixing.cpxInd.individual.reproduce.LGPMacroMutationPipeline {
	@Override
	public LGPIndividual produce(
			final int subpopulation,
	        final LGPIndividual ind,
	        final EvolutionState state,
	        final int thread) {
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		LGPIndividual4Graph i = (LGPIndividual4Graph) ind;
		
		if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.getTreesLength()))
            // uh oh
            state.output.fatal("LGP Mutation Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            
        LGPIndividual4Graph j;

        if (sources[0] instanceof BreedingPipeline)
            // it's already a copy, so just smash the tree in
            {
            j=i;
            }
        else // need to clone the individual
            {
            j = ((LGPIndividual4Graph)i).lightClone();
            
            // Fill in various tree information that didn't get filled in there
            //j.renewTrees();
            
            }
        for(int v = 0;v<i.getTreesLength();v++) {
        	int x = v;
    		GPTreeStruct tree = (GPTreeStruct) j.getTree(x);
    		tree = (GPTreeStruct)(i.getTree(x).clone());
            tree.owner = j;
            tree.child = (GPNode)(i.getTree(x).child.clone());
            tree.child.parent = tree;
            tree.child.argposition = 0;    
            j.setTree(x, tree);
        }
        
        int GraphSize = state.random[thread].nextInt(stepSize) + 1;
        
      //if insert a new graph
        double prob = state.random[thread].nextDouble();
    	if(j.getEffTreesLength() < j.getMaxNumTrees()-GraphSize && ( prob < probInsert || j.getTreesLength() == j.getMinNumTrees())) {
    		
    		//inserting slot
    		//int t = getLegalInsertIndex(j, state, thread);
    		int t = state.random[thread].nextInt(j.getTreesLength());
    		//randomly select an effective target register as output
    		Set<Integer> targetRegister = new HashSet<>(0);
    		ArrayList<Integer> list = new ArrayList<>(i.getTreeStruct(t).effRegisters);
    		int tar = state.random[thread].nextInt(i.getRegisters().length);
    		if(list.size() > 0){
    			tar = list.get(state.random[thread].nextInt(list.size()));
    		}
			targetRegister.add(tar);
    		
    		//build a list of instructions 
			Vector<GPTreeStruct> graph = new Vector(GraphSize);
			for(int g = 0;g<GraphSize;g++){
				//GPTreeStruct tree = new GPTreeStruct();
				GPTreeStruct tree = (GPTreeStruct) j.getTreeStruct(t).clone();
				tree.buildTree(state, thread);
				//set destination register
				list = new ArrayList<>(targetRegister);
				tar = list.get(state.random[thread].nextInt(list.size()));
				
				((WriteRegisterGPNode)tree.child).setIndex(tar);
				
				//update target registers
				targetRegister.remove(tar);
				tree.updateEffRegister(targetRegister);
				
				//update other information
				tree.owner = j;
	            tree.child.parent = tree;
	            tree.child.argposition = 0;
	            
				graph.add(0, tree);
			}
			
			//insert the graph
			for(int g = graph.size()-1;g>=0;g--){
				j.addTree(t, graph.get(g));
			}
            
            j.evaluated = false; 
    		
    	}
    	else if (j.getTreesLength() > j.getMinNumTrees() &&(prob < probInsert + probDelete || j.getTreesLength() == j.getMaxNumTrees())) {
    		
    		//int t = getLegalDeleteIndex(j, state, thread);
    		int t = state.random[thread].nextInt(j.getTreesLength());
    		
    		ArrayList<Integer> list = new ArrayList<>(i.getTreeStruct(t).effRegisters);
    		int tar = state.random[thread].nextInt(i.getRegisters().length);
    		if(list.size() > 0){
    			tar = list.get(state.random[thread].nextInt(list.size()));
    		}
    		
    		Integer tar1[] = new Integer[1];
        	tar1[0] = tar;  
    		
    		ArrayList<Integer> graph = j.getSubGraph(t, tar1);
    		
    		for(int g = 0;g<Math.min(GraphSize, graph.size()) && j.getTreesLength()>j.getMinNumTrees();g++){
    			j.removeTree(graph.get(g));
    			j.evaluated = false; 
    		}

    		if(j.evaluated == true && j.getTreesLength()>j.getMinNumTrees()){
    			t = getLegalDeleteIndex(j, state, thread);
        		j.removeTree(t);
        		j.evaluated = false; 
        		//return j;
    		}
    	}
    	
    	//remove introns
		if(j.getTreesLength() > j.getMaxNumTrees()){
			int cnt = j.getTreesLength() - j.getMaxNumTrees();
			for(int k = 0; k<cnt; k++){
				int res = state.random[thread].nextInt(j.getTreesLength());
				if(j.getEffTreesLength() <= j.getMaxNumTrees()){
					for(int x = 0;x < j.getTreesLength();x++) {
		        		if(!j.getTreeStruct(res).status){break;}
		        		res = (res + 1) % j.getTreesLength();
		        	}
					
				}
				j.removeTree(res);
			}
		}
        
        //some following operations
        switch (mutateFlag) {
		case EFFMACROMUT2:
			break;
		case EFFMACROMUT:
		case FREEMACROMUT:
			if(microMutation != null) j = (LGPIndividual4Graph) microMutation.produce(subpopulation, j, state, thread);
			break;
		case EFFMACROMUT3:
			//delete all non-effective instructions
			j.removeIneffectiveInstr();
//			for(int ii = 0;ii<j.getTreesLength();ii++) {
//				if(!j.getTreeStruct(ii).status && j.getTreesLength()>j.getMinNumTrees()) {
//					j.removeTree(ii);
//					ii--; //ii remain no change, so that it can point to the next tree
//				}
//			}
			break;
		default:
			break;
		}
        
        //if(microMutation != null) j = microMutation.produce(subpopulation, j, state, thread);
        // add the new individual, replacing its previous source
        //inds[q] = j;
        return j;
	}
}
