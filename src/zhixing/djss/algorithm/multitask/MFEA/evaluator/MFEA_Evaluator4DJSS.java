package zhixing.djss.algorithm.multitask.MFEA.evaluator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Population;
import ec.simple.SimpleEvaluator;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import ec.util.ThreadPool;
import zhixing.cpxInd.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator;
import zhixing.djss.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA4DJSS;
import zhixing.djss.algorithm.multitask.MFEA.individualoptimization.MFEA_IndividualOptimizationProblem;

public class MFEA_Evaluator4DJSS extends MFEA_Evaluator {
	
	@Override
	public void identifySkillFactor(EvolutionState state){
		
		MFEA_IndividualOptimizationProblem prob = (MFEA_IndividualOptimizationProblem)p_problem;
		
		int rankIndex[][] = new int [num_task][state.population.subpops[0].individuals.length];
		
		double tasksFitness[][] = new double [num_task][state.population.subpops[0].individuals.length];
		
		for(int t = 0; t<num_task;t++){
			
			if(! (p_problem instanceof MFEA_IndividualOptimizationProblem)){
				System.out.println("the optimizationi problem is not supported");
				System.exit(1);
			}
			
			
			//for all individuals, evaluate a certain tasks
			for(int s = 0;s<state.population.subpops.length;s++){
				
				for(int i = 0;i<state.population.subpops[s].individuals.length;i++){
					prob.evaluateOneTask(state, state.population.subpops[s].individuals[i], t, state.evalthreads);
					tasksFitness[t][i]=state.population.subpops[s].individuals[i].fitness.fitness();
				}
				
				//rank the fitness
				for(int i = 0;i<state.population.subpops[s].individuals.length;i++){
					rankIndex[t][i]=0;
					for(int j = 0;j<state.population.subpops[s].individuals.length;j++){
						if(tasksFitness[t][i] > tasksFitness[t][j] 
								|| tasksFitness[t][i] == tasksFitness[t][j] && i>j){
							rankIndex[t][i]++;
						}
					}
				}
				
			}
			
			
		}
		
		//get the scalarFitness of all tasks
		for(int s = 0;s<state.population.subpops.length;s++){
			for(int i = 0;i<state.population.subpops[s].individuals.length;i++){
				int sr = 1000000;
				int sf = 0;
				for(int t = 0;t<num_task;t++){
					if(rankIndex[t][i]<sr || (rankIndex[t][i]==sr&&state.random[0].nextDouble()>0.5)){
						sr = rankIndex[t][i];
						sf = t;
					}
				}
				((LGPIndividual_MFEA4DJSS)state.population.subpops[s].individuals[i]).scalarRank = sr;
				((LGPIndividual_MFEA4DJSS)state.population.subpops[s].individuals[i]).skillFactor = sf;
			}
		}
		
		
		//re-evaluate the fitness based on the skillFactor
		for(int s = 0;s<state.population.subpops.length;s++){
			for(int i = 0;i<state.population.subpops[s].individuals.length;i++){
				prob.evaluate(state, 
						state.population.subpops[s].individuals[i], 
						s, 
						state.evalthreads);
			}
		}
		
	}
	
	@Override
	public void evaluatePopulation_MFEA(EvolutionState state, Population pop){
		for(int s = 0;s<pop.subpops.length;s++){
			for(int i = 0;i<pop.subpops[s].individuals.length;i++){
				((MFEA_IndividualOptimizationProblem)p_problem).evaluate(state, 
						pop.subpops[s].individuals[i], 
						s, 
						state.evalthreads);
			}
		}
	}
	
}


