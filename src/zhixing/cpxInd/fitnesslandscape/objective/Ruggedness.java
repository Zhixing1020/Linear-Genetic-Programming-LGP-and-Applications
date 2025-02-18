package zhixing.cpxInd.fitnesslandscape.objective;

import java.util.ArrayList;

import ec.EvolutionState;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
import zhixing.cpxInd.individual.CpxGPIndividual;

public class Ruggedness extends FLMetrics{
	
	protected static int generation_sample_randomwalk = -1;

	protected static ArrayList<CpxGPIndividual> samples_randomwalk = new ArrayList<>();
	
	@Override
	public void getSamples(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
		
		if(state.generation == generation_sample_randomwalk) return;
		
		samples_randomwalk.clear();
		//random walk 
		int numsample = (int) Math.max(numMinSample, percentSample*landscape.getCoordinates().size());
		int epsilon = (int) Math.max(minEpsilon, minPercentEpsilon*landscape.indexlist.size());
		
		int mapsize = landscape.getMap().size();
		
		if(mapsize < 300) {
			numsample = mapsize + 1;
		}
		
		int tryindex = state.random[thread].nextInt(mapsize);
		CpxGPIndividual indi = landscape.getMap().get( tryindex );
		samples_randomwalk.add(indi);
		
		for(int s = 0; s<numsample; s++) {
//			GenoVector g0 = landscape.indexlist.getGenoVector(samples.get(samples.size() - 1));
//			
//			for(int cn = 0; cn < mapsize; cn++)
//			{
//				CpxGPIndividual tryindi = landscape.getMap().get(state.random[thread].nextInt(mapsize));
//				GenoVector g1 = landscape.indexlist.getGenoVector(tryindi);
//				
//				if(Math.sqrt(norm2Q.Q(g0, g1)) <= epsilon) {
//					samples.add(tryindi);
//					break;
//				}
//			}
			
			CpxGPIndividual sample = samples_randomwalk.get(samples_randomwalk.size() - 1);
			
			CpxGPIndividual tryindi =  neighborhood.getaNeighbor(state, thread, sample, landscape, epsilon);
			
//			CpxGPIndividual tryindi = tmp.get(state.random[thread].nextInt(tmp.size()));
			
			samples_randomwalk.add(tryindi);
			
//			for(int cn = 0; cn < mapsize; cn++) {
//				CpxGPIndividual tryindi = landscape.getMap().get(state.random[thread].nextInt(mapsize));
//				if(neighborhood.isNeighbor(state, thread, sample, tryindi, landscape, epsilon)) {
//					samples_randomwalk.add(tryindi);
//					break;
//				}
//			}
		}

		generation_sample_randomwalk = state.generation;
	}

	@Override
	public void getNeighbors(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double specific_eval(EvolutionState state, int thread, LGPFitnessLandscape landscape) {
		
		getSamples(state, thread, landscape);
		
		double diff = 0;
		
		//get the S
		ArrayList<Integer> S = new ArrayList<>();
		for(int i = 0; i<samples_randomwalk.size() - 1; i++) {
			int si = 0;
			if(samples_randomwalk.get(i + 1).fitness.fitness() - samples_randomwalk.get(i).fitness.fitness() < -diff) {
				si = -1;
			}
			else if(Math.abs(samples_randomwalk.get(i + 1).fitness.fitness() - samples_randomwalk.get(i).fitness.fitness()) <= diff) {
				si = 0;
			}
			else if(samples_randomwalk.get(i + 1).fitness.fitness() - samples_randomwalk.get(i).fitness.fitness() > diff) {
				si = 1;
			}
			S.add(si);
		}
		
		//get pq frequency
		double [][] pqFrequency = new double [3][3];
		for(int i = 0; i<S.size() -1; i++) {
			int x,y;
			
			if(S.get(i)==-1) x = 0;
			else if(S.get(i)==0) x = 1;
			else x =2;
			
			if(S.get(i+1)==-1) y = 0;
			else if(S.get(i+1)==0) y = 1;
			else y =2;
			
			pqFrequency[x][y] ++;
		}
		for(int x = 0; x<3; x++) {
			for(int y = 0; y<3; y++) {
				pqFrequency[x][y] /= (S.size() - 1);
			}
		}
		
		//get the entropy of ruggedness
		double H = 0;
		for(int x = 0; x<3; x++) {
			for(int y = 0; y<3; y++) {
				if(x == y) continue;
				
				if(pqFrequency[x][y] != 0)
					H += -1*pqFrequency[x][y]*(Math.log(pqFrequency[x][y]) / Math.log(6));
			}
		}
		
		return H;
	}

}
