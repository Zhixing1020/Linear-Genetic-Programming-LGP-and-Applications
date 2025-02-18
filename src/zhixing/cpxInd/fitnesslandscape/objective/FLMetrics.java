package zhixing.cpxInd.fitnesslandscape.objective;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.util.Parameter;
import ec.gp.GPProblem;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
import zhixing.cpxInd.fitnesslandscape.neighbor.NeighborStruct;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.LGPIndividual;

public abstract class FLMetrics {
	final static public String FLMETRIC = "fitlandscapemetric";
	
	final static public String P_MINNUMSAMPLE = "numMinSample";
	final static public String P_PERCENTSAMPLE = "percentSample";
	final static public String P_MINEPSILON = "minepsilon";
	final static public String P_PERCENTEPSILON = "minPercentEpsilon";
	final static public String P_NEIGHBORHOOD = "neighborhood";
	
	public int numMinSample; //define the minimum number of sampling points in the search space
	
	public double percentSample; //define the minimum sampling percentage from the search space
	
	public double minEpsilon; //define the minimum distance of neighborhood thresold
	
	public double minPercentEpsilon;//define the minimum distance of neighborhood thresold by percentage
	
	protected ArrayList<CpxGPIndividual> samples = new ArrayList<>();
	protected ArrayList<ArrayList<CpxGPIndividual>> neighbors = new ArrayList<>();
	
	static protected ArrayList<CpxGPIndividual> defaultSamples = new ArrayList<>();
	static protected ArrayList<ArrayList<CpxGPIndividual>> defaultNeighbors = new ArrayList<>();
	static int generation_default_sample = -1;
	static int generation_default_neighbor = -1;
	
	protected NeighborStruct neighborhood;
	
	protected double metricValue;
	
	public double getMetricValue() {
		return metricValue;
	}
	
	abstract public void getSamples(EvolutionState state, int thread, LGPFitnessLandscape landscape);
	abstract public void getNeighbors(EvolutionState state, int thread, LGPFitnessLandscape landscape);
	
	public void getDefaultSamples(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
		//this is a default sampling method
		if(state.generation == generation_default_sample) return;
		
		int numsample = (int) Math.max(numMinSample, percentSample*landscape.getCoordinates().size());
		
		defaultSamples.clear();
		
		int mapsize = landscape.getMap().size();
		
		if(mapsize < 300) {
			for(CpxGPIndividual indi : landscape.getMap()) {
				
				if(!indi.evaluated) {
					((GPProblem)state.evaluator.p_problem).evaluate(state, indi, 0, 0);
				}
				
				defaultSamples.add(indi);
			}
		}
		else {
			boolean [] used = new boolean[mapsize];
			
			for(int s = 0; s<numsample; s++) {
				int tryindex = state.random[thread].nextInt(mapsize);
				CpxGPIndividual indi = landscape.getMap().get( tryindex );
				if(s < state.population.subpops[0].individuals.length) {
					indi = (CpxGPIndividual) state.population.subpops[0].individuals[s];
				}
				else {
//					int tryindex = state.random[thread].nextInt(mapsize);
					for(int stry = 0; stry < 10 && used[tryindex]; stry++) {
						tryindex = state.random[thread].nextInt(mapsize);
					}
					
					indi = landscape.getMap().get( tryindex );
//					
//					defaultSamples.add(indi);
					
					used[tryindex] = true;
				}
				
				if(!indi.evaluated) {
					((GPProblem)state.evaluator.p_problem).evaluate(state, indi, 0, 0);
				}
				
				defaultSamples.add(indi);
			}
		}
		
		
		generation_default_sample = state.generation;
	}
	
