package zhixing.cpxInd.algorithm.LandscapeOptimization;


import java.util.ArrayList;
import java.util.Collections;

import ec.EvolutionState;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexList;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;

public class SubpopulationFLO extends ec.Subpopulation {
	
	public static final String P_SUBPOPULATION = "subpop_FLO";
	public static final String P_INDEXLIST = "indexlist";
	public static final String P_BOARD = "board";
	public static final String P_LEADINGBOARD = "leadingboard";
	public static final String P_LOSINGBOARD = "losingboard";
	public static final String P_UPDATEINTERVAL = "updateinterval";
	public static final String P_LOGMETINTERVAL = "logmetricinterval";
	
	public IndexList<GPTreeStruct> IndList;
	
	public Board fullBoard;
//	public Board LeadBoard;
//	public Board LoseBoard;
	
	public int updateInterval = 1;
	
	public int logMetricInterval = 10;
	
	public Parameter defaultBase()
    {
		return new Parameter(P_SUBPOPULATION);
    }
	
	@SuppressWarnings("unchecked")
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		Parameter def = defaultBase();
		
		IndList = (IndexList<GPTreeStruct>) state.parameters.getInstanceForParameter(base.push(P_INDEXLIST), def.push(P_INDEXLIST), IndexList.class);
		IndList.setup(state, base.push(P_INDEXLIST));
		
		fullBoard = (Board) state.parameters.getInstanceForParameter(base.push(P_BOARD), def.push(P_BOARD), ArrayList.class);
		fullBoard.setup(state, base.push(P_BOARD));

		updateInterval = state.parameters.getIntWithDefault(base.push(P_UPDATEINTERVAL), def.push(P_UPDATEINTERVAL), 1);
		if(updateInterval < 1) {
			state.output.fatal("The update interval of SubpopulationFLO is at least 1");
		}
		
		logMetricInterval = state.parameters.getIntWithDefault(base.push(P_LOGMETINTERVAL), def.push(P_LOGMETINTERVAL), 10);
		if(logMetricInterval < 1) {
			state.output.fatal("The log metric interval of SubpopulationFLO is at least 1");
		}
	}

	public void updateBoard(EvolutionState state, int thread) {
		fullBoard.loadOutAnchors((int) (1. - (double)state.generation / state.numGenerations));
		fullBoard.clear();
		for(int i = 0; i<this.individuals.length; i++) {
			fullBoard.addIndividual((CpxGPIndividual) this.individuals[i]); //no clone, to improve memory efficiency
		}
		Collections.sort(fullBoard);
		fullBoard.reloadAnchors(state, thread);

	}
	
	public void optimizeIndexes(EvolutionState state, int thread) {
		IndList.optimizeIndex(state, thread, this, fullBoard);
	}
}
