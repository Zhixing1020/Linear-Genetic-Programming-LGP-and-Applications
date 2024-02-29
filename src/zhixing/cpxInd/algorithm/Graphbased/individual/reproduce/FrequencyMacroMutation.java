package zhixing.cpxInd.algorithm.Graphbased.individual.reproduce;

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
import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public abstract class FrequencyMacroMutation extends zhixing.cpxInd.individual.reproduce.LGPMacroMutationPipeline{
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
	}
	@Override
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

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did


        GPInitializer initializer = ((GPInitializer)state.initializer);

        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
            {
        	
        	LGPIndividual[] parnts = new LGPIndividual[2];
        	
        	// grab two individuals from our sources
        	sources[0].produce(2,2,0,subpopulation,parnts,state,thread);
        	
            LGPIndividual i = (LGPIndividual)inds[q];

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
	        final LGPIndividual[] parents) {
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		LGPIndividual4Graph i = (LGPIndividual4Graph) parents[0];
		
		double [] frequency = new double [i.getDimension()];
		
		for(int k = 1;k<2;k++){
			double [] tmp_fre = ((LGPIndividual4Graph)parents[k]).getFrequency();
			
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
        	if(j.getEffTreesLength() < j.getMaxNumTrees() && ( state.random[thread].nextDouble() < probInsert || j.getTreesLength() == j.getMinNumTrees())) {
        		
        		if(j.getEffTreesLength() < j.getMaxNumTrees() && j.getTreesLength() >= j.getMaxNumTrees()){
        			int res = state.random[thread].nextInt(j.getTreesLength());
        			for(int x = 0;x<numTries;x++) {
                		if(!j.getTreeStruct(res).status) {
                			break;
                		}
                		res = state.random[thread].nextInt(j.getTreesLength());
                	}
        			j.removeTree(res);
        		}
        		
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
        	else if(j.getTreesLength() == j.getMinNumTrees() && j.getTreesLength() == j.getMaxNumTrees()) {
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
	
	
	abstract protected GPNode sampleInstrBasedFrequency(
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
	        final GPFunctionSet set);
	
	protected int sampleNextFunBasedFrequency(double []frequency,  int dimension1, int dimension2, EvolutionState state, int thread) {
		//dimension1: number of functions, dimension2: number of fun + constant
		int res = state.random[thread].nextInt(dimension1);
		
		double fre []=new double [dimension1];
		for(int d = 0;d<dimension1;d++) {
			fre[d] = frequency[d];
		}
		
		double sum = 0;
		for(int i = 0;i<dimension1;i++) {
			sum+=fre[i];
		}
		for(int i = 0;i<dimension1;i++) {
			fre[i] /= sum;
		}
		
		double prob = state.random[thread].nextDouble();
		double tmp = 0;
		for(int f = 0;f<dimension1;f++) {
			tmp += fre[f];
			if(tmp>prob) {
				res = f;
				break;
			}
		}
		
		return res;
	}
	
	protected int sampleConsBasedFrequency(double []frequency,  int dimension1, int dimension2, EvolutionState state, int thread) {
		int res = state.random[thread].nextInt(dimension2 - dimension1);
		
		double fre []=new double [dimension2 - dimension1];
		for(int d = dimension1;d<dimension2;d++) {
			fre[d-dimension1] = frequency[d];
		}
		
		double sum = 0;
		for(int i = 0;i<dimension2-dimension1;i++) {
			sum+=fre[i];
		}
		for(int i = 0;i<dimension2-dimension1;i++) {
			fre[i] /= sum;
		}
		
		double prob = state.random[thread].nextDouble();
		double tmp = 0;
		for(int f = 0;f<dimension2-dimension1;f++) {
			tmp += fre[f];
			if(tmp>prob) {
				res = f;
				break;
			}
		}
		
		return res;
	}
}
