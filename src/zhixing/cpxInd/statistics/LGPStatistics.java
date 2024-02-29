package zhixing.cpxInd.statistics;

import java.util.ArrayList;
import java.util.List;

import org.spiderland.Psh.intStack;

//import com.sun.xml.internal.ws.api.config.management.policy.ManagedServiceAssertion.NestedParameters;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPSpecies;
import ec.gp.koza.KozaFitness;
import ec.gp.koza.KozaShortStatistics;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleProblemForm;
import ec.simple.SimpleStatistics;
import ec.util.Parameter;
//import yimei.jss.ruleevaluation.AbstractEvaluationModel;
import zhixing.cpxInd.individual.LGPIndividual;
//import zhixing.djss.individualevaluation.SimpleEvaluationModel4Ind;
//import zhixing.djss.individualoptimization.IndividualOptimizationProblem;

public class LGPStatistics extends KozaShortStatistics{
	
	protected long start;
	
	public void setup(final EvolutionState state, final Parameter base)
    {
		super.setup(state,base);
    }
	
	@Override
	public void preInitializationStatistics(final EvolutionState state) {
		double totalTime = 0;
		//start = yimei.util.Timer.getCpuTime();
		start = yimei.util.Timer.getUserTime();
	}
	
	@Override
	public void postInitializationStatistics(final EvolutionState state)
    {
	    super.postInitializationStatistics_extensive(state);
	            
	    totalDepthSoFarTree = new long[state.population.subpops.length][];
	    totalSizeSoFarTree = new long[state.population.subpops.length][];
	
	    for(int x = 0 ; x < state.population.subpops.length; x++)
	        {
	        // check to make sure they're the right class
	        if ( !(state.population.subpops[x].species instanceof GPSpecies ))
	            state.output.fatal("Subpopulation " + x +
	                " is not of the species form GPSpecies." + 
	                "  Cannot do timing statistics with KozaShortStatistics.");
	            
	        LGPIndividual i = (LGPIndividual)(state.population.subpops[x].individuals[0]);
	        totalDepthSoFarTree[x] = new long[i.getMaxNumTrees()]; //======zhixing, 2021.3.26
	        totalSizeSoFarTree[x] = new long[i.getMaxNumTrees()];
	        }
    }
	
