package zhixing.djss.algorithm.multitask.MultipopMultioutreg.individualevaluation;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Fitness;
import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.jobshop.Objective;
import zhixing.djss.algorithm.multitask.MFEA.individualevaluation.MFEA_MT_EvaluationModel4Ind;
import zhixing.djss.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO4DJSS;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.individualevaluation.SimpleEvaluationModel4Ind;
import zhixing.djss.jobshop.SchedulingSet4Ind;
import zhixing.djss.simulation.Simulation4Ind;

public class MPMO_MT_EvaluationModel4Ind extends MFEA_MT_EvaluationModel4Ind{
	
	public void evaluate(List<Fitness> fitnesses,
            LGPIndividual_MPMO4DJSS indi,
            int subpopulation,
            EvolutionState state) {
        
        Fitness fitness = fitnesses.get(0);
        MultiObjectiveFitness f = (MultiObjectiveFitness) fitness;
        
        indi.setOutputRegister(outputReg);
        
        int numobj = f.getNumObjectives();
        double[] tmp_fitnesses = new double[numobj];
        
      for(int tf = 0;tf<f.getNumObjectives();tf++){ 
        	if(f.getNumObjectives() > 1){
        		indi.setCurrentOutputRegister(tf);
        		tmp_fitnesses[tf] = indi.calcFitnessInd4OneTask(state, schedulingSet, objectives, tf);
        	}
        	else{
        		indi.setCurrentOutputRegister(indi.skillFactor);
        		tmp_fitnesses[tf] = indi.calcFitnessInd4OneTask(state, schedulingSet, objectives, indi.skillFactor);
        	}

    	}
        
//        if(numobj > 1){ //randomly evaluate one task
//        	int tf = state.random[0].nextInt(numobj);
//        	for(int t = 0;t<numobj;t++){
//        		if(t == tf){
//        			indi.setCurrentOutputRegister(tf);
//            		tmp_fitnesses[t] = indi.calcFitnessInd4OneTask(state, schedulingSet, objectives, tf);
//        		}
//        		else{
//        			tmp_fitnesses[t] = LGPIndividual_MPMO.INF_SCALAR_RANK;
//        		}
//        	}
//    		
//    	}
//    	else{
//    		indi.setCurrentOutputRegister(indi.skillFactor);
//    		tmp_fitnesses[0] = indi.calcFitnessInd4OneTask(state, schedulingSet, objectives, indi.skillFactor);
//    	}
        
        f.setObjectives(state, tmp_fitnesses);
        
        //reset the effective status of all instructions
      	indi.updateStatus();

        //indi.reflectSemantics((GPRuleEvolutionState) state);
    }
}
