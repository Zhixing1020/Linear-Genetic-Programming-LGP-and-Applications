package zhixing.djss.ruleanalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ec.Fitness;
import ec.gp.koza.KozaFitness;
import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.ruleanalysis.RuleType;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.individual.LGPIndividual4DJSS;
import zhixing.djss.jobshop.SchedulingSet4Ind;

public class TestResult4CpxGP {
	
	private List<CpxGPInterface4DJSS> generationalRules;
	private List<Fitness> generationalTrainFitnesses;
	private List<Fitness> generationalValidationFitnesses;
	private List<Fitness> generationalTestFitnesses;
	private CpxGPInterface4DJSS bestInd;
	private Fitness bestTrainingFitness;
	private Fitness bestValidationFitness;
	private Fitness bestTestFitness;
	private DescriptiveStatistics generationalTimeStat;

	public static final long validationSimSeed = 483561;

	public TestResult4CpxGP() {
		generationalRules = new ArrayList<>();
		generationalTrainFitnesses = new ArrayList<>();
		generationalValidationFitnesses = new ArrayList<>();
		generationalTestFitnesses = new ArrayList<>();
	}

	public List<CpxGPInterface4DJSS> getGenerationalRules() {
		return generationalRules;
	}

	public CpxGPInterface4DJSS getGenerationalRule(int idx) {
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

	public CpxGPInterface4DJSS getBestRule() {
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

	public void setGenerationalRules(List<CpxGPInterface4DJSS> generationalRules) {
		this.generationalRules = generationalRules;
	}

	public void addGenerationalRule(CpxGPInterface4DJSS rule) {
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

	public void setBestRule(CpxGPInterface4DJSS bestRule) {
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

	public static TestResult4CpxGP readFromFile(File file, int numRegs, int maxIterations, boolean isMultiObj) {
		return ResultFileReader4LGP.readTestResultFromFile(file, numRegs, maxIterations, isMultiObj);
	}


	public void validate(List<Objective> objectives) {
		SchedulingSet4Ind validationSet =
				SchedulingSet4Ind.dynamicMissingSet(validationSimSeed, 0.95, 4.0, objectives, 50);

		Fitness validationFitness;
		if (objectives.size() == 1) {
			validationFitness = new KozaFitness();
			bestValidationFitness = new KozaFitness();
		}
		else {
			validationFitness = new MultiObjectiveFitness();
			bestValidationFitness = new MultiObjectiveFitness();
		}

		bestInd = generationalRules.get(0);

		bestInd.calcFitnessInd(bestValidationFitness, null, validationSet, objectives);
		generationalValidationFitnesses.add(bestValidationFitness);

//		System.out.println("Generation 0: validation fitness = " + bestValidationFitness.fitness());

		for (int i = 1; i < generationalRules.size(); i++) {
			generationalRules.get(i).calcFitnessInd(validationFitness, null, validationSet, objectives);
			generationalValidationFitnesses.add(validationFitness);


//			System.out.println("Generation " + i + ": validation fitness = " + validationFitness.fitness());

			if (validationFitness.betterThan(bestValidationFitness)) {
				bestInd = generationalRules.get(i);
				bestTrainingFitness = generationalTrainFitnesses.get(i);
				bestValidationFitness = validationFitness;
			}
		}
	}
}
