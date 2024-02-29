package zhixing.cpxInd.algorithm.semantic.library;

import java.util.ArrayList;

import ec.BreedingPipeline;
import ec.BreedingSource;
import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.util.Parameter;
import zhixing.cpxInd.individual.GPTreeStruct;

public abstract class SLBreedingPipeline extends SLBreedingSource{

    public static final String V_SAME = "same";

	public static final String P_PIPELINE = "SLbreedpipe";
	public static final String P_NUMSOURCES = "num-sources";
	public static final String P_SOURCE = "source";
    public static final int DYNAMIC_SOURCES = -1;

	
    public Parameter mybase;
	
	public SLBreedingSource[] sources;

//	@Override
//	public Parameter defaultBase() {
//		return new Parameter(P_PIPELINE);
//	}
    public abstract int numSources();


	public void setup(final EvolutionState state, final Parameter base)
    {
	    super.setup(state,base);
	    mybase = base;
	    
	    Parameter def = defaultBase();
	 
	    int numsources = numSources();
        if (numsources == DYNAMIC_SOURCES)
        {
        // figure it from the file
        numsources = state.parameters.getInt(base.push(P_NUMSOURCES), def.push(P_NUMSOURCES),0);
        if (numsources==-1)
            state.output.fatal("Breeding pipeline num-sources value must exist and be >= 0", base.push(P_NUMSOURCES), def.push(P_NUMSOURCES)); 
        }
        else if (numsources <= DYNAMIC_SOURCES)  // it's negative
        {
        	throw new RuntimeException("In " + this + " numSources() returned < DYNAMIC_SOURCES (that is, < -1)");
        }
        else
        {
        if (state.parameters.exists(base.push(P_NUMSOURCES), def.push(P_NUMSOURCES))) // uh oh
            state.output.warning("Breeding pipeline's number of sources is hard-coded to " + numsources + " yet num-sources was provided: num-sources will be ignored.",
                base.push(P_NUMSOURCES), def.push(P_NUMSOURCES));
        }
        
        
	    sources = new SLBreedingSource[numsources];
	
	    for(int x=0;x<sources.length;x++)
	        {
	        Parameter p = base.push(P_SOURCE).push(""+x);
	        Parameter d = def.push(P_SOURCE).push(""+x);
	
	        String s = state.parameters.getString(p,d);
	        if (s!=null && s.equals(V_SAME))
	            {
	            if (x==0)  // oops
	                state.output.fatal(
	                    "Source #0 cannot be declared with the value \"same\".",
	                    p,d);
	            
	            // else the source is the same source as before
	            sources[x] = sources[x-1];
	            }
	        else 
	            {
	            sources[x] = (SLBreedingSource)
	                (state.parameters.getInstanceForParameter(
	                    p,d,SLBreedingSource.class));
	            sources[x].setup(state,p);
	            }
	        }
	    state.output.exitIfErrors();
    }


	public Object clone()
    {
	    SLBreedingPipeline c = (SLBreedingPipeline)(super.clone());
	    
	    // make a new array
	    c.sources = new SLBreedingSource[sources.length];
	
	    // clone the sources -- we won't go through the hassle of
	    // determining if we have a DAG or not -- we'll just clone
	    // it out to a tree.  I doubt it's worth it.
	
	    for(int x=0;x<sources.length;x++)
	        {
	        if (x==0 || sources[x]!=sources[x-1])
	            c.sources[x] = (SLBreedingSource)(sources[x].clone());
	        else 
	            c.sources[x] = c.sources[x-1];
	        }
	
	    return c;
    }
}
