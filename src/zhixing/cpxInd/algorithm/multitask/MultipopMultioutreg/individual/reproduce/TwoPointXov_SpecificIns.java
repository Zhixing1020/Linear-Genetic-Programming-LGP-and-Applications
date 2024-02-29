package zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.reproduce;

import ec.BreedingSource;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.reproduce.LGP2PointCrossoverPipeline;
import zhixing.cpxInd.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator;
import zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO;

public abstract class TwoPointXov_SpecificIns extends LGP2PointCrossoverPipeline{
	public static final String SPECIFIC_2POINT_CROSSOVER_MULTITASK = "specific_2p_cross_MT";
	public static final String DO_MERGE = "do_merge";
	
	protected boolean tomerge;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		
		Parameter def = LGPDefaults.base().push(SPECIFIC_2POINT_CROSSOVER_MULTITASK);
		
		tossSecondParent = state.parameters.getBoolean(base.push(P_TOSS),
        def.push(P_TOSS),false);
		
		tomerge = state.parameters.getBoolean(base.push(DO_MERGE), def.push(DO_MERGE), false);
		
		//tossSecondParent = false; //because in MPMO, individuals from different sub populations have different number of objectives, they cannot be exchanged directly
		
		//set its own breeding source
		for(int x=0;x<sources.length;x++)
        {
        Parameter p = base.push(P_SOURCE).push(""+x);
        Parameter d = def.push(P_SOURCE).push(""+x);

        String s = state.parameters.getString(p,d);
        if (s!=null && s.equals(V_SAME))
            {
            if (x==0)  // oops
                state.output.fatal(
                    "Source #0 cannot be declared with the value \"same\".",
                    p,d);
            
            // else the source is the same source as before
            sources[x] = sources[x-1];
            }
        else 
            {
            sources[x] = (BreedingSource)
                (state.parameters.getInstanceForParameter(
                    p,d,BreedingSource.class));
            sources[x].setup(state,p);
            }
        }
		
	}
	
    abstract public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread);

