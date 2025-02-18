package zhixing.cpxInd.algorithm.LandscapeOptimization.objectives;

import java.util.ArrayList;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.BoardItem;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Index;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexList;

public class Navigation extends Objective4FLO {
public ArrayList<short [][][]> theta_list;
	
	public ArrayList<GenoVector[]> genoArray_list;
	
	Board leadBoard;
	
	double [] Ajabc_pI = null;
	short [][] W_k = null;
	short [][] W_kT = null;
	double [] Bjabc_pI = null;
	
	@Override
	protected double evaluate() {
		
		genoArray_list = new ArrayList<>();
		
		double norm_factor = 0;
		
		for(int c = 0; c<leadBoard.size(); c++) {
			genoArray_list.add(leadBoard.toGenoArray(indexlist, c));
		}
		
		//since leading board has been sorted in an ascending order of fitness values (smaller fitness is better), we directly use it
		double cos = 0;
		for(int b = 1; b<leadBoard.size() - 1; b++) {
			int a = b - 1, c = b + 1;
			BoardItem Ba = leadBoard.get(a);
			BoardItem Bb = leadBoard.get(b);
			BoardItem Bc = leadBoard.get(c);
			
			
			for(int ii = 0; ii<Ba.size(); ii++) {
				for(int jj = 0; jj<Bb.size(); jj++) {
					for(int kk = 0; kk<Bc.size(); kk++) {
						
						double dG1dG2 = 0;
						for(int k = 0; k<genoArray_list.get(0)[0].length; k++) {
							//dG1dG2 += (genoArray_list.get(a)[ii].G[k] - genoArray_list.get(c)[kk].G[k])*(genoArray_list.get(b)[jj].G[k] - genoArray_list.get(c)[kk].G[k]);
							dG1dG2 += norm2Q.subQ(genoArray_list.get(a)[ii].G[k], genoArray_list.get(c)[kk].G[k])*norm2Q.subQ(genoArray_list.get(b)[jj].G[k], genoArray_list.get(c)[kk].G[k]);
						}
						
						double Qac = norm2Q.Q(genoArray_list.get(a)[ii], genoArray_list.get(c)[kk]);
						double Qbc = norm2Q.Q(genoArray_list.get(b)[jj], genoArray_list.get(c)[kk]);

						cos += (Ba.weight + Bb.weight + Bc.weight) * (1 - dG1dG2 / Math.sqrt(Qac*Qbc));
						
						norm_factor ++;
					}
				}
			}
		}
		
		cos /= norm_factor;
		
		return cos*privateCoef;
	}

