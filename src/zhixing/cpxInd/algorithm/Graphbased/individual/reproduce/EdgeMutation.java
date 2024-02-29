package zhixing.cpxInd.algorithm.Graphbased.individual.reproduce;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public class EdgeMutation extends zhixing.cpxInd.individual.reproduce.LGPMicroMutationPipeline{
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
		int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);
		
        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        for(int q=start, parnt = 0;q<n+start; q++)  // keep on going until we're filled up
            {
        	
        	LGPIndividual[] parnts = new LGPIndividual[2];
        	
            // grab two individuals from our sources
        	sources[0].produce(2,2,0,subpopulation,parnts,state,thread);
        	
            inds[q] = this.produce(min, max, start, subpopulation, state, thread, parnts);
            }
            
        return n;
        }
	
	public LGPIndividual produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final EvolutionState state,
	        final int thread,
	        final LGPIndividual[] parents)  {
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		LGPIndividual4Graph i = (LGPIndividual4Graph) parents[0];
         
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

        LGPIndividual4Graph j;

        if (sources[0] instanceof BreedingPipeline)
            // it's already a copy, so just smash the tree in
            {
            j=(LGPIndividual4Graph) i;
            }
        else // need to clone the individual
            {
            j = ((LGPIndividual4Graph)i).lightClone();
            
            // Fill in various tree information that didn't get filled in there
            //j.renewTrees();
            }
        
        //double pickNum = Math.max(state.random[thread].nextDouble()*(i.getTreesLength()), 1);
        double pickNum = state.random[thread].nextInt(stepSize) + 1.0;
        for(int pick = 0;pick<pickNum;pick++){
        	int t = getLegalMutateIndex(j, state, thread);
        	
        	int ref_width = parents[1].getApproximateGraphWidth(t);
    		int actual_width = j.getApproximateGraphWidth(t);
    		
    		boolean increase_width_flag = false; 
    		if(ref_width > actual_width) {
    			increase_width_flag = true;
    		}

        	
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
            	
                int type = i.getTreeStruct(t).child.parentType(initializer).type;
                
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
						
						//p2.resetNode(state, thread);
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
            
            
            //connect the newly mutated registers
            if(p2 instanceof WriteRegisterGPNode) {
            	connectWriteRegister(j, t, (WriteRegisterGPNode)p2, state, thread);
            }
            if(p2 instanceof ReadRegisterGPNode) {
            	connectReadRegister(j, t, (ReadRegisterGPNode)p2, state, thread);
            }
            
        }
        
        return j;
	}
	
