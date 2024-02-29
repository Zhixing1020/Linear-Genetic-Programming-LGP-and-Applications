package zhixing.cpxInd.algorithm.Grammar.individual;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.DerivationRule;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.Grammarrules;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.ModuleConstraint;
import zhixing.cpxInd.individual.GPTreeStruct;

public class DTNode extends DerivationRule {
	//it is a node in derivation tree. it is also an instantiated derivation rule.
	//use a vector to store the sibling. the type of to-be-added DTNode can only follow its preceding instructions (single direction)
	public int repeatnum = 1;
//	public boolean constantFlag = false;
	public Vector<DTNode> siblings;	//point to the children DTNode in derivation tree
	public Vector<GPTreeStructGrammar> instructionChildren; //link derivation tree with instructions, it helps us identify the instructions when swapping sub derivation trees
	public Vector<DTNode> StartNode; //the starting index of siblings Vector in each repeat
	
	public int growNodeBasedGram (final LGPIndividual4Grammar ind,
			final EvolutionState state, 
			final int thread, 
			final Grammarrules RULE, 
			DTNode parent, 
			final String modLibrary,
			final int insertIndex,
			final int cutORgrow){
		//main task: generate a DTNode, recursively call the derivation rules (to generate children's node), and put it into parent's sibling
		//return the number of siblings of this DTNode
		
		//logic: 1) check the grammar rules based on the moduleName 2a) if it is a module or composite, grow siblings  2b) if it is an instruction, generate
		//a detailed instruction and insert (or append) to the individual
		
//		int numGeneratedInstr = 0;
		
		//search module constraint and its derivation rules
		ModuleConstraint mc = RULE.getModuleConstraint(modLibrary, parent.moduleName);
		
		//randomly select one derivation rule list
		int ruleInd = state.random[thread].nextInt(mc.derivation_siblings.size());
		
		ArrayList<DerivationRule> derule_list = mc.derivation_siblings.get(ruleInd);
		
//		numGeneratedInstr += 
		growNode(ind, state, thread, RULE, parent, modLibrary, derule_list, insertIndex, /*parent.siblings.size(),*/ cutORgrow);
		
		return derule_list.size();
	}
	
	public int growNode (final LGPIndividual4Grammar ind,
			final EvolutionState state, 
			final int thread, 
			final Grammarrules RULE, 
			DTNode parent, 
			final String modLibrary,
			final ArrayList<DerivationRule> ruleseq,
			final int insertInd,
			final int cutORgrow) {
		//cutORgrow: <0: it is the initialization,  >0: grow the repeating number (or reset the repeating number)
		//this function returns the number of generated instructions, for further check on the program size 
		int numGeneratedInstr = 0;
		
		//for each rule in the list, a) grow the node or b) initialize an instruction
		int ii = 0;
		for(DerivationRule rule : ruleseq){
			//clone DTNode from the derivation rule
			DTNode n = new DTNode();
			n.initialize(rule);
			
			if(cutORgrow < 0 && state.random[thread].nextDouble()<1.0) {
				n.repeatnum = state.random[thread].nextInt(Math.min(5, n.repeatnum)) + 1;
			}
			else if (cutORgrow > 0 && n.repeatnum<n.maxrepeatnum && state.random[thread].nextDouble()<1.0) {
				n.repeatnum += state.random[thread].nextInt(n.maxrepeatnum - n.repeatnum) + 1;
			}
			else {
				n.repeatnum = state.random[thread].nextInt(Math.min(10, n.maxrepeatnum)) + 1;
			}
			
			
			//if DTNode is instr
			if(n.moduleName.equals(DerivationRule.INSTRUCTION)) {
				//set the parameter values based on parent
				n.passValues(parent);
				
				//return numGeneratedInstr
				numGeneratedInstr += n.repeatnum;
			}
			//if DTNode is composite
			else if(n.moduleName.equals(DerivationRule.COMPOSITE)) {
				//because composite derivation has no specific parameter name and values, it directly inherits all parameters of the parent
				n.param_name = parent.param_name;
				n.param_pronoun = parent.param_pronoun;
				n.param_value = parent.param_value;
				//but the repeat number and the boolean check are not changed.
				
				for(int rep = 0; rep<n.repeatnum; rep++) {
					numGeneratedInstr += growNode(ind, state, thread, RULE, n, modLibrary, n.sub_rules, n.siblings.size(), cutORgrow);
				}
				
			}
			
			//if DTNode is a module, grow the derivation tree
			else if(RULE.getModuleConstraintLib(modLibrary) == null ) {
				System.err.println("invaild module constraint library in growing derivation tree: " + modLibrary);
				System.exit(1);
			}
			else if(RULE.getModuleConstraintLib(modLibrary).isContains(n.moduleName)) {
				n.passValues(parent);

				for(int rep = 0; rep<n.repeatnum; rep++) {
					growNodeBasedGram(ind, state, thread, RULE, n, modLibrary, n.siblings.size(), cutORgrow);
					numGeneratedInstr += this.getLeavesNum();
				}
				
			}
			
			//parent.add sibling DTNodes based on the DerivationRule
			//parent.siblings.add(insertInd+ii, n);
			parent.addSiblingsNStartNode(insertInd+ii, n, ii==0); //record the 
			ii++;
			
		}
		
		return numGeneratedInstr;
	}
	
	
	public int getTreeDepth() {
		int dep = 0;
		for(DTNode n : siblings) {
			int td = n.getTreeDepth();
			if(td>dep) {
				dep = td;
			}
		}
		return 1+dep;
	}
	
