package zhixing.cpxInd.individual.primitive;

import java.util.Vector;

import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.Problem;
//import ec.app.tutorial4.DoubleData;
import yimei.jss.gp.data.DoubleData;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class ConstantGPNode extends GPNode{
	private Double value;
	private static Vector<Double> range;
	
	private double lb = 0;
	private double ub = 1;
	private double step = 0.1;
	
	//ConstantGPNode and InputFeatureGPNode can also develop a setup function like Write(Read)RegisterGPNode, and be used in GPFunctionSet
	
	public ConstantGPNode() {
		super();
		children = new GPNode[0];
		this.value = 1.;
	}
	
	public ConstantGPNode(double val) {
		super();
		children = new GPNode[0];
		this.value = val;
	}
	
	public ConstantGPNode(double begin, double end, double step) {
		super();
		children = new GPNode[0];
		lb = begin;
		ub = end;
		this.step = step;
		
		range = new Vector<>();
		for(double i = begin; i<=end;i+=step) {
			range.add(i);
		}
		this.value = 1.;
	}
	
	public Double getValue() {
		return value;
	}
	
	public Vector<Double> getRange(){
		return range;
	}
	
	public void setValue(Double v) {
		value = v;
	}
	
	@Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int expectedChildren() {
        return 0;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {

        DoubleData data = ((DoubleData)input);
        //data.value = ((LGPIndividual)individual).getRegisters()[index];
        data.value = value;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof ConstantGPNode) {
            return value == ((ConstantGPNode) other).getValue();
        }

        return false;
    }
    
    @Override
    public void resetNode(final EvolutionState state, final int thread) {
//    	value = range.get(state.random[thread].nextInt(range.size()));
   		value = state.random[thread].nextDouble()*(ub - lb) + lb;
    }
    
    @Override
    public GPNode lightClone() {
    	GPNode n = super.lightClone();
    	
    	((ConstantGPNode)n).setValue(value);
    	((ConstantGPNode)n).range = this.range;
    	((ConstantGPNode)n).lb = this.lb;
    	((ConstantGPNode)n).ub = this.ub;
    	
    	return n;
    }
}
