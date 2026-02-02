package zhixing.symbolicregression.algorithm.multiform.ruleanalysis;

import java.io.File;

import zhixing.symbolicregression.ruleanalysis.ResultFileReader4LGPSR;
import zhixing.symbolicregression.ruleanalysis.TestResult4CpxGPSR;

public class TestResult4CpxGPSRMF extends TestResult4CpxGPSR{
	public static TestResult4CpxGPSRMF readFromFile(File file, int numRegs, int maxIterations, boolean isMultiObj) {
		return ResultFileReader4MRGP4SR.readTestResultFromFile4MultiForm(file, numRegs, maxIterations, isMultiObj);
	}
}
