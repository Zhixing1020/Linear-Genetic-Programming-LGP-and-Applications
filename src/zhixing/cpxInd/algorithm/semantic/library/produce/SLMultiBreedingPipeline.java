package zhixing.cpxInd.algorithm.semantic.library.produce;

import java.util.ArrayList;

import ec.BreedingPipeline;
import ec.BreedingSource;
import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.semantic.library.LibraryItem;
import zhixing.cpxInd.algorithm.semantic.library.SLBreedingPipeline;
import zhixing.cpxInd.algorithm.semantic.library.SLBreedingSource;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;

public class SLMultiBreedingPipeline extends SLBreedingPipeline{

	public static final String P_SLMULTIBREED = "slmultibreed";
	
	@Override
	public int numSources() {
		return DYNAMIC_SOURCES;
	}

	@Override
	public Parameter defaultBase() {
		return new Parameter(P_SLMULTIBREED);
	}
	
	public void setup(final EvolutionState state, final Parameter base)
    {
	    super.setup(state,base);
	
	    Parameter def = defaultBase();
	
	    double total = 0.0;
	    
	    if (sources.length == 0)  // uh oh
	        state.output.fatal("num-sources must be provided and > 0 for MultiBreedingPipeline",
	            base.push(P_NUMSOURCES), def.push(P_NUMSOURCES));
	    
	    for(int x=0;x<sources.length;x++)
	        {
	        // make sure the sources are actually breeding pipelines
	        if (!(sources[x] instanceof SLBreedingPipeline))
	            state.output.error("Source #" + x + "is not a SLBreedingPipeline",base);
	        else if (sources[x].probability<0.0) // null checked from state.output.error above
	            state.output.error("Pipe #" + x + " must have a probability >= 0.0",base);  // convenient that NO_PROBABILITY is -1...
	        else total += sources[x].probability;
	        }
	
	    state.output.exitIfErrors();
	
	    // Now check for nonzero probability (we know it's positive)
	    if (total == 0.0) {
	    	state.output.warning("MultiBreedingPipeline's children have all zero probabilities.  This could be an error.", base);
	    	System.exit(1);
	    }
	    
	    // allow all zero probabilities
        SLBreedingSource.setupProbabilities(sources);
    }

	@Override
	public LibraryItem produce(EvolutionState state, int thread, SemanticLibrary semlib) {
		
		SLInsBreedingPipeline s = (SLInsBreedingPipeline)sources[SLInsBreedingPipeline.pickRandom(
                sources,state.random[thread].nextDouble())];
		
		return s.produce(state, thread, semlib);
	}

	@Override
	public LibraryItem produce(EvolutionState state, int thread, SemanticLibrary semlib,
			ArrayList<LibraryItem> parents) {
		
		if(parents!=null) {
			System.err.print("this produce function in SLMultiBreedingPipeline will never use the parameter \"parents\"\n");
		}
		
		SLInsBreedingPipeline s = (SLInsBreedingPipeline)sources[SLInsBreedingPipeline.pickRandom(
                sources,state.random[thread].nextDouble())];
		
		return s.produce(state, thread, semlib);
	}

}
