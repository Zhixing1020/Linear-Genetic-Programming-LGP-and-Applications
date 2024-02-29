package zhixing.cpxInd.algorithm.Grammar.individual;

import java.util.ArrayList;

import org.spiderland.Psh.booleanStack;
import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.gp.GPTree;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.DerivationRule;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.Grammarrules;
import zhixing.cpxInd.algorithm.Grammar.grammarrules.ModuleConstraint;

public class DerivationTree {

	public DTNode root = null;
	
	public int treeDepth;
	
	public int numLeaves;  //count the number of instructions covered by this tree

	public DerivationTree newTree(final LGPIndividual4Grammar ind,
			final EvolutionState state,
			final int thread,
			final int numInstrMax,
			final int numInstrMin) {
		if(root == null) root = new DTNode();
		
		root.initialize(null);
		root.moduleName = ind.RULE.PROGRAM;
		
		root.growNodeBasedGram(ind, state, thread, ind.RULE, root, ind.moduleLibrary, root.siblings.size(), 0);
		numLeaves = root.getLeavesNum();
		
//		for(int t = 0; t<ind.getMaxNumTrees(); t++) { //try multiple times until the program size is legal
//			if(numLeaves >= numInstrMin && numLeaves <= numInstrMax) break;
//			
//			root.initialize(null);
//			root.moduleName = ind.RULE.PROGRAM;
//			
//			if(numLeaves < numInstrMin) {
//				root.growNodeBasedGram(ind, state, thread, ind.RULE, root, ind.moduleLibrary, root.siblings.size(), 1);
//				numLeaves = root.getLeavesNum();
//			}
//			else {
//				root.growNodeBasedGram(ind, state, thread, ind.RULE, root, ind.moduleLibrary, root.siblings.size(), -1);
//				numLeaves = root.getLeavesNum();
//			}
//		}
//		
//		if(numLeaves < numInstrMin || numLeaves > numInstrMax) { //the program size is illegal after adjusting
//			System.err.print("The program size is out of [minProgsize, maxProgsize] after many times trial\n");
//			System.exit(1);
//		}

		return this;
	}
	
	
	
	
	public Object clone(){
		DerivationTree obj = new DerivationTree();
		
		obj.root = (DTNode) this.root.clone();
		
		obj.treeDepth = obj.root.getTreeDepth();
		
		obj.numLeaves = obj.root.getLeavesNum();
		
		//rematch (or re-link) the derivation tree leaves to instruction
		
		return obj;
	}
	
	public Object lightClone() {
		DerivationTree obj = new DerivationTree();
		
		if(this.root!=null){
			obj.root = (DTNode) this.root.lightClone();
			obj.treeDepth = this.root.getTreeDepth();
			obj.numLeaves = this.root.getLeavesNum();
		}
		
      return obj;  // note that the root child reference is copied, not cloned
	}
	
	public int rematch(LGPIndividual4Grammar ind) {
		//connect the derivation tree with the individual ind
		ind.updateStatus();
		this.treeDepth = this.root.getTreeDepth();
		this.numLeaves = this.root.getLeavesNum();
		
		if(root != ind.DTree.root) {
			System.err.println("make sure the derivation tree is belong to the individual.");
			System.exit(1);
		}
		
		if(this.numLeaves != ind.getTreesLength()) {
			System.err.println("make sure the derivation tree is coincident with the instruction list.");
			System.exit(1);
		}
				
		int res = rematch_recur(root, 0, ind);
				
		return res;
	}
	
	protected int rematch_recur(DTNode node, int instrInd, LGPIndividual4Grammar ind) {
		if(node.siblings.size()>0) {
			for(DTNode n : node.siblings) {
				instrInd = rematch_recur(n, instrInd, ind);
			}
		}
		else {
			node.instructionChildren.clear();
			for(int i = 0; i<node.repeatnum; i++) {
				node.instructionChildren.add((GPTreeStructGrammar)ind.getTreeStruct(instrInd));
				((GPTreeStructGrammar)ind.getTreeStruct(instrInd)).grammarNode = node;
				instrInd ++;
			}
			
		}
		return instrInd;
	}
	
	public int numNodes(String nodesearch) {
		//nodesearch: a certain kind of node (i.e., module name)
		return root.getTreeNodeNum(nodesearch);
	}
	
