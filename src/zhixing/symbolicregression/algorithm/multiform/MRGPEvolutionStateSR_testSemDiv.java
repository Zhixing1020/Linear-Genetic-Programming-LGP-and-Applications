package zhixing.symbolicregression.algorithm.multiform;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.gp.GPProblem;
import ec.util.QuickSort;
import ec.util.SortComparatorL;
import zhixing.symbolicregression.individual.SREvolutionState;
import zhixing.symbolicregression.optimization.GPSymbolicRegression;

public class MRGPEvolutionStateSR_testSemDiv extends SREvolutionState{

	//this EvolutionState is for ECJ first round review, checking the semantic diversity over top 20 individuals at the final generation.
	@Override
	 public void finish(int result) 
    {
		
		//get the semantics of the top 20 individuals (or 10 for each representation)
		int topnum = 20; //collect the top 20 individuals
		Individual [] eliteinds = new Individual [topnum];
		ArrayList<double[]> outputs = new ArrayList<>();
		int sub_topnum = topnum / this.population.subpops.length;
		for(int sub = 0; sub < this.population.subpops.length; sub++) {
			//sort the population
			int[] orderedPop = new int[this.population.subpops[sub].individuals.length];
            for(int x=0;x<this.population.subpops[sub].individuals.length;x++) orderedPop[x] = x;

            // sort the best so far where "<" means "not as fit as"
            QuickSort.qsort(orderedPop, new EliteComparator(this.population.subpops[sub].individuals));       
		
			for(int x = 0; x < sub_topnum; x++) {
				eliteinds[sub*sub_topnum + x] = (Individual) this.population.subpops[sub].individuals[orderedPop[x]].clone();
				eliteinds[sub*sub_topnum + x].evaluated = false;
			}
		}
		
		//get the outputs of the elite individuals
		for(int x = 0; x<eliteinds.length; x++) {
			double [] outs = ((GPSymbolicRegression)this.evaluator.p_problem).getOutputs(eliteinds[x]);
			outputs.add(outs);
		}
		
		double semdiv = 0;
		for(int i = 0; i<outputs.size(); i++) {
			for(int cnt = 1; cnt<outputs.size(); cnt++) {
				int j = (i + cnt) % outputs.size();
				semdiv += EuclideanDis(outputs.get(i), outputs.get(j));
			}
		}
		
		//output the semantic diversity
		String filepath = System.getProperty("user.dir");
		try {
			
			FileWriter f = new FileWriter( filepath + "\\semantic_diversity.txt", true);
			
			BufferedWriter bw = new BufferedWriter(f);
			
//			for(SampleItem item : itemlist) {
//				bw.write(""+item.d+"\t"+item.Detailfit+"\n");
//			}
			
			bw.write(semdiv+"\n");
			
			bw.flush();
			
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    //Output.message("Finishing");
	    /* finish up -- we completed. */
	    statistics.finalStatistics(this,result);
	    finisher.finishPopulation(this,result);
	    exchanger.closeContacts(this,result);
	    evaluator.closeContacts(this,result);
    }
	
	static class EliteComparator implements SortComparatorL
    {
	    Individual[] inds;
	    public EliteComparator(Individual[] inds) {super(); this.inds = inds;}
	    public boolean lt(long a, long b)
	        { return inds[(int)b].fitness.betterThan(inds[(int)a].fitness); }
	    public boolean gt(long a, long b)
	        { return inds[(int)a].fitness.betterThan(inds[(int)b].fitness); }
    }
	
	double EuclideanDis(double a [], double [] b) {
		double res = 0;
		
		if(a == null || b == null || a.length != b.length) {
			System.err.print("we are comparing inconsistent arrays\n");
			System.exit(1);
		}
		
		for(int i = 0; i<a.length; i++) {
			res += (a[i] - b[i]) * (a[i] - b[i]);
		}
		res = Math.sqrt(res / a.length) ;
		
		return res;
	}
}
