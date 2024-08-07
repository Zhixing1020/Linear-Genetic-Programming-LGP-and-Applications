package zhixing.symbolicregression.individual;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.util.Code;
import ec.util.Parameter;
import yimei.jss.gp.data.DoubleData;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.simulation.state.SystemState;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.TGPInterface4Problem;
//import zhixing.jss.cpxInd.individual.CpxGPIndividual;

public class TGPIndividual4SR extends CpxGPIndividual implements TGPInterface4SR, TGPInterface4Problem{
	
	protected byte constraintsNum = 0;
	
	protected GPTree [] wrapTrees;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		
		Parameter def = defaultBase();
		
		// how many trees?
        int t = state.parameters.getInt(base.push(P_NUMTREES),def.push(P_NUMTREES),1);  // at least 1 tree for GP!
        if (t <= 0) 
            state.output.fatal("A GPIndividual must have at least one tree.",
                base.push(P_NUMTREES),def.push(P_NUMTREES));
        
        // load the trees
        trees = new GPTree[t];

        for (int x=0;x<t;x++)
            {
            Parameter p = base.push(P_TREE).push(""+x);
            trees[x] = (GPTree)(state.parameters.getInstanceForParameterEq(
                    p,def.push(P_TREE).push(""+x),GPTree.class));
            trees[x].owner = this;
            trees[x].setup(state,p);
            
            constraintsNum = trees[x].constraints; //simply assume all trees have the same constraint 
            }
        
        //set wrapper or not
		 towrap = state.parameters.getBoolean(base.push(P_TOWRAP), def.push(P_TOWRAP), false);
		 wrapTrees = new GPTree[t];
		 
