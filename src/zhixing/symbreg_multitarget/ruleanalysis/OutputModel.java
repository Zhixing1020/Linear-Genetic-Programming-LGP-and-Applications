package zhixing.symbreg_multitarget.ruleanalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import zhixing.symbreg_multitarget.optimization.GPSymbolicRegressionMultiTarget;
import ec.Individual;
import ec.multiobjective.MultiObjectiveFitness;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;

public class OutputModel {

	protected int numRegs;
	protected int maxIterations;
	protected boolean isMultiObj;
	protected String trainPath;
	protected String dataPath;
//	protected String dataName;
//    protected RuleType ruleType;
    protected int run_index;
    protected int fold_index;
    protected String dataName;
    protected List<String> objectives = new ArrayList<>(); // The objectives to test.
    
    protected final int maxgenerations = 5500;
    
    protected ParameterDatabase parameters;
    
    protected TestResult4CpxGPSRMT result_rulelist;
    GPSymbolicRegressionMultiTarget problem;
    
    public OutputModel(String args[]) {
    	int idx = 0;
        
    	this.trainPath = args[idx];
        idx ++;
        this.dataPath = args[idx];
        idx ++;
        this.dataName = args[idx];
        idx ++;
        this.run_index = Integer.valueOf(args[idx]);
        idx ++;
        this.fold_index = Integer.valueOf(args[idx]);
        idx ++;
        		
		this.numRegs = Integer.valueOf(args[idx++]);
		this.maxIterations = Integer.valueOf(args[idx++]);

		int numObjectives = Integer.valueOf(args[idx++]);
		
		if(numObjectives > 1) {
			System.err.print("the basic rule analysis in LGP for SR does not support multi-objective\n");
			System.exit(1);
		}
		isMultiObj = numObjectives > 1;
		
		for (int i = 0; i < numObjectives; i++) {
			addObjective(args[idx++]);
		}
		
		//algorithm-related parameters
		ParameterDatabase parameters = null;
		try
        {
			parameters = new ParameterDatabase(
					new File(new File(args[idx]).getAbsolutePath()),
					args);
        }
		catch(Exception e)
        {
			e.printStackTrace();
			Output.initialError("An exception was generated upon reading the parameter file \"" + args[idx] + "\".\nHere it is:\n" + e); 
        }
		idx++;
		addParamsfile(parameters);
		
		get_output_rulelist();
    }

	public void addParamsfile(ParameterDatabase parameters) {
	//	
		this.parameters = parameters;
		
	//   state = Evolve.initialize(this.parameters, 0);
	//	
	//	state.setup(state, null);
	}
	
	public void addObjective(String objective) {
		this.objectives.add(objective);
	}
	
	protected TestResult4CpxGPSRMT get_output_rulelist() {
		
		problem = new GPSymbolicRegressionMultiTarget(dataPath, dataName, objectives.get(0), false, parameters);
		
		int numOutRegs = parameters.getInt(new Parameter("pop.subpop.0.species.ind.num-output-register"),null);
        if(numOutRegs<=0){
	       	System.err.print("the number of output registers is illegal in RuleTest");
	       	System.exit(1);
        }
        
		List<Integer> outputRegs = new ArrayList<>();
        for(int r = 0;r<numOutRegs;r++){
//	       	int or = parameters.getInt(new Parameter("pop.subpop.0.species.ind.output-register."+r),null);
//	       	if(or<0 || or>numRegs){
//	       		System.err.print("the output register index is illegal in RuleTest");
//	           	System.exit(1);
//	       	}
	       	outputRegs.add(r);
        }
	     
        File sourceFile = new File(trainPath + "job." + run_index + ".out.stat");
	     
        problem.setFoldIndex(fold_index, false);
        
        result_rulelist = TestResult4CpxGPSRMT.readFromFile4LGP(sourceFile, numRegs, maxIterations, isMultiObj, outputRegs);
        
        return result_rulelist;
	}
	
	public ArrayList<Double[]> predict(){
		//return the predicting y based on settings of the model (i.e., input arguments)  
         
         CpxGPIndividual ind = (CpxGPIndividual) result_rulelist.getGenerationalRule(result_rulelist.getGenerationalRules().size()-1);
		
        ArrayList<Double[]> res = problem.quickevaluate(ind);
         
		return res;
	}
	
	public String printProgram() {
		
		String res = "";
		
		LGPIndividual ind = (LGPIndividual) result_rulelist.getGenerationalRule(result_rulelist.getGenerationalRules().size()-1);
		
		int x = 0;
        for(GPTreeStruct tree:ind.getTreelist())
        {
    		if(tree.status) {
    			res +="Ins " + x + ":" + tree.toString() + "\n";
    			x++;
    		}
        }   
        
        return res;
	}
	
	public double getProgramComplexity() {
		
		LGPIndividual ind = (LGPIndividual) result_rulelist.getGenerationalRule(result_rulelist.getGenerationalRules().size()-1);
		
		return ind.getProgramSize();
	}
	
	public static void main(String[] args) {
		OutputModel model = new OutputModel(args);
		
		ArrayList<Double[]> res = model.predict();
		
		System.out.print(res.size());
	}
}
