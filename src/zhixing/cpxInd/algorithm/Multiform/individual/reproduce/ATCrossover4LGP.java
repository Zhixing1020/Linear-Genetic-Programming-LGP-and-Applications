package zhixing.cpxInd.algorithm.Multiform.individual.reproduce;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.koza.CrossoverPipeline;
import ec.gp.koza.KozaNodeSelector;
import ec.gp.koza.MutationPipeline;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.Graphbased.individual.reproduce.ATCrossover;
import zhixing.cpxInd.algorithm.Multiform.individual.LGPIndividual4MForm;
import zhixing.cpxInd.algorithm.Multiform.individual.TGPIndividual4MForm;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.reproduce.LGP2PointCrossoverPipeline;
import zhixing.cpxInd.individual.reproduce.LGPMacroMutationPipeline;
import zhixing.cpxInd.individual.reproduce.LGPMicroMutationPipeline;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public abstract class ATCrossover4LGP extends ATCrossover {
	//the receiver is an LGP individual
	
	public static final String P_MACROMUTBASE = "macro_base";
	public static final String P_CROSSBASE = "cross_base";
	
	public static final String P_MACRORATE = "macro_rate";
	public static final String P_CROSSRATE = "cross_rate";
	
	protected LGPMacroMutationPipeline macroMutation;
	protected LGP2PointCrossoverPipeline crossover;
	
	protected double macrorate;
	protected double crossrate;
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = LGPDefaults.base().push(ADJTABLE_CROSSOVER);
		
		Parameter macrobase = new Parameter(state.parameters.getString(base.push(P_MACROMUTBASE), def.push(P_MACROMUTBASE)));
        macroMutation = null;
        if(!macrobase.toString().equals("null")){
        	//microMutation = new LGPMicroMutationPipeline();
        	macroMutation = (LGPMacroMutationPipeline)(state.parameters.getInstanceForParameter(
                    macrobase, def.push(P_MACROMUTBASE), MutationPipeline.class));
   		 macroMutation.setup(state, macrobase);
        }
        macrorate =  state.parameters.getDouble(base.push(P_MACRORATE),def.push(P_MACRORATE),0);
        if (macrorate<0)
            state.output.fatal("ATCrossover4LGP has an invalid macro rate (it must be >= 0 && <= 1).",base.push(P_MACRORATE),def.push(P_MACRORATE));
        
        Parameter crossbase = new Parameter(state.parameters.getString(base.push(P_CROSSBASE), def.push(P_CROSSBASE)));
        crossover = null;
        if(!crossbase.toString().equals("null")){
        	//microMutation = new LGPMicroMutationPipeline();
        	crossover = (LGP2PointCrossoverPipeline)(state.parameters.getInstanceForParameter(
                    crossbase, def.push(P_CROSSBASE), CrossoverPipeline.class));
   		 crossover.setup(state, crossbase);
        }
        crossrate =  state.parameters.getDouble(base.push(P_CROSSRATE),def.push(P_CROSSRATE),0);
        if (crossrate<0)
            state.output.fatal("ATCrossover4LGP has an invalid crossover rate (it must be >= 0 && <= 1).",base.push(P_CROSSRATE),def.push(P_CROSSRATE));
	}
	
	@Override
	public int typicalIndsProduced()
    {
    return 1;
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
        	sources[0].produce(1,1,0,subpopulation,parents,state,thread);  //LGP individual, receiver
            sources[1].produce(1,1,1,(subpopulation+1)%state.population.subpops.length,parents,state,thread); // LGP or TGP individual, donor
            
            // at this point, parents[] contains our two selected individuals
            CpxGPIndividual[] parnts = new CpxGPIndividual[2];
        	for(int ind = 0 ; ind < parnts.length; ind++){
        		parnts[ind] = (CpxGPIndividual) this.parents[ind]; 
        	}
        	
        	q += this.produce(min, max, start, subpopulation, inds, state, thread, parnts);
        	

//        	if (/*parnts[1].fitness.betterThan(parnts[0].fitness) && */ 
//        			!((LGPIndividual4MForm)parnts[0]).issameRepresentation(parnts[1])) {  //if the parent from all the population is better than the parent from the current population 
//        		q += this.produce(min, max, start, subpopulation, inds, state, thread, parnts);
//        	}
//        	else { //the current population is in a leading position
//        		double rate = state.random[thread].nextDouble();
//        		
//        		if(rate<=crossrate) {
//        			LGPIndividual[] lGPparnts = new LGPIndividual[2];
//        			for(int ind = 0 ; ind < parnts.length; ind++){
//                		lGPparnts[ind] = (LGPIndividual) parnts[ind]; 
//                	}
//        			
//        			//q += crossover.produce(min, max, start, subpopulation, inds, state, thread);
//        			q += crossover.produce(min, max, start, subpopulation, inds, state, thread, lGPparnts);
//        		}
//        		else if (rate <= crossrate + macrorate) {
//        			//q += macroMutation.produce(min, max, start, subpopulation, inds, state, thread);
//        			inds[q] = macroMutation.produce(subpopulation, (LGPIndividual)parnts[0], state, thread);
//        			q++;
//        		}
//        		else {
//        			//q += microMutation.produce(min, max, start, subpopulation, inds, state, thread);
//        			inds[q] = microMutation.produce(subpopulation, (LGPIndividual)parnts[0], state, thread);
//        			q++;
//        		}
//        	}
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
	
	protected void ATSwapping(LGPIndividual pr, 
			GPTreeStruct insPrototype, 
			int begin_genome, 
			int pickNum_pr, 
			int remove_num, 
			ArrayList<Pair<String, ArrayList<String>>> AT, 
			EvolutionState state, 
			int thread){
		
		//pickNum_pr: the range size of to-be-considered instructions
		//remove_num: the actual number of to-be-removed instructions
		
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
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
			

    		GPTreeStruct tree = (GPTreeStruct) (insPrototype.clone());
    		boolean valid = false;
    		
    		for(int c = 0; c<numTries; c++) {
    			
    			if(tree.child.children[0].children.length == item.getSecond().size()) {
    				valid = true;
    				break;
    			}
    			
    			int size = GPNodeBuilder.NOSIZEGIVEN;
        		GPNode p1 = tree.child;
        		GPNode p2 = null;
        		if(builder instanceof LGPMutationGrowBuilder) {
                	p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
    	                    p1.parentType(initializer),
    	                    thread,
    	                    p1.parent,
    	                    //i.getTree(t).constraints(initializer).functionset,
    	                    tree.constraints(initializer).functionset,
    	                    p1.argposition,
    	                    size,
    	                    p1.atDepth());
                }
                else {
                	 p2 = builder.newRootedTree(state,
     	                    p1.parentType(initializer),
     	                    thread,
     	                    p1.parent,
     	                    //i.getTree(t).constraints(initializer).functionset,
     	                    tree.constraints(initializer).functionset,
     	                    p1.argposition,
     	                    size);
    			}
        		
        		tree.child = tree.child.cloneReplacingNoSubclone(p2,p1);
                tree.child.parent = tree;
                tree.child.argposition = 0;
                
                
    		}
    		
    		if(!valid) continue;
    		
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
	
	protected boolean verifypoint(LGPIndividual j1, int ind1, int ind2){
		//ind1 and ind2 are respectively the number of instructions of the candidate instruction list
		//return true if ind1 and ind2 will not exceed the length constraints
		//ind1: number of instructions that are removed
		//ind2: number of instructions that are newly inserted
		
		if(ind2 == ind1 && ind1 == 0){
			return false;
		}
		
		if(Math.abs(ind1 - ind2)>MaxLenDiffSeg)
			return false;
		
		boolean res = true;
		if(ind1 < ind2){
			int diff = ind2 - ind1;
			if(j1.getEffTreesLength() + diff > j1.getMaxNumTrees())
				res = false;
		}
		else if (ind1 > ind2){
			int diff = ind1 - ind2;
			if(j1.getEffTreesLength() - diff < j1.getMinNumTrees())
				res = false;
		}
		return res;
	}
}
