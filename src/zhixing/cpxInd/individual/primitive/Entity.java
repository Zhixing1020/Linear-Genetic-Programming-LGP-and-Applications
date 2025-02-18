package zhixing.cpxInd.individual.primitive;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.util.Parameter;

public abstract class Entity extends GPNode implements EntityInterface{

	public final static String P_ARGS = "argument"; 
	
	public final static double LOOP_TERMINATE = 1E8;
	
	protected Args arguments;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		Parameter pp = base.push(P_ARGS);
		arguments = (Args) state.parameters.getInstanceForParameter(pp, null, GPNode.class);
    	if(arguments == null) {
    		state.output.fatal("Entity must have arguments");
    		System.exit(1);
    	}
    	
    	arguments.setup(state, base);
    	arguments.parent = this;
	}
	
	public void setFromString(String expression) {
		int L_bracket = expression.indexOf('[');
		int R_bracket = expression.indexOf(']');
    	String arguments_str = expression.substring(L_bracket+1, R_bracket);
    	String split[] = arguments_str.split(",");
    	int maxlen = split.length;
    	arguments = new Args();
    	arguments.parent = this;
    	arguments.setMaxLength(maxlen);
    	for(int i = 0; i<maxlen; i++) {
    		arguments.setValue(i, Double.valueOf(split[i]));
    	}
	}
	
//	abstract protected String getPrefix();
	
	public Args getArguments() {
		return arguments;
	}
	
	@Override
    public int expectedChildren() {
        return 0;
    }
	
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
    
    @Override
    public void resetNode(final EvolutionState state, final int thread) {
    	arguments.resetNode(state, thread);
    }
    
    @Override
    public GPNode lightClone() {
    	GPNode n = super.lightClone();
    	((Entity)n).arguments = (Args) this.arguments.lightClone();
    	((Entity)n).arguments.parent = n;
    	return n;
    }
}
