package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.util.Parameter;
import zhixing.cpxInd.individual.primitive.Args;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.optimization.SupervisedProblem;

public class Args4Rad_SRMT  extends Args4Extractor_SRMT{
	
	//this Argument class is for Gaussian Radius function f(X)=W0 + W1*exp(-W2^2 * \sum_d(xd - W[3+d])^2)
	
	public final static String P_NAME = "Radius_Argument";
	
//	public final static String P_RANGE = "range";
//	
//	public final static int MAX_SAMPLE = 1000;
//	
//	protected double range = 200; //the maximum number of input features
	
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
//		setArgsBasedSGD(state, thread, index1_f, index2_f);
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
//		setArgsBasedSGD(state, thread, index1_f, index2_f);
//	}
	
	@Override
	protected double setArgs(EvolutionState state, int thread, double index1_f, double index2_f) {
		int index1 = (int) Math.floor(index1_f * ((SupervisedProblem)state.evaluator.p_problem).getDatadim());
		int index2 = (int) Math.floor(index2_f * ((SupervisedProblem)state.evaluator.p_problem).getDatadim());
		
		//re-set the length of arguments and store index1 and index2 into the first two elements.
		int length = index2 - index1 + 1;  
		setMaxLength(length + 2 + 3); //first two elements for indexes, the second three elements for W0, W1, and W1
		setValue(0, index1_f);
		setValue(1, index2_f);
		
		//for the rest of elements, approximated by stochastic gradient descent (SGD).
		int n_instance = ((SupervisedProblem)state.evaluator.p_problem).getDatanum();
		ArrayList<Double[]> data = ((SupervisedProblem)state.evaluator.p_problem).getData();
		int tarindex = state.random[thread].nextInt(((SupervisedProblem)state.evaluator.p_problem).getTargets().length);
		int target = ((SupervisedProblem)state.evaluator.p_problem).getTargets()[tarindex];
		ArrayList<Double[]> output = ((SupervisedProblem)state.evaluator.p_problem).getDataOutput();
		
		
		double [][] predict = null;
		double [] real = null;
		
		if(n_instance <= BATCH_SIZE) {
			predict = new double [n_instance][length];
			real = new double[n_instance];
			
			for(int i = 0; i<n_instance; i++) {
				for(int j = index1, jj=0; j<=index2; j++, jj++) {
					predict[i][jj] = data.get(i)[j];
				}
				real[i] = output.get(i)[target];
			}
	        
		}
		else { //randomly select MAX_SAMPLE samples 
			predict = new double [BATCH_SIZE][length];
			real = new double[BATCH_SIZE];
			
			boolean [] used = new boolean[n_instance];
			
			for(int ci = 0; ci<BATCH_SIZE; ci++) {
				int i = state.random[thread].nextInt(n_instance);
				int cnt =100;
				while(used[i] && cnt >=0) {
					i = (i+1)%n_instance;
					cnt --;
				}
				used[i] = true;
				
				for(int j = index1, jj=0; j<=index2; j++, jj++) {
					predict[ci][jj] = data.get(i)[j];
				}
				real[ci] = output.get(i)[target];
			}
			
			
		}
		
		//initialize the weights
		double [] W = new double [predict[0].length + 3];
		
		
		//initialize W
		W[0] = real[state.random[thread].nextInt(real.length)];
		W[1] = state.random[thread].nextDouble();
		W[2] = state.random[thread].nextDouble();
		
		for(int d = 3; d< predict[0].length + 3; d++) {
			W[d] = predict[state.random[thread].nextInt(predict.length)][state.random[thread].nextInt(predict[0].length)];
		}
		
		
		W = getWeightsSGD(predict, real, W);
		
        for(int j = 0; j<W.length; j++) {
        	setValue(j+2, W[j]);
        }
        
        double [] pred_out = new double [n_instance];
        real = new double[n_instance];
        
		double Loss = 0;
		for(int i = 0; i<data.size(); i++) {
			double sqrttmp = 0;
			for(int d = 0, index = index1; d<length; d++, index++) {
				sqrttmp += (data.get(i)[index] - W[d+3])*(data.get(i)[index] - W[d+3]);
			}
			sqrttmp = Math.exp(-W[2]*W[2]*sqrttmp);
			pred_out[i] = W[0]+W[1]*sqrttmp;
			real[i] = output.get(i)[target];
		}
		
		return getLoss(pred_out, real);
	}
	
//	protected double [] getWeightsSGD(double [][] X, double [] Y, EvolutionState state, int thread) {
//	
//		int n_instance = X.length;
//		int dimension = X[0].length;
//		
//		double [] tmpW = new double [dimension + 2];
//		
//		
//		//initialize tmpW
//		tmpW[0] = Y[state.random[thread].nextInt(n_instance)];
//		tmpW[1] = Y[state.random[thread].nextInt(n_instance)];
//		
//		for(int d = 2; d< dimension + 2; d++) {
//			tmpW[d] = X[state.random[thread].nextInt(n_instance)][state.random[thread].nextInt(dimension)];
//		}
//		
//		//SGD iteration
//		
//		for(int t = 0; t<=30; t++) {
//			double step = 0.0001 / Math.pow(2, t/10);
////			double Loss = 0;
////			for(int i = 0; i<n_instance; i++) {
////				double sqrttmp = 0;
////				for(int d = 0; d<dimension; d++) {
////					sqrttmp += (X[i][d] - tmpW[d+2])*(X[i][d] - tmpW[d+2]);
////				}
////				sqrttmp = Math.sqrt(sqrttmp);
////				
////				Loss += Math.pow((tmpW[0]+tmpW[1]*sqrttmp - Y[i]), 2);
////			}
//			
//			double [] gradient = new double [dimension + 2];
//			
//			//get the common parts
//			for(int i = 0; i<n_instance; i++) {
//				//sq root item
//				double sqrtitem = 0;
//				for(int d = 0; d<dimension; d++) {
//					sqrtitem += (X[i][d] - tmpW[d+2])*(X[i][d] - tmpW[d+2]);
//				}
//				sqrtitem = Math.sqrt(sqrtitem);
//
//				//partial W0
//				double pw0 = 2*(tmpW[0] + tmpW[1] * sqrtitem - Y[i]);
//				
//				gradient[0] += pw0;
//				
//				gradient[1] += pw0*sqrtitem;
//					
//				for(int d = 2; d<dimension + 2; d++) {
//					if(sqrtitem == 0) {
//						sqrtitem += 1e-7;
//					}
//					gradient[d] += pw0* ( (-2*tmpW[1]*(X[i][d-2] - tmpW[d])) /(2*sqrtitem));
//				}
//				
//			} 
//
//			//average the gradient and move tmpW
//			for(int d = 0; d<dimension + 2; d++) {
//				gradient[d] /= n_instance;
//				tmpW[d] += (-step)*gradient[d];
//			}
//		}
//		
//		return tmpW;
//		
//	}
	
