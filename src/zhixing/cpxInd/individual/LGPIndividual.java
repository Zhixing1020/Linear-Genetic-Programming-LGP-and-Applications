package zhixing.cpxInd.individual;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPProblem;
import ec.gp.GPTree;
import ec.util.Code;
import ec.util.Output;
import ec.util.Parameter;
import yimei.jss.gp.function.Add;
import yimei.jss.gp.function.Mul;
import yimei.jss.gp.terminal.AttributeGPNode;
import zhixing.cpxInd.individual.primitive.Branching;
import zhixing.cpxInd.individual.primitive.ConstantGPNode;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.cpxInd.individual.primitive.FlowOperator;
import zhixing.cpxInd.individual.primitive.Iteration;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.util.LinearRegression;
import zhixing.symbolicregression.individual.primitive.InputFeatureGPNode;

public abstract class LGPIndividual extends CpxGPIndividual implements CpxGPInterface4Problem{


	private static final String P_NUMREGISTERS = "numregisters";
	private static final String P_MAXNUMTREES = "maxnumtrees";
	private static final String P_MINNUMTREES = "minnumtrees";
	
	private static final String P_INITMAXNUMTREES = "init_maxnumtrees";
	private static final String P_INITMINNUMTREES = "init_minnumtrees";
	
	private static final String P_RATEFLOWOPERATOR = "rate_flowoperator";
	private static final String P_MAXITERTIMES = "max_itertimes";
	
	private static final String P_NUMOUTPUTREGISTERS = "num-output-register";
	private static final String P_OUTPUTREGISTER = "output-register";
	private static final String P_FLOATOUTPUT = "to-float-outputs";
	
	private static final String P_EFFECTIVE_INITIAL = "effective_initial";
	
	protected Parameter privateParameter;
	
	protected int MaxNumTrees;
	protected int MinNumTrees;
	
	protected int initMaxNumTrees;
	protected int initMinNumTrees;
	
	protected int numRegs;
	protected int numOutputRegs;
	protected int tmp_numOutputRegs;
	protected boolean float_numOutputRegs = false;
	
	protected double rateFlowOperator;
	protected int maxIterTimes;
	
	protected boolean eff_initialize;
	
	protected double registers [] = null;
	//protected double constant_registers[] = null;
	
	protected ArrayList<GPTreeStruct> treelist;
	protected ArrayList<GPTreeStruct> wraplist;
	
	protected LGPFlowController flowctrl;
	
	protected int [] outputRegister;
	
	private int fastFlag;
	public ArrayList<GPTreeStruct> exec_trees;
	protected int initReg[] = null;
	protected int init_ConReg[] = null;
	protected GPNode initReg_values[] = null;
	
	protected byte constraintsNum = 0;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		//set up the individual prototype 
		 super.setup(state,base); 
		 Parameter def = defaultBase();
	     
		 // set my evaluation to false
		 evaluated = false;
		 
		 //set wrapper or not
		 towrap = state.parameters.getBoolean(base.push(P_TOWRAP), def.push(P_TOWRAP), false);
		 wraplist = new ArrayList<>();
		 wrap_max_sample = state.parameters.getInt(base.push(P_WRAP_MAX_SAMPLE), def.push(P_WRAP_MAX_SAMPLE), 1000);
		 if(wrap_max_sample < 1) {
			 state.output.fatal("The wrap_max_sample must be larger than 1.",
					 base.push(P_WRAP_MAX_SAMPLE), def.push(P_WRAP_MAX_SAMPLE));
		 }
		 
		 float_numOutputRegs = state.parameters.getBoolean(base.push(P_FLOATOUTPUT), def.push(P_FLOATOUTPUT), false);
		 
		 normalize_wrap = state.parameters.getBoolean(base.push(P_NORMWRAP), def.push(P_NORMWRAP), false);
		 normalize_f = state.parameters.getDoubleWithDefault(base.push(P_NORMWRAP_F), def.push(P_NORMWRAP_F), 1e-3);
	    
		 // the maximum/minimum number of trees
	     MaxNumTrees = state.parameters.getInt(base.push(P_MAXNUMTREES),def.push(P_MAXNUMTREES),1);  // at least 1 tree for GP!
	     if (MaxNumTrees <= 0) 
	         state.output.fatal("An LGPIndividual must have at least one tree.",
	             base.push(P_MAXNUMTREES),def.push(P_MAXNUMTREES));
	     
	     MinNumTrees = state.parameters.getInt(base.push(P_MINNUMTREES),def.push(P_MINNUMTREES),1);  // at least 1 tree for GP!
	     if (MinNumTrees <= 0) 
	         state.output.fatal("An LGPIndividual must have at least one tree.",
	             base.push(P_MINNUMTREES),def.push(P_MINNUMTREES));
	     
	     initMaxNumTrees = state.parameters.getInt(base.push(P_INITMAXNUMTREES),def.push(P_INITMAXNUMTREES),1);  // at least 1 tree for GP!
	     if (MaxNumTrees <= 0) 
	         state.output.fatal("An LGPIndividual must have at least one tree.",
	             base.push(P_INITMAXNUMTREES),def.push(P_INITMAXNUMTREES));
	     
	     initMinNumTrees = state.parameters.getInt(base.push(P_INITMINNUMTREES),def.push(P_INITMINNUMTREES),MinNumTrees);  // at least 1 tree for GP!
	     if (MinNumTrees <= 0) 
	         state.output.fatal("An LGPIndividual must have at least one tree.",
	             base.push(P_INITMINNUMTREES),def.push(P_INITMINNUMTREES));
	     
	     numRegs = state.parameters.getInt(base.push(P_NUMREGISTERS),def.push(P_NUMREGISTERS),1);  // at least 1 register for GP!
	     //numRegs = ((GPInitializer)state.initializer).treeConstraints[0].functionset.registers[0].length;  
	     		//first 0 index: index of tree constraints, second 0 index: index of register design
	     if (getNumRegs() <= 0) 
	         state.output.fatal("An LGPIndividual must have at least one register.",
	             base.push(P_NUMREGISTERS),def.push(P_NUMREGISTERS));
	     
	     rateFlowOperator = state.parameters.getDoubleWithDefault(base.push(P_RATEFLOWOPERATOR), def.push(P_RATEFLOWOPERATOR), 0.);
	     if(rateFlowOperator < 0 || rateFlowOperator > 1){
	    	 state.output.fatal("the rate of flow operator must be >=0 and <=1.",
	    			 base.push(P_RATEFLOWOPERATOR), def.push(P_RATEFLOWOPERATOR));
	     }
	     
	     maxIterTimes = state.parameters.getIntWithDefault(base.push(P_MAXITERTIMES), def.push(P_MAXITERTIMES), 100);
	     if(maxIterTimes <=0){
	    	 state.output.fatal("max iteration times must be >=1", base.push(P_MAXITERTIMES), def.push(P_MAXITERTIMES));
	     }
	     
	     eff_initialize = state.parameters.getBoolean(base.push(P_EFFECTIVE_INITIAL), def.push(P_EFFECTIVE_INITIAL), false);
	     
