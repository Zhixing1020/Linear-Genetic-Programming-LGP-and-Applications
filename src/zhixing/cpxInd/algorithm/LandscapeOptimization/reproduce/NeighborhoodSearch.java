package zhixing.cpxInd.algorithm.LandscapeOptimization.reproduce;

import java.util.ArrayList;

import org.spiderland.Psh.booleanStack;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPBreedingPipeline;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.LandscapeOptimization.SubpopulationFLO;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.BoardItem;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Direction;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Index;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexList;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.norm2Q;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.LGPIndividual;

public abstract class NeighborhoodSearch extends GPBreedingPipeline{

	public static final String P_NEIGHBORHOODSEARCH = "nbhsearch";
	public static final String P_MASKLENGTH = "mask_length"; //the maximum mask size: the maximum number of genotype elements can be changed each time
	public static final String P_MACROSIZE = "macro_size";
	public static final String P_MAXSTEP = "maxstep";
	public static final String P_NUM_TRIES = "tries";
	public static final String P_INSERT = "prob_insert";
	public static final String P_DELETE = "prob_delete";
	
	public static final int INDS_PRODUCED = 1;
    public static final int NUM_SOURCES = 2;
    
    public static final double default_step = 1;
    public static final double default_cosLimit = 0; // the maximum angles between the sampled moving directions and the target direction, represented by cosine
	
	public int maxMaskSize;
	public int maxMacroSize;
	public double maxStep;
	protected int numTries;
	
	protected ArrayList<Short> mask = null;
	protected int masklength;
	
	protected int master_i;
	
	protected int cur_generation = -1;
	protected Board leadBoard = null;
	
	protected double addRate;
	protected double removeRate;
	
	public CpxGPIndividual parents[] = new CpxGPIndividual [NUM_SOURCES];
	
	@Override
	public Parameter defaultBase() {
		return new Parameter(P_NEIGHBORHOODSEARCH);
	}

	@Override
	public int numSources() {
		return NUM_SOURCES;
	}
	
	public Object clone()
    {
	    NeighborhoodSearch c = (NeighborhoodSearch)(super.clone());
	
	    // deep-cloned stuff
	    c.parents = (CpxGPIndividual[]) parents.clone();
	
	    return c;
    }
	
	public void setup(final EvolutionState state, final Parameter base)
    {
	    super.setup(state,base);
	    
	    Parameter def = defaultBase();
	
	    maxMaskSize = state.parameters.getInt(
		        base.push(P_MASKLENGTH),def.push(P_MASKLENGTH),1);
	    if(maxMaskSize < 1) {
	    	 state.output.fatal("NeighborhoodSearch has an invalid max mask size (it must be >= 1).",base.push(P_MASKLENGTH),def.push(P_MASKLENGTH));
	    }
	    
	    maxMacroSize = state.parameters.getInt(
		        base.push(P_MACROSIZE),def.push(P_MACROSIZE),1);
	    if(maxMacroSize < 1) {
	    	 state.output.fatal("NeighborhoodSearch has an invalid max macro size (it must be >= 1).",base.push(P_MACROSIZE),def.push(P_MACROSIZE));
	    }
	    
	    maxStep = state.parameters.getDoubleWithDefault(base.push(P_MAXSTEP),def.push(P_MAXSTEP),0.2);
	    if(maxStep <= 0) {
	    	 state.output.fatal("NeighborhoodSearch has an invalid max step (it must be  in (0, 1]).",base.push(P_MAXSTEP),def.push(P_MAXSTEP));
	    }
	    
	    numTries = state.parameters.getIntWithDefault(
	        base.push(P_NUM_TRIES),def.push(P_NUM_TRIES),20);
	    if (numTries ==0)
	        state.output.fatal("NeighborhoodSearch has an invalid number of tries (it must be >= 1).",base.push(P_NUM_TRIES),def.push(P_NUM_TRIES));
	
	    addRate = state.parameters.getDouble(base.push(P_INSERT),def.push(P_INSERT));
	    if(addRate < 0 || addRate > 1) {
	    	state.output.fatal("the probability of adding symbols in NeighborhoodSearch must be [0,1].", base.push(P_INSERT),def.push(P_INSERT));
	    }
	    
	    removeRate = state.parameters.getDouble(base.push(P_DELETE),def.push(P_DELETE));
	    if(removeRate < 0 || removeRate > 1) {
	    	state.output.fatal("the probability of removing symbols in NeighborhoodSearch must be [0,1].", base.push(P_DELETE),def.push(P_DELETE));
	    }
    }

