package zhixing.djss.algorithm.Graphbased.individual.reproduce;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import ec.gp.GPType;
import ec.util.Parameter;
import yimei.jss.gp.GPRuleEvolutionState;
//import yimei.jss.gp.terminal.JobShopAttribute;
import yimei.jss.gp.terminal.TerminalERC;
import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public class AMMicroMutation4DJSS extends zhixing.cpxInd.algorithm.Graphbased.individual.reproduce.AMMicroMutation{
//	
//	public void setup(final EvolutionState state, final Parameter base) {
//		super.setup(state, base);
//	}
//	
//	@Override
//    public int produce(final int min, 
//        final int max, 
//        final int start,
//        final int subpopulation,
//        final Individual[] inds,
//        final EvolutionState state,
//        final int thread) 
//
//        {
//        // how many individuals should we make?
//		int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);
//		
//        // should we bother?
//        if (!state.random[thread].nextBoolean(likelihood))
//            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already
//
//
//
//        GPInitializer initializer = ((GPInitializer)state.initializer);
//        
//        for(int q=start, parnt = 0;q<n+start; q++)  // keep on going until we're filled up
//            {
//        	
//        	LGPIndividual[] parnts = new LGPIndividual[2];
//        	
//            // grab two individuals from our sources
//        	sources[0].produce(2,2,0,subpopulation,parnts,state,thread);
//        	
//            inds[q] = this.produce(min, max, start, subpopulation, state, thread, parnts);
//            }
//            
//        return n;
//        }
	
	@Override
	public LGPIndividual produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final EvolutionState state,
	        final int thread,
	        final LGPIndividual[] parents)  {
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		LGPIndividual4Graph i = (LGPIndividual4Graph) parents[0];
		
		double [][] AM = new double [i.getDimension()][i.getDimension()];
		
		for(int k = 1;k<2;k++){
			double [][] tmp_AM = ((LGPIndividual4Graph)parents[k]).getAM();
			
			for(int d = 0;d<AM.length;d++){
	         	for(int dd = 0;dd<AM[d].length;dd++){
	         		AM[d][dd] += tmp_AM[d][dd];
	         		//sum1 += AM1[d][dd];
	         	}
//	         	for(int dd = 0;dd<dimen;dd++){
//	         		normAM1[d][dd] = AM1[d][dd] / sum1;
//	         	}
	         }
		}
		
		//double [][] AM = ((LGPIndividual4Graph)parents[1]).getAM();

         for(int d = 0;d<AM.length;d++){
         	for(int dd = 0;dd<AM[d].length;dd++){
         		AM[d][dd] += 1;
         		//sum1 += AM1[d][dd];
         	}
//         	for(int dd = 0;dd<dimen;dd++){
//         		normAM1[d][dd] = AM1[d][dd] / sum1;
//         	}
         }
         
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
                		
                		GPTreeStruct tree = i.getTreeStruct(t);
                		
                		int functionInd = getFunIndexBasedAM(i, t, AM, state, thread, tree.child.parentType(initializer),  set);
                				
						p2 = (GPNode)((GPNode) set.nonterminals_v.get(functionInd)).lightClone();
						
						p2.resetNode(state, thread);
						break;
					case cons:
						if(state.random[thread].nextDouble()<((LGPMutationGrowBuilder)builder).probCons) {
							
							//find the index of its parent
		            		int parentInd = 0;
		            		GPTreeStruct tree2 = i.getTreeStruct(t);
		            		int type = tree2.child.parentType(initializer).type;
		            		for(int f = 0;f<set.nonterminals[type].length;f++) {
		            			if(set.nonterminals[type][f].toString().equals(tree2.child.children[0].toString())) {
		            				parentInd = f;
		            				break;
		            			}
		            		}
							
		            		int constantInd = sampleBasedAM(parentInd, AM, i.getDimension_fun(), i.getDimension(), state, thread);
		            		//p2 = (GPNode)(((GPRuleEvolutionState)state).getTerminals().get(constantInd).lightClone());
		            		p2 = (GPNode)((GPNode) set.constants_v.get(state.random[thread].nextInt(set.constants_v.size()))).lightClone();
		            		((TerminalERC)p2).setTerminal(((GPRuleEvolutionState)state).getTerminals().get(constantInd));
		            		
//							p2 = (GPNode)((GPNode) set.constants_v.get(state.random[thread].nextInt(set.constants_v.size()))).lightClone();
//							p2.resetNode(state, thread);
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
//	
//	protected int getFunIndexBasedAM(LGPIndividual4Graph ind, 
//			int insert,
//			double [][] AM, 
//			EvolutionState state, 
//			int thread,
//			final GPType type,
//	        final GPFunctionSet set) {
//		int t = type.type;
//	    GPNode[] functions = set.nonterminals[t];
//	    //GPNode[] constants = set.constants[t];
//	    
//	    GPTreeStruct tree = ind.getTreeStruct(insert);
//	    
//	    GPNode root = tree.child;
//	    
//	    if (functions==null || functions.length ==0)                            // no nonterminals, hope the guy knows what he's doing!
//        {    //functions = set.terminals[type.type];                                 // this can only happen with the warning about nonterminals above
//        	System.err.print("cannot collect functions in AMCrossover\n");
//        	System.exit(1);
//        }
//	    
//	    int wr = ((WriteRegisterGPNode) root).getIndex();
//		String name = null;
//		for(int ins = insert+1;ins<ind.getTreesLength();ins++) {
//			if(/*tree.status == */ ind.getTreeStruct(ins).status && ind.getTreeStruct(ins).collectReadRegister().contains(wr)){
//				name = ind.getTreeStruct(ins).child.children[0].toString();
//				break;
//			}
//		}
//		int parentInd = -1; // state.random[thread].nextInt(functions.length);
//		if(name != null) {
//			for(int f = 0;f<functions.length;f++) {
//    			if(functions[f].toString().equals(name)) {
//    				parentInd = f;
//    				break;
//    			}
//    		}
//		}
//		else{
//			parentInd =state.random[thread].nextInt(functions.length);
//		}
//		int functionInd;
//	    
//	    functionInd = sampleBasedAM(parentInd, AM,  ind.getStartEnd_fun()[0], ind.getStartEnd_fun()[1], state, thread);
//	    
//	    return functionInd;
//	}
//	
//	
//	protected int sampleNextFunBasedAM(int parentInd, double [][]AM,  int dimension1, int dimension2, EvolutionState state, int thread) {
//		//dimension1: number of functions, dimension2: number of fun + constant
//		int res = state.random[thread].nextInt(dimension1);
//		
//		double fre []=new double [dimension1];
//		for(int d = 0;d<dimension1;d++) {
//			fre[d] = AM[parentInd][d];
//		}
//		
//		double sum = 0;
//		for(int i = 0;i<dimension1;i++) {
//			sum+=fre[i];
//		}
//		for(int i = 0;i<dimension1;i++) {
//			fre[i] /= sum;
//		}
//		
//		double prob = state.random[thread].nextDouble();
//		double tmp = 0;
//		for(int f = 0;f<dimension1;f++) {
//			tmp += fre[f];
//			if(tmp>prob) {
//				res = f;
//				break;
//			}
//		}
//		
//		return res;
//	}
//	
//	protected int sampleConsBasedAM(int parentInd, double [][]AM,  int dimension1, int dimension2, EvolutionState state, int thread) {
//		int res = state.random[thread].nextInt(dimension2 - dimension1);
//		
//		double fre []=new double [dimension2 - dimension1];
//		for(int d = dimension1;d<dimension2;d++) {
//			fre[d-dimension1] = AM[parentInd][d];
//		}
//		
//		double sum = 0;
//		for(int i = 0;i<dimension2-dimension1;i++) {
//			sum+=fre[i];
//		}
//		for(int i = 0;i<dimension2-dimension1;i++) {
//			fre[i] /= sum;
//		}
//		
//		double prob = state.random[thread].nextDouble();
//		double tmp = 0;
//		for(int f = 0;f<dimension2-dimension1;f++) {
//			tmp += fre[f];
//			if(tmp>prob) {
//				res = f;
//				break;
//			}
//		}
//		
//		return res;
//	}
//	
//	protected int sampleBasedAM(int parentInd, double [][]AM,  int start, int end, EvolutionState state, int thread) {
//		//start: index of start visiting, end: index of stop visiting
//		int res = state.random[thread].nextInt(end - start);
//		
//		double fre []=new double [end - start];
//		for(int d = start;d<end;d++) {
//			fre[d-start] = AM[parentInd][d];
//		}
//		
//		double sum = 0;
//		for(int i = 0;i<end - start;i++) {
//			sum+=fre[i];
//		}
//		for(int i = 0;i<end - start;i++) {
//			fre[i] /= sum;
//		}
//		
//		double prob = state.random[thread].nextDouble();
//		double tmp = 0;
//		for(int f = 0;f<end - start;f++) {
//			tmp += fre[f];
//			if(tmp>=prob) {
//				res = f;
//				break;
//			}
//		}
//		
//		return res;
//	}
}
