package zhixing.symbolic_classification.optimization;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import yimei.jss.gp.data.DoubleData;
import zhixing.cpxInd.algorithm.semantic.individual.SLGPIndividual;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.optimization.SupervisedProblem;
import zhixing.symbolic_classification.individual.CpxGPInterface4Class;
import zhixing.symbolic_classification.individual.LGPInterface4Class;
//import zhixing.symbreg_multitarget.individual.LGPInterface4SRMT;
//import zhixing.symbreg_multitarget.optimization.GPSymbolicRegressionMultiTarget;

public class GPClassification extends GPProblem implements SimpleProblemForm,SupervisedProblem {

	public static final String PROBLEM_P = "ClassificationMTar";
	public static final String LOCATION_P = "location";
	public static final String DATA_NAME_P = "dataname";
	public static final String FITNESS_P = "fitness";
	public static final String NORMALIZE_P = "normalize";
	public static final String KFOLDINDEX_P = "Kfold_index";
	public static final String KFOLDNUM_P = "Kfold_num";
	public static final String TARGETNUM_P = "target_num";
	public static final String TARGETS_P = "targets";
	public static final String VALIDATION_P = "do-validation";
	
	public static final String CLASSNUM_P = "class_num";
	public static final String CLASS_P = "class";
//	
	protected String location;
	protected String dataname;
	protected String fitness;
	protected boolean istraining;
	protected boolean doValidation = false;
	
	protected boolean normalized = false;
	
	public int foldnum;
	public int foldindex;
	
//	protected int datanum_hub[];
	protected int datadim;
	
	protected int outputnum;
	protected int outputdim;
	
	protected int target_num;
	protected int[] targets;
	
	protected int datanum;
	
	protected static ArrayList<Double[]> data_hub []; //all data instances   [Kth fold][index][attribute]
	protected static ArrayList<Double[]> data_output_hub []; //the real output of all data instances [Kth fold][index][attribute]
	protected static ArrayList<Double[]> normdata_hub[]; //the data instances after normalization  [Kth fold][index][attribute]
	protected static ArrayList<Double[]> normoutput_hub[]; // the output after normalization [Kth fold][index][attribute]
//	public static double normdata_output[];  //the normalized output
	protected static double norm_mean_hub[][];  //[Kth fold][attribute]
	protected static double norm_std_hub[][];  //[Kth fold][attribute]
	
	protected static double out_mean_hub[][]; //the output mean and std can only obtain from training data [Kth fold][attribute]
	protected static double out_std_hub[][];
	
	protected static ArrayList<Double[]> data; //data instances in one of the K-fold
	protected static ArrayList<Double[]> data_output;  //the real output of one of the K-fold
	protected static ArrayList<Double[]> normdata; //the data instances after normalization in one of the K-fold
//	protected static ArrayList<Double[]> normoutput;
	protected static double norm_mean []; 
	protected static double norm_std [];
	protected static double out_mean []; // the output mean and std in one of the K-fold
	protected static double out_std [];
	protected static double data_max []; //the maximum value of each dimension
	protected static double data_min []; //the minimum value of each dimension
	
	protected static ArrayList<Double[]> validate_data; //validate data instances in one of the K-fold
	protected static ArrayList<Double[]> validate_data_output;  //validate real output of one of the K-fold
	protected int validatenum;
	
	public double X[];  //a data instance
	public int X_index; //the index of data instance
	
	int class_num = 2;
	double[] class_labels;
	
	public GPClassification () {
//		setGPSymbolicRegressionMultiTarget ("", "", "RMSE", true);
	}
	
	public GPClassification (String loca, String datan, String fitn, boolean istraining, ParameterDatabase parameters) {

		Parameter base = new Parameter("eval.problem");
		Parameter def = new Parameter(PROBLEM_P);
		
		this.foldindex = parameters.getIntWithDefault(base.push(KFOLDINDEX_P), def.push(KFOLDINDEX_P), 0);
		this.foldnum = parameters.getInt(base.push(KFOLDNUM_P), def.push(KFOLDNUM_P));
		if(foldnum <0) {
			System.err.print("A multi-target symbolic regression problem need to set a K-fold number.");
			System.exit(1);
		}
		
//		System.out.println("setting fold index as " + foldindex);
		
		target_num = parameters.getIntWithDefault(base.push(TARGETNUM_P), def.push(TARGETNUM_P), 1);
		if(target_num <= 0) {
//			state.output.fatal("A multi-target symbolic regression problem at least has one target.", base.push(TARGETNUM_P), def.push(TARGETNUM_P));
			System.err.print("A multi-target symbolic regression problem at least has one target.");
			System.exit(1);
		}
		targets = new int [target_num];
		for(int t = 0; t<target_num; t++) {
			Parameter b = base.push(TARGETS_P).push(""+t);
			
			 int tar = parameters.getIntWithDefault(b, null, 0);
	            if(tar < 0 ){
	            	System.err.println("ERROR:");
	                System.err.println("target index must be >= 0.");
	                System.exit(1);
	            }
	            targets[t] = tar;
		}
		
		normalized = parameters.getBoolean(base.push(NORMALIZE_P), def.push(NORMALIZE_P), false);
		
		doValidation = parameters.getBoolean(base.push(VALIDATION_P), def.push(VALIDATION_P), false);
		
		class_num = parameters.getInt(base.push(CLASSNUM_P), def.push(CLASSNUM_P));
		if(class_num < 2) {
			System.err.print("the number of classes must be >= 2");
			System.exit(1);
		}
		class_labels = new double [class_num];
		for(int c = 0; c<class_num; c++) {
			double cl = parameters.getDouble(base.push(CLASS_P).push(""+c), def.push(CLASS_P).push(""+c));
			class_labels [c] = cl;
		}
		
		setProblem (null, loca, datan, fitn, istraining);
	}
	

