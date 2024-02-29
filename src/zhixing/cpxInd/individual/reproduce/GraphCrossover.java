package zhixing.cpxInd.individual.reproduce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.gp.koza.CrossoverPipeline;
import ec.util.Parameter;
import yimei.jss.gp.terminal.JobShopAttribute;
import yimei.jss.gp.terminal.TerminalERC;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.djss.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA4DJSS;
import zhixing.djss.individual.primitive.*;
import zhixing.cpxInd.individual.LGPIndividual;

public class GraphCrossover extends CrossoverPipeline{
	public static final String GRAPH_CROSSOVER = "graph_cross";
	
	public static final String P_MICROMUTBASE = "micro_base";
	
	public static final String P_MAXGRAPHDIFF = "max_graph_diff";
	
	public static final String P_MAXGRAPH_SIZE = "maxgraphsize";
	
	public static final String P_MAXDIS_CROSS_POINT = "maxdistancecrosspoint";
	
	public static final String P_PROTECT_SUB_GRAPH = "protect_sub_graph";
	
	//public static final String P_PARTIALGRAPHRATE = "min_partial_graph_rate";
	
	protected LGPMicroMutationPipeline microMutation;
	
	public int MaxGraphDiff;
	
	public int ProtectSubGraph;
	
	public int MaxGraphSize;
	
	public int MaxDistanceCrossPoint;
	
	//public double PartialSubGraphRate;
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = LGPDefaults.base().push(GRAPH_CROSSOVER);
        
        Parameter microbase = new Parameter(state.parameters.getString(base.push(P_MICROMUTBASE), def.push(P_MICROMUTBASE)));
        microMutation = null;
        if(!microbase.toString().equals("null")){
        	microMutation = new LGPMicroMutationPipeline();
   		 microMutation.setup(state, microbase);
        }
        
        MaxGraphDiff =  state.parameters.getInt(base.push(P_MAXGRAPHDIFF),def.push(P_MAXGRAPHDIFF),1);
        if (MaxGraphDiff<0)
            state.output.fatal("GraphCrossover Pipeline has an invalid maximum difference of graph size (it must be >= 0).",base.push(P_MAXGRAPHDIFF),def.push(P_MAXGRAPHDIFF));
        
//        PartialSubGraphRate = state.parameters.getDoubleWithDefault(base.push(P_PARTIALGRAPHRATE),def.push(P_PARTIALGRAPHRATE),1.0);
//        if (PartialSubGraphRate<0)
//            state.output.fatal("LGPCrossover Pipeline has an invalid maximum distance of crossover points (it must be >= 0).",base.push(P_PARTIALGRAPHRATE),def.push(P_PARTIALGRAPHRATE));
        
        ProtectSubGraph = state.parameters.getIntWithDefault(base.push(P_PROTECT_SUB_GRAPH), def.push(P_PROTECT_SUB_GRAPH), 1);
        
        MaxGraphSize = state.parameters.getIntWithDefault(base.push(P_MAXGRAPH_SIZE), def.push(P_MAXGRAPH_SIZE), 10);
        if(MaxGraphSize < 0){
        	state.output.fatal("GraphCrossover Pipeline has an invalid maximum segment length (it must be >= 0).",base.push(P_MAXGRAPH_SIZE), def.push(P_MAXGRAPH_SIZE));
        }
        
