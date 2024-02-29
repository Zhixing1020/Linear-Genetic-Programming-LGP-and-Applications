package zhixing.cpxInd.algorithm.GrammarTGP.individual.reproduce;

import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.koza.CrossoverPipeline;
import zhixing.cpxInd.algorithm.Grammar.individual.primitives.NumericalValue;
import zhixing.cpxInd.algorithm.GrammarTGP.individual.primitives.BranchingTGP;

public class GrammarCrossoverPipeline extends CrossoverPipeline {
	@Override
	public boolean verifyPoints(final GPInitializer initializer,
	        final GPNode inner1, final GPNode inner2)
	        {
	        // first check to see if inner1 is swap-compatible with inner2
	        // on a type basis
	        if (!inner1.swapCompatibleWith(initializer, inner2)) return false;

	        if(! grammarCompatible(inner1, inner2)) return false;
	        
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

	        // checks done!
	        return true;
	        }
	
	protected boolean grammarCompatible(final GPNode inner1, final GPNode inner2) {
		//swap only when the two selected nodes are the same type
		//or both of their parents are IF or NULL
		if(inner1 instanceof BranchingTGP && inner2 instanceof BranchingTGP
				|| inner1 instanceof NumericalValue && inner2 instanceof NumericalValue
				|| inner1.expectedChildren() == 2 && inner2.expectedChildren() == 2/*since we only consider binary arithmetic functions in the experiments*/) {
			return true;
		}
		if(inner1.expectedChildren() == 2 && inner2 instanceof BranchingTGP) {
			if(inner1.parent instanceof BranchingTGP && inner2.parent instanceof BranchingTGP
					|| inner1.atDepth() == 0 && inner2.atDepth() == 0) {
				return true;
			}
		}
		if(inner2.expectedChildren() == 2 && inner1 instanceof BranchingTGP) {
			if(inner2.parent instanceof BranchingTGP && inner1.parent instanceof BranchingTGP
					|| inner1.atDepth() == 0 && inner2.atDepth() == 0) {
				return true;
			}
		}
		
		return false;
	}
}
