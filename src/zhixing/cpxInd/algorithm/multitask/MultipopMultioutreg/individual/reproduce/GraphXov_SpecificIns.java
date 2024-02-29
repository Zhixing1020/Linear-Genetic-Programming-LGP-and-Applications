package zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.reproduce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ec.BreedingSource;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Parameter;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.reproduce.GraphCrossover;
import zhixing.cpxInd.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator;
import zhixing.cpxInd.algorithm.multitask.MultipopMultioutreg.individual.LGPIndividual_MPMO;

public class GraphXov_SpecificIns extends GraphCrossover{
	
	public static final String SPECIFIC_GRAPH_CROSSOVER_MULTITASK = "specific_graph_cross_MT";
	public static final String DO_MERGE = "do_merge";
	
//	public static final String P_SHAREDRATE = "shared_rate";
//	
//	public double shared_ins_rate;
	
	protected boolean tomerge;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		
		Parameter def = LGPDefaults.base().push(SPECIFIC_GRAPH_CROSSOVER_MULTITASK);
		
//		shared_ins_rate = state.parameters.getDoubleWithDefault(base.push(P_SHAREDRATE),def.push(P_SHAREDRATE),0.0);
//		if(shared_ins_rate<0){
//			 state.output.fatal("SelectiveGraphCrossover of multitask has an invalid shared instruction swapping rate (it must be >= 0).",
//					 base.push(P_SHAREDRATE),def.push(P_SHAREDRATE));
//		}
		
		tossSecondParent = state.parameters.getBoolean(base.push(P_TOSS),
	            def.push(P_TOSS),false);
		
		//tossSecondParent = false; //because in MPMO, individuals from different sub populations have different number of objectives, they cannot be exchanged directly
		
		tomerge = state.parameters.getBoolean(base.push(DO_MERGE), def.push(DO_MERGE), false);
		
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

