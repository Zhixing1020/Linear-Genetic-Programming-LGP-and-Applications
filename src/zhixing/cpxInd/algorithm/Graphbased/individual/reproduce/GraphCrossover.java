package zhixing.cpxInd.algorithm.Graphbased.individual.reproduce;

import java.util.ArrayList;
import java.util.Iterator;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;
import zhixing.cpxInd.individual.LGPIndividual;

public class GraphCrossover extends zhixing.cpxInd.individual.reproduce.GraphCrossover{
	@Override
    public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) 

        {
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        for(int q=start, parnt = 0;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
            // grab two individuals from our sources
            if (sources[0]==sources[1])  // grab from the same source
                sources[0].produce(2,2,0,subpopulation,parents,state,thread);
            else // grab from different sources
                {
                sources[0].produce(1,1,0,subpopulation,parents,state,thread);
                sources[1].produce(1,1,1,subpopulation,parents,state,thread);
                }
            
            // at this point, parents[] contains our two selected individuals

            // are our tree values valid?
            if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= parents[0].getTreesLength()))
                // uh oh
                state.output.fatal("LGP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= parents[1].getTreesLength()))
                // uh oh
                state.output.fatal("LGP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

            int t1=0, t2=0;
            LGPIndividual4Graph j1, j2;
            j1 = ((LGPIndividual4Graph)parents[parnt]).lightClone();
        	t1 = parnt;
            j2 = ((LGPIndividual4Graph)parents[(parnt + 1)%parents.length]).lightClone();
            t2 = (parnt + 1)%parents.length;
            
            boolean flag = false;
            ArrayList<Integer> cand1 = new ArrayList<>();
            ArrayList<Integer> cand2 = new ArrayList<>();
            int maxdistance = 0;
            
            for(int t = 0;t<numTries;t++){
            	//select a target output register
            	int target1 = state.random[thread].nextInt(((LGPIndividual) parents[parnt]).getRegisters().length);
            	int target2 = state.random[thread].nextInt(((LGPIndividual) parents[parnt + 1]).getRegisters().length);
            	
            	//collect the class graph for the target output register (updateStatus)
            	Integer tar1[] = new Integer[1];
            	tar1[0] = target1;  
            	Integer tar2[] = new Integer[1];
            	tar2[0] = target2; 
            	
                int begin1=j1.getTreesLength(), begin2=j2.getTreesLength(), tt = 0;
                for(;tt<numTries;tt++){
                	begin1 = state.random[thread].nextInt(j1.getTreesLength());
                	begin2 = state.random[thread].nextInt(j2.getTreesLength());
                	if(Math.abs(begin1 - begin2)<=MaxDistanceCrossPoint) break;
                }
                //int pickNum1 = state.random[thread].nextInt(j1.getTreesLength()) + 1;
                //int pickNum2 = state.random[thread].nextInt(j2.getTreesLength()) + 1;
                
                //pick two instruction segments and skip the ineffective instructions
//                Integer tar1[] = j1.getTreeStruct(begin1).effRegisters.toArray(new Integer [0]);
//            	Integer tar2[] = j2.getTreeStruct(begin2).effRegisters.toArray(new Integer [0]);

                cand1 = j1.getSubGraph(begin1, tar1);
                cand2 = j2.getSubGraph(begin2, tar2);
                
//                ArrayList<Integer> tmp_cand1 = j1.getSubGraph(begin1, tar1);
//                ArrayList<Integer> tmp_cand2 = j2.getSubGraph(begin2, tar2);
            	
//                double rate = PartialSubGraphRate + state.random[thread].nextDouble()*PartialSubGraphRate;
//            	cand1 = j1.getPartialSubGraph(begin1, tar1, rate, state, thread);
//            	cand2 = j2.getPartialSubGraph(begin2, tar2, rate, state, thread);
            	
            	Iterator<Integer> it = cand1.iterator();
            	int cnt = 0;
            	int GraphSize = 1 + state.random[thread].nextInt(MaxGraphSize);
            	while(it.hasNext()){
            		int ind = it.next();
            		cnt ++;
            		if(cnt > GraphSize){
            			it.remove();
            		}
            	}
            	it = cand2.iterator();
            	cnt = 0;
            	GraphSize = 1 + state.random[thread].nextInt(MaxGraphSize);
            	while(it.hasNext()){
            		int ind = it.next();
            		cnt ++;
            		if(cnt > GraphSize){
            			it.remove();
            		}
            	}
            	
            	flag = verifypoint(j1, j2, cand1.size(), cand2.size());
            	
            	//boolean tmp_flag = verifypoint(j1, j2, tmp_cand1.size(), tmp_cand2.size());
            	
            	//int distance = graph_distance(j1,j2,tmp_cand1,tmp_cand2);
            	
            	if(flag) break;
//            	if(tmp_flag && distance >= maxdistance){
//            		cand1 = tmp_cand1;
//            		cand2 = tmp_cand2;
//            		maxdistance = distance;
//            		flag = tmp_flag;
//            	}
            }
            
            if(flag){
            	//replace j1's instructions by j2
            	graphSwapping(j1,(LGPIndividual)parents[t2],cand1,cand2, state, thread);
            }
            
          if(microMutation != null) j1 = (LGPIndividual4Graph) microMutation.produce(subpopulation, j1, state, thread);

            // add the individuals to the population
          	
            inds[q] = j1;
            q++;
            parnt ++;
            if (q<n+start && !tossSecondParent)
            {
            	if(flag){
            		//replace j2's instructions by j1
            		graphSwapping(j2,(LGPIndividual)parents[t1],cand2,cand1, state, thread);
            	}
            	if(microMutation != null) j2 = (LGPIndividual4Graph) microMutation.produce(subpopulation, j2, state, thread);
            	
	            inds[q] = j2;
	            q++;
	            parnt ++;
            }
            

            }
            
        return n;
        }
}
