package zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.reproduce;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.reproduce.LGPMicroMutationPipeline;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;
import zhixing.cpxInd.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator;
import zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO;

public class LGPMicroMutation_Specific extends LGPMicroMutationPipeline{
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
	        
	        int taskid = 0;
	        
	        if(inds[start] instanceof LGPIndividual_MPMO){
	        	taskid = ((LGPIndividual_MPMO)inds[start]).skillFactor;
	        	if(((LGPIndividual_MPMO)inds[start]).getOutputRegisters().length > 1){
	        		taskid = state.random[thread].nextInt(((MFEA_Evaluator)state.evaluator).getNumTasks());
	        		n = ((TournamentSel_SpecificIns)sources[0]).produce(min,max,start,subpopulation,inds,state,thread, taskid);
	        	}
	        }
	        else{
	        	System.err.print("LGPMicroMutation_Specific cannot be applied to other LGPIndividual types"
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
	            
//	            if(((LGPIndividual_MPMO)i).getOutputRegisters().length > 1){
//	            	inds[q] = this.produce(subpopulation, i, state, thread);
//	            }
//	            else
	            	
	            inds[q] = this.produce(subpopulation, i, state, thread, taskid);

	            }
	        return n;
	        }
	
//	@Override
//	public LGPIndividual produce(
//			final int subpopulation,
//	        final LGPIndividual ind,
//	        final EvolutionState state,
//	        final int thread) {
//		GPInitializer initializer = ((GPInitializer)state.initializer);
//		
//		LGPIndividual i = ind;
//
//        if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.getTreesLength()))
//            // uh oh
//            state.output.fatal("LGP Mutation Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual");
//        
//        //get the function set
//        GPFunctionSet set = i.getTree(0).constraints(initializer).functionset;  //all trees have the same function set
//       
//        
//      //get the mutation component
//        double rnd = state.random[thread].nextDouble();
//        if(rnd>p_function+p_constant+p_writereg+p_readreg) { //randomly select the component type
//        	componenttype = state.random[thread].nextInt(4);
//        }
//        else if(rnd > p_constant + p_writereg + p_readreg) { //function
//        	componenttype = functions;
//        }
//        else if (rnd > p_writereg + p_readreg) { //constnat
//        	componenttype = cons;
//        }
//        else if ( rnd > p_readreg) { //write register
//        	componenttype = writereg;
//        }
//        else {
//        	componenttype = readreg; //read register
//        }
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
//            }
//        
//        for(int taskid = 0;taskid<((MFEA_Evaluator)state.evaluator).getNumTasks();taskid++){
//        	//double pickNum = Math.max(state.random[thread].nextDouble()*(i.getTreesLength()), 1);
//            double pickNum = state.random[thread].nextInt(stepSize) + 1.0;
//            for(int pick = 0;pick<pickNum;pick++){
//            	int t = getLegalMutateIndex(j, state, thread, taskid);
//            	
//            	// pick random tree
//                if (tree!=TREE_UNFIXED)
//                    t = tree;
//                
//
//                // validity result...
//                boolean res = false;
//                
//                // prepare the nodeselector
//                nodeselect.reset();
//                
//                // pick a node
//                
//                GPNode p1=null;  // the node we pick
//                GPNode p2=null;
//                int cnt = 0; //the number of primitives that satisfies the given component type
//                int cntdown = 0;
//                GPTree oriTree = i.getTree(t);
//                int flag = -1;//wheter it need to reselect the p1
//                
//                switch (componenttype) {
//    			case functions:
//    				flag = GPNode.NODESEARCH_NONTERMINALS;
//    				break;
//    			case cons:
//    				flag = GPNode.NODESEARCH_CONSTANT;
//    				break;
//    			case writereg:
//    				flag = -1;
//    				cnt = 1;
//    				p1 = oriTree.child;
//    				break;
//    			case readreg:
//    				flag = GPNode.NODESEARCH_READREG;
//    				break;
//    			default:
//    				break;
//    			}
//                if (flag >=0) cnt = oriTree.child.numNodes(flag);
//
//                for(int x=0;x<numTries;x++)
//                    {
//                	// pick a node in individual 1
//                	//p1 = nodeselect.pickNode(state,subpopulation,thread,i,i.getTree(t));
//                	if(flag>=0 && cnt >0) p1 = oriTree.child.nodeInPosition(state.random[thread].nextInt(cnt),flag);
//                		
//                	
//                	int size = GPNodeBuilder.NOSIZEGIVEN;
//                    if (equalSize) size = p1.numNodes(GPNode.NODESEARCH_ALL);
//                	
//                    if(cnt > 0) {
//                    	switch (componenttype) {
//                    	case functions:
//    						p2 = (GPNode)((GPNode) set.nonterminals_v.get(state.random[thread].nextInt(set.nonterminals_v.size()))).lightClone();
//    						p2.resetNode(state, thread);
//    						break;
//    					case cons:
//    						if(state.random[thread].nextDouble()<((LGPMutationGrowBuilder)builder).probCons) {
//    							p2 = (GPNode)((GPNode) set.constants_v.get(state.random[thread].nextInt(set.constants_v.size()))).lightClone();
//    							p2.resetNode(state, thread);
//    						}
//    						else {
//    							p2 = (GPNode)((GPNode) set.nonconstants_v.get(state.random[thread].nextInt(set.nonconstants_v.size()))).lightClone();
//    							p2.resetNode(state, thread);
//    						}
//    						break;
//    					case writereg:
//    						p2 = (GPNode)((GPNode) set.registers_v.get(state.random[thread].nextInt(set.registers_v.size()))).lightClone();
//    						p2.resetNode(state, thread);
//    						break;
//    					case readreg:
////    						p2 = (GPNode) set.nonconstants_v.get(state.random[thread].nextInt(set.nonconstants_v.size()));
////    						p2.resetNode(state, thread);
//    						p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
//    	    	                    p1.parentType(initializer),
//    	    	                    thread,
//    	    	                    p1.parent,
//    	    	                    i.getTree(t).constraints(initializer).functionset,
//    	    	                    p1.argposition,
//    	    	                    size,
//    	    	                    p1.atDepth());
//    						break;
//    					default:
//    						break;
//    					}
//                    }
//                	 
//                	 
//                    else {
//                    	//no suitable instruction is found, so there is no primitive with the given component type
//                		 p1 = nodeselect.pickNode(state,subpopulation,thread,i,i.getTree(t));
//                		 p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
//    	    	                    p1.parentType(initializer),
//    	    	                    thread,
//    	    	                    p1.parent,
//    	    	                    i.getTree(t).constraints(initializer).functionset,
//    	    	                    p1.argposition,
//    	    	                    size,
//    	    	                    p1.atDepth());
//                	 }
//
//                    // check for depth and swap-compatibility limits
//                    //res = verifyPoints(p2,p1);  // p2 can fit in p1's spot  -- the order is important!
//                    
//                    //easy check
//                    res = checkPoints(p1, p2, state, thread, i, i.getTreeStruct(t));
//                    //instance check
//                    
//                    // did we get something that had both nodes verified?
//                    if (res) break;
//                    }
//                
//                if (res)  // we've got a tree with a kicking cross position!
//                {
//    	            int x = t;
//    	            GPTreeStruct tree = j.getTreeStruct(x);
//                    tree = (GPTreeStruct)(i.getTreeStruct(x).clone());
//                    tree.owner = j;
//                    tree.child = i.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
//                    tree.child.parent = tree;
//                    tree.child.argposition = 0;
//                    j.setTree(x, tree);
//                    j.evaluated = false; 
//                } // it's changed
//                else{
//                	int x = t;
//                	GPTreeStruct tree = j.getTreeStruct(x);
//            		tree = (GPTreeStruct)(i.getTreeStruct(x).clone());
//                    tree.owner = j;
//                    tree.child = (GPNode)(i.getTree(x).child.clone());
//                    tree.child.parent = tree;
//                    tree.child.argposition = 0;    
//                    j.setTree(x, tree);
//                }
//                
//               // j.updateStatus();
//                
//            }
//        }
//        
//        
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
        
