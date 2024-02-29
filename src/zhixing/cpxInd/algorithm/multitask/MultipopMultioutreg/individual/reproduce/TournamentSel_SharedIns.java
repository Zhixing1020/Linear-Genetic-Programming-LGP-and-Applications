package zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.reproduce;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import ec.EvolutionState;
import ec.Individual;
import ec.select.SelectDefaults;
import ec.select.TournamentSelection;
import ec.util.Parameter;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO;

public class TournamentSel_SharedIns extends TournamentSel_SpecificIns{
	
	public static final String P_TOURNAMENT_SHARED = "tournament_shared"; 
	
	public static final String P_SCORE_FUNCTION = "score_function";
	
	public static final int BEST = 0;
	public static final int HARMONIC = 1;
	public static final int GEOMETRIC = 2;
	public static final int ARITHMETIC = 3;
	public static final int QUADRATIC = 4;
	
	public String scoreType;
	
	protected int scoreFlag = -1;

	
//	public static final String P_SIZE2 = "size2";
//	
//	protected int second_size;
	
	
//    public boolean betterThan(Individual first, Individual second, int subpopulation, EvolutionState state, int thread, int tarSF)
//    {
//		return ((LGPIndividual_MPMO)first).scalarRank_v.get(tarSF) < ((LGPIndividual_MPMO)second).scalarRank_v.get(tarSF);
//    }
    
    public boolean betterThan(Individual first, Individual second, int subpopulation, EvolutionState state, int thread, int tarSF, double coeff)
    {
    	double score1 = ((LGPIndividual_MPMO)first).scalarRank_v.get(tarSF) * (1 - coeff) + getSecondBestScalarrank((LGPIndividual_MPMO)first, tarSF) * (coeff);
    	double score2 = ((LGPIndividual_MPMO)second).scalarRank_v.get(tarSF) * (1 - coeff) + getSecondBestScalarrank((LGPIndividual_MPMO)second, tarSF) * (coeff);
		return score1 < score2;
    }
    
    @Override
    public boolean betterThan(Individual first, Individual second, int subpopulation, EvolutionState state, int thread)
    {
		return getScore((LGPIndividual_MPMO)first) < getScore((LGPIndividual_MPMO)second);
    }
	
//	public Parameter defaultBase()
//    {
//		return SelectDefaults.base().push(P_TOURNAMENT_SPECIFIC);
//    }
//
	public void setup(final EvolutionState state, final Parameter base)
    {
	    super.setup(state,base);
	    
	    Parameter def = LGPDefaults.base().push(P_TOURNAMENT_SHARED);
	    
	    scoreType = state.parameters.getString(base.push(P_SCORE_FUNCTION),
	            def.push(P_SCORE_FUNCTION));
		if(scoreType == null) {
			state.output.fatal("TournamentSel_SharedIns Pipeline has an invalid score function.",base.push(P_SCORE_FUNCTION),
	            def.push(P_SCORE_FUNCTION));
		}
		if(scoreType.equals("best")) {
			scoreFlag = BEST;
		}
		else if(scoreType.equals("harmonic")) {
			scoreFlag = HARMONIC;
		}
		else if(scoreType.equals("geometric")){
			scoreFlag = GEOMETRIC;
		}
		else if(scoreType.equals("arithmetic")) {
			scoreFlag = ARITHMETIC;
		}
		else if(scoreType.equals("quadratic")) {
			scoreFlag = QUADRATIC;
		}
	
//	    double val = state.parameters.getDouble(base.push(P_SIZE2),def.push(P_SIZE2),1.0);
//	    if (val < 1.0)
//	        state.output.fatal("Tournament size must be >= 1.",base.push(P_SIZE),def.push(P_SIZE));
//	    else if (val == (int) val)  // easy, it's just an integer
//	        {
//	        second_size = (int) val;
//	        probabilityOfPickingSizePlusOne = 0.0;
//	        }
//	    else
//	        {
//	        second_size = (int) Math.floor(val);
//	        probabilityOfPickingSizePlusOne = val - size;  // for example, if we have 5.4, then the probability of picking *6* is 0.4
//	        }

    }
	
//	@Override
//	public int produce(final int subpopulation,
//	        final EvolutionState state,
//	        final int thread)
//	{
//	        // pick size random individuals, then pick the best.
//	        Individual[] oldinds = state.population.subpops[subpopulation].individuals;
//	        
//	        if(oldinds.length > 0 && ((LGPIndividual_MPMO)oldinds[0]).getOutputRegisters().length == 1){
//	        	return super.produce(subpopulation, state, thread);
//	        }
//	        else{
//	        	int tarSF = state.random[thread].nextInt(((MFEA_Evaluator)state.evaluator).getNumTasks());
//	        	
//	        	return this.produce(subpopulation, state, thread, tarSF);
//	        }
//	}
	
