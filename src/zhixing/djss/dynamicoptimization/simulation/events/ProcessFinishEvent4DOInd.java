package zhixing.djss.dynamicoptimization.simulation.events;

import java.util.List;

import yimei.jss.jobshop.Job;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.Process;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.DecisionSituation;
import zhixing.djss.dynamicoptimization.jobshop.*;
import zhixing.djss.dynamicoptimization.simulation.state.SystemState4DO;
import zhixing.djss.simulation.Simulation4Ind;
import zhixing.djss.simulation.events.AbstractEvent4Ind;


public class ProcessFinishEvent4DOInd extends AbstractEvent4DOInd {
	private Process process;

    public ProcessFinishEvent4DOInd(double time, Process process) {
        super(time);
        this.process = process;
    }

    public ProcessFinishEvent4DOInd(Process process) {
        this(process.getFinishTime(), process);
    }

    public Process getProcess() {
        return process;
    }
	
	@Override
    public void trigger(Simulation4Ind simulation) {
        WorkCenter4DO workCenter = (WorkCenter4DO) process.getWorkCenter();

        if (!workCenter.getQueue().isEmpty()) {
            DecisionSituation decisionSituation =
                    new DecisionSituation(workCenter.getQueue(), workCenter,
                            simulation.getSystemState());
            
            Operation dispatchedOp = null;
            if(simulation.getIndividual() != null){
            	dispatchedOp =
                        simulation.getIndividual().priorOperation(decisionSituation);
            }
            else if(simulation.getRule() != null) {
            	dispatchedOp =
                        simulation.getRule().priorOperation(decisionSituation);
            }
            else{
            	System.out.print("No individual or rule for scheduling\n");
            	System.exit(1);
            }

            workCenter.removeFromQueue(dispatchedOp);
            Process nextP = new Process(workCenter, process.getMachineId(),
                    dispatchedOp, time);
            simulation.addEvent(new ProcessStartEvent4DOInd(nextP));
        }

        Operation nextOp = process.getOperation().getNext();

        if (nextOp == null) {
            Job4DO job = (Job4DO) process.getOperation().getJob();
            job.setCompletionTime(process.getFinishTime());
            simulation.completeJob(job);
        }
        else {
            simulation.addEvent(new OperationVisitEvent4DOInd(time, nextOp));
        }
        
       
    }
	
	@Override
    public void addDecisionSituation(Simulation4Ind simulation,
                                     List<DecisionSituation> situations,
                                     int minQueueLength) {
        WorkCenter workCenter = process.getWorkCenter();

        if (!workCenter.getQueue().isEmpty()) {
            DecisionSituation decisionSituation =
                    new DecisionSituation(workCenter.getQueue(), workCenter,
                            simulation.getSystemState());

            if (workCenter.getQueue().size() >= minQueueLength) {
                situations.add(decisionSituation.clone());
            }

            Operation dispatchedOp = null;
            if(simulation.getIndividual() != null){
            	dispatchedOp =
                        simulation.getIndividual().priorOperation(decisionSituation);
            }
            else if(simulation.getRule() != null) {
            	dispatchedOp =
                        simulation.getRule().priorOperation(decisionSituation);
            }
            else{
            	System.out.print("No individual or rule for scheduling\n");
            	System.exit(1);
            }

            workCenter.removeFromQueue(dispatchedOp);
            Process nextP = new Process(workCenter, process.getMachineId(),
                    dispatchedOp, time);
            simulation.addEvent(new ProcessStartEvent4DOInd(nextP));
        }

        Operation nextOp = process.getOperation().getNext();
        if (nextOp == null) {
            Job job = process.getOperation().getJob();
            job.setCompletionTime(process.getFinishTime());
            simulation.completeJob(job);
        }
        else {
            simulation.addEvent(new OperationVisitEvent4DOInd(time, nextOp));
        }
    }

    @Override
    public String toString() {
        return String.format("%.1f: job %d op %d finished on work center %d.\n",
                time,
                process.getOperation().getJob().getId(),
                process.getOperation().getId(),
                process.getWorkCenter().getId());
    }

    @Override
    public int compareTo(AbstractEvent4Ind other) {
        if (time < other.getTime())
            return -1;

        if (time > other.getTime())
            return 1;

        if (other instanceof ProcessFinishEvent4DOInd)
            return 0;

        return 1;
    }
}