        MaxDistanceCrossPoint =  state.parameters.getInt(base.push(P_MAXDIS_CROSS_POINT),def.push(P_MAXDIS_CROSS_POINT),0);
        if (MaxDistanceCrossPoint<0)
            state.output.fatal("GraphCrossover Pipeline has an invalid maximum distance of crossover points (it must be >= 0).",base.push(P_MAXDIS_CROSS_POINT),def.push(P_MAXDIS_CROSS_POINT));
	}
	
	@Override
    public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) 

    {
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        for(int q=start, parnt = 0;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
            // grab two individuals from our sources
            if (sources[0]==sources[1])  // grab from the same source
                sources[0].produce(2,2,0,subpopulation,parents,state,thread);
            else // grab from different sources
                {
                sources[0].produce(1,1,0,subpopulation,parents,state,thread);
                sources[1].produce(1,1,1,subpopulation,parents,state,thread);
                }
            
            // at this point, parents[] contains our two selected individuals
            
            LGPIndividual[] parnts = new LGPIndividual[2];
        	for(int ind = 0 ; ind < parnts.length; ind++){
        		parnts[ind] = (LGPIndividual) this.parents[ind]; 
        	}
            q += this.produce(min, max, start, subpopulation, inds, state, thread, parnts);            

            }
            
        return n;
    }
	
	
	public int produce(final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread,
	        final LGPIndividual[] parents) {
		// how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
        
     // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

        GPInitializer initializer = ((GPInitializer)state.initializer);
        
        for(int q=start, parnt = 0;q<n+start; /* no increment */)  // keep on going until we're filled up
        {
        
	        // at this point, parents[] contains our two selected individuals
	
	        // are our tree values valid?
	        if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= parents[0].getTreesLength()))
	            // uh oh
	            state.output.fatal("LGP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
	        if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= parents[1].getTreesLength()))
	            // uh oh
	            state.output.fatal("LGP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
	
	        int t1=0, t2=0;
	        LGPIndividual j1, j2;
	        j1 = ((LGPIndividual)parents[parnt]).lightClone();
	    	t1 = parnt;
	        j2 = ((LGPIndividual)parents[(parnt + 1)%parents.length]).lightClone();
	        t2 = (parnt + 1)%parents.length;
	        
	        boolean flag = false;
	        ArrayList<Integer> cand1 = new ArrayList<>();
	        ArrayList<Integer> cand2 = new ArrayList<>();
	        int maxdistance = 0;
	        
	        for(int t = 0;t<numTries;t++){
	        	//select a target output register
	        	int target1 = state.random[thread].nextInt(((LGPIndividual) parents[parnt]).getRegisters().length);
	        	int target2 = state.random[thread].nextInt(((LGPIndividual) parents[parnt + 1]).getRegisters().length);
	        	
	        	//collect the class graph for the target output register (updateStatus)
	        	Integer tar1[] = new Integer[1];
	        	tar1[0] = target1;  
	        	Integer tar2[] = new Integer[1];
	        	tar2[0] = target2; 
	        	
	            int begin1=j1.getTreesLength(), begin2=j2.getTreesLength(), tt = 0;
	            for(;tt<numTries;tt++){
	            	begin1 = state.random[thread].nextInt(j1.getTreesLength());
	            	begin2 = state.random[thread].nextInt(j2.getTreesLength());
	            	if(Math.abs(begin1 - begin2)<=MaxDistanceCrossPoint) break;
	            }
	            //int pickNum1 = state.random[thread].nextInt(j1.getTreesLength()) + 1;
	            //int pickNum2 = state.random[thread].nextInt(j2.getTreesLength()) + 1;
	            
	            //pick two instruction segments and skip the ineffective instructions
	//            Integer tar1[] = j1.getTreeStruct(begin1).effRegisters.toArray(new Integer [0]);
	//        	Integer tar2[] = j2.getTreeStruct(begin2).effRegisters.toArray(new Integer [0]);
	
	            cand1 = j1.getSubGraph(begin1, tar1);
	            cand2 = j2.getSubGraph(begin2, tar2);
	            
	//            ArrayList<Integer> tmp_cand1 = j1.getSubGraph(begin1, tar1);
	//            ArrayList<Integer> tmp_cand2 = j2.getSubGraph(begin2, tar2);
	        	
	//            double rate = PartialSubGraphRate + state.random[thread].nextDouble()*PartialSubGraphRate;
	//        	cand1 = j1.getPartialSubGraph(begin1, tar1, rate, state, thread);
	//        	cand2 = j2.getPartialSubGraph(begin2, tar2, rate, state, thread);
	        	
	        	Iterator<Integer> it = cand1.iterator();
	        	int cnt = 0;
	        	int GraphSize = 1 + state.random[thread].nextInt(MaxGraphSize);
	        	while(it.hasNext()){
	        		int ind = it.next();
	        		cnt ++;
	        		if(cnt > GraphSize){
	        			it.remove();
	        		}
	        	}
	        	it = cand2.iterator();
	        	cnt = 0;
	        	GraphSize = 1 + state.random[thread].nextInt(MaxGraphSize);
	        	while(it.hasNext()){
	        		int ind = it.next();
	        		cnt ++;
	        		if(cnt > GraphSize){
	        			it.remove();
	        		}
	        	}
	        	
	        	flag = verifypoint(j1, j2, cand1.size(), cand2.size());
	        	
	        	//boolean tmp_flag = verifypoint(j1, j2, tmp_cand1.size(), tmp_cand2.size());
	        	
	        	//int distance = graph_distance(j1,j2,tmp_cand1,tmp_cand2);
	        	
	        	if(flag) break;
	//        	if(tmp_flag && distance >= maxdistance){
	//        		cand1 = tmp_cand1;
	//        		cand2 = tmp_cand2;
	//        		maxdistance = distance;
	//        		flag = tmp_flag;
	//        	}
	        }
	        
	        if(flag){
	        	//replace j1's instructions by j2
	        	graphSwapping(j1,(LGPIndividual)parents[t2],cand1,cand2, state, thread);
	        }
	        
	        if(microMutation != null) j1 = (LGPIndividual) microMutation.produce(subpopulation, j1, state, thread);
	
	        // add the individuals to the population
	        inds[q] = j1;
	        q++;
	        parnt ++;
	        if (q<n+start && !tossSecondParent)
	        {
	        	if(flag){
	        		//replace j2's instructions by j1
	        		graphSwapping(j2,(LGPIndividual)parents[t1],cand2,cand1, state, thread);
	        	}
	        	if(microMutation != null) j2 = (LGPIndividual) microMutation.produce(subpopulation, j2, state, thread);
	        	
	            inds[q] = j2;
	            q++;
	            parnt ++;
	        }
        

        }
        
        return n;
	}
	
	protected void graphSwapping(LGPIndividual pr, LGPIndividual pd, ArrayList<Integer> cand1, ArrayList<Integer> cand2, EvolutionState state, int thread){
		//replace pr's instructions by pd
		
		
		//add pd's instructions
		for(int i = 0;i<cand2.size();i++){
    		int destin = 0;
    		if(cand1.size()>0){
    			destin = cand1.get(0);
    		}
    		
    		int source = cand2.get(i);
    		
    		GPTreeStruct tree = (GPTreeStruct) (pd.getTreeStruct(source).clone());
    		//tree = (GPTree)(parents[parnt].getTree(pick).lightClone());
            tree.owner = pr;
            tree.child = (GPNode)(pd.getTree(source).child.clone());
            tree.child.parent = tree;
            tree.child.argposition = 0;
            
            if(i == 0 && cand1.size()>0){//replace the index of WriteRegister  in case of the target registers are different
            	//((WriteRegisterGPNode)tree.child).setIndex(((WriteRegisterGPNode)pr.getTree(destin).child).getIndex());
            	pr.setTree(destin, tree);
            }
            else{
            	pr.addTree(destin + 1, tree);
            }
            
            pr.evaluated = false; 
		}
		
		//remove pr's instructions
		for(int i = 1;i<cand1.size();i++){ //i = 1, because the first instruction has been replaced by pd's instruction
			pr.removeTree(cand1.get(i));
			
			pr.evaluated = false;
		}

		//remove introns
		if(pr.getTreesLength() > pr.getMaxNumTrees()){
			int cnt = pr.getTreesLength() - pr.getMaxNumTrees();
			for(int k = 0; k<cnt; k++){
				int res = state.random[thread].nextInt(pr.getTreesLength());
				if(pr.getEffTreesLength() <= pr.getMaxNumTrees()){
					for(int x = 0;x < pr.getTreesLength();x++) {
		        		if(!pr.getTreeStruct(res).status){break;}
		        		res = (res + state.random[thread].nextInt(pr.getTreesLength())) % pr.getTreesLength();
		        	}
					
				}
				pr.removeTree(res);
			}
		}
	}
	
	protected boolean verifypoint(LGPIndividual j1, LGPIndividual j2, int ind1, int ind2){
		//ind1 and ind2 are respectively the number of instructions of the candidate instruction list
		//return true if ind1 and ind2 will not exceed the length constraints
		
		if(ind2 == ind1 && ind1 == 0){
			return false;
		}
		
		if(Math.abs(ind1 - ind2)>MaxGraphDiff)
			return false;
		
		boolean res = true;
		if(ind1 < ind2){
			int diff = ind2 - ind1;
			if(j1.getEffTreesLength() + diff > j1.getMaxNumTrees() || j2.getEffTreesLength() - diff < j2.getMinNumTrees())
				res = false;
		}
		else if (ind1 > ind2){
			int diff = ind1 - ind2;
			if(j2.getEffTreesLength() + diff > j2.getMaxNumTrees() || j1.getEffTreesLength() - diff < j1.getMinNumTrees())
				res = false;
		}
		return res;
	}
	
	protected int graph_distance(LGPIndividual j1, LGPIndividual j2, ArrayList<Integer> cand1, ArrayList<Integer> cand2){
		//breath-first search, append the indexes of primitives into a list, function index: numreg+index, register index: self index
		//the difference of the two lists is the distance
		
		Graph g1 = new Graph(j1, cand1);
		Graph g2 = new Graph(j2, cand2);
		
		return g1.graphDifference(g2);
	}
	
	
