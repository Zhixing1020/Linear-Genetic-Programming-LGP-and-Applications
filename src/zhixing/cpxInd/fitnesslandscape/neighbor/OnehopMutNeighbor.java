package zhixing.cpxInd.fitnesslandscape.neighbor;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.gp.GPBreedingPipeline;
import ec.util.Parameter;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.reproduce.LGPMacroMutationPipeline;

public class OnehopMutNeighbor extends NeighborStruct{
	final static public String ONEHOPMUT = "onehop_mutation";
	final static public String P_PIPELINEINDEX = "pipelineindex";
	
	
	protected GPBreedingPipeline geneticOperator;
	
	int pipelineindex;
	
	protected ArrayList<CpxGPIndividual> neighbors = new ArrayList<>();
	protected CpxGPIndividual setSample;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = new Parameter(ONEHOPMUT);
		
		pipelineindex = state.parameters.getInt(base.push(P_PIPELINEINDEX), def.push(P_PIPELINEINDEX));
		if(pipelineindex < 0) {
			System.err.print("the index of breeding pipeline in OnehopMutNeighbor must be in range larger than or equal to 0.\n");
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
		
		geneticOperator = (GPBreedingPipeline) state.population.subpops[0].species.pipe_prototype.sources[pipelineindex];
		
		ArrayList<CpxGPIndividual> tmp = new ArrayList<>();
		
		int numAllindividuals = landscape.getMap().size();
		
		for(int cn = 0; cn < Math.max(300, default_percentNeighborSampling*numAllindividuals); cn++)
		{
			CpxGPIndividual tryindi;
			tryindi = (CpxGPIndividual) ((LGPMacroMutationPipeline)geneticOperator).produce(0, (LGPIndividual)sample, state, thread);
			
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
		
		return tmp;
	}

	@Override
	public boolean isNeighbor(EvolutionState state, int thread, CpxGPIndividual sample, CpxGPIndividual tryind,
			LGPFitnessLandscape landscape, double epsilon) {
		
		boolean res = false;

		if(sample != setSample) {
			neighbors = getNeighbors(state, thread, sample, landscape, epsilon);
			setSample = sample;
		}
		
		for(CpxGPIndividual t : neighbors) {
			if(t.equals(tryind)) {
				res = true;
				break;
			}
		}
		
		return res;
	}

	@Override
	public double distance(EvolutionState state, int thread, CpxGPIndividual sample1, CpxGPIndividual sample2,
			LGPFitnessLandscape landscape) {
		double res = 0;
		
		int size1 = sample1.getTreesLength();
		int size2 = sample2.getTreesLength();
		
		res += Math.abs(size1-size2);
		for(int i = 0; i<Math.min(size1, size2); i++) {
			if(! ((LGPIndividual)sample1).getTreeStruct(i).toString().equals(((LGPIndividual)sample2).getTreeStruct(i).toString())) {
				res += 1;
			}
		}
		
		return res;
	}

	@Override
	public CpxGPIndividual getaNeighbor(EvolutionState state, int thread, CpxGPIndividual sample,
			LGPFitnessLandscape landscape, double epsilon) {
		if(sample == null) {
			System.err.print("please sample individuals from the search space before identifying their neighbors");
			return null;
		}
		
		geneticOperator = (LGPMacroMutationPipeline) state.population.subpops[0].species.pipe_prototype.sources[pipelineindex];
		
		int numAllindividuals = landscape.getMap().size();
		
		CpxGPIndividual tryindi;
		tryindi = (CpxGPIndividual) ((LGPMacroMutationPipeline)geneticOperator).produce(0, (LGPIndividual)sample, state, thread);
		
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
