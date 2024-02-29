package zhixing.djss.algorithm.multitask.MultipopMultioutreg.ruleanalysis;

import java.io.File;
import java.util.List;

import zhixing.djss.ruleanalysis.*;

public class TestResult4LGP_MPMO extends TestResult4CpxGP{
	public static TestResult4CpxGP readFromFileMPMO(File file, int numRegs, int maxIterations, int taskind, int numtests){
		return ResultFileReader4LGP_MPMO.readTestResultFromFile(file, numRegs, maxIterations, taskind, numtests, null);
	}
	public static TestResult4CpxGP readFromFileMPMO(File file, int numRegs, int maxIterations, int taskind, int numtests, List<Integer> outputRegs){
		return ResultFileReader4LGP_MPMO.readTestResultFromFile(file, numRegs, maxIterations, taskind, numtests, outputRegs);
	}
}
