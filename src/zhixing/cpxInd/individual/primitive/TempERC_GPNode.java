package zhixing.cpxInd.individual.primitive;

import ec.EvolutionState;

public class TempERC_GPNode extends ConstantGPNode{

	@Override
    public void resetNode(final EvolutionState state, final int thread) {
//    	value = range.get(state.random[thread].nextInt(range.size()));
		double range = (ub - lb)*(Math.max( 1-(double)state.generation / state.numGenerations, 0.1));
			double val = (-range + 2 * range * state.random[thread].nextDouble());
			if(val >=0) {
				val += lb;
			}
			else {
				val -= lb;
			}
//			values[i] = new BigDecimal(val).setScale(2,BigDecimal.ROUND_FLOOR).floatValue();
			value = val;
//   		value = state.random[thread].nextDouble()*(ub - lb) + lb;
    }
}
