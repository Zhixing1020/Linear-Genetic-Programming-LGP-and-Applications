package zhixing.cpxInd.algorithm.Grammar.statistics;

import ec.EvolutionState;
import ec.simple.SimpleProblemForm;
import ec.simple.SimpleStatistics;
import zhixing.cpxInd.algorithm.Grammar.individual.LGPIndividual4Grammar;

public class GrammarLGPStatistics extends SimpleStatistics{
	
	@Override
	/** Logs the best individual of the run. */
    public void finalStatistics(final EvolutionState state, final int result)
        {
        super.finalStatistics(state,result);
        
        // for now we just print the best fitness 
        
        state.output.println("\n\nDerivation tree of the Best Individual of Run:",statisticslog);
        for(int x=0;x<state.population.subpops.length;x++ )
            {
            if (doFinal) state.output.println("Subpopulation " + x + ":",statisticslog);
            if (doFinal) {            	
            	
            	LGPIndividual4Grammar bestOfGeneration = null;
            	
            	for(int i = 0;i<state.population.subpops[x].individuals.length; i++) {
            		if(bestOfGeneration == null 
            				|| state.population.subpops[x].individuals[i].fitness.betterThan(bestOfGeneration.fitness)) {
            			bestOfGeneration = (LGPIndividual4Grammar) state.population.subpops[x].individuals[i];
            		}
            	}
            	
            	String bestGraphVizRule = bestOfGeneration.getDerivationTree().printTreeDOI();
            	
            	state.output.println(bestGraphVizRule, statisticslog);
            	
            	state.output.println("\n\nIndividual:",statisticslog);
            	bestOfGeneration.printIndividualForHumans(state,statisticslog);
            }
                  
            }
        }
}
