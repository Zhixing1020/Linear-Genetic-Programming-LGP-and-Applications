package zhixing.cpxInd.algorithm.LandscapeOptimization.indexing;

import java.util.ArrayList;
import java.util.Collections;

import ec.EvolutionState;

public class GenoVector {
	
	public static final int None = -1;
	
	public int [] G;
	
	public final int length; 
	
	public final int minlength; //the minimum number of effective elements;
	
	protected ArrayList<Integer> toCheckList = new ArrayList<>();
	
	public ArrayList<Integer> getCheckList(){
		return toCheckList;
	}
	
	public GenoVector(int length, int minlength) {
		G = new int[length];
		
		this.length= length; 
		
		this.minlength = minlength;
	}
	
	public Object clone() {
		GenoVector obj = new GenoVector(this.length, this.minlength);
		
		for(int g = 0; g<length; g++) {
			obj.G[g] = G[g];
		}
		
		//we will not clone toCheckList here since toCheckList is temporarily used.
		
		return obj;
	}

	public void moveOnDirection(Direction dir, double step, ArrayList<Short> mask, EvolutionState state, int thread, int sym_rng) {
		
		ArrayList<Integer> mutateIndexes = new ArrayList<>();
		Direction tmpDirection = (Direction) dir.clone();
		
		
		//get the actual length of this geno vector
		ArrayList<Integer> origin_GVlist = new ArrayList<>();
		int len = 0;
		for(int i = 0;i<this.length; i++) {
			if(this.G[i] >= 0) {
				len ++;
				origin_GVlist.add(this.G[i]);
			}
			else break;
		}
		
		//align the direction with the geno vector
		int n = len - tmpDirection.size();
		for(int i = 0; i < n; i++) { //add more 0 into direction. the direction only change a part of elements
			int insert = state.random[thread].nextInt(tmpDirection.size());
			tmpDirection.add(insert, 0.);
		}
		n = tmpDirection.size() - len;
		for(int i = 0; i < n; i++) { //remove some elements from direction. the direction is redundant
			int tryi = state.random[thread].nextInt(tmpDirection.size() - 1);
			tmpDirection.set(tryi+1, tmpDirection.get(tryi) + tmpDirection.get(tryi + 1));
			tmpDirection.remove(tryi);
		}
		Direction.normalization(tmpDirection);
		//at this point, tmpDirection has the same number of elements as tmpGenoVector
		
		ArrayList<Integer> gvList = new ArrayList<>();  //to facilitate the genovector update
		
		//1) all elements move based on the direction
		//for each G[gi], if sizeDirection > 0, add symbols or move, else if sizeDirection < 0, remove symbols or move
		int num_instr_change = 1;
		toCheckList.clear();
		for(short i = 0; i<origin_GVlist.size(); i++) {
			//if(this.G[gi] == GenoVector.None) break;
			boolean toCheckValue = false;
			if(mask.contains((Object)i)) {
				boolean accept_add_remove = state.random[thread].nextDouble() < 0.5;
				if(tmpDirection.sizeDirection > 0 && accept_add_remove && origin_GVlist.size() + num_instr_change <= this.length) { //add symbols or move on direction
					gvList.add((int) Math.floor( origin_GVlist.get(i) + (-step + state.random[thread].nextDouble()*2*step)));
					mask.remove((Object) i);
					i --;
					num_instr_change ++;
					tmpDirection.sizeDirection --;
					toCheckValue = true;
					toCheckList.add(gvList.size() - 1);
				}
				else if(tmpDirection.sizeDirection < 0 && accept_add_remove && origin_GVlist.size() - num_instr_change >= this.minlength) {
					tmpDirection.sizeDirection ++;
					mask.remove((Object)i);
					num_instr_change ++;
				}
				else {
					gvList.add((int) Math.floor( origin_GVlist.get(i) + tmpDirection.get(i)*step ));
					mask.remove((Object)i);
					toCheckValue = true;
					if(tmpDirection.get(i) != 0) {
						toCheckList.add(gvList.size() - 1);
					}
				}
				
				
			}
			else {
				gvList.add(origin_GVlist.get(i));
			}
			
			if(toCheckValue && (gvList.get( gvList.size() -1 )<0 || gvList.get( gvList.size() -1 )>=sym_rng)) {
				gvList.set( gvList.size() -1 , state.random[thread].nextInt(sym_rng));
			}
		}
		
		//transform the gvList into the geno vector
		for(int i = 0; i<this.length; i++) {
			if(i<gvList.size()) {
				this.G[i] = gvList.get(i);
			}
			else {
				this.G[i] = GenoVector.None;
			}
		}
		
	}
	
