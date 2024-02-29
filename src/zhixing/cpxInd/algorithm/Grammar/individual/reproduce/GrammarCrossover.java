package zhixing.cpxInd.algorithm.Grammar.individual.reproduce;

import java.util.ArrayList;

import org.spiderland.Psh.booleanStack;
import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.DerivationRule;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.Grammarrules;
import zhixing.cpxInd.algorithm.Grammar.individual.DTNode;
import zhixing.cpxInd.algorithm.Grammar.individual.GPTreeStructGrammar;
import zhixing.cpxInd.algorithm.Grammar.individual.GrammarLGPDefaults;
import zhixing.cpxInd.algorithm.Grammar.individual.LGPIndividual4Grammar;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.reproduce.LGP2PointCrossoverPipeline;

public class GrammarCrossover extends LGP2PointCrossoverPipeline {
	
	public static final String P_GRAMMARCROSSOVER = "grammarcrossover";
	public static final String P_GROWRATE = "growrate";
	public static final String P_BUILDER = "build";
	public static final String P_EXTENDTHRESOLD = "thresold";   //the number of instructions that is regarded as extendable

	
	protected GPNodeBuilder builder;
	protected double growrate = 0;
	protected int thresold = 1;

	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state,base);
		
		Parameter def = GrammarLGPDefaults.base().push(P_GRAMMARCROSSOVER);
		Parameter p = base.push(P_BUILDER).push(""+0);
		Parameter d = def.push(P_BUILDER).push(""+0);
		
		builder = (GPNodeBuilder)
	            (state.parameters.getInstanceForParameter(
	                    p,d, GPNodeBuilder.class));
	        builder.setup(state,p);
	        
        growrate = state.parameters.getDoubleWithDefault(base.push(P_GROWRATE), def.push(P_GROWRATE), 0.8);
		 if (growrate < 0)
	            state.output.fatal("Grammar LGP crossover Pipeline has an invalid number of grow rate (it must be >= 0).",base.push(P_GROWRATE), def.push(P_GROWRATE));
		 
		 thresold = state.parameters.getIntWithDefault(base.push(P_EXTENDTHRESOLD), def.push(P_EXTENDTHRESOLD), 1);
			if (growrate < 0)
		            state.output.fatal("Grammar LGP crossover Pipeline has an invalid number of extendable thresold (it must be >= 1).",base.push(P_EXTENDTHRESOLD), def.push(P_EXTENDTHRESOLD));
	}
	
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread,
	        final LGPIndividual[] parents) 

	        {
	        // how many individuals should we make?
	        int n = typicalIndsProduced();
	        if (n < min) n = min;
	        if (n > max) n = max;

	        // should we bother?
	        if (!state.random[thread].nextBoolean(likelihood))
	            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

	        GPInitializer initializer = ((GPInitializer)state.initializer);
	        
	        for(int q=start, parnt = 0;q<n+start; /* no increment */)  // keep on going until we're filled up
	            {   	
	            
	            // at this point, parents[] contains our two selected individuals
	            
	        	// are our tree values valid?
	            if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= ((LGPIndividual)parents[0]).getTreesLength()))
	                // uh oh
	                state.output.fatal("LGP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
	            if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= ((LGPIndividual)parents[1]).getTreesLength()))
	                // uh oh
	                state.output.fatal("LGP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

	            int t1=0, t2=0;
	            LGPIndividual j1, j2;
	            if(((LGPIndividual)parents[parnt]).getTreesLength() <= ((LGPIndividual)parents[(parnt + 1)%parents.length]).getTreesLength()) {
	            	j1 = ((LGPIndividual4Grammar)parents[parnt]).lightClone();
	            	t1 = parnt;
	                j2 = ((LGPIndividual4Grammar)parents[(parnt + 1)%parents.length]).lightClone();
	                t2 = (parnt + 1)%parents.length;
	            }
	            else {
	            	j2 = ((LGPIndividual4Grammar)parents[parnt]).lightClone();
	            	t2 = parnt;
	                j1 = ((LGPIndividual4Grammar)parents[(parnt + 1)%parents.length]).lightClone();
	                t1 = (parnt + 1)%parents.length;
	            }
	            
	            //randomly select one node from parent2 (serve as donor), randomly select one node with the same type from j1 (serve as receiver)
	            DTNode donorNode=null, recNode=null;
	            
	            DTNodePicker donorPicker=new DTNodePicker(), recPicker = new DTNodePicker();
	            
	            int pickNum1 = state.random[thread].nextInt(Math.min(parents[t2].getTreesLength(), MaxSegLength)) + 1;
//	            int pickNum2 = 1 + state.random[thread].nextInt(Math.min(j1.getTreesLength(), MaxSegLength));
	            
	            for(int tr = 0; tr<numTries && !checkPoints((LGPIndividual4Grammar)parents[t2], (LGPIndividual4Grammar) j1, donorNode, recNode, donorPicker, recPicker); tr++) {
	            	donorNode=null;
	            	recNode=null;
	            	
	            	pickNum1 = state.random[thread].nextInt(Math.min(parents[t2].getTreesLength(), MaxSegLength)) + 1;
//	            	pickNum2 = 1 + state.random[thread].nextInt(Math.min(j1.getTreesLength(), MaxSegLength));
	            	
	            	if(state.random[thread].nextDouble()<growrate) {
	            		donorNode = ((LGPIndividual4Grammar)parents[t2]).getDerivationTree().randomlyPickExtendNode(state, thread, null, thresold, true);
		            	
	            		if(donorNode!=null)
		            		recNode = ((LGPIndividual4Grammar)j1).getDerivationTree().randomlyPickCompatibleNode(state, thread, donorNode, true);
	            	}
	            	else {
	            		//donorNode = ((LGPIndividual4Grammar)parents[t2]).getDerivationTree().randomlyPickNode(state, thread, null);
	            		donorNode = ((LGPIndividual4Grammar)parents[t2]).getDerivationTree().randomlyPickNodeLength(state, thread, null, pickNum1, true);
		            	
		            	if(donorNode!=null)
		            		recNode = ((LGPIndividual4Grammar)j1).getDerivationTree().randomlyPickCompatibleNode(state, thread, donorNode, true);
		            	
	            	}

	            	if(recNode != null) {
	            		
	            		//pick the siblings or instructions from donorNode
	            		donorPicker.randomPick(state, thread, donorNode, false);
	            		
	            		//pick the siblings or instructions from recNode
	            		recPicker.randomPick(state, thread, recNode, false);
	            		
	            	}
	            }
	            
	            if(checkPoints((LGPIndividual4Grammar)parents[t2], (LGPIndividual4Grammar) j1, donorNode, recNode, donorPicker, recPicker)) {
	            	
	            	//swap
		            DTNode handleDtNode = swapDTreeNInstruction((LGPIndividual4Grammar)parents[t2], (LGPIndividual4Grammar) j1, donorNode, recNode, donorPicker, recPicker);
		            
		            //adapt the instructions
	            	adaptInstruction(state, thread, (LGPIndividual4Grammar) j1, handleDtNode, ((LGPIndividual4Grammar)j1).getDerivationTree().root);
	            	
	            	j1.evaluated = false;
	            	
	            	if(!((LGPIndividual4Grammar)j1).checkDTreeNInstr()) {
		            	System.err.print("something wrong after crossover breeding\n");
		            	System.exit(1);
		            }
	            }
	            //if recNode is still null,   only micro mutation...

	            if(microMutation != null) j1 = (LGPIndividual) microMutation.produce(subpopulation, j1, state, thread);
	            if(eff_flag) {
	            	System.err.print("Sorry, GrammarCrossover does not support effective crossover (i.e., removing introns after swapping)\n");
	            	System.exit(1);
	            }
	            	            
	            
	            if (n-(q-start)>=2 && !tossSecondParent) {
	            	donorNode=null; 
	            	recNode=null;
	            	donorPicker=new DTNodePicker(); 
	            	recPicker=new DTNodePicker();
	            	for(int tr = 0; tr<numTries && !checkPoints((LGPIndividual4Grammar)parents[t1], (LGPIndividual4Grammar) j2, donorNode, recNode, donorPicker, recPicker); tr++) {
	            		
	            		donorNode=null;
		            	recNode=null;
		            	
		            	pickNum1 = state.random[thread].nextInt(Math.min(parents[t1].getTreesLength(), MaxSegLength)) + 1;
//		            	pickNum2 = 1 + state.random[thread].nextInt(Math.min(j2.getTreesLength(), MaxSegLength));
		            	
		            	if(state.random[thread].nextDouble()<growrate) {
		            		donorNode = ((LGPIndividual4Grammar)parents[t1]).getDerivationTree().randomlyPickExtendNode(state, thread, null, thresold, true);
			            	
			            	if(donorNode!=null)
			            		recNode = ((LGPIndividual4Grammar)j2).getDerivationTree().randomlyPickCompatibleNode(state, thread, donorNode, true);
		            	}
		            	else {
//		            		donorNode = ((LGPIndividual4Grammar)parents[t1]).getDerivationTree().randomlyPickNode(state, thread, null);
		            		donorNode = ((LGPIndividual4Grammar)parents[t1]).getDerivationTree().randomlyPickNodeLength(state, thread, null, pickNum1, true);
			            	
			            	if(donorNode!=null)
			            		recNode = ((LGPIndividual4Grammar)j2).getDerivationTree().randomlyPickCompatibleNode(state, thread, donorNode, true);
		            	}
		            	
		            	
		            	if(recNode != null) {
		            		
		            		//pick the siblings or instructions from donorNode
		            		donorPicker.randomPick(state, thread, donorNode, false);
		            		
		            		//pick the siblings or instructions from recNode
		            		recPicker.randomPick(state, thread, recNode, false);
		            		
		            	}
		            }
		            
		            if(checkPoints((LGPIndividual4Grammar)parents[t1], (LGPIndividual4Grammar) j2, donorNode, recNode, donorPicker, recPicker)) {            	
		            	//swap
			            DTNode handleDtNode = swapDTreeNInstruction((LGPIndividual4Grammar)parents[t1], (LGPIndividual4Grammar) j2, donorNode, recNode, donorPicker, recPicker);
			            
			            //adapt the instructions
		            	adaptInstruction(state, thread, (LGPIndividual4Grammar)j2, handleDtNode, ((LGPIndividual4Grammar)j2).getDerivationTree().root);
		            	
		            	j2.evaluated = false;
		            	
		            	if(!((LGPIndividual4Grammar)j2).checkDTreeNInstr()) {
			            	System.err.print("something wrong after crossover breeding\n");
			            	System.exit(1);
			            }
		            }
		            //if recNode is still null,   only micro mutation...
		            
	            	if(microMutation != null) j2 =  (LGPIndividual) microMutation.produce(subpopulation, j2, state, thread);
	            	if(eff_flag) {
		            	System.err.print("Sorry, GrammarCrossover does not support effective crossover (i.e., removing introns after swapping)\n");
		            	System.exit(1);
		            }
	            	
	            }
	            
	            // add the individuals to the population
	            if(j1.getTreesLength() < j1.getMinNumTrees() || j1.getTreesLength() > j1.getMaxNumTrees()){
	            	state.output.fatal("illegal tree number in grammar cross j1");
	            }
	            inds[q] = j1;
	            q++;
	            parnt ++;
	            if (q<n+start && !tossSecondParent)
	            {
	            	if(j2.getTreesLength() < j2.getMinNumTrees() || j2.getTreesLength() > j2.getMaxNumTrees()){
	                	state.output.fatal("illegal tree number in grammar cross j2");
	                }
		            inds[q] = j2;
		            q++;
		            parnt ++;
	            }
	            
	            }
	            
	        return n;
	        }

	//define how to select instruction segments based on the grammar trees
	public DTNode swapDTreeNInstruction(LGPIndividual4Grammar donorInd, LGPIndividual4Grammar recInd, DTNode donorsubtree, DTNode recsubtree, DTNodePicker donorpick, DTNodePicker recpick) {
		//identify the instruction segments based the donor and old sub DTrees, formming two independent ArrayList
		
		ArrayList<GPTreeStructGrammar> donorList = new ArrayList(); //donorInd.getInstructionList(donorsubtree);
		ArrayList<GPTreeStructGrammar> recList = new ArrayList();  //recInd.getInstructionList(recsubtree);

		if( !donorInd.getDerivationTree().contains_pointer(donorsubtree, donorInd.getDerivationTree().root)
				|| !recInd.getDerivationTree().contains_pointer(recsubtree, recInd.getDerivationTree().root)) {
			System.err.print("the given DTNodes are not nodes in the given individuals in replaceDTreeNInstruction\n");
			System.exit(1);
		}
		
		//swap the DTrees
		DTNode handle = recsubtree;
		
		if((recsubtree.moduleName.equals(DerivationRule.INSTRUCTION)
				&& donorsubtree.moduleName.equals(DerivationRule.INSTRUCTION))) {
			
			//prepare donorList and recList
			for(int i = donorpick.startPick; i<donorpick.startPick+donorpick.seglen; i++) {
				donorList.add(donorsubtree.instructionChildren.get(i));
			}
			for(int j = recpick.startPick; j<recpick.startPick+recpick.seglen; j++) {
				recList.add(recsubtree.instructionChildren.get(j));
			}
			
			//because INSTRUCTION module has no siblings but have instruction children, so it does not need replaceDTree
			recsubtree.repeatnum = recsubtree.repeatnum - recpick.seglen + donorpick.seglen;
			
		}
		else { //both recsubtree and donorsubtree are the same kind of modules
			
			//prepare donorList and recList
			for(int i = donorpick.startPick; i<donorpick.startPick+donorpick.picknum; i++) {
				donorList.addAll(donorInd.getInstructionList(donorsubtree.siblings.get(i)));
			}
			for(int j = recpick.startPick; j<recpick.startPick+recpick.picknum; j++) {
				recList.addAll( recInd.getInstructionList(recsubtree.siblings.get(j)));
			}
			
			//remove sibling trees of recsubtree
//			for(int j = 0; j<recpick.picknum; j++) {
//				recsubtree.siblings.remove(recpick.startPick);
//			}
			recsubtree.removeSiblingsNStartNode(recpick.startPick, recpick.picknum);
			
			//add the clone of sibling trees from donorsubtree into recsubtree
			for(int i = 0; i<donorpick.picknum; i++) {
				//recsubtree.siblings.add(recpick.startPick+i, (DTNode) donorsubtree.siblings.get(donorpick.startPick+i).clone());
				//update repeatnum
				recsubtree.addSiblingsNStartNode(recpick.startPick+i, 
						(DTNode) donorsubtree.siblings.get(donorpick.startPick+i).clone(), 
						donorsubtree.StartNode.contains(donorsubtree.siblings.get(donorpick.startPick+i)));
			}
			
			recsubtree.repeatnum = recsubtree.repeatnum - recpick.pickRepeatnum + donorpick.pickRepeatnum;
			
//			//if the siblings of recsubtree and donorsubtree are compactible (the same type), swap based on DTNodePicker
//			if(recsubtree.sibling_equalsto(donorsubtree)) {
//
//				//prepare donorList and recList
//				for(int i = donorpick.startPick; i<donorpick.startPick+donorpick.picknum; i++) {
//					donorList.addAll(donorInd.getInstructionList(donorsubtree.siblings.get(i)));
//				}
//				for(int j = recpick.startPick; j<recpick.startPick+recpick.picknum; j++) {
//					recList.addAll( recInd.getInstructionList(recsubtree.siblings.get(j)));
//				}
//				
//				//remove sibling trees of recsubtree
//				for(int j = 0; j<recpick.picknum; j++) {
//					recsubtree.siblings.remove(recpick.startPick);
//				}
//				
//				//add the clone of sibling trees from donorsubtree into recsubtree
//				for(int i = 0; i<donorpick.picknum; i++) {
//					recsubtree.siblings.add(recpick.startPick+i, (DTNode) donorsubtree.siblings.get(donorpick.startPick+i).clone());
//					//update repeatnum
//				}
//				
//				recsubtree.repeatnum = recsubtree.repeatnum - recpick.picknum + donorpick.picknum;
//				
//				
//			}
//			//if the siblings of recsubtree and donorsubtree are incompactible (different type), swap donorsubtree and recsubtree straightly
//			else {
//				donorList = donorInd.getInstructionList(donorsubtree);
//				recList = recInd.getInstructionList(recsubtree);
//				
//				handle = recInd.getDerivationTree().root.replaceDTree((DTNode) donorsubtree.clone(), recsubtree);
//				if(handle == null) {
//					System.err.print("we cannot find the old sub derivation tree in grammar crossover, akward!\n");
//					System.exit(1);
//				}
//				
//			}
			
			
		}
		
		if(donorList.size() == 0 && recList.size() == 0) {
			System.err.print("we got two empty instruction lists in crossover, akward!\n");
			System.exit(1);
		}
		
		//insert instructions into original lists
		//remove the instructions linked with old sub tree
		int index = -1;
		if(recList.size()>0) {
			index = recInd.getTreeStructs().indexOf(recList.get(0));
		}
		else {
//			index = recInd.getTreeStructs().indexOf(
//					recsubtree.instructionChildren.get(recsubtree.instructionChildren.size()-1)
//					)+1;
			index = findLastInstrIndex(recsubtree, -1, recInd)+1;
		}
			
		
		for(GPTreeStructGrammar t : recList) {
			recInd.getTreeStructs().remove(t);
		}

		//add the instructions linked with donor sub tree into donorInd
		for(int i = donorList.size()-1; i>=0; i--) {
			
			GPTreeStruct instr = (GPTreeStruct) donorList.get(i).clone();
			
			instr.owner = recInd;
 			instr.child.parent = instr;
			
			recInd.getTreeStructs().add(index, instr);

		}
		
		recInd.getDerivationTree().rematch(recInd);
		
		return handle;
	}
	
	protected DTNode adaptInstruction(EvolutionState state, int thread, LGPIndividual4Grammar ind, DTNode handle, DTNode curpar) {
		//do some adaptation on instructions based on the new derivation tree since some values of the constraint modules might be changed
		//handle: the Derivation tree that has been variated
		//curpar: the current parent node
		
		DTNode res = null;
		
		for(DTNode node : curpar.siblings) {
			if(node == handle) {				
				node.updateByPassValues(state, thread, curpar, ind, builder);
				res = handle; //mark that we have updated the instructions
				break;
			}
			else {
				res = adaptInstruction(state, thread, ind, handle, node);
				if(res != null) break;
			}
		}
		
		return res;
	}
	
	protected boolean checkPoints(LGPIndividual4Grammar donorInd, LGPIndividual4Grammar recInd, DTNode donorDtNode, DTNode recDtNode, DTNodePicker donorpick, DTNodePicker recpick) {
		//check the selected nodes are not null and are equivalent.
		if(recDtNode == null || donorDtNode == null || donorpick == null || recpick == null) {/*System.out.println(0);*/  return false;}
		
		if(recDtNode!=null && !donorDtNode.compatiableto(recDtNode)) {/*System.out.println(1);*/  return false;}
		
		//if the two selected instruction segments are the same, return false
		ArrayList<GPTreeStructGrammar> donorlist = donorInd.getInstructionList(donorDtNode);
		ArrayList<GPTreeStructGrammar> reclist = recInd.getInstructionList(recDtNode);
		
		if(donorlist.size()==reclist.size()) {
			boolean sameInsSeg = true;
			for(int i = 0; i< donorlist.size(); i++) {
				sameInsSeg = sameInsSeg && donorlist.get(i).toString().equals(reclist.get(i).toString()); 
			}
			if(sameInsSeg) {/*System.out.println(2);*/  return false;};
		}
		
		int donorSegment = donorpick.seglen, recSegment = recpick.seglen; //recDtNode.getLeavesNum();
		
		if(donorSegment == 0 && recSegment == 0) {/*System.out.println(3);*/  return false;};
		
		if(recDtNode.moduleName.equals(DerivationRule.INSTRUCTION)) {
			if(recDtNode.repeatnum - recSegment + donorSegment < 1 
					|| recDtNode.repeatnum - recSegment + donorSegment > recDtNode.maxrepeatnum) {/*System.out.println(4);*/  return false;};
		}
		else {
			if(recDtNode.repeatnum - recpick.pickRepeatnum + donorpick.pickRepeatnum < 1
					|| recDtNode.repeatnum - recpick.pickRepeatnum + donorpick.pickRepeatnum > recDtNode.maxrepeatnum) {/*System.out.println(5);*/  return false;};
		}
		//they should obey MaxLenDiffSeg, MaxSegLength. (We DO NOT consider MaxDistanceCrossPoint since we already have grammar guided
		if(Math.abs(recSegment-donorSegment)>MaxLenDiffSeg
				|| donorSegment > MaxSegLength
				|| recSegment > MaxSegLength) {/*System.out.println(6);*/  return false;};
		
		//the program length should range between maximum and minimum program size
		if(recInd.getTreesLength() - recSegment + donorSegment > recInd.getMaxNumTrees()
				|| recInd.getTreesLength() - recSegment + donorSegment < recInd.getMinNumTrees()) {/*System.out.println(7);*/  return false;};
		
		
		
		{/*System.out.println(8);*/  return true;}
	}
	
	
	protected int findLastInstrIndex(DTNode node, int childInd, LGPIndividual4Grammar ind) {
		//find the index of the instruction that is specified by childInd th child, in the LGP instruction list
		if(node.moduleName.equals(DerivationRule.INSTRUCTION)) {
			GPTreeStruct target;
			if(childInd >=0)
				target = node.instructionChildren.get(childInd);
			else {
				target = node.instructionChildren.get(node.instructionChildren.size()-1);
			}
			
			int index = ind.getTreeStructs().indexOf(target); //since the cycle of INSTRUCTION DTNode is always 1.0
			
			if(index < 0) {
				System.err.print("we cannot find the last instruction index for the given DTNode");
				System.exit(1);
			}
			return index;
		}
		else {  //module
			if(childInd >=0) {
				return findLastInstrIndex(node.siblings.get(childInd), -1, ind );
			}
			else {
				return findLastInstrIndex(node.siblings.get(node.siblings.size()-1), -1, ind );
			}
		}
	}
}
