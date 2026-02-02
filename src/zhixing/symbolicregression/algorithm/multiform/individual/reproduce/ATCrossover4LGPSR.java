package zhixing.symbolicregression.algorithm.multiform.individual.reproduce;

import java.util.ArrayList;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
//import yimei.jss.gp.terminal.JobShopAttribute;
import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;
import zhixing.cpxInd.algorithm.Multiform.individual.reproduce.ATCrossover4LGP;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ConstantGPNode;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.symbolicregression.algorithm.multiform.individual.LGPIndividual4SR_MForm;
import zhixing.symbolicregression.individual.primitive.InputFeatureGPNode;
import zhixing.symbolicregression.optimization.GPSymbolicRegression;

public class ATCrossover4LGPSR extends ATCrossover4LGP {
	
	@Override
	protected void generateInstrBasedAT(
			LGPIndividual offspring,
			GPTreeStruct tree,
			int insert, 
			double consRate,
			Pair<String, ArrayList<String>> ATitem, 
			//double [][] normAM,
			EvolutionState state, 
			int thread
			//final GPType type,
			//final GPNodeParent parent,
	        //final int argposition,
	        ) {
		
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		 int t = tree.child.parentType(initializer).type;
		 GPFunctionSet set = tree.constraints(initializer).functionset;
	     GPNode[] terminals = set.terminals[t];
	     GPNode[] functions = set.nonterminals[t];
	     GPNode[] nodes = set.nodes[t];  
	     GPNode[] registers = set.registers[t];
	     GPNode[] nonregisters = set.nonregisters[t];
	     GPNode[] constants = set.constants[t]; //only
	     GPNode[] nonconstants = set.nonconstants[t]; // the terminals but not constants
	     

	     
	     if (nodes.length == 0) {// total failure
	    	 System.err.print("there is no primitives in AMCrossover\n");
	    	 System.exit(1);
	     }
		 
		 //effective or not?
	     GPNode root;
	     
		 root = tree.child;
		 root.resetNode(state, thread);
		 
		 if(root.depth()>3) {
			 System.err.print("ATC does not support instructions with more than 3 depth\n");
			 System.exit(1);
		 }
		 
		//2. select functions based on the AT item.
		 for(int i = 0;i<functions.length;i++) {
			 if(ATitem.getFirst().equals(functions[i].toString())) {
				 	GPNode n = (GPNode)(functions[i].lightClone());
		            n.resetNode(state,thread);  // give ERCs a chance to randomize
		            root.children[0].replaceWith(n);
			 }
		 }
		 
		 //3. generate registers
		 int j = state.random[thread].nextInt(ATitem.getSecond().size());
		 for(int jj = 0; jj<ATitem.getSecond().size(); jj++) {
			 j = (j + 1) % ATitem.getSecond().size();
			 GPNode m;
			 
			 int k = 0;
			 
			 m = (GPNode)(nonconstants[state.random[thread].nextInt(nonconstants.length)].lightClone());
    		 m.resetNode(state,thread);
    		 
    		 if(ATitem.getSecond().get(j) != null) {
    			 
    			 if(builder.canAddConstant(root)){
    				 for(int c = 0; c<constants.length; c++) {
        				 if(ATitem.getSecond().get(j).startsWith("In") 
        						 && constants[c].toString().startsWith("In")) {
            				 m = constants[c].lightClone();
            				 String indexStr = ATitem.getSecond().get(j).substring(2, ATitem.getSecond().get(j).length());
            	             int index = Integer.valueOf(indexStr);
            				 ((InputFeatureGPNode)m).setIndex(index);
            			 }
        				 
        				 else if(ATitem.getSecond().get(j).matches("-?\\d+(\\.\\d+)?")
        						 && constants[c].toString().matches("-?\\d+(\\.\\d+)?")) {
        					 m = constants[c].lightClone();
        					 ((ConstantGPNode)m).setValue(Double.valueOf(ATitem.getSecond().get(j)));
        				 }
        			 }
				 }
//				 else{
//					 if(k < offspring.getRegisters().length)
//						 ((ReadRegisterGPNode)m).setIndex(k);
//				 }
    			

    		 }
    		 
    		 if(root.children[0].children[j] == null) {
    			 root.children[0].children[j] = (GPNode)(nonconstants[state.random[thread].nextInt(nonconstants.length)].lightClone());
    			 root.children[0].children[j].parent = root.children[0];
    		 }
    			 
    		 root.children[0].children[j].replaceWith(m);
		 }

	}
	
