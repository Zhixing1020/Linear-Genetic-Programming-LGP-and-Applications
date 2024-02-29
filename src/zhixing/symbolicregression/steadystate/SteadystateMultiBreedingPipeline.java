package zhixing.symbolicregression.steadystate;

import org.apache.commons.lang3.ArrayUtils;

import ec.BreedingSource;
import ec.EvolutionState;
import ec.Individual;
import ec.SelectionMethod;
import ec.breed.MultiBreedingPipeline;
import ec.breed.ReproductionPipeline;
import ec.gp.koza.CrossoverPipeline;
import ec.gp.koza.MutationPipeline;

public class SteadystateMultiBreedingPipeline extends MultiBreedingPipeline{
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread) 

	        {
	    	
	        BreedingSource s = sources[BreedingSource.pickRandom(
	                sources,state.random[thread].nextDouble())];
	        
	        int [] winind = new int [2];
	        int [] loseind = new int [2];
	        Individual [] sink = new Individual [2];
	        Individual [] parnts;
	        int total = 1;
	        
	        total = s.produce(
	                min,max,start,subpopulation,sink,state,thread);
        	
	        //tournament selection for loser
        	for(int n = 0; n<total; n++) {
        		loseind[n] = state.random[thread].nextInt(inds.length);
        		for(int t = 0; t<6; t++) {  //by default, the tournament size of selecting loser is 7
        			int tmp = state.random[thread].nextInt(inds.length);
        			if(inds[loseind[n]].fitness.betterThan(inds[tmp].fitness)) {
        				loseind[n] = tmp;
        			}
        		}
        		
        		inds[loseind[n]] = sink[n];
        	}
	        
//	        if(s instanceof CrossoverPipeline) {
//	        	int numParnts = 2;
////	        	int numOffspring = s.typicalIndsProduced();
//	        	winind = new int [numParnts];
//	        	loseind = new int [numParnts];
//	        	sink = new Individual [numParnts];
//	        	parnts = new Individual[numParnts];
//	        	
////	        	for(int n = 0; n<numParnts; n++) {
////	        		
////	        		reproduce(1,n,subpopulation,parnts,state,thread,true); //produce an individual from source
////	        		winind[n] = ArrayUtils.indexOf(inds, parnts[n]);
//	        		
////	        		winind[n] = state.random[thread].nextInt(inds.length);
////	        		int tmp = state.random[thread].nextInt(inds.length);
////	        		if(tmp == winind[n]) {
////	        			tmp = (tmp + 1) % inds.length;
////	        		}
////	        		if(inds[winind[n]].fitness.
////	        				betterThan(inds[tmp].fitness)) {
////	        			loseind[n] = tmp;
////	        		}
////	        		else {
////	        			loseind[n] = winind[n];
////	        			winind[n] = tmp;
////	        		}
////	        	}
//	        	
////	        	for(int n = 0; n<numParnts; n++) {
////	        		parnts[n] = inds[winind[n]];
////	        	}
//	        	
//	        	total = s.produce(
//		                min,max,start,subpopulation,sink,state,thread);
//	        	
//	        	for(int n = 0; n<total; n++) {
//	        		loseind[n] = state.random[thread].nextInt(inds.length);
//	        		for(int t = 0; t<6; t++) {  //by default, the tournament size of selecting loser is 7
//	        			int tmp = state.random[thread].nextInt(inds.length);
//	        			if(inds[loseind[n]].fitness.betterThan(inds[tmp].fitness)) {
//	        				loseind[n] = tmp;
//	        			}
//	        		}
//	        		
//	        		inds[loseind[n]] = sink[n];
//	        	}
//	        }
//	        else if(s instanceof MutationPipeline) {
//	        	winind = new int[1];
//	        	loseind = new int[1];
//	        	parnts = new Individual[1];
//	        	sink = new Individual [1];
//	        	
////	        	reproduce(1,0,subpopulation,parnts,state,thread,true); //produce an individual from source
//	        	
////	        	winind[0] = state.random[thread].nextInt(inds.length);
////        		int tmp = state.random[thread].nextInt(inds.length);
////        		if(tmp == winind[0]) {
////        			tmp = (tmp + 1) % inds.length;
////        		}
////        		if(inds[winind[0]].fitness.
////        				betterThan(inds[tmp].fitness)) {
////        			loseind[0] = tmp;
////        		}
////        		else {
////        			loseind[0] = winind[0];
////        			winind[0] = tmp;
////        		}
//	        	
//	        	
//        		
////        		total = 1;
////        		sink[0] = s.produce(subpopulation, parnts[0], state, thread);
//        		total = s.produce(
//		                min,max,start,subpopulation,sink,state,thread);
//        		
//        		loseind[0] = state.random[thread].nextInt(inds.length);
//        		for(int t = 0; t<6; t++) {  //by default, the tournament size of selecting loser is 7
//        			int tmp = state.random[thread].nextInt(inds.length);
//        			if(inds[loseind[0]].fitness.betterThan(inds[tmp].fitness)) {
//        				loseind[0] = tmp;
//        			}
//        		}
//        		
//        		inds[loseind[0]] = sink[0];
//	        }
//	        else if (s instanceof ReproductionPipeline) {
//	        	sink = new Individual [1];
//	        	
//	        	sink[0] = s.produce(subpopulation, parnts[0], state, thread);
//	        }
//	        else {
//	        	System.err.print("unknown BreedingSource " + s.getClass() +" in SteadystateMultiBreeding\n");
//	        	System.exit(1);
//	        }
	        	     
	            
	        // clone if necessary
	        if (s instanceof SelectionMethod)
	            for(int q=start; q < total+start; q++)
	                inds[q] = (Individual)(inds[q].clone());
	        
	        return total;
	        }
}
