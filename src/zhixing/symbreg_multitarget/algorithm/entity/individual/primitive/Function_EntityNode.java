package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import yimei.jss.gp.data.DoubleData;
import zhixing.cpxInd.individual.primitive.Args;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.symbreg_multitarget.individual.LGPInterface4SRMT;
import zhixing.symbreg_multitarget.individual.primitive.EntityInterface4SRMT;
import zhixing.optimization.SupervisedProblem;

public abstract class Function_EntityNode extends Entity implements EntityInterface4SRMT{

	protected double fitness = 0;
	
	public final static int MAX_SAMPLE = 1000;
	public final static double upper_bound = 1e7;
	public final static double lower_bound = -1e7;
	protected ArrayList<double []> ValueList = new ArrayList<>();
//	protected ArrayList<Double> ResList = new ArrayList<>();
	protected ArrayList<Integer> IndexList = new ArrayList<>();
//	protected ArrayList<double []> TargetList = new ArrayList<>();
	
//	protected final double dropout_rate = 0.0;
//	protected boolean toDrop = true;
	
	public double getEntityFitness() {
		return fitness;
	}
	
	public void setEntityFitness(double fit) {
		fitness = fit;
	}
	
	@Override
	public double getAttributes(Object obj, Args args, int index, int bias) {
		int ind = 0;
		int xi = 0;
		double res = 0;
		
		System.err.print("Function_EntityNode has not defined getAttributes()\n");
		System.exit(1);
		
		return res;
	}
	
	protected void addValues2List(double [] vals, double res, EvolutionState state, int thread, Problem problem) {
		
		int X_index = ((SupervisedProblem)problem).getX_index();
		int n_instance = ((SupervisedProblem)problem).getDatanum();
		int batch = Math.max(1, n_instance / 20);
		
//		ArrayList<Double[]> output = ((SupervisedProblem)problem).getDataOutput();
//		
//		double[] target = new double[output.get(X_index).length];
//		for(int t = 0; t<target.length; t++) {
//			target[t] = output.get(X_index)[t];
//		}
		
		if(X_index == 0) clearList();
		
		if(state == null || ! ((SupervisedProblem)problem).istraining() ) return;
		
		if(X_index < MAX_SAMPLE) {
			ValueList.add(vals);
//			ResList.add(res);
			IndexList.add(X_index);
		}
		else {
			double duplicate_times = Math.ceil(((double)n_instance) / MAX_SAMPLE);
			//we expect the selection probability of an instance (a) is nearly 1/(d+1) where "d" is the duplication times. thus we have pow(a^d, 1/d) = 1/(d+1), thus a=pow(1/(d+1), 1/d) 
			double p = Math.pow(1./duplicate_times, 1./(duplicate_times-1)); 
			int ind = state.random[thread].nextInt(MAX_SAMPLE);
			
			if(state.random[thread].nextDouble() > p) {
				ValueList.set(ind, vals);
//				ResList.set(ind, res);
				IndexList.set(ind, X_index);
			}
		}
		
//		if(X_index == ((SupervisedProblem)problem).getDatanum())
//		if(state != null ) {
//			prepareTraining(state, thread);
//			
//			if( !toDrop ) {
////				if(state.generation > 0 
////						&& state.generation < state.numGenerations - 1 
////						&& X_index > 0 && (X_index % batch == 0)) {
////					this.arguments.varyNode(state, thread, null);
////				}
//			}
//			
//		}
		
//		if(ValueList.size() >= MAX_SAMPLE) {
//			int insert = state.random[thread].nextInt(ValueList.size());
//			ValueList.set(insert, vals);
//		}
//		else {
//			ValueList.add(vals);
//		}
		
	}
	
	protected void clearList() {
		ValueList.clear();
//		ResList.clear();
		IndexList.clear();
	}
	
