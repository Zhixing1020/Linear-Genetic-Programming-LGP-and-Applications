package zhixing.djss.algorithm.Multiform.ruleanalysis;

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
import yimei.jss.rule.evolved.GPRule;
import yimei.jss.ruleanalysis.RuleType;
import yimei.util.lisp.LispParser;
import yimei.util.lisp.LispSimplifier;
import zhixing.djss.algorithm.Multiform.individual.LGPIndividual4MForm4DJSS;
import zhixing.djss.algorithm.Multiform.individual.TGPIndividual4MForm4DJSS;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.individual.LGPIndividual4DJSS;
import zhixing.djss.ruleanalysis.ResultFileReader4LGP;
import zhixing.djss.ruleanalysis.TestResult4CpxGP;

public class ResultFileReader4Multiform4DJSS extends ResultFileReader4LGP {

	public static TestResult4Multiform4DJSS readTestResultFromFile4MultiForm(File file, int numRegs, int maxIterations,
			boolean isMultiObjective) {
		TestResult4Multiform4DJSS result = new TestResult4Multiform4DJSS();
		LGPIndividual4MForm4DJSS lgp_prog = null;
		LGPIndividual4MForm4DJSS lgp_prog2 = null;
		TGPIndividual4MForm4DJSS tree_prog = null;
		TGPIndividual4MForm4DJSS tree_prog2 = null;
		CpxGPInterface4DJSS best_prog = null;
		String line;

		Fitness fitness_lgp = null;
		Fitness fitness_lgp2 = null;
		Fitness fitness_tgp = null;
		Fitness fitness_tgp2 = null;
		Fitness best_fitness = null;

		GPTree tree = null;

		int generations = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			while (!(line = br.readLine()).equals("Best Individual of Run:")) {
				if (line.startsWith("Generation")) {

					lgp_prog = new LGPIndividual4MForm4DJSS();
					lgp_prog.resetIndividual(numRegs, maxIterations);

					lgp_prog2 = new LGPIndividual4MForm4DJSS();
					lgp_prog2.resetIndividual(numRegs, maxIterations);

					tree_prog = new TGPIndividual4MForm4DJSS();
					tree_prog2 = new TGPIndividual4MForm4DJSS();
					
					String expression;

					br.readLine(); // best individual

					// read the first LGP program
					br.readLine(); // subpopulation
					br.readLine(); // evaluate
					line = br.readLine(); // fitness
					fitness_lgp = readFitnessFromLine(line, isMultiObjective);
					expression = br.readLine();

					// reading LGP program
					while (!expression.startsWith("#")) {
						if (expression.startsWith("//")) {
							// expression = br.readLine();
							// continue;
							expression = expression.substring(2);
						}

						// remove the "Ins index"
						int nextWhiteSpaceIdx = expression.indexOf('\t');
						expression = expression.substring(nextWhiteSpaceIdx + 1, expression.length());
						expression.trim();

						// expression = LispSimplifier.simplifyExpression(expression);
						tree = LispParser.parseJobShopRule(expression);
						lgp_prog.addTree(lgp_prog.getTreesLength(), tree);
						expression = br.readLine();
					}

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

					// read another LGP program
//					br.readLine();  //subpopulation
//					br.readLine();  //evaluate
//					line = br.readLine(); //fitness
//					fitness_lgp2 = readFitnessFromLine(line, isMultiObjective);
//					expression = br.readLine();
//					
//					//reading LGP program
//					while(!expression.startsWith("#")){
//						if(expression.startsWith("//")){
//							//expression = br.readLine();
//							//continue;
//							expression = expression.substring(2);
//						}
//						
//						//remove the "Ins index"
//						int nextWhiteSpaceIdx = expression.indexOf('\t');
//			            expression = expression.substring(nextWhiteSpaceIdx + 1,
//			                    expression.length());
//			            expression.trim();
//						
//						//expression = LispSimplifier.simplifyExpression(expression);
//						tree = LispParser.parseJobShopRule(expression);
//						lgp_prog2.addTree(lgp_prog2.getTreesLength(), tree);
//						expression = br.readLine();
//					}

					// read another TGP program
//					br.readLine(); // subpopulation
//					br.readLine(); // evaluate
//					line = br.readLine(); // fitness
//					fitness_tgp2 = readFitnessFromLine(line, isMultiObjective);
//					br.readLine();
//					expression = br.readLine();
//
//					expression = LispSimplifier.simplifyExpression(expression);
//					tree = LispParser.parseJobShopRule(expression);
//					tree_prog2.addTree(tree);

					// record the best rule for each generation
					//LGP+TGP
					if (((MultiObjectiveFitness) fitness_lgp).objectives[0] < ((MultiObjectiveFitness) fitness_tgp).objectives[0]) {
						best_prog = (CpxGPInterface4DJSS) lgp_prog;
						best_fitness = fitness_lgp;
					} else {
						best_prog = (CpxGPInterface4DJSS) tree_prog;
						best_fitness = fitness_tgp;
					}
					
					//LGP+LGP
//					if(((MultiObjectiveFitness)fitness_lgp).objectives[0]
//                    		<((MultiObjectiveFitness)fitness_lgp2).objectives[0]) {
//                    	best_prog = lgp_prog;
//                    	best_fitness = fitness_lgp;
//                    }
//                    else {
//                    	best_prog = lgp_prog2;
//                    	best_fitness = fitness_lgp2;
//                    }

					//TGP+TGP
//					 if(((MultiObjectiveFitness)fitness_tgp).objectives[0]
//							 <((MultiObjectiveFitness)fitness_tgp2).objectives[0]) {
//						 best_prog = tree_prog;
//						 best_fitness = fitness_tgp; 
//					 } 
//					 else { 
//						 best_prog = tree_prog2; 
//						 best_fitness = fitness_tgp2; 
//					 }
					 
					//LGP+TGP+LGP
//					if (((MultiObjectiveFitness) fitness_lgp).objectives[0] < ((MultiObjectiveFitness) fitness_tgp).objectives[0]) {
//						if(((MultiObjectiveFitness) fitness_lgp).objectives[0] < ((MultiObjectiveFitness) fitness_lgp2).objectives[0]) {
//							best_prog = lgp_prog;
//							best_fitness = fitness_lgp;
//						}
//						else {
//							best_prog = lgp_prog2;
//							best_fitness = fitness_lgp2;
//						}
//					} else {
//						if(((MultiObjectiveFitness) fitness_tgp).objectives[0] < ((MultiObjectiveFitness) fitness_lgp2).objectives[0]) {
//							best_prog = tree_prog;
//							best_fitness = fitness_tgp;
//						}
//						else {
//							best_prog = lgp_prog2;
//							best_fitness = fitness_lgp2;
//						}
//						
//					}

					result.addGenerationalRule(best_prog);
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
			result.setBestRule(best_prog);
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

	public static List<String> readLispExpressionFromFile4LGP(File file, int numRegs, int maxIterations,
			boolean isMultiObjective) {
		List<String> expressions = new ArrayList<>();

		String line;
		LGPIndividual4MForm4DJSS rule = null;
		String ruleString = "";
		Fitness fitness = null;

		GPTree tree = null;

		int generations = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			while (!(line = br.readLine()).equals("Best Individual of Run:")) {
				if (line.startsWith("Generation")) {

					// read LGP
					rule = new LGPIndividual4MForm4DJSS();
					rule.resetIndividual(numRegs, maxIterations);
					ruleString = "";

					br.readLine();
					br.readLine();
					br.readLine();
					line = br.readLine();
					fitness = readFitnessFromLine(line, isMultiObjective);
					String expression = br.readLine();

					while (!expression.startsWith("#")) {

						ruleString += expression + "\n";

						if (expression.startsWith("//")) {
							// expression = br.readLine();
							// continue;
							expression = expression.substring(2);
						}

						// remove the "Ins index"
						int nextWhiteSpaceIdx = expression.indexOf('\t');
						expression = expression.substring(nextWhiteSpaceIdx + 1, expression.length());
						expression.trim();

						// expression = LispSimplifier.simplifyExpression(expression);
						tree = LispParser.parseJobShopRule(expression);
						rule.addTree(rule.getTreesLength(), tree);

						expression = br.readLine();
					}

					ruleString += "#\n";

					expressions.add(ruleString);

					// read TGP
					br.readLine(); // subpopulation
					br.readLine(); // evaluate
					line = br.readLine(); // fitness
					fitness = readFitnessFromLine(line, isMultiObjective);
					br.readLine();
					expression = br.readLine();

					expression = LispSimplifier.simplifyExpression(expression);
					tree = LispParser.parseJobShopRule(expression);
					// tree_prog.addTree(tree);
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return expressions;
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
