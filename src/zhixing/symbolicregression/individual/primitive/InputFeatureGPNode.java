package zhixing.symbolicregression.individual.primitive;

import java.util.Vector;

//import com.sun.corba.se.spi.orbutil.fsm.State;

import ec.EvolutionState;
import ec.Problem;
import yimei.jss.gp.data.*;
import ec.app.tutorial4.MultiValuedRegression;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import zhixing.symbolicregression.optimization.GPSymbolicRegression;

public class InputFeatureGPNode extends GPNode{
	protected int index;
	protected int range;
	
    public InputFeatureGPNode(int ind, int size) {
        super();
        children = new GPNode[0];
        this.index = ind;
        
        this.range = size;
        
        if(ind>=size || ind < 0) {
        	System.out.print("illegal index of InputFeatureGPNode\n");
        	System.exit(1);
        }
    }
    
    public InputFeatureGPNode(int ind) {
        super();
        children = new GPNode[0];
        this.index = ind;
    }
    
    public InputFeatureGPNode() {
        super();
        children = new GPNode[0];
        this.index = 0;
    }

    public int getIndex() {
        return index;
    }
    
    public int getRange() {
    	return range;
    }
    
    public void setIndex(int ind) {
    	index = ind;
    }
    
    public void setRange(int size) {
    	range = size;
    }

    @Override
    public String toString() {
    	String s = "In" + index;
    	return s;
//        switch(index) {
//        case 0:
//        	return "X";
//        case 1:
//        	return "Y";
//        default:
//        	return "unknow input features";
//        }
    }

    @Override
    public int expectedChildren() {
        return 0;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {

    	if(((GPSymbolicRegression)problem).datadim != this.range && state != null) {
    		setRange(((GPSymbolicRegression)problem).datadim);
    		index = state.random[thread].nextInt(range);
    	}
        DoubleData data = ((DoubleData)input);
        if(index < ((GPSymbolicRegression)problem).X.length) {
        	data.value = ((GPSymbolicRegression)problem).X[index];
        }
        else {
        	System.err.print("The input index exceeds the data dimension\n");
        	System.exit(1);
        }
//        switch(index) {
//        case 0:
//        	data.x = ((MultiValuedRegression)problem).currentX;
//        	break;
//        case 1:
//        	data.x = ((MultiValuedRegression)problem).currentY;
//        	break;
//        default:
//        	System.out.print("illegal InputFeatureGPNode index in evaluation\n");
//        	System.exit(1);
//        	break;
//        }
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof InputFeatureGPNode) {
        	InputFeatureGPNode o = (InputFeatureGPNode)other;
            return (index == o.getIndex());
        }

        return false;
    }
    
    @Override
    public void resetNode(final EvolutionState state, final int thread) {
    	index = state.random[thread].nextInt(range);
    }
    
    @Override
    public GPNode lightClone() {
    	GPNode n = super.lightClone();
    	
    	((InputFeatureGPNode)n).setIndex(this.index);
    	((InputFeatureGPNode)n).setRange(this.range);
    	
		return n;
    }
}
