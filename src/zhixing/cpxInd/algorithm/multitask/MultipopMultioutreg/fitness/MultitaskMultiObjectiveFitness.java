package zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.fitness;

import ec.Fitness;
import ec.multiobjective.MultiObjectiveFitness;

public class MultitaskMultiObjectiveFitness extends MultiObjectiveFitness{
	
	public boolean betterThan(MultitaskMultiObjectiveFitness other, int taskid){
		boolean abeatsb = false;
		
		double f1, f2;
		boolean max_flag = false;
		if(objectives.length>1){
			f1 = objectives[taskid];
			max_flag = maximize[taskid];
		}
		else{
			f1 = objectives[0];
			max_flag = maximize[0];
		}
			
		
		if(other.objectives.length > 1){
			f2 = other.objectives[taskid];
		}
		else{
			f2 = other.objectives[0];
		}

        if (max_flag)
        {
        if (f1 > f2)
            abeatsb = true;
        else if (f1 < f2)
            return false;
        }
        else
        {
        if (f1 < f2)
            abeatsb = true;
        else if (f1 > f2)
            return false;
        }
        return abeatsb;
	}
	
	public double getFitnessByTask(int taskid){
		if(objectives.length>1){
			return objectives[taskid];
		}
		else{
			return objectives[0];
		}
	}
}
