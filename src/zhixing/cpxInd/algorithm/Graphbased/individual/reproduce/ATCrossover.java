package zhixing.cpxInd.algorithm.Graphbased.individual.reproduce;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public abstract class ATCrossover extends zhixing.cpxInd.individual.reproduce.LGP2PointCrossoverPipeline{
	
	public static final String ADJTABLE_CROSSOVER = "ATcross";
	
	public static final String P_BUILDER = "build";
	
	public LGPMutationGrowBuilder builder;
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = LGPDefaults.base().push(ADJTABLE_CROSSOVER);
		
		Parameter p = base.push(P_BUILDER).push(""+0);
		Parameter d = def.push(P_BUILDER).push(""+0);
        
		builder = (LGPMutationGrowBuilder)
	            (state.parameters.getInstanceForParameter(
	                p,d, GPNodeBuilder.class));
	        builder.setup(state,p);
	}
	
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
            LGPIndividual[] parnts = new LGPIndividual[2];
        	for(int ind = 0 ; ind < parnts.length; ind++){
        		parnts[ind] = (LGPIndividual) this.parents[ind]; 
        	}
        	
        	q += this.produce(min, max, start, subpopulation, inds, state, thread, parnts);
            }
            
        return n;
        }
	
	
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread,
	        final LGPIndividual[] parents) 

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
	        LGPIndividual4Graph j1, j2;
	        j1 = ((LGPIndividual4Graph)parents[parnt]).lightClone();
	    	t1 = parnt;
	        j2 = ((LGPIndividual4Graph)parents[(parnt + 1)%parents.length]).lightClone();
	        t2 = (parnt + 1)%parents.length;
	        
	        boolean flag = false;
	        ArrayList<Pair<String, ArrayList<String>>> cand1 = new ArrayList<>();
	        ArrayList<Pair<String, ArrayList<String>>> cand2 = new ArrayList<>();
	        int begin1=j1.getTreesLength()+1, begin2=j2.getTreesLength()+1, pickNum1=j1.getTreesLength(), pickNum2=j2.getTreesLength();
	        
	        for(int t = 0;t<numTries;t++){
    	
	            int tt = 0;
	            for(;tt<numTries;tt++){
	            	begin1 = state.random[thread].nextInt(j1.getTreesLength());
	            	begin2 = state.random[thread].nextInt(j2.getTreesLength());
	            	if(Math.abs(begin1 - begin2)<=MaxDistanceCrossPoint) break;
	            }
	            pickNum1 = state.random[thread].nextInt(Math.min(MaxSegLength, j1.getTreesLength() - begin1)) + 1;
	            pickNum2 = state.random[thread].nextInt(Math.min(MaxSegLength, j2.getTreesLength() - begin2)) + 1;
	            
	            cand1 = j1.getAdjacencyTable(state, begin1, j1.getTreesLength());
	            cand2 = j2.getAdjacencyTable(state, begin2, j2.getTreesLength());
	                       
	            Iterator<Pair<String, ArrayList<String>>> it = cand1.iterator();
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
	                    	
	        	flag = verifypoint(j1, j2, cand1.size(), cand2.size());
	        	
	        	if(flag) break;

	        }

           if(flag) {
        	   ATSwapping(j1, j1.getTreeStruct(0), begin1, pickNum1, cand1.size(), cand2, state, thread);
        	   
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
	        		ATSwapping(j2, j2.getTreeStruct(0), begin2, pickNum2, cand2.size(), cand1, state, thread);
	        	}
	        	if(microMutation != null) j2 = (LGPIndividual4Graph) microMutation.produce(subpopulation, j2, state, thread);
	        	
	            inds[q] = j2;
	            q++;
	            parnt ++;
	        }
            }
            
        return n;
        }
	
