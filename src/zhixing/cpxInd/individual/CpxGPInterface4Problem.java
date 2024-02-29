package zhixing.cpxInd.individual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;

public interface CpxGPInterface4Problem {

	void resetRegisters(final Problem problem, double val, final CpxGPIndividual ind);
	
	void prepareExecution(EvolutionState state);
	
	double execute(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem);
	
	ArrayList<Pair<String, ArrayList<String>>> getAdjacencyTable(EvolutionState state, int start, int end);

	String makeGraphvizRule(List<Integer> outputRegs);
}
