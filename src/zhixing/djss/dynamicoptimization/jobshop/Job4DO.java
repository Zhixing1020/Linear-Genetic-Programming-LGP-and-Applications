package zhixing.djss.dynamicoptimization.jobshop;

import java.util.List;

import yimei.jss.jobshop.Operation;

public class Job4DO extends yimei.jss.jobshop.Job {

	private double responsetime;
	private double cost_rate;
	
	
	public Job4DO(int id, List<Operation> operations, double arrivalTime, double releaseTime, double dueDate,
			double weight, double response, double cost_rate) {
		super(id, operations, arrivalTime, releaseTime, dueDate, weight);
		responsetime = response;
		this.cost_rate = cost_rate;
	}
	
	public Job4DO(int id, List<Operation> operations) {
        this(id, operations, 0, 0, Double.POSITIVE_INFINITY, 1.0, 0.0, 1.0);
    }
	
 //OWT sum
	public double getResponseTime() {
		return responsetime;
	}
	public double getCostRate() {
		return cost_rate;
	}
	public void incrementResponseTime(double value) {
		responsetime += value;
	}
}