	protected void setProblem (EvolutionState state, String loca, String datan, String fitn, boolean istraining) {
		location = loca;
		dataname = datan;
		
		String dataname_address = "";
		
		if(location.contains("\\")) {
			if(!location.endsWith("\\"))
				location += "\\";
			
			if(!dataname.endsWith("\\")) {
				dataname_address = dataname + "\\";
			}
		}
		else if(location.contains("/")) {
			if(!location.endsWith("/"))
				location += "/";
			
			if(!dataname.endsWith("/")) {
				dataname_address = dataname + "/";
			}
		}
		
//		data_hub = new ArrayList [foldnum];
//		data_output_hub = new ArrayList [foldnum];
//		datanum_hub = new int [foldnum];
		
		String filename_X = "";
		String filename_y = "";
		
		this.istraining = istraining;
		
//		for(int k = 0; k<foldnum; k++) {
//			
//		}
		
		if(this.istraining) {
			filename_X = location + dataname_address + dataname + "_X_train_F" + foldindex + ".txt";
			filename_y = location + dataname_address + dataname + "_y_train_F" + foldindex + ".txt";
		}
		else {
			filename_X = location + dataname_address + dataname + "_X_test_F" + foldindex + ".txt";
			filename_y = location + dataname_address + dataname + "_y_test_F" + foldindex + ".txt";
		}
		
		System.out.println("evaluating on X: " + filename_X + ", Y: " + filename_y);
		
		if(! Files.exists(Paths.get( filename_X))) {
			System.err.print("the dataset " + filename_X + " does not exist\n");
			System.exit(1);
		}
		if(this.istraining && !Files.exists(Paths.get( filename_y))) {
			System.err.print("the dataset " + filename_y + " does not exist\n");
			System.exit(1);
		}
		
		//read X
		try {
            BufferedReader in = new BufferedReader(new FileReader(filename_X));
            String str = in.readLine();
            if(str != null) {
            	String [] info = str.split("\t|,");
            	datanum = Integer.valueOf(info[0]);
            	datadim = Integer.valueOf(info[1]);
            	
            	data = new ArrayList();
            	
            	data_max = new double [datadim];
            	data_min = new double [datadim];
            	for(int d = 0; d<datadim; d++) {
            		data_max [d] = -1e7;
            		data_min [d] = 1e7;
            	}
            }
            
            for(int d = 0; d<datanum; d++) {
            	if((str = in.readLine()) != null) {
            		String[] info = str.split("\t");
            		
            		Double [] instance = new Double [datadim];
            		
                    for(int i = 0; i<datadim; i++) {
                    	instance[i] = Double.valueOf(info[i]);
                    	
                    	if(instance[i] > data_max[i]) {
                    		data_max[i] = instance[i];
                    	}
                    	if(instance[i] < data_min[i]) {
                    		data_min[i] = instance[i];
                    	}
                    }
                    
                    data.add(instance);
            	}
            }
            
        } catch (IOException e) {
        }
		
		//read y
		try {
            BufferedReader in = new BufferedReader(new FileReader(filename_y));
            String str = in.readLine();
            if(str != null) {
            	String [] info = str.split("\t|,");
            	outputnum = Integer.valueOf(info[0]);
            	outputdim = Integer.valueOf(info[1]);
            	
            	data_output = new ArrayList();
            }
            
            for(int d = 0; d<outputnum; d++) {
            	if((str = in.readLine()) != null) {
            		String[] info = str.split("\t");
            		
            		Double [] instance = new Double [outputdim];
            		
                    for(int i = 0; i<outputdim; i++) {
                    	instance[i] = Double.valueOf(info[i]);
                    }
                    
                    data_output.add(instance);
            	}
            }
            
        } catch (IOException e) {
        }
		
		fitness = fitn;
		
		if(!(fitness.equals("ACC") || fitness.equals("ERR") || fitness.equals("RSE")|| fitness.equals("Fisher")|| fitness.equals("CONF"))) {
			System.err.print(fitness + " must be one of the following objectives: ACC, ERR, RSE, Fisher, CONF");
			System.exit(1);
		}
		
		//normalize data and their output
		if(normalized)
			normalizedataBasedTraining();
		
		if(this.istraining && state != null && doValidation) {
			validate_data = new ArrayList<>();
			validate_data_output = new ArrayList<>();
			
			validatenum = (int) Math.ceil(0.1*datanum);
			
			ArrayList<Double[]> usingdata;
			if(normalized) {
				usingdata = normdata;
			}
			else {
				usingdata = data;
			}
			
			for(int v = 0; v<validatenum; v++) {
				int i = state.random[0].nextInt(usingdata.size());
				
				validate_data.add(usingdata.get(i));
				validate_data_output.add(data_output.get(i));
				
				usingdata.remove(i);
				data_output.remove(i);
			}
			
			datanum = usingdata.size();
			outputnum = data_output.size();
		}
	}
	
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
		if(location.equals("")) {
			state.output.fatal("we got empty location for the data",base.push(LOCATION_P),def.push(LOCATION_P));
		}
		
		//read the dataset name
		dataname = state.parameters.getString(base.push(DATA_NAME_P),def.push(DATA_NAME_P));
		if(dataname.equals("")) {
			state.output.fatal("we got empty name for the data",base.push(DATA_NAME_P),def.push(DATA_NAME_P));
		}
		
