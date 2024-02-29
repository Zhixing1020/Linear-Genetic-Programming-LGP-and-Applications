package zhixing.djss.dynamicoptimization.simulation.events;

import java.util.List;

import yimei.jss.simulation.DecisionSituation;
import zhixing.djss.dynamicoptimization.simulation.DOSimulation4Ind;
import zhixing.djss.simulation.DynamicSimulation4Ind;
import zhixing.djss.simulation.Simulation4Ind;
import zhixing.djss.simulation.events.AbstractEvent4Ind;

public abstract class AbstractEvent4DOInd extends AbstractEvent4Ind{

    public AbstractEvent4DOInd(double time) {
        super(time);
    }

    public double getTime() {
        return time;
    }
    
    @Override
    public int compareTo(AbstractEvent4Ind other) {
        if (time < other.getTime())
            return -1;

        if (time > other.getTime())
            return 1;

        return 0;
    }
}
