package zhixing.cpxInd.algorithm.LandscapeOptimization.objectives;

import java.util.ArrayList;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Index;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexList;
import zhixing.cpxInd.individual.CpxGPIndividual;

public abstract class Objective4FLO<T> {
	
	public IndexList<T> indexlist;
	
	public Board board;
	
	protected boolean [] usedItem;
	
	protected double privateCoef = 1.0; //for normalization, normalize the objective values to [0, 1]

	public double evaluate(IndexList<T>indexlist, Board board) {
		
		this.indexlist = indexlist;
		this.board = board;
		
		return evaluate();
	}
	
	public double[]	gradient(IndexList<T>indexlist, Board board) {
		this.indexlist = indexlist;
		this.board = board;
		
		double [] gradient = gradient();
		
		//transform into a unit vector
//		double norm = 0;
//		for(int n = 0; n<gradient.length; n++) {
//			norm += gradient[n]*gradient[n];
//		}
//		norm = Math.sqrt(norm);
//		
//		for(int n = 0; n<gradient.length; n++) {
//			gradient[n] /= norm;
//		}
		
		return gradient;
	}
	
	public void setUsedItem(IndexList<T>indexlist, Board board) {
		this.indexlist = indexlist;
		this.board = board;
		
		ArrayList<GenoVector> gv_list = new ArrayList<>();
		
		for(int b = 0; b<board.size(); b++) {
			for(int bi = 0; bi<board.get(b).size(); bi++) {
				
				CpxGPIndividual ind = board.get(b).get(bi);
				GenoVector gv = this.indexlist.getGenoVector(ind);
				
				gv_list.add(gv);
			}
		}
		
		usedItem = new boolean [indexlist.size()];
		for(GenoVector gv : gv_list) {
			for(int k = 0; k < gv.length; k++) {
				if(gv.G[k] >= 0) {
					//find the position of item that has this index
					int pos = 0;
					for(Index ni : this.indexlist) {
						if(ni.index == gv.G[k]) {
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
		
//		for(int b = 0; b<loseboard.size(); b++) {
//			for(int bi = 0; bi<loseboard.get(b).size(); bi++) {
//				
//				CpxGPIndividual ind = loseboard.get(b).get(bi);
//				GenoVector gv = this.indexlist.getGenoVector(ind);
//				
//				for(int k = 0; k < gv.length; k++) {
//					if(gv.G[k] >= 0) {
//						//find the position of item that has this index
//						int pos = 0;
//						for(Index ni : this.indexlist) {
//							if(ni.index == gv.G[k]) {
//								break;
//							}
//							pos ++;
//						}
//						usedItem[pos] = true;
//					}
//					else {
//						break;
//					}
//				}
//			}
//		}
	}
	
	public boolean[] getUsedItem() {
		return usedItem;
	}
	
	public static boolean[] getUsedItem(IndexList indexlist, Board board) {

		boolean [] usedItem = new boolean [indexlist.size()];
		
		for(int b = 0; b<board.size(); b++) {
			for(int bi = 0; bi<board.get(b).size(); bi++) {
				
				CpxGPIndividual ind = board.get(b).get(bi);
				GenoVector gv = indexlist.getGenoVector(ind);
				
				for(int k = 0; k < gv.length; k++) {
					if(gv.G[k] >= 0) {
						//find the position of item that has this index
						int pos = 0;
						for( ;pos<indexlist.size(); pos ++) {
							if( ((Index)indexlist.get(pos)).index == gv.G[k]) {
								break;
							}
						}
						usedItem[pos] = true;
					}
					else {
						break;
					}
				}
			}
		}
		
//		for(int b = 0; b<loseboard.size(); b++) {
//			for(int bi = 0; bi<loseboard.get(b).size(); bi++) {
//				
//				CpxGPIndividual ind = loseboard.get(b).get(bi);
//				GenoVector gv = indexlist.getGenoVector(ind);
//				
//				for(int k = 0; k < gv.length; k++) {
//					if(gv.G[k]>=0) {
//						//find the position of item that has this index
//						int pos = 0;
//						for( ;pos<indexlist.size(); pos ++) {
//							if( ((Index)indexlist.get(pos)).index == gv.G[k]) {
//								break;
//							}
//						}
//						usedItem[pos] = true;
//					}
//					else {
//						break;
//					}
//				}
//			}
//		}
		
		return usedItem;
	}
	
//	public void setUsedItem(IndexList<T>indexlist, Board board) {
//		this.indexlist = indexlist;
//		this.board = board;
//		
//		usedItem = new boolean [indexlist.size()];
//		
//		for(int b = 0; b<board.size(); b++) {
//			for(int bi = 0; bi<board.get(b).size(); bi++) {
//				
//				CpxGPIndividual ind = board.get(b).get(bi);
//				GenoVector gv = this.indexlist.getGenoVector(ind);
//				
//				for(int k = 0; k < gv.length; k++) {
//					if(gv.G[k] >= 0) {
//						//find the position of item that has this index
//						int pos = 0;
//						for(Index ni : this.indexlist) {
//							if(ni.index == gv.G[k]) {
//								break;
//							}
//							pos ++;
//						}
//						usedItem[pos] = true;
//					}
//					else {
//						break;
//					}
//				}
//			}
//		}
//	}
	
	protected abstract double evaluate();
	
	protected abstract double[] gradient();
	
	public void setPrivateCoefficiency(IndexList indexlist, Board board) {
		privateCoef = 1.0;
		privateCoef = 1.0 / this.getRawObjective(indexlist, board);
	}

	protected double getRawObjective(IndexList indexlist, Board board) {
		return this.evaluate(indexlist, board) / privateCoef;
	}
	
	public abstract void preprocessing(EvolutionState state, int thread, IndexList indexlist, Board board, int batchsize);
	
	public abstract void preprocessing(EvolutionState state, int thread, IndexList indexlist, Board board, int boardsize, int batchsize);
	
	public void updateNewIndexList(EvolutionState state, int thread, IndexList indexlist, Board board) {
		setUsedItem(indexlist, board);
	}
}