        // now that our function sets are all associated with trees,
        // give the nodes a chance to determine whether or not this is
        // going to work for them (especially the ADFs).
        GPInitializer initializer = ((GPInitializer)state.initializer);
        for (int x=0;x<t;x++)
            {
            for(int w = 0;w < trees[x].constraints(initializer).functionset.nodes.length;w++)
                {
                GPNode[] gpfi = trees[x].constraints(initializer).functionset.nodes[w];
                for (int y = 0;y<gpfi.length;y++)
                    gpfi[y].checkConstraints(state,x,this,base);
                }
            }
        // because I promised with checkConstraints(...)
        state.output.exitIfErrors();
	}
	
	@Override
	public void rebuildIndividual(EvolutionState state, int thread) {
		
		for(GPTree t : trees) {
			t.constraints = this.constraintsNum;
			t.buildTree(state, thread);
		}
	}

	@Override
	public boolean equals(Object ind) {
		
		if (ind == null) return false;
        if (!(this.getClass().equals(ind.getClass()))) return false;  // GPIndividuals are special.
        GPIndividual i = (GPIndividual)ind;
        if (trees.length != i.getTrees().length) return false;
        // this default version works fine for most GPIndividuals.
        for(int x=0;x<trees.length;x++)
            if (!(trees[x].treeEquals(i.getTree(x)))) return false;
        return true;
	}

	@Override
	public int hashCode() {
		// stolen from GPNode.  It's a decent algorithm.
        int hash = this.getClass().hashCode();
        
        for(int x=0;x<trees.length;x++)
            hash =
                // Rotate hash and XOR
                (hash << 1 | hash >>> 31 ) ^
                trees[x].treeHashCode();
        return hash;
	}

	@Override
	public void verify(EvolutionState state) {
		if (!(state.initializer instanceof GPInitializer))
        { state.output.error("Initializer is not a GPInitializer"); return; }
        
	    // GPInitializer initializer = (GPInitializer)(state.initializer);
	
	    if (trees==null) 
	        { state.output.error("Null trees in GPIndividual."); return; }
	    for(int x=0;x<trees.length;x++) if (trees[x]==null) 
	                                        { state.output.error("Null tree (#"+x+") in GPIndividual."); return; }
	    for(int x=0;x<trees.length;x++)
	        trees[x].verify(state);
	    state.output.exitIfErrors();
	}

	@Override
	public void printTrees(EvolutionState state, int log) {
		for(int x=0;x<trees.length;x++)
        {
        state.output.println("Tree " + x + ":",log);
        trees[x].printTreeForHumans(state,log);
        }
	}

	@Override
	public void printIndividualForHumans(EvolutionState state, int log) {
		state.output.println(EVALUATED_PREAMBLE + (evaluated ? "true" : "false"), log);
        fitness.printFitnessForHumans(state,log);
        printTrees(state,log);
	}

	@Override
	public void printIndividual(EvolutionState state, int log) {
		state.output.println(EVALUATED_PREAMBLE + Code.encode(evaluated), log);
        fitness.printFitness(state,log);
        for(int x=0;x<trees.length;x++)
            {
            state.output.println("Tree " + x + ":",log);
            trees[x].printTree(state,log);
            }   
	}

	@Override
	public void printIndividual(EvolutionState state, PrintWriter writer) {
		writer.println(EVALUATED_PREAMBLE + Code.encode(evaluated));
        fitness.printFitness(state,writer);
        for(int x=0;x<trees.length;x++)
            {
            writer.println("Tree " + x + ":");
            trees[x].printTree(state,writer);
            }   
	}

	@Override
	public void writeGenotype(EvolutionState state, DataOutput dataOutput) throws IOException {
		dataOutput.writeInt(trees.length);
        for(int x=0;x<trees.length;x++)
            trees[x].writeTree(state,dataOutput);
	}

	@Override
	public void readGenotype(EvolutionState state, DataInput dataInput) throws IOException {
		int treelength = dataInput.readInt();
        if (trees == null || treelength != trees.length) // wrong size!
            state.output.fatal("Number of trees differ in GPIndividual when reading from readGenotype(EvolutionState, DataInput).");
        for(int x=0;x<trees.length;x++)
            trees[x].readTree(state,dataInput);
	}

	@Override
	public void parseGenotype(EvolutionState state, LineNumberReader reader) throws IOException {
		// Read my trees
        for(int x=0;x<trees.length;x++)
            {
            reader.readLine();  // throw it away -- it's the tree indicator
            trees[x].readTree(state,reader);
            }
	}

	@Override
	public Object clone()
    {
	    // a deep clone
	            
	    GPIndividual myobj = (GPIndividual)(super.clone());
	
	    // copy the tree array
	    myobj.renewTrees();
	    
	    for(int x=0;x<trees.length;x++)
	        {
	        myobj.setTree(x, (GPTree)(trees[x].clone()));  // force a deep clone
	        myobj.getTree(x).owner = myobj;  // reset owner away from me
	        }
	    return myobj;
    }
	
	@Override
	public CpxGPIndividual lightClone() {
		// a light clone
        CpxGPIndividual myobj = (CpxGPIndividual)(super.clone());
        
        // copy the tree array
        myobj.renewTrees();
        for(int x=0;x<trees.length;x++)
            {
            myobj.setTree(x, (GPTree)(trees[x].lightClone()));  // note light-cloned!
            myobj.getTree(x).owner = myobj;  // reset owner away from me
            }
        return myobj;
	}

	@Override
	public long size() {
		long size = 0;
        for(int x=0;x<trees.length;x++)
            size += trees[x].child.numNodes(GPNode.NODESEARCH_ALL);
        return size;
	}
	
	public void addTree(GPTree tree){
		if(trees == null){
			trees = new GPTree[1];
		}
		trees[0] = tree;
	}
	
//	@Override
//	public double execute(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual,
//			Problem problem) {
////		if(input == null) {
////			input = new DoubleData();
////		}
//		DoubleData tmp = new DoubleData();
//		
//        //assuming there is only one tree
//        trees[0].child.eval(state, thread, tmp, stack, this, problem);
//
//        input = tmp;
//        return tmp.value;
//	}
	
	@Override
	public double [] wrapper(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, double [] predict, double [] target) {
		return null;
	}
	
	@Override
	public ArrayList<Double[]> wrapper(ArrayList<Double[]> predict_list, ArrayList<Double[]> target_list){
		return null;
	}
	
	@Override
	public Object getWrapper() {
		return null;
	}
}