	public int getTreeNodeNum(String modulekind) {
		int res = 0;
		
		if(modulekind==null) { //count all tree nodes
			res += 1;
		}
		else {
			if(this.moduleName.equals(modulekind)) {
				res += 1;
			}
		}
		
		for(DTNode n : this.siblings) {
			res += n.getTreeNodeNum(modulekind);
		}
		return res;
	}
	
//	public void getTreeNodeFreedom(String modulekind, double[] freedom, int index) {
//		if(modulekind == null || this.moduleName.equals(modulekind)) {
//			freedom[index] = this.getFreedomDegree();
//			index ++;
//		}
//		
//		for(DTNode n : this.siblings) {
//			n.getTreeNodeFreedom(modulekind, freedom, index);
//		}
//	}
//	
//	protected double getFreedomDegree() { //return the degree of freedom of this node
//		
//		double res = 0;
//		
//		for(Set<String> values : param_value) {
//			if(values.size() == 0) {
//				System.err.print("please initialize the parameter value of "+ this.moduleName 
//						+" before calculating degree of freedom\n");
//				System.exit(1);
//			}
//			res += values.size() - 1;
//			
//		}
//		
//		return res / 4;
//	}
	
	public int getLeavesNum() {
		//the number of leaves of the derivation tree, equivalent to the number of instructions
		int res = 0;
		if(siblings.size()==0) {
			res += this.repeatnum; 
		}
		else {
			for(DTNode n : siblings) {
				res += n.getLeavesNum();
			}
		}
		return res;
	}
	
	public boolean containEffectiveInstruction() {
		boolean res = false;
		
		if(siblings.size()==0) {
			if(instructionChildren.size()>0) {
				for(GPTreeStructGrammar instr : instructionChildren) {
					if(instr.status) {
						res = true; break;
					}
				}
			}
			else {
				System.err.print("We found a DTNode having no sibling and instruction children\n");
				System.exit(1);
			}
		}
		else {
			for(DTNode n : siblings) {
				res = res || n.containEffectiveInstruction();
			}
		}
		return res;
	}
	
