package zhixing.djss.algorithm.multitask.MFEA.ruleanalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ec.Fitness;
import ec.gp.GPTree;
import yimei.util.lisp.LispParser;
import zhixing.djss.algorithm.multitask.M2GP.ruleanalysis.ResultFileReader4LGP_M2GP;
import zhixing.djss.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA4DJSS;
import zhixing.djss.individual.LGPIndividual4DJSS;
import zhixing.djss.ruleanalysis.TestResult4CpxGP;

public class ResultFileReader4LGP_MFEA extends ResultFileReader4LGP_M2GP{
	public static TestResult4CpxGP readTestResultFromFile(File file,
            int numRegs,
            int maxIterations,
            int taskind,
            int numtests,
            List<Integer> outputRegs){
		TestResult4CpxGP result = new TestResult4CpxGP();
		LGPIndividual_MFEA4DJSS rule = null;
		String line;
		Fitness fitness = null;
		
		GPTree tree = null;
		
		int generations = 0;
		
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			while (!(line = br.readLine()).equals("Best Individual of Run:")) {
				if (line.startsWith("Generation")) {
					br.readLine(); //"Best Individual"
					br.readLine();//subpopulation
					int i = 0;
					for(;i<=taskind - 1;i++){
						br.readLine();//subtask
						br.readLine();//Evaluated
						br.readLine();//Fitness
						readLGPindFromLine(br,numRegs,maxIterations, outputRegs);
					}	
					br.readLine();//subtask
					br.readLine();//Evaluated
					line = br.readLine();//Fitness
					fitness = readFitnessFromLine(line, false);  //false: is not MultiObjective, multiobjective is not support yet
					
					rule = (LGPIndividual_MFEA4DJSS)readLGPindFromLine(br, numRegs, maxIterations,outputRegs);
					
					result.addGenerationalRule(rule);
					result.addGenerationalTrainFitness(fitness);
					result.addGenerationalValidationFitnesses((Fitness)fitness.clone());
					result.addGenerationalTestFitnesses((Fitness)fitness.clone());
					
					//read the rest of sub-tasks in this generation
					for(i++; i<numtests;i++){
						br.readLine();//subtask
						br.readLine();//Evaluated
						br.readLine();//Fitness
						readLGPindFromLine(br,numRegs,maxIterations, outputRegs);
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
	
	
	
	protected static LGPIndividual_MFEA4DJSS readLGPindFromLine(BufferedReader br, int numRegs, int maxIterations, List<Integer> outputRegs){
		LGPIndividual_MFEA4DJSS rule = new LGPIndividual_MFEA4DJSS();
		if(outputRegs == null ) rule.resetIndividual(numRegs, maxIterations);
		else rule.resetIndividual(numRegs, maxIterations, outputRegs);
		
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
	
	public static List<String> readLispExpressionFromFile4LGP(File file,
			 int numRegs,
           int maxIterations,
           int taskind,
           List<Integer> outputRegs,
           int numtests) {
			List<String> expressions = new ArrayList<>();
			
			String line;
			LGPIndividual_MFEA4DJSS rule = null;
			String ruleString = "";
			Fitness fitness = null;
			
			GPTree tree = null;
			
			int generations = 0;
			
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				while (!(line = br.readLine()).equals("Best Individual of Run:")) {
					if (line.startsWith("Generation")) {
						
						rule = new LGPIndividual_MFEA4DJSS();
						if(outputRegs == null) rule.resetIndividual(numRegs, maxIterations);
						else rule.resetIndividual(numRegs, maxIterations, outputRegs);
						ruleString = "";
						
						br.readLine(); //"Best Individual"
						br.readLine();//subpopulation
						int i = 0;
						for(;i<=taskind - 1;i++){
							br.readLine();//subtask
							br.readLine();//Evaluated
							br.readLine();//Fitness
							readLGPindStringFromLine(br);
						}	
						br.readLine();//subtask
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
							br.readLine();//subtask
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
