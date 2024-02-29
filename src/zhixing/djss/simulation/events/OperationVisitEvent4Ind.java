package zhixing.djss.simulation.events;

import java.util.List;

import yimei.jss.jobshop.Machine;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.Process;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.DecisionSituation;
import yimei.jss.simulation.DynamicSimulation;
import yimei.jss.simulation.Simulation;
import yimei.jss.simulation.event.AbstractEvent;
import zhixing.djss.simulation.DynamicSimulation4Ind;
import zhixing.djss.simulation.Simulation4Ind;
import zhixing.djss.simulation.events.OperationVisitEvent4Ind;
import zhixing.djss.simulation.events.ProcessStartEvent4Ind;

public class OperationVisitEvent4Ind extends AbstractEvent4Ind {
	protected Operation operation;

    public OperationVisitEvent4Ind(double time, Operation operation) {
        super(time);
        this.operation = operation;
    }

    public OperationVisitEvent4Ind(Operation operation) {
        this(operation.getReadyTime(), operation);
    }
    
    @Override
    public void trigger(Simulation4Ind simulation) {
        operation.setReadyTime(time);

        WorkCenter workCenter = operation.getWorkCenter();
        Machine earliestMachine = workCenter.earliestReadyMachine();

        if (earliestMachine.getReadyTime() > time) {
            workCenter.addToQueue(operation);
        }
        else {
            Process p = new Process(workCenter, earliestMachine.getId(), operation, time);
            simulation.addEvent(new ProcessStartEvent4Ind(p));
        }
    }

    @Override
    public void addDecisionSituation(Simulation4Ind simulation,
                                     List<DecisionSituation> situations,
                                     int minQueueLength) {
        trigger(simulation);
    }

    @Override
    public String toString() {
        return String.format("%.1f: job %d op %d visits.\n",
                time, operation.getJob().getId(), operation.getId());
    }

    @Override
    public int compareTo(AbstractEvent4Ind other) {
        if (time < other.getTime())
            return -1;

        if (time > other.getTime())
            return 1;

        if (other instanceof JobArrivalEvent4Ind)
            return 1;

        if (other instanceof OperationVisitEvent4Ind)
            return 0;

        return -1;
    }
}