	public boolean contains_pointer(final DTNode node, DTNode cur) {
		//check whether the tree contains the certain DTNode (same memory address).
		
		boolean res = false;
		
		if(cur == node) {
			res = true;
		}
		else {
			for(DTNode n : cur.siblings) {
				res = res || contains_pointer(node, n);
				if(res) break;
			}
		}
		
		return res;
	}
	
	public DTNode randomlyPickNode(EvolutionState state, int thread, String moduletype, boolean pickeff) {
		int count = numNodes(moduletype);
		DTNodeGatherer res = new DTNodeGatherer();
		if(count==0) {
			return null;
		}
		
		int target = state.random[thread].nextInt(count);
		root.nodeInPosition(0, moduletype, res, target);
		
		if(pickeff) {
			for(int t = 0; t<count; t++) {
				if( ! res.node.containEffectiveInstruction()) {
					target ++;
					root.nodeInPosition(0, moduletype, res, target);
				}
			}
		}
		
//		if(target == 0 && moduletype == null) {//if we consider all DTNodes (moduletype == null) and we pick the root (target == 0)
//			target = state.random[thread].nextInt(count - 1) + 1;
//		}
//		
//		if(moduletype!=null && moduletype.equals(Grammarrules.PROGRAM)) {
//			System.err.println("we don't pick root node (i.e., PROGRAM node)");
//			System.exit(1);
//		}
		
		
		
		
		return res.node;
	}
	
//	public DTNode randomlyPickNode(EvolutionState state, int thread, String moduletype, 
//			LGPIndividual4Grammar ind, int start, int range, ArrayList<DTNode> used) {
//		int count = numNodes(moduletype);
//		DTNodeGatherer res = new DTNodeGatherer();
//		if(count==0) {
//			return null;
//		}
//		
//		int startInd = state.random[thread].nextInt(count);
//		for(int i = 0;i<count;i++) {
//			root.nodeInPosition(0, moduletype, res, startInd);
//			
//			if(used.contains(res.node)) {startInd = (++startInd)%count; continue;}
//			
//			//check whether the to-be-selected node overlap with the selected instruction segment (specified by [start, start+range])
//			ArrayList<GPTreeStructGrammar> instrlist = ind.getInstructionList(res.node);
//			if(ind.getTreeStructs().indexOf(instrlist.get(0))>start+range 
//					|| ind.getTreeStructs().indexOf(instrlist.get(instrlist.size() - 1))<start) {startInd = (++startInd)%count;continue;}
//			
//			
//			startInd = (++startInd)%count;
//			break;
//		}
//		
//		root.nodeInPosition(0, moduletype, res, startInd);
//		
//		return res.node;
//	}
	
	public DTNode randomlyPickReplaceNode(EvolutionState state, int thread, final LGPIndividual4Grammar ind, boolean pickeff) {
		int count = numNodes(null);
		DTNodeGatherer res = new DTNodeGatherer();
		if(count==0) {
			return null;
		}
		
		//collect possible replacing DTNodes
		ArrayList<Integer> candidate = new ArrayList();
		ArrayList<Double> possiblity = new ArrayList();
		double sumMaxrepeat = 0;
		count = numNodes(null);
		int startInd = state.random[thread].nextInt(count);
		for(int i = 0;i<count;i++) {
			root.nodeInPosition(0, null, res, startInd);
			
			String moduleName = res.node.moduleName;
			if(moduleName.equals(DerivationRule.INSTRUCTION) || moduleName.equals(DerivationRule.COMPOSITE)) {
				startInd = (++startInd)%count;
				continue;
			}
			ModuleConstraint mc = ind.getGrammarrules().getModuleConstraint(ind.getModuleConLibrary(), moduleName);
			
			if(mc.derivation_siblings.size()>1 && !candidate.contains(i) && (!pickeff || res.node.containEffectiveInstruction())){
				candidate.add(startInd);
				
				possiblity.add((double) mc.derivation_siblings.size());
				sumMaxrepeat += (double) mc.derivation_siblings.size();
				
			}
			
			startInd = (++startInd)%count;
			
		}
		
		if(candidate.isEmpty()) {
			return null;
		}
		else {
//			int target = candidate.get(state.random[thread].nextInt(candidate.size()));
//			root.nodeInPosition(0, moduletype, res, target);
			
			double p = state.random[thread].nextDouble();
			int target = 0;
			double sum = 0;
			for(;target<candidate.size()-1;target++) {
				sum += possiblity.get(target)/sumMaxrepeat;
				if(p<=sum) {
					break;
				}
			}
			root.nodeInPosition(0, null, res, candidate.get(target));
			
			return res.node;
		}
	}
	
