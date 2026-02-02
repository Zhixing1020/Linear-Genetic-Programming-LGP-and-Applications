package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.data.DoubleData;

public class MaxHub extends GPNode{

	public boolean equals(Object other) {
		if(other instanceof MaxHub) {
			
//			if(((AvgHub)other).getArguments().equals(arguments) ) {
//				return true;
//			}
			return true;
		}
		return false;
	}
	
	public String toString() {
		return "MaxHub";
	}
	
	public int expectedChildren() {
    	return CHILDREN_UNKNOWN;
    }
	
	@Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
    	DoubleData rd = ((DoubleData)input);
    	
//    	double [] vals = new double [children.length];
    	
    	int ind = 0;
    	double result = -1e7;
    	for(int i = 0; i<children.length; i++) {
    		children[i].eval(state,thread,input,stack,individual,problem);
    		result = Math.max(result, rd.value);
    		
//    		vals[i] = rd.value;
    	}
    	rd.value = result;
    	
//    	addValues2List(vals, state, thread, problem);
    	
    	return;
    }
}
