package zhixing.cpxInd.algorithm.LandscapeOptimization.indexing;

import java.util.ArrayList;
import java.util.Comparator;

import ec.Fitness;
import zhixing.cpxInd.individual.CpxGPIndividual;
//import zhixing.jss.cpxInd.individual.CpxGPIndividual;

public class BoardItem extends ArrayList<CpxGPIndividual> implements Comparable<BoardItem> {
	//an item of the leading board, including fitness and individuals
	
	public Fitness fitness;
	
	public double weight = 1.0;
	
	public BoardItem(CpxGPIndividual ind) {
		fitness = (Fitness) ind.fitness.clone();
		super.add(ind);
	}

	@Override
	public int compareTo(BoardItem o2) {
		
		if(this.fitness.betterThan(o2.fitness)) {
			return -1;
		}
		else if(this.fitness.equivalentTo(o2.fitness)) {
			return 0;
		}
		
		return 1;
	}
	
	@Override
	public boolean equals(Object o1) {
		
		if(fitness.equivalentTo((Fitness) o1)) {
			return true;
		}
		
		return false;
		
	}

	
	@Override
	public boolean add(CpxGPIndividual ind) {
		if(ind.fitness.equivalentTo(this.fitness)) {
			return super.add(ind);
		}
		
		return false;
		
	}
	
	public BoardItem lightClone() {
		BoardItem item = new BoardItem(this.get(0));
		
		for(int i = 1; i<this.size(); i++) {
			item.add(this.get(i));
		}
		
		item.weight = weight;
		
		return item;
	}
}
