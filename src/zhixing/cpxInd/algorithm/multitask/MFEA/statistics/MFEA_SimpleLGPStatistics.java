package zhixing.cpxInd.algorithm.multitask.MFEA.statistics;

import ec.EvolutionState;
import ec.Individual;
import ec.simple.SimpleProblemForm;
import ec.simple.SimpleStatistics;
import zhixing.cpxInd.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator;
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA;

public class MFEA_SimpleLGPStatistics extends SimpleStatistics{
	@Override
	public void postEvaluationStatistics(final EvolutionState state)
    {
		int numTasks = ((MFEA_Evaluator)state.evaluator).getNumTasks();
	    // for now we just print the best fitness per subpopulation.
	    
	    for(int x=0;x<state.population.subpops.length;x++)
	    {
	    	Individual[] best_i = new Individual[numTasks];  // quiets compiler complaints
	    	
	    	// print the best-of-generation individual
		    if (doGeneration) state.output.println("\nGeneration: " + state.generation,statisticslog);
		    if (doGeneration) state.output.println("Best Individual:",statisticslog);
		    if (doGeneration) state.output.println("Subpopulation " + x + ":",statisticslog);
	    	
	    	for(int t = 0;t<numTasks;t++){
	    		//collect the best individual for a task
	    		for(int i = 0;i<state.population.subpops[x].individuals.length;i++){
	    			if (((LGPIndividual_MFEA)state.population.subpops[x].individuals[i]).skillFactor==t){
	    				if (best_i[t]==null ||
                                state.population.subpops[x].individuals[i].fitness.betterThan(best_i[t].fitness))
                                {
	    							best_i[t] = state.population.subpops[x].individuals[t];
                                }
	    			}
	    		}
	    		
	    		if (doGeneration) state.output.println("Subtask " + t + ":",statisticslog);
		        if (doGeneration) best_i[t].printIndividualForHumans(state,statisticslog);
	    	}
	    
		    
//		    for(int t=0;t<numTasks;t++)
//		        {
		        
//		        if (doMessage && !silentPrint) state.output.message("Subtask " + t + " best fitness of generation" + 
//		            (best_i[x].evaluated ? " " : " (evaluated flag not set): ") +
//		            best_i[x].fitness.fitnessToStringForHumans());
//		            
//		        // describe the winner if there is a description
//		        if (doGeneration && doPerGenerationDescription) 
//		            {
//		            if (state.evaluator.p_problem instanceof SimpleProblemForm)
//		                ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(state, best_i[x], x, 0, statisticslog);   
//		            }   
//		        }
	    }
    }
	
    public void finalStatistics(final EvolutionState state, final int result)
    {
    
	    // for now we just print the best fitness 
    	int numTasks = ((MFEA_Evaluator)state.evaluator).getNumTasks();
    	
	    if (doFinal) state.output.println("\nBest Individual of Run:",statisticslog);
	    for(int x=0;x<state.population.subpops.length;x++ )
	    {
	    	if (doFinal) state.output.println("Subpopulation " + x + ":",statisticslog);
	    	
	    	Individual[] best_i = new Individual[numTasks];  // quiets compiler complaints
	    	
	    	for(int t = 0; t<numTasks; t++){
	    		//collect the best individual for a task
	    		for(int i = 0;i<state.population.subpops[x].individuals.length;i++){
	    			if (((LGPIndividual_MFEA)state.population.subpops[x].individuals[i]).skillFactor==t){
	    				if (best_i[t]==null ||
                                state.population.subpops[x].individuals[i].fitness.betterThan(best_i[t].fitness))
                                {
	    							best_i[t] = state.population.subpops[x].individuals[t];
                                }
	    			}
	    		}
	    		
	    		if (doFinal) state.output.println("Subtask " + t + ":",statisticslog);
	    		if (doFinal) best_i[t].printIndividualForHumans(state,statisticslog);
	    	}
	        
//	        if (doFinal) best_of_run[x].printIndividualForHumans(state,statisticslog);
//	        if (doMessage && !silentPrint) state.output.message("Subpop " + x + " best fitness of run: " + best_of_run[x].fitness.fitnessToStringForHumans());
//	
//	        // finally describe the winner if there is a description
//	        if (doFinal && doDescription) 
//	            if (state.evaluator.p_problem instanceof SimpleProblemForm)
//	                ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(state, best_of_run[x], x, 0, statisticslog);      
	    }
    }
}
