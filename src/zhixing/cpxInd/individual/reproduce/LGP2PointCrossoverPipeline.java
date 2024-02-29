package zhixing.cpxInd.individual.reproduce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.spiderland.Psh.booleanStack;
import org.spiderland.Psh.intStack;

import com.lowagie.text.xml.SAXiTextHandler;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.koza.CrossoverPipeline;
import ec.gp.koza.GPKozaDefaults;
import ec.gp.koza.MutationPipeline;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.Grammar.individual.LGPIndividual4Grammar;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.djss.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA4DJSS;

public class LGP2PointCrossoverPipeline extends CrossoverPipeline{
	//This crossover operator will insert a whole sequence of instructions from the other individual
	public static final String P_MAXLENGTH_SEG = "maxseglength";
	public static final String P_MAXLENDIFF_SEG = "maxlendiffseg";
	public static final String P_MAXDIS_CROSS_POINT = "maxdistancecrosspoint";
	
	public static final String TWOPOINT_CROSSOVER = "2pcross";
	
	public static final String P_MICROMUTBASE = "micro_base";
	
	public static final String P_EFFECTIVE = "effective";
	
	public int MaxSegLength;
	
	public int MaxLenDiffSeg;
	
	public int MaxDistanceCrossPoint;
	
	protected LGPMicroMutationPipeline microMutation;
	
	public boolean eff_flag; //whether we need effective crossover
	
	//public Parameter defaultBase() { return LGPDefaults.base().push(TWOPOINT_CROSSOVER); }
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = LGPDefaults.base().push(TWOPOINT_CROSSOVER);
		
		MaxSegLength = state.parameters.getInt(base.push(P_MAXLENGTH_SEG),
	            def.push(P_MAXLENGTH_SEG),1);
	        if (MaxSegLength == 0)
	            state.output.fatal("LGPCrossover Pipeline has an invalid number of maxseglength (it must be >= 1).",base.push(P_MAXLENGTH_SEG),def.push(P_MAXLENGTH_SEG));
	        
	    MaxLenDiffSeg = state.parameters.getInt(base.push(P_MAXLENDIFF_SEG),def.push(P_MAXLENDIFF_SEG),0);
        if (MaxLenDiffSeg<0)
            state.output.fatal("LGPCrossover Pipeline has an invalid maximum length difference of segments (it must be >= 0).",base.push(P_MAXLENDIFF_SEG),def.push(P_MAXLENDIFF_SEG));
        
        MaxDistanceCrossPoint =  state.parameters.getInt(base.push(P_MAXDIS_CROSS_POINT),def.push(P_MAXDIS_CROSS_POINT),0);
        if (MaxDistanceCrossPoint<0)
            state.output.fatal("LGPCrossover Pipeline has an invalid maximum distance of crossover points (it must be >= 0).",base.push(P_MAXDIS_CROSS_POINT),def.push(P_MAXDIS_CROSS_POINT));
        
        Parameter microbase = new Parameter(state.parameters.getString(base.push(P_MICROMUTBASE), def.push(P_MICROMUTBASE)));
        microMutation = null;
        if(!microbase.toString().equals("null")){
        	//microMutation = new LGPMicroMutationPipeline();
        	microMutation = (LGPMicroMutationPipeline)(state.parameters.getInstanceForParameter(
                    microbase, def.push(P_MICROMUTBASE), MutationPipeline.class));
   		 microMutation.setup(state, microbase);
        }
        
