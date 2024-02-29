package zhixing.djss.individual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

//import com.sun.xml.internal.bind.v2.runtime.output.C14nXmlOutput;

import ec.EvolutionState;
import ec.Fitness;
import ec.Problem;
import ec.gp.*;
import ec.multiobjective.MultiObjectiveFitness;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.djss.individual.primitive.ReadConstantRegisterGPNode;
import zhixing.djss.jobshop.SchedulingSet4Ind;
import zhixing.djss.simulation.Simulation4Ind;
import yimei.jss.gp.CalcPriorityProblem;
import yimei.jss.gp.data.DoubleData;
import yimei.jss.gp.terminal.AttributeGPNode;
import yimei.jss.gp.terminal.JobShopAttribute;
import yimei.jss.gp.terminal.TerminalERC;
import yimei.jss.jobshop.Objective;
import yimei.jss.jobshop.Operation;
import yimei.jss.jobshop.SchedulingSet;
import yimei.jss.jobshop.WorkCenter;
import yimei.jss.rule.AbstractRule;
import yimei.jss.simulation.DecisionSituation;
import yimei.jss.simulation.Simulation;
import yimei.jss.simulation.state.SystemState;
import yimei.util.lisp.LispParser;

public class LGPIndividual4DJSS extends LGPIndividual implements LGPInterface4DJSS{
//
//	private static final String P_NUMREGISTERS = "numregisters";
//	private static final String P_MAXNUMTREES = "maxnumtrees";
//	private static final String P_MINNUMTREES = "minnumtrees";
//	
//	private static final String P_INITMAXNUMTREES = "init_maxnumtrees";
//	private static final String P_INITMINNUMTREES = "init_minnumtrees";
//	
//	private static final String P_RATEFLOWOPERATOR = "rate_flowoperator";
//	private static final String P_MAXITERTIMES = "max_itertimes";
//	
//	private static final String P_NUMOUTPUTREGISTERS = "num-output-register";
//	private static final String P_OUTPUTREGISTER = "output-register";
//	
//	private static final String P_EFFECTIVE_INITIAL = "effective_initial";
//	
//	protected Parameter privateParameter;
//	
//	protected int MaxNumTrees;
//	protected int MinNumTrees;
//	
//	protected int initMaxNumTrees;
//	protected int initMinNumTrees;
//	
//	protected int numRegs;
//	protected int numOutputRegs;
//	
//	protected double rateFlowOperator;
//	protected int maxIterTimes;
//	
//	protected boolean eff_initialize;
//	
//	protected double registers [] = null;
//	//protected double constant_registers[] = null;
//	
//	protected ArrayList<GPTreeStruct> treelist;
//	
//	protected LGPFlowController flowctrl;
//	
//	protected int [] outputRegister;
//	
//	protected int fastFlag;
//	protected ArrayList<GPTreeStruct> exec_trees;
//	protected int initReg[] = null;
//	protected int init_ConReg[] = null;
//	protected GPNode initReg_values[] = null;
//	
//	protected byte constraintsNum = 0;
//	
//	@Override
//	public void setup(final EvolutionState state, final Parameter base) {
//		//set up the individual prototype 
//		 super.setup(state,base); 
//		 Parameter def = defaultBase();
//	     
//		 // set my evaluation to false
//		 evaluated = false;
//	    
//		 // the maximum/minimum number of trees
//	     MaxNumTrees = state.parameters.getInt(base.push(P_MAXNUMTREES),def.push(P_MAXNUMTREES),1);  // at least 1 tree for GP!
//	     if (MaxNumTrees <= 0) 
//	         state.output.fatal("An LGPIndividual must have at least one tree.",
//	             base.push(P_MAXNUMTREES),def.push(P_MAXNUMTREES));
//	     
//	     MinNumTrees = state.parameters.getInt(base.push(P_MINNUMTREES),def.push(P_MINNUMTREES),1);  // at least 1 tree for GP!
//	     if (MinNumTrees <= 0) 
//	         state.output.fatal("An LGPIndividual must have at least one tree.",
//	             base.push(P_MINNUMTREES),def.push(P_MINNUMTREES));
//	     
//	     initMaxNumTrees = state.parameters.getInt(base.push(P_INITMAXNUMTREES),def.push(P_INITMAXNUMTREES),1);  // at least 1 tree for GP!
//	     if (MaxNumTrees <= 0) 
//	         state.output.fatal("An LGPIndividual must have at least one tree.",
//	             base.push(P_INITMAXNUMTREES),def.push(P_INITMAXNUMTREES));
//	     
//	     initMinNumTrees = state.parameters.getInt(base.push(P_INITMINNUMTREES),def.push(P_INITMINNUMTREES),MinNumTrees);  // at least 1 tree for GP!
//	     if (MinNumTrees <= 0) 
//	         state.output.fatal("An LGPIndividual must have at least one tree.",
//	             base.push(P_INITMINNUMTREES),def.push(P_INITMINNUMTREES));
//	     
//	     numRegs = state.parameters.getInt(base.push(P_NUMREGISTERS),def.push(P_NUMREGISTERS),1);  // at least 1 register for GP!
//	     //numRegs = ((GPInitializer)state.initializer).treeConstraints[0].functionset.registers[0].length;  
//	     		//first 0 index: index of tree constraints, second 0 index: index of register design
//	     if (numRegs <= 0) 
//	         state.output.fatal("An LGPIndividual must have at least one register.",
//	             base.push(P_NUMREGISTERS),def.push(P_NUMREGISTERS));
//	     
//	     rateFlowOperator = state.parameters.getDoubleWithDefault(base.push(P_RATEFLOWOPERATOR), def.push(P_RATEFLOWOPERATOR), 0.);
//	     if(rateFlowOperator < 0 || rateFlowOperator > 1){
//	    	 state.output.fatal("the rate of flow operator must be >=0 and <=1.",
//	    			 base.push(P_RATEFLOWOPERATOR), def.push(P_RATEFLOWOPERATOR));
//	     }
//	     
//	     maxIterTimes = state.parameters.getIntWithDefault(base.push(P_MAXITERTIMES), def.push(P_MAXITERTIMES), 100);
//	     if(maxIterTimes <=0){
//	    	 state.output.fatal("max iteration times must be >=1", base.push(P_MAXITERTIMES), def.push(P_MAXITERTIMES));
//	     }
//	     
//	     eff_initialize = state.parameters.getBoolean(base.push(P_EFFECTIVE_INITIAL), def.push(P_EFFECTIVE_INITIAL), false);
//	     
//	     numOutputRegs = state.parameters.getIntWithDefault(base.push(P_NUMOUTPUTREGISTERS),def.push(P_NUMOUTPUTREGISTERS),1);
//	     if (numOutputRegs <= 0) 
//	         state.output.fatal("An LGPIndividual must have at least one output register.",
//	             base.push(P_NUMOUTPUTREGISTERS),def.push(P_NUMOUTPUTREGISTERS));
//	     outputRegister = new int[numOutputRegs];
//	     for(int r = 0; r<numOutputRegs; r++){
//	    	 Parameter b = base.push(P_OUTPUTREGISTER).push("" + r);
//	            
//            int reg = state.parameters.getIntWithDefault(b, null, 0);
//            if(reg < 0 ){
//            	System.err.println("ERROR:");
//                System.err.println("output register must be >= 0.");
//                System.exit(1);
//            }
//            outputRegister[r] = reg;
//	     }
//	     
//	     // load the trees
//	     treelist = new ArrayList<>();
//	     exec_trees = new ArrayList<>();
//
//	     for (int x=0;x<MaxNumTrees;x++)
//         {
//            Parameter p = base.push(P_TREE).push(""+0);
//            privateParameter = p;
//            GPTreeStruct t = (GPTreeStruct)(state.parameters.getInstanceForParameterEq(
//                    p,def.push(P_TREE).push(""+0),GPTreeStruct.class));
//            t.owner = this; 
//            t.status = false;
//            t.effRegisters = new HashSet<Integer>(0);
//            t.setup(state,p);
//            treelist.add(t);
//            
//            constraintsNum = t.constraints;
//         }
//	     
//	     //initialize registers
//	     registers = new double [numRegs];
//	     //resetRegisters();
//	     //constant_registers = new double [JobShopAttribute.values().length];
//	     
//	     //flow controller
//	     flowctrl = new LGPFlowController();
//	     flowctrl.maxIterTimes = maxIterTimes;
//	     
//	     // now that our function sets are all associated with trees,
//        // give the nodes a chance to determine whether or not this is
//        // going to work for them (especially the ADFs).
//        GPInitializer initializer = ((GPInitializer)state.initializer);
//        int x = 0;
//        for (GPTreeStruct tree: treelist)
//            {
//            for(int w = 0;w < tree.constraints(initializer).functionset.nodes.length;w++)
//                {
//                GPNode[] gpfi = tree.constraints(initializer).functionset.nodes[w];
//                for (int y = 0;y<gpfi.length;y++)
//                    gpfi[y].checkConstraints(state,x++,this,base);
//                }
//            }
//        // because I promised with checkConstraints(...)
//        state.output.exitIfErrors();
//	}
//	
//	public void adjustTreesLength(EvolutionState state, int thread, int numtrees) {
//		if(numtrees < treelist.size()) {
//			int cnt = treelist.size() - numtrees;
//			for(int i = 0;i<cnt;i++) {
//				int index = state.random[thread].nextInt(treelist.size());
//				treelist.remove(index);
//				
//			}
//		}
//		else if(numtrees > treelist.size()) {
//			int cnt = numtrees - treelist.size();
//			for(int i = 0;i<cnt;i++) {
//				int index = state.random[thread].nextInt(treelist.size());
//				GPTreeStruct newtGpTree = (GPTreeStruct)(state.parameters.getInstanceForParameterEq(
//	                    privateParameter,defaultBase().push(P_TREE).push(""+0),GPTreeStruct.class));
//				newtGpTree.owner = this;
//				newtGpTree.status = false;
//				newtGpTree.effRegisters = new HashSet<>(0);
//				newtGpTree.setup(state, privateParameter);
//				treelist.add(index, newtGpTree);
//			}
//		}
//		
//		updateStatus();
//	}
//	
//	public void rebuildIndividual(EvolutionState state, int thread) {
//		int numtrees = state.random[thread].nextInt(initMaxNumTrees - initMinNumTrees + 1) + initMinNumTrees;
//		
//		treelist.clear();
//		
//		for(int i =0;i<numtrees;i++){
//			//GPTreeStruct tree = new GPTreeStruct();
//			GPTreeStruct tree = (GPTreeStruct)(state.parameters.getInstanceForParameterEq(
//                    privateParameter,defaultBase().push(P_TREE).push(""+0),GPTreeStruct.class));
//			tree.constraints = this.constraintsNum;
//			tree.buildTree(state, thread);
//			treelist.add(tree);
//		}
//		
//		
//		updateStatus();
//		
//		if(eff_initialize){//if we have to ensure all the instructions are effective in the initialization
//			this.removeIneffectiveInstr();
//			int trial = 100*this.initMaxNumTrees;
//			while(countStatus()<numtrees && trial>0){
//				GPTreeStruct tree = (GPTreeStruct)(state.parameters.getInstanceForParameterEq(
//	                    privateParameter,defaultBase().push(P_TREE).push(""+0),GPTreeStruct.class));
//				tree.constraints = this.constraintsNum;
//				tree.buildTree(state, thread);
//				treelist.add(0,tree);
//				updateStatus();
//				this.removeIneffectiveInstr();
//				trial --;
//			}
//		}
//
//	}
//	
//	public double[] getRegisters(){
//		return registers;
//	}
//	
//	public double getRegisters(int i){
//		return registers[i];
//	}
//	
////	public double[] getConstantRegisters(){
////		return constant_registers;
////	}
//	
//	public int[] getOutputRegisters(){
//		return outputRegister;
//	}
//	
//	public void setOutputRegisters(int[] tar){
//		outputRegister = new int [tar.length];
//		int i = 0;
//		for(int t : tar){
//			outputRegister[i] = t;
//			i++;
//		}
//	}
//	
//	public void resetIndividual(int numReg, int maxIterTime) {
//		List<Integer> tmp = new ArrayList<>();
//	     tmp.add(0);
//	     resetIndividual(numReg, maxIterTime, tmp);
//	}
//	
//	public void resetIndividual(int numReg, int maxIterTime, List<Integer> outReg){
//		//numReg: the maximum number of registers,  maxIterTime: the maximum iteration time of loop structures,  outReg: output register
//		numRegs = numReg;
//		 maxIterTimes = maxIterTime;
//		 // set my evaluation to false
//		 evaluated = false;
//		
//		 //initialize registers
//	     registers = new double [numRegs];
//	     //resetRegisters();
//	     
//	     //flow controller
//	     flowctrl = new LGPFlowController();
//	     flowctrl.maxIterTimes = maxIterTimes;
//	     
//	     // load the trees
//	     treelist = new ArrayList<>();
//	     
//	     outputRegister = new int[outReg.size()]; //by default, only one output register and the first register is the output
//	     for(int i=0;i<outReg.size();i++){
//	    	 outputRegister[i] = outReg.get(i);
//	     }
//	     
//	}
//	
//	public void setRegister(int ind, double value){
//		registers[ind] = value;
//	}
//	
//	public static void resetRegisters(final Problem problem, final LGPIndividual4DJSS ind){
//		resetRegisters(problem, 1, ind);
//	}
	
//	@Override
//	public void resetRegisters(final Problem problem, double val, final LGPIndividual ind){
//
//		DoubleData tmp = new DoubleData();
//		
//		for(int i = 0;i<ind.getNumRegs();i++){
//			if(ind.getInitReg()[i] == 1){
////				JobShopAttribute a = list[i];
////				(new AttributeGPNode(a)).eval(null, 0, tmp, null, this, problem);
//				ind.getInitReg_values()[i].eval(null, 0, tmp, null, ind, problem);
//				ind.setRegister(i, tmp.value);
//			}
//			else{
//				ind.setRegister(i, val);
//			}
//		}
//	}
//	
//	public int getMaxNumTrees(){
//		return MaxNumTrees;
//	}
//	
//	public int getMinNumTrees(){
//		return MinNumTrees;
//	}
//	
//	public int getInitMaxNumTrees(){
//		return initMaxNumTrees;
//	}
//	
//	public int getInitMinNumTrees(){
//		return initMinNumTrees;
//	}
//	
//	public FlowController getFlowController() {
//		return flowctrl;
//	}
//	
//	public double getrateFlowOperator() {
//		return rateFlowOperator;
//	}
//	
//	public  boolean equals(Object ind){
//		if (ind == null) return false;
//        if (!(this.getClass().equals(ind.getClass()))) return false;  // LGPIndividuals are special.
//        LGPIndividual4DJSS i = (LGPIndividual4DJSS)ind;
//        if (treelist.size() != i.treelist.size()) return false;
//        // this default version works fine for most GPIndividuals.
//        for(int x=0;x<treelist.size();x++)
//            if (!(treelist.get(x).treeEquals(i.treelist.get(x)))) return false;
//
//        return true;
//	}
//	
	
//	public  int hashCode(){
//		// stolen from GPNode.  It's a decent algorithm.
//        int hash = this.getClass().hashCode();
//        
//        for(int x=0;x<treelist.size();x++)
//            hash =
//                // Rotate hash and XOR
//                (hash << 1 | hash >>> 31 ) ^
//                treelist.get(x).treeHashCode();
//        return hash;
//	}
//	
//	
//	public  void verify(EvolutionState state){
//		if (!(state.initializer instanceof GPInitializer))
//        { state.output.error("Initializer is not a CpxGPInitializer"); return; }
//        
//	    // GPInitializer initializer = (GPInitializer)(state.initializer);
//	
//	    if (treelist==null) 
//	        { state.output.error("Null trees in CpxGPIndividual."); return; }
//	    int x = 0;
//	    for(GPTreeStruct tree: treelist) {
//	    	if (tree==null) 
//	        { state.output.error("Null tree (#"+x+") in CpxGPIndividual."); return; }
//	    	x++;
//	    }
//	    for(GPTreeStruct tree: treelist)
//	        tree.verify(state);
//	    state.output.exitIfErrors();
//	}
//	
//	
//	public  void printTrees(final EvolutionState state, final int log){
//		int x = 0;
//		for(GPTreeStruct tree: treelist)
//        {
//			if(!tree.status) {
//				state.output.print("//", log);
//			}
//	        state.output.print("Ins " + x + ":\t",log);
//	        if(tree.type == GPTreeStruct.ARITHMETIC) {
//	        	 tree.printTreeForHumans(state,log);
//	        }
//	        else {
//	        	//it is flow control instruction
//	        	tree.child.children[0].printRootedTree(state, log, Output.V_VERBOSE);
//	        	state.output.println("",log);
//	        }
//	        x++;
//        }
//	}
//	
//	
//	public  void printIndividualForHumans(final EvolutionState state, final int log){
//		state.output.println(EVALUATED_PREAMBLE + (evaluated ? "true" : "false"), log);
//        fitness.printFitnessForHumans(state,log);
//        printTrees(state,log);
//        int cnteff = countStatus();
//        state.output.println("# Effective instructions:\t"+cnteff+"\teffective %:\t"+((double)cnteff)/treelist.size()*100, log);
//	}
//	
//	
//	public  void printIndividual(final EvolutionState state, final int log){
//		state.output.println(EVALUATED_PREAMBLE + Code.encode(evaluated), log);
//        fitness.printFitness(state,log);
//        int x = 0;
//        for(GPTreeStruct tree : treelist)
//            {
//            state.output.println("Ins " + x + ":",log);
//            tree.printTree(state,log);
//            x++;
//            }   
//	}
//	
//	
//	public  void printIndividual(final EvolutionState state, final PrintWriter writer){
//		writer.println(EVALUATED_PREAMBLE + Code.encode(evaluated));
//        fitness.printFitness(state,writer);
//        int x = 0;
//        for(GPTreeStruct tree:treelist)
//            {
//            writer.println("Ins " + x + ":");
//            tree.printTree(state,writer);
//            x++;
//            }   
//	}
//	
//	/** Overridden for the GPIndividual genotype. */
//	
//    public  void writeGenotype(final EvolutionState state,
//        final DataOutput dataOutput) throws IOException
//    {
//	    dataOutput.writeInt(treelist.size());
//	    for(GPTreeStruct tree : treelist)
//	        tree.writeTree(state,dataOutput);
//    }
//
//    /** Overridden for the GPIndividual genotype. */
//	
//    public  void readGenotype(final EvolutionState state,
//        final DataInput dataInput)throws IOException
//    {
//	    int treelength = dataInput.readInt();
//	    if(treelength > MaxNumTrees || treelength < MinNumTrees) {
//	    	state.output.fatal("Number of trees is inconsistent with the given Max / Min NumTrees.");
//	    }
//	    if (treelist == null)
//	        state.output.fatal("null trees collections!");
//	    
//	    adjustTreesLength(state, 0, treelength);
//	    
//	    for(int x=0;x<treelist.size();x++)
//	        treelist.get(x).readTree(state,dataInput);
//    }
//
//	
//    public  void parseGenotype(final EvolutionState state,
//        final LineNumberReader reader)throws IOException
//    {
//    	//suppose the tree in readLine has a same number of trees with the individual 
//	    // Read my trees
//	    for(int x=0;x<treelist.size();x++)
//	        {
//	        reader.readLine();  // throw it away -- it's the tree indicator
//	        treelist.get(x).readTree(state,reader);
//	        }
//    }
//	
//	@Override
//	public  Object clone(){
//		return super.clone();
//	}
//	
//	@Override
//	protected void copyLGPproperties(LGPIndividual obj){
//		super.copyLGPproperties(obj);
//	}
//	
//	@Override
//	public  LGPIndividual lightClone(){
//		return super.lightClone();
//	}
	
//	public double priority(Operation op, WorkCenter workCenter,
//            SystemState systemState) {
//		//it is used in Job Shop Scheduling, to prioritize the operations in a machine queue
//		CalcPriorityProblem calcPrioProb =
//		 new CalcPriorityProblem(op, workCenter, systemState);
//		
//		DoubleData tmp = new DoubleData();
//		
//		return execute(null, 0, tmp, null, this, calcPrioProb);
//	}
	
//	public double execute(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
//		//execute the whole individual. the fastFlag means that we will not consider the flow of instructions, just execute instructions one-by-one
//		//the fastFlag should be set true only when there is not flow control operators in the primitive set.
//		
//		this.resetRegisters(problem, 1, (LGPIndividual4DJSS) individual); //initialize registers by input features (or the default value)
//		
//		if(getFastFlag() == 1){
//			DoubleData rd = ((DoubleData)(input));
//			for(GPTreeStruct tree : exec_trees) {
//				tree.child.eval(state, thread, input, stack, individual, problem);
//		}
//		}
//		else{
//			getFlowctrl().execute(state, thread, input, stack, (CpxGPIndividual)individual, problem);
//		}
//			
//		
//		return registers[getOutputRegister()[0]]; //only the first output register will be used in basic LGP
//	}
	
//	@Override
//	public GPTree getTree(int index){
//		if(index >= treelist.size()){
//    		System.out.println("The tree index " + index + " is out of range");
//    		System.exit(1);
//    	}
//    	return treelist.get(index);
//	}
//	
//	public GPTreeStruct getTreeStruct(int index) {
//		if(index >= treelist.size()){
//    		System.out.println("The tree index " + index + " is out of range");
//    		System.exit(1);
//    	}
//    	return treelist.get(index);
//	}
//	
//	public List<GPTreeStruct> getTreeStructs(){
//		return treelist;
//	}
//	
//	@Override
//	public boolean setTree(int index, GPTree tree){
//		if(index < treelist.size()){
//			treelist.remove(index);
//			GPTreeStruct treeStr = (GPTreeStruct)tree;
//			if(!(tree instanceof GPTreeStruct)) {
//				treeStr.status = false;
//				treeStr.effRegisters = new HashSet<>(0);
//			}
//			treeStr.type = GPTreeStruct.ARITHMETIC;
//			if(treeStr.child.children[0] instanceof FlowOperator){
//				if(treeStr.child.children[0] instanceof Branching){
//					treeStr.type = GPTreeStruct.BRANCHING;
//				}
//				else{
//					treeStr.type = GPTreeStruct.ITERATION;
//				}
//			}
//			treelist.add(index, treeStr);
//			
//			this.evaluated = false;
//			
//			updateStatus();
//			
//			return true;
//		}
//		System.out.println("setTree index: " + index + " is out of range " + treelist.size());
//		return false;
//	}
//	
//	public void addTree(int index, GPTree tree){
//		//add "tree" to the index slot
//		GPTreeStruct treeStr;
//		if(tree instanceof GPTreeStruct){
//			treeStr = (GPTreeStruct) tree;
//		}
//		else{
//			treeStr = new GPTreeStruct();
//			treeStr.assignfrom(tree);
//			treeStr.status = false;
//			treeStr.effRegisters = new HashSet<>(0);
//		}
//		
//		treeStr.type = GPTreeStruct.ARITHMETIC;
//		if(treeStr.child.children[0] instanceof FlowOperator){
//			if(treeStr.child.children[0] instanceof Branching){
//				treeStr.type = GPTreeStruct.BRANCHING;
//			}
//			else{
//				treeStr.type = GPTreeStruct.ITERATION;
//			}
//		}
//		if(index < 0) index = 0;
//		if(index < treelist.size()) {
//			treelist.add(index, treeStr);
//		}
//		else {
//			treelist.add(treeStr);
//		}
//		
//		this.evaluated = false;
//		
//		updateStatus();
//	}
//	
//	public boolean removeTree(int index) {
//		if(index < treelist.size()){
//			treelist.remove(index);
//			
//			this.evaluated = false;
//			
//			updateStatus();
//			
//			return true;
//		}
//		System.out.println("removeTree index: " + index + " is out of range " + treelist.size());
//		return false;
//	}
//	
//	public boolean removeIneffectiveInstr(){
//		for(int ii = 0;ii<this.getTreesLength();ii++) {
//			if(!this.getTreeStruct(ii).status && this.getTreesLength()>this.getMinNumTrees()) {
//				this.removeTree(ii);
//				ii--; //ii remain no change, so that it can point to the next tree
//			}
//		}
//		return true;
//	}
//	
//	@Override
//	public int getTreesLength(){
//		return treelist.size();
//	}
//	
//	public int getEffTreesLength(){
//		updateStatus();
//		int res = 0;
//		for(GPTreeStruct tree : treelist){
//			if (tree.status){
//				res ++;
//			}
//		}
//		
//		return res;
//	}
	
//	public void updateStatus(int n, int []tar) {
//		//identify which instructions are extrons and vise versa
//		//start to update the status from position n
//		//tar: the output register
//		
//		boolean statusArray [] = new boolean [trees.size()];
//		boolean sourceArray [][] = new boolean [trees.size()][numRegs];
//		boolean destinationArray [][] = new boolean [trees.size()][numRegs];
//		
//		Set<Integer> source, destination;
//		
//		if(n > trees.size()) {
//			System.out.println("The n in updateStatus is larger than existing tree list");
//			System.exit(1);
//		}
//		
//		ListIterator<GPTreeStruct> it = trees.listIterator();
//		int cn = n;
//		while(it.hasNext() && cn > 0) {
//			it.next();
//			cn --;
//		}
//		
//		//initialize target effective registers
//		Set<Integer> targetRegister = new HashSet<>(0);
//		for(int i = 0; i<tar.length; i++)
//			targetRegister.add(tar[i]);
//		
//		//backward loop
//		cn = n - 1;  //serve as index
//		while(it.hasPrevious()) {
//			GPTreeStruct tree = (GPTreeStruct) it.previous();
//			
//			tree.effRegisters = new HashSet<>(0);
//			Iterator<Integer> itt = targetRegister.iterator();
//        	while(itt.hasNext()) {
//        		int v = itt.next();
//        		tree.effRegisters.add(v);
//        	}
//			
//			//check it is effective
//			if(tree.child.children[0] instanceof Branching){
//				//branching. if its body contain effective instruction, it is effective too.
//				tree.type = 1;
//				tree.status = statusArray[cn] = false;
//				for(int i = cn + 1; i < trees.size() && i <= ((FlowOperator)tree.child.children[0]).getBodyLength()+cn;i++){
//					if(statusArray[i]){
//						tree.status = statusArray[cn] = true;
//						tree.updateEffRegister(targetRegister);
//						break;
//					}
//				}
//				collectReadRegister(tree.child, sourceArray[cn]);
//			}
//			else if (tree.child.children[0] instanceof Iteration){
//				//iteration. if the effective instructions in its body have a nonempty intersection set
//				//of source and destination register
//				tree.type = 2;
//				source = new HashSet<>();
//				destination = new HashSet<>();
//				
//				//collect arithmetic instruction and their source and destination register
////				int j = 0;
//				boolean effective_block_exist = false;
//				for(int i = cn + 1; i < (this.trees).size() 
//						&& i <= ((FlowOperator)tree.child.children[0]).getBodyLength()+cn; i++){
//					if(statusArray[i] && !(trees.get(i).child.children[0] instanceof FlowOperator) 
////							&& j < ((FlowOperator)tree.child.children[0]).getBodyLength()
//							){
//
//						for(int r = 0; r<numRegs; r++){
//							if(sourceArray[i][r]) source.add(r);
//							if(destinationArray[i][r]) destination.add(r);
//						}
////						j++;
////						if(j>=((FlowOperator)tree.child.children[0]).getBodyLength()) break;
//					}
//					else {
//						//check the nested program block. we don't explicitly check the instructions in the nest program block
//						//as long as the nested one is effective, we simply see the outter program block is effective too.
//						if(statusArray[i] && (trees.get(i).child.children[0] instanceof FlowOperator)) {
//							effective_block_exist = true;
//							i += ((FlowOperator)trees.get(i).child.children[0]).getBodyLength() - 1;
//						}
//					}
//				}
//				source.retainAll(destination);
//				if(!source.isEmpty() || effective_block_exist){
//				    tree.status = statusArray[cn]=true;
//				    tree.updateEffRegister(targetRegister);
//				}
//				else {
//					tree.status = statusArray[cn]=false;
//				}
//				collectReadRegister(tree.child, sourceArray[cn]);
//			}
//			else {
//				tree.type = 0;
//				
//				
//				if(targetRegister.contains(((WriteRegisterGPNode) tree.child).getIndex())){
//					tree.status = true;
//					targetRegister.remove(((WriteRegisterGPNode) tree.child).getIndex());
//					tree.updateEffRegister(targetRegister);
//					
//					statusArray[cn] = true;
//				}
//				else {
//					tree.status = false;
//					
//					statusArray[cn] = false;
//				}
//				
//				destinationArray[cn][((WriteRegisterGPNode) tree.child).getIndex()] = true;
//				collectReadRegister(tree.child, sourceArray[cn]);
//			} 
//			cn--;
//		}
//		
//	}
	
//	public void updateStatus(int n, int []tar) {
//		//identify which instructions are extrons and vise versa
//		//start to update the status from position n
//		//tar: the output register
//		
//		boolean statusArray [] = new boolean [treelist.size()];
//		boolean sourceArray [][] = new boolean [treelist.size()][numRegs];
//		boolean destinationArray [][] = new boolean [treelist.size()][numRegs];
//		
//		Set<Integer> source, destination;
//		
//		if(n > treelist.size()) {
//			System.out.println("The n in updateStatus is larger than existing tree list");
//			System.exit(1);
//		}
//		
//		ListIterator<GPTreeStruct> it = treelist.listIterator();
//		int cn = n;
//		while(it.hasNext() && cn > 0) {
//			it.next();
//			cn --;
//		}
//		
//		final int [] output = tar;
//		
//		//initialize target effective registers
//		Set<Integer> targetRegister = new HashSet<>(0);
//		for(int i = 0; i<tar.length; i++)
//			targetRegister.add(tar[i]);
//		
//		//backward loop
//		cn = n - 1;  //serve as index
//		while(it.hasPrevious()) {
//			
//			GPTreeStruct tree = (GPTreeStruct) it.previous();
//			
//			tree.effRegisters = new HashSet<>(0);
//			Iterator<Integer> itt = targetRegister.iterator();
//        	while(itt.hasNext()) {
//        		int v = itt.next();
//        		tree.effRegisters.add(v);
//        	}
//        	
//			
//			//check it is effective
//			if(tree.child.children[0] instanceof Branching){
//				//branching. if its body contain effective instruction, it is effective too.
//				tree.type = GPTreeStruct.BRANCHING;
//				tree.status = statusArray[cn] = false;
//				for(int i = cn + 1; i < treelist.size() && i <= ((FlowOperator)tree.child.children[0]).getBodyLength()+cn;i++){
//					if(statusArray[i]){
//						tree.status = statusArray[cn] = true;
//
//						break;
//					}
//				}
//				
//				//since the flow control body might not be active in the execution,  we need to add the effective registers subsequent to the body.
//				int bodyend = ((FlowOperator)tree.child.children[0]).getBodyLength() + cn;
//				for(int effii = cn + 1; effii<=bodyend && effii < treelist.size(); effii++) {
//					if(treelist.get(effii).child.children[0] instanceof FlowOperator) {
//						bodyend = Math.max(bodyend, ((FlowOperator)treelist.get(effii).child.children[0]).getBodyLength() + effii);
//					}
//				}
//				int effi = bodyend + 1;
//				if(effi >= treelist.size()) {
//					for(int ii = 0; ii<output.length; ii++)
//						targetRegister.add(output[ii]);
//				}
//				else {
//					for(Integer r : treelist.get(effi).effRegisters) {
//						targetRegister.add(r);
//					}
//					if(statusArray[effi]) {
//						if(treelist.get(effi).type == GPTreeStruct.ARITHMETIC) {
//							targetRegister.remove(((WriteRegisterGPNode) treelist.get(effi).child ).getIndex());
//						}
//						treelist.get(effi).updateEffRegister(targetRegister);
//					}
//					
//				}
//				
//				itt = targetRegister.iterator();
//	        	while(itt.hasNext()) {
//	        		int v = itt.next();
//	        		tree.effRegisters.add(v);
//	        	}
//				//====================================
//				
//				if(statusArray[cn] == true) {
//					tree.updateEffRegister(targetRegister); //add the read registers in the branching instruction into targetRegister
//				}
//				
//				collectReadRegister(tree.child, sourceArray[cn]);
//				
//				
//			}
//			else if (tree.child.children[0] instanceof Iteration){
//				//iteration. if the effective instructions in its body have a nonempty intersection set
//				//of source and destination register
//				tree.type = GPTreeStruct.ITERATION;
//				source = new HashSet<>();
//				destination = new HashSet<>();
//				
//				//collect arithmetic instruction and their source and destination register
////				int j = 0;
//				boolean effective_block_exist = false;
//				for(int i = cn + 1; i < (this.treelist).size() 
//						&& i <= ((FlowOperator)tree.child.children[0]).getBodyLength()+cn; i++){
//					if(statusArray[i] && !(treelist.get(i).child.children[0] instanceof FlowOperator) 
////							&& j < ((FlowOperator)tree.child.children[0]).getBodyLength()
//							){
//
//						for(int r = 0; r<numRegs; r++){
//							if(sourceArray[i][r]) source.add(r);
//							if(destinationArray[i][r]) destination.add(r);
//						}
////						j++;
////						if(j>=((FlowOperator)tree.child.children[0]).getBodyLength()) break;
//					}
//					else {
//						//check the nested program block. we don't explicitly check the instructions in the nest program block
//						//as long as the nested one is effective, we simply see the outter program block is effective too.
//						if(statusArray[i] && (treelist.get(i).child.children[0] instanceof FlowOperator)) {
//							effective_block_exist = true;
//							i += ((FlowOperator)treelist.get(i).child.children[0]).getBodyLength() - 1;
//						}
//					}
//				}
//				source.retainAll(destination);
//				
//				//since the flow control body might not be active in the execution,  we need to add the effective registers subsequent to the body.
//				int bodyend = ((FlowOperator)tree.child.children[0]).getBodyLength() + cn;
//				for(int effii = cn + 1; effii<=bodyend && effii < treelist.size(); effii++) {
//					if(treelist.get(effii).child.children[0] instanceof FlowOperator) {
//						bodyend = Math.max(bodyend, ((FlowOperator)treelist.get(effii).child.children[0]).getBodyLength() + effii);
//					}
//				}
//				int effi = bodyend + 1;
//				if(effi >= treelist.size()) {
//					for(int i = 0; i<output.length; i++)
//						targetRegister.add(output[i]);
//				}
//				else {
//					for(Integer r : treelist.get(effi).effRegisters) {
//						targetRegister.add(r);
//					}
//					if(statusArray[effi]) {
//						if(treelist.get(effi).type == GPTreeStruct.ARITHMETIC) {
//							targetRegister.remove(((WriteRegisterGPNode) treelist.get(effi).child ).getIndex());
//						}
//						treelist.get(effi).updateEffRegister(targetRegister);
//					}
//					
//				}
//				
//				itt = targetRegister.iterator();
//	        	while(itt.hasNext()) {
//	        		int v = itt.next();
//	        		tree.effRegisters.add(v);
//	        	}
//				//=======================================
//				
//				if(!source.isEmpty() || effective_block_exist){
//				    tree.status = statusArray[cn]=true;
//				    tree.updateEffRegister(targetRegister);
//				}
//				else {
//					tree.status = statusArray[cn]=false;
//				}
//				collectReadRegister(tree.child, sourceArray[cn]);
//				
//				
//			}
//			else {
//				tree.type = GPTreeStruct.ARITHMETIC;
//				
//				
//				if(targetRegister.contains(((WriteRegisterGPNode) tree.child).getIndex())){
//					tree.status = true;
//					targetRegister.remove(((WriteRegisterGPNode) tree.child).getIndex());
//					tree.updateEffRegister(targetRegister);
//					
//					statusArray[cn] = true;
//				}
//				else {
//					tree.status = false;
//					
//					statusArray[cn] = false;
//				}
//				
//				destinationArray[cn][((WriteRegisterGPNode) tree.child).getIndex()] = true;
//				collectReadRegister(tree.child, sourceArray[cn]);
//			} 
//			cn--;
//		}
//		
//	}
//	
//	public void updateStatus() {
//		updateStatus(treelist.size(),outputRegister);
//	}
	
//	public ArrayList<Integer> getSubGraph(int n, Integer [] tar){
//		//return the indices of the instructions which form the sub-graph.
//		//the sub-graph is searched from index "n", with the target output indicated by "tar".
//		
//		updateStatus();
//		
//		if(n > treelist.size()) {
//			System.out.println("The n in updateStatus is larger than existing tree list");
//			System.exit(1);
//		}
//		
//		ArrayList<Integer> graph = new ArrayList<>();
//		
//		//initialize target effective registers
//		Set<Integer> targetRegister = new HashSet<>(0);
//		for(int i = 0; i<tar.length; i++)
//			targetRegister.add(tar[i]);
//		
//		//backward loop
//		int cn = n;
//		do{
//			GPTreeStruct tree = getTreeStruct(cn);
//			if( tree.status && targetRegister.contains(((WriteRegisterGPNode) tree.child).getIndex())){
//				graph.add(cn);
//				
//				//update targetRegister
//				targetRegister.remove(((WriteRegisterGPNode) tree.child).getIndex());
//				tree.updateEffRegister(targetRegister);
//			}
//			cn --;
//		}while(cn >= 0);
//		
//		return graph;
//		
//	}
//	
//	public ArrayList<Integer> getPartialSubGraph(int n, int [] tar, double rate, final EvolutionState state, final int thread){
//		//return the indices of the instructions which form the sub-graph based on a probability "rate". this function might randomly abandon some graph nodes
//		//the sub-graph is searched from index "n", with the target output indicated by "tar".
//		
//		updateStatus();
//		
//		if(n > treelist.size()) {
//			System.out.println("The n in updateStatus is larger than existing tree list");
//			System.exit(1);
//		}
//		
//		ArrayList<Integer> graph = new ArrayList<>();
//		
//		//initialize target effective registers
//		Set<Integer> targetRegister = new HashSet<>(0);
//		for(int i = 0; i<tar.length; i++)
//			targetRegister.add(tar[i]);
//		
//		//backward loop
//		int cn = n;
//		do{
//			GPTreeStruct tree = getTreeStruct(cn);
//			if( tree.status && targetRegister.contains(((WriteRegisterGPNode) tree.child).getIndex())){
//				graph.add(cn);
//				
//				//update targetRegister
//				targetRegister.remove(((WriteRegisterGPNode) tree.child).getIndex());
//				
//				Set<Integer> s = tree.collectReadRegister();
//				Iterator<Integer> it = s.iterator();
//				while(it.hasNext()){
//					it.next();
//					if(s.size()>1 && state.random[thread].nextDouble()>rate){
//						it.remove();
//					}
//				}
//				
//				for(Integer r : s){
//					targetRegister.add(r);
//				}
//			}
//			cn --;
//		}while(cn >= 0);
//		
//		return graph;
//		
//	}
//	
//	public ArrayList<Integer> getIntersecSubGraph(Set<Integer> tar, int maintarget){
//		ArrayList<Integer> res=null, res_tmp=null, tmp=null;
//		
//		res = this.getSubGraph(this.getTreesLength()-1, new Integer[]{maintarget});
//		
//		for(int output : tar){
//			tmp = this.getSubGraph(this.getTreesLength()-1, new Integer[]{output});
//			if(res_tmp == null){
//				res_tmp = (ArrayList<Integer>) tmp.clone();
//			}
//			else{
//				res_tmp.retainAll(tmp);
//			}
//		}
//		
//		if(res_tmp.size() == 0)
//			return res;
//		else
//			return res_tmp;
//	}
	
//	@Override
//	public ArrayList<Pair<String, ArrayList<String>>> getAdjacencyTable(int start, int end){
//		//return the adjacency table of instruction sequence [start, end), only counts effective instruction
//		if(start<0||start>=end||end>getTreelist().size()){
//			System.err.print("illegal arguments in getAdjacencyTable() of LGPIndividual4Graph\n");
//			System.exit(1);
//		}
//		
//		ArrayList<Pair<String, ArrayList<String>>> res = new ArrayList<>();
//		
//		for(int i = end - 1; i>=start; i--) {
//			//for each instruction, get the index of its function and children/constants
//			GPTreeStruct tree = getTreelist().get(i);
//			
//			if(!tree.status) continue;
//			
////			int si,tj;
////			//get the index of its function
////			String name = tree.child.children[0].toString();
////			tj = primitives.indexOf(name);
//			
//			String check = tree.child.children[0].toString();
//			ArrayList<String> slibings = new ArrayList<>();
//			
//			for(int j = 0; j<tree.child.children[0].expectedChildren(); j++) {
//				
////				if(tree.child.children[0].children[j] instanceof TerminalERC) 
//				if(! (tree.child.children[0].children[j] instanceof ReadRegisterGPNode) 
//						&& tree.child.children[0].children[j].expectedChildren()==0)
//				{
//					slibings.add(tree.child.children[0].children[j].toString());
//				}
//				else {
//					//find the writeRegister whose index is equals to the readRegister
//					slibings.add(null);
//					int k;
//					for(k = i-1; k>=0; k--) {
//						if(((WriteRegisterGPNode)getTreelist().get(k).child).getIndex() == ((ReadRegisterGPNode)tree.child.children[0].children[j]).getIndex()){
//							slibings.set(j, getTreelist().get(k).child.children[0].toString()); //setting null as an entiy may have bugs 
//							break;
//						}
//					}
//					if(k<0){ //find which input feature is used to initialize the register
//						int term_ind = ((ReadRegisterGPNode)tree.child.children[0].children[j]).getIndex();
//						slibings.set(j, JobShopAttribute.relativeAttributes()[term_ind].getName());
//					}
//				}
//
//			}
//			
//			res.add(new Pair<String, ArrayList<String>>(check, slibings));
//			
//			//check whether slibing functions exist in the map
////			for(int j = 0;j<check.expectedChildren(); j++) {
////				
////				int child = -1;
////				
////				if(check.children[j] instanceof TerminalERC) {
////					//store the constant index
////					
////				}
////				else {
////					//find its subsequent function
////					if(!res.keySet().contains(/* the child functions */)) {
////						if(end - 1 > start) {
////							HashMap<GPNode, ArrayList<Integer>> tmp = getAdjacencyTable(start, end - 1);
////						}
////						
////					}
////					
////					child = child 
////				}
////				
////				slibings.set(j, child);
////			}
//			
//			
//		}
//		
//		return res;
//	}
	
//	public ArrayList<Pair<String, ArrayList<String>>> getAdjacencyTable(){
//		return getAdjacencyTable(0, this.getTreesLength());
//	}
//	
//	public int countStatus(){
//		int cnt = 0;
//		for(GPTreeStruct tree:treelist){
//			if(tree.status){
//				cnt++;
//			}
//		}
//		return cnt;
//	}
//	
//	public int countStatus(int start, int end){
//		//return the number of effective instructions in [start, end)
//		int cnt = 0;
//		for(int i = start; i<end; i++){
//			GPTreeStruct tree = treelist.get(i);
//			if(tree.status){
//				cnt++;
//			}
//		}
//		return cnt;
//	}
//	
//	public boolean canAddFlowOperator(){
//		boolean res = true;
//		int cnt = 0;
//		for(GPTreeStruct tree:treelist){
//			if(tree.child.children[0] instanceof FlowOperator){
//				cnt++;
//			}
//		}
//		if(((double)cnt)/treelist.size() > rateFlowOperator) res = false;
//		return res;
//	}
//	
//	protected void collectReadRegister(GPNode node, boolean[] collect){
//		//collect the source registers for an instruction
//		//node: an instruction primitive
//		if(node instanceof ReadRegisterGPNode){
//			collect[((ReadRegisterGPNode) node).getIndex()] = true;
//		}
//		else{
//			if(node.children.length > 0){
//				for(int n =0;n<node.children.length;n++){
//					collectReadRegister(node.children[n], collect);
//				}
//			}
//		}
//		return;
//	}
	
//	@Override
//	public String makeGraphvizRule(List<Integer> outputRegs){
//		//this function is not support the instructions whose depth is larger than 3, also not support "IF" operation since
//		//DAG cannot tell the loop body. If there are more than one operation in one instruction, subgraph of Graphviz should be used
//		
//		//collect terminal names
//		String usedTerminals[] = new String[getNumRegs()];
//		for(int j = 0; j<getNumRegs();) {
//			for (JobShopAttribute a : JobShopAttribute.relativeAttributes()) {
//				
//				usedTerminals[j++] = a.getName();
//
//				if(j>=getNumRegs()) break;
//			}
//		}
//		
//		Set<String> JSSAttributes = new HashSet<>();
//		
//		//check all instructions and specify all effective operations, effective constants 
//		String nodeSpec ="";
//		for(int i = 0;i<getTreelist().size();i++){
//			GPTreeStruct tree = getTreelist().get(i);
//			
//			if(!tree.status) continue;
//			
//			nodeSpec += "" + i + "[label=\"" + tree.child.children[0].toString() + "\"];\n";
//			for(int c = 0;c<tree.child.children[0].children.length; c++){
//				GPNode node = tree.child.children[0].children[c];
//				if(node instanceof AttributeGPNode){
//					//check whether it has been here
//					if(!JSSAttributes.contains(node.toString())){
//						JSSAttributes.add(node.toString());
//						nodeSpec += node.toString()+"[shape=box];\n";
//					}
//					nodeSpec += "" + i + "->"+node.toString()+"[label=\"" + c +"\"];\n";
//				}
//			}
//		}
//		
//		//backward visit all effective instructions, connect the instructions
//		String connection = "";
//		Set<Integer> notUsed = new HashSet<>(outputRegs);
//		for(int i=getTreelist().size()-1;i>=0;i--){
//			GPTreeStruct tree = getTreelist().get(i);
//			
//			if(!tree.status) continue;
//			
//			if(notUsed.contains((((WriteRegisterGPNode) tree.child)).getIndex())){
//				connection += "R" +  (((WriteRegisterGPNode) tree.child)).getIndex() +"[shape=box];\n";
//				connection += "R" +  (((WriteRegisterGPNode) tree.child)).getIndex() + "->" + i + ";\n";
//				notUsed.remove((Integer)(((WriteRegisterGPNode) tree.child)).getIndex());
//			}
//			
//			//find the instructions whose destination register is the same with the source registers for this instruction
//			List<Integer> source = new ArrayList<>();
//			for(int c = 0;c<tree.child.children[0].children.length; c++){
//				GPNode node = tree.child.children[0].children[c];
//				if(node instanceof ReadRegisterGPNode){
//					source.add(((ReadRegisterGPNode)node).getIndex());
//					
//					for(int j = i-1;j>=0;j--){
//						
//						GPTreeStruct visit = getTreelist().get(j);
//						
//						if(!visit.status) continue;
//						
//						while(source.contains((((WriteRegisterGPNode) visit.child)).getIndex())){
//							connection += "" + i + "->" + j + "[label=\"" + c +"\"];\n";
//							source.remove(source.indexOf((((WriteRegisterGPNode) visit.child)).getIndex()));
//						}
//						
//						if(source.size()==0) break;
//					}
//					//if there is still source registers, connect the instruction with JSS attributes
//					for(Integer j : source){
//						connection += usedTerminals[j]+"[shape=box];\n";   // use job shop attributes to initialize registers
//						connection += "" + i + "->" + usedTerminals[j] + "[label=\"" + c +"\"];\n";
////						connection += "1[shape=box];\n";  // use "1" to initialize registers
////						connection += "" + i + "->" + "1[label=\"" + c +"\"];\n";
//					}
//					source.clear();
//				}
//				
//				
//			}
//			
//		}
//		
//		String result = "digraph g {\n" 
//		+"nodesep=0.2;\n"
//		+"ranksep=0;\n"
//		+ "node[fixedsize=true,width=1.3,height=0.6,fontsize=\"30\",fontname=\"times-bold\",style=filled, fillcolor=lightgrey];\n"
//		+"edge[fontsize=\"25.0\",fontname=\"times-bold\"];\n"
//		+ nodeSpec
//		+ connection
//		+ "}\n";
//		
//		return result;
//	}
	
//	
//	public String makeGraphvizRule(){
//		return makeGraphvizRule(null);
//	}
//	
//	public double getMeanEffDepenDistance(){
//		ArrayList<Integer> DependenceDis = new ArrayList<>();
//		
//		
//		
//		for(int i = 0;i<getTreesLength();i++){
//			GPTreeStruct tree = getTreeStruct(i);
//			
//			if(!tree.status || tree.type !=GPTreeStruct.ARITHMETIC) continue;  //ineffective or not arithmetic instruction
//			
//			int j = 1;
//			for(;i+j<getTreesLength();j++){
//				if(!getTreeStruct(i+j).status || getTreeStruct(i+j).type !=GPTreeStruct.ARITHMETIC) continue;  //ineffective or not arithmetic instruction
//				
//				if(getTreeStruct(i+j).collectReadRegister().contains(((WriteRegisterGPNode)tree.child).getIndex())){
//					DependenceDis.add(j);
//					break;
//				}
//			}
//		}
//		
//		if(DependenceDis.size()>0){
//			double res = 0;
//			for(Integer a : DependenceDis){
//				res += a;
//			}
//			return res / DependenceDis.size();
//		}
//		else
//			return 0;
//	}
//	
//	public ArrayList<Double> getEffDegree(){
//		ArrayList<Double> degreeList = new ArrayList<>();
//		
//		//arithmetic instructions
//		for(int i = 0;i<getTreesLength();i++){
//			degreeList.add(0.0);
//			GPTreeStruct tree = getTreeStruct(i);
//			
//			if(!tree.status || tree.type !=GPTreeStruct.ARITHMETIC) continue;  //ineffective or not arithmetic instruction
//			
//			
//			for(int j = 1;i+j<getTreesLength();j++){
//				if(!getTreeStruct(i+j).status || getTreeStruct(i+j).type !=GPTreeStruct.ARITHMETIC) continue;  //ineffective or not arithmetic instruction
//				
//				List<Integer> tmp = getTreeStruct(i+j).collectReadRegister_list();
//				
//				if(tmp.contains(((WriteRegisterGPNode)tree.child).getIndex())){
//					for(Integer t : tmp){
//						if(t == ((WriteRegisterGPNode)tree.child).getIndex()){
//							degreeList.set(i, degreeList.get(i)+1);
//						}
//					}
//				}
//				
//				//check the destination register, if the WriteRegister of instruction i is written, break;
//				if(((WriteRegisterGPNode)getTreeStruct(i+j).child).getIndex() == ((WriteRegisterGPNode)tree.child).getIndex()){
//					break;
//				}
//			}
//		}
//		
//		//branching and iteration instruction, take the average dependence degree in the branched instructions as their dependence degree
//		for(int i = 0;i<getTreesLength();i++){
//			GPTreeStruct tree = getTreeStruct(i);
//			
//			if(!tree.status || tree.type ==GPTreeStruct.ARITHMETIC) continue; 
//			
//			int bodyLength = ((FlowOperator)tree.child.children[0]).getBodyLength();
//			
//			double degree = 0;
//			double cnt = 0;
//			
//			for(int j = 1;j<=bodyLength && i+j < getTreesLength();j++){
//				if(!getTreeStruct(i+j).status) continue;
//				
//				//extend the body length if there is a nested one
//				if(getTreeStruct(i+j).type != GPTreeStruct.ARITHMETIC) 
//					bodyLength += ((FlowOperator)getTreeStruct(i+j).child.children[0]).getBodyLength();
//				else {
//					degree += degreeList.get(i+j);
//					cnt ++;
//				}
//			}
//			
//			degreeList.set(i, degree / cnt);
//			
//		}
//		return degreeList;
//	}
//	
//	public ArrayList<Integer> getNumEffRegister(){
//		ArrayList<Integer> EffRegList = new ArrayList<>();
//		
//		updateStatus();
//		
//		for(GPTreeStruct tree : treelist){
//			EffRegList.add(tree.effRegisters.size());
//		}
//		
//		return EffRegList;
//	}
//	
//	public int getApproximateGraphWidth(int index) {
//		ArrayList<Integer> tmp_list = getNumEffRegister();
//		if(index >= tmp_list.size()) return tmp_list.get(tmp_list.size()-1);
//		if(index < 0) return tmp_list.get(0);
//		
//		return tmp_list.get(index);
//	}
//	
//	public int getEffectiveIndex(int index) {
//		//inputs: instruction index in genome
//		//outputs: the index with only effective instructions. if it is an intron, returns the index of the closest effective instruction with smaller structural index;
//		//if index exceeds the maximum index, return # effective instructions (i.e., the root), if it is smaller than the minimum index, returns 0;
//		if(index < 0) return 0;
//		if(index >= getTreesLength()) return getEffTreesLength()-1;
//		
//		if(!getTreeStruct(index).status) {
//			int i;
//			for(i = index - 1; i>=0; i--) {
//				if(getTreeStruct(i).status) {
//					return getEffectiveIndex(i);
//				}
//			}
//			if(i<0) return 0;
//		}
//		
//		int res = -1;
//		
//		for(int i = 0;i<=index;i++) {
//			if(getTreeStruct(i).status) res++;
//		}
//		
//		return res;
//	}
	
//	@Override
//	public double evaluate(final EvolutionState state,
//	        final int thread,
//	        final GPData input,
//	        final ADFStack stack,
//	        final GPIndividual individual,
//	        final Problem problem) {
//			
//			resetRegisters(problem, 1.0, this);
//	        
//	 		for(int index = 0; index<this.getTreesLength(); index++){
//	 			GPTreeStruct tree = this.getTreeStruct(index);
//	 			if(tree.status) {
//	 				tree.child.eval(state, thread, input, stack, this, problem);
//	 			}
//	 			
//	 		}
//	 		
//	 		return registers[0];
//		}
	
//	@Override
//	public void prepareExecution(EvolutionState state){
//		//check whehter we need flow controller
//		//if instruction type of all effective instructions is 0, set fast mode
//		setFastFlag(1); // 1: fast mode,  0: slow mode
//		for(GPTreeStruct tree : getTreelist()) {
//			if(tree.status && tree.type != GPTreeStruct.ARITHMETIC) {  //effective and not arithmetic (branching or iteration)
//				setFastFlag(0);
//				break;
//			}
//		}
//		
//		if(exec_trees == null){
//			exec_trees = new ArrayList<>();
//		}
//		else{
//			exec_trees.clear();
//		}
//		
//		
//		//check which registers are necessarily to be initialized.
//		setInitReg(new int [getNumRegs()]);
//		//init_ConReg = new int [JobShopAttribute.values().length];
//		for(int i = 0;i<getNumRegs();i++){
//			getInitReg()[i] = -1;
//		}
//		for(GPTreeStruct tree : getTreelist()){
//			if(!tree.status) continue;
//			
//			for(int c = 0;c<2;c++){
//				GPNode node = tree.child.children[0].children[c];
//				if(node instanceof ReadRegisterGPNode){
//					int ind = ((ReadRegisterGPNode)node).getIndex();
//					if(getInitReg()[ind]==-1){ //have not been written or read
//						getInitReg()[ind] = 1;  //it is read before being written, necessary to be initialized. 
//					}
//				}
//				
//				//check which constant register is necessarily to be initialized.
////				if(node instanceof ReadConstantRegisterGPNode){
////					init_ConReg[((AttributeGPNode)((TerminalERC)node).getTerminal()).getJobShopAttribute().ordinal()] = 1;
////				}
//			}
//			int ind = ((WriteRegisterGPNode)tree.child).getIndex();
//			if(getInitReg()[ind] == -1){ //have not been written or read
//				getInitReg()[ind] = 0; //it is wirtten before being read, unnecessary to be initialized
//			}
//			
//			
//			if(getFastFlag() == 1){
//				exec_trees.add(tree);
//			}
//		}
//		
//		if(registers == null) {
//			setRegisters(new double[getNumRegs()]);
//		}
////		if(constant_registers == null){
////			constant_registers = new double [JobShopAttribute.values().length];
////		}
//		
//		//identify the initialization input features for the registers. 
//		JobShopAttribute list[] = JobShopAttribute.relativeAttributes();
//	     setInitReg_values(new AttributeGPNode [getNumRegs()]);
//		 for(int i = 0;i<getNumRegs();i++){
//			 JobShopAttribute a = list[i % list.length];
//			 getInitReg_values()[i] = new AttributeGPNode(a);
//		 }
//	}
	
//	protected void initializeFrequency(EvolutionState state){
//		//get the function set
//		 GPFunctionSet set = this.getTree(0).constraints((GPInitializer)state.initializer).functionset;  //all trees have the same function set	
//		 
//		 //collect the name of all functions and constants
//		 //functions
//		 int i = 0;
//		 dimension = 0;
//		 dimension += set.nonterminals_v.size();
//		 for(;i<set.nonterminals_v.size();i++){
//			 primitives.add(set.nonterminals_v.get(i).toString());
//		 }
//		//registers
//		 dimension += numRegs;
//		 for(i=0;i<numRegs;i++){
//			 primitives.add("R"+i);
//			 primitives.add("R"+i+"=");
//		 }
//		 //constants
//		 dimension += JobShopAttribute.relativeAttributes().length;
//		 for(i=0;i<JobShopAttribute.relativeAttributes().length;i++){
//			 primitives.add(JobShopAttribute.relativeAttributes()[i].getName());
//		 }
//		 
//		 
//	}
	
//	public double[] getFrequency(int start,int end){
//		//return the functions and terminals' frequency between start and end instruction
//		if(dimension <=0){
//			System.err.print("dimension is less than or equal to 0 in LGPIndividual\n");
//			System.exit(1);
//		}
//		else if(start<0||start>trees.size()-1||end<0||end>trees.size()-1){
//			System.err.print("start or end arguments are out of range in LGPIndividual\n");
//			System.exit(1);
//		}
//		else{
//			frequency = new double[dimension];
//			for(int i = start;i<=end;i++){
//				GPTreeStruct tree = trees.get(i);
//				
//				int si;
//				//get the index of its write register
//				String name = tree.child.toString();
//				si = primitives.indexOf(name);
//				frequency[si] ++;
//				
//				//get the index of its function
//				name = tree.child.children[0].toString();
//				si = primitives.indexOf(name);
//				frequency[si] ++;
//				
//				for(int j = 0;j<tree.child.children[0].expectedChildren();j++){
//					if(tree.child.children[0].children[j] instanceof TerminalERC){
//						name = ((TerminalERC)tree.child.children[0].children[j]).getTerminal().toString();
//						si = primitives.indexOf(name);
//						frequency[si] ++;
//					}
//				}
//			}
//		}
//		return frequency;
//	}
	
	
	
//	
//	
//	
//	@Override
//	public void calcFitnessInd(Fitness fitness, EvolutionState state, SchedulingSet4Ind schedulingSet,
//			List<Objective> objectives){
//		 
//			this.prepareExecution(state);
//			
//			CpxGPIndividual4DJSS.calcFitnessInd(fitness, state, schedulingSet, objectives);
//	 }
//	
//	public void calcFitnessInd4OneTask(Fitness fitness, EvolutionState state, SchedulingSet4Ind schedulingSet,
//			List<Objective> objectives, int simIndex){
//		double[] fitnesses = new double[objectives.size()];
//
//		this.prepareExecution(state);
//		
//		schedulingSet.setIndividual(this);
//		Simulation4Ind simulation = schedulingSet.getSimulations().get(simIndex);
//		int col = 0;
//
//		//System.out.println("The simulation size is "+simulations.size()); //1
//			//Simulation4Ind simulation = simulations.get(j);
//			//simulation.rerun(this, state, col);
//			simulation.rerun();
//
//			for (int i = 0; i < objectives.size(); i++) {
//				// System.out.println("Makespan:
//				// "+simulation.objectiveValue(objectives.get(i)));
//				// System.out.println("Benchmark makespan:
//				// "+schedulingSet.getObjectiveLowerBound(i, col));
//				double normObjValue = simulation.objectiveValue(objectives.get(i));
//				//		/ (schedulingSet.getObjectiveLowerBound(i, col));
//
//				//modified by fzhang, 26.4.2018  check in test process, whether there is ba
//				fitnesses[i] += normObjValue;
//			}
//
//			col++;
//
//			//System.out.println("The value of replication is "+schedulingSet.getReplications()); //50
//			for (int k = 1; k < schedulingSet.getReplications().get(simIndex); k++) {
//				//simulation.rerun(this, state, col);
//				simulation.rerun();
//
//				for (int i = 0; i < objectives.size(); i++) {
//					double normObjValue = simulation.objectiveValue(objectives.get(i));
//					//		/ (schedulingSet.getObjectiveLowerBound(i, col)+1e-6);
//					fitnesses[i] += normObjValue;
//				}
//
//				col++;
//			}
//
//			simulation.reset();
//		
//
//		for (int i = 0; i < fitnesses.length; i++) {
//			fitnesses[i] /= col;
//		}
//		
//		MultiObjectiveFitness f = (MultiObjectiveFitness) fitness;
//		if(f.getNumObjectives()>1){
//			f.setObjectives(state, fitnesses);
//		}
//		else{
//			double [] tmp_f = new double [1];
//			tmp_f[0] = fitnesses[simIndex];
//			f.setObjectives(state, tmp_f);
//		}
//	}
	
	
	
//	public Operation priorOperation(DecisionSituation decisionSituation) {
//        List<Operation> queue = decisionSituation.getQueue();
//        WorkCenter workCenter = decisionSituation.getWorkCenter();
//        SystemState systemState = decisionSituation.getSystemState();
//
//        Operation priorOp = queue.get(0);
//        priorOp.setPriority(
//                priority(priorOp, workCenter, systemState));
//
//        for (int i = 1; i < queue.size(); i++) {
//            Operation op = queue.get(i);
//            op.setPriority(priority(op, workCenter, systemState));
//
//            if (op.priorTo(priorOp))
//                priorOp = op;
//        }
//        
//        
//
//        return priorOp;
//    }
}