		fitness = state.parameters.getString(base.push(FITNESS_P),def.push(FITNESS_P));
		
		normalized = state.parameters.getBoolean(base.push(NORMALIZE_P), def.push(NORMALIZE_P), false);
		
		doValidation = state.parameters.getBoolean(base.push(VALIDATION_P), def.push(VALIDATION_P), false);
		
		foldindex = state.parameters.getIntWithDefault(base.push(KFOLDINDEX_P), def.push(KFOLDINDEX_P), 0);
		this.foldnum = state.parameters.getInt(base.push(KFOLDNUM_P), def.push(KFOLDNUM_P));
		if(foldnum <0) {
			System.err.print("A multi-target symbolic regression problem need to set a K-fold number.");
			System.exit(1);
		}
		
//		System.out.println("setting fold index as " + foldindex);
		
		target_num = state.parameters.getIntWithDefault(base.push(TARGETNUM_P), def.push(TARGETNUM_P), 1);
		if(target_num <= 0) {
			state.output.fatal("A multi-target symbolic regression problem at least has one target.", base.push(TARGETNUM_P), def.push(TARGETNUM_P));
		}
		targets = new int [target_num];
		for(int t = 0; t<target_num; t++) {
			Parameter b = base.push(TARGETS_P).push(""+t);
			
			 int tar = state.parameters.getIntWithDefault(b, null, 0);
	            if(tar < 0 ){
	            	System.err.println("ERROR:");
	                System.err.println("target index must be >= 0.");
	                System.exit(1);
	            }
	            targets[t] = tar;
		}
		
		class_num = state.parameters.getInt(base.push(CLASSNUM_P), def.push(CLASSNUM_P));
		if(class_num < 2) {
			System.err.print("the number of classes must be >= 2");
			System.exit(1);
		}
		class_labels = new double [class_num];
		for(int c = 0; c<class_num; c++) {
			double cl = state.parameters.getDouble(base.push(CLASS_P).push(""+c), def.push(CLASS_P).push(""+c));
			class_labels [c] = cl;
		}
		
		this.setProblem(state, location, dataname, fitness, true);
		
	}
	
	public int getDatanum() { return datanum;}
	public int getDatadim() { return datadim;}
	public int getOutputnum() { return outputnum;}
	public int getOutputdim() { return outputdim;}
	public int[] getTargets() { return targets; }
	public int getTargetNum() { return target_num;}
	public double[] getDataMax() { return data_max;}
	public double[] getDataMin() { return data_min;}
	public ArrayList<Double[]> getData() { 
		if(normalized) 
			return normdata;
		else
			return data;
	} 
	public ArrayList<Double[]> getDataOutput() { 
//		if(normalized)
//			return normoutput;
//		else
//			return data_output;
		return data_output;
	}
	public double [] getX() {
		return X;
	}
	public int getX_index()	{
		return X_index;
	}
	public void setX_index(int ind)	{
		X_index = ind;
	}
	public boolean isnormalized() {
		return normalized;
	}
	public boolean istraining() {
		return istraining;
	}
	public void setFoldIndex(int ind, boolean istraining) { 
		this.foldindex = ind;
		
//		System.out.println("setting fold index as " + foldindex);
		
		this.setProblem(null, location, dataname, fitness, istraining);
	}
	public int getFoldNum() {
		return this.foldnum;
	}
	
	public int getClassNum() {
		return class_num;
	}
	public double [] getClassLabels() {
		return class_labels;
	}
	
	
	public double getClassLabelFromOutput(Double [] res) {
		//res is the outputs of the LGP program of one instance 
		if(res.length < class_num) {
			System.err.print("the number of outputs is smaller the number of classes\n");
			System.exit(1);
		}
		
		//softmax
		double sum = 0;
		double [] tmpres = new double [res.length];
		for(int r = 0; r < class_num; r++) {
			tmpres[r] = Math.min(Math.exp(res[r]), 10);
			sum += tmpres[r];
		}
		for(int r = 0; r<class_num; r++) {
			tmpres[r] /= (sum + 1e-7);
		}
		
		//output the class label corresponding to the index of the largest value in res
		double val = -1e9;
		int index = 0;
		for(int i = 0;i<class_num;i++) {
			if(tmpres[i] > val) {
				index = i;
				val = tmpres[i];
			}
		}
		Double label = class_labels[index];
		return label;
	}
	
//	public int getDatanum() { return datanum;}
//	public int getDatadim() { return datadim;}
//	public int getOutputnum() { return outputnum;}
//	public int getOutputdim() { return outputdim;}
//	public int[] getTargets() { return targets; }
//	public int getTargetNum() { return target_num;}
//	public double[] getDataMax() { return data_max;}
//	public double[] getDataMin() { return data_min;}
//	public ArrayList<Double[]> getData() { 
//		if(normalized) 
//			return normdata;
//		else
//			return data;
//	} 
//	public ArrayList<Double[]> getDataOutput() { 
////		if(normalized)
////			return normoutput;
////		else
////			return data_output;
//		return data_output;
//	}
//	public boolean isnormalized() {
//		return normalized;
//	}
//	public boolean istraining() {
//		return istraining;
//	}
//	public void setFoldIndex(int ind, boolean istraining) { 
//		this.foldindex = ind;
//		
////		System.out.println("setting fold index as " + foldindex);
//		
//		this.setGPClassification(null, location, dataname, fitness, istraining);
//	}
//	public int getFoldNum() {
//		return this.foldnum;
//	}
//	public ArrayList<Double[]> getTargetOutput() {
//		ArrayList<Double[]> res_target = new ArrayList<>();
//		
//		for(int i = 0; i < datanum; i++) {
//			Double [] tar = new Double [target_num];
//			
//			for(int t = 0; t<target_num; t++) {
//				tar[t] = data_output.get(i)[targets[t]];
//			}
//			
//			res_target.add(tar);
//		}
//		
//		return res_target;
//	}
//	public double[] getOutMean() { return out_mean;}
//	public double[]	getOutStd() { return out_std;}
	
