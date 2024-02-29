package zhixing.djss.algorithm.multitask.M2GP.ruleanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.ruleanalysis.RuleType;
import yimei.jss.ruleanalysis.UniqueTerminalsGatherer;
import zhixing.djss.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA4DJSS;
import zhixing.djss.individual.LGPIndividual4DJSS;
import zhixing.djss.jobshop.SchedulingSet4Ind;
import zhixing.djss.ruleanalysis.TestResult4CpxGP;

public class RuleTest4LGP_M2GP {
	
	public static final long simSeed = 968356;

	protected String trainPath;
    protected RuleType ruleType;
    protected int numRuns;
    protected String testScenario;
    
	protected List<String> testSetName;
    protected List<Objective> objectives; // The objectives to test.
	
	protected int numRegs;
	protected int maxIterations;
    
	public RuleTest4LGP_M2GP(String trainPath,int numRuns,
            String testScenario, List<String> testSetName,
            List<Objective> objectives, int numReg, int maxIter) {
		this.trainPath = trainPath;
        this.ruleType = ruleType;
        this.numRuns = numRuns;
        this.testScenario = testScenario;
        this.testSetName = testSetName;
        this.objectives = objectives;
        
		this.numRegs = numReg;
		this.maxIterations = maxIter;
	}
	
	public String getTrainPath() {
        return trainPath;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public int getNumRuns() {
        return numRuns;
    }

    public String getTestScenario() {
        return testScenario;
    }

    public List<Objective> getObjectives() {
        return objectives;
    }

    public void setObjectives(List<Objective> objectives) {
		this.objectives = objectives;
	}

	public void addObjective(Objective objective) {
		this.objectives.add(objective);
	}

	public void addObjective(String objective) {
		addObjective(Objective.get(objective));
	}
	
	public SchedulingSet4Ind generateTestSet4Ind() {
        return generateTestSet4Ind(0);
    }
	
	public SchedulingSet4Ind generateTestSet4Ind(int ind){
		List<Objective> objective_list = new ArrayList<>();
		objective_list.add(objectives.get(ind % objectives.size()));
		
		return SchedulingSet4Ind.generateSet(simSeed, testScenario,
                testSetName.get(ind), objective_list, 50);
	}
	
	public void writeToCSV(int index, int numtests) {
        SchedulingSet4Ind testSet = generateTestSet4Ind(index);

        File targetPath = new File(trainPath + "test");
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }

        File csvFile = new File(targetPath + "/" + testSetName.get(index) + "-" + index + ".csv");

        List<TestResult4CpxGP> testResults = new ArrayList<>();
        
        double allTestFitness [][] = new double [1000][numRuns];
        
        for (int i = 0; i < numRuns; i++) {
            File sourceFile = new File(trainPath + "job." + i + ".out.stat");

            TestResult4CpxGP result = TestResult4LGP_M2GP.readFromFileM2GP(sourceFile, numRegs, maxIterations, index, numtests);

            //File timeFile = new File(trainPath + "job." + i + ".time.csv");
            //result.setGenerationalTimeStat(ResultFileReader.readTimeFromFile(timeFile));

            long start = System.currentTimeMillis();

//            result.validate(objectives);
            List<Objective> objective_list = new ArrayList<>();
    		objective_list.add(objectives.get(index % objectives.size()));

            for (int j = 0; j < result.getGenerationalRules().size(); j++) {
            	if(j % (int)Math.ceil(result.getGenerationalRules().size()/50.0) == 0 || j == result.getGenerationalRules().size()-1){
            		result.getGenerationalRule(j).calcFitnessInd(
                            result.getGenerationalTestFitness(j), null, testSet, objective_list);
            	}
            	else{
            		double[] fitnesses = new double[1];
            		fitnesses[0] = result.getGenerationalTestFitness(j-1).fitness();
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
                    LGPIndividual4DJSS rule = (LGPIndividual4DJSS) result.getGenerationalRule(j);

                    MultiObjectiveFitness trainFit =
                            (MultiObjectiveFitness)result.getGenerationalTrainFitness(j);
                    MultiObjectiveFitness testFit =
                            (MultiObjectiveFitness)result.getGenerationalTestFitness(j);

                    UniqueTerminalsGatherer gatherer = new UniqueTerminalsGatherer();
                    int numUniqueTerminals = 0;//rule.getGPTree().child.numNodes(gatherer);
                    
                    writer.write(i + "," + j + "," +
                            rule.getTreesLength() + "," +
                            numUniqueTerminals + ",0," +
                            trainFit.fitness() + "," +
                            testFit.fitness() + "," +
                            //result.getGenerationalTime(j)
                            0
                            );
                    writer.newLine();

//                    if (objectives.size() == 1) {
//                        
//                    }
//                    else {
//                        writer.write(i + "," + j + "," +
//                                rule.getTreesLength() + "," +
//                                numUniqueTerminals + ",");
//
//                        for (int k = 0; k < objectives.size(); k++) {
//                            writer.write(k + "," +
//                                    trainFit.getObjective(k) + "," +
//                                    testFit.getObjective(k) + ",");
//                        }
//
//                        //writer.write("" + result.getGenerationalTime(j));
//                        writer.write("" + 0);
//                        writer.newLine();
//                    }
                }
            }
            writer.close();
            
            csvFile = new File(targetPath + "/" + testSetName.get(index) + "-" + index + "-allTestFitness.csv");
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
        
        int numRegs = Integer.valueOf(args[idx++]);
        
		int maxIteration = Integer.valueOf(args[idx++]);
		
        String testScenario = args[idx];
        idx ++;
        int numTestSets = Integer.valueOf(args[idx]);
        idx ++;
        List<String> testSetNames = new ArrayList<String>();
        for(int i = 0; i < numTestSets; i++){
        	testSetNames.add(args[idx]);
        	idx ++;
        }

		int numObjectives = Integer.valueOf(args[idx++]);
		
		RuleTest4LGP_M2GP ruleTest = new RuleTest4LGP_M2GP(trainPath, numRuns, testScenario, testSetNames, new ArrayList<>(), numRegs, maxIteration);
		
		for (int i = 0; i < numObjectives; i++) {
			ruleTest.addObjective(args[idx++]);
		}
		
		for(int i = 0; i < numTestSets; i++){
			ruleTest.writeToCSV(i, numTestSets);
		}
		
	}
}
