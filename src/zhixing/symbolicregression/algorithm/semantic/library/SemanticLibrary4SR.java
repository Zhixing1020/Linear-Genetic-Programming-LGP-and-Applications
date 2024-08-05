package zhixing.symbolicregression.algorithm.semantic.library;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.spiderland.Psh.intStack;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.gp.GPTree;
import ec.util.Parameter;
import yimei.jss.gp.data.DoubleData;
import zhixing.cpxInd.algorithm.semantic.library.LibraryItem;
import zhixing.cpxInd.algorithm.semantic.library.SLBreedingPipeline;
import zhixing.cpxInd.algorithm.semantic.library.SLBreedingSource;
import zhixing.cpxInd.algorithm.semantic.library.SVSet;
import zhixing.cpxInd.algorithm.semantic.library.SemanticLibrary;
import zhixing.cpxInd.algorithm.semantic.library.SemanticVector;
import zhixing.cpxInd.algorithm.semantic.library.fitness.SLFitness;
import zhixing.cpxInd.algorithm.semantic.library.select.SLSelectionMethod;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
//import zhixing.symbolicregression.optimization.GPSymbolicRegression;
import zhixing.symbolicregression.optimization.GPSymbolicRegression;

public class SemanticLibrary4SR extends SemanticLibrary{
	//maintain a fixed-size instruction list, a set of inputs, and a set of outputs
//	private static final String P_SEMANTICLIBRARY = "semantic_library";
//	
//	private static final String P_MAXNUMINSTRUCTION = "maxNumInstr";
//	private static final String P_NUMREFERENCE = "numreference";
//	private static final String P_NUMREGS = "numregisters";
//	private static final String P_MAXNUMINPUTS = "maxnuminputs"; //the maximum number of input instances
//	private static final String P_MAXCOMBINE = "maxcombine"; // the maximum number of instruction in an item of the library
//	private static final String P_DECAYFACTOR = "decayfactor"; // the decay factor of the frequency
//	private static final String P_MAXNUMTRYS = "numtries"; // the maximum number of trys the library can make, equivalent to SPTOUSIZE in C++ version
//	private static final String P_LOWERBOUND = "lowbound";
//	private static final String P_UPPERBOUND = "upbound"; 
//	private static final String P_INDIVIDUALBASE = "individual_base";  // the parameter based of the template individual
//	private static final String P_NUMBERUPDATE = "numupdate"; // the number of to-be-updated instructions each time
//	private static final String P_ELIMINATE_SOURCE = "eliminate.source";  // the selection method of to-be-eliminated instructions in the library
//	private static final String P_DISTRIBUTE = "distribute"; // the denominator when normalizing frequency
//	private static final String P_PTHRESOLD = "pthresold"; // the thresold of normalized frequency
//	
//	
//	private static final String P_TREE = "tree";
//	private static final String P_PIPE = "pipe";
//	private static final String P_FITNESS = "fitness";
//	
//	private static final double Inf = 1e7;
//	
//	private int NUMREF;      // number of reference semantic vectors
//	private int maxNumInstr; // maximum number of instructions in the library
//	
//	private int maxCombine; // the maximum number of instructions in an instruction combination
//	private int numRegs;   // the number of registers, for initializing semantic vectors
//	private int maxNumInput; // the maximum number of inputs, for initilizing semantic vectors
//	private int NumUpdate;  // the number of to-be-updated instructions each time
//	private double decayf; // the decay factor of the frequency
//	private int maxnumtry;  // the maximum number of trys of the library, equivalent to SPTOUSIZE in C++ version
//	
//	private SLSelectionMethod eliminsource;
//	
//	private double lowbound;
//	private double upbound;
//	
//	protected Parameter privateParameter; // the parameters of instructions (i.e., GPTree)
//	protected GPSymbolicRegression privateProblem;
//	
//	public int usingNumInput; // the actual number of inputs, for save computation resources 
//	
//	private SLBreedingPipeline pipe_prototype;
//	private LGPIndividual i_prototype;
//	private GPTreeStruct ins_prototype;
//	private SLFitness fit_prototype;
//	private String individual_base;
//	
//	public int DISTRIBUTE;
//	public double Pthresold;
//	
//	SVSet inRefSV;
////	ArrayList<GPTreeStruct[]> InsList;
////	ArrayList<SVSet> outRefSV;
////	ArrayList<Double> occurFre; //occurence frequency of instructions
//	
//	ArrayList<LibraryItem> ItemList;
//	
//	public void setup(final EvolutionState state, final Parameter base) {
//		Parameter def = new Parameter(P_SEMANTICLIBRARY);
//		
//		maxNumInstr = state.parameters.getInt(base.push(P_MAXNUMINSTRUCTION),def.push(P_MAXNUMINSTRUCTION),1);  // at least 1 tree for GP!
//	     if (maxNumInstr < 1) 
//	         state.output.fatal("A semantic library must have at least one instruction.",
//	             base.push(P_MAXNUMINSTRUCTION),def.push(P_MAXNUMINSTRUCTION));
//		
//		NUMREF = state.parameters.getIntWithDefault(base.push(P_NUMREFERENCE),def.push(P_NUMREFERENCE),5);  // at least 1 tree for GP!
//	     if (NUMREF < 1) 
//	         state.output.fatal("A semantic library must have at least one reference semantic vector.",
//	             base.push(P_NUMREFERENCE),def.push(P_NUMREFERENCE));
//	     
//	     numRegs = state.parameters.getInt(base.push(P_NUMREGS),def.push(P_NUMREGS),1);  
//	     if (numRegs < 1) 
//	         state.output.fatal("A semantic vecotr (in SemanticLibrary.setup()) must have at least one register.",
//	             base.push(P_NUMREGS),def.push(P_NUMREGS));
//	     
//	     maxNumInput = state.parameters.getInt(base.push(P_MAXNUMINPUTS),def.push(P_MAXNUMINPUTS),1);  
//	     if (maxNumInput < 1) 
//	         state.output.fatal("A semantic vecotr (in SemanticLibrary.setup()) must have at least one input.",
//	             base.push(P_MAXNUMINPUTS),def.push(P_MAXNUMINPUTS));
//	     
//	     maxCombine = state.parameters.getIntWithDefault(base.push(P_MAXCOMBINE),def.push(P_MAXCOMBINE),1);  
//	     if (maxCombine < 1) 
//	         state.output.fatal("An item of semantic librarys must have at least one instruction.",
//	             base.push(P_MAXCOMBINE),def.push(P_MAXCOMBINE));
//	     
//	     decayf = state.parameters.getDoubleWithDefault(base.push(P_DECAYFACTOR),def.push(P_DECAYFACTOR),0.8);  
//	     if (decayf <= 0.) 
//	         state.output.fatal("the decay factor of frequency must be larger than 0.0\n",
//	             base.push(P_DECAYFACTOR),def.push(P_DECAYFACTOR));
//	     
//	     maxnumtry = state.parameters.getInt(base.push(P_MAXNUMTRYS),def.push(P_MAXNUMTRYS),1);  
//	     if (maxnumtry < 1) 
//	         state.output.fatal("Semantic library must have at least one try.",
//	             base.push(P_MAXNUMTRYS),def.push(P_MAXNUMTRYS));
//	     
//	     lowbound = state.parameters.getDouble(base.push(P_LOWERBOUND),def.push(P_LOWERBOUND));  
//	     upbound = state.parameters.getDouble(base.push(P_UPPERBOUND),def.push(P_UPPERBOUND));  
//	     if(lowbound >= upbound) {
//	    	 System.err.print("the lower bound must be smaller than upper bound in the semantic library\n");
//	    	 System.exit(1);
//	     }
//	     
//	     NumUpdate = state.parameters.getInt(base.push(P_NUMBERUPDATE),def.push(P_NUMBERUPDATE),1);  
//	     if (NumUpdate < 1) 
//	         state.output.fatal("the number of to-be-updated instructions must be at least one.",
//	             base.push(P_NUMBERUPDATE),def.push(P_NUMBERUPDATE));
//	     
//	     eliminsource = (SLSelectionMethod)(state.parameters.getInstanceForParameter(
//	                base.push(P_ELIMINATE_SOURCE),def.push(P_ELIMINATE_SOURCE),SLBreedingSource.class));
//	     eliminsource.setup(state, base.push(P_ELIMINATE_SOURCE));
//	     
//	     individual_base = state.parameters.getString(base.push(P_INDIVIDUALBASE), def.push(P_INDIVIDUALBASE));
//	     if(individual_base == null) {
//	    	 System.err.print("we need to define the parameter base for the template individual in the semantic library\n");
//	    	 System.exit(1);
//	     }
//	     String[] bases = individual_base.split("\\.");
//	     Parameter p_ind = new Parameter(individual_base);
//	     
//	     i_prototype = (LGPIndividual)(state.parameters.getInstanceForParameter(
//	                p_ind,null, Individual.class));
//	     i_prototype.setup(state,p_ind);
//	     i_prototype.getTreeStructs().clear();
//	     //set the output registers of i_prototype as all the registers
//	     int [] tar = new int [numRegs];
//	     for(int r = 0; r<numRegs; r++) {
//	    	 tar[r] = r;
//	     }
//	     i_prototype.setOutputRegisters(tar);
//	     
//	     // load the breeding pipeline
//	     pipe_prototype = (SLBreedingPipeline)(state.parameters.getInstanceForParameter(
//	                base.push(P_PIPE),def.push(P_PIPE),SLBreedingPipeline.class));
//	     pipe_prototype.setup(state,base.push(P_PIPE));
//	     
//	     fit_prototype = (SLFitness)(state.parameters.getInstanceForParameter(
//	                base.push(P_FITNESS),def.push(P_FITNESS),SLFitness.class));
//	     fit_prototype.setup(state,base.push(P_PIPE));
//	     
//	     ins_prototype = (GPTreeStruct)(state.parameters.getInstanceForParameter(
//	                base.push(P_TREE),def.push(P_TREE),GPTree.class));
//	     ins_prototype.setup(state, base.push(P_TREE));
//	     
//	     DISTRIBUTE = state.parameters.getIntWithDefault(base.push(P_DISTRIBUTE),def.push(P_DISTRIBUTE),1000);  
//	     if (DISTRIBUTE <= 0.) 
//	         state.output.fatal("the DISTRIBUTE of updating frequency must be larger than 0\n",
//	             base.push(P_DISTRIBUTE),def.push(P_DISTRIBUTE));
//	     
//	     Pthresold = state.parameters.getDoubleWithDefault(base.push(P_PTHRESOLD),def.push(P_PTHRESOLD),0.95);
//	     if(Pthresold <=0.)
//	    	 state.output.fatal("the Pthresold of updating frequency must be larger than 0\n",
//		             base.push(P_PTHRESOLD),def.push(P_PTHRESOLD));
//	     
//	     privateParameter = def.push(P_TREE);
//	     
//	     
////	     inRefSV = new SVSet(NUMREF, maxNumInput, numRegs);
//	     
////	     InsList = new ArrayList<>(maxNumInstr);
////	     outRefSV = new ArrayList<>(maxNumInstr);
////	     occurFre = new ArrayList<>(maxNumInstr);
//	     
//	     ItemList = new ArrayList<>(maxNumInstr);
//	     
//	     
//	}
//	
//	public boolean addInstr(EvolutionState state, int thread, LibraryItem objitem) {
//		
//		if(this.ItemList.size() >= maxNumInstr) {
//			System.err.print("semantic library esceeds the size limit\n");
//			System.exit(1);
//		}
//		
//		//check whether these instructions exist in the library
//		//LibraryItem item = new LibraryItem(ins_seq, this.NUMREF, this.maxNumInput, this.numRegs, this.fit_prototype);
//		
//		boolean noexist = checkNOexistence(state, thread, objitem);
//			
//		if(noexist) {
//			//LibraryItem tmp = new LibraryItem(ins_seq, outSV, 0.0, true, fit_prototype);
////			ItemList.add(tmp);
//			ItemList.add(objitem);
//			return true;
//		}
//			
//		return false;
//	}
//	
//	public boolean setInstr(EvolutionState state, int thread, int index, LibraryItem item) {
//		if(index >= this.ItemList.size() || index < 0) {
//			System.err.print("index " + index +" is out of the range of semantic library\n");
//			System.exit(1);
//		}
//		
//		//check whether these instructions exist in the library
//		boolean noexist = checkNOexistence(state, thread, item);
//
//		if(noexist) {
//			ItemList.set(index, item);
//			return true;
//		}
//			
//		return false;
//	}
//	
//	public int getMaxCombine() {
//		return maxCombine;
//	}
//	public int getNumRef() {
//		return NUMREF;
//	}
//	public int getMaxNumInput() {
//		return maxNumInput;
//	}
//	public int getNumRegs() {
//		return numRegs;
//	}
//	public int getLibrarySize() {
//		
//		return ItemList.size();
//	}
//	
//	public LibraryItem getItem(int index) {
//		return ItemList.get(index);
//	}
//	
//	public GPProblem getProblem() {
//		return privateProblem;
//	}
//	
//	public SLFitness getFitnessProto() {
//		return fit_prototype;
//	}
//	
//	public int getIndexOf(LibraryItem item) {
//		return ItemList.indexOf(item);
//	}
	
