package zhixing.cpxInd.algorithm.LandscapeOptimization.FLReductionLGP.individual.reproduce;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Individual;
import zhixing.cpxInd.algorithm.LandscapeOptimization.SubpopulationFLO;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Direction;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.IndexList;
import zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.indexing.IndexList4LGP;
import zhixing.cpxInd.algorithm.LandscapeOptimization.FLReductionLGP.indexing.IndexList4LGP_FLR;
import zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.individual.reproduce.NeighborhoodSearch4LGP;
import zhixing.cpxInd.individual.CpxGPIndividual;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;

public class NeighborhoodSearchFLR4LGP extends NeighborhoodSearch4LGP {

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
//			leadBoard.resetGenoAngle(indexlist, state.generation);
//			leadBoard.resetGenoDifference(indexlist, state.generation);
			
			cur_generation = state.generation;
		}
		
//		double mincos = leadBoard.getMinCosine();
//		double maxstep = leadBoard.getMaxDiff();
		
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
		
		progLength = ((LGPIndividual) parents[master_i]).getTreesLength();

		Direction direction = new Direction();
//		direction.setDirection(pgMaster, pgSource, state, thread);
//		direction.setDirection(pgMaster, pgSlaver, state, thread, indexlist.size());
		
		int mask_size = 1 + state.random[thread].nextInt(Math.min(maxMaskSize, progLength));
		//direction.setDirection(pgMaster, pgSource, state, thread, indexlist.size(), mask_size);
		
		for(int d = 0; d<pgSource.length; d++) {
			if(pgSource.G[d] >= 0) {
				direction.add(-1.*pgSource.G[d]);
			}
			else {
				direction.add(0.);
			}
		}
		
		//randomly highlight direction elements
//		int mask_size = 1 + state.random[thread].nextInt(Math.min(maxMaskSize, progLength));
		
		//pick "mask_size" non-zero elements from direction
		int cntNonZero = 0;
		for(int i = 0; i<direction.size(); i++) {
			if(direction.get(i) != 0) {
				cntNonZero ++;
//				double dir = direction.get(i);
//				direction.set(i, dir * (Math.max(Math.abs(dir), 100) / Math.abs(dir)));
			}
		}
		int index = state.random[thread].nextInt(direction.size());
		int cn = cntNonZero;
		for(int cnt = 0; cn > mask_size && cnt < direction.size(); cnt++) {
			if(direction.get(index) != 0) {
				direction.set(index, 0.);
				cn --;
			}
			index = (index + 1)%direction.size();
		}

		double rate = state.random[thread].nextDouble();
		if(rate <= addRate) {
			direction.sizeDirection = state.random[thread].nextInt(Math.min(maxMacroSize, cntNonZero) + 1);
		}
		else if (rate <= addRate + removeRate) {
			direction.sizeDirection = -1*state.random[thread].nextInt(Math.min(maxMacroSize, cntNonZero) + 1);
		}
		
//		double step = state.random[thread].nextDouble()*maxStep;
//		
//		pgSource.moveOnDirection(direction, step, state, thread, indexlist.size());
		
		pgSource.moveOnDirection(direction, maxStep, state, thread, indexlist.size());
		
		return pgSource;
	}
	
	@Override
	protected LGPIndividual buildIndividualBasedGenoVector(int subpopulation, EvolutionState state,
			int thread, Individual[] parents, GenoVector gv) {
		
		IndexList4LGP_FLR indexlist = (IndexList4LGP_FLR) ((SubpopulationFLO)state.population.subpops[subpopulation]).IndList;
		LGPIndividual newind = (LGPIndividual) parents[master_i].clone();				
		
		for(int genoi = 0, progi = 0; genoi<gv.length; genoi++, progi++) {
			
			if(gv.G[genoi] == GenoVector.None) {
				//check whether we need to remove instruction
				int numTrees = newind.getTreesLength();
				if(progi < numTrees) { //remove
					for(int i = 0; i < numTrees - progi; i++) {
						newind.removeTree(progi);
					}
				}
				
				break;
			}
			
			GPTreeStruct instr = (GPTreeStruct) ((GPTreeStruct) indexlist.getRandSymbolByIndex(gv.G[genoi], state, thread, subpopulation)).clone();
			
			if(progi < newind.getTreesLength()) {
				newind.setTree(progi, instr);
			}
			else {
				newind.addTree(progi, instr);
			}
			
		}
		
		return newind;
	}
	
	@Override
	protected boolean maintainPhenotype(EvolutionState state, int thread, LGPIndividual oldind, LGPIndividual newind, GenoVector newgv) {
		//the instructions covered by mask should be effective
		boolean res = false;
		
		if(newind.getTreesLength() < newind.getMinNumTrees() || newind.getTreesLength() > newind.getMaxNumTrees()
				|| newind.getEffTreesLength() < 1) return false;
		
		double eff_rate = (double)newind.getEffTreesLength() / newind.getTreesLength();
//		for(int i = newgv.getCheckList().size() - 1; i>=0; i--) {
//			
//		}
		
		if(newgv.getCheckList().size() > 0) {
			int ind = newgv.getCheckList().get(state.random[thread].nextInt(newgv.getCheckList().size()));
			if(!newind.getTreeStruct(ind).status) {
				
				ArrayList<Integer> list = new ArrayList<>(newind.getTreeStruct(ind).effRegisters);
				if(!list.isEmpty()) {
					((WriteRegisterGPNode)newind.getTreeStruct(ind).child).setIndex(list.get(state.random[thread].nextInt(list.size())));
				}
				newind.updateStatus();
			}
		}
		
		
//		double new_eff_rate = (double)newind.getEffTreesLength() / newind.getTreesLength();
//		if(eff_rate - new_eff_rate < 0.3) {
//			res = true;
//		}
		

		
		return res;
	}
}