	@Override
	public void postEvaluationStatistics(final EvolutionState state) {
		//this method will directly use the output log which has been specified in SimpleShortStatistic.setup()
		//since this method does not call super.postEvaluationStatistics(), it cannot call its children.
		//so, do append stat children to this object in the parameter file.
		//in fact, a better way may be re-write a class equal to SimpleShortStatistics or modify the code of SimpleShortStatistics
		boolean output = (state.generation % modulus == 0);

        // gather timings
        if (output && doTime)
            {
            Runtime r = Runtime.getRuntime();
            long curU =  r.totalMemory() - r.freeMemory();          
            state.output.print("" + (System.currentTimeMillis()-lastTime) + " ",  statisticslog);
            }
                        
        int subpops = state.population.subpops.length;                          // number of supopulations
        totalIndsThisGen = new long[subpops];                                           // total assessed individuals
        bestOfGeneration = new Individual[subpops];                                     // per-subpop best individual this generation
        totalSizeThisGen = new long[subpops];                           // per-subpop total size of individuals this generation
        totalFitnessThisGen = new double[subpops];                      // per-subpop mean fitness this generation
        
        long totalAbsProgLength [] = new long[subpops];
        long totalEffProgLength [] = new long[subpops];
        double totalEffRate[] = new double[subpops];
        long absProgLength_bestind[] = new long [subpops];
        long effProgLength_bestind[] = new long [subpops];
        
        double[] meanFitnessThisGen = new double[subpops];                      // per-subpop mean fitness this generation


        prepareStatistics(state);

        // gather per-subpopulation statistics
                
        for(int x=0;x<subpops;x++)
            {                   
            for(int y=0; y<state.population.subpops[x].individuals.length; y++)
                {
                if (state.population.subpops[x].individuals[y].evaluated)               // he's got a valid fitness
                    {
                    // update sizes
                    long size = state.population.subpops[x].individuals[y].size();
                    long proglength = ((LGPIndividual)state.population.subpops[x].individuals[y]).getTreesLength();
                    long effproglength = ((LGPIndividual)state.population.subpops[x].individuals[y]).countStatus();
                    totalSizeThisGen[x] += size;
                    totalSizeSoFar[x] += size;
                    totalIndsThisGen[x] += 1;
                    totalIndsSoFar[x] += 1;
                                        
                    // update fitness
                    if (bestOfGeneration[x]==null ||
                        state.population.subpops[x].individuals[y].fitness.betterThan(bestOfGeneration[x].fitness))
                        {
                        bestOfGeneration[x] = state.population.subpops[x].individuals[y];
                        
                        absProgLength_bestind[x] = proglength;
                        effProgLength_bestind[x] = effproglength;
                        
                        if (bestSoFar[x]==null || bestOfGeneration[x].fitness.betterThan(bestSoFar[x].fitness))
                            bestSoFar[x] = (Individual)(bestOfGeneration[x].clone());
                        }
            
                    // sum up mean fitness for population
                    //totalFitnessThisGen[x] += ((KozaFitness)state.population.subpops[x].individuals[y].fitness).standardizedFitness();
                    totalFitnessThisGen[x] += ((MultiObjectiveFitness)state.population.subpops[x].individuals[y].fitness).fitness();
                    
                    //program length and effective program length
                    totalAbsProgLength[x] += proglength;
                    totalEffProgLength[x] += effproglength;
                    totalEffRate[x] += (double)effproglength / proglength;
                                        
                    // hook for KozaShortStatistics etc.
                    gatherExtraSubpopStatistics(state, x, y);
                    }
                }
            // compute mean fitness stats
            meanFitnessThisGen[x] = (totalIndsThisGen[x] > 0 ? totalFitnessThisGen[x] / totalIndsThisGen[x] : 0);

            // hook for KozaShortStatistics etc.
            if (output && doSubpops) printExtraSubpopStatisticsBefore(state, x);
                        
            // print out optional average size information
            if (output && doSize && doSubpops)
                {
                state.output.print("" + (totalIndsThisGen[x] > 0 ? ((double)totalSizeThisGen[x])/totalIndsThisGen[x] : 0) + " ",  statisticslog);
                state.output.print("" + (totalIndsSoFar[x] > 0 ? ((double)totalSizeSoFar[x])/totalIndsSoFar[x] : 0) + " ",  statisticslog);
                state.output.print("" + (double)(bestOfGeneration[x].size()) + " ", statisticslog);
                state.output.print("" + (double)(bestSoFar[x].size()) + " ", statisticslog);
                }
                        
            // print out fitness information
            if (output && doSubpops)
                {
                state.output.print("" + meanFitnessThisGen[x] + " ", statisticslog);
                state.output.print("" + bestOfGeneration[x].fitness.fitness() + " ", statisticslog);
                state.output.print("" + bestSoFar[x].fitness.fitness() + " ", statisticslog);
                }

            // hook for KozaShortStatistics etc.
            if (output && doSubpops) printExtraSubpopStatisticsAfter(state, x);
            }
  
  
  
        // Now gather per-Population statistics
        long popTotalInds = 0;
        long popTotalIndsSoFar = 0;
        long popTotalSize = 0;
        long popTotalSizeSoFar = 0;
        double popMeanFitness = 0;
        double popTotalFitness = 0;
        
        double popAbsProgLength = 0;
        double popEffProgLength = 0;
        double popEffRate = 0;
        double popBestAbsProgLength = 0;
        double popBestEffProgLength = 0;
        
        Individual popBestOfGeneration = null;
        Individual popBestSoFar = null;
                
        for(int x=0;x<subpops;x++)
            {
            popTotalInds += totalIndsThisGen[x];
            popTotalIndsSoFar += totalIndsSoFar[x];
            popTotalSize += totalSizeThisGen[x];
            popTotalSizeSoFar += totalSizeSoFar[x];
            popTotalFitness += totalFitnessThisGen[x];
            
            popAbsProgLength += totalAbsProgLength[x];
            popEffProgLength += totalEffProgLength[x];
            popEffRate += totalEffRate[x];
            
            if (bestOfGeneration[x] != null && (popBestOfGeneration == null || bestOfGeneration[x].fitness.betterThan(popBestOfGeneration.fitness))){
            	popBestOfGeneration = bestOfGeneration[x];
            	popBestAbsProgLength = absProgLength_bestind[x];
            	popBestEffProgLength = effProgLength_bestind[x];
            }
                
            if (bestSoFar[x] != null && (popBestSoFar == null || bestSoFar[x].fitness.betterThan(popBestSoFar.fitness))) {
            	popBestSoFar = bestSoFar[x];
            	
            }
                

            // hook for KozaShortStatistics etc.
            gatherExtraPopStatistics(state, x);
            }
                        
        //===================2023.8.15, for the comparison with SLGP, zhixing===========================
        state.nodeEvaluation += popTotalSize;
//        state.nodeEvaluation += popEffProgLength*popTotalInds*2;  // serve as the number of node evaluation
        //====================================
        
        // build mean
        popMeanFitness = (popTotalInds > 0 ? popTotalFitness / popTotalInds : 0);               // average out
        
        if(popTotalInds > 0) {
        	popAbsProgLength /= popTotalInds;
        	popEffProgLength /= popTotalInds;
        	popEffRate /= popTotalInds;
        }
        else {
        	popAbsProgLength = 0;
        	popEffProgLength = 0;
        	popEffRate = 0;
        }
        
        //the duration time
        //long finish = yimei.util.Timer.getCpuTime();
        long finish = yimei.util.Timer.getUserTime();
        double duration = (finish - start)/1e9;
                
        // hook for KozaShortStatistics etc.
        if (output) printExtraPopStatisticsBefore(state);

        // optionally print out mean size info
        if (output && doSize)
            {
            state.output.print("" + (popTotalInds > 0 ? popTotalSize / popTotalInds : 0)  + " " , statisticslog);                                           // mean size of pop this gen
            state.output.print("" + (popTotalIndsSoFar > 0 ? popTotalSizeSoFar / popTotalIndsSoFar : 0) + " " , statisticslog);                             // mean size of pop so far
            state.output.print("" + (double)(popBestOfGeneration.size()) + " " , statisticslog);                                    // size of best ind of pop this gen
            state.output.print("" + (double)(popBestSoFar.size()) + " " , statisticslog);                           // size of best ind of pop so far
            }
                
        // print out fitness info
        if (output)
            {
            state.output.print("" + popMeanFitness + "\t" , statisticslog);                                                                                  // mean fitness of pop this gen
            //state.output.print("" + (((KozaFitness)popBestOfGeneration.fitness).standardizedFitness()) + "\t" , statisticslog);                 // best fitness of pop this gen
            //state.output.print("" + (double)(((KozaFitness)popBestSoFar.fitness).standardizedFitness()) + "\t" , statisticslog);                // best fitness of pop so far
            state.output.print("" + (((MultiObjectiveFitness)popBestOfGeneration.fitness).fitness()) + "\t" , statisticslog);                 // best fitness of pop this gen
            state.output.print("" + (double)(((MultiObjectiveFitness)popBestSoFar.fitness).fitness()) + "\t" , statisticslog);  
            state.output.print(""+ popAbsProgLength + "\t" 
            					+ popEffProgLength + "\t"
            					+ popEffRate + "\t"
            					+ popBestAbsProgLength + "\t"
            					+ popBestEffProgLength + "\t"
            					+ popBestEffProgLength / popBestAbsProgLength + "\t"
            					+ duration, statisticslog);
            }
                        
        // hook for KozaShortStatistics etc.
        if (output) printExtraPopStatisticsAfter(state);

        // we're done!
        if (output) state.output.println("", statisticslog);
	}


