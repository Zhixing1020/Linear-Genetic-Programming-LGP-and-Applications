package zhixing.symbolicregression.algorithm.LandscapeOptimization.toy.optimization;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import zhixing.symbolicregression.optimization.GPSymbolicRegression;

public class GPToySymbolicRegression extends GPSymbolicRegression{

	@Override
	protected void normalizedataBasedTraining() {
		if(data == null) {
			return;
		}
		
		//can only read the training data
		String filename = "";
		filename = location + dataname + "_training_data.txt";
		int num = 0, dim = 0;
		
		double [][] traindata = null;
		double [] traindata_output = null;
		
		try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String str = in.readLine();
            if(str != null) {
            	String [] info = str.split("\t");
            	num = Integer.valueOf(info[0]);
            	dim = Integer.valueOf(info[1]);
            	
            	traindata = new double[num][dim];
            	traindata_output = new double [num];
            }
            
            for(int d = 0; d<num; d++) {
            	if((str = in.readLine()) != null) {
            		String[] info = str.split("\t");
                    for(int i = 0; i<dim; i++) {
                    	traindata[d][i] = Double.valueOf(info[i]);
                    }
                    traindata_output[d] = Double.valueOf(info[dim]);
            	}
            }
            
        } catch (IOException e) {
        }
		
		
		normdata = new double [datanum][datadim];
		
//		norm_mean = new double [datadim];
//		norm_std = new double [datadim];
//		
//		//get mean of training data
//		for(int d = 0; d<dim; d++) {
//			double mean = 0;
//			for(int j = 0 ; j<num; j++) {
//				mean += traindata[j][d] / num;
//			}
//			norm_mean[d] = mean;
//		}
//		
//		//get std of training data
//		for(int d = 0; d<dim; d++) {
//			double std = 0;
//			for(int j = 0; j<num; j++) {
//				std += Math.pow(traindata[j][d] - norm_mean[d], 2) / num;
//			}
//			norm_std[d] = Math.sqrt(std);
//		}
		
		//normalize data
		for(int d = 0; d<datadim; d++) {
			for(int j = 0; j<datanum; j++) {
				normdata[j][d] = data[j][d];
			}
		}
		
	}
	
	@Override
	protected void getOutMeanStd() {
		if(location.length() == 0 || dataname.length() == 0) {
			return;
		}
		
		//can only read the training data
		String filename = "";
		filename = location + dataname + "_training_data.txt";
		int num = 0, dim = 0;
		
		double [][] traindata = null;
		double [] traindata_output = null;
		
		try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String str = in.readLine();
            if(str != null) {
            	String [] info = str.split("\t");
            	num = Integer.valueOf(info[0]);
            	dim = Integer.valueOf(info[1]);
            	
            	traindata = new double[num][dim];
            	traindata_output = new double [num];
            }
            
            for(int d = 0; d<num; d++) {
            	if((str = in.readLine()) != null) {
            		String[] info = str.split("\t");
                    for(int i = 0; i<dim; i++) {
                    	traindata[d][i] = Double.valueOf(info[i]);
                    }
                    traindata_output[d] = Double.valueOf(info[dim]);
            	}
            }
            
        } catch (IOException e) {
        }
		
		//get mean
		out_mean = 0.;
//		double mean = 0;
//		for(int j = 0 ; j<num; j++) {
//			mean += traindata_output[j] / num;
//		}
//		out_mean = mean;
		
		//get std
		out_std = 1.;
//		double std = 0;
//		for(int j = 0; j<num; j++) {
//			std += Math.pow(traindata_output[j] - out_mean, 2) / num;
//		}
//		out_std = Math.sqrt(std);
		
	}
}
