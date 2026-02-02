package zhixing.symbreg_multitarget.algorithm.entity.individual.produce;

import java.util.ArrayList;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Fitness;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ConstantGPNode;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.cpxInd.individual.primitive.FlowOperator;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMicroMutationPipeline;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.Args4Function_SRMT;
import zhixing.symbreg_multitarget.algorithm.entity.individual.primitive.Function_EntityNode;
import zhixing.symbreg_multitarget.optimization.GPSymbolicRegressionMultiTarget;

public class TunableFunctionLocalSearch  extends LGPMicroMutationPipeline{

	
	@Override
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
       
        componenttype = functions;

        LGPIndividual j, bestj;
        
        if (sources[0] instanceof BreedingPipeline)
            // it's already a copy, so just smash the tree in
            {
            j=i;
            bestj = i;
            }
        else // need to clone the individual
            {
//            j = ((LGPIndividual)i).lightClone();
            bestj = ((LGPIndividual)i).lightClone();
            
            // Fill in various tree information that didn't get filled in there
            //j.renewTrees();
            }
        
        //double pickNum = Math.max(state.random[thread].nextDouble()*(i.getTreesLength()), 1);
        double pickNum = state.random[thread].nextInt(stepSize) + 1.0;
        for(int pick = 0;pick<pickNum;pick++){
        	int t = getLegalMutateIndex(bestj, state, thread);
        	
        	if(t == -1) {
        		return bestj;
        	}
        	
            // validity result...
            boolean res = false;
            
            // pick a node
            
            GPNode p1=null;  // the node we pick
            GPNode p2=null;
            int cnt = 0; //the number of primitives that satisfies the given component type
            int cntdown = 0;
            int flag = -1;//wheter it need to reselect the p1
            nodeselect.reset();
            
            double [] steps = {1,0.1,0.01,0.001,1e-4};
            double step = steps[state.random[thread].nextInt(steps.length)];
            int search_dir = state.random[thread].nextDouble()>0.5 ? 1 : -1;//-1 means we inverse the search direction
            
            p1=null;  // the node we pick
            p2=null;
            cnt = 0; //the number of primitives that satisfies the given component type
            cntdown = 0;
            GPTree oriTree = bestj.getTree(t);
            flag = -1;//wheter it need to reselect the p1
            
            p1 = getRandFuncEntity (bestj.getTreeStruct(t), state, thread );
            
            for(int ls=0;ls<5;ls++)
            {	        	
            	j = ((LGPIndividual)bestj).lightClone();
            	
	        	// pick random tree
	            if (tree!=TREE_UNFIXED)
	                t = tree;
	            
	            p2 = p1.lightClone();
	            ((Args4Function_SRMT)((Entity) p2).getArguments()).setStep(step);
	            ((Args4Function_SRMT)((Entity) p2).getArguments()).setSearchDirection(search_dir);
				((Entity) p2).getArguments().varyNode(state, thread, (Entity) p1);	
				
	            //res = verifyPoints(p2,p1);  // p2 can fit in p1's spot  -- the order is important!
	            
	            //easy check
	            res = checkPoints(p1, p2, state, thread, bestj, bestj.getTreeStruct(t));
	            //instance check
	
	            // did we get something that had both nodes verified?
	            if (!res) {
	            	System.err.print("The TunableFunctionLocalSearch got two inconsistent Function_EntityNode");
	            	System.exit(1);
	            }
	            
	            int x = t;
	            GPTreeStruct tree = j.getTreeStruct(x);
	            tree = (GPTreeStruct)(i.getTreeStruct(x).clone());
	            tree.owner = j;
	            tree.child = i.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
	            tree.child.parent = tree;
	            tree.child.argposition = 0;
	            j.setTree(x, tree);
	            j.evaluated = false; 
	            
	            //evaluate the newly search individual
	            
	            ((GPSymbolicRegressionMultiTarget)state.evaluator.p_problem).evaluate(state, j, subpopulation, thread);
	            Fitness loss2 = j.fitness;
				if(loss2.betterThan(bestj.fitness) && !loss2.equivalentTo(bestj.fitness)) {
					step *= 2;
					bestj = j;
				}
				else {
					step /= 2;
					search_dir *= -1;
				}
            }
        }
        
        return bestj;
	}
	
	protected int getLegalMutateIndex(LGPIndividual ind, EvolutionState state, int thread) {
		//find all instructions with tunable functions
		ArrayList<Integer> candi_instr = new ArrayList<>();
		
		for(int i = 0; i<ind.getTreesLength(); i++) {
			if(ind.getTreeStruct(i).status && getRandFuncEntity(ind.getTreeStruct(i), state, thread) != null) {
				candi_instr.add(i);
			}
		}
		
		if(candi_instr.size() == 0) {
			return -1;
		}
		
		int res = candi_instr.get( state.random[thread].nextInt(candi_instr.size()) );
		
		return res;
	}
	
	protected Function_EntityNode getRandFuncEntity(GPTreeStruct instr, EvolutionState state, int thread) {
		ArrayList<Function_EntityNode> list = new ArrayList<>();
		
		list = getFuncEntityList(instr.child, list);
		
		if(list.isEmpty())
			return null;
		else {
			return list.get(state.random[thread].nextInt(list.size()));
		}
	}
	
	private ArrayList<Function_EntityNode> getFuncEntityList(GPNode node, ArrayList<Function_EntityNode> list){
		if(node instanceof Function_EntityNode) {
			list.add((Function_EntityNode)node);
		}
		
		for(int c = 0; c<node.children.length; c++) {
			getFuncEntityList(node.children[c], list);
		}
		
		return list;
	}
	
	protected boolean checkPoints(GPNode p1, GPNode p2, EvolutionState state, int thread, LGPIndividual ind, GPTreeStruct treeStr) {
		// p1 and p2 must be different. if they are at destination, they should also be effective
		boolean res = false;
		
		if(p1 instanceof Function_EntityNode && p2 instanceof Function_EntityNode && p1.expectedChildren() == p2.expectedChildren()) {
			res = true;
			
			for(int c = 0;c<p1.children.length;c++) {
    			p2.children[c] = (GPNode)p1.children[c].clone();
    		}
		}
		
		return res;
	}
}
