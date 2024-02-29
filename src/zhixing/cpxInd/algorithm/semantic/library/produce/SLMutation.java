package zhixing.cpxInd.algorithm.semantic.library.produce;

import java.util.ArrayList;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPNodeSelector;
import ec.gp.GPTree;
import ec.gp.koza.GPKozaDefaults;
import ec.gp.koza.MutationPipeline;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.semantic.library.LibraryItem;
import zhixing.cpxInd.algorithm.semantic.library.SVSet;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;

public class SLMutation extends SLInsBreedingPipeline {
	
	private static final String P_SLMUTATION = "slmutation";

    public static final String P_NUM_TRIES = "tries";
    public static final String P_MAXDEPTH = "maxdepth";
    public static final String P_MINDEPTH = "mindepth";
    public static final String P_MAXSIZE = "maxsize";        
    public static final String P_MUTATION = "mutate";
    public static final String P_BUILDER = "build";
    public static final int INDS_PRODUCED = 1;
    public static final int NUM_SOURCES = 1;
    public static final int NO_SIZE_LIMIT = -1;
    
    public static final int NUM_PARENT = 1; //the necessary number of parents when breeding

	/** How the pipeline chooses a subtree to mutate */
    public GPNodeSelector nodeselect;

    /** How the pipeline builds a new subtree */
    public GPNodeBuilder builder;

    /** The number of times the pipeline tries to build a valid mutated
        tree before it gives up and just passes on the original */
    protected int numTries;
    
    /** The maximum depth of a mutated tree */
    protected int maxDepth;
    
    protected int minDepth;

    /** The largest tree (measured as a nodecount) the pipeline is allowed to form. */
    public int maxSize;
    
    public int numSources() { return NUM_SOURCES; }
	
	@Override
	public Parameter defaultBase() {
		
		return new Parameter(P_SLMUTATION);
	}
	
	@Override
	public Object clone()
    {
		SLMutation c = (SLMutation)(super.clone());

	    // deep-cloned stuff
	    c.nodeselect = (GPNodeSelector)(nodeselect.clone());
	
	    return c;
    }

	public void setup(final EvolutionState state, final Parameter base)
    {
	    super.setup(state,base);
	    
	    Parameter def = defaultBase();
	    Parameter p = base.push(P_NODESELECTOR).push(""+0);
	    Parameter d = def.push(P_NODESELECTOR).push(""+0);
	
	    nodeselect = (GPNodeSelector)
	        (state.parameters.getInstanceForParameter(
	            p,d, GPNodeSelector.class));
	    nodeselect.setup(state,p);
	
	    p = base.push(P_BUILDER).push(""+0);
	    d = def.push(P_BUILDER).push(""+0);
	
	    builder = (GPNodeBuilder)
	        (state.parameters.getInstanceForParameter(
	            p,d, GPNodeBuilder.class));
	    builder.setup(state,p);
	
	    numTries = state.parameters.getInt(
	        base.push(P_NUM_TRIES),def.push(P_NUM_TRIES),1);
	    if (numTries ==0)
	        state.output.fatal("Mutation Pipeline has an invalid number of tries (it must be >= 1).",base.push(P_NUM_TRIES),def.push(P_NUM_TRIES));
	
	    maxDepth = state.parameters.getInt(
	        base.push(P_MAXDEPTH),def.push(P_MAXDEPTH),1);
	    if (maxDepth==0)
	        state.output.fatal("The Mutation Pipeline " + base + "has an invalid maximum depth (it must be >= 1).",base.push(P_MAXDEPTH),def.push(P_MAXDEPTH));
	    
	    minDepth = state.parameters.getInt(
		        base.push(P_MINDEPTH),def.push(P_MINDEPTH),1);
		    if (minDepth==0)
		        state.output.fatal("The Mutation Pipeline " + base + "has an invalid maximum depth (it must be >= 1).",base.push(P_MINDEPTH),def.push(P_MINDEPTH));
	
	    maxSize = NO_SIZE_LIMIT;
	    if (state.parameters.exists(base.push(P_MAXSIZE), def.push(P_MAXSIZE)))
        {
        maxSize = state.parameters.getInt(base.push(P_MAXSIZE), def.push(P_MAXSIZE), 1);
        if (maxSize < 1)
            state.output.fatal("Maximum tree size, if defined, must be >= 1");
        }
	    
	    
    }
	
	public boolean verifyPoints(GPNode inner1, GPNode inner2)
    {
	    // We know they're swap-compatible since we generated inner1
	    // to be exactly that.  So don't bother.
	
	    // next check to see if inner1 can fit in inner2's spot
	    if (inner1.depth()+inner2.atDepth() > maxDepth || inner1.depth()+inner2.atDepth() < minDepth) return false;
	
	    // check for size
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
	
	    // checks done!
	    return true;
    }
	
	
	@Override
	public int getNumParents() {
		return NUM_PARENT;
	}
	
	@Override
	public LibraryItem produce(EvolutionState state, int thread, SemanticLibrary semlib) {
		// grab individuals from our semantic library.

		ArrayList<LibraryItem> parents = new ArrayList<>(getNumParents());
		
		for(int np = 0; np<getNumParents(); np++) {
			parents.add(sources[0].produce(state, thread, semlib));
		}

		return produce(state, thread, semlib, parents);
	}
	
	@Override
	public LibraryItem produce(EvolutionState state, int thread, SemanticLibrary semlib, ArrayList<LibraryItem> parents) {
		
		if(parents.size() != getNumParents()) {
			System.err.print("SLMutation got inconsistent number of parents\n");
			System.exit(1);
		}
		
		GPTreeStruct[] trial = new GPTreeStruct[semlib.getMaxCombine()];
		
		GPTreeStruct[] parent = parents.get(0).instructions;

		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		 // validity result...
        boolean res = false;
        
        // prepare the nodeselector
        nodeselect.reset();
        
        // pick a node
        GPTreeStruct oldtree = parents.get(0).instructions[0];
        GPNode p1=null;  // the node we pick
        GPNode p2=null;
        
        int c1 = 0;  //the index of GPTreeStruct in the first donor (a combination of GPTreeStruct)
        
        for(int x=0;x<numTries;x++)
            {
        	c1 = state.random[thread].nextInt(parents.get(0).instructions.length);
            // pick a node in individual 1
        	oldtree = parents.get(0).instructions[c1];
            p1 = nodeselect.pickNode(state,0,thread,null,oldtree);//========zhixing, 2021.3.26
            
            // generate a tree swap-compatible with p1's position
            
            
            int size = GPNodeBuilder.NOSIZEGIVEN;

            p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
                p1.parentType(initializer),
                thread,
                p1.parent,
                parents.get(0).instructions[c1].constraints(initializer).functionset,
                p1.argposition,
                size,
                p1.atDepth());//========zhixing, 2021.3.26
            
            // check for depth and swap-compatibility limits
            res = verifyPoints(p2,p1);  // p2 can fit in p1's spot  -- the order is important!
            
            // did we get something that had both nodes verified?
            if (res) break;
            }
        
        GPTreeStruct newtree;
        
        newtree = oldtree.lightClone();
        newtree.child = oldtree.child.cloneReplacingNoSubclone(p2,p1);
        newtree.child.parent = newtree;
        newtree.child.argposition = 0;
        
        //clone the parents
  		for(int l = 0; l<trial.length; l++) {
  			if(l==c1 && res) {
  				trial[l] = newtree;
  			}
  			else {
  				trial[l] = (GPTreeStruct) parent[l].clone();
  			}
  		}
  				
  		LibraryItem item = new LibraryItem(trial, semlib);
		
		return item;
	}

	

}
