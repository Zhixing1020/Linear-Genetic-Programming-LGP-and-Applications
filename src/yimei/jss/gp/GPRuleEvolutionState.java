package yimei.jss.gp;

import java.io.*;
import java.util.*;

import org.spiderland.Psh.intStack;

//import com.sun.xml.internal.ws.policy.spi.PolicyAssertionValidator.Fitness;

import ec.Individual;
import yimei.jss.gp.terminal.AttributeGPNode;
import yimei.jss.gp.terminal.DoubleERC;
import yimei.jss.gp.terminal.JobShopAttribute;
import yimei.jss.ruleevaluation.AbstractEvaluationModel;
import ec.EvolutionState;
import ec.gp.GPNode;
import ec.simple.SimpleEvolutionState;
import ec.simple.SimpleStatistics;
import ec.util.Checkpoint;
import ec.util.Parameter;
import yimei.jss.ruleoptimisation.RuleOptimizationProblem;
import zhixing.cpxInd.algorithm.Grammar.individual.LGPIndividual4Grammar;
import zhixing.cpxInd.individual.primitive.ReadRegisterGPNode;
import zhixing.djss.individual.LGPIndividual4DJSS;
import zhixing.djss.individualevaluation.SimpleEvaluationModel4Ind;
import zhixing.djss.individualoptimization.IndividualOptimizationProblem;

/**
 * The evolution state of evolving dispatching rules with GP.
 *
 * @author yimei
 *
 */

public class GPRuleEvolutionState extends SimpleEvolutionState {

	/**
	 * Read the file to specify the terminals.
	 */
	public final static String P_TERMINALS_FROM = "terminals-from";
	public final static String P_INCLUDE_ERC = "include-erc";
	
	protected String terminalFrom;
	protected boolean includeErc;
	protected long jobSeed;
	protected List<GPNode> terminals;

	List<Double> genTimes = new ArrayList<>();

	public List<GPNode> getTerminals() {
		return terminals;
	}

	public long getJobSeed() {
		return jobSeed;
	}

	public void setTerminals(List<GPNode> terminals) {
		this.terminals = terminals;
	}

	/**
	 * Initialize the terminal set with all the job shop attributes.
	 */
	public void initTerminalSet() {
		if (terminalFrom.equals("basic")) {
			initBasicTerminalSet();
		}
		else if (terminalFrom.equals("relative")) {
			initRelativeTerminalSet();
		}
		else {
			String terminalFile = "terminals/" + terminalFrom;
			initTerminalSetFromCsv(new File(terminalFile));
		}

		if (includeErc)
			terminals.add(new DoubleERC());
	}

	public void initBasicTerminalSet() {
		terminals = new LinkedList<>();
		for (JobShopAttribute a : JobShopAttribute.basicAttributes()) {
			terminals.add(new AttributeGPNode(a));
		}
	}

	public void initRelativeTerminalSet() {
		terminals = new LinkedList<>();
		for (JobShopAttribute a : JobShopAttribute.relativeAttributes()) {
			terminals.add(new AttributeGPNode(a));
		}
	}
	
	public void initTerminalSetFromCsv(File csvFile) {
		terminals = new LinkedList<GPNode>();

		BufferedReader br = null;
        String line = "";

        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
            	JobShopAttribute a = JobShopAttribute.get(line);
				terminals.add(new AttributeGPNode(a));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}