        eff_flag = state.parameters.getBoolean(base.push(P_EFFECTIVE),def.push(P_EFFECTIVE),false);
	}
	
	@Override
	public boolean verifyPoints(final GPInitializer initializer,
	        final GPNode inner1, final GPNode inner2)
	        {
	        // first check to see if inner1 is swap-compatible with inner2
	        // on a type basis
	        if (!inner1.swapCompatibleWith(initializer, inner2)) return false;

	        // next check to see if inner1 can fit in inner2's spot
	        if (inner1.depth()+inner2.atDepth() > maxDepth) return false;

	        // check for size
	        // NOTE: this is done twice, which is more costly than it should be.  But
	        // on the other hand it allows us to toss a child without testing both times
	        // and it's simpler to have it all here in the verifyPoints code.  
	        if (maxSize != NO_SIZE_LIMIT)
	            {
	            // first easy check
	            int inner1size = inner1.numNodes(GPNode.NODESEARCH_ALL);
	            int inner2size = inner2.numNodes(GPNode.NODESEARCH_ALL);
	            if (inner1size > inner2size)  // need to test further
	                {
	                // let's keep on going for the more complex test
	                GPNode root2 = ((GPTree)(inner2.rootParent())).child;
	                int root2size = root2.numNodes(GPNode.NODESEARCH_ALL);
	                if (root2size - inner2size + inner1size > maxSize)  // take root2, remove inner2 and swap in inner1.  Is it still small enough?
	                    return false;
	                }
	            }
	        
	        //check the depth of the to-be-replaced node and the new node
	        //if only one of them is at depth 0, return false
	        if(inner1.atDepth() != inner2.atDepth() && (inner1.atDepth() == 0 || inner2.atDepth() == 0)) 
	        	return false;

	        // checks done!
	        return true;
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
	        final Individual[] parents) 

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
	            
	            // at this point, parents[] contains our two selected individuals
	            
	        	// are our tree values valid?
	            if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= ((LGPIndividual)parents[0]).getTreesLength()))
	                // uh oh
	                state.output.fatal("LGP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
	            if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= ((LGPIndividual)parents[1]).getTreesLength()))
	                // uh oh
	                state.output.fatal("LGP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

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
//	            	else{
//	            		pickNum2 = pickNum1 = Math.min(pickNum1, pickNum2);
//	            	}
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
	            
	            for(int pick = 0; pick < ((LGPIndividual)parents[t1]).getTreesLength(); pick ++){
	            	if(pick == begin1){
	            		//remove trees in j1
	            		for(int p = 0;p<pickNum1;p++) {
	            			j1.removeTree(pick);
	            			j1.evaluated = false;
	            		}
	            		
	            		//add trees in j1
	            		for(int p = 0;p<pickNum2;p++){
	            			GPTreeStruct tree = (GPTreeStruct) (((LGPIndividual)parents[t2]).getTree(begin2 + p).clone());
	                		//tree = (GPTree)(parents[parnt].getTree(pick).lightClone());
	                        tree.owner = j1;
	                        tree.child = (GPNode)(((LGPIndividual)parents[t2]).getTree(begin2 + p).child.clone());
	                        tree.child.parent = tree;
	                        tree.child.argposition = 0;
	                        j1.addTree(pick + p, tree);
	                        j1.evaluated = false; 
	            		}
	            	}
	            	
	            }
	            
	            if(microMutation != null) j1 = (LGPIndividual) microMutation.produce(subpopulation, j1, state, thread);
	            if(eff_flag) j1.removeIneffectiveInstr();
	            
	            if (n-(q-start)>=2 && !tossSecondParent) {
	            	for(int pick = 0; pick < ((LGPIndividual)parents[t2]).getTreesLength(); pick++) {
	            		if(pick == begin2){
	                		//remove trees in j2
	                		for(int p = 0;p<pickNum2;p++) {
	                			j2.removeTree(pick);
	                			j2.evaluated = false;
	                		}
	                		
	                		//add trees in j2
	                		for(int p = 0;p<pickNum1;p++){
	                			GPTreeStruct tree = (GPTreeStruct) (((LGPIndividual)parents[t1]).getTree(begin1 + p).clone());
	                    		//tree = (GPTree)(parents[parnt].getTree(pick).lightClone());
	                            tree.owner = j2;
	                            tree.child = (GPNode)(((LGPIndividual)parents[t1]).getTree(begin1 + p).child.clone());
	                            tree.child.parent = tree;
	                            tree.child.argposition = 0;
	                            j2.addTree(pick + p, tree);
	                            j2.evaluated = false; 
	                		}
	                	}
	            	}
	            	
	            	if(microMutation != null) j2 = (LGPIndividual) microMutation.produce(subpopulation, j2, state, thread);
	            	if(eff_flag) j2.removeIneffectiveInstr();
	            }
	            
	            // add the individuals to the population
	            if(j1.getTreesLength() < j1.getMinNumTrees() || j1.getTreesLength() > j1.getMaxNumTrees()){
	            	System.out.println(start);
	            	System.out.println(""+begin1+" "+pickNum1+" "+begin2+" "+pickNum2);
	            	System.out.println(""+j1.getTreesLength()+" "+j2.getTreesLength());
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
		            inds[q] = j2;
		            q++;
		            parnt ++;
	            }
	            
	            }
	            
	        return n;
	        }
}