	@Override
	public void initialize(final EvolutionState state, final int thread, final Problem problem) {
		privateProblem = (GPSymbolicRegression) problem;
		
		GPSymbolicRegression SRproblem = (GPSymbolicRegression) privateProblem;
		
		if(SRproblem.datanum > maxNumInput) {
			System.err.print("The number of data instances exceeds the maximum number of data of semantic library\n");
			System.exit(1);
		}
		
		inRefSV = new SVSet(NUMREF, SRproblem.datanum, numRegs);
		
		for(SemanticVector sv : inRefSV ) {
	    	 sv.randSetSem(state, thread, lowbound, upbound);
	     }
		
		for(int i =0;i<maxNumInstr;i++){
			//GPTreeStruct tree = new GPTreeStruct();
			GPTreeStruct[] ins_seq = new GPTreeStruct[maxCombine];
			
//			GPTreeStruct tree = (GPTreeStruct)(state.parameters.getInstanceForParameterEq(
//                    privateParameter,null,GPTree.class));
			GPTreeStruct tree = ins_prototype.lightClone();
			tree.owner = null; 
            tree.status = false;
            tree.effRegisters = new HashSet<Integer>(0);
            //tree.setup(state,privateParameter);
            
			tree.buildTree(state, thread);
			
			ins_seq[0] = tree;
			
			LibraryItem item = new LibraryItem(ins_seq, this);

			//check whether these instructions exist in the library
//			SVSet outSV = null;
//			boolean noexist = checkNOexistence(state, thread, ins_seq, outSV);
			boolean noexist = checkNOexistence(state, thread, item);

				if(noexist) {
//					ItemList.add(new LibraryItem(ins_seq, outSV, 0.0, fit_prototype));
					ItemList.add(item);
				}
			}
				
		
	}
	
