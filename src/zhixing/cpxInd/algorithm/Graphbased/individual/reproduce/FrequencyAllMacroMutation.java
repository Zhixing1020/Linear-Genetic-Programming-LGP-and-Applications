package zhixing.cpxInd.algorithm.Graphbased.individual.reproduce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import zhixing.cpxInd.algorithm.Graphbased.individual.GraphAttributes;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public abstract class FrequencyAllMacroMutation extends FrequencyMacroMutation {
//in this operator, the frequency comes from the whole population
	double topratio = 0.1; //select top "topratio" individuals to obtain Frequency
	
	public LGPIndividual produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final EvolutionState state,
	        final int thread,
	        final LGPIndividual[] parents) {
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		LGPIndividual i = (LGPIndividual) parents[0];
		
		double [] frequency = new double [((GraphAttributes)i).getDimension()];
		
		ArrayList<CpxGPIndividual> sortedPop = new ArrayList<>();
		
		for(Individual ind : state.population.subpops[subpopulation].individuals) {
			sortedPop.add((CpxGPIndividual) ind);
		}
		
		Collections.sort(sortedPop, new FitnessComparotor());
		
		for(int k = 0;k<sortedPop.size() * topratio;k++){
			double [] tmp_fre = ((GraphAttributes)sortedPop.get(k)).getFrequency();
			
			for(int d = 0;d<frequency.length;d++){
	         	
         		frequency[d] += tmp_fre[d];
         		
	         }
		}
		
		for(int d = 0;d<frequency.length;d++){
       	 frequency[d] += 0.1;
        }
		
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
//	                	p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
//	    	                    p1.parentType(initializer),
//	    	                    thread,
//	    	                    p1.parent,
//	    	                    //i.getTree(t).constraints(initializer).functionset,
//	    	                    j.getTree(t).constraints(initializer).functionset,
//	    	                    p1.argposition,
//	    	                    size,
//	    	                    p1.atDepth());
	                	GPTreeStruct tree = (GPTreeStruct) j.getTreeStruct(t).clone();
	                	p2 = sampleInstrBasedFrequency(j,tree,t,0.5,frequency,state,thread, j.getTreeStruct(t).child.parentType(initializer), j.getTree(t).constraints(initializer).functionset);
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
                    j.addTree(x, tree); // because of the logic of identifying effective register set in "sampleInstrBasedFrequency", it's x here rather than x+1
                    j.evaluated = false; 
                } // it's changed
        	}
        	else if (j.getTreesLength() > j.getMinNumTrees() &&(state.random[thread].nextDouble() < probInsert + probDelete || j.getTreesLength() == j.getMaxNumTrees())) {
        		
        		t = getLegalDeleteIndex(j, state, thread);
        		j.removeTree(t);
        		j.evaluated = false; 
        	}
        	
        	//j.updateStatus();
        	else if (j.getTreesLength() == j.getMinNumTrees() && j.getTreesLength() == j.getMaxNumTrees()) {
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
//	                	p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
//	    	                    p1.parentType(initializer),
//	    	                    thread,
//	    	                    p1.parent,
//	    	                    //i.getTree(t).constraints(initializer).functionset,
//	    	                    j.getTree(t).constraints(initializer).functionset,
//	    	                    p1.argposition,
//	    	                    size,
//	    	                    p1.atDepth());
	                	GPTreeStruct tree = (GPTreeStruct) j.getTreeStruct(t).clone();
	                	p2 = sampleInstrBasedFrequency(j,tree,t,0.5,frequency,state,thread, j.getTreeStruct(t).child.parentType(initializer), j.getTree(t).constraints(initializer).functionset);
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
                    j.addTree(x, tree); // because of the logic of identifying effective register set in "sampleInstrBasedFrequency", it's x here rather than x+1
                    j.removeTree(x+1);
                    j.evaluated = false; 
                } // it's changed
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
	
	class FitnessComparotor implements Comparator<CpxGPIndividual>{
		@Override
		public int compare(CpxGPIndividual o1, CpxGPIndividual o2) {
			return (int) Math.signum(o1.fitness.fitness() - o2.fitness.fitness());
		}
		
	}
}
