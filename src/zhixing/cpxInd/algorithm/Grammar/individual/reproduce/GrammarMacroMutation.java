package zhixing.cpxInd.algorithm.Grammar.individual.reproduce;

import java.io.ObjectInputFilter.Status;
import java.util.ArrayList;

import org.spiderland.Psh.intStack;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.Grammar.EvolutionState.EvolutionState4Grammar;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.DerivationRule;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.Grammarrules;
import zhixing.cpxInd.algorithm.Grammar.individual.DTNode;
import zhixing.cpxInd.algorithm.Grammar.individual.GPTreeStructGrammar;
import zhixing.cpxInd.algorithm.Grammar.individual.GrammarLGPDefaults;
import zhixing.cpxInd.algorithm.Grammar.individual.InstructionBuilder;
import zhixing.cpxInd.algorithm.Grammar.individual.LGPIndividual4Grammar;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.FlowOperator;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMacroMutationPipeline;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public class GrammarMacroMutation extends LGPMacroMutationPipeline{
	
	//mutate the DTree and then generate new instructions
	//to limit the step size,  we constraint that the operator can only mutate the leaves of DT trees 1) change the repeat number
	//2) use a different derivation rule
	
	//randomly select DTNode whose maxrepeatnum > repeatnum
	
	public static final String P_GRAMMARMACROMUTATION = "grammarmacromutate";
	public static final String P_GROWRATE = "growrate";
	public static final String P_EXTENDTHRESOLD = "thresold";   //the number of instructions that is regarded as extendable
	
	public static final String P_REPLACE = "prob_replace";
	public static final String P_EFFFLOW = "effective_flow_operation";
	
	protected double growrate = 0;
	protected int thresold = 1;
	
	public double probReplace;
	
	public boolean effflow = true;
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state,base);
		
		Parameter def = GrammarLGPDefaults.base().push(P_GRAMMARMACROMUTATION);
		
		growrate = state.parameters.getDoubleWithDefault(base.push(P_GROWRATE), def.push(P_GROWRATE), 0.8);
		if (growrate < 0)
	            state.output.fatal("Grammar LGP MacroMutation Pipeline has an invalid number of grow rate (it must be >= 0).",base.push(P_GROWRATE), def.push(P_GROWRATE));
		 
		probReplace = state.parameters.getDouble(base.push(P_REPLACE), def.push(P_REPLACE), 0.0);
		 if (probReplace < 0)
	            state.output.fatal("Grammar LGP MacroMutation Pipeline has an invalid number of prob_replace (it must be >= 0).",base.push(P_REPLACE),def.push(P_REPLACE));
		
		thresold = state.parameters.getIntWithDefault(base.push(P_EXTENDTHRESOLD), def.push(P_EXTENDTHRESOLD), 1);
		if (thresold < 0)
	            state.output.fatal("Grammar LGP MacroMutation Pipeline has an invalid number of extendable thresold (it must be >= 1).",base.push(P_EXTENDTHRESOLD), def.push(P_EXTENDTHRESOLD));
		
		effflow = state.parameters.getBoolean(base.push(P_EFFFLOW), def.push(P_EFFFLOW), true);
