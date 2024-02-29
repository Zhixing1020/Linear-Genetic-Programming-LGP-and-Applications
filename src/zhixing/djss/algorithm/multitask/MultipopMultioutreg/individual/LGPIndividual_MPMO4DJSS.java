package zhixing.djss.algorithm.multitask.MultipopMultioutreg.individual;

import java.util.List;
import java.util.Vector;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;
import yimei.jss.jobshop.Objective;
import zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.djss.individual.LGPInterface4DJSS;
import zhixing.djss.jobshop.SchedulingSet4Ind;
import zhixing.djss.simulation.Simulation4Ind;

public class LGPIndividual_MPMO4DJSS extends LGPIndividual_MPMO implements LGPInterface4DJSS{
	
//	private static final String P_SKILLFACTOR = "skillfactor";
//	
//	public static double INF_SCALAR_RANK = 1e10; 
//	
//	public Vector<Double> scalarRank_v = null;
//	
//	@Override
//	public void setup(final EvolutionState state, final Parameter base) {
//		super.setup(state,base); 
//		 Parameter def = defaultBase();
//		 
//		// the maximum/minimum number of trees
//	     skillFactor = state.parameters.getInt(base.push(P_SKILLFACTOR),def.push(P_SKILLFACTOR),0);  
//	     if (skillFactor < 0) 
//	         state.output.fatal("An LGPIndividual_MPMO must be assigned to at least one specific task when setting up.",
//	             base.push(P_SKILLFACTOR),def.push(P_SKILLFACTOR));
//	     
//	}
//	
//	public int getEffTreesLength(int taskid){
//		updateStatus(taskid);
//		int res = 0;
//		for(GPTreeStruct tree : getTreelist()){
//			if (tree.status){
//				res ++;
//			}
//		}
//		
//		return res;
//	}
//	
//	public void updateStatus(int taskid) {
//		curOutReg = outputRegister4Sim.get(taskid);
//		updateStatus(getTreelist().size(), new int[]{curOutReg}); 
//	}
//	
//	public void setCurrentOutputRegister(int t){
//		//set the current output for a specific task
//		curOutReg = outputRegister4Sim.get(t);
//		updateStatus(getTreelist().size(), new int[]{curOutReg}); 
//	}
	
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
	
	public double calcFitnessInd4OneTask(EvolutionState state, SchedulingSet4Ind schedulingSet,
			List<Objective> objectives, int simIndex){
		double[] fitnesses = new double[objectives.size()];

		this.prepareExecution(state);
		
		schedulingSet.setIndividual(this);
		Simulation4Ind simulation = schedulingSet.getSimulations().get(simIndex);
		int col = 0;

		//System.out.println("The simulation size is "+simulations.size()); //1
			//Simulation4Ind simulation = simulations.get(j);
			//simulation.rerun(this, state, col);
			simulation.rerun();

			for (int i = 0; i < objectives.size(); i++) {
				// System.out.println("Makespan:
				// "+simulation.objectiveValue(objectives.get(i)));
				// System.out.println("Benchmark makespan:
				// "+schedulingSet.getObjectiveLowerBound(i, col));
				double normObjValue = simulation.objectiveValue(objectives.get(i));
				//		/ (schedulingSet.getObjectiveLowerBound(i, col));

				//modified by fzhang, 26.4.2018  check in test process, whether there is ba
				fitnesses[i] += normObjValue;
			}

			col++;

			//System.out.println("The value of replication is "+schedulingSet.getReplications()); //50
			for (int k = 1; k < schedulingSet.getReplications().get(simIndex); k++) {
				//simulation.rerun(this, state, col);
				simulation.rerun();

				for (int i = 0; i < objectives.size(); i++) {
					double normObjValue = simulation.objectiveValue(objectives.get(i));
					//		/ (schedulingSet.getObjectiveLowerBound(i, col)+1e-6);
					fitnesses[i] += normObjValue;
				}

				col++;
			}

			simulation.reset();
		

		for (int i = 0; i < fitnesses.length; i++) {
			fitnesses[i] /= col;
		}
		
//		MultiObjectiveFitness f = (MultiObjectiveFitness) fitness;
//		if(f.getNumObjectives()>1){
//			f.setObjectives(state, fitnesses);
//		}
//		else{
//			double [] tmp_f = new double [1];
//			tmp_f[0] = fitnesses[simIndex];
//			f.setObjectives(state, tmp_f);
//		}
		return fitnesses[simIndex];
	}
	
//	protected void copyLGPproperties(LGPIndividual_MPMO4DJSS obj){
//		super.copyLGPproperties(obj);
//		this.scalarRank_v = new Vector<Double>();
//		for(Double d : obj.scalarRank_v){
//			this.scalarRank_v.add(d);
//		}
//	}
}
