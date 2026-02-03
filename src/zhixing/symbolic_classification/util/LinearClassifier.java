package zhixing.symbolic_classification.util;

import java.util.Arrays;

import ec.EvolutionState;
//import zhixing.cpxInd.util.LinearRegression;
import zhixing.cpxInd.util.MatrixOperation;

public class LinearClassifier {

	private double[][] weights;

    public double fit(double[][] X, double[] Y, double[] labels, EvolutionState state, int thread) {
    	
    	//labels: the list of possible labels
    	
        int n = X.length;
        int m = X[0].length;
        int C = labels.length;

        //randomly initialize weights
        weights = new double [C][m];
        double [][] tmpweights = new double [C][m];
//        for(int c = 0; c<C; c++) {
////        	for(int j = 0;j<m; j++) {	
////        		if(state == null) tmpweights[c][j] = 0;
////        		else {
////        			tmpweights[c][j] = state.random[thread].nextDouble()*2-1;
////        		}
////        	}
//        	weights[c][0] = 0;
//        	for(int j = 0;j<m;j++) {
//        		weights[c][j+1] = tmpweights[c][j]; 
//        	}
//        }
        //iterate 50 times
        //for each instance, identify the ground truth class, and use the two different gradient formulas to update the weights
        double [][] gradient = new double [C][m];
        double bestloss = getCrossEntropy(predict(X), Y, labels);
        int cnt = 0; //counting how many iterations without improvement
        double step = 0.01;
        boolean recalculate = true;
        for(int it = 0; it<150; it++) {
        	if(recalculate = true) {
        		for(int i = 0; i<n; i++) {
            		int k = 0; //class index
            		for(; k<labels.length; k++) {
            			if(labels[k] == Y[i]) {
            				break;
            			}
            		}
            		
            		double [] es = new double [C];
            		
            		double esk = Math.exp(Math.min(10, MatrixOperation.multiplyVector(tmpweights[k], X[i])));
            		double sum_es = 0;
            		
            		for(int j = 0; j<C; j++) {
            			es[j] = Math.exp(Math.min(10, MatrixOperation.multiplyVector(tmpweights[j], X[i])));
            			sum_es += es[j];
            		}
            		
            		for(int c = 0; c<C; c++) {
            			if(c == k) {
            				for(int l = 0;l<m; l++) {
                				gradient[c][l] += X[i][l]*(esk / (sum_es+1e-7) - 1);
                			}
            			}
            			else {
            				for(int l = 0;l<m; l++) {
                				gradient[c][l] += X[i][l]*es[c] / (sum_es+1e-7);
                			}
            			}

            		}
            		
            	}
            	
            	//average over instances
            	for(int c = 0; c<C; c++) {
            		for(int l = 0; l<m; l++) {
            			gradient[c][l] /= n;
            		}
            	}
        	}
        	
        	
        	//update weights
        	
//        	if(state != null) {
//        		step *= (state.random[thread].nextDouble()+0.5);
//        	}
        	for(int c = 0; c<C; c++) {
        		for(int l = 0; l<m; l++) {
        			tmpweights[c][l] -= gradient[c][l]*step;
        		}
        	}
        	double tmploss = getCrossEntropy(tmp_predict(X, tmpweights), Y, labels);
        	
        	if(tmploss < bestloss) {
        		
        		for(int c = 0; c<C; c++) {
            		for(int l = 0; l<m; l++) {
            			weights[c][l] = tmpweights[c][l];
            		}
            	}
        		recalculate = true;
        		bestloss = tmploss;
        		cnt = 0;
        		step *= 1.25;
        	}
        	else {
        		for(int c = 0; c<C; c++) {
            		for(int l = 0; l<m; l++) {
            			tmpweights[c][l] = weights[c][l];
            		}
            	}
        		recalculate = false;
        		cnt ++;
        		step *= 0.5;
        	}
        	if(cnt >= 5) break;
        	
//        	System.out.println(it + "\t" + tmploss);
//        	Loss += -Math.log(Math.exp(Ypred[i][k]) / tmp);
        }
        
        
//        for(int c = 0; c<C; c++) {
//        	for(int j = 1;j<=m; j++) {	
//        		weights[c][j] = tmpweights[c][j-1];
//        	}
//        }
        
        double loss = getCrossEntropy(predict(X), Y, labels);
        return loss;
    }