	@Override
	protected double[] gradient() {
		
		genoArray_list = new ArrayList<>();
		theta_list = new ArrayList<>();
		
		double norm_factor = 0;
		
//		setUsedItem(indexlist, leadBoard);
		
		for(int c = 0; c<leadBoard.size(); c++) {
			genoArray_list.add(leadBoard.toGenoArray(indexlist, c));
			theta_list.add(leadBoard.getThetaArrayByIndex(indexlist, c));
		}
		
		double [] gradient = new double [indexlist.size()];
		
		int n = indexlist.size();
		int m = genoArray_list.get(0)[0].length;
		
//		double [] Ajabc_pI = new double [n];
//		double [][] W_k = new double [n][n];
//		double [][] W_kT = new double [n][n];
//		double [] Bjabc_pI = new double [n];
		
		for(int b = 1; b<leadBoard.size() - 1; b++) {
			int a = b - 1, c = b + 1;
			BoardItem Ba = leadBoard.get(a);
			BoardItem Bb = leadBoard.get(b);
			BoardItem Bc = leadBoard.get(c);
			
			
			for(int ii = 0; ii<Ba.size(); ii++) {
				for(int jj = 0; jj<Bb.size(); jj++) {
					for(int kk = 0; kk<Bc.size(); kk++) {
						
						double Ajabc = 0, Bjabc = 0;
						
						double dG1dG2 = 0;
						for(int k = 0; k<m; k++) {
							if((genoArray_list.get(a)[ii].G[k] == GenoVector.None || genoArray_list.get(b)[jj].G[k] == GenoVector.None) && genoArray_list.get(c)[kk].G[k] == GenoVector.None ) break;
							dG1dG2 += (genoArray_list.get(a)[ii].G[k] - genoArray_list.get(c)[kk].G[k])*(genoArray_list.get(b)[jj].G[k] - genoArray_list.get(c)[kk].G[k]);
						}
						
						Ajabc = dG1dG2;

						double Qac = norm2Q.Q(genoArray_list.get(a)[ii], genoArray_list.get(c)[kk]);
						double Qbc = norm2Q.Q(genoArray_list.get(b)[jj], genoArray_list.get(c)[kk]);

						Bjabc = Math.sqrt(Qac*Qbc); 
						
						//get Ajabc_pI
//						double [] Ajabc_pI = new double [n];
//						reset1Darray(Ajabc_pI, n);
						for(int k = 0; k<m; k++) {
							if((genoArray_list.get(a)[ii].G[k] == GenoVector.None || genoArray_list.get(b)[jj].G[k] == GenoVector.None) && genoArray_list.get(c)[kk].G[k] == GenoVector.None ) break;
							//for each k, we get W_k * I + W_kT * I, and sum them up
							
							short [] theta_ak = theta_list.get(a)[ii][k];
							short [] theta_bk = theta_list.get(b)[jj][k];
							short [] theta_ck = theta_list.get(c)[kk][k];
							
							//get the positions of ones in theta_ak, theta_bk, and theta_ck, denoted as ta1, tb1, tc1
							//the non-zero elements would be W[ta1][tb1] += 1, W[ta1][tc1] += -1, W[tb1][tb1] += -1, W[tb1][tc1] = 1
							//for W[x][y] * I, we only need to add I[y] into pI[x] i.e., Ajabc_pI[l] += W[x][y]*I[y], l=x 
							
//							double [][] W_k = getW(theta_ak, theta_bk, theta_ck, n);
//							
//							double [][]W_kT = transpose(W_k);
//							
//							for(int l = 0; l<n; l++) {
//								
//								//W_k[l] * I
//								for(int ll = 0; ll<n; ll++) {
//									Ajabc_pI[l] += W_k[l][ll]*((Index)indexlist.get(ll)).index;
//								}
//								
//								//+ W_kT[l] * I
//								for(int ll = 0; ll<n; ll++) {
//									Ajabc_pI[l] += W_kT[l][ll]*((Index)indexlist.get(ll)).index;
//								}
//							}
							
							int ta1, tb1, tc1;
//							double [][] W_k = new double [n][n];
//							double [][] W_kT = new double [n][n];
//							reset2Darray(W_k, n, n);
//							reset2Darray(W_kT, n, n);
							for(ta1 = 0; ta1<theta_ak.length; ta1++) {
								if(theta_ak[ta1] == 1) break;
							}
							if(ta1 >= theta_ak.length) ta1 = -1;
							
							for(tb1 = 0; tb1<theta_bk.length; tb1++) {
								if(theta_bk[tb1] == 1) break;
							}
							if(tb1 >= theta_bk.length) tb1 = -1;
							
							for(tc1 = 0; tc1<theta_ck.length; tc1++) {
								if(theta_ck[tc1] == 1) break;
							}
							if(tc1 >= theta_ck.length) tc1 = -1;
							
							if(ta1 >= 0 && tb1 >= 0) {
								W_k[ta1][tb1] += 1;
								W_kT[tb1][ta1] += 1;
							}
							if(ta1 >= 0 && tc1 >= 0) {
								W_k[ta1][tc1] += -1;
								W_kT[tc1][ta1] += -1;
								
								W_k[tc1][tc1] += 1; 
								W_kT[tc1][tc1] += 1;
							}
							if(tb1 >= 0 && tc1 >= 0) {
								W_k[tb1][tc1] += -1;
								W_kT[tc1][tb1] += -1;
							}
							
							if(ta1 >= 0 && tb1 >= 0) {
								Ajabc_pI[ta1] += (W_k[ta1][tb1] + W_kT[ta1][tb1]) * ((Index)indexlist.get(tb1)).index;
								Ajabc_pI[tb1] += (W_k[tb1][ta1] + W_kT[tb1][ta1]) * ((Index)indexlist.get(ta1)).index;
								
							}
							
							if(ta1 >= 0 && tc1 >= 0) {
								Ajabc_pI[ta1] += (W_k[ta1][tc1] + W_kT[ta1][tc1]) * ((Index)indexlist.get(tc1)).index;
								Ajabc_pI[tc1] += (W_k[tc1][ta1] + W_kT[tc1][ta1]) * ((Index)indexlist.get(ta1)).index;
								
								Ajabc_pI[tc1] += (W_k[tc1][tc1] + W_kT[tc1][tc1]) * ((Index)indexlist.get(tc1)).index;
							}
							
							if(tb1 >= 0 && tc1 >= 0) {
								Ajabc_pI[tb1] += (W_k[tb1][tc1] + W_kT[tb1][tc1]) * ((Index)indexlist.get(tc1)).index;
								Ajabc_pI[tc1] += (W_k[tc1][tb1] + W_kT[tc1][tb1]) * ((Index)indexlist.get(tb1)).index;
							}
							
							//clear W_k and W_kT
							if(ta1 >= 0 && tb1 >= 0) {
								W_k[ta1][tb1] = 0;
								W_kT[tb1][ta1] = 0;
								
							}
							if(ta1 >= 0 && tc1 >= 0) {
								W_k[ta1][tc1] = 0;
								W_kT[tc1][ta1] = 0;
								
								W_k[tc1][tc1] = 0; 
								W_kT[tc1][tc1] = 0;
							}
							if(tb1 >= 0 && tc1 >= 0) {
								W_k[tb1][tc1] = 0;
								W_kT[tc1][tb1] = 0;
							}
						}
						
						//get Bjabc_pI
//						double [] Bjabc_pI = new double [n];
//						reset1Darray(Bjabc_pI, n);
						double norm = 1. / (2*Math.sqrt(Qac * Qbc));
						for(int l = 0; l<n; l++) {
							if(!usedItem[l]) continue;
							double pQacpIl = norm2Q.pQpI(genoArray_list.get(a), theta_list.get(a), genoArray_list.get(c), theta_list.get(c), ii, kk, l);
							double pQbcpIl = norm2Q.pQpI(genoArray_list.get(b), theta_list.get(b), genoArray_list.get(c), theta_list.get(c), jj, kk, l);
							
							Bjabc_pI[l] = norm * (pQacpIl*Qbc + Qac * pQbcpIl);
						}
						
						for(int l = 0; l<n; l++) {
							if(!usedItem[l]) continue;
							gradient[l] += (Ba.weight + Bb.weight + Bc.weight) * (Ajabc_pI[l] * Bjabc - Ajabc * Bjabc_pI[l]) / (Bjabc*Bjabc);
							
							//clear Ajabc_pI and Bjabc_pI
							Ajabc_pI[l] = Bjabc_pI[l] = 0;
						}
						
						norm_factor ++;
					}
				}
			}
		}
		
		for(int l = 0; l<n; l++) {
			gradient[l] *= (- privateCoef /norm_factor); 
		}
		
		return gradient;
	}
	
