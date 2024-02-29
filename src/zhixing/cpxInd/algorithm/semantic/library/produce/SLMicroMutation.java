package zhixing.cpxInd.algorithm.semantic.library.produce;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeSelector;
import ec.gp.GPType;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.semantic.library.LibraryItem;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;

public class SLMicroMutation extends SLInsBreedingPipeline{
	
	private static final String P_SLMICROMUTATION = "slmicromutation";

    public static final String P_NUM_TRIES = "tries";
	
    public static final int INDS_PRODUCED = 1;
    public static final int NUM_SOURCES = 1;
    public static final int NO_SIZE_LIMIT = -1;
    
    public static final int NUM_PARENT = 1; //the necessary number of parents when breeding
    
    /** How the pipeline chooses a subtree to mutate */
    public GPNodeSelector nodeselect;

    /** The number of times the pipeline tries to build a valid mutated
    tree before it gives up and just passes on the original */
    protected int numTries = 1;
    
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
	
//	    numTries = state.parameters.getInt(
//	        base.push(P_NUM_TRIES),def.push(P_NUM_TRIES),1);
//	    if (numTries ==0)
//	        state.output.fatal("Micro Mutation Pipeline of semantic library has an invalid number of tries (it must be >= 1).",base.push(P_NUM_TRIES),def.push(P_NUM_TRIES));
	    
    }

	private GPNode pickCompatibleNode(final GPNode original, final GPFunctionSet set, final EvolutionState state, final GPType returntype, final int thread)
    {
		
    // an expensive procedure: we will linearly search for a valid node
    int numValidNodes = 0;
    
    int type = returntype.type;
    GPInitializer initializer = ((GPInitializer)state.initializer);
    int len = original.constraints(initializer).childtypes.length;
    boolean failed;

    if (initializer.numAtomicTypes + initializer.numSetTypes == 1)  // easy
        numValidNodes = set.nodesByArity[type][len].length;
    else for(int x=0;x<set.nodesByArity[type][len].length;x++) // ugh, the hard way -- nodes swap-compatible with type, and of arity len
             {
             failed = false;
             for(int y=0;y<set.nodesByArity[type][len][x].constraints(initializer).childtypes.length;y++)
                 if (!set.nodesByArity[type][len][x].constraints(initializer).
                     childtypes[y].compatibleWith(initializer,original.children[y].
                         constraints(initializer).returntype))
                     { failed = true; break; }
             if (!failed) numValidNodes++;
             }
    
    // we must have at least success -- the node itself.  Otherwise we're
    // in deep doo-doo.

    // now pick a random node number
    int nodenum = state.random[thread].nextInt(numValidNodes);

    // find and return that node
    int prosnode = 0;
    
    if (numValidNodes == set.nodesByArity[type][len].length) // easy
        return set.nodesByArity[type][len][nodenum];
    else for(int x=0;x<set.nodesByArity[type][len].length;x++) // ugh, the hard way -- nodes swap-compatible with type, and of arity len
             {
             failed = false;
             for(int y=0;y<set.nodesByArity[type][len][x].constraints(initializer).childtypes.length;y++)
                 if (!set.nodesByArity[type][len][x].constraints(initializer).
                     childtypes[y].compatibleWith(initializer,original.children[y].
                         constraints(initializer).returntype))
                     { failed = true; break; }
             if (!failed) 
                 {
                 if (prosnode == nodenum)  // got it!
                     return set.nodesByArity[type][len][x];
                 prosnode++;
                 }
             }

    // should never be able to get here
    throw new InternalError();  // whoops!
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
            
            
//            int size = GPNodeBuilder.NOSIZEGIVEN;
            
            GPType type;
            type = p1.parentType(initializer);
            
            p2 = (GPNode)(pickCompatibleNode(p1,oldtree.constraints(initializer).functionset,state,type,thread)).lightClone();//========zhixing, 2021.3.26

            // if it's an ERC, let it set itself up
            p2.resetNode(state,thread);

//            p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
//                p1.parentType(initializer),
//                thread,
//                p1.parent,
//                parents.get(0).instructions[c1].constraints(initializer).functionset,
//                p1.argposition,
//                size,
//                p1.atDepth());//========zhixing, 2021.3.26
            
            // check for depth and swap-compatibility limits
//            res = verifyPoints(p2,p1);  // p2 can fit in p1's spot  -- the order is important!
            
            // did we get something that had both nodes verified?
            if (true) break;
            }
        
        GPTreeStruct newtree;
        
        newtree = oldtree.lightClone();
        newtree.child = oldtree.child.cloneReplacingAtomic(p2,p1);
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

	@Override
	public int getNumParents() {
		return NUM_PARENT;
	}

	@Override
	public int numSources() { return NUM_SOURCES; }

	@Override
public Parameter defaultBase() {
		
		return new Parameter(P_SLMICROMUTATION);
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
}