	public void getDefaultNeighbors(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
		//this is a default neighboring strcuture of the landscape
		if(state.generation == generation_default_neighbor) return;
		
		if(samples.isEmpty()) {
			System.err.print("please sample individuals from the search space before identifying their neighbors");
			return;
		}
		
		int epsilon = (int) Math.max(minEpsilon, minPercentEpsilon*landscape.indexlist.size());
		
		defaultNeighbors.clear();

		//for each sample,  get its neighbors
		for(CpxGPIndividual sample : defaultSamples) {
			
//			GenoVector g0 = landscape.indexlist.getGenoVector(sample);
//			
//			ArrayList<CpxGPIndividual> tmp = new ArrayList<>();
//			
//			int numAllindividuals = landscape.getMap().size();
//			
//			if(numAllindividuals < 300) {
//				for(CpxGPIndividual tryindi : landscape.getMap()) {
//					GenoVector g1 = landscape.indexlist.getGenoVector(tryindi);
//					
//					if(Math.sqrt(norm2Q.Q(g0, g1)) <= epsilon) {
//						tmp.add(tryindi);
//					}
//				}
//			}
//			else
//				for(int cn = 0; cn < default_percentNeighborSampling*numAllindividuals; cn++)
//				{
//					CpxGPIndividual tryindi = landscape.getMap().get(state.random[thread].nextInt(numAllindividuals));
//					GenoVector g1 = landscape.indexlist.getGenoVector(tryindi);
//					
//					if(Math.sqrt(norm2Q.Q(g0, g1)) <= epsilon) {
//						tmp.add(tryindi);
//					}
//				}
			
			ArrayList<CpxGPIndividual> tmp = neighborhood.getNeighbors(state, thread, sample, landscape, epsilon);
			
			defaultNeighbors.add(tmp);
		}
		
		generation_default_neighbor = state.generation;
	}
	
//	public ArrayList<CpxGPIndividual> getNeighbors4Sample(EvolutionState state, int thread, LGPFitnessLandscape landscape, CpxGPIndividual sample, double epsilon){
//		
//		ArrayList<CpxGPIndividual> tmp = new ArrayList<>();
//		GenoVector g0 = landscape.indexlist.getGenoVector(sample);
//		IndexList indexlist = landscape.indexlist;
//		
//		//get the number of none-zero elements
//		int nonzerosize = 0;
//		for(int g : g0.G) {
//			if(g == GenoVector.None) {
//				break;
//			}
//			nonzerosize++;
//		}
//				
//		for(int tryi = 0; tryi<10; tryi++) {
//			
//		}
//		//randomly pick one element, move epsilon to get a new one, insert or replace the old element by the new one
//		GenoVector g1 = landscape.indexlist.getGenoVector(sample);
//
//		//pick an element
//		int index = state.random[thread].nextInt(nonzerosize);
//		
//		//move epsilon
//		int newgeno = (int) (g1.G[index] + Math.pow(-1, state.random[thread].nextInt(2))*state.random[thread].nextDouble()*epsilon);
//		if(newgeno < 0) newgeno = 0;
//		if(newgeno >= indexlist.size()) newgeno = indexlist.size() - 1;
//		
//		//insert or replace
//		if(g1.length == nonzerosize || state.random[thread].nextDouble() < 0.5) { //replace
//			g1.G[index] = newgeno;
//		}
//		else { //insert
//			for(int i = 0, j = 0; i<nonzerosize; i++) {
//				//i: to-be-written index,  j: to-be-read index
//				if(i == index) {
//					g1.G[index] = newgeno;
//				}
//				else {
//					g1.G[i] = g0.G[j];
//					j++;
//				}
//			}
//		}
//		
////		tmp.add(tryindi);
//		
//		
//		return tmp;
//	}
	
	
	abstract public double specific_eval(EvolutionState state, int thread, LGPFitnessLandscape landscape) ;
	
	public double evaulate(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
		metricValue = specific_eval(state, thread, landscape);
		return metricValue;
	}
	
	public void setup(final EvolutionState state, final Parameter base) {
		Parameter def = new Parameter(FLMETRIC);
		
		if(state.parameters.exists(base.push(P_MINNUMSAMPLE), def.push(P_MINNUMSAMPLE)) 
				|| state.parameters.exists(base.push(P_PERCENTSAMPLE), def.push(P_PERCENTSAMPLE))) {
			
			numMinSample = state.parameters.getIntWithDefault(base.push(P_MINNUMSAMPLE), def.push(P_MINNUMSAMPLE), -1);
			percentSample = state.parameters.getDoubleWithDefault(base.push(P_PERCENTSAMPLE), def.push(P_PERCENTSAMPLE), -1);
			
			if(numMinSample > 0) { //we got a setting for numMinSample
				if(numMinSample < 1) {
					System.err.print("the minimum sampling number in the search space must be at least 1.\n");
					System.exit(1);
				}
			}
			if(percentSample > 0) {//we got a setting for percentSample
				if(percentSample <= 0 || percentSample > 1) {
					System.err.print("the minimum sampling percentage in the search space must be in range (0.0, 1.0].\n");
					System.exit(1);
				}
			}
			if(numMinSample < 0 && percentSample < 0) {
				System.err.print("we got neight sampling number nor percentage.\n");
				System.exit(1);
			}
			if(numMinSample < 0) numMinSample = 1;
		}
		
		
		if(state.parameters.exists(base.push(P_MINEPSILON), def.push(P_MINEPSILON))
				|| state.parameters.exists(base.push(P_PERCENTEPSILON), def.push(P_PERCENTEPSILON))) {
			
			minEpsilon = state.parameters.getDoubleWithDefault(base.push(P_MINEPSILON), def.push(P_MINEPSILON), -1);
			minPercentEpsilon = state.parameters.getDoubleWithDefault(base.push(P_PERCENTEPSILON), def.push(P_PERCENTEPSILON), -1);
			
			if(minEpsilon > 0) {
				if(minEpsilon < 1) {
					System.err.print("the minimum epsilon of neighbors must be at least 1.\n");
					System.exit(1);
				}
			}
			if(minPercentEpsilon > 0) {
				if(minPercentEpsilon <= 0 || minPercentEpsilon > 1) {
					System.err.print("the minimum epsilon percentage for the neighborhood must be in range (0.0, 1.0].\n");
					System.exit(1);
				}
			}
			if(minEpsilon < 0 && minPercentEpsilon < 0) {
				System.err.print("we got neight epsilon number nor percentage.\n");
				System.exit(1);
			}
			if(minEpsilon < 0) minEpsilon = 1;
		}
		
		neighborhood = (NeighborStruct) state.parameters.getInstanceForParameter(base.push(P_NEIGHBORHOOD), def.push(P_NEIGHBORHOOD), NeighborStruct.class);
		neighborhood.setup(state, base);
		
	}
	
}
