package zhixing.symbolicregression.algorithm.multiform.ruleanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ec.Individual;
import ec.gp.GPNode;
import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.ruleanalysis.UniqueTerminalsGatherer;
import zhixing.cpxInd.algorithm.Multiform.individual.LGPIndividual4MForm;
import zhixing.cpxInd.algorithm.Multiform.individual.TGPIndividual4MForm;
import zhixing.cpxInd.individual.CpxGPIndividual;
//import zhixing.djss.individual.LGPIndividual4DJSS;
import zhixing.symbolicregression.optimization.GPSymbolicRegression;
import zhixing.symbolicregression.ruleanalysis.RuleTest4LGPSR;
import zhixing.symbolicregression.ruleanalysis.TestResult4CpxGPSR;

public class RuleTest4MRGP4SR extends RuleTest4LGPSR {

	public RuleTest4MRGP4SR(String trainPath, String dataPath, String dataName, int numRuns, int numReg, int maxIter,
			boolean isMO) {
		super(trainPath, dataPath, dataName, numRuns, numReg, maxIter, isMO);
	}
	
	
	public void writeToCSV() {
		GPSymbolicRegression problem = new GPSymbolicRegression(dataPath, dataName, objectives.get(0), false);

        File targetPath = new File(trainPath + "test");
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }

        File csvFile = new File(targetPath + "/" + dataName + ".csv");

        List<TestResult4CpxGPSRMF> testResults = new ArrayList<>();
        
        double allTestFitness [][] = new double [maxgenerations][numRuns];
        
        for (int i = 0; i < numRuns; i++) {
            File sourceFile = new File(trainPath + "job." + i + ".out.stat");

            TestResult4CpxGPSRMF result = TestResult4CpxGPSRMF.readFromFile(sourceFile, numRegs, maxIterations, isMultiObj);

            //File timeFile = new File(trainPath + "job." + i + ".time.csv");
            //result.setGenerationalTimeStat(ResultFileReader.readTimeFromFile(timeFile));

            long start = System.currentTimeMillis();

//            result.validate(objectives);
            
            
            for (int j = 0; j < result.getGenerationalRules().size(); j++) {

            	if(j % (int)Math.ceil(result.getGenerationalRules().size()/50.0) == 0 || j == result.getGenerationalRules().size()-1 ){
            		
            		problem.simpleevaluate((Individual) result.getGenerationalRule(j));
            		
            		double[] fitnesses = new double[objectives.size()];
            		for(int f = 0;f<objectives.size();f++){
            			fitnesses[f] = ((CpxGPIndividual) result.getGenerationalRule(j)).fitness.fitness();
            		}
            		
            		((MultiObjectiveFitness)result.getGenerationalTestFitness(j)).setObjectives(null, fitnesses);
            	}
            	else{
            		double[] fitnesses = new double[objectives.size()];
            		for(int f = 0;f<objectives.size();f++){
            			fitnesses[f] = result.getGenerationalTestFitness(j-1).fitness();
            		}
            		((MultiObjectiveFitness)result.getGenerationalTestFitness(j)).setObjectives(null, fitnesses);
            	}

                System.out.println("Generation " + j + ": test fitness = " +
                        result.getGenerationalTestFitness(j).fitness());
                
                if(j<maxgenerations) allTestFitness[j][i] = result.getGenerationalTestFitness(j).fitness();
                else {
                	System.out.println("the evolution generation is larger than " + maxgenerations);
                	System.exit(1);
                }
            }

            long finish = System.currentTimeMillis();
            long duration = finish - start;
            System.out.println("Duration = " + duration + " ms.");

            testResults.add(result);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile.getAbsoluteFile()));
            writer.write("Run,Generation,Size,UniqueTerminals,Obj,TrainFitness,TestFitness,Time");
            writer.newLine();
            for (int i = 0; i < numRuns; i++) {
                TestResult4CpxGPSR result = testResults.get(i);

                for (int j = 0; j < result.getGenerationalRules().size(); j++) {
                	CpxGPIndividual rule = (CpxGPIndividual) result.getGenerationalRule(j);

                    MultiObjectiveFitness trainFit =
                            (MultiObjectiveFitness)result.getGenerationalTrainFitness(j);
                    MultiObjectiveFitness testFit =
                            (MultiObjectiveFitness)result.getGenerationalTestFitness(j);

                    UniqueTerminalsGatherer gatherer = new UniqueTerminalsGatherer();
                    int numUniqueTerminals = 0;//rule.getGPTree().child.numNodes(gatherer);

                    double programsize = 0;
                    int indicator = 0; //indicating which kind of GP produce the best program
                    if(rule instanceof LGPIndividual4MForm) {
                    	programsize = rule.getTreesLength();
                    	indicator = 0;
                    }
                    else if(rule instanceof TGPIndividual4MForm){
                    	programsize = rule.getTree(0).child.numNodes(GPNode.NODESEARCH_ALL);
                    	indicator = 1;
                    }
                    if (objectives.size() == 1) {
                        writer.write(i + "," + j + "," +
                                programsize + "," +
                                numUniqueTerminals + ",0," +
                                trainFit.fitness() + "," +
                                testFit.fitness() + "," +
                                //result.getGenerationalTime(j)
                                0
                                + "," + indicator
                                );
                        writer.newLine();
                    }
                    else {
                        writer.write(i + "," + j + "," +
                                programsize + "," +
                                numUniqueTerminals + ",");

                        for (int k = 0; k < objectives.size(); k++) {
                            writer.write(k + "," +
                                    trainFit.getObjective(k) + "," +
                                    testFit.getObjective(k) + ",");
                        }

                        //writer.write("" + result.getGenerationalTime(j));
                        writer.write("" + 0);
                        writer.newLine();
                    }
                }
            }
            writer.close();
            
            csvFile = new File(targetPath + "/" + dataName + "-allTestFitness.csv");
            writer = new BufferedWriter(new FileWriter(csvFile.getAbsoluteFile()));
            String tmp = "generation:";
            for(int i = 0;i<numRuns;i++){
            	tmp = tmp + "," + i;
            }
            writer.write(tmp);
            writer.newLine();
            int gen = testResults.get(0).getGenerationalRules().size();
            for(int j = 0; j<gen; j++){
            	String tmp2=""+j;
            	for(int i = 0; i<numRuns; i++){
            		tmp2 += "," + allTestFitness[j][i];
            	}
            	writer.write(tmp2);
            	writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
	public static void main(String[] args) {
		int idx = 0;
       
		String trainPath = args[idx];
        idx ++;
        String dataPath = args[idx];
        idx ++;
        String testSetName = args[idx];
        idx ++;
        int numRuns = Integer.valueOf(args[idx]);
        idx ++;
        		
		int numRegs = Integer.valueOf(args[idx++]);
		int maxIteration = Integer.valueOf(args[idx++]);

		int numObjectives = Integer.valueOf(args[idx++]);
		
		if(numObjectives > 1) {
			System.err.print("the basic rule analysis in LGP for SR does not support multi-objective\n");
			System.exit(1);
		}
		
		RuleTest4MRGP4SR ruleTest = new RuleTest4MRGP4SR(trainPath, dataPath, testSetName, numRuns, numRegs, maxIteration, numObjectives > 1);
		
		for (int i = 0; i < numObjectives; i++) {
			ruleTest.addObjective(args[idx++]);
		}
		ruleTest.writeToCSV();
		
	}
}
