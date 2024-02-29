package zhixing.cpxInd.individual.reproduce;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.gp.GPFunctionSet;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import ec.util.Parameter;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.LGPIndividual;

public class GraphMicroMutation extends LGPMicroMutationPipeline{
	
	public static final String GRAPHMUT = "graphmut";
	
	public static final String P_WRITEREG = "writereg";
	public static final String P_READREG = "readreg";
	
	public boolean writeReg_on;
	public boolean readReg_on;
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = LGPDefaults.base().push(GRAPHMUT);
		
		writeReg_on = state.parameters.getBoolean(base.push(P_WRITEREG),def.push(P_WRITEREG), true);
		
		readReg_on = state.parameters.getBoolean(base.push(P_READREG),def.push(P_READREG), true);
	}

	public LGPIndividual produce(
			final int subpopulation,
	        final LGPIndividual ind,
	        final EvolutionState state,
	        final int thread){
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
        	
        	// pick random tree
            if (tree!=TREE_UNFIXED)
                t = tree;
            

            // validity result...
            boolean res = false;
            
            // prepare the nodeselector
            nodeselect.reset();
            
            // pick a node
            
            GPNode p1=null;  // the node we pick
            GPNode p2=null;
            int cnt = 0; //the number of primitives that satisfies the given component type
            int cntdown = 0;
            GPTree oriTree = i.getTree(t);
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

            for(int x=0;x<numTries;x++)
                {
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
						 	if(readReg_on) p2 = GraphMutateReadReg(i,t,set.nonconstants_v,state,thread);
						 	else {
								p2 = (GPNode)((GPNode) set.nonconstants_v.get(state.random[thread].nextInt(set.nonconstants_v.size()))).lightClone();
								p2.resetNode(state, thread);
							}
						}
						break;
					case writereg:
						if(writeReg_on) p2 = GraphMutateWriteReg(i,t,set.registers_v,state,thread);
						else{
							p2 = (GPNode)((GPNode) set.registers_v.get(state.random[thread].nextInt(set.registers_v.size()))).lightClone();
							p2.resetNode(state, thread);
						}
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
						if(p2 instanceof ReadRegisterGPNode){
							if(readReg_on) p2 = GraphMutateReadReg(i,t,set.nonconstants_v,state,thread);
						 	else {
								p2 = (GPNode)((GPNode) set.nonconstants_v.get(state.random[thread].nextInt(set.nonconstants_v.size()))).lightClone();
								p2.resetNode(state, thread);
							}
						}
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
	            GPTree tree = j.getTree(x);
                tree = (GPTree)(i.getTree(x).lightClone());
                tree.owner = j;
                tree.child = i.getTree(x).child.cloneReplacingNoSubclone(p2,p1);
                tree.child.parent = tree;
                tree.child.argposition = 0;
                j.setTree(x, tree);
                j.evaluated = false; 
            } // it's changed
            else{
            	int x = t;
            	GPTree tree = j.getTree(x);
        		tree = (GPTree)(i.getTree(x).lightClone());
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
	
	protected GPNode GraphMutateReadReg(LGPIndividual j, int index, Vector primitives, EvolutionState state, int thread){
		//prefer higher instructions
		
		GPNode p2 = (GPNode)((GPNode) primitives.get(0)).lightClone();
		
		if(index > 0){
			//collect the distance of different available registers
			double [] values = new double [j.getRegisters().length];
			for(int i = 0;i<values.length;i++){
				values[i] = 1; // larger value is prefer
			}
			
			for(int i = index - 1; i>=0; i--){
				//if(j.getTreeStruct(i).status)
				{
					int wr = ((WriteRegisterGPNode)j.getTreeStruct(i).child).getIndex();
					int height = subGraphHeight(j,i);
					if(height > values[wr]){ //if it does not have preference values
						values[wr] = height;
					}
				}
			}
	
			
//			HashSet<Integer> s = new HashSet<>();
//			Set<Integer> regs = j.getTreeStruct(index).collectReadRegister();
//			for(Integer r : regs){
//				s.clear();
//				s.add(r);
//				
//				int [] layers = new int [j.getRegisters().length]; //recursive call layers
//				int tmp_lay = 1;
//				layers[r] = index;
//				
//				for(int i = index - 1; i>=0; i--){				
//					if(j.getTreeStruct(i).status && s.contains(((WriteRegisterGPNode)j.getTreeStruct(i).child).getIndex())){
//						int wr = ((WriteRegisterGPNode)j.getTreeStruct(i).child).getIndex();
//						if(values[wr] == 0 || values[wr] > layers[wr]){ //if it does not have preference values
//							values[wr] = layers[wr];
//						}
//						s.remove(wr);
//						
//						//update layers of different ReadRegisters
//						tmp_lay ++;
//						Set<Integer> readRegs = j.getTreeStruct(i).collectReadRegister();
//						for(Integer rreg : readRegs){
//							if(layers[rreg] == 0 || layers[rreg] > tmp_lay ){
//								s.add(rreg);
//								
//								layers[rreg] = tmp_lay;
//							}
//						}
//					}
//				}
//			}
			
//			for(int i = 0;i<values.length;i++){
//				if(values[i] > 0)
//					values[i] = 1.0 / (values[i]+1); // larger value is prefer
//			}
			
			int reg = rouletteSelection(values, state, thread);
			
			((ReadRegisterGPNode) p2).setIndex(reg);
		}
		else{
			p2.resetNode(state, thread);
		}

		
		return p2;
	}
	
	protected GPNode GraphMutateWriteReg(LGPIndividual j, int index, Vector primitives, EvolutionState state, int thread){
		Set<Integer> EffRegs = j.getTreeStruct(index).effRegisters;
		
		//check these writeRegisters and theirs height in graph, replace smaller ones
		double [] values = new double [j.getRegisters().length];
		for(int i = 0;i<values.length;i++){
			values[i] = 1; // larger value is prefer
		}
		HashSet<Integer> s = new HashSet<>(EffRegs);
//		int [] layers = new int [j.getRegisters().length]; //recursive call layers
//		int tmp_lay = 1;
//		for(int i = 0; i<layers.length; i++){
//			if(s.contains(i)){
//				layers[i] = tmp_lay;
//			}
//		}
		
		for(int i = index - 1; i>=0; i--){
			if(s.contains(((WriteRegisterGPNode)j.getTreeStruct(i).child).getIndex())){

				int wr = ((WriteRegisterGPNode)j.getTreeStruct(i).child).getIndex();
				int height = subGraphHeight(j,i);
				if(height > values[wr]){ //if it does not have preference values
					values[wr] = height;
				}
//				s.remove(wr);
//				
//				//update layers of different ReadRegisters
//				tmp_lay ++;
//				Set<Integer> readRegs = j.getTreeStruct(i).collectReadRegister();
//				for(Integer rreg : readRegs){
//					if(layers[rreg] == 0 || layers[rreg] > tmp_lay ){
//						s.add(rreg);
//						
//						layers[rreg] = tmp_lay;
//					}
//				}
			}
		}
		
		for(int i = 0;i<values.length;i++){
			if(values[i] > 0)
				values[i] = 1.0 / (values[i]+1); // larger value is prefer
		}
		
		int reg = rouletteSelection(values, state, thread);
		
		GPNode p2 = (GPNode)((GPNode) primitives.get(0)).lightClone();
		((WriteRegisterGPNode) p2).setIndex(reg);
		
		return p2;
	}
	
	protected int rouletteSelection(double [] values, EvolutionState state, int thread){
		//values: bigger is better
		double sum = 0;
		for(int i = 0;i<values.length;i++){
			sum += values[i];
		}
		values[0] = values[0] / sum;
		for(int i = 1;i<values.length;i++){
			values[i] = values[i]/sum + values[i-1];
		}
		double rnd = state.random[thread].nextDouble();
		int res = values.length - 1;
		for(int i = 0;i<values.length;i++){
			if(rnd < values[i]){
				res = i;
				break;
			}
		}
		
		return res;
	}
	
	protected int subGraphHeight(LGPIndividual j, int index){
		
		Set<Integer> regs = j.getTreeStruct(index).collectReadRegister();
		
		int maxSubGraphHeight = 0;
		
		for(Integer r : regs){
			int tmpheight = 0;
			for(int i = index - 1; i>=0; i--){
				if(r == ((WriteRegisterGPNode)j.getTreeStruct(i).child).getIndex()){
					tmpheight = subGraphHeight(j, i);

					break;
				}
			}
			if(tmpheight > maxSubGraphHeight){
				maxSubGraphHeight = tmpheight;
			}
		}
		
		return 1 + maxSubGraphHeight;
	}
}
