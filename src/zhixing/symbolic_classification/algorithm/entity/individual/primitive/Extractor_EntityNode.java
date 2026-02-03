package zhixing.symbolic_classification.algorithm.entity.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import yimei.jss.gp.data.DoubleData;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.symbolic_classification.individual.primitive.EntityInterface4Class;

public abstract class Extractor_EntityNode extends Entity implements EntityInterface4Class{

	protected double fitness = 0;
	
	public double getEntityFitness() {
		return fitness;
	}
	
	public void setEntityFitness(double fit) {
		fitness = fit;
	}
	
	@Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
    	DoubleData data = ((DoubleData)input);
    	
    	data.value = getAttributes(problem, arguments, 0, 0);
    	
    	return;
    }
	
}
