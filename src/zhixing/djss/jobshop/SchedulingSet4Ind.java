package zhixing.djss.jobshop;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.Scenario;
import yimei.jss.rule.AbstractRule;
import zhixing.djss.dynamicoptimization.simulation.DOSimulation4Ind;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.simulation.DynamicSimulation4Ind;
import zhixing.djss.simulation.Simulation4Ind;

public class SchedulingSet4Ind {
	private List<Simulation4Ind> simulations;
    private List<Integer> replications;
    private RealMatrix objectiveLowerBoundMtx;

    public SchedulingSet4Ind(List<Simulation4Ind> simulations,
                         List<Integer> replications,
                         List<Objective> objectives) {
        this.simulations = simulations;
        this.replications = replications;
        createObjectiveLowerBoundMatrix(objectives);
        lowerBoundsFromBenchmarkRule(objectives);
    }

    public List<Simulation4Ind> getSimulations() {
        return simulations;
    }

    public List<Integer> getReplications() {
        return replications;
    }

    public RealMatrix getObjectiveLowerBoundMtx() {
        return objectiveLowerBoundMtx;
    }

    public double getObjectiveLowerBound(int row, int col) {
        return objectiveLowerBoundMtx.getEntry(row, col);
    }

    public void setReplications(List<Integer> replications) {
        this.replications = replications;
    }

    public void setIndividual(CpxGPInterface4DJSS indi) {
        for (Simulation4Ind simulation : simulations) {
            simulation.setIndividual(indi);
        }
    }

    public void rotateSeed(List<Objective> objectives) {
        for (Simulation4Ind simulation : simulations) {
            simulation.rotateSeed();
        }

        lowerBoundsFromBenchmarkRule(objectives);
    }

//    public void reset() {
//        for (DynamicSimulation simulation : simulations) {
//            simulation.reset();
//        }
//    }

    private void createObjectiveLowerBoundMatrix(List<Objective> objectives) {
        int rows = objectives.size();
        int cols = 0;
        for (int rep : replications)
            cols += rep;

        objectiveLowerBoundMtx = new Array2DRowRealMatrix(rows, cols);
    }

    private void lowerBoundsFromBenchmarkRule(List<Objective> objectives) {
        for (int i = 0; i < objectives.size(); i++) {
            Objective objective = objectives.get(i);
            AbstractRule benchmarkRule = objective.benchmarkRule();

            int col = 0;
          //for some objectives, there are no pre-defined benchmark rules.
            //so, assign 1.0 (no normalization) to the lowerbound Matrix 
            if(benchmarkRule == null){
            	for(int j = 0;j<simulations.size(); j++){
            		objectiveLowerBoundMtx.setEntry(i, col, 1.0);
                    col ++;
                    for (int k = 1; k < replications.get(j); k++){
                    	 objectiveLowerBoundMtx.setEntry(i, col, 1.0);
                         col ++;
                	}
            	}
            }
            else{
            	for (int j = 0; j < simulations.size(); j++) {
                    Simulation4Ind simulation = simulations.get(j);
                    simulation.setRule(benchmarkRule);
                    simulation.rerun();
//                    System.out.println(simulation.workCenterUtilLevelsToString());
                    double value = simulation.objectiveValue(objective);
                    objectiveLowerBoundMtx.setEntry(i, col, value);
                    col ++;

                    for (int k = 1; k < replications.get(j); k++) {
                        simulation.rerun();
//                        System.out.println(simulation.workCenterUtilLevelsToString());
                        value = simulation.objectiveValue(objective);
                        objectiveLowerBoundMtx.setEntry(i, col, value);
                        col ++;
                    }

                    simulation.reset();
                }
            }
            
        }
    }

    public SchedulingSet4Ind surrogate(int numWorkCenters, int numJobsRecorded,
                                   int warmupJobs, List<Objective> objectives) {
        List<Simulation4Ind> surrogateSimulations = new ArrayList<>();
        List<Integer> surrogateReplications = new ArrayList<>();

        for (int i = 0; i < simulations.size(); i++) {
            surrogateSimulations.add(
                    simulations.get(i).surrogate(
                    numWorkCenters, numJobsRecorded, warmupJobs));
            surrogateReplications.add(1);
        }

        return new SchedulingSet4Ind(surrogateSimulations,
                surrogateReplications, objectives);
    }

    public SchedulingSet4Ind surrogateBusy(int numWorkCenters, int numJobsRecorded,
                                   int warmupJobs, List<Objective> objectives) {
        List<Simulation4Ind> surrogateSimulations = new ArrayList<>();
        List<Integer> surrogateReplications = new ArrayList<>();

        for (int i = 0; i < simulations.size(); i++) {
            surrogateSimulations.add(
                    simulations.get(i).surrogateBusy(
                            numWorkCenters, numJobsRecorded, warmupJobs));
            surrogateReplications.add(1);
        }

        return new SchedulingSet4Ind(surrogateSimulations,
                surrogateReplications, objectives);
    }