	protected void prepareStatistics(EvolutionState state)
	{
	    totalDepthThisGenTree = new long[state.population.subpops.length][];
	    totalSizeThisGenTree = new long[state.population.subpops.length][];
	
	    for(int x = 0 ; x < state.population.subpops.length; x++)
	        {
	        LGPIndividual i = (LGPIndividual)(state.population.subpops[x].individuals[0]);
	        totalDepthThisGenTree[x] = new long[i.getMaxNumTrees()]; //======zhixing, 2021.3.26
	        totalSizeThisGenTree[x] = new long[i.getMaxNumTrees()];
	        }
	}
	
	
	protected void gatherExtraSubpopStatistics(EvolutionState state, int subpop, int individual)
	{
	    LGPIndividual i = (LGPIndividual)(state.population.subpops[subpop].individuals[individual]);
	    for(int z =0; z < i.getTreesLength(); z++) //======zhixing, 2021.3.26
	        {
	        totalDepthThisGenTree[subpop][z] += i.getTree(z).child.depth(); //======zhixing, 2021.3.26
	        totalDepthSoFarTree[subpop][z] += totalDepthThisGenTree[subpop][z];
	        totalSizeThisGenTree[subpop][z] += i.getTree(z).child.numNodes(GPNode.NODESEARCH_ALL);//======zhixing, 2021.3.26
	        totalSizeSoFarTree[subpop][z] += totalSizeThisGenTree[subpop][z];
	        }
	 }
	
