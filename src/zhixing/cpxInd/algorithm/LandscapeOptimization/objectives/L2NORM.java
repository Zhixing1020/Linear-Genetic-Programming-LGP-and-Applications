package zhixing.cpxInd.algorithm.LandscapeOptimization.objectives;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexList;

public class L2NORM extends Objective4FLO{

public short [][][] theta;
	
	public GenoVector[] genoArray;
	
	Board leadBoard;
	
	@Override
	protected double[] gradient() {
		
		this.genoArray = leadBoard.toGenoArray(indexlist);
		this.theta = leadBoard.getThetaArray(indexlist);
		double [] weight = leadBoard.getWeightArray();
		
		int boardsize = leadBoard.boardSize();
		double pDpH = privateCoef /(boardsize);
		
//		setUsedItem(indexlist, leadBoard);
		
		double [] pHpI = new double[indexlist.size()];
		
		for(int l = 0; l<indexlist.size(); l++) {
			if(!usedItem[l]) continue;
			for(int j = 0;j<boardsize; j++) {
				pHpI[l] += weight[j] * getPL2N_Sum(genoArray[j], theta, j, l);
			}
		}
		
		for(int l = 0; l<indexlist.size(); l++) {
			pHpI[l] *= pDpH;
		}
		
		return pHpI;
	}

	@Override
	protected double evaluate() {
		
		this.genoArray = leadBoard.toGenoArray(indexlist);
		double [] weight = leadBoard.getWeightArray();
		
		double L2N = 0;
		int boardsize = leadBoard.boardSize();
		
		for(int j = 0; j<boardsize; j++) {
			L2N += weight[j] * getL2Norm(genoArray[j]);
		}
		L2N /= boardsize;
		
		
		return L2N*privateCoef;
	}

	@Override
	public void setPrivateCoefficiency(IndexList indexlist, Board board) {
		privateCoef = 1.0;
		privateCoef = 1.0 / this.getRawObjective(indexlist, board);
	}

	@Override
	public void preprocessing(EvolutionState state, int thread, IndexList indexlist, Board board, int batchsize) {
		board.weightBoardItem();
		leadBoard = board.lightTrimCloneByBest();
		leadBoard.randomShrinkItem(state, thread, batchsize);
//		leadBoard.trim2MaxsizeByBest();
		setUsedItem(indexlist, leadBoard);
	}

	@Override
	public void preprocessing(EvolutionState state, int thread, IndexList indexlist, Board board, int boardsize, int batchsize) {
		board.weightBoardItem();
		leadBoard = board.lightTrimCloneByBest(boardsize);
		leadBoard.randomShrinkItem(state, thread, batchsize);
//		leadBoard.trim2MaxsizeByBest();
		setUsedItem(indexlist, leadBoard);
		
	}
	
	@Override
	public void updateNewIndexList(EvolutionState state, int thread, IndexList indexlist, Board board) {
		setUsedItem(indexlist, leadBoard);
	}
	
	protected double getL2Norm(final GenoVector g1) {
		double res = 0;
		
		for(int k = 0; k<g1.length; k++) {
			if(g1.G[k] < 0) break;
			res += Math.pow(g1.G[k],2);
		}
		
		return res;
	}
	
	protected double getPL2N_Sum(final GenoVector g1, final short [][][] theta1, final int gj, int l) {
		double res = 0;
		
		for(int k = 0; k<g1.length; k++) {
			if(g1.G[k] < 0) break;
			
			res += 2*g1.G[k]*theta1[gj][k][l];
		}
		
		return res;
	}
}
