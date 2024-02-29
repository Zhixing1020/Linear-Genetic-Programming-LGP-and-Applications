package zhixing.djss.dynamicoptimization.simulation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomDataGenerator;

import yimei.jss.jobshop.Job;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.DecisionSituation;
import yimei.util.random.AbstractIntegerSampler;
import yimei.util.random.AbstractRealSampler;
import yimei.util.random.ExponentialSampler;
import yimei.util.random.TwoSixTwoSampler;
import yimei.util.random.UniformIntegerSampler;
import yimei.util.random.UniformSampler;
import zhixing.djss.dynamicoptimization.jobshop.Job4DO;
import zhixing.djss.dynamicoptimization.jobshop.WorkCenter4DO;
import zhixing.djss.dynamicoptimization.simulation.events.AbstractEvent4DOInd;
import zhixing.djss.dynamicoptimization.simulation.events.JobArrivalEvent4DOInd;
import zhixing.djss.dynamicoptimization.simulation.state.SystemState4DO;
import zhixing.djss.individual.CpxGPInterface4DJSS;
import zhixing.djss.simulation.DynamicSimulation4Ind;
import zhixing.djss.simulation.Simulation4Ind;
import zhixing.djss.simulation.events.AbstractEvent4Ind;
import zhixing.djss.util.EnergyPriceSampler;
import zhixing.djss.util.EnergyRateSampler;
import zhixing.djss.util.UserReactionFactorSampler;

public class DOSimulation4Ind extends Simulation4Ind{

	protected int numAllFinishJobs = 0;

	protected List<Job> jobsFinished = new ArrayList<>();

    public final static int SEED_ROTATION = 10000;
    
    public final static String DEFAULT = "default";
    public final static String ENERGY = "energy";
    public final static String ADVANCE = "advance";

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
    
	protected AbstractRealSampler energypriceSampler;
	protected AbstractRealSampler energyrateSampler;
	protected AbstractRealSampler userreactionFactorSampler;
	
	protected final int update_cycle;
	private String mode = "default";
	private double obj1cof = 0.5;
	private double obj2cof = 0.3;
	private double obj3cof = 0.3;
	
    private DOSimulation4Ind(long seed,
            CpxGPInterface4DJSS indi,
            int numWorkCenters,
            int numJobsRecorded,
            int warmupJobs,
            int minNumOps,
            int maxNumOps,
            double utilLevel,
            double dueDateFactor,
            boolean revisit,
            int cycle,
            String mode,
            AbstractIntegerSampler numOpsSampler,
            AbstractRealSampler procTimeSampler,
            AbstractRealSampler interArrivalTimeSampler,
            AbstractRealSampler jobWeightSampler,
            AbstractRealSampler energypriceSampler,
            AbstractRealSampler energyRateSampler,
            AbstractRealSampler satisfactorySampler) {
		super(indi, numWorkCenters, numJobsRecorded, warmupJobs);
		
		this.seed = seed;
		this.randomDataGenerator = new RandomDataGenerator();
		this.randomDataGenerator.reSeed(seed);
		
		this.minNumOps = minNumOps;
		this.maxNumOps = maxNumOps;
		this.utilLevel_tmp=this.utilLevel = utilLevel;
		this.dueDateFactor = dueDateFactor;
		this.revisit = revisit;
		this.mode = mode;
		
		this.numOpsSampler = numOpsSampler;
		this.procTimeSampler = procTimeSampler;
		this.interArrivalTimeSampler = interArrivalTimeSampler;
		this.jobWeightSampler = jobWeightSampler;
		this.energypriceSampler = energypriceSampler;
		this.energyrateSampler = energyRateSampler;
		this.userreactionFactorSampler = satisfactorySampler;
		
		this.update_cycle = cycle;
		
		setInterArrivalTimeSamplerMean();
		
		systemState = new SystemState4DO();
		
		// Create the work centers, with empty queue and ready to go initially.
        for (int i = 0; i < numWorkCenters; i++) {
            systemState.addWorkCenter(new WorkCenter4DO(i));
        }

        setup();
	}
    
