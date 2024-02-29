package zhixing.djss.algorithm.multitask.MFEA.ruleanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import yimei.jss.jobshop.Objective;
import yimei.jss.ruleanalysis.UniqueTerminalsGatherer;
import zhixing.djss.algorithm.multitask.M2GP.ruleanalysis.RuleTest4LGP_M2GP;
import zhixing.djss.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA4DJSS;
import zhixing.djss.individual.LGPIndividual4DJSS;
import zhixing.djss.jobshop.SchedulingSet4Ind;
import zhixing.djss.ruleanalysis.TestResult4CpxGP;

public class RuleTest4LGP_MFEA extends RuleTest4LGP_M2GP{
	
	protected List<Integer> outputReg4Sim;
	protected List<Integer> outputRegs;
	
	public RuleTest4LGP_MFEA(String trainPath,int numRuns,
            String testScenario, List<String> testSetName, List<Integer> outputregs4Sim,
            List<Objective> objectives, int numReg, int maxIter) {
		super(trainPath,numRuns,
	            testScenario, testSetName,
	            objectives, numReg, maxIter);
		outputReg4Sim = outputregs4Sim;
		outputRegs = null;
	}
	
	public RuleTest4LGP_MFEA(String trainPath,int numRuns,
            String testScenario, List<String> testSetName, List<Integer> outputregs4Sim,
            List<Objective> objectives, int numReg, int maxIter, List<Integer> outputRegs) {
		super(trainPath,numRuns,
	            testScenario, testSetName,
	            objectives, numReg, maxIter);
		outputReg4Sim = outputregs4Sim;
		this.outputRegs = outputRegs;
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
            File sourceFile = new File(trainPath + "job." + i + ".outprogram.stat");

            TestResult4CpxGP result = TestResult4LGP_MFEA.readFromFileMFEA(sourceFile, numRegs, maxIterations, index, numtests, outputRegs);

            //File timeFile = new File(trainPath + "job." + i + ".time.csv");
            //result.setGenerationalTimeStat(ResultFileReader.readTimeFromFile(timeFile));

            long start = System.currentTimeMillis();

//            result.validate(objectives);
            List<Objective> objective_list = new ArrayList<>();
    		objective_list.add(objectives.get(index % objectives.size()));

            for (int j = 0; j < result.getGenerationalRules().size(); j++) {
            	if(j % (int)Math.ceil(result.getGenerationalRules().size()/50.0) == 0 || j == result.getGenerationalRules().size()-1){
            		LGPIndividual_MFEA4DJSS res_ind = ((LGPIndividual_MFEA4DJSS)result.getGenerationalRule(j));
            		res_ind.setOutputRegister(outputReg4Sim);
//                    ((LGPIndividual_MFEA)result.getGenerationalRule(j)).calcFitnessInd4MT(
//                            result.getGenerationalTestFitness(j), null, testSet, objective_list, index);
                    res_ind.setCurrentOutputRegister(index);
                    res_ind.calcFitnessInd(result.getGenerationalTestFitness(j), null, testSet, objective_list);
                    
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
		
		//algorithm-related parameters
		ParameterDatabase parameters = null;
		try
        {
			parameters = new ParameterDatabase(
					new File(new File(args[idx]).getAbsolutePath()),
					args);
        }
		catch(Exception e)
        {
			e.printStackTrace();
			Output.initialError("An exception was generated upon reading the parameter file \"" + args[idx] + "\".\nHere it is:\n" + e); 
        }
		idx++;
       
		String trainPath = args[idx];
        idx ++;
        int numRuns = Integer.valueOf(args[idx]);
        idx ++;
        
        //int numRegs = Integer.valueOf(args[idx++]);
        int numRegs = parameters.getInt(new Parameter("pop.subpop.0.species.ind.numregisters"),null);
        if(numRegs<=0){
        	System.err.print("the number of registers is illegal in RuleTest");
        	System.exit(1);
        }
        
		//int maxIteration = Integer.valueOf(args[idx++]);
        int maxIteration = parameters.getInt(new Parameter("pop.subpop.0.species.ind.max_itertimes"),null);
        if(maxIteration<=0){
        	System.err.print("the times of iteration is illegal in RuleTest");
        	System.exit(1);
        }
        
        int numOutRegs = parameters.getInt(new Parameter("pop.subpop.0.species.ind.num-output-register"),null);
        if(numOutRegs<=0){
        	System.err.print("the number of output registers is illegal in RuleTest");
        	System.exit(1);
        }
        List<Integer> outputRegs = new ArrayList<>();
        for(int r = 0;r<numOutRegs;r++){
        	int or = parameters.getInt(new Parameter("pop.subpop.0.species.ind.output-register."+r),null);
        	if(or<0 || or>numRegs){
        		System.err.print("the output register index is illegal in RuleTest");
            	System.exit(1);
        	}
        	outputRegs.add(or);
        	
        }
        
		
        String testScenario = args[idx];
        idx ++;
        int numTestSets = Integer.valueOf(args[idx]);
        idx ++;
        List<String> testSetNames = new ArrayList<String>();
        List<Integer> outputRegs4Sim = new ArrayList<>();
        for(int i = 0; i < numTestSets; i++){
        	testSetNames.add(args[idx]);
        	idx ++;
        	outputRegs4Sim.add(Integer.valueOf(args[idx]));
        	idx++;
        }

		int numObjectives = Integer.valueOf(args[idx++]);
		
		//RuleTest4LGP_MFEA ruleTest = new RuleTest4LGP_MFEA(trainPath, numRuns, testScenario, testSetNames, outputRegs4Sim, new ArrayList<>(), numRegs, maxIteration);
		RuleTest4LGP_MFEA ruleTest = new RuleTest4LGP_MFEA(trainPath, numRuns, testScenario, testSetNames, outputRegs4Sim, new ArrayList<>(), numRegs, maxIteration, outputRegs);
		
		for (int i = 0; i < numObjectives; i++) {
			ruleTest.addObjective(args[idx++]);
		}
		
		for(int i = 0; i < numTestSets; i++){
			ruleTest.writeToCSV(i, numTestSets);
		}
		
	}
}
