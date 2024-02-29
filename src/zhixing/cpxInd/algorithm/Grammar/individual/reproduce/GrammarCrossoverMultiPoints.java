package zhixing.cpxInd.algorithm.Grammar.individual.reproduce;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.Grammar.individual.DTNode;
import zhixing.cpxInd.algorithm.Grammar.individual.GrammarLGPDefaults;
import zhixing.cpxInd.algorithm.Grammar.individual.LGPIndividual4Grammar;
import zhixing.cpxInd.individual.LGPIndividual;

public class GrammarCrossoverMultiPoints extends GrammarCrossover {
	public static final String P_GRAMMARCROSSOVERMultiPoint = "grammarcrossoverMP";
	public static final String P_MAXNUMCROSSOVERPOINTS = "maxnumcrosspoints";
	
	protected int maxnumCrossPoint = 1;
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state,base);
		
		Parameter def = GrammarLGPDefaults.base().push(P_GRAMMARCROSSOVERMultiPoint);
		
		maxnumCrossPoint = state.parameters.getIntWithDefault(base.push(P_MAXNUMCROSSOVERPOINTS), def.push(P_MAXNUMCROSSOVERPOINTS), 1);
		if(maxnumCrossPoint<1) {
            state.output.fatal("Grammar LGP crossover multiple points Pipeline has an invalid number of number of crossover points (it must be >= 1).",base.push(P_MAXNUMCROSSOVERPOINTS), def.push(P_MAXNUMCROSSOVERPOINTS));
		}
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
	            	j1 = ((LGPIndividual4Grammar)parents[parnt]).lightClone();
	            	t1 = parnt;
	                j2 = ((LGPIndividual4Grammar)parents[(parnt + 1)%parents.length]).lightClone();
	                t2 = (parnt + 1)%parents.length;
	            }
	            else {
	            	j2 = ((LGPIndividual4Grammar)parents[parnt]).lightClone();
	            	t2 = parnt;
	                j1 = ((LGPIndividual4Grammar)parents[(parnt + 1)%parents.length]).lightClone();
	                t1 = (parnt + 1)%parents.length;
	            }
	            
	            
	            DTNode donorNode=null, recNode=null;
	            
	            DTNodePicker donorPicker=new DTNodePicker(), recPicker = new DTNodePicker();
	            //now the two parents are ready
	            
	            int numCrossPoints = state.random[thread].nextInt(maxnumCrossPoint)+1; //select numCrossPoints pairs of crossover points
//	            int numCrossPoints = maxnumCrossPoint;
	            
