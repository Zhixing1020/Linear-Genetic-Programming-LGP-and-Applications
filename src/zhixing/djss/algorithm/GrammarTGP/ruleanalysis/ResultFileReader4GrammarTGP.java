package zhixing.djss.algorithm.GrammarTGP.ruleanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ec.Fitness;
import ec.gp.GPTree;
import ec.gp.koza.KozaFitness;
import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.ruleanalysis.RuleType;
import yimei.util.lisp.LispParser;
import yimei.util.lisp.LispSimplifier;
import zhixing.cpxInd.algorithm.GrammarTGP.individual.TGPIndividual4Grammar;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.djss.algorithm.GrammarTGP.individual.TGPIndividual4Grammar4DJSS;
import zhixing.djss.individual.CpxGPInterface4DJSS;

public class ResultFileReader4GrammarTGP {
	public static TestResult4GrammarTGP readTestResultFromFile4GrammarTGP(File file, int numRegs, int maxIterations,
			boolean isMultiObjective) {
		TestResult4GrammarTGP result = new TestResult4GrammarTGP();
		TGPIndividual4Grammar tree_prog = null;
		CpxGPIndividual best_prog = null;
		String line;

		Fitness fitness_tgp = null;
		Fitness best_fitness = null;

		GPTree tree = null;

		int generations = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			while (!(line = br.readLine()).equals("Best Individual of Run:")) {
				if (line.startsWith("Generation")) {

					tree_prog = new TGPIndividual4Grammar4DJSS();
					
					String expression;

					br.readLine(); // best individual

					// reading TGP program
					br.readLine(); // subpopulation
					br.readLine(); // evaluate
					line = br.readLine(); // fitness
					fitness_tgp = readFitnessFromLine(line, isMultiObjective);
					br.readLine();
					expression = br.readLine();

					expression = LispSimplifier.simplifyExpression(expression);
					tree = LispParser.parseJobShopRule(expression);
					tree_prog.addTree(tree);

					// record the best rule for each generation
					//LGP+TGP
					if (best_fitness == null ||
							((MultiObjectiveFitness) fitness_tgp).objectives[0] < ((MultiObjectiveFitness) best_fitness).objectives[0]) {
						best_prog = tree_prog;
						best_fitness = fitness_tgp;
					}
					
					result.addGenerationalRule((CpxGPInterface4DJSS) best_prog);
					result.addGenerationalTrainFitness(best_fitness);
					result.addGenerationalValidationFitnesses((Fitness) best_fitness.clone());
					result.addGenerationalTestFitnesses((Fitness) best_fitness.clone());
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Set the best rule as the rule in the last generation
		if (best_prog != null) {
			result.setBestRule((CpxGPInterface4DJSS) best_prog);
			result.setBestTrainingFitness(best_fitness);
		}

		return result;
	}
	
	private static Fitness readFitnessFromLine(String line, boolean isMultiobjective) {
		if (isMultiobjective) {
			// TODO read multi-objective fitness line
			String[] spaceSegments = line.split("\\s+");
			String[] equation = spaceSegments[1].split("=");
			double fitness = Double.valueOf(equation[1]);
			KozaFitness f = new KozaFitness();
			f.setStandardizedFitness(null, fitness);

			return f;
		} else {
			String[] spaceSegments = line.split("\\s+");
			String[] fitVec = spaceSegments[1].split("\\[|\\]");
			double fitness = Double.valueOf(fitVec[1]);
			MultiObjectiveFitness f = new MultiObjectiveFitness();
			f.objectives = new double[1];
			f.objectives[0] = fitness;

			return f;
		}
	}
	
	public static List<String> readLispExpressionFromFile4TGP(File file, RuleType ruleType, boolean isMultiObjective) {
		List<String> expressions = new ArrayList<>();

		String line;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			while (!(line = br.readLine()).equals("Best Individual of Run:")) {
				if (line.startsWith("Generation")) {

					// read LGP
					String ruleString = "";

					br.readLine();
					br.readLine();
					br.readLine();
					line = br.readLine();
					String expression = br.readLine();

					while (!expression.startsWith("#")) {
						expression = br.readLine();
					}

					ruleString += "#\n";

					// read TGP
					br.readLine(); // subpopulation
					br.readLine(); // evaluate
					line = br.readLine(); // fitness
					// fitness = readFitnessFromLine(line, isMultiObjective);
					br.readLine();
					expression = br.readLine();

					expression = LispSimplifier.simplifyExpression(expression);
					// tree = LispParser.parseJobShopRule(expression);

					expressions.add(expression);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return expressions;
	}
}
