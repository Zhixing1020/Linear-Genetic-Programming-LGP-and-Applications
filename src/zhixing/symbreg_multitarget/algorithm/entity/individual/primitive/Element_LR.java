package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import zhixing.cpxInd.individual.primitive.Args;
import zhixing.optimization.SupervisedProblem;

public class Element_LR extends Extractor_EntityNode{

	final int attributeNumber = 1;
	final int nonlist_attrNumber = 0;
    
//    @Override
//    public void eval(EvolutionState state, int thread, GPData input,
//                     ADFStack stack, GPIndividual individual, Problem problem) {
//    	DoubleData data = ((DoubleData)input);
//    	
//    	data.value = getAttributes(problem, arguments, 0, 0);
//    	
//    	return;
//    }
    
    public boolean equals(Object other) {
        if (other instanceof Element_LR) {
        	
        	if(((Element_LR)other).getArguments().equals(arguments)) {
        		return true;
        	}
        	
        }
        return false;
    }

	@Override
	public double getAttributes(Object obj, Args args, int index, int bias) {
		int ind = 0;
		int xi = 0;
		double res = 0;
		
		if(obj instanceof SupervisedProblem) {
			res = args.getValue(ind++);
			
			while(ind < args.getMaxLength()) {
				int index1 = (int) Math.floor(args.getValue(ind++) * ((SupervisedProblem)obj).getDatadim());
				res += ((SupervisedProblem)obj).getX()[index1] * args.getValue(ind++);
			}
		}
		else {
			System.err.print("Element_LR only accepts objects of SupervisedProblem\n");
			System.exit(1);
		}
		return res;
	}

	@Override
	public String toString() {
		
		return "Element_LR" + arguments.toString();
	}

	@Override
	public String toGraphvizString() {
		
		String args = String.format("%.2f", arguments.getValue(1)).toString();
		for(int i = 3; i<arguments.getMaxLength(); i+=2) {
			args += ","+String.format("%.2f", arguments.getValue(i)).toString();
		}
		
		return "ElementLR[" + args + "]";
	}
}
