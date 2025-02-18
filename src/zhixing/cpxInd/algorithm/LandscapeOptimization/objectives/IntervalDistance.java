package zhixing.cpxInd.algorithm.LandscapeOptimization.objectives;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Index;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexList;
import zhixing.cpxInd.individual.CpxGPIndividual;

public class IntervalDistance extends Objective4FLO{
	
	public short [][][] theta;
	
	public GenoVector[] genoArray;
	
	public short [][][] theta_lose;
	
	public GenoVector[] genoArray_lose;
	
	Board leadBoard;
	Board loseBoard;

	@Override
	protected double[] gradient() {
		
		this.genoArray = leadBoard .toGenoArray(indexlist);
		this.theta = leadBoard .getThetaArray(indexlist);
		this.genoArray_lose = loseBoard.toGenoArray(indexlist);
		this.theta_lose = loseBoard.getThetaArray(indexlist);
		double [] weightLead = leadBoard.getWeightArray();
		double [] weightLose = loseBoard.getWeightArray();
		
		int boardsize = leadBoard .boardSize();
		int loseboardsize = loseBoard.boardSize();
		double DloseI = this.getRawObjective(indexlist, board);
		double pDpH = - privateCoef /(boardsize*loseboardsize*DloseI*DloseI);
		
//		setUsedItem(indexlist, board);
		
		double [] pHpI = new double[indexlist.size()];
		
		for(int l = 0; l<indexlist.size(); l++) {
			if(! usedItem[l]) continue;
			for(int j = 0;j<boardsize; j++) {
				for(int i = 0; i<loseboardsize; i++) {
					pHpI[l] += (weightLead[j] + weightLose[i]) * norm2Q.pQpI(genoArray, theta, genoArray_lose, theta_lose, j, i, l);
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
		this.genoArray_lose = loseBoard.toGenoArray(indexlist);
		double [] weightLead = leadBoard.getWeightArray();
		double [] weightLose = loseBoard.getWeightArray();
		
		double DI = 0;
		int boardsize = leadBoard.boardSize();
		int loseboardsize = loseBoard.boardSize();
		double BL = boardsize * loseboardsize;
		
		for(int j = 0; j<boardsize; j++) {
			for(int i = 0; i<loseboardsize; i++) {
				DI += (weightLead[j] + weightLose[i]) * norm2Q.Q(genoArray[j], genoArray_lose[i]);
			}
		}
		DI /= BL;
		
		
//		return 1 + DI*privateCoef;
		return privateCoef / DI;
	}
	
	@Override
	protected double getRawObjective(IndexList indexlist, Board board) {
		return privateCoef / this.evaluate(indexlist, board);
	}

	@Override
	public void setPrivateCoefficiency(IndexList indexlist, Board board) {
		privateCoef = 1.0;
//		privateCoef = -1.0 / this.getRawObjective(indexlist, board);
		privateCoef = this.getRawObjective(indexlist, board);
	}
	
	public void setUsedItem(IndexList indexlist, Board board1, Board board2) {
		this.indexlist = indexlist;

//		Board leadBoard = board.lightTrimCloneByBest();
//		leadBoard.trim2MaxsizeByBest();
		
//		Board loseboard = board.lightTrimCloneByWorst();
//		loseboard.trim2MaxsizeByWorst();
		
		usedItem = new boolean [indexlist.size()];
		
		for(int b = 0; b<board1.size(); b++) {
			for(int bi = 0; bi<board1.get(b).size(); bi++) {
				
				CpxGPIndividual ind = board1.get(b).get(bi);
				GenoVector gv = this.indexlist.getGenoVector(ind);
				
				for(int k = 0; k < gv.length; k++) {
					if(gv.G[k] >= 0) {
						//find the position of item that has this index
						int pos = 0;
						for(Object ni : this.indexlist) {
							if(((Index)ni).index == gv.G[k]) {
								break;
							}
							pos ++;
						}
						usedItem[pos] = true;
					}
					else {
						break;
					}
				}
			}
		}
		
		for(int b = 0; b<board2.size(); b++) {
			for(int bi = 0; bi<board2.get(b).size(); bi++) {
				
				CpxGPIndividual ind = board2.get(b).get(bi);
				GenoVector gv = this.indexlist.getGenoVector(ind);
				
				for(int k = 0; k < gv.length; k++) {
					if(gv.G[k] >= 0) {
						//find the position of item that has this index
						int pos = 0;
						for(Object ni : this.indexlist) {
							if(((Index)ni).index == gv.G[k]) {
								break;
							}
							pos ++;
						}
						usedItem[pos] = true;
					}
					else {
						break;
					}
				}
			}
		}
	}

	@Override
	public void preprocessing(EvolutionState state, int thread, IndexList indexlist, Board board, int batchsize) {
		board.weightBoardItem();
//		leadBoard = board.lightTrimCloneByRandomBest(state, thread);
		leadBoard = board.lightTrimCloneByBest();
		leadBoard.randomShrinkItem(state, thread, batchsize);
//		leadBoard.trim2MaxsizeByBest();
		
		loseBoard = board.lightTrimCloneByRandomWorst(state, thread);
		loseBoard.randomShrinkItem(state, thread, batchsize);
//		loseBoard.trim2MaxsizeByWorst();
		
		setUsedItem(indexlist, leadBoard, loseBoard);

	}

	@Override
	public void preprocessing(EvolutionState state, int thread, IndexList indexlist, Board board, int boardsize, int batchsize) {
		board.weightBoardItem();
//		leadBoard = board.lightTrimCloneByRandomBest(state, thread, boardsize);
		leadBoard = board.lightTrimCloneByBest();
		leadBoard.randomShrinkItem(state, thread, batchsize);
//		leadBoard.trim2MaxsizeByBest();
		
		loseBoard = board.lightTrimCloneByRandomWorst(state, thread, boardsize);
		loseBoard.randomShrinkItem(state, thread, batchsize);
//		loseBoard.trim2MaxsizeByWorst();
		
		setUsedItem(indexlist, leadBoard, loseBoard);
		
	}
}
