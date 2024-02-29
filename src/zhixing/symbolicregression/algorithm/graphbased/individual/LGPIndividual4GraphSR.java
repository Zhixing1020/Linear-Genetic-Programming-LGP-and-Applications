package zhixing.symbolicregression.algorithm.graphbased.individual;

import java.util.Vector;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPFunctionSet;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.util.Parameter;
import yimei.jss.gp.data.DoubleData;
import yimei.jss.gp.terminal.TerminalERC;
import zhixing.cpxInd.algorithm.Graphbased.individual.GraphAttributes;
import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
//import zhixing.jss.cpxInd.individual.LGPIndividual4DJSS;
import zhixing.symbolicregression.individual.LGPIndividual4SR;
import zhixing.symbolicregression.individual.LGPInterface4SR;
import zhixing.symbolicregression.individual.primitive.InputFeatureGPNode;
import zhixing.symbolicregression.optimization.GPSymbolicRegression;

public class LGPIndividual4GraphSR extends LGPIndividual4Graph implements GraphAttributes, LGPInterface4SR{
	//the code in this class is copyed from LGPIndividual4Graph, except those SR problem specific functions: setup(), getFrequency(), getAM(). 
	
	protected static Vector<String> primitives = new Vector();	
	protected double [] frequency;
	protected int dimension = 1;
	protected int dimension_fun = 1;
	//protected int dimension_reg = 1;
	protected int dimension_cons = 1;

	
	private double multiplyVectors(double [][] a, double [][] b){
		
		if(a.length != b.length || a[0].length != b[0].length) {
			System.err.print("inconsistent dimension in multiplyVectors of LGPIndividual4Graph \n");
			System.exit(1);
		}
		
		double res = 0;
		for(int i = 0;i<a.length;i++){
			for(int j = 0;j<a[i].length;j++){
				res += a[i][j] * b[i][j]; 
			}
		}
		return res;
	}
	private double getNorm2(double [][] a){
		double res = 0;
		for(int i = 0;i<a.length;i++){
			for(int j = 0;j<a[i].length;j++){
				res += a[i][j]*a[i][j];
			}
		}
		return Math.sqrt(res);
	}
	
	private double getNorm1(double [][] a){
		double res = 0;
		for(int i = 0;i<a.length;i++){
			for(int j = 0;j<a[i].length;j++){
				res += Math.abs(a[i][j]);
			}
		}
		return res;
	}
	
	
	public int getDimension(){
		return dimension;
	}
	
	public int getDimension_fun() {
		return dimension_fun;
	}
	
	public int [] getStartEnd_fun() {
		return new int[] {0, dimension_fun};
	}
	
//	public int getDimension_reg() {
//		return dimension_reg;
//	}
	
//	public int [] getStartEnd_reg() {
//		return new int [] {dimension_fun, dimension_fun + dimension_reg};
//	}
	
	public int getDimension_cons() {
		return dimension_cons;
	}
	
