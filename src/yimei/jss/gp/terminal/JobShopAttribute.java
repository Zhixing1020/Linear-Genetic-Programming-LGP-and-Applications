package yimei.jss.gp.terminal;

import org.apache.commons.lang3.math.NumberUtils;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.state.SystemState;
import zhixing.djss.dynamicoptimization.jobshop.*;
import zhixing.djss.dynamicoptimization.simulation.state.SystemState4DO;

import java.util.*;

/**
 * The attributes of the job shop.
 * NOTE: All the attributes are relative to the current time.
 *       This is for making the decision making process memoryless,
 *       i.e. independent of the current time.
 *
 * @author yimei
 */

public enum JobShopAttribute {
    CURRENT_TIME("t"), // the current time

    // The machine-related attributes (independent of the jobs in the queue of the machine).
    NUM_OPS_IN_QUEUE("NIQ"), // the number of operations in the queue
    WORK_IN_QUEUE("WIQ"), // the work in the queue
    MACHINE_READY_TIME("MRT"), // the ready time of the machine

    // The job/operation-related attributes (depend on the jobs in the queue).
    PROC_TIME("PT"), // the processing time of the operation
    NEXT_PROC_TIME("NPT"), // the processing time of the next operation
    OP_READY_TIME("ORT"), // the ready time of the operation
    NEXT_READY_TIME("NRT"), // the ready time of the next machine
    WORK_REMAINING("WKR"), // the work remaining
    NUM_OPS_REMAINING("NOR"), // the number of operations remaining
    WORK_IN_NEXT_QUEUE("WINQ"), // the work in the next queue
    NUM_OPS_IN_NEXT_QUEUE("NINQ"), // number of operations in the next queue
    FLOW_DUE_DATE("FDD"), // the flow due date
    DUE_DATE("DD"), // the due date
    WEIGHT("W"), // the job weight
    ARRIVAL_TIME("AT"), // the arrival time

    // Relative version of the absolute time attributes
    MACHINE_WAITING_TIME("MWT"), // the waiting time of the machine = t - MRT
    OP_WAITING_TIME("OWT"), // the waiting time of the operation = t - ORT
    NEXT_WAITING_TIME("NWT"), // the waiting time for the next machine to be ready = NRT - t
    RELATIVE_FLOW_DUE_DATE("rFDD"), // the relative flow due date = FDD - t
    RELATIVE_DUE_DATE("rDD"), // the relative due date = DD - t

    // Used in Su's paper
    TIME_IN_SYSTEM("TIS"), // time in system = t - releaseTime
    SLACK("SL"), // the slack
	
	
	//Used in Zhixing's Grammar work, together with IF, WHILE statement
    PROC_TIME_RATIO("PTR"),
    NUM_OPS_REMAIN_RATIO("NORR"),
    WORK_REMAIN_RATIO("WKRR"),
    NUM_OPS_NEXT_QUEUE_RATIO("NNQR"),
    WORK_NEXT_QUEUE_RATIO("WNQR"),
    OP_WAIT_TIME_RATIO("OWTR"),
    WEIGHT_RATIO("WR"),
    RELATIVE_FLOW_DUEDATE_RATIO("rFDR"),
    JOB_ENERGY_RATIO("JERO"),
    
    JOB_ENERGY_RATE("JER"),
    MACHINE_ENERGY_RATE("MER"),
    
	NUM_OPS_IN_QUEUE_RATIO("NIQR"), // the ratio of the number of operations in the queue
	WORK_IN_QUEUE_RATIO("WIQR"), // the ratio of the work in the queue
//	SLACK_IN_QUEUE_RATIO("SIQR"), // the ratio of the minimum slack in the queue
//	OP_WAIT_TIME_IN_QUEUE_RATIO("OWQR"), // the ratio of the sum of the operation waiting time in the queue
//	NUM_OPS_IN_NEXT_QUEUE_RATIO("NNQR"), //the ratio of the sum of the number of operations in the next queue
//	WORK_REMAINING_RATIO("WKRR"),  //the ratio of the sum of the remaining work in the queue
//	RELATIVE_DUEDATE_RATIO("rDDR"),  //the ratio of the minimum of the relative due-date in the queue 
	BOTTLENECK_WORKLOAD_RATIO("BWR"),   //the ratio of the workload in the bottleneck machine
	
//	DEVIATION_SLACK_IN_QUEUE("DSIQ"),  //the ratio of the slack of the jobs in the queue
	DEVIATION_PROC_TIME("DPT"), //the deviation of processing time of the operations in a machine queue
	DEVIATION_OP_WAITING_TIME("DOWT"), //the deviation of operation waiting time in a machine queue
//	DEVIATION_MACHINE_READY_TIME("DMRT"), //the deviation of machine next waiting time
	DEVIATION_NUM_OPS_IN_NEXT_QUEUE("DNNQ"), //the deviation of the number of operations in next queue
	DEVIATION_WORK_IN_NEXT_QUEUE("DWNQ"), //the deviation of the workload in the next queue
	DEVIATION_NEXT_PROC_TIME("DNPT"),  //the deviation of the next processing time in the queue
//	DEVIATION_WORK_REMAINING("DWKR"),  //the deviation of remaining workload of an operation
//	DEVIATION_NUM_OPS_REMAINING("DNOR"),  //the deviation of the number of the remaining operations 
	
