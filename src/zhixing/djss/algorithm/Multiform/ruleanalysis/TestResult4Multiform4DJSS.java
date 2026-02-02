package zhixing.djss.algorithm.Multiform.ruleanalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ec.Fitness;
import ec.gp.koza.KozaFitness;
import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.jobshop.Objective;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.individual.LGPIndividual4DJSS;
import zhixing.djss.jobshop.SchedulingSet4Ind;
import zhixing.djss.ruleanalysis.*;

public class TestResult4Multiform4DJSS extends TestResult4CpxGP{
	
//	protected List<CpxGPIndividual> generationalCpxRules;
//	protected CpxGPIndividual bestCpxInd;
//	
//	public TestResult4LGP() {
//		generationalRules = new ArrayList<>();
//	}
//
//	public List<LGPIndividual> getGenerationalRules() {
//		return generationalRules;
//	}
//
//	public LGPIndividual getGenerationalRule(int idx) {
//		return generationalRules.get(idx);
//	}
//	
//	public void addGenerationalRule(CpxGPIndividual rule) {
//		this.generationalCpxRules.add(rule);
//	}
//	
//	public void setGenerationalRules(List<LGPIndividual> generationalRules) {
//		this.generationalRules = generationalRules;
//	}
//	
//	public void setBestRule(CpxGPIndividual bestCpxRule) {
//		this.bestCpxInd = bestCpxRule;
//	}
//	
//	public void validate(List<Objective> objectives) {
//		SchedulingSet4Ind validationSet =
//				SchedulingSet4Ind.dynamicMissingSet(validationSimSeed, 0.95, 4.0, objectives, 50);
//
//		Fitness validationFitness;
//		if (objectives.size() == 1) {
//			validationFitness = new KozaFitness();
//			bestValidationFitness = new KozaFitness();
//		}
//		else {
//			validationFitness = new MultiObjectiveFitness();
//			bestValidationFitness = new MultiObjectiveFitness();
//		}
//
//		bestInd = generationalRules.get(0);
//
//		bestInd.calcFitnessInd(bestValidationFitness, null, validationSet, objectives);
//		generationalValidationFitnesses.add(bestValidationFitness);
//
////		System.out.println("Generation 0: validation fitness = " + bestValidationFitness.fitness());
//
//		for (int i = 1; i < generationalRules.size(); i++) {
//			generationalRules.get(i).calcFitnessInd(validationFitness, null, validationSet, objectives);
//			generationalValidationFitnesses.add(validationFitness);
//
//
////			System.out.println("Generation " + i + ": validation fitness = " + validationFitness.fitness());
//
//			if (validationFitness.betterThan(bestValidationFitness)) {
//				bestInd = generationalRules.get(i);
//				bestTrainingFitness = generationalTrainFitnesses.get(i);
//				bestValidationFitness = validationFitness;
//			}
//		}
//	}
	
	public static TestResult4Multiform4DJSS readFromFileMultiForm(File file, int numRegs, int maxIterations, boolean isMultiObj) {
		return ResultFileReader4Multiform4DJSS.readTestResultFromFile4MultiForm(file, numRegs, maxIterations, isMultiObj);
	}
	
//	public static TestResult4LGP readFromFileMPMO(File file, int numRegs, int maxIterations, int taskind, int numtests, List<Integer> outputRegs){
//		return ResultFileReader4Multiform.readTestResultFromFile(file, numRegs, maxIterations, taskind, numtests, outputRegs);
//	}
}
