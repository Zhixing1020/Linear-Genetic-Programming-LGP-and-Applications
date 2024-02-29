package zhixing.djss.algorithm.Grammar.EvolutionState;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.util.Checkpoint;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.Grammar.EvolutionState.EvolutionState4Grammar;
import yimei.jss.gp.GPRuleEvolutionState;
import yimei.jss.gp.terminal.AttributeGPNode;
import yimei.jss.gp.terminal.DoubleERC;
import yimei.jss.gp.terminal.JobShopAttribute;
import yimei.jss.ruleoptimisation.RuleOptimizationProblem;

public class EvolutionState4Grammar4DJSS extends GPRuleEvolutionState implements  EvolutionState4Grammar {

	/**
	 * Read the file to specify the terminals.
	 */
	public final static String P_TERMINALS_FROM = "terminals-from";
	public final static String P_INCLUDE_ERC = "include-erc";
	
	protected String terminalFrom;
	protected boolean includeErc;
	protected long jobSeed;
	protected List<GPNode> terminals;

	public List<GPNode> getTerminals() {
		return terminals;
	}

	public long getJobSeed() {
		return jobSeed;
	}

	public void setTerminals(List<GPNode> terminals) {
		this.terminals = terminals;
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
	
	//followings are the specific functions for Grammar
	
	@Override
	public void initTerminalSet() {
		if (terminalFrom.equals("basic")) {
			initBasicTerminalSet();
		}
		else if (terminalFrom.equals("relative")) {
			initRelativeTerminalSet();
		}
		else if (terminalFrom.equals("relative4Grammar")) {
			initRelativeTerminalSet4Grammar();
		}
		else {
			String terminalFile = "terminals/" + terminalFrom;
			initTerminalSetFromCsv(new File(terminalFile));
		}

		if (includeErc)
			terminals.add(new DoubleERC());
	}
	
	@Override
	public void initRelativeTerminalSet4Grammar() {
		terminals = new LinkedList<>();
		for (JobShopAttribute a : JobShopAttribute.relativeAttributes4grammar()) {
			terminals.add(new AttributeGPNode(a));
		}
	}
	
	@Override
	public int evolve() {
	    if (generation > 0)
	        output.message("Generation " + generation);

	    // EVALUATION
	    statistics.preEvaluationStatistics(this);
	    evaluator.evaluatePopulation(this);
	    
	    updateBannedList(this);
	    
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