	@Override
	protected void maintainConnection(LGPIndividual pr, 
			int start, 
			int number, 
			ArrayList<Pair<String, ArrayList<String>>> AT, 
			EvolutionState state, 
			int thread) {
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		int t = pr.getTree(0).child.parentType(initializer).type;
		GPFunctionSet set = pr.getTree(0).constraints(initializer).functionset;
		GPNode[] nonconstants = set.nonconstants[t]; // the terminals but not constants
		 
		//double rate = state.random[thread].nextDouble();
		
		for(int i = start + number - 1, ii = 0;i>=start;i--,ii++) {
			
			Pair<String, ArrayList<String>> ATitem = AT.get(ii);
			
			//write register
			if(!pr.getTreeStruct(i).status ) {
            	int cnt = pr.getRegisters().length*5;
            	while(!pr.getTreeStruct(i).status && cnt > 0) {
            		((WriteRegisterGPNode)pr.getTreeStruct(i).child).resetNode(state, thread);
            		pr.updateStatus();
            		cnt --;
            	}
			}
			
			//source register
			 //int j = state.random[thread].nextInt(ATitem.getSecond().size());
			 //for(int jj = 0; jj<ATitem.getSecond().size(); jj++) 
			 for(int j = 0; j<ATitem.getSecond().size(); j++)
			 {
				 //j = (j + 1) % ATitem.getSecond().size();
				 int k = 0;
				 if(pr.getTreeStruct(i).child.children[0].children[j] instanceof ReadRegisterGPNode ) {
					 if(ATitem.getSecond().get(j) != null) {
		    			 for(;k<((GPSymbolicRegression) state.evaluator.p_problem).datadim;k++) {
		    				 if(ATitem.getSecond().get(j).equals(((LGPIndividual4SR_MForm)pr).getPrimitives().get(k).toString()))  //JobShopAttribute.relativeAttributes()[k].getName() 
		    				 { 
		    					 break;
		    				 }
		    			 }
		    			 if(k==((GPSymbolicRegression) state.evaluator.p_problem).datadim) {//it is not a constant, so it should be a function
		    				 //find the upper instructions in the genome to see which ones have this function
		    				 ArrayList<Integer> cand = new ArrayList<>();
		    				 
		    				 for(int l = i-1; l>=start; l--) {
		    					 if(pr.getTreeStruct(l).child.children[0].toString().equals(ATitem.getSecond().get(j))) {
		    						 cand.add(((WriteRegisterGPNode)pr.getTreeStruct(l).child).getIndex());
		    					 }
		    				 }
		    				 
		    				 if(!cand.isEmpty()) {
		    					 ((ReadRegisterGPNode)pr.getTreeStruct(i).child.children[0].
		    							 children[j]).setIndex(cand.get(state.random[thread].nextInt(cand.size())));
		    				 }
		    				 else if(i>0 && state.random[thread].nextInt(i) - 1 > 0)//connect source registers with one of the upper instructions (in genotype)
		    				 {
		    					//randomly pick one instruction and get its write register
		    					int tr = state.random[thread].nextInt(i);
		    					((ReadRegisterGPNode)pr.getTreeStruct(i).child.children[0].
		    							 children[j]).setIndex(((WriteRegisterGPNode)pr.getTreeStruct(tr).child).getIndex());
		    					
		    				 }
		    			 }
		    		 }
				 }
				 
	    		 pr.updateStatus();
			 }
		}
	}
}
