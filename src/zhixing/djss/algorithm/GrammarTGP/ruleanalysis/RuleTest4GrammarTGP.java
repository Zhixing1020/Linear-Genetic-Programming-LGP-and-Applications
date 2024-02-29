package zhixing.djss.algorithm.GrammarTGP.ruleanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ec.gp.GPNode;
import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.ruleanalysis.UniqueTerminalsGatherer;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.djss.jobshop.SchedulingSet4Ind;
import zhixing.djss.ruleanalysis.RuleTest4LGP;
import zhixing.djss.ruleanalysis.TestResult4CpxGP;

public class RuleTest4GrammarTGP  extends RuleTest4LGP{
	public RuleTest4GrammarTGP(String trainPath,int numRuns,
            String testScenario, String testSetName, int numReg, int maxIter, boolean isMO) {
		// TODO Auto-generated constructor stub
		super(trainPath, numRuns, testScenario, testSetName, numReg, maxIter, isMO);
		numRegs = numReg;
		maxIterations = maxIter;
		isMultiObj = isMO;
	}
	
	public SchedulingSet4Ind generateTestSet4Ind() {
        return SchedulingSet4Ind.generateSet(simSeed, testScenario,
                testSetName, objectives, 50);
    }
	
	public SchedulingSet4Ind generateValidSet4Ind() {
        return SchedulingSet4Ind.generateSet(validsimSeed, testScenario,
                testSetName, objectives, 10);
    }
	
	public void writeToCSV() {
        SchedulingSet4Ind testSet = generateTestSet4Ind();
        SchedulingSet4Ind validSet = generateValidSet4Ind();

        File targetPath = new File(trainPath + "test");
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }

        File csvFile = new File(targetPath + "/" + testSetName + ".csv");

        List<TestResult4GrammarTGP> testResults = new ArrayList<>();
        
        double allTestFitness [][] = new double [1000][numRuns];
        
        for (int i = 0; i < numRuns; i++) {
            File sourceFile = new File(trainPath + "job." + i + ".out.stat");

            TestResult4GrammarTGP result = TestResult4GrammarTGP.readFromFileGrammarTGP(sourceFile, numRegs, maxIterations, isMultiObj);

            //File timeFile = new File(trainPath + "job." + i + ".time.csv");
            //result.setGenerationalTimeStat(ResultFileReader.readTimeFromFile(timeFile));

            long start = System.currentTimeMillis();

//            result.validate(objectives);
            
            
            for (int j = 0; j < result.getGenerationalRules().size(); j++) {
            	if(j == result.getGenerationalRules().size()-1) {
            		result.getGenerationalRule(j).calcFitnessInd(
                            result.getGenerationalTestFitness(j), null, testSet, objectives);
            	}
            	else if(j % (int)Math.ceil(result.getGenerationalRules().size()/50.0) == 0){
            		result.getGenerationalRule(j).calcFitnessInd(
                            result.getGenerationalTestFitness(j), null, testSet, objectives);
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
                
                if(j<1000) allTestFitness[j][i] = result.getGenerationalTestFitness(j).fitness();
                else {
                	System.out.println("the evolution generation is larger than 1000");
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
                TestResult4CpxGP result = testResults.get(i);

                for (int j = 0; j < result.getGenerationalRules().size(); j++) {
                    CpxGPIndividual rule = (CpxGPIndividual) result.getGenerationalRule(j);

                    MultiObjectiveFitness trainFit =
                            (MultiObjectiveFitness)result.getGenerationalTrainFitness(j);
                    MultiObjectiveFitness testFit =
                            (MultiObjectiveFitness)result.getGenerationalTestFitness(j);

                    UniqueTerminalsGatherer gatherer = new UniqueTerminalsGatherer();
                    int numUniqueTerminals = 0;//rule.getGPTree().child.numNodes(gatherer);

                    double programsize = 0;
                    
                    programsize = rule.getTree(0).child.numNodes(GPNode.NODESEARCH_ALL);
                	
                    if (objectives.size() == 1) {
                        writer.write(i + "," + j + "," +
                                programsize + "," +
                                numUniqueTerminals + ",0," +
                                trainFit.fitness() + "," +
                                testFit.fitness() + "," +
                                //result.getGenerationalTime(j)
                                0
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
            
            csvFile = new File(targetPath + "/" + testSetName + "-allTestFitness.csv");
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
        int numRuns = Integer.valueOf(args[idx]);
        idx ++;
        String testScenario = args[idx];
        idx ++;
        String testSetName = args[idx];
        idx ++;
		
		int numRegs = Integer.valueOf(args[idx++]);
		int maxIteration = Integer.valueOf(args[idx++]);

		int numObjectives = Integer.valueOf(args[idx++]);
		
		RuleTest4GrammarTGP ruleTest = new RuleTest4GrammarTGP(trainPath, numRuns, testScenario, testSetName, numRegs, maxIteration, numObjectives > 1);
		
		for (int i = 0; i < numObjectives; i++) {
			ruleTest.addObjective(args[idx++]);
		}
		ruleTest.writeToCSV();
		
	}
}
