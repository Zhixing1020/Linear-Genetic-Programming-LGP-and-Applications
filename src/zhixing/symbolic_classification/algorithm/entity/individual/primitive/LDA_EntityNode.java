package zhixing.symbolic_classification.algorithm.entity.individual.primitive;

import zhixing.cpxInd.individual.primitive.Args;
import zhixing.symbolic_classification.optimization.GPClassification;

public class LDA_EntityNode extends Extractor_EntityNode{

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
        if (other instanceof LDA_EntityNode) {
        	
        	if(((LDA_EntityNode)other).getArguments().equals(arguments)) {
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
		
		if(obj instanceof GPClassification) {
			int index1 = (int) Math.floor(args.getValue(ind++) * ((GPClassification)obj).getDatadim());
			int index2 = (int) Math.floor(args.getValue(ind++) * ((GPClassification)obj).getDatadim());
			
			res = 0;
			xi = index1;
			
			while(ind < args.getMaxLength() && xi <= index2) {
				res += ((GPClassification)obj).X[xi++] * args.getValue(ind++);
			}
		}
		else {
			System.err.print("LDA_EntityNode only accepts objects of GPClassification\n");
			System.exit(1);
		}
		return res;
	}

	@Override
	public String toString() {
		
		return "LDA_entity" + arguments.toString();
	}

	@Override
	public String toGraphvizString() {
		return "LDA[" + String.format("%.2f", arguments.getValue(0)).toString() +","+String.format("%.2f", arguments.getValue(1)).toString()+"]";
	}
}
