package zhixing.djss.algorithm.LandscapeOptimization.simpleLGP.indexing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

import ec.EvolutionState;
import ec.gp.ERC;
import ec.gp.GPFunctionSet;
import ec.gp.GPNode;
import ec.gp.GPNodeParent;
import ec.gp.GPType;
import ec.gp.koza.KozaBuilder;
import ec.util.Parameter;
import yimei.jss.gp.GPRuleEvolutionState;
import yimei.jss.gp.terminal.TerminalERC;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexSymbolBuilder;
import zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.indexing.SimpleLGPBuilder;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.primitive.Branching;
import zhixing.cpxInd.individual.primitive.ConstantGPNode;
import zhixing.cpxInd.individual.primitive.FlowOperator;
import zhixing.cpxInd.individual.primitive.Iteration;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
//import zhixing.symbolicregression.individual.primitive.InputFeatureGPNode;
//import zhixing.symbolicregression.optimization.GPSymbolicRegression;

public class SimpleLGPBuilder4DJSS extends SimpleLGPBuilder {
//
//	public static final String SIMPLEBUILDER = "simpleLGPbuilder";
//	
//	public ArrayList<GPNode> desReg;
//	public ArrayList<GPNode> fun;
//	public ArrayList<GPNode> constant;
//	public ArrayList<GPNode> srcReg;
//
//	public void setup(final EvolutionState state, final Parameter base)
//    {
//	    super.setup(state,base);
//	
//	    Parameter def = defaultBase();
//
//    }
//	
//	@Override
//	public Parameter defaultBase() {
//		return new Parameter(SIMPLEBUILDER);
//	}
//
//	@Override
//	public GPNode newRootedTree(EvolutionState state, GPType type, int thread, GPNodeParent parent, GPFunctionSet set,
//			int argposition, int requestedSize) {
//		
//		System.err.print("the SimpleLGPBuilder does not support newRootedTree function\n");
//		System.exit(1);
//		
//		return null;
//	}
	
