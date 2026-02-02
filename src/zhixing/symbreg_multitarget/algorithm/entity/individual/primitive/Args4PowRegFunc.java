package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.optimization.SupervisedProblem;

public class Args4PowRegFunc extends Args4Function_SRMT{

	public final static String P_NAME = "PowRF_Argument";
	
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
	protected double setArgs(EvolutionState state, int thread) {

		double fitness = 0;
		Function_EntityNode par = ((Function_EntityNode) this.parent);
		ArrayList<double[]> list = par.getValueList();
		ArrayList<Integer> indexlist = par.getIndexList();

		setNumWeights(2);
		setNumInputCoeff(1);
		
		int length =  numInputCoeff();  
		
		if(list.size() == 0) {
			
			setMaxLength(numWeights()); 
			
//			for(int j = 0; j<length+1; j++) {
//	        	setValue(j, 1.);
//	        }
//			setValue(length+1, 0.);
			
			for(int j = 0; j<length+1; j++) {
				setValue(j, INIT_MIN_WEIGHT+state.random[thread].nextDouble()*(INIT_MAX_WEIGHT - INIT_MIN_WEIGHT));
//				setValue(j, 1);
			}

			return 0;
		}
		
		//for the rest of elements, approximated by linear regression.
		int n_instance = list.size();
//		ArrayList<Double[]> data = ((SupervisedProblem)state.evaluator.p_problem).getData();
		int tarindex = state.random[thread].nextInt(((SupervisedProblem)state.evaluator.p_problem).getTargets().length);
		int target = ((SupervisedProblem)state.evaluator.p_problem).getTargets()[tarindex];
		ArrayList<Double[]> output = ((SupervisedProblem)state.evaluator.p_problem).getDataOutput();
		
		
		double [][] predict;
		double [] real;
		
		if(n_instance <= MAX_SAMPLE) {
			predict = new double [n_instance][length];
			real = new double[n_instance];
			
			
			
			for(int i = 0; i<list.size(); i++) {
				for(int j = 0; j<list.get(0).length; j++) {
					predict[i][j] = list.get(i)[j];
				}
				real[i] = output.get(indexlist.get(i))[target];
			}	        
	        
		}
		else { //randomly select MAX_SAMPLE samples 
			predict = new double [MAX_SAMPLE][length];
			real = new double[MAX_SAMPLE];
			
			boolean [] used = new boolean[n_instance];
			
			for(int ci = 0; ci<MAX_SAMPLE; ci++) {
				int i = state.random[thread].nextInt(n_instance);
				int cnt =100;
				while(used[i] && cnt >=0) {
					i = (i+1)%n_instance;
					cnt --;
				}
				used[i] = true;
				
				for(int j = 0; j<list.get(0).length; j++) {
					predict[ci][j] = list.get(i)[j];
				}
				real[ci] = output.get(indexlist.get(i))[target];
			}

		}
		
		//initialize the weights
		double [] W = new double [numWeights()];
		
		
		//initialize W
		for(int j = 0; j<W.length; j++) {
        	W[j] = getValue(j);
        }
		
			
		double [] newW = getWeightsSGD(predict, real, W, state, thread);

		for(int j = 0; j<W.length; j++) {
//        	setValue(j, 0.01*W[j] + 0.99*getValue(j));
//        	setValue(j, (1-step)*getValue(j) + step*W[j]);
        	setValue(j, newW[j]);
        }

		return getLoss(predict, real, newW);
	}
	
//	protected double [] getWeightsSGD(double [][] X, double [] Y, double [] W) {
//		
//		int n_instance = X.length;
//		int dimension = X[0].length;
//
//		//SGD iteration
//		double [] tmpW = new double [W.length];
//		for(int i = 0; i<W.length; i++) {
//			tmpW[i] = W[i];
//		}
//		
//		for(int t = 0; t<=20; t++) {
//			double step = 0.0001 / Math.pow(2, t/10);
//			
////			double Loss = 0;
////			for(int i = 0; i<n_instance; i++) {
////				Loss += Math.pow((tmpW[0] * Math.sin(tmpW[1]*X[i][0] + tmpW[2]) - Y[i]), 2);
////			}
//			
//			
//			//average the gradient and move tmpW
//			for(int d = 0; d<W.length; d++) {
//				gradient[d] /= n_instance;
//				tmpW[d] += (-step)*gradient[d];
//			}
//		}
//		
//		return tmpW;
//		
//	}

	@Override
	protected double[] calculateGradient(double[][] X, double[] Y, double[] W) {
		
		int n_instance = X.length;
		int dimension = X[0].length;
		
		double [] tmpW = W;
		double [] gradient = new double [W.length];
		
		//get the common parts
		for(int i = 0; i<n_instance; i++) {

			//partial W0
			double pw0 = 2*(Math.pow( Math.abs(X[i][0]), tmpW[1]) + tmpW[0]  + X[i][0] - Y[i]);
			double pw1 = pw0 * Math.pow(Math.abs(X[i][0]), tmpW[1]);
			if(X[i][0] == 0) {
				pw1 *= 1e-6;
			}
			else {
				pw1 *= Math.log(Math.abs(X[i][0]));
			}
//			double pw1 = pw0 * Math.pow(Math.abs(X[i][0]), tmpW[1]) * Math.log(Math.abs(X[i][0])+1e-10);
			
			gradient[0] += pw0;
			gradient[1] += pw1;
		} 

		for(int d = 0; d<gradient.length; d++) {
			gradient[d] /= n_instance;
			gradient[d] += normcoeff * ( tmpW[d] >= 0? 1 : -1 );
		}
		
		gradient = normalizeGradient(gradient);
		
		return gradient;
	}

	@Override
	protected double getLoss(double[][] X, double[] Y, double[] W) {
		double Loss = 0;
		for(int i = 0; i<X.length; i++) {
			
			Loss += Math.pow((W[0] + Math.pow(Math.abs(X[i][0]), W[1]) + X[i][0] - Y[i]), 2);
		}
		Loss = Math.sqrt(Loss / X.length) + getL1Norm(W);
		
		return Loss;
	}
}
