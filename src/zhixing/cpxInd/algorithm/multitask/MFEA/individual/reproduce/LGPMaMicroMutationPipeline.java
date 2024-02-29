package zhixing.cpxInd.algorithm.multitask.MFEA.individual.reproduce;

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
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA;

public class LGPMaMicroMutationPipeline extends LGPMacroMutationPipeline{
	
	@Override
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread){
		// grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did

        LGPIndividual_MFEA[] parents = new LGPIndividual_MFEA[n];
        
        GPInitializer initializer = ((GPInitializer)state.initializer);

        // now let's mutate 'em
        for(int q=start, parnt = 0; q < n+start; q++, parnt++)
            {
            //LGPIndividual i = (LGPIndividual)inds[q];
            
            parents[parnt] = (LGPIndividual_MFEA)inds[q];

//            if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.getTreesLength()))
//                // uh oh
//                state.output.fatal("LGP Mutation Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
//                
//            LGPIndividual j;
//
//            if (sources[0] instanceof BreedingPipeline)
//                // it's already a copy, so just smash the tree in
//                {
//                j=i;
//                }
//            else // need to clone the individual
//                {
//                j = ((LGPIndividual)i).lightClone();
//                
//                // Fill in various tree information that didn't get filled in there
//                //j.renewTrees();
//                
//                }
//            for(int v = 0;v<i.getTreesLength();v++) {
//            	int x = v;
//        		GPTree tree = j.getTree(x);
//        		tree = (GPTree)(i.getTree(x).lightClone());
//                tree.owner = j;
//                tree.child = (GPNode)(i.getTree(x).child.clone());
//                tree.child.parent = tree;
//                tree.child.argposition = 0;    
//                j.setTree(x, tree);
//            }
//            
//            //double pickNum = Math.max(state.random[thread].nextDouble()*(i.getTreesLength()), 1);
//            double pickNum = state.random[thread].nextInt(stepSize) + 1.0;
//            for(int pick = 0;pick<pickNum;pick++){
//            	int t = state.random[thread].nextInt(j.getTreesLength());
//            	
//            	if(state.random[thread].nextDouble()>0.5){//MicroMutation
//            		j = microMutation.produce(subpopulation, j, state, thread);
//            	}
//            	else{
//            		//if insert a new instruction
//                	if(j.getTreesLength() < j.getMaxNumTrees() && ( state.random[thread].nextDouble() < probInsert || j.getTreesLength() == j.getMinNumTrees())) {
//                		
//                		t = getLegalInsertIndex(j, state, thread);
//                		
//                		// validity result...
//        	            boolean res = false;
//        	            
//        	            // prepare the nodeselector
//        	            nodeselect.reset();
//        	            
//        	            // pick a node
//        	            
//        	            //GPNode p1=i.getTree(t).child;  // the node we pick
//        	            GPNode p1=j.getTree(t).child; 
//        	            GPNode p2=null;
//        	            
//        	            for(int x=0;x<numTries;x++)
//        	                {    	                
//        	                // generate a tree swap-compatible with p1's position
//        	                
//        	                
//        	                int size = GPNodeBuilder.NOSIZEGIVEN;
//        	                if (equalSize) size = p1.numNodes(GPNode.NODESEARCH_ALL);
//        	                
//        	                if(builder instanceof LGPMutationGrowBuilder) {
//        	                	p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
//        	    	                    p1.parentType(initializer),
//        	    	                    thread,
//        	    	                    p1.parent,
//        	    	                    //i.getTree(t).constraints(initializer).functionset,
//        	    	                    j.getTree(t).constraints(initializer).functionset,
//        	    	                    p1.argposition,
//        	    	                    size,
//        	    	                    p1.atDepth());
//        	                }
//        	                else {
//        	                	 p2 = builder.newRootedTree(state,
//        	     	                    p1.parentType(initializer),
//        	     	                    thread,
//        	     	                    p1.parent,
//        	     	                    //i.getTree(t).constraints(initializer).functionset,
//        	     	                    j.getTree(t).constraints(initializer).functionset,
//        	     	                    p1.argposition,
//        	     	                    size);
//        					}
//        	               
//        	                
//        	                // check for depth and swap-compatibility limits
//        	                //res = verifyPoints(p2,p1);  // p2 can fit in p1's spot  -- the order is important!
//        	                res = checkPoints(p1, p2, j.getTreeStruct(t));
//        	                
//        	                // did we get something that had both nodes verified?
//        	                if (res) break;
//        	                }
//        	            
//        	            //if (res)  // we've got a tree with a kicking cross position!
//                        {
//        		            int x = t;
//        		            //GPTree tree = j.getTree(x);
//                            //tree = (GPTree)(i.getTree(x).lightClone());
//        		            GPTree tree = j.getTree(x).lightClone();
//                            tree.owner = j;
//                            //tree.child = i.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
//                            tree.child = j.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
//                            tree.child.parent = tree;
//                            tree.child.argposition = 0;
//                            j.addTree(x, tree);
//                            j.evaluated = false; 
//                        } // it's changed
//                	}
//                	else if (j.getTreesLength() > j.getMinNumTrees() &&(state.random[thread].nextDouble() < probInsert + probDelete || j.getTreesLength() == j.getMaxNumTrees())) {
//                		
//                		t = getLegalDeleteIndex(j, state, thread);
//                		j.removeTree(t);
//                		j.evaluated = false; 
//                	}
//                	
//                	//some following operations
//                    switch (mutateFlag) {
//        			case FREEMACROMUT:
//        			case EFFMACROMUT2:
//        				break;
//        			case EFFMACROMUT:
//        				j = microMutation.produce(subpopulation, j, state, thread);
//        				break;
//        			case EFFMACROMUT3:
//        				//delete all non-effective instructions
//        				for(int ii = 0;ii<j.getTreesLength();ii++) {
//        					if(!j.getTreeStruct(ii).status && j.getTreesLength()>j.getMinNumTrees()) {
//        						j.removeTree(ii);
//        						ii--; //ii remain no change, so that it can point to the next tree
//        					}
//        				}
//        				break;
//        			default:
//        				break;
//        			}
//                    
//                    j = microMutation.produce(subpopulation, j, state, thread);
//            	}
//            	
//            	
//            }
//            
//            // add the new individual, replacing its previous source
//            inds[q] = j;
            }
        
        return this.produce(min, max, start, subpopulation, inds, state, thread, parents);
        //return n;
	}
	
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread,
	        final LGPIndividual_MFEA[] parents) {
		// grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
		int n = parents.length;
		if (n < min) n = min;
        if (n > max) n = max;

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did


        GPInitializer initializer = ((GPInitializer)state.initializer);

        // now let's mutate 'em
        for(int q=start, parnt=0; q < n+start; q++, parnt++)
            {
            LGPIndividual i = parents[parnt];

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
                j = ((LGPIndividual_MFEA)i).lightClone();
                
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
            
            //double pickNum = Math.max(state.random[thread].nextDouble()*(i.getTreesLength()), 1);
            double pickNum = state.random[thread].nextInt(stepSize) + 1.0;
            for(int pick = 0;pick<pickNum;pick++){
            	int t = state.random[thread].nextInt(j.getTreesLength());
            	
            	if(state.random[thread].nextDouble()>0.5){//MicroMutation
            		j = (LGPIndividual) microMutation.produce(subpopulation, j, state, thread);
            	}
            	else{
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
        		            GPTreeStruct tree = (GPTreeStruct) j.getTree(x).clone();
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
                		
                		t = getLegalDeleteIndex(j, state, thread);
                		j.removeTree(t);
                		j.evaluated = false; 
                	}
                	
                	
                	//some following operations
                    switch (mutateFlag) {
        			case FREEMACROMUT:
        			case EFFMACROMUT2:
        				break;
        			case EFFMACROMUT:
        				j = (LGPIndividual) microMutation.produce(subpopulation, j, state, thread);
        				break;
        			case EFFMACROMUT3:
        				//delete all non-effective instructions
        				for(int ii = 0;ii<j.getTreesLength();ii++) {
        					if(!j.getTreeStruct(ii).status && j.getTreesLength()>j.getMinNumTrees()) {
        						j.removeTree(ii);
        						ii--; //ii remain no change, so that it can point to the next tree
        					}
        				}
        				break;
        			default:
        				break;
        			}
                    
                    j = (LGPIndividual) microMutation.produce(subpopulation, j, state, thread);
                	
            	}

            }

            // add the new individual, replacing its previous source
            inds[q] = j;
            }
        return n;
	}
}