        //get the function set
        GPFunctionSet set = i.getTree(0).constraints(initializer).functionset;  //all trees have the same function set
       
        
      //get the mutation component
        double rnd = state.random[thread].nextDouble();
        if(rnd>p_function+p_constant+p_writereg+p_readreg) { //randomly select the component type
        	componenttype = state.random[thread].nextInt(4);
        }
        else if(rnd > p_constant + p_writereg + p_readreg) { //function
        	componenttype = functions;
        }
        else if (rnd > p_writereg + p_readreg) { //constnat
        	componenttype = cons;
        }
        else if ( rnd > p_readreg) { //write register
        	componenttype = writereg;
        }
        else {
        	componenttype = readreg; //read register
        }

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
        double pickNum = state.random[thread].nextInt(stepSize) + 1.0;
        for(int pick = 0;pick<pickNum;pick++){
        	int t = getLegalMutateIndex(j, state, thread, taskid);
        	
        	// pick random tree
            if (tree!=TREE_UNFIXED)
                t = tree;
            

            // validity result...
            boolean res = false;
            
            // prepare the nodeselector
            nodeselect.reset();
            
            // pick a node
            
            GPNode p1=null;  // the node we pick
            GPNode p2=null;
            int cnt = 0; //the number of primitives that satisfies the given component type
            int cntdown = 0;
            GPTree oriTree = i.getTree(t);
            int flag = -1;//wheter it need to reselect the p1
            
            switch (componenttype) {
			case functions:
				flag = GPNode.NODESEARCH_NONTERMINALS;
				break;
			case cons:
				flag = GPNode.NODESEARCH_CONSTANT;
				break;
			case writereg:
				flag = -1;
				cnt = 1;
				p1 = oriTree.child;
				break;
			case readreg:
				flag = GPNode.NODESEARCH_READREG;
				break;
			default:
				break;
			}
            if (flag >=0) cnt = oriTree.child.numNodes(flag);

            for(int x=0;x<numTries;x++)
                {
            	// pick a node in individual 1
            	//p1 = nodeselect.pickNode(state,subpopulation,thread,i,i.getTree(t));
            	if(flag>=0 && cnt >0) p1 = oriTree.child.nodeInPosition(state.random[thread].nextInt(cnt),flag);
            		
            	
            	int size = GPNodeBuilder.NOSIZEGIVEN;
                if (equalSize) size = p1.numNodes(GPNode.NODESEARCH_ALL);
            	
                if(cnt > 0) {
                	switch (componenttype) {
                	case functions:
						p2 = (GPNode)((GPNode) set.nonterminals_v.get(state.random[thread].nextInt(set.nonterminals_v.size()))).lightClone();
						p2.resetNode(state, thread);
						break;
					case cons:
						if(state.random[thread].nextDouble()<((LGPMutationGrowBuilder)builder).probCons) {
							p2 = (GPNode)((GPNode) set.constants_v.get(state.random[thread].nextInt(set.constants_v.size()))).lightClone();
							p2.resetNode(state, thread);
						}
						else {
							p2 = (GPNode)((GPNode) set.nonconstants_v.get(state.random[thread].nextInt(set.nonconstants_v.size()))).lightClone();
							p2.resetNode(state, thread);
						}
						break;
					case writereg:
						p2 = (GPNode)((GPNode) set.registers_v.get(state.random[thread].nextInt(set.registers_v.size()))).lightClone();
						p2.resetNode(state, thread);
						break;
					case readreg:
//						p2 = (GPNode) set.nonconstants_v.get(state.random[thread].nextInt(set.nonconstants_v.size()));
//						p2.resetNode(state, thread);
						p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
	    	                    p1.parentType(initializer),
	    	                    thread,
	    	                    p1.parent,
	    	                    i.getTree(t).constraints(initializer).functionset,
	    	                    p1.argposition,
	    	                    size,
	    	                    p1.atDepth());
						break;
					default:
						break;
					}
                }
            	 
            	 
                else {
                	//no suitable instruction is found, so there is no primitive with the given component type
            		 p1 = nodeselect.pickNode(state,subpopulation,thread,i,i.getTree(t));
            		 p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
	    	                    p1.parentType(initializer),
	    	                    thread,
	    	                    p1.parent,
	    	                    i.getTree(t).constraints(initializer).functionset,
	    	                    p1.argposition,
	    	                    size,
	    	                    p1.atDepth());
            	 }

