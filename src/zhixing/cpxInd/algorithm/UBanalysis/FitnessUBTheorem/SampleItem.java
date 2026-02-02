package zhixing.cpxInd.algorithm.UBanalysis.FitnessUBTheorem;

import ec.EvolutionState;
import ec.app.tutorial4.DoubleData;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import zhixing.cpxInd.algorithm.semantic.individual.SLGPIndividual;
import zhixing.cpxInd.algorithm.semantic.library.SemanticVector;


public class SampleItem {

	public SLGPIndividual g_1;
	public SLGPIndividual g_2;
	
	public double d = 0;
	public double Detailfit = 0;
	
	public SampleItem (SLGPIndividual g_1, SLGPIndividual g_2) {

		this.g_1 = g_1;
		this.g_2 = g_2;
		
		this.d = FitnessUB.countInstrEditDis(g_1, g_2);
	}
	
	public SampleItem (SLGPIndividual g_1, SLGPIndividual g_2, int d) {

		this.g_1 = g_1;
		this.g_2 = g_2;
		
		this.d = d;
	}
	
	protected void evaluateProgram(EvolutionState state, SLGPIndividual ind, GPProblem problem) {
		problem.evaluate(state, ind, 0, 0);
	}
	
	public void evaluateItem(EvolutionState state, GPProblem problem) {
		
		evaluateProgram(state, g_1, problem);
		evaluateProgram(state, g_2, problem);
		
		Detailfit = Math.abs(g_1.fitness.fitness() - g_2.fitness.fitness());
	}
}
