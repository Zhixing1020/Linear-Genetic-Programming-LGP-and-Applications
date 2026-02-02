package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import zhixing.cpxInd.individual.primitive.Args;
import zhixing.optimization.SupervisedProblem;

public class CondLR_EntityNode extends Extractor_EntityNode{

	public boolean equals(Object other) {
        if (other instanceof CondLR_EntityNode) {
        	
        	if(((CondLR_EntityNode)other).getArguments().equals(arguments)) {
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
			int index1 = (int) Math.floor(args.getValue(ind++) * ((SupervisedProblem)obj).getDatadim());
			int index2 = (int) Math.floor(args.getValue(ind++) * ((SupervisedProblem)obj).getDatadim());
			
			int index3 = (int) Math.floor(index1 + args.getValue(ind++) * (index2 - index1)); //conditioning variable
			
			double min_val = args.getValue(ind++); //* ((SupervisedProblem)obj).getDataMax()[index3];
			double max_val = args.getValue(ind++); //* ((SupervisedProblem)obj).getDataMin()[index3];
			
			res = args.getValue(ind++);
			xi = index1;
			
			if(((SupervisedProblem)obj).getX()[index3]>=min_val && ((SupervisedProblem)obj).getX()[index3]<=max_val) {
				while(ind < args.getMaxLength() && xi <= index2) {
					res += ((SupervisedProblem)obj).getX()[xi++] * args.getValue(ind++);
				}
			}
			
		}
		else {
			System.err.print("CondLR_EntityNode only accepts objects of SupervisedProblem\n");
			System.exit(1);
		}
		return res;
	}

	@Override
	public String toString() {
		
		return "CondLR_entity" + arguments.toString();
	}

	@Override
	public String toGraphvizString() {
		return "CondLR[" + String.format("%.2f", arguments.getValue(0)).toString() +","+String.format("%.2f", arguments.getValue(1)).toString()+"]";
	}
}
