package zhixing.cpxInd.algorithm.Grammar.individual.reproduce;

import java.util.ArrayList;
import java.util.HashSet;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.DerivationRule;
import zhixing.cpxInd.algorithm.Grammar.individual.DTNode;
import zhixing.cpxInd.algorithm.Grammar.individual.GPTreeStructGrammar;
import zhixing.cpxInd.algorithm.Grammar.individual.InstructionBuilder;
import zhixing.cpxInd.algorithm.Grammar.individual.LGPIndividual4Grammar;
import zhixing.cpxInd.algorithm.Grammar.individual.primitives.NumericalValue;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.Branching;
import zhixing.cpxInd.individual.primitive.FlowOperator;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;

public class GrammarSemanticMutation extends GrammarMicroMutation {
	public LGPIndividual produce(
			final int subpopulation,
	        final LGPIndividual ind,
	        final EvolutionState state,
	        final int thread) {
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		LGPIndividual i = super.produce(subpopulation, ind, state, thread);
		
		if (tree!=TREE_UNFIXED && (tree<0 || tree >= i.getTreesLength()))
            // uh oh
            state.output.fatal("LGP Mutation Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
            
        LGPIndividual j;

        if (sources[0] instanceof BreedingPipeline)
            // it's already a copy, so just smash the tree in
            {
            j = (LGPIndividual4Grammar) i;
            }
        else // need to clone the individual
            {
            
            j = ((LGPIndividual4Grammar)i).lightClone();
            // Fill in various tree information that didn't get filled in there
            //j.renewTrees();
            
            }

        boolean res = false;
        if(j.getTreeStructs() != null) {
			
			ArrayList<ArrayList<Integer>> groups = new ArrayList();
			ArrayList<Integer> grp = null;
			int bodyend = 0;
			
			//prepare groups
			for(int ii = 0; ii<j.getTreesLength();ii++) {
				GPTreeStructGrammar ins = (GPTreeStructGrammar) j.getTreeStructs().get(ii);
				
				if(ii>bodyend) {
					grp = null;
					bodyend = ii;
				}
				
				if(ins.status && ins.child.children[0] instanceof FlowOperator) {
					//new a group
					if(grp == null) {
						grp = new ArrayList();
						
					}
					
					if(ins.child.children[0] instanceof Branching) {
						grp.add(ii);
					}
						
					bodyend = ii + ((FlowOperator)ins.child.children[0]).getBodyLength();
					
				}
				
				//wrap a nested group
				if(grp != null) {
					if(ii == j.getTreesLength() - 1 || (ii == bodyend && ! (ins.child.children[0] instanceof FlowOperator))) {
						groups.add(grp);
						grp = null;
					}
				}
				
			}
			
			for(ArrayList<Integer> gp : groups) {
				
				ArrayList<String> attributes = new ArrayList();
				ArrayList<FlowBodyRange> ranges = new ArrayList();
				ArrayList<ArrayList<Integer>> indexes = new ArrayList();
				
				for(Integer in : gp) {
					GPTreeStructGrammar ins = (GPTreeStructGrammar) j.getTreeStruct(in);
					String attriName = ins.child.children[0].children[0].toStringForHumans();
					
					//add an attribute into consideration
					if( ! attributes.contains(attriName) ) {
						attributes.add(attriName);
						ranges.add(new FlowBodyRange((Branching) ins.child.children[0], ((NumericalValue)ins.child.children[0].children[1]).getValue()));
						ArrayList<Integer> tmp = new ArrayList<>();
						tmp.add(in);
						indexes.add(tmp);
					}
					//if the nest IF group has contain this attribute, 
					else {
						//check whether 1) its range has been fully covered and 2) its range is conflict with existing ones
						int ii = attributes.indexOf(attriName);
						
						ranges.get(ii).interset(new FlowBodyRange((Branching) ins.child.children[0], ((NumericalValue)ins.child.children[0].children[1]).getValue()));
						
						indexes.get(ii).add(in);
					}
				}
				
				//check whether the range of each attribute is empty
				for(int ji = 0; ji<ranges.size(); ji++) {
					if(ranges.get(ji).isEmpty() || indexes.get(ji).size()>1) {
						
						GPNode p1=null;  // the node we pick
		                GPNode p2=null;
		                int tomutate_ind = indexes.get(ji).get(0);
		                
						for(int x=0;x<numTries;x++) {
							//collect the candidate instructions
    						int k = state.random[thread].nextInt(indexes.get(ji).size());
    						tomutate_ind = indexes.get(ji).get(k);
    						
    						//use micro mutation to updaate the instruction
    						randomGetComponentType(state, thread);
    						
    						// prepare the nodeselector
    		                nodeselect.reset();
    		                
    		                // pick a node
    		                
    		                p1=null;  // the node we pick
    		                p2=null;
    		                int cnt = 0; //the number of primitives that satisfies the given component type
    		                int cntdown = 0;
    		                GPTreeStructGrammar oriTree = (GPTreeStructGrammar) j.getTree(tomutate_ind);
    		                int flag = -1;//wheter it need to reselect the p1
    		                
    		                switch (componenttype) {
    		    			case functions:
    		    				flag = GPNode.NODESEARCH_NONTERMINALS;
    		    				break;
    		    			case writereg:
    		    			case cons:
    		    			case readreg:
    		    				flag = GPNode.NODESEARCH_CONSTANT;
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
    		                	p1 = nodeselect.pickNode(state,subpopulation,thread,i,i.getTree(tomutate_ind));
    		                }
    		                
    		                p2 = ((InstructionBuilder)builder).genOneNodeByGrammar(state, 
    								oriTree.constraints(initializer).treetype, 
    								thread, 
    								oriTree, 
    								p1.parent,
    								oriTree.constraints(initializer).functionset,
    								p1.argposition,
    								p1.atDepth()); 
    		                
    		                //easy check
    		                res = checkPoints(p1, p2, state, thread, i, i.getTreeStruct(tomutate_ind));
    		                
    		                // did we get something that had both nodes verified?
    		                if (res) break;
						}
						
						if (res)  // we've got a tree with a kicking cross position!
			            {
				            int x = tomutate_ind;
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
			            	int x = tomutate_ind;
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
			            
			            if(!((LGPIndividual4Grammar)j).checkDTreeNInstr()) {
			            	System.err.print("something wrong after micro mutation breeding\n");
			            	System.exit(1);
			            }
					}
				}
			}
    		
        }
        
        return j;
	}
	
//	protected boolean checkNestedIF(EvolutionState state, int thread, int subpopulation, LGPIndividual4Grammar ind, ArrayList<GPTreeStruct> inslist) {
//		//check the instruction of inslist, there should be at least one instruction is effective
//
//		GPInitializer initializer = ((GPInitializer)state.initializer);
//		boolean res = false;
//		
//		
//			
//			
//			for(int i = inslist.size()-1; i>=0; i--)
//			{
//				GPTreeStructGrammar ins = (GPTreeStructGrammar) inslist.get(i);
//
//				if(!ins.status) {
//					int originIndex = ((WriteRegisterGPNode)ins.child).getIndex();
//					DTNode grammarDtNode = ins.grammarNode;
//					
//					((WriteRegisterGPNode)ins.child).enumerateNode(state, thread);
//					ind.updateStatus();
//					String primitiveName = ins.child.toString().substring(0, ins.child.toString().length()-1);
//			        for(int ii = 0; ii<numTries; ii++) {
//			        	if(grammarDtNode.param_value.get(0).contains(primitiveName) && ins.status) {break;}
//			        	
//			        	//des.resetNode(state,thread);
//			        	((WriteRegisterGPNode)ins.child).enumerateNode(state, thread);
//			        	ind.updateStatus();
//			        	primitiveName = ins.child.toString().substring(0, ins.child.toString().length()-1);
//			        }
//			        if(!grammarDtNode.param_value.get(0).contains(primitiveName) || !ins.status) {
//			        	((WriteRegisterGPNode)ins.child).setIndex(originIndex);
//			        	ind.updateStatus();
//			        }
//				}
//				res = res || ins.status;
//			}
//			if(!res) return false;
//		}
//
//		if(ind.getTreesLength()>ind.getMaxNumTrees() || ind.getTreesLength()<ind.getMinNumTrees()) return false;
//		
//		return true;
//	}
}
