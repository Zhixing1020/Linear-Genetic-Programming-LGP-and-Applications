package zhixing.djss.dynamicoptimization.simulation.events;

import java.util.List;

import yimei.jss.simulation.DecisionSituation;
import zhixing.djss.dynamicoptimization.jobshop.Job4DO;
import zhixing.djss.dynamicoptimization.simulation.DOSimulation4Ind;
import zhixing.djss.simulation.DynamicSimulation4Ind;
import zhixing.djss.simulation.Simulation4Ind;
import zhixing.djss.simulation.events.AbstractEvent4Ind;

public class JobArrivalEvent4DOInd extends AbstractEvent4DOInd{

	protected Job4DO job;
	
	public JobArrivalEvent4DOInd(double time, Job4DO job) {
        super(time);
        this.job = job;
    }

    public JobArrivalEvent4DOInd(Job4DO job) {
        this(job.getArrivalTime(), job);
    }
	
	@Override
    public void trigger(Simulation4Ind simulation) {
        job.getOperation(0).setReadyTime(job.getReleaseTime());

        simulation.addEvent(
                new OperationVisitEvent4DOInd(job.getReleaseTime(), job.getOperation(0)));

        ((DOSimulation4Ind)simulation).generateJob();
    }
	
	@Override
    public void addDecisionSituation(Simulation4Ind simulation,
                                     List<DecisionSituation> situations,
                                     int minQueueLength) {
        trigger(simulation);
    }

    @Override
    public String toString() {
        return String.format("%.1f: job %d arrives.\n", time, job.getId());
    }

    @Override
    public int compareTo(AbstractEvent4Ind other) {
        if (time < other.getTime())
            return -1;

        if (time > other.getTime())
            return 1;

        if (other instanceof JobArrivalEvent4DOInd)
            return 0;

        return -1;
    }
}
