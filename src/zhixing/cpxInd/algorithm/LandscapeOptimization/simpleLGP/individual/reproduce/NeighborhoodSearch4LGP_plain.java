package zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.individual.reproduce;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Individual;
import zhixing.cpxInd.algorithm.LandscapeOptimization.SubpopulationFLO;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Direction;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.indexing.IndexList4LGP;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.LGPIndividual;

public class NeighborhoodSearch4LGP_plain extends NeighborhoodSearch4LGP{
	//this operator is just designed for RandomNS_Neighbor

	@Override
	public int produce(int min, int max, int start, int subpopulation, Individual[] inds, EvolutionState state,
			int thread, Individual[] parents) {
		
		GenoVector newgv = updateGenoVector(subpopulation, inds, state, thread, parents);
		
		//generate offspring based on the numerical genotype vector
		inds[start] = buildIndividualBasedGenoVector(subpopulation, state, thread, parents, newgv);
		
//		for(int tr = 0; tr < numTries; tr++) {
			
			
//			if(maintainPhenotype(state, thread, (LGPIndividual) parents[master_i], (LGPIndividual)inds[start], newgv)) 
//				break;
//		}

				
		return INDS_PRODUCED;
	}
	
	@Override
	public GenoVector updateGenoVector(final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread,
	        final Individual[] parents) {
		Board board = ((SubpopulationFLO)state.population.subpops[subpopulation]).fullBoard;
		IndexList4LGP indexlist = (IndexList4LGP) ((SubpopulationFLO)state.population.subpops[subpopulation]).IndList;
		
		//get the maximum angles (minimum cosine distance), get the maximum step
		if(leadBoard == null || cur_generation != state.generation) {
			leadBoard = board.lightTrimCloneByBest();
//			leadBoard.trim2MaxsizeByBest();
			leadBoard.resetGenoAngle(indexlist, state.generation);
			leadBoard.resetGenoDifference(indexlist, state.generation);
			
			cur_generation = state.generation;
		}
		
		double mincos = leadBoard.getMinCosine();
		double maxstep = leadBoard.getMaxDiff();
		
		//get the genotype vector of parents
		GenoVector pgSlaver, pgMaster, pgSource; 
		master_i = 0;
		int progLength = 0;
//		if(parents[master_i].fitness.betterThan(parents[(master_i + 1)%parents.length].fitness)) {
////			pgMaster = indexlist.getEffectiveGenoVector((LGPIndividual) parents[master_i]);
////			pgSlaver = indexlist.getEffectiveGenoVector((LGPIndividual) parents[(master_i + 1)%parents.length]);
//			pgMaster = indexlist.getGenoVector((CpxGPIndividual) parents[master_i]);
//			pgSlaver = indexlist.getGenoVector((CpxGPIndividual) parents[(master_i + 1)%parents.length]);
//			pgSource = (GenoVector) pgMaster.clone();
//		}
//		else {
////			pgMaster = indexlist.getEffectiveGenoVector((LGPIndividual) parents[(master_i + 1)%parents.length]);
////			pgSlaver = indexlist.getEffectiveGenoVector((LGPIndividual) parents[master_i]);
//			pgMaster = indexlist.getGenoVector((CpxGPIndividual) parents[(master_i + 1)%parents.length]);
//			pgSlaver = indexlist.getGenoVector((CpxGPIndividual) parents[master_i]);
//			pgSource = (GenoVector) pgSlaver.clone();
//		}
		
		pgSource = indexlist.getGenoVector((CpxGPIndividual) parents[master_i]);
//		pgMaster = indexlist.getGenoVector((CpxGPIndividual) parents[(master_i + 1)%parents.length]);
//		
//		progLength = ((LGPIndividual) parents[master_i]).getTreesLength();
//
//		Direction direction = new Direction();
////		direction.setDirection(pgMaster, pgSource, state, thread);
////		direction.setDirection(pgMaster, pgSlaver, state, thread, indexlist.size());
//		
//		int mask_size = 1 + state.random[thread].nextInt(Math.min(maxMaskSize, progLength));
//		direction.setDirection(pgMaster, pgSource, state, thread, indexlist.size(), mask_size);
//		
//		//randomly highlight direction elements
////		int mask_size = 1 + state.random[thread].nextInt(Math.min(maxMaskSize, progLength));
//		
//		//pick "mask_size" non-zero elements from direction
//		int cntNonZero = 0;
//		for(int i = 0; i<direction.size(); i++) {
//			if(direction.get(i) != 0) {
//				cntNonZero ++;
//			}
//		}
//		int index = state.random[thread].nextInt(direction.size());
//		int cn = cntNonZero;
//		for(int cnt = 0; cn > mask_size && cnt < direction.size(); cnt++) {
//			if(direction.get(index) != 0) {
//				direction.set(index, 0.);
//				cn --;
//			}
//			index = (index + 1)%direction.size();
//		}
//
//		double rate = state.random[thread].nextDouble();
//		if(rate <= addRate) {
//			direction.sizeDirection = state.random[thread].nextInt(Math.min(maxMacroSize, cntNonZero) + 1);
//		}
//		else if (rate <= addRate + removeRate) {
//			direction.sizeDirection = -1*state.random[thread].nextInt(Math.min(maxMacroSize, cntNonZero) + 1);
//		}
//		
////		double step = state.random[thread].nextDouble()*maxStep;
////		
////		pgSource.moveOnDirection(direction, step, state, thread, indexlist.size());
//		
//		pgSource.moveOnDirection(direction, maxStep, state, thread, indexlist.size());
		
		ArrayList<Integer> origin_GVlist = new ArrayList<>();
		int len = 0;
		for(int i = 0;i<pgSource.length; i++) {
			if(pgSource.G[i] >= 0) {
				len ++;
				origin_GVlist.add(pgSource.G[i]);
			}
			else break;
		}
		
		double rate = state.random[thread].nextDouble();
		if(rate <= addRate && origin_GVlist.size() < pgSource.G.length) {
			int index = state.random[thread].nextInt(origin_GVlist.size()+1);
			int newsym = state.random[thread].nextInt(indexlist.size());
			origin_GVlist.add(index, newsym);
		}
		else if (rate <= addRate + removeRate && origin_GVlist.size() > ((LGPIndividual) parents[master_i]).getMinNumTrees()) {
			int index = state.random[thread].nextInt(origin_GVlist.size());
			origin_GVlist.remove(index);
		}
		else {
			int index = state.random[thread].nextInt(origin_GVlist.size());
			int newsym = (int) (origin_GVlist.get(index) + Math.pow(-1, state.random[thread].nextInt(2)) * state.random[thread].nextDouble()*maxStep);
			if(newsym < 0 || newsym >= indexlist.size()) {
				newsym = state.random[thread].nextInt(indexlist.size());
			}
			
			origin_GVlist.set(index, newsym);
		}
		
		//transform the gvList into the geno vector
		for(int i = 0; i<pgSource.G.length; i++) {
			if(i<origin_GVlist.size()) {
				pgSource.G[i] = origin_GVlist.get(i);
			}
			else {
				pgSource.G[i] = GenoVector.None;
			}
		}

		
		return pgSource;
	}
}
