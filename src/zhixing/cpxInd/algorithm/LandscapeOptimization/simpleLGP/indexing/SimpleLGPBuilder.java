package zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.indexing;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.gp.GPFunctionSet;
import ec.gp.GPNode;
import ec.gp.GPNodeParent;
import ec.gp.GPType;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexSymbolBuilder;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.primitive.Branching;
import zhixing.cpxInd.individual.primitive.FlowOperator;
import zhixing.cpxInd.individual.primitive.Iteration;

public abstract class SimpleLGPBuilder extends IndexSymbolBuilder {

	public static final String SIMPLEBUILDER = "simpleLGPbuilder";
	
	public ArrayList<GPNode> desReg;
	public ArrayList<GPNode> fun;
	public ArrayList<GPNode> constant;
	public ArrayList<GPNode> srcReg;

	public void setup(final EvolutionState state, final Parameter base)
    {
	    super.setup(state,base);
	
	    Parameter def = defaultBase();

    }
	
	@Override
	public Parameter defaultBase() {
		return new Parameter(SIMPLEBUILDER);
	}

	@Override
	public GPNode newRootedTree(EvolutionState state, GPType type, int thread, GPNodeParent parent, GPFunctionSet set,
			int argposition, int requestedSize) {
		
		System.err.print("the SimpleLGPBuilder does not support newRootedTree function\n");
		System.exit(1);
		
		return null;
	}
	
	abstract public void setSymbolSet(EvolutionState state, GPType type,final int argposition, GPFunctionSet set);
	
	@Override
	public ArrayList<GPTreeStruct> enumerateSymbols(EvolutionState state, GPType type, int thread,final int argposition, GPFunctionSet set){
		
		ArrayList<GPTreeStruct> collect = new ArrayList<>(maxNumSymbols);
		
		setSymbolSet(state, type,argposition, set);
        
        //fix size: since LGP instructions are with fixed depth, we skip this step
        
        //list all the possible primitives for certain positions
//        ArrayList<GPNode> desReg = new ArrayList<>(nodes.length);
//        ArrayList<GPNode> fun = new ArrayList<>(nodes.length);
        ArrayList<GPNode> arg0 = new ArrayList<>();
        ArrayList<GPNode> arg1 = new ArrayList<>();
        
        
        for(int i = 0; i<constant.size();i++) {
        	GPNode n = constant.get(i).lightClone();
        	
        	arg0.add(n);
        	arg1.add(n);
        }
        for(int i = 0; i<srcReg.size(); i++) {
        	GPNode n = srcReg.get(i).lightClone();
        	
        	arg0.add(n);
        	arg1.add(n);
        }
        
        //construct instructions
        for(GPNode a : desReg) {

        	for(GPNode b : fun) {

        		for(int c = 0; c<arg0.size();c++) {

    				for(int d = 0; d<arg1.size(); d++) {
    					GPTreeStruct cand = new GPTreeStruct();
						cand.child = a.lightClone();
						cand.child.children[0] = b.lightClone();
						cand.child.children[0].children[0] = arg0.get(c).lightClone(); //at least one input, such as Cos, Sin, Exp, Ln
						if(cand.child.children[0].children.length == 2)
							cand.child.children[0].children[1] = arg1.get(d).lightClone();
						
						if(b instanceof Branching) {
							cand.type = GPTreeStruct.BRANCHING;
						}
						else if(b instanceof Iteration) {
							cand.type = GPTreeStruct.ITERATION;
						}
    					
    					if(verifyInstruction(cand.child)) {

    						collect.add(cand);
    					}
    				}
    			}
        	}
        }
        
        if(collect.size() > maxNumSymbols) {
        	System.out.print("there are too many symbols in the list (" + collect.size() +"), please consider using simpler symbols");
        }
        
        initialized = true;
        
        return collect;
	}

	protected boolean verifyInstruction(final GPNodeParent parent) {
		
		boolean res = true;
		
		//check the ratio of constants
    	GPNode root = (GPNode)parent;
    	
    	if(root instanceof FlowOperator) return true;
    	
    	int terminalsize = root.numNodes(GPNode.NODESEARCH_TERMINALS);
    	int constnatsize = root.numNodes(GPNode.NODESEARCH_CONSTANT);
    	int nullsize = root.numNodes(GPNode.NODESEARCH_NULL);
    	if(terminalsize > 0)
    		res = (constnatsize)/ (terminalsize + nullsize) <= probCons; //if add one more constant, will it be larger than probCons?
    	else {
			res = false;//never initialize constant as the first terminal in instruction
		}
    	
    	
    	return res;
	}
}
