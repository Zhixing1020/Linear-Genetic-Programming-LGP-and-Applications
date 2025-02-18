package zhixing.cpxInd.algorithm.LandscapeOptimization.indexing;

import java.util.ArrayList;

import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.LandscapeOptimization.SubpopulationFLO;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.Objective4FLO;
import zhixing.cpxInd.individual.CpxGPIndividual;

public abstract class IndexList<T> extends ArrayList<Index<T>>{
	
	public static final String INDEXLIST = "IndexList";
	public static final String BUILDER = "builder";
	public static final String ITEMPROTOTYPE = "itemprototype";
	public static final String NUMOBJECTIVES = "num_objectives";
	public static final String OBJECTIVES = "objectives";
	public static final String P_COEFFICIENCY = "coef";
	public static final String P_BOARDSIZE = "boardsize";
	
	public static final String NUMITERATIONS = "numiterations";
	public static final String P_STEP = "step";
	public static final String P_MINSTEP = "minstep";
	public static final String P_BATCHSIZE = "batchsize";
	
	public IndexSymbolBuilder<T> builder;
	
	protected Index<T> prototype;
	
	protected int numobjectives;
	protected Objective4FLO [] objectives;
	protected double [] coefficiency;
	protected int [] boardsize;
	
	protected static double [][] DiffNeighbor;
	
	protected static double [] usedItemHistory;
	
	protected int numiterations;  //the number of iterations when optimizing the indexes
	protected double step_rate;   //the percentage of a step on a certain index list
	protected double step = -1;		// the step length in gradient search
	protected double min_step;   //the minimum step length in gradient search
	protected int batchsize;   //the batch size when getting the gradients
	
	@SuppressWarnings("unchecked")
	public void setup(final EvolutionState state, final Parameter base) {
		
		Parameter def = new Parameter(INDEXLIST);
		
		builder = (IndexSymbolBuilder<T>) state.parameters.getInstanceForParameter(base.push(BUILDER), def.push(BUILDER), IndexSymbolBuilder.class);
		builder.setup(state, base.push(BUILDER));
		
		prototype = (Index<T>) state.parameters.getInstanceForParameter(base.push(ITEMPROTOTYPE), def.push(ITEMPROTOTYPE), Index.class);
		prototype.setup(state, base.push(ITEMPROTOTYPE));
		
		numobjectives = state.parameters.getInt(base.push(NUMOBJECTIVES), def.push(NUMOBJECTIVES), 1);
		if(numobjectives <=0) {
			state.output.fatal("the number of objectives for fitness landscape optimization must be at least 1", base.push(NUMOBJECTIVES), def.push(NUMOBJECTIVES));
		}
		objectives = new Objective4FLO[numobjectives];
		for(int obj = 0; obj < numobjectives; obj++) {
			objectives[obj] = (Objective4FLO) state.parameters.getInstanceForParameter(base.push(OBJECTIVES).push(""+obj), def.push(OBJECTIVES).push(""+obj), Objective4FLO.class);
		}
		
		coefficiency = new double [numobjectives];
		for(int obj = 0; obj < numobjectives; obj++) {
			coefficiency[obj] = state.parameters.getDoubleWithDefault(base.push(OBJECTIVES).push(""+obj).push(P_COEFFICIENCY), 
					def.push(OBJECTIVES).push(""+obj).push(P_COEFFICIENCY), 1.0);
			if(coefficiency[obj] <= 0) {
				state.output.fatal("the coefficiency of objectives for fitness landscape optimization must be larger than 0.0", base.push(OBJECTIVES).push(""+obj).push(P_COEFFICIENCY), 
						def.push(OBJECTIVES).push(""+obj).push(P_COEFFICIENCY));
			}
		}
		
		boardsize = new int [numobjectives];
		for(int obj = 0; obj < numobjectives; obj++) {
			boardsize[obj] = state.parameters.getIntWithDefault(base.push(OBJECTIVES).push(""+obj).push(P_BOARDSIZE), 
					def.push(OBJECTIVES).push(""+obj).push(P_BOARDSIZE), 10);
			if(boardsize[obj] <= 0) {
				state.output.fatal("the boardsize of objectives for fitness landscape optimization must be larger than 0.0", base.push(OBJECTIVES).push(""+obj).push(P_BOARDSIZE), 
						def.push(OBJECTIVES).push(""+obj).push(P_BOARDSIZE));
			}
		}
		
		numiterations = state.parameters.getInt(base.push(NUMITERATIONS), def.push(NUMITERATIONS), 1);
		if(numobjectives <=0) {
			state.output.fatal("the number of iterations for fitness landscape optimization must be at least 1", base.push(NUMITERATIONS), def.push(NUMITERATIONS));
		}
		
		step_rate = state.parameters.getDouble(base.push(P_STEP), def.push(P_STEP), 0.0);
		if(step_rate <= 0 || step_rate > 1) {
			state.output.fatal("the step for fitness landscape optimization must be larger than 0.0 and not larger than 1.0", base.push(P_STEP), def.push(P_STEP));
		}
		
		min_step = state.parameters.getDoubleWithDefault(base.push(P_MINSTEP), def.push(P_MINSTEP), 1.0);
		if(min_step <= 0) {
			state.output.fatal("the minimum step for fitness landscape optimization must be larger than 0.0", base.push(P_MINSTEP), def.push(P_MINSTEP));
		}
		
		batchsize = state.parameters.getIntWithDefault(base.push(P_BATCHSIZE), def.push(P_BATCHSIZE), 1);
		if(batchsize <= 0) {
			state.output.fatal("the batch size for fitness landscape optimization must be larger than 0.0", base.push(P_BATCHSIZE), def.push(P_BATCHSIZE));
		}
		
		initialize(state, 0);
	}