	public void finalStatistics(final EvolutionState state, final int result)
    {
	    
	    if(bestOfGeneration == null){
			System.out.println("The best individuals should be null?");
			return;
		}

		//get the number of testing simulation -> TODO: add a for loop to simulate the best individual with different seeds
//	    Parameter p = new Parameter(state.P_EVALUATOR);
//		IndividualOptimizationProblem problem;
//		problem = (IndividualOptimizationProblem)(state.parameters.getInstanceForParameter(
//                p.push(Evaluator.P_PROBLEM),null,Problem.class));
//        problem.setup(state,p.push(Evaluator.P_PROBLEM));
        
        //===========debug2021.12.1
//        ((SimpleEvaluationModel4Ind)problem.getEvaluationModel()).setSimSeed(968356);
        //================
        
        //get the output individual
        Individual outIndividual = bestOfGeneration[0];
        //Individual outIndividual = state.population.subpops[0].individuals[0];
//        double bestFit = 1e7;
//        for(int x = 0;x<state.population.subpops[0].individuals.length;x++) {
//        	Individual ind = state.population.subpops[0].individuals[x];
//        	ind.evaluated = false;
//        	
//        	//get EvaluationModel
//			AbstractEvaluationModel evaluationModel = problem.getEvaluationModel();
//			
//			//run the simulation
//	        List<ec.Fitness> fitnesses = new ArrayList();
//	        fitnesses.add(ind.fitness);
//	        ((SimpleEvaluationModel4Ind)evaluationModel).evaluate(fitnesses, (LGPIndividual)ind, state);
//	        
//	        if(ind.fitness.fitness() <= bestFit) {
//	        	outIndividual = ind;
//	        }
//        }
        
        //for validation 
//		for(int ti = 0; ti< 100; ti++) {
//			for(int x = 0;x<state.population.subpops.length; x++){
//				//get the best individual
//				//Individual ind = bestOfGeneration[x];
//				Individual ind = outIndividual;
//				ind.evaluated = false;
//				
//				if (problem.getEvaluationModel().isRotatable()) 
//				{
//					problem.rotateEvaluationModel();
//				}
//				//get EvaluationModel
//				AbstractEvaluationModel evaluationModel = problem.getEvaluationModel();
//				
//				//run the simulation
//		        List<ec.Fitness> fitnesses = new ArrayList();
//		        fitnesses.add(ind.fitness);
//		        ((SimpleEvaluationModel4Ind)evaluationModel).evaluate(fitnesses, (LGPIndividual)ind, state);
//				
//				//get the fitness and record it into the log file
//		        System.out.print(ind.fitness.fitness() + "\t"); 
//		        state.output.print(ind.fitness.fitness() + "\t", statisticslog);
//			}
//		}
//		System.out.println(); 
//		state.output.println("", statisticslog);
    }


}
