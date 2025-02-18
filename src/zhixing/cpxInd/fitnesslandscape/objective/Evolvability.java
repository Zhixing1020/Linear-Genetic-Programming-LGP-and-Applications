package zhixing.cpxInd.fitnesslandscape.objective;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Fitness;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
import zhixing.cpxInd.individual.CpxGPIndividual;

public class Evolvability extends FLMetrics{

	@Override
	public double specific_eval(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
		
		//get samples
		getSamples(state, thread, landscape);
		getNeighbors(state, thread, landscape);
		
		//for each sample
		//1. get all non-neutral unique fitness		
		//2. get the evolvability of sample i
		double evol = 0;
		for(int i = 0; i<samples.size(); i++) {
			CpxGPIndividual trial = samples.get(i);
			Fitness tarFitness = trial.fitness;
			double sizeNonneutralFit = Math.max(1, neighbors.get(i).size() - getNeighborsWithFitness(i, tarFitness).size());
			double tmpevol = 0;
			
			ArrayList<Fitness> NonneutralFitness = getFitterFitness(i, tarFitness);
			
			for(Fitness fit : NonneutralFitness) {
				double val = getNeighborsWithFitness(i, fit).size();
				
				double fij = val / sizeNonneutralFit;
				
				tmpevol += fij * fij;
			}
			
			if(! NonneutralFitness.isEmpty()) {
				evol += 1 - tmpevol;
			}
				
		}
		
		evol /= samples.size();
		
		return evol;
	}

	protected ArrayList<Fitness> getFitterFitness(int index, Fitness tarFitness){
		ArrayList<Fitness> result = new ArrayList<>();
		ArrayList<Double> usedFitness = new ArrayList<>();
		
		for(CpxGPIndividual trial : neighbors.get(index)) {
			if((!usedFitness.contains(trial.fitness.fitness())) && trial.fitness.betterThan(tarFitness)) {
				result.add(trial.fitness);
				usedFitness.add(trial.fitness.fitness());
			}
		}
		
		return result;
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
