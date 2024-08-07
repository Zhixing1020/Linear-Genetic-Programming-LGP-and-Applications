package zhixing.djss.individual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.GPRuleEvolutionState;
import yimei.jss.gp.data.DoubleData;
import yimei.jss.gp.terminal.AttributeGPNode;
import yimei.jss.gp.terminal.JobShopAttribute;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;

public interface LGPInterface4DJSS extends CpxGPInterface4DJSS {

	@Override
	default void resetRegisters(final Problem problem, double val, final CpxGPIndividual ind) {
		DoubleData tmp = new DoubleData();
		
		for(int i = 0;i<((LGPIndividual)ind).getNumRegs();i++){
			if(((LGPIndividual)ind).getInitReg()[i] == 1){
//				JobShopAttribute a = list[i];
//				(new AttributeGPNode(a)).eval(null, 0, tmp, null, this, problem);
				((LGPIndividual)ind).getInitReg_values()[i].eval(null, 0, tmp, null, ind, problem);
				((LGPIndividual)ind).setRegister(i, tmp.value);
			}
			else{
				((LGPIndividual)ind).setRegister(i, val);
			}
		}
	}
	
	@Override
	default void prepareExecution(EvolutionState state){
		//check whehter we need flow controller
		//if instruction type of all effective instructions is 0, set fast mode
		LGPIndividual LGPind = (LGPIndividual) this;
		LGPind.setFastFlag(1); // 1: fast mode,  0: slow mode
		for(GPTreeStruct tree : LGPind.getTreelist()) {
			if(tree.status && tree.type != GPTreeStruct.ARITHMETIC) {  //effective and not arithmetic (branching or iteration)
				LGPind.setFastFlag(0);
				break;
			}
		}
		
		if(LGPind.exec_trees == null){
			LGPind.exec_trees = new ArrayList<>();
		}
		else{
			LGPind.exec_trees.clear();
		}
		
		
		//check which registers are necessarily to be initialized.
		LGPind.setInitReg(new int [LGPind.getNumRegs()]);
		//init_ConReg = new int [JobShopAttribute.values().length];
		for(int i = 0;i<LGPind.getNumRegs();i++){
			LGPind.getInitReg()[i] = -1;
		}
		for(GPTreeStruct tree : LGPind.getTreelist()){
			if(!tree.status) continue;
			
			for(int c = 0;c<2;c++){
				GPNode node = tree.child.children[0].children[c];
				if(node instanceof ReadRegisterGPNode){
					int ind = ((ReadRegisterGPNode)node).getIndex();
					if(LGPind.getInitReg()[ind]==-1){ //have not been written or read
						LGPind.getInitReg()[ind] = 1;  //it is read before being written, necessary to be initialized. 
					}
				}
				
				//check which constant register is necessarily to be initialized.
//				if(node instanceof ReadConstantRegisterGPNode){
//					init_ConReg[((AttributeGPNode)((TerminalERC)node).getTerminal()).getJobShopAttribute().ordinal()] = 1;
//				}
			}
			int ind = ((WriteRegisterGPNode)tree.child).getIndex();
			if(LGPind.getInitReg()[ind] == -1){ //have not been written or read
				LGPind.getInitReg()[ind] = 0; //it is wirtten before being read, unnecessary to be initialized
			}
			
			
			if(LGPind.getFastFlag() == 1){
				LGPind.exec_trees.add(tree);
			}
		}
		
		if(LGPind.getRegisters() == null) {
			LGPind.setRegisters(new double[LGPind.getNumRegs()]);
		}
//		if(constant_registers == null){
//			constant_registers = new double [JobShopAttribute.values().length];
//		}
		
		//identify the initialization input features for the registers. 
//		JobShopAttribute list[] = JobShopAttribute.relativeAttributes();
		ArrayList<JobShopAttribute> list = new ArrayList<>();
		
		if(state != null) {
			for(GPNode gpn : ((GPRuleEvolutionState)state).getTerminals()) {
				list.add(((AttributeGPNode)gpn).getJobShopAttribute());
			}
		}
		else { //by default, use relative attributes to initialize registers
			list = new ArrayList<>(Arrays.asList(JobShopAttribute.relativeAttributes()));
		}
				
	    LGPind.setInitReg_values(new AttributeGPNode [LGPind.getNumRegs()]);
		 for(int i = 0;i<LGPind.getNumRegs();i++){
			 JobShopAttribute a = list.get(i % list.size());
			 LGPind.getInitReg_values()[i] = new AttributeGPNode(a);
		 }
	}
	
