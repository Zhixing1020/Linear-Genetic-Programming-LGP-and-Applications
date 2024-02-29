package zhixing.djss.algorithm.Grammar.individual;

import java.util.Vector;

import ec.EvolutionState;
import ec.gp.ERC;
import ec.gp.GPFunctionSet;
import ec.gp.GPNode;
import ec.gp.GPNodeParent;
import ec.gp.GPType;
import yimei.jss.gp.terminal.TerminalERCUniform;
import zhixing.cpxInd.individual.primitive.*;
import zhixing.cpxInd.algorithm.Grammar.individual.DTNode;
import zhixing.cpxInd.algorithm.Grammar.individual.GPTreeStructGrammar;
import zhixing.cpxInd.algorithm.Grammar.individual.InstructionBuilder;

public class InstructionBuilder4DJSS extends InstructionBuilder{

	@Override
	public GPNode genOneNodeByGrammar(final EvolutionState state,
	        final GPType type,
	        final int thread,
	        final GPTreeStructGrammar tree,
	        final GPNodeParent parent,
	        final GPFunctionSet set,
	        final int argposition,
	        final int curdepth) {
		
		int t = type.type;
        GPNode[] terminals = set.terminals[t];
        GPNode[] nonterminals = set.nonterminals[t];
        GPNode[] nodes = set.nodes[t];
        GPNode[] registers = set.registers[t];
        GPNode[] nonregisters = set.nonregisters[t];
        GPNode[] constants = set.constants[t];
        GPNode[] nonconstants = set.nonconstants[t]; // the terminals but not constants
        
        DTNode node = tree.grammarNode;
        
        boolean nooverlap = true;
		
		if(curdepth == 0) { //destination
			GPNode des = (GPNode)(registers[state.random[thread].nextInt(registers.length)].lightClone());
	        des.resetNode(state,thread);  // give ERCs a chance to randomize

	        String primitiveName = des.toString().substring(0, des.toString().length()-1);
	        for(int ii = 0; ii<100; ii++) {
	        	if(node.param_value.get(0).contains(primitiveName)) {nooverlap=false; break;}
	        	
	        	//des.resetNode(state,thread);
	        	((WriteRegisterGPNode)des).enumerateNode(state, thread);
	        	primitiveName = des.toString().substring(0, des.toString().length()-1);
	        }
	        
	        if(nooverlap) {
	        	System.err.println("the grammar rules do not cover suitable destination reigsters");
	        	System.exit(1);
	        }
	        
	        return des;
		}
		else if (curdepth == 1) {  //function
			Vector<GPNode> nodesToPick_v = new Vector();
	        
	        for(int i = 0;i<set.nonregisters[type.type].length;i++){
	        	boolean feasibleFunction = true;
	        	for (int j = 0;j<set.terminals[type.type].length;j++){
	        		if(set.nonregisters[type.type][i].toString().equals(set.terminals[type.type][j].toString())){ 
	        			feasibleFunction = false;
	        			break;
	        		}
	        	}
//	        	for(int j = 0;j<set.constants[type.type].length; j++) {
//	        		if(set.nonregisters[type.type][i].toString().equals(set.constants[type.type][j].toString()) && !canAddConstant(parent)){//cannot add more constant
//	        			feasibleFunction = false;
//	        			break;
//	        		}
//	        	}
	        	if(feasibleFunction) 
	        		nodesToPick_v.add(set.nonregisters[type.type][i].lightClone());
	        }
	        GPNode[] nodesToPick = (GPNode[])nodesToPick_v.toArray(new GPNode[nodesToPick_v.size()]);
	        if (nodesToPick==null || nodesToPick.length ==0)                            // no nonterminals, hope the guy knows what he's doing!
	            nodesToPick = set.terminals[type.type];                                 // this can only happen with the warning about nonterminals above
	        
	        int trial = state.random[thread].nextInt(nodesToPick.length);
	        GPNode fun = (GPNode)(nodesToPick[trial].lightClone());
	        
	        fun.resetNode(state, thread);
	        
	        String primitiveName = fun.toStringForHumans();
	        for(int ii = 0; ii<100; ii++) {
	        	if(node.param_value.get(1).contains(primitiveName)) {nooverlap=false; break;}
	        	
	        	fun = (GPNode)(nodesToPick[(++trial)%nodesToPick.length].lightClone());
	        	fun.resetNode(state, thread);
	        	primitiveName = fun.toStringForHumans();
	        }
	        
	        if(nooverlap) {
	        	System.err.println("the grammar rules do not cover suitable functions");
	        	System.exit(1);
	        }
	        
	        return fun;
		}
		else if (curdepth == 2) {  //source registers
//			int arg = state.random[thread].nextInt(tree.child.children[0].children.length);
			int arg = argposition;
			
			GPNode src;
        	if(state.random[thread].nextDouble()<probCons && canAddConstant(parent)) {
        		src = (GPNode)(constants[state.random[thread].nextInt(constants.length)].lightClone());
        	}
        	else {
        		src = (GPNode)(nonconstants[state.random[thread].nextInt(nonconstants.length)].lightClone());
        	}
        	
            src.resetNode(state, thread);
        	
            String primitiveName = src.toString();
            if(!node.param_value.get(arg+2).contains(primitiveName)) {
            	src = (GPNode)(nonconstants[state.random[thread].nextInt(nonconstants.length)].lightClone());
            	for(int ii = 0; ii<100; ii++) {
                	if(node.param_value.get(arg+2).contains(primitiveName)) {nooverlap=false; break;}
                	
                	//src = (GPNode)(nonconstants[state.random[thread].nextInt(nonconstants.length)].lightClone());
            		//src.resetNode(state,thread);  // give ERCs a chance to randomize
                	((ReadRegisterGPNode)src).enumerateNode(state, thread);
                	
                	primitiveName = src.toString();
                }
            }
            else {
            	nooverlap = false;
            }
            
            GPNode src2;
            if(nooverlap || 
            		(!nooverlap && state.random[thread].nextDouble()<probCons && canAddConstant(parent))){
            	
            	int trial = state.random[thread].nextInt(constants.length);
            	
            	src2 = (GPNode)(constants[trial].lightClone());
            	src2.resetNode(state, thread);
            	boolean nooverlap2 = true;
            	
                primitiveName = src2.toString();
                for(int j = 0;j<constants.length;j++) {
                	for(int ii = 0; ii<100; ii++) {
                		if(node.param_value.get(arg+2).contains(primitiveName)) {nooverlap2=false; break;}
                		
                		if(src2 instanceof ERC) {
                			if(src2 instanceof TerminalERCUniform) {
                				((TerminalERCUniform) src2).enumerateNode(state, thread,node.param_value.get(arg+2));
                    		}
                			else {
                				src2.resetNode(state, thread);
                			}
                		}
                		
                		primitiveName = src2.toString();
                	}
                	
                	if(!nooverlap2) break;
                	trial = (++trial)%constants.length;
                	src2 = (GPNode)(constants[trial].lightClone());
                	src2.resetNode(state, thread);
                	primitiveName = src2.toString();
                }
            	
            	
            	if(!nooverlap2) {
            		src = src2;
            		nooverlap = nooverlap2;
            	}
            }
            if(nooverlap) {
            	System.err.println("the grammar rules do not cover suitable source registers");
            	System.exit(1);
            }
            
            return src;
		}
		
		return super.fullNode_reg(state, curdepth, this.maxDepth, type, thread, parent, argposition, set);
	}
}
