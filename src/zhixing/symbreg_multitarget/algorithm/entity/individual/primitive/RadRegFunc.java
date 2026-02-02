package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import yimei.jss.gp.data.DoubleData;
import zhixing.optimization.SupervisedProblem;

public class RadRegFunc extends Function_EntityNode{

	public boolean equals(Object other) {
		if(other instanceof RadRegFunc) {
			
			if(((RadRegFunc)other).getArguments().equals(arguments) ) {
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
		double a0 = 0, a1 = 1, a2 = 1;
		
    	int ind = 0;
    	
    	a0 = arguments.getValue(ind++);
		a1 = arguments.getValue(ind++);
		a2 = arguments.getValue(ind++);
    	for(int i = 0; i<children.length; i++) {
    		children[i].eval(state,thread,input,stack,individual,problem);

    		res += Math.pow( rd.value - arguments.getValue(ind++), 2);
    		
    		vals[i] = rd.value;
    	}
    	
    	res = a1 * Math.exp(-a2*a2*res) + a0 + rd.value;
    	
    	res = rectify_output(res);
    	
    	rd.value = res;
    	
    	addValues2List(vals, res, state, thread, problem);
    	
    	return;
    }
	
	

	@Override
	public String toString() {
		
		return "RadRF_entity" + arguments.toString();
	}

	@Override
	public String toGraphvizString() {
		return "RadRF[" + String.format("%.2f", arguments.getValue(0)).toString() +","
				+String.format("%.2f", arguments.getValue(1)).toString()+","
				+String.format("%.2f", arguments.getValue(2)).toString()+"]";
	}
	
	@Override
	public int expectedChildren() {
    	return 1;
    }
}
