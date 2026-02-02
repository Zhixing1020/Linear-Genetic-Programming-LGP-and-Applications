package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.cpxInd.individual.primitive.Args;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.cpxInd.util.LinearRegression;
import zhixing.optimization.SupervisedProblem;

public abstract class Args4Function_SRMT extends Args{

	public final static String P_NAME = "Function_Argument";
	
//	public final static String P_RANGE = "range";
	
	public final static int MAX_SAMPLE = 512;
	
//	protected double range = 200; //the maximum number of input features
	
	protected final static int n_trial = 1;
	
	protected double step_size = 0.01;
	protected int search_direction = 0;  //0: the search direction has to be decided by the functino itself every time it runs. 1: search along negative gradient, -1: search along gradient 
	
	public final static int STACKING_PERIOD = 10;
	
	public final static double normcoeff = 1e-4;
	
	public final static double INIT_MAX_WEIGHT = 1;
	public final static double INIT_MIN_WEIGHT = -1;
	public final static double MAX_WEIGHT = 5;
	public final static double MIN_WEIGHT = -5;
	
	protected int numweights = 1;
	protected int numinputcoeff = 1;
	
	protected LinearRegression lr = new LinearRegression();
	
	Parameter def = new Parameter(P_NAME).push(P_ARGUMENTS);
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
//		Parameter pp = base.push(P_RANGE);
//    	range = state.parameters.getDoubleWithDefault(pp,def.push(P_RANGE),200);
//    	if(range < 1) {
//    		state.output.fatal("maximum number of input features must be >=1");
//    		System.exit(1);
//    	}
    	
	}
	
	@Override
    public void resetNode(final EvolutionState state, final int thread) {
		
		double fitness = setArgs(state, thread);
		
		((Function_EntityNode) this.parent).setEntityFitness(fitness);
    }
	
	@Override
	public void varyNode(EvolutionState state, int thread, Entity ref) {
		
		double fitness = setArgs(state, thread);
		
		((Function_EntityNode) this.parent).setEntityFitness(fitness);
	}
	
	protected abstract double setArgs(EvolutionState state, int thread);
	
	protected double [] getWeightsSGD(double [][] X, double [] Y, double [] W, EvolutionState state, int thread) {
		
		int n_instance = X.length;
		int dimension = X[0].length;

		//SGD iteration
		double [] tmpW = new double [W.length];
		double [] tmpW2 = new double [W.length];
		for(int i = 0; i<W.length; i++) {
			tmpW[i] = W[i];
		}
		
		double step = step_size;
		double loss = getLoss(X, Y, tmpW);
		int early_stop_cnt = STACKING_PERIOD;
		double [] gradient = null;
		boolean weight_updated = true;
		
		int inverse_search = search_direction;
		if(search_direction == 0)
			inverse_search = state.random[thread].nextDouble()>0.5 ? 1 : -1;//-1 means we inverse the search direction
//		double early_stop_cnt_inv = STACKING_PERIOD * 2;
		
		for(int t = 0; t<=1; t++) {
			
			for(int i = 0; i<W.length; i++) {
				tmpW2[i] = tmpW[i];
			}
			
//			double Loss = 0;
//			for(int i = 0; i<n_instance; i++) {
//				Loss += Math.pow((tmpW[0] * Math.sin(tmpW[1]*X[i][0] + tmpW[2]) - Y[i]), 2);
//			}
			
			if(weight_updated)
				gradient = calculateGradient(X, Y, tmpW);

			//average the gradient and move tmpW
			for(int d = 0; d<W.length; d++) {
				tmpW2[d] += inverse_search * (-step)*gradient[d];
				tmpW2[d] = Math.min(MAX_WEIGHT, Math.max(MIN_WEIGHT, tmpW2[d]));
			}
			
//			double loss2 = getLoss(X, Y, tmpW2);
			
//			if(!Double.isNaN(loss2) && loss2 < loss-1e-5) {
//				for(int i = 0; i<W.length; i++) {
//					tmpW[i] = tmpW2[i];
//				}
//				weight_updated = true;
//				loss = loss2;
//				step *= 2;
//				early_stop_cnt = STACKING_PERIOD;
//			}
//			else if(inverse_search == -1) {
//				for(int i = 0; i<W.length; i++) {
//					tmpW[i] = tmpW2[i];
//				}
//				weight_updated = true;
//				loss = loss2;
//				early_stop_cnt_inv --;
//			}
//			else {
//				weight_updated = false;
//				step /= 2;
//				early_stop_cnt --;
//			}
			
			for(int i = 0; i<W.length; i++) {
				tmpW[i] = tmpW2[i];
			}
				
//			if(early_stop_cnt <= 0 || early_stop_cnt_inv <= 0) {
//				break;
//			}
		}
		
		return tmpW;
		
	}

	protected void setNumWeights(int a) {
		numweights = a;
	}
	
	protected void setNumInputCoeff(int a) {
		numinputcoeff = a;
	}
	
	protected int numWeights() {
		return numweights;
	}
	
	protected int numInputCoeff() {
		return numinputcoeff;
	}
	
	abstract protected double [] calculateGradient(double [][] X, double [] Y, double [] W);
	
	abstract protected double getLoss(double [][] X, double [] Y, double [] W);
	
//	protected double getL2Norm(double [] W) {
//		double res = 0;
//		
//		for(double w : W) {
//			res += w*w;
//		}
//		
//		res *= normcoeff / 2;
//		
//		return res;
//	}
	
	protected double getL1Norm(double [] W) {
		double res = 0;
		
		for(double w : W) {
			res += Math.abs(w);
		}
		
		res *= normcoeff;
		
		return res;
	}
	
	protected double [] normalizeGradient(double [] grad) {
		double sum = 0;
		for(int i = 0; i<grad.length; i++) {
			sum += grad[i]*grad[i];
		}
		sum = Math.sqrt(sum) + 1e-10;
		for(int i = 0; i<grad.length; i++) {
			grad[i] = grad[i] / sum;
		}
		return grad;
	}
	
	public void setStep(double step) {
		step_size = step;
	}
	public void setSearchDirection(int dir) {
		search_direction = dir;
	}
}
