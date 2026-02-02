package zhixing.djss.individual.primitive;

import java.util.List;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import yimei.jss.jobshop.Operation;
import yimei.jss.gp.CalcPriorityProblem;
import yimei.jss.gp.data.DoubleData;
import yimei.jss.jobshop.Job;
import zhixing.cpxInd.individual.primitive.Args;
import zhixing.cpxInd.individual.primitive.Entity;

public class Job4Entity extends Entity implements EntityInterface4DJSS{
	
	final int attributeNumber = 5;
	final int nonlist_attrNumber = 4;
	
//	public Job4Entity(int id,
//            List<Operation> operations,
//            double arrivalTime,
//            double releaseTime,
//            double dueDate,
//            double weight) {
//		super(id, operations, arrivalTime, releaseTime, dueDate, weight);
//	}
//
//	 public Job4Entity(int id, List<Operation> operations) {
//	     this(id, operations, 0, 0, Double.POSITIVE_INFINITY, 1.0);
//	 }

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
		case 0: res = ((Job)obj).getArrivalTime(); break;
		case 1: res = ((Job)obj).getReleaseTime(); break;
		case 2: res = ((Job)obj).getDueDate(); break;
		case 3: res = ((Job)obj).getWeight(); break;
		case 4:{
			int list_ind = (int) Math.floor( args.getValue(index++) * ((Job)obj).getOperations().size() );
			
			if(list_ind + bias >= ((Job)obj).getOperations().size()) return LOOP_TERMINATE;
			
			Operation op = (Operation) ((Job)obj).getOperations().get( list_ind + bias );
			Operation4Entity opEntity = new Operation4Entity();
			res = opEntity.getAttributes(op, args, index, -1); //when Entity visit lists by "bias", the following bias must set to "-1" to force subsequent entity not to visit list
			break;
		}
		default: System.err.print("unknown attribute index when getting attributes from Entity in Job4Entity");
			System.exit(1);
		}
		
		return res;
	}

	@Override
	public String toString() {
		return "Job" + arguments.toString();
	}
    
    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
    	CalcPriorityProblem calcPrioProb = ((CalcPriorityProblem)problem);
        
        DoubleData data = ((DoubleData)input);
    	data.value = getAttributes(calcPrioProb.getOperation().getJob(), arguments, 0, 0);
    	
    	return;
    }
    
 
    public boolean equals(Object other) {
        if (other instanceof Job4Entity) {
        	
        	if(((Job4Entity)other).getArguments().equals(arguments)) {
        		return true;
        	}
        	
        }

        return false;
    }

	@Override
	public String toGraphvizString() {
		return toString();
	}
}
