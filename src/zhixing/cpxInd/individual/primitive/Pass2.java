package zhixing.cpxInd.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import yimei.jss.gp.data.DoubleData;

public class Pass2 extends yimei.jss.gp.function.Min{
	public String toString() {
		return "pass2";
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
    	return 2;
    }

    public void eval(final EvolutionState state,
    		final int thread,
    		final GPData input,
    		final ADFStack stack,
    		final GPIndividual individual,
    		final Problem problem) {

        double result;
        DoubleData rd = ((DoubleData)(input));

		children[1].eval(state,thread,input,stack,individual,problem);
		result = rd.value;

    }
    
  //=====================for Grammar LGP, zhixing 2022.12.27===================
    @Override
    public String toStringForHumans() {
		return "pass2";
	}
}
