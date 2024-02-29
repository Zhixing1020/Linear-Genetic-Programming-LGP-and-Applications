package zhixing.cpxInd.individual.reproduce;

import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.gp.GPFunctionSet;
import ec.gp.GPNode;
import ec.gp.GPNodeParent;
import ec.gp.GPType;
import ec.gp.koza.GrowBuilder;
import ec.gp.koza.HalfBuilder;
import ec.gp.koza.KozaBuilder;

public class LGPMutationGrowBuilder extends HalfBuilder {
	public GPNode newRootedTree(final EvolutionState state,
	        final GPType type,
	        final int thread,
	        final GPNodeParent parent,
	        final GPFunctionSet set,
	        final int argposition,
	        final int requestedSize,
	        final int position)
	//position: the current position of the root of old sub-trees
	        {
			GPNode node;
			if (state.random[thread].nextDouble() < pickGrowProbability)
                node = growNode_reg(state,position,state.random[thread].nextInt(maxDepth-minDepth+1) + minDepth,type,thread,parent,argposition,set);
            else
                node = fullNode_reg(state,position,state.random[thread].nextInt(maxDepth-minDepth+1) + minDepth,type,thread,parent,argposition,set);
			return node;
	        }
}
