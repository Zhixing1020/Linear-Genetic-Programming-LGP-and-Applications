package zhixing.symbolicregression.algorithm.multiform.ruleanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import ec.Fitness;
import ec.gp.GPTree;
import ec.gp.koza.KozaFitness;
import ec.multiobjective.MultiObjectiveFitness;
import yimei.util.lisp.LispSimplifier;
import zhixing.cpxInd.individual.CpxGPInterface4Problem;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.symbolicregression.algorithm.multiform.individual.LGPIndividual4SR_MForm;
import zhixing.symbolicregression.algorithm.multiform.individual.TGPIndividual4SR_MForm;
import zhixing.symbolicregression.individual.CpxGPInterface4SR;
import zhixing.symbolicregression.individual.LGPIndividual4SR;
import zhixing.symbolicregression.ruleanalysis.ResultFileReader4LGPSR;
import zhixing.symbolicregression.ruleanalysis.TestResult4CpxGPSR;
import zhixing.symbolicregression.util.LispParser;

public class ResultFileReader4MRGP4SR extends ResultFileReader4LGPSR {
	public static TestResult4CpxGPSRMF readTestResultFromFile4MultiForm(File file, int numRegs, int maxIterations,
			boolean isMultiObjective) {
		TestResult4CpxGPSRMF result = new TestResult4CpxGPSRMF();
		LGPIndividual4SR_MForm lgp_prog = null;
		LGPIndividual4SR_MForm lgp_prog2 = null;
		TGPIndividual4SR_MForm tree_prog = null;
		TGPIndividual4SR_MForm tree_prog2 = null;
		CpxGPInterface4SR best_prog = null;
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

					lgp_prog = new LGPIndividual4SR_MForm();
					lgp_prog.resetIndividual(numRegs, maxIterations);

					lgp_prog2 = new LGPIndividual4SR_MForm();
					lgp_prog2.resetIndividual(numRegs, maxIterations);

					tree_prog = new TGPIndividual4SR_MForm();
					tree_prog2 = new TGPIndividual4SR_MForm();
					
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
						tree = LispParser.parseSymRegRule(expression);
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
					tree = LispParser.parseSymRegRule(expression);
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
//						tree = LispParser.parseSymRegRule(expression);
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
//					tree = LispParser.parseSymRegRule(expression);
//					tree_prog2.addTree(tree);

					// record the best rule for each generation
					//LGP+TGP
					if (((MultiObjectiveFitness) fitness_lgp).objectives[0] < ((MultiObjectiveFitness) fitness_tgp).objectives[0]) {
						best_prog = lgp_prog;
						best_fitness = fitness_lgp;
					} else {
						best_prog = tree_prog;
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
	
//	private static Fitness readFitnessFromLine(String line, boolean isMultiobjective) {
//		if (isMultiobjective) {
//			// TODO read multi-objective fitness line
//			String[] spaceSegments = line.split("\\s+");
//			String[] equation = spaceSegments[1].split("=");
//			double fitness = Double.valueOf(equation[1]);
//			KozaFitness f = new KozaFitness();
//			f.setStandardizedFitness(null, fitness);
//
//			return f;
//		} else {
//			String[] spaceSegments = line.split("\\s+");
//			String[] fitVec = spaceSegments[1].split("\\[|\\]");
//			double fitness = Double.valueOf(fitVec[1]);
//			MultiObjectiveFitness f = new MultiObjectiveFitness();
//			f.objectives = new double[1];
//			f.objectives[0] = fitness;
//
//			return f;
//		}
//	}
}