	public int [] getStartEnd_cons() {
		return new int [] {dimension_fun, dimension};
	}
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		//set up the individual prototype 
		 super.setup(state,base);  //��Ҫ��LGP4Graph ��д�� ���ܴ�LGP4Graph�м̳�
		 Parameter def = defaultBase();
		 
//	    setPrimitives4Problem(state);		 
		 
	}
	
	
	public double[] getFrequency(int start,int end){
		//return the functions and terminals' frequency between [start, end) instruction, only counts effective instruction
		if(dimension <=0){
			System.err.print("dimension of adjacent matrixes is less than or equal to 0 in LGPIndividual4Graph\n");
			System.exit(1);
		}
		else if(start<0||start>=end||end<0||end>getTreelist().size()){
			System.err.print("start or end arguments are out of range in LGPIndividual4Graph\n");
			System.exit(1);
		}
		else{
			frequency = new double[dimension];
			for(int i = start;i<end;i++){
				GPTreeStruct tree = getTreelist().get(i);
				
				if(!tree.status) continue;
				
				int si;
				//get the index of the WriteRegister
//				String name = tree.child.toString().split("=")[0];
//				si = primitives.indexOf(name);
//				frequency[si] ++;
				
				//get the index of its function
				String name = tree.child.children[0].toString();
				si = primitives.indexOf(name);
				frequency[si] ++;
				
				for(int j = 0;j<tree.child.children[0].expectedChildren();j++){
					if(tree.child.children[0].children[j] instanceof TerminalERC){
						name = ((TerminalERC)tree.child.children[0].children[j]).getTerminal().toString();
						si = primitives.indexOf(name);
						frequency[si] ++;
					}
					else if(tree.child.children[0].children[j] instanceof InputFeatureGPNode) {
						name = (tree.child.children[0].children[j]).toString();
						si = primitives.indexOf(name);
						frequency[si] ++;
					}
					//else if it is ReadRegisterGPNode
					else{
						//check whether it is initialized by constants
						si = ((ReadRegisterGPNode)tree.child.children[0].children[j]).getIndex();
						
						
						int k = i - 1;
						for(; k>=0; k--){
							if(getTreelist().get(k).status && ((WriteRegisterGPNode)getTreelist().get(k).child).getIndex() == si){
								break;
							}
						}
						
						if(k<0){
							//because we use 1 to initialize registers in LGP for SR. we do not have to do anything when the node is a ReadRegisterGPNode
							
//							name = JobShopAttribute.relativeAttributes()[si % JobShopAttribute.relativeAttributes().length].toString();
//							si = primitives.indexOf(name);
//							frequency[si] ++;
							
						}
//						else {
//							name = tree.child.children[0].children[j].toString();
//							si = primitives.indexOf(name);
//							frequency[si] ++;
//						}
					}
					
				}
			}
		}
		return frequency;
	}

	public double[] getFrequency(){
		return getFrequency(0, this.getTreesLength());
	}
	
	public double[][] getAM(int start, int end){
		//return the adjacency matrix of instruction sequence [start, end), only counts effective instruction
		if(start<0||start>=end||end>getTreelist().size()){
			System.err.print("illegal arguments in getAM() of LGPIndividual4Graph\n");
			System.exit(1);
		}
		
		double[][] adjacencyMatrix = new double [dimension][dimension];
		
		//update AM based on genotype,  for each effective instruction, check its parents. If it is the root (no parent), all primitives to this operation + 1.
		for(int i = start; i<end; i++){
			
			//for each instruction, get the index of its function and children/constants
			GPTreeStruct tree = getTreelist().get(i);
			
			if(!tree.status) continue;
			
			int si,tj;
			//get the index of its function
			String name = tree.child.children[0].toString();
			tj = primitives.indexOf(name);
			si = -1;
			
			//see which following instructions use these writer register, then it is the parent of this function
			int writeRegInd = ((WriteRegisterGPNode)tree.child).getIndex();
			for(int j = start+1;j<getTreelist().size();j++) {
				if(getTreelist().get(j).status && getTreelist().get(j).collectReadRegister().contains(writeRegInd)) {
					name = getTreelist().get(j).child.children[0].toString();
					si = primitives.indexOf(name);
					break;
				}
			}
			if(si>=0) {
				adjacencyMatrix[si][tj] ++;
			}
//			else {
//				for(int sii = 0;sii<dimension;sii++) {
//					adjacencyMatrix[sii][tj] ++;
//				}
//			}
			
			//see whether input arguments of instruction tj  has constants
			for(int p = 0;p<tree.child.children[0].expectedChildren();p++) {
				if(tree.child.children[0].children[p] instanceof TerminalERC){
					name = ((TerminalERC)tree.child.children[0].children[p]).getTerminal().toString();
					int tjj = primitives.indexOf(name);
					adjacencyMatrix[tj][tjj] ++;
				}
				else if(tree.child.children[0].children[p] instanceof InputFeatureGPNode) {
					name = (tree.child.children[0].children[p]).toString();
					int tjj = primitives.indexOf(name);
					adjacencyMatrix[tj][tjj] ++;
				}
				//else if it is ReadRegisterGPNode, check whether it is initialized by constants
				else{
					int index = ((ReadRegisterGPNode)tree.child.children[0].children[p]).getIndex();
					
					int j = i - 1;
					for(; j>=0; j--){
						if(getTreelist().get(j).status && ((WriteRegisterGPNode)getTreelist().get(j).child).getIndex() == index){
							break;
						}
					}
					
					if(j<0){
						adjacencyMatrix[tj][getStartEnd_cons()[0]+index] ++;
					}
				}
			}
			
			//for each source register, get its index and update AM
//			for(int p = 0;p<tree.child.children[0].expectedChildren();p++){
//				if(tree.child.children[0].children[p] instanceof TerminalERC){
//					name = ((TerminalERC)tree.child.children[0].children[p]).getTerminal().toString();
//					tj = primitives.indexOf(name);
//					adjacencyMatrix[si][tj] ++;
//				}
//				else{
//					int register = ((ReadRegisterGPNode)tree.child.children[0].children[p]).getIndex();
//					for(int s = trees.indexOf(tree)-1;s>=start;s--){
//						if(((WriteRegisterGPNode)this.trees.get(s).child).getIndex() == register){
//							name = trees.get(s).child.children[0].toString();
//							tj = primitives.indexOf(name);
//							adjacencyMatrix[si][tj] ++;
//							break;
//						}
//					}
//				}
//			}
			
		}
		
		return adjacencyMatrix;
	}
	
	public double[][] getAM(){
		return getAM(0, getTreelist().size());
	}
	
	private void copyLGPproperties(LGPIndividual4GraphSR obj){
		this.dimension = obj.getDimension();
		this.dimension_fun = obj.getDimension_fun();
		//this.dimension_reg = obj.getDimension_reg();
		this.dimension_cons = obj.getDimension_cons();
	}
	@Override
	public  Object clone(){
		LGPIndividual4GraphSR ind = (LGPIndividual4GraphSR) (super.clone());
		
		ind.copyLGPproperties(this);
		
		return ind;
	}
	
	@Override
	public  LGPIndividual4GraphSR lightClone(){
		LGPIndividual4GraphSR ind = (LGPIndividual4GraphSR) (super.lightClone());
		
		ind.copyLGPproperties(this);
		
		return ind;
	}
	
	@Override
	public void rebuildIndividual(EvolutionState state, int thread){
		super.rebuildIndividual(state, thread);
		
		//randomly reset AMs
		//int num = state.random[thread].nextInt(trees.size())+1;
		GPFunctionSet set = this.getTree(0).constraints((GPInitializer)state.initializer).functionset;
		
	}
	@Override
	protected void setPrimitives4Problem(EvolutionState state) {
		//get the function set
		 GPFunctionSet set = this.getTree(0).constraints((GPInitializer)state.initializer).functionset;  //all trees have the same function set	
		 
		 //collect the name of all functions and constants
		 //functions
		 int i = 0;
		 dimension = 0;
		 dimension += set.nonterminals_v.size();
		 dimension_fun = set.nonterminals_v.size();
		 for(;i<set.nonterminals_v.size();i++){
			 primitives.add(set.nonterminals_v.get(i).toString());
		 }
		//registers
//		 dimension += numRegs;
//		 dimension_reg = numRegs;
//		 for(i=0;i<numRegs;i++){
//			 primitives.add("R"+i);
//		 }
		 //constants
		 if(state.evaluator.p_problem instanceof GPSymbolicRegression) {
			 dimension += ((GPSymbolicRegression) state.evaluator.p_problem).datadim;
			 dimension_cons = ((GPSymbolicRegression) state.evaluator.p_problem).datadim;
			 for(i=0;i<dimension_cons;i++){				 
				 InputFeatureGPNode m = new InputFeatureGPNode(i, dimension_cons);
				 primitives.add(m.toString());
			 }
		}
		
	}
}
