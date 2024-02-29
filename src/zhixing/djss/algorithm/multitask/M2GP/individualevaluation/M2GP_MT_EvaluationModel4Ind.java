package zhixing.djss.algorithm.multitask.M2GP.individualevaluation;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Fitness;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.simulation.Simulation;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.individualevaluation.SimpleEvaluationModel4Ind;
import zhixing.djss.jobshop.SchedulingSet4Ind;
import zhixing.djss.simulation.Simulation4Ind;

public class M2GP_MT_EvaluationModel4Ind extends SimpleEvaluationModel4Ind{

	public void evaluate(List<Fitness> fitnesses,
            CpxGPInterface4DJSS indi,
            int subpopulation,
            EvolutionState state) {

//		List<Simulation4Ind> simulations = new ArrayList<>();
//        List<Integer> replications = new ArrayList<>();
//        List<Objective> objective_list = new ArrayList<>();
//        
//        simulations.add(schedulingSet.getSimulations().get(subpopulation));
//        replications.add(schedulingSet.getReplications().get(subpopulation));
//        objective_list.add(objectives.get(subpopulation % objectives.size()));
//        
//        SchedulingSet4Ind schedulingSet_private = new SchedulingSet4Ind(simulations, replications, objective_list);
		
		Fitness fitness = fitnesses.get(0);
        
        //indi.calcFitnessInd(fitness, state, schedulingSet_private, objective_list);
        indi.calcFitnessInd4OneTask(fitness, state, schedulingSet, objectives, subpopulation);

        //indi.reflectSemantics((GPRuleEvolutionState) state);
    }
}
