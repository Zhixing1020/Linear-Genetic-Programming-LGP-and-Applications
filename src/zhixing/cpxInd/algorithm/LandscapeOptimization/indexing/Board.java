package zhixing.cpxInd.algorithm.LandscapeOptimization.indexing;

import java.util.ArrayList;
import java.util.Collections;

import ec.EvolutionState;
import ec.Fitness;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.norm2Q;
import zhixing.cpxInd.individual.CpxGPIndividual;

public class Board extends ArrayList<BoardItem>{

	public static final String BOARD = "board";
	
	public static final String P_MAXSIZE = "maxsize";
	public static final String P_ANCHORRATE = "anchor_rate";
	
	protected int maxsize; //record the top-maxsize unique fitness
	public static final int minsize = 3;
	protected double anchorRate;
	
	protected GenoVector [] genoArray;
	protected short [][][]  thetaArray;
	
	protected double maxdiff;
	protected double avgdiff;
	protected double mindiff;
	protected double mincosine;
	protected double avgcosine;
	protected double maxcosine;
	protected int cur_generation_maxdiff = -1;
	protected int cur_generation_mincosine = -1;
	
	private ArrayList<AnchorItem> AnchorList = new ArrayList<>();
	
	protected Parameter getDefault() {
		return  new Parameter(BOARD);
	}
	
	public void setup(final EvolutionState state, final Parameter base) {
		Parameter def = getDefault();
		
		maxsize = state.parameters.getInt(base.push(P_MAXSIZE), def.push(P_MAXSIZE), 1);
		if(maxsize < minsize) {
			System.err.print("borad at least record " + minsize +" fitness and individuals");
			System.exit(1);
		}
		
		anchorRate = state.parameters.getDoubleWithDefault(base.push(P_ANCHORRATE), def.push(P_ANCHORRATE), 0.2);
		if(anchorRate<=0 || anchorRate > 1) {
			System.err.print("the borad got an illegal anchor rate: " + anchorRate +". It must be (0,1].");
			System.exit(1);
		}
	}
	
	public int getIndividualRank(CpxGPIndividual ind) {
		int res = 0;
		String prog_str = ind.toString();
		boolean found = false;
		for(BoardItem item : this) {
			for(CpxGPIndividual in : item) {
				if(in.toString().equals(prog_str)) {
					found = true;
					break;
				}
			}
			if(found) break;
			res ++;
		}
		
		return res;
	}
	
	public double getMaxDiff() {
		return maxdiff;
	}
	public double getAvgDiff() {
		return avgdiff;
	}
	public double getMinDiff() {
		return mindiff;
	}
	public double getMinCosine() {
		return mincosine;
	}
	public double getAvgCosine() {
		return avgcosine;
	}
	public double getMaxCosine() {
		return maxcosine;
	}
	
	public void addIndividual(CpxGPIndividual ind) {
		if(!ind.evaluated) {
			System.out.print("please evaluate the individual before adding to the board");
			return;
		}
		
		int i = 0;
		for(; i< size(); i++) {
			if(this.get(i).add(ind)) {
				break;			
			}
		}
		
		if(i >= size()) { // no existing item having the equivalent fitness to the new individual
			BoardItem n = new BoardItem(ind);
			super.add(n);
		}
	}
	
	@Override
	public boolean add(BoardItem item) {
		
		int i = 0;
		for(; i< size(); i++) {
			if(this.get(i).fitness.equivalentTo(item.fitness)) {
				this.get(i).addAll(item);
				return true;
			}
		}
		
		return super.add(item);
	}
	
	@Override
	public void add(int in, BoardItem item) {
		
		int i = 0;
		for(; i< size(); i++) {
			if(this.get(i).fitness.equivalentTo(item.fitness)) {
				this.get(i).addAll(item);
				System.out.print("there has been an item with the equivalent fitness, Board.add(...) has put individuals to that place");
				break;
			}
		}
		if(i >= size()) {
			super.add(in, item);
		}
		
	}
	
