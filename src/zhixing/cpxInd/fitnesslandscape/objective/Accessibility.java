package zhixing.cpxInd.fitnesslandscape.objective;

import java.util.ArrayList;

import ec.EvolutionState;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
import zhixing.cpxInd.individual.CpxGPIndividual;

public class Accessibility extends Evolvability{

	@Override
	public double specific_eval(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
//		//get samples
//		getSamples(state, thread, landscape);
//		getNeighbors(state, thread, landscape);
//		
//		//for each sample
//		//1. get the neighbors with non-neutral unique fitness		
//		//2. for each neighbor, get its fji
//		double access = 0;
//		for(int i = 0; i<samples.size(); i++) {
//			CpxGPIndividual trial = samples.get(i);
//			double tarFitness = trial.fitness.fitness();
//			
//			
//			
//			double sizeNonneutralFit = Math.max(1, neighbors.get(i).size() - getNeighborsWithFitness(i, tarFitness).size());
//			double tmpevol = 0;
//			
//			ArrayList<Double> NonneutralFitness = getNonNeutralFitness(i, tarFitness);
//			
//			for(Double fit : NonneutralFitness) {
//				double val = getNeighborsWithFitness(i, fit).size();
//				
//				double fij = val / sizeNonneutralFit;
//				
//				tmpevol += fij * fij;
//			}
//			
//			evol += 1 - tmpevol;
//		}
//		
//		evol /= samples.size();
//		
//		return evol;
		
		return 0;
	}

	
//	protected double getFij(int i, double nonNeutralFit_j) {
//		CpxGPIndividual trial = samples.get(i);
//		double tarFitness = trial.fitness.fitness();
//		double sizeNonneutralFit = Math.max(1, neighbors.get(i).size() - getNeighborsWithFitness(i, tarFitness).size());
//		
//		double val = getNeighborsWithFitness(i, nonNeutralFit_j).size();
//		
//		double fij = val / sizeNonneutralFit;
//		
//		return fij;
//	}
}
