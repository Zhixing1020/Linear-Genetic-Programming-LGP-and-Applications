package zhixing.symbolicregression.statistics;

import ec.EvolutionState;
import ec.gp.koza.MyKozaShortStatistics;

public class TGP4SRstatistics extends MyKozaShortStatistics{
	public void finalStatistics(final EvolutionState state, final int result)
    {
    for(int x=0;x<children.length;x++)
        children[x].finalStatistics(state, result);
    }
}
