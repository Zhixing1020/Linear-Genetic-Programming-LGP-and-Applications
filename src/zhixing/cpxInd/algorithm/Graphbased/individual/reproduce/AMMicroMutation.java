package zhixing.cpxInd.algorithm.Graphbased.individual.reproduce;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import ec.gp.GPType;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public abstract class AMMicroMutation extends zhixing.cpxInd.individual.reproduce.LGPMicroMutationPipeline{
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
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
        // how many individuals should we make?
		int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);
		
        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already



        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        for(int q=start, parnt = 0;q<n+start; q++)  // keep on going until we're filled up
            {
        	
        	LGPIndividual[] parnts = new LGPIndividual[2];
        	
            // grab two individuals from our sources
        	sources[0].produce(2,2,0,subpopulation,parnts,state,thread);
        	
            inds[q] = this.produce(min, max, start, subpopulation, state, thread, parnts);
            }
            
        return n;
        }
	
	public abstract LGPIndividual produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final EvolutionState state,
	        final int thread,
	        final LGPIndividual[] parents);
	
	protected int getFunIndexBasedAM(LGPIndividual4Graph ind, 
			int insert,
			double [][] AM, 
			EvolutionState state, 
			int thread,
			final GPType type,
	        final GPFunctionSet set) {
		int t = type.type;
	    GPNode[] functions = set.nonterminals[t];
	    //GPNode[] constants = set.constants[t];
	    
	    GPTreeStruct tree = ind.getTreeStruct(insert);
	    
	    GPNode root = tree.child;
	    
	    if (functions==null || functions.length ==0)                            // no nonterminals, hope the guy knows what he's doing!
        {    //functions = set.terminals[type.type];                                 // this can only happen with the warning about nonterminals above
        	System.err.print("cannot collect functions in AMCrossover\n");
        	System.exit(1);
        }
	    
	    int wr = ((WriteRegisterGPNode) root).getIndex();
		String name = null;
		for(int ins = insert+1;ins<ind.getTreesLength();ins++) {
			if(/*tree.status == */ ind.getTreeStruct(ins).status && ind.getTreeStruct(ins).collectReadRegister().contains(wr)){
				name = ind.getTreeStruct(ins).child.children[0].toString();
				break;
			}
		}
		int parentInd = -1; // state.random[thread].nextInt(functions.length);
		if(name != null) {
			for(int f = 0;f<functions.length;f++) {
    			if(functions[f].toString().equals(name)) {
    				parentInd = f;
    				break;
    			}
    		}
		}
		else{
			parentInd =state.random[thread].nextInt(functions.length);
		}
		int functionInd;
	    
	    functionInd = sampleBasedAM(parentInd, AM,  ind.getStartEnd_fun()[0], ind.getStartEnd_fun()[1], state, thread);
	    
	    return functionInd;
	}
	
	
	protected int sampleNextFunBasedAM(int parentInd, double [][]AM,  int dimension1, int dimension2, EvolutionState state, int thread) {
		//dimension1: number of functions, dimension2: number of fun + constant
		int res = state.random[thread].nextInt(dimension1);
		
		double fre []=new double [dimension1];
		for(int d = 0;d<dimension1;d++) {
			fre[d] = AM[parentInd][d];
		}
		
		double sum = 0;
		for(int i = 0;i<dimension1;i++) {
			sum+=fre[i];
		}
		for(int i = 0;i<dimension1;i++) {
			fre[i] /= sum;
		}
		
		double prob = state.random[thread].nextDouble();
		double tmp = 0;
		for(int f = 0;f<dimension1;f++) {
			tmp += fre[f];
			if(tmp>prob) {
				res = f;
				break;
			}
		}
		
		return res;
	}
	
	protected int sampleConsBasedAM(int parentInd, double [][]AM,  int dimension1, int dimension2, EvolutionState state, int thread) {
		int res = state.random[thread].nextInt(dimension2 - dimension1);
		
		double fre []=new double [dimension2 - dimension1];
		for(int d = dimension1;d<dimension2;d++) {
			fre[d-dimension1] = AM[parentInd][d];
		}
		
		double sum = 0;
		for(int i = 0;i<dimension2-dimension1;i++) {
			sum+=fre[i];
		}
		for(int i = 0;i<dimension2-dimension1;i++) {
			fre[i] /= sum;
		}
		
		double prob = state.random[thread].nextDouble();
		double tmp = 0;
		for(int f = 0;f<dimension2-dimension1;f++) {
			tmp += fre[f];
			if(tmp>prob) {
				res = f;
				break;
			}
		}
		
		return res;
	}
	
	protected int sampleBasedAM(int parentInd, double [][]AM,  int start, int end, EvolutionState state, int thread) {
		//start: index of start visiting, end: index of stop visiting
		int res = state.random[thread].nextInt(end - start);
		
		double fre []=new double [end - start];
		for(int d = start;d<end;d++) {
			fre[d-start] = AM[parentInd][d];
		}
		
		double sum = 0;
		for(int i = 0;i<end - start;i++) {
			sum+=fre[i];
		}
		for(int i = 0;i<end - start;i++) {
			fre[i] /= sum;
		}
		
		double prob = state.random[thread].nextDouble();
		double tmp = 0;
		for(int f = 0;f<end - start;f++) {
			tmp += fre[f];
			if(tmp>=prob) {
				res = f;
				break;
			}
		}
		
		return res;
	}
}
