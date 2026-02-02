package zhixing.cpxInd.algorithm.LandscapeOptimization.FLReductionLGP.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.gp.GPFunctionSet;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPNodeSelector;
import ec.gp.GPType;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.LandscapeOptimization.SubpopulationFLO;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Index;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexList;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexSymbolBuilder;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.Distance;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.IntervalDistance;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.LosingDistance;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.Objective4FLO;
import zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.indexing.Index4LGP;
import zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.indexing.IndexList4LGP;
//import zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.indexing.IndexList4LGP;
//import zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.indexing.SimpleLGPBuilder;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ConstantGPNode;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.cpxInd.individual.primitive.FlowOperator;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
import zhixing.cpxInd.individual.reproduce.LGPMutationGrowBuilder;

public class IndexList4LGP_FLR extends IndexList4LGP{
	//the index list for fitness landscape reduction
	//this list has a maximum size which is much smaller than the total number of symbols.
	//different from the fix-size list, FLR list adds symbols from the population
	//FLR list removes less effective symbols when its size exceeds the limitation
	//as FLR list cannot cover the whole search space, the getGenoVector, and the objective function have some adaptation
	
	public static final String INDEXLIST_FLR = "IndexList_FLR";
	public static final String MAXLISTSIZE = "maxListSize";
	public static final String P_NODESELECTOR = "nodeSelector";
	public static final String P_UPDATERATE = "random_update_rate";
//	public static final String P_BUILDER = "build";
	
	int maxlistsize = 1000; // the maximum size of the index list.
	
	private EvolutionState self_state; //as some functions' interfaces do not include state and thread. To make use of state and thread and be consistent
	//with the function interfaces, IndexList4LGP_FLR remembers these two variables beforehand.
	private int self_thread;
	
	/** How the pipeline chooses a subtree to mutate */
    public GPNodeSelector nodeselect;
    
    public GPNodeBuilder builder;
    
    public static int MAXNUMTRIES = 10;
    
    public static double rand_update_rate = 0.2;
    
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
//		super.setup(state, base);
		
		Parameter def = new Parameter(INDEXLIST_FLR);
		
		maxlistsize = state.parameters.getIntWithDefault(base.push(MAXLISTSIZE), def.push(MAXLISTSIZE), 1000);
		if(maxlistsize <=0) {
			state.output.fatal("the maximum size of index list in fitness landscape reduction must be at least 1", base.push(MAXLISTSIZE), def.push(MAXLISTSIZE));
		}
		
		nodeselect = (GPNodeSelector)
	            (state.parameters.getInstanceForParameter(base.push(P_NODESELECTOR),def.push(P_NODESELECTOR), GPNodeSelector.class));
	    nodeselect.setup(state,base.push(P_NODESELECTOR));

        builder = (GPNodeBuilder)(state.parameters.getInstanceForParameter(base.push(BUILDER),def.push(BUILDER), GPNodeBuilder.class));
        builder.setup(state,base.push(BUILDER));
        
        rand_update_rate = state.parameters.getDoubleWithDefault(base.push(P_UPDATERATE), def.push(P_UPDATERATE), 0.2);
		if(rand_update_rate <0 || rand_update_rate >1) {
			state.output.fatal("the random updating rate in fitness landscape reduction must be [0,1]", base.push(P_UPDATERATE), def.push(P_UPDATERATE));
		}
		
