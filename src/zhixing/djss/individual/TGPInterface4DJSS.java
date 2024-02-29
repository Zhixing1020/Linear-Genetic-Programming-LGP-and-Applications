package zhixing.djss.individual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import yimei.jss.gp.CalcPriorityProblem;
import yimei.jss.gp.data.DoubleData;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.state.SystemState;
import zhixing.cpxInd.individual.CpxGPIndividual;

public interface TGPInterface4DJSS extends CpxGPInterface4DJSS{

	
	@Override
	default double priority(Operation op, WorkCenter workCenter, SystemState systemState) {
		CalcPriorityProblem calcPrioProb =
                new CalcPriorityProblem(op, workCenter, systemState);

//        DoubleData tmp = new DoubleData();
//        
//        //assuming there is only one tree
//        trees[0].child.eval(null, 0, tmp, null, null, calcPrioProb);
//
//        return tmp.value;
        
        return execute(null, 0, null, null, (GPIndividual) this, calcPrioProb);
	}
	
	@Override
	default double execute(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual,
			Problem problem) {
//		if(input == null) {
//			input = new DoubleData();
//		}
		DoubleData tmp = new DoubleData();
		
        //assuming there is only one tree
        individual.getTrees()[0].child.eval(state, thread, tmp, stack, (GPIndividual) this, problem);

        input = tmp;
        return tmp.value;
	}
	
	@Override
	default void resetRegisters(Problem problem, double val, CpxGPIndividual ind) {
		// TODO Auto-generated method stub
		
	}

	@Override
	default void prepareExecution(EvolutionState state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	default ArrayList<Pair<String, ArrayList<String>>> getAdjacencyTable(EvolutionState state, int start, int end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	default String makeGraphvizRule(List<Integer> outputRegs) {
		// TODO Auto-generated method stub
		return null;
	}
}
