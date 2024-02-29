package zhixing.cpxInd.algorithm.semantic.library.produce;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeSelector;
import ec.gp.GPTree;
import ec.gp.koza.CrossoverPipeline;
import ec.gp.koza.GPKozaDefaults;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.semantic.library.LibraryItem;
import zhixing.cpxInd.algorithm.semantic.library.SVSet;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;


public class SLCrossover extends SLInsBreedingPipeline{
	
	public static final String P_NUM_TRIES = "tries";
    public static final String P_MAXDEPTH = "maxdepth";
    public static final String P_MINDEPTH = "mindepth";
    public static final String P_MAXSIZE = "maxsize";
    public static final String P_SLCROSSOVER = "slxover";
    public static final int INDS_PRODUCED = 1;
    public static final int NUM_PARENTS = 2;
    public static final int NUM_SOURCES = 2;
    public static final int NO_SIZE_LIMIT = -1;
    
    /** How the pipeline selects a node from individual 1 */
    public GPNodeSelector nodeselect1;

    /** How the pipeline selects a node from individual 2 */
    public GPNodeSelector nodeselect2;

    /** How many times the pipeline attempts to pick nodes until it gives up. */
    public int numTries;

    /** The deepest tree the pipeline is allowed to form.  Single terminal trees are depth 1. */
    public int maxDepth;
    
    public int minDepth;

    /** The largest tree (measured as a nodecount) the pipeline is allowed to form. */
    public int maxSize;
    
    public GPTreeStruct[] parents [];
    
    public Parameter defaultBase() { return new Parameter(P_SLCROSSOVER); }

    public int numSources() { return NUM_SOURCES; }

	@Override
	public int getNumParents() {
		return NUM_PARENTS;  //the number of parents is the same as the number of produced individual
	}

	
	public Object clone()
    {
	    SLCrossover c = (SLCrossover)(super.clone());
	
	    // deep-cloned stuff
	    c.nodeselect1 = (GPNodeSelector)(nodeselect1.clone());
	    c.nodeselect2 = (GPNodeSelector)(nodeselect2.clone());
	    c.parents = (GPTreeStruct[][]) parents.clone();
	
	    return c;
    }
	
	public void setup(final EvolutionState state, final Parameter base)
    {
	    super.setup(state,base);
	
	    Parameter def = defaultBase();
	    Parameter p = base.push(P_NODESELECTOR).push("0");
	    Parameter d = def.push(P_NODESELECTOR).push("0");
	
	    nodeselect1 = (GPNodeSelector)
	        (state.parameters.getInstanceForParameter(
	            p,d, GPNodeSelector.class));
	    nodeselect1.setup(state,d);
	
	    p = base.push(P_NODESELECTOR).push("1");
	    d = def.push(P_NODESELECTOR).push("1");
	
	    if (state.parameters.exists(p,d) &&
	        state.parameters.getString(p,d).equals(V_SAME))
	        // can't just copy it this time; the selectors
	        // use internal caches.  So we have to clone it no matter what
	        nodeselect2 = (GPNodeSelector)(nodeselect1.clone());
	    else
	        {
	        nodeselect2 = (GPNodeSelector)
	            (state.parameters.getInstanceForParameter(
	                p,d, GPNodeSelector.class));
	        nodeselect2.setup(state,p);
	        }
	
	    numTries = state.parameters.getInt(base.push(P_NUM_TRIES),
	        def.push(P_NUM_TRIES),1);
	    if (numTries == 0)
	        state.output.fatal("GPCrossover Pipeline has an invalid number of tries (it must be >= 1).",base.push(P_NUM_TRIES),def.push(P_NUM_TRIES));
	
	    maxDepth = state.parameters.getInt(base.push(P_MAXDEPTH),def.push(P_MAXDEPTH),1);
	    if (maxDepth==0)
	        state.output.fatal("GPCrossover Pipeline has an invalid maximum depth (it must be >= 1).",base.push(P_MAXDEPTH),def.push(P_MAXDEPTH));
	    
	    minDepth = state.parameters.getInt(base.push(P_MINDEPTH),def.push(P_MINDEPTH),1);
	    if (minDepth==0)
	        state.output.fatal("GPCrossover Pipeline has an invalid minimum depth (it must be >= 1).",base.push(P_MINDEPTH),def.push(P_MINDEPTH));
	
	    maxSize = NO_SIZE_LIMIT;
	    if (state.parameters.exists(base.push(P_MAXSIZE), def.push(P_MAXSIZE)))
	        {
	        maxSize = state.parameters.getInt(base.push(P_MAXSIZE), def.push(P_MAXSIZE), 1);
	        if (maxSize < 1)
	            state.output.fatal("Maximum tree size, if defined, must be >= 1");
	        }

    }
	
