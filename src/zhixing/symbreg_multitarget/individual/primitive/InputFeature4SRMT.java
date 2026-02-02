package zhixing.symbreg_multitarget.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.data.DoubleData;
import zhixing.symbolicregression.individual.primitive.InputFeatureGPNode;
import zhixing.symbreg_multitarget.optimization.GPSymbolicRegressionMultiTarget;

public class InputFeature4SRMT extends InputFeatureGPNode {

	public InputFeature4SRMT(int ind, int size) {
        super(ind, size);
    }
    
    public InputFeature4SRMT(int ind) {
        super(ind);
    }
    
    public InputFeature4SRMT() {
        super();
    }
	
	@Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {

    	if(((GPSymbolicRegressionMultiTarget)problem).getDatadim() != this.range && state != null) {
    		setRange(((GPSymbolicRegressionMultiTarget)problem).getDatadim());
    		index = state.random[thread].nextInt(range);
    	}
        DoubleData data = ((DoubleData)input);
        if(index < ((GPSymbolicRegressionMultiTarget)problem).X.length) {
        	data.value = ((GPSymbolicRegressionMultiTarget)problem).X[index];
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
    public GPNode lightClone() {
    	GPNode n = super.lightClone();
    	
    	((InputFeature4SRMT)n).setIndex(this.index);
    	((InputFeature4SRMT)n).setRange(this.range);
    	
		return n;
    }
}
