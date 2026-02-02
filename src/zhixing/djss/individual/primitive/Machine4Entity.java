package zhixing.djss.individual.primitive;

import yimei.jss.jobshop.WorkCenter;
import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.CalcPriorityProblem;
import yimei.jss.gp.data.DoubleData;
import yimei.jss.jobshop.Machine;
import zhixing.cpxInd.individual.primitive.Args;
import zhixing.cpxInd.individual.primitive.Entity;

public class Machine4Entity extends Entity implements EntityInterface4DJSS{

	final int attributeNumber = 1;
	final int nonlist_attrNumber = 1;
	
//	public Machine4Entity(int id, WorkCenter workCenter, double readyTime) {
//        super(id, workCenter, readyTime);
//    }
//
//    public Machine4Entity(int id, WorkCenter workCenter) {
//        super(id, workCenter);
//    }

	@Override
	public double getAttributes(Object obj, Args args, int index, int bias) {
		double res = 0;
		int attr_ind = 0;
		
		if(index < args.getMaxLength()-2) {
			attr_ind = (int) Math.floor( args.getValue(index++) * attributeNumber );
		}
		else {
			attr_ind = (int) Math.floor( args.getValue(index++) * nonlist_attrNumber );
		}
		
		switch(attr_ind) {
		case 0: res = ((Machine)obj).getReadyTime(); break;
		default: System.err.print("unknown attribute index when getting attributes from Entity in Machine4Entity");
			System.exit(1);
		}
		
		return res;
	}
	
	@Override
	public String toString() {
		return "Machine"+arguments;
	}

    @Override
    public void eval(EvolutionState state, int thread, GPData input,
                     ADFStack stack, GPIndividual individual, Problem problem) {
    	CalcPriorityProblem calcPrioProb = ((CalcPriorityProblem)problem);
        
        DoubleData data = ((DoubleData)input);
//    	data.value = getAttributes(calcPrioProb.getWorkCenter(), (Args)children[0], 0);
        
        System.err.print("Machine4Enityt is incomplete. The WorkCenter class should be able to return its machines");
        System.exit(1);
    	
    	return;
    }
    
    public boolean equals(Object other) {
        if (other instanceof Machine4Entity) {
        	if(((Machine4Entity)other).getArguments().equals(arguments))
        		return true;
        }

        return false;
    }
    
    @Override
	public String toGraphvizString() {
		return toString();
	}
}
