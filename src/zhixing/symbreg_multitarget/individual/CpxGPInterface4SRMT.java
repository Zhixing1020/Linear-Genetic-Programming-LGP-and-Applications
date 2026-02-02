package zhixing.symbreg_multitarget.individual;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.symbolicregression.individual.CpxGPInterface4SR;
import zhixing.symbreg_multitarget.optimization.GPSymbolicRegressionMultiTarget;

public interface CpxGPInterface4SRMT extends CpxGPInterface4SR{

	Double[] execute_multitar(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem);
}