	     numOutputRegs = state.parameters.getIntWithDefault(base.push(P_NUMOUTPUTREGISTERS),def.push(P_NUMOUTPUTREGISTERS),1);
	     if (numOutputRegs <= 0) 
	         state.output.fatal("An LGPIndividual must have at least one output register.",
	             base.push(P_NUMOUTPUTREGISTERS),def.push(P_NUMOUTPUTREGISTERS));
	     outputRegister = new int[numOutputRegs];
	     tmp_numOutputRegs = numOutputRegs;
	     for(int r = 0; r<numOutputRegs; r++){
//	    	 Parameter b = base.push(P_OUTPUTREGISTER).push("" + r);
//	            
//            int reg = state.parameters.getIntWithDefault(b, null, 0);
//            if(reg < 0 ){
//            	System.err.println("ERROR:");
//                System.err.println("output register must be >= 0.");
//                System.exit(1);
//            }
            getOutputRegister()[r] = r;
	     }
	     
	     // load the trees
	     treelist = new ArrayList<>();
	     exec_trees = new ArrayList<>();

	     for (int x=0;x<MaxNumTrees;x++)
         {
            Parameter p = base.push(P_TREE).push(""+0);
            privateParameter = p;
            GPTreeStruct t = (GPTreeStruct)(state.parameters.getInstanceForParameterEq(
                    p,def.push(P_TREE).push(""+0),GPTreeStruct.class));
            t.owner = this; 
            t.status = false;
            t.effRegisters = new HashSet<Integer>(0);
            t.setup(state,p);
            getTreelist().add(t);
            
            constraintsNum = t.constraints;
         }
	     
	     //initialize registers
	     setRegisters(new double [getNumRegs()]);
	     //resetRegisters();
	     //constant_registers = new double [JobShopAttribute.values().length];
	     
	     //flow controller
	     flowctrl = new LGPFlowController();
	     getFlowctrl().maxIterTimes = maxIterTimes;
	     
	     // now that our function sets are all associated with trees,
        // give the nodes a chance to determine whether or not this is
        // going to work for them (especially the ADFs).
        GPInitializer initializer = ((GPInitializer)state.initializer);
        int x = 0;
        for (GPTreeStruct tree: getTreelist())
            {
            for(int w = 0;w < tree.constraints(initializer).functionset.nodes.length;w++)
                {
                GPNode[] gpfi = tree.constraints(initializer).functionset.nodes[w];
                for (int y = 0;y<gpfi.length;y++)
                    gpfi[y].checkConstraints(state,x++,this,base);
                }
            }
        // because I promised with checkConstraints(...)
        state.output.exitIfErrors();
	}
	
	public void adjustTreesLength(EvolutionState state, int thread, int numtrees) {
		if(numtrees < getTreelist().size()) {
			int cnt = getTreelist().size() - numtrees;
			for(int i = 0;i<cnt;i++) {
				int index = state.random[thread].nextInt(getTreelist().size());
				getTreelist().remove(index);
				
			}
		}
		else if(numtrees > getTreelist().size()) {
			int cnt = numtrees - getTreelist().size();
			for(int i = 0;i<cnt;i++) {
				int index = state.random[thread].nextInt(getTreelist().size());
				GPTreeStruct newtGpTree = (GPTreeStruct)(state.parameters.getInstanceForParameterEq(
	                    privateParameter,defaultBase().push(P_TREE).push(""+0),GPTreeStruct.class));
				newtGpTree.owner = this;
				newtGpTree.status = false;
				newtGpTree.effRegisters = new HashSet<>(0);
				newtGpTree.setup(state, privateParameter);
				getTreelist().add(index, newtGpTree);
			}
		}
		
		updateStatus();
	}
	
	public void rebuildIndividual(EvolutionState state, int thread) {
		int numtrees = state.random[thread].nextInt(initMaxNumTrees - initMinNumTrees + 1) + initMinNumTrees;
		
		getTreelist().clear();
		
		for(int i =0;i<numtrees;i++){
			//GPTreeStruct tree = new GPTreeStruct();
			GPTreeStruct tree = (GPTreeStruct)(state.parameters.getInstanceForParameterEq(
                    privateParameter,defaultBase().push(P_TREE).push(""+0),GPTreeStruct.class));
			tree.constraints = this.constraintsNum;
			tree.buildTree(state, thread);
			getTreelist().add(tree);
		}
		
		
		updateStatus();
		
		if(eff_initialize){//if we have to ensure all the instructions are effective in the initialization
			this.removeIneffectiveInstr();
			int trial = 100*this.initMaxNumTrees;
			while(countStatus()<numtrees && trial>0){
				GPTreeStruct tree = (GPTreeStruct)(state.parameters.getInstanceForParameterEq(
	                    privateParameter,defaultBase().push(P_TREE).push(""+0),GPTreeStruct.class));
				tree.constraints = this.constraintsNum;
				tree.buildTree(state, thread);
				getTreelist().add(0,tree);
				updateStatus();
				this.removeIneffectiveInstr();
				trial --;
			}
		}

	}
	
	public double[] getRegisters(){
		return registers;
	}
	
	public double getRegisters(int i){
		return registers[i];
	}
	