//	            int begin1 = state.random[thread].nextInt(j1.getTreesLength());
//	            int pickNum1 = state.random[thread].nextInt(Math.min(j1.getTreesLength() - begin1, MaxSegLength)) + 1;
//	            
//	            int feasibleLowerB = Math.max(0, begin1 - MaxDistanceCrossPoint);
//	            int feasibleUpperB = Math.min(j2.getTreesLength() - 1, begin1 + MaxDistanceCrossPoint);
//	            
//	            int begin2 = feasibleLowerB + state.random[thread].nextInt(feasibleUpperB - feasibleLowerB + 1);
//	            int pickNum2 = 1 + state.random[thread].nextInt(Math.min(j2.getTreesLength() - begin2, MaxSegLength));
	            
	            int pickNum1 = state.random[thread].nextInt(Math.min(((LGPIndividual) parents[t2]).getTreesLength(), MaxSegLength)) + 1;
	            int pickNum2 = 1 + state.random[thread].nextInt(Math.min(j1.getTreesLength(), MaxSegLength));
	            int sumpickNum1 = 0, sumpickNum2 = 0;
	            
	            ArrayList<DTNode> crossPointsListDonor = new ArrayList<>();
	            ArrayList<DTNode> crossPointsListRec = new ArrayList<>();
	            ArrayList<DTNodePicker> pickerListDonor = new ArrayList<>();
	            ArrayList<DTNodePicker> pickerListRec = new ArrayList<>();
	            
	            for(int c = 0;c<numTries;c++) {
	            	//randomly select one node from parent2 (serve as donor), randomly select one node with the same type from j1 (serve as receiver)
	            	donorNode=null;
	            	recNode=null;
	            	donorPicker=new DTNodePicker();
	            	recPicker = new DTNodePicker();
	            	
		            for(int tr = 0; tr<numTries && !checkPoints((LGPIndividual4Grammar)parents[t2], (LGPIndividual4Grammar) j1, donorNode, recNode, donorPicker, recPicker); tr++) {
		            	donorNode=null;
		            	recNode=null;
		            	
		            	if(state.random[thread].nextDouble()<growrate) {
		            		donorNode = ((LGPIndividual4Grammar)parents[t2]).getDerivationTree().randomlyPickExtendNode(state, thread, null, thresold, crossPointsListDonor, false);
//		            		donorNode = ((LGPIndividual4Grammar)parents[t2]).getDerivationTree().randomlyPickExtendNode(state, thread, null, thresold, 
//		            				(LGPIndividual4Grammar)parents[t2], begin2, pickNum2, crossPointsListDonor);
		            	}
		            	else {
		            		donorNode = ((LGPIndividual4Grammar)parents[t2]).getDerivationTree().randomlyPickNode(state, thread, null, false);
//		            		donorNode = ((LGPIndividual4Grammar)parents[t2]).getDerivationTree().randomlyPickNode(state, thread, null,
//		            				(LGPIndividual4Grammar)parents[t2], begin2, pickNum2, crossPointsListDonor);
		            	}
		            	
		            	if(donorNode != null) {
		            		recNode = ((LGPIndividual4Grammar)j1).getDerivationTree().randomlyPickCompatibleNode(state, thread, donorNode, crossPointsListRec, false);
		            	}

		            	if(recNode != null) {
		            		
		            		//pick the siblings or instructions from donorNode
		            		donorPicker.randomPick(state, thread, donorNode, false);
		            		
		            		//pick the siblings or instructions from recNode
		            		recPicker.randomPick(state, thread, recNode, false);
		            		
		            	}
		            }
		            
		            if(checkPoints((LGPIndividual4Grammar)parents[t2], (LGPIndividual4Grammar) j1, donorNode, recNode, donorPicker, recPicker)) {
		            	
		            	//check whether the new comming nodes overlap with existing nodes in the list
		            	boolean overlap = false;
		            	if(crossPointsListDonor.size()>0 && crossPointsListRec.size()>0) {
		            		for(DTNode node : crossPointsListDonor) {
		            			if(node.hasChildOf(donorNode)||donorNode.hasChildOf(node)) {
		            				overlap = true;
		            				break;
		            			}
		            		}
		            		
		            		for(DTNode node : crossPointsListRec) {
		            			if(node.hasChildOf(recNode)||recNode.hasChildOf(node)) {
		            				overlap = true;
		            				break;
		            			}
		            		}
		            	}
		            	
		            	//add them into the list
		            	if(!overlap) {
		            		crossPointsListDonor.add(donorNode);
		            		crossPointsListRec.add(recNode);
		            		pickerListDonor.add(donorPicker);
		            		pickerListRec.add(recPicker);
		            		
		            		sumpickNum1 += donorPicker.seglen;
		            		sumpickNum2 += recPicker.seglen;
		            	}
		            }
		            
		            if(crossPointsListDonor.size()>=numCrossPoints 
		            		/*|| sumpickNum1 >= pickNum1 || sumpickNum2 >= pickNum2*/
		            		|| Math.abs(sumpickNum1 - sumpickNum2)>MaxLenDiffSeg) {
		            	break;
		            }
	            }
	            
	            for(int ex = 0; ex < crossPointsListDonor.size(); ex++) {
	            	donorNode = crossPointsListDonor.get(ex); 
	            	recNode = crossPointsListRec.get(ex);
	            	donorPicker = pickerListDonor.get(ex);
	            	recPicker = pickerListRec.get(ex);
	            	
	            	if(checkPoints((LGPIndividual4Grammar)parents[t2], (LGPIndividual4Grammar) j1, donorNode, recNode, donorPicker, recPicker)) {
	            		//swap
			            DTNode handleDtNode = swapDTreeNInstruction((LGPIndividual4Grammar)parents[t2], (LGPIndividual4Grammar) j1, donorNode, recNode, donorPicker, recPicker);
			            
			            //adapt the instructions
		            	adaptInstruction(state, thread, (LGPIndividual4Grammar) j1, handleDtNode, ((LGPIndividual4Grammar)j1).getDerivationTree().root);
		            	
		            	j1.evaluated = false;
		            	
		            	if(!((LGPIndividual4Grammar)j1).checkDTreeNInstr()) {
			            	System.err.print("something wrong after crossover breeding\n");
			            	System.exit(1);
			            }
	            	}
	            	
	            }
	            
	            //if recNode is still null,   only micro mutation...

	            if(microMutation != null) j1 = (LGPIndividual) microMutation.produce(subpopulation, j1, state, thread);
	            if(eff_flag) {
	            	System.err.print("Sorry, GrammarCrossover does not support effective crossover (i.e., removing introns after swapping)\n");
	            	System.exit(1);
	            }
	            	            
	            
	            if (n-(q-start)>=2 && !tossSecondParent) {
	            	
	            	crossPointsListDonor.clear();
            		crossPointsListRec.clear();
            		pickerListDonor.clear();
            		pickerListRec.clear();
            		
            		numCrossPoints = state.random[thread].nextInt(maxnumCrossPoint)+1; //select numCrossPoints pairs of crossover points
            		
            		pickNum1 = state.random[thread].nextInt(Math.min(((LGPIndividual) parents[t1]).getTreesLength(), MaxSegLength)) + 1;
    	            pickNum2 = 1 + state.random[thread].nextInt(Math.min(j2.getTreesLength(), MaxSegLength));
    	            sumpickNum1 = 0; sumpickNum2 = 0;
            		
	            	
	            	for(int c = 0;c<numTries;c++) {
	            		donorNode=null;
		            	recNode=null;
		            	donorPicker=new DTNodePicker();
		            	recPicker = new DTNodePicker();
		            	
		            	for(int tr = 0; tr<numTries && !checkPoints((LGPIndividual4Grammar)parents[t1], (LGPIndividual4Grammar) j2, donorNode, recNode, donorPicker, recPicker); tr++) {
		            		
		            		donorNode=null;
			            	recNode=null;
			            	
			            	if(state.random[thread].nextDouble()<growrate) {
			            		donorNode = ((LGPIndividual4Grammar)parents[t1]).getDerivationTree().randomlyPickExtendNode(state, thread, null, thresold, crossPointsListDonor, false);
//			            		donorNode = ((LGPIndividual4Grammar)parents[t1]).getDerivationTree().randomlyPickExtendNode(state, thread, null, thresold, 
//			            				(LGPIndividual4Grammar)parents[t1], begin1, pickNum1, crossPointsListDonor);
			            	}
			            	else {
			            		donorNode = ((LGPIndividual4Grammar)parents[t1]).getDerivationTree().randomlyPickNode(state, thread, null, false);
//			            		donorNode = ((LGPIndividual4Grammar)parents[t1]).getDerivationTree().randomlyPickNode(state, thread, null,
//			            				(LGPIndividual4Grammar)parents[t1], begin1, pickNum1, crossPointsListDonor);
			            	}
			            	
			            	if(donorNode!=null)
			            		recNode = ((LGPIndividual4Grammar)j2).getDerivationTree().randomlyPickCompatibleNode(state, thread, donorNode, crossPointsListRec, false);
			            	
			            	
			            	if(recNode != null) {
			            		
			            		//pick the siblings or instructions from donorNode
			            		donorPicker.randomPick(state, thread, donorNode, false);
			            		
			            		//pick the siblings or instructions from recNode
			            		recPicker.randomPick(state, thread, recNode, false);
			            		
			            	}
			            }
		            	
		            	if(checkPoints((LGPIndividual4Grammar)parents[t1], (LGPIndividual4Grammar) j2, donorNode, recNode, donorPicker, recPicker)) {
		            		//check whether the new comming nodes overlap with existing nodes in the list
			            	boolean overlap = false;
			            	if(crossPointsListDonor.size()>0 && crossPointsListRec.size()>0) {
			            		for(DTNode node : crossPointsListDonor) {
			            			if(node.hasChildOf(donorNode)||donorNode.hasChildOf(node)) {
			            				overlap = true;
			            				break;
			            			}
			            		}
			            		
			            		for(DTNode node : crossPointsListRec) {
			            			if(node.hasChildOf(recNode)||recNode.hasChildOf(node)) {
			            				overlap = true;
			            				break;
			            			}
			            		}
			            	}
			            	
			            	//add them into the list
			            	if(!overlap) {
			            		crossPointsListDonor.add(donorNode);
			            		crossPointsListRec.add(recNode);
			            		pickerListDonor.add(donorPicker);
			            		pickerListRec.add(recPicker);
			            		
			            		sumpickNum1 += donorPicker.seglen;
			            		sumpickNum2 += recPicker.seglen;
			            	}
		            	}
		            	
		            	 if(crossPointsListDonor.size()>=numCrossPoints 
				            		/*|| sumpickNum1 >= pickNum1 || sumpickNum2 >= pickNum2*/
		            			 || Math.abs(sumpickNum1 - sumpickNum2)>MaxLenDiffSeg) {
				            	break;
				            }
	            	}
	            	
	            	for(int ex = 0; ex<crossPointsListDonor.size(); ex++) {
	            		donorNode = crossPointsListDonor.get(ex); 
		            	recNode = crossPointsListRec.get(ex);
		            	donorPicker = pickerListDonor.get(ex);
		            	recPicker = pickerListRec.get(ex);
		            	
		            	if(checkPoints((LGPIndividual4Grammar)parents[t1], (LGPIndividual4Grammar) j2, donorNode, recNode, donorPicker, recPicker)) {
		            		//swap
				            DTNode handleDtNode = swapDTreeNInstruction((LGPIndividual4Grammar)parents[t1], (LGPIndividual4Grammar) j2, donorNode, recNode, donorPicker, recPicker);
				            
				            //adapt the instructions
			            	adaptInstruction(state, thread, (LGPIndividual4Grammar)j2, handleDtNode, ((LGPIndividual4Grammar)j2).getDerivationTree().root);
			            	
			            	j2.evaluated = false;
			            	
			            	if(!((LGPIndividual4Grammar)j2).checkDTreeNInstr()) {
				            	System.err.print("something wrong after crossover breeding\n");
				            	System.exit(1);
				            }
		            	}
		            	
	            	}
		            //if recNode is still null,   only micro mutation...
		            
	            	if(microMutation != null) j2 =  (LGPIndividual) microMutation.produce(subpopulation, j2, state, thread);
	            	if(eff_flag) {
		            	System.err.print("Sorry, GrammarCrossover does not support effective crossover (i.e., removing introns after swapping)\n");
		            	System.exit(1);
		            }
	            	
	            }
	            
	            // add the individuals to the population
	            if(j1.getTreesLength() < j1.getMinNumTrees() || j1.getTreesLength() > j1.getMaxNumTrees()){
	            	state.output.fatal("illegal tree number in grammar cross j1");
	            }
	            inds[q] = j1;
	            q++;
	            parnt ++;
	            if (q<n+start && !tossSecondParent)
	            {
	            	if(j2.getTreesLength() < j2.getMinNumTrees() || j2.getTreesLength() > j2.getMaxNumTrees()){
	                	state.output.fatal("illegal tree number in grammar cross j2");
	                }
		            inds[q] = j2;
		            q++;
		            parnt ++;
	            }
	            
	            }
	            
	        return n;
	        }
}
