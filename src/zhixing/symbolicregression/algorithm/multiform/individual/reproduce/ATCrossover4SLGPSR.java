package zhixing.symbolicregression.algorithm.multiform.individual.reproduce;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPNodeParent;
import ec.gp.GPType;
import ec.gp.koza.KozaNodeSelector;
import ec.util.Parameter;
//import yimei.jss.gp.terminal.JobShopAttribute;
import zhixing.cpxInd.algorithm.Multiform.individual.TGPIndividual4MForm;
import zhixing.cpxInd.algorithm.Multiform.individual.reproduce.ATCrossover4SLGP;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ConstantGPNode;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.symbolicregression.individual.primitive.InputFeatureGPNode;

public class ATCrossover4SLGPSR extends ATCrossover4SLGP{

//	@Override
//	public int produce(final int min, 
//	        final int max, 
//	        final int start,
//	        final int subpopulation,
//	        final Individual[] inds,
//	        final EvolutionState state,
//	        final int thread,
//	        final CpxGPIndividual[] parents) 
//
//        {
//		// how many individuals should we make?
//        int n = typicalIndsProduced();
//        if (n < min) n = min;
//        if (n > max) n = max;
//		
//		GPInitializer initializer = ((GPInitializer)state.initializer);
//
//		//for every item in AT, new an instruction (with the specified function), the registers of the instruction are randomly initialized at first
//        //every new an instruction, clone the function GPNode based on the index in primitives
//        //check the write registers so that they are ?effective?
//        //check the read registers so that they are connected to corresponding children if they exist. 
//
//        for(int q=start, parnt = 0;q<n+start; /* no increment */)  // keep on going until we're filled up
//            {
//            // at this point, parents[] contains our two selected individuals
//            
//            // are our tree values valid?
//            if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= parents[0].getTreesLength()))
//                // uh oh
//                state.output.fatal("LGP ATCrossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
//            if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= parents[1].getTreesLength()))
//                // uh oh
//                state.output.fatal("LGP ATCrossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual");
//            
//            int t1=0, t2=0;
//	        LGPIndividual j1;
//	        CpxGPIndividual j2;
//	        j1 = ((LGPIndividual)parents[parnt]).lightClone();
//	    	t1 = parnt;
//	    	
//	        j2 = (parents[(parnt + 1)%parents.length]).lightClone();
//	        if (j2 instanceof TGPIndividual4MForm) {
//	        	if(parents[(parnt + 1)%parents.length].getTreesLength() > 1) 
//	        		t2 = state.random[thread].nextInt(parents[(parnt + 1)%parents.length].getTreesLength());
//	        	else t2 = 0;
//	        }
//	        else t2 = (parnt + 1)%parents.length;
//	        
//	        boolean flag = false;
//	        ArrayList<Pair<String, ArrayList<String>>> cand1 = new ArrayList<>();
//	        ArrayList<Pair<String, ArrayList<String>>> cand2 = new ArrayList<>();
//	        
//	        //prepare the node selector
//	        nodeselect2.reset();
//	        
//	        int begin1=j1.getTreesLength()+1, begin2=j2.getTreesLength()+1, pickNum1=j1.getTreesLength(), pickNum2=j2.getTreesLength();
//	        int end1 = 0;
//	        GPNode p2 = null;
//	        Iterator<Pair<String, ArrayList<String>>> it;
//	        
//	        int numFunNode1 = 0, numFunNode2 = 0;
//	        
//	        for(int i = 0; i< j1.getTreesLength(); i++) {
//	        	numFunNode1 += j1.getTreeStruct(i).child.numNodes(GPNode.NODESEARCH_NONTERMINALS); 
//	        }
//	        
//	        for(int t = 0;t<numTries;t++){
//
//	        	//prepare j2
//            	if(j2 instanceof TGPIndividual4MForm){
//            		
//            		//prepare j1
//		        	begin1 = state.random[thread].nextInt(j1.getTreesLength());
//		        	end1 = state.random[thread].nextInt(j1.getTreesLength()-begin1)+begin1+1;
//		        	for(int i = begin1; i< j1.getTreesLength(); i++) {
//			        	numFunNode1 += j1.getTreeStruct(i).child.numNodes(GPNode.NODESEARCH_NONTERMINALS); 
//			        }
//		        	pickNum1 = state.random[thread].nextInt(numFunNode1) + 1;
//		        	
//		        	cand1 = j1.getAdjacencyTable(begin1, end1);
//		        	it = cand1.iterator();
//	            	int cnt = cand1.size();
//	            	//int GraphSize = 1 + state.random[thread].nextInt(MaxGraphSize);
//	            	while(it.hasNext()){
//	            		//int ind = it.next();
//	            		it.next();
//	            		//cnt ++;
//	            		if(cnt > pickNum1){
//	            			it.remove();
//	            		}
//	            		cnt --;
//	            	}
//            		
//	            	int trial_dep = state.random[thread].nextInt(j2.getTree(t2).child.depth()); //the max random depth must be child.depth()-1, so it has ignored terminals
//	            	p2 = ((KozaNodeSelector)nodeselect2).pickNode(state,subpopulation,thread,j2,j2.getTree(t2), GPNode.NODESEARCH_NONTERMINALS);
//	            	for(int tt = 0; tt<numTries; tt++) {
//	            		if(p2.atDepth() == trial_dep) break;
//	            		p2 = ((KozaNodeSelector)nodeselect2).pickNode(state,subpopulation,thread,j2,j2.getTree(t2), GPNode.NODESEARCH_NONTERMINALS);
//	            	}
////	            	p2 = j2.getTree(t2).child;
//	            	cand2 = ((TGPIndividual4MForm)j2).getAdjacencyTable(p2);
//		        	//p2 = ((KozaNodeSelector)nodeselect2).pickNode(state,subpopulation,thread,j2,j2.getTree(t2), GPNode.NODESEARCH_NONTERMINALS);
////		        	cand2 = ((TGPIndividual4MForm)j2).getAdjacencyTable(j2.getTree(t2).child);
//		        	
////		        	if(cand2.size()>0)
////		        	{
////		        		begin2 = state.random[thread].nextInt(cand2.size());
//////		        		for(int tt = 0;tt<numTries;tt++) {
//////		        			begin2 = state.random[thread].nextInt(cand2.size());
//////		        			if(Math.abs(j1.getTreesLength() - begin1 - begin2)<=MaxDistanceCrossPoint) break;
//////		        		}
////		        		
////		        		pickNum2 = state.random[thread].nextInt(Math.min(begin2+1, MaxSegLength)) + 1;
////		        		
////		        		it = cand2.iterator();
////		            	//cnt = cand2.size();
////		            	int in = 0;
////		            	//GraphSize = 1 + state.random[thread].nextInt(MaxGraphSize);
////		            	while(it.hasNext()){
////		            		//int ind = it.next();
////		            		it.next();
////		            		in ++;
////
////		            		if(in < begin2 - pickNum2 || in > begin2){
////		            			it.remove();
////		            		}
////		            		//cnt --;
////		            	}
////		        		
////		        	}
//		        	
//		        	
//	        	}
//	        	else if (j2 instanceof LGPIndividual){
//	        		
//	        		//prepare j1
//		        	begin1 = state.random[thread].nextInt(j1.getTreesLength());
//		        	end1 = state.random[thread].nextInt(j1.getTreesLength()-begin1)+begin1+1;
//		        	for(int i = begin1; i< j1.getTreesLength(); i++) {
//			        	numFunNode1 += j1.getTreeStruct(i).child.numNodes(GPNode.NODESEARCH_NONTERMINALS); 
//			        }
//		        	pickNum1 = state.random[thread].nextInt(numFunNode1) + 1;
//		        	
//		        	cand1 = j1.getAdjacencyTable(begin1, end1);
//		        	it = cand1.iterator();
//	            	int cnt = cand1.size();
//	            	//int GraphSize = 1 + state.random[thread].nextInt(MaxGraphSize);
//	            	while(it.hasNext()){
//	            		//int ind = it.next();
//	            		it.next();
//	            		//cnt ++;
//	            		if(cnt > pickNum1){
//	            			it.remove();
//	            		}
//	            		cnt --;
//	            	}
//	        		
//	        		for(int tt = 0;tt<numTries;tt++) {
//	        			begin2 = state.random[thread].nextInt(j2.getTreesLength());
//	        			if(Math.abs(begin1 - begin2)<=MaxDistanceCrossPoint) break;
//	        		}
//	        		
//	        		int end2 = state.random[thread].nextInt(j2.getTreesLength()-begin2)+begin2+1;
//	        		
//	        		numFunNode2 = 0;
//	        		for(int i = begin2; i< j2.getTreesLength(); i++) {
//	        			numFunNode2 += ((LGPIndividual)j2).getTreeStruct(i).child.numNodes(GPNode.NODESEARCH_NONTERMINALS); 
//	    	        }
//	        		
//	        		pickNum2 = state.random[thread].nextInt(Math.min(numFunNode2, MaxSegLength)) + 1;
//	        		
//	        		cand2 = ((LGPIndividual)j2).getAdjacencyTable(begin2, end2);
//	        		
//	        		it = cand2.iterator();
//	            	cnt = cand2.size();
//	            	//GraphSize = 1 + state.random[thread].nextInt(MaxGraphSize);
//	            	while(it.hasNext()){
//	            		//int ind = it.next();
//	            		it.next();
//	            		//cnt ++;
//	            		if(cnt > pickNum2){
//	            			it.remove();
//	            		}
//	            		cnt --;
//	            	}
//	        	} 	
//	                    	
//	        	flag = verifypoint(j1, cand1.size(), cand2.size());
//	        	
//	        	if(flag) break;
//
//	        }
//
//           if(flag) {
//        	   this.ATSwapping(j1, j1.getTreeStruct(0), begin1, pickNum1, end1 - begin1, cand2, state, thread);
//        	   
//           }
//            
//           if(microMutation != null) j1 = (LGPIndividual) microMutation.produce(subpopulation, j1, state, thread);
//       	
//	        // add the individuals to the population
//	        inds[q] = j1;
//	        q++;
//	        parnt ++;
//	        
//            }
//            
//        return n;
//        }
//	
//	@Override
//	protected boolean verifypoint(LGPIndividual j1, int ind1, int ind2){
//		//ind1 and ind2 are respectively the number of instructions of the candidate instruction list
//		//return true if ind1 and ind2 will not exceed the length constraints
//		//ind1: number of instructions that are removed
//		//ind2: number of instructions that are newly inserted
//		
//		if(ind2 == ind1 && ind1 == 0){
//			return false;
//		}
//		
////		if(Math.abs(ind1 - ind2)>MaxLenDiffSeg)
////			return false;
////		
//		boolean res = true;
////		if(ind1 < ind2){
////			int diff = ind2 - ind1;
////			if(j1.getEffTreesLength() + diff > j1.getMaxNumTrees())
////				res = false;
////		}
////		else if (ind1 > ind2){
////			int diff = ind1 - ind2;
////			if(j1.getEffTreesLength() - diff < j1.getMinNumTrees())
////				res = false;
////		}
//		return res;
//	}
//	
//	@Override
//	protected void ATSwapping(LGPIndividual pr, 
//			GPTreeStruct insprototype, 
//			int begin_genome, 
//			int pickNum_pr, 
//			int remove_num, 
//			ArrayList<Pair<String, ArrayList<String>>> AT, 
//			EvolutionState state, 
//			int thread){
//		
//		//pickNum_pr: the range size of to-be-considered instructions
//		//remove_num: the number of items in to-be-removed adjacency list (selected from pr)
//		
//		//first generate new instructions
//		ArrayList<GPTreeStruct> newTreeList = new ArrayList<>();
//		for(int i = 0;i<AT.size();i++){
//    		
//			Pair<String, ArrayList<String>> item = AT.get(i);
//			
//
//    		GPTreeStruct tree = (GPTreeStruct) (insprototype.clone());
//    		
//    		i = generateTreeBasedAT(pr, tree, 0.5, AT, i, state, thread);
//    		
//            tree.owner = pr;
//            //tree.child = (GPNode)(pd.getTree(source).child.clone());
//            tree.child.parent = tree;
//            tree.child.argposition = 0;
//            
//            newTreeList.add(tree);
//		}
//		
//		//verify the number of to-be-changed instructions
//		int ind1 = remove_num, ind2 = newTreeList.size();
//		int add_num = ind2;
//		if(ind1 < ind2){
//			int diff = ind2 - ind1;
//			if(pr.getEffTreesLength() + diff > pr.getMaxNumTrees())
//				add_num = pr.getMaxNumTrees() - pr.getEffTreesLength() + ind1;
//		}
//		else if (ind1 > ind2){
//			int diff = ind1 - ind2;
//			if(pr.getTreesLength() - diff < pr.getMinNumTrees())
//				remove_num = ind1 = pr.getTreesLength() - pr.getMinNumTrees() + ind2;
//		}
//		
//		//remove pr's instructions
//		//remove the size of AT instructions from the segment [begin_pr, begin_pr+pickNum_pr)
//		for(int i = 0;i<remove_num;i++){ 
////			int rm = begin_genome + state.random[thread].nextInt(pickNum_pr - i);
//			
////			pr.removeTree(rm);
//			pr.removeTree(begin_genome);
//			
//			pr.evaluated = false;
//			
//		}
//
//		//add instructions generated based on AT
////		int start = begin_genome + state.random[thread].nextInt(pickNum_pr - remove_num + 1);
//		int start = begin_genome;
//		int num = 0;
//		for(int i = 0;i<add_num;i++){
//
//    		GPTreeStruct tree = newTreeList.get(i);
//            
//            pr.addTree(start, tree);
//            num++;
//
//            pr.evaluated = false; 
//		}
//		
//		maintainConnection(pr, start, num, AT, state, thread);
//		
//		//remove introns
//		if(pr.getTreesLength() > pr.getMaxNumTrees()){
//			int cnt = pr.getTreesLength() - pr.getMaxNumTrees();
//			for(int k = 0; k<cnt; k++){
//				int res = state.random[thread].nextInt(pr.getTreesLength());
//				if(pr.getEffTreesLength() <= pr.getMaxNumTrees()){
//					for(int x = 0;x < pr.getTreesLength();x++) {
//		        		if(!pr.getTreeStruct(res).status){break;}
//		        		res = (res + state.random[thread].nextInt(pr.getTreesLength())) % pr.getTreesLength();
//		        	}
//					
//				}
//				pr.removeTree(res);
//			}
//		}
//		
//	}
//	
//	protected int generateTreeBasedAT(
//			LGPIndividual offspring,
//			GPTreeStruct tree,
//			double consRate,
//			ArrayList<Pair<String, ArrayList<String>>> AT, 
//			int curind,
//			EvolutionState state, 
//			int thread
//	        ) {
//		
//		//return last used index of ATitem
//		
//		GPInitializer initializer = ((GPInitializer)state.initializer);
//		
//		 int t = tree.child.parentType(initializer).type;
//		 GPFunctionSet set = tree.constraints(initializer).functionset;
//	     GPNode[] terminals = set.terminals[t];
//	     GPNode[] functions = set.nonterminals[t];
//	     GPNode[] nodes = set.nodes[t];  
//	     GPNode[] registers = set.registers[t];
//	     GPNode[] nonregisters = set.nonregisters[t];
//	     GPNode[] constants = set.constants[t]; //only
//	     GPNode[] nonconstants = set.nonconstants[t]; // the terminals but not constants
//	     
//
//	     
//	     if (nodes.length == 0) {// total failure
//	    	 System.err.print("there is no primitives in AMCrossover\n");
//	    	 System.exit(1);
//	     }
//		 
//		 //effective or not?
//	     GPNode root;
//	     
//		 root = tree.child;
//		 root.resetNode(state, thread);
//		 
//		 if(root.depth()>builder.maxDepth) {
//			 System.err.print("ATC does not support instructions with more than "+ builder.maxDepth+ " depth\n");
//			 System.exit(1);
//		 }
//		 
//		 Pair<String, ArrayList<String>> ATitem = AT.get(curind);
//		 //grow the tree until it reaches the maximum depth
//		 
//		 GPNode newchildGpNode = growNodeBasedAT(state, 1, builder.maxDepth, root.parentType(initializer), thread, root, 0, set, AT, curind);
////		 root.children[0].replaceWith(newchildGpNode);
//		 root = root.cloneReplacing(newchildGpNode, root.children[0]);
//		 root.parent = tree;
//		 root.argposition = 0;
//		 tree.child = root;
//
//		 
////		//2. select functions based on the AT item.
////		 for(int i = 0;i<functions.length;i++) {
////			 if(ATitem.getFirst().equals(functions[i].toString())) {
////				 	GPNode n = (GPNode)(functions[i].lightClone());
////		            n.resetNode(state,thread);  // give ERCs a chance to randomize
////		            root.children[0].replaceWith(n);
////			 }
////		 }
////		 
////		 //3. generate registers
////		 int j = state.random[thread].nextInt(ATitem.getSecond().size());
////		 for(int jj = 0; jj<ATitem.getSecond().size(); jj++) {
////			 j = (j + 1) % ATitem.getSecond().size();
////			 GPNode m;
////			 
////			 int k = 0;
////			 
////			 m = (GPNode)(nonconstants[state.random[thread].nextInt(nonconstants.length)].lightClone());
////    		 m.resetNode(state,thread);
////    		 
////    		 if(ATitem.getSecond().get(j) != null) {
////    			 
////    			 if(builder.canAddConstant(root)){
////    				 for(int c = 0; c<constants.length; c++) {
////        				 if(ATitem.getSecond().get(j).startsWith("In") 
////        						 && constants[c].toString().startsWith("In")) {
////            				 m = constants[c].lightClone();
////            				 String indexStr = ATitem.getSecond().get(j).substring(2, ATitem.getSecond().get(j).length());
////            	             int index = Integer.valueOf(indexStr);
////            				 ((InputFeatureGPNode)m).setIndex(index);
////            			 }
////        				 
////        				 else if(ATitem.getSecond().get(j).matches("-?\\d+(\\.\\d+)?")
////        						 && constants[c].toString().matches("-?\\d+(\\.\\d+)?")) {
////        					 m = constants[c].lightClone();
////        					 ((ConstantGPNode)m).setValue(Double.valueOf(ATitem.getSecond().get(j)));
////        				 }
////        			 }
////				 }
////				 else{
////					 if(k < offspring.getRegisters().length)
////						 ((ReadRegisterGPNode)m).setIndex(k);
////				 }
////    			
////
////    		 }
////    		 
////    		 if(root.children[0].children[j] == null) {
////    			 root.children[0].children[j] = (GPNode)(nonconstants[state.random[thread].nextInt(nonconstants.length)].lightClone());
////    			 root.children[0].children[j].parent = root.children[0];
////    		 }
////    			 
////    		 root.children[0].children[j].replaceWith(m);
////		 }
//
//		 int res = curind + root.numNodes(GPNode.NODESEARCH_NONTERMINALS);
//		 
//		 return res;
//	}
//	
//	protected void maintainConnection(LGPIndividual pr, 
//			int start, 
//			int number, 
//			ArrayList<Pair<String, ArrayList<String>>> AT, 
//			EvolutionState state, 
//			int thread) {
//		GPInitializer initializer = ((GPInitializer)state.initializer);
//		
//		int t = pr.getTree(0).child.parentType(initializer).type;
//		GPFunctionSet set = pr.getTree(0).constraints(initializer).functionset;
//		GPNode[] nonconstants = set.nonconstants[t]; // the terminals but not constants
//		 
//		//double rate = state.random[thread].nextDouble();
//		
//		
//		//the number of functions in a tree   equals to  the number of AT items 
//		
//		for(int i = start + number - 1, ii = 0;i>=start;i--,ii++) {
//			
//			Pair<String, ArrayList<String>> ATitem = AT.get(ii);
//			
//			//write register
//			if(!pr.getTreeStruct(i).status ) {
//            	int cnt = pr.getRegisters().length*5;
//            	while(!pr.getTreeStruct(i).status && cnt > 0) {
//            		((WriteRegisterGPNode)pr.getTreeStruct(i).child).resetNode(state, thread);
//            		pr.updateStatus();
//            		cnt --;
//            	}
//			}
//			
//			//source register
//			 //int j = state.random[thread].nextInt(ATitem.getSecond().size());
//			 //for(int jj = 0; jj<ATitem.getSecond().size(); jj++) 
////			 for(int j = 0; j<ATitem.getSecond().size(); j++)
////			 {
////				 //j = (j + 1) % ATitem.getSecond().size();
////				 int k = 0;
////				 if(pr.getTreeStruct(i).child.children[0].children[j] instanceof ReadRegisterGPNode ) {
////					 if(ATitem.getSecond().get(j) != null) {
////		    			 for(;k<JobShopAttribute.relativeAttributes().length;k++) {
////		    				 if(ATitem.getSecond().get(j).equals(JobShopAttribute.relativeAttributes()[k].getName()) 
////		    						 ) 
////		    				 { 
////		    					 break;
////		    				 }
////		    			 }
////		    			 if(k==JobShopAttribute.relativeAttributes().length) {//it is not a constant, so it should be a function
////		    				 //find the upper instructions in the genome to see which ones have this function
////		    				 ArrayList<Integer> cand = new ArrayList<>();
////		    				 
////		    				 for(int l = i-1; l>=start; l--) {
////		    					 if(pr.getTreeStruct(l).child.children[0].toString().equals(ATitem.getSecond().get(j))) {
////		    						 cand.add(((WriteRegisterGPNode)pr.getTreeStruct(l).child).getIndex());
////		    					 }
////		    				 }
////		    				 
////		    				 if(!cand.isEmpty()) {
////		    					 ((ReadRegisterGPNode)pr.getTreeStruct(i).child.children[0].
////		    							 children[j]).setIndex(cand.get(state.random[thread].nextInt(cand.size())));
////		    				 }
////		    				 else if(i>0 && state.random[thread].nextInt(i) - 1 > 0)//connect source registers with one of the upper instructions (in genotype)
////		    				 {
////		    					//randomly pick one instruction and get its write register
////		    					int tr = state.random[thread].nextInt(i);
////		    					((ReadRegisterGPNode)pr.getTreeStruct(i).child.children[0].
////		    							 children[j]).setIndex(((WriteRegisterGPNode)pr.getTreeStruct(tr).child).getIndex());
////		    					
////		    				 }
////		    			 }
////		    		 }
////				 }
////				 
////	    		 pr.updateStatus();
////			 }
//		}
//	}
	
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
	     GPNode[] functions = set.nonterminals[t];
	     GPNode[] nodes = set.nodes[t];  
	     GPNode[] registers = set.registers[t];
	     GPNode[] nonregisters = set.nonregisters[t];
	     GPNode[] constants = set.constants[t]; //only
	     GPNode[] nonconstants = set.nonconstants[t]; // the terminals but not constants
        // GPNode[] nonterminals = set.nonterminals[t];      

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

