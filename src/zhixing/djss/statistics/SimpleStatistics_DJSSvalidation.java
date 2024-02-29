package zhixing.djss.statistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Problem;
import ec.Statistics;
import ec.gp.GPIndividual;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleProblemForm;
import ec.steadystate.SteadyStateStatisticsForm;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.QuickSort;
import ec.util.SortComparatorL;
import yimei.jss.ruleevaluation.AbstractEvaluationModel;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.individualevaluation.SimpleEvaluationModel4Ind;
import zhixing.djss.individualoptimization.IndividualOptimizationProblem;

public class SimpleStatistics_DJSSvalidation extends Statistics implements SteadyStateStatisticsForm{
	
	public static final long validsimSeed = 34567;
	
	static class EliteComparator implements SortComparatorL
    {
	    Individual[] inds;
	    public EliteComparator(Individual[] inds) {super(); this.inds = inds;}
	    public boolean lt(long a, long b)
	        { return inds[(int)a].fitness.betterThan(inds[(int)b].fitness); }
	    public boolean gt(long a, long b)
	        { return inds[(int)b].fitness.betterThan(inds[(int)a].fitness); }
    }
	
	/** Logs the best individual of the generation. */
	boolean warned = false;
	final int K = 10;
	final int V = 10;
	public void postEvaluationStatistics(final EvolutionState state)
    {
    super.postEvaluationStatistics(state);
    
    // for now we just print the best fitness per subpopulation.
    Individual[] best_i = new Individual[state.population.subpops.length];  // quiets compiler complaints
    
    if(state.generation == state.numGenerations - 1) { //perform validation at the final generation 
    	//collect the top 10 individuals for each sub-population, each performs 10 instances
    	
    	Parameter p = new Parameter(state.P_EVALUATOR);
		IndividualOptimizationProblem problem;
		problem = (IndividualOptimizationProblem)(state.parameters.getInstanceForParameter(
                p.push(Evaluator.P_PROBLEM),null,Problem.class));
        problem.setup(state,p.push(Evaluator.P_PROBLEM));
    	
    	for(int sub=0;sub<state.population.subpops.length;sub++)  {
    		
    		//collect the top K individuals
    		Individual[] topIndividuals = new Individual[K]; 
    		
    		int[] orderedPop = new int[state.population.subpops[sub].individuals.length];
            for(int x=0;x<state.population.subpops[sub].individuals.length;x++) {
            	
            	 if (state.population.subpops[sub].individuals[x] == null)
                 {
	                 if (!warned)
	                     {
	                     state.output.warnOnce("Null individuals found in subpopulation");
	                     warned = true;  // we do this rather than relying on warnOnce because it is much faster in a tight loop
	                     }
                 }
            	 
            	 orderedPop[x] = x;
            }

            // sort the best so far where "<" means "not as fit as"
            QuickSort.qsort(orderedPop, new EliteComparator(state.population.subpops[sub].individuals));
            
            Individual[] oldinds = state.population.subpops[sub].individuals;
            for(int i = 0;i<K;i++) {
            	topIndividuals[i] = (Individual)(oldinds[orderedPop[i]].clone());
            }
            
            //perform validation
            double fits[] = new double[K];
            
            for(int i = 0;i<K;i++) {
            	((SimpleEvaluationModel4Ind)problem.getEvaluationModel()).setSimSeed(validsimSeed);
            	
            	for(int vi = 0; vi<V; vi++) {
            		Individual ind = topIndividuals[i];
    				ind.evaluated = false;
    				
    				if (problem.getEvaluationModel().isRotatable()) 
    				{
    					problem.rotateEvaluationModel();
    				}
    				//get EvaluationModel
    				AbstractEvaluationModel evaluationModel = problem.getEvaluationModel();
    				
    				//run the simulation
    		        List<ec.Fitness> fitnesses = new ArrayList();
    		        fitnesses.add(ind.fitness);
    		        ((SimpleEvaluationModel4Ind)evaluationModel).evaluate(fitnesses, (CpxGPInterface4DJSS)ind, state);
    		        
    		        ind.evaluated = true;
    		        
    		        fits[i] += ind.fitness.fitness() / V;
            	}
            	
            }
            
            //find the individual with the best fitness
            best_i[sub] = topIndividuals[0];
            double best_fit_tmp = fits[0];
            for(int i = 1;i<K;i++) {
            	if(best_i[sub] == null || fits[i] < best_fit_tmp) {
            		best_i[sub] = topIndividuals[i];
            		best_fit_tmp = fits[i];
            	}
            		
            }
            
            MultiObjectiveFitness f = (MultiObjectiveFitness) (best_i[sub].fitness);
            f.setObjectives(state, new double[] {best_fit_tmp});
            
         // now test to see if it's the new best_of_run, but the update of "best_of_run" is not dependent on the validation performance
            if (best_of_run[sub]==null || best_i[sub].fitness.betterThan(best_of_run[sub].fitness))
                best_of_run[sub] = (Individual)(best_i[sub].clone());
            
    	}
    	
    }
    else {
    	for(int x=0;x<state.population.subpops.length;x++)
        {
        best_i[x] = state.population.subpops[x].individuals[0];
        for(int y=1;y<state.population.subpops[x].individuals.length;y++)
            {
            if (state.population.subpops[x].individuals[y] == null)
                {
                if (!warned)
                    {
                    state.output.warnOnce("Null individuals found in subpopulation");
                    warned = true;  // we do this rather than relying on warnOnce because it is much faster in a tight loop
                    }
                }
            else if (best_i[x] == null || state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness))
                best_i[x] = state.population.subpops[x].individuals[y];
            if (best_i[x] == null)
                {
                if (!warned)
                    {
                    state.output.warnOnce("Null individuals found in subpopulation");
                    warned = true;  // we do this rather than relying on warnOnce because it is much faster in a tight loop
                    }
                }
            }
    
        // now test to see if it's the new best_of_run
        if (best_of_run[x]==null || best_i[x].fitness.betterThan(best_of_run[x].fitness))
            best_of_run[x] = (Individual)(best_i[x].clone());
        }
    }
    
    
    // print the best-of-generation individual
    if (doGeneration) state.output.println("\nGeneration: " + state.generation,statisticslog);
    if (doGeneration) state.output.println("Best Individual:",statisticslog);
    for(int x=0;x<state.population.subpops.length;x++)
        {
        if (doGeneration) state.output.println("Subpopulation " + x + ":",statisticslog);
        if (doGeneration) best_i[x].printIndividualForHumans(state,statisticslog);
        //========debug,2021.11.28
//        if(doGeneration){
//        	for(int i = 0;i<state.population.subpops[x].individuals.length;i++){
//        		if(state.random[0].nextDouble()<0.1){
//        			state.population.subpops[x].individuals[i].printIndividualForHumans(state, statisticslog);
//            	}
//        	}
//        	
//        }
        //=========
        if (doMessage && !silentPrint) state.output.message("Subpop " + x + " best fitness of generation" + 
            (best_i[x].evaluated ? " " : " (evaluated flag not set): ") +
            best_i[x].fitness.fitnessToStringForHumans());
            
        // describe the winner if there is a description
        if (doGeneration && doPerGenerationDescription) 
            {
            if (state.evaluator.p_problem instanceof SimpleProblemForm)
                ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(state, best_i[x], x, 0, statisticslog);   
            }   
        }
    }
	
	
	
	//========== the following part is the same as those of ec.simple.SimpleStatistics
	public Individual[] getBestSoFar() { return best_of_run; }

    /** log file parameter */
    public static final String P_STATISTICS_FILE = "file";
    
    /** compress? */
    public static final String P_COMPRESS = "gzip";
    
    public static final String P_DO_FINAL = "do-final";
    public static final String P_DO_GENERATION = "do-generation";
    public static final String P_DO_MESSAGE = "do-message";
    public static final String P_DO_DESCRIPTION = "do-description";
    public static final String P_DO_PER_GENERATION_DESCRIPTION = "do-per-generation-description";

    /** The Statistics' log */
    public int statisticslog = 0;  // stdout

    /** The best individual we've found so far */
    public Individual[] best_of_run = null;
        
    /** Should we compress the file? */
    public boolean compress;
    public boolean doFinal;
    public boolean doGeneration;
    public boolean doMessage;
    public boolean doDescription;
    public boolean doPerGenerationDescription;

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);
        
        compress = state.parameters.getBoolean(base.push(P_COMPRESS),null,false);
                
        File statisticsFile = state.parameters.getFile(
            base.push(P_STATISTICS_FILE),null);

        doFinal = state.parameters.getBoolean(base.push(P_DO_FINAL),null,true);
        doGeneration = state.parameters.getBoolean(base.push(P_DO_GENERATION),null,true);
        doMessage = state.parameters.getBoolean(base.push(P_DO_MESSAGE),null,true);
        doDescription = state.parameters.getBoolean(base.push(P_DO_DESCRIPTION),null,true);
        doPerGenerationDescription = state.parameters.getBoolean(base.push(P_DO_PER_GENERATION_DESCRIPTION),null,false);

        if (silentFile)
            {
            statisticslog = Output.NO_LOGS;
            }
        else if (statisticsFile!=null)
            {
            try
                {
                statisticslog = state.output.addLog(statisticsFile, !compress, compress);
                }
            catch (IOException i)
                {
                state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
                }
            }
        else state.output.warning("No statistics file specified, printing to stdout at end.", base.push(P_STATISTICS_FILE));
        }

    public void postInitializationStatistics(final EvolutionState state)
        {
        super.postInitializationStatistics(state);
        
        // set up our best_of_run array -- can't do this in setup, because
        // we don't know if the number of subpopulations has been determined yet
        best_of_run = new Individual[state.population.subpops.length];
        }

    /** Allows MultiObjectiveStatistics etc. to call super.super.finalStatistics(...) without
        calling super.finalStatistics(...) */
    protected void bypassFinalStatistics(EvolutionState state, int result)
        { super.finalStatistics(state, result); }

    /** Logs the best individual of the run. */
    public void finalStatistics(final EvolutionState state, final int result)
        {
        super.finalStatistics(state,result);
        
        // for now we just print the best fitness 
        
        if (doFinal) state.output.println("\nBest Individual of Run:",statisticslog);
        for(int x=0;x<state.population.subpops.length;x++ )
            {
            if (doFinal) state.output.println("Subpopulation " + x + ":",statisticslog);
            if (doFinal) best_of_run[x].printIndividualForHumans(state,statisticslog);
            //========debug,2021.11.28
//            if(doFinal){
//            	for(int i = 0;i<state.population.subpops[x].individuals.length;i++){
//            		if(state.random[0].nextDouble()<0.1){
//            			state.population.subpops[x].individuals[i].printIndividualForHumans(state, statisticslog);
//                	}
//            	}
//            	
//            }
            //=========
            if (doMessage && !silentPrint) state.output.message("Subpop " + x + " best fitness of run: " + best_of_run[x].fitness.fitnessToStringForHumans());

            // finally describe the winner if there is a description
            if (doFinal && doDescription) 
                if (state.evaluator.p_problem instanceof SimpleProblemForm)
                    ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(state, best_of_run[x], x, 0, statisticslog);      
            }
        }
}
