package zhixing.symbolicregression.algorithm.LandscapeOptimization;

import java.util.ArrayList;
import java.util.List;

import ec.Individual;
import ec.util.Checkpoint;
import yimei.jss.ruleoptimisation.RuleOptimizationProblem;
import zhixing.cpxInd.algorithm.LandscapeOptimization.SubpopulationFLO;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.djss.algorithm.LandscapeOptimization.EvolutionStateFLO4DJSS;
import zhixing.symbolicregression.algorithm.LandscapeOptimization.toy.SubpopulationFLO_ToySR;
//import zhixing.jss.cpxInd.individual.LGPIndividual;
import zhixing.symbolicregression.optimization.GPSymbolicRegression;

public class EvolutionStateFLO4SR extends ec.simple.SimpleEvolutionState {

	List<Double> genTimes = new ArrayList<>();
	
	// the best individual in subpopulation
		public Individual bestIndi(int subpop) {
			int best = 0;
			for(int x = 1; x < population.subpops[subpop].individuals.length; x++)
				if (population.subpops[subpop].individuals[x].fitness.betterThan(population.subpops[subpop].individuals[best].fitness))
					best = x;

			return population.subpops[subpop].individuals[best];
		}
		
		@Override
		public void startFresh() {
			super.startFresh();
			
			if(population.subpops[0] instanceof SubpopulationFLO_ToySR) {
				SubpopulationFLO_ToySR pop = (SubpopulationFLO_ToySR) population.subpops[0];
				pop.fitnessLandscape.initialize((LGPIndividual) pop.individuals[0], pop.IndList);
				pop.fitnessLandscape.drawFitnessLandscape(this, (GPSymbolicRegression) this.evaluator.p_problem, pop.IndList);
				pop.fitnessLandscape.logMetrics(this, 0, (GPSymbolicRegression) this.evaluator.p_problem, pop.IndList);
			}
		}

//		@Override
//		public void run(int condition)
//	    {
//			double totalTime = 0;
//
//			if (condition == C_STARTED_FRESH) {
//				startFresh();
//	        }
//			else {
//				startFromCheckpoint();
//	        }
//
//			int result = R_NOTDONE;
//			while ( result == R_NOTDONE )
//	        {
//				long start = yimei.util.Timer.getCpuTime();
//				long startUser = yimei.util.Timer.getUserTime();
//				long startSys = yimei.util.Timer.getSystemTime();
//				result = evolve();
//
//				long finish = yimei.util.Timer.getCpuTime();
//				long finishUser = yimei.util.Timer.getUserTime();
//				long finishSys = yimei.util.Timer.getSystemTime();
//				double duration = (finish - start) / 1000000000;
//				double durationUser = (finishUser - startUser) / 1000000000;
//				double durationSys = (finishSys - startSys) / 1000000000;
//				genTimes.add(duration);
//				totalTime += duration;
//
//				output.message("Generation " + (generation-1) + " elapsed " + duration + " seconds, user: " + durationUser +", system: "+durationSys+".");
//	        }
//
//			output.message("The whole program elapsed " + totalTime + " seconds.");
//
//			finish(result);
//
//	    }
		
		@Override
		public int evolve() {
		    if (generation > 0)
		        output.message("Generation " + generation);

		    // EVALUATION
		    statistics.preEvaluationStatistics(this);
		    evaluator.evaluatePopulation(this);
		    statistics.postEvaluationStatistics(this);

		    // SHOULD WE QUIT?
		    if (evaluator.runComplete(this) && quitOnRunComplete)
		        {
		        output.message("Found Ideal Individual");
		        return R_SUCCESS;
		        }

		    // SHOULD WE QUIT?
		    if (generation == numGenerations-1)
		        {
		        return R_FAILURE;
		        }

		    // PRE-BREEDING EXCHANGING
		    statistics.prePreBreedingExchangeStatistics(this);
		    population = exchanger.preBreedingExchangePopulation(this);
		    statistics.postPreBreedingExchangeStatistics(this);
		    
		    //update the leading board and indexes for sub-populations
		    for(int s = 0; s<population.subpops.length; s++) {
		    	if(population.subpops[s] instanceof SubpopulationFLO) {
		    		
		    		if(generation % ((SubpopulationFLO)population.subpops[s]).updateInterval == 0) {
		    			SubpopulationFLO spop = (SubpopulationFLO) population.subpops[s];
			    		
			    		spop.updateBoard(this, 0);
			    		
		    			int thread = 0; // numThreads==1  		
			    		spop.optimizeIndexes(this, thread);
			    		
			    		System.gc();
		    		}
		    		
		    	}
		    }
		    
		    String exchangerWantsToShutdown = exchanger.runComplete(this);
		    if (exchangerWantsToShutdown!=null)
		        {
		        output.message(exchangerWantsToShutdown);
		        /*
		         * Don't really know what to return here.  The only place I could
		         * find where runComplete ever returns non-null is
		         * IslandExchange.  However, that can return non-null whether or
		         * not the ideal individual was found (for example, if there was
		         * a communication error with the server).
		         *
		         * Since the original version of this code didn't care, and the
		         * result was initialized to R_SUCCESS before the while loop, I'm
		         * just going to return R_SUCCESS here.
		         */

		        return R_SUCCESS;
		        }

		    // BREEDING
		    statistics.preBreedingStatistics(this);

		    population = breeder.breedPopulation(this);

		    // POST-BREEDING EXCHANGING
		    statistics.postBreedingStatistics(this);

		    // POST-BREEDING EXCHANGING
		    statistics.prePostBreedingExchangeStatistics(this);
		    population = exchanger.postBreedingExchangePopulation(this);
		    statistics.postPostBreedingExchangeStatistics(this);

		    // INCREMENT GENERATION AND CHECKPOINT
		    generation++;
		    if (checkpoint && generation%checkpointModulo == 0)
		        {
		        output.message("Checkpointing");
		        statistics.preCheckpointStatistics(this);
		        Checkpoint.setCheckpoint(this);
		        statistics.postCheckpointStatistics(this);
		        }

		    return R_NOTDONE;
		}
}