        // pick a register or constant when we're at max depth or if there are NO nonterminals
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
            		
            		if(builder.canAddConstant(parent)) {
            			for(int c = 0; c<terminals.length; c++) {
                			if(ATitem.getSecond().get(x).startsWith("In") 
              						 && terminals[c].toString().startsWith("In")) {
                  				 n.children[x] = terminals[c].lightClone();
                  				 String indexStr = ATitem.getSecond().get(x).substring(2, ATitem.getSecond().get(x).length());
                  	             int index = Integer.valueOf(indexStr);
                  				 ((InputFeatureGPNode)n.children[x]).setIndex(index);
                  			 }
                			else if(ATitem.getSecond().get(x).matches("-?\\d+(\\.\\d+)?")
              						 && terminals[c].toString().matches("-?\\d+(\\.\\d+)?")) {
                				n.children[x] = terminals[c].lightClone();
              					 ((ConstantGPNode)n.children[x]).setValue(Double.valueOf(ATitem.getSecond().get(x)));
              				 }
                		}
            		}
        			//functions
            		if(n.children[x] == null)  {
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
            	
            	if(n.children[x] == null) {
//            		n.children[x] = (GPNode)(nonconstants[state.random[thread].nextInt(nonconstants.length)].lightClone());
            		n.children[x] = builder.newRootedSubTree(state, current+1, childtypes[x], thread, n, set, x, GPNodeBuilder.NOSIZEGIVEN);
            		n.children[x].parent = n;
       		 	}

            }
                

            return n;
            }
	}

//	@Override
//	protected void generateInstrBasedAT(LGPIndividual offspring, GPTreeStruct tree, int insert, double consRate,
//			Pair<String, ArrayList<String>> ATitem, EvolutionState state, int thread) {
//		// TODO Auto-generated method stub
//		System.err.print("generateInstrBasedAT is a null method in ATCrossover4SLGPSR\n");
//	}
}
