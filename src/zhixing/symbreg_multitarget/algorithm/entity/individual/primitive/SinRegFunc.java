package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import yimei.jss.gp.data.DoubleData;

public class SinRegFunc extends Function_EntityNode{

	public boolean equals(Object other) {
		if(other instanceof SinRegFunc) {
			
			if(((SinRegFunc)other).getArguments().equals(arguments) ) {
				return true;
			}
		}
		return false;
	}
	
	@Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
    	DoubleData rd = ((DoubleData)input);
    	
    	double [] vals = new double [children.length];
    	double res = 0;
		double a0 = 0, a1 = 1, a2 = 1, a3=0;
		
    	int ind = 0;
    	
    	a0 = arguments.getValue(ind++);
		a1 = arguments.getValue(ind++);
		a2 = arguments.getValue(ind++);
		a3 = arguments.getValue(ind++);
    	
    	children[0].eval(state,thread,input,stack,individual,problem);
		
		vals[0] = rd.value;
    	
    	res = a0 + a1 * Math.sin(a2 * rd.value + a3) + rd.value;
    	
    	res = rectify_output(res);
    	
    	rd.value = res;
    	
    	addValues2List(vals, res, state, thread, problem);
    	
    	return;
    }
	
	

	@Override
	public String toString() {
		
		return "SinRF_entity" + arguments.toString();
	}

	@Override
	public String toGraphvizString() {
		return "SinRF[" + String.format("%.2f", arguments.getValue(0)).toString() +","
				+String.format("%.2f", arguments.getValue(1)).toString()+","
				+String.format("%.2f", arguments.getValue(2)).toString()+","
				+ String.format("%.2f", arguments.getValue(3)).toString()+"]";
	}
	
	@Override
	public int expectedChildren() {
    	return 1;
    }
}
