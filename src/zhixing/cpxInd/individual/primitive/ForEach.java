package zhixing.cpxInd.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import yimei.jss.gp.data.DoubleData;

public class ForEach extends Iteration{
	protected int iter = 0;
	protected int maxiter = 1;
	
	public void resetIter(int size) {
		iter = 0;
		resetContainerSize(size);
	}
	
	protected void resetContainerSize(int size) {
		if(size<0) {
			System.err.print("ForEach meets an illegal container\n");
			System.exit(1);
		}
		maxiter = size;
	}
	
	public boolean progressIterate() {
		if(iter<maxiter) {
			iter++;
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public String toString() { return "FOREACH#"+this.bodyLength; }
	
	@Override
	public int expectedChildren() { return 1; }
	
	@Override
	public void eval(final EvolutionState state,
	    final int thread,
	    final GPData input,
	    final ADFStack stack,
	    final GPIndividual individual,
	    final Problem problem)
	    {
	    double result;
	    DoubleData rd = ((DoubleData)(input));
	
	    children[0].eval(state,thread,input,stack,individual,problem);
	    result = rd.value;
	
	    children[1].eval(state,thread,input,stack,individual,problem);
	    
	    if(result <= rd.value){
	    	rd.value = 1.0;
	    }
	    else{
	    	rd.value = 0.0;
	    }
	    
	    }
	
	@Override
    public String toStringForHumans() {
		return "ForEach"+this.bodyLength;
	}
	
//	@Override
//    public void resetNode(final EvolutionState state, final int thread) {
//    	bodyLength = state.random[thread].nextInt(maxbodyLength)+1;
//    }
}