	public int nodeInPosition(int cur, String nodesearch, final DTNodeGatherer res, final int count) {

		if(nodesearch == null || this.moduleName.equals(nodesearch)) {
			
			if(cur == count) {
				res.node = this;
				return -1; //found it
			}
			cur++;
		}
		
		int t=0;
		for(int i = 0; i<siblings.size()&&t!=-1; i++) {
			t = siblings.get(i).nodeInPosition(cur, nodesearch, res, count);
			if(t!=-1)
			{
				cur = t;
			}
			else {
				return -1;
			}
		}
		return cur;
	}
	

	public Object clone(){		
		DTNode obj = new DTNode();
		
		copyProperties(obj);
		
		return obj;
	}
	
	public Object lightClone() {
		return clone();
	}
	
	protected void copyProperties(DTNode object) {
		super.copyProperties(object);
		
		object.repeatnum = this.repeatnum;
		object.siblings = new Vector<>();
		object.StartNode = new Vector<>();
		
		for(DTNode node : this.siblings) {
			DTNode copy = (DTNode)node.clone();
			
			object.siblings.add(copy);
			
			if(this.StartNode.contains(node)) {
				object.StartNode.add(copy);
			}
		}
		
		object.instructionChildren = new Vector();
//		for(GPTreeStructGrammar g : this.instructionChildren) {
//			object.instructionChildren.add(g);
//		}
		//Be careful, the instructionChildren is not cloned so far.
	}
	
	protected void initialize(DerivationRule rule) {
		if(rule != null)
		this.moduleName = rule.moduleName;
		
		this.param_name = new ArrayList<>();
		if(rule != null)
		for(String p : rule.param_name) {
			this.param_name.add(p);
		}
		
		this.param_pronoun = new ArrayList<>();
		if(rule != null)
		for(String p : rule.param_pronoun) {
			this.param_pronoun.add(p);
		}
		
		this.param_value = new ArrayList<>();
		if(rule != null)
		for(Set<String> psSet : rule.param_value) {
			
			Set<String> tmpSet = new HashSet<>();
			for (String s : psSet) {
				tmpSet.add(s);
			}
			
			this.param_value.add(tmpSet);
		}
		
		if(rule != null) {
			this.booleanCheck = rule.booleanCheck;
			this.maxrepeatnum = rule.maxrepeatnum;
		}
		else {
			this.booleanCheck = new String();
			this.maxrepeatnum = 1;
		}
		
		this.sub_rules = new ArrayList<>();
		if(rule != null)
		for(DerivationRule r : rule.sub_rules) {
			sub_rules.add((DerivationRule) r.clone());
		}
		
		this.siblings = new Vector<>();
		
		this.instructionChildren = new Vector<>();
		
		this.StartNode = new Vector<>();
	}
	
	
	protected void passValues(DTNode parent) {
		//set the parameter values for this DTNode
		for(int i = 0;i<this.param_value.size();i++) {
			Set<String> values = this.param_value.get(i);
			
			String pronoun = this.param_pronoun.get(i);
			
			if(values != null && values.size()>0 && pronoun==null) continue;  //the grammar rule has given predefined values for the parameter
			
			//obtain the values from parent
			values = readParameterValues(parent, pronoun);
			this.param_value.set(i, values);
			
		}
	}
	