//    {
//        // how many individuals should we make?
//        int n = typicalIndsProduced();
//        if (n < min) n = min;
//        if (n > max) n = max;
//
//        // should we bother?
//        if (!state.random[thread].nextBoolean(likelihood))
//            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already
//
//      //get the trial subpopulation for the origin-based offspring reservation
////        int trial = subpopulation;
////        while(state.population.subpops.length > 1 && trial == subpopulation){
////        	trial = state.random[thread].nextInt(state.population.subpops.length);
////        }
//        int trial = getLegalTrialPopulation(state, thread, subpopulation);
//
//        GPInitializer initializer = ((GPInitializer)state.initializer);
//        
//        for(int q=start, parnt = 0;q<n+start; /* no increment */)  // keep on going until we're filled up
//            {
//           
//        	int tarSF = state.random[thread].nextInt(((MFEA_Evaluator)state.evaluator).getNumTasks());
//        	
//        	if ((trial>0 && subpopulation==0)){
//        		tarSF = ((LGPIndividual_MPMO)state.population.subpops[trial].individuals[0]).skillFactor;
//        	}
//        	else if (subpopulation>0 && trial==0){
//        		tarSF = ((LGPIndividual_MPMO)state.population.subpops[subpopulation].individuals[0]).skillFactor;
//        	}
//        	else if (subpopulation>0 && subpopulation == trial){
//        		tarSF = ((LGPIndividual_MPMO)state.population.subpops[trial].individuals[0]).skillFactor;
//        	}
//        	
////        	((TournamentSel_SpecificIns)sources[0]).produce(1,1,0,subpopulation,parents,state,thread, tarSF);
////        	((TournamentSel_SpecificIns)sources[1]).produce(1,1,1,trial,parents,state,thread, tarSF);
//        	
//        	
//    		if(subpopulation > 0){
//        		sources[0].produce(1,1,0,subpopulation,parents,state,thread);
//        	}
//        	else{
//        		((TournamentSel_SpecificIns)sources[0]).produce(1,1,0,subpopulation,parents,state,thread, tarSF);
//        	}
//        	if(trial > 0){
//        		sources[1].produce(1,1,1,trial,parents,state,thread);
//        		
//        	}
//        	else{
//        		((TournamentSel_SpecificIns)sources[1]).produce(1,1,1,trial,parents,state,thread, tarSF);
//        	}
//        	
//        	if(tomerge && state.population.subpops.length > 1){
//        		((TournamentSel_SpecificIns)sources[0]).produce_merge(1,1,0,subpopulation, trial, parents,state,thread,tarSF);
//            	((TournamentSel_SpecificIns)sources[1]).produce_merge(1,1,1,subpopulation, trial, parents,state,thread,tarSF);
//        	}
//
//        	//==========debug
////        	try{
////        		OutputStream f = new FileOutputStream("E:/eclipse/eclipse/GPJSS-basicLGP/selectSubpop0.txt", true);
////        		OutputStreamWriter writer = new OutputStreamWriter(f);
////        		
////        		if(((LGPIndividual_MPMO)parents[0]).getOutputRegisters().length > 1){
////        			writer.append(state.generation + "\t" + 1.0 + "\n");
////        		}
////        		else{
////        			writer.append(state.generation + "\t" + 0.0 + "\n");
////        		}
////        		if(((LGPIndividual_MPMO)parents[1]).getOutputRegisters().length > 1){
////        			writer.append(state.generation + "\t" + 1.0 + "\n");
////        		}
////        		else{
////        			writer.append(state.generation + "\t" + 0.0 + "\n");
////        		}
////        		
////        		writer.close();
////        		f.close();
////        	}catch (IOException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
//        	//==========
//        	
//        	((MPMO_EvolutionState4DJSS)state).numXov[state.generation+1]+=2;
//    		if(((LGPIndividual_MPMO)parents[0]).getOutputRegisters().length > 1){
//    			((MPMO_EvolutionState4DJSS)state).numSharedXov[state.generation+1]++;
//			}
//			if(((LGPIndividual_MPMO)parents[1]).getOutputRegisters().length > 1){
//				((MPMO_EvolutionState4DJSS)state).numSharedXov[state.generation+1]++;
//			}
//
//        	
//        	
//        	//here the selection of parents can also be implemented by selecting parents by TournamentScalarRank until the skill factors of two parents are the same
////        	for(int t = 0;t<numTries;t++){
////        		if (sources[0]==sources[1])  // grab from the same source
////                    sources[0].produce(2,2,0,subpopulation,parents,state,thread);
////                else // grab from different sources
////                    {
////                    sources[0].produce(1,1,0,subpopulation,parents,state,thread);
////                    sources[1].produce(1,1,1,subpopulation,parents,state,thread);
////                    }
////        		
////        		if(((LGPIndividual_MFEA)parents[parnt]).skillFactor == ((LGPIndividual_MFEA)parents[(parnt + 1)%parents.length]).skillFactor)
////        			break;
////        	}
//            
//            // at this point, parents[] contains our two selected individuals
//            
//            LGPIndividual[] parnts = new LGPIndividual[2];
//        	for(int ind = 0 ; ind < parnts.length; ind++){
//        		parnts[ind] = (LGPIndividual) this.parents[ind]; 
//        	}
//        	
//            q += this.produce(min, max, start, subpopulation, inds, state, thread, parnts, tarSF);
//            
//            }
//            
//        return n;
//    }
	
	
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread,
	        final LGPIndividual[] parents,
	        final int taskid){
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
            
            // at this point, parents[] contains our two selected individuals
            
            // are our tree values valid?
            if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= parents[0].getTreesLength()))
                // uh oh
                state.output.fatal("LGP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= parents[1].getTreesLength()))
                // uh oh
                state.output.fatal("LGP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

            int retain_par = parnt;
            double f1 = ((LGPIndividual_MPMO)parents[parnt]).scalarRank_v.get(0);
    		if(((LGPIndividual_MPMO)parents[parnt]).scalarRank_v.size() > 1 && taskid < ((LGPIndividual_MPMO)parents[parnt]).scalarRank_v.size()){
    			f1 = ((LGPIndividual_MPMO)parents[parnt]).scalarRank_v.get(taskid);
    		}
    		
    		double f2 = ((LGPIndividual_MPMO)parents[(parnt + 1)%parents.length]).scalarRank_v.get(0);
    		if(((LGPIndividual_MPMO)parents[(parnt + 1)%parents.length]).scalarRank_v.size() > 1 && taskid < ((LGPIndividual_MPMO)parents[(parnt + 1)%parents.length]).scalarRank_v.size()){
    			f2 = ((LGPIndividual_MPMO)parents[(parnt + 1)%parents.length]).scalarRank_v.get(taskid);
    		}
    		if(f1 > f2 || f1==f2&&state.random[thread].nextDouble()<0.5){
    			retain_par = (parnt + 1)%parents.length;
    		}
            
            int t1=0, t2=0;
            LGPIndividual j1, j2;
            if(((LGPIndividual)parents[parnt]).getTreesLength() <= ((LGPIndividual)parents[(parnt + 1)%parents.length]).getTreesLength()) {
            	j1 = ((LGPIndividual)parents[parnt]).lightClone();
            	t1 = parnt;
                j2 = ((LGPIndividual)parents[(parnt + 1)%parents.length]).lightClone();
                t2 = (parnt + 1)%parents.length;
            }
            else {
            	j2 = ((LGPIndividual)parents[parnt]).lightClone();
            	t2 = parnt;
                j1 = ((LGPIndividual)parents[(parnt + 1)%parents.length]).lightClone();
                t1 = (parnt + 1)%parents.length;
            }
            
            // Fill in various tree information that didn't get filled in there
            //j1.renewTrees();
            //if (n-(q-start)>=2 && !tossSecondParent) j2.renewTrees();
            
            int begin1 = state.random[thread].nextInt(j1.getTreesLength());
            int pickNum1 = state.random[thread].nextInt(Math.min(j1.getTreesLength() - begin1, MaxSegLength)) + 1;
            
            int feasibleLowerB = Math.max(0, begin1 - MaxDistanceCrossPoint);
            int feasibleUpperB = Math.min(j2.getTreesLength() - 1, begin1 + MaxDistanceCrossPoint);

            int begin2 = feasibleLowerB + state.random[thread].nextInt(feasibleUpperB - feasibleLowerB + 1);
            int pickNum2 = 1 + state.random[thread].nextInt(Math.min(j2.getTreesLength() - begin2, MaxSegLength));
            boolean eff = Math.abs(pickNum1 - pickNum2) <= MaxLenDiffSeg;
            if(!eff) {
            	if(j2.getTreesLength() - begin2 > pickNum1 - MaxLenDiffSeg){
            		int compensate = MaxLenDiffSeg==0 ? 1 : 0;
            		pickNum2 = Math.max(1, pickNum1 - MaxLenDiffSeg) 
            				+ state.random[thread].nextInt(Math.min(MaxSegLength, Math.min(j2.getTreesLength() - begin2, pickNum1 + MaxLenDiffSeg))
        					- Math.max(0, pickNum1 - MaxLenDiffSeg) + compensate);
            	}
            	//the pesudo code of LGP book cannot guarantee the difference between pickNum1 and pickNum2 is smaller than 1
            	//especially when the begin2 is near to the tail and pickNum1 is relatively large, reselect pickNum2 can do nothing
//            	else{
//            		pickNum2 = pickNum1 = Math.min(pickNum1, pickNum2);
//            	}
            }
            
            if(pickNum1 <= pickNum2) {
            	if(j2.getTreesLength() - (pickNum2 - pickNum1)<j2.getMinNumTrees()
            			|| j1.getTreesLength() + (pickNum2 - pickNum1)>j1.getMaxNumTrees()) {
            		if(state.random[thread].nextDouble()<0.5) {
            			pickNum1 = pickNum2;
            		}
            		else {
            			pickNum2 = pickNum1;
            		}
            		if(begin1 + pickNum1 > j1.getTreesLength()) {
            			pickNum1 = pickNum2 = j1.getTreesLength() - begin1;
            		}
            	}
            }
            else{
            	if(j2.getTreesLength() + (pickNum1 - pickNum2) > j2.getMaxNumTrees()
            			|| j1.getTreesLength() - (pickNum1 - pickNum2)<j1.getMinNumTrees()) {
            		if(state.random[thread].nextDouble()<0.5) {
            			pickNum2 = pickNum1;
            		}
            		else {
            			pickNum1 = pickNum2;
            		}
            		if(begin2 + pickNum2 > j2.getTreesLength()) { //cannot provide as much as instructions
            			pickNum1 = pickNum2 = j2.getTreesLength() - begin2;
            		}
            	}
            }
            
            for(int pick = 0; pick < parents[t1].getTreesLength(); pick ++){
            	if(pick == begin1){
            		//remove trees in j1
            		for(int p = 0;p<pickNum1;p++) {
            			j1.removeTree(pick);
            			j1.evaluated = false;
            		}
            		
            		//add trees in j1
            		for(int p = 0;p<pickNum2;p++){
            			GPTreeStruct tree = (GPTreeStruct) (parents[t2].getTree(begin2 + p).clone());
                		//tree = (GPTree)(parents[parnt].getTree(pick).lightClone());
                        tree.owner = j1;
                        tree.child = (GPNode)(parents[t2].getTree(begin2 + p).child.clone());
                        tree.child.parent = tree;
                        tree.child.argposition = 0;
                        j1.addTree(pick + p, tree);
                        j1.evaluated = false; 
            		}
            	}
            	
            }
            
            if(microMutation instanceof LGPMicroMutation_Specific)
            	j1 = ((LGPMicroMutation_Specific)microMutation).produce(subpopulation, j1, state, thread,taskid);
            else if(microMutation != null) 
            	j1 = (LGPIndividual) microMutation.produce(subpopulation, j1, state, thread);
            
            for(int pick = 0; pick < parents[t2].getTreesLength(); pick++) {
        		if(pick == begin2){
            		//remove trees in j2
            		for(int p = 0;p<pickNum2;p++) {
            			j2.removeTree(pick);
            			j2.evaluated = false;
            		}
            		
            		//add trees in j2
            		for(int p = 0;p<pickNum1;p++){
            			GPTreeStruct tree = (GPTreeStruct) (parents[t1].getTree(begin1 + p).clone());
                		//tree = (GPTree)(parents[parnt].getTree(pick).lightClone());
                        tree.owner = j2;
                        tree.child = (GPNode)(parents[t1].getTree(begin1 + p).child.clone());
                        tree.child.parent = tree;
                        tree.child.argposition = 0;
                        j2.addTree(pick + p, tree);
                        j2.evaluated = false; 
            		}
            	}
        	}
        	
            if(microMutation instanceof LGPMicroMutation_Specific)
            	j2 = ((LGPMicroMutation_Specific)microMutation).produce(subpopulation, j2, state, thread,taskid);
            else if(microMutation != null) 
            	j2 = (LGPIndividual) microMutation.produce(subpopulation, j2, state, thread);
            
            //because different subpopulations may have different output registers, some just make them consistent
            LGPIndividual example = (LGPIndividual) state.population.subpops[subpopulation].individuals[0];
            j1.setOutputRegisters(example.getOutputRegisters());
    		j1.fitness = (MultiObjectiveFitness)example.fitness.clone();
    		((LGPIndividual_MPMO)j1).skillFactor = ((LGPIndividual_MPMO)example).skillFactor;
    		
    		j2.setOutputRegisters(example.getOutputRegisters());
    		j2.fitness = (MultiObjectiveFitness)example.fitness.clone();
    		((LGPIndividual_MPMO)j2).skillFactor = ((LGPIndividual_MPMO)example).skillFactor;
    		
        	if(t1 == retain_par/*parnt*/){
            	// add the individuals to the population
            if(j1.getTreesLength() < j1.getMinNumTrees() || j1.getTreesLength() > j1.getMaxNumTrees()){
            	state.output.fatal("illegal tree number in linear cross j1");
            }
            inds[q] = j1;
            q++;
            parnt ++;
            if (q<n+start && !tossSecondParent)
            {
            	if(j2.getTreesLength() < j2.getMinNumTrees() || j2.getTreesLength() > j2.getMaxNumTrees()){
                	state.output.fatal("illegal tree number in linear cross j2");
                }
            	
            	//because different subpopulations may have different output registers, some just make them consistent
//            	if(!Arrays.equals(j1.getOutputRegisters(), j2.getOutputRegisters())){
//            		j2.setOutputRegisters(j1.getOutputRegisters());
//            		j2.fitness = (MultiObjectiveFitness)j1.fitness.clone();
//            		((LGPIndividual_MPMO)j2).skillFactor = ((LGPIndividual_MPMO)j1).skillFactor;
//            	}
            	
	            inds[q] = j2;
	            q++;
	            parnt ++;
            }
            }
            else{
            	// add the individuals to the population
                if(j2.getTreesLength() < j2.getMinNumTrees() || j2.getTreesLength() > j2.getMaxNumTrees()){
                	state.output.fatal("illegal tree number in linear cross j2");
                }
                inds[q] = j2;
                q++;
                parnt ++;
                if (q<n+start && !tossSecondParent)
                {
                	if(j1.getTreesLength() < j1.getMinNumTrees() || j1.getTreesLength() > j1.getMaxNumTrees()){
                    	state.output.fatal("illegal tree number in linear cross j1");
                    }
                	
//                	if(!Arrays.equals(j1.getOutputRegisters(), j2.getOutputRegisters())){
//                		j1.setOutputRegisters(j2.getOutputRegisters());
//                		j1.fitness = (MultiObjectiveFitness)j2.fitness.clone();
//                		((LGPIndividual_MPMO)j1).skillFactor = ((LGPIndividual_MPMO)j2).skillFactor;
//                	}
                	
    	            inds[q] = j1;
    	            q++;
    	            parnt ++;
                }
            }
            }
            
        return n;
	}
	
	protected int getLegalTrialPopulation(EvolutionState state, int thread, int cursubpop){
		int trial = cursubpop;
		if(cursubpop==0 && state.population.subpops.length > 1){
//			if(state.random[thread].nextDouble()<0.5){
//				trial = 1 + state.random[thread].nextInt(state.population.subpops.length - 1);
//			}
//			else
				//trial = cursubpop;
			trial = 1 + state.random[thread].nextInt(state.population.subpops.length - 1);
//			if(tomerge){
//				trial = 1 + state.random[thread].nextInt(state.population.subpops.length - 1);
//			}
//			else
//				trial = cursubpop;
				
		}
		else if(cursubpop > 0){
			trial = 0;
//			if(tomerge){
//				trial = 0;
//			}
//			else
//				trial = cursubpop;
				//trial = 0;
			
		}
        return trial;
	}
}
