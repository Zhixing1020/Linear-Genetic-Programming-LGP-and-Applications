package zhixing.cpxInd.individual.primitive;

import java.util.Vector;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import java.math.BigDecimal;
import yimei.jss.gp.data.DoubleData;

public class Args extends GPNode implements ArgsInterface{
	
	
	public final static String P_ARGUMENTS = "arguments";
	public final static String P_MAXLENGTH = "maxlength";
	public final static String P_STEP = "step";
	
	protected double[] values; //the values of each argument
	protected int maxlength; //the length of the list of values
	
	protected double step = 0.1; //step for variation
	
	Parameter def = new Parameter(P_ARGUMENTS);
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		Parameter pp = base.push(P_MAXLENGTH);
    	maxlength = state.parameters.getIntWithDefault(pp,def.push(P_MAXLENGTH),1);
    	if(maxlength < 1) {
    		state.output.fatal("maximum length of arguments must be >=1");
    		System.exit(1);
    	}
    	values = new double[maxlength];
    	
    	pp = base.push(P_STEP);
    	step = state.parameters.getDoubleWithDefault(pp, def.push(P_STEP), 0.1);
    	if(step <=0) {
    		state.output.fatal("step of arguments must be >0");
    		System.exit(1);
    	}
	}
	
	public Args() {
		super();
		children = new GPNode[0];
		maxlength = 5;
		this.values = new double[maxlength];
	}
	
	public Args(double [] vals) {
		super();
		children = new GPNode[0];
		maxlength = vals.length;
		this.values = new double[maxlength];
		for(int i = 0; i<maxlength; i++) {
			values[i] = vals[i];
		}
	}
	
	public Args(double [] vals, double step) {
		super();
		
		this.step = step;
		
		children = new GPNode[0];
		maxlength = vals.length;
		this.values = new double[maxlength];
		for(int i = 0; i<maxlength; i++) {
			values[i] = vals[i];
		}
	}
	
	public Double getValue(int index) {
		return values[index];
	}
	
	public int getMaxLength(){
		return maxlength;
	}
	
	public void setValue(int index, double v) {
		values[index] = v;
	}
	
	public void setMaxLength(int length) {
		maxlength = length;
		this.values = new double[maxlength];
	}
	
	@Override
    public String toString() {
		String res = "[";
		for(int i = 0; i<maxlength; i++) {
			res += values[i];
			if(i < maxlength - 1) {
				res += ",";
			}
		}
		res +="]";
        return res;
    }

    @Override
    public int expectedChildren() {
        return 0;
    }
    
    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
    	return;
    }
    
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    public boolean equals(Object other) {
    	
    	boolean res = true;
        if (other instanceof Args) {
        	if(! (((Args)other).getMaxLength() == this.getMaxLength())) return false;
            for(int i = 0; i < this.getMaxLength(); i++) {
            	res = res && ((Args)other).getValue(i) == this.getValue(i);
            }
        }

        return res;
    }
    
    @Override
    public void resetNode(final EvolutionState state, final int thread) {
//    	value = range.get(state.random[thread].nextInt(range.size()));
   		
   		for(int i = 0; i<maxlength; i++) {
   			double val = state.random[thread].nextDouble();
//   			values[i] = new BigDecimal(val).setScale(2,BigDecimal.ROUND_FLOOR).floatValue();
   			values[i] = val;
   		}
    }
    
    @Override
    public GPNode lightClone() {
    	GPNode n = super.lightClone();
    	
    	((Args)n).setMaxLength(maxlength);
    	for(int i = 0; i<maxlength; i++) {
    		((Args)n).setValue(i, this.getValue(i));
    	}
    	
    	return n;
    }

	@Override
	public void varyNode(EvolutionState state, int thread, Entity ref) {
		// TODO Auto-generated method stub
//		for(int i = 0; i<maxlength; i++)
		int i = state.random[thread].nextInt(maxlength);
		{
			values[i] = moveByStep(state, thread, values[i]);
		}
	}
	
	protected double moveByStep(EvolutionState state, int thread, double src) {
		double tmp = src;
		double detail = 0;
		while(detail == 0) {
			detail = Math.signum(state.random[thread].nextDouble()*2-1)*(state.random[thread].nextDouble()*step);
		}
		tmp += detail;
		tmp = Math.min(0.99999, tmp);
		tmp = Math.max(0, tmp);
//		new BigDecimal(tmp).setScale(2,BigDecimal.ROUND_FLOOR).floatValue();
		return tmp;
	}
}
