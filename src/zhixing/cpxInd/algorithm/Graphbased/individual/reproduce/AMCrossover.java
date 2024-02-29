package zhixing.cpxInd.algorithm.Graphbased.individual.reproduce;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNodeBuilder;
import ec.gp.GPType;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public abstract class AMCrossover extends zhixing.cpxInd.individual.reproduce.LGP2PointCrossoverPipeline{

public static final String ADJMAT_CROSSOVER = "AMcross";
	
	public static final String P_BUILDER = "build";
	
	public LGPMutationGrowBuilder builder;
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = LGPDefaults.base().push(ADJMAT_CROSSOVER);
		
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
            
            // are our tree values valid?
            if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= parents[0].getTreesLength()))
                // uh oh
                state.output.fatal("LGP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= parents[1].getTreesLength()))
                // uh oh
                state.output.fatal("LGP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

            int t1=0, t2=0;
            LGPIndividual4Graph j1, j2;
            if(((LGPIndividual4Graph)parents[parnt]).getTreesLength() <= ((LGPIndividual4Graph)parents[(parnt + 1)%parents.length]).getTreesLength()) {
            	j1 = ((LGPIndividual4Graph)parents[parnt]).lightClone();
            	t1 = parnt;
                j2 = ((LGPIndividual4Graph)parents[(parnt + 1)%parents.length]).lightClone();
                t2 = (parnt + 1)%parents.length;
            }
            else {
            	j2 = ((LGPIndividual4Graph)parents[parnt]).lightClone();
            	t2 = parnt;
                j1 = ((LGPIndividual4Graph)parents[(parnt + 1)%parents.length]).lightClone();
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
            
            //here the begin1 & 2, and pickNum 1 & 2 are ready. get their AMs and effective rate

            //effective rate
            //double extronrate1 = 0, extronrate2 = 0;
            //if(pickNum1>0) extronrate1 = j1.countStatus(begin1,  begin1+pickNum1) / pickNum1;
            //if(pickNum2>0) extronrate2 = j2.countStatus(begin2,  begin2+pickNum2) / pickNum2;
            
          //adjacency matrix
            int dimen = j1.getDimension();
            double [][] AM1 = new double[dimen][dimen];
            double [][] AM2 = new double[dimen][dimen];
            
            //double [][] normAM1 = new double[dimen][dimen];
            //double [][] normAM2 = new double[dimen][dimen];
            
            AM1 = j1.getAM(begin1, begin1+pickNum1);
            AM2 = j2.getAM(begin2, begin2+pickNum2);
            
            //assign a little probability to all primitives
            double sum1 = 0, sum2 = 0;
            for(int d = 0;d<dimen;d++){
            	sum1 = 0;
            	for(int dd = 0;dd<dimen;dd++){
            		AM1[d][dd] += 0.1;
            		//sum1 += AM1[d][dd];
            	}
//            	for(int dd = 0;dd<dimen;dd++){
//            		normAM1[d][dd] = AM1[d][dd] / sum1;
//            	}
            }
            for(int d = 0;d<dimen;d++){
            	sum2 = 0;
            	for(int dd = 0;dd<dimen;dd++){
            		AM2[d][dd] += 0.1;
            		//sum2 += AM2[d][dd];
            	}
//            	for(int dd = 0;dd<dimen;dd++){
//            		normAM2[d][dd] = AM2[d][dd] / sum2;
//            	}
            }
            
            
          //add trees in j1
    		//GPInitializer initializer = ((GPInitializer)state.initializer);
    		for(int p = 0;p<pickNum2;p++){
    			
    			//GPTreeStruct tree = sampleInstrBasedAM(j1, effRegs, extronrate2, AM2, state, thread);
    			GPTreeStruct tree = (GPTreeStruct) (parents[t2].getTree(begin2 + pickNum2 - 1 - p).clone());
        		//tree = (GPTree)(parents[parnt].getTree(pick).lightClone());
                tree.owner = j1;
                //tree.child = (GPNode)(parents[t2].getTree(begin2 + p).child.clone());
                sampleInstrBasedAM(j1, tree, begin1, 0.5, AM2, state, thread, tree.child.parentType(initializer),  tree.constraints(initializer).functionset);
                tree.child.parent = tree;
                tree.child.argposition = 0;
                j1.addTree(begin1+pickNum1, tree);
                j1.evaluated = false; 
    		}
    		
    		//remove trees in j1
    		for(int p = 0;p<pickNum1;p++) {
    			j1.removeTree(begin1);
    			j1.evaluated = false;
    		}
            
            if(microMutation != null) j1 = (LGPIndividual4Graph) microMutation.produce(subpopulation, j1, state, thread);
            if(eff_flag) j1.removeIneffectiveInstr();
            
            if (n-(q-start)>=2 && !tossSecondParent) {
            	
            	
            	//add trees in j2
        		for(int p = 0;p<pickNum1;p++){
        			GPTreeStruct tree = (GPTreeStruct) (parents[t1].getTree(begin1 + pickNum1 - 1 - p).clone());
            		//tree = (GPTree)(parents[parnt].getTree(pick).lightClone());
                    tree.owner = j2;
                    //tree.child = (GPNode)(parents[t1].getTree(begin1 + p).child.clone());
                    sampleInstrBasedAM(j2, tree, begin2, 0.5, AM1, state, thread, tree.child.parentType(initializer),  tree.constraints(initializer).functionset);
                    tree.child.parent = tree;
                    tree.child.argposition = 0;
                    j2.addTree(begin2+pickNum2, tree);
                    j2.evaluated = false; 
        		}
        		
        		//remove trees in j2
        		for(int p = 0;p<pickNum2;p++) {
        			j2.removeTree(begin2);
        			j2.evaluated = false;
        		}
            	
            	if(microMutation != null) j2 = (LGPIndividual4Graph) microMutation.produce(subpopulation, j2, state, thread);
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
	
	protected abstract void sampleInstrBasedAM(
			LGPIndividual4Graph offspring,
			GPTreeStruct tree,
			int insert, 
			//double effRate, 
			double consRate,
			double [][] AM, 
			//double [][] normAM,
			EvolutionState state, 
			int thread,
			final GPType type,
			//final GPNodeParent parent,
	        //final int argposition,
	        final GPFunctionSet set);
	
	protected int sampleNextFunBasedAM(int parentInd, double [][]AM,  int dimension1, int dimension2, EvolutionState state, int thread) {
		//dimension1: number of functions, dimension2: number of fun + constant
		int res = state.random[thread].nextInt(dimension1);
		
		double fre []=new double [dimension1];
		for(int d = 0;d<dimension1;d++) {
			fre[d] = AM[parentInd][d];
		}
		
		double sum = 0;
		for(int i = 0;i<dimension1;i++) {
			sum+=fre[i];
		}
		for(int i = 0;i<dimension1;i++) {
			fre[i] /= sum;
		}
		
		double prob = state.random[thread].nextDouble();
		double tmp = 0;
		for(int f = 0;f<dimension1;f++) {
			tmp += fre[f];
			if(tmp>prob) {
				res = f;
				break;
			}
		}
		
		return res;
	}
	
	protected int sampleFunBasedAMOutDegree(double [][] AM, int dimension1, int dimension2, EvolutionState state, int thread) {
		int res = state.random[thread].nextInt(dimension1);
		
		double fre []=new double [dimension1];
		for(int i = 0;i<dimension1;i++) {
			for(int d = 0;d<dimension2;d++) {
				fre[i] += AM[i][d];
			}
		}
		
		double sum = 0;
		for(int i = 0;i<dimension1;i++) {
			sum+=fre[i];
		}
		for(int i = 0;i<dimension1;i++) {
			fre[i] /= sum;
		}
		
		double prob = state.random[thread].nextDouble();
		double tmp = 0;
		for(int f = 0;f<dimension1;f++) {
			tmp += fre[f];
			if(tmp>prob) {
				res = f;
				break;
			}
		}
		
		return res;
	}
	
	protected int sampleConsBasedAM(int parentInd, double [][]AM,  int dimension1, int dimension2, EvolutionState state, int thread) {
		int res = state.random[thread].nextInt(dimension2 - dimension1);
		
		double fre []=new double [dimension2 - dimension1];
		for(int d = dimension1;d<dimension2;d++) {
			fre[d-dimension1] = AM[parentInd][d];
		}
		
		double sum = 0;
		for(int i = 0;i<dimension2-dimension1;i++) {
			sum+=fre[i];
		}
		for(int i = 0;i<dimension2-dimension1;i++) {
			fre[i] /= sum;
		}
		
		double prob = state.random[thread].nextDouble();
		double tmp = 0;
		for(int f = 0;f<dimension2-dimension1;f++) {
			tmp += fre[f];
			if(tmp>prob) {
				res = f;
				break;
			}
		}
		
		return res;
	}
}
