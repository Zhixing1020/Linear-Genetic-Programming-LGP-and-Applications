package zhixing.cpxInd.algorithm.semantic.library;

import java.util.Vector;

import ec.EvolutionState;

public class SemanticVector extends Vector<Double>{
	
	private int maxnumInput = 1;
	private int maxnumReg = 1;
	
	public SemanticVector(int numInput, int numReg) {
		super(numInput*numReg);
		this.clear();
		maxnumInput = numInput;
		maxnumReg = numReg;
		for(int i = 0; i<maxnumInput*maxnumReg; i++) {
			this.add(0.);
		}
	}
	
	public SemanticVector(final SemanticVector obj) {
		super();
		assignfrom(obj);
	}
	
	public SemanticVector() {
		super();
		this.add(0.);
	}
	
	public void assignfrom(final SemanticVector obj) {
		this.clear();
		maxnumInput = obj.getMaxNumInput();
		maxnumReg = obj.getMaxNumReg();
		
		for(int i = 0; i<maxnumInput*maxnumReg;i++) {
			this.add(obj.get(i));
		}
	}
	
	public double sv_diff(SemanticVector sv2) {
		if(this.size() != sv2.size() ) {
			System.err.println("dimension inconsistent in vec_diff\\n");
			System.exit(1);
		}
		
		double diff = 0;
		for(int i = 0; i<this.size(); i++) {
			diff += Math.abs(this.get(i) - sv2.get(i));
		}
		
		return diff;
	}
	
	public void setSemByRegister(int index, double[] sv2) {
		if(sv2.length <= 0) {
			System.err.print("the len in SemanticVector.setSemByRegister() must be larger than 0\n");
			System.exit(1);
		}
		if(maxnumReg != sv2.length) {
			System.err.print("the number of registers in SemanticVector.setSemByRegister() is inconsistent with input parameter sv2\n");
			System.exit(1);
		}
		for(int l = index*maxnumReg, ll = 0; ll<maxnumReg; l++, ll++) {
			this.set(l, sv2[ll]);
		}
	}
	
	public void randSetSem(EvolutionState state, int thread, double lb, double ub) {
		if(ub < lb) {
			System.err.print("the upper bound in SemanticVector.randSetSem must be larger than or equal to its lower bound\n");
			System.exit(1);
		}
		for(int i = 0;i<this.size();i++) {
			this.set(i, lb+state.random[thread].nextDouble()*(ub - lb));
		}
	}
	
	public int getMaxNumInput() {
		return maxnumInput;
	}
	
	public int getMaxNumReg() {
		return maxnumReg;
	}
	
	public Object clone() {
		SemanticVector obj = new SemanticVector(this);
		return obj;
	}
}
