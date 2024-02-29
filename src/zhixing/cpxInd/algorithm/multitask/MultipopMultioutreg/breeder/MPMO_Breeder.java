package zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.breeder;

import java.util.PriorityQueue;
import java.util.Vector;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Initializer;
import ec.Population;
import ec.Subpopulation;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleBreeder;
import ec.util.Parameter;
import ec.util.QuickSort;
import ec.util.SortComparatorL;
import zhixing.cpxInd.algorithm.multitask.MFEA.breeder.MFGPS_Breeder;
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA;
import zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO;

public class MPMO_Breeder extends MFGPS_Breeder{
	public static final String P_SCALARRANK = "elitebyscalarrank";
	
	public boolean [] use_scalarrank;
	
	@Override
	public void setup(EvolutionState state, Parameter base)
    {
	    super.setup(state, base); // unnecessary really
	
	    Parameter p = new Parameter(Initializer.P_POP).push(Population.P_SIZE);
	    int size = state.parameters.getInt(p,null,1);
	    
	    use_scalarrank = new boolean [size];
	    
	    for(int x = 0;x<size;x++){
	    	use_scalarrank[x] = state.parameters.getBoolean(base.push(P_SCALARRANK).push(""+x), null, false);
	    }
	    
	    state.output.exitIfErrors();
    }
	
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
            QuickSort.qsort(orderedPop, new EliteComparator(state.population.subpops[sub].individuals, sub));
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
	
