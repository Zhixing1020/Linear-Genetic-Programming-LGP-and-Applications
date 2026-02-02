package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import yimei.jss.gp.data.DoubleData;
import zhixing.cpxInd.individual.primitive.Args;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.symbreg_multitarget.individual.primitive.EntityInterface4SRMT;
import zhixing.optimization.SupervisedProblem;

public class Radius_EntityNode extends Extractor_EntityNode{

	final int attributeNumber = 1;
	final int nonlist_attrNumber = 0;
	
//	@Override
//    public void eval(EvolutionState state, int thread, GPData input,
//                     ADFStack stack, GPIndividual individual, Problem problem) {
//    	DoubleData data = ((DoubleData)input);
//    	
//    	data.value = getAttributes(problem, arguments, 0, 0);
//    	
//    	return;
//    }
	
	public boolean equals(Object other) {
		if(other instanceof Radius_EntityNode) {
			
			if(((Radius_EntityNode)other).getArguments().equals(arguments) ) {
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
		double a0 = 0, a1 = 1, a2 = 1;
		
		if(obj instanceof SupervisedProblem) {
			int index1 = (int) Math.floor(args.getValue(ind++) * ((SupervisedProblem)obj).getDatadim());
			int index2 = (int) Math.floor(args.getValue(ind++) * ((SupervisedProblem)obj).getDatadim());
			
			a0 = args.getValue(ind++);
			a1 = args.getValue(ind++);
			a2 = args.getValue(ind++);
			
			xi = index1;
			
			while(ind < args.getMaxLength() && xi <= index2) {
				res += Math.pow( ((SupervisedProblem)obj).getX()[xi++] - args.getValue(ind++), 2);
			}
			
//			res = a1 * Math.sqrt(res) + a0;
			res = a1 * Math.exp(-a2*a2*res) + a0;
		}
		else {
			System.err.print("Radius_EntityNode only accepts objects of SupervisedProblem\n");
			System.exit(1);
		}
		
		
		return res;
	}
	
	@Override
	public String toString() {
		
		return "Rad_entity" + arguments.toString();
	}

	@Override
	public String toGraphvizString() {
		return "Rad[" + String.format("%.2f", arguments.getValue(0)).toString() +","+String.format("%.2f", arguments.getValue(1)).toString()+"]";
	}
}
