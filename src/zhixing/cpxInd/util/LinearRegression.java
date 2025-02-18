package zhixing.cpxInd.util;

import java.util.Arrays;

public class LinearRegression {
    private double[] weights;

    public double fit(double[][] X, double[] Y) {
        int n = X.length;
        int m = X[0].length;

        // Create the design matrix with an added column of ones for the intercept
        double[][] X_design = new double[n][m + 1];
        for (int i = 0; i < n; i++) {
            X_design[i][0] = 1.0;  // Intercept term
            for (int j = 0; j < m; j++) {
                X_design[i][j + 1] = X[i][j];
            }
        }

        // Transpose of the design matrix
        double[][] Xt = MatrixOperation.transpose(X_design);

        // XtX = Xt * X
        double[][] XtX = MatrixOperation.multiplyMatrices(Xt, X_design);

        // XtY = Xt * Y
        double[] XtY = MatrixOperation.multiplyMatrixVector(Xt, Y);

        // Inverse of XtX
        double[][] XtX_inv = MatrixOperation.invert(XtX);

        // Weights = XtX_inv * XtY
        weights = MatrixOperation.multiplyMatrixVector(XtX_inv, XtY);
        
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
            {1.0, 2.0},
            {2.0, 3.0},
            {3.0, 4.0},
            {4.0, 5.0}
        };

        double[] Y = {6.0, 7.0, 8.0, 9.0};

        LinearRegression lr = new LinearRegression();
        lr.fit(X, Y);

        double[] weights = lr.getWeights();
        System.out.println("Weights: " + Arrays.toString(weights));

        double[][] X_test = {
        	{1.0, 2.0},
        	{3.0, 4.0},
            {5.0, 6.0},
            {6.0, 7.0}
        };

        double[] predictions = lr.predict(X_test);
        System.out.println("Predictions: " + Arrays.toString(predictions));
    }
}

