package zhixing.symbolic_classification.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import ec.gp.GPNode;
import ec.gp.GPTree;
import yimei.util.lisp.LispUtil;

import zhixing.cpxInd.individual.primitive.ConstantGPNode;
import zhixing.cpxInd.individual.primitive.IFLargerThan;
import zhixing.cpxInd.individual.primitive.IFLessEqual;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.Temperature;
import zhixing.cpxInd.individual.primitive.WhileLargeLoop;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.symbolic_classification.algorithm.entity.individual.primitive.LDA_EntityNode;
import zhixing.symbolic_classification.individual.LGPIndividual4Class;
import zhixing.symbolic_classification.individual.primitive.InputFeature4Class;
import zhixing.symbolicregression.individual.primitive.Add;
import zhixing.symbolicregression.individual.primitive.Cos;
import zhixing.symbolicregression.individual.primitive.Div;
import zhixing.symbolicregression.individual.primitive.Exp;
import zhixing.symbolicregression.individual.primitive.IF;
import zhixing.symbolicregression.individual.primitive.Ln;
import zhixing.symbolicregression.individual.primitive.Max;
import zhixing.symbolicregression.individual.primitive.Min;
import zhixing.symbolicregression.individual.primitive.Mul;
import zhixing.symbolicregression.individual.primitive.Pow2;
import zhixing.symbolicregression.individual.primitive.ReLu;
import zhixing.symbolicregression.individual.primitive.Sin;
import zhixing.symbolicregression.individual.primitive.Sqrt;
import zhixing.symbolicregression.individual.primitive.Sub;
import zhixing.symbolicregression.individual.primitive.Tanh;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.AvgHub;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.CondLR_EntityNode;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.ExpoRegFunc;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.LR_EntityNode;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.LinearRegFunc_EntityNode;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.MaxHub;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.MinHub;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.PLSR_EntityNode;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.PowRegFunc;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.RadRegFunc;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.Radius_EntityNode;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.SinRegFunc;
//import zhixing.symbreg_multitarget.individual.LGPIndividual4SRMT;
//import zhixing.symbreg_multitarget.individual.primitive.InputFeature4SRMT;
import zhixing.symbreg_multitarget.ruleanalysis.ResultFileReader4LGPSRMT;
import zhixing.symbreg_multitarget.util.LispParser4SRMT;

public class LispParser4Class {

	public static GPTree parseSymRegRule(String expression) {
        GPTree tree = new GPTree();

        expression = expression.trim();

        tree.child = parseNode(expression);

        return tree;
    }
	
	public static LGPIndividual4Class parseClassLGPRule(String expression, int numRegs, int maxIterations) {
        GPTree tree = null;
        String line;
		LGPIndividual4Class rule = new LGPIndividual4Class();
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
			tree = LispParser4SRMT.parseSymRegRule(instruction);
			rule.addTree(rule.getTreesLength(), tree);
			
			instruction = split[i++];
		}

