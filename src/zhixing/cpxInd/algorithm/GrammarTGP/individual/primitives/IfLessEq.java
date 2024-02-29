package zhixing.cpxInd.algorithm.GrammarTGP.individual.primitives;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.data.DoubleData;

public class IfLessEq extends BranchingTGP  {
	public String toString() {
		return "if<=";
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
    	return 4;
    }

    public void eval(final EvolutionState state,
    		final int thread,
    		final GPData input,
    		final ADFStack stack,
    		final GPIndividual individual,
    		final Problem problem) {

        double result;
        DoubleData rd = ((DoubleData)(input));

		children[0].eval(state,thread,input,stack,individual,problem);
		result = rd.value;
		
		children[1].eval(state,thread,input,stack,individual,problem);
		
		if (result <= rd.value) {
			children[2].eval(state,thread,input,stack,individual,problem);
		}
		else {
			children[3].eval(state,thread,input,stack,individual,problem);
		}

    }
}
