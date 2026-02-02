package zhixing.cpxInd.algorithm.UBanalysis.theorymodel.add1remove1.individual;

import zhixing.cpxInd.algorithm.semantic.individual.SLGPIndividual;
import zhixing.cpxInd.algorithm.semantic.library.SemanticVector;

public abstract class SLGPIndividualUB extends SLGPIndividual {

	//record all pairs of 1) semantics, 2) fitness of semantics, 3) instruction output semantics
	
	public void getThetasS() {
		//check the GPTreeStructSemantics in each instruction to find the maximum ||S_1 - S_2||=\theta_s
	}
}
