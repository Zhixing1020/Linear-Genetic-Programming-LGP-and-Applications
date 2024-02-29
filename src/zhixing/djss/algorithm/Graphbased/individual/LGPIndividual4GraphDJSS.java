package zhixing.djss.algorithm.Graphbased.individual;

import ec.EvolutionState;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.util.Parameter;
//import yimei.jss.gp.terminal.JobShopAttribute;
import yimei.jss.gp.terminal.TerminalERC;
import yimei.jss.gp.GPRuleEvolutionState;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.djss.individual.LGPInterface4DJSS;

public class LGPIndividual4GraphDJSS extends zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph implements LGPInterface4DJSS{

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		//set up the individual prototype 
		 super.setup(state,base); 
		 Parameter def = defaultBase();
		 
	    //initialize AM
		setPrimitives4Problem(state);
		 
	}
	
	@Override
	protected void setPrimitives4Problem(final EvolutionState state) {
		//get the function set
		 GPFunctionSet set = this.getTree(0).constraints((GPInitializer)state.initializer).functionset;  //all trees have the same function set	
		 
		 //collect the name of all functions and constants
		 //functions
		 int i = 0;
		 dimension = 0;
		 dimension += set.nonterminals_v.size();
		 dimension_fun = set.nonterminals_v.size();
		 for(;i<set.nonterminals_v.size();i++){
			 getPrimitives().add(set.nonterminals_v.get(i).toString());
		 }
		//registers
//		 dimension += numRegs;
//		 dimension_reg = numRegs;
//		 for(i=0;i<numRegs;i++){
//			 primitives.add("R"+i);
//		 }
		 //constants
//		 dimension += JobShopAttribute.relativeAttributes().length;
//		 dimension_cons = JobShopAttribute.relativeAttributes().length;
//		 for(i=0;i<JobShopAttribute.relativeAttributes().length;i++){
//			 getPrimitives().add(JobShopAttribute.relativeAttributes()[i].getName());
//		 }
		 
		 dimension += ((GPRuleEvolutionState)state).getTerminals().size();
		 dimension_cons = ((GPRuleEvolutionState)state).getTerminals().size();
		 for(i=0;i<((GPRuleEvolutionState)state).getTerminals().size();i++){
			 getPrimitives().add(((GPRuleEvolutionState)state).getTerminals().get(i).toString());
		 }
	}
	
	@Override
	public double[] getFrequency(int start,int end){
		//return the functions and terminals' frequency between [start, end) instruction, only counts effective instruction
		if(dimension <=0){
			System.err.print("dimension of adjacent matrixes is less than or equal to 0 in LGPIndividual4Graph\n");
			System.exit(1);
		}
		else if(start<0||start>=end||end<0||end>getTreelist().size()){
			System.err.print("start or end arguments are out of range in LGPIndividual4Graph\n");
			System.exit(1);
		}
		else{
			frequency = new double[dimension];
			for(int i = start;i<end;i++){
				GPTreeStruct tree = getTreelist().get(i);
				
				if(!tree.status) continue;
				
				int si;
				//get the index of the WriteRegister
//				String name = tree.child.toString().split("=")[0];
//				si = primitives.indexOf(name);
//				frequency[si] ++;
				
				//get the index of its function
				String name = tree.child.children[0].toString();
				si = getPrimitives().indexOf(name);
				frequency[si] ++;
				
				for(int j = 0;j<tree.child.children[0].expectedChildren();j++){
					if(tree.child.children[0].children[j] instanceof TerminalERC){
						name = ((TerminalERC)tree.child.children[0].children[j]).getTerminal().toString();
						si = getPrimitives().indexOf(name);
						frequency[si] ++;
					}
					//else if it is ReadRegisterGPNode
					else{
						//check whether it is initialized by constants
						si = ((ReadRegisterGPNode)tree.child.children[0].children[j]).getIndex();
						
						
						int k = i - 1;
						for(; k>=0; k--){
							if(getTreelist().get(k).status && ((WriteRegisterGPNode)getTreelist().get(k).child).getIndex() == si){
								break;
							}
						}
						
						if(k<0){
//							name = JobShopAttribute.relativeAttributes()[si % JobShopAttribute.relativeAttributes().length].toString();
//							si = getPrimitives().indexOf(name);
							frequency[si + dimension_fun] ++;
						}
//						else {
//							name = tree.child.children[0].children[j].toString();
//							si = primitives.indexOf(name);
//							frequency[si] ++;
//						}
					}
					
				}
			}
		}
		return frequency;
	}
	
	@Override
	public double[][] getAM(int start, int end){
		//return the adjacency matrix of instruction sequence [start, end), only counts effective instruction
		if(start<0||start>=end||end>getTreelist().size()){
			System.err.print("illegal arguments in getAM() of LGPIndividual4Graph\n");
			System.exit(1);
		}
		
		double[][] adjacencyMatrix = new double [dimension][dimension];
		
		//update AM based on genotype,  for each effective instruction, check its parents. If it is the root (no parent), all primitives to this operation + 1.
		for(int i = start; i<end; i++){
			
			//for each instruction, get the index of its function and children/constants
			GPTreeStruct tree = getTreelist().get(i);
			
			if(!tree.status) continue;
			
			int si,tj;
			//get the index of its function
			String name = tree.child.children[0].toString();
			tj = getPrimitives().indexOf(name);
			si = -1;
			
			//see which following instructions use these writer register, then it is the parent of this function
			int writeRegInd = ((WriteRegisterGPNode)tree.child).getIndex();
			for(int j = start+1;j<getTreelist().size();j++) {
				if(getTreelist().get(j).status && getTreelist().get(j).collectReadRegister().contains(writeRegInd)) {
					name = getTreelist().get(j).child.children[0].toString();
					si = getPrimitives().indexOf(name);
					break;
				}
			}
			if(si>=0) {
				adjacencyMatrix[si][tj] ++;
			}
//			else {
//				for(int sii = 0;sii<dimension;sii++) {
//					adjacencyMatrix[sii][tj] ++;
//				}
//			}
			
			//see whether input arguments of instruction tj  has constants
			for(int p = 0;p<tree.child.children[0].expectedChildren();p++) {
				if(tree.child.children[0].children[p] instanceof TerminalERC){
					name = ((TerminalERC)tree.child.children[0].children[p]).getTerminal().toString();
					int tjj = getPrimitives().indexOf(name);
					adjacencyMatrix[tj][tjj] ++;
				}
				//else if it is ReadRegisterGPNode, check whether it is initialized by constants
				else{
					int index = ((ReadRegisterGPNode)tree.child.children[0].children[p]).getIndex();
					
					int j = i - 1;
					for(; j>=0; j--){
						if(getTreelist().get(j).status && ((WriteRegisterGPNode)getTreelist().get(j).child).getIndex() == index){
							break;
						}
					}
					
					if(j<0){
						adjacencyMatrix[tj][getStartEnd_cons()[0]+index] ++;
					}
				}
			}
			
			//for each source register, get its index and update AM
//			for(int p = 0;p<tree.child.children[0].expectedChildren();p++){
//				if(tree.child.children[0].children[p] instanceof TerminalERC){
//					name = ((TerminalERC)tree.child.children[0].children[p]).getTerminal().toString();
//					tj = primitives.indexOf(name);
//					adjacencyMatrix[si][tj] ++;
//				}
//				else{
//					int register = ((ReadRegisterGPNode)tree.child.children[0].children[p]).getIndex();
//					for(int s = trees.indexOf(tree)-1;s>=start;s--){
//						if(((WriteRegisterGPNode)this.trees.get(s).child).getIndex() == register){
//							name = trees.get(s).child.children[0].toString();
//							tj = primitives.indexOf(name);
//							adjacencyMatrix[si][tj] ++;
//							break;
//						}
//					}
//				}
//			}
			
		}
		
		return adjacencyMatrix;
	}
}