    public double[][] predict(double[][] X) {
        int n = X.length;
        int m = X[0].length;
        int C = weights.length;
        double[][] predictions = new double[n][C];

        for (int i = 0; i < n; i++) {
        	for(int c = 0; c<C; c++) {
        		predictions[i][c] = weights[c][0] * X[i][0];  // Intercept term
                for (int j = 1; j < m; j++) {
                    predictions[i][c] += weights[c][j] * X[i][j];
                }
        	}
            
        }

        return predictions;
    }
    
    public double[][] tmp_predict(double[][] X, double [][] tmpweights) {
        int n = X.length;
        int m = X[0].length;
        int C = tmpweights.length;
        double[][] predictions = new double[n][C];

        for (int i = 0; i < n; i++) {
        	for(int c = 0; c<C; c++) {
        		predictions[i][c] = tmpweights[c][0] * X[i][0];  // Intercept term
                for (int j = 1; j < m; j++) {
                    predictions[i][c] += tmpweights[c][j] * X[i][j];
                }
        	}
            
        }

        return predictions;
    }

    public double[][] getWeights() {
        return weights;
    }

	public double getCrossEntropy(double[][] Ypred, double [] Yreal, double[] labels) {
    	if(Ypred.length != Yreal.length) {
    		System.err.print("getCrossEntropy() in LinearClassifier is comparing Ys with different dimensions\n");
    		System.exit(1);
    	}
    	
    	if(Ypred.length == 0) {
    		System.err.print("getCrossEntropy() in LinearClassifier is comparing Ys with 0 length\n");
    		System.exit(1);
    	}
    	
    	if(Ypred[0].length != labels.length) {
    		System.err.print("getCrossEntropy() in LinearClassifier is comparing Ys with inconsistent number of labels\n");
    		System.exit(1);
    	}
    	
    	double Loss = 0;
    	int n = Ypred.length;
        int m = Ypred[0].length;
    	
    	
    	for(int i = 0; i<n; i++) {
    		double tmp = 0;
    		
    		for(int j = 0; j<m; j++) {
    			tmp += Math.exp(Ypred[i][j]);
    		}
    		
    		int k = 0;
    		for(; k<labels.length; k++) {
    			if(labels[k] == Yreal[i]) {
    				break;
    			}
    		}

    		double val = Math.exp(Ypred[i][k]) / (tmp+1e-7);
    		if(val!=0)
    			Loss += -Math.log(val);
    	}
    	
    	Loss /= n;
    	return Loss;
    }
	
    public static void main(String[] args) {
        double[][] X = {
            {1.0, 2.0},
            {2.0, 3.0},
            {3.0, 4.0},
            {4.0, 5.0}
        };

        double[] Y = {0.0, 0.0, 1.0, 1.0};
        
        double [] labels = {0, 1};

        LinearClassifier lc = new LinearClassifier();
        lc.fit(X, Y, labels, null, 0);

        double[][] weights = lc.getWeights();
//        System.out.println("Weights: " + Arrays.toString(weights));
        for(double [] row : weights) {
        	for(double v : row) {
        		System.out.print(v+"\t");
        	}
        	System.out.println();
        }

        double[][] X_test = {
        	{1.0, 2.0},
        	{3.0, 4.0},
            {5.0, 6.0},
            {6.0, 7.0}
        };

        double[][] predictions = lc.predict(X_test);
        
        double loss = lc.getCrossEntropy(predictions, Y, labels);
        System.out.println("loss: " + loss);
        
//        System.out.println("Predictions: " + Arrays.toString(predictions));
        for(double [] row : predictions) {
        	for(double v : row) {
        		System.out.print(v+"\t");
        	}
        	System.out.println();
        }
    }
}