	public void updateByPassValues(final EvolutionState state, final int thread, DTNode parent, LGPIndividual4Grammar ind, GPNodeBuilder builder) {
		//a recursive method to update the parameter values and 
		//ind: the individual owning the derivation tree
		
		if(! ind.getDerivationTree().contains_pointer(parent, ind.getDerivationTree().root)) {
			System.err.print("the individual does not contain the given DTNode. We cannot update for un-paried individuals and DTNodes\n");
			System.exit(1);
		}
		
		this.passValues(parent); //use the parent node to update
		
		for(DTNode node : this.siblings) {
			node.updateByPassValues(state, thread, this, ind, builder);
		}
		
		//if the DTNode specifies instructions
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		for(GPTreeStructGrammar instr_old : this.instructionChildren) { //check the primitives of each instruction  
			//destination
//			GPNode des = ins.child;
//			String primitiveName = des.toString().substring(0, des.toString().length()-1);
//			int trial = 50;
//			while(!this.param_value.get(0).contains(primitiveName) && trial>0) {
//				des.resetNode(state,thread);
//	        	primitiveName = des.toString().substring(0, des.toString().length()-1);
//				
//				trial --;
//			}
			int listIndex = ind.getTreeStructs().indexOf(instr_old);
			int siblingIndex = this.instructionChildren.indexOf(instr_old);
			GPTreeStructGrammar instr = (GPTreeStructGrammar) instr_old.clone();
			
			GPNode des = instr.child;
			String primitiveName = des.toString().substring(0, des.toString().length()-1);
			if(!this.param_value.get(0).contains(primitiveName)) {
				des = ((InstructionBuilder)builder).genOneNodeByGrammar(state,
						instr.constraints(initializer).treetype,
						thread,
						instr,
						instr,
						instr.constraints(initializer).functionset,
						0,0);
		        

				instr.child.replaceWith(des);
			}
			
			
			//function
			GPNode fun = instr.child.children[0];
			primitiveName = fun.toStringForHumans();
			if(!this.param_value.get(1).contains(primitiveName)) {
				fun = ((InstructionBuilder)builder).genOneNodeByGrammar(state,
		        		instr.constraints(initializer).treetype,
		        		thread,
		        		instr,
		        		des,
		        		instr.constraints(initializer).functionset,
		        		0,1);

				instr.child.children[0].replaceWith(fun);
				
			}
			
	        
			//source
	        for(int arg = 2; arg<this.param_value.size(); arg++) {
	        	GPNode src = instr.child.children[0].children[arg-2];
	        	primitiveName = src.toString();
	        	if(!this.param_value.get(arg).contains(primitiveName)) {
	        		src = ((InstructionBuilder)builder).genOneNodeByGrammar(state,
	            			instr.constraints(initializer).treetype,
	            			thread,
	            			instr,
	            			fun,
	            			instr.constraints(initializer).functionset,
	            			arg-2,2);                

	        		fun.children[arg-2].replaceWith(src);
	        	}
            	
            }
	        
	        
	        ind.getTreeStructs().set(listIndex, instr);
	        
	        this.instructionChildren.set(siblingIndex, instr);
		}
	}
	
	private Set<String> readParameterValues(final DTNode parent, final String pronoun) {
		//this functions reads the parameter values and assign them to specific parameters
		//Notes: only support simply operation between sets. the operations are executed linearly, 
		
		Set<String> tmpSet = new HashSet<>(), tmpSet2 = new HashSet<>();
		
		int index = getMinOpIndex(0, pronoun);
		
		if(index == -1) { //there is no set operation in parameter passing
			tmpSet = readSet(parent, pronoun);
			
			if(tmpSet.isEmpty()) {
				System.err.println("we have empty values for parameter " + pronoun);
				System.exit(1);
			}
			
			return tmpSet;
		}
		else {  //there is at least one operation, so record the first set of values.
			String value1 = pronoun.substring(0, index);
			
			tmpSet = readSet(parent, value1);
		}
		
		do { //sequentially execute the set operation
			//get the operand set
			int index2 = -1;
			index2 = getMinOpIndex(index+1, pronoun);
			
			if(index2 == -1) { //there is no set operation in parameter passing
				tmpSet2 = readSet(parent, pronoun.substring(index+1));
			}
			else {  //there is at least one operation, so record the first set of values.
				String value1 = pronoun.substring(index+1, index2);
				
				tmpSet2 = readSet(parent, value1);
			}
			
			if(pronoun.substring(index, index+1).equals(Grammarrules.UNIONSET.substring(1))) {
				tmpSet.addAll(tmpSet2);
			}
			else if(pronoun.substring(index, index+1).equals(Grammarrules.DIFFERENCE)) {
				tmpSet.removeAll(tmpSet2);
			}
			else if(pronoun.substring(index, index+1).equals(Grammarrules.INTERSECTION.substring(1))) {
				tmpSet.retainAll(tmpSet2);
			}
			
			index = index2;
		}while(index != -1);
		
		if(tmpSet.isEmpty()) {
			System.err.println("we have empty values for parameter " + pronoun);
			System.exit(1);
		}
		
		return tmpSet;
	}
	
