package zhixing.djss.dynamicoptimization.simulation.events;

import java.util.List;

import yimei.jss.jobshop.Machine;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.Process;
import yimei.jss.simulation.DecisionSituation;
import zhixing.djss.dynamicoptimization.jobshop.WorkCenter4DO;
import zhixing.djss.dynamicoptimization.simulation.DOSimulation4Ind;
import zhixing.djss.simulation.Simulation4Ind;
import zhixing.djss.simulation.events.AbstractEvent4Ind;


public class OperationVisitEvent4DOInd extends AbstractEvent4DOInd{

	protected Operation operation;

    public OperationVisitEvent4DOInd(double time, Operation operation) {
        super(time);
        this.operation = operation;
    }

    public OperationVisitEvent4DOInd(Operation operation) {
        this(operation.getReadyTime(), operation);
    }
	
	@Override
    public void trigger(Simulation4Ind simulation) {
        operation.setReadyTime(time);

        WorkCenter4DO workCenter = (WorkCenter4DO) operation.getWorkCenter();
        Machine earliestMachine = workCenter.earliestReadyMachine();

        if (earliestMachine.getReadyTime() > time) {
            workCenter.addToQueue(operation);
        }
        else {
            Process p = new Process(workCenter, earliestMachine.getId(), operation, time);
            simulation.addEvent(new ProcessStartEvent4DOInd(p));
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

        if (other instanceof JobArrivalEvent4DOInd)
            return 1;

        if (other instanceof OperationVisitEvent4DOInd)
            return 0;

        return -1;
    }
}
