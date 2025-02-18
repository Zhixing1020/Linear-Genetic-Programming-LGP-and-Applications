package zhixing.cpxInd.util;

public class MatrixOperation {

	public static double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposed = new double[cols][rows];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }

        return transposed;
    }

	public static double[][] multiplyMatrices(double[][] a, double[][] b) {
        int rowsA = a.length;
        int colsA = a[0].length;
        int colsB = b[0].length;
        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                result[i][j] = 0.0;
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }

        return result;
    }

	public static double[] multiplyMatrixVector(double[][] matrix, double[] vector) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[] result = new double[rows];

        for (int i = 0; i < rows; i++) {
            result[i] = 0.0;
            for (int j = 0; j < cols; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }

        return result;
    }

	public static double[][] invert(double[][] matrix) {
        int n = matrix.length;
        double[][] identity = new double[n][n];
        double[][] copy = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    identity[i][j] = 1.0;
                } else {
                    identity[i][j] = 0.0;
                }
                copy[i][j] = matrix[i][j];
            }
        }
        //[copy identity] is an augment matrix

        // Perform Gaussian elimination
        for (int i = 0; i < n; i++) {
        	// Make the diagonal element in the augment matrix 1
            double diag = copy[i][i];
            for (int j = 0; j < n; j++) {
                copy[i][j] /= Math.sqrt(diag*diag+1e-8)*(diag>=0?1:-1);
                identity[i][j] /= Math.sqrt(diag*diag+1e-8)*(diag>=0?1:-1);
            }
            
            //Make the other elements in column i to be 0
            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = copy[k][i];
                    for (int j = 0; j < n; j++) {
                        copy[k][j] -= factor * copy[i][j];
                        identity[k][j] -= factor * identity[i][j];
                    }
                }
            }
        }

        return identity;
    }
}
