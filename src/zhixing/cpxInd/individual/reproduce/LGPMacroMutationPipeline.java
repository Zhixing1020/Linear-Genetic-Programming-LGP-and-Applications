package zhixing.cpxInd.individual.reproduce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.spiderland.Psh.booleanStack;
import org.spiderland.Psh.intStack;

//import com.lowagie.text.pdf.hyphenation.TernaryTree.Iterator;

import ec.BreedingPipeline;
import ec.BreedingSource;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import ec.gp.koza.MutationPipeline;
import ec.util.Parameter;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;

public class LGPMacroMutationPipeline extends MutationPipeline {
	public static final String P_MUT_TYPE = "type";
	public static final String P_STEP = "step"; //the number of free mutation step size 
	public static final String P_INSERT = "prob_insert";
	public static final String P_DELETE = "prob_delete";
	
	public static final String MACROMUT = "macromut";
	
	public static final String P_MICROMUTBASE = "micro_base";
	
	public static final int FREEMACROMUT = 0;
	public static final int EFFMACROMUT = 1;
	public static final int EFFMACROMUT2 = 2;
	public static final int EFFMACROMUT3 = 3;
	
	//public Parameter defaultBase() { return LGPDefaults.base().push(MACROMUT); }
	
	public String mutateType;
	
	public int mutateFlag = -1;
	
	public int stepSize;
	
	public double probInsert;
	
	public double probDelete;
	
	protected LGPMicroMutationPipeline microMutation;
	
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = LGPDefaults.base().push(MACROMUT);
		
		mutateType = state.parameters.getString(base.push(P_MUT_TYPE),
	            def.push(P_MUT_TYPE));
		if(mutateType == null) {
			state.output.fatal("LGPMacroMutation Pipeline has an invalid mutation type.",base.push(P_MUT_TYPE),
	            def.push(P_MUT_TYPE));
		}
		if(mutateType.equals("freemut")) {
			mutateFlag = FREEMACROMUT;
		}
		else if(mutateType.equals("effmut")) {
			mutateFlag = EFFMACROMUT;
		}
		else if(mutateType.equals("effmut2")){
			mutateFlag = EFFMACROMUT2;
		}
		else if(mutateType.equals("effmut3")) {
			mutateFlag = EFFMACROMUT3;
		}
	    
		stepSize = state.parameters.getInt(base.push(P_STEP), def.push(P_STEP), 1);
		 if (stepSize == 0)
	            state.output.fatal("LGPMacroMutation Pipeline has an invalid number of step size (it must be >= 1).",base.push(P_STEP),def.push(P_STEP));
		 
		 probInsert = state.parameters.getDouble(base.push(P_INSERT), def.push(P_INSERT), 0.0);
		 if (probInsert < 0)
	            state.output.fatal("LGPMacroMutation Pipeline has an invalid number of prob_insert (it must be >= 0).",base.push(P_INSERT),def.push(P_INSERT));
		 
		 probDelete = state.parameters.getDouble(base.push(P_DELETE), def.push(P_DELETE), 0.0);
		 if (probDelete < 0)
	            state.output.fatal("LGPMacroMutation Pipeline has an invalid number of prob_delete (it must be >= 0).",base.push(P_DELETE),def.push(P_DELETE));
		 
