package zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.reproduce;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
//import zhixing.jss.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.reproduce.LGPMacroMutationPipeline;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;
import zhixing.cpxInd.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator;
import zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO;

public class LGPMacroMutation_Specific extends LGPMacroMutationPipeline{
	
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread) {
		// grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);
        
        int taskid = 0;
        
        if(inds[start] instanceof LGPIndividual_MPMO){
        	taskid = ((LGPIndividual_MPMO)inds[start]).skillFactor;
        	if(((LGPIndividual_MPMO)inds[start]).getOutputRegisters().length > 1){
        		taskid = state.random[thread].nextInt(((MFEA_Evaluator)state.evaluator).getNumTasks());
        		n = ((TournamentSel_SpecificIns)sources[0]).produce(min,max,start,subpopulation,inds,state,thread, taskid);
        	}
        }
        else{
        	System.err.print("LGPMacroMutation_Specific cannot be applied to other LGPIndividual types, "
        			+ "it can only be applied to LGPIndividual_MPMO \n");
        	System.exit(1);
        }

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did


        GPInitializer initializer = ((GPInitializer)state.initializer);

        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
            {
            LGPIndividual i = (LGPIndividual)inds[q];

//            if(((LGPIndividual_MPMO)i).getOutputRegisters().length > 1){
//            	inds[q] = this.produce(subpopulation, i, state, thread);
//            }
//            else
            	
            inds[q] = this.produce(subpopulation, i, state, thread, taskid);

            
            }
        return n;
	}
	