	@Override
    public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) 

        {
		if(! (state.population.subpops[subpopulation] instanceof SubpopulationFLO)) {
			state.output.fatal("NeighborhoodSearch does not support other subpopulation types except SubpopulationFLO");
		}
		
        // how many individuals should we make?
        int n = INDS_PRODUCED;
        
        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

        for(int q=start;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
            // grab two individuals from our sources
            if (sources[0]==sources[1])  // grab from the same source
                sources[0].produce(2,2,0,subpopulation,(Individual[]) parents,state,thread);
            else // grab from different sources
                {
                sources[0].produce(1,1,0,subpopulation,(Individual[]) parents,state,thread);
                sources[1].produce(1,1,1,subpopulation,(Individual[]) parents,state,thread);
                }
//            
//            sources[0].produce(1,1,0,subpopulation,parents,state,thread);
            // at this point, parents[] contains our two selected individuals
         // at this point, parents[] contains our two selected individuals
            CpxGPIndividual[] parnts = new CpxGPIndividual[NUM_SOURCES];
        	for(int ind = 0 ; ind < parnts.length; ind++){
        		parnts[ind] = (CpxGPIndividual) this.parents[ind]; 
        		
        	}
        	
            q += this.produce(min, max, start, subpopulation, inds, state, thread, (Individual[]) parnts);
            
            }
        return n;
        }

	public abstract int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread,
	        final Individual[] parents);
	
	public GenoVector updateGenoVector(final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread,
	        final Individual[] parents) {
		Board board = ((SubpopulationFLO)state.population.subpops[subpopulation]).fullBoard;
		IndexList indexlist = ((SubpopulationFLO)state.population.subpops[subpopulation]).IndList;
		
		//get the maximum angles (minimum cosine distance), get the maximum step
		if(leadBoard == null || cur_generation != state.generation) {
			leadBoard = board.lightTrimCloneByBest();
//			leadBoard.trim2MaxsizeByBest();
			leadBoard.resetGenoAngle(indexlist, state.generation);
			leadBoard.resetGenoDifference(indexlist, state.generation);
			
			cur_generation = state.generation;
		}
		
		double mincos = leadBoard.getAvgCosine();
		double maxstep = leadBoard.getAvgDiff();
		
		
		//get the genotype vector of parents
		GenoVector pgSlaver, pgMaster, pgSource; 
		master_i = 0;
		if(parents[0].fitness.betterThan(parents[1].fitness)) {
			pgMaster = indexlist.getGenoVector((CpxGPIndividual) parents[master_i]);
			pgSlaver = indexlist.getGenoVector((CpxGPIndividual) parents[(master_i + 1)%parents.length]);
			pgSource = (GenoVector) pgMaster.clone();
		}
		else {
			pgMaster = indexlist.getGenoVector((CpxGPIndividual) parents[(master_i + 1)%parents.length]);
			pgSlaver = indexlist.getGenoVector((CpxGPIndividual) parents[master_i]);
			pgSource = (GenoVector) pgSlaver.clone();
		}

		//set the mask for pgMaster
		setMask(state, thread, subpopulation, pgSource);
		
		//update the genotype vector
		Direction direction = randomlySetAngle(state, thread, pgMaster, pgSlaver, mincos);
		
		double step = state.random[thread].nextDouble()*(maxstep) + 1;
		
		pgSource.moveOnDirection(direction, step, mask, state, thread, indexlist.size());
		
		return pgSource;
	}
	
	protected abstract boolean maintainPhenotype(EvolutionState state, int thread, LGPIndividual oldind, LGPIndividual newind, GenoVector newgv);
	
	protected void resetMask(final EvolutionState state, final int thread, final int subpopulation) {
//		Board board = ((SubpopulationFLO)state.population.subpops[subpopulation]).fullBoard;
		IndexList indexlist = ((SubpopulationFLO)state.population.subpops[subpopulation]).IndList;
		
		masklength =  indexlist.getGenoVector((CpxGPIndividual) state.population.subpops[subpopulation].individuals[0]).length;
		
		if(mask == null) mask = new ArrayList<>();
		else mask.clear();
//		else {
//			for(int m = 0; m<mask.length; m++)
//				mask[m] = 0;
//		}
	}
	
	protected void setMask(final EvolutionState state, final int thread, final int subpopulation, final GenoVector pgMaster) {
		
		resetMask(state, thread, subpopulation);
		
		int progLength = 0;
		for(int ef = 0; ef<pgMaster.length; ef++) {
			if(pgMaster.G[ef] >= 0) {
				progLength ++;
			}
			else {
				break;
			}
		}

		int mask_size = 1 + state.random[thread].nextInt(Math.min(maxMaskSize, progLength));
		
//		for(int mk = mask_start; mk < mask_start + mask_size; mk++) {
//			mask.add((short) mk);
//		}
		for(int i = 0; i<mask_size; i++) {
			mask.add((short) state.random[thread].nextInt(progLength));
		}
	}
	
	protected Direction randomlySetAngle(EvolutionState state, int thread, GenoVector pgDestination, GenoVector pgSource, double mincosine) {
		ArrayList<GenoVector> candidate = new ArrayList<>();
		candidate.add(pgDestination);
		double maxbias = 10;
		
		//prepare some elements
		GenoVector Ge0 = (GenoVector) pgDestination.clone();
		//replace the overlapped element between pgDestinaton and pgSource as those in pgSource since the cosine distance only cares the overlap part
		for(int g = 0; g<pgSource.length; g++) {
			if(Ge0.G[g] >= 0 && pgSource.G[g] >= 0) {
				Ge0.G[g] = pgSource.G[g];
			}
			else break;
				
		}
		
		ArrayList<Integer> candidateElement = new ArrayList<>();
		ArrayList<Integer> selectedElement = new ArrayList<>();
		for(int d = 0; d<Ge0.length; d++) {
			if(Ge0.G[d] >= 0) {
				candidateElement.add(d); 
			}
		}
		
		for(int tr = 0; tr<numTries; tr++) {
			selectedElement.clear();
			
			GenoVector Ge = (GenoVector) Ge0.clone();
			
			//produce new direction, 1) randomly choose multiple elements, 2) bias the elements
			int NumElements = state.random[thread].nextInt(candidateElement.size()) + 1;
			int element;
			for(int e = 0; e<NumElements; e++) {
				int element_ind = (state.random[thread].nextInt(candidateElement.size()));
				element = candidateElement.get(element_ind);
				if(selectedElement.contains(element)) {
					for(int c = 1; c<candidateElement.size() - 1; c++) {
						element = candidateElement.get( (element_ind + c)%candidateElement.size());
						if(!selectedElement.contains(element)){
							break;
						}
					}
				}
				selectedElement.add( element );
			}
			for(Integer se : selectedElement) {
				Ge.G[se] += Math.floor( Math.pow(-1., state.random[thread].nextInt()) * state.random[thread].nextDouble()*maxbias ); 
			}
			
			//validate the new direction
			double cosdis = Direction.Cosine_direction(pgDestination, pgSource, Ge, pgSource, state, thread);
			if(cosdis >= mincosine) {
				candidate.add(Ge);
			}
			
		}
		
		//randomly pick a candidate to get the final direction
		GenoVector Gfinal = candidate.get(state.random[thread].nextInt(candidate.size()));
		
		Direction dir = new Direction();
		dir.setDirection(Gfinal, pgSource, state, thread);
		
		double rate = state.random[thread].nextDouble();
		if(rate <= addRate) {
			dir.sizeDirection = 1;
		}
		else if (rate <= addRate + removeRate) {
			dir.sizeDirection = -1;
		}
		
		return dir;
	}
	
}
