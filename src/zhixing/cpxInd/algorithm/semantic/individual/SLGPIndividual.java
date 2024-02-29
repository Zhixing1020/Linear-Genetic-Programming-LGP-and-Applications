package zhixing.cpxInd.algorithm.semantic.individual;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import zhixing.cpxInd.algorithm.semantic.library.SemanticVector;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;

public abstract class SLGPIndividual extends LGPIndividual{

	//record the semantics for every input during execution
	
	protected SemanticVector inputS = new SemanticVector();
	
	protected SemanticVector targetS = new SemanticVector();
	
	protected int currentDataIndex;
	
	public void setDataIndex(int ind) {
		currentDataIndex = ind;
	}
	
	public SemanticVector getInputS() {
		return inputS;
	}
	
	public SemanticVector getTargetS() {
		return targetS;
	}
	
	abstract public double execute(final EvolutionState state,
	        final int thread,
	        final GPData input,
	        final ADFStack stack,
	        final GPIndividual individual,
	        final Problem problem);
	
	protected void copyLGPproperties(LGPIndividual obj) {
		super.copyLGPproperties(obj);
		inputS = (SemanticVector) ((SLGPIndividual)obj).getInputS().clone();
		targetS = (SemanticVector) ((SLGPIndividual)obj).getTargetS().clone();
	}
	
	public ArrayList<Pair<String, ArrayList<String>>> getAdjacencyTable(int start, int end){
		//return the adjacency table of instruction sequence [start, end), only counts effective instruction
		if(start<0||start>=end||end>getTreelist().size()){
			System.err.print("illegal arguments in getAdjacencyTable() of LGPIndividual4Graph\n");
			System.exit(1);
		}
		
		ArrayList<Pair<String, ArrayList<String>>> res = new ArrayList<>();
		
		for(int i = end - 1; i>=start; i--) {
			//for each instruction, get the index of its function and children/constants
			GPTreeStruct tree = getTreelist().get(i);
			
			if(!tree.status) continue;
			
//			int si,tj;
//			//get the index of its function
//			String name = tree.child.children[0].toString();
//			tj = primitives.indexOf(name);
			
			ArrayList<Pair<String, ArrayList<String>>> tree_AL = getAdjacencyTable(tree.child.children[0], i);
			
//			String check = tree.child.children[0].toString();
//			ArrayList<String> slibings = new ArrayList<>();
//			
//			for(int j = 0; j<tree.child.children[0].expectedChildren(); j++) {
//				
////				if(tree.child.children[0].children[j] instanceof TerminalERC) 
//				if(! (tree.child.children[0].children[j] instanceof ReadRegisterGPNode) 
//						&& tree.child.children[0].children[j].expectedChildren()==0)
//				{
//					slibings.add(tree.child.children[0].children[j].toString());
//				}
//				else {
//					//find the writeRegister whose index is equals to the readRegister
//					slibings.add(null);
//					int k;
//					for(k = i-1; k>=0; k--) {
//						if(((WriteRegisterGPNode)trees.get(k).child).getIndex() == ((ReadRegisterGPNode)tree.child.children[0].children[j]).getIndex()){
//							slibings.set(j, trees.get(k).child.children[0].toString()); //setting null as an entiy may have bugs 
//							break;
//						}
//					}
//					if(k<0){ //find which input feature is used to initialize the register
//						int term_ind = ((ReadRegisterGPNode)tree.child.children[0].children[j]).getIndex();
//						slibings.set(j, JobShopAttribute.relativeAttributes()[term_ind].getName());
//					}
//				}
//
//			}
			
//			res.add(new Pair<String, ArrayList<String>>(check, slibings));
			res.addAll(tree_AL);
			
			//check whether slibing functions exist in the map
//			for(int j = 0;j<check.expectedChildren(); j++) {
//				
//				int child = -1;
//				
//				if(check.children[j] instanceof TerminalERC) {
//					//store the constant index
//					
//				}
//				else {
//					//find its subsequent function
//					if(!res.keySet().contains(/* the child functions */)) {
//						if(end - 1 > start) {
//							HashMap<GPNode, ArrayList<Integer>> tmp = getAdjacencyTable(start, end - 1);
//						}
//						
//					}
//					
//					child = child 
//				}
//				
//				slibings.set(j, child);
//			}
			
			
		}
		
		return res;
	}
	
	
	public ArrayList<Pair<String, ArrayList<String>>> getAdjacencyTable(GPNode start_node, int curi){
		
		if(start_node == null){
			System.err.print("null GPNode in getAdjacencyTable() of TGPIndividual4MForm\n");
			System.exit(1);
		}
		
		ArrayList<Pair<String, ArrayList<String>>> res = new ArrayList<>();
		
		if(start_node.expectedChildren() == 0){
//			System.err.print("there is no adjacent table for terminals in TGPIndividual4MForm\n");
//			System.exit(1);
			return res;
		}
		
		
		
		LinkedList<GPNode> queue = new LinkedList<>();
		
		queue.add(start_node);
		
		while(!queue.isEmpty()){
			GPNode node = queue.pop();
			
			//generate the AT item and put it into results
			String check = node.toString();
			ArrayList<String> slibings = new ArrayList<>();
			
			//find
//			for(int j = 0; j<node.expectedChildren(); j++) {
//				slibings.add(node.children[j].toString());
//			}
			for(int j = 0; j<node.expectedChildren(); j++) {
				//constants:  inputs or constant values   or  non-terminals, functions
				if(! (node.children[j] instanceof ReadRegisterGPNode) )
//						&& node.children[j].expectedChildren()==0)
				{
					slibings.add(node.children[j].toString());
				}
				else {
					//find the writeRegister whose index is equals to the readRegister
					slibings.add(null);
					int k;
					for(k = curi-1; k>=0; k--) {
						if(((WriteRegisterGPNode)getTreelist().get(k).child).getIndex() == ((ReadRegisterGPNode)node.children[j]).getIndex()){
							slibings.set(j, getTreelist().get(k).child.children[0].toString()); //setting null as an entiy may have bugs 
							break;
						}
					}
					if(k<0){ //find which input feature is used to initialize the register
						int term_ind = ((ReadRegisterGPNode)node.children[j]).getIndex();
						slibings.set(j, null);
					}
				}
			}
			
			
			if(node.expectedChildren()>0)
				res.add(new Pair<String, ArrayList<String>>(check, slibings));
			
			//put the children into queue
			for(int j = 0; j<node.expectedChildren(); j++) {
				if(node.children[j].expectedChildren() > 0){
					queue.add(node.children[j]);
				}
			}
		}
		
		return res;
	}
}