	@Override
	public BoardItem set(int in, BoardItem item) {
		
		if(this.get(in).fitness.equivalentTo(item.fitness)) {
			this.get(in).addAll(item);
			return this.get(in);
		}
		
		//else, check the existence of equivalent fitness
		int i = 0;
		for(; i< size(); i++) {
			if(this.get(i).fitness.equivalentTo(item.fitness)) {
				this.get(i).addAll(item);
				System.out.print("there has been an item with the equivalent fitness, Board.set(...) has put individuals to that place");
				return item;
			}
		}
		
		return super.set(in, item);
				
	}
	
//	public abstract void trim2Maxsize();
	
	public void trim2MaxsizeByBest() {
		Collections.sort(this);
		
		while(size()>maxsize) {
			this.remove(maxsize);
		}
	}
	
	public void trim2MaxsizeByWorst() {
		Collections.sort(this);
		
		while(size()>maxsize) {
			this.remove(0);
		}
	}
	
	public void trim2MaxisizeByRandomBest(EvolutionState state, int thread) {
		Collections.sort(this);
		
		while(size() > maxsize) {
			int i = state.random[thread].nextInt(this.size());
			int j = state.random[thread].nextInt(this.size());
			
			if(this.get(i).compareTo(this.get(j))<0) {
				this.remove(j);
			}
			else {
				this.remove(i);
			}
		}
		
	}

	public int boardSize(){
		int res = 0;
		for(BoardItem item : this) {
			res += item.size();
		}
		return res;
	}
	
	public GenoVector[] toGenoArray(IndexList indexes) {
		
//		if(size()>maxsize) {
//			trim2Maxsize();
//		}
		
		if(size() <= 0) {
			System.err.print("there is no items on the board but we try to get a Geno Array from it");
		}
		genoArray = new GenoVector[boardSize()];
		
		int i = 0;
		for(BoardItem item : this) {
			for(CpxGPIndividual ind : item) {
				genoArray[i] = indexes.getGenoVector(ind);
				i++;
			}
		}
		
		return genoArray;
		
	}
	
	public GenoVector[] toGenoArray(IndexList indexes, int boarditem_ind) {
		//indexes: the given index system,  boarditem_ind: the index of board item that is to be converted into a geno array
//		if(size()>maxsize) {
//			trim2Maxsize();
//		}
		
		if(size() <= 0) {
			System.err.print("there is no items on the board but we try to get a Geno Array from it");
		}
		
		if(boarditem_ind >= size()) {
			System.err.print("index of Board is out of range");
		}
		GenoVector[] subgenoArray = new GenoVector[this.get(boarditem_ind).size()];
		
		int i = 0;
		
		BoardItem item = this.get(boarditem_ind);
		
		for(CpxGPIndividual ind : item) {
			subgenoArray[i] = indexes.getGenoVector(ind);
			i++;
		}
		
		return subgenoArray;
		
	}
	
	public double [] getWeightArray() {
		double [] res = new double [this.boardSize()];
		
		int i = 0;
		for(BoardItem item : this) {
			for(int j = 0; j < item.size(); j++) {
				res[i] = item.weight;
				i++;
			}
		}
		
		return res; 
	}
	
	public short [][][] getThetaArray(IndexList indexes){
		if(genoArray == null) {
			this.toGenoArray(indexes);
		}
		
		thetaArray = new short [genoArray.length][genoArray[0].length][indexes.size()];
		
		for(int i = 0; i<genoArray.length; i++) {
			GenoVector Gi = genoArray[i];
			for(int j = 0; j<Gi.length; j++) {
				if(Gi.G[j]>=0) {
					int pos = 0;
					for(Object ind : indexes) {
						if(((Index)ind).index == Gi.G[j]) {
							break;
						}
						pos++;
					}
					
					thetaArray[i][j][pos] = 1;
				}
					
				else
					break;
			}
			
		}
		
		return thetaArray;
	}
	
	public short [][][] getThetaArrayByIndex(IndexList indexes, int boarditem_ind){
		
		GenoVector [] subgenoArray = this.toGenoArray(indexes, boarditem_ind);
		
		short[][][] subthetaArray = new short [subgenoArray.length][subgenoArray[0].length][indexes.size()];
		
		for(int i = 0; i<subgenoArray.length; i++) {
			GenoVector Gi = subgenoArray[i];
			for(int j = 0; j<Gi.length; j++) {
				if(Gi.G[j]>=0) {
					int pos = 0;
					for(Object ind : indexes) {
						if(((Index)ind).index == Gi.G[j]) {
							break;
						}
						pos++;
					}
					
					subthetaArray[i][j][pos] = 1;
				}
				else
					break;
			}
			
		}
		
		return subthetaArray;
	}
	