//	private void BFS_graph(LGPIndividual j, ArrayList<Integer> cand){
//		ArrayList<String> graph = new ArrayList<>();
//		Queue<Integer> queue = new LinkedList<>(); //for instruction indexes or -1*register index
//		
//		if(cand.size()>0){
//			graph.add(j.getTreeStruct(cand.get(0)).child.toString());
//			queue.offer(cand.get(0));
//		}
//		
//		for(int i = 0;i<cand.size() && !queue.isEmpty();i++){
//			//grab an instruction from a queue and insert the instruction function into the list
//			if(queue.peek()>=0){
//				GPTreeStruct tmp = j.getTreeStruct(queue.poll());
//				
//				graph.add(tmp.child.children[0].toString());
//				
//				
//				//put the children of the instruction into a queue
//				for(int r = 0;r<2;r++){
//					if(tmp.child.children[0].children[r] instanceof TerminalERC){
//						int index = ((AttributeGPNode)((TerminalERC)tmp.child.children[0].children[r]).getTerminal()).
//					}
//				}
//			}
//			else{
//				graph.add(JobShopAttribute.values()[queue.poll()*-1]);
//			}
//			
//		}
//	}
}


class Graph{
	public Graphnode root;
	
	public Graph(LGPIndividual j, ArrayList<Integer> cand){
		if(cand.size() > 0){
			root = buildGraph(j, cand);
		}
		else{
			root = null;
		}
	}
	
