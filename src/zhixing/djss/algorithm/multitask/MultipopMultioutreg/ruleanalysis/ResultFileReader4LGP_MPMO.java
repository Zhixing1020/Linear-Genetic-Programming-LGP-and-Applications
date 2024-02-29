package zhixing.djss.algorithm.multitask.MultipopMultioutreg.ruleanalysis;

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
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA;
//import zhixing.djss.algorithm.multitask.MFEA.individual.LGPIndividual_MPMO4DJSS;
import zhixing.djss.algorithm.multitask.MFEA.ruleanalysis.ResultFileReader4LGP_MFEA;
import zhixing.djss.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO4DJSS;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.ruleanalysis.TestResult4CpxGP;

public class ResultFileReader4LGP_MPMO extends ResultFileReader4LGP_MFEA{
	
	protected static LGPIndividual_MPMO4DJSS readLGPindFromLine_MPMO(BufferedReader br, int numRegs, int maxIterations, List<Integer> outputRegs){
		LGPIndividual_MPMO4DJSS rule = new LGPIndividual_MPMO4DJSS();
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

	public static TestResult4CpxGP readTestResultFromFile(File file,
            int numRegs,
            int maxIterations,
            int taskind,
            int numtests,
            List<Integer> outputRegs){
		TestResult4CpxGP result = new TestResult4CpxGP();
		LGPIndividual_MPMO4DJSS rule = null;
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
						readLGPindFromLine_MPMO(br,numRegs,maxIterations, outputRegs);
					}	
					br.readLine();//subtask
					br.readLine();//Evaluated
					line = br.readLine();//Fitness
					fitness = readFitnessFromLine(line, false, taskind);  //false: is not MultiObjective, multiobjective is not support yet
					
					rule = (LGPIndividual_MPMO4DJSS)readLGPindFromLine_MPMO(br, numRegs, maxIterations,outputRegs);
					
					result.addGenerationalRule((CpxGPInterface4DJSS) rule);
					result.addGenerationalTrainFitness(fitness);
					result.addGenerationalValidationFitnesses((Fitness)fitness.clone());
					result.addGenerationalTestFitnesses((Fitness)fitness.clone());
					
					//read the rest of sub-tasks in this generation
					for(i++; i<numtests;i++){
						br.readLine();//subtask
						br.readLine();//Evaluated
						br.readLine();//Fitness
						readLGPindFromLine_MPMO(br,numRegs,maxIterations, outputRegs);
					}
				}
			
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Set the best rule as the rule in the last generation
		if(rule != null){
			result.setBestRule((CpxGPInterface4DJSS) rule);
			result.setBestTrainingFitness(fitness);
		}

		return result;
	}
	
	protected static Fitness readFitnessFromLine(String line, boolean isMultiobjective, int subpop) {
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
            String[] spaceSegments = line.split("\\s+|\\[|\\]");
            
            String[] fitVec;
            if(spaceSegments.length > 3){
            	fitVec = spaceSegments[2+subpop].split("\\[|\\]");
            }
            else{
            	fitVec = spaceSegments[2].split("\\[|\\]");
            }
            
            double fitness = Double.valueOf(fitVec[0]);
            MultiObjectiveFitness f = new MultiObjectiveFitness();
            f.objectives = new double[1];
            f.objectives[0] = fitness;

            return f;
        }
    }
	
	public static List<String> readLispExpressionFromFile4LGP(File file,
			 int numRegs,
          int maxIterations,
          int taskind,
          List<Integer> outputRegs,
          int numtests) {
			List<String> expressions = new ArrayList<>();
			
			String line;
			LGPIndividual_MPMO4DJSS rule = null;
			String ruleString = "";
			Fitness fitness = null;
			
			GPTree tree = null;
			
			int generations = 0;
			
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				while (!(line = br.readLine()).equals("Best Individual of Run:")) {
					if (line.startsWith("Generation")) {
						
						rule = new LGPIndividual_MPMO4DJSS();
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