		 Parameter microbase = new Parameter(state.parameters.getString(base.push(P_MICROMUTBASE), def.push(P_MICROMUTBASE))) ;
		 microMutation = null;
		 if(!microbase.toString().equals("null")){
        	//microMutation = new LGPMicroMutationPipeline();
        	microMutation = (LGPMicroMutationPipeline)(state.parameters.getInstanceForParameter(
                    microbase, def.push(P_MICROMUTBASE), MutationPipeline.class));
   		 	microMutation.setup(state, microbase);
        }
	}
	
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread) {
		// grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did


        GPInitializer initializer = ((GPInitializer)state.initializer);

        // now let's mutate 'em
        for(int q=start; q < n+start; q++)
            {
            LGPIndividual i = (LGPIndividual)inds[q];

            inds[q] = this.produce(subpopulation, i, state, thread);
            }
        return n;
	}
	
	public LGPIndividual produce(
			final int subpopulation,
	        final LGPIndividual ind,
	        final EvolutionState state,
	        final int thread) {
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		LGPIndividual i = ind;
		
		if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.getTreesLength()))
            // uh oh
            state.output.fatal("LGP Mutation Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            
        LGPIndividual j;

        if (sources[0] instanceof BreedingPipeline)
            // it's already a copy, so just smash the tree in
            {
            j=i;
            }
        else // need to clone the individual
            {
            j = ((LGPIndividual)i).lightClone();
            
            // Fill in various tree information that didn't get filled in there
            //j.renewTrees();
            
            }
        for(int v = 0;v<i.getTreesLength();v++) {
        	int x = v;
    		GPTree tree = j.getTree(x);
    		tree = (GPTree)(i.getTree(x).lightClone());
            tree.owner = j;
            tree.child = (GPNode)(i.getTree(x).child.clone());
            tree.child.parent = tree;
            tree.child.argposition = 0;    
            j.setTree(x, tree);
        }
        
        //double pickNum = Math.max(state.random[thread].nextDouble()*(i.getTreesLength()), 1);
        double pickNum = state.random[thread].nextInt(stepSize) + 1.0;
        for(int pick = 0;pick<pickNum;pick++){
        	int t = state.random[thread].nextInt(j.getTreesLength());
        	
        	//if insert a new instruction
        	if(j.getTreesLength() < j.getMaxNumTrees() && ( state.random[thread].nextDouble() < probInsert || j.getTreesLength() == j.getMinNumTrees())) {
        		
        		t = getLegalInsertIndex(j, state, thread);
        		
        		// validity result...
	            boolean res = false;
	            
	            // prepare the nodeselector
	            nodeselect.reset();
	            
	            // pick a node
	            
	            //GPNode p1=i.getTree(t).child;  // the node we pick
	            GPNode p1=j.getTree(t).child; 
	            GPNode p2=null;
	            
	            for(int x=0;x<numTries;x++)
	                {    	                
	                // generate a tree swap-compatible with p1's position
	                
	                
	                int size = GPNodeBuilder.NOSIZEGIVEN;
	                if (equalSize) size = p1.numNodes(GPNode.NODESEARCH_ALL);
	                
	                if(builder instanceof LGPMutationGrowBuilder) {
	                	p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
	    	                    p1.parentType(initializer),
	    	                    thread,
	    	                    p1.parent,
	    	                    //i.getTree(t).constraints(initializer).functionset,
	    	                    j.getTree(t).constraints(initializer).functionset,
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
	     	                    j.getTree(t).constraints(initializer).functionset,
	     	                    p1.argposition,
	     	                    size);
					}
	               
	                
	                // check for depth and swap-compatibility limits
	                //res = verifyPoints(p2,p1);  // p2 can fit in p1's spot  -- the order is important!
	                res = checkPoints(p1, p2, j.getTreeStruct(t));
	                
	                // did we get something that had both nodes verified?
	                if (res) break;
	                }
	            
	            //if (res)  // we've got a tree with a kicking cross position!
                {
		            int x = t;
		            //GPTree tree = j.getTree(x);
                    //tree = (GPTree)(i.getTree(x).lightClone());
		            GPTreeStruct tree = (GPTreeStruct) j.getTreeStruct(x).clone();
                    tree.owner = j;
                    //tree.child = i.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
                    tree.child = j.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
                    tree.child.parent = tree;
                    tree.child.argposition = 0;
                    j.addTree(x+1, tree);
                    j.evaluated = false; 
                } // it's changed
        	}
        	else if (j.getTreesLength() > j.getMinNumTrees() &&(state.random[thread].nextDouble() < probInsert + probDelete || j.getTreesLength() == j.getMaxNumTrees())) {
        		
        		t = getLegalDeleteIndex(j, state, thread);
        		j.removeTree(t);
        		j.evaluated = false; 
        	}
        	
        	//j.updateStatus();
        	else if(j.getTreesLength() == j.getMinNumTrees() && j.getTreesLength() == j.getMaxNumTrees()) {
        		t = getLegalInsertIndex(j, state, thread);
        		
        		// validity result...
	            boolean res = false;
	            
	            // prepare the nodeselector
	            nodeselect.reset();
	            
	            // pick a node
	            
	            //GPNode p1=i.getTree(t).child;  // the node we pick
	            GPNode p1=j.getTree(t).child; 
	            GPNode p2=null;
	            
	            for(int x=0;x<numTries;x++)
	                {    	                
	                // generate a tree swap-compatible with p1's position
	                
	                
	                int size = GPNodeBuilder.NOSIZEGIVEN;
	                if (equalSize) size = p1.numNodes(GPNode.NODESEARCH_ALL);
	                
	                if(builder instanceof LGPMutationGrowBuilder) {
	                	p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
	    	                    p1.parentType(initializer),
	    	                    thread,
	    	                    p1.parent,
	    	                    //i.getTree(t).constraints(initializer).functionset,
	    	                    j.getTree(t).constraints(initializer).functionset,
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
	     	                    j.getTree(t).constraints(initializer).functionset,
	     	                    p1.argposition,
	     	                    size);
					}
	               
	                
	                // check for depth and swap-compatibility limits
	                //res = verifyPoints(p2,p1);  // p2 can fit in p1's spot  -- the order is important!
	                res = checkPoints(p1, p2, j.getTreeStruct(t));
	                
	                // did we get something that had both nodes verified?
	                if (res) break;
	                }
	            
	            //if (res)  // we've got a tree with a kicking cross position!
                {
		            int x = t;
		            //GPTree tree = j.getTree(x);
                    //tree = (GPTree)(i.getTree(x).lightClone());
		            GPTreeStruct tree = (GPTreeStruct) j.getTreeStruct(x).clone();
                    tree.owner = j;
                    //tree.child = i.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
                    tree.child = j.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
                    tree.child.parent = tree;
                    tree.child.argposition = 0;
                    j.addTree(x+1, tree);
                    j.removeTree(x);
                    j.evaluated = false; 
                } // it's changed
        	}
        }
        
        
        //some following operations
        switch (mutateFlag) {
		case EFFMACROMUT2:
			break;
		case EFFMACROMUT:
		case FREEMACROMUT:
			if(microMutation != null) j = (LGPIndividual) microMutation.produce(subpopulation, j, state, thread);
			break;
		case EFFMACROMUT3:
			//delete all non-effective instructions
			j.removeIneffectiveInstr();

			break;
		default:
			break;
		}
        

        return j;
	}
	
	protected int getLegalInsertIndex(LGPIndividual ind, EvolutionState state, int thread) {
		int res = 0;
		switch (mutateFlag) {
		case FREEMACROMUT: //insert an instruction into random position
			res = state.random[thread].nextInt(ind.getTreesLength());
			break;
		case EFFMACROMUT: //insert an instruction into an effective position
		case EFFMACROMUT2:
		case EFFMACROMUT3:
			res = state.random[thread].nextInt(ind.getTreesLength());
			for(int x = 0;x<numTries;x++) {
        		if(ind.getTreeStruct(res).effRegisters.size() > 0) break;
        		res = state.random[thread].nextInt(ind.getTreesLength());
        	}
			break;
		default:
			state.output.fatal("illegal mutateFlag in LGP macro mutation");
			break;
		}
		return res;
	}
	
	protected int getLegalDeleteIndex(LGPIndividual ind, EvolutionState state, int thread) {
		int res = 0;
		switch (mutateFlag) {
		case FREEMACROMUT: //delete an instruction on random position
		case EFFMACROMUT:
			res = state.random[thread].nextInt(ind.getTreesLength());
			break;
		case EFFMACROMUT2://delete a random effective instruction
		case EFFMACROMUT3:
			res = state.random[thread].nextInt(ind.getTreesLength());
			for(int x = 0;x<numTries;x++) {
        		if(ind.getTreeStruct(res).status) break;
        		res = state.random[thread].nextInt(ind.getTreesLength());
        	}
			break;
		default:
			state.output.fatal("illegal mutateFlag in LGP macro mutation");
			break;
		}
		return res;
	}
	
	protected boolean checkPoints(GPNode p1, GPNode p2, GPTreeStruct treeStr) {
		// p1 and p2 must be different. if they are at destination, they should also be effective
		boolean res = false;
		
		if(mutateFlag == FREEMACROMUT) {
			return true;
		}
		
		if((p1.atDepth()==0 && treeStr.effRegisters.contains(((WriteRegisterGPNode)p2).getIndex()))) { //guarantee the effectiveness
			res = true;
		}
		
		return res;
	}
	
}
