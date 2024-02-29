package zhixing.djss.algorithm.GrammarTGP.individual;

import java.util.ArrayList;
import java.util.HashSet;

import ec.EvolutionState;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeParent;
import ec.gp.GPType;
import ec.gp.koza.KozaBuilder;
import yimei.jss.gp.terminal.TerminalERC;
import yimei.jss.gp.terminal.TerminalERCUniform;
import zhixing.cpxInd.algorithm.Grammar.individual.primitives.NumericalValue;
import zhixing.cpxInd.algorithm.GrammarTGP.individual.primitives.BranchingTGP;

public abstract class KozaBuilder4IF4DJSS extends KozaBuilder{

	@Override
	protected GPNode fullNode(final EvolutionState state,
	        final int current,
	        final int max,
	        final GPType type,
	        final int thread,
	        final GPNodeParent parent,
	        final int argposition,
	        final GPFunctionSet set) {
		// fullNode can mess up if there are no available terminal for a given type.  If this occurs,
        // and we find ourselves unable to pick a terminal when we want to do so, we will issue a warning,
        // and pick a nonterminal, violating the "FULL" contract.  This can lead to pathological situations
        // where the system will continue to go on and on unable to stop because it can't pick a terminal,
        // resulting in running out of memory or some such.  But there are cases where we'd want to let
        // this work itself out.
        boolean triedTerminals = false;   // did we try -- and fail -- to fetch a terminal?
        
        int t = type.type;
        
        GPNode[] terminals = new GPNode [1]; //we directly separate input features and constants here, which is a stupid way
        GPNode[] constants = new GPNode [1];
        for(int i = 0; i<set.terminals[t].length; i++) {
        	if(set.terminals[t][i] instanceof TerminalERCUniform) {
        		terminals[0] = set.terminals[t][i];
        	}
        	if(set.constants[t][i] instanceof NumericalValue) {
        		constants[0] = set.terminals[t][i];
        	}
        }
//        GPNode[] terminals = set.terminals[t];
        GPNode[] nonterminals = set.nonterminals[t];
//        GPNode[] constants = set.constants[t];
        GPNode[] flowoperators = set.flowoperators[t];
        GPNode[] nodes = set.nodes[t];          

        if (nodes.length == 0)
            errorAboutNoNodeWithType(type, state);   // total failure

        // pick a terminal when we're at max depth 
        // or if there are NO nonterminals
        // or it is the first argument of IF primitives
        if((  current+1 >= max                                                       // Now pick if we're at max depth
        		|| (parent instanceof BranchingTGP && argposition == 0)
        		|| warnAboutNonterminal(nonterminals.length==0, type, false, state)) &&     // OR if there are NO nonterminals!
            // this will freak out the static checkers
            (triedTerminals = true) &&                                                  // [first set triedTerminals]
            terminals.length != 0) 
        	{
        	GPNode n;
        	if(parent instanceof BranchingTGP && argposition == 1)
        		n = (GPNode)(constants[state.random[thread].nextInt(constants.length)].lightClone());
        	else
        		n = (GPNode)(terminals[state.random[thread].nextInt(terminals.length)].lightClone());
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;
            return n;
        }
        else if(parent instanceof BranchingTGP && argposition == 1) {
        	GPNode n = (GPNode)(constants[state.random[thread].nextInt(constants.length)].lightClone());
        	n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;
            return n;
        }
        else if(parent instanceof BranchingTGP && argposition == 0) {
        	GPNode n = (GPNode)(terminals[state.random[thread].nextInt(terminals.length)].lightClone());
        	n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;
            return n;
        }
        else if(current == 0) {
        	if (triedTerminals) warnAboutNoTerminalWithType(type, false, state);        // we tried terminal and we're here because there were none!
            
            GPNode[] nodesToPick = set.nonterminals[type.type];
            if (nodesToPick==null || nodesToPick.length ==0)                            // no nonterminals, hope the guy knows what he's doing!
                nodesToPick = set.terminals[type.type];                                 // this can only happen with the warning about nonterminals above

            GPNode n = (GPNode)(nodesToPick[state.random[thread].nextInt(nodesToPick.length)].lightClone());
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;

            // Populate the node...
            GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
            for(int x=0;x<childtypes.length;x++)
                n.children[x] = fullNode(state,current+1,max,childtypes[x],thread,n,x,set);

            return n;
        }
        else if((!( parent instanceof BranchingTGP))) { //can only select arithmetic function nodes
        	if (triedTerminals) warnAboutNoTerminalWithType(type, false, state);        // we tried terminal and we're here because there were none!
            
            GPNode[] nodesToPick = set.nonterminals[type.type];
            GPNode[] nodesNotToPick = set.flowoperators[type.type];
            
            ArrayList<Integer> arithmeticFunctions = new ArrayList<>();
            for(int i = 0; i<set.nonterminals[type.type].length; i++) {
            	boolean exist = false;
            	for(int j = 0; j<set.flowoperators[type.type].length; j++) {
            		if(set.nonterminals[type.type][i].toString().equals(set.flowoperators[type.type][j].toString())) {
            			exist = true;
            			break;
            		}
            	}
            	if(!exist) {
            		arithmeticFunctions.add(i);
            	}
            }
            
            if (nodesToPick==null || nodesToPick.length ==0 || arithmeticFunctions.size()==0)                            // no nonterminals, hope the guy knows what he's doing!
                nodesToPick = set.terminals[type.type];                                 // this can only happen with the warning about nonterminals above

            int pick = arithmeticFunctions.get(state.random[thread].nextInt(arithmeticFunctions.size())); 
            GPNode n = (GPNode)(nodesToPick[pick].lightClone());
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;

            // Populate the node...
            GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
            for(int x=0;x<childtypes.length;x++)
                n.children[x] = fullNode(state,current+1,max,childtypes[x],thread,n,x,set);

            return n;
        }
        // else force a nonterminal unless we have no choice
        else 
//        	if(parent instanceof BranchingTGP && argposition > 1 || current == 0)  //can select any function or terminal nodes when the parent is branching
            {
            if (triedTerminals) warnAboutNoTerminalWithType(type, false, state);        // we tried terminal and we're here because there were none!
                                
            GPNode[] nodesToPick = set.nonterminals[type.type];
            if (nodesToPick==null || nodesToPick.length ==0)                            // no nonterminals, hope the guy knows what he's doing!
                nodesToPick = set.terminals[type.type];                                 // this can only happen with the warning about nonterminals above

            GPNode n = (GPNode)(nodesToPick[state.random[thread].nextInt(nodesToPick.length)].lightClone());
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;

            // Populate the node...
            GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
            for(int x=0;x<childtypes.length;x++)
                n.children[x] = fullNode(state,current+1,max,childtypes[x],thread,n,x,set);

            return n;
            }
        	
        
	}
	
