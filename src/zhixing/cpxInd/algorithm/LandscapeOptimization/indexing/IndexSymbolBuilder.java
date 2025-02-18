package zhixing.cpxInd.algorithm.LandscapeOptimization.indexing;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.gp.GPFunctionSet;
import ec.gp.GPType;
import ec.gp.koza.KozaBuilder;
import ec.util.Parameter;
import zhixing.cpxInd.individual.GPTreeStruct;

public abstract class IndexSymbolBuilder<T> extends KozaBuilder{

	public static final String SYMBOLBUILDER = "symbolbuilder";
	
	public static final String P_MAXNUMBERSYMBOLS = "maxnumsymbols";
	
	public static int maxNumSymbols; // the maximum number of symbols the builder can enumerate
	
	public boolean initialized = false;

	public void setup(final EvolutionState state, final Parameter base)
    {
	    super.setup(state,base);
	
	    Parameter def = defaultBase();
	    
	    maxNumSymbols = state.parameters.getInt(base.push(P_MAXNUMBERSYMBOLS),def.push(P_MAXNUMBERSYMBOLS),1);
	    if (maxNumSymbols<=0)
	        state.output.fatal("The maximum number of symbols for a symbol builder must be at least 1.",
	            base.push(P_MAXNUMBERSYMBOLS),def.push(P_MAXNUMBERSYMBOLS));
    }
	
	@Override
	public Parameter defaultBase() {
		return new Parameter(SYMBOLBUILDER);
	}
	
	public abstract ArrayList<T> enumerateSymbols(EvolutionState state, GPType type, int thread,final int argposition, GPFunctionSet set);
}
