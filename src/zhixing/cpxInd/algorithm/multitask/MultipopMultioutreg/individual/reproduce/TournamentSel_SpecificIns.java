package zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.reproduce;

import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import ec.select.SelectDefaults;
import ec.select.TournamentSelection;
import ec.util.Parameter;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator;
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA;
import zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO;

public class TournamentSel_SpecificIns extends TournamentSelection{
//	public static final String P_TOURNAMENT_SHARED = "tournament_shared";
//	
//	public Parameter defaultBase()
//    {
//		return SelectDefaults.base().push(P_TOURNAMENT_SHARED);
//    }
//
	public void setup(final EvolutionState state, final Parameter base)
    {
	    super.setup(state,base);
	    
//	    Parameter def = defaultBase();
//	
//	    double val = state.parameters.getDouble(base.push(P_SIZE),def.push(P_SIZE),1.0);
//	    if (val < 1.0)
//	        state.output.fatal("Tournament size must be >= 1.",base.push(P_SIZE),def.push(P_SIZE));
//	    else if (val == (int) val)  // easy, it's just an integer
//	        {
//	        size = (int) val;
//	        probabilityOfPickingSizePlusOne = 0.0;
//	        }
//	    else
//	        {
//	        size = (int) Math.floor(val);
//	        probabilityOfPickingSizePlusOne = val - size;  // for example, if we have 5.4, then the probability of picking *6* is 0.4
//	        }

    }
	
	@Override
    public boolean betterThan(Individual first, Individual second, int subpopulation, EvolutionState state, int thread)
        {
        return ((LGPIndividual_MFEA)first).scalarRank < ((LGPIndividual_MFEA)second).scalarRank;
        }
	
	public boolean betterThan(Individual first, Individual second, int subpopulation, EvolutionState state, int thread, int tarSF)
    {
		
		double f1 = ((LGPIndividual_MPMO)first).scalarRank_v.get(0);
		if(((LGPIndividual_MPMO)first).scalarRank_v.size() > 1 && tarSF < ((LGPIndividual_MPMO)first).scalarRank_v.size()){
			f1 = ((LGPIndividual_MPMO)first).scalarRank_v.get(tarSF);
		}
		
		double f2 = ((LGPIndividual_MPMO)second).scalarRank_v.get(0);
		if(((LGPIndividual_MPMO)second).scalarRank_v.size() > 1 && tarSF < ((LGPIndividual_MPMO)second).scalarRank_v.size()){
			f2 = ((LGPIndividual_MPMO)second).scalarRank_v.get(tarSF);
		}
		return f1 < f2;
    }
	
