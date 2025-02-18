package zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.indexing;

import java.util.ArrayList;
import java.util.Collections;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.gp.GPNodeBuilder;
import ec.gp.GPType;
import zhixing.cpxInd.algorithm.LandscapeOptimization.SubpopulationFLO;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Index;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexList;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.Distance;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.IntervalDistance;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.LosingDistance;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.Navigation;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.Objective4FLO;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ConstantGPNode;
import zhixing.cpxInd.individual.primitive.FlowOperator;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
//import zhixing.djss.algorithm.LandscapeOptimization.simpleLGP.indexing.SimpleLGPBuilder4DJSS;

public class IndexList4LGP extends IndexList<GPTreeStruct>{
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(final EvolutionState state, final int thread) {

		GPInitializer initializer = ((GPInitializer)state.initializer);
		
		GPType type = ((GPTreeStruct) prototype.sym_prototype).constraints(initializer).treetype;
		
		ArrayList<GPTreeStruct> allsymbols = builder.enumerateSymbols(state, 
				type, thread, 0, ((GPTreeStruct) prototype.sym_prototype).constraints(initializer).functionset);
		
		for(GPTreeStruct ins : allsymbols) {
			
			//check duplication with existing symbols in the list
			boolean dup = false;
			for(Index<GPTreeStruct> nd : this ) {
				if(nd.isduplicated(ins)) {
					dup = true;
					
					nd.addSymbol(ins);
					
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
		
		super.initialize(state, thread);
	}

	@Override
	public void optimizeIndex(EvolutionState state, int thread, SubpopulationFLO subpop, Board board) {
		// update the indexes based on the new leading board
		
		//1. initialize the search
		//rank the symbols based on their frequency in the current population
		int base = (int) Math.floor(this.size() / 2.0);
//		ShuffleIndexBasedFrequency(state, thread, subpop, base);
		
		
		//2. iteratively update the indexes
		double oldfit = 0, newfit = 0;
		IndexList4LGP newlist = (IndexList4LGP) this.cloneIndexList();
		
		for(int ep = 0; ep < 1; ep++) {
			boolean [] priorityItem = new boolean [this.size()];
			//set coefficiency of each objectives
			for(int ob = 0; ob < this.numobjectives; ob++) {
				objectives[ob].preprocessing(state, thread, this, board, boardsize[ob], batchsize);
				objectives[ob].setPrivateCoefficiency(this, board);
				
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
		
		
		
		
	}

	@Override
	public GenoVector getGenoVector(CpxGPIndividual ind) {
		
		if(! (ind instanceof LGPIndividual)) {
			System.err.print("we are trying to put a non-LGP indivdiual into the index list for LGP, execution quit");
			System.exit(1);
		}
		
		GenoVector Geno = new GenoVector( ((LGPIndividual)ind).getMaxNumTrees(),  ((LGPIndividual)ind).getMinNumTrees());
		
		for(int i = 0; i<Geno.length; i++) {
			if(i<((LGPIndividual)ind).getTreesLength()) {
				Geno.G[i] = this.getIndexBySymbol(((LGPIndividual)ind).getTreeStruct(i));
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

	
	protected void ShuffleIndexBasedFrequency(EvolutionState state, int thread, SubpopulationFLO pop, int base) {
		double [] frequency = new double [this.size()];
		double [] rank = new double [this.size()];
		
		ArrayList<Integer> toBeShuffleIndex = new ArrayList<>();
		
		Board board = pop.fullBoard;
		Board leadBoard = board.lightTrimCloneByBest();
//		leadBoard.trim2MaxsizeByBest();
		
		for(int i = 0; i<leadBoard.size(); i++) {
			for(CpxGPIndividual cpxind : leadBoard.get(i)) {
				LGPIndividual ind = (LGPIndividual) cpxind;
				for(int s = 0; s<ind.getTreesLength(); s++) {
					int index = this.getIndexBySymbol(ind.getTreeStruct(s));
					
					frequency[index]++;
				}
			}
			
		}
		
		for(int i = 0; i<this.size(); i++) {
			for(int j = 0; j<this.size(); j++) {
				if(j == i) continue;
				
				if(frequency[i] < frequency[j] 
						|| (frequency[i] == frequency[j] && Math.abs(get(i).index - base) > Math.abs(get(j).index - base))
						|| (frequency[i] == frequency[j] && Math.abs(get(i).index - base) == Math.abs(get(j).index - base) && get(i).index > get(j).index)
						) {
					rank[i] ++;   //indexes with worse frequency or equal frequency but larger index  will have larger rank
				}
			}
		}
		
		for(int i = 0;i<this.size();i++) {
			this.get(i).index = (int) (base + Math.pow(-1, rank[i])*Math.ceil(rank[i] / 2.0));
			if(frequency[i] == 0) {
				toBeShuffleIndex.add(this.get(i).index);
			}
		}
		
		shuffleIndex(state, thread, toBeShuffleIndex);
		
		for(int i = 0, j = 0; i<this.size(); i++) {
			if(frequency[i] == 0) {
				this.get(i).index = toBeShuffleIndex.get(j);
				j++;
			}
		}
		
	}

	@Override
	public IndexList cloneIndexList() {
		//only copy the indexes, it will not clone the board items
		IndexList list = new IndexList4LGP();
		for(Index<GPTreeStruct> ind : this) {
			
			list.add(ind.clone());
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
	
	private void checkIndexComplete() {
		short [] checklist = new short[this.size()];
		
		for(int j = 0; j<this.size(); j++) {
			Index item = this.get(j);
			int index = item.index;
			if(checklist[index] == 0) {
				checklist [index] = 1;
			}
			else {
				System.out.println("duplicate index: " + index + " " + j);
			}
		}
		int sum = 0;
		for(short i : checklist) {
			sum += i;
		}
		
		if(sum!=this.size()) {
			System.out.print("index system is incomplete\n");
		}
	}

	@Override
	protected void initializeDiffNeighbor(EvolutionState state, int thread) {
		int n = this.size();
		DiffNeighbor = new double [n][n];
		
		if(! builder.initialized) {
			System.err.print("the builder of index list has not been initialized\n");
			System.exit(1);
		}
		
		SimpleLGPBuilder lgpbuilder = (SimpleLGPBuilder) builder;
		
		//initialize the inputs and outputs
		final int num_cases = 10;
		final double data_rng = 20;
		ArrayList<double [][]> output_list = new ArrayList<>();
		double [][] output;
		final int num_cons = lgpbuilder.constant.size();
		final int num_srcreg = lgpbuilder.srcReg.size();
		double [][] inputs_cons = new double [num_cons][num_cases];
		double [][] inputs_srcreg = new double [num_srcreg][num_cases];
		
		for(int i = 0; i<num_cons; i++) {
			for(int c = 0; c<num_cases; c++) {
				inputs_cons[i][c] = -data_rng + state.random[thread].nextDouble()*data_rng*2;
			}
		}
		
		for(int i = 0; i<num_srcreg; i++) {
			for(int c = 0; c<num_cases; c++) {
				inputs_srcreg[i][c] = -data_rng + state.random[thread].nextDouble()*data_rng*2;
			}
		}
		
		for(int i = 0; i<n; i++) {
			output = new double [num_cases][lgpbuilder.desReg.size()];
			
			//initialize the output
			for(int r = 0; r<lgpbuilder.desReg.size(); r++) {
				for(int c = 0; c<num_cases; c++) {
					output[c][r] = inputs_srcreg[r][c];
				}
			}
			
			GPTreeStruct instruction = this.get(i).symbols.get(0);
			
			//get the inputs 			
			double [] input0 = getInput(lgpbuilder, instruction.child.children[0].children[0], inputs_cons, inputs_srcreg);
			double [] input1 = null;
			if(instruction.child.children[0].children.length == 2) {
				input1 = getInput(lgpbuilder, instruction.child.children[0].children[1], inputs_cons, inputs_srcreg);
			}
			double []output_fun = executeFunction(lgpbuilder, instruction.child.children[0], input0, input1);
			setOutput(lgpbuilder, instruction.child, output, output_fun);
			
			output_list.add(output);
		}
		
		double maxDg = 0, maxDs = 0;
		for(int i = 0;i<n;i++) {
			for(int j = i+1;j<n;j++) {
				if(i == j) continue;
				double Dg = getGenotypeDiff(this.get(i).symbols.get(0), this.get(j).symbols.get(0));
				double Ds = getSemanticDiff(output_list.get(i), output_list.get(j));
				
				if(Dg > maxDg) maxDg = Dg;
				if(Ds > maxDs) maxDs = Ds;
			}
		}
		
		//set the DiffNeighbor
		for(int i = 0;i<n;i++) {
			for(int j = i+1;j<n;j++) {
				if(i == j) continue;
				double Dg = getGenotypeDiff(this.get(i).symbols.get(0), this.get(j).symbols.get(0));
				double Ds = getSemanticDiff(output_list.get(i), output_list.get(j));
				
				DiffNeighbor[i][j] = DiffNeighbor[j][i] = (Dg / maxDg) * (Ds / maxDs);
			}
		}
		
		//normalize the DiffNeighbor by ranks (by columns)
//		ArrayList <Double> ranks = new ArrayList<>();
//		for(int j = 0; j<n; j++) {
//			ranks.clear();
//			
//			for(int i = 0; i<n; i++) {
//				double cnt = 0;
//				if(i == j) {
//					ranks.add(cnt);
//					continue;
//				}
//				for(int ii = 0; ii<n; ii++) {
//					if(DiffNeighbor[i][j] > DiffNeighbor[ii][j]) {
//						cnt ++;
//					}
//				}
//				ranks.add(cnt);
//			}
//			
//			//set the DiffNeighbor[*][j] as the ranks
//			for(int i = 0; i<n; i++) {
//				DiffNeighbor[i][j] = ranks.get(i);
//			}
//		}
	}
	
	private double[] getInput(SimpleLGPBuilder builder, GPNode n, double [][] input_cons, double [][] input_srcreg) {
		//return an array of results, each element for an instance case
		double [] res = new double[input_cons[0].length];
		boolean evaluated = false;
		
		for(int i = 0; i<builder.constant.size(); i++) {
			GPNode check = builder.constant.get(i);
			if(check.toString().equals(n.toString())) { //we find the symbol
				for(int c = 0; c<res.length; c++) {
					res[c] = input_cons[i][c];
				}
				evaluated = true;
				break;
			}
		}
		
		for(int i = 0; i<builder.srcReg.size(); i++) {
			GPNode check = builder.srcReg.get(i);
			if(check.toString().equals(n.toString())) { //we find the symbol
				for(int c = 0; c<res.length; c++) {
					res[c] = input_srcreg[i][c];
				}
				evaluated = true;
				break;
			}
		}
		
		if(!evaluated){
			System.err.print("unknown input features when building indexList4LGP: " + n.toString());
			System.exit(1);
		}
		
		return res;
	}
	
	private double[] executeFunction(SimpleLGPBuilder builder, GPNode n, double[] arg0, double[] arg1) {
		//return an array of results, each element for an instance case
		double [] res = new double[arg0.length];
		boolean evaluated = false;
		
		if(n instanceof FlowOperator) {
			for(int c = 0; c<res.length; c++) {
				res[c] = arg0[c];
			}
			evaluated = true;
		}
		else {
			for(int c = 0; c<res.length; c++) {
				
				switch(n.toString()) {
				case "add":
				case "+": res[c] = arg0[c] + arg1[c]; break;
				case "sub":
				case "-" : res[c] = arg0[c] - arg1[c]; break;
				case "mul":
				case "*": res[c] = arg0[c] * arg1[c]; break;
				case "div":
				case "/" : res[c] = arg0[c] / Math.sqrt(0.001+arg1[c]*arg1[c]); break;
				case "max": res[c] = Math.max(arg0[c], arg1[c]); break;
				case "min": res[c] = Math.min(arg0[c], arg1[c]); break;
				case "cos": res[c] = Math.cos(arg0[c]);break;
				case "sin": res[c] = Math.sin(arg0[c]);break;
				case "exp": res[c] = Math.exp(arg0[c]); break;
				case "ln": res[c] = Math.log(Math.max(1e-3, Math.abs(arg0[c]))); break;
				case "sqr": res[c] = Math.sqrt(Math.abs(arg0[c])); break;
				default:
					System.err.print("please add the corresponding behaviors for the function: " + n.toString());
					System.exit(1);
				}
				
			}
			evaluated = true;
		}
		
		if(!evaluated){
			System.err.print("unknown functions when building indexList4LGP: " + n.toString());
			System.exit(1);
		}
		
		return res;
	}
	
	private void setOutput(SimpleLGPBuilder builder, GPNode n, double [][]output, double [] input) {

		boolean evaluated = false;
		
		if(n instanceof WriteRegisterGPNode) {
			int reg_ind = ((WriteRegisterGPNode)n).getIndex();
			for(int c = 0; c<input.length; c++) {
				output[c][reg_ind] = input[c];
			}
			evaluated = true;
		}
		
		if(!evaluated){
			System.err.print("unknown destination register when building indexList4LGP: " + n.toString());
			System.exit(1);
		}
	}
	
	private double getSemanticDiff(double[][] sm1, double [][] sm2) {
		double res = 0;
		
		if(sm1.length==0 || sm2.length == 0 || sm1.length != sm2.length || sm1[0].length != sm2[0].length) {
			System.err.print("we got inconsistent semantic matrix when building indexList4LGP \n");
			System.exit(1);
		}
		
		for(int c = 0; c<sm1.length; c++) {
			for(int r = 0; r<sm1[c].length; r++) {
				res += Math.pow( sm1[c][r] - sm2[c][r], 2 );
			}
		}
		
		return Math.sqrt(res);
	}
	
	private double getGenotypeDiff(GPTreeStruct ins1, GPTreeStruct ins2) {
		double res = 0;
		
		if(! ins1.child.toString().equals(ins2.child.toString()) ) res += 20;
		
		if(! ins1.child.children[0].toString().equals(ins2.child.children[0].toString())) {			
			res += 10;
		}
				
		if(! ins1.child.children[0].children[0].toString().equals(ins2.child.children[0].children[0].toString())) res += 2.5;
		
		if(ins1.child.children[0].children.length != ins2.child.children[0].children.length) {
			res += 5;
		}
		else if(ins1.child.children[0].children.length == 2 
				&& ! ins1.child.children[0].children[1].toString().equals(ins2.child.children[0].children[1].toString())) {
			res += 2.5;
		}
		
		return res;
	}
	
}