	public void resetGenoDifference(IndexList indexlist, int generation) {
		//based on the board, estimate the maximum deviations of genotypes
		
		if(generation == cur_generation_maxdiff) return;
		
		maxdiff = 0;
		mindiff = 1e8;
		int cnt = 0;
		double sumdiff = 0;
		cur_generation_maxdiff = generation;
		
		ArrayList<GenoVector[]> genoArray_list = new ArrayList<>();
		
		for(int c = 0; c<size(); c++) {
			genoArray_list.add(toGenoArray(indexlist, c));
		}
		
		for(int j = 1; j<size() - 1; j++) {
			int i = j - 1;
			BoardItem bi = this.get(i);
			BoardItem bj = this.get(j);
			
			for(int ii = 0; ii<bi.size(); ii++) {
				for(int jj = 0; jj<bj.size(); jj++) {
					
					double tmp_diff = Math.sqrt(norm2Q.Q(genoArray_list.get(j)[jj], genoArray_list.get(i)[ii]));
					if(tmp_diff > maxdiff) {
						maxdiff = tmp_diff;
					}
					
					if(tmp_diff < mindiff) {
						mindiff = tmp_diff;
					}
					
					cnt++;
					sumdiff += tmp_diff;
				}
			}
		}
		
		avgdiff = sumdiff / cnt;
		
//		System.out.println("maxdiff: " + maxdiff + ", avgdiff: "+ avgdiff);
		
		return;
	}
	
	public void resetGenoAngle(IndexList indexlist, int generation) {
		//based on the board, estimate the maximum angles of genotypes (minimum cosine distance)
		
		if(generation == cur_generation_mincosine) return;
		
		mincosine = 1;
		maxcosine = -1;
		int cnt = 0;
		double sumcosine = 0;
		cur_generation_mincosine = generation;
		
		if(size() < minsize) {
			avgcosine = sumcosine;
			return;
		}
		
		ArrayList<GenoVector[]> genoArray_list = new ArrayList<>();
		
		for(int c = 0; c<size(); c++) {
			genoArray_list.add(toGenoArray(indexlist, c));
		}
		
		for(int k = 2; k<size(); k++) {
			int i = k - 2, j = k - 1;
			BoardItem Bi = this.get(i);
			BoardItem Bj = this.get(j);
			BoardItem Bk = this.get(k);
			
			for(int ii = 0; ii<Bi.size(); ii++) {
				for(int jj = 0; jj<Bj.size(); jj++) {
					for(int kk = 0; kk<Bk.size(); kk++) {
						
						double dG1dG2 = 0;
						for(int gk = 0; gk<genoArray_list.get(0)[0].length; gk++) {
							if((genoArray_list.get(i)[ii].G[gk]<0 || genoArray_list.get(j)[jj].G[gk]<0) && genoArray_list.get(k)[kk].G[gk]<0) break;
							dG1dG2 += norm2Q.subQ(genoArray_list.get(i)[ii].G[gk], genoArray_list.get(k)[kk].G[gk])*norm2Q.subQ(genoArray_list.get(j)[jj].G[gk], genoArray_list.get(k)[kk].G[gk]);
						}
						
						double Q1 = norm2Q.Q(genoArray_list.get(i)[ii], genoArray_list.get(k)[kk]);
						double Q2 = norm2Q.Q(genoArray_list.get(j)[jj], genoArray_list.get(k)[kk]);
						
						double tmp_cos = dG1dG2 / Math.sqrt(Q1*Q2);
						
						if(tmp_cos<mincosine) {
							mincosine = tmp_cos;
						}
						
						if(tmp_cos > maxcosine) {
							maxcosine = tmp_cos;
						}
						
						cnt++;
						sumcosine += tmp_cos;
					}
				}
			}
		}
		
		avgcosine = sumcosine / cnt;
		
//		System.out.println("mincosine: " + mincosine + ", avgcosine: "+ avgcosine);
		
		return;
	}
	
