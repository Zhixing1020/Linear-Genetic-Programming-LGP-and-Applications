package zhixing.cpxInd.fitnesslandscape.neighbor;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.norm2Q;
import zhixing.cpxInd.algorithm.LandscapeOptimization.reproduce.NeighborhoodSearch;

public class LNS_Neighbor extends OnehopMutNeighbor{
	
	final static public String LNS = "landscapeNeighborSearch";
		
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = new Parameter(LNS);
		
		pipelineindex = state.parameters.getInt(base.push(P_PIPELINEINDEX), def.push(P_PIPELINEINDEX));
		if(pipelineindex < 0) {
			System.err.print("the index of breeding pipeline in LNS_Neighbor must be in range larger than or equal to 0.\n");
			System.exit(1);
		}
	}
	
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
//		GenoVector g0 = landscape.indexlist.getGenoVector(sample);
		
		for(int cn = 0; cn < Math.max(300, default_percentNeighborSampling*numAllindividuals); cn++)
		{
			CpxGPIndividual tryindi;
			CpxGPIndividual inds[] = new CpxGPIndividual[1];
			
			//sample a parent from the current population
			int popsize = state.population.subpops[0].individuals.length;
			int tryindex = state.random[thread].nextInt(popsize);
			CpxGPIndividual parent = (CpxGPIndividual) state.population.subpops[0].individuals[ tryindex ];
			for(int tr = 1; !(parent.fitness.betterThan(sample.fitness) || parent.fitness.equivalentTo(sample.fitness)) && tr<7; tr++) {
				tryindex = state.random[thread].nextInt(popsize);
				parent = (CpxGPIndividual) state.population.subpops[0].individuals[ tryindex ];
			}
			parents[1] = parent;
			
			geneticOperator.produce(0, 0, 0, 0, inds, state, thread, parents);
			
			tryindi = inds[0];
			
//			GenoVector g1 = landscape.indexlist.getGenoVector(tryindi);
			
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

			if(tmp.size() >= 300) break;
		}			
		
		return tmp;
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
	public CpxGPIndividual getaNeighbor(EvolutionState state, int thread, CpxGPIndividual sample, LGPFitnessLandscape landscape, double epsilon) {
		if(sample == null) {
			System.err.print("please sample individuals from the search space before identifying their neighbors");
			return null;
		}
		
		geneticOperator = (NeighborhoodSearch) state.population.subpops[0].species.pipe_prototype.sources[pipelineindex];
		
		int numAllindividuals = landscape.getMap().size();
		
		CpxGPIndividual parents[] = new CpxGPIndividual[2];
		parents[0] = sample;
//		GenoVector g0 = landscape.indexlist.getGenoVector(sample);
		
		CpxGPIndividual tryindi = landscape.getMap().get(state.random[thread].nextInt(numAllindividuals));
		
		CpxGPIndividual inds[] = new CpxGPIndividual[1];
		
		//sample a parent from the current population
		int popsize = state.population.subpops[0].individuals.length;
		int tryindex = state.random[thread].nextInt(popsize);
		CpxGPIndividual parent = (CpxGPIndividual) state.population.subpops[0].individuals[ tryindex ];
		for(int tr = 1; !(parent.fitness.betterThan(sample.fitness) || parent.fitness.equivalentTo(sample.fitness)) && tr<7; tr++) {
			tryindex = state.random[thread].nextInt(popsize);
			parent = (CpxGPIndividual) state.population.subpops[0].individuals[ tryindex ];
		}
		parents[1] = parent;
		
		geneticOperator.produce(0, 0, 0, 0, inds, state, thread, parents);
		
		tryindi = inds[0];
		
//		GenoVector g1 = landscape.indexlist.getGenoVector(tryindi);
		
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