//	public void prepareEvaluation(int k) {
//		data = data_hub[k];
//		data_output = data_output_hub[k];
//		normdata = normdata_hub[k];
//		normoutput = normoutput_hub[k];
//		norm_mean = norm_mean_hub[k];
//		norm_std = norm_std_hub[k];
//		out_mean = out_mean_hub[k];
//		out_std = out_std_hub[k];
//		
//		if(data.size() == datanum_hub[k]) {
//			datanum = datanum_hub[k];
//		}
//		else {
//			System.err.print("inconsistent number of instances!\n");
//			System.exit(1);
//		}
//		
//		
//	}
	
	@Override
	public void evaluate(final EvolutionState state, 
	        final Individual ind, 
	        final int subpopulation,
	        final int threadnum) {
		
		 if (!ind.evaluated)  // don't bother reevaluating
         {
			 if(data.size()==0 || data_output.size()==0) {
				 System.err.print("we have an empty data source\n");
				 System.exit(1);
			 }
			 
//			 DoubleData input = (DoubleData)(this.input);
		     
	         double hits = 0;
	         double sum = 0.0;
	         double expectedResult;
	         double result = 0;
	         
	         //get the performance of each fold, and then average them
//			 for(int k = 0; k<foldnum; k++) {
//				 prepareEvaluation(k);
//				 
//				 
//				 
//			 }
			 
			 ArrayList<Double []> real = data_output;
			 ArrayList<Double []> predict = new ArrayList<>();
			 
			 for(int y = 0; y<datanum; y++) {
				 DoubleData tmp = new DoubleData();
				 
				 X = new double[datadim];
				 X_index = y;
	             for(int d = 0; d<datadim; d++) {
	            	 if(normalized)
	            		 X[d] = normdata.get(y)[d];
	            	 else
	            		 X[d] = data.get(y)[d];
	             }
	             
	             if(ind instanceof SLGPIndividual) {
		            	((SLGPIndividual)ind).setDataIndex(y); 
		         }
	             
	             predict.add( ((CpxGPInterface4Class)ind).execute_outs(state, threadnum, tmp, stack, (GPIndividual) ind, this) );
			 }
			 
			 //scaling outputs based on the mean and std.
//			 for(int y = 0; y<datanum; y++) {
//				 for(int od = 0; od<target_num; od++) {
//					 
//					 int di = targets[od];
//					 
//					 if(normalized) predict.get(y)[od] = predict.get(y)[od]*out_std[di] + out_mean[di];
//					 
//					 if(predict.get(y)[od] >= Double.POSITIVE_INFINITY || Double.isNaN(predict.get(y)[od])) {
//			             	predict.get(y)[od] = 1e6;
//			         }
//				 }
//			 }
			 
			 //check the hits
//			 for(int y = 0; y<datanum; y++) {
//				 double hit_tmp = 0;
//				 for(int od = 0; od < target_num; od++) {
//					 
//					 int di = targets[od];
//					 
//					 double hit_res = Math.abs(real.get(y)[di] - predict.get(y)[od]);
//
//		             if (hit_res <= 0.01) hit_tmp++;        
//				 }
//				 
//				 hit_tmp /= target_num;
//				 hits += hit_tmp;
//			 }
			 
			 
			 //get the fitness of this fold
			 if(ind instanceof CpxGPIndividual) {
				 if(((CpxGPIndividual)ind).IsWrap() ) {
					 //select the really care targets into real_care;
					 ArrayList<Double[]> real_care = new ArrayList<>();
					 
					 for(int i = 0; i<real.size(); i++) {
						 Double [] tmp = new Double [target_num];
						 for(int od = 0; od < target_num; od++) {
							 tmp[od] = real.get(i)[targets[od]];
						 }
						 real_care.add(tmp);
					 }
					 
					predict = ((CpxGPIndividual)ind).wrapper(predict, real_care, state, threadnum, this);
//					normwrap = ((CpxGPIndividual)ind).getWeightNorm();
				 }
			 }
			 
			 result = 0;
			 for(int od = 0; od < target_num; od++) {
				 double result_tmp = 0;
				 double [] real_d = new double [datanum];
				 double [] predict_d = new double [datanum];
				 double [][] predict_confidence = new double [datanum][predict.get(0).length];
				 
				 for(int y = 0; y<datanum; y++) {
					 
					 int di = targets[od];
					 
					 real_d[y] = real.get(y)[di];
					 predict_d[y] = getClassLabelFromOutput( predict.get(y));
					 for(int i = 0; i<predict.get(y).length; i++) {
						 predict_confidence[y][i] = predict.get(y)[i];
					 }
				 }
				 
//				 if(ind instanceof CpxGPIndividual) {
//					 if(((CpxGPIndividual)ind).IsWrap() ) {
//						predict_d = ((CpxGPIndividual)ind).wrapper(state, threadnum, input, stack, null, predict_d, real_d);
//					 }
//				 }
				 
//				 if(fitness.equals("RMSE")) {
//		        	 result_tmp = getRMSE(real_d, predict_d);
//		         }
//		         else if (fitness.equals("MSE")) {
//		        	 result_tmp = getMSE(real_d, predict_d);
//		         }
//		         else if (fitness.equals("R2")) {
//		        	 result_tmp = getR2(real_d, predict_d);
//		         }
//		         else if(fitness.equals("RSE")) {
//		        	 result_tmp = getRSE(real_d, predict_d);
//		         }
//		         else 
				 if(fitness.equals("ACC")) {
		        	 result_tmp = getAccuracy(real_d, predict_d);
		         }
				 else if(fitness.equals("ERR")) {
		        	 result_tmp = getError(real_d, predict_d);
		         }
				 else if(fitness.equals("RSE")) {
					 result_tmp = getRSE(real_d, predict_d);
				 }
				 else if(fitness.equals("CONF")) {
					 result_tmp = getConfidence(real_d, predict_confidence);
				 }
//				 else if(fitness.equals("Fisher")) {
//					 result_tmp = getFisher(real_d, predict_d);
//				 }
		         else {
		        	 System.err.print("unknown fitness objective "+fitness);
		        	 System.exit(1);
		         }
				 
				 result += result_tmp / target_num;
			 }
			 
			 
			 double validate_res = validationevaluation(state, ind, subpopulation, threadnum);
			 
			// the fitness better be KozaFitness!
//	        KozaFitness f = ((KozaFitness)ind.fitness);
//	        f.setStandardizedFitness(state, result);
	        
	        double[] fitnesses = new double[1];
	        fitnesses[0] = result + 0.1*validate_res;
	        MultiObjectiveFitness f = (MultiObjectiveFitness) ind.fitness;
			f.setObjectives(state, fitnesses);
//	        f.hits = hits;			
	        ind.evaluated = true;
        }
	}
	
	public double validationevaluation(final EvolutionState state, 
	        final Individual ind, 
	        final int subpopulation,
	        final int threadnum) {
		if(!this.doValidation) return 0.;
		
		ArrayList<Double []> real = validate_data_output;
		 ArrayList<Double []> predict = new ArrayList<>();
		 
		 for(int y = 0; y<validatenum; y++) {
			 DoubleData tmp = new DoubleData();
			 
			 X = new double[datadim];
			 X_index = y;
            for(int d = 0; d<datadim; d++) {
//           	 if(normalized)
//           		 X[d] = normdata.get(y)[d];
//           	 else
//           		 X[d] = data.get(y)[d];
           	 X[d] = validate_data.get(y)[d];
            }
            
//            if(ind instanceof SLGPIndividual) {
//	            	((SLGPIndividual)ind).setDataIndex(y); 
//	         }
            
            predict.add( ((LGPInterface4Class)ind).execute_outs_wrap(state, threadnum, tmp, stack, (GPIndividual) ind, this) );
		 }
		 
		 //scaling outputs based on the mean and std.
//		 for(int y = 0; y<validatenum; y++) {
//			 for(int od = 0; od<target_num; od++) {
//				 
//				 int di = targets[od];
//				 
//				 if(normalized) predict.get(y)[od] = predict.get(y)[od]*out_std[di] + out_mean[di];
//				 
//				 if(predict.get(y)[od] >= Double.POSITIVE_INFINITY || Double.isNaN(predict.get(y)[od])) {
//		             	predict.get(y)[od] = 1e6;
//		         }
//			 }
//		 }
		 
		 double result = 0;
		 for(int od = 0; od < target_num; od++) {
			 double result_tmp = 0;
			 double [] real_d = new double [validatenum];
			 double [] predict_d = new double [validatenum];
			 double [][] predict_confidence = new double [datanum][predict.get(0).length];
			 
			 for(int y = 0; y<validatenum; y++) {
				 
				 int di = targets[od];
				 
				 real_d[y] = real.get(y)[di];
				 predict_d[y] = getClassLabelFromOutput( predict.get(y));
				 for(int i = 0; i<predict.get(y).length; i++) {
					 predict_confidence[y][i] = predict.get(y)[i];
				 }
			 }
			 
//			 if(ind instanceof CpxGPIndividual) {
//				 if(((CpxGPIndividual)ind).IsWrap() ) {
//					predict_d = ((CpxGPIndividual)ind).wrapper(state, threadnum, input, stack, null, predict_d, real_d);
//				 }
//			 }
			 
//			 if(fitness.equals("RMSE")) {
//	        	 result_tmp = getRMSE(real_d, predict_d);
//	         }
//	         else if (fitness.equals("MSE")) {
//	        	 result_tmp = getMSE(real_d, predict_d);
//	         }
//	         else if (fitness.equals("R2")) {
//	        	 result_tmp = getR2(real_d, predict_d);
//	         }
//	         else if(fitness.equals("RSE")) {
//	        	 result_tmp = getRSE(real_d, predict_d);
//	         }
//	         else 
	         if(fitness.equals("ACC")) {
	        	 result_tmp = getAccuracy(real_d, predict_d);
	         }
	         else if(fitness.equals("ERR")) {
	        	 result_tmp = getError(real_d, predict_d);
	         }
	         else if(fitness.equals("RSE")) {
	        	 result_tmp = getRSE(real_d, predict_d);
	         }
	         else if(fitness.equals("CONF")) {
				 result_tmp = getConfidence(real_d, predict_confidence);
			 }
//	         else if(fitness.equals("Fisher")) {
//				 result_tmp = getFisher(real_d, predict_d);
//			 }
	         else {
	        	 System.err.print("unknown fitness objective "+fitness);
	        	 System.exit(1);
	         }
			 
			 result += result_tmp / target_num;
		 }
		 
		 return result;
	}
	
	public void simpleevaluate(final Individual ind) {
		
		if (!ind.evaluated)  // don't bother reevaluating
        {
			 if(data.size()==0 || data_output.size()==0) {
				 System.err.print("we have an empty data source\n");
				 System.exit(1);
			 }
			 
			 DoubleData input = (DoubleData)(this.input);
		     
	         double hits = 0;
	         double sum = 0.0;
	         double expectedResult;
	         double result = 0;
	         
	         //get the performance of each fold, and then average them
//			 for(int k = 0; k<foldnum; k++) {
//				 prepareEvaluation(k);
//				 
//				 
//				 
//			 }
			 
			 ArrayList<Double []> real = data_output;
			 ArrayList<Double []> predict = new ArrayList<>();
			 
			 for(int y = 0; y<datanum; y++) {
				 DoubleData tmp = new DoubleData();
				 
				 X = new double[datadim];
				 X_index = y;
	             for(int d = 0; d<datadim; d++) {
	            	 if(normalized)
	            		 X[d] = normdata.get(y)[d];
	            	 else
	            		 X[d] = data.get(y)[d];
	             }
	             
	             if(ind instanceof SLGPIndividual) {
		            	((SLGPIndividual)ind).setDataIndex(y); 
		         }
	             
	             predict.add( ((CpxGPInterface4Class)ind).execute_outs(null, 0, tmp, stack, (GPIndividual) ind, this) );
			 }
			 
			 //scaling outputs based on the mean and std.
//			 for(int y = 0; y<datanum; y++) {
//				 for(int od = 0; od<target_num; od++) {
//					 
//					 int di = targets[od];
//					 
//					 if(normalized) predict.get(y)[od] = predict.get(y)[od]*out_std[di] + out_mean[di];
//					 
//					 if(predict.get(y)[od] >= Double.POSITIVE_INFINITY || Double.isNaN(predict.get(y)[od])) {
//			             	predict.get(y)[od] = 1e6;
//			         }
//				 }
//			 }

			 
			 //check the hits
//			 for(int y = 0; y<datanum; y++) {
//				 double hit_tmp = 0;
//				 for(int od = 0; od < target_num; od++) {
//					 
//					 int di = targets[od];
//					 
//					 double hit_res = Math.abs(real.get(y)[di] - predict.get(y)[od]);
//
//		             if (hit_res <= 0.01) hit_tmp++;        
//				 }
//				 
//				 hit_tmp /= target_num;
//				 hits += hit_tmp;
//			 }
			 
			 
			 //get the fitness of this fold
			 result = 0;
			 for(int od = 0; od < target_num; od++) {
				 double result_tmp = 0;
				 double [] real_d = new double [datanum];
				 double [] predict_d = new double [datanum];
				 double [][] predict_confidence = new double [datanum][predict.get(0).length];
				 
				 for(int y = 0; y<datanum; y++) {
					 
					 int di = targets[od];
					 
					 real_d[y] = real.get(y)[di];
					 predict_d[y] = getClassLabelFromOutput( predict.get(y) );
					 for(int i = 0; i<predict.get(y).length; i++) {
						 predict_confidence[y][i] = predict.get(y)[i];
					 }
				 }
				 
//				 if(fitness.equals("RMSE")) {
//		        	 result_tmp = getRMSE(real_d, predict_d);
//		         }
//		         else if (fitness.equals("MSE")) {
//		        	 result_tmp = getMSE(real_d, predict_d);
//		         }
//		         else if (fitness.equals("R2")) {
//		        	 result_tmp = getR2(real_d, predict_d);
//		         }
//		         else if(fitness.equals("RSE")) {
//		        	 result_tmp = getRSE(real_d, predict_d);
//		         }
//		         else 
		         if(fitness.equals("ACC")) {
		        	 result_tmp = getAccuracy(real_d, predict_d);
		         }
		         else if(fitness.equals("ERR")) {
		        	 result_tmp = getError(real_d, predict_d);
		         }
		         else if(fitness.equals("RSE")) {
		        	 result_tmp = getRSE(real_d, predict_d);
		         }
		         else if(fitness.equals("CONF")) {
					 result_tmp = getConfidence(real_d, predict_confidence);
				 }
//		         else if(fitness.equals("Fisher")) {
//					 result_tmp = getFisher(real_d, predict_d);
//				 }
		         else {
		        	 System.err.print("unknown fitness objective "+fitness);
		        	 System.exit(1);
		         }
				 
				 result += result_tmp / target_num;
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
	        f.objectives = new double[1];
			f.setObjectives(null, fitnesses);
//	        f.hits = hits;
	        ind.evaluated = true;
       }
	}
	
	public ArrayList<Double[]> quickevaluate(final Individual ind){
		if(data.size()==0) {
			 System.err.print("we have an empty data source\n");
			 System.exit(1);
		 }
		 
		 DoubleData input = (DoubleData)(this.input);
	     
        double hits = 0;
        double sum = 0.0;
        double expectedResult;
        double result = 0;
        
		 ArrayList<Double []> predict = new ArrayList<>();
		 
		 for(int y = 0; y<datanum; y++) {
			 DoubleData tmp = new DoubleData();
			 
			 X = new double[datadim];
			 X_index = y;
            for(int d = 0; d<datadim; d++) {
           	 if(normalized)
           		 X[d] = normdata.get(y)[d];
           	 else
           		 X[d] = data.get(y)[d];
            }
            
            if(ind instanceof SLGPIndividual) {
	            	((SLGPIndividual)ind).setDataIndex(y); 
	         }
            
            predict.add( ((LGPInterface4Class)ind).execute_outs_wrap(null, 0, tmp, stack, (GPIndividual) ind, this) );
		 }
		 
		 ArrayList<Double []> res = new ArrayList<>();
		 
		 //scaling outputs based on the mean and std.
		 for(int y = 0; y<datanum; y++) {
			 
			 double label = getClassLabelFromOutput( predict.get(y) );
			 Double [] ra = new Double [1];
			 ra[0] = label;
			 res.add(ra);
			 
//			 Double [] tmp = new Double [target_num];
//			 for(int od = 0; od<target_num; od++) {
//				 
//				 int di = targets[od];
//				 
//				 if(normalized) predict.get(y)[od] = predict.get(y)[od]*out_std[di] + out_mean[di];
//				 
//				 if(predict.get(y)[od] >= Double.POSITIVE_INFINITY || Double.isNaN(predict.get(y)[od])) {
//		             	predict.get(y)[od] = 1e6;
//		         }
//				 
//				 tmp[od] = predict.get(y)[od];
//			 }
//			 res.add(tmp);
		 }
         return res;
	}
	
//	protected double getRMSE(double[] real, double[] predict) {
//		double res = 0;
//		for (int y=0;y<real.length;y++) {
//			double tmp = Math.abs(real[y] - predict[y]);
//			tmp = tmp*tmp;
//			res += tmp;
//		}
//		res = Math.sqrt(res / real.length);    
//        
//		if(res >= Double.POSITIVE_INFINITY || Double.isNaN(res)) {
//         	res = 1e6;
//         }
//		
//		return res;
//	}
	
//	protected double getMSE(double[] real, double[] predict) {
//		double res = 0;
//		for (int y=0;y<real.length;y++) {
//			double tmp = Math.abs(real[y] - predict[y]);
//			tmp = tmp*tmp;
//			res += tmp;
//		}
//		res = res / real.length;
//		
//		if(res >= Double.POSITIVE_INFINITY || Double.isNaN(res)) {
//         	res = 1e6;
//         }
//		
//		return res;
//	}
//	
//	protected double getR2(double[] real, double[] predict) {
//		//get variance of real
//		double avg = 0;
//		for(int y = 0; y<real.length; y++) {
//			avg += real[y];
//		}
//		avg /= real.length;
//		
//		double var = 0;
//		for(int y = 0; y<real.length; y++) {
//			var += (real[y] - avg)*(real[y] - avg);
//		}
//		var /= real.length;
//		
//		double mse = getMSE(real, predict);
//		
//		if(mse >= Double.POSITIVE_INFINITY || Double.isNaN(mse)) {
//         	mse = 1e6;
//         }
//		
//		double res = -1*(1. - mse / var); //-1: transform maximizing R^2 problems into minimizing -R^2 problems
////		if(res > 0) res = 0;
//		
//		return res;
//	}
//	
	protected double getRSE(double[] real, double[] predict) {
		//get variance of real
		double avg = 0;
		for(int y = 0; y<real.length; y++) {
			avg += real[y];
		}
		avg /= real.length;
		
		double var = 0;
		for(int y = 0; y<real.length; y++) {
			var += (real[y] - avg)*(real[y] - avg);
		}
		var /= real.length;
		
		double mse = getMSE(real, predict);
		
		if(mse >= Double.POSITIVE_INFINITY || Double.isNaN(mse)) {
         	mse = 1e6;
         }
		
		double res = mse / var;
		
		return res;
	}
	
	protected double getMSE(double[] real, double[] predict) {
		double res = 0;
		for (int y=0;y<real.length;y++) {
			double tmp = Math.abs(real[y] - predict[y]);
			tmp = tmp*tmp;
			res += tmp;
		}
		res = res / real.length;
		
		if(res >= Double.POSITIVE_INFINITY || Double.isNaN(res)) {
         	res = 1e6;
         }
		
		return res;
	}
	
	protected double getAccuracy(double[] real, double[] predict) {
		
		double hit = 0;
		
		for(int i = 0; i<real.length; i++) {
			if(real[i] == predict[i]) {
				hit ++;
			}
			
		}
		double res = hit / real.length;
		return res;
	}
	
	protected double getError(double[] real, double[] predict) {
		return 1. - getAccuracy(real, predict);
//		double [] misclass = new double [class_num];
//		for(int i = 0; i<real.length; i++) {
//			if(real[i] != predict[i]) {
//				int classi = 0;
//				for(; classi < class_num; classi++) {
//					if(class_labels[classi] == real[i]) {
//						break;
//					}
//				}
//				
//				misclass[classi] ++;
//			}
//		}
//		
//		double res = 0;
//		for(int c = 0; c<class_num; c++) {
//			res += misclass[c]*misclass[c];
//		}
//		res = Math.sqrt(res) / real.length;
//		return res;
	}
	
	protected double getConfidence(double[] real, double[][] predict_conf) {
		//maximize the gap between the true-class output and false-class outputs
		//the cross-entropy is maximizing the probability of the true-class
		
		double res = 0;
		
		double [] class_cnt = new double [class_num];
		int [] class_index = new int [real.length]; 
		double [] sum = new double [real.length];
		for(int i = 0; i<real.length; i++) {
			int classi = 0;
			for(; classi < class_num; classi++) {
				if(class_labels[classi] == real[i]) {
					break;
				}
			}
			class_cnt[classi] ++;
			class_index[i] = classi;
			for(int c = 0; c<class_num; c++) {
				sum[i] += Math.exp( Math.min(predict_conf[i][c], 10));
			}
			sum[i] += 1e-7;
		}
		
		for(int i = 0; i<real.length; i++) {
			//identify the true-class output
			double r = real[i];
			int classi = class_index[i];
//			for(; classi < class_num; classi++) {
//				if(class_labels[classi] == real[i]) {
//					break;
//				}
//			}
			
			for(int c = 0; c<class_num; c++) {
				if(c == classi) continue;
				
				res += (Math.exp( Math.min(predict_conf[i][c], 10)) - Math.exp( Math.min(predict_conf[i][classi], 10))) / (class_cnt[classi] * sum[i]);
			}
//			if(res < -5) {
//				int aaa = 1;
//			}
		}
		
		res /= ((class_num-1)*(class_num-1));

		return res;
	}
	
	protected void normalizedataBasedTraining() {
		if(data == null) {
			return;
		}
		
		String dataname_address = "";
		
		if(location.contains("\\")) {
			if(!location.endsWith("\\"))
				location += "\\";
			
			if(!dataname.endsWith("\\")) {
				dataname_address = dataname + "\\";
			}
		}
		else if(location.contains("/")) {
			if(!location.endsWith("/"))
				location += "/";
			
			if(!dataname.endsWith("/")) {
				dataname_address = dataname + "/";
			}
		}

		//can only read the training data
		String filename_X = location + dataname_address + dataname + "_X_train_F" + foldindex + ".txt";
		String filename_y = location + dataname_address + dataname + "_y_train_F" + foldindex + ".txt";
		
		ArrayList<Double[]> traindata = new ArrayList<>();
		ArrayList<Double[]> traindata_output = new ArrayList();
		
		int num = 0, dim = 0;
		
		//read X
		try {
            BufferedReader in = new BufferedReader(new FileReader(filename_X));
            String str = in.readLine();
            if(str != null) {
            	String [] info = str.split("\t|,");
            	num = Integer.valueOf(info[0]);
            	dim = Integer.valueOf(info[1]);
            }
            
            for(int d = 0; d<num; d++) {
            	if((str = in.readLine()) != null) {
            		String[] info = str.split("\t");
            		
            		Double [] instance = new Double [dim];
            		
                    for(int i = 0; i<dim; i++) {
                    	instance[i] = Double.valueOf(info[i]);
                    }
                    
                    traindata.add(instance);
            	}
            }
            
        } catch (IOException e) {
        }
		
		//read y
		try {
            BufferedReader in = new BufferedReader(new FileReader(filename_y));
            String str = in.readLine();
            if(str != null) {
            	String [] info = str.split("\t|,");
            	num = Integer.valueOf(info[0]);
            	dim = Integer.valueOf(info[1]);

            }
            
            for(int d = 0; d<num; d++) {
            	if((str = in.readLine()) != null) {
            		String[] info = str.split("\t");
            		
            		Double [] instance = new Double [dim];
            		
                    for(int i = 0; i<dim; i++) {
                    	instance[i] = Double.valueOf(info[i]);
                    }
                    
                    traindata_output.add(instance);
            	}
            }
            
        } catch (IOException e) {
        }
		
		num = traindata.size(); dim = traindata.get(0).length;
		norm_mean = new double [dim];
		norm_std = new double [dim];
		normdata = new ArrayList<>();
		//get mean of training data
		for(int d = 0; d<dim; d++) {
			double mean = 0;
			for(int j = 0 ; j<num; j++) {
				mean += traindata.get(j)[d] / num;
			}
			norm_mean[d] = mean;
		}
		
		//get std of training data
		for(int d = 0; d<dim; d++) {
			double std = 0;
			for(int j = 0; j<num; j++) {
				std += Math.pow(traindata.get(j)[d] - norm_mean[d], 2) / num;
			}
			norm_std[d] = Math.sqrt(std);
		}
		
		//normalize data
		for(int j = 0; j<data.size(); j++) {
			Double [] tmp = new Double [dim];
			
			for(int d = 0; d<dim; d++) {
				if(norm_std[d]>0)
					tmp[d] = (data.get(j)[d] - norm_mean[d]) / (norm_std[d]);
				else {
					tmp[d] = 0.;
				}
			}
			
			normdata.add(tmp);
		}
		
		
		num = traindata_output.size(); dim = traindata_output.get(0).length;
		out_mean = new double [dim];
		out_std = new double [dim];
//		normoutput = new ArrayList<>();
		//get mean of training data
		for(int d = 0; d<dim; d++) {
			double mean = 0;
			for(int j = 0 ; j<num; j++) {
				mean += traindata_output.get(j)[d] / num;
			}
			out_mean[d] = mean;
		}
		
		//get std of training data
		for(int d = 0; d<dim; d++) {
			double std = 0;
			for(int j = 0; j<num; j++) {
				std += Math.pow(traindata_output.get(j)[d] - out_mean[d], 2) / num;
			}
			out_std[d] = Math.sqrt(std);
		}
		
		//normalize data
//		for(int j = 0; j<data_output.size(); j++) {
//			Double [] tmp = new Double [dim];
//			
//			for(int d = 0; d<dim; d++) {
//				if(out_std[d]>0)
//					tmp[d] = (data_output.get(j)[d] - out_mean[d]) / (out_std[d]);
//				else {
//					tmp[d] = 0.;
//				}
//			}
//			
//			normoutput.add(tmp);
//		}
		
	}
	
	private double getEuclidean(Double [] a, Double [] b) {
		if(a.length != b.length){
			System.err.print("the getEuclidean function in CPClassification got two inconsistent arraies\n");
			System.exit(1);
		}
		double res = 0;
		for(int i = 0; i<a.length; i++) {
			res += (a[i] - b[i])*(a[i] - b[i]);
		}
		res = Math.sqrt(res / a.length);
		return res;
	}
}
