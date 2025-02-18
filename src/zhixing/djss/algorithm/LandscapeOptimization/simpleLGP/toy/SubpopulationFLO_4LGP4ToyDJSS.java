package zhixing.djss.algorithm.LandscapeOptimization.simpleLGP.toy;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.LandscapeOptimization.SubpopulationFLO;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
import zhixing.djss.individualoptimization.IndividualOptimizationProblem;

public class SubpopulationFLO_4LGP4ToyDJSS  extends SubpopulationFLO{

public LGPFitnessLandscape fitnessLandscape = new LGPFitnessLandscape();
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		fitnessLandscape.setup(state, base);
	}
	
	@Override
	public void optimizeIndexes(EvolutionState state, int thread) {
//		fitnessLandscape.drawFitnessLandscape((EvolutionStateFLO) state, (GPSymbolicRegression) state.evaluator.p_problem, IndList);
		
		IndList.optimizeIndex(state, thread, this, fullBoard);
		
		fitnessLandscape.updateFitnessLandscape(state, fullBoard, IndList);
		
		fitnessLandscape.drawFitnessLandscape(state, (IndividualOptimizationProblem) state.evaluator.p_problem, IndList);
		
		if(state.generation % this.logMetricInterval == 0 || state.generation == state.numGenerations-2) {
			fitnessLandscape.logMetrics(state, thread, (IndividualOptimizationProblem) state.evaluator.p_problem, IndList);
		}
		
	}
}
