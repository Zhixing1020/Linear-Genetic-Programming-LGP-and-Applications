package zhixing.symbolicregression.optimization;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.spiderland.Psh.booleanStack;
import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.app.majority.func.X;
import yimei.jss.gp.data.DoubleData;
import ec.gp.GPIndividual;
//import ec.app.tutorial4.MultiValuedRegression;
//import ec.app.tutorial4.Pow;
import ec.gp.GPProblem;
import ec.gp.koza.KozaFitness;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.semantic.individual.SLGPIndividual;
import zhixing.symbolicregression.individual.CpxGPInterface4SR;

public class GPSymbolicRegression extends GPProblem implements SimpleProblemForm {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String PROBLEM_P = "SRproblem";
	public static final String LOCATION_P = "location";
	public static final String DATA_NAME_P = "dataname";
	public static final String FITNESS_P = "fitness";
	
	protected String location;
	protected String dataname;
	protected String fitness;
	protected boolean istraining;
	
	public int datanum;
	public int datadim;
	
	public static double data [][]; //all data instances
	public static double data_output[]; //the real output of all data instances
	public static double normdata[][]; //the data instances after normalization
//	public static double normdata_output[];  //the normalized output
	protected static double norm_mean[];
	protected static double norm_std[];
	
	public static double out_mean; //the output mean and std can only obtain from training data
	public static double out_std;
	
	public double X[];  //a data instance
	
	public GPSymbolicRegression () {
		setGPSymbolicRegression ("", "", "RMSE", true);
	}
	
	public GPSymbolicRegression (String loca, String datan, String fitn, boolean istraining) {
		setGPSymbolicRegression (loca, datan, fitn, istraining);
	}

	protected void setGPSymbolicRegression (String loca, String datan, String fitn, boolean istraining) {
		location = loca;
		dataname = datan;
		
		if(!location.endsWith("\\")||!location.endsWith("/")) {
			if(location.contains("\\")) {
				location += "\\";
			}
			else if(location.contains("/")) {
				location += "/";
			}
		}
		
		String filename = "";
		
		this.istraining = istraining;
		
		if(this.istraining) {
			filename = location + dataname + "_training_data.txt";
		}
		else {
			filename = location + dataname + "_testing_data.txt";
		}
		
		try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String str = in.readLine();
            if(str != null) {
            	String [] info = str.split("\t|,");
            	datanum = Integer.valueOf(info[0]);
            	datadim = Integer.valueOf(info[1]);
            	
            	data = new double[datanum][datadim];
            	data_output = new double [datanum];
            }
            
            for(int d = 0; d<datanum; d++) {
            	if((str = in.readLine()) != null) {
            		String[] info = str.split("\t");
                    for(int i = 0; i<datadim; i++) {
                    	data[d][i] = Double.valueOf(info[i]);
                    }
                    data_output[d] = Double.valueOf(info[datadim]);
            	}
            }
            
        } catch (IOException e) {
        }
		
		fitness = fitn;
		
		if(!(fitness.equals("RMSE")||fitness.equals("MSE")||fitness.equals("R2")||fitness.equals("RSE"))) {
			System.err.print(fitness + " must be one of the following objectives: RMSE, MSE, RSE, and R2");
			System.exit(1);
		}
		
		//normalize data
		normalizedataBasedTraining();
		
		//get the mean and std in order to normalize output
		getOutMeanStd();
		
//		if(dataname.equals("Airfoil") || dataname.equals("BHouse") || dataname.equals("CNN") 
//				|| dataname.equals("Concrete") || dataname.equals("Redwine") || dataname.equals("Tower")
//				|| dataname.equals("USCrime") || dataname.equals("Whitewine")
//				) {
//			
//		}
//		else {
//			normdata = new double [datanum][datadim];
//			
//			norm_mean = new double [datadim];
//			norm_std = new double [datadim];
//			
//			//use the raw data
//			for(int d = 0; d<datadim; d++) {
//				for(int j = 0; j<datanum; j++) {
//					normdata[j][d] = data[j][d];						
//				}
//			}
//			
//			for(int d = 0; d<datadim; d++) {
//				norm_mean[d] = 0;
//				norm_std[d] = 1;
//			}
//			
//			out_mean = 0;
//			out_std = 1;
//		}
	}
	
	//take distance problem as an example
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = new Parameter(PROBLEM_P);
		
		// verify our input is the right class (or subclasses from it)
        if (!(input instanceof DoubleData))
            state.output.fatal("GPData class must subclass from " + DoubleData.class,
                base.push(P_DATA), null);
		
		//read the location
		location = state.parameters.getString(base.push(LOCATION_P),def.push(LOCATION_P));
