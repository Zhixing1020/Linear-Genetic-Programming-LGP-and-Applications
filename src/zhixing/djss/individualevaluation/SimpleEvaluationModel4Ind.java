package zhixing.djss.individualevaluation;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Fitness;
import ec.util.Parameter;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.rule.AbstractRule;
import yimei.jss.ruleevaluation.AbstractEvaluationModel;
import yimei.jss.simulation.DynamicSimulation;
import yimei.jss.simulation.Simulation;
import yimei.jss.simulation.StaticSimulation;
import zhixing.cpxInd.algorithm.Grammar.individual.LGPIndividual4Grammar;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.individual.LGPIndividual4DJSS;
import zhixing.djss.jobshop.SchedulingSet4Ind;
import zhixing.djss.simulation.DynamicSimulation4Ind;
import zhixing.djss.simulation.Simulation4Ind;

public class SimpleEvaluationModel4Ind extends AbstractEvaluationModel {

	 /**
     * The starting seed of the simulation models.
     */
    public final static String P_SIM_SEED = "sim-seed";

    /**
     * Whether to rotate the simulation seed or not.
     */
    public final static String P_ROTATE_SIM_SEED = "rotate-sim-seed";

    public final static String P_SIM_MODELS = "sim-models";
    public final static String P_SIM_NUM_MACHINES = "num-machines";
    public final static String P_SIM_NUM_JOBS = "num-jobs";
    public final static String P_SIM_WARMUP_JOBS = "warmup-jobs";
    public final static String P_SIM_MIN_NUM_OPS = "min-num-ops";
    public final static String P_SIM_MAX_NUM_OPS = "max-num-ops";
    public final static String P_SIM_UTIL_LEVEL = "util-level";
    public final static String P_SIM_DUE_DATE_FACTOR = "due-date-factor";
    public final static String P_SIM_REPLICATIONS = "replications";
    
    protected SchedulingSet4Ind schedulingSet;
    protected long simSeed;
    protected boolean rotateSimSeed;
    
    public SchedulingSet4Ind getSchedulingSet() {
        return schedulingSet;
    }

    public long getSimSeed() {
        return simSeed;
    }
    
    public void setSimSeed(long sed){
    	simSeed = sed;
    }

    public boolean isRotateSimSeed() {
        return rotateSimSeed;
    }
	
    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);

        // Get the seed for the simulation.
        Parameter p = base.push(P_SIM_SEED);
        simSeed = state.parameters.getLongWithDefault(p, null, 0);

        // Get the simulation models.
        p = base.push(P_SIM_MODELS);
        int numSimModels = state.parameters.getIntWithDefault(p, null, 0);

        if (numSimModels == 0) {
            System.err.println("ERROR:");
            System.err.println("No simulation model is specified.");
            System.exit(1);
        }

        List<Simulation4Ind> trainSimulations = new ArrayList<>();
        List<Integer> replications = new ArrayList<>();
        for (int x = 0; x < numSimModels; x++) {
            // Read this simulation model
            Parameter b = base.push(P_SIM_MODELS).push("" + x);
            // Number of machines
            p = b.push(P_SIM_NUM_MACHINES);
            int numMachines = state.parameters.getIntWithDefault(p, null, 10);
            // Number of jobs
            p = b.push(P_SIM_NUM_JOBS);
            int numJobs = state.parameters.getIntWithDefault(p, null, 5000);
            // Number of warmup jobs
            p = b.push(P_SIM_WARMUP_JOBS);
            int warmupJobs = state.parameters.getIntWithDefault(p, null, 1000);
            // Min number of operations
            p = b.push(P_SIM_MIN_NUM_OPS);
            int minNumOps = state.parameters.getIntWithDefault(p, null, 2);
            // Max number of operations
            p = b.push(P_SIM_MAX_NUM_OPS);
            int maxNumOps = state.parameters.getIntWithDefault(p, null, numMachines);
            // Utilization level
            p = b.push(P_SIM_UTIL_LEVEL);
            double utilLevel = state.parameters.getDoubleWithDefault(p, null, 0.85);
            // Due date factor
            p = b.push(P_SIM_DUE_DATE_FACTOR);
            double dueDateFactor = state.parameters.getDoubleWithDefault(p, null, 1.5);
            // Number of replications
            p = b.push(P_SIM_REPLICATIONS);
            int rep = state.parameters.getIntWithDefault(p, null, 1);

            Simulation4Ind simulation = new DynamicSimulation4Ind(simSeed,
                    null, numMachines, numJobs, warmupJobs,
                    minNumOps, maxNumOps, utilLevel, dueDateFactor, false);

            trainSimulations.add(simulation);
            replications.add(new Integer(rep));
        }

        schedulingSet = new SchedulingSet4Ind(trainSimulations, replications, objectives);

        p = base.push(P_ROTATE_SIM_SEED);
        rotateSimSeed = state.parameters.getBoolean(p, null, false);
    }
    
    public void evaluate(List<Fitness> fitnesses,
            CpxGPInterface4DJSS indi,
            EvolutionState state) {

        Fitness fitness = fitnesses.get(0);
        
        //==================attention  how to initialize the registers: input and calculation
        //((LGPIndividual)indi).resetRegisters();
        indi.calcFitnessInd(fitness, state, schedulingSet, objectives);

        //indi.reflectSemantics((GPRuleEvolutionState) state);
    }

	@Override
	public void evaluate(Fitness fitness, AbstractRule rule, EvolutionState state) {
		//rule.calcFitness(fitness, state, schedulingSet, objectives);
		System.out.print("This model is not built for rule evaluation");
		System.exit(1);
	}

	@Override
	public boolean isRotatable() {
		return rotateSimSeed;
	}

	@Override
	public void rotate() {
		schedulingSet.rotateSeed(objectives);
	}
}