	ENERGY_PRICE_RATIO("EPR"),   //the ratio of the energy price
	SATISFACTORY_FACTOR_RATIO("SFR") //the ratio of user satisfactory factor
	;

    private final String name;

    JobShopAttribute(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Reverse-lookup map
    private static final Map<String, JobShopAttribute> lookup = new HashMap<>();

    static {
        for (JobShopAttribute a : JobShopAttribute.values()) {
            lookup.put(a.getName(), a);
        }
    }

    public static JobShopAttribute get(String name) {
        return lookup.get(name);
    }

    public double value(Operation op, WorkCenter workCenter, SystemState systemState) {
        double value = -1;

        switch (this) {
            case CURRENT_TIME:
                value = systemState.getClockTime();
                break;
            case NUM_OPS_IN_QUEUE:
                value = workCenter.getQueue().size();
                break;
            case WORK_IN_QUEUE:
                value = workCenter.getWorkInQueue();
                break;
            case MACHINE_READY_TIME:
                value = workCenter.getReadyTime();
                break;
            case MACHINE_WAITING_TIME:
                value = systemState.getClockTime() - workCenter.getReadyTime();
                break;
            case PROC_TIME:
                value = op.getProcTime();
                break;
            case NEXT_PROC_TIME:
                value = op.getNextProcTime();
                break;
            case OP_READY_TIME:
                value = op.getReadyTime();    //debug, 2021.12.29  zhixing   original: systemState.getClockTime()
                break;
            case OP_WAITING_TIME:
                value = systemState.getClockTime() - op.getReadyTime();
                break;
            case NEXT_READY_TIME:
                value = systemState.nextReadyTime(op);
                break;
            case NEXT_WAITING_TIME:
                value = systemState.nextReadyTime(op) - systemState.getClockTime();
                break;
            case WORK_REMAINING:
                value = op.getWorkRemaining();
                break;
            case NUM_OPS_REMAINING:
                value = op.getNumOpsRemaining();
                break;
            case WORK_IN_NEXT_QUEUE:
                value = systemState.workInNextQueue(op);
                break;
            case NUM_OPS_IN_NEXT_QUEUE:
                value = systemState.numOpsInNextQueue(op);
                break;
            case FLOW_DUE_DATE:
                value = op.getFlowDueDate();
                break;
            case RELATIVE_FLOW_DUE_DATE:
                value = op.getFlowDueDate() - systemState.getClockTime();
                break;
            case DUE_DATE:
                value = op.getJob().getDueDate();
                break;
            case RELATIVE_DUE_DATE:
                value = op.getJob().getDueDate() - systemState.getClockTime();
                break;
            case WEIGHT:
                value = op.getJob().getWeight();
                break;
            case ARRIVAL_TIME:
                value = op.getJob().getArrivalTime();
                break;
            case TIME_IN_SYSTEM:
                value = systemState.getClockTime() - op.getJob().getReleaseTime();
                break;
            case SLACK:
                value = op.getJob().getDueDate() - systemState.getClockTime() - op.getWorkRemaining();
                break;
                
            case PROC_TIME_RATIO:
            	value = get_PTR(op,workCenter,systemState);
            	break;
            case NUM_OPS_REMAIN_RATIO:
            	value = get_NORR(op,workCenter,systemState);
            	break;
            case WORK_REMAIN_RATIO:
            	value = get_WKRR(op,workCenter,systemState);
            	break;
            case NUM_OPS_NEXT_QUEUE_RATIO:
            	value = get_NNQR(op,workCenter,systemState);
            	break;
            case WORK_NEXT_QUEUE_RATIO:
            	value = get_WNQR(op,workCenter,systemState);
            	break;
            case OP_WAIT_TIME_RATIO:
            	value = get_OWTR(op,workCenter,systemState);
            	break;
            case WEIGHT_RATIO:
            	value = get_WR(op,workCenter,systemState);
            	break;
            case RELATIVE_FLOW_DUEDATE_RATIO:
            	value = get_rFDR(op,workCenter,systemState);
            	break;
            case JOB_ENERGY_RATIO:
            	value = ((Job4DO)op.getJob()).getCostRate() / 3;
            	break;
                
            case JOB_ENERGY_RATE:
            	value = ((Job4DO)op.getJob()).getCostRate();
            case MACHINE_ENERGY_RATE:
            	value = ((WorkCenter4DO)workCenter).getIdleEnergyRate() / 7500;
                
            case NUM_OPS_IN_QUEUE_RATIO:
            	value = get_NIQR(op, workCenter, systemState);
            	break;
            case WORK_IN_QUEUE_RATIO:
            	value = get_WIQR(op, workCenter, systemState);
            	break;
//            case SLACK_IN_QUEUE_RATIO:
//            	value = get_SIQR(op, workCenter, systemState);
//            	break;
//            case OP_WAIT_TIME_IN_QUEUE_RATIO:
//            	value = get_OWQR(op, workCenter, systemState);
//            	break;
//            case NUM_OPS_IN_NEXT_QUEUE_RATIO:
//            	value = get_NNQSR(op, workCenter, systemState);
//            	break;
//            case WORK_REMAINING_RATIO:
//            	value = get_WKRSR(op, workCenter, systemState);
//            	break;
//            case RELATIVE_DUEDATE_RATIO:
//            	value = get_rDDR(op, workCenter, systemState);
//            	break;
//            case DEVIATION_SLACK_IN_QUEUE:
//            	value = get_DSIQ(op, workCenter, systemState);
//            	break;
            case DEVIATION_PROC_TIME:
            	value = get_DPT(op, workCenter, systemState);
            	break;
            case DEVIATION_OP_WAITING_TIME:
            	value = get_DOWT(op, workCenter, systemState);
            	break;
//            case DEVIATION_MACHINE_READY_TIME:
//            	value = get_DMRT(op, workCenter, systemState);
//            	break;
            case DEVIATION_NUM_OPS_IN_NEXT_QUEUE:
            	value = get_DNNQ(op, workCenter, systemState);
            	break;
            case DEVIATION_WORK_IN_NEXT_QUEUE:
            	value = get_DWNQ(op, workCenter, systemState);
            	break;
            case DEVIATION_NEXT_PROC_TIME:
            	value = get_DNPT(op, workCenter, systemState);
            	break;
//            case DEVIATION_WORK_REMAINING:
//            	value = get_DWKR(op, workCenter, systemState);
//            	break;
//            case DEVIATION_NUM_OPS_REMAINING:
//            	value = get_DNOR(op, workCenter, systemState);
//            	break;
            case BOTTLENECK_WORKLOAD_RATIO:
            	value = get_BWR(op, workCenter, systemState);
            	break;
            case ENERGY_PRICE_RATIO:
            	value = ((SystemState4DO)systemState).getEnergyPrice() / 1.5e-2;
            	break;
            case SATISFACTORY_FACTOR_RATIO:
            	value = ((SystemState4DO)systemState).getUserReactiveFactor() / 2.3;
            	break;
            default:
                System.err.println("Undefined attribute " + name);
                System.exit(1);
        }

        return value;
    }

    public static double valueOfString(String attribute, Operation op, WorkCenter workCenter,
                                       SystemState systemState,
                                       List<JobShopAttribute> ignoredAttributes) {
        JobShopAttribute a = get(attribute);
        if (a == null) {
            if (NumberUtils.isNumber(attribute)) {
                return Double.valueOf(attribute);
            } else {
                System.err.println(attribute + " is neither a defined attribute nor a number.");
                System.exit(1);
            }
        }

        if (ignoredAttributes.contains(a)) {
            return 1.0;
        } else {
            return a.value(op, workCenter, systemState);
        }
    }

    /**
     * Return the basic attributes.
     * @return the basic attributes.
     */
    public static JobShopAttribute[] basicAttributes() {
        return new JobShopAttribute[]{
                JobShopAttribute.CURRENT_TIME,
                JobShopAttribute.NUM_OPS_IN_QUEUE,
                JobShopAttribute.WORK_IN_QUEUE,
                JobShopAttribute.MACHINE_READY_TIME,
                JobShopAttribute.PROC_TIME,
                JobShopAttribute.NEXT_PROC_TIME,
                JobShopAttribute.OP_READY_TIME,
                JobShopAttribute.NEXT_READY_TIME,
                JobShopAttribute.WORK_REMAINING,
                JobShopAttribute.NUM_OPS_REMAINING,
                JobShopAttribute.WORK_IN_NEXT_QUEUE,
                JobShopAttribute.NUM_OPS_IN_NEXT_QUEUE,
                JobShopAttribute.FLOW_DUE_DATE,
                JobShopAttribute.DUE_DATE,
                JobShopAttribute.WEIGHT,

                JobShopAttribute.ARRIVAL_TIME,
                JobShopAttribute.SLACK
        };
    }

    /**
     * The attributes relative to the current time.
     * @return the relative attributes.
     */
    public static JobShopAttribute[] relativeAttributes() {
        return new JobShopAttribute[]{
//                JobShopAttribute.NUM_OPS_IN_QUEUE,
//                JobShopAttribute.WORK_IN_QUEUE,
//                JobShopAttribute.MACHINE_WAITING_TIME,
//                JobShopAttribute.PROC_TIME,
//                JobShopAttribute.NEXT_PROC_TIME,
//                JobShopAttribute.OP_WAITING_TIME,
//                JobShopAttribute.NEXT_WAITING_TIME,
//                JobShopAttribute.WORK_REMAINING,
//                JobShopAttribute.NUM_OPS_REMAINING,
//                JobShopAttribute.WORK_IN_NEXT_QUEUE,
//                JobShopAttribute.NUM_OPS_IN_NEXT_QUEUE,
//                JobShopAttribute.RELATIVE_FLOW_DUE_DATE,
//                JobShopAttribute.RELATIVE_DUE_DATE,
//                JobShopAttribute.WEIGHT,
//
//                JobShopAttribute.TIME_IN_SYSTEM,
//                JobShopAttribute.SLACK
                //=============2021.12.29  zhixing,  for better initialization of registers
                //principle: 1) diverse enough 
                JobShopAttribute.PROC_TIME,
                JobShopAttribute.NEXT_PROC_TIME,
                JobShopAttribute.WORK_IN_NEXT_QUEUE,
                JobShopAttribute.WORK_REMAINING,
                JobShopAttribute.RELATIVE_FLOW_DUE_DATE,
                JobShopAttribute.OP_WAITING_TIME,
                
                JobShopAttribute.NUM_OPS_REMAINING,
                JobShopAttribute.NUM_OPS_IN_NEXT_QUEUE,
                JobShopAttribute.WEIGHT,
                JobShopAttribute.RELATIVE_DUE_DATE,
                JobShopAttribute.NEXT_WAITING_TIME,
                JobShopAttribute.TIME_IN_SYSTEM,
                
                JobShopAttribute.SLACK,

                JobShopAttribute.NUM_OPS_IN_QUEUE,
                JobShopAttribute.WORK_IN_QUEUE,
                JobShopAttribute.MACHINE_WAITING_TIME,

                //================
        };
    }
    
    //==================2023.4.11   zhixing,  for grammar-guided LGP
    public static JobShopAttribute[] relativeAttributes4grammar() {
        return new JobShopAttribute[]{
                JobShopAttribute.PROC_TIME,
                JobShopAttribute.NEXT_PROC_TIME,
                JobShopAttribute.WORK_IN_NEXT_QUEUE,
                JobShopAttribute.WORK_REMAINING,
                JobShopAttribute.RELATIVE_FLOW_DUE_DATE,
                JobShopAttribute.OP_WAITING_TIME,
                
                JobShopAttribute.NUM_OPS_REMAINING,
                JobShopAttribute.NUM_OPS_IN_NEXT_QUEUE,
                JobShopAttribute.WEIGHT,
                JobShopAttribute.RELATIVE_DUE_DATE,
                JobShopAttribute.NEXT_WAITING_TIME,
                JobShopAttribute.TIME_IN_SYSTEM,
                
                JobShopAttribute.SLACK,

//                JobShopAttribute.NUM_OPS_IN_QUEUE,
//                JobShopAttribute.WORK_IN_QUEUE,
//                JobShopAttribute.MACHINE_WAITING_TIME,
        	
                JobShopAttribute.PROC_TIME_RATIO,
                JobShopAttribute.NUM_OPS_REMAIN_RATIO,
                JobShopAttribute.WORK_REMAIN_RATIO,
                JobShopAttribute.NUM_OPS_NEXT_QUEUE_RATIO,
                JobShopAttribute.WORK_NEXT_QUEUE_RATIO,
                JobShopAttribute.OP_WAIT_TIME_RATIO,
                JobShopAttribute.WEIGHT_RATIO,
                JobShopAttribute.RELATIVE_FLOW_DUEDATE_RATIO,
                JobShopAttribute.JOB_ENERGY_RATIO,
                
                JobShopAttribute.JOB_ENERGY_RATE,
                JobShopAttribute.MACHINE_ENERGY_RATE,
                
        		JobShopAttribute.NUM_OPS_IN_QUEUE_RATIO,
        		JobShopAttribute.WORK_IN_QUEUE_RATIO,
//        		JobShopAttribute.SLACK_IN_QUEUE_RATIO,
//        		JobShopAttribute.OP_WAIT_TIME_IN_QUEUE_RATIO,
//        		JobShopAttribute.NUM_OPS_IN_NEXT_QUEUE_RATIO,
//        		JobShopAttribute.WORK_REMAINING_RATIO,
//        		JobShopAttribute.RELATIVE_DUEDATE_RATIO,
        		
//        		JobShopAttribute.DEVIATION_SLACK_IN_QUEUE,
        		JobShopAttribute.DEVIATION_PROC_TIME,
        		JobShopAttribute.DEVIATION_OP_WAITING_TIME,
//        		JobShopAttribute.DEVIATION_MACHINE_READY_TIME,
        		JobShopAttribute.DEVIATION_NUM_OPS_IN_NEXT_QUEUE,
        		JobShopAttribute.DEVIATION_WORK_IN_NEXT_QUEUE,
        		JobShopAttribute.BOTTLENECK_WORKLOAD_RATIO,
        		JobShopAttribute.DEVIATION_NEXT_PROC_TIME,
//        		JobShopAttribute.DEVIATION_NUM_OPS_REMAINING,
//        		JobShopAttribute.DEVIATION_WORK_REMAINING,
        		
        		JobShopAttribute.ENERGY_PRICE_RATIO,
        		JobShopAttribute.SATISFACTORY_FACTOR_RATIO
        };
    }
  //=============================================
    
    //=====================2023.12.5  zhixing, for fitness landscape optimization
    public static JobShopAttribute[] simpleAttributes() {
    	return new JobShopAttribute[] {
    			JobShopAttribute.PROC_TIME,
    			JobShopAttribute.OP_WAITING_TIME
    	};
    }
    //======================

    /**
     * The attributes for minimising mean weighted tardiness (Su's paper).
     * @return the attributes.
     */
    public static JobShopAttribute[] mwtAttributes() {
        return new JobShopAttribute[]{
                JobShopAttribute.TIME_IN_SYSTEM,
                JobShopAttribute.OP_WAITING_TIME,
                JobShopAttribute.NUM_OPS_REMAINING,
                JobShopAttribute.WORK_REMAINING,
                JobShopAttribute.PROC_TIME,
                JobShopAttribute.DUE_DATE,
                JobShopAttribute.SLACK,
                JobShopAttribute.WEIGHT,
                JobShopAttribute.NEXT_PROC_TIME,
                JobShopAttribute.WORK_IN_NEXT_QUEUE
        };
    }

    public static JobShopAttribute[] countAttributes() {
        return new JobShopAttribute[] {
                JobShopAttribute.NUM_OPS_IN_QUEUE,
                JobShopAttribute.NUM_OPS_REMAINING,
                JobShopAttribute.NUM_OPS_IN_NEXT_QUEUE
        };
    }

    public static JobShopAttribute[] weightAttributes() {
        return new JobShopAttribute[] {
                JobShopAttribute.WEIGHT
        };
    }

    public static JobShopAttribute[] timeAttributes() {
        return new JobShopAttribute[] {
                JobShopAttribute.MACHINE_WAITING_TIME,
                JobShopAttribute.OP_WAITING_TIME,
                JobShopAttribute.NEXT_READY_TIME,
                JobShopAttribute.FLOW_DUE_DATE,
                JobShopAttribute.DUE_DATE,

                JobShopAttribute.WORK_IN_QUEUE,
                JobShopAttribute.PROC_TIME,
                JobShopAttribute.NEXT_PROC_TIME,
                JobShopAttribute.WORK_REMAINING,
                JobShopAttribute.WORK_IN_NEXT_QUEUE,

                JobShopAttribute.TIME_IN_SYSTEM,
                JobShopAttribute.SLACK
        };
    }
    
    private static double get_NIQR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double sum = 1e-7;
    	for(WorkCenter w : systemState.getWorkCenters()) {
    		sum += w.getQueue().size();
    	}
    	
    	res = workCenter.getQueue().size() / sum;
    	return res;
    }
    
