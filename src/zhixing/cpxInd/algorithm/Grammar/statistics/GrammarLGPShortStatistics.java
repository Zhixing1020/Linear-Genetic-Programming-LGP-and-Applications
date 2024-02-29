package zhixing.cpxInd.algorithm.Grammar.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.Grammar.individual.LGPIndividual4Grammar;
import zhixing.cpxInd.statistics.LGPStatistics;

public class GrammarLGPShortStatistics extends LGPStatistics {
	
	@Override
	public void finalStatistics(final EvolutionState state, final int result) {
		super.finalStatistics(state, result);
		
		//print the derivation tree of the best individual
//		LGPIndividual4Grammar rule = (LGPIndividual4Grammar) bestOfGeneration[0];
		
		//rule.getDerivationTree().newTree(rule, state, 0, rule.getTreesLength());
//		String bestGraphVizRule = rule.getDerivationTree().printTreeDOI();
		
		
	}
	
	@Override
	public void postEvaluationStatistics(final EvolutionState state) {
		super.postEvaluationStatistics(state);
		
		//===============debug=====================
		//print the derivation tree of the best rule
//		String sourcePath = System.getProperty("user.dir")+"\\";
//		File outFile = new File(sourcePath + "job." + 0 + ".bestrule.dot");
//		LGPIndividual4Grammar rule = (LGPIndividual4Grammar) bestOfGeneration[0];
//		
//		//rule.getDerivationTree().newTree(rule, state, 0, rule.getTreesLength());
//		String bestGraphVizRule = rule.getDerivationTree().printTreeDOI();
//
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile.getAbsoluteFile()));
//            writer.write(bestGraphVizRule);
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
	}
}
