package ec.app.tutorial4;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class Square extends GPNode {
	public String toString() { return "square"; }

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
	    public int expectedChildren() { return 1; }

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
	        rd.x = rd.x*rd.x;
	        
	        if(Math.abs(rd.x) >= 1e6) {
	        	rd.x = 1e6*(rd.x+0.001)/(rd.x+0.001);
	        }
	        
	        }
}
