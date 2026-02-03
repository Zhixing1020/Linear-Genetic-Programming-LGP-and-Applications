package zhixing.symbolic_classification.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ec.EvolutionState;
import zhixing.cpxInd.util.MatrixOperation;

import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

public class LDA_Transform {

    private SimpleMatrix [] eigenVectors;
    
    public double fit(double[][] X, double[] y) {
        int nFeatures = X[0].length;
        Set<Double> uniqueClasses = new HashSet<>();
        for (double label : y) uniqueClasses.add(label);
        
//        if (uniqueClasses.size() != 2) {
//            throw new IllegalArgumentException("This implementation supports only two classes.");
//        }
        
        double[] meanTotal = new double[nFeatures];
        Map<Double, double[]> classMeans = new HashMap<>();
        Map<Double, ArrayList<double[]>> classData = new HashMap<>();
        
        for (Double label : uniqueClasses) {
            classMeans.put(label, new double[nFeatures]);
            classData.put(label, new ArrayList<>());
        }
        
        for (int i = 0; i < X.length; i++) {
            classData.get(y[i]).add(X[i]);
            for (int j = 0; j < nFeatures; j++) {
                meanTotal[j] += X[i][j];
            }
        }
        
        for (int j = 0; j < nFeatures; j++) {
            meanTotal[j] /= X.length;
        }
        
        for (Double label : uniqueClasses) {
            List<double[]> dataPoints = classData.get(label);
            double[] meanVector = classMeans.get(label);
            for (double[] point : dataPoints) {
                for (int j = 0; j < nFeatures; j++) {
                    meanVector[j] += point[j];
                }
            }
            for (int j = 0; j < nFeatures; j++) {
                meanVector[j] /= dataPoints.size();
            }
        }
        
        SimpleMatrix SW = new SimpleMatrix(nFeatures, nFeatures);
        for (Double label : uniqueClasses) {
            List<double[]> dataPoints = classData.get(label);
            double[] meanVector = classMeans.get(label);
            for (double[] point : dataPoints) {
                double[][] diff = new double[nFeatures][1];
                for (int j = 0; j < nFeatures; j++) {
                    diff[j][0] = point[j] - meanVector[j];
                }
                SimpleMatrix diffMatrix = new SimpleMatrix(diff);
                SW = SW.plus(diffMatrix.mult(diffMatrix.transpose()));
            }
        }
        
        SimpleMatrix SB = new SimpleMatrix(nFeatures, nFeatures);
        for (Double label : uniqueClasses) {
            double[] meanVector = classMeans.get(label);
            double[][] meanDiff = new double[nFeatures][1];
            for (int j = 0; j < nFeatures; j++) {
                meanDiff[j][0] = meanVector[j] - meanTotal[j];
            }
            SimpleMatrix meanDiffMatrix = new SimpleMatrix(meanDiff);
            SB = SB.plus(meanDiffMatrix.mult(meanDiffMatrix.transpose()));
        }
        
        SimpleMatrix SW_inv = SW.pseudoInverse();
        SimpleMatrix S = SW_inv.mult(SB);
        
        SimpleSVD res = S.svd();
        SimpleMatrix U = res.getU();
        SimpleMatrix W = res.getW();
        
        this.eigenVectors = new SimpleMatrix [uniqueClasses.size()-1];
        for(int e = 0; e< Math.min(nFeatures, uniqueClasses.size()-1); e++) {
        	this.eigenVectors[e] = U.extractMatrix(0, U.numRows(), e, e+1);
        }
        
        
        double loss = eigenVectors[0].transpose().mult(SB).mult(eigenVectors[0]).getMatrix().getData()[0];
        loss -= W.get(0, 0)*(eigenVectors[0].transpose().mult(SW).mult(eigenVectors[0]).getMatrix().getData()[0] - 1);
        
        return loss;
    }
    
//    public double fit(double[][] X, double[] y, int dim) {
//    	//dim: the dim^th eigenvector
//        int nFeatures = X[0].length;
//        Set<Double> uniqueClasses = new HashSet<>();
//        for (double label : y) uniqueClasses.add(label);
//        
////        if (uniqueClasses.size() != 2) {
////            throw new IllegalArgumentException("This implementation supports only two classes.");
////        }
//        
//        double[] meanTotal = new double[nFeatures];
//        Map<Double, double[]> classMeans = new HashMap<>();
//        Map<Double, ArrayList<double[]>> classData = new HashMap<>();
//        
//        for (Double label : uniqueClasses) {
//            classMeans.put(label, new double[nFeatures]);
//            classData.put(label, new ArrayList<>());
//        }
//        
//        if(dim > uniqueClasses.size() - 1) {
//        	System.err.print("the dimension index of " + dim + " is larger than the number of classes - 1: " + (uniqueClasses.size() - 1));
//        	System.exit(1);
//        }
//        
//        for (int i = 0; i < X.length; i++) {
//            classData.get(y[i]).add(X[i]);
//            for (int j = 0; j < nFeatures; j++) {
//                meanTotal[j] += X[i][j];
//            }
//        }
//        
//        for (int j = 0; j < nFeatures; j++) {
//            meanTotal[j] /= X.length;
//        }
//        
//        for (Double label : uniqueClasses) {
//            List<double[]> dataPoints = classData.get(label);
//            double[] meanVector = classMeans.get(label);
//            for (double[] point : dataPoints) {
//                for (int j = 0; j < nFeatures; j++) {
//                    meanVector[j] += point[j];
//                }
//            }
//            for (int j = 0; j < nFeatures; j++) {
//                meanVector[j] /= dataPoints.size();
//            }
//        }
//        
//        SimpleMatrix SW = new SimpleMatrix(nFeatures, nFeatures);
//        for (Double label : uniqueClasses) {
//            List<double[]> dataPoints = classData.get(label);
//            double[] meanVector = classMeans.get(label);
//            for (double[] point : dataPoints) {
//                double[][] diff = new double[nFeatures][1];
//                for (int j = 0; j < nFeatures; j++) {
//                    diff[j][0] = point[j] - meanVector[j];
//                }
//                SimpleMatrix diffMatrix = new SimpleMatrix(diff);
//                SW = SW.plus(diffMatrix.mult(diffMatrix.transpose()));
//            }
//        }
//        
//        SimpleMatrix SB = new SimpleMatrix(nFeatures, nFeatures);
//        for (Double label : uniqueClasses) {
//            double[] meanVector = classMeans.get(label);
//            double[][] meanDiff = new double[nFeatures][1];
//            for (int j = 0; j < nFeatures; j++) {
//                meanDiff[j][0] = meanVector[j] - meanTotal[j];
//            }
//            SimpleMatrix meanDiffMatrix = new SimpleMatrix(meanDiff);
//            SB = SB.plus(meanDiffMatrix.mult(meanDiffMatrix.transpose()));
//        }
//        
//        SimpleMatrix SW_inv = SW.pseudoInverse();
//        SimpleMatrix S = SW_inv.mult(SB);
//        
//        SimpleSVD res = S.svd();
//        SimpleMatrix U = res.getU();
//        SimpleMatrix W = res.getW();
//        this.eigenVectors = U.extractMatrix(0, U.numRows(), dim, dim+1);
//        
//        double loss = eigenVectors.transpose().mult(SB).mult(eigenVectors).getMatrix().getData()[0];
//        loss -= W.get(dim, dim)*(eigenVectors.transpose().mult(SW).mult(eigenVectors).getMatrix().getData()[0] - 1);
//        
//        return loss;
//    }
    
