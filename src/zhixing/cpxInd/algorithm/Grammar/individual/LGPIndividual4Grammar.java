package zhixing.cpxInd.algorithm.Grammar.individual;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Fitness;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPFunctionSet;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPTree;
import ec.gp.GPType;
import ec.util.Parameter;
import yimei.jss.gp.data.DoubleData;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.DerivationRule;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.Grammarrules;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPFlowController;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.Branching;
import zhixing.cpxInd.individual.primitive.FlowOperator;
import zhixing.cpxInd.individual.primitive.Iteration;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;


public abstract class LGPIndividual4Grammar extends zhixing.cpxInd.individual.LGPIndividual{
	//there is a derivation tree, each tree node is a ModuleConstraint or a InstructionConstraint. The leaf nodes must be InstructionConstraint
	
	//each instruction manages the type matrices for the destination register and source registers
	//there is a register type matrix update method
	
	//each instruction remember its InstructionConstraint
	
	public final static String P_RULESPATH = "rulepath";
	public final static String P_TYPECONSTRAINT = "typeconstraint";
	public final static String P_MODULECONSTRAINT = "moduleconstraint";
	
	protected static Grammarrules RULE = new Grammarrules();
	
	protected DerivationTree DTree = new DerivationTree();
	
	protected String typeLibrary = Grammarrules.NOCONSTRAINT;  //the type constraint library the individual is using, "None" is default value -- means no constraint
	
	protected String moduleLibrary = Grammarrules.NOCONSTRAINT;  //the module constraint library the individual is using, "None" is default value -- means no constraint
	
	protected ArrayList<Double> instr_used;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		//set up the individual prototype 
		 super.setup(state,base); 
		 Parameter def = defaultBase();
		 
		 String rulefile = state.parameters.getString(base.push(P_RULESPATH),def.push(P_RULESPATH));
		 
		 String s = state.parameters.getString(base.push(P_MODULECONSTRAINT),def.push(P_MODULECONSTRAINT));
		 if(s != null) {
			 moduleLibrary = s;
		 }
		 s = state.parameters.getString(base.push(P_TYPECONSTRAINT),def.push(P_TYPECONSTRAINT));
		 if(s != null) {
			 typeLibrary = s;
		 }
		 
		 RULE.readGrammarrules(rulefile);
	}
	
	@Override
	public void rebuildIndividual(EvolutionState state, int thread) {
		
		if(moduleLibrary.equals(Grammarrules.NOCONSTRAINT)) {
			super.rebuildIndividual(state, thread);
			return;
		}
		
//		int numtrees = state.random[thread].nextInt(initMaxNumTrees - initMinNumTrees + 1) + initMinNumTrees;
		
		getTreelist().clear();
		
		//loop the generation until the number of instructions in [initMinNumTrees, initMaxNumTrees]
		//1. generate the derivation tree, for each leaf node of the derivation tree, generate a specific instruction
		DTree.newTree(this, state, thread, this.initMaxNumTrees, this.initMinNumTrees);
		
		GPTreeStructGrammar treePrototype = (GPTreeStructGrammar)(state.parameters.getInstanceForParameterEq(
                privateParameter,defaultBase().push(P_TREE).push(""+0),GPTreeStructGrammar.class));
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		treelist = ((InstructionBuilder) treePrototype.constraints(initializer).init).genInstructionSeq(state,
				treePrototype.constraints(initializer).treetype,
	            thread,
	            treePrototype,
	            treePrototype.constraints(initializer).functionset,
	            DTree.root);
		
		updateStatus();
		
		//remove instructions so that the program has suitable length
		
		
		//2. if it is effective initialization, re-sample the destination register untill it is effective
		if(eff_initialize) {
			for(int t = getTreelist().size()-1; t>=0; t--) {
				if(!getTreeStruct(t).status) {
					int trial = 50;
					GPTreeStructGrammar instr = (GPTreeStructGrammar) getTreeStruct(t);
					while(!instr.status && trial>0) {
						
						GPNode p2 = ((InstructionBuilder) treePrototype.constraints(initializer).init).genOneNodeByGrammar(state, 
								instr.constraints(initializer).treetype, 
								thread, 
								instr, 
								instr.child.parent,
								instr.constraints(initializer).functionset,
								instr.child.argposition,
								instr.child.atDepth()); //0: only shuffle the destination register, to make the instruction effective
						
						instr.owner = this;
						instr.child.replaceWith(p2);
		                instr.child.parent = instr;
		                instr.child.argposition = 0;
		                this.setTree(t, instr);

						trial --;
					}
				}
			}
			
		}
		
		if(!checkDTreeNInstr()) {
        	System.err.print("something wrong after initialization\n");
        	System.exit(1);
        }
	}
	
