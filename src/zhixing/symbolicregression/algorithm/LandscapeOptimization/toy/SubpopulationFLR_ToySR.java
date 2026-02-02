package zhixing.symbolicregression.algorithm.LandscapeOptimization.toy;

import ec.EvolutionState;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.symbolicregression.optimization.GPSymbolicRegression;

public class SubpopulationFLR_ToySR extends SubpopulationFLO_ToySR {

	
	@Override
	public void optimizeIndexes(EvolutionState state, int thread) {
//		fitnessLandscape.drawFitnessLandscape((EvolutionStateFLO) state, (GPSymbolicRegression) state.evaluator.p_problem, IndList);
		
		IndList.optimizeIndex(state, thread, this, fullBoard);
		
		fitnessLandscape.initialize((LGPIndividual) individuals[0], IndList);
		
		fitnessLandscape.updateFitnessLandscape(state, fullBoard, IndList);
		
		fitnessLandscape.drawFitnessLandscape(state, (GPSymbolicRegression) state.evaluator.p_problem, IndList);
		
		if(state.generation % this.logMetricInterval == 0 || state.generation == state.numGenerations-2) {
			fitnessLandscape.logMetrics(state, thread, (GPSymbolicRegression) state.evaluator.p_problem, IndList);
		}
		
	}
}
