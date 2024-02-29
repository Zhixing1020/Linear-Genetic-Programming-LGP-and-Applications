package zhixing.cpxInd.algorithm.Grammar.EvolutionState;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import ec.simple.SimpleEvolutionState;
import ec.util.Checkpoint;
import ec.util.Parameter;
//import yimei.jss.gp.GPRuleEvolutionState;
//import yimei.jss.gp.terminal.AttributeGPNode;
//import yimei.jss.gp.terminal.DoubleERC;
//import yimei.jss.gp.terminal.JobShopAttribute;
//import yimei.jss.ruleoptimisation.RuleOptimizationProblem;
import yimei.util.Timer;
import zhixing.cpxInd.algorithm.Grammar.individual.LGPIndividual4Grammar;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPFlowController;
import zhixing.cpxInd.individual.primitive.FlowOperator;

public interface EvolutionState4Grammar{
	
	List<Double> genTimes = new ArrayList<>();
	
	HashSet<String> BannedList4FlowInstr = new HashSet<>();
	
		
	void initTerminalSet();
	
	void initRelativeTerminalSet4Grammar();
	
//	@Override
//	public int evolve() {
//	    if (generation > 0)
//	        output.message("Generation " + generation);
//
//	    // EVALUATION
//	    statistics.preEvaluationStatistics(this);
//	    evaluator.evaluatePopulation(this);
//	    
//	    updateBannedList();
//	    
//	    statistics.postEvaluationStatistics(this);
//
//	    // SHOULD WE QUIT?
//	    if (evaluator.runComplete(this) && quitOnRunComplete)
//	        {
//	        output.message("Found Ideal Individual");
//	        return R_SUCCESS;
//	        }
//
//	    // SHOULD WE QUIT?
//	    if (generation == numGenerations-1)
//	        {
//	        return R_FAILURE;
//	        }
//
//	    // PRE-BREEDING EXCHANGING
//	    statistics.prePreBreedingExchangeStatistics(this);
//	    population = exchanger.preBreedingExchangePopulation(this);
//	    statistics.postPreBreedingExchangeStatistics(this);
//	    
//	    String exchangerWantsToShutdown = exchanger.runComplete(this);
//	    if (exchangerWantsToShutdown!=null)
//	        {
//	        output.message(exchangerWantsToShutdown);
//	        /*
//	         * Don't really know what to return here.  The only place I could
//	         * find where runComplete ever returns non-null is
//	         * IslandExchange.  However, that can return non-null whether or
//	         * not the ideal individual was found (for example, if there was
//	         * a communication error with the server).
//	         *
//	         * Since the original version of this code didn't care, and the
//	         * result was initialized to R_SUCCESS before the while loop, I'm
//	         * just going to return R_SUCCESS here.
//	         */
//
//	        return R_SUCCESS;
//	        }
//
//	    // BREEDING
//	    statistics.preBreedingStatistics(this);
//
//	    population = breeder.breedPopulation(this);
//
//	    // POST-BREEDING EXCHANGING
//	    statistics.postBreedingStatistics(this);
//
//	    // POST-BREEDING EXCHANGING
//	    statistics.prePostBreedingExchangeStatistics(this);
//	    population = exchanger.postBreedingExchangePopulation(this);
//	    statistics.postPostBreedingExchangeStatistics(this);
//
//
//	    // INCREMENT GENERATION AND CHECKPOINT
//	    generation++;
//	    if (checkpoint && generation%checkpointModulo == 0)
//	        {
//	        output.message("Checkpointing");
//	        statistics.preCheckpointStatistics(this);
//	        Checkpoint.setCheckpoint(this);
//	        statistics.postCheckpointStatistics(this);
//	        }
//
//	    return R_NOTDONE;
//	}
	
	default HashSet<String> getBannedList4FlowInstr(){
		//this function is used in Grammar-guided mutatioin, to check whether we construct the banned flow control structures again.
		return BannedList4FlowInstr;
	}
	
	default void updateBannedList(EvolutionState state) {
		//this function checks the contradictory and tautological flow control structures. If it finds the contradictory and tautological flow control structures,
		//it will record them in the BannedList.
		
		for(int s = 0; s<state.population.subpops.length; s++) {
			for(Individual ind : state.population.subpops[s].individuals) {
				if(ind instanceof LGPIndividual4Grammar) {
					LGPIndividual4Grammar indG = (LGPIndividual4Grammar) ind;
					
					double maxused = -1;
					for(int i = 0; i<indG.getTreesLength(); i++) {
						if(maxused < indG.getUsedList().get(i)) {
							maxused = indG.getUsedList().get(i);
						}
					}
					for(int i = 0; i<indG.getTreesLength(); i++) {
						indG.getUsedList().set(i, indG.getUsedList().get(i) / maxused);
					}
					
					for(int index = 0; index<indG.getTreesLength(); index++) {
						
						GPTreeStruct instr = indG.getTreeStruct(index);
						
						if(instr.child.children[0] instanceof FlowOperator && indG.getUsedList().get(index)>0) {
							int bodylength = LGPFlowController.getNestedBodyLength(index, indG.getTreeStructs());
							
							boolean bodyvalid = false;
							for(int b = 1;b<=bodylength && index+b<indG.getTreesLength();b++) {
								bodyvalid = bodyvalid 
										|| (indG.getUsedList().get(index + b)>0.0 && indG.getUsedList().get(index + b)<1);
							}
							//if effective but its body is not used or always used, add to the banned list
							if(! bodyvalid) {
								BannedList4FlowInstr.add(instr.toString());
							}
							//if effective but its body is used, remove from banned list
							else {
								BannedList4FlowInstr.remove(instr.toString());
							}
						}
					}
				}
			}
		}
	}
	
	// the best individual in subpopulation
	default Individual bestIndi(EvolutionState state, int subpop) {
		int best = 0;
		for(int x = 1; x < state.population.subpops[subpop].individuals.length; x++)
			if (state.population.subpops[subpop].individuals[x].fitness.betterThan(state.population.subpops[subpop].individuals[best].fitness))
				best = x;

		return state.population.subpops[subpop].individuals[best];
	}
	
//	@Override
//	public void run(int condition)
//    {
//		double totalTime = 0;
//
//		if (condition == C_STARTED_FRESH) {
//			startFresh();
//        }
//		else {
//			startFromCheckpoint();
//        }
//
//		int result = R_NOTDONE;
//		while ( result == R_NOTDONE )
//        {
//			long start = yimei.util.Timer.getCpuTime();
//			long startUser = yimei.util.Timer.getUserTime();
//			long startSys = yimei.util.Timer.getSystemTime();
//			result = evolve();
//
//			long finish = yimei.util.Timer.getCpuTime();
//			long finishUser = yimei.util.Timer.getUserTime();
//			long finishSys = yimei.util.Timer.getSystemTime();
//			double duration = (finish - start) / 1000000000;
//			double durationUser = (finishUser - startUser) / 1000000000;
//			double durationSys = (finishSys - startSys) / 1000000000;
//			genTimes.add(duration);
//			totalTime += duration;
//
//			output.message("Generation " + (generation-1) + " elapsed " + duration + " seconds, user: " + durationUser +", system: "+durationSys+".");
//        }
//
//		output.message("The whole program elapsed " + totalTime + " seconds.");
//
//
//		finish(result);
//
//    }
	
}
