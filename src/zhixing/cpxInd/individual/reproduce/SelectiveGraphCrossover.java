package zhixing.cpxInd.individual.reproduce;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.util.Parameter;
import zhixing.cpxInd.individual.LGPDefaults;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;

public class SelectiveGraphCrossover extends GraphCrossover{
	public static final String SEL_GRAPH_CROSSOVER = "selective_graph_cross";
	
	public static final String SEL_PREFERENCE = "select_perference"; //true: only keep the offspring receiving the better evolution material; false: keep both of offspring
	
	public static final String SEL_GRAPH = "select_graph";  //dependence degree (reusing rate), the correlation of intermediate semantics
	
	protected String [] offspring_dir;
	
	protected SelectionMethod s;
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = LGPDefaults.base().push(SEL_GRAPH_CROSSOVER);
		
		tossSecondParent = state.parameters.getBoolean(base.push(P_TOSS),
	            def.push(P_TOSS),false);
		
		//descend: pick the larger values,  ascend: pick the smaller values, specify two paradigms for the two parents.
        String select_offspring = state.parameters.getStringWithDefault(base.push(SEL_PREFERENCE), def.push(SEL_PREFERENCE), "ascend-descend");
        offspring_dir = select_offspring.split("-");
        for(int i = 0;i<offspring_dir.length;i++){
        	if(offspring_dir[i].equals("descend") || offspring_dir[i].equals("ascend")){
        		continue;
        	}
        	else{
        		System.err.print("invaild parameter value: " + offspring_dir[i] + " in SelectiveGraphCrossover\n");
        		System.exit(1);
        	}
        }
        
        //s = (SelectionMethod) state.parameters.getInstanceForParameter(base.push(SEL_GRAPH), def.push(SEL_GRAPH), null);
        s = new DependenceDegree();
        if(s == null){
        	s = new Random();
        }
	}
	
	@Override
	public int produce(final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread){
		// how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
        
        int return_n = 0; //actual number of returned offspring

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
            
            for(int t = 0;t<numTries;t++){
                int begin1 = s.pick(state, thread, j1, offspring_dir[0]);
                //int pickNum1 = state.random[thread].nextInt(j1.getTreesLength()) + 1;
                
                
                int begin2 = s.pick(state, thread, j2, offspring_dir[1]);
                //int pickNum2 = state.random[thread].nextInt(j2.getTreesLength()) + 1;
                
              //select a target output register
            	int target1 = ((WriteRegisterGPNode)parents[parnt].getTree(begin1).child).getIndex();
            	int target2 = ((WriteRegisterGPNode)parents[parnt+1].getTree(begin2).child).getIndex();
            	
            	//collect the class graph for the target output register (updateStatus)
            	Integer tar1[] = new Integer[1];
            	tar1[0] = target1;  
            	Integer tar2[] = new Integer[1];
            	tar2[0] = target2; 
                
                //pick two instruction segments and skip the ineffective instructions

            	cand1 = j1.getSubGraph(begin1, tar1);
            	cand2 = j2.getSubGraph(begin2, tar2);
            	
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
            	
            	if(flag) break;
            }
            
            if(flag){
            	//replace j1's instructions by j2
            	graphSwapping(j1,(LGPIndividual)parents[t2],cand1,cand2, state, thread);
//            	for(int i = 0;i<cand2.size();i++){
//            		int destin = 0;
//            		if(i<cand1.size()){
//            			destin = cand1.get(i);
//            		}
//            		
//            		int source = cand2.get(i);
//            		
//            		GPTree tree = (GPTree) (parents[t2].getTree(source).lightClone());
//            		//tree = (GPTree)(parents[parnt].getTree(pick).lightClone());
//                    tree.owner = j1;
//                    tree.child = (GPNode)(parents[t2].getTree(source).child.clone());
//                    tree.child.parent = tree;
//                    tree.child.argposition = 0;
//                    
//                    if(i<cand1.size()){
//                    	if(i == 0){//replace the index of WriteRegister
//                        	((WriteRegisterGPNode)tree.child).setIndex(((WriteRegisterGPNode)parents[t1].getTree(destin).child).getIndex());
//                        }
//                        
//                        j1.setTree(destin, tree);
//                    }
//                    else{
//                    	j1.addTree(0, tree);
//                    }
//                    
//                    //if the newly swapped instruction is not effective, remove the subsequent instruction whose WriteRegister is the same as it
//                    if(!j1.getTreeStruct(destin).status){
//                    	int removeI = destin+1;
//                    	while(!j1.getTreeStruct(destin).status && !cand1.isEmpty() && removeI < cand1.get(0)){
//                    		if(((WriteRegisterGPNode)j1.getTreeStruct(destin).child).getIndex() 
//                    				== ((WriteRegisterGPNode)j1.getTreeStruct(removeI).child).getIndex()){
//                    			j1.removeTree(removeI);
//                    			removeI --;
//                    		}
//                    		removeI ++;
//                    	}
//                    }
//                    
//                    j1.evaluated = false; 
//            	}
//            	if(cand1.size() > cand2.size()){
//            		//remove j1 based on cand1
//            		for(int i = cand2.size();i<cand1.size();i++){
//            			if(j1.getTreesLength() <= j1.getMinNumTrees()){
//            				break;
//            			}
//            			
//            			j1.removeTree(cand1.get(i));
//            			j1.evaluated = false;
//            		}
//            	}
            	
            }
            
            if(microMutation != null) j1 = (LGPIndividual) microMutation.produce(subpopulation, j1, state, thread);
        	
            // add the individuals to the population
            inds[q] = j1;
            q++;
            parnt ++;
            return_n ++;
            if (q<n+start && !tossSecondParent)
            {
            	if(flag){
            		//replace j2's instructions by j1
            		graphSwapping(j2,(LGPIndividual)parents[t1],cand2,cand1, state, thread);
//                	for(int i = 0;i<cand1.size();i++){
//                		
//                		int destin = 0; 
//                		if(i<cand2.size()){
//                			destin = cand2.get(i);
//                		}
//                		int source = cand1.get(i);
//                		
//                		GPTree tree = (GPTree) (parents[t1].getTree(source).lightClone());
//                		//tree = (GPTree)(parents[parnt].getTree(pick).lightClone());
//                        tree.owner = j2;
//                        tree.child = (GPNode)(parents[t1].getTree(source).child.clone());
//                        tree.child.parent = tree;
//                        tree.child.argposition = 0;
//                        
//                        if(i<cand2.size()){
//                        	if(i == 0){//replace the index of WriteRegister
//                            	((WriteRegisterGPNode)tree.child).setIndex(((WriteRegisterGPNode)parents[t2].getTree(destin).child).getIndex());
//                            }
//                            
//                            j2.setTree(destin, tree);
//                        }
//                        else{
//                        	j2.addTree(0, tree);
//                        }
//                        
//                      //if the newly swapped instruction is not effective, remove the subsequent instruction whose WriteRegister is the same as it
//                        if(!j2.getTreeStruct(destin).status){
//                        	int removeI = destin+1;
//                        	while(!j2.getTreeStruct(destin).status && !cand2.isEmpty() && removeI < cand2.get(0)){
//                        		if(((WriteRegisterGPNode)j2.getTreeStruct(destin).child).getIndex() 
//                        				== ((WriteRegisterGPNode)j2.getTreeStruct(removeI).child).getIndex()){
//                        			j2.removeTree(removeI);
//                        			removeI --;
//                        		}
//                        		removeI ++;
//                        	}
//                        }
//                        j2.evaluated = false; 
//                	}
//                	if(cand1.size() < cand2.size()){
//                		//remove j2 based on cand2
//                		for(int i = cand1.size();i<cand2.size();i++){
//                			if(j2.getTreesLength() <= j2.getMinNumTrees()){
//                				break;
//                			}
//                			
//                			j2.removeTree(cand2.get(i));
//                			j2.evaluated = false;
//                		}
//                	}
            	}
            	if(microMutation != null) j2 = (LGPIndividual) microMutation.produce(subpopulation, j2, state, thread);
            	
	            inds[q] = j2;
	            q++;
	            parnt ++;
	            return_n ++;
            }
            

            }
            
        return return_n;
	}
	
	
}


