package zhixing.cpxInd.algorithm.LandscapeOptimization.indexing;

import java.util.ArrayList;

import org.spiderland.Psh.booleanStack;
import org.spiderland.Psh.intStack;

import ec.EvolutionState;
import zhixing.cpxInd.algorithm.LandscapeOptimization.objectives.norm2Q;

public class Direction extends ArrayList<Double>{
	
	//Direction is not a specific direction from point A to point B. Instead, it is a template, indicating the number of elements that are to be changed,
	//the program size change, and the discrepancy. Adding a Direction to a specific point could generate different specific movement since we need to 
	//instantiate the direction (randomly completing or removing).
	
	public int sizeDirection = 0;  //>0: increase ``num'' of symbols, <0: remove ``num'' of symbols.

//	public ArrayList<Integer> toAddSymbols = new ArrayList<>();
//	public ArrayList<Integer> toRemoveSymbols = new ArrayList<>();
//	
	public Object clone() {
		Direction obj = new Direction();
		
		for(Double d : this ) {
			obj.add(d);
		}
		
//		for(Integer ind : toAddSymbols) {
//			obj.toAddSymbols.add(new Integer(ind));
//		}
//		for(Integer ind : toRemoveSymbols) {
//			obj.toRemoveSymbols.add(new Integer(ind));
//		}
		
		obj.sizeDirection = sizeDirection;
		
		return obj;
	}
	
	@Override
	public void clear() {
		super.clear();
		
//		toAddSymbols.clear();
//		toRemoveSymbols.clear();
	}
	
	public void setDirection(GenoVector pgDestination, GenoVector pgSource, EvolutionState state, int thread) {
		
		if(pgDestination.length != pgSource.length) {
			System.err.print("we got GenoVectors with inconsistent length in Direction: "+pgDestination.length+" and "+pgSource.length);
			System.exit(1);
		}
		
		//reset
		this.clear();
		
		//transform GenoVector
		ArrayList<Integer> pgDes_list = new ArrayList<>();
		ArrayList<Integer> pgSrc_list = new ArrayList<>();
		for(int i = 0; i<pgDestination.length; i++) {
			if(pgDestination.G[i]>=0) pgDes_list.add(pgDestination.G[i]);
			if(pgSource.G[i]>=0) pgSrc_list.add(pgSource.G[i]);
		}
		
		//align the length
		int len_des = pgDes_list.size(), len_src = pgSrc_list.size();
		sizeDirection = len_des - len_src;
		
		//get the difference
//		int num_element = Math.min(len_des, len_src);
//		//1) randomly select num_element elements from GenoVector
//		while(pgDes_list.size() > num_element) {
//			int index = state.random[thread].nextInt(pgDes_list.size());
//			toAddSymbols.add(pgDes_list.get(index));
//			pgDes_list.remove(index);
//		}
//		while(pgSrc_list.size() > num_element) {
//			int index = state.random[thread].nextInt(pgSrc_list.size());
//			toRemoveSymbols.add(pgSrc_list.get(index));
//			pgSrc_list.remove(index);
//		}
		
		//1) align pgDes_list and pgSrc_list
		int num_element = len_src;
		while(pgDes_list.size() > num_element) {
			int index = state.random[thread].nextInt(pgDes_list.size());
//			toAddSymbols.add(pgDes_list.get(index));
			pgDes_list.remove(index);
		}
		while(pgDes_list.size() < num_element) {
			int insert = state.random[thread].nextInt(pgDes_list.size()+1);
			pgDes_list.add(insert, -1);
		}
		
		//2) difference
		boolean allZero = true;
		for(int k = 0; k<num_element; k++) {
			if(pgDes_list.get(k) >= 0) {
				this.add((double) (pgDes_list.get(k) - pgSrc_list.get(k)));
				if(pgDes_list.get(k) - pgSrc_list.get(k) != 0){
					allZero = false;
				}
			}
			else {
				this.add(0.0);
			}
		}
		if(allZero) {
			this.set(state.random[thread].nextInt(this.size()), Math.pow(-1, state.random[thread].nextInt(2)));
		}
		
		
		normalization(this);
		
	}
	
