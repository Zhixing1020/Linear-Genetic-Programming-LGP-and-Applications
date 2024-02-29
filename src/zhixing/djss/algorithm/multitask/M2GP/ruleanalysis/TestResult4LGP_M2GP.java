package zhixing.djss.algorithm.multitask.M2GP.ruleanalysis;

import java.io.File;

import zhixing.djss.ruleanalysis.TestResult4CpxGP;

public class TestResult4LGP_M2GP extends TestResult4CpxGP{
	public static TestResult4CpxGP readFromFileM2GP(File file, int numRegs, int maxIterations, int taskind, int numtests){
		return ResultFileReader4LGP_M2GP.readTestResultFromFile(file, numRegs, maxIterations, taskind, numtests);
	}
}
