package zhixing.djss.algorithm.LandscapeOptimization.simpleLGP.toy;

import java.util.LinkedList;

import yimei.jss.gp.terminal.AttributeGPNode;
import yimei.jss.gp.terminal.JobShopAttribute;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.djss.algorithm.LandscapeOptimization.EvolutionStateFLO4DJSS;
import zhixing.djss.individualoptimization.IndividualOptimizationProblem;

public class EvolutionStateFLO4ToyDJSS extends EvolutionStateFLO4DJSS{

	@Override
	public void initTerminalSet() {
		initSimpleTerminalSet();
	}
	
	@Override
	public void startFresh() {
		super.startFresh();
		
		if(population.subpops[0] instanceof SubpopulationFLO_4LGP4ToyDJSS) {
			SubpopulationFLO_4LGP4ToyDJSS pop = (SubpopulationFLO_4LGP4ToyDJSS) population.subpops[0];
			pop.fitnessLandscape.initialize((LGPIndividual) pop.individuals[0], pop.IndList);
			pop.fitnessLandscape.drawFitnessLandscape(this, (IndividualOptimizationProblem) this.evaluator.p_problem, pop.IndList);
			pop.fitnessLandscape.logMetrics(this, 0, (IndividualOptimizationProblem) this.evaluator.p_problem, pop.IndList);
		}
	}
	
	public void initSimpleTerminalSet() {
		terminals = new LinkedList<>();
		for (JobShopAttribute a : JobShopAttribute.simpleAttributes()) {
			terminals.add(new AttributeGPNode(a));
		}
	}
}
