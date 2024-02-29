package zhixing.cpxInd.algorithm.semantic.library.produce;

import java.util.ArrayList;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.semantic.library.SLBreedingPipeline;
import zhixing.cpxInd.individual.GPTreeStruct;

public abstract class SLInsBreedingPipeline extends SLBreedingPipeline {
	/** Standard parameter for node-selectors associated with a GPBreedingPipeline */
    public static final String P_NODESELECTOR = "ns";
    
    public abstract int getNumParents(); //the number of parents
}
