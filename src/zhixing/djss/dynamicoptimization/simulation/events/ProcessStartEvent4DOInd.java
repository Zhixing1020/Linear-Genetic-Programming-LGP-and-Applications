package zhixing.djss.dynamicoptimization.simulation.events;

import java.util.List;

import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.Process;
import yimei.jss.simulation.DecisionSituation;
import zhixing.djss.dynamicoptimization.jobshop.Job4DO;
import zhixing.djss.dynamicoptimization.jobshop.WorkCenter4DO;
import zhixing.djss.dynamicoptimization.simulation.state.SystemState4DO;
import zhixing.djss.simulation.Simulation4Ind;
import zhixing.djss.simulation.events.AbstractEvent4Ind;

public class ProcessStartEvent4DOInd extends AbstractEvent4DOInd {

	private Process process;
	
	public ProcessStartEvent4DOInd(double time, Process process) {
		super(time);
        this.process = process;
	}
	
	public ProcessStartEvent4DOInd(Process process) {
		this(process.getStartTime(), process);
	}
	
	@Override
    public void trigger(Simulation4Ind simulation) {
        WorkCenter4DO workCenter = (WorkCenter4DO) process.getWorkCenter();
        SystemState4DO systemState = (SystemState4DO) simulation.getSystemState();
        
        //set machine's idle energy consumption
        double machineWaitTime = Math.max(0,  time - workCenter.getReadyTime());
        workCenter.incrementEnergyCost(machineWaitTime, systemState.getEnergyPrice(), null, true);
        
        workCenter.setMachineReadyTime(
                process.getMachineId(), process.getFinishTime());
        workCenter.incrementBusyTime(process.getDuration());
        
        //set machine's running energy consumption
        workCenter.incrementEnergyCost(process.getDuration(), systemState.getEnergyPrice(), (Job4DO) process.getOperation().getJob(), false);
        
        //set operation's waitting time
        Operation op = process.getOperation();
        ((Job4DO)op.getJob()).incrementResponseTime(Math.max(0, time - op.getReadyTime())*systemState.getUserReactiveFactor());

        simulation.addEvent(
                new ProcessFinishEvent4DOInd(process.getFinishTime(), process));
    }
	
	@Override
    public void addDecisionSituation(Simulation4Ind simulation,
                                     List<DecisionSituation> situations,
                                     int minQueueLength) {
        trigger(simulation);
    }

    @Override
    public String toString() {
        return String.format("%.1f: job %d op %d started on work center %d.\n",
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

        if (other instanceof ProcessStartEvent4DOInd)
            return 0;

        if (other instanceof ProcessFinishEvent4DOInd)
            return -1;

        return 1;
    }
}
