package zhixing.cpxInd.algorithm.semantic.individual.reproduce;

import ec.EvolutionState;
import ec.Individual;
//import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.koza.CrossoverPipeline;
import ec.gp.koza.MutationPipeline;
import ec.util.Parameter;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMicroMutationPipeline;
import zhixing.cpxInd.algorithm.semantic.individual.SLGPIndividual;

public class InstrWiseCrossover extends CrossoverPipeline{
	
	public static final String INSTRWISECROSS = "InstrWiseXov";
	public static final String P_MINDEPTH = "mindepth";
	
	public static final String P_MICROMUTBASE = "micro_base";

	protected LGPMicroMutationPipeline microMutation;
	
	int minDepth;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		
		super.setup(state,base);
		
		Parameter def = new Parameter(INSTRWISECROSS);
		
		minDepth = state.parameters.getInt(base.push(P_MINDEPTH),def.push(P_MINDEPTH),1);
        if (minDepth==0)
            state.output.fatal("Instruction-wise Crossover Pipeline has an invalid minimum depth (it must be >= 1).",base.push(P_MINDEPTH),def.push(P_MINDEPTH));
        
        Parameter microbase = new Parameter(state.parameters.getString(base.push(P_MICROMUTBASE), def.push(P_MICROMUTBASE)));
        microMutation = null;
        if(!microbase.toString().equals("null")){
        	//microMutation = new LGPMicroMutationPipeline();
        	microMutation = (LGPMicroMutationPipeline)(state.parameters.getInstanceForParameter(
                    microbase, def.push(P_MICROMUTBASE), MutationPipeline.class));
   		 microMutation.setup(state, microbase);
        }
	}

	@Override
	 public int produce(final int min, 
		        final int max, 
		        final int start,
		        final int subpopulation,
		        final Individual[] inds,
		        final EvolutionState state,
		        final int thread,
		        final Individual[] parents) {
	    	// how many individuals should we make?
	        int n = typicalIndsProduced();
	        if (n < min) n = min;
	        if (n > max) n = max;

	        // should we bother?
	        if (!state.random[thread].nextBoolean(likelihood))
	            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

	    	 GPInitializer initializer = ((GPInitializer)state.initializer);
	    	 
	    	 for(int q=start, parnt = 0;q<n+start; /* no increment */) {
	    		// are our tree values valid?
	    	        if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= ((SLGPIndividual)parents[0]).getTreesLength()))
	    	            // uh oh
	    	            state.output.fatal("SLGP Instruction-wise Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
	    	        if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= ((SLGPIndividual) parents[1]).getTreesLength()))
	    	            // uh oh
	    	            state.output.fatal("SLGP Instruction-wise Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

	    	        int t1=0; int t2=0;
	    	        if (tree1==TREE_UNFIXED || tree2==TREE_UNFIXED) 
	    	            {
	    	            do
	    	                // pick random trees  -- their GPTreeConstraints must be the same
	    	                {
	    	                if (tree1==TREE_UNFIXED) 
	    	                    if (((SLGPIndividual) parents[0]).getTreesLength() > 1) 
	    	                        t1 = state.random[thread].nextInt(((SLGPIndividual) parents[0]).getTreesLength());
	    	                    else t1 = 0;
	    	                else t1 = tree1;

	    	                if (tree2==TREE_UNFIXED) 
	    	                    if (((SLGPIndividual) parents[1]).getTreesLength()>1)
	    	                        t2 = state.random[thread].nextInt(((SLGPIndividual) parents[1]).getTreesLength());
	    	                    else t2 = 0;
	    	                else t2 = tree2;
	    	                } while (((SLGPIndividual) parents[0]).getTree(t1).constraints(initializer) != ((SLGPIndividual) parents[1]).getTree(t2).constraints(initializer));
	    	            }
	    	        else
	    	            {
	    	            t1 = tree1;
	    	            t2 = tree2;
	    	            // make sure the constraints are okay
	    	            if (((SLGPIndividual) parents[0]).getTree(t1).constraints(initializer)
	    	                != ((SLGPIndividual) parents[1]).getTree(t2).constraints(initializer)) // uh oh   
	    	                state.output.fatal("SLGP instruction-wise Crossover Pipeline's two tree choices are both specified by the user -- but their GPTreeConstraints are not the same");
	    	            }



	    	        // validity results...
	    	        boolean res1 = false;
	    	        boolean res2 = false;
	    	        
	    	        
	    	        // prepare the nodeselectors
	    	        nodeselect1.reset();
	    	        nodeselect2.reset();
	    	        
	    	        
	    	        // pick some nodes
	    	        
	    	        GPNode p1=null;
	    	        GPNode p2=null;
	    	        
	    	        for(int x=0;x<numTries;x++)
	    	            {
	    	            // pick a node in individual 1
	    	            p1 = nodeselect1.pickNode(state,subpopulation,thread,(SLGPIndividual) parents[0],((SLGPIndividual) parents[0]).getTreeStruct(t1));
	    	            
	    	            // pick a node in individual 2
	    	            p2 = nodeselect2.pickNode(state,subpopulation,thread,(SLGPIndividual) parents[1],((SLGPIndividual) parents[1]).getTreeStruct(t2));
	    	            
	    	            // check for depth and swap-compatibility limits
	    	            res1 = verifyPoints(initializer,p2,p1);  // p2 can fill p1's spot -- order is important!
	    	            if (n-(q-start)<2 || tossSecondParent) res2 = true;
	    	            else res2 = verifyPoints(initializer,p1,p2);  // p1 can fill p2's spot -- order is important!
	    	            
	    	            // did we get something that had both nodes verified?
	    	            // we reject if EITHER of them is invalid.  This is what lil-gp does.
	    	            // Koza only has numTries set to 1, so it's compatible as well.
	    	            if (res1 && res2) break;
	    	            }

	    	        // at this point, res1 AND res2 are valid, OR either res1
	    	        // OR res2 is valid and we ran out of tries, OR neither is
	    	        // valid and we ran out of tries.  So now we will transfer
	    	        // to a tree which has res1 or res2 valid, otherwise it'll
	    	        // just get replicated.  This is compatible with both Koza
	    	        // and lil-gp.
	    	        

	    	        // at this point I could check to see if my sources were breeding
	    	        // pipelines -- but I'm too lazy to write that code (it's a little
	    	        // complicated) to just swap one individual over or both over,
	    	        // -- it might still entail some copying.  Perhaps in the future.
	    	        // It would make things faster perhaps, not requiring all that
	    	        // cloning.

	    	        
	    	        
	    	        // Create some new individuals based on the old ones -- since
	    	        // GPTree doesn't deep-clone, this should be just fine.  Perhaps we
	    	        // should change this to proto off of the main species prototype, but
	    	        // we have to then copy so much stuff over; it's not worth it.
	    	                
	    	        SLGPIndividual j1 = (SLGPIndividual) (((SLGPIndividual) parents[0]).lightClone());
	    	        SLGPIndividual j2 = null;
	    	        if (n-(q-start)>=2 && !tossSecondParent) j2 = (SLGPIndividual) (((SLGPIndividual) parents[1]).lightClone());
	    	        
	    	        // Fill in various tree information that didn't get filled in there
	    	        //========zhixing, 2021.3.26
	    	        //j1.renewTrees();
	    	        //if (n-(q-start)>=2 && !tossSecondParent) j2.renewTrees();
	    	        
	    	        // at this point, p1 or p2, or both, may be null.
	    	        // If not, swap one in.  Else just copy the parent.
	    	        
	    	        for(int x=0;x<j1.getTreesLength();x++)
	    	            {
	    	        	GPTree tree = j1.getTree(x);
	    	            if (x==t1 && res1)  // we've got a tree with a kicking cross position!
	    	                { 
	    	                tree = (GPTree)(((SLGPIndividual) parents[0]).getTree(x).lightClone());
	    	                tree.owner = j1;
	    	                tree.child = ((SLGPIndividual) parents[0]).getTree(x).child.cloneReplacing(p2,p1); 
	    	                tree.child.parent = tree;
	    	                tree.child.argposition = 0;
	    	                j1.setTree(x, tree);
	    	                j1.evaluated = false; 
	    	                }  // it's changed
	    	            else 
	    	                {
	    	                tree = (GPTree)(((SLGPIndividual) parents[0]).getTree(x).lightClone());
	    	                tree.owner = j1;
	    	                tree.child = (GPNode)(((SLGPIndividual) parents[0]).getTree(x).child.clone());
	    	                tree.child.parent = tree;
	    	                tree.child.argposition = 0;
	    	                j1.setTree(x, tree);
	    	                }
	    	            }
	    	        
	    	        if (n-(q-start)>=2 && !tossSecondParent) 
	    	            for(int x=0;x<j2.getTreesLength();x++)
	    	                {
	    	            	GPTree tree = j2.getTree(x);
	    	                if (x==t2 && res2)  // we've got a tree with a kicking cross position!
	    	                    { 
	    	                    tree = (GPTree)(((SLGPIndividual) parents[1]).getTree(x).lightClone());           
	    	                    tree.owner = j2;
	    	                    tree.child = ((SLGPIndividual) parents[1]).getTree(x).child.cloneReplacing(p1,p2); 
	    	                    tree.child.parent = tree;
	    	                    tree.child.argposition = 0;
	    	                    j2.setTree(x, tree);
	    	                    j2.evaluated = false; 
	    	                    } // it's changed
	    	                else 
	    	                    {
	    	                    tree = (GPTree)(((SLGPIndividual) parents[1]).getTree(x).lightClone());           
	    	                    tree.owner = j2;
	    	                    tree.child = (GPNode)(((SLGPIndividual) parents[1]).getTree(x).child.clone());
	    	                    tree.child.parent = tree;
	    	                    tree.child.argposition = 0;
	    	                    j2.setTree(x, tree);
	    	                    }
	    	                }
	    	        //===================
	    	        
	    	        // add the individuals to the population
	    	        if(microMutation != null) j1 = (SLGPIndividual) microMutation.produce(subpopulation, j1, state, thread);
	    	        inds[q] = j1;
	    	        q++;
	    	        if (q<n+start && !tossSecondParent)
	    	            {
	    	        	if(microMutation != null) j2 = (SLGPIndividual) microMutation.produce(subpopulation, j2, state, thread);
	    	            inds[q] = j2;
	    	            q++;
	    	            }
	    	 }

	        return n;
	    }
	
	public boolean verifyPoints(final GPInitializer initializer,
	        final GPNode inner1, final GPNode inner2)
	        {
		
			boolean res = super.verifyPoints(initializer, inner1, inner2);
	        
			if(res) {
				if (inner1.depth()+inner2.atDepth() < minDepth) return false;
				
				if(inner1 instanceof WriteRegisterGPNode
						|| inner2 instanceof WriteRegisterGPNode) return false;
			}
			
	        return res;
	        }
}
