package zhixing.djss.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ec.gp.GPTree;
import yimei.util.lisp.LispParser;
import yimei.util.lisp.LispSimplifier;
import zhixing.djss.individual.LGPIndividual4DJSS;
import zhixing.djss.ruleanalysis.ResultFileReader4LGP;

public class Parser {
	public static LGPIndividual4DJSS parseJobShopLGPRule(String expression, int numRegs, int maxIterations) {
        GPTree tree = null;
        String line;
		LGPIndividual4DJSS rule = new LGPIndividual4DJSS();
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

	public static void main(String[] args) {

        String path = "D:\\xxxxx\\";
        String algo = "LGP_baseline_8reg";
        String scenario = "mean-weighted-tardiness-0.95-1.5";


        String sourcePath = path + algo + "\\" + scenario + "\\";

        int numRuns = 50;
        
        int numRegs = 8;
        
        int maxIterations = 100;
        
        List<Integer> outputRegs = new ArrayList<>();
        outputRegs.add(0);

        for (int run =  0; run < numRuns; run++) {
            File sourceFile = new File(sourcePath + "job." + run + ".out.stat");
            File outFile = new File(sourcePath + "job." + run + ".bestrule.dot");

            List<String> expressions =
                    ResultFileReader4LGP.readLispExpressionFromFile4LGP(sourceFile,
                            numRegs, maxIterations, false);

            String bestExpression = expressions.get(expressions.size()-1);
            LGPIndividual4DJSS rule = Parser.parseJobShopLGPRule(bestExpression, numRegs, maxIterations);
            String bestGraphVizRule = rule.makeGraphvizRule(outputRegs);

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outFile.getAbsoluteFile()));
                writer.write(bestGraphVizRule);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        String expression = " (* (+ (max (* (* (/ (+ WINQ PT) (* NOR WIQ)) (max SL (/ W NINQ))) (min WIQ (max SL (min PT t)))) WIQ) (max (* (* (/ (/ W NINQ) (* NOR WIQ)) (max SL (/ W NINQ))) (min WIQ (max SL (/ W NINQ)))) WIQ)) (* (min PT t) (/ WIQ W)))";
//        expression = LispSimplifier.simplifyExpression(expression);
//        GPTree tree = LispParser.parseJobShopRule(expression);
//        System.out.println(tree.child.makeGraphvizTree());
    }
}