	public DTNode randomlyPickNodeLength(EvolutionState state, int thread, String moduletype, int minlength, boolean pickeff) {
		int count = numNodes(moduletype);
		DTNodeGatherer res = new DTNodeGatherer();
		if(count==0) {
			return null;
		}
		
		//collect possible extendable DTNodes
		ArrayList<Integer> candidate = new ArrayList();
		ArrayList<Double> possiblity = new ArrayList();
		double sumMaxrepeat = 0;
		for(int i = 0;i<numNodes(null);i++) {
			root.nodeInPosition(0, moduletype, res, i);
			
			if(res.node.getLeavesNum()>=minlength && !candidate.contains(i) && (!pickeff || res.node.containEffectiveInstruction())) {
				candidate.add(i);				
			}
		}
		
		if(candidate.isEmpty()) {
			return null;
		}
		else {
			int target = candidate.get(state.random[thread].nextInt(candidate.size()));
			root.nodeInPosition(0, moduletype, res, target);
			
			
			return res.node;
		}

	}
	
	public DTNode randomlyPickExtendNode(EvolutionState state, int thread, String moduletype, int thresold, boolean pickeff) {
		int count = numNodes(moduletype);
		DTNodeGatherer res = new DTNodeGatherer();
		if(count==0) {
			return null;
		}
		
		//collect possible extendable DTNodes
		ArrayList<Integer> candidate = new ArrayList();
		ArrayList<Double> possiblity = new ArrayList();
		double sumMaxrepeat = 0;
		count = numNodes(null);
		int startInd = state.random[thread].nextInt(count);
		for(int i = 0;i<count;i++) {
			root.nodeInPosition(0, moduletype, res, startInd);
			
			if(res.node.maxrepeatnum>thresold && !candidate.contains(i) && (!pickeff || res.node.containEffectiveInstruction())) {
				candidate.add(startInd);
				
				possiblity.add((double) res.node.repeatnum);
				sumMaxrepeat += res.node.repeatnum;
				
			}
			
			startInd = (++startInd)%count;
		}
		
		if(candidate.isEmpty()) {
			return null;
		}
		else {
//			int target = candidate.get(state.random[thread].nextInt(candidate.size()));
//			root.nodeInPosition(0, moduletype, res, target);
			
			double p = state.random[thread].nextDouble();
			int target = 0;
			double sum = 0;
			for(;target<candidate.size()-1;target++) {
				sum += possiblity.get(target)/sumMaxrepeat;
				if(p<=sum) {
					break;
				}
			}
			root.nodeInPosition(0, moduletype, res, candidate.get(target));
			
			return res.node;
		}

	}
	
	public DTNode randomlyPickExtendNode(EvolutionState state, int thread, String moduletype, int thresold, ArrayList<DTNode> used, boolean pickeff) {
		int count = numNodes(moduletype);
		DTNodeGatherer res = new DTNodeGatherer();
		if(count==0) {
			return null;
		}
		
		//collect possible extendable DTNodes
		ArrayList<Integer> candidate = new ArrayList();
		ArrayList<Double> possiblity = new ArrayList();
		double sumMaxrepeat = 0;
		count = numNodes(null);
		int startInd = state.random[thread].nextInt(count);
		for(int i = 0;i<count;i++) {
			root.nodeInPosition(0, moduletype, res, startInd);
			
			if(used.contains(res.node)) {startInd = (++startInd)%count; continue;}
			
			boolean overlap = false;
			for(DTNode node : used) {
    			if(node.hasChildOf(res.node)||res.node.hasChildOf(node)) {
    				overlap = true;
    				break;
    			}
    		}
			if(overlap) {
				startInd = (++startInd)%count; continue;
			}
			
			if(res.node.maxrepeatnum>thresold && !candidate.contains(i) && (!pickeff || res.node.containEffectiveInstruction())) {
				candidate.add(startInd);
				
				possiblity.add((double) res.node.repeatnum);
				sumMaxrepeat += res.node.repeatnum;
				
			}
			
			startInd = (++startInd)%count;
			
		}
		
		if(candidate.isEmpty()) {
			return null;
		}
		else {
//			int target = candidate.get(state.random[thread].nextInt(candidate.size()));
//			root.nodeInPosition(0, moduletype, res, target);
			
			double p = state.random[thread].nextDouble();
			int target = 0;
			double sum = 0;
			for(;target<candidate.size()-1;target++) {
				sum += possiblity.get(target)/sumMaxrepeat;
				if(p<=sum) {
					break;
				}
			}
			root.nodeInPosition(0, moduletype, res, candidate.get(target));
			
			return res.node;
		}

	}
	
