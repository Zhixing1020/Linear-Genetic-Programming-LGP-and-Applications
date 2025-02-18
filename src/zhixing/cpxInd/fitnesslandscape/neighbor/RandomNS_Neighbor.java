package zhixing.cpxInd.fitnesslandscape.neighbor;

import java.util.ArrayList;

import ec.EvolutionState;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.norm2Q;
import zhixing.cpxInd.algorithm.LandscapeOptimization.reproduce.NeighborhoodSearch;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.reproduce.LGPMacroMutationPipeline;

public class RandomNS_Neighbor extends LNS_Neighbor{
	
	@Override
	public ArrayList<CpxGPIndividual> getNeighbors(EvolutionState state, int thread, CpxGPIndividual sample,
			LGPFitnessLandscape landscape, double epsilon) {
		if(sample == null) {
			System.err.print("please sample individuals from the search space before identifying their neighbors");
			return null;
		}
		
		geneticOperator = (NeighborhoodSearch) state.population.subpops[0].species.pipe_prototype.sources[pipelineindex];
		
		ArrayList<CpxGPIndividual> tmp = new ArrayList<>();
		
		int numAllindividuals = landscape.getMap().size();
		
		CpxGPIndividual parents[] = new CpxGPIndividual[2];
		parents[0] = sample;
		GenoVector g0 = landscape.indexlist.getGenoVector(sample);
		
		for(int cn = 0; cn < Math.max(300, default_percentNeighborSampling*numAllindividuals); cn++)
		{
			CpxGPIndividual tryindi;
			CpxGPIndividual inds[] = new CpxGPIndividual[1];
			
			//sample a parent from the current population
			int mapsize = landscape.getMap().size();
			int tryindex = state.random[thread].nextInt(mapsize);
			CpxGPIndividual parent = (CpxGPIndividual) landscape.getMap().get(tryindex);
			parents[1] = parent;
			
			geneticOperator.produce(0, 0, 0, 0, inds, state, thread, parents);
			
			tryindi = inds[0];
			
			GenoVector g1 = landscape.indexlist.getGenoVector(tryindi);
			
//			if(cn > 290 && tmp.size() == 0) {
//				int aaa =1;
//			}
//			
//			System.out.print(""+Math.sqrt(norm2Q.Q(g0, g1)) + "\t");
//			if(Math.sqrt(norm2Q.Q(g0, g1)) > epsilon) {
//				int bbb = 1;
//			}
			if(Math.sqrt(norm2Q.Q(g0, g1)) <= epsilon) {
				boolean exist = false;
				for(CpxGPIndividual t : tmp) {
					if(t.equals(tryindi)) {
						exist = true;
						break;
					}
				}
				if(!exist) {
					tmp.add(tryindi);
					
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
				}
			}

			if(tmp.size() >= 300) break;
		}			
		
//		System.out.print("\n");
		return tmp;
	}
	
	@Override
	public CpxGPIndividual getaNeighbor(EvolutionState state, int thread, CpxGPIndividual sample,
			LGPFitnessLandscape landscape, double epsilon) {
		if(sample == null) {
			System.err.print("please sample individuals from the search space before identifying their neighbors");
			return null;
		}
		
		geneticOperator = (NeighborhoodSearch) state.population.subpops[0].species.pipe_prototype.sources[pipelineindex];
		
		int numAllindividuals = landscape.getMap().size();
		
		CpxGPIndividual parents[] = new CpxGPIndividual[2];
		parents[0] = sample;
		GenoVector g0 = landscape.indexlist.getGenoVector(sample);
		
		CpxGPIndividual tryindi = landscape.getMap().get(state.random[thread].nextInt(numAllindividuals));
		
		for(int cn = 0; cn < Math.max(300, default_percentNeighborSampling*numAllindividuals); cn++)
		{
			
			CpxGPIndividual inds[] = new CpxGPIndividual[1];
			
			//sample a parent from the current population
			int mapsize = landscape.getMap().size();
			int tryindex = state.random[thread].nextInt(mapsize);
			CpxGPIndividual parent = (CpxGPIndividual) landscape.getMap().get(tryindex);
			parents[1] = parent;
			
			geneticOperator.produce(0, 0, 0, 0, inds, state, thread, parents);
			
			tryindi = inds[0];
			
			GenoVector g1 = landscape.indexlist.getGenoVector(tryindi);
			
//			if(cn > 290 && tmp.size() == 0) {
//				int aaa =1;
//			}
//			
//			System.out.print(""+Math.sqrt(norm2Q.Q(g0, g1)) + "\t");
//			if(Math.sqrt(norm2Q.Q(g0, g1)) > epsilon) {
//				int bbb = 1;
//			}
			if(Math.sqrt(norm2Q.Q(g0, g1)) <= epsilon) {
				
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

//			if(tmp.size() >= 300) break;
		}
		
		return tryindi;
	}
}
