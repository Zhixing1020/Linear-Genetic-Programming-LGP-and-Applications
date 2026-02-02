package zhixing.symbolicregression.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.data.DoubleData;

public class ReLu extends GPNode{

	public String toString() {
		return "relu";
	}

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
    public int expectedChildren() {
    	return 1;
    }

    public void eval(final EvolutionState state,
    		final int thread,
    		final GPData input,
    		final ADFStack stack,
    		final GPIndividual individual,
    		final Problem problem) {

		//double result;
		DoubleData rd = ((DoubleData)(input));

		children[0].eval(state,thread,input,stack,individual,problem);
		
		double result = Math.max(0, rd.value);
    }
    
    //=====================for Grammar LGP, zhixing 2022.12.27===================
    @Override
    public String toStringForHumans() {
		return "relu";
	}
}
