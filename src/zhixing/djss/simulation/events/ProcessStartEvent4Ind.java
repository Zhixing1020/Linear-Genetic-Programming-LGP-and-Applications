package zhixing.djss.simulation.events;

import java.util.List;

import yimei.jss.jobshop.Process;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.DecisionSituation;
import yimei.jss.simulation.Simulation;
import zhixing.djss.simulation.DynamicSimulation4Ind;
import zhixing.djss.simulation.Simulation4Ind;
import zhixing.djss.simulation.events.*;

public class ProcessStartEvent4Ind extends AbstractEvent4Ind {
	private Process process;

    public ProcessStartEvent4Ind(double time, Process process) {
        super(time);
        this.process = process;
    }

    public ProcessStartEvent4Ind(Process process) {
        this(process.getStartTime(), process);
    }

    public Process getProcess() {
        return process;
    }

    @Override
    public void trigger(Simulation4Ind simulation) {
        WorkCenter workCenter = process.getWorkCenter();
        workCenter.setMachineReadyTime(
                process.getMachineId(), process.getFinishTime());
        workCenter.incrementBusyTime(process.getDuration());

        simulation.addEvent(
                new ProcessFinishEvent4Ind(process.getFinishTime(), process));
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

        if (other instanceof ProcessStartEvent4Ind)
            return 0;

        if (other instanceof ProcessFinishEvent4Ind)
            return -1;

        return 1;
    }
}
