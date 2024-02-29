package zhixing.cpxInd.algorithm.multitask.MFEA.evaluator;

import java.util.Comparator;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.simple.SimpleEvaluator;
import ec.util.Parameter;

public abstract class MFEA_Evaluator extends SimpleEvaluator {

public final static String P_NUMBER_TASK = "num_task";
	
	protected int num_task;
	
	public int getNumTasks(){
		return num_task;
	}
	
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		Parameter p;
		
		//get the number of jobs 
		num_task = state.parameters.getIntWithDefault(base.push(P_NUMBER_TASK), null, 1);
		if(num_task < 1){
			System.out.println("the number of tasks for multittask learning is less than 1");
			System.exit(1);
		}
	}
	
	abstract public void identifySkillFactor(EvolutionState state);
	
	abstract public void evaluatePopulation_MFEA(EvolutionState state, Population pop);
	
	public class cmp implements Comparator<Individual>{
		@Override
		public int compare(Individual i1, Individual i2){
			if(i1.fitness.betterThan(i2.fitness))
				return -1;
			if(i2.fitness.betterThan(i1.fitness))
				return 1;
			else
				return 0;
		}
	}
}
