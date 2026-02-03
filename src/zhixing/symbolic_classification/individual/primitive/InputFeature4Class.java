package zhixing.symbolic_classification.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.data.DoubleData;
import zhixing.symbolic_classification.optimization.GPClassification;
import zhixing.symbolicregression.individual.primitive.InputFeatureGPNode;

public class InputFeature4Class extends InputFeatureGPNode {

	public InputFeature4Class(int ind, int size) {
        super(ind, size);
    }
    
    public InputFeature4Class(int ind) {
        super(ind);
    }
    
    public InputFeature4Class() {
        super();
    }
	
	@Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {

    	if(((GPClassification)problem).getDatadim() != this.range && state != null) {
    		setRange(((GPClassification)problem).getDatadim());
    		index = state.random[thread].nextInt(range);
    	}
        DoubleData data = ((DoubleData)input);
        if(index < ((GPClassification)problem).X.length) {
        	data.value = ((GPClassification)problem).X[index];
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
    	
    	((InputFeature4Class)n).setIndex(this.index);
    	((InputFeature4Class)n).setRange(this.range);
    	
		return n;
    }
}