    public double[] transform(double[][] X) {
        SimpleMatrix dataMatrix = new SimpleMatrix(X);
        SimpleMatrix projected = dataMatrix.mult(this.eigenVectors[0]);
        return projected.getMatrix().getData();
    }
    public double transform(double[] X) {
    	double [][] tmpX = new double [1][X.length];
    	for(int i = 0; i<X.length; i++) {
    		tmpX[0][i] = X[i];
    	}
        SimpleMatrix dataMatrix = new SimpleMatrix(tmpX);
        SimpleMatrix projected = dataMatrix.mult(this.eigenVectors[0]);
        return projected.getMatrix().getData()[0];
    }
    public double transform(double[] X, int e) {
    	//e: using the e^th eigen vector to transform
    	double [][] tmpX = new double [1][X.length];
    	for(int i = 0; i<X.length; i++) {
    		tmpX[0][i] = X[i];
    	}
        SimpleMatrix dataMatrix = new SimpleMatrix(tmpX);
        SimpleMatrix projected = dataMatrix.mult(this.eigenVectors[e]);
        return projected.getMatrix().getData()[0];
    }
    public double[] getWeights() {
        return this.eigenVectors[0].getMatrix().getData();
    }
  
    public double[] getWeights(int e) {
    	//e: the e^th eigen vector
        return this.eigenVectors[e].getMatrix().getData();
    }
    
    public static void main(String[] args) {
        double[][] X = {
            {4.0, 2.0}, {2.0, 4.0}, {2.0, 3.0}, {3.0, 6.0}, {4.0, 4.0},
            {9.0, 10.0}, {6.0, 8.0}, {9.0, 5.0}, {8.0, 7.0}, {10.0, 8.0}
        };
        double[] y = {0, 0, 0, 0, 0, 1, 1, 3.2, 3.2, 3.2};
        
        LDA_Transform lda = new LDA_Transform();
        double loss = lda.fit(X, y);
//        double[] transformed = lda.transform(X);
        for(double [] x : X) {
        	double res = lda.transform(x);
        	System.out.println(res);
        }
        
        System.out.print(Arrays.toString(lda.getWeights()));

    }
}
