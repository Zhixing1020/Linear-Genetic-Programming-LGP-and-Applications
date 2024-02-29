package zhixing.cpxInd.algorithm.semantic.library;

import java.util.ArrayList;

import ec.BreedingSource;
import ec.EvolutionState;
import ec.util.Parameter;
import ec.util.RandomChoice;
import ec.util.RandomChoiceChooserD;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;

public abstract class SLBreedingSource implements RandomChoiceChooserD{

	public static final String P_PROB = "prob";
    public static final double NO_PROBABILITY = -1.0;
    
    public double probability;
	
    public abstract Parameter defaultBase();
    
    public void setup(final EvolutionState state, final Parameter base)
    {
		Parameter def = defaultBase();
		
		if (!state.parameters.exists(base.push(P_PROB),def.push(P_PROB)))
		    probability = NO_PROBABILITY;
		else
		    {
		    probability = state.parameters.getDouble(base.push(P_PROB),def.push(P_PROB),0.0);
		    if (probability<0.0) state.output.error("SL Breeding Source's probability must be a double floating point value >= 0.0, or empty, which represents NO_PROBABILITY.",base.push(P_PROB),def.push(P_PROB));
		    }
    }
    
    public final double getProbability(final Object obj)
    {
    	return ((SLBreedingSource)obj).probability;
    }

    public final void setProbability(final Object obj, final double prob)
    {
    	((SLBreedingSource)obj).probability = prob;
    }
	
	
	public Object clone()
    {
	    try { return super.clone(); }
	    catch (CloneNotSupportedException e) 
	        { throw new InternalError(); } // never happens
    }
	
	public static int pickRandom(final SLBreedingSource[] sources, final double prob)
    {
		return RandomChoice.pickFromDistribution(sources,sources[0], prob);
    }
	
	public static void setupProbabilities(final SLBreedingSource[] sources)
    {
		RandomChoice.organizeDistribution(sources,sources[0],true);
    }
	
	public abstract LibraryItem produce(EvolutionState state, int thread, SemanticLibrary semlib);
	
	public abstract LibraryItem produce(EvolutionState state, int thread, SemanticLibrary semlib, ArrayList<LibraryItem> parents);
}
