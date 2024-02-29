package zhixing.cpxInd.algorithm.semantic.individual.reproduce;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPBreedingPipeline;
import ec.gp.GPIndividual;
import ec.gp.koza.MutationPipeline;
import ec.util.Parameter;
//import zhixing.symbolicregression.algorithm.semantic.individual.SLGPIndividual;
//import zhixing.symbolicregression.algorithm.semantic.library.SemanticLibrary;
import zhixing.cpxInd.algorithm.semantic.SubpopulationSLGP;
import zhixing.cpxInd.algorithm.semantic.individual.GPTreeStructSemantic;
import zhixing.cpxInd.algorithm.semantic.individual.SLGPIndividual;
import zhixing.cpxInd.algorithm.semantic.library.LibraryItem;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;
import zhixing.cpxInd.algorithm.semantic.library.SemanticVector;
import zhixing.cpxInd.individual.reproduce.LGPMicroMutationPipeline;

public class MutateAndDivide extends GPBreedingPipeline{

	public static final String P_MUTATEANDDIVIDE = "MDP";
	
	public static final String P_MICROMUTBASE = "micro_base";

	protected LGPMicroMutationPipeline microMutation = null;
	
	public static final int INDS_PRODUCED = 1;
    public static final int NUM_SOURCES = 1;
	
	@Override
	public Parameter defaultBase() {
		return new Parameter(P_MUTATEANDDIVIDE);
	}

	@Override
	public int numSources() { return NUM_SOURCES; }

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = defaultBase();
		
//		Parameter microbase = new Parameter(state.parameters.getString(base.push(P_MICROMUTBASE), def.push(P_MICROMUTBASE)));
//	        microMutation = null;
//	        if(!microbase.toString().equals("null")){
//	        	//microMutation = new LGPMicroMutationPipeline();
//	        	microMutation = (LGPMicroMutationPipeline)(state.parameters.getInstanceForParameter(
//	                    microbase, def.push(P_MICROMUTBASE), MutationPipeline.class));
//	   		 microMutation.setup(state, microbase);
//	     }
	}
	
	@Override
	public int produce(int min, int max, int start, int subpopulation, Individual[] inds, EvolutionState state,
			int thread) {
		// grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did


        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
            {
            GPIndividual i = (GPIndividual)inds[q];

            // add the new individual, replacing its previous source
            inds[q] = produce(subpopulation, i, state, thread);
            }
        return n;
	}

	
	public Individual produce(final int subpopulation,
	        final Individual i,
	        final EvolutionState state,
	        final int thread) {
		
		SLGPIndividual j = (SLGPIndividual) i.clone();
		
		SemanticLibrary semlib = ((SubpopulationSLGP) state.population.subpops[subpopulation]).semanticLib;
		
		MDP(semlib, j.getInputS(), j.getTargetS(), -1, j.getTreesLength() - 1, 0, state, thread, j);
		
		if(microMutation != null) j = (SLGPIndividual) microMutation.produce(subpopulation, j, state, thread);
		
		return j;
	}
	
	protected int randint(EvolutionState state, int thread, int L, int U) {
		return state.random[thread].nextInt(U - L + 1) + L;
	}
	
	protected SemanticVector MDP(final SemanticLibrary semlib, final SemanticVector Ins, final SemanticVector desS,
			int head, int tail, int STEP, EvolutionState state, int thread, SLGPIndividual ind) {
		if(STEP == 0) {
			STEP = randint(state, thread, 1, ind.getTreesLength());
		}
		
		int InputSlot = Math.max(head, randint(state, thread, tail - STEP, tail - 1));
		int MutateSlot = randint(state, thread, InputSlot + 1, tail);
		int Offset = 0; //if there are more than one instruction in the LibraryItem, the Offset indexes the number of additional instructions that are added into the program
		
		SemanticVector X_star;
		if(InputSlot == head) {
			X_star = Ins;
		}
		else {
			X_star = ((GPTreeStructSemantic) ind.getTreeStruct(InputSlot)).context;
		}
		
		SemanticVector DI = new SemanticVector();
		SemanticVector EO = new SemanticVector();
		SemanticVector nestedDI = new SemanticVector();
		LibraryItem trial;
		
		int trail_index = semlib.selectInstr(state, thread, X_star, desS, DI, EO);
		trial = semlib.getItem(trail_index);
		int insert_len = trial.instructions.length;
		
		for(int i = 0; i<insert_len; i++) {
			
			GPTreeStructSemantic trailStructSem = new GPTreeStructSemantic(trial.instructions[i]); //clone the library item
			
			
			if(i == 0) {
				trailStructSem.copySemanticVectorFrom( ((GPTreeStructSemantic) ind.getTreeStruct(MutateSlot)).context );
				ind.setTree(MutateSlot, trailStructSem);
			}
			else {
				
				if(MutateSlot + Offset + 1 <= tail) {
					Offset += 1;
					trailStructSem.copySemanticVectorFrom(((GPTreeStructSemantic) ind.getTreeStruct(MutateSlot + Offset)).context);
					ind.setTree(MutateSlot + Offset, trailStructSem);
				}

			}
		}
		
		nestedDI.assignfrom(DI);
		
		if(MutateSlot + Offset < tail) {
			MDP(semlib, EO, desS, MutateSlot + Offset, tail, STEP, state, thread, ind);
		}
		if(InputSlot < MutateSlot - 1) {
			SemanticVector tmp = MDP(semlib, X_star, DI, InputSlot, MutateSlot - 1, STEP, state, thread, ind);
			nestedDI.assignfrom(tmp);
		}
		if(head < InputSlot) {
			SemanticVector tmp = MDP(semlib, Ins, nestedDI, head, InputSlot, STEP, state, thread, ind);
			nestedDI.assignfrom(tmp);
		}
		
		ind.evaluated = false;
		
		return nestedDI;
	}
}
