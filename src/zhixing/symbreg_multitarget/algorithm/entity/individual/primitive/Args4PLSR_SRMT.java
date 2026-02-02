package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.cpxInd.util.PLSRegression;
import zhixing.optimization.SupervisedProblem;

public class Args4PLSR_SRMT  extends Args4Extractor_SRMT{

	public final static String P_NAME = "PLSR_Argument";
	public final static String P_NCOMPONENTS = "n_components";
	
	protected PLSRegression plsr = new PLSRegression();
	
	Parameter def = new Parameter(P_NAME).push(P_ARGUMENTS);
	
	int n_components = 20;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		Parameter pp = base.push(P_RANGE);
//    	range = state.parameters.getDoubleWithDefault(pp,def.push(P_RANGE),200);
//    	if(range < 1) {
//    		state.output.fatal("maximum number of input features must be >=1");
//    		System.exit(1);
//    	}
    	
    	pp = base.push(P_NCOMPONENTS);
    	n_components = state.parameters.getIntWithDefault(pp,def.push(P_NCOMPONENTS),20);
    	if(n_components < 1) {
    		state.output.fatal("number of components in PLSR must be >=1");
    		System.exit(1);
    	}
	}
	
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
		
		
		double [][] predict;
		double [] real;
		
		if(n_instance <= BATCH_SIZE) {
			predict = new double [n_instance][length];
			real = new double[n_instance];
			
			for(int i = 0; i<n_instance; i++) {
				for(int j = index1, jj=0; j<=index2; j++, jj++) {
					predict[i][jj] = data.get(i)[j];
				}
				real[i] = output.get(i)[target];
			}
			
			fitness = lr.fit(predict, real);
	        
	        
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
		
		fitness = plsr.fit(predict, real, n_components);
		
		double [] W = plsr.getWeights();
		
        for(int j = 0; j<W.length; j++) {
        	setValue(j+2, W[j]);
        }
        
        return fitness;
	}

	@Override
	protected void setPredict(double[][] predict, ArrayList<Double[]> data, int predi, int datai, int index1,
			int index2) {
		// TODO Auto-generated method stub
		
	}
}
