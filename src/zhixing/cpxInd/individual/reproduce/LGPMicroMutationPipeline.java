package zhixing.cpxInd.individual.reproduce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.spiderland.Psh.intStack;

//import com.sun.corba.se.spi.orbutil.fsm.State;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPNodeGatherer;
import ec.gp.GPTree;
import ec.gp.koza.MutationPipeline;
import ec.util.Parameter;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ConstantGPNode;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.cpxInd.individual.primitive.FlowOperator;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.Function_EntityNode;

public class LGPMicroMutationPipeline extends MutationPipeline {
public static final String P_STEP = "step"; //the number of free mutation step size 
	
	public static final String MICROMUT = "micromut";
	public static final String P_EFFFLAG = "effective";
	
	public static final String P_PROBFUNC = "probfunc";
	public static final String P_PROBCONS = "probcons";
	public static final String P_PROBWRIREG = "probwritereg";
	public static final String P_PROBREADREG = "probreadreg";
	public static final String P_CONSTSTEP = "conststep";
	
	public static final int functions = 0;
	public static final int cons = 1;
	public static final int writereg = 2;
	public static final int readreg = 3;
	
	//public Parameter defaultBase() { return LGPDefaults.base().push(FREEMUT); }
	
	public int stepSize;
	
	public boolean effflag;
	
	public double p_function;
	
	public double p_constant;
	
	public double p_writereg;
	
	public double p_readreg;
	
	public int cons_step;
	
	protected int componenttype;
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = LGPDefaults.base().push(MICROMUT);
	    
		stepSize = state.parameters.getInt(base.push(P_STEP), def.push(P_STEP), 1);
		 if (stepSize == 0)
	            state.output.fatal("LGPFreeMutation Pipeline has an invalid number of step size (it must be >= 1).",base.push(P_STEP),def.push(P_STEP));
		 
		 effflag = state.parameters.getBoolean(base.push(P_EFFFLAG), def.push(P_EFFFLAG),false);
		 
		 p_function = state.parameters.getDouble(base.push(P_PROBFUNC), def.push(P_PROBFUNC), 0.);
		 if (p_function == -1)
	            state.output.fatal("LGPFreeMutation Pipeline has an invalid number of probfunc (it must be >= 0).",base.push(P_PROBFUNC),def.push(P_PROBFUNC));
		 
		 p_constant = state.parameters.getDouble(base.push(P_PROBCONS), def.push(P_PROBCONS), 0.);
		 if (p_constant == -1)
	            state.output.fatal("LGPFreeMutation Pipeline has an invalid number of probcons (it must be >= 0).",base.push(P_PROBCONS),def.push(P_PROBCONS));
		 
		 p_writereg = state.parameters.getDouble(base.push(P_PROBWRIREG), def.push(P_PROBWRIREG), 0.);
		 if (p_writereg == -1)
	            state.output.fatal("LGPFreeMutation Pipeline has an invalid number of probwritereg (it must be >= 0).",base.push(P_PROBWRIREG),def.push(P_PROBWRIREG));
		 
		 p_readreg = state.parameters.getDouble(base.push(P_PROBREADREG), def.push(P_PROBREADREG), 0.);
		 if (p_readreg == -1)
	            state.output.fatal("LGPFreeMutation Pipeline has an invalid number of probreadreg (it must be >= 0).",base.push(P_PROBREADREG),def.push(P_PROBREADREG));
		 
