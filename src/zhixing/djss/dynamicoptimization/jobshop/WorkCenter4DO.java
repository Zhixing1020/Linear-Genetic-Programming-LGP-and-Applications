package zhixing.djss.dynamicoptimization.jobshop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import yimei.jss.jobshop.Operation;

public class WorkCenter4DO extends yimei.jss.jobshop.WorkCenter{
	//energy
	private static final double [] idleEnergyRates = {2400,3360,2000,1770,2200,7500,2000,1770,2200,7500,2400,3360,2000,1770,2200};
	private static final int runEnergyRateFactor = 2;
	private double idleEnergyRate;
//	private double runEnergyRate;
	private double energyCost;  // (idle energy rate * idle time + running energy rate * running time)*energy price
	
	public WorkCenter4DO(int id, int numMachines, LinkedList<Operation> queue, List<Double> machineReadyTimes,
			double workInQueue, double busyTime, double energyCost) {
		super(id, numMachines, queue, machineReadyTimes, workInQueue, busyTime);
		this.energyCost = energyCost;
		idleEnergyRate = idleEnergyRates[id%15];
//		runEnergyRate = idleEnergyRate + idleEnergyRates[(id + runEnergyRateFactor)%15];
	}
	
	public WorkCenter4DO(int id, int numMachines) {
        this(id, numMachines, new LinkedList<>(),
                new ArrayList<>(Collections.nCopies(numMachines, 0.0)),
                0.0, 0.0, 0.0);
    }

    public WorkCenter4DO(int id) {
        this(id, 1);
    }

    public void incrementEnergyCost(double value, double price, Job4DO job, boolean isidle) {
    	if(isidle) {
    		energyCost += value*idleEnergyRate*price;
    	}
    	else {
    		energyCost += value*idleEnergyRate*job.getCostRate()*price;
    	}
    }
    
    public double getEnergyCost() {
    	return energyCost;
    }
    
    public void resetEnergyCost() {
    	energyCost = 0;
    }
    
    public double getIdleEnergyRate() {
    	return idleEnergyRate;
    }
    
    @Override
    public void reset() {
        super.reset();
        energyCost = 0;
    }
}
