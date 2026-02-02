package zhixing.cpxInd.individual;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.util.Parameter;

public abstract class CpxGPIndividual extends GPIndividual{
	//The biggest differences between CpxGPIndividual and GPIndividual are using STL collections to contain GPTrees
	//and these individuals will contain more information.
	
	//To be compatible with existing functions of GPIndividual, CpxGPIndividual inherits GPIndividuals, with a lot of different features
	
	private static final long serialVersionUID = 1;

    public static final String P_NUMTREES = "numtrees";
    public static final String P_TREE = "tree";
    public static final String P_TOWRAP = "to-wrap";
    public static final String P_NORMWRAP = "norm-wrap";
    public static final String P_NORMWRAP_F = "norm-wrap-factor";
    public static final String P_WRAP_MAX_SAMPLE = "wrap_max_sample";
    
    protected boolean towrap = false;
    protected boolean normalize_wrap = false;
    protected double normalize_f = 1e-3;
    protected double weight_norm = 0;
    protected int wrap_max_sample = 1000;
    
    public abstract void rebuildIndividual(EvolutionState state, int thread);
    
    @Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup_extensive(state, base);
	}
	
	@Override
	public abstract boolean equals(Object ind);
	
	@Override
	public abstract int hashCode();
	
	public abstract void verify(EvolutionState state);
	
	public abstract void printTrees(final EvolutionState state, final int log);
	
	@Override
	public abstract void printIndividualForHumans(final EvolutionState state, final int log);
	
	@Override
	public abstract void printIndividual(final EvolutionState state, final int log);
	
	@Override
	public abstract void printIndividual(final EvolutionState state, final PrintWriter writer);
	
	/** Overridden for the GPIndividual genotype. */
	@Override
    public abstract void writeGenotype(final EvolutionState state,
        final DataOutput dataOutput)throws IOException;

    /** Overridden for the GPIndividual genotype. */
	@Override
    public abstract void readGenotype(final EvolutionState state,
        final DataInput dataInput)throws IOException;

	@Override
    public abstract void parseGenotype(final EvolutionState state,
        final LineNumberReader reader)throws IOException;
	
	@Override
	public Object clone(){
		CpxGPIndividual obj =(CpxGPIndividual) super.clone_extensive();
		obj.towrap = this.towrap;
		return obj;
	}

	/** Like clone(), but doesn't force the GPTrees to deep-clone themselves. */
	public abstract CpxGPIndividual lightClone();
	
	/** Returns the "size" of the individual, namely, the number of nodes
	    in all of its subtrees.  */
	@Override
	public abstract long size();

//	public abstract double evaluate(final EvolutionState state,
//        final int thread,
//        final GPData input,
//        final ADFStack stack,
//        final GPIndividual individual,
//        final Problem problem);
	
//	public void prepareExecution(EvolutionState state){};
	
	//the wrapper applies polynomial regression to fine tune the final output of GP programs, returns the tuned outputs. 
	public abstract double [] wrapper(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, double [] predict, double [] target);
	
	public abstract ArrayList<Double[]> wrapper(ArrayList<Double[]> predict_list, ArrayList<Double[]> target_list, EvolutionState state, int thread, GPProblem problem);
	
	public boolean IsWrap() {return towrap;}
	
	public abstract Object getWrapper();
	
	public double getWeightNorm() {
		return weight_norm;
	}
	public double getNormFactor() {
		return normalize_f;
	}
}
