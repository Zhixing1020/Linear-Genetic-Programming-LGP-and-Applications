package zhixing.djss.algorithm.GrammarTGP.ruleanalysis;

import java.io.File;

import zhixing.djss.ruleanalysis.TestResult4CpxGP;

public class TestResult4GrammarTGP extends TestResult4CpxGP {

	public static TestResult4GrammarTGP readFromFileGrammarTGP(File file, int numRegs, int maxIterations, boolean isMultiObj) {
		return ResultFileReader4GrammarTGP.readTestResultFromFile4GrammarTGP(file, numRegs, maxIterations, isMultiObj);
	}
}
