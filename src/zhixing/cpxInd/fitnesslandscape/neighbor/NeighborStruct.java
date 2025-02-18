package zhixing.cpxInd.fitnesslandscape.neighbor;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.cpxInd.fitnesslandscape.LGPFitnessLandscape;
import zhixing.cpxInd.individual.CpxGPIndividual;

public abstract class NeighborStruct {
	final static public String NEIGHBORSTRUCT = "neighborstructure";
	final static public String P_PERCENTNEIGHBORSAMPLE = "percentNeighborSample";

//	protected ArrayList<CpxGPIndividual> neighbors = new ArrayList<>();
	
	public static double default_percentNeighborSampling; //define the percentage of sampling when determining neighborhood
	
	public void setup(final EvolutionState state, final Parameter base) {
		Parameter def = new Parameter(NEIGHBORSTRUCT);
		
		default_percentNeighborSampling = state.parameters.getDoubleWithDefault(null, def.push(P_PERCENTNEIGHBORSAMPLE), 0.01);
		if(default_percentNeighborSampling <= 0 || default_percentNeighborSampling > 1) {
			System.err.print("the percentage of sampling neighbors must be in range (0.0, 1.0].\n");
			System.exit(1);
		}
	}
	
	abstract public ArrayList<CpxGPIndividual> getNeighbors(EvolutionState state, int thread, CpxGPIndividual sample, LGPFitnessLandscape landscape, double epsilon);
	
	abstract public CpxGPIndividual getaNeighbor(EvolutionState state, int thread, CpxGPIndividual sample, LGPFitnessLandscape landscape, double epsilon);
	
	abstract public boolean isNeighbor(EvolutionState state, int thread, CpxGPIndividual sample, CpxGPIndividual tryind, LGPFitnessLandscape landscape, double epsilon);
	
	abstract public double distance(EvolutionState state, int thread, CpxGPIndividual sample1, CpxGPIndividual sample2, LGPFitnessLandscape landscape);
}
