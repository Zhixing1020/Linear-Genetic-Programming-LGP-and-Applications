package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.optimization.SupervisedProblem;

public class Args4CondLR extends Args4Extractor_SRMT{

	public final static String P_NAME = "LR_Argument";
	
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
	protected double setArgs(EvolutionState state, int thread, double index1_f, double index2_f) {
		int index1 = (int) Math.floor(index1_f * ((SupervisedProblem)state.evaluator.p_problem).getDatadim());
		int index2 = (int) Math.floor(index2_f * ((SupervisedProblem)state.evaluator.p_problem).getDatadim());
		
		double index3_f = state.random[thread].nextDouble();
		int index3 = (int) Math.floor(index1 + index3_f * (index2 - index1)); //conditioning variable
		
		double index4_f = state.random[thread].nextDouble()*0.75;
		double index5_f = Math.min(1., index4_f + 0.25 + 0.25 * state.random[thread].nextDouble());
		
		double datamax = ((SupervisedProblem)state.evaluator.p_problem).getDataMax()[index3];
		double datamin = ((SupervisedProblem)state.evaluator.p_problem).getDataMin()[index3];
		
		
		double min_val = datamin + index4_f * (datamax - datamin);
		double max_val = datamin + index5_f * (datamax - datamin);
		
		double fitness = 0;
		
		//re-set the length of arguments and store index1 and index2 into the first two elements.
		int length = index2 - index1 + 1;  
		setMaxLength(length + 2 + 3 + 1); //first two elements for indexes, the third to fifth elements for condition, the sixth element for W0
		setValue(0, index1_f);
		setValue(1, index2_f);
		setValue(2, index3_f);
		setValue(3, min_val);
		setValue(4, max_val);
		
		//for the rest of elements, approximated by linear regression.
		int n_instance = ((SupervisedProblem)state.evaluator.p_problem).getDatanum();
		ArrayList<Double[]> data = ((SupervisedProblem)state.evaluator.p_problem).getData();
		int tarindex = state.random[thread].nextInt(((SupervisedProblem)state.evaluator.p_problem).getTargets().length);
		int target = ((SupervisedProblem)state.evaluator.p_problem).getTargets()[tarindex];
		ArrayList<Double[]> output = ((SupervisedProblem)state.evaluator.p_problem).getDataOutput();
		
		
//		double [][] predict;
//		double [] real;
		ArrayList<Double []> predict_list = new ArrayList<>();
		ArrayList<Double> real_list = new ArrayList<>();
		
		if(n_instance <= BATCH_SIZE) {
//			predict = new double [n_instance][length];
//			real = new double[n_instance];
			
			for(int i = 0; i<n_instance; i++) {
				if(data.get(i)[index3]>=min_val && data.get(i)[index3]<=max_val) {
					Double [] tmp = new Double [length]; 
					for(int j = index1, jj=0; j<=index2; j++, jj++) {
//						predict[i][jj] = data.get(i)[j];
						tmp[jj] = data.get(i)[j];
					}
					predict_list.add(tmp);
//					real[i] = output.get(i)[target];
					real_list.add( output.get(i)[target] );
				}
				
			}
			
			
	        
	        
		}
		else { //randomly select MAX_SAMPLE samples 
//			predict = new double [MAX_SAMPLE][length];
//			real = new double[MAX_SAMPLE];
			
			boolean [] used = new boolean[n_instance];
			
			for(int ci = 0; ci<BATCH_SIZE; ci++) {
				int i = state.random[thread].nextInt(n_instance);
				int cnt =100;
				while(used[i] && cnt >=0) {
					i = (i+1)%n_instance;
					cnt --;
				}
				used[i] = true;
				
//				if(data.get(i)[index3]>=min_val && data.get(i)[index3]<=max_val) {
//					for(int j = index1, jj=0; j<=index2; j++, jj++) {
//						predict[ci][jj] = data.get(i)[j];
//					}
//					real[ci] = output.get(i)[target];
//				}
				if(data.get(i)[index3]>=min_val && data.get(i)[index3]<=max_val) {
					Double [] tmp = new Double [length]; 
					for(int j = index1, jj=0; j<=index2; j++, jj++) {
//						predict[i][jj] = data.get(i)[j];
						tmp[jj] = data.get(i)[j];
					}
					predict_list.add(tmp);
//					real[i] = output.get(i)[target];
					real_list.add( output.get(i)[target] );
				}
				
			}
		}
		
		double [][] predict = new double [predict_list.size()][length];
		double [] real = new double[real_list.size()];
		
		for(int i = 0; i<predict_list.size(); i++) {
			for(int d = 0; d<length; d++) {
				predict[i][d] = predict_list.get(i)[d];
			}
			real[i] = real_list.get(i);
		}
		
		fitness = 1e6;
		double [] W = new double [length + 1];
		
		if(predict.length > 0) {
			fitness = lr.fit(predict, real);
			W = lr.getWeights();
		}
		
        for(int j = 0; j<W.length; j++) {
        	setValue(j+5, W[j]);
        }
        
        return fitness;
	}

	@Override
	protected void setPredict(double[][] predict, ArrayList<Double[]> data, int predi, int datai, int index1,
			int index2) {
		// TODO Auto-generated method stub
		
	}
}
