package zhixing.cpxInd.algorithm.multitask.MFEA.individual.reproduce;

import ec.BreedingSource;
import ec.EvolutionState;
import ec.Individual;
import ec.SelectionMethod;
import ec.breed.MultiBreedingPipeline;
import ec.gp.koza.CrossoverPipeline;
import ec.select.RandomSelection;
import ec.util.Parameter;
import zhixing.djss.algorithm.multitask.MFEA.evaluator.MFEA_Evaluator4DJSS;
import zhixing.djss.algorithm.multitask.MFEA.individual.LGPIndividual_MFEA4DJSS;
import zhixing.djss.individual.LGPIndividual4DJSS;

public class MFGPSBreedingPipeline extends MFEABreedingPipeline {
	
	//private TournamentSelection_ScalarRank selection;

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base); //this one is setup the things in MultiBreedingPipeline

//        selection = new TournamentSelection();
        
        selection = new TournamentSelection_ScalarRank();
        selection.setup(state, base); //selection is a new defined instance of TournamentSelection, if we want to use the parameter in TournamentSelection, we need setup selection.
    }
}
