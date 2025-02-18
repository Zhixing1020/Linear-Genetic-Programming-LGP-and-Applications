package zhixing.cpxInd.algorithm.LandscapeOptimization.objectives;

import java.util.ArrayList;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.BoardItem;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexList;

public class Causality extends Objective4FLO {

	public ArrayList<short [][][]> theta_list;
	
	public ArrayList<GenoVector[]> genoArray_list;
	
	Board leadBoard;
	
	@Override
	protected double evaluate() {
		
		genoArray_list = new ArrayList<>();
		
		double norm_factor = 0;
		
		for(int c = 0; c<leadBoard.size(); c++) {
			genoArray_list.add(leadBoard.toGenoArray(indexlist, c));
		}
		
		//since leading board has been sorted in an ascending order of fitness values (smaller fitness is better), we directly use it
		double SU = 0;
		for(int j = 1; j<leadBoard.size() - 1; j++) {
			int i = j - 1, k = j+1;
			BoardItem bi = leadBoard.get(i);
			BoardItem bj = leadBoard.get(j);
			BoardItem bk = leadBoard.get(k);
			
			
			for(int ii = 0; ii<bi.size(); ii++) {
				for(int jj = 0; jj<bj.size(); jj++) {
					for(int kk = 0; kk<bk.size(); kk++) {
						double DetailFit_ji = bj.fitness.fitness() - bi.fitness.fitness();
						double DetailFit_kj = bk.fitness.fitness() - bj.fitness.fitness();
						double Qji = norm2Q.Q(genoArray_list.get(j)[jj], genoArray_list.get(i)[ii]);
						double Qkj = norm2Q.Q(genoArray_list.get(k)[kk], genoArray_list.get(j)[jj]);

						SU += Math.pow((DetailFit_ji * Qkj)/(DetailFit_kj * Qji) - 1, 2);
						
						norm_factor ++;
					}
				}
			}
		}
		
		return SU*privateCoef / norm_factor;
	}

	@Override
	protected double[] gradient() {
		
		genoArray_list = new ArrayList<>();
		theta_list = new ArrayList<>();
		
		double norm_factor = 0;
		
		for(int c = 0; c<leadBoard.size(); c++) {
			genoArray_list.add(leadBoard.toGenoArray(indexlist, c));
			theta_list.add(leadBoard.getThetaArrayByIndex(indexlist, c));
		}
		
//		setUsedItem(indexlist, leadBoard);
		
		double [] gradient = new double [indexlist.size()];
		
		for(int j = 1; j<leadBoard.size() - 1; j++) {
			int i = j - 1, k = j+1;
			BoardItem bi = leadBoard.get(i);
			BoardItem bj = leadBoard.get(j);
			BoardItem bk = leadBoard.get(k);
			
			
			//ii, jj, kk  -> a, b, c where a \in BI(i), b \in BI(j), c \in BI(k)
			for(int ii = 0; ii<bi.size(); ii++) {
				for(int jj = 0; jj<bj.size(); jj++) {
					for(int kk = 0; kk<bk.size(); kk++) {
						double DetailFit_ji = bj.fitness.fitness() - bi.fitness.fitness();
						double DetailFit_kj = bk.fitness.fitness() - bj.fitness.fitness();
//						double Qji = norm2Q.Q(genoArray_list.get(j)[jj], genoArray_list.get(i)[ii]);
//						double Qkj = norm2Q.Q(genoArray_list.get(k)[kk], genoArray_list.get(j)[jj]);
						
						double Uabc = Uabc(genoArray_list.get(i), genoArray_list.get(j), genoArray_list.get(k), ii, jj, kk);
						
						double alpha = 2.0 * (DetailFit_ji*Uabc / DetailFit_kj - 1)*(DetailFit_ji / DetailFit_kj); //pSU_pUj
						
						for(int l = 0; l<indexlist.size(); l++) {
							if(!usedItem[l]) continue;
							double pUabc_pIl = pUabc_pIl(genoArray_list.get(i), theta_list.get(i), genoArray_list.get(j),theta_list.get(j), 
									genoArray_list.get(k), theta_list.get(k), ii, jj, kk, l);
							gradient[l] += alpha * pUabc_pIl;
						}
						
						norm_factor ++;
					}
				}
			}
		}
		
		for(int l = 0;l<indexlist.size(); l++) {
			gradient[l] *= privateCoef/norm_factor;
		}
		
		return gradient;
	}

	
	private double pUabc_pIl(final GenoVector[] GA_a, final short [][][] theta_a, final GenoVector[] GA_b, final short [][][] theta_b, 
			final GenoVector[] GA_c, final short [][][] theta_c, int ga, int gb, int gc, int l) {
		
		// GenoVector[] GA_a, _b, _c are three sets of geno vector from three board items,  theta_a, _b, _c are their corresponding theta 
		// ga, gb, gc are the index of the three geno vector arrays and their theta
		// l: the item index in the index list
		// i \in a,  j \in b, k \in c
		
		double Qba = norm2Q.Q(GA_b[gb], GA_a[ga]);
		double Qcb = norm2Q.Q(GA_c[gc], GA_b[gb]);
		double dQcb = norm2Q.pQpI(GA_c, theta_c, GA_b, theta_b, gc, gb, l);
		double dQba = norm2Q.pQpI(GA_b, theta_b, GA_a, theta_a, gb, ga, l);
		
		double res = (dQcb * Qba - Qcb * dQba) / (Qba * Qba);
		
		return res;
	}
	
	private double Uabc(final GenoVector[] GA_a, final GenoVector[] GA_b, final GenoVector[] GA_c, int ga, int gb, int gc) {
		double Qcb = norm2Q.Q(GA_c[gc], GA_b[gb]);
		double Qba = norm2Q.Q(GA_b[gb], GA_a[ga]);
		
		return Qcb / Qba;
	}
	
	@Override
	public void setPrivateCoefficiency(IndexList indexlist, Board board) {
		privateCoef = 1.0;
		privateCoef = 1.0 / this.getRawObjective(indexlist, board);
	}

	@Override
	public void preprocessing(EvolutionState state, int thread, IndexList indexlist, Board board, int batchsize) {
		leadBoard = board.lightTrimCloneByRandomBest(state, thread);
//		leadBoard.trim2MaxsizeByBest();
		setUsedItem(indexlist, leadBoard);
	}

	@Override
	public void preprocessing(EvolutionState state, int thread, IndexList indexlist, Board board, int boardsize, int batchsize) {
		leadBoard = board.lightTrimCloneByRandomBest(state, thread, boardsize);
//		leadBoard.trim2MaxsizeByBest();
		setUsedItem(indexlist, leadBoard);
		
	}
}
