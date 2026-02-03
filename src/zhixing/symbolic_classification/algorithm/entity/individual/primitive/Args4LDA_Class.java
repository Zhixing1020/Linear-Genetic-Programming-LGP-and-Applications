package zhixing.symbolic_classification.algorithm.entity.individual.primitive;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.symbolic_classification.optimization.GPClassification;

public class Args4LDA_Class extends Args4Extractor_Class{

	public final static String P_NAME = "LDA_Argument";
	
	Parameter def = new Parameter(P_NAME).push(P_ARGUMENTS);
	
	@Override
	protected double setArgs(EvolutionState state, int thread, double index1_f, double index2_f) {
		int index1 = (int) Math.floor(index1_f * ((GPClassification)state.evaluator.p_problem).getDatadim());
		int index2 = (int) Math.floor(index2_f * ((GPClassification)state.evaluator.p_problem).getDatadim());
		double fitness = 0;
		
//		int dim = Math.min(((GPClassification)state.evaluator.p_problem).getClassNum(), index2 - index1 + 1) - 1;
//		if(dim > 0) {
//			dim = state.random[thread].nextInt( dim ); //the to-be-selected dimension from LDA
//		}
				
		//re-set the length of arguments and store index1 and index2 into the first two elements.
		int length = index2 - index1 + 1;  
		setMaxLength(length + 2); //first two elements for indexes
		setValue(0, index1_f);
		setValue(1, index2_f);
		
		//for the rest of elements, approximated by linear regression.
		int n_instance = ((GPClassification)state.evaluator.p_problem).getDatanum();
		ArrayList<Double[]> data = ((GPClassification)state.evaluator.p_problem).getData();
		int tarindex = state.random[thread].nextInt(((GPClassification)state.evaluator.p_problem).getTargets().length);
		int target = ((GPClassification)state.evaluator.p_problem).getTargets()[tarindex];
		ArrayList<Double[]> output = ((GPClassification)state.evaluator.p_problem).getDataOutput();
		
		double [] W = new double [length];
		
		if(n_instance <= BATCH_SIZE) {
			double [][] predict = new double [n_instance][length];
			double [] real = new double[n_instance];
			
			for(int i = 0; i<n_instance; i++) {
				setPredict(predict, data, i, i, index1, index2);
				real[i] = output.get(i)[target];
			}
			
			fitness = lda.fit(predict, real);
	        
			W = lda.getWeights();
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
				
				fitness = lda.fit(predict, real);
				
				double [] tmpW = lda.getWeights();
				
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
		double [] pred_out = lda.transform(predict);
		
        return getLoss(pred_out, real);
	}

	@Override
	protected void setPredict(double[][] predict, ArrayList<Double[]> data, int predi, int datai, int index1, int index2) {
		
		for(int j = index1, jj=0; j<=index2; j++, jj++) {
			predict[predi][jj] = data.get(datai)[j];
		}
	}
}
