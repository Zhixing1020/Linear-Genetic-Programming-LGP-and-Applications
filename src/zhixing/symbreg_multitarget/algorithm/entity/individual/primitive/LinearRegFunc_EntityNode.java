package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.data.DoubleData;
import zhixing.cpxInd.individual.primitive.Args;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.symbreg_multitarget.optimization.GPSymbolicRegressionMultiTarget;

public class LinearRegFunc_EntityNode extends Function_EntityNode{
	
	public boolean equals(Object other) {
		if(other instanceof LinearRegFunc_EntityNode) {
			
			if(((LinearRegFunc_EntityNode)other).getArguments().equals(arguments) ) {
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
    	
    	int ind = 0;
    	double result = arguments.getValue(ind++);
    	for(int i = 0; i<children.length; i++) {
    		children[i].eval(state,thread,input,stack,individual,problem);
    		result += rd.value * arguments.getValue(ind++);
    		
    		vals[i] = rd.value;
    	}
    	
    	result = rectify_output(result);
    	
    	rd.value = result;
    	
    	addValues2List(vals, result, state, thread, problem);
    	
    	return;
    }
	
	

	@Override
	public String toString() {
		
		return "LRF_entity" + arguments.toString();
	}

	@Override
	public String toGraphvizString() {
		return "LRF[" + String.format("%.2f", arguments.getValue(0)).toString() +","+String.format("%.2f", arguments.getValue(1)).toString()+"]";
	}
	
	@Override
	public int expectedChildren() {
    	return 1;
    }
}