//	@Override
//	public LGPIndividual produce(
//			final int subpopulation,
//	        final LGPIndividual ind,
//	        final EvolutionState state,
//	        final int thread){
//		GPInitializer initializer = ((GPInitializer)state.initializer);
//		
//		LGPIndividual i = ind;
//		
//		if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.getTreesLength()))
//            // uh oh
//            state.output.fatal("LGP Mutation Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
//            
//        LGPIndividual j;
//
//        if (sources[0] instanceof BreedingPipeline)
//            // it's already a copy, so just smash the tree in
//            {
//            j=i;
//            }
//        else // need to clone the individual
//            {
//            j = ((LGPIndividual)i).lightClone();
//            
//            // Fill in various tree information that didn't get filled in there
//            //j.renewTrees();
//            
//            }
//        for(int v = 0;v<i.getTreesLength();v++) {
//        	int x = v;
//    		GPTree tree = j.getTree(x);
//    		tree = (GPTree)(i.getTree(x).lightClone());
//            tree.owner = j;
//            tree.child = (GPNode)(i.getTree(x).child.clone());
//            tree.child.parent = tree;
//            tree.child.argposition = 0;    
//            j.setTree(x, tree);
//        }
//        
//        for(int taskid = 0;taskid<((MFEA_Evaluator)state.evaluator).getNumTasks();taskid++){
//        	//double pickNum = Math.max(state.random[thread].nextDouble()*(i.getTreesLength()), 1);
//            double pickNum = state.random[thread].nextInt(stepSize) + 1.0;
//            for(int pick = 0;pick<pickNum;pick++){
//            	int t = state.random[thread].nextInt(j.getTreesLength());
//            	
//            	//if insert a new instruction
//            	if(j.getTreesLength() < j.getMaxNumTrees() && ( state.random[thread].nextDouble() < probInsert || j.getTreesLength() == j.getMinNumTrees())) {
//            		
//            		t = getLegalInsertIndex(j, state, thread, taskid);
//            		
//            		// validity result...
//    	            boolean res = false;
//    	            
//    	            // prepare the nodeselector
//    	            nodeselect.reset();
//    	            
//    	            // pick a node
//    	            
//    	            //GPNode p1=i.getTree(t).child;  // the node we pick
//    	            GPNode p1=j.getTree(t).child; 
//    	            GPNode p2=null;
//    	            
//    	            for(int x=0;x<numTries;x++)
//    	                {    	                
//    	                // generate a tree swap-compatible with p1's position
//    	                
//    	                
//    	                int size = GPNodeBuilder.NOSIZEGIVEN;
//    	                if (equalSize) size = p1.numNodes(GPNode.NODESEARCH_ALL);
//    	                
//    	                if(builder instanceof LGPMutationGrowBuilder) {
//    	                	p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
//    	    	                    p1.parentType(initializer),
//    	    	                    thread,
//    	    	                    p1.parent,
//    	    	                    //i.getTree(t).constraints(initializer).functionset,
//    	    	                    j.getTree(t).constraints(initializer).functionset,
//    	    	                    p1.argposition,
//    	    	                    size,
//    	    	                    p1.atDepth());
//    	                }
//    	                else {
//    	                	 p2 = builder.newRootedTree(state,
//    	     	                    p1.parentType(initializer),
//    	     	                    thread,
//    	     	                    p1.parent,
//    	     	                    //i.getTree(t).constraints(initializer).functionset,
//    	     	                    j.getTree(t).constraints(initializer).functionset,
//    	     	                    p1.argposition,
//    	     	                    size);
//    					}
//    	               
//    	                
//    	                // check for depth and swap-compatibility limits
//    	                //res = verifyPoints(p2,p1);  // p2 can fit in p1's spot  -- the order is important!
//    	                res = checkPoints(p1, p2, j.getTreeStruct(t));
//    	                
//    	                // did we get something that had both nodes verified?
//    	                if (res) break;
//    	                }
//    	            
//    	            //if (res)  // we've got a tree with a kicking cross position!
//                    {
//    		            int x = t;
//    		            //GPTree tree = j.getTree(x);
//                        //tree = (GPTree)(i.getTree(x).lightClone());
//    		            GPTreeStruct tree = (GPTreeStruct) j.getTreeStruct(x).clone();
//                        tree.owner = j;
//                        //tree.child = i.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
//                        tree.child = j.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
//                        tree.child.parent = tree;
//                        tree.child.argposition = 0;
//                        j.addTree(x, tree);
//                        j.evaluated = false; 
//                    } // it's changed
//            	}
//            	else if (j.getTreesLength() > j.getMinNumTrees() &&(state.random[thread].nextDouble() < probInsert + probDelete || j.getTreesLength() == j.getMaxNumTrees())) {
//            		
//            		t = getLegalDeleteIndex(j, state, thread, taskid);
//            		j.removeTree(t);
//            		j.evaluated = false; 
//            	}
//            	
//            	//j.updateStatus();
//            	
//            }
//            
//            
//            
//        }
//        
//      //some following operations
//        switch (mutateFlag) {
//		case EFFMACROMUT2:
//			break;
//		case EFFMACROMUT:
//		case FREEMACROMUT:
//			if(microMutation != null){
//				if(microMutation instanceof LGPMicroMutation_Specific){
//					j = ((LGPMicroMutation_Specific)microMutation).produce(subpopulation, j, state, thread);
//				}
//				else
//					j = microMutation.produce(subpopulation, j, state, thread);
//			}
//			break;
//		case EFFMACROMUT3:
//			//delete all non-effective instructions
//			j.removeIneffectiveInstr();
////			for(int ii = 0;ii<j.getTreesLength();ii++) {
////				if(!j.getTreeStruct(ii).status && j.getTreesLength()>j.getMinNumTrees()) {
////					j.removeTree(ii);
////					ii--; //ii remain no change, so that it can point to the next tree
////				}
////			}
//			break;
//		default:
//			break;
//		}
//        
//        //if(microMutation != null) j = microMutation.produce(subpopulation, j, state, thread);
//        // add the new individual, replacing its previous source
//        //inds[q] = j;
//        return j;
//	}
	
	public LGPIndividual produce(
			final int subpopulation,
	        final LGPIndividual ind,
	        final EvolutionState state,
	        final int thread,
	        final int taskid) {
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		LGPIndividual i = ind;
		
		if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.getTreesLength()))
            // uh oh
            state.output.fatal("LGP Mutation Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            
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
        for(int v = 0;v<i.getTreesLength();v++) {
        	int x = v;
    		GPTree tree = j.getTree(x);
    		tree = (GPTree)(i.getTree(x).lightClone());
            tree.owner = j;
            tree.child = (GPNode)(i.getTree(x).child.clone());
            tree.child.parent = tree;
            tree.child.argposition = 0;    
            j.setTree(x, tree);
        }
        
        //double pickNum = Math.max(state.random[thread].nextDouble()*(i.getTreesLength()), 1);
        double pickNum = state.random[thread].nextInt(stepSize) + 1.0;
        for(int pick = 0;pick<pickNum;pick++){
        	int t = state.random[thread].nextInt(j.getTreesLength());
        	
        	//if insert a new instruction
        	if(j.getTreesLength() < j.getMaxNumTrees() && ( state.random[thread].nextDouble() < probInsert || j.getTreesLength() == j.getMinNumTrees())) {
        		
        		t = getLegalInsertIndex(j, state, thread, taskid);
        		
        		// validity result...
	            boolean res = false;
	            
	            // prepare the nodeselector
	            nodeselect.reset();
	            
	            // pick a node
	            
	            //GPNode p1=i.getTree(t).child;  // the node we pick
	            GPNode p1=j.getTree(t).child; 
	            GPNode p2=null;
	            
	            for(int x=0;x<numTries;x++)
	                {    	                
	                // generate a tree swap-compatible with p1's position
	                
	                
	                int size = GPNodeBuilder.NOSIZEGIVEN;
	                if (equalSize) size = p1.numNodes(GPNode.NODESEARCH_ALL);
	                
	                if(builder instanceof LGPMutationGrowBuilder) {
	                	p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
	    	                    p1.parentType(initializer),
	    	                    thread,
	    	                    p1.parent,
	    	                    //i.getTree(t).constraints(initializer).functionset,
	    	                    j.getTree(t).constraints(initializer).functionset,
	    	                    p1.argposition,
	    	                    size,
	    	                    p1.atDepth());
	                }
	                else {
	                	 p2 = builder.newRootedTree(state,
	     	                    p1.parentType(initializer),
	     	                    thread,
	     	                    p1.parent,
	     	                    //i.getTree(t).constraints(initializer).functionset,
	     	                    j.getTree(t).constraints(initializer).functionset,
	     	                    p1.argposition,
	     	                    size);
					}
	               
	                
	                // check for depth and swap-compatibility limits
	                //res = verifyPoints(p2,p1);  // p2 can fit in p1's spot  -- the order is important!
	                res = checkPoints(p1, p2, j.getTreeStruct(t));
	                
	                // did we get something that had both nodes verified?
	                if (res) break;
	                }
	            
	            //if (res)  // we've got a tree with a kicking cross position!
                {
		            int x = t;
		            //GPTree tree = j.getTree(x);
                    //tree = (GPTree)(i.getTree(x).lightClone());
		            GPTreeStruct tree = (GPTreeStruct) j.getTreeStruct(x).clone();
                    tree.owner = j;
                    //tree.child = i.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
                    tree.child = j.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
                    tree.child.parent = tree;
                    tree.child.argposition = 0;
                    j.addTree(x+1, tree);
                    j.evaluated = false; 
                } // it's changed
        	}
        	else if (j.getTreesLength() > j.getMinNumTrees() &&(state.random[thread].nextDouble() < probInsert + probDelete || j.getTreesLength() == j.getMaxNumTrees())) {
        		
        		t = getLegalDeleteIndex(j, state, thread, taskid);
        		j.removeTree(t);
        		j.evaluated = false; 
        	}
        	
        	//j.updateStatus();
        	
        }
        
        
        //some following operations
        switch (mutateFlag) {
		case EFFMACROMUT2:
			break;
		case EFFMACROMUT:
		case FREEMACROMUT:
			if(microMutation != null){
				if(microMutation instanceof LGPMicroMutation_Specific){
					j = ((LGPMicroMutation_Specific)microMutation).produce(subpopulation, j, state, thread,taskid);
				}
				else
					j = (LGPIndividual) microMutation.produce(subpopulation, j, state, thread);
			}
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
	
	protected int getLegalInsertIndex(LGPIndividual ind, EvolutionState state, int thread, int taskid) {
		int res = 0;
		switch (mutateFlag) {
		case FREEMACROMUT: //insert an instruction into random position
			res = state.random[thread].nextInt(ind.getTreesLength());
			break;
		case EFFMACROMUT: //insert an instruction into an effective position
		case EFFMACROMUT2:
		case EFFMACROMUT3:
			((LGPIndividual_MPMO)ind).updateStatus(taskid);
			res = state.random[thread].nextInt(ind.getTreesLength());
			for(int x = 0;x<numTries;x++) {
        		if(ind.getTreeStruct(res).effRegisters.size() > 0) break;
        		res = state.random[thread].nextInt(ind.getTreesLength());
        	}
			ind.updateStatus();
			break;
		default:
			state.output.fatal("illegal mutateFlag in LGP macro mutation");
			break;
		}
		return res;
	}
	
	protected int getLegalDeleteIndex(LGPIndividual ind, EvolutionState state, int thread, int taskid) {
		int res = 0;
		switch (mutateFlag) {
		case FREEMACROMUT: //delete an instruction on random position
		case EFFMACROMUT:
			res = state.random[thread].nextInt(ind.getTreesLength());
			break;
		case EFFMACROMUT2://delete a random effective instruction
		case EFFMACROMUT3:
			((LGPIndividual_MPMO)ind).updateStatus(taskid);
			res = state.random[thread].nextInt(ind.getTreesLength());
			for(int x = 0;x<numTries;x++) {
        		if(ind.getTreeStruct(res).status) break;
        		res = state.random[thread].nextInt(ind.getTreesLength());
        	}
			ind.updateStatus();
			break;
		default:
			state.output.fatal("illegal mutateFlag in LGP macro mutation");
			break;
		}
		return res;
	}
}
