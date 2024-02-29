package zhixing.symbolicregression.individual;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import ec.simple.SimpleEvolutionState;
import ec.util.Checkpoint;
import ec.util.Parameter;

public class SREvolutionStateNNE extends SimpleEvolutionState{
	
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
		public void run(int condition)
	    {
			double totalTime = 0;

			if (condition == C_STARTED_FRESH) {
				startFresh();
	        }
			else {
				startFromCheckpoint();
	        }

			int result = R_NOTDONE;
			while ( result == R_NOTDONE )
	        {
				long start = yimei.util.Timer.getCpuTime();
				long startUser = yimei.util.Timer.getUserTime();
				long startSys = yimei.util.Timer.getSystemTime();
				result = evolve();

				long finish = yimei.util.Timer.getCpuTime();
				long finishUser = yimei.util.Timer.getUserTime();
				long finishSys = yimei.util.Timer.getSystemTime();
				double duration = (finish - start) / 1000000000;
				double durationUser = (finishUser - startUser) / 1000000000;
				double durationSys = (finishSys - startSys) / 1000000000;
				genTimes.add(duration);
				totalTime += duration;

				output.message("Generation " + (generation-1) + " elapsed " + duration + " seconds, user: " + durationUser +", system: "+durationSys+".");
	        }

			output.message("The whole program elapsed " + totalTime + " seconds.");

			finish(result);

	    }
		
	public int evolve()
    {
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
//        if (generation == numGenerations-1)
        if( nodeEvaluation >= numNodeEva)
            {
            return R_FAILURE;
            }

        // PRE-BREEDING EXCHANGING
        statistics.prePreBreedingExchangeStatistics(this);
        population = exchanger.preBreedingExchangePopulation(this);
        statistics.postPreBreedingExchangeStatistics(this);

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
