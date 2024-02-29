package zhixing.djss.algorithm.GrammarTGP.individual.reproduce;

import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.koza.CrossoverPipeline;
import yimei.jss.gp.terminal.TerminalERC;
import zhixing.cpxInd.algorithm.Grammar.individual.primitives.NumericalValue;
import zhixing.cpxInd.algorithm.GrammarTGP.individual.primitives.BranchingTGP;
import zhixing.cpxInd.algorithm.GrammarTGP.individual.reproduce.GrammarCrossoverPipeline;

public class GrammarCrossoverPipeline4DJSS extends GrammarCrossoverPipeline {

	protected boolean grammarCompatible(final GPNode inner1, final GPNode inner2) {
		//swap only when the two selected nodes are the same type
		//or both of their parents are IF or NULL
		boolean res = super.grammarCompatible(inner1, inner2);
		
		return res || (inner1 instanceof TerminalERC && inner2 instanceof TerminalERC);
		
//		if(inner1 instanceof BranchingTGP && inner2 instanceof BranchingTGP
//				|| inner1 instanceof TerminalERC && inner2 instanceof TerminalERC
//				|| inner1 instanceof NumericalValue && inner2 instanceof NumericalValue
//				|| inner1.expectedChildren() == 2 && inner2.expectedChildren() == 2/*since we only consider binary arithmetic functions in the experiments*/) {
//			return true;
//		}
//		if(inner1.expectedChildren() == 2 && inner2 instanceof BranchingTGP) {
//			if(inner1.parent instanceof BranchingTGP && inner2.parent instanceof BranchingTGP
//					|| inner1.atDepth() == 0 && inner2.atDepth() == 0) {
//				return true;
//			}
//		}
//		if(inner2.expectedChildren() == 2 && inner1 instanceof BranchingTGP) {
//			if(inner2.parent instanceof BranchingTGP && inner1.parent instanceof BranchingTGP
//					|| inner1.atDepth() == 0 && inner2.atDepth() == 0) {
//				return true;
//			}
//		}
//		
//		return false;
	}
}