	private Set<String> readSet(final DTNode parent, final String pronoun){
		//read the set of parameter values or read from its parent's parameters
		Set<String> tmpSet = new HashSet<>();
		
		if(pronoun.startsWith(Grammarrules.SET_l.substring(1)) && pronoun.endsWith(Grammarrules.SET_r.substring(1))) {
			String[] vsStrings = pronoun.split(Grammarrules.SET_sep+"|"+Grammarrules.SET_l+"|"+Grammarrules.SET_r);
			for(String vs : vsStrings) {
				tmpSet.add(vs);
			}
		}
		else {
			for(int i = 0; i<parent.param_name.size(); i++) {
				if(parent.param_name.get(i).equals(pronoun)) {
					for(String v : parent.param_value.get(i)) {
						tmpSet.add(v);
					}
				}
			}
		}
		
		return tmpSet;
	}

	private int getMinOpIndex(final int startInd, final String pronoun) {
		//this function gets the string index of the first operation in the parameter specification
		int index = pronoun.substring(startInd).indexOf(Grammarrules.UNIONSET.substring(1));
		
		int tmp = pronoun.substring(startInd).indexOf(Grammarrules.DIFFERENCE);
		if(tmp > 0) {
			if(index >= 0) {
				index = Math.min(index, tmp);
			}
			else {
				index = tmp;
			}
		}
		
		tmp = pronoun.substring(startInd).indexOf(Grammarrules.INTERSECTION.substring(1));
		if(tmp > 0) {
			if(index >= 0) {
				index = Math.min(index, tmp);
			}
			else {
				index = tmp;
			}
		}
		
		return index;
	}
	
	public String toString() {
		String reString = "";
		reString += this.moduleName;
		if(booleanCheck != null && !booleanCheck.isBlank()){
			reString += Grammarrules.BOOLEANCHECK_l + booleanCheck + Grammarrules.BOOLEANCHECK_r;
		}
		reString += Grammarrules.REPEAT + repeatnum;
		return reString;
	}
	
	public String makeGraphvizTree()
    {
		return "digraph g {\ngraph [ordering=out];\nnode [shape=rectangle];\n" + makeGraphvizSubtree("n") + "}\n";
    }
	
	protected String makeGraphvizSubtree(String prefix)
    {
	    String body = prefix + "[label = \"" + toString() + "\"];\n";
	    for(int x = 0; x < siblings.size(); x++)
	        {
		        String newprefix;
		        if (x < 10) newprefix = prefix + x;
		        else newprefix = prefix + "n" + x;  // to distinguish it
		        
		        body = body + siblings.get(x).makeGraphvizSubtree(newprefix);
		        body = body + prefix + " -> " + newprefix + ";\n";
	        }
	    return body; 
    }
	
	public DTNode replaceDTree(DTNode newsubtree, DTNode oldsubtree) {
		//find the old sub derivation tree based on oldsubtree
		DTNode res = null;
		if(siblings.size() > 0) {
			for(DTNode n : siblings) {
				if(n == oldsubtree) { //find the oldsubtree
					int ind = siblings.indexOf(n);
					siblings.set(ind, newsubtree);
					if(StartNode.contains(n)) {
						ind = StartNode.indexOf(n);
						StartNode.set(ind, newsubtree);
					}
					res = newsubtree;
					break;
				}
				else {
					res = n.replaceDTree(newsubtree, oldsubtree);
					if(res != null) break;
				}
			}
			
		}
		return res;
	}
	
//	public boolean sibling_equalsto(DTNode another) {//this method is useless
//		if(!this.equalsto(another)) return false;
//		
//		boolean res = true;
//		
//		double cycle = siblings.size() / repeatnum;
//		double cycle2 = another.siblings.size() / another.repeatnum;
//		
//		if(cycle != cycle2) return false;
//		
//		for(int c = 0; c<cycle; c++) {
//			res = res && siblings.get(c).equalsto(another.siblings.get(c));
//		}
//		
//		return res;
//	}
	
