package zhixing.cpxInd.algorithm.Grammar.individual;

import zhixing.cpxInd.individual.GPTreeStruct;

public class GPTreeStructGrammar extends GPTreeStruct{
	//each instruction manages the type matrices for the destination register and source registers
	
	//each instruction remembers its InstructionConstraint
	
	int[] typeMatrix;  //each register has a type vector
	
	public DTNode grammarNode;
	
	public Object clone() {
		GPTreeStructGrammar ins = (GPTreeStructGrammar) super.clone();
		
		//grammarNode is not cloned here, please use DerivationTree.rematch(...) to identify the grammarNode in new individual
		
		return ins;
	}
}
