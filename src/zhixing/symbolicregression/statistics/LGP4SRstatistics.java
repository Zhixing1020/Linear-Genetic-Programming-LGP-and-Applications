package zhixing.symbolicregression.statistics;

import ec.EvolutionState;
import zhixing.cpxInd.statistics.LGPStatistics;

public class LGP4SRstatistics extends LGPStatistics {
	public void finalStatistics(final EvolutionState state, final int result)
    {
    for(int x=0;x<children.length;x++)
        children[x].finalStatistics(state, result);
    }
}