//	public LGPIndividual produce(final int min, 
//	        final int max, 
//	        final int start,
//	        final int subpopulation,
//	        final EvolutionState state,
//	        final int thread,
//	        final LGPIndividual[] parents)  {
//		GPInitializer initializer = ((GPInitializer)state.initializer);
//		
//		LGPIndividual4Graph i = (LGPIndividual4Graph) parents[0];
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
//        LGPIndividual4Graph j;
//
//        if (sources[0] instanceof BreedingPipeline)
//            // it's already a copy, so just smash the tree in
//            {
//            j=(LGPIndividual4Graph) i;
//            }
//        else // need to clone the individual
//            {
//            j = ((LGPIndividual4Graph)i).lightClone();
//            
//            // Fill in various tree information that didn't get filled in there
//            //j.renewTrees();
//            }
//        
//        //double pickNum = Math.max(state.random[thread].nextDouble()*(i.getTreesLength()), 1);
//        double pickNum = state.random[thread].nextInt(stepSize) + 1.0;
//        for(int pick = 0;pick<pickNum;pick++){
//        	int t = getLegalMutateIndex(j, state, thread);
//        	
//        	int ref_width = parents[1].getApproximateGraphWidth(t);
//    		int actual_width = j.getApproximateGraphWidth(t);
//    		
//    		boolean increase_width_flag = false; 
//    		if(ref_width > actual_width) {
//    			increase_width_flag = true;
//    		}
//
//        	
//        	// pick random tree
//            if (tree!=TREE_UNFIXED)
//                t = tree;
//            
//
//            // validity result...
//            boolean res = false;
//            
//            // prepare the nodeselector
//            nodeselect.reset();
//            
//            // pick a node
//            
//            GPNode p1=null;  // the node we pick
//            GPNode p2=null;
//            int cnt = 0; //the number of primitives that satisfies the given component type
//            int cntdown = 0;
//            GPTree oriTree = i.getTree(t);
//            int flag = -1;//wheter it need to reselect the p1
//            
//            switch (componenttype) {
//			case functions:
//				flag = GPNode.NODESEARCH_NONTERMINALS;
//				break;
//			case cons:
//				flag = GPNode.NODESEARCH_CONSTANT;
//				break;
//			case writereg:
//				flag = -1;
//				cnt = 1;
//				p1 = oriTree.child;
//				break;
//			case readreg:
//				flag = GPNode.NODESEARCH_READREG;
//				break;
//			default:
//				break;
//			}
//            if (flag >=0) cnt = oriTree.child.numNodes(flag);
//
//            if(cnt>0){
//            	if(componenttype == functions || componenttype == cons){
//            		
//            		for(int x=0;x<numTries;x++)
//                    {
//	                	// pick a node in individual 1
//	                	//p1 = nodeselect.pickNode(state,subpopulation,thread,i,i.getTree(t));
//	                	if(flag>=0 && cnt >0) p1 = oriTree.child.nodeInPosition(state.random[thread].nextInt(cnt),flag);
//	                		
//	                	
//	                	int size = GPNodeBuilder.NOSIZEGIVEN;
//	                    if (equalSize) size = p1.numNodes(GPNode.NODESEARCH_ALL);
//	                	
//	                    int type = i.getTreeStruct(t).child.parentType(initializer).type;
//	                    
//	                    if(cnt > 0) {
//	                    	switch (componenttype) {
//	                    	case functions:
//	    						p2 = (GPNode)((GPNode) set.nonterminals_v.get(state.random[thread].nextInt(set.nonterminals_v.size()))).lightClone();
//	    						p2.resetNode(state, thread);
//	    						break;
//	    					case cons:
//	    						if(state.random[thread].nextDouble()<((LGPMutationGrowBuilder)builder).probCons) {
//	    							p2 = (GPNode)((GPNode) set.constants_v.get(state.random[thread].nextInt(set.constants_v.size()))).lightClone();
//	    							p2.resetNode(state, thread);
//	    						}
//	    						else {
//	    							p2 = (GPNode)((GPNode) set.nonconstants_v.get(state.random[thread].nextInt(set.nonconstants_v.size()))).lightClone();
//	    							p2.resetNode(state, thread);
//	    						}
//	    						break;
//	    					
//	    					default:
//	    						break;
//	    					}
//	                    }
//	                	 
//	                	 
//	                    else {
//	                    	//no suitable instruction is found, so there is no primitive with the given component type
//	                		 p1 = nodeselect.pickNode(state,subpopulation,thread,i,i.getTree(t));
//	                		 p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
//	    	    	                    p1.parentType(initializer),
//	    	    	                    thread,
//	    	    	                    p1.parent,
//	    	    	                    i.getTree(t).constraints(initializer).functionset,
//	    	    	                    p1.argposition,
//	    	    	                    size,
//	    	    	                    p1.atDepth());
//	                	 }
//	
//	                    // check for depth and swap-compatibility limits
//	                    //res = verifyPoints(p2,p1);  // p2 can fit in p1's spot  -- the order is important!
//	                    
//	                    //easy check
//	                    res = checkPoints(p1, p2, state, thread, i, i.getTreeStruct(t));
//	                    //instance check
//	                    
//	                    // did we get something that had both nodes verified?
//	                    if (res) break;
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
//            		
//            	}
//            	else{  //componenttype == writereg || readreg
//            		
//            		
////            	case writereg:
////					p2 = (GPNode)((GPNode) set.registers_v.get(state.random[thread].nextInt(set.registers_v.size()))).lightClone();
////					p2.resetNode(state, thread);
////					break;
////				case readreg:
////					//p2 = (GPNode) set.nonconstants_v.get(state.random[thread].nextInt(set.nonconstants_v.size()));
////					
////					//p2.resetNode(state, thread);
////					p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
////    	                    p1.parentType(initializer),
////    	                    thread,
////    	                    p1.parent,
////    	                    i.getTree(t).constraints(initializer).functionset,
////    	                    p1.argposition,
////    	                    size,
////    	                    p1.atDepth());
////					
////					break;
//            		
//            		
//            	}
//            }
//            
//            
//            
//           // j.updateStatus();
//            
//        }
//        
//        return j;
//	}
	
	protected void mutateEdge(LGPIndividual4Graph ind, int instr, EvolutionState state, int thread){
		
		
		
		//randomly select a start point (read register)
		int numchild = ind.getTreeStruct(instr).child.children[0].children.length;
		int tmp = state.random[thread].nextInt(numchild);
		
		GPNode start = ind.getTreeStruct(instr).child.children[0].children[tmp];
		if( ! (start instanceof ReadRegisterGPNode)){
			start = ind.getTreeStruct(instr).child.children[0].children[ (tmp + 1) % (numchild)];
		}
		
		if(instr == 0){
			if (componenttype == writereg){
				((WriteRegisterGPNode)ind.getTreeStruct(instr).child).resetNode(state, thread);
			}
			else{
				((ReadRegisterGPNode)start).resetNode(state, thread);
			}
		}
		
		//collect all instructions before this instruction in genome, and randomly select one of them
		//if the write register of the selected effective instruction already connects with the start point, reselect.
		int end_ins = state.random[thread].nextInt(instr);
		boolean reselect = false;
		int cnt = 10;
		
		do{
			if(reselect) end_ins = state.random[thread].nextInt(instr);
			for(int j = instr - 1; j>=end_ins; j--){
				if(((WriteRegisterGPNode)ind.getTreeStruct(j).child).getIndex() == ((ReadRegisterGPNode)start).getIndex()
						&& j > end_ins){ //the write register of the selected effective instruction already connects
					break;
				}
				if(((WriteRegisterGPNode)ind.getTreeStruct(j).child).getIndex() == ((ReadRegisterGPNode)start).getIndex()
						&& j == end_ins){
					reselect=true; 
					break;
				}
			}
			cnt --;
		}while(reselect && cnt > 0);
		
		
		//check which registers are not used in the whole program, randomly select one of them and set the index.
	}
	
	protected void connectReadRegister(LGPIndividual4Graph offspring, int instr, ReadRegisterGPNode p2, EvolutionState state, int thread) {
		//randomly select one instruction or initial register
		
//		int trial = state.random[thread].nextInt(instr+1)-1;
//		
//		//if trial == -1, it selects initial registers and skip the connection
//		if(trial>=0) {
//			//((WriteRegisterGPNode)offspring.getTreeStruct(trial).child).setIndex(p2.getIndex());
//			p2.setIndex(((WriteRegisterGPNode)offspring.getTreeStruct(trial).child).getIndex());
//		}
		
		p2.resetNode(state, thread);
		
		offspring.updateStatus();
	}
	
	protected void connectWriteRegister(LGPIndividual4Graph offspring, int instr, WriteRegisterGPNode p2, EvolutionState state, int thread) {
		//if there is no more instruction following "instr" instruction, skip the connection
		if(instr + 1 < offspring.getTreesLength()) {
			//randomly select one instruction 
			
			int trial = state.random[thread].nextInt(offspring.getTreesLength() - (1 + instr)) + (1 + instr);
			int cnt = 10;
			
//			while(!offspring.getTreeStruct(trial).status && cnt > 0) {
//				trial = state.random[thread].nextInt(offspring.getTreesLength() - (1 + instr)) + (1 + instr);
//				cnt --;
//			}
//			
//			//find trial's read register
//			int t = state.random[thread].nextInt(offspring.getTreeStruct(trial).child.children[0].expectedChildren());
//			while(!(offspring.getTreeStruct(trial).child.children[0].children[t] instanceof ReadRegisterGPNode)) {
//				t = (t + 1)%offspring.getTreeStruct(trial).child.children[0].expectedChildren();
//			}
//			
//			((ReadRegisterGPNode)offspring.getTreeStruct(trial).child.children[0].children[t]).setIndex(p2.getIndex());
//			
//			offspring.updateStatus();
			
			//while(offspring.getTreeStruct(instr).status && cnt>0) 
			{
				trial = state.random[thread].nextInt(offspring.getTreesLength() - (1 + instr)) + (1 + instr);
				
				//find trial's read register
				int t = state.random[thread].nextInt(offspring.getTreeStruct(trial).child.children[0].expectedChildren());
				while(!(offspring.getTreeStruct(trial).child.children[0].children[t] instanceof ReadRegisterGPNode)) {
					t = (t + 1)%offspring.getTreeStruct(trial).child.children[0].expectedChildren();
				}
				
				//((ReadRegisterGPNode)offspring.getTreeStruct(trial).child.children[0].children[t]).setIndex(p2.getIndex());
				//p2.setIndex(((ReadRegisterGPNode)offspring.getTreeStruct(trial).child.children[0].children[t]).getIndex());
				p2.resetNode(state, thread);
				
				offspring.updateStatus();
				
				cnt--;
			}

		}
		
	}
}