	public Graphnode buildGraph(LGPIndividual j, ArrayList<Integer> cand){		
		Graphnode root = buildSubGraph(j, cand, 0);
		return root;
	}
	
	public Graphnode buildSubGraph(LGPIndividual j, ArrayList<Integer> cand, int start){
		//s: start index of the slice of cand
		
		GPTreeStruct tmp = j.getTreeStruct(cand.get(start));
		Graphnode root = new Graphnode(tmp.child.children[0].toString());
		//root.name = tmp.child.children[0].toString();
		
		for(int r = 0;r<tmp.child.children[0].expectedChildren();r++){
			if(tmp.child.children[0].children[r] instanceof TerminalERC){
				//no more children
				root.children[r] = new Graphnode(tmp.child.children[0].children[r].toString());
			}
			else{
				//search starting index of the sub graph
				int tmp_start = -1;
				int register = ((ReadRegisterGPNode)tmp.child.children[0].children[r]).getIndex();
				for(int s = start+1;s<cand.size();s++){
					if(((WriteRegisterGPNode)j.getTreeStruct(cand.get(s)).child).getIndex() == register){
						tmp_start = s;
						break;
					}
				}
				
				if(tmp_start >=0){
					root.children[r] = buildSubGraph(j, cand, tmp_start);
				}
				else{
					//if no index, insert the name of readRegister
					root.children[r] = new Graphnode(tmp.child.children[0].children[r].toString());
				}
				
				
			}
		}
		
		return root;
	}
	
	public int size(){
		if(root == null)
			return 0;
		else{
			return root.getGraphSize();
		}
	}
	
	public int graphDifference(Graph j){
		return Graphnode.distance(this.root, j.root);
	}
}

class Graphnode{
	public String name;
	
	public Graphnode [] children;
	
	public Graphnode(String name){
		this.name = name;
		children = new Graphnode[2];
		children[0] = null;
		children[1] = null;
	}
	
	public Graphnode(){
		name = null;
		children = new Graphnode[2];
		children[0] = null;
		children[1] = null;
	}
	
	public int getGraphSize(){
		int res = 0;
		
		if(name != null){
			res = 1;
		}
		
		for(int r = 0;r<children.length;r++){
			if(children[r] != null){
				res += children[r].getGraphSize();
			}
		}
		
		return res;
	}
	
	public static int distance(Graphnode j1, Graphnode j2){
		int res = 0;
		
		if(j1 != null && j2 == null){
			return j1.getGraphSize();
		}
		else if(j2 != null && j1 == null){
			return j2.getGraphSize();
		}
		else if(j1 == null && j2 == null)
			return res;
		
		if(!j1.name.equals(j2.name)){
			res += 1;
		}
		for(int r = 0;r<2;r++){
			res += Graphnode.distance(j1.children[r], j2.children[r]);
		}
		
		return res;
	}
}
