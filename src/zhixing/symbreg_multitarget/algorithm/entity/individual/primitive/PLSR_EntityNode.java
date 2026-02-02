package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import zhixing.cpxInd.individual.primitive.Args;
import zhixing.optimization.SupervisedProblem;

public class PLSR_EntityNode  extends Extractor_EntityNode{

	public boolean equals(Object other) {
        if (other instanceof PLSR_EntityNode) {
        	
        	if(((PLSR_EntityNode)other).getArguments().equals(arguments)) {
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
			
			res = args.getValue(ind++);
			xi = index1;
			
			while(ind < args.getMaxLength() && xi <= index2) {
				res += ((SupervisedProblem)obj).getX()[xi++] * args.getValue(ind++);
			}
		}
		else {
			System.err.print("LR_EntityNode only accepts objects of SupervisedProblem\n");
			System.exit(1);
		}
		return res;
	}

	@Override
	public String toString() {
		
		return "PLSR_entity" + arguments.toString();
	}

	@Override
	public String toGraphvizString() {
		return "PLSR[" + String.format("%.2f", arguments.getValue(0)).toString() +","+String.format("%.2f", arguments.getValue(1)).toString()+"]";
	}
}
