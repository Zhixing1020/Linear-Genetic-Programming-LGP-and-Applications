package zhixing.cpxInd.individual.reproduce;

import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.koza.GPKozaDefaults;
import ec.gp.koza.KozaNodeSelector;
import ec.util.Parameter;

public class LGPNodeSelector extends KozaNodeSelector {
	
	public static final String P_CONSTANT_PROB = "constants";
	public static final String P_READREGISTER_PROB = "read_registers";
	
	public double constantProbability;
	
	public double readRegProbability;
	
	public int constants;
	
	public int readregs;
	
	public LGPNodeSelector() {
		reset();
	}
	
	public Parameter defaultBase()
    {
		Parameter pp = new Parameter("lgp");
		return pp.push(P_NODESELECTOR);
    }
	
	public void setup(final EvolutionState state, final Parameter base)
    {
	    Parameter def = defaultBase();
	
	    constantProbability = state.parameters.getDoubleWithMax(
	        base.push(P_CONSTANT_PROB),
	        def.push(P_CONSTANT_PROB), 0.0, 1.0);
	    if (constantProbability==-1.0)
	        state.output.fatal("Invalid constant probability for LGPNodeSelector ",
	            base.push(P_CONSTANT_PROB),
	            def.push(P_CONSTANT_PROB));
	    
	    readRegProbability = state.parameters.getDoubleWithMax(
	        base.push(P_READREGISTER_PROB), 
	        def.push(P_READREGISTER_PROB),0.0, 1.0);
	    if (readRegProbability==-1.0)
	        state.output.fatal("Invalid nonterminal probability for KozaNodeSelector ",
	            base.push(P_READREGISTER_PROB), 
	            def.push(P_READREGISTER_PROB));
	    
	    nonterminalProbability = state.parameters.getDoubleWithMax(
	            base.push(P_NONTERMINAL_PROBABILITY), 
	            def.push(P_NONTERMINAL_PROBABILITY),0.0, 1.0);
	        if (nonterminalProbability==-1.0)
	            state.output.fatal("Invalid nonterminal probability for KozaNodeSelector ",
	                base.push(P_NONTERMINAL_PROBABILITY), 
	                def.push(P_NONTERMINAL_PROBABILITY));

        rootProbability = state.parameters.getDoubleWithMax(
            base.push(P_ROOT_PROBABILITY),
            def.push(P_ROOT_PROBABILITY),0.0, 1.0);
        if (rootProbability==-1.0)
            state.output.fatal("Invalid root probability for KozaNodeSelector ",
                base.push(P_ROOT_PROBABILITY),
                def.push(P_ROOT_PROBABILITY));
        
        terminalProbability = constantProbability + readRegProbability;
	    
	    if (rootProbability+nonterminalProbability+readRegProbability+constantProbability > 1.0f)
	        state.output.fatal("The nonterminal, constant, readregister and root for LGPNodeSelector" + 
	    base + " may not sum to more than 1.0. (" + nonterminalProbability + " " + rootProbability + " " 
	        		+ constantProbability + " " + readRegProbability + ")",base);
	
	    reset();
    }
	
	public void reset()
    {
		nonterminals = nodes = constants = readregs = -1;
    }
	
	@Override
	public GPNode pickNode(final EvolutionState s,
        final int subpopulation,
        final int thread,
        final GPIndividual ind,
        final GPTree tree)
        {
        double rnd = s.random[thread].nextDouble();
        
        if (rnd > nonterminalProbability + constantProbability+ readRegProbability + rootProbability )  // pick anyone
            {
            if (nodes==-1) nodes=tree.child.numNodes(GPNode.NODESEARCH_ALL);
                    {
                    return tree.child.nodeInPosition(s.random[thread].nextInt(nodes), GPNode.NODESEARCH_ALL);
                    }
            }
        else if (rnd > nonterminalProbability + constantProbability+ readRegProbability)  // pick the root
            {
            return tree.child;
            }
        else if(rnd > nonterminalProbability + readRegProbability) { //pick constant if the instruction contains constants
        	if (constants==-1) constants = tree.child.numNodes(GPNode.NODESEARCH_CONSTANT);
        	if(constants > 0)
        		return tree.child.nodeInPosition(s.random[thread].nextInt(constants), GPNode.NODESEARCH_CONSTANT);
        	else {
        		if (readregs==-1) readregs = tree.child.numNodes(GPNode.NODESEARCH_READREG);
                return tree.child.nodeInPosition(s.random[thread].nextInt(readregs), GPNode.NODESEARCH_READREG);
        	}
        }
        else if (rnd > nonterminalProbability)  // pick read registers
            {
            if (readregs==-1) readregs = tree.child.numNodes(GPNode.NODESEARCH_READREG);
            if(readregs > 0)
            	return tree.child.nodeInPosition(s.random[thread].nextInt(readregs), GPNode.NODESEARCH_READREG);
            else{
            	constants = tree.child.numNodes(GPNode.NODESEARCH_CONSTANT);
            	return tree.child.nodeInPosition(s.random[thread].nextInt(constants), GPNode.NODESEARCH_CONSTANT);
            	//return tree.child; //there are no read registers. so return the root node as default
            }
            }
        else  // pick nonterminals if you can
            {
            if (nonterminals==-1) nonterminals = tree.child.numNodes(GPNode.NODESEARCH_NONTERMINALS);
            if (nonterminals > 0) // there are some nonterminals
                {
                return tree.child.nodeInPosition(s.random[thread].nextInt(nonterminals), GPNode.NODESEARCH_NONTERMINALS);
                }
            else // there ARE no nonterminals!  It must be the root node
                {
                return tree.child;
                }
            }
        }
}
