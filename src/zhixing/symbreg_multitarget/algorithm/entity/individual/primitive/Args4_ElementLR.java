package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.optimization.SupervisedProblem;

public class Args4_ElementLR extends Args4Extractor_SRMT{

	public final static String P_NAME = "ElementLR_Argument";
	public final static String P_FEATURENUM = "feature_num";
	
//	public final static String P_RANGE = "range";
//	
//	public final static int MAX_SAMPLE = 1000;
//	
	protected double range = 5; //the maximum number of input features
	
	protected int max_feature_num = 5;
//	
//	LinearRegression lr = new LinearRegression();
	
	Parameter def = new Parameter(P_NAME).push(P_ARGUMENTS);
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		Parameter pp = base.push(P_FEATURENUM);
    	max_feature_num = state.parameters.getIntWithDefault(pp, def.push(P_FEATURENUM), 5);
    	if(max_feature_num < 1) {
    		state.output.fatal("the maximum number of features in Args4_ElementLR must be >= 1");
    		System.exit(1);
    	}
	}
	
	@Override
	protected double setArgs(EvolutionState state, int thread, double index1_f, double index2_f) {
//		int index1 = (int) Math.floor(index1_f * ((SupervisedProblem)state.evaluator.p_problem).getDatadim());
//		int index2 = (int) Math.floor(index2_f * ((SupervisedProblem)state.evaluator.p_problem).getDatadim());
		double fitness = 0;
		final int datadim = ((SupervisedProblem)state.evaluator.p_problem).getDatadim();
		max_feature_num = Math.min(((SupervisedProblem)state.evaluator.p_problem).getDatadim(), max_feature_num);
		//re-set the length of arguments and store index1 and index2 into the first two elements.
//		range = Math.floor( (index2_f - index1_f) * ((SupervisedProblem)state.evaluator.p_problem).getDatadim() ) + 1;
		int feature_num = state.random[thread].nextInt(max_feature_num)+1;
		range = feature_num; //Math.min(range, max_feature_num);
		int length = (int) (range);  
		setMaxLength(2*length + 1); //first element for W0, then [index of feature, coefficient of feature]
//		setValue(0, index1_f);
//		setValue(1, index2_f);
		double indexes [] = new double [length];
		boolean used [] = new boolean [((SupervisedProblem)state.evaluator.p_problem).getDatadim()];
		for(int i = 0; i<length; i++) {
			double tmp = state.random[thread].nextDouble();
			int maxtrial = 10;
			while(used[(int) Math.floor( datadim * tmp )] && maxtrial > 0) {
				tmp = state.random[thread].nextDouble();
				maxtrial --;
			}
			indexes[i] = tmp;
			used[(int) Math.floor( datadim * tmp )] = true;
		}
		
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
				setPredict(predict, data, i, i, indexes);
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
					
					setPredict(predict, data, ci, i, indexes);
					real[ci] = output.get(i)[target];
				}
				
				fitness = lr.fit(predict, real);
				
				double [] tmpW = lr.getWeights();
				
				for(int j = 0; j<W.length; j++) {
		        	W[j] += coef * tmpW[j];
		        }
			}
			
			
		}
		
		setValue(0, W[0]);
        for(int j = 0; j<W.length-1; j++) {
//        	setValue(j+2, W[j]);
        	setValue(1+2*j, indexes[j]);
        	setValue(2+2*j, Math.min(MAX_WEIGHT, Math.max(MIN_WEIGHT, W[j+1])));
        }
        
        double [][] predict = new double [n_instance][length];
		double [] real = new double[n_instance];
		
		for(int i = 0; i<n_instance; i++) {
			setPredict(predict, data, i, i, indexes);
			real[i] = output.get(i)[target];
		}
		double [] pred_out = lr.predict(predict);
		
        return getLoss(pred_out, real);
	}
	
	@Override
	protected void setPredict(double[][] predict, ArrayList<Double[]> data, int predi, int datai, int index1, int index2) {
		System.err.print("the setPredict(index1, index2) is not defined for Args4_ElementLR\n");
		System.exit(1);
	}
	
	protected void setPredict(double[][] predict, ArrayList<Double[]> data, int predi, int datai, double indexes[]) {
		
		for(int j = 0, jj=0; j<indexes.length; j++, jj++) {
			int fea_ind = (int) Math.floor( data.get(0).length * indexes[j] );
			
			predict[predi][jj] = data.get(datai)[fea_ind];
		}
		
	}
	
	@Override
	public void varyNode(EvolutionState state, int thread, Entity ref) {
		
		resetNode(state, thread);
	}
}