	public void sortNupdateRankNFactor(EvolutionState state, int subpopulation){
		//identify the skillfactor based on their rank on different tasks.
		Subpopulation pop = (Subpopulation) state.population.subpops[subpopulation];
		
		int numTasks = ((MultiObjectiveFitness)pop.individuals[0].fitness).getNumObjectives();
		
		for(int i = 0;i<pop.individuals.length;i++){
			((LGPIndividual_MPMO)pop.individuals[i]).scalarRank_v = new Vector(numTasks);
//			if(((LGPIndividual_MPMO)pop.individuals[i]).scalarRank_v == null){
//				((LGPIndividual_MPMO)pop.individuals[i]).scalarRank_v = new Vector<>(numTasks);
//			     for(int ii = 0;ii<numTasks;ii++){
//			    	 ((LGPIndividual_MPMO)pop.individuals[i]).scalarRank_v.add(LGPIndividual_MPMO.INF_SCALAR_RANK);
//			     }
//			}
		}
		

		//for each task, visit all individuals and update their skill factor as the task with smallest rank
		if(numTasks > 1){
			for(int t = 0;t<numTasks;t++){
				
				Subpopulation twinspop = null;
				if(t+1 < state.population.subpops.length) {
					twinspop = (Subpopulation) state.population.subpops[t+1];
				}
					
				
				for(int i = 0; i<pop.individuals.length;i++){
					
					//if(((MultiObjectiveFitness)pop.individuals[i].fitness).getObjective(t) == LGPIndividual_MPMO.INF_SCALAR_RANK) continue;
					
					double rnk = 0;
					for(int j = 0;j<pop.individuals.length;j++){
						if(i == j) continue;
						double fi = ((MultiObjectiveFitness)pop.individuals[i].fitness).getObjective(t);
						double fj = ((MultiObjectiveFitness)pop.individuals[j].fitness).getObjective(t);
						if(fi > fj || (fi == fj && i > j) ){
							rnk++; 
						}
					}
					
					//ranking together with specific sub populations
					if(t+1 < state.population.subpops.length && twinspop != null){
						for(int j = 0;j<twinspop.individuals.length;j++){
							double fi = ((MultiObjectiveFitness)pop.individuals[i].fitness).getObjective(t);
							double fj = ((MultiObjectiveFitness)twinspop.individuals[j].fitness).getObjective(0);
							if(fi > fj || (fi == fj && i > j) ){
								rnk++; 
							}
						}
					}
					
					((LGPIndividual_MPMO)pop.individuals[i]).scalarRank_v.add((Double)rnk);
					
					if(t == 0 || (t>0 && rnk < ((LGPIndividual_MPMO)pop.individuals[i]).scalarRank)
							|| (t>0 && rnk == ((LGPIndividual_MPMO)pop.individuals[i]).scalarRank && state.random[0].nextDouble()<0.5)
							){
						((LGPIndividual_MPMO)pop.individuals[i]).scalarRank = rnk;
						((LGPIndividual_MPMO)pop.individuals[i]).skillFactor = t;
					}
					
				}
			}
			
			//==============debug
//			int cnt[] = new int[numTasks];
//			for(int i = 0; i<pop.individuals.length;i++){
//				cnt[((LGPIndividual_MPMO)pop.individuals[i]).skillFactor]++;
//			}
//			for(int t = 0;t<numTasks;t++){
//				System.out.print(""+cnt[t]+"\t");
//			}
//			System.out.println();
			//=============
		}
		else{
			//rank the individuals within a specific task
			for(int i = 0; i<pop.individuals.length;i++){
				double rnk = 0;
				int taskid = ((LGPIndividual_MPMO)pop.individuals[i]).skillFactor;
				for(int j = 0;j<pop.individuals.length;j++){
					if(i == j) continue;
					if(taskid != ((LGPIndividual_MPMO)pop.individuals[j]).skillFactor){
						System.err.print("In MPMO_Breeder, the skillfactor of individual "+i+" ("+taskid
								+") is inconsistent with individual "+j+" ("+((LGPIndividual_MPMO)pop.individuals[j]).skillFactor+")");
						System.exit(1);
					}
//					double fi = ((MultiObjectiveFitness)pop.individuals[i].fitness).getObjective(taskid);
//					double fj = ((MultiObjectiveFitness)pop.individuals[j].fitness).getObjective(taskid);
					
					Fitness f1 = pop.individuals[i].fitness;
					Fitness f2 = pop.individuals[j].fitness;
					if(f2.betterThan(f1) || (f1.equivalentTo(f2) && i > j) ){
						rnk++;
					}
				}
				
				//ranking together with the first sub population
				if(state.population.subpops.length > 1){
					Subpopulation twinspop = (Subpopulation) state.population.subpops[0];
					if(subpopulation > 1 && twinspop != null){
						for(int j = 0;j<twinspop.individuals.length;j++){
							double fi = ((MultiObjectiveFitness)pop.individuals[i].fitness).getObjective(0);
							double fj = ((MultiObjectiveFitness)twinspop.individuals[j].fitness).getObjective(subpopulation - 1);
							if(fi > fj || (fi == fj && i > j) ){
								rnk++; 
							}
						}
					}
				}
				
				((LGPIndividual_MPMO)pop.individuals[i]).scalarRank = rnk;
				((LGPIndividual_MPMO)pop.individuals[i]).scalarRank_v.add((Double)rnk);
			}
		}

	}
	
	class EliteComparator implements SortComparatorL
    {
    Individual[] inds;
    boolean compareByScalarrank = false;
    public EliteComparator(Individual[] inds) {super(); this.inds = inds;}
    public EliteComparator(Individual[] inds, int sub) {super(); this.inds = inds; compareByScalarrank = use_scalarrank[sub];}
    public boolean lt(long a, long b)
    { 
    	LGPIndividual_MFEA A = (LGPIndividual_MFEA) inds[(int)a];
    	LGPIndividual_MFEA B = (LGPIndividual_MFEA) inds[(int)b];
    	
    	if(!compareByScalarrank)
    		return B.fitness.betterThan(A.fitness); 
    	else
    		return B.scalarRank < A.scalarRank;
	}
    public boolean gt(long a, long b)
    { 
    	LGPIndividual_MFEA A = (LGPIndividual_MFEA) inds[(int)a];
    	LGPIndividual_MFEA B = (LGPIndividual_MFEA) inds[(int)b];
    	
    	if(!compareByScalarrank)
    		return A.fitness.betterThan(B.fitness); 
    	else
    		return A.scalarRank < B.scalarRank;
    }
    }
}