//	public int produce(final int min, 
//	        final int max, 
//	        final int start,
//	        final int subpopulation,
//	        final Individual[] inds,
//	        final EvolutionState state,
//	        final int thread,
//	        final LGPIndividual[] parents) 
//		//this "produce" function is for component analysis - swapping the lower part of the DAG
//        {
//		// how many individuals should we make?
//        int n = typicalIndsProduced();
//        if (n < min) n = min;
//        if (n > max) n = max;
//		
//		GPInitializer initializer = ((GPInitializer)state.initializer);
//
//		//for every item in AT, new an instruction (with the specified function), the registers of the instruction are randomly initialized at first
//        //every new an instruction, clone the function GPNode based on the index in primitives
//        //check the write registers so that they are ?effective?
//        //check the read registers so that they are connected to corresponding children if they exist. 
//
//        for(int q=start, parnt = 0;q<n+start; /* no increment */)  // keep on going until we're filled up
//            {
//            // at this point, parents[] contains our two selected individuals
//            
//            // are our tree values valid?
//            if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= parents[0].getTreesLength()))
//                // uh oh
//                state.output.fatal("LGP ATCrossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
//            if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= parents[1].getTreesLength()))
//                // uh oh
//                state.output.fatal("LGP ATCrossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual");
//            
//            int t1=0, t2=0;
//	        LGPIndividual4Graph j1, j2;
//	        j1 = ((LGPIndividual4Graph)parents[parnt]).lightClone();
//	    	t1 = parnt;
//	        j2 = ((LGPIndividual4Graph)parents[(parnt + 1)%parents.length]).lightClone();
//	        t2 = (parnt + 1)%parents.length;
//	        
//	        boolean flag = false;
//	        ArrayList<Pair<String, ArrayList<String>>> cand1 = new ArrayList<>();
//	        ArrayList<Pair<String, ArrayList<String>>> cand2 = new ArrayList<>();
//	        int begin1=j1.getTreesLength(), begin2=j2.getTreesLength(), pickNum1=j1.getTreesLength(), pickNum2=j2.getTreesLength();
//	        
//	        for(int t = 0;t<numTries;t++){
//    	
//	            int tt = 0;
//	            for(;tt<numTries;tt++){
//	            	begin1 = state.random[thread].nextInt(j1.getTreesLength())+1;
//	            	begin2 = state.random[thread].nextInt(j2.getTreesLength())+1;
//	            	if(Math.abs(begin1 - begin2)<=MaxDistanceCrossPoint) break;
//	            }
////	            pickNum1 = state.random[thread].nextInt(Math.min(MaxSegLength, j1.getTreesLength() - begin1)) + 1;
////	            pickNum2 = state.random[thread].nextInt(Math.min(MaxSegLength, j2.getTreesLength() - begin2)) + 1;
//	            
//	            pickNum1 = state.random[thread].nextInt(Math.min(MaxSegLength, begin1));
//	            pickNum2 = state.random[thread].nextInt(Math.min(MaxSegLength, begin2));
//	            
////	            cand1 = j1.getAdjacencyTable(begin1, begin1+pickNum1);
////	            cand2 = j2.getAdjacencyTable(begin2, begin2+pickNum2);
//	            
////	            cand1 = j1.getAdjacencyTable(begin1, j1.getTreesLength());
////	            cand2 = j2.getAdjacencyTable(begin2, j2.getTreesLength());
//	            
//	            cand1 = j1.getAdjacencyTable(0, begin1);
//	            cand2 = j2.getAdjacencyTable(0, begin2);
//	            
////	            cand1 = j1.getAdjacencyTable(begin1-pickNum1, begin1);
////	            cand2 = j2.getAdjacencyTable(begin2-pickNum2, begin2);
//	            
//	            Iterator<Pair<String, ArrayList<String>>> it = cand1.iterator();
//            	int cnt = 0;
//            	//int GraphSize = 1 + state.random[thread].nextInt(MaxGraphSize);
//            	while(it.hasNext()){
//            		//int ind = it.next();
//            		it.next();
//            		cnt ++;
//            		if(cnt > pickNum1){
//            			it.remove();
//            		}
////            		cnt --;
//            	}
//            	it = cand2.iterator();
//            	cnt = 0;
//            	//GraphSize = 1 + state.random[thread].nextInt(MaxGraphSize);
//            	while(it.hasNext()){
//            		//int ind = it.next();
//            		it.next();
//            		cnt ++;
//            		if(cnt > pickNum2){
//            			it.remove();
//            		}
////            		cnt --;
//            	}
//	                    	
//	        	flag = verifypoint(j1, j2, cand1.size(), cand2.size());
//	        	
//	        	if(flag) break;
//	//        	if(tmp_flag && distance >= maxdistance){
//	//        		cand1 = tmp_cand1;
//	//        		cand2 = tmp_cand2;
//	//        		maxdistance = distance;
//	//        		flag = tmp_flag;
//	//        	}
//	        }
//
//           if(flag) {
//        	   ATSwapping(j1, (LGPIndividual4Graph)parents[t2], begin1-pickNum1, pickNum1, cand1.size(), cand2, state, thread);
//        	   
//           }
//            
//           if(microMutation != null) j1 = (LGPIndividual4Graph) microMutation.produce(subpopulation, j1, state, thread);
//       	
//	        // add the individuals to the population
//	        inds[q] = j1;
//	        q++;
//	        parnt ++;
//	        if (q<n+start && !tossSecondParent)
//	        {
//	        	if(flag){
//	        		//replace j2's instructions by j1
//	        		ATSwapping(j2, (LGPIndividual4Graph)parents[t1], begin2-pickNum2, pickNum2, cand2.size(), cand1, state, thread);
//	        	}
//	        	if(microMutation != null) j2 = (LGPIndividual4Graph) microMutation.produce(subpopulation, j2, state, thread);
//	        	
//	            inds[q] = j2;
//	            q++;
//	            parnt ++;
//	        }
//            }
//            
//        return n;
//        }
	
	protected void ATSwapping(LGPIndividual pr, 
			GPTreeStruct insPrototype, 
			int begin_genome, 
			int pickNum_pr, 
			int remove_num, 
			ArrayList<Pair<String, ArrayList<String>>> AT, 
			EvolutionState state, 
			int thread){
		
		
		//remove pr's instructions
		//remove the size of AT instructions from the segment [begin_pr, begin_pr+pickNum_pr)
		for(int i = 0;i<remove_num;i++){ 
			int rm = begin_genome + state.random[thread].nextInt(pickNum_pr - i);
			
			pr.removeTree(rm);
			
			pr.evaluated = false;
			
		}

		//add instructions generated based on AT
		int start = begin_genome + state.random[thread].nextInt(pickNum_pr - remove_num + 1);
		for(int i = 0;i<AT.size();i++){
    		
			Pair<String, ArrayList<String>> item = AT.get(i);
			

    		GPTreeStruct tree = (GPTreeStruct) insPrototype.clone();
    		
    		generateInstrBasedAT(pr, tree, start, 0.5, item, state, thread);
    		
            tree.owner = pr;
            //tree.child = (GPNode)(pd.getTree(source).child.clone());
            tree.child.parent = tree;
            tree.child.argposition = 0;
            
            pr.addTree(start, tree);
            

            pr.evaluated = false; 
		}
		
		maintainConnection(pr, start, AT.size(), AT, state, thread);
		
		//remove introns
		if(pr.getTreesLength() > pr.getMaxNumTrees()){
			int cnt = pr.getTreesLength() - pr.getMaxNumTrees();
			for(int k = 0; k<cnt; k++){
				int res = state.random[thread].nextInt(pr.getTreesLength());
				if(pr.getEffTreesLength() <= pr.getMaxNumTrees()){
					for(int x = 0;x < pr.getTreesLength();x++) {
		        		if(!pr.getTreeStruct(res).status){break;}
		        		res = (res + state.random[thread].nextInt(pr.getTreesLength())) % pr.getTreesLength();
		        	}
					
				}
				pr.removeTree(res);
			}
		}
	}
	
	abstract protected void generateInstrBasedAT(
			LGPIndividual offspring,
			GPTreeStruct tree,
			int insert, 
			double consRate,
			Pair<String, ArrayList<String>> ATitem, 
			//double [][] normAM,
			EvolutionState state, 
			int thread
			//final GPType type,
			//final GPNodeParent parent,
	        //final int argposition,
	        ) ;
	
	protected boolean verifypoint(LGPIndividual j1, LGPIndividual j2, int ind1, int ind2){
		//ind1 and ind2 are respectively the number of instructions of the candidate instruction list
		//return true if ind1 and ind2 will not exceed the length constraints
		
		if(ind2 == ind1 && ind1 == 0){
			return false;
		}
		
		if(Math.abs(ind1 - ind2)>MaxLenDiffSeg)
			return false;
		
		boolean res = true;
		if(ind1 < ind2){
			int diff = ind2 - ind1;
			if(j1.getEffTreesLength() + diff > j1.getMaxNumTrees() || j2.getEffTreesLength() - diff < j2.getMinNumTrees())
				res = false;
		}
		else if (ind1 > ind2){
			int diff = ind1 - ind2;
			if(j2.getEffTreesLength() + diff > j2.getMaxNumTrees() || j1.getEffTreesLength() - diff < j1.getMinNumTrees())
				res = false;
		}
		return res;
	}
	
	
	abstract protected void maintainConnection(LGPIndividual pr, 
			int start, 
			int number, 
			ArrayList<Pair<String, ArrayList<String>>> AT, 
			EvolutionState state, 
			int thread) ;
}
