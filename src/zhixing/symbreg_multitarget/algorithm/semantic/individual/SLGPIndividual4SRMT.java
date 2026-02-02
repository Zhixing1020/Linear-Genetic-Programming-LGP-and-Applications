package zhixing.symbreg_multitarget.algorithm.semantic.individual;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import zhixing.cpxInd.algorithm.semantic.individual.GPTreeStructSemantic;
import zhixing.cpxInd.algorithm.semantic.library.SemanticVector;
import zhixing.symbolicregression.algorithm.semantic.individual.SLGPIndividual4SR;
import zhixing.symbolicregression.individual.LGPInterface4SR;
import zhixing.symbreg_multitarget.individual.CpxGPInterface4SRMT;
import zhixing.symbreg_multitarget.optimization.GPSymbolicRegressionMultiTarget;

public class SLGPIndividual4SRMT extends SLGPIndividual4SR implements CpxGPInterface4SRMT{

	@Override
	public Double[] execute_multitar(final EvolutionState state,
	        final int thread,
	        final GPData input,
	        final ADFStack stack,
	        final GPIndividual individual,
	        final Problem problem) {
		this.resetRegisters(problem, LGPInterface4SR.initVal, this);
		
		if(!(problem instanceof GPSymbolicRegressionMultiTarget)) {
			System.err.print("SLGPIndividual4SR must accept GPSymbolicRegression problems");
			System.exit(1);
		}
		
		//set the input semantic vector
		if(inputS == null || inputS.size()!= ((GPSymbolicRegressionMultiTarget)problem).getData().size() *this.getNumRegs())
			inputS = new SemanticVector(((GPSymbolicRegressionMultiTarget)problem).getData().size(), this.getNumRegs());
		
		inputS.setSemByRegister(currentDataIndex, registers);
		
		//set the target semantic vector
		if(targetS == null || targetS.size() != ((GPSymbolicRegressionMultiTarget)problem).getData().size()*this.getNumRegs())
			targetS = new SemanticVector(((GPSymbolicRegressionMultiTarget)problem).getData().size(), this.getNumRegs());
		
		final int targetnum = ((GPSymbolicRegressionMultiTarget)problem).getTargetNum();
		
		if(targetnum > numOutputRegs) {
			System.err.print("there are more targets (" + targetnum + ") than output registers(" + numOutputRegs + "), please check the parameter setting\n");
			System.exit(1);
		}
		
		double vals [] = new double [targetnum];
		
		for(int v = 0; v<targetnum; v++) {
			int dimindex = ((GPSymbolicRegressionMultiTarget)problem).getTargets()[v];
			
			vals[v] = ((GPSymbolicRegressionMultiTarget)problem).getDataOutput().get(currentDataIndex)[dimindex];
			
			targetS.set(currentDataIndex*this.getNumRegs()+v, vals[v]);
		}
      
		for(int index = 0; index<this.getTreesLength(); index++){
			GPTreeStructSemantic tree = (GPTreeStructSemantic) this.getTreeStruct(index);
			if(tree.status) {
				tree.child.eval(state, thread, input, stack, this, problem);
			}
			tree.record_semantic(currentDataIndex, registers, ((GPSymbolicRegressionMultiTarget)problem).getData().size());
		}

		Double[] res = new Double [targetnum];
		for(int t = 0; t<targetnum; t++) {
			res[t] =  registers[getOutputRegister()[t]];
		}
		
		return res;
	}
}
