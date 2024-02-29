package zhixing.djss.simulation;

import java.awt.Taskbar.State;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomDataGenerator;

import ec.EvolutionState;
import yimei.jss.jobshop.Job;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.DecisionSituation;
import yimei.jss.simulation.state.SystemState;
import yimei.util.random.AbstractIntegerSampler;
import yimei.util.random.AbstractRealSampler;
import yimei.util.random.ExponentialSampler;
import yimei.util.random.TwoSixTwoSampler;
import yimei.util.random.UniformIntegerSampler;
import yimei.util.random.UniformSampler;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.simulation.events.AbstractEvent4Ind;
import zhixing.djss.simulation.events.JobArrivalEvent4Ind;

public class DynamicSimulation4Ind extends Simulation4Ind {
	//lots of private attributes in DynamicSimulation do not be contained in this subclass
		//can change them to protected or use some wrapper method to achieve them
		protected int numAllFinishJobs = 0;

		protected List<Job> jobsFinished = new ArrayList<>();

	    public final static int SEED_ROTATION = 10000;

	    protected long seed;
	    protected RandomDataGenerator randomDataGenerator;

	    protected final int minNumOps;
	    protected final int maxNumOps;
	    protected final double utilLevel;
	    protected double utilLevel_tmp;
	    protected final double dueDateFactor;
	    protected final boolean revisit;

	    protected AbstractIntegerSampler numOpsSampler;
	    protected AbstractRealSampler procTimeSampler;
	    protected AbstractRealSampler interArrivalTimeSampler;
	    protected AbstractRealSampler jobWeightSampler;

	    private DynamicSimulation4Ind(long seed,
	                              CpxGPInterface4DJSS indi,
	                              int numWorkCenters,
	                              int numJobsRecorded,
	                              int warmupJobs,
	                              int minNumOps,
	                              int maxNumOps,
	                              double utilLevel,
	                              double dueDateFactor,
	                              boolean revisit,
	                              AbstractIntegerSampler numOpsSampler,
	                              AbstractRealSampler procTimeSampler,
	                              AbstractRealSampler interArrivalTimeSampler,
	                              AbstractRealSampler jobWeightSampler) {
	        super(indi, numWorkCenters, numJobsRecorded, warmupJobs);

	        this.seed = seed;
	        this.randomDataGenerator = new RandomDataGenerator();
	        this.randomDataGenerator.reSeed(seed);

	        this.minNumOps = minNumOps;
	        this.maxNumOps = maxNumOps;
	        this.utilLevel_tmp=this.utilLevel = utilLevel;
	        this.dueDateFactor = dueDateFactor;
	        this.revisit = revisit;

	        this.numOpsSampler = numOpsSampler;
	        this.procTimeSampler = procTimeSampler;
	        this.interArrivalTimeSampler = interArrivalTimeSampler;
	        this.jobWeightSampler = jobWeightSampler;

	        setInterArrivalTimeSamplerMean();

	        // Create the work centers, with empty queue and ready to go initially.
	        for (int i = 0; i < numWorkCenters; i++) {
	            systemState.addWorkCenter(new WorkCenter(i));
	        }

	        setup();
	    }

	    public DynamicSimulation4Ind(long seed,
	                             CpxGPInterface4DJSS indi,
	                             int numWorkCenters,
	                             int numJobsRecorded,
	                             int warmupJobs,
	                             int minNumOps,
	                             int maxNumOps,
	                             double utilLevel,
	                             double dueDateFactor,
	                             boolean revisit) {
	        this(seed, indi, numWorkCenters, numJobsRecorded, warmupJobs,
	                minNumOps, maxNumOps, utilLevel, dueDateFactor, revisit,
	                new UniformIntegerSampler(minNumOps, maxNumOps),
	                new UniformSampler(1, 99),
	                new ExponentialSampler(),
	                new TwoSixTwoSampler());
	    }

	    public int getNumWorkCenters() {
	        return numWorkCenters;
	    }

	    public int getNumJobsRecorded() {
	        return numJobsRecorded;
	    }

	    public int getWarmupJobs() {
	        return warmupJobs;
	    }

	    public int getMinNumOps() {
	        return minNumOps;
	    }

	    public int getMaxNumOps() {
	        return maxNumOps;
	    }

	    public double getUtilLevel() {
	        return utilLevel;
	    }

	    public double getDueDateFactor() {
	        return dueDateFactor;
	    }

	    public boolean isRevisit() {
	        return revisit;
	    }

	    public RandomDataGenerator getRandomDataGenerator() {
	        return randomDataGenerator;
	    }

	    public AbstractIntegerSampler getNumOpsSampler() {
	        return numOpsSampler;
	    }

	    public AbstractRealSampler getProcTimeSampler() {
	        return procTimeSampler;
	    }

