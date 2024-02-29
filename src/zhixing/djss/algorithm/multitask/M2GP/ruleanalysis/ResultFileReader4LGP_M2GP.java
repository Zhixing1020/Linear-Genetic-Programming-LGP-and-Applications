package zhixing.djss.algorithm.multitask.M2GP.ruleanalysis;

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
import yimei.util.lisp.LispParser;
import zhixing.djss.individual.LGPIndividual4DJSS;
import zhixing.djss.ruleanalysis.TestResult4CpxGP;

public class ResultFileReader4LGP_M2GP {
	public static TestResult4CpxGP readTestResultFromFile(File file,
            int numRegs,
            int maxIterations,
            int subpopulation,
            int numtests){
		TestResult4CpxGP result = new TestResult4CpxGP();
		LGPIndividual4DJSS rule = null;
		String line;
		Fitness fitness = null;
		
		GPTree tree = null;
		
		int generations = 0;
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			while (!(line = br.readLine()).equals("Best Individual of Run:")) {
				if (line.startsWith("Generation")) {
					br.readLine(); //"Best Individual"
					int i = 0;
					for(;i<=subpopulation - 1;i++){
						br.readLine();//subpopulation
						br.readLine();//Evaluated
						br.readLine();//Fitness
						readLGPindFromLine(br,numRegs,maxIterations);
					}	
					br.readLine();//supopulation
					br.readLine();//Evaluated
					line = br.readLine();//Fitness
					fitness = readFitnessFromLine(line, false);  //false: is not MultiObjective, multiobjective is not support yet
					
					rule = readLGPindFromLine(br, numRegs, maxIterations);
					
					//============debug========== delete after use
					//rule = readLGPindFromLine(br, numRegs, maxIterations, subpopulation);
					//============
					
					result.addGenerationalRule(rule);
					result.addGenerationalTrainFitness(fitness);
					result.addGenerationalValidationFitnesses((Fitness)fitness.clone());
					result.addGenerationalTestFitnesses((Fitness)fitness.clone());
					
					for(i++;i<numtests;i++){
						br.readLine();//subpopulation
						br.readLine();//Evaluated
						br.readLine();//Fitness
						readLGPindFromLine(br,numRegs,maxIterations);
					}
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
	
	//=============debug====   delete after use
//	protected static LGPIndividual readLGPindFromLine(BufferedReader br, int numRegs, int maxIterations, int subpop){
//		LGPIndividual rule = new LGPIndividual();
//		List<Integer> tmp = new ArrayList<>();
//		tmp.add(subpop);
//		rule.resetIndividual(numRegs, maxIterations, tmp);
//		
//		try{
//			String expression = br.readLine();
//			
//			while(!expression.startsWith("#")){
//				if(expression.startsWith("//")){
//					//expression = br.readLine();
//					//continue;
//					expression = expression.substring(2);
//				}
//				
//				//remove the "Ins index"
//				int nextWhiteSpaceIdx = expression.indexOf('\t');
//	            expression = expression.substring(nextWhiteSpaceIdx + 1,
//	                    expression.length());
//	            expression.trim();
//				
//				//expression = LispSimplifier.simplifyExpression(expression);
//	            GPTree tree = LispParser.parseJobShopRule(expression);
//				rule.addTree(rule.getTreesLength(), tree);
//				expression = br.readLine();
//			}
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		return rule;
//	}
	//===========================
	
	protected static LGPIndividual4DJSS readLGPindFromLine(BufferedReader br, int numRegs, int maxIterations){
		LGPIndividual4DJSS rule = new LGPIndividual4DJSS();
		rule.resetIndividual(numRegs, maxIterations);
		
		try{
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
	            GPTree tree = LispParser.parseJobShopRule(expression);
				rule.addTree(rule.getTreesLength(), tree);
				expression = br.readLine();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return rule;
	}
	
	protected static String readLGPindStringFromLine(BufferedReader br){
		String ruleString="";
		
		try{
			String expression = br.readLine();
			
			while(!expression.startsWith("#")){
				
				ruleString += expression + "\n";
				
				expression = br.readLine();
			}
			
			ruleString += "#\n";
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		return ruleString;
	}
	
	public static List<String> readLispExpressionFromFile4LGP(File file,
			 int numRegs,
            int maxIterations,
            int subpopulation,
            int numtests) {
			List<String> expressions = new ArrayList<>();
			
			String line;
			LGPIndividual4DJSS rule = null;
			String ruleString = "";
			Fitness fitness = null;
			
			GPTree tree = null;
			
			int generations = 0;
			
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				while (!(line = br.readLine()).equals("Best Individual of Run:")) {
					if (line.startsWith("Generation")) {
						
						rule = new LGPIndividual4DJSS();
						rule.resetIndividual(numRegs, maxIterations);
						ruleString = "";
						
						br.readLine(); //"Best Individual"
						int i = 0;
						for(;i<=subpopulation - 1;i++){
							br.readLine();//subpopulation
							br.readLine();//Evaluated
							br.readLine();//Fitness
							readLGPindStringFromLine(br);
						}	
						
						br.readLine();//supopulation
						br.readLine();//Evaluated
						line = br.readLine();//Fitness
						fitness = readFitnessFromLine(line, false);  //false: is not MultiObjective, multiobjective is not support yet
						
						ruleString = readLGPindStringFromLine(br);
						
//						String expression = br.readLine();
//						
//						while(!expression.startsWith("#")){
//							
//							ruleString += expression + "\n";
//							
//							if(expression.startsWith("//")){
//								//expression = br.readLine();
//								//continue;
//								expression = expression.substring(2);
//							}
//							
//							//remove the "Ins index"
//							int nextWhiteSpaceIdx = expression.indexOf('\t');
//				            expression = expression.substring(nextWhiteSpaceIdx + 1,
//				                    expression.length());
//				            expression.trim();
//							
//							//expression = LispSimplifier.simplifyExpression(expression);
//							tree = LispParser.parseJobShopRule(expression);
//							rule.addTree(rule.getTreesLength(), tree);
//							
//							expression = br.readLine();
//						}
//						
//						ruleString += "#\n";
						
						expressions.add(ruleString);
						
						//read the rest of individual for other tasks
						for(i++;i<numtests;i++){
							br.readLine();//subpopulation
							br.readLine();//Evaluated
							br.readLine();//Fitness
							readLGPindStringFromLine(br);
						}
					}
				
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return expressions;
		}
}
