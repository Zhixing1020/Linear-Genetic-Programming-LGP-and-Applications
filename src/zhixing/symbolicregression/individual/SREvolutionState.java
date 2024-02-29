package zhixing.symbolicregression.individual;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import ec.simple.SimpleEvolutionState;
import ec.util.Parameter;

public class SREvolutionState extends SimpleEvolutionState{
	
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

}
