package zhixing.symbolicregression.algorithm.LandscapeOptimization.simpleLGP.indexing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

import ec.EvolutionState;
import ec.gp.ERC;
import ec.gp.GPFunctionSet;
import ec.gp.GPNode;
import ec.gp.GPType;
//import yimei.jss.gp.GPRuleEvolutionState;
//import yimei.jss.gp.terminal.TerminalERC;
import zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.indexing.SimpleLGPBuilder;
import zhixing.cpxInd.individual.primitive.ConstantGPNode;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.symbolicregression.individual.primitive.InputFeatureGPNode;
import zhixing.symbolicregression.optimization.GPSymbolicRegression;

public class SimpleLGPBuilder4SR extends SimpleLGPBuilder {

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
	    	
	    	if(n instanceof InputFeatureGPNode) {
	    		if(state.evaluator.p_problem instanceof GPSymbolicRegression) {
	    			((InputFeatureGPNode)n).setRange(((GPSymbolicRegression) state.evaluator.p_problem).datadim);
	    		}
	    		for(int j = 0; j< ((InputFeatureGPNode)n).getRange(); j++) {
	    			InputFeatureGPNode m = (InputFeatureGPNode) n.lightClone();
	    			m.setIndex(j);
	    			m.argposition = (byte)argposition;
	    			/*m.parent will be set when constructing instructions*/
	        		constant.add(m);
	        		
	    		}
	    	}
//	    	if(n instanceof TerminalERC) {
//	    		if(! (state instanceof GPRuleEvolutionState)) {
//	    			System.err.print("the state in SimpleLGPBuilder.enumerateSymbols is not GPRuleEvolutionState, we fail to enumerate terminals");
//	    			System.exit(1);
//	    		}
//	    		
//	    		LinkedList<GPNode> termList = (LinkedList<GPNode>) ((GPRuleEvolutionState)state).getTerminals();
//	    		for(GPNode term : termList) {
//	    			if(term instanceof ERC) {
//	    				System.err.print("the SimpleLGPBuilder.enumerateSymbols does not support ERC in TerminalERC");
//	        			System.exit(1);
//	    			}
//	    			
//	    			TerminalERC m = (TerminalERC) n.lightClone();
//	    			m.setTerminal(term);
//	    			m.argposition = (byte)argposition;
//	    			/*m.parent will be set when constructing instructions*/
//	        		constant.add(m);
//	        		
//	    		}
//	    		
//	    	}
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
}