	@Override
	public int produce(final int subpopulation,
	        final EvolutionState state,
	        final int thread)
	{
	        // pick size random individuals, then pick the best.
	        Individual[] oldinds = state.population.subpops[subpopulation].individuals;
	        
	        if(oldinds.length > 0 && ((LGPIndividual_MPMO)oldinds[0]).getOutputRegisters().length == 1){
	        	return super.produce(subpopulation, state, thread);
	        }
	        else{
	        	int tarSF = state.random[thread].nextInt(((MFEA_Evaluator)state.evaluator).getNumTasks());
	        	
	        	return this.produce(subpopulation, state, thread, tarSF);
	        }
	}
	
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread,
	        final int taskid) 
	        {
	        int n=INDS_PRODUCED;
	        if (n<min) n = min;
	        if (n>max) n = max;
	        
	        //int tarSF = state.random[0].nextInt(((MFEA_Evaluator)state.evaluator).getNumTasks());
	        
	        for(int q=0;q<n;q++){
	        	inds[start+q] = state.population.subpops[subpopulation].
		                individuals[produce(subpopulation,state,thread, taskid)];
	        }
	            
	        return n;
	        }
	
	public int produce_merge(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final int trialsubpop,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread,
	        final int taskid) 
	        {
	        int n=INDS_PRODUCED;
	        if (n<min) n = min;
	        if (n>max) n = max;
	        
	        //int tarSF = state.random[0].nextInt(((MFEA_Evaluator)state.evaluator).getNumTasks());
	        
	        for(int q=0;q<n;q++){
	        	inds[start+q] = produce_merge(subpopulation, trialsubpop, state, thread, taskid);
	        }
	            
	        return n;
	        }
	
	public int produce(final int subpopulation,
	        final EvolutionState state,
	        final int thread,
	        final int taskid)
	{
//		//collect the individuals from a certain skill factor
//		ArrayList<Integer> indexes = new ArrayList<>();
//		int tarSF = taskid;
//		for(int i = 0;i<state.population.subpops[subpopulation].individuals.length;i++){
//			if(((LGPIndividual_MFEA)state.population.subpops[subpopulation].individuals[i]).skillFactor == tarSF){
//				indexes.add(i);
//			}
//		}
//		
//		if(indexes.size()==0){
//			System.err.print("cannot find the given skillfactor "+tarSF+" in TournamentSelection_SpecificIns\n");
//			System.exit(1);
//		}
//		
//        // pick tournament-size random individuals, then pick the best.
//		Individual[] oldinds = state.population.subpops[subpopulation].individuals;
//        int best = getRandomIndex(0, indexes, state, thread);
//        
//        int s = getTournamentSizeToUse(state.random[thread]);
//                
//        if (pickWorst)
//            for (int x=1;x<s;x++)
//                {
//                int j = getRandomIndex(x, indexes, state, thread);
//                if (!betterThan(oldinds[j], oldinds[best], subpopulation, state, thread, tarSF))  // j is at least as bad as best
//                    best = j;
//                }
//        else
//            for (int x=1;x<s;x++)
//                {
//                int j = getRandomIndex(x, indexes, state, thread);
//                if (betterThan(oldinds[j], oldinds[best], subpopulation, state, thread, tarSF))  // j is better than best
//                    best = j;
//                }
//            
//        return best;
		
		return pickRndmIndividual4ATask(subpopulation, state, thread, taskid);
	}
	
	
	public LGPIndividual produce_merge(final int subpopulation,
			final int trialsubpop,
	        final EvolutionState state,
	        final int thread,
	        final int taskid){
		
		int tarSF = taskid;
		
        // pick tournament-size random individuals, then pick the best.
		Individual[] oldinds1 = state.population.subpops[subpopulation].individuals;
		Individual[] oldinds2 = state.population.subpops[trialsubpop].individuals;
		
		double threshold = (double) oldinds1.length / (oldinds1.length + oldinds2.length);
		
		LGPIndividual best = (LGPIndividual) oldinds1[getRandomIndividual(0, subpopulation, state, thread)];
		if(state.random[thread].nextDouble() < threshold){
			best = (LGPIndividual) oldinds1[getRandomIndividual(0, subpopulation, state, thread)];
		}
		else{
			best = (LGPIndividual) oldinds2[getRandomIndividual(0, trialsubpop, state, thread)];
		}
		
        
        int s = getTournamentSizeToUse(state.random[thread]);
        LGPIndividual cand;
                
        if (pickWorst)
            for (int x=1;x<s;x++)
                {
	            	if(state.random[thread].nextDouble() < threshold){
	        			cand = (LGPIndividual) oldinds1[getRandomIndividual(0, subpopulation, state, thread)];
	        		}
	        		else{
	        			cand = (LGPIndividual) oldinds2[getRandomIndividual(0, trialsubpop, state, thread)];
	        		}
	                if (!betterThan(cand, best, subpopulation, state, thread, tarSF))  // j is at least as bad as best
	                    best = cand;
                }
        else
            for (int x=1;x<s;x++)
                {
	            	if(state.random[thread].nextDouble() < threshold){
	        			cand = (LGPIndividual) oldinds1[getRandomIndividual(0, subpopulation, state, thread)];
	        		}
	        		else{
	        			cand = (LGPIndividual) oldinds2[getRandomIndividual(0, trialsubpop, state, thread)];
	        		}
	                if (betterThan(cand, best, subpopulation, state, thread, tarSF))  // j is at least as bad as best
	                    best = cand;
                }
            
        return best;
		
	}
	
	protected int getRandomIndex(int number, List pop, EvolutionState state, int thread)
    {
		return (int) pop.get(state.random[thread].nextInt(pop.size())) ;
    }
	
	protected int pickRndmIndividual4ATask(final int subpopulation,
	        final EvolutionState state,
	        final int thread,
	        final int taskid)
	{
		//collect the individuals from a certain skill factor
		//ArrayList<Integer> indexes = new ArrayList<>();
		int tarSF = taskid;
		
        // pick tournament-size random individuals, then pick the best.
		Individual[] oldinds = state.population.subpops[subpopulation].individuals;
		int best = getRandomIndividual(0, subpopulation, state, thread);
        
        int s = getTournamentSizeToUse(state.random[thread]);
                
        if (pickWorst)
            for (int x=1;x<s;x++)
                {
                int j = getRandomIndividual(0, subpopulation, state, thread);
                if (!betterThan(oldinds[j], oldinds[best], subpopulation, state, thread, tarSF))  // j is at least as bad as best
                    best = j;
                }
        else
            for (int x=1;x<s;x++)
                {
                int j = getRandomIndividual(0, subpopulation, state, thread);
                if (betterThan(oldinds[j], oldinds[best], subpopulation, state, thread, tarSF))  // j is better than best
                    best = j;
                }
            
        return best;
	}
	
	
}
