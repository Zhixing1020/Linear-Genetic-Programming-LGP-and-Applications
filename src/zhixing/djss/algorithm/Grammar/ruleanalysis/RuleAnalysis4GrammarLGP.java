package zhixing.djss.algorithm.Grammar.ruleanalysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.gp.terminal.AttributeGPNode;
import yimei.jss.gp.terminal.JobShopAttribute;
import yimei.jss.gp.terminal.TerminalERC;
import yimei.jss.ruleanalysis.UniqueTerminalsGatherer;
import zhixing.cpxInd.algorithm.Grammar.individual.GPTreeStructGrammar;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.Branching;
import zhixing.cpxInd.individual.primitive.FlowOperator;
import zhixing.djss.jobshop.SchedulingSet4Ind;
import zhixing.djss.ruleanalysis.RuleTest4LGP;
import zhixing.djss.ruleanalysis.TestResult4CpxGP;

public class RuleAnalysis4GrammarLGP extends RuleTest4LGP{

	public RuleAnalysis4GrammarLGP(String trainPath, int numRuns, int numReg,
			int maxIter, boolean isMO) {
		super(trainPath, numRuns, null, null, numReg, maxIter, isMO);
		// TODO Auto-generated constructor stub
	}
	
	public void writeToTXT(String scenario) {

        File targetPath = new File(trainPath + scenario + "/test");
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }

        File txtFile = new File(targetPath + "/IFfeatures.txt");

        List<TestResult4CpxGP> analysisResults = new ArrayList<>();
        
        for (int i = 0; i < numRuns; i++) {
            File sourceFile = new File(trainPath + scenario + "/" + "job." + i + ".out.stat");

            TestResult4CpxGP result = TestResult4CpxGP.readFromFile(sourceFile, numRegs, maxIterations, isMultiObj);

            //File timeFile = new File(trainPath + "job." + i + ".time.csv");
            //result.setGenerationalTimeStat(ResultFileReader.readTimeFromFile(timeFile));

            long start = System.currentTimeMillis();

//            result.validate(objectives);

            analysisResults.add(result);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile.getAbsoluteFile()));
            //How many IF,  which feature follows IF
            String head = "Run,";
            
            int jj = 0;
            ArrayList<String> IFfeature = new ArrayList<>();
            for (JobShopAttribute a : JobShopAttribute.relativeAttributes4grammar()) {
            	if(jj>12) {
            		IFfeature.add(a.getName());
            		head += a.getName()+",";
            	}
            	jj++;
    			
    		}
            writer.write(head);
            writer.newLine();
            for (int i = 0; i < numRuns; i++) {
                TestResult4CpxGP result = analysisResults.get(i);
                
                int j = result.getGenerationalRules().size() - 1;
                
                LGPIndividual rule = (LGPIndividual) result.getGenerationalRule(j);
                
                ArrayList<Double> features = new ArrayList<>();
                
                for(int s = 0; s<IFfeature.size();s++) {
                	features.add(0.0);
                }
                
                for(GPTreeStruct instr : rule.getTreeStructs()) {
                	if(instr.status && instr.child.children[0] instanceof Branching) {
                		for(int c = 0;c<2;c++) {
                			if( IFfeature.contains( (instr.child.children[0].children[c]).toString() )) {
                				int index = IFfeature.indexOf( ( instr.child.children[0].children[c]).toString());
                				
                				features.set(index, features.get(index)+1);
                			}
                		}
                	}
                }
                
                String res_singlerun = "" + i + ",";
                for(Double f : features) {
                	res_singlerun += f + ",";
                }
                
                writer.write(res_singlerun);
                
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
		
		RuleAnalysis4GrammarLGP ruleTest = new RuleAnalysis4GrammarLGP(trainPath, numRuns, numRegs, maxIteration, numObjectives > 1);
		
		String[] scenarios = new String [] {
				"max-flowtime-0.85-1.5",
				"max-flowtime-0.95-1.5",
				"mean-flowtime-0.85-1.5",
				"mean-flowtime-0.95-1.5",
				"mean-weighted-flowtime-0.85-1.5",
				"mean-weighted-flowtime-0.95-1.5",
				
				"max-tardiness-0.85-1.5",
				"max-tardiness-0.95-1.5",
				"mean-tardiness-0.85-1.5",
				"mean-tardiness-0.95-1.5",
				"mean-weighted-tardiness-0.85-1.5",
				"mean-weighted-tardiness-0.95-1.5",
		};
		
		
		for(String s : scenarios) {
			ruleTest.writeToTXT(s);
		}
		
		
//		for (int i = 0; i < numObjectives; i++) {
//			ruleTest.addObjective(args[idx++]);
//		}
//		ruleTest.writeToCSV();
		
	}
}
