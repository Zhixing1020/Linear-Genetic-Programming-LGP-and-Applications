package zhixing.symbreg_multitarget.algorithm.semantic.library;

import java.util.HashSet;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.GPProblem;
import yimei.jss.gp.data.DoubleData;
import zhixing.cpxInd.algorithm.semantic.library.LibraryItem;
import zhixing.cpxInd.algorithm.semantic.library.SVSet;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;
import zhixing.cpxInd.algorithm.semantic.library.SemanticVector;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.symbreg_multitarget.optimization.GPSymbolicRegressionMultiTarget;

public class SemanticLibrary4SRMT extends SemanticLibrary{

	@Override
	public void initialize(final EvolutionState state, final int thread, final Problem problem) {
		privateProblem = (GPSymbolicRegressionMultiTarget) problem;
		
		GPSymbolicRegressionMultiTarget SRMTproblem = (GPSymbolicRegressionMultiTarget) privateProblem;
		
		if(SRMTproblem.getDatanum() > maxNumInput) {
			System.err.print("The number of data instances exceeds the maximum number of data of semantic library\n");
			System.exit(1);
		}
		
		inRefSV = new SVSet(NUMREF, SRMTproblem.getDatanum(), numRegs);
		
		for(SemanticVector sv : inRefSV ) {
	    	 sv.randSetSem(state, thread, lowbound, upbound);
	     }
		
		for(int i =0;i<maxNumInstr;i++){
			//GPTreeStruct tree = new GPTreeStruct();
			GPTreeStruct[] ins_seq = new GPTreeStruct[maxCombine];
			
//			GPTreeStruct tree = (GPTreeStruct)(state.parameters.getInstanceForParameterEq(
//                    privateParameter,null,GPTree.class));
			GPTreeStruct tree = ins_prototype.lightClone();
			tree.owner = null; 
            tree.status = false;
            tree.effRegisters = new HashSet<Integer>(0);
            //tree.setup(state,privateParameter);
            
			tree.buildTree(state, thread);
			
			ins_seq[0] = tree;
			
			LibraryItem item = new LibraryItem(ins_seq, this);

			//check whether these instructions exist in the library
//			SVSet outSV = null;
//			boolean noexist = checkNOexistence(state, thread, ins_seq, outSV);
			boolean noexist = checkNOexistence(state, thread, item);

				if(noexist) {
//					ItemList.add(new LibraryItem(ins_seq, outSV, 0.0, fit_prototype));
					ItemList.add(item);
				}
			}
				
		
	}
	
	@Override
	public SVSet evalInstr(EvolutionState state, int thread, LibraryItem item, GPProblem problem) {
		
		if(i_prototype.getRegisters().length != numRegs) {
			System.err.print("the semantic library has inconsistent number of registers with instructions.\n");
			System.exit(1);
		}
		
		GPSymbolicRegressionMultiTarget SRMTproblem = (GPSymbolicRegressionMultiTarget) problem;
		
		SVSet tmpout = new SVSet(NUMREF, SRMTproblem.getDatanum(), numRegs);
		
		//see the instruction as an individual
		//1) reset i_prototype  2) add instructions to form the i_prototype
		while(i_prototype.getTreeStructs().size()>0) {
			i_prototype.removeTree(0);
		}
		
		for(int i = 0; i<item.instructions.length; i++) {
			i_prototype.addTree(i, item.instructions[i]);
		}
		
		for(int svi = 0; svi<NUMREF; svi++) {
			
			SemanticVector inputSV = inRefSV.get(svi);
			SemanticVector outSV = new SemanticVector(SRMTproblem.getDatanum(), numRegs);
			
			for(int in = 0; in<SRMTproblem.getDatanum(); in++) {
				DoubleData tmp = new DoubleData();
				
				SRMTproblem.X = new double[SRMTproblem.getDatadim()];
				DoubleData input = new DoubleData();
				
				//set inputs and registers
				for(int d = 0; d<SRMTproblem.getDatadim(); d++) {
					SRMTproblem.X[d] = SRMTproblem.getData().get(in)[d];
				}
				for(int r = 0; r<numRegs; r++) {
					i_prototype.setRegister(r, inputSV.get(in*numRegs+r));
				}
				
				i_prototype.getFlowController().execute(state, thread, input, null, i_prototype, problem);
				
				outSV.setSemByRegister(in, i_prototype.getRegisters());
				
				
			}
			
			tmpout.set(svi, outSV);
		}
		
		item.outSVs = tmpout;
		item.evaluated = true;
		
		state.nodeEvaluation += (NUMREF) * i_prototype.size();
		
		return tmpout;
	}
}
