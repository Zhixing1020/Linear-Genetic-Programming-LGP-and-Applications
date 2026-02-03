package zhixing.symbolic_classification.ruleanalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ec.Fitness;
import zhixing.symbolic_classification.individual.CpxGPInterface4Class;
//import zhixing.symbreg_multitarget.individual.CpxGPInterface4SRMT;
//import zhixing.symbreg_multitarget.ruleanalysis.ResultFileReader4LGPSRMT;
//import zhixing.symbreg_multitarget.ruleanalysis.ResultFileReader4TGPSRMT;
//import zhixing.symbreg_multitarget.ruleanalysis.TestResult4CpxGPSRMT;

public class TestResult4CpxGPClass {

	private List<CpxGPInterface4Class> generationalRules;
	private List<Fitness> generationalTrainFitnesses;
	private List<Fitness> generationalValidationFitnesses;
	private List<Fitness> generationalTestFitnesses;
	private CpxGPInterface4Class bestInd;
	private Fitness bestTrainingFitness;
	private Fitness bestValidationFitness;
	private Fitness bestTestFitness;
	private DescriptiveStatistics generationalTimeStat;
	
	public static final long validationSimSeed = 483561;

	public TestResult4CpxGPClass() {
		generationalRules = new ArrayList<>();
		generationalTrainFitnesses = new ArrayList<>();
		generationalValidationFitnesses = new ArrayList<>();
		generationalTestFitnesses = new ArrayList<>();
	}

	public List<CpxGPInterface4Class> getGenerationalRules() {
		return generationalRules;
	}

	public CpxGPInterface4Class getGenerationalRule(int idx) {
		return generationalRules.get(idx);
	}

	public List<Fitness> getGenerationalTrainFitnesses() {
		return generationalTrainFitnesses;
	}

	public Fitness getGenerationalTrainFitness(int idx) {
		return generationalTrainFitnesses.get(idx);
	}

	public List<Fitness> getGenerationalValidationFitnesses() {
		return generationalValidationFitnesses;
	}

	public Fitness getGenerationalValidationFitness(int idx) {
		return generationalValidationFitnesses.get(idx);
	}

	public List<Fitness> getGenerationalTestFitnesses() {
		return generationalTestFitnesses;
	}

	public Fitness getGenerationalTestFitness(int idx) {
		return generationalTestFitnesses.get(idx);
	}

	public CpxGPInterface4Class getBestRule() {
		return bestInd;
	}

	public Fitness getBestTrainingFitness() {
		return bestTrainingFitness;
	}

	public Fitness getBestValidationFitness() {
		return bestValidationFitness;
	}

	public Fitness getBestTestFitness() {
		return bestTestFitness;
	}

	public DescriptiveStatistics getGenerationalTimeStat() {
		return generationalTimeStat;
	}

	public double getGenerationalTime(int gen) {
		return generationalTimeStat.getElement(gen);
	}

	public void setGenerationalRules(List<CpxGPInterface4Class> generationalRules) {
		this.generationalRules = generationalRules;
	}

	public void addGenerationalRule(CpxGPInterface4Class rule) {
		this.generationalRules.add(rule);
	}

	public void setGenerationalTrainFitnesses(List<Fitness> generationalTrainFitnesses) {
		this.generationalTrainFitnesses = generationalTrainFitnesses;
	}

	public void addGenerationalTrainFitness(Fitness f) {
		this.generationalTrainFitnesses.add(f);
	}

	public void setGenerationalValidationFitnesses(List<Fitness> generationalValidationFitnesses) {
		this.generationalValidationFitnesses = generationalValidationFitnesses;
	}

	public void addGenerationalValidationFitnesses(Fitness f) {
		this.generationalValidationFitnesses.add(f);
	}

	public void setGenerationalTestFitnesses(List<Fitness> generationalTestFitnesses) {
		this.generationalTestFitnesses = generationalTestFitnesses;
	}

	public void addGenerationalTestFitnesses(Fitness f) {
		this.generationalTestFitnesses.add(f);
	}

	public void setBestRule(CpxGPInterface4Class bestRule) {
		this.bestInd = bestRule;
	}

	public void setBestTrainingFitness(Fitness bestTrainingFitness) {
		this.bestTrainingFitness = bestTrainingFitness;
	}

	public void setBestValidationFitness(Fitness bestValidationFitness) {
		this.bestValidationFitness = bestValidationFitness;
	}

	public void setBestTestFitness(Fitness bestTestFitness) {
		this.bestTestFitness = bestTestFitness;
	}

	public void setGenerationalTimeStat(DescriptiveStatistics generationalTimeStat) {
		this.generationalTimeStat = generationalTimeStat;
	}
	
	public static TestResult4CpxGPClass readFromFile4LGP(File file, int numRegs, int maxIterations, boolean isMultiObj, List<Integer> outputRegs) {
		return ResultFileReader4LGPClass.readTestResultFromFile(file, numRegs, maxIterations, isMultiObj, outputRegs);
	}
	
	public static TestResult4CpxGPClass readFromFile4TGP(File file, boolean isMultiObj) {
		return ResultFileReader4TGPClass.readTestResultFromFile(file, isMultiObj);
	}
}
