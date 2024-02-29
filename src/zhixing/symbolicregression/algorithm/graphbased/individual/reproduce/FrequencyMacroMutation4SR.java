package zhixing.symbolicregression.algorithm.graphbased.individual.reproduce;

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
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ConstantGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;
import zhixing.symbolicregression.individual.primitive.InputFeatureGPNode;
import zhixing.symbolicregression.optimization.GPSymbolicRegression;

public class FrequencyMacroMutation4SR extends zhixing.cpxInd.algorithm.Graphbased.individual.reproduce.FrequencyMacroMutation{

	@Override
	protected GPNode sampleInstrBasedFrequency(LGPIndividual offspring, GPTreeStruct tree, int insert, double consRate,
			double[] frequency, EvolutionState state, int thread, GPType type, GPFunctionSet set) {

		 int t = type.type;
	     GPNode[] terminals = set.terminals[t];
	     GPNode[] functions = set.nonterminals[t];
	     GPNode[] nodes = set.nodes[t];  
	     GPNode[] registers = set.registers[t];
	     GPNode[] nonregisters = set.nonregisters[t];
	     GPNode[] constants = set.constants[t]; //only
	     GPNode[] nonconstants = set.nonconstants[t]; // the terminals but not constants

	     GPInitializer initializer = ((GPInitializer)state.initializer);

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
			
			int constant_dim = ((GPSymbolicRegression) state.evaluator.p_problem).datadim;
			
			int functionInd = sampleNextFunBasedFrequency(frequency,  functions.length, functions.length+constant_dim, state, thread);
			
			GPNode n = (GPNode)(functions[functionInd].lightClone());
           n.resetNode(state,thread);  // give ERCs a chance to randomize
           //tree.child = tree.child.cloneReplacingNoSubclone(n, tree.child.children[0]);
           
           for(int tr = 1; tr< 50 && (tree.child.children[0].constraints(initializer) != n.constraints(initializer)); tr++) {
        	   functionInd = sampleNextFunBasedFrequency(frequency,  functions.length, functions.length+constant_dim, state, thread);
        	   n = (GPNode)(functions[functionInd].lightClone());
               n.resetNode(state,thread);  // give ERCs a chance to randomize
           }
           
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
                   
               
               tree.child.children[0].replaceWith(n);
           }
           
           //n.argposition = (byte)argposition;
           //n.parent = tree;
           
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
//           		parentInd = 0;
//           		for(int f = 0;f<functions.length;f++) {
//           			if(functions[f].toString().equals(n.toString())) {
//           				parentInd = f;
//           				break;
//           			}
//           		}
           		
           		int constantInd = sampleConsBasedFrequency(frequency, functions.length, functions.length+constant_dim, state, thread);
           		m = (GPNode)((GPNode) set.constants_v.get(state.random[thread].nextInt(set.constants_v.size()))).lightClone();
           		
           		if(m.toString().startsWith("In")) {
           			((InputFeatureGPNode)m).setIndex(constantInd);
           		}
           		else if(m.toString().matches("-?\\d+(\\.\\d+)?")) {
           			((ConstantGPNode)m).resetNode(state, thread);
           		}
           		else {
           			System.err.print("under developed constant terminals in FrequencyMacroMutation4SR\n");
           			System.exit(1);
           		}
           		
           	}
           	else {
           		m = (GPNode)(nonconstants[state.random[thread].nextInt(nonconstants.length)].lightClone());
           		 m.resetNode(state,thread);
           	}
               
//               m.argposition = (byte)argposition;
//               m.parent = n;
               
               //tree.child.cloneReplacingNoSubclone(m, n.children[x]);
               n.children[x].replaceWith(m);
           }
           
		 }
		 
		 
		 return root;
	}

}
