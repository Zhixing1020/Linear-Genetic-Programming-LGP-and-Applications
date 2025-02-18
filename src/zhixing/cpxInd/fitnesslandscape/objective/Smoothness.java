package zhixing.cpxInd.fitnesslandscape.objective;

import java.util.ArrayList;

import ec.EvolutionState;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;

public class Smoothness extends Ruggedness{
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
		double h = 0;
		for(int x = 0; x<3; x++) {
			if(pqFrequency[x][x] != 0)
				h += -1*pqFrequency[x][x]*(Math.log(pqFrequency[x][x]) / Math.log(3));
		}
		
		return h;
	}
}
