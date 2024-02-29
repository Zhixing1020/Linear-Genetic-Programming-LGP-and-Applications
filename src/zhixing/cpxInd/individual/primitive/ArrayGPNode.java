package zhixing.cpxInd.individual.primitive;

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

public class ArrayGPNode extends GPNode{
	public final static String P_SIZE = "maxsize";
	public final static String P_NAME = "arrayname";
	
	protected static String name;
	
	protected static int range;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		Parameter pp_maxsize = base.push(P_SIZE);
    	range = state.parameters.getInt(pp_maxsize,null,1);
    	if(range < 1) {
    		state.output.fatal("number of registers must be >=1");
    	}

	}
	
    public ArrayGPNode(int range) {
        super();
        children = new GPNode[0];
        
        this.range = range;
    }
    
    public ArrayGPNode() {
    	super();
    	children = new GPNode[0];
    	this.range = 1;
    }
    
    @Override
    public String toString() {
        return name;
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
//        data.value = ind.getRegisters(index);
        //data.x = ((LGPIndividual)individual).getRegisters()[index];
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
