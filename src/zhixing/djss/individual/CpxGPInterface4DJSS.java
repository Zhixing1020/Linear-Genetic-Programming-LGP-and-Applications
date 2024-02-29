package zhixing.djss.individual;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.List;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPDefaults;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;
import yimei.jss.gp.CalcPriorityProblem;
import yimei.jss.gp.data.DoubleData;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.DecisionSituation;
import yimei.jss.simulation.state.SystemState;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.CpxGPInterface4Problem;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.djss.jobshop.SchedulingSet4Ind;
import zhixing.djss.simulation.DynamicSimulation4Ind;
import zhixing.djss.simulation.Simulation4Ind;

public interface CpxGPInterface4DJSS extends CpxGPInterface4Problem{

	
	default public void calcFitnessInd(Fitness fitness, EvolutionState state, SchedulingSet4Ind schedulingSet,
			List<Objective> objectives)
	{
		 
			double[] fitnesses = new double[objectives.size()];

			this.prepareExecution(state);
			
			schedulingSet.setIndividual(this);
			List<Simulation4Ind> simulations = schedulingSet.getSimulations();
			int col = 0;

			//System.out.println("The simulation size is "+simulations.size()); //1
			for (int j = 0; j < simulations.size(); j++) {
				Simulation4Ind simulation = simulations.get(j);
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
				for (int k = 1; k < schedulingSet.getReplications().get(j); k++) {
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
			}

			for (int i = 0; i < fitnesses.length; i++) {
				fitnesses[i] /= col;
			}
			MultiObjectiveFitness f = (MultiObjectiveFitness) fitness;
			f.setObjectives(state, fitnesses);
	 }
	
	default public void calcFitnessInd4OneTask(Fitness fitness, EvolutionState state, SchedulingSet4Ind schedulingSet,
			List<Objective> objectives, int simIndex)
	{
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
		
		MultiObjectiveFitness f = (MultiObjectiveFitness) fitness;
		if(f.getNumObjectives()>1){
			f.setObjectives(state, fitnesses);
		}
		else{
			double [] tmp_f = new double [1];
			tmp_f[0] = fitnesses[simIndex];
			f.setObjectives(state, tmp_f);
		}
	}
	
	
	
	default public Operation priorOperation(DecisionSituation decisionSituation)
	{
        List<Operation> queue = decisionSituation.getQueue();
        WorkCenter workCenter = decisionSituation.getWorkCenter();
        SystemState systemState = decisionSituation.getSystemState();

        Operation priorOp = queue.get(0);
        priorOp.setPriority(
                priority(priorOp, workCenter, systemState));

        for (int i = 1; i < queue.size(); i++) {
            Operation op = queue.get(i);
            op.setPriority(priority(op, workCenter, systemState));

            if (op.priorTo(priorOp))
                priorOp = op;
        }
        
        

        return priorOp;
    }
	
	default double priority(Operation op,
            WorkCenter workCenter,
            SystemState systemState){
		//it is used in Job Shop Scheduling, to prioritize the operations in a machine queue
		CalcPriorityProblem calcPrioProb =
		 new CalcPriorityProblem(op, workCenter, systemState);
		
		DoubleData tmp = new DoubleData();
		
		return execute(null, 0, tmp, null, (GPIndividual) this, calcPrioProb);
	}
	
}
