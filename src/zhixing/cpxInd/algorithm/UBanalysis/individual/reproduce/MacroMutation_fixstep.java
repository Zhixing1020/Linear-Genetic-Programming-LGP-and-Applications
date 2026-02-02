package zhixing.cpxInd.algorithm.UBanalysis.individual.reproduce;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public class MacroMutation_fixstep extends zhixing.cpxInd.individual.reproduce.LGPMacroMutationPipeline{

	@Override
	public LGPIndividual produce(
			final int subpopulation,
	        final LGPIndividual ind,
	        final EvolutionState state,
	        final int thread) {
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

        //double pickNum = Math.max(state.random[thread].nextDouble()*(i.getTreesLength()), 1);
        double pickNum = stepSize;
        boolean haspickedeff = false;
        
        for(int tryi = 0; tryi<numTries && !haspickedeff; tryi++) {
        	
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
        	
        	for(int pick = 0;pick<pickNum;pick++){
            	int t = state.random[thread].nextInt(j.getTreesLength());
            	
            	//if insert a new instruction
            	if(j.getTreesLength() < j.getMaxNumTrees() && ( state.random[thread].nextDouble() < probInsert || j.getTreesLength() == j.getMinNumTrees())) {
            		
            		t = getLegalInsertIndex(j, state, thread);
            		
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
                        
                        if(j.getTreeStruct(x+1).status) {
                    		haspickedeff = true;
                    	}
                    } // it's changed
            	}
            	else if (j.getTreesLength() > j.getMinNumTrees() &&(state.random[thread].nextDouble() < probInsert + probDelete || j.getTreesLength() == j.getMaxNumTrees())) {
            		
            		t = getLegalDeleteIndex(j, state, thread);
            		
            		if(j.getTreeStruct(t).status) {
                		haspickedeff = true;
                	}
            		
            		j.removeTree(t);
            		j.evaluated = false; 
            	}
            	
            	//j.updateStatus();
            	else if(j.getTreesLength() == j.getMinNumTrees() && j.getTreesLength() == j.getMaxNumTrees()) {
            		t = getLegalInsertIndex(j, state, thread);
            		
            		if(j.getTreeStruct(t).status) {
                		haspickedeff = true;
                	}
            		
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
                        j.removeTree(x);
                        
                        if(j.getTreeStruct(x).status) {
                    		haspickedeff = true;
                    	}
                        
                        j.evaluated = false; 
                    } // it's changed
            	}
            }
        }
        
        
        
        //some following operations
        switch (mutateFlag) {
		case EFFMACROMUT2:
			break;
		case EFFMACROMUT:
		case FREEMACROMUT:
			if(microMutation != null) j = (LGPIndividual) microMutation.produce(subpopulation, j, state, thread);
			break;
		case EFFMACROMUT3:
			//delete all non-effective instructions
			j.removeIneffectiveInstr();

			break;
		default:
			break;
		}
        

        return j;
	}
	
}
