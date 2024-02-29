package zhixing.symbolicregression.algorithm.semantic;

import org.spiderland.Psh.booleanStack;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.GPProblem;
import ec.util.Checkpoint;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.semantic.SubpopulationSLGP;
import zhixing.symbolicregression.individual.SREvolutionStateNNE;

public class SLGPEvolutionState4SR extends SREvolutionStateNNE{
	
	public static final String P_UPDATEINTERVAL = "update_interval";
	public static final String P_STOPBYGEN = "generation_stop";
	public static final String P_STOPBYNNE = "NNE_stop";

	protected GPProblem probm;
	
	protected int libraryUpdateInterval;
	
	protected boolean stopBygen = false;
	protected boolean stopByNNE = true;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		
		Parameter p = new Parameter(P_UPDATEINTERVAL);
        if (parameters.exists(p, null))
            {
        	libraryUpdateInterval = parameters.getInt(p, null, 1);  // 0 would be UNDEFINED
            if (libraryUpdateInterval <= 0)
                output.fatal("If defined, the number of generations of library update interval must be an integer >= 1", p, null);
            }
        else {
        	libraryUpdateInterval = 5;
        }
        
        p = new Parameter(P_STOPBYGEN);
        if (parameters.exists(p, null)) {
        	stopBygen = parameters.getBoolean(p, p, stopBygen);
        }
        
        p = new Parameter(P_STOPBYNNE);
        if (parameters.exists(p, null)) {
        	stopByNNE = parameters.getBoolean(p, p, stopByNNE);
        }
	}
	
	public void startFresh() {
		super.startFresh();
		
		probm = (GPProblem) this.evaluator.p_problem;
		
		for(int p = 0; p<population.subpops.length; p++) {
			if(population.subpops[p] instanceof SubpopulationSLGP) {
				((SubpopulationSLGP) population.subpops[p]).semanticLib.initialize(this, 0, probm); // unthreaded
			}
		}

	}
	
	@Override
	public int evolve()
    {
        if (generation > 0) 
            output.message("Generation " + generation + ", NNE " + nodeEvaluation);

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

        if(generation > 0 && generation%libraryUpdateInterval == 0) {
        	for(int p = 0; p<population.subpops.length; p++) {
    			if(population.subpops[p] instanceof SubpopulationSLGP) {
    				((SubpopulationSLGP) population.subpops[p]).semanticLib.updateLibrary(this, 0); // unthreaded
    			}
    		}
        }
        
        // SHOULD WE QUIT?
//        if (generation == numGenerations-1)
        if( (stopByNNE && nodeEvaluation >= numNodeEva) || (stopBygen && generation >= numGenerations-1))
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
