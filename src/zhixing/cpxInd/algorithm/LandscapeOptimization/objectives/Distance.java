package zhixing.cpxInd.algorithm.LandscapeOptimization.objectives;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexList;

public class Distance extends Objective4FLO{
	
	public short [][][] theta;
	
	public GenoVector[] genoArray;
	
	Board leadBoard;
	
	@Override
	protected double[] gradient() {
		
		this.genoArray = leadBoard.toGenoArray(indexlist);
		this.theta = leadBoard.getThetaArray(indexlist);
		double [] weight = leadBoard.getWeightArray();
		
		int boardsize = leadBoard.boardSize();
		double pDpH = privateCoef /(boardsize*boardsize);
		
//		setUsedItem(indexlist, leadBoard);
		
		double [] pHpI = new double[indexlist.size()];
		
		for(int l = 0; l<indexlist.size(); l++) {
			if(!usedItem[l]) continue;
			for(int j = 0;j<boardsize; j++) {
				for(int i = j+1; i<boardsize; i++) {
					pHpI[l] += (weight[j] + weight[i]) * norm2Q.pQpI(genoArray, theta, genoArray, theta, j, i, l);
				}
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
		
		double DI = 0;
		int boardsize = leadBoard.boardSize();
		double BB = boardsize * boardsize;
		
		for(int j = 0; j<boardsize; j++) {
			for(int i = j+1; i<boardsize; i++) {
				DI += (weight[j] + weight[i]) * norm2Q.Q(genoArray[j], genoArray[i]);
			}
		}
		DI /= BB;
		
		
		return DI*privateCoef;
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
}
