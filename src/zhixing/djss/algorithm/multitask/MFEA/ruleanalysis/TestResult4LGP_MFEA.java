package zhixing.djss.algorithm.multitask.MFEA.ruleanalysis;

import java.io.File;
import java.util.List;

import zhixing.djss.ruleanalysis.*;

public class TestResult4LGP_MFEA extends TestResult4CpxGP{
	public static TestResult4CpxGP readFromFileMFEA(File file, int numRegs, int maxIterations, int taskind, int numtests){
		return ResultFileReader4LGP_MFEA.readTestResultFromFile(file, numRegs, maxIterations, taskind, numtests, null);
	}
	public static TestResult4CpxGP readFromFileMFEA(File file, int numRegs, int maxIterations, int taskind, int numtests, List<Integer> outputRegs){
		return ResultFileReader4LGP_MFEA.readTestResultFromFile(file, numRegs, maxIterations, taskind, numtests, outputRegs);
	}
}
