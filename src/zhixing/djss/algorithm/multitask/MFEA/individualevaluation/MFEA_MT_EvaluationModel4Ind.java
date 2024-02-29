package zhixing.djss.algorithm.multitask.MFEA.individualevaluation;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Fitness;
import ec.util.Parameter;
import yimei.jss.jobshop.Objective;
import zhixing.djss.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA4DJSS;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.individualevaluation.SimpleEvaluationModel4Ind;
import zhixing.djss.jobshop.SchedulingSet4Ind;
import zhixing.djss.simulation.Simulation4Ind;

public class MFEA_MT_EvaluationModel4Ind extends SimpleEvaluationModel4Ind {
	
	private final static String P_SIM_OUTPUTREG = "output_register";
	
	protected List<Integer> outputReg = new ArrayList<>();
	protected int numSimModels = 0; 
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		
		// Get the simulation models.
		Parameter p = base.push(P_SIM_MODELS);
		numSimModels = state.parameters.getIntWithDefault(p, null, 0);

        if (numSimModels == 0) {
            System.err.println("ERROR:");
            System.err.println("No simulation model is specified.");
            System.exit(1);
        }
        
        for (int x = 0; x < numSimModels; x++){
        	// Read this simulation model
            Parameter b = base.push(P_SIM_MODELS).push("" + x);
            
            p = b.push(P_SIM_OUTPUTREG);
            int r = state.parameters.getIntWithDefault(p, null, 0);
            if(r < 0 ){
            	System.err.println("ERROR:");
                System.err.println("output register must be >= 0.");
                System.exit(1);
            }
            outputReg.add(r);
        }
	}
	
    public void evaluate(List<Fitness> fitnesses,
            LGPIndividual_MFEA4DJSS indi,
            int taskind,
            EvolutionState state) {

//    	List<Simulation4Ind> simulations = new ArrayList<>();
//        List<Integer> replications = new ArrayList<>();
//        List<Objective> objective_list = new ArrayList<>();
//        
//        simulations.add(schedulingSet.getSimulations().get(taskind));
//        replications.add(schedulingSet.getReplications().get(taskind));
//        objective_list.add(objectives.get(taskind % objectives.size()));
//        
//        SchedulingSet4Ind schedulingSet_private = new SchedulingSet4Ind(simulations, replications, objective_list);
        
        Fitness fitness = fitnesses.get(0);
        
        indi.setOutputRegister(outputReg);
        
        //indi.calcFitnessInd4MT(fitness, state, schedulingSet_private, objective_list, taskind);
        indi.calcFitnessInd4MT(fitness, state, schedulingSet, objectives, taskind);

    }
}
