package zhixing.symbolicregression.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import ec.gp.GPNode;
import ec.gp.GPTree;
import yimei.jss.ruleanalysis.ResultFileReader;
import yimei.jss.ruleanalysis.RuleType;
import yimei.util.lisp.LispSimplifier;
import yimei.util.lisp.LispUtil;
import zhixing.cpxInd.individual.primitive.ConstantGPNode;
import zhixing.cpxInd.individual.primitive.IFLargerThan;
import zhixing.cpxInd.individual.primitive.IFLessEqual;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WhileLargeLoop;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.symbolicregression.individual.primitive.Add;
import zhixing.symbolicregression.individual.primitive.Cos;
import zhixing.symbolicregression.individual.primitive.Div;
import zhixing.symbolicregression.individual.primitive.Exp;
import zhixing.symbolicregression.individual.primitive.IF;
import zhixing.symbolicregression.individual.primitive.InputFeatureGPNode;
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

public class LispParser {
	public static GPTree parseSymRegRule(String expression) {
        GPTree tree = new GPTree();

        expression = expression.trim();

        tree.child = parseNode(expression);

        return tree;
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
            else if(expression.startsWith("R")){
            	String indexStr = expression.substring(1, expression.length());
            	int index = Integer.valueOf(indexStr);
            	node = new ReadRegisterGPNode(index);
            }
            else if(expression.startsWith("In")) {
            	String indexStr = expression.substring(2, expression.length());
            	int index = Integer.valueOf(indexStr);
            	node = new InputFeatureGPNode(index);
            }
            node.children = new GPNode[0];
        }

        return node;
    }

    public static void main(String[] args) {

        String path = "/local/scratch/Dropbox/Research/JobShopScheduling/ExpResults/";
        String algo = "simple-gp-basic-terminals";
        String scenario = "mean-weighted-tardiness-0.85-4";


        String sourcePath = path + algo + "/" + scenario + "/";

        int numRuns = 30;

        for (int run =  0; run < numRuns; run++) {
            File sourceFile = new File(sourcePath + "job." + run + ".out.stat");
            File outFile = new File(sourcePath + "job." + run + ".bestrule.dot");

            List<String> expressions =
                    ResultFileReader.readLispExpressionFromFile(sourceFile,
                            RuleType.SIMPLE_RULE, false);

            String bestExpression = expressions.get(expressions.size()-1);
            GPTree bestTree = LispParser.parseSymRegRule(bestExpression);
            String bestGraphVizTree = bestTree.child.makeGraphvizTree();

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outFile.getAbsoluteFile()));
                writer.write(bestGraphVizTree);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String expression = " (* (+ (max (* (* (/ (+ WINQ PT) (* NOR WIQ)) (max SL (/ W NINQ))) (min WIQ (max SL (min PT t)))) WIQ) (max (* (* (/ (/ W NINQ) (* NOR WIQ)) (max SL (/ W NINQ))) (min WIQ (max SL (/ W NINQ)))) WIQ)) (* (min PT t) (/ WIQ W)))";
        expression = LispSimplifier.simplifyExpression(expression);
        GPTree tree = LispParser.parseSymRegRule(expression);
        System.out.println(tree.child.makeGraphvizTree());
    }
}
