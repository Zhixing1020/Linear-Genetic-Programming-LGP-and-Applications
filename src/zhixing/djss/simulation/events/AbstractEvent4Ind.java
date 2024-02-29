package zhixing.djss.simulation.events;

import java.util.List;

import yimei.jss.simulation.DecisionSituation;
import zhixing.djss.simulation.DynamicSimulation4Ind;
import zhixing.djss.simulation.Simulation4Ind;;

public abstract class AbstractEvent4Ind implements Comparable<AbstractEvent4Ind> {

    protected double time;

    public AbstractEvent4Ind(double time) {
        this.time = time;
    }

    public double getTime() {
        return time;
    }

    public abstract void trigger(Simulation4Ind simulation);

    public abstract void addDecisionSituation(Simulation4Ind simulation,
                                              List<DecisionSituation> situations,
                                              int minQueueLength);

    @Override
    public int compareTo(AbstractEvent4Ind other) {
        if (time < other.time)
            return -1;

        if (time > other.time)
            return 1;

        return 0;
    }
}
