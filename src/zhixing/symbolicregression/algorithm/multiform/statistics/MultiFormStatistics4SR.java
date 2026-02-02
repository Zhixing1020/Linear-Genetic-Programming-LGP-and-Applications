package zhixing.symbolicregression.algorithm.multiform.statistics;

import ec.EvolutionState;

public class MultiFormStatistics4SR extends zhixing.cpxInd.algorithm.Multiform.statistics.MultiFormStatistics{
	public void finalStatistics(final EvolutionState state, final int result)
    {
    for(int x=0;x<children.length;x++)
        children[x].finalStatistics(state, result);
    }
}
