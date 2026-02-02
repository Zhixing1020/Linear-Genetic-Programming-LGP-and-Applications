package zhixing.symbreg_multitarget.ruleanalysis;

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
import yimei.util.lisp.LispSimplifier;
import zhixing.symbreg_multitarget.individual.TGPIndividual4SRMT;
import zhixing.symbreg_multitarget.util.LispParser4SRMT;

public class ResultFileReader4TGPSRMT {

	public static TestResult4CpxGPSRMT readTestResultFromFile(File file,
            boolean isMultiObjective) {
		TestResult4CpxGPSRMT result = new TestResult4CpxGPSRMT();
		TGPIndividual4SRMT rule = null;
		String line;
		Fitness fitness = null;
		
		GPTree tree = null;
		
		int generations = 0;
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			while (!(line = br.readLine()).equals("Best Individual of Run:")) {
				if (line.startsWith("Generation")) {
					
					rule = new TGPIndividual4SRMT();
					
					br.readLine();  // best individual
					br.readLine();  // subpopulation
					br.readLine();  // evaluate
					line = br.readLine(); // fitness
					fitness = readFitnessFromLine(line, isMultiObjective);
					br.readLine();
					String expression = br.readLine();
					
					expression = LispSimplifier.simplifyExpression(expression);
					tree = LispParser4SRMT.parseSymRegRule(expression);
					rule.addTree(tree);
					
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
	
	public static List<String> readLispExpressionFromFile4TGP(File file,
			 int numRegs,
	         int maxIterations,
	         boolean isMultiObjective) {
			List<String> expressions = new ArrayList<>();
			
			String line;
			TGPIndividual4SRMT rule = null;
			String ruleString = "";
			Fitness fitness = null;
			
			GPTree tree = null;
			
			int generations = 0;
			
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				while (!(line = br.readLine()).equals("Best Individual of Run:")) {
					if (line.startsWith("Generation")) {
						
						rule = new TGPIndividual4SRMT();
						ruleString = "";
						
						br.readLine();
						br.readLine();
						br.readLine();
						line = br.readLine();
						fitness = readFitnessFromLine(line, isMultiObjective);
						String expression = br.readLine();
											
						expression = LispSimplifier.simplifyExpression(expression);
						tree = LispParser4SRMT.parseSymRegRule(expression);
						rule.addTree(tree);
						
						ruleString += expression + "\n";
						expressions.add(ruleString);
						
					}
				
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return expressions;
		}
}
