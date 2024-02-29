package zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.statistics;

import ec.EvolutionState;
import ec.Individual;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleStatistics;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.fitness.MultitaskMultiObjectiveFitness;
import zhixing.cpxInd.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator;
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA;
import zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO;

public class MPMO_SimpleLGPStatistics extends SimpleStatistics{
	
	public static final String P_SCALARRANK = "rankbyscalarrank";
	
	boolean rankByScalar = false;
	
	@Override
	public void setup(EvolutionState state, Parameter base)
    {
	    super.setup(state, base);
	    
	    rankByScalar = state.parameters.getBoolean(base.push(P_SCALARRANK),null,false);
    }    
	
	
	@Override
	public void postEvaluationStatistics(final EvolutionState state)
    {
		int numTasks = ((MFEA_Evaluator)state.evaluator).getNumTasks();
	    // for now we just print the best fitness per subpopulation.
	    
		Individual[] best_i = new Individual[numTasks];  // quiets compiler complaints
		
		// print the best-of-generation individual
	    if (doGeneration) state.output.println("\nGeneration: " + state.generation,statisticslog);
	    if (doGeneration) state.output.println("Best Individual:",statisticslog);
	    if (doGeneration) state.output.println("Subpopulation " + 0 + ":",statisticslog);
    	
    	for(int t = 0;t<numTasks;t++){
    		//collect the best individual for a task
    		
    		for(int x=0;x<state.population.subpops.length;x++){
    			for(int i = 0;i<state.population.subpops[x].individuals.length;i++){
        			if ((((LGPIndividual_MFEA)state.population.subpops[x].individuals[i]).skillFactor==t 
        					|| ((LGPIndividual_MPMO)state.population.subpops[x].individuals[i]).scalarRank_v.size() == ((MFEA_Evaluator)state.evaluator).getNumTasks())
        					&& state.population.subpops[x].individuals[i].evaluated){
        				if (best_i[t]==null 
        						|| (!rankByScalar && state.population.subpops[x].individuals[i].fitness.betterThan(best_i[t].fitness) )
                                || (rankByScalar && ((MultitaskMultiObjectiveFitness)state.population.subpops[x].individuals[i].fitness).betterThan(((MultitaskMultiObjectiveFitness)best_i[t].fitness),t) ))
                                {
        							best_i[t] = state.population.subpops[x].individuals[i];
                                }
        			}
        		}

    		}
    		//for MPMO individuals
        	MultiObjectiveFitness f = (MultiObjectiveFitness) ((LGPIndividual_MPMO)best_i[t]).fitness;
        	if(f.getNumObjectives() > 1){
    			((LGPIndividual_MPMO)best_i[t]).setCurrentOutputRegister(t);
        	}
    		if (doGeneration) state.output.println("Subtask " + t + ":",statisticslog);
	        if (doGeneration) best_i[t].printIndividualForHumans(state,statisticslog);
    	}
		
    }
	
    public void finalStatistics(final EvolutionState state, final int result)
    {
    
	    // for now we just print the best fitness 
    	int numTasks = ((MFEA_Evaluator)state.evaluator).getNumTasks();
    	
	    if (doFinal) state.output.println("\nBest Individual of Run:",statisticslog);
	    
	    if (doFinal) state.output.println("Subpopulation " + 0 + ":",statisticslog);
    	
    	Individual[] best_i = new Individual[numTasks];  // quiets compiler complaints
    	
    	for(int t = 0; t<numTasks; t++){
    		//collect the best individual for a task
    		
    		for(int x=0;x<state.population.subpops.length;x++ ){
    			for(int i = 0;i<state.population.subpops[x].individuals.length;i++){
        			if (((LGPIndividual_MFEA)state.population.subpops[x].individuals[i]).skillFactor==t
        					|| ((LGPIndividual_MPMO)state.population.subpops[x].individuals[i]).scalarRank_v.size() == ((MFEA_Evaluator)state.evaluator).getNumTasks())
        			{
        				if (best_i[t]==null 
        						|| (!rankByScalar && state.population.subpops[x].individuals[i].fitness.betterThan(best_i[t].fitness) )
                                || (rankByScalar &&((MultitaskMultiObjectiveFitness)state.population.subpops[x].individuals[i].fitness).betterThan(((MultitaskMultiObjectiveFitness)best_i[t].fitness),t) ))
                                {
        							best_i[t] = state.population.subpops[x].individuals[t];
                                }
        			}
        		}
        		
        		
    		}
    		
    		//for MPMO individuals
        	MultiObjectiveFitness f = (MultiObjectiveFitness) ((LGPIndividual_MPMO)best_i[t]).fitness;
        	if(f.getNumObjectives() > 1){
    			((LGPIndividual_MPMO)best_i[t]).setCurrentOutputRegister(t);
        	}
    		if (doFinal) state.output.println("Subtask " + t + ":",statisticslog);
    		if (doFinal) best_i[t].printIndividualForHumans(state,statisticslog);
    	}
    	
	    
	    {
	    	
	        
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