//	public double[] getConstantRegisters(){
//		return constant_registers;
//	}
	
	public int[] getOutputRegisters(){
		return getOutputRegister();
	}
	
	public void setOutputRegisters(int[] tar){
		outputRegister = new int [tar.length];
		int i = 0;
		for(int t : tar){
			getOutputRegister()[i] = t;
			i++;
		}
	}
	
	public int getNumRegs() {
		return numRegs;
	}
	
	public int getNumOutputRegs() {
		return numOutputRegs;
	}
	public int getCurNumOutputRegs() {
		return tmp_numOutputRegs;
	}
	public void setCurNumOutputRegs(int num) {
		if(num > numOutputRegs) {
			System.err.print("cannot set the number of output registers "+ num + " larger than the maximum number: "+ numOutputRegs);
			System.exit(1);
		}
		if(isFloatingOutputs()) {
			tmp_numOutputRegs = num;
		}
	}
	public boolean isFloatingOutputs() {
		return float_numOutputRegs;
	}

	public int[] getInitReg() {
		return initReg;
	}

	public GPNode[] getInitReg_values() {
		return initReg_values;
	}
	
	public void resetIndividual(int numReg, int maxIterTime) {
		List<Integer> tmp = new ArrayList<>();
	     tmp.add(0);
	     resetIndividual(numReg, maxIterTime, tmp);
	}
	
	public void resetIndividual(int numReg, int maxIterTime, List<Integer> outReg){
		//numReg: the maximum number of registers,  maxIterTime: the maximum iteration time of loop structures,  outReg: output register
		numRegs = numReg;
		 maxIterTimes = maxIterTime;
		 // set my evaluation to false
		 evaluated = false;
		
		 //initialize registers
	     setRegisters(new double [getNumRegs()]);
	     //resetRegisters();
	     
	     //flow controller
	     flowctrl = new LGPFlowController();
	     getFlowctrl().maxIterTimes = maxIterTimes;
	     
	     // load the trees
	     treelist = new ArrayList<>();
	     
	     outputRegister = new int[outReg.size()]; //by default, only one output register and the first register is the output
	     for(int i=0;i<outReg.size();i++){
	    	 getOutputRegister()[i] = outReg.get(i);
	     }
	     numOutputRegs = tmp_numOutputRegs = outputRegister.length;
	}
	
	public void setRegister(int ind, double value){
		registers[ind] = value;
	}
	
	public static void resetRegisters(final Problem problem, final LGPIndividual ind){
		ind.resetRegisters(problem, 1, ind);
	}
	
	
	

	public int getMaxNumTrees(){
		return MaxNumTrees;
	}
	
	public int getMinNumTrees(){
		return MinNumTrees;
	}
	
	public int getInitMaxNumTrees(){
		return initMaxNumTrees;
	}
	
	public int getInitMinNumTrees(){
		return initMinNumTrees;
	}
	
	public FlowController getFlowController() {
		return getFlowctrl();
	}
	
	public double getrateFlowOperator() {
		return rateFlowOperator;
	}
	
	public  boolean equals(Object ind){
		if (ind == null) return false;
        if (!(this.getClass().equals(ind.getClass()))) return false;  // LGPIndividuals are special.
        LGPIndividual i = (LGPIndividual)ind;
        if (getTreelist().size() != i.getTreelist().size()) return false;
        // this default version works fine for most GPIndividuals.
        for(int x=0;x<getTreelist().size();x++)
            if (!(getTreelist().get(x).treeEquals(i.getTreelist().get(x)))) return false;

        return true;
	}
	
	public  int hashCode(){
		// stolen from GPNode.  It's a decent algorithm.
        int hash = this.getClass().hashCode();
        
        for(int x=0;x<getTreelist().size();x++)
            hash =
                // Rotate hash and XOR
                (hash << 1 | hash >>> 31 ) ^
                getTreelist().get(x).treeHashCode();
        return hash;
	}
	
	
	public  void verify(EvolutionState state){
		if (!(state.initializer instanceof GPInitializer))
        { state.output.error("Initializer is not a CpxGPInitializer"); return; }
        
	    // GPInitializer initializer = (GPInitializer)(state.initializer);
	
	    if (getTreelist()==null) 
	        { state.output.error("Null trees in CpxGPIndividual."); return; }
	    int x = 0;
	    for(GPTreeStruct tree: getTreelist()) {
	    	if (tree==null) 
	        { state.output.error("Null tree (#"+x+") in CpxGPIndividual."); return; }
	    	x++;
	    }
	    for(GPTreeStruct tree: getTreelist())
	        tree.verify(state);
	    state.output.exitIfErrors();
	}
	
	
	public  void printTrees(final EvolutionState state, final int log){
		int x = 0;
		for(GPTreeStruct tree: getTreelist())
        {
			if(!tree.status) {
				state.output.print("//", log);
			}
	        state.output.print("Ins " + x + ":\t",log);
	        if(tree.type == GPTreeStruct.ARITHMETIC) {
	        	 tree.printTreeForHumans(state,log);
	        }
	        else {
	        	//it is flow control instruction
	        	tree.child.children[0].printRootedTree(state, log, Output.V_VERBOSE);
	        	state.output.println("",log);
	        }
	        x++;
        }
		
		if(towrap) {
			for(GPTreeStruct tree : wraplist) {
				state.output.print("Ins " + x + ":\t",log);
				 tree.printTreeForHumans(state,log);
				 x++;
			}
		}
	}
	
	
	public  void printIndividualForHumans(final EvolutionState state, final int log){
		state.output.println(EVALUATED_PREAMBLE + (evaluated ? "true" : "false"), log);
        fitness.printFitnessForHumans(state,log);
        printTrees(state,log);
        int cnteff = countStatus();
        state.output.println("# Effective instructions:\t"+cnteff+"\teffective %:\t"+((double)cnteff)/getTreelist().size()*100, log);
	}
	
	
	public  void printIndividual(final EvolutionState state, final int log){
		state.output.println(EVALUATED_PREAMBLE + Code.encode(evaluated), log);
        fitness.printFitness(state,log);
        int x = 0;
        for(GPTreeStruct tree : getTreelist())
            {
            state.output.println("Ins " + x + ":",log);
            tree.printTree(state,log);
            x++;
            }   
	}
	
	
	public  void printIndividual(final EvolutionState state, final PrintWriter writer){
		writer.println(EVALUATED_PREAMBLE + Code.encode(evaluated));
        fitness.printFitness(state,writer);
        int x = 0;
        for(GPTreeStruct tree:getTreelist())
            {
            writer.println("Ins " + x + ":");
            tree.printTree(state,writer);
            x++;
            }   
	}
	
	/** Overridden for the GPIndividual genotype. */
	
    public  void writeGenotype(final EvolutionState state,
        final DataOutput dataOutput) throws IOException
    {
	    dataOutput.writeInt(getTreelist().size());
	    for(GPTreeStruct tree : getTreelist())
	        tree.writeTree(state,dataOutput);
    }

    /** Overridden for the GPIndividual genotype. */
	
    public  void readGenotype(final EvolutionState state,
        final DataInput dataInput)throws IOException
    {
	    int treelength = dataInput.readInt();
	    if(treelength > MaxNumTrees || treelength < MinNumTrees) {
	    	state.output.fatal("Number of trees is inconsistent with the given Max / Min NumTrees.");
	    }
	    if (getTreelist() == null)
	        state.output.fatal("null trees collections!");
	    
	    adjustTreesLength(state, 0, treelength);
	    
	    for(int x=0;x<getTreelist().size();x++)
	        getTreelist().get(x).readTree(state,dataInput);
    }

	
    public  void parseGenotype(final EvolutionState state,
        final LineNumberReader reader)throws IOException
    {
    	//suppose the tree in readLine has a same number of trees with the individual 
	    // Read my trees
	    for(int x=0;x<getTreelist().size();x++)
	        {
	        reader.readLine();  // throw it away -- it's the tree indicator
	        getTreelist().get(x).readTree(state,reader);
	        }
    }
    
    public Object clone() {
    	// a deep clone
		LGPIndividual myobj = (LGPIndividual)(super.clone());
		// copy the trees
        myobj.treelist = new ArrayList<GPTreeStruct>();
        for(GPTreeStruct tree : getTreelist())
            {
        	GPTreeStruct t = (GPTreeStruct)(tree.clone());
        	t.owner = myobj;
            myobj.getTreelist().add(t);  // note light-cloned!
            //myobj.trees[x].owner = myobj;  // reset owner away from me
            }
        myobj.copyLGPproperties(this);
        return myobj;
    }
    
    protected void copyLGPproperties(LGPIndividual obj) {
    	this.numRegs = obj.getRegisters().length;
		this.MaxNumTrees = obj.getMaxNumTrees();
		this.MinNumTrees = obj.getMinNumTrees();
		this.setRegisters(new double[getNumRegs()]);
		for(int i = 0; i<getNumRegs(); i++){
			this.registers[i] = obj.getRegisters()[i];
		}
		
		this.flowctrl = new LGPFlowController();
		this.maxIterTimes = this.getFlowctrl().maxIterTimes = obj.getFlowController().maxIterTimes;
		
		this.rateFlowOperator = obj.getrateFlowOperator();
		
		this.wraplist = new ArrayList<>();
		for(GPTreeStruct tree : (ArrayList<GPTreeStruct>)obj.getWrapper()) {
			GPTreeStruct t = (GPTreeStruct)(tree.clone());
        	t.owner = this;
        	this.wraplist.add(t);
		}
		
		this.tmp_numOutputRegs = obj.getCurNumOutputRegs();
		this.float_numOutputRegs = obj.isFloatingOutputs();
    }
    
    /** Like clone(), but doesn't force the GPTrees to deep-clone themselves. */
    public LGPIndividual lightClone() {
    	// a deep clone
		LGPIndividual myobj = (LGPIndividual)(super.clone());
		// copy the trees
        myobj.treelist = new ArrayList<GPTreeStruct>();
        for(GPTreeStruct tree : getTreelist())
            {
        	GPTreeStruct t = (GPTreeStruct)(tree.lightClone());
        	t.owner = myobj;
            myobj.getTreelist().add(t);  // note light-cloned!
            //myobj.trees[x].owner = myobj;  // reset owner away from me
            }
        myobj.copyLGPproperties(this);
        return myobj;
    }
    
    /** Returns the "size" of the individual, namely, the number of nodes
    in all of its subtrees.  */
	public  long size(){
		long size = 0;
	    for(GPTreeStruct tree : getTreelist())
	        size += tree.child.numNodes(GPNode.NODESEARCH_ALL);
	    return size;
	}
	
	@Override
	public GPTree getTree(int index){
		if(index >= getTreelist().size()){
    		System.out.println("The tree index " + index + " is out of range");
    		System.exit(1);
    	}
    	return getTreelist().get(index);
	}
	
	public GPTreeStruct getTreeStruct(int index) {
		if(index >= getTreelist().size()){
    		System.out.println("The tree index " + index + " is out of range");
    		System.exit(1);
    	}
    	return getTreelist().get(index);
	}
	
	public List<GPTreeStruct> getTreeStructs(){
		return getTreelist();
	}
	
	@Override
	public boolean setTree(int index, GPTree tree){
		if(index < getTreelist().size()){
			getTreelist().remove(index);
			GPTreeStruct treeStr = (GPTreeStruct)tree;
			if(!(tree instanceof GPTreeStruct)) {
				treeStr.status = false;
				treeStr.effRegisters = new HashSet<>(0);
			}
			treeStr.type = GPTreeStruct.ARITHMETIC;
			if(treeStr.child.children[0] instanceof FlowOperator){
				if(treeStr.child.children[0] instanceof Branching){
					treeStr.type = GPTreeStruct.BRANCHING;
				}
				else{
					treeStr.type = GPTreeStruct.ITERATION;
				}
			}
			getTreelist().add(index, treeStr);
			
			this.evaluated = false;
			
			updateStatus();
			
			return true;
		}
		System.out.println("setTree index: " + index + " is out of range " + getTreelist().size());
		return false;
	}
	
	public void addTree(int index, GPTree tree){
		//add "tree" to the index slot
		GPTreeStruct treeStr;
		if(tree instanceof GPTreeStruct){
			treeStr = (GPTreeStruct) tree;
		}
		else{
			treeStr = new GPTreeStruct();
			treeStr.assignfrom(tree);
			treeStr.status = false;
			treeStr.effRegisters = new HashSet<>(0);
		}
		
		treeStr.type = GPTreeStruct.ARITHMETIC;
		if(treeStr.child.children[0] instanceof FlowOperator){
			if(treeStr.child.children[0] instanceof Branching){
				treeStr.type = GPTreeStruct.BRANCHING;
			}
			else{
				treeStr.type = GPTreeStruct.ITERATION;
			}
		}
		if(index < 0) index = 0;
		if(index < getTreelist().size()) {
			getTreelist().add(index, treeStr);
		}
		else {
			getTreelist().add(treeStr);
		}
		
		this.evaluated = false;
		
		updateStatus();
	}
	
	public boolean removeTree(int index) {
		if(index < getTreelist().size()){
			getTreelist().remove(index);
			
			this.evaluated = false;
			
			updateStatus();
			
			return true;
		}
		System.out.println("removeTree index: " + index + " is out of range " + getTreelist().size());
		return false;
	}
	
	public boolean removeIneffectiveInstr(){
		for(int ii = 0;ii<this.getTreesLength();ii++) {
			if(!this.getTreeStruct(ii).status && this.getTreesLength()>this.getMinNumTrees()) {
				this.removeTree(ii);
				ii--; //ii remain no change, so that it can point to the next tree
			}
		}
		return true;
	}
	
	@Override
	public int getTreesLength(){
		return getTreelist().size();
	}
	
	public int getEffTreesLength(){
		updateStatus();
		int res = 0;
		for(GPTreeStruct tree : getTreelist()){
			if (tree.status){
				res ++;
			}
		}
		
		return res;
	}
	
	public double getAvgNumEffFun() {
		updateStatus();
		double res = 0;
		int cnt_tree = 0;
		for(GPTreeStruct tree : getTreelist()){
			if (tree.status){
				cnt_tree ++;
				res += tree.child.numNodes(GPNode.NODESEARCH_NONTERMINALS);
			}
		}
		
		return res / cnt_tree;
	}
	
	public double getAvgNumFun() {
//		updateStatus();
		double res = 0;
		int cnt_tree = 0;
		for(GPTreeStruct tree : getTreelist()){
			cnt_tree ++;
			res += tree.child.numNodes(GPNode.NODESEARCH_NONTERMINALS);
		}
		
		return res / cnt_tree;
	}
	
	public double getNumEffNode() {
		updateStatus();
		double res = 0;
//		int cnt_tree = 0;
		for(GPTreeStruct tree : getTreelist()){
			if (tree.status){
//				cnt_tree ++;
				res += tree.child.numNodes(GPNode.NODESEARCH_ALL) - tree.child.numNodes(GPNode.NODESEARCH_READREG) - 1;  //"-1" is for WriteRegister
			}
		}
		
		return res ;
	}
	
	public double getProgramSize() {
//		return getEffTreesLength() * getAvgNumEffFun() * 2.0;
		return getNumEffNode();
	}
	
	public void updateStatus(int n, int []tar) {
		//identify which instructions are extrons and vise versa
		//start to update the status from position n
		//tar: the output register
		
		boolean statusArray [] = new boolean [getTreelist().size()];
		boolean sourceArray [][] = new boolean [getTreelist().size()][getNumRegs()];
		boolean destinationArray [][] = new boolean [getTreelist().size()][getNumRegs()];
		
		Set<Integer> source, destination;
		
		if(n > getTreelist().size()) {
			System.out.println("The n in updateStatus is larger than existing tree list");
			System.exit(1);
		}
		
		ListIterator<GPTreeStruct> it = getTreelist().listIterator();
		int cn = n;
		while(it.hasNext() && cn > 0) {
			it.next();
			cn --;
		}
		
		final int [] output = tar;
		
		//initialize target effective registers
		Set<Integer> targetRegister = new HashSet<>(0);
		for(int i = 0; i<tar.length; i++)
			targetRegister.add(tar[i]);
		
		//backward loop
		cn = n - 1;  //serve as index
		while(it.hasPrevious()) {
			
			GPTreeStruct tree = (GPTreeStruct) it.previous();
			
			tree.effRegisters = new HashSet<>(0);
			Iterator<Integer> itt = targetRegister.iterator();
        	while(itt.hasNext()) {
        		int v = itt.next();
        		tree.effRegisters.add(v);
        	}
        	
			
			//check it is effective
			if(tree.child.children[0] instanceof Branching){
				//branching. if its body contain effective instruction, it is effective too.
				tree.type = GPTreeStruct.BRANCHING;
				tree.status = statusArray[cn] = false;
				for(int i = cn + 1; i < getTreelist().size() && i <= ((FlowOperator)tree.child.children[0]).getBodyLength()+cn;i++){
					if(statusArray[i]){
						tree.status = statusArray[cn] = true;

						break;
					}
				}
				
				//since the flow control body might not be active in the execution,  we need to add the effective registers subsequent to the body.
				int bodyend = ((FlowOperator)tree.child.children[0]).getBodyLength() + cn;
				for(int effii = cn + 1; effii<=bodyend && effii < getTreelist().size(); effii++) {
					if(getTreelist().get(effii).child.children[0] instanceof FlowOperator) {
						bodyend = Math.max(bodyend, ((FlowOperator)getTreelist().get(effii).child.children[0]).getBodyLength() + effii);
					}
				}
				int effi = bodyend + 1;
				if(effi >= getTreelist().size()) {
					for(int ii = 0; ii<output.length; ii++)
						targetRegister.add(output[ii]);
				}
				else {
					for(Integer r : getTreelist().get(effi).effRegisters) {
						targetRegister.add(r);
					}
					if(statusArray[effi]) {
						if(getTreelist().get(effi).type == GPTreeStruct.ARITHMETIC) {
							targetRegister.remove(((WriteRegisterGPNode) getTreelist().get(effi).child ).getIndex());
						}
						getTreelist().get(effi).updateEffRegister(targetRegister);
					}
					
				}
				
				itt = targetRegister.iterator();
	        	while(itt.hasNext()) {
	        		int v = itt.next();
	        		tree.effRegisters.add(v);
	        	}
				//====================================
				
				if(statusArray[cn] == true) {
					tree.updateEffRegister(targetRegister); //add the read registers in the branching instruction into targetRegister
				}
				
				collectReadRegister(tree.child, sourceArray[cn]);
				
				
			}
			else if (tree.child.children[0] instanceof Iteration){
				//iteration. if the effective instructions in its body have a nonempty intersection set
				//of source and destination register
				tree.type = GPTreeStruct.ITERATION;
				source = new HashSet<>();
				destination = new HashSet<>();
				
				//collect arithmetic instruction and their source and destination register
//				int j = 0;
				boolean effective_block_exist = false;
				for(int i = cn + 1; i < (this.getTreelist()).size() 
						&& i <= ((FlowOperator)tree.child.children[0]).getBodyLength()+cn; i++){
					if(statusArray[i] && !(getTreelist().get(i).child.children[0] instanceof FlowOperator) 
//							&& j < ((FlowOperator)tree.child.children[0]).getBodyLength()
							){

						for(int r = 0; r<getNumRegs(); r++){
							if(sourceArray[i][r]) source.add(r);
							if(destinationArray[i][r]) destination.add(r);
						}
//						j++;
//						if(j>=((FlowOperator)tree.child.children[0]).getBodyLength()) break;
					}
					else {
						//check the nested program block. we don't explicitly check the instructions in the nest program block
						//as long as the nested one is effective, we simply see the outter program block is effective too.
						if(statusArray[i] && (getTreelist().get(i).child.children[0] instanceof FlowOperator)) {
							effective_block_exist = true;
							i += ((FlowOperator)getTreelist().get(i).child.children[0]).getBodyLength() - 1;
						}
					}
				}
				source.retainAll(destination);
				
				//since the flow control body might not be active in the execution,  we need to add the effective registers subsequent to the body.
				int bodyend = ((FlowOperator)tree.child.children[0]).getBodyLength() + cn;
				for(int effii = cn + 1; effii<=bodyend && effii < getTreelist().size(); effii++) {
					if(getTreelist().get(effii).child.children[0] instanceof FlowOperator) {
						bodyend = Math.max(bodyend, ((FlowOperator)getTreelist().get(effii).child.children[0]).getBodyLength() + effii);
					}
				}
				int effi = bodyend + 1;
				if(effi >= getTreelist().size()) {
					for(int i = 0; i<output.length; i++)
						targetRegister.add(output[i]);
				}
				else {
					for(Integer r : getTreelist().get(effi).effRegisters) {
						targetRegister.add(r);
					}
					if(statusArray[effi]) {
						if(getTreelist().get(effi).type == GPTreeStruct.ARITHMETIC) {
							targetRegister.remove(((WriteRegisterGPNode) getTreelist().get(effi).child ).getIndex());
						}
						getTreelist().get(effi).updateEffRegister(targetRegister);
					}
					
				}
				
				itt = targetRegister.iterator();
	        	while(itt.hasNext()) {
	        		int v = itt.next();
	        		tree.effRegisters.add(v);
	        	}
				//=======================================
				
				if(!source.isEmpty() || effective_block_exist){
				    tree.status = statusArray[cn]=true;
				    tree.updateEffRegister(targetRegister);
				}
				else {
					tree.status = statusArray[cn]=false;
				}
				collectReadRegister(tree.child, sourceArray[cn]);
				
				
			}
			else {
				tree.type = GPTreeStruct.ARITHMETIC;
				
				
				if(targetRegister.contains(((WriteRegisterGPNode) tree.child).getIndex())){
					tree.status = true;
					targetRegister.remove(((WriteRegisterGPNode) tree.child).getIndex());
					tree.updateEffRegister(targetRegister);
					
					statusArray[cn] = true;
				}
				else {
					tree.status = false;
					
					statusArray[cn] = false;
				}
				
				destinationArray[cn][((WriteRegisterGPNode) tree.child).getIndex()] = true;
				collectReadRegister(tree.child, sourceArray[cn]);
			} 
			cn--;
		}
		
	}
	
	public void updateStatus() {
		updateStatus(getTreelist().size(),getOutputRegister());
	}
	
	public ArrayList<Integer> getSubGraph(int n, Integer [] tar){
		//return the indices of the instructions which form the sub-graph.
		//the sub-graph is searched from index "n", with the target output indicated by "tar".
		
		updateStatus();
		
		if(n > getTreelist().size()) {
			System.out.println("The n in updateStatus is larger than existing tree list");
			System.exit(1);
		}
		
		ArrayList<Integer> graph = new ArrayList<>();
		
		//initialize target effective registers
		Set<Integer> targetRegister = new HashSet<>(0);
		for(int i = 0; i<tar.length; i++)
			targetRegister.add(tar[i]);
		
		//backward loop
		int cn = n;
		do{
			GPTreeStruct tree = getTreeStruct(cn);
			if( tree.status && targetRegister.contains(((WriteRegisterGPNode) tree.child).getIndex())){
				graph.add(cn);
				
				//update targetRegister
				targetRegister.remove(((WriteRegisterGPNode) tree.child).getIndex());
				tree.updateEffRegister(targetRegister);
			}
			cn --;
		}while(cn >= 0);
		
		return graph;
		
	}
	
	public ArrayList<Integer> getPartialSubGraph(int n, int [] tar, double rate, final EvolutionState state, final int thread){
		//return the indices of the instructions which form the sub-graph based on a probability "rate". this function might randomly abandon some graph nodes
		//the sub-graph is searched from index "n", with the target output indicated by "tar".
		
		updateStatus();
		
		if(n > getTreelist().size()) {
			System.out.println("The n in updateStatus is larger than existing tree list");
			System.exit(1);
		}
		
		ArrayList<Integer> graph = new ArrayList<>();
		
		//initialize target effective registers
		Set<Integer> targetRegister = new HashSet<>(0);
		for(int i = 0; i<tar.length; i++)
			targetRegister.add(tar[i]);
		
		//backward loop
		int cn = n;
		do{
			GPTreeStruct tree = getTreeStruct(cn);
			if( tree.status && targetRegister.contains(((WriteRegisterGPNode) tree.child).getIndex())){
				graph.add(cn);
				
				//update targetRegister
				targetRegister.remove(((WriteRegisterGPNode) tree.child).getIndex());
				
				Set<Integer> s = tree.collectReadRegister();
				Iterator<Integer> it = s.iterator();
				while(it.hasNext()){
					it.next();
					if(s.size()>1 && state.random[thread].nextDouble()>rate){
						it.remove();
					}
				}
				
				for(Integer r : s){
					targetRegister.add(r);
				}
			}
			cn --;
		}while(cn >= 0);
		
		return graph;
		
	}
	
	public ArrayList<Integer> getIntersecSubGraph(Set<Integer> tar, int maintarget){
		ArrayList<Integer> res=null, res_tmp=null, tmp=null;
		
		res = this.getSubGraph(this.getTreesLength()-1, new Integer[]{maintarget});
		
		for(int output : tar){
			tmp = this.getSubGraph(this.getTreesLength()-1, new Integer[]{output});
			if(res_tmp == null){
				res_tmp = (ArrayList<Integer>) tmp.clone();
			}
			else{
				res_tmp.retainAll(tmp);
			}
		}
		
		if(res_tmp.size() == 0)
			return res;
		else
			return res_tmp;
	}
	
	public ArrayList<Pair<String, ArrayList<String>>> getAdjacencyTable(EvolutionState state){
		return getAdjacencyTable(state, 0, this.getTreesLength());
	}
	
	public int countStatus(){
		int cnt = 0;
		for(GPTreeStruct tree:getTreelist()){
			if(tree.status){
				cnt++;
			}
		}
		return cnt;
	}
	
	public int countStatus(int start, int end){
		//return the number of effective instructions in [start, end)
		int cnt = 0;
		for(int i = start; i<end; i++){
			GPTreeStruct tree = getTreelist().get(i);
			if(tree.status){
				cnt++;
			}
		}
		return cnt;
	}
	
	public boolean canAddFlowOperator(){
		boolean res = true;
		int cnt = 0;
		for(GPTreeStruct tree:getTreelist()){
			if(tree.child.children[0] instanceof FlowOperator){
				cnt++;
			}
		}
		if(((double)cnt)/getTreelist().size() > rateFlowOperator) res = false;
		return res;
	}
	
	protected void collectReadRegister(GPNode node, boolean[] collect){
		//collect the source registers for an instruction
		//node: an instruction primitive
		if(node instanceof ReadRegisterGPNode){
			collect[((ReadRegisterGPNode) node).getIndex()] = true;
		}
		else{
			if(node.children.length > 0){
				for(int n =0;n<node.children.length;n++){
					collectReadRegister(node.children[n], collect);
				}
			}
		}
		return;
	}	

	public String makeGraphvizRule(){
		ArrayList<Integer> outputReg_list = new ArrayList<>();
		for(int r : this.getOutputRegisters()) {
			outputReg_list.add(r);
		}
		return makeGraphvizRule(outputReg_list);
	}
	
	public double getMeanEffDepenDistance(){
		ArrayList<Integer> DependenceDis = new ArrayList<>();
		
		
		
		for(int i = 0;i<getTreesLength();i++){
			GPTreeStruct tree = getTreeStruct(i);
			
			if(!tree.status || tree.type !=GPTreeStruct.ARITHMETIC) continue;  //ineffective or not arithmetic instruction
			
			int j = 1;
			for(;i+j<getTreesLength();j++){
				if(!getTreeStruct(i+j).status || getTreeStruct(i+j).type !=GPTreeStruct.ARITHMETIC) continue;  //ineffective or not arithmetic instruction
				
				if(getTreeStruct(i+j).collectReadRegister().contains(((WriteRegisterGPNode)tree.child).getIndex())){
					DependenceDis.add(j);
					break;
				}
			}
		}
		
		if(DependenceDis.size()>0){
			double res = 0;
			for(Integer a : DependenceDis){
				res += a;
			}
			return res / DependenceDis.size();
		}
		else
			return 0;
	}
	
	public ArrayList<Double> getEffDegree(){
		ArrayList<Double> degreeList = new ArrayList<>();
		
		//arithmetic instructions
		for(int i = 0;i<getTreesLength();i++){
			degreeList.add(0.0);
			GPTreeStruct tree = getTreeStruct(i);
			
			if(!tree.status || tree.type !=GPTreeStruct.ARITHMETIC) continue;  //ineffective or not arithmetic instruction
			
			
			for(int j = 1;i+j<getTreesLength();j++){
				if(!getTreeStruct(i+j).status || getTreeStruct(i+j).type !=GPTreeStruct.ARITHMETIC) continue;  //ineffective or not arithmetic instruction
				
				List<Integer> tmp = getTreeStruct(i+j).collectReadRegister_list();
				
				if(tmp.contains(((WriteRegisterGPNode)tree.child).getIndex())){
					for(Integer t : tmp){
						if(t == ((WriteRegisterGPNode)tree.child).getIndex()){
							degreeList.set(i, degreeList.get(i)+1);
						}
					}
				}
				
				//check the destination register, if the WriteRegister of instruction i is written, break;
				if(((WriteRegisterGPNode)getTreeStruct(i+j).child).getIndex() == ((WriteRegisterGPNode)tree.child).getIndex()){
					break;
				}
			}
		}
		
		//branching and iteration instruction, take the average dependence degree in the branched instructions as their dependence degree
		for(int i = 0;i<getTreesLength();i++){
			GPTreeStruct tree = getTreeStruct(i);
			
			if(!tree.status || tree.type ==GPTreeStruct.ARITHMETIC) continue; 
			
			int bodyLength = ((FlowOperator)tree.child.children[0]).getBodyLength();
			
			double degree = 0;
			double cnt = 0;
			
			for(int j = 1;j<=bodyLength && i+j < getTreesLength();j++){
				if(!getTreeStruct(i+j).status) continue;
				
				//extend the body length if there is a nested one
				if(getTreeStruct(i+j).type != GPTreeStruct.ARITHMETIC) 
					bodyLength += ((FlowOperator)getTreeStruct(i+j).child.children[0]).getBodyLength();
				else {
					degree += degreeList.get(i+j);
					cnt ++;
				}
			}
			
			degreeList.set(i, degree / cnt);
			
		}
		return degreeList;
	}
	
	public ArrayList<Integer> getNumEffRegister(){
		ArrayList<Integer> EffRegList = new ArrayList<>();
		
		updateStatus();
		
		for(GPTreeStruct tree : getTreelist()){
			EffRegList.add(tree.effRegisters.size());
		}
		
		return EffRegList;
	}
	
	public int getApproximateGraphWidth(int index) {
		ArrayList<Integer> tmp_list = getNumEffRegister();
		if(index >= tmp_list.size()) return tmp_list.get(tmp_list.size()-1);
		if(index < 0) return tmp_list.get(0);
		
		return tmp_list.get(index);
	}
	
	public int getEffectiveIndex(int index) {
		//inputs: instruction index in genome
		//outputs: the index with only effective instructions. if it is an intron, returns the index of the closest effective instruction with smaller structural index;
		//if index exceeds the maximum index, return # effective instructions (i.e., the root), if it is smaller than the minimum index, returns 0;
		if(index < 0) return 0;
		if(index >= getTreesLength()) return getEffTreesLength()-1;
		
		if(!getTreeStruct(index).status) {
			int i;
			for(i = index - 1; i>=0; i--) {
				if(getTreeStruct(i).status) {
					return getEffectiveIndex(i);
				}
			}
			if(i<0) return 0;
		}
		
		int res = -1;
		
		for(int i = 0;i<=index;i++) {
			if(getTreeStruct(i).status) res++;
		}
		
		return res;
	}

	public int getFastFlag() {
		return fastFlag;
	}

	public void setFastFlag(int fastFlag) {
		this.fastFlag = fastFlag;
	}

	public ArrayList<GPTreeStruct> getTreelist() {
		return treelist;
	}

	public void setInitReg(int initReg[]) {
		this.initReg = initReg;
	}

	public void setRegisters(double registers[]) {
		this.registers = registers;
	}

	public void setInitReg_values(GPNode initReg_values[]) {
		this.initReg_values = initReg_values;
	}

	public LGPFlowController getFlowctrl() {
		return flowctrl;
	}

	public int [] getOutputRegister() {
		return outputRegister;
	}

	@Override
	public double [] wrapper(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, double [] predict, double [] target) {
		double mean_pred = 0;
		double mean_real = 0;
		double a = 0, b = 0;
		
		int n = target.length;
		if(predict.length != n) {
			System.err.print("In wrapper, the dimension of predict " + predict.length + " and target " + target.length + " must be the same\n");
			System.exit(1);
		}
		
		for(int i = 0; i<n; i++) {
			mean_pred += predict[i];
			mean_real += target[i];
		}
		mean_pred /= n;
		mean_real /= n;
		
		//get a
		double quo = 0;
		double div = 0;
		for(int i = 0; i<n; i++) {
			quo += (predict[i] - mean_pred)*(target[i] - mean_real);
			div += (predict[i] - mean_pred)*(predict[i] - mean_pred);
		}
		if(div != 0)
			a = quo / div;
		else
			a = 0;
		
		//get b
		b = mean_real - a*mean_pred;
		
		//construct the wrap list
		wraplist.clear();
		for(int r = 0; r<outputRegister.length; r++) {
			int index = outputRegister[r];
			GPTreeStruct cand = new GPTreeStruct();
			
			WriteRegisterGPNode desReg = new WriteRegisterGPNode();
			desReg.setIndex(index);
			desReg.argposition = (byte) 0;
			
			ReadRegisterGPNode srcReg = new ReadRegisterGPNode();
			srcReg.setIndex(index);
			srcReg.argposition = (byte) 0;
			
			ConstantGPNode A = new ConstantGPNode();
			A.setValue(a);
			A.argposition = (byte) 0;
			ConstantGPNode B = new ConstantGPNode();
			B.setValue(b);
			B.argposition = (byte) 0;
			
			cand.child = desReg;
			desReg.children = new GPNode [1];
			cand.child.children[0] = new Add();
			cand.child.children[0].children = new GPNode[2];
			cand.child.children[0].children[0] = new Mul(); 
			cand.child.children[0].children[0].children = new GPNode[2];
			cand.child.children[0].children[1] = B;
			cand.child.children[0].children[0].children[0] = srcReg;
			cand.child.children[0].children[0].children[1] = A;
			
			wraplist.add(cand);
		}
		
		
		//scale the predict
		double [] newpred = new double [n];
		for(int i = 0; i<n; i++) {
			newpred[i] = a * predict[i] + b;
			if(newpred[i] > 1e6) {
				newpred[i] = 1e6;
			}
			if(newpred[i] < -1e6) {
				newpred[i] = -1e6;
			}
				
		}
		
		return newpred;
	}
	
	@Override
	public ArrayList<Double[]> wrapper(ArrayList<Double[]> predict_list, ArrayList<Double[]> target_list, EvolutionState state, int thread, GPProblem problem) {
		
		final int MAX_SAMPLE = wrap_max_sample;
		
		double [][] predict = null;
		double [] target = null;
		
		int [] indices = null;
		
		if(predict_list.size() > MAX_SAMPLE) {
			predict = new double [MAX_SAMPLE][predict_list.get(0).length]; //the predict of the current program
			target = new double [MAX_SAMPLE]; //since we linearly regress the output "column-by-column" (target-by-target)
			indices = new int [MAX_SAMPLE]; //the indices of selected samples 
			
			for(int s = 0; s<MAX_SAMPLE; s++) {
				int ind = state.random[thread].nextInt(predict_list.size());
				indices[s] = ind;
				for(int j = 0; j<predict[0].length; j++) {
					predict[s][j] = predict_list.get(ind)[j];
				}
			}
		}
		else {
			predict = new double [predict_list.size()][predict_list.get(0).length]; //the predict of the current program
			target = new double [target_list.size()]; //since we linearly regress the output "column-by-column" (target-by-target)
			indices = new int [predict_list.size()];
			
			//initialize (transform) the predict
			for(int i = 0; i<predict.length; i++) {
				indices[i] = i;
				for(int j = 0; j<predict[0].length; j++) {
					predict[i][j] = predict_list.get(i)[j];
				}
			}
		} 
		
		
		
		//predict: 0-axis: instances  1-axis: output registers
		//target: the target values of all the instances
		LinearRegression lr = new LinearRegression();
		
		//for each time, we only wrap one output register. Only the first-targetNumber output registers are wrapped.
		wraplist.clear();
        for(int tar = 0; tar<target_list.get(0).length; tar++) {
        	
        	//prepare the target
        	for(int i = 0; i<predict.length; i++) {
        		target[i] = target_list.get(indices[i])[tar];
        	}
        	
        	lr.fit(predict, target);
            
            double [] W = lr.getWeights();
            
            weight_norm = 0;
            if(normalize_wrap) {
            	for(int w = 1; w<W.length; w++) {
                	weight_norm += Math.abs(W[w]);
                }
                weight_norm = Math.sqrt(weight_norm/W.length) * normalize_f;
            }
            
            		
            GPTreeStruct instr = constructInstr(outputRegister[tar], W);
            wraplist.add(instr);
            
            //update predict
        	for(int i = 0; i<target_list.size(); i++) {
        		double tmp =  W[0];
        		for(int j = 0; j<predict_list.get(0).length; j++) {
        			tmp += W[j+1] * predict_list.get(i)[j];
        		}
        		
        		if(tmp > 1e6) {
        			tmp = 1e6;
        		}
        		if(tmp < -1e6) {
        			tmp = -1e6;
        		}
        		
        		predict_list.get(i)[tar] = tmp;
        	}
        }
        
        ArrayList<Double[]> newpred = new ArrayList<>();
        for(int i = 0;i<target_list.size(); i++	) {
        	Double [] tmp = new Double [target_list.get(0).length];
        	for(int tar = 0; tar< target_list.get(0).length; tar++) {
        		tmp [tar] = predict_list.get(i)[tar];
        	}
        	newpred.add(tmp);
        }
  		
  		return newpred;
	}
	
	public Object getWrapper() {
		return wraplist;
	}
	
	protected GPTreeStruct constructInstr(int OutIndex, double []W){
		
		GPTreeStruct cand = new GPTreeStruct();
		
		WriteRegisterGPNode desReg = new WriteRegisterGPNode();
		desReg.setIndex(OutIndex);
		desReg.argposition = (byte) 0;
		GPNode N = cand.child = desReg;
		N.children = new GPNode [1];
		
		for(int r = 0; r<=outputRegister.length; r++) {
			GPNode n = Add_Mul_Coef_R(W, r);
			
			N.children[0] = n;
			
			N = n;
		}
		
		return cand;
	}
	
	private GPNode Add_Mul_Coef_R(double [] W, int index) {
		//index: the index of outputRegister[]
		
		if(index < W.length-1) {
			ConstantGPNode A = new ConstantGPNode();
			A.setValue(W[index+1]);
			A.argposition = (byte) 0;
			
			ReadRegisterGPNode srcReg = new ReadRegisterGPNode();
			srcReg.setIndex(outputRegister[index]);
			srcReg.argposition = (byte) 0;
			
			GPNode n = new Add();
			
			n.children = new GPNode[2];
			n.children[1] = new Mul();
			
			n.children[1].children = new GPNode[2];
			n.children[1].children[0] = A;
			n.children[1].children[1] = srcReg;
			
			return n;
		}
		else { //index == W.length - 1
			ConstantGPNode A = new ConstantGPNode();
			A.setValue(W[0]);
			A.argposition = (byte) 0;
			
			return A;
		}
	}
	
	public String makeGraphvizInstr(int instr_index, Set<String> Inputs, String usedTerminals[], Set<Integer> notUsed, AtomicInteger cntindex)
    {
		//return the Graphviz string of an instruction
		//instr_index: index of an instruction,  Inputs: input nodes, 
		//userTerminals: the initial values of registers, cntindex: the index of graph nodes
		
		String connection = "";
		
		GPTree tree = getTreeStruct(instr_index);
		
		if(notUsed.contains((((WriteRegisterGPNode) tree.child)).getIndex())){
			connection += "R" +  (((WriteRegisterGPNode) tree.child)).getIndex() +"[shape=box];\n";
			connection += "R" +  (((WriteRegisterGPNode) tree.child)).getIndex() + "->" + instr_index + ";\n";
			notUsed.remove((Integer)(((WriteRegisterGPNode) tree.child)).getIndex());
		}
		
		for(int c = 0;c<tree.child.children[0].children.length; c++) {
			connection += makeGraphvizSubInstr(tree.child.children[0].children[c], instr_index, c,
					Inputs, usedTerminals, ""+instr_index, cntindex);
		}
	   
		return connection;
    }
	
	protected String makeGraphvizSubInstr(GPNode node, int instr_index, int child_index, Set<String> Inputs, 
			String usedTerminals[], String parentLabel, AtomicInteger cntindex) {
		
		//if it is a register,  backward visit to connect parent with the instruction index
		//if it is an input, specify the node and connect with parent.
		//if it is a function,  specify, connect with parent, and recursively call this makeGraphvizSubInstr(...)
		
		String res = "";
		if(node instanceof InputFeatureGPNode || node instanceof AttributeGPNode){
			//check whether it has been here
			if(!Inputs.contains(node.toString())){
				Inputs.add(node.toString());
				res += node.toString()+"[shape=box];\n";
			}
			res += "" + parentLabel+ "->"+node.toString()+"[label=\"" + child_index +"\"];\n";
			return res;
		}
		
		if(node instanceof Entity && node.expectedChildren() == 0) {
			if(!Inputs.contains(((Entity)node).toGraphvizString())){
				Inputs.add(((Entity)node).toGraphvizString());
				res += "\"" + ((Entity)node).toGraphvizString()+"\"[shape=box];\n";
			}
			res += "" + parentLabel + "->"+"\"" + ((Entity)node).toGraphvizString()+"\""+"[label=\"" + child_index +"\"];\n";
			return res;
		}
		
		//if it is a register
		if(node instanceof ReadRegisterGPNode){
			
			int j = instr_index-1;
			for(;j>=0;j--){
				
				GPTreeStruct visit = getTreelist().get(j);
				
				if(!visit.status) continue;
				
				if((((WriteRegisterGPNode) visit.child)).getIndex() == ((ReadRegisterGPNode)node).getIndex()){
					res += "" + parentLabel + "->" + j + "[label=\"" + child_index +"\"];\n";
					break;
				}
				
			}
			//if there is still source registers, connect the instruction with initial values
			if(j<0) {
				int t = ((ReadRegisterGPNode)node).getIndex();
				if(!Inputs.contains(usedTerminals[t])) {
					Inputs.add(usedTerminals[t]);
					res += usedTerminals[t]+"[shape=box];\n";
				}
				
				res += "" + parentLabel + "->" + usedTerminals[t] + "[label=\"" + child_index +"\"];\n";
			}
			return res;
		}
		
		//function
		String label = cntindex.toString();
		cntindex.set(cntindex.intValue() + 1);
		res += "" + label + "[label=\"" + node.toGraphvizString() + "\"];\n";
		res += parentLabel + "->" + label + "[label=\"" + child_index +"\"];\n";
		 for(int x = 0; x < node.children.length; x++)
	     {	        
	        res += makeGraphvizSubInstr(node.children[x], instr_index, x, 
	        		Inputs, usedTerminals, label, cntindex);
	     }
	    return res;
	}
	
	
}