	public boolean verifyPoints(final GPInitializer initializer,
	        final GPNode inner1, final GPNode inner2)
	        {
	        // first check to see if inner1 is swap-compatible with inner2
	        // on a type basis
	        if (!inner1.swapCompatibleWith(initializer, inner2)) return false;

	        // next check to see if inner1 can fit in inner2's spot
	        if (inner1.depth()+inner2.atDepth() > maxDepth || inner1.depth() + inner2.atDepth() < minDepth) return false;

	        // check for size
	        // NOTE: this is done twice, which is more costly than it should be.  But
	        // on the other hand it allows us to toss a child without testing both times
	        // and it's simpler to have it all here in the verifyPoints code.  
	        if (maxSize != NO_SIZE_LIMIT)
	            {
	            // first easy check
	            int inner1size = inner1.numNodes(GPNode.NODESEARCH_ALL);
	            int inner2size = inner2.numNodes(GPNode.NODESEARCH_ALL);
	            if (inner1size > inner2size)  // need to test further
	                {
	                // let's keep on going for the more complex test
	                GPNode root2 = ((GPTree)(inner2.rootParent())).child;
	                int root2size = root2.numNodes(GPNode.NODESEARCH_ALL);
	                if (root2size - inner2size + inner1size > maxSize)  // take root2, remove inner2 and swap in inner1.  Is it still small enough?
	                    return false;
	                }
	            }
	        
	      //check the depth of the to-be-replaced node and the new node
	        //if only one of them is at depth 0, return false
	        if(inner1.atDepth() != inner2.atDepth() && (inner1.atDepth() == 0 || inner2.atDepth() == 0)) 
	        	return false;
	        
	        //check the ratio of constants, ensure that  inner1 (to replace others) has more or equal number of read registers than inner2 (to-be-repleaced)
	        //and has less or equal number of constants
	        int cnt1 = inner1.numNodes(GPNode.NODESEARCH_READREG);
	        int cnt2 = inner2.numNodes(GPNode.NODESEARCH_READREG);
	        int cnt11 = inner1.numNodes(GPNode.NODESEARCH_CONSTANT);
	        int cnt22 = inner2.numNodes(GPNode.NODESEARCH_CONSTANT);
	        if(cnt1 < cnt2 || cnt11 > cnt22 ) return false;

	        // checks done!
	        return true;
	        }
	
	@Override
	public LibraryItem produce(EvolutionState state, int thread, SemanticLibrary semlib) {
		
		ArrayList<LibraryItem> parents = new ArrayList<>(getNumParents());
		
		for(int np = 0; np<getNumParents(); np++) {
			parents.add(sources[0].produce(state, thread, semlib));
		}
		
		return produce(state, thread, semlib, parents);
	}
	@Override
	public LibraryItem produce(EvolutionState state, int thread, SemanticLibrary semlib,
			ArrayList<LibraryItem> parents) {
		
		if(parents.size() != getNumParents()) {
			System.err.print("SLCrossover got inconsistent number of parents\n");
			System.exit(1);
		}
		
		GPTreeStruct[] trial = new GPTreeStruct[semlib.getMaxCombine()];

		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		int t1=0; //the index of GPTreeStruct in the first donor (a combination of GPTreeStruct)
		int t2=0;  //the index of GPTreeStruct in the second donor (a combination of GPTreeStruct)
		
		GPTreeStruct[] parent1 = parents.get(0).instructions;
		GPTreeStruct[] parent2 = parents.get(1).instructions;
		
		t1 = state.random[thread].nextInt(parent1.length);
		t2 = state.random[thread].nextInt(parent2.length);
		
		if(parent1[t1].constraints(initializer) != parent2[t2].constraints(initializer) ) {
			if(parent1.length == 1 && parent2.length == 1) {
				System.err.print("SLCrossover cannot find consistent instruction (GPTreeStruct) with the same initializer.\n");
				System.exit(1);
			}
			else {
				for(int a = 0; a<numTries; a++) {
					t1 = state.random[thread].nextInt(parent1.length);
					t2 = state.random[thread].nextInt(parent2.length);
					if(parent1[t1].constraints(initializer) != parent2[t2].constraints(initializer) ) break;
				}
			}
		}
		if(parent1[t1].constraints(initializer) != parent2[t2].constraints(initializer) ) {
			System.err.print("SLCrossover cannot find consistent instruction (GPTreeStruct) with the same initializer.\n");
			System.exit(1);
		}
		
		// validity results...
        boolean res1 = false;
        boolean res2 = false;
        
        
        // prepare the nodeselectors
        nodeselect1.reset();
        nodeselect2.reset();
        
        
        // pick some nodes
        
        GPNode p1=null;
        GPNode p2=null;
        
        for(int x=0;x<numTries;x++)
        {
            // pick a node in individual 1
            p1 = nodeselect1.pickNode(state,0,thread,null,parent1[t1]);
            
            // pick a node in individual 2
            p2 = nodeselect2.pickNode(state,0,thread,null,parent2[t2]);
            
            // check for depth and swap-compatibility limits
            res1 = verifyPoints(initializer,p2,p1);  // p2 can fill p1's spot -- order is important!
            //res2 = verifyPoints(initializer,p1,p2);  // p1 can fill p2's spot -- order is important!
            
            // did we get something that had both nodes verified?
            // we reject if EITHER of them is invalid.  This is what lil-gp does.
            // Koza only has numTries set to 1, so it's compatible as well.
            //if (res1 && res2) break;
            if (res1) break;
        }
        
        GPTreeStruct newtree;
        
        newtree = parent1[t1].lightClone();
        newtree.child = parent1[t1].child.cloneReplacing(p2,p1);
        newtree.child.parent = newtree;
        newtree.child.argposition = 0;
		
      //clone the parents
  		for(int l = 0; l<trial.length; l++) {
  			if(l==t1 && res1) {
  				trial[l] = newtree;
  			}
  			else {
  				trial[l] = (GPTreeStruct) parent1[l].clone();
  			}
  		}
		
  		LibraryItem item = new LibraryItem(trial, semlib);
		
		return item;
	}
	
}
