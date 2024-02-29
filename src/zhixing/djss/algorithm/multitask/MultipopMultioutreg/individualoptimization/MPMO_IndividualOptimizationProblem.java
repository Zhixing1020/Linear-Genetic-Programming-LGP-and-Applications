package zhixing.djss.algorithm.multitask.MultipopMultioutreg.individualoptimization;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import zhixing.djss.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO4DJSS;
import zhixing.djss.algorithm.multitask.MultipopMultioutreg.individualevaluation.MPMO_MT_EvaluationModel4Ind;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.individualevaluation.SimpleEvaluationModel4Ind;
import zhixing.djss.individualoptimization.IndividualOptimizationProblem;

public class MPMO_IndividualOptimizationProblem extends IndividualOptimizationProblem{
	@Override
    public void evaluate(EvolutionState state,
                         Individual indi,
                         int subpopulation,
                         int threadnum) {

        if (getObjectives().size() > 1
        		&& getObjectives().size() != ((SimpleEvaluationModel4Ind)getEvaluationModel()).getSchedulingSet().getSimulations().size()) {
            System.err.println("ERROR:");
            System.err.println("Do NOT support inconsistent number of objectives and simulations yet.");
            System.exit(1);
        }
        
        List fitnesses = new ArrayList();
        fitnesses.add(indi.fitness);

        ((MPMO_MT_EvaluationModel4Ind)getEvaluationModel()).evaluate(fitnesses, (LGPIndividual_MPMO4DJSS)indi, subpopulation, state);

        indi.evaluated = true;
    }
}
