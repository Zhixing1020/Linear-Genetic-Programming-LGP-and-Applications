package zhixing.djss.algorithm.multitask.MFEA.individualoptimization;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import yimei.jss.ruleevaluation.SimpleEvaluationModel;
import zhixing.djss.algorithm.multitask.M2GP.individualevaluation.M2GP_MT_EvaluationModel4Ind;
import zhixing.djss.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA4DJSS;
import zhixing.djss.algorithm.multitask.MFEA.individualevaluation.MFEA_MT_EvaluationModel4Ind;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.individualevaluation.SimpleEvaluationModel4Ind;
import zhixing.djss.individualoptimization.IndividualOptimizationProblem;

public class MFEA_IndividualOptimizationProblem extends IndividualOptimizationProblem{
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

        ((MFEA_MT_EvaluationModel4Ind)getEvaluationModel()).evaluate(fitnesses, (LGPIndividual_MFEA4DJSS)indi, ((LGPIndividual_MFEA4DJSS)indi).skillFactor, state);

        indi.evaluated = true;
    }
	
	public void evaluateOneTask(EvolutionState state,
                         Individual indi,
                         int taskind,
                         int threadnum){
		if (getObjectives().size() > 1
        		&& getObjectives().size() != ((SimpleEvaluationModel4Ind)getEvaluationModel()).getSchedulingSet().getSimulations().size()) {
            System.err.println("ERROR:");
            System.err.println("Do NOT support more than one objective yet.");
            System.exit(1);
        }
        
        List fitnesses = new ArrayList();
        fitnesses.add(indi.fitness);

        ((MFEA_MT_EvaluationModel4Ind)getEvaluationModel()).evaluate(fitnesses, (LGPIndividual_MFEA4DJSS)indi, taskind, state);

        indi.evaluated = true;
	}
}
