package zhixing.cpxInd.algorithm.multitask.MFEA.statistics;

import java.util.ArrayList;
import java.util.List;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;
import yimei.jss.ruleevaluation.AbstractEvaluationModel;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.statistics.LGPStatistics;
import zhixing.cpxInd.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator;
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA;
//import zhixing.djss.algorithm.multitask.MFEA.individualevaluation.MFEA_MT_EvaluationModel4Ind;
//import zhixing.djss.individualoptimization.IndividualOptimizationProblem;

public class MFEA_LGPStatistics extends LGPStatistics{
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
        
        int numTasks = ((MFEA_Evaluator)state.evaluator).getNumTasks();
        
        //population informatioin
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
        
        Individual bestIndividualperTask[] = new Individual [numTasks];
        
        for(int t = 0;t<numTasks;t++){
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
                    if (((LGPIndividual_MFEA)state.population.subpops[x].individuals[y]).skillFactor==t 
                    		&& state.population.subpops[x].individuals[y].evaluated)               // he's got a valid fitness
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
                            bestIndividualperTask[t] = bestOfGeneration[x] = state.population.subpops[x].individuals[y];
                            
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
                
                //collect information of every subpop
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
        }
        
                        
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
            					+ popBestEffProgLength / popAbsProgLength + "\t"
            					+ duration, statisticslog);
            
            for(int x = 0;x<subpops;x++){
            	for(int t = 0;t<numTasks;t++){
            		state.output.print("\t\t" + ((MultiObjectiveFitness)bestIndividualperTask[t].fitness).fitness() + "\t" , statisticslog);
            		state.output.print("" + ((LGPIndividual)bestIndividualperTask[t]).getTreesLength() + "\t" , statisticslog);
            		state.output.print("" + ((LGPIndividual)bestIndividualperTask[t]).getEffTreesLength() + "\t" , statisticslog);
            		state.output.print("" + ((LGPIndividual)bestIndividualperTask[t]).getEffTreesLength()/((LGPIndividual)bestIndividualperTask[t]).getEffTreesLength() + "\t" , statisticslog);
            		
            		state.output.message("The best fitness for task " + t + ": "+ ((MultiObjectiveFitness)bestIndividualperTask[t].fitness).fitness());
            	}
            	
            }
            }
                        
        // hook for KozaShortStatistics etc.
        if (output) printExtraPopStatisticsAfter(state);

        // we're done!
        if (output) state.output.println("", statisticslog);
	}
	
	public void finalStatistics(final EvolutionState state, final int result)
    {
	    
//	    if(bestOfGeneration == null){
//			System.out.println("The best individuals should be null?");
//			return;
//		}
//	    
//	    int numTasks = ((MFEA_Evaluator)state.evaluator).getNumTasks();

		//get the number of testing simulation -> TODO: add a for loop to simulate the best individual with different seeds
//	    Parameter p = new Parameter(state.P_EVALUATOR);
//		IndividualOptimizationProblem problem;
//		problem = (IndividualOptimizationProblem)(state.parameters.getInstanceForParameter(
//                p.push(Evaluator.P_PROBLEM),null,Problem.class));
//        problem.setup(state,p.push(Evaluator.P_PROBLEM));
        
        //get the output individual
        //Individual outIndividual = bestOfGeneration[0];
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
//        Individual bestIndividualperTask[] = new Individual [numTasks];
//        
//        for(int t = 0;t<numTasks;t++){
//        	bestOfGeneration = new Individual[state.population.subpops.length];                                     // per-subpop best individual this generation
//        	for(int x = 0;x<state.population.subpops.length;x++){
//        		for(int i = 0;i<state.population.subpops[x].individuals.length;i++){
//        			 if (((LGPIndividual_MFEA)state.population.subpops[x].individuals[i]).skillFactor==t){
//        				 if (bestOfGeneration[x]==null ||
//                                 state.population.subpops[x].individuals[i].fitness.betterThan(bestOfGeneration[x].fitness))
//                                 {
//                                 bestIndividualperTask[t] = bestOfGeneration[x] = state.population.subpops[x].individuals[i];
//                                 
//                                 if (bestSoFar[x]==null || bestOfGeneration[x].fitness.betterThan(bestSoFar[x].fitness))
//                                     bestSoFar[x] = (Individual)(bestOfGeneration[x].clone());
//                                 }
//        			 }
//        		}
//        	}
//        }
//        
//        for(int x = 0;x<state.population.subpops.length; x++) {
//        	for(int t = 0;t<numTasks;t++){
//        		Individual ind = bestIndividualperTask[t];
//        		for(int ti = 0; ti< 50; ti++) {
//    				//get the best individual
//    				
//    				ind.evaluated = false;
//    				
//    				problem.rotateEvaluationModel();
//    				//get EvaluationModel
//    				AbstractEvaluationModel evaluationModel = problem.getEvaluationModel();
//    				
//    				//run the simulation
//    		        List<ec.Fitness> fitnesses = new ArrayList();
//    		        fitnesses.add(ind.fitness);
//    		        ((MFEA_MT_EvaluationModel4Ind)evaluationModel).evaluate(fitnesses, (LGPIndividual_MFEA)ind, t, state);
//    				
//    				//get the fitness and record it into the log file
//    		        System.out.print(ind.fitness.fitness() + "\t"); 
//    		        state.output.print(ind.fitness.fitness() + "\t", statisticslog);
//    			}
//        		System.out.println(""); 
//            	state.output.println("", statisticslog);
//        	}
//		}
//		System.out.println(); 
//		state.output.println("", statisticslog);
    }
}
