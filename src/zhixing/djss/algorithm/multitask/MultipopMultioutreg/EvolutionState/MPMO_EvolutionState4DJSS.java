package zhixing.djss.algorithm.multitask.MultipopMultioutreg.EvolutionState;

import ec.util.Checkpoint;
import yimei.jss.gp.GPRuleEvolutionState;
import yimei.jss.ruleoptimisation.RuleOptimizationProblem;
import zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.breeder.MPMO_Breeder;
import zhixing.djss.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator4DJSS;

public class MPMO_EvolutionState4DJSS extends GPRuleEvolutionState {
	
//	@Override
//	public void startFresh() 
//    {
//    output.message("Setting up");
//    setup(this,null);  // a garbage Parameter
//
//    // POPULATION INITIALIZATION
//    output.message("Initializing Generation 0");
//    statistics.preInitializationStatistics(this);
//    population = initializer.initialPopulation(this, 0); // unthreaded
//    statistics.postInitializationStatistics(this);
//    
//    // Compute generations from evaluations if necessary
//    if (numEvaluations > UNDEFINED)
//        {
//        // compute a generation's number of individuals
//        int generationSize = 0;
//        for (int sub=0; sub < population.subpops.length; sub++)  
//            { 
//            generationSize += population.subpops[sub].individuals.length;  // so our sum total 'generationSize' will be the initial total number of individuals
//            }
//            
//        if (numEvaluations < generationSize)
//            {
//            numEvaluations = generationSize;
//            numGenerations = 1;
//            output.warning("Using evaluations, but evaluations is less than the initial total population size (" + generationSize + ").  Setting to the populatiion size.");
//            }
//        else 
//            {
//            if (numEvaluations % generationSize != 0)
//                output.warning("Using evaluations, but initial total population size does not divide evenly into it.  Modifying evaluations to a smaller value ("
//                    + ((numEvaluations / generationSize) * generationSize) +") which divides evenly.");  // note integer division
//            numGenerations = (int)(numEvaluations / generationSize);  // note integer division
//            numEvaluations = numGenerations * generationSize;
//            } 
//        output.message("Generations will be " + numGenerations);
//        }    
//    
//    //evaluate all individuals on all different tasks and identify the skill factor
//    ((MFEA_Evaluator)evaluator).identifySkillFactor(this);
//
//    // INITIALIZE CONTACTS -- done after initialization to allow
//    // a hook for the user to do things in Initializer before
//    // an attempt is made to connect to island models etc.
//    exchanger.initializeContacts(this);
//    evaluator.initializeContacts(this);
//    }
	
	public int [] numXov = null;
	public int [] numSharedXov = null;
	
	@Override
	public void startFresh(){
		super.startFresh();
		
		if(numGenerations > 0){
			numXov = new int [numGenerations];
			numSharedXov = new int [numGenerations];
		}
	}
	
	@Override
	public int evolve() {
	    if (generation > 0)
	        output.message("Generation " + generation);

	    // EVALUATION
	    statistics.preEvaluationStatistics(this);
	    evaluator.evaluatePopulation(this);
	    
	    for(int sub = 0;sub<this.population.subpops.length;sub++){
	    	((MPMO_Breeder)breeder).sortNupdateRankNFactor(this, sub);
	    }
	    
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

	    // Generate new instances if needed
		RuleOptimizationProblem problem = (RuleOptimizationProblem)evaluator.p_problem;
	    if (problem.getEvaluationModel().isRotatable()) {
			problem.rotateEvaluationModel();
		}

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
