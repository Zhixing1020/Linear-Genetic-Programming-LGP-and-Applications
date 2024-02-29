package zhixing.djss.individualoptimization;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPProblem;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import yimei.jss.jobshop.Objective;
import yimei.jss.ruleevaluation.AbstractEvaluationModel;
import yimei.jss.ruleoptimisation.RuleOptimizationProblem;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.individualevaluation.SimpleEvaluationModel4Ind;

public class IndividualOptimizationProblem extends RuleOptimizationProblem implements SimpleProblemForm {
	public static final String P_EVAL_MODEL = "eval-model";

    //private AbstractEvaluationModel evaluationModel;

    public List<Objective> getObjectives() {
        return evaluationModel.getObjectives();
    }

    public AbstractEvaluationModel getEvaluationModel() {
        return evaluationModel;
    }

    public void rotateEvaluationModel() {
        evaluationModel.rotate();
    }

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

//        Parameter p = base.push(P_EVAL_MODEL);
//        evaluationModel = (AbstractEvaluationModel)(
//                state.parameters.getInstanceForParameter(
//                        p, null, AbstractEvaluationModel.class));
//
//        evaluationModel.setup(state, p);
    }

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

        ((SimpleEvaluationModel4Ind)evaluationModel).evaluate(fitnesses, (CpxGPInterface4DJSS)indi, state);

        indi.evaluated = true;
    }
}