	/**
	 * Return the index of an attribute in the terminal set.
	 * @param attribute the attribute.
	 * @return the index of the attribute in the terminal set.
	 */
	public int indexOfAttribute(JobShopAttribute attribute) {
		for (int i = 0; i < terminals.size(); i++) {
			JobShopAttribute terminalAttribute = ((AttributeGPNode)terminals.get(i)).getJobShopAttribute();
			if (terminalAttribute == attribute) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Randomly pick a terminal from the terminal set.
	 * @return the selected terminal, which is a GPNode.
	 */
	public GPNode pickTerminalRandom() {
    	int index = random[0].nextInt(terminals.size());
    	return terminals.get(index);
    }
	

	// the best individual in subpopulation
	public Individual bestIndi(int subpop) {
		int best = 0;
		for(int x = 1; x < population.subpops[subpop].individuals.length; x++)
			if (population.subpops[subpop].individuals[x].fitness.betterThan(population.subpops[subpop].individuals[best].fitness))
				best = x;

		return population.subpops[subpop].individuals[best];
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		Parameter p;

		// Get the job seed.
		p = new Parameter("seed").push(""+0);
		jobSeed = parameters.getLongWithDefault(p, null, 0);

 		p = new Parameter(P_TERMINALS_FROM);
 		terminalFrom = parameters.getStringWithDefault(p, null, "basic");

		p = new Parameter(P_INCLUDE_ERC);
		includeErc = parameters.getBoolean(p, null, false);
		
		initTerminalSet();

		super.setup(this, base);
	}

	@Override
	public void run(int condition)
    {
		double totalTime = 0;

		if (condition == C_STARTED_FRESH) {
			startFresh();
        }
		else {
			startFromCheckpoint();
        }

		int result = R_NOTDONE;
		while ( result == R_NOTDONE )
        {
			long start = yimei.util.Timer.getCpuTime();
			long startUser = yimei.util.Timer.getUserTime();
			long startSys = yimei.util.Timer.getSystemTime();
			result = evolve();

			long finish = yimei.util.Timer.getCpuTime();
			long finishUser = yimei.util.Timer.getUserTime();
			long finishSys = yimei.util.Timer.getSystemTime();
			double duration = (finish - start) / 1000000000;
			double durationUser = (finishUser - startUser) / 1000000000;
			double durationSys = (finishSys - startSys) / 1000000000;
			genTimes.add(duration);
			totalTime += duration;

			output.message("Generation " + (generation-1) + " elapsed " + duration + " seconds, user: " + durationUser +", system: "+durationSys+".");
        }

		output.message("The whole program elapsed " + totalTime + " seconds.");

//		File timeFile = new File("job." + jobSeed + ".time.csv");
//		try {
//			BufferedWriter writer = new BufferedWriter(new FileWriter(timeFile));
//			writer.write("Gen,Time");
//			writer.newLine();
//			for (int gen = 0; gen < genTimes.size(); gen++) {
//				writer.write(gen + "," + genTimes.get(gen));
//				writer.newLine();
//			}
//			writer.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		finish(result);
		
		//========= zhixing LGP in JSS, 2021.3.23
		//the testing evaluation
//		if(((SimpleStatistics)statistics).best_of_run == null){
//			System.out.println("The best individuals should be null?");
//			return;
//		}
//		
//		System.out.print("The testing fitness is: "); 
//		this.output.print("The testing fitness is: ", ((SimpleStatistics)statistics).statisticslog);
//		//get the number of testing simulation -> TODO: add a for loop to simulate the best individual with different seeds
//		for(int ti = 0; ti< 10; ti++) {
//			for(int x = 0;x<this.population.subpops.length; x++){
//				//get the best individual
//				Individual ind = ((SimpleStatistics)statistics).best_of_run[x];
//				ind.evaluated = false;
//				//get the new problem
//				IndividualOptimizationProblem problem = (IndividualOptimizationProblem)evaluator.p_problem;
//				//if (problem.getEvaluationModel().isRotatable()) 
//				{
//					problem.rotateEvaluationModel();
//				}
//				//get EvaluationModel
//				AbstractEvaluationModel evaluationModel = problem.getEvaluationModel();
//				
//				//run the simulation
//		        List<ec.Fitness> fitnesses = new ArrayList();
//		        fitnesses.add(ind.fitness);
//		        ((SimpleEvaluationModel4Ind)evaluationModel).evaluate(fitnesses, (LGPIndividual)ind, this);
//				
//				//get the fitness and record it into the log file
//		        System.out.print(ind.fitness.fitness() + "\t"); 
//		        this.output.print(ind.fitness.fitness() + "\t", ((SimpleStatistics)statistics).statisticslog);
//			}
//		}
//		System.out.println(); 
//		this.output.println("", ((SimpleStatistics)statistics).statisticslog);

		//===========
    }

    @Override
	public int evolve() {
	    if (generation > 0)
	        output.message("Generation " + generation);

	    // EVALUATION
	    statistics.preEvaluationStatistics(this);
	    evaluator.evaluatePopulation(this);
	    statistics.postEvaluationStatistics(this);

	    // SHOULD WE QUIT?
	    if (evaluator.runComplete(this) && quitOnRunComplete)
	        {
	        output.message("Found Ideal Individual");
	        return R_SUCCESS;
	        }

	    // SHOULD WE QUIT?
	    if (generation == numGenerations-1)
	        {
	        return R_FAILURE;
	        }

	    // PRE-BREEDING EXCHANGING
	    statistics.prePreBreedingExchangeStatistics(this);
	    population = exchanger.preBreedingExchangePopulation(this);
	    statistics.postPreBreedingExchangeStatistics(this);
	    
	    String exchangerWantsToShutdown = exchanger.runComplete(this);
	    if (exchangerWantsToShutdown!=null)
	        {
	        output.message(exchangerWantsToShutdown);
	        /*
	         * Don't really know what to return here.  The only place I could
	         * find where runComplete ever returns non-null is
	         * IslandExchange.  However, that can return non-null whether or
	         * not the ideal individual was found (for example, if there was
	         * a communication error with the server).
	         *
	         * Since the original version of this code didn't care, and the
	         * result was initialized to R_SUCCESS before the while loop, I'm
	         * just going to return R_SUCCESS here.
	         */

	        return R_SUCCESS;
	        }

	    // BREEDING
	    statistics.preBreedingStatistics(this);

	    population = breeder.breedPopulation(this);

	    // POST-BREEDING EXCHANGING
	    statistics.postBreedingStatistics(this);

	    // POST-BREEDING EXCHANGING
	    statistics.prePostBreedingExchangeStatistics(this);
	    population = exchanger.postBreedingExchangePopulation(this);
	    statistics.postPostBreedingExchangeStatistics(this);

	    // Generate new instances if needed
		RuleOptimizationProblem problem = (RuleOptimizationProblem)evaluator.p_problem;
	    if (problem.getEvaluationModel().isRotatable()) {
			problem.rotateEvaluationModel();
		}

	    // INCREMENT GENERATION AND CHECKPOINT
	    generation++;
	    if (checkpoint && generation%checkpointModulo == 0)
	        {
	        output.message("Checkpointing");
	        statistics.preCheckpointStatistics(this);
	        Checkpoint.setCheckpoint(this);
	        statistics.postCheckpointStatistics(this);
	        }

	    return R_NOTDONE;
	}
}