      //get the trial subpopulation for the origin-based offspring reservation
//        int trial = subpopulation;
//        while(state.population.subpops.length > 1 && trial == subpopulation){
//        	trial = state.random[thread].nextInt(state.population.subpops.length);
//        }
        int trial = getLegalTrialPopulation(state, thread, subpopulation);

        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        for(int q=start, parnt = 0;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
           
        	int tarSF = state.random[0].nextInt(((MFEA_Evaluator)state.evaluator).getNumTasks());
        	
        	if ((trial>0 && subpopulation==0)){
        		tarSF = ((LGPIndividual_MPMO)state.population.subpops[trial].individuals[0]).skillFactor;
        	}
        	else if (subpopulation>0 && trial==0){
        		tarSF = ((LGPIndividual_MPMO)state.population.subpops[subpopulation].individuals[0]).skillFactor;
        	}
        	else if (subpopulation>0 && subpopulation == trial){
        		tarSF = ((LGPIndividual_MPMO)state.population.subpops[trial].individuals[0]).skillFactor;
        	}
        	
//        	((TournamentSel_SpecificIns)sources[0]).produce(1,1,0,subpopulation,parents,state,thread, tarSF);
//        	((TournamentSel_SpecificIns)sources[1]).produce(1,1,1,trial,parents,state,thread, tarSF);
        	
        	if(subpopulation > 0){
        		sources[0].produce(1,1,0,subpopulation,parents,state,thread);
        	}
        	else{
        		((TournamentSel_SpecificIns)sources[0]).produce(1,1,0,subpopulation,parents,state,thread, tarSF);
        	}
        	if(trial > 0){
        		sources[1].produce(1,1,1,trial,parents,state,thread);
        	}
        	else{
        		((TournamentSel_SpecificIns)sources[1]).produce(1,1,1,trial,parents,state,thread, tarSF);
        	}
        	
        	if(tomerge && state.population.subpops.length > 1){
        		((TournamentSel_SpecificIns)sources[0]).produce_merge(1,1,0,subpopulation, trial, parents,state,thread,tarSF);
            	((TournamentSel_SpecificIns)sources[1]).produce_merge(1,1,1,subpopulation, trial, parents,state,thread,tarSF);
        	}
        	
        	//here the selection of parents can also be implemented by selecting parents by TournamentScalarRank until the skill factors of two parents are the same
//        	for(int t = 0;t<numTries;t++){
//        		if (sources[0]==sources[1])  // grab from the same source
//                    sources[0].produce(2,2,0,subpopulation,parents,state,thread);
//                else // grab from different sources
//                    {
//                    sources[0].produce(1,1,0,subpopulation,parents,state,thread);
//                    sources[1].produce(1,1,1,subpopulation,parents,state,thread);
//                    }
//        		
//        		if(((LGPIndividual_MFEA)parents[parnt]).skillFactor == ((LGPIndividual_MFEA)parents[(parnt + 1)%parents.length]).skillFactor)
//        			break;
//        	}
            
            // at this point, parents[] contains our two selected individuals
            
            LGPIndividual[] parnts = new LGPIndividual[2];
        	for(int ind = 0 ; ind < parnts.length; ind++){
        		parnts[ind] = (LGPIndividual) this.parents[ind]; 
        	}
        	
            q += this.produce(min, max, start, subpopulation, inds, state, thread, parnts, tarSF);
            
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
	        final LGPIndividual[] parents,
	        final int taskid) {
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
	        j1 = ((LGPIndividual)parents[parnt]).lightClone();
	    	t1 = parnt;
	        j2 = ((LGPIndividual)parents[(parnt + 1)%parents.length]).lightClone();
	        t2 = (parnt + 1)%parents.length;
	        
	        //decide which kinds of crossover should be performed
	        Set<Integer> outRegs1 = new HashSet<>();
        	Set<Integer> outRegs2 = new HashSet<>();
        	int targetSF = taskid;//((LGPIndividual_MPMO)parents[0]).skillFactor;
        	
        	if(j1.getOutputRegisters().length > 1 && j2.getOutputRegisters().length > 1 && targetSF < j1.getOutputRegisters().length){
        		outRegs1.add(j1.getOutputRegisters()[targetSF]);
        		outRegs2.add(j2.getOutputRegisters()[targetSF]);
        	}
        	else{
        		for(int or : j1.getOutputRegisters()){
        			outRegs1.add(or);
        		}
        		for(int or : j2.getOutputRegisters()){
        			outRegs2.add(or);
        		}
        	}
//        	if(j1.getOutputRegisters().length > 1 && targetSF < j1.getOutputRegisters().length){
//        		outRegs1.add(j1.getOutputRegisters()[targetSF]);
//        	}
//        	else{ //there is only one output register for the parent
//        		outRegs1.add(j1.getOutputRegisters()[0]);
//        	}
//        	if(j2.getOutputRegisters().length > 1 && targetSF < j2.getOutputRegisters().length){
//        		outRegs2.add(j2.getOutputRegisters()[targetSF]);
//        	}
//        	else{
//        		outRegs2.add(j2.getOutputRegisters()[0]);
//        	}
        	
        	outRegs1.retainAll(outRegs2);
        	if(outRegs1.size() == 0){
        		state.output.message("akward situation in SelectiveGraphXov_interTask, in which there is no intersection in two parents' output registers\n");
        	}
//        	else if (outRegs1.size() == 1){
//        		q += super.produce(min, max, start, subpopulation, inds, state, thread, parents);
//        	}
        	else{
        		// the intersection of output registers is larger than 1, which means we can select to perform crossover on specific output register
        		//identify the sub graph of the selected output register, and select a segment as candidate swapping sub graph
        		
        		ArrayList<Integer> tmparray = new ArrayList<>(outRegs1);
        		
        		boolean flag = false;
    	        ArrayList<Integer> cand1 = new ArrayList<>();
    	        ArrayList<Integer> cand2 = new ArrayList<>();
    	        int maxdistance = 0;
    	        
    	        for(int t = 0;t<numTries;t++){
    	        	int target = tmparray.get(0);  //tmparray.get(state.random[thread].nextInt(outRegs1.size()));
    	        	
    	        	Integer tar[] = new Integer[1];
    	        	tar[0] = target;  
    	        	
    	        	ArrayList<Integer> graph1 = j1.getSubGraph(j1.getTreesLength()-1, tar);
            		ArrayList<Integer> graph2 = j2.getSubGraph(j2.getTreesLength()-1, tar);
    	        	
    	        	cand1.clear();
    	        	cand2.clear();
    	        	
    	            int begin1=j1.getTreesLength(), begin2=j2.getTreesLength(), tt = 0;
    	            for(;tt<numTries;tt++){
    	            	begin1 = state.random[thread].nextInt(j1.getTreesLength());
    	            	begin2 = state.random[thread].nextInt(j2.getTreesLength());
    	            	if(Math.abs(begin1 - begin2)<=MaxDistanceCrossPoint) break;
    	            }
    	            Iterator<Integer> it = graph1.iterator();
    	            while(it.hasNext()){
    	        		int ind = it.next();
    	        		if(ind <= begin1){
    	        			cand1.add(ind);
    	        		}
    	        	}
    	            it = graph2.iterator();
    	            while(it.hasNext()){
    	        		int ind = it.next();
    	        		if(ind <= begin2){
    	        			cand2.add(ind);
    	        		}
    	        	}

    	        	it = cand1.iterator();
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
    	//        	if(tmp_flag && distance >= maxdistance){
    	//        		cand1 = tmp_cand1;
    	//        		cand2 = tmp_cand2;
    	//        		maxdistance = distance;
    	//        		flag = tmp_flag;
    	//        	}
    	        }
    	        
    	        
    	        
    	        if(flag){
    	        	//replace j1's instructions by j2
    	        	graphSwapping(j1,(LGPIndividual)parents[t2],cand1,cand2, state, thread);
    	        	graphSwapping(j2,(LGPIndividual)parents[t1],cand2,cand1, state, thread);
    	        }
    	        
    	        if(microMutation != null) j1 = (LGPIndividual) microMutation.produce(subpopulation, j1, state, thread);
    	        if(microMutation != null) j2 = (LGPIndividual) microMutation.produce(subpopulation, j2, state, thread);
    	        
    	        //because different subpopulations may have different output registers, some just make them consistent
                LGPIndividual example = (LGPIndividual) state.population.subpops[subpopulation].individuals[0];
                j1.setOutputRegisters(example.getOutputRegisters());
        		j1.fitness = (MultiObjectiveFitness)example.fitness.clone();
        		((LGPIndividual_MPMO)j1).skillFactor = ((LGPIndividual_MPMO)example).skillFactor;
        		
        		j2.setOutputRegisters(example.getOutputRegisters());
        		j2.fitness = (MultiObjectiveFitness)example.fitness.clone();
        		((LGPIndividual_MPMO)j2).skillFactor = ((LGPIndividual_MPMO)example).skillFactor;
    	
    	        // add the individuals to the population
//    	        inds[q] = j1;
//    	        q++;
//    	        parnt ++;
//    	        if (q<n+start && !tossSecondParent)
//    	        {
//    	        	if(flag){
//    	        		//replace j2's instructions by j1
//    	        		graphSwapping(j2,(LGPIndividual)parents[t1],cand2,cand1, state, thread);
//    	        	}
//    	        	if(microMutation != null) j2 = microMutation.produce(subpopulation, j2, state, thread);
//    	        	
//    	            inds[q] = j2;
//    	            q++;
//    	            parnt ++;
//    	        }
    	        
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
                	if(!Arrays.equals(j1.getOutputRegisters(), j2.getOutputRegisters())){
                		j2.setOutputRegisters(j1.getOutputRegisters());
                		j2.fitness = (MultiObjectiveFitness)j1.fitness.clone();
                		((LGPIndividual_MPMO)j2).skillFactor = ((LGPIndividual_MPMO)j1).skillFactor;
                	}
                	
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
                    	
                    	if(!Arrays.equals(j1.getOutputRegisters(), j2.getOutputRegisters())){
                    		j1.setOutputRegisters(j2.getOutputRegisters());
                    		j1.fitness = (MultiObjectiveFitness)j2.fitness.clone();
                    		((LGPIndividual_MPMO)j1).skillFactor = ((LGPIndividual_MPMO)j2).skillFactor;
                    	}
                    	
        	            inds[q] = j1;
        	            q++;
        	            parnt ++;
                    }
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
				trial = cursubpop;
		}
		else if(cursubpop > 0){
			if(tomerge){
				trial = 0;
			}
			else
				trial = cursubpop;
			
		}
        return trial;
	}
}