//		if (growrate < 0)
//	            state.output.fatal("Grammar LGP MacroMutation Pipeline has an invalid number of grow rate (it must be >= 0).",base.push(P_EFFFLOW), def.push(P_EFFFLOW));
	}
	
	public LGPIndividual produce(
			final int subpopulation,
	        final LGPIndividual ind,
	        final EvolutionState state,
	        final int thread) {
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		LGPIndividual i = ind;
		
		if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.getTreesLength()))
            // uh oh
            state.output.fatal("LGP Mutation Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            
        LGPIndividual j, j_res;

        if (sources[0] instanceof BreedingPipeline)
            // it's already a copy, so just smash the tree in
            {
            j = j_res=(LGPIndividual4Grammar) i;
            }
        else // need to clone the individual
            {
            j_res = ((LGPIndividual4Grammar)i).lightClone();
            
            j = ((LGPIndividual4Grammar)i).lightClone();
            // Fill in various tree information that didn't get filled in there
            //j.renewTrees();
            
            }
        
        
        //stepsize: the number of DTNodes that are updated (the new generated sub DerivationTree is excluded)
    	
    	//pick a random DTNode from the derivation tree whose leavenum is increasable / decreasable (for adding or removing sub-DTree) 
    	//or if its leavenum cannot be increased or decreased, we can regenerate it if its leavenum is smaller than a certain number of instructions 
    	
        boolean res = false;
        
        for(int tr = 0; tr<numTries; tr++) {
        	
        	j = ((LGPIndividual4Grammar)i).lightClone();

        	double rate = state.random[thread].nextDouble();
        	if(rate<probInsert + probDelete + probReplace/*growrate*/) {
        		//mutate the DTree node and its corresponding instructions in each iteration
            	DTNode tomutate; // = ((LGPIndividual4Grammar)j).getDerivationTree().randomlyPickExtendNode(state, thread, null, thresold, false);
            	
            	DTNodePicker picker = new DTNodePicker();
            	
//            	picker.randomPick(state, thread, tomutate);
            	
            	//if tomutate's siblings or instructionChildren cannot be removed  or we decide to add instructions and adding is feasible
//            	if(tomutate.repeatnum < tomutate.maxrepeatnum 
//            			&& (/*state.random[thread].nextDouble()*/ rate < probInsert || tomutate.repeatnum == 1 ) ) 
        		if(rate < probInsert)
            	{
            		
            		tomutate = ((LGPIndividual4Grammar)j).getDerivationTree().randomlyPickExtendNode(state, thread, null, thresold, false);
            		if(tomutate.repeatnum < tomutate.maxrepeatnum) {
            			picker.randomPick(state, thread, tomutate, false);
                		
                		ArrayList<GPTreeStruct> trialtrees = addDTreeNInstruction(state,thread,j,tomutate,picker);
                		
                		//check validity
                		//check whether it is legal (# affected instructions < step size)
                    	if(Math.abs(j.getTreesLength() - i.getTreesLength()) <= stepSize 
//                    			&& Math.abs(j.getTreesLength() - i.getTreesLength()) >= 1
                    			&& checkSegment(state, thread, (LGPIndividual4Grammar) j, trialtrees)) {
                    		res = true;
                    		break;
                    	}
            		}
            		
            	}
            	//else if tomutate's siblings or instructionChildren cannot be added  or  we decide to remove instructions and removing is feasible
//            	if(tomutate.repeatnum > 1 
//            			&& (/*state.random[thread].nextDouble()*/ rate < probInsert + probDelete || tomutate.repeatnum == tomutate.maxrepeatnum ))
            	if(!res && rate < probInsert + probDelete && rate >= probInsert)	
            	{
            		tomutate = ((LGPIndividual4Grammar)j).getDerivationTree().randomlyPickExtendNode(state, thread, null, thresold, true);
            		if(tomutate != null && tomutate.repeatnum > 1 ) {
            			picker.randomPick(state, thread, tomutate, true);
                		
                		removeDTreeNInstruction(state, thread, j, tomutate, picker);
                		
                		//check validity
                		//check whether it is legal (# affected instructions < step size)
                    	if(Math.abs(j.getTreesLength() - i.getTreesLength()) <= stepSize 
                    			/*&& Math.abs(j.getTreesLength() - i.getTreesLength()) >= 1*/
                    			&& checkSegment(state, thread, (LGPIndividual4Grammar) j, null)) {
                    		res = true;

                    		break;
                    	}
            		}
            		
            	}
            	if(!res && rate >= probInsert + probDelete) {
            		tomutate = ((LGPIndividual4Grammar)j).getDerivationTree().randomlyPickFixNode(state, thread, null, thresold, false);
            		
            		picker.randomPick(state, thread, tomutate, false);
            		
            		ArrayList<GPTreeStruct> trialtrees = replaceDTreeNInstruction(state, thread, j, tomutate, picker);
            		
            		if(/*Math.abs(j.getTreesLength() - i.getTreesLength()) <= stepSize 
                			&& */checkSegment(state, thread, (LGPIndividual4Grammar) j, trialtrees)) {
                		res = true;

                		break;
                	}
            		
            		
            	}
        	}
        	
        	
        	else {
        		//here, randomlyPickReplaceNode(.) selects the DTNodes that have more than one corresponding derivation rule
        		//to jump from one derivation rule to another
        		DTNode tomutate = ((LGPIndividual4Grammar)j).getDerivationTree().randomlyPickReplaceNode(state, thread, (LGPIndividual4Grammar) j, false);
        		if(tomutate == null) {
        			tomutate = ((LGPIndividual4Grammar)j).getDerivationTree().randomlyPickNode(state, thread, null, false);
        		}
            	
            	DTNodePicker picker = new DTNodePicker();
            	
            	picker.randomPick(state, thread, tomutate, false);
            	
            	ArrayList<GPTreeStruct> trialtrees = replaceDTreeNInstruction(state, thread, j, tomutate, picker);
        		
        		if(/*Math.abs(j.getTreesLength() - i.getTreesLength()) <= stepSize 
            			&& */checkSegment(state, thread, (LGPIndividual4Grammar) j, trialtrees)) {
            		res = true;

            		break;
            	}
        		
//        		if(!tomutate.moduleName.equals(Grammarrules.PROGRAM)) {  //state.random[thread].nextDouble() > probInsert + probDelete && tomutate.repeatnum == tomutate.maxrepeatnum
//            		//avoid replacing the whole program, regenerating the whole program unlikely gets better programs
//            		
//            	}
        	}
        		
        }
        //if we cannot add or remove, we can apply micro mutation to variate the individual
        
        if(res) {
        	j_res = j;
        }
        
        //some following operations
        switch (mutateFlag) {
		case EFFMACROMUT2:
			break;
		case EFFMACROMUT:
		case FREEMACROMUT:
			if(microMutation != null) j_res = (LGPIndividual) microMutation.produce(subpopulation, j_res, state, thread);
			break;
		case EFFMACROMUT3:
			//delete all non-effective instructions
			//j.removeIneffectiveInstr();
			System.err.print("Grammar macro mutation does not support effmut3\n");
			System.exit(1);

			break;
		default:
			break;
		}
        
        return j_res;
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
	
	protected int findFirstInstrIndex(DTNode node, int childInd, LGPIndividual4Grammar ind) {
		if(node.moduleName.equals(DerivationRule.INSTRUCTION)) {
			
			GPTreeStruct target = node.instructionChildren.get(childInd);			
			
			int index = ind.getTreeStructs().indexOf(target); //since the cycle of INSTRUCTION DTNode is always 1.0
			
			if(index < 0) {
				System.err.print("we cannot find the last instruction index for the given DTNode");
				System.exit(1);
			}
			return index;
		}
		else {  //module
			
			return findFirstInstrIndex(node.siblings.get(childInd), 0, ind);

		}
	}
	
	protected boolean checkSegment(EvolutionState state, int thread, LGPIndividual4Grammar ind, ArrayList<GPTreeStruct> inslist) {
		//check the instruction of inslist, there should be at least one instruction is effective

		if(inslist != null) {
			boolean res = false;
			for(int i = inslist.size()-1; i>=0; i--)
			{
				GPTreeStructGrammar ins = (GPTreeStructGrammar) inslist.get(i);
				
				if(ins.child.children[0] instanceof FlowOperator && ins.status) {
					if(effflow && ((EvolutionState4Grammar)state).getBannedList4FlowInstr().contains(ins.toString()) 
							&& state.random[thread].nextDouble()>0.1 ) {
						return false;
					}
				}

				else
				if(!ins.status && ! (ins.child.children[0] instanceof FlowOperator)) {
					int originIndex = ((WriteRegisterGPNode)ins.child).getIndex();
					DTNode grammarDtNode = ins.grammarNode;
					
					((WriteRegisterGPNode)ins.child).enumerateNode(state, thread);
					ind.updateStatus();
					String primitiveName = ins.child.toString().substring(0, ins.child.toString().length()-1);
			        for(int ii = 0; ii<numTries; ii++) {
			        	if(grammarDtNode.param_value.get(0).contains(primitiveName) && ins.status) {break;}
			        	
			        	//des.resetNode(state,thread);
			        	((WriteRegisterGPNode)ins.child).enumerateNode(state, thread);
			        	ind.updateStatus();
			        	primitiveName = ins.child.toString().substring(0, ins.child.toString().length()-1);
			        }
			        if(!grammarDtNode.param_value.get(0).contains(primitiveName) || !ins.status) {
			        	((WriteRegisterGPNode)ins.child).setIndex(originIndex);
			        	ind.updateStatus();
			        }
				}
				res = res || ins.status;
				
				
			}
			if(!res) return false;
		}

		if(ind.getTreesLength()>ind.getMaxNumTrees() || ind.getTreesLength()<ind.getMinNumTrees()) return false;
		
		return true;
	}
	
	protected ArrayList<GPTreeStruct> addDTreeNInstruction(EvolutionState state, int thread, LGPIndividual j, DTNode tomutate, DTNodePicker picker) {
				
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		int siblingInsertIndex = (int) (picker.startPick + picker.cycle); //the index of new siblings or instructions in sibling list or instructionChildren list
		int insertIndex = 0; //the index of new instructions in the instruction list of the individual

		//GPTreeStructGrammar treePrototype = (GPTreeStructGrammar) j.getTreeStruct(0);
		GPTreeStructGrammar treePrototype = (GPTreeStructGrammar) j.getTreeStruct(0);
 		ArrayList<GPTreeStruct> trialtrees = new ArrayList(); 
 		
		//if the DTNode is an instruction node, no need to regenerate the derivation tree
 		if(tomutate.moduleName.equals(DerivationRule.INSTRUCTION)) {
 			
 			siblingInsertIndex = (int) (picker.startPick)+1;
 			insertIndex = (int) (findLastInstrIndex(tomutate, (int) (picker.startPick), (LGPIndividual4Grammar) j))+1;
    		         			
 			GPTreeStruct instr = ((InstructionBuilder) treePrototype.constraints(initializer).init).genOneInstr(state,
    				treePrototype.constraints(initializer).treetype,
    	            thread,
    	            treePrototype,
    	            treePrototype.constraints(initializer).functionset,
    	            tomutate,
    	            siblingInsertIndex);
 			
 			instr.owner = j;
 			instr.child.parent = instr;
 			
 			trialtrees.add(instr);
 			
 			tomutate.repeatnum ++;
 			
 		}
		//else grow nodes (siblings) based on existing siblings 
		//generate the instruction sequence based on the derivation sub-tree (  by InstructionBuilder.genInstructionSeq(.)  )
 		else {
 			
			//get the prototype list since the newly generated children should have the same type as the existing siblings
 			
 			siblingInsertIndex = (int) (picker.startPick + picker.cycle);
 			insertIndex = (int) (findLastInstrIndex(tomutate, (int) (picker.startPick+picker.cycle-1), (LGPIndividual4Grammar) j) + 1);
 			
			ArrayList<DerivationRule> prototypelist = new ArrayList();
			for(int c = picker.startPick; c< picker.startPick + picker.cycle; c++) {
				prototypelist.add(tomutate.siblings.get(c));
			}
			
			//regenerate the derivation sub-tree for the to-be-mutated DTNode (  by growNodeBasedGram(.)  )
			int numsiblings = 1;
			if(tomutate.moduleName.equals(DerivationRule.COMPOSITE)) {
				tomutate.growNode((LGPIndividual4Grammar) j, 
				state, 
				thread, 
				((LGPIndividual4Grammar)j).getGrammarrules(), 
				tomutate, 
				((LGPIndividual4Grammar)j).getModuleConLibrary(), 
				prototypelist, 
				siblingInsertIndex,
				-1);   //we set cutORgrow as -1 to minimize the variation step size
				
				numsiblings = (int) picker.cycle;
			}

			else {
				numsiblings = tomutate.growNodeBasedGram((LGPIndividual4Grammar) j, 
						state, 
						thread, 
						((LGPIndividual4Grammar)j).getGrammarrules(), 
						tomutate, 
						((LGPIndividual4Grammar)j).getModuleConLibrary(), 
						siblingInsertIndex,
						-1);
			}
			
			
			//generate the instruction list
			for(int si = siblingInsertIndex; 
//					si<siblingInsertIndex + picker.cycle; 
					si<siblingInsertIndex + numsiblings;
					si++) {
				
				trialtrees.addAll(  ((InstructionBuilder) treePrototype.constraints(initializer).init).genInstructionSeq(state,
    				treePrototype.constraints(initializer).treetype,
    	            thread,
    	            treePrototype,
    	            treePrototype.constraints(initializer).functionset,
    	            tomutate.siblings.get(si))  );
				
			}
			
			tomutate.repeatnum ++;

		}

		//add the newly generated instructions to corresponding slot in the instruction list
		for(int in = 0; in<trialtrees.size(); in++) {
			GPTreeStruct instr = trialtrees.get(in);
			
			instr.owner = j;
 			instr.child.parent = instr;
			
			j.addTree(insertIndex+in, instr);
		}
		
		j.evaluated = false;
		
		((LGPIndividual4Grammar) j).getDerivationTree().rematch((LGPIndividual4Grammar) j);
		
		if(!((LGPIndividual4Grammar)j).checkDTreeNInstr()) {
        	System.err.print("something wrong after macro mutation breeding\n");
        	System.exit(1);
        }
		
		return trialtrees;
	}
	
	protected void removeDTreeNInstruction(EvolutionState state, int thread, LGPIndividual j, DTNode tomutate, DTNodePicker picker) {
		
		if(tomutate.moduleName.equals(DerivationRule.INSTRUCTION)) {
			//remove one instruction directly

			int di = state.random[thread].nextInt(tomutate.instructionChildren.size());
			if(! j.getTreeStructs().remove(tomutate.instructionChildren.get(di))) {
				System.err.print("removing failed in GrammarMacroMutation\n");
				System.exit(1);
			}
			
			tomutate.instructionChildren.remove(di);
			
			if(tomutate.instructionChildren.size() == 0) {
				System.err.print("We empty all the instructionChildren of a node when removing instructions in GrammarMacroMutation\n");
				System.exit(1);
			}
			
			tomutate.repeatnum --;
		}
		else {
			//collect the sibling DTNodes that are going to be removed (the last cycle)
			ArrayList<DTNode> removelist = new ArrayList();
			for(int di = picker.startPick; di<picker.startPick+picker.cycle; di++) {
				removelist.add(tomutate.siblings.get(di));
			}
			
			for(DTNode re : removelist) {
				//remove instructions based on derivation tree
    			((LGPIndividual4Grammar)j).removeTreeBasedDTNode(re);

			}
			
			if(! tomutate.removeSiblingsNStartNode(picker.startPick, (int) picker.cycle)) {
				System.err.print("removing tomutate.siblings failed in GrammarMacroMutation\n");
				System.exit(1);
			}
			if(tomutate.siblings.size() == 0) {
				System.err.print("We empty all the siblings of a node when removing tomutate.siblings in GrammarMacroMutation\n");
				System.exit(1);
			}
			
			tomutate.repeatnum --;
		}
		
		j.evaluated = false;
		
//		if(tomutate.moduleName.equals(DerivationRule.INSTRUCTION)) {
//			
//			if(tomutate.instructionChildren.size() == 0) {
//				System.err.print("We empty all the instructionChildren of a node when removing instructions in GrammarMacroMutation\n");
//				System.exit(1);
//			}
//			
//		}
//		else {
//
//			if(tomutate.siblings.size() == 0) {
//				System.err.print("We empty all the siblings of a node when removing tomutate.siblings in GrammarMacroMutation\n");
//				System.exit(1);
//			}
//			
//		}
		
		((LGPIndividual4Grammar) j).getDerivationTree().rematch((LGPIndividual4Grammar) j);
		
		if(!((LGPIndividual4Grammar)j).checkDTreeNInstr()) {
        	System.err.print("something wrong after macro mutation breeding\n");
        	System.exit(1);
        }
	}
	
	protected ArrayList<GPTreeStruct> replaceDTreeNInstruction(EvolutionState state, int thread, LGPIndividual j, DTNode tomutate, DTNodePicker picker){
		
		int siblingInsertIndex = 0; //the index of new siblings or instructions in sibling list or instructionChildren list
		int insertIndex = 0; //the index of new instructions in the instruction list of the individual
		GPTreeStructGrammar treePrototype = (GPTreeStructGrammar) j.getTreeStruct(0);
		
		ArrayList<DerivationRule> prototypelist = new ArrayList();
		
		
		//======================remove===========================
		if(tomutate.moduleName.equals(DerivationRule.INSTRUCTION)) {
			//remove one instruction directly

			int di = state.random[thread].nextInt(tomutate.instructionChildren.size());
			
			siblingInsertIndex = di;
			insertIndex = j.getTreeStructs().indexOf(tomutate.instructionChildren.get(di));
			
			if(! j.getTreeStructs().remove(tomutate.instructionChildren.get(di))) {
				System.err.print("removing failed in GrammarMacroMutation\n");
				System.exit(1);
			}
			
			tomutate.instructionChildren.remove(di);
			
//			if(tomutate.instructionChildren.size() == 0) {
//				System.err.print("We empty all the instructionChildren of a node when removing instructions in GrammarMacroMutation\n");
//				System.exit(1);
//			}
			
			tomutate.repeatnum --;
		}
		else {
			//collect the sibling DTNodes that are going to be removed (the last cycle)
			ArrayList<DTNode> removelist = new ArrayList();
			for(int di = picker.startPick; di<picker.startPick+picker.cycle; di++) {
				removelist.add(tomutate.siblings.get(di));
			}
			
			for(int c = picker.startPick; c< picker.startPick + picker.cycle; c++) {
				prototypelist.add(tomutate.siblings.get(c));
			}
			
			siblingInsertIndex = picker.startPick;
			insertIndex = findFirstInstrIndex(tomutate, picker.startPick, (LGPIndividual4Grammar) j);
			
			for(DTNode re : removelist) {
				//remove instructions based on derivation tree
    			((LGPIndividual4Grammar)j).removeTreeBasedDTNode(re);

			}
			
			if(! tomutate.removeSiblingsNStartNode(picker.startPick, (int) picker.cycle)) {
				System.err.print("removing tomutate.siblings failed in GrammarMacroMutation\n");
				System.exit(1);
			}
//			if(tomutate.siblings.size() == 0) {
//				System.err.print("We empty all the siblings of a node when removing tomutate.siblings in GrammarMacroMutation\n");
//				System.exit(1);
//			}
			
			tomutate.repeatnum --;
		}
		
		j.evaluated = false;
		
		
		//=====================add============================
		GPInitializer initializer = ((GPInitializer)state.initializer);

		//GPTreeStructGrammar treePrototype = (GPTreeStructGrammar) j.getTreeStruct(0);
		
 		ArrayList<GPTreeStruct> trialtrees = new ArrayList(); 
 		
		//if the DTNode is an instruction node, no need to regenerate the derivation tree
 		if(tomutate.moduleName.equals(DerivationRule.INSTRUCTION)) {
 			
// 			siblingInsertIndex = (int) (picker.startPick)+1;
// 			insertIndex = (int) (findLastInstrIndex(tomutate, (int) (picker.startPick), (LGPIndividual4Grammar) j))+1;
    		         			
 			GPTreeStruct instr = ((InstructionBuilder) treePrototype.constraints(initializer).init).genOneInstr(state,
    				treePrototype.constraints(initializer).treetype,
    	            thread,
    	            treePrototype,
    	            treePrototype.constraints(initializer).functionset,
    	            tomutate,
    	            siblingInsertIndex);
 			
 			instr.owner = j;
 			instr.child.parent = instr;
 			
 			trialtrees.add(instr);
 			
 			tomutate.repeatnum ++;
 			
 		}
		//else grow nodes (siblings) based on existing siblings 
		//generate the instruction sequence based on the derivation sub-tree (  by InstructionBuilder.genInstructionSeq(.)  )
 		else {
 			
			//get the prototype list since the newly generated children should have the same type as the existing siblings
 			
// 			siblingInsertIndex = (int) (picker.startPick + picker.cycle);
// 			insertIndex = (int) (findLastInstrIndex(tomutate, (int) (picker.startPick+picker.cycle-1), (LGPIndividual4Grammar) j) + 1);
 			
			
//			//regenerate the derivation sub-tree for the to-be-mutated DTNode (  by growNodeBasedGram(.)  )
//			tomutate.growNode((LGPIndividual4Grammar) j, 
//					state, 
//					thread, 
//					((LGPIndividual4Grammar)j).getGrammarrules(), 
//					tomutate, 
//					((LGPIndividual4Grammar)j).getModuleConLibrary(), 
//					prototypelist, 
//					siblingInsertIndex,
//					-1);   //we set cutORgrow as -1 to minimize the variation step size
			
 			int numsiblings = 1;
			if(tomutate.moduleName.equals(DerivationRule.COMPOSITE)) {
				
				if(prototypelist.size()==0) {
					System.err.println("there is no prototype list for module " + tomutate.moduleName);
					System.exit(1);
				}
				
				tomutate.growNode((LGPIndividual4Grammar) j, 
				state, 
				thread, 
				((LGPIndividual4Grammar)j).getGrammarrules(), 
				tomutate, 
				((LGPIndividual4Grammar)j).getModuleConLibrary(), 
				prototypelist, 
				siblingInsertIndex,
				0);   //we set cutORgrow as -1 to minimize the variation step size
				
				numsiblings = (int) picker.cycle;
			}

			else {
				numsiblings = tomutate.growNodeBasedGram((LGPIndividual4Grammar) j, 
						state, 
						thread, 
						((LGPIndividual4Grammar)j).getGrammarrules(), 
						tomutate, 
						((LGPIndividual4Grammar)j).getModuleConLibrary(), 
						siblingInsertIndex,
						0);
			}
 			
//			int numsiblings = tomutate.growNodeBasedGram((LGPIndividual4Grammar) j, 
//					state, 
//					thread, 
//					((LGPIndividual4Grammar)j).getGrammarrules(), 
//					tomutate, 
//					((LGPIndividual4Grammar)j).getModuleConLibrary(), 
//					siblingInsertIndex,
//					-1);
			
			//generate the instruction list
			for(int si = siblingInsertIndex; 
					si<siblingInsertIndex + numsiblings; 
					si++) {
				
				trialtrees.addAll(  ((InstructionBuilder) treePrototype.constraints(initializer).init).genInstructionSeq(state,
    				treePrototype.constraints(initializer).treetype,
    	            thread,
    	            treePrototype,
    	            treePrototype.constraints(initializer).functionset,
    	            tomutate.siblings.get(si))  );
				
			}
			
			tomutate.repeatnum ++;

		}

		//add the newly generated instructions to corresponding slot in the instruction list
		for(int in = 0; in<trialtrees.size(); in++) {
			GPTreeStruct instr = trialtrees.get(in);
			
			instr.owner = j;
 			instr.child.parent = instr;
			
			j.addTree(insertIndex+in, instr);
		}
		
		j.evaluated = false;
		
		
//		GPTreeStruct prototype = j.getTreeStruct(0);
//		
//		removeDTreeNInstructionNotCheck(state, thread, j, tomutate, picker);
//		
//		ArrayList<GPTreeStruct> trialtrees = addDTreeNInstructionNotCheck(state, thread, j, prototype, tomutate, picker);
//		
		if(tomutate.moduleName.equals(DerivationRule.INSTRUCTION)) {
			
			if(tomutate.instructionChildren.size() == 0) {
				System.err.print("We empty all the instructionChildren of a node when removing instructions in GrammarMacroMutation\n");
				System.exit(1);
			}
			
		}
		else {

			if(tomutate.siblings.size() == 0) {
				System.err.print("We empty all the siblings of a node when removing tomutate.siblings in GrammarMacroMutation\n");
				System.exit(1);
			}
			
		}
		
		((LGPIndividual4Grammar) j).getDerivationTree().rematch((LGPIndividual4Grammar) j);
		
		if(!((LGPIndividual4Grammar)j).checkDTreeNInstr()) {
        	System.err.print("something wrong after macro mutation breeding\n");
        	System.exit(1);
        }
		
		return trialtrees;
	}
	
//	protected ArrayList<GPTreeStruct> addDTreeNInstructionNotCheck(EvolutionState state, int thread, LGPIndividual j, GPTreeStruct prototype, DTNode tomutate, DTNodePicker picker) {
//		GPInitializer initializer = ((GPInitializer)state.initializer);
//		
//		int siblingInsertIndex = (int) (picker.startPick + picker.cycle); //the index of new siblings or instructions in sibling list or instructionChildren list
//		int insertIndex = 0; //the index of new instructions in the instruction list of the individual
//
//		//GPTreeStructGrammar treePrototype = (GPTreeStructGrammar) j.getTreeStruct(0);
//		GPTreeStructGrammar treePrototype = (GPTreeStructGrammar) prototype;
// 		ArrayList<GPTreeStruct> trialtrees = new ArrayList(); 
// 		
//		//if the DTNode is an instruction node, no need to regenerate the derivation tree
// 		if(tomutate.moduleName.equals(DerivationRule.INSTRUCTION)) {
// 			
// 			siblingInsertIndex = (int) (picker.startPick)+1;
// 			insertIndex = (int) (findLastInstrIndex(tomutate, (int) (picker.startPick), (LGPIndividual4Grammar) j))+1;
//    		         			
// 			GPTreeStruct instr = ((InstructionBuilder) treePrototype.constraints(initializer).init).genOneInstr(state,
//    				treePrototype.constraints(initializer).treetype,
//    	            thread,
//    	            treePrototype,
//    	            treePrototype.constraints(initializer).functionset,
//    	            tomutate,
//    	            siblingInsertIndex);
// 			
// 			instr.owner = j;
// 			instr.child.parent = instr;
// 			
// 			trialtrees.add(instr);
// 			
// 			tomutate.repeatnum ++;
// 			
// 		}
//		//else grow nodes (siblings) based on existing siblings 
//		//generate the instruction sequence based on the derivation sub-tree (  by InstructionBuilder.genInstructionSeq(.)  )
// 		else {
// 			
//			//get the prototype list since the newly generated children should have the same type as the existing siblings
// 			
// 			siblingInsertIndex = (int) (picker.startPick + picker.cycle);
// 			insertIndex = (int) (findLastInstrIndex(tomutate, (int) (picker.startPick+picker.cycle-1), (LGPIndividual4Grammar) j) + 1);
// 			
//			ArrayList<DerivationRule> prototypelist = new ArrayList();
//			for(int c = picker.startPick; c< picker.startPick + picker.cycle; c++) {
//				prototypelist.add(tomutate.siblings.get(c));
//			}
//			
//			//regenerate the derivation sub-tree for the to-be-mutated DTNode (  by growNodeBasedGram(.)  )
//			tomutate.growNode((LGPIndividual4Grammar) j, 
//					state, 
//					thread, 
//					((LGPIndividual4Grammar)j).getGrammarrules(), 
//					tomutate, 
//					((LGPIndividual4Grammar)j).getModuleConLibrary(), 
//					prototypelist, 
//					siblingInsertIndex,
//					-1);   //we set cutORgrow as -1 to minimize the variation step size
//			
//			//generate the instruction list
//			for(int si = siblingInsertIndex; 
//					si<siblingInsertIndex + picker.cycle; 
//					si++) {
//				
//				trialtrees.addAll(  ((InstructionBuilder) treePrototype.constraints(initializer).init).genInstructionSeq(state,
//    				treePrototype.constraints(initializer).treetype,
//    	            thread,
//    	            treePrototype,
//    	            treePrototype.constraints(initializer).functionset,
//    	            tomutate.siblings.get(si))  );
//				
//			}
//			
//			tomutate.repeatnum ++;
//
//		}
//
//		//add the newly generated instructions to corresponding slot in the instruction list
//		for(int in = 0; in<trialtrees.size(); in++) {
//			GPTreeStruct instr = trialtrees.get(in);
//			
//			instr.owner = j;
// 			instr.child.parent = instr;
//			
//			j.addTree(insertIndex+in, instr);
//		}
//		
//		j.evaluated = false;
//		
//		return trialtrees;
//	}
//	
//	protected void removeDTreeNInstructionNotCheck(EvolutionState state, int thread, LGPIndividual j, DTNode tomutate, DTNodePicker picker) {
//		
//		if(tomutate.moduleName.equals(DerivationRule.INSTRUCTION)) {
//			//remove one instruction directly
//
//			int di = state.random[thread].nextInt(tomutate.instructionChildren.size());
//			if(! j.getTreeStructs().remove(tomutate.instructionChildren.get(di))) {
//				System.err.print("removing failed in GrammarMacroMutation\n");
//				System.exit(1);
//			}
//			
//			tomutate.instructionChildren.remove(di);
//			
////			if(tomutate.instructionChildren.size() == 0) {
////				System.err.print("We empty all the instructionChildren of a node when removing instructions in GrammarMacroMutation\n");
////				System.exit(1);
////			}
//			
//			tomutate.repeatnum --;
//		}
//		else {
//			//collect the sibling DTNodes that are going to be removed (the last cycle)
//			ArrayList<DTNode> removelist = new ArrayList();
//			for(int di = picker.startPick; di<picker.startPick+picker.cycle; di++) {
//				removelist.add(tomutate.siblings.get(di));
//			}
//			
//			for(DTNode re : removelist) {
//				//remove instructions based on derivation tree
//    			((LGPIndividual4Grammar)j).removeTreeBasedDTNode(re);
////    			if(! tomutate.siblings.remove(re)) {
////    				System.err.print("removing tomutate.siblings failed in GrammarMacroMutation\n");
////    				System.exit(1);
////    			}
////    			if(tomutate.siblings.size() == 0) {
////    				System.err.print("We empty all the siblings of a node when removing tomutate.siblings in GrammarMacroMutation\n");
////    				System.exit(1);
////    			}
//			}
//			
//			if(! tomutate.removeSiblingsNStartNode(picker.startPick, (int) picker.cycle)) {
//				System.err.print("removing tomutate.siblings failed in GrammarMacroMutation\n");
//				System.exit(1);
//			}
////			if(tomutate.siblings.size() == 0) {
////				System.err.print("We empty all the siblings of a node when removing tomutate.siblings in GrammarMacroMutation\n");
////				System.exit(1);
////			}
//			
//			tomutate.repeatnum --;
//		}
//		
//		j.evaluated = false;
//	}
}
