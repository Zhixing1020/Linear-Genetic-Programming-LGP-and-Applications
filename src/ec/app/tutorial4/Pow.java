/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.tutorial4;
import ec.*;
import ec.gp.*;
import ec.util.*;

public class Pow extends GPNode
    {
    public String toString() { return "pow"; }

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
    public int expectedChildren() { return 2; }

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
        result = rd.x;

        children[1].eval(state,thread,input,stack,individual,problem);
        if(result != 0 && Math.abs(rd.x) <= 10) {
        	rd.x = Math.pow(Math.abs(result),rd.x);
        }
        	
        else if(result == 0) {
        	rd.x = 1.;
        }
        else {
			rd.x = rd.x + result + 1e6;
		}
        
        if(Math.abs(rd.x) >= 1e6) {
        	rd.x = 1e6*(rd.x+0.001)/(rd.x+0.001);
        }
        
        }
    }

