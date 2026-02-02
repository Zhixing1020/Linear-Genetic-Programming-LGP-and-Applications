package zhixing.cpxInd.individual.primitive;

import java.util.Vector;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.data.DoubleData;

public class Temperature extends Entity {

	@Override
	public boolean nodeEquivalentTo(GPNode node)
    {
		boolean res = super.nodeEquivalentTo(node);
		return res && this.toString().equals(node.toString());
    }
	
	@Override
	public double getAttributes(Object obj, Args args, int index, int bias) {
		System.err.print("Temperature GPNode has not defined getAttributes()\n");
		System.exit(1);
		
		return 0;
	}

	@Override
	public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual,
			Problem problem) {
		DoubleData rd = ((DoubleData)input);
		
		double a0 = arguments.getValue(0);
		
		children[0].eval(state,thread,input,stack,individual,problem);
		
		rd.value = a0 * rd.value;
		
		return;
	}

	@Override
	public String toString() {
		
		return "Temp" + arguments.toString();
	}

	@Override
	public String toGraphvizString() {
		return "Temp[" + String.format("%.2f", arguments.getValue(0)).toString()+"]";
	}
	
	@Override
	public int expectedChildren() {
    	return 1;
    }
}
