package yimei.jss.gp.terminal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.gp.ERC;
import ec.gp.GPNode;
import ec.gp.GPNodeParent;
import ec.util.Parameter;
import yimei.jss.gp.GPRuleEvolutionState;

/**
 * The terminal ERC, with uniform selection.
 *
 * @author yimei
 */

public class TerminalERCUniform extends TerminalERC {

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        terminal = ((GPRuleEvolutionState)state).pickTerminalRandom();
    }

    @Override
    public void resetNode(EvolutionState state, int thread) {
        terminal = ((GPRuleEvolutionState)state).pickTerminalRandom();

        if (terminal instanceof ERC) {
            ERC ercTerminal = new DoubleERC();
            ercTerminal.resetNode(state, thread);
            terminal = ercTerminal;
        }
    }

    @Override
    public void mutateERC(EvolutionState state, int thread) {
        resetNode(state, thread);
    }
    
    //========================Grammar LGP, Zhixig 2022.12.28====================
    public void enumerateNode(EvolutionState state, int thread) {
    	
    	int index = 0; 
    	LinkedList<GPNode> termList = (LinkedList<GPNode>) ((GPRuleEvolutionState)state).getTerminals();
    	for(int i = 0; i<termList.size(); i++) {
    		if(termList.get(i).toString().equals(terminal.toString())) {
    			index = i;
    		}
    	}
    	int increment = state.random[thread].nextInt(termList.size()-1)+1;
    	index = (index+increment)%termList.size();
    	
    	terminal = termList.get(index);;

        if (terminal instanceof ERC) {
            ERC ercTerminal = new DoubleERC();
            ercTerminal.resetNode(state, thread);
            terminal = ercTerminal;
        }
    }
    
public void enumerateNode(EvolutionState state, int thread, Set<String> cand) {
    	
    	int index = 0; 
    	ArrayList<Integer> newindex = new ArrayList<>();
    	
    	LinkedList<GPNode> termList = (LinkedList<GPNode>) ((GPRuleEvolutionState)state).getTerminals();
    	
    	for(int i = 0; i<termList.size(); i++) {
//    		if(termList.get(i).toString().equals(terminal.toString())) {
//    			index = i;
//    		}
    		if(cand.contains(termList.get(i).toString())) {
    			newindex.add(i);
    		}
    	}
//    	int increment = state.random[thread].nextInt(termList.size()-1)+1;
//    	index = (++index)%termList.size();
    	
    	if(newindex.isEmpty()) return;
    	index = state.random[thread].nextInt(newindex.size());
    	
    	terminal = termList.get(newindex.get(index));

        if (terminal instanceof ERC) {
            ERC ercTerminal = new DoubleERC();
            ercTerminal.resetNode(state, thread);
            terminal = ercTerminal;
        }
    }
}
