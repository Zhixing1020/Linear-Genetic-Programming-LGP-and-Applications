package zhixing.djss.algorithm.Graphbased.individual.reproduce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ec.EvolutionState;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import ec.gp.GPType;
import yimei.jss.gp.GPRuleEvolutionState;
//import yimei.jss.gp.terminal.JobShopAttribute;
import yimei.jss.gp.terminal.TerminalERC;
import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;
import zhixing.cpxInd.algorithm.Graphbased.individual.reproduce.FrequencyAllMacroMutation;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ConstantGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public class FrequencyAllMacroMutation4DJSS extends FrequencyAllMacroMutation{

	@Override
	protected GPNode sampleInstrBasedFrequency(
			LGPIndividual offspring,
			GPTreeStruct tree,
			int insert, 
			//double effRate, 
			double consRate,
			double [] frequency, 
			//double [][] normAM,
			EvolutionState state, 
			int thread,
			final GPType type,
			//final GPNodeParent parent,
	        //final int argposition,
	        final GPFunctionSet set) {
		
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		 int t = type.type;
	     GPNode[] terminals = set.terminals[t];
	     GPNode[] functions = set.nonterminals[t];
	     GPNode[] nodes = set.nodes[t];  
	     GPNode[] registers = set.registers[t];
	     GPNode[] nonregisters = set.nonregisters[t];
	     GPNode[] constants = set.constants[t]; //only
	     GPNode[] nonconstants = set.nonconstants[t]; // the terminals but not constants
	     

	     if(insert - 1 >= 0) {
	    	 tree.effRegisters = offspring.getTreeStruct(insert - 1).effRegisters;
	     }
	     else {
	    	 tree.effRegisters = offspring.getTreeStruct(0).effRegisters;
	    	 
	    	 //remove WriteRegister and update ReadRegister
	    	 if(offspring.getTreeStruct(0).status) {
	    		 tree.effRegisters.remove(((WriteRegisterGPNode) offspring.getTreeStruct(0).child).getIndex());
				 tree.effRegisters.addAll(offspring.getTreeStruct(0).collectReadRegister());
	    	 }
	     }
	     
	     if (nodes.length == 0) {// total failure
	    	 System.err.print("there is no primitives in AMCrossover\n");
	    	 System.exit(1);
	     }
	     
	     final int trial = 10;
	     
	     boolean res = false;
		 
		 //effective or not?
	     GPNode root;

		 { //extron
			//1. randomly select an effective register
			 root = (GPNode)(registers[state.random[thread].nextInt(registers.length)].lightClone());
			 root.resetNode(state,thread);  
	         //root.argposition = (byte)argposition;
	         //root.parent = tree;
	         
			 int tri = trial;
			 while((/*tree.status &&*/ !tree.effRegisters.contains(((WriteRegisterGPNode)root).getIndex()))
					// && (!tree.status && tree.effRegisters.contains(((WriteRegisterGPNode)root).getIndex()))
					 && tri>0) {
				 root.resetNode(state,thread);  
				 tri --;
			 }
			 
			 tree.child.replaceWith(root);
			 root = tree.child;
			 
			 
			 	// Populate the node...
			 //GPType[] childtypes = root.constraints(((GPInitializer)state.initializer)).childtypes;
			 
			//2. select functions based on the AM. identify its parent based on the effective register.
			if (functions==null || functions.length ==0)                            // no nonterminals, hope the guy knows what he's doing!
            {    //functions = set.terminals[type.type];                                 // this can only happen with the warning about nonterminals above
            	System.err.print("cannot collect functions in AMCrossover\n");
            	System.exit(1);
            }
			
			int functionInd = sampleNextFunBasedFrequency(frequency,  ((LGPIndividual4Graph)offspring).getDimension_fun(), ((LGPIndividual4Graph)offspring).getDimension(), state, thread);
			
			GPNode n = (GPNode)(functions[functionInd].lightClone());
            n.resetNode(state,thread);  // give ERCs a chance to randomize
            //tree.child = tree.child.cloneReplacingNoSubclone(n, tree.child.children[0]);
//            tree.child.children[0].replaceWith(n);
            //n.argposition = (byte)argposition;
            //n.parent = tree;
            
            if(tree.child.children[0].constraints(initializer) == n.constraints(initializer)) {
         	   tree.child.children[0].replaceWith(n);
            }
            else {        	   
         	   // Populate the node...
                GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
                for(int x=0;x<childtypes.length;x++) {
             	   if(builder instanceof LGPMutationGrowBuilder) {
             		   n.children[x] = ((LGPMutationGrowBuilder)builder).newRootedTree(state, childtypes[x], thread, n, set, x, GPNodeBuilder.NOSIZEGIVEN, tree.child.children[0].atDepth()+1);
             	   }
             	   else {
             		   n.children[x] = builder.newRootedTree(state, childtypes[x], thread, n, set, x, GPNodeBuilder.NOSIZEGIVEN);
             	   }
                }
                    
                
//                tree.child.children[0].replaceWith(n);

             // copy the parent and argposition
                n.parent = tree.child.children[0].parent;
                n.argposition = tree.child.children[0].argposition;
                
                // replace the parent pointer
                if (n.parent instanceof GPNode)
                    ((GPNode)(n.parent)).children[tree.child.children[0].argposition] = n;
                else
                    ((GPTree)(n.parent)).child = n;
            }
            
          //3. randomly select registers.
            GPType[] childtypes = n.constraints(((GPInitializer)state.initializer)).childtypes;
            
            List<Integer> index = new ArrayList<>();
            for(int it = 0;it<childtypes.length;it++){
            	index.add(it);
            }
            for(int it = 0; it<index.size();it++) {
            	Collections.swap(index, it, state.random[thread].nextInt(index.size()));
            }
            
            for(int it=0;it<childtypes.length;it++) {
            	int x = index.get(it);
            	GPNode m;
            	
            	if(state.random[thread].nextDouble()<consRate && ((LGPMutationGrowBuilder)builder).canAddConstant(n)) {
            		//find the index of its parent
//            		parentInd = 0;
//            		for(int f = 0;f<functions.length;f++) {
//            			if(functions[f].toString().equals(n.toString())) {
//            				parentInd = f;
//            				break;
//            			}
//            		}
            		
            		int constantInd = sampleConsBasedFrequency(frequency, ((LGPIndividual4Graph)offspring).getDimension_fun(), ((LGPIndividual4Graph)offspring).getDimension(), state, thread);
            		m = (GPNode)((GPNode) set.constants_v.get(state.random[thread].nextInt(set.constants_v.size()))).lightClone();
            		((TerminalERC)m).setTerminal(((GPRuleEvolutionState)state).getTerminals().get(constantInd));
            		
            	}
            	else {
            		m = (GPNode)(nonconstants[state.random[thread].nextInt(nonconstants.length)].lightClone());
            		 m.resetNode(state,thread);
            	}
                
//                m.argposition = (byte)argposition;
//                m.parent = n;
                
                //tree.child.cloneReplacingNoSubclone(m, n.children[x]);
                n.children[x].replaceWith(m);
            }
			
            
		 }
		 
		 
		 return root;
	}
	
}