	public void initialize(final EvolutionState state, final int thread) {
		initializeDiffNeighbor(state, thread);
		
		step = Math.max(1.0, Math.round(step_rate * this.size()));
	}
	
	public ArrayList<T> getSymbolsByIndex(int index) {
		for(Index<T> it : this) {
			if(it.index == index) {
				return it.symbols;
			}
		}
		return null;
	}
	
	public T getSymbolByIndex(int index, final EvolutionState state, final int thread) {
		for(Index<T> it : this) {
			if(it.index == index) {				
				return it.symbols.get( state.random[thread].nextInt(it.symbols.size()) );
			}
		}
		
		System.err.println("The index list cannot find the index " + index);
		System.exit(1);
		
		return null;
	}
	
	public int getIndexBySymbol(T symbol) {
		for(Index<T> it : this) {
			for(T sym : it.symbols) {
				if(sym.toString().equals(symbol.toString())) {
					return it.index;
				}
			}
		}
		return -1;
	}

	public abstract GenoVector getGenoVector(CpxGPIndividual ind);
	
	public abstract void optimizeIndex(EvolutionState state, int thread, SubpopulationFLO subpop, Board board);
	
	public abstract IndexList cloneIndexList();
	
	public abstract double evaluateObjectives(IndexList list, Board board);
	
	protected abstract void initializeDiffNeighbor(EvolutionState state, int thread);
	
	public double [][] getDiffNeighbor(){
		return DiffNeighbor;
	}
	
	public void setDiffNeighbor(double [][] DN) {
		if(DN == null) return;
		this.DiffNeighbor = new double[DN.length][DN[0].length];
		for(int i = 0; i<DN.length;i++) {
			for(int j = i+1; j<DN.length; j++) {
				this.DiffNeighbor[i][j] = DN[i][j];
				this.DiffNeighbor[j][i] = DN[j][i];
			}
		}
	}
	
	
	protected static void shuffleIndex(EvolutionState state, int thread, ArrayList<Integer> list) {
		int n = list.size();
		for(int i = 0; i<n; i++) {
			int a = state.random[thread].nextInt(n);
			int b = state.random[thread].nextInt(n);
			if(a != b) {
				int tmp = list.get(a);
				list.set(a, list.get(b));
				list.set(b, tmp);
			}
		}
	}
}
