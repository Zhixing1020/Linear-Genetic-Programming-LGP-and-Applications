package zhixing.cpxInd.algorithm.LandscapeOptimization.indexing;

import ec.Fitness;
import zhixing.cpxInd.individual.CpxGPIndividual;
//import zhixing.jss.cpxInd.individual.CpxGPIndividual;

public class AnchorItem extends BoardItem{
	//a special kind of BoardItem that is always treated to be worse than other board items
	
	public AnchorItem(CpxGPIndividual ind) {
		super(ind);
	}

	//because the fitnesses of DJSS from different generations are not comparable, we use the worst fitness value in the current board to reset the fitness
	public AnchorItem(CpxGPIndividual ind, Fitness fitness) {
		super(ind);
		
		this.fitness = (Fitness) fitness.clone();
	}
	
	public AnchorItem(BoardItem item) {
		super((CpxGPIndividual) item.get(0).clone());
		
		for(int i = 1; i<this.size(); i++) {
			this.add((CpxGPIndividual) item.get(i).clone());
		}
		
		this.weight = item.weight;
	}
}
