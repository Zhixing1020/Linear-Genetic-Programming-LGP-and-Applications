package zhixing.cpxInd.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class FlowOperator extends GPNode {
	protected int bodyLength;
	protected int maxbodyLength;
	protected int minbodyLength;
	
	FlowOperator(){
		super();
		bodyLength = 1;
		maxbodyLength = 1;
		minbodyLength = 1;
	}
	
	FlowOperator(int maxbodylen){
		super();
		maxbodyLength = maxbodylen;
		bodyLength = 1;
		minbodyLength = 1;
	}
	
	FlowOperator(int maxbodylen, int minbodylen){
		super();
		maxbodyLength = maxbodylen;
		bodyLength = 1;
		minbodyLength = minbodylen;
		
		if(maxbodyLength < minbodyLength) {
			System.err.print("the max body length of Flowoperator should be >= the min body length");
			System.exit(1);
		}
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual,
			Problem problem) {
		// TODO Auto-generated method stub
		
	}
	
	public void setBodyLength(int len){
		if(len > maxbodyLength){
			System.out.print("the objective length > max body length in Branching");
			System.exit(1);
		}
		bodyLength = len;
	}
	
	public void setMaxBodyLength(int len) {
		maxbodyLength = len;
	}
	
	public void setMinBodyLength(int len) {
		minbodyLength = len;
	}
	
	public int getBodyLength() {
		return bodyLength;
	}
	
	@Override
    public void resetNode(final EvolutionState state, final int thread) {
    	bodyLength = state.random[thread].nextInt(maxbodyLength - minbodyLength + 1) + minbodyLength;
    }
}