    private static double get_WIQR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double sum = 1e-7;
    	for(WorkCenter w : systemState.getWorkCenters()) {
    		sum += w.getWorkInQueue();
    	}
    	
    	res = workCenter.getWorkInQueue() / sum;
    	return res;
    }
    
    private static double get_minSIQ(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double minSIQ = 1e9;
    	
    	if(workCenter.getQueue().size() == 0) return 1e6;
    	
    	//find min SLACK in the queue
    	double tmp;
    	for(Operation p : workCenter.getQueue()) {
    		tmp = p.getJob().getDueDate() - systemState.getClockTime() - p.getWorkRemaining();
    		if(tmp<minSIQ) minSIQ = tmp;
    	}
    	
    	res = minSIQ;
    	return res;
    }
    private static double get_SIQR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	
    	double minSIQ = get_minSIQ(op, workCenter, systemState);
    	
    	//find the min SLACK in the job shop
    	double minSIQ_shop = 1e9;
    	double maxSIQ_shop = -1e9;
    	double tmp;
    	for(WorkCenter w : systemState.getWorkCenters()) {
    		tmp = get_minSIQ(op, w, systemState);
    		if(tmp < minSIQ_shop) minSIQ_shop = tmp;
    		if(tmp > maxSIQ_shop) maxSIQ_shop = tmp;
    	}
    	
    	res = (minSIQ - minSIQ_shop) / (maxSIQ_shop - minSIQ_shop + 1e-7);
    	return res;
    }
    
    private static double get_OWQR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double sum = 1e-7;
    	double tmp_sum = 0;
    	double target_sum = 0;
    	for(WorkCenter w : systemState.getWorkCenters()) {
    		tmp_sum = 0;
    		for(Operation p : w.getQueue()) {
    			tmp_sum += systemState.getClockTime() - p.getReadyTime();
    		}
    		if(w == workCenter) target_sum = tmp_sum;
    		sum += tmp_sum;
    	}
    	
    	res = target_sum / sum;
    	return res;
    }
    
    private static double get_NNQSR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double sum = 1e-7;
    	double tmp_sum = 0;
    	double target_sum = 0;
    	for(WorkCenter w : systemState.getWorkCenters()) {
    		tmp_sum = 0;
    		for(Operation p : w.getQueue()) {
    			tmp_sum += systemState.numOpsInNextQueue(p);
    		}
    		if(w == workCenter) target_sum = tmp_sum;
    		sum += tmp_sum;
    	}
    	
    	res = target_sum / sum;
    	return res;
    }
    
    private static double get_DSIQ(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double maxSIQ = -1e7, minSIQ = 1e9;
    	
    	if(workCenter.getQueue().size() == 0) return 1;
    	
    	//find min and max Slack in the queue
    	double tmp;
    	for(Operation p : workCenter.getQueue()) {
    		tmp = p.getJob().getDueDate() - systemState.getClockTime() - p.getWorkRemaining();
    		if(tmp < 0) tmp *= -0.01;
    		if(tmp>maxSIQ) maxSIQ = tmp;
    		if(tmp<minSIQ) minSIQ = tmp;
    	}
    	
    	res = minSIQ / maxSIQ;
//    	res = (res / (Math.sqrt(res*res + 1)) + 1)/2;
    	return res;
    }
    
    private static double get_DPT(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double maxPT = 1e-4, minPT = 1e9;
    	
    	if(workCenter.getQueue().size() == 0) return 1;
    	
    	//find max PT in the queue
    	for(Operation p : workCenter.getQueue()) {
    		if(p.getProcTime()>maxPT) maxPT = p.getProcTime();
    		if(p.getProcTime()<minPT) minPT = p.getProcTime();
    	}
    	
    	res = minPT / maxPT;
    	return res;
    }
    
    private static double get_DOWT(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double maxOWT = 1e-4, minOWT = 1e9;
    	
    	if(workCenter.getQueue().size() == 0) return 1;
    	
    	//find max OWT in the queue
    	for(Operation p : workCenter.getQueue()) {
    		
    		double wt = systemState.getClockTime() - p.getReadyTime();
    		
    		if(wt>maxOWT) maxOWT = wt;
    		if(wt<minOWT) minOWT = wt;
    	}
    	
    	
    	res = minOWT / maxOWT;
    	return res;
    }
    
    private static double get_DMRT(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double maxMRT = 1e-4, minMRT = 1e9;
    	
    	//find max NWT in the job shop
    	if(systemState.getWorkCenters().size() == 1) return 1.;
    	
    	for(WorkCenter w : systemState.getWorkCenters()) {
    		
    		if(w == workCenter) continue;
    		
    		double wt = Math.max(0, w.getReadyTime() - systemState.getClockTime());
    		
    		if(wt>maxMRT) maxMRT = wt;
    		if(wt<minMRT) minMRT = wt;
    	}
    	
    	res = minMRT / maxMRT;
    	return res;
    }
    
    private static double get_DNNQ(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double maxNINQ = 1e-4, minNINQ = 1e9;
    	
    	if(workCenter.getQueue().size() == 0) return 1;
    	
    	//find max PT in the queue
    	for(Operation p : workCenter.getQueue()) {
    		double tmp = systemState.numOpsInNextQueue(p);
    		if(tmp>maxNINQ) maxNINQ = tmp;
    		if(tmp<minNINQ) minNINQ = tmp;
    	}
    	
    	res = minNINQ / maxNINQ;
    	return res;
    }
    
    private static double get_DWNQ(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double maxWINQ = 1e-4, minWINQ = 1e9;
    	
    	if(workCenter.getQueue().size() == 0) return 1;
    	
    	//find max PT in the queue
    	for(Operation p : workCenter.getQueue()) {
    		double tmp = systemState.workInNextQueue(p);
    		if(tmp>maxWINQ) maxWINQ = tmp;
    		if(tmp<minWINQ) minWINQ = tmp;
    	}
    	
    	res = minWINQ / maxWINQ;
    	return res;
    }
    
    private static double get_DNPT(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double maxNPT = 1e-4, minNPT = 1e9;
    	
    	if(workCenter.getQueue().size() == 0) return 1;
    	
    	//find max PT in the queue
    	for(Operation p : workCenter.getQueue()) {
    		double tmp = p.getNextProcTime();
    		if(tmp>maxNPT) maxNPT = tmp;
    		if(tmp<minNPT) minNPT = tmp;
    	}
    	
    	res = minNPT / maxNPT;
    	return res;
    }
    
    private static double get_DWKR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double maxWKR = 1e-4, minWKR = 1e9;
    	
    	if(workCenter.getQueue().size() == 0) return 1;
    	
    	//find max PT in the queue
    	for(Operation p : workCenter.getQueue()) {
    		double tmp = p.getWorkRemaining();
    		if(tmp>maxWKR) maxWKR = tmp;
    		if(tmp<minWKR) minWKR = tmp;
    	}
    	
    	res = minWKR / maxWKR;
    	return res;
    }
    
    private static double get_DNOR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double maxNOR = 1e-4, minNOR = 1e9;
    	
    	if(workCenter.getQueue().size() == 0) return 1;
    	
    	//find max PT in the queue
    	for(Operation p : workCenter.getQueue()) {
    		double tmp = p.getNumOpsRemaining();
    		if(tmp>maxNOR) maxNOR = tmp;
    		if(tmp<minNOR) minNOR = tmp;
    	}
    	
    	res = minNOR / maxNOR;
    	return res;
    }
    
    private static double get_BWR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double sum = 1e-7;
    	double maxWIQ = 0;
    	for(WorkCenter w : systemState.getWorkCenters()) {
    		sum += w.getWorkInQueue();
    		
    		if(w.getWorkInQueue() > maxWIQ) {
    			maxWIQ = w.getWorkInQueue();
    		}
    	}
    	
    	res = maxWIQ / sum;
    	return res;
    }
    private static double get_minrDD(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double minrDD = 1e9;
    	
    	if(workCenter.getQueue().size() == 0) return 1e6;
    	
    	//find min rDD in the queue
    	double tmp;
    	for(Operation p : workCenter.getQueue()) {
    		tmp = p.getJob().getDueDate() - systemState.getClockTime();
    		if(tmp<minrDD) minrDD = tmp;
    	}
    	
    	res = minrDD;
    	return res;
    }
    private static double get_rDDR(Operation op, WorkCenter workCenter, SystemState systemState) {
		double res = 0;
	    	
    	double minrDD = get_minrDD(op, workCenter, systemState);
    	
    	//find the min SLACK in the job shop
    	double minrDD_shop = 1e9;
    	double maxrDD_shop = -1e9;
    	double tmp;
    	for(WorkCenter w : systemState.getWorkCenters()) {
    		tmp = get_minrDD(op, w, systemState);
    		if(tmp < minrDD_shop) minrDD_shop = tmp;
    		if(tmp > maxrDD_shop) maxrDD_shop = tmp;
    	}
    	
    	res = (minrDD - minrDD_shop) / (maxrDD_shop - minrDD_shop + 1e-7);
    	return res;
	}
	 
	 private static double get_WKRSR(Operation op, WorkCenter workCenter, SystemState systemState) {
	    	double res = 0;
	    	double sum = 1e-7;
	    	double tmp_sum = 0;
	    	double target_sum = 0;
	    	for(WorkCenter w : systemState.getWorkCenters()) {
	    		tmp_sum = 0;
	    		for(Operation p : w.getQueue()) {
	    			tmp_sum += p.getWorkRemaining();
	    		}
	    		if(w == workCenter) target_sum = tmp_sum;
	    		sum += tmp_sum;
	    	}
	    	
	    	res = target_sum / sum;
	    	return res;
	  }
	 
	 
	 
    private static double get_PTR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double sum = 1e-7;
    	
    	double maxres = -1e5;

    	for(Operation o : workCenter.getQueue()) {
//    		sum += o.getProcTime();
    		
    		if(o.getProcTime() > maxres) {
    			maxres = o.getProcTime();
    		}
    	}