		 cons_step = state.parameters.getInt(base.push(P_CONSTSTEP),def.push(P_CONSTSTEP), 1);
		 if (cons_step == 0)
	            state.output.fatal("LGPFreeMutation Pipeline has an invalid number of cons step (it must be > 0).",base.push(P_CONSTSTEP),def.push(P_CONSTSTEP));
	}
	
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread) 
	        {
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
        
        //get the function set
        GPFunctionSet set = i.getTree(0).constraints(initializer).functionset;  //all trees have the same function set
       
        
      //get the mutation component
        double rnd = state.random[thread].nextDouble();
        if(rnd>p_function+p_constant+p_writereg+p_readreg) { //randomly select the component type
        	componenttype = state.random[thread].nextInt(4);
        }
        else if(rnd > p_constant + p_writereg + p_readreg) { //function
        	componenttype = functions;
        }
        else if (rnd > p_writereg + p_readreg) { //constnat
        	componenttype = cons;
        }
        else if ( rnd > p_readreg) { //write register
        	componenttype = writereg;
        }
        else {
        	componenttype = readreg; //read register
        }

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
        	
            // validity result...
            boolean res = false;
            
            // pick a node
            
            GPNode p1=null;  // the node we pick
            GPNode p2=null;
            int cnt = 0; //the number of primitives that satisfies the given component type
            int cntdown = 0;
            int flag = -1;//wheter it need to reselect the p1
            
            for(int x=0;x<numTries;x++)
                {

            	t = getLegalMutateIndex(j, state, thread);
            	
            	randomGetComponentType(state, thread);
            	
            	// pick random tree
                if (tree!=TREE_UNFIXED)
                    t = tree;
                
                // prepare the nodeselector
                nodeselect.reset();
                
                // pick a node
                
                p1=null;  // the node we pick
                p2=null;
                cnt = 0; //the number of primitives that satisfies the given component type
                cntdown = 0;
                GPTree oriTree = i.getTree(t);
                flag = -1;//wheter it need to reselect the p1
                
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
            	
                if(cnt > 0) {
                	switch (componenttype) {
                	case functions:
                		
                		p2 = (GPNode)((GPNode) set.nonterminals_v.get(state.random[thread].nextInt(set.nonterminals_v.size()))).lightClone();
						p2.resetNode(state, thread);

						break;
					case cons:
						if(state.random[thread].nextDouble()<((LGPMutationGrowBuilder)builder).probCons) {
							
							p2 = (GPNode)((GPNode) set.constants_v.get(state.random[thread].nextInt(set.constants_v.size()))).lightClone();
							p2.resetNode(state, thread);
							
						}
						else {
							p2 = (GPNode)((GPNode) set.nonconstants_v.get(state.random[thread].nextInt(set.nonconstants_v.size()))).lightClone();
							p2.resetNode(state, thread);
						}
						break;
					case writereg:
						p2 = (GPNode)((GPNode) set.registers_v.get(state.random[thread].nextInt(set.registers_v.size()))).lightClone();
						p2.resetNode(state, thread);
						break;
					case readreg:
//						p2 = (GPNode) set.nonconstants_v.get(state.random[thread].nextInt(set.nonconstants_v.size()));
//						p2.resetNode(state, thread);
						p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
	    	                    p1.parentType(initializer),
	    	                    thread,
	    	                    p1.parent,
	    	                    i.getTree(t).constraints(initializer).functionset,
	    	                    p1.argposition,
	    	                    size,
	    	                    p1.atDepth());
						break;
					default:
						break;
					}
                }
            	 
            	 
                else {
                	//no suitable instruction is found, so there is no primitive with the given component type
            		 p1 = nodeselect.pickNode(state,subpopulation,thread,i,i.getTree(t));
            		 p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
	    	                    p1.parentType(initializer),
	    	                    thread,
	    	                    p1.parent,
	    	                    i.getTree(t).constraints(initializer).functionset,
	    	                    p1.argposition,
	    	                    size,
	    	                    p1.atDepth());
            	 }

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
        }
        
        return j;
	}
	
	protected boolean checkPoints(GPNode p1, GPNode p2, EvolutionState state, int thread, LGPIndividual ind, GPTreeStruct treeStr) {
		// p1 and p2 must be different. if they are at destination, they should also be effective
		boolean res = false;
		
		if(p1.expectedChildren() == p2.expectedChildren()) {
        	if(!p1.toStringForHumans().equals(p2.toStringForHumans()) ){
        		if(effflag && treeStr.effRegisters.size()>0) {
        			if((p1.atDepth()==0 && !treeStr.effRegisters.contains(((WriteRegisterGPNode)p2).getIndex()))) { //guarantee the effectiveness
        				ArrayList<Integer> list = new ArrayList<>(treeStr.effRegisters);
        				((WriteRegisterGPNode)p2).setIndex(list.get(state.random[thread].nextInt(list.size())));
        				if(p1.toStringForHumans().equals(p2.toStringForHumans()) && treeStr.effRegisters.size() > 1 )
        					res = false;
        				else {
//        					GPInitializer initializer = ((GPInitializer)state.initializer);
//        					GPFunctionSet set = treeStr.constraints(initializer).functionset;
//        					p2 = (GPNode) set.registers_v.get(state.random[thread].nextInt(set.registers_v.size()));
//    						p2.resetNode(state, thread);
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
        		
        		//further check for constant value
        		if(p1 instanceof ConstantGPNode && p2 instanceof ConstantGPNode) {
        			if(Math.abs(((ConstantGPNode)p1).getValue() - ((ConstantGPNode)p2).getValue())>cons_step) {
        				if(((ConstantGPNode)p1).getValue() - ((ConstantGPNode)p2).getValue()>0)
        					((ConstantGPNode)p2).setValue(((ConstantGPNode)p1).getValue() - 1 - state.random[thread].nextInt(cons_step - 1));
        				else {
        					((ConstantGPNode)p2).setValue(((ConstantGPNode)p1).getValue() + 1 + state.random[thread].nextInt(cons_step - 1));
						}
        			}
        			res = true;
        		}
        		
        		//further check for flow operator
        		if(p2 instanceof FlowOperator && ! ind.canAddFlowOperator()) {
        			res = false;
        		}

        		for(int c = 0;c<p1.children.length;c++) {
        			p2.children[c] = (GPNode)p1.children[c].clone();
        		}
        	}
        }
		
		return res;
	}
	
	protected int getLegalMutateIndex(LGPIndividual ind, EvolutionState state, int thread) {
		int res = state.random[thread].nextInt(ind.getTreesLength());
		
		if(effflag) {//guarantee the effectiveness of the selected instruction
			if(componenttype != cons) {
				for(int x = 0;x<numTries;x++) {
	        		if(ind.getTreeStruct(res).status) break;
	        		res = state.random[thread].nextInt(ind.getTreesLength());
	        	}
				//it is different from the book description here. the modification here helps LGP to transform noneffective instructions
				//into effective ones. the empirical results show it will be better.
			}
			else {
    			for(int x = 0;x<numTries;x++) {
            		if(ind.getTreeStruct(res).status && ind.getTree(res).child.numNodes(GPNode.NODESEARCH_CONSTANT)>0) break;
            		res = state.random[thread].nextInt(ind.getTreesLength());
            	}
    		}
    	}
		else {
			if (componenttype == cons){
				for(int x = 0;x<numTries;x++) {
	        		if(ind.getTree(res).child.numNodes(GPNode.NODESEARCH_CONSTANT)>0) break;
	        		res = state.random[thread].nextInt(ind.getTreesLength());
	        	}
			}
		}
		
		
		return res;
	}
	
	protected void randomGetComponentType(EvolutionState state, int thread) {
		//get the mutation component
        double rnd = state.random[thread].nextDouble();
        if(rnd>p_function+p_constant+p_writereg+p_readreg) { //randomly select the component type
        	componenttype = state.random[thread].nextInt(4);
        }
        else if(rnd > p_constant + p_writereg + p_readreg) { //function
        	componenttype = functions;
        }
        else if (rnd > p_writereg + p_readreg) { //constnat
        	componenttype = cons;
        }
        else if ( rnd > p_readreg) { //write register
        	componenttype = writereg;
        }
        else {
        	componenttype = readreg; //read register
        }
	}
}
