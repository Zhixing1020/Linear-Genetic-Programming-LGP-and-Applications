package zhixing.cpxInd.individual.primitive;

import ec.EvolutionState;
import ec.gp.GPNode;

public interface ArgsInterface {
	//this interface defines the functions of varying Coefficients in GP operators
	public void varyNode(final EvolutionState state, final int thread, final Entity ref);
}
