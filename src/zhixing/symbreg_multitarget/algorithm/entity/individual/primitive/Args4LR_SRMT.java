package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.util.Parameter;
import zhixing.cpxInd.individual.primitive.Args;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.cpxInd.util.LinearRegression;
import zhixing.optimization.SupervisedProblem;

public class Args4LR_SRMT extends Args4Extractor_SRMT{
	
	public final static String P_NAME = "LR_Argument";
	
//	public final static String P_RANGE = "range";
//	
//	public final static int MAX_SAMPLE = 1000;
//	
//	protected double range = 200; //the maximum number of input features
//	
//	LinearRegression lr = new LinearRegression();
	
	Parameter def = new Parameter(P_NAME).push(P_ARGUMENTS);
	
//	@Override
//	public void setup(final EvolutionState state, final Parameter base){
//		super.setup(state, base);
//		Parameter pp = base.push(P_RANGE);
//    	range = state.parameters.getDoubleWithDefault(pp,def.push(P_RANGE),200);
//    	if(range < 1) {
//    		state.output.fatal("maximum number of input features must be >=1");
//    		System.exit(1);
//    	}
    	
//	}
	
//	@Override
//    public void resetNode(final EvolutionState state, final int thread) {
//		
//		double maxrange = 1.;
//		if( state.evaluator.p_problem instanceof SupervisedProblem ) {
//			maxrange = range / ((SupervisedProblem)state.evaluator.p_problem).getDatadim();
//		}
//		
//		double index1_f = state.random[thread].nextDouble();
//		double index2_f = state.random[thread].nextDouble();
//		
//		if(index2_f < index1_f) {
//			if(state.random[thread].nextDouble() < 0.5) {
//				index2_f = 0.999;
//			}
//			else {
//				index1_f = 0.0;
//			}
//		}
//		
//		while(index2_f - index1_f > maxrange && index2_f > index1_f) {
//			if(state.random[thread].nextDouble() < 0.5) {
//				index2_f = index1_f + state.random[thread].nextDouble()*(index2_f - index1_f);
//			}
//			else {
//				index1_f = index2_f - state.random[thread].nextDouble()*(index2_f - index1_f);
//			}
//		}
//		
//		
//		setArgsBasedLR(state, thread, index1_f, index2_f);
//    }
//	
//	@Override
//	public void varyNode(EvolutionState state, int thread, Entity ref) {
//		
//		double maxrange = 1.;
//		if( state.evaluator.p_problem instanceof SupervisedProblem ) {
//			maxrange = range / ((SupervisedProblem)state.evaluator.p_problem).getDatadim();
//		}
//		
//		//modify the first two arguments: [index1, index2]
//		double index1_f = moveByStep(state, thread, values[0]);
//		double index2_f = moveByStep(state, thread, values[1]);
//		
//		if(index2_f < index1_f) {
//			if(state.random[thread].nextDouble() < 0.5) {
//				index2_f = 0.999;
//			}
//			else {
//				index1_f = 0.0;
//			}
//		}
//		
//		while(index2_f - index1_f > maxrange && index2_f > index1_f) {
//			if(state.random[thread].nextDouble() < 0.5) {
//				index2_f = index1_f + state.random[thread].nextDouble()*(index2_f - index1_f);
//			}
//			else {
//				index1_f = index2_f - state.random[thread].nextDouble()*(index2_f - index1_f);
//			}
//		}
//		
//		setArgsBasedLR(state, thread, index1_f, index2_f);
//	}
	
	@Override
	protected double setArgs(EvolutionState state, int thread, double index1_f, double index2_f) {
		int index1 = (int) Math.floor(index1_f * ((SupervisedProblem)state.evaluator.p_problem).getDatadim());
		int index2 = (int) Math.floor(index2_f * ((SupervisedProblem)state.evaluator.p_problem).getDatadim());
		double fitness = 0;
		
		//re-set the length of arguments and store index1 and index2 into the first two elements.
		int length = index2 - index1 + 1;  
		setMaxLength(length + 2 + 1); //first two elements for indexes, the third element for W0
		setValue(0, index1_f);
		setValue(1, index2_f);
		
		//for the rest of elements, approximated by linear regression.
		int n_instance = ((SupervisedProblem)state.evaluator.p_problem).getDatanum();
		ArrayList<Double[]> data = ((SupervisedProblem)state.evaluator.p_problem).getData();
		int tarindex = state.random[thread].nextInt(((SupervisedProblem)state.evaluator.p_problem).getTargets().length);
		int target = ((SupervisedProblem)state.evaluator.p_problem).getTargets()[tarindex];
		ArrayList<Double[]> output = ((SupervisedProblem)state.evaluator.p_problem).getDataOutput();
		
		double [] W = new double [length+1];
		
		if(n_instance <= BATCH_SIZE) {
			double [][] predict = new double [n_instance][length];
			double [] real = new double[n_instance];
			
			for(int i = 0; i<n_instance; i++) {
				setPredict(predict, data, i, i, index1, index2);
				real[i] = output.get(i)[target];
			}
			
			fitness = lr.fit(predict, real);
	        
			W = lr.getWeights();
		}
		else { //randomly select MAX_SAMPLE samples 
			double [][] predict = new double [BATCH_SIZE][length];
			double [] real = new double[BATCH_SIZE];
			
//			boolean [] used = new boolean[n_instance];
			
			double loop_num = Math.ceil(n_instance / (double)BATCH_SIZE);
			double coef = 1./ loop_num;
			for(int l = 0; l<loop_num; l++) {
				for(int ci = 0; ci<BATCH_SIZE; ci++) {
					int i = state.random[thread].nextInt(n_instance);
//					int cnt =100;
//					while(used[i] && cnt >=0) {
//						i = (i+1)%n_instance;
//						cnt --;
//					}
//					used[i] = true;
					
					setPredict(predict, data, ci, i, index1, index2);
					real[ci] = output.get(i)[target];
				}
				
				fitness = lr.fit(predict, real);
				
				double [] tmpW = lr.getWeights();
				
				for(int j = 0; j<W.length; j++) {
		        	W[j] += coef * tmpW[j];
		        }
			}
			
			
		}
		
        for(int j = 0; j<W.length; j++) {
//        	setValue(j+2, W[j]);
        	setValue(j+2, Math.min(MAX_WEIGHT, Math.max(MIN_WEIGHT, W[j])));
        }
        
        double [][] predict = new double [n_instance][length];
		double [] real = new double[n_instance];
		
		for(int i = 0; i<n_instance; i++) {
			setPredict(predict, data, i, i, index1, index2);
			real[i] = output.get(i)[target];
		}
		double [] pred_out = lr.predict(predict);
		
        return getLoss(pred_out, real);
	}

	@Override
	protected void setPredict(double[][] predict, ArrayList<Double[]> data, int predi, int datai, int index1, int index2) {
		
		for(int j = index1, jj=0; j<=index2; j++, jj++) {
			predict[predi][jj] = data.get(datai)[j];
		}
	}
	
//	protected void setRange(double range) {
//		this.range = range;
//	}
//	
//	@Override
//    public GPNode lightClone() {
//    	GPNode n = super.lightClone();
//    	
//    	((Args4LR_SRMT)n).setRange(range);
//
//    	return n;
//    }
}
