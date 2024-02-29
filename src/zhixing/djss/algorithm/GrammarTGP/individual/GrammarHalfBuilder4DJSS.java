package zhixing.djss.algorithm.GrammarTGP.individual;

import ec.EvolutionState;
import ec.gp.GPFunctionSet;
import ec.gp.GPNode;
import ec.gp.GPNodeParent;
import ec.gp.GPType;
import ec.gp.koza.GPKozaDefaults;
import ec.util.Parameter;

public class GrammarHalfBuilder4DJSS extends KozaBuilder4IF4DJSS {
	public static final String P_HALFBUILDER = "half";
    public static final String P_PICKGROWPROBABILITY = "growp";

    /** The likelihood of using GROW over FULL. */
    public double pickGrowProbability;
    
    public Parameter defaultBase()
        {
        return GPKozaDefaults.base().push(P_HALFBUILDER); 
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        Parameter def = defaultBase();

        pickGrowProbability = state.parameters.getDoubleWithMax(
            base.push(P_PICKGROWPROBABILITY),
            def.push(P_PICKGROWPROBABILITY),0.0,1.0);
        if (pickGrowProbability < 0.0)
            state.output.fatal("The Pick-Grow Probability for HalfBuilder must be a double floating-point value between 0.0 and 1.0 inclusive.", base.push(P_MAXDEPTH),def.push(P_MAXDEPTH));
        }
    
    public GPNode newRootedTree(final EvolutionState state,
        final GPType type,
        final int thread,
        final GPNodeParent parent,
        final GPFunctionSet set,
        final int argposition,
        final int requestedSize)
        {
    	if (state.random[thread].nextDouble() < pickGrowProbability)
            return growNode(state,0,state.random[thread].nextInt(maxDepth-minDepth+1) + minDepth,type,thread,parent,argposition,set);
        else
            return fullNode(state,0,state.random[thread].nextInt(maxDepth-minDepth+1) + minDepth,type,thread,parent,argposition,set);
        }
}
