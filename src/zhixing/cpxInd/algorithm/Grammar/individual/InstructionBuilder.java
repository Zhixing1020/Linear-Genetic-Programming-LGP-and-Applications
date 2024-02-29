package zhixing.cpxInd.algorithm.Grammar.individual;

import java.util.ArrayList;
import java.util.Vector;

import org.spiderland.Psh.booleanStack;
import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.gp.ERC;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeParent;
import ec.gp.GPTree;
import ec.gp.GPType;
import ec.gp.koza.HalfBuilder;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.DerivationRule;
//import yimei.jss.gp.terminal.TerminalERCUniform;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;

public abstract class InstructionBuilder extends HalfBuilder{

	public ArrayList<GPTreeStruct> genInstructionSeq(
			final EvolutionState state, 
			final GPType type,
			final int thread,
			final GPTreeStructGrammar indPrototype,
	        final GPFunctionSet set,
			final DTNode node){
		
		ArrayList<GPTreeStruct> treelist = new ArrayList<>();
		if(node.moduleName.equals(DerivationRule.INSTRUCTION)) {
			
			for(int j = 0;j<node.repeatnum;j++) {
				treelist.add(genOneInstr(state, type, thread, indPrototype, set, node, node.instructionChildren.size()));
			}
			
		}
		else {
			
			for(DTNode n : node.siblings) {
				treelist.addAll(genInstructionSeq(state, type, thread, indPrototype, set, n));
			}
			
		}
		
		return treelist;
	}
	
	public GPTreeStructGrammar genOneInstr(final EvolutionState state,
	        final GPType type,
	        final int thread,
	        final GPTreeStructGrammar indPrototype,
	        final GPFunctionSet set,
			final DTNode node,
			final int insertInd) {
		
//		GPTreeStruct tree = (GPTreeStruct)(state.parameters.getInstanceForParameterEq(
//                privateParameter,defaultBase().push(P_TREE).push(""+0),GPTreeStruct.class));
		
		GPTreeStructGrammar instr = (GPTreeStructGrammar) indPrototype.lightClone();
		//instr.grammarNode = (DTNode) node.clone();
		instr.grammarNode = node;
		node.instructionChildren.add(insertInd, instr);
		
		int t = type.type;
        GPNode[] terminals = set.terminals[t];
        GPNode[] nonterminals = set.nonterminals[t];
        GPNode[] nodes = set.nodes[t];
        GPNode[] registers = set.registers[t];
        GPNode[] nonregisters = set.nonregisters[t];
        GPNode[] constants = set.constants[t];
        GPNode[] nonconstants = set.nonconstants[t]; // the terminals but not constants
        
        boolean nooverlap = true;
        
        if (nodes.length == 0)
            errorAboutNoNodeWithType(type, state);   // total failure
        

		//select one primitive string
        //destination register
        GPNode des = genOneNodeByGrammar(state,type,thread,instr,instr,set,0,0);
        
        des.argposition = (byte)0;
        des.parent = instr;
        instr.child = des;
        
        //function
        GPNode fun = genOneNodeByGrammar(state,type,thread,instr,des,set,0,1);
        
        fun.argposition = (byte)0;
        fun.parent = des;
        des.children[0] = fun;
        
        //source registers. that depends
        if (terminals.length != 0)  {
        	
        	for(int arg = 2; arg<node.param_value.size(); arg++) {
            	GPNode src = genOneNodeByGrammar(state,type,thread,instr,fun,set,arg-2,2);                
                src.argposition = (byte)(arg-2);
                src.parent = fun;
                fun.children[arg-2] = src;
            }
        }
        
        
		
		//return instruction
        return instr;
	}
	
	public abstract GPNode genOneNodeByGrammar(final EvolutionState state,
	        final GPType type,
	        final int thread,
	        final GPTreeStructGrammar tree,
	        final GPNodeParent parent,
	        final GPFunctionSet set,
	        final int argposition,
	        final int curdepth) ;
}
