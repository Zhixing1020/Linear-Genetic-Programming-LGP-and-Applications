package zhixing.cpxInd.algorithm.multitask.MFEA.breeder;

import java.util.PriorityQueue;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.simple.SimpleBreeder;
import zhixing.cpxInd.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator;
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA;

public class MFEA_Breeder extends SimpleBreeder{

	public Population concatenate(EvolutionState state, Population trial) {
		Population newpop = (Population) state.population.emptyClone();
		int numTasks = ((MFEA_Evaluator)state.evaluator).getNumTasks();

		int scalarFitIndex [] = new int [numTasks];
		for(int i = 0;i<numTasks;i++){
			scalarFitIndex[i] = 0;
		}
		
		//build several priority queue to store the individuals from different tasks.
		PriorityQueue<Individual> queue [] = new PriorityQueue [numTasks];
		
		for(int t = 0;t<numTasks;t++){
			queue[t] = new PriorityQueue<>(10, ((MFEA_Evaluator)state.evaluator).new cmp());
		}
		
		for(int s = 0; s<state.population.subpops.length; s++){
			if(numTasks > state.population.subpops[s].individuals.length){
				System.out.println("The population size is much smaller than the number of tasks");
				System.exit(1);
			}
			//load the old population
			for(int i = 0; i<state.population.subpops[s].individuals.length; i++){
				LGPIndividual_MFEA ind = (LGPIndividual_MFEA)state.population.subpops[s].individuals[i];
				queue[ind.skillFactor].add(ind);
			}
			
			//load the trial population
			for(int i = 0; i<trial.subpops[s].individuals.length; i++){
				LGPIndividual_MFEA ind = (LGPIndividual_MFEA)trial.subpops[s].individuals[i];
				queue[ind.skillFactor].add(ind);
			}
			
//			for(int t = 0;t<numTasks;t++){
//				System.out.print("" + queue[t].size()+"\t");
//			}
//			System.out.println();
			
			//put into the newpop
			for(int i = 0; i<state.population.subpops[s].individuals.length; i++){
				if(queue[i%numTasks].size() > 0){
					newpop.subpops[s].individuals[i] = queue[i % numTasks].poll();
					((LGPIndividual_MFEA)newpop.subpops[s].individuals[i]).scalarRank = scalarFitIndex[i % numTasks];
					scalarFitIndex[i % numTasks]++;
				}
				else{
					int j = i + 1;
					while(queue[j%numTasks].size() == 0){
						j++;
					}
					newpop.subpops[s].individuals[i] = queue[j % numTasks].poll();
					((LGPIndividual_MFEA)newpop.subpops[s].individuals[i]).scalarRank = scalarFitIndex[j % numTasks];
					scalarFitIndex[j % numTasks]++;
				}
			}
			
			for(int t = 0;t<numTasks;t++){
				queue[t].clear();
			}
			
		}

		return newpop;
	}
	
	public void sortNupdateScalarRank(EvolutionState state){
		Population newpop = (Population) state.population.emptyClone();
		int numTasks = ((MFEA_Evaluator)state.evaluator).getNumTasks();

		int scalarFitIndex [] = new int [numTasks];
		for(int i = 0;i<numTasks;i++){
			scalarFitIndex[i] = 0;
		}
		
		//build several priority queue to store the individuals from different tasks.
		PriorityQueue<Individual> queue [] = new PriorityQueue [numTasks];
		
		for(int t = 0;t<numTasks;t++){
			queue[t] = new PriorityQueue<>(10, ((MFEA_Evaluator)state.evaluator).new cmp());
		}
		
		for(int s = 0; s<state.population.subpops.length; s++){
			if(numTasks > state.population.subpops[s].individuals.length){
				System.out.println("The population size is much smaller than the number of tasks");
				System.exit(1);
			}
			//load the old population
			for(int i = 0; i<state.population.subpops[s].individuals.length; i++){
				LGPIndividual_MFEA ind = (LGPIndividual_MFEA)state.population.subpops[s].individuals[i];
				queue[ind.skillFactor].add(ind);
			}
//			for(int t = 0;t<numTasks;t++){
//				System.out.print("" + queue[t].size()+"\t");
//			}
//			System.out.println();
			
			
			//put into the newpop
			for(int i = 0; i<state.population.subpops[s].individuals.length; i++){
				if(queue[i%numTasks].size() > 0){
					newpop.subpops[s].individuals[i] = queue[i % numTasks].poll();
					((LGPIndividual_MFEA)newpop.subpops[s].individuals[i]).scalarRank = scalarFitIndex[i % numTasks];
					scalarFitIndex[i % numTasks]++;
				}
				else{
					int j = i + 1;
					while(queue[j%numTasks].size() == 0){
						j++;
					}
					newpop.subpops[s].individuals[i] = queue[j % numTasks].poll();
					((LGPIndividual_MFEA)newpop.subpops[s].individuals[i]).scalarRank = scalarFitIndex[j % numTasks];
					scalarFitIndex[j % numTasks]++;
				}
			}
			
			for(int t = 0;t<numTasks;t++){
				queue[t].clear();
			}
			
		}
		
		state.population = newpop;
	}
}