	public DTNode randomlyPickFixNode(EvolutionState state, int thread, String moduletype, int thresold, boolean pickeff) {
		//Fix: it is specified by only one "instr" grammar unit whose maximum repeat == 1 
		int count = numNodes(moduletype);
		DTNodeGatherer res = new DTNodeGatherer();
		if(count==0) {
			return null;
		}
		
		//collect possible extendable DTNodes
		ArrayList<Integer> candidate = new ArrayList();
		ArrayList<Double> possiblity = new ArrayList();
		double sumMaxrepeat = 0;
		count = numNodes(null);
		int startInd = state.random[thread].nextInt(count);
		for(int i = 0;i<count;i++) {
			root.nodeInPosition(0, moduletype, res, startInd);
			
			//if(used.contains(res.node)) {startInd = (++startInd)%count; continue;}
			
//			boolean overlap = false;
//			for(DTNode node : used) {
//    			if(node.hasChildOf(res.node)||res.node.hasChildOf(node)) {
//    				overlap = true;
//    				break;
//    			}
//    		}
//			if(overlap) {
//				startInd = (++startInd)%count; continue;
//			}
			
			if(res.node.maxrepeatnum == 1 && res.node.moduleName.equals(DerivationRule.INSTRUCTION) 
					&& !candidate.contains(i) && (!pickeff || res.node.containEffectiveInstruction())) {
				candidate.add(startInd);
				
				possiblity.add((double) res.node.repeatnum);
				sumMaxrepeat += res.node.repeatnum;
				
			}
			
			startInd = (++startInd)%count;
			
		}
		
		if(candidate.isEmpty()) {
			return null;
		}
		else {
//			int target = candidate.get(state.random[thread].nextInt(candidate.size()));
//			root.nodeInPosition(0, moduletype, res, target);
			
			double p = state.random[thread].nextDouble();
			int target = 0;
			double sum = 0;
			for(;target<candidate.size()-1;target++) {
				sum += possiblity.get(target)/sumMaxrepeat;
				if(p<=sum) {
					break;
				}
			}
			root.nodeInPosition(0, moduletype, res, candidate.get(target));
			
			return res.node;
		}

	}
	
//	public DTNode randomlyPickExtendNode(EvolutionState state, int thread, String moduletype, int thresold, 
//			LGPIndividual4Grammar ind, int start, int range, ArrayList<DTNode> used) {
//		int count = numNodes(moduletype);
//		DTNodeGatherer res = new DTNodeGatherer();
//		if(count==0) {
//			return null;
//		}
//		
//		//collect possible extendable DTNodes
//		ArrayList<Integer> candidate = new ArrayList();
//		ArrayList<Double> possiblity = new ArrayList();
//		double sumMaxrepeat = 0;
//		count = numNodes(null);
//		int startInd = state.random[thread].nextInt(count);
//		for(int i = 0;i<count;i++) {
//			root.nodeInPosition(0, moduletype, res, startInd);
//			
//			if(used.contains(res.node)) {startInd = (++startInd)%count; continue;}
//			
//			//check whether the to-be-selected node overlap with the selected instruction segment (specified by [start, start+range])
//			ArrayList<GPTreeStructGrammar> instrlist = ind.getInstructionList(res.node);
//			if(ind.getTreeStructs().indexOf(instrlist.get(0))>start+range 
//					|| ind.getTreeStructs().indexOf(instrlist.get(instrlist.size() - 1))<start) {startInd = (++startInd)%count; continue;}
//			
//			
//			if(res.node.maxrepeatnum>thresold && !candidate.contains(i)) {
//				candidate.add(startInd);
//				
//				possiblity.add((double) res.node.repeatnum);
//				sumMaxrepeat += res.node.repeatnum;
//				
//			}
//			
//			startInd = (++startInd)%count;
//			
//		}
//		
//		if(candidate.isEmpty()) {
//			return null;
//		}
//		else {
////			int target = candidate.get(state.random[thread].nextInt(candidate.size()));
////			root.nodeInPosition(0, moduletype, res, target);
//			
//			double p = state.random[thread].nextDouble();
//			int target = 0;
//			double sum = 0;
//			for(;target<candidate.size()-1;target++) {
//				sum += possiblity.get(target)/sumMaxrepeat;
//				if(p<=sum) {
//					break;
//				}
//			}
//			root.nodeInPosition(0, moduletype, res, candidate.get(target));
//			
//			return res.node;
//		}
//
//	}
	
