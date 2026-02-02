package zhixing.cpxInd.algorithm.Multiform.individual.reproduce;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPBreedingPipeline;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPNodeParent;
import ec.gp.GPTree;
import ec.gp.GPType;
import ec.gp.koza.CrossoverPipeline;
import ec.gp.koza.HalfBuilder;
import ec.gp.koza.KozaBuilder;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;
import zhixing.cpxInd.algorithm.Multiform.individual.LGPIndividual4MForm;
import zhixing.cpxInd.algorithm.Multiform.individual.TGPIndividual4MForm;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGP2PointCrossoverPipeline;
import zhixing.cpxInd.individual.reproduce.LGPMacroMutationPipeline;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;
import ec.gp.koza.KozaNodeSelector;
import ec.gp.koza.MutationPipeline;

public abstract class ATCrossover4TGP extends CrossoverPipeline {
	//the receiver is a TGP individual
	
	public static final String ADJTABLE_CROSSOVER_TGP = "ATcross_tgp";
	
	public static final String P_BUILDER = "build";
	
	public static final String P_MUTBASE = "mut_base";
	public static final String P_CROSSBASE = "cross_base";
	
	public static final String P_MUTRATE = "mut_rate";
	public static final String P_CROSSRATE = "cross_rate";
	
//	public static final String P_MAXLENGTH_SEG = "maxseglength";
//	
//	public static final String P_MAXLENDIFF_SEG = "maxlendiffseg";
//	
//	public int MaxSegLength;
//	
//	public int MaxLenDiffSeg;
	
	public HalfBuilder builder;

	protected MutationPipeline mutation;
	protected CrossoverPipeline crossover;
	
	protected double mutrate;
	protected double crossrate;
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = LGPDefaults.base().push(ADJTABLE_CROSSOVER_TGP);
		
//		MaxSegLength = state.parameters.getInt(base.push(P_MAXLENGTH_SEG),
//	            def.push(P_MAXLENGTH_SEG),1);
//	        if (MaxSegLength == 0)
//	            state.output.fatal("LGPCrossover Pipeline has an invalid number of maxseglength (it must be >= 1).",base.push(P_MAXLENGTH_SEG),def.push(P_MAXLENGTH_SEG));
//	        
//	    MaxLenDiffSeg = state.parameters.getInt(base.push(P_MAXLENDIFF_SEG),def.push(P_MAXLENDIFF_SEG),0);
//        if (MaxLenDiffSeg<0)
//            state.output.fatal("LGPCrossover Pipeline has an invalid maximum length difference of segments (it must be >= 0).",base.push(P_MAXLENDIFF_SEG),def.push(P_MAXLENDIFF_SEG));
		
		Parameter p = base.push(P_BUILDER).push(""+0);
		Parameter d = def.push(P_BUILDER).push(""+0);        
        
		builder = (HalfBuilder)
	            (state.parameters.getInstanceForParameter(
	                p,d, GPNodeBuilder.class));
	        builder.setup(state,p);
	        
	    
	        Parameter mutbase = new Parameter(state.parameters.getString(base.push(P_MUTBASE), def.push(P_MUTBASE)));
	        mutation = null;
	        if(!mutbase.toString().equals("null")){
	        	//microMutation = new LGPMicroMutationPipeline();
	        	mutation = (MutationPipeline)(state.parameters.getInstanceForParameter(
	                    mutbase, def.push(P_MUTBASE), GPBreedingPipeline.class));
	   		 mutation.setup(state, mutbase);
	        }
	        mutrate =  state.parameters.getDouble(base.push(P_MUTRATE),def.push(P_MUTRATE),0);
	        if (mutrate<0)
	            state.output.fatal("ATCrossover4TGP has an invalid mutation rate (it must be >= 0 && <= 1).",base.push(P_MUTRATE),def.push(P_MUTRATE));
	        