	public void setPrivateCoefficiency(IndexList indexlist, Board board) {
		privateCoef = 1.0;
		privateCoef = 1.0 / this.getRawObjective(indexlist, board);
	}

	@Override
	public void preprocessing(EvolutionState state, int thread, IndexList indexlist, Board board, int batchsize) {
		board.weightBoardItem();
		leadBoard = board.lightTrimCloneByRandomBest(state, thread);
		leadBoard.randomShrinkItem(state, thread, batchsize);
		if(W_k == null) {
			int n = indexlist.size();
			Ajabc_pI = new double [n];
			W_k = new short [n][n];
			W_kT = new short [n][n];
			Bjabc_pI = new double [n];
		}
		
		setUsedItem(indexlist, leadBoard);
		
	}

	@Override
	public void preprocessing(EvolutionState state, int thread, IndexList indexlist, Board board, int boardsize, int batchsize) {
		board.weightBoardItem();
		leadBoard = board.lightTrimCloneByRandomBest(state, thread, boardsize);
		leadBoard.randomShrinkItem(state, thread, batchsize);
		if(W_k == null) {
			int n = indexlist.size();
			Ajabc_pI = new double [n];
			W_k = new short [n][n];
			W_kT = new short [n][n];
			Bjabc_pI = new double [n];
		}
		
		setUsedItem(indexlist, leadBoard);
		
	}
}
