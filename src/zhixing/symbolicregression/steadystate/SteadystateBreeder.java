package zhixing.symbolicregression.steadystate;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.simple.SimpleBreeder;

public class SteadystateBreeder extends SimpleBreeder {
	public int SteadybreedPopulation(EvolutionState state, int subpop) {
		//only breed a small fraction of individuals (one or two offspring, based on the genetic operator)
		//the offspring replace some "losers" in the original population. The loser is determined by a size-2 tournament selection
		
		Population newpop = state.population;
        int updatenum = 0;
		
		if (reduceBy[subpop] > 0)
        {
        int prospectiveSize = Math.max(
            Math.max(state.population.subpops[subpop].individuals.length - reduceBy[subpop], minimumSize[subpop]),
            numElites(state, subpop));
        if (prospectiveSize < state.population.subpops[subpop].individuals.length)  // let's resize!
            {
            state.output.message("Subpop " + subpop + " reduced " + state.population.subpops[subpop].individuals.length + " -> " + prospectiveSize);
            newpop.subpops[subpop].resize(prospectiveSize);
            }
        }
      
        // how many threads do we really need?  No more than the maximum number of individuals in any subpopulation
        int numThreads = 0;
        
        numThreads = Math.max(numThreads, state.population.subpops[subpop].individuals.length);
        numThreads = Math.min(numThreads, state.breedthreads);
        if (numThreads < state.breedthreads)
            state.output.warnOnce("Largest subpopulation size (" + numThreads +") is smaller than number of breedthreads (" + state.breedthreads + "), so fewer breedthreads will be created.");
            
        int numinds[][] = 
            new int[numThreads][state.population.subpops.length];
        int from[][] = 
            new int[numThreads][state.population.subpops.length];
        
        int length = computeSubpopulationLength(state, newpop, subpop, 0);

        // we will have some extra individuals.  We distribute these among the early subpopulations
        int individualsPerThread = length / numThreads;  // integer division
        int slop = length - numThreads * individualsPerThread;
        int currentFrom = 0;
                            
        for(int y=0;y<numThreads;y++)
            {
            if (slop > 0)
                {
                numinds[y][subpop] = individualsPerThread + 1;
                slop--;
                }
            else
                numinds[y][subpop] = individualsPerThread;
                
            if (numinds[y][subpop] == 0)
                {
                state.output.warnOnce("More threads exist than can be used to breed some subpopulations (first example: subpopulation " + subpop + ")");
                }
                
            from[y][subpop] = currentFrom;
            currentFrom += numinds[y][subpop];
            }  
        
        
        if (numThreads==1)
            {
        	
        	updatenum = breedFractionalPopulation(newpop,subpop,state,0);

            }
        else
            {
        	
        	System.err.print("SteadystateBreeder does not support multi thread running");
        	System.exit(1);
            /*
              Thread[] t = new Thread[numThreads];
                
              // start up the threads
              for(int y=0;y<numThreads;y++)
              {
              SimpleBreederThread r = new SimpleBreederThread();
              r.threadnum = y;
              r.newpop = newpop;
              r.numinds = numinds[y];
              r.from = from[y];
              r.me = this;
              r.state = state;
              t[y] = new Thread(r);
              t[y].start();
              }
                
              // gather the threads
              for(int y=0;y<numThreads;y++) 
              try
              {
              t[y].join();
              }
              catch(InterruptedException e)
              {
              state.output.fatal("Whoa! The main breeding thread got interrupted!  Dying...");
              }
            */


            // start up the threads
//            for(int y=0;y<numThreads;y++)
//                {
//                SimpleBreederThread r = new SimpleBreederThread();
//                r.threadnum = y;
//                r.newpop = newpop;
//                r.numinds = numinds[y];
//                r.from = from[y];
//                r.me = this;
//                r.state = state;
//                pool.start(r, "ECJ Breeding Thread " + y );
//                }
//                
//            pool.joinAll();
            }
        return updatenum;
	}
	
	protected int breedFractionalPopulation(Population newpop, int subpop, EvolutionState state, int threadnum) {
		//here, newpop is not empty. it is full of evaluated individuals
		// do regular breeding of this subpopulation
        BreedingPipeline bp = null;
        if (clonePipelineAndPopulation)
            bp = (BreedingPipeline)newpop.subpops[subpop].species.pipe_prototype.clone();
        else
            bp = (BreedingPipeline)newpop.subpops[subpop].species.pipe_prototype;
                                
        // check to make sure that the breeding pipeline produces
        // the right kind of individuals.  Don't want a mistake there! :-)
        int x = 0;
        if (!bp.produces(state,newpop,subpop,threadnum))
            state.output.fatal("The Breeding Pipeline of subpopulation " + subpop + " does not produce individuals of the expected species " + newpop.subpops[subpop].species.getClass().getName() + " or fitness " + newpop.subpops[subpop].species.f_prototype );
        bp.prepareToProduce(state,subpop,threadnum);
        
        // start breedin'!
        int upperbound = 10; //each breeding maximally produces "upperbound" individuals
        x += bp.produce(1,upperbound,0,subpop,
                newpop.subpops[subpop].individuals,
                state,threadnum);
        
        bp.finishProducing(state,subpop,threadnum);
        
        return x;
	}
}

class SimpleBreederThread implements Runnable
{
Population newpop;
public int[] numinds;
public int[] from;
public SteadystateBreeder me;
public EvolutionState state;
public int threadnum;
public void run()
    {
	for(int x=0;x<state.population.subpops.length;x++) {
		me.breedFractionalPopulation(newpop,x, state,threadnum);
	}
    
    }
}
