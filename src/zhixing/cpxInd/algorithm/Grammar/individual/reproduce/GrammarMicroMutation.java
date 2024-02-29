package zhixing.cpxInd.algorithm.Grammar.individual.reproduce;

import java.util.ArrayList;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import ec.gp.koza.GPKozaDefaults;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.Grammar.EvolutionState.EvolutionState4Grammar;
import zhixing.cpxInd.algorithm.Grammar.individual.GPTreeStructGrammar;
import zhixing.cpxInd.algorithm.Grammar.individual.GrammarLGPDefaults;
import zhixing.cpxInd.algorithm.Grammar.individual.InstructionBuilder;
import zhixing.cpxInd.algorithm.Grammar.individual.LGPIndividual4Grammar;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ConstantGPNode;
import zhixing.cpxInd.individual.primitive.FlowOperator;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMicroMutationPipeline;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public class GrammarMicroMutation extends LGPMicroMutationPipeline{

	public static final String P_GRAMMARMICROMUTATION = "grammarmicromutate";
	
	public static final String P_EFFFLOW = "effective_flow_operation";

	public boolean effflow = true;
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state,base);
		
		Parameter def = GrammarLGPDefaults.base().push(P_GRAMMARMICROMUTATION);
		Parameter p = base.push(P_BUILDER).push(""+0);
		Parameter d = def.push(P_BUILDER).push(""+0);
		
		builder = (GPNodeBuilder)
	            (state.parameters.getInstanceForParameter(
	                    p,d, GPNodeBuilder.class));
	        builder.setup(state,p);
	        
	    effflow = state.parameters.getBoolean(base.push(P_EFFFLOW), def.push(P_EFFFLOW), true);
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
        
        //double pickNum = Math.max(state.random[thread].nextDouble()*(i.getTreesLength()), 1);
        double pickNum = state.random[thread].nextInt(stepSize) + 1.0;
        for(int pick = 0;pick<pickNum;pick++){
        	int t = getLegalMutateIndex(j, state, thread);
        	GPNode p1=null;  // the node we pick
            GPNode p2=null;
            
            // validity result...
            boolean res = false;
            
            for(int x=0;x<numTries;x++)
                {
            	t = getLegalMutateIndex(j, state, thread);
            	
            	randomGetComponentType(state, thread);
            	if(j.getTree(t).child.children[0] instanceof FlowOperator && componenttype == writereg) {
            		componenttype = (componenttype + state.random[thread].nextInt(3)+1)%4;
            	}
            	
            	// pick random tree
                if (tree!=TREE_UNFIXED)
                    t = tree;
                
                res = false;
                
                // prepare the nodeselector
                nodeselect.reset();
                
                // pick a node
                
                p1=null;  // the node we pick
                p2=null;
                int cnt = 0; //the number of primitives that satisfies the given component type
                int cntdown = 0;
                GPTreeStructGrammar oriTree = (GPTreeStructGrammar) j.getTree(t);
                int flag = -1;//wheter it need to reselect the p1
                
                switch (componenttype) {
    			case functions:
    				flag = GPNode.NODESEARCH_NONTERMINALS;
    				break;
    			case cons:
    				flag = GPNode.NODESEARCH_CONSTANT;
    				break;
    			case writereg:
    				flag = -1;
    				cnt = 1;
    				p1 = oriTree.child;
    				break;
    			case readreg:
    				flag = GPNode.NODESEARCH_READREG;
    				break;
    			default:
    				break;
    			}
                if (flag >=0) cnt = oriTree.child.numNodes(flag);
            	
            	// pick a node in individual 1
            	//p1 = nodeselect.pickNode(state,subpopulation,thread,i,i.getTree(t));
            	if(flag>=0 && cnt >0) p1 = oriTree.child.nodeInPosition(state.random[thread].nextInt(cnt),flag);
            		
            	
            	int size = GPNodeBuilder.NOSIZEGIVEN;
                if (equalSize) size = p1.numNodes(GPNode.NODESEARCH_ALL);

                if(cnt<=0) {
                	p1 = nodeselect.pickNode(state,subpopulation,thread,i,i.getTree(t));
                }
                
                p2 = ((InstructionBuilder)builder).genOneNodeByGrammar(state, 
						oriTree.constraints(initializer).treetype, 
						thread, 
						oriTree, 
						p1.parent,
						oriTree.constraints(initializer).functionset,
						p1.argposition,
						p1.atDepth()); 

                // check for depth and swap-compatibility limits
                //res = verifyPoints(p2,p1);  // p2 can fit in p1's spot  -- the order is important!
                
                //easy check
                res = checkPoints(p1, p2, state, thread, i, i.getTreeStruct(t));
                //instance check
                
                // did we get something that had both nodes verified?
                if (res) break;
                }
            
            if (res)  // we've got a tree with a kicking cross position!
            {
	            int x = t;
	            GPTreeStruct tree = j.getTreeStruct(x);
                tree = (GPTreeStruct)(i.getTreeStruct(x).clone());
                tree.owner = j;
                tree.child = i.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
                tree.child.parent = tree;
                tree.child.argposition = 0;
                j.setTree(x, tree);
                j.evaluated = false; 
            } // it's changed
            else{
            	int x = t;
            	GPTreeStruct tree = j.getTreeStruct(x);
        		tree = (GPTreeStruct)(i.getTreeStruct(x).clone());
                tree.owner = j;
                tree.child = (GPNode)(i.getTree(x).child.clone());
                tree.child.parent = tree;
                tree.child.argposition = 0;    
                j.setTree(x, tree);
            }
            
           // j.updateStatus();
            ((LGPIndividual4Grammar)j).getDerivationTree().rematch((LGPIndividual4Grammar) j);
        }
        
        if(!((LGPIndividual4Grammar)j).checkDTreeNInstr()) {
        	System.err.print("something wrong after micro mutation breeding\n");
        	System.exit(1);
        }
        
        return j;
	}
	
	@Override
	protected boolean checkPoints(GPNode p1, GPNode p2, EvolutionState state, int thread, LGPIndividual ind, GPTreeStruct treeStr) {
		// p1 and p2 must be different. if they are at destination, they should also be effective
		boolean res = false;
		
		//if it is not a flow control instruction
		if(!(treeStr.child.children[0] instanceof FlowOperator)) {
			if(p1.expectedChildren() == p2.expectedChildren()) {
	        	if(!p1.toStringForHumans().equals(p2.toStringForHumans()) ){
	        		if(effflag && treeStr.effRegisters.size()>0) {
	        			if((p1.atDepth()==0 && !treeStr.effRegisters.contains(((WriteRegisterGPNode)p2).getIndex()))) { //guarantee the effectiveness
//	        				ArrayList<Integer> list = new ArrayList<>(treeStr.effRegisters);
//	        				((WriteRegisterGPNode)p2).setIndex(list.get(state.random[thread].nextInt(list.size())));
//	        				
//	        				if(p1.toStringForHumans().equals(p2.toStringForHumans()) && treeStr.effRegisters.size() > 1 )
//	        					res = false;
//	        				else {
//								res = true;
//							}
	        				//====in the grammmar-guided LGP, because p2 is generated by grammar rules, if randomly change p2 here, it likely generates
	        				//====p2 inconsistent with grammar rules, so simply set p2 back to p1 here, to guarantee effectiveness and grammar coherence
	        				((WriteRegisterGPNode)p2).setIndex(((WriteRegisterGPNode)p1).getIndex());
	        				if(treeStr.effRegisters.size() > 1) {
	        					res = false;
	        				}
	        				else {
	        					res = true;
	        				}
	            		}
	            		else {
	            			res = true;
	            		}
	        		}
	        		else {
	        			res = true;
	        		}
	        		
	        	}
	        }
		}
		else{
			//further check for flow operator
			if(p1.expectedChildren() == p2.expectedChildren()
					&& !p1.toStringForHumans().equals(p2.toStringForHumans())) {
				res = true;
				
	    		if(p2 instanceof FlowOperator && ! ind.canAddFlowOperator()) {
	    			res = false;
	    		}
	    		
	    		if(treeStr.child.children[0] instanceof FlowOperator) {
	    			if(effflow && ((EvolutionState4Grammar)state).getBannedList4FlowInstr().contains(treeStr.toString())
	    					&& state.random[thread].nextDouble()>0.1 ) {
	    				res = false;
	    			}
	    		}
			}
			
		}
		
		//further check for constant value
		if(p1 instanceof ConstantGPNode && p2 instanceof ConstantGPNode) {
			if(Math.abs(((ConstantGPNode)p1).getValue() - ((ConstantGPNode)p2).getValue())>cons_step) {
				res = false;
			}
			else
				res = true;
		}
		
		if(res) {
			for(int c = 0;c<p1.children.length;c++) {
    			p2.children[c] = (GPNode)p1.children[c].clone();
    		}
		}
		
		return res;
	}
	
}