	public void moveOnDirection(Direction dir, double step, EvolutionState state, int thread, int sym_rng){
		
//		ArrayList<Integer> mutateIndexes = new ArrayList<>();
		Direction tmpDirection = (Direction) dir.clone();
		
		
		//get the actual length of this geno vector
		ArrayList<Integer> origin_GVlist = new ArrayList<>();
		int len = 0;
		for(int i = 0;i<this.length; i++) {
			if(this.G[i] >= 0) {
				len ++;
				origin_GVlist.add(this.G[i]);
			}
			else break;
		}
		
		//align the direction with the geno vector
		int n = len - tmpDirection.size();
		for(int i = 0; i < n; i++) { //add more 0 into direction. the direction only change a part of elements
			int insert = state.random[thread].nextInt(tmpDirection.size()+1);
			tmpDirection.add(insert, 0.);
		}
		n = tmpDirection.size() - len;
		for(int i = 0; i < n; i++) { //remove some elements from direction. the direction is redundant
			if(tmpDirection.size() == 1) break;
			int tryi = state.random[thread].nextInt(tmpDirection.size() - 1);
			if(Math.abs(tmpDirection.get(tryi)) > Math.abs(tmpDirection.get(tryi + 1))) {
				tmpDirection.set(tryi+1, tmpDirection.get(tryi));
			}
			tmpDirection.remove(tryi);
		}
		//at this point, tmpDirection has the same number of elements as tmpGenoVector
		
		ArrayList<Integer> gvList = new ArrayList<>();  //to facilitate the genovector update
		
		
		//1) all elements move based on the direction
		//for each G[gi], if sizeDirection > 0, add symbols or move, else if sizeDirection < 0, remove symbols or move
		int num_instr_change = 1;		
		
		//move based on the direction
		toCheckList.clear();
		ArrayList<Integer> cache = new ArrayList<>(); //cache stores the new symbols
		boolean cacheActive = false;
		int num_instr_changed = 0; //the total changed number of instructions
		for(int i = 0; i<origin_GVlist.size(); i++) { //check each position of origin geno vector
			boolean toCheckValue = true;
			
			if(tmpDirection.sizeDirection > 0 && tmpDirection.get(i) != 0) {
				
				//initialize the cache
				if(!cacheActive) {
					cacheActive = true;
//					sD_cnt = tmpDirection.sizeDirection;
				}
				
				//push into the cache
				int newsym = moveSymbol(state, thread, origin_GVlist.get(i), tmpDirection.get(i), step);
				
				if(newsym < 0 || newsym >= sym_rng) {
					newsym = state.random[thread].nextInt(sym_rng);
				}
				
				cache.add(newsym);
			}
			if ((i == origin_GVlist.size() - 1 || tmpDirection.get(i) == 0) && !cache.isEmpty()) { //flush the cache
				
				int cnt = Math.min(cache.size(), tmpDirection.sizeDirection);
				cnt = Math.min(cnt, this.length - (origin_GVlist.size() + num_instr_changed)); //cnt is the number of legible changing instructions.
				
				int start = i - cnt; //start: the starting position of the to-be-added instructions
				int end = i;
				if(i == origin_GVlist.size() - 1 && tmpDirection.get(i) != 0) { //flush the cache because of reaching the end
					start ++;
					end ++;
				}
				
				//add "cnt" original symbols into cache. because we are adding instructions, we have to 
				for(int j = start; j<end; j++) {
					cache.add(origin_GVlist.get(j));
					num_instr_changed ++;
				}
				
				//flush the cache
				for(int j = 0; j<cache.size(); j++) {
					int newsym = cache.get(j);

					gvList.add(newsym);
					
					if(j < cache.size() - cnt) {
						toCheckList.add(gvList.size() - 1);
					}
				}
				
				tmpDirection.sizeDirection -= cnt;
				
				if(tmpDirection.get(i) == 0) { //if flush the cache because of meeting not-to-change symbols, roll back i for 1. 
					i --;
				}
				
				cache.clear();
				cacheActive = false;
				continue;
			}
			
			//default: tmpDirection.sizeDirection == 0
			if(!cacheActive) {
				int newsym = moveSymbol(state, thread, origin_GVlist.get(i), tmpDirection.get(i), step);
				
				if(newsym < 0 || newsym >= sym_rng) {
					newsym = state.random[thread].nextInt(sym_rng);
				}
				gvList.add(newsym);
				if(tmpDirection.get(i) != 0) {
					toCheckList.add(gvList.size() - 1);
				}
			}
			
			
		}
		
		//remove symbols
		while(tmpDirection.sizeDirection < 0 && origin_GVlist.size() - num_instr_change >= this.minlength) {
			int index = state.random[thread].nextInt(gvList.size());
			gvList.remove(index);
			
			tmpDirection.sizeDirection ++;
			num_instr_change ++;
			//update toCheckList
			for(int j = 0; j<toCheckList.size(); j++) {
				if(toCheckList.get(j) >= index) {
					toCheckList.set(j, toCheckList.get(j) - 1);
				}
			}
			toCheckList.remove( (Object) (index-1));
		}
		
		//transform the gvList into the geno vector
		for(int i = 0; i<this.length; i++) {
			if(i<gvList.size()) {
				this.G[i] = gvList.get(i);
			}
			else {
				this.G[i] = GenoVector.None;
			}
		}

	}
	
	static public int moveSymbol(EvolutionState state, int thread, int oldsym, double direction, double confiStep) {
		
//		return (int) Math.floor(oldsym + direction*state.random[thread].nextDouble()*confiStep);
		
		return (int) Math.floor(oldsym + Math.signum(direction) * Math.max(1, Math.abs(direction)*state.random[thread].nextDouble()*confiStep));

	}
}