	@Override
	public int produce(final int subpopulation,
	        final EvolutionState state,
	        final int thread,
	        final int taskid)
	        {
		
			//int s = getTournamentSizeToUse(state.random[thread]);
//			Vector<Integer> candidates = new Vector<>(second_size);
			
	        // pick size random individuals, then pick the best.
	        Individual[] oldinds = state.population.subpops[subpopulation].individuals;
	        int best = 0; //getRandomIndividual(0, subpopulation, state, thread);
	        
	        if(((LGPIndividual_MPMO)oldinds[best]).getOutputRegisters().length==1){
	        	//return super.produce(subpopulation, state, thread);
	        	System.err.print("Tournament selection on shared instructions cannot be applied to subpopulation with one skillfactor while"
	        			+ "specifying a target skill factor argument\n");
	        	System.exit(1);
	        	return 0;
	        }
	        else{
//	        	int tarSF = taskid; //state.random[0].nextInt(((MFEA_Evaluator)state.evaluator).getNumTasks());
//	        	
//	    		for(int i = 0; i<second_size; i++){
//	    			//int cand = super.produce(subpopulation, state, thread, tarSF);
//	    			int cand = pickRndmIndividual4ATask(subpopulation, state, thread, tarSF);
//	    			if(candidates.contains(cand)){
//	    				//cand = getRandomIndividual(0, subpopulation, state, thread);
//	    				
//	    				int numTasks = ((MFEA_Evaluator)state.evaluator).getNumTasks();
//	    				int trial_taskid = tarSF;
//	    				if(numTasks > 1){
//	    					trial_taskid = (tarSF + (1 + state.random[0].nextInt(numTasks-1))) % numTasks;
//	    				}
//	    				cand = pickRndmIndividual4ATask(subpopulation, state, thread, tarSF);
//	    				
//	    			}
//	    			candidates.add(cand);
//	    		}
//	    		
//	    		//select the individual whose secondary scalarrank is the best
//	    		for (int x=1;x<candidates.size();x++)
//                {
//                if (getSecondBestScalarrank((LGPIndividual_MPMO)oldinds[candidates.get(x)], tarSF) 
//                		< getSecondBestScalarrank((LGPIndividual_MPMO)oldinds[candidates.get(best)], tarSF))  // j is better than best
//                    best = x;
//                }
	        	
	        	//return pickIndividualRandomLinearComScalarRnk(subpopulation, state, thread, taskid);
	        	
	        	// pick tournament-size random individuals, then pick the best.
	    		best = getRandomIndividual(0, subpopulation, state, thread);
	            
	            int s = getTournamentSizeToUse(state.random[thread]);
	                    
	            if (pickWorst)
	                for (int x=1;x<s;x++)
	                    {
	                    int j = getRandomIndividual(0, subpopulation, state, thread);
	                    if (!betterThan(oldinds[j], oldinds[best], subpopulation, state, thread))  // j is at least as bad as best
	                        best = j;
	                    }
	            else
	                for (int x=1;x<s;x++)
	                    {
	                    int j = getRandomIndividual(0, subpopulation, state, thread);
	                    if (betterThan(oldinds[j], oldinds[best], subpopulation, state, thread))  // j is better than best
	                        best = j;
	                    }
	                
	            return best;
	        }
	            
	        //return candidates.get(best);
		
		//return super.produce(subpopulation, state, thread);
	        }
	
//	public int pickRndmIndividual4ATask(final int subpopulation,
//	        final EvolutionState state,
//	        final int thread,
//	        final int taskid)
//	{
//		//collect the individuals from a certain skill factor
//		ArrayList<Integer> indexes = new ArrayList<>();
//		int tarSF = taskid;
//		
//        // pick tournament-size random individuals, then pick the best.
//		Individual[] oldinds = state.population.subpops[subpopulation].individuals;
//		int best = getRandomIndividual(0, subpopulation, state, thread);
//        
//        int s = getTournamentSizeToUse(state.random[thread]);
//                
//        if (pickWorst)
//            for (int x=1;x<s;x++)
//                {
//                int j = getRandomIndividual(0, subpopulation, state, thread);
//                if (!betterThan(oldinds[j], oldinds[best], subpopulation, state, thread, tarSF))  // j is at least as bad as best
//                    best = j;
//                }
//        else
//            for (int x=1;x<s;x++)
//                {
//                int j = getRandomIndividual(0, subpopulation, state, thread);
//                if (betterThan(oldinds[j], oldinds[best], subpopulation, state, thread, tarSF))  // j is better than best
//                    best = j;
//                }
//            
//        return best;
//	}
	