	public void setDirection(GenoVector pgDestination, GenoVector pgSource, EvolutionState state, int thread, int sym_rng) {
		
		if(pgDestination.length != pgSource.length) {
			System.err.print("we got GenoVectors with inconsistent length in Direction: "+pgDestination.length+" and "+pgSource.length);
			System.exit(1);
		}
		
		//reset
		this.clear();
		
		//transform GenoVector
		ArrayList<Integer> pgDes_list = new ArrayList<>();
		ArrayList<Integer> pgSrc_list = new ArrayList<>();
		for(int i = 0; i<pgDestination.length; i++) {
			if(pgDestination.G[i]>=0) pgDes_list.add(pgDestination.G[i]);
			if(pgSource.G[i]>=0) pgSrc_list.add(pgSource.G[i]);
		}
		
		//align the length
		int len_des = pgDes_list.size(), len_src = pgSrc_list.size();
		sizeDirection = len_des - len_src;
		
		//1) align pgDes_list and pgSrc_list
		int num_element = len_src;
		while(pgDes_list.size() > num_element) {
			int index = state.random[thread].nextInt(pgDes_list.size());
//			toAddSymbols.add(pgDes_list.get(index));
			pgDes_list.remove(index);
		}
		while(pgDes_list.size() < num_element) {
			int insert = state.random[thread].nextInt(pgDes_list.size()+1);
			pgDes_list.add(insert, -1);
		}
		
		//2) difference
		boolean allZero = true;
		for(int k = 0; k<num_element; k++) {
			if(pgDes_list.get(k) >= 0) {
				this.add((double) (pgDes_list.get(k) - pgSrc_list.get(k)));
				if(pgDes_list.get(k) - pgSrc_list.get(k) != 0){
					allZero = false;
				}
			}
			else {
				this.add(0.0);
			}
		}
		if(allZero) {
			int index = state.random[thread].nextInt(this.size());
			this.set(index, (double) (state.random[thread].nextInt(sym_rng) - pgDes_list.get(index)));
		}
		
		
//		normalization(this);
		
	}
	
	public void setDirection(GenoVector pgDestination, GenoVector pgSource, EvolutionState state, int thread, int sym_rng, int masksize) {
		
		if(pgDestination.length != pgSource.length) {
			System.err.print("we got GenoVectors with inconsistent length in Direction: "+pgDestination.length+" and "+pgSource.length);
			System.exit(1);
		}
		
		//reset
		this.clear();
		
		//transform GenoVector
		ArrayList<Integer> pgDes_list = new ArrayList<>();
		ArrayList<Integer> pgSrc_list = new ArrayList<>();
		for(int i = 0; i<pgDestination.length; i++) {
			if(pgDestination.G[i]>=0) pgDes_list.add(pgDestination.G[i]);
			if(pgSource.G[i]>=0) pgSrc_list.add(pgSource.G[i]);
		}
		sizeDirection = pgDes_list.size() - pgSrc_list.size();
		
		//mask the pgDestination to highlight some building blocks
		while(pgDes_list.size() > masksize) {
			int remove = state.random[thread].nextInt(pgDes_list.size());
			pgDes_list.remove(remove);
		}
		while(pgDes_list.size() < masksize) {
			int insert = state.random[thread].nextInt(pgDes_list.size()+1);
			pgDes_list.add(insert, -1);
		}
		
		//align the length
		int len_des = pgDes_list.size(), len_src = pgSrc_list.size();
		
		
		//1) align pgDes_list and pgSrc_list
		int num_element = len_src;
		while(pgDes_list.size() > num_element) {
			int index = state.random[thread].nextInt(pgDes_list.size());
//			toAddSymbols.add(pgDes_list.get(index));
			pgDes_list.remove(index);
		}
		while(pgDes_list.size() < num_element) {
			int insert = state.random[thread].nextInt(pgDes_list.size()+1);
			pgDes_list.add(insert, -1);
		}
		
		//2) difference
		boolean allZero = true;
		for(int k = 0; k<num_element; k++) {
			if(pgDes_list.get(k) >= 0) {
				this.add((double) (pgDes_list.get(k) - pgSrc_list.get(k)));
				if(pgDes_list.get(k) - pgSrc_list.get(k) != 0){
					allZero = false;
				}
			}
			else {
				this.add(0.0);
			}
		}
		if(allZero) {
			int index = state.random[thread].nextInt(this.size());
			this.set(index, (double) (state.random[thread].nextInt(sym_rng) - pgDes_list.get(index)));
		}
		
		
//		normalization(this);
		
	}
	
	public static void normalization(ArrayList<Double> dir) {
		if(dir.size() == 0) return;
		
		double norm = 0;
		
		for(Double d : dir) {
			norm += d*d;
		}
		
		norm = Math.sqrt(norm);
		
		for(int i = 0; i<dir.size(); i++) {
			dir.set(i, dir.get(i)/norm);
		}
		
		return;
	}
	
	public static double Cosine_direction(GenoVector des1, GenoVector src1, GenoVector des2, GenoVector src2, EvolutionState state, int thread) {
		//Direction itself cannot calculate cosine distance, but GenoVector can.
		double norm1 = Math.sqrt(norm2Q.Q(des1, src1));
		double norm2 = Math.sqrt(norm2Q.Q(des2, src2));
		
		Direction dir1 = new Direction();
		Direction dir2 = new Direction();
		
		dir1.setDirection(des1, src1, state, thread);
		dir2.setDirection(des2, src2, state, thread);
		
		double innerProd = 0;
		for(int i = 0; i<Math.min(dir1.size(), dir2.size()); i++) {			
			innerProd += norm2Q.subQ(des1.G[i], src1.G[i])*norm2Q.subQ(des2.G[i], src2.G[i]);
		}
		
		return innerProd / (norm1*norm2);
	}
}
