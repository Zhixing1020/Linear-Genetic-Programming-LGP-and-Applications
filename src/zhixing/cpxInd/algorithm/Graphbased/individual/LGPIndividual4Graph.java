package zhixing.cpxInd.algorithm.Graphbased.individual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.math3.util.Pair;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPFunctionSet;
import ec.gp.GPIndividual;
import ec.gp.GPInitializer;
import ec.gp.GPNode;
import ec.util.Parameter;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;
//import zhixing.jss.cpxInd.individual.LGPIndividual4DJSS;
//import yimei.jss.gp.terminal.JobShopAttribute;
//import yimei.jss.gp.terminal.TerminalERC;

public abstract class LGPIndividual4Graph extends zhixing.cpxInd.individual.LGPIndividual implements GraphAttributes{
	
	protected static Vector<String> primitives = new Vector();	
	public double [] frequency;
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
	
	abstract public double[] getFrequency(int start,int end);
	
	public double[] getFrequency(){
		return getFrequency(0, this.getTreesLength());
	}
	
	abstract public double[][] getAM(int start, int end);
	
	public double[][] getAM(){
		return getAM(0, getTreelist().size());
	}
	
//	protected void divertMomentum(){
//		//count how many elements larger than 1.0
//		
//		
//		//reset temporary momentum as 0 matrix
//		
//		
//		//randomly select smaller elements and add one in temporary momentum
//		
//	}
	
	@Override
	public void setup(final EvolutionState state, final Parameter base)
	{
		//set up the individual prototype 
		 super.setup(state,base); 	
		 
		 setPrimitives4Problem(state);
	}
	
	abstract protected void setPrimitives4Problem(final EvolutionState state);

	private void copyLGPproperties(LGPIndividual4Graph obj){
		this.dimension = obj.getDimension();
		this.dimension_fun = obj.getDimension_fun();
		//this.dimension_reg = obj.getDimension_reg();
		this.dimension_cons = obj.getDimension_cons();
	}
	@Override
	public  Object clone(){
		LGPIndividual4Graph ind = (LGPIndividual4Graph) (super.clone());
		
		ind.copyLGPproperties(this);
		
		return ind;
	}
	
	@Override
	public  LGPIndividual4Graph lightClone(){
		LGPIndividual4Graph ind = (LGPIndividual4Graph) (super.lightClone());
		
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
	public static Vector<String> getPrimitives() {
		return primitives;
	}
}