//    	res = op.getProcTime() / sum;
    	res = op.getProcTime() / maxres;
    	
    	return res;
    }
    
    private static double get_NORR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double sum = 1e-7;
    	
    	double maxres = -1e5;

    	for(Operation o : workCenter.getQueue()) {
//    		sum += o.getNumOpsRemaining();
    		
    		if(o.getNumOpsRemaining() > maxres) {
    			maxres = o.getNumOpsRemaining();
    		}
    	}
//    	res = op.getNumOpsRemaining() / sum;
    	res = (op.getNumOpsRemaining()+1e-7) / (maxres+1e-7);
    	
    	return res;
    }
    
    private static double get_WKRR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double sum = 1e-7;
    	
    	double maxres = -1e5;

    	for(Operation o : workCenter.getQueue()) {
//    		sum += o.getWorkRemaining();
    		
    		if(o.getWorkRemaining() > maxres) {
    			maxres = o.getWorkRemaining();
    		}
    	}
//    	res = op.getWorkRemaining() / sum;
    	res = (op.getWorkRemaining()+1e-7) / (maxres+1e-7);
    	
    	return res;
    }
    
    private static double get_NNQR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double sum = 1e-7;
    	
    	double maxres = -1e5;

    	for(Operation o : workCenter.getQueue()) {
//    		sum += systemState.numOpsInNextQueue(o);
    		
    		if(systemState.numOpsInNextQueue(o) > maxres) {
    			maxres = systemState.numOpsInNextQueue(o);
    		}
    	}