	@Override
	default double execute(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
		//execute the whole individual. the fastFlag means that we will not consider the flow of instructions, just execute instructions one-by-one
		//the fastFlag should be set true only when there is not flow control operators in the primitive set.
		
		LGPIndividual ind = (LGPIndividual) individual;
		
		ind.resetRegisters(problem, 1, ind); //initialize registers by input features (or the default value)
		
		if(ind.getFastFlag() == 1){
			DoubleData rd = ((DoubleData)(input));
			for(GPTreeStruct tree : ind.exec_trees) {
				tree.child.eval(state, thread, input, stack, individual, problem);
		}
		}
		else{
			ind.getFlowctrl().execute(state, thread, input, stack, (CpxGPIndividual)individual, problem);
		}
			
		
		return ind.getRegisters()[ind.getOutputRegister()[0]]; //only the first output register will be used in basic LGP
	}
	
	@Override
	default ArrayList<Pair<String, ArrayList<String>>> getAdjacencyTable(EvolutionState state, int start, int end){
		//return the adjacency table of instruction sequence [start, end), only counts effective instruction
		
		LGPIndividual ind = (LGPIndividual) this;
		
		if(start<0||start>=end||end>ind.getTreelist().size()){
			System.err.print("illegal arguments in getAdjacencyTable() of LGPIndividual4Graph\n");
			System.exit(1);
		}
		
		ArrayList<Pair<String, ArrayList<String>>> res = new ArrayList<>();
		
		for(int i = end - 1; i>=start; i--) {
			//for each instruction, get the index of its function and children/constants
			GPTreeStruct tree = ind.getTreelist().get(i);
			
			if(!tree.status) continue;
			
//			int si,tj;
//			//get the index of its function
//			String name = tree.child.children[0].toString();
//			tj = primitives.indexOf(name);
			
			String check = tree.child.children[0].toString();
			ArrayList<String> slibings = new ArrayList<>();
			
			for(int j = 0; j<tree.child.children[0].expectedChildren(); j++) {
				
//				if(tree.child.children[0].children[j] instanceof TerminalERC) 
				if(! (tree.child.children[0].children[j] instanceof ReadRegisterGPNode) 
						&& tree.child.children[0].children[j].expectedChildren()==0)
				{
					slibings.add(tree.child.children[0].children[j].toString());
				}
				else {
					//find the writeRegister whose index is equals to the readRegister
					slibings.add(null);
					int k;
					for(k = i-1; k>=0; k--) {
						if(((WriteRegisterGPNode)ind.getTreelist().get(k).child).getIndex() == ((ReadRegisterGPNode)tree.child.children[0].children[j]).getIndex()){
							slibings.set(j, ind.getTreelist().get(k).child.children[0].toString()); //setting null as an entiy may have bugs 
							break;
						}
					}
					if(k<0){ //find which input feature is used to initialize the register
						int term_ind = ((ReadRegisterGPNode)tree.child.children[0].children[j]).getIndex();
						slibings.set(j, ((GPRuleEvolutionState)state).getTerminals().get(term_ind % ((GPRuleEvolutionState)state).getTerminals().size()).toString());
					}
				}

			}
			
			res.add(new Pair<String, ArrayList<String>>(check, slibings));
			
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

	@Override
	default String makeGraphvizRule(List<Integer> outputRegs){
		//this function is not support the instructions whose depth is larger than 3, also not support "IF" operation since
		//DAG cannot tell the loop body. If there are more than one operation in one instruction, subgraph of Graphviz should be used
		
		System.err.print("this makeGraphvizRule method is only suitable for DJSS with JobShopAttribute.relativeAttributes(). \n");
		
		LGPIndividual ind = (LGPIndividual) this;
		
		//collect terminal names
		String usedTerminals[] = new String[ind.getNumRegs()];
		int jj = 0;
		for (JobShopAttribute a : JobShopAttribute.relativeAttributes()) {
			
			usedTerminals[jj++] = a.getName();

			if(jj>=ind.getNumRegs()) break;
		}
		
		Set<String> JSSAttributes = new HashSet<>();
		
		//check all instructions and specify all effective operations, effective constants 
		String nodeSpec ="";
		for(int i = 0;i<ind.getTreelist().size();i++){
			GPTreeStruct tree = ind.getTreelist().get(i);
			
			if(!tree.status) continue;
			
			nodeSpec += "" + i + "[label=\"" + tree.child.children[0].toString() + "\"];\n";
			for(int c = 0;c<tree.child.children[0].children.length; c++){
				GPNode node = tree.child.children[0].children[c];
				if(node instanceof AttributeGPNode){
					//check whether it has been here
					if(!JSSAttributes.contains(node.toString())){
						JSSAttributes.add(node.toString());
						nodeSpec += node.toString()+"[shape=box];\n";
					}
					nodeSpec += "" + i + "->"+node.toString()+"[label=\"" + c +"\"];\n";
				}
			}
		}
		
		//backward visit all effective instructions, connect the instructions
		String connection = "";
		Set<Integer> notUsed = new HashSet<>(outputRegs);
		for(int i=ind.getTreelist().size()-1;i>=0;i--){
			GPTreeStruct tree = ind.getTreelist().get(i);
			
			if(!tree.status) continue;
			
			if(notUsed.contains((((WriteRegisterGPNode) tree.child)).getIndex())){
				connection += "R" +  (((WriteRegisterGPNode) tree.child)).getIndex() +"[shape=box];\n";
				connection += "R" +  (((WriteRegisterGPNode) tree.child)).getIndex() + "->" + i + ";\n";
				notUsed.remove((Integer)(((WriteRegisterGPNode) tree.child)).getIndex());
			}
			
			//find the instructions whose destination register is the same with the source registers for this instruction
			List<Integer> source = new ArrayList<>();
			for(int c = 0;c<tree.child.children[0].children.length; c++){
				GPNode node = tree.child.children[0].children[c];
				if(node instanceof ReadRegisterGPNode){
					source.add(((ReadRegisterGPNode)node).getIndex());
					
					for(int j = i-1;j>=0;j--){
						
						GPTreeStruct visit = ind.getTreelist().get(j);
						
						if(!visit.status) continue;
						
						while(source.contains((((WriteRegisterGPNode) visit.child)).getIndex())){
							connection += "" + i + "->" + j + "[label=\"" + c +"\"];\n";
							source.remove(source.indexOf((((WriteRegisterGPNode) visit.child)).getIndex()));
						}
						
						if(source.size()==0) break;
					}
					//if there is still source registers, connect the instruction with JSS attributes
					for(Integer j : source){
						connection += usedTerminals[j]+"[shape=box];\n";   // use job shop attributes to initialize registers
						connection += "" + i + "->" + usedTerminals[j] + "[label=\"" + c +"\"];\n";
//						connection += "1[shape=box];\n";  // use "1" to initialize registers
//						connection += "" + i + "->" + "1[label=\"" + c +"\"];\n";
					}
					source.clear();
				}
				
				
			}
			
		}
		
		String result = "digraph g {\n" 
		+"nodesep=0.2;\n"
		+"ranksep=0;\n"
		+ "node[fixedsize=true,width=1.3,height=0.6,fontsize=\"30\",fontname=\"times-bold\",style=filled, fillcolor=lightgrey];\n"
		+"edge[fontsize=\"25.0\",fontname=\"times-bold\"];\n"
		+ nodeSpec
		+ connection
		+ "}\n";
		
		return result;
	}
}
