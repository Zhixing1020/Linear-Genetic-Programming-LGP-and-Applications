package zhixing.djss.individual.primitive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.CalcPriorityProblem;
import yimei.jss.gp.data.DoubleData;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.state.SystemState;
import zhixing.cpxInd.individual.primitive.Args;
import zhixing.cpxInd.individual.primitive.Entity;

public class WorkCenter4Entity extends Entity implements EntityInterface4DJSS {

	//0: operation queue, 1: list of machine ready time
	final int attributeNumber = 2;
	final int nonlist_attrNumber = 0;
	
//	public WorkCenter4Entity(int id, int numMachines,
//            LinkedList<Operation> queue,
//            List<Double> machineReadyTimes,
//            double workInQueue, double busyTime) {
//		super(id, numMachines, queue, machineReadyTimes, workInQueue, busyTime);
//	}
//	
//	public WorkCenter4Entity(int id, int numMachines) {
//		this(id, numMachines, new LinkedList<>(),
//	      new ArrayList<>(Collections.nCopies(numMachines, 0.0)),
//	      0.0, 0.0);
//	}
//	
//	public WorkCenter4Entity(int id) {
//		this(id, 1);
//	}

	@Override
	public double getAttributes(Object obj, Args args, int index, int bias) {
		double res = 0;
		int attr_ind = 0;
		
		if(index < args.getMaxLength()-2) { //"-2" because reading the list-based attributes at least requires two more indices.
			attr_ind = (int) Math.floor( args.getValue(index++) * attributeNumber );
		}
		else {
//			attr_ind = (int) Math.floor( args.getValue(index++) * nonlist_attrNumber );
			return res;
		}
		
		if(bias < 0) {
			return res;
		}
		
		switch(attr_ind) {
		case 0: {
			if(((WorkCenter)obj).getQueue().size() > 0) {
				int list_ind = (int) Math.floor( args.getValue(index++) * ((WorkCenter)obj).getQueue().size() );
				
				if(list_ind + bias >= ((WorkCenter)obj).getQueue().size()) return LOOP_TERMINATE;
				
				Operation op = (Operation) ((WorkCenter)obj).getQueue().get( list_ind + bias);
				Operation4Entity opEntity = new Operation4Entity();
				res = opEntity.getAttributes(op, args, index, -1);
			}
			break;
		}
		case 1: {
			int list_ind = (int) Math.floor( args.getValue(index++) * ((WorkCenter)obj).getMachineReadyTimes().size() );
			
			if(list_ind + bias >= ((WorkCenter)obj).getMachineReadyTimes().size()) return LOOP_TERMINATE;
			
			res = ((WorkCenter)obj).getMachineReadyTimes().get( list_ind + bias);
			break;
		}
		default: System.err.print("unknown attribute index when getting attributes from Entity in WorkCenter4Entity");
			System.exit(1);
		}
		
		return res;
	}
	
	@Override
	public String toString() {
		return "WorkCenter"+arguments.toString();
	}
    
    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
    	CalcPriorityProblem calcPrioProb = ((CalcPriorityProblem)problem);
        
        DoubleData data = ((DoubleData)input);
    	data.value = getAttributes(calcPrioProb.getWorkCenter(), arguments, 0, 0);
    	
    	return;
    }
    
    public boolean equals(Object other) {
        if (other instanceof WorkCenter4Entity) {
        	if(((WorkCenter4Entity)other).getArguments().equals(arguments))
        		return true;
        }

        return false;
    }
    
    @Override
	public String toGraphvizString() {
		return toString();
	}
}
