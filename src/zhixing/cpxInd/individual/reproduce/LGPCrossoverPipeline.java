package zhixing.cpxInd.individual.reproduce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.koza.CrossoverPipeline;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
//import zhixing.jss.cpxInd.individual.LGPIndividual;

public class LGPCrossoverPipeline extends CrossoverPipeline {
	
	@Override
	public boolean verifyPoints(final GPInitializer initializer,
	        final GPNode inner1, final GPNode inner2)
	        {
	        // first check to see if inner1 is swap-compatible with inner2
	        // on a type basis
	        if (!inner1.swapCompatibleWith(initializer, inner2)) return false;

	        // next check to see if inner1 can fit in inner2's spot
	        if (inner1.depth()+inner2.atDepth() > maxDepth) return false;

	        // check for size
	        // NOTE: this is done twice, which is more costly than it should be.  But
	        // on the other hand it allows us to toss a child without testing both times
	        // and it's simpler to have it all here in the verifyPoints code.  
	        if (maxSize != NO_SIZE_LIMIT)
	            {
	            // first easy check
	            int inner1size = inner1.numNodes(GPNode.NODESEARCH_ALL);
	            int inner2size = inner2.numNodes(GPNode.NODESEARCH_ALL);
	            if (inner1size > inner2size)  // need to test further
	                {
	                // let's keep on going for the more complex test
	                GPNode root2 = ((GPTree)(inner2.rootParent())).child;
	                int root2size = root2.numNodes(GPNode.NODESEARCH_ALL);
	                if (root2size - inner2size + inner1size > maxSize)  // take root2, remove inner2 and swap in inner1.  Is it still small enough?
	                    return false;
	                }
	            }
	        
	        //check the depth of the to-be-replaced node and the new node
	        //if only one of them is at depth 0, return false
	        if(inner1.atDepth() != inner2.atDepth() && (inner1.atDepth() == 0 || inner2.atDepth() == 0)) 
	        	return false;
	        
	        //check the ratio of constants, ensure that  inner1 (to replace others) has more or equal number of read registers than inner2 (to-be-repleaced)
	        //and has less or equal number of constants
	        int cnt1 = inner1.numNodes(GPNode.NODESEARCH_READREG);
	        int cnt2 = inner2.numNodes(GPNode.NODESEARCH_READREG);
	        int cnt11 = inner1.numNodes(GPNode.NODESEARCH_CONSTANT);
	        int cnt22 = inner2.numNodes(GPNode.NODESEARCH_CONSTANT);
	        if(cnt1 < cnt2 || cnt11 > cnt22 ) return false;

	        // checks done!
	        return true;
	        }
		
