package zhixing.symbreg_multitarget.individual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.data.DoubleData;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.symbolicregression.individual.primitive.InputFeatureGPNode;
import zhixing.symbreg_multitarget.optimization.GPSymbolicRegressionMultiTarget;

public interface LGPInterface4SRMT extends CpxGPInterface4SRMT{

	double initVal = 0.0; 
	
	default Double[] execute_multitar(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
		LGPIndividual ind = (LGPIndividual) individual;
		
		ind.resetRegisters(problem, initVal, ind);
      
		for(int index = 0; index<ind.getTreesLength(); index++){
			GPTreeStruct tree = ind.getTreeStruct(index);
			if(tree.status) {
				tree.child.eval(state, thread, input, stack, ind, problem);
			}
			
		}
		
		final int targetnum = ((GPSymbolicRegressionMultiTarget)problem).getTargetNum();
		
		if(targetnum > ind.getOutputRegisters().length) {
			System.err.print("the number of output registers must be at least equal to the number of targets\n");
			System.exit(1);
		}
		
		Double [] res = new Double[ind.getOutputRegisters().length];
		for(int d = 0; d<ind.getOutputRegisters().length; d++ ) {
			res [d] = ind.getRegisters()[ind.getOutputRegisters()[d]];
		}
		return res;
	}
	
	default Double[] execute_multitar_wrap(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
		LGPIndividual ind = (LGPIndividual) individual;
		
		ind.resetRegisters(problem, initVal, ind);
      
		for(int index = 0; index<ind.getTreesLength(); index++){
			GPTreeStruct tree = ind.getTreeStruct(index);
			if(tree.status) {
				tree.child.eval(state, thread, input, stack, ind, problem);
			}
			
		}
		
		if(((CpxGPIndividual)ind).IsWrap()) {
			for(int w = 0; w< ((ArrayList) ind.getWrapper()).size(); w++) {
				GPTreeStruct tree = (GPTreeStruct) ((ArrayList) ind.getWrapper()).get(w);
				tree.child.eval(state, thread, input, stack, ind, problem);
			}
		}
		
		final int targetnum = ((GPSymbolicRegressionMultiTarget)problem).getTargetNum();
		
		if(targetnum > ind.getOutputRegisters().length) {
			System.err.print("the number of output registers must be at least equal to the number of targets\n");
			System.exit(1);
		}
		
		Double [] res = new Double[ind.getOutputRegisters().length];
		for(int d = 0; d<ind.getOutputRegisters().length; d++ ) {
			res [d] = ind.getRegisters()[ind.getOutputRegisters()[d]];
		}
		return res;
	}
	
	@Override
	default void resetRegisters(final Problem problem, double val, final CpxGPIndividual ind){

		DoubleData tmp = new DoubleData();
		
//		double X[] = ((GPSymbolicRegression) problem).X;
		
		for(int i = 0;i<((LGPIndividual)ind).getRegisters().length;i++){
			((LGPIndividual)ind).setRegister(i, val);
//			ind.setRegister(i, X[i%X.length]);
//			if(initReg[i] == 1){
////				JobShopAttribute a = list[i];
////				(new AttributeGPNode(a)).eval(null, 0, tmp, null, this, problem);
//				initReg_values[i].eval(null, 0, tmp, null, this, problem);
//				registers[i] = tmp.value;
//			}
//			else{
//				registers[i] = val;
//			}
		}
	}
	
	@Override
	default double execute(final EvolutionState state,
	        final int thread,
	        final GPData input,
	        final ADFStack stack,
	        final GPIndividual individual,
	        final Problem problem) {
		
//		LGPIndividual ind = (LGPIndividual) individual;
//		
//		ind.resetRegisters(problem, initVal, ind);
//      
//		for(int index = 0; index<ind.getTreesLength(); index++){
//			GPTreeStruct tree = ind.getTreeStruct(index);
//			if(tree.status) {
//				tree.child.eval(state, thread, input, stack, ind, problem);
//			}
//			
//		}
//		
//		return ind.getRegisters()[ind.getOutputRegister()[0]];
		
		System.err.print("individuals for multi-target SR problems should not use this function to execute programs, try to use \"execute_multitar(...)\" \n");
		System.exit(1);
		
		return 0;
	}

	@Override
	default void prepareExecution(EvolutionState state) {
		//check whehter we need flow controller
		//if instruction type of all effective instructions is 0, set fast mode
		LGPIndividual ind = (LGPIndividual) this;
		
		ind.setFastFlag(1); // 1: fast mode,  0: slow mode
		for(GPTreeStruct tree : ind.getTreelist()) {
			if(tree.status && tree.type != GPTreeStruct.ARITHMETIC) {  //effective and not arithmetic (branching or iteration)
				ind.setFastFlag(0);
				break;
			}
		}
		
		if(ind.exec_trees == null){
			ind.exec_trees = new ArrayList<>();
		}
		else{
			ind.exec_trees.clear();
		}
		
		
		//check which registers are necessarily to be initialized.
		ind.setInitReg(new int [ind.getNumRegs()]);
		//init_ConReg = new int [JobShopAttribute.values().length];
		for(int i = 0;i<ind.getNumRegs();i++){
			ind.getInitReg()[i] = -1;
		}
		for(GPTreeStruct tree : ind.getTreelist()){
			if(!tree.status) continue;
			
			for(int c = 0;c<2;c++){
				GPNode node = tree.child.children[0].children[c];
				if(node instanceof ReadRegisterGPNode){
					int index = ((ReadRegisterGPNode)node).getIndex();
					if(ind.getInitReg()[index]==-1){ //have not been written or read
						ind.getInitReg()[index] = 1;  //it is read before being written, necessary to be initialized. 
					}
				}
				
				//check which constant register is necessarily to be initialized.
//						if(node instanceof ReadConstantRegisterGPNode){
//							init_ConReg[((AttributeGPNode)((TerminalERC)node).getTerminal()).getJobShopAttribute().ordinal()] = 1;
//						}
			}
			int index = ((WriteRegisterGPNode)tree.child).getIndex();
			if(ind.getInitReg()[index] == -1){ //have not been written or read
				ind.getInitReg()[index] = 0; //it is wirtten before being read, unnecessary to be initialized
			}
			
			
			if(ind.getFastFlag() == 1){
				ind.exec_trees.add(tree);
			}
		}
		
		if(ind.getRegisters() == null) {
			ind.setRegisters(new double[ind.getNumRegs()]);
		}
		
		ind.setInitReg_values( null /*new AttributeGPNode [ind.getNumRegs()]*/);
	}

