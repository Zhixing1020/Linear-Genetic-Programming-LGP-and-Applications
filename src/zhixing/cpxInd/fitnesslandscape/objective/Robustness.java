package zhixing.cpxInd.fitnesslandscape.objective;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Fitness;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
import zhixing.cpxInd.individual.CpxGPIndividual;

public class Robustness extends FLMetrics{

	@Override
	public double specific_eval(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
		
		//get samples
		getSamples(state, thread, landscape);
		getNeighbors(state, thread, landscape);
		
		//for each sample, get its robustness
		double res = 0;
		for(int i = 0; i<samples.size(); i++) {
			CpxGPIndividual trial = samples.get(i);
			Fitness tarFitness = trial.fitness;
			
			if(! neighbors.get(i).isEmpty())
			res += getNeighborsWithFitness(i, tarFitness).size() / (double)neighbors.get(i).size();
		}
		
		res /= samples.size();
		
		return res;
	}
	
	protected ArrayList<CpxGPIndividual> getNeighborsWithFitness(int index, Fitness tarFitness ){
		//index: the index of samples (and its neighbors)    tarFitness: the particular fitness value
		
		ArrayList<CpxGPIndividual> result = new ArrayList<>();
		
		for(CpxGPIndividual trial : neighbors.get(index)) {
			if(trial.fitness.equivalentTo(tarFitness)) {
				result.add(trial);
			}
		}
		
		return result;
	}

	@Override
	public void getSamples(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
		getDefaultSamples(state, thread, landscape);
		
		samples.clear();
		for(CpxGPIndividual indi : defaultSamples) {
			samples.add(indi);
		}
	}

	@Override
	public void getNeighbors(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
		getDefaultNeighbors(state, thread, landscape);
		
		neighbors.clear();
		for(ArrayList<CpxGPIndividual> item : defaultNeighbors) {
			neighbors.add(item);
		}
	}
	
	
}