//	
//	@Override
//	public void updateStatus(int n, int []tar) {
//		//update type matrix for each instruction
//	}
	
    public Object clone() {
    	LGPIndividual4Grammar rec = (LGPIndividual4Grammar) super.clone();
    	
    	rec.copyLGPproperties(this);
    	
    	return rec;
    }
    
    public LGPIndividual4Grammar lightClone() {
    	LGPIndividual4Grammar rec = (LGPIndividual4Grammar) super.lightClone();
    	
    	rec.copyLGPproperties(this);
    	
    	return rec;
    }
    
	@Override
	protected void copyLGPproperties(LGPIndividual obj) {
		super.copyLGPproperties(obj);
		
		this.typeLibrary = ((LGPIndividual4Grammar)obj).getTypeConLibrary();
		this.moduleLibrary = ((LGPIndividual4Grammar)obj).getModuleConLibrary();
		
		if(((LGPIndividual4Grammar)obj).getDerivationTree().root != null) {
			DTree = (DerivationTree) ((LGPIndividual4Grammar)obj).getDerivationTree().lightClone();
			
			if(DTree.numLeaves != this.getTreesLength()) {
				System.err.println("make sure the derivation tree is coincident with the instruction list.");
				System.exit(1);
			}
			else {
				DTree.rematch(this);
				
				if(!this.checkDTreeNInstr()) {
					if(! ((LGPIndividual4Grammar)obj).checkDTreeNInstr()) {
						System.err.print("something wrong before clone\n");
					}
		        	System.err.print("something wrong after clone\n");
		        	System.exit(1);
		        }
			}
		}
		else { //the objective individual has a null derivation tree  so this individual new a tree for itself
			DTree = new DerivationTree();
		}
		
	}
	
//	@Override
//	public boolean setTree(int index, GPTree tree){
//		//check whether the module constraints are consistent
//	}
//	
//	@Override
//	public void addTree(int index, GPTree tree){
//		
//	}
//	
//	@Override
//	public boolean removeTree(int index){
//		
//	}
	
	public final Grammarrules getGrammarrules() {
		return RULE;
	}
	
	public String getModuleConLibrary(){
		return moduleLibrary;
	}
	
	public String getTypeConLibrary(){
		return typeLibrary;
	}
	
	public DerivationTree getDerivationTree() {
		return DTree;
	}
	
	public ArrayList<GPTreeStructGrammar> getInstructionList(DTNode subtree){
		//get the corresponding instruction list based on the sub derivation tree
		ArrayList<GPTreeStructGrammar> reslist = new ArrayList<>();
		
		if(subtree.siblings.size()>0) {
			for(DTNode n : subtree.siblings) {
				reslist.addAll(getInstructionList(n));
			}
		}
		else { //it should be on the leaf of the derivation tree
			if(subtree.instructionChildren.size()==0) {
				System.err.print("we should link the derivation tree with the instruction list before getting the instructions\n");
				System.exit(1);
			}
			else {
				for(GPTreeStructGrammar ins : subtree.instructionChildren) {
					
					if(!getTreelist().contains(ins)) {
						System.err.print("the derivation tree does not link with instruction list properly in getInstructionList(DTNode)\n");
						System.exit(1);
					}
					
					reslist.add(ins);
				}
			}
		}
		return reslist;
	}
	
	public void removeTreeBasedDTNode(DTNode node) {
		
		for(GPTreeStruct instr : node.instructionChildren) {
			if(! getTreelist().remove(instr))
			{
				System.err.print("Removing failed in LGPIndividual4Grammar.removeTreeBasedDTNode()\n");
				System.exit(1);
			}
		}
		
		for(DTNode n : node.siblings) {
			removeTreeBasedDTNode(n);
		}
		
	}
	