	protected double [] getWeightsSGD(double [][] X, double [] Y, double [] W) {
		
		int n_instance = X.length;
		int dimension = X[0].length;

		//SGD iteration
		double [] tmpW = new double [W.length];
		for(int i = 0; i<W.length; i++) {
			tmpW[i] = W[i];
		}
		
		for(int t = 0; t<=50; t++) {
			double step = 0.0001 / Math.pow(2, t/10);
			
//			double Loss = 0;
//			for(int i = 0; i<n_instance; i++) {
//				double sqrttmp = 0;
//				for(int d = 0; d<dimension; d++) {
//					sqrttmp += (X[i][d] - tmpW[d+2])*(X[i][d] - tmpW[d+2]);
//				}
//				sqrttmp = Math.sqrt(sqrttmp);
//				
//				Loss += Math.pow((tmpW[0]+tmpW[1]*sqrttmp - Y[i]), 2);
//			}
			
			double [] gradient = new double [W.length];
			
			//get the common parts
			for(int i = 0; i<n_instance; i++) {
				//exp item
				double expitem = 0, squaresum = 0;
				for(int d = 0; d<dimension; d++) {
					squaresum += (X[i][d] - tmpW[d+3])*(X[i][d] - tmpW[d+3]);
				}
				expitem = Math.exp(-tmpW[2]*tmpW[2]*squaresum);

				//partial W0
				double pw0 = 2*(tmpW[0] + tmpW[1] * expitem - Y[i]);
				
				gradient[0] += pw0;
				
				gradient[1] += pw0*expitem;
				
				gradient[2] += pw0*expitem*(-2*tmpW[1])*squaresum;
					
				for(int d = 3; d<dimension + 3; d++) {
					if(expitem == 0) {
						expitem += 1e-7;
					}
					gradient[d] += pw0*tmpW[1]*expitem*2*tmpW[2]*tmpW[2]*(X[i][d-3] - tmpW[d]);
				}
				
			} 

			//average the gradient and move tmpW
			for(int d = 0; d<dimension + 3; d++) {
				gradient[d] /= n_instance;
				tmpW[d] += (-step)*gradient[d];
			}
		}
		
		return tmpW;
		
	}

	@Override
	protected void setPredict(double[][] predict, ArrayList<Double[]> data, int predi, int datai, int index1, int index2) {
		// TODO Auto-generated method stub
		
	}
	
//	protected void setRange(double range) {
//		this.range = range;
//	}
//	
//	@Override
//    public GPNode lightClone() {
//    	GPNode n = super.lightClone();
//    	
//    	((Args4Rad_SRMT)n).setRange(range);
//
//    	return n;
//    }
}
