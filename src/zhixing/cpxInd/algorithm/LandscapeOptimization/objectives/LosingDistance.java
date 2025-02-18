package zhixing.cpxInd.algorithm.LandscapeOptimization.objectives;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexList;

public class LosingDistance  extends Objective4FLO{

public short [][][] theta;
	
	public GenoVector[] genoArray;
	
	Board loseBoard;
	
	@Override
	protected double[] gradient() {
		
		this.genoArray = loseBoard.toGenoArray(indexlist);
		this.theta = loseBoard.getThetaArray(indexlist);
		double [] weight = loseBoard.getWeightArray();
		
		int boardsize = loseBoard.boardSize();
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
		
		this.genoArray = loseBoard.toGenoArray(indexlist);
		double [] weight = loseBoard.getWeightArray();
		
		double DI = 0;
		int boardsize = loseBoard.boardSize();
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
		loseBoard = board.lightTrimCloneByRandomWorst(state, thread);
		loseBoard.randomShrinkItem(state, thread, batchsize);
//		leadBoard.trim2MaxsizeByBest();
		setUsedItem(indexlist, loseBoard);
	}

	@Override
	public void preprocessing(EvolutionState state, int thread, IndexList indexlist, Board board, int boardsize, int batchsize) {
		board.weightBoardItem();
		loseBoard = board.lightTrimCloneByRandomWorst(state, thread, boardsize);
		loseBoard.randomShrinkItem(state, thread, batchsize);
//		leadBoard.trim2MaxsizeByBest();
		setUsedItem(indexlist, loseBoard);
		
	}
}