		parent_setup(state, base);
		
		
	}
	
	private void parent_setup(final EvolutionState state, final Parameter base) {
		Parameter def = new Parameter(INDEXLIST);
		
		prototype = (Index<GPTreeStruct>) state.parameters.getInstanceForParameter(base.push(ITEMPROTOTYPE), def.push(ITEMPROTOTYPE), Index.class);
		prototype.setup(state, base.push(ITEMPROTOTYPE));
		
		numobjectives = state.parameters.getInt(base.push(NUMOBJECTIVES), def.push(NUMOBJECTIVES), 1);
		if(numobjectives <=0) {
			state.output.fatal("the number of objectives for fitness landscape optimization must be at least 1", base.push(NUMOBJECTIVES), def.push(NUMOBJECTIVES));
		}
		objectives = new Objective4FLO[numobjectives];
		for(int obj = 0; obj < numobjectives; obj++) {
			objectives[obj] = (Objective4FLO) state.parameters.getInstanceForParameter(base.push(OBJECTIVES).push(""+obj), def.push(OBJECTIVES).push(""+obj), Objective4FLO.class);
		}
		
		coefficiency = new double [numobjectives];
		for(int obj = 0; obj < numobjectives; obj++) {
			coefficiency[obj] = state.parameters.getDoubleWithDefault(base.push(OBJECTIVES).push(""+obj).push(P_COEFFICIENCY), 
					def.push(OBJECTIVES).push(""+obj).push(P_COEFFICIENCY), 1.0);
			if(coefficiency[obj] <= 0) {
				state.output.fatal("the coefficiency of objectives for fitness landscape optimization must be larger than 0.0", base.push(OBJECTIVES).push(""+obj).push(P_COEFFICIENCY), 
						def.push(OBJECTIVES).push(""+obj).push(P_COEFFICIENCY));
			}
		}
		
		boardsize = new int [numobjectives];
		for(int obj = 0; obj < numobjectives; obj++) {
			boardsize[obj] = state.parameters.getIntWithDefault(base.push(OBJECTIVES).push(""+obj).push(P_BOARDSIZE), 
					def.push(OBJECTIVES).push(""+obj).push(P_BOARDSIZE), 10);
			if(boardsize[obj] <= 0) {
				state.output.fatal("the boardsize of objectives for fitness landscape optimization must be larger than 0.0", base.push(OBJECTIVES).push(""+obj).push(P_BOARDSIZE), 
						def.push(OBJECTIVES).push(""+obj).push(P_BOARDSIZE));
			}
		}
		
		numiterations = state.parameters.getInt(base.push(NUMITERATIONS), def.push(NUMITERATIONS), 1);
		if(numobjectives <=0) {
			state.output.fatal("the number of iterations for fitness landscape optimization must be at least 1", base.push(NUMITERATIONS), def.push(NUMITERATIONS));
		}
		
		step_rate = state.parameters.getDouble(base.push(P_STEP), def.push(P_STEP), 0.0);
		if(step_rate <= 0 || step_rate > 1) {
			state.output.fatal("the step for fitness landscape optimization must be larger than 0.0 and not larger than 1.0", base.push(P_STEP), def.push(P_STEP));
		}
		
		min_step = state.parameters.getDoubleWithDefault(base.push(P_MINSTEP), def.push(P_MINSTEP), 1.0);
		if(min_step <= 0) {
			state.output.fatal("the minimum step for fitness landscape optimization must be larger than 0.0", base.push(P_MINSTEP), def.push(P_MINSTEP));
		}
		
		batchsize = state.parameters.getIntWithDefault(base.push(P_BATCHSIZE), def.push(P_BATCHSIZE), 1);
		if(batchsize <= 0) {
			state.output.fatal("the batch size for fitness landscape optimization must be larger than 0.0", base.push(P_BATCHSIZE), def.push(P_BATCHSIZE));
		}
		
		initialize(state, 0);
	}
	
	@Override
	public void initialize(final EvolutionState state, final int thread) {

		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		GPType type = ((GPTreeStruct) prototype.sym_prototype).constraints(initializer).treetype;
		
//		ArrayList<GPTreeStruct> allsymbols = builder.enumerateSymbols(state, 
//				type, thread, 0, ((GPTreeStruct) prototype.sym_prototype).constraints(initializer).functionset);

		for(int i = 0; i<maxlistsize; i++) {
			
			//randomly generate a tree
			GPTreeStruct ins = ((GPTreeStruct) prototype.sym_prototype.lightClone());
			ins.owner = null; 
            ins.status = false;
            ins.effRegisters = new HashSet<Integer>(0);
            
			ins.buildTree(state, thread);
			
			//check duplication with existing symbols in the list
			boolean dup = false;
			for(Index<GPTreeStruct> nd : this ) {
				if(nd.isduplicated(ins)) {
					dup = true;
					
					boolean exist = false;
					for(GPTreeStruct sym : nd.symbols) {
						if(sym.toString().equals(ins.toString())) {
							exist = true;
							break;
						}
					}
					
					if(!exist) nd.addSymbol(ins);
					
					break;
				}
			}
			
			//if no duplication, add as a new index
			if(! dup) {
				Index<GPTreeStruct> nd = (Index<GPTreeStruct>) this.prototype.clone();
				
				nd.index = this.size();
				
				nd.addSymbol(ins);
				
				this.add(nd);
			}
			
		}
		
		
		ArrayList<Integer> toBeShuffleIndex = new ArrayList<>();
		for(int i = 0;i<this.size();i++) {
			toBeShuffleIndex.add(this.get(i).index);
		}
		
		shuffleIndex(state, thread, toBeShuffleIndex);
		
		for(int i = 0; i<this.size(); i++) {
			this.get(i).index = toBeShuffleIndex.get(i);
		}
		
		self_state = state;
		self_thread = thread;
		
		basic_initialize(state, thread);
	}
	
	@Override
	public void optimizeIndex(EvolutionState state, int thread, SubpopulationFLO subpop, Board board) {
		// update the indexes based on the new leading board
		
		//1. initialize the search
		//add the symbols from the current population into the list
//		updateSymbolsFromPop(state, thread, subpop);
		updateSymbolsFromRandPop(state, thread, subpop);
		updateSymbolsFromObjs(state, thread, subpop, board);
		
		//shuffle the indices to have better diversity on the FL
		for(int i = 0; i<rand_update_rate*this.size(); i++) {
			int samplei = state.random[thread].nextInt(this.size());
			int samplej = state.random[thread].nextInt(this.size());
			
			Index<GPTreeStruct> sample_itemi = this.get(samplei);
			Index<GPTreeStruct> sample_itemj = this.get(samplej);
			
			int tmp_index = sample_itemi.index;
			sample_itemi.index = sample_itemj.index;
			sample_itemj.index = tmp_index;
			
		}
		
		//rank the symbols based on their frequency in the current population
		int base = (int) Math.floor(this.size() / 2.0);
//		ShuffleIndexBasedFrequency(state, thread, subpop, base);
		
		
		//2. iteratively update the indexes
		double oldfit = 0, newfit = 0;
		IndexList4LGP_FLR newlist = (IndexList4LGP_FLR) this.cloneIndexList();
		
		for(int ep = 0; ep < 1; ep++) {
			boolean [] priorityItem = new boolean [this.size()];
			//set coefficiency of each objectives
			for(int ob = 0; ob < this.numobjectives; ob++) {
//				objectives[ob].preprocessing(state, thread, this, board, boardsize[ob], batchsize);
//				objectives[ob].setPrivateCoefficiency(this, board);
				
				if(objectives[ob] instanceof Distance || objectives[ob] instanceof LosingDistance || objectives[ob] instanceof IntervalDistance /*|| objectives[ob] instanceof Navigation */) {
					boolean [] tmp = objectives[ob].getUsedItem();
					for(int i = 0; i<tmp.length; i++) {
						priorityItem[i] = priorityItem[i] || tmp[i];
					}
				}
			}
			
			ArrayList<Integer> priorityItemList = new ArrayList<>();
			ArrayList<Integer> unpriorityItemList = new ArrayList<>();
			for(int i = 0; i<newlist.size(); i++) {
//				priorityItemList.add(i);
				if(priorityItem[i]) {
					priorityItemList.add(i);
				}
				else {
					unpriorityItemList.add(i);
				}
			}

			shuffleIndex(state, thread, priorityItemList);
			shuffleIndex(state, thread, unpriorityItemList);
			
			//set the old fitness
			oldfit = evaluateObjectives(this, board);
			
			boolean skip = false; //indicate whether we need to re-calculate the gradient in this iteration
			double [] grad = new double [this.size()];
			
			int toBreakCnt = 0;
			for(int it = 0; it<numiterations; it++) {
				
				//2.a get the minus gradient
				if(!skip) {
					for(int g = 0; g<grad.length; g++) grad[g] = 0;
					
					for(int ob = 0; ob<this.numobjectives; ob++) {
						double [] tmp = objectives[ob].gradient(newlist, board);
						for(int i = 0; i< this.size(); i++) {
							grad[i] += -1*tmp[i] * coefficiency[ob];				//negative gradient
						}
					}
					
					//normalize gradient
					double norm = 0;
					for(int i = 0; i<this.size(); i++) {
						norm += grad[i]*grad[i];
					}
					norm = Math.sqrt(norm);
					for(int i = 0; i<this.size(); i++) {
						grad[i] /= norm;
						grad[i] = Math.signum(grad[i]) / (-Math.log( Math.abs(grad[i])));
					}
				}
				
				
				//2.b update the indexes and maintain their unique and ranges
				if(step <= 0) {
					System.err.print("the step of index list is not initialized.\n");
					System.exit(1);
				}
				double stepg = state.random[thread].nextDouble()*this.step;
				boolean [] used = new boolean [this.size()];
				
				//first assign the priority items (the items that appeat in leader board and loser board)
				for(int ii = 0; ii<priorityItemList.size(); ii++) {
					int i = priorityItemList.get(ii);
//					if(!priorityItem[i]) continue;
					
					 int newind = (int) Math.floor(newlist.get(i).index + Math.signum(grad[i])*min_step + stepg * grad[i]);
					 
					 //ranges
					 if(newind < 0) {
						 newind = 0;
					 }
					 if(newind >= this.size()) {
						 newind = this.size() - 1;
					 }
					 
					 //uniques
					 if(used[newind]) {
						 int trialind = newind;
						 for(int j = 1; j<2*this.size(); j++) {
							 trialind = (int) (newind + Math.pow(-1, j)*Math.ceil(j / 2.0));
							 
							 if(trialind < 0 || trialind >= this.size() || used[trialind]) continue;
							 
							 newind = trialind;
							 break;
						 }
					 }
					 
					 if(used[newind]) {
						 System.err.print("Indexlist cannot find unique unused index after enumeration. What happens!!?");
						 System.exit(1);
					 }
					 
					 newlist.get(i).index = newind;
					 used[newind] = true;
				}
				//then assign those unprioritized items
				for(int ii = 0; ii<unpriorityItemList.size(); ii++) {
					int i = unpriorityItemList.get(ii);
//					if(priorityItem[i]) continue;
					
					 int newind = (int) Math.floor(newlist.get(i).index + stepg * grad[i]);
					 
					 //ranges
					 if(newind < 0) {
						 newind = 0;
					 }
					 if(newind >= this.size()) {
						 newind = this.size() - 1;
					 }
					 
					 //uniques
					 if(used[newind]) {
						 int trialind = newind;
						 for(int j = 1; j<2*this.size(); j++) {
							 trialind = (int) (newind + Math.pow(-1, j)*Math.ceil(j / 2.0));
							 
							 if(trialind < 0 || trialind >= this.size() || used[trialind]) continue;
							 
							 newind = trialind;
							 break;
						 }
					 }
					 
					 if(used[newind]) {
						 System.err.print("Indexlist cannot find unique unused index after enumeration. What happens!!?");
						 System.exit(1);
					 }
					 
					 newlist.get(i).index = newind;
					 used[newind] = true;
				}

				
				//set the new fitness,  if smaller, update to this index list; otherwise, break the iteration
				newfit = evaluateObjectives(newlist, board);
//				System.out.print("newfit: " + newfit + "; ");
				
				if(newfit <= oldfit) {
					for(int i = 0; i<this.size(); i++) {
						this.get(i).index = newlist.get(i).index;
					}
					oldfit = newfit;
					skip = false;
					toBreakCnt = 0;
				}
//				else {
//					break;
//				}
				else {
					for(int i = 0; i<this.size(); i++) {
						newlist.get(i).index = this.get(i).index;
					}
					skip = true;
					toBreakCnt ++;
					if(toBreakCnt == 5) break;
					
					shuffleIndex(state, thread, priorityItemList);
					shuffleIndex(state, thread, unpriorityItemList);
				}
				
			}
		}
		
		//add new symbols into the list
//		updateSymbolsRandomly(state, thread, subpop);
		
		trimList(state, thread);
	}

