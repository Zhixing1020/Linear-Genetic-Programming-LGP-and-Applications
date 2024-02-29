package zhixing.djss.algorithm.multitask.MFEA.individual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import ec.EvolutionState;
import ec.Fitness;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.multiobjective.MultiObjectiveFitness;
import yimei.jss.gp.CalcPriorityProblem;
import yimei.jss.gp.data.DoubleData;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.state.SystemState;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.djss.individual.LGPInterface4DJSS;
import zhixing.djss.jobshop.SchedulingSet4Ind;
import zhixing.djss.simulation.Simulation4Ind;
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA;

public class LGPIndividual_MFEA4DJSS extends LGPIndividual_MFEA implements LGPInterface4DJSS{
	
//	@Override
//	public void resetIndividual(int numReg, int maxIterTime){
//		System.err.println("LGPIndividual_MFEA must have output register when resetting");
//		System.exit(1);
//	}
//	
//	public void resetIndividual(int numReg, int maxIterTime, List<Integer> outputReg) {
//		 numRegs = numReg;
//		 maxIterTimes = maxIterTime;
//		 // set my evaluation to false
//		 evaluated = false;
//		
//		 //initialize registers
//	     registers = new double [numRegs];
//	     //resetRegisters();
//	     
//	     //flow controller
//	     flowctrl = new LGPFlowController();
//	     flowctrl.maxIterTimes = maxIterTimes;
//	     
//	     // load the trees
//	     trees = new LinkedList<>();
//	     
//	     outputRegister = new int[outputReg.size()]; 
//	     for(int i = 0;i<outputReg.size();i++){
//	    	 outputRegister[i] = outputReg.get(i);
//	     }
//	}
//	
	public void calcFitnessInd4MT(Fitness fitness, EvolutionState state, SchedulingSet4Ind schedulingSet,
			List<Objective> objectives, int taskind){
		
		//set the behaviour of LGP individual
		setCurrentOutputRegister(taskind);
		updateStatus(getTreelist().size(), new int[]{curOutReg}); 
		
		//calcFitnessInd(fitness, state, schedulingSet, objectives);
		calcFitnessInd4OneTask(fitness, state, schedulingSet, objectives, taskind);
		
		//reset the effective status of all instructions
		updateStatus();
	}
	
	@Override
	public double execute(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
//		resetRegisters(problem);
//		
//		for(GPTreeStruct tree : trees) {
//			if(tree.status) {
//				tree.child.eval(state, thread, input, stack, individual, problem);
//			}
//		}

		//flowctrl.execute(state, thread, input, stack, (CpxGPIndividual)individual, problem);
		
		LGPInterface4DJSS.super.execute(state, thread, input, stack, individual, problem);
		
		return registers[curOutReg];
	}
	
	
	
//	@Override
//	public  LGPIndividual_MFEA lightClone(){
//		// a deep clone
//		LGPIndividual_MFEA myobj = (LGPIndividual_MFEA)(super.clone());
//		// copy the trees
//        myobj.trees = new LinkedList<GPTreeStruct>();
//        for(GPTreeStruct tree : trees)
//            {
//        	GPTreeStruct t = (GPTreeStruct)(tree.lightClone());
//        	t.owner = myobj;
//            myobj.trees.add(t);  // note light-cloned!
//            //myobj.trees[x].owner = myobj;  // reset owner away from me
//            }
//        myobj.copyLGPproperties(this);
//        return myobj;
//	}
}
