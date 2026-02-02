package zhixing.cpxInd.algorithm.Multiform.individual.reproduce;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.koza.KozaNodeSelector;
import zhixing.cpxInd.algorithm.Multiform.individual.TGPIndividual4MForm;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.LGPIndividual;

public abstract class SwapRandAdjList4LGP extends ATCrossover4LGP{

	@Override
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread,
	        final CpxGPIndividual[] parents) 

        {
		// how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
		
		GPInitializer initializer = ((GPInitializer)state.initializer);

		//for every item in AT, new an instruction (with the specified function), the registers of the instruction are randomly initialized at first
        //every new an instruction, clone the function GPNode based on the index in primitives
        //check the write registers so that they are ?effective?
        //check the read registers so that they are connected to corresponding children if they exist. 

        for(int q=start, parnt = 0;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
            // at this point, parents[] contains our two selected individuals
            
            // are our tree values valid?
            if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= parents[0].getTreesLength()))
                // uh oh
                state.output.fatal("LGP ATCrossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= parents[1].getTreesLength()))
                // uh oh
                state.output.fatal("LGP ATCrossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual");
            
            int t1=0, t2=0;
	        LGPIndividual j1;
	        CpxGPIndividual j2;
	        j1 = ((LGPIndividual)parents[parnt]).lightClone();
	    	t1 = parnt;
	    	
	        j2 = (parents[(parnt + 1)%parents.length]).lightClone();
	        if (j2 instanceof TGPIndividual4MForm) {
	        	if(parents[(parnt + 1)%parents.length].getTreesLength() > 1) 
	        		t2 = state.random[thread].nextInt(parents[(parnt + 1)%parents.length].getTreesLength());
	        	else t2 = 0;
	        }
	        else t2 = (parnt + 1)%parents.length;
	        
	        //the key step in this operator, re-generate j2 so that we can swap random adjacency list
	        j2.rebuildIndividual(state, thread);
	        
	        boolean flag = false;
	        ArrayList<Pair<String, ArrayList<String>>> cand1 = new ArrayList<>();
	        ArrayList<Pair<String, ArrayList<String>>> cand2 = new ArrayList<>();
	        
	        //prepare the node selector
	        nodeselect2.reset();
	        
	        int begin1=j1.getTreesLength()+1, begin2=j2.getTreesLength()+1, pickNum1=j1.getTreesLength(), pickNum2=j2.getTreesLength();
	        GPNode p2 = null;
	        
	        int numFunNode1 = 0, numFunNode2 = 0;
	        
	        Iterator<Pair<String, ArrayList<String>>> it;
	        
	        for(int t = 0;t<numTries;t++){

	        	//prepare j2
            	if(j2 instanceof TGPIndividual4MForm){
            		
            		//prepare j1
		        	int tmp_begin1 = state.random[thread].nextInt(j1.getTreesLength());
		        	pickNum1 = state.random[thread].nextInt(Math.min(MaxSegLength, tmp_begin1+1)) + 1;
		        	begin1 = Math.max(0, tmp_begin1-pickNum1);
		        	
		        	cand1 = j1.getAdjacencyTable(state, begin1, tmp_begin1+1);
		        	it = cand1.iterator();
	            	int cnt = cand1.size();
	            	//int GraphSize = 1 + state.random[thread].nextInt(MaxGraphSize);
	            	while(it.hasNext()){
	            		//int ind = it.next();
	            		it.next();
	            		//cnt ++;
	            		if(cnt > pickNum1){
	            			it.remove();
	            		}
	            		cnt --;
	            	}
            		
	            	int trial_dep = state.random[thread].nextInt(j2.getTree(t2).child.depth()); //the max random depth must be child.depth()-1, so it has ignored terminals
	            	p2 = ((KozaNodeSelector)nodeselect2).pickNode(state,subpopulation,thread,j2,j2.getTree(t2), GPNode.NODESEARCH_NONTERMINALS);
	            	for(int tt = 0; tt<numTries; tt++) {
	            		if(p2.atDepth() == trial_dep) break;
	            		p2 = ((KozaNodeSelector)nodeselect2).pickNode(state,subpopulation,thread,j2,j2.getTree(t2), GPNode.NODESEARCH_NONTERMINALS);
	            	}
//	            	p2 = j2.getTree(t2).child;
	            	cand2 = ((TGPIndividual4MForm)j2).getAdjacencyTable(p2);
		        	//p2 = ((KozaNodeSelector)nodeselect2).pickNode(state,subpopulation,thread,j2,j2.getTree(t2), GPNode.NODESEARCH_NONTERMINALS);
//		        	cand2 = ((TGPIndividual4MForm)j2).getAdjacencyTable(j2.getTree(t2).child);
		        	
//		        	if(cand2.size()>0)
//		        	{
//		        		begin2 = state.random[thread].nextInt(cand2.size());
////		        		for(int tt = 0;tt<numTries;tt++) {
////		        			begin2 = state.random[thread].nextInt(cand2.size());
////		        			if(Math.abs(j1.getTreesLength() - begin1 - begin2)<=MaxDistanceCrossPoint) break;
////		        		}
//		        		
//		        		pickNum2 = state.random[thread].nextInt(Math.min(begin2+1, MaxSegLength)) + 1;
//		        		
//		        		it = cand2.iterator();
//		            	//cnt = cand2.size();
//		            	int in = 0;
//		            	//GraphSize = 1 + state.random[thread].nextInt(MaxGraphSize);
//		            	while(it.hasNext()){
//		            		//int ind = it.next();
//		            		it.next();
//		            		in ++;
//
//		            		if(in < begin2 - pickNum2 || in > begin2){
//		            			it.remove();
//		            		}
//		            		//cnt --;
//		            	}
//		        		
//		        	}
		        	
		        	
	        	}
	        	else if (j2 instanceof LGPIndividual){
	        		
	        		//prepare j1
		        	begin1 = state.random[thread].nextInt(j1.getTreesLength());
		        	pickNum1 = state.random[thread].nextInt(Math.min(MaxSegLength, j1.getTreesLength() - begin1)) + 1;
		        	
		        	cand1 = j1.getAdjacencyTable(state, begin1, j1.getTreesLength());
		        	it = cand1.iterator();
	            	int cnt = cand1.size();
	            	//int GraphSize = 1 + state.random[thread].nextInt(MaxGraphSize);
	            	while(it.hasNext()){
	            		//int ind = it.next();
	            		it.next();
	            		//cnt ++;
	            		if(cnt > pickNum1){
	            			it.remove();
	            		}
	            		cnt --;
	            	}
	        		
	        		for(int tt = 0;tt<numTries;tt++) {
	        			begin2 = state.random[thread].nextInt(j2.getTreesLength());
	        			if(Math.abs(begin1 - begin2)<=MaxDistanceCrossPoint) break;
	        		}
	        		
//	        		pickNum2 = state.random[thread].nextInt(Math.min(j2.getTreesLength() - begin2, MaxSegLength)) + 1;
//	        		
//	        		cand2 = ((LGPIndividual)j2).getAdjacencyTable(begin2, j2.getTreesLength());
	        		
	        		int end2 = state.random[thread].nextInt(j2.getTreesLength()-begin2)+begin2+1;
	        		
	        		numFunNode2 = 0;
	        		for(int i = begin2; i< j2.getTreesLength(); i++) {
	        			numFunNode2 += ((LGPIndividual)j2).getTreeStruct(i).child.numNodes(GPNode.NODESEARCH_NONTERMINALS); 
	    	        }
	        		
	        		pickNum2 = state.random[thread].nextInt(Math.min(numFunNode2, MaxSegLength)) + 1;
	        		
	        		
	        		cand2 = ((LGPIndividual)j2).getAdjacencyTable(state, begin2, end2);
	        		
	        		it = cand2.iterator();
	            	cnt = cand2.size();
	            	//GraphSize = 1 + state.random[thread].nextInt(MaxGraphSize);
	            	while(it.hasNext()){
	            		//int ind = it.next();
	            		it.next();
	            		//cnt ++;
	            		if(cnt > pickNum2){
	            			it.remove();
	            		}
	            		cnt --;
	            	}
	        	} 	
	                    	
	        	flag = verifypoint(j1, cand1.size(), cand2.size());
	        	
	        	if(flag) break;

	        }

           if(flag) {
        	   this.ATSwapping(j1, j1.getTreeStruct(0), begin1, pickNum1, cand1.size(), cand2, state, thread);
        	   
           }
            
           if(microMutation != null) j1 = (LGPIndividual) microMutation.produce(subpopulation, j1, state, thread);
       	
	        // add the individuals to the population
	        inds[q] = j1;
	        q++;
	        parnt ++;
	        
            }
            
        return n;
        }
	
}
