package zhixing.symbolicregression.ruleanalysis;

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
//import zhixing.jss.cpxInd.individual.LGPIndividual;
import zhixing.symbolicregression.individual.LGPIndividual4SR;
import zhixing.symbolicregression.util.LispParser;

public class ResultFileReader4LGPSR{
	
	public static TestResult4CpxGPSR readTestResultFromFile(File file,
            int numRegs,
            int maxIterations,
            boolean isMultiObjective) {
		TestResult4CpxGPSR result = new TestResult4CpxGPSR();
		LGPIndividual4SR rule = null;
		String line;
		Fitness fitness = null;
		
		GPTree tree = null;
		
		int generations = 0;
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			while (!(line = br.readLine()).equals("Best Individual of Run:")) {
				if (line.startsWith("Generation")) {
					
					rule = new LGPIndividual4SR();
					rule.resetIndividual(numRegs, maxIterations);
					
					br.readLine();
					br.readLine();
					br.readLine();
					line = br.readLine();
					fitness = readFitnessFromLine(line, isMultiObjective);
					String expression = br.readLine();
					
					while(!expression.startsWith("#")){
						if(expression.startsWith("//")){
							//expression = br.readLine();
							//continue;
							expression = expression.substring(2);
						}
						
						//remove the "Ins index"
						int nextWhiteSpaceIdx = expression.indexOf('\t');
			            expression = expression.substring(nextWhiteSpaceIdx + 1,
			                    expression.length());
			            expression.trim();
						
						//expression = LispSimplifier.simplifyExpression(expression);
						tree = LispParser.parseSymRegRule(expression);
						rule.addTree(rule.getTreesLength(), tree);
						expression = br.readLine();
					}
					
					result.addGenerationalRule(rule);
					result.addGenerationalTrainFitness(fitness);
					result.addGenerationalValidationFitnesses((Fitness)fitness.clone());
					result.addGenerationalTestFitnesses((Fitness)fitness.clone());
				}
			
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Set the best rule as the rule in the last generation
		if(rule != null){
			result.setBestRule(rule);
			result.setBestTrainingFitness(fitness);
		}

		return result;
	}
	
	protected static Fitness readFitnessFromLine(String line, boolean isMultiobjective) {
        if (isMultiobjective) {
            // TODO read multi-objective fitness line
            String[] spaceSegments = line.split("\\s+");
            String[] equation = spaceSegments[1].split("=");
            double fitness = Double.valueOf(equation[1]);
            KozaFitness f = new KozaFitness();
            f.setStandardizedFitness(null, fitness);

            return f;
        }
        else {
            String[] spaceSegments = line.split("\\s+");
            String[] fitVec = spaceSegments[1].split("\\[|\\]");
            double fitness = Double.valueOf(fitVec[1]);
            MultiObjectiveFitness f = new MultiObjectiveFitness();
            f.objectives = new double[1];
            f.objectives[0] = fitness;

            return f;
        }
    }
 
 public static List<String> readLispExpressionFromFile4LGP(File file,
		 int numRegs,
         int maxIterations,
         boolean isMultiObjective) {
		List<String> expressions = new ArrayList<>();
		
		String line;
		LGPIndividual4SR rule = null;
		String ruleString = "";
		Fitness fitness = null;
		
		GPTree tree = null;
		
		int generations = 0;
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			while (!(line = br.readLine()).equals("Best Individual of Run:")) {
				if (line.startsWith("Generation")) {
					
					rule = new LGPIndividual4SR();
					rule.resetIndividual(numRegs, maxIterations);
					ruleString = "";
					
					br.readLine();
					br.readLine();
					br.readLine();
					line = br.readLine();
					fitness = readFitnessFromLine(line, isMultiObjective);
					String expression = br.readLine();
					
					while(!expression.startsWith("#")){
						
						ruleString += expression + "\n";
						
						if(expression.startsWith("//")){
							//expression = br.readLine();
							//continue;
							expression = expression.substring(2);
						}
						
						//remove the "Ins index"
						int nextWhiteSpaceIdx = expression.indexOf('\t');
			            expression = expression.substring(nextWhiteSpaceIdx + 1,
			                    expression.length());
			            expression.trim();
						
						//expression = LispSimplifier.simplifyExpression(expression);
						tree = LispParser.parseSymRegRule(expression);
						rule.addTree(rule.getTreesLength(), tree);
						
						expression = br.readLine();
					}
					
					ruleString += "#\n";
					
					expressions.add(ruleString);
				}
			
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return expressions;
	}
}
