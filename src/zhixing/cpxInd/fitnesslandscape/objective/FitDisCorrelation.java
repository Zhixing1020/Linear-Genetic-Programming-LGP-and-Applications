package zhixing.cpxInd.fitnesslandscape.objective;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
import zhixing.cpxInd.individual.CpxGPIndividual;

public class FitDisCorrelation extends FLMetrics {

	final static public String FDC = "FDC";
	final static public String P_OPTIMIZATION = "optimization";
	final static public int MAX = 1;
	final static public int MIN = 0;
	
	ArrayList<Double> fitnesses = new ArrayList<>();
	ArrayList<Double> distances = new ArrayList<>();
	
	int optimization = MIN;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = new Parameter(FDC);
		
		optimization = state.parameters.getIntWithDefault(base.push(P_OPTIMIZATION), def.push(P_OPTIMIZATION), MIN);
		if(optimization != MAX && optimization != MIN) {
			System.err.print("we got an unknown optimization direction\n");
			System.exit(1);
		}
		
	}
	
	@Override
	public double specific_eval(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
		double res = 0;
		
		//1. get the optimal fitness and coordinate
		ArrayList<CpxGPIndividual> optima = new ArrayList<>();
		double optimal_fit = 1e7;
		for(CpxGPIndividual indi : landscape.getMap() ) {
			if(optimization == MIN && indi.fitness.fitness() < optimal_fit) {
				optimal_fit = indi.fitness.fitness();
			}
			if(optimization == MAX && indi.fitness.fitness() > optimal_fit) {
				optimal_fit = indi.fitness.fitness();
			}
		}
		for(CpxGPIndividual indi : landscape.getMap()) {
			if(indi.fitness.fitness() == optimal_fit) {
				optima.add(indi);
			}
		}
		
		while(optima.size() > 10) {
			optima.remove(state.random[thread].nextInt(optima.size()));
		}
		
		//2. get samples
		getSamples(state, thread, landscape);
//		getNeighbors(state, thread, landscape);
		
		//3. calculate FDC
		setFitnessNDistance(samples, null, optima, optimal_fit, landscape);
		
		double meanfit = getMeanFit();
		double meandis = getMeanDis();
		double stdfit = getStdFit();
		double stddis = getStdDis();
		
		double Cfd = 0;
		for(int i = 0; i<fitnesses.size(); i++) {
			Cfd += (fitnesses.get(i) - meanfit) * (distances.get(i) - meandis);
		}
		res = Cfd / (fitnesses.size()*stdfit*stddis);
		
		return res;
	}
	
	protected void setFitnessNDistance(ArrayList<CpxGPIndividual> samples, 
			ArrayList<ArrayList<CpxGPIndividual>> neighbors, 
			ArrayList<CpxGPIndividual> optima,
			double optimalFitness,
			LGPFitnessLandscape landscape) {
		
		fitnesses.clear();
		distances.clear();
		
		for(CpxGPIndividual indi : samples) {
			fitnesses.add( Math.abs(indi.fitness.fitness() - optimalFitness) );
			
//			GenoVector g0 = indexlist.getGenoVector(indi);
			double mindis = 1e7;
			
			for(CpxGPIndividual target : optima) {
//				GenoVector g1 = indexlist.getGenoVector(target);
//				
//				double tmpdis = Math.sqrt(norm2Q.Q(g1, g0));
				
				double tmpdis = neighborhood.distance(null, 0, target, indi, landscape);
				
				if(tmpdis < mindis) {
					mindis = tmpdis;
				}
			}
			
			distances.add(mindis);
		}
	}

	protected double getMeanFit() {
		double sum = 0;
		for(Double d : fitnesses) {
			sum += d;
		}
		
		return sum / fitnesses.size();
	}
	
	protected double getStdFit() {
		double avg = getMeanFit();
		double sum = 0;
		for(Double d : fitnesses) {
			sum += (d-avg)*(d-avg);
		}
		
		return Math.sqrt(sum / fitnesses.size());
	}
	
	protected double getMeanDis() {
		double sum = 0;
		for(Double d : distances) {
			sum += d;
		}
		
		return sum / distances.size();
	}
	
	protected double getStdDis() {
		double avg = getMeanDis();
		double sum = 0;
		for(Double d : distances) {
			sum += (d - avg) * (d - avg);
		}
		
		if(sum == 0) return 1;
		
		return Math.sqrt(sum / distances.size());
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
