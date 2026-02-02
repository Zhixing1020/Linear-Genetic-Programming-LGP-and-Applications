package zhixing.djss.individual.primitive;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.CalcPriorityProblem;
import yimei.jss.gp.data.DoubleData;
import yimei.jss.jobshop.Job;
//import yimei.jss.jobshop.Shop;
import yimei.jss.simulation.state.SystemState;
import yimei.jss.jobshop.WorkCenter;
import zhixing.cpxInd.individual.primitive.Args;
import zhixing.cpxInd.individual.primitive.Entity;

public class SystemState4Entity extends Entity implements EntityInterface4DJSS {

	//0: clock time, 1: list of work center, 2: list of jobs in the system
	final int attributeNumber = 3;
	final int nonlist_attrNumber = 1;
	
	@Override
	public double getAttributes(Object obj, Args args, int index, int bias) {
		double res = 0;
		int attr_ind = 0;
		
		if(index < args.getMaxLength()-2) { //"-2" because reading the list-based attributes at least requires two more indices.
			attr_ind = (int) Math.floor( args.getValue(index++) * attributeNumber );
		}
		else {
			attr_ind = (int) Math.floor( args.getValue(index++) * nonlist_attrNumber );
		}
		
		if(bias < 0) {
			attr_ind = (int) Math.floor( args.getValue(index++) * nonlist_attrNumber );
		}
		
		switch(attr_ind) {
		case 0: res = ((SystemState)obj).getClockTime(); break;
		case 1: {
			int list_ind = (int) Math.floor( args.getValue(index++) * ((SystemState)obj).getWorkCenters().size() );
			
			if(list_ind + bias >= ((SystemState)obj).getWorkCenters().size()) return LOOP_TERMINATE;
			
			WorkCenter workcenter = (WorkCenter) ((SystemState)obj).getWorkCenters().get( list_ind + bias);
			WorkCenter4Entity centerEntity = new WorkCenter4Entity();
			res = centerEntity.getAttributes(workcenter, args, index, -1);
			break;
		}
		case 2: {
			int list_ind = (int) Math.floor( args.getValue(index++) * ((SystemState)obj).getJobsInSystem().size() );
			
			if(list_ind + bias >= ((SystemState)obj).getJobsInSystem().size()) return LOOP_TERMINATE;
			
			Job job = (Job) ((SystemState)obj).getJobsInSystem().get( list_ind + bias);
			Job4Entity jobEntity = new Job4Entity();
			res = jobEntity.getAttributes(job, args, index, -1);
			break;
		}
		default: System.err.print("unknown attribute index when getting attributes from Entity in SystemStateEntity");
			System.exit(1);
		}
		
		return res;
	}
	
	@Override
	public String toString() {
		return "System"+arguments.toString();
	}

    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
    	CalcPriorityProblem calcPrioProb = ((CalcPriorityProblem)problem);
        
        DoubleData data = ((DoubleData)input);
    	data.value = getAttributes(calcPrioProb.getSystemState(), arguments, 0, 0);
    	
    	return;
    }
    
    public boolean equals(Object other) {
        if (other instanceof SystemState4Entity) {
        	if(((SystemState4Entity)other).getArguments().equals(arguments))
        		return true;
        }

        return false;
    }

    @Override
	public String toGraphvizString() {
		return toString();
	}
}
