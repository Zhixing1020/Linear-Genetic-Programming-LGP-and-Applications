package zhixing.cpxInd.algorithm.LandscapeOptimization.FLReductionLGP.individual.reproduce;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Individual;
import zhixing.cpxInd.algorithm.LandscapeOptimization.SubpopulationFLO;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Board;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.Direction;
import zhixing.cpxInd.algorithm.LandscapeOptimization.indexing.GenoVector;
import zhixing.cpxInd.algorithm.LandscapeOptimization.simpleLGP.indexing.IndexList4LGP;
import zhixing.cpxInd.individual.GPTreeStruct;
import zhixing.cpxInd.individual.LGPIndividual;
import zhixing.cpxInd.individual.primitive.WriteRegisterGPNode;

public class NeighborhoodSearch_noFLO extends NeighborhoodSearchFast{

	@Override
	public LGPIndividual all_in_one_produce(int subpopulation, Individual[] inds, EvolutionState state,
			int thread, Individual[] parents) {
		
		//update GenoVector
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
		master_i = 0;
		int progLength = 0;
		GenoVector pgSource; 
		
		progLength = ((LGPIndividual) parents[master_i]).getTreesLength();

		Direction direction = new Direction();
		
		int mask_size = 1 + state.random[thread].nextInt(Math.min(maxMaskSize, progLength));
		
		for(int d = 0; d<((LGPIndividual) parents[master_i]).getMaxNumTrees(); d++) {
			if(d < progLength) {
				direction.add(-1.);  //a place holder, indicating the reduction on symbol indices.
			}
			else {
				direction.add(0.);
			}
		}
		
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
		if(rate <= addRate || progLength == ((LGPIndividual) parents[master_i]).getMinNumTrees()) {
			direction.sizeDirection = state.random[thread].nextInt(Math.min(maxMacroSize, cntNonZero) + 1);
		}
		else if (rate <= addRate + removeRate || progLength == ((LGPIndividual) parents[master_i]).getMaxNumTrees()) {
			direction.sizeDirection = -1*state.random[thread].nextInt(Math.min(maxMacroSize, cntNonZero) + 1);
		}
		
		if(mask_size > Math.abs(direction.sizeDirection)) {
			mask_size -= Math.abs(direction.sizeDirection);
		}
		else if(Math.abs(direction.sizeDirection) > mask_size) {
			if(direction.sizeDirection > 0) {
				direction.sizeDirection -= mask_size;
			}
			//else, direction.sizeDirection < 0  we don't care
		}
		else { // mask_size == Math.abs(direction.sizeDirection)
			if(state.random[thread].nextDouble() < 0.5) {
				mask_size = 0;
			}
			else {
				direction.sizeDirection = 0;
			}
		}
		
//		pgSource.moveOnDirection(direction, maxStep, state, thread, indexlist.size());
		
//		return pgSource;
		
		//update LGP individual based on direction
		ArrayList<GPTreeStruct> checkList = new ArrayList<>();
		//1. update instructions based on direction
		LGPIndividual newind = (LGPIndividual) parents[master_i].clone();	
		for(int i = 0; i<newind.getTreesLength(); i++) {
			if(direction.get(i) != 0) {
				//get the symbol index of this instruction
//				GPTreeStruct instr = newind.getTreeStruct(i);
//				int symIndex = indexlist.getIndexBySymbol(instr);
//				
//				//get the new index
//				int newsym = GenoVector.moveSymbol(state, thread, symIndex, direction.get(i) * symIndex, maxStep);
//				
//				if(newsym < 0 || newsym >= indexlist.size()) {
//					newsym = state.random[thread].nextInt(indexlist.size());
//				}
				int newsym = state.random[thread].nextInt(indexlist.size());
				//get the new instruction and set it into the program
				GPTreeStruct newInstr = indexlist.getSymbolByIndex(newsym, state, thread);
				
				newind.setTree(i, newInstr);
				checkList.add(newInstr);
			}
			
		}
		newind.updateStatus();
		
		//2. add or remove random instructions
		while(direction.sizeDirection > 0 && newind.getTreesLength() < newind.getMaxNumTrees()) {
			int newsym = state.random[thread].nextInt(indexlist.size());
			GPTreeStruct newInstr = indexlist.getSymbolByIndex(newsym, state, thread);
			
			int newindex = state.random[thread].nextInt(newind.getTreesLength());
			newind.addTree(newindex, newInstr);
			checkList.add(newInstr);
			direction.sizeDirection --;
		}
		while(direction.sizeDirection <0 && newind.getTreesLength() > newind.getMinNumTrees()) {
			int newindex = state.random[thread].nextInt(newind.getTreesLength());
			newind.removeTree(newindex);
			direction.sizeDirection ++;
		}
		
		//3. check the effectiveness of the updated instructions
		if(newind.getTreesLength() < newind.getMinNumTrees() || newind.getTreesLength() > newind.getMaxNumTrees()
				|| newind.getEffTreesLength() < 1) {
			newind.rebuildIndividual(state, thread);
		}
		if(checkList.size() > 0) {
			GPTreeStruct instr  = checkList.get(state.random[thread].nextInt(checkList.size()));
			if(!instr.status) {
				
				ArrayList<Integer> list = new ArrayList<>(instr.effRegisters);
				if(!list.isEmpty()) {
					((WriteRegisterGPNode)instr.child).setIndex(list.get(state.random[thread].nextInt(list.size())));
				}
				newind.updateStatus();
			}
		}
		
		return newind;
	}
}