	public DTNode randomlyPickCompatibleNode(EvolutionState state, int thread, DTNode like, boolean pickeff) {
		int count = numNodes(null);
		DTNodeGatherer res = new DTNodeGatherer();
		if(count==0) {
			return null;
		}
		

		//collect possible compatible DTNodes
		ArrayList<Integer> candidate = new ArrayList();
		ArrayList<Double> possiblity = new ArrayList();
		double sumMaxrepeat = 0;
		count = numNodes(null);
		int startInd = state.random[thread].nextInt(count);
		for(int i = 0;i<count;i++) {
			root.nodeInPosition(0, null, res, startInd);
			
			if(res.node.compatiableto(like) && !candidate.contains(i) && (!pickeff || res.node.containEffectiveInstruction())) {
				candidate.add(startInd);
				possiblity.add((double) res.node.repeatnum);
				sumMaxrepeat += res.node.repeatnum;
				
			}
			
			startInd = (++startInd)%count;
		}
		
		if(candidate.isEmpty()) {
			return null;
		}
		else {
//			int target = candidate.get(state.random[thread].nextInt(candidate.size()));
//			root.nodeInPosition(0, null, res, target);
			
			double p = state.random[thread].nextDouble();
			int target = 0;
			double sum = 0;
			for(;target<candidate.size()-1;target++) {
				sum += possiblity.get(target)/sumMaxrepeat;
				if(p<=sum) {
					break;
				}
			}
			root.nodeInPosition(0, null, res, candidate.get(target));
			
			return res.node;
		}
		
	}
	
	public DTNode randomlyPickCompatibleNode(EvolutionState state, int thread, DTNode like, ArrayList<DTNode> used, boolean pickeff) {
		int count = numNodes(null);
		DTNodeGatherer res = new DTNodeGatherer();
		if(count==0) {
			return null;
		}

		//collect possible compatible DTNodes
		ArrayList<Integer> candidate = new ArrayList();
		ArrayList<Double> possiblity = new ArrayList();
		double sumMaxrepeat = 0;
		count = numNodes(null);
		int startInd = state.random[thread].nextInt(count);
		for(int i = 0;i<count;i++) {
			root.nodeInPosition(0, null, res, startInd);
			
			if(used.contains(res.node)) {startInd = (++startInd)%count; continue;}
			
			boolean overlap = false;
			for(DTNode node : used) {
    			if(node.hasChildOf(res.node)||res.node.hasChildOf(node)) {
    				overlap = true;
    				break;
    			}
    		}
			if(overlap) {
				startInd = (++startInd)%count; continue;
			}
			
			if(res.node.compatiableto(like) && !candidate.contains(i) && (!pickeff || res.node.containEffectiveInstruction())) {
				candidate.add(startInd);
				possiblity.add((double) res.node.repeatnum);
				sumMaxrepeat += res.node.repeatnum;
				
			}
			
			startInd = (++startInd)%count;
		}
		
		if(candidate.isEmpty()) {
			return null;
		}
		else {
//			int target = candidate.get(state.random[thread].nextInt(candidate.size()));
//			root.nodeInPosition(0, null, res, target);
			
			double p = state.random[thread].nextDouble();
			int target = 0;
			double sum = 0;
			for(;target<candidate.size()-1;target++) {
				sum += possiblity.get(target)/sumMaxrepeat;
				if(p<=sum) {
					break;
				}
			}
			root.nodeInPosition(0, null, res, candidate.get(target));
			
			return res.node;
		}
		
	}
	
	
	
	public String printTreeDOI() {
		return root.makeGraphvizTree();
	}
	
	public void printTreeForHumans(final EvolutionState state, final int log) {
		state.output.print(root.makeGraphvizTree(), log);
		state.output.println("",log);
	}
}