	public void setSymbolSet(EvolutionState state, GPType type,final int argposition, GPFunctionSet set) {
		
        desReg = new ArrayList<>();
        fun = new ArrayList<>();
        constant = new ArrayList<>();
        srcReg = new ArrayList<>();
		
		int t = type.type;
        GPNode[] terminals = set.terminals[t];
        GPNode[] nonterminals = set.nonterminals[t];
        GPNode[] nodes = set.nodes[t];
        
        GPNode[] registers = set.registers[t];
        GPNode[] nonregisters = set.nonregisters[t];
        GPNode[] constants = set.constants[t];
        GPNode[] nonconstants = set.nonconstants[t]; // the terminals but not constants
        
        if (nodes.length == 0)
            errorAboutNoNodeWithType(type, state);   // total failure
        
        //destination registers
        for(int i = 0; i<registers.length; i++) {
        	WriteRegisterGPNode n = (WriteRegisterGPNode)(registers[i].lightClone());
        	for(int j = 0; j< n.getRange(); j++) {
        		WriteRegisterGPNode m = (WriteRegisterGPNode) n.lightClone();
        		m.setIndex(j);
        		m.argposition = (byte)argposition;
        		/*m.parent will be set when constructing instructions*/
        		desReg.add(m);
        	}
        }
        
        //functions
        for(int i = 0;i<nonregisters.length;i++){
        	boolean feasibleFunction = true;
        	for (int j = 0;j<terminals.length;j++){
        		if(nonregisters[i].toString().equals(terminals[j].toString())){ 
        			feasibleFunction = false;
        			break;
        		}
        	}
        	if(feasibleFunction) {
        		GPNode m = (GPNode)nonregisters[i].lightClone();
        		m.argposition = (byte)argposition;
        		fun.add(m);
        	}
        	
        }
        
        //constants
	    for(int i = 0; i<constants.length; i++) {
	    	GPNode n = constants[i].lightClone();
	    	
//	    	if(n instanceof InputFeatureGPNode) {
//	    		if(state.evaluator.p_problem instanceof GPSymbolicRegression) {
//	    			((InputFeatureGPNode)n).setRange(((GPSymbolicRegression) state.evaluator.p_problem).datadim);
//	    		}
//	    		for(int j = 0; j< ((InputFeatureGPNode)n).getRange(); j++) {
//	    			InputFeatureGPNode m = (InputFeatureGPNode) n.lightClone();
//	    			m.setIndex(j);
//	    			m.argposition = (byte)argposition;
//	    			/*m.parent will be set when constructing instructions*/
//	        		constant.add(m);
//	        		
//	    		}
//	    	}
	    	if(n instanceof TerminalERC) {
	    		if(! (state instanceof GPRuleEvolutionState)) {
	    			System.err.print("the state in SimpleLGPBuilder.enumerateSymbols is not GPRuleEvolutionState, we fail to enumerate terminals");
	    			System.exit(1);
	    		}
	    		
	    		LinkedList<GPNode> termList = (LinkedList<GPNode>) ((GPRuleEvolutionState)state).getTerminals();
	    		for(GPNode term : termList) {
	    			if(term instanceof ERC) {
	    				System.err.print("the SimpleLGPBuilder.enumerateSymbols does not support ERC in TerminalERC");
	        			System.exit(1);
	    			}
	    			
	    			TerminalERC m = (TerminalERC) n.lightClone();
	    			m.setTerminal(term);
	    			m.argposition = (byte)argposition;
	    			/*m.parent will be set when constructing instructions*/
	        		constant.add(m);
	        		
	    		}
	    		
	    	}
	    	if(n instanceof ConstantGPNode) {
	    		//in many cases we use ERC as Constant nodes. but the random numbers in ERC leads to infinite constants. So we just list constants based on a step here
	    		Vector<Double> range = ((ConstantGPNode) n).getRange();
	    		for(Double d : range) {
	    			ConstantGPNode m = (ConstantGPNode) n.lightClone();
	    			m.setValue(d);
	    			m.argposition = (byte)argposition;
	    			/*m.parent will be set when constructing instructions*/
	        		constant.add(m);
	        		
	    		}
	    	}
	    }
        
	    //source registers
        for(int i = 0; i<nonconstants.length; i++) {
        	ReadRegisterGPNode n = (ReadRegisterGPNode) nonconstants[i].lightClone();
        	
        	for(int j = 0; j< n.getRange(); j++) {
        		ReadRegisterGPNode m = (ReadRegisterGPNode) n.lightClone();
        		m.setIndex(j);
        		m.argposition = (byte)argposition;
        		/*m.parent will be set when constructing instructions*/
        		srcReg.add(m);
        	}
        }
	}
	
//	@Override
//	public ArrayList<GPTreeStruct> enumerateSymbols(EvolutionState state, GPType type, int thread,final int argposition, GPFunctionSet set){
//		
//		ArrayList<GPTreeStruct> collect = new ArrayList<>(maxNumSymbols);
//		
//		setSymbolSet(state, type,argposition, set);
//        
//        //fix size: since LGP instructions are with fixed depth, we skip this step
//        
//        //list all the possible primitives for certain positions
////        ArrayList<GPNode> desReg = new ArrayList<>(nodes.length);
////        ArrayList<GPNode> fun = new ArrayList<>(nodes.length);
//        ArrayList<GPNode> arg0 = new ArrayList<>();
//        ArrayList<GPNode> arg1 = new ArrayList<>();
//        
//        
//        for(int i = 0; i<constant.size();i++) {
//        	GPNode n = constant.get(i).lightClone();
//        	
//        	arg0.add(n);
//        	arg1.add(n);
//        }
//        for(int i = 0; i<srcReg.size(); i++) {
//        	GPNode n = srcReg.get(i).lightClone();
//        	
//        	arg0.add(n);
//        	arg1.add(n);
//        }
//        
//        //construct instructions
//        for(GPNode a : desReg) {
//
//        	for(GPNode b : fun) {
//
//        		for(int c = 0; c<arg0.size();c++) {
//
//    				for(int d = 0; d<arg1.size(); d++) {
//    					GPTreeStruct cand = new GPTreeStruct();
//						cand.child = a.lightClone();
//						cand.child.children[0] = b.lightClone();
//						cand.child.children[0].children[0] = arg0.get(c).lightClone(); //at least one input, such as Cos, Sin, Exp, Ln
//						if(cand.child.children[0].children.length == 2)
//							cand.child.children[0].children[1] = arg1.get(d).lightClone();
//						
//						if(b instanceof Branching) {
//							cand.type = GPTreeStruct.BRANCHING;
//						}
//						else if(b instanceof Iteration) {
//							cand.type = GPTreeStruct.ITERATION;
//						}
//    					
//    					if(verifyInstruction(cand.child)) {
//
//    						collect.add(cand);
//    					}
//    				}
//    			}
//        	}
//        }
//        
//        if(collect.size() > maxNumSymbols) {
//        	System.out.print("there are too many symbols in the list, please consider using simpler symbols");
//        }
//        
//        initialized = true;
//        
//        return collect;
//	}
//
//	protected boolean verifyInstruction(final GPNodeParent parent) {
//		
//		boolean res = true;
//		
//		//check the ratio of constants
//    	GPNode root = (GPNode)parent;
//    	
//    	if(root instanceof FlowOperator) return true;
//    	
//    	int terminalsize = root.numNodes(GPNode.NODESEARCH_TERMINALS);
//    	int constnatsize = root.numNodes(GPNode.NODESEARCH_CONSTANT);
//    	int nullsize = root.numNodes(GPNode.NODESEARCH_NULL);
//    	if(terminalsize > 0)
//    		res = (constnatsize)/ (terminalsize + nullsize) <= probCons; //if add one more constant, will it be larger than probCons?
//    	else {
//			res = false;//never initialize constant as the first terminal in instruction
//		}
//    	
//    	
//    	return res;
//	}
}
