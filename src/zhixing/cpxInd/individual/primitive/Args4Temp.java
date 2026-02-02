package zhixing.cpxInd.individual.primitive;

import ec.EvolutionState;
import ec.util.Parameter;

public class Args4Temp extends Args{

	public final static String P_NAME = "Temperature_Argument";
	
	public final static String P_UPPER = "upper";
	public final static String P_LOWER = "lower";
	
	Parameter def = new Parameter(P_NAME).push(P_ARGUMENTS);
	
	double upper = 1.;
	double lower = 0.1;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);    	
		
		if(maxlength != 1) {
    		state.output.fatal("maximum length of ArgsTemp must be ==1");
    		System.exit(1);
    	}
		
		Parameter pp = base.push(P_UPPER);
    	upper = state.parameters.getDoubleWithDefault(pp, def.push(P_UPPER), 1);
    	if(upper <=0) {
    		state.output.fatal("upper of ArgsTemp must be >0");
    		System.exit(1);
    	}
    	
    	pp = base.push(P_LOWER);
    	lower = state.parameters.getDoubleWithDefault(pp, def.push(P_LOWER), 0.1);
    	if(lower <=0 || lower >= upper) {
    		state.output.fatal("lower of ArgsTemp must be >0 && <upper " + upper);
    		System.exit(1);
    	}
	}
	
	@Override
    public void resetNode(final EvolutionState state, final int thread) {
//    	value = range.get(state.random[thread].nextInt(range.size()));
   		
   		for(int i = 0; i<maxlength; i++) {
   			double range = (upper - lower)*(Math.max( 1-(double)state.generation / state.numGenerations, 0.1));
   			double val = (-range + 2 * range * state.random[thread].nextDouble());
   			if(val >=0) {
   				val += lower;
   			}
   			else {
   				val -= lower;
   			}
//   			values[i] = new BigDecimal(val).setScale(2,BigDecimal.ROUND_FLOOR).floatValue();
   			values[i] = val;
   		}
    }
	
	@Override
	public void varyNode(EvolutionState state, int thread, Entity ref) {
		// TODO Auto-generated method stub
//		for(int i = 0; i<maxlength; i++)
//		values[0] = moveByStep(state, thread, values[0]);
		resetNode(state, thread);
	}
	
}
