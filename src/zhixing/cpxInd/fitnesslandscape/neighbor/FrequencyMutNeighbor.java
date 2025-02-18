package zhixing.cpxInd.fitnesslandscape.neighbor;

import java.util.ArrayList;

import ec.EvolutionState;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.reproduce.LGPMacroMutationPipeline;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
import zhixing.cpxInd.algorithm.Graphbased.individual.reproduce.FrequencyMacroMutation;
//import zhixing.jss.cpxInd.algorithm.Graphbased.individual.reproduce.FrequencyMacroMutation;

public class FrequencyMutNeighbor extends OnehopMutNeighbor{

	@Override
	public ArrayList<CpxGPIndividual> getNeighbors(EvolutionState state, int thread, CpxGPIndividual sample,
			LGPFitnessLandscape landscape, double epsilon) {
		if(sample == null) {
			System.err.print("please sample individuals from the search space before identifying their neighbors");
			return null;
		}
		
		geneticOperator = (FrequencyMacroMutation) state.population.subpops[0].species.pipe_prototype.sources[pipelineindex];
		
		ArrayList<CpxGPIndividual> tmp = new ArrayList<>();
		
		int numAllindividuals = landscape.getMap().size();
		
		LGPIndividual parents[] = new LGPIndividual[1];
		parents[0] = (LGPIndividual) sample;
		
		for(int cn = 0; cn < Math.max(300, default_percentNeighborSampling*numAllindividuals); cn++)
		{
			CpxGPIndividual tryindi;
			
			tryindi = ((FrequencyMacroMutation)geneticOperator).produce(0, 0, 0, 0, state, thread, parents);
			
//			boolean exist = false;
//			for(CpxGPIndividual t : tmp) {
//				if(t.equals(tryindi)) {
//					exist = true;
//					break;
//				}
//			}
//			if(!exist) {
//				
//			}
			
			//to show the probability (or frequency) of different neighbors, we don't check the duplication of neighbors
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
			
			if(tmp.size() >= 300) break;
		}			
		
		return tmp;
	}
	
	@Override
	public CpxGPIndividual getaNeighbor(EvolutionState state, int thread, CpxGPIndividual sample,
			LGPFitnessLandscape landscape, double epsilon) {
		if(sample == null) {
			System.err.print("please sample individuals from the search space before identifying their neighbors");
			return null;
		}
		
		geneticOperator = (FrequencyMacroMutation) state.population.subpops[0].species.pipe_prototype.sources[pipelineindex];
		
		int numAllindividuals = landscape.getMap().size();
		
		LGPIndividual parents[] = new LGPIndividual[1];
		parents[0] = (LGPIndividual) sample;
		
		CpxGPIndividual tryindi = ((FrequencyMacroMutation)geneticOperator).produce(0, 0, 0, 0, state, thread, parents);
		
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