	        Parameter crossbase = new Parameter(state.parameters.getString(base.push(P_CROSSBASE), def.push(P_CROSSBASE)));
	        crossover = null;
	        if(!crossbase.toString().equals("null")){
	        	//microMutation = new LGPMicroMutationPipeline();
	        	crossover = (CrossoverPipeline)(state.parameters.getInstanceForParameter(
	                    crossbase, def.push(P_CROSSBASE), GPBreedingPipeline.class));
	   		 crossover.setup(state, crossbase);
	        }
	        crossrate =  state.parameters.getDouble(base.push(P_CROSSRATE),def.push(P_CROSSRATE),0);
	        if (crossrate<0)
	            state.output.fatal("ATCrossover4TGP has an invalid crossover rate (it must be >= 0 && <= 1).",base.push(P_CROSSRATE),def.push(P_CROSSRATE));     
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
            // grab two individuals from our sources
            
            sources[0].produce(1,1,0,subpopulation,parents,state,thread);  //TGP individual, receiver
            sources[1].produce(1,1,1,(subpopulation+1)%state.population.subpops.length,parents,state,thread); // LGP or TGP individual, donor
            
            // at this point, parents[] contains our two selected individuals
            CpxGPIndividual[] parnts = new CpxGPIndividual[2];
        	for(int ind = 0 ; ind < parnts.length; ind++){
        		parnts[ind] = (CpxGPIndividual) this.parents[ind]; 
        	}
        	
        	q += this.produce(min, max, start, subpopulation, inds, state, thread, parnts);
        	
        	
        	
//        	if (/*parnts[1].fitness.betterThan(parnts[0].fitness) && */ 
//        			!((TGPIndividual4MForm)parnts[0]).issameRepresentation(parnts[1])) {  //if the parent from all the population is better than the parent from the current population 
//        		q += this.produce(min, max, start, subpopulation, inds, state, thread, parnts);
//        	}
//        	else { //the current population is in a leading position
//        		double rate = state.random[thread].nextDouble();
//        		
//        		if(rate <= crossrate) {      			
//        			q += crossover.produce(min, max, start, subpopulation, inds, state, thread);
//        		}
//        		else if (rate <= crossrate + mutrate) {
//        			q += mutation.produce(min, max, start, subpopulation, inds, state, thread);
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
            
            TGPIndividual4MForm j1;
            CpxGPIndividual j2;
            int t1=0, t2=0;
	        j1 = (TGPIndividual4MForm) (parents[parnt]).lightClone();
	        
	        if (parents[parnt].getTreesLength() > 1) 
                t1 = state.random[thread].nextInt(parents[0].getTreesLength());
            else t1 = 0;
	        
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
	        
	        // prepare the nodeselectors
            nodeselect1.reset();
            nodeselect2.reset();

	        GPNode p1=null, p2=null, newp=null;
	        int numFunNode1 = 0, numFunNode2 = 0;

