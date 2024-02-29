package zhixing.cpxInd.individual;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Problem;
//import ec.app.tutorial4.DoubleData;
import yimei.jss.gp.data.DoubleData;
import zhixing.cpxInd.individual.primitive.FlowOperator;
import zhixing.cpxInd.individual.LGPIndividual;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPNode;

public class LGPFlowController extends FlowController {
	private ArrayList<GPTreeStruct> trees;
	
	public LGPFlowController(){
		super();
	}
	
	public void execute(EvolutionState state, int thread, GPData input, ADFStack stack, CpxGPIndividual individual, Problem problem){
		//trees = new ArrayList<>(((LGPIndividual)individual).getTreeStructs());
		trees = (ArrayList<GPTreeStruct>) ((LGPIndividual)individual).getTreeStructs();
		
		resetFlowController(trees.size());
		
		executeProgramBlock(null, 0, input, null, individual, problem);
	}
	
	private void executeProgramBlock(EvolutionState state, int thread, GPData input, ADFStack stack, CpxGPIndividual individual, Problem problem){
		//get the instruction of currentIndex, if arithmetic instruction, evaluate and increase currentIndex
		//if flow control instruction, put currentIndex as beginIndex and beginIndex + bodyLength as endIndex
		GPTreeStruct instr;
		do{
			instr = trees.get(currentIndex);
			GPNode newroot;
			switch(instr.type){
			case GPTreeStruct.ARITHMETIC:  //arithmetic
				if(instr.status) instr.child.eval(state, thread, input, stack, individual, problem);
				used.set(currentIndex, used.get(currentIndex)+1);
				currentIndex ++;
				break;
			case GPTreeStruct.BRANCHING: //branching
				newroot = instr.child.children[0]; //got the flow control operator
				if(!instr.status) {
					int bodylength = getNestedBodyLength(currentIndex, trees);
					currentIndex += bodylength + 1;
//					currentIndex += ((FlowOperator)newroot).getBodyLength() + 1;
					
				}
				else {
					newroot.eval(state, thread, input, stack, individual, problem);
					used.set(currentIndex, used.get(currentIndex)+1);
					//if(!instr.status || ((DoubleData)input).x == 0.0){
					if(((DoubleData)input).value == 0.0){
						int bodylength = getNestedBodyLength(currentIndex, trees);
						currentIndex += bodylength + 1;
//						currentIndex += ((FlowOperator)newroot).getBodyLength() + 1;
						
					}
					else{
						beginIndex.push(currentIndex);
						endIndex.push(currentIndex + ((FlowOperator)newroot).getBodyLength());
						currentIndex ++;
						executeProgramBlock(state, thread, input, stack, individual, problem);
					}
				}
				
				break;
			case GPTreeStruct.ITERATION: //iteration
				newroot = instr.child.children[0]; //got the flow control operator
				newroot.eval(state, thread, input, stack, individual, problem);
				used.set(currentIndex, used.get(currentIndex)+1);
				//if(((DoubleData)input).x == 0.0){
				if(((DoubleData)input).value == 0.0){
					int bodylength = getNestedBodyLength(currentIndex, trees);
					currentIndex += bodylength + 1;
//					currentIndex += ((FlowOperator)newroot).getBodyLength() + 1;
					
				}
				else if(currentIterTimes >= maxIterTimes || ! instr.status){ 
					//an iteration will be effective only when it 1) contains effective instructions 
					//and 2) contains an effective register which is simultaneously source and destination register
					//if we have run out of iteration time, the loop body only execute once
					currentIndex++;
				}
				else{
					beginIndex.push(currentIndex);
					endIndex.push(currentIndex + ((FlowOperator)newroot).getBodyLength());
					currentIndex ++;
					executeProgramBlock(state, thread, input, stack, individual, problem);
				}
				break;
			default:
				state.output.fatal("we got an illegal instruction type in LGPFlowController\n");
				System.exit(1);
			}
			
			
		}while(currentIndex < trees.size() && currentIndex <= endIndex.peek());
		
		if(trees.get(beginIndex.peek()).type == GPTreeStruct.ITERATION && currentIterTimes < maxIterTimes){
			currentIterTimes ++;
			currentIndex = beginIndex.peek(); //back to the beginning place of the iteration
		}
		
		beginIndex.pop();
		endIndex.pop();
	}

	public static int getNestedBodyLength(int currentIndex, List<GPTreeStruct> trees) {
		//calculate the total body length of nested flow control body
		int res = 0;
		int cur = 0; //the instruction index in the body
		GPTreeStruct curtree = trees.get(currentIndex);
		if(curtree.type == GPTreeStruct.BRANCHING || curtree.type == GPTreeStruct.ITERATION) {
			res = Math.max(res, cur + ((FlowOperator)curtree.child.children[0]).getBodyLength());
		}
		
		//if the body contains more flowOperators
		while(cur < res && currentIndex + cur < trees.size()-1) {
			cur ++;
			curtree = trees.get(currentIndex + cur);
			if(curtree.type == GPTreeStruct.BRANCHING || curtree.type == GPTreeStruct.ITERATION) {
				res = Math.max(res, cur + ((FlowOperator)curtree.child.children[0]).getBodyLength());
			}
		}
		
		return res;
	}
}