	protected GPNode growNode(final EvolutionState state,
	        final int current,
	        final int max,
	        final GPType type,
	        final int thread,
	        final GPNodeParent parent,
	        final int argposition,
	        final GPFunctionSet set) 
	  {
	        // growNode can mess up if there are no available terminal for a given type.  If this occurs,
	        // and we find ourselves unable to pick a terminal when we want to do so, we will issue a warning,
	        // and pick a nonterminal, violating the maximum-depth contract.  This can lead to pathological situations
	        // where the system will continue to go on and on unable to stop because it can't pick a terminal,
	        // resulting in running out of memory or some such.  But there are cases where we'd want to let
	        // this work itself out.
	        boolean triedTerminals = false;

	        int t = type.type;
	        
	        GPNode[] terminals = new GPNode [1]; //we directly separate input features and constants here, which is a stupid way
	        GPNode[] constants = new GPNode [1];
	        for(int i = 0; i<set.terminals[t].length; i++) {
	        	if(set.terminals[t][i] instanceof TerminalERCUniform) {
	        		terminals[0] = set.terminals[t][i];
	        	}
	        	if(set.constants[t][i] instanceof NumericalValue) {
	        		constants[0] = set.terminals[t][i];
	        	}
	        }
	        
//	        GPNode[] terminals = set.terminals[t];
	        GPNode[] nonterminals = set.nonterminals[t];
//	        GPNode[] constants = set.constants[t];
	        GPNode[] flowoperators = set.flowoperators[t];
	        GPNode[] nodes = set.nodes[t];          

	        if (nodes.length == 0)
	            errorAboutNoNodeWithType(type, state);   // total failure

	        // pick a terminal when we're at max depth or if there are NO nonterminals
	        if((  current+1 >= max                                                       // Now pick if we're at max depth
	        		|| (parent instanceof BranchingTGP && argposition == 0)
	        		|| warnAboutNonterminal(nonterminals.length==0, type, false, state)) &&     // OR if there are NO nonterminals!
	            // this will freak out the static checkers
	            (triedTerminals = true) &&                                                  // [first set triedTerminals]
	            terminals.length != 0) 
	        	{
	        	GPNode n;
	        	if(parent instanceof BranchingTGP && argposition == 1)
	        		n = (GPNode)(constants[state.random[thread].nextInt(constants.length)].lightClone());
	        	else
	        		n = (GPNode)(terminals[state.random[thread].nextInt(terminals.length)].lightClone());
	            n.resetNode(state,thread);  // give ERCs a chance to randomize
	            n.argposition = (byte)argposition;
	            n.parent = parent;
	            return n;
	        }
	        else if(parent instanceof BranchingTGP && argposition == 1) {
	        	GPNode n = (GPNode)(constants[state.random[thread].nextInt(constants.length)].lightClone());
	        	n.resetNode(state,thread);  // give ERCs a chance to randomize
	            n.argposition = (byte)argposition;
	            n.parent = parent;
	            return n;
	        }
	        else if(parent instanceof BranchingTGP && argposition == 0) {
	        	GPNode n = (GPNode)(terminals[state.random[thread].nextInt(terminals.length)].lightClone());
	        	n.resetNode(state,thread);  // give ERCs a chance to randomize
	            n.argposition = (byte)argposition;
	            n.parent = parent;
	            return n;
	        }
	        else if(current == 0) {
	        	if (triedTerminals) warnAboutNoTerminalWithType(type, false, state);        // we tried terminal and we're here because there were none!
	            
	            GPNode[] nodesToPick = set.nonterminals[type.type];
	            if (nodesToPick==null || nodesToPick.length ==0)                            // no nonterminals, hope the guy knows what he's doing!
	                nodesToPick = set.terminals[type.type];                                 // this can only happen with the warning about nonterminals above

	            GPNode n = (GPNode)(nodesToPick[state.random[thread].nextInt(nodesToPick.length)].lightClone());
	            n.resetNode(state,thread);  // give ERCs a chance to randomize
	            n.argposition = (byte)argposition;
	            n.parent = parent;

	            // Populate the node...
	            GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
	            for(int x=0;x<childtypes.length;x++)
	                n.children[x] = growNode(state,current+1,max,childtypes[x],thread,n,x,set);

	            return n;
	        }
	        else if((!(parent instanceof BranchingTGP))) { //can only select arithmetic function nodes
	        	if (triedTerminals) warnAboutNoTerminalWithType(type, false, state);        // we tried terminal and we're here because there were none!
	            
	            GPNode[] nodesToPick = set.nonterminals[type.type];
	            GPNode[] nodesNotToPick = set.flowoperators[type.type];
	            
	            ArrayList<Integer> arithmeticFunctions = new ArrayList<>();
	            for(int i = 0; i<set.nonterminals[type.type].length; i++) {
	            	boolean exist = false;
	            	for(int j = 0; j<set.flowoperators[type.type].length; j++) {
	            		if(set.nonterminals[type.type][i].toString().equals(set.flowoperators[type.type][j].toString())) {
	            			exist = true;
	            			break;
	            		}
	            	}
	            	if(!exist) {
	            		arithmeticFunctions.add(i);
	            	}
	            }
	            
	            if (nodesToPick==null || nodesToPick.length ==0 || arithmeticFunctions.size()==0)                            // no nonterminals, hope the guy knows what he's doing!
	                nodesToPick = set.terminals[type.type];                                 // this can only happen with the warning about nonterminals above

	            int pick = arithmeticFunctions.get(state.random[thread].nextInt(arithmeticFunctions.size())); 
	            GPNode n = (GPNode)(nodesToPick[pick].lightClone());
	            n.resetNode(state,thread);  // give ERCs a chance to randomize
	            n.argposition = (byte)argposition;
	            n.parent = parent;

	            // Populate the node...
	            GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
	            for(int x=0;x<childtypes.length;x++)
	                n.children[x] = growNode(state,current+1,max,childtypes[x],thread,n,x,set);

	            return n;
	        }
	        // else force a nonterminal unless we have no choice
	        else 
//	        	if(parent instanceof BranchingTGP && argposition > 1 || current == 0)  //can select any function or terminal nodes when the parent is branching
	            {
	            if (triedTerminals) warnAboutNoTerminalWithType(type, false, state);        // we tried terminal and we're here because there were none!
	                                
	            GPNode[] nodesToPick = set.nonterminals[type.type];
	            if (nodesToPick==null || nodesToPick.length ==0)                            // no nonterminals, hope the guy knows what he's doing!
	                nodesToPick = set.terminals[type.type];                                 // this can only happen with the warning about nonterminals above
	            
	            GPNode n = (GPNode)(nodesToPick[state.random[thread].nextInt(nodesToPick.length)].lightClone());
	            n.resetNode(state,thread);  // give ERCs a chance to randomize
	            n.argposition = (byte)argposition;
	            n.parent = parent;

	            // Populate the node...
	            GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
	            for(int x=0;x<childtypes.length;x++)
	                n.children[x] = growNode(state,current+1,max,childtypes[x],thread,n,x,set);

	            return n;
	            }
	  }
}
