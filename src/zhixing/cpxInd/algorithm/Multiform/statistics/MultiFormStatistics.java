package zhixing.cpxInd.algorithm.Multiform.statistics;

import java.util.ArrayList;
import java.util.List;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPSpecies;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.Multiform.individual.LGPIndividual4MForm;
import zhixing.cpxInd.algorithm.Multiform.individual.TGPIndividual4MForm;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.statistics.LGPStatistics;
//import zhixing.djss.individualevaluation.SimpleEvaluationModel4Ind;
//import zhixing.djss.individualoptimization.IndividualOptimizationProblem;

public class MultiFormStatistics extends LGPStatistics{
	
	@Override
	public void postInitializationStatistics(final EvolutionState state){
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
	            
	        CpxGPIndividual i = (CpxGPIndividual)(state.population.subpops[x].individuals[0]);
	        
	        if(i instanceof LGPIndividual){
	        	totalDepthSoFarTree[x] = new long[((LGPIndividual)i).getMaxNumTrees()]; //======zhixing, 2021.3.26
		        totalSizeSoFarTree[x] = new long[((LGPIndividual)i).getMaxNumTrees()];
	        }
	        
	        else if(i instanceof TGPIndividual4MForm){
	        	totalDepthSoFarTree[x] = new long[i.getTreesLength()]; //======zhixing, 2021.3.26
	            totalSizeSoFarTree[x] = new long[i.getTreesLength()];
	        }
	        
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
        
        //overall part
        for(int x=0;x<subpops;x++)
        {                 
            for(int y=0; y<state.population.subpops[x].individuals.length; y++)
                {
                if (state.population.subpops[x].individuals[y].evaluated)               // he's got a valid fitness
                    {
                    // update sizes
                    long size = state.population.subpops[x].individuals[y].size();
                    
                    long proglength = 0, effproglength = 0;
                    if(state.population.subpops[x].individuals[y] instanceof LGPIndividual) {
                    	proglength = ((LGPIndividual)state.population.subpops[x].individuals[y]).getTreesLength();
                    	effproglength = ((LGPIndividual)state.population.subpops[x].individuals[y]).countStatus();
                    }

                    totalSizeThisGen[x] += size;
                    totalSizeSoFar[x] += size;
                    totalIndsThisGen[x] += 1;
                    totalIndsSoFar[x] += 1;
                                        
                    // update fitness
                    if (bestOfGeneration[x]==null ||
                        state.population.subpops[x].individuals[y].fitness.betterThan(bestOfGeneration[x].fitness))
                        {
                        bestOfGeneration[x] = state.population.subpops[x].individuals[y];
                        
                        if(state.population.subpops[x].individuals[y] instanceof LGPIndividual) {
                            absProgLength_bestind[x] = proglength;
                            effProgLength_bestind[x] = effproglength;
                        }

                        if (bestSoFar[x]==null || bestOfGeneration[x].fitness.betterThan(bestSoFar[x].fitness))
                            bestSoFar[x] = (Individual)(bestOfGeneration[x].clone());
                        }
            
                    // sum up mean fitness for population
                    //totalFitnessThisGen[x] += ((KozaFitness)state.population.subpops[x].individuals[y].fitness).standardizedFitness();
                    totalFitnessThisGen[x] += ((MultiObjectiveFitness)state.population.subpops[x].individuals[y].fitness).fitness();
                    
                    //program length and effective program length
                    if(state.population.subpops[x].individuals[y] instanceof LGPIndividual) {
                    	totalAbsProgLength[x] += proglength;
	                    totalEffProgLength[x] += effproglength;
	                    totalEffRate[x] += (double)effproglength / proglength;
                    }
                    
                                        
                    // hook for KozaShortStatistics etc.
                    gatherExtraSubpopStatistics(state, x, y);
                    }
                }
            // compute mean fitness stats
            meanFitnessThisGen[x] = (totalIndsThisGen[x] > 0 ? totalFitnessThisGen[x] / totalIndsThisGen[x] : 0);

        }
        
     // Now gather the fitness information of all Population statistics
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
//            popTotalSizeSoFar += totalSizeSoFar[x];
            popTotalFitness += totalFitnessThisGen[x];
            
//            popAbsProgLength += totalAbsProgLength[x];
//            popEffProgLength += totalEffProgLength[x];
//            popEffRate += totalEffRate[x];
            
            if (bestOfGeneration[x] != null && (popBestOfGeneration == null || bestOfGeneration[x].fitness.betterThan(popBestOfGeneration.fitness))){
            	popBestOfGeneration = bestOfGeneration[x];
//            	popBestAbsProgLength = absProgLength_bestind[x];
//            	popBestEffProgLength = effProgLength_bestind[x];
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
        
//        if(popTotalInds > 0) {
//        	popAbsProgLength /= popTotalInds;
//        	popEffProgLength /= popTotalInds;
//        	popEffRate /= popTotalInds;
//        }
//        else {
//        	popAbsProgLength = 0;
//        	popEffProgLength = 0;
//        	popEffRate = 0;
//        }
        
        //the duration time
        //long finish = yimei.util.Timer.getCpuTime();
        long finish = yimei.util.Timer.getUserTime();
        double duration = (finish - start)/1e9;
                
        // hook for KozaShortStatistics etc.
        if (output) printExtraPopStatisticsBefore(state);

        // optionally print out mean size info
//        if (output && doSize)
//            {
//            state.output.print("" + (popTotalInds > 0 ? popTotalSize / popTotalInds : 0)  + " " , statisticslog);                                           // mean size of pop this gen
//            state.output.print("" + (popTotalIndsSoFar > 0 ? popTotalSizeSoFar / popTotalIndsSoFar : 0) + " " , statisticslog);                             // mean size of pop so far
//            state.output.print("" + (double)(popBestOfGeneration.size()) + " " , statisticslog);                                    // size of best ind of pop this gen
//            state.output.print("" + (double)(popBestSoFar.size()) + " " , statisticslog);                           // size of best ind of pop so far
//            }
                
        // print out fitness info
        if (output)
        {
            state.output.print("" + popMeanFitness + "\t" , statisticslog);                                                                                  // mean fitness of pop this gen
            //state.output.print("" + (((KozaFitness)popBestOfGeneration.fitness).standardizedFitness()) + "\t" , statisticslog);                 // best fitness of pop this gen
            //state.output.print("" + (double)(((KozaFitness)popBestSoFar.fitness).standardizedFitness()) + "\t" , statisticslog);                // best fitness of pop so far
            state.output.print("" + (((MultiObjectiveFitness)popBestOfGeneration.fitness).fitness()) + "\t" , statisticslog);                 // best fitness of pop this gen
            state.output.print("" + (double)(((MultiObjectiveFitness)popBestSoFar.fitness).fitness()) + "\t" , statisticslog);
            state.output.print("" + duration + "\t" , statisticslog);
//            if(isLGP) {
//            	state.output.print(""+ popAbsProgLength + "\t" 
//    					+ popEffProgLength + "\t"
//    					+ popEffRate + "\t"
//    					+ popBestAbsProgLength + "\t"
//    					+ popBestEffProgLength + "\t"
//    					+ popBestEffProgLength / popAbsProgLength + "\t"
//    					+ duration, statisticslog);
//            }
//            else {
//            	state.output.print(""+ (double)(popBestOfGeneration.size()) + "\t" 
//    					+ (double)(popBestSoFar.size()) + "\t"
//    					+ (double)(popTotalSize / popTotalInds) + "\t"
//    					+ duration, statisticslog);
//            }
        }
        
                
        //LGP part,   collect all LGP-related sub population
        state.output.print("\t" , statisticslog);
        for(int x=0;x<subpops;x++)
        {                 
        	if(! (state.population.subpops[x].individuals[0] instanceof LGPIndividual)) continue;
        	
//            for(int y=0; y<state.population.subpops[x].individuals.length; y++)
//                {
//                if (state.population.subpops[x].individuals[y].evaluated)               // he's got a valid fitness
//                    {
//                    // update sizes
//                    long size = state.population.subpops[x].individuals[y].size();
//                    
//                    long proglength = 0, effproglength = 0;
//                    if(state.population.subpops[x].individuals[y] instanceof LGPIndividual4MForm) {
//                    	proglength = ((LGPIndividual)state.population.subpops[x].individuals[y]).getTreesLength();
//                    	effproglength = ((LGPIndividual)state.population.subpops[x].individuals[y]).countStatus();
//                    }
//
//                    totalSizeThisGen[x] += size;
//                    totalSizeSoFar[x] += size;
////                    totalIndsThisGen[x] += 1;
////                    totalIndsSoFar[x] += 1;
//                                        
//                    // update fitness
////                    if (bestOfGeneration[x]==null ||
////                        state.population.subpops[x].individuals[y].fitness.betterThan(bestOfGeneration[x].fitness))
////                        {
////                        bestOfGeneration[x] = state.population.subpops[x].individuals[y];
////                        
////                        absProgLength_bestind[x] = proglength;
////                        effProgLength_bestind[x] = effproglength;
////                        
////                        if (bestSoFar[x]==null || bestOfGeneration[x].fitness.betterThan(bestSoFar[x].fitness))
////                            bestSoFar[x] = (Individual)(bestOfGeneration[x].clone());
////                        }
////            
////                    // sum up mean fitness for population
////                    //totalFitnessThisGen[x] += ((KozaFitness)state.population.subpops[x].individuals[y].fitness).standardizedFitness();
////                    totalFitnessThisGen[x] += ((MultiObjectiveFitness)state.population.subpops[x].individuals[y].fitness).fitness();
//                    
//                    //program length and effective program length
//                    if(state.population.subpops[x].individuals[y] instanceof LGPIndividual4MForm) {
//                    	totalAbsProgLength[x] += proglength;
//	                    totalEffProgLength[x] += effproglength;
//	                    totalEffRate[x] += (double)effproglength / proglength;
//                    }
//                    
//                                        
//                    // hook for KozaShortStatistics etc.
//                    gatherExtraSubpopStatistics(state, x, y);
//                    }
//                }
//            // compute mean fitness stats
//            meanFitnessThisGen[x] = (totalIndsThisGen[x] > 0 ? totalFitnessThisGen[x] / totalIndsThisGen[x] : 0);

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
        
        //now collect all LGP-related sub population
        popTotalInds = 0;
        popTotalIndsSoFar = 0;
        popTotalSize = 0;
        popTotalSizeSoFar = 0;
        popMeanFitness = 0;
        popTotalFitness = 0;
        
        popAbsProgLength = 0;
        popEffProgLength = 0;
        popEffRate = 0;
        popBestAbsProgLength = 0;
        popBestEffProgLength = 0;
        
        popBestOfGeneration = null;
        popBestSoFar = null;
        
        for(int x=0;x<subpops;x++)
        {
        	if(! (state.population.subpops[x].individuals[0] instanceof LGPIndividual)) continue;
        	
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
        
        // print out fitness info
        if (output && popTotalInds>0)
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
					+ popBestEffProgLength / popBestAbsProgLength + "\t", statisticslog);
//            if(isLGP) {
//            	state.output.print(""+ popAbsProgLength + "\t" 
//    					+ popEffProgLength + "\t"
//    					+ popEffRate + "\t"
//    					+ popBestAbsProgLength + "\t"
//    					+ popBestEffProgLength + "\t"
//    					+ popBestEffProgLength / popAbsProgLength + "\t"
//    					+ duration, statisticslog);
//            }
//            else {
//            	state.output.print(""+ (double)(popBestOfGeneration.size()) + "\t" 
//    					+ (double)(popBestSoFar.size()) + "\t"
//    					+ (double)(popTotalSize / popTotalInds) + "\t"
//    					+ duration, statisticslog);
//            }
        }
        

        //TGP part
        state.output.print("\t" , statisticslog);
        for(int x=0;x<subpops;x++)
        {                 
        	if(! (state.population.subpops[x].individuals[0] instanceof TGPIndividual4MForm)) continue;
        	
//            for(int y=0; y<state.population.subpops[x].individuals.length; y++)
//                {
//                if (state.population.subpops[x].individuals[y].evaluated)               // he's got a valid fitness
//                    {
//                    // update sizes
//                    long size = state.population.subpops[x].individuals[y].size();
//                    
//                    long proglength = 0, effproglength = 0;
//                    if(state.population.subpops[x].individuals[y] instanceof LGPIndividual4MForm) {
//                    	proglength = ((LGPIndividual)state.population.subpops[x].individuals[y]).getTreesLength();
//                    	effproglength = ((LGPIndividual)state.population.subpops[x].individuals[y]).countStatus();
//                    }
//
//                    totalSizeThisGen[x] += size;
//                    totalSizeSoFar[x] += size;
////                    totalIndsThisGen[x] += 1;
////                    totalIndsSoFar[x] += 1;
//                                        
//                    // update fitness
////                    if (bestOfGeneration[x]==null ||
////                        state.population.subpops[x].individuals[y].fitness.betterThan(bestOfGeneration[x].fitness))
////                        {
////                        bestOfGeneration[x] = state.population.subpops[x].individuals[y];
////                        
////                        absProgLength_bestind[x] = proglength;
////                        effProgLength_bestind[x] = effproglength;
////                        
////                        if (bestSoFar[x]==null || bestOfGeneration[x].fitness.betterThan(bestSoFar[x].fitness))
////                            bestSoFar[x] = (Individual)(bestOfGeneration[x].clone());
////                        }
////            
////                    // sum up mean fitness for population
////                    //totalFitnessThisGen[x] += ((KozaFitness)state.population.subpops[x].individuals[y].fitness).standardizedFitness();
////                    totalFitnessThisGen[x] += ((MultiObjectiveFitness)state.population.subpops[x].individuals[y].fitness).fitness();
//                    
//                    //program length and effective program length
//                    if(state.population.subpops[x].individuals[y] instanceof LGPIndividual4MForm) {
//                    	totalAbsProgLength[x] += proglength;
//	                    totalEffProgLength[x] += effproglength;
//	                    totalEffRate[x] += (double)effproglength / proglength;
//                    }
//                    
//                                        
//                    // hook for KozaShortStatistics etc.
//                    gatherExtraSubpopStatistics(state, x, y);
//                    }
//                }
//            // compute mean fitness stats
//            meanFitnessThisGen[x] = (totalIndsThisGen[x] > 0 ? totalFitnessThisGen[x] / totalIndsThisGen[x] : 0);

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
        
      //now collect all TGP-related sub population
        popTotalInds = 0;
        popTotalIndsSoFar = 0;
        popTotalSize = 0;
        popTotalSizeSoFar = 0;
        popMeanFitness = 0;
        popTotalFitness = 0;
        
        popBestOfGeneration = null;
        popBestSoFar = null;
        
        for(int x=0;x<subpops;x++)
        {
        	if(! (state.population.subpops[x].individuals[0] instanceof TGPIndividual4MForm)) continue;
        	
	        popTotalInds += totalIndsThisGen[x];
	        popTotalIndsSoFar += totalIndsSoFar[x];
	        popTotalSize += totalSizeThisGen[x];
	        popTotalSizeSoFar += totalSizeSoFar[x];
	        popTotalFitness += totalFitnessThisGen[x];
	        
	        if (bestOfGeneration[x] != null && (popBestOfGeneration == null || bestOfGeneration[x].fitness.betterThan(popBestOfGeneration.fitness)))
	            popBestOfGeneration = bestOfGeneration[x];
	        if (bestSoFar[x] != null && (popBestSoFar == null || bestSoFar[x].fitness.betterThan(popBestSoFar.fitness))) {
	        	popBestSoFar = bestSoFar[x];
	        }
	            
	
	        // hook for KozaShortStatistics etc.
	        gatherExtraPopStatistics(state, x);
        }
                    
	    // build mean
	    popMeanFitness = (popTotalInds > 0 ? popTotalFitness / popTotalInds : 0);               // average out
	
	    // optionally print out mean size info
	    if (output && doSize && popTotalInds>0)
        {
	        state.output.print("" + (popTotalInds > 0 ? popTotalSize / popTotalInds : 0)  + " " , statisticslog);                                           // mean size of pop this gen
	        state.output.print("" + (popTotalIndsSoFar > 0 ? popTotalSizeSoFar / popTotalIndsSoFar : 0) + " " , statisticslog);                             // mean size of pop so far
	        state.output.print("" + (double)(popBestOfGeneration.size()) + " " , statisticslog);                                    // size of best ind of pop this gen
	        state.output.print("" + (double)(popBestSoFar.size()) + " " , statisticslog);                           // size of best ind of pop so far
        }
	            
	    // print out fitness info
	    if (output&& popTotalInds>0)
        {
	        state.output.print("" + popMeanFitness + "\t" , statisticslog);                                                                                  // mean fitness of pop this gen
	        state.output.print("" + (((MultiObjectiveFitness)popBestOfGeneration.fitness).fitness()) + "\t" , statisticslog);                 // best fitness of pop this gen
	        state.output.print("" + (double)(((MultiObjectiveFitness)popBestSoFar.fitness).fitness()) + "\t" , statisticslog);  
	        state.output.print(""+ (double)(popBestOfGeneration.size()) + "\t" 
	        					+ (double)(popBestSoFar.size()) + "\t"
	        					+ ((double)popTotalSize / popTotalInds) + "\t", statisticslog);
        }
                        
        // hook for KozaShortStatistics etc.
        if (output) printExtraPopStatisticsAfter(state);

        // we're done!
        if (output) state.output.println("", statisticslog);
        
        //===================2023.8.15, for the comparison with SLGP, zhixing===========================
//        state.nodeEvaluation += popTotalSize;
        //================================
	}
	
	@Override
	protected void prepareStatistics(EvolutionState state){
		totalDepthThisGenTree = new long[state.population.subpops.length][];
	    totalSizeThisGenTree = new long[state.population.subpops.length][];
	
	    for(int x = 0 ; x < state.population.subpops.length; x++)
	        {
	        CpxGPIndividual i = (CpxGPIndividual)(state.population.subpops[x].individuals[0]);
	        
	        if(i instanceof LGPIndividual){
	        	totalDepthThisGenTree[x] = new long[((LGPIndividual)i).getMaxNumTrees()]; 
		        totalSizeThisGenTree[x] = new long[((LGPIndividual)i).getMaxNumTrees()];
	        }
	        else if(i instanceof TGPIndividual4MForm){
	        	totalDepthThisGenTree[x] = new long[i.getTreesLength()]; 
	            totalSizeThisGenTree[x] = new long[i.getTreesLength()];
	        }
	        
	        }
	}

	protected void gatherExtraSubpopStatistics(EvolutionState state, int subpop, int individual)
	{
	    CpxGPIndividual i = (CpxGPIndividual)(state.population.subpops[subpop].individuals[individual]);
	    for(int z =0; z < i.getTreesLength(); z++) //======zhixing, 2021.3.26
	        {
	        totalDepthThisGenTree[subpop][z] += i.getTree(z).child.depth(); //======zhixing, 2021.3.26
	        totalDepthSoFarTree[subpop][z] += totalDepthThisGenTree[subpop][z];
	        totalSizeThisGenTree[subpop][z] += i.getTree(z).child.numNodes(GPNode.NODESEARCH_ALL);//======zhixing, 2021.3.26
	        totalSizeSoFarTree[subpop][z] += totalSizeThisGenTree[subpop][z];
	        }
	 }
	
	@Override
	public void finalStatistics(final EvolutionState state, final int result){
		
		Individual outIndividual = null;
		
		for(int x=0;x<state.population.subpops.length;x++)
        {                 
            for(int y=0; y<state.population.subpops[x].individuals.length; y++)
            {
                if (state.population.subpops[x].individuals[y].evaluated)               // he's got a valid fitness
            	{                                       
                    // update fitness
                    if (outIndividual==null ||
                        state.population.subpops[x].individuals[y].fitness.betterThan(outIndividual.fitness))
                    {
                    	outIndividual = state.population.subpops[x].individuals[y];
                        
                    }
                }
            }

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
//				List<ec.Fitness> fitnesses = new ArrayList();
//		        fitnesses.add(ind.fitness);
//				if(ind instanceof LGPIndividual4MForm) {
//					
//			        ((SimpleEvaluationModel4Ind)evaluationModel).evaluate(fitnesses, (LGPIndividual4MForm)ind, state);
//				}
//				else {
//					//GPRule rule = new GPRule(((GPIndividual)ind).getTree(0));
//			        //((SimpleEvaluationModel)evaluationModel).evaluate(ind.fitness, rule, state);
//					((SimpleEvaluationModel4Ind)evaluationModel).evaluate(fitnesses, (TGPIndividual4MForm)ind, state);
//				}
//		        
//				
//				//get the fitness and record it into the log file
//		        System.out.print(ind.fitness.fitness() + "\t"); 
//		        state.output.print(ind.fitness.fitness() + "\t", statisticslog);
//			}
//		}
		System.out.println(); 
		state.output.println("", statisticslog);
	}
	
	@Override
	protected void printExtraPopStatisticsBefore(EvolutionState state)
    {
	    int subpops = state.population.subpops.length;
	
	    for(int y = 0; y < subpops; y++)
	    {
	    	long[] totalDepthThisGenTreePop = new long[totalDepthSoFarTree[y].length];
		    long[] totalSizeThisGenTreePop = new long[totalSizeSoFarTree[y].length];                // will assume each subpop has the same tree size
		    long totalIndsThisGenPop = 0;
		    long totalDepthThisGenPop = 0;
		    long totalDepthSoFarPop = 0;
		    
	        totalIndsThisGenPop += totalIndsThisGen[y];
	        for(int z =0; z < totalSizeThisGenTreePop.length; z++)
	            totalSizeThisGenTreePop[z] += totalSizeThisGenTree[y][z];
	        for(int z =0; z < totalDepthThisGenTreePop.length; z++)
	            totalDepthThisGenTreePop[z] += totalDepthThisGenTree[y][z];
        
        
	        if (doDepth)
	        {
		        state.output.print("[ ", statisticslog);
		        for(int z = 0 ; z < totalDepthThisGenTreePop.length; z++)
		            state.output.print("" + (totalIndsThisGenPop > 0 ? ((double)totalDepthThisGenTreePop[z])/totalIndsThisGenPop : 0) + " ",  statisticslog);
		        state.output.print("] ", statisticslog);
	        }
	        if (doSize)
	        {
		        state.output.print("[ ", statisticslog);
		        for(int z = 0 ; z < totalSizeThisGenTreePop.length; z++)
		            state.output.print("" + (totalIndsThisGenPop > 0 ? ((double)totalSizeThisGenTreePop[z])/totalIndsThisGenPop : 0) + " ",  statisticslog);
		        state.output.print("] ", statisticslog);
	        }
	    }
	
	    
    }
}
