package zhixing.djss.simulation;

import java.io.FileWriter;
import java.util.PriorityQueue;

import yimei.jss.jobshop.Job;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.Simulation;
import yimei.jss.simulation.state.SystemState;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.individual.LGPIndividual4DJSS;
import zhixing.djss.simulation.events.AbstractEvent4Ind;
import zhixing.djss.simulation.events.ProcessFinishEvent4Ind;

public abstract class Simulation4Ind {

    protected CpxGPInterface4DJSS individual;
    protected AbstractRule rule;
    protected SystemState systemState;
    protected PriorityQueue<AbstractEvent4Ind> eventQueue;

    protected int numWorkCenters;
    protected int numJobsRecorded;
    protected int warmupJobs;
    protected int numJobsArrived;
    protected int throughput;
    
    //private int queueLength;//====debug

    public Simulation4Ind(CpxGPInterface4DJSS indi,
                      int numWorkCenters,
                      int numJobsRecorded,
                      int warmupJobs) {
        this.individual = indi;
        this.numWorkCenters = numWorkCenters;
        this.numJobsRecorded = numJobsRecorded;
        this.warmupJobs = warmupJobs;

        systemState = new SystemState();
        eventQueue = new PriorityQueue<>();
    }

    public CpxGPInterface4DJSS getIndividual() {
        return individual;
    }
    
    public AbstractRule getRule() {
        return rule;
    }

    public SystemState getSystemState() {
        return systemState;
    }

    public PriorityQueue<AbstractEvent4Ind> getEventQueue() {
        return eventQueue;
    }

    public void setIndividual(CpxGPInterface4DJSS indi) {
        this.individual = indi;
        this.rule = null;
        
        //queueLength = 0; //debug
    }
    
    public void setRule(AbstractRule rule) {
        this.rule = rule;
        this.individual = null;
    }

    public double getClockTime() {
        return systemState.getClockTime();
    }

    public void addEvent(AbstractEvent4Ind event) {
        eventQueue.add(event);
    }

    public void run() {
        while (!eventQueue.isEmpty() && throughput < numJobsRecorded) {
            AbstractEvent4Ind nextEvent = eventQueue.poll();

            systemState.setClockTime(nextEvent.getTime());
            nextEvent.trigger(this);
            
//            if(nextEvent instanceof ProcessFinishEvent4Ind){//========debug
//            	queueLength += ((ProcessFinishEvent4Ind)nextEvent).getQueueLength();
//            }
        }
    }

    public void rerun() {
        resetState();

//        long start = yimei.util.Timer.getCpuTime();
//        queueLength = 0;
        run();
//        long finish = yimei.util.Timer.getCpuTime();
//        double duration = (finish - start) / 1000000;
//        if(/*Math.random()<0.01*/ this.getIndividual() == null) 
//        	System.out.print("RULE simulation time: " + duration + " mseconds\n");
//        else{
//        	System.out.print("" + ((LGPIndividual)this.getIndividual()).getEffTreesLength() + " " + systemState.getJobsInSystem().size() + "  INDIVIDUAL simulation time: " + duration + " mseconds\n");
//        }
        
//        try{
//        	FileWriter output = new FileWriter("output.txt", true);
//        	output.write("" + queueLength/6000.0 +"\n");
//        	output.close();
//        }catch (Exception e) {
//            e.getStackTrace();
//        }
    }

    public void completeJob(Job job) {
        if (numJobsArrived > warmupJobs && job.getId() >= 0
                && job.getId() < numJobsRecorded + warmupJobs) {
            throughput++;

            systemState.addCompletedJob(job);
        }
        
        //=================2022.1.4   zhixing   normalization
//        if(numJobsArrived >= warmupJobs && numJobsArrived % warmupJobs == 0){
//        	systemState.determineMaxValue();
//        }
        //===================
        
        systemState.removeJobFromSystem(job);
    }

    public double makespan() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.getCompletionTime();
            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double meanFlowtime() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            value += job.flowTime();
        }

        return value / numJobsRecorded;
    }

    public double maxFlowtime() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.flowTime();
            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double meanWeightedFlowtime() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            value += job.weightedFlowTime();
        }

        return value / numJobsRecorded;
    }

    public double maxWeightedFlowtime() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.weightedFlowTime();
            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double meanTardiness() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            value += job.tardiness();
        }

        return value / numJobsRecorded;
    }

    public double maxTardiness() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.tardiness();

            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double meanWeightedTardiness() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            value += job.weightedTardiness();
        }

        return value / numJobsRecorded;
    }

    public double maxWeightedTardiness() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            double tmp = job.weightedTardiness();

            if (value < tmp)
                value = tmp;
        }

        return value;
    }

    public double propTardyJobs() {
        double value = 0.0;
        for (Job job : systemState.getJobsCompleted()) {
            if (job.getCompletionTime() > job.getDueDate())
                value ++;
        }

        return value / numJobsRecorded;
    }

    public double objectiveValue(Objective objective) {
        switch (objective) {
            case MAKESPAN:
                return makespan();
            case MEAN_FLOWTIME:
                return meanFlowtime();
            case MAX_FLOWTIME:
                return maxFlowtime();
            case MEAN_WEIGHTED_FLOWTIME:
                return meanWeightedFlowtime();
            case MAX_WEIGHTED_FLOWTIME:
                return maxWeightedFlowtime();
            case MEAN_TARDINESS:
                return meanTardiness();
            case MAX_TARDINESS:
                return maxTardiness();
            case MEAN_WEIGHTED_TARDINESS:
                return meanWeightedTardiness();
            case MAX_WEIGHTED_TARDINESS:
                return maxWeightedTardiness();
            case PROP_TARDY_JOBS:
                return propTardyJobs();
        }

        return -1.0;
    }

    public double workCenterUtilLevel(int idx) {
        return systemState.getWorkCenter(idx).getBusyTime() / getClockTime();
    }

    public String workCenterUtilLevelsToString() {
        String string = "[";
        for (int i = 0; i < systemState.getWorkCenters().size(); i++) {
            string += String.format("%.3f ", workCenterUtilLevel(i));
        }
        string += "]";

        return string;
    }

    public abstract void setup();
    public abstract void resetState();
    public abstract void reset();
    public abstract void rotateSeed();
    public abstract void generateJob();
    public abstract Simulation4Ind surrogate(int numWorkCenters, int numJobsRecorded,
                                int warmupJobs);
    public abstract Simulation4Ind surrogateBusy(int numWorkCenters, int numJobsRecorded,
                                         int warmupJobs);
}