        return rule;
    }
	
	private static GPNode parseNode(String expression) {
		GPNode node = null;

        if (expression.charAt(0) == '(') {
            int nextWhiteSpaceIdx = expression.indexOf(' ');
            String func = expression.substring(1, nextWhiteSpaceIdx);
            String argsString = expression.substring(nextWhiteSpaceIdx + 1,
                    expression.length() - 1);
            List<String> args = LispUtil.splitArguments(argsString);
            
            //==============zhixing, 2021.5.28, for LGP in DJSS
            if(func.startsWith("R")&&func.endsWith("=")){
            	int equalIdx = func.indexOf('=');
            	String indexStr = func.substring(1, equalIdx);
            	int index = Integer.valueOf(indexStr);
            	node = new WriteRegisterGPNode();
            	((WriteRegisterGPNode)node).setIndex(index);
            	node.children = new GPNode[1];
                node.children[0] = parseNode(args.get(0));
                node.children[0].parent = node;
                node.children[0].argposition = 0;
            }
            else if(func.startsWith("IF>#")){
            	int NumIdx = func.indexOf('#');
            	String NumStr = func.substring(NumIdx+1, func.length());
            	int bodylength = Integer.valueOf(NumStr);
            	
            	node = new WriteRegisterGPNode();
            	node.children = new GPNode[1];
            	node.children[0] = new IFLargerThan();
            	((IFLargerThan)node.children[0]).setMaxBodyLength(bodylength);
            	((IFLargerThan)node.children[0]).setBodyLength(bodylength);
                node.children[0].parent = node;
                node.children[0].argposition = 0;
                node.children[0].children = new GPNode[2];
                node.children[0].children[0] = parseNode(args.get(0));
                node.children[0].children[1] = parseNode(args.get(1));
                node.children[0].children[0].parent = node.children[0];
                node.children[0].children[1].parent = node.children[0];
                node.children[0].children[0].argposition = 0;
                node.children[0].children[1].argposition = 1;
            }
            else if(func.startsWith("IF<=#")) {
            	int NumIdx = func.indexOf('#');
            	String NumStr = func.substring(NumIdx+1, func.length());
            	int bodylength = Integer.valueOf(NumStr);
            	
            	node = new WriteRegisterGPNode();
            	node.children = new GPNode[1];
            	node.children[0] = new IFLessEqual();
            	((IFLessEqual)node.children[0]).setMaxBodyLength(bodylength);
            	((IFLessEqual)node.children[0]).setBodyLength(bodylength);
                node.children[0].parent = node;
                node.children[0].argposition = 0;
                node.children[0].children = new GPNode[2];
                node.children[0].children[0] = parseNode(args.get(0));
                node.children[0].children[1] = parseNode(args.get(1));
                node.children[0].children[0].parent = node.children[0];
                node.children[0].children[1].parent = node.children[0];
                node.children[0].children[0].argposition = 0;
                node.children[0].children[1].argposition = 1;
            }
            else if(func.startsWith("WHILE>#")){
            	int NumIdx = func.indexOf('#');
            	String NumStr = func.substring(NumIdx+1, func.length());
            	int bodylength = Integer.valueOf(NumStr);
            	
            	node = new WriteRegisterGPNode();
            	node.children = new GPNode[1];
            	node.children[0] = new WhileLargeLoop();
            	((IFLargerThan)node.children[0]).setMaxBodyLength(bodylength);
            	((IFLargerThan)node.children[0]).setBodyLength(bodylength);
                node.children[0].parent = node;
                node.children[0].argposition = 0;
                node.children[0].children = new GPNode[2];
                node.children[0].children[0] = parseNode(args.get(0));
                node.children[0].children[1] = parseNode(args.get(1));
                node.children[0].children[0].parent = node.children[0];
                node.children[0].children[1].parent = node.children[0];
                node.children[0].children[0].argposition = 0;
                node.children[0].children[1].argposition = 1;
            }
            else if(func.startsWith("LRF_entity")) {
            	node = new LinearRegFunc_EntityNode();
            	int nextLeftBracketIdx = expression.indexOf('[');
            	int nextRightBracketIdx = expression.indexOf(']');
            	String args_str = expression.substring(nextLeftBracketIdx, nextRightBracketIdx+1);
            	((LinearRegFunc_EntityNode)node).setFromString(args_str);
            	int num_child = ((LinearRegFunc_EntityNode)node).getArguments().getMaxLength()-1;
            	node.children = new GPNode[num_child];
            	for(int c = 0; c<num_child; c++) {
            		node.children[c] = parseNode(args.get(c));
            		node.children[c].parent = node;
            		node.children[c].argposition = (byte) c;
            	}
            }
            else if(func.startsWith("Temp")) {
            	node = new Temperature();
            	int nextLeftBracketIdx = expression.indexOf('[');
            	int nextRightBracketIdx = expression.indexOf(']');
            	String args_str = expression.substring(nextLeftBracketIdx, nextRightBracketIdx+1);
            	((Temperature)node).setFromString(args_str);
            	int num_child = node.expectedChildren();
            	node.children = new GPNode[num_child];
            	for(int c = 0; c<num_child; c++) {
            		node.children[c] = parseNode(args.get(c));
            		node.children[c].parent = node;
            		node.children[c].argposition = (byte) c;
            	}
            }
            else if(func.startsWith("RadRF_entity")) {
            	node = new RadRegFunc();
            	int nextLeftBracketIdx = expression.indexOf('[');
            	int nextRightBracketIdx = expression.indexOf(']');
            	String args_str = expression.substring(nextLeftBracketIdx, nextRightBracketIdx+1);
            	((RadRegFunc)node).setFromString(args_str);
            	int num_child = ((RadRegFunc)node).getArguments().getMaxLength()-3;
            	node.children = new GPNode[num_child];
            	for(int c = 0; c<num_child; c++) {
            		node.children[c] = parseNode(args.get(c));
            		node.children[c].parent = node;
            		node.children[c].argposition = (byte) c;
            	}
            }
            else if(func.startsWith("SinRF_entity")) {
            	node = new SinRegFunc();
            	int nextLeftBracketIdx = expression.indexOf('[');
            	int nextRightBracketIdx = expression.indexOf(']');
            	String args_str = expression.substring(nextLeftBracketIdx, nextRightBracketIdx+1);
            	((SinRegFunc)node).setFromString(args_str);
            	int num_child = node.expectedChildren();
            	node.children = new GPNode[num_child];
            	for(int c = 0; c<num_child; c++) {
            		node.children[c] = parseNode(args.get(c));
            		node.children[c].parent = node;
            		node.children[c].argposition = (byte) c;
            	}
            }
            else if(func.startsWith("PowRF_entity")) {
            	node = new PowRegFunc();
            	int nextLeftBracketIdx = expression.indexOf('[');
            	int nextRightBracketIdx = expression.indexOf(']');
            	String args_str = expression.substring(nextLeftBracketIdx, nextRightBracketIdx+1);
            	((PowRegFunc)node).setFromString(args_str);
            	int num_child = node.expectedChildren();
            	node.children = new GPNode[num_child];
            	for(int c = 0; c<num_child; c++) {
            		node.children[c] = parseNode(args.get(c));
            		node.children[c].parent = node;
            		node.children[c].argposition = (byte) c;
            	}
            }
            else if(func.startsWith("ExpoRF_entity")) {
            	node = new ExpoRegFunc();
            	int nextLeftBracketIdx = expression.indexOf('[');
            	int nextRightBracketIdx = expression.indexOf(']');
            	String args_str = expression.substring(nextLeftBracketIdx, nextRightBracketIdx+1);
            	((ExpoRegFunc)node).setFromString(args_str);
            	int num_child = node.expectedChildren();
            	node.children = new GPNode[num_child];
            	for(int c = 0; c<num_child; c++) {
            		node.children[c] = parseNode(args.get(c));
            		node.children[c].parent = node;
            		node.children[c].argposition = (byte) c;
            	}
            }
            else if(func.startsWith("AvgHub")) {
            	node = new AvgHub();
            	
            	int num_child = 5;
            	node.children = new GPNode[num_child];
            	for(int c = 0; c<num_child; c++) {
            		node.children[c] = parseNode(args.get(c));
            		node.children[c].parent = node;
            		node.children[c].argposition = (byte) c;
            	}
            }
            else if(func.startsWith("MaxHub")) {
            	node = new MaxHub();
            	
            	int num_child = 5;
            	node.children = new GPNode[num_child];
            	for(int c = 0; c<num_child; c++) {
            		node.children[c] = parseNode(args.get(c));
            		node.children[c].parent = node;
            		node.children[c].argposition = (byte) c;
            	}
            }
            else if(func.startsWith("MinHub")) {
            	node = new MinHub();
            	
            	int num_child = 5;
            	node.children = new GPNode[num_child];
            	for(int c = 0; c<num_child; c++) {
            		node.children[c] = parseNode(args.get(c));
            		node.children[c].parent = node;
            		node.children[c].argposition = (byte) c;
            	}
            }
            //=======================================
            switch (func) {
                case "+":
                case "add":
                    node = new Add();
                    node.children = new GPNode[2];
                    node.children[0] = parseNode(args.get(0));
                    node.children[1] = parseNode(args.get(1));
                    node.children[0].parent = node;
                    node.children[1].parent = node;
                    node.children[0].argposition = 0;
                    node.children[1].argposition = 1;
                    break;
                case "-":
                case "sub":
                    node = new Sub();
                    node.children = new GPNode[2];
                    node.children[0] = parseNode(args.get(0));
                    node.children[1] = parseNode(args.get(1));
                    node.children[0].parent = node;
                    node.children[1].parent = node;
                    node.children[0].argposition = 0;
                    node.children[1].argposition = 1;
                    break;
                case "*":
                case "mul":
                    node = new Mul();
                    node.children = new GPNode[2];
                    node.children[0] = parseNode(args.get(0));
                    node.children[1] = parseNode(args.get(1));
                    node.children[0].parent = node;
                    node.children[1].parent = node;
                    node.children[0].argposition = 0;
                    node.children[1].argposition = 1;
                    break;
                case "/":
                case "div":
                    node = new Div();
                    node.children = new GPNode[2];
                    node.children[0] = parseNode(args.get(0));
                    node.children[1] = parseNode(args.get(1));
                    node.children[0].parent = node;
                    node.children[1].parent = node;
                    node.children[0].argposition = 0;
                    node.children[1].argposition = 1;
                    break;
                case "max":
                    node = new Max();
                    node.children = new GPNode[2];
                    node.children[0] = parseNode(args.get(0));
                    node.children[1] = parseNode(args.get(1));
                    node.children[0].parent = node;
                    node.children[1].parent = node;
                    node.children[0].argposition = 0;
                    node.children[1].argposition = 1;
                    break;
                case "min":
                    node = new Min();
                    node.children = new GPNode[2];
                    node.children[0] = parseNode(args.get(0));
                    node.children[1] = parseNode(args.get(1));
                    node.children[0].parent = node;
                    node.children[1].parent = node;
                    node.children[0].argposition = 0;
                    node.children[1].argposition = 1;
                    break;
                case "if":
                    node = new IF();
                    node.children = new GPNode[3];
                    node.children[0] = parseNode(args.get(0));
                    node.children[1] = parseNode(args.get(1));
                    node.children[2] = parseNode(args.get(2));
                    node.children[0].parent = node;
                    node.children[1].parent = node;
                    node.children[2].parent = node;
                    node.children[0].argposition = 0;
                    node.children[1].argposition = 1;
                    node.children[2].argposition = 2;
                    break;
                case "sin":
                	node = new Sin();
                    node.children = new GPNode[1];
                    node.children[0] = parseNode(args.get(0));
                    node.children[0].parent = node;
                    node.children[0].argposition = 0;
                    break;
                case "cos":
                	node = new Cos();
                    node.children = new GPNode[1];
                    node.children[0] = parseNode(args.get(0));
                    node.children[0].parent = node;
                    node.children[0].argposition = 0;
                    break;
                case "ln":
                	node = new Ln();
                    node.children = new GPNode[1];
                    node.children[0] = parseNode(args.get(0));
                    node.children[0].parent = node;
                    node.children[0].argposition = 0;
                    break;
                case "sqr":
                	node = new Sqrt();
                    node.children = new GPNode[1];
                    node.children[0] = parseNode(args.get(0));
                    node.children[0].parent = node;
                    node.children[0].argposition = 0;
                    break;
                case "exp":
                	node = new Exp();
                    node.children = new GPNode[1];
                    node.children[0] = parseNode(args.get(0));
                    node.children[0].parent = node;
                    node.children[0].argposition = 0;
                    break;
                case "pow2":
                	node = new Pow2();
                    node.children = new GPNode[1];
                    node.children[0] = parseNode(args.get(0));
                    node.children[0].parent = node;
                    node.children[0].argposition = 0;
                    break;
                case "tanh":
                	node = new Tanh();
                    node.children = new GPNode[1];
                    node.children[0] = parseNode(args.get(0));
                    node.children[0].parent = node;
                    node.children[0].argposition = 0;
                    break;
                case "relu":
                	node = new ReLu();
                    node.children = new GPNode[1];
                    node.children[0] = parseNode(args.get(0));
                    node.children[0].parent = node;
                    node.children[0].argposition = 0;
                    break;
                default:
                    break;
            }
        }
        else {
            if (NumberUtils.isNumber(expression)) {
                node = new ConstantGPNode(Double.valueOf(expression));
            }
            else if(expression.startsWith("Rad_entity")) {
            	node = new Radius_EntityNode();
            	((Radius_EntityNode)node).setFromString(expression);
            }
            else if(expression.startsWith("R")){
            	String indexStr = expression.substring(1, expression.length());
            	int index = Integer.valueOf(indexStr);
            	node = new ReadRegisterGPNode(index);
            }
            else if(expression.startsWith("In")) {
            	String indexStr = expression.substring(2, expression.length());
            	int index = Integer.valueOf(indexStr);
            	node = new InputFeature4Class(index);
            }

            else if(expression.startsWith("LR_entity")) {
            	node = new LR_EntityNode();
            	((LR_EntityNode)node).setFromString(expression);
            }
            else if(expression.startsWith("LDA_entity")) {
            	node = new LDA_EntityNode();
            	((LDA_EntityNode)node).setFromString(expression);
            }
            else if(expression.startsWith("PLSR_entity")) {
            	node = new PLSR_EntityNode();
            	((PLSR_EntityNode)node).setFromString(expression);
            }

            else if(expression.startsWith("CondLR_entity")) {
            	node = new CondLR_EntityNode();
            	((CondLR_EntityNode)node).setFromString(expression);
            }
            
            node.children = new GPNode[0];
        }

        return node;
	}
	
	public static void main(String[] args) {

        String path = "D:/zhixing/科研/plant_N_food/result/";
        String algo = "LGP-TP-maxrange";
        String scenario = "InGaARaman_V4_SNV_DA-RSE-0";


        String sourcePath = path + algo + "/" + scenario + "/";

        int numRuns = 13;
        
        int numRegs = 30;
        
        int maxIterations = 100;
        
        List<Integer> outputRegs = new ArrayList<>();
        outputRegs.add(0);

        for (int run =  12; run < numRuns; run++) {
            File sourceFile = new File(sourcePath + "job." + run + ".out.stat");
            File outFile = new File(sourcePath + "job." + run + ".bestrule.dot");

            List<String> expressions =
                    ResultFileReader4LGPSRMT.readLispExpressionFromFile4LGP(sourceFile,
                    		numRegs, maxIterations, false, outputRegs);

            String bestExpression = expressions.get(expressions.size()-1);
            LGPIndividual4Class rule = LispParser4Class.parseClassLGPRule(bestExpression, numRegs, maxIterations);
            String bestGraphVizTree = rule.makeGraphvizRule(outputRegs);

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outFile.getAbsoluteFile()));
                writer.write(bestGraphVizTree);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        String expression = " (* (+ (max (* (* (/ (+ WINQ PT) (* NOR WIQ)) (max SL (/ W NINQ))) (min WIQ (max SL (min PT t)))) WIQ) (max (* (* (/ (/ W NINQ) (* NOR WIQ)) (max SL (/ W NINQ))) (min WIQ (max SL (/ W NINQ)))) WIQ)) (* (min PT t) (/ WIQ W)))";
//        expression = LispSimplifier.simplifyExpression(expression);
//        GPTree tree = LispParser4SRMT.parseSymRegRule(expression);
//        System.out.println(tree.child.makeGraphvizTree());
    }
}
