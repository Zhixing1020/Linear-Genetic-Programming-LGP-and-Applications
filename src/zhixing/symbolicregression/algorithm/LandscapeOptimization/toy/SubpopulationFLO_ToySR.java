package zhixing.symbolicregression.algorithm.LandscapeOptimization.toy;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.LandscapeOptimization.SubpopulationFLO;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
//import zhixing.djss.algorithm.LandscapeOptimization.EvolutionStateFLO4DJSS;
//import zhixing.djss.individual.LGPIndividual4DJSS;
import zhixing.symbolicregression.optimization.GPSymbolicRegression;

public class SubpopulationFLO_ToySR extends SubpopulationFLO{

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
		
		fitnessLandscape.drawFitnessLandscape(state, (GPSymbolicRegression) state.evaluator.p_problem, IndList);
		
		if(state.generation % this.logMetricInterval == 0 || state.generation == state.numGenerations-2) {
			fitnessLandscape.logMetrics(state, thread, (GPSymbolicRegression) state.evaluator.p_problem, IndList);
		}
		
	}
}
