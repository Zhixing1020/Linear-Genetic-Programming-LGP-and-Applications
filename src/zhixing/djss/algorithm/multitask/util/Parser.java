package zhixing.djss.algorithm.multitask.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ec.gp.GPTree;
import yimei.jss.ruleanalysis.ResultFileReader;
import yimei.jss.ruleanalysis.RuleType;
import yimei.util.lisp.LispParser;
import yimei.util.lisp.LispSimplifier;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.djss.algorithm.multitask.M2GP.ruleanalysis.ResultFileReader4LGP_M2GP;
import zhixing.djss.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA4DJSS;
import zhixing.djss.algorithm.multitask.MFEA.ruleanalysis.ResultFileReader4LGP_MFEA;
import zhixing.djss.individual.LGPIndividual4DJSS;
import zhixing.djss.ruleanalysis.ResultFileReader4LGP;

public class Parser {
	public static LGPIndividual parseJobShopLGPRule(String expression, int numRegs, int maxIterations) {
        GPTree tree = null;
        String line;
		LGPIndividual rule = new LGPIndividual4DJSS();
		rule.resetIndividual(numRegs, maxIterations);

        expression = expression.trim();
        String split[] = expression.split("\n");
        
        //read the LGP rule based on the given string
        int i = 0;
        String instruction = split[i++];
		
		while(!instruction.startsWith("#")){
			if(instruction.startsWith("//")){
				//expression = br.readLine();
				//continue;
				instruction = instruction.substring(2);
			}
			
			//remove the "Ins index"
			int nextWhiteSpaceIdx = instruction.indexOf('\t');
            instruction = instruction.substring(nextWhiteSpaceIdx + 1,
                    instruction.length());
            instruction.trim();
			
			//expression = LispSimplifier.simplifyExpression(expression);
			tree = LispParser.parseJobShopRule(instruction);
			rule.addTree(rule.getTreesLength(), tree);
			
			instruction = split[i++];
		}

        return rule;
    }
	
	public static LGPIndividual parseJobShopLGPRule_MFEA(String expression, int numRegs, int maxIterations, List<Integer> outputRegs) {
        GPTree tree = null;
        String line;
        LGPIndividual_MFEA4DJSS rule = new LGPIndividual_MFEA4DJSS();
		if(outputRegs == null ) rule.resetIndividual(numRegs, maxIterations);
		else rule.resetIndividual(numRegs, maxIterations, outputRegs);

        expression = expression.trim();
        String split[] = expression.split("\n");
        
        //read the LGP rule based on the given string
        int i = 0;
        String instruction = split[i++];
		
		while(!instruction.startsWith("#")){
			if(instruction.startsWith("//")){
				//expression = br.readLine();
				//continue;
				instruction = instruction.substring(2);
			}
			
			//remove the "Ins index"
			int nextWhiteSpaceIdx = instruction.indexOf('\t');
            instruction = instruction.substring(nextWhiteSpaceIdx + 1,
                    instruction.length());
            instruction.trim();
			
			//expression = LispSimplifier.simplifyExpression(expression);
			tree = LispParser.parseJobShopRule(instruction);
			rule.addTree(rule.getTreesLength(), tree);
			
			instruction = split[i++];
		}

        return rule;
    }

	public static void main(String[] args) {

        String path = "D:\\����thinking\\����\\JobShopScheduling\\multitask\\baseline\\after2022CEC\\50runs\\";
        String algo = "MPMO_F43_50runs";
        String scenario = "heteWMean";


        String sourcePath = path + algo + "\\" + scenario + "\\";

        int numRuns = 50;
        
        int numRegs = 8;
        
        int maxIterations = 100;
        
        int multitask = 2;
        
        List<Integer> outputRegs = new ArrayList<>();
        outputRegs.add(0);
        outputRegs.add(1);
        //outputRegs.add(2);

        for(int subtask=0;subtask<multitask;subtask++){
        	for (int run =  0; run < numRuns; run++) {
                File sourceFile = null;
                if(algo.startsWith("M2GP")){
                	sourceFile = new File(sourcePath + "job." + run + ".out.stat");
                }
                else if(algo.startsWith("MFEA") || algo.startsWith("Onepop") 
                		|| algo.startsWith("OP") || algo.startsWith("MP")){
                	sourceFile = new File(sourcePath + "job." + run + ".outprogram.stat");
                }
                File outFile = new File(sourcePath + "job." + run + "." + subtask + ".bestrule.dot");

                List<String> expressions = null;
                String bestExpression = "";
                LGPIndividual rule = new LGPIndividual4DJSS();
                if(algo.startsWith("M2GP")){
                	expressions = ResultFileReader4LGP_M2GP.readLispExpressionFromFile4LGP(sourceFile, numRegs, maxIterations, subtask, multitask);
                	bestExpression = expressions.get(expressions.size()-1);
                	rule = Parser.parseJobShopLGPRule(bestExpression, numRegs, maxIterations);
                }
                	
                else if(algo.startsWith("MFEA") || algo.startsWith("Onepop") 
                		|| algo.startsWith("OP") || algo.startsWith("MP")){
                	expressions = ResultFileReader4LGP_MFEA.readLispExpressionFromFile4LGP(sourceFile, numRegs, maxIterations, subtask, outputRegs, multitask);
                	bestExpression = expressions.get(expressions.size()-1);
                	rule = Parser.parseJobShopLGPRule_MFEA(bestExpression, numRegs, maxIterations, outputRegs);
                	
                }
                else{
                	System.out.print("unknow algorithm " + algo + "\n");
                	System.exit(1);
                }
                	
                String bestGraphVizRule = rule.makeGraphvizRule(outputRegs);

                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(outFile.getAbsoluteFile()));
                    writer.write(bestGraphVizRule);
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        

//        String expression = " (* (+ (max (* (* (/ (+ WINQ PT) (* NOR WIQ)) (max SL (/ W NINQ))) (min WIQ (max SL (min PT t)))) WIQ) (max (* (* (/ (/ W NINQ) (* NOR WIQ)) (max SL (/ W NINQ))) (min WIQ (max SL (/ W NINQ)))) WIQ)) (* (min PT t) (/ WIQ W)))";
//        expression = LispSimplifier.simplifyExpression(expression);
//        GPTree tree = LispParser.parseJobShopRule(expression);
//        System.out.println(tree.child.makeGraphvizTree());
    }
}