//    	res = systemState.numOpsInNextQueue(op) / sum;
    	res = (systemState.numOpsInNextQueue(op)+1e-7) / (maxres+1e-7);
    	
    	return res;
    }
    
    private static double get_WNQR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double sum = 1e-7;
    	
    	double maxres = -1e5;

    	for(Operation o : workCenter.getQueue()) {
//    		sum += systemState.workInNextQueue(o);
    		
    		if(systemState.workInNextQueue(o) > maxres) {
    			maxres = systemState.workInNextQueue(o);
    		}
    	}
//    	res = systemState.workInNextQueue(op) / sum;
    	res = (systemState.workInNextQueue(op)+1e-7) / (maxres+1e-7);
    	
    	return res;
    }
    
    private static double get_OWTR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double sum = 1e-7;
    	
    	double maxres = -1e5;

    	for(Operation o : workCenter.getQueue()) {
//    		sum += systemState.getClockTime() - o.getReadyTime();
    		
    		if(systemState.getClockTime() - o.getReadyTime() > maxres) {
    			maxres = systemState.getClockTime() - o.getReadyTime();
    		}
    	}
//    	res = (systemState.getClockTime() - op.getReadyTime()) / sum;
    	res = (systemState.getClockTime() - op.getReadyTime()+1e-7) / (maxres+1e-7);
    	
    	return res;
    }
    
    private static double get_WR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double sum = 1e-7;
    	Set<Double> weights = new HashSet<>();
    	
    	for(Operation o : workCenter.getQueue()) {
    		if( !weights.contains(o.getJob().getWeight())) {
    			sum += o.getJob().getWeight();
    			weights.add(o.getJob().getWeight());
    		}
    		
    	}
    	res = op.getJob().getWeight() / sum;
    	
    	return res;
    }
    
    
    private static double get_rFDR(Operation op, WorkCenter workCenter, SystemState systemState) {
    	double res = 0;
    	double sum = 1e-7;
    	
    	double maxres = -1e5;

    	for(Operation o : workCenter.getQueue()) {
//    		sum += o.getFlowDueDate() - systemState.getClockTime();
    		
    		if(o.getFlowDueDate() - systemState.getClockTime() > maxres) {
    			maxres = o.getFlowDueDate() - systemState.getClockTime();
    		}
    	}
//    	res = (op.getFlowDueDate() - systemState.getClockTime()) / sum;
    	res = (op.getFlowDueDate() - systemState.getClockTime()+1e-7) / (maxres+1e-7);
    	
    	return res;
    }
}
