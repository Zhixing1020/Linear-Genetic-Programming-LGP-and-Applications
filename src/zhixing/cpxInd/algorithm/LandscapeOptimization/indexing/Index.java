package zhixing.cpxInd.algorithm.LandscapeOptimization.indexing;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.cpxInd.individual.LGPDefaults;

public abstract class Index <T> {
	//recording the symbols with the same genotype or phenotype or semantics
	public static final String INDEX = "index";
//	public static final String DUP_MODE = "duplicate_mode";
//	public static final String DUP_MODE_GENO = "genotype";
//	public static final String DUP_MODE_PHENO = "phenotype";
//	public static final String DUP_MODE_SEM = "semantics";
	
	public static final String NUM_INPUTS = "num_inputs";
	public static final String DIM_INPUTS = "dim_inputs";
	
	public static final String SYMBOL_PROTO = "symbol_prototype";

	protected int num_inputs;
	protected int dim_inputs; 
	
	protected double input_lb = -100;
	protected double input_ub = 100;
	
	protected static double inherent_inputs []; 
	
//	private String duplicateMode;
	
	public T sym_prototype;
	
	public int index; //the unique index in the index list
	public ArrayList<T> symbols  = new ArrayList<T>(); //symbols with the same phenotype or semantics
	
	double tabu_frequency = 0; // the frequency of this symbol
	public static final double TABU_THRESOLD = 0.95;
	
	public void setup(final EvolutionState state, final Parameter base) {
		Parameter def = new Parameter(INDEX);
		
//		duplicateMode = state.parameters.getString(base.push(DUP_MODE), def.push(DUP_MODE));
		
		
		num_inputs = state.parameters.getInt(base.push(NUM_INPUTS), def.push(NUM_INPUTS), 1);
		if(num_inputs <= 0) {
			System.err.print("Index must have a number of inputs larger than 0");
			System.exit(1);
		}
		
		dim_inputs = state.parameters.getIntWithDefault(base.push(DIM_INPUTS), def.push(DIM_INPUTS), 2);
		if(dim_inputs <= 0) {
			System.err.print("Index must have a dimension of inputs larger than 0");
			System.exit(1);
		}
		
		inherent_inputs = new double [num_inputs*dim_inputs];
		for(int i = 0; i<inherent_inputs.length; i++) {
			inherent_inputs[i] = state.random[0].nextDouble()*(input_ub - input_lb) + input_lb;
		}
		
	}
	
	public abstract boolean isduplicated(T newsym);
	
	public void addSymbol(T sym) {
		symbols.add(sym);
	}
	
	public abstract Object clone();
	
	public void set_tabu_frequency(double val) {
		tabu_frequency = val;
	}
	public double get_tabu_frequency() {
		return this.tabu_frequency;
	}
}
