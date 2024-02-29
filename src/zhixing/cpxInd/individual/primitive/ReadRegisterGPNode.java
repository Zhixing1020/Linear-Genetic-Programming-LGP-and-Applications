package zhixing.cpxInd.individual.primitive;

import java.util.LinkedList;

import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.ERC;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import yimei.jss.gp.data.DoubleData;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.djss.individual.LGPIndividual4DJSS;

public class ReadRegisterGPNode extends GPNode {
	
	public final static String P_NUMREGISTERS = "numregisters";
	
	protected int index;
	
	protected int range;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		Parameter pp_numreg = base.push(P_NUMREGISTERS);
    	range = state.parameters.getInt(pp_numreg,null,0);
    	if(range < 1) {
    		state.output.fatal("number of registers must be >=1");
    	}

	}
	
    public ReadRegisterGPNode(int ind, int range) {
        super();
        children = new GPNode[0];
        this.index = ind;
        
        this.range = range;
    }
    
    public ReadRegisterGPNode(int ind) {
        super();
        children = new GPNode[0];
        this.index = ind;
    }
    
    public ReadRegisterGPNode() {
        super();
        children = new GPNode[0];
        this.index = 0;
    }

    public int getIndex() {
        return index;
    }
    
    public int getRange() {
        return range;
    }
    
    public void setIndex(int ind){
    	index = ind;
    	
    }
    
    public void setRange(int x) {
    	range = x;
    }

    @Override
    public String toString() {
        return "R" + index;
    }

    @Override
    public int expectedChildren() {
        return 0;
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {

        DoubleData data = ((DoubleData)input);
        LGPIndividual ind = ((LGPIndividual)individual);
        data.value = ind.getRegisters(index);
        //data.x = ((LGPIndividual)individual).getRegisters()[index];
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof ReadRegisterGPNode) {
        	ReadRegisterGPNode o = (ReadRegisterGPNode)other;
            return (index == o.getIndex());
        }

        return false;
    }
    
    @Override
    public void resetNode(final EvolutionState state, final int thread) {
    	index = state.random[thread].nextInt(range);
    }

    //========================Grammar LGP, Zhixig 2022.12.28====================
    public void enumerateNode(EvolutionState state, int thread) {
//    	int increment = state.random[thread].nextInt(range-1)+1;
    	index = (++index)%range;
    }
    
    @Override
    public GPNode lightClone() {
    	
    	GPNode n = super.lightClone();
    	((ReadRegisterGPNode)n).setIndex(index);
    	((ReadRegisterGPNode)n).setRange(range);
    	return n;
    }
}