	        for(int t = 0;t<numTries;t++){
	        	//get j1's crossover point  
	        	
	        	p1 = ((KozaNodeSelector)nodeselect1).pickNode(state,subpopulation,thread,j1,j1.getTree(t1));
	        	cand1 = ((TGPIndividual4MForm)j1).getAdjacencyTable(p1);
	        	
	        	//get j2's AT
	        	if(j2 instanceof TGPIndividual4MForm){
		        	p2 = ((KozaNodeSelector)nodeselect2).pickNode(state,subpopulation,thread,j2,j2.getTree(t2));
		        	cand2 = ((TGPIndividual4MForm)j2).getAdjacencyTable(p2);
		        	
		        	//cand2 = ((TGPIndividual4MForm)j2).getAdjacencyTable(j2.getTree(t2).child);
		        	
//		        	if(cand2.size()>0)
//		        	{
////		        		for(int tt = 0;tt<numTries;tt++) {
////		        			begin2 = state.random[thread].nextInt(cand2.size());
////		        			if(Math.abs(begin1 - (cand2.size() - begin2))<=MaxDistanceCrossPoint) break;
////		        		}
//		        		
//		        		int begin2 = state.random[thread].nextInt(cand2.size());
//		        		int pickNum2 = state.random[thread].nextInt(Math.min(begin2+1, MaxSegLength)) + 1;
//		        		
//		        		Iterator<Pair<String, ArrayList<String>>> it = cand2.iterator();
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
	        		int begin2 = state.random[thread].nextInt(j2.getTreesLength());
//	        		int pickNum2 = state.random[thread].nextInt(j2.getTreesLength() - begin2) + 1;
	        		
	        		int end2 = state.random[thread].nextInt(j2.getTreesLength()-begin2)+begin2+1;
	        		
	        		numFunNode2 = 0;
	        		for(int i = begin2; i< j2.getTreesLength(); i++) {
	        			numFunNode2 += ((LGPIndividual)j2).getTreeStruct(i).child.numNodes(GPNode.NODESEARCH_NONTERMINALS); 
	    	        }
	        		
	        		int pickNum2 = state.random[thread].nextInt(numFunNode2) + 1;
	        		
	        		//int pickNum2 = state.random[thread].nextInt(Math.min(j2.getTreesLength() - begin2, MaxSegLength)) + 1;

	        		cand2 = ((LGPIndividual)j2).getAdjacencyTable(state, begin2, j2.getTreesLength());
//	        		cand2 = ((LGPIndividual4MForm)j2).getAdjacencyTable(0, begin2+1);
	        		Iterator<Pair<String, ArrayList<String>>> it = cand2.iterator();
	            	int cnt = cand2.size();
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
//	        		cand2 = ((LGPIndividual4MForm)j2).getAdjacencyTable(0, j2.getTreesLength());
	            	
	        	} 	
            	
//	        	if(/*Math.abs(cand2.size() - cand1.size())>MaxSegLength ||*/ cand2.size() == 0 || cand1.size() == 0) continue;
	        	
	        	if(cand2.size()>0)
	            newp = growNodeBasedAT(state,p1.atDepth(),maxDepth,p1.parentType(initializer),thread,p1.parent,
	            		p1.argposition, j1.getTree(t1).constraints(initializer).functionset,cand2,0);
	        	else continue;
	            
//	        	newp = growNodeBasedAT(state,0,maxDepth,p1.parentType(initializer),thread,p1.parent,
//	            		p1.argposition, j1.getTree(t1).constraints(initializer).functionset,cand2,0);
	        	
	        	flag = verifyPoints(initializer,newp,p1);
	        	
	        	if(flag) break;

	        }

	       if(flag) {
	    	   
	    	   for(int x=0;x<j1.getTreesLength();x++)
               {
	    		   GPTree tree = j1.getTree(x);
	               if (x==t1)  // we've got a tree with a kicking cross position!
                   { 
	                   tree = (GPTree)(parents[parnt].getTree(x).lightClone());
	                   tree.owner = j1;
	                   tree.child = parents[parnt].getTree(x).child.cloneReplacing(newp,p1); 
	                   tree.child.parent = tree;
	                   tree.child.argposition = 0;
	                   j1.setTree(x, tree);
	                   j1.evaluated = false; 
                   }  // it's changed
	               else 
                   {
	                   tree = (GPTree)(parents[0].getTree(x).lightClone());
	                   tree.owner = j1;
	                   tree.child = (GPNode)(parents[0].getTree(x).child.clone());
	                   tree.child.parent = tree;
	                   tree.child.argposition = 0;
	                   j1.setTree(x, tree);
                   }
               }
	    	   
	       }
       	
	        // add the individuals to the population
	        inds[q] = j1;
	        q++;
	        parnt ++;
	        
        }
            
        return n;
    }
	
//	protected boolean verifypoint(TGPIndividual4MForm trial, ArrayList<Pair<String, ArrayList<String>>> cand){
//		//we only have the maximum program size limitation 
//		//the cand cannot be null or no elements
//		
//	}
	
	
	abstract protected GPNode growNodeBasedAT(final EvolutionState state,
			final int current,
	        final int max,
	        final GPType type,
	        final int thread, 
	        final GPNodeParent parent,
	        final int argposition,
	        final GPFunctionSet set,
	        ArrayList<Pair<String, ArrayList<String>>> cand,
	        int ATindex);
	
	
}