	public boolean removeSiblingsNStartNode(int sIndex, int num) {
		//sIndex: the index to-be-removed DTNode in siblings, num: the number of to-be-removed sibling nodes
		//first: whether it is the first node
		boolean res = true;
		for(int i = 0; i<num; i++) {
			DTNode rem = siblings.get(sIndex);
			
			if(i == 0 && !StartNode.contains(rem)) {
				System.err.print("The first removing node is not a starting node of repeated pattern\n");
				System.exit(1);
			}
			
			if(StartNode.contains(rem)) {
				res = res && StartNode.remove(rem);
			}
			
			if(siblings.remove(sIndex) == null) {
				res = false;
			}
		}
		
		return res;
	}
	
//	public void removeSiblings(DTNode start) {
//		int index = StartNode.indexOf(start);
//		int deleteIndex = siblings.indexOf(start);
//		
//		if(index == -1 || deleteIndex == -1) {
//			System.err.print("We are removing a non-exist DTNode from siblings and StartNode Vectors\n");
//			System.exit(1);
//		}
//		
//		DTNode nextNode = null;
//		int nextDelIndex = siblings.size();
//				
//		if(index < StartNode.size()-1) {
//			nextNode = StartNode.get(index+1);
//			nextDelIndex = siblings.indexOf(nextNode);
//		}
//		
//		
//		for(int i = 0; i<nextDelIndex-deleteIndex; i++) {
//			siblings.remove(deleteIndex);
//		}
//		StartNode.remove(index);
//	}
	
	public void addSiblingsNStartNode(int sIndex, DTNode node, boolean first) {
		if(first) {
			addStartNode(sIndex, node);
		}
		
		siblings.add(sIndex, node);
	}

	
	private void addStartNode(int sIndex, DTNode node) {
		//sIndex: the to-be-inserted index in siblings
		if(sIndex < siblings.size()) {
			DTNode nextNode = siblings.get(sIndex);
			int StartIndex = 0;
			if(StartNode.size()>0) {
				StartIndex = StartNode.indexOf(nextNode);
			}
					
			
			if(StartIndex == -1) {
				System.err.print("the starting node in siblings is inconsistent with the StartNode Vector\n");
				System.exit(1);
			}
			
			StartNode.add(StartIndex, node);
		}
		else {
			StartNode.add(node);
		}
	}
	
	public boolean hasChildOf(DTNode child) {
		boolean res = false;
		
		if(this == child) return true;
		
		for(DTNode node : siblings) {
			res = res || node.hasChildOf(child);
		}
		
		return res;
	}
	
//	public int shrinkTree(final LGPIndividual4Grammar ind,
//			final EvolutionState state, 
//			final int thread) {
//		//gradually reduce the siblings number, return the new leave number (number of instruction)
//		
//		
//		
//		for(DTNode node : siblings) {
//			
//		}
//		
//		if(maxrepeatnum > 1 && )
//		repeatnum--;
//		
//		if(cutORgrow < 0) {
//			
//		}
//		else if (cutORgrow > 0 && repeatnum<maxrepeatnum) {
//			repeatnum += state.random[thread].nextInt(maxrepeatnum - repeatnum) + 1;
//		}
//		else {
//			repeatnum = state.random[thread].nextInt(maxrepeatnum) + 1;
//		}
//		
//		
//	}
}
