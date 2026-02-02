package zhixing.djss.algorithm.Multiform.individual.reproduce;

import java.util.ArrayList;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPNodeParent;
import ec.gp.GPType;
import yimei.jss.gp.GPRuleEvolutionState;
import yimei.jss.gp.terminal.TerminalERC;
import zhixing.cpxInd.algorithm.Multiform.individual.reproduce.SwapRandAdjList4TGP;

public class SwapRandAdjList4TGPDJSS extends SwapRandAdjList4TGP{

	@Override
	protected GPNode growNodeBasedAT(final EvolutionState state,
			final int current,
	        final int max,
	        final GPType type,
	        final int thread, 
	        final GPNodeParent parent,
	        final int argposition,
	        final GPFunctionSet set,
	        ArrayList<Pair<String, ArrayList<String>>> cand,
	        int ATindex){
		

		int t = type.type;
        GPNode[] terminals = set.terminals[t];
        // GPNode[] nonterminals = set.nonterminals[t];
        GPNode[] nodes = set.nodes[t];          

        if (nodes.length == 0){
        	System.err.print("there is no primitives for TGPIndividual4MForm\n");
        	System.exit(1);
        }
        

        if(cand.size()==0){
			//it means it cannot find corresponding item for this GP Node. Then, grow a new sub-tree anyway
//        	GPNode n = (GPNode)(terminals[state.random[thread].nextInt(terminals.length)].lightClone());
//        	n.resetNode(state,thread);  // give ERCs a chance to randomize
//            n.argposition = (byte)argposition;
//            n.parent = parent;
            
        	GPNode n = builder.newRootedSubTree(state, current, type, thread, parent, set, argposition, GPNodeBuilder.NOSIZEGIVEN);
            return n;
		}
        
        if(ATindex >= cand.size()) {
        	System.err.print("ATindex is larger than or equals to adjacency table's size in ATCrossover4TGP\n");
        	System.exit(1);
        }
        
        boolean triedTerminals = false;
		
		Pair<String, ArrayList<String>> ATitem = cand.get(ATindex);

        // pick a terminal when we're at max depth or if there are NO nonterminals
        if ((current+1 >= max) &&                                                       // Now pick if we're at max depth
            // this will freak out the static checkers
            (triedTerminals = true) &&                                                  // [first set triedTerminals]
            terminals.length != 0)                                                      // AND if there are available terminal
            {

        	GPNode n = (GPNode)(terminals[state.random[thread].nextInt(terminals.length)].lightClone());
        	//set the node based on ATitem
//        	for(int i = 0;i<terminals.length;i++) {
//
//	   			 if(ATitem.getFirst().equals(((GPRuleEvolutionState)state).getTerminals().get(i).toString())) {
//	   				((TerminalERC)n).setTerminal(((GPRuleEvolutionState)state).getTerminals().get(i));
//	   			 }
//   		 	}

            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;
            return n;
            }
                        
        // else pick a random node
        else
            {
            if (triedTerminals) {        // we tried terminal and we're here because there were none!
            	System.err.print("we tried terminal and we find nothing in ATC for TGP\n!");
            	System.exit(1);
            }

            GPNode n = (GPNode)(nodes[state.random[thread].nextInt(nodes.length)].lightClone());
            //set the node based on ATitem
            for(int i = 0;i<nodes.length;i++) {
	   			 if(ATitem.getFirst().equals(nodes[i].toString())) {
	   				 	n = (GPNode)(nodes[i].lightClone());
	   			 }
  		 	}
            
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            n.argposition = (byte)argposition;
            n.parent = parent;

            // Populate the node...
            GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
            for(int x=0;x<childtypes.length;x++){
            	
            	//find the next ATitem whose string is equals to this item's second part
            	if(ATitem.getSecond().get(x) != null) {
            		//if terminal
            		int k = 0;
            		for(;k<((GPRuleEvolutionState)state).getTerminals().size();k++) {
	    				 if(ATitem.getSecond().get(x).equals(((GPRuleEvolutionState)state).getTerminals().get(k).toString()) 
	    						 ) 
	    				 { 
	    					 //n.children[x] = (GPNode)(terminals[k].lightClone());
	    					 n.children[x] = (GPNode)(terminals[state.random[thread].nextInt(terminals.length)].lightClone());
	    					 ((TerminalERC)n.children[x]).setTerminal(((GPRuleEvolutionState)state).getTerminals().get(k));
	    					 break;
	    				 }
	    			 }
            		
            		//if function, find the following ATitems whose string is equals to this item's second part
            		if(k==((GPRuleEvolutionState)state).getTerminals().size()) {
            			ArrayList<Integer> ATitemList = new ArrayList<>();
	    				 
            			//following items
	    				 for(int l = ATindex+1; l<cand.size(); l++) {
	    					 if(cand.get(l).getFirst().equals(ATitem.getSecond().get(x))) {
	    						 ATitemList.add(l);
	    					 }
	    				 }
	    				 
	    				//recursively call growNodeBasedAT
	    				 if(!ATitemList.isEmpty()) {
	    					 int ll = ATitemList.get(state.random[thread].nextInt(ATitemList.size()));
	    					 n.children[x] = growNodeBasedAT(state,current+1,max,childtypes[x],thread,n,x,set,cand,ll);
	    				 }
	    				 else //randomly generate the tree node
	    				 {
	    					n.children[x] = builder.newRootedSubTree(state, current+1, childtypes[x], thread, n, set, x, GPNodeBuilder.NOSIZEGIVEN);
	    				 }
            		}

            	}
            	else {//randomly generate the tree node
            		n.children[x] = builder.newRootedSubTree(state, current+1, childtypes[x], thread, n, set, x, GPNodeBuilder.NOSIZEGIVEN);
            	}
            }
                

            return n;
            }
	}


}