	public Board lightClone() {
		Board obj = new Board();
		
		obj.maxsize = this.maxsize;
		
		for(BoardItem item : this) {
			obj.add(item.lightClone());
		}
		
		return obj;
	}
	
	public Board lightTrimCloneByBest() {
		Board obj = new Board();
		
		obj.maxsize = this.maxsize;
		
		int cnt = 0;
		
		for(BoardItem item : this) {
			obj.add(item.lightClone());
			cnt++;
			if(cnt >= obj.maxsize) break;
		}
		
		return obj;
	}
	
	public Board lightTrimCloneByBest(int size) {
		Board obj = new Board();
		
		obj.maxsize = size;
		
		int cnt = 0;
		
		for(BoardItem item : this) {
			obj.add(item.lightClone());
			cnt++;
			if(cnt >= obj.maxsize) break;
		}
		
		return obj;
	}
	
	public Board lightTrimCloneByWorst() {
		Board obj = new Board();
		
		obj.maxsize = this.maxsize;
		
		for(int i = Math.max(0, this.size() - obj.maxsize); i<this.size(); i++) {
			BoardItem item = this.get(i);
			obj.add(item.lightClone());
		}
		
		return obj;
	}
	
	public Board lightTrimCloneByWorst(int size) {
		Board obj = new Board();
		
		obj.maxsize = size;
		
		for(int i = Math.max(0, this.size() - obj.maxsize); i<this.size(); i++) {
			BoardItem item = this.get(i);
			obj.add(item.lightClone());
		}
		
		return obj;
	}
	
	public Board lightTrimCloneByRandomBest(EvolutionState state, int thread) {
		Board obj = new Board();
		
		obj.maxsize = this.maxsize;
		boolean used [] = new boolean [this.size()];
		
		int cnt = 0;
		for(int trial = 0 ; trial < 10*obj.maxsize; trial++) {
			int i = state.random[thread].nextInt((int) (this.size() / 2.));
			int j = state.random[thread].nextInt((int) (this.size() / 2.));
			
			if(this.get(i).compareTo(this.get(j))<0) {
				if(used[i]) continue;
				obj.add(this.get(i).lightClone());
				used[i] = true;
			}
			else {
				if(used[j]) continue;
				obj.add(this.get(j).lightClone());
				used[j] = true;
			}
			
			cnt ++;
			
			if(cnt>=obj.maxsize) break;
		}
		
		return obj;
	}
	
	public Board lightTrimCloneByRandomBest(EvolutionState state, int thread, int size) {
		Board obj = new Board();
		
		obj.maxsize = size;
		boolean used [] = new boolean [this.size()];
		
		int cnt = 0;
		for(int trial = 0 ; trial < 10*obj.maxsize; trial++) {
			int i = state.random[thread].nextInt((int) (this.size() / 2.));
			int j = state.random[thread].nextInt((int) (this.size() / 2.));
			
			if(this.get(i).compareTo(this.get(j))<0) {
				if(used[i]) continue;
				obj.add(this.get(i).lightClone());
				used[i] = true;
			}
			else {
				if(used[j]) continue;
				obj.add(this.get(j).lightClone());
				used[j] = true;
			}
			
			cnt ++;
			
			if(cnt>=obj.maxsize) break;
		}
		
		return obj;
	}
	
	public Board lightTrimCloneByRandomWorst(EvolutionState state, int thread) {
		Board obj = new Board();
		
		obj.maxsize = this.maxsize;
		boolean used [] = new boolean [this.size()];
		
		int cnt = 0;
		for(int trial = 0 ; trial < 10*obj.maxsize; trial++) {
			int i = (int) (this.size() * 0.5) + state.random[thread].nextInt((int) (this.size() / 2.));
			int j = (int) (this.size() * 0.5) + state.random[thread].nextInt((int) (this.size() / 2.));
			
			if(this.get(i).compareTo(this.get(j))>0) {
				if(used[i]) continue;
				obj.add(this.get(i).lightClone());
				used[i] = true;
			}
			else {
				if(used[j]) continue;
				obj.add(this.get(j).lightClone());
				used[j] = true;
			}
			
			cnt ++;
			
			if(cnt>=obj.maxsize) break;
		}
		
		return obj;
	}
	