    public static SchedulingSet4Ind dynamicFullSet(long simSeed,
                                               double utilLevel,
                                               double dueDateFactor,
                                               List<Objective> objectives,
                                               int reps) {
        List<Simulation4Ind> simulations = new ArrayList<>();
        simulations.add(
                DynamicSimulation4Ind.standardFull(simSeed, null, 10, 5000, 1000,
                        utilLevel, dueDateFactor));//=============2021.12.31   zhixing  debug  change the numJobsRecorded into 5000, rather than 4000
        List<Integer> replications = new ArrayList<>();
        replications.add(reps);

        return new SchedulingSet4Ind(simulations, replications, objectives);
    }

    public static SchedulingSet4Ind dynamicMissingSet(long simSeed,
                                                  double utilLevel,
                                                  double dueDateFactor,
                                                  List<Objective> objectives,
                                                  int reps) {
        List<Simulation4Ind> simulations = new ArrayList<>();
        simulations.add(
                DynamicSimulation4Ind.standardMissing(simSeed, null, 10, 5000, 1000,
                        utilLevel, dueDateFactor));
        List<Integer> replications = new ArrayList<>();
        replications.add(reps);

        return new SchedulingSet4Ind(simulations, replications, objectives);
    }
    
    public static SchedulingSet4Ind dynamicDynOptimSetDefault(long simSeed,
            double utilLevel,
            double dueDateFactor,
            List<Objective> objectives,
            int reps) {
		List<Simulation4Ind> simulations = new ArrayList<>();
		simulations.add(
		DOSimulation4Ind.standardMissing(simSeed, null, 10, 5000, 1000,
		utilLevel, dueDateFactor, 10, "default"));
		List<Integer> replications = new ArrayList<>();
		replications.add(reps);

		return new SchedulingSet4Ind(simulations, replications, objectives);
    }
    
    public static SchedulingSet4Ind dynamicDynOptimSetEnergy(long simSeed,
                                                  double utilLevel,
                                                  double dueDateFactor,
                                                  List<Objective> objectives,
                                                  int reps) {
    	List<Simulation4Ind> simulations = new ArrayList<>();
        simulations.add(
                DOSimulation4Ind.standardMissing(simSeed, null, 10, 5000, 1000,
                        utilLevel, dueDateFactor, 10, "energy"));
        List<Integer> replications = new ArrayList<>();
        replications.add(reps);

        return new SchedulingSet4Ind(simulations, replications, objectives);
    }
    
    public static SchedulingSet4Ind dynamicDynOptimSetAdvance(long simSeed,
            double utilLevel,
            double dueDateFactor,
            List<Objective> objectives,
            int reps) {
		List<Simulation4Ind> simulations = new ArrayList<>();
		simulations.add(
		DOSimulation4Ind.standardMissing(simSeed, null, 10, 5000, 1000,
		utilLevel, dueDateFactor, 10, "advance"));
		List<Integer> replications = new ArrayList<>();
		replications.add(reps);
		
		return new SchedulingSet4Ind(simulations, replications, objectives);
	}

    public static SchedulingSet4Ind generateSet(long simSeed,
                                            String scenario,
                                            String setName,
                                            List<Objective> objectives,
                                            int replications) {
        if (scenario.equals(Scenario.DYNAMIC_JOB_SHOP.getName())) {
            String[] parameters = setName.split("-");
            double utilLevel = Double.valueOf(parameters[1]);
            double dueDateFactor = Double.valueOf(parameters[2]);

            if (parameters[0].equals("missing")) {
                return SchedulingSet4Ind.dynamicMissingSet(simSeed, utilLevel, dueDateFactor, objectives, replications);
            }
            else if (parameters[0].equals("full")) {
                return SchedulingSet4Ind.dynamicFullSet(simSeed, utilLevel, dueDateFactor, objectives, replications);
            }
            else if (parameters[0].equals("default")) {
            	return SchedulingSet4Ind.dynamicDynOptimSetDefault(simSeed, utilLevel, dueDateFactor, objectives, replications);
            }
            else if (parameters[0].equals("energy")) {
            	return SchedulingSet4Ind.dynamicDynOptimSetEnergy(simSeed, utilLevel, dueDateFactor, objectives, replications);
            }
            else if (parameters[0].equals("advance")) {
            	return SchedulingSet4Ind.dynamicDynOptimSetAdvance(simSeed, utilLevel, dueDateFactor, objectives, replications);
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }
}