	public int pickIndividualRandomLinearComScalarRnk(final int subpopulation,
	        final EvolutionState state,
	        final int thread,
	        final int taskid)
	{
		//collect the individuals from a certain skill factor
		ArrayList<Integer> indexes = new ArrayList<>();
		int tarSF = taskid;
		
        // pick tournament-size random individuals, then pick the best.
		Individual[] oldinds = state.population.subpops[subpopulation].individuals;
		int best = getRandomIndividual(0, subpopulation, state, thread);
        
        int s = getTournamentSizeToUse(state.random[thread]);
        
        double coeff = state.random[thread].nextDouble();
                
        if (pickWorst)
            for (int x=1;x<s;x++)
                {
                int j = getRandomIndividual(0, subpopulation, state, thread);
                if (!betterThan(oldinds[j], oldinds[best], subpopulation, state, thread, tarSF, coeff))  // j is at least as bad as best
                    best = j;
                }
        else
            for (int x=1;x<s;x++)
                {
                int j = getRandomIndividual(0, subpopulation, state, thread);
                if (betterThan(oldinds[j], oldinds[best], subpopulation, state, thread, tarSF, coeff))  // j is better than best
                    best = j;
                }
            
        return best;
	}
	
	protected double getScore(LGPIndividual_MPMO ind){
		
		if(ind.scalarRank_v.size() == 0){
			System.err.print("the scalarRank vector of LGPIndividual_MPMO is empty in TournamentSel_SharedIns\n");
			System.exit(1);
		}
		
		switch(scoreFlag) {
		case BEST:
			return getBest(ind.scalarRank_v);
		case HARMONIC:
			return getHarmonicMean(ind.scalarRank_v);
		case GEOMETRIC:
			return getGeometricMean(ind.scalarRank_v);
		case ARITHMETIC:
			return getArithmeticMean(ind.scalarRank_v);
		case QUADRATIC:
			return getQuadraticMean(ind.scalarRank_v);
		default: 
			System.err.print("the scoreFlag is invalid in TournamentSel_SharedIns\n");
			System.exit(1);
			break;
		}
		
		return 0;
	}
	
	protected double getSecondBestScalarrank(LGPIndividual_MPMO ind, int tarSF){
		Vector<Double> scalar_v = ind.scalarRank_v;
		int best_i = 0;
		double min_scalar = 1e7;
		for(int i = 0; i<scalar_v.size();i++){
			if(i==tarSF) continue;
			
			if(scalar_v.get(i)<min_scalar){
				best_i = i;
				min_scalar = scalar_v.get(i);
			}
		}
		
		return scalar_v.get(best_i);
	}
	
	private double getBest(Vector<Double> scalarRanks) {
		double best = 1e7;
		for(Double v : scalarRanks){
			if(v < best) {
				best = v;
			}
		}
		return best;
	}
	
	private double getHarmonicMean(Vector<Double> scalarRanks) {
		double mean = 0;
		for(Double v : scalarRanks){
			mean += 1./(v+1e-4);
		}
		mean = scalarRanks.size() / mean;
		return mean;
	}
	
	private double getGeometricMean(Vector<Double> scalarRanks) {
		double mean = 1;
		for(Double v : scalarRanks){
			mean *= v;
		}
		mean = Math.pow(mean, 1./ scalarRanks.size()) ;
		return mean;
	}
	
	private double getArithmeticMean(Vector<Double> scalarRanks) {
		double mean = 0;
		for(Double v : scalarRanks){
			mean += v;
		}
		mean /= scalarRanks.size();
		return mean;
	}
	
	private double getQuadraticMean(Vector<Double> scalarRanks) {
		double mean = 0;
		for(Double v : scalarRanks){
			mean += v*v;
		}
		mean = Math.sqrt(mean / scalarRanks.size()) ;
		return mean;
	}
}
