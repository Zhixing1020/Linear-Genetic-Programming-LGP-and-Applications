package zhixing.cpxInd.individual.primitive;

import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import yimei.jss.gp.data.DoubleData;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.djss.individual.LGPIndividual4DJSS;

public class WriteRegisterGPNode extends GPNode {
	
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

    public WriteRegisterGPNode(int ind, int range) {
        super();
        this.index = ind;
        
        this.range = range;
    }
    
    public WriteRegisterGPNode(){
    	super();
    	this.index = 0;
    }

    public int getIndex() {
        return index;
    }
    
    public int getRange() {
        return range;
    }
    
    public void setIndex(int x) {
    	index = x;
    }
    
    public void setRange(int x) {
    	range = x;
    }
    
	public String toString() {
		return "R" + index + "=";
	}

    public int expectedChildren() {
    	return 1;
    }

    public void eval(final EvolutionState state,
    		final int thread,
    		final GPData input,
    		final ADFStack stack,
    		final GPIndividual individual,
    		final Problem problem) {

        DoubleData rd = ((DoubleData)(input));
        LGPIndividual ind = ((LGPIndividual)individual);

		children[0].eval(state,thread,input,stack,individual,problem);
		ind.setRegister(index, rd.value);
//        children[0].eval(state,thread,input,stack,individual,problem);
//		((LGPIndividual)individual).setRegister(index, rd.x);
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
    	((WriteRegisterGPNode)n).setIndex(index);
    	((WriteRegisterGPNode)n).setRange(range);
    	return n;
    }
}