	    public AbstractRealSampler getInterArrivalTimeSampler() {
	        return interArrivalTimeSampler;
	    }

	    public AbstractRealSampler getJobWeightSampler() {
	        return jobWeightSampler;
	    }

	    @Override
	    public void setup() {
	        numJobsArrived = 0;
	        throughput = 0;
	        generateJob();
	    }

	    @Override
	    public void resetState() {
	        systemState.reset();
	        eventQueue.clear();

	        setup();
	    }

	    @Override
	    public void reset() {
	        reset(seed);
	    }

	    public void reset(long seed) {
	        reseed(seed);
	        resetState();
	    }

	    public void reseed(long seed) {
	        this.seed = seed;
	        randomDataGenerator.reSeed(seed);
	    }

	    @Override
	    public void rotateSeed() {
	        seed += SEED_ROTATION; 
	        //seed = (seed + SEED_ROTATION) % (51*SEED_ROTATION);
	        reset();
	    }

	    @Override
	    public void generateJob() {
	        double arrivalTime = getClockTime()
	                + interArrivalTimeSampler.next(randomDataGenerator);
	        double weight = jobWeightSampler.next(randomDataGenerator);
	        Job job = new Job(numJobsArrived, new ArrayList<>(),
	                arrivalTime, arrivalTime, 0, weight);
	        int numOps = numOpsSampler.next(randomDataGenerator);

	        int[] route = randomDataGenerator.nextPermutation(numWorkCenters, numOps);

	        double totalProcTime = 0.0;
	        for (int i = 0; i < numOps; i++) {
	            double procTime = procTimeSampler.next(randomDataGenerator);
	            totalProcTime += procTime;

	            Operation o = new Operation(job, i, procTime, systemState.getWorkCenter(route[i]));

	            job.addOperation(o);
	        }

	        job.linkOperations();

	        double dueDate = job.getReleaseTime() + dueDateFactor * totalProcTime;
	        job.setDueDate(dueDate);

	        systemState.addJobToSystem(job);
	        numJobsArrived ++;

	        eventQueue.add(new JobArrivalEvent4Ind(job));
	        
	      //=================2023.4.17   zhixing  check the progress of the job shop
	        //record NIQR, WIQR, DPT, DOWT, DMRT, BWR   
//	        double NIQR = get_NIQR(null, systemState.getWorkCenter(0), systemState);
//	        double WIQR = get_WIQR(null, systemState.getWorkCenter(0), systemState);
//	        double SIQR = get_WKRR(null, systemState.getWorkCenter(0), systemState);
//	        double DPT = get_DPT(null, systemState.getWorkCenter(0), systemState);
//	        double DOWT = get_DOWT(null, systemState.getWorkCenter(0), systemState);
//	        double DMRT = get_DMRT(null, systemState.getWorkCenter(0), systemState);
//	        double BWR = get_BWR(null, systemState.getWorkCenter(0), systemState);
//	        
//	        
////	        System.out.print(""+numJobsArrived+"\t"+NIQR+"\t"+WIQR+"\t"+DPT+"\t"
////	        		+DOWT+"\t"+DMRT+"\t"+BWR+"\n");
//	        
//	        
////	        File f = new File("globalFeatureOverSim.txt");
//	        try {				
//				BufferedWriter out = new BufferedWriter(new FileWriter("globalFeatureOverSim.txt",true));
//	            out.write(""+numJobsArrived+"\t"+NIQR+"\t"+WIQR+"\t"+SIQR+"\t"+DPT+"\t"
//		        		+DOWT+"\t"+DMRT+"\t"+BWR+"\n");
//	            out.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	        
	        //======================
	        
	        //multi-utilization level
//	        if(numJobsArrived % 500 == 0 && numJobsArrived>1) {
////	        	this.utilLevel_tmp = this.utilLevel;
//	        	this.utilLevel_tmp = randomDataGenerator.nextUniform(this.utilLevel-0.2, this.utilLevel+0.2);
//	        }
	    }

	    public double interArrivalTimeMean(int numWorkCenters,
	                                             int minNumOps,
	                                             int maxNumOps,
	                                             double utilLevel) {
	        double meanNumOps = 0.5 * (minNumOps + maxNumOps);
	        double meanProcTime = procTimeSampler.getMean();

	        return (meanNumOps * meanProcTime) / (utilLevel * numWorkCenters);
	    }

	    public void setInterArrivalTimeSamplerMean() {
	        double mean = interArrivalTimeMean(numWorkCenters, minNumOps, maxNumOps, utilLevel_tmp);
	        interArrivalTimeSampler.setMean(mean);
	    }