	@Override
	//an LGP crossover like one-point crossover in TGP
    public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) 

        {
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already



        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        for(int q=start, parnt = 0;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
            // grab two individuals from our sources
            if (sources[0]==sources[1])  // grab from the same source
                sources[0].produce(2,2,0,subpopulation,parents,state,thread);
            else // grab from different sources
                {
                sources[0].produce(1,1,0,subpopulation,parents,state,thread);
                sources[1].produce(1,1,1,subpopulation,parents,state,thread);
                }
            
            // at this point, parents[] contains our two selected individuals
            
            // are our tree values valid?
            if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= parents[0].getTreesLength()))
                // uh oh
                state.output.fatal("GP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= parents[1].getTreesLength()))
                // uh oh
                state.output.fatal("GP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

            int t1=0, t2=0;
            LGPIndividual j1 = ((LGPIndividual)parents[parnt]).lightClone();
            LGPIndividual j2 = null;
            if (n-(q-start)>=2 && !tossSecondParent) j2 = ((LGPIndividual)parents[(parnt + 1)%parents.length]).lightClone();
            
            // Fill in various tree information that didn't get filled in there
            //j1.renewTrees();
            //if (n-(q-start)>=2 && !tossSecondParent) j2.renewTrees();
            
            double pickNum = Math.max(state.random[thread].nextDouble()*(parents[parnt].getTreesLength()), 1);
            List<Integer> index = new ArrayList<>();
            for(int i = 0;i<parents[parnt].getTreesLength();i++){
            	index.add(i);
            }
            for(int it = 0; it<index.size();it++) {
            	Collections.swap(index, it, state.random[thread].nextInt(index.size()));
            }
            
            for(int pick = 0; pick < parents[parnt].getTreesLength(); pick++) {
            	t1 = index.get(pick);
            	if(pick >= pickNum){
            		int x = t1;
            		GPTreeStruct tree = j1.getTreeStruct(x);
            		tree = (GPTreeStruct)(parents[parnt].getTree(x).clone());
                    tree.owner = j1;
                    tree.child = (GPNode)(parents[parnt].getTree(x).child.clone());
                    tree.child.parent = tree;
                    tree.child.argposition = 0;
                    j1.setTree(x, tree);
                    continue;
            	}
            	if (/*tree1==TREE_UNFIXED || */tree2==TREE_UNFIXED) 
                {
                do
                    // pick random trees  -- their GPTreeConstraints must be the same
                    {
//                    if (tree1==TREE_UNFIXED) 
//                        if (parents[0].trees.length > 1)
//                            t1 = state.random[thread].nextInt(parents[0].trees.length);
//                        else t1 = 0;
//                    else t1 = tree1;

                    if (tree2==TREE_UNFIXED) 
                        if (parents[(parnt + 1)%parents.length].getTreesLength()>1)
                            t2 = state.random[thread].nextInt(parents[(parnt + 1)%parents.length].getTreesLength());
                        else t2 = 0;
                    else t2 = tree2;
                    } while (parents[parnt].getTree(t1).constraints(initializer) != parents[(parnt + 1)%parents.length].getTree(t2).constraints(initializer));
                }
            else
                {
                t1 = tree1;
                t2 = tree2;
                // make sure the constraints are okay
                if (parents[0].getTree(t1).constraints(initializer) 
                    != parents[1].getTree(t2).constraints(initializer)) // uh oh
                    state.output.fatal("GP Crossover Pipeline's two tree choices are both specified by the user -- but their GPTreeConstraints are not the same");
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
                p1 = nodeselect1.pickNode(state,subpopulation,thread,parents[parnt],parents[parnt].getTree(t1));
                
                // pick a node in individual 2
                p2 = nodeselect2.pickNode(state,subpopulation,thread,parents[(parnt + 1)%parents.length],parents[(parnt + 1)%parents.length].getTree(t2));
                
                // check for depth and swap-compatibility limits
                res1 = verifyPoints(initializer,p2,p1);  // p2 can fill p1's spot -- order is important!
                //if (n-(q-start)<2 || tossSecondParent) res2 = true;
                //else res2 = verifyPoints(initializer,p1,p2);  // p1 can fill p2's spot -- order is important!
                
                // did we get something that had both nodes verified?
                // we reject if EITHER of them is invalid.  This is what lil-gp does.
                // Koza only has numTries set to 1, so it's compatible as well.
                if (res1) break;
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
                    
            
            
            // at this point, p1 or p2, or both, may be null.
            // If not, swap one in.  Else just copy the parent.
            if(res1) {
            	int x = t1;
            	GPTreeStruct tree = j1.getTreeStruct(x);
                tree = (GPTreeStruct)(parents[parnt].getTree(x).clone());
                tree.owner = j1;
                tree.child = parents[parnt].getTree(x).child.cloneReplacing(p2,p1); 
                tree.child.parent = tree;
                tree.child.argposition = 0;
                j1.setTree(x, tree);
                j1.evaluated = false; 
            }
            else {
            	int x = t1;
            	GPTreeStruct tree = j1.getTreeStruct(x);
        		tree = (GPTreeStruct)(parents[parnt].getTree(x).clone());
                tree.owner = j1;
                tree.child = (GPNode)(parents[parnt].getTree(x).child.clone());
                tree.child.parent = tree;
                tree.child.argposition = 0;
                j1.setTree(x, tree);
                pickNum ++;
            }
            
            //j1.updateStatus();

            }
            // add the individuals to the population
            inds[q] = j1;
            q++;
            parnt ++;

            }
            
        return n;
        }
}