abstract class SelectionMethod {
	public abstract int pick(final EvolutionState state, int thread, LGPIndividual Ind, String direct); //pick an instruction index from Ind, based on the order and the probability.
	//ascend true: smaller value is preferred,  false (descend): larger value is preferred 
}

class DependenceDegree extends SelectionMethod {
	public int pick(final EvolutionState state, int thread, LGPIndividual Ind, String direct){
		//get the dependence degree for all indexes.
		ArrayList<Double> degreeList = Ind.getEffDegree();
		
		//get the robin-selection list
		ArrayList<Pair<Double, Integer>> robinList = new ArrayList<>();
		
		double sum = 0., distribution = 0.;
		for(Double d : degreeList){
			distribution += d;
		}
		for(int i = 0; i<degreeList.size(); i++){
			degreeList.set(i, degreeList.get(i)/(distribution + 1e-6));
		}
		
		int i = 0;
		for(Double d : degreeList){
			if(d>0){
				robinList.add(new Pair<Double, Integer>(d+sum, i));
				
				sum += d;
			}
			i++;
		}
		
		//roulette selection
		Double rnd = state.random[thread].nextDouble();
		int begin = state.random[thread].nextInt(Ind.getTreesLength());
		
		if(direct.equals("descend")){
			for(Pair<Double, Integer> p : robinList){
				if(p.getFirst() > rnd){
					begin = p.getSecond();
					break;
				}
			}
		}
		else{
			Collections.reverse(robinList);
			for(Pair<Double, Integer> p : robinList){
				if(p.getFirst() < rnd){
					begin = p.getSecond();
					break;
				}
			}
		}
		
		
		return begin;
	}
}

class Random extends SelectionMethod{
	public int pick(final EvolutionState state, int thread, LGPIndividual Ind, String direct){
		return state.random[thread].nextInt(Ind.getTreesLength());
	}
}

