package zhixing.djss.dynamicoptimization.simulation.state;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import yimei.jss.jobshop.Job;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.state.SystemState;
import zhixing.djss.dynamicoptimization.jobshop.Job4DO;
import zhixing.djss.dynamicoptimization.jobshop.WorkCenter4DO;

public class SystemState4DO extends SystemState{
	
	private double energyPrice;
	private double userReactionFactor;
	
	private List<Job> jobsCompletedStage;  //the completed jobs in a certain stage
	
	public SystemState4DO(double clockTime, List<WorkCenter> workCenters,
            List<Job> jobsInSystem, List<Job> jobsCompleted, double energyprice, double reactionfactor) {
		super(clockTime, workCenters, jobsInSystem, jobsCompleted);
		this.energyPrice = energyprice;
		this.userReactionFactor = reactionfactor;

	}

	public SystemState4DO(double clockTime) {
		this(clockTime, new ArrayList<>(), new LinkedList<>(), new ArrayList<>(), 1e-2, 1);
	}

	public SystemState4DO() {
		this(0.0);
	}
	
	public double getEnergyPrice() {
		return energyPrice;
	}
	public void setEnergyPrice(double value) {
		energyPrice = value;
	}
	public double getUserReactiveFactor() {
		return userReactionFactor;
	}
	public void setUserReactiveFactor(double val) {
		userReactionFactor = val;
	}
	
	public List<Job> getJobsCompleted4Stage(){
		return jobsCompletedStage;
	}
	public void resetCompletedJobs4Stage() {
		jobsCompletedStage.clear();
	}
	public void addCompletedJob4Stage(Job4DO job) {
        jobsCompletedStage.add(job);
    }
	
	@Override
    public SystemState4DO clone() {
        List<WorkCenter> clonedWCs = new ArrayList<>();
        for (WorkCenter wc : getWorkCenters()) {
            clonedWCs.add(wc.clone());
        }

        return new SystemState4DO(getClockTime(), clonedWCs,
                new LinkedList<>(), new ArrayList<>(), energyPrice, userReactionFactor);

    }
	
	public void resetEnergyCost() {
		for (WorkCenter wc : getWorkCenters()) {
            ((WorkCenter4DO)wc).resetEnergyCost();
        }
	}
}