	    public List<DecisionSituation> decisionSituations(int minQueueLength) {
	        List<DecisionSituation> decisionSituations = new ArrayList<>();

	        while (!eventQueue.isEmpty() && throughput < numJobsRecorded) {
	            AbstractEvent4Ind nextEvent = eventQueue.poll();

	            systemState.setClockTime(nextEvent.getTime());
	            nextEvent.addDecisionSituation(this, decisionSituations, minQueueLength);
	        }

	        resetState();

	        return decisionSituations;
	    }

	    @Override
	    public Simulation4Ind surrogate(int numWorkCenters, int numJobsRecorded,
	                                       int warmupJobs) {
	        int surrogateMaxNumOps = maxNumOps;
	        AbstractIntegerSampler surrogateNumOpsSampler = numOpsSampler.clone();
	        AbstractRealSampler surrogateInterArrivalTimeSampler = interArrivalTimeSampler.clone();
	        if (surrogateMaxNumOps > numWorkCenters) {
	            surrogateMaxNumOps = numWorkCenters;
	            surrogateNumOpsSampler.setUpper(surrogateMaxNumOps);

	            surrogateInterArrivalTimeSampler.setMean(interArrivalTimeMean(numWorkCenters,
	                    minNumOps, surrogateMaxNumOps, utilLevel));
	        }

	        Simulation4Ind surrogate = new DynamicSimulation4Ind(seed, individual, numWorkCenters,
	                numJobsRecorded, warmupJobs, minNumOps, surrogateMaxNumOps,
	                utilLevel, dueDateFactor, revisit, surrogateNumOpsSampler,
	                procTimeSampler, surrogateInterArrivalTimeSampler, jobWeightSampler);

	        return surrogate;
	    }

	    @Override
	    public Simulation4Ind surrogateBusy(int numWorkCenters, int numJobsRecorded,
	                                int warmupJobs) {
	        double utilLevel = 1;
	        int surrogateMaxNumOps = maxNumOps;
	        AbstractIntegerSampler surrogateNumOpsSampler = numOpsSampler.clone();
	        AbstractRealSampler surrogateInterArrivalTimeSampler = interArrivalTimeSampler.clone();
	        if (surrogateMaxNumOps > numWorkCenters) {
	            surrogateMaxNumOps = numWorkCenters;
	            surrogateNumOpsSampler.setUpper(surrogateMaxNumOps);

	            surrogateInterArrivalTimeSampler.setMean(interArrivalTimeMean(numWorkCenters,
	                    minNumOps, surrogateMaxNumOps, utilLevel));
	        }

	        Simulation4Ind surrogate = new DynamicSimulation4Ind(seed, individual, numWorkCenters,
	                numJobsRecorded, warmupJobs, minNumOps, surrogateMaxNumOps,
	                utilLevel, dueDateFactor, revisit, surrogateNumOpsSampler,
	                procTimeSampler, surrogateInterArrivalTimeSampler, jobWeightSampler);

	        return surrogate;
	    }

	    public static DynamicSimulation4Ind standardFull(
	            long seed,
	            CpxGPInterface4DJSS indi,
	            int numWorkCenters,
	            int numJobsRecorded,
	            int warmupJobs,
	            double utilLevel,
	            double dueDateFactor) {
	        return new DynamicSimulation4Ind(seed, indi, numWorkCenters, numJobsRecorded,
	                warmupJobs, numWorkCenters, numWorkCenters, utilLevel,
	                dueDateFactor, false);
	    }

	    public static DynamicSimulation4Ind standardMissing(
	            long seed,
	            CpxGPInterface4DJSS indi,
	            int numWorkCenters,
	            int numJobsRecorded,
	            int warmupJobs,
	            double utilLevel,
	            double dueDateFactor) {
	        return new DynamicSimulation4Ind(seed, indi, numWorkCenters, numJobsRecorded,
	                warmupJobs, 2, numWorkCenters, utilLevel, dueDateFactor, false);
	    }
	    
	    
	    //=====================2023.4.17   zhixing  check the progress of the job shop
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
	    	
	    	res = (minSIQ - minSIQ_shop) / (maxSIQ_shop - minSIQ_shop);
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
	    private static double get_NNQR(Operation op, WorkCenter workCenter, SystemState systemState) {
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
	    	
	    	//find max PT in the queue
	    	double tmp;
	    	for(Operation p : workCenter.getQueue()) {
	    		tmp = p.getJob().getDueDate() - systemState.getClockTime() - p.getWorkRemaining();
	    		if(tmp>maxSIQ) maxSIQ = tmp;
	    		if(tmp<minSIQ) minSIQ = tmp;
	    	}
	    	
	    	res = minSIQ / maxSIQ;
	    	res = (res / (Math.sqrt(res*res + 1)) + 1)/2;
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
	    	
	    	res = (minrDD - minrDD_shop) / (maxrDD_shop - minrDD_shop);
	    	return res;
		}
		 
		 private static double get_WKRR(Operation op, WorkCenter workCenter, SystemState systemState) {
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
	    //======================================
}