    public DOSimulation4Ind(long seed, CpxGPInterface4DJSS indi, int numWorkCenters, int numJobsRecorded, int warmupJobs,
			int minNumOps, int maxNumOps, double utilLevel, double dueDateFactor, boolean revisit, int cycle, String mode) {
    	this(seed, indi, numWorkCenters, numJobsRecorded, warmupJobs,
                minNumOps, maxNumOps, utilLevel, dueDateFactor, revisit,cycle, mode,
                new UniformIntegerSampler(minNumOps, maxNumOps),
                new UniformSampler(1, 99),
                new ExponentialSampler(),
                new TwoSixTwoSampler(),
                new EnergyPriceSampler(),
                new EnergyRateSampler(),
                new UserReactionFactorSampler());

		
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
    
    public double meanEnergyCost() {
    	double value = 0.0;
    	for(WorkCenter wCenter : systemState.getWorkCenters()) {
    		value += ((WorkCenter4DO)wCenter).getEnergyCost();
    	}
    	return value / (systemState.getWorkCenters().size()*numJobsRecorded);
    }
    
    public double meanUserReaction() {
    	double value = 0.0;
    	
    	for (Job job : systemState.getJobsCompleted()) {
            value += ((Job4DO)job).getResponseTime();
        }

        return value / numJobsRecorded;
    }
    
    public double maxUserReaction() {
    	double value = 0.0;
    	for (Job job : systemState.getJobsCompleted()) {
            double tmp = ((Job4DO)job).getResponseTime();
            
            if(tmp > value) {
            	value = tmp;
            }
        }
    	
    	return value;
    }
    
    public double meanWeightedUserReaction() {
    	double value = 0.0;
    	
    	for (Job job : systemState.getJobsCompleted()) {
            value += ((Job4DO)job).getResponseTime()*job.getWeight();
        }

        return value / numJobsRecorded;
    }
    
    @Override
    public double objectiveValue(Objective objective) {
    	
    	double res = 0;
    	
        switch (objective) {
            case MAKESPAN:
                res = makespan();break;
            case MEAN_FLOWTIME:
                res = meanFlowtime(); break;
            case MAX_FLOWTIME:
                res = maxFlowtime(); break;
            case MEAN_WEIGHTED_FLOWTIME:
                res = meanWeightedFlowtime(); break;
            case MAX_WEIGHTED_FLOWTIME:
                res = maxWeightedFlowtime(); break;
            case MEAN_TARDINESS:
                res = meanTardiness(); break;
            case MAX_TARDINESS:
                res = maxTardiness(); break;
            case MEAN_WEIGHTED_TARDINESS:
                res = meanWeightedTardiness(); break;
            case MAX_WEIGHTED_TARDINESS:
                res = maxWeightedTardiness(); break;
            case PROP_TARDY_JOBS:
                res = propTardyJobs(); break;
                
            case MEAN_ENERGY_COST:
            	res = meanEnergyCost(); break;
            case MEAN_SATISFACTORY:
            	res = meanUserReaction(); break;
            case MAX_SATISFACTORY:
            	res = maxUserReaction(); break;
            case MEAN_WEIGHTED_SATISFACTORY:
            	res = meanWeightedUserReaction(); break;
            default:
            	return -1.;
        }
        
        if(mode.equals(ENERGY)) {
        	res = (1 - obj1cof) * res + obj1cof * meanEnergyCost();
        }
        else if(mode.equals(ADVANCE)) {
        	res = (1 - obj2cof - obj3cof) * res + obj2cof * meanEnergyCost() + obj3cof * meanUserReaction();
        }
//        res = obj1cof * res + obj2cof * meanEnergyCost();
//        res = obj1cof * res + obj2cof * meanEnergyCost() + obj3cof * meanUserReaction();
        
        return res;
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
        
        double cost_rate = energyrateSampler.next(randomDataGenerator);
        
        Job4DO job = new Job4DO(numJobsArrived, new ArrayList<>(),
                arrivalTime, arrivalTime, 0, weight, 0., cost_rate);
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

        eventQueue.add(new JobArrivalEvent4DOInd(job));
 
	      //=================2023.4.17   zhixing  check the progress of the job shop
        //record NIQR, WIQR, DPT, DOWT, DMRT, BWR   
//        double NIQR = get_NIQR(null, systemState.getWorkCenter(0), systemState);
//        double WIQR = get_WIQR(null, systemState.getWorkCenter(0), systemState);
//        double SIQR = get_WKRR(null, systemState.getWorkCenter(0), systemState);
//        double DPT = get_DPT(null, systemState.getWorkCenter(0), systemState);
//        double DOWT = get_DOWT(null, systemState.getWorkCenter(0), systemState);
//        double DMRT = get_DMRT(null, systemState.getWorkCenter(0), systemState);
//        double BWR = get_BWR(null, systemState.getWorkCenter(0), systemState);
//        
//        
////        System.out.print(""+numJobsArrived+"\t"+NIQR+"\t"+WIQR+"\t"+DPT+"\t"
////        		+DOWT+"\t"+DMRT+"\t"+BWR+"\n");
//        
//        
////        File f = new File("globalFeatureOverSim.txt");
//        try {				
//			BufferedWriter out = new BufferedWriter(new FileWriter("globalFeatureOverSim.txt",true));
//            out.write(""+((SystemState4DO)systemState).getEnergyPrice() / 1.5 + "\t" + ((SystemState4DO)systemState).getUserReactiveFactor() / 2.0 + "\n");
//            out.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        
        //======================
        
        //multi-utilization level
        if(numJobsArrived % update_cycle == 0 && numJobsArrived>1) {
//        	this.utilLevel_tmp = randomDataGenerator.nextUniform(this.utilLevel-0.2, this.utilLevel+0.2);
        	
        	//alter energy price
        	((SystemState4DO)systemState).setEnergyPrice(energypriceSampler.next(randomDataGenerator));
        	
        	if(mode.equals(ADVANCE)) {
        		//alter user reaction factor
            	((SystemState4DO)systemState).setUserReactiveFactor(userreactionFactorSampler.next(randomDataGenerator));
        	}
        	
        }
        
        if(numJobsArrived == warmupJobs) {
        	((SystemState4DO)systemState).resetEnergyCost();
        }
    }
	

	public void completeJob(Job4DO job) {
        if (numJobsArrived > warmupJobs && job.getId() >= 0
                && job.getId() < numJobsRecorded + warmupJobs) {
            throughput++;

            systemState.addCompletedJob(job);
            ((SystemState4DO)systemState).addCompletedJob4Stage(job);
        }
        
        systemState.removeJobFromSystem(job);
    }
	
	public AbstractRealSampler getEnergyPriceSampler() {
        return energypriceSampler;
    }
	
	public AbstractRealSampler getJobEnergyRateSampler() {
        return energyrateSampler;
    }
	
	public AbstractRealSampler getUserReactionFactorSampler() {
        return userreactionFactorSampler;
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
	public Simulation4Ind surrogate(int numWorkCenters, int numJobsRecorded, int warmupJobs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Simulation4Ind surrogateBusy(int numWorkCenters, int numJobsRecorded, int warmupJobs) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static DOSimulation4Ind standardMissing(
            long seed,
            CpxGPInterface4DJSS indi,
            int numWorkCenters,
            int numJobsRecorded,
            int warmupJobs,
            double utilLevel,
            double dueDateFactor,
            int cycle,
            String mode) {
        return new DOSimulation4Ind(seed, indi, numWorkCenters, numJobsRecorded,
                warmupJobs, 2, numWorkCenters, utilLevel, dueDateFactor, false, cycle, mode);
    }
}