	public void copyList(Function_EntityNode p1) {
		this.ValueList = new ArrayList<>();
//		this.ResList = new ArrayList<>();
		this.IndexList = new ArrayList<>();
		
		for(double [] src : p1.getValueList()) {
			double [] tmp = new double [src.length];
			for(int i = 0; i<src.length; i++) {
				tmp[i] = src[i];
			}
			this.ValueList.add(tmp);
		}
		
//		for(Double src : p1.getResList()) {
//			double tmp = src;
//			this.ResList.add(tmp);
//		}
		
		for(Integer src : p1.getIndexList()) {
			int tmp = src;
			this.IndexList.add(tmp);
		}
	}
	
//	protected void prepareTraining(EvolutionState state, int thread) {
//		int X_index = ((SupervisedProblem)state.evaluator.p_problem).X_index;
//		
//		//reset the training variables
//		if(X_index == 0) {
//			clearList();
//			if(state.random[thread].nextDouble() < dropout_rate) {
//				toDrop = true;
//			}
//			else {
//				toDrop = false;
//			}
//		}
//	}
	
	public ArrayList<double []> getValueList(){
		return ValueList;
	}
//	public ArrayList<Double> getResList(){
//		return ResList;
//	}
	public ArrayList<Integer> getIndexList(){
		return IndexList;
	}
	
	@Override
    public GPNode lightClone() {
    	GPNode n = super.lightClone();
    	
    	((Function_EntityNode)n).ValueList = new ArrayList<>();
    	if(ValueList.size() > 0) {
    		int dim = ValueList.get(0).length;
        	
        	for(int i = 0; i<ValueList.size(); i++) {
        		double [] tmp = new double [dim];
        		
        		for(int d = 0;d<dim; d++) {
        			tmp[d] = ValueList.get(i)[d];
        		}
        		
        		((Function_EntityNode)n).ValueList.add(tmp);
        	}
    	}
    	
//    	((Function_EntityNode)n).ResList = new ArrayList<>();
//    	if(ResList.size() > 0) {
//        	
//        	for(int i = 0; i<ResList.size(); i++) {
//        		double tmp = ResList.get(i);
//        		((Function_EntityNode)n).ResList.add(tmp);
//        	}
//    	}
    	
    	((Function_EntityNode)n).IndexList = new ArrayList<>();
    	if(IndexList.size() > 0) {
        	
        	for(int i = 0; i<IndexList.size(); i++) {
        		int tmp = IndexList.get(i);
        		((Function_EntityNode)n).IndexList.add(tmp);
        	}
    	}
    	
    	return n;
    }
	

    public void pretrainNode(final EvolutionState state, final int thread) {
		
		//visit all data instances, assuming the input of this function is the initial value of registers
		SupervisedProblem problem = ((SupervisedProblem)state.evaluator.p_problem);
		int n_instances = problem.getDatanum();
		double [] vals = new double [children.length]; //because the initial value of registers is 0.0.
//		for(int v = 0; v<children.length; v++) {
//			vals [v] = LGPInterface4SRMT.initVal;
//		}
		arguments.resetNode(state, thread);
		
		for(int i = 0; i<n_instances; i++) {
			problem.setX_index(i);
			addValues2List(vals, 0.0, state, thread, (Problem) problem);  //the second argument is not used in this function
		}
		
    	arguments.resetNode(state, thread);
    }
	
	public int expectedChildren() {
    	return CHILDREN_UNKNOWN;
    }
	
	public double rectify_output(double res) {
		double tmp = res;
//		if(Double.isNaN(tmp)){
//			System.err.println("we found a NAN value");
//		}
//		if( Double.isInfinite(tmp)) {
//			System.err.println("we found a infinite value");
//		}
		if(res > upper_bound || res >= Double.POSITIVE_INFINITY) {
			tmp = upper_bound;
		}
		else if(res < lower_bound || res <= Double.NEGATIVE_INFINITY) {
			tmp = lower_bound;
		}
		
		return tmp;
	}
}
