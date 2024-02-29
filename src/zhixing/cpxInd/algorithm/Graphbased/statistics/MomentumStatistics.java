package zhixing.cpxInd.algorithm.Graphbased.statistics;

import ec.EvolutionState;
import ec.Individual;
import ec.SelectionMethod;
import ec.gp.koza.KozaShortStatistics;
import ec.multiobjective.MultiObjectiveFitness;
import ec.select.TournamentSelection;
import ec.util.Parameter;
import zhixing.cpxInd.algorithm.Graphbased.individual.LGPIndividual4Graph;
import zhixing.cpxInd.algorithm.multitask.MFEA.individual.reproduce.TournamentSelection_ScalarRank;

public class MomentumStatistics extends KozaShortStatistics {
	@Override
	public void postEvaluationStatistics(final EvolutionState state) {
		//this method will directly use the output log which has been specified in SimpleShortStatistic.setup()
		//since this method does not call super.postEvaluationStatistics(), it cannot call its children.
		//so, do append stat children to this object in the parameter file.
		//in fact, a better way may be re-write a class equal to SimpleShortStatistics or modify the code of SimpleShortStatistics
		boolean output = (state.generation % modulus == 0);

        
        int subpops = state.population.subpops.length;                          // number of supopulations
        
        double Norm1_mom = 0;
        double Norm1_detailAM = 0;
        
        int funnum = 6;
        int dimension = 22;
        double [] avg_AM = new double [funnum * dimension]; 
        double [] avg_frequency = new double [dimension];
        
        prepareStatistics(state);

        // gather per-subpopulation statistics
                
        for(int x=0;x<subpops;x++)
            {            
        	
//        	for(int i = 0;i<state.population.subpops[x].individuals.length;i++){
//            	Norm1_mom += ((LGPIndividual4Graph)state.population.subpops[x].individuals[i]).getNorm1_Momentum();
//            	Norm1_detailAM += ((LGPIndividual4Graph)state.population.subpops[x].individuals[i]).getNorm1_detailAM();
//            }
//        	Norm1_mom /= state.population.subpops[x].individuals.length;
//        	Norm1_detailAM /= state.population.subpops[x].individuals.length;
        	double popsize = state.population.subpops[x].individuals.length;
        	SelectionMethod selection = new TournamentSelection();
        	selection.setup(state, new Parameter("select.tournament.size"));
        	for(int i = 0;i<25;i++){
        		int ind = selection.produce(x, state, 0);
        		//int ind = (int) (popsize - 1 - i);  //this line of code is applied when elitic size == 25
//        		int [][] tmp_AM = ((LGPIndividual4Graph)state.population.subpops[x].individuals[ind]).getAM();
//        		for(int p = 0;p<funnum;p++){
//        			for(int q = 0;q<dimension;q++){
//        				avg_AM [p*dimension + q] += tmp_AM[p][q] / popsize;
//        			}
//        		}
        		double [] tmp_freq = ((LGPIndividual4Graph)state.population.subpops[x].individuals[ind]).getFrequency(15, 19); //specifically selecting
        		//the frequency between 15th and 19th instructions is for testing.
    			for(int q = 0;q<dimension;q++){
    				avg_frequency [q] += tmp_freq[q] / 25;
    			}
        	}
            }

                
        // hook for KozaShortStatistics etc.
        //if (output) printExtraPopStatisticsBefore(state);

                
        // print out fitness info
        if (output)
            { 
//            state.output.print(""+ Norm1_mom + "\t" 
//            					+ Norm1_detailAM, statisticslog);
//        	for(int i = 0;i<funnum*dimension;i++){
//        		state.output.print(""+ avg_AM[i] + "\t", statisticslog);
//        	}
        	for(int i = 0;i<dimension;i++){
        		state.output.print(""+ avg_frequency[i] + "\t", statisticslog);
    		}
            }
                        
        // hook for KozaShortStatistics etc.
        //if (output) printExtraPopStatisticsAfter(state);

        // we're done!
        if (output) state.output.println("", statisticslog);
	}
}