//	@Override
//	public boolean setTree(int index, GPTree tree){
//		boolean res = super.setTree(index, tree);
//		
//		this.DTree.rematch(this.DTree.root, 0, this);
//		
//		return res;
//	}
//	
//	@Override
//	public void addTree(int index, GPTree tree){
//		super.addTree(index, tree);
//		
//		this.DTree.rematch(this.DTree.root, 0, this);
//	}
//
//	@Override
//	public boolean removeTree(int index) {
//		boolean res = super.removeTree(index);
//		
//		this.DTree.rematch(this.DTree.root, 0, this);
//		
//		return res;
//	}
	
	public boolean checkDTreeNInstr() {
		
		boolean res = true;
		
		DTree.numLeaves = DTree.root.getLeavesNum();
		DTree.treeDepth = DTree.root.getTreeDepth();
		
		//check DTtree leavenum vs. # instruction
		if(DTree.root.getLeavesNum() != getTreelist().size()) {
			System.err.print("the leave num of DTree is inconsistent with the instruction list\n");
			return false;
		}
		
		//check the leaf DTNodes are consistent with the corresponding instruction
		if(!subcheckDTNodeNInstr(DTree.root)) return false;
		
		return res;
	}
	
	protected boolean subcheckDTNodeNInstr(DTNode curnode) {
		boolean res = true;
		if(curnode.instructionChildren.size()>0) {
			for(GPTreeStructGrammar instr : curnode.instructionChildren) {
				
				if(!this.getTreeStructs().contains(instr)) {
					System.err.print("the DTNode's instruction is inconsistent with the instruction list");
					return false;
				}
				
				GPNode des = instr.child;
				String primitiveName = des.toString().substring(0, des.toString().length()-1);
				if(!curnode.param_value.get(0).contains(primitiveName)) {
					System.err.print("the destination register is out of grammar range\n");
					return false;
				}
				
				GPNode fun = instr.child.children[0];
				primitiveName = fun.toStringForHumans();
				if(!curnode.param_value.get(1).contains(primitiveName)) {
					System.err.print("the function is out of grammar range\n");
					return false;
				}
				
				for(int arg = 2; arg<curnode.param_value.size(); arg++) {
		        	GPNode src = instr.child.children[0].children[arg-2];
		        	primitiveName = src.toString();
		        	if(!curnode.param_value.get(arg).contains(primitiveName)) {
		        		System.err.print("the source register is out of grammar range\n");
		        		return false;
		        	}
				}
			}
		}
		
		if(curnode.siblings.size() > 0) {
			if(curnode.StartNode.size() != curnode.repeatnum) {
				System.err.print("Inconsistent StartNode and repeatnum\n");
        		return false;
			}
		}
		
		for(DTNode node : curnode.siblings) {
			res = res && subcheckDTNodeNInstr(node);
		}
		
		return res;
	}

	protected void updateBannedList() {
		//this function counts the actual execution times of each instruction.
		for(int i = 0;i<getTreelist().size();i++) {
			instr_used.set(i, instr_used.get(i) + getFlowctrl().used.get(i));
		}
	}
	
	@Override
	public double execute(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {
		//execute the whole individual. the fastFlag means that we will not consider the flow of instructions, just execute instructions one-by-one
		//the fastFlag should be set true only when there is not flow control operators in the primitive set.
		
		this.resetRegisters(problem, 1, (LGPIndividual) individual); //initialize registers by input features (or the default value)
		
		if(getFastFlag() == 1){
			DoubleData rd = ((DoubleData)(input));
			for(GPTreeStruct tree : exec_trees) {
				tree.child.eval(state, thread, input, stack, individual, problem);
		}
		}
		else{
			
			getFlowctrl().execute(state, thread, input, stack, (CpxGPIndividual)individual, problem);
			
			updateBannedList();
		}
			
		
		return registers[getOutputRegister()[0]]; //only the first output register will be used in basic LGP
	}
	
	@Override
	public void updateStatus() {
		super.updateStatus();
		
		instr_used = new ArrayList<>(getTreelist().size());
		for(int i = 0;i<getTreelist().size();i++) {
			instr_used.add(0.0);
		}
	}
	
	public ArrayList<Double> getUsedList(){
		return instr_used;
	}
}