	@Override
	default ArrayList<Pair<String, ArrayList<String>>> getAdjacencyTable(EvolutionState state, int start, int end) {
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
			
//					int si,tj;
//					//get the index of its function
//					String name = tree.child.children[0].toString();
//					tj = primitives.indexOf(name);
			
			String check = tree.child.children[0].toString();
			ArrayList<String> slibings = new ArrayList<>();
			
			for(int j = 0; j<tree.child.children[0].expectedChildren(); j++) {
				
//						if(tree.child.children[0].children[j] instanceof TerminalERC) 
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
						slibings.set(j, null);
					}
				}

			}
			
			res.add(new Pair<String, ArrayList<String>>(check, slibings));
			
			//check whether slibing functions exist in the map
//					for(int j = 0;j<check.expectedChildren(); j++) {
//						
//						int child = -1;
//						
//						if(check.children[j] instanceof TerminalERC) {
//							//store the constant index
//							
//						}
//						else {
//							//find its subsequent function
//							if(!res.keySet().contains(/* the child functions */)) {
//								if(end - 1 > start) {
//									HashMap<GPNode, ArrayList<Integer>> tmp = getAdjacencyTable(start, end - 1);
//								}
//								
//							}
//							
//							child = child 
//						}
//						
//						slibings.set(j, child);
//					}
			
			
		}
		
		return res;
	}

	@Override
	default String makeGraphvizRule(List<Integer> outputRegs) {
		//this function does not support "IF" operation since
		//DAG cannot tell the loop body.
		LGPIndividual ind = (LGPIndividual) this;
		
		//collect terminal names
		String usedTerminals[] = new String[ind.getNumRegs()];
		for(int j = 0; j<ind.getNumRegs();) {
			usedTerminals[j++] = ""+initVal; //since LGP for SR uses 1 to initialize registers
		}
		
		Set<String> SRInputs = new HashSet<>();
		
		//check all instructions and specify all effective operations, effective constants 
		String nodeSpec ="";
		for(int i = 0;i<ind.getTreelist().size();i++){
			GPTreeStruct tree = ind.getTreelist().get(i);
			
			if(!tree.status) continue;
			
			nodeSpec += "" + i + "[label=\"" + tree.child.children[0].toGraphvizString() + "\"];\n";
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
//				
//				if(node instanceof Entity) {
//					if(!SRInputs.contains(((Entity)node).toGraphvizString())){
//						SRInputs.add(((Entity)node).toGraphvizString());
//						nodeSpec += "\"" + ((Entity)node).toGraphvizString()+"\"[shape=box];\n";
//					}
//					nodeSpec += "" + i + "->"+"\"" + ((Entity)node).toGraphvizString()+"\""+"[label=\"" + c +"\"];\n";
//				}
//			}
		}
		
		//backward visit all effective instructions, connect the instructions
		String connection = "";
		Set<Integer> notUsed = new HashSet<>(outputRegs);
		
		AtomicInteger cntindex = new AtomicInteger(ind.getTreelist().size());
		
		for(int i=ind.getTreelist().size()-1;i>=0;i--){
			GPTreeStruct tree = ind.getTreelist().get(i);
			
			if(!tree.status) continue;

			connection += ind.makeGraphvizInstr(i, SRInputs, usedTerminals, notUsed, cntindex);
			
//			if(notUsed.contains((((WriteRegisterGPNode) tree.child)).getIndex())){
//				connection += "R" +  (((WriteRegisterGPNode) tree.child)).getIndex() +"[shape=box];\n";
//				connection += "R" +  (((WriteRegisterGPNode) tree.child)).getIndex() + "->" + i + ";\n";
//				notUsed.remove((Integer)(((WriteRegisterGPNode) tree.child)).getIndex());
//			}
			
			//find the instructions whose destination register is the same with the source registers for this instruction
//			List<Integer> source = new ArrayList<>();
//			for(int c = 0;c<tree.child.children[0].children.length; c++){
//				GPNode node = tree.child.children[0].children[c];
//				if(node instanceof ReadRegisterGPNode){
//					source.add(((ReadRegisterGPNode)node).getIndex());
//					
//					for(int j = i-1;j>=0;j--){
//						
//						GPTreeStruct visit = ind.getTreelist().get(j);
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
