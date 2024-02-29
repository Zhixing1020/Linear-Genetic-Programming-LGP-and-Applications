package zhixing.symbolicregression.individual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPFunctionSet;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import yimei.jss.gp.data.DoubleData;
import yimei.jss.gp.terminal.AttributeGPNode;
import yimei.jss.gp.terminal.JobShopAttribute;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
//import zhixing.jss.cpxInd.individual.LGPIndividual4DJSS;
import zhixing.symbolicregression.individual.primitive.InputFeatureGPNode;
import zhixing.symbolicregression.optimization.GPSymbolicRegression;

public class LGPIndividual4SR extends LGPIndividual implements LGPInterface4SR{
//	//@Override
//	public void prepareExecution(EvolutionState state, Problem problem){
//		//check whehter we need flow controller
//		//if instruction type of all effective instructions is 0, set fast mode
//		fastFlag = 1; // 1: fast mode,  0: slow mode
//		for(GPTreeStruct tree : trees) {
//			if(tree.status && tree.type != 0) {  //effective and not arithmetic (branching or iteration)
//				fastFlag = 0;
//				break;
//			}
//		}
//		
//		if(exec_trees == null){
//			exec_trees = new ArrayList<>();
//		}
//		else{
//			exec_trees.clear();
//		}
//		
//		
//		//check which registers are necessarily to be initialized.
//		initReg = new int [numRegs];
//		//init_ConReg = new int [JobShopAttribute.values().length];
//		for(int i = 0;i<numRegs;i++){
//			initReg[i] = -1;
//		}
//		for(GPTreeStruct tree : trees){
//			if(!tree.status) continue;
//			
//			for(int c = 0;c<2;c++){
//				GPNode node = tree.child.children[0].children[c];
//				if(node instanceof ReadRegisterGPNode){
//					int ind = ((ReadRegisterGPNode)node).getIndex();
//					if(initReg[ind]==-1){ //have not been written or read
//						initReg[ind] = 1;  //it is read before being written, necessary to be initialized. 
//					}
//				}
//				
//				//check which constant register is necessarily to be initialized.
////				if(node instanceof ReadConstantRegisterGPNode){
////					init_ConReg[((AttributeGPNode)((TerminalERC)node).getTerminal()).getJobShopAttribute().ordinal()] = 1;
////				}
//			}
//			int ind = ((WriteRegisterGPNode)tree.child).getIndex();
//			if(initReg[ind] == -1){ //have not been written or read
//				initReg[ind] = 0; //it is wirtten before being read, unnecessary to be initialized
//			}
//			
//			
//			if(fastFlag == 1){
//				exec_trees.add(tree);
//			}
//		}
//		
//		if(registers == null) {
//			registers = new double[numRegs];
//		}
////		if(constant_registers == null){
////			constant_registers = new double [JobShopAttribute.values().length];
////		}
//	
//	}
	
////	@Override
//	public void resetRegisters(final Problem problem, double val, final LGPIndividual ind){
//
//		DoubleData tmp = new DoubleData();
//		
////		double X[] = ((GPSymbolicRegression) problem).X;
//		
//		for(int i = 0;i<ind.getRegisters().length;i++){
//			ind.setRegister(i, val);
////			ind.setRegister(i, X[i%X.length]);
////			if(initReg[i] == 1){
//////				JobShopAttribute a = list[i];
//////				(new AttributeGPNode(a)).eval(null, 0, tmp, null, this, problem);
////				initReg_values[i].eval(null, 0, tmp, null, this, problem);
////				registers[i] = tmp.value;
////			}
////			else{
////				registers[i] = val;
////			}
//		}
//	}
//	
//	@Override
//	public double execute(final EvolutionState state,
//	        final int thread,
//	        final GPData input,
//	        final ADFStack stack,
//	        final GPIndividual individual,
//	        final Problem problem) {
//		this.resetRegisters(problem, 1.0, this);
//      
//		for(int index = 0; index<this.getTreesLength(); index++){
//			GPTreeStruct tree = this.getTreeStruct(index);
//			if(tree.status) {
//				tree.child.eval(state, thread, input, stack, this, problem);
//			}
//			
//		}
//		
//		return registers[getOutputRegister()[0]];
//	}
//
//	@Override
//	public void prepareExecution(EvolutionState state) {
//		//check whehter we need flow controller
//		//if instruction type of all effective instructions is 0, set fast mode
//		setFastFlag(1); // 1: fast mode,  0: slow mode
//		for(GPTreeStruct tree : getTreelist()) {
//			if(tree.status && tree.type != GPTreeStruct.ARITHMETIC) {  //effective and not arithmetic (branching or iteration)
//				setFastFlag(0);
//				break;
//			}
//		}
//		
//		if(exec_trees == null){
//			exec_trees = new ArrayList<>();
//		}
//		else{
//			exec_trees.clear();
//		}
//		
//		
//		//check which registers are necessarily to be initialized.
//		setInitReg(new int [getNumRegs()]);
//		//init_ConReg = new int [JobShopAttribute.values().length];
//		for(int i = 0;i<getNumRegs();i++){
//			getInitReg()[i] = -1;
//		}
//		for(GPTreeStruct tree : getTreelist()){
//			if(!tree.status) continue;
//			
//			for(int c = 0;c<2;c++){
//				GPNode node = tree.child.children[0].children[c];
//				if(node instanceof ReadRegisterGPNode){
//					int ind = ((ReadRegisterGPNode)node).getIndex();
//					if(getInitReg()[ind]==-1){ //have not been written or read
//						getInitReg()[ind] = 1;  //it is read before being written, necessary to be initialized. 
//					}
//				}
//				
//				//check which constant register is necessarily to be initialized.
////						if(node instanceof ReadConstantRegisterGPNode){
////							init_ConReg[((AttributeGPNode)((TerminalERC)node).getTerminal()).getJobShopAttribute().ordinal()] = 1;
////						}
//			}
//			int ind = ((WriteRegisterGPNode)tree.child).getIndex();
//			if(getInitReg()[ind] == -1){ //have not been written or read
//				getInitReg()[ind] = 0; //it is wirtten before being read, unnecessary to be initialized
//			}
//			
//			
//			if(getFastFlag() == 1){
//				exec_trees.add(tree);
//			}
//		}
//		
//		if(registers == null) {
//			setRegisters(new double[getNumRegs()]);
//		}
//		
//		setInitReg_values(new AttributeGPNode [getNumRegs()]);
//	}
//
//	@Override
//	public ArrayList<Pair<String, ArrayList<String>>> getAdjacencyTable(int start, int end) {
//		//return the adjacency table of instruction sequence [start, end), only counts effective instruction
//		if(start<0||start>=end||end>getTreelist().size()){
//			System.err.print("illegal arguments in getAdjacencyTable() of LGPIndividual4Graph\n");
//			System.exit(1);
//		}
//		
//		ArrayList<Pair<String, ArrayList<String>>> res = new ArrayList<>();
//		
//		for(int i = end - 1; i>=start; i--) {
//			//for each instruction, get the index of its function and children/constants
//			GPTreeStruct tree = getTreelist().get(i);
//			
//			if(!tree.status) continue;
//			
////					int si,tj;
////					//get the index of its function
////					String name = tree.child.children[0].toString();
////					tj = primitives.indexOf(name);
//			
//			String check = tree.child.children[0].toString();
//			ArrayList<String> slibings = new ArrayList<>();
//			
//			for(int j = 0; j<tree.child.children[0].expectedChildren(); j++) {
//				
////						if(tree.child.children[0].children[j] instanceof TerminalERC) 
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
//						if(((WriteRegisterGPNode)getTreelist().get(k).child).getIndex() == ((ReadRegisterGPNode)tree.child.children[0].children[j]).getIndex()){
//							slibings.set(j, getTreelist().get(k).child.children[0].toString()); //setting null as an entiy may have bugs 
//							break;
//						}
//					}
//					if(k<0){ //find which input feature is used to initialize the register
//						int term_ind = ((ReadRegisterGPNode)tree.child.children[0].children[j]).getIndex();
//						slibings.set(j, null);
//					}
//				}
//
//			}
//			
//			res.add(new Pair<String, ArrayList<String>>(check, slibings));
//			
//			//check whether slibing functions exist in the map
////					for(int j = 0;j<check.expectedChildren(); j++) {
////						
////						int child = -1;
////						
////						if(check.children[j] instanceof TerminalERC) {
////							//store the constant index
////							
////						}
////						else {
////							//find its subsequent function
////							if(!res.keySet().contains(/* the child functions */)) {
////								if(end - 1 > start) {
////									HashMap<GPNode, ArrayList<Integer>> tmp = getAdjacencyTable(start, end - 1);
////								}
////								
////							}
////							
////							child = child 
////						}
////						
////						slibings.set(j, child);
////					}
//			
//			
//		}
//		
//		return res;
//	}
//
//	@Override
//	public String makeGraphvizRule(List<Integer> outputRegs) {
//		//this function is not support the instructions whose depth is larger than 3, also not support "IF" operation since
//		//DAG cannot tell the loop body. If there are more than one operation in one instruction, subgraph of Graphviz should be used
//		
//		//collect terminal names
//		String usedTerminals[] = new String[getNumRegs()];
//		for(int j = 0; j<getNumRegs();) {
//			for (JobShopAttribute a : JobShopAttribute.relativeAttributes()) {
//				
//				usedTerminals[j++] = a.getName();
//
//				if(j>=getNumRegs()) break;
//			}
//		}
//		
//		Set<String> SRInputs = new HashSet<>();
//		
//		//check all instructions and specify all effective operations, effective constants 
//		String nodeSpec ="";
//		for(int i = 0;i<getTreelist().size();i++){
//			GPTreeStruct tree = getTreelist().get(i);
//			
//			if(!tree.status) continue;
//			
//			nodeSpec += "" + i + "[label=\"" + tree.child.children[0].toString() + "\"];\n";
//			for(int c = 0;c<tree.child.children[0].children.length; c++){
//				GPNode node = tree.child.children[0].children[c];
//				if(node instanceof InputFeatureGPNode){
//					//check whether it has been here
//					if(!SRInputs.contains(node.toString())){
//						SRInputs.add(node.toString());
//						nodeSpec += node.toString()+"[shape=box];\n";
//					}
//					nodeSpec += "" + i + "->"+node.toString()+"[label=\"" + c +"\"];\n";
//				}
//			}
//		}
//		
//		//backward visit all effective instructions, connect the instructions
//		String connection = "";
//		Set<Integer> notUsed = new HashSet<>(outputRegs);
//		for(int i=getTreelist().size()-1;i>=0;i--){
//			GPTreeStruct tree = getTreelist().get(i);
//			
//			if(!tree.status) continue;
//			
//			if(notUsed.contains((((WriteRegisterGPNode) tree.child)).getIndex())){
//				connection += "R" +  (((WriteRegisterGPNode) tree.child)).getIndex() +"[shape=box];\n";
//				connection += "R" +  (((WriteRegisterGPNode) tree.child)).getIndex() + "->" + i + ";\n";
//				notUsed.remove((Integer)(((WriteRegisterGPNode) tree.child)).getIndex());
//			}
//			
//			//find the instructions whose destination register is the same with the source registers for this instruction
//			List<Integer> source = new ArrayList<>();
//			for(int c = 0;c<tree.child.children[0].children.length; c++){
//				GPNode node = tree.child.children[0].children[c];
//				if(node instanceof ReadRegisterGPNode){
//					source.add(((ReadRegisterGPNode)node).getIndex());
//					
//					for(int j = i-1;j>=0;j--){
//						
//						GPTreeStruct visit = getTreelist().get(j);
//						
//						if(!visit.status) continue;
//						
//						while(source.contains((((WriteRegisterGPNode) visit.child)).getIndex())){
//							connection += "" + i + "->" + j + "[label=\"" + c +"\"];\n";
//							source.remove(source.indexOf((((WriteRegisterGPNode) visit.child)).getIndex()));
//						}
//						
//						if(source.size()==0) break;
//					}
//					//if there is still source registers, connect the instruction with JSS attributes
//					for(Integer j : source){
//						connection += usedTerminals[j]+"[shape=box];\n";   // use job shop attributes to initialize registers
//						connection += "" + i + "->" + usedTerminals[j] + "[label=\"" + c +"\"];\n";
////								connection += "1[shape=box];\n";  // use "1" to initialize registers
////								connection += "" + i + "->" + "1[label=\"" + c +"\"];\n";
//					}
//					source.clear();
//				}
//				
//				
//			}
//			
//		}
//		
//		String result = "digraph g {\n" 
//		+"nodesep=0.2;\n"
//		+"ranksep=0;\n"
//		+ "node[fixedsize=true,width=1.3,height=0.6,fontsize=\"30\",fontname=\"times-bold\",style=filled, fillcolor=lightgrey];\n"
//		+"edge[fontsize=\"25.0\",fontname=\"times-bold\"];\n"
//		+ nodeSpec
//		+ connection
//		+ "}\n";
//		
//		return result;
//	}
	
	
//	public double evaluate(final EvolutionState state,
//        final int thread,
//        final GPData input,
//        final ADFStack stack,
//        final GPIndividual individual,
//        final Problem problem) {
//		
//		LGPIndividual4SR.resetRegisters(problem, 1.0, this);
//        
// 		for(int index = 0; index<this.getTreesLength(); index++){
// 			GPTreeStruct tree = this.getTreeStruct(index);
// 			if(tree.status) {
// 				tree.child.eval(state, thread, input, stack, this, problem);
// 			}
// 			
// 		}
// 		
// 		return registers[0];
//	}
}
