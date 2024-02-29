package zhixing.djss.algorithm.GrammarTGP.individual;

import ec.EvolutionState;
import ec.gp.GPFunctionSet;
import ec.gp.GPNode;
import ec.gp.GPNodeParent;
import ec.gp.GPType;
import ec.gp.koza.GPKozaDefaults;
import ec.util.Parameter;

public class GrammarGrowBuilder4DJSS extends KozaBuilder4IF4DJSS{
	public static final String P_GROWBUILDER = "grow";

    public Parameter defaultBase()
        {
        return GPKozaDefaults.base().push(P_GROWBUILDER); 
        }

    public GPNode newRootedTree(final EvolutionState state,
        final GPType type,
        final int thread,
        final GPNodeParent parent,
        final GPFunctionSet set,
        final int argposition,
        final int requestedSize)
        {
        GPNode n = growNode(state,0,state.random[thread].nextInt(maxDepth-minDepth+1) + minDepth,type,thread,parent,argposition,set);
        return n;
        }
}
