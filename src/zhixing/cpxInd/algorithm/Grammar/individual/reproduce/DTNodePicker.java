package zhixing.cpxInd.algorithm.Grammar.individual.reproduce;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.DerivationRule;
import zhixing.cpxInd.algorithm.Grammar.individual.DTNode;

public class DTNodePicker {
	public int startPick=0; //startPick: starting picking index of repeated sibling or instructions
	public int picknum=0; //picknum: the number of the repeated siblings or instructions that are picked 
	public int pickRepeatnum = 0;  //the number of the repeated patterns that are picked
	public int seglen=0; //seglen: the selected instruction segment length
	
	public double cycle = 1; //cycle: the number of sibling for the to-be-picked repeat pattern
	
	public int randomPick(EvolutionState state, int thread, DTNode node, boolean pickeff) {
		
		if(node == null) {
			System.err.print("we cannot pick anything from a null DTNode");
			System.exit(1);
		}
		for(int t = 0; t<node.maxrepeatnum; t++) {
			//pick the siblings or instructions from recNode		
			if(node.moduleName.equals(DerivationRule.INSTRUCTION)) {
				startPick = state.random[thread].nextInt(node.getLeavesNum());
				if(node.maxrepeatnum==1) {
					pickRepeatnum = picknum = seglen = 1;
				}
				else {
					pickRepeatnum = picknum = seglen = state.random[thread].nextInt(node.getLeavesNum()-startPick+1);
				}
			}
			else {
				
//				cycle = (node.siblings.size() / (double)node.repeatnum); //each repeat time, a number of sibling will be appended
				cycle = 0;//the length of fisrt repeat pattern
				
				startPick = state.random[thread].nextInt(node.repeatnum);
				if(node.maxrepeatnum==1) {
					pickRepeatnum = 1;
				}
				else {
					pickRepeatnum = state.random[thread].nextInt(node.repeatnum - startPick+1);
				}
					
				
				picknum = 0;
				for(int i = startPick; i<startPick+pickRepeatnum; i++) {
					if(i+1==node.repeatnum) {
						picknum += node.siblings.size() - node.siblings.indexOf(node.StartNode.get(i));
					}
					else {
						picknum += node.siblings.indexOf(node.StartNode.get(i+1)) - node.siblings.indexOf(node.StartNode.get(i));
					}
				}
				
				if(startPick + 1 == node.repeatnum) {
					cycle = node.siblings.size() - node.siblings.indexOf(node.StartNode.get(startPick));
				}
				else {
					cycle = node.siblings.indexOf(node.StartNode.get(startPick+1)) - node.siblings.indexOf(node.StartNode.get(startPick));
				}
				
				//startPick *= cycle; //the first node of each repeat
				//picknum *= cycle;
				
				startPick = node.siblings.indexOf(node.StartNode.get(startPick)); //change to the index in siblings
				
				
				seglen = 0;
				for(int i = startPick; i<startPick + picknum; i++) {
					seglen+= node.siblings.get(i).getLeavesNum();
				}
				
			}
			
			if(pickeff) {
				boolean found = false;
				for(int i = startPick; i<startPick + picknum; i++) {
					if(!node.moduleName.equals(DerivationRule.INSTRUCTION) && node.siblings.get(i).containEffectiveInstruction()
						|| 	node.moduleName.equals(DerivationRule.INSTRUCTION) && node.instructionChildren.get(i).status) {
						found = true; break;
					}
				}
				if(found) break;
			}
			else break;
		}
		
		
		return seglen;
	}
}
