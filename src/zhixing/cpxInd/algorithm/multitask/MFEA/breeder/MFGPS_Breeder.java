package zhixing.cpxInd.algorithm.multitask.MFEA.breeder;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.simple.SimpleBreeder;
import ec.util.Parameter;
import ec.util.QuickSort;
import ec.util.SortComparatorL;
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA;

public class MFGPS_Breeder extends MFEA_Breeder{
	
	@Override
	protected void loadElites(EvolutionState state, Population newpop)
    {
    // are our elites small enough?
    for(int x=0;x<state.population.subpops.length;x++)
        {
        if (numElites(state, x)>state.population.subpops[x].individuals.length)
            state.output.error("The number of elites for subpopulation " + x + " exceeds the actual size of the subpopulation", 
                new Parameter(EvolutionState.P_BREEDER).push(P_ELITE).push(""+x));
        if (numElites(state, x)==state.population.subpops[x].individuals.length)
            state.output.warning("The number of elites for subpopulation " + x + " is the actual size of the subpopulation", 
                new Parameter(EvolutionState.P_BREEDER).push(P_ELITE).push(""+x));
        }
    state.output.exitIfErrors();

    // we assume that we're only grabbing a small number (say <10%), so
    // it's not being done multithreaded
    for(int sub=0;sub<state.population.subpops.length;sub++) 
        {
        if (!shouldBreedSubpop(state, sub, 0))  // don't load the elites for this one, we're not doing breeding of it
            {
            continue;
            }
                    
        // if the number of elites is 1, then we handle this by just finding the best one.
        if (numElites(state, sub)==1)
            {
            int best = 0;
            Individual[] oldinds = state.population.subpops[sub].individuals;
            for(int x=1;x<oldinds.length;x++)
                if (oldinds[x].fitness.betterThan(oldinds[best].fitness))
                    best = x;
            Individual[] inds = newpop.subpops[sub].individuals;
            inds[inds.length-1] = (Individual)(oldinds[best].clone());
            }
        else if (numElites(state, sub)>0)  // we'll need to sort
            {
            int[] orderedPop = new int[state.population.subpops[sub].individuals.length];
            for(int x=0;x<state.population.subpops[sub].individuals.length;x++) orderedPop[x] = x;

            // sort the best so far where "<" means "not as fit as"
            QuickSort.qsort(orderedPop, new EliteComparator(state.population.subpops[sub].individuals));
            // load the top N individuals

            Individual[] inds = newpop.subpops[sub].individuals;
            Individual[] oldinds = state.population.subpops[sub].individuals;
            for(int x=inds.length-numElites(state, sub);x<inds.length;x++)
                inds[x] = (Individual)(oldinds[orderedPop[x]].clone());
            }
        }
            
    // optionally force reevaluation
    unmarkElitesEvaluated(state, newpop);
    }
	
	static class EliteComparator implements SortComparatorL
    {
    Individual[] inds;
    public EliteComparator(Individual[] inds) {super(); this.inds = inds;}
    public boolean lt(long a, long b)
    { 
    	LGPIndividual_MFEA A = (LGPIndividual_MFEA) inds[(int)a];
    	LGPIndividual_MFEA B = (LGPIndividual_MFEA) inds[(int)b];
    	//return B.fitness.betterThan(A.fitness); 
    	return B.scalarRank < A.scalarRank;
	}
    public boolean gt(long a, long b)
    { 
    	LGPIndividual_MFEA A = (LGPIndividual_MFEA) inds[(int)a];
    	LGPIndividual_MFEA B = (LGPIndividual_MFEA) inds[(int)b];
    	//return A.fitness.betterThan(B.fitness); 
    	return A.scalarRank < B.scalarRank;
    }
    }
}

