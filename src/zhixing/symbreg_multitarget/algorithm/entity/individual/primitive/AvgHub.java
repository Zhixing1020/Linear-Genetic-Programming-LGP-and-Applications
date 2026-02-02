package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.data.DoubleData;

public class AvgHub extends GPNode{

	public boolean equals(Object other) {
		if(other instanceof AvgHub) {
			
//			if(((AvgHub)other).getArguments().equals(arguments) ) {
//				return true;
//			}
			return true;
		}
		return false;
	}
	
	public String toString() {
		return "AvgHub";
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
    	double result = 0;
    	for(int i = 0; i<children.length; i++) {
    		children[i].eval(state,thread,input,stack,individual,problem);
    		result += rd.value;
    		
//    		vals[i] = rd.value;
    	}
    	rd.value = result / children.length;
    	
//    	addValues2List(vals, state, thread, problem);
    	
    	return;
    }
	
}
