package zhixing.symbolic_classification.individual;

import java.util.HashMap;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import zhixing.cpxInd.individual.CpxGPInterface4Problem;
import zhixing.symbolic_classification.optimization.GPClassification;

public interface CpxGPInterface4Class extends CpxGPInterface4Problem{

	Double[] execute_outs(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem);
	
	Double[] execute_outs_wrap(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem);
	
//	Double[] predict_label(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem, Double [] outs);
	
//	int getClassNum();
}
