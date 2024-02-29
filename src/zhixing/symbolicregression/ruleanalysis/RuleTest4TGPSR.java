package zhixing.symbolicregression.ruleanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ec.Individual;
import ec.gp.GPNode;
import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.rule.evolved.GPRule;
import yimei.jss.ruleanalysis.UniqueTerminalsGatherer;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.symbolicregression.individual.CpxGPInterface4SR;
//import zhixing.jss.cpxInd.individual.CpxGPIndividual4DJSS;
//import zhixing.jss.cpxInd.individual.LGPIndividual;
import zhixing.symbolicregression.optimization.GPSymbolicRegression;

public class RuleTest4TGPSR {
	protected boolean isMultiObj;
	protected String trainPath;
	protected String dataPath;
//	protected String dataName;
//    protected RuleType ruleType;
    protected int numRuns;
    protected String dataName;
    protected List<String> objectives; // The objectives to test.
    private final int maxgenerations = 2000;
    
    public RuleTest4TGPSR(String trainPath, String dataPath, String dataName, int numRuns, boolean isMO) {
		this.trainPath = trainPath;
		 this.dataPath = dataPath;
//        this.ruleType = ruleType;
        this.numRuns = numRuns;
        this.dataName = dataName;
        this.objectives = new ArrayList<>();
		isMultiObj = isMO;
	}
    
    public String getDataPath() {
        return dataPath;
    }

//    public RuleType getRuleType() {
//        return ruleType;
//    }

    public int getNumRuns() {
        return numRuns;
    }


    public List<String> getObjectives() {
        return objectives;
    }

    public void setObjectives(List<String> objectives) {
		this.objectives = objectives;
	}

	public void addObjective(String objective) {
		this.objectives.add(objective);
	}
	
	

	public void writeToCSV() {
		GPSymbolicRegression problem = new GPSymbolicRegression(dataPath, dataName, objectives.get(0), false);

        File targetPath = new File(trainPath + "test");
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }

        File csvFile = new File(targetPath + "/" + dataName + ".csv");

        List<TestResult4CpxGPSR> testResults = new ArrayList<>();
        
        double allTestFitness [][] = new double [maxgenerations][numRuns];
        
        for (int i = 0; i < numRuns; i++) {
            File sourceFile = new File(trainPath + "job." + i + ".out.stat");

            TestResult4CpxGPSR result = TestResult4CpxGPSR.readFromFile4TGP(sourceFile, isMultiObj);

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

                    if (objectives.size() == 1) {
                        writer.write(i + "," + j + "," +
                        		rule.getTree(0).child.numNodes(GPNode.NODESEARCH_ALL) + "," +
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
                        		rule.getTree(0).child.numNodes(GPNode.NODESEARCH_ALL) + "," +
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

		int numObjectives = Integer.valueOf(args[idx++]);
		
		if(numObjectives > 1) {
			System.err.print("the basic rule analysis in LGP for SR does not support multi-objective\n");
			System.exit(1);
		}
		
		RuleTest4TGPSR ruleTest = new RuleTest4TGPSR(trainPath, dataPath, testSetName, numRuns, numObjectives > 1);
		
		for (int i = 0; i < numObjectives; i++) {
			ruleTest.addObjective(args[idx++]);
		}
		ruleTest.writeToCSV();
		
	}
}
