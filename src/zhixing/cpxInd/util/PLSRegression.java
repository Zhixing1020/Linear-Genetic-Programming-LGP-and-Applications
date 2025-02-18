package zhixing.cpxInd.util;

import java.util.Arrays;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;

public class PLSRegression {

	private RealMatrix X_weights;   // X weight matrix
    private RealMatrix X_scores;    // X score matrix
//    private RealMatrix Y_scores;    // Y score matrix
    private RealMatrix X_loadings;  // X loading matrix
    private RealMatrix q_vector;
    private int n_components;       // PLS number of components        
    
	private double[] weights;
	
	public double fit(double[][] X, double[] Y, int n_components) {
		//give X: R n*m, Y: n*l.
    	//Maximize <X * w_1, Y * c_1>, where ||w_1||=1 and ||c_1||=1     <...> for inner product
    	//scoring vectors t_1 = X * w_1, u_1 = Y * c_1.   where w_1 and c_1 are the weight vectors 
    	//also written as Maximize sqrt(Var(t_1)*Var(u_1))Corr(t_1, u_1) = <t_1, u_1>
    	//the solutions w_1 and c_1 of the optimization objective are the eigenvectors of X^TYY^TX (m*m) and eigenvectors of Y^TXX^TY (l*l)
		
		//regression: X = t_1*p_1^T + E, Y = u_1*q_1^T+G -> Y = t_1*q_1^T+F = X * w_1 * q_1^T + F
		
        this.n_components = n_components;
        
        double tol = 1e-7;
        
        RealMatrix X_matrix = new Array2DRowRealMatrix(X);
        RealMatrix Y_vector = new Array2DRowRealMatrix(Y);

        // initialize weights, score matrices, and loading matrices
        int n_samples = X_matrix.getRowDimension();
        int n_features_X = X_matrix.getColumnDimension();

        X_weights = new Array2DRowRealMatrix(n_features_X, n_components);
//        Y_weights = new Array2DRowRealMatrix(n_features_Y, n_components);
        X_scores = new Array2DRowRealMatrix(n_samples, n_components);
//        Y_scores = new Array2DRowRealMatrix(n_samples, n_components);
        X_loadings = new Array2DRowRealMatrix(n_features_X, n_components);
//        Y_loadings = new Array2DRowRealMatrix(n_features_Y, n_components);
        q_vector = new Array2DRowRealMatrix(n_components, 1);

        RealMatrix X_residual = X_matrix.copy();
        
        for (int i = 0; i < n_components; i++) {
            RealMatrix u = Y_vector;  // initialize score vector u as the first column of Y

            RealMatrix w = null;
            RealMatrix t = null;
            RealMatrix p = null;
            RealMatrix q = null;
            
            RealMatrix c = null;

            // get the weights of X_residual
            w = X_residual.transpose().multiply(Y_vector);

            // get score vector t  t is a kind of component
            t = X_residual.multiply(w);
            double tk = t.transpose().multiply(t).getEntry(0, 0);
            t = t.scalarMultiply(1.0 / tk); 
            
            // update loading vectors p and q
            p = X_residual.transpose().multiply(t);
            
            double q_scale = Y_vector.transpose().multiply(t).getEntry(0, 0);

            // storing results
            X_weights.setColumnMatrix(i, w);
//            Y_weights.setColumnMatrix(i, c);
            X_scores.setColumnMatrix(i, t);
//            Y_scores.setColumnMatrix(i, u);
            X_loadings.setColumnMatrix(i, p);
//            Y_loadings.setColumnMatrix(i, q);
            q_vector.setEntry(i, 0, q_scale);
            
            // Step 7: update residual error
            X_residual = X_residual.subtract(t.multiply(p.transpose()).scalarMultiply(tk));
            
            if(q_scale == 0 || X_residual.getFrobeniusNorm() < tol) {
            	this.n_components = i+1;
            	break;
            }
        }
        
        RealMatrix PTW =  X_loadings.transpose().multiply(X_weights);
        LUDecomposition luDecomposition = new LUDecomposition(PTW);
        double determinant = luDecomposition.getDeterminant();
        
        RealMatrix inverseMatrix;
        if(determinant != 0) {
        	inverseMatrix = luDecomposition.getSolver().getInverse();
        }
        else {
        	double [][] tmp = PTW.getData();
        	double [][] inverseArray = MatrixOperation.invert(tmp);
        	inverseMatrix = new Array2DRowRealMatrix(inverseArray);
        }
        
        RealMatrix weights_B = X_weights.multiply(inverseMatrix).multiply(q_vector);
        double [][] B = weights_B.getData();
        
        double B0 =  q_vector.getEntry(0, 0) - X_loadings.getColumnMatrix(0).transpose().multiply(weights_B).getEntry(0, 0);
        
        weights = new double [B.length + 1];
        weights[0] = B0;
        for(int i = 1; i<weights.length; i++) {
        	weights[i] = B [i-1][0];
        }
        
        double rmse = getRMSE(predict(X), Y);
        return rmse;
    }
	
	public double[] predict(double[][] X) {
		int n = X.length;
        int m = weights.length - 1;
        double[] predictions = new double[n];

        for (int i = 0; i < n; i++) {
            predictions[i] = weights[0];  // Intercept term
            for (int j = 1; j <= m; j++) {
                predictions[i] += weights[j] * X[i][j - 1];
            }
        }

        return predictions;
    }

    public double[] getWeights() {
        return weights;
    }

    private double getRMSE(double [] Ypred, double [] Yreal) {
    	if(Ypred.length != Yreal.length) {
    		System.err.print("getRMSE() in LinearRegression is comparing Ys with different dimensions\n");
    		System.exit(1);
    	}
    	
    	if(Ypred.length == 0) {
    		System.err.print("getRMSE() in LinearRegression is comparing Ys with 0 length\n");
    		System.exit(1);
    	}
    	
    	double res = 0;
    	for(int i = 0; i<Ypred.length; i++) {
    		res += (Ypred[i] - Yreal[i])*(Ypred[i] - Yreal[i]);
    	}
    	
    	res = Math.sqrt(res / Ypred.length);
    	return res;
    }

    public static void main(String[] args) {
        double[][] X = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9},
            {10, 11, 12}
        };

        double [] Y = {6,7,8,9};

        PLSRegression pls = new PLSRegression();
        pls.fit(X, Y, 3);
     
        double[][] X_test = {
        	{1, 2, 3},
            {2, 3, 4},
            {5, 6, 7},
            {4, 5, 6},
            {7, 8, 9},
            {10, 11, 12}
        };


        double[] Y_pred = pls.predict(X_test);

        System.out.println("Predictions: " + Arrays.toString(Y_pred));

        System.out.println("Weights: " + Arrays.toString(pls.getWeights()));
    }
}
