package zhixing.cpxInd.algorithm.Grammar.individual.primitives;

import java.io.DataOutput;
import java.io.IOException;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.ERC;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import yimei.jss.gp.data.DoubleData;
//import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
//import zhixing.djss.individual.LGPIndividual4DJSS;

public class NumericalValue extends ERC{

	public final static String P_NUMVALUES = "numvalues";
	
	public final static String P_VALUES = "values";
	
	protected int numValues;
	
	protected double[] candidate;
	
	protected double value;
	
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		Parameter p_numvalue = base.push(P_NUMVALUES);
    	numValues = state.parameters.getInt(p_numvalue,null,1);
    	if(numValues < 1) {
    		state.output.fatal("number of values must be >=1");
    	}
    	
    	candidate = new double[numValues];

    	for(int v = 0; v<numValues; v++) {
    		Parameter p_v = base.push(P_VALUES).push(""+v);
    		candidate[v] = state.parameters.getDouble(p_v, null, 0);
    	}
	}
	
	public NumericalValue() {
        super();
        this.numValues = 1;
        children = new GPNode[0];
        candidate = new double[] {1.0};
        value = candidate[0];
    }
	
    public String toString() {
        return ""+value;
    }

    public double getValue() {
    	return this.value;
    }
    
    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {

        DoubleData data = ((DoubleData)input);
        //LGPIndividual ind = ((LGPIndividual)individual);
        data.value = this.value;
        //data.x = ((LGPIndividual)individual).getRegisters()[index];
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof NumericalValue) {
        	NumericalValue o = (NumericalValue)other;
            return (this.value == o.getValue());
        }

        return false;
    }
    
    @Override
    public boolean nodeEquals(final GPNode node) {
    	return equals(node);
    }
    
    @Override
    public void resetNode(final EvolutionState state, final int thread) {
    	value = candidate[state.random[thread].nextInt(numValues)];
    }
    
    @Override
    public String encode() {
    	return toString();
    }
	
    @Override
    public void writeNode(EvolutionState state, DataOutput output) throws IOException {
        //do nothing
    }
}
