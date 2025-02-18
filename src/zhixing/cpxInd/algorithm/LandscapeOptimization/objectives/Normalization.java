package zhixing.cpxInd.algorithm.LandscapeOptimization.objectives;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Index;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexList;

public class Normalization extends Objective4FLO {

	double [][] DN; 
	
	Board leadBoard;
	
	@Override
	protected double evaluate() {
		
		DN = indexlist.getDiffNeighbor();

//		setUsedItem(indexlist, leadBoard);
		
		int n = DN.length;
		double res = 0;
		
		for(int i = 0; i<n; i++) {
			if(!usedItem[i]) continue;
			for(int j = 0; j<n; j++) {
				if(i == j) continue;
				double tmp = DN[i][j]*DN[i][j] - Math.pow( (((Index)indexlist.get(i)).index - ((Index)indexlist.get(j)).index)/((double)n)  , 2 ); 
				res += tmp*tmp;
			}
		}
		
		return res*privateCoef;
	}

	@Override
	protected double[] gradient() {
		
		DN = indexlist.getDiffNeighbor();
		int n = DN.length;
		double [] pEpI = new double[indexlist.size()];
		
//		setUsedItem(indexlist, leadBoard);
		
		for(int l = 0; l<n; l++) {
			
			
			for(int i = 0; i<n; i++) {
				if(!usedItem[i] || l == i) continue;
				
				double detail_I = ((Index)indexlist.get(i)).index - ((Index)indexlist.get(l)).index;
				pEpI[l] += detail_I * (DN[l][i]*DN[l][i] - detail_I*detail_I*(1./(n*n)));
				
			}
			pEpI[l] *= 8./(n*n);  //sine all dimensions are multiplied by 8.0, it is unnecessary
		}
		
		for(int l = 0; l<n; l++) {
			pEpI[l] *= privateCoef;
		}
		
		return pEpI;
	}

	@Override
	public void setPrivateCoefficiency(IndexList indexlist, Board board) {
		privateCoef = 1.0;
		privateCoef = 1.0 / this.getRawObjective(indexlist, board);
	}

	@Override
	public void preprocessing(EvolutionState state, int thread, IndexList indexlist, Board board, int batchsize) {
		board.weightBoardItem();
		leadBoard = board.lightTrimCloneByRandomBest(state, thread);
		leadBoard.randomShrinkItem(state, thread, batchsize);
//		leadBoard.trim2MaxsizeByBest();
		setUsedItem(indexlist, leadBoard);
	}

	@Override
	public void preprocessing(EvolutionState state, int thread, IndexList indexlist, Board board, int boardsize, int batchsize) {
		board.weightBoardItem();
		leadBoard = board.lightTrimCloneByRandomBest(state, thread, boardsize);
		leadBoard.randomShrinkItem(state, thread, batchsize);
//		leadBoard.trim2MaxsizeByBest();
		setUsedItem(indexlist, leadBoard);
		
	}
}
