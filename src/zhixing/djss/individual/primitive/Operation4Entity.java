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
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.jobshop.Operation;
import zhixing.cpxInd.individual.primitive.Args;
import zhixing.cpxInd.individual.primitive.Entity;

public class Operation4Entity extends Entity implements EntityInterface4DJSS{

	//0:processing time, 1:ready time, 2:flow due date, 3:priority, 4:Job
	final int attributeNumber = 5;
	final int nonlist_attrNumber = 4;
	
//	public Operation4Entity(Job job, int id, double procTime, WorkCenter workCenter) {
//		super(job, id, procTime, workCenter);
//	}

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
		
		switch(attr_ind) {
		case 0: res = ((Operation)obj).getProcTime(); break;
		case 1: res = ((Operation)obj).getReadyTime(); break;
		case 2: res = ((Operation)obj).getFlowDueDate(); break;
		case 3: res = ((Operation)obj).getPriority(); break;
		case 4:{
			Job job = ((Operation)obj).getJob();
			Job4Entity jobEntity = new Job4Entity();
			res = jobEntity.getAttributes(job, args, index, bias);
			break;
		}
		default: System.err.print("unknown attribute index when getting attributes from Entity in Operation4Entity");
			System.exit(1);
		}
		
		return res;
	}

	@Override
	public String toString() {
		return "Operation"+arguments;
	}

    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
    	CalcPriorityProblem calcPrioProb = ((CalcPriorityProblem)problem);
        
        DoubleData data = ((DoubleData)input);
    	data.value = getAttributes(calcPrioProb.getOperation(), arguments, 0, 0);
    	
    	return;
    }
    
    public boolean equals(Object other) {
        if (other instanceof Operation4Entity) {
        	if(((Operation4Entity)other).getArguments().equals(arguments))
        		return true;
        }

        return false;
    }
    
    @Override
	public String toGraphvizString() {
		return toString();
	}
}
