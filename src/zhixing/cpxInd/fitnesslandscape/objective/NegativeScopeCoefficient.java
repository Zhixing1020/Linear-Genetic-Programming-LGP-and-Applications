package zhixing.cpxInd.fitnesslandscape.objective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ec.EvolutionState;
import ec.Fitness;
import ec.util.Parameter;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
import zhixing.cpxInd.individual.CpxGPIndividual;

public class NegativeScopeCoefficient extends FLMetrics{

	public static final String NSC = "negScoCoef";
	public static final String P_NUMBINS = "numbins";
	
	int numBins;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = new Parameter(NSC);
		
		numBins = state.parameters.getIntWithDefault(base.push(P_NUMBINS), base.push(P_NUMBINS), 10);
		if(numBins < 2 ) {
			System.err.print("the number of bins must be at least 2.\n");
			System.exit(1);
		}
	}
	
	@Override
	public void getSamples(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
		getDefaultSamples(state, thread, landscape);
		
		samples.clear();
		for(CpxGPIndividual indi : defaultSamples) {
			samples.add(indi);
		}
		
		Collections.sort(samples, new FitnessComparotor());
	}

	@Override
	public void getNeighbors(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
		getDefaultNeighbors(state, thread, landscape);
		
		neighbors.clear();
		for(ArrayList<CpxGPIndividual> item : defaultNeighbors) {
			neighbors.add(item);
		}
	}

	@Override
	public double specific_eval(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
		
		//get samples
		getSamples(state, thread, landscape);
		getNeighbors(state, thread, landscape);
		
		//get the maximum and minimum fitness in samples
//		double maxfit = -1e7, minfit = 1e7;
//		for(CpxGPIndividual indi : samples) {
//			if(indi.fitness.fitness() > maxfit) {
//				maxfit = indi.fitness.fitness() + 1e-5;
//			}
//			if(indi.fitness.fitness() < minfit) {
//				minfit = indi.fitness.fitness();
//			}
//		}
		
//		double binStep = (maxfit - minfit) / numBins;
		
		int sizeStep = (int) Math.ceil(samples.size() / (double)numBins);
		
		double [] M = new double [numBins];
		double [] N = new double [numBins];
		int [] Mcnt = new int[numBins]; //count the number of abscissas
		int [] Ncnt = new int[numBins]; //count the number of ordinates
		
		
		for(int i = 0; i<samples.size(); i++) { //for each sample, put it into the right bin
			CpxGPIndividual indi = samples.get(i);
			int bini = 0;
			for(; bini < numBins; bini++) {
//				if(indi.fitness.fitness() >= 0 + bini*binStep && indi.fitness.fitness() < minfit + (bini+1)*binStep) 
				if(i >= bini*sizeStep && i < (bini+1)*sizeStep) 
				{
					break;
				}
			}
			
			M[bini] += indi.fitness.fitness();
			Mcnt[bini] ++;
			
			//get the ordinates
			ArrayList<CpxGPIndividual> neighbor = neighbors.get(i);
			for(CpxGPIndividual nb : neighbor) {
//				int neighborBini = 0;
//				for(; neighborBini < numBins; neighborBini ++) {
//					if(nb.fitness.fitness() >= minfit + neighborBini*binStep && nb.fitness.fitness() < minfit + (neighborBini+1)*binStep) {
//						break;
//					}
//				}
				
//				N[neighborBini] += nb.fitness.fitness();
//				Ncnt[neighborBini] ++;
				
				N[bini] += nb.fitness.fitness();
				Ncnt[bini] ++;
			}
			
		}
		
		//get the mean of M and N
		ArrayList<Double> Mcal = new ArrayList<>();
		ArrayList<Double> Ncal = new ArrayList<>();
		for(int i = 0; i<numBins; i++) {
			if(Mcnt[i] > 0 && Ncnt[i] > 0) {
				M[i] /= Mcnt[i];
				Mcal.add(M[i]);
				
				N[i] /= Ncnt[i];
				Ncal.add(N[i]);
			}
		}
		
		double nsc = 0;
//		for(int i = 0; i<numBins - 1; i++) {
//			if(M[i+1] - M[i] != 0 && Mcnt[i+1] != 0 && Mcnt[i] != 0)
//				nsc += Math.min(0, (N[i+1] - N[i])/(M[i+1] - M[i]));
//		}
		for(int i = 0; i<Mcal.size()-1; i++) {
			if(Mcal.get(i + 1) - Mcal.get(i) != 0) {
				nsc += Math.min(0, (Ncal.get(i+1) - Ncal.get(i)) / (Mcal.get(i+1) - Mcal.get(i)));
			}
		}
		
		return nsc;
	}

	class FitnessComparotor implements Comparator<CpxGPIndividual>{

		@Override
		public int compare(CpxGPIndividual o1, CpxGPIndividual o2) {
			return (int) Math.signum(o1.fitness.fitness() - o2.fitness.fitness());
		}
		
	}
}
