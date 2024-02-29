package zhixing.symbolicregression.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import yimei.jss.gp.data.DoubleData;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;

public class Add extends yimei.jss.gp.function.Add{
//	@Override
//	public void eval(final EvolutionState state,
//	        final int thread,
//	        final GPData input,
//	        final ADFStack stack,
//	        final GPIndividual individual,
//	        final Problem problem)
//	        {
//	        double result;
//	        DoubleData rd = ((DoubleData)(input));
//
//	        children[0].eval(state,thread,input,stack,individual,problem);
//	        result = rd.value;
//
//	        children[1].eval(state,thread,input,stack,individual,problem);
//	        rd.value = result + rd.value;
//	        
//	        if(Math.abs(rd.value) >= 1e6) {
//	        	rd.value = 1e6*(rd.value+0.001)/(rd.value+0.001);
//	        }
//	        }
}
