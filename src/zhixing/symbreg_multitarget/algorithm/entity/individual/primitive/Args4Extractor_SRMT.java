package zhixing.symbreg_multitarget.algorithm.entity.individual.primitive;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.util.Parameter;
import zhixing.cpxInd.individual.primitive.Args;
import zhixing.cpxInd.individual.primitive.Entity;
import zhixing.cpxInd.util.LinearRegression;
import zhixing.optimization.SupervisedProblem;

public abstract class Args4Extractor_SRMT  extends Args{

	public final static String P_NAME = "Extractor_Argument";
	
	public final static String P_RANGE = "range";
	
	public final static String P_BATCH = "batch";
	
	public int BATCH_SIZE = 512;
	
	protected double range; //the maximum number of input features
	
	public final static double MAX_WEIGHT = 1e7;
	public final static double MIN_WEIGHT = -1e7;
	
	protected final static int n_trial = 1;
	
	protected LinearRegression lr = new LinearRegression();
	
	Parameter def = new Parameter(P_NAME).push(P_ARGUMENTS);
	
	@Override
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		Parameter pp = base.push(P_RANGE);
    	range = state.parameters.getDoubleWithDefault(pp,def.push(P_RANGE),0.5);
    	if(range <= 0 || range > 1) {
    		state.output.fatal("maximum number of input features must be (0,1]");
    		System.exit(1);
    	}
    	
    	pp = base.push(P_BATCH);
    	BATCH_SIZE = state.parameters.getIntWithDefault(pp, def.push(P_BATCH), 512);
    	if(BATCH_SIZE < 1) {
    		state.output.fatal("batch size must be >= 1");
    		System.exit(1);
    	}
	}
	
	@Override
    public void resetNode(final EvolutionState state, final int thread) {
		
//		double maxrange = 1.;
//		if( state.evaluator.p_problem instanceof SupervisedProblem ) {
//			maxrange = range / ((SupervisedProblem)state.evaluator.p_problem).getDatadim();
//		}
		
		double index1_f = 0., index2_f = 0.99999;
		double minfit = 1e7;
		for(int t = 0; t<n_trial; t++) {
			double tmpindex1 = state.random[thread].nextDouble();
			double tmpindex2 = state.random[thread].nextDouble();
			
//			if(tmpindex2 < tmpindex1) {
//				if(state.random[thread].nextDouble() < 0.5) {
//					tmpindex2 = 0.999;
//				}
//				else {
//					tmpindex1 = 0.0;
//				}
//			}
			
			if(tmpindex2 - tmpindex1 > range || tmpindex2 <= tmpindex1) {
				if(state.random[thread].nextDouble() < 0.5) {
					tmpindex2 = tmpindex1 + state.random[thread].nextDouble()*range; //(tmpindex2 - tmpindex1);
				}
				else {
					tmpindex1 = tmpindex2 - state.random[thread].nextDouble()*range; //(tmpindex2 - tmpindex1);
				}
				tmpindex2 = Math.min(0.99999, tmpindex2);
				tmpindex1 = Math.max(0.0, tmpindex1);
			}
			
			
			double fitness = setArgs(state, thread, tmpindex1, tmpindex2);
			
			if(fitness <= minfit || minfit >= 1e7) {
				index1_f = tmpindex1;
				index2_f = tmpindex2;
				minfit = fitness;
			}
		}
		
		double fitness = setArgs(state, thread, index1_f, index2_f);
		
		((Extractor_EntityNode) this.parent).setEntityFitness(fitness);
    }
	
	@Override
	public void varyNode(EvolutionState state, int thread, Entity ref) {
		
//		double maxrange = 1.;
//		if( state.evaluator.p_problem instanceof SupervisedProblem ) {
//			maxrange = range / ((SupervisedProblem)state.evaluator.p_problem).getDatadim();
//		}
		
		//modify the first two arguments: [index1, index2]
		double index1_f = values[0];
		double index2_f = values[1];
		
		if(ref instanceof Extractor_EntityNode) {
			index1_f = ref.getArguments().getValue(0);
			index2_f = ref.getArguments().getValue(1);
		}
		
		double minfit = setArgs(state, thread, index1_f, index2_f);
		
		for(int t = 0; t<n_trial; t++) {
			double tmpindex1 = moveByStep(state, thread, index1_f);
			double tmpindex2 = moveByStep(state, thread, index2_f);
			
//			if(tmpindex2 < tmpindex1) {
//				if(state.random[thread].nextDouble() < 0.5) {
//					tmpindex2 = 0.999;
//				}
//				else {
//					tmpindex1 = 0.0;
//				}
//			}
			
			if(tmpindex2 - tmpindex1 > range || tmpindex2 <= tmpindex1) {
				if(state.random[thread].nextDouble() < 0.5) {
					tmpindex2 = tmpindex1 + state.random[thread].nextDouble()*range; //(tmpindex2 - tmpindex1);
				}
				else {
					tmpindex1 = tmpindex2 - state.random[thread].nextDouble()*range; //(tmpindex2 - tmpindex1);
				}
				tmpindex2 = Math.min(0.99999, tmpindex2);
				tmpindex1 = Math.max(0.0, tmpindex1);
			}
			
			double fitness = setArgs(state, thread, tmpindex1, tmpindex2);
			
			if(fitness < minfit || minfit >= 1e7) {
				index1_f = tmpindex1;
				index2_f = tmpindex2;
				minfit = fitness;
			}
		}
		
		double fitness = setArgs(state, thread, index1_f, index2_f);
		
		((Extractor_EntityNode) this.parent).setEntityFitness(fitness);
	}
	
	protected abstract double setArgs(EvolutionState state, int thread, double index1_f, double index2_f) ;
	
	protected abstract void setPredict(double [][] predict, ArrayList<Double[]>data, int predi, int datai, int index1, int index2);
	
	protected double getLoss(double [] pred_out, double [] real) {
		double loss = 0;
		for(int i = 0; i<pred_out.length; i++) {
			loss += (pred_out[i] - real[i])*(pred_out[i] - real[i]);
		}
		loss /= pred_out.length;
		loss = Math.sqrt(loss);
		
		return loss;
	}
	
	protected void setRange(double range) {
		this.range = range;
	}
	
	@Override
    public GPNode lightClone() {
    	GPNode n = super.lightClone();
    	
    	((Args4Extractor_SRMT)n).setRange(range);

    	return n;
    }
}
