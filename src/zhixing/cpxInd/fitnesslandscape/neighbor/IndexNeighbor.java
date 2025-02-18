package zhixing.cpxInd.fitnesslandscape.neighbor;

import java.util.ArrayList;

import ec.EvolutionState;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.norm2Q;
import zhixing.cpxInd.individual.CpxGPIndividual;

public class IndexNeighbor extends NeighborStruct{

	@Override
	public ArrayList<CpxGPIndividual> getNeighbors(EvolutionState state, int thread, CpxGPIndividual sample, LGPFitnessLandscape landscape, double epsilon) {
		if(sample == null) {
			System.err.print("please sample individuals from the search space before identifying their neighbors");
			return null;
		}
		
		//neighbors.clear();

		GenoVector g0 = landscape.indexlist.getGenoVector(sample);
		
		ArrayList<CpxGPIndividual> tmp = new ArrayList<>();
		
		int numAllindividuals = landscape.getMap().size();
		
		if(numAllindividuals < 300) {
			for(CpxGPIndividual tryindi : landscape.getMap()) {
				GenoVector g1 = landscape.indexlist.getGenoVector(tryindi);
				
				if(Math.sqrt(norm2Q.Q(g0, g1)) <= epsilon) {

//					if(!tryindi.evaluated) {
//						System.err.print("we must preprint the landscape before getting neighbors\n");
//						System.exit(1);
//					}
					
					//find the fitness of tryindi
					for(int i = 0; i<landscape.getMap().size(); i++) {
						CpxGPIndividual ind = landscape.getMap().get(i);
						if(ind.equals(tryindi)) {

							if(!ind.evaluated) {
								System.err.print("we must preprint the landscape before getting neighbors\n");
								System.exit(1);
							}
							
							tryindi.fitness = ind.fitness;
							tryindi.evaluated = true;
							break;
						}
					}
					
					tmp.add(tryindi);
				}
			}
		}
		else
			for(int cn = 0; cn < default_percentNeighborSampling*numAllindividuals; cn++)
			{
				CpxGPIndividual tryindi = landscape.getMap().get(state.random[thread].nextInt(numAllindividuals));
				GenoVector g1 = landscape.indexlist.getGenoVector(tryindi);
				
				if(Math.sqrt(norm2Q.Q(g0, g1)) <= epsilon) {

//					if(!tryindi.evaluated) {
//						System.err.print("we must preprint the landscape before getting neighbors\n");
//						System.exit(1);
//					}
					
					//find the fitness of tryindi
					for(int i = 0; i<landscape.getMap().size(); i++) {
						CpxGPIndividual ind = landscape.getMap().get(i);
						if(ind.equals(tryindi)) {

							if(!ind.evaluated) {
								System.err.print("we must preprint the landscape before getting neighbors\n");
								System.exit(1);
							}
							
							tryindi.fitness = ind.fitness;
							tryindi.evaluated = true;
							break;
						}
					}
					
					tmp.add(tryindi);
				}
			}
		
		return tmp;
	}

	@Override
	public boolean isNeighbor(EvolutionState state, int thread, CpxGPIndividual sample, CpxGPIndividual tryind, LGPFitnessLandscape landscape,
			double epsilon) {
		boolean res = false;
		
		GenoVector g0 = landscape.indexlist.getGenoVector(sample);
		GenoVector g1 = landscape.indexlist.getGenoVector(tryind);
		
		if(Math.sqrt(norm2Q.Q(g0, g1)) <= epsilon) {
			res = true;
		}
		
		return res;
	}

	@Override
	public double distance(EvolutionState state, int thread, CpxGPIndividual sample1, CpxGPIndividual sample2,
			LGPFitnessLandscape landscape) {
		double res = 0;
		
		GenoVector g0 = landscape.indexlist.getGenoVector(sample1);
		GenoVector g1 = landscape.indexlist.getGenoVector(sample2);
		
		return Math.sqrt(norm2Q.Q(g0, g1));
	}

	@Override
	public CpxGPIndividual getaNeighbor(EvolutionState state, int thread, CpxGPIndividual sample,
			LGPFitnessLandscape landscape, double epsilon) {
		if(sample == null) {
			System.err.print("please sample individuals from the search space before identifying their neighbors");
			return null;
		}
		
		int numAllindividuals = landscape.getMap().size();
		
		CpxGPIndividual tryindi = landscape.getMap().get(state.random[thread].nextInt(numAllindividuals));
		
		GenoVector g0 = landscape.indexlist.getGenoVector(sample);
		for(int cn = 0; cn < default_percentNeighborSampling*numAllindividuals; cn++)
		{
			tryindi = landscape.getMap().get(state.random[thread].nextInt(numAllindividuals));
			GenoVector g1 = landscape.indexlist.getGenoVector(tryindi);
			
			if(Math.sqrt(norm2Q.Q(g0, g1)) <= epsilon) {

//				if(!tryindi.evaluated) {
//					System.err.print("we must preprint the landscape before getting neighbors\n");
//					System.exit(1);
//				}
				
				//find the fitness of tryindi
				for(int i = 0; i<landscape.getMap().size(); i++) {
					CpxGPIndividual ind = landscape.getMap().get(i);
					if(ind.equals(tryindi)) {

						if(!ind.evaluated) {
							System.err.print("we must preprint the landscape before getting neighbors\n");
							System.exit(1);
						}
						
						tryindi.fitness = ind.fitness;
						tryindi.evaluated = true;
						break;
					}
				}
				
				return tryindi;
			}
		}
		
		return tryindi;
	}

}
