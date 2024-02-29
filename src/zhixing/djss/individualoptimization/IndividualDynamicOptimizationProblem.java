package zhixing.djss.individualoptimization;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.individualevaluation.DOEvaluationModel4Ind;

public class IndividualDynamicOptimizationProblem extends IndividualOptimizationProblem{
	@Override
    public void evaluate(EvolutionState state,
                         Individual indi,
                         int subpopulation,
                         int threadnum) {

        if (getObjectives().size() > 1) {
            System.err.println("ERROR:");
            System.err.println("Do NOT support more than one objective yet.");
            System.exit(1);
        }
        
        List fitnesses = new ArrayList();
        fitnesses.add(indi.fitness);

        ((DOEvaluationModel4Ind)evaluationModel).evaluate(fitnesses, (CpxGPInterface4DJSS)indi, state);

        indi.evaluated = true;
    }
}