//	@Override
//	public GenoVector getGenoVector(CpxGPIndividual ind) {
//		
//		if(! (ind instanceof LGPIndividual)) {
//			System.err.print("we are trying to put a non-LGP indivdiual into the index list for LGP, execution quit");
//			System.exit(1);
//		}
//		
//		GenoVector Geno = new GenoVector( ((LGPIndividual)ind).getMaxNumTrees(),  ((LGPIndividual)ind).getMinNumTrees());
//		
//		for(int i = 0; i<Geno.length; i++) {
//			if(i<((LGPIndividual)ind).getTreesLength()) {
//				Geno.G[i] = this.getIndexBySymbol(((LGPIndividual)ind).getTreeStruct(i));
//				if(Geno.G[i] == GenoVector.None) {
//					System.err.print("we find an unkonwn LGP instruction. Please check whether the builders in genetic operators "
//							+ "and the index list are the same\n");
//					System.exit(i);
//				}
//			}
//				
//			else
//				Geno.G[i] = GenoVector.None;
//		}
//		
//		return Geno;
//	}
	
	@Override
	public GenoVector getGenoVector(CpxGPIndividual ind) {
		if(! (ind instanceof LGPIndividual)) {
			System.err.print("we are trying to put a non-LGP indivdiual into the index list for LGP, execution quit");
			System.exit(1);
		}
		
		GenoVector Geno = new GenoVector( ((LGPIndividual)ind).getMaxNumTrees(),  ((LGPIndividual)ind).getMinNumTrees());
		
		for(int i = 0; i<Geno.length; i++) {
			if(i<((LGPIndividual)ind).getTreesLength()) {
				Geno.G[i] = this.addSymbol(((LGPIndividual)ind).getTreeStruct(i), self_state, self_thread);
				if(Geno.G[i] < 0) {
					System.err.print("we find an uncompatible LGP instruction. Please check whether the builders in genetic operators "
							+ "and the index list are the same\n");
					System.exit(i);
				}
			}
				
			else
				Geno.G[i] = GenoVector.None;
		}
		
		return Geno;
	}
	
	@Override
	public GPTreeStruct getSymbolByIndex(int index, final EvolutionState state, final int thread) {
		
		int i = 0;
		for(; i<MAXNUMTRIES; i++) {
			int next_index = (int) (index + Math.pow(-1, i)*Math.ceil(i / 2.));
			if(next_index < 0 || next_index >= this.size()) continue;
			Index<GPTreeStruct> item = getItemByIndex(next_index);
			
			if(state.random[thread].nextDouble() > item.get_tabu_frequency()) {
				return (GPTreeStruct) item.symbols.get(state.random[thread].nextInt(item.symbols.size())).clone();
			}
		}
		if(i >= MAXNUMTRIES) {
			GPTreeStruct ins = ((GPTreeStruct) prototype.sym_prototype.lightClone());
			ins.owner = null; 
            ins.status = false;
            ins.effRegisters = new HashSet<Integer>(0);
            
			ins.buildTree(state, thread);
			return ins;
		}
		
		
//		for(int it = 0; it < this.size(); it++) {
//			
//			Index<GPTreeStruct> symbol = this.get(it);
//			
//			if(symbol.index == index) { //we found the target
//				if(state.random[thread].nextDouble() > symbol.get_tabu_frequency()) {
//					return (GPTreeStruct) symbol.symbols.get(state.random[thread].nextInt(symbol.symbols.size())).clone();
//				}
//				else {
//					GPTreeStruct ins = ((GPTreeStruct) prototype.sym_prototype.lightClone());
//					ins.owner = null; 
//		            ins.status = false;
//		            ins.effRegisters = new HashSet<Integer>(0);
//		            
//					ins.buildTree(state, thread);
//					return ins;
//				}
//				
////				int i = 0;
////				for(; i<MAXNUMTRIES; i++) {
////					int next_index = (int) (it + Math.pow(-1, i)*Math.ceil(i / 2.));
////					
////					if(next_index < 0 || next_index >= this.size()) continue;
////					
////					symbol = this.get(next_index);
////					
////					if(state.random[thread].nextDouble() > symbol.get_tabu_frequency()) {
////						return (GPTreeStruct) symbol.symbols.get(state.random[thread].nextInt(symbol.symbols.size())).clone();
////					}
////
////				}
////				
////				if(i >= MAXNUMTRIES) {
////					GPTreeStruct ins = ((GPTreeStruct) prototype.sym_prototype.lightClone());
////					ins.owner = null; 
////		            ins.status = false;
////		            ins.effRegisters = new HashSet<Integer>(0);
////		            
////					ins.buildTree(state, thread);
////					return ins;
//////					symbol = this.get(state.random[thread].nextInt(this.size()));
//////					return (GPTreeStruct) symbol.symbols.get(state.random[thread].nextInt(symbol.symbols.size())).clone();
////					
////				}
//			}
//		}
		
		System.err.println("The index list cannot find the index " + index);
		System.exit(1);
		
		return null;
	}
	
	public GPTreeStruct getRandSymbolByIndex(int index, final EvolutionState state, final int thread, final int subpopulation) {
		
		Index<GPTreeStruct> item = getItemByIndex(index);
		if(state.random[thread].nextDouble() > item.get_tabu_frequency()) {
			return (GPTreeStruct) item.symbols.get(state.random[thread].nextInt(item.symbols.size())).clone();
		}
		else {
//			Index<GPTreeStruct> symbol = this.get(state.random[thread].nextInt(this.size()));
//			return (GPTreeStruct) symbol.symbols.get(state.random[thread].nextInt(symbol.symbols.size())).clone();
			
//			GPTreeStruct ins = ((GPTreeStruct) prototype.sym_prototype.lightClone());
//			ins.owner = null; 
//            ins.status = false;
//            ins.effRegisters = new HashSet<Integer>(0);
//            
//			ins.buildTree(state, thread);
			
			GPTreeStruct ins = microMut( (GPTreeStruct) item.symbols.get(state.random[thread].nextInt(item.symbols.size())), state, thread, subpopulation);
			return ins;
			
		}
		
//		int i = 0;
//		for(; i<MAXNUMTRIES; i++) {
//			int next_index = (int) (index + Math.pow(-1, i)*Math.ceil(i / 2.));
//			if(next_index < 0 || next_index >= this.size()) continue;
//			Index<GPTreeStruct> item = getItemByIndex(next_index);
//			
//			if(state.random[thread].nextDouble() > item.get_tabu_frequency()) {
//				GPTreeStruct ins = (GPTreeStruct) item.symbols.get(state.random[thread].nextInt(item.symbols.size())).clone();
////				if(state.random[thread].nextDouble() < 0.0) {
////					ins = microMut( ins, state, thread, subpopulation );
////				}
////				GPTreeStruct ins = microMut((GPTreeStruct) item.symbols.get(state.random[thread].nextInt(item.symbols.size())), state, thread, subpopulation );
//				return ins;
//			}
//		}
//		if(i >= MAXNUMTRIES) {
////			GPTreeStruct ins = ((GPTreeStruct) prototype.sym_prototype.lightClone());
////			ins.owner = null; 
////            ins.status = false;
////            ins.effRegisters = new HashSet<Integer>(0);
////            
////			ins.buildTree(state, thread);
////			return ins;
//			Index<GPTreeStruct> item = getItemByIndex(index);
//			GPTreeStruct ins = microMut((GPTreeStruct) item.symbols.get(state.random[thread].nextInt(item.symbols.size())), state, thread, subpopulation );
//			return ins;
//		}
		
		
//		for(int it = 0; it < this.size(); it++) {
//			
//			Index<GPTreeStruct> symbol = this.get(it);
//			
//			if(symbol.index == index) { //we found the target
//				if(state.random[thread].nextDouble() > symbol.get_tabu_frequency()) {
//					return (GPTreeStruct) symbol.symbols.get(state.random[thread].nextInt(symbol.symbols.size())).clone();
//				}
//				else {
//					GPTreeStruct ins = ((GPTreeStruct) prototype.sym_prototype.lightClone());
//					ins.owner = null; 
//		            ins.status = false;
//		            ins.effRegisters = new HashSet<Integer>(0);
//		            
//					ins.buildTree(state, thread);
//					return ins;
//				}
//				
////				int i = 0;
////				for(; i<MAXNUMTRIES; i++) {
////					int next_index = (int) (it + Math.pow(-1, i)*Math.ceil(i / 2.));
////					
////					if(next_index < 0 || next_index >= this.size()) continue;
////					
////					symbol = this.get(next_index);
////					
////					if(state.random[thread].nextDouble() > symbol.get_tabu_frequency()) {
////						return (GPTreeStruct) symbol.symbols.get(state.random[thread].nextInt(symbol.symbols.size())).clone();
////					}
////
////				}
////				
////				if(i >= MAXNUMTRIES) {
////					GPTreeStruct ins = ((GPTreeStruct) prototype.sym_prototype.lightClone());
////					ins.owner = null; 
////		            ins.status = false;
////		            ins.effRegisters = new HashSet<Integer>(0);
////		            
////					ins.buildTree(state, thread);
////					return ins;
//////					symbol = this.get(state.random[thread].nextInt(this.size()));
//////					return (GPTreeStruct) symbol.symbols.get(state.random[thread].nextInt(symbol.symbols.size())).clone();
////					
////				}
//			}
//		}
		
//		System.err.println("The index list cannot find the index " + index);
//		System.exit(1);
//		
//		return null;
	}
	
	protected int addSymbol(GPTreeStruct symbol, EvolutionState state, int thread) {
		//return the index of the newly added (or existing) symbol
		//check the compatibility
		GPInitializer initializer = ((GPInitializer)state.initializer);
		if(!symbol.constraints(initializer).treetype.equals(this.prototype.sym_prototype.constraints(initializer).treetype)) {
			return -1;
		}
		
		//check the duplication
		for(int listind = 0; listind < this.size(); listind ++) {
			Index<GPTreeStruct> item = this.get(listind);
			if(item.isduplicated(symbol)) {
				
				boolean exist = false;
				for(GPTreeStruct sym : item.symbols) {
					if(sym.toString().equals(symbol.toString())) {
						exist = true;
						break;
					}
				}
				
				if(!exist) item.addSymbol(symbol);
				
				//move the visited item forward
				int next_pos = (int) (listind * 0.8);
				if(next_pos<listind) {
					//swap the listind and next_pos
					Index<GPTreeStruct> tmp = this.get(next_pos);
					this.set(next_pos, item);
					this.set(listind, tmp);
				}
				
				
				return item.index;
			}
		}
		
		
		//if not exist, then add to the list.
		int n = this.size();
		
//		int index = (int) (state.random[thread].nextInt((int) (n)) ); 
		int index = n;
		
		//increase the indices that >= the generated "index"
//		for(Index<GPTreeStruct> item : this) {
//			if(item.index >= index) {
//				item.index ++;
//			}
//		}
		
		//add the new symbol into the list
		Index<GPTreeStruct> nd = (Index<GPTreeStruct>) this.prototype.clone();
		
		nd.index = index;
		
		nd.addSymbol(symbol);
		
//		int position = state.random[thread].nextInt(this.size());
		this.add(0, nd);
		
		return index;
	}
	
	protected int addSymbol(GPTreeStruct symbol, int index, EvolutionState state, int thread) {
		//return the index of the newly added (or existing) symbol
		//check the compatibility
		GPInitializer initializer = ((GPInitializer)state.initializer);
		if(!symbol.constraints(initializer).treetype.equals(this.prototype.sym_prototype.constraints(initializer).treetype)) {
			return -1;
		}
		
		//check the duplication
		for(Index<GPTreeStruct> item : this) {
			if(item.isduplicated(symbol)) {
				
				boolean exist = false;
				for(GPTreeStruct sym : item.symbols) {
					if(sym.toString().equals(symbol.toString())) {
						exist = true;
						break;
					}
				}
				
				if(!exist) item.addSymbol(symbol);
				
				return item.index;
			}
		}
		
		
		//if not exist, then add to the list.
//		int n = this.size();
//		
//		int index = state.random[thread].nextInt((int) (n)); 
		
		//increase the indices that >= the generated "index"
		for(Index<GPTreeStruct> item : this) {
			if(item.index >= index) {
				item.index ++;
			}
		}
		
		//add the new symbol into the list
		Index<GPTreeStruct> nd = (Index<GPTreeStruct>) this.prototype.clone();
		
		nd.index = index;
		
		nd.addSymbol(symbol);
		
		this.add(nd);
		
		return index;
	}
	
	protected int setSymbol(GPTreeStruct symbol, int index, EvolutionState state, int thread) {
		
		//check the duplication
		for(Index<GPTreeStruct> item : this) {
			if(item.isduplicated(symbol)) {
				
				boolean exist = false;
				for(GPTreeStruct sym : item.symbols) {
					if(sym.toString().equals(symbol.toString())) {
						exist = true;
						break;
					}
				}
				
				if(!exist) item.addSymbol(symbol);
				
				return item.index;
			}
		}
		
		Index<GPTreeStruct> item = getItemByIndex(index);
		
		item.symbols.clear();
		item.set_tabu_frequency(0);
		item.symbols.add(symbol);
		
		return index;
	}
	
	protected void sortList() {
		// sort the index list
        Collections.sort(this, new IndexComparator());
	}
	
	protected void trimList(EvolutionState state, int thread) {
		
//		//find the largest index in the leading batch
//		Distance obj = null;
		int tarsize = maxlistsize;
//		for(int ob = 0; ob < this.numobjectives; ob++) {
//			if(objectives[ob] instanceof Distance) {
//				obj = (Distance) objectives[ob];
//				break;
//			}
//		}
//		GenoVector[] genoArray = obj.genoArray;
//		double avg = 0;
//		double std = 0;
//		double cnt = 0;
//		for(GenoVector vec : genoArray) {
//			for(int g : vec.G) {
//				if(g>tarsize) {
//					tarsize = g;
//				}
//				if(g == -1) break;
//				avg += g;
//				cnt++;
//			}
//		}
//		avg /= cnt;
//		System.out.println(avg);
//		for(GenoVector vec : genoArray) {
//			for(int g : vec.G) {
//				if(g == -1) break;
//				std += (g - avg)*(g - avg);
//			}
//		}
//		std = Math.sqrt(std / cnt);
//		System.out.println(std);
//		System.out.println(tarsize);
////		int tarsize = Math.max(maxlistsize, this.size()/2);
		
//		if(this.size() > tarsize) {			
//			for(int i = 0; i<this.size(); i++) {
//				Index<GPTreeStruct> item = this.get(i);
//				if(item.index >= tarsize) {
//					this.remove(i);
//					i--;
//				}
//			}
//		}
		
		while(this.size() > tarsize) {
			double thre = state.random[thread].nextDouble();

			int targeti = state.random[thread].nextInt(this.size());
			for(int i = 0; i<this.size(); i++) {
				Index<GPTreeStruct> item = this.get( (targeti + i)%this.size() );
				if(0.1+0.9*item.index/this.size() >= thre) {
					int index = item.index;
					this.remove((targeti + i)%this.size());

					for(Index<GPTreeStruct> it : this) {
						if(it.index > index) {
							it.index --;
						}
					}

					break;
				}

			}			
			
		}
		sortList();
	}
	

	public void updateSymbolsFromPop(EvolutionState state, int thread, SubpopulationFLO subpop) {
		
		//reset the tabu frequency
		clear_tabu_frequency();
		
		double num_inds = subpop.individuals.length;
		int symind = 0;
		
		for(Individual ind : subpop.individuals) {
			LGPIndividual lgpind = (LGPIndividual) ind;
			
//			//estimate the rank
//			double worse = 0;
//			for(int tr = 0; tr<10; tr++) {
//				LGPIndividual tmpind = (LGPIndividual) subpop.individuals[state.random[thread].nextInt((int) num_inds)];
//				if(!lgpind.fitness.betterThan(tmpind.fitness)) {
//					worse++;
//				}
//			}
			
			
			for(GPTreeStruct tree : lgpind.getTreelist()) {
//				symind = this.addSymbol((GPTreeStruct) tree.clone(), (int) (worse*this.size()/10), state, thread);
				symind = this.addSymbol((GPTreeStruct) tree.clone(), state, thread);
				
				double freq = this.get(symind).get_tabu_frequency();
				this.get(symind).set_tabu_frequency( Math.min(Index.TABU_THRESOLD, freq + 1.*2/num_inds) );  //regarding a symbol showing in more than half a population is out-of-date
			}
		}
		
	}
	
	public void updateSymbolsFromRandPop(EvolutionState state, int thread, SubpopulationFLO subpop) {
		
		//reset the tabu frequency
		clear_tabu_frequency();
		
		double num_inds = 0.5*subpop.individuals.length;
		int symind = 0;
		
		for(int i = 0; i<num_inds; i++) {
			//randomly select a competitive individual
			LGPIndividual lgpind = (LGPIndividual) subpop.individuals[state.random[thread].nextInt((int) num_inds)];
			for(int tr = 1; tr<7; tr++) {
				LGPIndividual tmpind = (LGPIndividual) subpop.individuals[state.random[thread].nextInt((int) num_inds)];
				if(tmpind.fitness.betterThan(lgpind.fitness)) {
					lgpind = tmpind;
				}
			}
			
			for(GPTreeStruct tree : lgpind.getTreelist()) {
//				symind = this.addSymbol((GPTreeStruct) tree.clone(), (int) (worse*this.size()/10), state, thread);
				symind = this.addSymbol((GPTreeStruct) tree.clone(), state, thread);
				
				double freq = this.get(symind).get_tabu_frequency();
				this.get(symind).set_tabu_frequency( Math.min(Index.TABU_THRESOLD, freq + 1.*2/num_inds) );  //regarding a symbol showing in more than half a population is out-of-date
			}
		}
		
	}

	public void updateSymbolsRandomly(EvolutionState state, int thread, SubpopulationFLO subpop) {
				
//		ArrayList<GPTreeStruct> allsymbols = builder.enumerateSymbols(state, 
//				type, thread, 0, ((GPTreeStruct) prototype.sym_prototype).constraints(initializer).functionset);

		//get the index of the sub-population
		int subpopi = 0;
		for(Subpopulation pop : state.population.subpops) {
			if(pop == subpop) break;
			subpopi ++;
		}
		
		for(int i = 0; i<maxlistsize*rand_update_rate+1; i++) {
			
			//randomly generate a tree
//			GPTreeStruct ins = ((GPTreeStruct) prototype.sym_prototype.lightClone());
//			ins.owner = null; 
//          ins.status = false;
//          ins.effRegisters = new HashSet<Integer>(0);
//            
//			ins.buildTree(state, thread);
			
			
//			if(state.random[thread].nextDouble() < (double)state.generation / state.numGenerations + 0.1) 
//			{
				//apply micro mutation on symbols
				int n = (int) (this.size());
				int index = state.random[thread].nextInt(n);
				ArrayList<GPTreeStruct> symbolList = this.get(index).symbols;
				GPTreeStruct ins = microMut( (GPTreeStruct) symbolList.get(state.random[thread].nextInt(symbolList.size())), state, thread, subpopi);

//				GPTreeStruct ins = (GPTreeStruct) symbolList.get(state.random[thread].nextInt(symbolList.size())).clone();
//				GPNode p1 = null, p2 = null;
//				
//				for(int t = 0; t<10; t++) {
//					// prepare the nodeselector
//	                nodeselect.reset();
//	                
//					p1 = nodeselect.pickNode(state,subpopi,thread,null,ins);
//		   		 	p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
//			                    p1.parentType(initializer),
//			                    thread,
//			                    p1.parent,
//			                    ins.constraints(initializer).functionset,
//			                    p1.argposition,
//			                    GPNodeBuilder.NOSIZEGIVEN,
//			                    p1.atDepth());
//		   		 	
//		   		 	//easy check
//		            boolean res = checkPoints(p1, p2, state, thread, null, ins);
//		            
//		            if(res) break;
//		            else {
//		            	symbolList = this.get(state.random[thread].nextInt(n)).symbols;
//		            	ins = (GPTreeStruct) symbolList.get(state.random[thread].nextInt(symbolList.size())).clone();
//		            }
//				}
//				ins.child = ins.child.cloneReplacingNoSubclone(p2, p1);
//				ins.child.parent = ins;
//				ins.child.argposition = 0;
//			}
			
			
			this.addSymbol(ins, state, thread);
			
			
		}
	}

	public void updateSymbolsFromObjs(EvolutionState state, int thread, SubpopulationFLO subpop, Board board) {
		//this function 1) preprocess the objectives and 2) update the indexlist based on the objectives
		//every objective perform  preprocessing
		for(int ob = 0; ob < this.numobjectives; ob++) {
			objectives[ob].preprocessing(state, thread, this, board, boardsize[ob], batchsize);
			objectives[ob].setPrivateCoefficiency(this, board);
		}
		
		//reset the usedItem since usedItem is initialized in preprocessing but likely doesn't use the up-to-date instruction list
		for(int ob = 0; ob < this.numobjectives; ob++) {
			objectives[ob].updateNewIndexList(state, thread, this, board);
		}
	}
	public GenoVector getEffectiveGenoVector(LGPIndividual ind) {
		if(! (ind instanceof LGPIndividual)) {
			System.err.print("we are trying to put a non-LGP indivdiual into the index list for LGP, execution quit");
			System.exit(1);
		}
		
		if(ind.getEffTreesLength() == 0) {
			return getGenoVector(ind);
		}
		
		LGPIndividual tmpind = ind.lightClone();
		
		for(int i = 0; i < tmpind.getTreesLength(); i++) {
			if( ! tmpind.getTreeStruct(i).status) {
				tmpind.removeTree(i);
				i--;
			}
		}
		
		GenoVector Geno = new GenoVector( ((LGPIndividual)tmpind).getMaxNumTrees(),  ((LGPIndividual)tmpind).getMinNumTrees());
		
		for(int i = 0; i<Geno.length; i++) {
			if(i<((LGPIndividual)tmpind).getTreesLength()) {
				Geno.G[i] = this.getIndexBySymbol(((LGPIndividual)tmpind).getTreeStruct(i));
				if(Geno.G[i] == GenoVector.None) {
					System.err.print("we find an unkonwn LGP instruction. Please check whether the builders in genetic operators "
							+ "and the index list are the same\n");
					System.exit(i);
				}
			}
			else
				Geno.G[i] = GenoVector.None;
		}
		
		return Geno;
	}
	
	@Override
	public IndexList cloneIndexList() {
		//only copy the indexes, it will not clone the board items
		IndexList4LGP_FLR list = new IndexList4LGP_FLR();
		list.self_state = this.self_state;
		list.self_thread = this.self_thread;
		list.nodeselect = (GPNodeSelector)(nodeselect.clone());
		
		list.maxlistsize = this.maxlistsize;
		
		list.builder = this.builder;
		list.prototype = this.prototype;
		
		for(Index<GPTreeStruct> ind : this) {
			
			list.add((Index<GPTreeStruct>) ind.clone());
		}
		
//		list.setDiffNeighbor(DiffNeighbor);		
		
		return list;
	}

	@Override
	public double evaluateObjectives(IndexList list, Board board) {
		
		double fit = 0;
		for(int ob = 0; ob < this.numobjectives; ob++) {
			fit += coefficiency[ob] * objectives[ob].evaluate(list, board);
		}
		return fit;
	}
	
	@Override
	protected void initializeDiffNeighbor(EvolutionState state, int thread) {
		//because we decide not to use the optimization objective Normalization, we can save the calculation for initializing DiffNeighbor.
		int n = this.size();
		DiffNeighbor = new double [n][n];
	}
	
	protected boolean checkPoints(GPNode p1, GPNode p2, EvolutionState state, int thread, LGPIndividual ind, GPTreeStruct treeStr) {
		// p1 and p2 must be different. if they are at destination, they should also be effective
		boolean res = false;
		
		if(p1.expectedChildren() == p2.expectedChildren()) {
        	if(!p1.toStringForHumans().equals(p2.toStringForHumans()) ){
        		res = true;        		
        		
//        		for(int c = 0;c<p1.children.length;c++) {
//        			p2.children[c] = (GPNode)p1.children[c].clone();
//        		}
        	}
        }
//		else {
//			res = true;
//		}
		
		return res;
	}
	
	protected GPTreeStruct microMut(GPTreeStruct orig_ins, EvolutionState state, int thread, int subpopi) {
		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		GPType type = ((GPTreeStruct) prototype.sym_prototype).constraints(initializer).treetype;
		
		GPTreeStruct ins = (GPTreeStruct) orig_ins.clone();
		GPNode p1 = null, p2 = null;
		GPFunctionSet set = ins.constraints(initializer).functionset;
		
		boolean res = false;
		for(int t = 0; t<10; t++) {
			// prepare the nodeselector
            nodeselect.reset();
            
			p1 = nodeselect.pickNode(state,subpopi,thread,null,ins);
//			if(state.random[thread].nextDouble()<0.05) {
//	   		 	p2 = ((LGPMutationGrowBuilder)builder).newRootedTree(state,
//	                p1.parentType(initializer),
//	                thread,
//	                p1.parent,
//	                ins.constraints(initializer).functionset,
//	                p1.argposition,
//	                GPNodeBuilder.NOSIZEGIVEN,
//	                p1.atDepth());
//	   		 	
//	   		 	ins.child = ins.child.cloneReplacingNoSubclone(p2, p1);
//	   		 	ins.child.parent = ins;
//				ins.child.argposition = 0;
//				
//				return ins;
//			}
			if(p1 instanceof WriteRegisterGPNode) { //destination register
				p2 = (GPNode)((GPNode) set.registers_v.get(state.random[thread].nextInt(set.registers_v.size()))).lightClone();
				p2.resetNode(state, thread);
			}
			else if(p1.children.length == 0) { //terminals
				if(state.random[thread].nextDouble()<((LGPMutationGrowBuilder)builder).probCons
						&& ((LGPMutationGrowBuilder)builder).canAddConstant(ins.child)) {
					
					if(p1 instanceof Entity && state.random[thread].nextDouble()<0.5) {
						p2 = p1.lightClone();
						((Entity) p2).getArguments().varyNode(state, thread, (Entity) p1);
					}
					else {
						p2 = (GPNode)((GPNode) set.constants_v.get(state.random[thread].nextInt(set.constants_v.size()))).lightClone();
						p2.resetNode(state, thread);
					}
					
				}
				else {
					p2 = (GPNode)((GPNode) set.nonconstants_v.get(state.random[thread].nextInt(set.nonconstants_v.size()))).lightClone();
					p2.resetNode(state, thread);
				}
			}
			else { //functions
				if(p1 instanceof Entity && state.random[thread].nextDouble()<0.5) {
					p2 = p1.lightClone();
					((Entity) p2).getArguments().varyNode(state, thread, (Entity) p1);
				}
				else {
					p2 = (GPNode)((GPNode) set.nonterminals_v.get(state.random[thread].nextInt(set.nonterminals_v.size()))).lightClone();
					p2.resetNode(state, thread);
				}
				
			}

   		 	
   		 	//easy check
            res = checkPoints(p1, p2, state, thread, null, ins);
            
            if(res) break;
//            else {
//            	symbolList = this.get(state.random[thread].nextInt(n)).symbols;
//            	ins = (GPTreeStruct) symbolList.get(state.random[thread].nextInt(symbolList.size())).clone();
//            }
		}
		
		if(!res) {
			ins = ((GPTreeStruct) prototype.sym_prototype.lightClone());
			ins.owner = null; 
            ins.status = false;
            ins.effRegisters = new HashSet<Integer>(0);
            
			ins.buildTree(state, thread);
		}
		else {
//			ins.child = ins.child.cloneReplacingNoSubclone(p2, p1);
			p1.replaceWith(p2);
			ins.child.parent = ins;
			ins.child.argposition = 0;
		}
		
		
		return ins;
	}
	
	protected Index<GPTreeStruct> getItemByIndex(int index) {
		//different from getSymbolsByIndex(int) that returns symbols (e.g., instructions), this function returns the list item.
		for(Index<GPTreeStruct> it : this) {
			if(it.index == index) {
				return it;
			}
		}
		return null;
	}
	
	static class IndexComparator implements Comparator<Index>
    {
		@Override
		public int compare(Index o1, Index o2) {
			if (o1.index == o2.index)
	            return 0;
	        else if (o1.index > o2.index)
	            return 1;
	        else
	            return -1;
		}
    }
}