	@Override
	public SVSet evalInstr(EvolutionState state, int thread, LibraryItem item, GPProblem problem) {
		
		if(i_prototype.getRegisters().length != numRegs) {
			System.err.print("the semantic library has inconsistent number of registers with instructions.\n");
			System.exit(1);
		}
		
		GPSymbolicRegression SRproblem = (GPSymbolicRegression) problem;
		
		SVSet tmpout = new SVSet(NUMREF, SRproblem.datanum, numRegs);
		
		//see the instruction as an individual
		//1) reset i_prototype  2) add instructions to form the i_prototype
		while(i_prototype.getTreeStructs().size()>0) {
			i_prototype.removeTree(0);
		}
		
		for(int i = 0; i<item.instructions.length; i++) {
			i_prototype.addTree(i, item.instructions[i]);
		}
		
		for(int svi = 0; svi<NUMREF; svi++) {
			
			SemanticVector inputSV = inRefSV.get(svi);
			SemanticVector outSV = new SemanticVector(SRproblem.datanum, numRegs);
			
			for(int in = 0; in<SRproblem.datanum; in++) {
				DoubleData tmp = new DoubleData();
				
				SRproblem.X = new double[SRproblem.datadim];
				DoubleData input = new DoubleData();
				
				//set inputs and registers
				for(int d = 0; d<SRproblem.datadim; d++) {
					SRproblem.X[d] = SRproblem.getData()[in][d];
				}
				for(int r = 0; r<numRegs; r++) {
					i_prototype.setRegister(r, inputSV.get(in*numRegs+r));
				}
				
				i_prototype.getFlowController().execute(state, thread, input, null, i_prototype, problem);
				
				outSV.setSemByRegister(in, i_prototype.getRegisters());
				
				
			}
			
			tmpout.set(svi, outSV);
		}
		
		item.outSVs = tmpout;
		item.evaluated = true;
		
		state.nodeEvaluation += (NUMREF) * i_prototype.size();
		
		return tmpout;
	}
	
//	protected boolean checkNOexistence(EvolutionState state, int thread, LibraryItem objitem) {
//		boolean noexist = true;
//		for(LibraryItem item : ItemList) {
//			
//			GPTreeStruct[] ts = item.instructions;
//			
//			if(ts.length != objitem.instructions.length) continue;
//			boolean same = true;
//			for(int len = 0; len < objitem.instructions.length; len++) {
//				same = same && ts[len].toString().equals(objitem.instructions[len].toString()); 
//			}
//			if(same) {noexist = false; break;}
//		}
//		if(noexist) {
//			//check whether the output reference SVs are duplicated with someone in the library
//			if(! objitem.evaluated )
//				evalInstr(state, thread, objitem, privateProblem);
//			
//			for(LibraryItem item : ItemList) {
//				
//				SVSet os = item.outSVs;
//				
//				if(os.isequalto(objitem.outSVs)) {
//					if(state.random[thread].nextDouble()>0.5) noexist = false; break;
//				}
//			}
//		}
//		return noexist;
//	}
	
//	public void updateLibrary(EvolutionState state, int thread) {
//
//		ArrayList<LibraryItem> trialList = new ArrayList<>(NumUpdate);
//		ArrayList<LibraryItem> eliminatorList = new ArrayList<>(NumUpdate);
//		
//		for(int up = 0; up < NumUpdate; up++) {
//			LibraryItem trial = pipe_prototype.produce(state, thread, this);
//			trialList.add(trial);
//			
//			LibraryItem eliminator = eliminsource.produce(state, thread, this);
//			if(!eliminatorList.contains(eliminator)) {
//				eliminatorList.add(eliminator);
//			}
//			
//		}
//		
//		for(int up = 0, el = 0; up < NumUpdate && el < eliminatorList.size(); up++) {
//			
//			LibraryItem trial = trialList.get(up);
////			boolean noexist = checkNOexistence(state, thread, trial);
////			for(int tr = 1; !noexist && tr<maxnumtry; tr++) {
////				trial = pipe_prototype.produce(state, thread, this);
////				
////				noexist = checkNOexistence(state, thread, trial);
////				
////			}
//
//			if(ItemList.size() < maxNumInstr) {
//				addInstr(state, thread, trial);  
//			}
//			else {
//				//select to-be-eliminated instruction
//				LibraryItem  eliminator = eliminatorList.get(el);
//				
//				int index = getIndexOf(eliminator);
//				
//				if(setInstr(state, thread, index, trial)) {
//					el ++;
//				}
//			}
//
//		}
//		
//		//remove over-used items
////		for(int i = 0; i<ItemList.size(); i++) {
////			LibraryItem it = ItemList.get(i);
////			if(it.frequency > this.DISTRIBUTE) {
////				ItemList.remove(i);
////				i--;
////			}
////		}
//		
//		//udate frequency
//		for(LibraryItem it : this.ItemList) {
//			it.frequency *= decayf;
//		}
//	}
//	
//	public SemanticVector estimateX(final LibraryItem item, final SemanticVector Y_star) {
//		SVSet recordSV = item.outSVs;
//		
//		int nearest_index = 0;
//		double diff = recordSV.get(nearest_index).sv_diff(Y_star);
//		
//		for(int r = 1; r<NUMREF; r++) {
//			double tmp_diff = recordSV.get(nearest_index).sv_diff(Y_star);
//			if(tmp_diff < diff) {
//				diff = tmp_diff;
//				nearest_index = r;
//			}
//		}
//		
//		if(diff == 0) {
//			return inRefSV.get(nearest_index);
//		}
//		
//		//get effective registers
//		Set<Integer> readRegs = item.instructions[0].collectReadRegister();
//		for(int in = 1; in<item.instructions.length; in++) {
//			//remove the destination registers of following instructions from readRegs 
//			readRegs.remove(((WriteRegisterGPNode)item.instructions[in - 1].child).getIndex());
//			
//			//refill the source registers
//			readRegs.addAll(item.instructions[in].collectReadRegister());
//		}
//		
//		//estimate X
//		SemanticVector X_star = new SemanticVector(Y_star);
//		for(int k = 0; k<X_star.size(); k++) {
//			if(readRegs.contains(k % numRegs)) {
//				double x0k = inRefSV.get(nearest_index).get(k);
//				double dey_star = Y_star.get(k) - recordSV.get(nearest_index).get(k);
//				double sumX = 0, sumY = 0;
//				for(int i = 0;i<NUMREF; i++) {
//					if(i != nearest_index) {
//						sumX += inRefSV.get(i).get(k) - x0k;
//						sumY += recordSV.get(i).get(k) - recordSV.get(nearest_index).get(k);
//					}
//				}
//				
//				if(dey_star == 0 || sumX == 0) {
//					X_star.set(k, x0k);
//				}
//				else if (sumY != 0) {
//					X_star.set(k, x0k+dey_star*(sumX / sumY));
//				}
//				else
//					X_star.set(k, Inf);
//				
//				if(X_star.get(k) > Inf || X_star.get(k) < -Inf) {
//					X_star.set(k, Inf);
//				}
//			}
//		}
//		
//		return X_star;
//	}
//	
//	public SemanticVector estimateY(final LibraryItem item, final SemanticVector X_star) {
//		SVSet recordSV = item.outSVs;
//		
//		int nearest_index = 0;
//		double diff = inRefSV.get(nearest_index).sv_diff(X_star);
//		for(int r = 1; r<NUMREF; r++) {
//			double tmp_diff = inRefSV.get(r).sv_diff(X_star);
//			if(tmp_diff < diff) {
//				diff = tmp_diff;
//				nearest_index = r;
//			}
//		}
//		
//		if(diff == 0) {
//			return recordSV.get(nearest_index);
//		}
//		
//		//get effective registers
//		int itemlen = item.instructions.length;
//		Set<Integer> WrRegs = new HashSet<>();
//		WrRegs.add(((WriteRegisterGPNode)item.instructions[itemlen - 1].child).getIndex());
//		for(int in = itemlen - 2; in>=0; in--) {
//			//remove the source registers of preceding instructions from WrRegs 
//			WrRegs.remove(item.instructions[in].collectReadRegister());
//			
//			//refill the destination registers
//			WrRegs.add(((WriteRegisterGPNode)item.instructions[in].child).getIndex());
//		}
//		
//		//estimate Y
//		SemanticVector Y_star = new SemanticVector(X_star);
//		for(int k = 0; k<Y_star.size(); k++) {
//			if(WrRegs.contains(k % numRegs)) {
//				double y0k = recordSV.get(nearest_index).get(k);
//				double dex_star = X_star.get(k) - inRefSV.get(nearest_index).get(k);
//				double sumX = 0, sumY = 0;
//				for(int i = 0; i<NUMREF; i++) {
//					if(i != nearest_index) {
//						sumX += inRefSV.get(i).get(k) - inRefSV.get(nearest_index).get(k);
//						sumY += recordSV.get(i).get(k) - y0k;
//					}
//				}
//				if(dex_star == 0 || sumY == 0) {
//					Y_star.set(k, y0k);
//				}
//				else if(sumX != 0) {
//					Y_star.set(k, y0k+dex_star*(sumY / sumX));
//				}
//				else {
//					Y_star.set(k, Inf);
//				}
//				
//				if(Y_star.get(k) > Inf || Y_star.get(k) < -Inf) {
//					Y_star.set(k, Inf);
//				}
//			}
//		}
//		
//		return Y_star;
//	}
//	
//	public int selectInstr(EvolutionState state, int thread, final SemanticVector inputS, final SemanticVector Sdes, SemanticVector DI, SemanticVector EO) {
//		//return the index of the selected item
//		int tci = -1, ci = 0;
//		double min_diff = 1e6, diff=0;		
//		for(int sp = 0; sp < maxnumtry; sp++) {
//			ci = state.random[thread].nextInt(ItemList.size());
//			
//			if(tci == -1 || state.random[thread].nextDouble() > Math.min(ItemList.get(ci).frequency / DISTRIBUTE, Pthresold)) 
//			{
//				diff = estimateY(ItemList.get(ci), inputS).sv_diff(Sdes);
//				if(tci == -1 || diff < min_diff || (diff == min_diff && state.random[thread].nextBoolean())) {
//					min_diff = diff;
//					tci = ci;
//				}
//			}
//		}
//		
//		SemanticVector tmpDI = estimateX(ItemList.get(tci), Sdes);
//		DI.assignfrom(tmpDI);
//		SemanticVector tmpEO = estimateY(ItemList.get(tci), inputS);
//		EO.assignfrom(tmpEO);
//		
//		ItemList.get(tci).frequency += 1;
//		
//		return tci;
//	}
}
