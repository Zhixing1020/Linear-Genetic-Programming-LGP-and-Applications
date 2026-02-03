package zhixing.symbolic_classification.individual;

import java.util.ArrayList;
import java.util.HashMap;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.util.Parameter;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPFlowController;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.util.LinearRegression;
import zhixing.symbolic_classification.optimization.GPClassification;
import zhixing.symbolic_classification.util.LinearClassifier;

public class LGPIndividual4Class extends LGPIndividual implements LGPInterface4Class{
	
	public static final String CLASSNUM_P = "class_num";
	
//	int class_num = 2;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		//set up the individual prototype 
		 super.setup(state,base); 
		 Parameter def = defaultBase();
		
//		 class_num = state.parameters.getInt(base.push(CLASSNUM_P), def.push(CLASSNUM_P), class_num);
	}
	
//	@Override
//	public int getClassNum() {
//		return class_num;
//	}
	
	@Override
	public ArrayList<Double[]> wrapper(ArrayList<Double[]> predict_list, ArrayList<Double[]> target_list, EvolutionState state, int thread, GPProblem problem) {
		
		final int MAX_SAMPLE = 128;
		
		double [][] predict = null;
		double [] target = null;
		
		int [] indices = null;
		
		GPClassification classprob = (GPClassification) problem;
		
		if(predict_list.size() > MAX_SAMPLE) {
			predict = new double [MAX_SAMPLE][predict_list.get(0).length+1]; //the predict of the current program
			target = new double [MAX_SAMPLE]; //since we linearly regress the output "column-by-column" (target-by-target)
			indices = new int [MAX_SAMPLE]; //the indices of selected samples 
			
			for(int s = 0; s<MAX_SAMPLE; s++) {
				int ind = state.random[thread].nextInt(predict_list.size());
				indices[s] = ind;
//				for(int j = 0; j<predict[0].length; j++) {
//					predict[s][j] = predict_list.get(ind)[j];
//				}
			}
		}
		else {
			predict = new double [predict_list.size()][predict_list.get(0).length+1]; //the predict of the current program
			target = new double [target_list.size()]; //since we linearly regress the output "column-by-column" (target-by-target)
			indices = new int [predict_list.size()];
			
			//initialize (transform) the predict
			for(int i = 0; i<predict.length; i++) {
				indices[i] = i;
//				for(int j = 0; j<predict[0].length; j++) {
//					predict[i][j] = predict_list.get(i)[j];
//				}
			}
		} 
		
		//prepare the target
    	for(int i = 0; i<predict.length; i++) {
    		target[i] = target_list.get(indices[i])[0];
    	}
		
		//predict: 0-axis: instances  1-axis: output registers
		//target: the target values of all the instances
		LinearClassifier lc = new LinearClassifier();
		
		double [][] wrapWeights = new double [classprob.getClassLabels().length][predict_list.get(0).length + 1];
		
		//for each time, we only wrap one output register. Only the first-targetNumber output registers are wrapped.
		wraplist.clear();
        for(int label = 0; label<classprob.getClassLabels().length; label++) {
        	
        	for(int i = 0; i<indices.length;i++) {
        		predict[i][0] = 1;
        		for(int j = 0; j<predict_list.get(0).length; j++) {
        			predict[i][j+1] = predict_list.get(indices[i])[j];
        		}
        	}
        	
//        	double loss_before = lc.getCrossEntropy(predict, target, classprob.getClassLabels());
        	
        	lc.fit(predict, target, classprob.getClassLabels(), state, thread);
        	
        	
//        	double [][]tmppredict = lc.predict(predict);
//        	double loss_after = lc.getCrossEntropy(tmppredict, target, classprob.getClassLabels());
        	
//        	if(loss_after > loss_before) {
//        		System.out.println(""+loss_before+" "+loss_after);
//        	}
//        	if(loss_after > loss_before + 5) {
//        		int aa = 1;
//        	}
            
            double [][] W = lc.getWeights();
            
//            weight_norm = 0;
//            if(normalize_wrap) {
//            	for(double w : W) {
//                	weight_norm += w*w;
//                }
//                weight_norm = Math.sqrt(weight_norm/W.length) * normalize_f;
//            }
            
            		
            GPTreeStruct instr = constructInstr(outputRegister[label], W[label]);
            wraplist.add(instr);
            
            for(int w = 0; w<W[label].length; w++) {
            	wrapWeights[label][w] = W[label][w];
            }
            
            //update predict
        	for(int i = 0; i<target_list.size(); i++) {
        		double tmp =  W[label][0];
        		for(int j = 0; j<predict_list.get(0).length; j++) {
        			tmp += W[label][j+1] * predict_list.get(i)[j];
        		}
        		
        		if(tmp > 1e6) {
        			tmp = 1e6;
        		}
        		if(tmp < -1e6) {
        			tmp = -1e6;
        		}
        		
        		predict_list.get(i)[label] = tmp;
        	}
        }
        
        //output the updated prediction. Because the predict array might not contain all instances, we make a double-check here
        ArrayList<Double[]> newpred = new ArrayList<>();
        if(predict_list.size() <= MAX_SAMPLE) {
        	for(int i = 0; i<predict_list.size();i++) {
        		Double [] tmp = new Double [classprob.getClassLabels().length];
            	
            	for(int label = 0; label < classprob.getClassLabels().length; label++) {
            		tmp [label] = predict_list.get(i)[label];
            	}
            	
            	newpred.add(tmp);
        	}
        }
        else {
        	 for(int i = 0; i<predict_list.size();i++) {
        		Double [] tmp = new Double [classprob.getClassLabels().length];
             	if(inList(i, indices)) {//already updated

                 	for(int label = 0; label < classprob.getClassLabels().length; label++) {
                 		tmp [label] = predict_list.get(i)[label];
                 	}

             	}
             	else {
             		
             		for(int label = 0; label < classprob.getClassLabels().length; label++) {
             			double np =  wrapWeights[label][0];
                		for(int j = 0; j<predict_list.get(0).length; j++) {
                			np += wrapWeights[label][j+1] * predict_list.get(i)[j];
                		}
                		
                		if(np > 1e6) {
                			np = 1e6;
                		}
                		if(np < -1e6) {
                			np = -1e6;
                		}
                		
                 		tmp [label] = np;
                 	}
             		
             		
             	}
             	
             	newpred.add(tmp);
             }
        }
       
  		
  		return newpred;
	}
	
	private boolean inList(int a, int [] list) {
		boolean res = false;
		for(int v : list) {
			if(a == v) return true;
		}
		return res;
	}
	
	protected void copyLGPproperties(LGPIndividual4Class obj) {
    	super.copyLGPproperties(obj);
//    	this.class_num = obj.getClassNum();
    }
}
