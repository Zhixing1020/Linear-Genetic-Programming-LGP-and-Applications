package zhixing.cpxInd.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import yimei.jss.gp.data.DoubleData;

public class IFLessEqual extends Branching{
	@Override
	public String toString() { return "IF<=#"+this.bodyLength; }

	/*
	public void checkConstraints(final EvolutionState state,
	final int tree,
	final GPIndividual typicalIndividual,
	final Parameter individualBase)
	{
	super.checkConstraints(state,tree,typicalIndividual,individualBase);
	if (children.length!=2)
	state.output.error("Incorrect number of children for node " + 
	toStringForError() + " at " +
	individualBase);
	}
	*/
	@Override
	public int expectedChildren() { return 2; }
	
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
		return "IfLessEq"+this.bodyLength;
	}
	
//	@Override
//    public void resetNode(final EvolutionState state, final int thread) {
//    	bodyLength = state.random[thread].nextInt(maxbodyLength)+1;
//    }
}