                // check for depth and swap-compatibility limits
                //res = verifyPoints(p2,p1);  // p2 can fit in p1's spot  -- the order is important!
                
                //easy check
                res = checkPoints(p1, p2, state, thread, i, i.getTreeStruct(t));
                //instance check
                
                // did we get something that had both nodes verified?
                if (res) break;
                }
            
            if (res)  // we've got a tree with a kicking cross position!
            {
	            int x = t;
	            GPTreeStruct tree = j.getTreeStruct(x);
                tree = (GPTreeStruct)(i.getTreeStruct(x).clone());
                tree.owner = j;
                tree.child = i.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
                tree.child.parent = tree;
                tree.child.argposition = 0;
                j.setTree(x, tree);
                j.evaluated = false; 
            } // it's changed
            else{
            	int x = t;
            	GPTreeStruct tree = j.getTreeStruct(x);
        		tree = (GPTreeStruct)(i.getTreeStruct(x).clone());
                tree.owner = j;
                tree.child = (GPNode)(i.getTree(x).child.clone());
                tree.child.parent = tree;
                tree.child.argposition = 0;    
                j.setTree(x, tree);
            }
            
           // j.updateStatus();
            
        }
        
        return j;
	}
	
	protected int getLegalMutateIndex(LGPIndividual ind, EvolutionState state, int thread, int taskid) {
		int res = state.random[thread].nextInt(ind.getTreesLength());

		if(effflag) {//guarantee the effectiveness of the selected instruction
			
			((LGPIndividual_MPMO)ind).updateStatus(taskid);
			
			if(componenttype != cons) {
				for(int x = 0;x<numTries;x++) {
	        		if(ind.getTreeStruct(res).status) break;
	        		res = state.random[thread].nextInt(ind.getTreesLength());
	        	}
				//it is different from the book description here. the modification here helps LGP to transform noneffective instructions
				//into effective ones. the empirical results show it will be better.
			}
			else {
    			for(int x = 0;x<numTries;x++) {
            		if(ind.getTreeStruct(res).status && ind.getTree(res).child.numNodes(GPNode.NODESEARCH_CONSTANT)>0) break;
            		res = state.random[thread].nextInt(ind.getTreesLength());
            	}
    		}
			
			ind.updateStatus();
    	}
		else {
			if (componenttype == cons){
				for(int x = 0;x<numTries;x++) {
	        		if(ind.getTree(res).child.numNodes(GPNode.NODESEARCH_CONSTANT)>0) break;
	        		res = state.random[thread].nextInt(ind.getTreesLength());
	        	}
			}
		}

		return res;
	}
}