	public Board lightTrimCloneByRandomWorst(EvolutionState state, int thread, int size) {
		Board obj = new Board();
		
		obj.maxsize = size;
		boolean used [] = new boolean [this.size()];
		
		int cnt = 0;
		for(int trial = 0 ; trial < 10*obj.maxsize; trial++) {
			int i = (int) (this.size() * 0.5) + state.random[thread].nextInt((int) (this.size() / 2.));
			int j = (int) (this.size() * 0.5) + state.random[thread].nextInt((int) (this.size() / 2.));
			
			if(this.get(i).compareTo(this.get(j))>0) {
				if(used[i]) continue;
				obj.add(this.get(i).lightClone());
				used[i] = true;
			}
			else {
				if(used[j]) continue;
				obj.add(this.get(j).lightClone());
				used[j] = true;
			}
			
			cnt ++;
			
			if(cnt>=obj.maxsize) break;
		}
		
		return obj;
	}
	
	public Board lightTrimCloneByRandom(EvolutionState state, int thread) {
		Board obj = new Board();
		
		obj.maxsize = this.maxsize;
		boolean used [] = new boolean [this.size()];
		
		int cnt = 0;
		for(int trial = 0 ; trial < 10*obj.maxsize; trial++) {
			int i = state.random[thread].nextInt(this.size());
			
			if(used[i]) continue;
			obj.add(this.get(i).lightClone());
			used[i] = true;
			
			cnt ++;
			
			if(cnt>=obj.maxsize) break;
		}
		
		return obj;
	}
	
	public Board lightTrimCloneByRandom(EvolutionState state, int thread, int size) {
		Board obj = new Board();
		
		obj.maxsize = size;
		boolean used [] = new boolean [this.size()];
		
		int cnt = 0;
		for(int trial = 0 ; trial < 10*obj.maxsize; trial++) {
			int i = state.random[thread].nextInt(this.size());
			
			if(used[i]) continue;
			obj.add(this.get(i).lightClone());
			used[i] = true;
			
			cnt ++;
			
			if(cnt>=obj.maxsize) break;
		}
		
		return obj;
	}
	
	public void weightBoardItem() {
		//weight the board items by the fitness uniqueness
		
		//get the max board item size
		double maxboarditem = 0;
		for(BoardItem item : this) {
			if(item.size() > maxboarditem) {
				maxboarditem = item.size();
			}
		}
		
		int inverRank = this.size();
		double sum = 0;
		for(BoardItem item : this) {
			item.weight = maxboarditem * inverRank / item.size();
			inverRank -- ;
			sum += item.weight;
		}
		
		//normalize
		for(BoardItem item : this) {
			item.weight = item.weight / sum;
		}
		
	}
	
	public void randomShrinkItem(EvolutionState state, int thread, int batchsize) {
		for(BoardItem item : this) {
			while(item.size() > batchsize) {
				item.remove( state.random[thread].nextInt(item.size()) );
			}
		}
	}
	
	public void loadOutAnchors(double rate) {
		//record the worst BoardItem from the current board
		//rate: the final rate %
		clearAnchorsFromBoard();
		int n = this.size();
//		for(int i = n - 1; i>n*(1 - anchorRate); i--) 
		for(int i = n - 1; i>n*(1 - anchorRate*rate); i--) 
		{
			AnchorList.add( new AnchorItem(this.get(i)) );
		}
	}
	
	private void clearAnchorsFromBoard() {
		for(int i = this.size() - 1; i >= 0; i--) {
			if(this.get(i) instanceof AnchorItem) {
				this.remove(i);
			}
		}
	}
	
	public void reloadAnchors(EvolutionState state, int thread) {
		//re-load the anchors into the current board, treating them as part of the worst individuals
		//rate: the final rate %
		if(AnchorList.isEmpty()) return;
		int n = this.size();
		for(int i = 0; i<n*anchorRate; i++) {
			AnchorItem item = (AnchorItem) AnchorList.get( state.random[thread].nextInt(AnchorList.size())).clone();
			item.fitness = (Fitness) this.get(n-1).fitness.clone();
			super.add(item);
		}
	}
}
