package zhixing.djss.algorithm.Graphbased.individual.reproduce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public class FrequencyMacroMutation4DJSS extends zhixing.cpxInd.algorithm.Graphbased.individual.reproduce.FrequencyMacroMutation{
//	public void setup(final EvolutionState state, final Parameter base) {
//		super.setup(state, base);
//	}
//	@Override
//	public int produce(final int min, 
//	        final int max, 
//	        final int start,
//	        final int subpopulation,
//	        final Individual[] inds,
//	        final EvolutionState state,
//	        final int thread) {
//		// grab individuals from our source and stick 'em right into inds.
//        // we'll modify them from there
//        int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);
//
//        // should we bother?
//        if (!state.random[thread].nextBoolean(likelihood))
//            return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did
//
//
//        GPInitializer initializer = ((GPInitializer)state.initializer);
//
//        // now let's mutate 'em
//        for(int q=start; q < n+start; q++)
//            {
//        	
//        	LGPIndividual[] parnts = new LGPIndividual[2];
//        	
//        	// grab two individuals from our sources
//        	sources[0].produce(2,2,0,subpopulation,parnts,state,thread);
//        	
//            LGPIndividual i = (LGPIndividual)inds[q];
//
//            inds[q] = this.produce(min, max, start, subpopulation, state, thread, parnts);
//            }
//        return n;
//	}
//	
//	public LGPIndividual produce(final int min, 
//	        final int max, 
//	        final int start,
//	        final int subpopulation,
//	        final EvolutionState state,
//	        final int thread,
//	        final LGPIndividual[] parents) {
//		GPInitializer initializer = ((GPInitializer)state.initializer);
//		
//		LGPIndividual4Graph i = (LGPIndividual4Graph) parents[0];
//		
//		double [] frequency = new double [i.getDimension()];
//		
//		for(int k = 1;k<2;k++){
//			double [] tmp_fre = ((LGPIndividual4Graph)parents[k]).getFrequency();
//			
//			for(int d = 0;d<frequency.length;d++){
//	         	
//         		frequency[d] += tmp_fre[d];
//         		
//	         }
//		}
//		
//		for(int d = 0;d<frequency.length;d++){
//       	 frequency[d] += 0.1;
//        }
//		
//		if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.getTreesLength()))
//            // uh oh
//            state.output.fatal("LGP Mutation Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
//            
//        LGPIndividual4Graph j;
//
//        if (sources[0] instanceof BreedingPipeline)
//            // it's already a copy, so just smash the tree in
//            {
//            j=i;
//            }
//        else // need to clone the individual
//            {
//            j = ((LGPIndividual4Graph)i).lightClone();
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
//        //double pickNum = Math.max(state.random[thread].nextDouble()*(i.getTreesLength()), 1);
//        double pickNum = state.random[thread].nextInt(stepSize) + 1.0;
//        for(int pick = 0;pick<pickNum;pick++){
//        	int t = state.random[thread].nextInt(j.getTreesLength());
//        	
//        	//if insert a new instruction
//        	if(j.getEffTreesLength() < j.getMaxNumTrees() && ( state.random[thread].nextDouble() < probInsert || j.getTreesLength() == j.getMinNumTrees())) {
//        		
//        		if(j.getEffTreesLength() < j.getMaxNumTrees() && j.getTreesLength() >= j.getMaxNumTrees()){
//        			int res = state.random[thread].nextInt(j.getTreesLength());
//        			for(int x = 0;x<numTries;x++) {
//                		if(!j.getTreeStruct(res).status) {
//                			break;
//                		}
//                		res = state.random[thread].nextInt(j.getTreesLength());
//                	}
//        			j.removeTree(res);
//        		}
//        		
//        		t = getLegalInsertIndex(j, state, thread);
//        		
//        		// validity result...
//	            boolean res = false;
//	            
//	            // prepare the nodeselector
//	            nodeselect.reset();
//	            
//	            // pick a node
//	            
//	            //GPNode p1=i.getTree(t).child;  // the node we pick
//	            GPNode p1=j.getTree(t).child; 
//	            GPNode p2=null;
//	            
//	            for(int x=0;x<numTries;x++)
//	                {    	                
//	                // generate a tree swap-compatible with p1's position
//	                
//	                
//	                int size = GPNodeBuilder.NOSIZEGIVEN;
//	                if (equalSize) size = p1.numNodes(GPNode.NODESEARCH_ALL);
//	                
//	                if(builder instanceof LGPMutationGrowBuilder) {
////	                	p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
////	    	                    p1.parentType(initializer),
////	    	                    thread,
////	    	                    p1.parent,
////	    	                    //i.getTree(t).constraints(initializer).functionset,
////	    	                    j.getTree(t).constraints(initializer).functionset,
////	    	                    p1.argposition,
////	    	                    size,
////	    	                    p1.atDepth());
//	                	
//	                	p2 = sampleInstrBasedFrequency(j,j.getTreeStruct(t),t,0.5,frequency,state,thread, j.getTreeStruct(t).child.parentType(initializer), j.getTree(t).constraints(initializer).functionset);
//	                }
//	                else {
//	                	 p2 = builder.newRootedTree(state,
//	     	                    p1.parentType(initializer),
//	     	                    thread,
//	     	                    p1.parent,
//	     	                    //i.getTree(t).constraints(initializer).functionset,
//	     	                    j.getTree(t).constraints(initializer).functionset,
//	     	                    p1.argposition,
//	     	                    size);
//					}
//	               
//	                
//	                // check for depth and swap-compatibility limits
//	                //res = verifyPoints(p2,p1);  // p2 can fit in p1's spot  -- the order is important!
//	                res = checkPoints(p1, p2, j.getTreeStruct(t));
//	                
//	                // did we get something that had both nodes verified?
//	                if (res) break;
//	                }
//	            
//	            //if (res)  // we've got a tree with a kicking cross position!
//                {
//		            int x = t;
//		            //GPTree tree = j.getTree(x);
//                    //tree = (GPTree)(i.getTree(x).lightClone());
//		            GPTreeStruct tree = (GPTreeStruct) j.getTreeStruct(x).clone();
//                    tree.owner = j;
//                    //tree.child = i.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
//                    tree.child = j.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
//                    tree.child.parent = tree;
//                    tree.child.argposition = 0;
//                    j.addTree(x, tree); // because of the logic of identifying effective register set in "sampleInstrBasedFrequency", it's x here rather than x+1
//                    j.evaluated = false; 
//                } // it's changed
//        	}
//        	else if (j.getTreesLength() > j.getMinNumTrees() &&(state.random[thread].nextDouble() < probInsert + probDelete || j.getTreesLength() == j.getMaxNumTrees())) {
//        		
//        		t = getLegalDeleteIndex(j, state, thread);
//        		j.removeTree(t);
//        		j.evaluated = false; 
//        	}
//        	
//        	//j.updateStatus();
//        	
//        }
//        
//        
//        //some following operations
//        switch (mutateFlag) {
//		case EFFMACROMUT2:
//			break;
//		case EFFMACROMUT:
//		case FREEMACROMUT:
//			if(microMutation != null) j = (LGPIndividual4Graph) microMutation.produce(subpopulation, j, state, thread);
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
	
	@Override
	protected GPNode sampleInstrBasedFrequency(
			LGPIndividual offspring,
			GPTreeStruct tree,
			int insert, 
			//double effRate, 
			double consRate,
			double [] frequency, 
			//double [][] normAM,
			EvolutionState state, 
			int thread,
			final GPType type,
			//final GPNodeParent parent,
	        //final int argposition,
	        final GPFunctionSet set) {
		
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		 int t = type.type;
	     GPNode[] terminals = set.terminals[t];
	     GPNode[] functions = set.nonterminals[t];
	     GPNode[] nodes = set.nodes[t];  
	     GPNode[] registers = set.registers[t];
	     GPNode[] nonregisters = set.nonregisters[t];
	     GPNode[] constants = set.constants[t]; //only
	     GPNode[] nonconstants = set.nonconstants[t]; // the terminals but not constants
	     

	     if(insert - 1 >= 0) {
	    	 tree.effRegisters = offspring.getTreeStruct(insert - 1).effRegisters;
	     }
	     else {
	    	 tree.effRegisters = offspring.getTreeStruct(0).effRegisters;
	    	 
	    	 //remove WriteRegister and update ReadRegister
	    	 if(offspring.getTreeStruct(0).status) {
	    		 tree.effRegisters.remove(((WriteRegisterGPNode) offspring.getTreeStruct(0).child).getIndex());
				 tree.effRegisters.addAll(offspring.getTreeStruct(0).collectReadRegister());
	    	 }
	     }
	     
	     if (nodes.length == 0) {// total failure
	    	 System.err.print("there is no primitives in AMCrossover\n");
	    	 System.exit(1);
	     }
	     
	     final int trial = 10;
	     
	     boolean res = false;
		 
		 //effective or not?
	     GPNode root;

		 { //extron
			//1. randomly select an effective register
			 root = (GPNode)(registers[state.random[thread].nextInt(registers.length)].lightClone());
			 root.resetNode(state,thread);  
	         //root.argposition = (byte)argposition;
	         //root.parent = tree;
	         
			 int tri = trial;
			 while((/*tree.status &&*/ !tree.effRegisters.contains(((WriteRegisterGPNode)root).getIndex()))
					// && (!tree.status && tree.effRegisters.contains(((WriteRegisterGPNode)root).getIndex()))
					 && tri>0) {
				 root.resetNode(state,thread);  
				 tri --;
			 }
			 
			 tree.child.replaceWith(root);
			 root = tree.child;
			 
			 
			 	// Populate the node...
			 //GPType[] childtypes = root.constraints(((GPInitializer)state.initializer)).childtypes;
			 
			//2. select functions based on the AM. identify its parent based on the effective register.
			if (functions==null || functions.length ==0)                            // no nonterminals, hope the guy knows what he's doing!
            {    //functions = set.terminals[type.type];                                 // this can only happen with the warning about nonterminals above
            	System.err.print("cannot collect functions in AMCrossover\n");
            	System.exit(1);
            }
			
			int functionInd = sampleNextFunBasedFrequency(frequency,  functions.length, functions.length+constants.length, state, thread);
			
			GPNode n = (GPNode)(functions[functionInd].lightClone());
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            //tree.child = tree.child.cloneReplacingNoSubclone(n, tree.child.children[0]);
//            tree.child.children[0].replaceWith(n);
            //n.argposition = (byte)argposition;
            //n.parent = tree;
            
            if(tree.child.children[0].constraints(initializer) == n.constraints(initializer)) {
         	   tree.child.children[0].replaceWith(n);
            }
            else {        	   
         	   // Populate the node...
                GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
                for(int x=0;x<childtypes.length;x++) {
             	   if(builder instanceof LGPMutationGrowBuilder) {
             		   n.children[x] = ((LGPMutationGrowBuilder)builder).newRootedTree(state, childtypes[x], thread, n, set, x, GPNodeBuilder.NOSIZEGIVEN, tree.child.children[0].atDepth()+1);
             	   }
             	   else {
             		   n.children[x] = builder.newRootedTree(state, childtypes[x], thread, n, set, x, GPNodeBuilder.NOSIZEGIVEN);
             	   }
                }
                    
                
                tree.child.children[0].replaceWith(n);
            }
            
          //3. randomly select registers.
            GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
            
            List<Integer> index = new ArrayList<>();
            for(int it = 0;it<childtypes.length;it++){
            	index.add(it);
            }
            for(int it = 0; it<index.size();it++) {
            	Collections.swap(index, it, state.random[thread].nextInt(index.size()));
            }
            
            for(int it=0;it<childtypes.length;it++) {
            	int x = index.get(it);
            	GPNode m;
            	
            	if(state.random[thread].nextDouble()<consRate && ((LGPMutationGrowBuilder)builder).canAddConstant(n)) {
            		//find the index of its parent
//            		parentInd = 0;
//            		for(int f = 0;f<functions.length;f++) {
//            			if(functions[f].toString().equals(n.toString())) {
//            				parentInd = f;
//            				break;
//            			}
//            		}
            		
            		int constantInd = sampleConsBasedFrequency(frequency, ((LGPIndividual4Graph)offspring).getDimension_fun(), ((LGPIndividual4Graph)offspring).getDimension(), state, thread);
            		m = (GPNode)((GPNode) set.constants_v.get(state.random[thread].nextInt(set.constants_v.size()))).lightClone();
            		((TerminalERC)m).setTerminal(((GPRuleEvolutionState)state).getTerminals().get(constantInd));
            		
            	}
            	else {
            		m = (GPNode)(nonconstants[state.random[thread].nextInt(nonconstants.length)].lightClone());
            		 m.resetNode(state,thread);
            	}
                
//                m.argposition = (byte)argposition;
//                m.parent = n;
                
                //tree.child.cloneReplacingNoSubclone(m, n.children[x]);
                n.children[x].replaceWith(m);
            }
			
			//find its parent function and the index of the parent function
//			int wr = ((WriteRegisterGPNode) root).getIndex();
//			String name = null;
//			for(int ins = insert+1;ins<offspring.getTreesLength();ins++) {
//				if(/*tree.status == */ offspring.getTreeStruct(ins).status && offspring.getTreeStruct(ins).collectReadRegister().contains(wr)){
//					name = offspring.getTreeStruct(ins).child.children[0].toString();
//					break;
//				}
//			}
//			int parentInd = -1; // state.random[thread].nextInt(functions.length);
//			if(name != null) {
//				for(int f = 0;f<functions.length;f++) {
//	    			if(functions[f].toString().equals(name)) {
//	    				parentInd = f; 
//	    				break;
//	    			}
//	    		}
//			}
//			else{
//				parentInd =state.random[thread].nextInt(functions.length);
//			}
//			int functionInd;
//			if(parentInd > 0) {
//				//functionInd = sampleNextFunBasedAM(parentInd, AM,  functions.length, functions.length+constants.length, state, thread);
//					
//	            
//			}
			
//			else {
//				functionInd = sampleFunBasedAMOutDegree(AM, functions.length, functions.length+constants.length, state, thread);
//			}
//			
//			GPNode n = (GPNode)(functions[functionInd].lightClone());
//            n.resetNode(state,thread);  // give ERCs a chance to randomize
//            tree.child = tree.child.cloneReplacingNoSubclone(n, tree.child.children[0]);
//            n.argposition = (byte)argposition;
//            n.parent = tree;
//			 
//            //GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
//			 
//			//3. randomly select registers.
//            List<Integer> index = new ArrayList<>();
//            for(int it = 0;it<childtypes.length;it++){
//            	index.add(it);
//            }
//            for(int it = 0; it<index.size();it++) {
//            	Collections.swap(index, it, state.random[thread].nextInt(index.size()));
//            }
//            
//            for(int it=0;it<childtypes.length;it++) {
//            	int x = index.get(it);
//            	GPNode m = n.children[x];
//            	
//            	if(state.random[thread].nextDouble()<consRate && builder.canAddConstant(n)) {
//            		//find the index of its parent
//            		parentInd = 0;
//            		for(int f = 0;f<functions.length;f++) {
//            			if(functions[f].toString().equals(n.toString())) {
//            				parentInd = f;
//            				break;
//            			}
//            		}
//            		
//            		int constantInd = sampleConsBasedAM(parentInd, AM, functions.length, functions.length+constants.length, state, thread);
//            		m = (GPNode)(constants[constantInd].lightClone());
//            	}
//            	else {
//            		m = (GPNode)(nonconstants[state.random[thread].nextInt(nonconstants.length)].lightClone());
//            	}
//                
//                m.resetNode(state,thread);  // give ERCs a chance to randomize
//                m.argposition = (byte)argposition;
//                m.parent = n;
//            }	
            
		 }
		 
		 
		 return root;
	}
	
//	protected int sampleNextFunBasedFrequency(double []frequency,  int dimension1, int dimension2, EvolutionState state, int thread) {
//		//dimension1: number of functions, dimension2: number of fun + constant
//		int res = state.random[thread].nextInt(dimension1);
//		
//		double fre []=new double [dimension1];
//		for(int d = 0;d<dimension1;d++) {
//			fre[d] = frequency[d];
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
//	protected int sampleConsBasedFrequency(double []frequency,  int dimension1, int dimension2, EvolutionState state, int thread) {
//		int res = state.random[thread].nextInt(dimension2 - dimension1);
//		
//		double fre []=new double [dimension2 - dimension1];
//		for(int d = dimension1;d<dimension2;d++) {
//			fre[d-dimension1] = frequency[d];
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
}
