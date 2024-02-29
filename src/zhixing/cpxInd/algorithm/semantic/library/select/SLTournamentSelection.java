package zhixing.cpxInd.algorithm.semantic.library.select;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Individual;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.semantic.library.LibraryItem;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;

public class SLTournamentSelection extends SLSelectionMethod{

	private static final String P_SLTOURNAMENTSELECTION = "SLTournament";
	public static final String P_PICKSMALL = "pick-small";
	public static final String P_SIZE = "size";
	
	protected int size;
	public boolean pickSmall;
	
	@Override
	public Parameter defaultBase() {
		return new Parameter(P_SLTOURNAMENTSELECTION);
	}
	
	public void setup(final EvolutionState state, final Parameter base)
    {
	    super.setup(state,base);
	    
	    Parameter def = defaultBase();
	
	    size = state.parameters.getInt(base.push(P_SIZE),def.push(P_SIZE),1);
	
	    pickSmall = state.parameters.getBoolean(base.push(P_PICKSMALL),def.push(P_PICKSMALL),false);
    }
		
	public int getRandomIndividual(EvolutionState state, int thread, SemanticLibrary semlib)
    {
	    return state.random[thread].nextInt(semlib.getLibrarySize());
    }
	
	@Override
	public LibraryItem produce(EvolutionState state, int thread, SemanticLibrary semlib) {
		
		int best = getRandomIndividual(state, thread, semlib);
		
		int s = size;
		
		LibraryItem bestItem = semlib.getItem(best);
		bestItem.fitness.determineFitness(state, thread, semlib, best);
		
		 if (pickSmall)
	            for (int x=1;x<s;x++)
	                {
	                int j = getRandomIndividual(state, thread, semlib);
	                LibraryItem tmp = semlib.getItem(j);
	                tmp.fitness.determineFitness(state, thread, semlib, j);
	                if ( bestItem.fitness.value >= tmp.fitness.value && state.random[thread].nextDouble() > Math.min(tmp.frequency / semlib.DISTRIBUTE, semlib.Pthresold))  // j is at least as bad as best
	                    bestItem = tmp;
	                	
	                }
	        else
	            for (int x=1;x<s;x++)
	                {
	                int j = getRandomIndividual(state, thread, semlib);
	                LibraryItem tmp = semlib.getItem(j);
	                tmp.fitness.determineFitness(state, thread, semlib, j);
	                if (bestItem.fitness.value < tmp.fitness.value && state.random[thread].nextDouble() <= Math.min(tmp.frequency / semlib.DISTRIBUTE, semlib.Pthresold))  // j is better than best
	                    bestItem = tmp;
	                }
	            
		
		return bestItem;
	}

	@Override
	public LibraryItem produce(EvolutionState state, int thread, SemanticLibrary semlib,
			ArrayList<LibraryItem> parents) {
		
		System.err.print("SLTournamentSelection does not use the parameter \"parents\"\n");
		
		return produce(state, thread, semlib);
	}

}