//		if(!location.endsWith("\\")||!location.endsWith("/")) {
//			if(location.contains("\\")) {
//				location += "\\";
//			}
//			else if(location.contains("/")) {
//				location += "/";
//			}
//		}
		
		//read the dataset name
		dataname = state.parameters.getString(base.push(DATA_NAME_P),def.push(DATA_NAME_P));
		
		fitness = state.parameters.getString(base.push(FITNESS_P),def.push(FITNESS_P));
		
		this.setGPSymbolicRegression(location, dataname, fitness, true);
		
		//read the data
//		String filename = location + dataname + "_training_data.txt";
//		
//		try {
//            BufferedReader in = new BufferedReader(new FileReader(filename));
//            String str = in.readLine();
//            if(str != null) {
//            	String [] info = str.split("\t");
//            	datanum = Integer.valueOf(info[0]);
//            	datadim = Integer.valueOf(info[1]);
//            	
//            	data = new double[datanum][datadim];
//            	data_output = new double [datanum];
//            }
//            
//            for(int d = 0; d<datanum; d++) {
//            	if((str = in.readLine()) != null) {
//            		String[] info = str.split("\t");
//                    for(int i = 0; i<datadim; i++) {
//                    	data[d][i] = Double.valueOf(info[i]);
//                    }
//                    data_output[d] = Double.valueOf(info[datadim]);
//            	}
//            }
//            
//        } catch (IOException e) {
//        }
//		
//		//read the fitness type (RMSE, MSE, or R2)
//		
//		if(!(fitness.equals("RMSE")||fitness.equals("MSE")||fitness.equals("R2")||fitness.equals("RSE"))) {
//			System.err.print(fitness + " must be one of the following objectives: RMSE, MSE, RSE, and R2");
//			System.exit(1);
//		}
		
	}
	
	@Override
	public void evaluate(final EvolutionState state, 
	        final Individual ind, 
	        final int subpopulation,
	        final int threadnum) {
		
		 if (!ind.evaluated)  // don't bother reevaluating
         {
			 if(data.length==0 || data_output.length==0) {
				 System.err.print("we have an empty data source\n");
				 System.exit(1);
			 }
			 double [] real = new double [datanum]; 
			 double [] predict = new double [datanum]; 
			 
	         DoubleData input = (DoubleData)(this.input);
	     
	         int hits = 0;
	         double sum = 0.0;
	         double expectedResult;
	         double result = 0;
	         for (int y=0;y<datanum;y++)
	         {
	             
	             DoubleData tmp = new DoubleData();
//	             int treeNum = ((LGPIndividual)ind).getTreesLength();
	             
	             X = new double[datadim];
	             for(int d = 0; d<datadim; d++) {
//	            	 X[d] = data[y][d];
	            	 X[d] = normdata[y][d];
	             }
	             
	             //((LGPIndividual4SR)ind).resetRegisters(this);
//	             LGPIndividual4SR.resetRegisters(this, 1.0, (LGPIndividual) ind);
//	             
//	     		for(int index = 0; index<treeNum; index++){
//	     			GPTreeStruct tree = ((LGPIndividual)ind).getTreeStruct(index);
//	     			if(tree.status) {
//	     				tree.child.eval(null, 0, tmp, null, (LGPIndividual)ind, this);
//		        			 if(((LGPIndividual)ind).getRegisters()[((WriteRegisterGPNode)tree.child).getIndex()] 
//		        					 >= Double.POSITIVE_INFINITY) {
//		 	                	int a =1;
//		 	                }
//	     			}
//	     			
//	     		}
	             
	            if(ind instanceof SLGPIndividual) {
	            	((SLGPIndividual)ind).setDataIndex(y); 
	            }
	             
	            predict[y] = ((CpxGPInterface4SR)ind).execute(state, threadnum, input, stack, (GPIndividual) ind, this);
	            predict[y] = predict[y]*out_std + out_mean;
	     		
	     		real[y] = data_output[y];
//	     		predict[y] = ((LGPIndividual)ind).getRegisters()[0];
	     		
	     		 if(predict[y] >= Double.POSITIVE_INFINITY || Double.isNaN(predict[y])) {
		             	predict[y] = 1e6;
		             }
	     		
	             result = Math.abs(real[y] - predict[y]);

	             if (result <= 0.01) hits++;        
	        }
	         
	         
	         if(fitness.equals("RMSE")) {
	        	 result = getRMSE(real, predict);
	         }
	         else if (fitness.equals("MSE")) {
	        	 result = getMSE(real, predict);
	         }
	         else if (fitness.equals("R2")) {
	        	 result = getR2(real, predict);
	         }
	         else if(fitness.equals("RSE")) {
	        	 result = getRSE(real, predict);
	         }
	         else {
	        	 System.err.print("unknown fitness objective "+fitness);
	        	 System.exit(1);
	         }
			
			// the fitness better be KozaFitness!
//	        KozaFitness f = ((KozaFitness)ind.fitness);
//	        f.setStandardizedFitness(state, result);
	        
	        double[] fitnesses = new double[1];
	        fitnesses[0] = result;
	        MultiObjectiveFitness f = (MultiObjectiveFitness) ind.fitness;
			f.setObjectives(state, fitnesses);
//	        f.hits = hits;
	        ind.evaluated = true;
        }
	}
	
	public void simpleevaluate(final Individual ind) {
		
		 if (!ind.evaluated)  // don't bother reevaluating
         {
			 double [] real = new double [datanum]; 
			 double [] predict = new double [datanum]; 
			 
	         DoubleData input = (DoubleData)(this.input);
	     
	         int hits = 0;
	         double sum = 0.0;
	         double expectedResult;
	         double result = 0;
	         for (int y=0;y<datanum;y++)
	         {
	             
	             DoubleData tmp = new DoubleData();
//	             int treeNum = ((LGPIndividual)ind).getTreesLength();
	             
	             X = new double[datadim];
	             for(int d = 0; d<datadim; d++) {
//	            	 X[d] = data[y][d];
	            	 X[d] = normdata[y][d];
	             }
	             
	           //((LGPIndividual4SR)ind).resetRegisters(this);
//	             LGPIndividual4SR.resetRegisters(this, 1.0, (LGPIndividual) ind);
//	             
//	     		for(int index = 0; index<treeNum; index++){
//	     			GPTreeStruct tree = ((LGPIndividual)ind).getTreeStruct(index);
//	     			if(tree.status) {
//	     				tree.child.eval(null, 0, tmp, null, (LGPIndividual)ind, this);
//		        			 if(((LGPIndividual)ind).getRegisters()[((WriteRegisterGPNode)tree.child).getIndex()] 
//		        					 >= Double.POSITIVE_INFINITY) {
//		 	                	int a =1;
//		 	                }
//	     			}
//	     			
//	     		}
	     		
	     		predict[y] = ((CpxGPInterface4SR)ind).execute(null, 0, tmp, stack, (GPIndividual) ind, this);
	     		predict[y] = predict[y]*out_std + out_mean;
	     		
	     		real[y] = data_output[y];
//	     		predict[y] = ((LGPIndividual)ind).getRegisters()[0];
	     		
	     		 if(predict[y] >= Double.POSITIVE_INFINITY || Double.isNaN(predict[y])) {
		             	predict[y] = 1e6;
		             }
	     		
	     		result = Math.abs(real[y] - predict[y]);

	             if (result <= 0.01) hits++;        
	        }
	         
	         
	         if(fitness.equals("RMSE")) {
	        	 result = getRMSE(real, predict);
	         }
	         else if (fitness.equals("MSE")) {
	        	 result = getMSE(real, predict);
	         }
	         else if (fitness.equals("R2")) {
	        	 result = getR2(real, predict);
	         }
	         else if(fitness.equals("RSE")) {
	        	 result = getRSE(real, predict);
	         }
	         else {
	        	 System.err.print("unknown fitness objective "+fitness);
	        	 System.exit(1);
	         }
			
			// the fitness better be KozaFitness!
//	        KozaFitness f = ((KozaFitness)ind.fitness);
//	        f.setStandardizedFitness(state, result);
	        
	        double[] fitnesses = new double[1];
	        fitnesses[0] = result;
	        if(ind.fitness == null) {
	        	ind.fitness = new MultiObjectiveFitness();
	        }
	        MultiObjectiveFitness f = (MultiObjectiveFitness) ind.fitness;
//	        f.objectives = fitnesses;
            f.objectives = new double[1];
			f.setObjectives(null, fitnesses);
//	        f.hits = hits;
	        ind.evaluated = true;
	        
        }
	}
	
	public double[] getOutputs(final Individual ind) {
		
		if (!ind.evaluated)  // don't bother reevaluating
        {
			 double [] real = new double [datanum]; 
			 double [] predict = new double [datanum]; 
			 
	         DoubleData input = (DoubleData)(this.input);
	     
	         int hits = 0;
	         double sum = 0.0;
	         double expectedResult;
	         double result = 0;
	         for (int y=0;y<datanum;y++)
	         {
	             
	             DoubleData tmp = new DoubleData();
//	             int treeNum = ((LGPIndividual)ind).getTreesLength();
	             
	             X = new double[datadim];
	             for(int d = 0; d<datadim; d++) {
//	            	 X[d] = data[y][d];
	            	 X[d] = normdata[y][d];
	             }
	             
	           //((LGPIndividual4SR)ind).resetRegisters(this);
//	             LGPIndividual4SR.resetRegisters(this, 1.0, (LGPIndividual) ind);
//	             
//	     		for(int index = 0; index<treeNum; index++){
//	     			GPTreeStruct tree = ((LGPIndividual)ind).getTreeStruct(index);
//	     			if(tree.status) {
//	     				tree.child.eval(null, 0, tmp, null, (LGPIndividual)ind, this);
//		        			 if(((LGPIndividual)ind).getRegisters()[((WriteRegisterGPNode)tree.child).getIndex()] 
//		        					 >= Double.POSITIVE_INFINITY) {
//		 	                	int a =1;
//		 	                }
//	     			}
//	     			
//	     		}
	     		
	     		predict[y] = ((CpxGPInterface4SR)ind).execute(null, 0, tmp, stack, (GPIndividual) ind, this);
	     		predict[y] = predict[y]*out_std + out_mean;
	     		
	     		real[y] = data_output[y];
//	     		predict[y] = ((LGPIndividual)ind).getRegisters()[0];
	     		
	     		 if(predict[y] >= Double.POSITIVE_INFINITY || Double.isNaN(predict[y])) {
		             	predict[y] = 1e6;
		             }
	     		
	     		result = Math.abs(real[y] - predict[y]);

	             if (result <= 0.01) hits++;        
	        }
	         
	         
	         if(fitness.equals("RMSE")) {
	        	 result = getRMSE(real, predict);
	         }
	         else if (fitness.equals("MSE")) {
	        	 result = getMSE(real, predict);
	         }
	         else if (fitness.equals("R2")) {
	        	 result = getR2(real, predict);
	         }
	         else if(fitness.equals("RSE")) {
	        	 result = getRSE(real, predict);
	         }
	         else {
	        	 System.err.print("unknown fitness objective "+fitness);
	        	 System.exit(1);
	         }
			
			// the fitness better be KozaFitness!
//	        KozaFitness f = ((KozaFitness)ind.fitness);
//	        f.setStandardizedFitness(state, result);
	        
	        double[] fitnesses = new double[1];
	        fitnesses[0] = result;
	        if(ind.fitness == null) {
	        	ind.fitness = new MultiObjectiveFitness();
	        }
	        MultiObjectiveFitness f = (MultiObjectiveFitness) ind.fitness;
//	        f.objectives = fitnesses;
           f.objectives = new double[1];
			f.setObjectives(null, fitnesses);
//	        f.hits = hits;
	        ind.evaluated = true;
	        
	        return predict;
       }
		
		return null;
	}
	
	protected double getRMSE(double[] real, double[] predict) {
		double res = 0;
		for (int y=0;y<datanum;y++) {
			double tmp = Math.abs(real[y] - predict[y]);
			tmp = tmp*tmp;
			res += tmp;
		}
		res = Math.sqrt(res / datanum);    
        
		if(res >= Double.POSITIVE_INFINITY || Double.isNaN(res)) {
         	res = 1e6;
         }
		
		return res;
	}
	
	protected double getMSE(double[] real, double[] predict) {
		double res = 0;
		for (int y=0;y<datanum;y++) {
			double tmp = Math.abs(real[y] - predict[y]);
			tmp = tmp*tmp;
			res += tmp;
		}
		res = res / datanum;
		
		if(res >= Double.POSITIVE_INFINITY || Double.isNaN(res)) {
         	res = 1e6;
         }
		
		return res;
	}
	
	protected double getR2(double[] real, double[] predict) {
		//get variance of real
		double avg = 0;
		for(int y = 0; y<datanum; y++) {
			avg += real[y];
		}
		avg /= datanum;
		
		double var = 0;
		for(int y = 0; y<datanum; y++) {
			var += (real[y] - avg)*(real[y] - avg);
		}
		var /= datanum;
		
		double mse = getMSE(real, predict);
		
		if(mse >= Double.POSITIVE_INFINITY || Double.isNaN(mse)) {
         	mse = 1e6;
         }
		
		double res = -1*(1. - mse / var); //-1: transform maximizing R^2 problems into minimizing -R^2 problems
//		if(res > 0) res = 0;
		
		return res;
	}
	
	protected double getRSE(double[] real, double[] predict) {
		//get variance of real
		double avg = 0;
		for(int y = 0; y<datanum; y++) {
			avg += real[y];
		}
		avg /= datanum;
		
		double var = 0;
		for(int y = 0; y<datanum; y++) {
			var += (real[y] - avg)*(real[y] - avg);
		}
		var /= datanum;
		
		double mse = getMSE(real, predict);
		
		if(mse >= Double.POSITIVE_INFINITY || Double.isNaN(mse)) {
         	mse = 1e6;
         }
		
		double res = mse / var;
		
		return res;
	}
	
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
		
		norm_mean = new double [datadim];
		norm_std = new double [datadim];
		
		//get mean of training data
		for(int d = 0; d<dim; d++) {
			double mean = 0;
			for(int j = 0 ; j<num; j++) {
				mean += traindata[j][d] / num;
			}
			norm_mean[d] = mean;
		}
		
		//get std of training data
		for(int d = 0; d<dim; d++) {
			double std = 0;
			for(int j = 0; j<num; j++) {
				std += Math.pow(traindata[j][d] - norm_mean[d], 2) / num;
			}
			norm_std[d] = Math.sqrt(std);
		}
		
		//normalize data
		for(int d = 0; d<datadim; d++) {
			for(int j = 0; j<datanum; j++) {
				if(norm_std[d]>0)
					normdata[j][d] = (data[j][d] - norm_mean[d]) / (norm_std[d]);
				else {
					normdata[j][d] = 0;
				}
			}
		}
		
	}
	
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
		double mean = 0;
		for(int j = 0 ; j<num; j++) {
			mean += traindata_output[j] / num;
		}
		out_mean = mean;
		
		//get std
		double std = 0;
		for(int j = 0; j<num; j++) {
			std += Math.pow(traindata_output[j] - out_mean, 2) / num;
		}
		out_std = Math.sqrt(std);
		
	}
}
